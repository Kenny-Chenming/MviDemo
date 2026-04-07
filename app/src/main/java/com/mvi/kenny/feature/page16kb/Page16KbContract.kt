package com.mvi.kenny.feature.page16kb

import android.net.Uri
import androidx.compose.ui.graphics.Color

/**
 * ============================================================
 * Page16KbContract — 16KB Page Size 迁移工具 MVI 契约
 * ============================================================
 * MVI Architecture Pattern / MVI 架构模式
 *
 * - Model (State): Immutable data class, single source of truth for UI
 * - View: Composable functions that consume State and render UI
 * - Intent: User intentions, ViewModel processes and updates State
 * - Effect: One-time side effects (navigation, toast, etc.)
 *
 * @see Page16KbViewModel State management
 * @see Page16KbScreen Main screen
 */

// =============================================================
// Step — 引导步骤枚举
// =============================================================
/**
 * Wizard step / 引导步骤
 *
 * Represents the current step in the 16KB migration workflow.
 *
 * @param label Display label / 显示标签
 */
enum class Step(val label: String, val labelZh: String) {
    DETECT("Detect", "检测"),
    ANALYZE("Analyze", "分析"),
    FIX("Fix", "修复"),
    VERIFY("Verify", "验证")
}

// =============================================================
// FilterMode — .so 列表过滤模式
// =============================================================
/**
 * .so list filter mode / .so 列表过滤模式
 */
enum class FilterMode(val label: String) {
    ALL("All / 全部"),
    ISSUES_ONLY("Issues Only / 仅问题"),
    THIRD_PARTY_ONLY("3rd Party / 第三方 SDK")
}

// =============================================================
// SoAlignmentStatus — .so 对齐状态
// =============================================================
/**
 * .so 16KB alignment status / .so 16KB 对齐状态
 *
 * @param label Display label / 显示标签
 * @param color Status color / 状态颜色
 */
enum class SoAlignmentStatus(val label: String, val color: Color) {
    ALIGNED("Aligned / 已对齐", Color(0xFF388E3C)),     // Green — passed
    MISALIGNED("Misaligned / 未对齐", Color(0xFFD32F2F)), // Red — P0 critical
    UNKNOWN("Unknown / 未知", Color(0xFF757575))         // Gray
}

// =============================================================
// SoFileInfo — .so 文件信息
// =============================================================
/**
 * Native library (.so) file information / Native 库文件信息
 *
 * Represents a single .so file found in the APK.
 *
 * @param name So file name (e.g., libnative-lib.so) / .so 文件名
 * @param source Which dependency introduced this .so / 引入该 .so 的依赖来源
 * @param architecture CPU architecture (arm64-v8a, armeabi-v7a, etc.) / CPU 架构
 * @param size Size in bytes / 文件大小（字节）
 * @param alignmentStatus 16KB alignment status / 16KB 对齐状态
 * @param minNdkVersion Minimum NDK version required for 16KB support / 最低 NDK 版本要求
 * @param elfHeaderInfo ELF header info for detail page / ELF 头信息（用于详情页）
 * @param isFromThirdParty Whether this .so is from a third-party SDK / 是否来自第三方 SDK
 */
data class SoFileInfo(
    val name: String,
    val source: String,
    val architecture: String,
    val size: Long,
    val alignmentStatus: SoAlignmentStatus,
    val minNdkVersion: String,
    val elfHeaderInfo: String = "",
    val isFromThirdParty: Boolean = false
)

// =============================================================
// ApkInfo — APK 文件信息
// =============================================================
/**
 * APK file information / APK 文件信息
 *
 * @param packageName Package name (e.g., com.example.app) / 包名
 * @param versionName Version name (e.g., 1.0.0) / 版本名称
 * @param versionCode Version code / 版本号
 * @param minSdkVersion Minimum SDK version / 最低 SDK 版本
 * @param targetSdkVersion Target SDK version / 目标 SDK 版本
 * @param soCount Total number of .so files / .so 文件总数
 * @param fileSize APK file size in bytes / APK 文件大小（字节）
 */
data class ApkInfo(
    val packageName: String,
    val versionName: String,
    val versionCode: Long,
    val minSdkVersion: Int,
    val targetSdkVersion: Int,
    val soCount: Int,
    val fileSize: Long
)

