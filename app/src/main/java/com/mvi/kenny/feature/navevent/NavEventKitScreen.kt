package com.mvi.kenny.feature.navevent

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Compare
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FilePresent
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.NavigateNext
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.collectLatest

// =============================================================
// NavEventKitScreen — Navigation Event KMP 迁移工具包主界面
// PRD-140: Navigation Event KMP 库迁移检测与 PredictiveBackHandler 废弃替代工具包
// =============================================================
/**
 * Main screen for Navigation Event Kit / Navigation Event 工具包主界面
 *
 * Implements a multi-tab interface as specified in the design doc:
 * - Tab bar: Migration Report, File Diff, Dispatcher Tree, Platform Behavior
 *
 * @param viewModel ViewModel instance / ViewModel 实例
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NavEventKitScreen(
    viewModel: NavEventKitViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Collect effects / 收集副作用
    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is NavEventKitEffect.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(effect.message)
                }
                is NavEventKitEffect.ExportSuccess -> {
                    snackbarHostState.showSnackbar("Exported: ${effect.filePath}")
                }
                is NavEventKitEffect.ScanComplete -> {
                    snackbarHostState.showSnackbar("Scan complete: ${state.usages.size} usages found")
                }
                is NavEventKitEffect.MigrationApplied -> {
                    snackbarHostState.showSnackbar("Migration applied successfully!")
                }
                is NavEventKitEffect.DebuggerConnected -> {}
                is NavEventKitEffect.DebuggerDisconnected -> {}
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Navigation Event Kit",
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF1E1E1E),
                    titleContentColor = Color(0xFFE0E0E0)
                ),
                actions = {
                    IconButton(
                        onClick = {
                            viewModel.processIntent(NavEventKitIntent.StartScan("MyMviProject"))
                        }
                    ) {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = "Scan",
                            tint = Color(0xFFE0E0E0)
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFF1E1E1E))
        ) {
            // Tab bar / Tab 栏
            NavEventTabBar(
                activeTab = state.activeTab,
                onTabSelect = { viewModel.processIntent(NavEventKitIntent.SelectTab(it)) }
            )

            // Tab content / Tab 内容
            when (state.activeTab) {
                NavEventTab.MIGRATION_REPORT -> MigrationReportScreen(
                    state = state,
                    onIntent = viewModel::processIntent
                )
                NavEventTab.FILE_DIFF -> FileDiffScreen(
                    state = state,
                    onIntent = viewModel::processIntent
                )
                NavEventTab.DISPATCHER_TREE -> DispatcherTreeScreen(
                    state = state,
                    onIntent = viewModel::processIntent
                )
                NavEventTab.PLATFORM_BEHAVIOR -> PlatformBehaviorScreen(
                    state = state,
                    onIntent = viewModel::processIntent
                )
            }
        }
    }

    // Usage detail dialog / 用法详情对话框
    state.selectedUsage?.let { usage ->
        UsageDetailDialog(
            usage = usage,
            diff = state.previewDiffs[usage.filePath],
            onDismiss = { viewModel.processIntent(NavEventKitIntent.ClearUsage) }
        )
    }
}

// =============================================================
// NavEventTabBar — Tab 栏
// =============================================================
/**
 * Navigation Event Kit tab bar / Navigation Event 工具包 Tab 栏
 */
@Composable
private fun NavEventTabBar(
    activeTab: NavEventTab,
    onTabSelect: (NavEventTab) -> Unit
) {
    TabRow(
        selectedTabIndex = NavEventTab.entries.indexOf(activeTab),
        containerColor = Color(0xFF2D2D2D),
        contentColor = Color(0xFFE0E0E0)
    ) {
        NavEventTab.entries.forEach { tab ->
            Tab(
                selected = activeTab == tab,
                onClick = { onTabSelect(tab) },
                text = {
                    Text(
                        tab.title,
                        fontSize = 12.sp,
                        fontWeight = if (activeTab == tab) FontWeight.Bold else FontWeight.Normal
                    )
                },
                icon = {
                    Icon(
                        imageVector = getTabIcon(tab),
                        contentDescription = tab.title,
                        modifier = Modifier.size(20.dp)
                    )
                }
            )
        }
    }
}

/**
 * Get icon for tab / 获取 Tab 图标
 */
