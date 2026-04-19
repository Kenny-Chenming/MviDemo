package com.mvi.kenny.feature.memorylimit

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Badge
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.Card as MaterialCard
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mvi.kenny.base.TopBarAction
import com.mvi.kenny.base.TopBarConfig
import kotlinx.coroutines.flow.collect
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * ============================================================
 * MemoryLimitScreen — 内存限制检测主界面
 * ============================================================
 * PRD-123 | Android 17 App 内存限制检测与 LeakCanary Profiler 集成工具包
 *
 * 5-Tab BottomNav 结构：
 * - Overview: 内存仪表盘 + 趋势图 + 设备选择
 * - LeakCanary: 泄漏列表 + 触限风险评分
 * - Anomaly: 异常触发记录 + 堆转储
 * - Audio: 后台音频硬化检测
 * - CI: CI/CD 配置生成
 *
 * @param onUpdateTopBar 向 MainScreen 上报 TopBar 配置
 * @see MemoryLimitViewModel
 */

// ================================================================
// Colors / 颜色规范（深色主题）
// ================================================================
private object MemoryColors {
    val Primary = Color(0xFF6A1B9A)          // 深紫
    val HighRisk = Color(0xFFD32F2F)          // 红
    val MediumRisk = Color(0xFFF57C00)         // 橙
    val LowRisk = Color(0xFF388E3C)            // 绿
    val GaugeLow = Color(0xFF4CAF50)           // 仪表盘绿
    val GaugeMedium = Color(0xFFFF9800)         // 仪表盘橙
    val GaugeHigh = Color(0xFFF44336)           // 仪表盘红
    val Surface = Color(0xFF1E1E1E)            // 深色背景
    val Background = Color(0xFF121212)          // 背景
    val OnSurface = Color(0xFFE0E0E0)          // 浅色文字
    val CodeBackground = Color(0xFF2D2D2D)     // 代码背景
    val CardBackground = Color(0xFF262626)      // 卡片背景
}

// ================================================================
// Tab titles / Tab 标题
// ================================================================
private val tabTitles = listOf(
    "Overview" to "概览",
    "LeakCanary" to "泄漏",
    "Anomaly" to "异常",
    "Audio" to "音频",
    "CI" to "CI配置"
)

// ================================================================
// Main Screen / 主界面
// ================================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemoryLimitScreen(
    onUpdateTopBar: (TopBarConfig) -> Unit = {}
) {
    val viewModel: MemoryLimitViewModel = viewModel()
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    // Update TopBar when tab changes / Tab 切换时更新 TopBar
    LaunchedEffect(state.currentTab) {
        onUpdateTopBar(
            TopBarConfig(
                title = "${tabTitles[state.currentTab].second}",
                actions = listOf(
                    TopBarAction(
                        icon = Icons.Default.Refresh,
                        contentDescription = "刷新",
                        onClick = { viewModel.sendIntent(MemoryLimitIntent.RefreshMemoryUsage) }
                    )
                )
            )
        )
    }

    // Collect effects / 收集副作用
    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is MemoryLimitEffect.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(effect.message)
                }
                is MemoryLimitEffect.CopyToClipboard -> {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    clipboard.setPrimaryClip(ClipData.newPlainText("code", effect.text))
                }
                is MemoryLimitEffect.NavigateToSource -> {
                    // 由父组件处理
                }
                is MemoryLimitEffect.ShareReport -> {
                    // 由父组件处理
                }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MemoryColors.Background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Tab Row / Tab 切换
            TabRow(
                selectedTabIndex = state.currentTab,
                containerColor = MemoryColors.Surface
            ) {
                tabTitles.forEachIndexed { index, (en, zh) ->
                    Tab(
                        selected = state.currentTab == index,
                        onClick = { viewModel.sendIntent(MemoryLimitIntent.SelectTab(index)) },
                        text = { Text(zh, style = MaterialTheme.typography.labelMedium) },
                        selectedContentColor = MemoryColors.Primary,
                        unselectedContentColor = MemoryColors.OnSurface.copy(alpha = 0.6f)
                    )
                }
            }

            // Tab Content / Tab 内容区
            AnimatedContent(
                targetState = state.currentTab,
                transitionSpec = {
                    fadeIn(tween(200)) togetherWith fadeOut(tween(200))
                },
                modifier = Modifier.fillMaxSize()
            ) { tab ->
                when (tab) {
                    MemoryLimitTab.OVERVIEW -> OverviewTab(state = state, viewModel = viewModel)
                    MemoryLimitTab.LEAK_CANARY -> LeakCanaryTab(state = state, viewModel = viewModel)
                    MemoryLimitTab.ANOMALY -> AnomalyTab(state = state, viewModel = viewModel)
                    MemoryLimitTab.AUDIO -> AudioTab(state = state, viewModel = viewModel)
                    MemoryLimitTab.CI -> CITab(state = state, viewModel = viewModel)
                }
            }
        }
    }
}

