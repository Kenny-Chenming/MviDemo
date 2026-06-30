package com.gcp.agents.cli.iam.quota

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.ConcurrentHashMap

/**
 * QuotaController — Token 用量配额管理
 */
class QuotaController(private val defaultQuota: Int = 10000, private val warningThreshold: Float = 0.8f) {
    private val _quotas = MutableStateFlow<Map<String, AgentQuota>>(emptyMap())
    val quotas: StateFlow<Map<String, AgentQuota>> = _quotas.asStateFlow()
    private val usageTracker = ConcurrentHashMap<String, UsageTracker>()

    fun setQuota(agentName: String, hourlyLimit: Int, dailyLimit: Int? = null) {
        val current = _quotas.value.toMutableMap()
        current[agentName] = AgentQuota(agentName, hourlyLimit, dailyLimit)
        _quotas.value = current
    }

    fun canExecute(agentName: String, tokensRequested: Int): QuotaCheckResult {
        val quota = _quotas.value[agentName] ?: AgentQuota(agentName, defaultQuota, null)
        val tracker = usageTracker.computeIfAbsent(agentName) { UsageTracker() }

        val hourlyUsed = tracker.getHourlyUsage()
        val dailyUsed = tracker.getDailyUsage()
        val hourlyAllowed = hourlyUsed + tokensRequested <= quota.hourlyLimit
        val dailyAllowed = quota.dailyLimit?.let { dailyUsed + tokensRequested <= it } ?: true

        return when {
            !hourlyAllowed -> QuotaCheckResult.Denied("Hourly quota exceeded: ${hourlyUsed}/${quota.hourlyLimit}")
            !dailyAllowed -> QuotaCheckResult.Denied("Daily quota exceeded: ${dailyUsed}/${quota.dailyLimit}")
            hourlyUsed >= (quota.hourlyLimit * warningThreshold).toInt() -> QuotaCheckResult.Warning("Hourly quota warning: ${hourlyUsed}/${quota.hourlyLimit}")
            else -> QuotaCheckResult.Allowed
        }
    }

    fun recordUsage(agentName: String, tokensUsed: Int) {
        val tracker = usageTracker.computeIfAbsent(agentName) { UsageTracker() }
        tracker.record(tokensUsed)
    }

    fun getUsage(agentName: String): QuotaUsage {
        val tracker = usageTracker[agentName] ?: UsageTracker()
        val quota = _quotas.value[agentName] ?: AgentQuota(agentName, defaultQuota, null)
        return QuotaUsage(agentName, tracker.getHourlyUsage(), quota.hourlyLimit, tracker.getDailyUsage(), quota.dailyLimit)
    }

    fun resetUsage(agentName: String) { usageTracker.remove(agentName) }
}

data class AgentQuota(val agentName: String, val hourlyLimit: Int, val dailyLimit: Int?)
sealed class QuotaCheckResult { object Allowed : QuotaCheckResult(); data class Warning(val message: String) : QuotaCheckResult(); data class Denied(val reason: String) : QuotaCheckResult() }
data class QuotaUsage(val agentName: String, val hourlyUsed: Int, val hourlyLimit: Int, val dailyUsed: Int, val dailyLimit: Int?)

class UsageTracker {
    private val hourlyUsage = mutableListOf<Pair<Long, Int>>()
    private val dailyUsage = mutableListOf<Pair<Long, Int>>()

    fun record(tokens: Int) {
        val now = System.currentTimeMillis()
        hourlyUsage.add(now to tokens)
        dailyUsage.add(now to tokens)
        cleanup()
    }

    fun getHourlyUsage(): Int {
        cleanup()
        val hourAgo = System.currentTimeMillis() - 3600000
        return hourlyUsage.filter { it.first >= hourAgo }.sumOf { it.second }
    }

    fun getDailyUsage(): Int {
        cleanup()
        val dayAgo = System.currentTimeMillis() - 86400000
        return dailyUsage.filter { it.first >= dayAgo }.sumOf { it.second }
    }

    private fun cleanup() {
        val hourAgo = System.currentTimeMillis() - 3600000
        val dayAgo = System.currentTimeMillis() - 86400000
        hourlyUsage.removeAll { it.first < hourAgo }
        dailyUsage.removeAll { it.first < dayAgo }
    }
}
