package com.mvi.kenny.feature.journeys

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

// =============================================================
// JourneysViewModel — Journeys E2E 测试工具包 ViewModel
// PRD-099 | Journeys for Android Studio 自动化 E2E 测试工具包
// =============================================================
/**
 * ViewModel for Journeys E2E Testing Toolkit
 * Manages all state transitions following MVI pattern.
 *
 * Key responsibilities:
 * - Load and manage Journey test case list
 * - Parse and edit Journey XML files
 * - Execute Journey tests and display results
 * - Generate Journey XML from natural language descriptions
 * - Generate CI/CD configuration YAML
 *
 * @see JourneysContract For State, Intent, Effect definitions
 */
class JourneysViewModel : ViewModel() {

    // ---------------------------------------------------------
    // State — single source of truth for UI
    // ---------------------------------------------------------
    private val _state = MutableStateFlow(JourneysState.Initial)
    val state: StateFlow<JourneysState> = _state.asStateFlow()

    // ---------------------------------------------------------
    // Effect — one-time events for UI
    // ---------------------------------------------------------
    private val _effect = MutableSharedFlow<JourneysEffect>()
    val effect: SharedFlow<JourneysEffect> = _effect.asSharedFlow()

    // ---------------------------------------------------------
    // Internal state
    // ---------------------------------------------------------
    private var runJob: Job? = null

    init {
        processIntent(JourneysIntent.LoadJourneys)
    }

    // ============================================================
    // processIntent — Intent 处理器
    // ============================================================
    fun processIntent(intent: JourneysIntent) {
        when (intent) {
            is JourneysIntent.LoadJourneys -> handleLoadJourneys()
            is JourneysIntent.OpenJourney -> handleOpenJourney(intent.journeyId)
            is JourneysIntent.CreateJourney -> handleCreateJourney(intent.templateId)
            is JourneysIntent.SaveJourney -> handleSaveJourney(intent.journeyId, intent.xml)
            is JourneysIntent.DeleteJourney -> handleDeleteJourney(intent.journeyId)
            is JourneysIntent.RunJourney -> handleRunJourney(intent.journeyId)
            is JourneysIntent.RunSingleStep -> handleRunSingleStep(intent.journeyId, intent.stepId)
            is JourneysIntent.GenerateFromNL -> handleGenerateFromNL(intent.description)
            is JourneysIntent.InsertGeneratedJourney -> handleInsertGeneratedJourney(intent.xml)
            is JourneysIntent.SelectTemplate -> handleSelectTemplate(intent.templateId)
            is JourneysIntent.SelectCIPlatform -> handleSelectCIPlatform(intent.platform)
            is JourneysIntent.FilterJourneys -> handleFilterJourneys(intent.status)
            is JourneysIntent.AddStep -> handleAddStep(intent.afterStepId, intent.step)
            is JourneysIntent.DeleteStep -> handleDeleteStep(intent.stepId)
            is JourneysIntent.ReorderSteps -> handleReorderSteps(intent.fromIndex, intent.toIndex)
            is JourneysIntent.UpdateStep -> handleUpdateStep(intent.step)
            is JourneysIntent.SetEditorMode -> handleSetEditorMode(intent.mode)
            is JourneysIntent.UpdateJourneyXml -> handleUpdateJourneyXml(intent.xml)
            is JourneysIntent.ClearExecutionResult -> handleClearExecutionResult()
            is JourneysIntent.DismissError -> handleDismissError()
        }
    }

    // ============================================================
    // Handler Methods — 意图处理方法
    // ============================================================

    /** Load all Journey items / 加载所有 Journey 项 */
    private fun handleLoadJourneys() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            delay(500) // Simulate async file loading

