package com.mvi.kenny.feature.home

/**
 * ============================================================
 * HomeContract — 首页 MVI 契约
 * ============================================================
 * 采用 MVI（Model-View-Intent）架构模式。
 *
 * MVI 三要素：
 * - Model（State）：页面状态的唯一真相来源，Immutable 数据类
 * - View：Composable 函数，消费 State，渲染 UI
 * - Intent：用户意图（用户操作），ViewModel 收到 Intent 后执行业务逻辑
 *
 * Effect：一次性副作用（导航、Toast），通过 Channel 传递
 * —————————————————————————————————————————————————————
 */

/**
 * 首页页面状态
 *
 * @param isLoading 初始加载中（显示加载指示器）
 * @param isRefreshing 下拉刷新中（列表页特有）
 * @param user 当前登录用户，null 表示未登录
 * @param errorMessage 错误信息，null 表示无错误
 */
data class HomeState(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val user: HomeUser? = null,
    val errorMessage: String? = null
) {
    companion object {
        /** 初始状态 */
        val Initial = HomeState()
    }
}

/**
 * 用户数据模型
 *
 * @param id 用户唯一标识
 * @param name 显示名称
 * @param email 用户邮箱
 * @param avatar 头像 URL，null 表示使用默认头像
 */
data class HomeUser(
    val id: Long,
    val name: String,
    val email: String,
    val avatar: String? = null
)

/**
 * 首页用户意图（User Intent）
 * —————————————————————————————————————————————————————
 * 页面上的每一个用户操作都对应一个 Intent。
 * ViewModel 收到 Intent 后执行业务逻辑，然后更新 State。
 *
 * @see HomeViewModel.sendIntent 处理所有 Intent
 */
sealed interface HomeIntent {
    /** 加载用户（初始加载） */
    data object LoadUser : HomeIntent

    /** 下拉刷新用户信息 */
    data object RefreshUser : HomeIntent

    /** 更新用户昵称
     * @param name 新的昵称
     */
    data class UpdateName(val name: String) : HomeIntent

    /** 退出登录 */
    data object Logout : HomeIntent

    /** 跳转到列表页（Tab 内跳转，不需要 Activity 跳转） */
    data object GoToList : HomeIntent
}

/**
 * 首页副作用（Effect）
 * —————————————————————————————————————————————————————
 * 一次性事件，不可变，只能被消费一次。
 * UI 层通过 LaunchedEffect + flow.collect{} 监听并处理。
 *
 * @see HomeViewModel 中通过 _effect.send() 发送 Effect
 */
sealed interface HomeEffect {
    /** 显示 Toast
     * @param message Toast 文本
     */
    data class ShowToast(val message: String) : HomeEffect

    /** 显示错误（可用于在页面内显示错误卡片）
     * @param message 错误描述
     */
    data class ShowError(val message: String) : HomeEffect

    /** 导航到登录页（退出登录后触发） */
    data object NavigateToLogin : HomeEffect

    /** 导航到列表页（Tab 间切换，通过回调通知 MainScreen） */
    data object NavigateToList : HomeEffect
}
