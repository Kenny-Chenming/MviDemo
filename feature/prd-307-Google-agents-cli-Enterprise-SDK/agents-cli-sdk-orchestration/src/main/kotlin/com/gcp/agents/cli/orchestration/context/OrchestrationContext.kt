package com.gcp.agents.cli.orchestration.context

import com.gcp.agents.cli.model.AgentInstance

/**
 * OrchestrationContext — 编排上下文，持有所有已部署 agent 的引用
 * Orchestration context holding references to all deployed agents.
 *
 * @property deployedAgents 已部署的 Agent 映射 / Map of deployed agents
 * @property executionId 当前执行 ID / Current execution ID
 * @property namespace 命名空间（用于多租户隔离）/ Namespace for multi-tenant isolation
 */
class OrchestrationContext(
    val executionId: String,
    val namespace: String = "default"
) {
    private val _deployedAgents = mutableMapOf<String, AgentInstance>()

    /** 获取所有已部署的 Agent / Get all deployed agents */
    val deployedAgents: Map<String, AgentInstance> = _deployedAgents

    /** 添加已部署的 Agent / Add a deployed agent */
    fun addAgent(name: String, instance: AgentInstance) {
        _deployedAgents[name] = instance
    }

    /** 获取指定名称的 Agent / Get agent by name */
    fun getAgent(name: String): AgentInstance? = _deployedAgents[name]

    /** 获取所有运行中的 Agent / Get all running agents */
    fun getRunningAgents(): List<AgentInstance> {
        return _deployedAgents.values.filter { it.status == com.gcp.agents.cli.model.AgentDeploymentStatus.RUNNING }
    }

    /** 检查 Agent 是否存在 / Check if agent exists */
    fun hasAgent(name: String): Boolean = _deployedAgents.containsKey(name)

    /** 清空上下文 / Clear context */
    fun clear() = _deployedAgents.clear()
}
