package com.mvi.kenny.base

import androidx.compose.runtime.compositionLocalOf
import com.mvi.kenny.util.ThemeMode

/**
 * ============================================================
 * ThemeContext — 全局主题上下文
 * ============================================================
 * 通过 CompositionLocal 分发给整个 Compose 树，子组件无需接收参数即可获取主题状态。
 *
 * 使用场景：
 * - 在任何 Composable 中通过 rememberThemeContext() 获取当前主题
 * - SettingsDialog 需要读取和修改 themeMode / language
 *
 * @param themeMode 当前主题模式（Light / Dark / System）
 * @param language 当前语言代码（如 "zh"、"en"、"ar"）
 * @param isDarkTheme 是否当前处于深色模式（由 themeMode 和系统设置共同决定）
 * @param isRtl 当前语言是否需要从右到左布局
 * @param onThemeModeChange 主题模式变更回调，传入新模式
 * @param onLanguageChange 语言变更回调，传入新语言代码。调用后 Activity 会 recreate()
 *
 * @see BaseActivity 中创建 ThemeContext 的逻辑
 * @see rememberThemeContext AppContent.kt 中用于获取此上下文的 Composable 函数
 */
data class ThemeContext(
    val themeMode: ThemeMode,
    val language: String,
    val isDarkTheme: Boolean,
    val isRtl: Boolean,
    val onThemeModeChange: (ThemeMode) -> Unit,
    val onLanguageChange: (String) -> Unit
)

/**
 * ============================================================
 * LocalThemeContext — CompositionLocal 载体
 * ============================================================
 * CompositionLocal 是 Compose 提供的隐式传值机制。
 * 此 Local 通过 BaseActivity 中的 CompositionLocalProvider 分发，
 * 子树中的 Composable 通过 rememberThemeContext() 访问。
 *
 * @throws IllegalStateException 如果在 ThemeContext 未提供的组件树外调用
 * @see ThemeContext 数据类
 * @see com.mvi.kenny.ui.theme.rememberThemeContext 获取方法
 */
val LocalThemeContext = compositionLocalOf<ThemeContext> {
    error("ThemeContext not provided in this composition. Did you call rememberThemeContext() outside of BaseActivity screen()?")
}
