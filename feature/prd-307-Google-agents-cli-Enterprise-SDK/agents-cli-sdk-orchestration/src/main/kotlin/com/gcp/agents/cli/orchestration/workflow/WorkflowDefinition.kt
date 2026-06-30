package com.gcp.agents.cli.orchestration.workflow

import com.gcp.agents.cli.model.*
import com.gcp.agents.cli.orchestration.policy.OrchestrationPolicy

/**
 * WorkflowDefinition — 工作流定义 DSL
 */
class WorkflowDefinition {
    lateinit var name: String
    var parallelism: Int = 1
    var defaultPolicy: OrchestrationPolicy = OrchestrationPolicy.DEFAULT
    private val _steps = mutableMapOf<String, WorkflowStep>()

    val steps: Map<String, WorkflowStep> = _steps

    fun step(stepName: String, block: WorkflowStep.() -> Unit) {
        _steps[stepName] = WorkflowStep(stepName).apply(block)
    }

    fun validate(): Boolean {
        val visited = mutableSetOf<String>()
        val recursionStack = mutableSetOf<String>()

        fun hasCycle(stepName: String): Boolean {
            if (stepName in recursionStack) return true
            if (stepName in visited) return false
            visited.add(stepName)
            recursionStack.add(stepName)
            val step = _steps[stepName] ?: return false
            for (dep in step.dependsOn) { if (hasCycle(dep)) return true }
            recursionStack.remove(stepName)
            return true
        }
        return _steps.keys.none { hasCycle(it) }
    }

    fun toDAG(): WorkflowGraph = WorkflowGraph(this)
}

class WorkflowStep(val name: String) {
    lateinit var agent: String
    var triggerOn: Event? = null
    val dependsOn: MutableList<String> = mutableListOf()
    var continueOnFailure: Boolean = false
    var targetEnvironment: Environment? = null
    var policy: OrchestrationPolicy? = null

    fun dependsOn(vararg steps: String) { dependsOn.addAll(steps) }

    enum class Event { PR_CREATED, PR_UPDATED, PR_MERGED, PUSH, MANUAL, SCHEDULED, WEBHOOK }
    enum class Environment { DEV, STAGING, PRODUCTION }
}

class WorkflowGraph(private val workflow: WorkflowDefinition) {
    fun topologicalSort(): List<String> {
        val result = mutableListOf<String>()
        val visited = mutableSetOf<String>()
        val tempMark = mutableSetOf<String>()

        fun dfs(stepName: String): Boolean {
            if (stepName in tempMark) return false
            if (stepName in visited) return true
            tempMark.add(stepName)
            val step = workflow.steps[stepName] ?: return false
            for (dep in step.dependsOn) { if (!dfs(dep)) return false }
            tempMark.remove(stepName)
            visited.add(stepName)
            result.add(stepName)
            return true
        }
        workflow.steps.keys.forEach { if (it !in visited) dfs(it) }
        return result
    }

    fun getParallelGroups(): List<List<String>> {
        val sorted = topologicalSort()
        val groups = mutableListOf<List<String>>()
        val completed = mutableSetOf<String>()

        while (completed.size < workflow.steps.size) {
            val group = sorted.filter { step ->
                val s = workflow.steps[step]!!
                s.dependsOn.all { it in completed }
            }.filter { it !in completed }
            if (group.isEmpty()) break
            groups.add(group)
            completed.addAll(group)
        }
        return groups
    }

    fun getSuccessors(stepName: String): List<String> = workflow.steps.values.filter { stepName in it.dependsOn }.map { it.name }
    fun getPredecessors(stepName: String): List<String> = workflow.steps[stepName]?.dependsOn ?: emptyList()
}

typealias OrchestrationPolicy = com.gcp.agents.cli.orchestration.policy.OrchestrationPolicy
