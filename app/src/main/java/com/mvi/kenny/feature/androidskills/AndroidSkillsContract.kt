package com.mvi.kenny.feature.androidskills

import androidx.compose.ui.graphics.Color

// =============================================================
// AndroidSkillsContract — Android CLI & Android Skills 工具包 MVI 契约
// PRD-184: Android CLI & Android Skills 工具包
// =============================================================
// MVI Architecture Pattern / MVI 架构模式
//
// - Model (State): Immutable data class representing all UI state
// - View: Composable functions rendering UI from State
// - Intent: User intentions processed by ViewModel to update State
// - Effect: One-time side effects (navigation, snackbar, clipboard, etc.)
//
// @see AndroidSkillsViewModel State management implementation
// @see AndroidSkillsScreen UI implementation

// =============================================================
// SkillType — Skill 类型枚举
// =============================================================
/**
 * Android Skill type / Android Skill 类型
 *
 * @param label Display label / 显示标签
 * @param description Skill type description / 类型描述
 * @param iconName Material icon name / 图标名称
 */
enum class SkillType(
    val label: String,
    val description: String,
    val iconName: String
) {
    MIGRATION("迁移 / Migration", "XML to Compose / API migration / API 迁移", "swap_horiz"),
    OPTIMIZATION("优化 / Optimization", "Performance & memory optimization / 性能和内存优化", "speed"),
    DEBUGGING("调试 / Debugging", "Bug analysis & R8 tracing / 缺陷分析与 R8 追踪", "bugreport"),
    CI_CONFIG("CI 配置 / CI Config", "GitHub Actions & CI pipeline / CI 流水线配置", "build"),
    DOCUMENTATION("文档 / Documentation", "SKILL.md authoring & best practices / SKILL.md 编写规范", "description"),
    AUDIT("审核 / Audit", "Skill quality scoring & validation / Skill 质量评分与验证", "fact_check"),
    PUBLISH("发布 / Publish", "GitHub release & gh skill CLI / GitHub 发布与分发", "publish"),
    CLI_GUIDE("CLI 指南", "Android CLI task封装 / CLI 任务封装指南", "terminal"),
    MCP_INTEGRATION("MCP 集成", "Skills × MCP Server 协同 / MCP 协同集成", "hub")
}

// =============================================================
// SkillTool — Skill 可用工具定义
// =============================================================
/**
 * Tool available within a Skill / Skill 内可用的工具
 *
 * @param id Tool identifier / 工具标识符
 * @param name Tool name / 工具名称
 * @param description Tool description / 工具描述
 * @param permissionLevel Required permission level / 所需权限级别
 * @param isBuiltin Whether this is a built-in tool / 是否为内置工具
 */
data class SkillTool(
    val id: String,
    val name: String,
    val description: String,
    val permissionLevel: PermissionLevel = PermissionLevel.READ,
    val isBuiltin: Boolean = true
)

/**
 * Tool permission level / 工具权限级别
 */
enum class PermissionLevel(val label: String) {
    READ("Read-only / 只读"),
    WRITE("Read-write / 读写"),
    EXECUTE("Execute / 执行"),
    ADMIN("Admin / 管理")
}

// =============================================================
// WorkflowStep — Skill 工作流步骤
// =============================================================
/**
 * Skill workflow step / Skill 工作流步骤
 *
 * @param stepNumber Step number (1-indexed) / 步骤编号
 * @param title Step title / 步骤标题
 * @param description Step description / 步骤描述
 * @param estimatedMinutes Estimated time in minutes / 预计耗时（分钟）
 * @param isOptional Whether this step is optional / 是否为可选步骤
 */
data class WorkflowStep(
    val stepNumber: Int,
    val title: String,
    val description: String,
    val estimatedMinutes: Int = 5,
    val isOptional: Boolean = false
)

// =============================================================
// QualityScore — Skill 质量评分
// =============================================================
/**
 * Skill quality score / Skill 质量评分
 *
 * @param coverage Coverage score (0-100) / 覆盖率得分
 * @param accuracy Accuracy score (0-100) / 准确性得分
 * @param maintainability Maintainability score (0-100) / 可维护性得分
 * @param documentation Documentation completeness score (0-100) / 文档完整性得分
 * @param overall Overall weighted score (0-100) / 综合加权得分
 */
