package com.mvi.kenny.feature.room3kmpmigration

// ================================================================
// Room3KmpMigrationScreen — Room 3.0 KMP 数据库迁移工具包 主屏幕
// ================================================================
// Main composable for Room 3.0 KMP Database Migration Toolkit.
//
// PRD-294: Room 3.0 KMP 数据库迁移工具包
// Design Reference: memory/agency/designs/PRD-294-Room-3.0-KMP数据库迁移工具包.md
//
// Layout:
//   ┌─────────────────────────────────────────┐
//   │           Tab Row (4 tabs)               │
//   │  [首页] [扫描] [迁移] [参考]            │
//   ├─────────────────────────────────────────┤
//   │                                         │
//   │         Tab Content (scrollable)         │
//   │                                         │
//   └─────────────────────────────────────────┘
//
// Each tab is a separate composable function:
//   HomeScreen     — Hero card, quick access, milestones
//   ScannerScreen  — Project path input, scan, results
//   MigrationScreen — 4-step wizard, code diffs
//   ReferenceScreen — Scenarios, API table, docs
// ================================================================

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.Article
import androidx.compose.material.icons.filled.BuildCircle
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Transform
import androidx.compose.material.icons.filled.Upgrade
import androidx.compose.material.icons.filled.Web
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.VerticalDivider
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mvi.kenny.base.TopBarConfig
import kotlinx.coroutines.flow.collectLatest

// ================================================================
// Tab Definitions
// ================================================================

private data class TabDef(
    val title: String,
    val icon: ImageVector
)

private val tabs = listOf(
    TabDef("首页", Icons.Default.BuildCircle),
    TabDef("扫描", Icons.Default.Search),
    TabDef("迁移", Icons.Default.Upgrade),
    TabDef("参考", Icons.Default.MenuBook)
)

// ================================================================
// Root Composable — Tab Container
// ================================================================

/**
 * ============================================================
 * Room3KmpMigrationScreen — 根屏幕组件
 * ============================================================
 * Container for the 4-tab Room 3.0 KMP Migration Toolkit.
 *
 * @param viewModel Room3KmpMigrationViewModel instance
 * @param onUpdateTopBar TopBar 配置回调
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Room3KmpMigrationScreen(
    viewModel: Room3KmpMigrationViewModel = viewModel(),
    onUpdateTopBar: (TopBarConfig) -> Unit
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Update TopBar when tab changes
    LaunchedEffect(state.currentTab) {
        onUpdateTopBar(
            TopBarConfig(
                title = "Room 3.0 KMP 迁移",
                actions = emptyList()
            )
        )
    }

    // Handle effects (snackbar, navigation, etc.)
    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is Room3KmpMigrationEffect.ShowError -> {
                    snackbarHostState.showSnackbar("错误: ${effect.message}")
                }
                is Room3KmpMigrationEffect.ShowSuccess -> {
                    snackbarHostState.showSnackbar(effect.message)
                }
                is Room3KmpMigrationEffect.Navigate -> {
                    // Internal navigation handled by tab switch
                    val tabIndex = when (effect.route) {
                        "home" -> 0; "scanner" -> 1; "migration" -> 2; "reference" -> 3
                        else -> 0
                    }
                    viewModel.sendIntent(Room3KmpMigrationIntent.SwitchTab(tabIndex))
                }
                is Room3KmpMigrationEffect.OpenUrl -> {
                    // URL opening would be handled by the host Activity
                }
                is Room3KmpMigrationEffect.ShareReport -> {
                    snackbarHostState.showSnackbar("报告已生成，可分享")
                }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Tab Row
            ScrollableTabRow(
                selectedTabIndex = state.currentTab,
                edgePadding = 16.dp
            ) {
                tabs.forEachIndexed { index, tab ->
                    Tab(
                        selected = state.currentTab == index,
                        onClick = {
                            viewModel.sendIntent(Room3KmpMigrationIntent.SwitchTab(index))
                        },
                        text = { Text(tab.title) },
                        icon = {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = tab.title
                            )
                        }
                    )
                }
            }

            // Tab Content
            when (state.currentTab) {
                0 -> HomeTabContent(
                    state = state.homeState,
                    onIntent = { viewModel.sendIntent(Room3KmpMigrationIntent.SendHomeIntent(it)) }
                )
                1 -> ScannerTabContent(
                    state = state.scannerState,
                    onIntent = { viewModel.sendIntent(Room3KmpMigrationIntent.SendScannerIntent(it)) }
                )
                2 -> MigrationTabContent(
                    state = state.migrationState,
                    onIntent = { viewModel.sendIntent(Room3KmpMigrationIntent.SendMigrationIntent(it)) }
                )
                3 -> ReferenceTabContent(
                    state = state.referenceState,
                    onIntent = { viewModel.sendIntent(Room3KmpMigrationIntent.SendReferenceIntent(it)) }
                )
            }
        }
    }
}

// ================================================================
// Tab 1: Home
// ================================================================

/**
 * ============================================================
 * HomeTabContent — 首页 Tab 内容
 * ============================================================
 * Hero card with progress ring + quick access cards + milestones.
 */
