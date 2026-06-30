package com.mvi.kenny.feature.appfunctionsworkflow

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

/**
 * ============================================================
 * AppFunctionsWorkflowViewModel — AppFunctions AI 工作流中间件 ViewModel
 * ============================================================
 *
 * MVI Architecture: ViewModel handles Intent → executes business logic → updates State.
 *
 * State management:
 * - _state: MutableStateFlow holding current UI state
 * - state: Public immutable StateFlow exposed to UI layer
 *
 * Effect management:
 * - _effect: Channel for one-time side effects (Toast, Clipboard, Feishu notifications)
 * - effect: Public receiveAsFlow for UI to collect
 *
 * @see AppFunctionsWorkflowState
 * @see AppFunctionsWorkflowIntent
 * @see AppFunctionsWorkflowEffect
 */
class AppFunctionsWorkflowViewModel : ViewModel() {

    // ============================================================
    // State — UI 状态
    // ============================================================

    private val _state = MutableStateFlow(AppFunctionsWorkflowState.Initial)
    val state: StateFlow<AppFunctionsWorkflowState> = _state.asStateFlow()

    // ============================================================
    // Effect — 副作用通道
    // ============================================================

    private val _effect = Channel<AppFunctionsWorkflowEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    // ============================================================
    // Intent Processing — 意图处理
    // ============================================================

    /**
     * Process user intention
     * 处理用户意图
     *
     * Entry point for all user interactions. Called from UI layer via:
     *   viewModel.sendIntent(AppFunctionsWorkflowIntent.xxx)
     *
     * @param intent User intention / 用户意图
     */
    fun sendIntent(intent: AppFunctionsWorkflowIntent) {
        when (intent) {
            is AppFunctionsWorkflowIntent.SelectTab -> handleSelectTab(intent.index)
            is AppFunctionsWorkflowIntent.CreateWorkflow -> handleCreateWorkflow(intent.name, intent.description)
            is AppFunctionsWorkflowIntent.SelectWorkflow -> handleSelectWorkflow(intent.workflowId)
            is AppFunctionsWorkflowIntent.DeleteWorkflow -> handleDeleteWorkflow(intent.workflowId)
            is AppFunctionsWorkflowIntent.AddWorkflowNode -> handleAddWorkflowNode(intent.workflowId, intent.node)
            is AppFunctionsWorkflowIntent.RemoveWorkflowNode -> handleRemoveWorkflowNode(intent.workflowId, intent.nodeId)
            is AppFunctionsWorkflowIntent.AddWorkflowEdge -> handleAddWorkflowEdge(intent.workflowId, intent.edge)
            is AppFunctionsWorkflowIntent.RemoveWorkflowEdge -> handleRemoveWorkflowEdge(intent.workflowId, intent.fromNodeId, intent.toNodeId)
            is AppFunctionsWorkflowIntent.ExecuteWorkflow -> handleExecuteWorkflow(intent.workflowId)
            is AppFunctionsWorkflowIntent.RegisterAppFunction -> handleRegisterAppFunction(intent.entry)
            is AppFunctionsWorkflowIntent.UnregisterAppFunction -> handleUnregisterAppFunction(intent.functionId)
            is AppFunctionsWorkflowIntent.UpdateFeishuConfig -> handleUpdateFeishuConfig(intent.config)
            is AppFunctionsWorkflowIntent.ToggleWorkflowExpansion -> handleToggleWorkflowExpansion(intent.workflowId)
            is AppFunctionsWorkflowIntent.ToggleAuditLogExpansion -> handleToggleAuditLogExpansion(intent.logId)
            is AppFunctionsWorkflowIntent.ExportAuditLog -> handleExportAuditLog(intent.format)
            is AppFunctionsWorkflowIntent.CopyToClipboard -> handleCopyToClipboard(intent.text)
            is AppFunctionsWorkflowIntent.SendTestFeishuNotification -> handleSendTestFeishuNotification(intent.mentionedUsers)
        }
    }

