package com.mvi.kenny.feature.bubbles

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.geometry.Offset

/**
 * ============================================================
 * BubblesContract — Android 17 App Bubbles MVI 契约
 * ============================================================
 * MVI (Model-View-Intent) Architecture Pattern for App Bubbles.
 * 非消息类应用浮动窗口接入工具包
 *
 * 三要素：
 * - Model (State): 不可变数据类，UI 的单一数据源
 * - View: 消费 State 并渲染 UI 的 Composable 函数
 * - Intent: 用户意图（用户操作），ViewModel 处理并更新 State
 *
 * Effect: 一次性副作用（导航、Toast），通过 Channel 传递
 *
 * @see BubblesViewModel 状态管理
 * @see BubblesMainScreen 主屏幕
 * @see BubblesIntroScreen 引导首页
 * @see BubblesGradleWizardScreen Gradle 向导
 * @see BubblesComponentScreen 组件库
 * @see BubblesSizeConfigScreen 尺寸配置
 */

// =============================================================
// BubblesTab — 主 Tab 枚举
// =============================================================
/**
 * Bubbles Feature Main Tabs / Bubbles 功能主 Tab
 *
 * @param title Display title / 显示标题
 * @param titleZh Chinese title / 中文标题
 */
enum class BubblesTab(val title: String, val titleZh: String) {
    INTRO("Intro", "接入引导"),
    COMPONENTS("Components", "组件库"),
    TEMPLATES("Templates", "场景模板")
}

// =============================================================
// WizardStep — Gradle 向导步骤
// =============================================================
/**
 * Gradle Plugin Wizard Steps / Gradle 插件向导步骤
 *
 * @param stepNumber Step number (1-indexed) / 步骤编号
 * @param title Step title / 步骤标题
 */
enum class WizardStep(val stepNumber: Int, val title: String, val titleZh: String) {
    MODULE_SELECT(1, "Select Module", "选择 Module"),
    ACTIVITY_SELECT(2, "Select Activities", "选择 Activity"),
    METADATA_CONFIG(3, "Configure Metadata", "配置 Metadata"),
    MANIFEST_PREVIEW(4, "Preview Manifest", "预览 Manifest"),
    DONE(5, "Complete", "完成")
}

// =============================================================
// BubblePreset — 预设场景模板
// =============================================================
/**
 * Bubble Size Presets / 浮动窗口尺寸预设
 *
 * @param label Display label / 显示标签
 * @param labelZh Chinese label / 中文标签
 * @param defaultWidth Default width in dp / 默认宽度
 * @param defaultHeight Default height in dp / 默认高度
 */
enum class BubblePreset(
    val label: String,
    val labelZh: String,
    val defaultWidth: Dp,
    val defaultHeight: Dp
) {
    MUSIC("Music Player", "音乐播放器", 200.dp, 80.dp),
    NAVIGATION("Navigation", "导航指引", 240.dp, 320.dp),
    NOTES("Quick Notes", "笔记速记", 300.dp, 200.dp),
    TRANSLATION("Translation", "翻译结果", 280.dp, 120.dp),
    VIDEO("Video Player", "视频播放", 320.dp, 180.dp),
    CUSTOM("Custom", "自定义", 250.dp, 150.dp)
}

// =============================================================
// ScreenSize — 目标屏幕尺寸
// =============================================================
/**
 * Target screen size for simulator / 模拟器目标屏幕尺寸
 *
 * @param label Display label / 显示标签
 * @param widthDp Width in dp / 宽度
 * @param heightDp Height in dp / 高度
 */
enum class ScreenSize(val label: String, val widthDp: Dp, val heightDp: Dp) {
    PHONE("Phone", 360.dp, 800.dp),
    FOLDABLE("Foldable", 600.dp, 800.dp),
    TABLET("Tablet", 840.dp, 1200.dp)
}

// =============================================================
// BubbleComponent — 组件库组件
// =============================================================
/**
 * Component library item / 组件库项目
 *
 * @param name Component name / 组件名
 * @param nameZh Chinese name / 中文名
 * @param description Component description / 组件描述
 * @param descriptionZh Chinese description / 中文描述
 */
data class BubbleComponent(
    val name: String,
    val nameZh: String,
    val description: String,
    val descriptionZh: String,
    val category: ComponentCategory
)

/**
 * Component category / 组件分类
 */
