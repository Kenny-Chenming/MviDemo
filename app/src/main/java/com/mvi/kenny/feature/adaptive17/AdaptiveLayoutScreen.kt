package com.mvi.kenny.feature.adaptive17

/**
 * =============================================================
 * AdaptiveLayoutScreen.kt — Android 17 大屏自适应布局检测主界面
 * AdaptiveLayoutScreen.kt — Android 17 Adaptive Layout Detection Main UI
 * =============================================================
 *
 * 功能说明：
 * - 扫描 Android 17 大屏自适应布局违规项（screenOrientation / resizeableActivity / maxAspectRatio 等）
 * - 可视化展示违规详情、严重程度、修复建议
 * - 支持修复向导（Fix Wizard）和模板生成器
 *
 * 功能概览：
 * - Dashboard: 项目概览卡片、快速扫描入口、最近扫描历史
 * - Scan Results: 违规列表（按类型分组，按严重程度排序）
 * - Violation Card: 违规详情卡片（含文件路径、行号、代码片段、修复建议）
 * - Template Generator: 摄像头预览 / 视频播放 / 表单输入 / 通用响应式模板生成
 *
 * 设计规范：Material Design 3 / 深色主题 / Developer-first
 *
 * @see AdaptiveScanState 扫描状态定义
 * @see AdaptiveScanViewModel 扫描 ViewModel
 * @see TemplateGeneratorViewModel 模板生成器 ViewModel
 */

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// =============================================================
// Theme Colors — 主题颜色定义
// =============================================================

private object AdaptiveColors {
    // 背景色
    val Background = Color(0xFF0F1117)          // 深空黑
    val CardBackground = Color(0xFF1A1D27)     // 深蓝灰卡片
    val SurfaceVariant = Color(0xFF252A37)     // 表面变体

    // 严重程度颜色
    val Critical = Color(0xFFD93025)           // 严重（崩溃级）
    val Warning = Color(0xFFF9AB00)            // 中等（功能异常）
    val Low = Color(0xFF1E8E3E)                // 低级（提示）
    val Success = Color(0xFF27AE60)            // 已修复

    // 文字颜色
    val TextPrimary = Color(0xFFFFFFFF)         // 主文字
    val TextSecondary = Color(0xFFA0A4B0)      // 次文字
    val TextTertiary = Color(0xFF6B7080)       // 三级文字

    // 违规类型颜色
    val ScreenOrientation = Color(0xFF4285F4)   // 蓝色
    val ResizeableActivity = Color(0xFFEA4335) // 红色
    val MaxAspectRatio = Color(0xFFFBBC04)     // 黄色
    val CameraAspect = Color(0xFF34A853)        // 绿色
    val ConfigChanges = Color(0xFF9C27B0)      // 紫色

    // 扫描阶段颜色
    val PhaseActive = Color(0xFF4285F4)         // 当前阶段
    val PhasePending = Color(0xFF3C4556)        // 待执行
    val PhaseDone = Color(0xFF27AE60)           // 已完成
}

// =============================================================
// Scan Phase — 扫描阶段定义
// =============================================================

private enum class ScanPhase(
    val label: String,
    val labelZh: String,
    val description: String
) {
    MANIFEST("Manifest Scan", "Manifest 扫描", "解析 AndroidManifest.xml 属性"),
    CAMERA("CameraX Analysis", "CameraX 分析", "静态分析 CameraX aspect ratio"),
    WINDOW_SIZE("WindowSizeClass", "窗口尺寸分析", "检测 WindowSizeClass 覆盖度"),
    CONFIG_CHANGE("Config Changes", "配置变更分析", "分析 configuration change 处理"),
    MULTI_WINDOW("Multi-Window", "多窗口分析", "验证 freeform / 分屏兼容性")
}

// =============================================================
// AdaptiveLayoutScreen — 主界面
// AdaptiveLayoutScreen — Main Screen
// =============================================================

/**
 * Android 17 大屏自适应布局检测主界面
 * Android 17 Adaptive Layout Detection Main Screen
 *
 * @param scanViewModel 扫描 ViewModel
 * @param templateViewModel 模板生成器 ViewModel
 */
