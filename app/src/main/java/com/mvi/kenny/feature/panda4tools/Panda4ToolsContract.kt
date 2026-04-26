package com.mvi.kenny.feature.panda4tools

// ================================================================
// Panda4ToolsContract — Panda 4 AI Agent 开发工具包 MVI 契约
// ================================================================
// MVI architecture contract for Panda 4 AI Agent Toolkit.
//
// PRD-158: Android Studio Panda 4 AI Agent 开发工具包
// Design Reference: memory/agency/designs/PRD-158-Android-Studio-Panda-4-AI-Agent-开发工具包.md
//
// 八大工具模块：
// 1. Planning Mode 计划评审
// 2. NEP 链式编辑验证
// 3. Planning Git 集成
// 4. Planning 审计日志
// 5. NEP 代码审查联动
// 6. Gemini API Starter CLI
// 7. Google One 配额监控
// 8. Panda 4 vs Panda 3 对比
// ================================================================

// ================================================================
// Tool Module Enum / 工具模块枚举
// ================================================================

/**
 * ============================================================
 * ToolModule — 工具模块枚举
 * ================================================================
 * Eight tool modules in the Panda 4 AI Agent Toolkit.
 *
 * @param title Display name in Chinese
 * @param description Module description
 */
enum class ToolModule(val title: String, val description: String) {
    PlanningReview("计划评审", "解析 Planning Mode 计划，输出结构化评审意见"),
    NepVerify("NEP 验证", "验证 NEP 预测序列的语法正确性"),
    PlanningGit("Git 集成", "将计划内容自动写入 Git commit"),
    PlanningAudit("审计日志", "导出规划→执行的完整时间线审计日志"),
    NepReview("代码审查", "将 NEP 预测的 Diff 自动创建代码审查"),
    GeminiStarter("Gemini CLI", "快速启动 Gemini API 调用"),
    QuotaMonitor("配额监控", "监控 Gemini API 配额使用情况"),
    PandaCompare("版本对比", "Panda 4 vs Panda 3 功能对比")
}

// ================================================================
// Review Status / 评审状态
// ================================================================

/**
 * ============================================================
 * ReviewStatus — 计划评审状态枚举
 * ================================================================
 */
enum class ReviewStatus {
    Idle,
    Reviewing,
    Reviewed,
    Error
}

// ================================================================
// Verify Status / 验证状态
// ================================================================

/**
 * ============================================================
 * VerifyStatus — NEP 验证状态枚举
 * ================================================================
 */
enum class VerifyStatus {
    Idle,
    Verifying,
    Passed,
    Failed
}

// ================================================================
// Quota Level / 配额级别
// ================================================================

/**
 * ============================================================
 * QuotaLevel — 配额级别枚举
 * ================================================================
 */
enum class QuotaLevel {
    Normal,
    Warning,
    Critical,
    Exhausted
}

// ================================================================
// Data Models / 数据模型
// ================================================================

/**
 * ============================================================
 * PlanReview — 计划评审报告
 * ================================================================
 * Structure output from Planning Mode review.
 *
 * @param complexityScore Complexity score (1-10)
 * @param risks List of identified risk points
 * @param missedDependencies List of missed dependencies
 * @param testabilityScore Testability evaluation
 * @param overallRecommendation Overall recommendation (Approved / Needs Revision / Rejected)
 * @param generatedAt Report generation timestamp
 */
data class PlanReview(
    val complexityScore: Int,
    val risks: List<RiskPoint>,
    val missedDependencies: List<String>,
    val testabilityScore: TestabilityScore,
    val overallRecommendation: String,
    val generatedAt: String
)

/**
 * ============================================================
 * RiskPoint — 风险点
 * ================================================================
 *
 * @param severity Severity level (high / medium / low)
 * @param location File or module location
 * @param description Risk description
 * @param suggestion Fix suggestion
 */
data class RiskPoint(
    val severity: String,
    val location: String,
    val description: String,
    val suggestion: String
)

/**
 * ============================================================
 * TestabilityScore — 可测试性评分
 * ================================================================
 *
 * @param score Score (1-10)
 * @param analysis Analysis text
 * @param missingTests List of missing test cases
 */
data class TestabilityScore(
    val score: Int,
    val analysis: String,
    val missingTests: List<String>
)

/**
 * ============================================================
 * NepSequence — NEP 编辑序列
 * ================================================================
 *
 * @param stepNumber Step number in sequence
 * @param filePath Target file path
 * @param operation Operation type (insert / modify / delete)
 * @param description Step description
 */
data class NepSequence(
    val stepNumber: Int,
    val filePath: String,
    val operation: String,
    val description: String
)

/**
 * ============================================================
 * VerifyReport — NEP 验证报告
 * ================================================================
 *
 * @param overallStatus Overall verification status
 * @param passedSteps Number of passed steps
 * @param failedSteps Number of failed steps
 * @param errors List of verification errors
 * @param warnings List of warnings
 * @param isCompilable Whether the sequence is compilable
 */
