package com.mvi.kenny.feature.appfunctions

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import com.mvi.kenny.base.TopBarConfig
import com.mvi.kenny.base.TopBarAction
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

/**
 * ============================================================
 * AppFuncDesignToolScreen — AppFunctions 开发者工具包主界面
 * ============================================================
 * PRD-030 | AppFunctions 开发者工具包（Agent-Ready App 基础设施）
 *
 * 工具控制台风格深色主题界面，底部 Tab 导航：
 * - 校验台（VALIDATOR）：AppFunction 定义校验
 * - Mock 测试（MOCK）：本地 Mock Agent 测试
 * - 模板库（TEMPLATES）：场景模板生成器
 * - 兼容检测（COMPAT）：跨版本兼容性检测
 *
 * @param onUpdateTopBar TopBar 配置更新回调
 * @param viewModel ViewModel 实例
 *
 * @see AppFuncDesignToolContract MVI 契约定义
 */

/**
 * ============================================================
 * AppFuncDesignToolScreen — AppFunctions Developer Toolkit Main Screen
 * ============================================================
 * Tool console style dark theme interface with bottom tab navigation.
 *
 * Color scheme (GitHub Dark style):
 * - Background:     #0D1117
 * - Card:           #161B22
 * - Primary:        #58A6FF
 * - Success:        #3FB950
 * - Warning:        #D29922
 * - Error:          #F85149
 * - Text Primary:   #E6EDF3
 * - Text Secondary: #8B949E
 * - Border:         #30363D
 */

// ============================================================
// Theme Colors / 主题颜色（工具深色主题）
// ============================================================

private object AppFuncColors {
    val Background = Color(0xFF0D1117)
    val CardBackground = Color(0xFF161B22)
    val Primary = Color(0xFF58A6FF)
    val Success = Color(0xFF3FB950)
    val Warning = Color(0xFFD29922)
    val Error = Color(0xFFF85149)
    val TextPrimary = Color(0xFFE6EDF3)
    val TextSecondary = Color(0xFF8B949E)
    val Border = Color(0xFF30363D)
    val InputBackground = Color(0xFF0D1117)
}

// ============================================================
// Screen Entry Point / 屏幕入口
// ============================================================

@Composable
fun AppFuncDesignToolScreen(
    onUpdateTopBar: (TopBarConfig) -> Unit,
    viewModel: AppFuncDesignToolViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()

    // 更新 TopBar 配置 / Update TopBar config
    LaunchedEffect(state.selectedTab) {
        onUpdateTopBar(
            TopBarConfig(
                title = "AppFunctions 工具台",
                actions = listOf(
                    TopBarAction(
                        icon = Icons.Default.Refresh,
                        contentDescription = "重新扫描",
                        onClick = { viewModel.sendIntent(AppFuncDesignToolIntent.ScanFunctions) }
                    )
                )
            )
        )
    }

    // 处理副作用 / Handle effects
    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is AppFuncDesignToolEffect.ShowToast -> {
                    // Toast handled by the calling component
                }
                is AppFuncDesignToolEffect.NavigateToFunctionDetail -> {
                    // Navigation handled by the calling component
                }
                is AppFuncDesignToolEffect.TemplateApplied -> {
                    // Template applied feedback
                }
                is AppFuncDesignToolEffect.ValidationError -> {
                    // Validation error feedback
                }
            }
        }
    }

    Scaffold(
        containerColor = AppFuncColors.Background,
        bottomBar = {
            BottomNavigationBar(
                selectedTab = state.selectedTab,
                onTabSelected = { viewModel.sendIntent(AppFuncDesignToolIntent.SwitchTab(it)) }
            )
        },
        floatingActionButton = {
            when (state.selectedTab) {
                DesignTab.VALIDATOR -> FloatingActionButton(
                    onClick = { viewModel.sendIntent(AppFuncDesignToolIntent.ScanFunctions) },
                    containerColor = AppFuncColors.Primary
                ) {
                    if (state.isScanning) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(Icons.Default.Refresh, contentDescription = "重新扫描")
                    }
                }
                DesignTab.MOCK -> FloatingActionButton(
                    onClick = {
                        // FAB action for mock — start mock test for selected function
                        state.selectedFunction?.let {
                            viewModel.sendIntent(AppFuncDesignToolIntent.RunMockTest(it.id, "{}"))
                        }
                    },
                    containerColor = AppFuncColors.Primary
                ) {
                    if (state.isMockRunning) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(Icons.Default.SmartToy, contentDescription = "运行 Mock 测试")
                    }
                }
                else -> {}
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (state.selectedTab) {
                DesignTab.VALIDATOR -> ValidatorTab(state, viewModel)
                DesignTab.MOCK -> MockTestTab(state, viewModel)
                DesignTab.TEMPLATES -> TemplatesTab(state, viewModel)
                DesignTab.COMPAT -> CompatTab(state, viewModel)
            }
        }
    }
}

