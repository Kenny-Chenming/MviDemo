package com.mvi.kenny.feature.room3migration

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.CodeOff
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.DataObject
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.SyncAlt
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
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
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mvi.kenny.base.TopBarConfig
import com.mvi.kenny.base.TopBarAction
import kotlinx.coroutines.flow.collectLatest

/**
 * ============================================================
 * Room3MigrationScreen — Room 3.0 KMP 现代化迁移检测与自动化工具包主入口
 * ================================================================
 * Main entry point for the Room 3.0 KMP modernization migration toolkit.
 * Implements tabbed navigation with 9 tabs covering all migration aspects.
 *
 * @param onUpdateTopBar Callback to update the MainScreen TopAppBar title
 * @param viewModel ViewModel instance
 *
 * @see Room3MigrationState MVI state definition
 * @see Room3MigrationIntent MVI intent definition
 * @see MigrationTab Tab enumeration
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Room3MigrationScreen(
    onUpdateTopBar: (TopBarConfig) -> Unit,
    viewModel: Room3MigrationViewModel = viewModel()
) {
    // Observe MVI state / 观察 MVI 状态
    val state by viewModel.state.collectAsState()

    // Track selected tab index / 追踪选择的 Tab 索引
    var selectedTabIndex by remember { mutableIntStateOf(0) }

    // Tab definitions / Tab 定义
    val tabs = listOf(
        TabDef("仪表盘", Icons.Default.Dashboard, MigrationTab.DASHBOARD),
        TabDef("包迁移", Icons.Default.SyncAlt, MigrationTab.PACKAGE_MIGRATOR),
        TabDef("KSP切换", Icons.Default.Sync, MigrationTab.KSP_SWITCHER),
        TabDef("SQLite", Icons.Default.Storage, MigrationTab.SQLITE_DRIVER),
        TabDef("编译器", Icons.Default.Code, MigrationTab.KOTLIN_COMPILER),
        TabDef("Suspend", Icons.Default.Speed, MigrationTab.SUSPEND_API),
        TabDef("Flow订阅", Icons.Default.DataObject, MigrationTab.FLOW_INVALIDATION),
        TabDef("回归测试", Icons.Default.BugReport, MigrationTab.REGRESSION_TEST),
        TabDef("设置", Icons.Default.Settings, MigrationTab.SETTINGS)
    )

    // Map tab index to MigrationTab
    val currentTab = tabs.getOrNull(selectedTabIndex)?.tab ?: MigrationTab.DASHBOARD

    // Update TopAppBar when tab changes / Tab 切换时更新 TopAppBar
    LaunchedEffect(selectedTabIndex) {
        onUpdateTopBar(
            TopBarConfig(
                title = "Room 3.0 KMP 迁移工具",
                actions = listOf(
                    TopBarAction(
                        icon = Icons.Default.Refresh,
                        contentDescription = "刷新 / Refresh",
                        onClick = { viewModel.sendIntent(Room3MigrationIntent.RefreshScan) }
                    )
                )
            )
        )
    }

    // Collect effects for one-time events / 收集副作用
    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is Room3MigrationEffect.ShowToast -> {
                    // Toast handled by UI layer / Toast 由 UI 层处理
                }
                is Room3MigrationEffect.ShowError -> {
                    // Error handled by UI layer / 错误由 UI 层处理
                }
                else -> {}
            }
        }
    }

    // Rollback confirmation dialog / 回滚确认对话框
    if (state.showRollbackDialog) {
        RollbackConfirmDialog(
            onConfirm = { viewModel.sendIntent(Room3MigrationIntent.ConfirmRollback) },
            onDismiss = { viewModel.sendIntent(Room3MigrationIntent.CancelRollback) }
        )
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Tab Row / Tab 栏
        ScrollableTabRow(
            selectedTabIndex = selectedTabIndex,
            edgePadding = 12.dp,
            // Tab indicator color / Tab 指示条颜色
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary
        ) {
            tabs.forEachIndexed { index, tabDef ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(tabDef.title, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            // Show badge if module has issues / 如果模块有问题显示徽章
                            val badgeCount = when (tabDef.tab) {
                                MigrationTab.PACKAGE_MIGRATOR -> state.scanResults.packageIssues.size
                                MigrationTab.KSP_SWITCHER -> state.kaptConfigs.size
                                MigrationTab.SQLITE_DRIVER -> state.sqliteUsages.size
                                MigrationTab.KOTLIN_COMPILER -> state.kotlinCompilerIssues.size
                                MigrationTab.SUSPEND_API -> state.suspendApiIssues.size
                                MigrationTab.FLOW_INVALIDATION -> state.invalidationIssues.size
                                MigrationTab.REGRESSION_TEST -> state.generatedTests.size
                                else -> 0
                            }
                            if (badgeCount > 0) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Badge(
                                    containerColor = MaterialTheme.colorScheme.error,
                                    contentColor = Color.White,
                                    modifier = Modifier.size(16.dp)
                                ) {
                                    Text(badgeCount.toString(), fontSize = 9.sp)
                                }
                            }
                        }
                    },
                    icon = {
                        Icon(
                            imageVector = tabDef.icon,
                            contentDescription = tabDef.title,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                )
            }
        }

        // Tab Content / Tab 内容
        when (currentTab) {
            MigrationTab.DASHBOARD -> DashboardTab(state = state, viewModel = viewModel)
            MigrationTab.PACKAGE_MIGRATOR -> PackageMigratorTab(state = state, viewModel = viewModel)
            MigrationTab.KSP_SWITCHER -> KSPSwitcherTab(state = state, viewModel = viewModel)
            MigrationTab.SQLITE_DRIVER -> SQLiteDriverTab(state = state, viewModel = viewModel)
            MigrationTab.KOTLIN_COMPILER -> KotlinCompilerTab(state = state, viewModel = viewModel)
            MigrationTab.SUSPEND_API -> SuspendApiTab(state = state, viewModel = viewModel)
            MigrationTab.FLOW_INVALIDATION -> FlowInvalidationTab(state = state, viewModel = viewModel)
            MigrationTab.REGRESSION_TEST -> RegressionTestTab(state = state, viewModel = viewModel)
            MigrationTab.SETTINGS -> SettingsTab(state = state, viewModel = viewModel)
        }
    }
}

// ================================================================
// Tab Definition / Tab 定义
// ================================================================

/**
 * Tab definition for navigation / 导航 Tab 定义
 */
