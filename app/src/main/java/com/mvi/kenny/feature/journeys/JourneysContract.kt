package com.mvi.kenny.feature.journeys

import androidx.compose.ui.graphics.Color

// =============================================================
// JourneysContract — Journeys E2E 测试工具包 MVI 契约
// PRD-099 | Journeys for Android Studio 自动化 E2E 测试工具包
// =============================================================
// MVI Architecture Pattern / MVI 架构模式
//
// - Model (State): Immutable data class, single source of truth for UI
// - View: Composable functions that consume State and render UI
// - Intent: User intentions, ViewModel processes and updates State
// - Effect: One-time side effects (navigation, toast, etc.)
//
// @see JourneysViewModel State management
// @see JourneysDashboardScreen Main dashboard screen

// =============================================================
// EditorMode — 编辑器模式
// =============================================================
/**
 * Journey editor mode / Journey 编辑器模式
 *
 * @param label Display label / 显示标签
 */
enum class EditorMode(val label: String) {
    VIEW("View / 浏览"),
    EDIT("Edit / 编辑"),
    CREATE("Create / 新建")
}

// =============================================================
// CIPlatform — CI 平台枚举
// =============================================================
/**
 * CI platform type / CI 平台类型
 *
 * @param label Display label / 显示标签
 * @param yamlFileName Default YAML file name / 默认 YAML 文件名
 */
enum class CIPlatform(val label: String, val yamlFileName: String) {
    GITHUB_ACTIONS("GitHub Actions", ".github/workflows/journeys.yml"),
    GITLAB_CI("GitLab CI", ".gitlab-ci.yml"),
    JENKINS("Jenkins", "jenkinsfile")
}

// =============================================================
// FilterStatus — Journey 列表筛选状态
// =============================================================
/**
 * Journey filter status / Journey 列表筛选状态
 *
 * @param label Display label / 显示标签
 * @param emoji Filter emoji / 筛选 emoji
 */
enum class FilterStatus(val label: String, val emoji: String) {
    ALL("All / 全部", "📋"),
    PASSED("Passed / 通过", "✅"),
    FAILED("Failed / 失败", "❌"),
    RUNNING("Running / 运行中", "⏳")
}

// =============================================================
// StepStatus — 步骤执行状态
// =============================================================
/**
 * Step execution status / 步骤执行状态
 *
 * @param label Display label / 显示标签
 * @param emoji Status emoji / 状态 emoji
 */
enum class StepStatus(val label: String, val emoji: String) {
    PENDING("Pending / 待执行", "⏸️"),
    RUNNING("Running / 执行中", "⏳"),
    PASSED("Passed / 通过", "✅"),
    FAILED("Failed / 失败", "❌"),
    SKIPPED("Skipped / 跳过", "⏭️")
}

// =============================================================
// JourneyItem — Journey 测试用例项
// =============================================================
/**
 * Journey test case item / Journey 测试用例项
 *
 * Represents a single Journey XML file in the project.
 *
 * @param id Unique identifier / 唯一标识符
 * @param name File name without extension / 不带扩展名的文件名
 * @param filePath Absolute file path / 绝对文件路径
 * @param status Latest execution status / 最新执行状态
 * @param lastRunTime Last execution timestamp / 最后执行时间戳
 * @param lastRunDurationMs Execution duration in milliseconds / 执行时长（毫秒）
 * @param stepCount Number of steps in the journey / Journey 中的步骤数
 * @param passRate Historical pass rate / 历史通过率
 */
data class JourneyItem(
    val id: String,
    val name: String,
    val filePath: String,
    val status: StepStatus,
    val lastRunTime: Long,
    val lastRunDurationMs: Long,
    val stepCount: Int,
    val passRate: Float
)

// =============================================================
// JourneyStep — Journey XML 中的步骤
// =============================================================
/**
 * Journey XML step / Journey XML 步骤
 *
 * Represents a single <Step> element in Journey XML.
 *
 * @param id Step unique identifier / 步骤唯一标识符
 * @param actionType Action type (launch/input/click/swipe/assert/...) / 动作类型
 * @param description Human-readable description / 人类可读描述
 * @param target Target selector or package / 目标选择器或包名
 * @param value Input value (for input actions) / 输入值（用于输入动作）
 * @param timeoutMs Step timeout in milliseconds / 步骤超时（毫秒）
 * @param status Current execution status / 当前执行状态
 */
data class JourneyStep(
    val id: String,
    val actionType: String,
    val description: String,
    val target: String,
    val value: String = "",
    val timeoutMs: Int = 30000,
    val status: StepStatus = StepStatus.PENDING
)

// =============================================================
// ExecutionResult — 执行结果
// =============================================================
/**
 * Journey execution result / Journey 执行结果
 *
 * @param journeyId Journey identifier / Journey 标识符
 * @param journeyName Journey file name / Journey 文件名
 * @param overallStatus Overall execution status / 整体执行状态
 * @param startTime Start timestamp / 开始时间戳
 * @param endTime End timestamp / 结束时间戳
 * @param durationMs Total duration in milliseconds / 总时长（毫秒）
 * @param steps Execution results for each step / 各步骤执行结果
 * @param failedStepIndex Index of failed step (-1 if passed) / 失败步骤索引（通过时为 -1）
 * @param screenshotPaths Map of step index to screenshot file path / 步骤索引到截图路径的映射
 * @param errorMessage Error message if overall failure / 整体失败时的错误信息
 */
