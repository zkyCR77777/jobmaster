package com.example.client.data.repository

import com.example.client.BuildConfig
import com.example.client.data.AppModule
import com.example.client.data.simulatedResponses
import com.example.client.data.remote.ChatSendMessageRequest
import com.example.client.data.remote.NetworkClient
import com.example.client.data.remote.SmartPactApi
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okhttp3.Request
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull

data class ChatReplySnapshot(
    val sessionId: String?,
    val messageId: String? = null,
    val detectedModule: AppModule,
    val assistantMessages: List<String>,
    val welcomeMessage: String? = null,
    val simulated: Boolean,
)

class ChatRepository(
    private val api: SmartPactApi,
) {
    suspend fun sendMessage(
        content: String,
        currentModule: AppModule?,
        sessionId: String?,
    ): ChatReplySnapshot {
        var resolvedSessionId = sessionId
        var welcomeMessage: String? = null

        return runCatching {
            if (resolvedSessionId.isNullOrBlank()) {
                val session = api.createChatSession().data
                resolvedSessionId = session.session_id
                welcomeMessage = session.welcome_message.takeIf { it.isNotBlank() }
            }

            val currentSessionId = resolvedSessionId ?: error("session_id is required")
            val sendResult = api.sendChatMessage(
                sessionId = currentSessionId,
                payload = ChatSendMessageRequest(
                    content = content,
                    current_module = currentModule?.id,
                ),
            ).data

            val detectedModule = AppModule.fromId(sendResult.detected_module)
                ?: currentModule
                ?: AppModule.EAGLE

            ChatReplySnapshot(
                sessionId = currentSessionId,
                messageId = sendResult.message_id,
                detectedModule = detectedModule,
                assistantMessages = emptyList(),
                welcomeMessage = welcomeMessage,
                simulated = false,
            )
        }.getOrElse {
            val detectedModule = resolveModule(content = content, currentModule = currentModule)
            ChatReplySnapshot(
                sessionId = resolvedSessionId,
                detectedModule = detectedModule,
                assistantMessages = simulatedResponses[detectedModule].orEmpty()
                    .ifEmpty { listOf("当前展示演示回复，请稍后重试。") },
                welcomeMessage = welcomeMessage,
                simulated = true,
            )
        }
    }

    suspend fun loadLatestAssistantMessages(sessionId: String): List<String> {
        return api.getChatMessages(
            sessionId = sessionId,
            page = 1,
            pageSize = 50,
        ).data.items
            .filter { it.role == "assistant" && it.content.isNotBlank() }
            .takeLast(2)
            .map { it.content.trim() }
    }

    fun streamAssistantReply(sessionId: String, messageId: String): Flow<String> = flow {
        val baseUrl = BuildConfig.API_BASE_URL.toHttpUrlOrNull()
            ?: error("Invalid API_BASE_URL: ${BuildConfig.API_BASE_URL}")
        val streamUrl = baseUrl.newBuilder()
            .addPathSegments("api/v1/chat/sessions")
            .addPathSegment(sessionId)
            .addPathSegment("stream")
            .addQueryParameter("message_id", messageId)
            .build()
        val request = Request.Builder()
            .url(streamUrl)
            .header("Accept", "text/event-stream")
            .get()
            .build()

        NetworkClient.rawClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                error("SSE request failed: HTTP ${response.code}")
            }
            val source = response.body?.source() ?: error("SSE response body is empty")
            var currentEvent = ""
            val dataBuffer = StringBuilder()
            var streamDone = false

            while (!source.exhausted() && !streamDone) {
                val line = source.readUtf8Line() ?: break
                if (line.isEmpty()) {
                    if (dataBuffer.isNotEmpty()) {
                        val payload = dataBuffer.toString().trimEnd('\n')
                        val parsed = parseSsePayload(currentEvent, payload)
                        parsed.content?.let { emit(it) }
                        streamDone = parsed.done
                    } else if (currentEvent.equals("done", ignoreCase = true)) {
                        streamDone = true
                    }
                    currentEvent = ""
                    dataBuffer.clear()
                    continue
                }

                when {
                    line.startsWith(":") -> Unit
                    line.startsWith("event:") -> {
                        currentEvent = line.removePrefix("event:").trim()
                    }

                    line.startsWith("data:") -> {
                        dataBuffer.append(line.removePrefix("data:").trimStart())
                        dataBuffer.append('\n')
                    }
                }
            }
        }
    }

    fun buildMockSnapshot(
        content: String,
        currentModule: AppModule?,
        sessionId: String? = null,
    ): ChatReplySnapshot {
        val detectedModule = resolveModule(content = content, currentModule = currentModule)
        return ChatReplySnapshot(
            sessionId = sessionId,
            detectedModule = detectedModule,
            assistantMessages = simulatedResponses[detectedModule].orEmpty()
                .ifEmpty { listOf("当前展示演示回复，请稍后重试。") },
            simulated = true,
        )
    }

    private fun resolveModule(
        content: String,
        currentModule: AppModule?,
    ): AppModule {
        return when {
            content.contains("投递") || content.contains("简历") -> AppModule.PHANTOM
            content.contains("调查") || content.contains("公司") || content.contains("背景") -> AppModule.INVESTIGATOR
            content.contains("合同") || content.contains("offer") || content.contains("条款") -> AppModule.GUARDIAN
            content.contains("找工作") || content.contains("职位") || content.contains("招聘") -> AppModule.EAGLE
            currentModule != null -> currentModule
            else -> AppModule.EAGLE
        }
    }

    private fun parseSsePayload(
        event: String,
        payload: String,
    ): ParsedSseChunk {
        if (event.equals("done", ignoreCase = true) || payload.equals("[DONE]", ignoreCase = true)) {
            return ParsedSseChunk(content = null, done = true)
        }
        if (payload.isBlank()) {
            return ParsedSseChunk(content = null, done = false)
        }

        val json = runCatching { JsonParser.parseString(payload) }.getOrNull()
        if (json?.isJsonObject == true) {
            val root = json.asJsonObject
            val dataNode = root.objectOrNull("data")
            val messageNode = root.objectOrNull("message")

            val done = root.booleanOrFalse("done") ||
                dataNode.booleanOrFalse("done") ||
                root.stringOrNull("event").isDoneMarker() ||
                root.stringOrNull("type").isDoneMarker() ||
                root.stringOrNull("status").isDoneMarker() ||
                dataNode.stringOrNull("event").isDoneMarker() ||
                dataNode.stringOrNull("type").isDoneMarker() ||
                dataNode.stringOrNull("status").isDoneMarker()

            val text = firstNotBlank(
                root.stringOrNull("delta"),
                root.stringOrNull("content"),
                root.stringOrNull("token"),
                root.stringOrNull("text"),
                messageNode.stringOrNull("content"),
                dataNode.stringOrNull("delta"),
                dataNode.stringOrNull("content"),
                dataNode.stringOrNull("token"),
                dataNode.stringOrNull("text"),
            )

            return ParsedSseChunk(
                content = text,
                done = done,
            )
        }
        if (json?.isJsonPrimitive == true && json.asJsonPrimitive.isString) {
            return ParsedSseChunk(content = json.asString, done = false)
        }

        return ParsedSseChunk(content = payload, done = false)
    }

    private fun firstNotBlank(vararg values: String?): String? {
        return values.firstOrNull { !it.isNullOrBlank() }?.trim()
    }

    private fun String?.isDoneMarker(): Boolean {
        if (this.isNullOrBlank()) return false
        return this.equals("done", ignoreCase = true) ||
            this.equals("completed", ignoreCase = true) ||
            this.equals("finish", ignoreCase = true)
    }

    private fun JsonObject.objectOrNull(key: String): JsonObject {
        val element = this.get(key) ?: return JsonObject()
        return if (element.isJsonObject) element.asJsonObject else JsonObject()
    }

    private fun JsonObject.stringOrNull(key: String): String? {
        val element = this.get(key) ?: return null
        if (element.isJsonNull) return null
        return runCatching { element.asString }.getOrNull()?.takeIf { it.isNotBlank() }
    }

    private fun JsonObject.booleanOrFalse(key: String): Boolean {
        val element = this.get(key) ?: return false
        return runCatching { element.asBoolean }.getOrDefault(false)
    }

    private data class ParsedSseChunk(
        val content: String?,
        val done: Boolean,
    )
}