private fun getTabIcon(tab: NavEventTab): ImageVector {
    return when (tab) {
        NavEventTab.MIGRATION_REPORT -> Icons.Default.Search
        NavEventTab.FILE_DIFF -> Icons.Default.Compare
        NavEventTab.DISPATCHER_TREE -> Icons.Default.AccountTree
        NavEventTab.PLATFORM_BEHAVIOR -> Icons.Default.Devices
    }
}

// =============================================================
// MigrationReportScreen — 迁移报告页面
// =============================================================
/**
 * Migration Report Screen / 迁移报告页面
 *
 * Displays scan configuration, progress, and results.
 */
@Composable
private fun MigrationReportScreen(
    state: NavEventKitState,
    onIntent: (NavEventKitIntent) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Scan status card / 扫描状态卡片
        ScanStatusCard(
            scanPhase = state.scanPhase,
            scanProgress = state.scanProgress,
            scannedFiles = state.scannedFiles,
            onStartScan = { onIntent(NavEventKitIntent.StartScan("MyMviProject")) },
            onCancelScan = { onIntent(NavEventKitIntent.CancelScan) }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Summary cards / 摘要卡片
        if (state.scanPhase == ScanPhase.Done || state.scanPhase == ScanPhase.Idle && state.usages.isNotEmpty()) {
            ComplexitySummaryCards(complexityCounts = state.complexityCounts)

            Spacer(modifier = Modifier.height(16.dp))

            // Usage list / 用法列表
            Text(
                "Usages / 用法列表",
                style = MaterialTheme.typography.titleMedium,
                color = Color(0xFFE0E0E0),
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Selection controls / 选择控制
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TextButton(onClick = { onIntent(NavEventKitIntent.SelectAllUsages) }) {
                    Text("Select All", fontSize = 12.sp, color = Color(0xFF42A5F5))
                }
                TextButton(onClick = { onIntent(NavEventKitIntent.DeselectAllUsages) }) {
                    Text("Deselect All", fontSize = 12.sp, color = Color(0xFF42A5F5))
                }
                Text(
                    "${state.selectedCount} selected",
                    fontSize = 12.sp,
                    color = Color(0xFF9E9E9E),
                    modifier = Modifier.align(Alignment.CenterVertically)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            state.sortedUsages.forEach { usage ->
                UsageCard(
                    usage = usage,
                    isSelected = usage.id in state.selectedUsages,
                    onSelect = { onIntent(NavEventKitIntent.ToggleUsageSelection(usage.id)) },
                    onViewDetail = { onIntent(NavEventKitIntent.SelectUsage(usage)) }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Migration wizard controls / 迁移向导控制
            if (state.selectedUsages.isNotEmpty()) {
                MigrationWizardControls(
                    wizardStep = state.wizardStep,
                    selectedCount = state.selectedCount,
                    onNext = { onIntent(NavEventKitIntent.NextWizardStep) },
                    onPrev = { onIntent(NavEventKitIntent.PreviousWizardStep) },
                    onApply = { onIntent(NavEventKitIntent.ApplyMigration) }
                )
            }
        }

        // Empty state / 空状态
        if (state.scanPhase == ScanPhase.Idle && state.usages.isEmpty()) {
            EmptyStateCard(
                icon = Icons.Default.Search,
                title = "No scan results / 无扫描结果",
                description = "Click 'Start Scan' to scan for PredictiveBackHandler usages"
            )
        }
    }
}

// =============================================================
// ScanStatusCard — 扫描状态卡片
// =============================================================
/**
 * Scan status card / 扫描状态卡片
 */
@Composable
private fun ScanStatusCard(
    scanPhase: ScanPhase,
    scanProgress: Float,
    scannedFiles: Int,
    onStartScan: () -> Unit,
    onCancelScan: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2D2D2D))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "PredictiveBackHandler Scanner",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color(0xFFE0E0E0),
                    fontWeight = FontWeight.Bold
                )
                ScanPhaseIndicator(scanPhase = scanPhase)
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Phase indicator / 阶段指示器
            when (scanPhase) {
                is ScanPhase.Scanning -> {
                    Text(
                        "Scanning... / 扫描中...",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF9E9E9E)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { scanProgress },
                        modifier = Modifier.fillMaxWidth(),
                        color = Color(0xFF1565C0)
                    )
                    Text(
                        "Files scanned: $scannedFiles",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF757575)
                    )
                }
                is ScanPhase.Analyzing -> {
                    Text(
                        "Analyzing complexity... / 分析复杂度中...",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFFFA726)
                    )
                    LinearProgressIndicator(
                        progress = { scanProgress },
                        modifier = Modifier.fillMaxWidth(),
                        color = Color(0xFFFFA726)
                    )
                }
                is ScanPhase.GeneratingDiffs -> {
                    Text(
                        "Generating diffs... / 生成差异中...",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF42A5F5)
                    )
                    LinearProgressIndicator(
                        progress = { scanProgress },
                        modifier = Modifier.fillMaxWidth(),
                        color = Color(0xFF42A5F5)
                    )
                }
                is ScanPhase.ApplyingConfig -> {
                    Text(
                        scanPhase.message,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF66BB6A)
                    )
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth(),
                        color = Color(0xFF66BB6A)
                    )
                }
                is ScanPhase.Done -> {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color(0xFF66BB6A)
                        )
                        Text(
                            "Scan complete / 扫描完成",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF66BB6A)
                        )
                    }
                }
                is ScanPhase.Error -> {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.Error,
                            contentDescription = null,
                            tint = Color(0xFFEF5350)
                        )
                        Text(
                            scanPhase.message,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFFEF5350)
                        )
                    }
                }
                else -> {}
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action buttons / 操作按钮
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                if (scanPhase == ScanPhase.Scanning ||
                    scanPhase == ScanPhase.Analyzing ||
                    scanPhase == ScanPhase.GeneratingDiffs) {
                    TextButton(onClick = onCancelScan) {
                        Icon(Icons.Default.Close, null, tint = Color(0xFFEF5350))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Cancel", color = Color(0xFFEF5350))
                    }
                } else if (scanPhase == ScanPhase.Idle || scanPhase == ScanPhase.Done || scanPhase is ScanPhase.Error) {
                    Button(onClick = onStartScan) {
                        Icon(Icons.Default.Search, null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Start Scan")
                    }
                }
            }
        }
    }
}

