package com.mvi.kenny.feature.prd210compliance

import androidx.compose.ui.graphics.Color
import java.time.LocalDate

/**
 * ============================================================
 * Prd210ComplianceContract — PRD-210 Google Play 2026年4月政策三连击合规工具包
 * ============================================================
 * MVI Architecture: State (UI data) + Intent (user actions) + Effect (one-time events)
 *
 * Google Play April 2026 Policy Triple Strike:
 * 1. Contacts Permissions (2026-10-28 deadline)
 * 2. Location Permissions (2026-10-28 deadline)
 * 3. Account Transfer (2026-05-27 already effective)
 *
 * Design Doc: memory/agency/designs/PRD-210-Google-Play-政策三连击合规工具包.md
 * Status: 设计完成，待移交开发 | Completed: 2026-04-30
 *
 * Bilingual comments: CN + EN
 */

// =============================================================
// Tab — 功能 Tab 枚举
// =============================================================
/**
 * 5 功能 Tab / 5 functional tabs
 * @param title Tab display title / Tab 显示标题
 */
enum class ComplianceTab(val title: String) {
    CONTACTS_SCANNER("Contacts扫描器"),     // READ_CONTACTS usage scanner / READ_CONTACTS 使用扫描器
    LOCATION_SCANNER("Location扫描器"),     // Location permissions scanner / 位置权限扫描器
    ACCOUNT_TRANSFER("Account Transfer"),   // Ownership transfer guide / 所有权转移引导
    CHECKLIST("三合一清单"),                 // Triple policy checklist / 三政策协同检查清单
    DASHBOARD("仪表盘")                     // Compliance dashboard / 合规状态仪表盘
}

// =============================================================
// Risk Level — 风险等级
// =============================================================
/**
 * Risk level for compliance findings / 合规发现的风险等级
 * @param label Short label / 短标签
 * @param labelZh Chinese label / 中文标签
 * @param color Risk color / 风险颜色
 */
enum class RiskLevel(val label: String, val labelZh: String, val color: Color) {
    P0("P0", "立即行动", Color(0xFFF85149)),   // Must act now / 必须立即处理
    P1("P1", "近期行动", Color(0xFFFB8C00)),   // Act soon / 需要近期处理
    P2("P2", "建议关注", Color(0xFFFDD835))    // Monitor / 建议关注
}

// =============================================================
// Permission Type — 权限类型
// =============================================================
/**
 * Permission type under analysis / 分析中的权限类型
 */
enum class PermissionType {
    READ_CONTACTS,
    ACCESS_FINE_LOCATION,
    ACCESS_COARSE_LOCATION
}

// =============================================================
// Contacts Scan Result Item — Contacts 扫描结果项
// =============================================================
/**
 * Single READ_CONTACTS finding / 单条 READ_CONTACTS 发现
 * @param id Unique ID / 唯一 ID
 * @param filePath File containing the usage / 包含使用的文件路径
 * @param lineNumber Line number in file / 文件行号
 * @param permissionType Permission declared / 声明的权限类型
 * @param usageContext Context where permission is used / 权限使用场景
 * @param migrationGuide Migration suggestion / 迁移建议
 * @param riskLevel Associated risk level / 关联风险等级
 */
data class ContactsScanItem(
    val id: String,
    val filePath: String,
    val lineNumber: Int,
    val permissionType: PermissionType,
    val usageContext: String,
    val migrationGuide: String,
    val riskLevel: RiskLevel
)

// =============================================================
// Location Scan Result Item — Location 扫描结果项
// =============================================================
/**
 * Single location permission finding / 单条位置权限发现
 * @param id Unique ID / 唯一 ID
 * @param filePath File containing the usage / 包含使用的文件路径
 * @param lineNumber Line number in file / 文件行号
 * @param permissionType Fine or Coarse location / 精确或粗略位置
 * @param usageContext Context where permission is used / 权限使用场景
 * @param needsDeclaration Whether Play Developer Declaration is needed / 是否需要声明
 * @param riskLevel Associated risk level / 关联风险等级
 */
data class LocationScanItem(
    val id: String,
    val filePath: String,
    val lineNumber: Int,
    val permissionType: PermissionType,
    val usageContext: String,
    val needsDeclaration: Boolean,
    val riskLevel: RiskLevel
)

// =============================================================
// Account Transfer Guide — Account Transfer 引导
// =============================================================
/**
 * Account transfer step / 账户转移步骤
 * @param stepNumber Step number / 步骤编号
 * @param title Step title / 步骤标题
 * @param description Step description / 步骤描述
 * @param screenshotRequired Whether screenshot reference needed / 是否需要截图参考
 * @param isCompleted Whether step is completed / 步骤是否完成
 */
data class TransferStep(
    val stepNumber: Int,
    val title: String,
    val description: String,
    val screenshotRequired: Boolean = false,
    val isCompleted: Boolean = false
)

