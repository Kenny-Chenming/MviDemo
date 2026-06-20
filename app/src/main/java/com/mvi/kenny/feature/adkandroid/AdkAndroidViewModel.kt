package com.mvi.kenny.feature.adkandroid

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

/**
 * ============================================================
 * AdkAndroidViewModel — Google ADK for Android 状态管理
 * ============================================================
 * PRD-292 | Google ADK for Android & Kotlin 开发工具包
 *
 * @see AdkAndroidContract
 * @see AdkAndroidState
 * @see AdkAndroidIntent
 * @see AdkAndroidEffect
 */
class AdkAndroidViewModel : ViewModel() {

    private val _state = MutableStateFlow(AdkAndroidState.Initial)
    val state: StateFlow<AdkAndroidState> = _state.asStateFlow()

    private val _effect = Channel<AdkAndroidEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    init {
        // 初始化学习中心功能卡片
        _state.value = _state.value.copy(
            featureCards = getFeatureCards(),
            codeSnippets = getCodeSnippets(),
            benchmarkScenarios = getBenchmarkScenarios()
        )
    }

    /**
     * 处理用户意图
     * —————————————————————————————————————————————————————
     * @param intent 用户意图
     */
    fun sendIntent(intent: AdkAndroidIntent) {
        when (intent) {
            is AdkAndroidIntent.SelectTab -> selectTab(intent.index)
            is AdkAndroidIntent.CheckEnvironment -> checkEnvironment()
            is AdkAndroidIntent.CopySnippet -> copySnippet(intent.snippetId)
            is AdkAndroidIntent.FilterByCategory -> filterByCategory(intent.category)
            is AdkAndroidIntent.StartBenchmark -> startBenchmark(intent.scenarioId)
        }
    }

    private fun selectTab(index: Int) {
        _state.value = _state.value.copy(selectedTab = index)
    }

    /**
     * 检测 ADK 环境
     * —————————————————————————————————————————————————————
     * 模拟检测 Gemini Nano / ML Kit GenAI / ADK 版本。
     * ADK Android 0.1.0 是首发版本，部分设备可能不支持。
     */
    private fun checkEnvironment() {
        viewModelScope.launch {
            _state.value = _state.value.copy(
                environment = _state.value.environment.copy(isChecking = true)
            )
            // 模拟环境检测延迟
            delay(1500)
            _state.value = _state.value.copy(
                environment = AdkEnvironment(
                    geminiNanoAvailable = true,
                    mlKitGenAiAvailable = true,
                    adkVersion = "0.1.0",
                    isChecking = false
                )
            )
            _effect.send(AdkAndroidEffect.ShowToast("环境检测完成"))
        }
    }

