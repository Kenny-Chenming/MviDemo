package com.mvi.kenny.feature.bgaudio

// ================================================================
// BackgroundAudioScreen — Android 17 Background Audio Hardening 合规中心主界面
// ================================================================
// PRD-178: Android 17 Background Audio Hardening 合规检测工具包
//
// Implements MVI pattern:
//   - Contract: BackgroundAudioContract (State/Intent/Effect)
//   - ViewModel: BackgroundAudioViewModel
//   - Screen: BackgroundAudioScreen (Composables)
//
// Structure:
//   - Dashboard: Compliance score gauge + 9 tool cards in 3x3 grid
//   - Tool Detail: Each tool has its own detail screen
// ================================================================

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.flow.collectLatest

// ================================================================
// Constants
// ================================================================

/** Primary color: Indigo-500 — audio/waveform feel */
private val PrimaryColor = Color(0xFF6366F1)
/** Secondary: Violet-500 */
private val SecondaryColor = Color(0xFF8B5CF6)
/** Pass: Emerald-500 */
private val PassColor = Color(0xFF10B981)
/** Warning: Amber-500 */
private val WarningColor = Color(0xFFF59E0B)
/** Fail: Red-500 */
private val FailColor = Color(0xFFEF4444)
/** Unknown: Gray */
private val UnknownColor = Color(0xFF8B949E)

// ================================================================
// Main Entry Point
// ================================================================

/**
 * ============================================================
 * BackgroundAudioScreen — 合规中心入口
 * ============================================================
 * Routes between dashboard and tool detail screens based on selectedToolId.
 *
 * @param viewModel BackgroundAudioViewModel instance
 * @param onNavigateToRelatedTool Callback when navigating to related tool
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackgroundAudioScreen(
    viewModel: BackgroundAudioViewModel,
    onNavigateToRelatedTool: (String) -> Unit = {}
) {
    val state by viewModel.state.collectAsState()

    // Collect effects
    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is BackgroundAudioEffect.ShowToast -> {
                    // Toast handled by caller or snackbar host
                }
                is BackgroundAudioEffect.CopyToClipboard -> {
                    // Clipboard handled by caller
                }
                is BackgroundAudioEffect.NavigateToRelatedTool -> {
                    onNavigateToRelatedTool(effect.toolId)
                }
                is BackgroundAudioEffect.ShareReport -> {
                    // Share handled by caller
                }
            }
        }
    }

    // Route to tool detail or dashboard
    if (state.selectedToolId != null) {
        val toolState = state.toolDetailStates[state.selectedToolId]
        if (toolState != null) {
            ToolDetailScreen(
                toolId = state.selectedToolId!!,
                toolState = toolState,
                onBack = { viewModel.sendIntent(BackgroundAudioIntent.BackToDashboard) },
                onRun = { config -> viewModel.sendIntent(BackgroundAudioIntent.RunTool(state.selectedToolId!!, config)) },
                onStop = { viewModel.sendIntent(BackgroundAudioIntent.StopTool(state.selectedToolId!!)) },
                onExport = { format -> viewModel.sendIntent(BackgroundAudioIntent.ExportReport(state.selectedToolId!!, format)) },
                onSelectCIScenarios = { scenarios -> viewModel.sendIntent(BackgroundAudioIntent.SelectCIScenarios(scenarios)) },
                onSelectTemplate = { template -> viewModel.sendIntent(BackgroundAudioIntent.SelectDegradationTemplate(template)) }
            )
        }
    } else {
        DashboardScreen(
            state = state,
            onRefresh = { viewModel.sendIntent(BackgroundAudioIntent.RefreshDashboard) },
            onSelectTool = { toolId -> viewModel.sendIntent(BackgroundAudioIntent.SelectTool(toolId)) }
        )
    }
}

// ================================================================
// Dashboard Screen
// ================================================================

/**
 * ============================================================
 * DashboardScreen — 合规仪表盘
 * ============================================================
 * Shows compliance score gauge, failing tools count, and 9 tool cards.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DashboardScreen(
    state: BackgroundAudioState,
    onRefresh: () -> Unit,
    onSelectTool: (String) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Background Audio 合规中心",
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    IconButton(onClick = onRefresh) {
                        Icon(Icons.Default.Refresh, contentDescription = "刷新")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PrimaryColor,
                    titleContentColor = Color.White,
                    actionIconContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            // Compliance Score Header
            ComplianceScoreHeader(
                score = state.complianceScore,
                totalTools = state.totalTools,
                failingTools = state.failingTools,
                isRefreshing = state.isRefreshing
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Tool Cards Grid
            ToolCardsGrid(
                toolStatuses = state.toolStatuses,
                onSelectTool = onSelectTool
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

/**
 * ============================================================
 * ComplianceScoreHeader — 合规评分头部
 * ============================================================
 * Shows circular compliance gauge + summary stats.
 */
