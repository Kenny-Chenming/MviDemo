package com.mvi.kenny.feature.adkandroid

/**
 * ============================================================
 * AdkAndroidContract — Google ADK for Android MVI 契约
 * ============================================================
 * PRD-292 | Google ADK for Android & Kotlin 开发工具包
 * Design: 工具型 App，4 Tab 结构
 *
 * MVI 三要素：
 * - Model（State）：页面状态，唯一真相来源
 * - Intent：用户意图
 * - Effect：副作用（Toast、导航）
 *
 * @see AdkAndroidViewModel
 * @see AdkAndroidScreen
 */

/** ADK 环境检测结果 */
data class AdkEnvironment(
    val geminiNanoAvailable: Boolean? = null,
    val mlKitGenAiAvailable: Boolean? = null,
    val adkVersion: String? = null,
    val isChecking: Boolean = false
)

/** 代码片段数据模型 */
data class AdkCodeSnippet(
    val id: String,
    val title: String,
    val description: String,
    val code: String,
    val category: SnippetCategory,
    val adkVersionRange: String = "0.1.0+"
)

/** 代码片段分类 */
enum class SnippetCategory(val label: String) {
    ON_DEVICE("On-Device"),
    HYBRID("Hybrid"),
    MULTI_AGENT("Multi-Agent"),
    TESTING("Testing")
}

/** 基准测试场景 */
data class BenchmarkScenario(
    val id: String,
    val title: String,
    val description: String,
    val completed: Boolean = false,
    val lastResult: String? = null
)

/** 学习中心功能卡片 */
data class AdkFeatureCard(
    val id: String,
    val title: String,
    val description: String,
    val icon: String
)

/**
 * 页面状态（State）
 * —————————————————————————————————————————————————————
 * @param selectedTab 当前选中的 Tab (0-3)
 * @param environment ADK 环境检测结果
 * @param codeSnippets 代码片段列表
 * @param selectedCategory 代码片段筛选分类
 * @param benchmarkScenarios 基准测试场景列表
 * @param isLoading 整体加载状态
 */
data class AdkAndroidState(
    val selectedTab: Int = 0,
    val environment: AdkEnvironment = AdkEnvironment(),
    val codeSnippets: List<AdkCodeSnippet> = emptyList(),
    val selectedCategory: SnippetCategory? = null,
    val benchmarkScenarios: List<BenchmarkScenario> = emptyList(),
    val featureCards: List<AdkFeatureCard> = emptyList(),
    val copiedSnippetId: String? = null,
    val isLoading: Boolean = false
) {
    companion object {
        val Initial = AdkAndroidState()
    }
}

/**
 * 用户意图（Intent）
 * —————————————————————————————————————————————————————
 * @see AdkAndroidViewModel.sendIntent
 */
sealed interface AdkAndroidIntent {
    data class SelectTab(val index: Int) : AdkAndroidIntent
    data object CheckEnvironment : AdkAndroidIntent
    data class CopySnippet(val snippetId: String) : AdkAndroidIntent
    data class FilterByCategory(val category: SnippetCategory?) : AdkAndroidIntent
    data class StartBenchmark(val scenarioId: String) : AdkAndroidIntent
}

/**
 * 副作用（Effect）
 * —————————————————————————————————————————————————————
 * @see AdkAndroidViewModel.effect
 */
sealed interface AdkAndroidEffect {
    data class ShowToast(val message: String) : AdkAndroidEffect
    data class NavigateToLab(val tabIndex: Int = 1) : AdkAndroidEffect
}