data class QualityScore(
    val coverage: Int,
    val accuracy: Int,
    val maintainability: Int,
    val documentation: Int
) {
    val overall: Int
        get() = (coverage * 0.3 + accuracy * 0.3 + maintainability * 0.2 + documentation * 0.2).toInt()

    val grade: QualityGrade
        get() = when {
            overall >= 90 -> QualityGrade.EXCELLENT
            overall >= 75 -> QualityGrade.GOOD
            overall >= 60 -> QualityGrade.FAIR
            else -> QualityGrade.NEEDS_IMPROVEMENT
        }
}

/**
 * Quality grade / 质量等级
 */
enum class QualityGrade(val label: String, val color: Color) {
    EXCELLENT("Excellent / 优秀", Color(0xFF4CAF50)),
    GOOD("Good / 良好", Color(0xFF8BC34A)),
    FAIR("Fair / 一般", Color(0xFFFF9800)),
    NEEDS_IMPROVEMENT("Needs Improvement / 需改进", Color(0xFFF44336))
}

// =============================================================
// AuditResult — Skill 审核结果
// =============================================================
/**
 * Skill audit result / Skill 审核结果
 *
 * @param isPassed Whether the audit passed / 审核是否通过
 * @param warnings List of warning messages / 警告信息列表
 * @param errors List of error messages / 错误信息列表
 * @param suggestions List of improvement suggestions / 改进建议列表
 * @param score Overall quality score / 综合质量评分
 */
data class AuditResult(
    val isPassed: Boolean,
    val warnings: List<String> = emptyList(),
    val errors: List<String> = emptyList(),
    val suggestions: List<String> = emptyList(),
    val score: QualityScore? = null
)

// =============================================================
// SkillTemplate — Skill 模板
// =============================================================
/**
 * Skill template / Skill 模板
 *
 * @param id Template identifier / 模板标识符
 * @param name Template name / 模板名称
 * @param description Template description / 模板描述
 * @param category Skill type category / Skill 类型分类
 * @param fileName Generated SKILL.md filename / 生成的 SKILL.md 文件名
 * @param content Template content / 模板内容
 * @param tags Search tags / 搜索标签
 */
data class SkillTemplate(
    val id: String,
    val name: String,
    val description: String,
    val category: SkillType,
    val fileName: String,
    val content: String,
    val tags: List<String>
)

// =============================================================
// AgentInfo — Agent 能力信息
// =============================================================
/**
 * AI Agent capability information / AI Agent 能力信息
 *
 * @param name Agent name / Agent 名称
 * @param supportedFeatures List of supported features / 支持的功能列表
 * @param skillFormat Skill format supported / 支持的 Skill 格式
 * @param installCommand CLI install command / CLI 安装命令
 */
data class AgentInfo(
    val name: String,
    val supportedFeatures: List<String>,
    val skillFormat: String,
    val installCommand: String
)

// =============================================================
// AndroidSkillsTab — 功能 Tab
// =============================================================
/**
 * Feature tab / 功能 Tab
 *
 * @param title Tab display title / Tab 显示标题
 */
enum class AndroidSkillsTab(val title: String) {
    SKILL_CREATOR("Skill Creator"),
    SKILL_PUBLISHER("Publisher"),
    SKILL_AUDIT("Audit"),
    TEMPLATES("Templates"),
    AGENT_MATRIX("Agent Matrix"),
    CLI_GUIDE("CLI Guide"),
    MCP_GUIDE("MCP Guide"),
    VERSION_MGMT("Version"),
    QUICK_START("Quick Start")
}

// =============================================================
// SkillCreatorState — Skill Creator 状态（设计文档第 6 节定义）
// =============================================================
/**
 * Skill Creator State / Skill Creator 状态
 *
 * Single source of truth for the Skill Creator UI.
 * Tracks skill type selection, name, tools, workflow, and audit state.
 *
 * @param skillType Selected skill type / 选中的 Skill 类型
 * @param skillName Skill name / Skill 名称
 * @param skillDescription Skill description / Skill 描述
 * @param triggerConditions List of trigger conditions / 触发条件列表
 * @param tools List of available tools / 可用工具列表
 * @param workflow List of workflow steps / 工作流步骤列表
 * @param qualityScore Quality score result / 质量评分结果
 * @param auditResult Audit result / 审核结果
 * @param isPublishing Whether publishing is in progress / 是否正在发布
 * @param isRunningAudit Whether audit is running / 是否正在运行审核
 * @param selectedTemplate Selected skill template / 选中的 Skill 模板
 * @param generatedSkillMd Generated SKILL.md content / 生成的 SKILL.md 内容
 * @param isLoading Whether any operation is in progress / 是否有操作进行中
 * @param error Error message if any / 错误信息
 */
