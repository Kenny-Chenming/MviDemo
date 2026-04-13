package com.mvi.kenny.feature.gemma4

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import kotlin.random.Random

/**
 * ============================================================
 * Gemma4ViewModel — Gemma 4 Agent Mode 工具包 ViewModel
 * ================================================================
 * MVI architecture ViewModel for Gemma 4 Agent Mode Toolkit.
 *
 * PRD-098: Gemma 4 × Android Studio Agent Mode 本地编码 Agent 工具链
 * Design Reference: memory/agency/designs/PRD-098-Gemma-4-Agent-Mode工具链.md
 *
 * Key behaviors:
 * 1. Manages Gemma 4 connection lifecycle (connect/disconnect)
 * 2. Simulates tool call visualization with real-time timeline updates
 * 3. Variant recommendation based on project profile analysis
 * 4. Quality benchmark execution with radar chart scoring
 * 5. Gemma ↔ Gemini API fallback strategy management
 * —————————————————————————————————————————————————————
 */
class Gemma4ViewModel : ViewModel() {

    // MVI State — single source of truth using StateFlow
    private val _state = MutableStateFlow(Gemma4State())
    val state: StateFlow<Gemma4State> = _state.asStateFlow()

    // MVI Effect — one-time side effects via Channel
    private val _effect = Channel<Gemma4Effect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    // Date formatter for timestamps
    private val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

    init {
        // Initialize with mock benchmark history / 初始化模拟基准测试历史
        loadMockBenchmarkHistory()
    }

    /**
     * ============================================================
     * sendIntent — 处理用户意图
     * ================================================================
     * Entry point for all user intents. Maps Intent → Business Logic.
     *
     * @param intent User intent from the UI layer
     */
    fun sendIntent(intent: Gemma4Intent) {
        when (intent) {
            is Gemma4Intent.ConnectAgent -> connectAgent()
            is Gemma4Intent.DisconnectAgent -> disconnectAgent()
            is Gemma4Intent.SelectVariant -> selectVariant(intent.variant)
            is Gemma4Intent.ToggleContextInjection -> toggleContext(intent.contextType, intent.enabled)
            is Gemma4Intent.AddCustomContextPath -> addCustomContext(intent.path)
            is Gemma4Intent.RemoveCustomContextPath -> removeCustomContext(intent.path)
            is Gemma4Intent.RunBenchmark -> runBenchmark(intent.task, intent.promptA, intent.promptB)
            is Gemma4Intent.LoadBenchmarkHistory -> loadBenchmarkHistory()
            is Gemma4Intent.ConfigureFallbackMode -> configureFallbackMode(intent.mode)
            is Gemma4Intent.UpdateFallbackTrigger -> updateFallbackTrigger(intent.trigger)
            is Gemma4Intent.SetRealTimeMode -> setRealTimeMode(intent.enabled)
            is Gemma4Intent.ClearToolCalls -> clearToolCalls()
            is Gemma4Intent.ToggleToolCallDetail -> toggleToolCallDetail(intent.callId)
            is Gemma4Intent.UpdateProjectProfile -> updateProjectProfile(intent.profile)
            is Gemma4Intent.RequestVariantRecommendation -> requestVariantRecommendation()
        }
    }

    // ================================================================
    // Connection Management / 连接管理
    // ================================================================

    /**
     * Connects to Gemma 4 Agent.
     * Simulates async connection with Loading state.
     */
    private fun connectAgent() {
        viewModelScope.launch {
            _state.value = _state.value.copy(connectionStatus = ConnectionStatus.Connecting)

            // Simulate connection delay / 模拟连接延迟
            delay(1500)

            // 80% success rate simulation / 模拟 80% 成功率
            if (Random.nextFloat() > 0.2f) {
                val sessionId = UUID.randomUUID().toString().take(8)
                val session = AgentSession(
                    sessionId = sessionId,
                    startTime = timeFormat.format(Date()),
                    totalTokenUsage = 0,
                    toolCallCount = 0,
                    successCount = 0,
                    failureCount = 0
                )
                _state.value = _state.value.copy(
                    connectionStatus = ConnectionStatus.Connected,
                    activeSession = session
                )
                _effect.send(Gemma4Effect.ConnectionSuccess)

                // Start simulating tool calls / 开始模拟工具调用
                startToolCallSimulation()
            } else {
                _state.value = _state.value.copy(connectionStatus = ConnectionStatus.Disconnected)
                _effect.send(Gemma4Effect.ConnectionFailed("无法连接到 Android Studio Agent，请确保 Gemma 4 插件已启用"))
            }
        }
    }

