package com.mvi.kenny.feature.aapm

import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow

// =============================================================
// AAPMContract — Android 17 AdvancedProtectionManager API
// 合规检测与安全响应工具包 MVI 契约
// =============================================================
// MVI Architecture Pattern / MVI 架构模式
//
// - Model (State): Immutable data class, single source of truth for UI
// - View: Composable functions that consume State and render UI
// - Intent: User intentions, ViewModel processes and updates State
// - Effect: One-time side effects (navigation, toast, etc.)

// =============================================================
// AAPMState — AAPM 状态
// =============================================================
/**
 * AAPM State / AAPM 状态
 *
 * Represents the current state of Android 17 Advanced Protection Mode.
 *
 * NotSupported: Android < 17.2, AdvancedProtectionManager API not available
 * Disabled: AAPM is disabled, reason may be provided
 * Enabled: AAPM is active, isUserEnrolled indicates if user enrolled
 */
sealed class AAPMState {
    /** Android < 17.2, not supported / Android < 17.2，不支持 */
    data object NotSupported : AAPMState()

    /** AAPM is disabled / AAPM 已关闭
     * @param reason Reason why AAPM is disabled / AAPM 关闭的原因
     */
    data class Disabled(val reason: String?) : AAPMState()

    /** AAPM is enabled / AAPM 已开启
     * @param isUserEnrolled Whether user has enrolled in AAPM / 用户是否注册了 AAPM
     * @param affectedServices List of affected Accessibility Services / 受影响的 Accessibility Services 列表
     */
    data class Enabled(
        val isUserEnrolled: Boolean,
        val affectedServices: List<String>
    ) : AAPMState()
}

// =============================================================
// RiskLevel — 风险等级
// =============================================================
/**
 * Accessibility Service risk level / Accessibility Service 风险等级
 *
 * @param label Display label / 显示标签
 * @param color Badge color / 徽章颜色
 */
enum class RiskLevel(val label: String, val color: Color) {
    HIGH("高风险 / HIGH", Color(0xFFFF5252)),
    MEDIUM("中风险 / MEDIUM", Color(0xFFFFA726)),
    LOW("低风险 / LOW", Color(0xFF66BB6A))
}

// =============================================================
// ResponseStrategy — 响应策略
// =============================================================
/**
 * Recommended response strategy / 推荐的响应策略
 *
 * @param label Display label / 显示标签
 * @param description Strategy description / 策略描述
 */
enum class ResponseStrategy(val label: String, val description: String) {
    DISABLE("禁用 / Disable", "Completely disable the Accessibility Service / 完全禁用 Accessibility Service"),
    GRACEFUL_DEGRADE("优雅降级 / Graceful Degrade", "Provide alternative functionality without Accessibility API / 在不使用 Accessibility API 的情况下提供替代功能"),
    USER_GUIDANCE("用户引导 / User Guidance", "Help user understand the limitation and provide instructions / 帮助用户理解限制并提供操作指引"),
    NONE("无需操作 / None", "No action required / 无需任何操作")
}

// =============================================================
// AccessibilityServiceInfo — Accessibility Service 信息
// =============================================================
/**
 * Accessibility Service information / Accessibility Service 信息
 *
 * @param name Service name / 服务名称
 * @param className Fully qualified class name / 完全限定类名
 * @param riskLevel AAPM risk level / AAPM 风险等级
 * @param recommendedResponse Recommended response strategy / 推荐的响应策略
 * @param isEnabled Whether the service is currently enabled / 服务是否当前可用
 * @param description Service description / 服务描述
 */
data class AccessibilityServiceInfo(
    val name: String,
    val className: String,
    val riskLevel: RiskLevel,
    val recommendedResponse: ResponseStrategy,
    val isEnabled: Boolean,
    val description: String = ""
)

// =============================================================
// AuditLogEntry — 审计日志条目
// =============================================================
/**
 * Accessibility API audit log entry / Accessibility API 审计日志条目
 *
 * @param id Unique identifier / 唯一标识符
 * @param timestamp Timestamp of the access / 访问时间戳
 * @param targetApp Target app being accessed / 被访问的目标 App
 * @param operationType Type of operation performed / 执行的操怍类型
 * @param serviceName Service that performed the access / 执行访问的服务名称
 * @param isCompliant Whether this access is AAPM compliant / 此访问是否合规
 */
