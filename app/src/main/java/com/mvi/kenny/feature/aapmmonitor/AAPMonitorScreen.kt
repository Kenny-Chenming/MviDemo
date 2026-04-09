package com.mvi.kenny.feature.aapmmonitor

/**
 * ============================================================
 * AAPMonitorScreen.kt — 主界面 UI
 * AAPMonitorScreen.kt — Main Screen UI
 * ============================================================
 * 深色主题的 AAPM 检测与合规工具界面
 * Dark theme AAPM detection and compliance tool UI
 */

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// ============================================================
// Theme Colors — 主题颜色
// ============================================================

private object AAPMColors {
    // 背景色
    val Background = Color(0xFF1A1A2E)          // 深空蓝黑
    val CardBackground = Color(0xFF16213E)     // 深蓝卡片
    val SurfaceVariant = Color(0xFF1F2B47)     // 表面变体

    // 严重程度颜色
    val P0Color = Color(0xFFE94560)             // 警示红
    val P1Color = Color(0xFFF39C12)            // 橙色
    val P2Color = Color(0xFFF1C40F)            // 黄色
    val SafeColor = Color(0xFF27AE60)          // 安全绿

    // 文字颜色
    val TextPrimary = Color(0xFFFFFFFF)         // 主文字
    val TextSecondary = Color(0xFFA0A0B0)      // 次文字
    val TextTertiary = Color(0xFF6B6B80)       // 三级文字

    // AAPM 状态颜色
    val AAPMActive = Color(0xFFE94560)         // AAPM 激活（红）
    val AAPMInactive = Color(0xFF27AE60)       // AAPM 未激活（绿）
    val AAPMUnknown = Color(0xFFF39C12)       // 未知（橙）
}

// ============================================================
// AAPMonitorScreen — 主界面
// AAPMonitorScreen — Main Screen
// ============================================================

/**
 * AAPMonitor 主界面
 * AAPMonitor main screen
 *
 * @param state 当前状态
 * @param onIntent 发送意图回调
 */
@Composable
fun AAPMonitorScreen(
    state: AAPMonitorState,
    onIntent: (AAPMonitorIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("仪表盘", "扫描结果", "AAPM监控", "合规引导", "知识库")

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AAPMColors.Background)
    ) {
        // Tab 切换
        // Tab Switcher
        ScrollableTabRow(
            selectedTabIndex = selectedTab,
            containerColor = AAPMColors.CardBackground,
            contentColor = AAPMColors.TextPrimary,
            edgePadding = 16.dp,
            divider = {}
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = {
                        Text(
                            text = title,
                            color = if (selectedTab == index) AAPMColors.TextPrimary else AAPMColors.TextSecondary
                        )
                    }
                )
            }
        }

        // Tab 内容
        // Tab Content
        Box(modifier = Modifier.fillMaxSize()) {
            when (selectedTab) {
                0 -> DashboardTab(state, onIntent)
                1 -> ScanResultsTab(state, onIntent)
                2 -> AAPMMonitorTab(state, onIntent)
                3 -> ComplianceGuideTab(state, onIntent)
                4 -> KnowledgeBaseTab(state, onIntent)
            }
        }
    }
}

// ============================================================
// Dashboard Tab — 仪表盘 Tab
// ============================================================

@Composable
private fun DashboardTab(state: AAPMonitorState, onIntent: (AAPMonitorIntent) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 标题
        Text(
            text = "AAPM 检测仪表盘",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = AAPMColors.TextPrimary
        )

        // 扫描状态卡片
        // Scan Status Card
        ScanStatusCard(state, onIntent)

        // 快速统计
        // Quick Stats
        QuickStatsRow(state)

        // 快捷操作
        // Quick Actions
        QuickActionsCard(state, onIntent)

        // 最近问题预览
        // Recent Issues Preview
        if (state.scanResults.isNotEmpty()) {
            RecentIssuesPreview(state, onIntent)
        }
    }
}