/**
 * Scan phase indicator / 扫描阶段指示器
 */
@Composable
private fun ScanPhaseIndicator(scanPhase: ScanPhase) {
    val (color, text) = when (scanPhase) {
        is ScanPhase.Idle -> Pair(Color(0xFF757575), "Idle")
        is ScanPhase.Scanning -> Pair(Color(0xFF1565C0), "Scanning")
        is ScanPhase.Analyzing -> Pair(Color(0xFFFFA726), "Analyzing")
        is ScanPhase.GeneratingDiffs -> Pair(Color(0xFF42A5F5), "Generating")
        is ScanPhase.ApplyingConfig -> Pair(Color(0xFF66BB6A), "Applying")
        is ScanPhase.Done -> Pair(Color(0xFF66BB6A), "Done")
        is ScanPhase.Error -> Pair(Color(0xFFEF5350), "Error")
    }

    Badge(containerColor = color) {
        Text(text, modifier = Modifier.padding(horizontal = 4.dp))
    }
}

// =============================================================
// ComplexitySummaryCards — 复杂度摘要卡片
// =============================================================
/**
 * Complexity summary cards / 复杂度摘要卡片
 */
@Composable
private fun ComplexitySummaryCards(
    complexityCounts: Map<MigrationComplexity, Int>
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        MigrationComplexity.entries.forEach { complexity ->
            val count = complexityCounts[complexity] ?: 0
            ComplexityCard(complexity = complexity, count = count)
        }
    }
}

/**
 * Complexity card / 复杂度卡片
 */
@Composable
private fun ComplexityCard(
    complexity: MigrationComplexity,
    count: Int
) {
    Card(
        modifier = Modifier.width(90.dp),
        colors = CardDefaults.cardColors(
            containerColor = complexity.color.copy(alpha = 0.15f)
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "$count",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = complexity.color
            )
            Text(
                complexity.label,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                color = complexity.color,
                fontSize = 10.sp
            )
        }
    }
}

// =============================================================
// UsageCard — 用法卡片
// =============================================================
/**
 * Usage card / 用法卡片
 */