private data class TabDef(
    val title: String,
    val icon: ImageVector,
    val tab: MigrationTab
)

// ================================================================
// Dashboard Tab / 仪表盘 Tab
// ================================================================

/**
 * ============================================================
 * DashboardTab — 迁移概览仪表盘
 * ================================================================
 * Shows overall migration health score, radar chart, and module status cards.
 */
@Composable
private fun DashboardTab(
    state: Room3MigrationState,
    viewModel: Room3MigrationViewModel
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Scan button / 扫描按钮
        item {
            ScanButtonCard(
                isScanning = state.isScanning,
                onScan = { viewModel.sendIntent(Room3MigrationIntent.StartScan) }
            )
        }

        // Health Score Card / 健康度评分卡片
        item {
            HealthScoreCard(
                healthScore = state.healthScore,
                totalIssues = state.scanResults.totalIssues
            )
        }

        // Module Status Cards / 模块状态卡片
        item {
            Text(
                text = "模块状态 / Module Status",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }

        items(MigrationModule.entries.size) { index ->
            val module = MigrationModule.entries[index]
            val moduleState = state.moduleStates[module] ?: ModuleState.NOT_STARTED
            val radarIndex = index.coerceAtMost(state.radarAxisScores.size - 1)
            val radarScore = state.radarAxisScores.getOrElse(radarIndex) { 0f }
            ModuleStateCard(
                module = module,
                moduleState = moduleState,
                radarScore = radarScore
            )
        }
    }
}

/**
 * Scan button card with progress indicator.
 */
@Composable
private fun ScanButtonCard(
    isScanning: Boolean,
    onScan: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Room 3.0 迁移扫描",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "点击开始扫描项目 / Click to scan project",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
            }
            Button(
                onClick = onScan,
                enabled = !isScanning,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                if (isScanning) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("扫描中...")
                } else {
                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("开始扫描")
                }
            }
        }
    }
}

