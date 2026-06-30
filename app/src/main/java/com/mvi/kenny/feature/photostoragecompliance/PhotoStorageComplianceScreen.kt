package com.mvi.kenny.feature.photostoragecompliance

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import java.io.File

/**
 * ============================================================
 * PhotoStorageComplianceScreen — Photo Picker & Scoped Storage
 *                                      合规迁移工具包主屏幕
 * ============================================================
 *
 * PRD-308 | Android Photo Picker & Scoped Storage 合规迁移工具包
 * Ref: memory/agency/designs/PRD-308-Android-Photo-Picker-Scoped-Storage-Compliance-Migration-Toolkit.md
 *
 * Page structure follows the design document:
 * - Scan Configuration Page → Scan Progress → Scan Results (Tabbed)
 * - Tab 1: Photo Picker Violations / Tab 2: Health Connect Migration
 * - Report Preview / Export
 *
 * UI Color scheme from design:
 * - Primary: Deep Purple 700 (#5E35B1)
 * - Warning: Red 600 (#E53935) for violations
 * - Caution: Amber 700 (#FFA000) for pending migration
 * - Safe: Green 600 (#43A047) for compliant
 * - Background: Dark Surface (#1C1C1E)
 */

// ============================================================
// Color Constants / 颜色常量
// ============================================================

private val DeepPurple700 = Color(0xFF5E35B1)
private val Red600 = Color(0xFFE53935)
private val Amber700 = Color(0xFFFFA000)
private val Green600 = Color(0xFF43A047)
private val DarkSurface = Color(0xFF1C1C1E)
private val DarkCard = Color(0xFF2C2C2E)
private val CodeBackground = Color(0xFF1A1A1C)

// ============================================================
// Main Screen / 主屏幕
// ============================================================

/**
 * Photo Picker & Scoped Storage Compliance Screen
 * Photo Picker & Scoped Storage 合规迁移工具包主屏幕
 *
 * @param viewModel ViewModel for this screen / 屏幕 ViewModel
 * @param onNavigateBack Navigation callback / 导航回调
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhotoStorageComplianceScreen(
    viewModel: PhotoStorageComplianceViewModel,
    onNavigateBack: () -> Unit = {}
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    // Collect effects for one-time events / 收集副作用（一次性事件）
    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is PhotoStorageComplianceEffect.ShowToast -> {
                    snackbarHostState.showSnackbar(effect.message)
                }
                is PhotoStorageComplianceEffect.CopyToClipboard -> {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = ClipData.newPlainText("Fix Code", effect.text)
                    clipboard.setPrimaryClip(clip)
                }
                is PhotoStorageComplianceEffect.ShareReport -> {
                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                        type = effect.mimeType
                        putExtra(Intent.EXTRA_STREAM, effect.uri)
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    context.startActivity(Intent.createChooser(shareIntent, "Share Report"))
                }
                is PhotoStorageComplianceEffect.ScanComplete -> {
                    // Handled by state update / 由状态更新处理
                }
                is PhotoStorageComplianceEffect.ReportReady -> {
                    // Report is ready, handled by state / 报告已就绪，由状态处理
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Photo Picker 合规工具",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DeepPurple700,
                    titleContentColor = Color.White
                ),
                actions = {
                    // Help icon / 帮助图标
                    IconButton(onClick = { /* Show help */ }) {
                        Icon(Icons.Default.Description, contentDescription = "Help", tint = Color.White)
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = DarkSurface
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Main content based on scan phase / 根据扫描阶段显示主内容
            when (state.scanPhase) {
                ScanPhase.Idle -> {
                    if (state.photoViolations.isEmpty() && state.healthConnectItems.isEmpty()) {
                        // Show scan configuration / 显示扫描配置
                        ScanConfigContent(
                            state = state,
                            onIntent = viewModel::sendIntent
                        )
                    } else {
                        // Show scan results / 显示扫描结果
                        ScanResultsContent(
                            state = state,
                            onIntent = viewModel::sendIntent
                        )
                    }
                }
                else -> {
                    // Show scan progress / 显示扫描进度
                    ScanProgressContent(
                        scanPhase = state.scanPhase,
                        onCancel = { viewModel.sendIntent(PhotoStorageComplianceIntent.CancelScan) }
                    )
                }
            }
        }
    }
}

// ============================================================
// Scan Configuration Content / 扫描配置内容
// ============================================================

