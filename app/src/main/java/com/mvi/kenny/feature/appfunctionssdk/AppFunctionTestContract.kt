package com.mvi.kenny.feature.appfunctionssdk

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mvi.kenny.feature.appfunctions.ValidationState
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.util.UUID

/**
 * ============================================================
 * AppFunctionTestContract — AppFunctions SDK 测试工具 MVI 契约
 * ============================================================
 * PRD-185 | Android AppFunctions SDK 开发工具包（8个子工具）
 *
 * MVI 三要素：
 * - Model（State）：页面状态的唯一真相来源，Immutable 数据类
 * - View：Composable 函数，消费 State，渲染 UI
 * - Intent：用户意图，ViewModel 收到 Intent 后执行业务逻辑
 * - Effect：一次性副作用（Toast、导航），通过 Channel 传递
 * —————————————————————————————————————————————————————
 * This contract defines the in-app testing tool for AppFunctions SDK,
 * enabling developers to test and validate AppFunction implementations
 * locally before CI integration.
 *
 * Design reference: memory/agency/designs/PRD-185-AppFunctions-SDK-开发工具包.md
 *
 * @see AppFunctionTestViewModel 状态管理逻辑
 */

/**
 * ============================================================
 * AppFunctionTestContract — AppFunctions SDK Testing Tool MVI Contract
 * ============================================================
 * Architecture: MVI (Model-View-Intent)
 *
 * Testing Tool Tabs:
 * - FUNCTION_LIST → List all registered AppFunctions
 * - TEST_PANEL    → Test individual AppFunction with params
 * - AGENT_SIM     → Simulate Agent discovery and invocation
 * - LOG_VIEWER    → View execution logs
 */

// ============================================================
// Color System / 颜色系统
// ============================================================

/**
 * AppFunctions SDK 颜色规范
 *
 * @see PRD-185 Section 7 视觉规范
 */
object AppFunctionTestColors {
    val Primary = Color(0xFF4285F4)       // Google Blue
    val Secondary = Color(0xFF34A853)      // Google Green
    val Warning = Color(0xFFFBBC04)        // Google Yellow
    val Error = Color(0xFFEA4335)          // Google Red
    val Surface = Color(0xFFFFFFFF)
    val Background = Color(0xFFF8F9FA)
    val OnSurface = Color(0xFF202124)
}

// ============================================================
// Data Models / 数据模型
// ============================================================

/**
 * 测试工具底部 Tab 枚举
 *
 * @param title 中文标题
 * @param iconName 图标名称
 *
 * @author 开心果 🥜
 */
enum class AppFunctionTestTab(val title: String, val iconName: String) {
    FUNCTION_LIST("函数列表", "functions"),
    TEST_PANEL("测试面板", "play_arrow"),
    AGENT_SIM("Agent 模拟", "smart_toy"),
    LOG_VIEWER("执行日志", "list_alt")
}

/**
 * Agent 类型枚举
 *
 * @param displayName 显示名称
 * @param description 描述
 */
enum class AgentType(val displayName: String, val description: String) {
    GEMINI("Gemini", "Google Gemini AI Assistant"),
    ASSISTANT("Assistant", "Android Assistant with AppFunctions support"),
    CUSTOM("Custom", "Custom Agent implementation")
}

/**
 * 报告格式枚举
 *
 * @param extension 文件扩展名
 */
enum class ReportFormat(val extension: String) {
    MARKDOWN("md"),
    JSON("json"),
    HTML("html")
}

/**
 * AppFunction 列表项数据模型
 *
 * @param id 函数唯一标识
 * @param name 函数名称（Verb+Noun 模式，如 OrderFood, BookRide）
 * @param packageName 所属包名
 * @param params 参数列表
 * @param returnType 返回值类型
 * @param agentVisibility Agent 可见性
 * @param validationState 校验状态
 * @param isExpanded 是否展开
 *
 * @see ValidationState
 */
