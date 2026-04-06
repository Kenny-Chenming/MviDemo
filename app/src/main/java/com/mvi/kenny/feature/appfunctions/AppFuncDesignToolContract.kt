package com.mvi.kenny.feature.appfunctions

import androidx.compose.ui.graphics.Color

/**
 * ============================================================
 * AppFuncDesignToolContract — AppFunctions 开发者工具包 MVI 契约
 * ============================================================
 * PRD-030 | AppFunctions 开发者工具包（Agent-Ready App 基础设施）
 *
 * MVI 三要素：
 * - Model（State）：页面状态的唯一真相来源，Immutable 数据类
 * - View：Composable 函数，消费 State，渲染 UI
 * - Intent：用户意图，ViewModel 收到 Intent 后执行业务逻辑
 * - Effect：一次性副作用（Toast、导航），通过 Channel 传递
 * —————————————————————————————————————————————————————
 * This contract defines the developer tooling console for AppFunctions,
 * enabling Android apps to expose callable functions to AI agents like Gemini.
 *
 * Design reference: memory/agency/designs/PRD-030-AppFunctions开发者工具包.md
 *
 * @see AppFuncDesignToolViewModel 状态管理逻辑
 */

/**
 * ============================================================
 * AppFuncDesignToolContract — AppFunctions Developer Toolkit MVI Contract
 * ============================================================
 * Architecture: MVI (Model-View-Intent)
 *
 * Bottom Tab Navigation:
 * - VALIDATOR  → AppFunction definition validation
 * - MOCK       → Local Mock Agent test environment
 * - TEMPLATES  → Scenario template library
 * - COMPAT     → Android version compatibility detection
 */

// ============================================================
// Data Models / 数据模型
// ============================================================

/**
 * 设计工具底部 Tab 枚举
 *
 * @param title 中文标题
 * @param iconName 图标名称
 */
enum class DesignTab(val title: String, val iconName: String) {
    VALIDATOR("校验台", "check_circle"),
    MOCK("Mock 测试", "smart_toy"),
    TEMPLATES("模板库", "inventory_2"),
    COMPAT("兼容检测", "phone_android")
}

/**
 * AppFunction 列表项数据模型
 *
 * @param id 函数唯一标识
 * @param name 函数名称（方法名）
 * @param packageName 所属包名
 * @param params 参数列表
 * @param returnType 返回值类型
 * @param agentVisibility Agent 可见性（是否对 AI Agent 暴露）
 * @param validationState 校验状态
 *
 * @see ValidationState
 */
data class AppFunctionItem(
    val id: String,
    val name: String,
    val packageName: String,
    val params: List<FunctionParam>,
    val returnType: String,
    val agentVisibility: Boolean = true,
    val validationState: ValidationState = ValidationState.UNCHECKED
)

/**
 * 函数参数数据模型
 *
 * @param name 参数名称
 * @param type 参数类型（Fully qualified name）
 * @param isOptional 是否可选参数
 * @param description 参数描述
 */
data class FunctionParam(
    val name: String,
    val type: String,
    val isOptional: Boolean = false,
    val description: String = ""
)

/**
 * 校验状态枚举
 *
 * @param symbol 状态符号（emoji）
 * @param colorHex 状态颜色（hex）
 * @param label 状态标签
 */
enum class ValidationState(val symbol: String, val colorHex: String, val label: String) {
    UNCHECKED("⚪", "#8B949E", "未检测"),
    PASS("✅", "#3FB950", "通过"),
    WARNING("⚠️", "#D29922", "警告"),
    ERROR("❌", "#F85149", "错误")
}

/**
 * 校验结果数据模型
 *
 * @param functionId 对应的函数 ID
 * @param state 校验状态
 * @param messages 校验消息列表
 */
data class ValidationResult(
    val functionId: String,
    val state: ValidationState,
    val messages: List<String> = emptyList()
)

/**
 * Mock 调用记录数据模型
 *
 * @param id 记录唯一标识
 * @param functionId 调用的函数 ID
 * @param functionName 函数名称
 * @param params JSON 格式参数字符串
 * @param durationMs 调用耗时（毫秒）
 * @param result 调用结果（JSON）
 * @param isError 是否出错
 * @param timestamp 调用时间
 */
