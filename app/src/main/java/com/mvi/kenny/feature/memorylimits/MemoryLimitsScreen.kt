package com.mvi.kenny.feature.memorylimits

// ================================================================
// MemoryLimitsScreen — Android 17 App Memory Limits 主界面
// ================================================================
// Main screen for Android 17 Memory Limits detection & optimization toolkit.
//
// PRD-151: Android 17 App Memory Limits 内存限制检测与调优开发工具包
// Design Reference: memory/agency/designs/PRD-151-Android-17-App-Memory-Limits-内存限制检测与调优开发工具包.md
//
// Features:
//   1. Dashboard Tab — Overview with memory curve, risk gauge, event summary
//   2. Profiler Tab — Memory breakdown by module (pie chart / bar chart)
//   3. Events Tab — MemoryLimiter kill event list
//   4. Heap Dump Tab — Heap dump analysis results
//   5. CI Report Tab — Compliance report with pass/fail/warning status
//   6. Best Practices Tab — Android 17 memory best practices guide
//   7. Simulator Tab — Memory limit simulator for different device RAM
// —————————————————————————————————————————————————————

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Dangerous
import androidx.compose.material.icons.filled.DataUsage
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.FactCheck
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.MonitorHeart
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
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
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// ================================================================
// Developer Tools Dark Theme Colors
// ================================================================

private val DarkBackground = Color(0xFF121212)
private val DarkSurface = Color(0xFF1E1E1E)
private val MemoryBlue = Color(0xFF4FC3F7)
private val SafeGreen = Color(0xFF4CAF50)
private val WarningOrange = Color(0xFFFF9800)
private val DangerRed = Color(0xFFF44336)
private val CriticalRed = Color(0xFFB71C1C)
private val MonoWhite = Color(0xFFE0E0E0)
private val SecondaryText = Color(0xFFB0B0B0)

// ================================================================
// Sub-screen enum for internal navigation
// ================================================================

private enum class MemorySubScreen(val title: String, val icon: ImageVector) {
    Dashboard("总览", Icons.Default.MonitorHeart),
    Profiler("分析器", Icons.Default.Analytics),
    Events("事件", Icons.Default.BugReport),
    HeapDump("堆 Dump", Icons.Default.AccountTree),
    CIReport("合规报告", Icons.Default.FactCheck),
    BestPractices("最佳实践", Icons.Default.Security),
    Simulator("模拟器", Icons.Default.Devices)
}

// ================================================================
// MemoryLimitsScreen — 主入口
// ================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemoryLimitsScreen(
    state: MemoryLimitsState,
    onIntent: (MemoryLimitsIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    val snackbarHostState = remember { SnackbarHostState() }
    var selectedSubScreen by remember { mutableIntStateOf(0) }
    val subScreens = MemorySubScreen.entries.toTypedArray()

    // Collect effects
    LaunchedEffect(Unit) {
        // Effect collection would be handled here in production
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = DarkBackground,
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Sub-screen Tab Row
            TabRow(
                selectedTabIndex = selectedSubScreen,
                containerColor = DarkSurface,
                contentColor = MemoryBlue,
                modifier = Modifier.height(48.dp)
            ) {
                subScreens.forEachIndexed { index, screen ->
                    Tab(
                        selected = selectedSubScreen == index,
                        onClick = { selectedSubScreen = index },
                        text = {
                            Text(
                                text = screen.title,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                maxLines = 1
                            )
                        },
                        icon = {
                            Icon(
                                imageVector = screen.icon,
                                contentDescription = screen.title,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    )
                }
            }

            // Sub-screen content
            when (subScreens[selectedSubScreen]) {
                MemorySubScreen.Dashboard -> DashboardTab(
                    state = state,
                    onIntent = onIntent
                )
                MemorySubScreen.Profiler -> ProfilerTab(state = state)
                MemorySubScreen.Events -> EventsTab(state = state)
                MemorySubScreen.HeapDump -> HeapDumpTab(state = state, onIntent = onIntent)
                MemorySubScreen.CIReport -> CIReportTab(state = state, onIntent = onIntent)
                MemorySubScreen.BestPractices -> BestPracticesTab(state = state)
                MemorySubScreen.Simulator -> SimulatorTab(state = state, onIntent = onIntent)
            }
        }
    }
}

// ================================================================
// Dashboard Tab
// ================================================================

@Composable
private fun DashboardTab(
    state: MemoryLimitsState,
    onIntent: (MemoryLimitsIntent) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Monitoring Control Card
        item {
            MonitoringControlCard(state = state, onIntent = onIntent)
        }

        // Memory Overview Cards Row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MemoryUsageCard(
                    state = state,
                    modifier = Modifier.weight(1f)
                )
                RiskGaugeCard(
                    state = state,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Memory Curve Chart
        item {
            MemoryCurveCard(state = state)
        }

        // Limiter Events Summary
        item {
            LimiterEventsSummaryCard(state = state)
        }

        // Quick Actions
        item {
            QuickActionsCard(onIntent = onIntent, state = state)
        }
    }
}

