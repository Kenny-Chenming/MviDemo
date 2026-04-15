package com.mvi.kenny.feature.corektx

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
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
 * CoreKtxMigrationScreen — AndroidX core-ktx 迁移工具主入口
 * ================================================================
 * Main entry point for the AndroidX core-ktx → core migration toolkit.
 * Implements tabbed navigation with Dashboard, Scanner, Validator, Impact, Compliance, Settings.
 *
 * @param onUpdateTopBar Callback to update the MainScreen TopAppBar title
 * @param viewModel ViewModel instance (created by viewModel() in production)
 *
 * @see CoreKtxMigrationState MVI state definition
 * @see CoreKtxMigrationIntent MVI intent definition
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CoreKtxMigrationScreen(
    onUpdateTopBar: (TopBarConfig) -> Unit,
    viewModel: CoreKtxMigrationViewModel = viewModel()
) {
    // Observe MVI state / 观察 MVI 状态
    val state by viewModel.state.collectAsState()

    // Track selected tab index / 追踪选择的 Tab 索引
    var selectedTabIndex by remember { mutableIntStateOf(0) }

    // Tab titles / Tab 标题
    val tabTitles = listOf(
        "仪表盘" to Icons.Default.Info,
        "扫描器" to Icons.Default.FilterList,
        "验证器" to Icons.Default.Security,
        "影响分析" to Icons.Default.Shield,
        "合规检查" to Icons.Default.CheckCircle,
        "设置" to Icons.Default.Settings
    )

    // Update TopAppBar when tab changes / Tab 切换时更新 TopAppBar
    LaunchedEffect(selectedTabIndex) {
        onUpdateTopBar(
            TopBarConfig(
                title = "core-ktx 迁移工具",
                actions = listOf(
                    TopBarAction(
                        icon = Icons.Default.Refresh,
                        contentDescription = "刷新 / Refresh",
                        onClick = { viewModel.sendIntent(CoreKtxMigrationIntent.RefreshScan) }
                    )
                )
            )
        )
    }

    // Collect effects for one-time events / 收集副作用
    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is CoreKtxMigrationEffect.ShowToast -> {
                    // Toast handled by UI layer / Toast 由 UI 层处理
                }
                is CoreKtxMigrationEffect.ShowError -> {
                    // Error handled by UI layer / 错误由 UI 层处理
                }
                else -> {}
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("core-ktx → core 迁移工具") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Tab Row / Tab 栏
            ScrollableTabRow(
                selectedTabIndex = selectedTabIndex,
                edgePadding = 16.dp
            ) {
                tabTitles.forEachIndexed { index, (title, icon) ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = { Text(title) },
                        icon = {
                            Icon(
                                imageVector = icon,
                                contentDescription = title,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    )
                }
            }

            // Tab Content / Tab 内容
            when (selectedTabIndex) {
                0 -> DashboardTab(state = state, viewModel = viewModel)
                1 -> ScannerTab(state = state, viewModel = viewModel)
                2 -> ValidatorTab(state = state, viewModel = viewModel)
                3 -> ImpactTab(state = state, viewModel = viewModel)
                4 -> ComplianceTab(state = state, viewModel = viewModel)
                5 -> SettingsTab(state = state, viewModel = viewModel)
            }
        }
    }
}

// ================================================================
// Dashboard Tab / 仪表盘 Tab
// ================================================================

/**
 * ============================================================
 * DashboardTab — 仪表盘 Tab
 * ================================================================
 * Shows overall migration health score, summary stats, and quick actions.
 */
@Composable
private fun DashboardTab(
    state: CoreKtxMigrationState,
    viewModel: CoreKtxMigrationViewModel
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Health Score Card / 健康度评分卡片
        item {
            HealthScoreCard(
                healthScore = state.healthScore,
                scanState = state.scanState
            )
        }

        // Summary Stats / 摘要统计
        item {
            SummaryStatsRow(
                totalDeps = state.totalDependencies,
                affectedDeps = state.affectedDependencies,
                criticalCount = state.criticalCount,
                warningCount = state.warningCount
            )
        }

        // Quick Actions / 快捷操作
        item {
            QuickActionsCard(
                onStartScan = { viewModel.sendIntent(CoreKtxMigrationIntent.StartScan) },
                onValidate = { viewModel.sendIntent(CoreKtxMigrationIntent.SelectTab(2)) },
                onCheckCompliance = { viewModel.sendIntent(CoreKtxMigrationIntent.SelectTab(4)) }
            )
        }

        // Progress Card / 进度卡片
        item {
            MigrationProgressCard(
                progress = state.migrationProgress,
                fixedCount = state.fixedCount,
                totalCount = state.scanResults.size
            )
        }
    }
}

/**
 * ============================================================
 * HealthScoreCard — 健康度评分卡片
 * ================================================================
 * Radar chart showing migration health score (0-100).
 */
