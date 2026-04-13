package com.mvi.kenny.feature.gemma4

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ConnectWithoutContact
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Monitor
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.Token
import androidx.compose.material3.Badge
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mvi.kenny.base.TopBarConfig
import kotlinx.coroutines.flow.collectLatest

/**
 * ============================================================
 * Gemma4DashboardScreen — Gemma 4 Agent Mode 工具包主仪表盘
 * ================================================================
 * Main dashboard entry point for the Gemma 4 Agent Mode toolkit.
 *
 * PRD-098: Gemma 4 × Android Studio Agent Mode 本地编码 Agent 工具链
 * Design Reference: memory/agency/designs/PRD-098-Gemma-4-Agent-Mode工具链.md
 *
 * Features:
 * - Connection status overview with connect/disconnect
 * - Model variant selector (2B / 7B / 9B)
 * - Quick access cards for 5 core sub-tools
 * - Active session stats (tool calls, tokens)
 * - Real-time mode toggle
 *
 * Design Spec (per design doc §7):
 * - Primary color: #4285F4 (Google Blue)
 * - Background: #1E1E1E (dark, matching Android Studio)
 * - Surface: #2D2D2D
 * - Success: #34A853 / Warning: #FBBC04 / Error: #EA4335
 * - Text primary: #E8EAED / Text secondary: #9AA0A6
 * —————————————————————————————————————————————————————
 */

// Design spec colors / 设计规范颜色
private val GoogleBlue = Color(0xFF4285F4)
private val GoogleGreen = Color(0xFF34A853)
private val GoogleYellow = Color(0xFFFBBC04)
private val GoogleRed = Color(0xFFEA4335)
private val DarkBackground = Color(0xFF1E1E1E)
private val DarkSurface = Color(0xFF2D2D2D)
private val DarkCard = Color(0xFF2D2D2D)
private val TextPrimary = Color(0xFFE8EAED)
private val TextSecondary = Color(0xFF9AA0A6)

/**
 * ============================================================
 * Gemma4DashboardScreen — 主入口 Composable
 * ================================================================
 *
 * @param viewModel Gemma4ViewModel instance
 * @param onNavigateToRoute Callback for navigation to sub-screens
 * @param onUpdateTopBar Callback to update the TopAppBar title
 * @param onShowSnackbar Callback for showing snackbar messages
 */
