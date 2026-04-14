package com.mvi.kenny.feature.journeys

// ================================================================
// JourneysContract — Journeys E2E Testing Toolkit MVI Contract
// ================================================================
// MVI architecture contract for Journeys E2E Testing Toolkit.
//
// PRD-099: Journeys for Android Studio 自动化 E2E 测试工具包
// Design Reference: memory/agency/designs/PRD-099-Journeys-E2E测试工具包.md
//
// MVI 三要素 / Three pillars:
//   Model (State)  — Immutable page state, single source of truth
//   View           — Composable function, consumes State, renders UI
//   Intent         — User intentions, ViewModel processes and updates State
//   Effect         — One-time side effects (navigation, toast), via Channel
// ================================================================

/**
 * ============================================================
 * JourneyStatus — Journey 测试执行状态枚举
 * ================================================================
 * Represents the execution status of a Journey test case.
 */
enum class JourneyStatus {
    /** 全部 / All */
    All,
    /** 通过 / Passed */
    Passed,
    /** 失败 / Failed */
    Failed,
    /** 运行中 / Running */
    Running
}

/**
 * ============================================================
 * EditorMode — 编辑器模式枚举
 * ================================================================
 * Represents the current mode of the Journey editor.
 */
enum class EditorMode {
    /** 查看模式 */
    View,
    /** 编辑模式 */
    Edit,
    /** 创建模式 */
    Create
}

/**
 * ============================================================
 * CIPlatform — CI/CD 平台枚举
 * ================================================================
 * Supported CI/CD platforms for Journey test integration.
 */
enum class CIPlatform(val displayName: String) {
    GitHubActions("GitHub Actions"),
    GitLabCI("GitLab CI"),
    Jenkins("Jenkins")
}

/**
 * ============================================================
 * TemplateCategory — 模板分类枚举
 * ================================================================
 * Categories of Journey test templates.
 */
enum class TemplateCategory(val displayName: String) {
    LoginFlow("登录流程"),
    PaymentFlow("支付流程"),
    ListOperation("列表操作"),
    FormFilling("表单填写"),
    DeepLink("深度链接"),
    Navigation("导航流程")
}

/**
 * ============================================================
 * FrameworkType — 测试框架类型枚举
 * ================================================================
 * Types of testing frameworks available for comparison.
 */
enum class FrameworkType(val displayName: String) {
    Journey("Journey"),
    Espresso("Espresso"),
    UIAutomator("UIAutomator")
}

// ================================================================
// Data Models / 数据模型
// ================================================================

/**
 * ============================================================
 * JourneyItem — Journey 测试用例数据模型
 * ================================================================
 * Represents a single Journey test case.
 *
 * @param id Unique identifier
 * @param name Journey file name (without .xml extension)
 * @param filePath Absolute file path to the .xml file
 * @param status Execution status (Passed / Failed / Running)
 * @param lastRunTime Last execution timestamp (ISO 8601)
 * @param lastRunDurationMs Execution duration in milliseconds
 * @param stepCount Number of steps in this Journey
 * @param passedStepCount Number of passed steps
 * @param failedStepCount Number of failed steps
 * @param isRunning Whether this Journey is currently executing
 */
data class JourneyItem(
    val id: String,
    val name: String,
    val filePath: String,
    val status: JourneyStatus = JourneyStatus.Passed,
    val lastRunTime: String = "",
    val lastRunDurationMs: Long = 0L,
    val stepCount: Int = 0,
    val passedStepCount: Int = 0,
    val failedStepCount: Int = 0,
    val isRunning: Boolean = false
) {
    val durationDisplay: String
        get() = if (lastRunDurationMs > 0) {
            when {
                lastRunDurationMs < 1000 -> "${lastRunDurationMs}ms"
                lastRunDurationMs < 60000 -> "${lastRunDurationMs / 1000}s"
                else -> "${lastRunDurationMs / 60000}m ${(lastRunDurationMs % 60000) / 1000}s"
            }
        } else "-"

    val passRate: String
        get() = if (stepCount > 0) "${passedStepCount * 100 / stepCount}%" else "-"
}

/**
 * ============================================================
 * JourneyStep — Journey 测试步骤数据模型
 * ================================================================
 * Represents a single step within a Journey test.
 *
 * @param id Step unique identifier
 * @param order Step order (1-indexed)
 * @param description Human-readable step description
 * @param actionType Action type (launch / input / click / scroll / assert / navigate)
 * @param target Target element or resource (e.g., "#login_button", "com.example.app")
 * @param params Additional action parameters (JSON string)
 * @param expectedResult Expected outcome description
 * @param status Execution status (Pending / Running / Passed / Failed / Skipped)
 * @param durationMs Execution duration in milliseconds
 * @param screenshotPath Path to screenshot taken during execution (if any)
 * @param errorMessage Error message if failed
 */
