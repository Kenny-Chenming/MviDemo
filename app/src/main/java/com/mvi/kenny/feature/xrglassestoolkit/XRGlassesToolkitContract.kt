package com.mvi.kenny.feature.xrglassestoolkit

/**
 * ============================================================
 * XRGlassesToolkitContract — Android XR AI Glasses 开发工具包 MVI 契约
 * XRGlassesToolkitContract — Android XR AI Glasses Dev Toolkit MVI Contract
 * ============================================================
 *
 * PRD-258 | Android XR AI Glasses 开发工具包
 * Ref: memory/agency/designs/PRD-258-Android-XR-AI-Glasses开发工具包.md
 *
 * MVI Architecture:
 * - Model (State): Immutable page state, single source of truth
 * - View: Composable functions that consume State and render UI
 * - Intent: User intentions; ViewModel executes logic on receiving Intent
 * - Effect: One-time side effects (Toast, Navigation) delivered via Channel
 * —————————————————————————————————————————————————————
 *
 * Bottom Tab Navigation (5 Tabs):
 * - Tab 0: 入门指南 (HelloGlasses)
 * - Tab 1: Projected API (手机↔眼镜数据桥接)
 * - Tab 2: Compose Glimmer (眼镜端UI组件)
 * - Tab 3: Emulator 工作流 (AI Glasses Emulator)
 * - Tab 4: 隐私合规 (摄像头/麦克风隐私设计)
 */

// ============================================================
// Data Models / 数据模型
// ============================================================

/**
 * Device type for dual-device support
 * 设备类型：AI Glasses vs XR Headset
 *
 * @param displayName Human-readable name / 显示名称
 */
enum class DeviceType(val displayName: String) {
    AI_GLASSES("AI Glasses"),
    XR_HEADSET("XR Headset")
}

/**
 * Emulator status for Tab 4
 * 模拟器状态枚举
 *
 * @param displayName Status display text / 状态显示文本
 */
enum class EmulatorStatus(val displayName: String) {
    NOT_STARTED("未启动"),
    STARTING("启动中..."),
    RUNNING("运行中"),
    ERROR("启动失败")
}

/**
 * Glimmer Tab sub-navigation (Tab 3)
 * Glimmer 子 Tab 导航
 *
 * @param title Tab display title / Tab 显示标题
 */
enum class GlimmerTab(val title: String) {
    COMPONENTS("组件列表"),
    SPEC("设计规范")
}

/**
 * Privacy checklist filter
 * 隐私检查清单过滤器
 *
 * @param label Filter label / 过滤器标签
 */
enum class ChecklistFilter(val label: String) {
    ALL("全部"),
    COMPLIANT("合规"),
    NON_COMPLIANT("不合规"),
    NOT_CHECKED("未检测")
}

/**
 * Privacy checklist item data model
 * 隐私检查清单条目数据模型
 *
 * @param id Unique identifier / 唯一标识
 * @param title Item title / 条目标题
 * @param description Privacy requirement description / 隐私要求描述
 * @param isCompliant Compliance status: true=compliant, false=non-compliant, null=not checked
 *                       合规状态：true=合规, false=不合规, null=未检测
 */
data class PrivacyCheckItem(
    val id: Int,
    val title: String,
    val description: String,
    val isCompliant: Boolean? = null
)

/**
 * Projected API entry data model
 * Projected API 条目数据模型
 *
 * @param id API unique identifier / API 唯一标识
 * @param name API name / API 名称
 * @param description API function description / API 功能描述
 * @param codePreview Brief code preview / 代码预览
 * @param fullCode Complete code snippet / 完整代码
 */
data class ProjectedApi(
    val id: Int,
    val name: String,
    val description: String,
    val codePreview: String,
    val fullCode: String
)

/**
 * Glimmer component preview data model
 * Glimmer 组件预览数据模型
 *
 * @param id Component unique identifier / 组件唯一标识
 * @param name Component name / 组件名称
 * @param description Component description / 组件描述
 * @param category Component category / 组件分类
 */
data class GlimmerComponent(
    val id: Int,
    val name: String,
    val description: String,
    val category: String
)

// ============================================================
// State / 页面状态
// ============================================================

