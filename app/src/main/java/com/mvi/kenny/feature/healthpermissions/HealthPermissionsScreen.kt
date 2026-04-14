package com.mvi.kenny.feature.healthpermissions

// ================================================================
// HealthPermissionsScreen — Android 16 健康权限主界面
// ================================================================
// Main screen for Android 16 Body Sensors migration toolkit.
//
// PRD-106: Android 16 细粒度健康权限迁移检测与合规工具包
// Design Reference: memory/agency/designs/PRD-106-Android-16-细粒度健康权限迁移检测与合规工具包.md
//
// Features:
//   1. Scanner Tab — Dashboard + scan trigger + scan history
//   2. Mapping Tab — Old → New permission mapping table with search
//   3. Settings Tab — Scanner configuration
//   4. Scan Detail Screen — Full compliance report display
// —————————————————————————————————————————————————————

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mvi.kenny.base.TopBarConfig
import kotlinx.coroutines.flow.collectLatest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// ================================================================
// 颜色规范 / Color Palette
// ================================================================

/** 主色 — Material3 Purple */
private val PrimaryColor = Color(0xFF6750A4)

/** 次要色 */
private val SecondaryColor = Color(0xFF625B71)

/** 表面色 */
private val SurfaceColor = Color(0xFFFFFBFE)

/** 背景色 */
private val BackgroundColor = Color(0xFFF7F2FA)

/** 健康度-优秀 */
private val ScoreExcellent = Color(0xFF4CAF50)

/** 健康度-警告 */
private val ScoreWarning = Color(0xFFFF9800)

/** 健康度-危险 */
private val ScoreDanger = Color(0xFFF44336)

/** 代码背景 */
private val CodeBackground = Color(0xFF1E1E1E)

/** 代码前景 */
private val CodeForeground = Color(0xFFD4D4D4)

// ================================================================
// 主界面入口 / Main Entry
// ================================================================

/**
 * ============================================================
 * HealthPermissionsScreen — 健康权限功能主界面
 * ============================================================
 * Entry point for the Health Permissions migration toolkit.
 * Contains 3 tabs: Scanner / Mapping / Settings.
 *
 * @param viewModel ViewModel instance
 * @param onUpdateTopBar TopBar configuration callback
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HealthPermissionsScreen(
    viewModel: HealthPermissionsViewModel = viewModel(),
    onUpdateTopBar: (TopBarConfig) -> Unit = {}
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val clipboardManager = LocalClipboardManager.current

    // ── Effect Handler ────────────────────────────────────────────
    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is HealthPermissionsEffect.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(effect.message)
                }
                is HealthPermissionsEffect.ShowError -> {
                    snackbarHostState.showSnackbar("错误: ${effect.message}")
                }
                is HealthPermissionsEffect.ScanComplete -> {
                    snackbarHostState.showSnackbar("扫描完成！发现 ${state.lastScanResult?.issueCounts?.total ?: 0} 个问题")
                }
                is HealthPermissionsEffect.NavigateToScanDetail -> {
                    // In a full impl, would navigate to detail screen
                    snackbarHostState.showSnackbar("查看扫描详情: ${effect.scanId}")
                }
                is HealthPermissionsEffect.ExportSuccess -> {
                    snackbarHostState.showSnackbar("报告已导出: ${effect.filePath}")
                }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(BackgroundColor)
        ) {
            // ── Tab Row ──────────────────────────────────────────────
            TabRow(
                selectedTabIndex = state.activeTab.ordinal,
                containerColor = SurfaceColor,
                contentColor = PrimaryColor
            ) {
                TabItem(
                    selected = state.activeTab == ActiveTab.Scanner,
                    onClick = { viewModel.sendIntent(HealthPermissionsIntent.SwitchTab(ActiveTab.Scanner)) },
                    icon = Icons.Default.Radar,
                    title = ActiveTab.Scanner.title
                )
                TabItem(
                    selected = state.activeTab == ActiveTab.Mapping,
                    onClick = { viewModel.sendIntent(HealthPermissionsIntent.SwitchTab(ActiveTab.Mapping)) },
                    icon = Icons.Default.TableChart,
                    title = ActiveTab.Mapping.title
                )
                TabItem(
                    selected = state.activeTab == ActiveTab.Settings,
                    onClick = { viewModel.sendIntent(HealthPermissionsIntent.SwitchTab(ActiveTab.Settings)) },
                    icon = Icons.Default.Settings,
                    title = ActiveTab.Settings.title
                )
            }

            // ── Tab Content ─────────────────────────────────────────
            when (state.activeTab) {
                ActiveTab.Scanner -> ScannerTab(
                    state = state,
                    onIntent = viewModel::sendIntent
                )
                ActiveTab.Mapping -> MappingTab(
                    state = state,
                    onIntent = viewModel::sendIntent,
                    clipboardManager = clipboardManager
                )
                ActiveTab.Settings -> SettingsTab(
                    state = state.settings,
                    onIntent = viewModel::sendIntent
                )
            }
        }
    }
}

// ================================================================
// Tab Item Component
// ================================================================

@Composable
private fun TabItem(
    selected: Boolean,
    onClick: () -> Unit,
    icon: ImageVector,
    title: String
) {
    Tab(
        selected = selected,
        onClick = onClick,
        icon = {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = if (selected) PrimaryColor else SecondaryColor
            )
        },
        text = {
            Text(
                text = title,
                color = if (selected) PrimaryColor else SecondaryColor,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
            )
        }
    )
}

// ================================================================
// Scanner Tab
// ================================================================

/**
 * ScannerTab — 扫描器 Tab
 *
 * Contains:
 *   1. RadarChart — 5-dimension compliance score
 *   2. ScanButton — CTA with pulse animation
 *   3. ScanProgress — Progress bar during scanning
 *   4. ScanHistory — Historical scan records
 *   5. ScanResultCard — Latest scan result summary
 */
