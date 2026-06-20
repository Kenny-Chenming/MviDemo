package com.mvi.kenny.feature.xrsdkdevkit

import androidx.compose.ui.graphics.Color

/**
 * ============================================================
 * XrDevKitContract — Android XR SDK DP4 开发工具包 MVI 契约
 * XrDevKitContract — Android XR SDK DP4 Dev Toolkit MVI Contract
 * ============================================================
 * MVI (Model-View-Intent) Architecture Pattern.
 *
 * PRD-289: Android XR SDK Developer Preview 4 发布，2026年秋季 XR 眼镜硬件即将上市，
 * 市场完全缺乏 Android XR SDK 专用开发工具。
 *
 * PRD-289: Android XR SDK Developer Preview 4 released; XR glasses hardware
 * launching Fall 2026. Market has zero Android XR SDK development tools.
 */

// =============================================================
// Colors — 视觉配色常量（Developer Tools + XR 科技感）
// Colors — Vision palette constants (Developer Tools + XR Tech)
// =============================================================
/** Primary: Material Blue 700 / 主色 */
val XrDevKitPrimary = Color(0xFF1976D2)

/** Secondary: Teal 600 / XR spatial tech accent / 次色：XR 空间科技感 */
val XrDevKitSecondary = Color(0xFF00897B)

/** Accent: Amber 600 for important notices / 强调色：重要提示 */
val XrDevKitAccent = Color(0xFFFFB300)

/** Surface background / 背景色 */
val XrDevKitSurface = Color(0xFFFAFAFA)

/** Surface background dark / 深色背景 */
val XrDevKitSurfaceDark = Color(0xFF121212)

/** Code block background / 代码块背景 */
val XrDevKitCodeBg = Color(0xFFF5F5F5)

/** Code block background dark / 深色代码块背景 */
val XrDevKitCodeBgDark = Color(0xFF212121)

// =============================================================
// Tab definitions — Tab 定义
// =============================================================
/**
 * Eleven XR technical domain tabs / XR 技术领域 Tab
 *
 * @param title Tab title (English) / Tab 标题（英文）
 * @param titleZh Tab title (Chinese) / Tab 标题（中文）
 * @param iconName Material icon name for tab / Tab 图标名称
 */
enum class XrTab(
    val title: String,
    val titleZh: String,
    val iconName: String
) {
    OVERVIEW("Overview", "概览", "Home"),
    COMPOSE_XR("Compose XR", "Compose XR", "Dashboard"),
    GLIMMER("Glimmer", "Glimmer 眼镜UI", "Visibility"),
    SCENE_CORE("SceneCore", "SceneCore 3D", "3DRotation"),
    AR_CORE("ARCore XR", "ARCore XR", "ViewInAr"),
    SIMULATOR("Simulator", "XR 模拟器", "Phonelink"),
    AUDIO_GLASSES("Audio Glasses", "音频眼镜", "Headphones"),
    DISPLAY_GLASSES("Display Glasses", "显示眼镜", "Vrpano"),
    GEMINI_XR("Gemini × XR", "Gemini × XR", "AutoAwesome"),
    DECISION_TREE("Decision Tree", "决策树", "AccountTree"),
    CATALYST("Catalyst", "Catalyst 申请", "Rocket")
}

// =============================================================
// Tool Card — 工具卡片数据模型
// =============================================================
/**
 * Tool/Guide card data model / 工具/指南卡片数据模型
 *
 * @param id Unique card ID / 唯一卡片 ID
 * @param title Title (English) / 标题（英文）
 * @param titleZh Title (Chinese) / 标题（中文）
 * @param description Brief description / 简介
 * @param descriptionZh 简介（中文）
 * @param category Tool category / 工具类别
 * @param difficulty Difficulty level: Beginner/Intermediate/Advanced / 难度级别
 * @param isFavorite Whether this card is favorited / 是否收藏
 * @param isExpanded Whether card content is expanded / 是否展开
 */
data class ToolCard(
    val id: String,
    val title: String,
    val titleZh: String,
    val description: String,
    val descriptionZh: String,
    val category: XrTab,
    val difficulty: Difficulty = Difficulty.INTERMEDIATE,
    val isFavorite: Boolean = false,
    val isExpanded: Boolean = false,
    val contentItems: List<ToolContentItem> = emptyList()
)

/**
 * Tool content item within expanded card / 展开卡片内的工具内容条目
 *
 * @param id Item ID / 条目 ID
 * @param title Item title / 条目标题
 * @param titleZh 条目标题（中文）
 * @param codeSnippets Code snippets for this item / 代码片段
 * @param steps Step-by-step instructions / 分步骤说明
 * @param stepsZh 分步骤说明（中文）
 * @param warnings Warning notes / 警告注意
 * @param warningsZh 警告注意（中文）
 * @param relatedCards Related card IDs / 相关卡片 ID
 */
data class ToolContentItem(
    val id: String,
    val title: String,
    val titleZh: String,
    val codeSnippets: List<CodeSnippet> = emptyList(),
    val steps: List<String> = emptyList(),
    val stepsZh: List<String> = emptyList(),
    val warnings: List<String> = emptyList(),
    val warningsZh: List<String> = emptyList(),
    val relatedCards: List<String> = emptyList()
)

/**
 * Code snippet data model / 代码片段数据模型
 *
 * @param language Programming language / 编程语言
 * @param label Short label for code / 代码简短标签
 * @param labelZh 代码标签（中文）
 * @param code Code content / 代码内容
 * @param filename Suggested file name / 建议文件名
 */
