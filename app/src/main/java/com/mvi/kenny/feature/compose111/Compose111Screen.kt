package com.mvi.kenny.feature.compose111

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.VerticalDivider
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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mvi.kenny.base.TopBarAction
import com.mvi.kenny.base.TopBarConfig
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.min as kotlinMin

/**
 * ============================================================
 * Compose111Screen — Compose 1.11 破坏性变更检测主界面
 * ============================================================
 * PRD-130 | Compose 1.11.0-rc01 破坏性变更检测与迁移工具包
 *
 * 6-Tab 工具面板:
 * 0. Dashboard — 健康度总览 + 雷达图
 * 1. TextPadding — Text extra padding 检测与修复
 * 2. DrawLayer — DrawLayer API 重命名扫描
 * 3. SwipeToReveal — SwipeToReveal 迁移向导
 * 4. Report — 合规报告生成 + CI 配置
 * 5. Preview — 修复前后对比预览
 *
 * @param onUpdateTopBar TopBar 配置更新回调
 * @param viewModel Compose111ViewModel 实例
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Compose111Screen(
    onUpdateTopBar: (TopBarConfig) -> Unit,
    viewModel: Compose111ViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val pagerState = rememberPagerState(pageCount = { 6 })

    // Tab titles / Tab 标题
    val tabTitles = listOf(
        "总览" to Icons.Default.BugReport,
        "Text" to Icons.Default.Warning,
        "DrawLayer" to Icons.Default.BugReport,
        "SwipeToReveal" to Icons.Default.Refresh,
        "报告" to Icons.Default.Share,
        "预览" to Icons.Default.ContentCopy
    )

    // 监听 Effect / Listen to effects
    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is Compose111Effect.CopiedToClipboard -> {
                    Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                }
                is Compose111Effect.ShowFixSuggestion -> {
                    Toast.makeText(context, effect.suggestion, Toast.LENGTH_SHORT).show()
                }
                is Compose111Effect.ScanComplete -> {
                    Toast.makeText(context, "Scan complete", Toast.LENGTH_SHORT).show()
                }
                is Compose111Effect.ShowError -> {
                    Toast.makeText(context, effect.message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    // 更新 TopBar / Update TopBar
    LaunchedEffect(state.selectedTab) {
        val config = TopBarConfig(
            title = "Compose 1.11 破坏性变更",
            actions = if (state.isScanning) emptyList() else listOf(
                TopBarAction(
                    icon = Icons.Default.Refresh,
                    contentDescription = "Refresh scan",
                    onClick = { viewModel.processIntent(Compose111Intent.RefreshScan) }
                )
            )
        )
        onUpdateTopBar(config)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Tab 导航 / Tab navigation
        ScrollableTabRow(
            selectedTabIndex = pagerState.currentPage,
            edgePadding = 0.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            tabTitles.forEachIndexed { index, (title, icon) ->
                Tab(
                    selected = pagerState.currentPage == index,
                    onClick = { /* handled by pager */ },
                    text = { Text(title, fontSize = 12.sp) },
                    icon = {
                        Box {
                            Icon(icon, contentDescription = title, modifier = Modifier.size(20.dp))
                            val count = when (index) {
                                1 -> state.textPaddingIssues.count { !it.isFixed }
                                2 -> state.drawLayerIssues.count { !it.isFixed }
                                3 -> state.swipeToRevealIssues.count { !it.isMigrated }
                                else -> 0
                            }
                            if (count > 0) {
                                Badge(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .offset(x = 6.dp, y = (-2).dp)
                                ) {
                                    Text(count.toString(), fontSize = 9.sp)
                                }
                            }
                        }
                    }
                )
            }
        }

        // 扫描进度条 / Scan progress bar
        if (state.isScanning) {
            LinearProgressIndicator(
                progress = { state.scanProgress },
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Pager 内容 / Pager content
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            when (page) {
                Compose111Tab.DASHBOARD -> DashboardTabContent(
                    state = state,
                    onStartScan = { viewModel.processIntent(Compose111Intent.StartScan) },
                    onNavigateToTab = { /* handled externally */ }
                )
                Compose111Tab.TEXT_PADDING -> TextPaddingTabContent(
                    state = state,
                    onFix = { viewModel.processIntent(Compose111Intent.FixTextPadding(it)) },
                    onFilter = { viewModel.processIntent(Compose111Intent.FilterBySeverity(it)) }
                )
                Compose111Tab.DRAW_LAYER -> DrawLayerTabContent(
                    state = state,
                    onFix = { viewModel.processIntent(Compose111Intent.FixDrawLayer(it)) }
                )
                Compose111Tab.SWIPE_TO_REVEAL -> SwipeToRevealTabContent(
                    state = state,
                    onMigrate = { viewModel.processIntent(Compose111Intent.MigrateSwipeToReveal(it)) }
                )
                Compose111Tab.REPORT -> ReportTabContent(
                    state = state,
                    onGenerateReport = { viewModel.processIntent(Compose111Intent.GenerateReport) },
                    onGenerateCi = { viewModel.processIntent(Compose111Intent.GenerateCiConfig) },
                    onSelectPlatform = { viewModel.processIntent(Compose111Intent.SelectCiPlatform(it)) },
                    context = context
                )
                Compose111Tab.PREVIEW -> PreviewTabContent(
                    state = state,
                    onToggle = { viewModel.processIntent(Compose111Intent.TogglePreviewMode) }
                )
            }
        }
    }
}

