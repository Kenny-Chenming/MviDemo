package com.mvi.kenny.feature.styleapikit

// ================================================================
// StyleApiKitContract — Compose Style API Toolkit MVI Contract
// ================================================================
// MVI architecture contract for PRD-137: Compose 1.11 Style API
// Declarative Styling Development Toolkit.
//
// Design Reference: memory/agency/designs/PRD-137-Compose-1.11-Style-API-声明式样式开发工具包.md
//
// MVI 三要素 / Three pillars:
//   Model (State)  — Immutable page state, single source of truth
//   View           — Composable function, consumes State, renders UI
//   Intent         — User intentions, ViewModel processes and updates State
//   Effect         — One-time side effects (navigation, toast), via Channel
// ================================================================

import androidx.compose.ui.graphics.Color

// ================================================================
// Enums / 枚举
// ================================================================

/**
 * ============================================================
 * StyleTab — 工具包底部 Tab 导航枚举
 * ============================================================
 * Five core pages in the Style API Toolkit.
 *
 * @param titleCn 中文标题
 * @param iconName Material icon name
 */
enum class StyleTab(val titleCn: String, val iconName: String) {
    DecisionEngine("决策引擎", "analytics"),
    MigrationTool("迁移工具", "transform"),
    DebugPanel("调试面板", "bug_report"),
    ThemeGuide("主题指南", "book"),
    Playground("Playground", "code")
}

/**
 * ============================================================
 * InteractionState — 交互状态枚举
 * ============================================================
 * Compose interactive states for Style API debugging.
 *
 * @param labelCn 中文标签
 * @param colorHex Highlight color for this state
 */
enum class InteractionState(val labelCn: String, val colorHex: Long) {
    Default("默认", 0xFFFFFFFF),
    Pressed("按下", 0xFFEADDFF),
    Focused("聚焦", 0xFFFFD8E4),
    Hovered("悬停", 0xFFF3EDF7),
    Checked("选中", 0xFFE8DEF8),
    Disabled("禁用", 0x61FFFFFF)
}

/**
 * ============================================================
 * PlaygroundTemplate — Playground 预设模板枚举
 * ============================================================
 * Pre-built code templates for Style API Playground.
 *
 * @param displayName 中文显示名称
 * @param description 模板描述
 */
enum class PlaygroundTemplate(val displayName: String, val description: String) {
    CardStyle("Card 样式", "Card component with Style API"),
    ButtonStyle("Button 样式", "Button component with Style API"),
    ListItemStyle("列表项样式", "List item with Style API"),
    ChipStyle("Chip 样式", "Chip component with Style API")
}

/**
 * ============================================================
 * DecisionVerdict — 决策引擎判定结果枚举
 * ============================================================
 * Decision engine output: migrate to Style or keep Modifier.
 *
 * @param labelCn 中文标签
 * @param reason 判定理由
 */
enum class DecisionVerdict(val labelCn: String, val labelEn: String) {
    MigrateToStyle("迁移到 Style API", "migrate_to_style"),
    KeepModifier("保持 Modifier", "keep_modifier")
}

// ================================================================
// Data Models / 数据模型
// ================================================================

/**
 * ============================================================
 * DecisionReason — 决策理由条目
 * ============================================================
 * Individual reason item in the decision output.
 *
 * @param text 理由文本
 * @param isProStyle Whether this reason supports migrating to Style
 */
data class DecisionReason(
    val text: String,
    val isProStyle: Boolean
)

/**
 * ============================================================
 * MigrationDiff — 迁移 Diff 单项
 * ============================================================
 * Represents a single code change in the migration diff.
 *
 * @param originalLine Original source code line
 * @param migratedLine Migrated Style API code line
 * @param lineNumber Source line number
 * @param changeType ADD / REMOVE / MODIFY
 */
data class MigrationDiff(
    val originalLine: String,
    val migratedLine: String,
    val lineNumber: Int,
    val changeType: DiffChangeType
)

