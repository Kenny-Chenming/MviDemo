package com.mvi.kenny.feature.android17resizability

import androidx.compose.ui.graphics.Color

/**
 * ============================================================
 * Android17ResizabilityContract — Android 17 大屏自适应迁移工具包 MVI 契约
 * ============================================================
 * MVI (Model-View-Intent) Architecture Pattern.
 *
 * PRD-223: Android 17 (API 37) 正式移除大屏（sw>600dp）设备上的 orientation/resize opt-out。
 * 所有 App 将被强制进入 Desktop Windowing 或自由窗口模式。
 *
 * @see Android17ResizabilityViewModel State management
 * @see Android17ResizabilityScreen Main screen
 */

// =============================================================
// Colors — 视觉配色常量（深色 Terminal 风格）
// =============================================================
/** Accent color / 主强调色 */
val ResizabilityAccent = Color(0xFF7C4DFF)

/** Success / compliance color / 成功/合规状态色 */
val ResizabilitySuccess = Color(0xFF64FFDA)

/** Warning color / 警告色 */
val ResizabilityWarning = Color(0xFFFFD740)

/** Danger / P0 / 危险/P0 优先级色 */
val ResizabilityDanger = Color(0xFFFF5252)

/** Surface background / 背景色 */
val ResizabilitySurface = Color(0xFF121212)

/** Card background / 卡片背景色 */
val ResizabilityCardBg = Color(0xFF1E1E1E)

/** Code block background / 代码块背景色 */
val ResizabilityCodeBg = Color(0xFF0D1117)

// =============================================================
// Tab definitions — Tab 定义
// =============================================================
/**
 * Five main tabs of the toolkit / 工具包五个主 Tab
 *
 * @param title Tab title (English) / Tab 标题（英文）
 * @param titleZh Tab title (Chinese) / Tab 标题（中文）
 */
enum class ResizabilityTab(val title: String, val titleZh: String) {
    DETECTION("Detection", "检测扫描"),
    MANIFEST("Manifest", "Manifest迁移"),
    CI("CI Compliance", "CI合规"),
    BEHAVIOR("Behavior", "行为变更"),
    DECISION("Decision", "决策树")
}

// =============================================================
// Priority Badge — 优先级标签
// =============================================================
/**
 * Migration priority level / 迁移优先级等级
 *
 * @param label Display label (English) / 显示标签（英文）
 * @param labelZh Display label (Chinese) / 显示标签（中文）
 * @param color Badge background color / 徽章背景色
 */
enum class Priority(
    val label: String,
    val labelZh: String,
    val color: Color
) {
    /** P0: Must fix before Android 17 launch / Android 17 发布前必须修复 */
    P0("P0", "必须修复", ResizabilityDanger),

    /** P1: Should fix before Android 17 launch / Android 17 发布前应修复 */
    P1("P1", "建议修复", ResizabilityWarning),

    /** P2: Nice to fix / 可选修复 */
    P2("P2", "可选修复", ResizabilitySuccess)
}

// =============================================================
// Scanner — 扫描结果数据模型
// =============================================================
/**
 * Result of manifest opt-out detection scan / Manifest opt-out 检测扫描结果
 *
 * @param attribute Attribute name that is deprecated / 已废弃的属性名
 * @param attributeZh Attribute name (Chinese) / 属性名（中文）
 * @param element Element where attribute is used / 使用该属性的元素
 * @param filePath File path where attribute is found / 发现属性的文件路径
 * @param description Why this attribute is deprecated / 属性废弃原因
 * @param descriptionZh Why this attribute is deprecated (Chinese) / 废弃原因（中文）
 * @param fixSuggestion Suggested fix code / 建议修复代码
 * @param priority Migration priority / 迁移优先级
 */
data class OptOutResult(
    val attribute: String,
    val attributeZh: String,
    val element: String,
    val filePath: String,
    val description: String,
    val descriptionZh: String,
    val fixSuggestion: String,
    val priority: Priority = Priority.P0
)

