package com.mvi.kenny.feature.ottermcp

import android.widget.Toast
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.NavigateNext
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Screenshot
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mvi.kenny.feature.ottermcp.TemplateCategory.entries
import kotlinx.coroutines.launch

// ============================================================
// PRD-096 | Android Studio Otter MCP Server 生态接入工具包
// 主界面 — 6 Tab: Dashboard/Wizard/Template/Security/Audit/Device
// ============================================================

private val TAB_ICONS = listOf(
    Icons.Default.Description, Icons.Default.Cloud, Icons.Default.Storage,
    Icons.Default.Security, Icons.Default.Widgets, Icons.Default.PhoneAndroid,
)
private val TAB_LABELS = listOf("仪表盘", "连接向导", "模板库", "安全策略", "审计面板", "设备交互")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OtterMcpScreen(viewModel: OtterMcpViewModel = viewModel()) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val pagerState = rememberPagerState(pageCount = { 6 })

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is OtterMcpEffect.ShowToast -> Toast.makeText(context, effect.message, Toast.LENGTH_SHORT).show()
                is OtterMcpEffect.NavigateToDashboard -> scope.launch { pagerState.animateScrollToPage(0) }
                is OtterMcpEffect.NavigateToTemplateGallery -> scope.launch { pagerState.animateScrollToPage(2) }
                is OtterMcpEffect.ShowTestResult -> {
                    val msg = when (effect.result) {
                        is TestResult.Success -> "连接成功"
                        is TestResult.Error -> "连接失败: ${(effect.result as TestResult.Error).message}"
                    }
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                }
                is OtterMcpEffect.ShowConnectionError -> Toast.makeText(context, effect.message, Toast.LENGTH_LONG).show()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Otter MCP Server", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = { viewModel.processIntent(OtterMcpIntent.RefreshConnectionStatus) }) {
                        Icon(Icons.Default.Refresh, contentDescription = "刷新")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            TabRow(selectedTabIndex = pagerState.currentPage, modifier = Modifier.height(48.dp)) {
                TAB_LABELS.forEachIndexed { index, label ->
                    Tab(
                        selected = pagerState.currentPage == index,
                        onClick = {
                            scope.launch { pagerState.animateScrollToPage(index) }
                            viewModel.processIntent(OtterMcpIntent.SelectTab(index))
                        },
                        text = { Text(label, maxLines = 1) },
                        icon = { Icon(TAB_ICONS[index], contentDescription = null, modifier = Modifier.size(20.dp)) }
                    )
                }
            }
            HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize(), pageSpacing = 0.dp) { page ->
                when (page) {
                    0 -> DashboardTab(state.dashboardState)
                    1 -> WizardTab(state.wizardState, viewModel)
                    2 -> TemplateGalleryTab(state.templateState, viewModel)
                    3 -> SecurityTab()
                    4 -> AuditLogTab(state.auditState, viewModel)
                    5 -> DeviceInteractionTab(state.deviceState, viewModel)
                }
            }
        }
    }
}

// ==================== Dashboard ====================
@Composable private fun DashboardTab(state: DashboardState) {
    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { Text("已连接 Server", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) }
        items(state.servers) { server -> McpServerCard(server) }
        item { Spacer(modifier = Modifier.height(8.dp)) }
        item { Text("最近工具调用", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) }
        if (state.recentCalls.isEmpty()) item { Text("暂无调用记录", style = MaterialTheme.typography.bodyMedium, color = Color.Gray) }
        else items(state.recentCalls) { call -> AuditLogItem(call) }
    }
}

@Composable private fun McpServerCard(server: McpServerState) {
    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(if (server.isConnected) Icons.Default.CloudDone else Icons.Default.CloudOff, contentDescription = null,
                tint = if (server.isConnected) Color(0xFF4CAF50) else Color.Gray, modifier = Modifier.size(32.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(server.name, fontWeight = FontWeight.Medium)
                Text(server.url, style = MaterialTheme.typography.bodySmall, color = Color.Gray, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            Box(modifier = Modifier.clip(RoundedCornerShape(4.dp)).background(
                if (server.isConnected) Color(0xFF4CAF50).copy(alpha = 0.2f) else Color.Gray.copy(alpha = 0.2f)).padding(horizontal = 8.dp, vertical = 2.dp)) {
                Text(if (server.isConnected) "在线" else "离线", style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

// ==================== 连接向导 ====================
@Composable private fun WizardTab(state: ConnectionWizardState, vm: OtterMcpViewModel) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            listOf("类型", "配置", "验证").forEachIndexed { index, label ->
                Box(modifier = Modifier.clip(RoundedCornerShape(8.dp)).background(
                    if (index <= state.currentStep) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant).padding(horizontal = 12.dp, vertical = 6.dp)) {
                    Text(label, color = if (index <= state.currentStep) Color.White else Color.Gray, style = MaterialTheme.typography.labelMedium)
                }
                if (index < 2) Icon(Icons.Default.NavigateNext, contentDescription = null, modifier = Modifier.size(16.dp))
            }
        }
        when (state.currentStep) {
            0 -> StepTypeSelection(state, vm)
            1 -> StepConfig(state, vm)
            2 -> StepVerify(state, vm)
        }
    }
}

@Composable private fun StepTypeSelection(state: ConnectionWizardState, vm: OtterMcpViewModel) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("选择连接类型", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        FilterChip(selected = state.connectionType == ConnectionType.EXTERNAL, onClick = { vm.processIntent(OtterMcpIntent.SelectConnectionType(ConnectionType.EXTERNAL)) },
            label = { Text("连接已有 MCP Server") }, leadingIcon = { Icon(Icons.Default.Cloud, contentDescription = null) })
        FilterChip(selected = state.connectionType == ConnectionType.ANDROID_TEMPLATE, onClick = { vm.processIntent(OtterMcpIntent.SelectConnectionType(ConnectionType.ANDROID_TEMPLATE)) },
            label = { Text("从模板创建 Android 专用 Server") }, leadingIcon = { Icon(Icons.Default.Android, contentDescription = null) })
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { vm.processIntent(OtterMcpIntent.NextStep) }, enabled = state.connectionType != null) { Text("下一步") }
        }
    }
}