/**
 * ============================================================
 * DiffChangeType — Diff 变更类型
 * ============================================================
 * @param value Raw change type string
 */
enum class DiffChangeType(val value: String) {
    ADD("add"),
    REMOVE("remove"),
    MODIFY("modify")
}

/**
 * ============================================================
 * ThemeGuideSection — 主题指南章节
 * ============================================================
 * A section in the theme integration guide.
 *
 * @param title 章节标题
 * @param content 章节内容
 * @param codeExample 示例代码（可选）
 */
data class ThemeGuideSection(
    val title: String,
    val content: String,
    val codeExample: String? = null
)

// ================================================================
// State / 状态
// ================================================================

/**
 * ============================================================
 * StyleApiKitState — 工具包页面状态
 * ================================================================
 * Immutable UI state — single source of truth for the entire toolkit.
 *
 * @param currentTab Currently selected bottom navigation tab
 * @param decisionEngineState Decision engine sub-state
 * @param migrationState Migration tool sub-state
 * @param debugPanelState Debug panel sub-state
 * @param playgroundState Playground sub-state
 */
data class StyleApiKitState(
    val currentTab: StyleTab = StyleTab.DecisionEngine,
    val decisionEngineState: DecisionEngineState = DecisionEngineState(),
    val migrationState: MigrationState = MigrationState(),
    val debugPanelState: DebugPanelState = DebugPanelState(),
    val playgroundState: PlaygroundState = PlaygroundState()
)

/**
 * ============================================================
 * DecisionEngineState — 决策引擎子状态
 * ============================================================
 * State for the Style vs Modifier decision engine.
 *
 * @param inputCode User-pasted Kotlin source code
 * @param isAnalyzing Whether analysis is in progress
 * @param decisionResult Verdict: migrate_to_style or keep_modifier
 * @param decisionReasons List of reasons for the decision
 * @param analysisLog Step-by-step decision tree log
 */
data class DecisionEngineState(
    val inputCode: String = "",
    val isAnalyzing: Boolean = false,
    val decisionResult: DecisionVerdict? = null,
    val decisionReasons: List<DecisionReason> = emptyList(),
    val analysisLog: List<String> = emptyList()
)

/**
 * ============================================================
 * MigrationState — 迁移工具子状态
 * ============================================================
 * State for the Modifier → Style API migration tool.
 *
 * @param selectedModule Selected Gradle module name
 * @param availableModules List of available modules in the project
 * @param originalCode Original source code to migrate
 * @param migratedCode Generated Style API code
 * @param diffs List of individual line changes
 * @param isMigrating Whether migration is in progress
 * @param migrationComplete Whether migration has completed
 * @param appliedDiffs Set of diff line numbers that have been applied
 */
data class MigrationState(
    val selectedModule: String = "",
    val availableModules: List<String> = listOf(":app", ":feature:module", ":library"),
    val originalCode: String = "",
    val migratedCode: String = "",
    val diffs: List<MigrationDiff> = emptyList(),
    val isMigrating: Boolean = false,
    val migrationComplete: Boolean = false,
    val appliedDiffs: Set<Int> = emptySet()
)

/**
 * ============================================================
 * DebugPanelState — 调试面板子状态
 * ============================================================
 * State for the Style API animation debug panel.
 *
 * @param selectedInteractionState Currently selected interaction state
 * @param currentColor Current highlight color for the state
 * @param animationCurve Animation curve name (e.g. "easeInOut")
 * @param animationDurationMs Animation duration in milliseconds
 * @param isAnimating Whether animation is currently playing
 * @param showAnimationCurve Whether to show the animation curve visualization
 */
data class DebugPanelState(
    val selectedInteractionState: InteractionState = InteractionState.Default,
    val currentColor: Color = Color.Unspecified,
    val animationCurve: String = "easeInOut",
    val animationDurationMs: Int = 300,
    val isAnimating: Boolean = false,
    val showAnimationCurve: Boolean = true
)

