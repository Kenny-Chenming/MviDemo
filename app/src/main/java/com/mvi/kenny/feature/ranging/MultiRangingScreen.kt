package com.mvi.kenny.feature.ranging

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DeviceHub
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// =============================================================
// MultiRangingScreen — 多设备协同测距页面
// UWB Ranging API 开发工具包 - 多设备测距 Tab
// =============================================================
/**
 * Multi-Ranging Screen / 多设备协同测距页面
 *
 * Displays and manages simultaneous ranging sessions with multiple UWB devices.
 *
 * Features:
 * - View all active ranging sessions / 查看所有活跃测距会话
 * - Add/remove devices from multi-ranging / 添加/移除测距设备
 * - Real-time distance for each device / 每个设备的实时距离
 * - Batch stop all sessions / 批量停止所有会话
 * - Max concurrent limit indicator / 最大并发限制指示器
 *
 * @param state Current UI state / 当前 UI 状态
 * @param onIntent Send intent to ViewModel / 发送意图到 ViewModel
 *
 * @see RangingDashboardState Full UI state
 * @see RangingIntent User intents
 */
@Composable
fun MultiRangingScreen(
    state: RangingDashboardState,
    onIntent: (RangingIntent) -> Unit
) {
    val rangingDevices = state.rangingDevices
    val maxRanging = RangingViewModel.MAX_CONCURRENT_RANGING
    val currentCount = rangingDevices.size

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // ============================================================
        // Header / 头部
        // ============================================================
        MultiRangingHeader(
            activeCount = currentCount,
            maxCount = maxRanging,
            onStopAll = { onIntent(RangingIntent.StopAllRanging) }
        )

        // ============================================================
        // Content / 内容区
        // ============================================================
        if (rangingDevices.isEmpty()) {
            // Empty state / 空状态
            EmptyMultiRangingContent(
                onStartDiscovery = { onIntent(RangingIntent.StartDiscovery) }
            )
        } else {
            // Multi-ranging device list / 多设备测距列表
            MultiRangingDeviceList(
                devices = rangingDevices,
                onStopRanging = { device -> onIntent(RangingIntent.StopRanging(device.address)) },
                onNavigateToDetail = { device -> onIntent(RangingIntent.SelectDevice(device.address)) }
            )
        }

        // ============================================================
        // Add Device Section / 添加设备区域
        // ============================================================
        if (state.devices.isNotEmpty() && currentCount < maxRanging) {
            AddDeviceSection(
                availableDevices = state.devices.filter { !state.isRanging(it.address) },
                onStartRanging = { device -> onIntent(RangingIntent.StartRanging(device.address)) }
            )
        }
    }
}

// =============================================================
// MultiRangingHeader — 多设备测距头部
// =============================================================
/**
 * Multi-ranging header with session count and controls / 多设备测距头部（会话计数和控制）
 */
@Composable
private fun MultiRangingHeader(
    activeCount: Int,
    maxCount: Int,
    onStopAll: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Rounded.DeviceHub,
                    contentDescription = null,
                    tint = Color(0xFF4FC3F7),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "多设备测距 / Multi-Ranging",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "$activeCount / $maxCount 活跃会话 / active sessions",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Progress indicator / 进度指示器
            ConcurrentProgressBar(
                current = activeCount,
                max = maxCount
            )

            // Stop all button / 全部停止按钮
            if (activeCount > 0) {
                Button(
                    onClick = onStopAll,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    ),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Stop,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("全部停止 / Stop All", fontSize = 12.sp)
                }
            }
        }
    }
}

// =============================================================
// ConcurrentProgressBar — 并发进度条
// =============================================================
/**
 * Concurrent ranging progress bar / 并发测距进度条
 */
@Composable
private fun ConcurrentProgressBar(
    current: Int,
    max: Int
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(horizontal = 8.dp)
    ) {
        Text(
            text = "$current/$max",
            style = MaterialTheme.typography.labelSmall,
            fontFamily = FontFamily.Monospace,
            color = if (current >= max) Color(0xFFFF6B6B) else Color(0xFF69F0AE)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .width(60.dp)
                .height(6.dp)
                .background(
                    Color.Gray.copy(alpha = 0.3f),
                    RoundedCornerShape(3.dp)
                )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(current.toFloat() / max.toFloat())
                    .height(6.dp)
                    .background(
                        if (current >= max) Color(0xFFFF6B6B) else Color(0xFF69F0AE),
                        RoundedCornerShape(3.dp)
                    )
            )
        }
    }
}

