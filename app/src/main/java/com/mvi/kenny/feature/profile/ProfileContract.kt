package com.mvi.kenny.feature.profile

/**
 * ============================================================
 * ProfileContract — 个人中心 MVI 契约
 * ============================================================
 */

/**
 * 个人中心页面状态
 *
 * @param isLoading 加载中
 * @param nickname 昵称
 * @param email 邮箱
 * @param avatar 头像 URL（预留）
 * @param isLoggedIn 是否已登录
 */
data class ProfileState(
    val isLoading: Boolean = false,
    val nickname: String = "未登录用户",
    val email: String = "",
    val avatar: String? = null,
    val isLoggedIn: Boolean = false
) {
    companion object {
        val Initial = ProfileState()
    }
}

/**
 * 个人中心意图
 */
sealed interface ProfileIntent {
    data object LoadProfile : ProfileIntent
    data object Login : ProfileIntent
    data object Logout : ProfileIntent
}

/**
 * 个人中心副作用
 */
sealed interface ProfileEffect {
    data object NavigateToLogin : ProfileEffect
    data class ShowToast(val message: String) : ProfileEffect
}
