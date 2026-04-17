package com.mvi.kenny.feature.messagequeue

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import java.util.UUID
import kotlin.random.Random

/**
 * ViewModel for Android 17 Lock-Free MessageQueue Reflection Toolkit
 * Android 17 无锁 MessageQueue 反射破坏性变更检测与迁移工具包的 ViewModel
 *
 * Design: Implements MVI pattern — State is the single source of truth,
 * Intent represents user actions, Effect represents one-time side effects.
 * 设计: MVI 架构 — State 是唯一数据源, Intent 表示用户动作, Effect 表示一次性副作用
 *
 * The toolkit detects reflection-based access to MessageQueue private fields
 * which breaks in Android 17's lock-free MessageQueue implementation.
 * 本工具检测对 MessageQueue 私有字段的反射访问，这些在 Android 17
 * 的无锁 MessageQueue 实现中会破坏。
 */
class MessageQueueViewModel : ViewModel() {

    private val _state = MutableStateFlow(MessageQueueState())
    val state: StateFlow<MessageQueueState> = _state.asStateFlow()

    private val _effect = Channel<MessageQueueEffect>(Channel.BUFFERED)
    val effect: Flow<MessageQueueEffect> = _effect.receiveAsFlow()

    // Simulated reflection access points — In production, use KSP/APT to parse AST
    // 模拟的反射访问点 — 实际使用时需要用 KSP/APT 解析 AST
    // 这些示例反映真实 Android 项目中常见的问题模式
    // These reflect real patterns found in Android projects
    private val simulatedScanResults = listOf(
        // CRASH severity — Field.set() modifying MessageQueue state
        // CRASH 严重级别 — Field.set() 修改 MessageQueue 状态
        ReflectionAccess(
            filePath = "app/src/main/java/com/example/looperdebug/DebugLooper.kt",
            lineNumber = 47,
            codeSnippet = "mQueueField.setAccessible(true); mQueueField.set(looper, null);",
            accessedField = MQField.M_QUEUE,
            severity = Severity.CRASH,
            reflectionType = ReflectionType.FIELD_SET,
            contextSnippet = "private void clearMessageQueue(Looper looper) {\n    Field mQueueField = Looper.class.getDeclaredField(\"mQueue\");\n    mQueueField.setAccessible(true); // Lock-free breaking change!\n    mQueueField.set(looper, null);\n}"
        ),
        ReflectionAccess(
            filePath = "app/src/main/java/com/example/plugin/HookMessageQueue.kt",
            lineNumber = 112,
            codeSnippet = "mMessagesField.set(myQueue, newMsg);",
            accessedField = MQField.M_MESSAGES,
            severity = Severity.CRASH,
            reflectionType = ReflectionType.FIELD_SET,
            contextSnippet = "// Hot-fix framework injecting messages into queue\nField mMessagesField = MessageQueue.class.getDeclaredField(\"mMessages\");\nmMessagesField.setAccessible(true);\nmMessagesField.set(myQueue, newMsg); // CRASH on Android 17!"
        ),
        // BEHAVIOR_ANOMALY severity — Field.get() with ART optimization issues
        // BEHAVIOR_ANOMALY 严重级别 — Field.get() 配合 ART 优化问题
        ReflectionAccess(
            filePath = "app/src/main/java/com/example/monitor/QueueMonitor.kt",
            lineNumber = 23,
            codeSnippet = "Message firstMsg = (Message) mMessagesField.get(queue);",
            accessedField = MQField.M_MESSAGES,
            severity = Severity.BEHAVIOR_ANOMALY,
            reflectionType = ReflectionType.FIELD_GET,
            contextSnippet = "// Monitoring framework reading queue state\nField mMessagesField = MessageQueue.class.getDeclaredField(\"mMessages\");\nmMessagesField.setAccessible(true);\nMessage firstMsg = (Message) mMessagesField.get(queue); // Value may differ after ART inlining"
        ),
        ReflectionAccess(
            filePath = "app/src/main/java/com/example/test/QueueInspector.kt",
            lineNumber = 88,
            codeSnippet = "boolean quitting = mQuittingField.getBoolean(queue);",
            accessedField = MQField.M_QUITING,
            severity = Severity.BEHAVIOR_ANOMALY,
            reflectionType = ReflectionType.FIELD_GET,
            contextSnippet = "// Test framework inspecting queue state\nField mQuittingField = MessageQueue.class.getDeclaredField(\"mQuitting\");\nmQuittingField.setAccessible(true);\nboolean quitting = mQuittingField.getBoolean(queue); // ART may inline and optimize this away"
        ),
        ReflectionAccess(
            filePath = "app/src/main/java/com/example/debug/IdleHandlerDebug.kt",
            lineNumber = 55,
            codeSnippet = "mIdleHandlersField.get(queue);",
            accessedField = MQField.M_IDLE_HANDLERS,
            severity = Severity.BEHAVIOR_ANOMALY,
            reflectionType = ReflectionType.FIELD_GET,
            contextSnippet = "// Debug tool reading idle handlers\nField mIdleHandlersField = MessageQueue.class.getDeclaredField(\"mIdleHandlers\");\nmIdleHandlersField.setAccessible(true);\nObject handlers = mIdleHandlersField.get(queue); // Behavior may change"
        ),
        // SAFE severity — Reading immutable fields that don't change
        // SAFE 严重级别 — 读取不变字段
        ReflectionAccess(
            filePath = "app/src/main/java/com/example/utils/QueueUtils.kt",
            lineNumber = 15,
            codeSnippet = "mLlocksField.get(queue); // Immutable lock list",
            accessedField = MQField.M_LLOCKS,
            severity = Severity.SAFE,
            reflectionType = ReflectionType.FIELD_GET,
            contextSnippet = "// Reading lock objects (immutable after init)\nField mLlocksField = MessageQueue.class.getDeclaredField(\"mLlocks\");\nmLlocksField.setAccessible(true);\n// mLlocks is initialized once and never changed — SAFE in lock-free impl"
        )
    )

