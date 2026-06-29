package com.mvi.kenny.feature.devverificationbatch

/**
 * ============================================================
 * PRD-304 | Android 开发者身份验证合规批量管理平台
 * VerificationDashboardContract — MVI 契约层
 * ============================================================
 * MVI (Model-View-Intent) Architecture:
 * - Model (State): Immutable data class — single source of truth for UI state
 * - View: Composable functions that consume State and render UI
 * - Intent: User intentions (user actions) processed by ViewModel
 * - Effect: One-time side effects (navigation, Toast) delivered via Channel
 *
 * Design Reference: `memory/agency/designs/PRD-304-Android-开发者身份验证合规批量管理平台.md`
 * —————————————————————————————————————————————————————
 */

/**
 * 开发者数据模型 / Developer data model
 *
 * @param id Developer unique ID
 * @param name Display name
 * @param email Developer email
 * @param avatarUrl Avatar URL, null = default
 * @param verificationStatus Current verification status
 * @param appCount Number of associated apps
 * @param lastVerifiedAt Last verification timestamp (epoch ms)
 */
data class DeveloperItem(
    val id: String,
    val name: String,
    val email: String,
    val avatarUrl: String? = null,
    val verificationStatus: VerificationStatus = VerificationStatus.Pending,
    val appCount: Int = 0,
    val lastVerifiedAt: Long? = null
)

/**
 * App 数据模型 / App data model
 *
 * @param id App unique ID
 * @param name App display name
 * @param packageName App package name
 * @param developerId Associated developer ID
 * @param verificationStatus App-level verification status
 * @param expiresAt Verification expiry timestamp (epoch ms), null = no expiry
 */
data class AppItem(
    val id: String,
    val name: String,
    val packageName: String,
    val developerId: String,
    val verificationStatus: VerificationStatus = VerificationStatus.Pending,
    val expiresAt: Long? = null
)

/**
 * 仪表板统计数据 / Dashboard statistics
 *
 * @param totalDevelopers Total developer count
 * @param totalApps Total app count
 * @param verified Verified count
 * @param pending Pending verification count
 * @param expired Expired count
 * @param failed Failed count
 */
data class DashboardStats(
    val totalDevelopers: Int = 0,
    val totalApps: Int = 0,
    val verified: Int = 0,
    val pending: Int = 0,
    val expired: Int = 0,
    val failed: Int = 0
)

/**
 * 预警项 / Warning item for dashboard
 *
 * @param id Unique warning ID
 * @param title Warning title
 * @param description Warning description
 * @param severity Warning severity: CRITICAL(1), WARNING(2), INFO(3)
 * @param developerId Associated developer ID (nullable)
 * @param appId Associated app ID (nullable)
 */
data class WarningItem(
    val id: String,
    val title: String,
    val description: String,
    val severity: WarningSeverity,
    val developerId: String? = null,
    val appId: String? = null
)

/**
 * 验证状态枚举 / Verification status enum
 */
enum class VerificationStatus {
    Verified,  // 已验证
    Pending,  // 待验证
    Expired,  // 已过期
    Failed    // 验证失败
}

/**
 * 预警严重程度 / Warning severity level
 */
enum class WarningSeverity {
    CRITICAL,  // 严重（1天内过期）
    WARNING,   // 警告（7天内过期）
    INFO       // 提示（30天内过期）
}

/**
 * 报告导出格式 / Report export format
 */
enum class ReportFormat {
    PDF,
    CSV
}

/**
 * CI/CD 平台类型 / CI/CD platform type
 */
enum class CICDPlatform {
    GitHub_Actions,
    GitLab_CI,
    Jenkins
}

/**
 * ============================================================
 * State — 页面状态 / Page State
 * ============================================================
 * The single source of truth for the Verification Dashboard UI.
 * All UI state is captured here; no additional mutable state allowed in Composables.
 *
 * @see VerificationDashboardViewModel
 */
data class VerificationDashboardState(
    // List data / 列表数据
    val developers: List<DeveloperItem> = emptyList(),
    val apps: List<AppItem> = emptyList(),

    // Selection state / 选择状态
    val selectedDevelopers: Set<String> = emptySet(),
    val selectedApps: Set<String> = emptySet(),

    // Filter & search / 筛选与搜索
    val filterStatus: VerificationStatus? = null,
    val searchQuery: String = "",

    // Loading states / 加载状态
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val isBatchSubmitting: Boolean = false,

    // Dashboard stats / 仪表板统计
    val countdownDays: Int = 0,  // Days until Sep 30 enforcement deadline
    val stats: DashboardStats = DashboardStats(),
    val warnings: List<WarningItem> = emptyList(),

    // Wizard state / 向导状态
    val showWizard: Boolean = false,
    val wizardStep: Int = 0,

    // CI/CD config / CI/CD 配置
    val cicdPlatform: CICDPlatform = CICDPlatform.GitHub_Actions,
    val generatedConfigSnippet: String = "",

    // Error / 错误
    val errorMessage: String? = null,

    // Active tab / 当前 Tab
    val activeTab: DashboardTab = DashboardTab.Dashboard
) {
    companion object {
        /** Initial state / 初始状态 */
        val Initial = VerificationDashboardState()
    }
}

