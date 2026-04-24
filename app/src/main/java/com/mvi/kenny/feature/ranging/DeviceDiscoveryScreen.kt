package com.mvi.kenny.feature.ranging

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DeviceHub
import androidx.compose.material.icons.rounded.NearMe
import androidx.compose.material.icons.rounded.SignalWifi4Bar
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// =============================================================
// DeviceDiscoveryScreen — 设备发现列表页面
// UWB Ranging API 开发工具包 - 设备发现 Tab
// =============================================================
/**
 * Device Discovery Screen / 设备发现页面
 *
 * Displays all discovered UWB devices in the vicinity with their
 * signal strength and connection status.
 *
 * Features:
 * - Pull-to-refresh for device discovery / 下拉刷新设备发现
 * - Real-time RSSI indicator / 实时 RSSI 指示器
 * - Tap to start ranging / 点击开始测距
 * - Long-press for device options / 长按显示设备选项
 *
 * @param state Current UI state / 当前 UI 状态
 * @param onIntent Send intent to ViewModel / 发送意图到 ViewModel
 *
 * @see RangingDashboardState Full UI state
 * @see RangingIntent User intents
 */
@Composable
fun DeviceDiscoveryScreen(
    state: RangingDashboardState,
    onIntent: (RangingIntent) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // ============================================================
        // Header Status Bar / 头部状态栏
        // ============================================================
        DiscoveryHeader(
            isUwbAvailable = state.isUwbAvailable,
            isDiscovering = state.isDiscovering,
            deviceCount = state.devices.size,
            onStartDiscovery = { onIntent(RangingIntent.StartDiscovery) },
            onStopDiscovery = { onIntent(RangingIntent.StopDiscovery) }
        )

        // ============================================================
        // Content / 内容区
        // ============================================================
        when {
            // UWB not available state / UWB 不可用状态
            !state.isUwbAvailable -> {
                UwbNotAvailableContent()
            }

            // Empty state / 空状态
            state.devices.isEmpty() && !state.isDiscovering -> {
                EmptyDiscoveryContent(
                    onStartDiscovery = { onIntent(RangingIntent.StartDiscovery) }
                )
            }

            // Device list / 设备列表
            else -> {
                DeviceList(
                    devices = state.devices,
                    activeRangings = state.activeRangings,
                    onDeviceClick = { device ->
                        if (state.isRanging(device.address)) {
                            onIntent(RangingIntent.SelectDevice(device.address))
                        } else {
                            onIntent(RangingIntent.StartRanging(device.address))
                        }
                    },
                    onStopRanging = { device -> onIntent(RangingIntent.StopRanging(device.address)) },
                    onNavigateToDetail = { device -> onIntent(RangingIntent.SelectDevice(device.address)) }
                )
            }
        }
    }
}

// =============================================================
// DiscoveryHeader — 发现状态头部
// =============================================================
/**
 * Discovery header with status and controls / 带状态和控制的发现头部
 */
@Composable
private fun DiscoveryHeader(
    isUwbAvailable: Boolean,
    isDiscovering: Boolean,
    deviceCount: Int,
    onStartDiscovery: () -> Unit,
    onStopDiscovery: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isUwbAvailable) {
                MaterialTheme.colorScheme.surfaceVariant
            } else {
                MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
            }
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
                // Status indicator / 状态指示器
                PulsingIndicator(
                    isActive = isUwbAvailable && isDiscovering,
                    activeColor = Color(0xFF4FC3F7),
                    inactiveColor = if (isUwbAvailable) Color(0xFF69F0AE) else Color(0xFFFF6B6B)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = if (isUwbAvailable) {
                            if (isDiscovering) "扫描中... / Scanning..."
                            else "UWB 可用 / UWB Available"
                        } else {
                            "UWB 不可用 / UWB Unavailable"
                        },
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (deviceCount > 0) "发现 $deviceCount 个设备 / $deviceCount devices found" else "点击开始发现 / Tap to discover",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Discovery button / 发现按钮
            Button(
                onClick = if (isDiscovering) onStopDiscovery else onStartDiscovery,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isDiscovering) {
                        MaterialTheme.colorScheme.error
                    } else {
                        Color(0xFF4FC3F7)
                    }
                ),
                enabled = isUwbAvailable
            ) {
                if (isDiscovering) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("停止 / Stop", fontSize = 12.sp)
                } else {
                    Icon(
                        imageVector = Icons.Rounded.NearMe,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("发现 / Scan", fontSize = 12.sp)
                }
            }
        }
    }
}

