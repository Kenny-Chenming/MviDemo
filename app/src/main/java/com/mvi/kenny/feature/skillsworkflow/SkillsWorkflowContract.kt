package com.mvi.kenny.feature.skillsworkflow

import androidx.compose.ui.graphics.Color

// =============================================================
// SkillsWorkflowContract — Android CLI Skills Workflow 自动化工具包 MVI 契约
// PRD-195: Android CLI Skills Workflow 自动化工具包
// =============================================================
// MVI Architecture Pattern / MVI 架构模式
//
// - Model (State): Immutable data class representing all UI state
// - View: Composable functions rendering UI from State
// - Intent: User intentions processed by ViewModel to update State
// - Effect: One-time side effects (navigation, snackbar, etc.)

// =============================================================
// Color Palette — 深色 Terminal 风格配色（设计文档第 7 节定义）
// =============================================================
/**
 * Deep Terminal color palette / 深色 Terminal 风格配色
 *
 * Aligned with GitHub Dark theme and Android Studio dark theme.
 * Provides consistent visual language across the toolkit.
 */
object WorkflowColors {
    val Primary = Color(0xFF4CAF50)        // Android 绿 / Android Green
    val Background = Color(0xFF0D1117)      // GitHub 深色背景 / GitHub Dark Background
    val Surface = Color(0xFF161B22)          // 卡片表面 / Card Surface
    val Card = Color(0xFF21262D)            // 卡片背景 / Card Background
    val TextPrimary = Color(0xFFE6EDF3)      // 主文字 / Primary Text
    val TextSecondary = Color(0xFF8B949E)    // 次文字 / Secondary Text
    val Accent = Color(0xFF58A6FF)          // 链接/高亮 / Link/Highlight
    val Success = Color(0xFF3FB950)         // 成功 / Success
    val Warning = Color(0xFFD29922)          // 警告 / Warning
    val Error = Color(0xFFF85149)            // 错误 / Error
    val TerminalGreen = Color(0xFF39D353)    // Terminal 绿色输出 / Terminal Green Output
}

// =============================================================
// SkillInfo — Skill 信息
// =============================================================
/**
 * Skill information / Skill 信息
 *
 * Represents a single Skill with its metadata and status.
 *
 * @param id Unique identifier / 唯一标识符
 * @param name Skill display name / Skill 显示名称
 * @param version Semantic version / 语义化版本
 * @param author Author or organization / 作者或组织
 * @param description Brief description / 简短描述
 * @param category Skill category / Skill 分类
 * @param status Skill lifecycle status / Skill 生命周期状态
 * @param downloadCount Download count / 下载次数
 * @param rating Average rating (0-5) / 平均评分
 * @param tags Search tags / 搜索标签
 * @param installCommand CLI install command / CLI 安装命令
 */
data class SkillInfo(
    val id: String,
    val name: String,
    val version: String,
    val author: String,
    val description: String,
    val category: SkillCategory,
    val status: SkillStatus,
    val downloadCount: Int = 0,
    val rating: Float = 0f,
    val tags: List<String> = emptyList(),
    val installCommand: String = ""
) {
    /** Full install command / 完整安装命令 */
    val fullInstallCommand: String
        get() = if (installCommand.isNotEmpty()) installCommand else "gh skill install $id"
}

/**
 * Skill category / Skill 分类
 */
enum class SkillCategory(val label: String, val emoji: String) {
    MIGRATION("迁移 Migration", "🔄"),
    OPTIMIZATION("优化 Optimization", "⚡"),
    DEBUGGING("调试 Debugging", "🐛"),
    CI_CD("CI/CD", "🔧"),
    DOCUMENTATION("文档 Documentation", "📝"),
    AUDIT("审核 Audit", "✅"),
    TEMPLATE("模板 Template", "📦"),
    WORKFLOW("工作流 Workflow", "🔗")
}

/**
 * Skill lifecycle status / Skill 生命周期状态
 */
enum class SkillStatus(val label: String, val color: Color) {
    DRAFT("Draft / 草稿", WorkflowColors.Warning),
    PUBLISHED("Published / 已发布", WorkflowColors.Success),
    DEPRECATED("Deprecated / 已弃用", WorkflowColors.Error),
    UNDER_REVIEW("Under Review / 审核中", WorkflowColors.Accent)
}