@Composable
private fun ScanStatusCard(state: AAPMonitorState, onIntent: (AAPMonitorIntent) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = AAPMColors.CardBackground),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when (state.scanStatus) {
                ScanStatus.IDLE -> {
                    Icon(
                        imageVector = Icons.Default.PlayCircle,
                        contentDescription = null,
                        tint = AAPMColors.TextSecondary,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "点击开始扫描",
                        color = AAPMColors.TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "检测项目中的 AccessibilityService 使用情况",
                        color = AAPMColors.TextSecondary,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { onIntent(AAPMonitorIntent.StartScan) },
                        colors = ButtonDefaults.buttonColors(containerColor = AAPMColors.SafeColor)
                    ) {
                        Icon(Icons.Default.Search, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("开始扫描")
                    }
                }

                ScanStatus.SCANNING -> {
                    Box(contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(
                            progress = { state.scanProgress },
                            modifier = Modifier.size(80.dp),
                            color = AAPMColors.P1Color,
                            strokeWidth = 8.dp,
                            trackColor = AAPMColors.SurfaceVariant
                        )
                        Text(
                            text = "${(state.scanProgress * 100).toInt()}%",
                            color = AAPMColors.TextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "正在扫描...",
                        color = AAPMColors.TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "分析 AccessibilityService API 调用",
                        color = AAPMColors.TextSecondary,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedButton(
                        onClick = { onIntent(AAPMonitorIntent.CancelScan) }
                    ) {
                        Text("取消")
                    }
                }

                ScanStatus.DONE -> {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = AAPMColors.SafeColor,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "扫描完成",
                        color = AAPMColors.TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "发现 ${state.unresolvedCount} 个问题",
                        color = if (state.unresolvedCount > 0) AAPMColors.P0Color else AAPMColors.SafeColor,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { onIntent(AAPMonitorIntent.StartScan) },
                        colors = ButtonDefaults.buttonColors(containerColor = AAPMColors.SurfaceVariant)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("重新扫描")
                    }
                }

                ScanStatus.ERROR -> {
                    Icon(
                        imageVector = Icons.Default.Error,
                        contentDescription = null,
                        tint = AAPMColors.P0Color,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "扫描出错",
                        color = AAPMColors.TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = state.error ?: "未知错误",
                        color = AAPMColors.P0Color,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun QuickStatsRow(state: AAPMonitorState) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StatCard(
            title = "P0 问题",
            value = "${state.p0Count}",
            color = AAPMColors.P0Color,
            modifier = Modifier.weight(1f)
        )
        StatCard(
            title = "P1 问题",
            value = "${state.p1Count}",
            color = AAPMColors.P1Color,
            modifier = Modifier.weight(1f)
        )
        StatCard(
            title = "P2 问题",
            value = "${state.p2Count}",
            color = AAPMColors.P2Color,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun StatCard(title: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = AAPMColors.CardBackground),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = title,
                fontSize = 12.sp,
                color = AAPMColors.TextSecondary
            )
        }
    }
}

@Composable
private fun QuickActionsCard(state: AAPMonitorState, onIntent: (AAPMonitorIntent) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = AAPMColors.CardBackground),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "快捷操作",
                color = AAPMColors.TextPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ActionButton(
                    icon = Icons.Default.Share,
                    label = "导出报告",
                    onClick = { onIntent(AAPMonitorIntent.ExportReport) },
                    modifier = Modifier.weight(1f)
                )
                ActionButton(
                    icon = Icons.Default.Refresh,
                    label = "刷新AAPM",
                    onClick = { onIntent(AAPMonitorIntent.RefreshAapmStatus) },
                    modifier = Modifier.weight(1f)
                )
                ActionButton(
                    icon = Icons.Default.Book,
                    label = "知识库",
                    onClick = { /* Navigate to knowledge base tab */ },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun ActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier,
        colors = ButtonDefaults.outlinedButtonColors(contentColor = AAPMColors.TextPrimary),
        border = BorderStroke(1.dp, AAPMColors.TextTertiary)
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Text(label, fontSize = 12.sp)
    }
}

@Composable
private fun RecentIssuesPreview(state: AAPMonitorState, onIntent: (AAPMonitorIntent) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = AAPMColors.CardBackground),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "最近问题",
                    color = AAPMColors.TextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
                TextButton(onClick = { /* Navigate to scan results */ }) {
                    Text("查看全部", color = AAPMColors.SafeColor)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            state.scanResults.take(3).forEach { issue ->
                IssuePreviewItem(issue = issue, onClick = { onIntent(AAPMonitorIntent.SelectIssue(issue)) })
                if (issue != state.scanResults.take(3).last()) {
                    HorizontalDivider(color = AAPMColors.SurfaceVariant, modifier = Modifier.padding(vertical = 8.dp))
                }
            }
        }
    }
}