@Composable private fun StepConfig(state: ConnectionWizardState, vm: OtterMcpViewModel) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("配置连接参数", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        OutlinedTextField(value = state.serverUrl, onValueChange = { vm.processIntent(OtterMcpIntent.UpdateServerUrl(it)) },
            label = { Text("Server URL") }, placeholder = { Text("https://mcp.example.com") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
        OutlinedTextField(value = state.authToken, onValueChange = { vm.processIntent(OtterMcpIntent.UpdateAuthToken(it)) },
            label = { Text("Auth Token（可选）") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { vm.processIntent(OtterMcpIntent.TestConnection) }, enabled = !state.isTesting) {
                if (state.isTesting) CircularProgressIndicator(modifier = Modifier.size(16.dp)) else Text("测试连接")
            }
            TextButton(onClick = { vm.processIntent(OtterMcpIntent.PrevStep) }) { Text("上一步") }
            Button(onClick = { vm.processIntent(OtterMcpIntent.NextStep) }) { Text("下一步") }
        }
    }
}

@Composable private fun StepVerify(state: ConnectionWizardState, vm: OtterMcpViewModel) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("验证结果", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        when (state.connectionTestResult) {
            is TestResult.Success -> Card(containerColor = Color(0xFF4CAF50).copy(alpha = 0.1f)) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CloudDone, contentDescription = null, tint = Color(0xFF4CAF50))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("连接成功！")
                }
            }
            is TestResult.Error -> Card(containerColor = Color(0xFFF44336).copy(alpha = 0.1f)) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CloudOff, contentDescription = null, tint = Color(0xFFF44336))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text((state.connectionTestResult as TestResult.Error).message)
                }
            }
            null -> Text("请先在「配置」步骤测试连接", color = Color.Gray)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { vm.processIntent(OtterMcpIntent.SaveConnection) }, enabled = state.connectionTestResult is TestResult.Success) { Text("保存") }
            TextButton(onClick = { vm.processIntent(OtterMcpIntent.PrevStep) }) { Text("上一步") }
        }
    }
}

// ==================== 模板库 ====================
@Composable private fun TemplateGalleryTab(state: TemplateGalleryState, vm: OtterMcpViewModel) {
    Column(modifier = Modifier.fillMaxSize()) {
        LazyRow(contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(entries) { category ->
                FilterChip(selected = state.selectedTab == category, onClick = { vm.processIntent(OtterMcpIntent.SelectTemplateTab(category)) },
                    label = { Text(category.label) }, icon = { Icon(category.icon, contentDescription = null, modifier = Modifier.size(16.dp)) })
            }
        }
        if (state.isLoading) Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        else LazyColumn(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(state.templates) { template -> TemplateCard(template) { vm.processIntent(OtterMcpIntent.ImportTemplate(template)) } }
        }
    }
}

@Composable private fun TemplateCard(template: McpTemplate, onImport: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(template.category.icon, contentDescription = null, modifier = Modifier.size(40.dp), tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(template.name, fontWeight = FontWeight.Medium)
                Text(template.description, style = MaterialTheme.typography.bodySmall, color = Color.Gray, maxLines = 2)
            }
            Button(onClick = onImport) { Text("导入") }
        }
    }
}

