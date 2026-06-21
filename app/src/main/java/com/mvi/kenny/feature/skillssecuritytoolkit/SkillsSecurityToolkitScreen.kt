package com.mvi.kenny.feature.skillssecuritytoolkit

import com.mvi.kenny.base.TopBarAction
import com.mvi.kenny.base.TopBarConfig
// ================================================================
// SkillsSecurityToolkitScreen — Android Skills 安全扫描工具包主界面
// ================================================================
// Main screen for Android Skills Security Scanner.
//
// PRD-280: Android Skills 安全扫描工具包
// Design Reference: memory/agency/designs/PRD-280-Android-Skills-安全扫描工具包.md
//
// Architecture:
//   - 4-Tab Navigation: Dashboard / Scanner / Report / Reference
//   - Dark security theme: #0D1117 background, #F85149 danger color
//   - Material 3 NavigationBar for Tab switching
//   - MVI pattern: Contract → ViewModel → Screen (this file)
//
// Tabs:
//   1. DashboardScreen — Security score gauge, recent scans, quick actions
//   2. ScannerScreen — Input mode selection, scan trigger, results display
//   3. ReportScreen — Historical reports list, report detail with attack chain
//   4. ReferenceScreen — Malicious patterns / vulnerability types / fix suggestions
// ================================================================

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Paint
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Shield
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
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.collectLatest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// ================================================================
// Design Tokens — 安全主题色彩
// ================================================================
private object SecurityColors {
    val Primary = Color(0xFF1E88E5)        // 科技蓝
    val Background = Color(0xFF0D1117)       // 深色背景
    val Surface = Color(0xFF161B22)         // 卡片背景
    val Error = Color(0xFFF85149)           // 高危/恶意红
    val Warning = Color(0xFFF0883E)         // 注意级橙
    val Safe = Color(0xFF3FB950)            // 安全绿
    val OnBackground = Color(0xFFE6EDF3)    // 主要文字
    val OnSurfaceVariant = Color(0xFF8B949E) // 次要文字
    val DangerBg = Color(0xFFF85149).copy(alpha = 0.1f)
    val WarningBg = Color(0xFFF0883E).copy(alpha = 0.1f)
    val SafeBg = Color(0xFF3FB950).copy(alpha = 0.1f)
}

// ================================================================
// SkillsSecurityToolkitScreen — 主入口 Composable
// ================================================================

/**
 * SkillsSecurityToolkitScreen — 安全扫描工具包主界面入口
 *
 * @param viewModel ViewModel instance
 * @param onUpdateTopBar Callback to update parent TopBar config
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SkillsSecurityToolkitScreen(
    viewModel: SkillsSecurityToolkitViewModel = viewModel(),
    onUpdateTopBar: (TopBarConfig) -> Unit = {}
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    // ── Update parent TopBar ─────────────────────────────────────
    LaunchedEffect(state.selectedTab, state.dashboardState.securityScore) {
        onUpdateTopBar(
            TopBarConfig(
                title = when (state.selectedTab) {
                    SecurityTab.DASHBOARD -> "🛡️ Skills 安全扫描"
                    SecurityTab.SCANNER -> "🔍 安全扫描器"
                    SecurityTab.REPORT -> "📋 安全报告"
                    SecurityTab.REFERENCE -> "📚 参考库"
                },
                actions = listOf(
                    TopBarAction(
                        icon = Icons.Default.Refresh,
                        contentDescription = "刷新",
                        onClick = { viewModel.sendIntent(SkillsSecurityToolkitIntent.LoadDashboard) }
                    )
                )
            )
        )
    }

    // ── Handle Effects ──────────────────────────────────────────
    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is SkillsSecurityToolkitEffect.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(effect.message)
                }
                is SkillsSecurityToolkitEffect.NavigateToReport -> {
                    viewModel.sendIntent(SkillsSecurityToolkitIntent.SelectReport(effect.reportId))
                }
                is SkillsSecurityToolkitEffect.ScanComplete -> {
                    snackbarHostState.showSnackbar(
                        "✅ 扫描完成！安全评分: ${effect.score}/100 (${effect.threatLevel.label})"
                    )
                }
                is SkillsSecurityToolkitEffect.ExportSuccess -> {
                    snackbarHostState.showSnackbar("✅ ${effect.filePath}")
                }
                is SkillsSecurityToolkitEffect.ExportError -> {
                    snackbarHostState.showSnackbar("❌ ${effect.error}")
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Android Skills 安全扫描工具包",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "NVIDIA SkillSpector · ClawHavoc 威胁情报",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SecurityColors.Surface,
                    titleContentColor = SecurityColors.OnBackground
                ),
                actions = {
                    IconButton(onClick = { viewModel.sendIntent(SkillsSecurityToolkitIntent.LoadDashboard) }) {
                        Icon(Icons.Default.Refresh, contentDescription = "刷新", tint = SecurityColors.OnBackground)
                    }
                }
            )
        },
        bottomBar = {
            SecurityNavigationBar(
                selectedTab = state.selectedTab,
                onTabSelect = { tab ->
                    viewModel.sendIntent(SkillsSecurityToolkitIntent.SelectTab(tab))
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = SecurityColors.Background
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(SecurityColors.Background)
        ) {
            when (state.selectedTab) {
                SecurityTab.DASHBOARD -> DashboardScreen(
                    state = state.dashboardState,
                    onQuickScan = { viewModel.sendIntent(SkillsSecurityToolkitIntent.QuickScan) },
                    onScanSelect = { scanId ->
                        viewModel.sendIntent(SkillsSecurityToolkitIntent.SelectReport(scanId))
                    }
                )
                SecurityTab.SCANNER -> ScannerScreen(
                    state = state.scannerState,
                    onIntent = viewModel::sendIntent
                )
                SecurityTab.REPORT -> ReportScreen(
                    state = state.reportState,
                    onIntent = viewModel::sendIntent
                )
                SecurityTab.REFERENCE -> ReferenceScreen(
                    state = state.referenceState,
                    onIntent = viewModel::sendIntent
                )
            }
        }
    }
}


// ================================================================
// SecurityNavigationBar — 底部导航栏
// ================================================================

/**
 * SecurityNavigationBar — Bottom navigation bar for the 4 tabs.
 *
 * @param selectedTab Currently selected tab
 * @param onTabSelect Callback when a tab is selected
 */