    // ============================================================
    // Tab Navigation / Tab 切换
    // ============================================================

    /**
     * Handle tab selection
     * 处理 Tab 切换
     *
     * @param index Selected tab index / 选中的 Tab 索引
     */
    private fun handleSelectTab(index: Int) {
        _state.update { it.copy(selectedTab = index) }
    }

    // ============================================================
    // Workflow Management / 工作流管理
    // ============================================================

    /**
     * Handle workflow creation
     * 处理工作流创建
     *
     * @param name Workflow name / 工作流名称
     * @param description Workflow description / 工作流描述
     */
    private fun handleCreateWorkflow(name: String, description: String) {
        val newWorkflow = WorkflowDefinition(
            id = UUID.randomUUID().toString(),
            name = name,
            description = description,
            createdAt = System.currentTimeMillis()
        )
        _state.update { currentState ->
            currentState.copy(
                workflows = currentState.workflows + newWorkflow,
                selectedWorkflow = newWorkflow
            )
        }
        viewModelScope.launch {
            _effect.send(AppFunctionsWorkflowEffect.ShowToast("工作流已创建 / Workflow created"))
        }
    }

    /**
     * Handle workflow selection
     * 处理工作流选择
     *
     * @param workflowId Workflow ID / 工作流 ID
     */
    private fun handleSelectWorkflow(workflowId: String) {
        _state.update { currentState ->
            currentState.copy(
                selectedWorkflow = currentState.workflows.find { it.id == workflowId }
            )
        }
    }

    /**
     * Handle workflow deletion
     * 处理工作流删除
     *
     * @param workflowId Workflow ID / 工作流 ID
     */
    private fun handleDeleteWorkflow(workflowId: String) {
        _state.update { currentState ->
            val updatedWorkflows = currentState.workflows.filter { it.id != workflowId }
            currentState.copy(
                workflows = updatedWorkflows,
                selectedWorkflow = if (currentState.selectedWorkflow?.id == workflowId) null else currentState.selectedWorkflow
            )
        }
        viewModelScope.launch {
            _effect.send(AppFunctionsWorkflowEffect.ShowToast("工作流已删除 / Workflow deleted"))
        }
    }

    /**
     * Handle adding node to workflow
     * 处理向工作流添加节点
     *
     * @param workflowId Workflow ID / 工作流 ID
     * @param node Node to add / 要添加的节点
     */
    private fun handleAddWorkflowNode(workflowId: String, node: WorkflowNode) {
        _state.update { currentState ->
            currentState.copy(
                workflows = currentState.workflows.map { workflow ->
                    if (workflow.id == workflowId) {
                        workflow.copy(nodes = workflow.nodes + node)
                    } else workflow
                },
                selectedWorkflow = currentState.selectedWorkflow?.let { selected ->
                    if (selected.id == workflowId) {
                        selected.copy(nodes = selected.nodes + node)
                    } else selected
                }
            )
        }
        viewModelScope.launch {
            _effect.send(AppFunctionsWorkflowEffect.ShowToast("节点已添加 / Node added"))
        }
    }

    /**
     * Handle removing node from workflow
     * 处理从工作流移除节点
     *
     * @param workflowId Workflow ID / 工作流 ID
     * @param nodeId Node ID / 节点 ID
     */
    private fun handleRemoveWorkflowNode(workflowId: String, nodeId: String) {
        _state.update { currentState ->
            val updateWorkflow: (WorkflowDefinition) -> WorkflowDefinition = { workflow ->
                if (workflow.id == workflowId) {
                    workflow.copy(
                        nodes = workflow.nodes.filter { it.id != nodeId },
                        edges = workflow.edges.filter { it.fromNodeId != nodeId && it.toNodeId != nodeId }
                    )
                } else workflow
            }
            currentState.copy(
                workflows = currentState.workflows.map(updateWorkflow),
                selectedWorkflow = currentState.selectedWorkflow?.let(updateWorkflow)
            )
        }
        viewModelScope.launch {
            _effect.send(AppFunctionsWorkflowEffect.ShowToast("节点已移除 / Node removed"))
        }
    }

