package com.mvi.kenny.feature.androidfragment

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.Animation
import androidx.compose.material.icons.filled.Api
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QuestionAnswer
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
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
import androidx.compose.material3.OutlinedCard
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.collectLatest

// ================================================================
// Design tokens / 设计令牌
// ================================================================
private val PRIMARY_COLOR = Color(0xFF6750A4)       // Purple500
private val SECONDARY_COLOR = Color(0xFF03DAC5)     // Teal200
private val ERROR_COLOR = Color(0xFFF44336)         // Red500
private val WARNING_COLOR = Color(0xFFFFC107)       // Amber500
private val SURFACE_COLOR = Color(0xFFE7E0EC)        // SurfaceVariant
private val P0_COLOR = Color(0xFFF44336)            // Red — must migrate
private val P1_COLOR = Color(0xFFFF9800)            // Orange — recommend migrate
private val P2_COLOR = Color(0xFF4CAF50)            // Green — optional

// ================================================================
// Tab titles / Tab 标题
// ================================================================
private val TAB_TITLES = listOf(
    "概览",        // T0 Dashboard
    "场景指南",    // T1 Scenario Guide
    "迁移扫描器",  // T2 Migration Scanner
    "生命周期",    // T3 Lifecycle Debug
    "Result API",  // T4 Result API
    "动画配置",    // T5 Animation Config
    "导航融合"     // T6 Navigation Fusion
)

// ================================================================
// Severity color / 严重程度颜色
// ================================================================
private fun severityColor(severity: ScanSeverity): Color = when (severity) {
    ScanSeverity.P0 -> P0_COLOR
    ScanSeverity.P1 -> P1_COLOR
    ScanSeverity.P2 -> P2_COLOR
}

