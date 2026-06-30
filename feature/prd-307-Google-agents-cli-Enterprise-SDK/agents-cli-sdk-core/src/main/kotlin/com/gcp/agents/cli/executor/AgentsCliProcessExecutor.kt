package com.gcp.agents.cli.executor

import com.gcp.agents.cli.model.*
import com.gcp.agents.cli.parser.AgentDescriptorParser
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.concurrent.TimeUnit

/**
 * AgentsCliProcessExecutor — 执行 agents-cli 命令的核心执行器
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

    suspend fun stop(agentName: String, instanceId: String): AgentOperationResult = withContext(Dispatchers.IO) {
        val args = listOf("agent", "stop", "--name", agentName, "--instance-id", instanceId)
        executeCommand(args) { stdout, stderr, exitCode ->
            AgentOperationResult(exitCode == 0, stdout, stderr.takeIf { exitCode != 0 })
        }
    }

    suspend fun getStatus(agentName: String): AgentDeploymentStatus = withContext(Dispatchers.IO) {
        val args = listOf("agent", "status", "--name", agentName)
        executeCommand(args) { stdout, _, exitCode ->
            if (exitCode == 0) parser.parseDeploymentStatus(stdout) else AgentDeploymentStatus.FAILED
        }
    }

    suspend fun delete(agentName: String, instanceId: String): AgentOperationResult = withContext(Dispatchers.IO) {
        val args = listOf("agent", "delete", "--name", agentName, "--instance-id", instanceId)
        executeCommand(args) { stdout, stderr, exitCode ->
            AgentOperationResult(exitCode == 0, stdout, stderr.takeIf { exitCode != 0 })
        }
    }

    suspend fun listAgents(): List<AgentInstance> = withContext(Dispatchers.IO) {
        val args = listOf("agent", "list", "--output", "json")
        executeCommand(args) { stdout, _, exitCode ->
            if (exitCode == 0) parser.parseAgentList(stdout) else emptyList()
        }
    }

    private fun buildDeployArgs(request: AgentDeploymentRequest): List<String> {
        val args = mutableListOf("agent", "deploy", "--name", request.agentName)
        when (val source = request.agentSource) {
            is AgentSource.GitHubSource -> args.addAll(listOf("--source", "github", "--repo", source.repo, "--branch", source.branch))
            is AgentSource.LocalSource -> args.addAll(listOf("--source", "local", "--path", source.path))
            is AgentSource.GcsSource -> args.addAll(listOf("--source", "gcs", "--bucket", source.bucket, "--object", source.objectKey))
        }
        when (val target = request.targetGcpService) {
            is GcpTargetService.VertexAiEndpoint -> args.addAll(listOf("--target", "vertex-ai", "--endpoint", target.endpoint))
            is GcpTargetService.CloudRunService -> args.addAll(listOf("--target", "cloud-run", "--service", target.serviceName, "--region", target.region))
            is GcpTargetService.GkeCluster -> args.addAll(listOf("--target", "gke", "--cluster", target.cluster, "--namespace", target.namespace))
        }
        return args
    }

    private inline fun <T> executeCommand(args: List<String>, transform: (stdout: String, stderr: String, exitCode: Int) -> T): T {
        val processBuilder = ProcessBuilder(listOf(agentsCliPath) + args)
        val process = processBuilder.start()
        val stdout = BufferedReader(InputStreamReader(process.inputStream)).readText()
        val stderr = BufferedReader(InputStreamReader(process.errorStream)).readText()
        val exitCode = process.waitFor()
        return transform(stdout, stderr, exitCode)
    }

    private fun extractLogUrl(stdout: String): String? = parser.extractLogUrl(stdout)
}