    /**
     * Handle adding edge to workflow
     * 处理向工作流添加边
     *
     * @param workflowId Workflow ID / 工作流 ID
     * @param edge Edge to add / 要添加的边
     */
    private fun handleAddWorkflowEdge(workflowId: String, edge: WorkflowEdge) {
        _state.update { currentState ->
            val updateWorkflow: (WorkflowDefinition) -> WorkflowDefinition = { workflow ->
                if (workflow.id == workflowId) {
                    // Check for duplicate edges / 检查重复边
                    val exists = workflow.edges.any { it.fromNodeId == edge.fromNodeId && it.toNodeId == edge.toNodeId }
                    if (exists) workflow else workflow.copy(edges = workflow.edges + edge)
                } else workflow
            }
            currentState.copy(
                workflows = currentState.workflows.map(updateWorkflow),
                selectedWorkflow = currentState.selectedWorkflow?.let(updateWorkflow)
            )
        }
        viewModelScope.launch {
            _effect.send(AppFunctionsWorkflowEffect.ShowToast("连接已添加 / Edge added"))
        }
    }

    /**
     * Handle removing edge from workflow
     * 处理从工作流移除边
     *
     * @param workflowId Workflow ID / 工作流 ID
     * @param fromNodeId Source node ID / 源节点 ID
     * @param toNodeId Target node ID / 目标节点 ID
     */
    private fun handleRemoveWorkflowEdge(workflowId: String, fromNodeId: String, toNodeId: String) {
        _state.update { currentState ->
            val updateWorkflow: (WorkflowDefinition) -> WorkflowDefinition = { workflow ->
                if (workflow.id == workflowId) {
                    workflow.copy(edges = workflow.edges.filter { !(it.fromNodeId == fromNodeId && it.toNodeId == toNodeId) })
                } else workflow
            }
            currentState.copy(
                workflows = currentState.workflows.map(updateWorkflow),
                selectedWorkflow = currentState.selectedWorkflow?.let(updateWorkflow)
            )
        }
        viewModelScope.launch {
            _effect.send(AppFunctionsWorkflowEffect.ShowToast("连接已移除 / Edge removed"))
        }
    }

    // ============================================================
    // Workflow Execution / 工作流执行
    // ============================================================