data class JourneyStep(
    val id: String,
    val order: Int,
    val description: String,
    val actionType: String,
    val target: String = "",
    val params: String = "{}",
    val expectedResult: String = "",
    val status: StepStatus = StepStatus.Pending,
    val durationMs: Long = 0L,
    val screenshotPath: String? = null,
    val errorMessage: String? = null
)

/**
 * ============================================================
 * StepStatus — 步骤执行状态枚举
 * ================================================================
 */
enum class StepStatus {
    Pending,
    Running,
    Passed,
    Failed,
    Skipped
}

/**
 * ============================================================
 * JourneyTemplate — Journey 模板数据模型
 * ================================================================
 * Represents a reusable Journey test template.
 *
 * @param id Template unique identifier
 * @param name Template display name
 * @param category Template category
 * @param description Template description
 * @param stepCount Number of steps in this template
 * @param tags Tags for search/filter
 * @param previewXml Snippet of the Journey XML content
 * @param isBuiltIn Whether this is a built-in template
 */
data class JourneyTemplate(
    val id: String,
    val name: String,
    val category: TemplateCategory,
    val description: String,
    val stepCount: Int,
    val tags: List<String> = emptyList(),
    val previewXml: String,
    val isBuiltIn: Boolean = true
)

/**
 * ============================================================
 * ExecutionResult — Journey 执行结果数据模型
 * ================================================================
 * Represents the complete execution result of a Journey test.
 *
 * @param journeyId The Journey being executed
 * @param overallStatus Overall execution status
 * @param startTime Execution start time (ISO 8601)
 * @param endTime Execution end time (ISO 8601)
 * @param totalDurationMs Total execution duration in milliseconds
 * @param steps Execution results for each step
 * @param failedStepId ID of the first failed step (if any)
 * @param totalPassed Total passed steps
 * @param totalFailed Total failed steps
 * @param totalSkipped Total skipped steps
 * @param outputLog Full execution log output
 */
data class ExecutionResult(
    val journeyId: String,
    val overallStatus: JourneyStatus,
    val startTime: String,
    val endTime: String,
    val totalDurationMs: Long,
    val steps: List<JourneyStep>,
    val failedStepId: String? = null,
    val totalPassed: Int = 0,
    val totalFailed: Int = 0,
    val totalSkipped: Int = 0,
    val outputLog: String = ""
)

/**
 * ============================================================
 * FrameworkCapability — 框架能力对比数据模型
 * ================================================================
 * Represents a single capability comparison between frameworks.
 *
 * @param capabilityName Capability name (Chinese + English)
 * @param journeySupport Support level for Journey (full / partial / none)
 * @param espressoSupport Support level for Espresso
 * @param uiautoSupport Support level for UIAutomator
 */
data class FrameworkCapability(
    val capabilityName: String,
    val journeySupport: SupportLevel,
    val espressoSupport: SupportLevel,
    val uiautoSupport: SupportLevel
)

/**
 * ============================================================
 * SupportLevel — 能力支持级别枚举
 * ================================================================
 */
enum class SupportLevel(val symbol: String, val description: String) {
    Full("✅", "完全支持"),
    Partial("⚠️", "部分支持"),
    None("❌", "不支持")
}

/**
 * ============================================================
 * ActiveTab — 当前激活的子页面 Tab 枚举
 * ================================================================
 */
enum class ActiveTab {
    Dashboard,
    Editor,
    Templates,
    Results,
    CI,
    Comparison
}

// ================================================================
// State / 状态
// ================================================================

/**
 * ============================================================
 * JourneysState — Journeys E2E 工具包页面状态
 * ================================================================
 * Immutable UI state — single source of truth for the entire toolkit.
 *
 * Primary sub-states:
 *   - Dashboard: Journey list, stats overview, filters
 *   - Editor: Current Journey XML, step tree, NL generation
 *   - Templates: Available templates, selected template
 *   - Results: Execution results, timeline view
 *   - CI: CI platform selection, YAML generation
 *   - Comparison: Framework capability matrix
 */