@Composable
fun AdaptiveLayoutScreen(
    scanViewModel: AdaptiveScanViewModel,
    templateViewModel: TemplateGeneratorViewModel,
    modifier: Modifier = Modifier
) {
    val scanState by scanViewModel.state.collectAsStateWithLifecycle()
    val templateState by templateViewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // 当前 Tab 页
    var currentTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("扫描", "模板生成", "设置")

    // 选中的违规详情 Sheet
    var selectedViolation by remember { mutableStateOf<Violation?>(null) }
    var showViolationSheet by remember { mutableStateOf(false) }

    // Fix Wizard 状态
    var showFixWizard by remember { mutableStateOf(false) }
    var fixWizardStep by remember { mutableIntStateOf(0) }
    var selectedFixIds by remember { mutableStateOf<Set<String>>(emptySet()) }

    // 监听 Effects
    LaunchedEffect(Unit) {
        scanViewModel.effect.collect { effect: ScanEffect ->
            when (effect) {
                is ScanEffect.ShowToast -> {
                    Toast.makeText(context, effect.message, Toast.LENGTH_SHORT).show()
                }
                is ScanEffect.ShowError -> {
                    Toast.makeText(context, effect.message, Toast.LENGTH_LONG).show()
                }
                is ScanEffect.ShowViolationDetail -> {
                    selectedViolation = effect.violation
                    showViolationSheet = true
                }
                is ScanEffect.CopyFixCode -> {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = ClipData.newPlainText("Fix Code", effect.code)
                    clipboard.setPrimaryClip(clip)
                    Toast.makeText(context, "修复代码已复制", Toast.LENGTH_SHORT).show()
                }
                is ScanEffect.NavigateToTemplateGenerator -> {
                    currentTab = 1 // 切换到模板生成 Tab
                    templateViewModel.handleIntent(TemplateIntent.UpdateActivityName(effect.activityName))
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        templateViewModel.effect.collect { effect: TemplateEffect ->
            when (effect) {
                is TemplateEffect.ShowToast -> {
                    Toast.makeText(context, effect.message, Toast.LENGTH_SHORT).show()
                }
                is TemplateEffect.CopySuccess -> {
                    Toast.makeText(context, effect.message, Toast.LENGTH_SHORT).show()
                }
                is TemplateEffect.ShowError -> {
                    Toast.makeText(context, effect.message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    // 背景
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(AdaptiveColors.Background)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // ============================================================
            // Tab Row — Tab 切换
            // ============================================================
            ScrollableTabRow(
                selectedTabIndex = currentTab,
                containerColor = AdaptiveColors.CardBackground,
                contentColor = AdaptiveColors.TextPrimary,
                edgePadding = 16.dp
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = currentTab == index,
                        onClick = { currentTab = index },
                        text = {
                            Text(
                                text = title,
                                color = if (currentTab == index) AdaptiveColors.TextPrimary
                                        else AdaptiveColors.TextSecondary
                            )
                        }
                    )
                }
            }

            // ============================================================
            // Tab Content — 各 Tab 内容
            // ============================================================
            when (currentTab) {
                0 -> ScanTabContent(
                    scanState = scanState,
                    onStartScan = { scanViewModel.handleIntent(ScanIntent.StartScan) },
                    onViolationClick = { violation ->
                        scanViewModel.handleIntent(ScanIntent.SelectViolation(violation))
                    },
                    onApplyFix = { violationId ->
                        scanViewModel.handleIntent(ScanIntent.ApplyFix(violationId))
                    },
                    onExportHtml = { scanViewModel.handleIntent(ScanIntent.ExportHtml) },
                    onStartFixWizard = { showFixWizard = true },
                    selectedFixIds = selectedFixIds,
                    onToggleFixSelection = { id ->
                        selectedFixIds = if (id in selectedFixIds) selectedFixIds - id
                                        else selectedFixIds + id
                    }
                )
                1 -> TemplateGeneratorTabContent(
                    templateState = templateState,
                    onSelectScenario = { scenario ->
                        templateViewModel.handleIntent(TemplateIntent.SelectScenario(scenario))
                    },
                    onUpdateActivityName = { name ->
                        templateViewModel.handleIntent(TemplateIntent.UpdateActivityName(name))
                    },
                    onGenerate = { templateViewModel.handleIntent(TemplateIntent.GenerateTemplate) },
                    onCopy = { templateViewModel.handleIntent(TemplateIntent.CopyToClipboard) }
                )
                2 -> SettingsTabContent(
                    scanState = scanState,
                    onStartScan = { scanViewModel.handleIntent(ScanIntent.StartScan) }
                )
            }
        }

        // ============================================================
        // Violation Detail Bottom Sheet — 违规详情抽屉
        // ============================================================
        if (showViolationSheet && selectedViolation != null) {
            ViolationDetailSheet(
                violation = selectedViolation!!,
                onDismiss = {
                    showViolationSheet = false
                    selectedViolation = null
                },
                onApplyFix = { id ->
                    scanViewModel.handleIntent(ScanIntent.ApplyFix(id))
                    showViolationSheet = false
                }
            )
        }

        // ============================================================
        // Fix Wizard Dialog — 修复向导弹窗
        // ============================================================
        if (showFixWizard) {
            FixWizardDialog(
                violations = scanState.violations.filter { it.id in selectedFixIds },
                step = fixWizardStep,
                onStepChange = { fixWizardStep = it },
                onConfirm = {
                    // 执行修复（此处为模拟）
                    showFixWizard = false
                    fixWizardStep = 0
                    selectedFixIds = emptySet()
                    Toast.makeText(context, "修复向导已启动", Toast.LENGTH_SHORT).show()
                },
                onDismiss = {
                    showFixWizard = false
                    fixWizardStep = 0
                }
            )
        }
    }
}

// =============================================================
// Scan Tab Content — 扫描 Tab 内容
// =============================================================

@Composable
private fun ScanTabContent(
    scanState: AdaptiveScanState,
    onStartScan: () -> Unit,
    onViolationClick: (Violation) -> Unit,
    onApplyFix: (String) -> Unit,
    onExportHtml: () -> Unit,
    onStartFixWizard: () -> Unit,
    selectedFixIds: Set<String>,
    onToggleFixSelection: (String) -> Unit
) {
    val scope = rememberCoroutineScope()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        // ============================================================
        // Dashboard Card — 项目概览卡片
        // ============================================================
        item {
            DashboardCard(
                scanState = scanState,
                onStartScan = onStartScan,
                onExportHtml = onExportHtml
            )
        }

        // ============================================================
        // Scan Progress — 扫描进度
        // ============================================================
        if (scanState.isScanning) {
            item {
                ScanProgressCard(progress = scanState.progress)
            }
        }

        // ============================================================
        // Violations Summary — 违规汇总（扫描完成后显示）
        // ============================================================
        if (scanState.scanReport != null && !scanState.isScanning) {
            item {
                ViolationsSummaryCard(
                    scanReport = scanState.scanReport!!,
                    selectedFixIds = selectedFixIds,
                    onToggleFixSelection = onToggleFixSelection
                )
            }
        }

        // ============================================================
        // Violation List — 违规列表
        // ============================================================
        if (scanState.violations.isNotEmpty() && !scanState.isScanning) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "违规详情",
                        style = MaterialTheme.typography.titleMedium,
                        color = AdaptiveColors.TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    if (selectedFixIds.isNotEmpty()) {
                        FilledTonalButton(
                            onClick = onStartFixWizard,
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = AdaptiveColors.PhaseActive.copy(alpha = 0.2f)
                            )
                        ) {
                            Icon(
                                Icons.Default.Build,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text("修复 ${selectedFixIds.size} 项")
                        }
                    }
                }
            }

            // 按违规类型分组展示
            val groupedViolations = scanState.violations.groupBy { it.violationType }
            groupedViolations.forEach { (type, violations) ->
                item {
                    ViolationTypeHeader(type = type, count = violations.size)
                }
                items(violations, key = { it.id }) { violation ->
                    ViolationCard(
                        violation = violation,
                        isSelected = violation.id in selectedFixIds,
                        onSelect = { onToggleFixSelection(violation.id) },
                        onClick = { onViolationClick(violation) },
                        onApplyFix = { onApplyFix(violation.id) }
                    )
                }
            }
        }

        // ============================================================
        // Empty State — 空状态
        // ============================================================
        if (scanState.violations.isEmpty() && !scanState.isScanning && scanState.scanReport == null) {
            item {
                EmptyScanState(onStartScan = onStartScan)
            }
        }

        // ============================================================
        // Error State — 错误状态
        // ============================================================
        if (scanState.error != null) {
            item {
                ErrorCard(
                    message = scanState.error!!,
                    onDismiss = { /* handle via intent */ }
                )
            }
        }
    }
}

