package com.mvi.kenny.feature.login

import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.view.WindowCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mvi.kenny.MainActivity
import com.mvi.kenny.base.BaseActivity
import com.mvi.kenny.base.LoadingState
import com.mvi.kenny.base.ThemeContext
import kotlinx.coroutines.flow.collect

/**
 * ============================================================
 * LoginActivity — 登录页面Activity
 * ============================================================
 * 独立 Activity，可通过 Intent 唤起（如退出登录后跳转）。
 * 内部使用 LoginScreen 渲染 Compose 界面。
 *
 * @see LoginScreen 登录页 Composable
 */
class LoginActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
    }

    /**
     * screen — 渲染登录页
     */
    @Composable
    override fun screen(theme: ThemeContext) {
        val viewModel: LoginViewModel = viewModel()
        val state by viewModel.state.collectAsState()

        // 监听 Effect
        LaunchedEffect(Unit) {
            viewModel.effect.collect { effect ->
                when (effect) {
                    is LoginEffect.NavigateToHome -> {
                        // 登录成功 → 跳转 MainActivity 并关闭 LoginActivity
                        startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                        finish()
                    }
                    is LoginEffect.ShowError -> {
                        // 错误已在 UI 显示
                    }
                }
            }
        }

        LoginScreen(
            state = state,
            onIntent = viewModel::sendIntent,
            loadingState = if (state.isLoading) LoadingState.Loading else LoadingState.Idle,
        )
    }
}
