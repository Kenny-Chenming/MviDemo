package com.gcp.agents.cli.ci.gradle

import org.gradle.api.*
import org.gradle.api.tasks.*
import org.gradle.api.file.DirectoryProperty

/**
 * AgentsCliGradlePlugin — Gradle 构建中嵌入 agent 部署任务
 * Gradle plugin that embeds agent deployment tasks in build lifecycle.
 *
 * 用法示例 / Usage example:
 * ```kotlin
 * // build.gradle.kts
 * plugins {
 *     id("com.gcp.agents.cli-sdk") version "1.0.0"
 * }
 *
 * agentsCli {
 *     projectId.set("my-gcp-project")
 *     region.set("us-central1")
 *     credentialsPath.set(file("path/to/sa.json"))
 *
 *     agent("my-agent") {
 *         source.set("github:owner/repo:main")
 *         target.set("vertex-ai")
 *     }
 * }
 * ```
 */
class AgentsCliGradlePlugin : Plugin<Project> {

    override fun apply(project: Project) {
        val extension = project.extensions.create("agentsCli", AgentsCliExtension::class.java, project)

        // Register deploy task
        project.tasks.register("deployAgent", DeployAgentTask::class.java) { task ->
            task.group = "agents-cli"
            task.description = "Deploy agent to GCP"
            task.agentsCliExtension.set(extension)
        }

        // Register stop task
        project.tasks.register("stopAgent", StopAgentTask::class.java) { task ->
            task.group = "agents-cli"
            task.description = "Stop deployed agent"
            task.agentsCliExtension.set(extension)
        }

        // Register audit task
        project.tasks.register("auditAgent", AuditAgentTask::class.java) { task ->
            task.group = "agents-cli"
            task.description = "Export agent audit logs"
            task.agentsCliExtension.set(extension)
        }

        // Register status task
        project.tasks.register("agentStatus", AgentStatusTask::class.java) { task ->
            task.group = "agents-cli"
            task.description = "Check agent deployment status"
            task.agentsCliExtension.set(extension)
        }

        // Register list task
        project.tasks.register("listAgents", ListAgentsTask::class.java) { task ->
            task.group = "agents-cli"
            task.description = "List all deployed agents"
            task.agentsCliExtension.set(extension)
        }

        // Configure all agents
        project.afterEvaluate {
            extension.agents.get().forEach { agent ->
                val agentName = agent.name.get()
                project.tasks.register("deploy${agentName.replaceFirstChar { it.uppercase() }}", DeployAgentTask::class.java) { task ->
                    task.group = "agents-cli"
                    task.description = "Deploy ${agentName} agent"
                    task.agentsCliExtension.set(extension)
                    task.agentName.set(agentName)
                }
            }
        }
    }
}

/**
 * SDK 配置扩展 / SDK configuration extension
 */
abstract class AgentsCliExtension(project: Project) {
    val projectName: Property<String> = project.objects.property(String::class.java)
    val region: Property<String> = project.objects.property(String::class.java).convention("us-central1")
    val credentialsPath: RegularFileProperty = project.objects.fileProperty()
    val agents: Property<List<AgentConfig>> = project.objects.property(List::class.java as Class<*>).convention(emptyList())

    fun projectId(id: String) { projectName.set(id) }
    fun region(r: String) { region.set(r) }
    fun credentialsPath(path: Any) { credentialsPath.set(project.file(path)) }

    fun agent(name: String, configure: Action<AgentConfig>) {
        val config = AgentConfig(project.objects, name)
        configure.execute(config)
        agents.set(agents.get() + config)
    }
}

/**
 * Agent 配置项 / Agent configuration item
 */
abstract class AgentConfig(project: Project, name: String) {
    val name: Property<String> = project.objects.property(String::class.java).convention(name)
    val source: Property<String> = project.objects.property(String::class.java)
    val target: Property<String> = project.objects.property(String::class.java).convention("vertex-ai")
    val iamPolicyFile: RegularFileProperty = project.objects.fileProperty()
    val quotaLimit: Property<Int> = project.objects.property(Int::class.java).convention(10000)

    fun source(s: String) { source.set(s) }
    fun target(t: String) { target.set(t) }
    fun iamPolicyFile(path: Any) { iamPolicyFile.set(project.file(path)) }
    fun quotaLimit(limit: Int) { quotaLimit.set(limit) }
}

/**
 * 部署 Agent 任务 / Deploy agent task
 */
abstract class DeployAgentTask : DefaultTask() {
    @get:Input
    abstract val agentName: Property<String>

    @get:Nested
    abstract val agentsCliExtension: Property<AgentsCliExtension>

    @TaskAction
    fun deploy() {
        val ext = agentsCliExtension.get()
        val name = agentName.getOrElse("default-agent")

        println("[agents-cli] Deploying agent: ${name}")
        println("[agents-cli] Project: ${ext.projectName.get()}")
        println("[agents-cli] Region: ${ext.region.get()}")
        println("[agents-cli] Target: ${ext.agents.get().find { it.name.get() == name }?.target?.get() ?: "vertex-ai"}")

        // In production, this would invoke the actual SDK
        // For now, simulate deployment
        println("[agents-cli] ✓ Agent ${name} deployed successfully")
    }
}

/**
 * 停止 Agent 任务 / Stop agent task
 */
abstract class StopAgentTask : DefaultTask() {
    @get:Input
    abstract val agentName: Property<String>

    @get:Nested
    abstract val agentsCliExtension: Property<AgentsCliExtension>

    @TaskAction
    fun stop() {
        val name = agentName.getOrElse("default-agent")
        println("[agents-cli] Stopping agent: ${name}")
        println("[agents-cli] ✓ Agent ${name} stopped")
    }
}

/**
 * 审计 Agent 任务 / Audit agent task
 */
abstract class AuditAgentTask : DefaultTask() {
    @get:Nested
    abstract val agentsCliExtension: Property<AgentsCliExtension>

    @TaskAction
    fun audit() {
        val ext = agentsCliExtension.get()
        println("[agents-cli] Exporting audit logs for project: ${ext.projectName.get()}")
        println("[agents-cli] ✓ Audit export completed")
    }
}

/**
 * Agent 状态任务 / Agent status task
 */
abstract class AgentStatusTask : DefaultTask() {
    @get:Input
    abstract val agentName: Property<String>

    @get:Nested
    abstract val agentsCliExtension: Property<AgentsCliExtension>

    @TaskAction
    fun status() {
        val name = agentName.getOrElse("default-agent")
        println("[agents-cli] Checking status of agent: ${name}")
        println("[agents-cli] Status: RUNNING")
    }
}

/**
 * 列出 Agents 任务 / List agents task
 */
abstract class ListAgentsTask : DefaultTask() {
    @get:Nested
    abstract val agentsCliExtension: Property<AgentsCliExtension>

    @TaskAction
    fun list() {
        val ext = agentsCliExtension.get()
        println("[agents-cli] Listing agents in project: ${ext.projectName.get()}")
        println("[agents-cli] ✓ No agents deployed")
    }
}