@Composable
private fun ComplianceScoreHeader(
    score: Int,
    totalTools: Int,
    failingTools: Int,
    isRefreshing: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Score Gauge
            ComplianceScoreGauge(score = score)

            Spacer(modifier = Modifier.width(24.dp))

            // Stats
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "合规评分",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                StatRow(
                    label = "总工具数",
                    value = "$totalTools",
                    color = MaterialTheme.colorScheme.onSurface
                )
                StatRow(
                    label = "不合规/警告",
                    value = "$failingTools 项",
                    color = if (failingTools > 0) FailColor else PassColor
                )
                if (isRefreshing) {
                    Spacer(modifier = Modifier.height(4.dp))
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth(),
                        color = PrimaryColor
                    )
                }
            }
        }
    }
}

/**
 * ============================================================
 * ComplianceScoreGauge — 圆形合规评分仪表盘
 * ============================================================
 * Animated circular gauge showing score 0-100.
 */
@Composable
private fun ComplianceScoreGauge(score: Int) {
    val animatedProgress by animateFloatAsState(
        targetValue = score / 100f,
        animationSpec = tween(durationMillis = 800),
        label = "score_animation"
    )

    val gaugeColor = when {
        score >= 80 -> PassColor
        score >= 50 -> WarningColor
        else -> FailColor
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(100.dp)
    ) {
        CircularProgressIndicator(
            progress = { 1f },
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.surfaceVariant,
            strokeWidth = 10.dp,
            strokeCap = StrokeCap.Round
        )
        CircularProgressIndicator(
            progress = { animatedProgress },
            modifier = Modifier.fillMaxSize(),
            color = gaugeColor,
            strokeWidth = 10.dp,
            strokeCap = StrokeCap.Round
        )
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "$score",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = gaugeColor
            )
            Text(
                text = "/100",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * ============================================================
 * StatRow — 统计行
 * ============================================================
 */
@Composable
private fun StatRow(label: String, value: String, color: Color) {
    Row(
        modifier = Modifier.padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "$label: ",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = color
        )
    }
}

/**
 * ============================================================
 * ToolCardsGrid — 工具卡片网格
 * ============================================================
 * 3x3 grid of tool cards (9 tools total).
 */
@Composable
private fun ToolCardsGrid(
    toolStatuses: List<ToolStatus>,
    onSelectTool: (String) -> Unit
) {
    Column(modifier = Modifier.padding(horizontal = 12.dp)) {
        // First row: 4 cards
        if (toolStatuses.size >= 4) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                toolStatuses.take(4).forEach { tool ->
                    ToolCard(
                        tool = tool,
                        modifier = Modifier.weight(1f),
                        onClick = { onSelectTool(tool.toolId) }
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Second row: 4 cards
        if (toolStatuses.size >= 8) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                toolStatuses.drop(4).take(4).forEach { tool ->
                    ToolCard(
                        tool = tool,
                        modifier = Modifier.weight(1f),
                        onClick = { onSelectTool(tool.toolId) }
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Third row: remaining cards (1 card centered)
        if (toolStatuses.size > 8) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                toolStatuses.drop(8).forEach { tool ->
                    ToolCard(
                        tool = tool,
                        modifier = Modifier.width(160.dp),
                        onClick = { onSelectTool(tool.toolId) }
                    )
                }
            }
        }
    }
}

/**
 * ============================================================
 * ToolCard — 工具入口卡片
 * ============================================================
 * Card showing tool icon, name, and compliance status.
 */
@Composable
private fun ToolCard(
    tool: ToolStatus,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val statusColor = when (tool.status) {
        ComplianceStatus.PASS -> PassColor
        ComplianceStatus.WARNING -> WarningColor
        ComplianceStatus.FAIL -> FailColor
        ComplianceStatus.UNKNOWN -> UnknownColor
    }

    Card(
        modifier = modifier
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Status indicator dot + icon
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = tool.iconEmoji,
                    fontSize = 28.sp
                )
                // Status dot
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .align(Alignment.TopEnd)
                        .background(statusColor, CircleShape)
                        .border(1.5.dp, Color.White, CircleShape)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = tool.toolName,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = tool.status.emoji + " " + tool.status.displayName,
                style = MaterialTheme.typography.bodySmall,
                color = statusColor
            )
        }
    }
}

