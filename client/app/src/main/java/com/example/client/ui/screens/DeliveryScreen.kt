package com.example.client.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.client.data.AppModule
import com.example.client.data.DeliveryQueueItem
import com.example.client.data.DeliveryQueueStatus
import com.example.client.data.phantomDeliveryQueue
import com.example.client.data.phantomResumeSteps
import com.example.client.ui.components.AppCard
import com.example.client.ui.components.ModuleHeader
import com.example.client.ui.components.PillTag
import com.example.client.ui.components.ProgressBar
import com.example.client.ui.components.SectionHeader
import com.example.client.ui.components.modulePalette
import com.example.client.ui.theme.AppBorder
import com.example.client.ui.theme.AppTextPrimary
import com.example.client.ui.theme.AppTextSecondary
import com.example.client.ui.theme.Emerald100
import com.example.client.ui.theme.Emerald600
import com.example.client.ui.theme.Pink100
import com.example.client.ui.theme.Pink50
import com.example.client.ui.theme.Pink500
import com.example.client.ui.theme.Pink600
import com.example.client.ui.theme.Rose400
import com.example.client.ui.theme.Slate100
import com.example.client.ui.theme.Slate400
import com.example.client.ui.theme.Slate500
import com.example.client.ui.theme.White
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

@Composable
fun DeliveryScreen(onBack: () -> Unit) {
    val palette = modulePalette(AppModule.PHANTOM)
    var completedLines by remember { mutableStateOf(emptyList<String>()) }
    var currentTypingLine by remember { mutableStateOf("") }
    var queue by remember { mutableStateOf(phantomDeliveryQueue) }

    LaunchedEffect(Unit) {
        phantomResumeSteps.forEach { step ->
            currentTypingLine = ""
            step.forEach { char ->
                currentTypingLine += char
                delay(38)
            }
            completedLines = completedLines + step
            currentTypingLine = ""
            delay(520)
        }
    }

    LaunchedEffect(Unit) {
        while (true) {
            delay(2_000)
            var promoting = true
            queue = queue.map { item ->
                when {
                    item.status == DeliveryQueueStatus.DELIVERING -> item.copy(status = DeliveryQueueStatus.DELIVERED)
                    item.status == DeliveryQueueStatus.PENDING && promoting -> {
                        promoting = false
                        item.copy(status = DeliveryQueueStatus.DELIVERING)
                    }
                    else -> item
                }
            }
        }
    }

    val progress = ((completedLines.size + if (currentTypingLine.isNotEmpty()) 0.5f else 0f) / phantomResumeSteps.size)
        .coerceIn(0f, 1f)
    val deliveredCount = queue.count { it.status == DeliveryQueueStatus.DELIVERED }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(brush = Brush.verticalGradient(listOf(palette.screenStart, palette.screenEnd))),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 10.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            ModuleHeader(
                module = AppModule.PHANTOM,
                subtitle = "智能简历定制与自动投递",
                onBack = onBack,
            )
        }

        item {
            AppCard(borderColor = Pink100, shapeRadius = 32.dp) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Pink100),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Tune,
                            contentDescription = null,
                            tint = Pink600,
                            modifier = Modifier.size(16.dp),
                        )
                    }

                    Column {
                        Text(
                            text = "简历智能定制",
                            style = MaterialTheme.typography.titleMedium,
                            color = AppTextPrimary,
                        )
                        Text(
                            text = "AI 正在为你优化简历",
                            style = MaterialTheme.typography.bodySmall,
                            color = AppTextSecondary,
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(Slate100)
                        .padding(16.dp),
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        completedLines.forEach { line ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.CheckCircle,
                                    contentDescription = null,
                                    tint = Emerald600,
                                    modifier = Modifier.size(16.dp),
                                )
                                Text(
                                    text = line,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace),
                                    color = AppTextSecondary,
                                )
                            }
                        }

                        if (completedLines.size < phantomResumeSteps.size) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.AutoAwesome,
                                    contentDescription = null,
                                    tint = Pink600,
                                    modifier = Modifier.size(16.dp),
                                )
                                Text(
                                    text = "$currentTypingLine${if (currentTypingLine.isNotEmpty()) "|" else ""}",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace),
                                    color = Pink600,
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "优化进度",
                        style = MaterialTheme.typography.bodyMedium,
                        color = AppTextSecondary,
                    )
                    Text(
                        text = "${(progress * 100).roundToInt()}%",
                        style = MaterialTheme.typography.labelLarge,
                        color = Pink600,
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                ProgressBar(
                    progress = progress,
                    startColor = Pink500,
                    endColor = Rose400,
                )
            }
        }

        item {
            SectionHeader(
                icon = Icons.Filled.Description,
                iconTint = Pink500,
                title = "投递队列",
                trailing = "$deliveredCount/${queue.size} 已投递",
            )
        }

        itemsIndexed(queue, key = { _, item -> item.id }) { _, item ->
            val isDelivering = item.status == DeliveryQueueStatus.DELIVERING
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(White)
                    .border(1.dp, if (isDelivering) Pink100 else AppBorder, RoundedCornerShape(24.dp))
                    .padding(16.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    DeliveryStatusIcon(status = item.status)

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = item.company,
                            style = MaterialTheme.typography.titleMedium,
                            color = AppTextPrimary,
                        )
                        Text(
                            text = item.position,
                            style = MaterialTheme.typography.bodySmall,
                            color = AppTextSecondary,
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        DeliveryStatusPill(status = item.status)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = item.time,
                            style = MaterialTheme.typography.bodySmall,
                            color = Slate400,
                        )
                    }

                    Icon(
                        imageVector = Icons.Filled.KeyboardArrowRight,
                        contentDescription = null,
                        tint = Slate400,
                    )
                }
            }
        }
    }
}

@Composable
private fun DeliveryStatusIcon(status: DeliveryQueueStatus) {
    val background = when (status) {
        DeliveryQueueStatus.DELIVERED -> Emerald100
        DeliveryQueueStatus.DELIVERING -> Pink100
        DeliveryQueueStatus.PENDING -> Slate100
    }

    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(background),
        contentAlignment = Alignment.Center,
    ) {
        when (status) {
            DeliveryQueueStatus.DELIVERED -> Icon(
                imageVector = Icons.Filled.CheckCircle,
                contentDescription = null,
                tint = Emerald600,
            )

            DeliveryQueueStatus.DELIVERING -> Box(
                modifier = Modifier
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(listOf(Pink500, Rose400))),
            )

            DeliveryQueueStatus.PENDING -> Icon(
                imageVector = Icons.Filled.AccessTime,
                contentDescription = null,
                tint = Slate500,
            )
        }
    }
}

@Composable
private fun DeliveryStatusPill(status: DeliveryQueueStatus) {
    val (text, background, color) = when (status) {
        DeliveryQueueStatus.DELIVERED -> Triple("已投递", Emerald100, Emerald600)
        DeliveryQueueStatus.DELIVERING -> Triple("投递中", Pink100, Pink600)
        DeliveryQueueStatus.PENDING -> Triple("等待中", Slate100, Slate500)
    }

    PillTag(text = text, backgroundColor = background, contentColor = color)
}
