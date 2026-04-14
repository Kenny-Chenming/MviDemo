package com.mvi.kenny.feature.healthpermissions

// ================================================================
// HealthPermissionsContract — Android 16 细粒度健康权限 MVI 契约
// ================================================================
// MVI architecture contract for Android 16 Body Sensors migration toolkit.
//
// PRD-106: Android 16 细粒度健康权限迁移检测与合规工具包
// Design Reference: memory/agency/designs/PRD-106-Android-16-细粒度健康权限迁移检测与合规工具包.md
//
// MVI 三要素 / Three pillars:
//   Model (State)  — Immutable page state, single source of truth
//   View           — Composable function, consumes State, renders UI
//   Intent         — User intentions, ViewModel processes and updates State
//   Effect         — One-time side effects (navigation, toast), via Channel
// ================================================================

/**
 * ============================================================
 * ScanState — 扫描状态枚举
 * ============================================================
 *
 * @param displayName 中文显示名称
 */
enum class ScanState(val displayName: String) {
    /** 初始空闲状态 */
    Idle("空闲"),
    /** 正在扫描中 */
    Scanning("扫描中"),
    /** 扫描成功完成 */
    Success("已完成"),
    /** 扫描出错 */
    Error("错误")
}

/**
 * ============================================================
 * Severity — 问题严重程度枚举
 * ============================================================
 *
 * @param displayName 中文显示名称
 * @param emoji Emoji representation
 * @param colorHex 颜色值（Compose Color）
 */
enum class Severity(val displayName: String, val emoji: String, val colorHex: Long) {
    /** P0 阻断 — 编译失败或运行时崩溃 */
    P0("P0 阻断", "🔴", 0xFFF44336),
    /** P1 警告 — 行为异常但不会崩溃 */
    P1("P1 警告", "🟡", 0xFFFF9800),
    /** P2 建议 — 最佳实践建议 */
    P2("P2 建议", "🟢", 0xFF4CAF50)
}

/**
 * ============================================================
 * ReportFormat — 报告导出格式枚举
 * ============================================================
 *
 * @param displayName 中文显示名称
 * @param extension 文件扩展名
 */
enum class ReportFormat(val displayName: String, val extension: String) {
    Markdown("Markdown", "md"),
    JSON("JSON", "json"),
    HTML("HTML", "html")
}

/**
 * ============================================================
 * ActiveTab — 当前激活的 Tab
 * ============================================================
 *
 * @param title Tab 显示标题
 */
enum class ActiveTab(val title: String) {
    Scanner("扫描器"),
    Mapping("权限映射"),
    Settings("设置")
}

/**
 * ============================================================
 * ScanProgressStep — 扫描进度步骤枚举
 * ============================================================
 *
 * @param stepNumber 步骤序号
 * @param description 步骤描述
 */
enum class ScanProgressStep(val stepNumber: Int, val description: String) {
    ParsingManifest(1, "解析 Manifest"),
    ScanningCode(2, "扫描代码"),
    AnalyzingDependencies(3, "分析依赖"),
    DetectingConflicts(4, "Health Connect 冲突检测"),
    GeneratingReport(5, "生成报告")
}

/**
 * ============================================================
 * HealthPermissionsState — 健康权限页面状态（MVI State）
 * ============================================================
 * Immutable page state, single source of truth.
 *
 * @param scanState Current scan state
 * @param scanProgress Overall scan progress (0.0 ~ 1.0)
 * @param scanProgressStepDescription Current step description
 * @param currentStepNumber Current step number (1-5)
 * @param lastScanResult Latest scan result
 * @param scanHistory Historical scan records
 * @param activeTab Currently active tab: Scanner / Mapping / Settings
 * @param selectedScanRecordId Selected scan record for detail view
 * @param mappingSearchQuery Permission mapping search query
 * @param permissionMappings Permission mapping list (filtered by search)
 * @param settings Scanner settings
 * @param errorMessage Error message if any
 *
 * @see ScanState
 * @see ActiveTab
 */