@Composable
private fun UsageCard(
    usage: PredictiveBackHandlerUsage,
    isSelected: Boolean,
    onSelect: () -> Unit,
    onViewDetail: () -> Unit
) {
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) Color(0xFF1565C0) else usage.complexity.color.copy(alpha = 0.5f),
        label = "borderColor"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 2.dp,
                color = borderColor,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onViewDetail),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF2D2D2D)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = isSelected,
                onCheckedChange = { onSelect() }
            )

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        usage.className,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFE0E0E0)
                    )
                    Badge(containerColor = usage.complexity.color) {
                        Text(
                            usage.complexity.label,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 4.dp),
                            fontSize = 10.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    "${usage.methodName}()",
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = FontFamily.Monospace,
                    color = Color(0xFFBDBDBD)
                )

                Text(
                    "${usage.filePath}:${usage.lineNumber}",
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = FontFamily.Monospace,
                    color = Color(0xFF757575),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Icon(
                Icons.Default.NavigateNext,
                contentDescription = "View detail",
                tint = Color(0xFF757575)
            )
        }
    }
}

// =============================================================
// MigrationWizardControls — 迁移向导控制
// =============================================================
/**
 * Migration wizard controls / 迁移向导控制
 */
@Composable
private fun MigrationWizardControls(
    wizardStep: WizardStep,
    selectedCount: Int,
    onNext: () -> Unit,
    onPrev: () -> Unit,
    onApply: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2D2D2D))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Migration Wizard / 迁移向导",
                style = MaterialTheme.typography.titleMedium,
                color = Color(0xFFE0E0E0),
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                "Step: ${wizardStep.name} | Selected: $selectedCount usages",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF9E9E9E)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (wizardStep != WizardStep.SELECT_SCOPE && wizardStep != WizardStep.APPLYING && wizardStep != WizardStep.DONE) {
                    TextButton(onClick = onPrev) {
                        Icon(Icons.AutoMirrored.Filled.Backspace, null, tint = Color(0xFF42A5F5))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Back", color = Color(0xFF42A5F5))
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                when (wizardStep) {
                    WizardStep.CONFIRM -> {
                        Button(onClick = onApply) {
                            Icon(Icons.Default.Check, null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Apply Migration")
                        }
                    }
                    WizardStep.DONE -> {
                        Button(onClick = {}) {
                            Icon(Icons.Default.CheckCircle, null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Done")
                        }
                    }
                    else -> {
                        Button(onClick = onNext) {
                            Text("Next")
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, null)
                        }
                    }
                }
            }
        }
    }
}

// =============================================================
// FileDiffScreen — 文件差异页面
// =============================================================
/**
 * File Diff Screen / 文件差异页面
 */
@Composable
private fun FileDiffScreen(
    state: NavEventKitState,
    onIntent: (NavEventKitIntent) -> Unit
) {
    var selectedFile by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            "Migration Diffs / 迁移差异",
            style = MaterialTheme.typography.titleMedium,
            color = Color(0xFFE0E0E0),
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        // File list / 文件列表
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            // File list panel / 文件列表面板
            Card(
                modifier = Modifier
                    .width(250.dp)
                    .fillMaxSize(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF2D2D2D))
            ) {
                LazyColumn(modifier = Modifier.padding(8.dp)) {
                    items(state.usages) { usage ->
                        DiffFileItem(
                            filePath = usage.filePath,
                            isSelected = selectedFile == usage.filePath,
                            complexity = usage.complexity,
                            onClick = { selectedFile = usage.filePath }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Diff view / 差异视图
            Card(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0D1117))
            ) {
                val diff = selectedFile?.let { state.previewDiffs[it] }

                if (diff != null) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                selectedFile ?: "",
                                style = MaterialTheme.typography.titleSmall,
                                color = Color(0xFFE0E0E0),
                                fontFamily = FontFamily.Monospace,
                                fontSize = 12.sp
                            )
                            IconButton(
                                onClick = { /* Copy to clipboard */ }
                            ) {
                                Icon(
                                    Icons.Default.ContentCopy,
                                    contentDescription = "Copy",
                                    tint = Color(0xFF9E9E9E)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF161B22))
                                .padding(12.dp)
                                .verticalScroll(rememberScrollState())
                        ) {
                            DiffContent(diff = diff)
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Select a file to view diff / 选择文件查看差异",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF757575)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Diff file item / 差异文件项
 */
@Composable
private fun DiffFileItem(
    filePath: String,
    isSelected: Boolean,
    complexity: MigrationComplexity,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable(onClick = onClick)
            .border(
                width = if (isSelected) 2.dp else 0.dp,
                color = if (isSelected) Color(0xFF1565C0) else Color.Transparent,
                shape = RoundedCornerShape(8.dp)
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color(0xFF1565C0).copy(alpha = 0.2f) else Color(0xFF161B22)
        )
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.FilePresent,
                contentDescription = null,
                tint = complexity.color,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    filePath.substringAfterLast("/"),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFFE0E0E0),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp
                )
                Text(
                    complexity.label,
                    style = MaterialTheme.typography.labelSmall,
                    color = complexity.color,
                    fontSize = 10.sp
                )
            }
        }
    }
}