/**
 * Scan configuration page content
 * 扫描配置页面内容
 *
 * @param state Current page state / 当前页面状态
 * @param onIntent Intent handler / 意图处理器
 */
@Composable
private fun ScanConfigContent(
    state: PhotoStorageComplianceState,
    onIntent: (PhotoStorageComplianceIntent) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header card / 头部说明卡片
        item {
            ScanConfigHeaderCard()
        }

        // Scan scope selection / 扫描范围选择
        item {
            ScanScopeCard(
                selectedScope = state.scanScope,
                selectedModule = state.selectedModule,
                onScopeChange = { onIntent(PhotoStorageComplianceIntent.SelectScanScope(it)) },
                onModuleChange = { onIntent(PhotoStorageComplianceIntent.SelectModule(it)) }
            )
        }

        // Target API level selection / 目标API级别选择
        item {
            TargetApiLevelCard(
                selectedApiLevel = state.targetApiLevel,
                onApiLevelChange = { onIntent(PhotoStorageComplianceIntent.SelectTargetApiLevel(it)) }
            )
        }

        // Start scan button / 开始扫描按钮
        item {
            Button(
                onClick = { onIntent(PhotoStorageComplianceIntent.StartScan) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = state.canStartScan,
                colors = ButtonDefaults.buttonColors(containerColor = DeepPurple700),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "开始扫描 / Start Scan",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        }

        // Info card / 信息卡片
        item {
            ScanInfoCard()
        }
    }
}

/**
 * Header card explaining the tool purpose
 * 说明工具用途的头部卡片
 */
@Composable
private fun ScanConfigHeaderCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DarkCard),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Policy,
                    contentDescription = null,
                    tint = DeepPurple700,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Android Photo Picker & Scoped Storage 合规检测",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "检测 App 中直接使用 READ_MEDIA_* 权限和 Scoped Storage 例外条款的代码路径，生成 Photo Picker 迁移建议和 Health Connect 适配指南。",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                InfoChip(label = "API 35 = 警告", color = Amber700)
                InfoChip(label = "API 36 = 强制执行", color = Red600)
            }
        }
    }
}

/**
 * Scan scope selection card
 * 扫描范围选择卡片
 */
@Composable
private fun ScanScopeCard(
    selectedScope: ScanScope,
    selectedModule: String,
    onScopeChange: (ScanScope) -> Unit,
    onModuleChange: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DarkCard),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "扫描范围 / Scan Scope",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ScanScope.entries.forEach { scope ->
                    FilterChip(
                        selected = selectedScope == scope,
                        onClick = { onScopeChange(scope) },
                        label = { Text(scope.label) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = DeepPurple700,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }
            if (selectedScope == ScanScope.SPECIFIC_MODULE) {
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = selectedModule,
                    onValueChange = onModuleChange,
                    label = { Text("模块名称 / Module Name") },
                    placeholder = { Text("例如: app, library-core") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyMedium.copy(color = Color.White)
                )
            }
        }
    }
}

/**
 * Target API level selection card
 * 目标API级别选择卡片
 */
@Composable
private fun TargetApiLevelCard(
    selectedApiLevel: Int,
    onApiLevelChange: (Int) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DarkCard),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "目标 API 级别 / Target API Level",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "选择你要检测的 targetSdk 版本",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(35 to "API 35 (警告)", 36 to "API 36 (强制)").forEach { (level, label) ->
                    FilterChip(
                        selected = selectedApiLevel == level,
                        onClick = { onApiLevelChange(level) },
                        label = { Text(label) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = if (level == 36) Red600 else Amber700,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }
        }
    }
}

/**
 * Info card with compliance notes
 * 带合规说明的信息卡片
 */
@Composable
private fun ScanInfoCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DarkCard.copy(alpha = 0.7f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Warning,
                    contentDescription = null,
                    tint = Amber700,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "合规说明 / Compliance Notes",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            val notes = listOf(
                "• 本工具仅在本地执行分析，不收集/上传任何代码",
                "• QPR1 Beta 2 将 Photo Picker 强制执行范围扩展至 API 36",
                "• Health Connect 是健身/健康数据的唯一合规路径",
                "• JSON 报告格式与 GitHub Actions SARIF 对齐"
            )
            notes.forEach { note ->
                Text(
                    text = note,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    modifier = Modifier.padding(vertical = 2.dp)
                )
            }
        }
    }
}