@Composable
private fun MonitoringControlCard(
    state: MemoryLimitsState,
    onIntent: (MemoryLimitsIntent) -> Unit
) {
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
                Text(
                    text = "内存监控",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = when (state.monitoringState) {
                        MonitoringState.Idle -> "空闲"
                        MonitoringState.Monitoring -> if (state.isMonitoringPaused) "已暂停" else "监控中 (每秒刷新)"
                        MonitoringState.Error -> "错误"
                        else -> ""
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = SecondaryText
                )
            }
            Row {
                // Play/Pause button
                IconButton(
                    onClick = {
                        if (state.monitoringState == MonitoringState.Monitoring) {
                            onIntent(MemoryLimitsIntent.ToggleMonitoring)
                        } else {
                            onIntent(MemoryLimitsIntent.StartMonitoring)
                        }
                    }
                ) {
                    Icon(
                        imageVector = if (state.monitoringState == MonitoringState.Monitoring && !state.isMonitoringPaused)
                            Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = "Toggle monitoring",
                        tint = MemoryBlue
                    )
                }
                // Stop button
                if (state.monitoringState == MonitoringState.Monitoring) {
                    IconButton(onClick = { onIntent(MemoryLimitsIntent.StopMonitoring) }) {
                        Icon(
                            imageVector = Icons.Default.Stop,
                            contentDescription = "Stop monitoring",
                            tint = DangerRed
                        )
                    }
                }
                // Refresh button
                IconButton(onClick = { onIntent(MemoryLimitsIntent.RefreshMemoryData) }) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh",
                        tint = Color.White
                    )
                }
            }
        }
    }
}

@Composable
private fun MemoryUsageCard(
    state: MemoryLimitsState,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Memory,
                    contentDescription = null,
                    tint = MemoryBlue,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "内存使用",
                    style = MaterialTheme.typography.bodyMedium,
                    color = SecondaryText,
                    fontFamily = FontFamily.Monospace
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = formatBytes(state.memoryUsageBytes),
                style = MaterialTheme.typography.headlineSmall,
                color = Color.White,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "/ ${formatBytes(state.memoryLimitBytes)}",
                style = MaterialTheme.typography.bodySmall,
                color = SecondaryText,
                fontFamily = FontFamily.Monospace
            )
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { state.memoryUsagePercent / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = state.riskLevel.color,
                trackColor = Color(0xFF2A2A2A),
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${"%.1f".format(state.memoryUsagePercent)}%",
                style = MaterialTheme.typography.bodySmall,
                color = state.riskLevel.color,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

@Composable
private fun RiskGaugeCard(
    state: MemoryLimitsState,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Speed,
                    contentDescription = null,
                    tint = state.riskLevel.color,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "风险等级",
                    style = MaterialTheme.typography.bodyMedium,
                    color = SecondaryText,
                    fontFamily = FontFamily.Monospace
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            // Risk Gauge
            RiskGauge(
                percent = state.memoryUsagePercent,
                riskLevel = state.riskLevel,
                modifier = Modifier.size(80.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "${state.riskLevel.emoji} ${state.riskLevel.displayName}",
                style = MaterialTheme.typography.bodyMedium,
                color = state.riskLevel.color,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun RiskGauge(
    percent: Float,
    riskLevel: RiskLevel,
    modifier: Modifier = Modifier
) {
    val animatedPercent by animateFloatAsState(
        targetValue = percent / 100f,
        animationSpec = tween(durationMillis = 500),
        label = "risk_gauge"
    )

    Canvas(modifier = modifier) {
        val strokeWidth = 8.dp.toPx()
        val radius = (size.minDimension - strokeWidth) / 2
        val center = Offset(size.width / 2, size.height / 2)

        // Background arc
        drawArc(
            color = Color(0xFF2A2A2A),
            startAngle = 135f,
            sweepAngle = 270f,
            useCenter = false,
            topLeft = Offset(center.x - radius, center.y - radius),
            size = Size(radius * 2, radius * 2),
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )

        // Progress arc
        drawArc(
            color = riskLevel.color,
            startAngle = 135f,
            sweepAngle = 270f * animatedPercent,
            useCenter = false,
            topLeft = Offset(center.x - radius, center.y - radius),
            size = Size(radius * 2, radius * 2),
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )
    }
}

@Composable
private fun MemoryCurveCard(state: MemoryLimitsState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "内存使用曲线",
                style = MaterialTheme.typography.titleSmall,
                color = Color.White,
                fontFamily = FontFamily.Monospace
            )
            Text(
                text = "最近 60 秒",
                style = MaterialTheme.typography.bodySmall,
                color = SecondaryText
            )
            Spacer(modifier = Modifier.height(16.dp))

            if (state.memoryHistory.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "启动监控以查看实时曲线",
                        color = SecondaryText,
                        fontFamily = FontFamily.Monospace
                    )
                }
            } else {
                MemoryLineChart(
                    dataPoints = state.memoryHistory,
                    limitBytes = state.memoryLimitBytes,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                )
            }
        }
    }
}

