package com.example.client.ui.screens

import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts.OpenDocument
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
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.TipsAndUpdates
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.client.data.AppModule
import com.example.client.data.ContractClause
import com.example.client.data.ContractRiskLevel
import com.example.client.data.guardianScanStages
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.client.ui.components.ErrorNotice
import com.example.client.ui.viewmodel.ContractViewModel
import com.example.client.ui.components.AppCard
import com.example.client.ui.components.ModuleHeader
import com.example.client.ui.components.PillTag
import com.example.client.ui.components.SectionHeader
import com.example.client.ui.components.StatusStage
import com.example.client.ui.components.contractRiskColors
import com.example.client.ui.components.modulePalette
import com.example.client.ui.theme.Amber100
import com.example.client.ui.theme.Amber50
import com.example.client.ui.theme.Amber500
import com.example.client.ui.theme.Amber600
import com.example.client.ui.theme.AppBorder
import com.example.client.ui.theme.AppTextPrimary
import com.example.client.ui.theme.AppTextSecondary
import com.example.client.ui.theme.Blue100
import com.example.client.ui.theme.Blue50
import com.example.client.ui.theme.Blue600
import com.example.client.ui.theme.Emerald100
import com.example.client.ui.theme.Emerald50
import com.example.client.ui.theme.Emerald600
import com.example.client.ui.theme.Pink100
import com.example.client.ui.theme.Pink600
import com.example.client.ui.theme.Red50
import com.example.client.ui.theme.Red600
import com.example.client.ui.theme.Slate100
import com.example.client.ui.theme.Slate300
import com.example.client.ui.theme.Slate400
import com.example.client.ui.theme.White
import kotlinx.coroutines.delay

private data class UploadedDocument(
    val name: String,
    val sizeLabel: String,
)

