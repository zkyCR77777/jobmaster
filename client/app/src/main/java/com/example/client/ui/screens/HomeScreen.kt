package com.example.client.ui.screens

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.client.data.AppModule
import com.example.client.data.greetingForHour
import com.example.client.data.homeAgents
import com.example.client.ui.components.GradientIcon
import com.example.client.ui.components.PillTag
import com.example.client.ui.components.SectionHeader
import com.example.client.ui.components.homeAgentIcon
import com.example.client.ui.components.modulePalette
import com.example.client.ui.theme.AppBorder
import com.example.client.ui.theme.AppPrimary
import com.example.client.ui.theme.AppTextPrimary
import com.example.client.ui.theme.AppTextSecondary
import com.example.client.ui.theme.Blue100
import com.example.client.ui.theme.Blue50
import com.example.client.ui.theme.Blue500
import com.example.client.ui.theme.Indigo50
import com.example.client.ui.theme.Indigo600
import com.example.client.ui.theme.Red500
import com.example.client.ui.theme.Slate100
import com.example.client.ui.theme.Slate300
import com.example.client.ui.theme.Slate50
import com.example.client.ui.theme.Slate500
import com.example.client.ui.theme.Slate700
import com.example.client.ui.theme.White
import kotlinx.coroutines.delay
import java.util.Calendar

@Composable
fun HomeScreen() {
    var activeModuleId by rememberSaveable { mutableStateOf<String?>(null) }
    var isChatOpen by rememberSaveable { mutableStateOf(false) }
    val activeModule = AppModule.fromId(activeModuleId)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(brush = Brush.verticalGradient(listOf(White, Slate50)))
            .statusBarsPadding(),
    ) {
        Crossfade(targetState = activeModule, label = "home_module_crossfade") { module ->
            when (module) {
                null -> AppHomeContent(
                    onOpenChat = { isChatOpen = true },
                    onSelectModule = { activeModuleId = it.id },
                )

                AppModule.EAGLE -> HawkEyeScreen(onBack = { activeModuleId = null })
                AppModule.PHANTOM -> DeliveryScreen(onBack = { activeModuleId = null })
                AppModule.INVESTIGATOR -> InvestigatorScreen(onBack = { activeModuleId = null })
                AppModule.GUARDIAN -> GuardianScreen(onBack = { activeModuleId = null })
            }
        }

        if (activeModule != null && !isChatOpen) {
            val palette = modulePalette(activeModule)
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .navigationBarsPadding()
                    .padding(end = 20.dp, bottom = 20.dp)
                    .size(56.dp)
                    .shadow(18.dp, CircleShape, spotColor = palette.gradientStart.copy(alpha = 0.28f))
                    .clip(CircleShape)
                    .background(brush = Brush.linearGradient(listOf(Indigo600, Blue500)))
                    .clickable { isChatOpen = true },
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Filled.ChatBubbleOutline,
                    contentDescription = "打开 AI 对话",
                    tint = White,
                    modifier = Modifier.size(26.dp),
                )
            }
        }

        AiChatOverlay(
            isOpen = isChatOpen,
            onClose = { isChatOpen = false },
            currentModule = activeModule,
        )
    }
}