/**
 * ============================================================
 * PlaygroundState — Playground 子状态
 * ============================================================
 * State for the Style API Playground (code editor + live preview).
 *
 * @param code Current code in the editor
 * @param previewError Error message if code has compilation issues
 * @param selectedTemplate Currently selected template
 * @param isPreviewUpdating Whether preview is being refreshed
 */
data class PlaygroundState(
    val code: String = "",
    val previewError: String? = null,
    val selectedTemplate: PlaygroundTemplate? = null,
    val isPreviewUpdating: Boolean = false
)

// ================================================================
// Intent / 意图
// ================================================================

/**
 * ============================================================
 * StyleApiKitIntent — 用户操作意图
 * ================================================================
 * Sealed class representing all possible user intentions.
 */
sealed class StyleApiKitIntent {
    /** 切换底部 Tab */
    data class SwitchTab(val tab: StyleTab) : StyleApiKitIntent()

    // ─── Decision Engine ───────────────────────────────────────
    /** 更新代码输入 */
    data class UpdateInputCode(val code: String) : StyleApiKitIntent()

    /** 触发代码分析 */
    data object AnalyzeCode : StyleApiKitIntent()

    /** 清除分析结果 */
    data object ClearAnalysis : StyleApiKitIntent()

    // ─── Migration Tool ───────────────────────────────────────
    /** 选择 Gradle 模块 */
    data class SelectModule(val module: String) : StyleApiKitIntent()

    /** 更新原始代码 */
    data class UpdateOriginalCode(val code: String) : StyleApiKitIntent()

    /** 触发迁移 */
    data object MigrateCode : StyleApiKitIntent()

    /** 应用单个 Diff 变更 */
    data class ApplyDiff(val lineNumber: Int) : StyleApiKitIntent()

    /** 应用全部 Diff */
    data object ApplyAllDiffs : StyleApiKitIntent()

    /** 复制迁移后的代码 */
    data object CopyMigratedCode : StyleApiKitIntent()

    /** 导出迁移报告 */
    data object ExportMigrationReport : StyleApiKitIntent()

    // ─── Debug Panel ──────────────────────────────────────────
    /** 选择交互状态 */
    data class SelectInteractionState(val state: InteractionState) : StyleApiKitIntent()

    /** 更新动画参数 */
    data class UpdateAnimationParams(val durationMs: Int, val curve: String) : StyleApiKitIntent()

    /** 播放/暂停动画预览 */
    data object ToggleAnimation : StyleApiKitIntent()

    /** 切换动画曲线可视化 */
    data object ToggleAnimationCurve : StyleApiKitIntent()

    // ─── Playground ──────────────────────────────────────────
    /** 更新 Playground 代码 */
    data class UpdatePlaygroundCode(val code: String) : StyleApiKitIntent()

    /** 选择预设模板 */
    data class SelectTemplate(val template: PlaygroundTemplate) : StyleApiKitIntent()

    /** 重置为默认代码 */
    data object ResetToDefault : StyleApiKitIntent()
}

// ================================================================
// Effect / 副作用
// ================================================================

/**
 * ============================================================
 * StyleApiKitEffect — 一次性副作用
 * ================================================================
 * One-time side effects triggered by ViewModel.
 */
sealed class StyleApiKitEffect {
    /** 显示 Snackbar 提示 */
    data class ShowSnackbar(val message: String) : StyleApiKitEffect()

    /** 复制文本到剪贴板 */
    data class CopyToClipboard(val text: String) : StyleApiKitEffect()

    /** 迁移应用完成 */
    data object MigrationApplied : StyleApiKitEffect()

    /** 显示错误信息 */
    data class ShowError(val message: String) : StyleApiKitEffect()

    /** 导出报告成功 */
    data class ExportReport(val content: String, val fileName: String) : StyleApiKitEffect()
}
