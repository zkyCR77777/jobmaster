package com.example.client.ui.screens

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Canvas
import com.example.client.data.AppModule
import com.example.client.data.eagleJobs
import com.example.client.data.repository.RepositoryProvider
import com.example.client.ui.components.AppCard
import com.example.client.ui.components.MockFallbackNotice
import com.example.client.ui.components.ModuleHeader
import com.example.client.ui.components.PillTag
import com.example.client.ui.components.SectionHeader
import com.example.client.ui.components.modulePalette
import com.example.client.ui.theme.AppBorder
import com.example.client.ui.theme.AppTextPrimary
import com.example.client.ui.theme.AppTextSecondary
import com.example.client.ui.theme.Blue100
import com.example.client.ui.theme.Blue400
import com.example.client.ui.theme.Blue500
import com.example.client.ui.theme.Blue50
import com.example.client.ui.theme.Blue600
import com.example.client.ui.theme.Cyan400
import com.example.client.ui.theme.Emerald50
import com.example.client.ui.theme.Emerald600
import com.example.client.ui.theme.Red50
import com.example.client.ui.theme.Red600
import com.example.client.ui.theme.Slate100
import com.example.client.ui.theme.Slate400
import com.example.client.ui.theme.Slate500
import com.example.client.ui.theme.White
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