// ================================================================
// Overview Tab / 概览 Tab
// ================================================================
@Composable
private fun OverviewTab(
    state: MemoryLimitState,
    viewModel: MemoryLimitViewModel
) {
    val percent = state.memoryUsagePercent
    val gaugeColor = when {
        percent > 80 -> MemoryColors.GaugeHigh
        percent > 60 -> MemoryColors.GaugeMedium
        else -> MemoryColors.GaugeLow
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Alert Banner / 告警横幅
        if (state.isNearLimit) {
            item {
                MaterialCard(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MemoryColors.HighRisk.copy(alpha = 0.2f))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Warning,
                            contentDescription = null,
                            tint = MemoryColors.HighRisk
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                "⚠️ 内存使用接近限制",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MemoryColors.HighRisk
                            )
                            Text(
                                "当前使用 ${percent.toInt()}%，超过 80% 告警阈值",
                                style = MaterialTheme.typography.bodySmall,
                                color = MemoryColors.HighRisk.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            }
        }

        // Memory Gauge / 内存仪表盘
        item {
            MaterialCard(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MemoryColors.CardBackground)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "内存使用量" /* Memory Usage */,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MemoryColors.OnSurface
                    )
                    Spacer(modifier = Modifier.height(20.dp))

                    Box(
                        modifier = Modifier.size(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        MemoryGauge(
                            percent = percent / 100f,
                            color = gaugeColor,
                            modifier = Modifier.size(200.dp)
                        )
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                "${percent.toInt()}%",
                                style = MaterialTheme.typography.displayMedium,
                                fontWeight = FontWeight.Bold,
                                color = gaugeColor
                            )
                            Text(
                                "${state.currentMemoryUsageMb.toInt()} MB",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MemoryColors.OnSurface.copy(alpha = 0.7f)
                            )
                            Text(
                                "/ ${state.memoryLimitMb.toInt()} MB 限制",
                                style = MaterialTheme.typography.labelSmall,
                                color = MemoryColors.OnSurface.copy(alpha = 0.5f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // 刷新按钮 / Refresh button
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = { viewModel.sendIntent(MemoryLimitIntent.RefreshMemoryUsage) },
                            colors = ButtonDefaults.buttonColors(containerColor = MemoryColors.Primary)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("刷新" /* Refresh */)
                        }

                        // Device Selector / 设备选择
                        DeviceSelector(
                            selectedDevice = state.selectedDevice,
                            onDeviceSelected = { viewModel.sendIntent(MemoryLimitIntent.SelectDevice(it)) }
                        )
                    }
                }
            }
        }

        // Risk Level Card / 风险级别卡片
        item {
            RiskLevelCard(state = state)
        }

        // Memory Trend Chart / 内存趋势图
        item {
            MaterialCard(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MemoryColors.CardBackground)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "最近 60 秒内存趋势" /* Memory Trend (Last 60s) */,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MemoryColors.OnSurface
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    MemoryTrendChart(
                        data = state.memoryTrendData,
                        limit = state.memoryLimitMb,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                    )
                }
            }
        }
    }
}

