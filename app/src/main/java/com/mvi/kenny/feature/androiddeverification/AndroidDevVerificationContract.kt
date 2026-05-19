package com.mvi.kenny.feature.androiddeverification

/**
 * ============================================================
 * AndroidDevVerificationContract — Android 开发者验证合规与 CI 集成工具包 MVI 契约
 * AndroidDevVerification MVI Contract — Android Developer Verification Compliance & CI Toolkit
 * ============================================================
 *
 * PRD-261 | Android 开发者验证合规与 CI 集成工具包
 * Ref: memory/agency/designs/PRD-261-Android-Developer-Verification-合规与CI集成工具包.md
 *
 * Android 开发者验证计划：2026 年 9 月在巴西/印尼/新加坡/泰国强制执行，2027 年全球覆盖。
 * 所有在 Google Play 以外分发 Android 应用的开发者必须完成验证。
 *
 * MVI Architecture:
 * - Model (State): Immutable page state, single source of truth
 * - View: Composable functions that consume State and render UI
 * - Intent: User intentions; ViewModel executes logic on receiving Intent
 * - Effect: One-time side effects (Toast, Navigation, Clipboard) delivered via Channel
 * —————————————————————————————————————————————————————
 *
 * 5 Tab 布局:
 * - Tab 0: 验证检测 — APK 验证状态检测 + 开发者验证合规 CI 插件
 * - Tab 1: 政策解读 — 各国强制执行时间线 + 认证设备定义 + 违规后果
 * - Tab 2: 分发指南 — 有限分发账号 + 高级流程用户体验分析
 * - Tab 3: 替代方案 — 未验证应用替代分发策略 + App Claim 流程
 * - Tab 4: 集成工具 — Android Studio 验证状态集成 + IDE 工作流
 */

// ============================================================
// Risk Level Enum / 风险等级枚举
// ============================================================

/**
 * Risk level enumeration for verification results
 * 验证结果风险等级枚举
 *
 * @property label Display label in Chinese/English
 * @property emoji Emoji representation for quick identification
 * @property colorHex Hex color code for UI display
 */
enum class RiskLevel(val label: String, val emoji: String, val colorHex: Long) {
    SAFE("安全 / Safe", "🟢", 0xFF4CAF50),
    PENDING("待确认 / Pending", "🟡", 0xFFFF9800),
    NON_COMPLIANT("未合规 / Non-compliant", "🔴", 0xFFF44336),
    UNKNOWN("未知 / Unknown", "⚪", 0xFF9E9E9E)
}

// ============================================================
// Data Models / 数据模型
// ============================================================

/**
 * Verification result data model
 * 验证结果数据模型
 *
 * @param packageName App package name / 应用包名
 * @param developerVerified Whether developer is verified / 开发者是否已验证
 * @param verificationDate Date of verification (ISO 8601) / 验证日期
 * @param distributionType Distribution type: Google Play / Limited / Direct / Unknown / 分发类型
 * @param complianceStatus Compliance status description / 合规状态描述
 */
data class VerificationResult(
    val packageName: String,
    val developerVerified: Boolean,
    val verificationDate: String?,
    val distributionType: String,
    val complianceStatus: String,
)

/**
 * Country enforcement timeline data model
 * 国家强制执行时间线数据模型
 *
 * @param country Country name (Chinese + English) / 国家名称
 * @param enforcementDate Enforcement date / 强制执行日期
 * @param isMandatory Whether verification is mandatory / 是否强制
 * @param notes Additional notes / 备注
 */
data class EnforcementTimeline(
    val country: String,
    val enforcementDate: String,
    val isMandatory: Boolean,
    val notes: String,
)

/**
 * Code snippet data model for CI configuration examples
 * CI 配置代码片段数据模型
 *
 * @param id Unique identifier / 唯一标识
 * @param title Snippet title / 片段标题
 * @param language Programming language (gradle/yaml/kotlin) / 语言
 * @param content Code content / 代码内容
 * @param description Description / 描述
 */
data class CodeSnippet(
    val id: String,
    val title: String,
    val language: String,
    val content: String,
    val description: String,
)

/**
 * Policy item data model for policy interpretation tab
 * 政策解读条目数据模型
 *
 * @param id Unique identifier / 唯一标识
 * @param title Policy title / 政策标题
 * @param description Policy description / 政策描述
 * @param severity Severity level / 严重程度
 */
data class PolicyItem(
    val id: Int,
    val title: String,
    val description: String,
    val severity: String, // "critical" | "warning" | "info"
)

/**
 * Alternative distribution strategy data model
 * 替代分发策略数据模型
 *
 * @param id Unique identifier / 唯一标识
 * @param title Strategy title / 策略标题
 * @param description Strategy description / 策略描述
 * @param applicability Applicability description / 适用性描述
 * @param steps Implementation steps / 实施步骤
 */
data class AlternativeStrategy(
    val id: Int,
    val title: String,
    val description: String,
    val applicability: String,
    val steps: List<String>,
)

