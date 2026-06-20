package com.mvi.kenny.feature.room3kmpmigration

// ================================================================
// Room3KmpMigrationContract — Room 3.0 KMP 数据库迁移工具包 MVI 契约
// ================================================================
// MVI architecture contract for Room 3.0 KMP Database Migration Toolkit.
//
// PRD-294: Room 3.0 KMP 数据库迁移工具包
// Design Reference: memory/agency/designs/PRD-294-Room-3.0-KMP数据库迁移工具包.md
//
// MVI 三要素 / Three pillars:
//   Model (State)  — Immutable page state, single source of truth
//   View           — Composable function, consumes State, renders UI
//   Intent         — User intentions, ViewModel processes and updates State
//   Effect         — One-time side effects (navigation, toast), via Channel
//
// ================================================================

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

// ================================================================
// Section 1: Enums — Supporting Types
// ================================================================

/**
 * ============================================================
 * ChangeSeverity — 变更严重程度枚举
 * ============================================================
 * Used to categorize Room 2.x → 3.0 migration changes.
 *
 * @param displayName 中文显示名称
 * @param emoji Emoji representation
 * @param color UI color for this severity level
 */
enum class ChangeSeverity(
    val displayName: String,
    val emoji: String,
    val color: Color
) {
    /** 断裂性变更 — Breaking change, must fix before compilation */
    BREAKING("断裂性", "🔴", Color(0xFFB3261E)),
    /** 高风险 — High risk, significant API change */
    HIGH_RISK("高风险", "🟠", Color(0xFFF9A825)),
    /** 建议项 — Suggested change, recommended but not required */
    SUGGESTED("建议项", "🟡", Color(0xFFFFD93D)),
    /** 安全 — Safe, no action needed */
    SAFE("安全", "🟢", Color(0xFF2E7D32))
}

/**
 * ============================================================
 * ScanPhase — 扫描阶段枚举
 * ============================================================
 * Progress phases during project scan.
 *
 * @param displayName 中文显示名称
 */
enum class ScanPhase(val displayName: String) {
    IDLE("空闲"),
    PARSING("解析依赖"),
    ANALYZING("分析代码"),
    REPORTING("生成报告")
}

/**
 * ============================================================
 * MigrationStep — 迁移步骤枚举
 * ============================================================
 * Four steps in the Room 3.0 migration wizard.
 *
 * @param displayName 中文显示名称
 * @param description Step description
 */
enum class MigrationStep(
    val displayName: String,
    val description: String
) {
    PREPARATION("准备工作", "备份项目、检查兼容性、阅读变更日志"),
    DEPENDENCY_UPGRADE("依赖升级", "更新 build.gradle.kts 中的 Room 依赖到 3.0"),
    CODE_MIGRATION("代码迁移", "迁移包名、Suspend 函数化、Driver API"),
    VERIFICATION("验证测试", "运行 KSP 重新生成代码、验证编译和功能")
}

/**
 * ============================================================
 * KmpModuleType — KMP 模块类型枚举
 * ============================================================
 * Kotlin Multiplatform module structure choice.
 *
 * @param displayName 中文显示名称
 * @param description 模块类型说明
 */
enum class KmpModuleType(
    val displayName: String,
    val description: String
) {
    SHARED("共享模块", "所有平台共享一个 Room 数据库模块（推荐）"),
    PLATFORM_SPECIFIC("平台特定", "各平台独立配置 Room 数据库（Android/iOS/JVM/Web）")
}

// ================================================================
// Section 2: Data Classes — Domain Models
// ================================================================

/**
 * ============================================================
 * QuickAccess — 首页快速入口数据类
 * ============================================================
 * Four quick-access cards on the Home tab.
 *
 * @param title 卡片标题
 * @param subtitle 卡片副标题
 * @param icon 图标
 * @param route 导航路由
 */
data class QuickAccess(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val route: String
)

/**
 * ============================================================
 * ScanSummary — 扫描摘要数据类
 * ============================================================
 * Summary of the last scan result, shown on Home tab.
 *
 * @param totalBreakingChanges 断裂性变更数量
 * @param totalHighRiskChanges 高风险变更数量
 * @param totalSuggestedChanges 建议变更数量
 * @param scannedAt 扫描时间戳（毫秒）
 */
data class ScanSummary(
    val totalBreakingChanges: Int,
    val totalHighRiskChanges: Int,
    val totalSuggestedChanges: Int,
    val scannedAt: Long
)

/**
 * ============================================================
 * ChangeItem — 变更项数据类
 * ============================================================
 * Represents a single change required for Room 3.0 migration.
 *
 * @param title 变更标题
 * @param description 变更描述
 * @param severity 严重程度
 * @param filePath 涉及的文件路径
 * @param lineNumber 行号（可选）
 * @param before 变更前代码（可选）
 * @param after 变更后代码（可选）
 */
