package com.mvi.kenny.feature.androidfragment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.random.Random

/**
 * ================================================================
 * AndroidFragmentViewModel — AndroidFragment Composable 开发工具包状态管理
 * ================================================================
 * PRD-123 | AndroidFragment Composable 开发工具包
 *
 * 职责：
 * 1. 管理页面状态（State）
 * 2. 处理用户意图（Intent）
 * 3. 触发一次性副作用（Effect）
 *
 * 架构：MVI（Model-View-Intent）
 *
 * @see AndroidFragmentState 页面状态定义
 * @see AndroidFragmentEffect 副作用定义
 */
class AndroidFragmentViewModel : ViewModel() {

    // ----- State -----
    private val _state = MutableStateFlow(AndroidFragmentState.Initial)
    val state: StateFlow<AndroidFragmentState> = _state.asStateFlow()

    // ----- Effect Channel -----
    private val _effect = Channel<AndroidFragmentEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    init {
        // 初始化时计算健康度 / Calculate health score on init
        refreshHealthScore()
        // 初始化扫描结果 / Initialize scan results
        loadInitialScanResults()
    }

    // ================================================================
    // sendIntent — 接收 Intent 并处理 / Receive Intent and process
    // ================================================================
    fun sendIntent(intent: AndroidFragmentIntent) {
        when (intent) {
            is AndroidFragmentIntent.SelectTab -> handleSelectTab(intent.index)
            is AndroidFragmentIntent.StartScan -> handleStartScan()
            is AndroidFragmentIntent.CancelScan -> handleCancelScan()
            is AndroidFragmentIntent.FilterBySeverity -> handleFilterBySeverity(intent.severity)
            is AndroidFragmentIntent.StartLiveMonitoring -> handleStartLiveMonitoring()
            is AndroidFragmentIntent.StopLiveMonitoring -> handleStopLiveMonitoring()
            is AndroidFragmentIntent.AnswerDecisionTree -> handleAnswerDecisionTree(intent.questionIndex, intent.answer)
            is AndroidFragmentIntent.SetTransitionType -> handleSetTransitionType(intent.type)
            is AndroidFragmentIntent.AnalyzeNavGraph -> handleAnalyzeNavGraph()
            is AndroidFragmentIntent.RefreshHealthScore -> refreshHealthScore()
            is AndroidFragmentIntent.ClearError -> _state.update { it.copy(errorMessage = null) }
        }
    }

    // ================================================================
    // Tab selection / Tab 选择
    // ================================================================
    private fun handleSelectTab(index: Int) {
        _state.update { it.copy(currentTab = index) }
    }

    // ================================================================
    // Scan logic / 扫描逻辑
    // ================================================================
    private fun handleStartScan() {
        viewModelScope.launch {
            _state.update { it.copy(isScanning = true, scanProgress = 0f, scanResults = emptyList()) }

            // Simulate scan progress / 模拟扫描进度
            // In production, this would scan actual project files
            val filesToScan = listOf(
                "MainActivity.kt", "HomeFragment.kt", "DetailFragment.kt",
                "SettingsFragment.kt", "ProfileFragment.kt", "ListFragment.kt"
            )

            val results = mutableListOf<FragmentScanResult>()

            filesToScan.forEachIndexed { index, fileName ->
                delay(300) // Simulate file scanning
                _state.update { it.copy(scanProgress = (index + 1).toFloat() / filesToScan.size) }

                // Generate realistic scan results based on file name
                val result = generateScanResult(fileName)
                results.add(result)
            }

            _state.update {
                it.copy(
                    isScanning = false,
                    scanProgress = 1f,
                    scanResults = results
                )
            }
        }
    }

    /**
     * 生成模拟扫描结果
     * Generate mock scan results based on file characteristics
     */
    private fun generateScanResult(fileName: String): FragmentScanResult {
        val id = "scan-${fileName.hashCode()}"
        val isFragment = fileName.endsWith("Fragment.kt")

        return when {
            isFragment -> FragmentScanResult(
                id = id,
                filePath = "app/src/main/java/com/mvi/kenny/$fileName",
                fragmentType = FragmentType.FRAGMENT_CONTAINER_VIEW,
                severity = ScanSeverity.P0,
                description = "发现 Fragment 使用 FragmentContainerView，强烈建议迁移到 AndroidFragment",
                suggestedAction = "将 <androidx.fragment.app.FragmentContainerView> 替换为 AndroidFragment { fragmentClass = ${fileName.replace(".kt", "::class.java")} }",
                canMigrate = true
            )
            else -> FragmentScanResult(
                id = id,
                filePath = "app/src/main/java/com/mvi/kenny/$fileName",
                fragmentType = FragmentType.ANDROID_VIEW_BINDING,
                severity = ScanSeverity.P1,
                description = "发现 AndroidViewBinding 用法，可考虑迁移到 AndroidFragment 获得更好生命周期同步",
                suggestedAction = "评估后决定是否迁移，如需保留 AndroidView 保留当前写法",
                canMigrate = true
            )
        }
    }

