package com.mvi.kenny.feature.profile

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
 * ProfileViewModel — 个人中心状态管理
 * ============================================================
 */
class ProfileViewModel : ViewModel() {

    private val _state = MutableStateFlow(ProfileState.Initial)
    val state: StateFlow<ProfileState> = _state.asStateFlow()

    private val _effect = Channel<ProfileEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    init {
        sendIntent(ProfileIntent.LoadProfile)
    }

    fun sendIntent(intent: ProfileIntent) {
        when (intent) {
            is ProfileIntent.LoadProfile -> loadProfile()
            is ProfileIntent.Login -> login()
            is ProfileIntent.Logout -> logout()
        }
    }

    /**
     * 加载个人资料（模拟已登录用户）
     */
    private fun loadProfile() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            delay(500)
            _state.value = _state.value.copy(
                isLoading = false,
                nickname = "Kenny",
                email = "kenny@example.com",
                isLoggedIn = true
            )
        }
    }

    /**
     * 登录（发送跳转 Effect，由 UI 处理实际跳转）
     */
    private fun login() {
        viewModelScope.launch {
            _effect.send(ProfileEffect.NavigateToLogin)
        }
    }

    /**
     * 退出登录
     */
    private fun logout() {
        viewModelScope.launch {
            _state.value = ProfileState.Initial
            _effect.send(ProfileEffect.ShowToast("已退出登录"))
        }
    }
}
