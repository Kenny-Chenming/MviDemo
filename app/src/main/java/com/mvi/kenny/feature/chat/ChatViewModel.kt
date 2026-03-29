package com.mvi.kenny.feature.chat

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
 * ChatViewModel — AI 聊天助手状态管理
 * ============================================================
 * 继承 ViewModel，持有 ChatState（页面状态）和 ChatEffect（副作用）。
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
 *     │                ChatViewModel                    │
 *     │  - 接收 Intent                                   │
 *     │  - 执行 business logic（suspend functions）       │
 *     │  - 更新 _state（MutableStateFlow）               │
 *     │  - 发送 _effect（Channel）                       │
 *     └────────────────────┬────────────────────────────┘
 *                          │ state: StateFlow<ChatState>
 *                          ▼
 *     ┌─────────────────────────────────────────────────┐
 *     │                   Model                         │
 *     │  (ChatState — Immutable data class)             │
 *     └─────────────────────────────────────────────────┘
 *
 * 状态管理：
 * —————————————————————————————————————————————————————
 * - _state：私有 MutableStateFlow，ViewModel 内部写入
 * - state：公开 StateFlow，供 UI 层订阅（collectAsState）
 *
 * 副作用管理：
 * —————————————————————————————————————————————————————
 * - _effect：Channel（热流），缓冲区大小 BUFFERED
 * - effect：receiveAsFlow，UI 层通过 collect{} 监听
 *
 * @see ChatState 页面状态定义
 * @see ChatIntent 用户意图
 * @see ChatEffect 副作用
 */
class ChatViewModel : ViewModel() {

    // ============================================================
    // State — 页面状态（StateFlow，UI 只读）
    // ============================================================
    private val _state = MutableStateFlow(ChatState.Initial)
    val state: StateFlow<ChatState> = _state.asStateFlow()

    /**
     * 当前状态的快照
     * 用于 Compose lambda 表达式内部访问状态
     * （collectAsState 是异步的，lambda 内直接访问 state.value 更即时）
     */
    val currentState: ChatState get() = _state.value

    // ============================================================
    // Effect — 副作用（Channel，热流）
    // ============================================================
    private val _effect = Channel<ChatEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    // ============================================================
    // 消息 ID 自增计数器（保证每条消息有唯一 id）
    // ============================================================
    private var messageIdCounter = 0L

    init {
        // ViewModel 创建时，添加一条系统欢迎消息
        addSystemMessage("👋 你好！我是 AI 助手。有什么我可以帮你的吗？")
    }

    // ============================================================
    // Intent 处理入口
    // ============================================================

    /**
     * 接收并处理用户意图
     * —————————————————————————————————————————————————————
     * 入口方法，UI 层通过 viewModel.sendIntent(intent) 调用。
     * 根据 intent 类型分发到对应的处理函数。
     *
     * 为什么用 when (intent) 而不是 if-else？
     * —————————————————————————————————————————————————————
     * when 是 Kotlin 的模式匹配表达式，比 if-else 更表达力，
     * 且 sealed interface + when 可以做到穷尽检查。
     *
     * @param intent 用户意图（非空）
     */
    fun sendIntent(intent: ChatIntent) {
        when (intent) {
            is ChatIntent.UpdateInput -> updateInput(intent.text)
            is ChatIntent.SendMessage -> sendMessage()
            is ChatIntent.ClearChat -> clearChat()
        }
    }

    // ============================================================
    // Intent 处理函数（更新状态 + 执行业务逻辑）
    // ============================================================

    /**
     * 更新输入框文本
     * —————————————————————————————————————————————————————
     * 每次键盘输入都触发，不需要异步操作，直接更新 State。
     * 这是 MVI 中"纯同步状态更新"的典型场景。
     *
     * @param text 新的输入文本
     */
    private fun updateInput(text: String) {
        _state.value = _state.value.copy(inputText = text)
    }

