package com.yourpackage.ui.theme

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

// ============================================================
// 使用示例
// ============================================================

/**
 * 在 Compose 中使用涨跌主题的最简示例
 */
@Composable
fun StockThemeDemo() {
    // 实际项目中这个状态应该来自你的设置存储（DataStore/SharedPreferences）
    var currentStyle by remember { mutableStateOf(AppThemeStyle.MODERN_LIGHT) }
    var currentMode by remember { mutableStateOf(StockColorMode.GREEN_UP) }

    StockTheme(style = currentStyle, colorMode = currentMode) {
        val colors = StockColors.get()

        Column {
            // 蜡烛图标
            CandlestickIcon(modifier = Modifier.size(48.dp))

            // 涨跌颜色示例
            Row {
                Text("上涨色:", color = colors.upText)
                Text("  绿涨时绿  ", color = colors.upFunctional)
                Text("  红涨时红  ", color = colors.upText)
            }

            Row {
                Text("下跌色:", color = colors.downText)
                Text("  绿涨时红  ", color = colors.downFunctional)
                Text("  红涨时绿  ", color = colors.downText)
            }

            // 快捷访问基础色
            val base = StockColors.base()
            Text("背景色: ${base.backgroundL3}", color = base.textL1)
            Text("边框色: ${base.borderL1}", color = base.textL2)
        }
    }
}

/**
 * 切换涨跌模式的示例
 */
@Composable
fun StockModeSwitcher() {
    var colorMode by remember { mutableStateOf(StockColorMode.GREEN_UP) }

    StockTheme(style = AppThemeStyle.MODERN_LIGHT, colorMode = colorMode) {
        val colors = StockColors.get()

        Column {
            CandlestickIcon(modifier = Modifier.size(32.dp))

            // 点击切换涨跌模式
            Text(
                text = if (colorMode == StockColorMode.GREEN_UP) "当前：绿涨红跌" else "当前：红涨绿跌",
                color = colors.upText
            )

            // 切换按钮（实际用 Button）
            Text(
                text = "切换模式",
                color = colors.upButtonText,
                modifier = Modifier.size(100.dp, 48.dp)
            )
        }
    }
}

/**
 * 完整主题切换示例（4种基础主题 × 2种涨跌模式）
 */
@Composable
fun FullThemeSwitcher() {
    var style by remember { mutableStateOf(AppThemeStyle.MODERN_LIGHT) }
    var mode by remember { mutableStateOf(StockColorMode.GREEN_UP) }

    StockTheme(style = style, colorMode = mode) {
        val colors = StockColors.get()
        val base = StockColors.base()

        Column {
            // 显示当前主题
            Text("主题: ${style.name}  涨跌: ${mode.name}", color = base.textL1)

            // 蜡烛图标
            Row {
                AppThemeStyle.entries.forEach { s ->
                    StockTheme(style = s, colorMode = mode) {
                        CandlestickIcon(modifier = Modifier.size(24.dp))
                    }
                }
            }

            // 涨跌模式切换
            Row {
                StockColorMode.entries.forEach { m ->
                    StockTheme(style = style, colorMode = m) {
                        CandlestickIcon(modifier = Modifier.size(24.dp))
                    }
                }
            }
        }
    }
}