@Composable
private fun HomeTabContent(
    state: HomeState,
    onIntent: (HomeIntent) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Hero Card — 迁移进度环形图
        item {
            HeroCard(state = state)
        }

        // 快速入口
        item {
            Text(
                text = "快速入口",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
        items(state.quickAccessItems) { quickAccess ->
            QuickAccessCard(
                quickAccess = quickAccess,
                onClick = { onIntent(HomeIntent.QuickAccessClicked(quickAccess.route)) }
            )
        }

        // 最新扫描摘要
        state.lastScanSummary?.let { summary ->
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "最新扫描结果",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            item {
                ScanSummaryCard(summary = summary)
            }
        }

        // 迁移里程碑
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "迁移里程碑",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
        item {
            MigrationTimeline(completedSteps = state.completedSteps)
        }
    }
}

/**
 * Hero Card — 迁移进度环形图卡片
 */
@Composable
private fun HeroCard(state: HomeState) {
    val animatedProgress by animateFloatAsState(
        targetValue = state.migrationProgress,
        animationSpec = spring(dampingRatio = 0.7f),
        label = "progress"
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Circular progress indicator
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(80.dp)
            ) {
                CircularProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier.size(80.dp),
                    strokeWidth = 8.dp,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
                Text(
                    text = "${(animatedProgress * 100).toInt()}%",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.width(20.dp))

            Column {
                Text(
                    text = "Room 3.0 KMP 迁移进度",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${state.completedSteps} / ${state.totalSteps} 步骤完成",
                    style = MaterialTheme.typography.bodyMedium
                )
                if (state.completedSteps == state.totalSteps && state.totalSteps > 0) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "🎉 迁移已完成！",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF2E7D32)
                    )
                }
            }
        }
    }
}

/**
 * Quick Access Card — 快速入口卡片
 */
@Composable
private fun QuickAccessCard(
    quickAccess: QuickAccess,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = quickAccess.icon,
                contentDescription = quickAccess.title,
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = quickAccess.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = quickAccess.subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = "进入",
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

/**
 * Scan Summary Card — 扫描摘要卡片
 */
@Composable
private fun ScanSummaryCard(summary: ScanSummary) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                SummaryItem(
                    emoji = "🔴",
                    count = summary.totalBreakingChanges,
                    label = "断裂性"
                )
                SummaryItem(
                    emoji = "🟠",
                    count = summary.totalHighRiskChanges,
                    label = "高风险"
                )
                SummaryItem(
                    emoji = "🟡",
                    count = summary.totalSuggestedChanges,
                    label = "建议项"
                )
            }
        }
    }
}

@Composable
private fun SummaryItem(emoji: String, count: Int, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = emoji, fontSize = 24.sp)
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall
        )
    }
}

/**
 * Migration Timeline — 迁移里程碑时间线
 */
@Composable
private fun MigrationTimeline(completedSteps: Int) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            MigrationStep.entries.forEachIndexed { index, step ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 6.dp)
                ) {
                    val isCompleted = index < completedSteps
                    val isCurrent = index == completedSteps

                    Icon(
                        imageVector = if (isCompleted) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                        contentDescription = step.displayName,
                        tint = if (isCompleted) Color(0xFF2E7D32)
                        else if (isCurrent) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.outline,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = step.displayName,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal
                        )
                        Text(
                            text = step.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                if (index < MigrationStep.entries.size - 1) {
                    HorizontalDivider(modifier = Modifier.padding(start = 36.dp, top = 4.dp, bottom = 4.dp))
                }
            }
        }
    }
}

// ================================================================
// Tab 2: Scanner
// ================================================================

