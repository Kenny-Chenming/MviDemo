package com.mvi.kenny.feature.verifiedfinancialcalls

/**
 * ============================================================
 * VerifiedFinancialCallsContract — Verified Financial Calls API 工具包 MVI 契约
 * Verified Financial Calls API Developer Toolkit MVI Contract
 * ============================================================
 *
 * PRD-257 | Android Security 2026 Verified Financial Calls API 集成工具包
 * Ref: memory/agency/designs/PRD-257-Android-Security-Verified-Financial-Calls-API集成工具包.md
 *
 * MVI Architecture:
 * - Model (State): Immutable page state, single source of truth
 * - View: Composable functions that consume State and render UI
 * - Intent: User intentions; ViewModel executes logic on receiving Intent
 * - Effect: One-time side effects (Snackbar, Clipboard) delivered via Channel
 * —————————————————————————————————————————————————————
 *
 * Bottom Tab Navigation (5 Tabs):
 * - Tab 0: 概述 (Overview) — API background, core concepts, prerequisites
 * - Tab 1: 验证 API (Verification API) — Verified Financial Calls API integration
 * - Tab 2: 注册配置 (Registration) — Developer Console config, number registration
 * - Tab 3: 合规指南 (Compliance) — GDPR/CCPA compliance checklist
 * - Tab 4: 扩展能力 (Extensions) — Live Threat Detection / Dynamic Signal Monitoring / Intrusion Logging
 * —————————————————————————————————————————————————————
 */

// ============================================================
// Tab Definitions / Tab 定义
// ============================================================

/**
 * Bottom navigation tab enumeration
 * 底部导航 Tab 枚举
 *
 * @param title Tab display title / Tab 显示标题
 * @param iconName Material icon name / 图标名称
 */
enum class VerifiedFinancialCallsTab(
    val title: String,
    val iconName: String
) {
    OVERVIEW("概述", "info"),
    VERIFICATION_API("验证 API", "security"),
    REGISTRATION("注册配置", "settings"),
    COMPLIANCE("合规指南", "fact_check"),
    EXTENSIONS("扩展能力", "layers")
}

// ============================================================
// Code Block Model / 代码块模型
// ============================================================

/**
 * Code block data model for displaying code snippets
 * 代码块数据模型，用于展示代码示例
 *
 * @param id Unique block identifier / 唯一标识
 * @param title Code block title / 代码块标题
 * @param code Code content / 代码内容
 * @param language Programming language (kotlin/xml) / 编程语言
 * @param description Explanation text / 说明文字
 */
data class CodeBlock(
    val id: String,
    val title: String,
    val code: String,
    val language: String = "kotlin",
    val description: String = ""
)

// ============================================================
// Compliance Checklist Item / 合规清单项
// ============================================================

/**
 * Compliance checklist item data model
 * 合规清单项数据模型
 *
 * @param id Item unique identifier / 唯一标识
 * @param title Item title / 标题
 * @param description Item description / 描述
 * @param category Compliance category / 所属类别
 * @param isChecked Whether item is checked / 是否已勾选
 */
data class ComplianceItem(
    val id: Int,
    val title: String,
    val description: String,
    val category: String, // "GDPR" | "CCPA" | "General"
    val isChecked: Boolean = false
)

// ============================================================
// Feature Card Model / 功能卡片模型
// ============================================================

/**
 * Feature card data model for displaying capability overview
 * 功能卡片数据模型
 *
 * @param id Card unique identifier / 唯一标识
 * @param title Card title / 卡片标题
 * @param description Brief description / 简要描述
 * @param iconName Material icon name / 图标名称
 * @param tags Feature tags / 标签列表
 */
data class FeatureCard(
    val id: Int,
    val title: String,
    val description: String,
    val iconName: String,
    val tags: List<String> = emptyList()
)

// ============================================================
// State / 页面状态
// ============================================================

