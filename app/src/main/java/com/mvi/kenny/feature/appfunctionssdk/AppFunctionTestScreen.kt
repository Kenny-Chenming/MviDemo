package com.mvi.kenny.feature.appfunctionssdk

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Functions
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mvi.kenny.feature.appfunctions.ValidationState
import kotlinx.coroutines.flow.collectLatest
import android.widget.Toast
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.VerticalDivider

/**
 * ============================================================
 * AppFunctionTestScreen — AppFunctions SDK 测试工具主界面
 * ============================================================
 * PRD-185 | Android AppFunctions SDK 开发工具包
 *
 * 4 个 Tab：
 * 1. 函数列表 (FUNCTION_LIST) — 展示所有注册的 AppFunction
 * 2. 测试面板 (TEST_PANEL) — 单个 AppFunction 测试（输入参数/执行/看结果）
 * 3. Agent 模拟 (AGENT_SIM) — 模拟 Agent 发现和调用流程
 * 4. 执行日志 (LOG_VIEWER) — 执行日志查看
 *
 * @see AppFunctionTestViewModel 状态管理
 * @see AppFunctionTestState 页面状态
 */

// ============================================================
// 常量 / Constants
// ============================================================

private val ColorPrimary = Color(0xFF4285F4)
private val ColorSecondary = Color(0xFF34A853)
private val ColorWarning = Color(0xFFFBBC04)
private val ColorError = Color(0xFFEA4335)
private val ColorBackground = Color(0xFFF8F9FA)
private val ColorSurface = Color(0xFFFFFFFF)
private val ColorOnSurface = Color(0xFF202124)

// ============================================================
// 主界面入口 / Main Entry Point
// ============================================================

@Composable
fun AppFunctionTestScreen(
    onUpdateTopBar: (TopBarConfig) -> Unit = {}
) {
    val state = AppFunctionTestViewModel().state.collectAsState()
    val viewModel = remember { AppFunctionTestViewModel() }

    // 监听副作用
    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is AppFunctionTestEffect.ShowToast -> {
                    // Toast handled by Android context
                }
                is AppFunctionTestEffect.NavigateToFunction -> {
                    // Navigation handled by state change
                }
                is AppFunctionTestEffect.ExportReport -> {
                    // Export handled by system
                }
            }
        }
    }

    // 更新 TopBar
    LaunchedEffect(state.value.selectedTab) {
        onUpdateTopBar(
            TopBarConfig(
                title = "AppFunctions SDK 测试工具",
                subtitle = when (state.value.selectedTab) {
                    AppFunctionTestTab.FUNCTION_LIST -> "函数列表"
                    AppFunctionTestTab.TEST_PANEL -> "测试面板"
                    AppFunctionTestTab.AGENT_SIM -> "Agent 模拟"
                    AppFunctionTestTab.LOG_VIEWER -> "执行日志"
                }
            )
        )
    }

    Scaffold(
        containerColor = ColorBackground,
        bottomBar = {
            BottomNavigationBar(
                selectedTab = state.value.selectedTab,
                onTabSelected = { tab ->
                    viewModel.sendIntent(AppFunctionTestIntent.SwitchTab(tab))
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // 内容区域
            when (state.value.selectedTab) {
                AppFunctionTestTab.FUNCTION_LIST -> FunctionListTab(
                    state = state.value,
                    onIntent = viewModel::sendIntent
                )
                AppFunctionTestTab.TEST_PANEL -> TestPanelTab(
                    state = state.value,
                    onIntent = viewModel::sendIntent
                )
                AppFunctionTestTab.AGENT_SIM -> AgentSimTab(
                    state = state.value,
                    onIntent = viewModel::sendIntent
                )
                AppFunctionTestTab.LOG_VIEWER -> LogViewerTab(
                    state = state.value,
                    onIntent = viewModel::sendIntent
                )
            }
        }
    }
}

// ============================================================
// TopBar 配置 / TopBar Configuration
// ============================================================

data class TopBarConfig(
    val title: String = "AppFunctions SDK",
    val subtitle: String = ""
)

// ============================================================
// 底部导航 / Bottom Navigation Bar
// ============================================================

@Composable
private fun BottomNavigationBar(
    selectedTab: AppFunctionTestTab,
    onTabSelected: (AppFunctionTestTab) -> Unit
) {
    NavigationBar(
        containerColor = ColorSurface,
        contentColor = ColorOnSurface
    ) {
        AppFunctionTestTab.entries.forEach { tab ->
            val selected = selectedTab == tab
            NavigationBarItem(
                selected = selected,
                onClick = { onTabSelected(tab) },
                icon = {
                    BadgedBox(
                        badge = {
                            // Optional badge
                        }
                    ) {
                        Icon(
                            imageVector = getTabIcon(tab),
                            contentDescription = tab.title
                        )
                    }
                },
                label = { Text(tab.title, maxLines = 1) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = ColorPrimary,
                    selectedTextColor = ColorPrimary,
                    indicatorColor = ColorPrimary.copy(alpha = 0.1f)
                )
            )
        }
    }
}

private fun getTabIcon(tab: AppFunctionTestTab): ImageVector = when (tab) {
    AppFunctionTestTab.FUNCTION_LIST -> Icons.Default.Functions
    AppFunctionTestTab.TEST_PANEL -> Icons.Default.PlayArrow
    AppFunctionTestTab.AGENT_SIM -> Icons.Default.SmartToy
    AppFunctionTestTab.LOG_VIEWER -> Icons.Default.List
}

// ============================================================
// Tab 1: 函数列表 / Function List Tab
// ============================================================

@Composable
private fun FunctionListTab(
    state: AppFunctionTestState,
    onIntent: (AppFunctionTestIntent) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        // 工具栏
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "AppFunction 列表",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${state.functions.size} 个函数注册",
                    style = MaterialTheme.typography.bodySmall,
                    color = ColorOnSurface.copy(alpha = 0.6f)
                )
            }
            Row {
                // 合规检测按钮
                Button(
                    onClick = { onIntent(AppFunctionTestIntent.RunComplianceCheck) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ColorSecondary
                    ),
                    enabled = !state.isLoading
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("合规检测")
                }
                Spacer(Modifier.width(8.dp))
                // 刷新按钮
                IconButton(
                    onClick = { onIntent(AppFunctionTestIntent.LoadFunctions) },
                    enabled = !state.isLoading
                ) {
                    if (state.isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    } else {
                        Icon(Icons.Default.Refresh, contentDescription = "刷新")
                    }
                }
            }
        }

        // 合规报告摘要
        state.complianceReport?.let { report ->
            ComplianceReportCard(report = report)
            Spacer(Modifier.height(8.dp))
        }

        // 函数列表
        if (state.isLoading && state.functions.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = ColorPrimary)
                    Spacer(Modifier.height(16.dp))
                    Text("正在扫描 @AppFunction...")
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(state.functions, key = { it.id }) { function ->
                    FunctionListItem(
                        function = function,
                        isExpanded = function.id in state.expandedFunctionIds,
                        onToggleExpand = { onIntent(AppFunctionTestIntent.ToggleFunctionExpand(function.id)) },
                        onSelect = { onIntent(AppFunctionTestIntent.SelectFunction(function.id)) }
                    )
                }
                item { Spacer(Modifier.height(16.dp)) }
            }
        }
    }
}