@Composable
private fun SecurityNavigationBar(
    selectedTab: SecurityTab,
    onTabSelect: (SecurityTab) -> Unit
) {
    NavigationBar(
        containerColor = SecurityColors.Surface
    ) {
        SecurityTab.entries.forEach { tab ->
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
// DashboardScreen — 首页仪表盘 Tab
// ================================================================

/**
 * DashboardScreen — Security overview dashboard.
 *
 * Features:
 *   - Security score gauge (0-100, animated ring chart)
 *   - Threat level badge (SAFE/CAREFUL/DANGER/HIGH)
 *   - Scan statistics cards
 *   - Quick action buttons
 *   - Recent scan history (last 3)
 *
 * @param state Dashboard state
 * @param onQuickScan Quick scan button click
 * @param onScanSelect Select a recent scan to view detail
 */
@Composable
private fun DashboardScreen(
    state: DashboardState,
    onQuickScan: () -> Unit,
    onScanSelect: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // ── Security Score Gauge ──────────────────────────────────
        item {
            SecurityScoreGaugeCard(
                score = state.securityScore,
                threatLevel = state.threatLevel,
                isLoading = state.isLoading
            )
        }

        // ── Statistics Row ───────────────────────────────────────
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    modifier = Modifier.weight(1f),
                    title = "已扫描",
                    value = "${state.totalScanned}",
                    subtitle = "Skills",
                    color = SecurityColors.Primary
                )
                StatCard(
                    modifier = Modifier.weight(1f),
                    title = "漏洞",
                    value = "${state.vulnerabilitiesFound}",
                    subtitle = "项发现",
                    color = SecurityColors.Warning
                )
                StatCard(
                    modifier = Modifier.weight(1f),
                    title = "恶意",
                    value = "${state.maliciousSkillsFound}",
                    subtitle = "个确认",
                    color = SecurityColors.Error
                )
            }
        }

        // ── Quick Actions ─────────────────────────────────────────
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                QuickActionButton(
                    modifier = Modifier.weight(1f),
                    emoji = "🔍",
                    label = "快速扫描",
                    onClick = onQuickScan
                )
                QuickActionButton(
                    modifier = Modifier.weight(1f),
                    emoji = "📁",
                    label = "本地路径",
                    onClick = { }
                )
                QuickActionButton(
                    modifier = Modifier.weight(1f),
                    emoji = "🔗",
                    label = "输入URL",
                    onClick = { }
                )
            }
        }

        // ── Recent Scans ──────────────────────────────────────────
        item {
            Text(
                text = "📜 最近扫描",
                style = MaterialTheme.typography.titleMedium,
                color = SecurityColors.OnBackground
            )
        }

        if (state.recentScans.isEmpty()) {
            item {
                EmptyStateCard(
                    emoji = "🔍",
                    message = "暂无扫描记录",
                    submessage = "点击上方按钮开始首次扫描"
                )
            }
        } else {
            items(state.recentScans) { scan ->
                RecentScanCard(
                    scan = scan,
                    onClick = { onScanSelect(scan.id) }
                )
            }
        }
    }
}