/**
 * Dashboard Tab / 仪表板 Tab
 */
enum class DashboardTab {
    Dashboard,     // 主仪表板
    Developers,    // 开发者管理
    Apps,          // App 管理
    CICD,          // CI/CD 集成
    Reports        // 合规报告
}

/**
 * ============================================================
 * Intent — 用户意图 / User Intents
 * ============================================================
 * Every user action on the dashboard corresponds to an Intent.
 * ViewModel receives Intent via sendIntent() and processes business logic.
 *
 * @see VerificationDashboardViewModel.sendIntent
 */
sealed interface VerificationDashboardIntent {
    /** Load initial dashboard data / 加载初始仪表板数据 */
    data object LoadDashboard : VerificationDashboardIntent

    /** Refresh dashboard data / 刷新仪表板数据 */
    data object RefreshDashboard : VerificationDashboardIntent

    /** Search developers or apps / 搜索开发者或 App */
    data class Search(val query: String) : VerificationDashboardIntent

    /** Filter by verification status / 按验证状态筛选 */
    data class FilterByStatus(val status: VerificationStatus?) : VerificationDashboardIntent

    /** Select/deselect a developer / 选择/取消选择开发者 */
    data class ToggleDeveloperSelection(val developerId: String) : VerificationDashboardIntent

    /** Select/deselect an app / 选择/取消选择 App */
    data class ToggleAppSelection(val appId: String) : VerificationDashboardIntent

    /** Select all filtered developers / 全选当前筛选的开发者 */
    data object SelectAllFiltered : VerificationDashboardIntent

    /** Clear all selections / 清除所有选择 */
    data object ClearSelection : VerificationDashboardIntent

    /** Open verification wizard / 打开验证申请向导 */
    data class OpenWizard(val preSelectedDeveloperIds: List<String> = emptyList()) : VerificationDashboardIntent

    /** Close verification wizard / 关闭验证申请向导 */
    data object CloseWizard : VerificationDashboardIntent

    /** Advance wizard to next step / 向导前进到下一步 */
    data object WizardNextStep : VerificationDashboardIntent

    /** Go back wizard to previous step / 向导向后退一步 */
    data object WizardPrevStep : VerificationDashboardIntent

    /** Submit batch verification application / 提交批量验证申请 */
    data class BatchSubmit(
        val developerIds: List<String>,
        val appIds: List<String>,
        val verificationType: VerificationType
    ) : VerificationDashboardIntent

    /** Refresh single developer status / 刷新单个开发者验证状态 */
    data class RefreshDeveloperStatus(val developerId: String) : VerificationDashboardIntent

    /** Switch active tab / 切换 Tab */
    data class SwitchTab(val tab: DashboardTab) : VerificationDashboardIntent

    /** Generate CI/CD config snippet / 生成 CI/CD 配置片段 */
    data class GenerateCICDSnippet(val platform: CICDPlatform) : VerificationDashboardIntent

    /** Copy CI/CD config to clipboard / 复制 CI/CD 配置到剪贴板 */
    data object CopyCICDSnippet : VerificationDashboardIntent

    /** Export compliance report / 导出合规报告 */
    data class ExportReport(
        val format: ReportFormat,
        val dateRange: DateRange
    ) : VerificationDashboardIntent

    /** Dismiss error / 关闭错误提示 */
    data object DismissError : VerificationDashboardIntent
}

/**
 * 验证类型 / Verification type
 */
enum class VerificationType {
    FirstTime,   // 首次验证
    Renewal      // 续期
}

/**
 * 报告日期范围 / Report date range
 */
data class DateRange(
    val startDate: Long,  // epoch ms
    val endDate: Long     // epoch ms
)

/**
 * ============================================================
 * Effect — 一次性副作用 / One-time Side Effects
 * ============================================================
 * Effects are immutable and can only be consumed once.
 * UI layer listens via LaunchedEffect + effect.collect {}
 *
 * @see VerificationDashboardViewModel._effect
 */
sealed interface VerificationDashboardEffect {
    /** Show success toast / 显示成功提示 */
    data class ShowSuccess(val message: String) : VerificationDashboardEffect

    /** Show error toast with optional retry action / 显示错误提示（可选重试） */
    data class ShowError(val message: String, val retryIntent: VerificationDashboardIntent? = null) : VerificationDashboardEffect

    /** Navigate to verification wizard / 导航到验证向导 */
    data class NavigateToWizard(val preSelectedDeveloperIds: List<String>) : VerificationDashboardEffect

    /** Content copied to clipboard / 内容已复制到剪贴板 */
    data class CopiedToClipboard(val content: String) : VerificationDashboardEffect

    /** Report export completed / 报告导出完成 */
    data class ReportExported(val format: ReportFormat, val filePath: String) : VerificationDashboardEffect

    /** Webhook triggered / Webhook 已触发 */
    data class WebhookTriggered(val event: String) : VerificationDashboardEffect

    /** Show confirmation dialog / 显示确认对话框 */
    data class ShowConfirmDialog(
        val title: String,
        val message: String,
        val confirmIntent: VerificationDashboardIntent,
        val dismissIntent: VerificationDashboardIntent
    ) : VerificationDashboardEffect
}
