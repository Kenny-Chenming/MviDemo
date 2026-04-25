package com.mvi.kenny.feature.composemigration

import java.io.File

/**
 * ============================================================
 * ComposeMigrationContract — PRD-162 MVI Contract
 * Compose 1.12.0 Migration 开发工具包
 * ============================================================
 * 
 * This contract defines the complete MVI architecture for the Compose 1.12.0
 * Migration Toolbox, which provides automated tooling for the most significant
 * build chain breaking change in Compose history (AGP 9 + SDK 37 mandatory).
 * 
 * Design Doc: memory/agency/designs/PRD-162-Compose-1.12.0-Migration-开发工具包.md
 * Status: 设计完成，待移交开发
 * 
 * MVI Architecture:
 * — State: Immutable data class representing UI state
 * — Intent: User actions / events that trigger state changes
 * — Effect: One-time side effects (navigation, toasts, etc.)
 */

// ============================================================
// Tab Types / 工具分Tab枚举
// ============================================================
/**
 * Seven tool tabs corresponding to the 8 core functionalities:
 * 合规检测 | 迁移扫描 | KMP向导 | KSP迁移 | API替换 | Kotlin合规 | 验证套件
 */
enum class MigrationTab {
    COMPLIANCE,        // 合规检测 — Check compileSdkVersion, AGP version, Compose BOM
    MIGRATION_SCAN,    // 迁移扫描 — AGP 8→9 automated migration diff
    KMP_WIZARD,        // KMP向导 — KMP project split wizard (androidApp/composeApp)
    KSP_MIGRATION,     // KSP迁移 — kapt → KSP automated migration
    API_REPLACE,       // API替换 — applicationVariants → androidComponents API
    KOTLIN_COMPLIANCE, // Kotlin合规 — AGP 9 built-in Kotlin compliance check
    VERIFICATION       // 验证套件 — Compose 1.12.0 upgrade verification suite
}

// ============================================================
// Scan Phase / 扫描阶段进度
// ============================================================
/**
 * Scan progress phases for animated progress bar
 * 扫描动画进度条各阶段
 */
enum class ScanPhase {
    IDLE,               // 空闲状态
    ANALYZING_GRADLE,   // "正在分析 Gradle 配置…"
    CHECKING_AGP,       // "正在检查 AGP 版本…"
    CHECKING_COMPOSE,   // "正在检查 Compose 依赖…"
    CHECKING_KSP,       // "正在检查 KSP 配置…"
    GENERATING_REPORT   // "正在生成报告…"
}

// ============================================================
// Compliance Status / 合规状态
// ============================================================
enum class ComplianceStatus {
    PASS,   // 绿灯 — 全部合规
    WARN,   // 黄灯 — 有警告
    FAIL    // 红灯 — 不合规
}

// ============================================================
// State / 界面状态
// ============================================================
/**
 * MigrationToolboxState — Complete UI state for the Migration Toolbox
 * 
 * Tab structure:
 * — COMPLIANCE: Project info card + compliance status indicator
 * — MIGRATION_SCAN: Diff viewer with Before/After columns
 * — KMP_WIZARD: Stepper wizard (4 steps)
 * — KSP_MIGRATION: kapt → KSP migration list
 * — API_REPLACE: applicationVariants usage locations
 * — KOTLIN_COMPLIANCE: Redundant kotlin-android plugin check
 * — VERIFICATION: Checklist with pass/fail status
 */
data class ComposeMigrationState(
    // Navigation / 导航
    val selectedTab: MigrationTab = MigrationTab.COMPLIANCE,
    
    // Project Info / 项目信息
    val projectPath: String = "",
    val compileSdkVersion: Int? = null,
    val agpVersion: String? = null,
    val composeBomVersion: String? = null,
    val kotlinVersion: String? = null,
    
    // Scan Progress / 扫描进度
    val scanInProgress: Boolean = false,
    val scanPhase: ScanPhase = ScanPhase.IDLE,
    val scanProgress: Float = 0f,        // 0.0 ~ 1.0
    
    // Compliance Result / 合规检测结果
    val complianceStatus: ComplianceStatus = ComplianceStatus.PASS,
    val complianceItems: List<ComplianceItem> = emptyList(),
    
    // Migration Scan Result / 迁移扫描结果
    val diffResults: List<DiffResult> = emptyList(),
    val selectedDiff: DiffResult? = null,
    
    // KMP Wizard / KMP向导
    val kmpWizardStep: Int = 0,           // 0-3
    val kmpDetectedModules: List<String> = emptyList(),
    val kmpAndroidAppName: String = "androidApp",
    val kmpComposeAppName: String = "composeApp",
    val kmpPreviewStructure: String = "",
    
    // KSP Migration / KSP迁移
    val kspMigrations: List<KspMigrationItem> = emptyList(),
    val kspMappingTable: Map<String, String> = defaultKspMapping,
    
    // API Replace / API替换
    val apiMigrations: List<ApiMigrationItem> = emptyList(),
    
    // Kotlin Compliance / Kotlin合规
    val kotlinComplianceResult: KotlinComplianceResult? = null,
    
    // Verification Suite / 验证套件
    val verificationItems: List<VerificationItem> = emptyList(),
    
    // Report Export / 报告导出
    val reportExportFormat: ReportFormat = ReportFormat.HTML,
    
    // Error / 错误
    val errorMessage: String? = null,
    
    // CI Mode / CI模式
    val isCiMode: Boolean = false
)

