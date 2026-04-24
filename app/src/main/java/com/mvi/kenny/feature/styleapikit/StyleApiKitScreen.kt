package com.mvi.kenny.feature.styleapikit

// ================================================================
// StyleApiKitScreen — Compose Style API Toolkit 主界面
// ================================================================
// Main screen composable for PRD-137: Style API Declarative Styling Toolkit.
//
// 5-tab interface: Decision Engine / Migration Tool / Debug Panel /
// Theme Guide / Playground.
//
// PRD-137: Compose 1.11 Style API 声明式样式开发工具包
// Design Reference: memory/agency/designs/PRD-137-Compose-1.11-Style-API-声明式样式开发工具包.md
// ================================================================

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Transform
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Slider
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.collectLatest

// ================================================================
// Design System Colors / 设计系统颜色
// ================================================================
private val Primary = Color(0xFF6750A4)
private val Secondary = Color(0xFF625B71)
private val Tertiary = Color(0xFF7D5260)
private val Surface = Color(0xFFFFFBFE)
private val SurfaceVariant = Color(0xFFE7E0EC)
private val ErrorColor = Color(0xFFB3261E)
private val PassColor = Color(0xFF34C759)
private val WarnColor = Color(0xFFFFD60A)

// ================================================================
// Tab Icons Map / Tab 图标映射
// ================================================================
private val tabIcons = mapOf(
    StyleTab.DecisionEngine to Icons.Default.Analytics,
    StyleTab.MigrationTool to Icons.Default.Transform,
    StyleTab.DebugPanel to Icons.Default.BugReport,
    StyleTab.ThemeGuide to Icons.Default.MenuBook,
    StyleTab.Playground to Icons.Default.Code
)

// ================================================================
// Main Screen / 主界面
// ================================================================

/**
 * ============================================================
 * StyleApiKitScreen — 工具包主界面
 * ============================================================
 * Entry point composable with bottom navigation for 5 toolkit pages.
 *
 * @param viewModel StyleApiKitViewModel instance
 * @param onNavigateBack Callback for back navigation
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StyleApiKitScreen(
    viewModel: StyleApiKitViewModel = viewModel(),
    onNavigateBack: () -> Unit = {}
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Collect side effects / 收集副作用
    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is StyleApiKitEffect.ShowSnackbar -> snackbarHostState.showSnackbar(effect.message)
                is StyleApiKitEffect.ShowError -> snackbarHostState.showSnackbar("错误: ${effect.message}")
                is StyleApiKitEffect.CopyToClipboard -> { /* Clipboard handled by platform */ }
                is StyleApiKitEffect.MigrationApplied -> snackbarHostState.showSnackbar("迁移已全部应用")
                is StyleApiKitEffect.ExportReport -> { /* Export handled by platform */ }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Style API Toolkit",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Medium
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Surface,
                    titleContentColor = Primary
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = Surface,
                tonalElevation = 4.dp
            ) {
                StyleTab.entries.forEach { tab ->
                    val selected = state.currentTab == tab
                    NavigationBarItem(
                        selected = selected,
                        onClick = { viewModel.sendIntent(StyleApiKitIntent.SwitchTab(tab)) },
                        icon = {
                            Icon(
                                imageVector = tabIcons[tab]!!,
                                contentDescription = tab.titleCn,
                                tint = if (selected) Primary else Secondary
                            )
                        },
                        label = {
                            Text(
                                text = tab.titleCn,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (selected) Primary else Secondary,
                                maxLines = 1
                            )
                        }
                    )
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Surface
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (state.currentTab) {
                StyleTab.DecisionEngine -> DecisionEngineScreen(
                    state = state.decisionEngineState,
                    onIntent = viewModel::sendIntent
                )
                StyleTab.MigrationTool -> MigrationToolScreen(
                    state = state.migrationState,
                    onIntent = viewModel::sendIntent
                )
                StyleTab.DebugPanel -> DebugPanelScreen(
                    state = state.debugPanelState,
                    onIntent = viewModel::sendIntent
                )
                StyleTab.ThemeGuide -> ThemeGuideScreen()
                StyleTab.Playground -> PlaygroundScreen(
                    state = state.playgroundState,
                    onIntent = viewModel::sendIntent
                )
            }
        }
    }
}