/**
 * Diff content display / 差异内容显示
 */
@Composable
private fun DiffContent(diff: String) {
    Column {
        diff.lines().forEach { line ->
            val (color, prefix) = when {
                line.startsWith("---") -> Pair(Color(0xFFEF5350), "-")
                line.startsWith("+++") -> Pair(Color(0xFF66BB6A), "+")
                line.startsWith("@@") -> Pair(Color(0xFF42A5F5), "")
                line.startsWith("+") && !line.startsWith("+++") -> Pair(Color(0xFF66BB6A).copy(alpha = 0.7f), "+")
                line.startsWith("-") && !line.startsWith("---") -> Pair(Color(0xFFEF5350).copy(alpha = 0.7f), "-")
                else -> Pair(Color(0xFFBDBDBD), " ")
            }

            Row(modifier = Modifier.fillMaxWidth()) {
                Text(
                    prefix,
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    color = color,
                    modifier = Modifier.width(16.dp)
                )
                Text(
                    line.removePrefix(prefix),
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    color = color
                )
            }
        }
    }
}

// =============================================================
// DispatcherTreeScreen — Dispatcher 树页面
// =============================================================
/**
 * Dispatcher Tree Screen / Dispatcher 树页面
 */
@Composable
private fun DispatcherTreeScreen(
    state: NavEventKitState,
    onIntent: (NavEventKitIntent) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Toolbar / 工具栏
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Hierarchical Dispatcher Tree / 分层 Dispatcher 树",
                style = MaterialTheme.typography.titleMedium,
                color = Color(0xFFE0E0E0),
                fontWeight = FontWeight.Bold
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (state.isConnected) {
                    // Export dropdown / 导出下拉菜单
                    ExportDropdown(
                        onExport = { format -> onIntent(NavEventKitIntent.ExportTree(format)) }
                    )
                }

                // Connect/Disconnect button / 连接/断开按钮
                TextButton(
                    onClick = {
                        if (state.isConnected) {
                            onIntent(NavEventKitIntent.DisconnectDebugger)
                        } else {
                            onIntent(NavEventKitIntent.ConnectDebugger)
                        }
                    }
                ) {
                    Icon(
                        if (state.isConnected) Icons.Default.Close else Icons.Default.BugReport,
                        null,
                        tint = if (state.isConnected) Color(0xFFEF5350) else Color(0xFF66BB6A)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        if (state.isConnected) "Disconnect" else "Connect",
                        color = if (state.isConnected) Color(0xFFEF5350) else Color(0xFF66BB6A)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (state.isConnected && state.treeRoot != null) {
            // Tree view / 树视图
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF2D2D2D))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Connection status / 连接状态
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color(0xFF66BB6A),
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            "Debugger Connected",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF66BB6A)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Platform filter / 平台过滤器
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = state.filterPlatform == null,
                            onClick = { onIntent(NavEventKitIntent.SetPlatformFilter(null)) },
                            label = { Text("All", fontSize = 11.sp) }
                        )
                        TargetPlatform.entries.take(2).forEach { platform ->
                            FilterChip(
                                selected = state.filterPlatform == platform,
                                onClick = { onIntent(NavEventKitIntent.SetPlatformFilter(platform)) },
                                label = { Text(platform.displayName, fontSize = 11.sp) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Tree / 树
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                    ) {
                        state.treeRoot?.let { root ->
                            DispatcherTreeNode(
                                node = root,
                                expandedNodes = state.expandedNodes,
                                selectedNode = state.selectedNode,
                                onToggleExpand = { onIntent(NavEventKitIntent.ToggleNodeExpand(it)) },
                                onSelect = { onIntent(NavEventKitIntent.SelectDispatcherNode(it)) }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Selected node info / 选中节点信息
            state.selectedNode?.let { node ->
                DispatcherNodeInfoCard(node = node)
            }
        } else {
            // Not connected state / 未连接状态
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.AccountTree,
                        contentDescription = null,
                        tint = Color(0xFF757575),
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "Not connected to debugger",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color(0xFF9E9E9E)
                    )
                    Text(
                        "Connect to view dispatcher tree",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF757575)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { onIntent(NavEventKitIntent.ConnectDebugger) }) {
                        Icon(Icons.Default.BugReport, null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Connect Debugger")
                    }
                }
            }
        }
    }
}