    /**
     * Disconnects from Gemma 4 Agent and clears session.
     */
    private fun disconnectAgent() {
        _state.value = _state.value.copy(
            connectionStatus = ConnectionStatus.Disconnected,
            activeSession = null,
            toolCalls = emptyList()
        )
    }

    /**
     * Selects a Gemma 4 variant.
     *
     * @param variant The variant to select
     */
    private fun selectVariant(variant: GemmaVariant) {
        _state.value = _state.value.copy(currentModel = variant)
    }

    // ================================================================
    // Tool Call Simulation / 工具调用模拟
    // ================================================================

    /**
     * Starts a simulation loop that generates tool calls in real-time.
     * Only runs when connectionStatus is Connected and isRealTimeMode is true.
     * 实时工具调用模拟——仅在连接状态且实时模式下运行
     */
    private fun startToolCallSimulation() {
        viewModelScope.launch {
            val toolTypes = listOf(
                ToolType.ReadFile to listOf(
                    "app/src/main/java/com/example/Main.kt" to "package com.example...",
                    "build.gradle.kts" to "plugins { kotlin(\"android\") }",
                    "settings.gradle.kts" to "rootProject.name = \"MyApp\""
                ),
                ToolType.EditFile to listOf(
                    "app/src/main/java/com/example/ViewModel.kt" to "+    val newState = ...",
                    "app/build.gradle.kts" to "+    implementation(\"androidx.compose:...\")"
                ),
                ToolType.BuildProject to listOf(
                    ":app:compileDebugKotlin" to "BUILD SUCCESSFUL",
                    ":app:assembleDebug" to "APK generated at app/build/outputs/"
                ),
                ToolType.SearchCode to listOf(
                    "grep -r \"ViewModel\" app/src/main/" to "Found 42 matches in 12 files",
                    "find . -name \"*.kt\" | wc -l" to "Total Kotlin files: 156"
                ),
                ToolType.RunTests to listOf(
                    "./gradlew test" to "Tests passed: 124, Failed: 0",
                    "./gradlew :app:testDebugUnitTest" to "All 89 tests passed"
                )
            )

            var callIndex = 0
            while (_state.value.connectionStatus == ConnectionStatus.Connected && _state.value.isRealTimeMode) {
                delay(Random.nextLong(800, 2500))  // Random interval between calls / 随机间隔

                val (toolType, examples) = toolTypes.random()
                val (path, preview) = examples.random()
                val callId = UUID.randomUUID().toString().take(8)
                val status = when {
                    Random.nextFloat() < 0.85f -> ToolCallStatus.Success
                    Random.nextFloat() < 0.95f -> ToolCallStatus.Failed
                    else -> ToolCallStatus.Running
                }

                val entry = ToolCallEntry(
                    id = callId,
                    timestamp = timeFormat.format(Date()),
                    toolType = toolType,
                    toolName = toolType.displayName,
                    params = "{ path: \"$path\" }",
                    result = if (status == ToolCallStatus.Success) preview else "Error: File not found",
                    status = status,
                    durationMs = Random.nextLong(50, 500)
                )

                val updatedCalls = (_state.value.toolCalls + entry).takeLast(50)  // Keep last 50 entries / 保留最近50条
                val updatedSession = _state.value.activeSession?.copy(
                    toolCallCount = updatedCalls.size,
                    successCount = updatedCalls.count { it.status == ToolCallStatus.Success },
                    failureCount = updatedCalls.count { it.status == ToolCallStatus.Failed },
                    totalTokenUsage = (_state.value.activeSession?.totalTokenUsage ?: 0) + Random.nextInt(50, 500)
                )

                _state.value = _state.value.copy(
                    toolCalls = updatedCalls,
                    activeSession = updatedSession
                )
                callIndex++
            }
        }
    }

    // ================================================================
    // Context Injection / 上下文注入
    // ================================================================

    /**
     * Toggles a context injection type on/off.
     */
    private fun toggleContext(contextType: ContextType, enabled: Boolean) {
        val updatedContexts = _state.value.contextConfig.enabledContexts.toMutableMap()
        updatedContexts[contextType] = enabled
        _state.value = _state.value.copy(
            contextConfig = _state.value.contextConfig.copy(enabledContexts = updatedContexts)
        )
    }

    /**
     * Adds a custom context file path.
     */
    private fun addCustomContext(path: String) {
        if (path.isNotBlank()) {
            val updated = _state.value.contextConfig.customContextPaths + path
            _state.value = _state.value.copy(
                contextConfig = _state.value.contextConfig.copy(customContextPaths = updated)
            )
        }
    }

    /**
     * Removes a custom context file path.
     */
    private fun removeCustomContext(path: String) {
        val updated = _state.value.contextConfig.customContextPaths.filter { it != path }
        _state.value = _state.value.copy(
            contextConfig = _state.value.contextConfig.copy(customContextPaths = updated)
        )
    }

