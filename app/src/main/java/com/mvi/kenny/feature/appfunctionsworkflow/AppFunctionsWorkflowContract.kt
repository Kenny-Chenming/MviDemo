package com.mvi.kenny.feature.appfunctionsworkflow

/**
 * ============================================================
 * AppFunctionsWorkflowContract — AppFunctions AI 工作流中间件 MVI 契约
 * AppFunctions AI Workflow Middleware MVI Contract
 * ============================================================
 *
 * PRD-297 | Android AppFunctions AI 工作流中间件
 * Ref: memory/agency/designs/PRD-297-AppFunctions-AI-Workflow-Middleware.md
 *
 * MVI Architecture:
 * - Model (State): Immutable page state, single source of truth
 * - View: Composable functions that consume State and render UI
 * - Intent: User intentions; ViewModel executes logic on receiving Intent
 * - Effect: One-time side effects (Toast, Navigation) delivered via Channel
 * —————————————————————————————————————————————————————
 *
 * Core Features:
 * - Tab 1: AppFunctions 企业适配层 (Enterprise Adaptation Layer)
 * - Tab 2: 多 App 编排引擎 (Multi-App Orchestration Engine - DAG)
 * - Tab 3: 权限与审计 (Permissions & Audit)
 * - Tab 4: 飞书集成 (Feishu Integration)
 */

// ============================================================
// Data Models / 数据模型
// ============================================================

/**
 * Bottom navigation tab enumeration
 * 底部导航 Tab 枚举
 *
 * @param title Tab display title / Tab 显示标题
 * @param iconName Material icon name / 图标名称
 */
enum class AppFunctionsWorkflowTab(val title: String, val iconName: String) {
    ADAPTER_LAYER("适配层", "hub"),
    ORCHESTRATION("编排引擎", "account_tree"),
    PERMISSION_AUDIT("权限审计", "security"),
    FEISHU_INTEGRATION("飞书集成", "notifications")
}

/**
 * Workflow DAG node status / 工作流 DAG 节点状态
 *
 * @param id Node unique identifier / 节点唯一标识
 * @param name Node display name / 节点显示名称
 * @param appName Associated app name / 关联应用名称
 * @param functionName AppFunction name / AppFunction 名称
 * @param status Execution status / 执行状态
 * @param inputParams Input parameters / 输入参数
 * @param outputResult Output result / 输出结果
 * @param executionTimeMs Execution time in milliseconds / 执行时间（毫秒）
 * @param errorMessage Error message if failed / 错误信息（如果失败）
 */
data class WorkflowNode(
    val id: String,
    val name: String,
    val appName: String,
    val functionName: String,
    val status: NodeStatus = NodeStatus.PENDING,
    val inputParams: Map<String, String> = emptyMap(),
    val outputResult: String? = null,
    val executionTimeMs: Long? = null,
    val errorMessage: String? = null
)

/**
 * Workflow node execution status / 工作流节点执行状态
 *
 * @param PENDING Not yet executed / 未执行
 * @param RUNNING Currently executing / 执行中
 * @param SUCCESS Successfully completed / 成功完成
 * @param FAILED Execution failed / 执行失败
 * @param SKIPPED Skipped (dependency failed) / 跳过（依赖失败）
 */
enum class NodeStatus { PENDING, RUNNING, SUCCESS, FAILED, SKIPPED }

/**
 * Workflow DAG edge (connection between nodes) / 工作流 DAG 边（节点间连接）
 *
 * @param fromNodeId Source node ID / 源节点 ID
 * @param toNodeId Target node ID / 目标节点 ID
 * @param label Edge label / 边标签
 */
data class WorkflowEdge(
    val fromNodeId: String,
    val toNodeId: String,
    val label: String = ""
)

/**
 * Workflow definition / 工作流定义
 *
 * @param id Workflow unique identifier / 工作流唯一标识
 * @param name Workflow display name / 工作流显示名称
 * @param description Workflow description / 工作流描述
 * @param nodes List of workflow nodes / 工作流节点列表
 * @param edges List of workflow edges / 工作流边列表
 * @param createdAt Creation timestamp / 创建时间戳
 * @param lastExecutedAt Last execution timestamp / 最后执行时间戳
 */