// ================================================================
// Memory Gauge / 内存仪表盘（Canvas 自绘）
// ================================================================
@Composable
private fun MemoryGauge(
    percent: Float,
    color: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val strokeWidth = 20.dp.toPx()
        val radius = (size.minDimension - strokeWidth) / 2
        val center = Offset(size.width / 2, size.height / 2)

        // Background arc / 背景弧
        drawArc(
            color = Color.Gray.copy(alpha = 0.2f),
            startAngle = 135f,
            sweepAngle = 270f,
            useCenter = false,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
            topLeft = Offset(center.x - radius, center.y - radius),
            size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2)
        )

        // Progress arc / 进度弧
        drawArc(
            color = color,
            startAngle = 135f,
            sweepAngle = 270f * percent.coerceIn(0f, 1f),
            useCenter = false,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
            topLeft = Offset(center.x - radius, center.y - radius),
            size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2)
        )
    }
}

// ================================================================
// Device Selector / 设备选择器
// ================================================================
@Composable
private fun DeviceSelector(
    selectedDevice: DeviceInfo?,
    onDeviceSelected: (DeviceInfo) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val devices = listOf(
        DeviceInfo("Pixel 8 Pro", 12288, 37, 3072),
        DeviceInfo("Samsung Galaxy S26", 16384, 37, 4096),
        DeviceInfo("Pixel 7a", 8192, 36, 2048),
        DeviceInfo("Xiaomi 15 Ultra", 16384, 37, 4096),
        DeviceInfo("OnePlus 13", 12288, 37, 3072)
    )

    Box {
        AssistChip(
            onClick = { expanded = true },
            label = {
                Text(selectedDevice?.model ?: "选择设备" /* Select Device */)
            },
            leadingIcon = {
                Icon(Icons.Default.Storage, contentDescription = null, modifier = Modifier.size(16.dp))
            },
            trailingIcon = {
                Icon(Icons.Default.ArrowDropDown, contentDescription = null)
            }
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            devices.forEach { device ->
                DropdownMenuItem(
                    text = {
                        Column {
                            Text(device.model, fontWeight = FontWeight.Bold)
                            Text(
                                "${device.totalRamMb / 1024}GB RAM / API ${device.androidVersion} / Limit ${device.memoryLimitMb}MB",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    onClick = {
                        onDeviceSelected(device)
                        expanded = false
                    }
                )
            }
        }
    }
}

// ================================================================
// Risk Level Card / 风险级别卡片
// ================================================================
@Composable
private fun RiskLevelCard(state: MemoryLimitState) {
    val riskColor = when (state.riskLevel) {
        RiskLevel.HIGH -> MemoryColors.HighRisk
        RiskLevel.MEDIUM -> MemoryColors.MediumRisk
        RiskLevel.LOW -> MemoryColors.LowRisk
    }

    val riskLabel = when (state.riskLevel) {
        RiskLevel.HIGH -> "高风险" /* High Risk */
        RiskLevel.MEDIUM -> "中风险" /* Medium Risk */
        RiskLevel.LOW -> "低风险" /* Low Risk */
    }

    val triggerProbability = when (state.riskLevel) {
        RiskLevel.HIGH -> "85-100%"
        RiskLevel.MEDIUM -> "40-84%"
        RiskLevel.LOW -> "0-39%"
    }

    MaterialCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = riskColor.copy(alpha = 0.1f))
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
                    "风险级别" /* Risk Level */,
                    style = MaterialTheme.typography.labelMedium,
                    color = MemoryColors.OnSurface.copy(alpha = 0.7f)
                )
                Text(
                    riskLabel,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = riskColor
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    "触限概率" /* Trigger Probability */,
                    style = MaterialTheme.typography.labelMedium,
                    color = MemoryColors.OnSurface.copy(alpha = 0.7f)
                )
                Text(
                    triggerProbability,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = riskColor
                )
            }
        }
    }
}

