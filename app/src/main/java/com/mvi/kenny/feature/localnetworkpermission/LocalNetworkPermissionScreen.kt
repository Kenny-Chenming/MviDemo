package com.mvi.kenny.feature.localnetworkpermission

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

// Contract types are top-level in same package — no import needed
// Contract types: LocalNetworkPermissionState, LocalNetworkPermissionIntent, LocalNetworkPermissionEffect + enums/data classes

// ===== Color Palette — Android 17 Permission Theme =====
// ===== 配色方案 — Android 17 权限主题 =====

private val Primary = Color(0xFF1565C0)              // Primary blue
private val ErrorP0 = Color(0xFFD32F2F)               // P0 Error: Red
private val WarningP1 = Color(0xFFF57C00)             // P1 Warning: Orange
private val CautionP2 = Color(0xFFFBC02D)             // P2 Caution: Yellow
private val Success = Color(0xFF388E3C)               // Success: Green
private val Surface = Color(0xFF1E1E1E)              // Surface: Dark gray
private val SurfaceVariant = Color(0xFF2D2D2D)       // Surface variant
private val OnSurfaceLight = Color(0xFFE0E0E0)
private val OnSurfaceDim = Color(0xFF9E9E9E)

// ===== Main Screen =====
// ===== 主屏幕 =====

@Composable
fun LocalNetworkPermissionScreen(
    viewModel: LocalNetworkPermissionViewModel = viewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var selectedTab by remember { mutableStateOf(LocalNetworkTab.DASHBOARD) }

    // Collect effects / 收集副作用
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is LocalNetworkPermissionEffect.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(effect.message)
                }
                is LocalNetworkPermissionEffect.TestSuiteGenerated -> {
                    snackbarHostState.showSnackbar("Test suite: ${effect.filePath}")
                }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            NavigationBar(
                containerColor = SurfaceVariant
            ) {
                LocalNetworkTab.entries.forEach { tab ->
                    NavigationBarItem(
                        icon = {
                            Icon(
                                imageVector = when (tab) {
                                    LocalNetworkTab.DASHBOARD -> Icons.Default.Dashboard
                                    LocalNetworkTab.SCANNER -> Icons.Default.Search
                                    LocalNetworkTab.MIGRATION -> Icons.Default.ArrowForward
                                    LocalNetworkTab.SETTINGS -> Icons.Default.Settings
                                },
                                contentDescription = tab.title
                            )
                        },
                        label = { Text(tab.title, fontSize = 11.sp) },
                        selected = selectedTab == tab,
                        onClick = { selectedTab = tab },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Primary,
                            selectedTextColor = Primary,
                            indicatorColor = Primary.copy(alpha = 0.15f)
                        )
                    )
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Surface)
                .padding(paddingValues)
        ) {
            // Header / 顶部标题栏
            LocalNetworkHeader()

            // Tab Content / 标签页内容
            AnimatedContent(
                targetState = selectedTab,
                transitionSpec = {
                    fadeIn() togetherWith fadeOut()
                },
                label = "TabContent"
            ) { tab ->
                when (tab) {
                    LocalNetworkTab.DASHBOARD -> DashboardTab(
                        state = state,
                        onIntent = viewModel::processIntent
                    )
                    LocalNetworkTab.SCANNER -> ScannerTab(
                        state = state,
                        onIntent = viewModel::processIntent
                    )
                    LocalNetworkTab.MIGRATION -> MigrationTab(
                        state = state,
                        onIntent = viewModel::processIntent
                    )
                    LocalNetworkTab.SETTINGS -> SettingsTab(
                        state = state,
                        onIntent = viewModel::processIntent
                    )
                }
            }
        }
    }
}

// ===== Header =====
// ===== 标题栏 =====

@Composable
private fun LocalNetworkHeader() {
    Surface(
        color = SurfaceVariant,
        shadowElevation = 4.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.NetworkCheck,
                    contentDescription = null,
                    tint = Primary,
                    modifier = Modifier.size(28.dp)
                )
                Column {
                    Text(
                        text = "Local Network Permission",
                        style = MaterialTheme.typography.titleMedium,
                        color = OnSurfaceLight,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Android 17 本地网络权限迁移检测与合规工具包",
                        style = MaterialTheme.typography.bodySmall,
                        color = OnSurfaceDim
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                Badge(
                    containerColor = Primary.copy(alpha = 0.2f),
                    contentColor = Primary
                ) {
                    Text("PRD-122", fontSize = 12.sp, modifier = Modifier.padding(horizontal = 4.dp))
                }
            }
        }
    }
    HorizontalDivider(color = Primary.copy(alpha = 0.3f), thickness = 1.dp)
}