/**
 * Health score card with radar chart visualization.
 */
@Composable
private fun HealthScoreCard(
    healthScore: Float,
    totalIssues: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Radar chart / 雷达图
            Box(
                modifier = Modifier.size(120.dp),
                contentAlignment = Alignment.Center
            ) {
                RadarChartView(score = healthScore)
                // Center score display / 中心分数显示
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${(healthScore * 100).toInt()}",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "健康度",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Stats / 统计
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                StatRow("总问题数", totalIssues.toString())
                StatRow("健康度", "${(healthScore * 100).toInt()}%")
                val completed = (healthScore * 7).toInt()
                StatRow("已完成模块", "$completed/7")
            }
        }
    }
}

/**
 * Single stat row.
 */
@Composable
private fun StatRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold
        )
    }
}

/**
 * Module state card showing individual module status.
 */
@Composable
private fun ModuleStateCard(
    module: MigrationModule,
    moduleState: ModuleState,
    radarScore: Float
) {
    val (stateColor, stateText) = when (moduleState) {
        ModuleState.NOT_STARTED -> Color(0xFF9E9E9E) to "未开始"
        ModuleState.IN_PROGRESS -> Color(0xFF2196F3) to "进行中"
        ModuleState.COMPLETED -> Color(0xFF4CAF50) to "已完成"
        ModuleState.BLOCKED -> Color(0xFFFF5722) to "阻塞"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(stateColor)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = module.displayName,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = module.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Progress bar / 进度条
                LinearProgressIndicator(
                    progress = { radarScore },
                    modifier = Modifier
                        .width(60.dp)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = stateColor,
                    trackColor = stateColor.copy(alpha = 0.2f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stateText,
                    style = MaterialTheme.typography.labelSmall,
                    color = stateColor,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

/**
 * ============================================================
 * RadarChartView — 雷达图视图
 * ================================================================
 * Simple radar/spider chart for health score visualization.
 * Shows 7 axes representing different migration modules.
 */
@Composable
private fun RadarChartView(
    score: Float,
    modifier: Modifier = Modifier
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val fillColor = primaryColor.copy(alpha = 0.3f)

    androidx.compose.foundation.Canvas(modifier = modifier) {
        val centerX = size.width / 2
        val centerY = size.height / 2
        val radius = minOf(centerX, centerY) * 0.85f
        val sides = 7

        // Draw background polygon / 绘制背景多边形
        val bgPath = androidx.compose.ui.graphics.Path()
        for (i in 0 until sides) {
            val angle = (i * 360.0 / sides - 90) * (Math.PI / 180)
            val x = centerX + radius * kotlin.math.cos(angle).toFloat()
            val y = centerY + radius * kotlin.math.sin(angle).toFloat()
            if (i == 0) bgPath.moveTo(x, y) else bgPath.lineTo(x, y)
        }
        bgPath.close()
        drawPath(bgPath, fillColor, style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.dp.toPx()))

        // Draw score area / 绘制分数区域
        val fillRadius = radius * score
        val scorePath = androidx.compose.ui.graphics.Path()
        for (i in 0 until sides) {
            val angle = (i * 360.0 / sides - 90) * (Math.PI / 180)
            val x = centerX + fillRadius * kotlin.math.cos(angle).toFloat()
            val y = centerY + fillRadius * kotlin.math.sin(angle).toFloat()
            if (i == 0) scorePath.moveTo(x, y) else scorePath.lineTo(x, y)
        }
        scorePath.close()
        drawPath(scorePath, fillColor.copy(alpha = 0.5f))

        // Draw outline / 绘制轮廓
        drawCircle(
            color = primaryColor,
            radius = fillRadius,
            center = androidx.compose.ui.geometry.Offset(centerX, centerY),
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.dp.toPx())
        )

        // Draw axis lines / 绘制轴线
        for (i in 0 until sides) {
            val angle = (i * 360.0 / sides - 90) * (Math.PI / 180)
            val x = centerX + radius * kotlin.math.cos(angle).toFloat()
            val y = centerY + radius * kotlin.math.sin(angle).toFloat()
            drawLine(
                color = primaryColor.copy(alpha = 0.3f),
                start = androidx.compose.ui.geometry.Offset(centerX, centerY),
                end = androidx.compose.ui.geometry.Offset(x, y),
                strokeWidth = 1.dp.toPx()
            )
        }
    }
}

// ================================================================
// Package Migrator Tab / 包迁移 Tab
// ================================================================

/**
 * ============================================================
 * PackageMigratorTab — 包名批量替换引擎
 * ================================================================
 * Shows import replacements for androidx.room → androidx.room3 migration.
 */
@Composable
private fun PackageMigratorTab(
    state: Room3MigrationState,
    viewModel: Room3MigrationViewModel
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Header with select all / 全选头
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "包命名空间迁移",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "androidx.room → androidx.room3",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = state.allReplacementsSelected,
                        onCheckedChange = {
                            viewModel.sendIntent(Room3MigrationIntent.ToggleSelectAllReplacements)
                        }
                    )
                    Text("全选", style = MaterialTheme.typography.bodySmall)
                }
            }
        }

        if (state.pendingReplacements.isEmpty()) {
            item {
                EmptyStateCard(
                    icon = Icons.Default.SyncAlt,
                    title = "无待替换项",
                    subtitle = "扫描未发现需要迁移的包引用 / No packages need migration"
                )
            }
        } else {
            items(state.pendingReplacements) { replacement ->
                ReplacementCard(
                    replacement = replacement,
                    isSelected = replacement.id in state.selectedReplacements,
                    onToggle = {
                        viewModel.sendIntent(Room3MigrationIntent.ToggleReplacementSelection(replacement.id))
                    },
                    onExecute = {
                        viewModel.sendIntent(Room3MigrationIntent.ExecuteReplacement(replacement))
                    }
                )
            }

            // Execute button / 执行按钮
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = {
                        viewModel.sendIntent(Room3MigrationIntent.ExecuteSelectedReplacements)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = state.selectedReplacements.isNotEmpty() && !state.isLoading
                ) {
                    if (state.isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                    } else {
                        Icon(Icons.Default.AutoFixHigh, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("执行选中替换 (${state.selectedReplacements.size})")
                    }
                }
            }
        }
    }
}

