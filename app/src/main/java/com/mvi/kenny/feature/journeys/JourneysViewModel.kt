package com.mvi.kenny.feature.journeys

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
 * JourneysViewModel — Journeys E2E 测试工具包 ViewModel
 * ================================================================
 * MVI architecture ViewModel for Journeys E2E Testing Toolkit.
 *
 * PRD-099: Journeys for Android Studio 自动化 E2E 测试工具包
 * Design Reference: memory/agency/designs/PRD-099-Journeys-E2E测试工具包.md
 *
 * Key behaviors:
 * 1. Journey test case CRUD (create, read, update, delete)
 * 2. Journey XML editor with step tree management
 * 3. Natural Language → Journey XML generation (mock)
 * 4. Journey execution simulation with step-by-step results
 * 5. CI/CD YAML configuration generation (GitHub Actions / GitLab CI / Jenkins)
 * 6. Framework capability comparison matrix (Journey vs Espresso vs UIAutomator)
 * —————————————————————————————————————————————————————
 */
class JourneysViewModel : ViewModel() {

    // MVI State — single source of truth using StateFlow
    private val _state = MutableStateFlow(JourneysState())
    val state: StateFlow<JourneysState> = _state.asStateFlow()

    // MVI Effect — one-time side effects via Channel
    private val _effect = Channel<JourneysEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    // Date formatter for timestamps
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

    init {
        // Load initial data / 加载初始数据
        loadMockJourneys()
        loadMockTemplates()
        loadMockFrameworkCapabilities()
    }

    /**
     * ============================================================
     * sendIntent — 处理用户意图
     * ================================================================
     * Entry point for all user intents. Maps Intent → Business Logic.
     *
     * @param intent User intent from the UI layer
     */
    fun sendIntent(intent: JourneysIntent) {
        when (intent) {
            // Dashboard / 仪表盘
            is JourneysIntent.LoadJourneys -> loadJourneys()
            is JourneysIntent.FilterJourneys -> filterJourneys(intent.status)
            is JourneysIntent.SearchJourneys -> searchJourneys(intent.query)
            is JourneysIntent.SwitchTab -> switchTab(intent.tab)

            // Journey Management / Journey 管理
            is JourneysIntent.OpenJourney -> openJourney(intent.journeyId)
            is JourneysIntent.CreateJourneyFromTemplate -> createFromTemplate(intent.templateId)
            is JourneysIntent.CreateBlankJourney -> createBlankJourney()
            is JourneysIntent.SaveJourney -> saveJourney(intent.journeyId, intent.xml)
            is JourneysIntent.DeleteJourney -> deleteJourney(intent.journeyId)

            // Editor / 编辑器
            is JourneysIntent.UpdateJourneyXml -> updateJourneyXml(intent.xml)
            is JourneysIntent.AddStep -> addStep(intent.afterStepId, intent.step)
            is JourneysIntent.DeleteStep -> deleteStep(intent.stepId)
            is JourneysIntent.ReorderSteps -> reorderSteps(intent.fromIndex, intent.toIndex)
            is JourneysIntent.SelectStep -> selectStep(intent.stepId)

            // NL Generation / 自然语言生成
            is JourneysIntent.UpdateNLInput -> updateNLInput(intent.text)
            is JourneysIntent.GenerateFromNL -> generateFromNL()
            is JourneysIntent.InsertGeneratedXML -> insertGeneratedXML()

            // Template / 模板
            is JourneysIntent.SearchTemplates -> searchTemplates(intent.query)
            is JourneysIntent.FilterTemplatesByCategory -> filterByCategory(intent.category)

            // Execution / 执行
            is JourneysIntent.RunJourney -> runJourney(intent.journeyId)
            is JourneysIntent.RerunFailedStep -> rerunFailedStep(intent.journeyId, intent.stepId)
            is JourneysIntent.ToggleStepDetail -> toggleStepDetail(intent.stepId)

            // CI/CD / CI/CD 配置
            is JourneysIntent.SelectCIPlatform -> selectCIPlatform(intent.platform)
            is JourneysIntent.CopyCIConfig -> copyCIConfig()
            is JourneysIntent.DownloadCIConfig -> downloadCIConfig()

            // UI / UI 操作
            is JourneysIntent.ClearError -> clearError()
        }
    }

    // ================================================================
    // Dashboard / 仪表盘
    // ================================================================