// ===== Dashboard Tab =====
// ===== 仪表盘标签页 =====

@Composable
private fun DashboardTab(
    state: LocalNetworkPermissionState,
    onIntent: (LocalNetworkPermissionIntent) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Radar Chart Card / 雷达图卡片
        item {
            RadarChartCard(state = state)
        }

        // Health Score Card / 健康度卡片
        item {
            HealthScoreCard(state = state)
        }

        // Scan Button / 扫描按钮
        item {
            ScanControlCard(state = state, onIntent = onIntent)
        }

        // Issue Summary / 问题摘要
        if (state.affectedApis.isNotEmpty()) {
            item {
                IssueSummaryCard(state = state)
            }
        }
    }
}

@Composable
private fun RadarChartCard(state: LocalNetworkPermissionState) {
    Card(
        colors = CardDefaults.cardColors(containerColor = SurfaceVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Permission Health Radar / 权限健康度雷达图",
                style = MaterialTheme.typography.titleSmall,
                color = OnSurfaceLight
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Canvas-drawn hexagon radar chart
            // Canvas 绘制的六边形雷达图
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp),
                contentAlignment = Alignment.Center
            ) {
                RadarChart(
                    scores = state.radarScores.toList(),
                    modifier = Modifier.fillMaxWidth(0.85f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Dimension labels / 维度标签
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                state.radarScores.toList().forEachIndexed { index, score ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = score.dimension.split(" / ")[0],
                            style = MaterialTheme.typography.bodySmall,
                            color = OnSurfaceDim
                        )
                        Text(
                            text = "${score.score}",
                            style = MaterialTheme.typography.bodySmall,
                            color = when {
                                score.score >= 60 -> Success
                                score.score >= 40 -> WarningP1
                                else -> ErrorP0
                            },
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

/**
 * Canvas-drawn hexagon radar chart with 6 axes
 * Canvas 绘制的六边形雷达图，6 个轴
 */
@Composable
private fun RadarChart(
    scores: List<RadarScore>,
    modifier: Modifier = Modifier
) {
    val primaryColor = Primary
    val gridColor = OnSurfaceDim.copy(alpha = 0.3f)
    val scoreFillColor = Primary.copy(alpha = 0.3f)
    val scoreStrokeColor = Primary

    Canvas(modifier = modifier.aspectRatio(1f)) {
        val centerX = size.width / 2
        val centerY = size.height / 2
        val radius = minOf(centerX, centerY) * 0.85f
        val numAxes = scores.size.coerceAtLeast(6)
        val angleStep = (2 * PI / numAxes).toFloat()

        // Draw concentric hexagon grids / 绘制同心六边形网格
        for (level in 1..5) {
            val levelRadius = radius * level / 5
            val path = Path()
            for (i in 0 until numAxes) {
                val angle = -PI / 2 + i * angleStep
                val x = centerX + levelRadius * cos(angle).toFloat()
                val y = centerY + levelRadius * sin(angle).toFloat()
                if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }
            path.close()
            drawPath(
                path = path,
                color = gridColor.copy(alpha = 0.3f),
                style = Stroke(width = 1.dp.toPx())
            )
        }

        // Draw axes / 绘制轴
        for (i in 0 until numAxes) {
            val angle = -PI / 2 + i * angleStep
            val x = centerX + radius * cos(angle).toFloat()
            val y = centerY + radius * sin(angle).toFloat()
            drawLine(
                color = gridColor,
                start = Offset(centerX, centerY),
                end = Offset(x, y),
                strokeWidth = 1.dp.toPx()
            )
        }

        // Draw score polygon / 绘制分数多边形
        if (scores.isNotEmpty()) {
            val scorePath = Path()
            for (i in 0 until numAxes) {
                val score = scores.getOrNull(i)?.score?.coerceIn(0, 100) ?: 0
                val normalizedScore = score / 100f
                val angle = -PI / 2 + i * angleStep
                val x = centerX + radius * normalizedScore * cos(angle).toFloat()
                val y = centerY + radius * normalizedScore * sin(angle).toFloat()
                if (i == 0) scorePath.moveTo(x, y) else scorePath.lineTo(x, y)
            }
            scorePath.close()

            drawPath(
                path = scorePath,
                color = scoreFillColor,
                style = androidx.compose.ui.graphics.drawscope.Fill
            )
            drawPath(
                path = scorePath,
                color = scoreStrokeColor,
                style = Stroke(width = 2.dp.toPx())
            )

            // Draw score dots / 绘制分数点
            for (i in 0 until numAxes) {
                val score = scores.getOrNull(i)?.score?.coerceIn(0, 100) ?: 0
                val normalizedScore = score / 100f
                val angle = -PI / 2 + i * angleStep
                val x = centerX + radius * normalizedScore * cos(angle).toFloat()
                val y = centerY + radius * normalizedScore * sin(angle).toFloat()
                drawCircle(
                    color = scoreStrokeColor,
                    radius = 4.dp.toPx(),
                    center = Offset(x, y)
                )
            }
        }
    }
}

@Composable
private fun HealthScoreCard(state: LocalNetworkPermissionState) {
    Card(
        colors = CardDefaults.cardColors(containerColor = SurfaceVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "Overall Health Score / 总体健康度",
                    style = MaterialTheme.typography.bodyMedium,
                    color = OnSurfaceDim
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "${state.overallHealthScore}",
                        style = MaterialTheme.typography.displayMedium,
                        color = when {
                            state.overallHealthScore >= 70 -> Success
                            state.overallHealthScore >= 40 -> WarningP1
                            else -> ErrorP0
                        },
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "%",
                        style = MaterialTheme.typography.titleLarge,
                        color = OnSurfaceDim,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = when {
                        state.overallHealthScore >= 70 -> "Good / 状态良好"
                        state.overallHealthScore >= 40 -> "Needs attention / 需要处理"
                        else -> "Critical / 严重阻塞"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = OnSurfaceDim
                )
            }
            // Circular progress indicator / 环形进度
            Box(contentAlignment = Alignment.Center) {
                CircularProgressIndicator(
                    progress = { state.overallHealthScore / 100f },
                    modifier = Modifier.size(80.dp),
                    strokeWidth = 8.dp,
                    color = when {
                        state.overallHealthScore >= 70 -> Success
                        state.overallHealthScore >= 40 -> WarningP1
                        else -> ErrorP0
                    },
                    trackColor = Surface
                )
                Text(
                    text = "${state.affectedApis.size}",
                    style = MaterialTheme.typography.titleMedium,
                    color = OnSurfaceLight,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun ScanControlCard(
    state: LocalNetworkPermissionState,
    onIntent: (LocalNetworkPermissionIntent) -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = SurfaceVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = when {
                            state.scanStatus == ScanStatus.IDLE -> "Ready to scan / 准备就绪"
                            state.scanStatus == ScanStatus.SCANNING -> "Scanning... / 扫描中"
                            else -> "Scan completed / 扫描完成"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = OnSurfaceLight
                    )
                    if (state.scanStatus == ScanStatus.SCANNING) {
                        Text(
                            text = "${(state.scanProgress * 100).toInt()}%",
                            style = MaterialTheme.typography.bodySmall,
                            color = OnSurfaceDim
                        )
                    }
                }
                if (state.isScanning) {
                    FilledTonalButton(
                        onClick = { onIntent(LocalNetworkPermissionIntent.CancelScan) }
                    ) {
                        Text("Cancel / 取消")
                    }
                } else {
                    Button(
                        onClick = { onIntent(LocalNetworkPermissionIntent.StartScan) },
                        colors = ButtonDefaults.buttonColors(containerColor = Primary)
                    ) {
                        Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Start Scan / 开始扫描")
                    }
                }
            }
            // Scan progress bar / 扫描进度条
            if (state.isScanning) {
                Spacer(modifier = Modifier.height(12.dp))
                LinearProgressIndicator(
                    progress = { state.scanProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = Primary,
                    trackColor = Surface
                )
            }
        }
    }
}

@Composable
private fun IssueSummaryCard(state: LocalNetworkPermissionState) {
    Card(
        colors = CardDefaults.cardColors(containerColor = SurfaceVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Issue Summary / 问题摘要",
                style = MaterialTheme.typography.titleSmall,
                color = OnSurfaceLight
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                SeverityStatChip(
                    label = "P0",
                    count = state.countBySeverity(Severity.P0),
                    color = ErrorP0
                )
                SeverityStatChip(
                    label = "P1",
                    count = state.countBySeverity(Severity.P1),
                    color = WarningP1
                )
                SeverityStatChip(
                    label = "P2",
                    count = state.countBySeverity(Severity.P2),
                    color = CautionP2
                )
            }
        }
    }
}

@Composable
private fun SeverityStatChip(label: String, count: Int, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "$count",
            style = MaterialTheme.typography.titleLarge,
            color = color,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = OnSurfaceDim
        )
    }
}

// ===== Scanner Tab =====
// ===== 扫描器标签页 =====

@Composable
private fun ScannerTab(
    state: LocalNetworkPermissionState,
    onIntent: (LocalNetworkPermissionIntent) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        // Severity filter chips / 严重级别过滤 Chips
        SeverityFilterChips(state = state, onIntent = onIntent)

        if (state.affectedApis.isEmpty()) {
            EmptyStateMessage(
                icon = Icons.Default.Search,
                title = "No scan results / 无扫描结果",
                subtitle = "Run a scan first to detect affected APIs / 请先运行扫描以检测受影响的 API"
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(state.filteredApis()) { api ->
                    AffectedApiCard(api = api)
                }
            }
        }
    }
}

@Composable
private fun SeverityFilterChips(
    state: LocalNetworkPermissionState,
    onIntent: (LocalNetworkPermissionIntent) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FilterChip(
            selected = state.severityFilter == null,
            onClick = { onIntent(LocalNetworkPermissionIntent.SetSeverityFilter(null)) },
            label = { Text("All / 全部") },
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = Primary.copy(alpha = 0.2f),
                selectedLabelColor = Primary
            )
        )
        FilterChip(
            selected = state.severityFilter == Severity.P0,
            onClick = { onIntent(LocalNetworkPermissionIntent.SetSeverityFilter(Severity.P0)) },
            label = { Text("P0 (${state.countBySeverity(Severity.P0)})") },
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = ErrorP0.copy(alpha = 0.2f),
                selectedLabelColor = ErrorP0
            )
        )
        FilterChip(
            selected = state.severityFilter == Severity.P1,
            onClick = { onIntent(LocalNetworkPermissionIntent.SetSeverityFilter(Severity.P1)) },
            label = { Text("P1 (${state.countBySeverity(Severity.P1)})") },
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = WarningP1.copy(alpha = 0.2f),
                selectedLabelColor = WarningP1
            )
        )
        FilterChip(
            selected = state.severityFilter == Severity.P2,
            onClick = { onIntent(LocalNetworkPermissionIntent.SetSeverityFilter(Severity.P2)) },
            label = { Text("P2 (${state.countBySeverity(Severity.P2)})") },
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = CautionP2.copy(alpha = 0.2f),
                selectedLabelColor = CautionP2
            )
        )
    }
}