// ==================== 安全策略 ====================
@Composable private fun SecurityTab() {
    var whitelistEnabled by remember { mutableStateOf(true) }
    var sensitiveOpsEnabled by remember { mutableStateOf(false) }
    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { Text("安全策略配置", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) }
        item { Card(modifier = Modifier.fillMaxWidth()) {
            Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column { Text("工具白名单模式", fontWeight = FontWeight.Medium); Text("仅允许列表中的工具被调用", style = MaterialTheme.typography.bodySmall, color = Color.Gray) }
                Switch(checked = whitelistEnabled, onCheckedChange = { whitelistEnabled = it })
            }
        }}
        item { Card(modifier = Modifier.fillMaxWidth()) {
            Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column { Text("敏感操作确认", fontWeight = FontWeight.Medium); Text("修改文件/删除数据时需要确认", style = MaterialTheme.typography.bodySmall, color = Color.Gray) }
                Switch(checked = sensitiveOpsEnabled, onCheckedChange = { sensitiveOpsEnabled = it })
            }
        }}
        item { Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("权限矩阵", fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    listOf("文件操作", "设备交互", "网络请求").forEach { tool -> Text(tool, style = MaterialTheme.typography.bodySmall, color = Color.Gray) }
                }
            }
        }}
    }
}

// ==================== 审计面板 ====================
@Composable private fun AuditLogTab(state: AuditLogState, vm: OtterMcpViewModel) {
    var showExportMenu by remember { mutableStateOf(false) }
    Column(modifier = Modifier.fillMaxSize()) {
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("审计日志", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Box {
                TextButton(onClick = { showExportMenu = true }) {
                    Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp)); Text("导出")
                }
                DropdownMenu(expanded = showExportMenu, onDismissRequest = { showExportMenu = false }) {
                    DropdownMenuItem(text = { Text("CSV") }, onClick = { vm.processIntent(OtterMcpIntent.ExportLogs(ExportFormat.CSV)); showExportMenu = false })
                    DropdownMenuItem(text = { Text("JSON") }, onClick = { vm.processIntent(OtterMcpIntent.ExportLogs(ExportFormat.JSON)); showExportMenu = false })
                }
            }
        }
        LazyColumn(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            if (state.isLoading) item { LinearProgressIndicator(modifier = Modifier.fillMaxWidth()) }
            items(state.logs) { call -> AuditLogItem(call) }
            if (state.logs.isEmpty() && !state.isLoading) item { Text("暂无审计日志", color = Color.Gray, modifier = Modifier.padding(16.dp)) }
        }
    }
}

@Composable private fun AuditLogItem(call: McpToolCall) {
    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(call.toolName, fontWeight = FontWeight.Medium, style = MaterialTheme.typography.bodyMedium)
                Text(call.serverName, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                if (call.paramsSummary.isNotEmpty()) Text(call.paramsSummary, style = MaterialTheme.typography.bodySmall, color = Color.Gray, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            Text(call.resultSummary ?: "—", color = if (call.resultSummary == "OK") Color(0xFF4CAF50) else Color.Gray, style = MaterialTheme.typography.labelMedium)
        }
    }
}

// ==================== 设备交互 ====================
@Composable private fun DeviceInteractionTab(state: DeviceInteractionState, vm: OtterMcpViewModel) {
    LaunchedEffect(Unit) { vm.processIntent(OtterMcpIntent.RefreshDevices) }
    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { Text("已连接设备", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) }
        if (state.connectedDevices.isEmpty()) item { Text("未发现设备，请确保 ADB 已连接", color = Color.Gray) }
        else items(state.connectedDevices) { device -> DeviceCard(device, state.selectedDevice?.id == device.id) { vm.processIntent(OtterMcpIntent.SelectDevice(device)) } }
        item { Spacer(modifier = Modifier.height(8.dp)) }
        if (state.selectedDevice != null) {
            item { Text("操作面板", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ActionButton(Icons.Default.Screenshot, "截图", state.isLoadingScreenshot) { vm.processIntent(OtterMcpIntent.TakeScreenshot) }
                    ActionButton(Icons.Default.Build, "安装APK") { vm.processIntent(OtterMcpIntent.InstallApk("/path/to/app.apk")) }
                    ActionButton(Icons.Default.Description, "日志") { vm.processIntent(OtterMcpIntent.ReadLogcat) }
                }
            }
        }
    }
}

@Composable private fun DeviceCard(device: AndroidDevice, isSelected: Boolean, onClick: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant)) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.PhoneAndroid, contentDescription = null, modifier = Modifier.size(32.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) { Text(device.name, fontWeight = FontWeight.Medium); Text(device.model, style = MaterialTheme.typography.bodySmall, color = Color.Gray) }
            Box(modifier = Modifier.clip(RoundedCornerShape(4.dp)).background(
                if (device.isConnected) Color(0xFF4CAF50).copy(alpha = 0.2f) else Color.Gray.copy(alpha = 0.2f)).padding(horizontal = 8.dp, vertical = 2.dp)) {
                Text(if (device.isConnected) "已连接" else "离线", style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

@Composable private fun ActionButton(icon: ImageVector, label: String, isLoading: Boolean = false, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        IconButton(onClick = onClick, modifier = Modifier.size(56.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer), enabled = !isLoading) {
            if (isLoading) CircularProgressIndicator(modifier = Modifier.size(24.dp)) else Icon(icon, contentDescription = label)
        }
        Text(label, style = MaterialTheme.typography.labelSmall)
    }
}