// =============================================================
// Dashboard Card — 项目概览卡片
// =============================================================

@Composable
private fun DashboardCard(
    scanState: AdaptiveScanState,
    onStartScan: () -> Unit,
    onExportHtml: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = AdaptiveColors.CardBackground),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // 标题行
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Android 17 大屏合规检测",
                        style = MaterialTheme.typography.titleLarge,
                        color = AdaptiveColors.TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Android 17 Adaptive Layout Compliance",
                        style = MaterialTheme.typography.bodySmall,
                        color = AdaptiveColors.TextTertiary
                    )
                }
                Icon(
                    Icons.Default.ViewModule,
                    contentDescription = null,
                    tint = AdaptiveColors.PhaseActive,
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 扫描状态
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatusChip(
                    label = "Target SDK 37",
                    icon = Icons.Default.SystemUpdate,
                    color = AdaptiveColors.PhaseActive
                )
                StatusChip(
                    label = "sw ≥ 600dp",
                    icon = Icons.Default.TableChart,
                    color = AdaptiveColors.Warning
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 扫描按钮
            Button(
                onClick = onStartScan,
                enabled = !scanState.isScanning,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AdaptiveColors.PhaseActive,
                    disabledContainerColor = AdaptiveColors.PhasePending
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (scanState.isScanning) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("扫描中...")
                } else {
                    Icon(Icons.Default.Search, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("开始扫描 / Start Scan")
                }
            }

            // 报告导出
            if (scanState.reportUrl != null) {
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(
                    onClick = onExportHtml,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Download, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("导出 HTML 报告")
                }
            }
        }
    }
}

// =============================================================
// Status Chip — 状态标签
// =============================================================

@Composable
private fun StatusChip(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = color.copy(alpha = 0.15f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = color
            )
        }
    }
}

