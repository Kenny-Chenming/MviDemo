// ================================================================
// XRToolkitContract — Android XR Gemini 智能眼镜开发工具包 MVI 契约
// ================================================================
// MVI architecture contract for Android XR Gemini Smart Glasses Toolkit.
//
// PRD-267: Android XR Gemini 智能眼镜应用开发工具包
// Design Reference: memory/agency/designs/PRD-267-Android-XR-Gemini智能眼镜开发工具包.md
//
// MVI 三要素 / Three pillars:
//   State  — Immutable page state, single source of truth
//   Intent — User intentions, ViewModel processes and updates State
//   Effect — One-time side effects (navigation, toast), via Channel
// ================================================================

package com.mvi.kenny.feature.xrtoolkit

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion as ComposeColor

// ================================================================
// 页面路由 / Page Routes
// ================================================================

/**
 * ============================================================
 * XRToolkitPage — 工具包子页面路由
 * ============================================================
 *
 * @param route 路由标识符
 * @param title 中文标题
 * @param emoji Emoji 标识
 */
enum class XRToolkitPage(
    val route: String,
    val title: String,
    val emoji: String
) {
    INDEX("xr_toolkit", "XR 工具箱", "🥽"),
    GETTING_STARTED("xr_toolkit_getting_started", "入门指南", "🚀"),
    GEMINI("xr_toolkit_gemini", "Gemini 集成", "✨"),
    UI_DESIGN("xr_toolkit_ui_design", "UI 设计规范", "🎨"),
    LIVE_TRANSLATE("xr_toolkit_live_translate", "实时翻译", "🌐"),
    ARCORE_VS_XR("xr_toolkit_arcore_vs_xr", "ARCore 对比", "🔍"),
    VIDEO_CALL("xr_toolkit_video_call", "视频通话", "📹"),
    TEST_DEBUG("xr_toolkit_test_debug", "测试调试", "🔧"),
    PRIVACY("xr_toolkit_privacy", "隐私合规", "🔒"),
    CROSS_DEVICE("xr_toolkit_cross_device", "跨设备协同", "📱")
}

// ================================================================
// 视觉规范常量 / Visual Constants
// ================================================================

/**
 * XR 工具包视觉颜色规范
 * Deep blue/purple theme for immersive XR feel.
 * XR 沉浸感深色主题色。
 */
object XRToolkitColors {
    val Primary = Color(0xFF4285F4)          // Gemini Blue
    val Surface = Color(0xFF121212)           // Deep dark background
    val SurfaceVariant = Color(0xFF1E1E2E)    // Slightly lighter dark
    val OnSurface = Color(0xFFFFFFFF)          // White text on dark
    val OnSurfaceVariant = Color(0xFFB0B0C0)  // Muted text
    val TranslateOverlay = Color(0xFFFFFFFF)   // Glasses text: white
    val TranslateOverlayBg = Color(0xCC121212) // Semi-transparent dark bg
    val SafeZoneBorder = Color(0xFF4285F4)    // Safe zone indicator
    val DangerZone = Color(0xFFEA4335)       // Edge danger zone
    val SuccessGreen = Color(0xFF34A853)      // Success
    val WarningOrange = Color(0xFFFBBC04)     // Warning
    val ErrorRed = Color(0xFFEA4335)           // Error
    val PrivacyPurple = Color(0xFF7C4DFF)     // Privacy accent
    val CrossDeviceTeal = Color(0xFF00BCD4)   // Cross-device accent
}

// ================================================================
// 学习路径 / Learning Path
// ================================================================

/**
 * ============================================================
 * LearningStage — 学习阶段数据模型
 * ============================================================
 * 首页 HorizontalPager 展示的 4 个学习阶段。
 *
 * @param index 阶段编号（0-3）
 * @param title 阶段标题
 * @param description 阶段描述
 * @param icon 阶段图标
 * @param items 阶段包含的内容项
 * @param estimatedTime 预计学习时间
 */
