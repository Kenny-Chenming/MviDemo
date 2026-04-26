package com.mvi.kenny.feature.pandatools

// ============================================================
// PandaToolsContract — Android Studio Panda 4 AI Tools MVI Contract
// PRD-136 | Android Studio Panda 4 AI 编程助手开发工具包
// ============================================================
/**
 * MVI (Model-View-Intent) Architecture Pattern.
 *
 * MVI 三要素 / Three Elements:
 * - Model (State): 页面状态的唯一真相来源，Immutable 数据类
 * - View: Composable 函数，消费 State，渲染 UI
 * - Intent: 用户意图（用户操作），ViewModel 收到 Intent 后执行业务逻辑
 *
 * Effect: 一次性副作用（导航、Toast），通过 Channel 传递
 *
 * @see PandaToolsViewModel
 * @see PandaToolsScreen
 */

// ============================================================
// Tab 枚举 / Tab Enumeration
// ============================================================
/**
 * PandaTools 五大核心 Tab。
 * Five core tabs of PandaTools.
 *
 * @param titleCn 中文标题
 * @param titleEn 英文标题
 * @param iconName Material 图标名称
 */
enum class PandaTab(
    val titleCn: String,
    val titleEn: String,
    val iconName: String
) {
    SkillsEditor("Skills 编辑器", "Skills Editor", "EditDocument"),
    PlanManagement("计划管理", "Plan Management", "Plan"),
    NepAnalysis("NEP 分析", "NEP Analysis", "Analytics"),
    PermissionAudit("权限审计", "Permission Audit", "Security"),
    QualityScore("质量评分", "Quality Score", "Star")
}

// ============================================================
// 数据模型 / Data Models
// ============================================================

/**
 * Skill 文件元数据。
 * Skill file metadata.
 *
 * @param id 文件唯一 ID
 * @param name 文件名
 * @param path 文件路径
 * @param category 分类（Official / Community）
 * @param isValid 是否通过语法验证
 */
data class SkillFile(
    val id: String,
    val name: String,
    val path: String,
    val category: String = "Community",
    val isValid: Boolean = true
)

/**
 * YAML 验证错误。
 * YAML validation error.
 *
 * @param line 错误所在行
 * @param message 错误信息
 * @param severity 严重程度（Error / Warning）
 */
data class ValidationError(
    val line: Int,
    val message: String,
    val severity: String = "Error"
)

/**
 * Agent 计划。
 * Agent implementation plan.
 *
 * @param id 计划 ID
 * @param title 计划标题
 * @param createdAt 创建时间
 * @param updatedAt 更新时间
 * @param version 版本号
 * @param status 状态（Draft / Pending / Approved / Rejected / Executing / Completed）
 * @param steps 步骤列表
 * @param developerComments 开发者评审注释
 */
data class AgentPlan(
    val id: String,
    val title: String,
    val createdAt: String,
    val updatedAt: String,
    val version: Int = 1,
    val status: String = "Pending",
    val steps: List<PlanStep> = emptyList(),
    val developerComments: List<PlanComment> = emptyList()
)

/**
 * 计划步骤。
 * Plan step.
 *
 * @param id 步骤 ID
 * @param title 步骤标题
 * @param description 步骤描述
 * @param isCompleted 是否完成
 * @param order 顺序
 */
data class PlanStep(
    val id: String,
    val title: String,
    val description: String,
    val isCompleted: Boolean = false,
    val order: Int
)

/**
 * 计划评审注释。
 * Plan review comment.
 *
 * @param id 注释 ID
 * @param author 作者
 * @param content 内容
 * @param timestamp 时间戳
 * @param type 类型（Approve / RequestChange / General）
 */
data class PlanComment(
    val id: String,
    val author: String,
    val content: String,
    val timestamp: String,
    val type: String = "General"
)

/**
 * NEP 预测记录。
 * Next Edit Prediction record.
 *
 * @param id 记录 ID
 * @param predictedContent 预测内容
 * @param actualContent 实际内容
 * @param isMatch 是否匹配
 * @param timestamp 时间戳
 * @param filePath 文件路径
 * @param language 编程语言
 */
data class PredictionRecord(
    val id: String,
    val predictedContent: String,
    val actualContent: String,
    val isMatch: Boolean,
    val timestamp: String,
    val filePath: String,
    val language: String
)

