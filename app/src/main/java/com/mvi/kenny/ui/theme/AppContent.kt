package com.mvi.kenny.ui.theme

import androidx.compose.runtime.Composable
import com.mvi.kenny.base.LocalThemeContext
import com.mvi.kenny.base.ThemeContext

/**
 * 获取当前主题上下文
 *
 * 使用场景：
 * - HomeScreen、SettingsDialog 等 Composable 页面
 * - 任何在 AppContent 树内的组件
 *
 * @throws IllegalStateException 如果不在 AppContent 树内调用
 */
@Composable
fun rememberThemeContext(): ThemeContext = LocalThemeContext.current