data class LearningStage(
    val index: Int,
    val title: String,
    val description: String,
    val icon: String,
    val items: List<String>,
    val estimatedTime: String
) {
    companion object {
        /**
         * Default 4 learning stages for XR toolkit.
         * 默认的 4 个学习阶段。
         */
        val DEFAULT_STAGES = listOf(
            LearningStage(
                index = 0,
                title = "认识 Android XR",
                description = "了解什么是 Android XR，它与 Google Glass 有何不同，以及为什么现在是入局的最佳时机。",
                icon = "👁️",
                items = listOf(
                    "Android XR 是 Google 第二次进入智能眼镜市场",
                    "与 Samsung + 时尚品牌（Warby Parker/Gentle Monster）联合打造",
                    "Gemini 是主要 AI 接口，2026 年秋季上市",
                    "与 Google Glass 失败的教训：时尚 + AI 是新策略"
                ),
                estimatedTime = "10 分钟"
            ),
            LearningStage(
                index = 1,
                title = "环境搭建",
                description = "安装 Android XR SDK，配置模拟器，为第一个 XR App 做好准备。",
                icon = "⚙️",
                items = listOf(
                    "申请 Google 内部测试资格（Early Access Program）",
                    "下载并安装 Android XR SDK",
                    "配置 XR 模拟器（支持手机和眼镜两种形态）",
                    "验证开发环境：运行 HelloXR 示例项目"
                ),
                estimatedTime = "30 分钟"
            ),
            LearningStage(
                index = 2,
                title = "第一个 XR App",
                description = "基于 Android XR SDK 和 Gemini API，构建你的第一个智能眼镜应用。",
                icon = "🚀",
                items = listOf(
                    "创建 Android XR 项目结构",
                    "使用 Gemini API 实现语音输入",
                    "在眼镜视野中渲染 Gemini 的响应",
                    "处理用户交互（触摸/语音/手势）"
                ),
                estimatedTime = "45 分钟"
            ),
            LearningStage(
                index = 3,
                title = "Gemini + XR 深度集成",
                description = "将 Gemini 的多模态能力（视觉 + 语音）与 XR 眼镜深度结合，构建 AI-First 应用。",
                icon = "✨",
                items = listOf(
                    "Gemini 多模态输入：摄像头实时视觉 + 语音",
                    "实时翻译：眼镜中实时显示翻译文字",
                    "上下文感知：基于用户视野内容生成响应",
                    "跨设备协同：手机作为计算中枢，眼镜作为显示/音频端"
                ),
                estimatedTime = "60 分钟"
            )
        )
    }
}

// ================================================================
// Getting Started State
// ================================================================

/**
 * ============================================================
 * GettingStartedState — 入门页面状态
 * ============================================================
 *
 * @param sdkDownloadProgress SDK 下载进度（0.0 - 1.0）
 * @param isSdkInstalled SDK 是否已安装
 * @param emulatorConfigSteps 模拟器配置步骤
 * @param currentStep 当前正在进行的步骤索引
 * @param isHelloXRBuilt HelloXR 示例是否构建成功
 */
data class GettingStartedState(
    val sdkDownloadProgress: Float = 0f,
    val isSdkInstalled: Boolean = false,
    val emulatorConfigSteps: List<ConfigStep> = defaultConfigSteps(),
    val currentStep: Int = 0,
    val isHelloXRBuilt: Boolean = false
)

/**
 * ============================================================
 * ConfigStep — 模拟器配置步骤
 * ============================================================
 *
 * @param title 步骤标题
 * @param description 步骤描述
 * @param status 步骤状态
 * @param command 相关命令（可选）
 */
data class ConfigStep(
    val title: String,
    val description: String,
    val status: StepStatus = StepStatus.PENDING,
    val command: String? = null
)

/**
 * ============================================================
 * StepStatus — 步骤状态
 * ============================================================
 */
enum class StepStatus { PENDING, IN_PROGRESS, DONE, ERROR }

// ================================================================
// Gemini Integration State
// ================================================================

/**
 * ============================================================
 * GeminiIntegrationState — Gemini 集成页面状态
 * ============================================================
 *
 * @param inputPrompt 用户输入的 prompt
 * @param geminiResponse Gemini 返回的响应
 * @param isLoading 是否正在调用 Gemini
 * @param error 错误信息
 * @param useVoiceInput 是否使用语音输入模式
 */
data class GeminiIntegrationState(
    val inputPrompt: String = "",
    val geminiResponse: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val useVoiceInput: Boolean = false
)

// ================================================================
// UI Design State
// ================================================================

