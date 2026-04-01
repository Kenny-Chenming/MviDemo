package com.mvi.kenny.feature.mcp

/**
 * ============================================================
 * McpContract — Android MCP Server 工具包 MVI 契约
 * ============================================================
 * PRD-011 | Android MCP Server 工具包
 *
 * MVI 三要素：
 * - Model（State）：页面状态的唯一真相来源，Immutable 数据类
 * - View：Composable 函数，消费 State，渲染 UI
 * - Intent：用户意图（用户操作），ViewModel 收到 Intent 后执行业务逻辑
 *
 * Effect：一次性副作用（导航、Toast），通过 Channel 传递
 * —————————————————————————————————————————————————————
 */

/** 设备类型枚举（跨设备桥接用） */
enum class DeviceType {
    PHONE,  // 手机
    TABLET, // 平板
    TV,     // 电视
    XR      // XR 眼镜
}

/** MCP Server 运行状态 */
enum class ServerStatus {
    IDLE,      // 空闲/已停止
    STARTING,  // 启动中
    RUNNING,   // 运行中
    STOPPING,  // 停止中
    ERROR      // 错误状态
}

/** 日志条目 */
data class LogEntry(
    val id: Long,
    val timestamp: String,
    val level: LogLevel,
    val message: String
)

/** 日志级别 */
enum class LogLevel {
    INFO,   // 正常日志（绿色）
    ERROR   // 错误日志（红色）
}

/** MCP 工具 */
data class McpTool(
    val id: String,
    val name: String,
    val description: String,
    val enabled: Boolean,
    val callHistory: List<ToolCall> = emptyList()
)

/** 工具调用记录 */
data class ToolCall(
    val id: Long,
    val timestamp: String,
    val params: Map<String, String>,
    val rawOutput: String,
    val success: Boolean
)

/** Android 设备（跨设备桥接） */
data class AndroidDevice(
    val id: String,
    val name: String,
    val type: DeviceType,
    val connected: Boolean
)

/** MCP 设置项 */
data class McpSettings(
    val serverAddress: String = "localhost",
    val serverPort: Int = 3456,
    val autoStart: Boolean = false,
    val whitelistMode: Boolean = false,
    val logLevel: LogLevel = LogLevel.INFO
)

/**
 * MCP Server 页面状态
 * —————————————————————————————————————————————————————
 *
 * @param serverStatus 当前 Server 运行状态
 * @param serverPort Server 监听端口（默认 3456）
 * @param tools 可用工具列表（来自 MCP Server）
 * @param selectedTool 当前选中的工具（用于详情 BottomSheet）
 * @param connectionLogs 实时日志列表（保留最新 100 条）
 * @param devices 跨设备桥接设备列表
 * @param settings 用户配置
 * @param isLoading 是否正在调用工具
 * @param errorMessage 错误信息，null 表示无错误
 */
data class McpState(
    val serverStatus: ServerStatus = ServerStatus.IDLE,
    val serverPort: Int = 3456,
    val tools: List<McpTool> = emptyList(),
    val selectedTool: McpTool? = null,
    val connectionLogs: List<LogEntry> = emptyList(),
    val devices: List<AndroidDevice> = emptyList(),
    val settings: McpSettings = McpSettings(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
) {
    companion object {
        /** 初始状态 */
        val Initial = McpState()
    }
}

/**
 * MCP Server 用户意图（User Intent）
 * —————————————————————————————————————————————————————
 * 页面上的每一个用户操作都对应一个 Intent。
 * ViewModel 收到 Intent 后执行业务逻辑，然后更新 State。
 *
 * @see McpViewModel.sendIntent 处理所有 Intent
 */
sealed interface McpIntent {
    /** 启动 MCP Server */
    data object StartServer : McpIntent

    /** 停止 MCP Server */
    data object StopServer : McpIntent

    /** 切换工具启用状态
     * @param toolId 工具 ID
     * @param enabled 是否启用
     */
    data class ToggleTool(val toolId: String, val enabled: Boolean) : McpIntent

    /** 选中工具（打开详情 Sheet）
     * @param tool 被选中的工具
     */
    data class SelectTool(val tool: McpTool) : McpIntent

    /** 关闭工具详情 Sheet */
    data object DismissToolDetail : McpIntent

    /** 调用工具（测试调用）
     * @param toolId 工具 ID
     * @param params 调用参数
     */
    data class CallTool(val toolId: String, val params: Map<String, String>) : McpIntent

    /** 刷新设备列表（跨设备桥接） */
    data object RefreshDevices : McpIntent

    /** 更新设置
     * @param settings 新的设置
     */
    data class UpdateSettings(val settings: McpSettings) : McpIntent
}

/**
 * MCP Server 副作用（Effect）
 * —————————————————————————————————————————————————————
 * 一次性事件，不可变，只能被消费一次。
 * UI 层通过 LaunchedEffect + flow.collect{} 监听并处理。
 *
 * @see McpViewModel 中通过 _effect.send() 发送 Effect
 */
sealed interface McpEffect {
    /** 显示 Toast
     * @param message Toast 文本
     */
    data class ShowToast(val message: String) : McpEffect

    /** 工具调用结果
     * @param toolId 工具 ID
     * @param rawOutput 原始输出
     */
    data class ToolCallResult(val toolId: String, val rawOutput: String) : McpEffect

    /** 导航到工具详情页
     * @param toolId 工具 ID
     */
    data class NavigateToToolDetail(val toolId: String) : McpEffect
}
