package com.mvi.kenny.feature.panda4agenttools

// ================================================================
// Panda4AgentToolsContract — Android Studio Panda 4 AI Agent 增强工具包 MVI 契约
// ================================================================
// MVI architecture contract for Panda 4 AI Agent Enhancement Toolkit.
//
// PRD-240: Android Studio Panda 4 AI Agent 增强工具包
// Design Reference: memory/agency/designs/PRD-240-Android-Studio-Panda-4-AI-Agent-增强工具包.md
//
// Five feature modules:
// 1. Planning Mode — plan templates + readability tool + × Agent Skills integration
// 2. Next Edit Prediction — adoption analytics + custom prediction rules
// 3. Agent Web Search — result quality scoring + enterprise KB integration
// 4. Ask Mode — knowledge base configuration
// 5. Dev Verification — CI integration + audit log export
// ================================================================

import androidx.compose.ui.graphics.Color

// ================================================================
// Color Palette — Dark Terminal Style / 配色方案 — 深色 Terminal 风格
// ================================================================

/**
 * ============================================================
 * Panda4AgentColors — 工具包配色方案
 * ================================================================
 * Dark Terminal aesthetic — GitHub Dark inspired palette.
 * Applied consistently across all 5 tabs.
 */
object Panda4AgentColors {
    val Background = Color(0xFF0D1117)     // Deep dark background
    val Surface = Color(0xFF161B22)        // Card / panel surface
    val Card = Color(0xFF21262D)          // Elevated card
    val Primary = Color(0xFF58A6FF)       // Blue accent — tools / links
    val Success = Color(0xFF3FB950)        // Green — pass / success
    val Warning = Color(0xFFD29922)         // Yellow — warning
    val Error = Color(0xFFF85149)          // Red — error / critical
    val TextPrimary = Color(0xFFE6EDF3)   // Main text
    val TextSecondary = Color(0xFF8B949E)  // Muted / secondary text
    val Border = Color(0xFF30363D)         // Subtle border
}

// ================================================================
// Tab Definitions / Tab 定义
// ================================================================

/**
 * ============================================================
 * Panda4AgentTab — 工具包 Tab 枚举
 * ================================================================
 * Five tabs corresponding to the five core feature modules.
 *
 * @param title Display title in Chinese
 * @param iconName Material icon name for the tab
 */
enum class Panda4AgentTab(val title: String, val iconName: String) {
    PlanningMode("Planning Mode", "planning_mode"),
    NextEditPrediction("Next Edit Prediction", "nep"),
    AgentWebSearch("Agent Web Search", "web_search"),
    AskMode("Ask Mode", "ask_mode"),
    DevVerification("Dev Verification", "dev_verification")
}

// ================================================================
// Scenario Types / 场景类型（Planning Mode）
// ================================================================

/**
 * ============================================================
 * PlanScenario — 计划场景类型
 * ================================================================
 * Supported planning scenario types for template selection.
 */
enum class PlanScenario(val label: String) {
    Refactoring("重构"),
    NewFeature("新功能"),
    BugFix("Bug修复"),
    PerformanceOptimization("性能优化"),
    ArchitectureUpgrade("架构升级")
}

// ================================================================
// Plan Template / 计划模板
// ================================================================

/**
 * ============================================================
 * PlanTemplate — 计划模板
 * ================================================================
 * Represents a reusable planning template for a specific scenario.
 *
 * @param id Unique template identifier
 * @param scenario Plan scenario type
 * @param title Template display title
 * @param description Template description
 * @param content Template markdown content
 * @param tags Comma-separated tags
 */
data class PlanTemplate(
    val id: String,
    val scenario: PlanScenario,
    val title: String,
    val description: String,
    val content: String,
    val tags: String = ""
)

// ================================================================
// NEP Prediction Rule / NEP 预测规则
// ================================================================

/**
 * ============================================================
 * PredictionRule — NEP 预测规则
 * ================================================================
 * Custom rule for Next Edit Prediction behavior.
 * DSL format: "trigger_pattern:action:target_file_pattern"
 *
 * @param id Unique rule identifier
 * @param name Human-readable rule name
 * @param pattern DSL pattern string
 * @param isEnabled Whether the rule is active
 * @param description Rule description
 * @param adoptionCount Number of times this rule was triggered
 */
data class PredictionRule(
    val id: String,
    val name: String,
    val pattern: String,
    val isEnabled: Boolean = true,
    val description: String = "",
    val adoptionCount: Int = 0
)

// ================================================================
// Knowledge Base Entry / 知识库条目
// ================================================================

/**
 * ============================================================
 * KnowledgeBase — 知识库条目
 * ================================================================
 * A knowledge base entry for Ask Mode or Agent Web Search.
 *
 * @param id Unique entry identifier
 * @param type Content type: markdown, url, or code
 * @param title Display title
 * @param content The actual content (markdown text, URL, or code snippet)
 * @param description Optional description
 */
data class KnowledgeBase(
    val id: String,
    val type: KbType,
    val title: String,
    val content: String,
    val description: String = ""
)

/** Knowledge base entry type */
enum class KbType { Markdown, Url, Code }