@Composable
private fun ScannerTab(
    state: HealthPermissionsState,
    onIntent: (HealthPermissionsIntent) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // ── RadarChart Dashboard ───────────────────────────────────
        item {
            RadarChartCard(
                score = state.lastScanResult?.score ?: 0,
                issueCounts = state.lastScanResult?.issueCounts ?: IssueCounts()
            )
        }

        // ── Scan Button ────────────────────────────────────────────
        item {
            ScanButton(
                scanState = state.scanState,
                onStartScan = { onIntent(HealthPermissionsIntent.StartScan) },
                onCancelScan = { onIntent(HealthPermissionsIntent.CancelScan) }
            )
        }

        // ── Scan Progress ──────────────────────────────────────────
        if (state.scanState == ScanState.Scanning) {
            item {
                ScanProgressCard(
                    progress = state.scanProgress,
                    stepNumber = state.currentStepNumber,
                    stepDescription = state.scanProgressStepDescription
                )
            }
        }

        // ── Latest Scan Result Card ─────────────────────────────────
        state.lastScanResult?.let { result ->
            item {
                ScanResultCard(
                    result = result,
                    onRefresh = { onIntent(HealthPermissionsIntent.RefreshScan) },
                    onExport = { onIntent(HealthPermissionsIntent.ExportReport(ReportFormat.Markdown)) }
                )
            }
        }

        // ── Scan History ───────────────────────────────────────────
        if (state.scanHistory.isNotEmpty()) {
            item {
                Text(
                    text = "扫描历史",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            items(state.scanHistory) { record ->
                ScanHistoryCard(
                    record = record,
                    onClick = { onIntent(HealthPermissionsIntent.LoadScanDetail(record.id)) }
                )
            }
        }

        // ── Empty State ─────────────────────────────────────────────
        if (state.scanState == ScanState.Idle && state.lastScanResult == null && state.scanHistory.isEmpty()) {
            item {
                EmptyScanState()
            }
        }
    }
}

/**
 * ============================================================
 * RadarChartCard — 5 维度健康度雷达图
 * ============================================================
 * Displays compliance score across 5 dimensions:
 *   1. Manifest 声明合规
 *   2. 前台权限使用
 *   3. 后台权限使用
 *   4. Health Connect 整合
 *   5. SDK 兼容性
 *
 * @param score Overall compliance score (0-100)
 * @param issueCounts Issue count breakdown
 */
@Composable
private fun RadarChartCard(
    score: Int,
    issueCounts: IssueCounts
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SurfaceColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "健康权限合规度",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier.size(200.dp),
                contentAlignment = Alignment.Center
            ) {
                // Radar chart background
                RadarChartBackground()

                // Score display
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "$score",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = when {
                            score >= 80 -> ScoreExcellent
                            score >= 60 -> ScoreWarning
                            else -> ScoreDanger
                        }
                    )
                    Text(
                        text = "分",
                        style = MaterialTheme.typography.bodyMedium,
                        color = SecondaryColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Issue count badges
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                IssueBadge(emoji = "🔴", count = issueCounts.p0Count, label = "P0阻断")
                IssueBadge(emoji = "🟡", count = issueCounts.p1Count, label = "P1警告")
                IssueBadge(emoji = "🟢", count = issueCounts.p2Count, label = "P2建议")
            }
        }
    }
}