// =============================================================
// WorkflowNode — 工作流节点
// =============================================================
/**
 * Workflow node / 工作流节点
 *
 * Represents a single step/skill in a workflow graph.
 *
 * @param id Unique node ID / 唯一节点 ID
 * @param skillId Referenced skill ID / 引用的 Skill ID
 * @param skillName Skill display name / Skill 显示名称
 * @param positionX X coordinate in canvas / 画布 X 坐标
 * @param positionY Y coordinate in canvas / 画布 Y 坐标
 * @param inputs Input mappings / 输入映射
 * @param outputs Output mappings / 输出映射
 * @param isEntryPoint Whether this is a workflow entry / 是否为工作流入口
 */
data class WorkflowNode(
    val id: String,
    val skillId: String,
    val skillName: String,
    val positionX: Float = 0f,
    val positionY: Float = 0f,
    val inputs: List<InputMapping> = emptyList(),
    val outputs: List<OutputMapping> = emptyList(),
    val isEntryPoint: Boolean = false
)

/**
 * Input mapping for workflow node / 工作流节点输入映射
 */
data class InputMapping(
    val paramName: String,
    val sourceNodeId: String?,
    val sourceOutputName: String?,
    val defaultValue: String = ""
)

/**
 * Output mapping for workflow node / 工作流节点输出映射
 */
data class OutputMapping(
    val outputName: String,
    val targetNodeId: String?,
    val targetInputName: String?
)

// =============================================================
// WorkflowEdge — 工作流边（连接线）
// =============================================================
/**
 * Workflow edge / 工作流边
 *
 * Represents a directed connection between two workflow nodes.
 *
 * @param id Unique edge ID / 唯一边 ID
 * @param fromNodeId Source node ID / 源节点 ID
 * @param toNodeId Target node ID / 目标节点 ID
 * @param label Optional edge label / 可选边标签
 * @param condition Optional condition for execution / 可选执行条件
 */
data class WorkflowEdge(
    val id: String,
    val fromNodeId: String,
    val toNodeId: String,
    val label: String = "",
    val condition: String = ""
)

// =============================================================
// TestReport — 技能测试报告
// =============================================================
/**
 * Skill test report / Skill 测试报告
 *
 * @param skillId Tested skill ID / 测试的 Skill ID
 * @param passedTestCount Number of passed tests / 通过测试数
 * @param failedTestCount Number of failed tests / 失败测试数
 * @param totalTestCount Total number of tests / 总测试数
 * @param coverage Test coverage percentage / 测试覆盖率
 * @param executionTimeMs Total execution time in milliseconds / 总执行时间（毫秒）
 * @param testResults List of individual test results / 单个测试结果列表
 */
data class TestReport(
    val skillId: String,
    val passedTestCount: Int,
    val failedTestCount: Int,
    val totalTestCount: Int,
    val coverage: Int,
    val executionTimeMs: Long,
    val testResults: List<TestResult> = emptyList()
) {
    val passRate: Float
        get() = if (totalTestCount > 0) passedTestCount.toFloat() / totalTestCount else 0f

    val isPassed: Boolean
        get() = failedTestCount == 0
}

/**
 * Individual test result / 单个测试结果
 */
data class TestResult(
    val testName: String,
    val isPassed: Boolean,
    val message: String = "",
    val durationMs: Long = 0
)

// =============================================================
// WizardStep — 安装向导步骤
// =============================================================
/**
 * Installation wizard step / 安装向导步骤
 *
 * @param stepNumber Step number (1-indexed) / 步骤编号
 * @param title Step title / 步骤标题
 * @param description Step description / 步骤描述
 * @param isCompleted Whether step is completed / 步骤是否完成
 * @param isOptional Whether step is optional / 步骤是否可选
 */
data class WizardStep(
    val stepNumber: Int,
    val title: String,
    val description: String,
    val isCompleted: Boolean = false,
    val isOptional: Boolean = false
)

// =============================================================
// SkillsWorkflowTab — 功能 Tab
// =============================================================
/**
 * Feature tab / 功能 Tab
 *
 * @param title Tab display title / Tab 显示标题
 * @param iconName Material icon name / 图标名称
 */