    // Knowledge base: reflection pattern → alternative API mapping
    // 知识库: 反射模式 → 替代 API 映射
    private val simulatedKnowledgeBase = listOf(
        AlternativeSolution(
            id = "sol-001",
            targetPattern = "Field.setAccessible + Field.set(mQueue, ...)",
            alternativeApi = "Looper.getMainLooper().queue",
            useCase = "Clearing or replacing the message queue / 清除或替换消息队列",
            exampleCode = "// Instead of: mQueueField.set(looper, newQueue)\n// Use: Create new Looper with new MessageQueue\nval newLooper = Looper.prepareMainLooper()\n// Or use: Looper.getMainLooper().quit() to drain queue",
            riskLevel = Severity.BEHAVIOR_ANOMALY,
            migrationSteps = listOf(
                "Identify why you need to replace the queue / 确认为何需要替换队列",
                "Use Looper.quit() to safely drain messages / 使用 Looper.quit() 安全清空消息",
                "If you need a custom queue, create it at Looper.prepare() time / 如需自定义队列，在 Looper.prepare() 时创建"
            )
        ),
        AlternativeSolution(
            id = "sol-002",
            targetPattern = "Field.get(mMessages)",
            alternativeApi = "Handler.getCallback() or Choreographer",
            useCase = "Monitoring next message in queue / 监控队列中的下一条消息",
            exampleCode = "// Instead of reading mMessages directly\n// Use: Handler with callback to observe message dispatch\nval handler = Handler(Looper.getMainLooper()) { msg ->\n    // Observe each message before it is handled\n    true // return true to consume, false to continue\n}",
            riskLevel = Severity.SAFE,
            migrationSteps = listOf(
                "Replace direct queue reading with Handler callback / 用 Handler 回调替代直接队列读取",
                "Use Choreographer.postFrameCallback() for timing-aware observation / 使用 Choreographer.postFrameCallback() 进行时间感知观察",
                "Test message observation still works after migration / 测试迁移后消息观察是否正常"
            )
        ),
        AlternativeSolution(
            id = "sol-003",
            targetPattern = "Field.get(mQuitting)",
            alternativeApi = "Looper.getMainLooper().quit() + state tracking",
            useCase = "Checking if looper is quitting / 检查 looper 是否正在退出",
            exampleCode = "// Instead of: mQuittingField.getBoolean(queue)\n// Use: Maintain your own quit state flag\nvar isQuitting = false\nhandler.post {\n    // Check your own flag instead of internal state\n}",
            riskLevel = Severity.SAFE,
            migrationSteps = listOf(
                "Introduce your own AtomicBoolean quit flag / 引入你自己的 AtomicBoolean 退出标志",
                "Replace mQuitting read with your own flag check / 用你自己的标志检查替换 mQuitting 读取",
                "Ensure flag is set before calling Looper.quit() / 确保在调用 Looper.quit() 前设置标志"
            )
        ),
        AlternativeSolution(
            id = "sol-004",
            targetPattern = "Field.get(mIdleHandlers)",
            alternativeApi = "HandlerThread.idleHandler or custom IdlePolicy",
            useCase = "Registering idle-time callbacks / 注册空闲时回调",
            exampleCode = "// Instead of: mIdleHandlersField.get(queue)\n// Use: Looper.myQueue().addIdleHandler()\nLooper.myQueue().addIdleHandler {\n    // This runs when the queue is idle\n    true // return true to stay registered, false to remove\n}",
            riskLevel = Severity.SAFE,
            migrationSteps = listOf(
                "Use Looper.myQueue().addIdleHandler() / 使用 Looper.myQueue().addIdleHandler()",
                "Migrate any custom idle handler logic / 迁移自定义空闲处理器逻辑",
                "Test idle callbacks fire correctly / 测试空闲回调是否正常触发"
            )
        ),
        AlternativeSolution(
            id = "sol-005",
            targetPattern = "Field.setAccessible + MessageQueue injection",
            alternativeApi = "Handler.sendMessage() + MessageQueue.next() observation",
            useCase = "Injecting messages into queue head / 向队列头部注入消息",
            exampleCode = "// Instead of queue injection via reflection\n// Use: Handler.sendMessageAtFrontOfQueue()\nhandler.sendMessageAtFrontOfQueue(msg)\n// Or use custom MessageQueue.next() override (if you control the queue)",
            riskLevel = Severity.BEHAVIOR_ANOMALY,
            migrationSteps = listOf(
                "Use sendMessageAtFrontOfQueue() for head insertion / 使用 sendMessageAtFrontOfQueue() 插入队首",
                "If you need precise ordering, consider using Handler with sorted priority / 如需精确排序，考虑使用带优先级的 Handler",
                "Test message ordering after migration / 测试迁移后消息排序是否正确"
            )
        )
    )