/**
 * ============================================================
 * UIDesignState — UI 设计规范页面状态
 * ============================================================
 *
 * @param selectedSpecTab 当前选中的规范 Tab
 * @param glassesSimulationActive 是否激活眼镜视野模拟
 */
data class UIDesignState(
    val selectedSpecTab: UISpecTab = UISpecTab.FONT_SIZE,
    val glassesSimulationActive: Boolean = false
)

/**
 * ============================================================
 * UISpecTab — UI 规范 Tab
 * ============================================================
 */
enum class UISpecTab(val title: String, val emoji: String) {
    FONT_SIZE("字号", "🔤"),
    COLOR("颜色", "🎨"),
    LAYOUT("布局安全区", "📐"),
    INTERACTION("交互", "👆")
}

// ================================================================
// Live Translation State
// ================================================================

/**
 * ============================================================
 * LiveTranslateState — 实时翻译页面状态
 * ============================================================
 *
 * @param sourceLanguage 源语言
 * @param targetLanguage 目标语言
 * @param translatedText 翻译文本
 * @param isTranslating 是否正在翻译
 * @param showAnimation 是否显示翻译浮现动画
 */
data class LiveTranslateState(
    val sourceLanguage: String = "English",
    val targetLanguage: String = "中文",
    val translatedText: String = "你好世界",
    val isTranslating: Boolean = false,
    val showAnimation: Boolean = true
)

// ================================================================
// XRToolkitState — 主状态
// ================================================================

/**
 * ============================================================
 * XRToolkitState — XR 工具包主页面状态（MVI State）
 * ============================================================
 * Immutable page state, single source of truth.
 *
 * @param currentPage 当前页面路由
 * @param learningPath 学习路径阶段列表
 * @param completedStages 已完成的学习阶段索引集合
 * @param selectedTool 当前选中的工具（用于首页工具入口点击）
 * @param gettingStartedState 入门页面状态
 * @param geminiState Gemini 集成页面状态
 * @param uiDesignState UI 设计规范页面状态
 * @param liveTranslateState 实时翻译页面状态
 * @param snackbarMessage Snackbar 消息
 *
 * @see XRToolkitPage
 * @see LearningStage
 */
data class XRToolkitState(
    // ── Navigation / 全局状态 ────────────────────────────────────
    val currentPage: XRToolkitPage = XRToolkitPage.INDEX,
    val learningPath: List<LearningStage> = LearningStage.DEFAULT_STAGES,
    val completedStages: Set<Int> = emptySet(),
    val selectedTool: String? = null,
    val snackbarMessage: String? = null,

    // ── Sub-page States ──────────────────────────────────────────
    val gettingStartedState: GettingStartedState = GettingStartedState(),
    val geminiState: GeminiIntegrationState = GeminiIntegrationState(),
    val uiDesignState: UIDesignState = UIDesignState(),
    val liveTranslateState: LiveTranslateState = LiveTranslateState(),

    // ── ARCore vs XR Comparison ────────────────────────────────────
    val arcoreVsXrState: ARCoreVsXRState = ARCoreVsXRState(),

    // ── Video Call ────────────────────────────────────────────────
    val videoCallState: VideoCallState = VideoCallState(),

    // ── Privacy ───────────────────────────────────────────────────
    val privacyState: PrivacyState = PrivacyState(),

    // ── Cross-Device ──────────────────────────────────────────────
    val crossDeviceState: CrossDeviceState = CrossDeviceState()
) {
    companion object {
        /** Initial/empty state */
        val Initial = XRToolkitState()
    }
}

/**
 * ============================================================
 * ARCoreVsXRState — ARCore vs XR 对比页面状态
 * ============================================================
 *
 * @param selectedScenario 当前选中的对比场景
 */
data class ARCoreVsXRState(
    val selectedScenario: String = "indoor_navigation"
)

/**
 * ============================================================
 * VideoCallState — 视频通话页面状态
 * ============================================================
 *
 * @param isCameraEnabled 摄像头是否启用
 * @param isMicrophoneEnabled 麦克风是否启用
 * @param isWebRTCConnected WebRTC 是否已连接
 */
data class VideoCallState(
    val isCameraEnabled: Boolean = true,
    val isMicrophoneEnabled: Boolean = true,
    val isWebRTCConnected: Boolean = false
)

