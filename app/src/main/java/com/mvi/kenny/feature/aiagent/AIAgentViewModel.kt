package com.mvi.kenny.feature.aiagent

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
 * AIAgentViewModel — AI Agent 隐私优先框架状态管理
 * ============================================================
 * 继承 ViewModel，持有 AIAgentState（页面状态）和 AIAgentEffect（副作用）。
 *
 * MVI 架构核心流程：
 *
 *     ┌─────────────────────────────────────────────────┐
 *     │                    View                          │
 *     │  (Composable — 消费 State，发送 Intent)          │
 *     └────────────────────┬────────────────────────────┘
 *                          │ state.collectAsState()
 *                          │ intent.sendIntent()
 *                          ▼
 *     ┌─────────────────────────────────────────────────┐
 *     │              AIAgentViewModel                   │
 *     │  - 接收 Intent                                   │
 *     │  - 执行 business logic（suspend functions）       │
 *     │  - 更新 _state（MutableStateFlow）               │
 *     │  - 发送 _effect（Channel）                       │
 *     └────────────────────┬────────────────────────────┘
 *                          │ state: StateFlow<AIAgentState>
 *                          ▼
 *     ┌─────────────────────────────────────────────────┐
 *     │                   Model                         │
 *     │  (AIAgentState — Immutable data class)          │
 *     └─────────────────────────────────────────────────┘
 *
 * @see AIAgentState 页面状态定义
 * @see AIAgentIntent 用户意图
 * @see AIAgentEffect 副作用
 */

/**
 * ============================================================
 * AIAgentViewModel — AI Agent Privacy-First Framework ViewModel
 * ============================================================
 * Manages AIAgentState and AIAgentEffect following MVI pattern.
 *
 * Key design decisions:
 * - StateFlow for state (hot stream, remembers last value)
 * - Channel for effects (hot stream, one-time events)
 * - viewModelScope.launch for coroutine management
 */

// ============================================================
// ViewModel
// ============================================================

class AIAgentViewModel : ViewModel() {

    // ============================================================
    // State — 页面状态（StateFlow，UI 只读）
    // ============================================================
    private val _state = MutableStateFlow(AIAgentState.Initial)
    val state: StateFlow<AIAgentState> = _state.asStateFlow()

    /**
     * 当前状态的快照
     * Snapshot of current state for lambda access
     */
    val currentState: AIAgentState get() = _state.value

    // ============================================================
    // Effect — 副作用（Channel，热流）
    // ============================================================
    private val _effect = Channel<AIAgentEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    // ============================================================
    // 消息 ID 自增计数器
    // ============================================================
    private var messageIdCounter = 0L

    init {
        // 初始化可用模型和工具列表
        initializeModelsAndTools()
        // 添加系统欢迎消息
        addSystemMessage("🛡️ 隐私优先 AI Agent 已就绪\n\n" +
            "• 发送消息与我对话\n" +
            "• 点击 🔧 打开工具面板\n" +
            "• 开启隐私模式🔐，对话仅存内存")
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
    fun sendIntent(intent: AIAgentIntent) {
        when (intent) {
            is AIAgentIntent.UpdateInput -> updateInput(intent.text)
            is AIAgentIntent.SendMessage -> sendMessage()
            is AIAgentIntent.ToggleTool -> toggleTool(intent.toolId)
            is AIAgentIntent.SwitchModel -> switchModel(intent.model)
            is AIAgentIntent.TogglePrivacyMode -> togglePrivacyMode()
            is AIAgentIntent.ClearSession -> clearSession()
            is AIAgentIntent.ExpandToolCall -> expandToolCall(intent.toolCallId)
            is AIAgentIntent.SetToolDrawerVisible -> setToolDrawerVisible(intent.visible)
            is AIAgentIntent.SwitchSession -> switchSession(intent.sessionId)
            is AIAgentIntent.NewSession -> newSession()
        }
    }

    // ============================================================
    // Intent 处理函数
    // ============================================================

    /**
     * 更新输入框文本
     * Update input text synchronously
     *
     * @param text 新的输入文本
     */
    private fun updateInput(text: String) {
        _state.value = _state.value.copy(inputText = text)
    }

    /**
     * 发送消息
     * Core business logic: user sends a message
     *
     * Flow:
     * 1. Validate input (not empty)
     * 2. Add user message to list
     * 3. Clear input, show typing indicator
     * 4. Simulate AI response (delay 1-2s)
     * 5. Add AI response to list
     * 6. Send ScrollToBottom effect
     */
    private fun sendMessage() {
        val inputText = _state.value.inputText.trim()

        // 校验：输入不能为空
        if (inputText.isEmpty()) {
            viewModelScope.launch {
                _effect.send(AIAgentEffect.ShowToast("请输入消息"))
            }
            return
        }

        viewModelScope.launch {
            // —— 步骤 1：添加用户消息 —— //
            val userMessage = AgentMessage(
                id = nextMessageId(),
                content = inputText,
                role = MessageRole.USER
            )

            _state.value = _state.value.copy(
                messages = _state.value.messages + userMessage,
                inputText = "",
                isLoading = true,
                error = null
            )

            _effect.send(AIAgentEffect.ScrollToBottom)

            // —— 步骤 2：模拟 AI 异步回复 —— //
            try {
                // 模拟网络延迟（1-2 秒）
                delay((1000..2000L).random())

                // 模拟 AI 回复内容
                val aiResponse = generateAiResponse(inputText)

                val assistantMessage = AgentMessage(
                    id = nextMessageId(),
                    content = aiResponse.content,
                    role = MessageRole.ASSISTANT,
                    toolCalls = aiResponse.toolCalls
                )

                // —— 步骤 3：追加 AI 消息，关闭 loading 状态 —— //
                _state.value = _state.value.copy(
                    messages = _state.value.messages + assistantMessage,
                    isLoading = false
                )

                // 如果 AI 响应包含工具调用，模拟执行
                if (aiResponse.toolCalls.isNotEmpty()) {
                    simulateToolExecution(aiResponse.toolCalls)
                }

                _effect.send(AIAgentEffect.ScrollToBottom)

            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = "AI 回复失败: ${e.message}"
                )
                _effect.send(AIAgentEffect.ShowToast("AI 回复失败，请重试"))
            }
        }
    }

