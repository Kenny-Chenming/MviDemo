package com.mvi.kenny.feature.remotetoolkit.sub_screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mvi.kenny.feature.remotetoolkit.BreakdownItem
import com.mvi.kenny.feature.remotetoolkit.RemoteToolkitColors
import com.mvi.kenny.feature.remotetoolkit.RemoteToolkitState
import com.mvi.kenny.feature.remotetoolkit.RemoteToolkitIntent
import com.mvi.kenny.feature.remotetoolkit.Trend

// =============================================================
// PerformanceAnalyzerScreen — 性能分析页面
// Performance Analyzer / 性能分析
// =============================================================

private val sampleBreakdown = listOf(
    BreakdownItem("Layout", 42, RemoteToolkitColors.Purple),
    BreakdownItem("Style", 25, RemoteToolkitColors.Teal),
    BreakdownItem("Props", 17, RemoteToolkitColors.Info),
    BreakdownItem("Strings", 8, RemoteToolkitColors.Warning),
    BreakdownItem("Images", 4, RemoteToolkitColors.Coral)
)

@Composable
fun PerformanceAnalyzerScreen(
    state: RemoteToolkitState,
    onIntent: (RemoteToolkitIntent) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(RemoteToolkitColors.Background)
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Performance Analyzer",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = RemoteToolkitColors.OnSurface
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = { onIntent(RemoteToolkitIntent.RunBenchmark) },
                        enabled = !state.isBenchmarking,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = RemoteToolkitColors.Purple)
                    ) {
                        if (state.isBenchmarking) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = RemoteToolkitColors.Purple
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Running...")
                        } else {
                            Text("Run Benchmark")
                        }
                    }
                    OutlinedButton(
                        onClick = { onIntent(RemoteToolkitIntent.ExportReport) },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = RemoteToolkitColors.Teal)
                    ) {
                        Text("Export Report")
                    }
                }
            }
        }

        // ============================================================
        // Metrics Grid (2x2) / 指标网格
        // ============================================================
        item {
            Text(
                text = "Metrics / 性能指标",
                style = MaterialTheme.typography.titleMedium,
                color = RemoteToolkitColors.OnSurface
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MetricCard(
                    title = "Avg Serialize",
                    value = "${state.metrics.avgSerializeMs}ms",
                    trend = state.metrics.serializeTrend,
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    title = "Avg Deserialize",
                    value = "${state.metrics.avgDeserializeMs}ms",
                    trend = state.metrics.deserializeTrend,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MetricCard(
                    title = "Avg Byte Size",
                    value = "${state.metrics.avgByteSizeKb}KB",
                    trend = state.metrics.sizeTrend,
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    title = "Compression",
                    value = "${state.metrics.compressionPercent}%",
                    trend = state.metrics.compressionTrend,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // ============================================================
        // Byte Size Breakdown Bar Chart / 字节大小分解柱状图
        // ============================================================
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Byte Size Breakdown / 字节大小分解",
                style = MaterialTheme.typography.titleMedium,
                color = RemoteToolkitColors.OnSurface
            )
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = RemoteToolkitColors.Surface),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    val breakdown = if (state.breakdownItems.isNotEmpty()) state.breakdownItems else sampleBreakdown
                    breakdown.forEach { item ->
                        BreakdownBarRow(item = item)
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            }
        }

        // ============================================================
        // Radar Chart / 雷达图 (Health Score)
        // ============================================================
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Health Radar / 健康雷达",
                style = MaterialTheme.typography.titleMedium,
                color = RemoteToolkitColors.OnSurface
            )
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = RemoteToolkitColors.Surface),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadarChart(
                            scores = listOf(0.9f, 0.75f, 0.85f, 0.7f, 0.95f, 0.8f),
                            labels = listOf("Serialize", "Deserialize", "Size", "Security", "Reliability", "Latency"),
                            modifier = Modifier.size(200.dp)
                        )
                        Column(
                            modifier = Modifier.padding(start = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Overall Score",
                                style = MaterialTheme.typography.labelMedium,
                                color = RemoteToolkitColors.OnSurfaceVariant
                            )
                            Text(
                                text = "82%",
                                style = MaterialTheme.typography.headlineLarge,
                                fontWeight = FontWeight.Bold,
                                color = RemoteToolkitColors.PassColor
                            )
                            Text(
                                text = "Good / 良好",
                                style = MaterialTheme.typography.bodySmall,
                                color = RemoteToolkitColors.PassColor
                            )
                        }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}

