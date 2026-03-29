package com.mvi.kenny.feature.chat

/**
 * ============================================================
 * ChatContract — AI 聊天助手 MVI 契约
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
 */

/**
 * 聊天消息数据模型
 *
 * @param id 消息唯一标识（用于 LazyColumn key）
 * @param content 消息文本内容
 * @param role 消息角色：user（用户）/ assistant（AI助手）
 * @param timestamp 消息发送时间（毫秒时间戳）
 *
 * @see ChatRole
 */
data class ChatMessage(
    val id: Long,
    val content: String,
    val role: ChatRole,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * 消息角色枚举
 *
 * @param value 角色标识字符串
 * - user：用户发送的消息
 * - assistant：AI 助手回复的消息
 * - system：系统消息（如"开始对话"）
 */
enum class ChatRole(val value: String) {
    /** 用户角色 */
    USER("user"),

    /** AI 助手角色 */
    ASSISTANT("assistant"),

    /** 系统角色 */
    SYSTEM("system");

    companion object {
        /**
         * 根据字符串值查找对应的 ChatRole
         * 未知值默认返回 USER
         */
        fun fromValue(value: String): ChatRole {
            return entries.find { it.value == value } ?: USER
        }
    }
}

/**
 * 聊天页面状态
 *
 * MVI 架构中的 Model 层，持有页面的所有状态。
 * 状态是 Immutable 的，每次状态变化都创建新的 State 对象。
 *
 * @param messages 聊天消息列表（按时间顺序排列）
 * @param inputText 输入框当前的文本
 * @param isAiTyping AI 是否正在"思考"（显示 typing 指示器）
 * @param errorMessage 错误信息，null 表示无错误
 *
 * @see ChatIntent 用户意图（触发状态变化的操作）
 * @see ChatViewModel 状态管理逻辑
 */
data class ChatState(
    /** 聊天消息列表 */
    val messages: List<ChatMessage> = emptyList(),

    /** 输入框文本 */
    val inputText: String = "",

    /** AI 正在输入标志（显示 typing 动画） */
    val isAiTyping: Boolean = false,

    /** 错误信息 */
    val errorMessage: String? = null
) {
    companion object {
        /** 初始状态 */
        val Initial = ChatState()
    }
}

/**
 * 聊天页面用户意图（User Intent）
 * —————————————————————————————————————————————————————
 * 页面上的每一个用户操作都对应一个 Intent。
 * ViewModel 收到 Intent 后执行业务逻辑，然后更新 State。
 *
 * 为什么用 sealed interface？
 * —————————————————————————————————————————————————————
 * sealed interface 保证所有 Intent 子类都在本文件内定义，
 * when 表达式可以做到穷尽检查（exhaustive），新增 Intent 时编译器会提示补全。
 *
 * @see ChatViewModel.sendIntent 处理所有 Intent
 */
sealed interface ChatIntent {
    /**
     * 更新输入框文本
     * 每次键盘输入都会触发此 Intent
     *
     * @param text 新的输入文本
     */
    data class UpdateInput(val text: String) : ChatIntent

    /**
     * 发送消息
     * 用户点击发送按钮或按回车时触发
     * - 将用户输入追加到消息列表
     * - 触发 AI 模拟回复
     */
    data object SendMessage : ChatIntent

    /**
     * 清空聊天记录
     * 用户点击清空按钮时触发
     */
    data object ClearChat : ChatIntent
}

/**
 * 聊天页面副作用（Effect）
 * —————————————————————————————————————————————————————
 * 一次性事件，不可变，只能被消费一次。
 * UI 层通过 LaunchedEffect + flow.collect{} 监听并处理。
 *
 * 为什么用 Channel 而不是 StateFlow？
 * —————————————————————————————————————————————————————
 * StateFlow 会记住当前值，新订阅者会收到上一次的值。
 * Channel 只传递新事件，适合"一次性"事件（Toast、滚动）。
 * 导航事件用 Channel，避免重复导航。
 *
 * @see ChatViewModel 中通过 _effect.send() 发送 Effect
 */
sealed interface ChatEffect {
    /**
     * 显示 Toast
     *
     * @param message Toast 文本内容
     */
    data class ShowToast(val message: String) : ChatEffect

    /**
     * 滚动到消息列表底部
     * 发送消息或收到 AI 回复后触发
     */
    data object ScrollToBottom : ChatEffect
}