data class WorkflowDefinition(
    val id: String,
    val name: String,
    val description: String,
    val nodes: List<WorkflowNode> = emptyList(),
    val edges: List<WorkflowEdge> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
    val lastExecutedAt: Long? = null
)

/**
 * AppFunction registration entry / AppFunction 注册条目
 *
 * @param id Registration ID / 注册 ID
 * @param appName App name / 应用名称
 * @param functionName Function name / 功能名称
 * @param description Function description / 功能描述
 * @param parameters JSON schema for parameters / 参数 JSON Schema
 * @param returns JSON schema for return values / 返回值 JSON Schema
 * @param permissionLevel Required permission level / 所需权限级别
 * @param isRegistered Whether currently registered / 是否已注册
 */
data class AppFunctionEntry(
    val id: String,
    val appName: String,
    val functionName: String,
    val description: String,
    val parameters: String = "{}",
    val returns: String = "{}",
    val permissionLevel: PermissionLevel = PermissionLevel.NORMAL,
    val isRegistered: Boolean = false
)

/**
 * Permission level enumeration / 权限级别枚举
 *
 * @param NORMAL Normal permission, auto-approved / 普通权限，自动批准
 * @param SENSITIVE Sensitive permission, requires approval / 敏感权限，需要批准
 * @param CRITICAL Critical permission, requires explicit approval / 关键权限，需要明确批准
 */
enum class PermissionLevel { NORMAL, SENSITIVE, CRITICAL }

/**
 * Audit log entry / 审计日志条目
 *
 * @param id Log entry ID / 日志条目 ID
 * @param timestamp Execution timestamp / 执行时间戳
 * @param workflowName Workflow name / 工作流名称
 * @param nodeName Node name / 节点名称
 * @param functionName Function name / 功能名称
 * @param caller Caller identifier / 调用者标识
 * @param status Execution status / 执行状态
 * @param durationMs Execution duration in ms / 执行时长（毫秒）
 * @param inputSummary Input parameters summary / 输入参数摘要
 * @param outputSummary Output result summary / 输出结果摘要
 * @param errorDetail Error detail if failed / 错误详情（如果失败）
 */
data class AuditLogEntry(
    val id: String,
    val timestamp: Long,
    val workflowName: String,
    val nodeName: String,
    val functionName: String,
    val caller: String,
    val status: NodeStatus,
    val durationMs: Long,
    val inputSummary: String,
    val outputSummary: String,
    val errorDetail: String? = null
)

/**
 * Feishu notification config / 飞书通知配置
 *
 * @param webhookUrl Feishu webhook URL / 飞书 Webhook URL
 * @param enabled Whether notifications are enabled / 是否启用通知
 * @param notifyOnSuccess Notify on workflow success / 工作流成功时通知
 * @param notifyOnFailure Notify on workflow failure / 工作流失败时通知
 * @param mentionUsers List of user IDs to mention / 需要 @ 的用户 ID 列表
 */
data class FeishuConfig(
    val webhookUrl: String = "",
    val enabled: Boolean = false,
    val notifyOnSuccess: Boolean = true,
    val notifyOnFailure: Boolean = true,
    val mentionUsers: List<String> = emptyList()
)

// ============================================================
// State / 页面状态
// ============================================================

/**
 * AppFunctions AI Workflow Middleware page state
 * AppFunctions AI 工作流中间件页面状态
 *
 * MVI Architecture: Model layer, holds all page state.
 * State is Immutable — each state change creates a new State object.
 *
 * @param selectedTab Current bottom navigation tab index / 当前选中的 Tab
 * @param workflows List of workflow definitions / 工作流定义列表
 * @param selectedWorkflow Currently selected workflow / 当前选中的工作流
 * @param registeredFunctions List of registered AppFunctions / 已注册的 AppFunctions 列表
 * @param auditLogs List of audit log entries / 审计日志列表
 * @param feishuConfig Feishu notification configuration / 飞书通知配置
 * @param isExecuting Whether workflow is currently executing / 工作流是否正在执行
 * @param executionProgress Current execution progress (0-100) / 执行进度 (0-100)
 * @param expandedWorkflows Set of expanded workflow IDs / 已展开的工作流 ID 集合
 * @param expandedAuditLogs Set of expanded audit log IDs / 已展开的审计日志 ID 集合
 * @param error Error message, null means no error / 错误信息，null 表示无错误
 *
 * @see AppFunctionsWorkflowIntent
 * @see AppFunctionsWorkflowViewModel
 */