    /**
     * Process user intents / 处理用户意图
     * This is the main entry point for all user actions in the MVI pattern.
     * 这是 MVI 模式中所有用户动作的主入口点。
     */
    fun processIntent(intent: MessageQueueIntent) {
        when (intent) {
            is MessageQueueIntent.SelectTab -> selectTab(intent.tab)
            is MessageQueueIntent.StartScan -> startScan()
            is MessageQueueIntent.CancelScan -> cancelScan()
            is MessageQueueIntent.FilterBySeverity -> filterBySeverity(intent.filter)
            is MessageQueueIntent.SelectAccess -> selectAccess(intent.access)
            is MessageQueueIntent.DismissDetail -> dismissDetail()
            is MessageQueueIntent.SearchKnowledgeBase -> searchKnowledgeBase(intent.query)
            is MessageQueueIntent.StartMigration -> startMigration(intent.step)
            is MessageQueueIntent.PreviewMigration -> previewMigration(intent.step)
            is MessageQueueIntent.DismissMigrationPreview -> dismissMigrationPreview()
            is MessageQueueIntent.GenerateAllTests -> generateAllTests()
            is MessageQueueIntent.RunTest -> runTest(intent.test)
            is MessageQueueIntent.RunAllTests -> runAllTests()
            is MessageQueueIntent.UpdateSettings -> updateSettings(intent.settings)
            is MessageQueueIntent.ExportReport -> exportReport(intent.format)
            is MessageQueueIntent.DismissError -> dismissError()
        }
    }