    /**
     * Load all Journey test cases.
     * In production, this reads from app/src/test/journeys/ directory.
     */
    private fun loadJourneys() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            delay(500) // Simulate loading delay / 模拟加载延迟
            _state.value = _state.value.copy(isLoading = false)
        }
    }

    /**
     * Filter journey list by execution status.
     *
     * @param status Status filter to apply
     */
    private fun filterJourneys(status: JourneyStatus) {
        _state.value = _state.value.copy(filterStatus = status)
    }

    /**
     * Search journeys by name.
     *
     * @param query Search query string
     */
    private fun searchJourneys(query: String) {
        _state.value = _state.value.copy(searchQuery = query)
    }

    /**
     * Switch active tab in the toolkit.
     *
     * @param tab Target tab to switch to
     */
    private fun switchTab(tab: ActiveTab) {
        _state.value = _state.value.copy(activeTab = tab)
    }

    // ================================================================
    // Journey Management / Journey 管理
    // ================================================================

    /**
     * Open a journey for viewing or editing.
     *
     * @param journeyId ID of the journey to open
     */
    private fun openJourney(journeyId: String) {
        val journey = _state.value.journeys.find { it.id == journeyId }
        if (journey != null) {
            val steps = generateMockStepsForJourney(journey)
            _state.value = _state.value.copy(
                activeJourneyId = journeyId,
                editorMode = EditorMode.View,
                currentJourneyXml = generateMockXml(journey),
                currentSteps = steps
            )
        }
    }

    /**
     * Create a new journey from a template.
     *
     * @param templateId ID of the template to use
     */
    private fun createFromTemplate(templateId: String) {
        val template = _state.value.templates.find { it.id == templateId }
        if (template != null) {
            val newId = UUID.randomUUID().toString()
            val newJourney = JourneyItem(
                id = newId,
                name = "${template.name.lowercase().replace(" ", "_")}_${System.currentTimeMillis() % 10000}",
                filePath = "app/src/test/journeys/${template.name.lowercase().replace(" ", "_")}.xml",
                status = JourneyStatus.Passed,
                lastRunTime = dateFormat.format(Date()),
                stepCount = template.stepCount
            )
            _state.value = _state.value.copy(
                journeys = _state.value.journeys + newJourney,
                activeJourneyId = newId,
                editorMode = EditorMode.Edit,
                currentJourneyXml = template.previewXml,
                currentSteps = emptyList(),
                activeTab = ActiveTab.Editor
            )
            viewModelScope.launch {
                _effect.send(JourneysEffect.JourneyCreated(newId, newJourney.name))
            }
        }
    }

    /**
     * Create a blank new journey.
     */
    private fun createBlankJourney() {
        val newId = UUID.randomUUID().toString()
        val blankXml = """<?xml version="1.0" encoding="UTF-8"?>
<Journey name="new_journey" version="1.0">
    <!-- Add steps here -->
</Journey>"""
        _state.value = _state.value.copy(
            activeJourneyId = newId,
            editorMode = EditorMode.Create,
            currentJourneyXml = blankXml,
            currentSteps = emptyList(),
            activeTab = ActiveTab.Editor
        )
        viewModelScope.launch {
            _effect.send(JourneysEffect.JourneyCreated(newId, "new_journey"))
        }
    }

    /**
     * Save journey XML content.
     *
     * @param journeyId ID of the journey to save
     * @param xml New XML content
     */
    private fun saveJourney(journeyId: String, xml: String) {
        _state.value = _state.value.copy(
            currentJourneyXml = xml,
            editorMode = EditorMode.View
        )
        viewModelScope.launch {
            _effect.send(JourneysEffect.JourneySaved(journeyId))
            _effect.send(JourneysEffect.ShowToast("Journey saved successfully / Journey 保存成功"))
        }
    }

    /**
     * Delete a journey.
     *
     * @param journeyId ID of the journey to delete
     */
    private fun deleteJourney(journeyId: String) {
        val journey = _state.value.journeys.find { it.id == journeyId }
        if (journey != null) {
            _state.value = _state.value.copy(
                journeys = _state.value.journeys.filter { it.id != journeyId },
                activeJourneyId = if (_state.value.activeJourneyId == journeyId) null else _state.value.activeJourneyId
            )
            viewModelScope.launch {
                _effect.send(JourneysEffect.JourneyDeleted(journeyId, journey.name))
            }
        }
    }

    // ================================================================
    // Editor / 编辑器
    // ================================================================

    /**
     * Update the Journey XML content in the editor.
     *
     * @param xml New XML content
     */
    private fun updateJourneyXml(xml: String) {
        _state.value = _state.value.copy(
            currentJourneyXml = xml,
            editorMode = EditorMode.Edit
        )
    }

    /**
     * Add a new step to the current journey.
     *
     * @param afterStepId Insert after this step ID (null = append to end)
     * @param step Step to add
     */
    private fun addStep(afterStepId: String?, step: JourneyStep) {
        val steps = _state.value.currentSteps.toMutableList()
        val insertIndex = if (afterStepId != null) {
            steps.indexOfFirst { it.id == afterStepId } + 1
        } else {
            steps.size
        }
        steps.add(insertIndex.coerceAtLeast(0), step)
        // Re-index orders / 重新编号
        val reindexedSteps = steps.mapIndexed { index, s -> s.copy(order = index + 1) }
        _state.value = _state.value.copy(currentSteps = reindexedSteps)
    }

    /**
     * Delete a step from the current journey.
     *
     * @param stepId ID of the step to delete
     */
    private fun deleteStep(stepId: String) {
        val steps = _state.value.currentSteps.filter { it.id != stepId }
        // Re-index orders / 重新编号
        val reindexedSteps = steps.mapIndexed { index, s -> s.copy(order = index + 1) }
        _state.value = _state.value.copy(
            currentSteps = reindexedSteps,
            selectedStepId = if (_state.value.selectedStepId == stepId) null else _state.value.selectedStepId
        )
    }

    /**
     * Reorder steps by moving from one index to another.
     *
     * @param fromIndex Source index
     * @param toIndex Destination index
     */
    private fun reorderSteps(fromIndex: Int, toIndex: Int) {
        val steps = _state.value.currentSteps.toMutableList()
        if (fromIndex in steps.indices && toIndex in steps.indices) {
            val item = steps.removeAt(fromIndex)
            steps.add(toIndex, item)
            // Re-index orders / 重新编号
            val reindexedSteps = steps.mapIndexed { index, s -> s.copy(order = index + 1) }
            _state.value = _state.value.copy(currentSteps = reindexedSteps)
        }
    }

    /**
     * Select a step for editing.
     *
     * @param stepId ID of the step to select (null to deselect)
     */
    private fun selectStep(stepId: String?) {
        _state.value = _state.value.copy(selectedStepId = stepId)
    }

    // ================================================================
    // NL Generation / 自然语言生成
    // ================================================================

    /**
     * Update natural language input text.
     *
     * @param text New NL input text
     */
    private fun updateNLInput(text: String) {
        _state.value = _state.value.copy(nlInputText = text)
    }

    /**
     * Generate Journey XML from natural language description.
     * In production, this calls Gemini API.
     * Currently simulates the generation with mock output.
     */
    private fun generateFromNL() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isGeneratingNL = true)
            delay(2000) // Simulate API call delay / 模拟 API 调用延迟

            val nlText = _state.value.nlInputText
            val generatedXml = when {
                nlText.contains("登录", ignoreCase = true) -> getLoginFlowXml()
                nlText.contains("支付", ignoreCase = true) || nlText.contains("checkout", ignoreCase = true) -> getPaymentFlowXml()
                nlText.contains("搜索", ignoreCase = true) || nlText.contains("search", ignoreCase = true) -> getSearchFlowXml()
                else -> getDefaultFlowXml(nlText)
            }

            _state.value = _state.value.copy(
                isGeneratingNL = false,
                nlGenerationResult = generatedXml
            )
            _effect.send(JourneysEffect.NLGenerationCompleted(generatedXml))
        }
    }

    /**
     * Insert the generated XML into the editor.
     */
    private fun insertGeneratedXML() {
        val generated = _state.value.nlGenerationResult
        if (generated != null) {
            _state.value = _state.value.copy(
                currentJourneyXml = generated,
                editorMode = EditorMode.Edit,
                nlGenerationResult = null
            )
            viewModelScope.launch {
                _effect.send(JourneysEffect.ShowToast("XML inserted into editor / XML 已插入编辑器"))
            }
        }
    }

    // ================================================================
    // Template / 模板
    // ================================================================

    /**
     * Search templates by name or description.
     *
     * @param query Search query
     */
    private fun searchTemplates(query: String) {
        _state.value = _state.value.copy(templateSearchQuery = query)
    }

    /**
     * Filter templates by category.
     *
     * @param category Category to filter by (null = show all)
     */
    private fun filterByCategory(category: TemplateCategory?) {
        _state.value = _state.value.copy(selectedCategory = category)
    }

    // ================================================================
    // Execution / 执行
    // ================================================================

    /**
     * Run a Journey test.
     *
     * @param journeyId ID of the journey to run
     */
    private fun runJourney(journeyId: String) {
        viewModelScope.launch {
            val journey = _state.value.journeys.find { it.id == journeyId } ?: return@launch

            // Mark as running / 标记为运行中
            val updatedJourneys = _state.value.journeys.map {
                if (it.id == journeyId) it.copy(isRunning = true) else it
            }
            _state.value = _state.value.copy(
                journeys = updatedJourneys,
                isRunning = true,
                runningJourneyId = journeyId,
                activeTab = ActiveTab.Results
            )

            // Simulate execution with mock steps / 模拟执行过程
            val startTime = dateFormat.format(Date())
            val steps = generateMockStepsForJourney(journey)
            val mutableSteps = steps.toMutableList()
            val results = mutableListOf<JourneyStep>()

            for (step in mutableSteps) {
                // Running state for this step / 当前步骤运行中
                val runningStep = step.copy(status = StepStatus.Running)
                _state.value = _state.value.copy(executionResult = ExecutionResult(
                    journeyId = journeyId,
                    overallStatus = JourneyStatus.Running,
                    startTime = startTime,
                    endTime = "",
                    totalDurationMs = 0,
                    steps = results + runningStep
                ))
                delay(800) // Simulate step execution time / 模拟步骤执行时间

                // Simulate success/failure (90% success rate) / 模拟成功/失败（90%成功率）
                val isSuccess = Random.nextFloat() > 0.1f
                val executedStep = if (isSuccess) {
                    step.copy(
                        status = StepStatus.Passed,
                        durationMs = Random.nextLong(500, 3000)
                    )
                } else {
                    step.copy(
                        status = StepStatus.Failed,
                        durationMs = Random.nextLong(500, 2000),
                        screenshotPath = "app/build/journeys/screenshots/${step.id}.png",
                        errorMessage = getRandomErrorMessage()
                    )
                }
                results.add(executedStep)

                val totalDuration = results.sumOf { it.durationMs }
                val overallStatus = if (executedStep.status == StepStatus.Failed) JourneyStatus.Failed else JourneyStatus.Passed

                _state.value = _state.value.copy(executionResult = ExecutionResult(
                    journeyId = journeyId,
                    overallStatus = overallStatus,
                    startTime = startTime,
                    endTime = dateFormat.format(Date()),
                    totalDurationMs = totalDuration,
                    steps = results,
                    failedStepId = results.find { it.status == StepStatus.Failed }?.id,
                    totalPassed = results.count { it.status == StepStatus.Passed },
                    totalFailed = results.count { it.status == StepStatus.Failed },
                    totalSkipped = results.count { it.status == StepStatus.Skipped }
                ))
            }

            // Finalize / 完成
            val finalResult = _state.value.executionResult!!
            val finalUpdatedJourneys = _state.value.journeys.map {
                if (it.id == journeyId) it.copy(
                    isRunning = false,
                    status = finalResult.overallStatus,
                    lastRunTime = finalResult.endTime,
                    lastRunDurationMs = finalResult.totalDurationMs,
                    passedStepCount = finalResult.totalPassed,
                    failedStepCount = finalResult.totalFailed
                ) else it
            }
            _state.value = _state.value.copy(
                journeys = finalUpdatedJourneys,
                isRunning = false,
                runningJourneyId = null
            )
            _effect.send(JourneysEffect.JourneyRunCompleted(journeyId, finalResult))
        }
    }

    /**
     * Re-run a failed step individually.
     *
     * @param journeyId ID of the parent journey
     * @param stepId ID of the step to re-run
     */
    private fun rerunFailedStep(journeyId: String, stepId: String) {
        viewModelScope.launch {
            val result = _state.value.executionResult ?: return@launch
            val stepIndex = result.steps.indexOfFirst { it.id == stepId }
            if (stepIndex < 0) return@launch

            // Mark as running / 标记为运行中
            val updatedSteps = result.steps.toMutableList()
            updatedSteps[stepIndex] = updatedSteps[stepIndex].copy(status = StepStatus.Running)
            _state.value = _state.value.copy(executionResult = result.copy(steps = updatedSteps))
            delay(1500) // Simulate re-run delay / 模拟重新运行延迟

            // Simulate 70% success on retry / 模拟重试70%成功率
            val isSuccess = Random.nextFloat() > 0.3f
            updatedSteps[stepIndex] = if (isSuccess) {
                updatedSteps[stepIndex].copy(
                    status = StepStatus.Passed,
                    durationMs = Random.nextLong(500, 3000),
                    errorMessage = null,
                    screenshotPath = null
                )
            } else {
                updatedSteps[stepIndex].copy(
                    status = StepStatus.Failed,
                    durationMs = Random.nextLong(500, 2000)
                )
            }

            val totalPassed = updatedSteps.count { it.status == StepStatus.Passed }
            val totalFailed = updatedSteps.count { it.status == StepStatus.Failed }
            val overallStatus = if (totalFailed > 0) JourneyStatus.Failed else JourneyStatus.Passed

            _state.value = _state.value.copy(executionResult = result.copy(
                steps = updatedSteps,
                overallStatus = overallStatus,
                totalPassed = totalPassed,
                totalFailed = totalFailed,
                failedStepId = updatedSteps.find { it.status == StepStatus.Failed }?.id
            ))
        }
    }

    /**
     * Toggle expanded detail view for a step.
     *
     * @param stepId ID of the step to toggle
     */
    private fun toggleStepDetail(stepId: String) {
        _state.value = _state.value.copy(
            expandedStepId = if (_state.value.expandedStepId == stepId) null else stepId
        )
    }

    // ================================================================
    // CI/CD / CI/CD 配置
    // ================================================================

    /**
     * Select CI/CD platform and generate corresponding YAML.
     *
     * @param platform CI/CD platform to generate YAML for
     */
    private fun selectCIPlatform(platform: CIPlatform) {
        val yaml = when (platform) {
            CIPlatform.GitHubActions -> getGitHubActionsYaml()
            CIPlatform.GitLabCI -> getGitLabCIYaml()
            CIPlatform.Jenkins -> getJenkinsYaml()
        }
        _state.value = _state.value.copy(
            selectedCIPlatform = platform,
            generatedYAML = yaml
        )
    }

    /**
     * Copy CI configuration to clipboard.
     */
    private fun copyCIConfig() {
        viewModelScope.launch {
            _effect.send(JourneysEffect.CopiedToClipboard(_state.value.generatedYAML))
            _effect.send(JourneysEffect.ShowToast("CI configuration copied / CI 配置已复制到剪贴板"))
        }
    }

    /**
     * Download CI configuration file.
     */
    private fun downloadCIConfig() {
        viewModelScope.launch {
            _effect.send(JourneysEffect.ShowToast("CI configuration downloaded / CI 配置已下载"))
        }
    }

    // ================================================================
    // UI / UI 操作
    // ================================================================

    /**
     * Clear current error message.
     */
    private fun clearError() {
        _state.value = _state.value.copy(errorMessage = null)
    }

    // ================================================================
    // Mock Data Generators / 模拟数据生成器
    // ================================================================

    /**
     * Load mock Journey test cases for demonstration.
     */
    private fun loadMockJourneys() {
        val mockJourneys = listOf(
            JourneyItem(
                id = "j1", name = "login_journey", filePath = "app/src/test/journeys/login_journey.xml",
                status = JourneyStatus.Passed, lastRunTime = dateFormat.format(Date()),
                lastRunDurationMs = 5300, stepCount = 4, passedStepCount = 4, failedStepCount = 0
            ),
            JourneyItem(
                id = "j2", name = "checkout_flow", filePath = "app/src/test/journeys/checkout_flow.xml",
                status = JourneyStatus.Failed, lastRunTime = dateFormat.format(Date()),
                lastRunDurationMs = 8700, stepCount = 6, passedStepCount = 3, failedStepCount = 2
            ),
            JourneyItem(
                id = "j3", name = "search_product", filePath = "app/src/test/journeys/search_product.xml",
                status = JourneyStatus.Running, lastRunTime = dateFormat.format(Date()),
                lastRunDurationMs = 0, stepCount = 5, passedStepCount = 0, failedStepCount = 0,
                isRunning = true
            ),
            JourneyItem(
                id = "j4", name = "payment_flow", filePath = "app/src/test/journeys/payment_flow.xml",
                status = JourneyStatus.Passed, lastRunTime = dateFormat.format(Date()),
                lastRunDurationMs = 12000, stepCount = 7, passedStepCount = 7, failedStepCount = 0
            ),
            JourneyItem(
                id = "j5", name = "profile_update", filePath = "app/src/test/journeys/profile_update.xml",
                status = JourneyStatus.Failed, lastRunTime = dateFormat.format(Date()),
                lastRunDurationMs = 4200, stepCount = 3, passedStepCount = 2, failedStepCount = 1
            )
        )
        _state.value = _state.value.copy(journeys = mockJourneys)
    }

    /**
     * Load mock Journey templates.
     */
    private fun loadMockTemplates() {
        val mockTemplates = listOf(
            JourneyTemplate(
                id = "t1", name = "标准登录流程", category = TemplateCategory.LoginFlow,
                description = "包含登录/注册/登出的标准用户认证流程",
                stepCount = 4,
                tags = listOf("login", "auth", "authentication"),
                previewXml = getLoginFlowXml(),
                isBuiltIn = true
            ),
            JourneyTemplate(
                id = "t2", name = "购物车流程", category = TemplateCategory.PaymentFlow,
                description = "搜索→商品详情→加入购物车→结算的标准电商流程",
                stepCount = 6,
                tags = listOf("shopping", "cart", "checkout", "ecommerce"),
                previewXml = getPaymentFlowXml(),
                isBuiltIn = true
            ),
            JourneyTemplate(
                id = "t3", name = "表单填写", category = TemplateCategory.FormFilling,
                description = "多字段表单输入、验证、提交的完整流程",
                stepCount = 5,
                tags = listOf("form", "input", "validation"),
                previewXml = """<?xml version="1.0" encoding="UTF-8"?>
<Journey name="form_filling" version="1.0">
    <Step id="1" type="launch"><Package>com.example.app</Package></Step>
    <Step id="2" type="input"><Target>#name_field</Target><Value>John Doe</Value></Step>
    <Step id="3" type="input"><Target>#email_field</Target><Value>john@example.com</Value></Step>
    <Step id="4" type="input"><Target>#phone_field</Target><Value>1234567890</Value></Step>
    <Step id="5" type="click"><Target>#submit_button</Target></Step>
</Journey>""",
                isBuiltIn = true
            ),
            JourneyTemplate(
                id = "t4", name = "深度链接跳转", category = TemplateCategory.DeepLink,
                description = "通过 App Link / URI 直接跳转到指定页面",
                stepCount = 3,
                tags = listOf("deeplink", "uri", "navigation", "app-link"),
                previewXml = """<?xml version="1.0" encoding="UTF-8"?>
<Journey name="deep_link" version="1.0">
    <Step id="1" type="deeplink"><Uri>myapp://product/12345</Uri></Step>
    <Step id="2" type="assert"><Target>#product_title</Target><Expected>iPhone 15 Pro</Expected></Step>
    <Step id="3" type="click"><Target>#add_to_cart</Target></Step>
</Journey>""",
                isBuiltIn = true
            ),
            JourneyTemplate(
                id = "t5", name = "搜索商品", category = TemplateCategory.ListOperation,
                description = "搜索→浏览结果列表→选择商品的完整搜索流程",
                stepCount = 5,
                tags = listOf("search", "list", "scroll", "select"),
                previewXml = getSearchFlowXml(),
                isBuiltIn = true
            ),
            JourneyTemplate(
                id = "t6", name = "导航流程", category = TemplateCategory.Navigation,
                description = "应用内多页面导航与返回的完整流程",
                stepCount = 4,
                tags = listOf("navigation", "back", "stack"),
                previewXml = """<?xml version="1.0" encoding="UTF-8"?>
<Journey name="navigation_flow" version="1.0">
    <Step id="1" type="launch"><Package>com.example.app</Package></Step>
    <Step id="2" type="click"><Target>#settings_icon</Target></Step>
    <Step id="3" type="assert"><Target>#settings_page</Target><Expected>visible</Expected></Step>
    <Step id="4" type="navigate"><Direction>back</Direction></Step>
</Journey>""",
                isBuiltIn = true
            )
        )
        _state.value = _state.value.copy(templates = mockTemplates)
    }

    /**
     * Load mock framework capability comparisons.
     */
    private fun loadMockFrameworkCapabilities() {
        val capabilities = listOf(
            FrameworkCapability("AI 驱动的步骤理解 / AI-driven step understanding",
                SupportLevel.Full, SupportLevel.None, SupportLevel.None),
            FrameworkCapability("视觉反馈验证 / Visual feedback verification",
                SupportLevel.Full, SupportLevel.Partial, SupportLevel.Partial),
            FrameworkCapability("无需元素定位器 / No element locators required",
                SupportLevel.Full, SupportLevel.None, SupportLevel.None),
            FrameworkCapability("跨 App 自动化 / Cross-app automation",
                SupportLevel.Full, SupportLevel.None, SupportLevel.Full),
            FrameworkCapability("CI/CD 友好 / CI/CD friendly",
                SupportLevel.Full, SupportLevel.Full, SupportLevel.Full),
            FrameworkCapability("执行速度 / Execution speed",
                SupportLevel.Partial, SupportLevel.Full, SupportLevel.Partial),
            FrameworkCapability("稳定性 / Stability",
                SupportLevel.Partial, SupportLevel.Full, SupportLevel.Partial),
            FrameworkCapability("截图对比 / Screenshot comparison",
                SupportLevel.Full, SupportLevel.Partial, SupportLevel.Partial),
            FrameworkCapability("地理定位模拟 / Geolocation mocking",
                SupportLevel.None, SupportLevel.Partial, SupportLevel.Full),
            FrameworkCapability("网络条件模拟 / Network condition mocking",
                SupportLevel.Partial, SupportLevel.Full, SupportLevel.Full)
        )
        _state.value = _state.value.copy(frameworkCapabilities = capabilities)
    }

    /**
     * Generate mock steps for a given journey.
     */
    private fun generateMockStepsForJourney(journey: JourneyItem): List<JourneyStep> {
        return when (journey.id) {
            "j1" -> listOf(
                JourneyStep("s1", 1, "启动应用 / Launch App", "launch", "com.example.app"),
                JourneyStep("s2", 2, "输入用户名 / Input username", "input", "#username", "{\"value\": \"testuser\"}"),
                JourneyStep("s3", 3, "输入密码 / Input password", "input", "#password", "{\"value\": \"****\"}"),
                JourneyStep("s4", 4, "点击登录按钮 / Click login", "click", "#login_button")
            )
            "j2" -> listOf(
                JourneyStep("s1", 1, "启动应用 / Launch App", "launch", "com.example.app"),
                JourneyStep("s2", 2, "点击购物车 / Click cart", "click", "#cart_icon"),
                JourneyStep("s3", 3, "点击结算 / Click checkout", "click", "#checkout_button"),
                JourneyStep("s4", 4, "输入支付信息 / Enter payment", "input", "#card_number", "{\"value\": \"4111111111111111\"}"),
                JourneyStep("s5", 5, "确认支付 / Confirm payment", "click", "#confirm_payment"),
                JourneyStep("s6", 6, "验证成功 / Verify success", "assert", "#success_message")
            )
            "j3" -> listOf(
                JourneyStep("s1", 1, "启动应用 / Launch App", "launch", "com.example.app"),
                JourneyStep("s2", 2, "点击搜索框 / Click search", "click", "#search_box"),
                JourneyStep("s3", 3, "输入搜索词 / Input query", "input", "#search_input", "{\"value\": \"iPhone\"}"),
                JourneyStep("s4", 4, "点击搜索按钮 / Click search", "click", "#search_submit"),
                JourneyStep("s5", 5, "选择第一个结果 / Select first result", "click", "#result_item_0")
            )
            else -> listOf(
                JourneyStep("s1", 1, "步骤 1 / Step 1", "launch", "com.example.app"),
                JourneyStep("s2", 2, "步骤 2 / Step 2", "click", "#button"),
                JourneyStep("s3", 3, "步骤 3 / Step 3", "assert", "#element")
            )
        }
    }

    /**
     * Generate mock XML for a given journey.
     */
    private fun generateMockXml(journey: JourneyItem): String {
        return """<?xml version="1.0" encoding="UTF-8"?>
<!-- Journey: ${journey.name} -->
<!-- Generated by Journeys E2E Testing Toolkit -->
<!-- Last run: ${journey.lastRunTime} -->
<Journey name="${journey.name}" version="1.0">
${generateMockStepsForJourney(journey).joinToString("\n") { step ->
    "    <Step id=\"${step.id}\" type=\"${step.actionType}\" description=\"${step.description}\">\n" +
    "        <Target>${step.target}</Target>\n" +
    "    </Step>"
}}
</Journey>"""
    }

    private fun getLoginFlowXml() = """<?xml version="1.0" encoding="UTF-8"?>
<Journey name="login_flow" version="1.0">
    <Step id="1" type="launch"><Package>com.example.app</Package></Step>
    <Step id="2" type="input"><Target>#username</Target><Value>user@example.com</Value></Step>
    <Step id="3" type="input"><Target>#password</Target><Value>********</Value></Step>
    <Step id="4" type="click"><Target>#login_button</Target></Step>
</Journey>"""

    private fun getPaymentFlowXml() = """<?xml version="1.0" encoding="UTF-8"?>
<Journey name="payment_flow" version="1.0">
    <Step id="1" type="launch"><Package>com.example.app</Package></Step>
    <Step id="2" type="click"><Target>#cart_icon</Target></Step>
    <Step id="3" type="click"><Target>#checkout_button</Target></Step>
    <Step id="4" type="input"><Target>#card_number</Target><Value>4111111111111111</Value></Step>
    <Step id="5" type="input"><Target>#expiry</Target><Value>12/28</Value></Step>
    <Step id="6" type="input"><Target>#cvv</Target><Value>123</Value></Step>
    <Step id="7" type="click"><Target>#pay_now</Target></Step>
</Journey>"""

    private fun getSearchFlowXml() = """<?xml version="1.0" encoding="UTF-8"?>
<Journey name="search_flow" version="1.0">
    <Step id="1" type="launch"><Package>com.example.app</Package></Step>
    <Step id="2" type="click"><Target>#search_box</Target></Step>
    <Step id="3" type="input"><Target>#search_input</Target><Value>iPhone</Value></Step>
    <Step id="4" type="click"><Target>#search_submit</Target></Step>
    <Step id="5" type="click"><Target>#result_item_0</Target></Step>
</Journey>"""

    private fun getDefaultFlowXml(nlText: String) = """<?xml version="1.0" encoding="UTF-8"?>
<!-- Generated from: $nlText -->
<Journey name="generated_journey" version="1.0">
    <Step id="1" type="launch"><Package>com.example.app</Package></Step>
    <Step id="2" type="click"><Target>#next_button</Target></Step>
    <Step id="3" type="assert"><Target>#success_message</Target></Step>
</Journey>"""

    private fun getRandomErrorMessage(): String {
        val errors = listOf(
            "Element not found: target selector returned null / 元素未找到：目标选择器返回空",
            "Timeout: element did not appear within 30 seconds / 超时：元素在30秒内未出现",
            "Action blocked: element is obscured by overlay / 操作被阻止：元素被遮罩层遮挡",
            "App crashed during step execution / 应用在执行步骤时崩溃",
            "Navigation failed: back stack inconsistent / 导航失败：返回栈不一致",
            "Assertion failed: expected text not found / 断言失败：未找到预期文本"
        )
        return errors.random()
    }

    // ================================================================
    // CI YAML Generators / CI YAML 生成器
    // ================================================================

    /**
     * Generate GitHub Actions YAML for Journey E2E tests.
     * @return GitHub Actions workflow YAML string
     */
    private fun getGitHubActionsYaml(): String = """
name: Journeys E2E Tests
on:
  push:
    branches: [ main, develop ]
  pull_request:
    branches: [ main, develop ]

jobs:
  journey-tests:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4

      - name: Set up JDK 17
        uses: actions/setup-java@v4
        with:
          distribution: 'temurin'
          java-version: '17'

      - name: Grant Gradle execute permission
        run: chmod +x gradlew

      - name: Run Journeys E2E tests
        run: ./gradlew runJourneys

      - name: Upload Journey results
        uses: actions/upload-artifact@v4
        if: always()
        with:
          name: journey-results
          path: app/build/journeys/results/
          retention-days: 30

      # - name: Publish Journey Report (optional, uncomment if GITHUB_STEP_SUMMARY is needed)
""".trimIndent()

    /**
     * Generate GitLab CI YAML for Journey E2E tests.
     * @return GitLab CI configuration YAML string
     */
    private fun getGitLabCIYaml(): String = """
variables:
  GRADLE_OPTS: "-Dorg.gradle.daemon=false -Dorg.gradle.logging.level=warn"

journey-e2e:
  stage: test
  image: registry.gitlab.com/android-ci/android:17
  script:
    - chmod +x gradlew
    - ./gradlew runJourneys
  artifacts:
    when: always
    paths:
      - app/build/journeys/results/
    expire_in: 30 days
  coverage: '/Total.*?([0-9]{1,3})%/'
""".trimIndent()

    /**
     * Generate Jenkins pipeline for Journey E2E tests.
     * @return Jenkins pipeline Groovy script string
     */
    private fun getJenkinsYaml(): String = """
pipeline {
    agent {
        label 'android && jdk17'
    }
    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }
        stage('Journey E2E Tests') {
            steps {
                sh './gradlew runJourneys'
            }
        }
        stage('Archive Results') {
            steps {
                archiveArtifacts artifacts: 'app/build/journeys/results/**', fingerprint: true
            }
        }
    }
    post {
        always {
            junit 'app/build/journeys/results/*.xml'
        }
    }
}
""".trimIndent()
}