@Composable
private fun IssuePreviewItem(issue: AccessibilityIssue, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(
                    when (issue.severity) {
                        Severity.P0 -> AAPMColors.P0Color
                        Severity.P1 -> AAPMColors.P1Color
                        Severity.P2 -> AAPMColors.P2Color
                    }
                )
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = issue.serviceName,
                color = AAPMColors.TextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = issue.filePath.split("/").last(),
                color = AAPMColors.TextSecondary,
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        SeverityBadge(severity = issue.severity)
    }
}

// ============================================================
// Scan Results Tab — 扫描结果 Tab
// ============================================================

@Composable
private fun ScanResultsTab(state: AAPMonitorState, onIntent: (AAPMonitorIntent) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // 筛选 Chips
        // Filter Chips
        FilterChipsRow(state, onIntent)

        Spacer(modifier = Modifier.height(16.dp))

        // 问题列表
        // Issue List
        if (state.filteredResults.isEmpty()) {
            EmptyState(
                icon = Icons.Default.CheckCircle,
                message = "没有发现问题",
                description = if (state.scanStatus == ScanStatus.IDLE) "请先运行扫描" else "所有问题已修复"
            )
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(state.filteredResults, key = { it.id }) { issue ->
                    ProblemCard(
                        issue = issue,
                        onClick = { onIntent(AAPMonitorIntent.SelectIssue(issue)) },
                        onResolve = { onIntent(AAPMonitorIntent.MarkIssueResolved(issue)) }
                    )
                }
            }
        }
    }
}

@Composable
private fun FilterChipsRow(state: AAPMonitorState, onIntent: (AAPMonitorIntent) -> Unit) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            FilterChip(
                selected = state.filterOptions.module == null,
                onClick = { onIntent(AAPMonitorIntent.SetFilter(FilterOptions())) },
                label = { Text("全部") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = AAPMColors.SafeColor,
                    selectedLabelColor = AAPMColors.TextPrimary
                )
            )
        }
        item {
            FilterChip(
                selected = state.filterOptions.module == "P0",
                onClick = { onIntent(AAPMonitorIntent.SetFilter(FilterOptions(module = "P0"))) },
                label = { Text("P0") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = AAPMColors.P0Color,
                    selectedLabelColor = AAPMColors.TextPrimary
                )
            )
        }
        item {
            FilterChip(
                selected = state.filterOptions.module == "P1",
                onClick = { onIntent(AAPMonitorIntent.SetFilter(FilterOptions(module = "P1"))) },
                label = { Text("P1") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = AAPMColors.P1Color,
                    selectedLabelColor = AAPMColors.TextPrimary
                )
            )
        }
        item {
            FilterChip(
                selected = state.filterOptions.module == "P2",
                onClick = { onIntent(AAPMonitorIntent.SetFilter(FilterOptions(module = "P2"))) },
                label = { Text("P2") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = AAPMColors.P2Color,
                    selectedLabelColor = AAPMColors.Background
                )
            )
        }
    }
}