    /**
     * 切换工具启用状态
     * Toggle a tool's enabled state
     *
     * @param toolId 工具 ID
     */
    private fun toggleTool(toolId: String) {
        val tools = _state.value.availableTools.toMutableList()
        val index = tools.indexOfFirst { it.id == toolId }
        if (index != -1) {
            val tool = tools[index]
            tools[index] = tool.copy(isEnabled = !tool.isEnabled)
            _state.value = _state.value.copy(availableTools = tools)

            viewModelScope.launch {
                val status = if (!tool.isEnabled) "已启用" else "已禁用"
                _effect.send(AIAgentEffect.ShowToast("${tool.name} $status"))
            }
        }
    }

    /**
     * 切换模型
     * Switch LLM provider
     *
     * @param model 新的模型提供商
     */
    private fun switchModel(model: LLMProvider) {
        _state.value = _state.value.copy(currentModel = model)
        viewModelScope.launch {
            _effect.send(AIAgentEffect.ShowToast("已切换到 ${model.name}"))
        }
    }

    /**
     * 切换隐私模式
     * Toggle privacy mode — when enabled, conversations stay in memory only
     */
    private fun togglePrivacyMode() {
        val newPrivacyMode = !_state.value.privacyMode
        _state.value = _state.value.copy(privacyMode = newPrivacyMode)

        viewModelScope.launch {
            val msg = if (newPrivacyMode) "隐私模式已开启🔐" else "隐私模式已关闭"
            _effect.send(AIAgentEffect.ShowToast(msg))
        }
    }

    /**
     * 清空当前会话
     * Clear current session (respects privacy mode)
     */
    private fun clearSession() {
        viewModelScope.launch {
            messageIdCounter = 0L

            if (_state.value.privacyMode) {
                // 隐私模式下仅清空内存
                _state.value = AIAgentState.Initial.copy(
                    privacyMode = true,
                    availableModels = _state.value.availableModels,
                    availableTools = _state.value.availableTools,
                    currentModel = _state.value.currentModel
                )
                _effect.send(AIAgentEffect.ShowToast("隐私模式：会话已清空"))
            } else {
                _state.value = AIAgentState.Initial.copy(
                    availableModels = _state.value.availableModels,
                    availableTools = _state.value.availableTools,
                    currentModel = _state.value.currentModel
                )
                _effect.send(AIAgentEffect.ShowToast("会话已清空"))
            }

            addSystemMessage("🛡️ 新会话已创建")
        }
    }

    /**
     * 展开/折叠工具调用详情
     * Expand or collapse a tool call's details
     *
     * @param toolCallId 工具调用 ID
     */
    private fun expandToolCall(toolCallId: String) {
        val messages = _state.value.messages.map { message ->
            message.copy(
                toolCalls = message.toolCalls.map { tc ->
                    if (tc.id == toolCallId) tc.copy(isExpanded = !tc.isExpanded) else tc
                }
            )
        }
        _state.value = _state.value.copy(messages = messages)
    }