/**
 * SecurityScoreGaugeCard — Animated security score ring chart.
 *
 * @param score Security score 0-100
 * @param threatLevel Threat level classification
 * @param isLoading Whether data is loading
 */
@Composable
private fun SecurityScoreGaugeCard(
    score: Int,
    threatLevel: ThreatLevel,
    isLoading: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SecurityColors.Surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (isLoading) {
                CircularProgressIndicator(color = SecurityColors.Primary)
            } else {
                // ── Score Gauge ───────────────────────────────────
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(160.dp)
                ) {
                    SecurityScoreGauge(
                        score = score,
                        modifier = Modifier.fillMaxSize()
                    )
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "$score",
                            style = MaterialTheme.typography.displayMedium,
                            fontWeight = FontWeight.Bold,
                            color = threatLevel.color
                        )
                        Text(
                            text = "安全评分",
                            style = MaterialTheme.typography.bodySmall,
                            color = SecurityColors.OnSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // ── Threat Level Badge ────────────────────────────
                ThreatLevelBadge(threatLevel = threatLevel)

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = when (threatLevel) {
                        ThreatLevel.SAFE -> "✅ 未检测到明显威胁"
                        ThreatLevel.CAREFUL -> "⚠️ 检测到低风险项，请审查"
                        ThreatLevel.DANGER -> "🚨 检测到中高风险项，建议立即处理"
                        ThreatLevel.HIGH -> "🔴 检测到高危威胁，必须处理！"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = threatLevel.color
                )
            }
        }
    }
}

/**
 * SecurityScoreGauge — Canvas-drawn animated ring chart for security score.
 *
 * @param score Score 0-100
 * @param modifier Compose modifier
 */
@Composable
private fun SecurityScoreGauge(
    score: Int,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "gauge")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    val scoreColor = when {
        score >= 80 -> SecurityColors.Safe
        score >= 50 -> SecurityColors.Warning
        else -> SecurityColors.Error
    }

    Canvas(modifier = modifier) {
        val strokeWidth = 16.dp.toPx()
        val diameter = size.minDimension - strokeWidth
        val topLeft = Offset(strokeWidth / 2, strokeWidth / 2)

        // Background arc
        drawArc(
            color = Color.Gray.copy(alpha = 0.2f),
            startAngle = 135f,
            sweepAngle = 270f,
            useCenter = false,
            topLeft = topLeft,
            size = androidx.compose.ui.geometry.Size(diameter, diameter),
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )

        // Score arc
        val sweepAngle = (score / 100f) * 270f
        drawArc(
            color = scoreColor.copy(alpha = pulseAlpha),
            startAngle = 135f,
            sweepAngle = sweepAngle,
            useCenter = false,
            topLeft = topLeft,
            size = androidx.compose.ui.geometry.Size(diameter, diameter),
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )
    }
}

/**
 * ThreatLevelBadge — Threat level label badge.
 *
 * @param threatLevel Threat level
 */
@Composable
private fun ThreatLevelBadge(threatLevel: ThreatLevel) {
    Surface(
        color = threatLevel.color.copy(alpha = 0.15f),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(text = threatLevel.emoji, fontSize = 20.sp)
            Text(
                text = "威胁等级: ${threatLevel.label}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = threatLevel.color
            )
        }
    }
}

/**
 * StatCard — Statistics card with value display.
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
        colors = CardDefaults.cardColors(containerColor = SecurityColors.Surface)
    ) {
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
                color = SecurityColors.OnSurfaceVariant
            )
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = SecurityColors.OnSurfaceVariant
            )
        }
    }
}

/**
 * QuickActionButton — Quick action card button.
 */
@Composable
private fun QuickActionButton(
    modifier: Modifier = Modifier,
    emoji: String,
    label: String,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = SecurityColors.Surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = emoji, fontSize = 24.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = SecurityColors.OnBackground
            )
        }
    }
}