@Composable
private fun ProblemCard(
    issue: AccessibilityIssue,
    onClick: () -> Unit,
    onResolve: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = AAPMColors.CardBackground),
        shape = RoundedCornerShape(12.dp)
    ) {
        // 左侧严重程度色带
        // Left severity color band
        Row {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .background(
                        when (issue.severity) {
                            Severity.P0 -> AAPMColors.P0Color
                            Severity.P1 -> AAPMColors.P1Color
                            Severity.P2 -> AAPMColors.P2Color
                        }
                    )
            )
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = issue.serviceName,
                        color = AAPMColors.TextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                    SeverityBadge(severity = issue.severity)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = issue.description,
                    color = AAPMColors.TextSecondary,
                    fontSize = 14.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = issue.filePath.split("/").takeLast(2).joinToString("/"),
                        color = AAPMColors.TextTertiary,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    if (!issue.isResolved) {
                        TextButton(onClick = onResolve) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = null,
                                tint = AAPMColors.SafeColor,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("标记已解决", color = AAPMColors.SafeColor, fontSize = 12.sp)
                        }
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = AAPMColors.SafeColor,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("已解决", color = AAPMColors.SafeColor, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SeverityBadge(severity: Severity) {
    val (color, text) = when (severity) {
        Severity.P0 -> AAPMColors.P0Color to "P0"
        Severity.P1 -> AAPMColors.P1Color to "P1"
        Severity.P2 -> AAPMColors.P2Color to "P2"
    }
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(color.copy(alpha = 0.2f))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = text,
            color = color,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

// ============================================================
// AAPM Monitor Tab — AAPM 监控 Tab
// ============================================================

@Composable
private fun AAPMMonitorTab(state: AAPMonitorState, onIntent: (AAPMonitorIntent) -> Unit) {
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "AAPM 实时监控",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = AAPMColors.TextPrimary
        )

        // AAPM 状态卡片
        // AAPM Status Card
        AAPMStatusCard(state, onIntent)

        // 设备信息
        // Device Info
        DeviceInfoCard(state)

        // 降级策略提示
        // Downgrade Strategy Tips
        DowngradeStrategyCard()

        // BottomSheet 预览按钮
        // BottomSheet Preview Button
        Button(
            onClick = { onIntent(AAPMonitorIntent.SetBottomSheetExpanded(true)) },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = AAPMColors.CardBackground)
        ) {
            Icon(Icons.Default.ArrowDownward, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("查看降级策略详情")
        }
    }
}

@Composable
private fun AAPMStatusCard(state: AAPMonitorState, onIntent: (AAPMonitorIntent) -> Unit) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0.3f,
        animationSpec = infiniteRepeatableAnimation(
            animation = tween(800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = AAPMColors.CardBackground),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                // 状态指示灯
                // Status indicator
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(
                            when (state.aapmStatus) {
                                AapmStatus.ACTIVE -> AAPMColors.AAPMActive.copy(alpha = if (state.aapmStatus == AapmStatus.ACTIVE) alpha else 1f)
                                AapmStatus.INACTIVE -> AAPMColors.AAPMInactive
                                AapmStatus.UNKNOWN -> AAPMColors.AAPMUnknown
                            }
                        )
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = when (state.aapmStatus) {
                        AapmStatus.ACTIVE -> "AAPM 已激活"
                        AapmStatus.INACTIVE -> "AAPM 未激活"
                        AapmStatus.UNKNOWN -> "状态未知"
                    },
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = when (state.aapmStatus) {
                        AapmStatus.ACTIVE -> AAPMColors.AAPMActive
                        AapmStatus.INACTIVE -> AAPMColors.AAPMInactive
                        AapmStatus.UNKNOWN -> AAPMColors.AAPMUnknown
                    }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = when (state.aapmStatus) {
                    AapmStatus.ACTIVE -> "您的设备已启用高级保护模式，所有未认证的辅助工具将被禁用"
                    AapmStatus.INACTIVE -> "当前设备未启用高级保护模式"
                    AapmStatus.UNKNOWN -> "无法获取 AAPM 状态，请检查权限"
                },
                color = AAPMColors.TextSecondary,
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedButton(
                onClick = { onIntent(AAPMonitorIntent.RefreshAapmStatus) },
                enabled = !state.isLoading
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(Icons.Default.Refresh, contentDescription = null)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text("刷新状态")
            }
        }
    }
}

@Composable
private fun DeviceInfoCard(state: AAPMonitorState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = AAPMColors.CardBackground),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "设备信息",
                color = AAPMColors.TextPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(12.dp))
            state.deviceInfo?.let { info ->
                InfoRow("SDK 版本", "${info.sdkVersion}")
                InfoRow("设备厂商", info.manufacturer)
                InfoRow("设备型号", info.model)
                InfoRow("Root 状态", if (info.isRooted) "是" else "否")
            } ?: Text(
                text = "点击刷新获取设备信息",
                color = AAPMColors.TextSecondary,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, color = AAPMColors.TextSecondary, fontSize = 14.sp)
        Text(text = value, color = AAPMColors.TextPrimary, fontSize = 14.sp)
    }
}