enum class ComponentCategory(val label: String, val labelZh: String) {
    CONTAINER("Container", "容器"),
    STATE("State Management", "状态管理"),
    LIFECYCLE("Lifecycle", "生命周期"),
    PERMISSION("Permission", "权限"),
    UI("UI Components", "UI 组件")
}

// =============================================================
// BubbleScenario — 场景化 Demo 场景
// =============================================================
/**
 * Bubble scenario demo / 浮动窗口场景 Demo
 *
 * @param id Scenario ID / 场景 ID
 * @param name Scenario name / 场景名称
 * @param nameZh Chinese name / 中文名称
 * @param description Scenario description / 场景描述
 * @param descriptionZh Chinese description / 中文描述
 * @param preset Associated size preset / 关联尺寸预设
 */
data class BubbleScenario(
    val id: String,
    val name: String,
    val nameZh: String,
    val description: String,
    val descriptionZh: String,
    val preset: BubblePreset
)

// =============================================================
// BubbleMetadataConfig — Bubble 元数据配置
// =============================================================
/**
 * Bubble Metadata Configuration / Bubble 元数据配置
 *
 * @param iconResId Icon resource ID / 图标资源 ID
 * @param title Bubble title / 浮动窗口标题
 * @param shortcutId Shortcut ID / 快捷方式 ID
 * @param badgeIconResId Badge icon resource ID / 徽章图标资源 ID
 */
data class BubbleMetadataConfig(
    val iconResId: String = "",
    val title: String = "My Bubble",
    val shortcutId: String = "",
    val badgeIconResId: String = ""
)

// =============================================================
// ApplyResult — Gradle 配置应用结果
// =============================================================
/**
 * Gradle config apply result / Gradle 配置应用结果
 *
 * @param success Whether apply was successful / 是否成功
 * @param message Result message / 结果消息
 * @param manifestPath Path to modified manifest / 修改的 Manifest 路径
 */
data class ApplyResult(
    val success: Boolean,
    val message: String,
    val manifestPath: String? = null
)

// =============================================================
// BubblesMainState — 主页面状态
// =============================================================
/**
 * Bubbles Main Screen State / Bubbles 主页面状态
 *
 * @param selectedTab Currently selected tab / 当前选中的 Tab
 * @param introState Intro screen state / 引导页状态
 * @param wizardState Gradle wizard state / Gradle 向导状态
 * @param componentsState Components screen state / 组件库状态
 * @param sizeSimulatorState Size simulator state / 尺寸模拟器状态
 */
data class BubblesMainState(
    val selectedTab: BubblesTab = BubblesTab.INTRO,
    val introState: BubblesIntroState = BubblesIntroState(),
    val wizardState: BubblesGradleWizardState = BubblesGradleWizardState(),
    val componentsState: BubblesComponentsState = BubblesComponentsState(),
    val sizeSimulatorState: BubbleSizeSimulatorState = BubbleSizeSimulatorState(),
    val templatesState: BubblesTemplatesState = BubblesTemplatesState()
)

// =============================================================
// BubblesIntroState — 引导页状态
// =============================================================
/**
 * Bubbles Intro Screen State / Bubbles 引导首页状态
 *
 * @param isAnimating Whether preview animation is playing / 预览动画是否播放中
 * @param showVsPiP Whether to show Bubbles vs PiP comparison / 是否显示 vs PiP 对比
 */
data class BubblesIntroState(
    val isAnimating: Boolean = true,
    val showVsPiP: Boolean = false
)

// =============================================================
// BubblesGradleWizardState — Gradle 向导状态
// =============================================================
/**
 * Gradle Plugin Wizard State / Gradle 插件向导状态
 *
 * @param currentStep Current wizard step / 当前向导步骤
 * @param availableModules Available modules in project / 项目中可用的模块
 * @param selectedModule Selected module path / 已选模块路径
 * @param availableActivities Available activities in module / 模块中可用的 Activity
 * @param selectedActivities Selected activities to bubbleify / 已选要浮动化的 Activity
 * @param bubbleMetadata Bubble metadata configuration / Bubble 元数据配置
 * @param generatedManifestXml Generated manifest XML / 生成的 Manifest XML
 * @param isApplying Whether config is being applied / 是否正在应用配置
 * @param applyResult Apply result if any / 应用结果
 */