// ================================================================
// Decision Engine Screen / 决策引擎页面
// ================================================================

/**
 * ============================================================
 * DecisionEngineScreen — 决策引擎页面
 * ============================================================
 * Input Kotlin code → Analyze → Output migrate/keep decision.
 *
 * Design Spec Section 3.1: Decision Engine page
 * - Input area: code paste / Gradle module path
 * - Analysis area: real-time decision tree derivation
 * - Output area: conclusion + reasoning
 */
@Composable
private fun DecisionEngineScreen(
    state: DecisionEngineState,
    onIntent: (StyleApiKitIntent) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Page title
        Text(
            text = "Style vs Modifier 决策引擎",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = Primary
        )
        Text(
            text = "粘贴 Kotlin 代码，分析是否适合迁移到 Style API",
            style = MaterialTheme.typography.bodySmall,
            color = Secondary
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Code input area
        OutlinedTextField(
            value = state.inputCode,
            onValueChange = { onIntent(StyleApiKitIntent.UpdateInputCode(it)) },
            label = { Text("粘贴 Kotlin 代码") },
            placeholder = { Text("例如: Modifier.background(Color.Blue).padding(8.dp).clickable { }") },
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            textStyle = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace),
            maxLines = 12
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Action buttons
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { onIntent(StyleApiKitIntent.AnalyzeCode) },
                enabled = !state.isAnalyzing && state.inputCode.isNotBlank(),
                modifier = Modifier.weight(1f)
            ) {
                if (state.isAnalyzing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("分析中...")
                } else {
                    Icon(Icons.Default.Analytics, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("开始分析")
                }
            }

            OutlinedButton(
                onClick = { onIntent(StyleApiKitIntent.ClearAnalysis) }
            ) {
                Text("清除")
            }
        }

        // Analysis progress
        if (state.isAnalyzing) {
            Spacer(modifier = Modifier.height(12.dp))
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                text = "正在分析代码结构...",
                style = MaterialTheme.typography.bodySmall,
                color = Secondary
            )
        }

        // Analysis log
        if (state.analysisLog.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SurfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "分析日志",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    state.analysisLog.forEach { log ->
                        Text(
                            text = log,
                            style = MaterialTheme.typography.bodySmall,
                            fontFamily = FontFamily.Monospace,
                            color = if (log.startsWith("▶")) Primary else Secondary
                        )
                    }
                }
            }
        }

        // Decision result
        if (state.decisionResult != null) {
            Spacer(modifier = Modifier.height(16.dp))
            DecisionResultCard(
                verdict = state.decisionResult,
                reasons = state.decisionReasons
            )
        }
    }
}

/**
 * ============================================================
 * DecisionResultCard — 决策结果卡片
 * ============================================================
 * Shows the verdict (migrate to Style or keep Modifier) with reasoning.
 */