    /**
     * 显示/隐藏工具抽屉
     * Show or hide the tool drawer BottomSheet
     *
     * @param visible 是否可见
     */
    private fun setToolDrawerVisible(visible: Boolean) {
        _state.value = _state.value.copy(toolDrawerVisible = visible)
    }

    /**
     * 切换会话
     * Switch to a different session (stub implementation)
     *
     * @param sessionId 会话 ID
     */
    private fun switchSession(sessionId: String) {
        viewModelScope.launch {
            _effect.send(AIAgentEffect.ShowToast("会话切换功能开发中"))
        }
    }

    /**
     * 新建会话
     * Create a new session (stub for multi-session support)
     */
    private fun newSession() {
        clearSession()
    }

    // ============================================================
    // 辅助方法
    // ============================================================

    /**
     * 生成下一条消息的唯一 ID
     */
    private fun nextMessageId(): Long = ++messageIdCounter

    /**
     * 添加系统消息
     */
    private fun addSystemMessage(content: String) {
        val systemMessage = AgentMessage(
            id = nextMessageId(),
            content = content,
            role = MessageRole.SYSTEM
        )
        _state.value = _state.value.copy(
            messages = _state.value.messages + systemMessage
        )
    }

    /**
     * 初始化可用模型和工具列表
     * Initialize available LLM models and tools
     */
    private fun initializeModelsAndTools() {
        val models = listOf(
            LLMProvider(
                id = "gpt-4o",
                name = "GPT-4o",
                apiEndpoint = "https://api.openai.com/v1/chat/completions",
                isLocal = false
            ),
            LLMProvider(
                id = "gpt-4o-mini",
                name = "GPT-4o Mini",
                apiEndpoint = "https://api.openai.com/v1/chat/completions",
                isLocal = false
            ),
            LLMProvider(
                id = "claude-3-5-sonnet",
                name = "Claude 3.5 Sonnet",
                apiEndpoint = "https://api.anthropic.com/v1/messages",
                isLocal = false
            ),
            LLMProvider(
                id = "ollama-llama3",
                name = "Ollama Llama3 (本地)",
                apiEndpoint = "http://localhost:11434/api/chat",
                isLocal = true
            ),
            LLMProvider(
                id = "ollama-qwen2",
                name = "Ollama Qwen2 (本地)",
                apiEndpoint = "http://localhost:11434/api/chat",
                isLocal = true
            )
        )

        val tools = listOf(
            Tool(
                id = "web_search",
                name = "网络搜索",
                description = "搜索互联网获取最新信息",
                isEnabled = true
            ),
            Tool(
                id = "calculator",
                name = "计算器",
                description = "执行数学计算",
                isEnabled = true
            ),
            Tool(
                id = "code_runner",
                name = "代码运行",
                description = "运行代码片段（Kotlin/Java）",
                isEnabled = false
            ),
            Tool(
                id = "file_manager",
                name = "文件管理",
                description = "读取和写入本地文件",
                isEnabled = false
            ),
            Tool(
                id = "shell",
                name = "Shell 命令",
                description = "执行终端命令",
                isEnabled = false
            ),
            Tool(
                id = "clipboard",
                name = "剪贴板",
                description = "读取和写入系统剪贴板",
                isEnabled = true
            )
        )

        _state.value = _state.value.copy(
            availableModels = models,
            availableTools = tools,
            enabledTools = tools.filter { it.isEnabled }
        )
    }

    /**
     * 模拟工具执行
     * Simulate tool execution (stub for function calling)
     *
     * @param toolCalls 要执行的工具调用列表
     */
    private fun simulateToolExecution(toolCalls: List<ToolCall>) {
        viewModelScope.launch {
            // 延迟模拟执行
            delay(500)

            toolCalls.forEach { toolCall ->
                val result = when (toolCall.toolId) {
                    "calculator" -> "计算结果：42"
                    "web_search" -> "搜索结果：暂无结果（模拟数据）"
                    "code_runner" -> "运行结果：Hello, World!"
                    else -> "执行完成"
                }

                val toolResult = ToolResult(
                    toolCallId = toolCall.id,
                    output = result,
                    isError = false
                )

                val messages = _state.value.messages.map { message ->
                    if (message.toolCalls.any { it.id == toolCall.id }) {
                        message.copy(
                            toolResults = message.toolResults + toolResult
                        )
                    } else {
                        message
                    }
                }

                _state.value = _state.value.copy(messages = messages)
                _effect.send(AIAgentEffect.ScrollToBottom)
            }
        }
    }

    /**
     * 模拟 AI 回复生成器
     * Simulated AI response generator (replaces real API call)
     *
     * @param userInput 用户输入
     * @return AI 响应（包含可选的工具调用）
     */
    private data class AiResponse(
        val content: String,
        val toolCalls: List<ToolCall> = emptyList()
    )