data class HealthPermissionsState(
    val scanState: ScanState = ScanState.Idle,
    val scanProgress: Float = 0f,
    val scanProgressStepDescription: String = "",
    val currentStepNumber: Int = 0,
    val lastScanResult: ScanResult? = null,
    val scanHistory: List<ScanRecord> = emptyList(),
    val activeTab: ActiveTab = ActiveTab.Scanner,
    val selectedScanRecordId: String? = null,
    val mappingSearchQuery: String = "",
    val permissionMappings: List<PermissionMapping> = defaultPermissionMappings,
    val settings: HealthPermissionsSettings = HealthPermissionsSettings(),
    val errorMessage: String? = null
) {
    companion object {
        /** Initial/empty state */
        val Initial = HealthPermissionsState()
    }
}

/**
 * ============================================================
 * HealthPermissionsIntent — 用户意图（User Intent）
 * ============================================================
 * Every user action on the page corresponds to an Intent.
 * ViewModel receives Intent, processes business logic, then updates State.
 *
 * @see HealthPermissionsViewModel.sendIntent handles all Intents
 */
sealed interface HealthPermissionsIntent {

    /** 用户点击"开始扫描"按钮 */
    data object StartScan : HealthPermissionsIntent

    /** 用户取消正在进行的扫描 */
    data object CancelScan : HealthPermissionsIntent

    /** 用户点击查看某次扫描记录
     * @param recordId Scan record ID
     */
    data class LoadScanDetail(val recordId: String) : HealthPermissionsIntent

    /** 用户切换 Tab
     * @param tab Target tab
     */
    data class SwitchTab(val tab: ActiveTab) : HealthPermissionsIntent

    /** 用户标记某个问题为已修复
     * @param issueId Issue ID
     */
    data class MarkIssueFixed(val issueId: String) : HealthPermissionsIntent

    /** 用户忽略某个问题
     * @param issueId Issue ID
     */
    data class DismissIssue(val issueId: String) : HealthPermissionsIntent

    /** 用户在权限映射 Tab 搜索
     * @param query Search query
     */
    data class SearchMappings(val query: String) : HealthPermissionsIntent

    /** 用户更新设置
     * @param settings Updated settings
     */
    data class UpdateSettings(val settings: HealthPermissionsSettings) : HealthPermissionsIntent

    /** 用户导出报告
     * @param format Export format
     */
    data class ExportReport(val format: ReportFormat) : HealthPermissionsIntent

    /** 用户清除错误消息 */
    data object ClearError : HealthPermissionsIntent

    /** 用户下拉刷新（重新扫描） */
    data object RefreshScan : HealthPermissionsIntent
}

/**
 * ============================================================
 * HealthPermissionsEffect — 一次性副作用（Effect）
 * ============================================================
 * One-time events, immutable, can only be consumed once.
 * UI layer listens via LaunchedEffect + flow.collect{}.
 *
 * @see HealthPermissionsViewModel _effect.send() sends Effects
 */
sealed interface HealthPermissionsEffect {

    /** 扫描完成
     * @param scanId Scan result ID
     */
    data class ScanComplete(val scanId: String) : HealthPermissionsEffect

    /** 导航到扫描详情页
     * @param scanId Scan record ID
     */
    data class NavigateToScanDetail(val scanId: String) : HealthPermissionsEffect

    /** 导出报告成功
     * @param filePath Exported file path
     */
    data class ExportSuccess(val filePath: String) : HealthPermissionsEffect

    /** 显示错误
     * @param message Error message
     */
    data class ShowError(val message: String) : HealthPermissionsEffect

    /** 显示 Snackbar 消息
     * @param message Snackbar text
     */
    data class ShowSnackbar(val message: String) : HealthPermissionsEffect
}

// ================================================================
// 数据模型 / Data Models
// ================================================================

