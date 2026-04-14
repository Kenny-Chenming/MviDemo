package com.mvi.kenny.feature.remotetoolkit.sub_screens

import com.mvi.kenny.feature.remotetoolkit.RemoteToolkitState
import com.mvi.kenny.feature.remotetoolkit.RemoteToolkitIntent
import com.mvi.kenny.feature.remotetoolkit.RemoteToolkitEffect
import com.mvi.kenny.feature.remotetoolkit.RemoteToolkitColors


import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mvi.kenny.feature.remotetoolkit.ServerStatus
import com.mvi.kenny.feature.remotetoolkit.TimelineEvent
import com.mvi.kenny.feature.remotetoolkit.TimelineStatus
import com.mvi.kenny.feature.remotetoolkit.UIRemoteNode

// =============================================================
// DebugPanelScreen — 实时调试面板
// Debug Panel / 调试面板
// =============================================================

@Composable
fun DebugPanelScreen(
    state: RemoteToolkitState,
    onIntent: (RemoteToolkitIntent) -> Unit
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(RemoteToolkitColors.Background)
            .padding(horizontal = 24.dp, vertical = 16.dp)
    ) {
        // ============================================================
        // Header / 标题栏
        // ============================================================
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Debug Panel",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = RemoteToolkitColors.OnSurface
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                when (state.serverStatus) {
                    ServerStatus.STOPPED -> {
                        Button(
                            onClick = { onIntent(RemoteToolkitIntent.StartDebugServer) },
                            colors = ButtonDefaults.buttonColors(containerColor = RemoteToolkitColors.Teal)
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Start Server")
                        }
                    }
                    ServerStatus.STARTING -> {
                        Button(
                            onClick = { },
                            enabled = false,
                            colors = ButtonDefaults.buttonColors(disabledContainerColor = RemoteToolkitColors.SurfaceVariant)
                        ) {
                            Text("Starting...")
                        }
                    }
                    ServerStatus.RUNNING -> {
                        OutlinedButton(
                            onClick = { onIntent(RemoteToolkitIntent.StopDebugServer) },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = RemoteToolkitColors.Coral)
                        ) {
                            Icon(Icons.Default.Stop, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Stop")
                        }
                        IconButton(
                            onClick = { onIntent(RemoteToolkitIntent.ClearTimeline) }
                        ) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear", tint = RemoteToolkitColors.OnSurfaceVariant)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ============================================================
        // Split Layout: Tree + Inspector / 分栏布局
        // ============================================================
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Left: UI Tree / 左侧：UI 树
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            ) {
                Text(
                    text = "UI Tree",
                    style = MaterialTheme.typography.titleSmall,
                    color = RemoteToolkitColors.OnSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    modifier = Modifier.fillMaxSize(),
                    colors = CardDefaults.cardColors(containerColor = RemoteToolkitColors.Surface),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (state.uiTree != null) {
                        LazyColumn(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            item {
                                UITreeNode(
                                    node = state.uiTree,
                                    selectedNodeId = state.selectedNodeId,
                                    depth = 0,
                                    onSelect = { onIntent(RemoteToolkitIntent.SelectTreeNode(it)) }
                                )
                            }
                        }
                    } else {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (state.serverStatus == ServerStatus.RUNNING) "Loading tree..."
                                       else "Start server to view UI tree",
                                style = MaterialTheme.typography.bodyMedium,
                                color = RemoteToolkitColors.OnSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Right: Inspector / 右侧：属性检查器
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            ) {
                Text(
                    text = "Inspector",
                    style = MaterialTheme.typography.titleSmall,
                    color = RemoteToolkitColors.OnSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    modifier = Modifier.fillMaxSize(),
                    colors = CardDefaults.cardColors(containerColor = RemoteToolkitColors.Surface),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        if (state.selectedNodeId != null && state.uiTree != null) {
                            val node = findNode(state.uiTree, state.selectedNodeId)
                            if (node != null) {
                                InspectorContent(node = node)
                            }
                        } else {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Select a node to inspect",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = RemoteToolkitColors.OnSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ============================================================
        // Timeline / 时间线
        // ============================================================
        Text(
            text = "Serialization Timeline",
            style = MaterialTheme.typography.titleSmall,
            color = RemoteToolkitColors.OnSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = RemoteToolkitColors.Surface),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                if (state.timelineEvents.isNotEmpty()) {
                    SerializationTimeline(events = state.timelineEvents)
                } else {
                    Text(
                        text = "No timeline events / 暂无时间线事件",
                        style = MaterialTheme.typography.bodySmall,
                        color = RemoteToolkitColors.OnSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ============================================================
        // Byte Inspector / 字节检查器
        // ============================================================
        Text(
            text = "Byte Inspector",
            style = MaterialTheme.typography.titleSmall,
            color = RemoteToolkitColors.OnSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = RemoteToolkitColors.Surface),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF0D1117))
                        .border(1.dp, RemoteToolkitColors.Outline, RoundedCornerShape(8.dp))
                        .horizontalScroll(scrollState)
                        .padding(12.dp)
                ) {
                    Text(
                        text = "0x1F 8B 08 00 00 00 00 00  00 03 A2 54 6F 70 52 65  │  ...TopRe│\n" +
                                "6D 6F 74 65 43 6F 6E 74  65 6E 74 2E 6A 73 6F 6E  │  moteCont│\n" +
                                "00 00 00 01 00 00 00 00  00 00 00 00 00 00 00 00  │  ........│",
                        style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                        color = Color(0xFFE1E4E8)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(onClick = { }) {
                        Text("Copy", color = RemoteToolkitColors.Purple)
                    }
                    TextButton(onClick = { }) {
                        Text("Export", color = RemoteToolkitColors.Info)
                    }
                }
            }
        }
    }
}

// =============================================================
// UITreeNode — UI 树节点（递归）
// =============================================================
@Composable
private fun UITreeNode(
    node: UIRemoteNode,
    selectedNodeId: String?,
    depth: Int,
    onSelect: (String) -> Unit
) {
    val isSelected = node.id == selectedNodeId
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(4.dp))
                .background(
                    if (isSelected) RemoteToolkitColors.Purple.copy(alpha = 0.2f)
                    else Color.Transparent
                )
                .clickable { onSelect(node.id) }
                .padding(start = (depth * 16).dp, top = 4.dp, bottom = 4.dp, end = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (node.children.isNotEmpty()) "▼" else "○",
                color = RemoteToolkitColors.OnSurfaceVariant,
                style = MaterialTheme.typography.bodySmall
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = node.name,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = RemoteToolkitColors.OnSurface
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "(${node.type})",
                style = MaterialTheme.typography.labelSmall,
                color = RemoteToolkitColors.Purple
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = "${node.serializedSize}B",
                style = MaterialTheme.typography.labelSmall,
                color = RemoteToolkitColors.OnSurfaceVariant
            )
        }
        node.children.forEach { child ->
            UITreeNode(
                node = child,
                selectedNodeId = selectedNodeId,
                depth = depth + 1,
                onSelect = onSelect
            )
        }
    }
}

