package com.mvi.kenny.feature.privacycompliance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.time.Instant
import java.util.UUID

/**
 * ============================================================
 * PrivacyComplianceViewModel — 企业隐私合规审计平台状态管理
 * ============================================================
 * PRD-309 | Android 企业隐私合规审计与合规状态管理平台
 *
 * 继承 ViewModel，持有 PrivacyComplianceState（页面状态）和 PrivacyComplianceEffect（副作用）。
 *
 * 状态管理：
 * - _state：私有 MutableStateFlow，ViewModel 内部写入
 * - state：公开 StateFlow，供 UI 层订阅（collectAsState）
 *
 * 副作用管理：
 * - _effect：Channel（热流），缓冲区大小 BUFFERED
 * - effect：receiveAsFlow，UI 层通过 collect{} 监听
 *
 * 为什么用 Channel 而不是 StateFlow？
 * —————————————————————————————————————————————————————
 * StateFlow 会记住当前值，新订阅者会收到上一次的值。
 * Channel 只传递新事件，适合"一次性"事件（导航、Toast、报告下载完成）。
 *
 * @see PrivacyComplianceState 页面状态定义
 * @see PrivacyComplianceIntent 用户意图
 * @see PrivacyComplianceEffect 副作用
 */
class PrivacyComplianceViewModel : ViewModel() {

    /** 页面状态（StateFlow，UI 只读） */
    private val _state = MutableStateFlow(PrivacyComplianceState.Initial)
    val state: StateFlow<PrivacyComplianceState> = _state.asStateFlow()

    /**
     * 当前状态的快照
     * 用于 Compose 中 lambda 表达式内部访问状态
     */
    val currentState: PrivacyComplianceState get() = _state.value

    /**
     * 副作用 Channel
     * @see PrivacyComplianceEffect
     */
    private val _effect = Channel<PrivacyComplianceEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    init {
        // ViewModel 创建时自动加载仪表板数据
        sendIntent(PrivacyComplianceIntent.RefreshDashboard)
    }

    /**
     * 接收并处理用户意图
     * —————————————————————————————————————————————————————
     * 入口方法，UI 层通过 viewModel.sendIntent(intent) 调用。
     * 根据 intent 类型分发到对应的处理函数。
     *
     * @param intent 用户意图（非空）
     */
    fun sendIntent(intent: PrivacyComplianceIntent) {
        when (intent) {
            is PrivacyComplianceIntent.RefreshDashboard -> refreshDashboard()
            is PrivacyComplianceIntent.SelectApp -> selectApp(intent.appId)
            is PrivacyComplianceIntent.BackToDashboard -> backToDashboard()
            is PrivacyComplianceIntent.SwitchTab -> switchTab(intent.tab)
            is PrivacyComplianceIntent.StartReportGeneration -> startReportGeneration(intent.template)
            is PrivacyComplianceIntent.CopyCIConfig -> copyCIConfig(intent.pluginId)
            is PrivacyComplianceIntent.SetComplianceBaseline -> setComplianceBaseline(intent.appIds, intent.minApiLevel)
            is PrivacyComplianceIntent.RefreshSelectedApp -> refreshSelectedApp()
            is PrivacyComplianceIntent.SelectReportTemplate -> selectReportTemplate(intent.template)
        }
    }

    // ==========================================================================
    // Intent Handlers
    // ==========================================================================

