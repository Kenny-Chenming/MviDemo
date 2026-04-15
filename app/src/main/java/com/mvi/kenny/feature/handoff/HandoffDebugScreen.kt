package com.mvi.kenny.feature.handoff

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * ============================================================
 * HandoffDebugScreen — 调试面板
 * ============================================================
 * 模拟发送接收、JSON 查看、冲突调试
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HandoffDebugScreen(
    onNavigateBack: () -> Unit,
    viewModel: HandoffViewModel
) {
    val state by viewModel.debugState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("调试面板") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.sendIntent(HandoffIntent.ClearDebugLogs) }) {
                        Icon(Icons.Default.Delete, contentDescription = "清除日志")
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(modifier = Modifier.height(8.dp)) }

            // Simulate Actions / 模拟操作
            item {
                SimulateActionsCard(
                    isSimulatingSend = state.isSimulatingSend,
                    isSimulatingReceive = state.isSimulatingReceive,
                    onSimulateSend = { viewModel.sendIntent(HandoffIntent.SimulateSend) },
                    onSimulateReceive = { viewModel.sendIntent(HandoffIntent.SimulateReceive) }
                )
            }

            // Conflict Simulation / 冲突模拟
            item {
                ConflictSimulationCard(
                    conflictData = state.simulatedConflict,
                    isResolved = state.isConflictResolved,
                    selectedResolution = state.selectedResolution,
                    onSimulateConflict = { viewModel.sendIntent(HandoffIntent.SimulateConflict) },
                    onResolve = { viewModel.sendIntent(HandoffIntent.ResolveConflict(it)) }
                )
            }

            // Serialized JSON / 序列化 JSON
            if (state.serializedJson.isNotEmpty()) {
                item {
                    JsonViewerCard(
                        title = "序列化数据 (发送)",
                        json = state.serializedJson
                    )
                }
            }

            // Deserialized Data / 反序列化数据
            if (state.deserializedData.isNotEmpty()) {
                item {
                    JsonViewerCard(
                        title = "反序列化数据 (接收)",
                        json = state.deserializedData
                    )
                }
            }

            // Debug Logs / 调试日志
            item {
                Text("调试日志 (${state.debugLogs.size})", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            }

            if (state.debugLogs.isEmpty()) {
                item {
                    EmptyLogsState()
                }
            } else {
                items(state.debugLogs.reversed()) { log ->
                    DebugLogItem(log = log)
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}

/**
 * Simulate Actions Card / 模拟操作卡片
 */
@Composable
private fun SimulateActionsCard(
    isSimulatingSend: Boolean,
    isSimulatingReceive: Boolean,
    onSimulateSend: () -> Unit,
    onSimulateReceive: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.BugReport, contentDescription = null, tint = HandoffColors.Primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("模拟 Handoff", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Send button / 发送按钮
                Button(
                    onClick = onSimulateSend,
                    enabled = !isSimulatingSend && !isSimulatingReceive,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = HandoffColors.Primary)
                ) {
                    if (isSimulatingSend) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = Color.White)
                    } else {
                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (isSimulatingSend) "发送中..." else "模拟发送")
                }

                // Receive button / 接收按钮
                Button(
                    onClick = onSimulateReceive,
                    enabled = !isSimulatingSend && !isSimulatingReceive,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = HandoffColors.HandoffActive)
                ) {
                    if (isSimulatingReceive) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = Color.White)
                    } else {
                        Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (isSimulatingReceive) "接收中..." else "模拟接收")
                }
            }
        }
    }
}

/**
 * Conflict Simulation Card / 冲突模拟卡片
 */
@Composable
private fun ConflictSimulationCard(
    conflictData: String,
    isResolved: Boolean,
    selectedResolution: SerializationStrategy,
    onSimulateConflict: () -> Unit,
    onResolve: (SerializationStrategy) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (conflictData.isNotEmpty() && !isResolved)
                HandoffColors.Error.copy(alpha = 0.05f)
            else
                MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Warning,
                        contentDescription = null,
                        tint = if (conflictData.isNotEmpty() && !isResolved) HandoffColors.Error else HandoffColors.HandoffInactive
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("状态冲突", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    if (isResolved) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("✓ 已解决", style = MaterialTheme.typography.labelSmall, color = HandoffColors.HandoffActive)
                    }
                }

                OutlinedButton(onClick = onSimulateConflict) {
                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("模拟冲突")
                }
            }

            if (conflictData.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF1E1E1E))
                        .padding(12.dp)
                ) {
                    Text(
                        text = conflictData,
                        style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                        color = if (isResolved) HandoffColors.HandoffActive else Color(0xFFFF6B6B)
                    )
                }

                if (!isResolved) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("选择解决策略:", style = MaterialTheme.typography.labelMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SerializationStrategy.entries.forEach { strategy ->
                            FilterChip(
                                selected = selectedResolution == strategy,
                                onClick = { onResolve(strategy) },
                                label = { Text(strategy.labelZh) }
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * JSON Viewer Card / JSON 查看器卡片
 */
@Composable
private fun JsonViewerCard(title: String, json: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF1E1E1E))
                    .padding(12.dp)
            ) {
                Text(
                    text = json,
                    style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                    color = Color(0xFFD4D4D4),
                    modifier = Modifier.horizontalScroll(rememberScrollState())
                )
            }
        }
    }
}

/**
 * Debug Log Item / 调试日志项
 */
@Composable
private fun DebugLogItem(log: DebugLogEntry) {
    val dateFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .padding(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        // Level indicator / 级别指示器
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(log.level.color)
        )

        Spacer(modifier = Modifier.width(8.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = log.level.label,
                    style = MaterialTheme.typography.labelSmall,
                    color = log.level.color,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = dateFormat.format(Date(log.timestamp)),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                )
            }
            Text(
                text = log.message,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 2.dp)
            )
            if (log.data != null) {
                Text(
                    text = log.data,
                    style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

/**
 * Empty Logs State / 空日志状态
 */
@Composable
private fun EmptyLogsState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            Icons.Default.BugReport,
            contentDescription = null,
            modifier = Modifier.size(48.dp),
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text("暂无调试日志", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
        Text("模拟发送/接收操作后，日志将显示在这里", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
    }
}