/**
 * RadarChartBackground — 雷达图背景绘制
 *
 * Draws a 5-axis radar chart with pentagon grid lines.
 * Each axis represents one compliance dimension.
 */
@Composable
private fun RadarChartBackground() {
    Canvas(modifier = Modifier.size(180.dp)) {
        val center = Offset(size.width / 2, size.height / 2)
        val radius = size.minDimension / 2 * 0.85f
        val angleStep = 360f / 5

        // Draw concentric pentagons (grid)
        listOf(0.25f, 0.5f, 0.75f, 1.0f).forEach { scale ->
            val path = Path()
            repeat(5) { i ->
                val angleRad = Math.toRadians((angleStep * i - 90).toDouble())
                val x = center.x + radius * scale * kotlin.math.cos(angleRad).toFloat()
                val y = center.y + radius * scale * kotlin.math.sin(angleRad).toFloat()
                if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }
            path.close()
            drawPath(
                path = path,
                color = Color(0xFFE0E0E0),
                style = Stroke(width = 1.dp.toPx())
            )
        }

        // Draw axis lines
        repeat(5) { i ->
            val angleRad = Math.toRadians((angleStep * i - 90).toDouble())
            val x = center.x + radius * kotlin.math.cos(angleRad).toFloat()
            val y = center.y + radius * kotlin.math.sin(angleRad).toFloat()
            drawLine(
                color = Color(0xFFE0E0E0),
                start = center,
                end = Offset(x, y),
                strokeWidth = 1.dp.toPx()
            )
        }

        // Draw filled data area (semi-transparent)
        val dataPath = Path()
        val dataValues = listOf(0.7f, 0.5f, 0.3f, 0.8f, 0.6f) // Mock data values
        dataValues.forEachIndexed { i, value ->
            val angleRad = Math.toRadians((angleStep * i - 90).toDouble())
            val x = center.x + radius * value * kotlin.math.cos(angleRad).toFloat()
            val y = center.y + radius * value * kotlin.math.sin(angleRad).toFloat()
            if (i == 0) dataPath.moveTo(x, y) else dataPath.lineTo(x, y)
        }
        dataPath.close()
        drawPath(
            path = dataPath,
            color = PrimaryColor.copy(alpha = 0.2f)
        )
        drawPath(
            path = dataPath,
            color = PrimaryColor,
            style = Stroke(width = 2.dp.toPx())
        )

        // Draw axis labels
        val labels = listOf("Manifest", "前台", "后台", "HealthConnect", "SDK兼容")
        labels.forEachIndexed { i, label ->
            val angleRad = Math.toRadians((angleStep * i - 90).toDouble())
            val x = center.x + (radius + 20.dp.toPx()) * kotlin.math.cos(angleRad).toFloat()
            val y = center.y + (radius + 20.dp.toPx()) * kotlin.math.sin(angleRad).toFloat()
            // Label positioning would need drawText, simplified here
        }
    }
}

@Composable
private fun IssueBadge(emoji: String, count: Int, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = emoji, fontSize = 20.sp)
        Text(
            text = "$count",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = SecondaryColor
        )
    }
}