@Composable
private fun ComplianceReportCard(report: ComplianceReport) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = ColorSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "合规检测报告",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatChip(label = "总数", value = "${report.totalFunctions}", color = ColorOnSurface)
                StatChip(label = "通过", value = "${report.passed}", color = ColorSecondary)
                StatChip(label = "警告", value = "${report.warnings}", color = ColorWarning)
                StatChip(label = "错误", value = "${report.errors}", color = ColorError)
            }
        }
    }
}

@Composable
private fun StatChip(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = ColorOnSurface.copy(alpha = 0.6f)
        )
    }
}

@Composable
private fun FunctionListItem(
    function: AppFunctionTestItem,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onSelect: () -> Unit
) {
    val borderColor by animateColorAsState(
        targetValue = when (function.validationState) {
            ValidationState.PASS -> ColorSecondary
            ValidationState.WARNING -> ColorWarning
            ValidationState.ERROR -> ColorError
            ValidationState.UNCHECKED -> ColorOnSurface.copy(alpha = 0.2f)
        },
        label = "borderColor"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(2.dp, borderColor, RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = ColorSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // 头部行
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 状态图标
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(borderColor.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = function.validationState.symbol,
                        fontSize = 18.sp
                    )
                }
                Spacer(Modifier.width(12.dp))
                // 函数名和包名
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = function.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = function.packageName,
                        style = MaterialTheme.typography.bodySmall,
                        color = ColorOnSurface.copy(alpha = 0.5f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                // Agent 可见性图标
                Icon(
                    imageVector = if (function.agentVisibility) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                    contentDescription = if (function.agentVisibility) "对 Agent 可见" else "对 Agent 隐藏",
                    tint = if (function.agentVisibility) ColorSecondary else ColorOnSurface.copy(alpha = 0.3f),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(8.dp))
                // 展开/折叠按钮
                IconButton(onClick = onToggleExpand) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (isExpanded) "折叠" else "展开"
                    )
                }
            }

            // 展开内容
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    HorizontalDivider(color = ColorOnSurface.copy(alpha = 0.1f))
                    Spacer(Modifier.height(12.dp))

                    // 返回类型
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "返回类型: ",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = function.returnType,
                            style = MaterialTheme.typography.bodySmall,
                            fontFamily = FontFamily.Monospace,
                            color = ColorPrimary
                        )
                    }
                    Spacer(Modifier.height(8.dp))

                    // 参数列表
                    Text(
                        text = "参数列表 (${function.params.size})",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(4.dp))
                    function.params.forEach { param ->
                        Row(
                            modifier = Modifier.padding(start = 8.dp, top = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (param.isOptional) "○" else "●",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (param.isOptional) ColorOnSurface.copy(alpha = 0.4f) else ColorPrimary
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = param.name,
                                style = MaterialTheme.typography.bodySmall,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = ": ${param.type}",
                                style = MaterialTheme.typography.bodySmall,
                                fontFamily = FontFamily.Monospace,
                                color = ColorOnSurface.copy(alpha = 0.6f)
                            )
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    // 测试按钮
                    Button(
                        onClick = onSelect,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = ColorPrimary)
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("进入测试面板")
                    }
                }
            }
        }
    }
}

