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
// Base Colors / 基础颜色定义
// ============================================================
// Colors defined in attrs.xml and styles.xml.
// Colors mapped from XML theme attributes to Compose Color objects.
// 来源于 attrs.xml 和 styles.xml 中定义的颜色属性，
// 将 XML 主题属性中的颜色值映射为 Compose 中的 Color 对象。

/**
 * 基础颜色数据类 / Base colors data class
 * Contains all theme colors for a single theme variant (e.g., Classic Light).
 * 包含单个主题变体（如 Classic Light）的所有主题颜色。
 *
 * @property textL1  Primary text color / 主要文字颜色
 * @property textL2  Secondary text color / 次要文字颜色
 * @property textL3  Tertiary text color / 第三级文字颜色
 * @property textL4  Fourth-level text color / 第四级文字颜色
 * @property textL5  Fifth-level text color / 第五级文字颜色
 * @property textGreen  Green text color (for rising price) / 绿色文字颜色（用于上涨）
 * @property textRed  Red text color (for falling price) / 红色文字颜色（用于下跌）
 * @property backgroundL1  Primary background color / 主要背景颜色
 * @property backgroundL2  Secondary background color / 次要背景颜色
 * @property backgroundL3  Tertiary background color / 第三级背景颜色
 * @property backgroundPopUp  Popup/modal background color / 弹窗背景颜色
 * @property backgroundToast  Toast notification background / Toast 通知背景
 * @property borderL1  Primary border color / 主要边框颜色
 * @property borderL2  Secondary border color / 次要边框颜色
 * @property functionalBlue  Functional blue color / 功能性蓝色
 * @property functionalOrange  Functional orange color / 功能性橙色
 * @property functionalWhite  Functional white color / 功能性白色
 * @property functionalBlack  Functional black color / 功能性黑色
 * @property functionalYellow  Functional yellow color / 功能性黄色
 * @property functionalGreen  Functional green color / 功能性绿色
 * @property functionalRed  Functional red color / 功能性红色
 * @property functionalGreenBg  Green background color with alpha / 带透明度的绿色背景
 * @property functionalRedBg  Red background color with alpha / 带透明度的红色背景
 * @property buttonBlueFill  Blue button fill color / 蓝色按钮填充色
 * @property buttonBlueText  Blue button text color / 蓝色按钮文字颜色
 * @property buttonGreenFill  Green button fill color / 绿色按钮填充色
 * @property buttonGreenText  Green button text color / 绿色按钮文字颜色
 * @property buttonRedFill  Red button fill color / 红色按钮填充色
 * @property buttonRedText  Red button text color / 红色按钮文字颜色
 * @property klineMa1~8  K-line moving average colors / K线均线颜色（MA1~MA8）
 */
data class BaseColors(
    // 文字色 / Text colors
    val textL1: Color, val textL2: Color, val textL3: Color,
    val textL4: Color, val textL5: Color,
    val textGreen: Color, val textRed: Color,
    // 背景色 / Background colors
    val backgroundL1: Color, val backgroundL2: Color,
    val backgroundL3: Color, val backgroundPopUp: Color,
    val backgroundToast: Color,
    // 边框色 / Border colors
    val borderL1: Color, val borderL2: Color,
    // 功能色 / Functional colors
    val functionalBlue: Color, val functionalOrange: Color,
    val functionalWhite: Color, val functionalBlack: Color,
    val functionalYellow: Color,
    // 涨跌功能色 / Stock market functional colors (shared by GreenUp/RedUp modes)
    val functionalGreen: Color, val functionalRed: Color,
    val functionalGreenBg: Color, val functionalRedBg: Color,
    // 按钮色 / Button colors
    val buttonBlueFill: Color, val buttonBlueText: Color,
    val buttonGreenFill: Color, val buttonGreenText: Color,
    val buttonRedFill: Color, val buttonRedText: Color,
    // K线均线颜色 / K-line moving average colors
    val klineMa1: Color, val klineMa2: Color, val klineMa3: Color,
    val klineMa4: Color, val klineMa5: Color, val klineMa6: Color,
    val klineMa7: Color, val klineMa8: Color,
)