@Composable
fun Gemma4DashboardScreen(
    viewModel: Gemma4ViewModel = viewModel(),
    onNavigateToRoute: (Gemma4Route) -> Unit = {},
    onUpdateTopBar: (TopBarConfig) -> Unit = {},
    onShowSnackbar: (String) -> Unit = {}
) {
    val state by viewModel.state.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }

    // Update TopBar title / 更新顶部栏标题
    LaunchedEffect(Unit) {
        onUpdateTopBar(TopBarConfig(
            title = "Gemma 4 Agent Toolkit"
        ))
    }

    // Collect effects for snackbar / 收集副作用用于 snackbar
    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is Gemma4Effect.ShowError -> onShowSnackbar(effect.message)
                is Gemma4Effect.ConnectionSuccess -> onShowSnackbar("Gemma 4 连接成功")
                is Gemma4Effect.ConnectionFailed -> onShowSnackbar("连接失败: ${effect.reason}")
                is Gemma4Effect.ShowFallbackNotification -> onShowSnackbar("降级通知: ${effect.from} → ${effect.to}")
                is Gemma4Effect.BenchmarkCompleted -> onShowSnackbar("基准测试完成: ${effect.result.winner} 配置胜出")
                is Gemma4Effect.RecommendationReady -> onShowSnackbar("推荐生成完成 (${effect.count} 个方案)")
            }
        }
    }

    // Sub-tab titles / 子标签页标题
    val tabTitles = listOf("仪表盘", "变体选型", "调用追踪", "上下文注入", "质量基准", "降级策略")
    val tabIcons = listOf(
        Icons.Default.Memory,
        Icons.Default.Hub,
        Icons.Default.Terminal,
        Icons.Default.Description,
        Icons.Default.Speed,
        Icons.Default.Security
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // ─────────────────────────────────────────────────
        // Connection Status Header / 连接状态头部
        // ─────────────────────────────────────────────────
        item {
            ConnectionStatusHeader(
                status = state.connectionStatus,
                currentModel = state.currentModel,
                onConnect = { viewModel.sendIntent(Gemma4Intent.ConnectAgent) },
                onDisconnect = { viewModel.sendIntent(Gemma4Intent.DisconnectAgent) }
            )
        }

        // ─────────────────────────────────────────────────
        // Session Stats / 会话统计
        // ─────────────────────────────────────────────────
        state.activeSession?.let { session ->
            item {
                SessionStatsCard(session = session)
            }
        }

        // ─────────────────────────────────────────────────
        // Variant Selector / 变体选择器
        // ─────────────────────────────────────────────────
        item {
            VariantSelectorCard(
                currentVariant = state.currentModel,
                onVariantSelected = { viewModel.sendIntent(Gemma4Intent.SelectVariant(it)) }
            )
        }

        // ─────────────────────────────────────────────────
        // Quick Access Cards / 快速入口卡片
        // ─────────────────────────────────────────────────
        item {
            Text(
                text = "核心工具 / Core Tools",
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary,
                fontWeight = FontWeight.SemiBold
            )
        }

        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                val quickAccessItems = listOf(
                    QuickAccessItem(
                        route = Gemma4Route.VariantAdvisor,
                        title = "变体选型顾问",
                        titleEn = "Variant Advisor",
                        icon = Icons.Default.Hub,
                        description = "智能推荐最优变体"
                    ),
                    QuickAccessItem(
                        route = Gemma4Route.ToolCallVisualizer,
                        title = "工具调用追踪",
                        titleEn = "Tool Call",
                        icon = Icons.Default.Terminal,
                        description = "实时可视化调用链"
                    ),
                    QuickAccessItem(
                        route = Gemma4Route.ContextInjection,
                        title = "上下文注入",
                        titleEn = "Context Inject",
                        icon = Icons.Default.Description,
                        description = "配置项目上下文"
                    ),
                    QuickAccessItem(
                        route = Gemma4Route.QualityBenchmark,
                        title = "质量基准测试",
                        titleEn = "Benchmark",
                        icon = Icons.Default.Speed,
                        description = "代码质量评估"
                    ),
                    QuickAccessItem(
                        route = Gemma4Route.FallbackStrategy,
                        title = "降级策略",
                        titleEn = "Fallback",
                        icon = Icons.Default.Security,
                        description = "Gemma ↔ Gemini"
                    )
                )
                items(quickAccessItems) { item ->
                    QuickAccessCard(
                        item = item,
                        onClick = { onNavigateToRoute(item.route) }
                    )
                }
            }
        }

        // ─────────────────────────────────────────────────
        // Recent Tool Calls / 最近工具调用
        // ─────────────────────────────────────────────────
        if (state.toolCalls.isNotEmpty()) {
            item {
                Text(
                    text = "最近工具调用 / Recent Tool Calls",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                    fontWeight = FontWeight.SemiBold
                )
            }

            items(state.toolCalls.takeLast(5).reversed()) { call ->
                RecentToolCallItem(call = call)
            }
        }

        // ─────────────────────────────────────────────────
        // Benchmark History / 基准测试历史
        // ─────────────────────────────────────────────────
        if (state.benchmarkHistory.isNotEmpty()) {
            item {
                Text(
                    text = "基准测试历史 / Benchmark History",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                    fontWeight = FontWeight.SemiBold
                )
            }

            items(state.benchmarkHistory.takeLast(3).reversed()) { result ->
                BenchmarkHistoryItem(result = result)
            }
        }
    }
}

// ================================================================
// Sub-Components / 子组件
// ================================================================

/**
 * Quick access card data class.
 */
private data class QuickAccessItem(
    val route: Gemma4Route,
    val title: String,
    val titleEn: String,
    val icon: ImageVector,
    val description: String
)

/**
 * Connection status header with connect/disconnect button.
 */
@Composable
private fun ConnectionStatusHeader(
    status: ConnectionStatus,
    currentModel: GemmaVariant,
    onConnect: () -> Unit,
    onDisconnect: () -> Unit
) {
    val statusColor = when (status) {
        ConnectionStatus.Connected -> GoogleGreen
        ConnectionStatus.Connecting -> GoogleYellow
        ConnectionStatus.Disconnected -> TextSecondary
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(statusColor)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Gemma 4 Agent",
                        color = TextPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "${status.displayName} · ${currentModel.displayName}",
                        color = TextSecondary,
                        fontSize = 13.sp
                    )
                }
            }

            when (status) {
                ConnectionStatus.Disconnected -> {
                    Button(
                        onClick = onConnect,
                        colors = ButtonDefaults.buttonColors(containerColor = GoogleBlue)
                    ) {
                        Icon(Icons.Default.ConnectWithoutContact, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("连接")
                    }
                }
                ConnectionStatus.Connecting -> {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = GoogleBlue,
                        strokeWidth = 2.dp
                    )
                }
                ConnectionStatus.Connected -> {
                    Button(
                        onClick = onDisconnect,
                        colors = ButtonDefaults.buttonColors(containerColor = GoogleRed)
                    ) {
                        Text("断开")
                    }
                }
            }
        }
    }
}

/**
 * Session statistics card.
 */