data class BubblesGradleWizardState(
    val currentStep: WizardStep = WizardStep.MODULE_SELECT,
    val availableModules: List<String> = listOf("app", "library"),
    val selectedModule: String = "",
    val availableActivities: List<String> = emptyList(),
    val selectedActivities: List<String> = emptyList(),
    val bubbleMetadata: BubbleMetadataConfig = BubbleMetadataConfig(),
    val generatedManifestXml: String = "",
    val isApplying: Boolean = false,
    val applyResult: ApplyResult? = null
)

// =============================================================
// BubblesComponentsState — 组件库状态
// =============================================================
/**
 * Components Screen State / 组件库页面状态
 *
 * @param selectedCategory Filter by category, null means all / 按分类过滤，null 表示全部
 * @param selectedComponent Selected component for detail view / 选中的组件（详情视图）
 * @param components All available components / 所有可用组件
 * @param playgroundState Playground state / Playground 状态
 */
data class BubblesComponentsState(
    val selectedCategory: ComponentCategory? = null,
    val selectedComponent: BubbleComponent? = null,
    val components: List<BubbleComponent> = getDefaultComponents(),
    val playgroundState: BubblePlaygroundState = BubblePlaygroundState()
)

// =============================================================
// BubblePlaygroundState — Playground 状态
// =============================================================
/**
 * Component Playground State / 组件 Playground 状态
 *
 * @param selectedComponent Currently previewed component / 当前预览的组件
 * @param componentProps Component properties / 组件属性
 * @param isDragging Whether bubble is being dragged / 浮动窗口是否正在拖动
 * @param windowPosition Window position / 窗口位置
 * @param windowSize Window size / 窗口尺寸
 */
data class BubblePlaygroundState(
    val selectedComponent: BubbleComponent? = null,
    val componentProps: Map<String, Any> = emptyMap(),
    val isDragging: Boolean = false,
    val windowPosition: Offset = Offset(100f, 200f),
    val windowSize: DpSize = DpSize(250.dp, 150.dp)
)

// =============================================================
// BubbleSizeSimulatorState — 尺寸模拟器状态
// =============================================================
/**
 * Bubble Size Simulator State / 浮动窗口尺寸模拟器状态
 *
 * @param selectedPreset Selected size preset / 选中的尺寸预设
 * @param customWidth Custom width / 自定义宽度
 * @param customHeight Custom height / 自定义高度
 * @param targetScreenSize Target screen size for preview / 预览目标屏幕尺寸
 * @param isAnimating Whether animation is playing / 动画是否播放中
 */
data class BubbleSizeSimulatorState(
    val selectedPreset: BubblePreset = BubblePreset.MUSIC,
    val customWidth: Dp = BubblePreset.MUSIC.defaultWidth,
    val customHeight: Dp = BubblePreset.MUSIC.defaultHeight,
    val targetScreenSize: ScreenSize = ScreenSize.PHONE,
    val isAnimating: Boolean = false
)

// =============================================================
// BubblesTemplatesState — 场景模板状态
// =============================================================
/**
 * Templates Screen State / 场景模板页面状态
 *
 * @param selectedScenario Selected scenario for detail / 选中的场景（详情视图）
 * @param scenarios All available scenarios / 所有可用场景
 */
data class BubblesTemplatesState(
    val selectedScenario: BubbleScenario? = null,
    val scenarios: List<BubbleScenario> = getDefaultScenarios()
)

// =============================================================
// BubblesIntent — 用户意图
// =============================================================
/**
 * Bubbles Feature User Intents / Bubbles 功能用户意图
 *
 * @see BubblesViewModel.sendIntent 处理所有意图
 */
sealed interface BubblesIntent {
    /** Select main tab / 选择主 Tab
     * @param tab Tab to select / 要选择的 Tab
     */
    data class SelectTab(val tab: BubblesTab) : BubblesIntent

    /** Start Gradle wizard / 开始 Gradle 向导
     * @param modulePath Module path / 模块路径
     */
    data class StartGradleWizard(val modulePath: String) : BubblesIntent

    /** Next wizard step / 下一步向导
     * @param currentStep Current step / 当前步骤
     */
    data class NextWizardStep(val currentStep: WizardStep) : BubblesIntent

    /** Previous wizard step / 上一步向导
     * @param currentStep Current step / 当前步骤
     */
    data class PrevWizardStep(val currentStep: WizardStep) : BubblesIntent