data class ChangeItem(
    val title: String,
    val description: String,
    val severity: ChangeSeverity,
    val filePath: String,
    val lineNumber: Int? = null,
    val before: String? = null,
    val after: String? = null
)

/**
 * ============================================================
 * CodeDiff — 代码 Diff 数据类
 * ============================================================
 * Represents a code diff block for migration display.
 *
 * @param fileName 文件名
 * @param filePath 完整文件路径
 * @param hunks 多个 diff hunk
 */
data class CodeDiff(
    val fileName: String,
    val filePath: String,
    val hunks: List<DiffHunk>
)

/**
 * ============================================================
 * DiffHunk — Diff 块数据类
 * ============================================================
 * A single hunk within a code diff.
 *
 * @param linesBefore 变更前代码行
 * @param linesAfter 变更后代码行
 * @param description Hunk 描述
 */
data class DiffHunk(
    val linesBefore: List<String>,
    val linesAfter: List<String>,
    val description: String
)

/**
 * ============================================================
 * Scenario — 预置场景数据类
 * ============================================================
 * Five preset migration scenarios on the Reference tab.
 *
 * @param title 场景标题
 * @param description 场景描述
 * @param icon 图标
 * @param route 导航路由
 */
data class Scenario(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val route: String
)

/**
 * ============================================================
 * ApiChange — API 变更数据类
 * ============================================================
 * Room 2.x → 3.0 API change reference entry.
 *
 * @param apiName API 名称
 * @param v2Usage 2.x 用法
 * @param v3Usage 3.0 用法
 * @param notes 备注说明
 */
data class ApiChange(
    val apiName: String,
    val v2Usage: String,
    val v3Usage: String,
    val notes: String
)

// ================================================================
// Section 3: State — MVI State Definitions
// ================================================================

/**
 * ============================================================
 * Room3KmpMigrationState — 根状态
 * ============================================================
 * Top-level MVI state for the entire Room 3.0 KMP Migration Toolkit.
 * Contains all tab states as well as global navigation state.
 *
 * @param currentTab 当前选中的 Tab 索引 (0=Home, 1=Scanner, 2=Migration, 3=Reference)
 * @param homeState Home Tab 状态
 * @param scannerState Scanner Tab 状态
 * @param migrationState Migration Tab 状态
 * @param referenceState Reference Tab 状态
 */
data class Room3KmpMigrationState(
    val currentTab: Int = 0,
    val homeState: HomeState = HomeState(),
    val scannerState: ScannerState = ScannerState(),
    val migrationState: MigrationState = MigrationState(),
    val referenceState: ReferenceState = ReferenceState()
)

// -------------------- Home Tab State --------------------

/**
 * ============================================================
 * HomeState — Home Tab 状态
 * ============================================================
 *
 * @param migrationProgress 迁移进度 (0.0 ~ 1.0)
 * @param completedSteps 已完成步骤数
 * @param totalSteps 总步骤数
 * @param quickAccessItems 4 个快速入口
 * @param lastScanSummary 最近扫描摘要（可为 null）
 * @param isLoading 是否加载中
 */
data class HomeState(
    val migrationProgress: Float = 0f,
    val completedSteps: Int = 0,
    val totalSteps: Int = 4,
    val quickAccessItems: List<QuickAccess> = emptyList(),
    val lastScanSummary: ScanSummary? = null,
    val isLoading: Boolean = false
)

// -------------------- Scanner Tab State --------------------

/**
 * ============================================================
 * ScannerState — Scanner Tab 状态
 * ============================================================
 *
 * @param projectPath 项目路径
 * @param scanPhase 当前扫描阶段
 * @param progress 扫描进度 (0.0 ~ 1.0)
 * @param breakingChanges 断裂性变更列表
 * @param highRiskChanges 高风险变更列表
 * @param suggestedChanges 建议变更列表
 * @param scanComplete 扫描是否完成
 */
data class ScannerState(
    val projectPath: String = "",
    val scanPhase: ScanPhase = ScanPhase.IDLE,
    val progress: Float = 0f,
    val breakingChanges: List<ChangeItem> = emptyList(),
    val highRiskChanges: List<ChangeItem> = emptyList(),
    val suggestedChanges: List<ChangeItem> = emptyList(),
    val scanComplete: Boolean = false
)

// -------------------- Migration Tab State --------------------

/**
 * ============================================================
 * MigrationState — Migration Tab 状态
 * ============================================================
 *
 * @param currentStep 当前步骤
 * @param completedSteps 已完成步骤集合
 * @param preparationComplete 准备工作是否完成
 * @param dependencyMigrationComplete 依赖升级是否完成
 * @param codeMigrationComplete 代码迁移是否完成
 * @param verificationComplete 验证测试是否完成
 * @param currentCodeDiff 当前显示的代码 Diff
 * @param kmpModuleType KMP 模块类型选择
 * @param availableSteps 可用步骤列表
 */
