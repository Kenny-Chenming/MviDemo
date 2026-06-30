package com.gcp.agents.cli.orchestration.context

import com.gcp.agents.cli.model.AgentInstance
import com.gcp.agents.cli.model.AgentDeploymentStatus

/**
 * OrchestrationContext — 编排上下文，持有所有已部署 agent 的引用
 */
class OrchestrationContext(
    val executionId: String,
    val namespace: String = "default"
) {
    private val _deployedAgents = mutableMapOf<String, AgentInstance>()

    val deployedAgents: Map<String, AgentInstance> = _deployedAgents

    fun addAgent(name: String, instance: AgentInstance) { _deployedAgents[name] = instance }
    fun getAgent(name: String): AgentInstance? = _deployedAgents[name]

    fun getRunningAgents(): List<AgentInstance> {
        return _deployedAgents.values.filter { it.status == AgentDeploymentStatus.RUNNING }
    }

    fun hasAgent(name: String): Boolean = _deployedAgents.containsKey(name)
    fun clear() = _deployedAgents.clear()
}
