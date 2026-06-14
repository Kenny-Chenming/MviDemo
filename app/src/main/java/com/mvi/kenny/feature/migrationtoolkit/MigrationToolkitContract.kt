package com.mvi.kenny.feature.migrationtoolkit

// ================================================================
// MigrationToolkitContract — Material Views → Compose 迁移工具包 MVI 契约
// ================================================================
// MVI architecture contract for MDC-Android Views → Compose Migration Toolkit.
//
// PRD-266: Material Views → Compose 迁移工具包
// Design Reference: memory/agency/designs/PRD-266-Material-Views-Compose迁移工具包.md
//
// MVI 三要素 / Three pillars:
//   Model (State)  — Immutable page state, single source of truth
//   View           — Composable function, consumes State, renders UI
//   Intent         — User intentions, ViewModel processes and updates State
//   Effect         — One-time side effects (navigation, toast), via Channel
// ================================================================

import android.net.Uri
import androidx.compose.ui.graphics.Color

/**
 * ============================================================
 * MigrationTab — 工具 Tab 枚举
 * ============================================================
 * 首页 Tab 筛选枚举。
 *
 * @param title 中文标题
 * @param emoji Emoji 标识
 */
enum class MigrationTab(val title: String, val emoji: String) {
    ALL("全部", "📦"),
    ASSESSMENT("评估工具", "🔍"),
    CONVERTER("转换工具", "🔄"),
    GUIDE("指南文档", "📖")
}

/**
 * ============================================================
 * Priority — 优先级枚举
 * ============================================================
 * 工具优先级标签。
 *
 * @param label 标签文字
 * @param color 优先级颜色
 */
enum class Priority(
    val label: String,
    val color: Color,
    val emoji: String
) {
    P0("P0", Color(0xFFFF6B6B), "🔴"),
    P1("P1", Color(0xFFFFB347), "🟠"),
    P2("P2", Color(0xFF4CAF50), "🟢")
}

/**
 * ============================================================
 * MigrationToolkitState — 迁移工具包页面状态（MVI State）
 * ============================================================
 * Immutable page state, single source of truth.
 *
 * @param selectedTab 当前 Tab 筛选
 * @param tools 工具卡片列表
 * @param isLoading 是否加载中
 * @param error 错误信息
 * @param snackbarMessage Snackbar 消息
 *
 * @see MigrationTab
 * @see ToolCard
 */
data class MigrationToolkitState(
    // ── Navigation / 全局状态 ────────────────────────────────────
    val selectedTab: MigrationTab = MigrationTab.ALL,
    val tools: List<ToolCard> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val snackbarMessage: String? = null,

    // ── Assessment State ─────────────────────────────────────────
    val assessmentState: AssessmentState = AssessmentState(),

    // ── Converter State ───────────────────────────────────────────
    val converterState: ConverterState = ConverterState()
) {
    companion object {
        /** Initial/empty state */
        val Initial = MigrationToolkitState()
    }
}

/**
 * ============================================================
 * AssessmentState — 评估工具页面状态
 * ============================================================
 *
 * @param scanResult 扫描结果
 * @param isScanning 是否正在扫描
 * @param selectedFiles 选中的文件列表（Uri）
 */
data class AssessmentState(
    val scanResult: ScanResult? = null,
    val isScanning: Boolean = false,
    val selectedFiles: List<Uri> = emptyList()
)

/**
 * ============================================================
 * ConverterState — 转换器页面状态
 * ============================================================
 *
 * @param sourceXml 原始 XML 内容
 * @param convertedCode 转换后的 Compose 代码
 * @param isConverting 是否正在转换
 * @param conversionError 转换错误信息
 */
data class ConverterState(
    val sourceXml: String = "",
    val convertedCode: String = "",
    val isConverting: Boolean = false,
    val conversionError: String? = null
)

/**
 * ============================================================
 * MigrationToolkitIntent — 用户意图（User Intent）
 * ============================================================
 * Every user action corresponds to an Intent.
 * ViewModel receives Intent, processes business logic, then updates State.
 *
 * @see MigrationToolkitViewModel.sendIntent handles all Intents
 */
sealed interface MigrationToolkitIntent {