// ============================================================
// Bottom Navigation Bar / 底部导航栏
// ============================================================

@Composable
private fun BottomNavigationBar(
    selectedTab: DesignTab,
    onTabSelected: (DesignTab) -> Unit
) {
    NavigationBar(
        containerColor = AppFuncColors.CardBackground
    ) {
        DesignTab.entries.forEach { tab ->
            val icon = when (tab) {
                DesignTab.VALIDATOR -> Icons.Default.CheckCircle
                DesignTab.MOCK -> Icons.Default.SmartToy
                DesignTab.TEMPLATES -> Icons.Default.Inventory2
                DesignTab.COMPAT -> Icons.Default.Android
            }

            NavigationBarItem(
                icon = {
                    BadgedBox(
                        badge = {
                            // Show badge for validation errors
                            if (tab == DesignTab.VALIDATOR) {
                                val errorCount = 1 // stub
                                if (errorCount > 0) {
                                    Badge { Text("$errorCount") }
                                }
                            }
                        }
                    ) {
                        Icon(icon, contentDescription = tab.title)
                    }
                },
                label = { Text(tab.title) },
                selected = selectedTab == tab,
                onClick = { onTabSelected(tab) }
            )
        }
    }
}

// ============================================================
// Validator Tab / 校验台 Tab
// ============================================================

@Composable
private fun ValidatorTab(
    state: AppFuncDesignToolState,
    viewModel: AppFuncDesignToolViewModel
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Project Info Card / 项目信息卡片
        state.projectInfo?.let { info ->
            ProjectInfoCard(info)
            Spacer(modifier = Modifier.height(12.dp))
        }

        // Scanning indicator / 扫描指示器
        if (state.isScanning) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    color = AppFuncColors.Primary,
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "正在扫描 @AppFunction 标注项...",
                    color = AppFuncColors.TextSecondary,
                    fontSize = 14.sp
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Function List / 函数列表
        if (state.functionList.isEmpty() && !state.isScanning) {
            EmptyStateCard(
                icon = Icons.Default.CheckCircle,
                title = "尚未扫描",
                description = "点击右下角 🔄 按钮扫描项目中的 @AppFunction"
            )
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(state.functionList, key = { it.id }) { function ->
                    AppFunctionListCard(
                        function = function,
                        onSelect = { viewModel.sendIntent(AppFuncDesignToolIntent.SelectFunction(function)) },
                        onToggleVisibility = {
                            viewModel.sendIntent(AppFuncDesignToolIntent.ToggleAgentVisibility(function.id))
                        }
                    )
                }
            }
        }
    }
}

/**
 * 项目信息卡片
 * Display project info and scan summary
 */
@Composable
private fun ProjectInfoCard(info: ProjectInfo) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = AppFuncColors.CardBackground),
        shape = RoundedCornerShape(12.dp)
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
                    text = info.name,
                    color = AppFuncColors.TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(
                    text = "${info.moduleCount} 个模块",
                    color = AppFuncColors.TextSecondary,
                    fontSize = 12.sp
                )
            }
            Icon(
                Icons.Default.CheckCircle,
                contentDescription = null,
                tint = AppFuncColors.Success
            )
        }
    }
}

// ============================================================
// AppFunction List Card / 函数列表卡片
// ============================================================

