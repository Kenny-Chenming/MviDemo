package com.mvi.kenny.feature.audiohardening.screens

// ================================================================
// AudioHardeningDebugPanelScreen — 可视化调试工具（工具⑧）
// ================================================================
// API 调用链路节点图可视化调试。
//
// PRD-145 工具⑧
// ================================================================

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mvi.kenny.feature.audiohardening.APINode
import com.mvi.kenny.feature.audiohardening.AudioHardeningDebugPanelIntent
import com.mvi.kenny.feature.audiohardening.AudioHardeningDebugPanelViewModel
import com.mvi.kenny.feature.audiohardening.NodeFilter
import com.mvi.kenny.feature.audiohardening.NodeStatus
import com.mvi.kenny.feature.audiohardening.TimelineEvent
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun AudioHardeningDebugPanelScreen(
    onBack: () -> Unit = {},
    viewModel: AudioHardeningDebugPanelViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "⑧ 可视化调试工具",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "API 调用链路节点图可视化调试",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 过滤器
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            NodeFilter.entries.forEach { filter ->
                FilterChip(
                    selected = state.selectedFilter == filter,
                    onClick = { viewModel.sendIntent(AudioHardeningDebugPanelIntent.SetNodeFilter(filter)) },
                    label = { Text(filter.displayName) }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 节点图可视化
        Text("API 调用链路", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Medium)
        Spacer(Modifier.height(8.dp))

        NodeGraphCanvas(
            nodes = state.apiNodes.filter {
                when (state.selectedFilter) {
                    NodeFilter.All -> true
                    NodeFilter.Success -> it.status == NodeStatus.Success
                    NodeFilter.Failed -> it.status == NodeStatus.Failed
                    NodeFilter.Pending -> it.status == NodeStatus.Pending
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 时间轴视图
        Text("时间轴", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Medium)
        Spacer(Modifier.height(8.dp))
        TimelineView(events = state.timelineEvents, modifier = Modifier.weight(1f))

        Spacer(modifier = Modifier.height(12.dp))

        // 导出按钮
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = { viewModel.sendIntent(AudioHardeningDebugPanelIntent.ExportAsHAR) },
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.Download, null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("导出 HAR")
            }
            OutlinedButton(
                onClick = { viewModel.sendIntent(AudioHardeningDebugPanelIntent.ExportAsJSON) },
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.Download, null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("导出 JSON")
            }
        }
    }
}

@Composable
private fun NodeGraphCanvas(nodes: List<APINode>, modifier: Modifier = Modifier) {
    val nodeColor = { status: NodeStatus ->
        when (status) {
            NodeStatus.Success -> Color(0xFF4CAF50)
            NodeStatus.Failed -> Color(0xFFF44336)
            NodeStatus.Pending -> Color(0xFF9E9E9E)
        }
    }

    Canvas(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF1E1E1E))
            .padding(8.dp)
    ) {
        val spacing = if (nodes.isEmpty()) 0f else size.width / (nodes.size + 1)
        nodes.forEachIndexed { index, node ->
            val x = spacing * (index + 1)
            val y = size.height / 2

            // 绘制连接线
            if (index > 0) {
                drawLine(
                    color = Color(0xFF555555),
                    start = Offset(spacing * index, y),
                    end = Offset(x - 20, y),
                    strokeWidth = 2f
                )
            }

            // 绘制节点
            drawCircle(
                color = nodeColor(node.status),
                radius = 14f,
                center = Offset(x, y)
            )

            // 节点边框
            drawCircle(
                color = Color.White.copy(alpha = 0.3f),
                radius = 14f,
                center = Offset(x, y),
                style = Stroke(width = 2f)
            )
        }
    }
}

@Composable
private fun TimelineView(events: List<TimelineEvent>, modifier: Modifier = Modifier) {
    val timeFormat = remember { SimpleDateFormat("HH:mm:ss", Locale.getDefault()) }

    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(events, key = { it.id }) { event ->
            val color = when (event.status) {
                NodeStatus.Success -> Color(0xFF4CAF50)
                NodeStatus.Failed -> Color(0xFFF44336)
                NodeStatus.Pending -> Color(0xFF9E9E9E)
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(color)
                    )
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(event.methodName, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                        Text(
                            "${timeFormat.format(Date(event.timestamp))} | ${event.duration}ms",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }
                    Text(
                        when (event.status) {
                            NodeStatus.Success -> "✅"
                            NodeStatus.Failed -> "❌"
                            NodeStatus.Pending -> "⏳"
                        },
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}
