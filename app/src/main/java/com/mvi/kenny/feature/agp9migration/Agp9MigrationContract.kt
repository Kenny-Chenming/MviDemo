package com.mvi.kenny.feature.agp9migration

/**
 * PRD-082 | AGP 9.0 KMP NDK/C++ Migration Detection & Auto-Fix Toolkit
 * MVI Contract — Defines State, Intent, and Effect
 *
 * Design Doc: memory/agency/designs/PRD-082-AGP-9-KMP-NDK-CPP-迁移检测与自动化修复工具包.md
 * Status: 设计完成，待移交开发 | Completed: 2026-04-11
 */

// ============ Data Models ============
// ============ 数据模型 ============

/**
 * Scan phase during full project analysis
 * 完整扫描过程中的各阶段
 */
enum class ScanPhase(val label: String) {
    PARSING_MODULES("解析模块结构 / Parsing module structure"),
    DETECTING_NDK("检测 NDK/C++ 用法 / Detecting NDK/C++ usage"),
    CHECKING_ANDROID_BLOCK("检查 android{} 配置 / Checking android{} config"),
    CHECKING_KSP("检查 KSP 兼容性 / Checking KSP compatibility"),
    GENERATING_REPORT("生成报告 / Generating report")
}

/**
 * Severity level for migration issues
 * 迁移问题严重级别
 */
enum class Severity { NONE, P2, P1, P0 }

/**
 * Migration status per module
 * 单个模块的迁移状态
 */
enum class MigrationStatus { NOT_STARTED, IN_PROGRESS, COMPLETED, BLOCKED }

/**
 * Overall scan status
 * 整体扫描状态
 */
enum class ScanStatus { IDLE, SCANNING, COMPLETED, ERROR }

/**
 * Native file information in a KMP module
 * KMP 模块中的原生文件信息
 */
data class NativeFileInfo(
    val fileName: String,           // e.g., "jni.cpp"
    val filePath: String,           // Full relative path
    val calledFromKotlin: List<String>  // Kotlin files that call this native code
)

/**
 * NDK/C++ isolation item for the isolator engine
 * NDK/C++ 隔离引擎的处理项
 */
data class NDKIsolationItem(
    val moduleName: String,
    val modulePath: String,
    val nativeFiles: List<NativeFileInfo>,
    val suggestedTarget: String,  // "application module" or a new module name
    val hasApplicationModule: Boolean,
    val generatedInterface: String? = null
)

/**
 * android{} block migration item
 * android{} 块迁移项
 */
data class AndroidBlockItem(
    val sourceModule: String,
    val sourceModulePath: String,
    val currentAndroidBlock: String,
    val suggestedAndroidBlock: String,
    val removedProperties: List<String>,
    val targetModuleName: String
)

/**
 * KSP compatibility item
 * KSP 兼容性问题项
 */
data class KSPCheckItem(
    val moduleName: String,
    val modulePath: String,
    val issue: String,
    val suggestion: String,
    val severity: Severity
)

/**
 * Module summary item for dashboard list
 * 仪表盘模块列表项
 */
data class ModuleItem(
    val name: String,
    val path: String,
    val hasNDK: Boolean,
    val hasAndroidBlockIssue: Boolean,
    val hasKSPIssue: Boolean,
    val severity: Severity,
    val status: MigrationStatus
)

/**
 * Migration progress summary
 * 迁移进度摘要
 */
data class MigrationProgress(
    val totalModules: Int,
    val completedModules: Int,
    val inProgressModules: Int,
    val blockedModules: Int
) {
    val overallPercent: Int
        get() = if (totalModules == 0) 0 else (completedModules * 100) / totalModules
}

/**
 * Report export format
 * 报告导出格式
 */
enum class ReportFormat { HTML, JSON, MARKDOWN }

/**
 * Tool settings
 * 工具设置
 */