// ================================================================
// Tool Detail Screen
// ================================================================

/**
 * ============================================================
 * ToolDetailScreen — 工具详情页路由
 * ============================================================
 * Routes to the appropriate tool-specific detail screen.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ToolDetailScreen(
    toolId: String,
    toolState: ToolDetailState,
    onBack: () -> Unit,
    onRun: (Map<String, Any>) -> Unit,
    onStop: () -> Unit,
    onExport: (String) -> Unit,
    onSelectCIScenarios: (Set<CIScenario>) -> Unit,
    onSelectTemplate: (DegradationStrategyTemplate) -> Unit
) {
    val toolName = when (toolId) {
        ToolId.SILENT_FAILURE_DETECTOR -> "静默失败检测器"
        ToolId.FGS_CONFIG_VALIDATOR -> "FGS 配置验证器"
        ToolId.AUDIO_FOCUS_CHECKER -> "音频焦点合规检测"
        ToolId.CI_SCENARIO_SIMULATOR -> "CI 场景模拟器"
        ToolId.DEGRADATION_STRATEGY -> "降级策略模板"
        ToolId.BOOT_AUDIO_MIGRATION -> "BOOT+音频迁移"
        ToolId.BLUETOOTH_AUDIO_GUIDE -> "蓝牙自动播放指南"
        ToolId.AUDIO_ATTRIBUTES_GUIDE -> "AudioAttributes 解读"
        ToolId.FOCUS_LOSS_MONITOR -> "焦点丢失回调监控"
        else -> "工具"
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(toolName, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PrimaryColor,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Tool description
            Text(
                text = toolState.toolDescription,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(16.dp)
            )

            when (toolId) {
                ToolId.SILENT_FAILURE_DETECTOR -> SilentFailureDetectorDetail(toolState, onRun, onStop, onExport)
                ToolId.FGS_CONFIG_VALIDATOR -> FGSValidatorDetail(toolState, onRun, onStop, onExport)
                ToolId.AUDIO_FOCUS_CHECKER -> AudioFocusCheckerDetail(toolState, onRun, onStop, onExport)
                ToolId.CI_SCENARIO_SIMULATOR -> CISimulatorDetail(toolState, onRun, onStop, onExport, onSelectCIScenarios)
                ToolId.DEGRADATION_STRATEGY -> DegradationStrategyDetail(toolState, onRun, onStop, onExport, onSelectTemplate)
                ToolId.BOOT_AUDIO_MIGRATION -> BootAudioMigrationDetail(toolState, onRun, onStop, onExport)
                ToolId.BLUETOOTH_AUDIO_GUIDE -> BluetoothAudioGuideDetail(toolState, onRun, onStop, onExport)
                ToolId.AUDIO_ATTRIBUTES_GUIDE -> AudioAttributesGuideDetail(toolState, onRun, onStop, onExport)
                ToolId.FOCUS_LOSS_MONITOR -> FocusLossMonitorDetail(toolState, onRun, onStop, onExport)
            }
        }
    }
}

// ================================================================
// Tool Detail Implementations
// ================================================================

/**
 * Silent Failure Detector detail — shows timeline of silent failures.
 */