    /** 用户切换 Tab 筛选
     * @param tab 目标 Tab
     */
    data class SelectTab(val tab: MigrationTab) : MigrationToolkitIntent

    /** 用户点击工具卡片
     * @param tool 工具卡片
     */
    data class SelectTool(val tool: ToolCard) : MigrationToolkitIntent

    /** 用户开始评估（选择文件）
     * @param uris 文件 Uri 列表
     */
    data class StartAssessment(val uris: List<Uri>) : MigrationToolkitIntent

    /** 用户选择 XML 文件
     * @param uri 文件 Uri
     */
    data class SelectXmlFile(val uri: Uri) : MigrationToolkitIntent

    /** 用户输入 XML 内容
     * @param xml XML 内容
     */
    data class InputXml(val xml: String) : MigrationToolkitIntent

    /** 用户触发转换
     * @param xml XML 内容
     */
    data class ConvertXml(val xml: String) : MigrationToolkitIntent

    /** 用户复制代码
     * @param code 代码内容
     */
    data class CopyCode(val code: String) : MigrationToolkitIntent

    /** 用户导出报告
     * @param report 报告内容
     */
    data class ExportReport(val report: String) : MigrationToolkitIntent

    /** 用户关闭 Snackbar */
    data object DismissSnackbar : MigrationToolkitIntent

    /** 用户重置状态 */
    data object ResetAll : MigrationToolkitIntent
}

/**
 * ============================================================
 * MigrationToolkitEffect — 一次性副作用（Effect）
 * ============================================================
 * One-time events, immutable, can only be consumed once.
 * UI layer listens via LaunchedEffect + flow.collect{}.
 *
 * @see MigrationToolkitViewModel _effect.send() sends Effects
 */
sealed interface MigrationToolkitEffect {

    /** 显示 Snackbar 消息
     * @param message 消息文本
     */
    data class ShowSnackbar(val message: String) : MigrationToolkitEffect

    /** 复制到剪贴板
     * @param content 要复制的内容
     */
    data class CopyToClipboard(val content: String) : MigrationToolkitEffect

    /** 分享报告
     * @param content 报告内容
     */
    data class ShareReport(val content: String) : MigrationToolkitEffect

    /** 导航到子页面
     * @param route 路由名称
     */
    data class NavigateToRoute(val route: String) : MigrationToolkitEffect

    /** 显示错误
     * @param message 错误消息
     */
    data class ShowError(val message: String) : MigrationToolkitEffect
}

// ================================================================
// 数据模型 / Data Models
// ================================================================

/**
 * ============================================================
 * ToolCard — 工具卡片数据模型
 * ============================================================
 * 首页展示的工具入口卡片。
 *
 * @param id 工具 ID
 * @param name 工具名称
 * @param description 工具描述
 * @param category 工具类别（ASSESSMENT/CONVERTER/GUIDE）
 * @param priority 优先级
 * @param estimatedTime 预计节省时间
 * @param status 状态标签
 * @param icon Emoji 图标
 */
data class ToolCard(
    val id: String,
    val name: String,
    val description: String,
    val category: ToolCategory,
    val priority: Priority,
    val estimatedTime: String,
    val status: ToolStatus,
    val icon: String
)

/**
 * ============================================================
 * ToolCategory — 工具类别
 * ============================================================
 */
enum class ToolCategory {
    ASSESSMENT,  // 评估工具
    CONVERTER,   // 转换工具
    GUIDE        // 指南文档
}

/**
 * ============================================================
 * ToolStatus — 工具状态
 * ============================================================
 */
enum class ToolStatus(val label: String, val emoji: String) {
    READY("就绪", "✅"),
    COMING_SOON("即将推出", "🚧"),
    BETA("Beta", "🧪")
}

/**
 * ============================================================
 * ScanResult — 评估扫描结果
 * ============================================================
 *
 * @param totalViews 总 View 数量
 * @param totalViewGroups 总 ViewGroup 数量
 * @param maxNestingDepth 最大嵌套深度
 * @param componentStats 组件统计
 * @param complexityScore 复杂度评分（1-5星）
 * @param priorityRecommendations 优先级排序的迁移建议
 */