@Composable
fun HawkEyeScreen(onBack: () -> Unit) {
    var isScanning by remember { mutableStateOf(true) }
    var scanProgress by remember { mutableIntStateOf(0) }
    var discoveredCount by remember { mutableIntStateOf(0) }
    var sourceJobs by remember { mutableStateOf(eagleJobs) }
    var isMockFallback by remember { mutableStateOf(false) }
    val jobsRepository = remember { RepositoryProvider.jobsRepository }
    val palette = modulePalette(AppModule.EAGLE)
    val sweepTransition = rememberInfiniteTransition(label = "eagle_sweep")
    val sweepRotation by sweepTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2_200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "eagle_sweep_rotation",
    )

    LaunchedEffect(Unit) {
        val snapshot = jobsRepository.getJobsSnapshot()
        sourceJobs = snapshot.items
        isMockFallback = snapshot.simulated
        while (scanProgress < 100) {
            delay(60)
            scanProgress += 2
            val totalJobs = sourceJobs.size
            discoveredCount = if (totalJobs == 0 || scanProgress == 0) {
                0
            } else {
                ((scanProgress / 100f) * totalJobs).toInt().coerceIn(1, totalJobs)
            }
        }
        isScanning = false
    }

    val jobs = sourceJobs.take(discoveredCount.coerceAtMost(sourceJobs.size))

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(brush = Brush.verticalGradient(listOf(palette.screenStart, palette.screenEnd))),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 10.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            ModuleHeader(
                module = AppModule.EAGLE,
                subtitle = "全天候职位雷达扫描",
                onBack = onBack,
            )
        }

        if (isMockFallback) {
            item {
                MockFallbackNotice(message = "职位 API 调用失败，当前展示本地演示数据。")
            }
        }

        item {
            AppCard(
                borderColor = Blue100,
                shapeRadius = 32.dp,
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Box(
                        modifier = Modifier.size(220.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        listOf(220.dp, 164.dp, 108.dp).forEachIndexed { index, size ->
                            Box(
                                modifier = Modifier
                                    .size(size)
                                    .clip(CircleShape)
                                    .border(
                                        width = if (index == 0) 2.dp else 1.dp,
                                        color = Blue100,
                                        shape = CircleShape,
                                    ),
                            )
                        }

                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .background(Blue500),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Radar,
                                contentDescription = null,
                                tint = White,
                                modifier = Modifier.size(34.dp),
                            )
                        }

                        if (isScanning) {
                            Canvas(
                                modifier = Modifier
                                    .matchParentSize()
                                    .graphicsLayer { rotationZ = sweepRotation },
                            ) {
                                drawLine(
                                    brush = Brush.linearGradient(
                                        colors = listOf(Blue500.copy(alpha = 0.85f), Color.Transparent),
                                        start = center,
                                        end = Offset(size.width, center.y),
                                    ),
                                    start = center,
                                    end = Offset(size.width, center.y),
                                    strokeWidth = 6f,
                                    cap = StrokeCap.Round,
                                )
                            }
                        }

                        jobs.take(4).forEachIndexed { index, _ ->
                            val angle = (index * 90 + 45) * (PI / 180f)
                            val x = (cos(angle) * 72f).roundToInt()
                            val y = (sin(angle) * 72f).roundToInt()
                            Box(
                                modifier = Modifier
                                    .align(Alignment.Center)
                                    .offset { IntOffset(x, y) }
                                    .size(14.dp)
                                    .clip(CircleShape)
                                    .background(Blue500),
                            ) {
                                val pulseTransition = rememberInfiniteTransition(label = "eagle_marker_$index")
                                val pulse by pulseTransition.animateFloat(
                                    initialValue = 1f,
                                    targetValue = 1.6f,
                                    animationSpec = infiniteRepeatable(
                                        animation = tween(1200),
                                        repeatMode = RepeatMode.Reverse,
                                    ),
                                    label = "eagle_marker_pulse_$index",
                                )
                                Box(
                                    modifier = Modifier
                                        .matchParentSize()
                                        .graphicsLayer {
                                            scaleX = pulse
                                            scaleY = pulse
                                            alpha = 0.28f
                                        }
                                        .clip(CircleShape)
                                        .background(Blue400),
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (isScanning) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Bolt,
                                contentDescription = null,
                                tint = Blue600,
                                modifier = Modifier.size(16.dp),
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "正在扫描职位...",
                                style = MaterialTheme.typography.labelLarge,
                                color = Blue600,
                            )
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(999.dp))
                                .background(Blue100),
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(scanProgress / 100f)
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(999.dp))
                                    .background(brush = Brush.horizontalGradient(listOf(Blue500, Cyan400))),
                            )
                        }

                        Text(
                            text = "已发现 ${jobs.size} 个匹配职位",
                            style = MaterialTheme.typography.bodySmall,
                            color = AppTextSecondary,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        )
                    }
                } else {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            text = "扫描完成",
                            style = MaterialTheme.typography.labelLarge,
                            color = Emerald600,
                        )
                        Text(
                            text = "共发现 ${jobs.size} 个优质职位",
                            style = MaterialTheme.typography.bodySmall,
                            color = AppTextSecondary,
                        )
                    }
                }
            }
        }

        item {
            SectionHeader(
                icon = Icons.Filled.TrendingUp,
                iconTint = Blue500,
                title = "发现的职位",
                trailing = "${jobs.size} 个",
            )
        }

        itemsIndexed(jobs, key = { _, job -> job.id }) { _, job ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(White)
                    .border(1.dp, AppBorder, RoundedCornerShape(24.dp))
                    .clickable { }
                    .padding(16.dp),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                Text(
                                    text = job.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = AppTextPrimary,
                                )
                                if (job.isNew) {
                                    PillTag(
                                        text = "NEW",
                                        backgroundColor = Red50,
                                        contentColor = Red600,
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            JobMetaRow(
                                icon = Icons.Filled.Business,
                                text = job.company,
                                trailingIcon = Icons.Filled.LocationOn,
                                trailingText = job.location,
                            )
                        }

                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(White),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = Icons.Filled.BookmarkBorder,
                                contentDescription = "收藏",
                                tint = Slate400,
                            )
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        job.tags.forEach { tag ->
                            PillTag(
                                text = tag,
                                backgroundColor = Blue50,
                                contentColor = Blue600,
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            text = job.salary,
                            style = MaterialTheme.typography.titleMedium,
                            color = Blue600,
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.AccessTime,
                                    contentDescription = null,
                                    tint = Slate400,
                                    modifier = Modifier.size(14.dp),
                                )
                                Text(
                                    text = "刚刚",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Slate500,
                                )
                            }

                            PillTag(
                                text = "匹配度 ${job.match}%",
                                backgroundColor = Emerald50,
                                contentColor = Emerald600,
                            )

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
    }
}

@Composable
private fun JobMetaRow(
    icon: ImageVector,
    text: String,
    trailingIcon: ImageVector,
    trailingText: String,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Icon(icon, contentDescription = null, tint = Slate400, modifier = Modifier.size(14.dp))
        Text(text = text, style = MaterialTheme.typography.bodySmall, color = AppTextSecondary)
        Icon(trailingIcon, contentDescription = null, tint = Slate400, modifier = Modifier.size(14.dp))
        Text(text = trailingText, style = MaterialTheme.typography.bodySmall, color = AppTextSecondary)
    }
}