@Composable
private fun SilentFailureDetectorDetail(
    toolState: ToolDetailState,
    onRun: (Map<String, Any>) -> Unit,
    onStop: () -> Unit,
    onExport: (String) -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("事件列表", "配置", "报告")

    Column(modifier = Modifier.fillMaxSize()) {
        TabRow(selectedTabIndex = selectedTab) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title) }
                )
            }
        }

        when (selectedTab) {
            0 -> {
                // Events list
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(toolState.silentFailureEvents) { event ->
                        SilentFailureEventCard(event)
                    }
                    if (toolState.silentFailureEvents.isEmpty() && !toolState.isRunning) {
                        item {
                            EmptyStateCard("暂无静默失败事件", "点击「开始检测」运行扫描")
                        }
                    }
                }
            }
            1 -> {
                // Config tab
                ConfigRunPanel(
                    isRunning = toolState.isRunning,
                    progress = toolState.progress,
                    onRun = { onRun(emptyMap()) },
                    onStop = onStop
                )
            }
            2 -> {
                // Report tab
                ReportPanel(
                    report = toolState.report,
                    onExport = onExport
                )
            }
        }
    }
}

@Composable
private fun SilentFailureEventCard(event: SilentFailureEvent) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = FailColor.copy(alpha = 0.1f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "🔊", fontSize = 20.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = event.apiName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "原因: ${event.reason}",
                style = MaterialTheme.typography.bodyMedium,
                color = FailColor
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = event.stackTrace,
                style = MaterialTheme.typography.bodySmall,
                fontFamily = FontFamily.Monospace,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * FGS Config Validator detail — shows FGS compliance matrix.
 */
@Composable
private fun FGSValidatorDetail(
    toolState: ToolDetailState,
    onRun: (Map<String, Any>) -> Unit,
    onStop: () -> Unit,
    onExport: (String) -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("配置矩阵", "执行", "报告")

    Column(modifier = Modifier.fillMaxSize()) {
        TabRow(selectedTabIndex = selectedTab) {
            tabs.forEachIndexed { index, title ->
                Tab(selected = selectedTab == index, onClick = { selectedTab = index }, text = { Text(title) })
            }
        }

        when (selectedTab) {
            0 -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(toolState.fgsConfigItems) { item ->
                        FGSConfigItemCard(item)
                    }
                    if (toolState.fgsConfigItems.isEmpty() && !toolState.isRunning) {
                        item { EmptyStateCard("暂无配置数据", "点击「开始检测」扫描 FGS 配置") }
                    }
                }
            }
            1 -> ConfigRunPanel(isRunning = toolState.isRunning, progress = toolState.progress, onRun = { onRun(emptyMap()) }, onStop = onStop)
            2 -> ReportPanel(report = toolState.report, onExport = onExport)
        }
    }
}

@Composable
private fun FGSConfigItemCard(item: FGSConfigItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (item.isCompliant) PassColor.copy(alpha = 0.1f) else WarningColor.copy(alpha = 0.1f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (item.isCompliant) Icons.Default.Check else Icons.Default.Close,
                contentDescription = null,
                tint = if (item.isCompliant) PassColor else WarningColor
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = item.itemName, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Text(text = item.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (!item.isCompliant) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "修复: ${item.fixSuggestion}", style = MaterialTheme.typography.bodySmall, color = WarningColor)
                }
            }
        }
    }
}

/**
 * Audio Focus Checker detail.
 */