@Composable
private fun DowngradeStrategyCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = AAPMColors.CardBackground),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "建议降级策略",
                color = AAPMColors.TextPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(12.dp))
            StrategyItem(
                number = "1",
                title = "Autofill API 迁移",
                description = "密码管理器迁移至 AutofillService"
            )
            StrategyItem(
                number = "2",
                title = "Screen Capture API",
                description = "截屏功能迁移至 MediaProjection"
            )
            StrategyItem(
                number = "3",
                title = "NotificationListener",
                description = "消息拦截迁移至 NotificationListenerService"
            )
            StrategyItem(
                number = "4",
                title = "InputMethodService",
                description = "安全键盘迁移至自定义输入法"
            )
        }
    }
}

@Composable
private fun StrategyItem(number: String, title: String, description: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(AAPMColors.SafeColor),
            contentAlignment = Alignment.Center
        ) {
            Text(text = number, color = AAPMColors.Background, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(text = title, color = AAPMColors.TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            Text(text = description, color = AAPMColors.TextSecondary, fontSize = 12.sp)
        }
    }
}

// ============================================================
// Compliance Guide Tab — 合规引导 Tab
// ============================================================

@Composable
private fun ComplianceGuideTab(state: AAPMonitorState, onIntent: (AAPMonitorIntent) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "合规标注引导",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = AAPMColors.TextPrimary
        )

        Text(
            text = "按照以下步骤完成 Android 17 AAPM 合规",
            color = AAPMColors.TextSecondary,
            fontSize = 14.sp
        )

        // 步骤指示器
        // Step Indicator
        StepperIndicator(currentStep = state.complianceStep)

        // 步骤内容卡片
        // Step Content Cards
        ComplianceStepContent(
            step = state.complianceStep,
            onStepChange = { onIntent(AAPMonitorIntent.SetComplianceStep(it)) }
        )
    }
}

@Composable
private fun StepperIndicator(currentStep: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        (1..4).forEach { step ->
            val isCompleted = step < currentStep
            val isCurrent = step == currentStep

            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(
                            when {
                                isCompleted -> AAPMColors.SafeColor
                                isCurrent -> AAPMColors.P1Color
                                else -> AAPMColors.SurfaceVariant
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (isCompleted) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = null,
                            tint = AAPMColors.Background,
                            modifier = Modifier.size(20.dp)
                        )
                    } else {
                        Text(
                            text = "$step",
                            color = if (isCurrent) AAPMColors.Background else AAPMColors.TextSecondary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = when (step) {
                        1 -> "理解"
                        2 -> "检查"
                        3 -> "设置"
                        4 -> "申请"
                        else -> ""
                    },
                    color = if (isCurrent) AAPMColors.TextPrimary else AAPMColors.TextSecondary,
                    fontSize = 12.sp
                )
            }

            if (step < 4) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(2.dp)
                        .padding(horizontal = 8.dp)
                        .background(
                            if (isCompleted) AAPMColors.SafeColor else AAPMColors.SurfaceVariant
                        )
                )
            }
        }
    }
}