// =============================================================
// Scan Progress Card — 扫描进度卡片
// =============================================================

@Composable
private fun ScanProgressCard(progress: Float) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = AdaptiveColors.CardBackground),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "正在扫描",
                    style = MaterialTheme.typography.titleMedium,
                    color = AdaptiveColors.TextPrimary,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "${(progress * 100).toInt()}%",
                    style = MaterialTheme.typography.titleMedium,
                    color = AdaptiveColors.PhaseActive,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 进度条
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = AdaptiveColors.PhaseActive,
                trackColor = AdaptiveColors.PhasePending,
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 扫描阶段指示器
            val phases = ScanPhase.entries
            val currentPhaseIndex = (progress * phases.size).toInt().coerceIn(0, phases.size - 1)

            phases.forEachIndexed { index, phase ->
                val phaseProgress = when {
                    index < currentPhaseIndex -> 1f
                    index == currentPhaseIndex -> (progress * phases.size) - index
                    else -> 0f
                }
                PhaseIndicator(
                    phase = phase,
                    progress = phaseProgress,
                    isActive = index == currentPhaseIndex,
                    isDone = index < currentPhaseIndex
                )
                if (index < phases.lastIndex) {
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

// =============================================================
// Phase Indicator — 扫描阶段指示器
// =============================================================

@Composable
private fun PhaseIndicator(
    phase: ScanPhase,
    progress: Float,
    isActive: Boolean,
    isDone: Boolean
) {
    val color = when {
        isDone -> AdaptiveColors.PhaseDone
        isActive -> AdaptiveColors.PhaseActive
        else -> AdaptiveColors.PhasePending
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 阶段状态图标
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            if (isDone) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(16.dp)
                )
            } else if (isActive) {
                CircularProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.size(24.dp),
                    color = color,
                    strokeWidth = 2.dp
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(color)
                )
            }
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = phase.labelZh,
                style = MaterialTheme.typography.bodyMedium,
                color = if (isActive || isDone) AdaptiveColors.TextPrimary else AdaptiveColors.TextTertiary,
                fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Normal
            )
            Text(
                text = phase.description,
                style = MaterialTheme.typography.bodySmall,
                color = AdaptiveColors.TextTertiary
            )
        }
    }
}

// =============================================================
// Violations Summary Card — 违规汇总卡片
// =============================================================

@Composable
private fun ViolationsSummaryCard(
    scanReport: ScanReport,
    selectedFixIds: Set<String>,
    onToggleFixSelection: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = AdaptiveColors.CardBackground),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "扫描报告",
                style = MaterialTheme.typography.titleMedium,
                color = AdaptiveColors.TextPrimary,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 统计数字
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(
                    value = scanReport.totalViolations.toString(),
                    label = "总计",
                    color = AdaptiveColors.TextPrimary
                )
                StatItem(
                    value = scanReport.criticalCount.toString(),
                    label = "严重",
                    color = AdaptiveColors.Critical
                )
                StatItem(
                    value = scanReport.warningCount.toString(),
                    label = "中等",
                    color = AdaptiveColors.Warning
                )
                StatItem(
                    value = scanReport.lowCount.toString(),
                    label = "低级",
                    color = AdaptiveColors.Low
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 扫描时间
            Text(
                text = "扫描时间: ${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date(scanReport.scanTimestamp))}",
                style = MaterialTheme.typography.bodySmall,
                color = AdaptiveColors.TextTertiary
            )

            Text(
                text = "扫描 Activity: ${scanReport.scannedActivities.size} 个",
                style = MaterialTheme.typography.bodySmall,
                color = AdaptiveColors.TextTertiary
            )
        }
    }
}

// =============================================================
// Stat Item — 统计数据项
// =============================================================

@Composable
private fun StatItem(
    value: String,
    label: String,
    color: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineMedium,
            color = color,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = AdaptiveColors.TextSecondary
        )
    }
}

// =============================================================
// Violation Type Header — 违规类型分组标题
// =============================================================

@Composable
private fun ViolationTypeHeader(
    type: ViolationType,
    count: Int
) {
    val color = when (type) {
        ViolationType.SCREEN_ORIENTATION -> AdaptiveColors.ScreenOrientation
        ViolationType.RESIZEABLE_ACTIVITY -> AdaptiveColors.ResizeableActivity
        ViolationType.MAX_ASPECT_RATIO -> AdaptiveColors.MaxAspectRatio
        ViolationType.MIN_ASPECT_RATIO -> AdaptiveColors.MaxAspectRatio
        ViolationType.CAMERAX_ASPECT_RATIO -> AdaptiveColors.CameraAspect
        ViolationType.CONFIG_CHANGES -> AdaptiveColors.ConfigChanges
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .width(4.dp)
                .height(20.dp)
                .background(color, RoundedCornerShape(2.dp))
        )
        Text(
            text = type.labelZh,
            style = MaterialTheme.typography.titleSmall,
            color = AdaptiveColors.TextPrimary,
            fontWeight = FontWeight.SemiBold
        )
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = color.copy(alpha = 0.2f)
        ) {
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.labelSmall,
                color = color,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
            )
        }
    }
}

