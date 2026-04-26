package com.mvi.kenny.feature.wearos7tools

// ================================================================
// WearOS7ToolsContract — Wear OS 7 开发者工具箱 MVI 契约
// ================================================================
// MVI architecture contract for Wear OS 7 developer toolkit.
//
// PRD-164: Wear OS 7 开发工具包
// Design Reference: memory/agency/designs/PRD-164-Wear-OS-7-开发工具包.md
//
// MVI 三要素 / Three pillars:
//   Model (State)  — Immutable page state, single source of truth
//   View           — Composable function, consumes State, renders UI
//   Intent         — User intentions, ViewModel processes and updates State
//   Effect         — One-time side effects (navigation, toast), via Channel
//
// Google I/O 2026 (May 19-20) — Wear OS 7 will be officially announced.
// Tools covered: Live Updates migration, Health API × Health Connect,
// Compose for Wear OS best practices, multi-screen CI adaptation,
// Health API Mock testing, Wear OS 7 API compliance, Android XR synergy.
// ================================================================

/**
 * ============================================================
 * WearOS7Tab — 底部 Tab 分类枚举
 * ============================================================
 *
 * @param title Tab display title
 * @param toolsCount Number of tools in this tab
 */
enum class WearOS7Tab(val title: String, val toolsCount: Int) {
    Overview("总览", 8),         // Tool 0: Dashboard
    LiveUpdates("LiveUpdates", 2),  // Tool 1: Migration Guide, Tool 2: Watch UI Template
    Health("健康", 2),           // Tool 3: Health API × Health Connect, Tool 6: Health API Mock
    Compose("Compose", 1),      // Tool 4: Compose for Wear OS best practices
    Compliance("合规", 2)        // Tool 5: Multi-screen CI, Tool 7: API compliance
}

/**
 * ============================================================
 * WearOS7Tool — 8个独立工具枚举
 * ============================================================
 *
 * @param id Tool unique ID
 * @param name Tool display name
 * @param description Tool description
 * @param tabBelongsTo Which tab this tool belongs to
 */
enum class WearOS7Tool(
    val id: String,
    val displayName: String,
    val description: String,
    val tabBelongsTo: WearOS7Tab
) {
    // Tool 1: Live Updates migration guide (phone ↔ watch dual-screen view)
    LiveUpdatesGuide(
        id = "live_updates_guide",
        displayName = "Live Updates 迁移指南",
        description = "Live Update 如何同时在手机和手表上正确渲染，跨设备协同模板",
        tabBelongsTo = WearOS7Tab.LiveUpdates
    ),
    // Tool 2: Live Updates Watch UI template
    LiveUpdatesUI(
        id = "live_updates_ui",
        displayName = "Live Updates Watch UI 模板",
        description = "Watch 屏幕的 Live Update 布局规范：圆形/OLED/方形屏幕适配",
        tabBelongsTo = WearOS7Tab.LiveUpdates
    ),
    // Tool 3: Health API × Health Connect synergy tool
    HealthConnect(
        id = "health_connect",
        displayName = "健康 API × Health Connect",
        description = "跨设备健康数据同步 SDK，标准化数据读写路径",
        tabBelongsTo = WearOS7Tab.Health
    ),
    // Tool 4: Compose for Wear OS best practices
    ComposeWearOS(
        id = "compose_wearos",
        displayName = "Compose for Wear OS 最佳实践",
        description = "Tiles/Complications/WatchFace 的 Compose 化开发指南",
        tabBelongsTo = WearOS7Tab.Compose
    ),
    // Tool 5: Multi-screen CI adaptation detector
    MultiScreenCI(
        id = "multi_screen_ci",
        displayName = "多屏幕尺寸 CI 适配检测",
        description = "Gradle 插件，检测 Watch App 在不同屏幕形态下的布局合规性",
        tabBelongsTo = WearOS7Tab.Compliance
    ),
    // Tool 6: Health Services API Mock testing framework
    HealthMock(
        id = "health_mock",
        displayName = "Health API Mock 测试框架",
        description = "Health API 单元测试模板，支持模拟传感器数据",
        tabBelongsTo = WearOS7Tab.Health
    ),
    // Tool 7: Wear OS 7 API compliance detector
    Compliance(
        id = "compliance",
        displayName = "Wear OS 7 新 API 合规检测",
        description = "验证 App 是否正确调用 Wear OS 7 新 API，输出兼容性报告",
        tabBelongsTo = WearOS7Tab.Compliance
    ),
    // Tool 8: Wear OS × Android XR synergy template
    XR协同(
        id = "xr_synergy",
        displayName = "Wear OS × Android XR 协同",
        description = "手表 + XR 眼镜的跨设备场景示例：通知接力/健康数据共享",
        tabBelongsTo = WearOS7Tab.Overview
    )
}