    /** Select module in wizard / 在向导中选择模块
     * @param module Module name / 模块名
     */
    data class SelectModule(val module: String) : BubblesIntent

    /** Toggle activity selection / 切换 Activity 选择
     * @param activity Activity name / Activity 名
     */
    data class ToggleActivity(val activity: String) : BubblesIntent

    /** Update bubble metadata / 更新 Bubble 元数据
     * @param config Metadata config / 元数据配置
     */
    data class UpdateMetadata(val config: BubbleMetadataConfig) : BubblesIntent

    /** Apply Gradle config / 应用 Gradle 配置
     * @param state Current wizard state / 当前向导状态
     */
    data class ApplyGradleConfig(val state: BubblesGradleWizardState) : BubblesIntent

    /** Reset wizard / 重置向导 */
    data object ResetWizard : BubblesIntent

    /** Select component detail / 选择组件详情
     * @param component Component to view / 要查看的组件
     */
    data class SelectComponent(val component: BubbleComponent) : BubblesIntent

    /** Clear component selection / 清除组件选择 */
    data object ClearComponentSelection : BubblesIntent

    /** Select size preset / 选择尺寸预设
     * @param preset Size preset / 尺寸预设
     */
    data class SelectSizePreset(val preset: BubblePreset) : BubblesIntent

    /** Update custom size / 更新自定义尺寸
     * @param width Width in dp / 宽度
     * @param height Height in dp / 高度
     */
    data class UpdateCustomSize(val width: Dp, val height: Dp) : BubblesIntent

    /** Update target screen size / 更新目标屏幕尺寸
     * @param screenSize Screen size / 屏幕尺寸
     */
    data class UpdateTargetScreenSize(val screenSize: ScreenSize) : BubblesIntent

    /** Select scenario / 选择场景
     * @param scenario Scenario to select / 要选择的场景
     */
    data class SelectScenario(val scenario: BubbleScenario) : BubblesIntent

    /** Clear scenario selection / 清除场景选择 */
    data object ClearScenarioSelection : BubblesIntent

    /** Update playground drag state / 更新 Playground 拖动状态
     * @param isDragging Whether dragging / 是否拖动中
     */
    data class UpdatePlaygroundDrag(val isDragging: Boolean) : BubblesIntent

    /** Update playground window position / 更新 Playground 窗口位置
     * @param position New position / 新位置
     */
    data class UpdatePlaygroundPosition(val position: Offset) : BubblesIntent

    /** Update playground window size / 更新 Playground 窗口尺寸
     * @param size New size / 新尺寸
     */
    data class UpdatePlaygroundSize(val size: DpSize) : BubblesIntent

    /** Toggle vs PiP comparison / 切换 vs PiP 对比显示 */
    data object ToggleVsPiP : BubblesIntent

    /** Toggle preview animation / 切换预览动画 */
    data object TogglePreviewAnimation : BubblesIntent
}

// =============================================================
// BubblesEffect — 副作用
// =============================================================
/**
 * Bubbles Feature Side Effects / Bubbles 功能副作用
 *
 * @see BubblesViewModel 通过 _effect.send() 发送
 */
sealed interface BubblesEffect {
    /** Show toast message / 显示 Toast 消息
     * @param message Toast text / Toast 文本
     */
    data class ShowToast(val message: String) : BubblesEffect

    /** Show error message / 显示错误消息
     * @param message Error description / 错误描述
     */
    data class ShowError(val message: String) : BubblesEffect

    /** Config apply success / 配置应用成功
     * @param manifestPath Modified manifest path / 修改的 Manifest 路径
     */
    data class ConfigApplySuccess(val manifestPath: String) : BubblesEffect

    /** Navigate to component detail / 导航到组件详情
     * @param componentName Component name / 组件名
     */
    data class NavigateToComponentDetail(val componentName: String) : BubblesEffect

    /** Navigate to scenario demo / 导航到场景 Demo
     * @param scenarioId Scenario ID / 场景 ID
     */
    data class NavigateToScenarioDemo(val scenarioId: String) : BubblesEffect

    /** Open external link / 打开外部链接
     * @param url URL to open / 要打开的 URL
     */
    data class OpenExternalLink(val url: String) : BubblesEffect

    /** Share code snippet / 分享代码片段
     * @param code Code to share / 要分享的代码
     */
    data class ShareCode(val code: String) : BubblesEffect
}