    // ================================================================
    // Variant Advisor / 变体选型顾问
    // ================================================================

    /**
     * Updates the project profile used for variant recommendation.
     */
    private fun updateProjectProfile(profile: ProjectProfile) {
        _state.value = _state.value.copy(projectProfile = profile)
    }

    /**
     * Generates variant recommendations based on current project profile.
     * 分析项目特征并生成变体推荐
     */
    private fun requestVariantRecommendation() {
        viewModelScope.launch {
            _state.value = _state.value.copy(scanStatus = ScanStatus.Scanning)
            delay(1200)  // Simulate analysis time / 模拟分析耗时

            val profile = _state.value.projectProfile
            val recommendations = buildRecommendations(profile)

            _state.value = _state.value.copy(
                scanStatus = ScanStatus.Completed,
                recommendations = recommendations
            )
            _effect.send(Gemma4Effect.RecommendationReady(recommendations.size))
        }
    }

    /**
     * Builds variant recommendations based on project profile.
     */
    private fun buildRecommendations(profile: ProjectProfile): List<VariantRecommendation> {
        val baseReasons = mutableListOf<String>()

        // Analyze code size / 分析代码规模
        when {
            profile.codeLineRange.startsWith("1K") || profile.codeLineRange.startsWith("5K") -> {
                baseReasons.add("轻量级项目，小显存即可流畅运行")
            }
            profile.codeLineRange.startsWith("100K") || profile.codeLineRange.startsWith("500K") -> {
                baseReasons.add("大型项目，建议使用 7B 以上变体")
            }
            else -> baseReasons.add("中等规模项目，7B 变体性价比最优")
        }

        // Analyze memory / 分析内存配置
        if (profile.totalMemoryGB >= 32 && profile.hasDiscreteGPU) {
            baseReasons.add("高端硬件配置，可运行 9B 全性能版本")
        } else if (profile.totalMemoryGB <= 8) {
            baseReasons.add("内存有限，建议使用 2B 轻量版")
        }

        // Analyze KMP / 分析 KMP
        if (profile.hasKMP) {
            baseReasons.add("KMP 项目需要更强的上下文理解能力")
        }

        return listOf(
            VariantRecommendation(
                variant = GemmaVariant.Gemma2B,
                confidenceScore = if (!profile.hasDiscreteGPU && profile.totalMemoryGB < 16) 0.85f else 0.4f,
                reasons = listOf("低显存占用", "快速推理", "适合简单任务"),
                estimatedTime = "~2-5s/task"
            ),
            VariantRecommendation(
                variant = GemmaVariant.Gemma7B,
                confidenceScore = if (profile.totalMemoryGB in 16..32) 0.9f else 0.6f,
                reasons = baseReasons + listOf("平衡性能和资源消耗", "推荐作为默认选择"),
                estimatedTime = "~5-15s/task"
            ),
            VariantRecommendation(
                variant = GemmaVariant.Gemma9B,
                confidenceScore = if (profile.hasDiscreteGPU && profile.totalMemoryGB >= 32) 0.8f else 0.3f,
                reasons = listOf("最强推理能力", "最适合复杂项目分析", "需要高端 GPU"),
                estimatedTime = "~15-30s/task"
            )
        ).sortedByDescending { it.confidenceScore }
    }

    // ================================================================
    // Benchmark / 基准测试
    // ================================================================

