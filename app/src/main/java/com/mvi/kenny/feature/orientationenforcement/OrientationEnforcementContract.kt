package com.mvi.kenny.feature.orientationenforcement

// ================================================================
// OrientationEnforcementContract — Android 17 Large Screen Orientation Enforcement MVI Contract
// ================================================================
// MVI architecture contract for Android 17 Large Screen Orientation Enforcement compliance toolkit.
//
// PRD-183: Android 17 Large Screen Resizability & Orientation Enforcement 合规检测工具包
// Design Reference: memory/agency/designs/PRD-183-Android-17-Large-Screen-Resizability-Orientation-Enforcement.md
//
// MVI 三要素 / Three pillars:
//   Model (State)  — Immutable page state, single source of truth
//   Intent          — User intentions, ViewModel processes and updates State
//   Effect          — One-time side effects (navigation, toast), via Channel
// ================================================================

import androidx.compose.ui.graphics.Color

/**
 * ============================================================
 * ComplianceStatus — 合规状态枚举
 * ============================================================
 * Represents the compliance status of an orientation/resizability check.
 *
 * @param displayName Chinese display name
 * @param emoji Emoji representation
 * @param color Status color
 */
enum class ComplianceStatus(
    val displayName: String,
    val emoji: String,
    val color: Color
) {
    PASS("合规", "✅", Color(0xFF3FB950)),
    WARNING("警告", "⚠️", Color(0xFFFFD700)),
    FAIL("不合规", "❌", Color(0xFFF85149)),
    UNKNOWN("未检测", "❓", Color(0xFF8B949E))
}

/**
 * ============================================================
 * ToolId — 工具 ID 常量
 * ============================================================
 */
object ToolId {
    const val ORIENTATION_LOCK_SCANNER = "orientation_lock_scanner"
    const val RESIZABLE_ACTIVITY_DETECTOR = "resizable_activity_detector"
    const val CONFIG_CHANGE_VALIDATOR = "config_change_validator"
    const val ORIENTATION_MIGRATION_GUIDE = "orientation_migration_guide"
    const val WINDOW_SIZE_CLASS_CI = "window_size_class_ci"
    const val LEGACY_APP_MIGRATION_PATH = "legacy_app_migration_path"
    const val ACCESSIBILITY_ORIENTATION_CHECK = "accessibility_orientation_check"
    const val CONFIG_CHANGE_STATE_SAVER = "config_change_state_saver"
}

/**
 * ============================================================
 * WindowSizeClassOption — 窗口大小分类选项
 * ============================================================
 */
enum class WindowSizeClassOption(
    val displayName: String,
    val displayNameEn: String,
    val description: String
) {
    COMPACT("Compact", "Compact", "宽度 < 600dp，典型手机竖屏"),
    MEDIUM("Medium", "Medium", "600dp ≤ 宽度 < 840dp，典型平板/折叠屏竖屏"),
    EXPANDED("Expanded", "Expanded", "宽度 ≥ 840dp，典型桌面/大屏设备")
}

/**
 * ============================================================
 * MigrationStep — 迁移向导步骤
 * ============================================================
 */
enum class MigrationStep(val stepNumber: Int, val title: String, val titleEn: String) {
    MANIFEST_LOCK_REMOVAL(1, "移除 Manifest 方向锁定", "Remove Manifest Orientation Lock"),
    CODE_LOCK_REMOVAL(2, "移除代码中 setRequestedOrientation", "Remove setRequestedOrientation in Code"),
    BIDIRECTIONAL_LAYOUT_VERIFY(3, "验证双向布局支持", "Verify Bidirectional Layout Support"),
    CONFIG_CHANGE_HANDLING(4, "处理配置变更（状态保存）", "Handle Configuration Change (State Save)")
}

/**
 * ============================================================
 * OrientationEnforcementState — 主状态（MVI State）
 * ============================================================
 * Immutable page state, single source of truth.
 */
data class OrientationEnforcementState(
    val scanPhase: ScanPhase = ScanPhase.Idle,
    val progress: Float = 0f,
    val selectedApp: AppInfo? = null,
    val scanResult: ScanResult? = null,
    val selectedViolation: ViolationItem? = null,
    val migrationStep: MigrationStep = MigrationStep.MANIFEST_LOCK_REMOVAL,
    val generatedDiff: DiffResult? = null,
    val windowSizeClass: WindowSizeClassOption = WindowSizeClassOption.COMPACT,
    val historyRecords: List<ScanRecord> = emptyList(),
    val currentToolId: String? = null,
    val toolDetailStates: Map<String, ToolDetailState> = emptyMap(),
    val error: String? = null
) {
    companion object {
        val Initial = OrientationEnforcementState()
    }
}