data class VerifyReport(
    val overallStatus: VerifyStatus,
    val passedSteps: Int,
    val failedSteps: Int,
    val errors: List<VerifyError>,
    val warnings: List<String>,
    val isCompilable: Boolean
)

/**
 * ============================================================
 * VerifyError — 验证错误
 * ================================================================
 *
 * @param stepNumber Step number where error occurred
 * @param errorType Error type
 * @param message Error message
 * @param filePath Related file path
 */
data class VerifyError(
    val stepNumber: Int,
    val errorType: String,
    val message: String,
    val filePath: String
)

/**
 * ============================================================
 * GitIntegration — Git 集成结果
 * ================================================================
 *
 * @param success Whether integration succeeded
 * @param branchName Created branch name
 * @param commitHash Commit hash
 * @param planFilePath Plan file path
 * @param message Result message
 */
data class GitIntegration(
    val success: Boolean,
    val branchName: String,
    val commitHash: String,
    val planFilePath: String,
    val message: String
)

/**
 * ============================================================
 * AuditEntry — 审计日志条目
 * ================================================================
 *
 * @param id Entry ID
 * @param timestamp Entry timestamp
 * @param action Action type (Planning / Edit / Build)
 * @param description Action description
 * @param plannerOutput Planner output
 * @param agentAction Agent action taken
 * @param deviation Whether there was deviation from plan
 */
data class AuditEntry(
    val id: String,
    val timestamp: String,
    val action: String,
    val description: String,
    val plannerOutput: String,
    val agentAction: String,
    val deviation: Boolean
)

/**
 * ============================================================
 * CodeReviewRequest — 代码审查请求
 * ================================================================
 *
 * @param id Review request ID
 * @param title Review title
 * @param targetBranch Target branch for review
 * @param changes List of file changes
 * @param reviewUrl Generated review URL (empty if pending)
 */
data class CodeReviewRequest(
    val id: String,
    val title: String,
    val targetBranch: String,
    val changes: List<String>,
    val reviewUrl: String = ""
)

/**
 * ============================================================
 * GeminiResponse — Gemini API 响应
 * ================================================================
 *
 * @param content Response content
 * @param usage Token usage information
 */
data class GeminiResponse(
    val content: String,
    val usage: TokenUsage
)

/**
 * ============================================================
 * TokenUsage — Token 使用量
 * ================================================================
 *
 * @param promptTokens Prompt tokens used
 * @param completionTokens Completion tokens used
 * @param totalTokens Total tokens used
 */
data class TokenUsage(
    val promptTokens: Int,
    val completionTokens: Int,
    val totalTokens: Int
)

/**
 * ============================================================
 * QuotaInfo — 配额信息
 * ================================================================
 *
 * @param email Google account email
 * @param quotaType Quota type
 * @param dailyUsed Daily usage
 * @param dailyLimit Daily limit
 * @param dailyLevel Daily quota level
 * @param monthlyUsed Monthly usage
 * @param monthlyLimit Monthly limit
 * @param monthlyLevel Monthly quota level
 * @param rpmUsed Requests per minute used
 * @param rpmLimit Requests per minute limit
 * @param rpmLevel RPM quota level
 * @param alerts List of active alerts
 */
data class QuotaInfo(
    val email: String,
    val quotaType: String,
    val dailyUsed: Long,
    val dailyLimit: Long,
    val dailyLevel: QuotaLevel,
    val monthlyUsed: Long,
    val monthlyLimit: Long,
    val monthlyLevel: QuotaLevel,
    val rpmUsed: Long,
    val rpmLimit: Long,
    val rpmLevel: QuotaLevel,
    val alerts: List<String>
)

/**
 * ============================================================
 * FeatureComparison — 功能对比项
 * ================================================================
 *
 * @param featureName Feature name
 * @param panda4Description Panda 4 description
 * @param panda3Description Panda 3 description
 * @param isNewInPanda4 Whether this is new in Panda 4
 * @param improvementPercentage Performance improvement percentage
 */
data class FeatureComparison(
    val featureName: String,
    val panda4Description: String,
    val panda3Description: String,
    val isNewInPanda4: Boolean,
    val improvementPercentage: String
)

// ================================================================
// State / 状态
// ================================================================

/**
 * ============================================================
 * Panda4ToolsState — Panda 4 工具包页面状态
 * ================================================================
 * Immutable UI state — single source of truth for the entire toolkit.
 *
 * @param selectedModule Currently selected tool module
 * @param planFilePath Planning file path for review
 * @param reviewStatus Current review status
 * @param reviewReport Generated review report
 * @param nepSequences NEP prediction sequences
 * @param verifyStatus Current verification status
 * @param verifyReport Generated verification report
 * @param branchName Git branch name
 * @param gitIntegration Git integration result
 * @param auditLog Audit log entries
 * @param isExportingAudit Whether audit export is in progress
 * @param codeReviewRequest Code review request
 * @param isCreatingReview Whether review creation is in progress
 * @param geminiResponse Gemini API response
 * @param isRunningCli Whether CLI is running
 * @param quotaInfo Quota information
 * @param isFetchingQuota Whether quota fetch is in progress
 * @param featureComparison Feature comparison data
 * @param isLoadingComparison Whether comparison data is loading
 * @param isLoading Whether any loading is in progress
 */