// ============================================================
// Tab 2: 测试面板 / Test Panel Tab
// ============================================================

@Composable
private fun TestPanelTab(
    state: AppFunctionTestState,
    onIntent: (AppFunctionTestIntent) -> Unit
) {
    val selectedFunction = state.selectedFunction

    if (selectedFunction == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    Icons.Default.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = ColorOnSurface.copy(alpha = 0.3f)
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    text = "请先选择一个函数",
                    style = MaterialTheme.typography.bodyLarge,
                    color = ColorOnSurface.copy(alpha = 0.5f)
                )
                Text(
                    text = "在「函数列表」Tab 中选择后即可测试",
                    style = MaterialTheme.typography.bodySmall,
                    color = ColorOnSurface.copy(alpha = 0.3f)
                )
            }
        }
        return
    }

    val paramInputs = state.paramInputs[selectedFunction.id] ?: emptyMap()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(Modifier.height(16.dp))

        // 函数信息卡片
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = ColorPrimary.copy(alpha = 0.08f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Functions, contentDescription = null, tint = ColorPrimary)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = selectedFunction.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    text = selectedFunction.packageName,
                    style = MaterialTheme.typography.bodySmall,
                    color = ColorOnSurface.copy(alpha = 0.5f)
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "返回类型: ${selectedFunction.returnType}",
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = FontFamily.Monospace,
                    color = ColorPrimary
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        // 参数输入区
        Text(
            text = "参数输入",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(8.dp))

        selectedFunction.params.forEach { param ->
            val currentValue = paramInputs[param.name] ?: param.exampleValue
            OutlinedTextField(
                value = currentValue,
                onValueChange = { newValue ->
                    onIntent(
                        AppFunctionTestIntent.UpdateParamInput(
                            selectedFunction.id,
                            param.name,
                            newValue
                        )
                    )
                },
                label = {
                    Text(
                        text = "${param.name}${if (param.isOptional) " (可选)" else ""}",
                        style = MaterialTheme.typography.bodySmall
                    )
                },
                placeholder = { Text(param.exampleValue, fontFamily = FontFamily.Monospace, fontSize = 12.sp) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                textStyle = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace, fontSize = 13.sp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ColorPrimary,
                    cursorColor = ColorPrimary
                ),
                singleLine = true
            )
        }

        Spacer(Modifier.height(16.dp))

        // 执行按钮
        Button(
            onClick = {
                val params = paramInputs.mapValues { (_, v) ->
                    // 简单解析 JSON 值
                    try {
                        when {
                            v.startsWith("{") || v.startsWith("[") || v.startsWith("\"") -> v
                            v == "true" || v == "false" -> v
                            v.toLongOrNull() != null -> v.toLong()
                            v.toDoubleOrNull() != null -> v.toDouble()
                            else -> v
                        }
                    } catch (e: Exception) {
                        v
                    }
                }
                onIntent(AppFunctionTestIntent.ExecuteFunction(selectedFunction.id, params))
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !state.isExecuting,
            colors = ButtonDefaults.buttonColors(containerColor = ColorPrimary)
        ) {
            if (state.isExecuting) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
                Spacer(Modifier.width(8.dp))
                Text("执行中...")
            } else {
                Icon(Icons.Default.PlayArrow, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("执行 AppFunction")
            }
        }

        Spacer(Modifier.height(16.dp))

        // 测试结果
        state.testResult?.let { result ->
            TestResultCard(
                result = result,
                onClear = { onIntent(AppFunctionTestIntent.ClearTestResult) }
            )
        }

        Spacer(Modifier.height(32.dp))
    }
}