data class AppFunctionTestItem(
    val id: String,
    val name: String,
    val packageName: String,
    val params: List<AppFunctionTestParam>,
    val returnType: String,
    val agentVisibility: Boolean = true,
    val validationState: ValidationState = ValidationState.UNCHECKED,
    val isExpanded: Boolean = false
)

/**
 * 函数参数数据模型
 *
 * @param name 参数名称
 * @param type 参数类型（Fully qualified name）
 * @param isOptional 是否可选参数
 * @param description 参数描述
 * @param exampleValue 示例值
 */
data class AppFunctionTestParam(
    val name: String,
    val type: String,
    val isOptional: Boolean = false,
    val description: String = "",
    val exampleValue: String = ""
)

/**
 * 测试结果数据模型
 *
 * @param functionId 对应的函数 ID
 * @param functionName 函数名称
 * @param inputParams 输入参数
 * @param outputResult 输出结果（JSON）
 * @param durationMs 执行耗时（毫秒）
 * @param isSuccess 是否成功
 * @param errorMessage 错误信息（如果有）
 * @param timestamp 执行时间戳
 */
data class AppFunctionTestResult(
    val functionId: String,
    val functionName: String,
    val inputParams: Map<String, Any>,
    val outputResult: String,
    val durationMs: Long,
    val isSuccess: Boolean,
    val errorMessage: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Agent 发现记录数据模型
 *
 * @param agentType Agent 类型
 * @param discoveredFunctions 发现到的函数列表
 * @param discoveryTimeMs 发现耗时
 * @param timestamp 发现时间戳
 */
data class AgentDiscoveryRecord(
    val agentType: AgentType,
    val discoveredFunctions: List<String>,
    val discoveryTimeMs: Long,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * 合规报告数据模型
 *
 * @param totalFunctions 函数总数
 * @param passed 合规通过数量
 * @param warnings 警告数量
 * @param errors 错误数量
 * @param issues 问题和详情列表
 */
data class ComplianceReport(
    val totalFunctions: Int,
    val passed: Int,
    val warnings: Int,
    val errors: Int,
    val issues: List<ComplianceIssue> = emptyList(),
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * 合规问题数据模型
 *
 * @param functionId 函数 ID
 * @param severity 严重程度
 * @param message 问题描述
 * @param suggestion 修复建议
 */
data class ComplianceIssue(
    val functionId: String,
    val functionName: String,
    val severity: ComplianceSeverity,
    val message: String,
    val suggestion: String
)

/**
 * 合规严重程度枚举
 */
enum class ComplianceSeverity(val label: String, val colorHex: String) {
    PASS("通过", "#34A853"),
    WARNING("警告", "#FBBC04"),
    ERROR("错误", "#EA4335")
}

// ============================================================
// State / 页面状态
// ============================================================

/**
 * AppFunctions SDK 测试工具页面状态
 *
 * MVI 架构中的 Model 层，持有页面的所有状态。
 * 状态是 Immutable 的，每次状态变化都创建新的 State 对象。
 *
 * @param selectedTab 当前选中的底部 Tab
 * @param functions AppFunction 列表
 * @param selectedFunction 选中的函数（用于测试）
 * @param testResult 测试结果
 * @param executionLogs 执行日志列表
 * @param isLoading 是否加载中
 * @param complianceReport 合规报告
 * @param isExecuting 是否正在执行测试
 * @param isSimulatingAgent 是否正在模拟 Agent
 * @param agentDiscoveryRecords Agent 发现记录列表
 * @param paramInputs 参数输入 Map（functionId → Map<paramName, value>）
 * @param expandedFunctionIds 展开的函数 ID 集合
 * @param error 错误信息
 *
 * @see AppFunctionTestIntent 用户意图
 * @see AppFunctionTestViewModel 状态管理逻辑
 */
data class AppFunctionTestState(
    val selectedTab: AppFunctionTestTab = AppFunctionTestTab.FUNCTION_LIST,
    val functions: List<AppFunctionTestItem> = emptyList(),
    val selectedFunction: AppFunctionTestItem? = null,
    val testResult: AppFunctionTestResult? = null,
    val executionLogs: List<AppFunctionTestResult> = emptyList(),
    val isLoading: Boolean = false,
    val complianceReport: ComplianceReport? = null,
    val isExecuting: Boolean = false,
    val isSimulatingAgent: Boolean = false,
    val agentDiscoveryRecords: List<AgentDiscoveryRecord> = emptyList(),
    val paramInputs: Map<String, Map<String, String>> = emptyMap(),
    val expandedFunctionIds: Set<String> = emptySet(),
    val error: String? = null
) {
    companion object {
        /** 初始状态 */
        val Initial = AppFunctionTestState()
    }
}

// ============================================================
// Intent / 用户意图
// ============================================================

/**
 * AppFunctions SDK 测试工具页面用户意图（User Intent）
 * —————————————————————————————————————————————————————
 * 页面上的每一个用户操作都对应一个 Intent。
 * ViewModel 收到 Intent 后执行业务逻辑，然后更新 State。
 *
 * @see AppFunctionTestViewModel.sendIntent 处理所有 Intent
 */
sealed interface AppFunctionTestIntent {

    /**
     * 加载 AppFunctions 列表
     * Load list of registered AppFunctions
     */
    data object LoadFunctions : AppFunctionTestIntent

    /**
     * 选中某个函数，进入测试面板
     * Select a function to test
     *
     * @param id 函数 ID
     */
    data class SelectFunction(val id: String) : AppFunctionTestIntent

    /**
     * 执行指定的 AppFunction
     * Execute a specific AppFunction with given parameters
     *
     * @param id 函数 ID
     * @param params 参数 Map
     */
    data class ExecuteFunction(val id: String, val params: Map<String, Any>) : AppFunctionTestIntent

    /**
     * 模拟 Agent 发现 AppFunction
     * Simulate Agent discovering AppFunctions
     *
     * @param agentType Agent 类型
     */
    data class SimulateAgentDiscovery(val agentType: AgentType) : AppFunctionTestIntent

    /**
     * 运行合规检测
     * Run compliance check for all AppFunctions
     */
    data object RunComplianceCheck : AppFunctionTestIntent

    /**
     * 切换底部 Tab
     * Switch bottom navigation tab
     *
     * @param tab 目标 Tab
     */
    data class SwitchTab(val tab: AppFunctionTestTab) : AppFunctionTestIntent

    /**
     * 展开/折叠函数详情
     * Toggle function detail expansion
     *
     * @param functionId 函数 ID
     */
    data class ToggleFunctionExpand(val functionId: String) : AppFunctionTestIntent

    /**
     * 更新参数输入值
     * Update parameter input value
     *
     * @param functionId 函数 ID
     * @param paramName 参数名称
     * @param value 参数值
     */
    data class UpdateParamInput(
        val functionId: String,
        val paramName: String,
        val value: String
    ) : AppFunctionTestIntent

    /**
     * 清除测试结果
     * Clear test result
     */
    data object ClearTestResult : AppFunctionTestIntent

    /**
     * 清除执行日志
     * Clear execution logs
     */
    data object ClearLogs : AppFunctionTestIntent
}

// ============================================================
// Effect / 副作用
// ============================================================

/**
 * AppFunctions SDK 测试工具页面副作用（Effect）
 * —————————————————————————————————————————————————————
 * 一次性事件，不可变，只能被消费一次。
 * UI 层通过 LaunchedEffect + flow.collect{} 监听并处理。
 *
 * @see AppFunctionTestViewModel 中通过 _effect.send() 发送 Effect
 */
sealed interface AppFunctionTestEffect {

    /**
     * 显示 Toast
     * Show a toast message
     *
     * @param message Toast 文本内容
     */
    data class ShowToast(val message: String) : AppFunctionTestEffect

    /**
     * 导航到指定函数
     * Navigate to a specific function's test panel
     *
     * @param functionId 函数 ID
     */
    data class NavigateToFunction(val functionId: String) : AppFunctionTestEffect

    /**
     * 导出报告
     * Export compliance report in specified format
     *
     * @param format 报告格式
     */
    data class ExportReport(val format: ReportFormat) : AppFunctionTestEffect
}

// ============================================================
// ViewModel
// ============================================================

/**
 * AppFunctionTestViewModel — AppFunctions SDK 测试工具状态管理
 * —————————————————————————————————————————————————————
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
 *     │           AppFunctionTestViewModel                 │
 *     │  - 接收 Intent                                       │
 *     │  - 执行 business logic（suspend functions）           │
 *     │  - 更新 _state（MutableStateFlow）                   │
 *     │  - 发送 _effect（Channel）                          │
 *     └──────────────────────┬────────────────────────────┘
 *                            │ state: StateFlow<AppFunctionTestState>
 *                            ▼
 *     ┌─────────────────────────────────────────────────────┐
 *     │                     Model                           │
 *     │  (AppFunctionTestState — Immutable data class)    │
 *     └─────────────────────────────────────────────────────┘
 *
 * @see AppFunctionTestState 页面状态定义
 * @see AppFunctionTestIntent 用户意图
 * @see AppFunctionTestEffect 副作用
 */
class AppFunctionTestViewModel : ViewModel() {

    // ============================================================
    // State — 页面状态（StateFlow，UI 只读）
    // ============================================================
    private val _state = MutableStateFlow(AppFunctionTestState.Initial)
    val state: StateFlow<AppFunctionTestState> = _state.asStateFlow()

    /**
     * 当前状态的快照
     * Snapshot of current state for lambda access
     */
    val currentState: AppFunctionTestState get() = _state.value

    // ============================================================
    // Effect — 副作用（Channel，热流）
    // ============================================================
    private val _effect = Channel<AppFunctionTestEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    init {
        // 初始化 Mock 数据
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
    fun sendIntent(intent: AppFunctionTestIntent) {
        when (intent) {
            is AppFunctionTestIntent.LoadFunctions -> loadFunctions()
            is AppFunctionTestIntent.SelectFunction -> selectFunction(intent.id)
            is AppFunctionTestIntent.ExecuteFunction -> executeFunction(intent.id, intent.params)
            is AppFunctionTestIntent.SimulateAgentDiscovery -> simulateAgentDiscovery(intent.agentType)
            is AppFunctionTestIntent.RunComplianceCheck -> runComplianceCheck()
            is AppFunctionTestIntent.SwitchTab -> switchTab(intent.tab)
            is AppFunctionTestIntent.ToggleFunctionExpand -> toggleFunctionExpand(intent.functionId)
            is AppFunctionTestIntent.UpdateParamInput -> updateParamInput(
                intent.functionId,
                intent.paramName,
                intent.value
            )
            is AppFunctionTestIntent.ClearTestResult -> clearTestResult()
            is AppFunctionTestIntent.ClearLogs -> clearLogs()
        }
    }

    // ============================================================
    // Intent 处理函数
    // ============================================================

    /**
     * 加载 AppFunctions 列表
     * Load AppFunctions list (mock data for demo)
     *
     * ⚠️ 真实实现需要 KSP/ASM 处理 @AppFunction 注解
     */
    private fun loadFunctions() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)

            try {
                delay(1200) // 模拟扫描延迟

                val mockFunctions = listOf(
                    AppFunctionTestItem(
                        id = "sdk_func_001",
                        name = "OrderFood",
                        packageName = "com.example.app.functions",
                        params = listOf(
                            AppFunctionTestParam("restaurantId", "String", false, "餐厅 ID", "\"rest_123\""),
                            AppFunctionTestParam("items", "List<OrderItem>", false, "订单项列表", "[{\"id\":\"item_1\",\"qty\":2}]"),
                            AppFunctionTestParam("deliveryAddress", "String", true, "配送地址", "\"123 Main St\""),
                            AppFunctionTestParam("couponCode", "String", true, "优惠码", "\"SAVE10\"")
                        ),
                        returnType = "OrderResult",
                        agentVisibility = true,
                        validationState = ValidationState.PASS
                    ),
                    AppFunctionTestItem(
                        id = "sdk_func_002",
                        name = "BookRide",
                        packageName = "com.example.app.functions",
                        params = listOf(
                            AppFunctionTestParam("pickupLocation", "Location", false, "上车地点", "{\"lat\":37.7749,\"lng\":-122.4194}"),
                            AppFunctionTestParam("dropoffLocation", "Location", false, "下车地点", "{\"lat\":37.3382,\"lng\":-121.8863}"),
                            AppFunctionTestParam("vehicleType", "String", true, "车型", "\"comfort\""),
                            AppFunctionTestParam("scheduledTime", "Long", true, "预约时间戳", "1746000000000")
                        ),
                        returnType = "RideBooking",
                        agentVisibility = true,
                        validationState = ValidationState.PASS
                    ),
                    AppFunctionTestItem(
                        id = "sdk_func_003",
                        name = "SearchProducts",
                        packageName = "com.example.app.functions",
                        params = listOf(
                            AppFunctionTestParam("query", "String", false, "搜索关键词", "\"wireless headphones\""),
                            AppFunctionTestParam("maxResults", "Int", true, "最大结果数", "20"),
                            AppFunctionTestParam("category", "String", true, "商品分类", "\"electronics\""),
                            AppFunctionTestParam("priceRange", "PriceRange", true, "价格区间", "{\"min\":10,\"max\":500}")
                        ),
                        returnType = "List<Product>",
                        agentVisibility = true,
                        validationState = ValidationState.WARNING
                    ),
                    AppFunctionTestItem(
                        id = "sdk_func_004",
                        name = "PlayMedia",
                        packageName = "com.example.app.functions",
                        params = listOf(
                            AppFunctionTestParam("mediaId", "String", false, "媒体 ID", "\"media_abc123\""),
                            AppFunctionTestParam("position", "Long", true, "播放位置（毫秒）", "0"),
                            AppFunctionTestParam("volume", "Float", true, "音量 (0.0-1.0)", "0.8")
                        ),
                        returnType = "PlaybackState",
                        agentVisibility = false,
                        validationState = ValidationState.UNCHECKED
                    ),
                    AppFunctionTestItem(
                        id = "sdk_func_005",
                        name = "CreateReminder",
                        packageName = "com.example.app.functions",
                        params = listOf(
                            AppFunctionTestParam("title", "String", false, "提醒标题", "\"Take medication\""),
                            AppFunctionTestParam("reminderTime", "Long", false, "提醒时间戳", "1746086400000"),
                            AppFunctionTestParam("repeatInterval", "Duration", true, "重复间隔", "\"P1D\""),
                            AppFunctionTestParam("notes", "String", true, "备注", "\"Before breakfast\"")
                        ),
                        returnType = "Reminder",
                        agentVisibility = true,
                        validationState = ValidationState.PASS
                    ),
                    AppFunctionTestItem(
                        id = "sdk_func_006",
                        name = "GetOrderStatus",
                        packageName = "com.example.app.functions",
                        params = listOf(
                            AppFunctionTestParam("orderId", "String", false, "订单 ID", "\"order_xyz789\""),
                            AppFunctionTestParam("includeHistory", "Boolean", true, "是否包含历史", "true")
                        ),
                        returnType = "OrderStatus",
                        agentVisibility = true,
                        validationState = ValidationState.PASS
                    ),
                    AppFunctionTestItem(
                        id = "sdk_func_007",
                        name = "CancelOrder",
                        packageName = "com.example.app.functions",
                        params = listOf(
                            AppFunctionTestParam("orderId", "String", false, "订单 ID", "\"order_xyz789\""),
                            AppFunctionTestParam("reason", "String", true, "取消原因", "\"Changed my mind\"")
                        ),
                        returnType = "CancellationResult",
                        agentVisibility = true,
                        validationState = ValidationState.ERROR
                    ),
                    AppFunctionTestItem(
                        id = "sdk_func_008",
                        name = "ScheduleAppointment",
                        packageName = "com.example.app.functions",
                        params = listOf(
                            AppFunctionTestParam("serviceType", "String", false, "服务类型", "\"haircut\""),
                            AppFunctionTestParam("providerId", "String", false, "服务商 ID", "\"provider_123\""),
                            AppFunctionTestParam("slotTime", "Long", false, "预约时间戳", "1746172800000"),
                            AppFunctionTestParam("notes", "String", true, "备注", "")
                        ),
                        returnType = "Appointment",
                        agentVisibility = true,
                        validationState = ValidationState.PASS
                    )
                )

                _state.value = _state.value.copy(
                    functions = mockFunctions,
                    isLoading = false
                )

                _effect.send(AppFunctionTestEffect.ShowToast("加载完成，共 ${mockFunctions.size} 个 AppFunction"))

            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = "加载失败: ${e.message}"
                )
                _effect.send(AppFunctionTestEffect.ShowToast("加载失败: ${e.message}"))
            }
        }
    }

    /**
     * 选中某个函数
     * Select a function to test
     *
     * @param id 函数 ID
     */
    private fun selectFunction(id: String) {
        val function = _state.value.functions.find { it.id == id }
        _state.value = _state.value.copy(
            selectedFunction = function,
            selectedTab = AppFunctionTestTab.TEST_PANEL
        )
        if (function != null) {
            viewModelScope.launch {
                _effect.send(AppFunctionTestEffect.ShowToast("已选择: ${function.name}"))
            }
        }
    }

    /**
     * 执行 AppFunction
     * Execute AppFunction with given parameters
     *
     * ⚠️ 注意：设备上执行，无网络请求
     *
     * @param id 函数 ID
     * @param params 参数 Map
     */
    private fun executeFunction(id: String, params: Map<String, Any>) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isExecuting = true, error = null)

            try {
                delay(600 + (Math.random() * 400).toLong()) // 模拟执行延迟

                val function = _state.value.functions.find { it.id == id }
                val functionName = function?.name ?: "Unknown"

                // 根据函数名模拟返回结果
                val mockResult = when {
                    functionName == "OrderFood" -> """
                    {
                        "orderId": "ord_${UUID.randomUUID().toString().take(8)}",
                        "status": "confirmed",
                        "estimatedDelivery": "2026-04-28T12:30:00Z",
                        "totalPrice": 45.99,
                        "restaurantName": "Golden Dragon"
                    }
                    """.trimIndent()

                    functionName == "BookRide" -> """
                    {
                        "bookingId": "ride_${UUID.randomUUID().toString().take(8)}",
                        "status": "matched",
                        "driverName": "John D.",
                        "vehicleInfo": "Toyota Camry - ABC 123",
                        "etaMinutes": 4
                    }
                    """.trimIndent()

                    functionName == "SearchProducts" -> """
                    {
                        "products": [
                            {"id": "p1", "name": "Wireless Headphones Pro", "price": 79.99, "rating": 4.5},
                            {"id": "p2", "name": "Bluetooth Earbuds", "price": 49.99, "rating": 4.3}
                        ],
                        "totalResults": 2,
                        "page": 1
                    }
                    """.trimIndent()

                    functionName == "PlayMedia" -> """
                    {
                        "state": "playing",
                        "mediaId": "${params["mediaId"] ?: "unknown"}",
                        "positionMs": ${params["position"] ?: 0},
                        "volume": ${params["volume"] ?: 0.8f}
                    }
                    """.trimIndent()

                    functionName == "CreateReminder" -> """
                    {
                        "reminderId": "rem_${UUID.randomUUID().toString().take(8)}",
                        "title": "${params["title"] ?: ""}",
                        "scheduledTime": ${params["reminderTime"] ?: 0},
                        "status": "active"
                    }
                    """.trimIndent()

                    functionName == "GetOrderStatus" -> """
                    {
                        "orderId": "${params["orderId"] ?: ""}",
                        "status": "in_transit",
                        "progress": 0.65,
                        "estimatedArrival": "2026-04-28T11:45:00Z"
                    }
                    """.trimIndent()

                    functionName == "CancelOrder" -> """
                    {
                        "success": false,
                        "error": "ORDER_CANNOT_BE_CANCELLED",
                        "message": "This order is already being prepared and cannot be cancelled."
                    }
                    """.trimIndent()

                    functionName == "ScheduleAppointment" -> """
                    {
                        "appointmentId": "apt_${UUID.randomUUID().toString().take(8)}",
                        "status": "confirmed",
                        "serviceType": "${params["serviceType"] ?: ""}",
                        "scheduledTime": ${params["slotTime"] ?: 0}
                    }
                    """.trimIndent()

                    else -> """{"success": true, "result": "mock_data"}"""
                }

                val isSuccess = functionName != "CancelOrder"
                val durationMs = (600 + (Math.random() * 400)).toLong()

                val result = AppFunctionTestResult(
                    functionId = id,
                    functionName = functionName,
                    inputParams = params,
                    outputResult = mockResult,
                    durationMs = durationMs,
                    isSuccess = isSuccess,
                    errorMessage = if (!isSuccess) "Order cannot be cancelled (already preparing)" else null
                )

                val updatedLogs = listOf(result) + _state.value.executionLogs.take(49)

                _state.value = _state.value.copy(
                    testResult = result,
                    executionLogs = updatedLogs,
                    isExecuting = false
                )

                _effect.send(
                    AppFunctionTestEffect.ShowToast(
                        if (isSuccess) "执行成功 (${durationMs}ms)" else "执行失败"
                    )
                )

            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isExecuting = false,
                    error = "执行失败: ${e.message}"
                )
                _effect.send(AppFunctionTestEffect.ShowToast("执行失败: ${e.message}"))
            }
        }
    }

    /**
     * 模拟 Agent 发现 AppFunction
     * Simulate Agent discovering AppFunctions
     *
     * @param agentType Agent 类型
     */
    private fun simulateAgentDiscovery(agentType: AgentType) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isSimulatingAgent = true, error = null)

            try {
                delay(1500 + (Math.random() * 500).toLong()) // 模拟 Agent 发现延迟

                val visibleFunctions = _state.value.functions.filter { it.agentVisibility }
                val discoveredNames = when (agentType) {
                    AgentType.GEMINI -> visibleFunctions.map { it.name }.sorted()
                    AgentType.ASSISTANT -> visibleFunctions
                        .filter { it.name !in listOf("OrderFood", "CancelOrder") }
                        .map { it.name }.sorted()
                    AgentType.CUSTOM -> visibleFunctions
                        .filter { it.validationState == ValidationState.PASS }
                        .map { it.name }.sorted()
                }

                val record = AgentDiscoveryRecord(
                    agentType = agentType,
                    discoveredFunctions = discoveredNames,
                    discoveryTimeMs = (1500 + (Math.random() * 500)).toLong(),
                    timestamp = System.currentTimeMillis()
                )

                val updatedRecords = listOf(record) + _state.value.agentDiscoveryRecords.take(9)

                _state.value = _state.value.copy(
                    agentDiscoveryRecords = updatedRecords,
                    isSimulatingAgent = false
                )

                _effect.send(
                    AppFunctionTestEffect.ShowToast(
                        "${agentType.displayName} 发现了 ${discoveredNames.size} 个函数"
                    )
                )

            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isSimulatingAgent = false,
                    error = "Agent 模拟失败: ${e.message}"
                )
                _effect.send(AppFunctionTestEffect.ShowToast("Agent 模拟失败: ${e.message}"))
            }
        }
    }

    /**
     * 运行合规检测
     * Run compliance check for all AppFunctions
     */
    private fun runComplianceCheck() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)

            try {
                delay(2000) // 模拟检测延迟

                val functions = _state.value.functions
                var passed = 0
                var warnings = 0
                var errors = 0
                val issues = mutableListOf<ComplianceIssue>()

                functions.forEach { func ->
                    when (func.validationState) {
                        ValidationState.PASS -> {
                            passed++
                        }
                        ValidationState.WARNING -> {
                            warnings++
                            issues.add(
                                ComplianceIssue(
                                    functionId = func.id,
                                    functionName = func.name,
                                    severity = ComplianceSeverity.WARNING,
                                    message = "参数 ${func.params.firstOrNull()?.name ?: "unknown"} 类型未完全适配",
                                    suggestion = "建议使用 Parcelable 类型以提高兼容性"
                                )
                            )
                        }
                        ValidationState.ERROR -> {
                            errors++
                            issues.add(
                                ComplianceIssue(
                                    functionId = func.id,
                                    functionName = func.name,
                                    severity = ComplianceSeverity.ERROR,
                                    message = "@AppFunction 注解配置不正确或参数不符合规范",
                                    suggestion = "检查 @AppFunction 注解是否在 appfunctions 闭包内，参数是否实现 Parcelable"
                                )
                            )
                        }
                        ValidationState.UNCHECKED -> {
                            warnings++
                            issues.add(
                                ComplianceIssue(
                                    functionId = func.id,
                                    functionName = func.name,
                                    severity = ComplianceSeverity.WARNING,
                                    message = "函数尚未进行合规检测",
                                    suggestion = "请在 CI 中运行 ./gradlew detectAppFunctions 进行检测"
                                )
                            )
                        }
                    }
                }

                val report = ComplianceReport(
                    totalFunctions = functions.size,
                    passed = passed,
                    warnings = warnings,
                    errors = errors,
                    issues = issues
                )

                _state.value = _state.value.copy(
                    complianceReport = report,
                    isLoading = false
                )

                _effect.send(
                    AppFunctionTestEffect.ShowToast(
                        "合规检测完成: $passed 通过, $warnings 警告, $errors 错误"
                    )
                )

            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = "合规检测失败: ${e.message}"
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
    private fun switchTab(tab: AppFunctionTestTab) {
        _state.value = _state.value.copy(selectedTab = tab)
    }

    /**
     * 展开/折叠函数详情
     * Toggle function detail expansion
     *
     * @param functionId 函数 ID
     */
    private fun toggleFunctionExpand(functionId: String) {
        val currentExpanded = _state.value.expandedFunctionIds
        val updated = if (functionId in currentExpanded) {
            currentExpanded - functionId
        } else {
            currentExpanded + functionId
        }
        _state.value = _state.value.copy(expandedFunctionIds = updated)
    }

    /**
     * 更新参数输入值
     * Update parameter input value for a function
     *
     * @param functionId 函数 ID
     * @param paramName 参数名称
     * @param value 参数值
     */
    private fun updateParamInput(functionId: String, paramName: String, value: String) {
        val currentInputs = _state.value.paramInputs.toMutableMap()
        val functionInputs = currentInputs[functionId]?.toMutableMap() ?: mutableMapOf()
        functionInputs[paramName] = value
        currentInputs[functionId] = functionInputs
        _state.value = _state.value.copy(paramInputs = currentInputs)
    }

    /**
     * 清除测试结果
     * Clear current test result
     */
    private fun clearTestResult() {
        _state.value = _state.value.copy(testResult = null)
    }

    /**
     * 清除执行日志
     * Clear execution logs
     */
    private fun clearLogs() {
        viewModelScope.launch {
            _state.value = _state.value.copy(executionLogs = emptyList())
            _effect.send(AppFunctionTestEffect.ShowToast("日志已清除"))
        }
    }

    // ============================================================
    // Mock 数据初始化
    // ============================================================

    /**
     * 初始化 Mock 数据
     * Initialize mock data for demonstration
     */
    private fun initializeMockData() {
        // 触发加载
        sendIntent(AppFunctionTestIntent.LoadFunctions)
    }
}
