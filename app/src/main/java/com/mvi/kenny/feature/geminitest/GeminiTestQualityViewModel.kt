package com.mvi.kenny.feature.geminitest

// ================================================================
// GeminiTestQualityViewModel — Gemini Test Quality Toolkit ViewModel
// ================================================================
// ViewModel implementing MVI pattern for PRD-104.
//
// Processes GeminiTestQualityIntent and updates GeminiTestQualityState.
// Emits one-time side effects via GeminiTestQualityEffect Channel.
//
// PRD-104: Android Studio Panda 4 Gemini 单元测试生成工具包
// ================================================================

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

/**
 * ============================================================
 * GeminiTestQualityViewModel — MVI ViewModel
 * ================================================================
 * Manages UI state and processes user intents for the toolkit.
 * Single source of truth via StateFlow, side effects via Channel.
 */
class GeminiTestQualityViewModel : ViewModel() {

    // ─────────────────────────────────────────────────────────
    // State — Single source of truth (StateFlow)
    // ─────────────────────────────────────────────────────────
    private val _state = MutableStateFlow(GeminiTestQualityState())
    val state: StateFlow<GeminiTestQualityState> = _state.asStateFlow()

    // ─────────────────────────────────────────────────────────
    // Effect — One-time side effects (Channel)
    // ─────────────────────────────────────────────────────────
    private val _effect = Channel<GeminiTestQualityEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    // ─────────────────────────────────────────────────────────
    // Intent Processing Entry Point
    // ─────────────────────────────────────────────────────────
    /**
     * Process user intent and update state accordingly.
     * Call this from the UI layer via sendIntent().
     *
     * @param intent The user intention to process
     */
    fun sendIntent(intent: GeminiTestQualityIntent) {
        when (intent) {
            is GeminiTestQualityIntent.LoadDashboard -> loadDashboard()
            is GeminiTestQualityIntent.SelectTab -> selectTab(intent.tab)
            is GeminiTestQualityIntent.GenerateTests -> generateTests(intent.sourceFile, intent.framework)
            is GeminiTestQualityIntent.EvaluateQuality -> evaluateQuality(intent.testFile)
            is GeminiTestQualityIntent.SaveSpecTemplate -> saveSpecTemplate(intent.template)
            is GeminiTestQualityIntent.DeleteSpecTemplate -> deleteSpecTemplate(intent.templateId)
            is GeminiTestQualityIntent.ActivateSpec -> activateSpec(intent.templateId)
            is GeminiTestQualityIntent.GenerateCIConfig -> generateCIConfig(intent.ciProvider)
            is GeminiTestQualityIntent.AnalyzeBlindSpots -> analyzeBlindSpots()
            is GeminiTestQualityIntent.RefreshAll -> refreshAll()
            is GeminiTestQualityIntent.UpdateCoverageStrategy -> updateCoverageStrategy(intent.strategy)
            is GeminiTestQualityIntent.ExportReport -> exportReport()
        }
    }

    // ─────────────────────────────────────────────────────────
    // Intent Handlers
    // ─────────────────────────────────────────────────────────

