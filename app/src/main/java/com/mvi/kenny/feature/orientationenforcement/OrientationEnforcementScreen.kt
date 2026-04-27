package com.mvi.kenny.feature.orientationenforcement

// ================================================================
// OrientationEnforcementScreen — Android 17 大屏方向锁定合规检测工具主界面
// ================================================================
// Composable screen for Android 17 Large Screen Orientation Enforcement compliance toolkit.
//
// PRD-183: Android 17 Large Screen Resizability & Orientation Enforcement 合规检测工具包
// Design Reference: memory/agency/designs/PRD-183-Android-17-Large-Screen-Resizability-Orientation-Enforcement.md
//
// UI 架构 / UI Architecture:
//   MVI 模式：State → Composable → Intent → ViewModel → State
//   状态驱动：所有 UI 变化由 State 驱动，无直接 UI 状态修改
//
// 页面结构 / Page Structure:
//   1. DashboardScreen — 首页扫描仪表盘
//   2. ScanResultScreen — 扫描结果详情页
//   3. MigrationWizardScreen — 修复向导页（Step 1-4）
//   4. WindowPreviewScreen — 窗口大小适配验证页
//   5. HistoryScreen — 历史记录页
// ================================================================

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mvi.kenny.feature.orientationenforcement.ComplianceStatus as Status
import com.mvi.kenny.feature.orientationenforcement.ScanPhase as Phase
import com.mvi.kenny.feature.orientationenforcement.ToolId as TId
import com.mvi.kenny.feature.orientationenforcement.ViolationType as VType
import com.mvi.kenny.feature.orientationenforcement.ViolationSeverity as VSeverity
import com.mvi.kenny.feature.orientationenforcement.MigrationStep as MStep

// ================================================================
// 颜色系统 / Color System
// ================================================================
// Design Doc Reference: Section 7 视觉规范
// | 语义 | 颜色 | 用途 |
// |------|------|------|
// | Primary | #4285F4 | 主按钮、选中状态 |
// | Error | #D32F2F | 违规项标识 |
// | Warning | #F9A825 | 警告项标识 |
// | Success | #388E3C | 合规项标识 |
// | Background | #121212 | 页面背景 |
// | Surface | #1E1E1E | 卡片背景 |
// | CodeBlock BG | #2D2D2D | 代码展示区背景 |

private val DarkBackground = Color(0xFF121212)
private val DarkSurface = Color(0xFF1E1E1E)
private val CodeBlockBg = Color(0xFF2D2D2D)
private val CodeLineAdded = Color(0xFF1B5E20)
private val CodeLineRemoved = Color(0xFFB71C1C)
private val AndroidBlue = Color(0xFF4285F4)

// ================================================================
// OrientationEnforcementScreen — 主页容器
// ================================================================