    // ===== Tab Navigation / 标签页导航 =====

    private fun selectTab(tab: MessageQueueTab) {
        _state.update { it.copy(selectedTab = tab) }
    }

    // ===== Scan / 扫描 =====

    private fun startScan() {
        viewModelScope.launch {
            _state.update {
                it.copy(
                    scanStatus = ScanStatus.SCANNING,
                    scanProgress = 0f,
                    scanPhases = ScanPhase.entries,
                    currentPhase = ScanPhase.PARSING_FILES,
                    scannedFilesCount = 0,
                    totalFilesCount = 42,
                    scanResults = emptyList(),
                    impactSummary = ImpactSummary()
                )
            }

            // Phase 1: Parsing files / 阶段 1: 解析文件
            simulatePhasedProgress(ScanPhase.PARSING_FILES) { progress ->
                _state.update { it.copy(scanProgress = progress * 0.25f, scannedFilesCount = (progress * 42).toInt()) }
            }

            // Phase 2: Detecting reflection / 阶段 2: 检测反射
            simulatePhasedProgress(ScanPhase.DETECTING_REFLECTION) { progress ->
                _state.update { it.copy(scanProgress = 0.25f + progress * 0.25f) }
            }

            // Phase 3: Checking MessageQueue access / 阶段 3: 检查 MessageQueue 访问
            simulatePhasedProgress(ScanPhase.CHECKING_MESSAGE_QUEUE_ACCESS) { progress ->
                _state.update { it.copy(scanProgress = 0.5f + progress * 0.25f) }
            }

            // Phase 4: Analyzing severity / 阶段 4: 分析严重级别
            simulatePhasedProgress(ScanPhase.ANALYZING_SEVERITY) { progress ->
                _state.update { it.copy(scanProgress = 0.75f + progress * 0.15f) }
            }

            // Phase 5: Generating report / 阶段 5: 生成报告
            simulatePhasedProgress(ScanPhase.GENERATING_REPORT) { progress ->
                _state.update { it.copy(scanProgress = 0.9f + progress * 0.1f) }
            }

            // Compute impact summary / 计算影响摘要
            val results = simulatedScanResults
            val crashCount = results.count { it.severity == Severity.CRASH }
            val anomalyCount = results.count { it.severity == Severity.BEHAVIOR_ANOMALY }
            val safeCount = results.count { it.severity == Severity.SAFE }
            val impactSummary = ImpactSummary(
                totalAccessPoints = results.size,
                crashCount = crashCount,
                anomalyCount = anomalyCount,
                safeCount = safeCount,
                topRiskPaths = results.sortedByDescending {
                    when (it.severity) {
                        Severity.CRASH -> 2
                        Severity.BEHAVIOR_ANOMALY -> 1
                        Severity.SAFE -> 0
                    }
                }.take(5)
            )

            _state.update {
                it.copy(
                    scanStatus = ScanStatus.COMPLETED,
                    scanProgress = 1f,
                    scanResults = results,
                    impactSummary = impactSummary,
                    currentPhase = null
                )
            }

            _effect.send(MessageQueueEffect.ShowSnackbar("Scan completed: ${results.size} access points found"))
        }
    }

    private fun cancelScan() {
        viewModelScope.launch {
            _state.update {
                it.copy(
                    scanStatus = ScanStatus.IDLE,
                    scanProgress = 0f,
                    currentPhase = null
                )
            }
            _effect.send(MessageQueueEffect.ShowSnackbar("Scan cancelled"))
        }
    }

    // ===== Filter / 过滤 =====

    private fun filterBySeverity(filter: SeverityFilter) {
        _state.update { it.copy(selectedFilter = filter) }
    }

