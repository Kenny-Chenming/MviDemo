package com.mvi.kenny.feature.kmpnewstructuremigration

import androidx.compose.ui.graphics.Color

/**
 * ============================================================
 * KMP New Structure Migration — MVI Contract
 * KMP 新默认项目结构迁移工具包 — MVI 契约层
 * ============================================================
 *
 * PRD-299 | AGP 9.0 新 KMP 项目结构迁移工具包
 *
 * AGP 9.0 (2026年1月发布) 强制要求 Android 应用入口点必须与共享代码
 * 分离到独立模块。本工具帮助企业 KMP 项目完成自动化结构迁移。
 *
 * Reference: memory/agency/designs/PRD-299-KMP-AGP9-Project-Structure-Migration-Toolkit.md
 */

/** 迁移阶段枚举 — Migration Phase Enum */
enum class MigrationPhase {
    /** 初始状态，等待用户输入项目路径 */
    IDLE,
    /** 正在扫描项目结构 */
    SCANNING,
    /** 扫描完成，展示检测结果 */
    SCAN_COMPLETE,
    /** 正在执行迁移 */
    MIGRATING,
    /** 迁移完成，验证编译结果 */
    VERIFYING,
    /** 全部完成 */
    COMPLETE,
    /** 发生错误 */
    ERROR
}

/** AGP 9.0 合规检测结果 — AGP 9.0 Compliance Check Result */
data class StructureIssue(
    val issueId: String,
    val severity: IssueSeverity,
    val description: String,
    val descriptionEn: String,
    val filePath: String,
    val currentValue: String,
    val suggestedFix: String,
    val suggestedFixEn: String
)

enum class IssueSeverity {
    /** 严重问题：阻断迁移，必须先修复 */
    BLOCKER,
    /** 警告：可能影响功能，需人工确认 */
    WARNING,
    /** 提示：建议改进，非强制 */
    INFO
}

/** 迁移任务项 — Migration Task Item */
data class MigrationTask(
    val taskId: String,
    val title: String,
    val titleEn: String,
    val description: String,
    val descriptionEn: String,
    val status: TaskStatus,
    val diffPreview: String? = null
)

enum class TaskStatus {
    PENDING,
    IN_PROGRESS,
    SUCCESS,
    FAILED,
    SKIPPED
}

/** 项目扫描结果 — Project Scan Result */
data class ScanResult(
    val projectPath: String,
    val projectName: String,
    val currentStructure: ProjectStructure,
    val issues: List<StructureIssue>,
    val migrationTasks: List<MigrationTask>,
    val isCompliant: Boolean,
    val complianceScore: Int // 0-100
)

/** 当前项目结构信息 — Current Project Structure Info */
data class ProjectStructure(
    val settingsFile: String?,        // settings.gradle.kts
    val rootBuildFile: String?,        // build.gradle.kts (root)
    val appModuleExists: Boolean,       // androidApp module exists
    val composeAppModuleExists: Boolean, // composeApp module exists
    val hasSeparatedEntry: Boolean,     // androidApp contains only entry point
    val gradleVersion: String?,
    val agpVersion: String?,
    val kotlinVersion: String?
)

/** 迁移结果 — Migration Result */
data class MigrationResult(
    val totalTasks: Int,
    val succeeded: Int,
    val failed: Int,
    val skipped: Int,
    val verificationPassed: Boolean,
    val compileOutput: String,
    val errors: List<String>,
    val warnings: List<String>
)

// ─────────────────────────────────────────────
// UI State — 界面状态
// ─────────────────────────────────────────────

/**
 * 主界面状态 — Main Screen State
 * All UI state for the KMP New Structure Migration screen.
 */
data class KMPNewStructureMigrationState(
    /** 当前用户输入的项目路径 */
    val projectPathInput: String = "",
    /** 是否正在处理（显示加载状态）*/
    val isProcessing: Boolean = false,
    /** 当前迁移阶段 */
    val phase: MigrationPhase = MigrationPhase.IDLE,
    /** 扫描结果（扫描完成后填充）*/
    val scanResult: ScanResult? = null,
    /** 迁移结果（迁移完成后填充）*/
    val migrationResult: MigrationResult? = null,
    /** 是否为 dry-run 预览模式 */
    val isDryRun: Boolean = true,
    /** 错误消息 */
    val errorMessage: String? = null,
    /** 用户确认继续迁移 */
    val userConfirmedMigration: Boolean = false
)

// ─────────────────────────────────────────────
// UI Intent — 用户意图
// ─────────────────────────────────────────────

/**
 * 用户操作意图 — User Intent
 * Sealed class representing all possible user actions.
 */
sealed class KMPNewStructureMigrationIntent {
    data class UpdateProjectPath(val path: String) : KMPNewStructureMigrationIntent()
    data class SetDryRun(val enabled: Boolean) : KMPNewStructureMigrationIntent()
    data object StartScan : KMPNewStructureMigrationIntent()
    data object StartMigration : KMPNewStructureMigrationIntent()
    data object ConfirmMigration : KMPNewStructureMigrationIntent()
    data object Reset : KMPNewStructureMigrationIntent()
    data object DismissError : KMPNewStructureMigrationIntent()
}

// ─────────────────────────────────────────────
// UI Effect — 副作用
// ─────────────────────────────────────────────

/**
 * 一次性副作用 — One-time Side Effects
 * Effects that should be handled once (navigation, snackbar, etc.)
 */
sealed class KMPNewStructureMigrationEffect {
    data class ShowSnackbar(val message: String, val isError: Boolean = false) : KMPNewStructureMigrationEffect()
    data object NavigateBack : KMPNewStructureMigrationEffect()
    data class OpenFile(val filePath: String) : KMPNewStructureMigrationEffect()
}