/**
 * Single replacement card.
 */
@Composable
private fun ReplacementCard(
    replacement: ImportReplacement,
    isSelected: Boolean,
    onToggle: () -> Unit,
    onExecute: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (replacement.isExecuted)
                Color(0xFF4CAF50).copy(alpha = 0.1f)
            else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = isSelected,
                        onCheckedChange = { onToggle() },
                        enabled = !replacement.isExecuted
                    )
                    Column {
                        Text(
                            text = replacement.filePath,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "Line ${replacement.lineNumber}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }
                }
                if (replacement.isExecuted) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = "已执行",
                        tint = Color(0xFF4CAF50),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Code diff / 代码差异
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    Text(
                        text = replacement.oldImport,
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = FontFamily.Monospace,
                        color = Color(0xFFFF5722)
                    )
                    Text(
                        text = "↓",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = replacement.newImport,
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = FontFamily.Monospace,
                        color = Color(0xFF4CAF50)
                    )
                }
            }

            if (!replacement.isExecuted) {
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(
                    onClick = onExecute,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.AutoFixHigh, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("执行替换", fontSize = 12.sp)
                }
            }
        }
    }
}

// ================================================================
// KSP Switcher Tab / KSP 切换 Tab
// ================================================================

/**
 * ============================================================
 * KSPSwitcherTab — KAPT → KSP 配置迁移
 * ================================================================
 * Shows KAPT configurations and converts them to KSP.
 */