/**
 * Classic Light 主题颜色 / Classic Light theme colors
 * Traditional light theme with high contrast text.
 * 传统浅色主题，文字对比度高。
 */
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

/**
 * Modern Light 主题颜色 / Modern Light theme colors
 * Modern light theme with softer colors.
 * 现代浅色主题，配色更加柔和。
 */
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

/**
 * Classic Dark 主题颜色 / Classic Dark theme colors
 * Traditional dark theme with high contrast.
 * 传统深色主题，对比度高。
 */
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

/**
 * Modern Dark 主题颜色 / Modern Dark theme colors
 * Modern dark theme with softer accent colors.
 * 现代深色主题，强调色更加柔和。
 */
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
// Stock Color Mode / 涨跌颜色模式
// ============================================================
/**
 * 涨跌颜色模式枚举 / Stock color mode enumeration
 * Defines whether green means rising or red means rising.
 * 定义绿色代表上涨还是红色代表上涨。
 *
 * @GREEN_UP  Green for rising, red for falling (default in most markets)
 *            绿涨红跌（大多数市场的默认配置）
 * @RED_UP    Red for rising, green for falling (e.g., Chinese mainland market)
 *            红涨绿跌（如中国大陆市场）
 */
enum class StockColorMode {
    GREEN_UP,  // 绿涨红跌（默认）/ Green up, red down (default)
    RED_UP     // 红涨绿跌 / Red up, green down
}

// ============================================================
// App Colors / 完整颜色（含涨跌逻辑）
// ============================================================
/**
 * 完整颜色数据类（含涨跌逻辑）/ Complete colors (including stock color logic)
 * Combines BaseColors with StockColorMode to provide adaptive up/down colors.
 * 将基础颜色与涨跌模式结合，提供自适应的涨跌颜色。
 *
 * @property base  Base colors for the current theme / 当前主题的基础颜色
 * @property colorMode  Current stock color mode / 当前涨跌颜色模式
 *
 * Up/down colors automatically switch based on colorMode:
 * 涨跌颜色会根据 colorMode 自动切换：
 * - GREEN_UP: upFunctional = base.functionalGreen, downFunctional = base.functionalRed
 * - RED_UP:   upFunctional = base.functionalRed,   downFunctional = base.functionalGreen
 */
data class AppColors(
    val base: BaseColors,
    val colorMode: StockColorMode,
) {
    // 上涨功能色 / Up (rising) functional color
    val upFunctional: Color
        get() = if (colorMode == StockColorMode.GREEN_UP) base.functionalGreen else base.functionalRed

    // 下跌功能色 / Down (falling) functional color
    val downFunctional: Color
        get() = if (colorMode == StockColorMode.GREEN_UP) base.functionalRed else base.functionalGreen

    // 上涨文字色 / Up (rising) text color
    val upText: Color
        get() = if (colorMode == StockColorMode.GREEN_UP) base.textGreen else base.textRed

    // 下跌文字色 / Down (falling) text color
    val downText: Color
        get() = if (colorMode == StockColorMode.GREEN_UP) base.textRed else base.textGreen

    // 上涨按钮填充色 / Up button fill color
    val upButtonFill: Color
        get() = if (colorMode == StockColorMode.GREEN_UP) base.buttonGreenFill else base.buttonRedFill

    // 下跌按钮填充色 / Down button fill color
    val downButtonFill: Color
        get() = if (colorMode == StockColorMode.GREEN_UP) base.buttonRedFill else base.buttonGreenFill

    // 上涨按钮文字色 / Up button text color
    val upButtonText: Color
        get() = if (colorMode == StockColorMode.GREEN_UP) base.buttonGreenText else base.buttonRedText

    // 下跌按钮文字色 / Down button text color
    val downButtonText: Color
        get() = if (colorMode == StockColorMode.GREEN_UP) base.buttonRedText else base.buttonGreenText

    // 上涨背景色（带透明度）/ Up background color (with alpha)
    val upFunctionalBg: Color
        get() = if (colorMode == StockColorMode.GREEN_UP) base.functionalGreenBg else base.functionalRedBg

    // 下跌背景色（带透明度）/ Down background color (with alpha)
    val downFunctionalBg: Color
        get() = if (colorMode == StockColorMode.GREEN_UP) base.functionalRedBg else base.functionalGreenBg
}

