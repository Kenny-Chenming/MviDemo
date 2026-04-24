package com.mvi.kenny.feature.audiohardening.screens

// ================================================================
// SilentFailureMonitorScreen — 静默失败监控面板（工具②）
// ================================================================
// 实时监控后台音频 API 静默失败事件。
//
// PRD-145 工具②
// ================================================================

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mvi.kenny.feature.audiohardening.SilentFailureMonitorIntent
import com.mvi.kenny.feature.audiohardening.SilentFailureMonitorViewModel
import com.mvi.kenny.feature.audiohardening.AudioAPIEvent
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun SilentFailureMonitorScreen(
    onBack: () -> Unit = {},
    viewModel: SilentFailureMonitorViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "② 静默失败监控面板",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "实时监控后台音频 API 静默失败事件",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 监控开关
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                if (state.isMonitoring) Icons.Default.Pending else Icons.Default.PlayArrow,
                contentDescription = null,
                tint = if (state.isMonitoring) Color(0xFF4CAF50) else Color(0xFF9E9E9E)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                if (state.isMonitoring) "监控中…" else "已停止",
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(Modifier.weight(1f))
            Switch(
                checked = state.isMonitoring,
                onCheckedChange = { viewModel.sendIntent(SilentFailureMonitorIntent.ToggleMonitoring) }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // API 过滤器
        Text("API 类型过滤", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Medium)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(null to "全部") + state.filterOptions.map { it to it }.toList().take(4).let {
                it
            }.forEach { (api, label) ->
                FilterChip(
                    selected = state.selectedFilter == api,
                    onClick = { viewModel.sendIntent(SilentFailureMonitorIntent.SetApiFilter(api)) },
                    label = { Text(label) }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 统计摘要
        if (state.statistics.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("失败频率统计", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Medium)
                    Spacer(Modifier.height(8.dp))
                    state.statistics.forEach { (api, count) ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(api, style = MaterialTheme.typography.bodySmall)
                            Text("$count 次", style = MaterialTheme.typography.bodySmall, color = Color(0xFFF44336))
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 清空按钮
        Row {
            TextButton(onClick = { viewModel.sendIntent(SilentFailureMonitorIntent.ClearEvents) }) {
                Icon(Icons.Default.Delete, null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("清空事件")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 事件列表
        val filteredEvents = if (state.selectedFilter != null)
            state.events.filter { it.apiType == state.selectedFilter }
        else state.events

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(filteredEvents, key = { it.id }) { event ->
                EventItem(event = event)
            }
        }
    }
}

@Composable
private fun EventItem(event: AudioAPIEvent) {
    val timeFormat = remember { SimpleDateFormat("HH:mm:ss", Locale.getDefault()) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF44336).copy(alpha = 0.05f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.ErrorOutline,
                    contentDescription = null,
                    tint = Color(0xFFF44336),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    event.apiType,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Spacer(Modifier.weight(1f))
                Text(
                    timeFormat.format(Date(event.timestamp)),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }
            Spacer(Modifier.height(4.dp))
            Text(
                "线程: ${event.callingThread} | 错误码: ${event.errorCode}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            Text(
                event.stackTrace,
                style = MaterialTheme.typography.bodySmall,
                fontFamily = FontFamily.Monospace,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                maxLines = 2
            )
        }
    }
}