@Composable
private fun TestResultCard(
    result: AppFunctionTestResult,
    onClear: () -> Unit
) {
    val bgColor = if (result.isSuccess) ColorSecondary.copy(alpha = 0.08f) else ColorError.copy(alpha = 0.08f)
    val borderColor = if (result.isSuccess) ColorSecondary else ColorError

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, borderColor, RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = bgColor)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // 头部
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (result.isSuccess) Icons.Default.CheckCircle else Icons.Default.Error,
                        contentDescription = null,
                        tint = borderColor
                    )
                    Spacer(Modifier.width(8.dp))
                    Column {
                        Text(
                            text = if (result.isSuccess) "执行成功" else "执行失败",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = borderColor
                        )
                        Text(
                            text = "${result.functionName} (${result.durationMs}ms)",
                            style = MaterialTheme.typography.bodySmall,
                            color = ColorOnSurface.copy(alpha = 0.6f)
                        )
                    }
                }
                IconButton(onClick = onClear) {
                    Icon(Icons.Default.Clear, contentDescription = "清除")
                }
            }

            Spacer(Modifier.height(12.dp))

            // 输入参数
            Text(
                text = "输入参数:",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(4.dp))
            SelectionContainer {
                Text(
                    text = result.inputParams.entries.joinToString("\n") { "${it.key} = ${it.value}" },
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    color = ColorOnSurface.copy(alpha = 0.7f)
                )
            }

            Spacer(Modifier.height(12.dp))

            // 输出结果
            Text(
                text = "输出结果:",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(4.dp))
            SelectionContainer {
                Text(
                    text = result.outputResult,
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    color = if (result.isSuccess) ColorSecondary else ColorError
                )
            }

            // 错误信息
            result.errorMessage?.let { errorMsg ->
                Spacer(Modifier.height(12.dp))
                Text(
                    text = "错误: $errorMsg",
                    style = MaterialTheme.typography.bodySmall,
                    color = ColorError
                )
            }
        }
    }
}

// ============================================================
// Tab 3: Agent 模拟 / Agent Simulation Tab
// ============================================================

@Composable
private fun AgentSimTab(
    state: AppFunctionTestState,
    onIntent: (AppFunctionTestIntent) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(Modifier.height(16.dp))

        Text(
            text = "Agent 发现模拟",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "模拟不同 Agent 类型发现 AppFunction 的流程",
            style = MaterialTheme.typography.bodySmall,
            color = ColorOnSurface.copy(alpha = 0.6f)
        )

        Spacer(Modifier.height(16.dp))

        // Agent 类型选择
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AgentType.entries.forEach { agentType ->
                FilterChip(
                    selected = state.agentDiscoveryRecords.lastOrNull()?.agentType == agentType,
                    onClick = { onIntent(AppFunctionTestIntent.SimulateAgentDiscovery(agentType)) },
                    label = { Text(agentType.displayName) },
                    enabled = !state.isSimulatingAgent,
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = ColorPrimary.copy(alpha = 0.15f),
                        selectedLabelColor = ColorPrimary
                    )
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        // 执行按钮
        Button(
            onClick = {
                val lastAgent = state.agentDiscoveryRecords.lastOrNull()?.agentType ?: AgentType.GEMINI
                onIntent(AppFunctionTestIntent.SimulateAgentDiscovery(lastAgent))
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !state.isSimulatingAgent && state.functions.isNotEmpty(),
            colors = ButtonDefaults.buttonColors(containerColor = ColorPrimary)
        ) {
            if (state.isSimulatingAgent) {
                CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
                Spacer(Modifier.width(8.dp))
                Text("Agent 模拟中...")
            } else {
                Icon(Icons.Default.SmartToy, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("开始 Agent 模拟")
            }
        }

        Spacer(Modifier.height(16.dp))

        // 发现记录列表
        if (state.agentDiscoveryRecords.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.SmartToy,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = ColorOnSurface.copy(alpha = 0.2f)
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = "暂无模拟记录",
                        style = MaterialTheme.typography.bodyLarge,
                        color = ColorOnSurface.copy(alpha = 0.4f)
                    )
                    Text(
                        text = "选择一个 Agent 类型开始模拟",
                        style = MaterialTheme.typography.bodySmall,
                        color = ColorOnSurface.copy(alpha = 0.3f)
                    )
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(state.agentDiscoveryRecords) { record ->
                    AgentDiscoveryCard(record = record)
                }
            }
        }
    }
}