            val mockJourneys = listOf(
                JourneyItem(
                    id = "j001",
                    name = "login_journey",
                    filePath = "app/src/test/journeys/login_journey.xml",
                    status = StepStatus.PASSED,
                    lastRunTime = System.currentTimeMillis() - 2 * 60 * 1000,
                    lastRunDurationMs = 12500,
                    stepCount = 5,
                    passRate = 0.92f
                ),
                JourneyItem(
                    id = "j002",
                    name = "checkout_flow",
                    filePath = "app/src/test/journeys/checkout_flow.xml",
                    status = StepStatus.FAILED,
                    lastRunTime = System.currentTimeMillis() - 5 * 60 * 1000,
                    lastRunDurationMs = 8700,
                    stepCount = 8,
                    passRate = 0.65f
                ),
                JourneyItem(
                    id = "j003",
                    name = "search_product",
                    filePath = "app/src/test/journeys/search_product.xml",
                    status = StepStatus.RUNNING,
                    lastRunTime = System.currentTimeMillis() - 1 * 60 * 1000,
                    lastRunDurationMs = 0,
                    stepCount = 4,
                    passRate = 0.88f
                ),
                JourneyItem(
                    id = "j004",
                    name = "user_profile_update",
                    filePath = "app/src/test/journeys/user_profile_update.xml",
                    status = StepStatus.PASSED,
                    lastRunTime = System.currentTimeMillis() - 30 * 60 * 1000,
                    lastRunDurationMs = 6200,
                    stepCount = 6,
                    passRate = 0.95f
                ),
                JourneyItem(
                    id = "j005",
                    name = "payment_flow",
                    filePath = "app/src/test/journeys/payment_flow.xml",
                    status = StepStatus.PASSED,
                    lastRunTime = System.currentTimeMillis() - 60 * 60 * 1000,
                    lastRunDurationMs = 15200,
                    stepCount = 7,
                    passRate = 0.78f
                )
            )
            val templates = getDefaultTemplates()

