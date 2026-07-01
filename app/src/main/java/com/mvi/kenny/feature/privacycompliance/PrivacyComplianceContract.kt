package com.mvi.kenny.feature.privacycompliance

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Warning
import androidx.compose.ui.graphics.vector.ImageVector
import java.time.Instant

/**
 * ============================================================
 * PrivacyComplianceContract — 企业隐私合规审计平台 MVI 契约
 * ============================================================
 * PRD-309 | Android 企业隐私合规审计与合规状态管理平台
 *
 * MVI 三要素：
 * - Model（State）：页面状态的唯一真相来源，Immutable 数据类
 * - View：Composable 函数，消费 State，渲染 UI
 * - Intent：用户意图（用户操作），ViewModel 收到 Intent 后执行业务逻辑
 *
 * Effect：一次性副作用（导航、Toast、报告生成完成），通过 Channel 传递
 * —————————————————————————————————————————————————————
 */

/**
 * 合规状态枚举
 * —————————————————————————————————————————————————————
 * 用于表示单个合规检查项的状态
 *
 * @param icon 状态对应的图标
 * @param label 状态标签文本
 */
enum class ComplianceStatus(
    val icon: ImageVector,
    val label: String  // English label for iconAlt, Chinese for display
) {
    COMPLIANT(Icons.Default.CheckCircle, "已合规"),       // 已合规
    PENDING(Icons.Default.Warning, "待修复"),              // 待修复
    NON_COMPLIANT(Icons.Default.Error, "未合规");          // 未合规
}

/**
 * Android API 版本枚举
 * —————————————————————————————————————————————————————
 * 用于分组展示各 API 级别的合规要求
 */
enum class AndroidApiLevel(val level: Int, val displayName: String) {
    API_34(34, "Android 14 (API 34)"),
    API_35(35, "Android 15 (API 35)"),
    API_36(36, "Android 16 (API 36)"),
    API_37(37, "Android 17 (API 37)");
}

/**
 * 合规检查项
 * —————————————————————————————————————————————————————
 * 表示一条具体的合规检查规则及其当前状态
 *
 * @param id 唯一标识
 * @param title 检查项标题
 * @param description 检查项描述
 * @param apiLevel 所属 API 级别
 * @param playStorePolicy 关联的 Play Store 政策条目
 * @param status 当前合规状态
 * @param affectedApps 受影响的 App 数量
 * @param riskScore 风险评分（0-100）
 * @param codeFilePaths 相关代码文件路径（用于跳转）
 */
data class ComplianceCheckItem(
    val id: String,
    val title: String,
    val description: String,
    val apiLevel: AndroidApiLevel,
    val playStorePolicy: String,
    val status: ComplianceStatus,
    val affectedApps: Int = 0,
    val riskScore: Int = 0,  // 0-100
    val codeFilePaths: List<String> = emptyList()
)

/**
 * App 合规摘要
 * —————————————————————————————————————————————————————
 * 仪表板中每个 App 卡片的摘要信息
 *
 * @param appId App 唯一标识
 * @param appName App 显示名称
 * @param overallStatus 整体合规状态
 * @param riskScore 综合风险评分（0-100）
 * @param unfixedCount 未修复的合规项数量
 * @param lastUpdated 最后更新时间
 */
data class AppComplianceSummary(
    val appId: String,
    val appName: String,
    val overallStatus: ComplianceStatus,
    val riskScore: Int,  // 0-100
    val unfixedCount: Int,
    val lastUpdated: Instant
)

/**
 * App 合规详情
 * —————————————————————————————————————————————————————
 * App 详情页的完整数据，包含合规清单和风险量化
 *
 * @param appId App 唯一标识
 * @param appName App 显示名称
 * @param checkItems 所有合规检查项（按 API 级别分组）
 * @param riskScores 各维度风险评分
 * @param ciCdConfig CI/CD 配置信息
 * @param reports 历史报告列表
 */
data class AppComplianceDetail(
    val appId: String,
    val appName: String,
    val checkItems: Map<AndroidApiLevel, List<ComplianceCheckItem>> = emptyMap(),
    val riskScores: RiskQuantification = RiskQuantification(),
    val ciCdConfig: CICDConfig? = null,
    val reports: List<ComplianceReport> = emptyList()
)