data class AgpSettings(
    val targetAGPVersion: String = "9.0",
    val targetKotlinVersion: String = "2.4",
    val reportFormat: ReportFormat = ReportFormat.HTML,
    val autoBackup: Boolean = true
)

// ============ State ============
// ============ 状态定义 ============

/**
 * Overall UI state for AGP 9.0 Migration Toolkit
 * AGP 9.0 迁移工具包的整体 UI 状态
 */
data class Agp9MigrationState(
    // Scan / 扫描状态
    val scanStatus: ScanStatus = ScanStatus.IDLE,
    val currentPhase: ScanPhase? = null,
    val scanPhases: List<ScanPhase> = emptyList(),
    val scanProgress: Float = 0f,

    // Results / 扫描结果
    val overallHealthScore: Int = 0,  // 0-100
    val moduleItems: List<ModuleItem> = emptyList(),
    val ndkIsolationItems: List<NDKIsolationItem> = emptyList(),
    val androidBlockItems: List<AndroidBlockItem> = emptyList(),
    val kspCheckItems: List<KSPCheckItem> = emptyList(),

    // Selection / 选中项
    val selectedModule: ModuleItem? = null,
    val selectedNDKItem: NDKIsolationItem? = null,
    val selectedAndroidBlockItem: AndroidBlockItem? = null,

    // Progress / 迁移进度
    val migrationProgress: MigrationProgress = MigrationProgress(0, 0, 0, 0),

    // Settings / 设置
    val settings: AgpSettings = AgpSettings(),

    // UI state / 界面状态
    val isNDKDialogOpen: Boolean = false,
    val isAndroidBlockDialogOpen: Boolean = false,
    val isConfirmDialogOpen: Boolean = false,
    val confirmDialogMessage: String = "",
    val confirmDialogOnConfirm: (() -> Unit)? = null,
    val errorMessage: String? = null
)

// ============ Intent ============
// ============ 用户意图 ============

sealed class Agp9MigrationIntent {
    // Scan / 扫描
    object RunFullScan : Agp9MigrationIntent()
    object CancelScan : Agp9MigrationIntent()

    // Module selection / 模块选择
    data class SelectModule(val module: ModuleItem) : Agp9MigrationIntent()
    object ClearModuleSelection : Agp9MigrationIntent()

    // NDK Isolation / NDK 隔离
    data class SelectNDKItem(val item: NDKIsolationItem) : Agp9MigrationIntent()
    data class StartNDKIsolation(val item: NDKIsolationItem, val targetModule: String) : Agp9MigrationIntent()
    data class PreviewAndroidBlockMigration(val item: AndroidBlockItem) : Agp9MigrationIntent()
    object DismissNDKDialog : Agp9MigrationIntent()
    object DismissAndroidBlockDialog : Agp9MigrationIntent()

    // Android Block Migration / android{} 迁移
    data class ApplyAndroidBlockMigration(val item: AndroidBlockItem) : Agp9MigrationIntent()

    // Settings / 设置
    data class UpdateSettings(val settings: AgpSettings) : Agp9MigrationIntent()

    // Report / 报告
    data class ExportReport(val format: ReportFormat) : Agp9MigrationIntent()

    // Error / 错误处理
    object DismissError : Agp9MigrationIntent()
    object DismissConfirmDialog : Agp9MigrationIntent()
}

// ============ Effect ============
// ============ 副作用（一次性事件）===========

sealed class Agp9MigrationEffect {
    data class ShowSnackbar(
        val message: String,
        val isError: Boolean = false
    ) : Agp9MigrationEffect()

    data class ReportGenerated(
        val filePath: String,
        val format: ReportFormat
    ) : Agp9MigrationEffect()

    data class ShowConfirmDialog(
        val title: String,
        val message: String,
        val onConfirm: () -> Unit
    ) : Agp9MigrationEffect()

    object MigrationCompleted : Agp9MigrationEffect()

    data class NavigateToModuleScan(
        val modulePath: String
    ) : Agp9MigrationEffect()
}