@Composable
private fun HealthScoreCard(
    healthScore: Int,
    scanState: ScanState
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "迁移健康度 / Migration Health",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Radar Chart / 雷达图
            Box(
                modifier = Modifier.size(200.dp),
                contentAlignment = Alignment.Center
            ) {
                RadarChartView(
                    score = healthScore,
                    modifier = Modifier.fillMaxSize()
                )

                // Score display / 分数显示
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "$healthScore",
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Bold,
                        color = when {
                            healthScore >= 80 -> Color(0xFF4CAF50)
                            healthScore >= 50 -> Color(0xFFFF9800)
                            else -> Color(0xFFF44336)
                        }
                    )
                    Text(
                        text = when {
                            healthScore >= 80 -> "优秀 / Excellent"
                            healthScore >= 50 -> "需处理 / Needs Attention"
                            else -> "紧急 / Critical"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (scanState == ScanState.Scanning) {
                Spacer(modifier = Modifier.height(12.dp))
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "扫描中... / Scanning...",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 4.dp)
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
 */
@Composable
private fun RadarChartView(
    score: Int,
    modifier: Modifier = Modifier
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val fillColor = primaryColor.copy(alpha = 0.3f)

    Canvas(modifier = modifier) {
        val centerX = size.width / 2
        val centerY = size.height / 2
        val radius = minOf(centerX, centerY) * 0.8f

        // Draw background pentagon / 绘制背景五边形
        val bgPath = Path()
        for (i in 0 until 5) {
            val angle = (i * 72 - 90) * (Math.PI / 180)
            val x = centerX + radius * kotlin.math.cos(angle).toFloat()
            val y = centerY + radius * kotlin.math.sin(angle).toFloat()
            if (i == 0) bgPath.moveTo(x, y) else bgPath.lineTo(x, y)
        }
        bgPath.close()
        drawPath(bgPath, fillColor, style = Stroke(width = 1.dp.toPx()))

        // Draw score area / 绘制分数区域
        val fillRadius = radius * (score / 100f)
        val scorePath = Path()
        for (i in 0 until 5) {
            val angle = (i * 72 - 90) * (Math.PI / 180)
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
            center = Offset(centerX, centerY),
            style = Stroke(width = 2.dp.toPx())
        )

        // Draw axis lines / 绘制轴线
        for (i in 0 until 5) {
            val angle = (i * 72 - 90) * (Math.PI / 180)
            val x = centerX + radius * kotlin.math.cos(angle).toFloat()
            val y = centerY + radius * kotlin.math.sin(angle).toFloat()
            drawLine(
                color = primaryColor.copy(alpha = 0.3f),
                start = Offset(centerX, centerY),
                end = Offset(x, y),
                strokeWidth = 1.dp.toPx()
            )
        }
    }
}

/**
 * ============================================================
 * SummaryStatsRow — 摘要统计行
 * ================================================================
 * Row of stat cards showing key metrics.
 */
@Composable
private fun SummaryStatsRow(
    totalDeps: Int,
    affectedDeps: Int,
    criticalCount: Int,
    warningCount: Int
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StatCard(
            modifier = Modifier.weight(1f),
            title = "总依赖",
            value = "$totalDeps",
            subtitle = "Total Dependencies",
            color = Color(0xFF6750A4)
        )
        StatCard(
            modifier = Modifier.weight(1f),
            title = "受影响",
            value = "$affectedDeps",
            subtitle = "Affected",
            color = Color(0xFFFF9800)
        )
        StatCard(
            modifier = Modifier.weight(1f),
            title = "P0",
            value = "$criticalCount",
            subtitle = "Critical",
            color = Color(0xFFF44336)
        )
        StatCard(
            modifier = Modifier.weight(1f),
            title = "P1",
            value = "$warningCount",
            subtitle = "Warning",
            color = Color(0xFFFF9800)
        )
    }
}

/**
 * ============================================================
 * StatCard — 统计卡片
 * ================================================================
 * Individual stat display card.
 */
@Composable
private fun StatCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    subtitle: String,
    color: Color
) {
    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * ============================================================
 * QuickActionsCard — 快捷操作卡片
 * ================================================================
 * Quick action buttons for common tasks.
 */
@Composable
private fun QuickActionsCard(
    onStartScan: () -> Unit,
    onValidate: () -> Unit,
    onCheckCompliance: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "快捷操作 / Quick Actions",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onStartScan,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.FilterList,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("扫描", fontSize = 13.sp)
                }

                OutlinedButton(
                    onClick = onValidate,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("验证", fontSize = 13.sp)
                }

                OutlinedButton(
                    onClick = onCheckCompliance,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("合规", fontSize = 13.sp)
                }
            }
        }
    }
}

/**
 * ============================================================
 * MigrationProgressCard — 迁移进度卡片
 * ================================================================
 * Shows overall migration progress.
 */
@Composable
private fun MigrationProgressCard(
    progress: Float,
    fixedCount: Int,
    totalCount: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "迁移进度 / Migration Progress",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "$fixedCount / $totalCount",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = Color(0xFF4CAF50),
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "${(progress * 100).toInt()}% 已修复 / ${(progress * 100).toInt()}% Fixed",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