/**
 * 风险量化数据
 * —————————————————————————————————————————————————————
 * 多维度风险评分，用于雷达图展示
 *
 * @param auditRisk 审核风险评分（0-100）
 * @param delistRisk 下架风险评分（0-100）
 * @param dataLeakRisk 数据泄露风险评分（0-100）
 * @param complianceGapRisk 合规缺口风险评分（0-100）
 * @param userImpactRisk 用户影响风险评分（0-100）
 */
data class RiskQuantification(
    val auditRisk: Int = 0,
    val delistRisk: Int = 0,
    val dataLeakRisk: Int = 0,
    val complianceGapRisk: Int = 0,
    val userImpactRisk: Int = 0
) {
    /** 计算综合风险评分（三维加权平均） */
    fun overallRiskScore(): Int {
        return ((auditRisk * 0.3 + delistRisk * 0.3 + dataLeakRisk * 0.4) * 100).toInt().coerceIn(0, 100)
    }
}

/**
 * CI/CD 插件配置
 * —————————————————————————————————————————————————————
 * 存储 App 的 CI/CD 合规卡点配置
 *
 * @param pluginId 插件 ID（github_actions / gitlab_ci）
 * @param enabled 是否启用
 * @param ymlSnippet YML 配置代码片段
 * @param lastTestRun 最后测试运行时间
 * @param lastTestResult 最后测试结果（null = 未测试）
 */
data class CICDConfig(
    val pluginId: String,
    val enabled: Boolean = false,
    val ymlSnippet: String = "",
    val lastTestRun: Instant? = null,
    val lastTestResult: TestResult? = null
)

/**
 * 测试结果
 * —————————————————————————————————————————————————————
 * @param passed 通过的检查项数量
 * @param failed 失败的检查项数量
 * @param warnings 警告数量
 */
data class TestResult(
    val passed: Int,
    val failed: Int,
    val warnings: Int
)

/**
 * 合规报告
 * —————————————————————————————————————————————————————
 * 生成的合规审计报告摘要
 *
 * @param id 报告 ID
 * @param title 报告标题
 * @param template 报告模板（GDPR / 金融 / 医疗）
 * @param generatedAt 生成时间
 * @param downloadUrl 下载链接（null = 尚未生成）
 */
data class ComplianceReport(
    val id: String,
    val title: String,
    val template: ReportTemplate,
    val generatedAt: Instant,
    val downloadUrl: String? = null
)

/**
 * 报告模板枚举
 */
enum class ReportTemplate(val displayName: String) {
    GDPR("GDPR 通用数据保护条例"),
    FINANCIAL("金融行业合规报告"),
    MEDICAL("医疗行业合规报告 (HIPAA)"),
    GENERAL("通用合规报告");
}

/**
 * 风险告警
 * —————————————————————————————————————————————————————
 * 高风险项告警，用于仪表板横幅展示
 *
 * @param id 告警 ID
 * @param title 告警标题
 * @param description 告警描述
 * @param severity 严重程度（WARNING / CRITICAL）
 * @param appId 关联的 App ID
 * @param affectedCheckItemIds 受影响的检查项 ID 列表
 */
data class RiskAlert(
    val id: String,
    val title: String,
    val description: String,
    val severity: AlertSeverity,
    val appId: String,
    val affectedCheckItemIds: List<String> = emptyList()
)

enum class AlertSeverity { WARNING, CRITICAL }

/**
 * CI/CD 插件信息
 * —————————————————————————————————————————————————————
 * 插件市场的插件条目
 *
 * @param id 插件 ID
 * @param name 插件名称
 * @param description 插件描述
 * @param supportedPlatforms 支持的平台（GitHub Actions / GitLab CI）
 * @param isInstalled 是否已安装
 */
data class CICDPlugin(
    val id: String,
    val name: String,
    val description: String,
    val supportedPlatforms: List<String>,
    val isInstalled: Boolean = false
)

/**
 * App 详情页 Tab 枚举
 * —————————————————————————————————————————————————————
 */
enum class AppDetailTab {
    Checklist,           // 合规清单
    RiskQuantification,   // 风险量化
    CICD,                // CI/CD 配置
    Reports               // 历史报告
}

// =============================================================================
// State / Intent / Effect
// =============================================================================

