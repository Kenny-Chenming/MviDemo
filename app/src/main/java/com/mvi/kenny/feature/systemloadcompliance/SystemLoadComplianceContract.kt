package com.mvi.kenny.feature.systemloadcompliance

import android.net.Uri
import androidx.compose.ui.graphics.Color

/**
 * ============================================================
 * SystemLoadComplianceContract — Android 17 System.load() 合规检测工具 MVI 契约
 * ============================================================
 * MVI Architecture Pattern / MVI 架构模式
 *
 * - Model (State): Immutable data class, single source of truth for UI
 * - View: Composable functions that consume State and render UI
 * - Intent: User intentions, ViewModel processes and updates State
 * - Effect: One-time side effects (navigation, toast, etc.)
 *
 * Android 17 Read-Only Native Library 合规性：
 * System.load() 和 System.loadLibrary() 调用的 .so 文件在 APK 签名后被标记为 read-only，
 * Android 17 强制执行此约束，违规将导致 UnsatisfiedLinkError。
 *
 * @see SystemLoadComplianceViewModel State management
 * @see SystemLoadComplianceScreen Main screen
 */

// =============================================================
// ScanStatus — 扫描状态
// =============================================================
/**
 * Scan status / 扫描状态
 *
 * @param label Display label / 显示标签
 */
enum class ScanStatus(val label: String) {
    IDLE("Idle / 空闲"),
    SCANNING("Scanning / 扫描中"),
    ANALYZING("Analyzing / 分析中"),
    COMPLETED("Completed / 完成"),
    ERROR("Error / 错误")
}

// =============================================================
// ComplianceStatus — 合规状态
// =============================================================
/**
 * Compliance status / 合规状态
 *
 * @param label Display label / 显示标签
 * @param color Status color / 状态颜色
 */
enum class ComplianceStatus(val label: String, val color: Color) {
    COMPLIANT("Compliant / 合规", Color(0xFF4CAF50)),         // Green
    NON_COMPLIANT("Non-Compliant / 不合规", Color(0xFFF44336)), // Red
    WARNING("Warning / 警告", Color(0xFFFF9800)),              // Orange
    UNKNOWN("Unknown / 未知", Color(0xFF9E9E9E))               // Gray
}

// =============================================================
// LoadCallType — Native Library 加载调用类型
// =============================================================
/**
 * Native library load call type / Native 库加载调用类型
 *
 * @param label Display label / 显示标签
 * @param riskLevel Risk level (0=none, 1=low, 2=medium, 3=high) / 风险等级
 */
enum class LoadCallType(val label: String, val riskLevel: Int) {
    LOAD_LIBRARY("System.loadLibrary()", 1),         // Low risk: static lib name
    LOAD_ABSOLUTE("System.load(absolute path)", 2), // Medium risk: absolute path
    LOAD_DYNAMIC("System.load(dynamic concat)", 3)    // High risk: dynamic path concatenation
}

// =============================================================
// Framework — 插件化框架
// =============================================================
/**
 * Plugin framework / 插件化框架
 *
 * @param label Display label / 显示标签
 * @param migrationComplexity Migration complexity 1-5 / 迁移复杂度
 */
enum class Framework(val label: String, val migrationComplexity: Int) {
    VIRTUAL_APK("VirtualAPK", 2),
    RE_PLUGIN("RePlugin", 3),
    DYNAMIC_LOAD("DynamicLoad", 3),
    APKPLUG("apkplug", 4),
    NONE("None / 无", 0)
}

// =============================================================
// MarkingMethod — read-only 标记方式
// =============================================================
/**
 * read-only marking method / 只读标记方式
 *
 * @param label Display label / 显示标签
 * @param description Method description / 方法说明
 * @param isRecommended Whether this method is recommended / 是否推荐
 */
enum class MarkingMethod(
    val label: String,
    val description: String,
    val isRecommended: Boolean
) {
    CHMOD_0444(
        "chmod 0444",
        "Set file permission to read-only. ⚠️ Will be reset after APK signing.",
        false
    ),
    FILE_CHANNEL_LOCK(
        "FileChannel.lock()",
        "Use Java NIO FileChannel to acquire shared file lock at runtime. ✅ Survives signing.",
        true
    ),
    GRADLE_TASK(
        "Gradle Task (signingBlock)",
        "Configure android.signingBlock to preserve file attributes after APK signing.",
        true
    ),
    ANDROID_API(
        "Android API (File.setReadOnly())",
        "Use File.setReadOnly() API. Works on Android 6.0+ (API 23).",
        false
    )
}

// =============================================================
// GameEngine — 游戏引擎
// =============================================================
/**
 * Game engine / 游戏引擎
 *
 * @param label Display label / 显示标签
 * @param defaultVersion Recommended default version / 默认推荐版本
 */