@Composable
private fun AffectedApiCard(api: AffectedApi) {
    val severityColor = when (api.severity) {
        Severity.P0 -> ErrorP0
        Severity.P1 -> WarningP1
        Severity.P2 -> CautionP2
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = SurfaceVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SeverityBadge(severity = api.severity)
                Text(
                    text = api.apiName,
                    style = MaterialTheme.typography.titleMedium,
                    color = OnSurfaceLight,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.weight(1f))
                Badge(containerColor = severityColor.copy(alpha = 0.2f), contentColor = severityColor) {
                    Text(api.severity.name, modifier = Modifier.padding(horizontal = 4.dp))
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "${api.filePath}:${api.lineNumber}",
                style = MaterialTheme.typography.bodySmall,
                color = OnSurfaceDim
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = api.suggestion,
                style = MaterialTheme.typography.bodySmall,
                color = OnSurfaceLight
            )
            api.alternativeApi?.let { alt ->
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Alternative / 替代: $alt",
                    style = MaterialTheme.typography.bodySmall,
                    color = Success
                )
            }
        }
    }
}

@Composable
private fun SeverityBadge(severity: Severity) {
    val (color, label) = when (severity) {
        Severity.P0 -> ErrorP0 to "P0"
        Severity.P1 -> WarningP1 to "P1"
        Severity.P2 -> CautionP2 to "P2"
    }
    Box(
        modifier = Modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(color.copy(alpha = 0.2f)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            color = color,
            fontWeight = FontWeight.Bold
        )
    }
}