    // ===== Detail / 详情 =====

    private fun selectAccess(access: ReflectionAccess) {
        _state.update {
            it.copy(
                selectedAccess = access,
                isDetailSheetOpen = true
            )
        }
    }

    private fun dismissDetail() {
        _state.update {
            it.copy(
                selectedAccess = null,
                isDetailSheetOpen = false
            )
        }
    }

    // ===== Knowledge Base / 知识库 =====

    private fun searchKnowledgeBase(query: String) {
        _state.update { it.copy(knowledgeSearchQuery = query) }
    }

    // ===== Migration / 迁移 =====

    private fun startMigration(step: MigrationStep) {
        viewModelScope.launch {
            _state.update { state ->
                val updatedSteps = state.migrationSteps.map {
                    if (it.id == step.id) it.copy(isInProgress = true) else it
                }
                state.copy(migrationSteps = updatedSteps, isMigrationInProgress = true)
            }

            // Simulate migration processing / 模拟迁移处理
            delay(1500)

            val success = Random.nextBoolean()
            _state.update { state ->
                val updatedSteps = state.migrationSteps.map {
                    if (it.id == step.id) {
                        if (success) {
                            it.copy(isCompleted = true, isInProgress = false)
                        } else {
                            it.copy(isInProgress = false, errorMessage = "File write failed: permission denied")
                        }
                    } else it
                }
                state.copy(
                    migrationSteps = updatedSteps,
                    isMigrationInProgress = updatedSteps.any { it.isInProgress }
                )
            }

            if (success) {
                _effect.send(MessageQueueEffect.ShowSnackbar("Migration step completed: ${step.access.filePath}:${step.access.lineNumber}"))
            } else {
                _effect.send(MessageQueueEffect.ShowSnackbar("Migration failed: permission denied", isError = true))
            }
        }
    }

    private fun previewMigration(step: MigrationStep) {
        _state.update { it.copy(selectedMigrationStep = step) }
    }

    private fun dismissMigrationPreview() {
        _state.update { it.copy(selectedMigrationStep = null) }
    }

    // ===== Regression Tests / 回归测试 =====

    private fun generateAllTests() {
        val accessPoints = simulatedScanResults
        val tests = accessPoints.mapIndexed { index, access ->
            val methodName = "testMessageQueue_${access.accessedField.name}_$index"
            val solution = simulatedKnowledgeBase.firstOrNull {
                access.codeSnippet.contains(it.targetPattern.take(10))
            }
            RegressionTest(
                id = "test-${access.filePath}:${access.lineNumber}",
                access = access,
                testMethodName = methodName,
                testDescription = "Verify ${access.severity.name} access at ${access.filePath}:${access.lineNumber}",
                testCode = """
                    @Test
                    fun $methodName() {
                        // TODO: Migrate from reflection to ${solution?.alternativeApi ?: "official API"}
                        // Original: ${access.codeSnippet}
                        // After lock-free migration, verify behavior is unchanged
                        assertTrue("MessageQueue access should be safe after migration",
                            true /* Replace with actual verification */)
                    }
                """.trimIndent()
            )
        }

        _state.update { it.copy(regressionTests = tests) }
        viewModelScope.launch {
            _effect.send(MessageQueueEffect.ShowSnackbar("Generated ${tests.size} regression tests"))
        }
    }

    private fun runTest(test: RegressionTest) {
        viewModelScope.launch {
            _state.update { state ->
                val updatedTests = state.regressionTests.map {
                    if (it.id == test.id) it.copy(isRunning = true) else it
                }
                state.copy(regressionTests = updatedTests)
            }

            // Simulate test execution / 模拟测试执行
            delay(2000)

            val passed = Random.nextBoolean()
            _state.update { state ->
                val updatedTests = state.regressionTests.map {
                    if (it.id == test.id) it.copy(isPassed = passed, isRunning = false) else it
                }
                state.copy(regressionTests = updatedTests)
            }

            _effect.send(
                MessageQueueEffect.ShowSnackbar(
                    if (passed) "Test passed: ${test.testMethodName}"
                    else "Test failed: ${test.testMethodName}",
                    isError = !passed
                )
            )
        }
    }