/**
 * Result of Camera aspect ratio issue scan / Camera aspect ratio 问题扫描结果
 *
 * @param cameraApi Camera API used (Camera2/CameraX) / 使用的 Camera API
 * @param filePath File path where issue is found / 发现问题的文件路径
 * @param methodName Method name / 方法名
 * @param description Issue description / 问题描述
 * @param descriptionZh Issue description (Chinese) / 问题描述（中文）
 * @param fixSuggestion Suggested fix code / 建议修复代码
 * @param priority Migration priority / 迁移优先级
 */
data class CameraIssueResult(
    val cameraApi: String,
    val filePath: String,
    val methodName: String,
    val description: String,
    val descriptionZh: String,
    val fixSuggestion: String,
    val priority: Priority = Priority.P1
)

// =============================================================
// CI Config — CI 配置数据模型
// =============================================================
/**
 * CI compliance configuration / CI 合规配置
 *
 * @param gradlePluginEnabled Whether Gradle plugin is enabled / Gradle 插件是否启用
 * @param blockOnViolation Whether to block CI on violation / 是否在违规时阻塞 CI
 * @param reportFormat CI report format / CI 报告格式
 */
data class CiConfig(
    val gradlePluginEnabled: Boolean = false,
    val blockOnViolation: Boolean = true,
    val reportFormat: String = "json"
)

// =============================================================
// Migration Guide Item — 迁移指南条目
// =============================================================
/**
 * Migration guide item / 迁移指南条目
 *
 * @param id Unique ID / 唯一 ID
 * @param title Title (English) / 标题（英文）
 * @param titleZh Title (Chinese) / 标题（中文）
 * @param beforeCode Before fix code snippet / 修复前代码
 * @param afterCode After fix code snippet / 修复后代码
 * @param explanation Explanation / 说明
 * @param explanationZh Explanation (Chinese) / 说明（中文）
 * @param priority Priority badge / 优先级标签
 * @param isExpanded Whether this guide is expanded / 是否展开
 */
data class MigrationGuideItem(
    val id: String,
    val title: String,
    val titleZh: String,
    val beforeCode: String,
    val afterCode: String,
    val explanation: String,
    val explanationZh: String,
    val priority: Priority = Priority.P1,
    val isExpanded: Boolean = false
)

// =============================================================
// Decision Tree — 决策树数据模型
// =============================================================
/**
 * Decision tree node / 决策树节点
 *
 * @param id Node ID / 节点 ID
 * @param question Question text (English) / 问题文本（英文）
 * @param questionZh Question text (Chinese) / 问题文本（中文）
 * @param options List of child options / 子选项列表
 * @param result Result if this is a leaf node / 如果是叶子节点的结果
 * @param priority Recommended priority if leaf node / 叶子节点的建议优先级
 */
data class DecisionTreeNode(
    val id: String,
    val question: String,
    val questionZh: String,
    val options: List<DecisionOption> = emptyList(),
    val result: String? = null,
    val resultZh: String? = null,
    val priority: Priority? = null
)

/**
 * Decision option / 决策选项
 *
 * @param label Option label (English) / 选项标签（英文）
 * @param labelZh Option label (Chinese) / 选项标签（中文）
 * @param nextNodeId Next node ID if this option is selected / 选择此选项后的下一个节点 ID
 */
data class DecisionOption(
    val label: String,
    val labelZh: String,
    val nextNodeId: String
)

// =============================================================
// State — 页面状态
// =============================================================
/**
 * Android 17 Resizability Toolkit State / Android 17 大屏自适应迁移工具包状态
 *
 * @param selectedTab Currently selected tab index / 当前选中的 Tab 索引
 * @param scannerInput Scanner input path / 扫描器输入路径
 * @param scanProgress Scan progress 0.0~1.0 / 扫描进度
 * @param isScanning Whether scan is in progress / 是否正在扫描
 * @param scanResults Opt-out scan results / Opt-out 扫描结果
 * @param cameraResults Camera issue scan results / Camera 问题扫描结果
 * @param migrationGuidesExpanded Which migration guide is expanded / 哪个迁移指南已展开
 * @param decisionTreeState Current decision tree navigation state / 当前决策树导航状态
 * @param ciConfig CI compliance configuration / CI 合规配置
 * @param snackbarMessage Snackbar message to display / 要显示的 Snackbar 消息
 */