// ================================================================
// Dashboard Tab / 总览 Tab
// ================================================================

/**
 * Dashboard — 健康度总览 + 雷达图
 * Health overview with radar chart and severity summary cards
 */
@Composable
private fun DashboardTabContent(
    state: Compose111State,
    onStartScan: () -> Unit,
    onNavigateToTab: (Int) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 雷达图 / Radar chart
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Compose 1.11 健康度",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    // Radar chart / 雷达图
                    Box(
                        modifier = Modifier.size(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        RadarChart(
                            scores = listOf(
                                state.textPaddingScore,
                                state.drawLayerScore,
                                state.swipeToRevealScore
                            ),
                            labels = listOf("Text\nPadding", "DrawLayer\nAPI", "Swipe\nToReveal"),
                            modifier = Modifier.fillMaxSize()
                        )
                        // Center score / 中心分数
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                "${state.overallHealthScore.toInt()}",
                                fontSize = 36.sp,
                                fontWeight = FontWeight.Bold,
                                color = scoreColor(state.overallHealthScore)
                            )
                            Text("综合健康度", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }

        // 三大变更健康度卡片 / Three change health score cards
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                HealthScoreCard(
                    title = "Text Padding",
                    score = state.textPaddingScore,
                    critical = state.textPaddingIssues.count { it.severity == Compose111Severity.CRITICAL && !it.isFixed },
                    warning = state.textPaddingIssues.count { it.severity == Compose111Severity.WARNING && !it.isFixed },
                    onClick = { onNavigateToTab(Compose111Tab.TEXT_PADDING) },
                    modifier = Modifier.weight(1f)
                )
                HealthScoreCard(
                    title = "DrawLayer",
                    score = state.drawLayerScore,
                    critical = state.drawLayerIssues.count { !it.isFixed },
                    warning = 0,
                    onClick = { onNavigateToTab(Compose111Tab.DRAW_LAYER) },
                    modifier = Modifier.weight(1f)
                )
                HealthScoreCard(
                    title = "SwipeToReveal",
                    score = state.swipeToRevealScore,
                    critical = state.swipeToRevealIssues.count { !it.isMigrated },
                    warning = 0,
                    onClick = { onNavigateToTab(Compose111Tab.SWIPE_TO_REVEAL) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // 严重性统计 / Severity summary
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "破坏性变更统计",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        SeverityCountItem(
                            label = "Critical",
                            count = state.criticalCount,
                            color = MaterialTheme.colorScheme.error
                        )
                        SeverityCountItem(
                            label = "Warning",
                            count = state.warningCount,
                            color = Color(0xFFFFC107)
                        )
                        SeverityCountItem(
                            label = "Pass",
                            count = state.passCount,
                            color = Color(0xFF4CAF50)
                        )
                    }
                }
            }
        }

        // 扫描按钮 / Scan button
        item {
            Button(
                onClick = onStartScan,
                modifier = Modifier.fillMaxWidth(),
                enabled = !state.isScanning
            ) {
                if (state.isScanning) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("扫描中... ${(state.scanProgress * 100).toInt()}%")
                } else {
                    Icon(Icons.Default.Refresh, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("重新扫描")
                }
            }
        }
    }
}

/**
 * 健康度分数卡片 / Health score card
 */
