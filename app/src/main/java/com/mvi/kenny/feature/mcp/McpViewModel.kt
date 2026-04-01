package com.mvi.kenny.feature.mcp

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
 * McpViewModel — Android MCP Server 工具包状态管理
 * ============================================================
 * PRD-011 | Android MCP Server 工具包
 *
 * 继承 ViewModel，持有 McpState（页面状态）和 McpEffect（副作用）。
 *
 * 状态管理：
 * - _state：私有 MutableStateFlow，ViewModel 内部写入
 * - state：公开 StateFlow，供 UI 层订阅（collectAsState）
 *
 * 副作用管理：
 * - _effect：Channel（热流），缓冲区大小 BUFFERED
 * - effect：receiveAsFlow，UI 层通过 collect{} 监听
 *
 * @see McpState 页面状态定义
 * @see McpIntent 用户意图
 * @see McpEffect 副作用
 */
class McpViewModel : ViewModel() {

    /** 页面状态（StateFlow，UI 只读） */
    private val _state = MutableStateFlow(McpState.Initial)
    val state: StateFlow<McpState> = _state.asStateFlow()

    /**
     * 当前状态的快照
     * 用于 Compose 中 lambda 表达式内部访问状态
     */
    val currentState: McpState get() = _state.value

    /**
     * 副作用 Channel
     * @see McpEffect
     */
    private val _effect = Channel<McpEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    /** 日志 ID 自增器 */
    private var logIdCounter = 0L

    /** 调用记录 ID 自增器 */
    private var callIdCounter = 0L

    init {
        // 初始化时加载模拟工具列表（真实场景从 MCP Server 获取）
        loadMockTools()
    }

    /**
     * 接收并处理用户意图
     * —————————————————————————————————————————————————————
     *
     * @param intent 用户意图（非空）
     */
    fun sendIntent(intent: McpIntent) {
        when (intent) {
            is McpIntent.StartServer -> startServer()
            is McpIntent.StopServer -> stopServer()
            is McpIntent.ToggleTool -> toggleTool(intent.toolId, intent.enabled)
            is McpIntent.SelectTool -> selectTool(intent.tool)
            is McpIntent.DismissToolDetail -> dismissToolDetail()
            is McpIntent.CallTool -> callTool(intent.toolId, intent.params)
            is McpIntent.RefreshDevices -> refreshDevices()
            is McpIntent.UpdateSettings -> updateSettings(intent.settings)
        }
    }

    /**
     * 加载模拟工具列表
     * 真实场景：MCP Server 启动后通过 JSON-RPC 获取工具列表
     */
    private fun loadMockTools() {
        val mockTools = listOf(
            McpTool(
                id = "adb-shell",
                name = "ADB Shell",
                description = "Execute shell commands on connected Android device via ADB",
                enabled = true
            ),
            McpTool(
                id = "gradle-build",
                name = "Gradle Build",
                description = "Trigger Gradle build tasks with customizable arguments",
                enabled = true
            ),
            McpTool(
                id = "logcat",
                name = "Logcat",
                description = "Stream real-time logcat output from ADB with buffer size limit",
                enabled = false
            ),
            McpTool(
                id = "screenrecord",
                name = "Screen Record",
                description = "Record device screen via ADB screenrecord command",
                enabled = true
            ),
            McpTool(
                id = "ddms-screenshot",
                name = "DDMS Screenshot",
                description = "Capture device screenshot via DDMS framebuffer dump",
                enabled = true
            ),
            McpTool(
                id = "compose-preview",
                name = "Compose Preview",
                description = "Generate and retrieve Compose UI preview renders",
                enabled = false
            )
        )
        _state.value = _state.value.copy(tools = mockTools)
        appendLog("INFO", "工具列表加载完成（${mockTools.size} 个工具）")
    }