data class JourneysState(
    // ─────────────────────────────────────────────────────────
    // Dashboard State / 仪表盘状态
    // ─────────────────────────────────────────────────────────
    val journeys: List<JourneyItem> = emptyList(),
    val filterStatus: JourneyStatus = JourneyStatus.All,
    val searchQuery: String = "",

    // ─────────────────────────────────────────────────────────
    // Editor State / 编辑器状态
    // ─────────────────────────────────────────────────────────
    val activeJourneyId: String? = null,
    val editorMode: EditorMode = EditorMode.View,
    val currentJourneyXml: String = "",
    val currentSteps: List<JourneyStep> = emptyList(),
    val selectedStepId: String? = null,
    val nlInputText: String = "",
    val nlGenerationResult: String? = null,
    val isGeneratingNL: Boolean = false,

    // ─────────────────────────────────────────────────────────
    // Template State / 模板状态
    // ─────────────────────────────────────────────────────────
    val templates: List<JourneyTemplate> = emptyList(),
    val selectedTemplateId: String? = null,
    val templateSearchQuery: String = "",
    val selectedCategory: TemplateCategory? = null,

    // ─────────────────────────────────────────────────────────
    // Execution State / 执行状态
    // ─────────────────────────────────────────────────────────
    val executionResult: ExecutionResult? = null,
    val isRunning: Boolean = false,
    val runningJourneyId: String? = null,
    val expandedStepId: String? = null,

    // ─────────────────────────────────────────────────────────
    // CI/CD State / CI/CD 配置状态
    // ─────────────────────────────────────────────────────────
    val selectedCIPlatform: CIPlatform = CIPlatform.GitHubActions,
    val generatedYAML: String = "",

    // ─────────────────────────────────────────────────────────
    // Framework Comparison State / 框架对比状态
    // ─────────────────────────────────────────────────────────
    val frameworkCapabilities: List<FrameworkCapability> = emptyList(),

    // ─────────────────────────────────────────────────────────
    // Navigation State / 导航状态
    // ─────────────────────────────────────────────────────────
    val activeTab: ActiveTab = ActiveTab.Dashboard,

    // ─────────────────────────────────────────────────────────
    // UI State / UI 状态
    // ─────────────────────────────────────────────────────────
    val isLoading: Boolean = false,
    val errorMessage: String? = null,

    // ─────────────────────────────────────────────────────────
    // Computed Properties / 计算属性
    // ─────────────────────────────────────────────────────────
) {
    /** Filtered journey list based on current filter and search query */
    val filteredJourneys: List<JourneyItem>
        get() = journeys.filter { journey ->
            val matchesFilter = filterStatus == JourneyStatus.All || journey.status == filterStatus
            val matchesSearch = searchQuery.isEmpty() || journey.name.contains(searchQuery, ignoreCase = true)
            matchesFilter && matchesSearch
        }

    /** Total count of journeys */
    val totalCount: Int get() = journeys.size

    /** Count of passed journeys */
    val passedCount: Int get() = journeys.count { it.status == JourneyStatus.Passed }

    /** Count of failed journeys */
    val failedCount: Int get() = journeys.count { it.status == JourneyStatus.Failed }

    /** Count of running journeys */
    val runningCount: Int get() = journeys.count { it.isRunning }

    /** Current editor journey item */
    val currentJourney: JourneyItem?
        get() = activeJourneyId?.let { id -> journeys.find { it.id == id } }

    /** Filtered templates based on search and category */
    val filteredTemplates: List<JourneyTemplate>
        get() = templates.filter { template ->
            val matchesSearch = templateSearchQuery.isEmpty() ||
                template.name.contains(templateSearchQuery, ignoreCase = true) ||
                template.description.contains(templateSearchQuery, ignoreCase = true)
            val matchesCategory = selectedCategory == null || template.category == selectedCategory
            matchesSearch && matchesCategory
        }
}

// ================================================================
// Intent / 意图
// ================================================================

/**
 * ============================================================
 * JourneysIntent — 用户操作意图
 * ================================================================
 * Sealed class representing all possible user intentions.
 * ViewModel receives intents via sendIntent() and processes them.
 */
sealed class JourneysIntent {
    // ─────────────────────────────────────────────────────────
    // Dashboard / 仪表盘
    // ─────────────────────────────────────────────────────────
    /** 加载所有 Journey 用例 */
    data object LoadJourneys : JourneysIntent()

    /** 筛选 Journey 列表 */
    data class FilterJourneys(val status: JourneyStatus) : JourneysIntent()

    /** 搜索 Journey */
    data class SearchJourneys(val query: String) : JourneysIntent()

    /** 切换 Tab */
    data class SwitchTab(val tab: ActiveTab) : JourneysIntent()