@Composable
private fun AudioFocusCheckerDetail(
    toolState: ToolDetailState,
    onRun: (Map<String, Any>) -> Unit,
    onStop: () -> Unit,
    onExport: (String) -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("检测结果", "执行", "报告")

    Column(modifier = Modifier.fillMaxSize()) {
        TabRow(selectedTabIndex = selectedTab) {
            tabs.forEachIndexed { index, title -> Tab(selected = selectedTab == index, onClick = { selectedTab = index }, text = { Text(title) }) }
        }

        when (selectedTab) {
            0 -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(toolState.audioFocusResults) { result ->
                        AudioFocusResultCard(result)
                    }
                    if (toolState.audioFocusResults.isEmpty() && !toolState.isRunning) {
                        item { EmptyStateCard("暂无检测结果", "点击「开始检测」") }
                    }
                }
            }
            1 -> ConfigRunPanel(isRunning = toolState.isRunning, progress = toolState.progress, onRun = { onRun(emptyMap()) }, onStop = onStop)
            2 -> ReportPanel(report = toolState.report, onExport = onExport)
        }
    }
}

@Composable
private fun AudioFocusResultCard(result: AudioFocusResult) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (result.willSucceedInBackground) PassColor.copy(alpha = 0.1f) else WarningColor.copy(alpha = 0.1f)
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(text = result.apiCall, style = MaterialTheme.typography.bodySmall, fontFamily = FontFamily.Monospace)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "场景: ${result.context}", style = MaterialTheme.typography.bodySmall)
            Text(text = "后台成功: ${if (result.willSucceedInBackground) "✅" else "❌"}", style = MaterialTheme.typography.bodySmall)
            Text(text = "建议: ${result.recommendation}", style = MaterialTheme.typography.bodySmall, color = WarningColor)
        }
    }
}

/**
 * CI Scenario Simulator detail — allows selecting scenarios.
 */
@Composable
private fun CISimulatorDetail(
    toolState: ToolDetailState,
    onRun: (Map<String, Any>) -> Unit,
    onStop: () -> Unit,
    onExport: (String) -> Unit,
    onSelectScenarios: (Set<CIScenario>) -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("场景选择", "测试结果", "报告")

    Column(modifier = Modifier.fillMaxSize()) {
        TabRow(selectedTabIndex = selectedTab) {
            tabs.forEachIndexed { index, title -> Tab(selected = selectedTab == index, onClick = { selectedTab = index }, text = { Text(title) }) }
        }

        when (selectedTab) {
            0 -> {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("选择要模拟的场景：", style = MaterialTheme.typography.titleSmall)
                    Spacer(modifier = Modifier.height(12.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(CIScenario.entries.toList()) { scenario ->
                            FilterChip(
                                selected = scenario in toolState.selectedScenarios,
                                onClick = {
                                    val newSet = if (scenario in toolState.selectedScenarios) {
                                        toolState.selectedScenarios - scenario
                                    } else {
                                        toolState.selectedScenarios + scenario
                                    }
                                    onSelectScenarios(newSet)
                                },
                                label = { Text(scenario.displayName) }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    ConfigRunPanel(isRunning = toolState.isRunning, progress = toolState.progress, onRun = { onRun(emptyMap()) }, onStop = onStop)
                }
            }
            1 -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(toolState.ciTestResults) { result ->
                        CITestResultCard(result)
                    }
                    if (toolState.ciTestResults.isEmpty() && !toolState.isRunning) {
                        item { EmptyStateCard("暂无测试结果", "选择场景后点击「开始模拟」") }
                    }
                }
            }
            2 -> ReportPanel(report = toolState.report, onExport = onExport)
        }
    }
}

@Composable
private fun CITestResultCard(result: CITestResult) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (result.passed) PassColor.copy(alpha = 0.1f) else FailColor.copy(alpha = 0.1f)
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = if (result.passed) "✅" else "❌", fontSize = 18.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = result.scenario.displayName, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "音频状态: ${result.audioStateSnapshot}", style = MaterialTheme.typography.bodySmall)
            Text(text = result.notes, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

/**
 * Degradation Strategy template generator.
 */
@Composable
private fun DegradationStrategyDetail(
    toolState: ToolDetailState,
    onRun: (Map<String, Any>) -> Unit,
    onStop: () -> Unit,
    onExport: (String) -> Unit,
    onSelectTemplate: (DegradationStrategyTemplate) -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("模板选择", "生成代码", "报告")

    Column(modifier = Modifier.fillMaxSize()) {
        TabRow(selectedTabIndex = selectedTab) {
            tabs.forEachIndexed { index, title -> Tab(selected = selectedTab == index, onClick = { selectedTab = index }, text = { Text(title) }) }
        }

        when (selectedTab) {
            0 -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(defaultDegradationTemplates) { template ->
                        DegradationTemplateCard(
                            template = template,
                            isSelected = toolState.selectedStrategy?.id == template.id,
                            onClick = { onSelectTemplate(template) }
                        )
                    }
                }
            }
            1 -> {
                Column(modifier = Modifier.fillMaxSize()) {
                    if (toolState.generatedCode != null) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .padding(16.dp)
                                .background(Color(0xFF1E1E1E), RoundedCornerShape(8.dp))
                                .padding(12.dp)
                        ) {
                            LazyColumn {
                                item {
                                    Text(
                                        text = toolState.generatedCode,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontFamily = FontFamily.Monospace,
                                        color = Color(0xFFD4D4D4)
                                    )
                                }
                            }
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = { onExport("markdown") },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = null)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("复制代码")
                            }
                        }
                    } else {
                        EmptyStateCard("未选择模板", "在上方选择一个降级策略模板")
                    }
                }
            }
            2 -> ReportPanel(report = toolState.report, onExport = onExport)
        }
    }
}