    private fun runAllTests() {
        viewModelScope.launch {
            _state.update { it.copy(isRunningAllTests = true) }

            val tests = _state.value.regressionTests
            tests.forEach { test ->
                runTest(test)
                delay(300) // Small delay between tests / 测试间隔
            }

            _state.update { it.copy(isRunningAllTests = false) }
            _effect.send(MessageQueueEffect.ShowSnackbar("All ${tests.size} tests completed"))
        }
    }

    // ===== Settings / 设置 =====

    private fun updateSettings(settings: MessageQueueSettings) {
        _state.update { it.copy(settings = settings) }
    }

    // ===== Report / 报告 =====

    private fun exportReport(format: ReportFormat) {
        viewModelScope.launch {
            val filePath = "/tmp/MessageQueue-Report-${System.currentTimeMillis()}.${format.name.lowercase()}"
            _effect.send(MessageQueueEffect.ReportGenerated(filePath, format))
            _effect.send(MessageQueueEffect.ShowSnackbar("Report exported: $filePath"))
        }
    }

    // ===== Error / 错误 =====

    private fun dismissError() {
        _state.update { it.copy(errorMessage = null) }
    }

    // ===== Helper Functions / 辅助函数 =====

    /**
     * Simulate phased progress with incremental updates
     * 模拟阶段性进度，逐步更新
     */
    private suspend fun simulatePhasedProgress(
        phase: ScanPhase,
        onProgress: suspend (Float) -> Unit
    ) {
        _state.update { it.copy(currentPhase = phase) }
        for (i in 1..10) {
            delay(150)  // 150ms per step, total ~1.5s per phase / 每步 150ms，每阶段约 1.5s
            onProgress(i / 10f)
        }
    }

    /**
     * Get filtered scan results based on current severity filter
     * 根据当前严重级别过滤器获取过滤后的扫描结果
     */
    fun getFilteredResults(): List<ReflectionAccess> {
        val currentState = _state.value
        return when (currentState.selectedFilter) {
            SeverityFilter.ALL -> currentState.scanResults
            SeverityFilter.CRASH -> currentState.scanResults.filter { it.severity == Severity.CRASH }
            SeverityFilter.ANOMALY -> currentState.scanResults.filter { it.severity == Severity.BEHAVIOR_ANOMALY }
            SeverityFilter.SAFE -> currentState.scanResults.filter { it.severity == Severity.SAFE }
        }
    }

    /**
     * Get filtered knowledge base based on search query
     * 根据搜索查询获取过滤后的知识库
     */
    fun getFilteredKnowledgeBase(): List<AlternativeSolution> {
        val query = _state.value.knowledgeSearchQuery.lowercase()
        return if (query.isBlank()) {
            simulatedKnowledgeBase
        } else {
            simulatedKnowledgeBase.filter {
                it.targetPattern.lowercase().contains(query) ||
                it.alternativeApi.lowercase().contains(query) ||
                it.useCase.lowercase().contains(query)
            }
        }
    }

    /**
     * Initialize migration steps from scan results
     * 从扫描结果初始化迁移步骤
     */
    fun initializeMigrationSteps() {
        val steps = simulatedScanResults
            .filter { it.severity != Severity.SAFE }
            .map { access ->
                val solution = simulatedKnowledgeBase.firstOrNull {
                    access.codeSnippet.contains(it.targetPattern.take(10))
                } ?: simulatedKnowledgeBase.first()

                MigrationStep(
                    id = UUID.randomUUID().toString(),
                    access = access,
                    targetSolution = solution,
                    previewBefore = access.contextSnippet,
                    previewAfter = "// Migrated from reflection to: ${solution.alternativeApi}\n" +
                            "// ${solution.migrationSteps.firstOrNull() ?: "See knowledge base for full steps"}"
                )
            }

        _state.update { it.copy(migrationSteps = steps) }
    }
}