@Composable
private fun MemoryLineChart(
    dataPoints: List<MemoryDataPoint>,
    limitBytes: Long,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        if (dataPoints.isEmpty()) return@Canvas

        val maxUsage = dataPoints.maxOf { it.usageBytes }.toFloat()
        val minUsage = dataPoints.minOf { it.usageBytes }.toFloat()
        val range = (maxUsage - minUsage).coerceAtLeast(1f)

        val width = size.width
        val height = size.height
        val padding = 8.dp.toPx()

        val chartWidth = width - padding * 2
        val chartHeight = height - padding * 2

        val stepX = chartWidth / (dataPoints.size - 1).coerceAtLeast(1)

        // Draw limit line
        val limitY = padding + chartHeight * (1 - (limitBytes - minUsage) / range)
        drawLine(
            color = DangerRed.copy(alpha = 0.5f),
            start = Offset(padding, limitY),
            end = Offset(width - padding, limitY),
            strokeWidth = 1.dp.toPx()
        )

        // Draw curve
        val path = Path()
        dataPoints.forEachIndexed { index, point ->
            val x = padding + index * stepX
            val normalizedY = (point.usageBytes - minUsage) / range
            val y = padding + chartHeight * (1 - normalizedY)

            if (index == 0) {
                path.moveTo(x, y)
            } else {
                path.lineTo(x, y)
            }
        }

        drawPath(
            path = path,
            color = MemoryBlue,
            style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
        )

        // Draw dots at each point
        dataPoints.forEachIndexed { index, point ->
            val x = padding + index * stepX
            val normalizedY = (point.usageBytes - minUsage) / range
            val y = padding + chartHeight * (1 - normalizedY)

            drawCircle(
                color = MemoryBlue,
                radius = 3.dp.toPx(),
                center = Offset(x, y)
            )
        }
    }
}