@Composable
private fun HealthScoreCard(
    title: String,
    score: Float,
    critical: Int,
    warning: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(title, fontSize = 11.sp, fontWeight = FontWeight.Medium, maxLines = 1)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "${score.toInt()}",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = scoreColor(score)
            )
            Text("健康度", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            if (critical > 0) {
                Spacer(modifier = Modifier.height(4.dp))
                Text("$critical 🔴", fontSize = 11.sp)
            }
            if (warning > 0) {
                Text("$warning 🟡", fontSize = 11.sp)
            }
        }
    }
}

/**
 * 严重性计数项 / Severity count item
 */
@Composable
private fun SeverityCountItem(
    label: String,
    count: Int,
    color: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            count.toString(),
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

// ================================================================
// Text Padding Tab / Text Padding 检测 Tab
// ================================================================

/**
 * Text Padding 检测与修复引导内容
 * Text padding detection and fix guidance content
 */
@Composable
private fun TextPaddingTabContent(
    state: Compose111State,
    onFix: (Int) -> Unit,
    onFilter: (Compose111Severity?) -> Unit
) {
    var selectedIssue by remember { mutableStateOf<TextPaddingIssue?>(null) }

    Column(modifier = Modifier.fillMaxSize()) {
        // Filter chips / 过滤芯片
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                FilterChip(
                    selected = state.selectedSeverityFilter == null,
                    onClick = { onFilter(null) },
                    label = { Text("全部") }
                )
            }
            item {
                FilterChip(
                    selected = state.selectedSeverityFilter == Compose111Severity.CRITICAL,
                    onClick = { onFilter(Compose111Severity.CRITICAL) },
                    label = { Text("Critical") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.errorContainer
                    )
                )
            }
            item {
                FilterChip(
                    selected = state.selectedSeverityFilter == Compose111Severity.WARNING,
                    onClick = { onFilter(Compose111Severity.WARNING) },
                    label = { Text("Warning") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFFFFC107).copy(alpha = 0.3f)
                    )
                )
            }
        }

        val filtered = state.textPaddingIssues.filter {
            state.selectedSeverityFilter == null || it.severity == state.selectedSeverityFilter
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            itemsIndexed(filtered) { index, issue ->
                TextPaddingIssueCard(
                    issue = issue,
                    originalIndex = state.textPaddingIssues.indexOf(issue),
                    onFix = { onFix(state.textPaddingIssues.indexOf(issue)) },
                    onClick = { selectedIssue = issue }
                )
            }
        }
    }

    // Fix suggestion bottom sheet / 修复建议弹窗
    selectedIssue?.let { issue ->
        AlertDialog(
            onDismissRequest = { selectedIssue = null },
            title = { Text("Text Padding 修复建议") },
            text = {
                Column {
                    Text("文件: ${issue.filePath}:${issue.lineNumber}", style = MaterialTheme.typography.bodySmall)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("代码片段:", fontWeight = FontWeight.Bold)
                    Text(issue.codeSnippet, style = MaterialTheme.typography.bodySmall, fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("建议修复:", fontWeight = FontWeight.Bold)
                    Text(issue.suggestedFix)
                }
            },
            confirmButton = {
                TextButton(onClick = { selectedIssue = null }) {
                    Text("关闭")
                }
            }
        )
    }
}

/**
 * Text Padding 问题卡片 / Text padding issue card
 */
