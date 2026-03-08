package com.example.client.ui.screens

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.client.data.AppModule
import com.example.client.data.CompanyProfile
import com.example.client.data.CompanyRiskLevel
import com.example.client.data.investigatorCompanies
import com.example.client.data.investigatorSources
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.client.ui.viewmodel.CompanyViewModel
import com.example.client.ui.components.AppCard
import com.example.client.ui.components.MockFallbackNotice
import com.example.client.ui.components.ModuleHeader
import com.example.client.ui.components.PillTag
import com.example.client.ui.components.SectionHeader
import com.example.client.ui.components.contractRiskColors
import com.example.client.ui.components.modulePalette
import com.example.client.ui.components.riskBadgeColors
import com.example.client.ui.components.riskLabel
import com.example.client.ui.components.StatusStage
import com.example.client.ui.theme.AppBorder
import com.example.client.ui.theme.AppTextPrimary
import com.example.client.ui.theme.AppTextSecondary
import com.example.client.ui.theme.Emerald100
import com.example.client.ui.theme.Emerald50
import com.example.client.ui.theme.Emerald500
import com.example.client.ui.theme.Emerald600
import com.example.client.ui.theme.Red50
import com.example.client.ui.theme.Red600
import com.example.client.ui.theme.Slate100
import com.example.client.ui.theme.Slate300
import com.example.client.ui.theme.Slate400
import com.example.client.ui.theme.Slate500
import com.example.client.ui.theme.White
import kotlinx.coroutines.delay

@Composable
fun InvestigatorScreen(onBack: () -> Unit) {
    val palette = modulePalette(AppModule.INVESTIGATOR)
    val companyViewModel: CompanyViewModel = hiltViewModel()
    val uiState by companyViewModel.uiState.collectAsStateWithLifecycle()
    var isAnalyzing by remember { mutableStateOf(true) }
    var progress by remember { mutableIntStateOf(0) }
    var expandedId by remember { mutableStateOf<Int?>(null) }
    val companies = uiState.companies
    val isMockFallback = uiState.simulated
    val completedSources = remember { mutableStateListOf<String>() }
    val lowRiskCount = companies.count { it.riskLevel == CompanyRiskLevel.LOW }
    val mediumRiskCount = companies.count { it.riskLevel == CompanyRiskLevel.MEDIUM }
    val highRiskCount = companies.count { it.riskLevel == CompanyRiskLevel.HIGH }


    LaunchedEffect(Unit) {
        while (progress < 100) {
            delay(50)
            progress += 2
            if (progress > 20 && !completedSources.contains("工商信息")) completedSources += "工商信息"
            if (progress > 45 && !completedSources.contains("舆情分析")) completedSources += "舆情分析"
            if (progress > 70 && !completedSources.contains("员工评价")) completedSources += "员工评价"
            if (progress >= 100 && !completedSources.contains("财务状况")) completedSources += "财务状况"
        }
        isAnalyzing = false
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(androidx.compose.ui.graphics.Brush.verticalGradient(listOf(palette.screenStart, palette.screenEnd))),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 10.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            ModuleHeader(
                module = AppModule.INVESTIGATOR,
                subtitle = "多维数据企业风险洞察",
                onBack = onBack,
            )
        }

        if (isMockFallback) {
            item {
                MockFallbackNotice(message = "企业分析 API 调用失败，当前展示本地演示数据。")
            }
        }

        if (isAnalyzing) {
            item {
                AppCard(borderColor = palette.border, shapeRadius = 32.dp) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Sync,
                            contentDescription = null,
                            tint = Emerald500,
                            modifier = Modifier
                                .size(24.dp)
                                .rotate(progress * 8f),
                        )
                        Column {
                            Text(
                                text = "正在深度分析企业数据",
                                style = MaterialTheme.typography.titleMedium,
                                color = AppTextPrimary,
                            )
                            Text(
                                text = "整合多维度数据源...",
                                style = MaterialTheme.typography.bodySmall,
                                color = AppTextSecondary,
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        investigatorSources.forEach { source ->
                            val isDone = completedSources.contains(source)
                            StatusStage(
                                text = source,
                                completed = isDone,
                                accentColor = Emerald500,
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(999.dp))
                            .background(Emerald100),
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(progress / 100f)
                                .height(8.dp)
                                .clip(RoundedCornerShape(999.dp))
                                .background(androidx.compose.ui.graphics.Brush.horizontalGradient(listOf(Emerald500, palette.gradientEnd))),
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "$progress% 完成",
                        style = MaterialTheme.typography.bodySmall,
                        color = AppTextSecondary,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    )
                }
            }
        } else {
            item {
                AppCard(shapeRadius = 32.dp) {
                    SectionHeader(
                        icon = Icons.Filled.WarningAmber,
                        iconTint = palette.accent,
                        title = "风险分布概览",
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        RiskOverviewCard(value = lowRiskCount.toString(), label = "低风险", background = Emerald50, color = Emerald600, modifier = Modifier.weight(1f))
                        RiskOverviewCard(value = mediumRiskCount.toString(), label = "中风险", background = palette.accentSoft, color = palette.accent, modifier = Modifier.weight(1f))
                        RiskOverviewCard(value = highRiskCount.toString(), label = "高风险", background = Red50, color = Red600, modifier = Modifier.weight(1f))
                    }
                }
            }
        }

        item {
            SectionHeader(
                icon = Icons.Filled.Business,
                iconTint = palette.accent,
                title = "企业风险画像",
            )
        }

        itemsIndexed(companies, key = { _, company -> company.id }) { _, company ->
            CompanyRiskCard(
                company = company,
                expanded = expandedId == company.id,
                onToggle = { expandedId = if (expandedId == company.id) null else company.id },
            )
        }
    }
}