@Composable
private fun AppFunctionListCard(
    function: AppFunctionItem,
    onSelect: () -> Unit,
    onToggleVisibility: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val borderColor = when (function.validationState) {
        ValidationState.PASS -> AppFuncColors.Success
        ValidationState.WARNING -> AppFuncColors.Warning
        ValidationState.ERROR -> AppFuncColors.Error
        ValidationState.UNCHECKED -> AppFuncColors.Border
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, borderColor, RoundedCornerShape(12.dp))
            .clickable {
                expanded = !expanded
                onSelect()
            },
        colors = CardDefaults.cardColors(containerColor = AppFuncColors.CardBackground),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header row / 头部行
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    // Validation badge / 校验状态徽章
                    Text(
                        text = function.validationState.symbol,
                        fontSize = 18.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = function.name,
                            color = AppFuncColors.TextPrimary,
                            fontWeight = FontWeight.Medium,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 14.sp
                        )
                        Text(
                            text = function.packageName,
                            color = AppFuncColors.TextSecondary,
                            fontSize = 11.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Agent visibility toggle / Agent 可见性开关
                    IconButton(
                        onClick = onToggleVisibility,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = if (function.agentVisibility)
                                Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = "Agent 可见性",
                            tint = if (function.agentVisibility)
                                AppFuncColors.Success else AppFuncColors.TextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    // Expand icon / 展开图标
                    Icon(
                        imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (expanded) "收起" else "展开",
                        tint = AppFuncColors.TextSecondary
                    )
                }
            }

            // Parameters preview / 参数预览行
            Row(
                modifier = Modifier.padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ValidationBadge(state = function.validationState)
                ParameterCountBadge(count = function.params.size)
                ReturnTypeBadge(type = function.returnType)
            }

            // Expanded details / 展开详情
            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column(
                    modifier = Modifier.padding(top = 12.dp)
                ) {
                    // Separator / 分隔线
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(AppFuncColors.Border)
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // Parameters table / 参数表格
                    Text(
                        "参数 (${function.params.size})",
                        color = AppFuncColors.TextSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    function.params.forEach { param ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row {
                                Text(
                                    text = param.name,
                                    color = AppFuncColors.Primary,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 12.sp
                                )
                                if (param.isOptional) {
                                    Text(
                                        text = "?",
                                        color = AppFuncColors.Warning,
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                            Text(
                                text = param.type.substringAfterLast("."),
                                color = AppFuncColors.TextSecondary,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 12.sp
                            )
                        }
                    }

                    // Return type / 返回值
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "返回值",
                            color = AppFuncColors.TextSecondary,
                            fontSize = 12.sp
                        )
                        Text(
                            function.returnType,
                            color = AppFuncColors.Success,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp
                        )
                    }

                    // Validation messages / 校验消息
                    if (function.validationState != ValidationState.PASS) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = when (function.validationState) {
                                ValidationState.WARNING -> "⚠️ 参数类型可能不兼容部分 Agent"
                                ValidationState.ERROR -> "❌ 缺少必要参数或返回类型不支持"
                                else -> ""
                            },
                            color = when (function.validationState) {
                                ValidationState.WARNING -> AppFuncColors.Warning
                                ValidationState.ERROR -> AppFuncColors.Error
                                else -> AppFuncColors.TextSecondary
                            },
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}

// ============================================================
// Badge Components / 徽章组件
// ============================================================

@Composable
private fun ValidationBadge(state: ValidationState) {
    Box(
        modifier = Modifier
            .background(
                color = Color(android.graphics.Color.parseColor(state.colorHex)).copy(alpha = 0.15f),
                shape = RoundedCornerShape(4.dp)
            )
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = state.label,
            color = Color(android.graphics.Color.parseColor(state.colorHex)),
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun ParameterCountBadge(count: Int) {
    Box(
        modifier = Modifier
            .background(
                color = AppFuncColors.Primary.copy(alpha = 0.15f),
                shape = RoundedCornerShape(4.dp)
            )
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = "$count 参数",
            color = AppFuncColors.Primary,
            fontSize = 11.sp
        )
    }
}

@Composable
private fun ReturnTypeBadge(type: String) {
    val shortType = type.substringAfterLast(".")
    Box(
        modifier = Modifier
            .background(
                color = AppFuncColors.Success.copy(alpha = 0.15f),
                shape = RoundedCornerShape(4.dp)
            )
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = shortType,
            color = AppFuncColors.Success,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace
        )
    }
}

// ============================================================
// Mock Test Tab / Mock 测试 Tab
// ============================================================

@Composable
private fun MockTestTab(
    state: AppFuncDesignToolState,
    viewModel: AppFuncDesignToolViewModel
) {
    var selectedFunctionId by remember { mutableStateOf<String?>(null) }
    var mockParams by remember { mutableStateOf("{}") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Function selector / 函数选择器
        Text(
            "选择函数",
            color = AppFuncColors.TextSecondary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(8.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(state.functionList) { func ->
                FilterChip(
                    selected = selectedFunctionId == func.id,
                    onClick = { selectedFunctionId = func.id },
                    label = { Text(func.name, fontFamily = FontFamily.Monospace, fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = AppFuncColors.Primary.copy(alpha = 0.2f)
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // JSON Params Editor / JSON 参数编辑器
        Text(
            "请求参数 (JSON)",
            color = AppFuncColors.TextSecondary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = mockParams,
            onValueChange = { mockParams = it },
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = AppFuncColors.Primary,
                unfocusedBorderColor = AppFuncColors.Border,
                focusedTextColor = AppFuncColors.TextPrimary,
                unfocusedTextColor = AppFuncColors.TextPrimary,
                cursorColor = AppFuncColors.Primary
            ),
            textStyle = androidx.compose.ui.text.TextStyle(fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace, fontSize = 13.sp),
            shape = RoundedCornerShape(8.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Run button / 运行按钮
        Button(
            onClick = {
                selectedFunctionId?.let {
                    viewModel.sendIntent(AppFuncDesignToolIntent.RunMockTest(it, mockParams))
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = AppFuncColors.Primary),
            enabled = selectedFunctionId != null && !state.isMockRunning
        ) {
            if (state.isMockRunning) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(if (state.isMockRunning) "测试中..." else "发送 Mock 请求")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // History header / 历史记录标题
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "最近调用 (${state.mockHistory.size})",
                color = AppFuncColors.TextSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
            if (state.mockHistory.isNotEmpty()) {
                Text(
                    "最近 20 条",
                    color = AppFuncColors.TextSecondary,
                    fontSize = 11.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Mock History List / Mock 历史记录列表
        if (state.mockHistory.isEmpty()) {
            EmptyStateCard(
                icon = Icons.Default.SmartToy,
                title = "尚无调用记录",
                description = "选择一个函数并发送 Mock 请求"
            )
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(state.mockHistory) { record ->
                    MockCallRecordCard(record)
                }
            }
        }
    }
}

@Composable
private fun MockCallRecordCard(record: MockCallRecord) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = AppFuncColors.CardBackground),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = record.functionName,
                    color = AppFuncColors.TextPrimary,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Medium,
                    fontSize = 13.sp
                )
                Text(
                    text = "${record.durationMs}ms",
                    color = if (record.isError) AppFuncColors.Error else AppFuncColors.Success,
                    fontSize = 12.sp
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = record.result,
                color = AppFuncColors.TextSecondary,
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

// ============================================================
// Templates Tab / 模板库 Tab
// ============================================================

@Composable
private fun TemplatesTab(
    state: AppFuncDesignToolState,
    viewModel: AppFuncDesignToolViewModel
) {
    var selectedCategory by remember { mutableStateOf<TemplateCategory?>(null) }
    var showApplyDialog by remember { mutableStateOf<FunctionTemplate?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Category filter / 分类筛选
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                FilterChip(
                    selected = selectedCategory == null,
                    onClick = { selectedCategory = null },
                    label = { Text("全部") }
                )
            }
            items(TemplateCategory.entries.toTypedArray()) { category ->
                FilterChip(
                    selected = selectedCategory == category,
                    onClick = { selectedCategory = category },
                    label = { Text(category.displayName) }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Templates list / 模板列表
        val filteredTemplates = if (selectedCategory != null) {
            state.templates.filter { it.category == selectedCategory }
        } else {
            state.templates
        }

        if (filteredTemplates.isEmpty()) {
            EmptyStateCard(
                icon = Icons.Default.Inventory2,
                title = "无模板",
                description = "当前分类下暂无模板"
            )
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredTemplates) { template ->
                    TemplateCard(
                        template = template,
                        onApply = { showApplyDialog = template }
                    )
                }
            }
        }
    }

    // Apply confirmation dialog / 应用确认对话框
    showApplyDialog?.let { template ->
        AlertDialog(
            onDismissRequest = { showApplyDialog = null },
            title = { Text("应用模板") },
            text = {
                Text("确定要将「${template.name}」应用到当前项目吗？\n\n" +
                    "这将创建 ${template.functionCount} 个 @AppFunction 标注的函数。\n\n" +
                    "⚠️ AppFunctions 为 alpha 版本 (1.0.0-alpha08)，API 可能不稳定。")
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.sendIntent(AppFuncDesignToolIntent.ApplyTemplate(template.id))
                        showApplyDialog = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AppFuncColors.Primary)
                ) {
                    Text("确认应用")
                }
            },
            dismissButton = {
                TextButton(onClick = { showApplyDialog = null }) {
                    Text("取消", color = AppFuncColors.TextSecondary)
                }
            },
            containerColor = AppFuncColors.CardBackground
        )
    }
}

@Composable
private fun TemplateCard(
    template: FunctionTemplate,
    onApply: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = AppFuncColors.CardBackground),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = template.name,
                        color = AppFuncColors.TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    Text(
                        text = template.category.displayName,
                        color = AppFuncColors.Primary,
                        fontSize = 11.sp
                    )
                }
                Button(
                    onClick = onApply,
                    colors = ButtonDefaults.buttonColors(containerColor = AppFuncColors.Primary),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("应用", fontSize = 12.sp)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = template.description,
                color = AppFuncColors.TextSecondary,
                fontSize = 13.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                template.tags.forEach { tag ->
                    Box(
                        modifier = Modifier
                            .background(
                                AppFuncColors.Border.copy(alpha = 0.5f),
                                RoundedCornerShape(4.dp)
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = tag,
                            color = AppFuncColors.TextSecondary,
                            fontSize = 10.sp
                        )
                    }
                }
            }
        }
    }
}

// ============================================================
// Compatibility Tab / 兼容检测 Tab
// ============================================================

@Composable
private fun CompatTab(
    state: AppFuncDesignToolState,
    viewModel: AppFuncDesignToolViewModel
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Version filter chips / 版本筛选
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Button(
                    onClick = {
                        viewModel.sendIntent(
                            AppFuncDesignToolIntent.RunCompatibilityCheck(listOf(36, 35, 34, 33))
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AppFuncColors.Primary),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("检测全部", fontSize = 12.sp)
                }
            }
            items(listOf(36, 35, 34, 33)) { version ->
                FilterChip(
                    selected = false,
                    onClick = {
                        viewModel.sendIntent(AppFuncDesignToolIntent.RunCompatibilityCheck(listOf(version)))
                    },
                    label = { Text("API $version") }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Note / 注意
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = AppFuncColors.Warning.copy(alpha = 0.1f)
            ),
            shape = RoundedCornerShape(8.dp)
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Warning,
                    contentDescription = null,
                    tint = AppFuncColors.Warning,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "AppFunctions 要求 Android 16+ (API 36)。检测结果仅供参考。",
                    color = AppFuncColors.Warning,
                    fontSize = 12.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Reports list / 报告列表
        if (state.isScanning) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    color = AppFuncColors.Primary,
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "正在检测兼容性...",
                    color = AppFuncColors.TextSecondary,
                    fontSize = 14.sp
                )
            }
        } else if (state.compatibilityReports.isEmpty()) {
            EmptyStateCard(
                icon = Icons.Default.Android,
                title = "尚未检测",
                description = "点击「检测全部」开始兼容性检测"
            )
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(state.compatibilityReports) { report ->
                    CompatibilityReportCard(report)
                }
            }
        }
    }
}

