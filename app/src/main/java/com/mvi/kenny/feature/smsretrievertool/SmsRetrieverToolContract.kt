package com.mvi.kenny.feature.smsretrievertool

import androidx.compose.ui.graphics.Color

// =============================================================
// SmsRetrieverToolContract — Android 17 SMS Retriever API
// 迁移检测与自动化工具包 MVI 契约
// =============================================================
// MVI Architecture Pattern / MVI 架构模式
//
// - Model (State): Immutable data class, single source of truth for UI
// - View: Composable functions that consume State and render UI
// - Intent: User intentions, ViewModel processes and updates State
// - Effect: One-time side effects (navigation, toast, etc.)
//
// @see SmsRetrieverToolViewModel State management
// @see SmsRetrieverToolScreen Main screen

// =============================================================
// ScanStatus — 扫描状态枚举
// =============================================================
/**
 * Scan status / 扫描状态
 *
 * Represents the current state of the SMS permission scan.
 *
 * @param label Display label / 显示标签
 */
enum class ScanStatus(val label: String) {
    IDLE("Idle / 空闲"),
    SCANNING("Scanning / 扫描中"),
    SUCCESS("Completed / 完成"),
    ERROR("Error / 错误")
}

// =============================================================
// Severity — 问题严重性等级
// =============================================================
/**
 * Issue severity level / 问题严重性等级
 *
 * @param label Display label / 显示标签
 * @param priority Priority for sorting / 排序优先级
 * @param color Badge color / 徽章颜色
 */
enum class Severity(val label: String, val priority: Int, val color: Color) {
    BLOCKER("🔴 阻断 / BLOCKER", 0, Color(0xFFB3261E)),    // Red — must fix
    WARNING("🟡 警告 / WARNING", 1, Color(0xFFE8A317)),   // Amber — should fix
    INFO("🟢 通过 / INFO", 2, Color(0xFF386A20))          // Green — compliant
}

// =============================================================
// ComplianceStatus — 合规状态
// =============================================================
/**
 * Overall compliance status / 整体合规状态
 *
 * @param label Display label / 显示标签
 * @param color Status color / 状态颜色
 */
enum class ComplianceStatus(val label: String, val color: Color) {
    COMPLIANT("✅ 合规 / COMPLIANT", Color(0xFF386A20)),
    PARTIAL("⚠️ 部分合规 / PARTIAL", Color(0xFFE8A317)),
    NON_COMPLIANT("❌ 不合规 / NON_COMPLIANT", Color(0xFFB3261E))
}

// =============================================================
// AffectedCodePath — 受影响的代码路径
// =============================================================
/**
 * Affected code path / 受影响的代码路径
 *
 * Represents a location in the codebase where SMS permission
 * is being used in a way incompatible with Android 17.
 *
 * @param id Unique identifier / 唯一标识符
 * @param filePath Source file path / 源文件路径
 * @param lineNumber Line number in source file / 源文件行号
 * @param codeSnippet Problematic code snippet / 有问题的代码片段
 * @param severity Issue severity / 问题严重性
 * @param description Why this is a problem / 问题说明
 * @param suggestedMigration Suggested migration approach / 建议迁移方案
 */
data class AffectedCodePath(
    val id: String,
    val filePath: String,
    val lineNumber: Int,
    val codeSnippet: String,
    val severity: Severity,
    val description: String,
    val suggestedMigration: String
)

// =============================================================
// ModuleComplianceReport — 模块合规报告
// =============================================================
/**
 * Module compliance report / 模块合规报告
 *
 * @param moduleName Module name / 模块名称
 * @param isCompliant Whether module is compliant / 是否合规
 * @param blockerCount Number of blocker issues / 阻断性问题数量
 * @param warningCount Number of warning issues / 警告性问题数量
 * @param affectedFiles List of affected file paths / 受影响文件列表
 */
data class ModuleComplianceReport(
    val moduleName: String,
    val isCompliant: Boolean,
    val blockerCount: Int,
    val warningCount: Int,
    val affectedFiles: List<String>
)

// =============================================================
// ComplianceReport — 整体合规报告
// =============================================================
/**
 * Overall compliance report / 整体合规报告
 *
 * @param overallStatus Overall compliance status / 整体合规状态
 * @param moduleReports List of per-module reports / 各模块报告列表
 * @param totalAffectedFiles Total number of affected files / 受影响文件总数
 * @param generatedAt Report generation timestamp / 报告生成时间戳
 */