@Composable
private fun ComplianceStepContent(step: Int, onStepChange: (Int) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = AAPMColors.CardBackground),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            val (title, description, content) = when (step) {
                1 -> Triple(
                    "理解 Android 17 AAPM 要求",
                    "了解高级保护模式对辅助工具的影响",
                    Step1Content()
                )
                2 -> Triple(
                    "检查应用资格",
                    "确认您的应用是否符合辅助工具认证条件",
                    Step2Content()
                )
                3 -> Triple(
                    "设置 isAccessibilityTool 标志",
                    "在 AndroidManifest.xml 中正确配置",
                    Step3Content()
                )
                4 -> Triple(
                    "申请辅助工具认证",
                    "提交 Google 认证申请流程",
                    Step4Content()
                )
                else -> return
            }

            Text(
                text = title,
                color = AAPMColors.TextPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = description,
                color = AAPMColors.TextSecondary,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(16.dp))
            content

            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (step > 1) {
                    OutlinedButton(onClick = { onStepChange(step - 1) }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("上一步")
                    }
                } else {
                    Spacer(modifier = Modifier.width(1.dp))
                }
                if (step < 4) {
                    Button(onClick = { onStepChange(step + 1) }) {
                        Text("下一步")
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(Icons.Default.ArrowForward, contentDescription = null)
                    }
                } else {
                    Button(
                        onClick = { /* Complete */ },
                        colors = ButtonDefaults.buttonColors(containerColor = AAPMColors.SafeColor)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("完成")
                    }
                }
            }
        }
    }
}

@Composable
private fun Step1Content() {
    Column {
        Text(
            text = "Android 17 Beta 2 引入了 Advanced Protection Mode (AAPM)，带来史上最严格的安全限制：",
            color = AAPMColors.TextSecondary,
            fontSize = 14.sp
        )
        Spacer(modifier = Modifier.height(12.dp))
        BulletPoint("所有未标记为 isAccessibilityTool=\"true\" 的应用将被完全禁止使用 AccessibilityService API")
        BulletPoint("受影响应用：密码管理器、自动化工具、屏幕录制、聊天 App 等")
        BulletPoint("强制迁移窗口：Android 17 正式发布后")
    }
}

@Composable
private fun Step2Content() {
    Column {
        Text(
            text = "以下应用类型可能有资格申请辅助工具认证：",
            color = AAPMColors.TextSecondary,
            fontSize = 14.sp
        )
        Spacer(modifier = Modifier.height(12.dp))
        BulletPoint("密码管理器（Password Manager）")
        BulletPoint("自动化工具（Tasker、MacroDroid 等）")
        BulletPoint("屏幕录制/截图应用")
        BulletPoint("银行类 App 安全键盘")
        BulletPoint("需要 Google 认证审核")
    }
}

@Composable
private fun Step3Content() {
    Column {
        Text(
            text = "在 AndroidManifest.xml 中添加以下配置：",
            color = AAPMColors.TextSecondary,
            fontSize = 14.sp
        )
        Spacer(modifier = Modifier.height(12.dp))
        CodeBlock("""
            <accessibility-service
                android:name=".YourService"
                android:accessibilityToolType="passwordManager"
                ... />
        """.trimIndent())
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "accessibilityToolType 可选值：",
            color = AAPMColors.TextSecondary,
            fontSize = 14.sp
        )
        BulletPoint("passwordManager — 密码管理器")
        BulletPoint("automation — 自动化工具")
        BulletPoint("screenCapture — 屏幕录制")
    }
}

@Composable
private fun Step4Content() {
    Column {
        Text(
            text = "提交 Google 辅助工具认证申请：",
            color = AAPMColors.TextSecondary,
            fontSize = 14.sp
        )
        Spacer(modifier = Modifier.height(12.dp))
        BulletPoint("准备应用说明文档和截图")
        BulletPoint("在 Google Play Console 提交审核")
        BulletPoint("等待 Google 安全团队审核（通常 2-4 周）")
        BulletPoint("审核通过后获得官方认证")
    }
}

@Composable
private fun CodeBlock(code: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(AAPMColors.Background)
            .padding(12.dp)
            .horizontalScroll(rememberScrollState())
    ) {
        Text(
            text = code,
            color = AAPMColors.SafeColor,
            fontSize = 12.sp,
            fontFamily = FontFamily.Monospace
        )
    }
}

// ============================================================
// Knowledge Base Tab — 知识库 Tab
// ============================================================

