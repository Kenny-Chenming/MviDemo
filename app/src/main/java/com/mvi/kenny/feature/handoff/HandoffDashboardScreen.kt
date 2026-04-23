package com.mvi.kenny.feature.handoff

import android.widget.Toast
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.DesignServices
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.Badge
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mvi.kenny.base.TopBarConfig
import kotlinx.coroutines.flow.collectLatest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * ============================================================
 * HandoffDashboardScreen — Handoff 功能仪表盘主界面
 * ============================================================
 */
@Composable
fun HandoffDashboardScreen(
    viewModel: HandoffDashboardViewModel = viewModel(),
    onUpdateTopBar: (TopBarConfig) -> Unit = {},
    onNavigateToSubScreen: (HandoffScreen) -> Unit = {}
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(Unit) { onUpdateTopBar(TopBarConfig(title = "Handoff 工具台")) }

    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is HandoffDashboardEffect.NavigateTo -> onNavigateToSubScreen(effect.screen)
                is HandoffDashboardEffect.ShowToast -> Toast.makeText(context, effect.message, Toast.LENGTH_SHORT).show()
                is HandoffDashboardEffect.ShowError -> Toast.makeText(context, effect.throwable.message, Toast.LENGTH_LONG).show()
                is HandoffDashboardEffect.ScanCompleted -> { }
                is HandoffDashboardEffect.PairingSucceeded -> Toast.makeText(context, "${effect.deviceName} 配对成功", Toast.LENGTH_SHORT).show()
            }
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Handoff Capability Card
        item {
            HandoffCapabilityCard(
                isAvailable = state.isHandoffAvailable,
                androidVersion = state.androidVersion,
                apiLevel = state.apiLevel,
                isScanning = state.isScanning,
                onStartScan = { viewModel.sendIntent(HandoffDashboardIntent.StartDeviceScan) },
                onStopScan = { viewModel.sendIntent(HandoffDashboardIntent.StopDeviceScan) }
            )
        }

        // Feature Grid Header
        item {
            Text("功能入口", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 8.dp))
        }

        // Feature Grid
        item {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.height(300.dp)
            ) {
                items(state.allFeatures) { feature ->
                    FeatureEntryCard(
                        feature = feature,
                        onClick = { viewModel.sendIntent(HandoffDashboardIntent.NavigateToFeature(feature)) }
                    )
                }
            }
        }

        // Paired Devices
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("已配对设备", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text("${state.pairedDevices.size} 台", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        if (state.pairedDevices.isEmpty()) {
            item { EmptyStateCard("暂无配对设备，点击扫描发现新设备") }
        } else {
            items(state.pairedDevices) { device ->
                PairedDeviceItem(device = device, onRemove = { viewModel.sendIntent(HandoffDashboardIntent.RemovePairedDevice(device.id)) })
            }
        }

        // Recent Handoffs
        item {
            Text("最近 Handoff", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 8.dp))
        }

        if (state.recentHandoffs.isEmpty()) {
            item { EmptyStateCard("暂无 Handoff 记录") }
        } else {
            items(state.recentHandoffs.take(5)) { record ->
                HandoffRecordItem(record = record)
            }
        }

        // Version Footer
        item {
            Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Handoff Toolkit v1.0.0", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("Cross-Device Handoff API Level ${state.apiLevel}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                val compat = when {
                    state.apiLevel >= 37 -> "✅ 兼容当前设备"
                    state.apiLevel >= 35 -> "⚠️ 部分功能可用（需要 API 37）"
                    else -> "❌ 需要 Android 17 (API 37)"
                }
                Text(compat, style = MaterialTheme.typography.labelSmall, color = when {
                    state.apiLevel >= 37 -> Color(0xFF4CAF50)
                    state.apiLevel >= 35 -> Color(0xFFFF9800)
                    else -> Color(0xFFB3261E)
                })
            }
        }
    }
}