// ============================================================
// Scan Progress Content / 扫描进度内容
// ============================================================

/**
 * Scan progress page with 3-phase animation
 * 带三阶段动画的扫描进度页面
 */
@Composable
private fun ScanProgressContent(
    scanPhase: ScanPhase,
    onCancel: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Animated progress indicator / 动画进度指示器
        val infiniteTransition = rememberInfiniteTransition(label = "scan_animation")
        val progress by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(1500, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "progress_animation"
        )

        Box(
            modifier = Modifier.size(120.dp),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                progress = { progress },
                modifier = Modifier.size(120.dp),
                color = DeepPurple700,
                strokeWidth = 8.dp,
                trackColor = DarkCard,
                strokeCap = StrokeCap.Round
            )
            Text(
                text = when (scanPhase) {
                    ScanPhase.StaticAnalysis -> "1/3"
                    ScanPhase.RuntimeDetection -> "2/3"
                    ScanPhase.Generating -> "3/3"
                    else -> "0/3"
                },
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Current phase label / 当前阶段标签
        Text(
            text = scanPhase.label,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = scanPhase.description,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Phase stepper / 阶段步骤指示器
        PhaseStepper(currentPhase = scanPhase)

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedButton(onClick = onCancel) {
            Text("取消 / Cancel")
        }
    }
}

/**
 * 3-phase stepper showing scan progress
 * 显示扫描进度的三阶段步骤指示器
 */
@Composable
private fun PhaseStepper(currentPhase: ScanPhase) {
    val phases = listOf(ScanPhase.StaticAnalysis, ScanPhase.RuntimeDetection, ScanPhase.Generating)
    val currentIndex = phases.indexOf(currentPhase).coerceAtLeast(0)

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        phases.forEachIndexed { index, phase ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                // Phase indicator dot / 阶段指示点
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(
                            when {
                                index < currentIndex -> Green600
                                index == currentIndex -> DeepPurple700
                                else -> DarkCard
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (index < currentIndex) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    } else if (index == currentIndex) {
                        CircularProgressIndicator(
                            progress = { 1f },
                            modifier = Modifier.size(20.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Phase label / 阶段标签
                Text(
                    text = phase.label,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (index <= currentIndex) Color.White else Color.Gray,
                    fontWeight = if (index == currentIndex) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
    }
}

// ============================================================
// Scan Results Content / 扫描结果内容
// ============================================================

/**
 * Scan results page with tabbed interface
 * 带 Tab 的扫描结果页面
 */
@Composable
private fun ScanResultsContent(
    state: PhotoStorageComplianceState,
    onIntent: (PhotoStorageComplianceIntent) -> Unit
) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf(
        "Photo Picker 违规 (${state.photoViolations.size})",
        "Health Connect 迁移 (${state.healthConnectItems.size})",
        "报告导出"
    )

    Column(modifier = Modifier.fillMaxSize()) {
        // Overall status banner / 整体状态横幅
        OverallStatusBanner(
            status = state.overallStatus,
            violationCount = state.photoViolations.size,
            healthItemCount = state.healthConnectItems.size
        )

        // Tab row / Tab 行
        TabRow(
            selectedTabIndex = selectedTabIndex,
            containerColor = DarkCard,
            contentColor = DeepPurple700
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = {
                        Text(
                            text = title,
                            color = if (selectedTabIndex == index) DeepPurple700 else Color.Gray
                        )
                    }
                )
            }
        }

        // Tab content / Tab 内容
        when (selectedTabIndex) {
            0 -> PhotoPickerViolationsTab(
                violations = state.photoViolations,
                selectedItems = state.selectedItems,
                onIntent = onIntent
            )
            1 -> HealthConnectMigrationTab(
                items = state.healthConnectItems,
                onIntent = onIntent
            )
            2 -> ReportExportTab(
                state = state,
                onIntent = onIntent
            )
        }
    }
}

/**
 * Overall compliance status banner
 * 整体合规状态横幅
 */
@Composable
private fun OverallStatusBanner(
    status: ComplianceStatus,
    violationCount: Int,
    healthItemCount: Int
) {
    val (icon, bgColor) = when (status) {
        ComplianceStatus.Compliant -> Icons.Default.CheckCircle to Green600
        ComplianceStatus.Warnings -> Icons.Default.Warning to Amber700
        ComplianceStatus.Violations -> Icons.Default.Error to Red600
        ComplianceStatus.Unknown -> Icons.Default.Policy to Color.Gray
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = bgColor.copy(alpha = 0.15f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = bgColor, modifier = Modifier.size(28.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = status.label,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = bgColor
                    )
                    Text(
                        text = "${violationCount} 违规 / ${healthItemCount} 迁移项",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
            }
            ComplianceStatusBadge(status = status)
        }
    }
}

/**
 * Compliance status badge component
 * 合规状态徽章组件
 */
@Composable
private fun ComplianceStatusBadge(status: ComplianceStatus) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(status.color.copy(alpha = 0.2f))
            .border(1.dp, status.color, RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = status.label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = status.color
        )
    }
}

// ============================================================
// Photo Picker Violations Tab / Photo Picker 违规 Tab
// ============================================================

/**
 * Photo Picker violations tab content
 * Photo Picker 违规 Tab 内容
 */
@Composable
private fun PhotoPickerViolationsTab(
    violations: List<Violation>,
    selectedItems: Set<String>,
    onIntent: (PhotoStorageComplianceIntent) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        // Batch action bar (shown when items selected) / 批量操作栏（选中项时显示）
        AnimatedVisibility(
            visible = selectedItems.isNotEmpty(),
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            BatchActionBar(
                selectedCount = selectedItems.size,
                onBatchCopy = { onIntent(PhotoStorageComplianceIntent.BatchCopyFixCode) },
                onSelectAll = { onIntent(PhotoStorageComplianceIntent.SelectAll) },
                onDeselectAll = { onIntent(PhotoStorageComplianceIntent.DeselectAll) }
            )
        }

        if (violations.isEmpty()) {
            // Empty state / 空状态
            EmptyViolationsState()
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Severity legend / 严重程度图例
                item {
                    SeverityLegend()
                }

                items(violations, key = { it.id }) { violation ->
                    ViolationItemCard(
                        violation = violation,
                        isSelected = violation.id in selectedItems,
                        onToggleSelection = { onIntent(PhotoStorageComplianceIntent.ToggleItemSelection(violation.id)) },
                        onToggleExpansion = { onIntent(PhotoStorageComplianceIntent.ToggleViolationExpansion(violation.id)) },
                        onCopyFix = { onIntent(PhotoStorageComplianceIntent.CopyFixCode(violation.id)) }
                    )
                }
            }
        }
    }
}