    /**
     * 发送消息
     * —————————————————————————————————————————————————————
     * 核心业务流程：
     * 1. 校验输入（不能为空）
     * 2. 将用户消息追加到消息列表
     * 3. 清空输入框
     * 4. 触发 AI"思考"状态（显示 typing 指示器）
     * 5. 模拟 AI 异步回复（延迟 1-2 秒）
     * 6. 将 AI 回复追加到消息列表
     * 7. 发送 ScrollToBottom Effect
     *
     * 为什么输入验证在 ViewModel 层而不是 UI 层？
     * —————————————————————————————————————————————————————
     * MVI 架构强调单一数据源，状态变化必须经过 Intent + ViewModel，
     * 即使是简单的"发送按钮是否可点击"也需要通过 State 驱动。
     * UI 层只负责渲染和收集输入，不做业务判断。
     */
    private fun sendMessage() {
        val inputText = _state.value.inputText.trim()

        // 校验：输入不能为空
        if (inputText.isEmpty()) {
            viewModelScope.launch {
                _effect.send(ChatEffect.ShowToast("请输入消息"))
            }
            return
        }

        viewModelScope.launch {
            // —— 步骤 1：添加用户消息 —— //
            val userMessage = ChatMessage(
                id = nextMessageId(),
                content = inputText,
                role = ChatRole.USER
            )

            _state.value = _state.value.copy(
                messages = _state.value.messages + userMessage,
                inputText = "",       // 清空输入框
                isAiTyping = true,    // 显示 AI typing 指示器
                errorMessage = null
            )

            // 通知 UI 滚动到底部
            _effect.send(ChatEffect.ScrollToBottom)

            // —— 步骤 2：模拟 AI 异步回复 —— //
            try {
                // 模拟网络延迟（1-2 秒）
                delay((1000..2000L).random())

                // 模拟 AI 回复内容（根据用户输入生成简单回复）
                val aiResponse = generateAiResponse(inputText)

                val assistantMessage = ChatMessage(
                    id = nextMessageId(),
                    content = aiResponse,
                    role = ChatRole.ASSISTANT
                )

                // —— 步骤 3：追加 AI 消息，关闭 typing 状态 —— //
                _state.value = _state.value.copy(
                    messages = _state.value.messages + assistantMessage,
                    isAiTyping = false
                )

                _effect.send(ChatEffect.ScrollToBottom)

            } catch (e: Exception) {
                // —— 错误处理 —— //
                _state.value = _state.value.copy(
                    isAiTyping = false,
                    errorMessage = "AI 回复失败: ${e.message}"
                )
                _effect.send(ChatEffect.ShowToast("AI 回复失败，请重试"))
            }
        }
    }

    /**
     * 清空聊天记录
     * —————————————————————————————————————————————————————
     * 将 messages 设为空列表，保留其他状态。
     */
    private fun clearChat() {
        viewModelScope.launch {
            messageIdCounter = 0L  // 重置计数器
            _state.value = ChatState.Initial.copy()  // 重置为初始状态
            // 重新添加欢迎消息
            addSystemMessage("👋 你好！我是 AI 助手。有什么我可以帮你的吗？")
            _effect.send(ChatEffect.ShowToast("聊天记录已清空"))
        }
    }

    // ============================================================
    // 辅助方法
    // ============================================================

    /**
     * 生成下一条消息的唯一 ID
     * 线程安全（AtomicLong 的替代方案，简单自增）
     */
    private fun nextMessageId(): Long = ++messageIdCounter

    /**
     * 添加系统消息
     * 在 init 和清空聊天时调用
     */
    private fun addSystemMessage(content: String) {
        val systemMessage = ChatMessage(
            id = nextMessageId(),
            content = content,
            role = ChatRole.SYSTEM
        )
        _state.value = _state.value.copy(
            messages = _state.value.messages + systemMessage
        )
    }

    /**
     * 模拟 AI 回复生成器
     * —————————————————————————————————————————————————————
     * 这是一个简单的规则匹配回复，实际项目中应替换为真实 AI API 调用。
     *
     * 回复策略：
     * - 包含"你好"：友善问候
     * - 包含"帮助"：说明功能
     * - 包含"天气"：模拟天气查询
     * - 包含"笑话"：讲一个笑话
     * - 默认：通用回复
     *
     * @param userInput 用户输入的文本
     * @return AI 回复文本
     */
    private fun generateAiResponse(userInput: String): String {
        val lowerInput = userInput.lowercase()

        return when {
            lowerInput.contains("你好") || lowerInput.contains("hi") ||
            lowerInput.contains("hello") || lowerInput.contains("嗨") ->
                "你好！很高兴和你聊天 😊 有什么问题尽管问我！"

            lowerInput.contains("帮助") || lowerInput.contains("help") ->
                "我可以帮你解答各种问题！比如：\n" +
                "• 查询天气\n• 讲笑话\n• 聊天解闷\n" +
                "• 技术问题解答\n\n你想聊点什么？"

            lowerInput.contains("天气") ->
                "今天天气不错，晴天，温度 22-28°C，适合出门！不过我这里只能模拟查询哦 😄"

            lowerInput.contains("笑话") || lowerInput.contains(" joke") ->
                "从前有一个程序员……\n" +
                "他没有女票。\n\n" +
                "后来他学会了 MVI 架构！💔😂"

            lowerInput.contains("mvi") || lowerInput.contains("compose") ||
            lowerInput.contains("android") ->
                "MVI（Model-View-Intent）是一种非常适合 Compose 的架构模式！\n" +
                "它的核心思想是：\n" +
                "• State：单一数据源，Immutable\n" +
                "• Intent：用户意图，驱动状态变化\n" +
                "• Effect：一次性副作用\n\n" +
                "这个 Chat 功能就是用 MVI 架构实现的 👍"

            lowerInput.contains("谢谢") || lowerInput.contains("thank") ->
                "不客气！有其他问题随时问我 😊"

            else ->
                "嗯，我收到了你的消息：\"$userInput\"\n\n" +
                "作为 MVI 架构演示 demo，我的回复是模拟的 😄\n" +
                "你可以试试问我：\n" +
                "• 天气怎么样？\n• 讲个笑话\n" +
                "• MVI 架构是什么？"
        }
    }
}
