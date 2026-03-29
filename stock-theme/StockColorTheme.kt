package com.yourpackage.ui.theme

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

// ============================================================
// 基础颜色定义（来自 attrs.xml + styles.xml 里的硬编码值）
// ============================================================
data class BaseColors(
    // 文字色
    val textL1: Color,
    val textL2: Color,
    val textL3: Color,
    val textL4: Color,
    val textL5: Color,
    val textGreen: Color,
    val textRed: Color,
    // 背景色
    val backgroundL1: Color,
    val backgroundL2: Color,
    val backgroundL3: Color,
    val backgroundPopUp: Color,
    val backgroundToast: Color,
    // 边框色
    val borderL1: Color,
    val borderL2: Color,
    // 功能色
    val functionalBlue: Color,
    val functionalOrange: Color,
    val functionalWhite: Color,
    val functionalBlack: Color,
    val functionalYellow: Color,
    // 涨跌功能色（GreenUp/RedUp 模式共用）
    val functionalGreen: Color,
    val functionalRed: Color,
    val functionalGreenBg: Color,
    val functionalRedBg: Color,
    // 按钮色
    val buttonBlueFill: Color,
    val buttonBlueText: Color,
    val buttonGreenFill: Color,
    val buttonGreenText: Color,
    val buttonRedFill: Color,
    val buttonRedText: Color,
    // K线 MA 颜色
    val klineMa1: Color,
    val klineMa2: Color,
    val klineMa3: Color,
    val klineMa4: Color,
    val klineMa5: Color,
    val klineMa6: Color,
    val klineMa7: Color,
    val klineMa8: Color,
)

val ClassicLightColors = BaseColors(
    textL1 = Color(0xFF000000), textL2 = Color(0xFF595959),
    textL3 = Color(0xFF8C8C8C), textL4 = Color(0xFFB3B3B3),
    textL5 = Color(0xFFC8C8C8),
    textGreen = Color(0xFF07B067), textRed = Color(0xFFF24966),
    backgroundL1 = Color(0xFFFFFFFF), backgroundL2 = Color(0xFFF6F6F6),
    backgroundL3 = Color(0xFFF3F3F3), backgroundPopUp = Color(0xFFFFFFFF),
    backgroundToast = Color(0xFF4D4D4D),
    borderL1 = Color(0xFFDDDDDD), borderL2 = Color(0xFFEBEBEB),
    functionalBlue = Color(0xFF007FFF), functionalOrange = Color(0xFFFF9500),
    functionalWhite = Color(0xFFFFFFFF), functionalBlack = Color(0xFF000000),
    functionalYellow = Color(0xFFEBFF39),
    functionalGreen = Color(0xFF07B067), functionalRed = Color(0xFFF24966),
    functionalGreenBg = Color(0x1907B067), functionalRedBg = Color(0x19F24966),
    buttonBlueFill = Color(0xFF007FFF), buttonBlueText = Color(0xFFFFFFFF),
    buttonGreenFill = Color(0xFF07B067), buttonGreenText = Color(0xFFFFFFFF),
    buttonRedFill = Color(0xFFF24966), buttonRedText = Color(0xFFFFFFFF),
    klineMa1 = Color(0xFFC566D5), klineMa2 = Color(0xFF27C6DA),
    klineMa3 = Color(0xFF584EEE), klineMa4 = Color(0xFFEC407A),
    klineMa5 = Color(0xFFFF9500), klineMa6 = Color(0xFFFFC042),
    klineMa7 = Color(0xFF66BB6A), klineMa8 = Color(0xFF007FFF),
)

val ModernLightColors = BaseColors(
    textL1 = Color(0xFF000000), textL2 = Color(0xFF595959),
    textL3 = Color(0xFF8C8C8C), textL4 = Color(0xFFB3B3B3),
    textL5 = Color(0xFFC8C8C8),
    textGreen = Color(0xFF236808), textRed = Color(0xFFB21012),
    backgroundL1 = Color(0xFFFFFFFF), backgroundL2 = Color(0xFFF6F6F6),
    backgroundL3 = Color(0xFFF3F3F3), backgroundPopUp = Color(0xFFFFFFFF),
    backgroundToast = Color(0xFF4D4D4D),
    borderL1 = Color(0xFFDDDDDD), borderL2 = Color(0xFFEBEBEB),
    functionalBlue = Color(0xFF007FFF), functionalOrange = Color(0xFFFF9500),
    functionalWhite = Color(0xFFFFFFFF), functionalBlack = Color(0xFF000000),
    functionalYellow = Color(0xFFEBFF39),
    functionalGreen = Color(0xFF68B300), functionalRed = Color(0xFFFF415B),
    functionalGreenBg = Color(0x3368B300), functionalRedBg = Color(0x33D7737D),
    buttonBlueFill = Color(0xFF000000), buttonBlueText = Color(0xFFFFFFFF),
    buttonGreenFill = Color(0x3368B300), buttonGreenText = Color(0xFF236808),
    buttonRedFill = Color(0x33D7737D), buttonRedText = Color(0xFFB21012),
    klineMa1 = Color(0xFFC566D5), klineMa2 = Color(0xFF27C6DA),
    klineMa3 = Color(0xFF584EEE), klineMa4 = Color(0xFFEC407A),
    klineMa5 = Color(0xFFFF9500), klineMa6 = Color(0xFFFFC042),
    klineMa7 = Color(0xFF66BB6A), klineMa8 = Color(0xFF007FFF),
)