/**
 * OrientationEnforcementScreen — 主屏幕容器
 * Routes between dashboard and tool detail screens based on currentToolId.
 *
 * @param viewModel The MVI ViewModel
 * @param onNavigateBack Callback when user wants to go back
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrientationEnforcementScreen(
    viewModel: OrientationEnforcementViewModel,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val effect by viewModel.effect.collectAsStateWithLifecycle()

    // Handle effects / 处理副作用
    LaunchedEffect(effect) {
        effect?.let {
            when (it) {
                is OrientationEnforcementEffect.ShowToast -> {
                    // Toast would be shown here
                    viewModel.consumeEffect()
                }
                is OrientationEnforcementEffect.CopyToClipboard -> {
                    viewModel.consumeEffect()
                }
                is OrientationEnforcementEffect.ShareReport -> {
                    viewModel.consumeEffect()
                }
                is OrientationEnforcementEffect.NavigateToViolation -> {
                    viewModel.consumeEffect()
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Android 17 大屏合规检测",
                        style = MaterialTheme.typography.titleMedium
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "返回"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkSurface
                )
            )
        },
        containerColor = DarkBackground
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                // Tool detail view / 工具详情页
                state.currentToolId != null -> {
                    ToolDetailScreen(
                        toolId = state.currentToolId!!,
                        state = state,
                        onBack = { viewModel.sendIntent(OrientationEnforcementIntent.BackToDashboard) },
                        onGenerateDiff = { v -> viewModel.sendIntent(OrientationEnforcementIntent.GenerateDiff(v)) },
                        onApplyDiff = { d -> viewModel.sendIntent(OrientationEnforcementIntent.ApplyDiff(d)) },
                        onSetMigrationStep = { s -> viewModel.sendIntent(OrientationEnforcementIntent.SetMigrationStep(s)) },
                        onSetWindowSizeClass = { w -> viewModel.sendIntent(OrientationEnforcementIntent.SetWindowSizeClass(w)) }
                    )
                }
                // Scan result detail view / 扫描结果详情
                state.scanResult != null && state.scanPhase == Phase.Completed -> {
                    ScanResultScreen(
                        state = state,
                        onViolationClick = { v -> viewModel.sendIntent(OrientationEnforcementIntent.SelectViolation(v)) },
                        onToolClick = { t -> viewModel.sendIntent(OrientationEnforcementIntent.SelectTool(t)) },
                        onBackToDashboard = { viewModel.sendIntent(OrientationEnforcementIntent.BackToDashboard) },
                        onExportReport = { f -> viewModel.sendIntent(OrientationEnforcementIntent.ExportReport(f)) }
                    )
                }
                // Main dashboard / 主仪表盘
                else -> {
                    DashboardScreen(
                        state = state,
                        onSelectApp = { app -> viewModel.sendIntent(OrientationEnforcementIntent.SelectApp(app)) },
                        onStartScan = { viewModel.sendIntent(OrientationEnforcementIntent.StartScan) },
                        onCancelScan = { viewModel.sendIntent(OrientationEnforcementIntent.CancelScan) },
                        onToolClick = { t -> viewModel.sendIntent(OrientationEnforcementIntent.SelectTool(t)) }
                    )
                }
            }
        }
    }
}

// ================================================================
// DashboardScreen — 首页扫描仪表盘
// ================================================================
// Design Doc Reference: Section 3.1 首页 / 扫描仪表盘
// - App 选择器（从已安装/本地项目中选择目标 App）
// - 扫描启动按钮
// - 扫描进度指示器
// - 扫描结果总览卡片：合规项数 / 警告项数 / 违规项数

@Composable
private fun DashboardScreen(
    state: OrientationEnforcementState,
    onSelectApp: (AppInfo) -> Unit,
    onStartScan: () -> Unit,
    onCancelScan: () -> Unit,
    onToolClick: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // App Selector Card / App 选择器卡片
        AppSelectorCard(
            selectedApp = state.selectedApp,
            onSelectApp = onSelectApp
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Scan Button / 扫描按钮
        ScanControlCard(
            scanPhase = state.scanPhase,
            progress = state.progress,
            onStartScan = onStartScan,
            onCancelScan = onCancelScan
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Tool Grid / 工具网格
        Text(
            text = "合规检测工具",
            style = MaterialTheme.typography.titleMedium,
            color = Color(0xFFE0E0E0),
            modifier = Modifier.padding(vertical = 8.dp)
        )

        // 2-column grid of tool cards / 双列工具卡片网格
        val tools = defaultOrientationToolStatuses
        val rows = tools.chunked(2)

        rows.forEach { rowTools ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                rowTools.forEach { tool ->
                    ToolCard(
                        tool = tool,
                        onClick = { onToolClick(tool.toolId) },
                        modifier = Modifier.weight(1f)
                    )
                }
                // Fill empty space if odd number / 奇数时填充空白
                if (rowTools.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        Spacer(modifier = Modifier.height(80.dp))
    }
}

/**
 * AppSelectorCard — App 选择器卡片
 * Allows user to select an Android app to scan.
 */