// =============================================================
// MetricCard — 指标卡片
// =============================================================
@Composable
private fun MetricCard(
    title: String,
    value: String,
    trend: Trend,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = RemoteToolkitColors.Surface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = RemoteToolkitColors.OnSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = RemoteToolkitColors.OnSurface
                )
                Spacer(modifier = Modifier.width(8.dp))
                val trendIcon = when (trend) {
                    Trend.UP -> Icons.Default.ArrowUpward
                    Trend.DOWN -> Icons.Default.ArrowDownward
                    Trend.STABLE -> Icons.Default.Remove
                }
                val trendColor = when (trend) {
                    Trend.UP -> RemoteToolkitColors.PassColor
                    Trend.DOWN -> RemoteToolkitColors.CriticalColor
                    Trend.STABLE -> RemoteToolkitColors.OnSurfaceVariant
                }
                Icon(
                    trendIcon,
                    contentDescription = null,
                    tint = trendColor,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = when (trend) {
                        Trend.UP -> "faster"
                        Trend.DOWN -> "slower"
                        Trend.STABLE -> "stable"
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = trendColor
                )
            }
        }
    }
}

// =============================================================
// BreakdownBarRow — 分解条形行
// =============================================================
@Composable
private fun BreakdownBarRow(item: BreakdownItem) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = item.label,
                style = MaterialTheme.typography.bodySmall,
                color = RemoteToolkitColors.OnSurface
            )
            Text(
                text = "${item.percent}%",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = item.color
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
        ) {
            drawRoundRect(
                color = RemoteToolkitColors.SurfaceVariant,
                cornerRadius = CornerRadius(4.dp.toPx())
            )
            drawRoundRect(
                color = item.color,
                size = Size(size.width * (item.percent / 100f), size.height),
                cornerRadius = CornerRadius(4.dp.toPx())
            )
        }
    }
}

// =============================================================
// RadarChart — 雷达图（Canvas 绘制）
// =============================================================
@Composable
private fun RadarChart(
    scores: List<Float>,
    labels: List<String>,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val centerX = size.width / 2
        val centerY = size.height / 2
        val maxRadius = minOf(centerX, centerY) * 0.9f
        val sides = scores.size
        val angleStep = (2 * Math.PI / sides).toFloat()

        // Draw grid rings / 绘制网格圈
        listOf(0.25f, 0.5f, 0.75f, 1f).forEach { ring ->
            val ringPoints = (0 until sides).map { i ->
                val angle = i * angleStep - Math.PI.toFloat() / 2
                Pair(
                    centerX + maxRadius * ring * kotlin.math.cos(angle),
                    centerY + maxRadius * ring * kotlin.math.sin(angle)
                )
            }
            for (i in ringPoints.indices) {
                drawLine(
                    color = RemoteToolkitColors.Outline,
                    start = Offset(ringPoints[i].first, ringPoints[i].second),
                    end = Offset(
                        ringPoints[(i + 1) % sides].first,
                        ringPoints[(i + 1) % sides].second
                    ),
                    strokeWidth = 1.dp.toPx()
                )
            }
        }

        // Draw axes / 绘制轴线
        (0 until sides).forEach { i ->
            val angle = i * angleStep - Math.PI.toFloat() / 2
            val endX = centerX + maxRadius * kotlin.math.cos(angle)
            val endY = centerY + maxRadius * kotlin.math.sin(angle)
            drawLine(
                color = RemoteToolkitColors.Outline,
                start = Offset(centerX, centerY),
                end = Offset(endX, endY),
                strokeWidth = 1.dp.toPx()
            )
        }

        // Draw score polygon / 绘制得分多边形
        val scorePoints = scores.mapIndexed { i, score ->
            val angle = i * angleStep - Math.PI.toFloat() / 2
            Pair(
                centerX + maxRadius * score * kotlin.math.cos(angle),
                centerY + maxRadius * score * kotlin.math.sin(angle)
            )
        }
        for (i in scorePoints.indices) {
            drawLine(
                color = RemoteToolkitColors.Purple,
                start = Offset(scorePoints[i].first, scorePoints[i].second),
                end = Offset(
                    scorePoints[(i + 1) % scorePoints.size].first,
                    scorePoints[(i + 1) % scorePoints.size].second
                ),
                strokeWidth = 2.dp.toPx()
            )
        }

        // Draw score points / 绘制得分点
        scorePoints.forEach { point ->
            drawCircle(
                color = RemoteToolkitColors.Purple,
                radius = 4.dp.toPx(),
                center = Offset(point.first, point.second)
            )
        }
    }
}