/**
 * Batch action bar for multi-select operations
 * 多选批量操作栏
 */
@Composable
private fun BatchActionBar(
    selectedCount: Int,
    onBatchCopy: () -> Unit,
    onSelectAll: () -> Unit,
    onDeselectAll: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = DeepPurple700.copy(alpha = 0.2f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "已选择 $selectedCount 项 / $selectedCount selected",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            Row {
                TextButton(onClick = onSelectAll) { Text("全选", color = DeepPurple700) }
                TextButton(onClick = onDeselectAll) { Text("取消", color = Color.Gray) }
                Button(
                    onClick = onBatchCopy,
                    colors = ButtonDefaults.buttonColors(containerColor = DeepPurple700)
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("批量复制", color = Color.White)
                }
            }
        }
    }
}

/**
 * Severity legend showing P0-P3 indicators
 * 严重程度图例，显示 P0-P3 指示器
 */
@Composable
private fun SeverityLegend() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        ViolationSeverity.entries.take(3).forEach { severity ->
            val color = when (severity) {
                ViolationSeverity.Critical -> Red600
                ViolationSeverity.High -> Amber700
                ViolationSeverity.Medium -> Color(0xFFFF9800)
                ViolationSeverity.Low -> Green600
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(color)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = severity.label,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray
                )
            }
        }
    }
}

/**
 * Empty violations state when no violations found
 * 未发现违规时的空状态
 */
@Composable
private fun EmptyViolationsState() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.CheckCircle,
            contentDescription = null,
            tint = Green600,
            modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "未发现违规 / No Violations Found",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Green600
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "您的代码已符合 Photo Picker 和 Scoped Storage 合规要求",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )
    }
}

/**
 * Single violation item card
 * 单个违规项卡片
 */