@Composable
private fun DegradationTemplateCard(
    template: DegradationStrategyTemplate,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .then(
                if (isSelected) Modifier.border(2.dp, PrimaryColor, RoundedCornerShape(12.dp))
                else Modifier
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) PrimaryColor.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = template.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = template.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            if (isSelected) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "✓ 已选择", color = PrimaryColor, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

/**
 * BOOT Audio Migration detail.
 */
@Composable
private fun BootAudioMigrationDetail(
    toolState: ToolDetailState,
    onRun: (Map<String, Any>) -> Unit,
    onStop: () -> Unit,
    onExport: (String) -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("迁移 Diff", "执行", "报告")

    Column(modifier = Modifier.fillMaxSize()) {
        TabRow(selectedTabIndex = selectedTab) {
            tabs.forEachIndexed { index, title -> Tab(selected = selectedTab == index, onClick = { selectedTab = index }, text = { Text(title) }) }
        }

        when (selectedTab) {
            0 -> {
                Column(modifier = Modifier.fillMaxSize()) {
                    if (toolState.migrationDiff != null) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .padding(16.dp)
                                .background(Color(0xFF1E1E1E), RoundedCornerShape(8.dp))
                                .padding(12.dp)
                        ) {
                            LazyColumn {
                                item {
                                    Text(
                                        text = toolState.migrationDiff,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontFamily = FontFamily.Monospace,
                                        color = Color(0xFFD4D4D4)
                                    )
                                }
                            }
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(onClick = { onExport("markdown") }, modifier = Modifier.weight(1f)) {
                                Icon(Icons.Default.ContentCopy, contentDescription = null)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("复制 Diff")
                            }
                        }
                    } else {
                        EmptyStateCard("无迁移 Diff", "点击「生成迁移 Diff」创建")
                    }
                }
            }
            1 -> ConfigRunPanel(isRunning = toolState.isRunning, progress = toolState.progress, onRun = { onRun(emptyMap()) }, onStop = onStop)
            2 -> ReportPanel(report = toolState.report, onExport = onExport)
        }
    }
}

/**
 * Bluetooth Audio Guide detail.
 */