/**
 * Verified Financial Calls Developer Toolkit page state
 * Verified Financial Calls 开发者工具包页面状态
 *
 * MVI Architecture: Model layer, holds all page state.
 * State is Immutable — each state change creates a new State object.
 *
 * @param selectedTab Current bottom navigation tab index (0-4) / 当前选中的 Tab 索引
 * @param complianceCheckedItems Set of checked compliance item IDs / 已勾选的合规项 ID 集合
 * @param isLoading Loading state / 加载状态
 * @param errorMessage Error message, null means no error / 错误信息，null 表示无错误
 * @param copiedBlockId Last copied code block ID / 最近复制的代码块 ID
 * @param expandedCards Set of expanded card IDs / 已展开的卡片 ID 集合
 *
 * @see VerifiedFinancialCallsIntent
 * @see VerifiedFinancialCallsViewModel
 */
data class VerifiedFinancialCallsState(
    val selectedTab: Int = 0,
    // Compliance checklist state / 合规清单状态
    val complianceCheckedItems: Set<Int> = emptySet(),
    // Loading & error state / 加载和错误状态
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    // Copy feedback / 复制反馈
    val copiedBlockId: String? = null,
    // Card expansion state / 卡片展开状态
    val expandedCards: Set<Int> = emptySet()
) {
    companion object {
        /** Initial / default state / 初始状态 */
        val Initial = VerifiedFinancialCallsState()
    }

    /**
     * Get unchecked compliance items count for banner display
     * 获取未勾选合规项数量，用于显示红色 Banner
     *
     * @param totalItems Total compliance items count / 合规项总数
     * @return Unchecked items count / 未勾选数量
     */
    fun getUncheckedCount(totalItems: Int): Int = totalItems - complianceCheckedItems.size
}

// ============================================================
// Intent / 用户意图
// ============================================================

/**
 * User intentions for Verified Financial Calls Developer Toolkit
 * Verified Financial Calls 开发者工具包用户意图
 *
 * Every user interaction on the page corresponds to an Intent.
 * ViewModel receives Intent, executes business logic, then updates State.
 *
 * @see VerifiedFinancialCallsViewModel.sendIntent
 */
sealed interface VerifiedFinancialCallsIntent {

    /**
     * Switch bottom navigation tab / 切换底部 Tab
     *
     * @param index Target tab index (0-4) / 目标 Tab 索引
     */
    data class SelectTab(val index: Int) : VerifiedFinancialCallsIntent

    /**
     * Toggle compliance item check state
     * 切换合规清单项勾选状态
     *
     * @param itemId Item unique identifier / 清单项唯一标识
     */
    data class ToggleComplianceItem(val itemId: Int) : VerifiedFinancialCallsIntent

    /**
     * Copy code block content to clipboard
     * 复制代码块内容到剪贴板
     *
     * @param blockId Code block identifier / 代码块标识
     * @param content Code content to copy / 要复制的代码内容
     */
    data class CopyCodeBlock(val blockId: String, val content: String) : VerifiedFinancialCallsIntent

    /**
     * Toggle expandable card expansion state
     * 切换可展开卡片展开/收起状态
     *
     * @param cardId Card unique identifier / 卡片唯一标识
     */
    data class ToggleCard(val cardId: Int) : VerifiedFinancialCallsIntent

    /**
     * Dismiss error message
     * 关闭错误提示
     */
    data object DismissError : VerifiedFinancialCallsIntent
}

// ============================================================
// Effect / 副作用
// ============================================================

/**
 * One-time side effects for Verified Financial Calls Developer Toolkit
 * Verified Financial Calls 开发者工具包副作用
 *
 * One-time events, immutable, consumed only once.
 * UI layer listens via LaunchedEffect + flow.collect {}
 *
 * @see VerifiedFinancialCallsViewModel
 */
sealed interface VerifiedFinancialCallsEffect {

    /**
     * Show snackbar message / 显示 Snackbar 消息
     *
     * @param message Message text / 消息文本
     */
    data class ShowSnackbar(val message: String) : VerifiedFinancialCallsEffect

    /**
     * Copy text to system clipboard / 复制文本到剪贴板
     *
     * @param content Text to copy / 要复制的文本
     */
    data class CopyToClipboard(val content: String) : VerifiedFinancialCallsEffect
}
