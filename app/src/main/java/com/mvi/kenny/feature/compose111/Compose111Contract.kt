package com.mvi.kenny.feature.compose111

/**
 * ============================================================
 * Compose111Contract — Compose 1.11 破坏性变更检测 MVI 契约
 * ============================================================
 * PRD-130 | Compose 1.11.0-rc01 破坏性变更检测与迁移工具包
 *
 * MVI Architecture:
 * - State: Immutable data class representing UI state
 * - Intent: User intentions that trigger business logic
 * - Effect: One-time side effects via Channel
 *
 * 三大破坏性变更:
 * 1. Text extra line padding 完全移除 (影响最广)
 * 2. DrawLayer API 重命名 (outlineShape → shape, clipToOutline → clip)
 * 3. Material SwipeToReveal 改为 slot-based API
 *
 * @see Compose111ViewModel
 * @see Compose111Screen
 */

// ================================================================
// Tab index constants / Tab 索引常量
// ================================================================
object Compose111Tab {
    const val DASHBOARD = 0
    const val TEXT_PADDING = 1
    const val DRAW_LAYER = 2
    const val SWIPE_TO_REVEAL = 3
    const val REPORT = 4
    const val PREVIEW = 5
}

// ================================================================
// Severity levels / 严重程度级别
// ================================================================
/**
 * Compose 1.11 破坏性变更严重程度
 *
 * CRITICAL: 直接破坏布局 — Text 组件高度突变、Drawable 失效、SwipeToReveal 无法使用
 * WARNING: 需要手动适配 — API 重命名但有兼容写法
 * PASS: 无影响 — 未使用相关 API
 */
enum class Compose111Severity {
    CRITICAL, // 🔴 直接破坏
    WARNING,  // 🟡 需手动适配
    PASS      // 🟢 安全通过
}

// ================================================================
// Health score categories / 健康度分类
// ================================================================
/**
 * 健康度分项分类
 *
 * @property name 分项名称
 * @property weight 权重（影响总分）
 * @property score 当前得分 0-100
 * @property criticalCount Critical 问题数
 * @property warningCount Warning 问题数
 */
data class HealthScoreItem(
    val name: String,
    val weight: Float,
    val score: Float,
    val criticalCount: Int,
    val warningCount: Int
)

// ================================================================
// Text Padding Issue / Text extra padding 问题
// ================================================================
/**
 * Text extra padding 问题项
 *
 * Compose 1.11 移除了 Text 组件首行顶部和末行底部的额外 padding，
 * 这会导致依赖精确 Text 高度的 App（阅读器/新闻/聊天）出现布局突变。
 *
 * @property filePath 文件路径
 * @property lineNumber 代码行号
 * @property codeSnippet 涉事代码片段
 * @property severity 严重程度
 * @property suggestedFix 建议修复方案（Modifier.padding / heightIn / lineHeight）
 * @property isFixed 是否已修复
 */
data class TextPaddingIssue(
    val filePath: String,
    val lineNumber: Int,
    val codeSnippet: String,
    val severity: Compose111Severity,
    val suggestedFix: String,
    val isFixed: Boolean = false
)

// ================================================================
// DrawLayer Issue / DrawLayer API 重命名问题
// ================================================================
/**
 * DrawLayer API 重命名问题项
 *
 * outlineShape → shape, clipToOutline → clip
 * 这是 DrawLayer 语义的重要更新，影响所有使用这些参数的 Compose UI。
 *
 * @property filePath 文件路径
 * @property lineNumber 代码行号
 * @property oldApi 原 API 名称
 * @property newApi 新 API 名称
 * @property codeSnippet 涉事代码片段
 * @property isFixed 是否已修复
 */
data class DrawLayerIssue(
    val filePath: String,
    val lineNumber: Int,
    val oldApi: String,
    val newApi: String,
    val codeSnippet: String,
    val isFixed: Boolean = false
)

// ================================================================
// SwipeToReveal Issue / SwipeToReveal 迁移问题
// ================================================================
/**
 * SwipeToReveal API 迁移问题项
 *
 * Material SwipeToReveal 改为 slot-based API，需要重构使用方式。
 *
 * @property filePath 文件路径
 * @property lineNumber 代码行号
 * @property oldUsage 旧版用法
 * @property newUsage 新版 slot-based 用法
 * @property migrationStep 当前迁移步骤 (1-3)
 * @property isMigrated 是否已迁移
 */
data class SwipeToRevealIssue(
    val filePath: String,
    val lineNumber: Int,
    val oldUsage: String,
    val newUsage: String,
    val migrationStep: Int,
    val isMigrated: Boolean = false
)

// ================================================================
// CI Config type / CI 配置类型
// ================================================================
/**
 * CI 平台类型
 *
 * @param id 平台 ID
 * @param name 平台显示名
 */