/**
 * XR Glasses Toolkit page state
 * Android XR AI Glasses 开发工具包页面状态
 *
 * MVI Architecture: Model layer, holds all page state.
 * State is Immutable — each state change creates a new State object.
 *
 * @param selectedTab Current bottom navigation tab index (0-4) / 当前选中的 Tab
 * @param deviceType Selected device type / 选中的设备类型
 * // Tab 0: 入门指南
 * @param expandedStepIndex Currently expanded step card index, null if none / 当前展开的步骤索引
 * // Tab 1: Projected API
 * @param apiList List of Projected APIs / API 列表
 * @param selectedApi Currently selected API for bottom sheet / 当前选中的 API
 * @param isBottomSheetVisible Bottom sheet visibility / 底部 Sheet 可见性
 * // Tab 2: Compose Glimmer
 * @param componentGrid List of Glimmer components / Glimmer 组件列表
 * @param fullScreenPreviewComponent Component for full-screen preview / 全屏预览的组件
 * @param glimmerTab Current Glimmer sub-tab / Glimmer 子 Tab
 * // Tab 3: Emulator Workflow
 * @param currentStep Current emulator setup step / 当前模拟器设置步骤
 * @param simulatorInput Touchpad event sequence input / 触摸板事件序列输入
 * @param emulatorStatus Current emulator status / 模拟器状态
 * // Tab 4: Privacy Compliance
 * @param checklist Privacy checklist items / 隐私检查清单
 * @param checklistFilter Current filter / 当前过滤器
 *
 * @see XRGlassesToolkitIntent
 * @see XRGlassesToolkitViewModel
 */
data class XRGlassesToolkitState(
    val selectedTab: Int = 0,
    val deviceType: DeviceType = DeviceType.AI_GLASSES,
    // Tab 0: Getting Started
    val expandedStepIndex: Int? = null,
    // Tab 1: Projected API
    val apiList: List<ProjectedApi> = emptyList(),
    val selectedApi: ProjectedApi? = null,
    val isBottomSheetVisible: Boolean = false,
    // Tab 2: Compose Glimmer
    val componentGrid: List<GlimmerComponent> = emptyList(),
    val fullScreenPreviewComponent: GlimmerComponent? = null,
    val glimmerTab: GlimmerTab = GlimmerTab.COMPONENTS,
    // Tab 3: Emulator Workflow
    val currentStep: Int = 0,
    val simulatorInput: String = "",
    val emulatorStatus: EmulatorStatus = EmulatorStatus.NOT_STARTED,
    // Tab 4: Privacy Compliance
    val checklist: List<PrivacyCheckItem> = emptyList(),
    val checklistFilter: ChecklistFilter = ChecklistFilter.ALL,
    // Loading / Error state
    val isLoading: Boolean = false,
    val error: String? = null
) {
    companion object {
        /** Initial / default state / 初始状态 */
        val Initial = XRGlassesToolkitState()
    }

    /**
     * Filtered privacy checklist based on current filter
     * 根据当前过滤器筛选后的隐私检查清单
     */
    fun filteredChecklist(): List<PrivacyCheckItem> = when (checklistFilter) {
        ChecklistFilter.ALL -> checklist
        ChecklistFilter.COMPLIANT -> checklist.filter { it.isCompliant == true }
        ChecklistFilter.NON_COMPLIANT -> checklist.filter { it.isCompliant == false }
        ChecklistFilter.NOT_CHECKED -> checklist.filter { it.isCompliant == null }
    }
}

// ============================================================
// Intent / 用户意图
// ============================================================

/**
 * User intentions for XR Glasses Toolkit
 * Android XR AI Glasses 开发工具包用户意图
 *
 * Every user interaction on the page corresponds to an Intent.
 * ViewModel receives Intent, executes business logic, then updates State.
 *
 * @see XRGlassesToolkitViewModel.sendIntent
 */
sealed interface XRGlassesToolkitIntent {

    /**
     * Switch bottom navigation tab / 切换底部 Tab
     *
     * @param index Target tab index (0-4) / 目标 Tab 索引
     */
    data class SelectTab(val index: Int) : XRGlassesToolkitIntent

    /**
     * Select device type / 选择设备类型
     *
     * @param type Target device type / 目标设备类型
     */
    data class SelectDevice(val type: DeviceType) : XRGlassesToolkitIntent

    /**
     * Toggle getting started step card expansion (Tab 0)
     * 切换入门步骤卡片展开/收起状态
     *
     * @param index Step card index / 步骤卡片索引
     */
    data class ToggleStep(val index: Int) : XRGlassesToolkitIntent

    /**
     * Select Projected API (Tab 1) / 选择 Projected API
     *
     * @param api Selected API / 选中的 API
     */
    data class SelectApi(val api: ProjectedApi) : XRGlassesToolkitIntent