data class ScanResult(
    val totalViews: Int = 0,
    val totalViewGroups: Int = 0,
    val maxNestingDepth: Int = 0,
    val componentStats: List<ComponentStat> = emptyList(),
    val complexityScore: Int = 3, // 1-5星
    val priorityRecommendations: List<PriorityRecommendation> = emptyList()
)

/**
 * ============================================================
 * ComponentStat — 组件统计条目
 * ============================================================
 *
 * @param componentName 组件名（如 Button, TextView, RecyclerView）
 * @param count 出现次数
 * @param migrateEffort 迁移难度（低/中/高）
 */
data class ComponentStat(
    val componentName: String,
    val count: Int,
    val migrateEffort: MigrateEffort
)

/**
 * ============================================================
 * MigrateEffort — 迁移难度
 * ============================================================
 */
enum class MigrateEffort(val label: String, val color: Color) {
    LOW("低", Color(0xFF4CAF50)),
    MEDIUM("中", Color(0xFFFFB347)),
    HIGH("高", Color(0xFFFF6B6B))
}

/**
 * ============================================================
 * PriorityRecommendation — 优先级迁移建议
 * ============================================================
 *
 * @param priority 优先级
 * @param title 建议标题
 * @param description 建议描述
 * @param targetComponents 涉及的组件
 * @param estimatedHours 预估工时（小时）
 */
data class PriorityRecommendation(
    val priority: Priority,
    val title: String,
    val description: String,
    val targetComponents: List<String>,
    val estimatedHours: Int
)

// ================================================================
// 默认/模拟数据 / Default & Simulation Data
// ================================================================

/**
 * ============================================================
 * DEFAULT_TOOL_CARDS — 默认工具卡片列表
 * ============================================================
 * 首页展示的 10 个工具入口。
 */
val DEFAULT_TOOL_CARDS = listOf(
    // 评估工具
    ToolCard(
        id = "assessment",
        name = "迁移评估工具",
        description = "扫描 XML layouts，评估迁移复杂度，输出优先级建议",
        category = ToolCategory.ASSESSMENT,
        priority = Priority.P0,
        estimatedTime = "5 分钟",
        status = ToolStatus.READY,
        icon = "🔍"
    ),
    // 转换工具
    ToolCard(
        id = "converter",
        name = "自动化转换工具",
        description = "XML → Composable 代码自动化转换，覆盖高频组件",
        category = ToolCategory.CONVERTER,
        priority = Priority.P0,
        estimatedTime = "节省 80% 工作量",
        status = ToolStatus.READY,
        icon = "🔄"
    ),
    ToolCard(
        id = "mapping",
        name = "MDC Views → Compose 组件映射指南",
        description = "Views 组件与 Compose 组件的 API 对照表，可搜索",
        category = ToolCategory.GUIDE,
        priority = Priority.P1,
        estimatedTime = "即时查询",
        status = ToolStatus.READY,
        icon = "📊"
    ),
    // 指南文档
    ToolCard(
        id = "theme",
        name = "Material Theming 迁移指南",
        description = "colors.xml/dimens.xml/strings.xml → Compose Theme 迁移",
        category = ToolCategory.GUIDE,
        priority = Priority.P1,
        estimatedTime = "即时查询",
        status = ToolStatus.READY,
        icon = "🎨"
    ),
    ToolCard(
        id = "hybrid",
        name = "混合 App 渐进迁移策略",
        description = "Views + Compose 共存期架构，迁移检查清单",
        category = ToolCategory.GUIDE,
        priority = Priority.P1,
        estimatedTime = "即时查询",
        status = ToolStatus.READY,
        icon = "🏗️"
    ),
    ToolCard(
        id = "mdc-maintenance",
        name = "MDC-Android 维护模式解读",
        description = "Bug 修复 SLA，安全修复政策，停更时间线预测",
        category = ToolCategory.GUIDE,
        priority = Priority.P2,
        estimatedTime = "即时查询",
        status = ToolStatus.READY,
        icon = "📋"
    ),
    ToolCard(
        id = "m2-m3",
        name = "Material 2 → 3 Compose 迁移指南",
        description = "M2 → M3 breaking changes 和迁移步骤",
        category = ToolCategory.GUIDE,
        priority = Priority.P1,
        estimatedTime = "即时查询",
        status = ToolStatus.READY,
        icon = "⬆️"
    ),
    ToolCard(
        id = "expressive",
        name = "M3 Expressive APIs 迁移指南",
        description = "Experimental APIs 用法，即将 stable 的变化和迁移建议",
        category = ToolCategory.GUIDE,
        priority = Priority.P2,
        estimatedTime = "即时查询",
        status = ToolStatus.READY,
        icon = "✨"
    ),
    ToolCard(
        id = "dynamic-color",
        name = "Material 3 Dynamic Color 集成工具",
        description = "Material You 主题的 Kotlin 实现，色板映射",
        category = ToolCategory.GUIDE,
        priority = Priority.P2,
        estimatedTime = "即时查询",
        status = ToolStatus.READY,
        icon = "🌈"
    ),
    ToolCard(
        id = "styles-api",
        name = "Styles API Compose 集成指南",
        description = "新 Styles API 用法，样式定制最佳实践",
        category = ToolCategory.GUIDE,
        priority = Priority.P2,
        estimatedTime = "即时查询",
        status = ToolStatus.BETA,
        icon = "🖌️"
    )
)