enum class SkillsWorkflowTab(val title: String, val iconName: String) {
    SKILL_HUB("Skill Center / 技能中心", "hub"),
    WORKFLOW_EDITOR("Workflow / 工作流编排", "account_tree"),
    WIZARD("Setup Wizard / 安装向导", "build_circle"),
    SKILL_DETAIL("Skill Detail / 技能详情", "info")
}

// =============================================================
// SkillHubState — Skill Center 状态
// =============================================================
/**
 * Skill Hub State / 技能中心状态
 *
 * @param installedSkills List of locally installed skills / 本地已安装 Skill 列表
 * @param marketSkills List of skills available in the market / 市场可用 Skill 列表
 * @param searchQuery Current search query / 当前搜索查询
 * @param selectedCategory Filter by category / 按分类筛选
 * @param isLoading Whether loading is in progress / 是否正在加载
 * @param error Error message if any / 错误信息
 */
data class SkillHubState(
    val installedSkills: List<SkillInfo> = emptyList(),
    val marketSkills: List<SkillInfo> = emptyList(),
    val searchQuery: String = "",
    val selectedCategory: SkillCategory? = null,
    val isLoading: Boolean = false,
    val error: String? = null
) {
    companion object {
        /** Initial state / 初始状态 */
        val Initial = SkillHubState()
    }

    /**
     * Filtered market skills based on search and category / 基于搜索和分类筛选的市场 Skill
     */
    val filteredMarketSkills: List<SkillInfo>
        get() = marketSkills.filter { skill ->
            val matchesSearch = searchQuery.isEmpty() ||
                    skill.name.contains(searchQuery, ignoreCase = true) ||
                    skill.description.contains(searchQuery, ignoreCase = true) ||
                    skill.tags.any { it.contains(searchQuery, ignoreCase = true) }
            val matchesCategory = selectedCategory == null || skill.category == selectedCategory
            matchesSearch && matchesCategory
        }
}

// =============================================================
// SkillHubIntent — Skill Center 用户意图
// =============================================================
/**
 * Skill Hub User Intents / 技能中心用户意图
 *
 * All user actions in the Skill Hub screen.
 */
sealed interface SkillHubIntent {
    /** Load skills from local and market / 从本地和市场加载 Skill
     * Triggers initial data loading or refresh.
     */
    data object LoadSkills : SkillHubIntent

    /** Search skills / 搜索 Skill
     * @param query Search query string / 搜索查询字符串
     */
    data class SearchSkills(val query: String) : SkillHubIntent

    /** Filter by category / 按分类筛选
     * @param category Category to filter by, null for all / 筛选分类，null 表示全部
     */
    data class FilterByCategory(val category: SkillCategory?) : SkillHubIntent

    /** Install a skill / 安装 Skill
     * @param skillId Skill ID to install / 要安装的 Skill ID
     */
    data class InstallSkill(val skillId: String) : SkillHubIntent

    /** Uninstall a skill / 卸载 Skill
     * @param skillId Skill ID to uninstall / 要卸载的 Skill ID
     */
    data class UninstallSkill(val skillId: String) : SkillHubIntent

    /** Open skill detail / 打开 Skill 详情
     * @param skillId Skill ID to view / 要查看的 Skill ID
     */
    data class OpenSkillDetail(val skillId: String) : SkillHubIntent

    /** Dismiss error / 关闭错误信息 */
    data object DismissError : SkillHubIntent
}

// =============================================================
// SkillHubEffect — Skill Center 副作用
// =============================================================
/**
 * Skill Hub Side Effects / 技能中心副作用
 *
 * One-time events consumed only once by the UI layer.
 */
sealed interface SkillHubEffect {
    /** Show install success / 显示安装成功
     * @param skillName Installed skill name / 已安装的 Skill 名称
     */
    data class ShowInstallSuccess(val skillName: String) : SkillHubEffect

    /** Show uninstall success / 显示卸载成功
     * @param skillName Uninstalled skill name / 已卸载的 Skill 名称
     */
    data class ShowUninstallSuccess(val skillName: String) : SkillHubEffect

    /** Navigate to skill detail / 导航到 Skill 详情
     * @param skillId Skill ID to navigate to / 要导航到的 Skill ID
     */
    data class NavigateToDetail(val skillId: String) : SkillHubEffect