// =============================================================
// PulsingIndicator — 脉冲指示器
// =============================================================
/**
 * Pulsing status indicator / 脉冲状态指示器
 *
 * @param isActive Whether the indicator is active (pulsing) / 是否活跃（脉冲动画）
 * @param activeColor Color when active / 活跃时颜色
 * @param inactiveColor Color when inactive / 非活跃时颜色
 */
@Composable
private fun PulsingIndicator(
    isActive: Boolean,
    activeColor: Color,
    inactiveColor: Color
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    Box(
        modifier = Modifier
            .size(12.dp)
            .clip(CircleShape)
            .background(if (isActive) activeColor.copy(alpha = alpha) else inactiveColor)
    )
}

// =============================================================
// EmptyDiscoveryContent — 空状态内容
// =============================================================
/**
 * Empty state content when no devices found / 未发现设备时的空状态
 */
@Composable
private fun EmptyDiscoveryContent(
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
            text = "未发现 UWB 设备",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "No UWB devices found",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "确保周围有 UWB 设备并尝试刷新",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onStartDiscovery,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4FC3F7))
        ) {
            Icon(Icons.Rounded.NearMe, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("开始发现 / Start Discovery")
        }
    }
}

// =============================================================
// UwbNotAvailableContent — UWB 不可用内容
// =============================================================
/**
 * Content shown when UWB hardware is not available / UWB 硬件不可用时显示的内容
 */
@Composable
private fun UwbNotAvailableContent() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Rounded.Warning,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = Color(0xFFFF6B6B).copy(alpha = 0.7f)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "当前设备不支持 UWB",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "UWB Not Available on This Device",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "UWB 需要 Android 17+ 设备和硬件支持",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// =============================================================
// DeviceList — 设备列表
// =============================================================
/**
 * Device list with animated items / 带动画的设备列表
 */
@Composable
private fun DeviceList(
    devices: List<UwbDevice>,
    activeRangings: Map<String, RangingStatus>,
    onDeviceClick: (UwbDevice) -> Unit,
    onStopRanging: (UwbDevice) -> Unit,
    onNavigateToDetail: (UwbDevice) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        itemsIndexed(
            items = devices,
            key = { _, device -> device.address }
        ) { index, device ->
            AnimatedVisibility(
                visible = true,
                enter = slideInVertically(
                    initialOffsetY = { -it },
                    animationSpec = tween(300, delayMillis = index * 50)
                ) + fadeIn(animationSpec = tween(300, delayMillis = index * 50))
            ) {
                DeviceCard(
                    device = device,
                    status = activeRangings[device.address] ?: RangingStatus.IDLE,
                    onClick = { onDeviceClick(device) },
                    onStopRanging = { onStopRanging(device) },
                    onLongClick = { onNavigateToDetail(device) }
                )
            }
        }
    }
}

// =============================================================
// DeviceCard — 设备卡片
// =============================================================
/**
 * Device card component / 设备卡片组件
 *
 * @param device UWB device info / UWB 设备信息
 * @param status Ranging status / 测距状态
 * @param onClick Card click handler / 卡片点击处理
 * @param onStopRanging Stop ranging handler / 停止测距处理
 * @param onLongClick Long press handler / 长按处理
 */