@Composable
private fun BluetoothAudioGuideDetail(
    toolState: ToolDetailState,
    onRun: (Map<String, Any>) -> Unit,
    onStop: () -> Unit,
    onExport: (String) -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("权限状态", "执行", "报告")

    Column(modifier = Modifier.fillMaxSize()) {
        TabRow(selectedTabIndex = selectedTab) {
            tabs.forEachIndexed { index, title -> Tab(selected = selectedTab == index, onClick = { selectedTab = index }, text = { Text(title) }) }
        }

        when (selectedTab) {
            0 -> {
                Column(modifier = Modifier.padding(16.dp)) {
                    val status = toolState.bluetoothPermissionStatus
                    if (status != null) {
                        BluetoothPermissionRow("BLUETOOTH_CONNECT 权限", status.hasBluetoothConnectPermission)
                        BluetoothPermissionRow("BLUETOOTH_SCAN 权限", status.hasBluetoothScanPermission)
                        BluetoothPermissionRow("音频播放服务声明", status.hasAudioPlaybackService)
                        BluetoothPermissionRow("蓝牙自动播放逻辑", status.autoPlayOnConnect)
                    } else {
                        EmptyStateCard("暂无权限数据", "点击「开始检测」扫描权限配置")
                    }
                }
            }
            1 -> ConfigRunPanel(isRunning = toolState.isRunning, progress = toolState.progress, onRun = { onRun(emptyMap()) }, onStop = onStop)
            2 -> ReportPanel(report = toolState.report, onExport = onExport)
        }
    }
}

@Composable
private fun BluetoothPermissionRow(label: String, isGranted: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (isGranted) Icons.Default.Check else Icons.Default.Close,
            contentDescription = null,
            tint = if (isGranted) PassColor else FailColor
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(text = label, style = MaterialTheme.typography.bodyMedium)
    }
}

/**
 * AudioAttributes Guide detail.
 */
@Composable
private fun AudioAttributesGuideDetail(
    toolState: ToolDetailState,
    onRun: (Map<String, Any>) -> Unit,
    onStop: () -> Unit,
    onExport: (String) -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("属性列表", "执行", "报告")

    Column(modifier = Modifier.fillMaxSize()) {
        TabRow(selectedTabIndex = selectedTab) {
            tabs.forEachIndexed { index, title -> Tab(selected = selectedTab == index, onClick = { selectedTab = index }, text = { Text(title) }) }
        }

        when (selectedTab) {
            0 -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(toolState.audioAttributesItems) { item ->
                        AudioAttributesItemCard(item)
                    }
                    if (toolState.audioAttributesItems.isEmpty() && !toolState.isRunning) {
                        item { EmptyStateCard("暂无属性数据", "点击「开始检测」扫描 AudioAttributes") }
                    }
                }
            }
            1 -> ConfigRunPanel(isRunning = toolState.isRunning, progress = toolState.progress, onRun = { onRun(emptyMap()) }, onStop = onStop)
            2 -> ReportPanel(report = toolState.report, onExport = onExport)
        }
    }
}

@Composable
private fun AudioAttributesItemCard(item: AudioAttributesItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (item.isCompliant) PassColor.copy(alpha = 0.1f) else WarningColor.copy(alpha = 0.1f)
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = item.attributeKey, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = if (item.isCompliant) "✅ 合规" else "⚠️ 需关注", color = if (item.isCompliant) PassColor else WarningColor)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "ContentType: ${item.contentType} | Flags: ${item.flags}", style = MaterialTheme.typography.bodySmall)
            Text(text = "Android 17: ${item.android17Change}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            if (!item.isCompliant) {
                Text(text = "建议: ${item.recommendation}", style = MaterialTheme.typography.bodySmall, color = WarningColor)
            }
        }
    }
}

/**
 * Focus Loss Monitor detail.
 */