/**
 * ============================================================
 * ScannerTabContent — 扫描 Tab 内容
 * ============================================================
 * Project path input + scan trigger + results display.
 */
@Composable
private fun ScannerTabContent(
    state: ScannerState,
    onIntent: (ScannerIntent) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // 项目路径输入
        OutlinedTextField(
            value = state.projectPath,
            onValueChange = { onIntent(ScannerIntent.UpdateProjectPath(it)) },
            label = { Text("项目路径 / Project Path") },
            placeholder = { Text("例如: /path/to/your/android/project") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        // 扫描按钮 + 进度
        if (state.scanPhase != ScanPhase.IDLE) {
            // Progress display during scan
            Column {
                Text(
                    text = "扫描中: ${state.scanPhase.displayName}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { state.progress },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        } else {
            // Scan button
            androidx.compose.material3.Button(
                onClick = { onIntent(ScannerIntent.StartScan) },
                modifier = Modifier.fillMaxWidth(),
                enabled = state.projectPath.isNotBlank()
            ) {
                Icon(Icons.Default.Search, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("开始扫描")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 扫描结果
        if (state.scanComplete) {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // 断裂性变更
                if (state.breakingChanges.isNotEmpty()) {
                    item {
                        Text(
                            text = "🔴 断裂性变更 (${state.breakingChanges.size})",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color(0xFFB3261E),
                            fontWeight = FontWeight.Bold
                        )
                    }
                    items(state.breakingChanges) { item ->
                        ChangeItemCard(item = item, onIntent = onIntent)
                    }
                }

                // 高风险变更
                if (state.highRiskChanges.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "🟠 高风险变更 (${state.highRiskChanges.size})",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color(0xFFF9A825),
                            fontWeight = FontWeight.Bold
                        )
                    }
                    items(state.highRiskChanges) { item ->
                        ChangeItemCard(item = item, onIntent = onIntent)
                    }
                }

                // 建议项
                if (state.suggestedChanges.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "🟡 建议项 (${state.suggestedChanges.size})",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color(0xFFFFD93D),
                            fontWeight = FontWeight.Bold
                        )
                    }
                    items(state.suggestedChanges) { item ->
                        ChangeItemCard(item = item, onIntent = onIntent)
                    }
                }

                // 导出报告按钮
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    androidx.compose.material3.OutlinedButton(
                        onClick = { onIntent(ScannerIntent.ExportReport) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Article, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("导出 Markdown 报告")
                    }
                }
            }
        } else {
            // Empty state
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.outline
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "输入项目路径后开始扫描",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        }
    }
}

/**
 * ChangeItemCard — 变更项卡片
 */
@Composable
private fun ChangeItemCard(
    item: ChangeItem,
    onIntent: (ScannerIntent) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded },
        colors = CardDefaults.cardColors(
            containerColor = item.severity.color.copy(alpha = 0.1f)
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = item.severity.emoji,
                    fontSize = 20.sp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = item.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = item.filePath,
                        style = MaterialTheme.typography.labelSmall,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (expanded) "收起" else "展开"
                )
            }

            // Expanded: show code diff
            if (expanded && (item.before != null || item.after != null)) {
                Spacer(modifier = Modifier.height(8.dp))
                CodeDiffBlock(before = item.before, after = item.after)
            }
        }
    }
}

/**
 * CodeDiffBlock — 代码 Diff 双栏展示
 */
@Composable
private fun CodeDiffBlock(before: String?, after: String?) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFFF5F5F5))
            .padding(8.dp)
    ) {
        // Before (left, red)
        if (before != null) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "变更前",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFFB3261E)
                )
                Text(
                    text = before,
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = FontFamily.Monospace,
                    color = Color(0xFFB3261E)
                )
            }
            VerticalDivider(modifier = Modifier.padding(horizontal = 8.dp))
        }
        // After (right, green)
        if (after != null) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "变更后",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF2E7D32)
                )
                Text(
                    text = after,
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = FontFamily.Monospace,
                    color = Color(0xFF2E7D32)
                )
            }
        }
    }
}

// ================================================================
// Tab 3: Migration
// ================================================================

/**
 * ============================================================
 * MigrationTabContent — 迁移 Tab 内容
 * ============================================================
 * 4-step wizard + KMP module type selector + code diff viewer.
 */