@Composable
fun DeviceCard(
    device: UwbDevice,
    status: RangingStatus,
    onClick: () -> Unit,
    onStopRanging: () -> Unit,
    onLongClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when (status) {
                RangingStatus.RANGING -> Color(0xFF1E3A2F)
                RangingStatus.DISCOVERING -> Color(0xFF3A3520)
                RangingStatus.ERROR -> Color(0xFF3A1E1E)
                RangingStatus.IDLE -> MaterialTheme.colorScheme.surfaceVariant
            }
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Signal strength icon / 信号强度图标
                    Icon(
                        imageVector = Icons.Rounded.SignalWifi4Bar,
                        contentDescription = null,
                        tint = device.signalLevel.color,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
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

                // Status badge / 状态徽章
                StatusBadge(status = status)
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Distance and RSSI info / 距离和 RSSI 信息
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Distance display (if ranging) / 距离显示（如果正在测距）
                if (device.distanceMeters != null && status == RangingStatus.RANGING) {
                    Column {
                        Text(
                            text = "距离 / Distance",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = String.format("%.2f m", device.distanceMeters),
                            style = MaterialTheme.typography.headlineSmall,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF69F0AE)
                        )
                    }
                } else {
                    Column {
                        Text(
                            text = "信号强度 / Signal",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${device.rssi} dBm",
                            style = MaterialTheme.typography.titleMedium,
                            fontFamily = FontFamily.Monospace,
                            color = device.signalLevel.color
                        )
                    }
                }

                // RSSI bar / RSSI 条
                RssiBar(rssi = device.rssi)

                // Action button / 操作按钮
                if (status == RangingStatus.RANGING) {
                    Button(
                        onClick = onStopRanging,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        ),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text("停止 / Stop", fontSize = 12.sp)
                    }
                } else {
                    Button(
                        onClick = onClick,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF4FC3F7)
                        ),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text("测距 / Range", fontSize = 12.sp)
                    }
                }
            }

            // Angle info if available / 角度信息（如可用）
            if (device.azimuth != null && device.elevation != null && status == RangingStatus.RANGING) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Color.Black.copy(alpha = 0.2f),
                            RoundedCornerShape(8.dp)
                        )
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    AngleInfo(label = "方位角 / Azimuth", value = String.format("%.1f°", device.azimuth))
                    AngleInfo(label = "仰角 / Elevation", value = String.format("%.1f°", device.elevation))
                }
            }
        }
    }
}

// =============================================================
// StatusBadge — 状态徽章
// =============================================================
/**
 * Status badge component / 状态徽章组件
 */
@Composable
private fun StatusBadge(status: RangingStatus) {
    Box(
        modifier = Modifier
            .background(
                color = status.color.copy(alpha = 0.2f),
                shape = RoundedCornerShape(8.dp)
            )
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = status.label,
            style = MaterialTheme.typography.labelSmall,
            color = status.color
        )
    }
}

// =============================================================
// RssiBar — RSSI 信号强度条
// =============================================================
/**
 * RSSI bar indicator / RSSI 信号强度条
 */
@Composable
private fun RssiBar(rssi: Int) {
    val level = when {
        rssi >= -60 -> 4
        rssi >= -75 -> 3
        rssi >= -90 -> 2
        else -> 1
    }

    Row(
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        repeat(4) { index ->
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(((index + 1) * 6).dp)
                    .background(
                        if (index < level) {
                            when (level) {
                                4 -> Color(0xFF69F0AE)
                                3 -> Color(0xFF4FC3F7)
                                2 -> Color(0xFFFFD54F)
                                else -> Color(0xFFFF6B6B)
                            }
                        } else {
                            MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                        },
                        RoundedCornerShape(2.dp)
                    )
            )
        }
    }
}

// =============================================================
// AngleInfo — 角度信息显示
// =============================================================
/**
 * Angle info display component / 角度信息显示组件
 */
@Composable
private fun AngleInfo(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold
        )
    }
}