    /**
     * 启动 MCP Server
     * —————————————————————————————————————————————————————
     * 模拟启动流程：IDLE → STARTING → RUNNING
     * 真实场景：建立 STDIO/TCP 双通道，握手 MCP 协议
     */
    private fun startServer() {
        if (_state.value.serverStatus == ServerStatus.RUNNING) return

        viewModelScope.launch {
            _state.value = _state.value.copy(
                serverStatus = ServerStatus.STARTING,
                errorMessage = null
            )
            appendLog("INFO", "正在启动 MCP Server（端口: ${_state.value.serverPort}）...")

            try {
                // 模拟启动延迟（真实场景：MCP Server 进程建立连接）
                delay(1500)

                _state.value = _state.value.copy(serverStatus = ServerStatus.RUNNING)
                appendLog("INFO", "MCP Server 已启动，运行中（端口: ${_state.value.serverPort}）")
                _effect.send(McpEffect.ShowToast("MCP Server 已启动"))

            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    serverStatus = ServerStatus.ERROR,
                    errorMessage = "启动失败: ${e.message}"
                )
                appendLog("ERROR", "MCP Server 启动失败: ${e.message}")
            }
        }
    }

    /**
     * 停止 MCP Server
     * —————————————————————————————————————————————————————
     * 模拟停止流程：RUNNING → STOPPING → IDLE
     * 真实场景：关闭所有 subprocess，避免僵尸进程
     */
    private fun stopServer() {
        if (_state.value.serverStatus != ServerStatus.RUNNING) return

        viewModelScope.launch {
            _state.value = _state.value.copy(serverStatus = ServerStatus.STOPPING)
            appendLog("INFO", "正在停止 MCP Server...")

            try {
                delay(800)
                _state.value = _state.value.copy(serverStatus = ServerStatus.IDLE)
                appendLog("INFO", "MCP Server 已停止")
                _effect.send(McpEffect.ShowToast("MCP Server 已停止"))

            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    serverStatus = ServerStatus.ERROR,
                    errorMessage = "停止失败: ${e.message}"
                )
                appendLog("ERROR", "MCP Server 停止失败: ${e.message}")
            }
        }
    }

    /**
     * 切换工具启用状态
     *
     * @param toolId 工具 ID
     * @param enabled 是否启用
     */
    private fun toggleTool(toolId: String, enabled: Boolean) {
        val updatedTools = _state.value.tools.map { tool ->
            if (tool.id == toolId) tool.copy(enabled = enabled) else tool
        }
        _state.value = _state.value.copy(tools = updatedTools)

        val action = if (enabled) "启用" else "禁用"
        appendLog("INFO", "工具 [$toolId] 已$action")
    }

    /**
     * 选中工具（打开详情 Sheet）
     *
     * @param tool 被选中的工具
     */
    private fun selectTool(tool: McpTool) {
        _state.value = _state.value.copy(selectedTool = tool)
    }

    /**
     * 关闭工具详情 Sheet
     */
    private fun dismissToolDetail() {
        _state.value = _state.value.copy(selectedTool = null)
    }

    /**
     * 调用工具（测试调用）
     * —————————————————————————————————————————————————————
     * 模拟工具调用流程：显示加载态 → 延迟 → 返回模拟输出
     * 真实场景：通过 MCP JSON-RPC 协议发送 tool_call 请求
     *
     * @param toolId 工具 ID
     * @param params 调用参数
     */
    private fun callTool(toolId: String, params: Map<String, String>) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)

            val tool = _state.value.tools.find { it.id == toolId }
            appendLog("INFO", "调用工具 [$toolId]，参数: $params")

            try {
                delay(1200) // 模拟工具执行时间

                val mockOutput = generateMockOutput(toolId, params)
                appendLog("INFO", "工具 [$toolId] 调用成功")

                // 更新工具调用历史
                val updatedTools = _state.value.tools.map { t ->
                    if (t.id == toolId) {
                        val newCall = ToolCall(
                            id = ++callIdCounter,
                            timestamp = getCurrentTimestamp(),
                            params = params,
                            rawOutput = mockOutput,
                            success = true
                        )
                        t.copy(callHistory = (listOf(newCall) + t.callHistory).take(20))
                    } else t
                }
                _state.value = _state.value.copy(
                    isLoading = false,
                    tools = updatedTools
                )

                _effect.send(McpEffect.ToolCallResult(toolId, mockOutput))
                _effect.send(McpEffect.ShowToast("调用成功"))

            } catch (e: Exception) {
                _state.value = _state.value.copy(isLoading = false)
                appendLog("ERROR", "工具 [$toolId] 调用失败: ${e.message}")
                _effect.send(McpEffect.ShowToast("调用失败: ${e.message}"))
            }
        }
    }

    /**
     * 生成模拟工具输出（用于演示）
     * 真实场景：替换为 MCP JSON-RPC 实际返回结果
     */
    private fun generateMockOutput(toolId: String, params: Map<String, String>): String {
        return when (toolId) {
            "adb-shell" -> "```\n\$ adb shell ${params["command"] ?: "ls"}\n[device] connected\ndata  cache  etc  system\n```"
            "gradle-build" -> "```\n> Task :app:assembleDebug\nBUILD SUCCESSFUL in 45s\n```"
            "logcat" -> "```\n04-01 11:38:00.123 D/McpServer(12345): Server started on port 3456\n04-01 11:38:01.456 I/App(12345): Activity resumed\n```"
            "screenrecord" -> "Recording saved: /sdcard/screenrecord_20260401.mp4\nResolution: 1080x1920\nDuration: 30s"
            "ddms-screenshot" -> "Screenshot saved: /sdcard/screenshot_20260401.png\nResolution: 1080x2400"
            "compose-preview" -> "Preview URL: http://localhost:3456/preview/abc123"
            else -> "Unknown tool: $toolId"
        }
    }

    /**
     * 刷新设备列表（跨设备桥接）
     * —————————————————————————————————————————————————————
     * 模拟设备发现：延迟后返回模拟设备列表
     * 真实场景：使用 Android 17 NsdManager 进行 mDNS 发现
     */
    private fun refreshDevices() {
        viewModelScope.launch {
            appendLog("INFO", "正在搜索设备...")

            delay(2000)

            val mockDevices = listOf(
                AndroidDevice(
                    id = "device-phone-001",
                    name = "Pixel 8 Pro",
                    type = DeviceType.PHONE,
                    connected = true
                ),
                AndroidDevice(
                    id = "device-tablet-001",
                    name = "Samsung Tab S9",
                    type = DeviceType.TABLET,
                    connected = true
                ),
                AndroidDevice(
                    id = "device-tv-001",
                    name = "Google TV Streamer",
                    type = DeviceType.TV,
                    connected = false
                ),
                AndroidDevice(
                    id = "device-xr-001",
                    name = "Samsung Galaxy XR",
                    type = DeviceType.XR,
                    connected = false
                )
            )

            _state.value = _state.value.copy(devices = mockDevices)
            appendLog("INFO", "设备搜索完成（${mockDevices.size} 台设备）")
        }
    }

    /**
     * 更新设置
     *
     * @param settings 新的设置
     */
    private fun updateSettings(settings: McpSettings) {
        _state.value = _state.value.copy(settings = settings)
        appendLog("INFO", "设置已更新")
    }

    /**
     * 添加日志条目（保留最新 100 条）
     *
     * @param level 日志级别（INFO/ERROR）
     * @param message 日志内容
     */
    private fun appendLog(level: String, message: String) {
        val entry = LogEntry(
            id = ++logIdCounter,
            timestamp = getCurrentTimestamp(),
            level = if (level == "ERROR") LogLevel.ERROR else LogLevel.INFO,
            message = message
        )
        val updatedLogs = (_state.value.connectionLogs + entry).takeLast(100)
        _state.value = _state.value.copy(connectionLogs = updatedLogs)
    }

    /** 获取当前时间戳字符串（模拟格式） */
    private fun getCurrentTimestamp(): String {
        val now = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault())
        return now.format(java.util.Date())
    }
}
