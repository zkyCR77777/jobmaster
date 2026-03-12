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
    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    private var nextMessageId = 1L

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
            runCatching {
                chatRepository.sendMessage(
                    content = content,
                    currentModule = targetModule,
                    sessionId = _uiState.value.sessionId,
                )
            }.onSuccess { snapshot ->
                _uiState.update {
                    it.copy(
                        sessionId = snapshot.sessionId ?: it.sessionId,
                        activeAgent = snapshot.detectedModule,
                        errorMessage = null,
                    )
                }

                snapshot.welcomeMessage
                    ?.takeIf { welcome -> _uiState.value.messages.none { it.content == welcome } }
                    ?.let { welcome ->
                        delay(120)
                        appendAssistantMessage(welcome, snapshot.detectedModule)
                    }

                snapshot.assistantMessages.forEach { reply ->
                    delay(220)
                    appendAssistantMessage(reply, snapshot.detectedModule)
                }
            }.onFailure {
                _uiState.update { state ->
                    state.copy(
                        isTyping = false,
                        errorMessage = "AI 对话服务调用失败，请稍后重试。",
                    )
                }
                return@launch
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
}

data class ChatUiState(
    val messages: List<ChatMessage> = emptyList(),
    val inputValue: String = "",
    val isTyping: Boolean = false,
    val activeAgent: AppModule? = null,
    val sessionId: String? = null,
    val errorMessage: String? = null,
)
