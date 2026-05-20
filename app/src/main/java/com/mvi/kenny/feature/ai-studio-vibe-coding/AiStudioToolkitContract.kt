package com.mvi.kenny.feature.ai_studio_vibe_coding

/**
 * ============================================================
 * AiStudioToolkitContract — Google AI Studio Android Vibe Coding 开发工具包 MVI 契约
 * ============================================================
 *
 * PRD-267 | Google AI Studio Android Vibe Coding 开发工具包
 * Ref: memory/agency/designs/PRD-267-Google-AI-Studio-Android-Vibe-Coding开发工具包.md
 *
 * MVI Architecture:
 * - Model (State): Immutable page state, single source of truth
 * - View: Composable functions that consume State and render UI
 * - Intent: User intentions; ViewModel executes logic on receiving Intent
 * - Effect: One-time side effects (Toast, Navigation) delivered via Channel
 * —————————————————————————————————————————————————————
 *
 * Bottom Tab Navigation (7 Tabs):
 * - Tab 0: Quick Start Guide / 上手指南
 * - Tab 1: Quality Assessment / 质量评估工具
 * - Tab 2: Studio Handoff Workflow / Android Studio 移交工作流
 * - Tab 3: Emulator Debug Guide / 嵌入式 Emulator 调试指南
 * - Tab 4: Mobile Dev Workflow / 移动端开发工作流
 * - Tab 5: Workspace Integration / Google Workspace 集成
 * - Tab 6: Best Practices & Security / 最佳实践与安全合规
 */

// ============================================================
// Data Models / 数据模型
// ============================================================

/**
 * Bottom navigation tab enumeration
 * 底部导航 Tab 枚举
 *
 * @param title Tab display title / Tab 显示标题
 * @param iconName Material icon name / 图标名称
 */
enum class AiStudioTab(val title: String, val iconName: String) {
    QUICK_START("上手指南", "rocket_launch"),
    QUALITY("质量评估", "fact_check"),
    HANDOFF("Studio移交", "swap_horiz"),
    EMULATOR("Emulator调试", "phone_android"),
    MOBILE_WORKFLOW("移动端工作流", "smartphone"),
    WORKSPACE("Workspace集成", "integration_instructions"),
    BEST_PRACTICES("最佳实践", "star")
}

/**
 * Quick start step data model
 * 快速开始步骤数据模型
 *
 * @param stepNumber Step number (1-based) / 步骤编号
 * @param title Step title / 步骤标题
 * @param description Step description / 步骤描述
 * @param codeSnippet Code snippet (if applicable) / 代码片段
 * @param imagePlaceholder Placeholder for screenshot / 截图占位区
 */
data class QuickStartStep(
    val stepNumber: Int,
    val title: String,
    val description: String,
    val codeSnippet: String? = null,
    val imagePlaceholder: String? = null
)

/**
 * Quality checklist item for generated code assessment
 * 生成代码质量检查清单项
 *
 * @param id Unique identifier / 唯一标识
 * @param category Category name / 类别名称
 * @param checkItem Check item description / 检查项描述
 * @param severity Error severity: error/warning/info / 严重级别
 * @param isChecked Whether the item is checked / 是否已检查
 */
data class QualityCheckItem(
    val id: Int,
    val category: String,
    val checkItem: String,
    val severity: String, // "error" | "warning" | "info"
    val isChecked: Boolean = false
)

/**
 * Handoff workflow step data model
 * 移交工作流步骤数据模型
 *
 * @param id Unique identifier / 唯一标识
 * @param title Step title / 步骤标题
 * @param description Step description / 步骤描述
 * @param subSteps Sub-steps list / 子步骤列表
 * @param codeSnippet Code snippet (if applicable) / 代码片段
 */
data class HandoffStep(
    val id: Int,
    val title: String,
    val description: String,
    val subSteps: List<String> = emptyList(),
    val codeSnippet: String? = null
)

