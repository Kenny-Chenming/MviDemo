package com.mvi.kenny.feature.aiagent

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Memory
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * ============================================================
 * AIAgentContract — AI Agent 隐私优先框架 MVI 契约
 * ============================================================
 * 采用 MVI（Model-View-Intent）架构模式。
 *
 * MVI 三要素：
 * - Model（State）：页面状态的唯一真相来源，Immutable 数据类
 * - View：Composable 函数，消费 State，渲染 UI
 * - Intent：用户意图，ViewModel 收到 Intent 后执行业务逻辑
 *
 * Effect：一次性副作用（Toast、滚动到底部），通过 Channel 传递
 * —————————————————————————————————————————————————————
 * This contract defines the privacy-first AI Agent framework for mobile.
 * All local conversations can be kept in memory only (privacy mode).
 *
 * @see AIAgentViewModel 状态管理逻辑
 */

/**
 * ============================================================
 * AIAgentContract — AI Agent MVI Contract (Privacy-First Framework)
 * ============================================================
 * Architecture: MVI (Model-View-Intent)
 *
 * Core components:
 * - Model (State): Single source of truth, Immutable data class
 * - View: Composable functions, consume State, render UI
 * - Intent: User intentions, drive state changes via ViewModel
 * - Effect: One-time side effects (Toast, scroll), delivered via Channel
 */

// ============================================================
// Data Models / 数据模型
// ============================================================

/**
 * AI Agent 聊天消息数据模型
 *
 * @param id 消息唯一标识（用于 LazyColumn key）
 * @param content 消息文本内容
 * @param role 消息角色：user / assistant / system / tool_result
 * @param timestamp 消息发送时间（毫秒时间戳）
 * @param toolCalls 嵌入的工具调用列表（可选）
 * @param toolResults 工具执行结果列表（可选）
 *
 * @see MessageRole
 */
data class AgentMessage(
    val id: Long,
    val content: String,
    val role: MessageRole,
    val timestamp: Long = System.currentTimeMillis(),
    val toolCalls: List<ToolCall> = emptyList(),
    val toolResults: List<ToolResult> = emptyList()
)

/**
 * 消息角色枚举
 *
 * @param value 角色标识字符串
 * - user：用户发送的消息
 * - assistant：AI 助手回复的消息
 * - system：系统消息（如"开始对话"）
 * - tool_result：工具执行结果
 *
 * @param displayName 中文显示名称
 */
enum class MessageRole(val value: String, val displayName: String) {
    USER("user", "用户"),
    ASSISTANT("assistant", "AI 助手"),
    SYSTEM("system", "系统"),
    TOOL_RESULT("tool_result", "工具结果");

    companion object {
        fun fromValue(value: String): MessageRole {
            return entries.find { it.value == value } ?: USER
        }
    }
}

/**
 * 工具调用数据模型
 *
 * @param id 工具调用唯一标识
 * @param toolId 工具 ID（如 "web_search", "calculator"）
 * @param toolName 工具显示名称
 * @param arguments JSON 格式参数字符串
 * @param isExpanded 是否展开显示详情
 * @param isExecuting 是否正在执行
 * @param result 工具执行结果（完成后填充）
 */
data class ToolCall(
    val id: String,
    val toolId: String,
    val toolName: String,
    val arguments: String,
    val isExpanded: Boolean = false,
    val isExecuting: Boolean = false,
    val result: String? = null
)

/**
 * 工具执行结果数据模型
 *
 * @param toolCallId 对应的 ToolCall ID
 * @param output 执行结果输出
 * @param isError 是否执行出错
 */
data class ToolResult(
    val toolCallId: String,
    val output: String,
    val isError: Boolean = false
)

/**
 * LLM Provider / 模型提供商
 *
 * @param id 提供商唯一标识
 * @param name 显示名称
 * @param apiEndpoint API 端点
 * @param isLocal 是否为本地模型
 */
data class LLMProvider(
    val id: String,
    val name: String,
    val apiEndpoint: String = "",
    val isLocal: Boolean = false
)

/**
 * Agent 工具数据模型
 *
 * @param id 工具唯一标识
 * @param name 工具名称
 * @param description 工具描述
 * @param isEnabled 是否启用
 * @param icon 工具图标（可选）
 */
data class Tool(
    val id: String,
    val name: String,
    val description: String,
    val isEnabled: Boolean = true,
    val icon: ImageVector? = null
)

// ============================================================
// State / 页面状态
// ============================================================

