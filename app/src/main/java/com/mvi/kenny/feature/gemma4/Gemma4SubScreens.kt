package com.mvi.kenny.feature.gemma4

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.Token
import androidx.compose.material3.Badge
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mvi.kenny.base.TopBarConfig
import kotlinx.coroutines.flow.collectLatest
import kotlin.math.cos
import kotlin.math.sin

// ================================================================
// Design Spec Colors / 设计规范颜色
// ================================================================
private val GoogleBlue = Color(0xFF4285F4)
private val GoogleGreen = Color(0xFF34A853)
private val GoogleYellow = Color(0xFFFBBC04)
private val GoogleRed = Color(0xFFEA4335)
private val DarkBackground = Color(0xFF1E1E1E)
private val DarkSurface = Color(0xFF2D2D2D)
private val DarkCard = Color(0xFF2D2D2D)
private val TextPrimary = Color(0xFFE8EAED)
private val TextSecondary = Color(0xFF9AA0A6)

// ================================================================
// Gemma4Screen — 主入口（Tab 式导航）
// ================================================================

/**
 * ============================================================
 * Gemma4Screen — Gemma 4 工具包主入口（含 Tab 导航）
 * ================================================================
 * Tab 式导航聚合所有子屏幕：
 * 0: 仪表盘 / Dashboard
 * 1: 变体选型 / Variant Advisor
 * 2: 调用追踪 / Tool Call Visualizer
 * 3: 上下文注入 / Context Injection
 * 4: 质量基准 / Quality Benchmark
 * 5: 降级策略 / Fallback Strategy
 */
@Composable
fun Gemma4Screen(
    onUpdateTopBar: (TopBarConfig) -> Unit,
    onShowSnackbar: (String) -> Unit = {},
    viewModel: Gemma4ViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }

    val tabTitles = listOf("仪表盘", "变体选型", "调用追踪", "上下文注入", "质量基准", "降级策略")

    LaunchedEffect(selectedTab) {
        val subtitles = listOf(
            state.connectionStatus.displayName,
            "智能推荐",
            "${state.totalCalls} 次调用",
            "${state.contextConfig.enabledContexts.count { it.value }} 项已启用",
            "${state.benchmarkHistory.size} 条记录",
            state.fallbackMode.displayName
        )
        onUpdateTopBar(TopBarConfig(
            title = "Gemma 4 Agent Toolkit"
        ))
    }

    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is Gemma4Effect.ShowError -> onShowSnackbar(effect.message)
                is Gemma4Effect.ConnectionSuccess -> onShowSnackbar("Gemma 4 连接成功")
                is Gemma4Effect.ConnectionFailed -> onShowSnackbar("连接失败: ${effect.reason}")
                is Gemma4Effect.ShowFallbackNotification -> onShowSnackbar("降级: ${effect.from} → ${effect.to}")
                is Gemma4Effect.BenchmarkCompleted -> onShowSnackbar("基准测试完成: ${effect.result.winner} 胜出")
                is Gemma4Effect.RecommendationReady -> onShowSnackbar("推荐就绪 (${effect.count} 个方案)")
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(DarkBackground)) {
        // Tab Row / 标签页导航
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = DarkSurface,
            contentColor = GoogleBlue,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = GoogleBlue
                )
            }
        ) {
            tabTitles.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = {
                        Text(
                            text = title,
                            fontSize = 12.sp,
                            fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedTab == index) GoogleBlue else TextSecondary
                        )
                    }
                )
            }
        }

        // Content / 内容区
        Box(modifier = Modifier.fillMaxSize()) {
            when (selectedTab) {
                0 -> Gemma4DashboardScreen(
                    viewModel = viewModel,
                    onNavigateToRoute = { route ->
                        selectedTab = when (route) {
                            Gemma4Route.VariantAdvisor -> 1
                            Gemma4Route.ToolCallVisualizer -> 2
                            Gemma4Route.ContextInjection -> 3
                            Gemma4Route.QualityBenchmark -> 4
                            Gemma4Route.FallbackStrategy -> 5
                        }
                    },
                    onUpdateTopBar = {},
                    onShowSnackbar = onShowSnackbar
                )
                1 -> VariantAdvisorScreen(viewModel = viewModel)
                2 -> ToolCallVisualizerScreen(viewModel = viewModel)
                3 -> ContextInjectionScreen(viewModel = viewModel)
                4 -> QualityBenchmarkScreen(viewModel = viewModel)
                5 -> FallbackStrategyScreen(viewModel = viewModel)
            }
        }
    }
}