// =============================================================
// EmptyMultiRangingContent — 多设备测距空状态
// =============================================================
/**
 * Empty state for multi-ranging / 多设备测距空状态
 */
@Composable
private fun EmptyMultiRangingContent(
    onStartDiscovery: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Rounded.DeviceHub,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = Color(0xFF4FC3F7).copy(alpha = 0.5f)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "暂无活跃测距会话",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "No active ranging sessions",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "从设备发现页面选择设备开始测距",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onStartDiscovery,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4FC3F7))
        ) {
            Icon(Icons.Rounded.PlayArrow, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("开始发现设备 / Start Discovery")
        }
    }
}

// =============================================================
// MultiRangingDeviceList — 多设备测距列表
// =============================================================
/**
 * Multi-ranging device list / 多设备测距列表
 */
@Composable
private fun MultiRangingDeviceList(
    devices: List<UwbDevice>,
    onStopRanging: (UwbDevice) -> Unit,
    onNavigateToDetail: (UwbDevice) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(
            items = devices,
            key = { it.address }
        ) { device ->
            MultiRangingDeviceCard(
                device = device,
                onStopRanging = { onStopRanging(device) },
                onClick = { onNavigateToDetail(device) }
            )
        }
    }
}

// =============================================================
// MultiRangingDeviceCard — 多设备测距设备卡片
// =============================================================
/**
 * Multi-ranging device card / 多设备测距设备卡片
 */
@Composable
private fun MultiRangingDeviceCard(
    device: UwbDevice,
    onStopRanging: () -> Unit,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1E3A2F)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Device index indicator / 设备序号指示器
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            Color(0xFF4FC3F7).copy(alpha = 0.2f),
                            RoundedCornerShape(12.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "#${device.address.takeLast(2)}",
                        style = MaterialTheme.typography.labelMedium,
                        fontFamily = FontFamily.Monospace,
                        color = Color(0xFF4FC3F7)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = device.displayName ?: device.address,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = device.address,
                        style = MaterialTheme.typography.labelSmall,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Distance display / 距离显示
            Column(horizontalAlignment = Alignment.End) {
                if (device.distanceMeters != null) {
                    Text(
                        text = String.format("%.2f m", device.distanceMeters),
                        style = MaterialTheme.typography.headlineSmall,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF69F0AE)
                    )
                    if (device.azimuth != null) {
                        Text(
                            text = String.format("Az: %.0f°", device.azimuth),
                            style = MaterialTheme.typography.labelSmall,
                            fontFamily = FontFamily.Monospace,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    Text(
                        text = "-- m",
                        style = MaterialTheme.typography.headlineSmall,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Stop button / 停止按钮
            IconButton(
                onClick = onStopRanging,
                modifier = Modifier
                    .size(36.dp)
                    .background(
                        MaterialTheme.colorScheme.error.copy(alpha = 0.2f),
                        RoundedCornerShape(10.dp)
                    )
            ) {
                Icon(
                    imageVector = Icons.Rounded.Close,
                    contentDescription = "Stop / 停止",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

// =============================================================
// AddDeviceSection — 添加设备区域
// =============================================================
/**
 * Section to add more devices to multi-ranging / 添加更多设备到多设备测距的区域
 */
@Composable
private fun AddDeviceSection(
    availableDevices: List<UwbDevice>,
    onStartRanging: (UwbDevice) -> Unit
) {
    if (availableDevices.isEmpty()) return

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Rounded.Add,
                    contentDescription = null,
                    tint = Color(0xFF4FC3F7),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "添加更多设备 / Add More Devices",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Available device chips / 可用设备标签
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                availableDevices.take(4).forEach { device ->
                    AvailableDeviceChip(
                        device = device,
                        onClick = { onStartRanging(device) }
                    )
                }
            }
        }
    }
}

// =============================================================
// AvailableDeviceChip — 可用设备标签
// =============================================================
/**
 * Chip for available device to add / 可用设备添加标签
 */
@Composable
private fun AvailableDeviceChip(
    device: UwbDevice,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF4FC3F7).copy(alpha = 0.2f)
        ),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
        shape = RoundedCornerShape(20.dp)
    ) {
        Icon(
            imageVector = Icons.Rounded.Add,
            contentDescription = null,
            modifier = Modifier.size(14.dp),
            tint = Color(0xFF4FC3F7)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = device.displayInfo,
            style = MaterialTheme.typography.labelSmall,
            color = Color(0xFF4FC3F7)
        )
    }
}