data class AuditLogEntry(
    val id: String,
    val timestamp: Long,
    val targetApp: String,
    val operationType: String,
    val serviceName: String,
    val isCompliant: Boolean
)

// =============================================================
// ComplianceReport — 合规报告
// =============================================================
/**
 * Compliance report / 合规报告
 *
 * @param isCompliant Overall compliance status / 总体合规状态
 * @param totalServices Total number of Accessibility Services / Accessibility Services 总数
 * @param compliantServices Number of compliant services / 合规服务数量
 * @param nonCompliantServices Number of non-compliant services / 不合规服务数量
 * @param issues List of compliance issues / 合规问题列表
 * @param generatedAt Report generation timestamp / 报告生成时间戳
 */
data class ComplianceReport(
    val isCompliant: Boolean,
    val totalServices: Int,
    val compliantServices: Int,
    val nonCompliantServices: Int,
    val issues: List<ComplianceIssue>,
    val generatedAt: Long
)

// =============================================================
// ComplianceIssue — 合规问题
// =============================================================
/**
 * Compliance issue / 合规问题
 *
 * @param serviceName Affected service name / 受影响的服务名称
 * @param description Issue description / 问题描述
 * @param severity Issue severity / 问题严重程度
 * @param suggestion Fix suggestion / 修复建议
 */
data class ComplianceIssue(
    val serviceName: String,
    val description: String,
    val severity: RiskLevel,
    val suggestion: String
)

// =============================================================
// AAPMTab — AAPM 工具 Tab 枚举
// =============================================================
/**
 * AAPM Dashboard Tab / AAPM 工具面板 Tab
 *
 * @param title Display title / 显示标题
 * @param iconName Icon name / 图标名称
 */
enum class AAPMTab(val title: String, val iconName: String) {
    STATUS("状态 / Status", "shield"),
    IMPACT("影响 / Impact", "analytics"),
    GUIDE("指南 / Guide", "menu_book"),
    AUDIT("审计 / Audit", "fact_check"),
    REPORT("报告 / Report", "description")
}

// =============================================================
// AAPMDashboardState — 页面状态
// =============================================================
/**
 * AAPM Dashboard State / AAPM 工具面板状态
 *
 * Single source of truth for the entire AAPM Dashboard UI.
 * All UI state is derived from this data class.
 *
 * @param activeTab Currently active tool tab / 当前活跃的工具 Tab
 * @param aapmState Current AAPM state / 当前 AAPM 状态
 * @param isAccessibilityTool Whether this app is marked as accessibility tool / 此 App 是否标记为辅助功能工具
 * @param services List of Accessibility Services / Accessibility Services 列表
 * @param auditLogs List of audit log entries / 审计日志条目列表
 * @param complianceReport Generated compliance report / 生成的合规报告
 * @param isScanning Whether scan is in progress / 是否正在扫描
 * @param isGeneratingReport Whether report generation is in progress / 是否正在生成报告
 * @param selectedService Currently selected service for detail view / 当前选中的服务详情
 * @param exportFormat Selected export format / 选中的导出格式
 * @param error Error message if any / 错误信息
 */
data class AAPMDashboardState(
    val activeTab: AAPMTab = AAPMTab.STATUS,
    val aapmState: AAPMState = AAPMState.NotSupported,
    val isAccessibilityTool: Boolean = false,
    val services: List<AccessibilityServiceInfo> = emptyList(),
    val auditLogs: List<AuditLogEntry> = emptyList(),
    val complianceReport: ComplianceReport? = null,
    val isScanning: Boolean = false,
    val isGeneratingReport: Boolean = false,
    val selectedService: AccessibilityServiceInfo? = null,
    val exportFormat: ExportFormat = ExportFormat.JSON,
    val error: String? = null
) {
    companion object {
        /** Initial state / 初始状态 */
        val Initial = AAPMDashboardState()
    }

    /**
     * High risk services count / 高风险服务数量
     */
    val highRiskCount: Int
        get() = services.count { it.riskLevel == RiskLevel.HIGH }

    /**
     * Medium risk services count / 中风险服务数量
     */
    val mediumRiskCount: Int
        get() = services.count { it.riskLevel == RiskLevel.MEDIUM }

    /**
     * Low risk services count / 低风险服务数量
     */
    val lowRiskCount: Int
        get() = services.count { it.riskLevel == RiskLevel.LOW }

    /**
     * Enabled services count / 启用的服务数量
     */
    val enabledServicesCount: Int
        get() = services.count { it.isEnabled }
}