enum class CiPlatform(val id: String, val displayName: String) {
    GITHUB_ACTIONS("github", "GitHub Actions"),
    GITLAB_CI("gitlab", "GitLab CI")
}

// ================================================================
// MVI State / MVI 状态
// ================================================================
/**
 * Compose111Screen 页面状态 — 单一数据源
 *
 * @property isScanning 是否正在扫描
 * @property scanProgress 扫描进度 (0.0 - 1.0)
 * @property textPaddingIssues Text padding 问题列表
 * @property drawLayerIssues DrawLayer API 问题列表
 * @property swipeToRevealIssues SwipeToReveal 问题列表
 * @property textPaddingScore Text padding 健康度得分
 * @property drawLayerScore DrawLayer 健康度得分
 * @property swipeToRevealScore SwipeToReveal 健康度得分
 * @property overallHealthScore 综合健康度得分
 * @property criticalCount Critical 问题总数
 * @property warningCount Warning 问题总数
 * @property passCount 通过项总数
 * @property selectedTab 当前选中的 Tab
 * @property reportMarkdown 生成的 Markdown 报告
 * @property ciConfigYaml 生成的 CI YAML 配置
 * @property selectedCiPlatform 选中的 CI 平台
 * @property selectedSeverityFilter 严重性过滤（null = 全部）
 * @property previewFixMode 预览模式：true=修复后, false=修复前
 */
/**
 * Compose111Screen — MVI State
 */
data class Compose111State(
    val isScanning: Boolean = false,
    val scanProgress: Float = 0f,
    val textPaddingIssues: List<TextPaddingIssue> = emptyList(),
    val drawLayerIssues: List<DrawLayerIssue> = emptyList(),
    val swipeToRevealIssues: List<SwipeToRevealIssue> = emptyList(),
    val textPaddingScore: Float = 100f,
    val drawLayerScore: Float = 100f,
    val swipeToRevealScore: Float = 100f,
    val overallHealthScore: Float = 100f,
    val criticalCount: Int = 0,
    val warningCount: Int = 0,
    val passCount: Int = 3, // 三大变更，初始各算 pass
    val selectedTab: Int = 0,
    val reportMarkdown: String = "",
    val ciConfigYaml: String = "",
    val selectedCiPlatform: CiPlatform = CiPlatform.GITHUB_ACTIONS,
    val selectedSeverityFilter: Compose111Severity? = null,
    val previewFixMode: Boolean = false // false=修复前(1.11前), true=修复后(1.11后)
) 

// ================================================================
// MVI Intent / MVI 用户意图
// ================================================================
/**
 * Compose111Screen 用户意图
 */
sealed class Compose111Intent {
    /** 开始全量扫描 / Start full scan */
    object StartScan : Compose111Intent()

    /** 刷新扫描 / Refresh scan */
    object RefreshScan : Compose111Intent()

    /** 标记 Text Padding 问题已修复 / Mark Text padding issue as fixed */
    data class FixTextPadding(val index: Int) : Compose111Intent()

    /** 标记 DrawLayer 问题已修复 / Mark DrawLayer issue as fixed */
    data class FixDrawLayer(val index: Int) : Compose111Intent()

    /** 标记 SwipeToReveal 已迁移 / Mark SwipeToReveal as migrated */
    data class MigrateSwipeToReveal(val index: Int) : Compose111Intent()

    /** 生成合规报告 / Generate compliance report */
    object GenerateReport : Compose111Intent()

    /** 生成 CI 配置 / Generate CI config */
    object GenerateCiConfig : Compose111Intent()

    /** 切换 Tab / Switch tab */
    data class SelectTab(val index: Int) : Compose111Intent()

    /** 选择 CI 平台 / Select CI platform */
    data class SelectCiPlatform(val platform: CiPlatform) : Compose111Intent()

    /** 切换严重性过滤 / Toggle severity filter */
    data class FilterBySeverity(val severity: Compose111Severity?) : Compose111Intent()

    /** 切换预览模式（修复前/修复后）/ Toggle preview mode */
    object TogglePreviewMode : Compose111Intent()

    /** 复制内容到剪贴板 / Copy content to clipboard */
    data class CopyToClipboard(val content: String) : Compose111Intent()
}

// ================================================================
// MVI Effect / MVI 副作用
// ================================================================
/**
 * Compose111Screen 副作用（一次性事件）
 */
sealed class Compose111Effect {
    /** 显示修复建议 / Show fix suggestion */
    data class ShowFixSuggestion(val suggestion: String) : Compose111Effect()

    /** 扫描完成 / Scan complete */
    object ScanComplete : Compose111Effect()

    /** 已复制到剪贴板 / Copied to clipboard */
    data class CopiedToClipboard(val text: String) : Compose111Effect()

    /** 显示错误 / Show error */
    data class ShowError(val message: String) : Compose111Effect()
}
