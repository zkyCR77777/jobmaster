package com.example.client.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.client.data.AppModule
import com.example.client.data.CompanyRiskLevel
import com.example.client.data.ContractRiskLevel
import com.example.client.ui.theme.Amber100
import com.example.client.ui.theme.Amber50
import com.example.client.ui.theme.Amber500
import com.example.client.ui.theme.Amber600
import com.example.client.ui.theme.AppBorder
import com.example.client.ui.theme.AppSurface
import com.example.client.ui.theme.AppTextPrimary
import com.example.client.ui.theme.AppTextSecondary
import com.example.client.ui.theme.Blue100
import com.example.client.ui.theme.Blue50
import com.example.client.ui.theme.Blue500
import com.example.client.ui.theme.Blue600
import com.example.client.ui.theme.Cyan400
import com.example.client.ui.theme.Emerald100
import com.example.client.ui.theme.Emerald50
import com.example.client.ui.theme.Emerald500
import com.example.client.ui.theme.Emerald600
import com.example.client.ui.theme.Indigo100
import com.example.client.ui.theme.Indigo50
import com.example.client.ui.theme.Indigo500
import com.example.client.ui.theme.Indigo600
import com.example.client.ui.theme.Orange400
import com.example.client.ui.theme.Pink100
import com.example.client.ui.theme.Pink50
import com.example.client.ui.theme.Pink500
import com.example.client.ui.theme.Pink600
import com.example.client.ui.theme.Red100
import com.example.client.ui.theme.Red50
import com.example.client.ui.theme.Red400
import com.example.client.ui.theme.Red600
import com.example.client.ui.theme.Rose400
import com.example.client.ui.theme.Slate100
import com.example.client.ui.theme.Slate200
import com.example.client.ui.theme.Slate300
import com.example.client.ui.theme.Slate400
import com.example.client.ui.theme.Slate500
import com.example.client.ui.theme.Slate700
import com.example.client.ui.theme.Teal400
import com.example.client.ui.theme.White

data class ModulePalette(
    val screenStart: Color,
    val screenEnd: Color,
    val gradientStart: Color,
    val gradientEnd: Color,
    val accent: Color,
    val accentSoft: Color,
    val accentSurface: Color,
    val border: Color,
)

fun modulePalette(module: AppModule): ModulePalette = when (module) {
    AppModule.EAGLE -> ModulePalette(Blue50, White, Blue500, Cyan400, Blue600, Blue50, Blue100, Blue100)
    AppModule.PHANTOM -> ModulePalette(Pink50, White, Pink500, Rose400, Pink600, Pink50, Pink100, Pink100)
    AppModule.INVESTIGATOR -> ModulePalette(Emerald50, White, Emerald500, Teal400, Emerald600, Emerald50, Emerald100, Emerald100)
    AppModule.GUARDIAN -> ModulePalette(Amber50, White, Amber500, Orange400, Amber600, Amber50, Amber100, Amber100)
}

fun moduleIcon(module: AppModule): ImageVector = when (module) {
    AppModule.EAGLE -> Icons.Filled.Radar
    AppModule.PHANTOM -> Icons.Filled.Send
    AppModule.INVESTIGATOR -> Icons.Filled.Search
    AppModule.GUARDIAN -> Icons.Filled.Shield
}

@Composable
fun AppCard(
    modifier: Modifier = Modifier,
    backgroundColor: Color = AppSurface,
    borderColor: Color = AppBorder,
    shapeRadius: Dp = 28.dp,
    contentPadding: PaddingValues = PaddingValues(20.dp),
    content: @Composable ColumnScope.() -> Unit,
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(shapeRadius),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        border = BorderStroke(1.dp, borderColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(
            modifier = Modifier.padding(contentPadding),
            content = content,
        )
    }
}

@Composable
fun GradientIcon(
    module: AppModule,
    modifier: Modifier = Modifier,
    size: Dp = 40.dp,
    iconSize: Dp = 20.dp,
) {
    val palette = modulePalette(module)
    Box(
        modifier = modifier
            .size(size)
            .shadow(14.dp, CircleShape, spotColor = palette.gradientStart.copy(alpha = 0.28f))
            .clip(CircleShape)
            .background(brush = Brush.linearGradient(listOf(palette.gradientStart, palette.gradientEnd))),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = moduleIcon(module),
            contentDescription = module.title,
            tint = White,
            modifier = Modifier.size(iconSize),
        )
    }
}

@Composable
fun BackCircleButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    IconButton(
        onClick = onClick,
        modifier = modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(White)
            .border(1.dp, Slate100, CircleShape),
    ) {
        Icon(
            imageVector = Icons.Outlined.ArrowBack,
            contentDescription = "返回",
            tint = Slate700,
        )
    }
}

@Composable
fun ModuleHeader(
    module: AppModule,
    subtitle: String,
    onBack: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (onBack != null) {
            BackCircleButton(onClick = onBack)
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = module.title,
                style = MaterialTheme.typography.titleLarge,
                color = AppTextPrimary,
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelMedium,
                color = AppTextSecondary,
            )
        }
        GradientIcon(module = module)
    }
}

