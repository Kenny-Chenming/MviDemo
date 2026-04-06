package com.mvi.kenny.feature.nav3tool

import androidx.compose.ui.graphics.Color

/**
 * ============================================================
 * NavToolContract — Navigation 3 迁移与多平台工具套件 MVI 契约
 * ============================================================
 * PRD-031 | Navigation 3 迁移与多平台工具套件
 *
 * 功能模块（6个Tab）：
 * - DETECTION  → Nav 2 → Nav 3 自动检测报告
 * - MIGRATION  → AI 辅助迁移引擎（字符串路由 → @Serializable 路由）
 * - VISUALIZER → Navigation 3 可视化调试面板（BackStack / 路由树 / 动画预览）
 * - TEMPLATE   → KMP 通用路由定义模板生成器
 * - SNAPSHOT   → BackStack 状态快照与回放
 * - LIFECYCLE  → Navigation 3 生命周期感知调试器
 *
 * MVI 三要素：
 * - State：页面状态的唯一真相来源，Immutable 数据类
 * - Intent：用户意图，ViewModel 收到 Intent 后执行业务逻辑
 * - Effect：一次性副作用（Snackbar、导航、文件导出）
 *
 * Design reference: memory/agency/designs/PRD-031-Navigation-3-迁移与多平台工具套件.md
 *
 * @see NavToolViewModel 状态管理逻辑
 * @see NavToolScreen UI 渲染层
 */

// ============================================================
// Data Models / 数据模型
// ============================================================

/**
 * 工具模块 Tab 枚举
 * Tab 切换时状态重置，仅保留与当前模块相关的数据
 *
 * @param title 中文标题
 * @param iconName Material 图标名称
 * @param description 模块功能描述
 */
enum class NavToolTab(
    val title: String,
    val iconName: String,
    val description: String
) {
    DETECTION("检测报告", "search", "扫描 NavHostFragment / NavGraph XML / NavArgs 用法"),
    MIGRATION("迁移引擎", "auto_fix_high", "AI 辅助将字符串路由迁移为 @Serializable 强类型路由"),
    VISUALIZER("可视化调试", "account_tree", "实时 BackStack / 路由树 / 过渡动画预览"),
    TEMPLATE("模板生成", "description", "KMP 通用路由定义，一次生成 Android / iOS / Desktop / Web 代码"),
    SNAPSHOT("快照回放", "history", "记录并回放 Navigation BackStack 历史状态"),
    LIFECYCLE("生命周期", " lifecycle", "NavDisplay vs Screen 生命周期映射可视化")
}

/**
 * 扫描进度状态
 *
 * @param status 当前状态：Idle / Scanning / Complete / Error
 * @param progress 0.0 ~ 1.0 扫描进度
 * @param currentFile 正在扫描的当前文件路径
 */
data class ScanProgress(
    val status: ScanStatus = ScanStatus.IDLE,
    val progress: Float = 0f,
    val currentFile: String = ""
)

/** 扫描状态枚举 */
enum class ScanStatus { IDLE, SCANNING, COMPLETE, ERROR }

/**
 * 扫描发现的问题文件
 *
 * @param filePath 文件绝对路径
 * @param issueType 问题类型：NavHostFragment / NavGraphXml / NavArgs / NavDeepLink
 * @param lineNumber 出问题代码行号
 * @param priority 迁移优先级：P0(必须迁移) / P1(强烈建议) / P2(可选) / P3(可忽略)
 * @param estimatedMinutes 预估迁移耗时（分钟）
 * @param codeSnippet 出问题的代码片段
 */
data class NavFileIssue(
    val filePath: String,
    val issueType: IssueType,
    val lineNumber: Int,
    val priority: MigrationPriority,
    val estimatedMinutes: Int,
    val codeSnippet: String
)

/** 问题类型 */
enum class IssueType {
    NavHostFragment,   // XML 中使用 NavHostFragment（Fragment 导航模式）
    NavGraphXml,       // XML NavGraph 定义
    NavArgs,           // NavArgs 生成类（Safe Args 风格）
    NavDeepLink        // DeepLink 定义（字符串路由）
}

/** 迁移优先级 */
enum class MigrationPriority(val label: String, val color: Long) {
    P0("P0 必须迁移", 0xFFB00020),
    P1("P1 强烈建议", 0xFFFF6D00),
    P2("P2 可选优化", 0xFFFFC107),
    P3("P3 可忽略", 0xFF9E9E9E)
}

/**
 * 迁移预览数据
 *
 * @param originalCode 原始代码（左侧）
 * @param migratedCode 迁移后代码（右侧）
 * @param changes 本次迁移涉及的代码变更列表
 * @param hasAiSuggestion 是否包含 AI 建议
 * @param migrationNotes AI 迁移说明
 */