    /**
     * Handle workflow execution
     * 处理工作流执行
     *
     * Implements DAG-based topological execution order.
     * 实现基于 DAG 的拓扑执行顺序。
     *
     * @param workflowId Workflow ID / 工作流 ID
     */
    private fun handleExecuteWorkflow(workflowId: String) {
        val workflow = _state.value.workflows.find { it.id == workflowId } ?: return

        viewModelScope.launch {
            _state.update { it.copy(isExecuting = true, executionProgress = 0) }

            // Build adjacency list for topological sort / 构建邻接表用于拓扑排序
            val adjacencyList = mutableMapOf<String, MutableList<String>>()
            val inDegree = mutableMapOf<String, Int>()

            workflow.nodes.forEach { node ->
                adjacencyList[node.id] = mutableListOf()
                inDegree[node.id] = 0
            }
            workflow.edges.forEach { edge ->
                adjacencyList[edge.fromNodeId]?.add(edge.toNodeId)
                inDegree[edge.toNodeId] = (inDegree[edge.toNodeId] ?: 0) + 1
            }

            // Kahn's algorithm for topological sort / Kahn 拓扑排序算法
            val queue = ArrayDeque<String>()
            inDegree.filter { it.value == 0 }.keys.forEach { queue.add(it) }

            val executionOrder = mutableListOf<String>()
            while (queue.isNotEmpty()) {
                val current = queue.removeFirst()
                executionOrder.add(current)
                adjacencyList[current]?.forEach { neighbor ->
                    val newDegree = (inDegree[neighbor] ?: 1) - 1
                    inDegree[neighbor] = newDegree
                    if (newDegree == 0) queue.add(neighbor)
                }
            }

            // Check for cycles / 检查循环
            if (executionOrder.size != workflow.nodes.size) {
                _state.update { it.copy(isExecuting = false, error = "工作流存在循环依赖 / Workflow has circular dependency") }
                _effect.send(AppFunctionsWorkflowEffect.ShowToast("执行失败：循环依赖 / Execution failed: Circular dependency"))
                return@launch
            }

            // Execute nodes in topological order / 按拓扑顺序执行节点
            val totalNodes = executionOrder.size
            var completedNodes = 0

            executionOrder.forEach { nodeId ->
                val node = workflow.nodes.find { it.id == nodeId } ?: return@forEach

                // Update node status to RUNNING / 更新节点状态为执行中
                updateNodeStatus(workflowId, nodeId, NodeStatus.RUNNING)

                // Simulate execution / 模拟执行
                delay(500)

                // Simulate success/failure (90% success rate for demo) / 模拟成功/失败（演示用 90% 成功率）
                val isSuccess = kotlin.random.Random.nextFloat() > 0.1f

                if (isSuccess) {
                    updateNodeStatus(workflowId, nodeId, NodeStatus.SUCCESS, outputResult = "{\"status\":\"success\"}")
                    addAuditLogEntry(workflow, node, NodeStatus.SUCCESS)
                } else {
                    updateNodeStatus(workflowId, nodeId, NodeStatus.FAILED, errorMessage = "Execution failed / 执行失败")
                    addAuditLogEntry(workflow, node, NodeStatus.FAILED)
                    // Skip dependent nodes / 跳过依赖节点
                    skipDependentNodes(workflowId, nodeId)
                }

                completedNodes++
                _state.update { it.copy(executionProgress = (completedNodes * 100) / totalNodes) }
            }

            _state.update { currentState ->
                currentState.copy(
                    isExecuting = false,
                    executionProgress = 100,
                    workflows = currentState.workflows.map { wf ->
                        if (wf.id == workflowId) wf.copy(lastExecutedAt = System.currentTimeMillis()) else wf
                    }
                )
            }

            // Send Feishu notification if configured / 如果配置了飞书通知则发送
            val feishuConfig = _state.value.feishuConfig
            if (feishuConfig.enabled) {
                val hasFailures = _state.value.workflows.find { it.id == workflowId }?.nodes?.any { it.status == NodeStatus.FAILED } == true
                if ((hasFailures && feishuConfig.notifyOnFailure) || (!hasFailures && feishuConfig.notifyOnSuccess)) {
                    _effect.send(
                        AppFunctionsWorkflowEffect.SendFeishuNotification(
                            title = if (hasFailures) "⚠️ 工作流执行失败" else "✅ 工作流执行成功",
                            content = "工作流: ${workflow.name}\n状态: ${if (hasFailures) "部分节点失败" else "全部成功"}",
                            mentionedUsers = feishuConfig.mentionUsers
                        )
                    )
                }
            }

            _effect.send(AppFunctionsWorkflowEffect.ShowToast("工作流执行完成 / Workflow execution completed"))
        }
    }

    /**
     * Update node status in workflow
     * 更新工作流中的节点状态
     *
     * @param workflowId Workflow ID / 工作流 ID
     * @param nodeId Node ID / 节点 ID
     * @param status New status / 新状态
     * @param outputResult Output result if successful / 输出结果（如果成功）
     * @param errorMessage Error message if failed / 错误信息（如果失败）
     */
    private fun updateNodeStatus(
        workflowId: String,
        nodeId: String,
        status: NodeStatus,
        outputResult: String? = null,
        errorMessage: String? = null
    ) {
        _state.update { currentState ->
            val updateWorkflow: (WorkflowDefinition) -> WorkflowDefinition = { workflow ->
                if (workflow.id == workflowId) {
                    workflow.copy(
                        nodes = workflow.nodes.map { node ->
                            if (node.id == nodeId) {
                                node.copy(
                                    status = status,
                                    outputResult = outputResult ?: node.outputResult,
                                    errorMessage = errorMessage ?: node.errorMessage,
                                    executionTimeMs = if (status == NodeStatus.SUCCESS || status == NodeStatus.FAILED) 500L else node.executionTimeMs
                                )
                            } else node
                        }
                    )
                } else workflow
            }
            currentState.copy(
                workflows = currentState.workflows.map(updateWorkflow),
                selectedWorkflow = currentState.selectedWorkflow?.let(updateWorkflow)
            )
        }
    }