@Composable
private fun LimiterEventsSummaryCard(state: MemoryLimitsState) {
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
                Text(
                    text = "MemoryLimiter 杀死事件",
                    style = MaterialTheme.typography.titleSmall,
                    color = Color.White,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "${state.limiterEvents.size} 次",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (state.limiterEvents.isEmpty()) SafeGreen else DangerRed,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(8.dp))

            if (state.limiterEvents.isEmpty()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = SafeGreen,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "暂无 MemoryLimiter 杀死记录",
                        style = MaterialTheme.typography.bodySmall,
                        color = SafeGreen
                    )
                }
            } else {
                state.limiterEvents.take(2).forEach { event ->
                    LimiterEventItem(event = event, isCompact = true)
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
private fun LimiterEventItem(event: LimiterEvent, isCompact: Boolean = false) {
    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF2A2A2A), RoundedCornerShape(8.dp))
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = dateFormat.format(Date(event.timestamp)),
                style = MaterialTheme.typography.bodySmall,
                color = Color.White,
                fontFamily = FontFamily.Monospace
            )
            Text(
                text = formatBytes(event.memoryUsageBytes),
                style = MaterialTheme.typography.bodySmall,
                color = DangerRed,
                fontFamily = FontFamily.Monospace
            )
        }
        if (!isCompact) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "进程: ${event.processName}",
                style = MaterialTheme.typography.bodySmall,
                color = SecondaryText,
                fontFamily = FontFamily.Monospace
            )
            Text(
                text = "限制: ${formatBytes(event.memoryLimitBytes)}",
                style = MaterialTheme.typography.bodySmall,
                color = SecondaryText,
                fontFamily = FontFamily.Monospace
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = event.reasonDescription,
                style = MaterialTheme.typography.bodySmall,
                color = WarningOrange,
                fontFamily = FontFamily.Monospace
            )
            Spacer(modifier = Modifier.height(4.dp))
            SelectionContainer {
                Text(
                    text = event.stackTrace,
                    style = MaterialTheme.typography.bodySmall,
                    color = SecondaryText,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun QuickActionsCard(
    state: MemoryLimitsState,
    onIntent: (MemoryLimitsIntent) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "快捷操作",
                style = MaterialTheme.typography.titleSmall,
                color = Color.White,
                fontFamily = FontFamily.Monospace
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { onIntent(MemoryLimitsIntent.TriggerHeapDump("Manual trigger")) },
                    colors = ButtonDefaults.buttonColors(containerColor = MemoryBlue),
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(8.dp)
                ) {
                    Icon(Icons.Default.BugReport, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Heap Dump", fontSize = 12.sp)
                }
                Button(
                    onClick = { onIntent(MemoryLimitsIntent.RunComplianceCheck(state.simulatedDeviceRam)) },
                    colors = ButtonDefaults.buttonColors(containerColor = SafeGreen),
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(8.dp)
                ) {
                    Icon(Icons.Default.FactCheck, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("合规检查", fontSize = 12.sp)
                }
                OutlinedButton(
                    onClick = { onIntent(MemoryLimitsIntent.RunSimulator(state.simulatedDeviceRam)) },
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(8.dp)
                ) {
                    Icon(Icons.Default.Devices, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("模拟器", fontSize = 12.sp)
                }
            }
        }
    }
}

// ================================================================
// Profiler Tab (MemoryProfilerScreen)
// ================================================================

@Composable
private fun ProfilerTab(state: MemoryLimitsState) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "内存占用画像",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                fontFamily = FontFamily.Monospace
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "当前 App 各模块内存占用分布",
                style = MaterialTheme.typography.bodySmall,
                color = SecondaryText
            )
        }

        // Pie Chart Card
        item {
            MemoryPieChartCard(state = state)
        }

        // Module Memory Bars
        item {
            Text(
                text = "模块内存明细",
                style = MaterialTheme.typography.titleSmall,
                color = Color.White,
                fontFamily = FontFamily.Monospace
            )
        }

        items(state.moduleMemoryBreakdown) { module ->
            ModuleMemoryBarItem(module = module, totalUsage = state.memoryUsageBytes)
        }
    }
}

@Composable
private fun MemoryPieChartCard(state: MemoryLimitsState) {
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
                Text(
                    text = "内存分布",
                    style = MaterialTheme.typography.titleSmall,
                    color = Color.White,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "总计: ${formatBytes(state.memoryUsageBytes)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MemoryBlue,
                    fontFamily = FontFamily.Monospace
                )
            }
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Pie Chart
                MemoryPieChart(
                    modules = state.moduleMemoryBreakdown,
                    modifier = Modifier.size(140.dp)
                )

                // Legend
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    state.moduleMemoryBreakdown.forEach { module ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .background(module.riskLevel.color, CircleShape)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = module.moduleName,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MemoryPieChart(
    modules: List<ModuleMemory>,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val center = Offset(size.width / 2, size.height / 2)
        val radius = size.minDimension / 2 * 0.8f

        var startAngle = -90f
        modules.forEach { module ->
            val sweepAngle = module.percentage / 100f * 360f
            drawArc(
                color = module.riskLevel.color,
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = true,
                topLeft = Offset(center.x - radius, center.y - radius),
                size = Size(radius * 2, radius * 2)
            )
            startAngle += sweepAngle
        }
    }
}

@Composable
private fun ModuleMemoryBarItem(module: ModuleMemory, totalUsage: Long) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(module.riskLevel.color, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = module.moduleName,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White,
                        fontFamily = FontFamily.Monospace
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = formatBytes(module.usageBytes),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "${"%.1f".format(module.percentage)}%",
                        style = MaterialTheme.typography.bodySmall,
                        color = SecondaryText,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { module.percentage / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = module.riskLevel.color,
                trackColor = Color(0xFF2A2A2A),
            )
        }
    }
}

// ================================================================
// Events Tab (MemoryLimiterEventScreen)
// ================================================================