    /** Load dashboard data with mock Gemini-generated test files */
    private fun loadDashboard() {
        viewModelScope.launch {
            _state.update { it.copy(error = null) }

            // Simulate loading Gemini-generated test files
            val mockTests = listOf(
                GeneratedTestFile(
                    id = "gtf-001",
                    sourceFileName = "UserRepository.kt",
                    testFileName = "UserRepositoryTest.kt",
                    framework = TestFramework.JUNIT5,
                    qualityScore = 82,
                    branchCoverage = 0.85f,
                    conditionCoverage = 0.78f,
                    pathCoverage = 0.72f,
                    generatedAt = "2026-04-14 06:00",
                    testCaseCount = 12,
                    missingRiskCount = 2
                ),
                GeneratedTestFile(
                    id = "gtf-002",
                    sourceFileName = "LoginViewModel.kt",
                    testFileName = "LoginViewModelTest.kt",
                    framework = TestFramework.JUNIT5,
                    qualityScore = 75,
                    branchCoverage = 0.70f,
                    conditionCoverage = 0.68f,
                    pathCoverage = 0.60f,
                    generatedAt = "2026-04-14 05:45",
                    testCaseCount = 8,
                    missingRiskCount = 4
                ),
                GeneratedTestFile(
                    id = "gtf-003",
                    sourceFileName = "NetworkService.kt",
                    testFileName = "NetworkServiceTest.kt",
                    framework = TestFramework.MOCKITO,
                    qualityScore = 91,
                    branchCoverage = 0.92f,
                    conditionCoverage = 0.88f,
                    pathCoverage = 0.85f,
                    generatedAt = "2026-04-14 05:30",
                    testCaseCount = 15,
                    missingRiskCount = 1
                ),
                GeneratedTestFile(
                    id = "gtf-004",
                    sourceFileName = "CacheManager.kt",
                    testFileName = "CacheManagerTest.kt",
                    framework = TestFramework.KOTEST,
                    qualityScore = 67,
                    branchCoverage = 0.55f,
                    conditionCoverage = 0.50f,
                    pathCoverage = 0.45f,
                    generatedAt = "2026-04-14 05:15",
                    testCaseCount = 6,
                    missingRiskCount = 6
                )
            )

            val heatmap = mapOf(
                "UserRepository.kt:28" to BranchCoverage(
                    "UserRepository.kt", "isValidUser", 28, 0.85f, CoverageLevel.High
                ),
                "UserRepository.kt:35" to BranchCoverage(
                    "UserRepository.kt", "authenticate", 35, 0.70f, CoverageLevel.Medium
                ),
                "LoginViewModel.kt:42" to BranchCoverage(
                    "LoginViewModel.kt", "validateInput", 42, 0.65f, CoverageLevel.Medium
                ),
                "NetworkService.kt:19" to BranchCoverage(
                    "NetworkService.kt", "retryOnFailure", 19, 0.92f, CoverageLevel.High
                ),
                "CacheManager.kt:55" to BranchCoverage(
                    "CacheManager.kt", "evictExpired", 55, 0.40f, CoverageLevel.Low
                )
            )

            val avgQuality = if (mockTests.isEmpty()) 0
                else mockTests.sumOf { it.qualityScore } / mockTests.size

            _state.update {
                it.copy(
                    generatedTests = mockTests,
                    qualityScore = QualityScore(
                        overall = avgQuality,
                        branchCoverage = mockTests.map { t -> t.branchCoverage }.average().toFloat(),
                        conditionCoverage = mockTests.map { t -> t.conditionCoverage }.average().toFloat(),
                        pathCoverage = mockTests.map { t -> t.pathCoverage }.average().toFloat(),
                        boundaryCoverage = 0.72f,
                        exceptionHandling = 0.68f
                    ),
                    coverageHeatmap = heatmap
                )
            }
        }
    }

    /** Switch to specified tab */
    private fun selectTab(tab: TestQualityTab) {
        _state.update { it.copy(selectedTab = tab) }
    }

    /** Generate tests for source file using specified framework */
    private fun generateTests(sourceFile: String, framework: TestFramework) {
        viewModelScope.launch {
            _state.update { it.copy(isGenerating = true, generationProgress = 0f, error = null) }

            // Simulate progressive generation progress
            for (i in 1..10) {
                delay(150)
                _state.update { it.copy(generationProgress = i / 10f) }
            }

            val newTest = GeneratedTestFile(
                id = "gtf-${System.currentTimeMillis()}",
                sourceFileName = sourceFile,
                testFileName = "${sourceFile.removeSuffix(".kt")}Test.kt",
                framework = framework,
                qualityScore = (65..95).random(),
                branchCoverage = (0.5f..1.0f).random(),
                conditionCoverage = (0.5f..1.0f).random(),
                pathCoverage = (0.4f..0.9f).random(),
                generatedAt = "2026-04-14 06:34",
                testCaseCount = (5..20).random(),
                missingRiskCount = (0..5).random()
            )

            _state.update {
                it.copy(
                    generatedTests = it.generatedTests + newTest,
                    isGenerating = false,
                    generationProgress = 1f
                )
            }

            _effect.send(GeminiTestQualityEffect.GenerationCompleted(
                testFileName = newTest.testFileName,
                testCaseCount = newTest.testCaseCount
            ))
            _effect.send(GeminiTestQualityEffect.ShowSnackbar(
                "测试生成完成: ${newTest.testFileName} (${newTest.testCaseCount} cases)"
            ))
        }
    }

    /** Evaluate quality of specified test file */
    private fun evaluateQuality(testFile: String) {
        viewModelScope.launch {
            _state.update { it.copy(error = null) }
            _effect.send(GeminiTestQualityEffect.ShowSnackbar("正在评估: $testFile"))
            delay(800)
            _effect.send(GeminiTestQualityEffect.ShowSnackbar("质量评估完成"))
        }
    }

    /** Save a new or updated spec template */
    private fun saveSpecTemplate(template: TestSpecTemplate) {
        viewModelScope.launch {
            val current = _state.value.specTemplates.toMutableList()
            val existing = current.indexOfFirst { it.id == template.id }
            if (existing >= 0) {
                current[existing] = template
            } else {
                current.add(template)
            }
            _state.update { it.copy(specTemplates = current) }
            _effect.send(GeminiTestQualityEffect.ShowSnackbar("规范模板已保存: ${template.name}"))
        }
    }