@Composable
private fun AppSelectorCard(
    selectedApp: AppInfo?,
    onSelectApp: (AppInfo) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "选择目标 App",
                style = MaterialTheme.typography.titleSmall,
                color = Color(0xFFE0E0E0)
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Demo apps for selection / 演示用 App 列表
            val demoApps = listOf(
                AppInfo("com.example.portraitlock", "Portrait Lock Demo", "1.0.0", 34),
                AppInfo("com.example.resizabledemo", "Resizable Demo", "2.1.0", 35),
                AppInfo("com.example.legacyapp", "Legacy App", "3.5.2", 31)
            )

            demoApps.forEach { app ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = selectedApp?.packageName == app.packageName,
                        onClick = { onSelectApp(app) },
                        colors = RadioButtonDefaults.colors(
                            selectedColor = AndroidBlue
                        )
                    )
                    Column(modifier = Modifier.padding(start = 8.dp)) {
                        Text(
                            text = app.appName,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFFE0E0E0)
                        )
                        Text(
                            text = "${app.packageName} (SDK ${app.targetSdk})",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF9E9E9E)
                        )
                    }
                }
            }
        }
    }
}

/**
 * ScanControlCard — 扫描控制卡片
 * Displays scan progress and controls.
 */
@Composable
private fun ScanControlCard(
    scanPhase: Phase,
    progress: Float,
    onStartScan: () -> Unit,
    onCancelScan: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when (scanPhase) {
                Phase.Idle -> {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = AndroidBlue,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "点击开始扫描，检测方向锁定合规性",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF9E9E9E),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = onStartScan,
                        colors = ButtonDefaults.buttonColors(containerColor = AndroidBlue)
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("开始扫描")
                    }
                }
                Phase.Scanning -> {
                    CircularProgressIndicator(
                        progress = { progress },
                        color = AndroidBlue,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "扫描中... ${(progress * 100).toInt()}%",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFFE0E0E0)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    LinearProgressIndicator(
                        progress = { progress },
                        color = AndroidBlue,
                        trackColor = DarkSurface,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedButton(onClick = onCancelScan) {
                        Text("取消")
                    }
                }
                Phase.Completed -> {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Color(0xFF3FB950),
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "扫描完成",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF3FB950)
                    )
                }
                Phase.Error -> {
                    Icon(
                        imageVector = Icons.Default.Error,
                        contentDescription = null,
                        tint = Color(0xFFF85149),
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "扫描出错",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFFF85149)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = onStartScan,
                        colors = ButtonDefaults.buttonColors(containerColor = AndroidBlue)
                    ) {
                        Text("重试")
                    }
                }
            }
        }
    }
}

/**
 * ToolCard — 工具卡片
 * Displays a single tool in the dashboard grid.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ToolCard(
    tool: ToolStatus,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = tool.iconEmoji,
                    fontSize = 20.sp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = tool.toolName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFFE0E0E0),
                    fontWeight = FontWeight.Medium
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = tool.toolDescription,
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF9E9E9E),
                maxLines = 2
            )
            Spacer(modifier = Modifier.height(8.dp))
            // Status chip / 状态标签
            SuggestionChip(
                onClick = { },
                label = {
                    Text(
                        text = tool.status.displayName,
                        style = MaterialTheme.typography.labelSmall
                    )
                },
                colors = SuggestionChipDefaults.suggestionChipColors(
                    containerColor = tool.status.color.copy(alpha = 0.2f),
                    labelColor = tool.status.color
                )
            )
        }
    }
}

// ================================================================
// ScanResultScreen — 扫描结果详情页
// ================================================================
// Design Doc Reference: Section 3.2 扫描结果详情页
// - Manifest 扫描结果区
// - 代码扫描结果区
// - 每个违规项提供「查看详情」和「生成修复代码」按钮

@Composable
private fun ScanResultScreen(
    state: OrientationEnforcementState,
    onViolationClick: (ViolationItem) -> Unit,
    onToolClick: (String) -> Unit,
    onBackToDashboard: () -> Unit,
    onExportReport: (String) -> Unit
) {
    val result = state.scanResult ?: return

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Summary Cards / 摘要卡片
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SummaryCard(
                title = "违规项",
                value = result.totalViolations.toString(),
                color = Color(0xFFF85149),
                modifier = Modifier.weight(1f)
            )
            SummaryCard(
                title = "警告项",
                value = result.warningCount.toString(),
                color = Color(0xFFFFD700),
                modifier = Modifier.weight(1f)
            )
            SummaryCard(
                title = "合规项",
                value = result.passCount.toString(),
                color = Color(0xFF3FB950),
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Manifest Violations Section / Manifest 违规区
        if (result.manifestViolations.isNotEmpty()) {
            SectionHeader(title = "Manifest 违规", emoji = "📋")
            result.manifestViolations.forEach { violation ->
                ViolationCard(
                    violation = violation,
                    onClick = { onViolationClick(violation) }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Code Violations Section / 代码违规区
        if (result.codeViolations.isNotEmpty()) {
            SectionHeader(title = "代码违规", emoji = "💻")
            result.codeViolations.forEach { violation ->
                ViolationCard(
                    violation = violation,
                    onClick = { onViolationClick(violation) }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Action Buttons / 操作按钮
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onBackToDashboard,
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("返回")
            }
            Button(
                onClick = { onExportReport("markdown") },
                colors = ButtonDefaults.buttonColors(containerColor = AndroidBlue),
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.Share, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("导出报告")
            }
        }

        Spacer(modifier = Modifier.height(80.dp))
    }
}

/**
 * SummaryCard — 摘要卡片
 */
