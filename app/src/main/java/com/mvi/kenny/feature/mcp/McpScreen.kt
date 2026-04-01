package com.mvi.kenny.feature.mcp

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.Vrpano
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mvi.kenny.base.TopBarConfig
import com.mvi.kenny.base.TopBarAction
import kotlinx.coroutines.flow.collect

/**
 * ============================================================
 * McpScreen — Android MCP Server 工具包主屏幕
 * ============================================================
 * PRD-011 | Android MCP Server 工具包
 *
 * 功能面板化 + 深色主题 + 状态驱动的 UI。
 *
 * @param onUpdateTopBar 向 MainScreen 上报 TopBar 配置
 * @see McpViewModel 状态管理
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun McpScreen(
    onUpdateTopBar: (TopBarConfig) -> Unit = {}
) {
    val viewModel: McpViewModel = viewModel()
    val uiState by viewModel.state.collectAsState()

    // 顶部导航栏配置（工具名称 + 刷新按钮）
    LaunchedEffect(Unit) {
        onUpdateTopBar(
            TopBarConfig(
                title = "MCP Server",
                actions = listOf(
                    TopBarAction(
                        icon = Icons.Default.Refresh,
                        contentDescription = "Refresh Devices",
                        onClick = { viewModel.sendIntent(McpIntent.RefreshDevices) }
                    )
                )
            )
        )
    }

    // 收集副作用（Toast 等）
    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is McpEffect.ShowToast -> { /* Toast handled by parent */ }
                is McpEffect.ToolCallResult -> { /* Handled in BottomSheet */ }
                is McpEffect.NavigateToToolDetail -> { /* Handled by BottomSheet */ }
            }
        }
    }

    // 工具详情 BottomSheet
    val sheetState = rememberModalBottomSheetState()
    if (uiState.selectedTool != null) {
        ModalBottomSheet(
            onDismissRequest = { viewModel.sendIntent(McpIntent.DismissToolDetail) },
            sheetState = sheetState
        ) {
            ToolDetailSheet(
                tool = uiState.selectedTool!!,
                isLoading = uiState.isLoading,
                onCallTool = { params ->
                    viewModel.sendIntent(McpIntent.CallTool(uiState.selectedTool!!.id, params))
                }
            )
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "MCP Server",
                        fontWeight = FontWeight.Medium
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF1E1E1E),
                    titleContentColor = Color.White
                ),
                actions = {
                    IconButton(onClick = { viewModel.sendIntent(McpIntent.RefreshDevices) }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh Devices",
                            tint = Color.White
                        )
                    }
                }
            )
        },
        containerColor = Color(0xFF121212)
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { Spacer(modifier = Modifier.height(8.dp)) }

            // ——————————————————————————————————
            // 1. Server Status Card（服务状态卡片）
            // ——————————————————————————————————
            item {
                ServerStatusCard(
                    serverStatus = uiState.serverStatus,
                    serverPort = uiState.serverPort,
                    onToggle = {
                        when (uiState.serverStatus) {
                            ServerStatus.IDLE, ServerStatus.ERROR ->
                                viewModel.sendIntent(McpIntent.StartServer)
                            ServerStatus.RUNNING ->
                                viewModel.sendIntent(McpIntent.StopServer)
                            else -> {} // 启动中/停止中忽略
                        }
                    }
                )
            }

            // ——————————————————————————————————
            // 2. Tools Section（工具列表）
            // ——————————————————————————————————
            item {
                Text(
                    text = "Tools",
                    color = Color(0xFF4FC3F7),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            items(uiState.tools) { tool ->
                ToolListItem(
                    tool = tool,
                    onToggle = { enabled ->
                        viewModel.sendIntent(McpIntent.ToggleTool(tool.id, enabled))
                    },
                    onClick = {
                        viewModel.sendIntent(McpIntent.SelectTool(tool))
                    }
                )
            }

            // ——————————————————————————————————
            // 3. Devices Section（跨设备桥接）
            // ——————————————————————————————————
            item {
                Text(
                    text = "Devices",
                    color = Color(0xFF4FC3F7),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            if (uiState.devices.isEmpty()) {
                item {
                    OutlinedButton(
                        onClick = { viewModel.sendIntent(McpIntent.RefreshDevices) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color(0xFF4FC3F7)
                        )
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Scan Devices")
                    }
                }
            } else {
                items(uiState.devices) { device ->
                    DeviceListItem(device = device)
                }
            }

            // ——————————————————————————————————
            // 4. Connection Log Viewer（日志查看器）
            // ——————————————————————————————————
            item {
                Text(
                    text = "Logs",
                    color = Color(0xFF4FC3F7),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            item {
                ConnectionLogViewer(logs = uiState.connectionLogs)
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}

/**
 * Server Status Card
 * 服务状态卡片：显示运行状态 + 一键启停 Toggle
 */
@Composable
private fun ServerStatusCard(
    serverStatus: ServerStatus,
    serverPort: Int,
    onToggle: () -> Unit
) {
    val statusColor by animateColorAsState(
        targetValue = when (serverStatus) {
            ServerStatus.IDLE -> Color(0xFF9E9E9E)
            ServerStatus.STARTING, ServerStatus.STOPPING -> Color(0xFFFFB74D)
            ServerStatus.RUNNING -> Color(0xFF4CAF50)
            ServerStatus.ERROR -> Color(0xFFEF5350)
        },
        animationSpec = tween(300),
        label = "statusColor"
    )

    val statusText = when (serverStatus) {
        ServerStatus.IDLE -> "Stopped"
        ServerStatus.STARTING -> "Starting..."
        ServerStatus.RUNNING -> "Running"
        ServerStatus.STOPPING -> "Stopping..."
        ServerStatus.ERROR -> "Error"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(statusColor)
                )
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(
                        text = statusText,
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                    if (serverStatus == ServerStatus.RUNNING) {
                        Text(
                            text = "Port: $serverPort",
                            color = Color.White.copy(alpha = 0.6f),
                            fontSize = 12.sp
                        )
                    }
                }
            }

            Button(
                onClick = onToggle,
                enabled = serverStatus != ServerStatus.STARTING && serverStatus != ServerStatus.STOPPING,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (serverStatus == ServerStatus.RUNNING) Color(0xFFEF5350) else Color(0xFF4FC3F7)
                )
            ) {
                if (serverStatus == ServerStatus.STARTING || serverStatus == ServerStatus.STOPPING) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(if (serverStatus == ServerStatus.RUNNING) "Stop" else "Start")
                }
            }
        }
    }
}

/**
 * Tool List Item
 * 工具列表项：支持启用/禁用 + 点击展开详情
 */
@Composable
private fun ToolListItem(
    tool: McpTool,
    onToggle: (Boolean) -> Unit,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = tool.name,
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = tool.description,
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 12.sp,
                    maxLines = 1
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Switch(
                    checked = tool.enabled,
                    onCheckedChange = onToggle,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color(0xFF4FC3F7),
                        checkedTrackColor = Color(0xFF4FC3F7).copy(alpha = 0.4f)
                    )
                )
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "Details",
                    tint = Color.White.copy(alpha = 0.4f),
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }
    }
}

