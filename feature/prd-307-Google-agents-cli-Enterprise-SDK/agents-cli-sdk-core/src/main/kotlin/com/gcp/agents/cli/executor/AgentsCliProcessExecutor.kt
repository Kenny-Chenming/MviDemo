package com.gcp.agents.cli.executor

import com.gcp.agents.cli.model.*
import com.gcp.agents.cli.parser.AgentDescriptorParser
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.util.concurrent.TimeUnit

/**
 * AgentsCliProcessExecutor — 执行 agents-cli 命令的核心执行器
 * Agents CLI process executor that wraps CLI invocations with type-safe parsing.
 *
 * @property agentsCliPath agents-cli 可执行文件路径 / Path to agents-cli binary
 * @property httpClient OkHttp client for HTTP operations
 */
class AgentsCliProcessExecutor(
    private val agentsCliPath: String = "agents-cli",
    private val httpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()
) {
    private val gson = Gson()
    private val parser = AgentDescriptorParser()

    /**
     * 部署单个 Agent / Deploy a single agent instance
     * @param request 部署请求对象 / Deployment request
     * @return 部署结果 / Deployment result
     */
    suspend fun deploy(request: AgentDeploymentRequest): DeploymentResult = withContext(Dispatchers.IO) {
        val args = buildDeployArgs(request)
        executeCommand(args) { stdout, stderr, exitCode ->
            if (exitCode == 0) {
                val agentInstance = parser.parseAgentInstance(stdout)
                DeploymentResult(
                    success = true,
                    agentInstance = agentInstance,
                    logUrl = extractLogUrl(stdout),
                    errorMessage = null
                )
            } else {
                DeploymentResult(
                    success = false,
                    agentInstance = null,
                    logUrl = null,
                    errorMessage = stderr.takeIf { it.isNotBlank() } ?: "Unknown error (exit code: $exitCode)"
                )
            }
        }
    }

    /**
     * 停止 Agent 实例 / Stop an agent instance
     * @param agentName Agent 名称 / Agent name
     * @param instanceId 实例 ID / Instance ID
     */
    suspend fun stop(agentName: String, instanceId: String): AgentOperationResult = withContext(Dispatchers.IO) {
        val args = listOf("agent", "stop", "--name", agentName, "--instance-id", instanceId)
        executeCommand(args) { stdout, stderr, exitCode ->
            AgentOperationResult(
                success = exitCode == 0,
                output = stdout,
                errorMessage = stderr.takeIf { exitCode != 0 }
            )
        }
    }

    /**
     * 查询 Agent 状态 / Query agent status
     * @param agentName Agent 名称 / Agent name
     * @return Agent 运行状态 / Agent runtime status
     */
    suspend fun getStatus(agentName: String): AgentDeploymentStatus = withContext(Dispatchers.IO) {
        val args = listOf("agent", "status", "--name", agentName)
        executeCommand(args) { stdout, _, exitCode ->
            if (exitCode == 0) {
                parser.parseDeploymentStatus(stdout)
            } else {
                AgentDeploymentStatus.FAILED
            }
        }
    }

    /**
     * 删除 Agent 实例 / Delete an agent instance
     * @param agentName Agent 名称 / Agent name
     * @param instanceId 实例 ID / Instance ID
     */
    suspend fun delete(agentName: String, instanceId: String): AgentOperationResult = withContext(Dispatchers.IO) {
        val args = listOf("agent", "delete", "--name", agentName, "--instance-id", instanceId)
        executeCommand(args) { stdout, stderr, exitCode ->
            AgentOperationResult(
                success = exitCode == 0,
                output = stdout,
                errorMessage = stderr.takeIf { exitCode != 0 }
            )
        }
    }

    /**
     * 列出所有 Agent 实例 / List all agent instances
     * @return Agent 实例列表 / List of agent instances
     */
    suspend fun listAgents(): List<AgentInstance> = withContext(Dispatchers.IO) {
        val args = listOf("agent", "list", "--output", "json")
        executeCommand(args) { stdout, _, exitCode ->
            if (exitCode == 0) {
                parser.parseAgentList(stdout)
            } else {
                emptyList()
            }
        }
    }

    /**
     * 构建部署命令参数 / Build deployment command arguments
     */
    private fun buildDeployArgs(request: AgentDeploymentRequest): List<String> {
        val args = mutableListOf("agent", "deploy", "--name", request.agentName)
        when (val source = request.agentSource) {
            is AgentSource.GitHubSource -> {
                args.addAll(listOf("--source", "github", "--repo", source.repo, "--branch", source.branch))
            }
            is AgentSource.LocalSource -> {
                args.addAll(listOf("--source", "local", "--path", source.path))
            }
            is AgentSource.GcsSource -> {
                args.addAll(listOf("--source", "gcs", "--bucket", source.bucket, "--object", source.objectKey))
            }
        }
        when (val target = request.targetGcpService) {
            is GcpTargetService.VertexAiEndpoint -> {
                args.addAll(listOf("--target", "vertex-ai", "--endpoint", target.endpoint))
            }
            is GcpTargetService.CloudRunService -> {
                args.addAll(listOf("--target", "cloud-run", "--service", target.serviceName, "--region", target.region))
            }
            is GcpTargetService.GkeCluster -> {
                args.addAll(listOf("--target", "gke", "--cluster", target.cluster, "--namespace", target.namespace))
            }
        }
        if (request.iamPolicy != null) {
            args.addAll(listOf("--iam-policy", gson.toJson(request.iamPolicy)))
        }
        return args
    }

    /**
     * 执行 CLI 命令 / Execute CLI command
     */
    private inline fun <T> executeCommand(args: List<String>, transform: (stdout: String, stderr: String, exitCode: Int) -> T): T {
        val processBuilder = ProcessBuilder(listOf(agentsCliPath) + args)
            .redirectErrorStream(false)

        val process = processBuilder.start()
        val stdout = BufferedReader(InputStreamReader(process.inputStream)).readText()
        val stderr = BufferedReader(InputStreamReader(process.errorStream)).readText()
        val exitCode = process.waitFor()
        return transform(stdout, stderr, exitCode)
    }

    /**
     * 从 stdout 提取日志 URL / Extract log URL from stdout
     */
    private fun extractLogUrl(stdout: String): String? {
        return parser.extractLogUrl(stdout)
    }
}
