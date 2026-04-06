package com.mvi.kenny.feature.appfunctions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

/**
 * ============================================================
 * AppFuncDesignToolViewModel — AppFunctions 开发者工具包状态管理
 * ============================================================
 * PRD-030 | AppFunctions 开发者工具包（Agent-Ready App 基础设施）
 *
 * MVI 架构核心流程：
 *
 *     ┌─────────────────────────────────────────────────────┐
 *     │                      View                           │
 *     │  (Composable — 消费 State，发送 Intent)             │
 *     └──────────────────────┬────────────────────────────┘
 *                            │ state.collectAsState()
 *                            │ intent.sendIntent()
 *                            ▼
 *     ┌─────────────────────────────────────────────────────┐
 *     │           AppFuncDesignToolViewModel                │
 *     │  - 接收 Intent                                       │
 *     │  - 执行 business logic（suspend functions）           │
 *     │  - 更新 _state（MutableStateFlow）                   │
 *     │  - 发送 _effect（Channel）                          │
 *     └──────────────────────┬────────────────────────────┘
 *                            │ state: StateFlow<AppFuncDesignToolState>
 *                            ▼
 *     ┌─────────────────────────────────────────────────────┐
 *     │                     Model                           │
 *     │  (AppFuncDesignToolState — Immutable data class)   │
 *     └─────────────────────────────────────────────────────┘
 *
 * @see AppFuncDesignToolState 页面状态定义
 * @see AppFuncDesignToolIntent 用户意图
 * @see AppFuncDesignToolEffect 副作用
 */

/**
 * ============================================================
 * AppFuncDesignToolViewModel — AppFunctions Developer Toolkit ViewModel
 * ============================================================
 * Manages AppFuncDesignToolState and AppFuncDesignToolEffect following MVI pattern.
 *
 * Key design decisions:
 * - StateFlow for state (hot stream, remembers last value)
 * - Channel for effects (hot stream, one-time events)
 * - viewModelScope.launch for coroutine management
 */

// ============================================================
// ViewModel
// ============================================================

class AppFuncDesignToolViewModel : ViewModel() {

    // ============================================================
    // State — 页面状态（StateFlow，UI 只读）
    // ============================================================
    private val _state = MutableStateFlow(AppFuncDesignToolState.Initial)
    val state: StateFlow<AppFuncDesignToolState> = _state.asStateFlow()

    /**
     * 当前状态的快照
     * Snapshot of current state for lambda access
     */
    val currentState: AppFuncDesignToolState get() = _state.value

    // ============================================================
    // Effect — 副作用（Channel，热流）
    // ============================================================
    private val _effect = Channel<AppFuncDesignToolEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    init {
        // 初始化 Mock 数据（演示用，⚠️ alpha 版本 API）
        initializeMockData()
    }

    // ============================================================
    // Intent 处理入口
    // ============================================================

    /**
     * 接收并处理用户意图
     * Dispatch center for all user intents
     *
     * @param intent 用户意图（非空）
     */
    fun sendIntent(intent: AppFuncDesignToolIntent) {
        when (intent) {
            is AppFuncDesignToolIntent.ScanFunctions -> scanFunctions()
            is AppFuncDesignToolIntent.SelectFunction -> selectFunction(intent.function)
            is AppFuncDesignToolIntent.RunMockTest -> runMockTest(intent.functionId, intent.params)
            is AppFuncDesignToolIntent.ApplyTemplate -> applyTemplate(intent.templateId)
            is AppFuncDesignToolIntent.RunCompatibilityCheck -> runCompatibilityCheck(intent.targetVersions)
            is AppFuncDesignToolIntent.SwitchTab -> switchTab(intent.tab)
            is AppFuncDesignToolIntent.ExpandFunction -> expandFunction(intent.functionId)
            is AppFuncDesignToolIntent.ToggleAgentVisibility -> toggleAgentVisibility(intent.functionId)
        }
    }

    // ============================================================
    // Intent 处理函数
    // ============================================================

