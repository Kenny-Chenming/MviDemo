package com.mvi.kenny.feature.devverification

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import androidx.lifecycle.viewModelScope

/**
 * PRD-078 | Android Developer Verification Compliance Toolkit
 * ViewModel — Manages Dashboard, Wizard, MDM, and Settings states
 *
 * Design: ComplianceDashboardState / VerificationWizardState / MDMComplianceState / SettingsState
 * MVI Pattern: State (UI) + Intent (User Action) + Effect (One-time events)
 * Bilingual comments: CN + EN
 */

class DevVerificationViewModel : ViewModel() {

    /* ============ Dashboard ViewModel ============ */
    /* ============ 仪表盘 ViewModel ============ */

    private val _dashboardState = MutableStateFlow(ComplianceDashboardState())
val dashboardState: StateFlow<ComplianceDashboardState> = _dashboardState.asStateFlow()

private val _dashboardEffects = Channel<DashboardEffect>(Channel.BUFFERED)
val dashboardEffects = _dashboardEffects.receiveAsFlow()

fun processDashboardIntent(intent: DashboardIntent) {
    viewModelScope.launch {
        when (intent) {
            is DashboardIntent.RefreshScan -> handleRefreshScan()
            is DashboardIntent.SelectApp -> handleSelectApp(intent.packageName)
            is DashboardIntent.BatchVerify -> handleBatchVerify(intent.packageNames)
            is DashboardIntent.FilterByRegion -> handleFilterByRegion(intent.regions)
            is DashboardIntent.GenerateReport -> handleGenerateReport()
            is DashboardIntent.NavigateToWizard -> _dashboardEffects.send(DashboardEffect.NavigateToWizard())
            is DashboardIntent.NavigateToMDM -> _dashboardEffects.send(DashboardEffect.NavigateToMDM())
            is DashboardIntent.NavigateToSettings -> _dashboardEffects.send(DashboardEffect.NavigateToSettings())
        }
    }
}

private suspend fun handleRefreshScan() {
    _dashboardState.value = _dashboardState.value.copy(isScanning = true, scanProgress = 0f, error = null)
    // Simulate Gradle plugin scan progress
    // 模拟 Gradle 插件扫描进度
    repeat(10) { step ->
        delay(200)
        _dashboardState.value = _dashboardState.value.copy(scanProgress = (step + 1) / 10f)
    }
    // Mock scan results — replace with actual Gradle plugin JSON parsing
    // 模拟扫描结果 — 实际使用时替换为 Gradle 插件 JSON 解析
    val mockApps = listOf(
        AppVerificationStatus("com.example.myapp", "My App", isVerified = true, verificationDate = LocalDate.now().minusDays(30)),
        AppVerificationStatus("com.example.oldapp", "Old App", isVerified = false),
        AppVerificationStatus("com.example.newapp", "New App", isVerified = false)
    )
    val score = when {
        mockApps.all { it.isVerified } -> ComplianceScore.PASS
        mockApps.any { !it.isVerified } -> ComplianceScore.AT_RISK
        else -> ComplianceScore.UNKNOWN
    }
    _dashboardState.value = _dashboardState.value.copy(
        isScanning = false,
        scanProgress = 1f,
        apps = mockApps,
        overallScore = score
    )
    _dashboardEffects.send(DashboardEffect.ShowSnackbar("Scan complete: ${mockApps.size} apps found"))
}

private suspend fun handleSelectApp(packageName: String) {
    _dashboardEffects.send(DashboardEffect.ShowSnackbar("Selected: $packageName"))
}

private suspend fun handleBatchVerify(packageNames: List<String>) {
    _dashboardEffects.send(DashboardEffect.ShowSnackbar("${packageNames.size} apps queued for verification"))
}

private fun handleFilterByRegion(regions: Set<Region>) {
    _dashboardState.value = _dashboardState.value.copy(selectedRegions = regions)
}

private suspend fun handleGenerateReport() {
    val apps = _dashboardState.value.apps
    val verifiedCount = apps.count { it.isVerified }
    val totalCount = apps.size
    _dashboardEffects.send(DashboardEffect.ShowSnackbar("Report: $verifiedCount/$totalCount verified"))
    _dashboardEffects.send(DashboardEffect.ExportReport("/data/reports/dev-verification-${LocalDate.now()}.pdf"))
}

/* ============ Wizard ViewModel ============ */
/* ============ 向导 ViewModel ============ */

private val _wizardState = MutableStateFlow(VerificationWizardState())
val wizardState: StateFlow<VerificationWizardState> = _wizardState.asStateFlow()

private val _wizardEffects = Channel<WizardEffect>(Channel.BUFFERED)
val wizardEffects = _wizardEffects.receiveAsFlow()

fun processWizardIntent(intent: WizardIntent) {
    viewModelScope.launch {
        when (intent) {
            is WizardIntent.GoToStep -> handleGoToStep(intent.step)
            is WizardIntent.NextStep -> handleNextStep()
            is WizardIntent.PrevStep -> handlePrevStep()
            is WizardIntent.UpdateAccountRegistration -> handleUpdateAccountRegistration(intent.data)
            is WizardIntent.UpdateDeveloperProfile -> handleUpdateDeveloperProfile(intent.data)
            is WizardIntent.TriggerAppScan -> handleTriggerAppScan()
            is WizardIntent.PreviewAdvancedFlow -> handlePreviewAdvancedFlow()
            is WizardIntent.ExportReport -> handleExportReport()
            is WizardIntent.SubmitCompliance -> handleSubmitCompliance()
        }
    }
}

private fun handleGoToStep(step: WizardStep) {
    _wizardState.value = _wizardState.value.copy(currentStep = step)
}

private fun handleNextStep() {
    val current = _wizardState.value.currentStep
    val nextStep = WizardStep.entries.getOrNull(current.index + 1)
    if (nextStep != null) {
        _wizardState.value = _wizardState.value.copy(currentStep = nextStep)
    }
}

private fun handlePrevStep() {
    val current = _wizardState.value.currentStep
    val prevStep = WizardStep.entries.getOrNull(current.index - 1)
    if (prevStep != null) {
        _wizardState.value = _wizardState.value.copy(currentStep = prevStep)
    }
}

private fun handleUpdateAccountRegistration(data: AccountRegistrationState) {
    _wizardState.value = _wizardState.value.copy(accountRegistration = data)
}

private fun handleUpdateDeveloperProfile(data: DeveloperProfileState) {
    _wizardState.value = _wizardState.value.copy(developerProfile = data)
}

private suspend fun handleTriggerAppScan() {
    _wizardState.value = _wizardState.value.copy(isLoading = true, error = null)
    delay(1500) // Simulate Gradle plugin scan / 模拟 Gradle 插件扫描
    val apps = _dashboardState.value.apps
    val results = apps.map { app ->
        AppScanResult(
            app = app,
            needsVerification = !app.isVerified,
            reason = if (!app.isVerified) "App not registered under verified developer account" else "Verified"
        )
    }
    _wizardState.value = _wizardState.value.copy(
        isLoading = false,
        appScanResults = results,
        advancedFlowPreview = AdvancedFlowPreviewState(isSimulating = false)
    )
    _wizardEffects.send(WizardEffect.ShowSnackbar("App scan complete: ${results.count { it.needsVerification }} need verification"))
}

private suspend fun handlePreviewAdvancedFlow() {
    _wizardState.value = _wizardState.value.copy(
        advancedFlowPreview = _wizardState.value.advancedFlowPreview.copy(isSimulating = true)
    )
    delay(1000)
    _wizardState.value = _wizardState.value.copy(
        advancedFlowPreview = _wizardState.value.advancedFlowPreview.copy(
            isSimulating = false,
            user流失风险评估 = "Medium"
        )
    )
}

private suspend fun handleExportReport() {
    val state = _wizardState.value
    val apps = state.appScanResults.map { it.app }
    val report = ComplianceReport(
        generatedAt = LocalDate.now(),
        appsRequiringVerification = apps.filter { !it.isVerified },
        appsVerified = apps.filter { it.isVerified },
        regionStatuses = Region.entries.associateWith { ComplianceScore.AT_RISK }
    )
    _wizardState.value = _wizardState.value.copy(complianceReport = report)
    _wizardEffects.send(WizardEffect.ReportExported("/data/reports/compliance-report-${LocalDate.now()}.pdf"))
}

private suspend fun handleSubmitCompliance() {
    _wizardState.value = _wizardState.value.copy(isLoading = true)
    delay(1000)
    _wizardState.value = _wizardState.value.copy(isLoading = false)
    _wizardEffects.send(WizardEffect.ShowSnackbar("Compliance submission initiated"))
    _wizardEffects.send(WizardEffect.NavigateToDashboard())
}

/* ============ MDM ViewModel ============ */
/* ============ MDM ViewModel ============ */

private val _mdmState = MutableStateFlow(MDMComplianceState())
val mdmState: StateFlow<MDMComplianceState> = _mdmState.asStateFlow()

private val _mdmEffects = Channel<MDMEffect>(Channel.BUFFERED)
val mdmEffects = _mdmEffects.receiveAsFlow()

fun processMDMIntent(intent: MDMIntent) {
    viewModelScope.launch {
        when (intent) {
            is MDMIntent.ScanMDM -> handleScanMDM()
            is MDMIntent.IgnoreViolation -> handleIgnoreViolation(intent.packageName)
            is MDMIntent.FixViolation -> handleFixViolation(intent.packageName)
        }
    }
}

private suspend fun handleScanMDM() {
    _mdmState.value = _mdmState.value.copy(isScanning = true, error = null)
    delay(2000) // Simulate MDM API scan / 模拟 MDM API 扫描
    val mockViolations = listOf(
        MDMViolation(
            packageName = "com.example.corporate.app",
            appName = "Corporate App",
            violationType = "Unverified developer distribution",
            severity = "High",
            suggestedFix = "Register developer account and reassign app ownership"
        )
    )
    _mdmState.value = _mdmState.value.copy(isScanning = false, violations = mockViolations)
    _mdmEffects.send(MDMEffect.ShowSnackbar("MDM scan complete: ${mockViolations.size} violations found"))
}

private suspend fun handleIgnoreViolation(packageName: String) {
    val updated = _mdmState.value.violations.filter { it.packageName != packageName }
    _mdmState.value = _mdmState.value.copy(violations = updated)
    _mdmEffects.send(MDMEffect.ShowSnackbar("Violation ignored: $packageName"))
}

private suspend fun handleFixViolation(packageName: String) {
    _mdmEffects.send(MDMEffect.ShowSnackbar("Fix guidance opened for: $packageName"))
    _mdmEffects.send(MDMEffect.OpenExternalUrl("https://developer.android.com/developer-verification"))
}

/* ============ Settings ViewModel ============ */
/* ============ 设置 ViewModel ============ */

private val _settingsState = MutableStateFlow(SettingsState())
val settingsState: StateFlow<SettingsState> = _settingsState.asStateFlow()

private val _settingsEffects = Channel<SettingsEffect>(Channel.BUFFERED)
val settingsEffects = _settingsEffects.receiveAsFlow()

fun processSettingsIntent(intent: SettingsIntent) {
    viewModelScope.launch {
        when (intent) {
            is SettingsIntent.UpdateRegions -> {
                _settingsState.value = _settingsState.value.copy(selectedRegions = intent.regions)
                _settingsEffects.send(SettingsEffect.ShowSnackbar("Regions updated"))
            }
            is SettingsIntent.UpdateDeadlineReminder -> {
                _settingsState.value = _settingsState.value.copy(deadlineReminderEnabled = intent.enabled)
                val msg = if (intent.enabled) "Deadline reminders enabled" else "Deadline reminders disabled"
                _settingsEffects.send(SettingsEffect.ShowSnackbar(msg))
            }
            is SettingsIntent.UpdateExportFormat -> {
                _settingsState.value = _settingsState.value.copy(reportExportFormat = intent.format)
                _settingsEffects.send(SettingsEffect.ShowSnackbar("Export format: ${intent.format}"))
            }
        }
    }
}

/* ============ Shared Utility Functions ============ */
/* ============ 共享工具函数 ============ */

/**
 * Calculate days remaining until a specific region's deadline
 * 计算距特定地区截止日期的剩余天数
 */
fun daysUntilDeadline(region: Region): Long {
    val today = LocalDate.now()
    return ChronoUnit.DAYS.between(today, region.deadline).coerceAtLeast(0)
}

/**
 * Determine compliance score based on app verification coverage
 * 根据 App 验证覆盖率确定合规评分
 */
    fun calculateOverallScore(apps: List<AppVerificationStatus>): ComplianceScore {
        if (apps.isEmpty()) return ComplianceScore.UNKNOWN
        val verifiedRatio = apps.count { it.isVerified }.toFloat() / apps.size
        return when {
            verifiedRatio >= 1f -> ComplianceScore.PASS
            verifiedRatio >= 0.5f -> ComplianceScore.AT_RISK
            else -> ComplianceScore.FAILED
        }
    }
} // class DevVerificationViewModel