    /** Delete specified spec template */
    private fun deleteSpecTemplate(templateId: String) {
        viewModelScope.launch {
            _state.update {
                it.copy(
                    specTemplates = it.specTemplates.filter { t -> t.id != templateId },
                    activeSpec = it.activeSpec?.takeIf { a -> a.id != templateId }
                )
            }
            _effect.send(GeminiTestQualityEffect.ShowSnackbar("规范模板已删除"))
        }
    }

    /** Activate specified spec template */
    private fun activateSpec(templateId: String) {
        viewModelScope.launch {
            val template = _state.value.specTemplates.find { it.id == templateId }
            if (template != null) {
                _state.update {
                    it.copy(
                        activeSpec = template,
                        specTemplates = it.specTemplates.map { t ->
                            t.copy(isActive = t.id == templateId)
                        }
                    )
                }
                _effect.send(GeminiTestQualityEffect.ShowSnackbar("已激活: ${template.name}"))
            }
        }
    }

    /** Generate CI/CD configuration for specified provider */
    private fun generateCIConfig(ciProvider: CIProvider) {
        viewModelScope.launch {
            _state.update { it.copy(isGenerating = true, error = null) }
            delay(600)

            val yamlContent = when (ciProvider) {
                CIProvider.GitHubActions -> buildString {
                    appendLine("name: Gemini Test Generation")
                    appendLine("on:")
                    appendLine("  pull_request:")
                    appendLine("  push:")
                    appendLine("jobs:")
                    appendLine("  generate-and-test:")
                    appendLine("    runs-on: ubuntu-latest")
                    appendLine("    steps:")
                    appendLine("      - uses: actions/checkout@v4")
                    appendLine("      - name: Setup JDK")
                    appendLine("        uses: actions/setup-java@v4")
                    appendLine("        with:")
                    appendLine("          distribution: 'temurin'")
                    appendLine("      - name: Run Gemini Test Generation")
                    appendLine("        run: ./gradlew generateGeminiTests")
                    appendLine("      - name: Run Unit Tests")
                    appendLine("        run: ./gradlew testDebugUnitTest")
                    appendLine("      - name: Upload Test Reports")
                    appendLine("        if: failure()")
                    appendLine("        uses: actions/upload-artifact@v4")
                    appendLine("        with:")
                    appendLine("          name: test-reports")
                    appendLine("          path: app/build/reports/tests/")
                }
                CIProvider.GitLabCI -> buildString {
                    appendLine("gemini-tests:")
                    appendLine("  stage: test")
                    appendLine("  script:")
                    appendLine("    - ./gradlew generateGeminiTests")
                    appendLine("    - ./gradlew testDebugUnitTest")
                    appendLine("  artifacts:")
                    appendLine("    when: always")
                    appendLine("    paths:")
                    appendLine("      - app/build/reports/tests/")
                }
            }

            _state.update {
                it.copy(
                    ciConfig = CIConfig(
                        provider = ciProvider,
                        yamlContent = yamlContent,
                        autoRegenerateOnFailure = it.ciConfig.autoRegenerateOnFailure,
                        triggerOn = it.ciConfig.triggerOn
                    ),
                    isGenerating = false
                )
            }
            _effect.send(GeminiTestQualityEffect.ShowSnackbar("${ciProvider.displayName} 配置已生成"))
        }
    }

    /** Analyze test blind spots */
    private fun analyzeBlindSpots() {
        viewModelScope.launch {
            _state.update { it.copy(isGenerating = true, error = null) }
            delay(1000)

            val mockBlindSpots = listOf(
                BlindSpot(
                    id = "bs-001",
                    fileName = "UserRepository.kt",
                    methodName = "updateUserProfile",
                    lineNumber = 72,
                    description = "未覆盖 null profile 字段的异常路径",
                    severity = BlindSpotSeverity.Critical,
                    suggestedTestcases = listOf(
                        "updateUserProfile_withNullFields_shouldThrow",
                        "updateUserProfile_withEmptyName_shouldHandle"
                    )
                ),
                BlindSpot(
                    id = "bs-002",
                    fileName = "LoginViewModel.kt",
                    methodName = "onLoginClick",
                    lineNumber = 55,
                    description = "网络超时场景未测试",
                    severity = BlindSpotSeverity.Warning,
                    suggestedTestcases = listOf(
                        "onLoginClick_withTimeout_shouldShowError"
                    )
                ),
                BlindSpot(
                    id = "bs-003",
                    fileName = "CacheManager.kt",
                    methodName = "put",
                    lineNumber = 33,
                    description = "缓存满时的 LRU 淘汰逻辑未验证",
                    severity = BlindSpotSeverity.Critical,
                    suggestedTestcases = listOf(
                        "put_withFullCache_shouldEvictOldest",
                        "put_withMixedKeys_shouldFollowLRU"
                    )
                ),
                BlindSpot(
                    id = "bs-004",
                    fileName = "NetworkService.kt",
                    methodName = "parseResponse",
                    lineNumber = 88,
                    description = "空响应体的解析路径未覆盖",
                    severity = BlindSpotSeverity.Info,
                    suggestedTestcases = listOf(
                        "parseResponse_withEmptyBody_shouldNotThrow"
                    )
                )
            )

            _state.update {
                it.copy(blindSpots = mockBlindSpots, isGenerating = false)
            }
            _effect.send(GeminiTestQualityEffect.ShowSnackbar(
                "盲区分析完成: 发现 ${mockBlindSpots.size} 个未覆盖路径"
            ))
        }
    }