@Composable
private fun ViolationItemCard(
    violation: Violation,
    isSelected: Boolean,
    onToggleSelection: () -> Unit,
    onToggleExpansion: () -> Unit,
    onCopyFix: () -> Unit
) {
    val severityColor = when (violation.severity) {
        ViolationSeverity.Critical -> Red600
        ViolationSeverity.High -> Amber700
        ViolationSeverity.Medium -> Color(0xFFFF9800)
        ViolationSeverity.Low -> Green600
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
            .clickable { onToggleExpansion() },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) DeepPurple700.copy(alpha = 0.15f) else DarkCard
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Selection checkbox / 选择复选框
                Checkbox(
                    checked = isSelected,
                    onCheckedChange = { onToggleSelection() }
                )

                // Severity badge / 严重程度徽章
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(severityColor.copy(alpha = 0.2f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = violation.severity.label,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = severityColor
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Title / 标题
                Text(
                    text = violation.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.weight(1f)
                )

                // Expand icon / 展开图标
                Icon(
                    if (violation.isExpanded) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowRight,
                    contentDescription = "Toggle",
                    tint = Color.Gray
                )
            }

            // File path and line number / 文件路径和行号
            Text(
                text = "${violation.filePath}:${violation.lineNumber}",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.padding(start = 48.dp, top = 4.dp)
            )

            // Expanded details / 展开的详情
            AnimatedVisibility(
                visible = violation.isExpanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    // Original code / 原始代码
                    Text(
                        text = "问题代码 / Problematic Code:",
                        style = MaterialTheme.typography.labelSmall,
                        color = Red600,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(start = 48.dp)
                    )
                    CodeBlock(
                        code = violation.codeSnippet,
                        backgroundColor = Red600.copy(alpha = 0.1f)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Fix code / 修复代码
                    Text(
                        text = "修复代码 / Fix Code:",
                        style = MaterialTheme.typography.labelSmall,
                        color = Green600,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(start = 48.dp)
                    )
                    CodeBlock(
                        code = violation.fixCode,
                        backgroundColor = Green600.copy(alpha = 0.1f)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Copy fix button / 复制修复按钮
                    Button(
                        onClick = onCopyFix,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Green600)
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("复制修复代码 / Copy Fix Code", color = Color.White)
                    }
                }
            }
        }
    }
}

/**
 * Code block display component
 * 代码块展示组件
 */
@Composable
private fun CodeBlock(
    code: String,
    backgroundColor: Color
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 48.dp, top = 4.dp),
        color = backgroundColor,
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = code,
            style = MaterialTheme.typography.bodySmall,
            color = Color.White,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.padding(12.dp)
        )
    }
}

// ============================================================
// Health Connect Migration Tab / Health Connect 迁移 Tab
// ============================================================

/**
 * Health Connect migration tab content
 * Health Connect 迁移 Tab 内容
 */
@Composable
private fun HealthConnectMigrationTab(
    items: List<HealthMigrationItem>,
    onIntent: (PhotoStorageComplianceIntent) -> Unit
) {
    if (items.isEmpty()) {
        EmptyHealthConnectState()
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(items, key = { it.id }) { item ->
                HealthMigrationCard(
                    item = item,
                    onToggleExpansion = { onIntent(PhotoStorageComplianceIntent.ToggleHealthItemExpansion(item.id)) },
                    onAdvanceStep = { onIntent(PhotoStorageComplianceIntent.AdvanceHealthStep(item.id)) }
                )
            }
        }
    }
}

/**
 * Empty state for Health Connect tab
 * Health Connect Tab 空状态
 */
@Composable
private fun EmptyHealthConnectState() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.CheckCircle,
            contentDescription = null,
            tint = Green600,
            modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "无需 Health Connect 迁移 / No Health Connect Migration Needed",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Green600
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "您的 App 未检测到直接传感器读取健康数据",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )
    }
}

/**
 * Health Connect migration card with 3-step stepper
 * 带三步流程的 Health Connect 迁移卡片
 */
