package com.gcp.agents.cli.iam.cost

import com.gcp.agents.cli.AgentsCliSdk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.ConcurrentHashMap

/**
 * CostBudgetController — 月度/日度预算控制
 * Monthly/daily budget control for agent operations.
 *
 * @property monthlyBudget 月度预算 / Monthly budget
 * @property dailyBudget 日度预算（可选）/ Daily budget (optional)
 * @property currency 货币单位 / Currency
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

    // 告警阈值 / Alert thresholds
    private val warningThresholds = listOf(0.5f, 0.75f, 0.9f, 1.0f)

    /**
     * 检查是否可以执行操作 / Check if operation can be executed
     * @param agentName Agent 名称 / Agent name
     * @param estimatedCost 预估费用 / Estimated cost
     * @return 是否允许 / Whether allowed
     */
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

    /**
     * 记录费用支出 / Record cost spending
     * @param agentName Agent 名称 / Agent name
     * @param actualCost 实际费用 / Actual cost
     */
    fun recordSpending(agentName: String, actualCost: Double) {
        spendingByAgent.merge(agentName, actualCost, Double::plus)

        val current = _currentSpending.value
        val newMonthly = current.monthlySpending + actualCost
        val newDaily = current.dailySpending + actualCost

        _currentSpending.value = CostSpending(newMonthly, newDaily, currency)

        // Check alert thresholds
        checkAlerts(agentName, actualCost)
    }

    /**
     * 检查告警阈值 / Check alert thresholds
     */
    private fun checkAlerts(agentName: String, cost: Double) {
        val current = _currentSpending.value
        val monthlyPct = current.monthlySpending / monthlyBudget

        for (threshold in warningThresholds) {
            if (monthlyPct >= threshold) {
                val alert = CostAlert(
                    agentName = agentName,
                    threshold = threshold,
                    currentSpending = current.monthlySpending,
                    budget = monthlyBudget,
                    currency = currency,
                    timestamp = System.currentTimeMillis()
                )
                if (_alerts.value.none { it.threshold == threshold }) {
                    _alerts.value = _alerts.value + alert
                }
            }
        }
    }

    /**
     * 重置日度统计（应由调度器每日调用）/ Reset daily stats (should be called daily by scheduler)
     */
    fun resetDaily() {
        _currentSpending.value = CostSpending(_currentSpending.value.monthlySpending, 0.0, currency)
    }

    /**
     * 重置月度统计（应由调度器每月调用）/ Reset monthly stats (should be called monthly by scheduler)
     */
    fun resetMonthly() {
        _currentSpending.value = CostSpending(0.0, 0.0, currency)
        spendingByAgent.clear()
        _alerts.value = emptyList()
    }

    /**
     * 获取各 Agent 的费用分布 / Get cost distribution by agent
     */
    fun getSpendingByAgent(): Map<String, Double> = spendingByAgent.toMap()
}

/**
 * CostSpending — 当前费用支出状态
 * Current cost spending state.
 *
 * @property monthlySpending 月度已支出 / Monthly spent
 * @property dailySpending 日度已支出 / Daily spent
 * @property currency 货币单位 / Currency
 */
data class CostSpending(
    val monthlySpending: Double,
    val dailySpending: Double,
    val currency: String
)

/**
 * CostAlert — 费用告警
 * Cost alert notification.
 *
 * @property agentName Agent 名称 / Agent name
 * @property threshold 触发的阈值 / Triggered threshold
 * @property currentSpending 当前支出 / Current spending
 * @property budget 预算 / Budget
 * @property currency 货币单位 / Currency
 * @property timestamp 告警时间 / Alert timestamp
 */
data class CostAlert(
    val agentName: String,
    val threshold: Float,
    val currentSpending: Double,
    val budget: Double,
    val currency: String,
    val timestamp: Long
)

/**
 * CostCheckResult — 费用检查结果
 * Cost check result.
 */
sealed class CostCheckResult {
    object Allowed : CostCheckResult()
    data class Warning(val message: String) : CostCheckResult()
    data class Denied(val reason: String) : CostCheckResult()
}