/**
 * ============================================================
 * ScanButton — 主 CTA 扫描按钮
 * ============================================================
 *
 * States:
 *   - Idle: Primary button "开始扫描"
 *   - Scanning: Outlined button with cancel option
 *
 * @param scanState Current scan state
 * @param onStartScan Callback when start scan is clicked
 * @param onCancelScan Callback when cancel is clicked
 */
@Composable
private fun ScanButton(
    scanState: ScanState,
    onStartScan: () -> Unit,
    onCancelScan: () -> Unit
) {
    when (scanState) {
        ScanState.Idle, ScanState.Success, ScanState.Error -> {
            Button(
                onClick = onStartScan,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Radar,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "开始扫描",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        ScanState.Scanning -> {
            OutlinedButton(
                onClick = onCancelScan,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                    color = PrimaryColor
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "取消扫描",
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}

/**
 * ============================================================
 * ScanProgressCard — 扫描进度卡片
 * ============================================================
 *
 * @param progress Overall progress (0.0 ~ 1.0)
 * @param stepNumber Current step number (1-5)
 * @param stepDescription Current step description
 */
@Composable
private fun ScanProgressCard(
    progress: Float,
    stepNumber: Int,
    stepDescription: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SurfaceColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "正在扫描...",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "${(progress * 100).toInt()}%",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryColor
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = PrimaryColor,
                trackColor = PrimaryColor.copy(alpha = 0.2f)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "第 $stepNumber/5 步：$stepDescription",
                style = MaterialTheme.typography.bodyMedium,
                color = SecondaryColor
            )
        }
    }
}

/**
 * ============================================================
 * ScanResultCard — 最新扫描结果卡片
 * ============================================================
 */
@Composable
private fun ScanResultCard(
    result: ScanResult,
    onRefresh: () -> Unit,
    onExport: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SurfaceColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "最新扫描结果",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Row {
                    IconButton(onClick = onRefresh) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "刷新",
                            tint = PrimaryColor
                        )
                    }
                    IconButton(onClick = onExport) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "导出",
                            tint = PrimaryColor
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Score ring
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.size(80.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        progress = { result.score / 100f },
                        modifier = Modifier.fillMaxSize(),
                        strokeWidth = 8.dp,
                        color = when {
                            result.score >= 80 -> ScoreExcellent
                            result.score >= 60 -> ScoreWarning
                            else -> ScoreDanger
                        },
                        trackColor = Color(0xFFE0E0E0)
                    )
                    Text(
                        text = "${result.score}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(
                        text = "合规评分",
                        style = MaterialTheme.typography.bodyMedium,
                        color = SecondaryColor
                    )
                    Text(
                        text = formatTimestamp(result.timestamp),
                        style = MaterialTheme.typography.bodySmall,
                        color = SecondaryColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Issue summary
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                SeverityChip(severity = Severity.P0, count = result.issueCounts.p0Count)
                SeverityChip(severity = Severity.P1, count = result.issueCounts.p1Count)
                SeverityChip(severity = Severity.P2, count = result.issueCounts.p2Count)
            }

            // Declaration summary
            if (result.declarations.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Manifest 声明问题: ${result.declarations.size} 处",
                    style = MaterialTheme.typography.bodyMedium,
                    color = SecondaryColor
                )
            }

            // Code path summary
            if (result.codePaths.isNotEmpty()) {
                Text(
                    text = "代码路径问题: ${result.codePaths.size} 处",
                    style = MaterialTheme.typography.bodyMedium,
                    color = SecondaryColor
                )
            }

            // SDK conflict
            result.sdkReport?.let { report ->
                if (!report.isGoogleFitCompatible) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = ScoreWarning,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Google Fit SDK 兼容性问题",
                            style = MaterialTheme.typography.bodySmall,
                            color = ScoreWarning
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SeverityChip(severity: Severity, count: Int) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color(severity.colorHex).copy(alpha = 0.15f))
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = "${severity.emoji} ${count}个 ${severity.displayName}",
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium
        )
    }
}

