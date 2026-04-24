package com.mvi.kenny.feature.audiohardening.screens

// ================================================================
// AudioFallbackPathDetectorScreen — 降级路径检测（工具⑨）
// ================================================================
// 检测音频降级路径 fallback 逻辑是否存在。
//
// PRD-145 工具⑨
// ================================================================

import androidx.compose.foundation.background
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mvi.kenny.feature.audiohardening.AudioFallbackPathDetectorIntent
import com.mvi.kenny.feature.audiohardening.AudioFallbackPathDetectorViewModel
import com.mvi.kenny.feature.audiohardening.DeviceConnectionState
import com.mvi.kenny.feature.audiohardening.FallbackPath

private val ColorCodeBackground = Color(0xFF1E1E1E)

@Composable
fun AudioFallbackPathDetectorScreen(
    onBack: () -> Unit = {},
    viewModel: AudioFallbackPathDetectorViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "⑨ 降级路径检测",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "检测音频降级路径 fallback 逻辑是否存在",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 设备连接状态选择
        Text("设备连接状态", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Medium)
        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            DeviceConnectionState.entries.forEach { deviceState ->
                FilterChip(
                    selected = state.deviceState == deviceState,
                    onClick = { viewModel.sendIntent(AudioFallbackPathDetectorIntent.SetDeviceState(deviceState)) },
                    label = { Text("${deviceState.emoji} ${deviceState.titleCn}") }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 检测按钮
        Button(
            onClick = { viewModel.sendIntent(AudioFallbackPathDetectorIntent.RunDetection) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Radar, null, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(8.dp))
            Text("运行检测")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 检测结果
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(1f)
        ) {
            if (state.detectedPaths.isNotEmpty()) {
                item {
                    Text(
                        "检测到的降级路径",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
                items(state.detectedPaths) { path ->
                    FallbackPathCard(path = path)
                }
            }

            if (state.suggestedEnhancements.isNotEmpty()) {
                item {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "建议增强方案",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                items(state.suggestedEnhancements) { suggestion ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Icon(
                                Icons.Default.Lightbulb,
                                null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                suggestion,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
        }

        if (state.detectedPaths.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = { viewModel.sendIntent(AudioFallbackPathDetectorIntent.ApplyEnhancements) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Build, null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(8.dp))
                Text("应用增强方案")
            }
        }
    }
}

@Composable
private fun FallbackPathCard(path: FallbackPath) {
    val statusColor = if (path.hasFallbackLogic) Color(0xFF4CAF50) else Color(0xFFF44336)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    if (path.hasFallbackLogic) Icons.Default.CheckCircle else Icons.Default.Error,
                    contentDescription = null,
                    tint = statusColor,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "${path.fromState} → ${path.toState}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        if (path.hasFallbackLogic) "✅ 存在降级逻辑" else "❌ 缺少降级逻辑",
                        style = MaterialTheme.typography.bodySmall,
                        color = statusColor
                    )
                }
            }

            if (path.codeSnippet.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    color = ColorCodeBackground
                ) {
                    Text(
                        text = path.codeSnippet,
                        fontFamily = FontFamily.Monospace,
                        fontSize = MaterialTheme.typography.bodySmall.fontSize,
                        color = Color(0xFFD4D4D4),
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }

            if (path.suggestion.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.Top) {
                    Icon(
                        Icons.Default.Lightbulb,
                        null,
                        tint = Color(0xFFFFC107),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        path.suggestion,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}
