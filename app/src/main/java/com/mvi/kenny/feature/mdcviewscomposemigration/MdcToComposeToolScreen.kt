package com.mvi.kenny.feature.mdcviewscomposemigration

// ================================================================
// MdcToComposeToolScreen — MDC-Android Views → Compose 迁移工具包主界面
// ================================================================
// Main screen for MDC-Android Views → Compose Migration Toolkit.
//
// PRD-283: Android MDC-Views → Compose 迁移工具包
// Design Reference: memory/agency/designs/PRD-283-Android-MDC-Views-Compose迁移工具包.md
//
// Architecture:
//   - 4-Tab Navigation: Scanner / Migration / Report / Reference
//   - Material 3 NavigationBar for Tab switching
//   - MVI pattern: Contract → ViewModel → Screen (this file)
//
// Tabs:
//   1. ScannerScreen — Scan MDC Views project, evaluate migration complexity
//   2. MigrationScreen — Step-by-step guided migration workflow
//   3. ReportScreen — Migration progress report with export
//   4. ReferenceScreen — Searchable MDC → Compose component mapping table
// ================================================================

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
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
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

// ================================================================
// MdcToComposeToolScreen — 主入口 Composable
// ================================================================

/**
 * MdcToComposeToolScreen — 迁移工具包主界面入口
 *
 * Top-level composable that sets up MVI state collection
 * and routes to the appropriate Tab screen based on selectedTab.
 *
 * @param viewModel ViewModel instance (default: created by viewModel())
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MdcToComposeToolScreen(
    viewModel: MdcToComposeToolViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // ── Handle Effects ──────────────────────────────────────────
    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is MdcToComposeToolEffect.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(effect.message)
                }
                is MdcToComposeToolEffect.CopyToClipboard -> {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    clipboard.setPrimaryClip(ClipData.newPlainText(effect.label, effect.content))
                }
                is MdcToComposeToolEffect.ShareReport -> {
                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, effect.content)
                        putExtra(Intent.EXTRA_SUBJECT, "MDC → Compose Migration Report")
                    }
                    context.startActivity(Intent.createChooser(shareIntent, "Export Report"))
                }
                is MdcToComposeToolEffect.ShowError -> {
                    snackbarHostState.showSnackbar("❌ ${effect.message}")
                }
            }
        }
    }

    // ── Tab-based content rendering ────────────────────────────
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "MDC Views → Compose 迁移工具包",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "Material Views 进入维护模式 · Google I/O 2026",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.sendIntent(MdcToComposeToolIntent.StartScan("")) }) {
                        Icon(Icons.Default.Refresh, contentDescription = "重新扫描")
                    }
                }
            )
        },
        bottomBar = {
            MdcNavigationBar(
                selectedTab = state.selectedTab,
                onTabSelect = { tab ->
                    viewModel.sendIntent(MdcToComposeToolIntent.SelectTab(tab))
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (state.selectedTab) {
                MdcTab.SCANNER -> ScannerScreen(
                    state = state.scannerState,
                    onIntent = viewModel::sendIntent
                )
                MdcTab.MIGRATION -> MigrationScreen(
                    state = state.migrationState,
                    onIntent = viewModel::sendIntent
                )
                MdcTab.REPORT -> ReportScreen(
                    state = state.reportState,
                    migrationState = state.migrationState,
                    scanResult = state.scannerState.scanResult,
                    onIntent = viewModel::sendIntent
                )
                MdcTab.REFERENCE -> ReferenceScreen(
                    state = state.referenceState,
                    onIntent = viewModel::sendIntent
                )
            }
        }
    }
}

// ================================================================
// MdcNavigationBar — 底部导航栏
// ================================================================

/**
 * MdcNavigationBar — Bottom navigation bar for the 4 tabs.
 *
 * @param selectedTab Currently selected tab
 * @param onTabSelect Callback when a tab is selected
 */
@Composable
private fun MdcNavigationBar(
    selectedTab: MdcTab,
    onTabSelect: (MdcTab) -> Unit
) {
    NavigationBar {
        MdcTab.entries.forEach { tab ->
            NavigationBarItem(
                icon = {
                    Text(
                        text = tab.emoji,
                        fontSize = 20.sp
                    )
                },
                label = { Text(tab.label) },
                selected = selectedTab == tab,
                onClick = { onTabSelect(tab) }
            )
        }
    }
}

// ================================================================
// ScannerScreen — 扫描 Tab
// ================================================================