@Composable
private fun AppHomeContent(
    onOpenChat: () -> Unit,
    onSelectModule: (AppModule) -> Unit,
) {
    val hour = remember { Calendar.getInstance().get(Calendar.HOUR_OF_DAY) }
    val greeting = remember(hour) { greetingForHour(hour) }
    var activeIndex by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(3_000)
            activeIndex = (activeIndex + 1) % homeAgents.size
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column {
                    Text(
                        text = greeting,
                        style = MaterialTheme.typography.bodyMedium,
                        color = AppTextSecondary,
                    )
                    Text(
                        text = "求职高手",
                        style = MaterialTheme.typography.headlineMedium,
                        color = AppTextPrimary,
                    )
                }

                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Slate100),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Filled.NotificationsNone,
                        contentDescription = "通知",
                        tint = Slate700,
                        modifier = Modifier.size(20.dp),
                    )

                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(top = 2.dp, end = 2.dp)
                            .size(18.dp)
                            .clip(CircleShape)
                            .background(Red500),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "3",
                            color = White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }
        }

        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(28.dp))
                    .background(brush = Brush.horizontalGradient(listOf(Indigo600, Blue500)))
                    .padding(20.dp),
            ) {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Filled.AutoAwesome,
                            contentDescription = null,
                            tint = White,
                            modifier = Modifier.size(18.dp),
                        )
                        Text(
                            text = "Agent 工作中",
                            style = MaterialTheme.typography.labelLarge,
                            color = White,
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        HomeHeroStat(value = "24", label = "今日新发现职位")
                        HomeHeroDivider()
                        HomeHeroStat(value = "89%", label = "匹配成功率")
                        HomeHeroDivider()
                        HomeHeroStat(value = "5", label = "面试邀请")
                    }
                }
            }
        }

        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(brush = Brush.horizontalGradient(listOf(Indigo50, Blue50)))
                    .border(1.dp, Blue100, RoundedCornerShape(24.dp))
                    .clickable(onClick = onOpenChat)
                    .padding(16.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .shadow(14.dp, RoundedCornerShape(16.dp), spotColor = Blue500.copy(alpha = 0.25f))
                            .clip(RoundedCornerShape(16.dp))
                            .background(brush = Brush.linearGradient(listOf(Indigo600, Blue500))),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Filled.ChatBubbleOutline,
                            contentDescription = null,
                            tint = White,
                            modifier = Modifier.size(24.dp),
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "与 AI 对话",
                            style = MaterialTheme.typography.titleMedium,
                            color = AppTextPrimary,
                        )
                        Text(
                            text = "用自然语言告诉我你想做什么...",
                            style = MaterialTheme.typography.bodySmall,
                            color = AppTextSecondary,
                        )
                    }

                    Icon(
                        imageVector = Icons.Filled.KeyboardArrowRight,
                        contentDescription = null,
                        tint = AppPrimary,
                    )
                }
            }
        }

        item {
            SectionHeader(
                icon = Icons.Filled.TrendingUp,
                iconTint = AppPrimary,
                title = "智能体动态",
            )
        }

        item {
            val activeAgent = homeAgents[activeIndex]
            val palette = modulePalette(activeAgent.module)

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(Slate100)
                    .padding(horizontal = 16.dp, vertical = 14.dp),
            ) {
                Column {
                    Crossfade(targetState = activeAgent, label = "home_agent_preview") { agent ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(palette.accentSurface),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    imageVector = homeAgentIcon(agent.module),
                                    contentDescription = null,
                                    tint = palette.accent,
                                    modifier = Modifier.size(20.dp),
                                )
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = agent.module.title,
                                    style = MaterialTheme.typography.labelLarge,
                                    color = AppTextPrimary,
                                )
                                Text(
                                    text = agent.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = AppTextSecondary,
                                )
                            }

                            PillTag(
                                text = agent.stats,
                                backgroundColor = palette.accentSoft,
                                contentColor = palette.accent,
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            homeAgents.forEachIndexed { index, _ ->
                                Box(
                                    modifier = Modifier
                                        .size(width = if (index == activeIndex) 16.dp else 6.dp, height = 6.dp)
                                        .clip(RoundedCornerShape(999.dp))
                                        .background(if (index == activeIndex) AppPrimary else Slate300),
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            Text(
                text = "智能助手团队",
                style = MaterialTheme.typography.titleLarge,
                color = AppTextPrimary,
            )
        }

        items(homeAgents.chunked(2)) { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                rowItems.forEach { agent ->
                    val palette = modulePalette(agent.module)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(0.96f)
                            .clip(RoundedCornerShape(24.dp))
                            .background(palette.accentSoft)
                            .clickable { onSelectModule(agent.module) }
                            .padding(16.dp),
                    ) {
                        Column(modifier = Modifier.fillMaxSize()) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .shadow(12.dp, RoundedCornerShape(16.dp), spotColor = palette.gradientStart.copy(alpha = 0.24f))
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(brush = Brush.linearGradient(listOf(palette.gradientStart, palette.gradientEnd))),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    imageVector = homeAgentIcon(agent.module),
                                    contentDescription = null,
                                    tint = White,
                                    modifier = Modifier.size(24.dp),
                                )
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            Text(
                                text = agent.module.title,
                                style = MaterialTheme.typography.titleMedium,
                                color = AppTextPrimary,
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = agent.module.subtitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = AppTextSecondary,
                            )

                            Spacer(modifier = Modifier.weight(1f))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Text(
                                    text = agent.stats,
                                    style = MaterialTheme.typography.labelLarge,
                                    color = palette.accent,
                                )
                                Icon(
                                    imageVector = Icons.Filled.KeyboardArrowRight,
                                    contentDescription = null,
                                    tint = palette.accent,
                                )
                            }
                        }
                    }
                }

                if (rowItems.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun HomeHeroStat(
    value: String,
    label: String,
) {
    Column(horizontalAlignment = Alignment.Start) {
        Text(
            text = value,
            color = White,
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = White.copy(alpha = 0.85f),
        )
    }
}

@Composable
private fun HomeHeroDivider() {
    Box(
        modifier = Modifier
            .height(60.dp)
            .size(width = 1.dp, height = 60.dp)
            .background(Color.White.copy(alpha = 0.22f)),
    )
}