val ClassicDarkColors = BaseColors(
    textL1 = Color(0xFFFFFFFF), textL2 = Color(0xFF999999),
    textL3 = Color(0xFF828282), textL4 = Color(0xFF5F5F5F),
    textL5 = Color(0xFF444444),
    textGreen = Color(0xFF06995C), textRed = Color(0xFFD9415B),
    backgroundL1 = Color(0xFF000000), backgroundL2 = Color(0xFF2B2B2B),
    backgroundL3 = Color(0xFF1F1F1F), backgroundPopUp = Color(0xFF161616),
    backgroundToast = Color(0xFF4D4D4D),
    borderL1 = Color(0xFF333333), borderL2 = Color(0xFF262626),
    functionalBlue = Color(0xFF007FFF), functionalOrange = Color(0xFFFF9500),
    functionalWhite = Color(0xFFFFFFFF), functionalBlack = Color(0xFF000000),
    functionalYellow = Color(0xFFEBFF39),
    functionalGreen = Color(0xFF06995C), functionalRed = Color(0xFFD9415B),
    functionalGreenBg = Color(0x3307B067), functionalRedBg = Color(0x33F24966),
    buttonBlueFill = Color(0xFF007FFF), buttonBlueText = Color(0xFFFFFFFF),
    buttonGreenFill = Color(0xFF06995C), buttonGreenText = Color(0xFFFFFFFF),
    buttonRedFill = Color(0xFFD9415B), buttonRedText = Color(0xFFFFFFFF),
    klineMa1 = Color(0xFFC566D5), klineMa2 = Color(0xFF27C6DA),
    klineMa3 = Color(0xFF584EEE), klineMa4 = Color(0xFFEC407A),
    klineMa5 = Color(0xFFFF9500), klineMa6 = Color(0xFFFFC042),
    klineMa7 = Color(0xFF66BB6A), klineMa8 = Color(0xFF007FFF),
)

val ModernDarkColors = BaseColors(
    textL1 = Color(0xFFFFFFFF), textL2 = Color(0xFF999999),
    textL3 = Color(0xFF828282), textL4 = Color(0xFF5F5F5F),
    textL5 = Color(0xFF444444),
    textGreen = Color(0xFF8ED670), textRed = Color(0xFFFF415B),
    backgroundL1 = Color(0xFF000000), backgroundL2 = Color(0xFF2B2B2B),
    backgroundL3 = Color(0xFF1F1F1F), backgroundPopUp = Color(0xFF161616),
    backgroundToast = Color(0xFF4D4D4D),
    borderL1 = Color(0xFF333333), borderL2 = Color(0xFF262626),
    functionalBlue = Color(0xFF007FFF), functionalOrange = Color(0xFFFF9500),
    functionalWhite = Color(0xFFFFFFFF), functionalBlack = Color(0xFF000000),
    functionalYellow = Color(0xFFEBFF39),
    functionalGreen = Color(0xFF8ED670), functionalRed = Color(0xFFFF415B),
    functionalGreenBg = Color(0x335ABF28), functionalRedBg = Color(0x33FF0000),
    buttonBlueFill = Color(0xFFFFFFFF), buttonBlueText = Color(0xFF000000),
    buttonGreenFill = Color(0x335ABF28), buttonGreenText = Color(0xFF8ED670),
    buttonRedFill = Color(0x33FF0000), buttonRedText = Color(0xFFFF415B),
    klineMa1 = Color(0xFFC566D5), klineMa2 = Color(0xFF27C6DA),
    klineMa3 = Color(0xFF007FFF), klineMa4 = Color(0xFFEC407A),
    klineMa5 = Color(0xFFFF9500), klineMa6 = Color(0xFFFFC042),
    klineMa7 = Color(0xFF66BB6A), klineMa8 = Color(0xFF007FFF),
)

// ============================================================
// 涨跌模式
// ============================================================
enum class StockColorMode {
    GREEN_UP,  // 绿涨红跌（默认）
    RED_UP     // 红涨绿跌
}