data class AppFunctionsWorkflowState(
    val selectedTab: Int = 0,
    // Workflow definitions / 工作流定义
    val workflows: List<WorkflowDefinition> = emptyList(),
    val selectedWorkflow: WorkflowDefinition? = null,
    // AppFunctions registry / AppFunctions 注册表
    val registeredFunctions: List<AppFunctionEntry> = emptyList(),
    // Audit logs / 审计日志
    val auditLogs: List<AuditLogEntry> = emptyList(),
    // Feishu config / 飞书配置
    val feishuConfig: FeishuConfig = FeishuConfig(),
    // Execution state / 执行状态
    val isExecuting: Boolean = false,
    val executionProgress: Int = 0,
    // Expansion state / 展开状态
    val expandedWorkflows: Set<String> = emptySet(),
    val expandedAuditLogs: Set<String> = emptySet(),
    // Error state / 错误状态
    val error: String? = null
) {
    companion object {
        /** Initial / default state / 初始状态 */
        val Initial = AppFunctionsWorkflowState()
    }
}

// ============================================================
// Intent / 用户意图
// ============================================================

/**
 * User intentions for AppFunctions AI Workflow Middleware
 * AppFunctions AI 工作流中间件用户意图
 *
 * Every user interaction on the page corresponds to an Intent.
 * ViewModel receives Intent, executes business logic, then updates State.
 *
 * @see AppFunctionsWorkflowViewModel.sendIntent
 */
sealed interface AppFunctionsWorkflowIntent {

    /**
     * Switch bottom navigation tab / 切换底部 Tab
     *
     * @param index Target tab index / 目标 Tab 索引
     */
    data class SelectTab(val index: Int) : AppFunctionsWorkflowIntent

    /**
     * Create a new workflow / 创建新工作流
     *
     * @param name Workflow name / 工作流名称
     * @param description Workflow description / 工作流描述
     */
    data class CreateWorkflow(val name: String, val description: String) : AppFunctionsWorkflowIntent

    /**
     * Select a workflow for editing/execution / 选择工作流进行编辑/执行
     *
     * @param workflowId Workflow ID / 工作流 ID
     */
    data class SelectWorkflow(val workflowId: String) : AppFunctionsWorkflowIntent

    /**
     * Delete a workflow / 删除工作流
     *
     * @param workflowId Workflow ID / 工作流 ID
     */
    data class DeleteWorkflow(val workflowId: String) : AppFunctionsWorkflowIntent

    /**
     * Add a node to workflow / 向工作流添加节点
     *
     * @param workflowId Workflow ID / 工作流 ID
     * @param node Node to add / 要添加的节点
     */
    data class AddWorkflowNode(val workflowId: String, val node: WorkflowNode) : AppFunctionsWorkflowIntent

    /**
     * Remove a node from workflow / 从工作流移除节点
     *
     * @param workflowId Workflow ID / 工作流 ID
     * @param nodeId Node ID / 节点 ID
     */
    data class RemoveWorkflowNode(val workflowId: String, val nodeId: String) : AppFunctionsWorkflowIntent

    /**
     * Add edge between nodes / 在节点间添加边
     *
     * @param workflowId Workflow ID / 工作流 ID
     * @param edge Edge to add / 要添加的边
     */
    data class AddWorkflowEdge(val workflowId: String, val edge: WorkflowEdge) : AppFunctionsWorkflowIntent