@Composable
private fun EventsTab(state: MemoryLimitsState) {
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
                Column {
                    Text(
                        text = "MemoryLimiter 杀死事件",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "ApplicationExitInfo.getDescription() 包含 MemoryLimiter 的记录",
                        style = MaterialTheme.typography.bodySmall,
                        color = SecondaryText
                    )
                }
                if (state.limiterEvents.isNotEmpty()) {
                    Text(
                        text = "${state.limiterEvents.size} 次",
                        style = MaterialTheme.typography.bodyMedium,
                        color = DangerRed,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        if (state.limiterEvents.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = SafeGreen,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "暂无 MemoryLimiter 杀死事件",
                            style = MaterialTheme.typography.bodyLarge,
                            color = SafeGreen,
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "继续保持良好的内存使用习惯",
                            style = MaterialTheme.typography.bodySmall,
                            color = SecondaryText
                        )
                    }
                }
            }
        } else {
            items(state.limiterEvents) { event ->
                LimiterEventItem(event = event, isCompact = false)
            }
        }
    }
}

// ================================================================
// Heap Dump Tab (HeapDumpAnalysisScreen)
// ================================================================

@Composable
private fun HeapDumpTab(
    state: MemoryLimitsState,
    onIntent: (MemoryLimitsIntent) -> Unit
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
                Column {
                    Text(
                        text = "Heap Dump 分析",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "TRIGGER_TYPE_ANOMALY 自动采集或手动触发",
                        style = MaterialTheme.typography.bodySmall,
                        color = SecondaryText
                    )
                }
                Button(
                    onClick = { onIntent(MemoryLimitsIntent.TriggerHeapDump("Manual trigger")) },
                    colors = ButtonDefaults.buttonColors(containerColor = MemoryBlue)
                ) {
                    Icon(Icons.Default.BugReport, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("触发 Dump")
                }
            }
        }

        if (state.heapDumpResults.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountTree,
                            contentDescription = null,
                            tint = SecondaryText,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "暂无 Heap Dump 记录",
                            style = MaterialTheme.typography.bodyLarge,
                            color = SecondaryText,
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "点击上方按钮手动触发 Heap Dump",
                            style = MaterialTheme.typography.bodySmall,
                            color = SecondaryText
                        )
                    }
                }
            }
        }

        items(state.heapDumpResults) { result ->
            HeapDumpResultCard(result = result)
        }
    }
}

@Composable
private fun HeapDumpResultCard(result: HeapDumpResult) {
    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()) }
    val clipboardManager = LocalClipboardManager.current

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Dump #${result.id.takeLast(8)}",
                        style = MaterialTheme.typography.titleSmall,
                        color = Color.White,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = dateFormat.format(Date(result.timestamp)),
                        style = MaterialTheme.typography.bodySmall,
                        color = SecondaryText,
                        fontFamily = FontFamily.Monospace
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = formatBytes(result.fileSizeBytes),
                        style = MaterialTheme.typography.bodySmall,
                        color = MemoryBlue,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "泄漏: ${formatBytes(result.totalLeakingBytes)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = DangerRed,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Leaking Objects Section
            Text(
                text = "泄漏对象",
                style = MaterialTheme.typography.titleSmall,
                color = Color.White,
                fontFamily = FontFamily.Monospace
            )
            Spacer(modifier = Modifier.height(8.dp))

            result.leakingObjects.forEach { obj ->
                LeakingObjectItem(obj = obj)
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Suggestions Section
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "修复建议",
                style = MaterialTheme.typography.titleSmall,
                color = Color.White,
                fontFamily = FontFamily.Monospace
            )
            Spacer(modifier = Modifier.height(8.dp))

            result.suggestions.forEachIndexed { index, suggestion ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF2A2A2A), RoundedCornerShape(4.dp))
                        .padding(8.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = "${index + 1}.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MemoryBlue,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = suggestion,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White,
                        fontFamily = FontFamily.Monospace
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
            }
        }
    }
}

