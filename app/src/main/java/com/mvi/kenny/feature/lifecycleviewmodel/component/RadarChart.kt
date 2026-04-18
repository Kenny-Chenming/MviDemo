package com.mvi.kenny.feature.lifecycleviewmodel.component

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mvi.kenny.feature.lifecycleviewmodel.RadarChartData
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * 雷达图组件
 * 展示项目健康度的 6 个维度
 *
 * @param data 雷达图数据
 * @param modifier 修饰符
 */
@Composable
fun RadarChart(data: RadarChartData, modifier: Modifier = Modifier) {
    val anim = remember { Animatable(0f) }
    LaunchedEffect(data) {
        anim.snapTo(0f)
        anim.animateTo(1f, animationSpec = tween(800))
    }

    Column(modifier = modifier.fillMaxWidth()) {
        // 雷达图主体
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.size(220.dp)) {
                val center = Offset(size.width / 2, size.height / 2)
                val radius = size.minDimension / 2 * 0.75f
                val sides = data.values.size
                val angleStep = (2 * PI / sides).toFloat()

                // 绘制背景网格（5层）
                for (level in 1..5) {
                    val levelRadius = radius * (level / 5f)
                    val path = Path()
                    for (i in 0 until sides) {
                        val angle = -PI.toFloat() / 2 + i * angleStep
                        val x = center.x + levelRadius * cos(angle)
                        val y = center.y + levelRadius * sin(angle)
                        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                    }
                    path.close()
                    drawPath(path = path, color = Color.Gray.copy(alpha = 0.2f), style = Stroke(1.dp.toPx()))
                }

                // 绘制轴线
                for (i in 0 until sides) {
                    val angle = -PI.toFloat() / 2 + i * angleStep
                    val x = center.x + radius * cos(angle)
                    val y = center.y + radius * sin(angle)
                    drawLine(
                        color = Color.Gray.copy(alpha = 0.3f),
                        start = center,
                        end = Offset(x, y),
                        strokeWidth = 1.dp.toPx()
                    )
                }

                // 绘制数据区域
                val animatedValues = data.values.map { it * anim.value }
                val dataPath = Path()
                for (i in 0 until sides) {
                    val angle = -PI.toFloat() / 2 + i * angleStep
                    val valueRadius = radius * animatedValues[i].coerceIn(0f, 1f)
                    val x = center.x + valueRadius * cos(angle)
                    val y = center.y + valueRadius * sin(angle)
                    if (i == 0) dataPath.moveTo(x, y) else dataPath.lineTo(x, y)
                }
                dataPath.close()
                drawPath(path = dataPath, color = data.colors[0].copy(alpha = 0.3f))
                drawPath(path = dataPath, color = data.colors[0], style = Stroke(2.dp.toPx(), cap = StrokeCap.Round))

                // 绘制数据点
                for (i in 0 until sides) {
                    val angle = -PI.toFloat() / 2 + i * angleStep
                    val valueRadius = radius * animatedValues[i].coerceIn(0f, 1f)
                    val x = center.x + valueRadius * cos(angle)
                    val y = center.y + valueRadius * sin(angle)
                    drawCircle(
                        color = data.colors[i % data.colors.size],
                        radius = 4.dp.toPx(),
                        center = Offset(x, y)
                    )
                }
            }

            // 中心百分比显示
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "${(data.values.average() * 100).toInt()}%",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "健康度",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 图例
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            data.labels.mapIndexed { index, label ->
                val item = RadarLegendItem(
                    label = label,
                    value = data.values.getOrElse(index) { 0f },
                    color = data.colors.getOrElse(index) { Color.Gray }
                )
                item
            }.chunked(2).forEach { rowItems ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    rowItems.forEach { RadarLegendItemView(item = it) }
                    if (rowItems.size == 1) Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

private data class RadarLegendItem(
    val label: String,
    val value: Float,
    val color: Color
) {
    val pct: Int get() = ((value / 1f) * 100).toInt().coerceIn(0, 100)
}

@Composable
private fun RadarLegendItemView(item: RadarLegendItem) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(item.color)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = "${item.label}: ${item.pct}%",
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(start = 4.dp)
        )
    }
}