@Composable
fun PillTag(
    text: String,
    backgroundColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(999.dp))
            .background(backgroundColor)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            color = contentColor,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Medium,
        )
    }
}

@Composable
fun SectionHeader(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    trailing: String? = null,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(16.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = AppTextPrimary,
            )
        }
        if (trailing != null) {
            Text(
                text = trailing,
                style = MaterialTheme.typography.labelLarge,
                color = iconTint,
            )
        }
    }
}

@Composable
fun ProgressBar(
    progress: Float,
    startColor: Color,
    endColor: Color,
    modifier: Modifier = Modifier,
    height: Dp = 8.dp,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(999.dp))
            .background(startColor.copy(alpha = 0.18f)),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(progress.coerceIn(0f, 1f))
                .height(height)
                .clip(RoundedCornerShape(999.dp))
                .background(Brush.linearGradient(listOf(startColor, endColor))),
        )
    }
}

@Composable
fun TypingDots(
    modifier: Modifier = Modifier,
    dotColor: Color = Slate400,
) {
    val transition = rememberInfiniteTransition(label = "typing_dots")
    val scales = listOf(0, 120, 240).map { delayMillis ->
        transition.animateFloat(
            initialValue = 0.55f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(600, delayMillis = delayMillis, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse,
            ),
            label = "dot_scale_$delayMillis",
        )
    }

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        scales.forEach { scale ->
            Box(
                modifier = Modifier
                    .size((8.dp * scale.value).coerceAtLeast(5.dp))
                    .clip(CircleShape)
                    .background(dotColor),
            )
        }
    }
}

@Composable
fun StatusStage(
    text: String,
    completed: Boolean,
    accentColor: Color,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        if (completed) {
            Icon(
                imageVector = Icons.Filled.CheckCircle,
                contentDescription = null,
                tint = accentColor,
                modifier = Modifier.size(16.dp),
            )
        } else {
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .clip(CircleShape)
                    .border(2.dp, Slate300, CircleShape),
            )
        }
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = if (completed) AppTextPrimary else AppTextSecondary,
        )
    }
}

@Composable
fun SmallInfoBadge(
    icon: ImageVector,
    text: String,
    backgroundColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(999.dp))
            .background(backgroundColor)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Icon(icon, contentDescription = null, tint = contentColor, modifier = Modifier.size(14.dp))
        Text(text = text, style = MaterialTheme.typography.labelMedium, color = contentColor)
    }
}

@Composable
fun QuickStatCard(
    value: String,
    label: String,
    backgroundColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(22.dp))
            .background(backgroundColor)
            .defaultMinSize(minHeight = 88.dp)
            .padding(horizontal = 12.dp, vertical = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineMedium,
            color = contentColor,
            fontWeight = FontWeight.Bold,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = contentColor,
        )
    }
}

fun riskBadgeColors(level: CompanyRiskLevel): Pair<Color, Color> = when (level) {
    CompanyRiskLevel.LOW -> Emerald100 to Emerald600
    CompanyRiskLevel.MEDIUM -> Amber100 to Amber600
    CompanyRiskLevel.HIGH -> Red100 to Red600
}

fun riskLabel(level: CompanyRiskLevel): String = when (level) {
    CompanyRiskLevel.LOW -> "低风险"
    CompanyRiskLevel.MEDIUM -> "中风险"
    CompanyRiskLevel.HIGH -> "高风险"
}

fun contractRiskColors(level: ContractRiskLevel): Triple<Color, Color, Color> = when (level) {
    ContractRiskLevel.SAFE -> Triple(Emerald100, Emerald600, Emerald50)
    ContractRiskLevel.WARNING -> Triple(Amber100, Amber600, Amber50)
    ContractRiskLevel.DANGER -> Triple(Red100, Red600, Red50)
}

@Composable
fun NeutralInputCircle(
    icon: ImageVector,
    contentDescription: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(Slate100),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = Slate500,
            modifier = Modifier.size(20.dp),
        )
    }
}

fun homeAgentIcon(module: AppModule): ImageVector = when (module) {
    AppModule.EAGLE -> Icons.Filled.Radar
    AppModule.PHANTOM -> Icons.Filled.Send
    AppModule.INVESTIGATOR -> Icons.Filled.Search
    AppModule.GUARDIAN -> Icons.Filled.Shield
}

fun chatFallbackIcon(): ImageVector = Icons.Filled.AutoAwesome

fun documentIcon(): ImageVector = Icons.Filled.Description

@Composable
fun MockFallbackNotice(
    message: String = "网络或服务异常，当前展示本地演示数据（Mock）。",
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(Red50)
            .border(1.dp, Red100, RoundedCornerShape(18.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon(
            imageVector = Icons.Filled.WarningAmber,
            contentDescription = null,
            tint = Red600,
            modifier = Modifier.size(18.dp),
        )
        Text(
            text = message,
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
            color = Red600,
        )
    }
}