/**
 * ============================================================
 * ScanPhase — 扫描阶段
 * ============================================================
 */
enum class ScanPhase {
    Idle,       // 初始空闲状态
    Scanning,   // 扫描中
    Completed,  // 扫描完成
    Error       // 扫描出错
}

/**
 * ============================================================
 * AppInfo — App 信息
 * ============================================================
 *
 * @param packageName Package name
 * @param appName App display name
 * @param versionName Version string
 * @param targetSdk Target SDK version
 */
data class AppInfo(
    val packageName: String,
    val appName: String,
    val versionName: String,
    val targetSdk: Int
)

/**
 * ============================================================
 * ScanResult — 扫描结果
 * ============================================================
 *
 * @param totalViolations Total violation count
 * @param warningCount Warning count
 * @param passCount Pass count
 * @param manifestViolations Manifest-based violations
 * @param codeViolations Code-based violations
 * @param scanDurationMs Scan duration in milliseconds
 */
data class ScanResult(
    val totalViolations: Int = 0,
    val warningCount: Int = 0,
    val passCount: Int = 0,
    val manifestViolations: List<ViolationItem> = emptyList(),
    val codeViolations: List<ViolationItem> = emptyList(),
    val scanDurationMs: Long = 0L
)

/**
 * ============================================================
 * ViolationItem — 违规项
 * ============================================================
 *
 * @param id Unique ID
 * @param type Violation type: MANIFEST_ORIENTATION_LOCK, RESIZABLE_ACTIVITY_FALSE, CODE_SET_ORIENTATION, CONFIG_CHANGE_UNHANDLED
 * @param severity Severity: HIGH, MEDIUM, LOW
 * @param filePath File path
 * @param lineNumber Line number
 * @param currentValue Current value
 * @param expectedValue Expected value
 * @param description Description
 * @param fixSuggestion Fix suggestion
 */
data class ViolationItem(
    val id: String,
    val type: ViolationType,
    val severity: ViolationSeverity,
    val filePath: String,
    val lineNumber: Int,
    val currentValue: String,
    val expectedValue: String,
    val description: String,
    val fixSuggestion: String
)

/**
 * ============================================================
 * ViolationType — 违规类型
 * ============================================================
 */
enum class ViolationType(val displayName: String) {
    MANIFEST_ORIENTATION_LOCK("Manifest 方向锁定"),
    RESIZABLE_ACTIVITY_FALSE("resizeableActivity=\"false\""),
    CODE_SET_ORIENTATION("代码 setRequestedOrientation 调用"),
    CONFIG_CHANGE_UNHANDLED("配置变更未处理")
}

/**
 * ============================================================
 * ViolationSeverity — 违规严重程度
 * ============================================================
 */
enum class ViolationSeverity(val displayName: String) {
    HIGH("高"),
    MEDIUM("中"),
    LOW("低")
}

/**
 * ============================================================
 * DiffResult — Diff 结果
 * ============================================================
 *
 * @param before Original code
 * @param after Fixed code
 * @param language Language: kotlin, xml, java
 * @param description Change description
 */
data class DiffResult(
    val before: String,
    val after: String,
    val language: String,
    val description: String
)

/**
 * ============================================================
 * ScanRecord — 扫描记录
 * ============================================================
 *
 * @param id Record ID
 * @param appPackageName App package name
 * @param scanTime Scan timestamp
 * @param totalViolations Total violations
 * @param passRate Pass rate percentage
 */
data class ScanRecord(
    val id: String,
    val appPackageName: String,
    val scanTime: Long,
    val totalViolations: Int,
    val passRate: Int
)

/**
 * ============================================================
 * ToolDetailState — 工具详情状态
 * ============================================================
 */
data class ToolDetailState(
    val toolId: String,
    val toolName: String,
    val toolDescription: String,
    val isRunning: Boolean = false,
    val progress: Float = 0f,
    val report: ToolReport? = null,
    val error: String? = null,
    // Orientation Lock Scanner specific
    val manifestScanItems: List<ManifestScanItem> = emptyList(),
    // Resizable Activity Detector specific
    val resizableActivityItems: List<ResizableActivityItem> = emptyList(),
    // Migration Guide specific
    val migrationDiff: DiffResult? = null,
    val migrationStep: MigrationStep = MigrationStep.MANIFEST_LOCK_REMOVAL,
    // Window Size Class CI specific
    val generatedCIYaml: String? = null,
    // Legacy Migration Path specific
    val migrationPhase: Int = 1,
    val riskMatrix: List<RiskItem> = emptyList(),
    // Accessibility Check specific
    val accessibilityResults: List<AccessibilityResult> = emptyList(),
    // Config Change State Saver specific
    val stateSaveTemplates: List<StateSaveTemplate> = emptyList()
)