    /**
     * Copy API code to clipboard (Tab 1)
     * 复制 API 代码到剪贴板
     *
     * @param apiId API identifier / API 标识符
     */
    data class CopyApiCode(val apiId: Int) : XRGlassesToolkitIntent

    /**
     * Download API code as .kt file (Tab 1)
     * 下载 API 代码为 .kt 文件
     *
     * @param apiId API identifier / API 标识符
     */
    data class DownloadApiCode(val apiId: Int) : XRGlassesToolkitIntent

    /**
     * Dismiss bottom sheet (Tab 1) / 关闭底部 Sheet
     */
    data object DismissBottomSheet : XRGlassesToolkitIntent

    /**
     * Open full-screen component preview (Tab 2)
     * 打开全屏组件预览
     *
     * @param component Component to preview / 要预览的组件
     */
    data class OpenFullScreenPreview(val component: GlimmerComponent) : XRGlassesToolkitIntent

    /**
     * Dismiss full-screen preview (Tab 2) / 关闭全屏预览
     */
    data object DismissFullScreenPreview : XRGlassesToolkitIntent

    /**
     * Switch Glimmer sub-tab (Tab 2)
     * 切换 Glimmer 子 Tab
     *
     * @param tab Target Glimmer sub-tab / 目标 Glimmer 子 Tab
     */
    data class SwitchGlimmerTab(val tab: GlimmerTab) : XRGlassesToolkitIntent

    /**
     * Update simulator touchpad event input (Tab 3)
     * 更新模拟器触摸板事件输入
     *
     * @param input JSON touchpad event sequence / JSON 触摸板事件序列
     */
    data class UpdateSimulatorInput(val input: String) : XRGlassesToolkitIntent

    /**
     * Launch AI Glasses Emulator (Tab 3)
     * 启动 AI Glasses 模拟器
     *
     * @param avdName AVD device name / AVD 设备名称
     */
    data class LaunchEmulator(val avdName: String) : XRGlassesToolkitIntent

    /**
     * Advance to next emulator setup step (Tab 3)
     * 进入下一个模拟器设置步骤
     */
    data object NextStep : XRGlassesToolkitIntent

    /**
     * Toggle privacy checklist item compliance status (Tab 4)
     * 切换隐私检查清单条目合规状态
     *
     * @param itemId Item unique identifier / 条目唯一标识
     */
    data class ToggleChecklistItem(val itemId: Int) : XRGlassesToolkitIntent

    /**
     * Filter privacy checklist (Tab 4)
     * 筛选隐私检查清单
     *
     * @param filter Target filter / 目标过滤器
     */
    data class FilterChecklist(val filter: ChecklistFilter) : XRGlassesToolkitIntent

    /**
     * Generate privacy compliance report (Tab 4)
     * 生成隐私合规报告
     */
    data object GeneratePrivacyReport : XRGlassesToolkitIntent

    /**
     * Load initial data / 加载初始数据
     */
    data object LoadData : XRGlassesToolkitIntent
}

// ============================================================
// Effect / 副作用
// ============================================================

/**
 * One-time side effects for XR Glasses Toolkit
 * Android XR AI Glasses 开发工具包副作用
 *
 * One-time events, immutable, consumed only once.
 * UI layer listens via LaunchedEffect + flow.collect {}
 *
 * @see XRGlassesToolkitViewModel
 */
sealed interface XRGlassesToolkitEffect {

    /**
     * Copy text to system clipboard / 复制文本到剪贴板
     *
     * @param text Text to copy / 要复制的文本
     */
    data class CopyToClipboard(val text: String) : XRGlassesToolkitEffect

    /**
     * Download file / 下载文件
     *
     * @param fileName File name / 文件名
     * @param content File content / 文件内容
     */
    data class DownloadFile(val fileName: String, val content: String) : XRGlassesToolkitEffect

    /**
     * Open Android Studio with project / 打开 Android Studio
     *
     * @param projectPath Project path / 项目路径
     */
    data class OpenAndroidStudio(val projectPath: String) : XRGlassesToolkitEffect

    /**
     * Execute shell command / 执行 Shell 命令
     *
     * @param command Command to execute / 要执行的命令
     */
    data class ExecuteCommand(val command: String) : XRGlassesToolkitEffect

    /**
     * Show snackbar message / 显示 Snackbar 消息
     *
     * @param message Message to display / 要显示的消息
     */
    data class ShowSnackbar(val message: String) : XRGlassesToolkitEffect

    /**
     * Share file / 分享文件
     *
     * @param filePath File path to share / 要分享的文件路径
     */
    data class ShareFile(val filePath: String) : XRGlassesToolkitEffect
}