/**
 * ============================================================
 * PrivacyState — 隐私合规页面状态
 * ============================================================
 *
 * @param auditResults 审计结果列表
 * @param isAuditing 是否正在审计
 * @param passedItems 已通过项数量
 * @param failedItems 失败项数量
 */
data class PrivacyState(
    val auditResults: List<PrivacyAuditItem> = defaultPrivacyAuditItems(),
    val isAuditing: Boolean = false,
    val passedItems: Int = 0,
    val failedItems: Int = 0
)

/**
 * ============================================================
 * PrivacyAuditItem — 隐私审计条目
 * ============================================================
 *
 * @param title 审计项标题
 * @param description 审计项描述
 * @param status 通过/失败/警告
 * @param recommendation 建议
 */
data class PrivacyAuditItem(
    val title: String,
    val description: String,
    val status: PrivacyAuditStatus,
    val recommendation: String
)

/**
 * ============================================================
 * PrivacyAuditStatus — 隐私审计状态
 * ============================================================
 */
enum class PrivacyAuditStatus { PASS, FAIL, WARNING }

/**
 * ============================================================
 * CrossDeviceState — 跨设备协同页面状态
 * ============================================================
 *
 * @param isConnected 是否已连接手机
 * @param syncStatus 同步状态
 * @param lastSyncTime 上次同步时间
 */
data class CrossDeviceState(
    val isConnected: Boolean = false,
    val syncStatus: SyncStatus = SyncStatus.DISCONNECTED,
    val lastSyncTime: String = "—"
)

/**
 * ============================================================
 * SyncStatus — 同步状态
 * ============================================================
 */
enum class SyncStatus { DISCONNECTED, CONNECTING, SYNCING, SYNCED }

// ================================================================
// XRToolkitIntent — 用户意图
// ================================================================

/**
 * ============================================================
 * XRToolkitIntent — 用户意图（User Intent）
 * ============================================================
 * Every user action corresponds to an Intent.
 * ViewModel receives Intent, processes business logic, then updates State.
 *
 * @see XRToolkitViewModel.sendIntent handles all Intents
 */
sealed interface XRToolkitIntent {

    /** 导航到指定页面
     * @param page 目标页面
     */
    data class NavigateTo(val page: XRToolkitPage) : XRToolkitIntent

    /** 完成学习阶段
     * @param stageIndex 阶段索引
     */
    data class CompleteStage(val stageIndex: Int) : XRToolkitIntent

    /** 开始 SDK 下载
     * @param url SDK 下载 URL
     */
    data class DownloadSDK(val url: String) : XRToolkitIntent

    /** 模拟配置步骤完成
     * @param stepIndex 步骤索引
     */
    data class CompleteConfigStep(val stepIndex: Int) : XRToolkitIntent

    /** 构建 HelloXR 示例
     * @param projectPath 项目路径
     */
    data class BuildHelloXR(val projectPath: String) : XRToolkitIntent

    /** 发送 Gemini Prompt
     * @param prompt Prompt 内容
     */
    data class SendGeminiPrompt(val prompt: String) : XRToolkitIntent

    /** 更新 Gemini Prompt 输入
     * @param prompt Prompt 内容
     */
    data class UpdatePrompt(val prompt: String) : XRToolkitIntent

    /** 切换语音输入模式
     * @param enabled 是否启用
     */
    data class SetVoiceInput(val enabled: Boolean) : XRToolkitIntent

    /** 切换 UI 规范 Tab
     * @param tab 目标 Tab
     */
    data class SelectUISpecTab(val tab: UISpecTab) : XRToolkitIntent

    /** 切换眼镜视野模拟
     * @param active 是否激活
     */
    data class SetGlassesSimulation(val active: Boolean) : XRToolkitIntent

    /** 复制代码示例
     * @param code 代码内容
     */
    data class CopyCodeSample(val code: String) : XRToolkitIntent

    /** 切换翻译动画
     * @param show 是否显示
     */
    data class SetTranslateAnimation(val show: Boolean) : XRToolkitIntent

    /** 切换 ARCore vs XR 场景
     * @param scenario 场景标识
     */
    data class SelectARCoreVsXRScenario(val scenario: String) : XRToolkitIntent

    /** 切换摄像头
     * @param enabled 是否启用
     */
    data class SetCamera(val enabled: Boolean) : XRToolkitIntent

