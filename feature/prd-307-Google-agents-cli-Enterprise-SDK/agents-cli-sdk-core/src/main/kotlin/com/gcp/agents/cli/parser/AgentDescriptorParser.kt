package com.gcp.agents.cli.parser

import com.gcp.agents.cli.model.*
import com.google.gson.JsonParser
import java.util.regex.Pattern

/**
 * AgentDescriptorParser — 解析 agents-cli 输出的 agent 元数据
 */
class AgentDescriptorParser {

    fun parseAgentInstance(jsonOutput: String): AgentInstance {
        return try {
            val json = JsonParser.parseString(jsonOutput).asJsonObject
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
            parseFromPlainText(jsonOutput)
        }
    }

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

    fun parseAgentList(jsonOutput: String): List<AgentInstance> {
        return try {
            val json = JsonParser.parseString(jsonOutput)
            when {
                json.isJsonArray -> json.asJsonArray.map { parseAgentInstance(it.toString()) }
                json.isJsonObject -> {
                    val agents = json.asJsonObject.getAsJsonArray("agents")
                        ?: json.asJsonObject.getAsJsonArray("items")
                        ?: json.asJsonObject.getAsJsonArray("instances")
                    agents?.map { parseAgentInstance(it.toString()) } ?: emptyList()
                }
                else -> emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

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
            config = AgentConfig("unknown", "unknown", ResourceAllocation(2, 8, 50), emptyMap()),
            gcpProjectId = "",
            region = "us-central1",
            createdAt = System.currentTimeMillis(),
            lastHeartbeat = System.currentTimeMillis()
        )
    }

    private fun parseConfig(configJson: com.google.gson.JsonObject?): AgentConfig {
        if (configJson == null) return AgentConfig("unknown", "unknown", ResourceAllocation(2, 8, 50), emptyMap())
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

    fun extractLogUrl(output: String): String? {
        val logPattern = Pattern.compile("(https?://[\\w./:-]+logs?[\\w./:&-=]*)|(logs?[:\\s]+(https?://[\\w./:-]+))", Pattern.CASE_INSENSITIVE)
        val matcher = logPattern.matcher(output)
        return if (matcher.find()) matcher.group(1) ?: matcher.group(3) else null
    }
}