@Composable
private fun HealthMigrationCard(
    item: HealthMigrationItem,
    onToggleExpansion: () -> Unit,
    onAdvanceStep: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
            .clickable { onToggleExpansion() },
        colors = CardDefaults.cardColors(containerColor = DarkCard),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Amber badge for pending migration / 待迁移项使用琥珀色徽章
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(Amber700.copy(alpha = 0.2f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "待迁移",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Amber700
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = item.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.weight(1f)
                )

                Icon(
                    if (item.isExpanded) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowRight,
                    contentDescription = "Toggle",
                    tint = Color.Gray
                )
            }

            // 3-step progress indicator / 三步进度指示器
            if (!item.isExpanded) {
                Spacer(modifier = Modifier.height(8.dp))
                HealthConnectStepper(currentStep = item.currentStep, totalSteps = item.migrationSteps.size)
            }

            // Expanded migration steps / 展开的迁移步骤
            AnimatedVisibility(
                visible = item.isExpanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    // Current code / 当前代码
                    Text(
                        text = "当前代码 / Current Code:",
                        style = MaterialTheme.typography.labelSmall,
                        color = Amber700,
                        fontWeight = FontWeight.Bold
                    )
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = CodeBackground,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = item.currentCode,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.padding(12.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // 3-step migration guide / 三步迁移指南
                    HealthConnectStepper(currentStep = item.currentStep, totalSteps = item.migrationSteps.size)

                    Spacer(modifier = Modifier.height(12.dp))

                    // Step content / 步骤内容
                    item.migrationSteps.forEachIndexed { index, step ->
                        val isActive = index == item.currentStep
                        val isCompleted = index < item.currentStep

                        MigrationStepCard(
                            step = step,
                            isActive = isActive,
                            isCompleted = isCompleted
                        )
                        if (index < item.migrationSteps.lastIndex) {
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Next step button / 下一步按钮
                    if (item.currentStep < item.migrationSteps.size - 1) {
                        Button(
                            onClick = onAdvanceStep,
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = DeepPurple700)
                        ) {
                            Text("下一步: ${item.migrationSteps[item.currentStep + 1].title}", color = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(Icons.Default.KeyboardArrowRight, contentDescription = null, tint = Color.White)
                        }
                    } else {
                        Button(
                            onClick = onAdvanceStep,
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = Green600)
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("迁移完成 / Migration Complete", color = Color.White)
                        }
                    }
                }
            }
        }
    }
}

/**
 * Health Connect 3-step progress indicator
 * Health Connect 三步进度指示器
 */
@Composable
private fun HealthConnectStepper(currentStep: Int, totalSteps: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(totalSteps) { index ->
            val isCompleted = index < currentStep
            val isActive = index == currentStep

            // Step circle / 步骤圆圈
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(
                        when {
                            isCompleted -> Green600
                            isActive -> DeepPurple700
                            else -> DarkCard
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isCompleted) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                } else {
                    Text(
                        text = "${index + 1}",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isActive) Color.White else Color.Gray
                    )
                }
            }

            // Connector line / 连接线
            if (index < totalSteps - 1) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(2.dp)
                        .background(if (index < currentStep) Green600 else DarkCard)
                )
            }
        }
    }
}

/**
 * Single migration step card
 * 单个迁移步骤卡片
 */
@Composable
private fun MigrationStepCard(
    step: MigrationStep,
    isActive: Boolean,
    isCompleted: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when {
                isActive -> DeepPurple700.copy(alpha = 0.2f)
                isCompleted -> Green600.copy(alpha = 0.1f)
                else -> DarkCard.copy(alpha = 0.5f)
            }
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "步骤 ${step.stepNumber}",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (isActive) DeepPurple700 else if (isCompleted) Green600 else Color.Gray
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = step.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                if (isCompleted) {
                    Spacer(modifier = Modifier.weight(1f))
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Green600,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            if (isActive) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = step.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = CodeBackground,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = step.code,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(10.dp)
                    )
                }
            }
        }
    }
}

// ============================================================
// Report Export Tab / 报告导出 Tab
// ============================================================

/**
 * Report export tab content
 * 报告导出 Tab 内容
 */
