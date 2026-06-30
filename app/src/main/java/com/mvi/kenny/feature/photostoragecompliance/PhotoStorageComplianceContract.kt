package com.mvi.kenny.feature.photostoragecompliance

import android.net.Uri
import androidx.compose.ui.graphics.Color

/**
 * ============================================================
 * PhotoStorageComplianceContract — Photo Picker & Scoped Storage
 *                             合规迁移工具包 MVI 契约
 * PhotoStorageCompliance MVI Contract — Android Privacy Compliance Toolkit
 * ============================================================
 *
 * PRD-308 | Android Photo Picker & Scoped Storage 合规迁移工具包
 * Ref: memory/agency/designs/PRD-308-Android-Photo-Picker-Scoped-Storage-Compliance-Migration-Toolkit.md
 *
 * MVI Architecture:
 * - Model (State): Immutable page state, single source of truth
 * - View: Composable functions that consume State and render UI
 * - Intent: User intentions; ViewModel executes logic on receiving Intent
 * - Effect: One-time side effects (Toast, Navigation) delivered via Channel
 * —————————————————————————————————————————————————————
 *
 * Page Structure (5 Main Sections):
 * - Section 1: Scan Configuration Page (scan scope & target API level)
 * - Section 2: Scan Progress Page (3-phase animation)
 * - Section 3: Photo Picker Violations Tab
 * - Section 4: Health Connect Migration Tab
 * - Section 5: Report Preview / Export Page
 */

// ============================================================
// Data Models / 数据模型
// ============================================================

/**
 * Scan phase enumeration — 3 phases of compliance scan
 * 扫描阶段枚举 — 合规扫描的3个阶段
 *
 * @param label Display label for progress indicator
 * @param description Phase description shown to user
 */
enum class ScanPhase(val label: String, val description: String) {
    Idle("待扫描", "Click Start to begin compliance scan"),
    StaticAnalysis("静态分析", "Analyzing code for READ_MEDIA_* and Scoped Storage usage"),
    RuntimeDetection("运行时检测", "Simulating Photo Picker fallback behavior"),
    Generating("生成报告", "Compiling compliance report and fix suggestions")
}

/**
 * Compliance overall status — Red/Yellow/Green
 * 合规状态 — 红/黄/绿
 *
 * @param label Display label
 * @param color Badge background color
 */
enum class ComplianceStatus(val label: String, val color: Color) {
    Compliant("合规", Color(0xFF43A047)),       // Green 600
    Warnings("待修复", Color(0xFFFFA000)),        // Amber 700
    Violations("违规", Color(0xFFE53935)),        // Red 600
    Unknown("未知", Color(0xFF9E9E9E))           // Gray 500
}

/**
 * Violation severity level
 * 违规严重程度
 *
 * @param weight Sort weight for ordering
 * @param label Display label
 */
enum class ViolationSeverity(val weight: Int, val label: String) {
    Critical(0, "P0 严重"),
    High(1, "P1 高"),
    Medium(2, "P2 中"),
    Low(3, "P3 低")
}

/**
 * Single Photo Picker violation item
 * Photo Picker 违规项
 *
 * @param id Unique identifier
 * @param title Violation title / 违规标题
 * @param filePath Source file path / 源文件路径
 * @param lineNumber Line number in file / 行号
 * @param codeSnippet Original problematic code / 问题代码片段
 * @param fixCode Suggested fix code / 修复代码
 * @param severity Violation severity / 违规严重程度
 * @param apiLevel Target API level causing violation / 触发违规的目标API级别
 */
data class Violation(
    val id: String,
    val title: String,
    val filePath: String,
    val lineNumber: Int,
    val codeSnippet: String,
    val fixCode: String,
    val severity: ViolationSeverity,
    val apiLevel: Int,
    val isExpanded: Boolean = false
)

/**
 * Health Connect migration item
 * Health Connect 迁移项
 *
 * @param id Unique identifier
 * @param title Migration item title / 迁移项标题
 * @param currentCode Current code using direct sensor / 当前直接传感器代码
 * @param migrationSteps 3-step migration process / 三步迁移流程
 * @param currentStep Current step user is on / 当前步骤
 */
data class HealthMigrationItem(
    val id: String,
    val title: String,
    val currentCode: String,
    val migrationSteps: List<MigrationStep>,
    val currentStep: Int = 0,
    val isExpanded: Boolean = false
)

/**
 * Single migration step in Health Connect migration flow
 * Health Connect 迁移流程中的单个步骤
 *
 * @param stepNumber Step number (1-3)
 * @param title Step title / 步骤标题
 * @param description Step description / 步骤描述
 * @param code Code snippet for this step / 步骤代码片段
 */
data class MigrationStep(
    val stepNumber: Int,
    val title: String,
    val description: String,
    val code: String
)