data class ComplianceReport(
    val overallStatus: ComplianceStatus,
    val moduleReports: List<ModuleComplianceReport>,
    val totalAffectedFiles: Int,
    val generatedAt: Long
)

// =============================================================
// CodeDiff — 代码迁移 Diff
// =============================================================
/**
 * Code migration diff / 代码迁移 Diff
 *
 * @param filePath File path / 文件路径
 * @param beforeCode Original code / 原始代码
 * @param afterCode Migrated code / 迁移后代码
 * @param diffText Unified diff text / 统一 diff 文本
 */
data class CodeDiff(
    val filePath: String,
    val beforeCode: String,
    val afterCode: String,
    val diffText: String
)

// =============================================================
// MigrationResult — 迁移结果
// =============================================================
/**
 * Migration result / 迁移结果
 *
 * @param success Whether migration succeeded / 迁移是否成功
 * @param migratedFiles List of migrated files / 已迁移文件列表
 * @param failedFiles List of failed files / 失败文件列表
 * @param buildVerification Whether build verification passed / 构建验证是否通过
 * @param errorMessage Error message if any / 错误信息
 */
data class MigrationResult(
    val success: Boolean,
    val migratedFiles: List<String>,
    val failedFiles: List<String>,
    val buildVerification: Boolean,
    val errorMessage: String? = null
)

// =============================================================
// SmsRetrieverSettings — 设置
// =============================================================
/**
 * Tool settings / 工具设置
 *
 * @param targetSdk Target SDK version for compliance check / 合规检查目标 SDK 版本
 * @param customOtpRegex Custom OTP regex pattern / 自定义 OTP 正则表达式
 * @param ignorePaths List of paths to ignore during scan / 扫描时忽略的路径
 */
data class SmsRetrieverSettings(
    val targetSdk: Int = 37,
    val customOtpRegex: String = "\\d{4,8}",
    val ignorePaths: List<String> = emptyList()
)

// =============================================================
// SmsRetrieverToolState — 页面状态
// =============================================================
/**
 * SMS Retriever Tool State / SMS Retriever 工具页面状态
 *
 * Single source of truth for the entire SMS Retriever Tool UI.
 * All UI state is derived from this data class.
 *
 * @param scanStatus Current scan status / 当前扫描状态
 * @param scannedFilesCount Number of files scanned / 已扫描文件数量
 * @param affectedCodePaths List of affected code paths / 受影响代码路径列表
 * @param selectedModule Currently selected module / 当前选中模块
 * @param availableModules List of available modules / 可用模块列表
 * @param targetSdk Target SDK version / 目标 SDK 版本
 * @param complianceReport Generated compliance report / 生成的合规报告
 * @param migrationInProgress Whether migration is in progress / 迁移是否进行中
 * @param migrationResult Result of migration / 迁移结果
 * @param selectedCodePath Selected code path for detail view / 选中的代码路径
 * @param settings Tool settings / 工具设置
 * @param error Error message if any / 错误信息
 */
data class SmsRetrieverToolState(
    val scanStatus: ScanStatus = ScanStatus.IDLE,
    val scannedFilesCount: Int = 0,
    val affectedCodePaths: List<AffectedCodePath> = emptyList(),
    val selectedModule: String? = null,
    val availableModules: List<String> = emptyList(),
    val targetSdk: Int = 37,
    val complianceReport: ComplianceReport? = null,
    val migrationInProgress: Boolean = false,
    val migrationResult: MigrationResult? = null,
    val selectedCodePath: AffectedCodePath? = null,
    val settings: SmsRetrieverSettings = SmsRetrieverSettings(),
    val error: String? = null
) {
    companion object {
        /** Initial state / 初始状态 */
        val Initial = SmsRetrieverToolState()
    }

    /**
     * Sorted affected code paths by severity / 按严重性排序的受影响代码路径
     * BLOCKER issues first / 阻断性问题优先
     */
    val sortedAffectedPaths: List<AffectedCodePath>
        get() = affectedCodePaths.sortedBy { it.severity.priority }

    /**
     * Count of blocker issues / 阻断性问题数量
     */
    val blockerCount: Int
        get() = affectedCodePaths.count { it.severity == Severity.BLOCKER }

    /**
     * Count of warning issues / 警告性问题数量
     */
    val warningCount: Int
        get() = affectedCodePaths.count { it.severity == Severity.WARNING }

    /**
     * Count of info/compliant issues / 通过/信息性问题数量
     */
    val infoCount: Int
        get() = affectedCodePaths.count { it.severity == Severity.INFO }

    /**
     * Whether scan has results / 扫描是否有结果
     */
    val hasResults: Boolean
        get() = affectedCodePaths.isNotEmpty()
}

