package com.mvi.kenny.feature.ottermcp

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
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.Input
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ScreenshotMonitor
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DeviceInteractionScreen(viewModel: DeviceInteractionViewModel) {
    val uiState by viewModel.state.collectAsState()

    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Connected Devices", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.primary)

        if (uiState.connectedDevices.isEmpty()) {
            EmptyDevicesCard({ viewModel.sendIntent(DeviceInteractionIntent.RefreshDevices) })
        } else {
            Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                uiState.connectedDevices.forEach { d ->
                    DeviceCard(d, uiState.selectedDevice?.id == d.id, { viewModel.sendIntent(DeviceInteractionIntent.SelectDevice(d)) })
                }
            }
        }

        if (uiState.selectedDevice != null) {
            Text("Operations - ${uiState.selectedDevice!!.name}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.primary)
            OperationPanel(
                uiState.selectedDevice!!,
                uiState.isLoadingScreenshot,
                { viewModel.sendIntent(DeviceInteractionIntent.TakeScreenshot) },
                { viewModel.sendIntent(DeviceInteractionIntent.ReadLogcat) },
                { cmd -> viewModel.sendIntent(DeviceInteractionIntent.SimulateInput(cmd)) }
            )
        }

        if (uiState.recentScreenshots.isNotEmpty()) {
            Text("Recent Screenshots", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.primary)
            Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                uiState.recentScreenshots.take(5).forEach { ScreenshotPreview(it) }
            }
        }

        Text("Operation Log", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.primary)

        if (uiState.operationLog.isEmpty()) {
            EmptyLogCard()
        } else {
            LazyColumn(Modifier.fillMaxWidth().height(200.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(uiState.operationLog) { OperationLogItem(it) }
            }
        }

        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun DeviceCard(device: AndroidDevice, isSelected: Boolean, onClick: () -> Unit) {
    val connectedColor = if (device.isConnected) Color(0xFF4CAF50) else Color(0xFF9E9E9E)

    Card(
        Modifier.width(140.dp).clickable(onClick = onClick).then(if (isSelected) Modifier.border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(12.dp)) else Modifier),
        colors = CardDefaults.cardColors(containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f) else MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(Modifier.fillMaxWidth().padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.PhoneAndroid, device.name, Modifier.size(40.dp), tint = connectedColor)
            Spacer(Modifier.height(8.dp))
            Text(device.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, maxLines = 1)
            Text(device.model, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
            Spacer(Modifier.height(4.dp))
            Box(Modifier.clip(RoundedCornerShape(4.dp)).background(connectedColor.copy(alpha = 0.2f)).padding(horizontal = 8.dp, vertical = 2.dp)) {
                Text(if (device.isConnected) "Connected" else "Offline", style = MaterialTheme.typography.labelSmall, color = connectedColor)
            }
        }
    }
}

@Composable
private fun EmptyDevicesCard(onRefresh: () -> Unit) {
    Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)), shape = RoundedCornerShape(12.dp)) {
        Column(Modifier.fillMaxWidth().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.PhoneAndroid, null, Modifier.size(48.dp), tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f))
            Spacer(Modifier.height(8.dp))
            Text("No devices connected", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
            Spacer(Modifier.height(12.dp))
            OutlinedButton(onRefresh) {
                Icon(Icons.Default.Refresh, null, Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("Scan Devices")
            }
        }
    }
}

@Composable
private fun OperationPanel(device: AndroidDevice, isLoading: Boolean, onScreenshot: () -> Unit, onLogcat: () -> Unit, onInput: (String) -> Unit) {
    var inputCmd by remember { mutableStateOf("") }

    Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant), shape = RoundedCornerShape(12.dp)) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(device.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Medium)
                Text(device.model, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f)), shape = RoundedCornerShape(8.dp)) {
                Text("Note: ADB operations require host machine to have adb in PATH.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onTertiaryContainer, modifier = Modifier.padding(8.dp))
            }

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onScreenshot, Modifier.weight(1f), enabled = !isLoading, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3))) {
                    if (isLoading) CircularProgressIndicator(Modifier.size(16.dp), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp) else Icon(Icons.Default.ScreenshotMonitor, null, Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Screenshot")
                }
                OutlinedButton(onLogcat, Modifier.weight(1f)) {
                    Icon(Icons.Default.Computer, null, Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Logcat")
                }
            }

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(inputCmd, { inputCmd = it }, Modifier.weight(1f), singleLine = true, placeholder = { Text("Input command") })
                Button({ if (inputCmd.isNotBlank()) { onInput(inputCmd); inputCmd = "" } }, enabled = inputCmd.isNotBlank()) {
                    Icon(Icons.Default.Input, null, Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Send")
                }
            }
        }
    }
}

@Composable
private fun ScreenshotPreview(screenshot: Screenshot) {
    Card(Modifier.size(100.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant), shape = RoundedCornerShape(8.dp)) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.ScreenshotMonitor, null, Modifier.size(32.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(4.dp))
                Text(formatTimeShort(screenshot.timestamp), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun OperationLogItem(operation: DeviceOperation) {
    val iconColor = when (operation.type) {
        OperationType.SCREENSHOT -> Color(0xFF2196F3)
        OperationType.INSTALL -> Color(0xFF4CAF50)
        OperationType.LOGCAT -> Color(0xFF9C27B0)
        OperationType.INPUT -> Color(0xFFFF9800)
    }

    Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), shape = RoundedCornerShape(8.dp)) {
        Row(Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
            Row(Modifier.weight(1f)) {
                Icon(getOpIcon(operation.type), operation.type.name, Modifier.size(16.dp), iconColor)
                Spacer(Modifier.width(8.dp))
                Column {
                    Text(operation.type.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                    Text(operation.params, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
                }
            }
            Text(formatTimeShort(operation.timestamp), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun EmptyLogCard() {
    Card(Modifier.fillMaxWidth().height(100.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)), shape = RoundedCornerShape(8.dp)) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No operations yet", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
        }
    }
}

private fun getOpIcon(type: OperationType): ImageVector = when (type) {
    OperationType.SCREENSHOT -> Icons.Default.ScreenshotMonitor
    OperationType.INSTALL -> Icons.Default.PhoneAndroid
    OperationType.LOGCAT -> Icons.Default.Computer
    OperationType.INPUT -> Icons.Default.Input
}

private fun formatTimeShort(ts: Long) = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(ts))