    /** Show snackbar / 显示 Snackbar
     * @param message Message to display / 要显示的消息
     * @param isError Whether this is an error / 是否为错误
     */
    data class ShowSnackbar(val message: String, val isError: Boolean = false) : SkillHubEffect
}

// =============================================================
// SkillDetailState — Skill Detail 状态
// =============================================================
/**
 * Skill Detail State / 技能详情状态
 *
 * @param skill Skill information / Skill 信息
 * @param readme Raw SKILL.md content / 原始 SKILL.md 内容
 * @param testReport Cached test report / 缓存的测试报告
 * @param versionHistory List of version history / 版本历史列表
 * @param isLoading Whether loading is in progress / 是否正在加载
 * @param isRunningTest Whether test is running / 是否正在运行测试
 * @param error Error message if any / 错误信息
 */
data class SkillDetailState(
    val skill: SkillInfo? = null,
    val readme: String = "",
    val testReport: TestReport? = null,
    val versionHistory: List<String> = emptyList(),
    val isLoading: Boolean = false,
    val isRunningTest: Boolean = false,
    val error: String? = null
) {
    companion object {
        /** Initial state / 初始状态 */
        val Initial = SkillDetailState()
    }
}

// =============================================================
// SkillDetailIntent — Skill Detail 用户意图
// =============================================================
/**
 * Skill Detail User Intents / 技能详情用户意图
 */
sealed interface SkillDetailIntent {
    /** Load skill detail / 加载 Skill 详情
     * @param skillId Skill ID to load / 要加载的 Skill ID
     */
    data class LoadSkillDetail(val skillId: String) : SkillDetailIntent

    /** Run skill test / 运行 Skill 测试
     * @param skillId Skill ID to test / 要测试的 Skill ID
     */
    data class RunTest(val skillId: String) : SkillDetailIntent

    /** Publish skill / 发布 Skill
     * @param skillId Skill ID to publish / 要发布的 Skill ID
     */
    data class PublishSkill(val skillId: String) : SkillDetailIntent

    /** Copy install command / 复制安装命令
     * @param command Command to copy / 要复制的命令
     */
    data class CopyInstallCommand(val command: String) : SkillDetailIntent

    /** Dismiss error / 关闭错误信息 */
    data object DismissError : SkillDetailIntent
}

// =============================================================
// SkillDetailEffect — Skill Detail 副作用
// =============================================================
/**
 * Skill Detail Side Effects / 技能详情副作用
 */
sealed interface SkillDetailEffect {
    /** Show publish success / 显示发布成功
     * @param repoUrl Repository URL / 仓库 URL
     */
    data class ShowPublishSuccess(val repoUrl: String) : SkillDetailEffect

    /** Show test complete / 显示测试完成
     * @param report Test report / 测试报告
     */
    data class ShowTestComplete(val report: TestReport) : SkillDetailEffect

    /** Copy to clipboard success / 复制到剪贴板成功
     * @param content Copied content / 已复制的内容
     */
    data class CopySuccess(val content: String) : SkillDetailEffect

    /** Show snackbar / 显示 Snackbar
     * @param message Message to display / 要显示的消息
     * @param isError Whether this is an error / 是否为错误
     */
    data class ShowSnackbar(val message: String, val isError: Boolean = false) : SkillDetailEffect
}

// =============================================================
// WorkflowState — Workflow Editor 状态
// =============================================================
/**
 * Workflow Editor State / 工作流编排器状态
 *
 * @param nodes List of workflow nodes / 工作流节点列表
 * @param edges List of workflow edges / 工作流边列表
 * @param selectedNodeId Currently selected node ID / 当前选中的节点 ID
 * @param workflowName Workflow name / 工作流名称
 * @param workflowDescription Workflow description / 工作流描述
 * @param isExecuting Whether workflow is executing / 工作流是否正在执行
 * @param executionLog Execution log lines / 执行日志行
 * @param isSaving Whether workflow is being saved / 工作流是否正在保存
 * @param error Error message if any / 错误信息
 */