/**
 * Comparison entry for tool comparison table
 * 工具对比表条目
 *
 * @param aspect Comparison aspect / 对比维度
 * @param aiStudio Description for AI Studio / AI Studio 描述
 * @param androidStudio Description for Android Studio / Android Studio 描述
 * @param androidCli Description for Android CLI / Android CLI 描述
 */
data class ToolComparisonEntry(
    val aspect: String,
    val aiStudio: String,
    val androidStudio: String,
    val androidCli: String
)

/**
 * Security compliance checklist item
 * 安全合规自查清单项
 *
 * @param id Unique identifier / 唯一标识
 * @param category Category / 类别
 * @param checkItem Check item / 检查项
 * @param description Detailed description / 详细描述
 * @param isPassed Pass status / 是否通过
 */
data class SecurityCheckItem(
    val id: Int,
    val category: String,
    val checkItem: String,
    val description: String,
    val isPassed: Boolean = false
)

// ============================================================
// State / 页面状态
// ============================================================

/**
 * AI Studio Vibe Coding Toolkit page state
 * AI Studio Vibe Coding 开发者工具包页面状态
 *
 * MVI Architecture: Model layer, holds all page state.
 * State is Immutable — each state change creates a new State object.
 *
 * @param selectedTab Current bottom navigation tab index / 当前选中的 Tab
 * @param expandedQuickStartSteps Set of expanded quick start step indices / 已展开的快速开始步骤索引
 * @param expandedHandoffSteps Set of expanded handoff step indices / 已展开的移交步骤索引
 * @param qualityChecks List of quality check items / 质量检查项列表
 * @param securityChecks List of security check items / 安全检查项列表
 * @param selectedComparisonTab Selected comparison tab index / 选中的对比 Tab 索引
 * @param promptInput User's prompt input for preview / 提示词输入
 * @param generatedCodePreview Preview of generated code / 生成代码预览
 * @param copiedCodeBlockId ID of copied code block / 已复制的代码块 ID
 * @param error Error message, null means no error / 错误信息
 *
 * @see AiStudioTab
 * @see AiStudioIntent
 * @see AiStudioViewModel
 */
data class AiStudioState(
    val selectedTab: Int = 0,
    // Quick Start Guide state / 上手指南状态
    val expandedQuickStartSteps: Set<Int> = emptySet(),
    // Quality Assessment state / 质量评估状态
    val qualityChecks: List<QualityCheckItem> = emptyList(),
    val promptInput: String = "",
    val generatedCodePreview: String = "",
    // Handoff Workflow state / 移交工作流状态
    val expandedHandoffSteps: Set<Int> = emptySet(),
    // Comparison state / 对比状态
    val selectedComparisonTab: Int = 0,
    // Security & Compliance state / 安全合规状态
    val securityChecks: List<SecurityCheckItem> = emptyList(),
    // Code copy state / 代码复制状态
    val copiedCodeBlockId: String? = null,
    // Error state / 错误状态
    val error: String? = null
) {
    companion object {
        /** Initial / default state / 初始状态 */
        val Initial = AiStudioState()
    }

    /**
     * Generate quality report summary
     * 生成质量报告摘要
     *
     * @return Summary string with pass/fail counts / 通过/失败计数的摘要
     */
    fun generateQualityReport(): String {
        val total = qualityChecks.size
        val passed = qualityChecks.count { it.isChecked }
        val errors = qualityChecks.count { it.severity == "error" && !it.isChecked }
        val warnings = qualityChecks.count { it.severity == "warning" && !it.isChecked }
        return "总计: $total | 通过: $passed | 错误: $errors | 警告: $warnings"
    }

    /**
     * Generate security compliance report
     * 生成安全合规报告
     *
     * @return Summary string with compliance status / 合规状态摘要
     */
    fun generateSecurityReport(): String {
        val total = securityChecks.size
        val passed = securityChecks.count { it.isPassed }
        return "合规检查: $passed/$total 通过"
    }
}