    /** 切换麦克风
     * @param enabled 是否启用
     */
    data class SetMicrophone(val enabled: Boolean) : XRToolkitIntent

    /** 连接跨设备
     * @param enabled 是否连接
     */
    data class SetCrossDeviceConnection(val enabled: Boolean) : XRToolkitIntent

    /** 运行隐私审计
     */
    data object RunPrivacyAudit : XRToolkitIntent

    /** 关闭 Snackbar */
    data object DismissSnackbar : XRToolkitIntent

    /** 返回首页 */
    data object NavigateBack : XRToolkitIntent
}

// ================================================================
// XRToolkitEffect — 一次性副作用
// ================================================================

/**
 * ============================================================
 * XRToolkitEffect — 一次性副作用（Effect）
 * ============================================================
 * One-time events, immutable, can only be consumed once.
 * UI layer listens via LaunchedEffect + flow.collect{}.
 *
 * @see XRToolkitViewModel _effect.send() sends Effects
 */
sealed interface XRToolkitEffect {

    /** 显示 Toast 消息
     * @param message 消息文本
     */
    data class ShowToast(val message: String) : XRToolkitEffect

    /** SDK 下载完成
     */
    data object SdkDownloadComplete : XRToolkitEffect

    /** 复制到剪贴板
     * @param code 要复制的代码
     */
    data class CopyToClipboard(val code: String) : XRToolkitEffect

    /** 显示眼镜视野模拟
     * @param type 模拟类型
     */
    data class ShowSimulation(val type: XRSimulationType) : XRToolkitEffect

    /** HelloXR 构建成功
     */
    data object HelloXRBuildSuccess : XRToolkitEffect

    /** 构建失败
     * @param error 错误信息
     */
    data class BuildError(val error: String) : XRToolkitEffect
}

/**
 * ============================================================
 * XRSimulationType — XR 模拟类型
 * ============================================================
 */
enum class XRSimulationType { FONT_SIZE, COLOR_CONTRAST, LAYOUT_SAFETY }

// ================================================================
// 默认数据 / Default Data
// ================================================================

/**
 * ============================================================
 * defaultConfigSteps — 默认模拟器配置步骤
 * ============================================================
 */
fun defaultConfigSteps() = listOf(
    ConfigStep(
        title = "1. 申请 Early Access",
        description = "访问 Google 开发者门户，申请 Android XR SDK Early Access 资格",
        command = null
    ),
    ConfigStep(
        title = "2. 安装 Android XR SDK",
        description = "通过 Android Studio SDK Manager 安装 Android XR SDK（最低 API 34）",
        command = "sdk install android-xr 2026.0.1"
    ),
    ConfigStep(
        title = "3. 创建 XR 模拟器",
        description = "在 AVD Manager 中创建 XR 模拟器（选择 XR Device Profile）",
        command = "avd create --name xr-emulator --profile xr-standard"
    ),
    ConfigStep(
        title = "4. 启动模拟器",
        description = "启动 XR 模拟器并验证连接",
        command = "emulator -avd xr-emulator"
    ),
    ConfigStep(
        title = "5. 运行 HelloXR",
        description = "下载并运行 HelloXR 示例项目，验证环境配置",
        command = null
    )
)

/**
 * ============================================================
 * defaultPrivacyAuditItems — 默认隐私审计条目
 * ============================================================
 */
fun defaultPrivacyAuditItems() = listOf(
    PrivacyAuditItem(
        title = "摄像头数据本地处理",
        description = "摄像头捕获的图像数据必须在设备本地处理，不得上传至第三方服务器",
        status = PrivacyAuditStatus.PASS,
        recommendation = "使用 MLKit 的设备端模型处理视觉输入"
    ),
    PrivacyAuditItem(
        title = "Gemini API 调用权限",
        description = "调用 Gemini API 前必须明确告知用户并获取同意",
        status = PrivacyAuditStatus.PASS,
        recommendation = "在首次调用前显示隐私说明对话框"
    ),
    PrivacyAuditItem(
        title = "音频数据处理",
        description = "语音输入的音频数据必须明确告知处理目的",
        status = PrivacyAuditStatus.WARNING,
        recommendation = "添加音频处理隐私声明，说明是否临时存储"
    ),
    PrivacyAuditItem(
        title = "跨设备数据传输加密",
        description = "手机与眼镜之间的数据传输必须使用 TLS 加密",
        status = PrivacyAuditStatus.PASS,
        recommendation = "使用 Android 跨设备 API 的默认加密传输"
    ),
    PrivacyAuditItem(
        title = "翻译结果缓存",
        description = "翻译结果临时缓存时间不得超过 24 小时",
        status = PrivacyAuditStatus.WARNING,
        recommendation = "在 DataStore 中设置 24 小时自动过期策略"
    ),
    PrivacyAuditItem(
        title = "隐私沙盒合规",
        description = "确保应用符合 Android Privacy Sandbox 规范",
        status = PrivacyAuditStatus.FAIL,
        recommendation = "升级到 Android XR SDK 最新版本并重新审核隐私沙盒 API 调用"
    )
)