enum class GameEngine(val label: String, val defaultVersion: String) {
    UNITY("Unity", "2022.3 LTS"),
    UNREAL("Unreal Engine", "5.3"),
    COCOS("Cocos Creator", "3.8")
}

// =============================================================
// CIPlatform — CI 平台
// =============================================================
/**
 * CI platform / CI 平台
 *
 * @param label Display label / 显示标签
 */
enum class CIPlatform(val label: String, val description: String) {
    GITHUB_ACTIONS("GitHub Actions", "GitHub Actions CI"),
    GITLAB_CI("GitLab CI", "GitLab CI/CD")
}

// =============================================================
// LoadCallLocation — 不合规调用位置
// =============================================================
/**
 * Non-compliant load call location / 不合规调用位置
 *
 * Represents a System.load() / System.loadLibrary() call that violates Android 17 read-only constraint.
 *
 * @param filePath Source file path / 源文件路径
 * @param lineNumber Line number in source file / 源文件行号
 * @param methodName Method containing the call / 包含调用的方法名
 * @param className Class containing the method / 包含方法的类名
 * @param callType Load call type / 加载调用类型
 * @param libName Library name (extracted) / 库名称（提取）
 * @param rawCode Raw code snippet / 原始代码片段
 * @param isDynamicConcat Whether dynamic path concatenation is detected / 是否检测到动态路径拼接
 * @param fixSuggestion Suggested fix / 修复建议
 */
data class LoadCallLocation(
    val filePath: String,
    val lineNumber: Int,
    val methodName: String,
    val className: String,
    val callType: LoadCallType,
    val libName: String,
    val rawCode: String,
    val isDynamicConcat: Boolean = false,
    val fixSuggestion: String = ""
)

// =============================================================
// SoFileInfo — .so 文件信息
// =============================================================
/**
 * .so file information / .so 文件信息
 *
 * @param name So file name (e.g., libnative-lib.so) / .so 文件名
 * @param source Dependency that introduced this .so / 引入该 .so 的依赖来源
 * @param architecture CPU architecture / CPU 架构
 * @param size Size in bytes / 文件大小（字节）
 * @param isReadOnlyMarked Whether read-only marker has been applied / 是否已标记为只读
 * @param markingMethod How read-only was marked / 标记方式
 * @param complianceStatus Current compliance status / 当前合规状态
 */
data class SoFileInfo(
    val name: String,
    val source: String,
    val architecture: String,
    val size: Long,
    val isReadOnlyMarked: Boolean = false,
    val markingMethod: MarkingMethod? = null,
    val complianceStatus: ComplianceStatus = ComplianceStatus.UNKNOWN
)

// =============================================================
// MigrationGuide — 插件化迁移指南
// =============================================================
/**
 * Plugin migration guide / 插件化迁移指南
 *
 * @param framework Selected framework / 选中的框架
 * @param steps Migration steps / 迁移步骤
 * @param keyChanges Key changes summary / 关键变更摘要
 * @param warnings Warnings and cautions / 警告和注意事项
 */
data class MigrationGuide(
    val framework: Framework,
    val steps: List<MigrationStep>,
    val keyChanges: List<String>,
    val warnings: List<String>
)

/**
 * Migration step / 迁移步骤
 *
 * @param stepNumber Step number / 步骤编号
 * @param title Step title / 步骤标题
 * @param description Step description / 步骤描述
 * @param codeSnippet Optional code snippet / 可选代码片段
 * @param isCompleted Whether this step is completed / 是否已完成
 */
data class MigrationStep(
    val stepNumber: Int,
    val title: String,
    val description: String,
    val codeSnippet: String = "",
    val isCompleted: Boolean = false
)

// =============================================================
// RootCauseResult — UnsatisfiedLinkError 根因分析结果
// =============================================================
/**
 * UnsatisfiedLinkError root cause analysis result / UnsatisfiedLinkError 根因分析结果
 *
 * @param isRootCauseFound Whether root cause was identified / 是否找到根因
 * @param rootCause Root cause description / 根因描述
 * @param affectedCallLocations Affected call locations / 受影响的调用位置
 * @param fixRecommendation Fix recommendation / 修复建议
 * @param relatedErrorMessages Related error messages from log / 日志中的相关错误信息
 */
data class RootCauseResult(
    val isRootCauseFound: Boolean,
    val rootCause: String,
    val affectedCallLocations: List<LoadCallLocation>,
    val fixRecommendation: String,
    val relatedErrorMessages: List<String> = emptyList()
)

