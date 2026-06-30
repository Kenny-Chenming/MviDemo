package com.gcp.agents.cli.parser

import com.gcp.agents.cli.model.*
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import java.util.regex.Pattern

/**
 * AgentDescriptorParser — 解析 agents-cli 输出的 agent 元数据
 * Parses agent metadata from agents-cli JSON output.
 *
 * 注意：Google 官方 CLI 的输出格式可能在版本间变化
 * WARNING: Google official CLI output format may change between versions.
 */
class AgentDescriptorParser {
    private val gson = Gson()

    /**
     * 解析 Agent 实例 / Parse agent instance from JSON output
     * @param jsonOutput agents-cli JSON 输出 / agents-cli JSON output
     * @return AgentInstance 对象 / AgentInstance object
     */
    fun parseAgentInstance(jsonOutput: String): AgentInstance {
        return try {
            val json = JsonParser.parseString(jsonOutput).asJsonObject
            val statusStr = json.get("status")?.asString ?: "PENDING"
            AgentInstance(
                name = json.get("name")?.asString ?: "",
                instanceId = json.get("instanceId")?.asString ?: json.get("id")?.asString ?: "",
                status = parseDeploymentStatus(jsonOutput),
                endpoint = json.get("endpoint")?.asString ?: json.get("serviceUrl")?.asString,
                config = parseConfig(json.getAsJsonObject("config")),
                gcpProjectId = json.get("projectId")?.asString ?: "",
                region = json.get("region")?.asString ?: "us-central1",
                createdAt = json.get("createdAt")?.asLong ?: System.currentTimeMillis(),
                lastHeartbeat = json.get("lastHeartbeat")?.asLong ?: System.currentTimeMillis()
            )
        } catch (e: Exception) {
            // Fallback: 尝试从纯文本解析 / Fallback: try to parse from plain text
            parseFromPlainText(jsonOutput)
        }
    }

    /**
     * 解析部署状态 / Parse deployment status
     * @param output 命令输出 / Command output
     * @return AgentDeploymentStatus
     */
    fun parseDeploymentStatus(output: String): AgentDeploymentStatus {
        val lowerOutput = output.lowercase()
        return when {
            lowerOutput.contains("\"status\":\"pending\"") || lowerOutput.contains("status: pending") -> AgentDeploymentStatus.PENDING
            lowerOutput.contains("\"status\":\"deploying\"") || lowerOutput.contains("status: deploying") -> AgentDeploymentStatus.DEPLOYING
            lowerOutput.contains("\"status\":\"running\"") || lowerOutput.contains("status: running") || lowerOutput.contains("✓") -> AgentDeploymentStatus.RUNNING
            lowerOutput.contains("\"status\":\"stopping\"") || lowerOutput.contains("status: stopping") -> AgentDeploymentStatus.STOPPING
            lowerOutput.contains("\"status\":\"stopped\"") || lowerOutput.contains("status: stopped") -> AgentDeploymentStatus.STOPPED
            lowerOutput.contains("\"status\":\"updating\"") || lowerOutput.contains("status: updating") -> AgentDeploymentStatus.UPDATING
            lowerOutput.contains("✗") || lowerOutput.contains("failed") || lowerOutput.contains("error") -> AgentDeploymentStatus.FAILED
            else -> AgentDeploymentStatus.PENDING
        }
    }

    /**
     * 解析 Agent 列表 / Parse agent list
     * @param jsonOutput JSON 输出 / JSON output
     * @return Agent 实例列表 / List of agent instances
     */
    fun parseAgentList(jsonOutput: String): List<AgentInstance> {
        return try {
            val json = JsonParser.parseString(jsonOutput)
            when {
                json.isJsonArray -> json.asJsonArray.map { parseAgentInstance(it.toString()) }
                json.isJsonObject -> {
                    val agents = json.asJsonObject.getAsJsonArray("agents")
                        ?: json.asJsonObject.getAsJsonArray("items")
                        ?: json.asJsonObject.getAsJsonArray("instances")
                    agents.map { parseAgentInstance(it.toString()) }
                }
                else -> emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * 从纯文本输出解析 / Parse from plain text output
     * Fallback method for unstable CLI output formats.
     */
    private fun parseFromPlainText(output: String): AgentInstance {
        val namePattern = Pattern.compile("name[:\\s]+([\\w-]+)", Pattern.CASE_INSENSITIVE)
        val idPattern = Pattern.compile("(?:instance[_-]?id|id)[:\\s]+([\\w-]+)", Pattern.CASE_INSENSITIVE)
        val endpointPattern = Pattern.compile("(?:endpoint|url)[:\\s]+(https?://[\\w./:-]+)", Pattern.CASE_INSENSITIVE)

        val nameMatcher = namePattern.matcher(output)
        val idMatcher = idPattern.matcher(output)
        val endpointMatcher = endpointPattern.matcher(output)

        return AgentInstance(
            name = if (nameMatcher.find()) nameMatcher.group(1) else "unknown",
            instanceId = if (idMatcher.find()) idMatcher.group(1) ?: "" else "",
            status = parseDeploymentStatus(output),
            endpoint = if (endpointMatcher.find()) endpointMatcher.group(1) else null,
            config = AgentConfig(
                version = "unknown",
                runtime = "unknown",
                resources = ResourceAllocation(2, 8, 50),
                environmentVariables = emptyMap()
            ),
            gcpProjectId = "",
            region = "us-central1",
            createdAt = System.currentTimeMillis(),
            lastHeartbeat = System.currentTimeMillis()
        )
    }

    /**
     * 解析 Agent 配置 / Parse agent config
     */
    private fun parseConfig(configJson: JsonObject?): AgentConfig {
        if (configJson == null) {
            return AgentConfig(
                version = "unknown",
                runtime = "unknown",
                resources = ResourceAllocation(2, 8, 50),
                environmentVariables = emptyMap()
            )
        }
        val resources = configJson.getAsJsonObject("resources")
        return AgentConfig(
            version = configJson.get("version")?.asString ?: "unknown",
            runtime = configJson.get("runtime")?.asString ?: "unknown",
            resources = ResourceAllocation(
                cpu = resources?.get("cpu")?.asInt ?: 2,
                memoryGb = resources?.get("memoryGb")?.asInt ?: 8,
                diskGb = resources?.get("diskGb")?.asInt ?: 50
            ),
            environmentVariables = emptyMap()
        )
    }

    /**
     * 从输出中提取日志 URL / Extract log URL from output
     * @param output 命令输出 / Command output
     * @return 日志 URL 或 null / Log URL or null
     */
    fun extractLogUrl(output: String): String? {
        val logPattern = Pattern.compile("(https?://[\\w./:-]+logs?[\\w./:&-=]*)|(logs?[:\\s]+(https?://[\\w./:-]+))", Pattern.CASE_INSENSITIVE)
        val matcher = logPattern.matcher(output)
        return if (matcher.find()) {
            matcher.group(1) ?: matcher.group(3)
        } else {
            null
        }
    }
}