// ============================================================
// Theme Style / 主题风格
// ============================================================
/**
 * 主题风格枚举 / Theme style enumeration
 * Four theme dimensions: Classic/Modern × Light/Dark
 * 四种主题维度：经典/现代 × 浅色/深色
 */
enum class AppThemeStyle {
    CLASSIC_LIGHT,  // 经典浅色主题 / Classic light theme
    MODERN_LIGHT,   // 现代浅色主题 / Modern light theme
    CLASSIC_DARK,   // 经典深色主题 / Classic dark theme
    MODERN_DARK,    // 现代深色主题 / Modern dark theme
}

// ============================================================
// CompositionLocal / 组合本地变量
// ============================================================
// Provides theme colors scoped to the composition tree.
// 为组合树提供作用域内的主题颜色。
val LocalAppColors = staticCompositionLocalOf<AppColors> {
    error("AppColors not provided, wrap content in StockTheme")
    // 错误：未提供 AppColors，请使用 StockTheme 包裹内容
}

/**
 * 提供当前涨跌模式 / Provides the current stock color mode
 * Used to access StockColorMode.GREEN_UP or RED_UP anywhere in the composition tree.
 * 用于在组合树任意位置访问涨跌模式。
 */
val LocalStockColorMode = staticCompositionLocalOf<StockColorMode> {
    error("StockColorMode not provided, wrap content in StockTheme")
    // 错误：未提供 StockColorMode，请使用 StockTheme 包裹内容
}

// ============================================================
// Global Access / 全局访问对象
// ============================================================
/**
 * 主题颜色全局访问入口 / Theme colors global access entry point
 * Usage: StockColors.get() returns the current AppColors.
 * 用法：StockColors.get() 返回当前 AppColors。
 */
object StockColors {
    /**
     * 获取当前完整颜色（含涨跌逻辑）/ Get current complete colors (including up/down logic)
     * @return AppColors combining base colors and stock color mode
     */
    @Composable
    fun get(): AppColors = LocalAppColors.current

    /**
     * 获取当前基础颜色 / Get current base colors only
     * @return BaseColors for the current theme style
     */
    @Composable
    fun base(): BaseColors = LocalAppColors.current.base

    /**
     * 获取当前涨跌模式 / Get current stock color mode
     * @return StockColorMode.GREEN_UP or RED_UP
     */
    @Composable
    fun mode(): StockColorMode = LocalStockColorMode.current
}

// ============================================================
// Theme Entry Point / 主题入口
// ============================================================
/**
 * 主题统一入口 / Unified theme entry point
 * Wrap your composable content with StockTheme to provide all theme colors.
 * 使用 StockTheme 包裹 Compose 内容，以提供完整的主题颜色支持。
 *
 * @param style  Theme style (Classic/Modern × Light/Dark) / 主题风格
 * @param colorMode  Stock color mode (GreenUp or RedUp) / 涨跌颜色模式
 * @param content  Composable content to be themed / 要应用主题的 Compose 内容
 *
 * @sample
 * StockTheme(style = AppThemeStyle.MODERN_LIGHT, colorMode = StockColorMode.GREEN_UP) {
 *     CandlestickIcon()
 *     Text("涨", color = StockColors.get().upText)
 * }
 */