    // ─────────────────────────────────────────────────────────
    // Journey Management / Journey 管理
    // ─────────────────────────────────────────────────────────
    /** 打开 Journey 进行查看/编辑 */
    data class OpenJourney(val journeyId: String) : JourneysIntent()

    /** 从模板创建新 Journey */
    data class CreateJourneyFromTemplate(val templateId: String) : JourneysIntent()

    /** 创建空白 Journey */
    data object CreateBlankJourney : JourneysIntent()

    /** 保存当前 Journey XML */
    data class SaveJourney(val journeyId: String, val xml: String) : JourneysIntent()

    /** 删除 Journey */
    data class DeleteJourney(val journeyId: String) : JourneysIntent()

    // ─────────────────────────────────────────────────────────
    // Editor / 编辑器
    // ─────────────────────────────────────────────────────────
    /** 更新 Journey XML 内容 */
    data class UpdateJourneyXml(val xml: String) : JourneysIntent()

    /** 添加步骤 */
    data class AddStep(val afterStepId: String?, val step: JourneyStep) : JourneysIntent()

    /** 删除步骤 */
    data class DeleteStep(val stepId: String) : JourneysIntent()

    /** 重新排序步骤 */
    data class ReorderSteps(val fromIndex: Int, val toIndex: Int) : JourneysIntent()

    /** 选择步骤 */
    data class SelectStep(val stepId: String?) : JourneysIntent()

    // ─────────────────────────────────────────────────────────
    // NL Generation / 自然语言生成
    // ─────────────────────────────────────────────────────────
    /** 更新自然语言输入 */
    data class UpdateNLInput(val text: String) : JourneysIntent()

    /** 从自然语言生成 Journey XML */
    data object GenerateFromNL : JourneysIntent()

    /** 插入生成的 XML 到编辑器 */
    data object InsertGeneratedXML : JourneysIntent()

    // ─────────────────────────────────────────────────────────
    // Template / 模板
    // ─────────────────────────────────────────────────────────
    /** 搜索模板 */
    data class SearchTemplates(val query: String) : JourneysIntent()

    /** 按分类筛选模板 */
    data class FilterTemplatesByCategory(val category: TemplateCategory?) : JourneysIntent()

    // ─────────────────────────────────────────────────────────
    // Execution / 执行
    // ─────────────────────────────────────────────────────────
    /** 运行单个 Journey */
    data class RunJourney(val journeyId: String) : JourneysIntent()

    /** 重新运行上次失败的步骤 */
    data class RerunFailedStep(val journeyId: String, val stepId: String) : JourneysIntent()

    /** 展开/收起步骤详情 */
    data class ToggleStepDetail(val stepId: String) : JourneysIntent()

    // ─────────────────────────────────────────────────────────
    // CI/CD / CI/CD 配置
    // ─────────────────────────────────────────────────────────
    /** 选择 CI 平台 */
    data class SelectCIPlatform(val platform: CIPlatform) : JourneysIntent()

    /** 复制 CI 配置到剪贴板 */
    data object CopyCIConfig : JourneysIntent()

    /** 下载 CI 配置文件 */
    data object DownloadCIConfig : JourneysIntent()

    // ─────────────────────────────────────────────────────────
    // UI / UI 操作
    // ─────────────────────────────────────────────────────────
    /** 清除错误消息 */
    data object ClearError : JourneysIntent()
}

// ================================================================
// Effect / 副作用
// ================================================================

/**
 * ============================================================
 * JourneysEffect — 一次性副作用
 * ================================================================
 * One-time side effects triggered by ViewModel.
 * Delivered via Channel<UiEffect> and consumed by the UI layer.
 */
sealed class JourneysEffect {
    /** 显示 Toast 消息 */
    data class ShowToast(val message: String) : JourneysEffect()

    /** Journey 运行完成 */
    data class JourneyRunCompleted(
        val journeyId: String,
        val result: ExecutionResult
    ) : JourneysEffect()

    /** NL 生成完成 */
    data class NLGenerationCompleted(val xml: String) : JourneysEffect()

    /** 复制到剪贴板成功 */
    data class CopiedToClipboard(val content: String) : JourneysEffect()

    /** 显示错误消息 */
    data class ShowError(val message: String) : JourneysEffect()

    /** Journey 删除成功 */
    data class JourneyDeleted(val journeyId: String, val name: String) : JourneysEffect()

    /** Journey 保存成功 */
    data class JourneySaved(val journeyId: String) : JourneysEffect()

    /** Journey 创建成功 */
    data class JourneyCreated(val journeyId: String, val name: String) : JourneysEffect()
}