    /**
     * Skip nodes that depend on failed node
     * 跳过依赖于失败节点的节点
     *
     * @param workflowId Workflow ID / 工作流 ID
     * @param failedNodeId Failed node ID / 失败节点 ID
     */
    private fun skipDependentNodes(workflowId: String, failedNodeId: String) {
        val workflow = _state.value.workflows.find { it.id == workflowId } ?: return
        val dependents = workflow.edges.filter { it.fromNodeId == failedNodeId }.map { it.toNodeId }
        dependents.forEach { dependentId ->
            updateNodeStatus(workflowId, dependentId, NodeStatus.SKIPPED)
            // Recursively skip dependents of this node / 递归跳过该节点的依赖节点
            skipDependentNodes(workflowId, dependentId)
        }
    }

    /**
     * Add audit log entry
     * 添加审计日志条目
     *
     * @param workflow Workflow definition / 工作流定义
     * @param node Executed node / 执行的节点
     * @param status Execution status / 执行状态
     */
    private fun addAuditLogEntry(workflow: WorkflowDefinition, node: WorkflowNode, status: NodeStatus) {
        val logEntry = AuditLogEntry(
            id = UUID.randomUUID().toString(),
            timestamp = System.currentTimeMillis(),
            workflowName = workflow.name,
            nodeName = node.name,
            functionName = node.functionName,
            caller = "AI Agent",
            status = status,
            durationMs = node.executionTimeMs ?: 500L,
            inputSummary = node.inputParams.entries.joinToString { "${it.key}=${it.value}" },
            outputSummary = node.outputResult ?: "N/A",
            errorDetail = node.errorMessage
        )
        _state.update { it.copy(auditLogs = listOf(logEntry) + it.auditLogs) }
    }

    // ============================================================
    // AppFunction Registration / AppFunction 注册
    // ============================================================

    /**
     * Handle AppFunction registration
     * 处理 AppFunction 注册
     *
     * @param entry AppFunction entry to register / 要注册的 AppFunction 条目
     */
    private fun handleRegisterAppFunction(entry: AppFunctionEntry) {
        val registeredEntry = entry.copy(id = UUID.randomUUID().toString(), isRegistered = true)
        _state.update { currentState ->
            currentState.copy(registeredFunctions = currentState.registeredFunctions + registeredEntry)
        }
        viewModelScope.launch {
            _effect.send(AppFunctionsWorkflowEffect.ShowToast("AppFunction 已注册 / AppFunction registered"))
        }
    }

    /**
     * Handle AppFunction unregistration
     * 处理 AppFunction 取消注册
     *
     * @param functionId Function ID / 功能 ID
     */
    private fun handleUnregisterAppFunction(functionId: String) {
        _state.update { currentState ->
            currentState.copy(
                registeredFunctions = currentState.registeredFunctions.map {
                    if (it.id == functionId) it.copy(isRegistered = false) else it
                }
            )
        }
        viewModelScope.launch {
            _effect.send(AppFunctionsWorkflowEffect.ShowToast("AppFunction 已取消注册 / AppFunction unregistered"))
        }
    }

    // ============================================================
    // Feishu Configuration / 飞书配置
    // ============================================================

    /**
     * Handle Feishu config update
     * 处理飞书配置更新
     *
     * @param config New Feishu configuration / 新的飞书配置
     */
    private fun handleUpdateFeishuConfig(config: FeishuConfig) {
        _state.update { it.copy(feishuConfig = config) }
        viewModelScope.launch {
            _effect.send(AppFunctionsWorkflowEffect.ShowToast("飞书配置已更新 / Feishu config updated"))
        }
    }