/**
 * RecentScanCard — Recent scan history card.
 */
@Composable
private fun RecentScanCard(
    scan: ScanRecord,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = SecurityColors.Surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Score circle
            Surface(
                shape = CircleShape,
                color = scan.threatLevel.color.copy(alpha = 0.15f),
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "${scan.securityScore}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = scan.threatLevel.color
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = scan.target,
                    style = MaterialTheme.typography.bodyMedium,
                    color = SecurityColors.OnBackground,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = SimpleDateFormat("MM/dd HH:mm", Locale.getDefault())
                        .format(Date(scan.timestamp)),
                    style = MaterialTheme.typography.bodySmall,
                    color = SecurityColors.OnSurfaceVariant
                )
            }

            Text(
                text = "${scan.vulnerabilityCount}项",
                style = MaterialTheme.typography.labelSmall,
                color = scan.threatLevel.color
            )
        }
    }
}

/**
 * EmptyStateCard — Empty state display card.
 */
@Composable
private fun EmptyStateCard(emoji: String, message: String, submessage: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SecurityColors.Surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = emoji, fontSize = 48.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.titleMedium,
                color = SecurityColors.OnBackground
            )
            Text(
                text = submessage,
                style = MaterialTheme.typography.bodySmall,
                color = SecurityColors.OnSurfaceVariant
            )
        }
    }
}

// ================================================================
// ScannerScreen — 扫描器 Tab
// ================================================================

/**
 * ScannerScreen — Security scan input and results display.
 *
 * Features:
 *   - Input mode tabs: Local / URL / Text
 *   - Scan configuration options
 *   - Animated scan progress bar
 *   - Scan result: vulnerabilities list, security score
 *
 * @param state Scanner state
 * @param onIntent Intent sender
 */
@Composable
private fun ScannerScreen(
    state: ScannerState,
    onIntent: (SkillsSecurityToolkitIntent) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // ── Input Mode Tabs ───────────────────────────────────────
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SecurityColors.Surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "📥 输入方式",
                    style = MaterialTheme.typography.titleMedium,
                    color = SecurityColors.OnBackground
                )
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    InputMode.entries.forEach { mode ->
                        FilterChip(
                            selected = state.inputMode == mode,
                            onClick = { onIntent(SkillsSecurityToolkitIntent.SetInputMode(mode)) },
                            label = { Text(mode.label) },
                            leadingIcon = {
                                Text(
                                    text = when (mode) {
                                        InputMode.LOCAL -> "📁"
                                        InputMode.URL -> "🔗"
                                        InputMode.TEXT -> "📝"
                                    }
                                )
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // ── Mode-specific input ────────────────────────────
                when (state.inputMode) {
                    InputMode.LOCAL -> OutlinedTextField(
                        value = state.inputPath,
                        onValueChange = { onIntent(SkillsSecurityToolkitIntent.UpdatePath(it)) },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("输入 SKILL.md 本地路径") },
                        leadingIcon = { Icon(Icons.Default.Folder, contentDescription = null) },
                        singleLine = true
                    )
                    InputMode.URL -> OutlinedTextField(
                        value = state.inputUrl,
                        onValueChange = { onIntent(SkillsSecurityToolkitIntent.UpdateUrl(it)) },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("输入 ClawHub Skill URL") },
                        leadingIcon = { Icon(Icons.Default.Link, contentDescription = null) },
                        singleLine = true
                    )
                    InputMode.TEXT -> {
                        OutlinedTextField(
                            value = state.inputText,
                            onValueChange = { onIntent(SkillsSecurityToolkitIntent.UpdateText(it)) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            placeholder = { Text("粘贴 SKILL.md 内容...") },
                            maxLines = 10
                        )
                    }
                }
            }
        }

        // ── Scan Trigger Button ───────────────────────────────────
        Button(
            onClick = { onIntent(SkillsSecurityToolkitIntent.StartScan) },
            modifier = Modifier.fillMaxWidth(),
            enabled = state.scanPhase == ScanPhase.IDLE || state.scanPhase == ScanPhase.DONE,
            colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                containerColor = SecurityColors.Primary
            )
        ) {
            if (state.scanPhase != ScanPhase.IDLE && state.scanPhase != ScanPhase.DONE) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                    color = Color.White
                )
                Spacer(modifier = Modifier.width(8.dp))
            } else {
                Icon(Icons.Default.Security, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = when (state.scanPhase) {
                    ScanPhase.IDLE -> "开始安全扫描"
                    ScanPhase.PARSING -> "解析 SKILL.md..."
                    ScanPhase.DETECTING -> "检测恶意模式..."
                    ScanPhase.ANALYZING -> "分析权限和依赖..."
                    ScanPhase.GENERATING -> "生成报告..."
                    ScanPhase.DONE -> "重新扫描"
                }
            )
        }

        // ── Scan Progress ──────────────────────────────────────────
        if (state.scanPhase != ScanPhase.IDLE && state.scanPhase != ScanPhase.DONE) {
            ScanProgressCard(
                phase = state.scanPhase,
                progress = state.scanProgress
            )
        }

        // ── Scan Result ───────────────────────────────────────────
        state.scanResult?.let { result ->
            ScanResultCard(result = result)
        }
    }
}