// ============================================================
// Intent / 用户意图
// ============================================================

/**
 * User intentions for AI Studio Vibe Coding Toolkit
 * AI Studio Vibe Coding 开发者工具包用户意图
 *
 * Every user interaction on the page corresponds to an Intent.
 * ViewModel receives Intent, executes business logic, then updates State.
 *
 * @see AiStudioViewModel.sendIntent
 */
sealed interface AiStudioIntent {

    /**
     * Switch bottom navigation tab / 切换底部 Tab
     *
     * @param index Target tab index / 目标 Tab 索引
     */
    data class SelectTab(val index: Int) : AiStudioIntent

    /**
     * Toggle quick start step expansion
     * 切换快速开始步骤展开/收起
     *
     * @param stepIndex Step index / 步骤索引
     */
    data class ToggleQuickStartStep(val stepIndex: Int) : AiStudioIntent

    /**
     * Toggle quality check item
     * 切换质量检查项勾选状态
     *
     * @param checkId Check item ID / 检查项 ID
     */
    data class ToggleQualityCheck(val checkId: Int) : AiStudioIntent

    /**
     * Toggle handoff step expansion
     * 切换移交步骤展开/收起
     *
     * @param stepId Step ID / 步骤 ID
     */
    data class ToggleHandoffStep(val stepId: Int) : AiStudioIntent

    /**
     * Update prompt input field
     * 更新提示词输入
     *
     * @param prompt Prompt text / 提示词文本
     */
    data class UpdatePromptInput(val prompt: String) : AiStudioIntent

    /**
     * Select comparison tab
     * 选择对比 Tab
     *
     * @param tabIndex Tab index / Tab 索引
     */
    data class SelectComparisonTab(val tabIndex: Int) : AiStudioIntent

    /**
     * Toggle security check item
     * 切换安全检查项勾选状态
     *
     * @param checkId Check item ID / 检查项 ID
     */
    data class ToggleSecurityCheck(val checkId: Int) : AiStudioIntent

    /**
     * Copy code to clipboard
     * 复制代码到剪贴板
     *
     * @param code Code text to copy / 要复制的代码文本
     * @param blockId Unique block identifier / 唯一代码块标识
     */
    data class CopyCode(val code: String, val blockId: String) : AiStudioIntent

    /**
     * Clear copied state (after snackbar shown)
     * 清除已复制状态
     */
    data object ClearCopiedState : AiStudioIntent

    /**
     * Run quality assessment on prompt
     * 对提示词运行质量评估
     *
     * @param prompt User's prompt / 用户的提示词
     */
    data class RunQualityAssessment(val prompt: String) : AiStudioIntent
}

// ============================================================
// Effect / 副作用
// ============================================================

/**
 * One-time side effects for AI Studio Vibe Coding Toolkit
 * AI Studio Vibe Coding 开发者工具包副作用
 *
 * One-time events, immutable, consumed only once.
 * UI layer listens via LaunchedEffect + flow.collect {}
 *
 * @see AiStudioViewModel
 */
sealed interface AiStudioEffect {

    /**
     * Show toast message / 显示 Toast
     *
     * @param message Toast message text / Toast 文本
     */
    data class ShowToast(val message: String) : AiStudioEffect

    /**
     * Copy text to system clipboard / 复制文本到剪贴板
     *
     * @param text Text to copy / 要复制的文本
     * @param blockId Block identifier for UI state / 代码块标识
     */
    data class CopyToClipboard(val text: String, val blockId: String) : AiStudioEffect

    /**
     * Scroll to top of page / 滚动到页面顶部
     */
    data object ScrollToTop : AiStudioEffect

    /**
     * Show quality assessment result / 显示质量评估结果
     *
     * @param report Quality report text / 质量报告文本
     */
    data class ShowQualityReport(val report: String) : AiStudioEffect
}