    // ============================================================
    // Expand/Collapse Handlers / 展开/收起处理
    // ============================================================

    /**
     * Toggle workflow expansion state
     * 切换工作流展开/收起状态
     *
     * @param workflowId Workflow ID / 工作流 ID
     */
    private fun handleToggleWorkflowExpansion(workflowId: String) {
        _state.update { currentState ->
            val newExpanded = if (workflowId in currentState.expandedWorkflows) {
                currentState.expandedWorkflows - workflowId
            } else {
                currentState.expandedWorkflows + workflowId
            }
            currentState.copy(expandedWorkflows = newExpanded)
        }
    }

    /**
     * Toggle audit log expansion state
     * 切换审计日志展开/收起状态
     *
     * @param logId Log entry ID / 日志条目 ID
     */
    private fun handleToggleAuditLogExpansion(logId: String) {
        _state.update { currentState ->
            val newExpanded = if (logId in currentState.expandedAuditLogs) {
                currentState.expandedAuditLogs - logId
            } else {
                currentState.expandedAuditLogs + logId
            }
            currentState.copy(expandedAuditLogs = newExpanded)
        }
    }

    // ============================================================
    // Export & Clipboard / 导出与剪贴板
    // ============================================================

    /**
     * Handle audit log export
     * 处理审计日志导出
     *
     * @param format Export format (json/csv) / 导出格式 (json/csv)
     */
    private fun handleExportAuditLog(format: String) {
        val logs = _state.value.auditLogs
        if (logs.isEmpty()) {
            viewModelScope.launch {
                _effect.send(AppFunctionsWorkflowEffect.ShowToast("没有可导出的日志 / No logs to export"))
            }
            return
        }

        val content = when (format) {
            "json" -> logs.joinToString("\n") { log ->
                """{"id":"${log.id}","timestamp":${log.timestamp},"workflow":"${log.workflowName}","node":"${log.nodeName}","status":"${log.status}","duration":${log.durationMs}}"""
            }
            "csv" -> "ID,Timestamp,Workflow,Node,Function,Caller,Status,Duration(ms),Input,Output,Error\n" +
                logs.joinToString("\n") { log ->
                    "${log.id},${log.timestamp},${log.workflowName},${log.nodeName},${log.functionName},${log.caller},${log.status},${log.durationMs},\"${log.inputSummary}\",\"${log.outputSummary}\",\"${log.errorDetail ?: ""}\""
                }
            else -> ""
        }

        viewModelScope.launch {
            _effect.send(AppFunctionsWorkflowEffect.ExportAuditLogFile(content, "audit_log_${System.currentTimeMillis()}.$format"))
            _effect.send(AppFunctionsWorkflowEffect.ShowToast("审计日志已导出 / Audit log exported"))
        }
    }

    /**
     * Handle copy to clipboard action
     * 处理复制到剪贴板操作
     *
     * @param text Text to copy / 要复制的文本
     */
    private fun handleCopyToClipboard(text: String) {
        viewModelScope.launch {
            _effect.send(AppFunctionsWorkflowEffect.CopyToClipboard(text))
            _effect.send(AppFunctionsWorkflowEffect.ShowToast("已复制到剪贴板 / Copied to clipboard"))
        }
    }

    /**
     * Handle send test Feishu notification
     * 处理发送测试飞书通知
     *
     * @param mentionedUsers List of user IDs to mention / 需要 @ 的用户 ID 列表
     */
    private fun handleSendTestFeishuNotification(mentionedUsers: List<String>) {
        viewModelScope.launch {
            _effect.send(
                AppFunctionsWorkflowEffect.SendFeishuNotification(
                    title = "🧪 测试通知",
                    content = "这是一条来自 AppFunctions 工作流中间件的测试消息",
                    mentionedUsers = mentionedUsers
                )
            )
            _effect.send(AppFunctionsWorkflowEffect.ShowToast("测试通知已发送 / Test notification sent"))
        }
    }
}