data class MigrationState(
    val currentStep: MigrationStep = MigrationStep.PREPARATION,
    val completedSteps: Set<MigrationStep> = emptySet(),
    val preparationComplete: Boolean = false,
    val dependencyMigrationComplete: Boolean = false,
    val codeMigrationComplete: Boolean = false,
    val verificationComplete: Boolean = false,
    val currentCodeDiff: CodeDiff? = null,
    val kmpModuleType: KmpModuleType = KmpModuleType.SHARED,
    val availableSteps: List<MigrationStep> = MigrationStep.entries
)

// -------------------- Reference Tab State --------------------

/**
 * ============================================================
 * ReferenceState — Reference Tab 状态
 * ============================================================
 *
 * @param scenarios 预置场景列表
 * @param apiChanges API 变更对照表
 * @param officialDocUrl 官方文档链接
 */
data class ReferenceState(
    val scenarios: List<Scenario> = emptyList(),
    val apiChanges: List<ApiChange> = emptyList(),
    val officialDocUrl: String = "https://developer.android.com/jetpack/androidx/releases/room3"
)

// ================================================================
// Section 4: Intent — MVI Intent Definitions
// ================================================================

/**
 * ============================================================
 * Room3KmpMigrationIntent — 根 Intent
 * ============================================================
 * User intentions that affect the whole toolkit or switch tabs.
 */
sealed class Room3KmpMigrationIntent {
    data class SwitchTab(val tabIndex: Int) : Room3KmpMigrationIntent()
    data class SendHomeIntent(val intent: HomeIntent) : Room3KmpMigrationIntent()
    data class SendScannerIntent(val intent: ScannerIntent) : Room3KmpMigrationIntent()
    data class SendMigrationIntent(val intent: MigrationIntent) : Room3KmpMigrationIntent()
    data class SendReferenceIntent(val intent: ReferenceIntent) : Room3KmpMigrationIntent()
}

// -------------------- Home Tab Intent --------------------

/**
 * ============================================================
 * HomeIntent — Home Tab Intent
 * ============================================================
 * User intentions for the Home tab.
 */
sealed class HomeIntent {
    object LoadHomeData : HomeIntent()
    object NavigateToScanner : HomeIntent()
    object NavigateToMigration : HomeIntent()
    object NavigateToReference : HomeIntent()
    data class QuickAccessClicked(val route: String) : HomeIntent()
}

// -------------------- Scanner Tab Intent --------------------

/**
 * ============================================================
 * ScannerIntent — Scanner Tab Intent
 * ============================================================
 * User intentions for the Scanner tab.
 */
sealed class ScannerIntent {
    data class UpdateProjectPath(val path: String) : ScannerIntent()
    object StartScan : ScannerIntent()
    data class ExpandChangeItem(val item: ChangeItem) : ScannerIntent()
    object ExportReport : ScannerIntent()
}

// -------------------- Migration Tab Intent --------------------

/**
 * ============================================================
 * MigrationIntent — Migration Tab Intent
 * ============================================================
 * User intentions for the Migration tab.
 */
sealed class MigrationIntent {
    data class SelectKmpModuleType(val type: KmpModuleType) : MigrationIntent()
    data class StartStep(val step: MigrationStep) : MigrationIntent()
    data class CompleteStep(val step: MigrationStep) : MigrationIntent()
    data class ViewCodeDiff(val diff: CodeDiff) : MigrationIntent()
    object GenerateMigrationReport : MigrationIntent()
}

// -------------------- Reference Tab Intent --------------------

/**
 * ============================================================
 * ReferenceIntent — Reference Tab Intent
 * ============================================================
 * User intentions for the Reference tab.
 */
sealed class ReferenceIntent {
    data class ScenarioClicked(val scenario: Scenario) : ReferenceIntent()
    object OpenOfficialDoc : ReferenceIntent()
}

// ================================================================
// Section 5: Effect — MVI Effect Definitions
// ================================================================

/**
 * ============================================================
 * Room3KmpMigrationEffect — 根 Effect
 * ============================================================
 * One-time side effects for the whole toolkit.
 */
sealed class Room3KmpMigrationEffect {
    data class Navigate(val route: String) : Room3KmpMigrationEffect()
    data class ShowError(val message: String) : Room3KmpMigrationEffect()
    data class ShowSuccess(val message: String) : Room3KmpMigrationEffect()
    data class ShareReport(val markdown: String) : Room3KmpMigrationEffect()
    data class OpenUrl(val url: String) : Room3KmpMigrationEffect()
}