@Composable
private fun KSPSwitcherTab(
    state: Room3MigrationState,
    viewModel: Room3MigrationViewModel
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            InfoCard(
                icon = Icons.Default.Info,
                title = "KAPT → KSP 迁移",
                subtitle = "Room 3.0 强制使用 KSP，KAPT 已废弃 / Room 3.0 requires KSP, KAPT is deprecated"
            )
        }

        if (state.kaptConfigs.isEmpty()) {
            item {
                EmptyStateCard(
                    icon = Icons.Default.CheckCircle,
                    title = "无 KAPT 配置",
                    subtitle = "项目中未发现 KAPT 配置 / No KAPT configurations found"
                )
            }
        } else {
            items(state.kaptConfigs) { config ->
                KaptConfigCard(
                    config = config,
                    onConvert = {
                        viewModel.sendIntent(Room3MigrationIntent.ConvertKaptToKsp(setOf(config.id)))
                    }
                )
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = { viewModel.sendIntent(Room3MigrationIntent.ConvertAllKaptToKsp) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !state.isLoading
                ) {
                    Icon(Icons.Default.Sync, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("转换所有 KAPT → KSP")
                }
            }
        }
    }
}

/**
 * KAPT configuration card.
 */
@Composable
private fun KaptConfigCard(
    config: KaptConfig,
    onConvert: () -> Unit
) {
    val hasConflictColor = if (config.hasConflict) Color(0xFFFF5722) else Color.Unspecified

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = config.filePath,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "Line ${config.lineNumber}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
                if (config.hasConflict) {
                    Badge(containerColor = Color(0xFFFF5722)) {
                        Text("冲突", color = Color.White, fontSize = 10.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Original KAPT config / 原始 KAPT 配置
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFFF5722).copy(alpha = 0.1f)
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = config.configContent,
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.padding(8.dp),
                    color = Color(0xFFFF5722)
                )
            }

            if (config.suggestedKspConfig.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "↓ 建议替换为",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(4.dp))

                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF4CAF50).copy(alpha = 0.1f)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = config.suggestedKspConfig,
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(8.dp),
                        color = Color(0xFF4CAF50)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(
                onClick = onConvert,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("转换为 KSP", fontSize = 12.sp)
            }
        }
    }
}

// ================================================================
// SQLite Driver Tab / SQLite Driver Tab
// ================================================================

/**
 * ============================================================
 * SQLiteDriverTab — SupportSQLite → SQLiteDriver 迁移
 * ================================================================
 * Shows SupportSQLite usages and provides migration guidance.
 */
@Composable
private fun SQLiteDriverTab(
    state: Room3MigrationState,
    viewModel: Room3MigrationViewModel
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            InfoCard(
                icon = Icons.Default.Storage,
                title = "SQLite Driver 迁移",
                subtitle = "SupportSQLite API 已完全移除，必须使用 SQLiteDriver / SupportSQLite API removed, must use SQLiteDriver"
            )
        }

        if (state.sqliteUsages.isEmpty()) {
            item {
                EmptyStateCard(
                    icon = Icons.Default.CheckCircle,
                    title = "无 SQLite 使用",
                    subtitle = "项目中未发现 SupportSQLite 使用 / No SupportSQLite usages found"
                )
            }
        } else {
            items(state.sqliteUsages) { usage ->
                SqliteUsageCard(usage = usage)
            }
        }

        item {
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = { viewModel.sendIntent(Room3MigrationIntent.MigrateSqliteDriver) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.AutoAwesome, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("生成迁移指南")
            }
        }
    }
}

/**
 * SQLite usage card.
 */
@Composable
private fun SqliteUsageCard(usage: SqliteUsage) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = usage.filePath,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "Line ${usage.lineNumber}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = usage.content,
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.padding(8.dp)
                )
            }

            if (usage.needsWrapperDependency) {
                Spacer(modifier = Modifier.height(8.dp))
                Badge(containerColor = Color(0xFFFF5722)) {
                    Text("需添加 room3-sqlite-wrapper 依赖", color = Color.White, fontSize = 10.sp)
                }
            }
        }
    }
}

// ================================================================
// Kotlin Compiler Tab / Kotlin 编译器 Tab
// ================================================================

/**
 * ============================================================
 * KotlinCompilerTab — Kotlin 编译器合规检测
 * ================================================================
 * Shows Kotlin compiler compliance issues.
 */