// =============================================================
// FixRecommendation — 修复建议
// =============================================================
/**
 * Fix recommendation for a problem .so / 问题 .so 的修复建议
 *
 * @param soFileInfo The target .so file info / 目标 .so 文件信息
 * @param fixType Fix type / 修复类型
 * @param ndkVersion Required NDK version / 所需 NDK 版本
 * @param fixCommand Command to recompile / 重新编译命令
 * @param suppressionNote Note for suppression strategy / suppression 策略说明
 * @param isAutoFixable Whether this can be auto-fixed / 是否可自动修复
 */
data class FixRecommendation(
    val soFileInfo: SoFileInfo,
    val fixType: FixType,
    val ndkVersion: String,
    val fixCommand: String,
    val suppressionNote: String,
    val isAutoFixable: Boolean
)

/**
 * Fix type / 修复类型
 */
enum class FixType {
    RECOMPILE,
    SUPPRESS,
    UPGRADE_SDK
}

// =============================================================
// VerificationResult — 验证结果
// =============================================================
/**
 * Verification result / 验证结果
 *
 * @param isAllPassed Whether all .so files passed 16KB alignment / 是否所有 .so 均通过 16KB 对齐
 * @param passedCount Number of passed .so files / 通过的 .so 文件数
 * @param totalCount Total number of .so files / 总 .so 文件数
 * @param verificationMessage Additional message / 附加信息
 */
data class VerificationResult(
    val isAllPassed: Boolean,
    val passedCount: Int,
    val totalCount: Int,
    val verificationMessage: String
)

// =============================================================
// SdkCompatibility — SDK 兼容性信息
// =============================================================
/**
 * Third-party SDK 16KB compatibility information / 第三方 SDK 16KB 兼容性信息
 *
 * @param sdkName SDK name / SDK 名称
 * @param version SDK version / SDK 版本
 * @param isCompatible Whether this SDK supports 16KB / 是否支持 16KB
 * @param compatibleVersion The version that supports 16KB / 支持 16KB 的版本
 * @param notes Additional notes / 附加说明
 */
data class SdkCompatibility(
    val sdkName: String,
    val version: String,
    val isCompatible: Boolean,
    val compatibleVersion: String?,
    val notes: String
)

// =============================================================
// Page16KbState — 页面状态
// =============================================================
/**
 * 16KB Migration Tool State / 16KB 迁移工具页面状态
 *
 * Single source of truth for the entire 16KB migration UI.
 * All UI state is derived from this data class.
 *
 * @param currentStep Current wizard step / 当前引导步骤
 * @param uploadedApkInfo Uploaded APK information, null if none / 已上传 APK 信息
 * @param soList List of all .so files found in APK / APK 中所有 .so 文件列表
 * @param issueCount Number of .so files with alignment issues / 有对齐问题的 .so 文件数
 * @param totalCount Total number of .so files / .so 文件总数
 * @param filterMode Current filter mode / 当前过滤模式
 * @param fixRecommendations List of fix recommendations / 修复建议列表
 * @param verificationResult Verification result, null if not verified yet / 验证结果
 * @param isLoading Whether a background operation is in progress / 是否有后台操作进行中
 * @param isParsingApk Whether APK is being parsed / 是否正在解析 APK
 * @param parseProgress APK parsing progress 0.0~1.0 / APK 解析进度
 * @param sdkCompatibilityQuery Search query for SDK compatibility / SDK 兼容性查询
 * @param sdkCompatibilityResults SDK compatibility search results / SDK 兼容性搜索结果
 * @param selectedSoDetail Selected .so detail for detail page / 选中的 .so 详情
 * @param error Error message if any / 错误信息
 */