data class MigrationPreview(
    val originalCode: String,
    val migratedCode: String,
    val changes: List<CodeChange>,
    val hasAiSuggestion: Boolean,
    val migrationNotes: String
)

/**
 * 单条代码变更
 *
 * @param lineNumber 代码行号
 * @param changeType 变更类型：ADD / REMOVE / MODIFY
 * @param originalLine 原始代码行
 * @param newLine 迁移后代码行
 * @param explanation 变更说明
 */
data class CodeChange(
    val lineNumber: Int,
    val changeType: ChangeType,
    val originalLine: String,
    val newLine: String,
    val explanation: String
)

enum class ChangeType { ADD, REMOVE, MODIFY }

/**
 * BackStack 快照
 *
 * @param snapshotId 快照唯一 ID
 * @param timestamp 快照时间戳
 * @param backstackItems 当前 BackStack 中的路由节点列表
 * @param currentRoute 当前活动路由
 * @param navDisplayState NavDisplay 当前状态
 */
data class BackstackSnapshot(
    val snapshotId: String,
    val timestamp: Long,
    val backstackItems: List<BackstackItem>,
    val currentRoute: String,
    val navDisplayState: String
)

/**
 * BackStack 中的单个路由节点
 *
 * @param id 节点唯一 ID
 * @param routeName 路由名称
 * @param isActive 是否为当前活动节点
 * @param parentId 父节点 ID（用于路由树展示）
 * @param depth 节点深度（根节点为 0）
 * @param transitionInfo 转场信息
 */
data class BackstackItem(
    val id: String,
    val routeName: String,
    val isActive: Boolean,
    val parentId: String?,
    val depth: Int,
    val transitionInfo: String
)

/**
 * Navigation 生命周期事件
 *
 * @param eventId 事件唯一 ID
 * @param timestamp 事件时间戳
 * @param navDisplayLifecycle NavDisplay 生命周期状态
 * @param screenLifecycle Screen 生命周期状态
 * @param eventType 事件类型
 */
data class NavLifecycleEvent(
    val eventId: String,
    val timestamp: Long,
    val navDisplayLifecycle: String,
    val screenLifecycle: String,
    val eventType: String
)

/**
 * KMP 路由平台枚举
 *
 * @param displayName 平台显示名
 * @param routeSyntax 路由语法说明
 */
enum class KmpPlatform(val displayName: String, val routeSyntax: String) {
    ANDROID("Android", "NavHost + NavGraph (Kotlin Serialization)"),
    IOS("iOS", "SwiftUI NavigationPath (Kotlin Multiplatform)"),
    DESKTOP("Desktop", "Default Compose Navigation"),
    WEB("Web", "popbackstack() semantic")
}

/**
 * 路由定义（用户编辑的 YAML/DSL 路由）
 *
 * @param routeName 路由名称
 * @param path 路由路径，如 /home/{userId}
 * @param arguments 路由参数列表
 * @param deepLinks DeepLink 列表
 * @param isSerializable 是否标记 @Serializable
 */
data class RouteDef(
    val routeName: String,
    val path: String,
    val arguments: List<ArgumentDef> = emptyList(),
    val deepLinks: List<String> = emptyList(),
    val isSerializable: Boolean = true
)

/**
 * 路由参数定义
 *
 * @param name 参数名
 * @param type 参数类型：String / Int / Long / Boolean / Float / Double
 * @param isOptional 是否可选
 * @param defaultValue 可选参数的默认值
 */
data class ArgumentDef(
    val name: String,
    val type: String,
    val isOptional: Boolean = false,
    val defaultValue: String? = null
)

// ============================================================
// MVI State / 状态
// ============================================================

/**
 * Navigation 3 工具页面完整状态
 *
 * @param currentTab 当前激活的 Tab
 * @param scanProgress 扫描进度状态
 * @param scanResults 扫描发现的问题列表
 * @param migrationPreview 当前迁移预览
 * @param backstackSnapshot 当前 BackStack 快照
 * @param lifecycleEvents 生命周期事件列表
 * @param selectedSnapshotId 用户选中的快照 ID（用于回放控制）
 * @param isPlayingSnapshot 是否正在播放快照回放
 * @param generatedTemplate 生成的 KMP 路由模板代码
 * @param isLoading 是否显示加载态
 * @param error 错误信息（null 表示无错误）
 */
data class NavToolState(
    val currentTab: NavToolTab = NavToolTab.DETECTION,
    val scanProgress: ScanProgress = ScanProgress(),
    val scanResults: List<NavFileIssue> = emptyList(),
    val migrationPreview: MigrationPreview? = null,
    val backstackSnapshot: BackstackSnapshot? = null,
    val lifecycleEvents: List<NavLifecycleEvent> = emptyList(),
    val selectedSnapshotId: String? = null,
    val isPlayingSnapshot: Boolean = false,
    val generatedTemplate: Map<KmpPlatform, String> = emptyMap(),
    val selectedPlatforms: Set<KmpPlatform> = setOf(KmpPlatform.ANDROID),
    val routeDefEditor: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
)

