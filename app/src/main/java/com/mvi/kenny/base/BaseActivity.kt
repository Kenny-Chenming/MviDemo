package com.mvi.kenny.base

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.LayoutDirection
import com.mvi.kenny.ui.theme.AppTheme
import com.mvi.kenny.util.LocaleHelper
import com.mvi.kenny.util.SettingsManager
import com.mvi.kenny.util.ThemeMode

/**
 * ============================================================
 * 页面 Loading 状态
 * ============================================================
 * 用于统一管理页面的加载状态，BaseActivity 可根据此状态显示全局 Loading 遮罩
 *
 * @see LoadingOverlay 全局 Loading 遮罩组件
 */
sealed class LoadingState {
    /** 空闲状态，不显示 Loading */
    data object Idle : LoadingState()

    /** 加载中，显示 Loading 遮罩 */
    data object Loading : LoadingState()

    /** 错误状态，显示错误信息
     * @param message 错误描述文本
     */
    data class Error(val message: String) : LoadingState()
}

/**
 * ============================================================
 * 全局 Effect（预留）
 * ============================================================
 * 用于跨页面的一次性副作用通知，如全局 Toast、全局错误处理、登录失效跳转等
 * 目前由各页面的 Local Effect 处理，未来可扩展为 Global Effect Channel
 *
 * @see BaseActivity.showToast 发送全局短 Toast
 * @see BaseActivity.showError 发送全局错误 Toast
 */
sealed interface GlobalEffect {
    /** 显示短 Toast
     * @param message 要显示的文本
     */
    data class ShowToast(val message: String) : GlobalEffect

    /** 显示长 Toast（通常用于错误）
     * @param message 要显示的文本
     */
    data class ShowError(val message: String) : GlobalEffect

    /** 跳转到登录页（登录失效时触发） */
    data object NavigateToLogin : GlobalEffect
}

/**
 * ============================================================
 * BaseActivity — App 所有 Activity 的基类
 * ============================================================
 * 职责：
 * 1. 语言切换：通过 attachBaseContext 注入 Locale 配置，实现动态语言切换
 * 2. 主题上下文：通过 CompositionLocal 分发 ThemeContext，供所有子组件访问
 * 3. Compose 树初始化：在 setContent 中创建 Compose 环境
 *
 * 子类只需实现 screen() 方法，传入自己的页面 Composable
 *
 * @see attachBaseContext 语言切换核心方法
 * @see screen 子类必须实现的抽象方法
 */
abstract class BaseActivity : ComponentActivity() {

    /** SettingsManager 实例，通过 getInstance() 获取单例 */
    private lateinit var settingsManager: SettingsManager

    /**
     * 语言切换的关键入口
     * —————————————————————————————————————————————————————
     * 每次 Activity 重建（recreate、语言切换等）时，系统会调用此方法。
     * 在这里读取 SettingsManager 中保存的语言偏好，然后用 LocaleHelper
     * 创建一个注入了目标语言的 Context，替换掉 Activity 原本的 Context。
     * 这样 Activity 就会使用新的语言资源。
     *
     * 注意：SettingsManager.language 使用 commit()（同步写入）而非 apply()（异步），
     * 确保 recreate() 时语言配置已经持久化完成。
     *
     * @param newBase Activity 原始的 Context（未注入语言）
     */
    override fun attachBaseContext(newBase: Context) {
        val settings = SettingsManager.getInstance(newBase)
        super.attachBaseContext(LocaleHelper.setLocale(newBase, settings.language))
    }

    /**
     * Activity 初始化入口
     * —————————————————————————————————————————————————————
     * 1. 获取 SettingsManager 单例
     * 2. 创建 Compose 环境（setContent）
     * 3. 在 Compose 树中初始化主题状态（themeMode / language）
     * 4. 创建 ThemeContext 并通过 CompositionLocal 分发给子组件
     * 5. 应用深色/浅色主题和 RTL 布局方向
     * 6. 调用子类的 screen() 方法渲染页面
     *
     * @param savedInstanceState Activity 状态恢复数据
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        settingsManager = SettingsManager.getInstance(this)

        setContent {
            // 从 SharedPreferences 读取当前设置，作为 Compose 状态的初始值
            // mutableStateOf 保证状态变化时自动触发重组（recomposition）
            var themeMode by remember { mutableStateOf(settingsManager.themeMode) }
            var language by remember { mutableStateOf(settingsManager.language) }

            // 根据 themeMode 确定是否深色模式
            // ThemeMode.SYSTEM 时跟随系统设置
            val isDark = when (themeMode) {
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
            }

            // 判断当前语言是否为 RTL（从右到左）
            // RTL 语言需要特殊布局方向处理
            val isRtl = LocaleHelper.isRtlLanguage(language)
            val layoutDir = if (isRtl) LayoutDirection.Rtl else LayoutDirection.Ltr

            // 创建 ThemeContext，通过回调让子组件可以修改主题/语言
            // remember(themeMode, language) 在这两个值变化时重建 ThemeContext
            val theme = remember(themeMode, language) {
                ThemeContext(
                    themeMode = themeMode,
                    language = language,
                    isDarkTheme = isDark,
                    isRtl = isRtl,
                    onThemeModeChange = { newMode ->
                        themeMode = newMode
                        settingsManager.themeMode = newMode
                        // 注：主题变化不需要 recreate()，Compose 会自动应用新主题
                    },
                    onLanguageChange = { newLang ->
                        language = newLang
                        settingsManager.language = newLang
                        // 语言变化必须 recreate() 才能生效，因为资源是 Context 级别的
                        recreate()
                    }
                )
            }

            // 应用主题配置
            AppTheme(
                darkTheme = isDark,
                // SYSTEM 模式时启用动态颜色（Material You / Material Design 3）
                dynamicColor = themeMode == ThemeMode.SYSTEM,
                layoutDirection = layoutDir  // RTL 语言时自动切换布局方向
            ) {
                // 通过 CompositionLocal 分发 ThemeContext
                // 任何子 Composable 都可以通过 rememberThemeContext() 获取
                CompositionLocalProvider(LocalThemeContext provides theme) {
                    Surface(modifier = Modifier.fillMaxSize()) {
                        // 调用子类的 screen() 方法——这是子类唯一的职责
                        screen(theme)
                    }
                }
            }
        }
    }

    /**
     * 页面内容 Composable
     * —————————————————————————————————————————————————————
     * 子类实现此方法，渲染自己的页面内容
     *
     * @param theme ThemeContext，包含当前主题/语言状态和修改回调。
     *              子组件通过 rememberThemeContext() 获取。
     */
    @Composable
    protected abstract fun screen(theme: ThemeContext)

    /**
     * 显示全局短 Toast
     * 可在任何需要的地方调用（ViewModel 通过 Activity 引用等）
     *
     * @param message 要显示的文本，内容不超过 100 字符
     */
    protected fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    /**
     * 显示全局错误 Toast（时长较长）
     *
     * @param message 错误描述文本，建议 50 字符以内
     */
    protected fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
}