data class WorkflowState(
    val nodes: List<WorkflowNode> = emptyList(),
    val edges: List<WorkflowEdge> = emptyList(),
    val selectedNodeId: String? = null,
    val workflowName: String = "",
    val workflowDescription: String = "",
    val isExecuting: Boolean = false,
    val executionLog: List<String> = emptyList(),
    val isSaving: Boolean = false,
    val error: String? = null
) {
    companion object {
        /** Initial state / 初始状态 */
        val Initial = WorkflowState()
    }

    /**
     * Selected node / 选中的节点
     */
    val selectedNode: WorkflowNode?
        get() = nodes.find { it.id == selectedNodeId }

    /**
     * Whether workflow can be executed / 工作流是否可以执行
     */
    val canExecute: Boolean
        get() = nodes.isNotEmpty() && !isExecuting && nodes.any { it.isEntryPoint }
}

// =============================================================
// WorkflowIntent — Workflow Editor 用户意图
// =============================================================
/**
 * Workflow Editor User Intents / 工作流编排器用户意图
 */
sealed interface WorkflowIntent {
    /** Add a node to workflow / 添加节点到工作流
     * @param skillId Skill ID to add / 要添加的 Skill ID
     */
    data class AddNode(val skillId: String) : WorkflowIntent

    /** Remove a node from workflow / 从工作流移除节点
     * @param nodeId Node ID to remove / 要移除的节点 ID
     */
    data class RemoveNode(val nodeId: String) : WorkflowIntent

    /** Select a node / 选中节点
     * @param nodeId Node ID to select / 要选中的节点 ID
     */
    data class SelectNode(val nodeId: String?) : WorkflowIntent

    /** Move a node / 移动节点
     * @param nodeId Node ID to move / 要移动的节点 ID
     * @param deltaX X axis delta / X 轴增量
     * @param deltaY Y axis delta / Y 轴增量
     */
    data class MoveNode(val nodeId: String, val deltaX: Float, val deltaY: Float) : WorkflowIntent

    /** Connect two nodes / 连接两个节点
     * @param fromNodeId Source node ID / 源节点 ID
     * @param toNodeId Target node ID / 目标节点 ID
     * @param condition Optional condition / 可选条件
     */
    data class ConnectNodes(val fromNodeId: String, val toNodeId: String, val condition: String = "") : WorkflowIntent

    /** Disconnect two nodes / 断开两个节点的连接
     * @param edgeId Edge ID to remove / 要移除的边 ID
     */
    data class DisconnectNodes(val edgeId: String) : WorkflowIntent

    /** Set node as entry point / 设置节点为入口点
     * @param nodeId Node ID to set as entry / 要设置为入口的节点 ID
     */
    data class SetEntryPoint(val nodeId: String) : WorkflowIntent

    /** Update workflow metadata / 更新工作流元数据
     * @param name Workflow name / 工作流名称
     * @param description Workflow description / 工作流描述
     */
    data class UpdateWorkflowMeta(val name: String, val description: String) : WorkflowIntent

    /** Execute workflow / 执行工作流
     * Triggers workflow execution from entry point.
     */
    data object ExecuteWorkflow : WorkflowIntent

    /** Save workflow / 保存工作流
     * Serializes workflow to JSON and saves to file.
     */
    data object SaveWorkflow : WorkflowIntent

    /** Load workflow / 加载工作流
     * @param workflowId Workflow ID to load / 要加载的工作流 ID
     */
    data class LoadWorkflow(val workflowId: String) : WorkflowIntent

    /** Clear all nodes and edges / 清除所有节点和边
     */
    data object ClearAll : WorkflowIntent

    /** Dismiss error / 关闭错误信息 */
    data object DismissError : WorkflowIntent
}

// =============================================================
// WorkflowEffect — Workflow Editor 副作用
// =============================================================
/**
 * Workflow Editor Side Effects / 工作流编排器副作用
 */
sealed interface WorkflowEffect {
    /** Show execution result / 显示执行结果
     * @param log Execution log lines / 执行日志行
     * @param isSuccess Whether execution was successful / 执行是否成功
     */
    data class ShowExecutionResult(val log: List<String>, val isSuccess: Boolean) : WorkflowEffect

    /** Show save success / 显示保存成功
     * @param path Saved file path / 保存的文件路径
     */
    data class ShowSaveSuccess(val path: String) : WorkflowEffect

    /** Show snackbar / 显示 Snackbar
     * @param message Message to display / 要显示的消息
     * @param isError Whether this is an error / 是否为错误
     */
    data class ShowSnackbar(val message: String, val isError: Boolean = false) : WorkflowEffect
}