// ================================================================
// Main screen / 主屏幕
// ================================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AndroidFragmentScreen(
    onUpdateTopBar: (com.mvi.kenny.base.TopBarConfig) -> Unit,
    viewModel: AndroidFragmentViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Update TopBar / 更新顶部导航栏配置
    LaunchedEffect(state.currentTab) {
        val config = com.mvi.kenny.base.TopBarConfig(
            title = TAB_TITLES.getOrElse(state.currentTab) { "AndroidFragment" }
        )
        onUpdateTopBar(config)
    }

    // Collect effects / 收集副作用
    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is AndroidFragmentEffect.ShowMigrationGuide -> {
                    snackbarHostState.showSnackbar("迁移指南已生成：${effect.steps.size} 步")
                }
                is AndroidFragmentEffect.ShowAlert -> {
                    snackbarHostState.showSnackbar(effect.message)
                }
                is AndroidFragmentEffect.NavigateToTab -> {
                    viewModel.sendIntent(AndroidFragmentIntent.SelectTab(effect.tabIndex))
                }
                is AndroidFragmentEffect.ExportScanReport -> {
                    snackbarHostState.showSnackbar("报告已导出：${effect.filePath}")
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("AndroidFragment 工具包", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                actions = {
                    IconButton(
                        onClick = { viewModel.sendIntent(AndroidFragmentIntent.RefreshHealthScore) }
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
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
        ) {
            // Tab row / Tab 行
            TabRow(
                selectedTabIndex = state.currentTab,
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                TAB_TITLES.forEachIndexed { index, title ->
                    Tab(
                        selected = state.currentTab == index,
                        onClick = { viewModel.sendIntent(AndroidFragmentIntent.SelectTab(index)) },
                        text = {
                            Text(
                                text = title,
                                maxLines = 1,
                                fontSize = 11.sp
                            )
                        }
                    )
                }
            }

            // Tab content / Tab 内容
            when (state.currentTab) {
                AndroidFragmentTab.DASHBOARD -> DashboardTabContent(state, viewModel)
                AndroidFragmentTab.SCENARIO_GUIDE -> ScenarioGuideTabContent(state, viewModel)
                AndroidFragmentTab.MIGRATION_SCANNER -> MigrationScannerTabContent(state, viewModel)
                AndroidFragmentTab.LIFECYCLE_DEBUG -> LifecycleDebugTabContent(state, viewModel)
                AndroidFragmentTab.RESULT_API -> ResultApiTabContent(state)
                AndroidFragmentTab.ANIMATION_CONFIG -> AnimationConfigTabContent(state, viewModel)
                AndroidFragmentTab.NAVIGATION_FUSION -> NavigationFusionTabContent(state, viewModel)
            }
        }
    }
}

// ================================================================
// Dashboard Tab / 概览 Tab
// ================================================================
@Composable
private fun DashboardTabContent(
    state: AndroidFragmentState,
    viewModel: AndroidFragmentViewModel
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Health score card / 健康度卡片
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "整体健康度",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    // Radar chart / 雷达图
                    RadarChartView(
                        score = state.healthScore,
                        dimensions = state.healthDimensions,
                        modifier = Modifier.size(200.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "${state.healthScore}分",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = PRIMARY_COLOR
                    )
                }
            }
        }

        // Quick access cards / 快捷入口卡片
        item {
            Text(
                text = "快捷入口",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                QuickAccessCard(
                    icon = Icons.Default.Search,
                    title = "迁移扫描",
                    subtitle = "${state.scanResults.size} 个文件",
                    color = PRIMARY_COLOR,
                    onClick = { viewModel.sendIntent(AndroidFragmentIntent.SelectTab(AndroidFragmentTab.MIGRATION_SCANNER)) },
                    modifier = Modifier.weight(1f)
                )
                QuickAccessCard(
                    icon = Icons.Default.Sync,
                    title = "生命周期",
                    subtitle = "调试同步",
                    color = SECONDARY_COLOR,
                    onClick = { viewModel.sendIntent(AndroidFragmentIntent.SelectTab(AndroidFragmentTab.LIFECYCLE_DEBUG)) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                QuickAccessCard(
                    icon = Icons.Default.Api,
                    title = "Result API",
                    subtitle = "状态桥接",
                    color = Color(0xFF2196F3),
                    onClick = { viewModel.sendIntent(AndroidFragmentIntent.SelectTab(AndroidFragmentTab.RESULT_API)) },
                    modifier = Modifier.weight(1f)
                )
                QuickAccessCard(
                    icon = Icons.Default.Animation,
                    title = "动画配置",
                    subtitle = "过渡动画",
                    color = Color(0xFFE91E63),
                    onClick = { viewModel.sendIntent(AndroidFragmentIntent.SelectTab(AndroidFragmentTab.ANIMATION_CONFIG)) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                QuickAccessCard(
                    icon = Icons.Default.Navigation,
                    title = "导航融合",
                    subtitle = "NavHost 集成",
                    color = Color(0xFF9C27B0),
                    onClick = { viewModel.sendIntent(AndroidFragmentIntent.SelectTab(AndroidFragmentTab.NAVIGATION_FUSION)) },
                    modifier = Modifier.weight(1f)
                )
                QuickAccessCard(
                    icon = Icons.Default.QuestionAnswer,
                    title = "场景指南",
                    subtitle = "决策树",
                    color = Color(0xFF009688),
                    onClick = { viewModel.sendIntent(AndroidFragmentIntent.SelectTab(AndroidFragmentTab.SCENARIO_GUIDE)) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Alert messages / 告警消息
        if (state.alertMessages.isNotEmpty()) {
            item {
                Text(
                    text = "告警",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            items(state.alertMessages.take(3)) { alert ->
                AlertBanner(alert = alert)
            }
        }

        // Version notice / 版本提示
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = WARNING_COLOR.copy(alpha = 0.1f))
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = null,
                        tint = WARNING_COLOR
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "AndroidFragment (fragment-compose:1.8.0-alpha01) 为 alpha 版本，API 可能在稳定版前发生变化",
                        style = MaterialTheme.typography.bodySmall,
                        color = WARNING_COLOR
                    )
                }
            }
        }
    }
}

// ================================================================
// Radar chart view / 雷达图视图
// ================================================================
@Composable
private fun RadarChartView(
    score: Int,
    dimensions: Map<HealthDimension, Float>,
    modifier: Modifier = Modifier
) {
    val dimensionCount = dimensions.size
    val angleStep = 360f / dimensionCount

    Canvas(modifier = modifier) {
        val centerX = size.width / 2
        val centerY = size.height / 2
        val radius = minOf(centerX, centerY) * 0.8f

        // Draw grid circles / 绘制网格圆
        for (level in 1..5) {
            val gridRadius = radius * level / 5
            drawCircle(
                color = Color.Gray.copy(alpha = 0.2f),
                radius = gridRadius,
                center = Offset(centerX, centerY),
                style = Stroke(width = 1.dp.toPx())
            )
        }

        // Draw axis lines / 绘制轴线
        for (i in 0 until dimensionCount) {
            val angle = Math.toRadians((angleStep * i - 90).toDouble())
            val endX = centerX + radius * kotlin.math.cos(angle).toFloat()
            val endY = centerY + radius * kotlin.math.sin(angle).toFloat()
            drawLine(
                color = Color.Gray.copy(alpha = 0.3f),
                start = Offset(centerX, centerY),
                end = Offset(endX, endY),
                strokeWidth = 1.dp.toPx()
            )
        }

        // Draw data polygon / 绘制数据多边形
        val path = Path()
        dimensions.entries.forEachIndexed { index, (dim, value) ->
            val normalizedValue = (value / 100f).coerceIn(0f, 1f)
            val angle = Math.toRadians((angleStep * index - 90).toDouble())
            val x = centerX + radius * normalizedValue * kotlin.math.cos(angle).toFloat()
            val y = centerY + radius * normalizedValue * kotlin.math.sin(angle).toFloat()
            if (index == 0) {
                path.moveTo(x, y)
            } else {
                path.lineTo(x, y)
            }
        }
        path.close()

        drawPath(
            path = path,
            color = PRIMARY_COLOR.copy(alpha = 0.4f)
        )
        drawPath(
            path = path,
            color = PRIMARY_COLOR,
            style = Stroke(width = 2.dp.toPx())
        )
    }
}

// ================================================================
// Quick access card / 快捷入口卡片
// ================================================================
@Composable
private fun QuickAccessCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedCard(
        modifier = modifier.clickable(onClick = onClick),
        colors = CardDefaults.outlinedCardColors(
            containerColor = color.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                icon,
                contentDescription = title,
                tint = color,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// ================================================================
// Alert banner / 告警横幅
// ================================================================
@Composable
private fun AlertBanner(alert: AlertMessage) {
    val (bgColor, iconColor) = when (alert.severity) {
        AlertSeverity.ERROR -> ERROR_COLOR.copy(alpha = 0.1f) to ERROR_COLOR
        AlertSeverity.WARNING -> WARNING_COLOR.copy(alpha = 0.1f) to WARNING_COLOR
        AlertSeverity.INFO -> Color(0xFF2196F3).copy(alpha = 0.1f) to Color(0xFF2196F3)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = bgColor)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                when (alert.severity) {
                    AlertSeverity.ERROR -> Icons.Default.Warning
                    AlertSeverity.WARNING -> Icons.Default.Warning
                    AlertSeverity.INFO -> Icons.Default.Info
                },
                contentDescription = null,
                tint = iconColor
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = alert.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = iconColor
                )
                Text(
                    text = alert.message,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

// ================================================================
// Scenario Guide Tab / 场景指南 Tab
// ================================================================
@Composable
private fun ScenarioGuideTabContent(
    state: AndroidFragmentState,
    viewModel: AndroidFragmentViewModel
) {
    val questions = listOf(
        "项目是否使用了 FragmentContainerView？" to "检测到 FragmentContainerView 或 Fragment 依赖",
        "是否需要保留 Fragment 组件？" to "如果不需要保留，可以全量迁移到 Compose",
        "是否使用了 Fragment Result API？" to "setFragmentResult / getFragmentResult"
    )

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "回答以下问题，获取迁移方案推荐",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        items(questions.size) { index ->
            val (question, hint) = questions[index]
            val answer = state.decisionTreeAnswers[index]

            DecisionTreeCard(
                question = question,
                hint = hint,
                questionIndex = index,
                selectedAnswer = answer,
                onAnswer = { ans ->
                    viewModel.sendIntent(AndroidFragmentIntent.AnswerDecisionTree(index, ans))
                }
            )
        }

        // Recommended scenario / 推荐方案
        state.selectedScenario?.let { scenario ->
            item {
                Spacer(modifier = Modifier.height(8.dp))
                RecommendedScenarioCard(scenario = scenario)
            }
        }
    }
}

@Composable
private fun DecisionTreeCard(
    question: String,
    hint: String,
    questionIndex: Int,
    selectedAnswer: Boolean?,
    onAnswer: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Q${questionIndex + 1}. $question",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = hint,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { onAnswer(true) },
                    modifier = Modifier.weight(1f),
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                        containerColor = if (selectedAnswer == true) PRIMARY_COLOR else MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = if (selectedAnswer == true) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                ) {
                    Text("是")
                }
                Button(
                    onClick = { onAnswer(false) },
                    modifier = Modifier.weight(1f),
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                        containerColor = if (selectedAnswer == false) Color.Gray else MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = if (selectedAnswer == false) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                ) {
                    Text("否")
                }
            }
        }
    }
}

@Composable
private fun RecommendedScenarioCard(scenario: Scenario) {
    val (bgColor, icon) = when (scenario.type) {
        ScenarioType.ANDROID_FRAGMENT -> PRIMARY_COLOR.copy(alpha = 0.1f) to Icons.Default.Code
        ScenarioType.ANDROID_VIEW -> Color(0xFF2196F3).copy(alpha = 0.1f) to Icons.Default.Info
        ScenarioType.FULL_COMPOSE_MIGRATION -> Color(0xFF4CAF50).copy(alpha = 0.1f) to Icons.Default.CheckCircle
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = bgColor)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = PRIMARY_COLOR)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = scenario.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = scenario.description, style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = scenario.reason,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// ================================================================
// Migration Scanner Tab / 迁移扫描器 Tab
// ================================================================
@Composable
private fun MigrationScannerTabContent(
    state: AndroidFragmentState,
    viewModel: AndroidFragmentViewModel
) {
    Column(modifier = Modifier.fillMaxSize()) {
        // Scan button and progress / 扫描按钮和进度
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "迁移扫描器",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    if (state.isScanning) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Button(
                            onClick = { viewModel.sendIntent(AndroidFragmentIntent.StartScan) }
                        ) {
                            Icon(Icons.Default.Search, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("开始扫描")
                        }
                    }
                }

                if (state.isScanning) {
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { state.scanProgress },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        text = "扫描进度: ${(state.scanProgress * 100).toInt()}%",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }

        // Severity filter / 严重程度筛选
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = state.selectedSeverity == null,
                onClick = { viewModel.sendIntent(AndroidFragmentIntent.FilterBySeverity(null)) },
                label = { Text("全部") }
            )
            ScanSeverity.entries.forEach { severity ->
                FilterChip(
                    selected = state.selectedSeverity == severity,
                    onClick = { viewModel.sendIntent(AndroidFragmentIntent.FilterBySeverity(severity)) },
                    label = { Text("P${severity.ordinal}") },
                    colors = androidx.compose.material3.FilterChipDefaults.filterChipColors(
                        selectedContainerColor = severityColor(severity).copy(alpha = 0.2f)
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Scan results / 扫描结果
        val filteredResults = if (state.selectedSeverity != null) {
            state.scanResults.filter { it.severity == state.selectedSeverity }
        } else {
            state.scanResults
        }

        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(filteredResults) { result ->
                ScannerResultCard(result = result)
            }

            if (filteredResults.isEmpty() && !state.isScanning) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "点击「开始扫描」分析项目中的 Fragment 使用情况",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ScannerResultCard(result: FragmentScanResult) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded }
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(severityColor(result.severity), RoundedCornerShape(4.dp))
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = result.filePath,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = result.fragmentType.name.replace("_", " "),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                AssistChip(
                    onClick = { },
                    label = { Text("P${result.severity.ordinal}") },
                    colors = androidx.compose.material3.AssistChipDefaults.assistChipColors(
                        containerColor = severityColor(result.severity).copy(alpha = 0.15f)
                    )
                )
            }

            if (expanded) {
                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = result.description,
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "建议: ${result.suggestedAction}",
                    style = MaterialTheme.typography.bodySmall,
                    color = PRIMARY_COLOR,
                    fontWeight = FontWeight.Medium
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Icon(
                    if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = if (expanded) "收起" else "展开",
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// ================================================================
// Lifecycle Debug Tab / 生命周期调试 Tab
// ================================================================
@Composable
private fun LifecycleDebugTabContent(
    state: AndroidFragmentState,
    viewModel: AndroidFragmentViewModel
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Control buttons / 控制按钮
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { viewModel.sendIntent(AndroidFragmentIntent.StartLiveMonitoring) },
                    enabled = !state.isLiveMonitoring,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("开始监控")
                }
                Button(
                    onClick = { viewModel.sendIntent(AndroidFragmentIntent.StopLiveMonitoring) },
                    enabled = state.isLiveMonitoring,
                    modifier = Modifier.weight(1f),
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                        containerColor = ERROR_COLOR
                    )
                ) {
                    Icon(Icons.Default.Stop, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("停止监控")
                }
            }
        }

        // Status indicator / 状态指示
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .background(
                            if (state.isLiveMonitoring) Color(0xFF4CAF50) else Color.Gray,
                            RoundedCornerShape(6.dp)
                        )
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (state.isLiveMonitoring) "实时监控中..." else "监控已停止",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        // Lifecycle timeline / 生命周期时间轴
        item {
            Text(
                text = "生命周期时间轴",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        items(state.lifecycleEvents.reversed()) { event ->
            LifecycleEventCard(event = event)
        }

        if (state.lifecycleEvents.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "点击「开始监控」查看 Fragment 生命周期状态",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
private fun LifecycleEventCard(event: LifecycleEvent) {
    val syncColor = if (event.isSynced) SECONDARY_COLOR else ERROR_COLOR

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = syncColor.copy(alpha = 0.08f)
        )
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                if (event.isSynced) Icons.Default.CheckCircle else Icons.Default.Warning,
                contentDescription = null,
                tint = syncColor,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = event.state.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = event.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = formatTimestamp(event.timestamp),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun formatTimestamp(ts: Long): String {
    val s = (ts % 1000) / 10
    return "${s}ms"
}

// ================================================================
// Result API Tab / Result API Tab
// ================================================================
@Composable
private fun ResultApiTabContent(state: AndroidFragmentState) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "Fragment Result API × Compose State 桥接",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        // Mapping table / 映射表
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "API 映射表",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Fragment Result",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = PRIMARY_COLOR
                        )
                        Text(
                            text = "→",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Compose StateFlow",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = SECONDARY_COLOR
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    listOf(
                        "setFragmentResult" to "viewModelScope.launch { _state.update { } }",
                        "getFragmentResult" to "FragmentResultListener → StateFlow",
                        "setFragmentResultListener" to "DisposableEffect cleanup"
                    ).forEach { (frag, compose) ->
                        HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))
                        Text(text = frag, style = MaterialTheme.typography.bodySmall, color = PRIMARY_COLOR)
                        Text(text = compose, style = MaterialTheme.typography.bodySmall, color = SECONDARY_COLOR)
                    }
                }
            }
        }

        // Before/After comparison / 前后对比
        item {
            Text(
                text = "代码示例",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            BeforeAfterCard(
                title = "FragmentResultListener 在 Compose 中的用法",
                before = """
// 旧写法（AndroidFragment 之外，不工作）
fragment.resultListener = { key, bundle ->
    val data = bundle.getString("data")
    // ❌ 无法直接写入 Compose State
}
                """.trimIndent(),
                after = """
// 新写法（AndroidFragment 下，正常工作）
AndroidFragment(fragmentClass = MyFragment::class.java) {
    // FragmentResultListener 自动同步到 Compose State
    LaunchedEffect(Unit) {
        fragment.fragmentResultFlow.collect { result ->
            _state.update { it.copy(data = result) }
        }
    }
}
                """.trimIndent()
            )
        }

        // Note / 注意事项
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = WARNING_COLOR.copy(alpha = 0.1f))
            ) {
                Row(modifier = Modifier.padding(12.dp)) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = null,
                        tint = WARNING_COLOR,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Result API 桥接需要在 FragmentResultListener 中调用 viewModelScope.launch { _state.update { } } 写入 Compose State",
                        style = MaterialTheme.typography.bodySmall,
                        color = WARNING_COLOR
                    )
                }
            }
        }
    }
}