// ================================================================
// VariantAdvisorScreen — 变体选型顾问
// ================================================================

/**
 * ============================================================
 * VariantAdvisorScreen — 变体选型顾问
 * ================================================================
 * Helps developers select the optimal Gemma 4 variant (2B/7B/9B)
 * based on their project characteristics.
 *
 * Features:
 * - Project profile input form
 * - Variant recommendation cards sorted by confidence
 * - Hardware requirements and speed comparison
 */
@Composable
fun VariantAdvisorScreen(viewModel: Gemma4ViewModel) {
    val state by viewModel.state.collectAsState()
    var profile by remember { mutableStateOf(state.projectProfile) }

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(DarkBackground),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "项目特征 / Project Profile",
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary,
                fontWeight = FontWeight.SemiBold
            )
        }

        // Project size selector / 项目规模选择
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "代码规模 / Code Size", color = TextPrimary, fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("1K-5K", "5K-10K", "10K-50K", "50K-100K", "100K+").forEach { range ->
                            FilterChip(
                                selected = profile.codeLineRange == range,
                                onClick = {
                                    profile = profile.copy(codeLineRange = range)
                                    viewModel.sendIntent(Gemma4Intent.UpdateProjectProfile(profile))
                                },
                                label = { Text(range, fontSize = 11.sp) },
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

        // Module count slider / 模块数量滑块
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Text(text = "Module 数量", color = TextPrimary, fontWeight = FontWeight.Medium)
                        Text(text = "${profile.moduleCount} 个", color = GoogleBlue, fontWeight = FontWeight.Bold)
                    }
                    Slider(
                        value = profile.moduleCount.toFloat(),
                        onValueChange = {
                            profile = profile.copy(moduleCount = it.toInt())
                            viewModel.sendIntent(Gemma4Intent.UpdateProjectProfile(profile))
                        },
                        valueRange = 1f..20f,
                        steps = 18,
                        colors = SliderDefaults.colors(thumbColor = GoogleBlue, activeTrackColor = GoogleBlue)
                    )
                }
            }
        }

        // Feature toggles / 功能开关
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "项目特性 / Project Features", color = TextPrimary, fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.height(8.dp))
                    ToggleRow(
                        label = "Kotlin Multiplatform (KMP)",
                        checked = profile.hasKMP,
                        onCheckedChange = {
                            profile = profile.copy(hasKMP = it)
                            viewModel.sendIntent(Gemma4Intent.UpdateProjectProfile(profile))
                        }
                    )
                    ToggleRow(
                        label = "含 Native 代码 (C++/NDK)",
                        checked = profile.hasNativeCode,
                        onCheckedChange = {
                            profile = profile.copy(hasNativeCode = it)
                            viewModel.sendIntent(Gemma4Intent.UpdateProjectProfile(profile))
                        }
                    )
                    ToggleRow(
                        label = "有独立显卡 (GPU)",
                        checked = profile.hasDiscreteGPU,
                        onCheckedChange = {
                            profile = profile.copy(hasDiscreteGPU = it)
                            viewModel.sendIntent(Gemma4Intent.UpdateProjectProfile(profile))
                        }
                    )
                }
            }
        }

        // Hardware config / 硬件配置
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Text(text = "内存 (RAM)", color = TextPrimary, fontWeight = FontWeight.Medium)
                        Text(text = "${profile.totalMemoryGB} GB", color = GoogleBlue, fontWeight = FontWeight.Bold)
                    }
                    Slider(
                        value = profile.totalMemoryGB.toFloat(),
                        onValueChange = {
                            profile = profile.copy(totalMemoryGB = it.toInt())
                            viewModel.sendIntent(Gemma4Intent.UpdateProjectProfile(profile))
                        },
                        valueRange = 4f..64f,
                        steps = 11,
                        colors = SliderDefaults.colors(thumbColor = GoogleBlue, activeTrackColor = GoogleBlue)
                    )
                }
            }
        }

        // Analyze button / 分析按钮
        item {
            Button(
                onClick = { viewModel.sendIntent(Gemma4Intent.RequestVariantRecommendation) },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = GoogleBlue),
                enabled = state.scanStatus != ScanStatus.Scanning
            ) {
                if (state.scanStatus == ScanStatus.Scanning) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("分析中...")
                } else {
                    Icon(Icons.Default.Code, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("开始分析 / Analyze")
                }
            }
        }

        // Recommendations / 推荐结果
        if (state.recommendations.isNotEmpty()) {
            item {
                HorizontalDivider(color = DarkCard, thickness = 1.dp)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "推荐结果 / Recommendations",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                    fontWeight = FontWeight.SemiBold
                )
            }

            items(state.recommendations) { rec ->
                VariantRecommendCard(recommendation = rec)
            }
        }
    }
}