    private fun handleCancelScan() {
        _state.update { it.copy(isScanning = false, scanProgress = 0f) }
    }

    private fun handleFilterBySeverity(severity: ScanSeverity?) {
        _state.update { it.copy(selectedSeverity = severity) }
    }

    // ================================================================
    // Live monitoring / 实时监控
    // ================================================================
    private fun handleStartLiveMonitoring() {
        _state.update { it.copy(isLiveMonitoring = true) }
        viewModelScope.launch {
            var eventIndex = 0
            val states = FragmentLifecycleState.entries.toTypedArray()
            while (_state.value.isLiveMonitoring) {
                delay(1500) // Emit lifecycle event every 1.5s
                val newState = states[eventIndex % states.size]
                val isSynced = Random.nextFloat() > 0.1f // 90% sync rate
                val event = LifecycleEvent(
                    timestamp = System.currentTimeMillis(),
                    state = newState,
                    isSynced = isSynced,
                    description = getLifecycleDescription(newState)
                )
                _state.update { current ->
                    val events = (current.lifecycleEvents + event).takeLast(20)
                    val alerts = if (!isSynced) {
                        current.alertMessages + AlertMessage(
                            id = "alert-${System.currentTimeMillis()}",
                            severity = AlertSeverity.WARNING,
                            title = "生命周期不同步",
                            message = "Fragment 进入 $newState 但 Compose 重组未同步"
                        )
                    } else current.alertMessages
                    current.copy(lifecycleEvents = events, alertMessages = alerts)
                }
                eventIndex++
            }
        }
    }

    private fun handleStopLiveMonitoring() {
        _state.update { it.copy(isLiveMonitoring = false) }
    }

    /** 获取生命周期状态描述 / Get lifecycle state description */
    private fun getLifecycleDescription(state: FragmentLifecycleState): String {
        return when (state) {
            FragmentLifecycleState.CREATED -> "Fragment 已创建，View 已绑定"
            FragmentLifecycleState.STARTED -> "Fragment 已启动"
            FragmentLifecycleState.RESUMED -> "Fragment 处于前台，可交互"
            FragmentLifecycleState.PAUSED -> "Fragment 处于暂停状态"
            FragmentLifecycleState.STOPPED -> "Fragment 已停止"
            FragmentLifecycleState.DESTROYED -> "Fragment 已销毁"
        }
    }

    // ================================================================
    // Decision tree / 决策树
    // ================================================================
    private fun handleAnswerDecisionTree(questionIndex: Int, answer: Boolean) {
        val newAnswers = _state.value.decisionTreeAnswers + (questionIndex to answer)
        _state.update { it.copy(decisionTreeAnswers = newAnswers) }

        // 当回答完 3 个问题后，计算推荐场景
        // Calculate recommended scenario after 3 questions answered
        if (newAnswers.size >= 3) {
            val scenario = calculateRecommendedScenario(newAnswers)
            _state.update { it.copy(selectedScenario = scenario) }
        }
    }

