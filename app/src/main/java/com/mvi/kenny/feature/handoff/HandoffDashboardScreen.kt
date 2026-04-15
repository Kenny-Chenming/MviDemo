package com.mvi.kenny.feature.handoff

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.DataObject
import androidx.compose.material.icons.filled.DesignServices
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mvi.kenny.base.TopBarConfig
import kotlinx.coroutines.flow.collectLatest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * ============================================================
 * HandoffDashboardScreen — Cross-Device Handoff 主仪表盘
 * ============================================================
 * Handoff feature entry point showing capability overview and feature grid.
 * Handoff 功能入口页面，展示能力概览和功能入口网格。
 */
@Composable
fun HandoffDashboardScreen(
    onUpdateTopBar: (TopBarConfig) -> Unit,
    onNavigateToScreen: (HandoffScreen) -> Unit,
    viewModel: HandoffViewModel = viewModel()
) {
    val state by viewModel.dashboardState.collectAsState()

    LaunchedEffect(Unit) {
        onUpdateTopBar(TopBarConfig(title = "Android 17 Handoff · 跨设备传输"))
    }

    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is HandoffEffect.NavigateTo -> onNavigateToScreen(effect.screen)
                else -> {}
            }
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Handoff Capability Overview Card / Handoff 能力概览卡片
        item {
            HandoffCapabilityCard(
                isAvailable = state.isHandoffAvailable,
                pairedDevicesCount = state.pairedDevices.size,
                recentHandoffsCount = state.recentHandoffs.size,
                isScanning = state.isScanning,
                onStartScan = { viewModel.sendIntent(HandoffIntent.StartDeviceScan) },
                onStopScan = { viewModel.sendIntent(HandoffIntent.StopDeviceScan) }
            )
        }

        // Feature Grid Title / 功能网格标题
        item {
            Text(
                text = "功能工具",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        // Feature Grid / 功能网格
        item {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.height(400.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(state.allFeatures) { feature ->
                    FunctionEntryCard(
                        feature = feature,
                        onClick = { viewModel.sendIntent(HandoffIntent.NavigateToFeature(feature)) }
                    )
                }
            }
        }

        // Recent Handoffs / 最近 Handoff 记录
        if (state.recentHandoffs.isNotEmpty()) {
            item {
                Text(
                    text = "最近传输",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            items(state.recentHandoffs.take(3)) { record ->
                HandoffRecordItem(record = record)
            }
        }

        // Paired Devices / 已配对设备
        if (state.pairedDevices.isNotEmpty()) {
            item {
                Text(
                    text = "已配对设备",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            items(state.pairedDevices) { device ->
                PairedDeviceItem(
                    device = device,
                    onRemove = { viewModel.sendIntent(HandoffIntent.RemovePairedDevice(device.deviceId)) }
                )
            }
        }

        // Bottom spacing / 底部间距
        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}

/**
 * ============================================================
 * HandoffCapabilityCard — Handoff 能力概览卡片
 * ============================================================
 */
@Composable
private fun HandoffCapabilityCard(
    isAvailable: Boolean,
    pairedDevicesCount: Int,
    recentHandoffsCount: Int,
    isScanning: Boolean,
    onStartScan: () -> Unit,
    onStopScan: () -> Unit
) {
    val statusColor by animateColorAsState(
        targetValue = if (isAvailable) HandoffColors.HandoffActive else HandoffColors.HandoffInactive,
        label = "status_color"
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Status indicator / 状态指示器
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(statusColor)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isAvailable) "Handoff 可用" else "Handoff 不可用",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Stats row / 统计行
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(label = "配对设备", value = pairedDevicesCount.toString())
                StatItem(label = "最近传输", value = recentHandoffsCount.toString())
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Scan button / 扫描按钮
            if (isScanning) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("扫描中...", style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.weight(1f))
                    Surface(
                        modifier = Modifier.clickable { onStopScan() },
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.error
                    ) {
                        Text(
                            "停止",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            color = MaterialTheme.colorScheme.onError,
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }
            } else {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onStartScan() },
                    shape = RoundedCornerShape(8.dp),
                    color = HandoffColors.Primary
                ) {
                    Text(
                        "扫描设备",
                        modifier = Modifier.padding(vertical = 10.dp),
                        color = Color.White,
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
    }
}

/**
 * Stat Item / 统计项
 */
@Composable
private fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = HandoffColors.Primary
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
        )
    }
}

/**
 * ============================================================
 * FunctionEntryCard — 功能入口卡片
 * ============================================================
 */
@Composable
private fun FunctionEntryCard(
    feature: HandoffFeature,
    onClick: () -> Unit
) {
    val icon = getIconForFeature(feature.iconName)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = feature.isAvailable) { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (feature.isAvailable)
                MaterialTheme.colorScheme.surface
            else
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Icon / 图标
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (feature.isAvailable) HandoffColors.Primary.copy(alpha = 0.1f)
                        else Color.Gray.copy(alpha = 0.1f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = feature.title,
                    tint = if (feature.isAvailable) HandoffColors.Primary else Color.Gray,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Title / 标题
            Text(
                text = feature.title,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            // Description / 描述
            Text(
                text = feature.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

/**
 * ============================================================
 * HandoffRecordItem — Handoff 记录项
 * ============================================================
 */
@Composable
private fun HandoffRecordItem(record: HandoffRecord) {
    val dateFormat = SimpleDateFormat("MM-dd HH:mm", Locale.getDefault())

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Status icon / 状态图标
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(
                        when (record.status) {
                            HandoffStatus.SUCCESS -> HandoffColors.HandoffActive.copy(alpha = 0.1f)
                            HandoffStatus.FAILED -> HandoffColors.Error.copy(alpha = 0.1f)
                            else -> HandoffColors.Primary.copy(alpha = 0.1f)
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = when (record.status) {
                        HandoffStatus.SUCCESS -> "✓"
                        HandoffStatus.FAILED -> "✗"
                        HandoffStatus.PENDING -> "..."
                        HandoffStatus.CANCELLED -> "○"
                    },
                    color = when (record.status) {
                        HandoffStatus.SUCCESS -> HandoffColors.HandoffActive
                        HandoffStatus.FAILED -> HandoffColors.Error
                        else -> HandoffColors.Primary
                    }
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = record.activityName,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "→ ${record.targetDevice}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }

            Text(
                text = dateFormat.format(Date(record.timestamp)),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
        }
    }
}

/**
 * ============================================================
 * PairedDeviceItem — 配对设备项
 * ============================================================
 */
@Composable
private fun PairedDeviceItem(
    device: PairedDevice,
    onRemove: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Device icon / 设备图标
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(HandoffColors.Primary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Devices,
                    contentDescription = null,
                    tint = HandoffColors.Primary,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = device.deviceName,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    // Online status / 在线状态
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(if (device.isOnline) HandoffColors.HandoffActive else HandoffColors.HandoffInactive)
                    )
                }
                Text(
                    text = device.deviceType.labelZh,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }

            // Remove button / 移除按钮
            Surface(
                modifier = Modifier.clickable { onRemove() },
                shape = RoundedCornerShape(6.dp),
                color = MaterialTheme.colorScheme.errorContainer
            ) {
                Text(
                    "移除",
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}

/**
 * Get icon for feature / 获取功能图标
 */
private fun getIconForFeature(iconName: String): ImageVector {
    return when (iconName) {
        "Share" -> Icons.Default.Share
        "Analytics" -> Icons.Default.Analytics
        "DataObject" -> Icons.Default.DataObject
        "Devices" -> Icons.Default.Devices
        "DesignServices" -> Icons.Default.DesignServices
        "BugReport" -> Icons.Default.BugReport
        else -> Icons.Default.Share
    }
}