/**
 * 代码风格画像。
 * Code style profile.
 *
 * @param languagePreference 主要语言偏好
 * @param namingPattern 命名习惯（camelCase / snake_case / PascalCase）
 * @param refactoringPatterns 重构模式标签列表
 * @param avgLineLength 平均行长度
 */
data class CodeStyleProfile(
    val languagePreference: String,
    val namingPattern: String,
    val refactoringPatterns: List<String>,
    val avgLineLength: Int
)

/**
 * 审计概览。
 * Audit overview.
 *
 * @param totalPermissions 权限总数
 * @param coveredPermissions 已覆盖权限数
 * @param overGrantedCount 过度授权数量
 * @param riskLevel 风险等级（High / Medium / Low）
 * @param coverageRate 覆盖率
 */
data class AuditOverview(
    val totalPermissions: Int,
    val coveredPermissions: Int,
    val overGrantedCount: Int,
    val riskLevel: String,
    val coverageRate: Float
)

/**
 * 权限矩阵行。
 * Permission matrix row.
 *
 * @param operation 操作类型
 * @param fileAccess 文件访问权限级别
 * @param networkAccess 网络访问权限级别
 * @param execAccess 执行权限级别
 * @param riskLevel 风险等级
 */
data class PermissionRow(
    val operation: String,
    val fileAccess: String,
    val networkAccess: String,
    val execAccess: String,
    val riskLevel: String
)

/**
 * 风险项。
 * Risk item.
 *
 * @param id 风险项 ID
 * @param title 标题
 * @param description 描述
 * @param riskLevel 风险等级
 * @param suggestion 修复建议
 */
data class RiskItem(
    val id: String,
    val title: String,
    val description: String,
    val riskLevel: String,
    val suggestion: String
)

/**
 * Skill 卡片数据。
 * Skill card data.
 *
 * @param id Skill ID
 * @param name 名称
 * @param description 描述
 * @param author 作者（Official / Community Username）
 * @param rating 评分（0-5）
 * @param usageCount 使用量
 * @param category 分类
 * @param isOfficial 是否官方认证
 * @param tags 功能标签
 */
data class SkillCard(
    val id: String,
    val name: String,
    val description: String,
    val author: String,
    val rating: Float,
    val usageCount: Int,
    val category: String,
    val isOfficial: Boolean,
    val tags: List<String>
)

/**
 * Skill 排序选项。
 * Skill sort option.
 */
enum class SkillSortOption(val labelCn: String, val labelEn: String) {
    Rating("评分", "Rating"),
    UsageCount("使用量", "Usage Count"),
    Latest("最新", "Latest"),
    Alphabetical("字母序", "Alphabetical")
}

// ============================================================
// State — 页面状态
// ============================================================

/**
 * PandaTools 全局状态。
 * Global state for PandaTools.
 *
 * @param currentTab 当前激活的 Tab
 * @param skillsEditorState Skills 编辑器状态
 * @param planManagementState 计划管理状态
 * @param nepAnalysisState NEP 分析状态
 * @param permissionAuditState 权限审计状态
 * @param qualityScoreState 质量评分状态
 */
data class PandaToolsState(
    val currentTab: PandaTab = PandaTab.SkillsEditor,
    val skillsEditorState: SkillsEditorState = SkillsEditorState(),
    val planManagementState: PlanManagementState = PlanManagementState(),
    val nepAnalysisState: NepAnalysisState = NepAnalysisState(),
    val permissionAuditState: PermissionAuditState = PermissionAuditState(),
    val qualityScoreState: QualityScoreState = QualityScoreState()
)

/**
 * Skills 编辑器状态。
 * Skills editor state.
 *
 * @param projectPath 当前项目路径
 * @param selectedFile 选中的文件
 * @param fileContent 文件内容（编辑器文本）
 * @param isDirty 是否有未保存更改
 * @param validationErrors 验证错误列表
 * @param validationOutput 验证输出文本
 * @param isLoading 是否正在加载
 */
data class SkillsEditorState(
    val projectPath: String = "",
    val selectedFile: SkillFile? = null,
    val fileContent: String = "",
    val isDirty: Boolean = false,
    val validationErrors: List<ValidationError> = emptyList(),
    val validationOutput: String = "",
    val isLoading: Boolean = false
)

/**
 * 计划管理状态。
 * Plan management state.
 *
 * @param plans 计划列表
 * @param selectedPlan 选中的计划
 * @param isLoading 是否正在加载
 * @param diffVersions 用于对比的两个版本
 */
