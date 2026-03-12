package com.example.client.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.client.data.AppModule
import com.example.client.data.ChatMessage
import com.example.client.data.ChatSender
import com.example.client.data.chatQuickCommands
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.client.ui.viewmodel.ChatViewModel
import com.example.client.ui.components.ErrorNotice
import com.example.client.ui.components.NeutralInputCircle
import com.example.client.ui.components.PillTag
import com.example.client.ui.components.TypingDots
import com.example.client.ui.components.chatFallbackIcon
import com.example.client.ui.components.moduleIcon
import com.example.client.ui.components.modulePalette
import com.example.client.ui.theme.AppBorder
import com.example.client.ui.theme.AppPrimary
import com.example.client.ui.theme.AppTextPrimary
import com.example.client.ui.theme.AppTextSecondary
import com.example.client.ui.theme.Indigo100
import com.example.client.ui.theme.Indigo600
import com.example.client.ui.theme.Slate100
import com.example.client.ui.theme.Slate300
import com.example.client.ui.theme.Slate400
import com.example.client.ui.theme.Slate500
import com.example.client.ui.theme.Slate600
import com.example.client.ui.theme.White
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@Composable
fun AiChatOverlay(
    isOpen: Boolean,
    onClose: () -> Unit,
    currentModule: AppModule?,
) {
    val chatViewModel: ChatViewModel = hiltViewModel()
    val uiState by chatViewModel.uiState.collectAsStateWithLifecycle()
    val messages = uiState.messages
    val inputValue = uiState.inputValue
    val isTyping = uiState.isTyping
    val activeAgent = uiState.activeAgent ?: currentModule
    val listState = rememberLazyListState()

    LaunchedEffect(currentModule) {
        chatViewModel.syncCurrentModule(currentModule)
    }

    LaunchedEffect(messages.size, isTyping, isOpen) {
        if (isOpen) {
            val target = when {
                isTyping -> messages.size
                messages.isNotEmpty() -> messages.lastIndex
                else -> 0
            }
            listState.animateScrollToItem(target.coerceAtLeast(0))
        }
    }

    fun sendMessage(text: String? = null) {
        chatViewModel.sendMessage(text = text, currentModule = currentModule)
    }

    AnimatedVisibility(
        visible = isOpen,
        enter = fadeIn() + slideInVertically(initialOffsetY = { it }),
        exit = fadeOut() + slideOutVertically(targetOffsetY = { it }),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 12.dp)
                .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
                .background(White),
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Brush.linearGradient(listOf(Indigo600, AppPrimary))),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = Icons.Filled.ChatBubbleOutline,
                                contentDescription = null,
                                tint = White,
                                modifier = Modifier.size(20.dp),
                            )
                        }
                        Column {
                            Text(
                                text = "AI 对话助手",
                                style = MaterialTheme.typography.titleMedium,
                                color = AppTextPrimary,
                            )
                            Text(
                                text = "用自然语言与 Agent 交流",
                                style = MaterialTheme.typography.bodySmall,
                                color = AppTextSecondary,
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Slate100)
                            .clickable(onClick = onClose),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Filled.KeyboardArrowDown,
                            contentDescription = "关闭对话",
                            tint = Slate600,
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(AppBorder),
                )

                uiState.errorMessage?.let { errorMessage ->
                    ErrorNotice(
                        message = errorMessage,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                    )
                }

                LazyColumn(
                    state = listState,
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    items(messages, key = { it.id }) { message ->
                        ChatBubble(message = message)
                    }

                    if (isTyping) {
                        item(key = "typing") {
                            TypingBubble(activeAgent = activeAgent)
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(White)
                        .padding(top = 6.dp),
                ) {
                    Column {
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            items(chatQuickCommands) { command ->
                                val palette = modulePalette(command.module)
                                PillTag(
                                    text = command.label,
                                    backgroundColor = palette.accentSurface,
                                    contentColor = palette.accent,
                                    modifier = Modifier.clickable(enabled = !isTyping) { sendMessage(command.label) },
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 6.dp)
                                .background(AppBorder.copy(alpha = 0.65f))
                                .height(1.dp),
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .navigationBarsPadding()
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            NeutralInputCircle(
                                icon = Icons.Filled.AttachFile,
                                contentDescription = "附件",
                            )

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(999.dp))
                                    .background(Slate100)
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                contentAlignment = Alignment.CenterStart,
                            ) {
                                if (inputValue.isBlank()) {
                                    Text(
                                        text = "告诉我你想做什么...",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Slate400,
                                    )
                                }

                                BasicTextField(
                                    value = inputValue,
                                    onValueChange = { chatViewModel.updateInput(it) },
                                    singleLine = true,
                                    textStyle = MaterialTheme.typography.bodyMedium.copy(color = AppTextPrimary),
                                    modifier = Modifier.fillMaxWidth(),
                                )
                            }

                            NeutralInputCircle(
                                icon = Icons.Filled.Mic,
                                contentDescription = "语音",
                            )

                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (inputValue.isBlank()) {
                                            Brush.linearGradient(listOf(Slate300, Slate300))
                                        } else {
                                            Brush.linearGradient(listOf(Indigo600, AppPrimary))
                                        }
                                    )
                                    .clickable(enabled = inputValue.isNotBlank() && !isTyping) { sendMessage() },
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Send,
                                    contentDescription = "发送",
                                    tint = White,
                                    modifier = Modifier.size(20.dp),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ChatBubble(message: ChatMessage) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (message.sender == ChatSender.USER) Arrangement.End else Arrangement.Start,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .clip(
                    RoundedCornerShape(
                        topStart = 22.dp,
                        topEnd = 22.dp,
                        bottomStart = if (message.sender == ChatSender.USER) 22.dp else 8.dp,
                        bottomEnd = if (message.sender == ChatSender.USER) 8.dp else 22.dp,
                    ),
                )
                .background(if (message.sender == ChatSender.USER) AppPrimary else Slate100)
                .padding(horizontal = 16.dp, vertical = 14.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (message.sender == ChatSender.AGENT && message.agent != null) {
                    val palette = modulePalette(message.agent)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .clip(CircleShape)
                                .background(palette.accentSurface),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = moduleIcon(message.agent),
                                contentDescription = null,
                                tint = palette.accent,
                                modifier = Modifier.size(12.dp),
                            )
                        }
                        Text(
                            text = message.agent.title,
                            style = MaterialTheme.typography.labelMedium,
                            color = palette.accent,
                        )
                    }
                }

                Text(
                    text = message.content,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (message.sender == ChatSender.USER) White else AppTextPrimary,
                )
            }
        }
    }
}

@Composable
private fun TypingBubble(activeAgent: AppModule?) {
    val module = activeAgent
    val palette = module?.let { modulePalette(it) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(22.dp))
                .background(White)
                .padding(horizontal = 4.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(palette?.accentSurface ?: Slate100),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = module?.let { moduleIcon(it) } ?: chatFallbackIcon(),
                    contentDescription = null,
                    tint = palette?.accent ?: Slate500,
                    modifier = Modifier.size(16.dp),
                )
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(Slate100)
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                contentAlignment = Alignment.Center,
            ) {
                TypingDots()
            }
        }
    }
}