// ================================================================
// Memory Trend Chart / 内存趋势图（Canvas 自绘折线图）
// ================================================================
@Composable
private fun MemoryTrendChart(
    data: List<Float>,
    limit: Float,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        if (data.isEmpty()) return@Canvas

        val maxValue = limit
        val minValue = 0f
        val range = maxValue - minValue

        val stepX = size.width / (data.size - 1).coerceAtLeast(1)
        val paddingY = 4.dp.toPx()

        // Draw limit line / 绘制限制线
        val limitY = size.height - paddingY - ((limit - minValue) / range) * (size.height - 2 * paddingY)
        drawLine(
            color = Color.Red.copy(alpha = 0.5f),
            start = Offset(0f, limitY),
            end = Offset(size.width, limitY),
            strokeWidth = 1.dp.toPx(),
            pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(8f, 4f))
        )

        // Draw data line / 绘制折线
        if (data.size >= 2) {
            val path = Path()
            data.forEachIndexed { i, value ->
                val x = i * stepX
                val y = size.height - paddingY - ((value - minValue) / range) * (size.height - 2 * paddingY)
                if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }
            drawPath(
                path = path,
                color = MemoryColors.Primary,
                style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
            )
        }
    }
}

// ================================================================
// LeakCanary Tab / 泄漏 Tab
// ================================================================
@Composable
private fun LeakCanaryTab(
    state: MemoryLimitState,
    viewModel: MemoryLimitViewModel
) {
    val sortedLeaks = when (state.sortOption) {
        LeakSortOption.BY_RISK -> state.leakCanaryResults.sortedByDescending { it.riskScore }
        LeakSortOption.BY_SIZE -> state.leakCanaryResults.sortedByDescending { it.leakSizeKb }
        LeakSortOption.BY_TIME -> state.leakCanaryResults.sortedByDescending { it.detectedAt }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Sort options / 排序选项
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                LeakSortOption.entries.forEach { option ->
                    FilterChip(
                        selected = state.sortOption == option,
                        onClick = { viewModel.sendIntent(MemoryLimitIntent.SetLeakSortOption(option)) },
                        label = {
                            Text(
                                when (option) {
                                    LeakSortOption.BY_RISK -> "按风险"
                                    LeakSortOption.BY_SIZE -> "按大小"
                                    LeakSortOption.BY_TIME -> "按时间"
                                }
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MemoryColors.Primary.copy(alpha = 0.2f)
                        )
                    )
                }
            }
        }

        // Top fix recommendation / 最优先修复推荐
        val topLeak = sortedLeaks.firstOrNull()
        if (topLeak != null) {
            item {
                MaterialCard(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MemoryColors.HighRisk.copy(alpha = 0.1f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.BugReport, contentDescription = null, tint = MemoryColors.HighRisk)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "最优先修复" /* Top Priority Fix */,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MemoryColors.HighRisk
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            topLeak.leakSignature,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MemoryColors.OnSurface
                        )
                        Text(
                            "风险评分: ${topLeak.riskScore}/100 | 大小: ${topLeak.leakSizeKb}KB",
                            style = MaterialTheme.typography.bodySmall,
                            color = MemoryColors.OnSurface.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }

        // Leak list / 泄漏列表
        itemsIndexed(sortedLeaks) { _, leak ->
            LeakCard(
                leak = leak,
                isExpanded = state.expandedLeakSignature == leak.leakSignature,
                onToggle = { viewModel.sendIntent(MemoryLimitIntent.ToggleLeakDetail(leak.leakSignature)) },
                onCopy = { viewModel.sendIntent(MemoryLimitIntent.CopyScript(leak.leakPath)) }
            )
        }
    }
}

// ================================================================
// Leak Card / 泄漏卡片
// ================================================================
@Composable
private fun LeakCard(
    leak: LeakResult,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    onCopy: () -> Unit
) {
    val riskColor = when {
        leak.riskScore >= 80 -> MemoryColors.HighRisk
        leak.riskScore >= 50 -> MemoryColors.MediumRisk
        else -> MemoryColors.LowRisk
    }

    val dateFormat = remember { SimpleDateFormat("HH:mm:ss", Locale.getDefault()) }

    MaterialCard(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(animationSpec = tween(250)),
        colors = CardDefaults.cardColors(containerColor = MemoryColors.CardBackground)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Risk badge / 风险徽章
                Box(
                    modifier = Modifier
                        .background(riskColor.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        "风险 ${leak.riskScore}",
                        style = MaterialTheme.typography.labelSmall,
                        color = riskColor,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        leak.leakSignature,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MemoryColors.OnSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        "${leak.leakSizeKb} KB | ${dateFormat.format(Date(leak.detectedAt))}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MemoryColors.OnSurface.copy(alpha = 0.6f)
                    )
                }

                IconButton(onClick = onToggle) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = if (isExpanded) "折叠" else "展开"
                    )
                }
            }

            if (isExpanded) {
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = MemoryColors.OnSurface.copy(alpha = 0.1f))
                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    "引用链" /* Reference Chain */,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MemoryColors.OnSurface
                )
                Spacer(modifier = Modifier.height(6.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MemoryColors.CodeBackground, RoundedCornerShape(6.dp))
                        .padding(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            leak.leakPath,
                            style = MaterialTheme.typography.bodySmall,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.weight(1f),
                            color = MemoryColors.OnSurface.copy(alpha = 0.8f)
                        )
                        IconButton(onClick = onCopy, modifier = Modifier.size(20.dp)) {
                            Icon(
                                Icons.Default.ContentCopy,
                                contentDescription = "复制",
                                modifier = Modifier.size(14.dp),
                                tint = MemoryColors.OnSurface.copy(alpha = 0.5f)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    "修复建议" /* Suggested Fix */,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MemoryColors.OnSurface
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    leak.suggestedFix,
                    style = MaterialTheme.typography.bodySmall,
                    color = MemoryColors.OnSurface.copy(alpha = 0.7f)
                )
            }
        }
    }
}