// =============================================================
// Default Data Helpers / 默认数据辅助函数
// =============================================================
/**
 * Get default component library items / 获取默认组件库列表
 */
private fun getDefaultComponents(): List<BubbleComponent> = listOf(
    BubbleComponent(
        name = "BubbleContainer",
        nameZh = "浮动窗口容器",
        description = "Root container for bubble content",
        descriptionZh = "浮动窗口内容的根容器",
        category = ComponentCategory.CONTAINER
    ),
    BubbleComponent(
        name = "BubbleState",
        nameZh = "浮动窗口状态",
        description = "State management for bubble lifecycle",
        descriptionZh = "浮动窗口生命周期状态管理",
        category = ComponentCategory.STATE
    ),
    BubbleComponent(
        name = "BubbleLifecycleOwner",
        nameZh = "生命周期所有者",
        description = "Multi-window lifecycle handling",
        descriptionZh = "多窗口生命周期处理",
        category = ComponentCategory.LIFECYCLE
    ),
    BubbleComponent(
        name = "BubblePermissionHandler",
        nameZh = "权限处理器",
        description = "SYSTEM_ALERT_WINDOW permission handling",
        descriptionZh = "SYSTEM_ALERT_WINDOW 权限处理",
        category = ComponentCategory.PERMISSION
    ),
    BubbleComponent(
        name = "BubbleDragHandle",
        nameZh = "拖动手柄",
        description = "Draggable handle component",
        descriptionZh = "可拖动的手柄组件",
        category = ComponentCategory.UI
    ),
    BubbleComponent(
        name = "BubbleResizeHandle",
        nameZh = "调整大小手柄",
        description = "Resize handle for bubble window",
        descriptionZh = "浮动窗口大小调整手柄",
        category = ComponentCategory.UI
    )
)

/**
 * Get default scenario list / 获取默认场景列表
 */
private fun getDefaultScenarios(): List<BubbleScenario> = listOf(
    BubbleScenario(
        id = "music",
        name = "Music Player",
        nameZh = "音乐播放器",
        description = "Music playback floating bubble",
        descriptionZh = "音乐播放浮动窗口",
        preset = BubblePreset.MUSIC
    ),
    BubbleScenario(
        id = "navigation",
        name = "Navigation",
        nameZh = "导航指引",
        description = "GPS navigation floating bubble",
        descriptionZh = "GPS 导航浮动窗口",
        preset = BubblePreset.NAVIGATION
    ),
    BubbleScenario(
        id = "notes",
        name = "Quick Notes",
        nameZh = "笔记速记",
        description = "Quick note taking bubble",
        descriptionZh = "快速笔记浮动窗口",
        preset = BubblePreset.NOTES
    ),
    BubbleScenario(
        id = "translation",
        name = "Translation",
        nameZh = "翻译结果",
        description = "Translation result bubble",
        descriptionZh = "翻译结果浮动窗口",
        preset = BubblePreset.TRANSLATION
    ),
    BubbleScenario(
        id = "video",
        name = "Video Player",
        nameZh = "视频小窗播放",
        description = "Picture-in-picture style video",
        descriptionZh = "画中画风格视频播放",
        preset = BubblePreset.VIDEO
    ),
    BubbleScenario(
        id = "call",
        name = "Call Controls",
        nameZh = "通话悬浮控件",
        description = "Call control floating bubble",
        descriptionZh = "通话控制浮动窗口",
        preset = BubblePreset.MUSIC
    )
)

/**
 * Color palette for Bubbles feature / Bubbles 功能配色
 */
object BubblesColors {
    val Primary = Color(0xFF6750A4)           // Material 3 Purple
    val Secondary = Color(0xFF625B71)
    val Tertiary = Color(0xFF7D5260)
    val BubblesActive = Color(0xFF4CAF50)       // Green
    val BubblesInactive = Color(0xFF9E9E9E)     // Grey
    val PiPColor = Color(0xFF2196F3)            // Blue (for Bubbles vs PiP)
    val Surface = Color(0xFFFFFBFE)
    val Background = Color(0xFFF7F2FA)          // Bubbles 特色淡紫背景
    val BubbleBackground = Color(0xFFFFFFFF)
    val BubbleCornerRadius = 16.dp
    val BubbleElevation = 8.dp
    val DragHandleWidth = 32.dp
    val DragHandleHeight = 4.dp
}