// ===== Migration Tab =====
// ===== 迁移指南标签页 =====

@Composable
private fun MigrationTab(
    state: LocalNetworkPermissionState,
    onIntent: (LocalNetworkPermissionIntent) -> Unit
) {
    if (state.migrationGuides.isEmpty()) {
        EmptyStateMessage(
            icon = Icons.Default.ArrowForward,
            title = "No migration guides / 无迁移指南",
            subtitle = "Run a scan first to get migration recommendations / 请先运行扫描以获取迁移建议"
        )
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    text = "Permission Migration Paths / 权限迁移路径",
                    style = MaterialTheme.typography.titleSmall,
                    color = OnSurfaceLight
                )
                Text(
                    text = "Android 17 本地网络访问的三种权限路径",
                    style = MaterialTheme.typography.bodySmall,
                    color = OnSurfaceDim
                )
            }
            items(state.migrationGuides) { guide ->
                MigrationGuideCard(guide = guide)
            }
        }
    }
}

@Composable
private fun MigrationGuideCard(guide: MigrationGuide) {
    var expanded by remember { mutableStateOf(false) }

    val priorityColor = when (guide.priority) {
        1 -> Success
        2 -> WarningP1
        else -> CautionP2
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = SurfaceVariant),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Badge(containerColor = priorityColor.copy(alpha = 0.2f), contentColor = priorityColor) {
                    Text("#${guide.priority}", modifier = Modifier.padding(horizontal = 4.dp))
                }
                Text(
                    text = guide.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = OnSurfaceLight,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.weight(1f))
                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = OnSurfaceDim
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = guide.description,
                style = MaterialTheme.typography.bodySmall,
                color = OnSurfaceDim
            )

            if (expanded) {
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = OnSurfaceDim.copy(alpha = 0.3f))
                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Steps / 步骤:",
                    style = MaterialTheme.typography.bodySmall,
                    color = OnSurfaceLight,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                guide.steps.forEach { step ->
                    Text(
                        text = step,
                        style = MaterialTheme.typography.bodySmall,
                        color = OnSurfaceDim,
                        modifier = Modifier.padding(start = 8.dp, top = 2.dp)
                    )
                }

                guide.codeSnippet?.let { snippet ->
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Code / 代码示例:",
                        style = MaterialTheme.typography.bodySmall,
                        color = OnSurfaceLight,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Surface(
                        color = Surface,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 200.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        Text(
                            text = snippet,
                            style = MaterialTheme.typography.bodySmall,
                            color = Primary,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }
            }
        }
    }
}