data class MockCallRecord(
    val id: String,
    val functionId: String,
    val functionName: String,
    val params: String,
    val durationMs: Long,
    val result: String,
    val isError: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * 函数模板数据模型
 *
 * @param id 模板唯一标识
 * @param name 模板名称
 * @param description 模板描述
 * @param category 模板分类
 * @param functionCount 覆盖函数数量
 * @param tags 标签列表
 */
data class FunctionTemplate(
    val id: String,
    val name: String,
    val description: String,
    val category: TemplateCategory,
    val functionCount: Int,
    val tags: List<String> = emptyList()
)

/**
 * 模板分类枚举
 */
enum class TemplateCategory(val displayName: String) {
    CALENDAR("Calendar"),
    NOTES("Notes"),
    TASKS("Tasks"),
    SHOPPING("Shopping"),
    CUSTOM("自定义")
}

/**
 * 兼容性报告数据模型
 *
 * @param id 报告唯一标识
 * @param androidVersion Android 版本号
 * @param versionName 版本名称
 * @param state 兼容性状态
 * @param affectedFunctions 受影响的函数列表
 * @param suggestion 降级建议
 */
data class CompatibilityReport(
    val id: String,
    val androidVersion: Int,
    val versionName: String,
    val state: ValidationState,
    val affectedFunctions: List<String> = emptyList(),
    val suggestion: String = ""
)

// ============================================================
// State / 页面状态
// ============================================================

/**
 * AppFunctions 开发者工具页面状态
 *
 * MVI 架构中的 Model 层，持有页面的所有状态。
 * 状态是 Immutable 的，每次状态变化都创建新的 State 对象。
 *
 * @param selectedTab 当前选中的底部 Tab
 * @param projectInfo 项目信息（stub）
 * @param functionList AppFunction 列表
 * @param selectedFunction 选中的函数（用于详情页）
 * @param validationResults 校验结果 Map（functionId → ValidationResult）
 * @param mockHistory Mock 调用历史记录
 * @param templates 函数模板列表
 * @param compatibilityReports 兼容性报告列表
 * @param isScanning 是否正在扫描
 * @param isMockRunning Mock 测试是否运行中
 * @param error 错误信息，null 表示无错误
 *
 * @see AppFuncDesignToolIntent 用户意图
 * @see AppFuncDesignToolViewModel 状态管理逻辑
 */
data class AppFuncDesignToolState(
    val selectedTab: DesignTab = DesignTab.VALIDATOR,
    val projectInfo: ProjectInfo? = null,
    val functionList: List<AppFunctionItem> = emptyList(),
    val selectedFunction: AppFunctionItem? = null,
    val validationResults: Map<String, ValidationResult> = emptyMap(),
    val mockHistory: List<MockCallRecord> = emptyList(),
    val templates: List<FunctionTemplate> = emptyList(),
    val compatibilityReports: List<CompatibilityReport> = emptyList(),
    val isScanning: Boolean = false,
    val isMockRunning: Boolean = false,
    val error: String? = null
) {
    companion object {
        /** 初始状态 */
        val Initial = AppFuncDesignToolState()
    }
}

/**
 * 项目信息（stub 实现）
 *
 * @param name 项目名称
 * @param path 项目路径
 * @param moduleCount 模块数量
 */
data class ProjectInfo(
    val name: String = "MyMviProject",
    val path: String = "/path/to/project",
    val moduleCount: Int = 1
)

// ============================================================
// Intent / 用户意图
// ============================================================

/**
 * AppFunctions 开发者工具页面用户意图（User Intent）
 * —————————————————————————————————————————————————————
 * 页面上的每一个用户操作都对应一个 Intent。
 * ViewModel 收到 Intent 后执行业务逻辑，然后更新 State。
 *
 * @see AppFuncDesignToolViewModel.sendIntent 处理所有 Intent
 */
sealed interface AppFuncDesignToolIntent {

    /**
     * 扫描项目中的 @AppFunction 标注项
     * Scan project for @AppFunction annotated items
     */
    data object ScanFunctions : AppFuncDesignToolIntent

    /**
     * 选中某个函数，查看详情
     * Select a function to view its details
     *
     * @param function 选中的函数
     */
    data class SelectFunction(val function: AppFunctionItem?) : AppFuncDesignToolIntent

    /**
     * 运行 Mock 测试
     * Run mock test for a specific function
     *
     * @param functionId 函数 ID
     * @param params JSON 格式参数字符串
     */
    data class RunMockTest(val functionId: String, val params: String) : AppFuncDesignToolIntent

    /**
     * 应用模板到当前项目
     * Apply a template to the current project
     *
     * @param templateId 模板 ID
     */
    data class ApplyTemplate(val templateId: String) : AppFuncDesignToolIntent

    /**
     * 运行兼容性检测
     * Run Android version compatibility check
     *
     * @param targetVersions 目标 Android 版本列表
     */
    data class RunCompatibilityCheck(val targetVersions: List<Int>) : AppFuncDesignToolIntent

    /**
     * 切换底部 Tab
     * Switch bottom navigation tab
     *
     * @param tab 目标 Tab
     */
    data class SwitchTab(val tab: DesignTab) : AppFuncDesignToolIntent

    /**
     * 展开/折叠函数详情
     * Expand or collapse function detail
     *
     * @param functionId 函数 ID
     */
    data class ExpandFunction(val functionId: String) : AppFuncDesignToolIntent

    /**
     * 切换函数的 Agent 可见性
     * Toggle function's Agent visibility
     *
     * @param functionId 函数 ID
     */
    data class ToggleAgentVisibility(val functionId: String) : AppFuncDesignToolIntent
}

// ============================================================
// Effect / 副作用
// ============================================================

/**
 * AppFunctions 开发者工具页面副作用（Effect）
 * —————————————————————————————————————————————————————
 * 一次性事件，不可变，只能被消费一次。
 * UI 层通过 LaunchedEffect + flow.collect{} 监听并处理。
 *
 * @see AppFuncDesignToolViewModel 中通过 _effect.send() 发送 Effect
 */
sealed interface AppFuncDesignToolEffect {

    /**
     * 显示 Toast
     * Show a toast message
     *
     * @param message Toast 文本内容
     */
    data class ShowToast(val message: String) : AppFuncDesignToolEffect

    /**
     * 导航到函数详情页
     * Navigate to function detail page
     *
     * @param functionId 函数 ID
     */
    data class NavigateToFunctionDetail(val functionId: String) : AppFuncDesignToolEffect

    /**
     * 模板应用成功
     * Template has been successfully applied
     */
    data object TemplateApplied : AppFuncDesignToolEffect

    /**
     * 校验错误
     * Validation error occurred
     *
     * @param message 错误信息
     */
    data class ValidationError(val message: String) : AppFuncDesignToolEffect
}