data class SkillCreatorState(
    val skillType: SkillType? = null,
    val skillName: String = "",
    val skillDescription: String = "",
    val triggerConditions: List<String> = emptyList(),
    val tools: List<SkillTool> = emptyList(),
    val workflow: List<WorkflowStep> = emptyList(),
    val qualityScore: QualityScore? = null,
    val auditResult: AuditResult? = null,
    val isPublishing: Boolean = false,
    val isRunningAudit: Boolean = false,
    val selectedTemplate: SkillTemplate? = null,
    val generatedSkillMd: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
) {
    companion object {
        /** Initial state / 初始状态 */
        val Initial = SkillCreatorState()
    }

    /**
     * Whether the skill can be published / 是否可以发布
     */
    val canPublish: Boolean
        get() = skillType != null && skillName.isNotBlank() && triggerConditions.isNotEmpty()

    /**
     * Whether the skill has passed audit / 是否通过审核
     */
    val isAuditPassed: Boolean
        get() = auditResult?.isPassed == true

    /**
     * Validation errors / 验证错误
     */
    val validationErrors: List<String>
        get() = buildList {
            if (skillType == null) add("Please select a skill type / 请选择 Skill 类型")
            if (skillName.isBlank()) add("Skill name is required / Skill 名称不能为空")
            if (triggerConditions.isEmpty()) add("At least one trigger condition is required / 至少需要一个触发条件")
        }
}

// =============================================================
// SkillCreatorIntent — Skill Creator 用户意图（设计文档第 6 节定义）
// =============================================================
/**
 * Skill Creator User Intents / Skill Creator 用户意图
 *
 * Every user action corresponds to an Intent.
 * ViewModel receives Intent and executes business logic.
 */
sealed interface SkillCreatorIntent {
    /** Select a skill type / 选择 Skill 类型
     * @param type Skill type to select / 要选择的 Skill 类型
     */
    data class SelectSkillType(val type: SkillType) : SkillCreatorIntent

    /** Update skill name / 更新 Skill 名称
     * @param name New skill name / 新的 Skill 名称
     */
    data class UpdateSkillName(val name: String) : SkillCreatorIntent

    /** Update skill description / 更新 Skill 描述
     * @param description New skill description / 新的 Skill 描述
     */
    data class UpdateSkillDescription(val description: String) : SkillCreatorIntent

    /** Add a trigger condition / 添加触发条件
     * @param condition Trigger condition to add / 要添加的触发条件
     */
    data class AddTriggerCondition(val condition: String) : SkillCreatorIntent

    /** Remove a trigger condition / 移除触发条件
     * @param condition Trigger condition to remove / 要移除的触发条件
     */
    data class RemoveTriggerCondition(val condition: String) : SkillCreatorIntent

    /** Add a tool to the skill / 添加工具到 Skill
     * @param tool Tool to add / 要添加的工具
     */
    data class AddTool(val tool: SkillTool) : SkillCreatorIntent

    /** Remove a tool from the skill / 从 Skill 移除工具
     * @param tool Tool to remove / 要移除的工具
     */
    data class RemoveTool(val tool: SkillTool) : SkillCreatorIntent

    /** Add a workflow step / 添加工作流步骤
     * @param step Workflow step to add / 要添加的工作流步骤
     */
    data class AddWorkflowStep(val step: WorkflowStep) : SkillCreatorIntent

    /** Remove a workflow step / 移除工作流步骤
     * @param stepIndex Step index to remove / 要移除的步骤索引
     */
    data class RemoveWorkflowStep(val stepIndex: Int) : SkillCreatorIntent

    /** Run skill audit / 运行 Skill 审核
     * Triggers quality scoring and validation checks
     */
    data object RunAudit : SkillCreatorIntent

    /** Publish skill / 发布 Skill */
    data object Publish : SkillCreatorIntent

    /** Select a skill template / 选择 Skill 模板
     * @param template Template to select / 要选择的模板
     */
    data class SelectTemplate(val template: SkillTemplate) : SkillCreatorIntent

    /** Generate SKILL.md content / 生成 SKILL.md 内容 */
    data object GenerateSkillMd : SkillCreatorIntent

