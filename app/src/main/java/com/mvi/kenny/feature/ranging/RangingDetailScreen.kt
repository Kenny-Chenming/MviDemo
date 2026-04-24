package com.mvi.kenny.feature.ranging

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.cos
import kotlin.math.sin

// =============================================================
// RangingDetailScreen — 单设备测距详情页面
// UWB Ranging API 开发工具包 - 单设备测距 Tab
// =============================================================
/**
 * Ranging Detail Screen / 单设备测距详情页面
 *
 * Displays real-time ranging data for a single UWB device:
 * - Large distance display with animation / 大字体距离显示（带动画）
 * - Distance change chart (last 60 seconds) / 距离变化曲线（最近 60 秒）
 * - Signal strength gauge / 信号强度仪表盘
 * - Angle information (azimuth/elevation) / 角度信息
 *
 * @param state Current UI state / 当前 UI 状态
 * @param onIntent Send intent to ViewModel / 发送意图到 ViewModel
 * @param onNavigateBack Navigate back handler / 返回处理
 *
 * @see RangingDashboardState Full UI state
 * @see RangingIntent User intents
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RangingDetailScreen(
    state: RangingDashboardState,
    onIntent: (RangingIntent) -> Unit,
    onNavigateBack: () -> Unit
) {
    val device = state.selectedDevice
    val history = state.selectedDeviceHistory
    val isRanging = device?.let { state.isRanging(it.address) } ?: false

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // ============================================================
        // Top App Bar / 顶部导航栏
        // ============================================================
        TopAppBar(
            title = {
                Column {
                    Text(
                        text = "测距详情 / Ranging Detail",
                        style = MaterialTheme.typography.titleMedium
                    )
                    if (device != null) {
                        Text(
                            text = device.displayInfo,
                            style = MaterialTheme.typography.labelSmall,
                            fontFamily = FontFamily.Monospace,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            },
            navigationIcon = {
                IconButton(onClick = {
                    onIntent(RangingIntent.ClearSelection)
                    onNavigateBack()
                }) {
                    Icon(Icons.Rounded.ArrowBack, contentDescription = "Back / 返回")
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        )

        if (device == null) {
            // No device selected / 未选择设备
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "请先选择一个设备 / Please select a device first",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            // ============================================================
            // Content / 内容区
            // ============================================================
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Distance Display / 距离显示
                item {
                    DistanceDisplayCard(
                        distance = device.distanceMeters,
                        isRanging = isRanging
                    )
                }

                // Control Button / 控制按钮
                item {
                    RangingControlButton(
                        isRanging = isRanging,
                        onStartRanging = { onIntent(RangingIntent.StartRanging(device.address)) },
                        onStopRanging = { onIntent(RangingIntent.StopRanging(device.address)) }
                    )
                }

                // Angle Info / 角度信息
                if (device.azimuth != null && device.elevation != null && isRanging) {
                    item {
                        AngleInfoCard(
                            azimuth = device.azimuth,
                            elevation = device.elevation
                        )
                    }
                }

                // Signal Strength Gauge / 信号强度仪表盘
                item {
                    SignalGaugeCard(rssi = device.rssi, isRanging = isRanging)
                }

                // Distance Chart / 距离变化曲线
                if (history.isNotEmpty()) {
                    item {
                        DistanceChartCard(history = history)
                    }
                }

                // Device Info / 设备信息
                item {
                    DeviceInfoCard(device = device)
                }
            }
        }
    }
}

// =============================================================
// DistanceDisplayCard — 距离显示卡片
// =============================================================
/**
 * Large distance display with animated number / 大字体距离显示（带数字滚动动画）
 */