@Composable
private fun KotlinCompilerTab(
    state: Room3MigrationState,
    viewModel: Room3MigrationViewModel
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            InfoCard(
                icon = Icons.Default.Code,
                title = "Kotlin 编译器合规检测",
                subtitle = "Room 3.0 要求 Kotlin 代码生成唯一，Kotlin 编译器强制要求 / Room 3.0 requires Kotlin-only codegen, Kotlin compiler is mandatory"
            )
        }

        if (state.kotlinCompilerIssues.isEmpty()) {
            item {
                EmptyStateCard(
                    icon = Icons.Default.CheckCircle,
                    title = "无合规问题",
                    subtitle = "Kotlin 编译器配置符合要求 / Kotlin compiler configuration is compliant"
                )
            }
        } else {
            items(state.kotlinCompilerIssues) { issue ->
                KotlinCompilerIssueCard(issue = issue)
            }
        }

        item {
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = { viewModel.sendIntent(Room3MigrationIntent.CheckKotlinCompiler) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Security, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("重新检测")
            }
        }
    }
}

/**
 * Kotlin compiler issue card.
 */
@Composable
private fun KotlinCompilerIssueCard(issue: KotlinCompilerIssue) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Badge(containerColor = MaterialTheme.colorScheme.error) {
                    Text(issue.issueType, color = Color.White, fontSize = 10.sp)
                }
                Text(
                    text = "Line ${issue.lineNumber}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = issue.filePath,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = issue.content,
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.padding(8.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.AutoFixHigh,
                    contentDescription = null,
                    tint = Color(0xFF4CAF50),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = issue.recommendation,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF4CAF50)
                )
            }
        }
    }
}

// ================================================================
// Suspend API Tab / Suspend API Tab
// ================================================================

/**
 * ============================================================
 * SuspendApiTab — 全协程化 API 迁移
 * ============================================================
 * Shows synchronous API usages that need migration to suspend functions.
 */
@Composable
private fun SuspendApiTab(
    state: Room3MigrationState,
    viewModel: Room3MigrationViewModel
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            InfoCard(
                icon = Icons.Default.Speed,
                title = "Suspend API 迁移",
                subtitle = "Room 3.0 全协程化，同步 DAO 方法需迁移为 suspend 函数 / Room 3.0 is fully coroutine-based, sync DAO methods must become suspend"
            )
        }

        if (state.suspendApiIssues.isEmpty()) {
            item {
                EmptyStateCard(
                    icon = Icons.Default.CheckCircle,
                    title = "无同步 API 问题",
                    subtitle = "所有 DAO 方法已符合协程化要求 / All DAO methods are already coroutine-compliant"
                )
            }
        } else {
            items(state.suspendApiIssues) { issue ->
                SuspendApiIssueCard(issue = issue)
            }
        }

        item {
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = { viewModel.sendIntent(Room3MigrationIntent.MigrateSuspendApi) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.AutoAwesome, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("生成迁移建议")
            }
        }
    }
}

/**
 * Suspend API issue card.
 */
@Composable
private fun SuspendApiIssueCard(issue: SuspendApiIssue) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = issue.filePath,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "Line ${issue.lineNumber}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Card(
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFFF5722).copy(alpha = 0.1f)
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = issue.content,
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.padding(8.dp),
                    color = Color(0xFFFF5722)
                )
            }

            if (issue.suggestedSuspendReplacement.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "↓ 建议替换为",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF4CAF50).copy(alpha = 0.1f)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = issue.suggestedSuspendReplacement,
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(8.dp),
                        color = Color(0xFF4CAF50)
                    )
                }
            }

            if (issue.callerCount > 0) {
                Spacer(modifier = Modifier.height(8.dp))
                Badge(containerColor = MaterialTheme.colorScheme.primary) {
                    Text("影响 ${issue.callerCount} 个调用点", color = Color.White, fontSize = 10.sp)
                }
            }
        }
    }
}

// ================================================================
// Flow Invalidation Tab / Flow 订阅 Tab
// ================================================================

/**
 * ============================================================
 * FlowInvalidationTab — InvalidationTracker → Flow API
 * ============================================================
 * Shows InvalidationTracker usages that need migration to Flow API.
 */