/**
 * ============================================================
 * SIMULATED_SCAN_RESULT — 模拟扫描结果
 * ============================================================
 */
val SIMULATED_SCAN_RESULT = ScanResult(
    totalViews = 342,
    totalViewGroups = 89,
    maxNestingDepth = 12,
    componentStats = listOf(
        ComponentStat("Button", 47, MigrateEffort.LOW),
        ComponentStat("TextView", 98, MigrateEffort.LOW),
        ComponentStat("RecyclerView", 23, MigrateEffort.MEDIUM),
        ComponentStat("ConstraintLayout", 18, MigrateEffort.MEDIUM),
        ComponentStat("CardView", 15, MigrateEffort.LOW),
        ComponentStat("LinearLayout", 67, MigrateEffort.MEDIUM),
        ComponentStat("FrameLayout", 12, MigrateEffort.HIGH),
        ComponentStat("NestedScrollView", 8, MigrateEffort.MEDIUM),
        ComponentStat("SwipeRefreshLayout", 5, MigrateEffort.HIGH),
        ComponentStat("ViewPager2", 3, MigrateEffort.HIGH),
        ComponentStat("TabLayout", 7, MigrateEffort.MEDIUM),
        ComponentStat("FloatingActionButton", 6, MigrateEffort.LOW),
        ComponentStat("TextInputLayout", 14, MigrateEffort.MEDIUM),
        ComponentStat("Toolbar", 9, MigrateEffort.LOW)
    ),
    complexityScore = 4,
    priorityRecommendations = listOf(
        PriorityRecommendation(
            priority = Priority.P0,
            title = "优先迁移独立组件",
            description = "先迁移不含嵌套的 Button、TextView、CardView、FAB，降低风险",
            targetComponents = listOf("Button", "TextView", "CardView", "FloatingActionButton"),
            estimatedHours = 8
        ),
        PriorityRecommendation(
            priority = Priority.P0,
            title = "RecyclerView → LazyColumn/Grid",
            description = "RecyclerView 是最常用的列表组件，建议尽早迁移",
            targetComponents = listOf("RecyclerView"),
            estimatedHours = 24
        ),
        PriorityRecommendation(
            priority = Priority.P1,
            title = "ConstraintLayout → ConstraintLayout Compose / Grid",
            description = "ConstraintLayout 嵌套深的页面优先处理",
            targetComponents = listOf("ConstraintLayout"),
            estimatedHours = 16
        ),
        PriorityRecommendation(
            priority = Priority.P1,
            title = "ViewPager2 → HorizontalPager",
            description = "ViewPager2 迁移到 Compose 需要使用 accompanist pager 或官方 HorizontalPager",
            targetComponents = listOf("ViewPager2"),
            estimatedHours = 12
        ),
        PriorityRecommendation(
            priority = Priority.P2,
            title = "复杂嵌套布局最后处理",
            description = "FrameLayout + include 标签的混合嵌套布局建议最后处理",
            targetComponents = listOf("FrameLayout", "NestedScrollView", "SwipeRefreshLayout"),
            estimatedHours = 32
        )
    )
)