// =============================================================
// WizardState — Setup Wizard 状态
// =============================================================
/**
 * Setup Wizard State / 安装向导状态
 *
 * @param currentStep Current wizard step index / 当前向导步骤索引
 * @param steps List of wizard steps / 向导步骤列表
 * @param androidCliInstalled Whether Android CLI is installed / Android CLI 是否已安装
 * @param cliVersion Installed CLI version / 已安装的 CLI 版本
 * @param installedSkillsCount Number of installed skills / 已安装 Skill 数量
 * @param firstSkillCreated Whether first skill has been created / 第一个 Skill 是否已创建
 * @param isInstallingCli Whether CLI installation is in progress / CLI 安装是否正在进行
 * @param error Error message if any / 错误信息
 */
data class WizardState(
    val currentStep: Int = 0,
    val steps: List<WizardStep> = listOf(
        WizardStep(1, "Install Android CLI", "Download and configure Android CLI", false, false),
        WizardStep(2, "Configure Environment", "Set up environment variables and paths", false, true),
        WizardStep(3, "Install First Skill", "Install your first Android Skill", false, false),
        WizardStep(4, "Create First Skill", "Create your own custom skill", false, true),
        WizardStep(5, "Run Verification", "Verify installation and configuration", false, false)
    ),
    val androidCliInstalled: Boolean = false,
    val cliVersion: String = "",
    val installedSkillsCount: Int = 0,
    val firstSkillCreated: Boolean = false,
    val isInstallingCli: Boolean = false,
    val error: String? = null
) {
    companion object {
        /** Initial state / 初始状态 */
        val Initial = WizardState()
    }

    /**
     * Total number of required steps / 所需步骤总数
     */
    val requiredStepsCount: Int
        get() = steps.count { !it.isOptional }

    /**
     * Number of completed steps / 已完成步骤数
     */
    val completedStepsCount: Int
        get() = steps.count { it.isCompleted }

    /**
     * Progress percentage / 进度百分比
     */
    val progressPercent: Float
        get() = if (steps.isNotEmpty()) completedStepsCount.toFloat() / steps.size else 0f

    /**
     * Whether wizard is complete / 向导是否完成
     */
    val isComplete: Boolean
        get() = steps.all { it.isCompleted || it.isOptional }
}

// =============================================================
// WizardIntent — Setup Wizard 用户意图
// =============================================================
/**
 * Setup Wizard User Intents / 安装向导用户意图
 */
sealed interface WizardIntent {
    /** Go to next step / 前往下一步
     */
    data object NextStep : WizardIntent

    /** Go to previous step / 返回上一步
     */
    data object PreviousStep : WizardIntent

    /** Go to specific step / 前往指定步骤
     * @param stepIndex Step index to go to / 要前往的步骤索引
     */
    data class GoToStep(val stepIndex: Int) : WizardIntent

    /** Mark current step as complete / 将当前步骤标记为完成
     */
    data object CompleteCurrentStep : WizardIntent

    /** Install Android CLI / 安装 Android CLI
     */
    data object InstallAndroidCli : WizardIntent

    /** Check CLI status / 检查 CLI 状态
     */
    data object CheckCliStatus : WizardIntent

    /** Install first skill / 安装第一个 Skill
     * @param skillId Skill ID to install / 要安装的 Skill ID
     */
    data class InstallFirstSkill(val skillId: String) : WizardIntent

    /** Create first skill / 创建第一个 Skill
     * Opens Skill Creator in a new context.
     */
    data object CreateFirstSkill : WizardIntent

    /** Run verification / 运行验证
     * Verifies installation and configuration are correct.
     */
    data object RunVerification : WizardIntent

    /** Reset wizard / 重置向导
     * Clears all progress and restarts from step 1.
     */
    data object ResetWizard : WizardIntent

    /** Dismiss error / 关闭错误信息 */
    data object DismissError : WizardIntent
}

// =============================================================
// WizardEffect — Setup Wizard 副作用
// =============================================================
/**
 * Setup Wizard Side Effects / 安装向导副作用
 */
sealed interface WizardEffect {
    /** Show CLI installation started / 显示 CLI 安装已开始
     */
    data object ShowCliInstallationStarted : WizardEffect