@Composable
private fun LeakingObjectItem(obj: LeakingObject, depth: Int = 0) {
    val indentPadding = (depth * 16).dp

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF2A2A2A), RoundedCornerShape(4.dp))
            .padding(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = obj.className,
                style = MaterialTheme.typography.bodyMedium,
                color = WarningOrange,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.padding(start = indentPadding)
            )
            Text(
                text = "${obj.instanceCount} 实例",
                style = MaterialTheme.typography.bodySmall,
                color = SecondaryText,
                fontFamily = FontFamily.Monospace
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = indentPadding),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Shallow: ${formatBytes(obj.shallowSizeBytes)}",
                style = MaterialTheme.typography.bodySmall,
                color = SecondaryText,
                fontFamily = FontFamily.Monospace
            )
            Text(
                text = "Retained: ${formatBytes(obj.retainedSizeBytes)}",
                style = MaterialTheme.typography.bodySmall,
                color = DangerRed,
                fontFamily = FontFamily.Monospace
            )
        }

        // Children (recursively)
        obj.children.forEach { child ->
            Spacer(modifier = Modifier.height(4.dp))
            LeakingObjectItem(obj = child, depth = depth + 1)
        }
    }
}

// ================================================================
// CI Report Tab (CIReportScreen)
// ================================================================

@Composable
private fun CIReportTab(
    state: MemoryLimitsState,
    onIntent: (MemoryLimitsIntent) -> Unit
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
                Column {
                    Text(
                        text = "CI 合规报告",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "Android 17 per-app memory limits 合规性检测",
                        style = MaterialTheme.typography.bodySmall,
                        color = SecondaryText
                    )
                }
                if (state.ciReport != null) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(ReportFormat.JSON, ReportFormat.HTML, ReportFormat.Markdown).forEach { format ->
                            OutlinedButton(
                                onClick = { onIntent(MemoryLimitsIntent.ExportCiReport(format)) },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(format.name, fontSize = 10.sp)
                            }
                        }
                    }
                }
            }
        }

        // Run Check Button
        if (state.ciReport == null) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudDone,
                            contentDescription = null,
                            tint = SecondaryText,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "点击运行合规检查",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.White,
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { onIntent(MemoryLimitsIntent.RunComplianceCheck(state.simulatedDeviceRam)) },
                            colors = ButtonDefaults.buttonColors(containerColor = SafeGreen)
                        ) {
                            Icon(Icons.Default.FactCheck, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("运行合规检查")
                        }
                    }
                }
            }
        }

        // CI Report Content
        state.ciReport?.let { report ->
            // Overall Status Card
            item {
                CIOverallStatusCard(report = report)
            }

            // Module Reports
            item {
                Text(
                    text = "模块合规详情",
                    style = MaterialTheme.typography.titleSmall,
                    color = Color.White,
                    fontFamily = FontFamily.Monospace
                )
            }

            items(report.moduleReports) { moduleReport ->
                ModuleComplianceCard(report = moduleReport)
            }

            // Summary Stats
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatCard(
                        label = "通过",
                        value = "${report.passedChecks}",
                        color = SafeGreen,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        label = "失败",
                        value = "${report.failedChecks}",
                        color = DangerRed,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        label = "警告",
                        value = "${report.warningChecks}",
                        color = WarningOrange,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun CIOverallStatusCard(report: CiReport) {
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
                Text(
                    text = "综合评分",
                    style = MaterialTheme.typography.bodyMedium,
                    color = SecondaryText,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "${report.overallScore}/100",
                    style = MaterialTheme.typography.headlineMedium,
                    color = when {
                        report.overallScore >= 70 -> SafeGreen
                        report.overallScore >= 50 -> WarningOrange
                        else -> DangerRed
                    },
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = when (report.overallStatus) {
                        ComplianceStatus.PASS -> Icons.Default.CheckCircle
                        ComplianceStatus.FAIL -> Icons.Default.Error
                        ComplianceStatus.WARNING -> Icons.Default.Warning
                    },
                    contentDescription = null,
                    tint = when (report.overallStatus) {
                        ComplianceStatus.PASS -> SafeGreen
                        ComplianceStatus.FAIL -> DangerRed
                        ComplianceStatus.WARNING -> WarningOrange
                    },
                    modifier = Modifier.size(48.dp)
                )
                Text(
                    text = "${report.overallStatus.emoji} ${report.overallStatus.displayName}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = when (report.overallStatus) {
                        ComplianceStatus.PASS -> SafeGreen
                        ComplianceStatus.FAIL -> DangerRed
                        ComplianceStatus.WARNING -> WarningOrange
                    },
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}

@Composable
private fun ModuleComplianceCard(report: ModuleComplianceReport) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = when (report.status) {
                            ComplianceStatus.PASS -> Icons.Default.CheckCircle
                            ComplianceStatus.FAIL -> Icons.Default.Dangerous
                            ComplianceStatus.WARNING -> Icons.Default.Warning
                        },
                        contentDescription = null,
                        tint = when (report.status) {
                            ComplianceStatus.PASS -> SafeGreen
                            ComplianceStatus.FAIL -> DangerRed
                            ComplianceStatus.WARNING -> WarningOrange
                        },
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = report.moduleName,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White,
                        fontFamily = FontFamily.Monospace
                    )
                }
                Text(
                    text = "${report.riskLevel.emoji} ${report.riskLevel.displayName}",
                    style = MaterialTheme.typography.bodySmall,
                    color = report.riskLevel.color,
                    fontFamily = FontFamily.Monospace
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "占用: ${formatBytes(report.memoryUsageBytes)} / ${formatBytes(report.memoryLimitBytes)}",
                style = MaterialTheme.typography.bodySmall,
                color = SecondaryText,
                fontFamily = FontFamily.Monospace
            )
            if (report.suggestions.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                report.suggestions.forEach { suggestion ->
                    Text(
                        text = "• $suggestion",
                        style = MaterialTheme.typography.bodySmall,
                        color = MemoryBlue,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}

@Composable
private fun StatCard(
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                color = color,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = SecondaryText,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

// ================================================================
// Best Practices Tab (BestPracticeGuideScreen)
// ================================================================

@Composable
private fun BestPracticesTab(state: MemoryLimitsState) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1A237E).copy(alpha = 0.3f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Android 17 内存最佳实践",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Android 17 引入 per-app memory limits，当 App 内存使用突破限制时，MemoryLimiter 会杀死 App。遵循以下最佳实践可有效降低被杀死风险。",
                        style = MaterialTheme.typography.bodySmall,
                        color = SecondaryText
                    )
                }
            }
        }

        item {
            Text(
                text = "实践清单",
                style = MaterialTheme.typography.titleSmall,
                color = Color.White,
                fontFamily = FontFamily.Monospace
            )
        }

        items(state.bestPractices) { practice ->
            BestPracticeCard(practice = practice)
        }
    }
}