// ================================================================
// Search Quality Result / 搜索质量结果
// ================================================================

/**
 * ============================================================
 * SearchQualityResult — 搜索质量评分结果
 * ================================================================
 * Result of URL quality scoring for Agent Web Search.
 *
 * @param url The analyzed URL
 * @param relevanceScore Relevance score 0-100
 * @param freshnessScore Freshness score 0-100
 * @param authorityScore Authority score 0-100
 * @param overallScore Weighted overall score 0-100
 * @param summary One-line summary
 */
data class SearchQualityResult(
    val url: String,
    val relevanceScore: Int,
    val freshnessScore: Int,
    val authorityScore: Int,
    val overallScore: Int,
    val summary: String
)

// ================================================================
// CI Config / CI 配置
// ================================================================

/**
 * ============================================================
 * CiConfig — Dev Verification CI 配置
 * ================================================================
 * Configuration for Developer Verification CI integration.
 *
 * @param identityProvider Identity provider (e.g., GitHub, GitLab)
 * @param repository Repository full name (owner/repo)
 * @param requiredRole Required role for agent access
 * @param approvalWorkflow Whether approval workflow is required
 * @param tokenPrefix Token prefix hint (masked display)
 */
data class CiConfig(
    val identityProvider: String = "GitHub",
    val repository: String = "",
    val requiredRole: String = "write",
    val approvalWorkflow: Boolean = true,
    val tokenPrefix: String = "ghp_****"
)

// ================================================================
// Audit Log Entry / 审计日志条目
// ================================================================

/**
 * ============================================================
 * AuditLog — Planning Mode 审计日志条目
 * ================================================================
 * A single audit log entry for AI planning operations.
 *
 * @param id Unique log entry ID
 * @param timestamp ISO timestamp
 * @param action Action type: PlanCreated, PlanReviewed, PlanApproved, PlanRejected, TaskExecuted
 * @param description Human-readable description
 * @param plannerOutput Raw planner output (if applicable)
 * @param agentAction Agent action taken
 * @param deviationDegree Deviation from plan (0.0 = exact, 1.0 = completely different)
 * @param executor Who/what executed (human or agent name)
 */
data class AuditLog(
    val id: String,
    val timestamp: String,
    val action: String,
    val description: String,
    val plannerOutput: String = "",
    val agentAction: String = "",
    val deviationDegree: Float = 0f,
    val executor: String = "Agent"
)

// ================================================================
// Export Format / 导出格式
// ================================================================

/**
 * ============================================================
 * ExportFormat — 审计日志导出格式
 * ================================================================
 * Supported export formats for audit logs.
 */
enum class ExportFormat { JSON, CSV, PDF }

// ================================================================
// State / 状态
// ================================================================

/**
 * ============================================================
 * Panda4AgentToolsState — 工具包完整 UI 状态
 * ================================================================
 * Immutable single source of truth for the entire 5-tab toolkit.
 *
 * Tab navigation:
 * @param currentTabIndex Currently active tab index (0-4)
 *
 * Planning Mode state:
 * @param selectedScenario Selected planning scenario
 * @param planTemplates Available plan templates for the selected scenario
 * @param previewTemplate Template currently shown in preview (null = no preview)
 * @param formattedPlanOutput Formatted plan output after user formats an AI plan
 * @param isFormattingPlan Whether formatting is in progress
 *
 * NEP state:
 * @param adoptionRate Current NEP adoption rate (0.0-1.0)
 * @param adoptionTrend Historical adoption rates (newest last)
 * @param predictionRules Custom prediction rules
 * @param isAnalyzingRules Whether rule analysis is running
 *
 * Web Search state:
 * @param searchUrlInput URL input field
 * @param searchResult Most recent quality analysis result
 * @param knowledgeBases Enterprise knowledge base entries
 * @param isAnalyzingUrl Whether URL analysis is running
 *
 * Ask Mode state:
 * @param askKnowledgeItems Knowledge items configured for Ask Mode
 * @param askPreviewOutput Preview output from Ask Mode
 * @param isPreviewingAsk Whether ask preview is running
 *
 * Dev Verification state:
 * @param ciConfig CI integration configuration
 * @param auditLogs Audit log entries
 * @param isExporting Whether export is in progress
 * @param isLoadingAudit Whether audit log is loading
 * @param newKbInput Temporary input for adding new KB entry
 * @param newRuleInput Temporary input for adding new prediction rule
 */