/**
 * ============================================================
 * SIMULATED_COMPONENT_MAPPINGS — 组件映射数据
 * ============================================================
 */
val SIMULATED_COMPONENT_MAPPINGS = listOf(
    ComponentMapping("Button", "android.widget.Button", "androidx.compose.material3.Button", "迁移：onClick → onClick，text → content", MigrateEffort.LOW),
    ComponentMapping("TextView", "android.widget.TextView", "androidx.compose.foundation.text.BasicText", "迁移：android:text → Compose Text content", MigrateEffort.LOW),
    ComponentMapping("ImageView", "android.widget.ImageView", "androidx.compose.foundation.Image", "迁移：android:src → painter / imageResource", MigrateEffort.LOW),
    ComponentMapping("CardView", "androidx.cardview.widget.CardView", "androidx.compose.material3.Card", "迁移：cardCornerRadius → shape，elevation → tonalElevation", MigrateEffort.LOW),
    ComponentMapping("RecyclerView", "androidx.recyclerview.widget.RecyclerView", "androidx.compose.foundation.lazy.LazyColumn / LazyRow", "迁移：RecyclerView.Adapter → LazyListScope，ViewHolder → item {}", MigrateEffort.MEDIUM),
    ComponentMapping("ConstraintLayout", "androidx.constraintlayout.widget.ConstraintLayout", "androidx.compose.foundation.layout.Box / Column / Row", "迁移：app:layout_constraintXxx → Modifier.align()，guideline → 分隔组件", MigrateEffort.MEDIUM),
    ComponentMapping("LinearLayout", "android.widget.LinearLayout", "androidx.compose.foundation.layout.Column / Row", "迁移：orientation → Column(vertical)/Row(horizontal)，weight → weight Modifier", MigrateEffort.MEDIUM),
    ComponentMapping("FrameLayout", "android.widget.FrameLayout", "androidx.compose.foundation.layout.Box", "迁移：FrameLayout 用于层叠时 → Box，用于简单容器 → Box", MigrateEffort.HIGH),
    ComponentMapping("FloatingActionButton", "com.google.android.material.floatingactionbutton.FloatingActionButton", "androidx.compose.material3.FloatingActionButton", "迁移：app:fabSize → size，onClick 保持不变", MigrateEffort.LOW),
    ComponentMapping("TextInputLayout", "com.google.android.material.textfield.TextInputLayout", "androidx.compose.material3.OutlinedTextField / TextField", "迁移：TextInputLayout + EditText → OutlinedTextField，hint → placeholder", MigrateEffort.MEDIUM),
    ComponentMapping("Toolbar", "androidx.appcompat.widget.Toolbar", "androidx.compose.material3.TopAppBar", "迁移：android:title → title，onMenuItemClick → TopAppBar actions", MigrateEffort.LOW),
    ComponentMapping("TabLayout", "com.google.android.material.tabs.TabLayout", "androidx.compose.material3.TabRow", "迁移：TabLayout.Tab → TabRow，addTab → tabs {}", MigrateEffort.MEDIUM),
    ComponentMapping("ViewPager2", "androidx.viewpager2.widget.ViewPager2", "androidx.compose.foundation.pager.HorizontalPager / VerticalPager", "迁移：ViewPager2 + FragmentStateAdapter → HorizontalPager + rememberPagerState", MigrateEffort.HIGH),
    ComponentMapping("SwipeRefreshLayout", "androidx.swiperefreshlayout.widget.SwipeRefreshLayout", "androidx.compose.material3.pullrefresh.PullRefreshIndicator", "迁移：SwipeRefreshLayout → Box + pullRefresh Modifier + PullRefreshIndicator", MigrateEffort.HIGH),
    ComponentMapping("NestedScrollView", "androidx.core.widget.NestedScrollView", "androidx.compose.foundation.rememberScrollState + Column", "迁移：NestedScrollView → Column(Modifier.verticalScroll(scrollState))", MigrateEffort.MEDIUM),
    ComponentMapping("RadioButton", "android.widget.RadioButton", "androidx.compose.material3.RadioButton", "迁移：android:checked → selected，onCheckedChange → onClick", MigrateEffort.LOW),
    ComponentMapping("CheckBox", "android.widget.CheckBox", "androidx.compose.material3.Checkbox", "迁移：android:checked → checked，onCheckedChange → onCheckedChange", MigrateEffort.LOW),
    ComponentMapping("Switch", "android.widget.Switch", "androidx.compose.material3.Switch", "迁移：android:checked → checked，onCheckedChange → onCheckedChange", MigrateEffort.LOW),
    ComponentMapping("ProgressBar", "android.widget.ProgressBar", "androidx.compose.material3.LinearProgressIndicator / CircularProgressIndicator", "迁移：ProgressBar → CircularProgressIndicator，style=horizontal → LinearProgressIndicator", MigrateEffort.LOW),
    ComponentMapping("SeekBar", "android.widget.SeekBar", "androidx.compose.material3.Slider", "迁移：SeekBar → Slider，OnSeekBarChangeListener → onValueChange", MigrateEffort.MEDIUM)
)