@Composable
private fun TextPaddingIssueCard(
    issue: TextPaddingIssue,
    originalIndex: Int,
    onFix: () -> Unit,
    onClick: () -> Unit
) {
    val severityColor = when (issue.severity) {
        Compose111Severity.CRITICAL -> MaterialTheme.colorScheme.error
        Compose111Severity.WARNING -> Color(0xFFFFC107)
        Compose111Severity.PASS -> Color(0xFF4CAF50)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = severityColor.copy(alpha = 0.08f)
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        when (issue.severity) {
                            Compose111Severity.CRITICAL -> Icons.Default.Error
                            Compose111Severity.WARNING -> Icons.Default.Warning
                            Compose111Severity.PASS -> Icons.Default.Check
                        },
                        contentDescription = null,
                        tint = severityColor,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        issue.severity.name,
                        color = severityColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
                if (!issue.isFixed) {
                    TextButton(onClick = onFix) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("已修复", fontSize = 12.sp)
                    }
                } else {
                    Text("✅ 已修复", fontSize = 12.sp, color = Color(0xFF4CAF50))
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "${issue.filePath}:${issue.lineNumber}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                issue.codeSnippet,
                style = MaterialTheme.typography.bodySmall,
                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

// ================================================================
// DrawLayer Tab / DrawLayer 检测 Tab
// ================================================================

/**
 * DrawLayer API 重命名扫描内容
 * DrawLayer API rename scan content
 */
@Composable
private fun DrawLayerTabContent(
    state: Compose111State,
    onFix: (Int) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        itemsIndexed(state.drawLayerIssues) { index, issue ->
            DrawLayerIssueCard(
                issue = issue,
                onFix = { onFix(index) }
            )
        }
    }
}

/**
 * DrawLayer 问题卡片（Before/After Diff 视图）
 * DrawLayer issue card with Before/After diff view
 */
@Composable
private fun DrawLayerIssueCard(
    issue: DrawLayerIssue,
    onFix: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "${issue.oldApi} → ${issue.newApi}",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error
                )
                if (!issue.isFixed) {
                    Button(onClick = onFix, contentPadding = PaddingValues(horizontal = 12.dp)) {
                        Text("修复", fontSize = 12.sp)
                    }
                } else {
                    Text("✅ 已修复", color = Color(0xFF4CAF50))
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "${issue.filePath}:${issue.lineNumber}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Before/After 对比 / Before/After diff
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("修复前", fontSize = 11.sp, color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        issue.codeSnippet.replace(issue.oldApi, "~~${issue.oldApi}~~"),
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.error
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text("修复后", fontSize = 11.sp, color = Color(0xFF4CAF50), fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        issue.codeSnippet.replace(issue.oldApi, issue.newApi),
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                        color = Color(0xFF4CAF50)
                    )
                }
            }
        }
    }
}

// ================================================================
// SwipeToReveal Tab / SwipeToReveal 迁移 Tab
// ================================================================

/**
 * SwipeToReveal 迁移向导内容
 * SwipeToReveal migration wizard content
 */
@Composable
private fun SwipeToRevealTabContent(
    state: Compose111State,
    onMigrate: (Int) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        itemsIndexed(state.swipeToRevealIssues) { index, issue ->
            SwipeToRevealMigrationCard(
                issue = issue,
                onMigrate = { onMigrate(index) }
            )
        }
    }
}

/**
 * SwipeToReveal 迁移卡片（Step-by-Step 向导）
 * SwipeToReveal migration card with step-by-step wizard
 */
@Composable
private fun SwipeToRevealMigrationCard(
    issue: SwipeToRevealIssue,
    onMigrate: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "迁移步骤 ${issue.migrationStep}/3",
                    fontWeight = FontWeight.Bold
                )
                if (!issue.isMigrated) {
                    Button(onClick = onMigrate, contentPadding = PaddingValues(horizontal = 12.dp)) {
                        Text(if (issue.migrationStep < 3) "下一步" else "完成", fontSize = 12.sp)
                    }
                } else {
                    Text("✅ 已迁移", color = Color(0xFF4CAF50))
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "${issue.filePath}:${issue.lineNumber}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Migration steps indicator / 迁移步骤指示器
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                repeat(3) { step ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(
                                if (step < issue.migrationStep) Color(0xFF4CAF50)
                                else MaterialTheme.colorScheme.surfaceVariant
                            )
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text("旧版用法:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
            Text(
                issue.oldUsage,
                style = MaterialTheme.typography.bodySmall,
                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f),
                        RoundedCornerShape(4.dp)
                    )
                    .padding(8.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text("新版 slot-based 用法:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
            Text(
                issue.newUsage,
                style = MaterialTheme.typography.bodySmall,
                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                color = Color(0xFF4CAF50),
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Color(0xFF4CAF50).copy(alpha = 0.1f),
                        RoundedCornerShape(4.dp)
                    )
                    .padding(8.dp)
            )
        }
    }
}

// ================================================================
// Report Tab / 报告生成 Tab
// ================================================================

/**
 * 报告生成内容
 * Report generation content
 */