@Composable
private fun MigrationTabContent(
    state: MigrationState,
    onIntent: (MigrationIntent) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // KMP 模块类型选择器
        Text(
            text = "KMP 模块类型",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        KmpModuleTypeSelector(
            selected = state.kmpModuleType,
            onSelect = { onIntent(MigrationIntent.SelectKmpModuleType(it)) }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 步骤向导
        Text(
            text = "迁移步骤",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(state.availableSteps) { step ->
                MigrationStepCard(
                    step = step,
                    isCompleted = state.completedSteps.contains(step),
                    isCurrent = state.currentStep == step,
                    onStart = { onIntent(MigrationIntent.StartStep(step)) },
                    onComplete = { onIntent(MigrationIntent.CompleteStep(step)) }
                )
            }

            // 生成报告按钮
            item {
                Spacer(modifier = Modifier.height(8.dp))
                androidx.compose.material3.Button(
                    onClick = { onIntent(MigrationIntent.GenerateMigrationReport) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Article, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("生成迁移报告")
                }
            }
        }
    }
}

/**
 * KMP Module Type Selector — KMP 模块类型选择器
 */
@Composable
private fun KmpModuleTypeSelector(
    selected: KmpModuleType,
    onSelect: (KmpModuleType) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        KmpModuleType.entries.forEach { type ->
            val isSelected = type == selected
            Card(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onSelect(type) },
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected)
                        MaterialTheme.colorScheme.primaryContainer
                    else
                        MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (type == KmpModuleType.SHARED)
                                Icons.Default.AccountTree else Icons.Default.Storage,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = type.displayName,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = type.description,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}

/**
 * MigrationStepCard — 迁移步骤卡片
 */
@Composable
private fun MigrationStepCard(
    step: MigrationStep,
    isCompleted: Boolean,
    isCurrent: Boolean,
    onStart: () -> Unit,
    onComplete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when {
                isCompleted -> Color(0xFF2E7D32).copy(alpha = 0.1f)
                isCurrent -> MaterialTheme.colorScheme.primaryContainer
                else -> MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (isCompleted) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                contentDescription = step.displayName,
                tint = if (isCompleted) Color(0xFF2E7D32) else MaterialTheme.colorScheme.outline,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = step.displayName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal
                )
                Text(
                    text = step.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (!isCompleted) {
                if (isCurrent) {
                    androidx.compose.material3.Button(onClick = onComplete) {
                        Text("完成")
                    }
                } else {
                    TextButton(onClick = onStart) {
                        Text("开始")
                    }
                }
            }
        }
    }
}

// ================================================================
// Tab 4: Reference
// ================================================================

/**
 * ============================================================
 * ReferenceTabContent — 参考 Tab 内容
 * ============================================================
 * Scenario cards + API change table + official doc link.
 */
@Composable
private fun ReferenceTabContent(
    state: ReferenceState,
    onIntent: (ReferenceIntent) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // 场景卡片
        Text(
            text = "预置场景",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(state.scenarios) { scenario ->
                ScenarioCard(
                    scenario = scenario,
                    onClick = { onIntent(ReferenceIntent.ScenarioClicked(scenario)) }
                )
            }

            // 官方文档入口
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "官方文档",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onIntent(ReferenceIntent.OpenOfficialDoc) },
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Article,
                            contentDescription = null,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Room 3.0 官方文档",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "developer.android.com/jetpack/androidx/releases/room3",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }

            // API 变更速查表
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "API 变更速查表",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            items(state.apiChanges) { apiChange ->
                ApiChangeCard(apiChange = apiChange)
            }
        }
    }
}

/**
 * Scenario Card — 预置场景卡片
 */
@Composable
private fun ScenarioCard(
    scenario: Scenario,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = scenario.icon,
                contentDescription = scenario.title,
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = scenario.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = scenario.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                Icons.Default.PlayArrow,
                contentDescription = "进入",
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

/**
 * ApiChangeCard — API 变更卡片
 */
@Composable
private fun ApiChangeCard(apiChange: ApiChange) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = apiChange.apiName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (expanded) "收起" else "展开"
                )
            }

            if (expanded) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "2.x 用法:",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFFB3261E)
                )
                Text(
                    text = apiChange.v2Usage,
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = FontFamily.Monospace
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "3.0 用法:",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF2E7D32)
                )
                Text(
                    text = apiChange.v3Usage,
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = FontFamily.Monospace
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "备注: ${apiChange.notes}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