// ================================================================
// Anomaly Tab / 异常 Tab
// ================================================================
@Composable
private fun AnomalyTab(
    state: MemoryLimitState,
    viewModel: MemoryLimitViewModel
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Anomaly trigger switch / 异常触发开关
        item {
            MaterialCard(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MemoryColors.CardBackground)
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
                            "启用 TRIGGER_TYPE_ANOMALY" /* Enable TRIGGER_TYPE_ANOMALY */,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MemoryColors.OnSurface
                        )
                        Text(
                            "当内存使用接近限制时自动触发堆转储收集",
                            style = MaterialTheme.typography.bodySmall,
                            color = MemoryColors.OnSurface.copy(alpha = 0.6f)
                        )
                    }
                    Switch(
                        checked = state.isAnomalyEnabled,
                        onCheckedChange = {
                            viewModel.sendIntent(MemoryLimitIntent.SetAnomalyEnabled(it))
                        },
                        colors = androidx.compose.material3.SwitchDefaults.colors(
                            checkedThumbColor = MemoryColors.Primary,
                            checkedTrackColor = MemoryColors.Primary.copy(alpha = 0.5f)
                        )
                    )
                }
            }
        }

        // Heap dump list / 堆转储列表
        item {
            Text(
                "堆转储记录 (${state.anomalyTriggers.size})" /* Heap Dump Records */,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MemoryColors.OnSurface
            )
        }

        if (state.anomalyTriggers.isEmpty()) {
            item {
                MaterialCard(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MemoryColors.CardBackground)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "暂无堆转储记录\n开启异常触发后将在检测到超限时自动记录",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MemoryColors.OnSurface.copy(alpha = 0.5f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        items(state.anomalyTriggers) { trigger ->
            val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()) }

            MaterialCard(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MemoryColors.CardBackground)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "#${trigger.id}",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MemoryColors.OnSurface
                        )
                        Text(
                            dateFormat.format(Date(trigger.triggerTime)),
                            style = MaterialTheme.typography.labelSmall,
                            color = MemoryColors.OnSurface.copy(alpha = 0.6f)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        trigger.triggerReason,
                        style = MaterialTheme.typography.bodySmall,
                        color = MemoryColors.HighRisk.copy(alpha = 0.9f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "堆转储大小: ${trigger.heapDumpSizeKb / 1024} MB",
                            style = MaterialTheme.typography.labelSmall,
                            color = MemoryColors.OnSurface.copy(alpha = 0.6f)
                        )
                        TextButton(
                            onClick = { viewModel.sendIntent(MemoryLimitIntent.ViewHeapDump(trigger.id)) }
                        ) {
                            Text("查看详情" /* View Detail */)
                        }
                    }
                }
            }
        }
    }
}