/**
 * 页面状态（State）
 * —————————————————————————————————————————————————————
 *
 * @param apps 所有 App 的合规摘要列表
 * @param selectedApp 当前选中的 App 详情（null = 未选中，显示仪表板）
 * @param selectedTab 当前详情页 Tab
 * @param isLoading 是否正在加载
 * @param lastRefreshed 最后刷新时间
 * @param isGeneratingReport 是否正在生成报告
 * @param reportJobId 当前报告生成任务 ID（null = 无进行中任务）
 * @param ciCdPlugins 可用的 CI/CD 插件列表
 * @param alerts 风险告警列表
 * @param selectedReportTemplate 当前选择的报告模板
 */
data class PrivacyComplianceState(
    val apps: List<AppComplianceSummary> = emptyList(),
    val selectedApp: AppComplianceDetail? = null,
    val selectedTab: AppDetailTab = AppDetailTab.Checklist,
    val isLoading: Boolean = false,
    val lastRefreshed: Instant? = null,
    val isGeneratingReport: Boolean = false,
    val reportJobId: String? = null,
    val ciCdPlugins: List<CICDPlugin> = emptyList(),
    val alerts: List<RiskAlert> = emptyList(),
    val selectedReportTemplate: ReportTemplate = ReportTemplate.GENERAL
) {
    companion object {
        /** 初始状态 */
        val Initial = PrivacyComplianceState()
    }
}

/**
 * 用户意图（User Intent）
 * —————————————————————————————————————————————————————
 * 页面上的每一个用户操作都对应一个 Intent。
 * ViewModel 收到 Intent 后执行业务逻辑，然后更新 State。
 *
 * @see PrivacyComplianceViewModel.sendIntent 处理所有 Intent
 */
sealed interface PrivacyComplianceIntent {
    /** 刷新仪表板（初始加载 + 下拉刷新） */
    data object RefreshDashboard : PrivacyComplianceIntent

    /** 选中某个 App，进入详情页
     * @param appId 要查看的 App ID
     */
    data class SelectApp(val appId: String) : PrivacyComplianceIntent

    /** 返回仪表板（取消选中 App） */
    data object BackToDashboard : PrivacyComplianceIntent

    /** 切换 App 详情页 Tab
     * @param tab 要切换到的 Tab
     */
    data class SwitchTab(val tab: AppDetailTab) : PrivacyComplianceIntent

    /** 开始生成合规报告
     * @param template 报告模板类型
     */
    data class StartReportGeneration(val template: ReportTemplate) : PrivacyComplianceIntent

    /** 复制 CI/CD 配置代码片段
     * @param pluginId 插件 ID
     */
    data class CopyCIConfig(val pluginId: String) : PrivacyComplianceIntent

    /** 设置合规基线（最低要求 API 级别）
     * @param appIds 要设置的 App ID 列表
     * @param minApiLevel 最低 API 级别
     */
    data class SetComplianceBaseline(val appIds: List<String>, val minApiLevel: Int) : PrivacyComplianceIntent

    /** 刷新选中 App 的详情
     */
    data object RefreshSelectedApp : PrivacyComplianceIntent

    /** 切换报告模板选择
     * @param template 报告模板
     */
    data class SelectReportTemplate(val template: ReportTemplate) : PrivacyComplianceIntent
}

/**
 * 副作用（Effect）
 * —————————————————————————————————————————————————————
 * 一次性事件，不可变，只能被消费一次。
 * UI 层通过 LaunchedEffect + flow.collect{} 监听并处理。
 *
 * @see PrivacyComplianceViewModel 中通过 _effect.send() 发送 Effect
 */
sealed interface PrivacyComplianceEffect {
    /** 复制成功提示
     * @param message 成功消息
     */
    data class ShowCopySuccess(val message: String) : PrivacyComplianceEffect

    /** 报告生成完成，可下载
     * @param downloadUrl 报告下载链接
     */
    data class ReportReady(val downloadUrl: String) : PrivacyComplianceEffect

    /** 显示错误提示
     * @param message 错误描述
     */
    data class ShowError(val message: String) : PrivacyComplianceEffect

    /** CI/CD 测试运行完成 */
    data object CICDTestRunComplete : PrivacyComplianceEffect
}