data class Panda4AgentToolsState(
    // ── Tab Navigation ──────────────────────────────────────────
    val currentTabIndex: Int = 0,

    // ── Planning Mode ──────────────────────────────────────────
    val selectedScenario: PlanScenario = PlanScenario.Refactoring,
    val planTemplates: List<PlanTemplate> = emptyList(),
    val previewTemplate: PlanTemplate? = null,
    val formattedPlanOutput: String = "",
    val isFormattingPlan: Boolean = false,
    val rawPlanInput: String = "",

    // ── NEP ────────────────────────────────────────────────────
    val adoptionRate: Float = 0f,
    val adoptionTrend: List<Float> = emptyList(),
    val predictionRules: List<PredictionRule> = emptyList(),
    val isAnalyzingRules: Boolean = false,
    val editingRuleId: String? = null,

    // ── Agent Web Search ────────────────────────────────────────
    val searchUrlInput: String = "",
    val searchResult: SearchQualityResult? = null,
    val knowledgeBases: List<KnowledgeBase> = emptyList(),
    val isAnalyzingUrl: Boolean = false,
    val newKbTitleInput: String = "",
    val newKbContentInput: String = "",
    val newKbType: KbType = KbType.Markdown,

    // ── Ask Mode ────────────────────────────────────────────────
    val askKnowledgeItems: List<KnowledgeBase> = emptyList(),
    val askPreviewOutput: String = "",
    val isPreviewingAsk: Boolean = false,
    val askQueryInput: String = "",

    // ── Dev Verification ───────────────────────────────────────
    val ciConfig: CiConfig = CiConfig(),
    val auditLogs: List<AuditLog> = emptyList(),
    val isExporting: Boolean = false,
    val isLoadingAudit: Boolean = false,
    val exportProgress: Float = 0f,

    // ── Shared ──────────────────────────────────────────────────
    val snackbarMessage: String? = null
)

// ================================================================
// Intent / 意图
// ================================================================

/**
 * ============================================================
 * Panda4AgentToolsIntent — 所有用户操作意图
 * ================================================================
 * Sealed class covering every possible user action across all 5 tabs.
 */
sealed class Panda4AgentToolsIntent {

    // ── Tab Navigation ──────────────────────────────────────────
    /** 切换到指定 Tab */
    data class SelectTab(val index: Int) : Panda4AgentToolsIntent()

    // ── Planning Mode ──────────────────────────────────────────
    /** 选择计划场景 */
    data class SelectScenario(val scenario: PlanScenario) : Panda4AgentToolsIntent()

    /** 预览模板 */
    data class PreviewTemplate(val template: PlanTemplate) : Panda4AgentToolsIntent()

    /** 关闭模板预览 */
    data object ClosePreview : Panda4AgentToolsIntent()

    /** 格式化原始计划文本 */
    data class FormatPlan(val rawText: String) : Panda4AgentToolsIntent()

    /** 更新原始计划输入 */
    data class UpdateRawPlanInput(val text: String) : Panda4AgentToolsIntent()

    // ── NEP ────────────────────────────────────────────────────
    /** 添加预测规则 */
    data class AddPredictionRule(val rule: PredictionRule) : Panda4AgentToolsIntent()

    /** 删除预测规则 */
    data class DeletePredictionRule(val id: String) : Panda4AgentToolsIntent()

    /** 切换规则启用状态 */
    data class ToggleRuleEnabled(val id: String) : Panda4AgentToolsIntent()

    /** 开始分析规则采纳率 */
    data object AnalyzeAdoptionRate : Panda4AgentToolsIntent()

    // ── Agent Web Search ────────────────────────────────────────
    /** 分析 URL 质量 */
    data class AnalyzeSearchUrl(val url: String) : Panda4AgentToolsIntent()
    data class UpdateSearchUrlInput(val text: String) : Panda4AgentToolsIntent()

    /** 添加知识库条目 */
    data class AddKnowledgeBase(val kb: KnowledgeBase) : Panda4AgentToolsIntent()

    /** 删除知识库条目 */
    data class DeleteKnowledgeBase(val id: String) : Panda4AgentToolsIntent()

    /** 更新 KB 添加表单输入 */
    data class UpdateKbInput(val title: String, val content: String, val type: KbType) : Panda4AgentToolsIntent()

    // ── Ask Mode ────────────────────────────────────────────────
    /** 预览 Ask Mode 输出 */
    data class PreviewAskOutput(val query: String) : Panda4AgentToolsIntent()

    /** 更新 Ask Mode 查询输入 */
    data class UpdateAskQuery(val query: String) : Panda4AgentToolsIntent()

    // ── Dev Verification ───────────────────────────────────────
    /** 更新 CI 配置 */
    data class UpdateCiConfig(val config: CiConfig) : Panda4AgentToolsIntent()

    /** 加载审计日志 */
    data object LoadAuditLogs : Panda4AgentToolsIntent()

    /** 导出审计日志 */
    data class ExportAuditLogs(val format: ExportFormat) : Panda4AgentToolsIntent()

    // ── Shared ──────────────────────────────────────────────────
    /** 清除 snackbar 消息 */
    data object ClearSnackbar : Panda4AgentToolsIntent()
}

// ================================================================
// Effect / 副作用
// ================================================================

/**
 * ============================================================
 * Panda4AgentToolsEffect — 一次性副作用
 * ================================================================
 * One-time side effects that the UI layer should handle exactly once.
 */
sealed class Panda4AgentToolsEffect {
    /** 显示 Snackbar 消息 */
    data class ShowSnackbar(val message: String) : Panda4AgentToolsEffect()

    /** 下载文件完成 */
    data class DownloadFile(val format: ExportFormat, val path: String) : Panda4AgentToolsEffect()

    /** 复制到剪贴板 */
    data class CopyToClipboard(val text: String) : Panda4AgentToolsEffect()
}