    /**
     * 扫描项目中的 @AppFunction 标注项
     * Scan project for @AppFunction annotated items (stub: uses mock data)
     *
     * ⚠️ 注意：AppFunctions 为 alpha 版本（1.0.0-alpha08），API 可能不稳定
     * 真实实现需要 KSP/ASM 处理 @AppFunction 注解
     */
    private fun scanFunctions() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isScanning = true, error = null)

            try {
                // 模拟扫描延迟（真实场景需要编译时注解处理）
                delay(1500)

                // 模拟扫描到的 AppFunction 列表
                val scannedFunctions = listOf(
                    AppFunctionItem(
                        id = "func_001",
                        name = "createCalendarEvent",
                        packageName = "com.example.app.functions",
                        params = listOf(
                            FunctionParam("title", "String", false, "事件标题"),
                            FunctionParam("startTime", "Long", false, "开始时间（毫秒）"),
                            FunctionParam("endTime", "Long", true, "结束时间（毫秒）"),
                            FunctionParam("description", "String", true, "事件描述")
                        ),
                        returnType = "String (eventId)",
                        agentVisibility = true,
                        validationState = ValidationState.PASS
                    ),
                    AppFunctionItem(
                        id = "func_002",
                        name = "getTasks",
                        packageName = "com.example.app.functions",
                        params = listOf(
                            FunctionParam("filter", "TaskFilter", true, "任务过滤器"),
                            FunctionParam("limit", "Int", true, "返回数量限制")
                        ),
                        returnType = "List<Task>",
                        agentVisibility = true,
                        validationState = ValidationState.WARNING
                    ),
                    AppFunctionItem(
                        id = "func_003",
                        name = "searchNotes",
                        packageName = "com.example.app.functions",
                        params = listOf(
                            FunctionParam("query", "String", false, "搜索关键词"),
                            FunctionParam("maxResults", "Int", true, "最大结果数")
                        ),
                        returnType = "List<Note>",
                        agentVisibility = false,
                        validationState = ValidationState.ERROR
                    ),
                    AppFunctionItem(
                        id = "func_004",
                        name = "addToCart",
                        packageName = "com.example.app.functions",
                        params = listOf(
                            FunctionParam("productId", "String", false, "商品 ID"),
                            FunctionParam("quantity", "Int", false, "数量")
                        ),
                        returnType = "Boolean",
                        agentVisibility = true,
                        validationState = ValidationState.PASS
                    ),
                    AppFunctionItem(
                        id = "func_005",
                        name = "sendMessage",
                        packageName = "com.example.app.functions",
                        params = listOf(
                            FunctionParam("recipient", "String", false, "接收者 ID"),
                            FunctionParam("content", "String", false, "消息内容"),
                            FunctionParam("attachments", "List<String>", true, "附件路径列表")
                        ),
                        returnType = "MessageResult",
                        agentVisibility = true,
                        validationState = ValidationState.UNCHECKED
                    )
                )

                _state.value = _state.value.copy(
                    functionList = scannedFunctions,
                    isScanning = false,
                    projectInfo = ProjectInfo(
                        name = "MyMviProject",
                        path = "/Users/kenny/WorkSpace/AndroidStudioProjects/MyMviProject",
                        moduleCount = 3
                    )
                )

                _effect.send(AppFuncDesignToolEffect.ShowToast("扫描完成，发现 ${scannedFunctions.size} 个 @AppFunction"))

            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isScanning = false,
                    error = "扫描失败: ${e.message}"
                )
                _effect.send(AppFuncDesignToolEffect.ValidationError("扫描失败: ${e.message}"))
            }
        }
    }

    /**
     * 选中某个函数
     * Select a function to view its details
     *
     * @param function 选中的函数，null 表示取消选中
     */
    private fun selectFunction(function: AppFunctionItem?) {
        _state.value = _state.value.copy(selectedFunction = function)
        if (function != null) {
            viewModelScope.launch {
                _effect.send(AppFuncDesignToolEffect.NavigateToFunctionDetail(function.id))
            }
        }
    }

    /**
     * 运行 Mock 测试
     * Run mock test for a specific function (no real Gemini needed)
     *
     * @param functionId 函数 ID
     * @param params JSON 格式参数字符串
     */
    private fun runMockTest(functionId: String, params: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isMockRunning = true, error = null)

            try {
                // 模拟网络延迟
                delay(800)

                val function = _state.value.functionList.find { it.id == functionId }
                val functionName = function?.name ?: "unknown"

                // 模拟返回结果
                val mockResult = when {
                    functionName.contains("calendar", ignoreCase = true) ->
                        """{"eventId": "evt_123456", "status": "created", "message": "Event created successfully"}"""
                    functionName.contains("task", ignoreCase = true) ->
                        """{"tasks": [{"id": "task_001", "title": "Sample Task", "completed": false}], "total": 1}"""
                    functionName.contains("note", ignoreCase = true) ->
                        """{"notes": [{"id": "note_001", "title": "Sample Note", "preview": "..."}], "total": 1}"""
                    functionName.contains("cart", ignoreCase = true) ->
                        """{"success": true, "cartId": "cart_789", "itemCount": 1}"""
                    else ->
                        """{"success": true, "result": "mock_data"}"""
                }

                val record = MockCallRecord(
                    id = "mock_${System.currentTimeMillis()}",
                    functionId = functionId,
                    functionName = functionName,
                    params = params.ifEmpty { "{}" },
                    durationMs = (500..1000L).random(),
                    result = mockResult,
                    isError = false
                )

                val history = listOf(record) + _state.value.mockHistory.take(19)

                _state.value = _state.value.copy(
                    mockHistory = history,
                    isMockRunning = false
                )

                _effect.send(AppFuncDesignToolEffect.ShowToast("Mock 测试完成 (${record.durationMs}ms)"))

            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isMockRunning = false,
                    error = "Mock 测试失败: ${e.message}"
                )
                _effect.send(AppFuncDesignToolEffect.ShowToast("Mock 测试失败: ${e.message}"))
            }
        }
    }

    /**
     * 应用模板到当前项目
     * Apply a template to the current project (stub: shows confirmation)
     *
     * @param templateId 模板 ID
     */
    private fun applyTemplate(templateId: String) {
        viewModelScope.launch {
            val template = _state.value.templates.find { it.id == templateId }
            if (template != null) {
                _effect.send(AppFuncDesignToolEffect.ShowToast("正在应用模板: ${template.name}..."))
                delay(1000)
                _effect.send(AppFuncDesignToolEffect.TemplateApplied)
                _effect.send(AppFuncDesignToolEffect.ShowToast("${template.name} 应用成功！"))
            }
        }
    }

    /**
     * 运行兼容性检测
     * Run Android version compatibility check
     *
     * ⚠️ 注意：AppFunctions 要求 Android 16+（SDK 36）
     *
     * @param targetVersions 目标 Android 版本列表
     */
    private fun runCompatibilityCheck(targetVersions: List<Int>) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isScanning = true, error = null)

            try {
                // 模拟多版本检测延迟
                delay(2000)

                val reports = targetVersions.map { version ->
                    val (state, functions, suggestion) = when {
                        version >= 36 -> Triple(
                            ValidationState.PASS,
                            emptyList<String>(),
                            "完全兼容"
                        )
                        version >= 34 -> Triple(
                            ValidationState.WARNING,
                            listOf("sendMessage", "addToCart"),
                            "部分兼容：部分 API 需要降级处理"
                        )
                        else -> Triple(
                            ValidationState.ERROR,
                            listOf("createCalendarEvent", "getTasks", "searchNotes", "sendMessage", "addToCart"),
                            "不兼容：AppFunctions 要求 Android 16+ (SDK 36)"
                        )
                    }

                    CompatibilityReport(
                        id = "compat_${version}_${System.currentTimeMillis()}",
                        androidVersion = version,
                        versionName = getVersionName(version),
                        state = state,
                        affectedFunctions = functions,
                        suggestion = suggestion
                    )
                }

                _state.value = _state.value.copy(
                    compatibilityReports = reports,
                    isScanning = false
                )

                val passCount = reports.count { it.state == ValidationState.PASS }
                _effect.send(AppFuncDesignToolEffect.ShowToast("兼容性检测完成: $passCount/${reports.size} 版本通过"))

            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isScanning = false,
                    error = "兼容性检测失败: ${e.message}"
                )
            }
        }
    }

    /**
     * 切换底部 Tab
     * Switch bottom navigation tab
     *
     * @param tab 目标 Tab
     */
    private fun switchTab(tab: DesignTab) {
        _state.value = _state.value.copy(selectedTab = tab)
    }

    /**
     * 展开/折叠函数详情
     * Expand or collapse function details (toggles expanded state)
     *
     * @param functionId 函数 ID
     */
    private fun expandFunction(functionId: String) {
        // 在实际实现中，这里会切换展开状态
        // 目前 stub 实现不做状态持久化
    }

    /**
     * 切换函数的 Agent 可见性
     * Toggle function's Agent visibility
     *
     * @param functionId 函数 ID
     */
    private fun toggleAgentVisibility(functionId: String) {
        val functionList = _state.value.functionList.map { func ->
            if (func.id == functionId) {
                func.copy(agentVisibility = !func.agentVisibility)
            } else {
                func
            }
        }
        _state.value = _state.value.copy(functionList = functionList)

        val updated = functionList.find { it.id == functionId }
        viewModelScope.launch {
            val msg = if (updated?.agentVisibility == true) "已对 Agent 可见" else "已隐藏"
            _effect.send(AppFuncDesignToolEffect.ShowToast("${updated?.name} $msg"))
        }
    }

    // ============================================================
    // 辅助方法
    // ============================================================

    /**
     * 初始化 Mock 数据
     * Initialize mock data for demonstration
     *
     * ⚠️ 注意：AppFunctions 为 alpha 版本，模板仅作演示用途
     */
    private fun initializeMockData() {
        val templates = listOf(
            FunctionTemplate(
                id = "tpl_calendar",
                name = "日历场景模板",
                description = "暴露日历相关函数：创建事件、查询日程、修改事件、删除事件",
                category = TemplateCategory.CALENDAR,
                functionCount = 4,
                tags = listOf("calendar", "schedule", "event")
            ),
            FunctionTemplate(
                id = "tpl_notes",
                name = "笔记场景模板",
                description = "暴露笔记相关函数：创建笔记、搜索笔记、标签管理",
                category = TemplateCategory.NOTES,
                functionCount = 3,
                tags = listOf("notes", "search", "tags")
            ),
            FunctionTemplate(
                id = "tpl_tasks",
                name = "任务管理场景模板",
                description = "暴露任务管理函数：创建任务、完成任务、任务列表、过滤器",
                category = TemplateCategory.TASKS,
                functionCount = 4,
                tags = listOf("tasks", "todo", "productivity")
            ),
            FunctionTemplate(
                id = "tpl_shopping",
                name = "电商购物场景模板",
                description = "暴露购物车函数：添加商品、结算、查询订单、取消订单",
                category = TemplateCategory.SHOPPING,
                functionCount = 4,
                tags = listOf("shopping", "cart", "order")
            ),
            FunctionTemplate(
                id = "tpl_custom",
                name = "自定义模板",
                description = "从零开始定义 AppFunction，适合高级用户",
                category = TemplateCategory.CUSTOM,
                functionCount = 0,
                tags = listOf("custom", "advanced")
            )
        )

        _state.value = _state.value.copy(templates = templates)

        // 初始化兼容性报告（默认检测 4 个版本）
        viewModelScope.launch {
            sendIntent(AppFuncDesignToolIntent.RunCompatibilityCheck(listOf(36, 35, 34, 33)))
        }
    }

    /**
     * 将 Android 版本号转换为版本名称
     *
     * @param sdkVersion SDK 版本号
     * @return 版本名称字符串
     */
    private fun getVersionName(sdkVersion: Int): String = when (sdkVersion) {
        36 -> "Android 17 (API 36)"
        35 -> "Android 16 (API 35)"
        34 -> "Android 14 (API 34)"
        33 -> "Android 13 (API 33)"
        else -> "Android $sdkVersion (API $sdkVersion)"
    }
}