/**
 * Recommendation card for a specific variant.
 */
@Composable
private fun VariantRecommendCard(recommendation: VariantRecommendation) {
    val variant = recommendation.variant
    val confidenceColor = when {
        recommendation.confidenceScore >= 0.75f -> GoogleGreen
        recommendation.confidenceScore >= 0.5f -> GoogleYellow
        else -> TextSecondary
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = variant.displayName,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Text(
                        text = "Confidence: ${(recommendation.confidenceScore * 100).toInt()}%",
                        color = confidenceColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
                Badge(containerColor = confidenceColor) {
                    Text("${variant.paramCount} params", color = Color.White, fontSize = 10.sp)
                }
            }
            Spacer(modifier = Modifier.height(10.dp))

            // VRAM and Speed info / 显存和速度
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Column {
                    Text(text = "显存需求", color = TextSecondary, fontSize = 11.sp)
                    Text(text = variant.vramRequirement, color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                }
                Column {
                    Text(text = "推理速度", color = TextSecondary, fontSize = 11.sp)
                    Text(text = variant.speedRating, color = TextPrimary, fontSize = 12.sp)
                }
                Column {
                    Text(text = "预估耗时", color = TextSecondary, fontSize = 11.sp)
                    Text(text = recommendation.estimatedTime, color = GoogleBlue, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            recommendation.reasons.forEach { reason ->
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 2.dp)) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = GoogleGreen, modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = reason, color = TextSecondary, fontSize = 12.sp)
                }
            }
        }
    }
}

/**
 * Toggle row with label and switch.
 */
@Composable
private fun ToggleRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, color = TextPrimary, fontSize = 13.sp)
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(checkedThumbColor = GoogleBlue, checkedTrackColor = GoogleBlue.copy(alpha = 0.5f))
        )
    }
}

// ================================================================
// ToolCallVisualizerScreen — 工具调用可视化面板
// ================================================================

/**
 * ============================================================
 * ToolCallVisualizerScreen — 工具调用追踪面板
 * ================================================================
 * Real-time visualization of Gemma 4 agent tool invocations.
 *
 * Features:
 * - Vertical timeline of tool calls
 * - Real-time / Replay mode toggle
 * - Expandable call details
 * - Success/Failure statistics
 */