/**
 * Dispatcher tree node / Dispatcher 树节点
 */
@Composable
private fun DispatcherTreeNode(
    node: DispatcherNode,
    expandedNodes: Set<String>,
    selectedNode: DispatcherNode?,
    onToggleExpand: (String) -> Unit,
    onSelect: (DispatcherNode) -> Unit
) {
    val isExpanded = node.id in expandedNodes
    val isSelected = selectedNode?.id == node.id

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onSelect(node) }
                .background(
                    if (isSelected) node.type.color.copy(alpha = 0.2f) else Color.Transparent
                )
                .padding(start = (node.depth * 16).dp, top = 4.dp, bottom = 4.dp, end = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Expand/collapse button / 展开/折叠按钮
            if (node.children.isNotEmpty()) {
                IconButton(
                    onClick = { onToggleExpand(node.id) },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (isExpanded) "Collapse" else "Expand",
                        tint = Color(0xFF9E9E9E),
                        modifier = Modifier.size(16.dp)
                    )
                }
            } else {
                Spacer(modifier = Modifier.width(24.dp))
            }

            // Node icon / 节点图标
            Icon(
                getDispatcherIcon(node.type),
                contentDescription = null,
                tint = node.type.color,
                modifier = Modifier.size(20.dp)
            )

            Spacer(modifier = Modifier.width(8.dp))

            // Node name / 节点名称
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    node.name,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFFE0E0E0),
                    fontWeight = if (node.isActive) FontWeight.Bold else FontWeight.Normal
                )
                Text(
                    node.type.label,
                    style = MaterialTheme.typography.labelSmall,
                    color = node.type.color
                )
            }

            // Active indicator / 活跃指示器
            if (node.isActive) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF4CAF50))
                )
            }
        }

        // Children / 子节点
        if (isExpanded && node.children.isNotEmpty()) {
            node.children.forEach { child ->
                DispatcherTreeNode(
                    node = child,
                    expandedNodes = expandedNodes,
                    selectedNode = selectedNode,
                    onToggleExpand = onToggleExpand,
                    onSelect = onSelect
                )
            }
        }
    }
}

/**
 * Get icon for dispatcher type / 获取 Dispatcher 类型图标
 */
private fun getDispatcherIcon(type: DispatcherType): ImageVector {
    return when (type) {
        DispatcherType.ROOT -> Icons.Default.Home
        DispatcherType.NAV_HOST -> Icons.Default.AccountTree
        DispatcherType.SCREEN -> Icons.Default.Terminal
        DispatcherType.TAB -> Icons.Default.Info
    }
}

/**
 * Dispatcher node info card / Dispatcher 节点信息卡片
 */
@Composable
private fun DispatcherNodeInfoCard(node: DispatcherNode) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2D2D2D))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    getDispatcherIcon(node.type),
                    contentDescription = null,
                    tint = node.type.color
                )
                Text(
                    node.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color(0xFFE0E0E0),
                    fontWeight = FontWeight.Bold
                )
                if (node.isActive) {
                    Badge(containerColor = Color(0xFF4CAF50)) {
                        Text("Active", modifier = Modifier.padding(horizontal = 4.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                InfoItem(label = "Type", value = node.type.label)
                InfoItem(label = "Depth", value = "${node.depth}")
                InfoItem(label = "Children", value = "${node.children.size}")
            }
        }
    }
}

/**
 * Info item / 信息项
 */
@Composable
private fun InfoItem(label: String, value: String) {
    Column {
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = Color(0xFF757575)
        )
        Text(
            value,
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFFE0E0E0)
        )
    }
}

/**
 * Export dropdown / 导出下拉菜单
 */
@Composable
private fun ExportDropdown(
    onExport: (ExportFormat) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        TextButton(onClick = { expanded = true }) {
            Icon(Icons.Default.Download, null, tint = Color(0xFF42A5F5))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Export", color = Color(0xFF42A5F5))
        }

        ExportDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            onExport = onExport
        )
    }
}

// =============================================================
// PlatformBehaviorScreen — 平台行为页面
// =============================================================
/**
 * Platform Behavior Screen / 平台行为页面
 */