data class Page16KbState(
    val currentStep: Step = Step.DETECT,
    val uploadedApkInfo: ApkInfo? = null,
    val soList: List<SoFileInfo> = emptyList(),
    val issueCount: Int = 0,
    val totalCount: Int = 0,
    val filterMode: FilterMode = FilterMode.ALL,
    val fixRecommendations: List<FixRecommendation> = emptyList(),
    val verificationResult: VerificationResult? = null,
    val isLoading: Boolean = false,
    val isParsingApk: Boolean = false,
    val parseProgress: Float = 0f,
    val sdkCompatibilityQuery: String = "",
    val sdkCompatibilityResults: List<SdkCompatibility> = emptyList(),
    val selectedSoDetail: SoFileInfo? = null,
    val error: String? = null
) {
    companion object {
        /** Initial state / 初始状态 */
        val Initial = Page16KbState()
    }

    /**
     * Filtered .so list based on current filter mode / 根据当前过滤模式过滤后的 .so 列表
     */
    val filteredSoList: List<SoFileInfo>
        get() = when (filterMode) {
            FilterMode.ALL -> soList
            FilterMode.ISSUES_ONLY -> soList.filter { it.alignmentStatus == SoAlignmentStatus.MISALIGNED }
            FilterMode.THIRD_PARTY_ONLY -> soList.filter { it.isFromThirdParty }
        }

    /**
     * Progress ratio string (e.g., "3/10") / 进度比字符串
     */
    val progressRatio: String
        get() = "${totalCount - issueCount}/$totalCount"

    /**
     * Issue percentage / 问题比例
     */
    val issuePercentage: Float
        get() = if (totalCount == 0) 0f else issueCount.toFloat() / totalCount

    /**
     * Step index (0-based) / 步骤索引
     */
    val stepIndex: Int
        get() = Step.entries.indexOf(currentStep)
}

// =============================================================
// Page16KbIntent — 用户意图
// =============================================================
/**
 * 16KB Migration Tool User Intents / 16KB 迁移工具用户意图
 *
 * Every user action corresponds to an Intent.
 * ViewModel receives Intent and executes business logic.
 */
sealed interface Page16KbIntent {
    /** Upload APK file / 上传 APK 文件
     * @param uri APK file URI / APK 文件 URI
     */
    data class UploadApk(val uri: Uri) : Page16KbIntent

    /** Select .so to view detail / 选择查看 .so 详情
     * @param soFileInfo .so file info / .so 文件信息
     */
    data class SelectSoDetail(val soFileInfo: SoFileInfo) : Page16KbIntent

    /** Clear selected .so detail / 清除选中的 .so 详情 */
    data object ClearSoDetail : Page16KbIntent

    /** Filter .so list / 过滤 .so 列表
     * @param mode Filter mode / 过滤模式
     */
    data class FilterSoList(val mode: FilterMode) : Page16KbIntent

    /** Apply fix recommendation / 应用修复建议
     * @param recommendation Fix recommendation to apply / 要应用的修复建议
     */
    data class ApplyFix(val recommendation: FixRecommendation) : Page16KbIntent

    /** Run verification / 运行验证
     * @param apkUri APK file URI for verification / 用于验证的 APK 文件 URI
     */
    data class RunVerification(val apkUri: Uri) : Page16KbIntent

    /** Search SDK compatibility / 搜索 SDK 兼容性
     * @param query Search query / 搜索查询
     */
    data class SearchSdkCompatibility(val query: String) : Page16KbIntent

    /** Navigate to next step / 进入下一步
     */
    data object NextStep : Page16KbIntent

    /** Navigate to previous step / 返回上一步
     */
    data object PreviousStep : Page16KbIntent

    /** Export report / 导出报告
     * @param isJson Whether export as JSON (false = PDF) / 是否导出为 JSON
     */
    data class ExportReport(val isJson: Boolean) : Page16KbIntent

    /** Dismiss error / 关闭错误信息
     */
    data object DismissError : Page16KbIntent
}

// =============================================================
// Page16KbEffect — 副作用
// =============================================================
/**
 * 16KB Migration Tool Side Effects / 16KB 迁移工具副作用
 *
 * One-time events, consumed only once by UI layer.
 * UI layer listens via LaunchedEffect + flow.collect{}.
 */
sealed interface Page16KbEffect {
    /** Navigate to .so detail page / 导航到 .so 详情页
     * @param soFileInfo .so file info / .so 文件信息
     */
    data class NavigateToSoDetail(val soFileInfo: SoFileInfo) : Page16KbEffect

    /** Show export success message / 显示导出成功消息
     * @param filePath Exported file path / 导出文件路径
     */
    data class ShowExportSuccess(val filePath: String) : Page16KbEffect

    /** Show error message / 显示错误消息
     * @param message Error message / 错误信息
     */
    data class ShowError(val message: String) : Page16KbEffect

    /** Show toast message / 显示 Toast 消息
     * @param message Toast message / Toast 消息
     */
    data class ShowToast(val message: String) : Page16KbEffect

    /** Scroll to top of list / 滚动到列表顶部
     */
    data object ScrollToTop : Page16KbEffect

    /** Open file picker / 打开文件选择器
     */
    data object OpenFilePicker : Page16KbEffect
}