/**
 * ============================================================
 * ComponentMapping — 组件映射数据模型
 * ============================================================
 *
 * @param composeName Compose 组件名
 * @param viewsImport Views import 路径
 * @param composeImport Compose import 路径
 * @param migrationNote 迁移说明
 * @param effort 迁移难度
 */
data class ComponentMapping(
    val viewsName: String,
    val viewsImport: String,
    val composeImport: String,
    val migrationNote: String,
    val effort: MigrateEffort
)

/**
 * ============================================================
 * THEMING_MIGRATION_GUIDE — Theming 迁移指南数据
 * ============================================================
 */
val THEMING_MIGRATION_GUIDE = listOf(
    ThemingMigrationItem(
        id = "colors",
        title = "colors.xml → Compose Color",
        beforeCode = """<!-- res/values/colors.xml -->
<resources>
    <color name="primary">#6200EE</color>
    <color name="primary_variant">#3700B3</color>
    <color name="secondary">#03DAC6</color>
    <color name="background">#FFFFFF</color>
    <color name="surface">#FFFFFF</color>
    <color name="on_primary">#FFFFFF</color>
    <color name="on_secondary">#000000</color>
</resources>""",
        afterCode = """// ui/theme/Color.kt
package com.example.app.ui.theme

import androidx.compose.ui.graphics.Color

// Primary colors
val Purple500 = Color(0xFF6200EE)
val Purple700 = Color(0xFF3700B3)
val Teal200 = Color(0xFF03DAC6)

// Light theme colors
val Primary = Purple500
val PrimaryVariant = Purple700
val Secondary = Teal200
val Background = Color.White
val Surface = Color.White
val OnPrimary = Color.White
val OnSecondary = Color.Black

// Dark theme colors (if different)
val PrimaryDark = Purple500
val SecondaryDark = Teal200"""
    ),
    ThemingMigrationItem(
        id = "dimens",
        title = "dimens.xml → Compose Dimension / spacing",
        beforeCode = """<!-- res/values/dimens.xml -->
<resources>
    <dimen name="spacing_xs">4dp</dimen>
    <dimen name="spacing_sm">8dp</dimen>
    <dimen name="spacing_md">16dp</dimen>
    <dimen name="spacing_lg">24dp</dimen>
    <dimen name="spacing_xl">32dp</dimen>
    <dimen name="corner_radius">8dp</dimen>
    <dimen name="card_elevation">4dp</dimen>
</resources>""",
        afterCode = """// ui/theme/Dimens.kt
package com.example.app.ui.theme

import androidx.compose.ui.unit.dp

// Spacing constants — replace @dimen/* references with these
object Spacing {
    val xs = 4.dp
    val sm = 8.dp
    val md = 16.dp
    val lg = 24.dp
    val xl = 32.dp
}

// Shape constants
val CornerRadius = 8.dp

// Elevation constants
val CardElevation = 4.dp"""
    ),
    ThemingMigrationItem(
        id = "strings",
        title = "strings.xml → Compose StringResource (mostly unchanged)",
        beforeCode = """<!-- res/values/strings.xml -->
<resources>
    <string name="app_name">MyApp</string>
    <string name="btn_submit">Submit</string>
    <string name="error_network">Network error</string>
    <string name="format_items">%d items</string>
</resources>""",
        afterCode = """// Compose 中使用 stringResource() 引用字符串资源
// res/values/strings.xml 保持不变（推荐）
// Compose 代码中：
import androidx.compose.ui.res.stringResource
import com.example.app.R

@Composable
fun MyScreen() {
    val appName = stringResource(R.string.app_name)
    val submitText = stringResource(R.string.btn_submit)
    val errorMsg = stringResource(R.string.error_network)
    val itemsText = stringResource(R.string.format_items, 5)
}"""
    ),
    ThemingMigrationItem(
        id = "theme-xml",
        title = "themes.xml → Compose MaterialTheme",
        beforeCode = """<!-- res/values/themes.xml -->
<resources>
    <style name="Theme.MyApp" parent="Theme.MaterialComponents.DayNight.DarkActionBar">
        <item name="colorPrimary">@color/primary</item>
        <item name="colorPrimaryVariant">@color/primary_variant</item>
        <item name="colorSecondary">@color/secondary</item>
        <item name="android:statusBarColor">@color/primary_variant</item>
    </style>
</resources>""",
        afterCode = """// ui/theme/Theme.kt
package com.example.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = Purple500,
    onPrimary = Color.White,
    primaryContainer = Purple700,
    secondary = Teal200,
    onSecondary = Color.Black,
    background = Color.White,
    surface = Color.White
)

private val DarkColorScheme = darkColorScheme(
    primary = Purple500,
    onPrimary = Color.White,
    primaryContainer = Purple700,
    secondary = Teal200,
    onSecondary = Color.Black
)

@Composable
fun MyAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}"""
    )
)

