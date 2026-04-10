package com.mvi.kenny.feature.devverification

import java.time.LocalDate

/**
 * PRD-078 | Android Developer Verification Compliance Toolkit
 * MVI Contract — Defines State, Intent, and Effect for all verification flows
 *
 * Design Doc: designs/PRD-078-Android-开发者身份验证合规工具包.md
 * Status: 设计完成，待移交开发 | Completed: 2026-04-10
 */

// ============ Region & Compliance Models ============
// ============ 地区与合规数据模型 ============

/**
 * Supported regions for developer verification compliance
 * 支持开发者验证合规的地区
 */
enum class Region(val displayName: String, val deadline: LocalDate) {
    BRAZIL("Brazil", LocalDate.of(2026, 9, 30)),
    INDONESIA("Indonesia", LocalDate.of(2026, 9, 30)),
    SINGAPORE("Singapore", LocalDate.of(2026, 9, 30)),
    THAILAND("Thailand", LocalDate.of(2026, 9, 30))
}

/**
 * Overall compliance score classification
 * 整体合规评分分类
 */
enum class ComplianceScore(val label: String) {
    PASS("Pass"),
    AT_RISK("At Risk"),
    FAILED("Failed"),
    UNKNOWN("Unknown")
}

/**
 * Per-app verification status
 * 单个 App 的验证状态
 */
data class AppVerificationStatus(
    val packageName: String,
    val appName: String,
    val isVerified: Boolean,
    val verificationDate: LocalDate? = null,
    val isIgnored: Boolean = false
)

/**
 * Wizard step definitions for verification flow
 * 验证流程的向导步骤定义
 */
enum class WizardStep(val index: Int, val title: String) {
    ACCOUNT_REGISTRATION(0, "Account Registration"),
    DEVELOPER_PROFILE(1, "Developer Profile"),
    APP_SCAN(2, "App Scan"),
    ADVANCED_FLOW_PREVIEW(3, "Advanced Flow"),
    COMPLIANCE_REPORT(4, "Compliance Report")
}

// ============ Dashboard State ============
// ============ 仪表盘状态 ============

data class ComplianceDashboardState(
    val overallScore: ComplianceScore = ComplianceScore.UNKNOWN,
    val apps: List<AppVerificationStatus> = emptyList(),
    val regionDeadlines: Map<Region, LocalDate> = Region.entries.associate { it to it.deadline },
    val isScanning: Boolean = false,
    val scanProgress: Float = 0f,
    val selectedRegions: Set<Region> = Region.entries.toSet(),
    val error: String? = null
)

// ============ Wizard State ============
// ============ 向导状态 ============

data class AccountRegistrationState(
    val hasGooglePlayAccount: Boolean = false,
    val hasPaymentProfile: Boolean = false,
    val isBusinessAccount: Boolean = false,
    val registrationUrl: String = "https://play.google.com/console",
    val paymentProfileUrl: String = "https://pay.google.com"
)

data class DeveloperProfileState(
    val developerName: String = "",
    val developerEmail: String = "",
    val verifiedWebsite: String = "",
    val websiteVerified: Boolean = false
)

data class AppScanResult(
    val app: AppVerificationStatus,
    val needsVerification: Boolean,
    val reason: String = ""
)

data class AdvancedFlowPreviewState(
    val isSimulating: Boolean = false,
    val user流失风险评估: String = "Low"
)

data class ComplianceReport(
    val generatedAt: LocalDate = LocalDate.now(),
    val appsRequiringVerification: List<AppVerificationStatus> = emptyList(),
    val appsVerified: List<AppVerificationStatus> = emptyList(),
    val regionStatuses: Map<Region, ComplianceScore> = emptyMap()
)

data class VerificationWizardState(
    val currentStep: WizardStep = WizardStep.ACCOUNT_REGISTRATION,
    val accountRegistration: AccountRegistrationState = AccountRegistrationState(),
    val developerProfile: DeveloperProfileState = DeveloperProfileState(),
    val appScanResults: List<AppScanResult> = emptyList(),
    val advancedFlowPreview: AdvancedFlowPreviewState = AdvancedFlowPreviewState(),
    val complianceReport: ComplianceReport? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

// ============ MDM State ============
// ============ MDM 状态 ============

data class MDMViolation(
    val packageName: String,
    val appName: String,
    val violationType: String,
    val severity: String,
    val suggestedFix: String
)

data class MDMComplianceState(
    val violations: List<MDMViolation> = emptyList(),
    val isScanning: Boolean = false,
    val error: String? = null
)

// ============ Settings State ============
// ============ 设置状态 ============

data class SettingsState(
    val selectedRegions: Set<Region> = Region.entries.toSet(),
    val deadlineReminderEnabled: Boolean = true,
    val reportExportFormat: String = "PDF"
)

// ============ Intent ============
// ============ 用户意图 ============

sealed class DashboardIntent {
    object RefreshScan : DashboardIntent()
    data class SelectApp(val packageName: String) : DashboardIntent()
    data class BatchVerify(val packageNames: List<String>) : DashboardIntent()
    data class FilterByRegion(val regions: Set<Region>) : DashboardIntent()
    object GenerateReport : DashboardIntent()
    object NavigateToWizard : DashboardIntent()
    object NavigateToMDM : DashboardIntent()
    object NavigateToSettings : DashboardIntent()
}

sealed class WizardIntent {
    data class GoToStep(val step: WizardStep) : WizardIntent()
    object NextStep : WizardIntent()
    object PrevStep : WizardIntent()
    data class UpdateAccountRegistration(val data: AccountRegistrationState) : WizardIntent()
    data class UpdateDeveloperProfile(val data: DeveloperProfileState) : WizardIntent()
    object TriggerAppScan : WizardIntent()
    object PreviewAdvancedFlow : WizardIntent()
    object ExportReport : WizardIntent()
    object SubmitCompliance : WizardIntent()
}

sealed class MDMIntent {
    object ScanMDM : MDMIntent()
    data class IgnoreViolation(val packageName: String) : MDMIntent()
    data class FixViolation(val packageName: String) : MDMIntent()
}

sealed class SettingsIntent {
    data class UpdateRegions(val regions: Set<Region>) : SettingsIntent()
    data class UpdateDeadlineReminder(val enabled: Boolean) : SettingsIntent()
    data class UpdateExportFormat(val format: String) : SettingsIntent()
}

// ============ Effect ============
// ============ 副作用（一次性事件）===========

sealed class DashboardEffect {
    data class ShowSnackbar(val message: String) : DashboardEffect()
    data class NavigateToWizard(val step: WizardStep = WizardStep.ACCOUNT_REGISTRATION) : DashboardEffect()
    data class NavigateToMDM(val unit: Unit = Unit) : DashboardEffect()
    data class NavigateToSettings(val unit: Unit = Unit) : DashboardEffect()
    data class OpenExternalUrl(val url: String) : DashboardEffect()
    data class ExportReport(val filePath: String) : DashboardEffect()
}

sealed class WizardEffect {
    data class ShowSnackbar(val message: String) : WizardEffect()
    data class NavigateToDashboard(val unit: Unit = Unit) : WizardEffect()
    data class OpenExternalUrl(val url: String) : WizardEffect()
    data class ReportExported(val path: String) : WizardEffect()
}

sealed class MDMEffect {
    data class ShowSnackbar(val message: String) : MDMEffect()
    data class OpenExternalUrl(val url: String) : MDMEffect()
}

sealed class SettingsEffect {
    data class ShowSnackbar(val message: String) : SettingsEffect()
}