/**
 * ============================================================
 * WatchFormFactor — Watch 屏幕形态枚举
 * ============================================================
 *
 * @param displayName 中文显示名称
 * @param description 形态描述
 */
enum class WatchFormFactor(val displayName: String, val description: String) {
    Round("圆形", "圆形 Watch 屏幕，适合大多数 Wear OS 设备"),
    OLED("OLED 方形", "低功耗 OLED 方形屏幕，如 Galaxy Watch FE"),
    Square("传统方形", "传统方形屏幕，较少见")
}

/**
 * ============================================================
 * ScanState — 扫描/检测状态枚举
 * ============================================================
 *
 * @param displayName 中文显示名称
 */
enum class ScanState(val displayName: String) {
    Idle("空闲"),
    Scanning("扫描中"),
    Success("完成"),
    Error("错误")
}

/**
 * ============================================================
 * ComplianceLevel — 合规级别枚举
 * ============================================================
 *
 * @param emoji Emoji representation
 * @param displayName 中文显示名称
 * @param colorHex 颜色值（Compose Color）
 */
enum class ComplianceLevel(val emoji: String, val displayName: String, val colorHex: Long) {
    NonCompliant("🔴", "不合规", 0xFFF44336),
    Warning("🟡", "警告", 0xFFFF9800),
    Compliant("🟢", "合规", 0xFF4CAF50)
}

// ================================================================
// State / Intent / Effect — MVI Core
// ================================================================

/**
 * ============================================================
 * WearOS7ToolsState — Wear OS 7 工具箱页面状态（MVI State）
 * ============================================================
 * Immutable page state, single source of truth.
 *
 * @param selectedTab Currently selected bottom tab
 * @param selectedTool Currently selected tool (null = showing dashboard)
 * @param isScanning Whether scan/compliance check is in progress
 * @param scanProgress Scan progress (0.0 ~ 1.0)
 * @param scanResults List of compliance scan results
 * @param watchFormFactor Current watch form factor for UI preview
 * @param codeCopied Whether code was just copied (for toast feedback)
 * @param expandedCodeBlockId ID of expanded code block for live updates guide
 * @param errorMessage Error message if any
 *
 * @see WearOS7Tab
 * @see WearOS7Tool
 * @see WatchFormFactor
 */
data class WearOS7ToolsState(
    val selectedTab: WearOS7Tab = WearOS7Tab.Overview,
    val selectedTool: WearOS7Tool? = null,
    val isScanning: Boolean = false,
    val scanProgress: Float = 0f,
    val scanResults: List<ComplianceResult> = emptyList(),
    val watchFormFactor: WatchFormFactor = WatchFormFactor.Round,
    val codeCopied: Boolean = false,
    val expandedCodeBlockId: String? = null,
    val errorMessage: String? = null
) {
    companion object {
        /** Initial/empty state */
        val Initial = WearOS7ToolsState()
    }
}

/**
 * ============================================================
 * WearOS7ToolsIntent — 用户意图（User Intent）
 * ============================================================
 * Every user action on the page corresponds to an Intent.
 * ViewModel receives Intent, processes business logic, then updates State.
 *
 * @see WearOS7ToolsViewModel.sendIntent handles all Intents
 */
sealed interface WearOS7ToolsIntent {

    /** 用户切换底部 Tab
     * @param tab Target tab
     */
    data class SelectTab(val tab: WearOS7Tab) : WearOS7ToolsIntent

    /** 用户点击工具卡片，进入工具面板
     * @param tool Target tool
     */
    data class SelectTool(val tool: WearOS7Tool) : WearOS7ToolsIntent

    /** 用户点击返回，回到总览
     */
    data object BackToOverview : WearOS7ToolsIntent

    /** 用户切换 Watch 屏幕形态预览
     * @param formFactor Target form factor
     */
    data class ChangeWatchFormFactor(val formFactor: WatchFormFactor) : WearOS7ToolsIntent

    /** 用户点击"一键扫描"按钮（合规检测工具）
     */
    data object StartScan : WearOS7ToolsIntent

    /** 用户点击代码示例的"复制"按钮
     * @param codeBlockId Code block ID
     */
    data class CopyCode(val codeBlockId: String) : WearOS7ToolsIntent