// ================================================================
// Audio Tab / 音频 Tab
// ================================================================
@Composable
private fun AudioTab(
    state: MemoryLimitState,
    viewModel: MemoryLimitViewModel
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Audio hardening detector / 音频硬化检测
        item {
            MaterialCard(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MemoryColors.CardBackground)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Memory,
                            contentDescription = null,
                            tint = MemoryColors.Primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "后台音频硬化检测" /* Background Audio Hardening Detection */,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MemoryColors.OnSurface
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Android 17 Beta 4 引入了音频框架后台硬化，部分音频 API 在后台交互时被限制",
                        style = MaterialTheme.typography.bodySmall,
                        color = MemoryColors.OnSurface.copy(alpha = 0.6f)
                    )
                }
            }
        }

        item {
            Text(
                "受影响代码路径 (${state.audioAffectedPaths.count { it.isAffected }})" /* Affected Code Paths */,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MemoryColors.OnSurface
            )
        }

        items(state.audioAffectedPaths) { audioPath ->
            AudioPathCard(
                audioPath = audioPath,
                onCopy = { viewModel.sendIntent(MemoryLimitIntent.CopyScript(audioPath.description)) }
            )
        }

        // WhileInUse FGS Template / FGS 模板
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "while-in-use FGS 正确用法模板" /* While-In-Use FGS Template */,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MemoryColors.OnSurface
            )
        }

        item {
            CodeCard(
                title = "While-In-Use Foreground Service",
                code = """
                    |// 1. 声明 FOREGROUND_SERVICE_MEDIA_PLAYBACK 权限
                    |<uses-permission android:name="android.permission.FOREGROUND_SERVICE_MEDIA_PLAYBACK" />
                    |
                    |// 2. 创建 Foreground Service notification
                    |val notification = NotificationCompat.Builder(context, channelId)
                    |    .setContentTitle("Playing music")
                    |    .setSmallIcon(R.drawable.ic_play)
                    |    .setPriority(PRIORITY_LOW)
                    |    .build()
                    |
                    |// 3. 使用 while-in-use 类型音频属性
                    |val audioAttributes = AudioAttributes.Builder()
                    |    .setUsage(AudioAttributes.USAGE_MEDIA)  // ✅ Use MEDIA not GAME
                    |    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    |    .build()
                    |
                    |// 4. 请求音频焦点（while-in-use）
                    |val focusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                    |    .setAudioAttributes(audioAttributes)
                    |    .setAcceptsDelayedFocusGain(true)
                    |    .setWillPauseWhenDucked(false)  // Don't pause for other audio
                    |    .build()
                    |
                    |// 5. Start foreground service
                    |val intent = Intent(context, MusicPlaybackService::class.java)
                    |context.startForegroundService(intent)
                    |""".trimMargin()
                )
            }
        }
    }

// ================================================================
// AudioPathCard — 受影响音频路径卡片
// ================================================================
@Composable
private fun AudioPathCard(
    audioPath: AudioPath,
    onCopy: () -> Unit
) {
    MaterialCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (audioPath.isAffected)
                MemoryColors.HighRisk.copy(alpha = 0.1f)
            else
                MemoryColors.CardBackground
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = audioPath.apiName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (audioPath.isAffected) MemoryColors.HighRisk else MemoryColors.OnSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${audioPath.filePath}:${audioPath.lineNumber}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MemoryColors.OnSurface.copy(alpha = 0.6f)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = audioPath.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MemoryColors.OnSurface.copy(alpha = 0.8f)
                )
            }
            IconButton(onClick = onCopy) {
                Icon(
                    Icons.Default.ContentCopy,
                    contentDescription = "复制",
                    tint = MemoryColors.OnSurface.copy(alpha = 0.6f)
                )
            }
        }
    }
}