            _state.update {
                it.copy(journeys = mockJourneys, templates = templates, isLoading = false)
            }
        }
    }

    /** Open a specific Journey / 打开指定的 Journey */
    private fun handleOpenJourney(journeyId: String) {
        viewModelScope.launch {
            val journey = _state.value.journeys.find { it.id == journeyId }
            if (journey != null) {
                val steps = parseJourneyXml(journeyId)
                _state.update {
                    it.copy(
                        activeJourneyId = journeyId,
                        activeJourneySteps = steps,
                        editorMode = EditorMode.VIEW,
                        currentJourneyXml = generateJourneyXml(steps)
                    )
                }
                _effect.emit(JourneysEffect.NavigateToEditor(journeyId))
            } else {
                _effect.emit(JourneysEffect.ShowError("Journey not found / Journey 未找到: $journeyId"))
            }
        }
    }

    /** Create a new Journey / 新建 Journey */
    private fun handleCreateJourney(templateId: String?) {
        viewModelScope.launch {
            val newId = "j${UUID.randomUUID().toString().take(8)}"
            val steps = if (templateId != null) {
                val template = _state.value.templates.find { it.id == templateId }
                template?.let { parseTemplateXml(it.xmlContent) } ?: emptyList()
            } else {
                emptyList()
            }
            _state.update {
                it.copy(
                    activeJourneyId = newId,
                    activeJourneySteps = steps,
                    editorMode = EditorMode.CREATE,
                    currentJourneyXml = generateJourneyXml(steps)
                )
            }
            _effect.emit(JourneysEffect.NavigateToEditor(null))
        }
    }

    /** Save Journey XML / 保存 Journey XML */
    private fun handleSaveJourney(journeyId: String, xml: String) {
        viewModelScope.launch {
            _state.update { it.copy(currentJourneyXml = xml) }
            _effect.emit(JourneysEffect.JourneySaved(journeyId))
            _effect.emit(JourneysEffect.ShowToast("Journey saved / Journey 已保存"))
        }
    }

    /** Delete a Journey / 删除 Journey */
    private fun handleDeleteJourney(journeyId: String) {
        viewModelScope.launch {
            _state.update { it.copy(journeys = it.journeys.filter { j -> j.id != journeyId }) }
            _effect.emit(JourneysEffect.ShowToast("Journey deleted / Journey 已删除"))
        }
    }

    /** Run a Journey / 运行 Journey */
    private fun handleRunJourney(journeyId: String) {
        runJob?.cancel()
        runJob = viewModelScope.launch {
            _state.update { it.copy(isRunning = true, activeJourneyId = journeyId) }

            val journey = _state.value.journeys.find { it.id == journeyId }
            val steps = parseJourneyXml(journeyId)
            val startTime = System.currentTimeMillis()

            _state.update {
                it.copy(activeJourneySteps = steps.map { s -> s.copy(status = StepStatus.RUNNING) })
            }

            val stepResults = mutableListOf<StepResult>()
            for ((index, step) in steps.withIndex()) {
                delay(800L + (index * 200L))

                val status = if (index == 2 && journeyId == "j002") StepStatus.FAILED else StepStatus.PASSED
                val stepResult = StepResult(
                    step = step.copy(status = status),
                    status = status,
                    startTime = startTime + index * 1000L,
                    endTime = startTime + (index + 1) * 1000L,
                    durationMs = 800L + index * 200L,
                    screenshotPath = if (status == StepStatus.FAILED) "app/build/journeys/results/${journeyId}_step${index + 1}.png" else null,
                    errorMessage = if (status == StepStatus.FAILED) "Element not found: ${step.target}" else null,
                    expectedResult = if (status == StepStatus.FAILED) "Click \"${step.target}\"" else null,
                    actualResult = if (status == StepStatus.FAILED) "Page shows loading animation, button obscured" else null
                )
                stepResults.add(stepResult)

                _state.update { currentState ->
                    currentState.copy(
                        activeJourneySteps = currentState.activeJourneySteps.mapIndexed { i, s ->
                            if (i == index) s.copy(status = status) else s
                        }
                    )
                }
            }

            val endTime = System.currentTimeMillis()
            val overallStatus = if (stepResults.any { it.status == StepStatus.FAILED }) StepStatus.FAILED else StepStatus.PASSED

            val executionResult = ExecutionResult(
                journeyId = journeyId,
                journeyName = journey?.name ?: "Unknown",
                overallStatus = overallStatus,
                startTime = startTime,
                endTime = endTime,
                durationMs = endTime - startTime,
                steps = stepResults,
                failedStepIndex = stepResults.indexOfFirst { it.status == StepStatus.FAILED },
                screenshotPaths = stepResults.mapIndexedNotNull { idx, r -> r.screenshotPath?.let { idx to it } }.toMap()
            )

            _state.update { it.copy(isRunning = false, executionResult = executionResult) }
            _effect.emit(JourneysEffect.JourneyRunCompleted(journeyId, executionResult))
        }
    }

    /** Run a single step / 运行单个步骤 */
    private fun handleRunSingleStep(journeyId: String, stepId: String) {
        viewModelScope.launch {
            _state.update { it.copy(isRunning = true) }
            delay(1500)
            _state.update {
                it.copy(
                    isRunning = false,
                    activeJourneySteps = it.activeJourneySteps.map { s ->
                        if (s.id == stepId) s.copy(status = StepStatus.PASSED) else s
                    }
                )
            }
            _effect.emit(JourneysEffect.ShowToast("Step executed / 步骤已执行"))
        }
    }

    /** Generate Journey XML from natural language / 从自然语言生成 Journey XML */
    private fun handleGenerateFromNL(description: String) {
        viewModelScope.launch {
            _state.update { it.copy(isGeneratingNL = true) }
            delay(2000)
            val xml = generateXmlFromNl(description)
            _state.update { it.copy(isGeneratingNL = false, nlGenerationResult = xml) }
            _effect.emit(JourneysEffect.NlGenerationCompleted(xml))
        }
    }

    /** Insert generated Journey XML into editor / 将生成的 XML 插入编辑器 */
    private fun handleInsertGeneratedJourney(xml: String) {
        viewModelScope.launch {
            _state.update { it.copy(currentJourneyXml = xml, nlGenerationResult = null, editorMode = EditorMode.EDIT) }
        }
    }

    /** Select a template / 选择模板 */
    private fun handleSelectTemplate(templateId: String) {
        viewModelScope.launch {
            val template = _state.value.templates.find { it.id == templateId }
            if (template != null) {
                val steps = parseTemplateXml(template.xmlContent)
                _state.update {
                    it.copy(
                        activeJourneySteps = steps,
                        currentJourneyXml = template.xmlContent,
                        editorMode = EditorMode.CREATE
                    )
                }
                _effect.emit(JourneysEffect.NavigateToEditor(null))
            }
        }
    }

    /** Select CI platform / 选择 CI 平台 */
    private fun handleSelectCIPlatform(platform: CIPlatform) {
        _state.update { it.copy(selectedCIPlatform = platform) }
    }

    /** Filter Journey list by status / 按状态筛选 Journey 列表 */
    private fun handleFilterJourneys(status: FilterStatus) {
        _state.update { it.copy(filterStatus = status) }
    }

    /** Add a new step / 添加新步骤 */
    private fun handleAddStep(afterStepId: String?, step: JourneyStep) {
        _state.update { currentState ->
            val steps = currentState.activeJourneySteps.toMutableList()
            if (afterStepId == null) {
                steps.add(0, step)
            } else {
                val index = steps.indexOfFirst { it.id == afterStepId }
                if (index >= 0) steps.add(index + 1, step) else steps.add(step)
            }
            currentState.copy(activeJourneySteps = steps, currentJourneyXml = generateJourneyXml(steps))
        }
    }

    /** Delete a step / 删除步骤 */
    private fun handleDeleteStep(stepId: String) {
        _state.update { currentState ->
            val steps = currentState.activeJourneySteps.filter { it.id != stepId }
            currentState.copy(activeJourneySteps = steps, currentJourneyXml = generateJourneyXml(steps))
        }
    }

    /** Reorder steps / 重新排序步骤 */
    private fun handleReorderSteps(fromIndex: Int, toIndex: Int) {
        _state.update { currentState ->
            val steps = currentState.activeJourneySteps.toMutableList()
            if (fromIndex in steps.indices && toIndex in steps.indices) {
                val item = steps.removeAt(fromIndex)
                steps.add(toIndex, item)
            }
            currentState.copy(activeJourneySteps = steps, currentJourneyXml = generateJourneyXml(steps))
        }
    }

    /** Update step details / 更新步骤详情 */
    private fun handleUpdateStep(step: JourneyStep) {
        _state.update { currentState ->
            val steps = currentState.activeJourneySteps.map { s -> if (s.id == step.id) step else s }
            currentState.copy(activeJourneySteps = steps, currentJourneyXml = generateJourneyXml(steps))
        }
    }

    /** Set editor mode / 设置编辑器模式 */
    private fun handleSetEditorMode(mode: EditorMode) {
        _state.update { it.copy(editorMode = mode) }
    }

    /** Update Journey XML content / 更新 Journey XML 内容 */
    private fun handleUpdateJourneyXml(xml: String) {
        _state.update { it.copy(currentJourneyXml = xml) }
    }

    /** Clear execution result / 清除执行结果 */
    private fun handleClearExecutionResult() {
        _state.update { it.copy(executionResult = null) }
    }

    /** Dismiss error / 关闭错误提示 */
    private fun handleDismissError() {
        _state.update { it.copy(errorMessage = null) }
    }

    // ============================================================
    // Helper Methods — 辅助方法
    // ============================================================

    /** Get default Journey templates / 获取默认 Journey 模板 */
    private fun getDefaultTemplates(): List<JourneyTemplate> = listOf(
        JourneyTemplate(
            id = "tpl_login",
            name = "标准登录流程 / Standard Login",
            category = TemplateCategory.LOGIN_FLOW,
            description = "App 启动 → 输入账号 → 输入密码 → 点击登录 → 验证跳转首页",
            xmlContent = generateLoginTemplateXml(),
            tags = listOf("login", "authentication", "signin")
        ),
        JourneyTemplate(
            id = "tpl_shopping",
            name = "购物流程 / Shopping Flow",
            category = TemplateCategory.SHOPPING_FLOW,
            description = "搜索商品 → 点击商品 → 加入购物车 → 去结算 → 确认订单",
            xmlContent = generateShoppingTemplateXml(),
            tags = listOf("shopping", "cart", "checkout", "ecommerce")
        ),
        JourneyTemplate(
            id = "tpl_form",
            name = "表单填写 / Form Filling",
            category = TemplateCategory.FORM_FILLING,
            description = "打开表单 → 填写多字段 → 表单验证 → 提交",
            xmlContent = generateFormTemplateXml(),
            tags = listOf("form", "input", "validation", "submission")
        ),
        JourneyTemplate(
            id = "tpl_deep_link",
            name = "深度链接跳转 / Deep Link",
            category = TemplateCategory.DEEP_LINK,
            description = "触发 deep link → 验证 App 启动 → 验证目标页面",
            xmlContent = generateDeepLinkTemplateXml(),
            tags = listOf("deeplink", "urischeme", "applink")
        ),
        JourneyTemplate(
            id = "tpl_payment",
            name = "支付流程 / Payment Flow",
            category = TemplateCategory.PAYMENT_FLOW,
            description = "进入支付页 → 选择支付方式 → 确认支付 → 验证结果",
            xmlContent = generatePaymentTemplateXml(),
            tags = listOf("payment", "checkout", "transaction")
        ),
        JourneyTemplate(
            id = "tpl_navigation",
            name = "导航流程 / Navigation Flow",
            category = TemplateCategory.NAVIGATION_FLOW,
            description = "底部导航切换 → 各 Tab 内容加载 → 验证内容正确性",
            xmlContent = generateNavigationTemplateXml(),
            tags = listOf("navigation", "tabs", "bottomnav")
        )
    )

    /** Parse Journey XML into steps / 将 Journey XML 解析为步骤 */
    private fun parseJourneyXml(journeyId: String): List<JourneyStep> {
        return when (journeyId) {
            "j001" -> listOf(
                JourneyStep("s1", "launch", "启动 App / Launch App", "com.example.app"),
                JourneyStep("s2", "input", "输入账号 / Enter username", "#username", "testuser@example.com"),
                JourneyStep("s3", "input", "输入密码 / Enter password", "#password", "Test1234!"),
                JourneyStep("s4", "click", "点击登录按钮 / Click login", "#login_button"),
                JourneyStep("s5", "assert", "验证跳转首页 / Verify home page", "#home_content")
            )
            "j002" -> listOf(
                JourneyStep("s1", "launch", "启动 App / Launch App", "com.example.app"),
                JourneyStep("s2", "click", "点击商品 / Click product", "#product_item_1"),
                JourneyStep("s3", "click", "加入购物车 / Add to cart", "#add_to_cart"),
                JourneyStep("s4", "click", "点击购物车图标 / Click cart icon", "#cart_icon"),
                JourneyStep("s5", "click", "点击结算 / Click checkout", "#checkout_button"),
                JourneyStep("s6", "input", "输入地址 / Enter address", "#address_input", "123 Main St"),
                JourneyStep("s7", "click", "选择支付方式 / Select payment", "#payment_method"),
                JourneyStep("s8", "click", "确认支付 / Confirm payment", "#confirm_payment")
            )
            "j003" -> listOf(
                JourneyStep("s1", "launch", "启动 App / Launch App", "com.example.app"),
                JourneyStep("s2", "click", "点击搜索框 / Click search", "#search_box"),
                JourneyStep("s3", "input", "输入商品 / Enter product", "#search_input", "iPhone"),
                JourneyStep("s4", "click", "点击搜索 / Click search", "#search_button")
            )
            "j004" -> listOf(
                JourneyStep("s1", "launch", "启动 App / Launch App", "com.example.app"),
                JourneyStep("s2", "click", "点击头像 / Click avatar", "#avatar"),
                JourneyStep("s3", "click", "点击编辑 / Click edit", "#edit_profile"),
                JourneyStep("s4", "input", "输入昵称 / Enter nickname", "#nickname_input", "NewNickname"),
                JourneyStep("s5", "click", "保存 / Save", "#save_button"),
                JourneyStep("s6", "assert", "验证保存成功 / Verify save success", "#success_message")
            )
            "j005" -> listOf(
                JourneyStep("s1", "launch", "启动 App / Launch App", "com.example.app"),
                JourneyStep("s2", "click", "选择商品 / Select product", "#product_1"),
                JourneyStep("s3", "click", "加入购物车 / Add to cart", "#add_to_cart"),
                JourneyStep("s4", "input", "输入优惠码 / Enter coupon", "#coupon_input", "SAVE10"),
                JourneyStep("s5", "click", "确认优惠 / Apply coupon", "#apply_coupon"),
                JourneyStep("s6", "click", "去支付 / Go to payment", "#go_to_payment"),
                JourneyStep("s7", "assert", "验证支付页 / Verify payment page", "#payment_page")
            )
            else -> emptyList()
        }
    }

    /** Parse template XML into steps / 将模板 XML 解析为步骤 */
    private fun parseTemplateXml(xml: String): List<JourneyStep> {
        return listOf(
            JourneyStep(
                id = UUID.randomUUID().toString().take(8),
                actionType = "launch",
                description = "启动 App / Launch App",
                target = "com.example.app"
            )
        )
    }

    /** Generate Journey XML from steps / 从步骤生成 Journey XML */
    private fun generateJourneyXml(steps: List<JourneyStep>): String {
        if (steps.isEmpty()) {
            return """<?xml version="1.0" encoding="UTF-8"?>
<Journey>
  <!-- Empty Journey / 空 Journey -->
</Journey>"""
        }
        val stepsXml = steps.joinToString("\n") { step ->
            """    <Step id="${step.id}">
        <Action type="${step.actionType}">
            <Target>${step.target}</Target>
            ${if (step.value.isNotEmpty()) "<Value>${step.value}</Value>" else ""}
        </Action>
        <Description>${step.description}</Description>
        <Timeout>${step.timeoutMs}</Timeout>
    </Step>"""
        }
        return """<?xml version="1.0" encoding="UTF-8"?>
<Journey>
$stepsXml
</Journey>"""
    }

    /** Generate XML from natural language / 从自然语言生成 XML */
    private fun generateXmlFromNl(description: String): String {
        val step = JourneyStep(
            id = UUID.randomUUID().toString().take(8),
            actionType = "click",
            description = description,
            target = "#element"
        )
        return generateJourneyXml(listOf(step))
    }

    // ============================================================
    // Template XML Generators — 模板 XML 生成器
    // ============================================================

    private fun generateLoginTemplateXml(): String = """<?xml version="1.0" encoding="UTF-8"?>
<Journey name="Standard Login / 标准登录">
    <Step id="l1">
        <Action type="launch">
            <Target>com.example.app</Target>
        </Action>
        <Description>启动 App / Launch App</Description>
    </Step>
    <Step id="l2">
        <Action type="input">
            <Target>#username</Target>
            <Value>user@example.com</Value>
        </Action>
        <Description>输入账号 / Enter username</Description>
    </Step>
    <Step id="l3">
        <Action type="input">
            <Target>#password</Target>
            <Value>password123</Value>
        </Action>
        <Description>输入密码 / Enter password</Description>
    </Step>
    <Step id="l4">
        <Action type="click">
            <Target>#login_button</Target>
        </Action>
        <Description>点击登录 / Click login</Description>
    </Step>
    <Step id="l5">
        <Action type="assert">
            <Target>#home_content</Target>
        </Action>
        <Description>验证首页 / Verify home page</Description>
    </Step>
</Journey>"""

    private fun generateShoppingTemplateXml(): String = """<?xml version="1.0" encoding="UTF-8"?>
<Journey name="Shopping Flow / 购物流程">
    <Step id="s1">
        <Action type="launch">
            <Target>com.example.app</Target>
        </Action>
        <Description>启动 App / Launch App</Description>
    </Step>
    <Step id="s2">
        <Action type="click">
            <Target>#search_box</Target>
        </Action>
        <Description>点击搜索框 / Click search</Description>
    </Step>
    <Step id="s3">
        <Action type="input">
            <Target>#search_input</Target>
            <Value>product name</Value>
        </Action>
        <Description>输入商品名称 / Enter product name</Description>
    </Step>
    <Step id="s4">
        <Action type="click">
            <Target>#search_button</Target>
        </Action>
        <Description>点击搜索 / Click search</Description>
    </Step>
    <Step id="s5">
        <Action type="click">
            <Target>#first_product</Target>
        </Action>
        <Description>点击第一个商品 / Click first product</Description>
    </Step>
    <Step id="s6">
        <Action type="click">
            <Target>#add_to_cart</Target>
        </Action>
        <Description>加入购物车 / Add to cart</Description>
    </Step>
    <Step id="s7">
        <Action type="click">
            <Target>#cart_icon</Target>
        </Action>
        <Description>打开购物车 / Open cart</Description>
    </Step>
    <Step id="s8">
        <Action type="click">
            <Target>#checkout_button</Target>
        </Action>
        <Description>去结算 / Go to checkout</Description>
    </Step>
</Journey>"""

    private fun generateFormTemplateXml(): String = """<?xml version="1.0" encoding="UTF-8"?>
<Journey name="Form Filling / 表单填写">
    <Step id="f1">
        <Action type="launch">
            <Target>com.example.app</Target>
        </Action>
        <Description>启动 App / Launch App</Description>
    </Step>
    <Step id="f2">
        <Action type="click">
            <Target>#form_button</Target>
        </Action>
        <Description>打开表单 / Open form</Description>
    </Step>
    <Step id="f3">
        <Action type="input">
            <Target>#field_name</Target>
            <Value>John Doe</Value>
        </Action>
        <Description>输入姓名 / Enter name</Description>
    </Step>
    <Step id="f4">
        <Action type="input">
            <Target>#field_email</Target>
            <Value>john@example.com</Value>
        </Action>
        <Description>输入邮箱 / Enter email</Description>
    </Step>
    <Step id="f5">
        <Action type="input">
            <Target>#field_phone</Target>
            <Value>+1234567890</Value>
        </Action>
        <Description>输入电话 / Enter phone</Description>
    </Step>
    <Step id="f6">
        <Action type="click">
            <Target>#submit_button</Target>
        </Action>
        <Description>提交表单 / Submit form</Description>
    </Step>
    <Step id="f7">
        <Action type="assert">
            <Target>#success_message</Target>
        </Action>
        <Description>验证成功 / Verify success</Description>
    </Step>
</Journey>"""

    private fun generateDeepLinkTemplateXml(): String = """<?xml version="1.0" encoding="UTF-8"?>
<Journey name="Deep Link / 深度链接">
    <Step id="d1">
        <Action type="deeplink">
            <Target>example://product/123</Target>
        </Action>
        <Description>触发深度链接 / Trigger deep link</Description>
    </Step>
    <Step id="d2">
        <Action type="assert">
            <Target>#product_detail_page</Target>
        </Action>
        <Description>验证商品详情页 / Verify product detail page</Description>
    </Step>
    <Step id="d3">
        <Action type="assert">
            <Target>#product_id_123</Target>
        </Action>
        <Description>验证商品 ID / Verify product ID</Description>
    </Step>
</Journey>"""

    private fun generatePaymentTemplateXml(): String = """<?xml version="1.0" encoding="UTF-8"?>
<Journey name="Payment Flow / 支付流程">
    <Step id="p1">
        <Action type="launch">
            <Target>com.example.app</Target>
        </Action>
        <Description>启动 App / Launch App</Description>
    </Step>
    <Step id="p2">
        <Action type="click">
            <Target>#cart_icon</Target>
        </Action>
        <Description>打开购物车 / Open cart</Description>
    </Step>
    <Step id="p3">
        <Action type="click">
            <Target>#checkout_button</Target>
        </Action>
        <Description>去结算 / Go to checkout</Description>
    </Step>
    <Step id="p4">
        <Action type="click">
            <Target>#payment_method_creditcard</Target>
        </Action>
        <Description>选择信用卡 / Select credit card</Description>
    </Step>
    <Step id="p5">
        <Action type="input">
            <Target>#card_number</Target>
            <Value>4111111111111111</Value>
        </Action>
        <Description>输入卡号 / Enter card number</Description>
    </Step>
    <Step id="p6">
        <Action type="input">
            <Target>#card_expiry</Target>
            <Value>12/28</Value>
        </Action>
        <Description>输入有效期 / Enter expiry</Description>
    </Step>
    <Step id="p7">
        <Action type="input">
            <Target>#card_cvv</Target>
            <Value>123</Value>
        </Action>
        <Description>输入 CVV / Enter CVV</Description>
    </Step>
    <Step id="p8">
        <Action type="click">
            <Target>#confirm_payment</Target>
        </Action>
        <Description>确认支付 / Confirm payment</Description>
    </Step>
    <Step id="p9">
        <Action type="assert">
            <Target>#payment_success</Target>
        </Action>
        <Description>验证支付成功 / Verify payment success</Description>
    </Step>
</Journey>"""

    private fun generateNavigationTemplateXml(): String = """<?xml version="1.0" encoding="UTF-8"?>
<Journey name="Navigation Flow / 导航流程">
    <Step id="n1">
        <Action type="launch">
            <Target>com.example.app</Target>
        </Action>
        <Description>启动 App / Launch App</Description>
    </Step>
    <Step id="n2">
        <Action type="click">
            <Target>#tab_home</Target>
        </Action>
        <Description>点击首页 Tab / Click home tab</Description>
    </Step>
    <Step id="n3">
        <Action type="assert">
            <Target>#home_content</Target>
        </Action>
        <Description>验证首页内容 / Verify home content</Description>
    </Step>
    <Step id="n4">
        <Action type="click">
            <Target>#tab_search</Target>
        </Action>
        <Description>点击搜索 Tab / Click search tab</Description>
    </Step>
    <Step id="n5">
        <Action type="assert">
            <Target>#search_page</Target>
        </Action>
        <Description>验证搜索页 / Verify search page</Description>
    </Step>
    <Step id="n6">
        <Action type="click">
            <Target>#tab_profile</Target>
        </Action>
        <Description>点击我的 Tab / Click profile tab</Description>
    </Step>
    <Step id="n7">
        <Action type="assert">
            <Target>#profile_page</Target>
        </Action>
        <Description>验证个人页 / Verify profile page</Description>
    </Step>
</Journey>"""
}
