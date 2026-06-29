package com.mvi.kenny.feature.devverificationbatch

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

/**
 * ============================================================
 * PRD-304 | Android 开发者身份验证合规批量管理平台
 * VerificationDashboardViewModel — 状态管理 / State Management
 * ============================================================
 * Inherits ViewModel, holds VerificationDashboardState and VerificationDashboardEffect.
 *
 * State Management:
 * - _state: Private MutableStateFlow — written internally by ViewModel
 * - state: Public StateFlow — UI layer subscribes via collectAsState
 *
 * Effect Management:
 * - _effect: Channel (hot flow), buffer size BUFFERED
 * - effect: receiveAsFlow — UI layer listens via collect{}
 *
 * Why Channel instead of StateFlow for Effects?
 * —————————————————————————————————————————————————————
 * StateFlow remembers the last value; new subscribers receive the stale value.
 * Channel delivers only new events, ideal for "one-time" events (navigation, toast).
 *
 * @see VerificationDashboardState Page state definition
 * @see VerificationDashboardIntent User intents
 * @see VerificationDashboardEffect Side effects
 */
class VerificationDashboardViewModel : ViewModel() {

    // ─────────────────────────────────────────────────────────
    // State — 页面状态 / Page State
    // ─────────────────────────────────────────────────────────

    private val _state = MutableStateFlow(VerificationDashboardState.Initial)
    val state: StateFlow<VerificationDashboardState> = _state.asStateFlow()

    /** Snapshot of current state for use in Compose lambdas */
    val currentState: VerificationDashboardState get() = _state.value

    // ─────────────────────────────────────────────────────────
    // Effect Channel — 副作用通道 / Side Effects
    // ─────────────────────────────────────────────────────────

    private val _effect = Channel<VerificationDashboardEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    // ─────────────────────────────────────────────────────────
    // Mock Data Streams — 模拟数据流 / Simulated Data
    // ─────────────────────────────────────────────────────────

    /** Simulated developer list / 模拟开发者列表 */
    private val _developersFlow = MutableStateFlow<List<DeveloperItem>>(emptyList())

    /** Simulated app list / 模拟 App 列表 */
    private val _appsFlow = MutableStateFlow<List<AppItem>>(emptyList())

    // ─────────────────────────────────────────────────────────
    // Init — ViewModel 初始化时自动加载数据
    // ─────────────────────────────────────────────────────────

    init {
        sendIntent(VerificationDashboardIntent.LoadDashboard)
    }

    // ─────────────────────────────────────────────────────────
    // Intent Processing — 接收并处理用户意图
    // ─────────────────────────────────────────────────────────
    // Entry point called by UI: viewModel.sendIntent(intent)
    // Routes to appropriate handler based on Intent type.

    fun sendIntent(intent: VerificationDashboardIntent) {
        when (intent) {
            is VerificationDashboardIntent.LoadDashboard -> loadDashboard()
            is VerificationDashboardIntent.RefreshDashboard -> refreshDashboard()
            is VerificationDashboardIntent.Search -> search(intent.query)
            is VerificationDashboardIntent.FilterByStatus -> filterByStatus(intent.status)
            is VerificationDashboardIntent.ToggleDeveloperSelection -> toggleDeveloperSelection(intent.developerId)
            is VerificationDashboardIntent.ToggleAppSelection -> toggleAppSelection(intent.appId)
            is VerificationDashboardIntent.SelectAllFiltered -> selectAllFiltered()
            is VerificationDashboardIntent.ClearSelection -> clearSelection()
            is VerificationDashboardIntent.OpenWizard -> openWizard(intent.preSelectedDeveloperIds)
            is VerificationDashboardIntent.CloseWizard -> closeWizard()
            is VerificationDashboardIntent.WizardNextStep -> wizardNextStep()
            is VerificationDashboardIntent.WizardPrevStep -> wizardPrevStep()
            is VerificationDashboardIntent.BatchSubmit -> batchSubmit(intent.developerIds, intent.appIds, intent.verificationType)
            is VerificationDashboardIntent.RefreshDeveloperStatus -> refreshDeveloperStatus(intent.developerId)
            is VerificationDashboardIntent.SwitchTab -> switchTab(intent.tab)
            is VerificationDashboardIntent.GenerateCICDSnippet -> generateCICDSnippet(intent.platform)
            is VerificationDashboardIntent.CopyCICDSnippet -> copyCICDSnippet()
            is VerificationDashboardIntent.ExportReport -> exportReport(intent.format, intent.dateRange)
            is VerificationDashboardIntent.DismissError -> dismissError()
        }
    }