/**
 * AI Agent 页面状态
 *
 * MVI 架构中的 Model 层，持有页面的所有状态。
 * 状态是 Immutable 的，每次状态变化都创建新的 State 对象。
 *
 * @param sessionId 当前会话 ID
 * @param messages 聊天消息列表（按时间顺序排列）
 * @param isLoading AI 是否正在"思考"（显示 typing 指示器）
 * @param currentModel 当前选中的 LLM 模型
 * @param enabledTools 已启用的工具列表
 * @param privacyMode 隐私模式开关（开启后仅存内存）
 * @param inputText 输入框当前的文本
 * @param toolDrawerVisible 工具抽屉是否可见
 * @param error 错误信息，null 表示无错误
 * @param availableModels 可用模型列表
 * @param availableTools 可用工具列表
 * @param sessions 会话历史列表
 *
 * @see AIAgentIntent 用户意图
 * @see AIAgentViewModel 状态管理逻辑
 */
data class AIAgentState(
    val sessionId: String = "default",
    val messages: List<AgentMessage> = emptyList(),
    val isLoading: Boolean = false,
    val currentModel: LLMProvider = LLMProvider(
        id = "gpt-4o",
        name = "GPT-4o",
        apiEndpoint = "https://api.openai.com/v1/chat/completions"
    ),
    val enabledTools: List<Tool> = emptyList(),
    val privacyMode: Boolean = false,
    val inputText: String = "",
    val toolDrawerVisible: Boolean = false,
    val error: String? = null,
    val availableModels: List<LLMProvider> = emptyList(),
    val availableTools: List<Tool> = emptyList(),
    val sessions: List<Session> = emptyList()
) {
    companion object {
        /** 初始状态 */
        val Initial = AIAgentState()
    }
}

/**
 * 会话历史数据模型
 *
 * @param id 会话唯一标识
 * @param title 会话标题（取首条用户消息的前 20 字符）
 * @param lastMessage 最后一条消息预览
 * @param lastUpdated 最后更新时间
 * @param messageCount 消息数量
 */
data class Session(
    val id: String,
    val title: String,
    val lastMessage: String,
    val lastUpdated: Long = System.currentTimeMillis(),
    val messageCount: Int = 0
)

// ============================================================
// Intent / 用户意图
// ============================================================

/**
 * AI Agent 页面用户意图（User Intent）
 * —————————————————————————————————————————————————————
 * 页面上的每一个用户操作都对应一个 Intent。
 * ViewModel 收到 Intent 后执行业务逻辑，然后更新 State。
 *
 * sealed interface + when 可以做到穷尽检查（exhaustive）。
 *
 * @see AIAgentViewModel.sendIntent 处理所有 Intent
 */
sealed interface AIAgentIntent {
    /**
     * 更新输入框文本
     * Update input text — triggered on every keystroke
     *
     * @param text 新的输入文本
     */
    data class UpdateInput(val text: String) : AIAgentIntent

    /**
     * 发送消息
     * Send message — user taps send button or presses Enter
     */
    data object SendMessage : AIAgentIntent

    /**
     * 切换工具启用状态
     * Toggle tool enabled state
     *
     * @param toolId 工具 ID
     */
    data class ToggleTool(val toolId: String) : AIAgentIntent

    /**
     * 切换模型
     * Switch LLM model
     *
     * @param model 新的模型提供商
     */
    data class SwitchModel(val model: LLMProvider) : AIAgentIntent

    /**
     * 切换隐私模式
     * Toggle privacy mode (in-memory only when enabled)
     */
    data object TogglePrivacyMode : AIAgentIntent

    /**
     * 清空当前会话
     * Clear current session
     */
    data object ClearSession : AIAgentIntent

    /**
     * 展开/折叠工具调用详情
     * Expand or collapse tool call details
     *
     * @param toolCallId 工具调用 ID
     */
    data class ExpandToolCall(val toolCallId: String) : AIAgentIntent

    /**
     * 显示/隐藏工具抽屉
     * Show or hide tool drawer BottomSheet
     *
     * @param visible 是否可见
     */
    data class SetToolDrawerVisible(val visible: Boolean) : AIAgentIntent

    /**
     * 切换会话
     * Switch to a different session
     *
     * @param sessionId 会话 ID
     */
    data class SwitchSession(val sessionId: String) : AIAgentIntent

    /**
     * 新建会话
     * Create a new session
     */
    data object NewSession : AIAgentIntent
}

// ============================================================
// Effect / 副作用
// ============================================================

/**
 * AI Agent 页面副作用（Effect）
 * —————————————————————————————————————————————————————
 * 一次性事件，不可变，只能被消费一次。
 * UI 层通过 LaunchedEffect + flow.collect{} 监听并处理。
 *
 * @see AIAgentViewModel 中通过 _effect.send() 发送 Effect
 */
sealed interface AIAgentEffect {
    /**
     * 显示 Toast
     * Show a toast message
     *
     * @param message Toast 文本内容
     */
    data class ShowToast(val message: String) : AIAgentEffect

    /**
     * 滚动到消息列表底部
     * Scroll to bottom of message list
     */
    data object ScrollToBottom : AIAgentEffect

    /**
     * 复制到剪贴板
     * Copy text to clipboard
     *
     * @param text 要复制的文本
     */
    data class CopyToClipboard(val text: String) : AIAgentEffect
}