/**
 * ScanProgressCard — Animated scan progress display card.
 *
 * @param phase Current scan phase
 * @param progress Scan progress 0.0~1.0
 */
@Composable
private fun ScanProgressCard(
    phase: ScanPhase,
    progress: Float
) {
    val infiniteTransition = rememberInfiniteTransition(label = "scan")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(600),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SecurityColors.Surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = phase.label,
                    style = MaterialTheme.typography.titleMedium,
                    color = SecurityColors.Primary.copy(alpha = pulseAlpha)
                )
                Text(
                    text = "${(progress * 100).toInt()}%",
                    style = MaterialTheme.typography.titleMedium,
                    color = SecurityColors.Primary
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = SecurityColors.Primary,
                trackColor = Color.Gray.copy(alpha = 0.3f)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = phase.progressHint,
                style = MaterialTheme.typography.bodySmall,
                color = SecurityColors.OnSurfaceVariant
            )
        }
    }
}

/**
 * ScanResultCard — Scan result display card with vulnerability list.
 *
 * @param result Scan result data
 */
@Composable
private fun ScanResultCard(result: ScanResult) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // ── Summary Card ──────────────────────────────────────────
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SecurityColors.Surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "扫描结果",
                            style = MaterialTheme.typography.titleMedium,
                            color = SecurityColors.OnBackground
                        )
                        Text(
                            text = result.target,
                            style = MaterialTheme.typography.bodySmall,
                            color = SecurityColors.OnSurfaceVariant
                        )
                    }
                    ThreatLevelBadge(threatLevel = result.threatLevel)
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${result.securityScore}",
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold,
                            color = result.threatLevel.color
                        )
                        Text("安全评分", style = MaterialTheme.typography.labelSmall, color = SecurityColors.OnSurfaceVariant)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${result.vulnerabilities.size}",
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold,
                            color = SecurityColors.Warning
                        )
                        Text("漏洞数", style = MaterialTheme.typography.labelSmall, color = SecurityColors.OnSurfaceVariant)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${result.maliciousPatterns.size}",
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold,
                            color = SecurityColors.Error
                        )
                        Text("恶意模式", style = MaterialTheme.typography.labelSmall, color = SecurityColors.OnSurfaceVariant)
                    }
                }

                if (result.memoryPoisoningDetected) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(
                        color = SecurityColors.DangerBg,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("🚨", fontSize = 20.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "检测到内存投毒！SOUL.md/MEMORY.md 已被恶意篡改",
                                style = MaterialTheme.typography.bodySmall,
                                color = SecurityColors.Error
                            )
                        }
                    }
                }
            }
        }

        // ── Vulnerabilities List ───────────────────────────────────
        if (result.vulnerabilities.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SecurityColors.Surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "🐛 发现漏洞",
                        style = MaterialTheme.typography.titleMedium,
                        color = SecurityColors.OnBackground
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    result.vulnerabilities.forEach { vuln ->
                        VulnerabilityCard(vulnerability = vuln)
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }

        // ── Malicious Patterns ────────────────────────────────────
        if (result.maliciousPatterns.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SecurityColors.Surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "⚠️ 恶意模式",
                        style = MaterialTheme.typography.titleMedium,
                        color = SecurityColors.Error
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    result.maliciousPatterns.forEach { pattern ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("🔴", fontSize = 14.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = pattern,
                                style = MaterialTheme.typography.bodyMedium,
                                color = SecurityColors.Error
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * VulnerabilityCard — Individual vulnerability finding card.
 *
 * @param vulnerability Vulnerability data
 */
@Composable
private fun VulnerabilityCard(vulnerability: Vulnerability) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = vulnerability.severity.color.copy(alpha = 0.05f)
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = vulnerability.type,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = vulnerability.severity.color
                )
                Surface(
                    color = vulnerability.severity.color.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = vulnerability.severity.label,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = vulnerability.severity.color
                    )
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = vulnerability.description,
                style = MaterialTheme.typography.bodySmall,
                color = SecurityColors.OnSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "位置: ${vulnerability.location}",
                style = MaterialTheme.typography.labelSmall,
                color = SecurityColors.Primary
            )
            if (vulnerability.codeSnippet.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                CodeBlock(code = vulnerability.codeSnippet, isDark = true)
            }
        }
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
            .border(1.dp, SecurityColors.OnSurfaceVariant.copy(alpha = 0.3f), RoundedCornerShape(8.dp)),
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

// ================================================================
// ReportScreen — 报告 Tab
// ================================================================

/**
 * ReportScreen — Historical scan reports list and detail view.
 *
 * Features:
 *   - Report list with score and threat level
 *   - Report detail with full vulnerability breakdown
 *   - Attack chain Canvas visualization
 *   - Export functionality (SARIF/JSON/PDF)
 *
 * @param state Report tab state
 * @param onIntent Intent sender
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReportScreen(
    state: ReportState,
    onIntent: (SkillsSecurityToolkitIntent) -> Unit
) {
    var showDetailSheet by remember { mutableStateOf(false) }

    if (state.selectedReport != null) {
        ModalBottomSheet(
            onDismissRequest = { showDetailSheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = SecurityColors.Surface
        ) {
            ReportDetailSheet(
                report = state.selectedReport,
                onExport = { format ->
                    onIntent(SkillsSecurityToolkitIntent.ExportReport(state.selectedReport.summary.id, format))
                },
                onDismiss = { showDetailSheet = false }
            )
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "📋 安全报告",
                style = MaterialTheme.typography.titleMedium,
                color = SecurityColors.OnBackground
            )
        }

        if (state.reports.isEmpty()) {
            item {
                EmptyStateCard(
                    emoji = "📋",
                    message = "暂无报告",
                    submessage = "在扫描器中完成扫描后会自动生成报告"
                )
            }
        } else {
            items(state.reports) { report ->
                ReportListCard(
                    report = report,
                    onClick = {
                        onIntent(SkillsSecurityToolkitIntent.SelectReport(report.id))
                        showDetailSheet = true
                    }
                )
            }
        }
    }
}

/**
 * ReportListCard — Report summary card for list display.
 *
 * @param report Report summary
 * @param onClick Card click handler
 */
@Composable
private fun ReportListCard(
    report: ReportSummary,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = SecurityColors.Surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Score circle
            Surface(
                shape = CircleShape,
                color = report.threatLevel.color.copy(alpha = 0.15f),
                modifier = Modifier.size(56.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${report.securityScore}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = report.threatLevel.color
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = report.target,
                    style = MaterialTheme.typography.bodyMedium,
                    color = SecurityColors.OnBackground,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ThreatLevelBadgeSmall(threatLevel = report.threatLevel)
                    Text(
                        text = "${report.vulnerabilityCount}项漏洞",
                        style = MaterialTheme.typography.labelSmall,
                        color = SecurityColors.Warning
                    )
                }
                Text(
                    text = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                        .format(Date(report.timestamp)),
                    style = MaterialTheme.typography.bodySmall,
                    color = SecurityColors.OnSurfaceVariant
                )
            }

            IconButton(onClick = onClick) {
                Icon(
                    Icons.Default.Description,
                    contentDescription = "查看详情",
                    tint = SecurityColors.Primary
                )
            }
        }
    }
}