    /**
     * 刷新仪表板
     * —————————————————————————————————————————————————————
     * 模拟网络请求获取所有 App 的合规摘要。
     * 包含：App 列表、风险告警、可用 CI/CD 插件。
     */
    private fun refreshDashboard() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            try {
                // 模拟网络请求延迟
                delay(800)

                // 模拟 App 数据（实际场景中从企业后端 API 获取）
                val apps = generateMockAppSummaries()
                val alerts = generateMockAlerts(apps)
                val plugins = generateMockPlugins()

                _state.value = _state.value.copy(
                    isLoading = false,
                    apps = apps,
                    alerts = alerts,
                    ciCdPlugins = plugins,
                    lastRefreshed = Instant.now()
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(isLoading = false)
                _effect.send(PrivacyComplianceEffect.ShowError("刷新失败: ${e.message}"))
            }
        }
    }

    /**
     * 选中 App，进入详情页
     * —————————————————————————————————————————————————————
     *
     * @param appId 要查看的 App ID
     */
    private fun selectApp(appId: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            try {
                delay(500)

                // 模拟获取 App 详情
                val detail = generateMockAppDetail(appId)
                _state.value = _state.value.copy(
                    isLoading = false,
                    selectedApp = detail,
                    selectedTab = AppDetailTab.Checklist
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(isLoading = false)
                _effect.send(PrivacyComplianceEffect.ShowError("加载 App 详情失败: ${e.message}"))
            }
        }
    }

    /**
     * 返回仪表板
     * —————————————————————————————————————————————————————
     */
    private fun backToDashboard() {
        _state.value = _state.value.copy(selectedApp = null, selectedTab = AppDetailTab.Checklist)
    }

    /**
     * 切换 App 详情页 Tab
     *
     * @param tab 要切换到的 Tab
     */
    private fun switchTab(tab: AppDetailTab) {
        _state.value = _state.value.copy(selectedTab = tab)
    }

    /**
     * 开始生成合规报告
     * —————————————————————————————————————————————————————
     * 模拟后台异步生成报告，完成后发送 ReportReady Effect。
     *
     * @param template 报告模板类型
     */
    private fun startReportGeneration(template: ReportTemplate) {
        viewModelScope.launch {
            val jobId = UUID.randomUUID().toString()
            _state.value = _state.value.copy(
                isGeneratingReport = true,
                reportJobId = jobId,
                selectedReportTemplate = template
            )
            try {
                // 模拟报告生成延迟（3-5秒）
                delay(3500)

                val downloadUrl = "https://compliance.internal/reports/$jobId.pdf"
                _state.value = _state.value.copy(
                    isGeneratingReport = false,
                    reportJobId = null
                )
                _effect.send(PrivacyComplianceEffect.ReportReady(downloadUrl))
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isGeneratingReport = false,
                    reportJobId = null
                )
                _effect.send(PrivacyComplianceEffect.ShowError("报告生成失败: ${e.message}"))
            }
        }
    }

    /**
     * 复制 CI/CD 配置代码片段
     * —————————————————————————————————————————————————————
     * 模拟将 YML 配置复制到剪贴板（实际使用 ClipboardManager）。
     * 复制成功后发送 ShowCopySuccess Effect。
     *
     * @param pluginId 插件 ID
     */
    private fun copyCIConfig(pluginId: String) {
        viewModelScope.launch {
            try {
                // 模拟获取插件配置片段
                val snippet = getPluginYmlSnippet(pluginId)

                // 实际场景中这里会调用 ClipboardManager 复制到系统剪贴板
                // clipboardManager.setPrimaryClip(ClipData.newPlainText("CI Config", snippet))

                _effect.send(PrivacyComplianceEffect.ShowCopySuccess("CI/CD 配置已复制到剪贴板"))
            } catch (e: Exception) {
                _effect.send(PrivacyComplianceEffect.ShowError("复制失败: ${e.message}"))
            }
        }
    }

    /**
     * 设置合规基线
     * —————————————————————————————————————————————————————
     * 为选中的 App 设置最低要求 API 级别。
     * 实际场景中会更新后端配置并触发重新扫描。
     *
     * @param appIds App ID 列表
     * @param minApiLevel 最低 API 级别
     */
    private fun setComplianceBaseline(appIds: List<String>, minApiLevel: Int) {
        viewModelScope.launch {
            try {
                // 模拟 API 调用延迟
                delay(300)

                // 实际场景中调用后端 API 更新合规基线配置
                // api.updateComplianceBaseline(appIds, minApiLevel)

                _effect.send(PrivacyComplianceEffect.ShowCopySuccess("合规基线已更新（API $minApiLevel+）"))
            } catch (e: Exception) {
                _effect.send(PrivacyComplianceEffect.ShowError("更新失败: ${e.message}"))
            }
        }
    }

    /**
     * 刷新选中 App 的详情
     * —————————————————————————————————————————————————————
     */
    private fun refreshSelectedApp() {
        val appId = _state.value.selectedApp?.appId ?: return
        selectApp(appId)
    }

    /**
     * 选择报告模板
     *
     * @param template 报告模板
     */
    private fun selectReportTemplate(template: ReportTemplate) {
        _state.value = _state.value.copy(selectedReportTemplate = template)
    }

    // ==========================================================================
    // Mock Data Generators
    // ==========================================================================

    /**
     * 生成模拟 App 合规摘要列表
     * 实际场景中替换为真实的企业后端 API 调用
     */
    private fun generateMockAppSummaries(): List<AppComplianceSummary> {
        return listOf(
            AppComplianceSummary(
                appId = "app_001",
                appName = "企业内部通讯",
                overallStatus = ComplianceStatus.PENDING,
                riskScore = 58,
                unfixedCount = 3,
                lastUpdated = Instant.now().minusSeconds(3600)
            ),
            AppComplianceSummary(
                appId = "app_002",
                appName = "CRM 移动端",
                overallStatus = ComplianceStatus.COMPLIANT,
                riskScore = 12,
                unfixedCount = 0,
                lastUpdated = Instant.now().minusSeconds(7200)
            ),
            AppComplianceSummary(
                appId = "app_003",
                appName = "财务报销",
                overallStatus = ComplianceStatus.NON_COMPLIANT,
                riskScore = 87,
                unfixedCount = 7,
                lastUpdated = Instant.now().minusSeconds(1800)
            ),
            AppComplianceSummary(
                appId = "app_004",
                appName = "员工健康打卡",
                overallStatus = ComplianceStatus.PENDING,
                riskScore = 45,
                unfixedCount = 2,
                lastUpdated = Instant.now().minusSeconds(5400)
            ),
            AppComplianceSummary(
                appId = "app_005",
                appName = "供应链管理",
                overallStatus = ComplianceStatus.COMPLIANT,
                riskScore = 8,
                unfixedCount = 0,
                lastUpdated = Instant.now().minusSeconds(10800)
            ),
            AppComplianceSummary(
                appId = "app_006",
                appName = "会议预约",
                overallStatus = ComplianceStatus.NON_COMPLIANT,
                riskScore = 73,
                unfixedCount = 5,
                lastUpdated = Instant.now().minusSeconds(900)
            )
        )
    }

    /**
     * 根据 App 列表生成风险告警
     */
    private fun generateMockAlerts(apps: List<AppComplianceSummary>): List<RiskAlert> {
        val alerts = mutableListOf<RiskAlert>()

        apps.filter { it.overallStatus == ComplianceStatus.NON_COMPLIANT }.forEach { app ->
            alerts.add(
                RiskAlert(
                    id = "alert_${app.appId}",
                    title = "${app.appName} 合规风险",
                    description = "存在 ${app.unfixedCount} 项未合规，存在下架风险",
                    severity = AlertSeverity.CRITICAL,
                    appId = app.appId
                )
            )
        }

        apps.filter { it.overallStatus == ComplianceStatus.PENDING && it.riskScore > 50 }.forEach { app ->
            alerts.add(
                RiskAlert(
                    id = "alert_pending_${app.appId}",
                    title = "${app.appName} 待修复",
                    description = "存在 ${app.unfixedCount} 项待修复，建议尽快处理",
                    severity = AlertSeverity.WARNING,
                    appId = app.appId
                )
            )
        }

        return alerts
    }

    /**
     * 生成模拟 CI/CD 插件列表
     */
    private fun generateMockPlugins(): List<CICDPlugin> {
        return listOf(
            CICDPlugin(
                id = "github_actions",
                name = "GitHub Actions",
                description = "GitHub CI/CD 流水线合规卡点插件，支持 PR 上下文检测",
                supportedPlatforms = listOf("GitHub Actions")
            ),
            CICDPlugin(
                id = "gitlab_ci",
                name = "GitLab CI",
                description = "GitLab CI/CD 流水线合规卡点插件，支持 .gitlab-ci.yml 模板注入",
                supportedPlatforms = listOf("GitLab CI")
            ),
            CICDPlugin(
                id = "jenkins",
                name = "Jenkins",
                description = "Jenkins 流水线合规卡点插件，支持通用 Pipeline 脚本",
                supportedPlatforms = listOf("Jenkins")
            )
        )
    }

    /**
     * 根据 App ID 生成模拟 App 详情
     */
    private fun generateMockAppDetail(appId: String): AppComplianceDetail {
        val appName = when (appId) {
            "app_001" -> "企业内部通讯"
            "app_002" -> "CRM 移动端"
            "app_003" -> "财务报销"
            "app_004" -> "员工健康打卡"
            "app_005" -> "供应链管理"
            "app_006" -> "会议预约"
            else -> "未知应用"
        }

        // 生成各 API 级别的合规检查项
        val checkItems = mapOf(
            AndroidApiLevel.API_34 to listOf(
                ComplianceCheckItem(
                    id = "${appId}_photo_picker_34",
                    title = "Photo Picker API",
                    description = "检测是否使用 Photo Picker 替代直接媒体权限",
                    apiLevel = AndroidApiLevel.API_34,
                    playStorePolicy = "Android 14 Photo Picker Requirement",
                    status = if (appId == "app_002" || appId == "app_005") ComplianceStatus.COMPLIANT else ComplianceStatus.PENDING,
                    affectedApps = 1,
                    riskScore = 65,
                    codeFilePaths = listOf("app/src/main/java/.../MediaPicker.kt")
                ),
                ComplianceCheckItem(
                    id = "${appId}_foreground_service_34",
                    title = "Foreground Service Type",
                    description = "检测 foreground service 是否声明正确的服务类型",
                    apiLevel = AndroidApiLevel.API_34,
                    playStorePolicy = "Android 14 Foreground Service Requirements",
                    status = if (appId == "app_005") ComplianceStatus.COMPLIANT else ComplianceStatus.COMPLIANT,
                    affectedApps = 0,
                    riskScore = 20
                )
            ),
            AndroidApiLevel.API_36 to listOf(
                ComplianceCheckItem(
                    id = "${appId}_local_network_36",
                    title = "Local Network Permission",
                    description = "检测扫描本地网络时是否声明 INTERACT_ACROSS_USERS_FULL",
                    apiLevel = AndroidApiLevel.API_36,
                    playStorePolicy = "Android 16 Local Network Access",
                    status = if (appId == "app_003" || appId == "app_006") ComplianceStatus.NON_COMPLIANT else ComplianceStatus.PENDING,
                    affectedApps = if (appId == "app_003") 1 else 0,
                    riskScore = 85,
                    codeFilePaths = listOf("app/src/main/java/.../NetworkScanner.kt")
                ),
                ComplianceCheckItem(
                    id = "${appId}_health_connect_36",
                    title = "Health Connect Migration",
                    description = "检测健康数据读取是否迁移至 Health Connect API",
                    apiLevel = AndroidApiLevel.API_36,
                    playStorePolicy = "Android 16 Health Data Access",
                    status = if (appId == "app_004") ComplianceStatus.PENDING else ComplianceStatus.COMPLIANT,
                    affectedApps = if (appId == "app_004") 1 else 0,
                    riskScore = 70
                )
            ),
            AndroidApiLevel.API_37 to listOf(
                ComplianceCheckItem(
                    id = "${appId}_resizability_37",
                    title = "Resizeability & Multi-Window",
                    description = "检测是否支持多窗口和任意屏幕比例（API 37 强制）",
                    apiLevel = AndroidApiLevel.API_37,
                    playStorePolicy = "Android 17 Resizability Requirement",
                    status = if (appId == "app_002" || appId == "app_005") ComplianceStatus.COMPLIANT else ComplianceStatus.NON_COMPLIANT,
                    affectedApps = if (appId != "app_002" && appId != "app_005") 1 else 0,
                    riskScore = 92,
                    codeFilePaths = listOf("app/src/main/AndroidManifest.xml")
                ),
                ComplianceCheckItem(
                    id = "${appId}_static_reflection_37",
                    title = "Static Final Reflection",
                    description = "检测是否使用反射访问静态 final 字段（ART 优化后禁用）",
                    apiLevel = AndroidApiLevel.API_37,
                    playStorePolicy = "Android 17 ART Optimization",
                    status = if (appId == "app_003") ComplianceStatus.NON_COMPLIANT else ComplianceStatus.COMPLIANT,
                    affectedApps = if (appId == "app_003") 1 else 0,
                    riskScore = 88,
                    codeFilePaths = listOf("app/src/main/java/.../RetrofitFactory.kt", "app/src/main/java/.../MockKSetup.kt")
                ),
                ComplianceCheckItem(
                    id = "${appId}_background_audio_37",
                    title = "Background Audio Hardening",
                    description = "检测后台音频 API 调用是否使用 WIU Foreground Service",
                    apiLevel = AndroidApiLevel.API_37,
                    playStorePolicy = "Android 17 Background Audio Policy",
                    status = if (appId == "app_006") ComplianceStatus.NON_COMPLIANT else ComplianceStatus.PENDING,
                    affectedApps = if (appId == "app_006") 1 else 0,
                    riskScore = 75
                )
            )
        )

        // 生成风险量化数据
        val riskScores = RiskQuantification(
            auditRisk = (30..90).random(),
            delistRisk = (20..85).random(),
            dataLeakRisk = (40..80).random(),
            complianceGapRisk = (50..95).random(),
            userImpactRisk = (30..70).random()
        )

        // 生成 CI/CD 配置
        val cicdConfig = CICDConfig(
            pluginId = "github_actions",
            enabled = appId != "app_003",
            ymlSnippet = getPluginYmlSnippet("github_actions"),
            lastTestRun = if (appId != "app_003") Instant.now().minusSeconds(86400) else null,
            lastTestResult = if (appId != "app_003") TestResult(passed = 12, failed = 2, warnings = 1) else null
        )

        // 生成历史报告
        val reports = listOf(
            ComplianceReport(
                id = "report_${appId}_001",
                title = "${appName} - Q2 2026 合规报告",
                template = ReportTemplate.GENERAL,
                generatedAt = Instant.now().minusSeconds(86400 * 30),
                downloadUrl = "https://compliance.internal/reports/report_${appId}_001.pdf"
            ),
            ComplianceReport(
                id = "report_${appId}_002",
                title = "${appName} - GDPR 合规审计",
                template = ReportTemplate.GDPR,
                generatedAt = Instant.now().minusSeconds(86400 * 7),
                downloadUrl = "https://compliance.internal/reports/report_${appId}_002.pdf"
            )
        )

        return AppComplianceDetail(
            appId = appId,
            appName = appName,
            checkItems = checkItems,
            riskScores = riskScores,
            ciCdConfig = cicdConfig,
            reports = reports
        )
    }

    /**
     * 根据插件 ID 获取 YML 配置片段
     * 实际场景中从插件市场 API 或本地模板获取
     */
    private fun getPluginYmlSnippet(pluginId: String): String {
        return when (pluginId) {
            "github_actions" -> """
                # Android Privacy Compliance Check
                # Add this to your .github/workflows/android.yml
                - name: Run Privacy Compliance Check
                  uses: enterprise/android-privacy-check@v2
                  with:
                    api_levels: '34,35,36,37'
                    fail_on_warn: true
                    report_format: 'json'
            """.trimIndent()

            "gitlab_ci" -> """
                # Android Privacy Compliance Check
                # Add this to your .gitlab-ci.yml
                android_privacy_check:
                  stage: test
                  image: enterprise/android-privacy-check:latest
                  script:
                    - android-privacy-check --api-levels 34,35,36,37 --fail-on-warn
                  artifacts:
                    reports:
                      json: compliance-report.json
            """.trimIndent()

            "jenkins" -> """
                // Android Privacy Compliance Check
                // Add this to your Jenkinsfile
                stage('Privacy Compliance') {
                    steps {
                        sh '''
                            docker run --rm enterprise/android-privacy-check:latest \
                                --api-levels 34,35,36,37 \
                                --fail-on-warn \
                                --report-format json \
                                --output compliance-report.json
                        '''
                    }
                    post {
                        always {
                            archiveArtifacts artifacts: 'compliance-report.json'
                        }
                    }
                }
            """.trimIndent()

            else -> "# Unknown plugin: $pluginId"
        }
    }
}
