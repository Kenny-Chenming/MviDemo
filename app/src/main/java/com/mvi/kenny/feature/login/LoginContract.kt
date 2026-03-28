package com.mvi.kenny.feature.login

/**
 * ============================================================
 * LoginContract — 登录页 MVI 契约
 * ============================================================
 */

/**
 * 登录页状态
 *
 * @param email 邮箱输入
 * @param password 密码输入
 * @param isLoading 登录请求中
 * @param emailError 邮箱格式错误提示
 * @param passwordError 密码错误提示
 * @param errorMessage 全局错误提示（如登录失败）
 */
data class LoginState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val emailError: String? = null,
    val passwordError: String? = null,
    val errorMessage: String? = null
) {
    companion object {
        val Initial = LoginState()
    }
}

/**
 * 登录页意图
 *
 * @see LoginViewModel.sendIntent
 */
sealed interface LoginIntent {
    data class UpdateEmail(val email: String) : LoginIntent
    data class UpdatePassword(val password: String) : LoginIntent
    data object Login : LoginIntent
    data object ClearError : LoginIntent
}

/**
 * 登录页副作用
 */
sealed interface LoginEffect {
    data class ShowError(val message: String) : LoginEffect
    data object NavigateToHome : LoginEffect
}