/**
 * ThreatLevelBadgeSmall — Compact threat level badge.
 */
@Composable
private fun ThreatLevelBadgeSmall(threatLevel: ThreatLevel) {
    Surface(
        color = threatLevel.color.copy(alpha = 0.15f),
        shape = RoundedCornerShape(4.dp)
    ) {
        Text(
            text = threatLevel.emoji + " " + threatLevel.label,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            style = MaterialTheme.typography.labelSmall,
            color = threatLevel.color
        )
    }
}

/**
 * ReportDetailSheet — Full report detail in a bottom sheet.
 *
 * @param report Full report detail
 * @param onExport Export button handler
 * @param onDismiss Dismiss handler
 */
@Composable
private fun ReportDetailSheet(
    report: ReportDetail,
    onExport: (ExportFormat) -> Unit,
    onDismiss: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
            .padding(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // ── Header ───────────────────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = report.summary.target,
                    style = MaterialTheme.typography.titleLarge,
                    color = SecurityColors.OnBackground
                )
                Text(
                    text = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                        .format(Date(report.summary.timestamp)),
                    style = MaterialTheme.typography.bodySmall,
                    color = SecurityColors.OnSurfaceVariant
                )
            }
            ThreatLevelBadge(threatLevel = report.summary.threatLevel)
        }

        // ── Attack Chain Canvas ──────────────────────────────────
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SecurityColors.Surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "🔗 攻击链",
                    style = MaterialTheme.typography.titleMedium,
                    color = SecurityColors.OnBackground
                )
                Spacer(modifier = Modifier.height(12.dp))
                AttackChainCanvas(
                    nodes = report.attackChain,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                )
            }
        }

        // ── Vulnerabilities ───────────────────────────────────────
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SecurityColors.Surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "🐛 漏洞详情 (${report.vulnerabilities.size})",
                    style = MaterialTheme.typography.titleMedium,
                    color = SecurityColors.OnBackground
                )
                Spacer(modifier = Modifier.height(12.dp))
                report.vulnerabilities.forEach { vuln ->
                    VulnerabilityCard(vulnerability = vuln)
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }

        // ── Memory Poisoning ─────────────────────────────────────
        if (report.memoryPoisoning.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SecurityColors.DangerBg)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "🚨 内存投毒检测",
                        style = MaterialTheme.typography.titleMedium,
                        color = SecurityColors.Error
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    report.memoryPoisoning.forEach { finding ->
                        Text(
                            text = "${finding.filePath}: ${finding.description}",
                            style = MaterialTheme.typography.bodySmall,
                            color = SecurityColors.Error
                        )
                    }
                }
            }
        }

        // ── Export Actions ────────────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { onExport(ExportFormat.SARIF) },
                modifier = Modifier.weight(1f),
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = SecurityColors.Primary)
            ) {
                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("SARIF", fontSize = 12.sp)
            }
            Button(
                onClick = { onExport(ExportFormat.JSON) },
                modifier = Modifier.weight(1f),
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = SecurityColors.Primary)
            ) {
                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("JSON", fontSize = 12.sp)
            }
        }

        TextButton(
            onClick = onDismiss,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("关闭")
        }
    }
}