@Composable
fun StockTheme(
    style: AppThemeStyle,
    colorMode: StockColorMode,
    content: @Composable () -> Unit
) {
    // 根据主题风格选择对应基础颜色 / Select base colors based on theme style
    val baseColors = when (style) {
        AppThemeStyle.CLASSIC_LIGHT -> ClassicLightColors
        AppThemeStyle.MODERN_LIGHT  -> ModernLightColors
        AppThemeStyle.CLASSIC_DARK  -> ClassicDarkColors
        AppThemeStyle.MODERN_DARK   -> ModernDarkColors
    }

    // 提供主题颜色到组合树 / Provide theme colors to the composition tree
    CompositionLocalProvider(
        LocalAppColors provides AppColors(base = baseColors, colorMode = colorMode),
        LocalStockColorMode provides colorMode,
        content = content
    )
}

// ============================================================
// Candlestick Icon / 涨跌蜡烛图标
// ============================================================
/**
 * 涨跌蜡烛图标 Composable（Compose Canvas 实现）
 * Candlestick icon with adaptive colors based on StockColorMode.
 * 对应 XML 中的 ic_candlestick vector，涨跌颜色根据 StockColorMode 自动适配。
 *
 * Color mapping / 颜色映射：
 * - Background_L3  → 背景色 / background color (colors.base.backgroundL3)
 * - Text_Red       → 上涨色（GreenUp时）/ up color when GREEN_UP (colors.upFunctional)
 * - Text_Green     → 下跌色（GreenUp时）/ down color when GREEN_UP (colors.downFunctional)
 * - Border_L1      → 三条竖线颜色 / three vertical line colors (colors.base.borderL1)
 *
 * @param modifier  Compose modifier / Compose 修饰符
 *
 * @sample
 * StockTheme(style = AppThemeStyle.MODERN_LIGHT, colorMode = StockColorMode.GREEN_UP) {
 *     CandlestickIcon(Modifier.size(24.dp))
 * }
 */
@Composable
fun CandlestickIcon(modifier: Modifier = Modifier) {
    val colors = StockColors.get()

    // 使用 Canvas 绘制图标 / Draw the icon using Canvas
    Canvas(modifier = modifier.size(24.dp)) {
        // 1. 背景圆角矩形 / Background rounded rectangle
        drawRoundRect(
            color = colors.base.backgroundL3,
            size = size,
            cornerRadius = CornerRadius(6.dp.toPx())
        )

        // 2. 上涨部分（蜡烛上半段）/ Up portion (upper candlestick body)
        drawRoundRect(
            color = colors.upFunctional,
            topLeft = Offset(4.dp.toPx(), 4.dp.toPx()),
            size = Size(6.dp.toPx(), 7.226.dp.toPx()),
            cornerRadius = CornerRadius(2.dp.toPx())
        )

        // 3. 下跌部分（蜡烛下半段）/ Down portion (lower candlestick body)
        drawRoundRect(
            color = colors.downFunctional,
            topLeft = Offset(4.dp.toPx(), 12.774.dp.toPx()),
            size = Size(6.dp.toPx(), 7.226.dp.toPx()),
            cornerRadius = CornerRadius(2.dp.toPx())
        )

        // 4. 三条竖线（边框色）/ Three vertical lines (border color)
        val lineColor = colors.base.borderL1
        drawRect(lineColor, Offset(12.dp.toPx(), 4.dp.toPx()),  Size(1.dp.toPx(), 4.dp.toPx()))
        drawRect(lineColor, Offset(12.dp.toPx(), 10.dp.toPx()), Size(1.dp.toPx(), 4.dp.toPx()))
        drawRect(lineColor, Offset(12.dp.toPx(), 16.dp.toPx()), Size(1.dp.toPx(), 4.dp.toPx()))
    }
}
