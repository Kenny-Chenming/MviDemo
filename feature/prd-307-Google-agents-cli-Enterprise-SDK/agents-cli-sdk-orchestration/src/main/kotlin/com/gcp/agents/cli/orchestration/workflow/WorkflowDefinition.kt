package com.gcp.agents.cli.orchestration.workflow

import com.gcp.agents.cli.model.*

/**
 * WorkflowDefinition — 工作流定义 DSL
 * Workflow definition DSL for multi-agent orchestration.
 *
 * 用法示例 / Usage example:
 * ```kotlin
 * val workflow = WorkflowDefinition {
 *     name = "android-ci-workflow"
 *     parallelism = 2
 *
 *     step("code-review") {
 *         agent = "code-review-agent"
 *         triggerOn = Event.PR_CREATED
 *     }
 *
 *     step("security-scan") {
 *         agent = "security-agent"
 *         dependsOn("code-review")
 *         continueOnFailure = false
 *     }
 * }
 * ```
 */
class WorkflowDefinition {
    /** 工作流名称 / Workflow name */
    lateinit var name: String

    /** 并行度 / Parallelism */
    var parallelism: Int = 1

    /** 默认策略 / Default policy */
    var defaultPolicy: OrchestrationPolicy = OrchestrationPolicy.DEFAULT

    /** 工作流步骤映射 / Workflow steps map */
    private val _steps = mutableMapOf<String, WorkflowStep>()

    /** 获取所有步骤 / Get all steps */
    val steps: Map<String, WorkflowStep> = _steps

    /** 添加步骤 / Add a step */
    fun step(stepName: String, block: WorkflowStep.() -> Unit) {
        _steps[stepName] = WorkflowStep(stepName).apply(block)
    }

    /** 验证工作流 DAG（检测循环依赖）/ Validate workflow DAG (detect circular dependencies) */
    fun validate(): Boolean {
        val visited = mutableSetOf<String>()
        val recursionStack = mutableSetOf<String>()

        fun hasCycle(stepName: String): Boolean {
            if (stepName in recursionStack) return true
            if (stepName in visited) return false

            visited.add(stepName)
            recursionStack.add(stepName)

            val step = _steps[stepName] ?: return false
            for (dep in step.dependsOn) {
                if (hasCycle(dep)) return true
            }

            recursionStack.remove(stepName)
            return false
        }

        return _steps.keys.none { hasCycle(it) }
    }

    /** 转换为 DAG 表示 / Convert to DAG representation */
    fun toDAG(): WorkflowGraph {
        return WorkflowGraph(this)
    }
}

/**
 * WorkflowStep — 工作流步骤定义
 * Workflow step definition.
 *
 * @property name 步骤名称 / Step name
 * @property agent 关联的 Agent 名称 / Associated agent name
 * @property triggerOn 触发事件 / Trigger event
 * @property dependsOn 依赖的步骤 / Dependent steps
 * @property continueOnFailure 失败后是否继续 / Continue on failure
 * @property targetEnvironment 目标环境 / Target environment
 * @property policy 此步骤的覆盖策略 / Override policy for this step
 */
class WorkflowStep(
    val name: String
) {
    lateinit var agent: String

    /** 触发事件 / Trigger event */
    var triggerOn: Event? = null

    /** 依赖的步骤列表 / List of dependent steps */
    val dependsOn: MutableList<String> = mutableListOf()

    /** 失败后是否继续 / Continue on failure */
    var continueOnFailure: Boolean = false

    /** 目标环境 / Target environment */
    var targetEnvironment: Environment? = null

    /** 步骤级策略覆盖 / Step-level policy override */
    var policy: OrchestrationPolicy? = null

    /** 触发事件类型 / Trigger event types */
    enum class Event {
        PR_CREATED, PR_UPDATED, PR_MERGED,
        PUSH, MANUAL, SCHEDULED, WEBHOOK
    }

    /** 部署环境 / Deployment environment */
    enum class Environment {
        DEV, STAGING, PRODUCTION
    }

    /** 添加依赖步骤 / Add dependent step */
    fun dependsOn(vararg steps: String) {
        dependsOn.addAll(steps)
    }
}

/**
 * WorkflowGraph — 工作流的 DAG 表示
 * DAG representation of a workflow.
 *
 * @param workflow 工作流定义 / Workflow definition
 */
class WorkflowGraph(
    private val workflow: WorkflowDefinition
) {
    /** 获取步骤的拓扑排序 / Get topological sort of steps */
    fun topologicalSort(): List<String> {
        val result = mutableListOf<String>()
        val visited = mutableSetOf<String>()
        val tempMark = mutableSetOf<String>()

        fun dfs(stepName: String): Boolean {
            if (stepName in tempMark) return false // Cycle detected
            if (stepName in visited) return true

            tempMark.add(stepName)
            val step = workflow.steps[stepName] ?: return false

            for (dep in step.dependsOn) {
                if (!dfs(dep)) return false
            }

            tempMark.remove(stepName)
            visited.add(stepName)
            result.add(stepName)
            return true
        }

        workflow.steps.keys.forEach { step ->
            if (step !in visited) {
                dfs(step)
            }
        }

        return result
    }

    /** 获取可并行执行的步骤组 / Get step groups that can run in parallel */
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

    /** 获取步骤的直接后继 / Get direct successors of a step */
    fun getSuccessors(stepName: String): List<String> {
        return workflow.steps.values
            .filter { stepName in it.dependsOn }
            .map { it.name }
    }

    /** 获取步骤的直接前驱 / Get direct predecessors of a step */
    fun getPredecessors(stepName: String): List<String> {
        return workflow.steps[stepName]?.dependsOn ?: emptyList()
    }
}

/**
 * OrchestrationPolicy — 从 workflow 包重新导出
 * Re-exported from workflow package for convenience.
 */
typealias OrchestrationPolicy = com.gcp.agents.cli.orchestration.policy.OrchestrationPolicy