// =============================================================
// Checklist Item — 检查清单项
// =============================================================
/**
 * Single checklist item across 3 policies / 三政策之一的检查清单项
 * @param id Unique ID / 唯一 ID
 * @param policyName Policy name (Contacts/Location/AccountTransfer) / 政策名称
 * @param title Item title / 清单标题
 * @param description Item description / 清单描述
 * @param deadline Associated deadline / 关联截止日期
 * @param daysRemaining Days remaining / 剩余天数
 * @param isCompleted Whether item is completed / 是否已完成
 * @param riskLevel Associated risk level / 关联风险等级
 */
data class ChecklistItem(
    val id: String,
    val policyName: String,
    val title: String,
    val description: String,
    val deadline: LocalDate,
    val daysRemaining: Long,
    val isCompleted: Boolean = false,
    val riskLevel: RiskLevel
)

// =============================================================
// App Compliance Status — App 合规状态
// =============================================================
/**
 * Per-app compliance status across 3 policies / App 在三条政策下的合规状态
 * @param packageName App package name / App 包名
 * @param appName App display name / App 显示名称
 * @param contactsStatus Contacts policy compliance / Contacts 政策合规状态
 * @param locationStatus Location policy compliance / Location 政策合规状态
 * @param accountTransferStatus Account transfer status / 账户转移状态
 * @param overallRiskLevel Overall risk level / 总体风险等级
 */
data class AppComplianceStatus(
    val packageName: String,
    val appName: String,
    val contactsStatus: ComplianceStatusType,
    val locationStatus: ComplianceStatusType,
    val accountTransferStatus: ComplianceStatusType,
    val overallRiskLevel: RiskLevel
)

/**
 * Compliance status type / 合规状态类型
 */
enum class ComplianceStatusType(val label: String, val labelZh: String) {
    COMPLIANT("Compliant", "已合规"),
    NON_COMPLIANT("Non-compliant", "不合规"),
    NEEDS_REVIEW("Needs Review", "需审核"),
    NOT_APPLICABLE("N/A", "不适用")
}

// =============================================================
// Dashboard Stats — 仪表盘统计
// =============================================================
/**
 * Dashboard statistics / 仪表盘统计数据
 * @param totalApps Total apps scanned / 扫描的 App 总数
 * @param contactsCompliant Contacts-compliant apps / Contacts 合规 App 数
 * @param locationCompliant Location-compliant apps / Location 合规 App 数
 * @param accountTransferCompliant Account transfer compliant apps / Account Transfer 合规 App 数
 * @param urgentCount Apps with P0 issues / 有 P0 问题的 App 数
 * @param accountTransferDeadline Account Transfer deadline / Account Transfer 截止日期
 * @param contactsLocationDeadline Contacts/Location deadline / Contacts/Location 截止日期
 */
data class DashboardStats(
    val totalApps: Int = 0,
    val contactsCompliant: Int = 0,
    val locationCompliant: Int = 0,
    val accountTransferCompliant: Int = 0,
    val urgentCount: Int = 0,
    val accountTransferDeadline: LocalDate = LocalDate.of(2026, 5, 27),
    val contactsLocationDeadline: LocalDate = LocalDate.of(2026, 10, 28)
)

// =============================================================
// Prd210ComplianceState — 主状态
// =============================================================
/**
 * Prd210Compliance MVI State / PRD-210 主状态
 *
 * Single source of truth for the entire Google Play Compliance Tool UI.
 * 整个 Google Play 合规工具 UI 的单一数据源。
 *
 * @param selectedTab Currently selected tab / 当前选中 Tab
 * @param contactsProjectPath Project path for contacts scan / Contacts 扫描的项目路径
 * @param contactsScanResults Contacts scan results / Contacts 扫描结果
 * @param contactsScanning Whether contacts scan is in progress / Contacts 是否正在扫描
 * @param contactsScanOutput Terminal-style output text / 终端风格输出文本
 * @param locationProjectPath Project path for location scan / Location 扫描的项目路径
 * @param locationScanResults Location scan results / Location 扫描结果
 * @param locationScanning Whether location scan is in progress / Location 是否正在扫描
 * @param locationScanOutput Terminal-style output text / 终端风格输出文本
 * @param accountTransferAppId App ID for account transfer / Account Transfer 的 App ID
 * @param accountTransferGuide Account transfer guide steps / Account Transfer 引导步骤
 * @param accountTransferGenerating Whether guide is being generated / 是否正在生成引导
 * @param checklistItems All checklist items / 所有检查清单项
 * @param dashboardStats Dashboard statistics / 仪表盘统计数据
 * @param dashboardApps Dashboard app list / 仪表盘 App 列表
 * @param isLoadingGeneral Whether general loading is shown / 是否显示通用加载
 * @param errorMessage Error message if any / 错误信息
 */