data class Android17ResizabilityState(
    val selectedTab: ResizabilityTab = ResizabilityTab.DETECTION,
    val scannerInput: String = "",
    val scanProgress: Float = 0f,
    val isScanning: Boolean = false,
    val scanResults: List<OptOutResult> = emptyList(),
    val cameraResults: List<CameraIssueResult> = emptyList(),
    val migrationGuidesExpanded: Int? = null,
    val decisionTreeState: DecisionTreeNode? = null,
    val decisionPath: List<String> = emptyList(),
    val ciConfig: CiConfig = CiConfig(),
    val snackbarMessage: String? = null,
    val copiedCode: String? = null
) {
    companion object {
        /** Initial state / 初始状态 */
        val Initial = Android17ResizabilityState()
    }
}

// =============================================================
// Intent — 用户意图
// =============================================================
/**
 * Android 17 Resizability Toolkit User Intents / 用户意图
 *
 * Every user action corresponds to an Intent.
 * ViewModel receives Intent and executes business logic.
 *
 * @see Android17ResizabilityViewModel.sendIntent Process all intents
 */
sealed interface Android17ResizabilityIntent {
    /** Select tab / 选择 Tab
     * @param tab Tab to select / 要选择的 Tab
     */
    data class SelectTab(val tab: ResizabilityTab) : Android17ResizabilityIntent

    /** Update scanner input / 更新扫描器输入
     * @param path Input path / 输入路径
     */
    data class UpdateScannerInput(val path: String) : Android17ResizabilityIntent

    /** Start scan / 开始扫描 */
    data object StartScan : Android17ResizabilityIntent

    /** Cancel ongoing scan / 取消正在进行的扫描 */
    data object CancelScan : Android17ResizabilityIntent

    /** Expand migration guide / 展开迁移指南
     * @param index Guide index / 指南索引
     */
    data class ExpandMigrationGuide(val index: Int?) : Android17ResizabilityIntent

    /** Navigate decision tree / 导航决策树
     * @param nodeId Next node ID / 下一个节点 ID
     */
    data class NavigateDecisionTree(val nodeId: String) : Android17ResizabilityIntent

    /** Reset decision tree / 重置决策树 */
    data object ResetDecisionTree : Android17ResizabilityIntent

    /** Update CI config / 更新 CI 配置
     * @param config New CI config / 新的 CI 配置
     */
    data class UpdateCiConfig(val config: CiConfig) : Android17ResizabilityIntent

    /** Copy code to clipboard / 复制代码到剪贴板
     * @param code Code to copy / 要复制的代码
     */
    data class CopyCode(val code: String) : Android17ResizabilityIntent

    /** Dismiss snackbar / 关闭 Snackbar */
    data object DismissSnackbar : Android17ResizabilityIntent
}

// =============================================================
// Effect — 副作用
// =============================================================
/**
 * Android 17 Resizability Toolkit Side Effects / 副作用
 *
 * One-time events, immutable, consumed only once.
 * UI layer listens via LaunchedEffect + flow.collect{}.
 *
 * @see Android17ResizabilityViewModel Send via _effect.send()
 */
sealed interface Android17ResizabilityEffect {
    /** Show snackbar message / 显示 Snackbar 消息
     * @param message Message to display / 要显示的消息
     */
    data class ShowSnackbar(val message: String) : Android17ResizabilityEffect

    /** Copy code to clipboard / 复制代码到剪贴板
     * @param code Code to copy / 要复制的代码
     */
    data class CopyToClipboard(val code: String) : Android17ResizabilityEffect

    /** Scan complete / 扫描完成 */
    data object ScanComplete : Android17ResizabilityEffect
}