    /**
     * Loads mock benchmark history for demonstration.
     */
    private fun loadMockBenchmarkHistory() {
        val mockResults = listOf(
            BenchmarkResult(
                id = "bm-001",
                taskDescription = "实现一个带分页的列表页面",
                promptA = "基础 Prompt: 实现分页列表",
                promptB = "增强 Prompt: 实现分页列表，包含 MVI 架构和双语注释",
                scoresA = listOf(
                    BenchmarkScore(BenchmarkDimension.SyntaxCorrectness, 70),
                    BenchmarkScore(BenchmarkDimension.LogicCompleteness, 55),
                    BenchmarkScore(BenchmarkDimension.CodeReadability, 60),
                    BenchmarkScore(BenchmarkDimension.KotlinIdioms, 50)
                ),
                scoresB = listOf(
                    BenchmarkScore(BenchmarkDimension.SyntaxCorrectness, 88),
                    BenchmarkScore(BenchmarkDimension.LogicCompleteness, 85),
                    BenchmarkScore(BenchmarkDimension.CodeReadability, 92),
                    BenchmarkScore(BenchmarkDimension.KotlinIdioms, 80)
                ),
                overallScoreA = 59,
                overallScoreB = 86,
                timestamp = "2026-04-12 14:30",
                winner = "B"
            ),
            BenchmarkResult(
                id = "bm-002",
                taskDescription = "编写 ViewModel 单元测试",
                promptA = "基础 Prompt: 编写 ViewModel 测试",
                promptB = "增强 Prompt: 编写 ViewModel 测试，使用 MockK，覆盖所有 Intent",
                scoresA = listOf(
                    BenchmarkScore(BenchmarkDimension.SyntaxCorrectness, 75),
                    BenchmarkScore(BenchmarkDimension.LogicCompleteness, 60),
                    BenchmarkScore(BenchmarkDimension.CodeReadability, 65),
                    BenchmarkScore(BenchmarkDimension.KotlinIdioms, 55)
                ),
                scoresB = listOf(
                    BenchmarkScore(BenchmarkDimension.SyntaxCorrectness, 90),
                    BenchmarkScore(BenchmarkDimension.LogicCompleteness, 88),
                    BenchmarkScore(BenchmarkDimension.CodeReadability, 85),
                    BenchmarkScore(BenchmarkDimension.KotlinIdioms, 82)
                ),
                overallScoreA = 63,
                overallScoreB = 86,
                timestamp = "2026-04-12 16:45",
                winner = "B"
            )
        )
        _state.value = _state.value.copy(benchmarkHistory = mockResults)
    }

    /**
     * Loads benchmark history.
     */
    private fun loadBenchmarkHistory() {
        viewModelScope.launch {
            delay(500)
            // History already loaded in init / 历史记录已在初始化时加载
            _effect.send(Gemma4Effect.ShowError("历史记录已加载"))
        }
    }

    /**
     * Runs a benchmark comparison between two prompts/configurations.
     */
    private fun runBenchmark(task: String, promptA: String, promptB: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(
                isRunningBenchmark = true,
                currentBenchmarkTask = task
            )

            // Simulate benchmark execution / 模拟基准测试执行
            delay(3000)

            val scoresA = BenchmarkDimension.entries.map { dim ->
                BenchmarkScore(dim, Random.nextInt(55, 90))
            }
            val scoresB = BenchmarkDimension.entries.map { dim ->
                BenchmarkScore(dim, Random.nextInt(60, 95))
            }

            val overallA = scoresA.sumOf { it.score } / scoresA.size
            val overallB = scoresB.sumOf { it.score } / scoresB.size

            val result = BenchmarkResult(
                id = "bm-${System.currentTimeMillis()}",
                taskDescription = task,
                promptA = promptA,
                promptB = promptB,
                scoresA = scoresA,
                scoresB = scoresB,
                overallScoreA = overallA,
                overallScoreB = overallB,
                timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date()),
                winner = when {
                    overallB > overallA -> "B"
                    overallA > overallB -> "A"
                    else -> "Tie"
                }
            )

            val updatedHistory = _state.value.benchmarkHistory + result
            _state.value = _state.value.copy(
                isRunningBenchmark = false,
                currentBenchmarkTask = "",
                benchmarkHistory = updatedHistory
            )
            _effect.send(Gemma4Effect.BenchmarkCompleted(result))
        }
    }

    // ================================================================
    // Fallback Strategy / 降级策略
    // ================================================================

    /**
     * Configures the fallback mode.
     */
    private fun configureFallbackMode(mode: FallbackMode) {
        _state.value = _state.value.copy(fallbackMode = mode)
    }

    /**
     * Updates fallback trigger thresholds.
     */
    private fun updateFallbackTrigger(trigger: FallbackTrigger) {
        _state.value = _state.value.copy(fallbackTrigger = trigger)
    }

    // ================================================================
    // Tool Call Visualization / 工具调用可视化
    // ================================================================

    /**
     * Sets real-time mode on/off.
     */
    private fun setRealTimeMode(enabled: Boolean) {
        _state.value = _state.value.copy(isRealTimeMode = enabled)
        if (enabled && _state.value.connectionStatus == ConnectionStatus.Connected) {
            startToolCallSimulation()
        }
    }

    /**
     * Clears all tool call history.
     */
    private fun clearToolCalls() {
        _state.value = _state.value.copy(
            toolCalls = emptyList(),
            activeSession = _state.value.activeSession?.copy(
                toolCallCount = 0,
                successCount = 0,
                failureCount = 0
            )
        )
    }

    /**
     * Toggles expanded detail view for a specific tool call.
     * (State managed locally in UI for now — ViewModel state can track if needed)
     */
    private fun toggleToolCallDetail(callId: String) {
        // For now, detail expansion is handled at UI level with remember.
        // This can be extended to track expandedCallId in Gemma4State if needed.
    }
}