data class Prd210ComplianceState(
    val selectedTab: ComplianceTab = ComplianceTab.CONTACTS_SCANNER,
    // Contacts Scanner / Contacts 扫描器
    val contactsProjectPath: String = "",
    val contactsScanResults: List<ContactsScanItem> = emptyList(),
    val contactsScanning: Boolean = false,
    val contactsScanOutput: String = "",
    // Location Scanner / Location 扫描器
    val locationProjectPath: String = "",
    val locationScanResults: List<LocationScanItem> = emptyList(),
    val locationScanning: Boolean = false,
    val locationScanOutput: String = "",
    // Account Transfer / 账户转移
    val accountTransferAppId: String = "",
    val accountTransferGuide: List<TransferStep> = emptyList(),
    val accountTransferGenerating: Boolean = false,
    // Checklist / 检查清单
    val checklistItems: List<ChecklistItem> = emptyList(),
    // Dashboard / 仪表盘
    val dashboardStats: DashboardStats = DashboardStats(),
    val dashboardApps: List<AppComplianceStatus> = emptyList(),
    // General / 通用
    val isLoadingGeneral: Boolean = false,
    val errorMessage: String? = null
) {
    companion object {
        /** Initial state / 初始状态 */
        val Initial = Prd210ComplianceState()
    }
}

// =============================================================
// Prd210ComplianceIntent — 用户意图
// =============================================================
/**
 * Prd210Compliance MVI Intent / PRD-210 用户意图
 *
 * Every user action in the UI corresponds to an Intent.
 * 每一个 UI 中的用户操作都对应一个 Intent。
 */
sealed interface Prd210ComplianceIntent {
    /** Select a tab / 选中 Tab
     * @param tab Tab to select / 要选中的 Tab
     */
    data class SelectTab(val tab: ComplianceTab) : Prd210ComplianceIntent

    // Contacts Scanner / Contacts 扫描器
    /** Update contacts project path / 更新 Contacts 项目路径
     * @param path Project directory path / 项目目录路径
     */
    data class ContactsPathChanged(val path: String) : Prd210ComplianceIntent
    /** Start contacts scan / 开始 Contacts 扫描 */
    data object StartContactsScan : Prd210ComplianceIntent
    /** Clear contacts scan results / 清除 Contacts 扫描结果 */
    data object ClearContactsScan : Prd210ComplianceIntent

    // Location Scanner / Location 扫描器
    /** Update location project path / 更新 Location 项目路径
     * @param path Project directory path / 项目目录路径
     */
    data class LocationPathChanged(val path: String) : Prd210ComplianceIntent
    /** Start location scan / 开始 Location 扫描 */
    data object StartLocationScan : Prd210ComplianceIntent
    /** Clear location scan results / 清除 Location 扫描结果 */
    data object ClearLocationScan : Prd210ComplianceIntent

    // Account Transfer / 账户转移
    /** Update account transfer App ID / 更新 Account Transfer App ID
     * @param appId Google Play App ID / Google Play App ID
     */
    data class AccountTransferAppIdChanged(val appId: String) : Prd210ComplianceIntent
    /** Generate account transfer guide / 生成 Account Transfer 引导 */
    data object GenerateAccountTransferGuide : Prd210ComplianceIntent
    /** Mark transfer step as completed / 标记转移步骤完成
     * @param stepNumber Step number / 步骤编号
     */
    data class MarkTransferStepCompleted(val stepNumber: Int) : Prd210ComplianceIntent

    // Checklist / 检查清单
    /** Generate triple-policy checklist / 生成三合一检查清单 */
    data object GenerateChecklist : Prd210ComplianceIntent
    /** Toggle checklist item / 切换检查清单项
     * @param itemId Item ID to toggle / 要切换的项 ID
     */
    data class ToggleChecklistItem(val itemId: String) : Prd210ComplianceIntent

    // Dashboard / 仪表盘
    /** Load dashboard data / 加载仪表盘数据 */
    data object LoadDashboard : Prd210ComplianceIntent

    // General / 通用
    /** Dismiss error message / 关闭错误消息 */
    data object DismissError : Prd210ComplianceIntent
    /** Export compliance report / 导出合规报告
     * @param format Report format (MARKDOWN/JSON) / 报告格式
     */
    data class ExportReport(val format: String) : Prd210ComplianceIntent
}

// =============================================================
// Prd210ComplianceEffect — 副作用
// =============================================================
/**
 * Prd210Compliance MVI Effect / PRD-210 一次性副作用
 *
 * One-time side effects, delivered via Channel.
 * 一次性副作用，通过 Channel 投递。
 */
sealed interface Prd210ComplianceEffect {
    /** Show snackbar message / 显示 Snackbar 消息
     * @param message Message text / 消息文本
     */
    data class ShowSnackbar(val message: String) : Prd210ComplianceEffect

    /** Show error message / 显示错误消息
     * @param message Error description / 错误描述
     */
    data class ShowError(val message: String) : Prd210ComplianceEffect

    /** Report exported / 报告已导出
     * @param content Report content / 报告内容
     * @param format Report format / 报告格式
     */
    data class ReportExported(val content: String, val format: String) : Prd210ComplianceEffect

    /** Scan complete notification / 扫描完成通知 */
    data object ScanComplete : Prd210ComplianceEffect
}