    // ─────────────────────────────────────────────────────────
    // Load Dashboard — 加载仪表板数据
    // ─────────────────────────────────────────────────────────

    /**
     * Load initial dashboard data including developer list, app list, stats, and warnings.
     * 加载初始仪表板数据（开发者列表、App 列表、统计数据、预警列表）。
     *
     * Flow:
     * 1. Set isLoading = true to show loading indicator
     * 2. Fetch mock data with simulated delay
     * 3. Compute dashboard stats from fetched data
     * 4. Generate warnings (expired / expiring soon)
     * 5. Update state; on error set errorMessage
     */
    private fun loadDashboard() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, errorMessage = null)
            try {
                // Simulate network request / 模拟网络请求
                delay(800)

                // Mock developer data / 模拟开发者数据
                val mockDevelopers = listOf(
                    DeveloperItem("dev_001", "Alice Chen", "alice@company.com", null, VerificationStatus.Verified, 3, System.currentTimeMillis() - 30L * 24 * 3600 * 1000),
                    DeveloperItem("dev_002", "Bob Wang", "bob@company.com", null, VerificationStatus.Verified, 2, System.currentTimeMillis() - 15L * 24 * 3600 * 1000),
                    DeveloperItem("dev_003", "Charlie Li", "charlie@company.com", null, VerificationStatus.Pending, 1, null),
                    DeveloperItem("dev_004", "Diana Zhang", "diana@company.com", null, VerificationStatus.Expired, 4, System.currentTimeMillis() - 90L * 24 * 3600 * 1000),
                    DeveloperItem("dev_005", "Eric Liu", "eric@company.com", null, VerificationStatus.Failed, 2, null),
                    DeveloperItem("dev_006", "Fiona Wu", "fiona@company.com", null, VerificationStatus.Pending, 3, null),
                    DeveloperItem("dev_007", "George Xu", "george@company.com", null, VerificationStatus.Verified, 1, System.currentTimeMillis() - 5L * 24 * 3600 * 1000),
                    DeveloperItem("dev_008", "Hannah Sun", "hannah@company.com", null, VerificationStatus.Expired, 2, System.currentTimeMillis() - 60L * 24 * 3600 * 1000),
                )
                _developersFlow.value = mockDevelopers

                // Mock app data / 模拟 App 数据
                val mockApps = mockDevelopers.flatMapIndexed { devIdx, dev ->
                    (1..dev.appCount).map { appIdx ->
                        val appId = "app_${devIdx * 3 + appIdx}"
                        val status = when {
                            dev.verificationStatus == VerificationStatus.Verified && appIdx <= dev.appCount / 2 -> VerificationStatus.Verified
                            dev.verificationStatus == VerificationStatus.Expired -> VerificationStatus.Expired
                            dev.verificationStatus == VerificationStatus.Failed -> VerificationStatus.Failed
                            else -> VerificationStatus.Pending
                        }
                        AppItem(
                            id = appId,
                            name = "App ${devIdx * 3 + appIdx}",
                            packageName = "com.company.app${devIdx * 3 + appIdx}",
                            developerId = dev.id,
                            verificationStatus = status,
                            expiresAt = if (status == VerificationStatus.Verified) {
                                System.currentTimeMillis() + (30 + appIdx * 10) * 24L * 3600 * 1000
                            } else null
                        )
                    }
                }
                _appsFlow.value = mockApps

                // Compute stats / 计算统计数据
                val stats = DashboardStats(
                    totalDevelopers = mockDevelopers.size,
                    totalApps = mockApps.size,
                    verified = mockDevelopers.count { it.verificationStatus == VerificationStatus.Verified },
                    pending = mockDevelopers.count { it.verificationStatus == VerificationStatus.Pending },
                    expired = mockDevelopers.count { it.verificationStatus == VerificationStatus.Expired },
                    failed = mockDevelopers.count { it.verificationStatus == VerificationStatus.Failed }
                )

                // Calculate countdown to Sep 30, 2026 / 计算距离2026年9月30日的倒计时
                val sep30 = java.time.LocalDate.of(2026, 9, 30)
                val today = java.time.LocalDate.now()
                val countdownDays = java.time.temporal.ChronoUnit.DAYS.between(today, sep30).toInt().coerceAtLeast(0)

                // Generate warnings / 生成预警列表
                val warnings = buildWarnings(mockDevelopers, countdownDays)

                _state.value = _state.value.copy(
                    isLoading = false,
                    developers = mockDevelopers,
                    apps = mockApps,
                    stats = stats,
                    countdownDays = countdownDays,
                    warnings = warnings
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    errorMessage = "Failed to load dashboard: ${e.message}"
                )
                _effect.send(VerificationDashboardEffect.ShowError("加载失败: ${e.message}"))
            }
        }
    }

    /**
     * Build warnings from developer data / 从开发者数据生成预警列表
     *
     * @param developers List of developers / 开发者列表
     * @param countdownDays Days until Sep 30 deadline / 距离9月30日的天数
     */
    private fun buildWarnings(developers: List<DeveloperItem>, countdownDays: Int): List<WarningItem> {
        val warnings = mutableListOf<WarningItem>()

        // Deadline warning / 截止日期预警
        warnings.add(WarningItem(
            id = "deadline",
            title = "强制执行倒计时 / Enforcement Countdown",
            description = "$countdownDays days until Sep 30, 2026 — Brazil, Indonesia, Singapore, Thailand enforcement",
            severity = if (countdownDays <= 7) WarningSeverity.CRITICAL else if (countdownDays <= 30) WarningSeverity.WARNING else WarningSeverity.INFO
        ))

        // Expired developers / 已过期开发者
        developers.filter { it.verificationStatus == VerificationStatus.Expired }.forEach { dev ->
            warnings.add(WarningItem(
                id = "expired_${dev.id}",
                title = "开发者验证已过期 / Developer Verification Expired",
                description = "${dev.name} (${dev.email}) — verification expired",
                severity = WarningSeverity.CRITICAL,
                developerId = dev.id
            ))
        }

        // Failed developers / 验证失败开发者
        developers.filter { it.verificationStatus == VerificationStatus.Failed }.forEach { dev ->
            warnings.add(WarningItem(
                id = "failed_${dev.id}",
                title = "开发者验证失败 / Developer Verification Failed",
                description = "${dev.name} (${dev.email}) — verification failed, needs re-application",
                severity = WarningSeverity.WARNING,
                developerId = dev.id
            ))
        }

        // Pending too long / 待验证时间过长
        developers.filter { it.verificationStatus == VerificationStatus.Pending }.take(3).forEach { dev ->
            warnings.add(WarningItem(
                id = "pending_${dev.id}",
                title = "开发者待验证 / Developer Pending",
                description = "${dev.name} (${dev.email}) — pending since submission",
                severity = WarningSeverity.INFO,
                developerId = dev.id
            ))
        }

        return warnings.sortedBy { it.severity.ordinal }
    }

    // ─────────────────────────────────────────────────────────
    // Refresh Dashboard — 刷新仪表板数据
    // ─────────────────────────────────────────────────────────

    /**
     * Refresh dashboard data without full reload animation.
     * 刷新仪表板数据（不显示全量加载动画）。
     */
    private fun refreshDashboard() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isRefreshing = true)
            try {
                delay(500)
                // In production, re-fetch from API / 生产环境应重新请求 API
                _state.value = _state.value.copy(isRefreshing = false)
                _effect.send(VerificationDashboardEffect.ShowSuccess("刷新成功 / Refreshed"))
            } catch (e: Exception) {
                _state.value = _state.value.copy(isRefreshing = false)
                _effect.send(VerificationDashboardEffect.ShowError("刷新失败: ${e.message}"))
            }
        }
    }

    // ─────────────────────────────────────────────────────────
    // Search — 搜索开发者或 App
    // ─────────────────────────────────────────────────────────

    /**
     * Filter developers and apps by search query.
     * 根据搜索关键词过滤开发者和 App。
     *
     * @param query Search query / 搜索关键词
     */
    private fun search(query: String) {
        _state.value = _state.value.copy(searchQuery = query)
        applyFilters()
    }

    // ─────────────────────────────────────────────────────────
    // Filter by Status — 按验证状态筛选
    // ─────────────────────────────────────────────────────────

    /**
     * Filter developers and apps by verification status.
     * 按验证状态筛选开发者和 App。
     *
     * @param status Verification status to filter by, null = no filter / 筛选状态，null = 不过滤
     */
    private fun filterByStatus(status: VerificationStatus?) {
        _state.value = _state.value.copy(filterStatus = status)
        applyFilters()
    }

    /**
     * Apply current search query and status filter to developer and app lists.
     * 应用当前搜索词和状态筛选条件。
     */
    private fun applyFilters() {
        val query = _state.value.searchQuery.lowercase()
        val status = _state.value.filterStatus

        val filteredDevelopers = _developersFlow.value.filter { dev ->
            val matchesQuery = query.isEmpty() ||
                    dev.name.lowercase().contains(query) ||
                    dev.email.lowercase().contains(query)
            val matchesStatus = status == null || dev.verificationStatus == status
            matchesQuery && matchesStatus
        }

        val filteredAppIds = filteredDevelopers.map { it.id }.toSet()
        val filteredApps = _appsFlow.value.filter { app ->
            val matchesQuery = query.isEmpty() ||
                    app.name.lowercase().contains(query) ||
                    app.packageName.lowercase().contains(query)
            val matchesStatus = status == null || app.verificationStatus == status
            val matchesDeveloper = filteredAppIds.contains(app.developerId) || filteredDevelopers.any { it.id == app.developerId }
            matchesQuery && matchesStatus && matchesDeveloper
        }

        _state.value = _state.value.copy(
            developers = filteredDevelopers,
            apps = filteredApps
        )
    }

    // ─────────────────────────────────────────────────────────
    // Selection — 选择管理
    // ─────────────────────────────────────────────────────────

    /** Toggle developer selection / 切换开发者选择状态 */
    private fun toggleDeveloperSelection(developerId: String) {
        val current = _state.value.selectedDevelopers.toMutableSet()
        if (current.contains(developerId)) current.remove(developerId) else current.add(developerId)
        _state.value = _state.value.copy(selectedDevelopers = current)
    }

    /** Toggle app selection / 切换 App 选择状态 */
    private fun toggleAppSelection(appId: String) {
        val current = _state.value.selectedApps.toMutableSet()
        if (current.contains(appId)) current.remove(appId) else current.add(appId)
        _state.value = _state.value.copy(selectedApps = current)
    }

    /** Select all filtered developers / 全选当前筛选的开发者 */
    private fun selectAllFiltered() {
        val allIds = _state.value.developers.map { it.id }.toSet()
        _state.value = _state.value.copy(selectedDevelopers = allIds)
    }

    /** Clear all selections / 清除所有选择 */
    private fun clearSelection() {
        _state.value = _state.value.copy(selectedDevelopers = emptySet(), selectedApps = emptySet())
    }

    // ─────────────────────────────────────────────────────────
    // Wizard — 验证申请向导
    // ─────────────────────────────────────────────────────────

    /** Open wizard with optional pre-selected developers / 打开向导（可选预选开发者） */
    private fun openWizard(preSelectedDeveloperIds: List<String>) {
        val selected = if (preSelectedDeveloperIds.isNotEmpty()) {
            preSelectedDeveloperIds.toSet()
        } else {
            _state.value.selectedDevelopers
        }
        _state.value = _state.value.copy(
            showWizard = true,
            wizardStep = 0,
            selectedDevelopers = selected
        )
    }

    /** Close wizard / 关闭向导 */
    private fun closeWizard() {
        _state.value = _state.value.copy(showWizard = false, wizardStep = 0)
    }

    /** Advance to next wizard step / 向导前进 */
    private fun wizardNextStep() {
        val currentStep = _state.value.wizardStep
        if (currentStep < 3) {
            _state.value = _state.value.copy(wizardStep = currentStep + 1)
        }
    }

    /** Go back to previous wizard step / 向导后退 */
    private fun wizardPrevStep() {
        val currentStep = _state.value.wizardStep
        if (currentStep > 0) {
            _state.value = _state.value.copy(wizardStep = currentStep - 1)
        }
    }

    // ─────────────────────────────────────────────────────────
    // Batch Submit — 批量提交验证申请
    // ─────────────────────────────────────────────────────────

    /**
     * Submit batch verification application for selected developers and apps.
     * 批量提交验证申请。
     *
     * @param developerIds List of developer IDs to submit / 开发者 ID 列表
     * @param appIds List of app IDs to submit / App ID 列表
     * @param verificationType First-time or renewal / 首次验证或续期
     */
    private fun batchSubmit(
        developerIds: List<String>,
        appIds: List<String>,
        verificationType: VerificationType
    ) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isBatchSubmitting = true)
            try {
                // Simulate API call / 模拟 API 调用
                delay(1500)

                // Update local state to reflect submission / 更新本地状态
                val updatedDevelopers = _developersFlow.value.map { dev ->
                    if (developerIds.contains(dev.id)) {
                        dev.copy(verificationStatus = VerificationStatus.Pending)
                    } else dev
                }
                _developersFlow.value = updatedDevelopers
                applyFilters()

                // Recompute stats / 重新计算统计
                val stats = _state.value.stats.copy(
                    pending = updatedDevelopers.count { it.verificationStatus == VerificationStatus.Pending },
                    verified = updatedDevelopers.count { it.verificationStatus == VerificationStatus.Verified }
                )
                _state.value = _state.value.copy(
                    isBatchSubmitting = false,
                    showWizard = false,
                    wizardStep = 0,
                    stats = stats,
                    selectedDevelopers = emptySet()
                )
                _effect.send(VerificationDashboardEffect.ShowSuccess(
                    "已提交 ${developerIds.size} 个开发者验证申请 / Submitted ${developerIds.size} verification applications"
                ))
            } catch (e: Exception) {
                _state.value = _state.value.copy(isBatchSubmitting = false)
                _effect.send(VerificationDashboardEffect.ShowError(
                    "提交失败: ${e.message}",
                    retryIntent = VerificationDashboardIntent.BatchSubmit(developerIds, appIds, verificationType)
                ))
            }
        }
    }

    // ─────────────────────────────────────────────────────────
    // Refresh Developer Status — 刷新单个开发者状态
    // ─────────────────────────────────────────────────────────

    /** Refresh verification status for a single developer / 刷新单个开发者验证状态 */
    private fun refreshDeveloperStatus(developerId: String) {
        viewModelScope.launch {
            try {
                delay(300)
                // Simulate status refresh / 模拟状态刷新
                val updated = _developersFlow.value.map { dev ->
                    if (dev.id == developerId) {
                        // Simulate random status change for demo / 模拟随机状态变化
                        val newStatus = when {
                            Math.random() > 0.5 -> VerificationStatus.Verified
                            else -> dev.verificationStatus
                        }
                        dev.copy(
                            verificationStatus = newStatus,
                            lastVerifiedAt = if (newStatus == VerificationStatus.Verified) System.currentTimeMillis() else dev.lastVerifiedAt
                        )
                    } else dev
                }
                _developersFlow.value = updated
                applyFilters()
                _effect.send(VerificationDashboardEffect.ShowSuccess("状态已刷新 / Status refreshed"))
            } catch (e: Exception) {
                _effect.send(VerificationDashboardEffect.ShowError("刷新失败: ${e.message}"))
            }
        }
    }

    // ─────────────────────────────────────────────────────────
    // Tab Switching — Tab 切换
    // ─────────────────────────────────────────────────────────

    /** Switch active dashboard tab / 切换活跃的仪表板 Tab */
    private fun switchTab(tab: DashboardTab) {
        _state.value = _state.value.copy(activeTab = tab)
    }

    // ─────────────────────────────────────────────────────────
    // CI/CD Config — CI/CD 配置生成
    // ─────────────────────────────────────────────────────────

    /**
     * Generate CI/CD configuration snippet for the selected platform.
     * 为指定平台生成 CI/CD 配置片段。
     *
     * @param platform CI/CD platform / CI/CD 平台
     */
    private fun generateCICDSnippet(platform: CICDPlatform) {
        val snippet = when (platform) {
            CICDPlatform.GitHub_Actions -> """
                |# Android Developer Verification CI/CD Pipeline
                |# PRD-304: Android 开发者身份验证合规批量管理平台
                |name: Android Developer Verification
                |
                |on:
                |  schedule:
                |    - cron: '0 0 * * *'  # Daily at midnight
                |  workflow_dispatch:
                |
                |jobs:
                |  verify-developers:
                |    runs-on: ubuntu-latest
                |    steps:
                |      - name: Check Verification Status
                |        run: |
                |          echo "Checking developer verification status..."
                |          # Integrate with Android Developer ID Status API
                |          # Ref: https://developer.android.com/studio/build/android-developer-id-status-api
                |
                |      - name: Submit Verification
                |        env:
                |          API_KEY: ${'$'}{{ secrets.ANDROID_DEV_API_KEY }}
                |        run: |
                |          curl -X POST https://developer.android.com/api/v1/verify \
                |            -H "Authorization: Bearer ${'$'}API_KEY" \
                |            -H "Content-Type: application/json" \
                |            -d '{"developer_ids": ${'$'}{{ vars.DEVELOPER_IDS }}}'
            """.trimMargin()

            CICDPlatform.GitLab_CI -> """
                |# Android Developer Verification CI/CD Pipeline
                |# PRD-304: Android 开发者身份验证合规批量管理平台
                |android-verification:
                |  stage: compliance
                |  only:
                |    - schedules
                |    - triggers
                |  script:
                |    - echo "Checking developer verification status..."
                |    - |
                |      curl -X POST https://developer.android.com/api/v1/verify \
                |        -H "Authorization: Bearer ${'$'}API_KEY" \
                |        -H "Content-Type: application/json" \
                |        -d '{"developer_ids": "${'$'}{DEVELOPER_IDS}"}'
            """.trimMargin()

            CICDPlatform.Jenkins -> """
                |// Android Developer Verification Jenkins Pipeline
                |// PRD-304: Android 开发者身份验证合规批量管理平台
                |pipeline {
                |    agent any
                |    stages {
                |        stage('Check Verification Status') {
                |            steps {
                |                echo 'Checking developer verification status...'
                |                sh '''
                |                    curl -X POST https://developer.android.com/api/v1/verify \
                |                      -H "Authorization: Bearer ${'$'}{env.API_KEY}" \
                |                      -H "Content-Type: application/json" \
                |                      -d "{\\"developer_ids\\": \\"${'$'}{DEVELOPER_IDS}\\"}"
                |                '''
                |            }
                |        }
                |    }
                |}
            """.trimMargin()
        }
        _state.value = _state.value.copy(
            cicdPlatform = platform,
            generatedConfigSnippet = snippet
        )
    }

    /** Copy generated CI/CD snippet to clipboard / 复制生成的 CI/CD 配置片段 */
    private fun copyCICDSnippet() {
        viewModelScope.launch {
            val snippet = _state.value.generatedConfigSnippet
            if (snippet.isNotEmpty()) {
                _effect.send(VerificationDashboardEffect.CopiedToClipboard(snippet))
                _effect.send(VerificationDashboardEffect.ShowSuccess("配置片段已复制 / Config snippet copied"))
            }
        }
    }

    // ─────────────────────────────────────────────────────────
    // Report Export — 报告导出
    // ─────────────────────────────────────────────────────────

    /**
     * Export compliance report in the specified format.
     * 导出合规报告。
     *
     * @param format PDF or CSV / 报告格式
     * @param dateRange Date range for the report / 报告日期范围
     */
    private fun exportReport(format: ReportFormat, dateRange: DateRange) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            try {
                delay(1000)
                // Simulate export / 模拟导出
                val fileName = "compliance_report_${System.currentTimeMillis()}.${format.name.lowercase()}"
                _state.value = _state.value.copy(isLoading = false)
                _effect.send(VerificationDashboardEffect.ReportExported(format, fileName))
                _effect.send(VerificationDashboardEffect.ShowSuccess("报告已导出: $fileName / Report exported: $fileName"))
            } catch (e: Exception) {
                _state.value = _state.value.copy(isLoading = false)
                _effect.send(VerificationDashboardEffect.ShowError("导出失败: ${e.message}"))
            }
        }
    }

    // ─────────────────────────────────────────────────────────
    // Error Dismissal — 关闭错误提示
    // ─────────────────────────────────────────────────────────

    /** Dismiss current error message / 关闭当前错误提示 */
    private fun dismissError() {
        _state.value = _state.value.copy(errorMessage = null)
    }
}