// =============================================================
// SmsRetrieverToolIntent — 用户意图
// =============================================================
/**
 * SMS Retriever Tool User Intents / SMS Retriever 工具用户意图
 *
 * Every user action corresponds to an Intent.
 * ViewModel receives Intent and executes business logic.
 */
sealed interface SmsRetrieverToolIntent {
    /** Start scanning for SMS permission usage / 开始扫描 SMS 权限使用情况
     * @param module Module path to scan / 要扫描的模块路径
     */
    data class StartScan(val module: String) : SmsRetrieverToolIntent

    /** Cancel ongoing scan / 取消正在进行的扫描 */
    data object CancelScan : SmsRetrieverToolIntent

    /** Select a module / 选择模块
     * @param module Module to select / 要选择的模块
     */
    data class SelectModule(val module: String) : SmsRetrieverToolIntent

    /** View affected code path detail / 查看受影响代码路径详情
     * @param codePath Code path to view / 要查看的代码路径
     */
    data class ViewAffectedCode(val codePath: AffectedCodePath) : SmsRetrieverToolIntent

    /** Clear selected code path / 清除选中的代码路径 */
    data object ClearSelectedCodePath : SmsRetrieverToolIntent

    /** Start migration for a specific code path / 开始迁移特定代码路径
     * @param codePath Code path to migrate / 要迁移的代码路径
     */
    data class StartMigration(val codePath: AffectedCodePath) : SmsRetrieverToolIntent

    /** Confirm and apply migration / 确认并应用迁移
     * @param diff Migration diff to apply / 要应用的迁移 diff
     */
    data class ConfirmMigration(val diff: CodeDiff) : SmsRetrieverToolIntent

    /** Generate compliance report / 生成合规报告 */
    data object GenerateComplianceReport : SmsRetrieverToolIntent

    /** Update tool settings / 更新工具设置
     * @param settings New settings / 新设置
     */
    data class UpdateSettings(val settings: SmsRetrieverSettings) : SmsRetrieverToolIntent

    /** Dismiss error message / 关闭错误信息 */
    data object DismissError : SmsRetrieverToolIntent

    /** Clear migration result / 清除迁移结果 */
    data object ClearMigrationResult : SmsRetrieverToolIntent
}

// =============================================================
// SmsRetrieverToolEffect — 副作用
// =============================================================
/**
 * SMS Retriever Tool Side Effects / SMS Retriever 工具副作用
 *
 * One-time events, consumed only once by UI layer.
 * UI layer listens via LaunchedEffect + flow.collect{}.
 */
sealed interface SmsRetrieverToolEffect {
    /** Show toast message / 显示 Toast 消息
     * @param message Message to display / 要显示的消息
     */
    data class ShowToast(val message: String) : SmsRetrieverToolEffect

    /** Navigate to code detail / 导航到代码详情
     * @param codePath Code path to show / 要显示的代码路径
     */
    data class NavigateToDetail(val codePath: AffectedCodePath) : SmsRetrieverToolEffect

    /** Navigate to migration preview / 导航到迁移预览 */
    data object NavigateToMigrationPreview : SmsRetrieverToolEffect

    /** Compliance report generated / 合规报告已生成
     * @param report Generated report / 生成的报告
     */
    data class ReportGenerated(val report: ComplianceReport) : SmsRetrieverToolEffect

    /** Migration completed / 迁移完成
     * @param result Migration result / 迁移结果
     */
    data class MigrationCompleted(val result: MigrationResult) : SmsRetrieverToolEffect

    /** Show error message / 显示错误消息
     * @param message Error message / 错误消息
     */
    data class ShowError(val message: String) : SmsRetrieverToolEffect
}
