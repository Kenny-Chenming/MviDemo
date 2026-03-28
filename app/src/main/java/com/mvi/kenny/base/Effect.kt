package com.mvi.kenny.base

/**
 * ============================================================
 * Effect — 通用 Effect 根类型（预留）
 * ============================================================
 * 未来可用于统一所有页面的 Effect 类型，实现全局 Effect Channel。
 * 目前各页面仍使用自己的 Effect 接口（如 HomeEffect、LoginEffect）。
 *
 * 设计思路：
 * - Effect 表示"一次性副作用"（toast、导航、对话框等）
 * - 与 State 的区别：State 是页面状态的快照，Effect 是瞬时事件
 * - Effect 通过 Channel（热流）而非 StateFlow 传递，避免漏掉事件
 *
 * @see com.mvi.kenny.feature.home.HomeEffect 首页 Effect
 * @see com.mvi.kenny.feature.login.LoginEffect 登录页 Effect
 */
sealed interface Effect {
    /** 显示短 Toast
     * @param message 要显示的文本
     */
    data class ShowToast(val message: String) : Effect
}
