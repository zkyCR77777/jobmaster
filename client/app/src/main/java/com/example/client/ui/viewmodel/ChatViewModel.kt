package com.example.client.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.client.data.AppModule
import com.example.client.data.ChatMessage
import com.example.client.data.ChatSender
import com.example.client.data.repository.ChatRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(
        ChatUiState(
            messages = listOf(
                ChatMessage(
                    id = 1L,
                    sender = ChatSender.AGENT,
                    content = DEFAULT_WELCOME,
                )
            )
        )
    )
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    private var nextMessageId = 2L

    fun updateInput(value: String) {
        _uiState.update { it.copy(inputValue = value) }
    }

    fun syncCurrentModule(currentModule: AppModule?) {
        if (currentModule == null) return
        _uiState.update { state ->
            if (state.messages.size > 1) state else state.copy(activeAgent = currentModule)
        }
    }

    fun sendMessage(text: String? = null, currentModule: AppModule?) {
        val content = (text ?: _uiState.value.inputValue).trim()
        if (content.isBlank() || _uiState.value.isTyping) return

        val targetModule = resolveModule(content = content, currentModule = currentModule)
        appendUserMessage(content)
        _uiState.update {
            it.copy(
                inputValue = "",
                isTyping = true,
                activeAgent = targetModule,
            )
        }

        viewModelScope.launch {
            val snapshot = chatRepository.sendMessage(
                content = content,
                currentModule = targetModule,
                sessionId = _uiState.value.sessionId,
            )
            _uiState.update {
                it.copy(
                    sessionId = snapshot.sessionId ?: it.sessionId,
                    activeAgent = snapshot.detectedModule,
                    isMockFallback = snapshot.simulated,
                )
            }

            val replies = snapshot.assistantMessages.ifEmpty {
                listOf("当前展示演示回复，请稍后重试。")
            }
            replies.forEach { reply ->
                delay(220)
                appendAssistantMessage(reply, snapshot.detectedModule)
            }
            _uiState.update { it.copy(isTyping = false) }
        }
    }

    private fun appendUserMessage(content: String) {
        val message = ChatMessage(
            id = nextId(),
            sender = ChatSender.USER,
            content = content,
        )
        _uiState.update { it.copy(messages = it.messages + message) }
    }

    private fun appendAssistantMessage(content: String, module: AppModule) {
        val message = ChatMessage(
            id = nextId(),
            sender = ChatSender.AGENT,
            content = content,
            agent = module,
        )
        _uiState.update { it.copy(messages = it.messages + message) }
    }

    private fun resolveModule(content: String, currentModule: AppModule?): AppModule {
        return when {
            content.contains("投递") || content.contains("简历") -> AppModule.PHANTOM
            content.contains("调查") || content.contains("公司") || content.contains("背景") -> AppModule.INVESTIGATOR
            content.contains("合同") || content.contains("offer") || content.contains("条款") -> AppModule.GUARDIAN
            content.contains("找工作") || content.contains("职位") || content.contains("招聘") -> AppModule.EAGLE
            currentModule != null -> currentModule
            else -> AppModule.EAGLE
        }
    }

    private fun nextId(): Long {
        val value = nextMessageId
        nextMessageId += 1
        return value
    }

    companion object {
        private const val DEFAULT_WELCOME =
            "你好！我是求职高手 AI 助手。你可以用自然语言告诉我你的需求，我会调度合适的 Agent 来帮助你。试试说“帮我找一份产品经理的工作”或“分析这份 offer”。"
    }
}

data class ChatUiState(
    val messages: List<ChatMessage> = emptyList(),
    val inputValue: String = "",
    val isTyping: Boolean = false,
    val activeAgent: AppModule? = null,
    val sessionId: String? = null,
    val isMockFallback: Boolean = false,
)