/**
 * ============================================================
 * ToolReport — 工具报告
 * ============================================================
 */
data class ToolReport(
    val toolId: String,
    val title: String,
    val summary: String,
    val status: ComplianceStatus,
    val findings: List<Finding> = emptyList(),
    val recommendations: List<String> = emptyList(),
    val generatedAt: Long = System.currentTimeMillis()
)

/**
 * ============================================================
 * Finding — 检测发现
 * ============================================================
 */
data class Finding(
    val severity: String,
    val title: String,
    val description: String,
    val location: String,
    val fixSuggestion: String
)

/**
 * ============================================================
 * ManifestScanItem — Manifest 扫描项
 * ============================================================
 */
data class ManifestScanItem(
    val activityName: String,
    val attribute: String,
    val currentValue: String,
    val isCompliant: Boolean,
    val fixSuggestion: String
)

/**
 * ============================================================
 * ResizableActivityItem — ResizableActivity 检测项
 * ============================================================
 */
data class ResizableActivityItem(
    val activityName: String,
    val currentValue: Boolean,
    val isCompliant: Boolean,
    val killRiskDescription: String,
    val fixSuggestion: String
)

/**
 * ============================================================
 * RiskItem — 风险矩阵项
 * ============================================================
 */
data class RiskItem(
    val phase: String,
    val riskDescription: String,
    val riskLevel: String,
    val mitigation: String
)

/**
 * ============================================================
 * AccessibilityResult — 无障碍检测结果
 * ============================================================
 */
data class AccessibilityResult(
    val checkItem: String,
    val isCompliant: Boolean,
    val description: String,
    val recommendation: String
)

/**
 * ============================================================
 * StateSaveTemplate — 状态保存模板
 * ============================================================
 */
data class StateSaveTemplate(
    val templateName: String,
    val description: String,
    val codeTemplate: String,
    val applicableScenarios: List<String>
)

// ================================================================
// OrientationEnforcementIntent — 用户意图（User Intent）
// ================================================================

/**
 * ============================================================
 * OrientationEnforcementIntent — 用户意图
 * ============================================================
 * Every user action corresponds to an Intent.
 */
sealed interface OrientationEnforcementIntent {

    /** 用户选择 App
     * @param app App info
     */
    data class SelectApp(val app: AppInfo) : OrientationEnforcementIntent

    /** 用户点击开始扫描 */
    data object StartScan : OrientationEnforcementIntent

    /** 用户取消扫描 */
    data object CancelScan : OrientationEnforcementIntent

    /** 用户选择违规项查看详情
     * @param violation Violation item
     */
    data class SelectViolation(val violation: ViolationItem) : OrientationEnforcementIntent

    /** 用户生成修复代码 Diff
     * @param violation Violation item
     */
    data class GenerateDiff(val violation: ViolationItem) : OrientationEnforcementIntent

    /** 用户应用 Diff 修复
     * @param diff Diff result
     */
    data class ApplyDiff(val diff: DiffResult) : OrientationEnforcementIntent

    /** 用户设置迁移向导步骤
     * @param step Migration step
     */
    data class SetMigrationStep(val step: MigrationStep) : OrientationEnforcementIntent

    /** 用户切换窗口大小分类
     * @param wsc Window size class
     */
    data class SetWindowSizeClass(val wsc: WindowSizeClassOption) : OrientationEnforcementIntent

    /** 用户选择工具
     * @param toolId Tool ID
     */
    data class SelectTool(val toolId: String) : OrientationEnforcementIntent

    /** 用户返回仪表盘 */
    data object BackToDashboard : OrientationEnforcementIntent

    /** 用户导出报告
     * @param format Export format: markdown or json
     */
    data class ExportReport(val format: String) : OrientationEnforcementIntent

    /** 用户清除错误 */
    data object ClearError : OrientationEnforcementIntent
}

// ================================================================
// OrientationEnforcementEffect — 一次性副作用（Effect）
// ================================================================

/**
 * ============================================================
 * OrientationEnforcementEffect — 一次性副作用
 * ============================================================
 * One-time events, immutable, can only be consumed once.
 */
sealed interface OrientationEnforcementEffect {