data class CodeSnippet(
    val id: String? = null, // Optional ID for copy tracking / 用于复制追踪的 ID
    val language: String,
    val label: String,
    val labelZh: String,
    val code: String,
    val filename: String = ""
)

/**
 * Difficulty level for tool cards / 工具卡片难度级别
 */
enum class Difficulty(val label: String, val labelZh: String) {
    BEGINNER("Beginner", "入门"),
    INTERMEDIATE("Intermediate", "进阶"),
    ADVANCED("Advanced", "高级")
}

// =============================================================
// Overview metrics — 概览指标
// =============================================================
/**
 * Overview statistics for dashboard / 仪表板概览统计数据
 *
 * @param totalTools Total number of tools/guides / 工具/指南总数
 * @param newTools New tools added count / 新增工具数
 * @param updatedTools Recently updated count / 最近更新数
 * @param categories Total category count / 类别总数
 */
data class OverviewStats(
    val totalTools: Int = 10,
    val newTools: Int = 3,
    val updatedTools: Int = 5,
    val categories: Int = 11
)

// =============================================================
// State — 页面状态
// =============================================================
/**
 * XR Dev Kit State / XR 开发工具包状态
 *
 * @param currentTab Currently active XR tab / 当前活跃的 XR Tab
 * @param isSearchActive Whether search mode is active / 搜索模式是否激活
 * @param searchQuery Current search query / 当前搜索查询
 * @param searchResults Search results list / 搜索结果列表
 * @param isLoading Whether content is loading / 内容是否加载中
 * @param error Error message if any / 错误信息
 * @param favorites Set of favorited card IDs / 收藏卡片 ID 集合
 * @param expandedCards Set of expanded card IDs / 已展开卡片 ID 集合
 * @param overviewStats Overview statistics / 概览统计数据
 * @param toolCards All tool cards / 所有工具卡片
 * @param copiedCodeId Currently copied code snippet ID / 当前复制的代码片段 ID
 */
data class XrDevKitState(
    val currentTab: XrTab = XrTab.OVERVIEW,
    val isSearchActive: Boolean = false,
    val searchQuery: String = "",
    val searchResults: List<ToolCard> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val favorites: Set<String> = emptySet(),
    val expandedCards: Set<String> = emptySet(),
    val overviewStats: OverviewStats = OverviewStats(),
    val toolCards: List<ToolCard> = emptyList(),
    val copiedCodeId: String? = null
) {
    companion object {
        /** Initial state / 初始状态 */
        val Initial = XrDevKitState()
    }
}

// =============================================================
// Intent — 用户意图
// =============================================================
/**
 * XR Dev Kit User Intents / XR 开发工具包用户意图
 *
 * Every user action corresponds to an Intent.
 * 所有用户操作对应一个 Intent。
 *
 * @see XrDevKitViewModel.sendIntent Process all intents
 */
sealed interface XrDevKitIntent {
    /** Select tab / 选择 Tab
     * @param tab Tab to select / 要选择的 Tab
     */
    data class SelectTab(val tab: XrTab) : XrDevKitIntent

    /** Toggle search mode / 切换搜索模式
     * @param active Whether to activate search / 是否激活搜索
     */
    data class SetSearchActive(val active: Boolean) : XrDevKitIntent

    /** Update search query / 更新搜索查询
     * @param query Search query text / 搜索查询文本
     */
    data class UpdateSearchQuery(val query: String) : XrDevKitIntent

    /** Search all content / 搜索所有内容
     * @param query Search query / 搜索查询
     */
    data class Search(val query: String) : XrDevKitIntent

    /** Toggle card favorite status / 切换卡片收藏状态
     * @param cardId Card ID / 卡片 ID
     */
    data class ToggleFavorite(val cardId: String) : XrDevKitIntent

    /** Toggle card expanded state / 切换卡片展开状态
     * @param cardId Card ID / 卡片 ID
     */
    data class ToggleCardExpand(val cardId: String) : XrDevKitIntent

    /** Clear search and exit search mode / 清除搜索并退出搜索模式 */
    data object ClearSearch : XrDevKitIntent

    /** Navigate to top of current tab / 导航到当前 Tab 顶部 */
    data object ScrollToTop : XrDevKitIntent

    /** Dismiss/copy toast / 关闭复制提示
     * @param codeId Code snippet ID that was copied / 已复制的代码片段 ID
     */
    data class DismissCopiedToast(val codeId: String?) : XrDevKitIntent
}

// =============================================================
// Effect — 副作用
// =============================================================
/**
 * XR Dev Kit Side Effects / XR 开发工具包副作用
 *
 * One-time events, immutable, consumed only once.
 * 一次性事件，不可变，仅消费一次。
 *
 * @see XrDevKitViewModel Send via _effect.send()
 */
sealed interface XrDevKitEffect {
    /** Show toast message / 显示 Toast 消息
     * @param message Message to display / 要显示的消息
     */
    data class ShowToast(val message: String) : XrDevKitEffect

    /** Copy code to clipboard / 复制代码到剪贴板
     * @param code Code to copy / 要复制的代码
     * @param codeId Code snippet ID for tracking / 代码片段 ID（用于追踪）
     */
    data class CopyToClipboard(val code: String, val codeId: String) : XrDevKitEffect

    /** Scroll to top of list / 滚动到列表顶部 */
    data object ScrollToTop : XrDevKitEffect
}