@Composable
private fun SummaryCard(
    title: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                color = color,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF9E9E9E)
            )
        }
    }
}

/**
 * SectionHeader — 区块标题
 */
@Composable
private fun SectionHeader(title: String, emoji: String) {
    Row(
        modifier = Modifier.padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = emoji, fontSize = 20.sp)
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = Color(0xFFE0E0E0)
        )
    }
}

/**
 * ViolationCard — 违规项卡片
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ViolationCard(
    violation: ViolationItem,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Severity badge / 严重程度标签
                SuggestionChip(
                    onClick = { },
                    label = { Text(violation.severity.displayName, style = MaterialTheme.typography.labelSmall) },
                    colors = SuggestionChipDefaults.suggestionChipColors(
                        containerColor = when (violation.severity) {
                            VSeverity.HIGH -> Color(0xFFF85149).copy(alpha = 0.2f)
                            VSeverity.MEDIUM -> Color(0xFFFFD700).copy(alpha = 0.2f)
                            VSeverity.LOW -> Color(0xFF3FB950).copy(alpha = 0.2f)
                        },
                        labelColor = when (violation.severity) {
                            VSeverity.HIGH -> Color(0xFFF85149)
                            VSeverity.MEDIUM -> Color(0xFFFFD700)
                            VSeverity.LOW -> Color(0xFF3FB950)
                        }
                    )
                )
                // Type badge / 类型标签
                Text(
                    text = violation.type.displayName,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF9E9E9E)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = violation.description,
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFFE0E0E0)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Location / 位置
            Text(
                text = "${violation.filePath}:${violation.lineNumber}",
                style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                color = Color(0xFF9E9E9E)
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "当前值: ${violation.currentValue} → 期望值: ${violation.expectedValue}",
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFF9E9E9E)
            )
        }
    }
}

// ================================================================
// ToolDetailScreen — 工具详情页
// ================================================================
// Shows detailed view for a specific tool.

@Composable
private fun ToolDetailScreen(
    toolId: String,
    state: OrientationEnforcementState,
    onBack: () -> Unit,
    onGenerateDiff: (ViolationItem) -> Unit,
    onApplyDiff: (DiffResult) -> Unit,
    onSetMigrationStep: (MStep) -> Unit,
    onSetWindowSizeClass: (WindowSizeClassOption) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Header with back button / 返回按钮
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "返回",
                    tint = Color(0xFFE0E0E0)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            val tool = defaultOrientationToolStatuses.find { it.toolId == toolId }
            Text(
                text = tool?.toolName ?: "工具详情",
                style = MaterialTheme.typography.titleMedium,
                color = Color(0xFFE0E0E0)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        when (toolId) {
            TId.ORIENTATION_LOCK_SCANNER -> OrientationLockScannerDetail()
            TId.RESIZABLE_ACTIVITY_DETECTOR -> ResizableActivityDetectorDetail()
            TId.CONFIG_CHANGE_VALIDATOR -> ConfigChangeValidatorDetail()
            TId.ORIENTATION_MIGRATION_GUIDE -> MigrationGuideDetail(
                currentStep = state.migrationStep,
                generatedDiff = state.generatedDiff,
                onSetMigrationStep = onSetMigrationStep,
                onApplyDiff = onApplyDiff
            )
            TId.WINDOW_SIZE_CLASS_CI -> WindowSizeClassCIDetail(
                selectedWsc = state.windowSizeClass,
                onSetWindowSizeClass = onSetWindowSizeClass
            )
            TId.LEGACY_APP_MIGRATION_PATH -> LegacyMigrationPathDetail()
            TId.ACCESSIBILITY_ORIENTATION_CHECK -> AccessibilityOrientationDetail()
            TId.CONFIG_CHANGE_STATE_SAVER -> ConfigChangeStateSaverDetail()
            else -> Text("工具详情", color = Color(0xFFE0E0E0))
        }

        Spacer(modifier = Modifier.height(80.dp))
    }
}

// ================================================================
// Tool Detail Sections — 工具详情子页面
// ================================================================

/**
 * OrientationLockScannerDetail — 方向锁定扫描器详情
 */