    /** 用户点击"导出报告"按钮
     */
    data object ExportReport : WearOS7ToolsIntent

    /** 用户清除错误消息
     */
    data object ClearError : WearOS7ToolsIntent

    /** 用户展开/折叠代码块
     * @param codeBlockId Code block ID
     */
    data class ToggleCodeBlock(val codeBlockId: String) : WearOS7ToolsIntent
}

/**
 * ============================================================
 * WearOS7ToolsEffect — 一次性副作用（Effect）
 * ============================================================
 * One-time events, immutable, can only be consumed once.
 * UI layer listens via LaunchedEffect + flow.collect{}.
 *
 * @see WearOS7ToolsViewModel _effect.send() sends Effects
 */
sealed interface WearOS7ToolsEffect {

    /** 显示 Snackbar/Toast 消息
     * @param message Message text
     */
    data class ShowToast(val message: String) : WearOS7ToolsEffect

    /** 导航到具体指南/模板
     * @param tool Target tool
     */
    data class NavigateToGuide(val tool: WearOS7Tool) : WearOS7ToolsEffect

    /** 分享报告
     * @param reportPath Report file path
     */
    data class ShareReport(val reportPath: String) : WearOS7ToolsEffect

    /** 显示错误
     * @param message Error message
     */
    data class ShowError(val message: String) : WearOS7ToolsEffect
}

// ================================================================
// 数据模型 / Data Models
// ================================================================

/**
 * ============================================================
 * ComplianceResult — 合规检测结果
 * ============================================================
 *
 * @param id Unique result ID
 * @param toolName Tool that generated this result
 * @param filePath Affected file path
 * @param lineNumber Line number (if applicable)
 * @param issueDescription Issue description
 * @param severity Issue severity
 * @param complianceLevel Compliance level
 * @param suggestedFix Suggested fix description
 * @param isFixed Whether this issue has been marked as fixed
 */
data class ComplianceResult(
    val id: String,
    val toolName: String,
    val filePath: String,
    val lineNumber: Int? = null,
    val issueDescription: String,
    val severity: String, // "P0", "P1", "P2"
    val complianceLevel: ComplianceLevel,
    val suggestedFix: String,
    val isFixed: Boolean = false
)

/**
 * ============================================================
 * ToolCard — 工具卡片数据
 * ============================================================
 *
 * @param tool The tool enum
 * @param isNew Whether this is a new tool (for badge)
 * @param hasWarning Whether there are warnings for this tool
 */
data class ToolCard(
    val tool: WearOS7Tool,
    val isNew: Boolean = false,
    val hasWarning: Boolean = false
)

/**
 * ============================================================
 * WatchScreenPreview — Watch 屏幕预览数据
 * ============================================================
 *
 * @param formFactor Watch form factor
 * @param content Preview content to display
 */
data class WatchScreenPreview(
    val formFactor: WatchFormFactor,
    val content: String
)

// ================================================================
// 静态内容数据 / Static Content Data
// ================================================================

/**
 * ============================================================
 * All 8 tools organized by tab — for dashboard display
 * ============================================================
 */
val wearOS7ToolsByTab: Map<WearOS7Tab, List<ToolCard>> = mapOf(
    WearOS7Tab.Overview to listOf(
        ToolCard(WearOS7Tool.LiveUpdatesGuide, isNew = true),
        ToolCard(WearOS7Tool.LiveUpdatesUI, isNew = true),
        ToolCard(WearOS7Tool.HealthConnect),
        ToolCard(WearOS7Tool.ComposeWearOS),
        ToolCard(WearOS7Tool.MultiScreenCI),
        ToolCard(WearOS7Tool.HealthMock),
        ToolCard(WearOS7Tool.Compliance, isNew = true),
        ToolCard(WearOS7Tool.XR协同, isNew = true)
    ),
    WearOS7Tab.LiveUpdates to listOf(
        ToolCard(WearOS7Tool.LiveUpdatesGuide, isNew = true),
        ToolCard(WearOS7Tool.LiveUpdatesUI, isNew = true)
    ),
    WearOS7Tab.Health to listOf(
        ToolCard(WearOS7Tool.HealthConnect),
        ToolCard(WearOS7Tool.HealthMock)
    ),
    WearOS7Tab.Compose to listOf(
        ToolCard(WearOS7Tool.ComposeWearOS)
    ),
    WearOS7Tab.Compliance to listOf(
        ToolCard(WearOS7Tool.MultiScreenCI),
        ToolCard(WearOS7Tool.Compliance, isNew = true)
    )
)