data class ExecutionResult(
    val journeyId: String,
    val journeyName: String,
    val overallStatus: StepStatus,
    val startTime: Long,
    val endTime: Long,
    val durationMs: Long,
    val steps: List<StepResult>,
    val failedStepIndex: Int = -1,
    val screenshotPaths: Map<Int, String> = emptyMap(),
    val errorMessage: String? = null
)

// =============================================================
// StepResult — 单个步骤执行结果
// =============================================================
/**
 * Single step execution result / 单个步骤执行结果
 *
 * @param step The step definition / 步骤定义
 * @param status Execution status / 执行状态
 * @param startTime Step start timestamp / 步骤开始时间戳
 * @param endTime Step end timestamp / 步骤结束时间戳
 * @param durationMs Step duration in milliseconds / 步骤时长（毫秒）
 * @param screenshotPath Screenshot file path if captured / 截图路径（如有）
 * @param errorMessage Error message if failed / 失败时的错误信息
 * @param expectedResult Expected result description / 预期结果描述
 * @param actualResult Actual result description / 实际结果描述
 */
data class StepResult(
    val step: JourneyStep,
    val status: StepStatus,
    val startTime: Long,
    val endTime: Long,
    val durationMs: Long,
    val screenshotPath: String? = null,
    val errorMessage: String? = null,
    val expectedResult: String? = null,
    val actualResult: String? = null
)

// =============================================================
// JourneyTemplate — Journey 模板
// =============================================================
/**
 * Journey template / Journey 模板
 *
 * Pre-built Journey XML templates for common test scenarios.
 *
 * @param id Template unique identifier / 模板唯一标识符
 * @param name Template name / 模板名称
 * @param category Template category / 模板分类
 * @param description Template description / 模板描述
 * @param xmlContent Template XML content / 模板 XML 内容
 * @param tags List of tags for search / 搜索标签列表
 */
data class JourneyTemplate(
    val id: String,
    val name: String,
    val category: TemplateCategory,
    val description: String,
    val xmlContent: String,
    val tags: List<String>
)

// =============================================================
// TemplateCategory — 模板分类
// =============================================================
/**
 * Template category / 模板分类
 *
 * @param label Display label / 显示标签
 * @param emoji Category emoji / 分类 emoji
 */
enum class TemplateCategory(val label: String, val emoji: String) {
    LOGIN_FLOW("Login Flow / 登录流程", "🧭"),
    SHOPPING_FLOW("Shopping Flow / 购物流程", "🛒"),
    LIST_OPERATION("List Operation / 列表操作", "📋"),
    FORM_FILLING("Form Filling / 表单填写", "📝"),
    DEEP_LINK("Deep Link / 深度链接", "🔗"),
    PAYMENT_FLOW("Payment Flow / 支付流程", "💳"),
    NAVIGATION_FLOW("Navigation Flow / 导航流程", "🧭")
}

// =============================================================
// FrameworkOption — 测试框架选项
// =============================================================
/**
 * Testing framework option / 测试框架选项
 *
 * @param name Framework name / 框架名称
 * @param supportedFeatures List of supported features / 支持的功能列表
 * @param unsupportedFeatures List of unsupported features / 不支持的功能列表
 * @param bestFor Best use cases / 最佳使用场景
 * @param color Brand color / 品牌色
 */
data class FrameworkOption(
    val name: String,
    val supportedFeatures: List<String>,
    val unsupportedFeatures: List<String>,
    val bestFor: List<String>,
    val color: Color
)

// =============================================================
// JourneysState — 主状态
// =============================================================
/**
 * Journeys feature global state / Journeys 功能全局状态
 *
 * Single source of truth for all Journey-related UI state.
 * Managed by JourneysViewModel following MVI pattern.
 *
 * @param journeys List of all Journey items / 所有 Journey 项列表
 * @param activeJourneyId Currently active/open Journey ID / 当前活跃/打开的 Journey ID
 * @param activeJourneySteps Steps of the active Journey / 活跃 Journey 的步骤
 * @param editorMode Current editor mode / 当前编辑器模式
 * @param currentJourneyXml Current Journey XML content / 当前 Journey XML 内容
 * @param executionResult Latest execution result / 最新执行结果
 * @param isRunning Whether a Journey is currently running / 是否有 Journey 正在运行
 * @param templates Available Journey templates / 可用的 Journey 模板列表
 * @param selectedCIPlatform Selected CI platform / 选中的 CI 平台
 * @param nlGenerationResult Natural language to XML generation result / 自然语言生成结果
 * @param isGeneratingNL Whether NL generation is in progress / 是否正在生成 NL
 * @param filterStatus Current filter status / 当前筛选状态
 * @param isLoading Whether data is loading / 是否正在加载
 * @param errorMessage Current error message (if any) / 当前错误信息（如有）
 */