@Composable
private fun FlowInvalidationTab(
    state: Room3MigrationState,
    viewModel: Room3MigrationViewModel
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            InfoCard(
                icon = Icons.Default.DataObject,
                title = "Flow Invalidation 迁移",
                subtitle = "InvalidationTracker 改为 Flow API，invalidationFlow() 已移除 / InvalidationTracker replaced with Flow API, invalidationFlow() removed"
            )
        }

        if (state.invalidationIssues.isEmpty()) {
            item {
                EmptyStateCard(
                    icon = Icons.Default.CheckCircle,
                    title = "无 Invalidation 问题",
                    subtitle = "所有订阅已使用 Flow API / All subscriptions use Flow API"
                )
            }
        } else {
            items(state.invalidationIssues) { issue ->
                InvalidationIssueCard(issue = issue)
            }
        }

        item {
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = { viewModel.sendIntent(Room3MigrationIntent.MigrateFlowInvalidation) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.AutoAwesome, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("生成 Flow 迁移建议")
            }
        }
    }
}

/**
 * Invalidation issue card.
 */
@Composable
private fun InvalidationIssueCard(issue: InvalidationIssue) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = issue.filePath,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.weight(1f)
                )
                if (issue.isInDao) {
                    Badge(containerColor = MaterialTheme.colorScheme.primary) {
                        Text("DAO", color = Color.White, fontSize = 10.sp)
                    }
                }
            }
            Text(
                text = "Line ${issue.lineNumber}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )

            Spacer(modifier = Modifier.height(8.dp))
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFFF5722).copy(alpha = 0.1f)
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = issue.content,
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.padding(8.dp),
                    color = Color(0xFFFF5722)
                )
            }

            if (issue.suggestedFlowReplacement.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "↓ 建议替换为",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF4CAF50).copy(alpha = 0.1f)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = issue.suggestedFlowReplacement,
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(8.dp),
                        color = Color(0xFF4CAF50)
                    )
                }
            }
        }
    }
}

// ================================================================
// Regression Test Tab / 回归测试 Tab
// ================================================================

/**
 * ============================================================
 * RegressionTestTab — 回归测试用例生成
 * ============================================================
 * Shows test coverage and generated regression test cases.
 */
@Composable
private fun RegressionTestTab(
    state: Room3MigrationState,
    viewModel: Room3MigrationViewModel
) {
    val testCoverage = state.testCoverage

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Test coverage summary / 测试覆盖率摘要
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "测试覆盖率 / Test Coverage",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        StatColumn("总 DAO", testCoverage.totalDaos.toString())
                        StatColumn("已测试", testCoverage.testedDaos.toString())
                        StatColumn("未测试", testCoverage.untestedDaos.size.toString())
                        StatColumn("覆盖率", "${(testCoverage.coveragePercent * 100).toInt()}%")
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { testCoverage.coveragePercent },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                    )
                }
            }
        }

        // Untested DAOs / 未测试的 DAO
        if (testCoverage.untestedDaos.isNotEmpty()) {
            item {
                Text(
                    text = "待测试 DAO / Untested DAOs",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium
                )
            }
            items(testCoverage.untestedDaos) { daoName ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(daoName, style = MaterialTheme.typography.bodyMedium)
                        Badge(containerColor = Color(0xFFFF5722)) {
                            Text("待补充", color = Color.White, fontSize = 10.sp)
                        }
                    }
                }
            }
        }

        // Generated tests / 生成的测试
        if (state.generatedTests.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "生成的测试 / Generated Tests",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium
                )
            }
            items(state.generatedTests) { test ->
                GeneratedTestCard(
                    test = test,
                    onSave = {
                        viewModel.sendIntent(Room3MigrationIntent.SaveGeneratedTests(setOf(test.id)))
                    }
                )
            }
        }

        // Generate button / 生成按钮
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = { viewModel.sendIntent(Room3MigrationIntent.GenerateRegressionTests) },
                modifier = Modifier.fillMaxWidth(),
                enabled = !state.isLoading
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                } else {
                    Icon(Icons.Default.BugReport, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("生成回归测试用例")
                }
            }
        }
    }
}

/**
 * Stat column for coverage card.
 */