    /** Copy generated SKILL.md to clipboard / 复制 SKILL.md 到剪贴板 */
    data object CopyToClipboard : SkillCreatorIntent

    /** Export skill to specified path / 导出 Skill 到指定路径
     * @param path Target directory path / 目标目录路径
     */
    data class ExportSkill(val path: String) : SkillCreatorIntent

    /** Clear all inputs / 清除所有输入 */
    data object ClearAll : SkillCreatorIntent

    /** Dismiss error message / 关闭错误信息 */
    data object DismissError : SkillCreatorIntent
}

// =============================================================
// SkillCreatorEffect — Skill Creator 副作用（设计文档第 6 节定义）
// =============================================================
/**
 * Skill Creator Side Effects / Skill Creator 副作用
 *
 * One-time events consumed only once by the UI layer.
 * UI layer listens via LaunchedEffect + flow.collect{}.
 */
sealed interface SkillCreatorEffect {
    /** Show audit warning / 显示审核警告
     * @param message Warning message / 警告信息
     */
    data class ShowAuditWarning(val message: String) : SkillCreatorEffect

    /** Navigate to publish / 导航到发布页面
     * @param repoUrl Repository URL / 仓库 URL
     */
    data class NavigateToPublish(val repoUrl: String) : SkillCreatorEffect

    /** Show quality score / 显示质量评分
     * @param score Quality score / 质量评分
     */
    data class ShowQualityScore(val score: QualityScore) : SkillCreatorEffect

    /** Show snackbar message / 显示 Snackbar 消息
     * @param message Message to display / 要显示的消息
     * @param isError Whether this is an error / 是否为错误
     */
    data class ShowSnackbar(val message: String, val isError: Boolean = false) : SkillCreatorEffect

    /** Copy to clipboard success / 复制到剪贴板成功
     * @param content Copied content / 已复制的内容
     */
    data class CopySuccess(val content: String) : SkillCreatorEffect

    /** Skill exported successfully / Skill 导出成功
     * @param path Exported file path / 导出文件路径
     */
    data class ExportSuccess(val path: String) : SkillCreatorEffect

    /** Skill published successfully / Skill 发布成功
     * @param repoUrl Repository URL / 仓库 URL
     */
    data class PublishSuccess(val repoUrl: String) : SkillCreatorEffect
}

// =============================================================
// AndroidSkillsState — Android Skills 工具包主状态
// =============================================================
/**
 * Android Skills Toolkit State / Android Skills 工具包主状态
 *
 * @param activeTab Currently active tab / 当前活跃的 Tab
 * @param skillCreatorState Skill Creator sub-feature state / Skill Creator 子功能状态
 * @param isLoading Whether any background operation is in progress / 是否有后台操作进行中
 * @param error Error message if any / 错误信息
 */
data class AndroidSkillsState(
    val activeTab: AndroidSkillsTab = AndroidSkillsTab.SKILL_CREATOR,
    val skillCreatorState: SkillCreatorState = SkillCreatorState.Initial,
    val isLoading: Boolean = false,
    val error: String? = null
) {
    companion object {
        /** Initial state / 初始状态 */
        val Initial = AndroidSkillsState()
    }
}

// =============================================================
// AndroidSkillsIntent — Android Skills 工具包用户意图
// =============================================================
/**
 * Android Skills Toolkit User Intents / Android Skills 工具包用户意图
 */
sealed interface AndroidSkillsIntent {
    /** Select a tab / 选择 Tab
     * @param tab Tab to select / 要选择的 Tab
     */
    data class SelectTab(val tab: AndroidSkillsTab) : AndroidSkillsIntent

    /** Forward intent to Skill Creator / 转发意图到 Skill Creator
     * @param intent Intent to forward / 要转发的意图
     */
    data class ForwardToSkillCreator(val intent: SkillCreatorIntent) : AndroidSkillsIntent

    /** Dismiss error message / 关闭错误信息 */
    data object DismissError : AndroidSkillsIntent
}

// =============================================================
// AndroidSkillsEffect — Android Skills 工具包副作用
// =============================================================
/**
 * Android Skills Toolkit Side Effects / Android Skills 工具包副作用
 */
sealed interface AndroidSkillsEffect {
    /** Show snackbar message / 显示 Snackbar 消息
     * @param message Message to display / 要显示的消息
     * @param isError Whether this is an error / 是否为错误
     */
    data class ShowSnackbar(val message: String, val isError: Boolean = false) : AndroidSkillsEffect
}