// ============================================================
// 完整颜色（含涨跌逻辑）
// ============================================================
data class AppColors(
    val base: BaseColors,
    val colorMode: StockColorMode,
) {
    val upFunctional: Color
        get() = if (colorMode == StockColorMode.GREEN_UP) base.functionalGreen else base.functionalRed

    val downFunctional: Color
        get() = if (colorMode == StockColorMode.GREEN_UP) base.functionalRed else base.functionalGreen

    val upText: Color
        get() = if (colorMode == StockColorMode.GREEN_UP) base.textGreen else base.textRed

    val downText: Color
        get() = if (colorMode == StockColorMode.GREEN_UP) base.textRed else base.textGreen

    val upButtonFill: Color
        get() = if (colorMode == StockColorMode.GREEN_UP) base.buttonGreenFill else base.buttonRedFill

    val downButtonFill: Color
        get() = if (colorMode == StockColorMode.GREEN_UP) base.buttonRedFill else base.buttonGreenFill

    val upButtonText: Color
        get() = if (colorMode == StockColorMode.GREEN_UP) base.buttonGreenText else base.buttonRedText

    val downButtonText: Color
        get() = if (colorMode == StockColorMode.GREEN_UP) base.buttonRedText else base.buttonGreenText

    val upFunctionalBg: Color
        get() = if (colorMode == StockColorMode.GREEN_UP) base.functionalGreenBg else base.functionalRedBg

    val downFunctionalBg: Color
        get() = if (colorMode == StockColorMode.GREEN_UP) base.functionalRedBg else base.functionalGreenBg
}

// ============================================================
// 主题风格
// ============================================================
enum class AppThemeStyle {
    CLASSIC_LIGHT,
    MODERN_LIGHT,
    CLASSIC_DARK,
    MODERN_DARK,
}

// ============================================================
// CompositionLocal
// ============================================================
val LocalAppColors = staticCompositionLocalOf<AppColors> {
    error("AppColors not provided, wrap content in StockTheme")
}

val LocalStockColorMode = staticCompositionLocalOf<StockColorMode> {
    error("StockColorMode not provided, wrap content in StockTheme")
}

// ============================================================
// 全局访问对象
// ============================================================
object StockColors {
    @Composable
    fun get(): AppColors = LocalAppColors.current

    @Composable
    fun base(): BaseColors = LocalAppColors.current.base

    @Composable
    fun mode(): StockColorMode = LocalStockColorMode.current
}

// ============================================================
// 主题入口
// ============================================================
@Composable
fun StockTheme(
    style: AppThemeStyle,
    colorMode: StockColorMode,
    content: @Composable () -> Unit
) {
    val baseColors = when (style) {
        AppThemeStyle.CLASSIC_LIGHT -> ClassicLightColors
        AppThemeStyle.MODERN_LIGHT  -> ModernLightColors
        AppThemeStyle.CLASSIC_DARK  -> ClassicDarkColors
        AppThemeStyle.MODERN_DARK   -> ModernDarkColors
    }

    CompositionLocalProvider(
        LocalAppColors provides AppColors(base = baseColors, colorMode = colorMode),
        LocalStockColorMode provides colorMode,
        content = content
    )
}

// ============================================================
// 涨跌蜡烛图标（Compose Canvas 实现）
// 对应 XML 中的 ic_candlestick vector：
//   - Background_L3  → 背景
//   - Text_Red       → 上涨色（根据 colorMode 取 upFunctional）
//   - Text_Green     → 下跌色（根据 colorMode 取 downFunctional）
//   - Border_L1      → 三条竖线
// ============================================================
@Composable
fun CandlestickIcon(modifier: Modifier = Modifier) {
    val colors = StockColors.get()
    Canvas(modifier = modifier.size(24.dp)) {
        // 背景圆角矩形
        drawRoundRect(
            color = colors.base.backgroundL3,
            size = size,
            cornerRadius = CornerRadius(6.dp.toPx())
        )
        // 上涨部分（蜡烛上半段）
        drawRoundRect(
            color = colors.upFunctional,
            topLeft = Offset(4.dp.toPx(), 4.dp.toPx()),
            size = Size(6.dp.toPx(), 7.226.dp.toPx()),
            cornerRadius = CornerRadius(2.dp.toPx())
        )
        // 下跌部分（蜡烛下半段）
        drawRoundRect(
            color = colors.downFunctional,
            topLeft = Offset(4.dp.toPx(), 12.774.dp.toPx()),
            size = Size(6.dp.toPx(), 7.226.dp.toPx()),
            cornerRadius = CornerRadius(2.dp.toPx())
        )
        // 三条竖线
        val lineColor = colors.base.borderL1
        drawRect(lineColor, Offset(12.dp.toPx(), 4.dp.toPx()),  Size(1.dp.toPx(), 4.dp.toPx()))
        drawRect(lineColor, Offset(12.dp.toPx(), 10.dp.toPx()), Size(1.dp.toPx(), 4.dp.toPx()))
        drawRect(lineColor, Offset(12.dp.toPx(), 16.dp.toPx()), Size(1.dp.toPx(), 4.dp.toPx()))
    }
}