@Composable
private fun StatColumn(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
        )
    }
}

/**
 * Generated test card.
 */
@Composable
private fun GeneratedTestCard(
    test: GeneratedTest,
    onSave: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = test.daoName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium
                )
                if (test.isGenerated) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = "已保存",
                        tint = Color(0xFF4CAF50),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            Text(
                text = test.testFilePath,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = test.testContent,
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.padding(8.dp),
                    maxLines = 5,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (!test.isGenerated) {
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(
                    onClick = onSave,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Description, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("保存测试", fontSize = 12.sp)
                }
            }
        }
    }
}

// ================================================================
// Settings Tab / 设置 Tab
// ================================================================

/**
 * ============================================================
 * SettingsTab — 回滚/备份配置
 * ============================================================
 * Backup and rollback management panel.
 */
@Composable
private fun SettingsTab(
    state: Room3MigrationState,
    viewModel: Room3MigrationViewModel
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Backup section / 备份区
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Backup, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "备份管理 / Backup Management",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (state.backupInfo != null) {
                        val backup = state.backupInfo!!
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFF4CAF50).copy(alpha = 0.1f)
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF4CAF50), modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("备份已创建 / Backup Created", fontWeight = FontWeight.Medium, fontSize = 14.sp)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("路径: ${backup.backupPath}", style = MaterialTheme.typography.bodySmall, fontFamily = FontFamily.Monospace)
                                Text("时间: ${backup.createdAt}", style = MaterialTheme.typography.bodySmall)
                                Text("文件数: ${backup.fileCount}", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    } else {
                        Text(
                            text = "尚未创建备份 / No backup created",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = { viewModel.sendIntent(Room3MigrationIntent.CreateBackup) },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !state.isLoading
                    ) {
                        Icon(Icons.Default.Backup, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("创建备份 / Create Backup")
                    }
                }
            }
        }

        // Rollback section / 回滚区
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Restore, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "回滚管理 / Rollback Management",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "回滚将恢复所有迁移操作前的文件状态 / Rollback will restore all files to pre-migration state",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = { viewModel.sendIntent(Room3MigrationIntent.ShowRollbackDialog) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        ),
                        enabled = state.rollbackAvailable && !state.isLoading
                    ) {
                        Icon(Icons.Default.Restore, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("回滚到备份 / Rollback to Backup")
                    }

                    if (!state.rollbackAvailable) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "请先创建备份 / Please create a backup first",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }

        // Migration progress summary / 迁移进度摘要
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "迁移进度摘要 / Migration Progress Summary",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    MigrationModule.entries.forEach { module ->
                        val moduleState = state.moduleStates[module] ?: ModuleState.NOT_STARTED
                        val (stateColor, stateText) = when (moduleState) {
                            ModuleState.NOT_STARTED -> Color(0xFF9E9E9E) to "未开始"
                            ModuleState.IN_PROGRESS -> Color(0xFF2196F3) to "进行中"
                            ModuleState.COMPLETED -> Color(0xFF4CAF50) to "已完成"
                            ModuleState.BLOCKED -> Color(0xFFFF5722) to "阻塞"
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(module.displayName, style = MaterialTheme.typography.bodyMedium)
                            Text(stateText, style = MaterialTheme.typography.bodyMedium, color = stateColor, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }
        }
    }
}

// ================================================================
// Common Components / 通用组件
// ================================================================

/**
 * Info card with icon and message.
 */
@Composable
private fun InfoCard(
    icon: ImageVector,
    title: String,
    subtitle: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
            }
        }
    }
}

/**
 * Empty state card.
 */
@Composable
private fun EmptyStateCard(
    icon: ImageVector,
    title: String,
    subtitle: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color(0xFF4CAF50),
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}

/**
 * Rollback confirmation dialog.
 */
@Composable
private fun RollbackConfirmDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
        title = { Text("确认回滚？ / Confirm Rollback?") },
        text = {
            Text("回滚将恢复所有文件到迁移前的状态，此操作不可撤销。\nRollback will restore all files to pre-migration state. This cannot be undone.")
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text("确认回滚")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消 / Cancel")
            }
        }
    )
}