// ============================================================
// State / 页面状态
// ============================================================

/**
 * Android Developer Verification page state
 * Android 开发者验证工具箱页面状态
 *
 * MVI Architecture: Model layer, holds all page state.
 * State is Immutable — each state change creates a new State object.
 *
 * @param selectedTab Current tab index (0-4) / 当前选中的 Tab 索引
 * @param isScanning Whether APK scan is in progress / APK 扫描是否进行中
 * @param scanResult Latest verification result / 最新验证结果
 * @param riskLevel Current risk level / 当前风险等级
 * @param copiedSnippetId ID of the last copied snippet / 最近复制的片段 ID
 * @param isReportVisible Whether BottomSheet report is visible / 报告 BottomSheet 是否可见
 * @param reportContent Content of the compliance report / 合规报告内容
 * @param apkPath Input APK path / 输入的 APK 路径
 *
 * @see AndroidDevVerificationIntent
 * @see AndroidDevVerificationViewModel
 */
data class AndroidDevVerificationState(
    val selectedTab: Int = 0,
    val isScanning: Boolean = false,
    val scanResult: VerificationResult? = null,
    val riskLevel: RiskLevel = RiskLevel.UNKNOWN,
    val copiedSnippetId: String? = null,
    val isReportVisible: Boolean = false,
    val reportContent: String = "",
    val apkPath: String = "",
    // Code snippet expansion state / 代码片段展开状态
    val expandedSnippets: Set<String> = emptySet(),
) {
    companion object {
        /** Initial / default state / 初始状态 */
        val Initial = AndroidDevVerificationState()
    }
}

// ============================================================
// Intent / 用户意图
// ============================================================

/**
 * User intentions for Android Developer Verification Toolkit
 * Android 开发者验证工具包用户意图
 *
 * Every user interaction on the page corresponds to an Intent.
 * ViewModel receives Intent, executes business logic, then updates State.
 *
 * @see AndroidDevVerificationViewModel.sendIntent
 */
sealed interface AndroidDevVerificationIntent {

    /**
     * Switch tab / 切换 Tab
     *
     * @param index Target tab index / 目标 Tab 索引 (0-4)
     */
    data class SelectTab(val index: Int) : AndroidDevVerificationIntent

    /**
     * Scan APK for verification status (simulated) / 扫描 APK 验证状态（模拟）
     *
     * @param apkPath Path to the APK file / APK 文件路径
     */
    data class ScanApk(val apkPath: String) : AndroidDevVerificationIntent

    /**
     * Update APK path input / 更新 APK 路径输入
     *
     * @param path APK file path / APK 文件路径
     */
    data class UpdateApkPath(val path: String) : AndroidDevVerificationIntent

    /**
     * Generate compliance report / 生成合规报告
     */
    data object GenerateReport : AndroidDevVerificationIntent

    /**
     * Copy code snippet to clipboard / 复制代码片段到剪贴板
     *
     * @param snippetId Unique ID of the snippet / 片段唯一 ID
     * @param content Content to copy / 要复制的内容
     */
    data class CopySnippet(val snippetId: String, val content: String) : AndroidDevVerificationIntent

    /**
     * Toggle code snippet expansion / 切换代码片段展开/收起
     *
     * @param snippetId Snippet unique identifier / 片段唯一标识
     */
    data class ToggleSnippet(val snippetId: String) : AndroidDevVerificationIntent

    /**
     * Open external URL / 打开外部链接
     *
     * @param url Target URL / 目标 URL
     */
    data class OpenUrl(val url: String) : AndroidDevVerificationIntent

    /**
     * Dismiss compliance report BottomSheet / 关闭合规报告 BottomSheet
     */
    data object DismissReport : AndroidDevVerificationIntent
}

// ============================================================
// Effect / 副作用
// ============================================================

/**
 * One-time side effects for Android Developer Verification Toolkit
 * Android 开发者验证工具包副作用
 *
 * One-time events, immutable, consumed only once.
 * UI layer listens via LaunchedEffect + flow.collect {}
 *
 * @see AndroidDevVerificationViewModel
 */
sealed interface AndroidDevVerificationEffect {

    /**
     * Show toast message / 显示 Toast
     *
     * @param message Toast message text / Toast 文本
     */
    data class ShowToast(val message: String) : AndroidDevVerificationEffect

    /**
     * Copy text to system clipboard / 复制文本到剪贴板
     *
     * @param text Text to copy / 要复制的文本
     */
    data class CopyToClipboard(val text: String) : AndroidDevVerificationEffect

    /**
     * Open URL in external browser / 在外部浏览器打开链接
     *
     * @param url Target URL / 目标 URL
     */
    data class OpenUrl(val url: String) : AndroidDevVerificationEffect

    /**
     * Show compliance report in BottomSheet / 在 BottomSheet 显示合规报告
     *
     * @param reportContent Report content in JSON format / JSON 格式报告内容
     */
    data class ShowReport(val reportContent: String) : AndroidDevVerificationEffect
}
