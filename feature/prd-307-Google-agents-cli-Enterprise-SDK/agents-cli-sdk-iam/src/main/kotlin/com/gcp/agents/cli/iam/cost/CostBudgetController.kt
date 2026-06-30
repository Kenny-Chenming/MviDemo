package com.gcp.agents.cli.iam.cost

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.ConcurrentHashMap

/**
 * CostBudgetController — 月度/日度预算控制
 */
class CostBudgetController(
    private val monthlyBudget: Double,
    private val dailyBudget: Double? = null,
    private val currency: String = "USD"
) {
    private val _currentSpending = MutableStateFlow(CostSpending(0.0, 0.0, currency))
    val currentSpending: StateFlow<CostSpending> = _currentSpending.asStateFlow()

    private val _alerts = MutableStateFlow<List<CostAlert>>(emptyList())
    val alerts: StateFlow<List<CostAlert>> = _alerts.asStateFlow()

    private val spendingByAgent = ConcurrentHashMap<String, Double>()
    private val warningThresholds = listOf(0.5f, 0.75f, 0.9f, 1.0f)

    fun canExecute(agentName: String, estimatedCost: Double): CostCheckResult {
        val current = _currentSpending.value
        val projectedMonthly = current.monthlySpending + estimatedCost
        val projectedDaily = current.dailySpending + estimatedCost
        val monthlyAllowed = projectedMonthly <= monthlyBudget
        val dailyAllowed = dailyBudget?.let { projectedDaily <= it } ?: true

        return when {
            !monthlyAllowed -> CostCheckResult.Denied("Monthly budget exceeded: \$${current.monthlySpending}/$${monthlyBudget}")
            !dailyAllowed -> CostCheckResult.Denied("Daily budget exceeded: \$${current.dailySpending}/$${dailyBudget}")
            projectedMonthly >= (monthlyBudget * 0.9) -> CostCheckResult.Warning("Monthly budget almost exhausted: ${(projectedMonthly / monthlyBudget * 100).toInt()}%")
            else -> CostCheckResult.Allowed
        }
    }

    fun recordSpending(agentName: String, actualCost: Double) {
        spendingByAgent.merge(agentName, actualCost, Double::plus)
        val current = _currentSpending.value
        _currentSpending.value = CostSpending(current.monthlySpending + actualCost, current.dailySpending + actualCost, currency)
        checkAlerts(agentName, actualCost)
    }

    private fun checkAlerts(agentName: String, cost: Double) {
        val current = _currentSpending.value
        val monthlyPct = current.monthlySpending / monthlyBudget
        for (threshold in warningThresholds) {
            if (monthlyPct >= threshold) {
                val alert = CostAlert(agentName, threshold, current.monthlySpending, monthlyBudget, currency, System.currentTimeMillis())
                if (_alerts.value.none { it.threshold == threshold }) {
                    _alerts.value = _alerts.value + alert
                }
            }
        }
    }

    fun resetDaily() { _currentSpending.value = CostSpending(_currentSpending.value.monthlySpending, 0.0, currency) }
    fun resetMonthly() { _currentSpending.value = CostSpending(0.0, 0.0, currency); spendingByAgent.clear(); _alerts.value = emptyList() }
    fun getSpendingByAgent(): Map<String, Double> = spendingByAgent.toMap()
}

data class CostSpending(val monthlySpending: Double, val dailySpending: Double, val currency: String)
data class CostAlert(val agentName: String, val threshold: Float, val currentSpending: Double, val budget: Double, val currency: String, val timestamp: Long)
sealed class CostCheckResult { object Allowed : CostCheckResult(); data class Warning(val message: String) : CostCheckResult(); data class Denied(val reason: String) : CostCheckResult() }