// =============================================================
// ExportFormat — 导出格式
// =============================================================
/**
 * Audit log export format / 审计日志导出格式
 */
enum class ExportFormat(val label: String, val extension: String) {
    JSON("JSON", "json"),
    CSV("CSV", "csv")
}

// =============================================================
// AAPMDashboardIntent — 用户意图
// =============================================================
/**
 * AAPM Dashboard User Intents / AAPM 工具面板用户意图
 *
 * Every user action corresponds to an Intent.
 * ViewModel receives Intent and executes business logic.
 */
sealed interface AAPMDashboardIntent {
    /** Select a tool tab / 选择工具 Tab
     * @param tab Tab to select / 要选择的 Tab
     */
    data class SelectTab(val tab: AAPMTab) : AAPMDashboardIntent

    /** Check AAPM status / 检查 AAPM 状态 */
    data object CheckAAPMStatus : AAPMDashboardIntent

    /** Scan Accessibility Services / 扫描 Accessibility Services */
    data object ScanAccessibilityServices : AAPMDashboardIntent

    /** Generate compliance report / 生成合规报告 */
    data object GenerateComplianceReport : AAPMDashboardIntent

    /** Select a service to view detail / 选择服务查看详情
     * @param service Service to select / 要选择的服务
     */
    data class SelectService(val service: AccessibilityServiceInfo) : AAPMDashboardIntent

    /** Clear selected service / 清除选中的服务 */
    data object ClearSelectedService : AAPMDashboardIntent

    /** Navigate to migration guide / 导航到迁移指南
     * @param serviceName Service name to navigate to / 要导航到的服务名称
     */
    data class NavigateToMigration(val serviceName: String) : AAPMDashboardIntent

    /** Update service response strategy / 更新服务响应策略
     * @param serviceName Service name / 服务名称
     * @param strategy New strategy / 新策略
     */
    data class UpdateServiceResponse(val serviceName: String, val strategy: ResponseStrategy) : AAPMDashboardIntent

    /** Export audit logs / 导出审计日志
     * @param format Export format / 导出格式
     */
    data class ExportAuditLogs(val format: ExportFormat) : AAPMDashboardIntent

    /** Dismiss error message / 关闭错误信息 */
    data object DismissError : AAPMDashboardIntent

    /** Clear compliance report / 清除合规报告 */
    data object ClearReport : AAPMDashboardIntent
}

// =============================================================
// AAPMDashboardEffect — 副作用
// =============================================================
/**
 * AAPM Dashboard Side Effects / AAPM 工具面板副作用
 *
 * One-time events, consumed only once by UI layer.
 * UI layer listens via LaunchedEffect + flow.collect{}.
 */
sealed interface AAPMDashboardEffect {
    /** Show compliance passed notification / 显示合规检查通过提示 */
    data object ShowCompliancePassed : AAPMDashboardEffect

    /** Show compliance failed notification / 显示合规检查失败提示
     * @param count Number of failed items / 失败项数量
     */
    data class ShowComplianceFailed(val count: Int) : AAPMDashboardEffect

    /** Navigate to migration guide / 导航到迁移指南
     * @param serviceName Service name to navigate to / 要导航到的服务名称
     */
    data class NavigateToMigration(val serviceName: String) : AAPMDashboardEffect

    /** Show export success notification / 显示导出成功提示
     * @param path Exported file path / 导出文件路径
     */
    data class ShowExportSuccess(val path: String) : AAPMDashboardEffect

    /** Show scan error notification / 显示扫描错误提示
     * @param message Error message / 错误信息
     */
    data class ShowScanError(val message: String) : AAPMDashboardEffect

    /** Show snackbar message / 显示 Snackbar 消息
     * @param message Message to display / 要显示的消息
     * @param isError Whether this is an error message / 是否为错误消息
     */
    data class ShowSnackbar(val message: String, val isError: Boolean = false) : AAPMDashboardEffect
}