// ================================================================
// CodeCard — 代码模板卡片
// ================================================================
@Composable
private fun CodeCard(
    title: String,
    code: String
) {
    MaterialCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MemoryColors.OnSurface
                )
                Icon(
                    Icons.Default.ContentCopy,
                    contentDescription = "复制代码",
                    tint = MemoryColors.OnSurface.copy(alpha = 0.6f),
                    modifier = Modifier
                        .size(20.dp)
                        .clickable { }
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = code,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontFamily = FontFamily.Monospace
                ),
                color = Color(0xFFE0E0E0)
            )
        }
    }
}

// ================================================================
// CI Tab / CI Tab
// ================================================================
@Composable
private fun CITab(
    state: MemoryLimitState,
    viewModel: MemoryLimitViewModel
) {
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is MemoryLimitEffect.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(effect.message)
                }
                else -> {}
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // CI/CD Memory Behavior Validation / CI/CD 内存行为验证
            item {
                MaterialCard(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MemoryColors.CardBackground)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.BugReport,
                                contentDescription = null,
                                tint = MemoryColors.Primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "CI/CD 内存行为验证" /* CI/CD Memory Behavior Validation */,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MemoryColors.OnSurface
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "生成 GitHub Actions / GitLab CI 配置文件，在 CI 中验证 App 的内存限制行为",
                            style = MaterialTheme.typography.bodySmall,
                            color = MemoryColors.OnSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            }

            // Generate button / 生成按钮
            item {
                Button(
                    onClick = { viewModel.sendIntent(MemoryLimitIntent.GenerateCIConfig) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MemoryColors.Primary)
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("生成 CI 配置" /* Generate CI Config */)
                }
            }

            // CI Config YAML / CI 配置 YAML
            if (state.ciConfigYaml.isNotEmpty()) {
                item {
                    MaterialCard(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "CI Configuration YAML",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MemoryColors.OnSurface
                                )
                                Row {
                                    IconButton(
                                        onClick = {
                                            viewModel.sendIntent(
                                                MemoryLimitIntent.CopyScript(state.ciConfigYaml)
                                            )
                                        }
                                    ) {
                                        Icon(
                                            Icons.Default.ContentCopy,
                                            contentDescription = "复制",
                                            tint = MemoryColors.OnSurface.copy(alpha = 0.6f)
                                        )
                                    }
                                    IconButton(
                                        onClick = {
                                            viewModel.sendIntent(
                                                MemoryLimitIntent.ShareReport(state.ciConfigYaml)
                                            )
                                        }
                                    ) {
                                        Icon(
                                            Icons.Default.Share,
                                            contentDescription = "分享",
                                            tint = MemoryColors.OnSurface.copy(alpha = 0.6f)
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = state.ciConfigYaml,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontFamily = FontFamily.Monospace
                                ),
                                color = Color(0xFFE0E0E0)
                            )
                        }
                    }
                }
            }

            // Memory limit parameters info / 内存限制参数信息
            item {
                MaterialCard(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MemoryColors.CardBackground)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "内存限制参数" /* Memory Limit Parameters */,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MemoryColors.OnSurface
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    "当前内存限制",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MemoryColors.OnSurface.copy(alpha = 0.6f)
                                )
                                Text(
                                    "${state.memoryLimitMb.toInt()} MB",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MemoryColors.Primary
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    "风险级别",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MemoryColors.OnSurface.copy(alpha = 0.6f)
                                )
                                val riskColor = when (state.riskLevel) {
                                    RiskLevel.HIGH -> MemoryColors.HighRisk
                                    RiskLevel.MEDIUM -> MemoryColors.MediumRisk
                                    RiskLevel.LOW -> MemoryColors.LowRisk
                                }
                                Text(
                                    state.riskLevel.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = riskColor
                                )
                            }
                        }
                    }
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}