@Composable
private fun SessionStatsCard(session: AgentSession) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "当前会话 / Session: ${session.sessionId}",
                color = TextPrimary,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(label = "开始时间", value = session.startTime, color = TextSecondary)
                StatItem(label = "调用次数", value = session.toolCallCount.toString(), color = GoogleBlue)
                StatItem(label = "Token消耗", value = "${session.totalTokenUsage}", color = GoogleYellow)
                StatItem(
                    label = "成功率",
                    value = if (session.toolCallCount > 0) "${(session.successCount * 100 / session.toolCallCount)}%" else "—",
                    color = GoogleGreen
                )
            }
        }
    }
}

/**
 * Single statistic item.
 */
@Composable
private fun StatItem(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, color = color, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Text(text = label, color = TextSecondary, fontSize = 11.sp)
    }
}

/**
 * Variant selector chips.
 */
@Composable
private fun VariantSelectorCard(
    currentVariant: GemmaVariant,
    onVariantSelected: (GemmaVariant) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "选择模型变体 / Model Variant",
                color = TextPrimary,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                GemmaVariant.entries.forEach { variant ->
                    val isSelected = variant == currentVariant
                    FilterChip(
                        selected = isSelected,
                        onClick = { onVariantSelected(variant) },
                        label = {
                            Column {
                                Text(variant.displayName, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
                                Text(
                                    text = variant.vramRequirement,
                                    fontSize = 10.sp,
                                    color = if (isSelected) TextPrimary.copy(alpha = 0.7f) else TextSecondary
                                )
                            }
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = GoogleBlue,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }
        }
    }
}

/**
 * Quick access card for sub-tool navigation.
 */
@Composable
private fun QuickAccessCard(
    item: QuickAccessItem,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(140.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = DarkCard),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(GoogleBlue.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(item.icon, contentDescription = null, tint = GoogleBlue, modifier = Modifier.size(22.dp))
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = item.title,
                color = TextPrimary,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp,
                maxLines = 1
            )
            Text(
                text = item.titleEn,
                color = GoogleBlue,
                fontSize = 10.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = item.description,
                color = TextSecondary,
                fontSize = 11.sp,
                maxLines = 2
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "进入", color = GoogleBlue, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                Icon(
                    Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = GoogleBlue,
                    modifier = Modifier.size(12.dp)
                )
            }
        }
    }
}

/**
 * Recent tool call list item.
 */
@Composable
private fun RecentToolCallItem(call: ToolCallEntry) {
    val statusColor = when (call.status) {
        ToolCallStatus.Success -> GoogleGreen
        ToolCallStatus.Failed -> GoogleRed
        ToolCallStatus.Running -> GoogleYellow
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height(36.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(statusColor)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "[${call.timestamp}]",
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = call.toolType.displayName,
                        color = TextPrimary,
                        fontWeight = FontWeight.Medium,
                        fontSize = 13.sp
                    )
                }
                Text(
                    text = call.params,
                    color = TextSecondary,
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            when (call.status) {
                ToolCallStatus.Success -> Icon(Icons.Default.CheckCircle, contentDescription = null, tint = GoogleGreen, modifier = Modifier.size(16.dp))
                ToolCallStatus.Failed -> Icon(Icons.Default.Error, contentDescription = null, tint = GoogleRed, modifier = Modifier.size(16.dp))
                ToolCallStatus.Running -> CircularProgressIndicator(modifier = Modifier.size(14.dp), color = GoogleYellow, strokeWidth = 2.dp)
            }
        }
    }
}

/**
 * Benchmark history item.
 */
@Composable
private fun BenchmarkHistoryItem(result: BenchmarkResult) {
    val winnerColor = when (result.winner) {
        "A" -> GoogleYellow
        "B" -> GoogleGreen
        else -> TextSecondary
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = result.taskDescription,
                    color = TextPrimary,
                    fontWeight = FontWeight.Medium,
                    fontSize = 13.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = result.timestamp,
                    color = TextSecondary,
                    fontSize = 11.sp
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "A: ${result.overallScoreA}", color = TextSecondary, fontSize = 12.sp)
                Text(text = " vs ", color = TextSecondary, fontSize = 11.sp)
                Text(text = "B: ${result.overallScoreB}", color = TextSecondary, fontSize = 12.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Badge(containerColor = winnerColor) {
                    Text(text = "${result.winner}胜", color = Color.White, fontSize = 10.sp)
                }
            }
        }
    }
}

/**
 * Navigation routes for Gemma4 sub-screens.
 */
enum class Gemma4Route(val route: String) {
    VariantAdvisor("gemma4_variant_advisor"),
    ToolCallVisualizer("gemma4_tool_call_visualizer"),
    ContextInjection("gemma4_context_injection"),
    QualityBenchmark("gemma4_quality_benchmark"),
    FallbackStrategy("gemma4_fallback_strategy")
}