/**
 * Compliance item for COMPLIANCE tab
 * 单个合规检测项
 */
data class ComplianceItem(
    val id: String,
    val title: String,        // e.g. "Compile SDK Version"
    val status: ComplianceStatus,
    val currentValue: String, // e.g. "36"
    val requiredValue: String, // e.g. "37"
    val description: String,
    val fileLocation: String? = null,
    val fixSuggestion: String? = null
)

/**
 * Diff result for MIGRATION_SCAN tab
 * 迁移Diff项（Before/After对比）
 */
data class DiffResult(
    val id: String,
    val filePath: String,
    val fileName: String,
    val changeType: DiffChangeType,
    val beforeLines: List<String>,
    val afterLines: List<String>,
    val lineNumber: Int,
    val canAutoApply: Boolean = true
)

enum class DiffChangeType {
    ADD,      // 新增行（绿色）
    REMOVE,   // 删除行（红色）
    MODIFY    // 修改行
}

/**
 * KSP migration item for KSP_MIGRATION tab
 * kapt → KSP 迁移项
 */
data class KspMigrationItem(
    val id: String,
    val kaptDependency: String,      // e.g. "androidx.room:room-compiler:2.6.1"
    val suggestedKspPlugin: String,   // e.g. "com.google.devtools.ksp"
    val suggestedProcessor: String,   // e.g. "androidx.room:room-ksp"
    val fileLocation: String,
    val lineNumber: Int,
    val isBreakingChange: Boolean = false
)

/**
 * API migration item for API_REPLACE tab
 * applicationVariants → androidComponents API 替换项
 */
data class ApiMigrationItem(
    val id: String,
    val filePath: String,
    val lineNumber: Int,
    val currentCode: String,
    val suggestedReplacement: String,
    val description: String
)

/**
 * Kotlin compliance result for KOTLIN_COMPLIANCE tab
 * Kotlin 合规检测结果
 */
data class KotlinComplianceResult(
    val hasRedundantKotlinPlugin: Boolean,
    val redundantPluginDeclarations: List<PluginDeclaration>,
    val overallStatus: ComplianceStatus
)

data class PluginDeclaration(
    val filePath: String,
    val lineNumber: Int,
    val pluginId: String,
    val suggestedAction: String
)

/**
 * Verification item for VERIFICATION tab
 * 升级验证套件项
 */
data class VerificationItem(
    val id: String,
    val title: String,
    val description: String,
    val isChecked: Boolean = false,
    val status: VerificationStatus = VerificationStatus.PENDING
)

enum class VerificationStatus {
    PENDING,  // 待检查
    PASS,     // 通过
    FAIL,     // 失败
    SKIP      // 跳过
}

/**
 * Report export format
 * 报告导出格式
 */
enum class ReportFormat {
    HTML,     // HTML可视化报告
    JSON,     // 机器可读JSON
    MARKDOWN  // Markdown格式
}

// ============================================================
// Intent / 用户意图
// ============================================================
/**
 * MigrationIntent — All user actions that can trigger state changes
 */
sealed class ComposeMigrationIntent {
    // Navigation / 导航
    data class SelectTab(val tab: MigrationTab) : ComposeMigrationIntent()
    
    // Project / 项目
    data class UpdateProjectPath(val path: String) : ComposeMigrationIntent()
    data class SelectProject(val path: String) : ComposeMigrationIntent()
    
    // Scan / 扫描
    data object StartScan : ComposeMigrationIntent()
    data object CancelScan : ComposeMigrationIntent()
    data object RunCiScan : ComposeMigrationIntent()
    