/**
 * ScannerScreen — Scan MDC Views project and display analysis results.
 *
 * Features:
 *   - Project path input
 *   - Scan trigger with progress animation
 *   - Scan result: component stats, complexity score, recommendations
 *
 * @param state Current scanner tab state
 * @param onIntent Intent sender for user actions
 */
@Composable
private fun ScannerScreen(
    state: ScannerTabState,
    onIntent: (MdcToComposeToolIntent) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // ── Path Input Card ───────────────────────────────────────
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "📁 项目路径",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = state.projectPath,
                    onValueChange = { onIntent(MdcToComposeToolIntent.UpdateProjectPath(it)) },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("输入 Android 项目根目录路径") },
                    leadingIcon = { Icon(Icons.Default.Folder, contentDescription = null) },
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = { onIntent(MdcToComposeToolIntent.StartScan(state.projectPath)) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = state.scanState != ScanState.RUNNING
                ) {
                    if (state.scanState == ScanState.RUNNING) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("扫描中... ${(state.scanProgress * 100).toInt()}%")
                    } else {
                        Icon(Icons.Default.Search, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("开始扫描")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ── Scan Progress ────────────────────────────────────────
        if (state.scanState == ScanState.RUNNING) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("🔍 正在分析项目...", style = MaterialTheme.typography.titleSmall)
                    Spacer(modifier = Modifier.height(8.dp))
                    val animatedProgress by animateFloatAsState(
                        targetValue = state.scanProgress,
                        animationSpec = tween(300),
                        label = "scanProgress"
                    )
                    LinearProgressIndicator(
                        progress = { animatedProgress },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = when {
                            state.scanProgress < 0.3f -> "解析 Gradle 配置..."
                            state.scanProgress < 0.6f -> "扫描 XML Layout 文件..."
                            state.scanProgress < 0.9f -> "分析组件依赖..."
                            else -> "生成报告..."
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // ── Scan Result ───────────────────────────────────────────
        state.scanResult?.let { result ->
            ScanResultCard(result = result)
        }
    }
}

/**
 * ScanResultCard — Displays the scan analysis result.
 *
 * @param result Scan result data
 */
@Composable
private fun ScanResultCard(result: ScanResult) {
    // ── Summary Card ────────────────────────────────────────────
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("📊 扫描摘要", style = MaterialTheme.typography.titleMedium)
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "v${result.mdcVersion}",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(label = "模块", value = result.moduleCount.toString())
                StatItem(label = "XML文件", value = result.xmlFileCount.toString())
                StatItem(label = "总组件", value = result.componentStats.sumOf { it.count }.toString())
            }

            Spacer(modifier = Modifier.height(12.dp))
            ComplexityRating(score = result.complexityScore)

            if (result.navigationMigrationNeeded || result.themeMigrationNeeded) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (result.navigationMigrationNeeded) {
                        Surface(
                            color = MaterialTheme.colorScheme.errorContainer,
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "⚠️ Navigation 2→3 需迁移",
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                    if (result.themeMigrationNeeded) {
                        Surface(
                            color = MaterialTheme.colorScheme.errorContainer,
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "⚠️ Theme 需迁移",
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                }
            }
        }
    }

    Spacer(modifier = Modifier.height(16.dp))

    // ── Component Stats ──────────────────────────────────────────
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("📈 组件统计", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(12.dp))
            result.componentStats.take(8).forEach { stat ->
                ComponentStatRow(
                    stat = stat,
                    maxCount = result.componentStats.maxOfOrNull { it.count } ?: 1
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }

    Spacer(modifier = Modifier.height(16.dp))

    // ── Priority Recommendations ────────────────────────────────
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("🎯 优先级迁移建议", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(12.dp))
            result.priorityRecommendations.forEach { rec ->
                RecommendationCard(rec = rec)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

// ================================================================
// MigrationScreen — 迁移 Tab
// ================================================================

/**
 * MigrationScreen — Step-by-step migration workflow.
 *
 * Features:
 *   - Ordered migration steps with priority labels
 *   - Batch mode toggle for auto-completing LOW-effort steps
 *   - Step detail bottom sheet with before/after code
 *
 * @param state Current migration tab state
 * @param onIntent Intent sender for user actions
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MigrationScreen(
    state: MigrationTabState,
    onIntent: (MdcToComposeToolIntent) -> Unit
) {
    val sheetState = rememberModalBottomSheetState()
    var showBottomSheet by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // ── Batch Mode Toggle ────────────────────────────────────
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = if (state.batchModeEnabled)
                    MaterialTheme.colorScheme.primaryContainer
                else
                    MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "⚡ 批量模式",
                        style = MaterialTheme.typography.titleSmall
                    )
                    Text(
                        text = "开启后，低难度组件自动标记为完成",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = state.batchModeEnabled,
                    onCheckedChange = { onIntent(MdcToComposeToolIntent.ToggleBatchMode(it)) }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ── Progress Summary ─────────────────────────────────────
        val completedCount = state.migrationSteps.count { it.status == MigrationStepStatus.COMPLETED }
        val totalCount = state.migrationSteps.size

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "迁移进度",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = "$completedCount / $totalCount 步骤已完成",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        LinearProgressIndicator(
            progress = { if (totalCount > 0) completedCount.toFloat() / totalCount else 0f },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
        )

        Spacer(modifier = Modifier.height(16.dp))

        // ── Migration Steps ──────────────────────────────────────
        if (state.migrationSteps.isEmpty()) {
            EmptyMigrationState()
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(state.migrationSteps) { index, step ->
                    MigrationStepCard(
                        step = step,
                        onSelect = {
                            onIntent(MdcToComposeToolIntent.SelectMigrationStep(index))
                            showBottomSheet = true
                        },
                        onMarkComplete = {
                            onIntent(MdcToComposeToolIntent.MarkStepCompleted(index))
                        }
                    )
                }
            }
        }
    }

    // ── Step Detail Bottom Sheet ─────────────────────────────────
    if (showBottomSheet) {
        state.selectedStepDetail?.let { step ->
            ModalBottomSheet(
                onDismissRequest = { showBottomSheet = false },
                sheetState = sheetState
            ) {
                StepDetailSheet(
                    step = step,
                    onMarkComplete = {
                        onIntent(MdcToComposeToolIntent.MarkStepCompleted(step.index))
                        showBottomSheet = false
                    },
                    onDismiss = { showBottomSheet = false }
                )
            }
        }
    }
}

/**
 * EmptyMigrationState — Shown when no scan has been run yet.
 */
@Composable
private fun EmptyMigrationState() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "🔍", fontSize = 48.sp)
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "暂无迁移步骤",
            style = MaterialTheme.typography.titleMedium
        )
        Text(
            text = "请先在「扫描」Tab 中扫描项目",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * MigrationStepCard — Displays a single migration step card.
 */
@Composable
private fun MigrationStepCard(
    step: MigrationStep,
    onSelect: () -> Unit,
    onMarkComplete: () -> Unit
) {
    val isCompleted = step.status == MigrationStepStatus.COMPLETED

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onSelect),
        colors = CardDefaults.cardColors(
            containerColor = if (isCompleted)
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            else
                MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // ── Status Icon ──────────────────────────────────────
            Icon(
                imageVector = if (isCompleted) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                contentDescription = null,
                tint = if (isCompleted) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            // ── Content ──────────────────────────────────────────
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = step.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    PriorityChip(priority = step.priority)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = step.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "组件: ${step.componentType}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // ── Complete Button ──────────────────────────────────
            if (!isCompleted) {
                IconButton(onClick = onMarkComplete) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = "标记完成",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

/**
 * StepDetailSheet — Bottom sheet showing step detail with code diff.
 */
@Composable
private fun StepDetailSheet(
    step: MigrationStep,
    onMarkComplete: () -> Unit,
    onDismiss: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
            .padding(bottom = 32.dp)
    ) {
        // ── Header ───────────────────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = step.title,
                    style = MaterialTheme.typography.titleLarge
                )
                Text(
                    text = step.componentType,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            PriorityChip(priority = step.priority)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ── Description ──────────────────────────────────────────
        Text(
            text = step.description,
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(16.dp))

        // ── Before Code ─────────────────────────────────────────
        Text("❌ 迁移前（XML）", style = MaterialTheme.typography.labelLarge, color = Color(0xFFFF6B6B))
        Spacer(modifier = Modifier.height(8.dp))
        CodeBlock(code = step.beforeCode, isDark = true)

        Spacer(modifier = Modifier.height(16.dp))

        // ── After Code ──────────────────────────────────────────
        Text("✅ 迁移后（Compose）", style = MaterialTheme.typography.labelLarge, color = Color(0xFF4CAF50))
        Spacer(modifier = Modifier.height(8.dp))
        CodeBlock(code = step.afterCode, isDark = true)

        Spacer(modifier = Modifier.height(16.dp))

        // ── Notes ───────────────────────────────────────────────
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.tertiaryContainer
            )
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text("💡 迁移笔记", style = MaterialTheme.typography.titleSmall)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = step.notes,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ── Actions ──────────────────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TextButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                Text("关闭")
            }
            Button(
                onClick = onMarkComplete,
                modifier = Modifier.weight(1f),
                enabled = step.status != MigrationStepStatus.COMPLETED
            ) {
                Icon(Icons.Default.Check, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (step.status == MigrationStepStatus.COMPLETED) "已完成" else "标记完成")
            }
        }
    }
}

// ================================================================
// ReportScreen — 报告 Tab
// ================================================================

/**
 * ReportScreen — Migration progress report with export functionality.
 *
 * Features:
 *   - Hero card with completion percentage ring chart
 *   - Statistics: steps completed, components migrated
 *   - Export to Markdown / JSON
 *
 * @param state Report tab state
 * @param migrationState Migration tab state (for step data)
 * @param scanResult Scan result (for component data)
 * @param onIntent Intent sender for user actions
 */
@Composable
private fun ReportScreen(
    state: ReportTabState,
    migrationState: MigrationTabState,
    scanResult: ScanResult?,
    onIntent: (MdcToComposeToolIntent) -> Unit
) {
    val completedCount = migrationState.migrationSteps.count { it.status == MigrationStepStatus.COMPLETED }
    val totalCount = migrationState.migrationSteps.size
    val completionPct = if (totalCount > 0) (completedCount * 100 / totalCount) else 0

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // ── Hero Card — Completion Ring ───────────────────────────
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = "🏆", fontSize = 48.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "$completionPct%",
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "迁移完成度",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Visual progress bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(12.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(completionPct / 100f)
                            .height(12.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(
                                when {
                                    completionPct >= 80 -> Color(0xFF4CAF50)
                                    completionPct >= 50 -> Color(0xFFFFB347)
                                    else -> MaterialTheme.colorScheme.primary
                                }
                            )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ── Statistics Cards ─────────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard(
                modifier = Modifier.weight(1f),
                title = "已完成步骤",
                value = "$completedCount",
                subtitle = "/ $totalCount 步骤",
                color = Color(0xFF4CAF50)
            )
            StatCard(
                modifier = Modifier.weight(1f),
                title = "组件总数",
                value = "${scanResult?.componentStats?.sumOf { it.count } ?: 0}",
                subtitle = "待迁移",
                color = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ── Completed Steps List ─────────────────────────────────
        if (completedCount > 0) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "✅ 已完成步骤",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    migrationState.migrationSteps.filter { it.status == MigrationStepStatus.COMPLETED }
                        .forEach { step ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = Color(0xFF4CAF50),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = step.title,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // ── Export Actions ───────────────────────────────────────
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "📤 导出报告",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { onIntent(MdcToComposeToolIntent.ExportReport(ExportFormat.MARKDOWN)) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Description, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Markdown")
                    }
                    Button(
                        onClick = { onIntent(MdcToComposeToolIntent.ExportReport(ExportFormat.JSON)) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("JSON")
                    }
                }
            }
        }
    }
}

// ================================================================
// ReferenceScreen — 参考 Tab
// ================================================================

/**
 * ReferenceScreen — Searchable MDC Views → Compose component mapping table.
 *
 * Features:
 *   - Real-time search filtering
 *   - Component mapping cards with migration notes
 *   - Copy to clipboard functionality
 *
 * @param state Reference tab state
 * @param onIntent Intent sender for user actions
 */
@Composable
private fun ReferenceScreen(
    state: ReferenceTabState,
    onIntent: (MdcToComposeToolIntent) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // ── Search Bar ─────────────────────────────────────────
        OutlinedTextField(
            value = state.searchQuery,
            onValueChange = { onIntent(MdcToComposeToolIntent.SearchComponents(it)) },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("搜索组件名称...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            singleLine = true
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "共 ${state.filteredComponents.size} 个组件映射",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(8.dp))

        // ── Component Mapping List ───────────────────────────────
        val displayList = if (state.searchQuery.isBlank() && state.filteredComponents.isEmpty()) {
            SIMULATED_COMPONENT_MAPPINGS
        } else {
            state.filteredComponents
        }

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(displayList) { mapping ->
                ReferenceMappingCard(
                    mapping = mapping,
                    onCopy = { onIntent(MdcToComposeToolIntent.CopyMappingNote(mapping)) }
                )
            }
        }
    }
}

/**
 * ReferenceMappingCard — Displays a single component mapping entry.
 */
@Composable
private fun ReferenceMappingCard(
    mapping: ComponentMapping,
    onCopy: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${mapping.viewsName} → ${mapping.composeImport.substringAfterLast(".")}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                EffortChip(effort = mapping.effort)
            }

            Spacer(modifier = Modifier.height(8.dp))

            // ── Migration Note ─────────────────────────────────
            Text(
                text = mapping.migrationNote,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            // ── Import Paths ───────────────────────────────────
            Text(
                text = "❌ ${mapping.viewsImport}",
                style = MaterialTheme.typography.labelSmall,
                fontFamily = FontFamily.Monospace,
                color = Color(0xFFFF6B6B).copy(alpha = 0.8f)
            )
            Text(
                text = "✅ ${mapping.composeImport}",
                style = MaterialTheme.typography.labelSmall,
                fontFamily = FontFamily.Monospace,
                color = Color(0xFF4CAF50).copy(alpha = 0.8f)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // ── Copy Button ────────────────────────────────────
            TextButton(onClick = onCopy) {
                Icon(
                    Icons.Default.ContentCopy,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("复制迁移说明", style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

// ================================================================
// Shared UI Components
// ================================================================

/**
 * StatItem — Simple label + value statistic display.
 */
@Composable
private fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * ComplexityRating — Star-based complexity score display.
 *
 * @param score Complexity score 1-5
 */
@Composable
private fun ComplexityRating(score: Int) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text("复杂度:", style = MaterialTheme.typography.labelMedium)
        repeat(5) { index ->
            Text(
                text = if (index < score) "⭐" else "☆",
                fontSize = 16.sp
            )
        }
        Text(
            text = " ($score/5)",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * PriorityChip — Priority badge chip (P0/P1/P2).
 *
 * @param priority Priority level
 */
@Composable
private fun PriorityChip(priority: MdcPriority) {
    Surface(
        color = priority.color.copy(alpha = 0.15f),
        shape = RoundedCornerShape(4.dp)
    ) {
        Text(
            text = "${priority.emoji} ${priority.label}",
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            style = MaterialTheme.typography.labelSmall,
            color = priority.color
        )
    }
}

/**
 * EffortChip — Migration effort difficulty chip.
 *
 * @param effort Effort level
 */
@Composable
private fun EffortChip(effort: MigrateEffort) {
    Surface(
        color = effort.color.copy(alpha = 0.15f),
        shape = RoundedCornerShape(4.dp)
    ) {
        Text(
            text = "${effort.label}难度",
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            style = MaterialTheme.typography.labelSmall,
            color = effort.color
        )
    }
}

/**
 * CodeBlock — Syntax-highlighted code display block.
 *
 * @param code Code string to display
 * @param isDark Whether to use dark background
 */
@Composable
private fun CodeBlock(code: String, isDark: Boolean = false) {
    val bgColor = if (isDark) Color(0xFF1E1E1E) else Color(0xFFF5F5F5)
    val textColor = if (isDark) Color(0xFFD4D4D4) else Color(0xFF1E1E1E)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp)),
        color = bgColor,
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = code,
            modifier = Modifier.padding(12.dp),
            style = MaterialTheme.typography.bodySmall,
            fontFamily = FontFamily.Monospace,
            color = textColor
        )
    }
}

/**
 * StatCard — Statistics card with title, value, subtitle, and color.
 *
 * @param modifier Modifier
 * @param title Card title
 * @param value Large statistic value
 * @param subtitle Subtitle text
 * @param color Accent color
 */
@Composable
private fun StatCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    subtitle: String,
    color: Color
) {
    Card(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * RecommendationCard — Priority recommendation card.
 *
 * @param rec Migration recommendation
 */
@Composable
private fun RecommendationCard(rec: MigrationRecommendation) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = rec.priority.color.copy(alpha = 0.05f)
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(rec.priority.emoji, fontSize = 18.sp)
                Text(
                    rec.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    "⏱ ${rec.estimatedHours}h",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                rec.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "涉及: ${rec.targetComponents.joinToString(", ")}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

/**
 * ComponentStatRow — Component usage statistics bar row.
 *
 * @param stat Component stat
 * @param maxCount Maximum count for bar scaling
 */
@Composable
private fun ComponentStatRow(stat: ComponentStat, maxCount: Int) {
    val fraction = stat.count.toFloat() / maxCount.toFloat()
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(stat.componentName, style = MaterialTheme.typography.bodyMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    "${stat.count}x",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
                Surface(
                    color = stat.migrateEffort.color.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = stat.migrateEffort.label,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = stat.migrateEffort.color
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { fraction },
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp)),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
    }
}