@Composable
private fun ReportExportTab(
    state: PhotoStorageComplianceState,
    onIntent: (PhotoStorageComplianceIntent) -> Unit
) {
    val context = LocalContext.current

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Report summary card / 报告摘要卡片
        item {
            ReportSummaryCard(state = state)
        }

        // Export format selection / 导出格式选择
        item {
            ExportFormatCard(
                selectedFormat = state.selectedExportFormat,
                onFormatChange = { onIntent(PhotoStorageComplianceIntent.SelectExportFormat(it)) }
            )
        }

        // Export button / 导出按钮
        item {
            Button(
                onClick = { onIntent(PhotoStorageComplianceIntent.ExportReport(state.selectedExportFormat)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = !state.isExporting,
                colors = ButtonDefaults.buttonColors(containerColor = DeepPurple700),
                shape = RoundedCornerShape(16.dp)
            ) {
                if (state.isExporting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("导出中... / Exporting...", color = Color.White)
                } else {
                    Icon(Icons.Default.FileDownload, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "导出 ${state.selectedExportFormat.name} 报告",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Report format info / 报告格式说明
        item {
            ReportFormatInfoCard(format = state.selectedExportFormat)
        }

        // Previously exported report / 已有报告
        state.exportedReport?.let { report ->
            item {
                ExportedReportCard(
                    report = report,
                    onShare = {
                        // In production, would create file and share
                        onIntent(PhotoStorageComplianceIntent.ShareReport(Uri.EMPTY))
                    }
                )
            }
        }
    }
}

/**
 * Report summary card showing scan results overview
 * 报告摘要卡片，显示扫描结果概览
 */
@Composable
private fun ReportSummaryCard(state: PhotoStorageComplianceState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DarkCard),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Description,
                    contentDescription = null,
                    tint = DeepPurple700,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "报告摘要 / Report Summary",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(
                    label = "Photo Picker 违规",
                    value = "${state.photoViolations.size}",
                    color = if (state.photoViolations.isNotEmpty()) Red600 else Green600
                )
                StatItem(
                    label = "Health Connect 迁移项",
                    value = "${state.healthConnectItems.size}",
                    color = if (state.healthConnectItems.isNotEmpty()) Amber700 else Green600
                )
                StatItem(
                    label = "目标 API",
                    value = "${state.targetApiLevel}",
                    color = DeepPurple700
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "整体状态 / Overall:",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.width(8.dp))
                ComplianceStatusBadge(status = state.computeOverallStatus())
            }
        }
    }
}

/**
 * Single statistic item for summary card
 * 摘要卡片的单个统计项
 */
@Composable
private fun StatItem(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = Color.Gray
        )
    }
}

/**
 * Export format selection card
 * 导出格式选择卡片
 */
@Composable
private fun ExportFormatCard(
    selectedFormat: ExportFormat,
    onFormatChange: (ExportFormat) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DarkCard),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "导出格式 / Export Format",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                ExportFormat.entries.forEach { format ->
                    FilterChip(
                        selected = selectedFormat == format,
                        onClick = { onFormatChange(format) },
                        label = {
                            Text(
                                text = when (format) {
                                    ExportFormat.PDF -> "PDF (人类可读)"
                                    ExportFormat.JSON -> "JSON (CI/CD集成)"
                                }
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = DeepPurple700,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }
        }
    }
}

/**
 * Report format information card
 * 报告格式说明卡片
 */
@Composable
private fun ReportFormatInfoCard(format: ExportFormat) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DarkCard.copy(alpha = 0.7f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "格式说明 / Format Details",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(8.dp))
            when (format) {
                ExportFormat.PDF -> {
                    Text("• 人类可读的完整合规报告", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    Text("• 包含所有违规项和修复建议", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    Text("• 适合提交给合规审核团队", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                }
                ExportFormat.JSON -> {
                    Text("• 机器可读的合规数据", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    Text("• 与 GitHub Actions SARIF 格式对齐", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    Text("• 适合 CI/CD 流水线集成", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                }
            }
        }
    }
}

/**
 * Exported report card showing previously generated report
 * 已导出报告卡片，显示之前生成的报告
 */
@Composable
private fun ExportedReportCard(
    report: Report,
    onShare: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Green600.copy(alpha = 0.1f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.CheckCircle,
                contentDescription = null,
                tint = Green600,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "报告已生成 / Report Generated",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = Green600
                )
                Text(
                    text = "${report.photoViolations} 违规 / ${report.healthConnectItems} 迁移项",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
            Button(
                onClick = onShare,
                colors = ButtonDefaults.buttonColors(containerColor = Green600)
            ) {
                Icon(Icons.Default.FileDownload, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(4.dp))
                Text("分享", color = Color.White)
            }
        }
    }
}

// ============================================================
// Helper Composables / 辅助组件
// ============================================================

/**
 * Info chip for displaying small labels
 * 显示小标签的信息芯片
 */
@Composable
private fun InfoChip(label: String, color: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(color.copy(alpha = 0.15f))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.Medium
        )
    }
}