@Composable
private fun DistanceDisplayCard(
    distance: Float?,
    isRanging: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1E3A2F)
        ),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = if (isRanging) "实时距离 / Real-time Distance" else "当前距离 / Current Distance",
                style = MaterialTheme.typography.labelMedium,
                color = Color(0xFF69F0AE)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Animated distance value / 动画距离数值
            AnimatedContent(
                targetState = distance,
                transitionSpec = {
                    fadeIn(animationSpec = tween(300)) togetherWith
                            fadeOut(animationSpec = tween(300))
                },
                label = "distanceAnimation"
            ) { targetDistance ->
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    if (targetDistance != null) {
                        // Distance in meters / 米
                        Text(
                            text = String.format("%.2f", targetDistance),
                            style = MaterialTheme.typography.displayLarge.copy(
                                fontSize = 72.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            ),
                            color = Color(0xFF69F0AE)
                        )
                        Text(
                            text = "米 / meters",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color(0xFF69F0AE).copy(alpha = 0.7f)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Distance in centimeters / 厘米
                        Text(
                            text = String.format("%.0f cm", targetDistance * 100),
                            style = MaterialTheme.typography.headlineMedium,
                            fontFamily = FontFamily.Monospace,
                            color = Color.White.copy(alpha = 0.6f)
                        )
                    } else {
                        Text(
                            text = "--.--",
                            style = MaterialTheme.typography.displayLarge.copy(
                                fontSize = 72.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "米 / meters",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

// =============================================================
// RangingControlButton — 测距控制按钮
// =============================================================
/**
 * Start/Stop ranging button / 开始/停止测距按钮
 */
@Composable
private fun RangingControlButton(
    isRanging: Boolean,
    onStartRanging: () -> Unit,
    onStopRanging: () -> Unit
) {
    Button(
        onClick = if (isRanging) onStopRanging else onStartRanging,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isRanging) MaterialTheme.colorScheme.error else Color(0xFF4FC3F7)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Icon(
            imageVector = if (isRanging) Icons.Rounded.Stop else Icons.Rounded.PlayArrow,
            contentDescription = null,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = if (isRanging) "停止测距 / Stop Ranging" else "开始测距 / Start Ranging",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

// =============================================================
// AngleInfoCard — 角度信息卡片
// =============================================================
/**
 * Angle information card (azimuth and elevation) / 角度信息卡片
 */
@Composable
private fun AngleInfoCard(
    azimuth: Float,
    elevation: Float
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            // Azimuth / 方位角
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "方位角 / Azimuth",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                // Compass visualization / 指南针可视化
                CompassView(azimuth = azimuth)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = String.format("%.1f°", azimuth),
                    style = MaterialTheme.typography.headlineSmall,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            }

            // Divider / 分隔线
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(120.dp)
                    .background(MaterialTheme.colorScheme.outline)
            )

            // Elevation / 仰角
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "仰角 / Elevation",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                ElevationView(elevation = elevation)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = String.format("%.1f°", elevation),
                    style = MaterialTheme.typography.headlineSmall,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// =============================================================
// CompassView — 指南针视图
// =============================================================
/**
 * Compass visualization for azimuth / 方位角指南针可视化
 */
@Composable
private fun CompassView(azimuth: Float) {
    val animatedAzimuth by animateFloatAsState(
        targetValue = azimuth,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "azimuthAnimation"
    )

    Canvas(modifier = Modifier.size(80.dp)) {
        val center = Offset(size.width / 2, size.height / 2)
        val radius = size.minDimension / 2 - 8.dp.toPx()

        // Draw compass circle / 绘制指南针圆
        drawCircle(
            color = Color(0xFF4FC3F7).copy(alpha = 0.3f),
            radius = radius,
            center = center,
            style = Stroke(width = 2.dp.toPx())
        )

        // Draw cardinal directions / 绘制方位标记
        listOf(0f, 90f, 180f, 270f).forEach { angle ->
            val radians = Math.toRadians((angle - animatedAzimuth).toDouble())
            val start = Offset(
                (center.x + (radius - 10.dp.toPx()) * sin(radians)).toFloat(),
                (center.y - (radius - 10.dp.toPx()) * cos(radians)).toFloat()
            )
            val end = Offset(
                (center.x + radius * sin(radians)).toFloat(),
                (center.y - radius * cos(radians)).toFloat()
            )
            drawLine(
                color = Color(0xFF4FC3F7),
                start = start,
                end = end,
                strokeWidth = 2.dp.toPx()
            )
        }

        // Draw needle / 绘制指针
        val needleRadians = Math.toRadians(-animatedAzimuth.toDouble())
        val needleEnd = Offset(
            (center.x + radius * 0.6f * sin(needleRadians)).toFloat(),
            (center.y - radius * 0.6f * cos(needleRadians)).toFloat()
        )
        drawLine(
            color = Color(0xFFFF6B6B),
            start = center,
            end = needleEnd,
            strokeWidth = 3.dp.toPx(),
            cap = StrokeCap.Round
        )
    }
}

// =============================================================
// ElevationView — 仰角视图
// =============================================================
/**
 * Elevation angle visualization / 仰角角度可视化
 */
@Composable
private fun ElevationView(elevation: Float) {
    val normalizedElevation = ((elevation + 90f) / 180f).coerceIn(0f, 1f)
    val animatedElevation by animateFloatAsState(
        targetValue = normalizedElevation,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "elevationAnimation"
    )

    Canvas(modifier = Modifier.size(80.dp, 60.dp)) {
        val center = Offset(size.width / 2, size.height / 2)

        // Draw semicircle / 绘制半圆
        drawArc(
            color = Color(0xFF4FC3F7).copy(alpha = 0.3f),
            startAngle = 0f,
            sweepAngle = 180f,
            useCenter = false,
            topLeft = Offset(center.x - size.height / 2, center.y - size.height / 2),
            size = androidx.compose.ui.geometry.Size(size.height, size.height),
            style = Stroke(width = 2.dp.toPx())
        )

        // Draw elevation indicator / 绘制仰角指示器
        val indicatorRadians = Math.toRadians((1 - animatedElevation) * 180.0)
        val indicatorX = center.x + (size.height / 2 - 4.dp.toPx()) * kotlin.math.cos(indicatorRadians).toFloat()
        val indicatorY = center.y - (size.height / 2 - 4.dp.toPx()) * kotlin.math.sin(indicatorRadians).toFloat()

        drawCircle(
            color = Color(0xFF69F0AE),
            radius = 6.dp.toPx(),
            center = Offset(indicatorX, indicatorY)
        )
    }
}

// =============================================================
// SignalGaugeCard — 信号强度仪表盘
// =============================================================
/**
 * Signal strength gauge / 信号强度仪表盘
 */
@Composable
private fun SignalGaugeCard(
    rssi: Int,
    isRanging: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Gauge / 仪表盘
            RssiGauge(
                rssi = if (isRanging) rssi else rssi,
                modifier = Modifier.size(100.dp)
            )

            Spacer(modifier = Modifier.width(24.dp))

            Column {
                Text(
                    text = "信号强度 / Signal Strength",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "$rssi dBm",
                    style = MaterialTheme.typography.headlineMedium,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = when {
                        rssi >= -60 -> Color(0xFF69F0AE)
                        rssi >= -75 -> Color(0xFF4FC3F7)
                        rssi >= -90 -> Color(0xFFFFD54F)
                        else -> Color(0xFFFF6B6B)
                    }
                )
                Text(
                    text = when {
                        rssi >= -60 -> "极强 / Excellent"
                        rssi >= -75 -> "良好 / Good"
                        rssi >= -90 -> "一般 / Fair"
                        else -> "较弱 / Poor"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// =============================================================
// RssiGauge — RSSI 仪表盘
// =============================================================
/**
 * RSSI gauge visualization / RSSI 仪表盘可视化
 */
@Composable
private fun RssiGauge(
    rssi: Int,
    modifier: Modifier = Modifier
) {
    val normalizedRssi = ((rssi + 100f) / 60f).coerceIn(0f, 1f)

    Canvas(modifier = modifier) {
        val center = Offset(size.width / 2, size.height / 2)
        val radius = size.minDimension / 2 - 8.dp.toPx()

        // Background arc / 背景弧
        drawArc(
            color = Color.Gray.copy(alpha = 0.3f),
            startAngle = 135f,
            sweepAngle = 270f,
            useCenter = false,
            topLeft = Offset(center.x - radius, center.y - radius),
            size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2),
            style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
        )

        // Value arc / 数值弧
        val sweepAngle = normalizedRssi * 270f
        val arcColor = when {
            rssi >= -60 -> Color(0xFF69F0AE)
            rssi >= -75 -> Color(0xFF4FC3F7)
            rssi >= -90 -> Color(0xFFFFD54F)
            else -> Color(0xFFFF6B6B)
        }

        drawArc(
            brush = Brush.sweepGradient(
                colors = listOf(
                    Color(0xFFFF6B6B),
                    Color(0xFFFFD54F),
                    Color(0xFF4FC3F7),
                    Color(0xFF69F0AE)
                ),
                center = center
            ),
            startAngle = 135f,
            sweepAngle = sweepAngle,
            useCenter = false,
            topLeft = Offset(center.x - radius, center.y - radius),
            size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2),
            style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
        )
    }
}

// =============================================================
// DistanceChartCard — 距离变化曲线卡片
// =============================================================
/**
 * Distance change chart (line chart) / 距离变化曲线（折线图）
 */
@Composable
private fun DistanceChartCard(
    history: List<RangingResult>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "距离变化曲线 / Distance Trend",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "最近 ${history.size} 次测量 / Last ${history.size} measurements",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Chart / 图表
            DistanceLineChart(
                history = history,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
            )
        }
    }
}

// =============================================================
// DistanceLineChart — 距离折线图
// =============================================================
/**
 * Distance line chart visualization / 距离折线图可视化
 */
@Composable
private fun DistanceLineChart(
    history: List<RangingResult>,
    modifier: Modifier = Modifier
) {
    if (history.isEmpty()) return

    val distances = remember(history) { history.map { it.distanceMeters } }
    val minDistance = (distances.minOrNull() ?: 0f) - 0.5f
    val maxDistance = (distances.maxOrNull() ?: 10f) + 0.5f
    val range = (maxDistance - minDistance).coerceAtLeast(1f)

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val padding = 16.dp.toPx()

        val chartWidth = width - padding * 2
        val chartHeight = height - padding * 2

        // Draw grid lines / 绘制网格线
        for (i in 0..4) {
            val y = padding + (chartHeight * i / 4)
            drawLine(
                color = Color.Gray.copy(alpha = 0.2f),
                start = Offset(padding, y),
                end = Offset(width - padding, y),
                strokeWidth = 1.dp.toPx()
            )
        }

        // Draw line chart / 绘制折线图
        if (history.size > 1) {
            val stepX = chartWidth / (history.size - 1)

            // Draw filled area / 绘制填充区域
            val fillPath = androidx.compose.ui.graphics.Path().apply {
                moveTo(padding, height - padding)
                history.forEachIndexed { index, result ->
                    val x = padding + index * stepX
                    val y = padding + chartHeight - ((result.distanceMeters - minDistance) / range * chartHeight)
                    lineTo(x, y)
                }
                lineTo(padding + (history.size - 1) * stepX, height - padding)
                close()
            }

            drawPath(
                path = fillPath,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF69F0AE).copy(alpha = 0.3f),
                        Color(0xFF69F0AE).copy(alpha = 0.0f)
                    )
                )
            )

            // Draw line / 绘制线条
            for (i in 0 until history.size - 1) {
                val x1 = padding + i * stepX
                val y1 = padding + chartHeight - ((history[i].distanceMeters - minDistance) / range * chartHeight)
                val x2 = padding + (i + 1) * stepX
                val y2 = padding + chartHeight - ((history[i + 1].distanceMeters - minDistance) / range * chartHeight)

                drawLine(
                    color = Color(0xFF69F0AE),
                    start = Offset(x1, y1),
                    end = Offset(x2, y2),
                    strokeWidth = 2.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }

            // Draw points / 绘制数据点
            history.forEachIndexed { index, result ->
                val x = padding + index * stepX
                val y = padding + chartHeight - ((result.distanceMeters - minDistance) / range * chartHeight)
                drawCircle(
                    color = Color(0xFF69F0AE),
                    radius = 3.dp.toPx(),
                    center = Offset(x, y)
                )
            }
        }
    }
}

// =============================================================
// DeviceInfoCard — 设备信息卡片
// =============================================================
/**
 * Device information card / 设备信息卡片
 */
@Composable
private fun DeviceInfoCard(
    device: UwbDevice
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "设备信息 / Device Info",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            InfoRow(label = "地址 / Address", value = device.address)
            InfoRow(label = "名称 / Name", value = device.displayName ?: "N/A")
            InfoRow(label = "RSSI", value = "${device.rssi} dBm")
            InfoRow(label = "首次发现 / First Seen", value = formatTimestamp(device.firstSeenTimestamp))
            InfoRow(label = "信号等级 / Signal Level", value = device.signalLevel.label)
        }
    }
}

// =============================================================
// InfoRow — 信息行
// =============================================================
/**
 * Info row component / 信息行组件
 */
@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Medium
        )
    }
}

// =============================================================
// Timestamp formatter / 时间戳格式化
// =============================================================
/**
 * Format timestamp to readable string / 将时间戳格式化为可读字符串
 */
private fun formatTimestamp(timestamp: Long): String {
    val diff = System.currentTimeMillis() - timestamp
    return when {
        diff < 60_000 -> "刚刚 / Just now"
        diff < 3600_000 -> "${diff / 60_000}s ago / ${diff / 60_000}秒前"
        diff < 86400_000 -> "${diff / 3600_000}h ago / ${diff / 3600_000}小时前"
        else -> "${diff / 86400_000}d ago / ${diff / 86400_000}天前"
    }
}