@Composable
private fun PlatformBehaviorScreen(
    state: NavEventKitState,
    onIntent: (NavEventKitIntent) -> Unit
) {
    val platformBehaviors = remember {
        listOf(
            PlatformBehavior(
                feature = "OnBackInvokedInput",
                androidBehavior = "Full support / 完全支持",
                iosBehavior = "Not available / 不可用",
                desktopBehavior = "Not available / 不可用",
                jsBehavior = "Not available / 不可用",
                hasDifference = true
            ),
            PlatformBehavior(
                feature = "DirectNavigationEventInput",
                androidBehavior = "Full support / 完全支持",
                iosBehavior = "Limited / 有限支持",
                desktopBehavior = "Limited / 有限支持",
                jsBehavior = "Not available / 不可用",
                hasDifference = true
            ),
            PlatformBehavior(
                feature = "PredictiveBackGesture",
                androidBehavior = "System handles / 系统处理",
                iosBehavior = "System handles / 系统处理",
                desktopBehavior = "N/A",
                jsBehavior = "N/A",
                hasDifference = false
            ),
            PlatformBehavior(
                feature = "BackHandler Priority",
                androidBehavior = "High priority / 高优先级",
                iosBehavior = "Normal priority / 普通优先级",
                desktopBehavior = "N/A",
                jsBehavior = "N/A",
                hasDifference = true
            ),
            PlatformBehavior(
                feature = "Event Propagation",
                androidBehavior = "Top-down / 自上而下",
                iosBehavior = "Bottom-up / 自下而上",
                desktopBehavior = "Top-down / 自上而下",
                jsBehavior = "Top-down / 自上而下",
                hasDifference = true
            ),
            PlatformBehavior(
                feature = "Multi-window Support",
                androidBehavior = "Full support / 完全支持",
                iosBehavior = "Limited / 有限支持",
                desktopBehavior = "Full support / 完全支持",
                jsBehavior = "N/A",
                hasDifference = true
            )
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            "Platform Behavior Matrix / 平台行为矩阵",
            style = MaterialTheme.typography.titleMedium,
            color = Color(0xFFE0E0E0),
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            "NavigationEventDispatcher behavior differences across platforms / 各平台 NavigationEventDispatcher 行为差异",
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFF9E9E9E)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Platform header / 平台头部
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF2D2D2D))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Box(modifier = Modifier.width(140.dp)) {
                    Text(
                        "Feature",
                        style = MaterialTheme.typography.titleSmall,
                        color = Color(0xFFE0E0E0),
                        fontWeight = FontWeight.Bold
                    )
                }
                PlatformHeaderItem(platform = "Android", color = Color(0xFF66BB6A))
                PlatformHeaderItem(platform = "iOS", color = Color(0xFF1565C0))
                PlatformHeaderItem(platform = "Desktop", color = Color(0xFFFFA726))
                PlatformHeaderItem(platform = "JS/Web", color = Color(0xFF42A5F5))
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Behavior rows / 行为行
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(platformBehaviors) { behavior ->
                PlatformBehaviorRow(behavior = behavior)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Legend / 图例
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF2D2D2D))
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    "Legend / 图例",
                    style = MaterialTheme.typography.titleSmall,
                    color = Color(0xFFE0E0E0),
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    LegendItem(color = Color(0xFF66BB6A), label = "Full support")
                    LegendItem(color = Color(0xFFFFA726), label = "Limited")
                    LegendItem(color = Color(0xFFEF5350), label = "Not available")
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFFEB3B))
                    )
                    Text(
                        "Has cross-platform difference / 存在跨平台差异",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF9E9E9E)
                    )
                }
            }
        }
    }
}

/**
 * Platform header item / 平台头部项
 */
@Composable
private fun PlatformHeaderItem(platform: String, color: Color) {
    Box(
        modifier = Modifier.width(100.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            platform,
            style = MaterialTheme.typography.titleSmall,
            color = color,
            fontWeight = FontWeight.Bold
        )
    }
}

/**
 * Platform behavior row / 平台行为行
 */