data class Panda4ToolsState(
    val selectedModule: ToolModule = ToolModule.PlanningReview,
    val planFilePath: String = "",
    val reviewStatus: ReviewStatus = ReviewStatus.Idle,
    val reviewReport: PlanReview? = null,
    val nepSequences: List<NepSequence> = emptyList(),
    val verifyStatus: VerifyStatus = VerifyStatus.Idle,
    val verifyReport: VerifyReport? = null,
    val branchName: String = "",
    val gitIntegration: GitIntegration? = null,
    val auditLog: List<AuditEntry> = emptyList(),
    val isExportingAudit: Boolean = false,
    val codeReviewRequest: CodeReviewRequest? = null,
    val isCreatingReview: Boolean = false,
    val geminiResponse: GeminiResponse? = null,
    val isRunningCli: Boolean = false,
    val quotaInfo: QuotaInfo? = null,
    val isFetchingQuota: Boolean = false,
    val featureComparison: List<FeatureComparison> = emptyList(),
    val isLoadingComparison: Boolean = false,
    val isLoading: Boolean = false
)

// ================================================================
// Intent / 意图
// ================================================================

/**
 * ============================================================
 * Panda4ToolsIntent — 用户操作意图
 * ================================================================
 * Sealed class representing all possible user intentions.
 */
sealed class Panda4ToolsIntent {
    // ─────────────────────────────────────────────────────────
    // Module Selection / 模块选择
    // ─────────────────────────────────────────────────────────
    /** 选择工具模块 */
    data class SelectModule(val module: ToolModule) : Panda4ToolsIntent()

    // ─────────────────────────────────────────────────────────
    // Planning Review / 计划评审
    // ─────────────────────────────────────────────────────────
    /** 开始评审 */
    data class StartReview(val planFilePath: String) : Panda4ToolsIntent()

    /** 清除评审结果 */
    data object ClearReview : Panda4ToolsIntent()

    // ─────────────────────────────────────────────────────────
    // NEP Verify / NEP 验证
    // ─────────────────────────────────────────────────────────
    /** 加载 NEP 序列 */
    data class LoadNepSequence(val sequences: List<NepSequence>) : Panda4ToolsIntent()

    /** 开始验证 */
    data object StartVerification : Panda4ToolsIntent()

    /** 清除验证结果 */
    data object ClearVerification : Panda4ToolsIntent()

    // ─────────────────────────────────────────────────────────
    // Planning Git / Git 集成
    // ─────────────────────────────────────────────────────────
    /** 执行 Git 集成 */
    data class ExecuteGitIntegration(val planFilePath: String, val branchName: String) : Panda4ToolsIntent()

    // ─────────────────────────────────────────────────────────
    // Planning Audit / 审计日志
    // ─────────────────────────────────────────────────────────
    /** 加载审计日志 */
    data class LoadAuditLog(val entries: List<AuditEntry>) : Panda4ToolsIntent()

    /** 导出审计日志 */
    data class ExportAuditLog(val format: String) : Panda4ToolsIntent()

    // ─────────────────────────────────────────────────────────
    // NEP Review / NEP 代码审查
    // ─────────────────────────────────────────────────────────
    /** 创建代码审查 */
    data class CreateCodeReview(val sequences: List<NepSequence>, val targetBranch: String) : Panda4ToolsIntent()

    // ─────────────────────────────────────────────────────────
    // Gemini Starter / Gemini CLI
    // ─────────────────────────────────────────────────────────
    /** 启动 Gemini CLI */
    data class StartGeminiCli(val apiKey: String, val promptTemplate: String) : Panda4ToolsIntent()

    // ─────────────────────────────────────────────────────────
    // Quota Monitor / 配额监控
    // ─────────────────────────────────────────────────────────
    /** 查询配额 */
    data class FetchQuota(val email: String) : Panda4ToolsIntent()

    // ─────────────────────────────────────────────────────────
    // Panda Compare / 版本对比
    // ─────────────────────────────────────────────────────────
    /** 加载功能对比数据 */
    data object LoadFeatureComparison : Panda4ToolsIntent()
}

// ================================================================
// Effect / 副作用
// ================================================================

/**
 * ============================================================
 * Panda4ToolsEffect — 一次性副作用
 * ================================================================
 * One-time side effects triggered by ViewModel.
 */
sealed class Panda4ToolsEffect {
    /** 显示成功消息 */
    data class ShowSuccess(val message: String) : Panda4ToolsEffect()

    /** 显示错误消息 */
    data class ShowError(val message: String) : Panda4ToolsEffect()

    /** 导出完成通知 */
    data class ExportCompleted(val format: String, val path: String) : Panda4ToolsEffect()

    /** 代码审查创建完成 */
    data class ReviewCreated(val url: String) : Panda4ToolsEffect()
}