/**
 * ============================================================
 * ScanHistoryCard — 扫描历史记录卡片
 * ============================================================
 */
@Composable
private fun ScanHistoryCard(
    record: ScanRecord,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (record.isNew) PrimaryColor.copy(alpha = 0.08f) else SurfaceColor
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
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
                    text = formatTimestamp(record.timestamp),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "P0:${record.issueCounts.p0Count} / P1:${record.issueCounts.p1Count} / P2:${record.issueCounts.p2Count}",
                    style = MaterialTheme.typography.bodySmall,
                    color = SecondaryColor
                )
            }
            Text(
                text = "${record.score}分",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = when {
                    record.score >= 80 -> ScoreExcellent
                    record.score >= 60 -> ScoreWarning
                    else -> ScoreDanger
                }
            )
        }
    }
}

/**
 * ============================================================
 * EmptyScanState — 空状态占位
 * ============================================================
 */
@Composable
private fun EmptyScanState() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SurfaceColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Shield,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = PrimaryColor.copy(alpha = 0.5f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "暂无扫描记录",
                style = MaterialTheme.typography.titleMedium,
                color = SecondaryColor
            )
            Text(
                text = "点击上方按钮开始扫描",
                style = MaterialTheme.typography.bodyMedium,
                color = SecondaryColor.copy(alpha = 0.7f)
            )
        }
    }
}

// ================================================================
// Mapping Tab
// ================================================================

/**
 * MappingTab — 权限映射 Tab
 *
 * Displays the BODY_SENSORS → 细粒度权限 mapping table.
 * Supports search by old permission name, new permission name, or description.
 */
@Composable
private fun MappingTab(
    state: HealthPermissionsState,
    onIntent: (HealthPermissionsIntent) -> Unit,
    clipboardManager: androidx.compose.ui.platform.ClipboardManager
) {
    var expandedMappingId by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Search bar
        OutlinedTextField(
            value = state.mappingSearchQuery,
            onValueChange = { onIntent(HealthPermissionsIntent.SearchMappings(it)) },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("搜索旧权限 / 新权限 / 场景...") },
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = null)
            },
            trailingIcon = {
                if (state.mappingSearchQuery.isNotEmpty()) {
                    IconButton(
                        onClick = { onIntent(HealthPermissionsIntent.SearchMappings("")) }
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "清除")
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Mapping count
        Text(
            text = "共 ${state.permissionMappings.size} 条映射",
            style = MaterialTheme.typography.bodySmall,
            color = SecondaryColor
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Mapping list
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(state.permissionMappings) { mapping ->
                PermissionMappingCard(
                    mapping = mapping,
                    isExpanded = expandedMappingId == mapping.oldPermission,
                    onToggle = {
                        expandedMappingId = if (expandedMappingId == mapping.oldPermission) null
                        else mapping.oldPermission
                    },
                    clipboardManager = clipboardManager
                )
            }
        }
    }
}

/**
 * ============================================================
 * PermissionMappingCard — 权限映射卡片
 * ============================================================
 */