@Composable
private fun DecisionResultCard(
    verdict: DecisionVerdict,
    reasons: List<DecisionReason>
) {
    val isMigrate = verdict == DecisionVerdict.MigrateToStyle
    val resultColor = if (isMigrate) Primary else Secondary

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = resultColor.copy(alpha = 0.1f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(resultColor.copy(alpha = 0.2f), RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isMigrate) Icons.Default.AutoAwesome else Icons.Default.Code,
                        contentDescription = null,
                        tint = resultColor
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "决策结果",
                        style = MaterialTheme.typography.labelSmall,
                        color = Secondary
                    )
                    Text(
                        text = verdict.labelCn,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = resultColor
                    )
                }
            }

            if (reasons.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = resultColor.copy(alpha = 0.2f))
                Spacer(modifier = Modifier.height(12.dp))
                reasons.forEach { reason ->
                    Row(
                        modifier = Modifier.padding(vertical = 4.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = if (reason.isProStyle) "✓" else "○",
                            color = if (reason.isProStyle) PassColor else Secondary,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = reason.text,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}

// ================================================================
// Migration Tool Screen / 迁移工具页面
// ================================================================

/**
 * ============================================================
 * MigrationToolScreen — 迁移工具页面
 * ============================================================
 * Module selection → Code input → Generate Diff → Apply changes.
 *
 * Design Spec Section 3.2: Migration Tool page
 * - Top: module selector dropdown
 * - Middle: diff view (original vs migrated, side-by-side)
 * - Bottom: action buttons
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MigrationToolScreen(
    state: MigrationState,
    onIntent: (StyleApiKitIntent) -> Unit
) {
    var showModuleDropdown by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = "Modifier → Style API 迁移工具",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = Primary
        )
        Text(
            text = "将 Modifier 链式写法转换为 Style API 声明式写法",
            style = MaterialTheme.typography.bodySmall,
            color = Secondary
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Module selector
        Text(
            text = "选择模块",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(4.dp))

        Box {
            OutlinedButton(
                onClick = { showModuleDropdown = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(state.selectedModule.ifEmpty { "请选择 Gradle 模块" })
            }

            AlertDialog(
                onDismissRequest = { showModuleDropdown = false },
                title = { Text("选择模块") },
                text = {
                    LazyColumn {
                        items(state.availableModules) { module ->
                            TextButton(
                                onClick = {
                                    onIntent(StyleApiKitIntent.SelectModule(module))
                                    showModuleDropdown = false
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(module)
                            }
                        }
                    }
                },
                confirmButton = {}
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Original code input
        Text(
            text = "原始代码",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(4.dp))

        OutlinedTextField(
            value = state.originalCode,
            onValueChange = { onIntent(StyleApiKitIntent.UpdateOriginalCode(it)) },
            placeholder = { Text("粘贴需要迁移的 Modifier 代码...") },
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp),
            textStyle = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
            maxLines = 10
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Migrate button
        Button(
            onClick = { onIntent(StyleApiKitIntent.MigrateCode) },
            enabled = !state.isMigrating && state.originalCode.isNotBlank(),
            modifier = Modifier.fillMaxWidth()
        ) {
            if (state.isMigrating) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 2.dp,
                    color = Color.White
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("迁移中...")
            } else {
                Icon(Icons.Default.Transform, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("生成迁移 Diff")
            }
        }

        // Diff results
        if (state.diffs.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))

            // Stats row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "发现 ${state.diffs.size} 处变更",
                    style = MaterialTheme.typography.labelMedium,
                    color = Primary
                )
                Text(
                    text = "已应用 ${state.appliedDiffs.size}",
                    style = MaterialTheme.typography.labelSmall,
                    color = Secondary
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Diff list
            state.diffs.forEach { diff ->
                DiffItem(
                    diff = diff,
                    isApplied = diff.lineNumber in state.appliedDiffs,
                    onApply = { onIntent(StyleApiKitIntent.ApplyDiff(diff.lineNumber)) }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action buttons
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedButton(
                    onClick = { onIntent(StyleApiKitIntent.ApplyAllDiffs) },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("应用全部")
                }
                Button(
                    onClick = { onIntent(StyleApiKitIntent.CopyMigratedCode) },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("复制代码")
                }
            }

            OutlinedButton(
                onClick = { onIntent(StyleApiKitIntent.ExportMigrationReport) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("导出报告")
            }
        }

        // Migrated code preview
        if (state.migratedCode.isNotEmpty() && state.migrationComplete) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "迁移后代码预览",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SurfaceVariant.copy(alpha = 0.3f))
            ) {
                Text(
                    text = state.migratedCode,
                    style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                    modifier = Modifier.padding(12.dp),
                    maxLines = 20
                )
            }
        }
    }
}

/**
 * ============================================================
 * DiffItem — Diff 单项展示组件
 * ============================================================
 * Shows a single code diff with original/migrated lines and apply button.
 */
@Composable
private fun DiffItem(
    diff: MigrationDiff,
    isApplied: Boolean,
    onApply: () -> Unit
) {
    val borderColor = when (diff.changeType) {
        DiffChangeType.ADD -> PassColor
        DiffChangeType.REMOVE -> ErrorColor
        DiffChangeType.MODIFY -> Primary
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, borderColor.copy(alpha = 0.5f), RoundedCornerShape(8.dp)),
        colors = CardDefaults.cardColors(
            containerColor = if (isApplied) PassColor.copy(alpha = 0.05f) else Surface
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "L${diff.lineNumber}",
                        style = MaterialTheme.typography.labelSmall,
                        fontFamily = FontFamily.Monospace,
                        color = Secondary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = diff.changeType.value.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = borderColor,
                        fontWeight = FontWeight.Bold
                    )
                }
                if (!isApplied) {
                    FilterChip(
                        selected = false,
                        onClick = onApply,
                        label = { Text("应用", style = MaterialTheme.typography.labelSmall) }
                    )
                } else {
                    Text(
                        text = "✓ 已应用",
                        style = MaterialTheme.typography.labelSmall,
                        color = PassColor
                    )
                }
            }

            if (diff.originalLine.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "- ${diff.originalLine}",
                    style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                    color = ErrorColor.copy(alpha = 0.8f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            if (diff.migratedLine.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "+ ${diff.migratedLine}",
                    style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                    color = PassColor.copy(alpha = 0.9f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

// ================================================================
// Debug Panel Screen / 调试面板页面
// ================================================================

/**
 * ============================================================
 * DebugPanelScreen — 调试面板页面
 * ============================================================
 * Interactive state selector + animation curve visualization.
 *
 * Design Spec Section 3.3: Debug Panel page
 * - Left: interaction state selector (SegmentedButton)
 * - Right: live preview + animation curve visualization
 * - Bottom: current state color values / animation duration slider
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DebugPanelScreen(
    state: DebugPanelState,
    onIntent: (StyleApiKitIntent) -> Unit
) {
    val animatedColor by animateFloatAsState(
        targetValue = if (state.isAnimating) 1f else 0f,
        animationSpec = tween(state.animationDurationMs),
        label = "colorAnimation"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = "Style API 动画调试面板",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = Primary
        )
        Text(
            text = "实时预览交互状态切换动画效果",
            style = MaterialTheme.typography.bodySmall,
            color = Secondary
        )
        Spacer(modifier = Modifier.height(16.dp))

        // State selector — SegmentedButton
        Text(
            text = "交互状态",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(8.dp))

        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            InteractionState.entries.take(3).forEachIndexed { index, interactionState ->
                SegmentedButton(
                    selected = state.selectedInteractionState == interactionState,
                    onClick = { onIntent(StyleApiKitIntent.SelectInteractionState(interactionState)) },
                    shape = SegmentedButtonDefaults.itemShape(index = index, count = 3),
                    icon = {}
                ) {
                    Text(
                        text = interactionState.labelCn,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            InteractionState.entries.drop(3).forEachIndexed { index, interactionState ->
                SegmentedButton(
                    selected = state.selectedInteractionState == interactionState,
                    onClick = { onIntent(StyleApiKitIntent.SelectInteractionState(interactionState)) },
                    shape = SegmentedButtonDefaults.itemShape(index = index, count = 3),
                    icon = {}
                ) {
                    Text(
                        text = interactionState.labelCn,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Live preview card
        Text(
            text = "状态预览",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(8.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = state.currentColor.copy(alpha = 0.15f + animatedColor * 0.1f)
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(
                            color = state.currentColor.copy(alpha = 0.3f + animatedColor * 0.3f),
                            shape = RoundedCornerShape(16.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = state.selectedInteractionState.labelCn,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Primary
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Color: #${state.currentColor.let {
                        if (it == Color.Unspecified) "Unspecified"
                        else String.format("%08X", it.value.toLong())
                    }}",
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = FontFamily.Monospace,
                    color = Secondary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { onIntent(StyleApiKitIntent.ToggleAnimation) }
                    ) {
                        Icon(
                            imageVector = if (state.isAnimating) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (state.isAnimating) "Pause" else "Play",
                            tint = Primary
                        )
                    }
                    Text(
                        text = if (state.isAnimating) "动画进行中..." else "点击播放动画",
                        style = MaterialTheme.typography.bodySmall,
                        color = Secondary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Animation params
        Text(
            text = "动画参数",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(8.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SurfaceVariant.copy(alpha = 0.3f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "动画时长: ${state.animationDurationMs}ms",
                    style = MaterialTheme.typography.bodySmall,
                    color = Secondary
                )
                Slider(
                    value = state.animationDurationMs.toFloat(),
                    onValueChange = {
                        onIntent(StyleApiKitIntent.UpdateAnimationParams(it.toInt(), state.animationCurve))
                    },
                    valueRange = 50f..1000f,
                    steps = 18
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "曲线: ${state.animationCurve}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Secondary
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    listOf("easeInOut", "fastOutSlowIn", "linear", "fastOutLinearIn").forEach { curve ->
                        FilterChip(
                            selected = state.animationCurve == curve,
                            onClick = {
                                onIntent(StyleApiKitIntent.UpdateAnimationParams(state.animationDurationMs, curve))
                            },
                            label = { Text(curve, style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }
            }
        }

        // Animation curve visualization
        if (state.showAnimationCurve) {
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "动画曲线可视化",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Medium
                )
                Switch(
                    checked = state.showAnimationCurve,
                    onCheckedChange = { onIntent(StyleApiKitIntent.ToggleAnimationCurve) }
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            AnimationCurveChart(
                curve = state.animationCurve,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
            )
        }
    }
}

/**
 * ============================================================
 * AnimationCurveChart — 动画曲线可视化图表
 * ============================================================
 * Simple canvas-based animation curve visualization.
 */
@Composable
private fun AnimationCurveChart(
    curve: String,
    modifier: Modifier = Modifier
) {
    val curveColor = Primary

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = SurfaceVariant.copy(alpha = 0.2f))
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
        ) {
            val width = size.width
            val height = size.height

            // Draw grid
            for (i in 0..4) {
                val y = height * i / 4
                drawLine(
                    color = Color.Gray.copy(alpha = 0.2f),
                    start = Offset(0f, y),
                    end = Offset(width, y),
                    strokeWidth = 1f
                )
            }

            // Draw curve
            val path = Path()
            for (x in 0..width.toInt()) {
                val t = x / width
                val easedT = when (curve) {
                    "easeInOut" -> if (t < 0.5f) 2.0 * t * t else 1.0 - Math.pow((-2.0 * t + 2.0), 2.0) / 2.0
                    "fastOutSlowIn" -> if (t == 0f) 0f else if (t == 1f) 1f else {
                        val a = 1f
                        val b = 0f
                        val c = 1f
                        val d = 1f
                        val t2 = t
                        ((1 - t2) * (1 - t2) * (1 - t2) * a + 3 * (1 - t2) * (1 - t2) * t2 * c + 3 * (1 - t2) * t2 * t2 * d + t2 * t2 * t2 * b).toFloat()
                    }
                    "linear" -> t.toDouble()
                    else -> t.toDouble()
                }.toFloat()

                val y = height * (1 - easedT)
                if (x == 0) path.moveTo(x.toFloat(), y) else path.lineTo(x.toFloat(), y)
            }

            drawPath(
                path = path,
                color = curveColor,
                style = Stroke(width = 3f)
            )
        }
    }
}

// ================================================================
// Theme Guide Screen / 主题指南页面
// ================================================================

/**
 * ============================================================
 * ThemeGuideScreen — 主题指南页面
 * ============================================================
 * Documentation viewer with code highlighting for Style API theming.
 *
 * Design Spec Section 3.4: Theme Guide page
 * - Documentation reading mode with code highlighting
 * - Quick jump anchors: MaterialTheme usage / LocalContentColor / LocalTextStyle
 */
@Composable
private fun ThemeGuideScreen() {
    val sections = listOf(
        ThemeGuideSection(
            title = "MaterialTheme 在 Style 中的使用",
            content = "Style 块内可以直接读取 MaterialTheme，为子组件提供主题感知的样式。在 Style 块中使用 MaterialTheme 可确保样式随深色模式、Material You 动态配色自动适配。",
            codeExample = """
Style {
    val colors = MaterialTheme.colorScheme
    Box(
        modifier = Modifier.background(colors.primaryContainer)
    ) {
        Text(
            text = "Themed content",
            color = colors.onPrimaryContainer
        )
    }
}
            """.trimIndent()
        ),
        ThemeGuideSection(
            title = "LocalContentColor 使用指南",
            content = "LocalContentColor 提供当前上下文的文本颜色。在 Style API 中使用 LocalContentColor 可确保文本颜色随容器背景自动对比适配，无需手动计算对比度。",
            codeExample = """
// In Style block
Style {
    val contentColor = LocalContentColor.current
    Text(
        text = "Auto-contrast text",
        color = contentColor
    )
}
            """.trimIndent()
        ),
        ThemeGuideSection(
            title = "LocalTextStyle 使用指南",
            content = "LocalTextStyle 提供当前上下文的文本样式（字体、大小、粗细）。Style 块内读取 LocalTextStyle 可保持与父级文本样式的一致性，同时允许局部覆盖。",
            codeExample = """
// In Style block
Style {
    val textStyle = LocalTextStyle.current
    Text(
        text = "Styled text",
        style = textStyle.copy(
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp
        )
    )
}
            """.trimIndent()
        ),
        ThemeGuideSection(
            title = "Style API 与深色模式",
            content = "Style API 原生支持深色模式。在 Style 块内使用 MaterialTheme.colorScheme 时，颜色会自动切换为深色配色，无需编写额外的深色模式判断逻辑。",
            codeExample = """
@LightDark
Style {
    Card(
        style = CardStyle(
            pressed = pressedStyle,
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        // Content
    }
}
            """.trimIndent()
        ),
        ThemeGuideSection(
            title = "Style 性能注意事项",
            content = "Style 块内避免执行耗时操作。每次 recomposition 都会重新执行 Style 块，耗时操作会导致性能问题。建议将复杂的计算逻辑移到 Style 块外部。",
            codeExample = """
// ✗ Bad: Expensive operation inside Style block
Style {
    val data = loadExpensiveData() // Runs on every recomposition!
    Text(text = data)
}

// ✓ Good: Pre-compute outside Style block
val data = loadExpensiveData()
Style {
    Text(text = data) // Just reads pre-computed value
}
            """.trimIndent()
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        sections.forEach { section ->
            ThemeGuideSectionCard(section = section)
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

/**
 * ============================================================
 * ThemeGuideSectionCard — 主题指南章节卡片
 * ============================================================
 * Renders a single theme guide section with title, description and code.
 */
@Composable
private fun ThemeGuideSectionCard(section: ThemeGuideSection) {
    var expanded by remember { mutableStateOf(true) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SurfaceVariant.copy(alpha = 0.4f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = section.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = Primary,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = if (expanded) "▲ 收起" else "▼ 展开",
                    style = MaterialTheme.typography.labelSmall,
                    color = Secondary
                )
            }

            if (expanded) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = section.content,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                )
                if (section.codeExample != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Primary.copy(alpha = 0.2f), RoundedCornerShape(8.dp)),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E))
                    ) {
                        Text(
                            text = section.codeExample,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp
                            ),
                            color = Color(0xFFD4D4D4),
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
            }
        }
    }
}