@Composable
private fun CompatibilityReportCard(report: CompatibilityReport) {
    val borderColor = when (report.state) {
        ValidationState.PASS -> AppFuncColors.Success
        ValidationState.WARNING -> AppFuncColors.Warning
        ValidationState.ERROR -> AppFuncColors.Error
        ValidationState.UNCHECKED -> AppFuncColors.Border
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, borderColor, RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = AppFuncColors.CardBackground),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = report.state.symbol,
                        fontSize = 20.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = report.versionName,
                            color = AppFuncColors.TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "API ${report.androidVersion}",
                            color = AppFuncColors.TextSecondary,
                            fontSize = 11.sp
                        )
                    }
                }
                ValidationBadge(state = report.state)
            }

            if (report.affectedFunctions.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "受影响函数: ${report.affectedFunctions.joinToString(", ")}",
                    color = AppFuncColors.Error,
                    fontSize = 11.sp
                )
            }

            if (report.suggestion.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = report.suggestion,
                    color = AppFuncColors.TextSecondary,
                    fontSize = 12.sp
                )
            }
        }
    }
}

// ============================================================
// Empty State / 空状态
// ============================================================

@Composable
private fun EmptyStateCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = AppFuncColors.CardBackground),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = AppFuncColors.TextSecondary,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = title,
                color = AppFuncColors.TextPrimary,
                fontWeight = FontWeight.Medium,
                fontSize = 15.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = description,
                color = AppFuncColors.TextSecondary,
                fontSize = 13.sp
            )
        }
    }
}