// =============================================================
// EngineComplianceResult — 游戏引擎合规检测结果
// =============================================================
/**
 * Game engine compliance detection result / 游戏引擎合规检测结果
 *
 * @param engine Game engine / 游戏引擎
 * @param version Engine version / 引擎版本
 * @param complianceStatus Overall compliance status / 整体合规状态
 * @param detectedIssues List of detected issues / 检测到的问题列表
 * @param fixGuide Fix guide / 修复指南
 * @param isUpgradeRequired Whether version upgrade is required / 是否需要升级版本
 * @param recommendedVersion Recommended version / 推荐版本
 */
data class EngineComplianceResult(
    val engine: GameEngine,
    val version: String,
    val complianceStatus: ComplianceStatus,
    val detectedIssues: List<String>,
    val fixGuide: String,
    val isUpgradeRequired: Boolean = false,
    val recommendedVersion: String = ""
)

// =============================================================
// SystemLoadComplianceState — 页面状态
// =============================================================
/**
 * System.load() Compliance Tool State / System.load() 合规工具页面状态
 *
 * Single source of truth for the entire System.load() compliance UI.
 *
 * @param complianceScore Overall compliance score (0-100) / 整体合规评分
 * @param scanStatus Current scan status / 当前扫描状态
 * @param scannedFilesCount Number of scanned files / 已扫描文件数
 * @param nonCompliantCalls List of non-compliant load calls / 不合规调用列表
 * @param soFiles List of .so files found / 发现的 .so 文件列表
 * @param markingCode Generated read-only marking code / 生成的只读标记代码
 * @param migrationGuide Plugin migration guide / 插件化迁移指南
 * @param rootCauseResult UnsatisfiedLinkError root cause analysis / 根因分析结果
 * @param engineResult Game engine compliance result / 游戏引擎合规结果
 * @param ciConfig CI configuration template / CI 配置模板
 * @param selectedFramework Selected plugin framework / 选中的插件化框架
 * @param selectedTab Currently selected tab index (0-6) / 当前选中的 Tab 索引
 * @param markingMethod Selected marking method / 选中的标记方式
 * @param selectedEngine Selected game engine / 选中的游戏引擎
 * @param engineVersion Selected engine version / 选中的引擎版本
 * @param crashLogInput Crash log input text / 崩溃日志输入文本
 * @param ciPlatform Selected CI platform / 选中的 CI 平台
 * @param scanSourceDir Scan source directory path / 扫描源目录路径
 * @param scanProgress Scan progress (0.0-1.0) / 扫描进度
 * @param isLoading Whether a background operation is in progress / 是否有后台操作进行中
 * @param errorMessage Error message if any / 错误信息
 */
data class SystemLoadComplianceState(
    val complianceScore: Int = 100,
    val scanStatus: ScanStatus = ScanStatus.IDLE,
    val scannedFilesCount: Int = 0,
    val nonCompliantCalls: List<LoadCallLocation> = emptyList(),
    val soFiles: List<SoFileInfo> = emptyList(),
    val markingCode: String = "",
    val migrationGuide: MigrationGuide? = null,
    val rootCauseResult: RootCauseResult? = null,
    val engineResult: EngineComplianceResult? = null,
    val ciConfig: String = "",
    val selectedFramework: Framework? = null,
    val selectedTab: Int = 0,
    val markingMethod: MarkingMethod = MarkingMethod.FILE_CHANNEL_LOCK,
    val selectedEngine: GameEngine = GameEngine.UNITY,
    val engineVersion: String = "2022.3 LTS",
    val crashLogInput: String = "",
    val ciPlatform: CIPlatform = CIPlatform.GITHUB_ACTIONS,
    val scanSourceDir: String = "",
    val scanProgress: Float = 0f,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
) {
    companion object {
        /** Initial state / 初始状态 */
        val Initial = SystemLoadComplianceState()
    }

    /**
     * Compliant calls count / 合规调用数量
     */
    val compliantCallsCount: Int
        get() = if (nonCompliantCalls.isEmpty() && scanStatus == ScanStatus.COMPLETED) scannedFilesCount else 0

    /**
     * Non-compliant calls count / 不合规调用数量
     */
    val nonCompliantCallsCount: Int
        get() = nonCompliantCalls.size

    /**
     * Whether scan has results / 扫描是否有结果
     */
    val hasResults: Boolean
        get() = scanStatus == ScanStatus.COMPLETED && nonCompliantCalls.isNotEmpty()

    /**
     * Marked .so count / 已标记只读的 .so 数量
     */
    val markedSoCount: Int
        get() = soFiles.count { it.isReadOnlyMarked }
}

// =============================================================
// SystemLoadComplianceIntent — 用户意图
// =============================================================
/**
 * System.load() Compliance Tool User Intents / 合规工具用户意图
 *
 * Every user action corresponds to an Intent.
 */
sealed interface SystemLoadComplianceIntent {