/**
 * Device List Item
 * 跨设备桥接设备列表项
 */
@Composable
private fun DeviceListItem(device: AndroidDevice) {
    val deviceIcon: ImageVector = when (device.type) {
        DeviceType.PHONE -> Icons.Default.PhoneAndroid
        DeviceType.TABLET -> Icons.Default.Computer
        DeviceType.TV -> Icons.Default.Tv
        DeviceType.XR -> Icons.Default.Vrpano
    }

    val connectedColor = if (device.connected) Color(0xFF4CAF50) else Color(0xFF9E9E9E)

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
        shape = RoundedCornerShape(12.dp)
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
                    imageVector = deviceIcon,
                    contentDescription = device.type.name,
                    tint = connectedColor,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    text = device.name,
                    color = Color.White,
                    fontSize = 14.sp
                )
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(connectedColor.copy(alpha = 0.2f))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = if (device.connected) "Connected" else "Offline",
                    color = connectedColor,
                    fontSize = 12.sp
                )
            }
        }
    }
}

/**
 * Connection Log Viewer
 * 实时日志查看器（虚拟滚动，保留最新 100 行）
 */
@Composable
private fun ConnectionLogViewer(logs: List<LogEntry>) {
    val listState = rememberLazyListState()

    // Auto-scroll to bottom when new logs arrive
    LaunchedEffect(logs.size) {
        if (logs.isNotEmpty()) {
            listState.animateScrollToItem(logs.size - 1)
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0D0D0D)),
        shape = RoundedCornerShape(12.dp)
    ) {
        if (logs.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No logs yet",
                    color = Color.White.copy(alpha = 0.4f),
                    fontSize = 12.sp
                )
            }
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp)
            ) {
                items(logs) { entry ->
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = entry.timestamp,
                            color = Color.White.copy(alpha = 0.4f),
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = entry.message,
                            color = if (entry.level == LogLevel.ERROR) Color(0xFFEF5350) else Color(0xFF81C784),
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }
    }
}

/**
 * Tool Detail Bottom Sheet
 * 工具详情底部 Sheet：显示调用历史 + 测试调用入口
 */
@Composable
private fun ToolDetailSheet(
    tool: McpTool,
    isLoading: Boolean,
    onCallTool: (Map<String, String>) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
    ) {
        Text(
            text = tool.name,
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = tool.description,
            color = Color.White.copy(alpha = 0.6f),
            fontSize = 14.sp
        )
        Spacer(Modifier.height(16.dp))

        // Call History（调用历史）
        if (tool.callHistory.isNotEmpty()) {
            Text(
                text = "Recent Calls",
                color = Color(0xFF4FC3F7),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(Modifier.height(8.dp))

            tool.callHistory.take(3).forEach { call ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0D0D0D))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = call.timestamp,
                                color = Color.White.copy(alpha = 0.5f),
                                fontSize = 11.sp
                            )
                            Text(
                                text = if (call.success) "✓ Success" else "✗ Failed",
                                color = if (call.success) Color(0xFF4CAF50) else Color(0xFFEF5350),
                                fontSize = 11.sp
                            )
                        }
                        Text(
                            text = call.rawOutput.take(100) + if (call.rawOutput.length > 100) "..." else "",
                            color = Color(0xFF81C784),
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
        } else {
            Text(
                text = "No call history yet",
                color = Color.White.copy(alpha = 0.4f),
                fontSize = 12.sp
            )
        }

        Spacer(Modifier.height(16.dp))

        // Test Call Button
        Button(
            onClick = { onCallTool(emptyMap()) },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4FC3F7))
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
                Spacer(Modifier.width(8.dp))
            }
            Text(if (isLoading) "Calling..." else "Test Call")
        }

        Spacer(Modifier.height(32.dp))
    }
}
