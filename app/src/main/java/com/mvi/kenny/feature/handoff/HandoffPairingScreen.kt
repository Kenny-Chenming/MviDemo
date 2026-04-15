package com.mvi.kenny.feature.handoff

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Link
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * ============================================================
 * HandoffPairingScreen — 设备发现与配对
 * ============================================================
 * 设备发现、配对流程、Web 降级配置
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HandoffPairingScreen(
    onNavigateBack: () -> Unit,
    viewModel: HandoffViewModel
) {
    val state by viewModel.pairingState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("设备配对") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
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

            // Pairing Steps / 配对步骤
            item {
                PairingStepsCard(
                    currentStep = state.currentStep,
                    isDiscovering = state.isDiscovering,
                    onStartDiscovery = { viewModel.sendIntent(HandoffIntent.StartDiscovery) },
                    onStopDiscovery = { viewModel.sendIntent(HandoffIntent.StopDiscovery) }
                )
            }

            // Discovered Devices / 发现的设备
            if (state.discoveredDevices.isNotEmpty()) {
                item {
                    Text("发现的设备", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                }
                items(state.discoveredDevices) { device ->
                    DiscoveredDeviceCard(
                        device = device,
                        onSelect = { viewModel.sendIntent(HandoffIntent.SelectDeviceForPairing(device)) }
                    )
                }
            }

            // Selected Device for Pairing / 选中的待配对设备
            if (state.selectedDevice != null) {
                item {
                    PairingConfirmCard(
                        device = state.selectedDevice!!,
                        progress = state.pairingProgress,
                        onConfirm = { viewModel.sendIntent(HandoffIntent.ConfirmPairing(state.selectedDevice!!)) },
                        onCancel = { viewModel.sendIntent(HandoffIntent.CancelPairing) }
                    )
                }
            }

            // Paired Devices / 已配对设备
            item {
                Text("已配对设备 (${state.pairedDevices.size})", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            }

            if (state.pairedDevices.isEmpty()) {
                item {
                    EmptyPairedDevicesState()
                }
            } else {
                items(state.pairedDevices) { device ->
                    PairedDeviceCard(device = device)
                }
            }

            // Web Fallback / Web 降级配置
            item {
                WebFallbackCard(
                    url = state.webFallbackUrl,
                    onUrlChange = { viewModel.sendIntent(HandoffIntent.UpdateWebFallbackUrl(it)) }
                )
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}

/**
 * Pairing Steps Card / 配对步骤卡片
 */
@Composable
private fun PairingStepsCard(
    currentStep: PairingStep,
    isDiscovering: Boolean,
    onStartDiscovery: () -> Unit,
    onStopDiscovery: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("配对流程", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))

            // Steps / 步骤
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                PairingStep.entries.forEach { step ->
                    StepIndicator(
                        step = step,
                        isActive = step.stepNumber <= currentStep.stepNumber,
                        isCurrent = step == currentStep
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action button / 操作按钮
            if (isDiscovering) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ScanningAnimation()
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("正在搜索设备...", style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.width(12.dp))
                    OutlinedButton(onClick = onStopDiscovery) {
                        Text("停止")
                    }
                }
            } else {
                Button(
                    onClick = onStartDiscovery,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Bluetooth, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("开始发现设备")
                }
            }
        }
    }
}

/**
 * Step Indicator / 步骤指示器
 */
@Composable
private fun StepIndicator(
    step: PairingStep,
    isActive: Boolean,
    isCurrent: Boolean
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(
                    when {
                        isCurrent -> HandoffColors.Primary
                        isActive -> HandoffColors.HandoffActive
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            when {
                isActive && !isCurrent -> Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                isCurrent -> Text("${step.stepNumber}", color = Color.White, fontWeight = FontWeight.Bold)
                else -> Text("${step.stepNumber}", color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = step.titleZh,
            style = MaterialTheme.typography.labelSmall,
            color = if (isActive) HandoffColors.Primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
        )
    }
}

/**
 * Scanning Animation / 扫描动画
 */
@Composable
private fun ScanningAnimation() {
    val infiniteTransition = rememberInfiniteTransition(label = "scan")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "scan_scale"
    )
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "scan_alpha"
    )

    Box(contentAlignment = Alignment.Center) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .scale(scale)
                .clip(CircleShape)
                .background(HandoffColors.Primary.copy(alpha = alpha))
        )
        Box(
            modifier = Modifier
                .size(12.dp)
                .clip(CircleShape)
                .background(HandoffColors.Primary)
        )
    }
}

/**
 * Discovered Device Card / 发现的设备卡片
 */
@Composable
private fun DiscoveredDeviceCard(
    device: PairedDevice,
    onSelect: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() },
        colors = CardDefaults.cardColors(containerColor = HandoffColors.Primary.copy(alpha = 0.05f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Devices, contentDescription = null, tint = HandoffColors.Primary, modifier = Modifier.size(32.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(device.deviceName, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                Text(device.deviceType.labelZh, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(HandoffColors.Primary)
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text("配对", color = Color.White, style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}

/**
 * Pairing Confirm Card / 配对确认卡片
 */
@Composable
private fun PairingConfirmCard(
    device: PairedDevice,
    progress: Int,
    onConfirm: () -> Unit,
    onCancel: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = HandoffColors.HandoffActive.copy(alpha = 0.1f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Link, contentDescription = null, tint = HandoffColors.HandoffActive)
                Spacer(modifier = Modifier.width(8.dp))
                Text("确认配对", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text("与 ${device.deviceName} 配对", style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(12.dp))
            if (progress > 0) {
                LinearProgressIndicator(progress = { progress / 100f }, modifier = Modifier.fillMaxWidth())
                Text("$progress%", style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(top = 4.dp))
            } else {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = onCancel, modifier = Modifier.weight(1f)) {
                        Text("取消")
                    }
                    Button(onClick = onConfirm, modifier = Modifier.weight(1f)) {
                        Text("确认")
                    }
                }
            }
        }
    }
}

/**
 * Paired Device Card / 已配对设备卡片
 */
@Composable
private fun PairedDeviceCard(device: PairedDevice) {
    val dateFormat = SimpleDateFormat("MM-dd HH:mm", Locale.getDefault())

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(HandoffColors.Primary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Devices, contentDescription = null, tint = HandoffColors.Primary)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(device.deviceName, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(if (device.isOnline) HandoffColors.HandoffActive else HandoffColors.HandoffInactive)
                    )
                }
                Text(
                    text = if (device.lastHandoffAt != null) "上次传输: ${dateFormat.format(Date(device.lastHandoffAt))}"
                           else "未进行过传输",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }
    }
}

/**
 * Empty Paired Devices State / 空状态
 */
@Composable
private fun EmptyPairedDevicesState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(Icons.Default.Devices, contentDescription = null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f))
        Spacer(modifier = Modifier.height(8.dp))
        Text("暂无已配对设备", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
        Text("开始发现设备进行配对", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
    }
}

/**
 * Web Fallback Card / Web 降级卡片
 */
@Composable
private fun WebFallbackCard(
    url: String,
    onUrlChange: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Web 降级配置", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "当目标设备未安装 App 时，Handoff 可降级到 Web 版本",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(12.dp)
            ) {
                Text(
                    text = url.ifEmpty { "https://your-app.com/handoff" },
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (url.isEmpty()) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                           else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