/**
 * Export format for compliance report
 * 合规报告导出格式
 *
 * @param extension File extension
 * @param mimeType MIME type for file sharing
 */
enum class ExportFormat(val extension: String, val mimeType: String) {
    PDF("pdf", "application/pdf"),
    JSON("json", "application/json")
}

/**
 * Compliance report data class
 * 合规报告数据类
 *
 * @param generatedAt Report generation timestamp
 * @param photoViolations Count of Photo Picker violations
 * @param healthConnectItems Count of Health Connect migration items
 * @param overallStatus Overall compliance status
 * @param targetApiLevel Target API level scanned
 * @param reportUri URI of generated report file
 */
data class Report(
    val generatedAt: Long,
    val photoViolations: Int,
    val healthConnectItems: Int,
    val overallStatus: ComplianceStatus,
    val targetApiLevel: Int,
    val reportUri: Uri? = null
)

// ============================================================
// State / 页面状态
// ============================================================

/**
 * Photo Picker & Scoped Storage Compliance page state
 * Photo Picker & Scoped Storage 合规迁移工具包页面状态
 *
 * MVI Architecture: Model layer, holds all page state.
 * State is Immutable — each state change creates a new State object.
 *
 * @param scanPhase Current scan phase / 当前扫描阶段
 * @param photoViolations List of Photo Picker violations / Photo Picker 违规列表
 * @param healthConnectItems List of Health Connect migration items / Health Connect 迁移列表
 * @param selectedItems Set of selected violation IDs for batch operations / 选中的违规项ID集合
 * @param overallStatus Overall compliance status / 整体合规状态
 * @param targetApiLevel Selected target API level for scan / 扫描目标API级别
 * @param scanScope Selected scan scope (whole project or specific module) / 扫描范围
 * @param selectedModule Selected module name when scanScope=MODULE / 选中的模块名
 * @param isExporting Whether report export is in progress / 是否正在导出报告
 * @param exportedReport Generated report data / 已生成的报告
 * @param selectedExportFormat Selected export format (PDF/JSON) / 选中的导出格式
 * @param error Error message, null means no error / 错误信息，null表示无错误
 *
 * @see PhotoStorageComplianceIntent
 * @see PhotoStorageComplianceViewModel
 */
data class PhotoStorageComplianceState(
    // Scan state / 扫描状态
    val scanPhase: ScanPhase = ScanPhase.Idle,
    // Violation data / 违规数据
    val photoViolations: List<Violation> = emptyList(),
    val healthConnectItems: List<HealthMigrationItem> = emptyList(),
    // Selection state / 选中状态
    val selectedItems: Set<String> = emptySet(),
    // Overall status / 整体状态
    val overallStatus: ComplianceStatus = ComplianceStatus.Unknown,
    // Configuration / 配置
    val targetApiLevel: Int = 36,
    val scanScope: ScanScope = ScanScope.WHOLE_PROJECT,
    val selectedModule: String = "",
    // Export state / 导出状态
    val isExporting: Boolean = false,
    val exportedReport: Report? = null,
    val selectedExportFormat: ExportFormat = ExportFormat.JSON,
    // Error state / 错误状态
    val error: String? = null
) {
    companion object {
        /** Initial / default state / 初始状态 */
        val Initial = PhotoStorageComplianceState()
    }

    /**
     * Compute overall compliance status based on violations
     * 根据违规情况计算整体合规状态
     */
    fun computeOverallStatus(): ComplianceStatus = when {
        photoViolations.any { it.severity == ViolationSeverity.Critical } -> ComplianceStatus.Violations
        photoViolations.any { it.severity == ViolationSeverity.High } -> ComplianceStatus.Violations
        photoViolations.isNotEmpty() -> ComplianceStatus.Warnings
        healthConnectItems.isNotEmpty() -> ComplianceStatus.Warnings
        else -> ComplianceStatus.Compliant
    }

    /**
     * Check if scan can be started
     * 检查是否可以开始扫描
     */
    val canStartScan: Boolean
        get() = scanPhase == ScanPhase.Idle &&
                (scanScope == ScanScope.WHOLE_PROJECT || selectedModule.isNotBlank())
}

/**
 * Scan scope enumeration
 * 扫描范围枚举
 *
 * @param label Display label
 */
enum class ScanScope(val label: String) {
    WHOLE_PROJECT("整个项目"),
    SPECIFIC_MODULE("指定模块")
}

// ============================================================
// Intent / 用户意图
// ============================================================

/**
 * User intentions for Photo Picker & Scoped Storage Compliance page
 * Photo Picker & Scoped Storage 合规迁移工具包用户意图
 *
 * Every user interaction on the page corresponds to an Intent.
 * ViewModel receives Intent, executes business logic, then updates State.
 *
 * @see PhotoStorageComplianceViewModel.sendIntent
 */