    // Diff / Diff操作
    data class SelectDiff(val diff: DiffResult) : ComposeMigrationIntent()
    data class ApplyDiff(val diffId: String) : ComposeMigrationIntent()
    data object ApplyAllDiffs : ComposeMigrationIntent()
    
    // KMP Wizard / KMP向导
    data class UpdateKmpWizardStep(val step: Int) : ComposeMigrationIntent()
    data class UpdateAndroidAppName(val name: String) : ComposeMigrationIntent()
    data class UpdateComposeAppName(val name: String) : ComposeMigrationIntent()
    data object KmpWizardNextStep : ComposeMigrationIntent()
    data object ExecuteKmpWizard : ComposeMigrationIntent()
    
    // Verification / 验证
    data class ToggleVerificationItem(val itemId: String) : ComposeMigrationIntent()
    data object RunVerification : ComposeMigrationIntent()
    
    // Export / 导出
    data class SetExportFormat(val format: ReportFormat) : ComposeMigrationIntent()
    data object ExportReport : ComposeMigrationIntent()
    
    // Error / 错误处理
    data object DismissError : ComposeMigrationIntent()
}

// ============================================================
// Effect / 副作用
// ============================================================
/**
 * MigrationEffect — One-time side effects (not part of state)
 * These are events that should be handled once (toasts, navigation, etc.)
 */
sealed class ComposeMigrationEffect {
    // Toast notification / 轻量提示
    data class ShowToast(val message: String) : ComposeMigrationEffect()
    
    // Open file at specific line / 跳转到文件
    data class OpenFile(val path: String, val line: Int) : ComposeMigrationEffect()
    
    // Share/send report / 分享报告
    data class ShareReport(val file: File) : ComposeMigrationEffect()
    
    // Scan complete event / 扫描完成
    data object ScanComplete : ComposeMigrationEffect()
    
    // Diff applied / Diff已应用
    data class DiffApplied(val diffId: String) : ComposeMigrationEffect()
    
    // Error event / 错误事件
    data class ShowError(val message: String) : ComposeMigrationEffect()
    
    // KMP wizard complete / KMP向导完成
    data object KmpWizardComplete : ComposeMigrationEffect()
    
    // Report exported / 报告已导出
    data class ReportExported(val format: ReportFormat, val filePath: String) : ComposeMigrationEffect()
}

// ============================================================
// Default KSP Mapping Table / 默认KSP映射表
// ============================================================
/**
 * Default kapt → KSP mapping table
 * Key: kapt plugin ID, Value: KSP plugin ID
 * 
 * 常见的 kapt 依赖到 KSP processor 的映射关系
 */
val defaultKspMapping: Map<String, String> = mapOf(
    // Room
    "androidx.room:room-compiler" to "androidx.room:room-ksp",
    
    // Hilt
    "com.google.dagger:hilt-android-compiler" to "com.google.dagger:hilt-compiler",
    
    // Navigation
    "androidx.navigation:navigation-safe-args-gradle-plugin" to "androidx.navigation:navigation-safe-args-gradle-plugin (KSP version TBD)",
    
    // Kotlin Metadata / Serialization
    "org.jetbrains.kotlin:kotlin-metadata" to "org.jetbrains.kotlin:kotlin-metadata (KSP version TBD)",
    
    // KSP itself is the replacement for kapt
    "org.jetbrains.kotlin:kotlin-kapt" to "com.google.devtools.ksp"
)

// ============================================================
// Key Constants / 关键常量
// ============================================================
object ComposeMigrationConstants {
    /** AGP 9.0 is the minimum required version for Compose 1.12.0 */
    const val MIN_AGP_VERSION = "9.0.0"
    
    /** Compose 1.12.0 requires compileSdk 37 minimum */
    const val MIN_COMPILE_SDK = 37
    
    /** Compose 1.12.0 BOM version */
    const val COMPOSE_1_12_BOM = "2026.05.00"  // Placeholder — update when official BOM is released
    
    /** AGP 9 built-in Kotlin — redundant plugin ID */
    const val REDUNDANT_KOTLIN_PLUGIN = "org.jetbrains.kotlin.android"
    
    /** Color theme — Primary Indigo 500 */
    const val THEME_COLOR_PRIMARY = 0xFF6366F1
    
    /** Color — Pass Green 500 */
    const val COLOR_PASS = 0xFF22C55E
    
    /** Color — Warn Amber 500 */
    const val COLOR_WARN = 0xFFF59E0B
    
    /** Color — Fail Red 500 */
    const val COLOR_FAIL = 0xFFEF4444
}