data class JourneysState(
    val journeys: List<JourneyItem> = emptyList(),
    val activeJourneyId: String? = null,
    val activeJourneySteps: List<JourneyStep> = emptyList(),
    val editorMode: EditorMode = EditorMode.VIEW,
    val currentJourneyXml: String = "",
    val executionResult: ExecutionResult? = null,
    val isRunning: Boolean = false,
    val templates: List<JourneyTemplate> = emptyList(),
    val selectedCIPlatform: CIPlatform = CIPlatform.GITHUB_ACTIONS,
    val nlGenerationResult: String? = null,
    val isGeneratingNL: Boolean = false,
    val filterStatus: FilterStatus = FilterStatus.ALL,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
) {
    companion object {
        /** Initial state / 初始状态 */
        val Initial = JourneysState()
    }
}

// =============================================================
// JourneysIntent — 用户意图
// =============================================================
/**
 * Journeys user intentions / Journeys 用户意图
 *
 * All user actions that can trigger state changes.
 * Processed by JourneysViewModel.
 */
sealed class JourneysIntent {
    /** Load all Journey items / 加载所有 Journey 项 */
    data object LoadJourneys : JourneysIntent()

    /** Open a specific Journey / 打开指定的 Journey */
    data class OpenJourney(val journeyId: String) : JourneysIntent()

    /** Create a new Journey / 新建 Journey */
    data class CreateJourney(val templateId: String? = null) : JourneysIntent()

    /** Save current Journey / 保存当前 Journey */
    data class SaveJourney(val journeyId: String, val xml: String) : JourneysIntent()

    /** Delete a Journey / 删除 Journey */
    data class DeleteJourney(val journeyId: String) : JourneysIntent()

    /** Run a Journey / 运行 Journey */
    data class RunJourney(val journeyId: String) : JourneysIntent()

    /** Run a single step / 运行单个步骤 */
    data class RunSingleStep(val journeyId: String, val stepId: String) : JourneysIntent()

    /** Generate Journey XML from natural language / 从自然语言生成 Journey XML */
    data class GenerateFromNL(val description: String) : JourneysIntent()

    /** Insert generated Journey XML into editor / 将生成的 XML 插入编辑器 */
    data class InsertGeneratedJourney(val xml: String) : JourneysIntent()

    /** Select a template / 选择模板 */
    data class SelectTemplate(val templateId: String) : JourneysIntent()

    /** Select CI platform / 选择 CI 平台 */
    data class SelectCIPlatform(val platform: CIPlatform) : JourneysIntent()

    /** Filter Journey list / 筛选 Journey 列表 */
    data class FilterJourneys(val status: FilterStatus) : JourneysIntent()

    /** Add a new step / 添加新步骤 */
    data class AddStep(val afterStepId: String?, val step: JourneyStep) : JourneysIntent()

    /** Delete a step / 删除步骤 */
    data class DeleteStep(val stepId: String) : JourneysIntent()

    /** Reorder steps / 重新排序步骤 */
    data class ReorderSteps(val fromIndex: Int, val toIndex: Int) : JourneysIntent()

    /** Update step details / 更新步骤详情 */
    data class UpdateStep(val step: JourneyStep) : JourneysIntent()

    /** Set editor mode / 设置编辑器模式 */
    data class SetEditorMode(val mode: EditorMode) : JourneysIntent()

    /** Update Journey XML content / 更新 Journey XML 内容 */
    data class UpdateJourneyXml(val xml: String) : JourneysIntent()

    /** Clear execution result / 清除执行结果 */
    data object ClearExecutionResult : JourneysIntent()

    /** Dismiss error / 关闭错误提示 */
    data object DismissError : JourneysIntent()
}

// =============================================================
// JourneysEffect — 副作用
// =============================================================
/**
 * Journeys one-time side effects / Journeys 一次性副作用
 *
 * One-time events triggered by ViewModel that UI should handle.
 * Effects are consumed once and not replayed on configuration change.
 */
sealed class JourneysEffect {
    /** Show a toast message / 显示 Toast 消息 */
    data class ShowToast(val message: String) : JourneysEffect()

    /** Journey run completed notification / Journey 运行完成通知 */
    data class JourneyRunCompleted(
        val journeyId: String,
        val result: ExecutionResult
    ) : JourneysEffect()

    /** NL generation completed / 自然语言生成完成 */
    data class NlGenerationCompleted(val xml: String) : JourneysEffect()

    /** Copy to clipboard / 复制到剪贴板 */
    data class CopiedToClipboard(val content: String) : JourneysEffect()

    /** Show error dialog / 显示错误对话框 */
    data class ShowError(val message: String) : JourneysEffect()

    /** Navigate to Journey editor / 导航到 Journey 编辑器 */
    data class NavigateToEditor(val journeyId: String?) : JourneysEffect()

    /** Journey saved successfully / Journey 保存成功 */
    data class JourneySaved(val journeyId: String) : JourneysEffect()
}