@Composable
private fun HandoffCapabilityCard(isAvailable: Boolean, androidVersion: Int, apiLevel: Int, isScanning: Boolean, onStartScan: () -> Unit, onStopScan: () -> Unit) {
    val bgColor = if (isAvailable) Color(0xFF4CAF50).copy(alpha = 0.1f) else Color(0xFFB3261E).copy(alpha = 0.1f)
    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = bgColor), shape = RoundedCornerShape(16.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(if (isAvailable) Color(0xFF4CAF50) else Color(0xFFB3261E)))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (isAvailable) "Cross-Device Handoff 可用" else "Handoff 不可用", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Android $androidVersion / API $apiLevel", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    if (!isAvailable) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("需要 Android 17 (API 37) 才能使用 Cross-Device Handoff", style = MaterialTheme.typography.bodySmall, color = Color(0xFFB3261E))
                    }
                }
                if (isScanning) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        val infiniteTransition = rememberInfiniteTransition(label = "scan")
                        val alpha by infiniteTransition.animateFloat(0.3f, 1.0f, infiniteRepeatable(tween(600, easing = LinearEasing), RepeatMode.Reverse), label = "scan_alpha")
                        IconButton(onClick = onStopScan, modifier = Modifier.size(48.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = alpha))) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimaryContainer)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("扫描中...", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                    }
                } else {
                    IconButton(onClick = onStartScan, modifier = Modifier.size(48.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer)) {
                        Icon(Icons.Default.Bluetooth, "扫描设备", tint = MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                }
            }
        }
    }
}

@Composable
private fun FeatureEntryCard(feature: HandoffFeature, onClick: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().height(140.dp).clickable(onClick = onClick), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant), shape = RoundedCornerShape(12.dp)) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                Box(modifier = Modifier.size(40.dp).clip(RoundedCornerShape(8.dp)).background(MaterialTheme.colorScheme.primaryContainer), contentAlignment = Alignment.Center) {
                    Icon(getFeatureIcon(feature.iconName), null, tint = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.size(24.dp))
                }
                if (feature.isNew) { Badge(containerColor = Color(0xFF4CAF50)) { Text("NEW", fontSize = 9.sp, fontWeight = FontWeight.Bold) } }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(feature.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(feature.titleEn, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(4.dp))
            Text(feature.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2, overflow = TextOverflow.Ellipsis, lineHeight = 14.sp)
            Spacer(modifier = Modifier.weight(1f))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.AutoMirrored.Filled.ArrowForward, "进入", modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@Composable
private fun PairedDeviceItem(device: PairedDevice, onRemove: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)), shape = RoundedCornerShape(8.dp)) {
        Row(modifier = Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(device.type.emoji, fontSize = 24.sp)
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(device.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(if (device.isOnline) Color(0xFF4CAF50) else Color(0xFF9E9E9E)))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (device.isOnline) "在线" else "离线", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        if (device.lastHandoffTime > 0) { Text(" · ${formatRelativeTime(device.lastHandoffTime)}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                    }
                }
            }
            IconButton(onClick = onRemove) { Icon(Icons.Default.Add, "移除设备", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(20.dp)) }
        }
    }
}

@Composable
private fun HandoffRecordItem(record: HandoffRecord) {
    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)), shape = RoundedCornerShape(8.dp)) {
        Row(modifier = Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(record.appPackage, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, fontFamily = FontFamily.Monospace, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(record.activityName, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontFamily = FontFamily.Monospace, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Spacer(modifier = Modifier.height(4.dp))
                Text(formatRelativeTime(record.timestamp), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(record.status.displayName, style = MaterialTheme.typography.labelMedium, color = record.status.color, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Text(formatBytes(record.dataSizeBytes), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun EmptyStateCard(message: String) {
    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)), shape = RoundedCornerShape(8.dp)) {
        Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
            Text(message, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

private fun getFeatureIcon(iconName: String): ImageVector = when (iconName) {
    "Code" -> Icons.Default.Code; "Analytics" -> Icons.Default.Analytics; "SwapVert" -> Icons.Default.SwapVert
    "Bluetooth" -> Icons.Default.Bluetooth; "DesignServices" -> Icons.Default.DesignServices; "BugReport" -> Icons.Default.BugReport
    else -> Icons.Default.Code
}

private fun formatRelativeTime(timestamp: Long): String {
    val diff = System.currentTimeMillis() - timestamp
    return when {
        diff < 60000 -> "刚刚"; diff < 3600000 -> "${diff / 60000}分钟前"
        diff < 86400000 -> "${diff / 3600000}小时前"; diff < 604800000 -> "${diff / 86400000}天前"
        else -> SimpleDateFormat("MM-dd", Locale.getDefault()).format(Date(timestamp))
    }
}

private fun formatBytes(bytes: Int): String = when {
    bytes < 1024 -> "$bytes B"; bytes < 1024 * 1024 -> "${bytes / 1024} KB"
    else -> "${bytes / (1024 * 1024)} MB"
}