    /** Select tab / 选择 Tab
     * @param tabIndex Tab index (0-6) / Tab 索引
     */
    data class SelectTab(val tabIndex: Int) : SystemLoadComplianceIntent

    /** Start scan / 开始扫描
     * @param sourceDir Source directory to scan / 要扫描的源目录
     */
    data class StartScan(val sourceDir: String) : SystemLoadComplianceIntent

    /** Cancel scan / 取消扫描 */
    data object CancelScan : SystemLoadComplianceIntent

    /** Apply one-click fix / 应用一键修复
     * @param callLocation Call location to fix / 要修复的调用位置
     */
    data class ApplyFix(val callLocation: LoadCallLocation) : SystemLoadComplianceIntent

    /** Apply fix to all non-compliant calls / 修复所有不合规调用 */
    data object ApplyFixAll : SystemLoadComplianceIntent

    /** Select marking method / 选择标记方式
     * @param method Marking method / 标记方式
     */
    data class SelectMarkingMethod(val method: MarkingMethod) : SystemLoadComplianceIntent

    /** Select .so file for marking / 选择要标记的 .so 文件
     * @param soFile .so file info / .so 文件信息
     */
    data class SelectSoForMarking(val soFile: SoFileInfo) : SystemLoadComplianceIntent

    /** Apply marking to selected .so / 对选中的 .so 应用标记
     * @param soFile .so file info / .so 文件信息
     */
    data class ApplyMarking(val soFile: SoFileInfo) : SystemLoadComplianceIntent

    /** Select plugin framework / 选择插件化框架
     * @param framework Framework / 框架
     */
    data class SelectFramework(val framework: Framework) : SystemLoadComplianceIntent

    /** Generate migration guide / 生成迁移指南
     * @param framework Framework / 框架
     */
    data class GenerateMigrationGuide(val framework: Framework) : SystemLoadComplianceIntent

    /** Input crash log / 输入崩溃日志
     * @param log Crash log text / 崩溃日志文本
     */
    data class InputCrashLog(val log: String) : SystemLoadComplianceIntent

    /** Analyze root cause / 分析根因 */
    data object AnalyzeRootCause : SystemLoadComplianceIntent

    /** Select game engine / 选择游戏引擎
     * @param engine Game engine / 游戏引擎
     */
    data class SelectEngine(val engine: GameEngine) : SystemLoadComplianceIntent

    /** Input engine version / 输入引擎版本
     * @param version Engine version / 引擎版本
     */
    data class InputEngineVersion(val version: String) : SystemLoadComplianceIntent

    /** Detect game engine compliance / 检测游戏引擎合规性 */
    data object DetectEngineCompliance : SystemLoadComplianceIntent

    /** Select CI platform / 选择 CI 平台
     * @param platform CI platform / CI 平台
     */
    data class SelectCIPlatform(val platform: CIPlatform) : SystemLoadComplianceIntent

    /** Generate CI configuration / 生成 CI 配置 */
    data object GenerateCIConfig : SystemLoadComplianceIntent

    /** Copy CI config to clipboard / 复制 CI 配置到剪贴板 */
    data object CopyCIConfig : SystemLoadComplianceIntent

    /** Reset state / 重置状态 */
    data object Reset : SystemLoadComplianceIntent

    /** Dismiss error / 关闭错误信息 */
    data object DismissError : SystemLoadComplianceIntent
}

// =============================================================
// SystemLoadComplianceEffect — 副作用
// =============================================================
/**
 * System.load() Compliance Tool Side Effects / 合规工具副作用
 *
 * One-time events, consumed only once by UI layer.
 */
sealed interface SystemLoadComplianceEffect {

    /** Show toast message / 显示 Toast 消息
     * @param message Toast message / Toast 消息
     */
    data class ShowToast(val message: String) : SystemLoadComplianceEffect

    /** Show error message / 显示错误消息
     * @param message Error message / 错误信息
     */
    data class ShowError(val message: String) : SystemLoadComplianceEffect

    /** Copy text to clipboard / 复制文本到剪贴板
     * @param text Text to copy / 要复制的文本
     * @param label Label for the copied content / 已复制内容的标签
     */
    data class CopyToClipboard(val text: String, val label: String) : SystemLoadComplianceEffect

    /** Open file location / 打开文件位置
     * @param filePath File path / 文件路径
     */
    data class OpenFileLocation(val filePath: String) : SystemLoadComplianceEffect

    /** Show scan completion notification / 显示扫描完成通知
     * @param passedCount Number of compliant calls / 合规调用数
     * @param failedCount Number of non-compliant calls / 不合规调用数
     */
    data class ShowScanComplete(val passedCount: Int, val failedCount: Int) : SystemLoadComplianceEffect
}