@Composable
private fun ReportTabContent(
    state: Compose111State,
    onGenerateReport: () -> Unit,
    onGenerateCi: () -> Unit,
    onSelectPlatform: (CiPlatform) -> Unit,
    context: Context
) {
    var showReport by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Markdown 报告生成 / Markdown report generation
        item {
            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("📋 合规报告", fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "生成 Compose 1.11 破坏性变更 Markdown 格式合规报告，包含所有问题的严重性和修复建议。",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = {
                            onGenerateReport()
                            showReport = true
                        }) {
                            Text("生成报告")
                        }
                        if (state.reportMarkdown.isNotEmpty()) {
                            IconButton(onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                clipboard.setPrimaryClip(ClipData.newPlainText("report", state.reportMarkdown))
                                Toast.makeText(context, "Report copied", Toast.LENGTH_SHORT).show()
                            }) {
                                Icon(Icons.Default.ContentCopy, contentDescription = "Copy")
                            }
                        }
                    }
                }
            }
        }

        // CI 配置生成 / CI config generation
        item {
            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("⚙️ CI 配置", fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))

                    // CI 平台选择 / CI platform selection
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        CiPlatform.entries.forEach { platform ->
                            FilterChip(
                                selected = state.selectedCiPlatform == platform,
                                onClick = { onSelectPlatform(platform) },
                                label = { Text(platform.displayName) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Button(onClick = onGenerateCi) {
                        Text("生成 ${state.selectedCiPlatform.displayName} 配置")
                    }
                }
            }
        }

        // CI YAML 显示 / CI YAML display
        if (state.ciConfigYaml.isNotEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("CI YAML", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            IconButton(onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                clipboard.setPrimaryClip(ClipData.newPlainText("ci_yaml", state.ciConfigYaml))
                                Toast.makeText(context, "YAML copied", Toast.LENGTH_SHORT).show()
                            }) {
                                Icon(Icons.Default.ContentCopy, contentDescription = "Copy YAML", modifier = Modifier.size(18.dp))
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            state.ciConfigYaml,
                            style = MaterialTheme.typography.bodySmall,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                        )
                    }
                }
            }
        }

        // Markdown 报告显示 / Markdown report display
        if (showReport && state.reportMarkdown.isNotEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Markdown 报告", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            IconButton(onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                clipboard.setPrimaryClip(ClipData.newPlainText("markdown", state.reportMarkdown))
                                Toast.makeText(context, "Report copied", Toast.LENGTH_SHORT).show()
                            }) {
                                Icon(Icons.Default.ContentCopy, contentDescription = "Copy Report", modifier = Modifier.size(18.dp))
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            state.reportMarkdown,
                            style = MaterialTheme.typography.bodySmall,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }
    }
}

// ================================================================
// Preview Tab / 修复前后对比预览 Tab
// ================================================================

/**
 * 修复前后对比预览内容
 * Before/After preview content
 */
@Composable
private fun PreviewTabContent(
    state: Compose111State,
    onToggle: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            // 预览模式切换 / Preview mode toggle
            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("🔍 Text Padding 布局对比", fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "模拟 Compose 1.11 Text extra padding 移除前后的布局差异。" +
                                "额外 padding（首行顶部 + 末行底部）约 4-6dp，移除后高度会突变。",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            if (state.previewFixMode) "当前: 修复后（1.11+）" else "当前: 修复前（1.11 前）",
                            fontWeight = FontWeight.Medium
                        )
                        Button(onClick = onToggle) {
                            Text(if (state.previewFixMode) "查看修复前" else "查看修复后")
                        }
                    }
                }
            }
        }

        // Text 布局模拟 / Text layout simulation
        item {
            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("多行文本", fontWeight = FontWeight.Bold)
                        if (state.previewFixMode) {
                            Text("✅ 已修复", color = Color(0xFF4CAF50), fontSize = 12.sp)
                        } else {
                            Text("⚠️ 修复前（1.11 前）", color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))

                    // 模拟 Text 组件（修复前：额外 padding 增加高度约 8dp）
                            val extraPadding = if (state.previewFixMode) 0.dp else 8.dp
                            Column {
                                Text(
                                    "这是一段多行文本，用于模拟 Text extra padding 的视觉效果。Compose 1.11 移除了 Text 组件首行顶部和末行底部的额外 padding。",
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(
                                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                                            RoundedCornerShape(4.dp)
                                        )
                                        .padding(
                                            top = 8.dp + extraPadding,
                                            bottom = 8.dp + extraPadding,
                                            start = 8.dp,
                                            end = 8.dp
                                        )
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    "这是另一段多行文本，用于展示修复前后的高度差异。额外 padding 移除后，整体高度会减少约 $extraPadding。",
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(
                                            MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f),
                                            RoundedCornerShape(4.dp)
                                        )
                                        .padding(
                                            top = 8.dp + extraPadding,
                                            bottom = 8.dp + extraPadding,
                                            start = 8.dp,
                                            end = 8.dp
                                        )
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "紫色背景 = 首行顶部额外 padding; 绿色背景 = 末行底部额外 padding\n" +
                                        "修复后：padding 完全移除，高度减少 ~8dp",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                }
            }
        }
    }
}