// ============================================================
// MVI Intent / 用户意图
// ============================================================

/**
 * Navigation 3 工具用户意图
 * ViewModel 处理 Intent，返回新状态或触发 Effect
 */
sealed class NavToolIntent {

    // ---------- Tab 切换 ----------
    data class SwitchTab(val tab: NavToolTab) : NavToolIntent()

    // ---------- 检测报告 ----------
    data object StartScan : NavToolIntent()
    data object CancelScan : NavToolIntent()
    data class SelectIssue(val issue: NavFileIssue) : NavToolIntent()
    data object ExportScanReport : NavToolIntent()

    // ---------- 迁移引擎 ----------
    data class ApplyMigration(val changes: List<CodeChange>) : NavToolIntent()
    data class PreviewMigration(val code: String) : NavToolIntent()
    data object ConfirmMigration : NavToolIntent()
    data object DiscardMigration : NavToolIntent()

    // ---------- 可视化调试 ----------
    data object RefreshBackstack : NavToolIntent()
    data class SelectBackstackNode(val nodeId: String) : NavToolIntent()
    data object CaptureSnapshot : NavToolIntent()
    data object PlayTransitionPreview : NavToolIntent()

    // ---------- 模板生成 ----------
    data class UpdateRouteDef(val yaml: String) : NavToolIntent()
    data class TogglePlatform(val platform: KmpPlatform) : NavToolIntent()
    data object GenerateTemplate : NavToolIntent()
    data class CopyTemplate(val platform: KmpPlatform) : NavToolIntent()

    // ---------- 快照回放 ----------
    data class SelectSnapshot(val snapshotId: String) : NavToolIntent()
    data object PlaybackSnapshot : NavToolIntent()
    data object PauseSnapshot : NavToolIntent()
    data object StepForward : NavToolIntent()
    data object StepBackward : NavToolIntent()
    data object ImportSnapshot : NavToolIntent()
    data object ExportSnapshot : NavToolIntent()

    // ---------- 生命周期 ----------
    data object StartLifecycleMonitor : NavToolIntent()
    data object StopLifecycleMonitor : NavToolIntent()
    data object ClearLifecycleLog : NavToolIntent()
}

// ============================================================
// MVI Effect / 副作用
// ============================================================

/**
 * Navigation 3 工具一次性副作用
 * 通过 Channel 传递，UI 层消费
 */
sealed class NavToolEffect {

    /** 显示 Snackbar 提示 */
    data class ShowSnackbar(val message: String) : NavToolEffect()

    /** 打开指定文件（IDE 跳转） */
    data class OpenFile(val filePath: String, val lineNumber: Int) : NavToolEffect()

    /** 导出报告到指定路径 */
    data class ExportReport(val reportPath: String) : NavToolEffect()

    /** 复制文本到剪贴板 */
    data class CopyToClipboard(val text: String, val label: String) : NavToolEffect()

    /** 导航到指定路由 */
    data class NavigateTo(val route: String) : NavToolEffect()

    /** 显示错误对话框 */
    data class ShowError(val title: String, val message: String) : NavToolEffect()
}

// ============================================================
// Companion Constants / 伴生常量
// ============================================================

/**
 * NavToolContract 伴生对象
 * 定义主题颜色、间距等视觉规范常量
 */
object NavToolContract {

    /** 视觉规范 — 颜色定义（来自设计文档） */
    object Colors {
        val PrimaryBlue = Color(0xFF1E88E5)
        val PrimaryCyan = Color(0xFF00ACC1)
        val BackgroundDark = Color(0xFF1C1C1E)
        val BackgroundLight = Color(0xFFF5F5F5)
        val SurfaceDark = Color(0xFF2C2C2E)
        val SurfaceLight = Color(0xFFFFFFFF)
        val ErrorColor = Color(0xFFCF6679)
        val ErrorColorDark = Color(0xFFB00020)
        val SuccessColor = Color(0xFF4CAF50)
        val WarningColor = Color(0xFFFFC107)
        val CodeBackground = Color(0xFF1E1E1E)
    }

    /** 视觉规范 — 间距定义 */
    object Spacing {
        const val PagePadding = 24
        const val CardPadding = 16
        const val ElementGapSmall = 8
        const val ElementGapMedium = 16
        const val CornerRadius = 12
    }

    /** 视觉规范 — 字号定义 */
    object Typography {
        const val TitleSize = 20f
        const val SubtitleSize = 16f
        const val BodySize = 14f
        const val CodeSize = 12f
    }
}