@Composable
private fun OrientationLockScannerDetail() {
    Card(
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "🔍 方向锁定扫描器",
                style = MaterialTheme.typography.titleMedium,
                color = Color(0xFFE0E0E0)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "扫描 AndroidManifest.xml 和代码中的 screenOrientation 锁定用法，识别受影响文件和行号。",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF9E9E9E)
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Scan items preview / 扫描项预览
            val items = listOf(
                "android:screenOrientation=\"portrait\" — 竖屏锁定",
                "android:screenOrientation=\"landscape\" — 横屏锁定",
                "android:screenOrientation=\"locked\" — 方向锁定",
                "setRequestedOrientation() — 代码中动态锁定"
            )
            items.forEach { item ->
                Row(
                    modifier = Modifier.padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.RadioButtonUnchecked,
                        contentDescription = null,
                        tint = Color(0xFF9E9E9E),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = item,
                        style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                        color = Color(0xFFE0E0E0)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Note about sensor / 注意 sensor 不等于合规
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF9A825).copy(alpha = 0.1f)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Text(text = "⚠️", fontSize = 16.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "注意：screenOrientation=\"sensor\" 不等于合规！需要同时处理 resizeableActivity=true。",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFF9A825)
                    )
                }
            }
        }
    }
}

/**
 * ResizableActivityDetectorDetail — ResizableActivity 合规检测详情
 */
@Composable
private fun ResizableActivityDetectorDetail() {
    Card(
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "📐 ResizableActivity 合规检测",
                style = MaterialTheme.typography.titleMedium,
                color = Color(0xFFE0E0E0)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "检测 resizeableActivity=\"false\" 声明，识别会在 Android 17 大屏（sw≥600dp）上被系统杀死的场景。",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF9E9E9E)
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Risk description / 风险描述
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF85149).copy(alpha = 0.1f)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "🔴 高风险场景",
                        style = MaterialTheme.typography.titleSmall,
                        color = Color(0xFFF85149)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "当 android:resizeableActivity=\"false\" 时，App 在可调整大小窗口中会被系统直接杀死（Force Stop）。Android 17 从此不再允许此退出选项。",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFE0E0E0)
                    )
                }
            }
        }
    }
}

/**
 * ConfigChangeValidatorDetail — 配置变更验证器详情
 */