@Composable
private fun BestPracticeCard(practice: BestPracticeItem) {
    val impactColor = when (practice.impact) {
        "HIGH" -> DangerRed
        "MEDIUM" -> WarningOrange
        else -> SafeGreen
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = when (practice.category) {
                            "Bitmap" -> Icons.Default.Storage
                            "Leak" -> Icons.Default.BugReport
                            "Performance" -> Icons.Default.Speed
                            "Memory" -> Icons.Default.Memory
                            "Debug" -> Icons.Default.BugReport
                            "Config" -> Icons.Default.Info
                            "Lifecycle" -> Icons.Default.Analytics
                            "CI" -> Icons.Default.CloudDone
                            else -> Icons.Default.Info
                        },
                        contentDescription = null,
                        tint = MemoryBlue,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = practice.title,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White,
                        fontFamily = FontFamily.Monospace
                    )
                }
                Box(
                    modifier = Modifier
                        .background(impactColor.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "${practice.impact}",
                        style = MaterialTheme.typography.bodySmall,
                        color = impactColor,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = practice.description,
                style = MaterialTheme.typography.bodySmall,
                color = SecondaryText
            )
            Spacer(modifier = Modifier.height(4.dp))
            Box(
                modifier = Modifier
                    .background(Color(0xFF2A2A2A), RoundedCornerShape(4.dp))
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text(
                    text = practice.category,
                    style = MaterialTheme.typography.bodySmall,
                    color = MemoryBlue,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}

// ================================================================
// Simulator Tab (SimulatorScreen)
// ================================================================

@Composable
private fun SimulatorTab(
    state: MemoryLimitsState,
    onIntent: (MemoryLimitsIntent) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "内存限制模拟器",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "在不同 RAM 设备配置下模拟 Android 17 per-app memory limits",
                        style = MaterialTheme.typography.bodySmall,
                        color = SecondaryText
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    // Device RAM Selector
                    Text(
                        text = "选择设备配置",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    DeviceRamDropdown(
                        selectedPreset = state.selectedDevicePreset,
                        onPresetSelected = { onIntent(MemoryLimitsIntent.SelectDevicePreset(it)) },
                        currentRam = state.simulatedDeviceRam,
                        onRamChanged = { onIntent(MemoryLimitsIntent.RunSimulator(it)) }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Run Simulator Button
                    Button(
                        onClick = { onIntent(MemoryLimitsIntent.RunSimulator(state.simulatedDeviceRam)) },
                        colors = ButtonDefaults.buttonColors(containerColor = MemoryBlue),
                        enabled = !state.isSimulatorRunning,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (state.isSimulatorRunning) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("模拟运行中...")
                        } else {
                            Icon(Icons.Default.PlayArrow, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("开始模拟")
                        }
                    }
                }
            }
        }

        // Simulator Result
        state.simulatorResult?.let { result ->
            item {
                SimulatorResultCard(result = result)
            }
        }

        // Note
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF2A2A2A)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = WarningOrange,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "注意：实际设备内存限制由 Linux cgroup 强制执行，模拟器只能做应用层估算，不能完全复制真实 cgroup 行为。",
                        style = MaterialTheme.typography.bodySmall,
                        color = SecondaryText
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DeviceRamDropdown(
    selectedPreset: DeviceRamPreset,
    onPresetSelected: (DeviceRamPreset) -> Unit,
    currentRam: Long,
    onRamChanged: (Long) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var customRamText by remember { mutableStateOf("") }

    Column {
        Box {
            OutlinedButton(
                onClick = { expanded = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = if (selectedPreset == DeviceRamPreset.Custom)
                            "自定义: ${formatBytes(currentRam)}"
                        else
                            "${selectedPreset.displayName} (${formatBytes(selectedPreset.ramBytes)})",
                        fontFamily = FontFamily.Monospace
                    )
                    Text("▼", fontFamily = FontFamily.Monospace)
                }
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                DeviceRamPreset.entries.forEach { preset ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = if (preset == DeviceRamPreset.Custom)
                                    "自定义"
                                else
                                    "${preset.displayName} (${formatBytes(preset.ramBytes)})",
                                fontFamily = FontFamily.Monospace
                            )
                        },
                        onClick = {
                            onPresetSelected(preset)
                            expanded = false
                        }
                    )
                }
            }
        }

        if (selectedPreset == DeviceRamPreset.Custom) {
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = customRamText,
                onValueChange = { customRamText = it },
                label = { Text("输入 RAM（GB）", fontFamily = FontFamily.Monospace) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                textStyle = LocalTextStyle.current.copy(fontFamily = FontFamily.Monospace)
            )
            if (customRamText.isNotEmpty()) {
                val gbValue = customRamText.toDoubleOrNull()
                if (gbValue != null && gbValue > 0) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "= ${formatBytes((gbValue * 1024 * 1024 * 1024).toLong())}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MemoryBlue,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}

@Composable
private fun SimulatorResultCard(result: SimulatorResult) {
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
                Text(
                    text = "模拟结果",
                    style = MaterialTheme.typography.titleSmall,
                    color = Color.White,
                    fontFamily = FontFamily.Monospace
                )
                Box(
                    modifier = Modifier
                        .background(
                            if (result.wouldTriggerOom) DangerRed.copy(alpha = 0.2f)
                            else SafeGreen.copy(alpha = 0.2f),
                            RoundedCornerShape(4.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (result.wouldTriggerOom) "⚠️ 会触发 OOM" else "✅ 安全",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (result.wouldTriggerOom) DangerRed else SafeGreen,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Stats Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "设备 RAM",
                        style = MaterialTheme.typography.bodySmall,
                        color = SecondaryText,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = formatBytes(result.deviceRam),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White,
                        fontFamily = FontFamily.Monospace
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "App 内存上限",
                        style = MaterialTheme.typography.bodySmall,
                        color = SecondaryText,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = formatBytes(result.appMemoryLimit),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MemoryBlue,
                        fontFamily = FontFamily.Monospace
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "当前占用",
                        style = MaterialTheme.typography.bodySmall,
                        color = SecondaryText,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = formatBytes(result.estimatedUsageBytes),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            // Warnings
            if (result.warnings.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "警告",
                    style = MaterialTheme.typography.titleSmall,
                    color = WarningOrange,
                    fontFamily = FontFamily.Monospace
                )
                Spacer(modifier = Modifier.height(8.dp))
                result.warnings.forEach { warning ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF2A2A2A), RoundedCornerShape(4.dp))
                            .padding(8.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = "⚠️",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = warning,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }

            // Suggestions
            if (result.suggestions.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "优化建议",
                    style = MaterialTheme.typography.titleSmall,
                    color = SafeGreen,
                    fontFamily = FontFamily.Monospace
                )
                Spacer(modifier = Modifier.height(8.dp))
                result.suggestions.forEachIndexed { index, suggestion ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = "${index + 1}.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MemoryBlue,
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = suggestion,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }
    }
}