@Composable
private fun PlatformBehaviorRow(behavior: PlatformBehavior) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (behavior.hasDifference)
                Color(0xFFFFEB3B).copy(alpha = 0.05f)
            else
                Color(0xFF2D2D2D)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Feature name / 功能名称
            Box(modifier = Modifier.width(140.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (behavior.hasDifference) {
                        Icon(
                            Icons.Default.Warning,
                            contentDescription = "Has differences",
                            tint = Color(0xFFFFEB3B),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                    Text(
                        behavior.feature,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFE0E0E0),
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Android / Android
            PlatformBehaviorCell(
                behavior = behavior.androidBehavior,
                hasSupport = behavior.androidBehavior.contains("Full"),
                isLimited = behavior.androidBehavior.contains("Limited")
            )

            // iOS
            PlatformBehaviorCell(
                behavior = behavior.iosBehavior,
                hasSupport = behavior.iosBehavior.contains("Full"),
                isLimited = behavior.iosBehavior.contains("Limited")
            )

            // Desktop
            PlatformBehaviorCell(
                behavior = behavior.desktopBehavior,
                hasSupport = behavior.desktopBehavior.contains("Full"),
                isLimited = behavior.desktopBehavior.contains("Limited")
            )

            // JS/Web
            PlatformBehaviorCell(
                behavior = behavior.jsBehavior,
                hasSupport = behavior.jsBehavior.contains("Full"),
                isLimited = behavior.jsBehavior.contains("Limited")
            )
        }
    }
}

/**
 * Platform behavior cell / 平台行为单元格
 */
@Composable
private fun PlatformBehaviorCell(
    behavior: String,
    hasSupport: Boolean,
    isLimited: Boolean
) {
    val color = when {
        behavior.contains("N/A") || behavior.contains("Not available") -> Color(0xFFEF5350)
        hasSupport -> Color(0xFF66BB6A)
        isLimited -> Color(0xFFFFA726)
        else -> Color(0xFF9E9E9E)
    }

    Box(modifier = Modifier.width(100.dp)) {
        Text(
            behavior,
            style = MaterialTheme.typography.bodySmall,
            color = color,
            fontSize = 11.sp
        )
    }
}

/**
 * Legend item / 图例项
 */
@Composable
private fun LegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            label,
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFF9E9E9E)
        )
    }
}

// =============================================================
// UsageDetailDialog — 用法详情对话框
// =============================================================
/**
 * Usage detail dialog / 用法详情对话框
 */
@Composable
private fun UsageDetailDialog(
    usage: PredictiveBackHandlerUsage,
    diff: String?,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(usage.className)
                Badge(containerColor = usage.complexity.color) {
                    Text(
                        usage.complexity.label,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    "Call Chain / 调用链",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    usage.callChain,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    "Location / 位置",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "${usage.filePath}:${usage.lineNumber}",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    "Migration Suggestion / 迁移建议",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    usage.migrationSuggestion,
                    style = MaterialTheme.typography.bodySmall
                )

                if (diff != null) {
                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        "Migration Diff / 迁移差异",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF161B22))
                            .padding(8.dp)
                    ) {
                        DiffContent(diff = diff)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

// =============================================================
// EmptyStateCard — 空状态卡片
// =============================================================
/**
 * Empty state card / 空状态卡片
 */
@Composable
private fun EmptyStateCard(
    icon: ImageVector,
    title: String,
    description: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF2D2D2D).copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = Color(0xFF757575)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                title,
                style = MaterialTheme.typography.titleMedium,
                color = Color(0xFF9E9E9E)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                description,
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF757575),
                textAlign = TextAlign.Center
            )
        }
    }
}

// =============================================================
// ExportDropdownMenu — 自定义导出下拉菜单
// =============================================================
/**
 * Custom dropdown menu item / 自定义下拉菜单项
 */
@Composable
private fun ExportDropdownMenuItem(
    text: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Text(text = text, color = Color(0xFFE0E0E0))
    }
}

/**
 * Custom dropdown menu / 自定义下拉菜单
 */
@Composable
private fun ExportDropdownMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    onExport: (ExportFormat) -> Unit
) {
    if (expanded) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable(onClick = onDismissRequest)
                .background(Color.Black.copy(alpha = 0.3f))
        ) {
            Card(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF2D2D2D))
            ) {
                Column {
                    ExportDropdownMenuItem(text = "JSON", onClick = { onExport(ExportFormat.JSON); onDismissRequest() })
                    ExportDropdownMenuItem(text = "GraphML", onClick = { onExport(ExportFormat.GRAPHML); onDismissRequest() })
                    ExportDropdownMenuItem(text = "Markdown", onClick = { onExport(ExportFormat.MARKDOWN); onDismissRequest() })
                }
            }
        }
    }
}

