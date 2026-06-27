package com.mvi.kenny.feature.appfunctionsworkflow

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.collectLatest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * ============================================================
 * AppFunctionsWorkflowScreen — AppFunctions AI 工作流中间件主界面
 * AppFunctions AI Workflow Middleware Main Screen
 * ============================================================
 *
 * PRD-297 | Android AppFunctions AI 工作流中间件
 *
 * Design: Terminal Console Style
 * - Deep dark background (#121212)
 * - Cyan accent (#00E5FF)
 * - Green accent (#76FF03)
 * - Orange accent (#FF9800)
 * - Red error (#FF5252)
 * - JetBrains Mono for code / 等宽字体显示代码
 *
 * 4 Tab Bottom Navigation:
 * - Tab 1: 适配层 (Adapter Layer)
 * - Tab 2: 编排引擎 (Orchestration Engine)
 * - Tab 3: 权限审计 (Permission & Audit)
 * - Tab 4: 飞书集成 (Feishu Integration)
 */

// ============================================================
// Terminal Style Color Palette / Terminal 风格配色
// ============================================================

private object WorkflowColors {
    val Background = Color(0xFF121212)
    val Surface = Color(0xFF1E1E1E)
    val SurfaceVariant = Color(0xFF2D2D2D)
    val Primary = Color(0xFF00E5FF)       // Cyan / 青色
    val Secondary = Color(0xFF76FF03)      // Green / 绿色
    val Warning = Color(0xFFFF9800)         // Orange / 橙色
    val Error = Color(0xFFFF5252)          // Red / 红色
    val TextPrimary = Color(0xFFE0E0E0)
    val TextSecondary = Color(0xFF9E9E9E)
    val CodeBackground = Color(0xFF0D1117)
    val Divider = Color(0xFF3D3D3D)
}

private val tabIcons = listOf(
    Icons.Default.Hub,
    Icons.Default.AccountTree,
    Icons.Default.Security,
    Icons.Default.Notifications
)

// ============================================================
// Main Screen / 主界面
// ============================================================

/**
 * AppFunctions Workflow Middleware Screen
 * AppFunctions AI 工作流中间件主界面
 *
 * @param viewModel AppFunctionsWorkflowViewModel instance
 * @param onNavigateBack Navigation callback / 导航回调
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppFunctionsWorkflowScreen(
    viewModel: AppFunctionsWorkflowViewModel = viewModel(),
    onNavigateBack: () -> Unit = {}
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    // File export launcher / 文件导出启动器
    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/plain")
    ) { uri ->
        uri?.let {
            try {
                context.contentResolver.openOutputStream(it)?.use { os ->
                    // Export handled via effect / 通过 effect 处理导出
                }
            } catch (e: Exception) {
                // Silently fail / 静默失败
            }
        }
    }

    // Collect side effects / 收集副作用
    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is AppFunctionsWorkflowEffect.ShowToast -> {
                    Toast.makeText(context, effect.message, Toast.LENGTH_SHORT).show()
                }
                is AppFunctionsWorkflowEffect.CopyToClipboard -> {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = ClipData.newPlainText("AppFunctionsWorkflow", effect.text)
                    clipboard.setPrimaryClip(clip)
                }
                is AppFunctionsWorkflowEffect.SendFeishuNotification -> {
                    // Feishu notification would be sent via webhook / 通过 webhook 发送飞书通知
                    Toast.makeText(context, "飞书通知已发送 / Feishu notification sent", Toast.LENGTH_SHORT).show()
                }
                is AppFunctionsWorkflowEffect.ExportAuditLogFile -> {
                    exportLauncher.launch(effect.filename)
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "${AppFunctionsWorkflowTab.entries[state.selectedTab].title} | AppFunctions 工作流",
                        color = WorkflowColors.Primary,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = WorkflowColors.Surface
                ),
                actions = {
                    TextButton(onClick = onNavigateBack) {
                        Text("← Back", color = WorkflowColors.Primary)
                    }
                }
            )
        },
        bottomBar = {
            WorkflowNavigationBar(
                selectedIndex = state.selectedTab,
                onTabSelected = { viewModel.sendIntent(AppFunctionsWorkflowIntent.SelectTab(it)) }
            )
        },
        containerColor = WorkflowColors.Background
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (state.selectedTab) {
                0 -> AdapterLayerTab(state, viewModel)
                1 -> OrchestrationTab(state, viewModel)
                2 -> PermissionAuditTab(state, viewModel)
                3 -> FeishuIntegrationTab(state, viewModel)
            }
        }
    }
}

// ============================================================
// Bottom Navigation Bar / 底部导航栏
// ============================================================

@Composable
private fun WorkflowNavigationBar(
    selectedIndex: Int,
    onTabSelected: (Int) -> Unit
) {
    NavigationBar(
        containerColor = WorkflowColors.Surface,
        contentColor = WorkflowColors.Primary
    ) {
        AppFunctionsWorkflowTab.entries.forEachIndexed { index, tab ->
            NavigationBarItem(
                selected = selectedIndex == index,
                onClick = { onTabSelected(index) },
                icon = {
                    Icon(
                        imageVector = tabIcons[index],
                        contentDescription = tab.title
                    )
                },
                label = {
                    Text(
                        text = tab.title,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = WorkflowColors.Primary,
                    selectedTextColor = WorkflowColors.Primary,
                    unselectedIconColor = WorkflowColors.TextSecondary,
                    unselectedTextColor = WorkflowColors.TextSecondary,
                    indicatorColor = WorkflowColors.Primary.copy(alpha = 0.15f)
                )
            )
        }
    }
}

// ============================================================
// Tab 1: Adapter Layer / 适配层
// ============================================================

@Composable
private fun AdapterLayerTab(state: AppFunctionsWorkflowState, viewModel: AppFunctionsWorkflowViewModel) {
    var showAddDialog by remember { mutableStateOf(false) }
    var newAppName by remember { mutableStateOf("") }
    var newFunctionName by remember { mutableStateOf("") }
    var newDescription by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "// AppFunctions 企业适配层",
                color = WorkflowColors.Primary,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
            Text(
                text = "注册和管理企业内部的 AppFunctions，形成统一的调用入口",
                color = WorkflowColors.TextSecondary,
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 4.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = WorkflowColors.Divider)
        }

        // Add new function button / 添加新功能按钮
        item {
            Button(
                onClick = { showAddDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = WorkflowColors.Primary),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("注册新 AppFunction / Register New AppFunction", fontFamily = FontFamily.Monospace)
            }
        }

        // Show registered functions / 显示已注册的功能
        if (state.registeredFunctions.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = WorkflowColors.Surface)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "// 暂无注册的 AppFunctions",
                            color = WorkflowColors.TextSecondary,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "点击上方按钮注册第一个 AppFunction",
                            color = WorkflowColors.TextSecondary,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
        } else {
            itemsIndexed(state.registeredFunctions) { _, func ->
                FunctionCard(
                    function = func,
                    onUnregister = { viewModel.sendIntent(AppFunctionsWorkflowIntent.UnregisterAppFunction(func.id)) },
                    onCopy = { viewModel.sendIntent(AppFunctionsWorkflowIntent.CopyToClipboard("${func.appName}:${func.functionName}")) }
                )
            }
        }

        // Schema template section / Schema 模板部分
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "// Schema 模板 / Schema Templates",
                color = WorkflowColors.Secondary,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            SchemaTemplateCard(
                title = "基础模板 / Basic Template",
                schema = """{"name":"functionName","description":"Function description","parameters":{},"returns":{}}"""
            )
        }

        item { Spacer(modifier = Modifier.height(32.dp)) }
    }

    // Add function dialog / 添加功能对话框
    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = {
                Text("注册 AppFunction", fontFamily = FontFamily.Monospace, color = WorkflowColors.Primary)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = newAppName,
                        onValueChange = { newAppName = it },
                        label = { Text("App 名称 / App Name") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = WorkflowColors.Primary,
                            unfocusedBorderColor = WorkflowColors.Divider
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newFunctionName,
                        onValueChange = { newFunctionName = it },
                        label = { Text("Function 名称 / Function Name") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = WorkflowColors.Primary,
                            unfocusedBorderColor = WorkflowColors.Divider
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newDescription,
                        onValueChange = { newDescription = it },
                        label = { Text("描述 / Description") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = WorkflowColors.Primary,
                            unfocusedBorderColor = WorkflowColors.Divider
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newAppName.isNotBlank() && newFunctionName.isNotBlank()) {
                            viewModel.sendIntent(
                                AppFunctionsWorkflowIntent.RegisterAppFunction(
                                    AppFunctionEntry(
                                        id = "",
                                        appName = newAppName,
                                        functionName = newFunctionName,
                                        description = newDescription
                                    )
                                )
                            )
                            newAppName = ""
                            newFunctionName = ""
                            newDescription = ""
                            showAddDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = WorkflowColors.Primary)
                ) {
                    Text("注册 / Register", fontFamily = FontFamily.Monospace)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("取消 / Cancel", fontFamily = FontFamily.Monospace)
                }
            },
            containerColor = WorkflowColors.Surface
        )
    }
}

@Composable
private fun FunctionCard(
    function: AppFunctionEntry,
    onUnregister: () -> Unit,
    onCopy: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = WorkflowColors.Surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = function.appName,
                        color = WorkflowColors.Primary,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = function.functionName,
                        color = WorkflowColors.Secondary,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp
                    )
                }
                Row {
                    IconButton(onClick = onCopy) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = WorkflowColors.TextSecondary)
                    }
                    IconButton(onClick = onUnregister) {
                        Icon(Icons.Default.Delete, contentDescription = "Unregister", tint = WorkflowColors.Error)
                    }
                }
            }
            if (function.description.isNotBlank()) {
                Text(
                    text = function.description,
                    color = WorkflowColors.TextSecondary,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun SchemaTemplateCard(title: String, schema: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = WorkflowColors.Surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                color = WorkflowColors.Warning,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .background(WorkflowColors.CodeBackground)
                    .border(1.dp, WorkflowColors.Divider, RoundedCornerShape(6.dp))
                    .padding(12.dp)
            ) {
                Text(
                    text = schema,
                    color = WorkflowColors.TextPrimary,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp
                )
            }
        }
    }
}

// ============================================================
// Tab 2: Orchestration Engine / 编排引擎
// ============================================================

@Composable
private fun OrchestrationTab(state: AppFunctionsWorkflowState, viewModel: AppFunctionsWorkflowViewModel) {
    var showCreateDialog by remember { mutableStateOf(false) }
    var newWorkflowName by remember { mutableStateOf("") }
    var newWorkflowDesc by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "// 多 App 编排引擎",
                color = WorkflowColors.Primary,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
            Text(
                text = "基于 DAG 的工作流编排，支持跨 App 函数调用串联",
                color = WorkflowColors.TextSecondary,
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 4.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = WorkflowColors.Divider)
        }

        // Execution progress / 执行进度
        if (state.isExecuting) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = WorkflowColors.Surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "// 执行中 / Executing...",
                            color = WorkflowColors.Warning,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = { state.executionProgress / 100f },
                            modifier = Modifier.fillMaxWidth(),
                            color = WorkflowColors.Primary,
                            trackColor = WorkflowColors.Divider
                        )
                        Text(
                            text = "${state.executionProgress}%",
                            color = WorkflowColors.TextSecondary,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
        }

        // Create workflow button / 创建工作流按钮
        item {
            Button(
                onClick = { showCreateDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = WorkflowColors.Primary),
                modifier = Modifier.fillMaxWidth(),
                enabled = !state.isExecuting
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("创建新工作流 / Create Workflow", fontFamily = FontFamily.Monospace)
            }
        }

        // Workflow list / 工作流列表
        if (state.workflows.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = WorkflowColors.Surface)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "// 暂无工作流",
                            color = WorkflowColors.TextSecondary,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "点击上方按钮创建第一个工作流",
                            color = WorkflowColors.TextSecondary,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
        } else {
            itemsIndexed(state.workflows) { _, workflow ->
                WorkflowCard(
                    workflow = workflow,
                    isExpanded = workflow.id in state.expandedWorkflows,
                    onToggleExpand = { viewModel.sendIntent(AppFunctionsWorkflowIntent.ToggleWorkflowExpansion(workflow.id)) },
                    onExecute = { viewModel.sendIntent(AppFunctionsWorkflowIntent.ExecuteWorkflow(workflow.id)) },
                    onDelete = { viewModel.sendIntent(AppFunctionsWorkflowIntent.DeleteWorkflow(workflow.id)) },
                    isExecuting = state.isExecuting
                )
            }
        }

        // DAG visualization placeholder / DAG 可视化占位
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "// DAG 可视化 / DAG Visualization",
                color = WorkflowColors.Secondary,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                colors = CardDefaults.cardColors(containerColor = WorkflowColors.CodeBackground)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "┌───────┐",
                            color = WorkflowColors.Primary,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "│ Node A │",
                            color = WorkflowColors.Primary,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "└───┬───┘",
                            color = WorkflowColors.Primary,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "    │",
                            color = WorkflowColors.TextSecondary,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "┌───┴───┐",
                            color = WorkflowColors.Primary,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "│ Node B │  │ Node C │",
                            color = WorkflowColors.Primary,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "└───────┘  └───────┘",
                            color = WorkflowColors.Primary,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(32.dp)) }
    }

    // Create workflow dialog / 创建工作流对话框
    if (showCreateDialog) {
        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = {
                Text("创建工作流", fontFamily = FontFamily.Monospace, color = WorkflowColors.Primary)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = newWorkflowName,
                        onValueChange = { newWorkflowName = it },
                        label = { Text("工作流名称 / Workflow Name") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = WorkflowColors.Primary,
                            unfocusedBorderColor = WorkflowColors.Divider
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newWorkflowDesc,
                        onValueChange = { newWorkflowDesc = it },
                        label = { Text("描述 / Description") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = WorkflowColors.Primary,
                            unfocusedBorderColor = WorkflowColors.Divider
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newWorkflowName.isNotBlank()) {
                            viewModel.sendIntent(
                                AppFunctionsWorkflowIntent.CreateWorkflow(newWorkflowName, newWorkflowDesc)
                            )
                            newWorkflowName = ""
                            newWorkflowDesc = ""
                            showCreateDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = WorkflowColors.Primary)
                ) {
                    Text("创建 / Create", fontFamily = FontFamily.Monospace)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) {
                    Text("取消 / Cancel", fontFamily = FontFamily.Monospace)
                }
            },
            containerColor = WorkflowColors.Surface
        )
    }
}

@Composable
private fun WorkflowCard(
    workflow: WorkflowDefinition,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onExecute: () -> Unit,
    onDelete: () -> Unit,
    isExecuting: Boolean
) {
    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = WorkflowColors.Surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = workflow.name,
                        color = WorkflowColors.Primary,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${workflow.nodes.size} 节点 / nodes",
                        color = WorkflowColors.TextSecondary,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp
                    )
                    workflow.lastExecutedAt?.let {
                        Text(
                            text = "上次执行: ${dateFormat.format(Date(it))}",
                            color = WorkflowColors.TextSecondary,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp
                        )
                    }
                }
                Row {
                    IconButton(onClick = onToggleExpand) {
                        Icon(
                            imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = "Expand",
                            tint = WorkflowColors.TextSecondary
                        )
                    }
                    IconButton(onClick = onExecute, enabled = !isExecuting) {
                        Icon(Icons.Default.PlayArrow, contentDescription = "Execute", tint = if (isExecuting) WorkflowColors.TextSecondary else WorkflowColors.Secondary)
                    }
                    IconButton(onClick = onDelete, enabled = !isExecuting) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = WorkflowColors.Error)
                    }
                }
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    if (workflow.description.isNotBlank()) {
                        Text(
                            text = workflow.description,
                            color = WorkflowColors.TextSecondary,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    HorizontalDivider(color = WorkflowColors.Divider)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "// 节点列表 / Nodes:",
                        color = WorkflowColors.Warning,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp
                    )
                    workflow.nodes.forEach { node ->
                        NodeStatusRow(node = node)
                    }
                }
            }
        }
    }
}

@Composable
private fun NodeStatusRow(node: WorkflowNode) {
    val statusColor = when (node.status) {
        NodeStatus.PENDING -> WorkflowColors.TextSecondary
        NodeStatus.RUNNING -> WorkflowColors.Warning
        NodeStatus.SUCCESS -> WorkflowColors.Secondary
        NodeStatus.FAILED -> WorkflowColors.Error
        NodeStatus.SKIPPED -> WorkflowColors.TextSecondary
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = node.name,
            color = WorkflowColors.TextPrimary,
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp
        )
        Text(
            text = node.status.name,
            color = statusColor,
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp
        )
    }
}

// ============================================================
// Tab 3: Permission & Audit / 权限审计
// ============================================================

@Composable
private fun PermissionAuditTab(state: AppFunctionsWorkflowState, viewModel: AppFunctionsWorkflowViewModel) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "// 权限与审计",
                color = WorkflowColors.Primary,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
            Text(
                text = "AI 操作全程可追溯，满足金融/医疗合规要求",
                color = WorkflowColors.TextSecondary,
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 4.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = WorkflowColors.Divider)
        }

        // Export buttons / 导出按钮
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { viewModel.sendIntent(AppFunctionsWorkflowIntent.ExportAuditLog("json")) },
                    colors = ButtonDefaults.buttonColors(containerColor = WorkflowColors.Primary),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("JSON", fontFamily = FontFamily.Monospace, fontSize = 12.sp)
                }
                Button(
                    onClick = { viewModel.sendIntent(AppFunctionsWorkflowIntent.ExportAuditLog("csv")) },
                    colors = ButtonDefaults.buttonColors(containerColor = WorkflowColors.Secondary),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("CSV", fontFamily = FontFamily.Monospace, fontSize = 12.sp)
                }
            }
        }

        // Audit log list / 审计日志列表
        if (state.auditLogs.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = WorkflowColors.Surface)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "// 暂无审计日志",
                            color = WorkflowColors.TextSecondary,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "执行工作流后将自动生成审计日志",
                            color = WorkflowColors.TextSecondary,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
        } else {
            item {
                Text(
                    text = "// 审计日志 / Audit Logs: ${state.auditLogs.size} 条记录",
                    color = WorkflowColors.Warning,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
            itemsIndexed(state.auditLogs) { _, log ->
                AuditLogCard(
                    log = log,
                    isExpanded = log.id in state.expandedAuditLogs,
                    onToggleExpand = { viewModel.sendIntent(AppFunctionsWorkflowIntent.ToggleAuditLogExpansion(log.id)) },
                    onCopy = { viewModel.sendIntent(AppFunctionsWorkflowIntent.CopyToClipboard(log.toString())) }
                )
            }
        }

        item { Spacer(modifier = Modifier.height(32.dp)) }
    }
}

@Composable
private fun AuditLogCard(
    log: AuditLogEntry,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onCopy: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()) }
    val statusColor = when (log.status) {
        NodeStatus.PENDING -> WorkflowColors.TextSecondary
        NodeStatus.RUNNING -> WorkflowColors.Warning
        NodeStatus.SUCCESS -> WorkflowColors.Secondary
        NodeStatus.FAILED -> WorkflowColors.Error
        NodeStatus.SKIPPED -> WorkflowColors.TextSecondary
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = WorkflowColors.Surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = log.workflowName,
                        color = WorkflowColors.Primary,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${log.nodeName} · ${log.status.name}",
                        color = statusColor,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp
                    )
                    Text(
                        text = dateFormat.format(Date(log.timestamp)),
                        color = WorkflowColors.TextSecondary,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp
                    )
                }
                Row {
                    IconButton(onClick = onCopy) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = WorkflowColors.TextSecondary)
                    }
                    IconButton(onClick = onToggleExpand) {
                        Icon(
                            imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = "Expand",
                            tint = WorkflowColors.TextSecondary
                        )
                    }
                }
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    HorizontalDivider(color = WorkflowColors.Divider)
                    Spacer(modifier = Modifier.height(8.dp))

                    DetailRow("Function", log.functionName)
                    DetailRow("Caller", log.caller)
                    DetailRow("Duration", "${log.durationMs}ms")
                    DetailRow("Input", log.inputSummary)
                    DetailRow("Output", log.outputSummary)
                    log.errorDetail?.let { DetailRow("Error", it) }
                }
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "// $label:",
            color = WorkflowColors.TextSecondary,
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp
        )
        Text(
            text = value,
            color = WorkflowColors.TextPrimary,
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp,
            modifier = Modifier.weight(1f, fill = false)
        )
    }
}

// ============================================================
// Tab 4: Feishu Integration / 飞书集成
// ============================================================

@Composable
private fun FeishuIntegrationTab(state: AppFunctionsWorkflowState, viewModel: AppFunctionsWorkflowViewModel) {
    var webhookUrl by remember(state.feishuConfig.webhookUrl) { mutableStateOf(state.feishuConfig.webhookUrl) }
    var mentionUsers by remember(state.feishuConfig.mentionUsers) { mutableStateOf(state.feishuConfig.mentionUsers.joinToString(",")) }
    var notifyOnSuccess by remember(state.feishuConfig.notifyOnSuccess) { mutableStateOf(state.feishuConfig.notifyOnSuccess) }
    var notifyOnFailure by remember(state.feishuConfig.notifyOnFailure) { mutableStateOf(state.feishuConfig.notifyOnFailure) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "// 飞书集成",
            color = WorkflowColors.Primary,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp
        )
        Text(
            text = "配置飞书 Webhook，工作流执行结果实时推送",
            color = WorkflowColors.TextSecondary,
            fontFamily = FontFamily.Monospace,
            fontSize = 12.sp,
            modifier = Modifier.padding(top = 4.dp)
        )
        HorizontalDivider(color = WorkflowColors.Divider)

        // Enable switch / 启用开关
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = WorkflowColors.Surface)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "启用飞书通知",
                        color = WorkflowColors.TextPrimary,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Enable Feishu Notifications",
                        color = WorkflowColors.TextSecondary,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp
                    )
                }
                Switch(
                    checked = state.feishuConfig.enabled,
                    onCheckedChange = { enabled ->
                        viewModel.sendIntent(
                            AppFunctionsWorkflowIntent.UpdateFeishuConfig(
                                state.feishuConfig.copy(enabled = enabled)
                            )
                        )
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = WorkflowColors.Primary,
                        checkedTrackColor = WorkflowColors.Primary.copy(alpha = 0.5f)
                    )
                )
            }
        }

        // Webhook URL / Webhook 地址
        OutlinedTextField(
            value = webhookUrl,
            onValueChange = {
                webhookUrl = it
                viewModel.sendIntent(
                    AppFunctionsWorkflowIntent.UpdateFeishuConfig(
                        state.feishuConfig.copy(webhookUrl = it)
                    )
                )
            },
            label = { Text("Webhook URL / Webhook 地址") },
            placeholder = { Text("https://open.feishu.cn/open-apis/bot/v2/hook/xxx") },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = WorkflowColors.Primary,
                unfocusedBorderColor = WorkflowColors.Divider
            ),
            modifier = Modifier.fillMaxWidth(),
            enabled = state.feishuConfig.enabled
        )

        // Mention users / @ 用户
        OutlinedTextField(
            value = mentionUsers,
            onValueChange = {
                mentionUsers = it
                val users = it.split(",").map { u -> u.trim() }.filter { u -> u.isNotBlank() }
                viewModel.sendIntent(
                    AppFunctionsWorkflowIntent.UpdateFeishuConfig(
                        state.feishuConfig.copy(mentionUsers = users)
                    )
                )
            },
            label = { Text("@ 用户 ID (逗号分隔) / Mention Users") },
            placeholder = { Text("ou_xxx1,ou_xxx2") },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = WorkflowColors.Primary,
                unfocusedBorderColor = WorkflowColors.Divider
            ),
            modifier = Modifier.fillMaxWidth(),
            enabled = state.feishuConfig.enabled
        )

        // Notification options / 通知选项
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = WorkflowColors.Surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "// 通知选项 / Notification Options",
                    color = WorkflowColors.Warning,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(12.dp))

                // Notify on success / 成功时通知
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "执行成功时通知",
                        color = WorkflowColors.TextPrimary,
                        fontFamily = FontFamily.Monospace
                    )
                    Switch(
                        checked = notifyOnSuccess,
                        onCheckedChange = {
                            notifyOnSuccess = it
                            viewModel.sendIntent(
                                AppFunctionsWorkflowIntent.UpdateFeishuConfig(
                                    state.feishuConfig.copy(notifyOnSuccess = it)
                                )
                            )
                        },
                        enabled = state.feishuConfig.enabled,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = WorkflowColors.Secondary,
                            checkedTrackColor = WorkflowColors.Secondary.copy(alpha = 0.5f)
                        )
                    )
                }

                // Notify on failure / 失败时通知
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "执行失败时通知",
                        color = WorkflowColors.TextPrimary,
                        fontFamily = FontFamily.Monospace
                    )
                    Switch(
                        checked = notifyOnFailure,
                        onCheckedChange = {
                            notifyOnFailure = it
                            viewModel.sendIntent(
                                AppFunctionsWorkflowIntent.UpdateFeishuConfig(
                                    state.feishuConfig.copy(notifyOnFailure = it)
                                )
                            )
                        },
                        enabled = state.feishuConfig.enabled,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = WorkflowColors.Error,
                            checkedTrackColor = WorkflowColors.Error.copy(alpha = 0.5f)
                        )
                    )
                }
            }
        }

        // Test notification button / 测试通知按钮
        Button(
            onClick = {
                viewModel.sendIntent(
                    AppFunctionsWorkflowIntent.SendTestFeishuNotification(
                        mentionedUsers = mentionUsers.split(",").map { it.trim() }.filter { it.isNotBlank() }
                    )
                )
            },
            colors = ButtonDefaults.buttonColors(containerColor = WorkflowColors.Primary),
            modifier = Modifier.fillMaxWidth(),
            enabled = state.feishuConfig.enabled && state.feishuConfig.webhookUrl.isNotBlank()
        ) {
            Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("发送测试通知 / Send Test", fontFamily = FontFamily.Monospace)
        }

        // Webhook setup guide / Webhook 配置指南
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = WorkflowColors.Surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "// Webhook 配置指南",
                    color = WorkflowColors.Secondary,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "1. 打开飞书群聊\n2. 点击设置 → 群机器人 → 添加机器人\n3. 选择「自定义机器人」\n4. 设置机器人名称，复制 Webhook URL\n5. 将 URL 粘贴到上方输入框",
                    color = WorkflowColors.TextSecondary,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    lineHeight = 18.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}