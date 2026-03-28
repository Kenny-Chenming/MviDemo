package com.mvi.kenny.feature.login

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
 * LoginViewModel — 登录页状态管理
 * ============================================================
 */
class LoginViewModel : ViewModel() {

    private val _state = MutableStateFlow(LoginState.Initial)
    val state: StateFlow<LoginState> = _state.asStateFlow()
    val currentState: LoginState get() = _state.value

    private val _effect = Channel<LoginEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    fun sendIntent(intent: LoginIntent) {
        when (intent) {
            is LoginIntent.UpdateEmail -> updateEmail(intent.email)
            is LoginIntent.UpdatePassword -> updatePassword(intent.password)
            is LoginIntent.Login -> login()
            is LoginIntent.ClearError -> clearError()
        }
    }

    /**
     * 更新邮箱并清除全局错误
     */
    private fun updateEmail(email: String) {
        val error = if (email.isBlank()) null
                    else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) "邮箱格式不正确"
                    else null
        _state.value = _state.value.copy(email = email, emailError = error, errorMessage = null)
    }

    /**
     * 更新密码并清除全局错误
     */
    private fun updatePassword(password: String) {
        val error = if (password.isBlank()) null
                    else if (password.length < 6) "密码至少6位"
                    else null
        _state.value = _state.value.copy(password = password, passwordError = error, errorMessage = null)
    }

    /**
     * 执行登录
     * —————————————————————————————————————————————————————
     * 模拟登录请求（延迟 1500ms），演示账号：a@a.com / 123456
     */
    private fun login() {
        val s = _state.value

        // 前端校验
        val emailError = if (s.email.isBlank()) "请输入邮箱"
            else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(s.email).matches()) "邮箱格式不正确"
            else null
        val passwordError = if (s.password.isBlank()) "请输入密码"
            else if (s.password.length < 6) "密码至少6位"
            else null

        if (emailError != null || passwordError != null) {
            _state.value = _state.value.copy(emailError = emailError, passwordError = passwordError)
            return
        }

        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, errorMessage = null)
            try {
                delay(1500)  // 模拟网络请求

                if (s.email == "a@a.com" && s.password == "123456") {
                    _effect.send(LoginEffect.NavigateToHome)
                } else {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        errorMessage = "邮箱或密码错误"
                    )
                    _effect.send(LoginEffect.ShowError("邮箱或密码错误"))
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    errorMessage = "登录失败: ${e.message}"
                )
                _effect.send(LoginEffect.ShowError("登录失败: ${e.message}"))
            }
        }
    }

    private fun clearError() {
        _state.value = _state.value.copy(errorMessage = null)
    }
}