@Composable
private fun ConfigChangeValidatorDetail() {
    Card(
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "🔄 配置变更状态验证器",
                style = MaterialTheme.typography.titleMedium,
                color = Color(0xFFE0E0E0)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "检测 App 在屏幕旋转/窗口调整后是否正确调用 onSaveInstanceState，验证状态恢复完整性。",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF9E9E9E)
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Checklist / 检查清单
            listOf(
                "Activity 重写了 onSaveInstanceState(outState: Bundle)",
                "在 onSaveInstanceState 中保存了所有关键 UI 状态",
                "Activity 重写了 onRestoreInstanceState(savedInstanceState: Bundle)",
                "Bundle 包含非空检查：savedInstanceState?.getString(...) ?: defaultValue"
            ).forEach { item ->
                Row(
                    modifier = Modifier.padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.CheckBoxOutlineBlank,
                        contentDescription = null,
                        tint = Color(0xFF9E9E9E),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = item,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFE0E0E0)
                    )
                }
            }
        }
    }
}

/**
 * MigrationGuideDetail — 迁移指南详情（含分步骤向导）
 */
@Composable
private fun MigrationGuideDetail(
    currentStep: MStep,
    generatedDiff: DiffResult?,
    onSetMigrationStep: (MStep) -> Unit,
    onApplyDiff: (DiffResult) -> Unit
) {
    Column {
        Card(
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "🛠️ 方向锁定移除迁移指南",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color(0xFFE0E0E0)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "从 portrait-only 迁移到双向布局的渐进路径，Compose/View 双版本代码 Diff。",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF9E9E9E)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Step indicator / 步骤指示器
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            MStep.entries.forEach { step ->
                val isActive = step == currentStep
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(
                                color = if (isActive) AndroidBlue else DarkSurface,
                                shape = RoundedCornerShape(16.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = step.stepNumber.toString(),
                            style = MaterialTheme.typography.labelMedium,
                            color = if (isActive) Color.White else Color(0xFF9E9E9E)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = step.title,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isActive) Color(0xFFE0E0E0) else Color(0xFF9E9E9E),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Step content / 步骤内容
        Card(
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Step ${currentStep.stepNumber}: ${currentStep.title}",
                    style = MaterialTheme.typography.titleSmall,
                    color = AndroidBlue
                )
                Spacer(modifier = Modifier.height(12.dp))

                when (currentStep) {
                    MStep.MANIFEST_LOCK_REMOVAL -> {
                        Text(
                            text = "移除 AndroidManifest.xml 中的 screenOrientation 锁定配置。",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF9E9E9E)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        DiffPreview(
                            diff = DiffResult(
                                before = """<activity
    android:name=".MainActivity"
    android:screenOrientation="portrait" />""",
                                after = """<activity
    android:name=".MainActivity" />""",
                                language = "xml",
                                description = ""
                            )
                        )
                    }
                    MStep.CODE_LOCK_REMOVAL -> {
                        Text(
                            text = "移除代码中 setRequestedOrientation() 调用。",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF9E9E9E)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        DiffPreview(
                            diff = DiffResult(
                                before = """// setRequestedOrientation removed
setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);""",
                                after = """// Removed for Android 17 compliance
// Let the system decide orientation
setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);""",
                                language = "kotlin",
                                description = ""
                            )
                        )
                    }
                    MStep.BIDIRECTIONAL_LAYOUT_VERIFY -> {
                        Text(
                            text = "验证双向布局支持：在 Compose 中使用 WindowSizeClass 适配不同方向。",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF9E9E9E)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        CodeBlock(
                            code = """
// Bidirectional layout with WindowSizeClass
@Composable
fun AdaptiveLayout() {
    val windowSizeClass = calculateWindowSizeClass(this)
    when (windowSizeClass.widthSizeClass) {
        WindowWidthSizeClass.Compact -> {
            // Portrait-optimized layout
            PortraitLayout()
        }
        WindowWidthSizeClass.Expanded -> {
            // Landscape-optimized layout
            LandscapeLayout()
        }
    }
}
                            """.trimIndent(),
                            language = "kotlin"
                        )
                    }
                    MStep.CONFIG_CHANGE_HANDLING -> {
                        Text(
                            text = "处理配置变更：在 Activity 中正确实现 onSaveInstanceState/onRestoreInstanceState。",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF9E9E9E)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        DiffPreview(
                            diff = DiffResult(
                                before = """// No state save
class EditorActivity : AppCompatActivity() {
    // Missing state save/restore
}""",
                                after = """// Proper state save on configuration change
class EditorActivity : AppCompatActivity() {
    private var editorContent: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        editorContent = savedInstanceState?.getString("content") ?: ""
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("content", editorContent)
    }
}""",
                                language = "kotlin",
                                description = ""
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Navigation buttons / 导航按钮
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    if (currentStep != MStep.entries.first()) {
                        OutlinedButton(
                            onClick = {
                                val prevStep = MStep.entries[currentStep.ordinal - 1]
                                onSetMigrationStep(prevStep)
                            }
                        ) {
                            Icon(Icons.Default.ArrowBack, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("上一步")
                        }
                    } else {
                        Spacer(modifier = Modifier.width(1.dp))
                    }
                    if (currentStep != MStep.entries.last()) {
                        Button(
                            onClick = {
                                val nextStep = MStep.entries[currentStep.ordinal + 1]
                                onSetMigrationStep(nextStep)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = AndroidBlue)
                        ) {
                            Text("下一步")
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(Icons.Default.ArrowForward, contentDescription = null)
                        }
                    }
                }
            }
        }
    }
}

/**
 * WindowSizeClassCIDetail — Window Size Class CI 检测详情
 */
@Composable
private fun WindowSizeClassCIDetail(
    selectedWsc: WindowSizeClassOption,
    onSetWindowSizeClass: (WindowSizeClassOption) -> Unit
) {
    Column {
        Card(
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "⚙️ Window Size Class CI 检测",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color(0xFFE0E0E0)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "GitHub Actions，自动化验证 App 在 Compact/Medium/Expanded 窗口下的布局正确性。",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF9E9E9E)
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Window size class selector / 窗口大小分类选择器
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    WindowSizeClassOption.entries.forEach { wsc ->
                        FilterChip(
                            selected = selectedWsc == wsc,
                            onClick = { onSetWindowSizeClass(wsc) },
                            label = { Text(wsc.displayName) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = AndroidBlue,
                                selectedLabelColor = Color.White
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // CI YAML preview / CI YAML 预览
        Card(
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "GitHub Actions YAML",
                    style = MaterialTheme.typography.titleSmall,
                    color = Color(0xFFE0E0E0)
                )
                Spacer(modifier = Modifier.height(12.dp))
                CodeBlock(
                    code = """
name: Window Size Class Test
on: [push, pull_request]
jobs:
  test-layout:
    strategy:
      matrix:
        window: [compact, medium, expanded]
    steps:
      - uses: actions/checkout@v4
      - name: Run layout tests
        run: |
          ./gradlew test
          # Verify layout under ${'$'}{{ matrix.window }}
                    """.trimIndent(),
                    language = "yaml"
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedButton(
                    onClick = { /* Copy YAML */ }
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("复制 YAML")
                }
            }
        }
    }
}

/**
 * LegacyMigrationPathDetail — Legacy App 增量迁移路径详情
 */
@Composable
private fun LegacyMigrationPathDetail() {
    Card(
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "📊 Legacy App 增量迁移路径",
                style = MaterialTheme.typography.titleMedium,
                color = Color(0xFFE0E0E0)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "大型 App 分 3 阶段适配：①快速止血检测→②核心流程迁移→③全面适配，含风险评估矩阵。",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF9E9E9E)
            )
            Spacer(modifier = Modifier.height(16.dp))

            // 3 phases / 3 个阶段
            listOf(
                Triple("Phase 1", "快速止血检测", "识别所有方向锁定点，评估风险优先级"),
                Triple("Phase 2", "核心流程迁移", "迁移核心 Activity，支持双向布局"),
                Triple("Phase 3", "全面适配", "全面重构，WindowSizeClass 适配")
            ).forEach { (phase, title, desc) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(AndroidBlue, RoundedCornerShape(16.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = phase.last().toString(),
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleSmall,
                            color = Color(0xFFE0E0E0)
                        )
                        Text(
                            text = desc,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF9E9E9E)
                        )
                    }
                }
            }
        }
    }
}

/**
 * AccessibilityOrientationDetail — 双向布局无障碍检测详情
 */
@Composable
private fun AccessibilityOrientationDetail() {
    Card(
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "♿ 双向布局无障碍检测",
                style = MaterialTheme.typography.titleMedium,
                color = Color(0xFFE0E0E0)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "验证双向布局下的 TalkBack/大字体的正确行为。",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF9E9E9E)
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Checklist / 检查清单
            listOf(
                Pair("TalkBack 导航顺序", "横竖屏切换后焦点顺序正确"),
                Pair("内容描述", "所有图像和图标有正确的 contentDescription"),
                Pair("大字体适配", "字体放大 200% 后布局不溢出"),
                Pair("触控目标", "所有可点击元素 ≥ 48dp × 48dp")
            ).forEach { (item, requirement) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.CheckBoxOutlineBlank,
                        contentDescription = null,
                        tint = Color(0xFF9E9E9E),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = item,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFFE0E0E0)
                        )
                        Text(
                            text = requirement,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF9E9E9E)
                        )
                    }
                }
            }
        }
    }
}