@Composable
fun GuardianScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val palette = modulePalette(AppModule.GUARDIAN)
    val contractViewModel: ContractViewModel = hiltViewModel()
    val contractUiState by contractViewModel.uiState.collectAsStateWithLifecycle()
    var uploadedFile by remember { mutableStateOf<UploadedDocument?>(null) }
    var showUploadPanel by remember { mutableStateOf(true) }
    var isScanning by remember { mutableStateOf(false) }
    var scanProgress by remember { mutableIntStateOf(0) }
    var expandedId by remember { mutableStateOf<Int?>(null) }
    var highlightedId by remember { mutableStateOf<Int?>(null) }
    var clauses by remember(contractUiState.clauses) { mutableStateOf(contractUiState.clauses) }
    var summaryLabel by remember(contractUiState.summaryLabel) { mutableStateOf(contractUiState.summaryLabel) }
    var shouldLoadSampleContract by remember { mutableStateOf(false) }
    var pendingUploadName by remember { mutableStateOf<String?>(null) }
    var pendingUploadBytes by remember { mutableStateOf<ByteArray?>(null) }
    val (summaryBackground, summaryColor) = when (summaryLabel) {
        "较安全" -> Emerald100 to Emerald600
        "高风险" -> Red50 to Red600
        else -> Amber100 to Amber600
    }

    val filePicker = rememberLauncherForActivityResult(contract = OpenDocument()) { uri: Uri? ->
        val file = uri?.let { context.resolveDocument(it) }
        if (file != null) {
            val bytes = uri?.let { selectedUri ->
                context.contentResolver.openInputStream(selectedUri)?.use { it.readBytes() }
            }
            if (bytes == null) return@rememberLauncherForActivityResult
            uploadedFile = file
            showUploadPanel = false
            isScanning = true
            scanProgress = 0
            shouldLoadSampleContract = false
            pendingUploadName = file.name
            pendingUploadBytes = bytes
            clauses = emptyList()
            summaryLabel = "分析中"
            expandedId = null
            contractViewModel.reset()
        }
    }

    LaunchedEffect(isScanning) {
        if (!isScanning) return@LaunchedEffect
        while (scanProgress < 100) {
            delay(50)
            scanProgress += 2
        }
        isScanning = false
    }

    LaunchedEffect(isScanning, showUploadPanel, shouldLoadSampleContract, pendingUploadName, pendingUploadBytes) {
        if (isScanning || showUploadPanel) return@LaunchedEffect
        when {
            shouldLoadSampleContract -> {
                contractViewModel.loadSampleContract()
                shouldLoadSampleContract = false
            }
            pendingUploadName != null && pendingUploadBytes != null -> {
                contractViewModel.uploadContract(pendingUploadName!!, pendingUploadBytes!!)
                pendingUploadName = null
                pendingUploadBytes = null
            }
        }
    }

    LaunchedEffect(contractUiState.summaryLabel, contractUiState.clauses) {
        summaryLabel = contractUiState.summaryLabel
        clauses = contractUiState.clauses
        expandedId = null
    }

    LaunchedEffect(isScanning, showUploadPanel, shouldLoadSampleContract, clauses) {
        if (isScanning || showUploadPanel || shouldLoadSampleContract) return@LaunchedEffect
        val riskyIds = clauses.filter { it.riskLevel != ContractRiskLevel.SAFE }.map { it.id }
        riskyIds.forEach { id ->
            highlightedId = id
            delay(600)
        }
        highlightedId = null
    }

    val stats = Triple(
        clauses.count { it.riskLevel == ContractRiskLevel.SAFE },
        clauses.count { it.riskLevel == ContractRiskLevel.WARNING },
        clauses.count { it.riskLevel == ContractRiskLevel.DANGER },
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(palette.screenStart, palette.screenEnd))),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 10.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            ModuleHeader(
                module = AppModule.GUARDIAN,
                subtitle = "智能合同条款解读",
                onBack = onBack,
            )
        }

        contractUiState.errorMessage?.let { errorMessage ->
            item {
                ErrorNotice(message = errorMessage)
            }
        }

        if (showUploadPanel) {
            item {
                AppCard(borderColor = Amber100, shapeRadius = 32.dp) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(18.dp))
                                .background(Amber100),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = Icons.Filled.TipsAndUpdates,
                                contentDescription = null,
                                tint = Amber600,
                                modifier = Modifier.size(24.dp),
                            )
                        }

                        Column {
                            Text(
                                text = "上传合同文件",
                                style = MaterialTheme.typography.titleMedium,
                                color = AppTextPrimary,
                            )
                            Text(
                                text = "AI 将自动识别并分析条款风险",
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
                            .border(2.dp, Slate300, RoundedCornerShape(24.dp))
                            .clickable {
                                filePicker.launch(
                                    arrayOf(
                                        "application/pdf",
                                        "application/msword",
                                        "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                                        "image/*",
                                    ),
                                )
                            }
                            .padding(horizontal = 20.dp, vertical = 24.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(CircleShape)
                                    .background(Amber100),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.UploadFile,
                                    contentDescription = null,
                                    tint = Amber600,
                                    modifier = Modifier.size(30.dp),
                                )
                            }
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = "点击上传合同文件",
                                style = MaterialTheme.typography.titleMedium,
                                color = AppTextPrimary,
                            )
                            Text(
                                text = "支持 PDF、Word、图片格式",
                                style = MaterialTheme.typography.bodySmall,
                                color = AppTextSecondary,
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            PillTag(
                                text = "选择文件",
                                backgroundColor = Amber500,
                                contentColor = White,
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        UploadActionCard(
                            title = "拍照扫描",
                            subtitle = "拍摄纸质合同",
                            icon = Icons.Filled.PhotoCamera,
                            background = Blue100,
                            tint = Blue600,
                            modifier = Modifier.weight(1f),
                            onClick = {
                                filePicker.launch(
                                    arrayOf(
                                        "application/pdf",
                                        "application/msword",
                                        "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                                        "image/*",
                                    ),
                                )
                            },
                        )

                        UploadActionCard(
                            title = "示例合同",
                            subtitle = "查看样例结果",
                            icon = Icons.Filled.Description,
                            background = Pink100,
                            tint = Pink600,
                            modifier = Modifier.weight(1f),
                            onClick = {
                                uploadedFile = UploadedDocument("劳动合同示例.pdf", "268.4 KB")
                                showUploadPanel = false
                                isScanning = true
                                scanProgress = 0
                                shouldLoadSampleContract = true
                                pendingUploadName = null
                                pendingUploadBytes = null
                                clauses = emptyList()
                                summaryLabel = "分析中"
                                expandedId = null
                                contractViewModel.reset()
                            },
                        )
                    }
                }
            }

            item {
                AppCard(backgroundColor = Blue50, borderColor = Blue100, shapeRadius = 24.dp) {
                    Row(
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Lightbulb,
                            contentDescription = null,
                            tint = Blue600,
                            modifier = Modifier.size(20.dp),
                        )
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = "小贴士",
                                style = MaterialTheme.typography.labelLarge,
                                color = Blue600,
                            )
                            Text(
                                text = "契约卫士会重点关注竞业限制、保密条款、试用期、薪资福利等关键条款，并标注潜在风险点。",
                                style = MaterialTheme.typography.bodySmall,
                                color = Blue600,
                            )
                        }
                    }
                }
            }
        } else {
            item {
                uploadedFile?.let { file ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(24.dp))
                            .background(White)
                            .border(1.dp, AppBorder, RoundedCornerShape(24.dp))
                            .padding(14.dp),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Amber100),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Description,
                                    contentDescription = null,
                                    tint = Amber600,
                                    modifier = Modifier.size(24.dp),
                                )
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = file.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = AppTextPrimary,
                                )
                                Text(
                                    text = file.sizeLabel,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = AppTextSecondary,
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(Slate100)
                                    .clickable {
                                        uploadedFile = null
                                        showUploadPanel = true
                                        scanProgress = 0
                                        shouldLoadSampleContract = false
                                        pendingUploadName = null
                                        pendingUploadBytes = null
                                        clauses = emptyList()
                                        summaryLabel = "分析中"
                                        expandedId = null
                                        highlightedId = null
                                        contractViewModel.reset()
                                    },
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Close,
                                    contentDescription = "移除文件",
                                    tint = Slate400,
                                )
                            }
                        }
                    }
                }
            }

            if (isScanning) {
                item {
                    AppCard(borderColor = Amber100, shapeRadius = 32.dp) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Visibility,
                                contentDescription = null,
                                tint = Amber500,
                                modifier = Modifier.size(24.dp),
                            )
                            Column {
                                Text(
                                    text = "AI 正在审查合同条款",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = AppTextPrimary,
                                )
                                Text(
                                    text = "智能识别风险条款...",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = AppTextSecondary,
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(12.dp)
                                .clip(RoundedCornerShape(999.dp))
                                .background(Amber100),
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(scanProgress / 100f)
                                    .height(12.dp)
                                    .clip(RoundedCornerShape(999.dp))
                                    .background(Brush.horizontalGradient(listOf(Amber500, palette.gradientEnd))),
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "扫描进度 $scanProgress%",
                            style = MaterialTheme.typography.bodySmall,
                            color = AppTextSecondary,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            guardianScanStages.forEach { (label, threshold) ->
                                StatusStage(
                                    text = label,
                                    completed = scanProgress >= threshold,
                                    accentColor = Amber500,
                                )
                            }
                        }
                    }
                }
            } else {
                item {
                    AppCard(shapeRadius = 32.dp) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            SectionHeader(
                                icon = Icons.Filled.Description,
                                iconTint = Amber500,
                                title = "风险摘要",
                            )
                            PillTag(
                                text = summaryLabel,
                                backgroundColor = summaryBackground,
                                contentColor = summaryColor,
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            SummaryCard(value = stats.first.toString(), label = "安全条款", background = Emerald50, color = Emerald600, modifier = Modifier.weight(1f))
                            SummaryCard(value = stats.second.toString(), label = "需注意", background = Amber50, color = Amber600, modifier = Modifier.weight(1f))
                            SummaryCard(value = stats.third.toString(), label = "高风险", background = Red50, color = Red600, modifier = Modifier.weight(1f))
                        }
                    }
                }

                item {
                    SectionHeader(
                        icon = Icons.Filled.Shield,
                        iconTint = Amber500,
                        title = "条款详解",
                    )
                }

                itemsIndexed(clauses, key = { _, clause -> clause.id }) { _, clause ->
                    ClauseCard(
                        clause = clause,
                        expanded = expandedId == clause.id,
                        highlighted = highlightedId == clause.id && clause.riskLevel != ContractRiskLevel.SAFE,
                        onToggle = { expandedId = if (expandedId == clause.id) null else clause.id },
                    )
                }
            }
        }
    }
}

@Composable
private fun UploadActionCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    background: androidx.compose.ui.graphics.Color,
    tint: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(22.dp))
            .background(Slate100)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 14.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(background),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(20.dp))
            }
            Column {
                Text(text = title, style = MaterialTheme.typography.labelLarge, color = AppTextPrimary)
                Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = AppTextSecondary)
            }
        }
    }
}

@Composable
private fun SummaryCard(
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
            Text(text = value, style = MaterialTheme.typography.headlineMedium, color = color, fontWeight = FontWeight.Bold)
            Text(text = label, style = MaterialTheme.typography.bodySmall, color = color)
        }
    }
}

@Composable
private fun ClauseCard(
    clause: ContractClause,
    expanded: Boolean,
    highlighted: Boolean,
    onToggle: () -> Unit,
) {
    val (iconBg, textColor, detailBg) = contractRiskColors(clause.riskLevel)
    val borderColor = if (highlighted) textColor.copy(alpha = 0.28f) else AppBorder

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(White)
            .border(1.dp, borderColor, RoundedCornerShape(24.dp)),
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onToggle)
                    .padding(16.dp),
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
                            .clip(RoundedCornerShape(14.dp))
                            .background(iconBg),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = when (clause.riskLevel) {
                                ContractRiskLevel.SAFE -> Icons.Filled.CheckCircle
                                ContractRiskLevel.WARNING -> Icons.Filled.WarningAmber
                                ContractRiskLevel.DANGER -> Icons.Filled.Cancel
                            },
                            contentDescription = null,
                            tint = textColor,
                        )
                    }

                    Column {
                        Text(text = clause.title, style = MaterialTheme.typography.titleMedium, color = AppTextPrimary)
                        Text(text = clause.riskLevel.label, style = MaterialTheme.typography.bodySmall, color = textColor)
                    }
                }

                Icon(
                    imageVector = if (expanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                    contentDescription = null,
                    tint = Slate400,
                )
            }

            if (expanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    DetailBlock(title = null, icon = null, background = Slate100, tint = AppTextSecondary, content = clause.content)
                    DetailBlock(title = "AI 解读", icon = Icons.Filled.Visibility, background = detailBg, tint = textColor, content = clause.explanation)
                    clause.suggestion?.let {
                        DetailBlock(title = "修改建议", icon = Icons.Filled.Lightbulb, background = Blue50, tint = Blue600, content = it)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun DetailBlock(
    title: String?,
    icon: androidx.compose.ui.graphics.vector.ImageVector?,
    background: androidx.compose.ui.graphics.Color,
    tint: androidx.compose.ui.graphics.Color,
    content: String,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(background)
            .padding(14.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            if (title != null && icon != null) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(16.dp))
                    Text(text = title, style = MaterialTheme.typography.labelLarge, color = tint)
                }
            }
            Text(text = content, style = MaterialTheme.typography.bodyMedium, color = AppTextPrimary)
        }
    }
}

private fun android.content.Context.resolveDocument(uri: Uri): UploadedDocument? {
    var name = "未命名文件"
    var sizeLabel = "未知大小"

    contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE), null, null, null)?.use { cursor ->
        if (cursor.moveToFirst()) {
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
            if (nameIndex >= 0) {
                name = cursor.getString(nameIndex) ?: name
            }
            if (sizeIndex >= 0) {
                val bytes = cursor.getLong(sizeIndex)
                sizeLabel = if (bytes > 0) {
                    String.format("%.1f KB", bytes / 1024f)
                } else {
                    sizeLabel
                }
            }
        }
    }

    return UploadedDocument(name = name, sizeLabel = sizeLabel)
}