// ================================================================
// Radar Chart / 雷达图组件
// ================================================================

/**
 * 雷达图组件 / Radar chart component
 *
 * 绘制三条轴的健康度得分，用于展示三大破坏性变更的整体健康度。
 *
 * @param scores 三个得分（0-100）
 * @param labels 三个标签
 * @param modifier Compose modifier
 */
@Composable
private fun RadarChart(
    scores: List<Float>,
    labels: List<String>,
    modifier: Modifier = Modifier
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant

    Canvas(modifier = modifier) {
        val centerX = size.width / 2
        val centerY = size.height / 2
        val radius = kotlinMin(centerX, centerY) * 0.7f
        val angleStep = (2 * Math.PI / 3).toFloat()
        val startAngle = -Math.PI.toFloat() / 2 // Start from top

        val points = scores.mapIndexed { index, score ->
            val angle = startAngle + index * angleStep
            val normalizedScore = (score / 100f).coerceIn(0f, 1f)
            Pair(
                centerX + radius * normalizedScore * cos(angle),
                centerY + radius * normalizedScore * sin(angle)
            )
        }

        val axisPoints = (0..2).map { index ->
            val angle = startAngle + index * angleStep
            Pair(
                centerX + radius * cos(angle),
                centerY + radius * sin(angle)
            )
        }

        // Draw background hexagon / 绘制背景六边形
        val bgPath = androidx.compose.ui.graphics.Path().apply {
            axisPoints.forEachIndexed { index, point ->
                if (index == 0) moveTo(point.first, point.second)
                else lineTo(point.first, point.second)
            }
            close()
        }
        drawPath(bgPath, surfaceVariant, style = Stroke(2.dp.toPx()))

        // Draw inner rings / 绘制内圈
        listOf(0.33f, 0.66f).forEach { ratio ->
            val ringPoints = (0..2).map { index ->
                val angle = startAngle + index * angleStep
                Pair(
                    centerX + radius * ratio * cos(angle),
                    centerY + radius * ratio * sin(angle)
                )
            }
            val ringPath = androidx.compose.ui.graphics.Path().apply {
                ringPoints.forEachIndexed { index, point ->
                    if (index == 0) moveTo(point.first, point.second)
                    else lineTo(point.first, point.second)
                }
                close()
            }
            drawPath(ringPath, surfaceVariant.copy(alpha = 0.5f), style = Stroke(1.dp.toPx()))
        }

        // Draw axes / 绘制轴线
        axisPoints.forEach { point ->
            drawLine(
                color = surfaceVariant,
                start = Offset(centerX, centerY),
                end = Offset(point.first, point.second),
                strokeWidth = 1.dp.toPx()
            )
        }

        // Draw score polygon / 绘制得分多边形
        if (scores.isNotEmpty()) {
            val scorePath = androidx.compose.ui.graphics.Path().apply {
                points.forEachIndexed { index, point ->
                    if (index == 0) moveTo(point.first, point.second)
                    else lineTo(point.first, point.second)
                }
                close()
            }
            drawPath(
                scorePath,
                primaryColor.copy(alpha = 0.3f)
            )
            drawPath(
                scorePath,
                primaryColor,
                style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
            )

            // Draw score points / 绘制得分点
            points.forEach { point ->
                drawCircle(
                    color = primaryColor,
                    radius = 4.dp.toPx(),
                    center = Offset(point.first, point.second)
                )
            }
        }
    }

    // Labels / 标签（叠加在 Canvas 上）
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        // Labels are drawn separately via drawIntoCanvas if needed
        // For simplicity, we use Text composables positioned around the chart
    }
}

// ================================================================
// Utility functions / 工具函数
// ================================================================

/**
 * 根据健康度返回颜色 / Return color based on health score
 *
 * > 80: 绿色 (safe)
 * 50-80: 琥珀色 (warning)
 * < 50: 红色 (critical)
 */
@Composable
private fun scoreColor(score: Float): Color = when {
    score >= 80 -> Color(0xFF4CAF50) // Green
    score >= 50 -> Color(0xFFFFC107) // Amber
    else -> MaterialTheme.colorScheme.error // Red
}