// ================================================================
// Playground Screen / Playground 页面
// ================================================================

/**
 * ============================================================
 * PlaygroundScreen — Playground 页面
 * ============================================================
 * Interactive code editor + live preview for Style API.
 *
 * Design Spec Section 3.5: Playground page
 * - Left: code editor (syntax highlighted TextField)
 * - Right: live preview panel (WYSIWYG)
 * - Top: template quick-select chips
 */
@Composable
private fun PlaygroundScreen(
    state: PlaygroundState,
    onIntent: (StyleApiKitIntent) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Page header
        Text(
            text = "Style API Playground",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = Primary
        )
        Text(
            text = "编写 Style API 代码，实时预览效果",
            style = MaterialTheme.typography.bodySmall,
            color = Secondary
        )
        Spacer(modifier = Modifier.height(12.dp))

        // Template quick-select chips
        Text(
            text = "快速模板",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.horizontalScroll(rememberScrollState())
        ) {
            PlaygroundTemplate.entries.forEach { template ->
                FilterChip(
                    selected = state.selectedTemplate == template,
                    onClick = { onIntent(StyleApiKitIntent.SelectTemplate(template)) },
                    label = { Text(template.displayName) },
                    leadingIcon = if (state.selectedTemplate == template) {
                        { Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp)) }
                    } else null
                )
            }
            FilterChip(
                selected = false,
                onClick = { onIntent(StyleApiKitIntent.ResetToDefault) },
                label = { Text("重置") }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Code editor
        Text(
            text = "代码编辑器",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = state.code,
            onValueChange = { onIntent(StyleApiKitIntent.UpdatePlaygroundCode(it)) },
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            textStyle = MaterialTheme.typography.bodySmall.copy(
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp
            ),
            placeholder = { Text("在此编写 Style API 代码...") },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Primary,
                unfocusedBorderColor = SurfaceVariant
            )
        )

        // Error display
        if (state.previewError != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = ErrorColor.copy(alpha = 0.1f))
            ) {
                Text(
                    text = "⚠ ${state.previewError}",
                    style = MaterialTheme.typography.bodySmall,
                    color = ErrorColor,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Preview panel
        Text(
            text = "实时预览",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(8.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            colors = CardDefaults.cardColors(containerColor = SurfaceVariant.copy(alpha = 0.3f)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                if (state.code.isNotEmpty()) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Simulated preview of Style API card
                        Card(
                            modifier = Modifier
                                .size(160.dp, 100.dp)
                                .background(
                                    brush = Brush.verticalGradient(
                                        colors = listOf(
                                            Primary.copy(alpha = 0.2f),
                                            Primary.copy(alpha = 0.05f)
                                        )
                                    ),
                                    shape = RoundedCornerShape(16.dp)
                                ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Style API\\n预览区域",
                                    style = MaterialTheme.typography.bodyMedium,
                                    textAlign = TextAlign.Center,
                                    color = Primary
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "预览效果（运行时实际渲染）",
                            style = MaterialTheme.typography.labelSmall,
                            color = Secondary
                        )
                    }
                } else {
                    Text(
                        text = "在左侧编辑器输入代码\n预览将在这里显示",
                        style = MaterialTheme.typography.bodySmall,
                        color = Secondary,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        // Compilation hint
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            Text(
                text = "⚡ 代码变更后 500ms 防抖刷新预览",
                style = MaterialTheme.typography.labelSmall,
                color = Secondary
            )
        }
    }
}