// =============================================================
// Violation Card — 违规信息卡片
// =============================================================

@Composable
private fun ViolationCard(
    violation: Violation,
    isSelected: Boolean,
    onSelect: () -> Unit,
    onClick: () -> Unit,
    onApplyFix: () -> Unit
) {
    val severityColor = when (violation.severity) {
        ViolationSeverity.CRITICAL -> AdaptiveColors.Critical
        ViolationSeverity.WARNING -> AdaptiveColors.Warning
        ViolationSeverity.LOW -> AdaptiveColors.Low
    }

    val typeColor = when (violation.violationType) {
        ViolationType.SCREEN_ORIENTATION -> AdaptiveColors.ScreenOrientation
        ViolationType.RESIZEABLE_ACTIVITY -> AdaptiveColors.ResizeableActivity
        ViolationType.MAX_ASPECT_RATIO, ViolationType.MIN_ASPECT_RATIO -> AdaptiveColors.MaxAspectRatio
        ViolationType.CAMERAX_ASPECT_RATIO -> AdaptiveColors.CameraAspect
        ViolationType.CONFIG_CHANGES -> AdaptiveColors.ConfigChanges
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (isSelected) Modifier.border(
                    2.dp,
                    AdaptiveColors.PhaseActive,
                    RoundedCornerShape(12.dp)
                )
                else Modifier
            ),
        colors = CardDefaults.cardColors(containerColor = AdaptiveColors.SurfaceVariant),
        shape = RoundedCornerShape(12.dp),
        onClick = onClick
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // 顶部行：勾选框 + 严重程度 + 类型
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = isSelected,
                    onCheckedChange = { onSelect() },
                    colors = CheckboxDefaults.colors(
                        checkedColor = AdaptiveColors.PhaseActive,
                        uncheckedColor = AdaptiveColors.TextTertiary
                    )
                )

                SeverityBadge(
                    severity = violation.severity,
                    color = severityColor
                )

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = typeColor.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = violation.violationType.labelZh,
                        style = MaterialTheme.typography.labelSmall,
                        color = typeColor,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                Spacer(Modifier.weight(1f))

                if (violation.isAutoFixable) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = AdaptiveColors.Success.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = "可自动修复",
                            style = MaterialTheme.typography.labelSmall,
                            color = AdaptiveColors.Success,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Activity 名称
            Text(
                text = violation.activityName,
                style = MaterialTheme.typography.titleSmall,
                color = AdaptiveColors.TextPrimary,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(4.dp))

            // 文件路径
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Description,
                    contentDescription = null,
                    tint = AdaptiveColors.TextTertiary,
                    modifier = Modifier.size(12.dp)
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    text = "${violation.filePath}:${violation.lineNumber}",
                    style = MaterialTheme.typography.bodySmall,
                    color = AdaptiveColors.TextTertiary
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 描述
            Text(
                text = violation.description,
                style = MaterialTheme.typography.bodySmall,
                color = AdaptiveColors.TextSecondary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            // 修复建议预览
            if (violation.suggestedFix.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = AdaptiveColors.CardBackground
                ) {
                    Text(
                        text = violation.suggestedFix.take(100) + if (violation.suggestedFix.length > 100) "..." else "",
                        style = MaterialTheme.typography.bodySmall,
                        color = AdaptiveColors.TextTertiary,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }

            // 操作按钮
            if (violation.isAutoFixable) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    FilledTonalButton(
                        onClick = onApplyFix,
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = AdaptiveColors.PhaseActive.copy(alpha = 0.2f)
                        ),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Icon(
                            Icons.Default.Build,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text("应用修复", style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        }
    }
}

// =============================================================
// Severity Badge — 严重程度徽章
// =============================================================

@Composable
private fun SeverityBadge(
    severity: ViolationSeverity,
    color: Color
) {
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = color.copy(alpha = 0.15f)
    ) {
        Text(
            text = severity.labelZh,
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}

// =============================================================
// Empty Scan State — 空扫描状态
// =============================================================

@Composable
private fun EmptyScanState(onStartScan: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            Icons.Default.ViewModule,
            contentDescription = null,
            tint = AdaptiveColors.TextTertiary,
            modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "暂无扫描数据",
            style = MaterialTheme.typography.titleMedium,
            color = AdaptiveColors.TextSecondary
        )
        Text(
            text = "点击上方按钮开始扫描 Android 17 大屏合规问题",
            style = MaterialTheme.typography.bodySmall,
            color = AdaptiveColors.TextTertiary,
            textAlign = TextAlign.Center
        )
    }
}

// =============================================================
// Error Card — 错误卡片
// =============================================================