@Composable
private fun FocusLossMonitorDetail(
    toolState: ToolDetailState,
    onRun: (Map<String, Any>) -> Unit,
    onStop: () -> Unit,
    onExport: (String) -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("回调列表", "执行", "报告")

    Column(modifier = Modifier.fillMaxSize()) {
        TabRow(selectedTabIndex = selectedTab) {
            tabs.forEachIndexed { index, title -> Tab(selected = selectedTab == index, onClick = { selectedTab = index }, text = { Text(title) }) }
        }

        when (selectedTab) {
            0 -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(toolState.focusLossCallbacks) { cb ->
                        FocusLossCallbackCard(cb)
                    }
                    if (toolState.focusLossCallbacks.isEmpty() && !toolState.isRunning) {
                        item { EmptyStateCard("暂无回调数据", "点击「开始检测」扫描焦点丢失处理") }
                    }
                }
            }
            1 -> ConfigRunPanel(isRunning = toolState.isRunning, progress = toolState.progress, onRun = { onRun(emptyMap()) }, onStop = onStop)
            2 -> ReportPanel(report = toolState.report, onExport = onExport)
        }
    }
}

@Composable
private fun FocusLossCallbackCard(cb: FocusLossCallback) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (cb.isComplete) PassColor.copy(alpha = 0.1f) else WarningColor.copy(alpha = 0.1f)
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = cb.listenerClass, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = if (cb.isComplete) "✅ 完整" else "⚠️ 不完整", color = if (cb.isComplete) PassColor else WarningColor)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "已处理: ${cb.callbackMethods.joinToString()}", style = MaterialTheme.typography.bodySmall)
            if (cb.missingHandlers.isNotEmpty()) {
                Text(text = "缺失: ${cb.missingHandlers.joinToString()}", style = MaterialTheme.typography.bodySmall, color = WarningColor)
            }
        }
    }
}

// ================================================================
// Shared Components
// ================================================================

/**
 * ConfigRunPanel — 执行面板
 */
@Composable
private fun ConfigRunPanel(
    isRunning: Boolean,
    progress: Float,
    onRun: () -> Unit,
    onStop: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (isRunning) {
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth(),
                color = PrimaryColor
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "运行中... ${(progress * 100).toInt()}%", style = MaterialTheme.typography.bodySmall)
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedButton(onClick = onStop) {
                Icon(Icons.Default.Stop, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("停止")
            }
        } else {
            Button(
                onClick = onRun,
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("开始检测 / Start Scan")
            }
        }
    }
}

/**
 * ReportPanel — 报告面板
 */
@Composable
private fun ReportPanel(
    report: ToolReport?,
    onExport: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        if (report != null) {
            // Status badge
            Surface(
                color = report.status.color.copy(alpha = 0.15f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "${report.status.emoji} ${report.status.displayName}",
                    color = report.status.color,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = report.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = report.summary, style = MaterialTheme.typography.bodyMedium)

            if (report.findings.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(text = "发现 (${report.findings.size})", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                report.findings.forEach { finding ->
                    FindingCard(finding)
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            if (report.recommendations.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(text = "建议", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                report.recommendations.forEach { rec ->
                    Text(text = "• $rec", style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(bottom = 4.dp))
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = { onExport("markdown") }, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("复制 Markdown")
                }
                OutlinedButton(onClick = { onExport("json") }, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Default.Share, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("复制 JSON")
                }
            }
        } else {
            EmptyStateCard("暂无报告", "先运行工具生成报告")
        }
    }
}

@Composable
private fun FindingCard(finding: Finding) {
    val severityColor = when (finding.severity) {
        "HIGH" -> FailColor
        "MEDIUM" -> WarningColor
        else -> PassColor
    }
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = severityColor.copy(alpha = 0.08f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(color = severityColor, shape = CircleShape) {
                    Text(
                        text = finding.severity,
                        color = Color.White,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = finding.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = finding.description, style = MaterialTheme.typography.bodySmall)
            Text(text = "位置: ${finding.location}", style = MaterialTheme.typography.bodySmall, fontFamily = FontFamily.Monospace)
            Text(text = "修复: ${finding.fixSuggestion}", style = MaterialTheme.typography.bodySmall, color = severityColor)
        }
    }
}

/**
 * EmptyStateCard — 空状态卡片
 */
@Composable
private fun EmptyStateCard(title: String, subtitle: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "📋", fontSize = 36.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = title, style = MaterialTheme.typography.titleSmall, textAlign = TextAlign.Center)
            Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
        }
    }
}