    private fun generateAiResponse(userInput: String): AiResponse {
        val lowerInput = userInput.lowercase()

        return when {
            lowerInput.contains("你好") || lowerInput.contains("hi") ||
            lowerInput.contains("hello") || lowerInput.contains("嗨") ->
                AiResponse(
                    content = "你好！我是隐私优先的 AI Agent 🛡️\n\n" +
                        "我可以帮你：\n" +
                        "• 回答问题\n" +
                        "• 搜索信息\n" +
                        "• 执行计算\n" +
                        "• 编写代码\n\n" +
                        "开启🔐隐私模式后，所有对话仅存本地内存，不会上传云端。"
                )

            lowerInput.contains("隐私") || lowerInput.contains("privacy") ->
                AiResponse(
                    content = "🔐 隐私保护是我的核心能力：\n\n" +
                        "• 开启隐私模式后，对话仅存于设备内存\n" +
                        "• 重启应用后不会保留任何记录\n" +
                        "• 支持本地模型（Ollama），完全不联网\n" +
                        "• API 密钥加密存储，不记录日志\n\n" +
                        "当前隐私模式：${if (_state.value.privacyMode) "✅ 已开启" else "❌ 未开启"}"
                )

            lowerInput.contains("工具") || lowerInput.contains("tool") ->
                AiResponse(
                    content = "🔧 可用工具列表：\n\n" +
                        _state.value.availableTools
                            .filter { it.isEnabled }
                            .joinToString("\n") { "• ${it.name}: ${it.description}" } +
                        "\n\n点击输入框右侧 🔧 按钮打开工具面板"
                )

            lowerInput.contains("模型") || lowerInput.contains("model") ||
            lowerInput.contains("切换") ->
                AiResponse(
                    content = "🤖 当前模型：${_state.value.currentModel.name}\n\n" +
                        "可切换模型：\n" +
                        _state.value.availableModels.joinToString("\n") { "• ${it.name} ${if (it.isLocal) "(本地)" else ""}" } +
                        "\n\n长按发送按钮 🔘 可快速切换模型"
                )

            lowerInput.contains("计算") || lowerInput.contains("数学") ||
            lowerInput.contains("加") || lowerInput.contains("减") ||
            lowerInput.contains("乘") || lowerInput.contains("除") ->
                AiResponse(
                    content = "🧮 计算器工具已就绪！\n\n" +
                        "例如：1 + 2 = ?",
                    toolCalls = listOf(
                        ToolCall(
                            id = "tc_${System.currentTimeMillis()}",
                            toolId = "calculator",
                            toolName = "计算器",
                            arguments = "{\"expression\": \"待输入\"}"
                        )
                    )
                )

            lowerInput.contains("搜索") || lowerInput.contains("search") ||
            lowerInput.contains("查找") ->
                AiResponse(
                    content = "🌐 网络搜索工具已触发\n\n" +
                        "正在搜索：\"$userInput\"...",
                    toolCalls = listOf(
                        ToolCall(
                            id = "tc_${System.currentTimeMillis()}",
                            toolId = "web_search",
                            toolName = "网络搜索",
                            arguments = "{\"query\": \"$userInput\"}"
                        )
                    )
                )

            lowerInput.contains("代码") || lowerInput.contains("code") ||
            lowerInput.contains("kotlin") || lowerInput.contains("java") ->
                AiResponse(
                    content = "💻 代码运行工具可用！\n\n" +
                        "请描述你想写的代码，我来帮你生成。\n" +
                        "支持 Kotlin 和 Java。",
                    toolCalls = listOf(
                        ToolCall(
                            id = "tc_${System.currentTimeMillis()}",
                            toolId = "code_runner",
                            toolName = "代码运行",
                            arguments = "{\"language\": \"kotlin\"}"
                        )
                    )
                )

            lowerInput.contains("帮助") || lowerInput.contains("help") ->
                AiResponse(
                    content = "📖 AI Agent 使用指南：\n\n" +
                        "• 输入文字与我对话\n" +
                        "• 点击 🔧 打开工具面板\n" +
                        "• 点击 🔐 开启隐私模式\n" +
                        "• 长按 🔘 切换 AI 模型\n" +
                        "• 点击清空按钮重置会话\n\n" +
                        "有什么我可以帮你的？"
                )

            else ->
                AiResponse(
                    content = "📝 收到你的消息：\"$userInput\"\n\n" +
                        "作为 MVI 架构演示，我的回复是模拟的 😄\n\n" +
                        "试试问我：\n" +
                        "• 你好！\n" +
                        "• 隐私模式是什么？\n" +
                        "• 有哪些工具可用？\n" +
                        "• 切换模型"
                )
        }
    }
}