@Composable
private fun PermissionMappingCard(
    mapping: PermissionMapping,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    clipboardManager: androidx.compose.ui.platform.ClipboardManager
) {
    Card(
        onClick = onToggle,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SurfaceColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = mapping.oldPermission,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = ScoreDanger
                    )
                    Text(
                        text = "↓",
                        style = MaterialTheme.typography.bodyLarge,
                        color = PrimaryColor
                    )
                    mapping.newPermissions.firstOrNull()?.let { first ->
                        Text(
                            text = first,
                            style = MaterialTheme.typography.bodySmall,
                            fontFamily = FontFamily.Monospace,
                            color = ScoreExcellent,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF6750A4).copy(alpha = 0.1f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "API ${mapping.apiLevel}",
                        style = MaterialTheme.typography.labelSmall,
                        color = PrimaryColor
                    )
                }
            }

            // Description
            Text(
                text = mapping.description,
                style = MaterialTheme.typography.bodySmall,
                color = SecondaryColor,
                modifier = Modifier.padding(top = 8.dp)
            )

            // Expanded: All new permissions
            AnimatedVisibility(visible = isExpanded) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    Text(
                        text = "对应新权限：",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Medium,
                        color = SecondaryColor
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    mapping.newPermissions.forEach { newPerm ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = newPerm,
                                style = MaterialTheme.typography.bodySmall,
                                fontFamily = FontFamily.Monospace,
                                color = ScoreExcellent,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(
                                onClick = {
                                    clipboardManager.setText(AnnotatedString(newPerm))
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = "复制",
                                    modifier = Modifier.size(16.dp),
                                    tint = PrimaryColor
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ================================================================
// Settings Tab
// ================================================================

/**
 * SettingsTab — 设置 Tab
 */
@Composable
private fun SettingsTab(
    state: HealthPermissionsSettings,
    onIntent: (HealthPermissionsIntent) -> Unit
) {
    var localSettings by remember { mutableStateOf(state) }

    LaunchedEffect(state) {
        localSettings = state
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "扫描配置",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        // Include test code
        SettingsSwitchItem(
            title = "包含测试代码",
            description = "扫描范围是否包含 androidTest / test 目录",
            checked = localSettings.includeTestCode,
            onCheckedChange = { checked ->
                val newSettings = localSettings.copy(includeTestCode = checked)
                localSettings = newSettings
                onIntent(HealthPermissionsIntent.UpdateSettings(newSettings))
            }
        )

        // Include AAR dependencies
        SettingsSwitchItem(
            title = "扫描 AAR 依赖",
            description = "是否分析第三方 AAR 库中的权限声明",
            checked = localSettings.includeAarDependencies,
            onCheckedChange = { checked ->
                val newSettings = localSettings.copy(includeAarDependencies = checked)
                localSettings = newSettings
                onIntent(HealthPermissionsIntent.UpdateSettings(newSettings))
            }
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "报告配置",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        // Export format
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SurfaceColor)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "默认导出格式",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ReportFormat.entries.forEach { format ->
                        val isSelected = localSettings.exportFormat == format
                        OutlinedButton(
                            onClick = {
                                val newSettings = localSettings.copy(exportFormat = format)
                                localSettings = newSettings
                                onIntent(HealthPermissionsIntent.UpdateSettings(newSettings))
                            },
                            colors = if (isSelected) ButtonDefaults.outlinedButtonColors(
                                containerColor = PrimaryColor.copy(alpha = 0.1f)
                            ) else ButtonDefaults.outlinedButtonColors(),
                            border = if (isSelected) androidx.compose.foundation.BorderStroke(
                                2.dp, PrimaryColor
                            ) else androidx.compose.foundation.BorderStroke(
                                1.dp, Color(0xFFE0E0E0)
                            )
                        ) {
                            Text(
                                text = format.displayName,
                                color = if (isSelected) PrimaryColor else SecondaryColor
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // About section
        Text(
            text = "关于",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SurfaceColor)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = PrimaryColor
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Android 16 细粒度健康权限迁移工具包",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "版本 1.0.0 — PRD-106",
                    style = MaterialTheme.typography.bodySmall,
                    color = SecondaryColor
                )
                Text(
                    text = "帮助开发者检测 BODY_SENSORS 废弃情况并完成向 Android 16 细粒度健康权限的迁移",
                    style = MaterialTheme.typography.bodySmall,
                    color = SecondaryColor
                )
            }
        }
    }
}

// ================================================================
// Settings Switch Item Component
// ================================================================

@Composable
private fun SettingsSwitchItem(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SurfaceColor)
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
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = SecondaryColor
                )
            }
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = androidx.compose.material3.SwitchDefaults.colors(
                    checkedThumbColor = PrimaryColor,
                    checkedTrackColor = PrimaryColor.copy(alpha = 0.5f)
                )
            )
        }
    }
}

// ================================================================
// Utility Functions
// ================================================================

/**
 * formatTimestamp — 格式化时间戳为可读字符串
 *
 * @param timestamp Milliseconds since epoch
 * @return Formatted string like "04-14 10:30"
 */
private fun formatTimestamp(timestamp: Long): String {
    val sdf = SimpleDateFormat("MM-dd HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