    /** 显示 Toast 消息
     * @param message Toast text
     */
    data class ShowToast(val message: String) : OrientationEnforcementEffect

    /** 复制内容到剪贴板
     * @param content Content to copy
     */
    data class CopyToClipboard(val content: String) : OrientationEnforcementEffect

    /** 分享报告
     * @param content Report content
     * @param title Report title
     */
    data class ShareReport(val content: String, val title: String) : OrientationEnforcementEffect

    /** 导航到违规项详情
     * @param violationId Violation ID
     */
    data class NavigateToViolation(val violationId: String) : OrientationEnforcementEffect
}

// ================================================================
// 默认数据 / Default Data
// ================================================================

/**
 * Default tool list for the dashboard.
 * Each tool has a unique ID, Chinese/English names, description, and initial status.
 */
val defaultOrientationToolStatuses = listOf(
    ToolStatus(
        toolId = ToolId.ORIENTATION_LOCK_SCANNER,
        toolName = "方向锁定扫描器",
        toolNameEn = "Orientation Lock Scanner",
        toolDescription = "扫描 AndroidManifest.xml 和代码中的 screenOrientation 锁定，识别受影响文件和行号",
        status = ComplianceStatus.UNKNOWN,
        iconEmoji = "🔍"
    ),
    ToolStatus(
        toolId = ToolId.RESIZABLE_ACTIVITY_DETECTOR,
        toolName = "ResizableActivity 合规检测",
        toolNameEn = "ResizableActivity Compliance Detector",
        toolDescription = "检测 resizeableActivity=\"false\" 声明，识别会在 Android 17 大屏上被系统杀死的场景",
        status = ComplianceStatus.UNKNOWN,
        iconEmoji = "📐"
    ),
    ToolStatus(
        toolId = ToolId.CONFIG_CHANGE_VALIDATOR,
        toolName = "配置变更状态验证器",
        toolNameEn = "Configuration Change State Validator",
        toolDescription = "检测 App 在屏幕旋转/窗口调整后是否正确调用 onSaveInstanceState，验证状态恢复完整性",
        status = ComplianceStatus.UNKNOWN,
        iconEmoji = "🔄"
    ),
    ToolStatus(
        toolId = ToolId.ORIENTATION_MIGRATION_GUIDE,
        toolName = "方向锁定移除迁移指南",
        toolNameEn = "Orientation Lock Migration Guide",
        toolDescription = "从 portrait-only 迁移到双向布局的渐进路径，Compose/View 双版本代码 Diff",
        status = ComplianceStatus.UNKNOWN,
        iconEmoji = "🛠️"
    ),
    ToolStatus(
        toolId = ToolId.WINDOW_SIZE_CLASS_CI,
        toolName = "Window Size Class CI 检测",
        toolNameEn = "Window Size Class CI Detector",
        toolDescription = "GitHub Actions，自动化验证 App 在 Compact/Medium/Expanded 窗口下的布局正确性",
        status = ComplianceStatus.UNKNOWN,
        iconEmoji = "⚙️"
    ),
    ToolStatus(
        toolId = ToolId.LEGACY_APP_MIGRATION_PATH,
        toolName = "Legacy App 增量迁移路径",
        toolNameEn = "Legacy App Incremental Migration Path",
        toolDescription = "大型 App 分 3 阶段适配：①快速止血检测→②核心流程迁移→③全面适配，含风险评估矩阵",
        status = ComplianceStatus.UNKNOWN,
        iconEmoji = "📊"
    ),
    ToolStatus(
        toolId = ToolId.ACCESSIBILITY_ORIENTATION_CHECK,
        toolName = "双向布局无障碍检测",
        toolNameEn = "Accessibility × Orientation Check",
        toolDescription = "验证双向布局下的 TalkBack/大字体的正确行为",
        status = ComplianceStatus.UNKNOWN,
        iconEmoji = "♿"
    ),
    ToolStatus(
        toolId = ToolId.CONFIG_CHANGE_STATE_SAVER,
        toolName = "配置变更状态保存模板",
        toolNameEn = "Configuration Change State Save Templates",
        toolDescription = "检测 onSaveInstanceState 使用情况，验证状态恢复完整性",
        status = ComplianceStatus.UNKNOWN,
        iconEmoji = "💾"
    )
)

/**
 * Tool status data class for dashboard.
 */
data class ToolStatus(
    val toolId: String,
    val toolName: String,
    val toolNameEn: String,
    val toolDescription: String,
    val status: ComplianceStatus,
    val iconEmoji: String
)