@Composable
private fun ErrorCard(
    message: String,
    onDismiss: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = AdaptiveColors.Critical.copy(alpha = 0.15f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Error,
                contentDescription = null,
                tint = AdaptiveColors.Critical
            )
            Spacer(Modifier.width(12.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = AdaptiveColors.Critical,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = onDismiss) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "关闭",
                    tint = AdaptiveColors.TextSecondary
                )
            }
        }
    }
}

// =============================================================
// Violation Detail Sheet — 违规详情抽屉
// =============================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ViolationDetailSheet(
    violation: Violation,
    onDismiss: () -> Unit,
    onApplyFix: (String) -> Unit
) {
    val severityColor = when (violation.severity) {
        ViolationSeverity.CRITICAL -> AdaptiveColors.Critical
        ViolationSeverity.WARNING -> AdaptiveColors.Warning
        ViolationSeverity.LOW -> AdaptiveColors.Low
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = AdaptiveColors.CardBackground,
        contentColor = AdaptiveColors.TextPrimary
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .padding(bottom = 32.dp)
        ) {
            // 标题
            Text(
                text = "违规详情",
                style = MaterialTheme.typography.headlineSmall,
                color = AdaptiveColors.TextPrimary,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 严重程度
            Row(verticalAlignment = Alignment.CenterVertically) {
                SeverityBadge(severity = violation.severity, color = severityColor)
                Spacer(Modifier.width(8.dp))
                Text(
                    text = violation.severity.labelZh,
                    style = MaterialTheme.typography.bodyMedium,
                    color = severityColor
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 违规类型
            DetailRow(label = "违规类型", value = violation.violationType.labelZh)
            DetailRow(label = "Activity", value = violation.activityName)
            DetailRow(label = "文件", value = "${violation.filePath}:${violation.lineNumber}")
            DetailRow(label = "描述", value = violation.description)

            Spacer(modifier = Modifier.height(16.dp))

            // 修复建议
            Text(
                text = "修复建议",
                style = MaterialTheme.typography.titleSmall,
                color = AdaptiveColors.TextPrimary,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = AdaptiveColors.SurfaceVariant
            ) {
                Text(
                    text = violation.suggestedFix,
                    style = MaterialTheme.typography.bodySmall,
                    color = AdaptiveColors.TextSecondary,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.padding(12.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 操作按钮
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("关闭")
                }
                if (violation.isAutoFixable) {
                    Button(
                        onClick = { onApplyFix(violation.id) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AdaptiveColors.PhaseActive
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Build, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("应用修复")
                    }
                }
            }
        }
    }
}

// =============================================================
// Detail Row — 详情行
// =============================================================

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Text(
            text = "$label:",
            style = MaterialTheme.typography.bodyMedium,
            color = AdaptiveColors.TextTertiary,
            modifier = Modifier.width(80.dp)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = AdaptiveColors.TextPrimary,
            modifier = Modifier.weight(1f)
        )
    }
}

// =============================================================
// Fix Wizard Dialog — 修复向导弹窗
// Fix Wizard Dialog — Multi-step Fix Wizard
// =============================================================

/**
 * 修复向导弹窗
 * Multi-step wizard for applying fixes
 *
 * Step 1: 选择修复范围
 * Step 2: 确认修复预览 (diff)
 * Step 3: 执行修复 & 验证
 */
@Composable
private fun FixWizardDialog(
    violations: List<Violation>,
    step: Int,
    onStepChange: (Int) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = AdaptiveColors.CardBackground,
        title = {
            Text(
                text = "修复向导",
                color = AdaptiveColors.TextPrimary
            )
        },
        text = {
            Column {
                // 步骤指示器
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("范围", "预览", "执行").forEachIndexed { index, label ->
                        val isActive = step == index
                        val isDone = step > index
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(
                                    when {
                                        isDone -> AdaptiveColors.Success
                                        isActive -> AdaptiveColors.PhaseActive
                                        else -> AdaptiveColors.PhasePending
                                    }
                                )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                when (step) {
                    0 -> {
                        // Step 1: 选择修复范围
                        Text(
                            text = "选择要修复的违规项",
                            style = MaterialTheme.typography.bodyMedium,
                            color = AdaptiveColors.TextPrimary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        violations.forEach { violation ->
                            Row(
                                modifier = Modifier.padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = AdaptiveColors.Warning,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = violation.activityName,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = AdaptiveColors.TextSecondary
                                )
                            }
                        }
                    }
                    1 -> {
                        // Step 2: 修复预览
                        Text(
                            text = "修复预览 (Diff)",
                            style = MaterialTheme.typography.bodyMedium,
                            color = AdaptiveColors.TextPrimary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        violations.take(3).forEach { violation ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = AdaptiveColors.SurfaceVariant,
                                modifier = Modifier.padding(vertical = 4.dp)
                            ) {
                                Column(modifier = Modifier.padding(8.dp)) {
                                    Text(
                                        text = "- ${violation.filePath}:${violation.lineNumber}",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontFamily = FontFamily.Monospace,
                                        color = AdaptiveColors.Critical
                                    )
                                    Text(
                                        text = "+ [AUTO-FIXED] ${violation.suggestedFix.take(50)}...",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontFamily = FontFamily.Monospace,
                                        color = AdaptiveColors.Success
                                    )
                                }
                            }
                        }
                    }
                    2 -> {
                        // Step 3: 执行
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(32.dp),
                                color = AdaptiveColors.PhaseActive,
                                strokeWidth = 3.dp
                            )
                            Spacer(Modifier.width(16.dp))
                            Text(
                                text = "正在执行修复...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = AdaptiveColors.TextPrimary
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            when (step) {
                0 -> {
                    Button(
                        onClick = { onStepChange(1) },
                        enabled = violations.isNotEmpty(),
                        colors = ButtonDefaults.buttonColors(containerColor = AdaptiveColors.PhaseActive)
                    ) {
                        Text("下一步")
                    }
                }
                1 -> {
                    Button(
                        onClick = {
                            onStepChange(2)
                            // 模拟执行
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AdaptiveColors.Success)
                    ) {
                        Text("确认修复")
                    }
                }
                2 -> {
                    Button(
                        onClick = onConfirm,
                        colors = ButtonDefaults.buttonColors(containerColor = AdaptiveColors.Success)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null)
                        Spacer(Modifier.width(4.dp))
                        Text("完成")
                    }
                }
            }
        },
        dismissButton = {
            if (step < 2) {
                TextButton(onClick = onDismiss) {
                    Text("取消", color = AdaptiveColors.TextSecondary)
                }
            }
        }
    )
}

// =============================================================
// Template Generator Tab Content — 模板生成 Tab 内容
// Template Generator Tab Content — Template Generator Tab
// =============================================================

@Composable
private fun TemplateGeneratorTabContent(
    templateState: TemplateState,
    onSelectScenario: (LayoutScenario) -> Unit,
    onUpdateActivityName: (String) -> Unit,
    onGenerate: () -> Unit,
    onCopy: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        // 标题
        item {
            Text(
                text = "模板生成器",
                style = MaterialTheme.typography.headlineSmall,
                color = AdaptiveColors.TextPrimary,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Layout Template Generator — Android 17 Adaptive Layout Templates",
                style = MaterialTheme.typography.bodySmall,
                color = AdaptiveColors.TextTertiary
            )
        }

        // Activity 名称输入
        item {
            OutlinedTextField(
                value = templateState.selectedActivity,
                onValueChange = onUpdateActivityName,
                label = { Text("Activity 名称") },
                placeholder = { Text("MainActivity") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AdaptiveColors.PhaseActive,
                    unfocusedBorderColor = AdaptiveColors.TextTertiary,
                    focusedLabelColor = AdaptiveColors.PhaseActive,
                    unfocusedLabelColor = AdaptiveColors.TextSecondary,
                    focusedTextColor = AdaptiveColors.TextPrimary,
                    unfocusedTextColor = AdaptiveColors.TextPrimary
                )
            )
        }

        // 场景选择
        item {
            Text(
                text = "选择布局场景",
                style = MaterialTheme.typography.titleSmall,
                color = AdaptiveColors.TextPrimary,
                fontWeight = FontWeight.SemiBold
            )
        }

        items(LayoutScenario.entries) { scenario ->
            ScenarioCard(
                scenario = scenario,
                isSelected = templateState.selectedScenario == scenario,
                onClick = { onSelectScenario(scenario) }
            )
        }

        // 生成按钮
        item {
            Button(
                onClick = onGenerate,
                enabled = !templateState.isGenerating,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AdaptiveColors.PhaseActive
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (templateState.isGenerating) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("生成中...")
                } else {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("生成模板代码")
                }
            }
        }

        // 生成结果
        if (templateState.generatedCode != null) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = AdaptiveColors.SurfaceVariant),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "生成结果",
                                style = MaterialTheme.typography.titleSmall,
                                color = AdaptiveColors.TextPrimary,
                                fontWeight = FontWeight.SemiBold
                            )
                            FilledTonalButton(
                                onClick = onCopy,
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Icon(
                                    Icons.Default.ContentCopy,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(Modifier.width(4.dp))
                                Text("复制")
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = AdaptiveColors.CardBackground
                        ) {
                            Text(
                                text = templateState.generatedCode!!,
                                style = MaterialTheme.typography.bodySmall,
                                color = AdaptiveColors.TextSecondary,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier
                                    .padding(12.dp)
                                    .heightIn(min = 200.dp, max = 400.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

// =============================================================
// Scenario Card — 场景选择卡片
// =============================================================

@Composable
private fun ScenarioCard(
    scenario: LayoutScenario,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val icon = when (scenario) {
        LayoutScenario.CAMERA -> Icons.Default.CameraAlt
        LayoutScenario.VIDEO -> Icons.Default.PlayCircle
        LayoutScenario.FORM -> Icons.Default.Edit
        LayoutScenario.GENERIC -> Icons.Default.Dashboard
    }

    val color = when (scenario) {
        LayoutScenario.CAMERA -> AdaptiveColors.CameraAspect
        LayoutScenario.VIDEO -> AdaptiveColors.Critical
        LayoutScenario.FORM -> AdaptiveColors.PhaseActive
        LayoutScenario.GENERIC -> AdaptiveColors.Warning
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (isSelected) Modifier.border(
                    2.dp,
                    color,
                    RoundedCornerShape(12.dp)
                )
                else Modifier
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) color.copy(alpha = 0.1f)
                             else AdaptiveColors.SurfaceVariant
        ),
        shape = RoundedCornerShape(12.dp),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = scenario.labelZh,
                    style = MaterialTheme.typography.titleSmall,
                    color = AdaptiveColors.TextPrimary,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = scenario.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = AdaptiveColors.TextSecondary
                )
            }
            if (isSelected) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = color
                )
            }
        }
    }
}