    /** Refresh all data */
    private fun refreshAll() {
        viewModelScope.launch {
            _state.update { it.copy(error = null) }
            loadDashboard()
            loadSpecTemplates()
            loadTraceabilityRecords()
            _effect.send(GeminiTestQualityEffect.ShowSnackbar("数据已刷新"))
        }
    }

    /** Update coverage strategy configuration */
    private fun updateCoverageStrategy(strategy: CoverageStrategy) {
        _state.update { it.copy(coverageStrategy = strategy) }
    }

    /** Export quality report */
    private fun exportReport() {
        viewModelScope.launch {
            _effect.send(GeminiTestQualityEffect.ExportReport("/tmp/gemini-test-quality-report.html"))
            _effect.send(GeminiTestQualityEffect.ShowSnackbar("报告已导出"))
        }
    }

    // ─────────────────────────────────────────────────────────
    // Private Helpers
    // ─────────────────────────────────────────────────────────

    /** Load mock spec templates */
    private fun loadSpecTemplates() {
        val templates = listOf(
            TestSpecTemplate(
                id = "spec-001",
                name = "标准 JUnit 5 规范",
                description = "适用于大多数 Android 项目的标准测试规范",
                namingRule = "MethodName_ShouldExpectedBehavior_WhenCondition",
                commentStyle = "// Arrange / Act / Assert",
                boundaryRule = "Test edge cases: null, empty, min, max, min-1, max+1",
                mockRule = "Use @Mock for dependencies, @InjectMocks for SUT",
                isActive = true
            ),
            TestSpecTemplate(
                id = "spec-002",
                name = "Kotest 规范",
                description = "Kotest 框架专用测试规范",
                namingRule = "shouldBehaveCorrectlyWhen(condition)",
                commentStyle = "Given-When-Then 或 BDD 风格",
                boundaryRule = "Property-based testing with checkAll { }",
                mockRule = "MockAdapter 或 mockk()",
                isActive = false
            ),
            TestSpecTemplate(
                id = "spec-003",
                name = "严格边界值规范",
                description = "边界值测试优先，适用于安全敏感场景",
                namingRule = "testMethod_name_boundary",
                commentStyle = "标注测试目标边界值",
                boundaryRule = "必须覆盖: min-1, min, typical, max, max+1",
                mockRule = "Strict stubs, verify all interactions",
                isActive = false
            )
        )
        _state.update { it.copy(specTemplates = templates, activeSpec = templates.firstOrNull()) }
    }

    /** Load mock traceability records */
    private fun loadTraceabilityRecords() {
        val records = listOf(
            TraceabilityRecord(
                id = "tr-001",
                sourceFileName = "UserRepository.kt",
                changedAt = "2026-04-14 06:00",
                changeType = "MODIFY",
                impactedTests = listOf("UserRepositoryTest.kt"),
                regressionNeeded = true
            ),
            TraceabilityRecord(
                id = "tr-002",
                sourceFileName = "LoginViewModel.kt",
                changedAt = "2026-04-13 22:00",
                changeType = "ADD",
                impactedTests = listOf("LoginViewModelTest.kt"),
                regressionNeeded = true
            )
        )
        _state.update { it.copy(traceabilityRecords = records) }
    }

    init {
        // Load initial data / 加载初始数据
        loadDashboard()
        loadSpecTemplates()
        loadTraceabilityRecords()
    }

    // ─────────────────────────────────────────────────────────
    // Extension: random float in range
    // ─────────────────────────────────────────────────────────
    private fun ClosedFloatingPointRange<Float>.random(): Float {
        return start + (endInclusive - start) * kotlin.random.Random.nextFloat()
    }
}