@Composable
private fun KnowledgeBaseTab(state: AAPMonitorState, onIntent: (AAPMonitorIntent) -> Unit) {
    LaunchedEffect(Unit) {
        onIntent(AAPMonitorIntent.LoadKnowledgeBase)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "替代方案知识库",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = AAPMColors.TextPrimary
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "从 AccessibilityService 迁移到 Scoped API",
            color = AAPMColors.TextSecondary,
            fontSize = 14.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 搜索框
        // Search Box
        OutlinedTextField(
            value = state.searchQuery,
            onValueChange = { onIntent(AAPMonitorIntent.SetSearchQuery(it)) },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("搜索替代方案...", color = AAPMColors.TextTertiary) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = AAPMColors.TextSecondary) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = AAPMColors.SafeColor,
                unfocusedBorderColor = AAPMColors.SurfaceVariant,
                focusedTextColor = AAPMColors.TextPrimary,
                unfocusedTextColor = AAPMColors.TextPrimary,
                cursorColor = AAPMColors.SafeColor
            ),
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 知识库列表
        // Knowledge Base List
        val filteredEntries = state.knowledgeEntries.filter { entry ->
            state.searchQuery.isEmpty() ||
                    entry.name.contains(state.searchQuery, ignoreCase = true) ||
                    entry.description.contains(state.searchQuery, ignoreCase = true) ||
                    entry.alternative.contains(state.searchQuery, ignoreCase = true)
        }

        if (filteredEntries.isEmpty()) {
            EmptyState(
                icon = Icons.Default.Book,
                message = "未找到相关知识库条目",
                description = "尝试其他搜索关键词"
            )
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredEntries, key = { it.name }) { entry ->
                    KnowledgeBaseItemCard(entry = entry)
                }
            }
        }
    }
}

@Composable
private fun KnowledgeBaseItemCard(entry: ScopedApiEntry) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = AAPMColors.CardBackground),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = entry.name,
                    color = AAPMColors.TextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = null,
                    tint = AAPMColors.TextSecondary,
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = entry.description,
                color = AAPMColors.TextSecondary,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "替代方案：",
                color = AAPMColors.TextTertiary,
                fontSize = 12.sp
            )
            Text(
                text = entry.alternative,
                color = AAPMColors.SafeColor,
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "适用场景：",
                color = AAPMColors.TextTertiary,
                fontSize = 12.sp
            )
            Text(
                text = entry.applicableScenario,
                color = AAPMColors.P1Color,
                fontSize = 12.sp
            )
        }
    }
}

// ============================================================
// Call Chain Flame — 调用链火焰图
// ============================================================

/**
 * 调用链火焰图可视化组件
 * Call chain flame chart visualization component
 *
 * 横向滚动展示调用链
 * Horizontally scrollable call chain visualization
 */
@Composable
private fun CallChainFlame(callChain: List<String>) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
    ) {
        items(callChain.size) { index ->
            val methodName = callChain[index]
            CallChainNode(
                methodName = methodName,
                depth = index,
                isLast = index == callChain.size - 1
            )
        }
    }
}

@Composable
private fun CallChainNode(methodName: String, depth: Int, isLast: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(4.dp))
                .background(
                    when {
                        depth == 0 -> AAPMColors.P0Color
                        depth == 1 -> AAPMColors.P1Color
                        else -> AAPMColors.P2Color
                    }.copy(alpha = 1f - (depth * 0.15f))
                )
                .padding(horizontal = 8.dp, vertical = 6.dp)
        ) {
            Text(
                text = methodName,
                color = AAPMColors.TextPrimary,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        if (!isLast) {
            Icon(
                imageVector = Icons.Default.ArrowForward,
                contentDescription = null,
                tint = AAPMColors.TextTertiary,
                modifier = Modifier.size(12.dp)
            )
        }
    }
}

// ============================================================
// Common Components — 通用组件
// ============================================================

@Composable
private fun BulletPoint(text: String) {
    Row(
        modifier = Modifier.padding(vertical = 2.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = "•",
            color = AAPMColors.SafeColor,
            modifier = Modifier.padding(end = 8.dp)
        )
        Text(
            text = text,
            color = AAPMColors.TextSecondary,
            fontSize = 14.sp
        )
    }
}

@Composable
private fun EmptyState(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    message: String,
    description: String
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
            tint = AAPMColors.TextTertiary,
            modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = message,
            color = AAPMColors.TextPrimary,
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = description,
            color = AAPMColors.TextSecondary,
            fontSize = 14.sp
        )
    }
}