@Composable
fun ToolCallVisualizerScreen(viewModel: Gemma4ViewModel) {
    val state by viewModel.state.collectAsState()
    var expandedCallId by remember { mutableStateOf<String?>(null) }

    Column(modifier = Modifier.fillMaxSize().background(DarkBackground)) {
        // Control bar / 控制栏
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "实时模式", color = TextPrimary, fontSize = 13.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Switch(
                        checked = state.isRealTimeMode,
                        onCheckedChange = { viewModel.sendIntent(Gemma4Intent.SetRealTimeMode(it)) },
                        colors = SwitchDefaults.colors(checkedThumbColor = GoogleBlue, checkedTrackColor = GoogleBlue.copy(alpha = 0.5f))
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "${state.successCalls}", color = GoogleGreen, fontWeight = FontWeight.Bold)
                        Text(text = "成功", color = TextSecondary, fontSize = 10.sp)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "${state.failedCalls}", color = GoogleRed, fontWeight = FontWeight.Bold)
                        Text(text = "失败", color = TextSecondary, fontSize = 10.sp)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "${state.totalCalls}", color = TextPrimary, fontWeight = FontWeight.Bold)
                        Text(text = "总计", color = TextSecondary, fontSize = 10.sp)
                    }
                }
            }
        }

        // Clear button / 清空按钮
        if (state.toolCalls.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.End
            ) {
                OutlinedButton(
                    onClick = { viewModel.sendIntent(Gemma4Intent.ClearToolCalls) },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = GoogleRed)
                ) {
                    Icon(Icons.Default.Cancel, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("清空历史", fontSize = 12.sp)
                }
            }
        }

        // Tool call timeline / 调用时间线
        if (state.toolCalls.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Terminal, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(text = "暂无工具调用", color = TextSecondary)
                    Text(text = "连接 Agent 后开始追踪", color = TextSecondary.copy(alpha = 0.7f), fontSize = 12.sp)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(state.toolCalls.reversed()) { call ->
                    ToolCallTimelineItem(
                        call = call,
                        isExpanded = expandedCallId == call.id,
                        onToggle = {
                            expandedCallId = if (expandedCallId == call.id) null else call.id
                        }
                    )
                }
            }
        }
    }
}

/**
 * Single tool call timeline item with expandable details.
 */
@Composable
private fun ToolCallTimelineItem(call: ToolCallEntry, isExpanded: Boolean, onToggle: () -> Unit) {
    val statusColor = when (call.status) {
        ToolCallStatus.Success -> GoogleGreen
        ToolCallStatus.Failed -> GoogleRed
        ToolCallStatus.Running -> GoogleYellow
    }

    val toolIcon = when (call.toolType) {
        ToolType.ReadFile -> Icons.Default.Description
        ToolType.EditFile -> Icons.Default.Build
        ToolType.WriteFile -> Icons.Default.Build
        ToolType.ExecuteCommand -> Icons.Default.Terminal
        ToolType.SearchCode -> Icons.Default.Code
        ToolType.Navigate -> Icons.Default.Code
        ToolType.BuildProject -> Icons.Default.Build
        ToolType.RunTests -> Icons.Default.CheckCircle
        ToolType.Unknown -> Icons.Default.Error
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(10.dp)
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Status indicator bar / 状态指示条
                Box(
                    modifier = Modifier
                        .width(3.dp)
                        .height(40.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(statusColor)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Icon(toolIcon, contentDescription = null, tint = statusColor, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = call.toolType.displayName,
                            color = TextPrimary,
                            fontWeight = FontWeight.Medium,
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "[${call.timestamp}]", color = TextSecondary, fontSize = 11.sp)
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
                    ToolCallStatus.Success -> Icon(Icons.Default.CheckCircle, contentDescription = null, tint = GoogleGreen, modifier = Modifier.size(18.dp))
                    ToolCallStatus.Failed -> Icon(Icons.Default.Error, contentDescription = null, tint = GoogleRed, modifier = Modifier.size(18.dp))
                    ToolCallStatus.Running -> CircularProgressIndicator(modifier = Modifier.size(16.dp), color = GoogleYellow, strokeWidth = 2.dp)
                }
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = TextSecondary,
                    modifier = Modifier.size(18.dp)
                )
            }

            // Expanded details / 展开详情
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(DarkBackground.copy(alpha = 0.5f))
                        .padding(12.dp)
                ) {
                    Text(text = "参数 / Parameters:", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                    Text(text = call.params, color = TextPrimary, fontSize = 12.sp, fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace)
                    if (call.result.isNotBlank()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(text = "结果 / Result:", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                        Text(
                            text = call.result,
                            color = if (call.status == ToolCallStatus.Failed) GoogleRed else GoogleGreen,
                            fontSize = 12.sp,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "耗时 / Duration: ${call.durationMs}ms", color = TextSecondary, fontSize = 11.sp)
                }
            }
        }
    }
}

// ================================================================
// ContextInjectionScreen — 上下文注入配置
// ================================================================