    /**
     * 复制代码片段
     * —————————————————————————————————————————————————————
     * @param snippetId 片段 ID
     */
    private fun copySnippet(snippetId: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(copiedSnippetId = snippetId)
            _effect.send(AdkAndroidEffect.ShowToast("代码已复制"))
            delay(1500)
            // 重置复制状态
            if (_state.value.copiedSnippetId == snippetId) {
                _state.value = _state.value.copy(copiedSnippetId = null)
            }
        }
    }

    /**
     * 按分类筛选代码片段
     * —————————————————————————————————————————————————————
     * @param category 分类，null 表示显示全部
     */
    private fun filterByCategory(category: SnippetCategory?) {
        _state.value = _state.value.copy(selectedCategory = category)
    }

    /**
     * 启动基准测试场景
     * —————————————————————————————————————————————————————
     * @param scenarioId 场景 ID
     */
    private fun startBenchmark(scenarioId: String) {
        viewModelScope.launch {
            val scenarios = _state.value.benchmarkScenarios.map { scenario ->
                if (scenario.id == scenarioId) {
                    // 模拟运行测试
                    delay(800)
                    scenario.copy(completed = true, lastResult = "Passed")
                } else scenario
            }
            _state.value = _state.value.copy(benchmarkScenarios = scenarios)
            _effect.send(AdkAndroidEffect.ShowToast("测试完成: Passed"))
        }
    }

    /**
     * 获取学习中心功能卡片列表
     * —————————————————————————————————————————————————————
     */
    private fun getFeatureCards(): List<AdkFeatureCard> = listOf(
        AdkFeatureCard("1", "On-Device Gemini Nano", "端侧推理，零 API 成本，数据不离设备", "🤖"),
        AdkFeatureCard("2", "Hybrid Orchestration", "云端协调 + 端侧执行，智能卸载", "☁️"),
        AdkFeatureCard("3", "Multi-Agent System", "LLM-based / Workflow / Custom 多形态", "👥"),
        AdkFeatureCard("4", "ML Kit GenAI 集成", "与 ML Kit 无缝集成，扩展 AI 能力", "🧩"),
        AdkFeatureCard("5", "Local Retrieval Agent", "本地文档解析，数据不离开设备", "📄"),
        AdkFeatureCard("6", "Flexible Tooling", "@Tool/@Param 注解定义自定义工具", "🔧"),
        AdkFeatureCard("7", "State Management", "完整的 Agent 状态管理", "💾"),
        AdkFeatureCard("8", "跨平台 Kotlin", "Backend / JVM / KMP 一套代码多端运行", "🎯"),
        AdkFeatureCard("9", "Session 管理", "多 Agent 之间共享 Session State", "🔗"),
        AdkFeatureCard("10", "CI/CD 集成", "On-Device Agent 行为测试策略", "🚀")
    )

    /**
     * 获取代码片段列表
     * —————————————————————————————————————————————————————
     */
    private fun getCodeSnippets(): List<AdkCodeSnippet> = listOf(
        // On-Device
        AdkCodeSnippet(
            id = "snip_001",
            title = "初始化 ADK Android Agent",
            description = "创建 On-Device Agent，配置 Gemini Nano",
            code = """val agent = AdkAndroidAgent(
    modelName = "gemini-2.0-flash-nano",
    apiKey = null  // On-Device, no API key needed
)
agent.start()""",
            category = SnippetCategory.ON_DEVICE
        ),
        AdkCodeSnippet(
            id = "snip_002",
            title = "定义自定义 Tool",
            description = "@Tool 注解暴露 Kotlin 函数给 LLM 调用",
            code = """@Tool
suspend fun getWeather(
    @Param("city name") city: String
): String {
    return weatherService.getCurrent(city)
}""",
            category = SnippetCategory.ON_DEVICE
        ),
        AdkCodeSnippet(
            id = "snip_003",
            title = "Local Retrieval Agent",
            description = "用 Gemini Nano 解析本地文档",
            code = """val localAgent = LocalRetrievalAgent(
    documentsDir = filesDir,
    embeddingModel = "semantic-retriever"
)
val answer = localAgent.query("项目架构是？")""",
            category = SnippetCategory.ON_DEVICE
        ),
        // Hybrid
        AdkCodeSnippet(
            id = "snip_004",
            title = "Hybrid Orchestration 配置",
            description = "云端协调器 + 端侧子 Agent 自动卸载",
            code = """val orchestrator = HybridOrchestrator(
    cloudModel = "gemini-2.5-pro",
    onDeviceModel = "gemini-2.0-flash-nano",
    offloadStrategy = OffloadStrategy.LATENCY_BASED
)""",
            category = SnippetCategory.HYBRID
        ),
        AdkCodeSnippet(
            id = "snip_005",
            title = "云端 → 端侧 Offload",
            description = "按任务复杂度自动分配到云端或端侧",
            code = """val result = orchestrator.run {
    if (task.complexity > COMPLEXITY_THRESHOLD) {
        cloudModel.complete(task)
    } else {
        onDeviceModel.complete(task)
    }
}""",
            category = SnippetCategory.HYBRID
        ),
        // Multi-Agent
        AdkCodeSnippet(
            id = "snip_006",
            title = "Sequential Sub-Agents",
            description = "定义顺序执行的子 Agent 链",
            code = """val workflow = SequentialAgents(
    agents = listOf(
        triageAgent,
        researchAgent,
        summaryAgent
    )
)
val result = workflow.run(userQuery)""",
            category = SnippetCategory.MULTI_AGENT
        ),
        AdkCodeSnippet(
            id = "snip_007",
            title = "多 Agent Session 共享",
            description = "子 Agent 之间共享上下文状态",
            code = """val session = AgentSession()
val agentA = AgentA(session)
val agentB = AgentB(session)
// A 和 B 共享 session 中的状态""",
            category = SnippetCategory.MULTI_AGENT
        ),
        // Testing
        AdkCodeSnippet(
            id = "snip_008",
            title = "On-Device Agent 单元测试",
            description = "模拟 Gemini Nano 的测试策略",
            code = """@Test
fun testAgentResponse() = runTest {
    val agent = createTestAgent(nanoModel = mockNano)
    agent.send("What is 2+2?")
    verify(mockNano).generate(promptEq("What is 2+2?"))
}""",
            category = SnippetCategory.TESTING
        ),
        AdkCodeSnippet(
            id = "snip_009",
            title = "Hybrid 编排集成测试",
            description = "测试云端和端侧的切换逻辑",
            code = """@Test
fun testOffloadDecision() = runTest {
    val orch = HybridOrchestrator(mockCloud, mockNano)
    // 低复杂度 → 应走端侧
    val r1 = orch.complete(simpleTask)
    verify(mockNano).complete(simpleTask)
}""",
            category = SnippetCategory.TESTING
        )
    )

    /**
     * 获取基准测试场景列表
     * —————————————————————————————————————————————————————
     */
    private fun getBenchmarkScenarios(): List<BenchmarkScenario> = listOf(
        BenchmarkScenario(
            id = "bench_001",
            title = "On-Device 响应测试",
            description = "测试 Gemini Nano 端侧推理延迟"
        ),
        BenchmarkScenario(
            id = "bench_002",
            title = "Hybrid 编排测试",
            description = "测试云端/端侧任务卸载决策"
        ),
        BenchmarkScenario(
            id = "bench_003",
            title = "多 Agent 协作测试",
            description = "测试 Sequential Sub-Agents 执行链"
        ),
        BenchmarkScenario(
            id = "bench_004",
            title = "Tool 调用测试",
            description = "测试 @Tool 注解的函数调用"
        ),
        BenchmarkScenario(
            id = "bench_005",
            title = "Session 状态测试",
            description = "测试多 Agent 共享 Session"
        )
    )
}