    /**
     * Remove edge from workflow / 从工作流移除边
     *
     * @param workflowId Workflow ID / 工作流 ID
     * @param fromNodeId Source node ID / 源节点 ID
     * @param toNodeId Target node ID / 目标节点 ID
     */
    data class RemoveWorkflowEdge(val workflowId: String, val fromNodeId: String, val toNodeId: String) : AppFunctionsWorkflowIntent

    /**
     * Execute workflow / 执行工作流
     *
     * @param workflowId Workflow ID / 工作流 ID
     */
    data class ExecuteWorkflow(val workflowId: String) : AppFunctionsWorkflowIntent

    /**
     * Register an AppFunction / 注册 AppFunction
     *
     * @param entry AppFunction entry to register / 要注册的 AppFunction 条目
     */
    data class RegisterAppFunction(val entry: AppFunctionEntry) : AppFunctionsWorkflowIntent

    /**
     * Unregister an AppFunction / 取消注册 AppFunction
     *
     * @param functionId Function ID / 功能 ID
     */
    data class UnregisterAppFunction(val functionId: String) : AppFunctionsWorkflowIntent

    /**
     * Update Feishu configuration / 更新飞书配置
     *
     * @param config New Feishu configuration / 新的飞书配置
     */
    data class UpdateFeishuConfig(val config: FeishuConfig) : AppFunctionsWorkflowIntent

    /**
     * Toggle workflow expansion state / 切换工作流展开/收起状态
     *
     * @param workflowId Workflow ID / 工作流 ID
     */
    data class ToggleWorkflowExpansion(val workflowId: String) : AppFunctionsWorkflowIntent

    /**
     * Toggle audit log expansion state / 切换审计日志展开/收起状态
     *
     * @param logId Log entry ID / 日志条目 ID
     */
    data class ToggleAuditLogExpansion(val logId: String) : AppFunctionsWorkflowIntent

    /**
     * Export audit log / 导出审计日志
     *
     * @param format Export format (json/csv) / 导出格式 (json/csv)
     */
    data class ExportAuditLog(val format: String) : AppFunctionsWorkflowIntent

    /**
     * Copy text to clipboard / 复制文本到剪贴板
     *
     * @param text Text to copy / 要复制的文本
     */
    data class CopyToClipboard(val text: String) : AppFunctionsWorkflowIntent

    /**
     * Send test Feishu notification / 发送测试飞书通知
     *
     * @param mentionedUsers List of user IDs to mention / 需要 @ 的用户 ID 列表
     */
    data class SendTestFeishuNotification(val mentionedUsers: List<String>) : AppFunctionsWorkflowIntent
}

// ============================================================
// Effect / 副作用
// ============================================================

/**
 * One-time side effects for AppFunctions AI Workflow Middleware
 * AppFunctions AI 工作流中间件副作用
 *
 * One-time events, immutable, consumed only once.
 * UI layer listens via LaunchedEffect + flow.collect {}
 *
 * @see AppFunctionsWorkflowViewModel
 */
sealed interface AppFunctionsWorkflowEffect {

    /**
     * Show toast message / 显示 Toast
     *
     * @param message Toast message text / Toast 文本
     */
    data class ShowToast(val message: String) : AppFunctionsWorkflowEffect

    /**
     * Copy text to system clipboard / 复制文本到剪贴板
     *
     * @param text Text to copy / 要复制的文本
     */
    data class CopyToClipboard(val text: String) : AppFunctionsWorkflowEffect

    /**
     * Send Feishu notification / 发送飞书通知
     *
     * @param title Notification title / 通知标题
     * @param content Notification content / 通知内容
     * @param mentionedUsers List of user IDs to mention / 需要 @ 的用户 ID 列表
     */
    data class SendFeishuNotification(
        val title: String,
        val content: String,
        val mentionedUsers: List<String> = emptyList()
    ) : AppFunctionsWorkflowEffect

    /**
     * Export audit log file / 导出审计日志文件
     *
     * @param content File content / 文件内容
     * @param filename File name / 文件名
     */
    data class ExportAuditLogFile(val content: String, val filename: String) : AppFunctionsWorkflowEffect
}