    /**
     * 根据决策树答案计算推荐场景
     * Calculate recommended scenario based on decision tree answers
     */
    private fun calculateRecommendedScenario(answers: Map<Int, Boolean>): Scenario {
        val hasFragmentContainerView = answers[0] ?: false
        val needsToKeepFragment = answers[1] ?: false
        val usesFragmentResult = answers[2] ?: false

        return when {
            needsToKeepFragment && usesFragmentResult -> Scenario(
                type = ScenarioType.ANDROID_FRAGMENT,
                title = "推荐：AndroidFragment",
                description = "需要保留 Fragment 且使用 Result API，AndroidFragment 是最佳选择",
                reason = "AndroidFragment 提供官方生命周期同步和 Result API 支持"
            )
            needsToKeepFragment -> Scenario(
                type = ScenarioType.ANDROID_FRAGMENT,
                title = "推荐：AndroidFragment",
                description = "需要保留 Fragment，AndroidFragment 提供最佳 Compose 集成",
                reason = "相比 AndroidView，AndroidFragment 提供更好的生命周期管理和 Compose 状态同步"
            )
            hasFragmentContainerView -> Scenario(
                type = ScenarioType.FULL_COMPOSE_MIGRATION,
                title = "推荐：全量迁移 Compose",
                description = "不需要保留 Fragment，建议直接迁移到原生 Compose",
                reason = "全量 Compose 可消除 Fragment/Compose 互操作复杂性"
            )
            else -> Scenario(
                type = ScenarioType.ANDROID_VIEW,
                title = "推荐：继续使用 AndroidView",
                description = "当前无 Fragment 依赖，可继续使用 AndroidView",
                reason = "AndroidView 仍是稳定方案，AndroidFragment 仅为 alpha 版本"
            )
        }
    }

    // ================================================================
    // Animation config / 动画配置
    // ================================================================
    private fun handleSetTransitionType(type: TransitionType) {
        _state.update { it.copy(transitionType = type) }
    }

    // ================================================================
    // Nav graph analysis / 导航图分析
    // ================================================================
    private fun handleAnalyzeNavGraph() {
        viewModelScope.launch {
            delay(500) // Simulate analysis description
            val analysis = NavGraphAnalysis(
                totalFragments = 6,
                migratedFragments = 0,
                suggestions = listOf(
                    "NavHostFragment 建议替换为 AndroidFragment { navHost = true }",
                    "所有 Fragment 目的地可迁移到 AndroidFragment 以获得 Compose 生命周期同步",
                    "NavigationComponent 3.x 原生支持 AndroidFragment",
                    "迁移前请确保所有 Fragment 使用 viewLifecycleOwner 进行 Compose 状态同步"
                )
            )
            _state.update { it.copy(navGraphAnalysis = analysis) }
        }
    }

    // ================================================================
    // Health score / 健康度
    // ================================================================
    private fun refreshHealthScore() {
        viewModelScope.launch {
            val dimensions = HealthDimension.entries.associateWith { dim ->
                when (dim) {
                    HealthDimension.MIGRATION_COVERAGE -> Random.nextFloat() * 40 + 50 // 50-90
                    HealthDimension.LIFECYCLE_SYNC -> Random.nextFloat() * 30 + 60 // 60-90
                    HealthDimension.RESULT_API_COMPLIANCE -> Random.nextFloat() * 50 + 40 // 40-90
                    HealthDimension.ANIMATION_CONFIG -> Random.nextFloat() * 40 + 50 // 50-90
                    HealthDimension.NAVIGATION_FUSION -> Random.nextFloat() * 30 + 50 // 50-80
                    HealthDimension.COMPONENT_MODERNIZATION -> Random.nextFloat() * 40 + 50 // 50-90
                }
            }

            val weightedScore = dimensions.entries.sumOf { (dim, score) ->
                (dim.weight * score * 100).toInt()
            } / 100

            _state.update {
                it.copy(
                    healthScore = weightedScore,
                    healthDimensions = dimensions
                )
            }
        }
    }

    // ================================================================
    // Initial scan results / 初始扫描结果
    // ================================================================
    private fun loadInitialScanResults() {
        // Load demo scan results for initial display
        val demoResults = listOf(
            FragmentScanResult(
                id = "demo-1",
                filePath = "app/src/main/java/com/mvi/kenny/feature/home/HomeFragment.kt",
                fragmentType = FragmentType.FRAGMENT_CONTAINER_VIEW,
                severity = ScanSeverity.P0,
                description = "发现 FragmentContainerView 使用，建议迁移到 AndroidFragment",
                suggestedAction = "AndroidFragment { fragmentClass = HomeFragment::class.java }",
                canMigrate = true
            ),
            FragmentScanResult(
                id = "demo-2",
                filePath = "app/src/main/java/com/mvi/kenny/feature/detail/DetailFragment.kt",
                fragmentType = FragmentType.ANDROID_VIEW_BINDING,
                severity = ScanSeverity.P1,
                description = "发现 AndroidViewBinding 用法，可考虑迁移",
                suggestedAction = "评估后决定，AndroidFragment 提供更好生命周期同步",
                canMigrate = true
            )
        )
        _state.update { it.copy(scanResults = demoResults) }
    }
}