/**
 * ConfigChangeStateSaverDetail — 配置变更状态保存模板详情
 */
@Composable
private fun ConfigChangeStateSaverDetail() {
    Card(
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "💾 配置变更状态保存模板",
                style = MaterialTheme.typography.titleMedium,
                color = Color(0xFFE0E0E0)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "检测 onSaveInstanceState 使用情况，提供状态保存代码模板。",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF9E9E9E)
            )
            Spacer(modifier = Modifier.height(16.dp))

            CodeBlock(
                code = """
// State save template for configuration change
class MyActivity : AppCompatActivity() {
    private var uiState: UiState? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Restore state
        uiState = savedInstanceState?.getParcelable("ui_state")
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        // Save critical UI state
        outState.putParcelable("ui_state", uiState)
    }
}
                """.trimIndent(),
                language = "kotlin"
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(onClick = { }) {
                Icon(Icons.Default.ContentCopy, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("复制代码")
            }
        }
    }
}

// ================================================================
// Reusable Components — 可复用组件
// ================================================================

/**
 * DiffPreview — 代码 Diff 预览组件（左右对照）
 * Shows before/after code side by side.
 *
 * @param diff The diff result to display
 */
@Composable
private fun DiffPreview(diff: DiffResult) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Before / 修复前
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "修复前",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFFF85149)
                )
                Spacer(modifier = Modifier.height(4.dp))
                CodeBlock(
                    code = diff.before,
                    language = diff.language,
                    highlightRemoved = true
                )
            }
            // After / 修复后
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "修复后",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF3FB950)
                )
                Spacer(modifier = Modifier.height(4.dp))
                CodeBlock(
                    code = diff.after,
                    language = diff.language,
                    highlightAdded = true
                )
            }
        }
    }
}

/**
 * CodeBlock — 代码展示块
 * Displays code with syntax highlighting simulation.
 *
 * @param code Code string to display
 * @param language Programming language
 * @param highlightAdded Whether to highlight added lines (green bg)
 * @param highlightRemoved Whether to highlight removed lines (red bg)
 */
@Composable
private fun CodeBlock(
    code: String,
    language: String,
    highlightAdded: Boolean = false,
    highlightRemoved: Boolean = false
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CodeBlockBg),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Language badge / 语言标签
            Text(
                text = language.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFF9E9E9E)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = code,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp
                ),
                color = Color(0xFFE0E0E0),
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = when {
                            highlightAdded -> CodeLineAdded.copy(alpha = 0.2f)
                            highlightRemoved -> CodeLineRemoved.copy(alpha = 0.2f)
                            else -> Color.Transparent
                        },
                        shape = RoundedCornerShape(4.dp)
                    )
                    .padding(4.dp)
            )
        }
    }
}