// =============================================================
// InspectorContent — 检查器内容
// =============================================================
@Composable
private fun InspectorContent(node: UIRemoteNode) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "Selected: ",
                style = MaterialTheme.typography.labelMedium,
                color = RemoteToolkitColors.OnSurfaceVariant
            )
            Text(
                text = node.name,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = RemoteToolkitColors.Purple
            )
        }

        HorizontalDivider(color = RemoteToolkitColors.Outline)

        Text(
            text = "Type: ${node.type}",
            style = MaterialTheme.typography.bodySmall,
            color = RemoteToolkitColors.OnSurfaceVariant
        )

        Text(
            text = "Props:",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Medium,
            color = RemoteToolkitColors.OnSurface
        )
        node.props.forEach { (key, value) ->
            Row {
                Text(
                    text = "$key = ",
                    style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                    color = RemoteToolkitColors.Teal
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                    color = RemoteToolkitColors.OnSurface
                )
            }
        }

        HorizontalDivider(color = RemoteToolkitColors.Outline)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "Serialized Size",
                    style = MaterialTheme.typography.labelSmall,
                    color = RemoteToolkitColors.OnSurfaceVariant
                )
                Text(
                    text = "${node.serializedSize}B",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = RemoteToolkitColors.OnSurface
                )
            }
            Column {
                Text(
                    text = "Deserialize",
                    style = MaterialTheme.typography.labelSmall,
                    color = RemoteToolkitColors.OnSurfaceVariant
                )
                Text(
                    text = "0.3ms",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = RemoteToolkitColors.Teal
                )
            }
        }
    }
}

// =============================================================
// SerializationTimeline — 序列化时间线
// =============================================================
@Composable
private fun SerializationTimeline(events: List<TimelineEvent>) {
    val scrollState = rememberScrollState()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState),
        horizontalArrangement = Arrangement.spacedBy(0.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        events.forEachIndexed { index, event ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.width(80.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            when (event.status) {
                                TimelineStatus.COMPLETE -> RemoteToolkitColors.PassColor
                                TimelineStatus.ACTIVE -> RemoteToolkitColors.Purple
                                TimelineStatus.PENDING -> RemoteToolkitColors.SurfaceVariant
                            }
                        )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = event.label,
                    style = MaterialTheme.typography.labelSmall,
                    color = RemoteToolkitColors.OnSurface
                )
                Text(
                    text = "${event.durationMs}ms",
                    style = MaterialTheme.typography.labelSmall,
                    color = when (event.status) {
                        TimelineStatus.ACTIVE -> RemoteToolkitColors.Purple
                        else -> RemoteToolkitColors.OnSurfaceVariant
                    }
                )
            }
            if (index < events.size - 1) {
                Canvas(
                    modifier = Modifier
                        .width(40.dp)
                        .height(2.dp)
                ) {
                    drawLine(
                        color = RemoteToolkitColors.Outline,
                        start = Offset(0f, size.height / 2),
                        end = Offset(size.width, size.height / 2),
                        strokeWidth = 2.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                }
            }
        }
    }
}

// =============================================================
// findNode — 递归查找树节点
// =============================================================
private fun findNode(root: UIRemoteNode, nodeId: String): UIRemoteNode? {
    if (root.id == nodeId) return root
    root.children.forEach { child ->
        findNode(child, nodeId)?.let { return it }
    }
    return null
}