    /** Show verification result / 显示验证结果
     * @param isSuccess Whether verification passed / 验证是否通过
     * @param message Result message / 结果消息
     */
    data class ShowVerificationResult(val isSuccess: Boolean, val message: String) : WizardEffect

    /** Navigate to skill creator / 导航到 Skill 创建器
     */
    data object NavigateToSkillCreator : WizardEffect

    /** Show snackbar / 显示 Snackbar
     * @param message Message to display / 要显示的消息
     * @param isError Whether this is an error / 是否为错误
     */
    data class ShowSnackbar(val message: String, val isError: Boolean = false) : WizardEffect

    /** Wizard completed / 向导完成
     */
    data object WizardCompleted : WizardEffect
}

// =============================================================
// SkillsWorkflowState — 主状态（整合所有子功能）
// =============================================================
/**
 * Skills Workflow Toolkit State / Skills Workflow 工具包主状态
 *
 * @param activeTab Currently active tab / 当前活跃的 Tab
 * @param skillHubState Skill Hub sub-feature state / 技能中心子功能状态
 * @param skillDetailState Skill Detail sub-feature state / 技能详情子功能状态
 * @param workflowState Workflow Editor sub-feature state / 工作流编排器子功能状态
 * @param wizardState Setup Wizard sub-feature state / 安装向导子功能状态
 * @param isLoading Whether any background operation is in progress / 是否有后台操作进行中
 * @param error Error message if any / 错误信息
 */
data class SkillsWorkflowState(
    val activeTab: SkillsWorkflowTab = SkillsWorkflowTab.SKILL_HUB,
    val skillHubState: SkillHubState = SkillHubState.Initial,
    val skillDetailState: SkillDetailState = SkillDetailState.Initial,
    val workflowState: WorkflowState = WorkflowState.Initial,
    val wizardState: WizardState = WizardState.Initial,
    val isLoading: Boolean = false,
    val error: String? = null
) {
    companion object {
        /** Initial state / 初始状态 */
        val Initial = SkillsWorkflowState()
    }
}

// =============================================================
// SkillsWorkflowIntent — 主用户意图
// =============================================================
/**
 * Skills Workflow Toolkit User Intents / Skills Workflow 工具包用户意图
 */
sealed interface SkillsWorkflowIntent {
    /** Select a tab / 选择 Tab
     * @param tab Tab to select / 要选择的 Tab
     */
    data class SelectTab(val tab: SkillsWorkflowTab) : SkillsWorkflowIntent

    /** Forward intent to Skill Hub / 转发意图到技能中心
     * @param intent Intent to forward / 要转发的意图
     */
    data class ForwardToSkillHub(val intent: SkillHubIntent) : SkillsWorkflowIntent

    /** Forward intent to Skill Detail / 转发意图到技能详情
     * @param intent Intent to forward / 要转发的意图
     */
    data class ForwardToSkillDetail(val intent: SkillDetailIntent) : SkillsWorkflowIntent

    /** Forward intent to Workflow Editor / 转发意图到工作流编排器
     * @param intent Intent to forward / 要转发的意图
     */
    data class ForwardToWorkflow(val intent: WorkflowIntent) : SkillsWorkflowIntent

    /** Forward intent to Setup Wizard / 转发意图到安装向导
     * @param intent Intent to forward / 要转发的意图
     */
    data class ForwardToWizard(val intent: WizardIntent) : SkillsWorkflowIntent

    /** Dismiss error / 关闭错误信息 */
    data object DismissError : SkillsWorkflowIntent
}

// =============================================================
// SkillsWorkflowEffect — 主副作用
// =============================================================
/**
 * Skills Workflow Toolkit Side Effects / Skills Workflow 工具包副作用
 */
sealed interface SkillsWorkflowEffect {
    /** Show snackbar / 显示 Snackbar
     * @param message Message to display / 要显示的消息
     * @param isError Whether this is an error / 是否为错误
     */
    data class ShowSnackbar(val message: String, val isError: Boolean = false) : SkillsWorkflowEffect

    /** Show verification result / 显示验证结果
     * @param isSuccess Whether verification passed / 验证是否通过
     * @param message Result message / 结果消息
     */
    data class ShowVerificationResult(val isSuccess: Boolean, val message: String) : SkillsWorkflowEffect
}
