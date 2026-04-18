package com.mvi.kenny.feature.ottermcp

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * DeviceInteractionScreen — stub for OtterMCP device interaction
 * 占位符实现
 */
@Composable
fun DeviceInteractionScreen(viewModel: DeviceInteractionViewModel) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Device Interaction / 设备交互", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "Otter MCP Server device management / Otter MCP 服务器设备管理",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// Stub ViewModel
class DeviceInteractionViewModel