// ================================================================
// Before/After card / 前后对比卡片
// ================================================================
@Composable
private fun BeforeAfterCard(
    title: String,
    before: String,
    after: String
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded }
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (expanded) {
                Spacer(modifier = Modifier.height(8.dp))
                // Before / 旧写法
                Text(
                    text = "旧写法",
                    style = MaterialTheme.typography.labelSmall,
                    color = ERROR_COLOR,
                    fontWeight = FontWeight.Bold
                )
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = ERROR_COLOR.copy(alpha = 0.05f)
                    )
                ) {
                    Text(
                        text = before,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(8.dp),
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                // After / 新写法
                Text(
                    text = "新写法",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF4CAF50),
                    fontWeight = FontWeight.Bold
                )
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF4CAF50).copy(alpha = 0.05f)
                    )
                ) {
                    Text(
                        text = after,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(8.dp),
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                    )
                }
            }
        }
    }
}

// ================================================================
// Animation Config Tab / 动画配置 Tab
// ================================================================
@Composable
private fun AnimationConfigTabContent(
    state: AndroidFragmentState,
    viewModel: AndroidFragmentViewModel
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "AndroidFragment 动画配置",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        // Transition type selector / 动画类型选择器
        item {
            Text(
                text = "选择动画类型",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TransitionType.entries.forEach { type ->
                    FilterChip(
                        selected = state.transitionType == type,
                        onClick = { viewModel.sendIntent(AndroidFragmentIntent.SetTransitionType(type)) },
                        label = { Text(type.name.replace("_", " ")) },
                        colors = androidx.compose.material3.FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PRIMARY_COLOR.copy(alpha = 0.2f)
                        )
                    )
                }
            }
        }

        // Preview panel / 预览面板
        item {
            Text(
                text = "动画效果预览",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(8.dp))
            TransitionPreviewPanel(transitionType = state.transitionType)
        }

        // Code snippet card / 代码片段卡片
        item {
            Text(
                text = "配置代码",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium
            )
        }

        item {
            CodeSnippetCard(
                title = "XML 动画配置",
                code = """
<!-- 在 Fragment 的布局 XML 中配置 -->
<androidx.fragment.app.FragmentContainerView
    android:id="@+id/nav_host_fragment"
    android:name="androidx.navigation.fragment.NavHostFragment"
    app:defaultAnimations="true"
    app:enterAnim="@anim/slide_in_right"
    app:exitAnim="@anim/slide_out_left"
    app:popEnterAnim="@anim/slide_in_left"
    app:popExitAnim="@anim/slide_out_right" />
                """.trimIndent()
            )
        }

        item {
            CodeSnippetCard(
                title = "AndroidFragment 动画配置",
                code = """
// 在 FragmentTransaction 中配置
AndroidFragment(fragmentClass = DetailFragment::class.java) {
    // 配置 enter/exit 动画
    enterTransition = slideIn(AnimatedContentTransitionScope.SlideDirection.Start)
    exitTransition = slideOut(AnimatedContentTransitionScope.SlideDirection.End)
    popEnterTransition = slideIn(AnimatedContentTransitionScope.SlideDirection.End)
    popExitTransition = slideOut(AnimatedContentTransitionScope.SlideDirection.Start)
}
                """.trimIndent()
            )
        }

        // Animation type guide / 动画类型指南
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "动画类型说明",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    listOf(
                        "ENTER" to "Fragment 进入时的动画",
                        "EXIT" to "Fragment 退出时的动画",
                        "POP_ENTER" to "返回栈弹出时进入的动画",
                        "POP_EXIT" to "返回栈弹出时退出的动画"
                    ).forEach { (type, desc) ->
                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                        Text(text = type, style = MaterialTheme.typography.bodySmall, color = PRIMARY_COLOR, fontWeight = FontWeight.Bold)
                        Text(text = desc, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}

@Composable
private fun TransitionPreviewPanel(transitionType: TransitionType) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            val offsetX by animateFloatAsState(
                targetValue = when (transitionType) {
                    TransitionType.ENTER, TransitionType.POP_EXIT -> 50f
                    TransitionType.EXIT, TransitionType.POP_ENTER -> -50f
                },
                label = "offsetX"
            )
            val alpha by animateFloatAsState(
                targetValue = when (transitionType) {
                    TransitionType.ENTER, TransitionType.POP_ENTER -> 1f
                    TransitionType.EXIT, TransitionType.POP_EXIT -> 0.3f
                },
                label = "alpha"
            )

            Box(
                modifier = Modifier
                    .size(60.dp)
                    .padding(start = offsetX.dp)
                    .background(PRIMARY_COLOR.copy(alpha = alpha), RoundedCornerShape(12.dp))
            )
            Text(
                text = when (transitionType) {
                    TransitionType.ENTER -> "淡入 + 右滑"
                    TransitionType.EXIT -> "淡出 + 左滑"
                    TransitionType.POP_ENTER -> "淡入 + 右滑"
                    TransitionType.POP_EXIT -> "淡出 + 左滑"
                },
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}

@Composable
private fun CodeSnippetCard(title: String, code: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = Color.White
                )
                IconButton(
                    onClick = { /* Copy to clipboard */ },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = "Copy",
                        tint = Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            Text(
                text = code,
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF9CDCFE),
                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

// ================================================================
// Navigation Fusion Tab / 导航融合 Tab
// ================================================================
@Composable
private fun NavigationFusionTabContent(
    state: AndroidFragmentState,
    viewModel: AndroidFragmentViewModel
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "NavHostFragment → AndroidFragment",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Button(
                    onClick = { viewModel.sendIntent(AndroidFragmentIntent.AnalyzeNavGraph) }
                ) {
                    Icon(Icons.Default.AccountTree, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("分析")
                }
            }
        }

        // Analysis result / 分析结果
        state.navGraphAnalysis?.let { analysis ->
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "导航图分析结果",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "${analysis.totalFragments}",
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = PRIMARY_COLOR
                                )
                                Text(text = "总 Fragment 数", style = MaterialTheme.typography.bodySmall)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "${analysis.migratedFragments}",
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF4CAF50)
                                )
                                Text(text = "已迁移", style = MaterialTheme.typography.bodySmall)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "${analysis.totalFragments - analysis.migratedFragments}",
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = ERROR_COLOR
                                )
                                Text(text = "待迁移", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }

            items(analysis.suggestions) { suggestion ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color(0xFF4CAF50),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = suggestion,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }

        if (state.navGraphAnalysis == null) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "点击「分析」扫描 NavGraph 中的 Fragment 使用情况",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        // Migration step cards / 迁移步骤卡片
        item {
            Text(
                text = "迁移步骤",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
        }

        val migrationSteps = listOf(
            MigrationStep(
                step = 1,
                title = "添加 fragment-compose 依赖",
                description = "在 build.gradle.kts 中添加 androidx.fragment:fragment-compose:1.8.0-alpha01",
                codeSnippet = """dependencies {
    implementation("androidx.fragment:fragment-compose:1.8.0-alpha01")
}"""
            ),
            MigrationStep(
                step = 2,
                title = "替换 FragmentContainerView 为 AndroidFragment",
                description = "在 Compose UI 中使用 AndroidFragment 替代 FragmentContainerView + AndroidView",
                codeSnippet = """// Before
AndroidView(
    factory = { ctx -> FragmentContainerView(ctx) },
    modifier = Modifier.fillMaxSize()
)

// After
AndroidFragment(
    fragmentClass = MyFragment::class.java,
    modifier = Modifier.fillMaxSize()
)"""
            ),
            MigrationStep(
                step = 3,
                title = "更新导航图",
                description = "在 NavGraph 中将 NavHostFragment 替换为 AndroidFragment 目的地",
                codeSnippet = """// NavGraph 中
composable("detail") {
    AndroidFragment(
        fragmentClass = DetailFragment::class.java,
        modifier = Modifier.fillMaxSize()
    )
}"""
            ),
            MigrationStep(
                step = 4,
                title = "验证生命周期同步",
                description = "使用生命周期调试工具验证 Fragment 与 Compose 重组同步正常"
            )
        )

        items(migrationSteps) { step ->
            MigrationStepCard(step = step)
        }
    }
}

@Composable
private fun MigrationStepCard(step: MigrationStep) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .background(PRIMARY_COLOR, RoundedCornerShape(14.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${step.step}",
                        color = Color.White,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = step.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = step.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            step.codeSnippet?.let { code ->
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E))
                ) {
                    Text(
                        text = code,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF9CDCFE),
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }
        }
    }
}