// ===== Settings Tab =====
// ===== 设置标签页 =====

@Composable
private fun SettingsTab(
    state: LocalNetworkPermissionState,
    onIntent: (LocalNetworkPermissionIntent) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "Permission State / 权限状态",
                style = MaterialTheme.typography.titleSmall,
                color = OnSurfaceLight
            )
            Text(
                text = "Select the permission state for your project / 选择您项目的权限状态",
                style = MaterialTheme.typography.bodySmall,
                color = OnSurfaceDim
            )
        }

        // Permission state radio buttons / 权限状态单选按钮
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    PermissionState.entries.forEach { permissionState ->
                        PermissionStateRadioItem(
                            permissionState = permissionState,
                            isSelected = state.selectedPermissionState == permissionState,
                            onSelect = { onIntent(LocalNetworkPermissionIntent.SetPermissionState(permissionState)) }
                        )
                    }
                }
            }
        }

        // Generate Test Suite Button / 生成测试套件按钮
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = { onIntent(LocalNetworkPermissionIntent.GenerateTestSuite) },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Primary)
            ) {
                Icon(Icons.Default.Science, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Generate Test Suite / 生成测试套件")
            }
        }

        // Info card / 信息卡片
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Primary.copy(alpha = 0.1f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = Primary)
                        Text(
                            text = "Android 17 Changes / Android 17 变更",
                            style = MaterialTheme.typography.titleSmall,
                            color = Primary
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Android 17 引入了 ACCESS_LOCAL_NETWORK 运行时权限，需要应用在使用本地网络功能前明确申请。NEARBY_WIFI_DEVICES 权限仍然适用于 Wi-Fi P2P 场景。",
                        style = MaterialTheme.typography.bodySmall,
                        color = OnSurfaceLight
                    )
                }
            }
        }
    }
}

@Composable
private fun PermissionStateRadioItem(
    permissionState: PermissionState,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        RadioButton(
            selected = isSelected,
            onClick = onSelect,
            colors = RadioButtonDefaults.colors(
                selectedColor = Primary
            )
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = permissionState.displayName,
                style = MaterialTheme.typography.bodyMedium,
                color = OnSurfaceLight
            )
            Text(
                text = permissionState.description,
                style = MaterialTheme.typography.bodySmall,
                color = OnSurfaceDim
            )
        }
    }
}

// ===== Reusable Components =====
// ===== 可复用组件 =====

@Composable
private fun EmptyStateMessage(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = OnSurfaceDim,
                modifier = Modifier.size(48.dp)
            )
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = OnSurfaceLight
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = OnSurfaceDim
            )
        }
    }
}