// =============================================================
// Settings Tab Content — 设置 Tab 内容
// =============================================================

@Composable
private fun SettingsTabContent(
    scanState: AdaptiveScanState,
    onStartScan: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        item {
            Text(
                text = "设置",
                style = MaterialTheme.typography.headlineSmall,
                color = AdaptiveColors.TextPrimary,
                fontWeight = FontWeight.Bold
            )
        }

        // 目标 SDK 版本
        item {
            SettingsCard(title = "目标配置") {
                SettingsRow(
                    icon = Icons.Default.SystemUpdate,
                    title = "Target SDK Version",
                    subtitle = "37 (Android 17)",
                    trailing = {
                        Text(
                            text = "SDK 37",
                            style = MaterialTheme.typography.bodyMedium,
                            color = AdaptiveColors.PhaseActive
                        )
                    }
                )
                HorizontalDivider(color = AdaptiveColors.SurfaceVariant, modifier = Modifier.padding(vertical = 8.dp))
                SettingsRow(
                    icon = Icons.Default.TableChart,
                    title = "屏幕尺寸阈值",
                    subtitle = "sw ≥ 600dp 视为大屏",
                    trailing = {
                        Text(
                            text = "600dp",
                            style = MaterialTheme.typography.bodyMedium,
                            color = AdaptiveColors.Warning
                        )
                    }
                )
            }
        }

        // 扫描配置
        item {
            SettingsCard(title = "扫描配置") {
                SettingsRow(
                    icon = Icons.Default.Folder,
                    title = "扫描路径",
                    subtitle = "app/src/main/AndroidManifest.xml",
                    trailing = {
                        Icon(
                            Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = AdaptiveColors.TextTertiary
                        )
                    }
                )
                HorizontalDivider(color = AdaptiveColors.SurfaceVariant, modifier = Modifier.padding(vertical = 8.dp))
                SettingsRow(
                    icon = Icons.Default.BugReport,
                    title = "检查违规类型",
                    subtitle = "全部 (6 种)",
                    trailing = {
                        Text(
                            text = "全部",
                            style = MaterialTheme.typography.bodyMedium,
                            color = AdaptiveColors.PhaseActive
                        )
                    }
                )
            }
        }

        // 报告配置
        item {
            SettingsCard(title = "报告导出") {
                SettingsRow(
                    icon = Icons.Default.Code,
                    title = "导出格式",
                    subtitle = "HTML / JSON / Markdown",
                    trailing = {
                        Text(
                            text = "HTML",
                            style = MaterialTheme.typography.bodyMedium,
                            color = AdaptiveColors.PhaseActive
                        )
                    }
                )
            }
        }

        // 关于
        item {
            SettingsCard(title = "关于") {
                SettingsRow(
                    icon = Icons.Default.Info,
                    title = "版本",
                    subtitle = "Android 17 Adaptive Layout Detector v1.0",
                    trailing = {}
                )
                HorizontalDivider(color = AdaptiveColors.SurfaceVariant, modifier = Modifier.padding(vertical = 8.dp))
                SettingsRow(
                    icon = Icons.Default.Security,
                    title = "合规截止日期",
                    subtitle = "Android 17 正式版: 2026 年 6 月",
                    trailing = {
                        Text(
                            text = "2026-06",
                            style = MaterialTheme.typography.bodyMedium,
                            color = AdaptiveColors.Critical,
                            fontWeight = FontWeight.Bold
                        )
                    }
                )
            }
        }
    }
}

// =============================================================
// Settings Card — 设置卡片
// =============================================================

@Composable
private fun SettingsCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = AdaptiveColors.CardBackground),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = AdaptiveColors.TextPrimary,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}

// =============================================================
// Settings Row — 设置行
// =============================================================

@Composable
private fun SettingsRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    trailing: @Composable () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = AdaptiveColors.PhaseActive,
            modifier = Modifier.size(20.dp)
        )
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = AdaptiveColors.TextPrimary
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = AdaptiveColors.TextTertiary
            )
        }
        trailing()
    }
}