/**
 * ============================================================
 * ScanResult — 单次扫描结果
 * ============================================================
 *
 * @param id Unique scan ID (timestamp-based)
 * @param timestamp Scan timestamp (milliseconds since epoch)
 * @param score Compliance score (0-100)
 * @param issueCounts Issue count by severity
 * @param declarations All BODY_SENSORS permission declarations found
 * @param codePaths All code paths that use BODY_SENSORS permissions
 * @param conflicts Health Connect + native permission conflicts
 * @param sdkReport Google Fit / Samsung Health SDK compatibility report
 */
data class ScanResult(
    val id: String,
    val timestamp: Long,
    val score: Int,
    val issueCounts: IssueCounts,
    val declarations: List<PermissionDeclaration>,
    val codePaths: List<CodePathIssue>,
    val conflicts: List<HealthConnectConflict>,
    val sdkReport: SdkCompatibilityReport?
)

/**
 * ============================================================
 * ScanRecord — 扫描历史记录（轻量版结果）
 * ============================================================
 *
 * @param id Unique scan ID
 * @param timestamp Scan timestamp
 * @param score Compliance score
 * @param issueCounts Issue count by severity
 * @param isNew Whether this is a new scan result
 */
data class ScanRecord(
    val id: String,
    val timestamp: Long,
    val score: Int,
    val issueCounts: IssueCounts,
    val isNew: Boolean = false
)

/**
 * ============================================================
 * IssueCounts — 问题数量统计
 * ============================================================
 *
 * @param p0Count P0 blocking issues count
 * @param p1Count P1 warning issues count
 * @param p2Count P2 suggestion issues count
 */
data class IssueCounts(
    val p0Count: Int = 0,
    val p1Count: Int = 0,
    val p2Count: Int = 0
) {
    val total: Int get() = p0Count + p1Count + p2Count
}

/**
 * ============================================================
 * PermissionDeclaration — Manifest 权限声明
 * ============================================================
 *
 * @param permission Old permission name (e.g. "BODY_SENSORS")
 * @param filePath AndroidManifest.xml path
 * @param lineNumber Line number in manifest
 * @param suggestedNewPermissions Recommended new permissions
 * @param severity Issue severity
 */
data class PermissionDeclaration(
    val permission: String,
    val filePath: String,
    val lineNumber: Int,
    val suggestedNewPermissions: List<String>,
    val severity: Severity
)

/**
 * ============================================================
 * CodePathIssue — 代码路径问题
 * ============================================================
 *
 * @param id Unique issue ID
 * @param permission Permission being used
 * @param filePath Source file path
 * @param lineNumber Line number
 * @param codeSnippet Code snippet (max 3 lines)
 * @param affectedComponent Affected Activity/Fragment/Class name
 * @param suggestedFix Suggested fix description
 * @param severity Issue severity
 * @param isFixed Whether this issue has been marked as fixed
 * @param isDismissed Whether this issue has been dismissed by user
 */
data class CodePathIssue(
    val id: String,
    val permission: String,
    val filePath: String,
    val lineNumber: Int,
    val codeSnippet: String,
    val affectedComponent: String,
    val suggestedFix: String,
    val severity: Severity,
    val isFixed: Boolean = false,
    val isDismissed: Boolean = false
)

/**
 * ============================================================
 * HealthConnectConflict — Health Connect 与原生权限冲突
 * ============================================================
 *
 * @param dataType Data type with conflict (e.g. "Heart Rate")
 * @param nativePermission Native permission used
 * @param healthConnectDataType Corresponding Health Connect data type
 * @param suggestion Resolution suggestion
 */
data class HealthConnectConflict(
    val dataType: String,
    val nativePermission: String,
    val healthConnectDataType: String,
    val suggestion: String
)

/**
 * ============================================================
 * SdkCompatibilityReport — SDK 兼容性报告
 * ============================================================
 *
 * @param googleFitVersion Google Fit SDK version if present
 * @param samsungHealthVersion Samsung Health SDK version if present
 * @param isGoogleFitCompatible Whether Google Fit SDK is compatible
 * @param isSamsungHealthCompatible Whether Samsung Health SDK is compatible
 * @param googleFitNote Compatibility note for Google Fit
 * @param samsungHealthNote Compatibility note for Samsung Health
 */
