package com.mvi.kenny.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

/**
 * ============================================================
 * HomeViewModel — 首页状态管理
 * ============================================================
 * 继承 ViewModel，持有 HomeState（页面状态）和 HomeEffect（副作用）。
 *
 * 状态管理：
 * - _state：私有 MutableStateFlow，ViewModel 内部写入
 * - state：公开 StateFlow，供 UI 层订阅（collectAsState）
 *
 * 副作用管理：
 * - _effect：Channel（热流），缓冲区大小 BUFFERED
 * - effect：receiveAsFlow，UI 层通过 collect{} 监听
 *
 * 为什么用 Channel 而不是 StateFlow？
 * —————————————————————————————————————————————————————
 * StateFlow 会记住当前值，新订阅者会收到上一次的值。
 * Channel 只传递新事件，适合"一次性"事件（导航、Toast）。
 *
 * @see HomeState 页面状态定义
 * @see HomeIntent 用户意图
 * @see HomeEffect 副作用
 */
class HomeViewModel : ViewModel() {

    /** 页面状态（StateFlow，UI 只读） */
    private val _state = MutableStateFlow(HomeState.Initial)
    val state: StateFlow<HomeState> = _state.asStateFlow()

    /**
     * 当前状态的快照
     * 用于 Compose 中 lambda 表达式内部访问状态
     * （因为 collectAsState 是异步的，lambda 内直接访问 state.value 可能不是最新值）
     */
    val currentState: HomeState get() = _state.value

    /**
     * 副作用 Channel
     * @see HomeEffect
     */
    private val _effect = Channel<HomeEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    /** 模拟用户数据流（用于刷新场景） */
    private val _userFlow = MutableStateFlow<HomeUser?>(null)

    init {
        // ViewModel 创建时自动加载用户
        sendIntent(HomeIntent.LoadUser)
    }

    /**
     * 接收并处理用户意图
     * —————————————————————————————————————————————————————
     * 入口方法，UI 层通过 viewModel.sendIntent(intent) 调用。
     * 根据 intent 类型分发到对应的处理函数。
     *
     * @param intent 用户意图（非空）
     */
    fun sendIntent(intent: HomeIntent) {
        when (intent) {
            is HomeIntent.LoadUser -> loadUser()
            is HomeIntent.RefreshUser -> refreshUser()
            is HomeIntent.UpdateName -> updateName(intent.name)
            is HomeIntent.Logout -> logout()
            is HomeIntent.GoToList -> goToList()
        }
    }

    /**
     * 加载用户
     * —————————————————————————————————————————————————————
     * 模拟网络请求，延迟 500ms 后返回模拟用户数据。
     * - 设置 isLoading = true 显示加载指示器
     * - 失败时设置 errorMessage
     * - 成功后更新 user 字段
     */
    private fun loadUser() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, errorMessage = null)
            try {
                // 模拟网络请求
                kotlinx.coroutines.delay(500)
                val user = HomeUser(
                    id = 1L,
                    name = "Kenny",
                    email = "kenny@example.com",
                    avatar = null
                )
                _userFlow.value = user
                _state.value = _state.value.copy(isLoading = false, user = user)
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    errorMessage = "获取用户失败: ${e.message}"
                )
                _effect.send(HomeEffect.ShowError("获取用户失败: ${e.message}"))
            }
        }
    }

    /**
     * 刷新用户信息
     * —————————————————————————————————————————————————————
     * 从 _userFlow 中读取现有用户数据进行"刷新"，
     * 不重新请求完整数据（模拟快速刷新）。
     */
    private fun refreshUser() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isRefreshing = true, errorMessage = null)
            try {
                kotlinx.coroutines.delay(500)
                val user = _userFlow.value ?: return@launch
                _state.value = _state.value.copy(isRefreshing = false, user = user)
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isRefreshing = false,
                    errorMessage = "刷新失败: ${e.message}"
                )
            }
        }
    }

    /**
     * 更新用户昵称
     *
     * @param name 新的昵称
     */
    private fun updateName(name: String) {
        val currentUser = _state.value.user ?: return
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            try {
                kotlinx.coroutines.delay(300)
                val updated = currentUser.copy(name = name)
                _userFlow.value = updated
                _state.value = _state.value.copy(isLoading = false, user = updated)
                _effect.send(HomeEffect.ShowToast("更新成功"))
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    errorMessage = "更新失败: ${e.message}"
                )
                _effect.send(HomeEffect.ShowError("更新失败: ${e.message}"))
            }
        }
    }

    /**
     * 退出登录
     * —————————————————————————————————————————————————————
     * 清空用户状态，发送 NavigateToLogin Effect。
     * 注意：登录状态的清除只是本地状态，真实场景需要清除 Token 等。
     */
    private fun logout() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            try {
                kotlinx.coroutines.delay(200)
                _userFlow.value = null
                _state.value = _state.value.copy(isLoading = false, user = null)
                _effect.send(HomeEffect.NavigateToLogin)
            } catch (e: Exception) {
                _state.value = _state.value.copy(isLoading = false)
                _effect.send(HomeEffect.ShowError("退出失败: ${e.message}"))
            }
        }
    }

    /**
     * 跳转到列表页
     * —————————————————————————————————————————————————————
     * 发送 NavigateToList Effect，通知 UI 层切换到列表 Tab。
     * 注意：当前 MainScreen 已经将列表作为 Tab，不需要额外处理。
     */
    private fun goToList() {
        viewModelScope.launch {
            _effect.send(HomeEffect.NavigateToList)
        }
    }
}