@Composable
private fun RiskOverviewCard(
    value: String,
    label: String,
    background: androidx.compose.ui.graphics.Color,
    color: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(22.dp))
            .background(background)
            .padding(vertical = 16.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                color = color,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = color,
            )
        }
    }
}

@Composable
private fun CompanyRiskCard(
    company: CompanyProfile,
    expanded: Boolean,
    onToggle: () -> Unit,
) {
    val (badgeBg, badgeColor) = riskBadgeColors(company.riskLevel)
    val avatarBackground = when (company.riskLevel) {
        CompanyRiskLevel.LOW -> Emerald100
        CompanyRiskLevel.MEDIUM -> badgeBg
        CompanyRiskLevel.HIGH -> androidx.compose.ui.graphics.Color(0xFFFEE2E2)
    }
    val avatarColor = when (company.riskLevel) {
        CompanyRiskLevel.LOW -> Emerald600
        CompanyRiskLevel.MEDIUM -> badgeColor
        CompanyRiskLevel.HIGH -> Red600
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(White)
            .border(1.dp, if (company.riskLevel == CompanyRiskLevel.HIGH) androidx.compose.ui.graphics.Color(0xFFFECACA) else AppBorder, RoundedCornerShape(24.dp)),
    ) {
        Column {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(avatarBackground),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = company.name.take(1),
                                style = MaterialTheme.typography.titleLarge,
                                color = avatarColor,
                                fontWeight = FontWeight.Bold,
                            )
                        }

                        Column {
                            Text(
                                text = company.name,
                                style = MaterialTheme.typography.titleMedium,
                                color = AppTextPrimary,
                            )
                            Text(
                                text = "${company.industry} | ${company.size}人",
                                style = MaterialTheme.typography.bodySmall,
                                color = AppTextSecondary,
                            )
                        }
                    }

                    PillTag(
                        text = riskLabel(company.riskLevel),
                        backgroundColor = badgeBg,
                        contentColor = badgeColor,
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MiniStatCard(
                        icon = Icons.Filled.Star,
                        iconTint = androidx.compose.ui.graphics.Color(0xFFF59E0B),
                        primary = company.rating.toString(),
                        secondary = "评分",
                        modifier = Modifier.weight(1f),
                    )

                    MiniStatCard(
                        icon = if (company.growth >= 0) Icons.Filled.TrendingUp else Icons.Filled.TrendingDown,
                        iconTint = if (company.growth >= 0) Emerald500 else Red600,
                        primary = "${if (company.growth > 0) "+" else ""}${company.growth}%",
                        secondary = "增长",
                        modifier = Modifier.weight(1f),
                    )

                    MiniStatCard(
                        icon = null,
                        iconTint = Slate500,
                        primary = company.salary,
                        secondary = "薪资",
                        modifier = Modifier.weight(1f),
                    )

                    MiniStatCard(
                        icon = Icons.Filled.Groups,
                        iconTint = Slate400,
                        primary = "",
                        secondary = "团队",
                        modifier = Modifier.weight(1f),
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onToggle)
                        .padding(top = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                ) {
                    Icon(
                        imageVector = if (expanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                        contentDescription = null,
                        tint = Slate500,
                    )
                    Text(
                        text = if (expanded) "收起详情" else "展开风险分析",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Slate500,
                    )
                }
            }

            if (expanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Slate100)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    if (company.risks.isNotEmpty()) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Cancel,
                                    contentDescription = null,
                                    tint = Red600,
                                    modifier = Modifier.size(14.dp),
                                )
                                Text(
                                    text = "风险提示",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = Red600,
                                )
                            }

                            company.risks.forEach { risk ->
                                Row(
                                    verticalAlignment = Alignment.Top,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .padding(top = 6.dp)
                                            .size(4.dp)
                                            .clip(CircleShape)
                                            .background(Red600),
                                    )
                                    Text(
                                        text = risk,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = AppTextPrimary,
                                    )
                                }
                            }
                        }
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Filled.CheckCircle,
                                contentDescription = null,
                                tint = Emerald600,
                                modifier = Modifier.size(14.dp),
                            )
                            Text(
                                text = "正面信息",
                                style = MaterialTheme.typography.labelLarge,
                                color = Emerald600,
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            company.positives.forEach { item ->
                                PillTag(
                                    text = item,
                                    backgroundColor = Emerald100,
                                    contentColor = Emerald600,
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
private fun MiniStatCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector?,
    iconTint: androidx.compose.ui.graphics.Color,
    primary: String,
    secondary: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(White)
            .padding(horizontal = 10.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        if (icon != null) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(14.dp))
                if (primary.isNotEmpty()) {
                    Text(
                        text = primary,
                        style = MaterialTheme.typography.labelLarge,
                        color = if (primary.startsWith("-")) Red600 else if (primary.startsWith("+")) Emerald600 else AppTextPrimary,
                    )
                }
            }
        } else {
            Text(text = primary, style = MaterialTheme.typography.labelLarge, color = AppTextPrimary)
        }
        Text(text = secondary, style = MaterialTheme.typography.labelSmall, color = AppTextSecondary)
    }
}