data class SdkCompatibilityReport(
    val googleFitVersion: String? = null,
    val samsungHealthVersion: String? = null,
    val isGoogleFitCompatible: Boolean = true,
    val isSamsungHealthCompatible: Boolean = true,
    val googleFitNote: String = "",
    val samsungHealthNote: String = ""
)

/**
 * ============================================================
 * PermissionMapping — 旧新权限映射
 * ============================================================
 *
 * @param oldPermission Old permission name (e.g. "BODY_SENSORS")
 * @param newPermissions Corresponding new permissions (one old → multiple new)
 * @param description Usage scenario description
 * @param apiLevel Required API level
 */
data class PermissionMapping(
    val oldPermission: String,
    val newPermissions: List<String>,
    val description: String,
    val apiLevel: Int
)

/**
 * ============================================================
 * HealthPermissionsSettings — 扫描设置
 * ============================================================
 *
 * @param includeTestCode Whether to include test code in scan
 * @param includeAarDependencies Whether to scan AAR dependencies
 * @param targetSdkOverride Target SDK override (null = from build.gradle)
 * @param exportFormat Default export format
 */
data class HealthPermissionsSettings(
    val includeTestCode: Boolean = true,
    val includeAarDependencies: Boolean = false,
    val targetSdkOverride: Int? = null,
    val exportFormat: ReportFormat = ReportFormat.Markdown
)

// ================================================================
// 默认权限映射数据 / Default Permission Mappings
// ================================================================

/**
 * ============================================================
 * defaultPermissionMappings — Android 16 BODY_SENSORS 迁移映射表
 * ============================================================
 * Android 16 (API 36) deprecated BODY_SENSORS and BODY_SENSORS_BACKGROUND.
 * This table maps old permissions to new granular health permissions.
 *
 * Reference: developer.android.com/about/versions/16/summary
 *            developer.android.com/health-and-fitness/health-services/permissions
 */
val defaultPermissionMappings = listOf(
    PermissionMapping(
        oldPermission = "android.permission.BODY_SENSORS",
        newPermissions = listOf(
            "android.permission.health.HEALTH_HEART_RATE",
            "android.permission.health.HEALTH_BLOOD_GLUCOSE",
            "android.permission.health.HEALTH_BLOOD_PRESSURE",
            "android.permission.health.HEALTH_BODY_TEMPERATURE",
            "android.permission.health.HEALTH_OXYGEN_SATURATION",
            "android.permission.health.HEALTH_RESPIRATORY_RATE"
        ),
        description = "Body sensors (心率/血糖/血压/体温/血氧/呼吸频率) — 细粒度按类型授权",
        apiLevel = 36
    ),
    PermissionMapping(
        oldPermission = "android.permission.BODY_SENSORS_BACKGROUND",
        newPermissions = listOf(
            "android.permission.health.HEALTH_HEART_RATE_BACKGROUND",
            "android.permission.health.HEALTH_BLOOD_GLUCOSE_BACKGROUND",
            "android.permission.health.HEALTH_BLOOD_PRESSURE_BACKGROUND",
            "android.permission.health.HEALTH_BODY_TEMPERATURE_BACKGROUND",
            "android.permission.health.HEALTH_OXYGEN_SATURATION_BACKGROUND",
            "android.permission.health.HEALTH_RESPIRATORY_RATE_BACKGROUND"
        ),
        description = "后台身体传感器 — 需要额外申请细粒度后台权限",
        apiLevel = 36
    ),
    PermissionMapping(
        oldPermission = "android.permission.BODY_SENSORS",
        newPermissions = listOf("android.permission.health.HEALTH_STEPS"),
        description = "计步数据 — 从 BODY_SENSORS 独立为单独权限",
        apiLevel = 36
    ),
    PermissionMapping(
        oldPermission = "android.permission.BODY_SENSORS",
        newPermissions = listOf("android.permission.health.HEALTH_ACTIVITY_RECOGNITION"),
        description = "活动识别（行走/跑步/骑行）— 新增独立权限",
        apiLevel = 36
    )
)