/**
 * AttackChainCanvas — Canvas-drawn attack chain visualization.
 * Draws nodes connected by lines to show the attack progression.
 *
 * @param nodes Attack chain nodes
 * @param modifier Compose modifier
 */
@Composable
private fun AttackChainCanvas(
    nodes: List<AttackChainNode>,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        if (nodes.isEmpty()) return@Canvas

        val nodeWidth = 120.dp.toPx()
        val nodeHeight = 60.dp.toPx()
        val spacing = (size.width - nodeWidth * minOf(nodes.size, 3)) / (minOf(nodes.size, 3) + 1)

        nodes.forEachIndexed { index, node ->
            val row = index / 3
            val col = index % 3
            val x = spacing + col * (nodeWidth + spacing)
            val y = 20.dp.toPx() + row * (nodeHeight + 20.dp.toPx())

            // Draw connection line
            if (node.connectedTo.isNotEmpty()) {
                val nextIndex = nodes.indexOfFirst { it.id == node.connectedTo.first() }
                if (nextIndex > index) {
                    val nextRow = nextIndex / 3
                    val nextCol = nextIndex % 3
                    val nextX = spacing + nextCol * (nodeWidth + spacing)
                    val nextY = 20.dp.toPx() + nextRow * (nodeHeight + 20.dp.toPx())
                    drawLine(
                        color = Color.Gray,
                        start = Offset(x + nodeWidth, y + nodeHeight / 2),
                        end = Offset(nextX, nextY + nodeHeight / 2),
                        strokeWidth = 2.dp.toPx()
                    )
                }
            }

            // Draw node rectangle
            val nodeColor = when (node.type) {
                AttackNodeType.START -> SecurityColors.Primary
                AttackNodeType.MIDDLE -> SecurityColors.Warning
                AttackNodeType.END -> SecurityColors.Safe
                AttackNodeType.ATTACK -> SecurityColors.Error
            }

            drawRoundRect(
                color = nodeColor.copy(alpha = 0.2f),
                topLeft = Offset(x, y),
                size = androidx.compose.ui.geometry.Size(nodeWidth, nodeHeight),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(8.dp.toPx())
            )
            drawRoundRect(
                color = nodeColor,
                topLeft = Offset(x, y),
                size = androidx.compose.ui.geometry.Size(nodeWidth, 4.dp.toPx()),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(8.dp.toPx())
            )
        }
    }

    // Node labels (drawn as overlay Text)
    Column(modifier = Modifier.fillMaxWidth()) {
        nodes.chunked(3).forEach { rowNodes ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                rowNodes.forEach { node ->
                    Surface(
                        modifier = Modifier.weight(1f),
                        color = when (node.type) {
                            AttackNodeType.START -> SecurityColors.Primary.copy(alpha = 0.15f)
                            AttackNodeType.MIDDLE -> SecurityColors.Warning.copy(alpha = 0.15f)
                            AttackNodeType.END -> SecurityColors.Safe.copy(alpha = 0.15f)
                            AttackNodeType.ATTACK -> SecurityColors.Error.copy(alpha = 0.15f)
                        },
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = node.label,
                            modifier = Modifier.padding(4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = when (node.type) {
                                AttackNodeType.START -> SecurityColors.Primary
                                AttackNodeType.MIDDLE -> SecurityColors.Warning
                                AttackNodeType.END -> SecurityColors.Safe
                                AttackNodeType.ATTACK -> SecurityColors.Error
                            },
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            textAlign = TextAlign.Center
                        )
                    }
                }
                // Fill remaining space if row is incomplete
                repeat(3 - rowNodes.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

// ================================================================
// ReferenceScreen — 参考库 Tab
// ================================================================

/**
 * ReferenceScreen — Searchable reference library (malicious patterns / vulnerabilities / fix suggestions).
 *
 * @param state Reference tab state
 * @param onIntent Intent sender
 */
@Composable
private fun ReferenceScreen(
    state: ReferenceState,
    onIntent: (SkillsSecurityToolkitIntent) -> Unit
) {
    LaunchedEffect(state.activeTab) {
        onIntent(SkillsSecurityToolkitIntent.SwitchReferenceTab(state.activeTab))
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // ── Reference Tab Selector ─────────────────────────────────
        TabRow(
            selectedTabIndex = state.activeTab.ordinal,
            containerColor = SecurityColors.Surface
        ) {
            ReferenceTab.entries.forEach { tab ->
                Tab(
                    selected = state.activeTab == tab,
                    onClick = { onIntent(SkillsSecurityToolkitIntent.SwitchReferenceTab(tab)) },
                    text = {
                        Text(
                            text = when (tab) {
                                ReferenceTab.MALICIOUS_PATTERNS -> "恶意模式"
                                ReferenceTab.VULNERABILITY_TYPES -> "漏洞类型"
                                ReferenceTab.FIX_SUGGESTIONS -> "修复建议"
                            },
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // ── Search Bar ───────────────────────────────────────────
        OutlinedTextField(
            value = state.searchQuery,
            onValueChange = { onIntent(SkillsSecurityToolkitIntent.SearchReference(it)) },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("搜索...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            singleLine = true
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "共 ${state.items.size} 条记录",
            style = MaterialTheme.typography.bodySmall,
            color = SecurityColors.OnSurfaceVariant
        )

        Spacer(modifier = Modifier.height(8.dp))

        // ── Reference Items List ─────────────────────────────────
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(state.items) { item ->
                ReferenceItemCard(item = item)
            }
        }
    }
}

/**
 * ReferenceItemCard — Reference library item card.
 *
 * @param item Reference item data
 */
@Composable
private fun ReferenceItemCard(item: ReferenceItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SecurityColors.Surface)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = SecurityColors.OnBackground,
                    modifier = Modifier.weight(1f)
                )
                item.severity?.let { severity ->
                    Surface(
                        color = severity.color.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = severity.label,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = severity.color
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = item.description,
                style = MaterialTheme.typography.bodySmall,
                color = SecurityColors.OnSurfaceVariant
            )

            if (item.exampleCode.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                CodeBlock(code = item.exampleCode, isDark = true)
            }

            if (item.fixSuggestion.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    color = SecurityColors.Safe.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text(
                            text = "💡 修复建议",
                            style = MaterialTheme.typography.labelSmall,
                            color = SecurityColors.Safe
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = item.fixSuggestion,
                            style = MaterialTheme.typography.bodySmall,
                            color = SecurityColors.OnBackground
                        )
                    }
                }
            }
        }
    }
}