sealed interface PhotoStorageComplianceIntent {

    /**
     * Start compliance scan / 开始合规扫描
     */
    data object StartScan : PhotoStorageComplianceIntent

    /**
     * Cancel ongoing scan / 取消正在进行的扫描
     */
    data object CancelScan : PhotoStorageComplianceIntent

    /**
     * Select scan scope (whole project or module)
     * 选择扫描范围
     *
     * @param scope Scan scope / 扫描范围
     */
    data class SelectScanScope(val scope: ScanScope) : PhotoStorageComplianceIntent

    /**
     * Select target API level for scan
     * 选择扫描目标API级别
     *
     * @param apiLevel Target API level (35=warning, 36=enforcement) / 目标API级别
     */
    data class SelectTargetApiLevel(val apiLevel: Int) : PhotoStorageComplianceIntent

    /**
     * Select specific module to scan
     * 选择要扫描的特定模块
     *
     * @param module Module name / 模块名称
     */
    data class SelectModule(val module: String) : PhotoStorageComplianceIntent

    /**
     * Toggle violation item selection
     * 切换违规项选中状态
     *
     * @param id Violation item ID / 违规项ID
     */
    data class ToggleItemSelection(val id: String) : PhotoStorageComplianceIntent

    /**
     * Select all violations
     * 全选所有违规项
     */
    data object SelectAll : PhotoStorageComplianceIntent

    /**
     * Deselect all violations
     * 取消全选
     */
    data object DeselectAll : PhotoStorageComplianceIntent

    /**
     * Toggle violation detail expansion
     * 切换违规详情展开/收起
     *
     * @param id Violation item ID / 违规项ID
     */
    data class ToggleViolationExpansion(val id: String) : PhotoStorageComplianceIntent

    /**
     * Copy fix code for a single violation
     * 复制单个违规项的修复代码
     *
     * @param id Violation item ID / 违规项ID
     */
    data class CopyFixCode(val id: String) : PhotoStorageComplianceIntent

    /**
     * Copy fix code for all selected violations (batch operation)
     * 批量复制所有选中违规项的修复代码
     */
    data object BatchCopyFixCode : PhotoStorageComplianceIntent

    /**
     * Toggle Health Connect migration item expansion
     * 切换 Health Connect 迁移项展开/收起
     *
     * @param id Migration item ID / 迁移项ID
     */
    data class ToggleHealthItemExpansion(val id: String) : PhotoStorageComplianceIntent

    /**
     * Advance Health Connect migration step
     * 进入 Health Connect 迁移下一步
     *
     * @param id Migration item ID / 迁移项ID
     */
    data class AdvanceHealthStep(val id: String) : PhotoStorageComplianceIntent

    /**
     * Select export format (PDF or JSON)
     * 选择导出格式
     *
     * @param format Export format / 导出格式
     */
    data class SelectExportFormat(val format: ExportFormat) : PhotoStorageComplianceIntent

    /**
     * Export compliance report
     * 导出合规报告
     *
     * @param format Export format / 导出格式
     */
    data class ExportReport(val format: ExportFormat) : PhotoStorageComplianceIntent

    /**
     * Share exported report
     * 分享导出的报告
     *
     * @param uri Report file URI / 报告文件URI
     */
    data class ShareReport(val uri: Uri) : PhotoStorageComplianceIntent
}

// ============================================================
// Effect / 副作用
// ============================================================

/**
 * One-time side effects for Photo Picker & Scoped Storage Compliance page
 * Photo Picker & Scoped Storage 合规迁移工具包副作用
 *
 * One-time events, immutable, consumed only once.
 * UI layer listens via LaunchedEffect + flow.collect {}
 *
 * @see PhotoStorageComplianceViewModel
 */
sealed interface PhotoStorageComplianceEffect {

    /**
     * Show toast message / 显示 Toast
     *
     * @param message Toast message text / Toast 文本
     */
    data class ShowToast(val message: String) : PhotoStorageComplianceEffect

    /**
     * Copy text to system clipboard / 复制文本到剪贴板
     *
     * @param text Text to copy / 要复制的文本
     */
    data class CopyToClipboard(val text: String) : PhotoStorageComplianceEffect

    /**
     * Scan complete notification / 扫描完成通知
     */
    data object ScanComplete : PhotoStorageComplianceEffect

    /**
     * Share report file / 分享报告文件
     *
     * @param uri Report file URI / 报告文件URI
     * @param mimeType MIME type / MIME类型
     */
    data class ShareReport(val uri: Uri, val mimeType: String) : PhotoStorageComplianceEffect

    /**
     * Report export complete / 报告导出完成
     *
     * @param downloadUrl Download URL or file path / 下载URL或文件路径
     */
    data class ReportReady(val downloadUrl: String) : PhotoStorageComplianceEffect
}