// ================================================================
// 工具入口数据 / Tool Entry Data
// ================================================================

/**
 * ============================================================
 * XR_TOOL_CARDS — XR 工具包工具入口列表
 * ============================================================
 * 首页工具入口展示的 10 个工具。
 */
val XR_TOOL_CARDS = listOf(
    ToolCardXR(
        id = "getting_started",
        name = "Android XR SDK 入门",
        description = "SDK 安装、模拟器配置、HelloXR 第一个 XR App",
        page = XRToolkitPage.GETTING_STARTED,
        icon = "🚀",
        priority = "P0"
    ),
    ToolCardXR(
        id = "gemini_integration",
        name = "Gemini API 集成指南",
        description = "在 XR 眼镜中调用 Gemini 多模态能力",
        page = XRToolkitPage.GEMINI,
        icon = "✨",
        priority = "P0"
    ),
    ToolCardXR(
        id = "ui_design",
        name = "UI/UX 设计规范",
        description = "眼镜视野 UI 设计原则、字体/颜色/布局安全区",
        page = XRToolkitPage.UI_DESIGN,
        icon = "🎨",
        priority = "P1"
    ),
    ToolCardXR(
        id = "live_translate",
        name = "实时翻译 API 集成",
        description = "MLKit Translate + 眼镜 Overlay 显示翻译结果",
        page = XRToolkitPage.LIVE_TRANSLATE,
        icon = "🌐",
        priority = "P1"
    ),
    ToolCardXR(
        id = "arcore_vs_xr",
        name = "ARCore vs Android XR",
        description = "两个平台的技术对比与选型决策树",
        page = XRToolkitPage.ARCORE_VS_XR,
        icon = "🔍",
        priority = "P1"
    ),
    ToolCardXR(
        id = "video_call",
        name = "视频通话开发指南",
        description = "摄像头采集、WebRTC 集成、隐私合规",
        page = XRToolkitPage.VIDEO_CALL,
        icon = "📹",
        priority = "P1"
    ),
    ToolCardXR(
        id = "test_debug",
        name = "测试与调试工具",
        description = "XR 模拟器调试、真机调试、常见问题排查",
        page = XRToolkitPage.TEST_DEBUG,
        icon = "🔧",
        priority = "P2"
    ),
    ToolCardXR(
        id = "privacy",
        name = "隐私合规检测",
        description = "摄像头数据处理、隐私沙盒合规、审计报告",
        page = XRToolkitPage.PRIVACY,
        icon = "🔒",
        priority = "P0"
    ),
    ToolCardXR(
        id = "cross_device",
        name = "跨设备协同架构",
        description = "眼镜 × 手机状态同步、CrossDeviceService 使用",
        page = XRToolkitPage.CROSS_DEVICE,
        icon = "📱",
        priority = "P1"
    )
)

/**
 * ============================================================
 * ToolCardXR — XR 工具卡片数据模型
 * ============================================================
 *
 * @param id 工具 ID
 * @param name 工具名称
 * @param description 工具描述
 * @param page 关联页面
 * @param icon Emoji 图标
 * @param priority 优先级
 */
data class ToolCardXR(
    val id: String,
    val name: String,
    val description: String,
    val page: XRToolkitPage,
    val icon: String,
    val priority: String
)

// ================================================================
// 代码示例数据 / Code Sample Data
// ================================================================

/**
 * ============================================================
 * HELLO_XR_CODE — HelloXR 示例代码
 * ============================================================
 */