/**
 * ============================================================
 * ThemingMigrationItem — Theming 迁移条目
 * ============================================================
 */
data class ThemingMigrationItem(
    val id: String,
    val title: String,
    val beforeCode: String,
    val afterCode: String
)

/**
 * ============================================================
 * HYBRID_MIGRATION_CHECKLIST — 混合 App 渐进迁移检查清单
 * ============================================================
 */
val HYBRID_MIGRATION_CHECKLIST = listOf(
    HybridCheckItem("Phase 1: 基础设施", listOf(
        "确认项目已添加 Compose 依赖（compose-bom 统一版本管理）",
        "配置 Compose Theme，将 MDC 主题颜色迁移到 Material3 ColorScheme",
        "确保 Navigation 已支持 Compose（Navigation Compose 或 Navigation2 with ComposeView）",
        "建立双主题兼容层（MDC Views 用 Theme.MaterialComponents，Compose 用 MaterialTheme）"
    )),
    HybridCheckItem("Phase 2: 独立组件迁移", listOf(
        "从不含业务逻辑的展示组件开始（Card、Button、TextView → Card、Button、Text）",
        "建立组件对照表（ComponentMapping），团队统一认知",
        "每个迁移后的 Composable 独立测试，确保行为一致",
        "建立 Design System Token（颜色/字体/间距）统一管理"
    )),
    HybridCheckItem("Phase 3: 列表和布局迁移", listOf(
        "RecyclerView → LazyColumn/LazyGrid（优先处理数据展示类列表）",
        "ConstraintLayout → Compose ConstraintLayout / Grid / FlexBox",
        "嵌套 LinearLayout → Column/Row + Arrangement",
        "处理 include 标签：提取为独立 Composable 或使用 AndroidViewBinding"
    )),
    HybridCheckItem("Phase 4: 导航和状态管理", listOf(
        "Fragment → Composable Screen（一对一替换，减少混用复杂度）",
        "ViewModel + LiveData → ViewModel + StateFlow（Compose 天然支持）",
        "共享 ViewModel 跨 Fragment 通信 → 导航参数传递",
        "按需迁移：先迁移用户交互频繁的 Screen"
    )),
    HybridCheckItem("Phase 5: 清理和优化", listOf(
        "移除所有未使用的 XML layouts",
        "清理 MDC-Android 依赖（如已全部迁移完毕）",
        "执行性能测试（重组次数、布局层级）",
        "更新 CI/CD 流程（移除 KAPT，切换到 KSP 如使用 Room）"
    ))
)

/**
 * ============================================================
 * HybridCheckItem — 混合迁移检查项
 * ============================================================
 */
data class HybridCheckItem(
    val phase: String,
    val tasks: List<String>
)