/**
 * ============================================================
 * ContextInjectionScreen — 上下文注入配置
 * ================================================================
 * Configure which project context is injected into Gemma 4 agent.
 *
 * Features:
 * - Toggle switches for each context type
 * - Custom context file path management
 * - Context preview summary
 */
@Composable
fun ContextInjectionScreen(viewModel: Gemma4ViewModel) {
    val state by viewModel.state.collectAsState()
    var newPath by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(DarkBackground),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "上下文类型 / Context Types",
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary,
                fontWeight = FontWeight.SemiBold
            )
        }

        // Context type toggles / 上下文类型开关
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    ContextType.entries.forEachIndexed { index, contextType ->
                        val isEnabled = state.contextConfig.enabledContexts[contextType] == true
                        Column {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = contextType.displayName,
                                        color = TextPrimary,
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 13.sp
                                    )
                                    Text(
                                        text = contextType.description,
                                        color = TextSecondary,
                                        fontSize = 11.sp
                                    )
                                }
                                Switch(
                                    checked = isEnabled,
                                    onCheckedChange = {
                                        viewModel.sendIntent(Gemma4Intent.ToggleContextInjection(contextType, it))
                                    },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = GoogleBlue,
                                        checkedTrackColor = GoogleBlue.copy(alpha = 0.5f)
                                    )
                                )
                            }
                            if (index < ContextType.entries.size - 1) {
                                HorizontalDivider(color = DarkCard, thickness = 0.5.dp)
                            }
                        }
                    }
                }
            }
        }

        // Custom context paths / 自定义上下文路径
        item {
            Text(
                text = "自定义上下文 / Custom Context",
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary,
                fontWeight = FontWeight.SemiBold
            )
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = newPath,
                            onValueChange = { newPath = it },
                            label = { Text("文件路径", fontSize = 12.sp) },
                            placeholder = { Text("e.g., /Users/me/coding-standards.md", fontSize = 12.sp) },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = GoogleBlue,
                                unfocusedBorderColor = TextSecondary,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary,
                                focusedLabelColor = GoogleBlue,
                                unfocusedLabelColor = TextSecondary
                            )
                        )
                        IconButton(
                            onClick = {
                                if (newPath.isNotBlank()) {
                                    viewModel.sendIntent(Gemma4Intent.AddCustomContextPath(newPath))
                                    newPath = ""
                                }
                            }
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "添加", tint = GoogleBlue)
                        }
                    }
                }
            }
        }

        // Custom path list / 自定义路径列表
        if (state.contextConfig.customContextPaths.isNotEmpty()) {
            items(state.contextConfig.customContextPaths) { path ->
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
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Icon(Icons.Default.Description, contentDescription = null, tint = GoogleBlue, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = path, color = TextPrimary, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                        IconButton(onClick = { viewModel.sendIntent(Gemma4Intent.RemoveCustomContextPath(path)) }) {
                            Icon(Icons.Default.Cancel, contentDescription = "移除", tint = GoogleRed, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }

        // Context summary / 上下文摘要预览
        item {
            Text(
                text = "上下文预览 / Context Preview",
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary,
                fontWeight = FontWeight.SemiBold
            )
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    val enabledCount = state.contextConfig.enabledContexts.count { it.value }
                    val totalCount = state.contextConfig.enabledContexts.size
                    Text(
                        text = "已启用 $enabledCount / $totalCount 个上下文类型",
                        color = GoogleBlue,
                        fontWeight = FontWeight.Medium,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { enabledCount.toFloat() / totalCount },
                        modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                        color = GoogleBlue,
                        trackColor = DarkCard
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(text = "将注入给 Agent 的上下文摘要:", color = TextSecondary, fontSize = 11.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    val summaryLines = state.contextConfig.enabledContexts
                        .filter { it.value }
                        .keys
                        .take(5)
                        .map { "• ${it.displayName}" }
                    if (summaryLines.isEmpty()) {
                        Text(text = "（无启用上下文）", color = TextSecondary, fontSize = 12.sp)
                    } else {
                        summaryLines.forEach { line ->
                            Text(text = line, color = TextPrimary, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

// ================================================================
// QualityBenchmarkScreen — 质量基准测试
// ================================================================

/**
 * ============================================================
 * QualityBenchmarkScreen — 代码质量基准测试
 * ================================================================
 * Run code quality benchmark comparisons between different prompts.
 *
 * Features:
 * - Task and prompt A/B input form
 * - Radar chart visualization
 * - Benchmark history list
 */
@Composable
fun QualityBenchmarkScreen(viewModel: Gemma4ViewModel) {
    val state by viewModel.state.collectAsState()
    var taskDesc by remember { mutableStateOf("") }
    var promptA by remember { mutableStateOf("") }
    var promptB by remember { mutableStateOf("") }
    var selectedResult by remember { mutableStateOf<BenchmarkResult?>(null) }

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(DarkBackground),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Benchmark form / 基准测试表单
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "测试任务描述 / Task Description", color = TextPrimary, fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = taskDesc,
                        onValueChange = { taskDesc = it },
                        label = { Text("描述你要测试的任务", fontSize = 12.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GoogleBlue,
                            unfocusedBorderColor = TextSecondary,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedLabelColor = GoogleBlue,
                            unfocusedLabelColor = TextSecondary
                        )
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(text = "Prompt A / 配置 A", color = TextPrimary, fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = promptA,
                        onValueChange = { promptA = it },
                        label = { Text("基础 Prompt", fontSize = 12.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GoogleYellow,
                            unfocusedBorderColor = TextSecondary,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedLabelColor = GoogleYellow,
                            unfocusedLabelColor = TextSecondary
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "Prompt B / 配置 B", color = TextPrimary, fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = promptB,
                        onValueChange = { promptB = it },
                        label = { Text("增强 Prompt", fontSize = 12.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GoogleGreen,
                            unfocusedBorderColor = TextSecondary,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedLabelColor = GoogleGreen,
                            unfocusedLabelColor = TextSecondary
                        )
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = {
                            if (taskDesc.isNotBlank() && promptA.isNotBlank() && promptB.isNotBlank()) {
                                viewModel.sendIntent(Gemma4Intent.RunBenchmark(taskDesc, promptA, promptB))
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = GoogleBlue),
                        enabled = !state.isRunningBenchmark && taskDesc.isNotBlank() && promptA.isNotBlank() && promptB.isNotBlank()
                    ) {
                        if (state.isRunningBenchmark) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("测试运行中...")
                        } else {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("开始基准测试")
                        }
                    }
                }
            }
        }

        // Selected result radar chart / 选中结果的雷达图
        selectedResult?.let { result ->
            item {
                RadarChartCard(result = result)
            }
        }

        // Benchmark history / 历史记录
        if (state.benchmarkHistory.isNotEmpty()) {
            item {
                Text(
                    text = "历史记录 / History (${state.benchmarkHistory.size})",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                    fontWeight = FontWeight.SemiBold
                )
            }

            items(state.benchmarkHistory.reversed()) { result ->
                BenchmarkHistoryCard(
                    result = result,
                    isSelected = selectedResult == result,
                    onClick = { selectedResult = if (selectedResult == result) null else result }
                )
            }
        }
    }
}

/**
 * Radar chart card for benchmark visualization.
 */
@Composable
private fun RadarChartCard(result: BenchmarkResult) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "质量对比 / Quality Comparison", color = TextPrimary, fontWeight = FontWeight.SemiBold)
                Badge(containerColor = if (result.winner == "A") GoogleYellow else if (result.winner == "B") GoogleGreen else TextSecondary) {
                    Text(text = "${result.winner} 胜", color = Color.White, fontSize = 11.sp)
                }
            }
            Spacer(modifier = Modifier.height(12.dp))

            // Simple bar chart comparison / 简易柱状图对比
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom
            ) {
                result.scoresA.forEachIndexed { index, scoreA ->
                    val scoreB = result.scoresB[index]
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = scoreA.dimension.displayName, color = TextSecondary, fontSize = 9.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.Bottom) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Box(
                                    modifier = Modifier
                                        .width(20.dp)
                                        .height((scoreA.score * 0.8).dp)
                                        .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                        .background(GoogleYellow)
                                )
                                Text(text = "${scoreA.score}", color = GoogleYellow, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Box(
                                    modifier = Modifier
                                        .width(20.dp)
                                        .height((scoreB.score * 0.8).dp)
                                        .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                        .background(GoogleGreen)
                                )
                                Text(text = "${scoreB.score}", color = GoogleGreen, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(GoogleYellow))
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = "A: ${result.overallScoreA}", color = GoogleYellow, fontSize = 11.sp)
                Spacer(modifier = Modifier.width(16.dp))
                Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(GoogleGreen))
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = "B: ${result.overallScoreB}", color = GoogleGreen, fontSize = 11.sp)
            }
        }
    }
}

/**
 * Benchmark history item card.
 */
@Composable
private fun BenchmarkHistoryCard(result: BenchmarkResult, isSelected: Boolean, onClick: () -> Unit) {
    val winnerColor = when (result.winner) {
        "A" -> GoogleYellow
        "B" -> GoogleGreen
        else -> TextSecondary
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .then(if (isSelected) Modifier.border(2.dp, GoogleBlue, RoundedCornerShape(12.dp)) else Modifier),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = result.taskDescription,
                    color = TextPrimary,
                    fontWeight = FontWeight.Medium,
                    fontSize = 13.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Badge(containerColor = winnerColor) {
                    Text(text = "${result.winner} 胜", color = Color.White, fontSize = 10.sp)
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = result.timestamp, color = TextSecondary, fontSize = 11.sp)
            Spacer(modifier = Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(text = "A: ${result.overallScoreA}", color = GoogleYellow, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                Text(text = " vs ", color = TextSecondary, fontSize = 11.sp)
                Text(text = "B: ${result.overallScoreB}", color = GoogleGreen, fontSize = 12.sp, fontWeight = FontWeight.Medium)
            }
        }
    }
}

// ================================================================
// FallbackStrategyScreen — 降级策略引擎
// ================================================================

/**
 * ================================================================
 * FallbackStrategyScreen — 降级策略引擎
 * ================================================================
 * Configure Gemma 4 ↔ Gemini API fallback behavior.
 */
@Composable
fun FallbackStrategyScreen(viewModel: Gemma4ViewModel) {
    val state by viewModel.state.collectAsState()
    var tokenThreshold by remember { mutableFloatStateOf(state.fallbackTrigger.tokenThreshold.toFloat()) }
    var failureThreshold by remember { mutableIntStateOf(state.fallbackTrigger.failureThreshold) }
    var timeoutSeconds by remember { mutableIntStateOf(state.fallbackTrigger.timeoutSeconds) }

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(DarkBackground),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Current mode status / 当前模式状态
        item {
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
                    Column {
                        Text(text = "当前模式 / Current Mode", color = TextSecondary, fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = state.fallbackMode.displayName,
                            color = if (state.connectionStatus == ConnectionStatus.Connected) GoogleGreen else TextSecondary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    }
                    Badge(
                        containerColor = if (state.connectionStatus == ConnectionStatus.Connected) GoogleGreen else TextSecondary
                    ) {
                        Text(
                            text = if (state.connectionStatus == ConnectionStatus.Connected) "Gemma 4 Active" else "Disconnected",
                            color = Color.White,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }

        // Fallback mode selection / 降级模式选择
        item {
            Text(
                text = "降级模式 / Fallback Mode",
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary,
                fontWeight = FontWeight.SemiBold
            )
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    FallbackMode.entries.forEachIndexed { index, mode ->
                        val isSelected = state.fallbackMode == mode
                        val description = when (mode) {
                            FallbackMode.LocalOnly -> "仅使用本地 Gemma 4，不允许降级到 Gemini API"
                            FallbackMode.AutoSwitch -> "当条件触发时，自动切换到 Gemini API"
                            FallbackMode.ManualOnly -> "触发降级时，提示用户手动选择"
                        }
                        Column {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { viewModel.sendIntent(Gemma4Intent.ConfigureFallbackMode(mode)) }
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                androidx.compose.material3.RadioButton(
                                    selected = isSelected,
                                    onClick = { viewModel.sendIntent(Gemma4Intent.ConfigureFallbackMode(mode)) },
                                    colors = androidx.compose.material3.RadioButtonDefaults.colors(
                                        selectedColor = GoogleBlue,
                                        unselectedColor = TextSecondary
                                    )
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = mode.displayName,
                                        color = TextPrimary,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        fontSize = 13.sp
                                    )
                                    Text(
                                        text = description,
                                        color = TextSecondary,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                            if (index < FallbackMode.entries.size - 1) {
                                HorizontalDivider(color = DarkCard, thickness = 0.5.dp)
                            }
                        }
                    }
                }
            }
        }

        // Trigger thresholds / 触发条件阈值
        if (state.fallbackMode == FallbackMode.AutoSwitch) {
            item {
                Text(
                    text = "触发条件 / Trigger Conditions",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                    fontWeight = FontWeight.SemiBold
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        // Token threshold
                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Text(text = "Token 消耗阈值", color = TextPrimary, fontWeight = FontWeight.Medium)
                            Text(text = "${tokenThreshold.toInt()}%", color = GoogleBlue, fontWeight = FontWeight.Bold)
                        }
                        Slider(
                            value = tokenThreshold,
                            onValueChange = { tokenThreshold = it },
                            onValueChangeFinished = {
                                viewModel.sendIntent(Gemma4Intent.UpdateFallbackTrigger(
                                    state.fallbackTrigger.copy(tokenThreshold = tokenThreshold.toInt())
                                ))
                            },
                            valueRange = 50f..100f,
                            colors = SliderDefaults.colors(thumbColor = GoogleBlue, activeTrackColor = GoogleBlue)
                        )
                        Text(text = "超过此阈值时触发降级", color = TextSecondary, fontSize = 11.sp)

                        Spacer(modifier = Modifier.height(16.dp))

                        // Failure threshold
                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Text(text = "连续失败次数", color = TextPrimary, fontWeight = FontWeight.Medium)
                            Text(text = "$failureThreshold 次", color = GoogleRed, fontWeight = FontWeight.Bold)
                        }
                        Slider(
                            value = failureThreshold.toFloat(),
                            onValueChange = { failureThreshold = it.toInt() },
                            onValueChangeFinished = {
                                viewModel.sendIntent(Gemma4Intent.UpdateFallbackTrigger(
                                    state.fallbackTrigger.copy(failureThreshold = failureThreshold)
                                ))
                            },
                            valueRange = 1f..10f,
                            steps = 8,
                            colors = SliderDefaults.colors(thumbColor = GoogleRed, activeTrackColor = GoogleRed)
                        )
                        Text(text = "连续失败超过此次数时触发降级", color = TextSecondary, fontSize = 11.sp)

                        Spacer(modifier = Modifier.height(16.dp))

                        // Timeout threshold
                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Text(text = "推理超时阈值", color = TextPrimary, fontWeight = FontWeight.Medium)
                            Text(text = "${timeoutSeconds}s", color = GoogleYellow, fontWeight = FontWeight.Bold)
                        }
                        Slider(
                            value = timeoutSeconds.toFloat(),
                            onValueChange = { timeoutSeconds = it.toInt() },
                            onValueChangeFinished = {
                                viewModel.sendIntent(Gemma4Intent.UpdateFallbackTrigger(
                                    state.fallbackTrigger.copy(timeoutSeconds = timeoutSeconds)
                                ))
                            },
                            valueRange = 10f..180f,
                            steps = 16,
                            colors = SliderDefaults.colors(thumbColor = GoogleYellow, activeTrackColor = GoogleYellow)
                        )
                        Text(text = "推理时间超过此值时触发降级", color = TextSecondary, fontSize = 11.sp)
                    }
                }
            }
        }

        // Gemini API config hint
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Security, contentDescription = null, tint = GoogleBlue, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "Gemini API 配置 / Gemini API Config", color = TextPrimary, fontWeight = FontWeight.Medium)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "API Key:", color = TextSecondary, fontSize = 12.sp)
                    Text(text = "•••••••••••••••", color = TextPrimary, fontSize = 12.sp, fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "模型 / Model:", color = TextSecondary, fontSize = 12.sp)
                    Text(text = "Gemini 2.5 Pro", color = GoogleBlue, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}