@Composable
private fun AgentDiscoveryCard(record: AgentDiscoveryRecord) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = ColorSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.SmartToy,
                        contentDescription = null,
                        tint = ColorPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Column {
                        Text(
                            text = record.agentType.displayName,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = record.agentType.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = ColorOnSurface.copy(alpha = 0.5f)
                        )
                    }
                }
                Text(
                    text = "${record.discoveryTimeMs}ms",
                    style = MaterialTheme.typography.bodySmall,
                    color = ColorOnSurface.copy(alpha = 0.5f)
                )
            }
            Spacer(Modifier.height(12.dp))
            Text(
                text = "发现 ${record.discoveredFunctions.size} 个函数:",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(4.dp))
            if (record.discoveredFunctions.isEmpty()) {
                Text(
                    text = "(无可见函数)",
                    style = MaterialTheme.typography.bodySmall,
                    color = ColorOnSurface.copy(alpha = 0.4f),
                    fontFamily = FontFamily.Monospace
                )
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    record.discoveredFunctions.take(6).forEach { funcName ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = ColorPrimary.copy(alpha = 0.08f))
                        ) {
                            Text(
                                text = funcName,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.bodySmall,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp,
                                color = ColorPrimary
                            )
                        }
                    }
                }
                if (record.discoveredFunctions.size > 6) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "+${record.discoveredFunctions.size - 6} 更多",
                        style = MaterialTheme.typography.bodySmall,
                        color = ColorOnSurface.copy(alpha = 0.5f)
                    )
                }
            }
        }
    }
}

// ============================================================
// Tab 4: 执行日志 / Execution Log Viewer Tab
// ============================================================

@Composable
private fun LogViewerTab(
    state: AppFunctionTestState,
    onIntent: (AppFunctionTestIntent) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "执行日志",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${state.executionLogs.size} 条记录",
                    style = MaterialTheme.typography.bodySmall,
                    color = ColorOnSurface.copy(alpha = 0.6f)
                )
            }
            if (state.executionLogs.isNotEmpty()) {
                Button(
                    onClick = { onIntent(AppFunctionTestIntent.ClearLogs) },
                    colors = ButtonDefaults.buttonColors(containerColor = ColorError.copy(alpha = 0.8f))
                ) {
                    Icon(Icons.Default.Clear, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("清空日志")
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        if (state.executionLogs.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.List,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = ColorOnSurface.copy(alpha = 0.2f)
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = "暂无执行日志",
                        style = MaterialTheme.typography.bodyLarge,
                        color = ColorOnSurface.copy(alpha = 0.4f)
                    )
                    Text(
                        text = "在「测试面板」中执行函数后会显示在这里",
                        style = MaterialTheme.typography.bodySmall,
                        color = ColorOnSurface.copy(alpha = 0.3f)
                    )
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(state.executionLogs, key = { "${it.functionId}_${it.timestamp}" }) { log ->
                    ExecutionLogCard(log = log)
                }
                item { Spacer(Modifier.height(16.dp)) }
            }
        }
    }
}

@Composable
private fun ExecutionLogCard(log: AppFunctionTestResult) {
    val borderColor = if (log.isSuccess) ColorSecondary else ColorError

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, borderColor.copy(alpha = 0.5f), RoundedCornerShape(8.dp)),
        colors = CardDefaults.cardColors(containerColor = ColorSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            // 状态图标
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(borderColor.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (log.isSuccess) Icons.Default.CheckCircle else Icons.Default.Error,
                    contentDescription = null,
                    tint = borderColor,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = log.functionName,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${log.durationMs}ms • ${formatTimestamp(log.timestamp)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = ColorOnSurface.copy(alpha = 0.5f)
                    )
                }
                Spacer(Modifier.height(4.dp))
                SelectionContainer {
                    Text(
                        text = log.outputResult,
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                log.errorMessage?.let { err ->
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "❗ $err",
                        style = MaterialTheme.typography.bodySmall,
                        color = ColorError,
                        maxLines = 2
                    )
                }
            }
        }
    }
}

// ============================================================
// 辅助函数 / Helper Functions
// ============================================================

private fun formatTimestamp(ts: Long): String {
    val sdf = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault())
    return sdf.format(java.util.Date(ts))
}