data class PlanManagementState(
    val plans: List<AgentPlan> = emptyList(),
    val selectedPlan: AgentPlan? = null,
    val isLoading: Boolean = false,
    val diffVersions: Pair<AgentPlan, AgentPlan>? = null
)

/**
 * NEP 分析状态。
 * NEP analysis state.
 *
 * @param totalPredictions 总预测次数
 * @param accuracyRate 准确率
 * @param codeStyleProfile 代码风格画像
 * @param predictionHistory 预测历史
 * @param selectedRecord 选中的记录
 */
data class NepAnalysisState(
    val totalPredictions: Int = 0,
    val accuracyRate: Float = 0f,
    val codeStyleProfile: CodeStyleProfile? = null,
    val predictionHistory: List<PredictionRecord> = emptyList(),
    val selectedRecord: PredictionRecord? = null
)

/**
 * 权限审计状态。
 * Permission audit state.
 *
 * @param auditOverview 审计概览
 * @param permissionMatrix 权限矩阵
 * @param riskItems 风险项列表
 * @param isAuditing 是否正在审计
 */
data class PermissionAuditState(
    val auditOverview: AuditOverview? = null,
    val permissionMatrix: List<PermissionRow> = emptyList(),
    val riskItems: List<RiskItem> = emptyList(),
    val isAuditing: Boolean = false
)

/**
 * 质量评分状态。
 * Quality score state.
 *
 * @param skills Skill 列表
 * @param selectedSkill 选中的 Skill
 * @param sortBy 排序方式
 * @param filterCategory 筛选分类
 */
data class QualityScoreState(
    val skills: List<SkillCard> = emptyList(),
    val selectedSkill: SkillCard? = null,
    val sortBy: SkillSortOption = SkillSortOption.Rating,
    val filterCategory: String? = null
)

// ============================================================
// Intent — 用户意图
// ============================================================

/**
 * PandaTools 用户意图。
 * User intents for PandaTools.
 */
sealed class PandaToolsIntent {
    // Tab 切换 / Tab switching
    data class SwitchTab(val tab: PandaTab) : PandaToolsIntent()

    // Skills 编辑器 / Skills Editor
    data class OpenProject(val path: String) : PandaToolsIntent()
    data class SelectFile(val file: SkillFile) : PandaToolsIntent()
    data class UpdateFileContent(val content: String) : PandaToolsIntent()
    data object SaveFile : PandaToolsIntent()
    data object ValidateFile : PandaToolsIntent()

    // 计划管理 / Plan Management
    data class SelectPlan(val plan: AgentPlan) : PandaToolsIntent()
    data class ApprovePlan(val planId: String) : PandaToolsIntent()
    data class RequestPlanRevision(val planId: String, val feedback: String) : PandaToolsIntent()
    data class ComparePlanVersions(val v1: AgentPlan, val v2: AgentPlan) : PandaToolsIntent()
    data class RollbackPlan(val planId: String, val targetVersion: Int) : PandaToolsIntent()

    // NEP 分析 / NEP Analysis
    data class SelectPredictionRecord(val record: PredictionRecord) : PandaToolsIntent()
    data class MarkPredictionMisclassification(val recordId: String) : PandaToolsIntent()

    // 权限审计 / Permission Audit
    data object RunPermissionAudit : PandaToolsIntent()

    // 质量评分 / Quality Score
    data class SortSkills(val option: SkillSortOption) : PandaToolsIntent()
    data class FilterSkillsByCategory(val category: String?) : PandaToolsIntent()
    data class SelectSkill(val skill: SkillCard) : PandaToolsIntent()
}

// ============================================================
// Effect — 副作用
// ============================================================

/**
 * PandaTools 副作用。
 * One-time side effects for PandaTools.
 */
sealed class PandaToolsEffect {
    data class ShowSnackbar(val message: String) : PandaToolsEffect()
    data object FileSaved : PandaToolsEffect()
    data class ValidationFailed(val errors: List<ValidationError>) : PandaToolsEffect()
    data class PlanApproved(val planId: String) : PandaToolsEffect()
    data class PlanRolledBack(val planId: String, val version: Int) : PandaToolsEffect()
    data class ShowError(val message: String) : PandaToolsEffect()
    data object AuditComplete : PandaToolsEffect()
    data class ShowPlanDiff(val v1: AgentPlan, val v2: AgentPlan) : PandaToolsEffect()
}