const val HELLO_XR_CODE = """// MainActivity.kt — Android XR HelloXR 示例
package com.example.helloxr

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.xr.compose.text.XrText
import androidx.xr.runtime.XREnvironment

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Check XR environment support
        if (!XREnvironment.isXRAvailable(this)) {
            finish()
            return
        }

        setContent {
            XrSurface {
                // Render text in glasses field of view
                XrText(
                    text = "Hello, Android XR!",
                    style = TextStyle(
                        fontSize = 24.sp,
                        color = Color.White
                    ),
                    modifier = Modifier.xrAlign(XRAlign.CENTER)
                )
            }
        }
    }
}"""

/**
 * ============================================================
 * GEMINI_XR_INTEGRATION_CODE — Gemini XR 集成示例代码
 * ============================================================
 */
const val GEMINI_XR_INTEGRATION_CODE = """// GeminiIntegration.kt — Gemini × Android XR 集成
package com.example.xrapp

import androidx.xr.samples.gemini.GeminiClient
import androidx.xr.runtime.GlassesEvent
import com.google.ai.client.generativeai.GenerativeModel

class GeminiXRIntegration(
    private val generativeModel: GenerativeModel
) {
    /**
     * Process voice input and display Gemini's response in glasses.
     * 处理语音输入并在眼镜中显示 Gemini 响应。
     */
    suspend fun processVoiceInput(
        audioData: ByteArray,
        glassesDisplay: GlassesDisplay
    ) {
        // 1. Convert speech to text using SpeechRecognizer
        val inputText = speechToText(audioData)

        // 2. Send to Gemini and get response
        val response = generativeModel.generateContent(inputText)

        // 3. Render response in glasses field of view
        glassesDisplay.showText(
            text = response.text,
            style = TextStyle(fontSize = 20.sp, color = Color.White)
        )
    }

    /**
     * Process visual context from camera feed.
     * 处理来自摄像头馈送的视觉上下文。
     */
    suspend fun processVisualContext(
        cameraFrame: ByteBuffer,
        glassesDisplay: GlassesDisplay
    ) {
        // 1. Preprocess image for Gemini vision input
        val imageInput = Image preprocessing(cameraFrame)

        // 2. Send multimodal prompt to Gemini
        val response = generativeModel.generateContent(
            Content("What do I see?", imageInput)
        )

        // 3. Display Gemini's understanding in glasses
        glassesDisplay.showText(response.text)
    }
}"""

/**
 * ============================================================
 * MLKIT_TRANSLATION_CODE — MLKit 翻译集成代码
 * ================================================================
 */
const val MLKIT_TRANSLATION_CODE = """// TranslationOverlay.kt — 实时翻译 Overlay
package com.example.xrapp.translation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.TranslatorOptions

@Composable
fun TranslationOverlay(
    translatedText: String,
    isVisible: Boolean,
    modifier: Modifier = Modifier
) {
    // Animated visibility — text appears/disappears smoothly
    // 动画可见性 — 文字平滑浮现/消失
    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .background(Color.Black.copy(alpha = 0.7f))
                .padding(horizontal = 24.dp, vertical = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = translatedText,
                color = Color.White,
                fontSize = 22.sp,
                textAlign = TextAlign.Center,
                lineHeight = 32.sp
            )
        }
    }
}

/**
 * MLKit Translation setup — MLKit 翻译设置
 */
fun setupTranslator(
    sourceLanguage: String,
    targetLanguage: String
) {
    val options = TranslatorOptions.Builder()
        .setSourceLanguage(mapToMLKitLanguage(sourceLanguage))
        .setTargetLanguage(mapToMLKitLanguage(targetLanguage))
        .build()

    val translator = Translation.getClient(options)

    // Download model on device — 在设备上下载模型
    val conditions = DownloadConditions.Builder()
        .requireWifi()
        .build()

    translator.downloadModelIfNeeded(conditions)
}

private fun mapToMLKitLanguage(lang: String) = when (lang) {
    "English" -> TranslateLanguage.ENGLISH
    "中文" -> TranslateLanguage.CHINESE
    "日本語" -> TranslateLanguage.JAPANESE
    "한국어" -> TranslateLanguage.KOREAN
    else -> TranslateLanguage.ENGLISH
}"""
