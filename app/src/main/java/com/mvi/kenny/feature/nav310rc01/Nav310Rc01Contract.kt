package com.mvi.kenny.feature.nav310rc01

import androidx.compose.ui.graphics.Color

/**
 * ============================================================
 * Nav310Rc01Contract — Jetpack Navigation 3.1.0-rc01 工具 MVI 契约
 * ============================================================
 * PRD-117 | Jetpack Navigation 3.1.0-rc01 新版 API 变更检测与迁移工具包
 */

// ============================================================
// Data Models / 数据模型
// ============================================================

/** 工具模块 Tab 枚举（5个 Tab） */
enum class Nav31Tab(val title: String, val iconName: String) {
    Dashboard("总览", "dashboard"),
    BreakingChanges("变更检测", "warning"),
    NavDisplay("NavDisplay", "explore"),
    Recipes("Recipes", "menu_book"),
    Migration("迁移调试", "build")
}

/** 升级建议级别 */
enum class UpgradeLevel(val label: String) {
    Mandatory("强制升级"),
    Recommended("建议升级"),
    Optional("可选升级")
}

/** 变更严重程度 */
enum class Severity(val label: String, val colorHex: Long) {
    High("高危", 0xFFF44336),
    Medium("中危", 0xFFFF9800),
    Low("低危", 0xFF4CAF50)
}

/** 变更分类 */
enum class ChangeCategory(val label: String) {
    NavDisplayApi("NavDisplay API 变更"),
    NavEntryHierarchy("NavEntry 层级变化"),
    BehaviorChange("行为变更")
}

/** 环形图数据 */
data class RingChartData(
    val high: Int = 0,
    val medium: Int = 0,
    val low: Int = 0
) {
    val total: Int get() = high + medium + low
}

/** 时间轴节点 */
data class TimelineNode(
    val version: String,
    val date: String,
    val description: String,
    val stability: String
)

/** Breaking Change 单项 */
data class BreakingChangeItem(
    val id: String,
    val category: ChangeCategory,
    val title: String,
    val description: String,
    val oldCode: String,
    val newCode: String,
    val affectedCount: Int,
    val severity: Severity,
    val migrationPriority: Int
)

/** Recipe 卡片 */
data class RecipeCard(
    val id: String,
    val name: String,
    val description: String,
    val applicableScenario: String,
    val toolStatus: ToolStatus,
    val recipeSource: String
)

/** 工具化状态 */
enum class ToolStatus(val label: String, val colorHex: Long) {
    Integrated("已封装", 0xFF4CAF50),
    Pending("待封装", 0xFFFF9800),
    NotApplicable("不适用", 0xFF9E9E9E)
}

/** NavEntry 树形节点 */
data class NavEntryTreeNode(
    val id: String,
    val name: String,
    val type: String,
    val depth: Int,
    val children: List<NavEntryTreeNode> = emptyList(),
    val isExpanded: Boolean = false
)

/** NavEntry 树整体结构 */
data class NavEntryTree(
    val root: NavEntryTreeNode,
    val totalNodes: Int
)

/** 迁移步骤 */
data class MigrationStep(
    val stepNumber: Int,
    val title: String,
    val description: String,
    val isCompleted: Boolean = false,
    val codeSnippet: String? = null
)

// ============================================================
// MVI State / 状态
// ============================================================

data class Nav31State(
    val currentVersion: String = "1.0.0",
    val latestVersion: String = "1.1.0-rc01",
    val upgradeRecommendation: UpgradeLevel = UpgradeLevel.Recommended,
    val breakingChangesRing: RingChartData = RingChartData(),
    val navDisplayReadiness: Float = 0f,
    val breakingChanges: List<BreakingChangeItem> = emptyList(),
    val selectedChange: BreakingChangeItem? = null,
    val isDetecting: Boolean = false,
    val detectProgress: Float = 0f,
    val navDisplayTimeline: List<TimelineNode> = emptyList(),
    val recipes: List<RecipeCard> = emptyList(),
    val selectedRecipe: RecipeCard? = null,
    val navEntryTree: NavEntryTree? = null,
    val activeTab: Nav31Tab = Nav31Tab.Dashboard,
    val migrationSteps: List<MigrationStep> = emptyList(),
    val selectedMigrationStep: MigrationStep? = null,
    val errorMessage: String? = null
)

interface MviState

// ============================================================
// MVI Intent / 用户意图
// ============================================================

sealed class Nav31Intent : MviIntent {
    data class NavigateToTab(val tab: Nav31Tab) : Nav31Intent()
    data object DetectBreakingChanges : Nav31Intent()
    data class SelectChange(val item: BreakingChangeItem?) : Nav31Intent()
    data class GeneratePatch(val items: List<BreakingChangeItem>) : Nav31Intent()
    data object GenerateAllPatches : Nav31Intent()
    data class SelectRecipe(val recipe: RecipeCard?) : Nav31Intent()
    data class ToggleTreeNode(val nodeId: String) : Nav31Intent()
    data class SelectMigrationStep(val step: MigrationStep?) : Nav31Intent()
    data class CompleteMigrationStep(val stepNumber: Int) : Nav31Intent()
    data object ScanLegacyCode : Nav31Intent()
    data object ClearError : Nav31Intent()
}

interface MviIntent

// ============================================================
// MVI Effect / 副作用
// ============================================================

sealed class Nav31Effect : MviEffect {
    data class ShowDetectComplete(val total: Int, val high: Int, val medium: Int, val low: Int) : Nav31Effect()
    data class PatchGenerated(val count: Int) : Nav31Effect()
    data class ShowError(val message: String) : Nav31Effect()
    data class ShowSnackbar(val message: String) : Nav31Effect()
    data class CopyToClipboard(val text: String, val label: String) : Nav31Effect()
}

interface MviEffect

// ============================================================
// Preset Data / 预设数据
// ============================================================

object Nav310Colors {
    val PrimaryBlue = Color(0xFF2196F3)
    val XrPurple = Color(0xFF7C4DFF)
    val HighSeverity = Color(0xFFF44336)
    val MediumSeverity = Color(0xFFFF9800)
    val LowSeverity = Color(0xFF4CAF50)
    val CodeBackground = Color(0xFF1E1E1E)
}

object Nav310Presets {
    val BREAKING_CHANGES = listOf(
        BreakingChangeItem(
            id = "bc-001",
            category = ChangeCategory.NavDisplayApi,
            title = "NavDisplay composable 签名变更",
            description = "NavDisplay composable 新增 requiredFeatures 参数，移除 deprecated 的 spatialAttributes。",
            oldCode = "// 1.0.0\nNavDisplay(\n    navEntry = navEntry,\n    spatialAttributes = SpatialAttributes(...)\n)",
            newCode = "// 1.1.x\nNavDisplay(\n    navEntry = navEntry,\n    requiredFeatures = setOf(Feature.SPATIAL)\n)",
            affectedCount = 12,
            severity = Severity.High,
            migrationPriority = 1
        ),
        BreakingChangeItem(
            id = "bc-002",
            category = ChangeCategory.NavEntryHierarchy,
            title = "NavEntry 父子层级关系变更",
            description = "嵌套 NavHost 场景下，NavEntry 的父子关系从平铺改为树形层级结构。",
            oldCode = "// 1.0.0\nnavGraph.entries.flatMap { it.children }",
            newCode = "// 1.1.x\nnavGraph.entries.forEach { entry ->\n    entry.children.forEach { child -> }\n}",
            affectedCount = 5,
            severity = Severity.Medium,
            migrationPriority = 2
        ),
        BreakingChangeItem(
            id = "bc-003",
            category = ChangeCategory.BehaviorChange,
            title = "NavKey 传递行为变更",
            description = "NavKey 不再默认向上传递，需显式声明 navigateUpWithNavKey。",
            oldCode = "// 1.0.0\nnavController.navigateWithKey(key)",
            newCode = "// 1.1.x\nnavController.navigateWithKey(\n    key,\n    navigateUpWithNavKey = true\n)",
            affectedCount = 8,
            severity = Severity.Low,
            migrationPriority = 3
        )
    )

    val TIMELINE = listOf(
        TimelineNode("1.0.0", "2025-09", "初始 NavDisplay API，basic spatial positioning", "Alpha"),
        TimelineNode("1.0.1", "2025-11", "修复 spatialAttributes 边界问题", "Beta"),
        TimelineNode("1.1.0-rc01", "2026-04", "新增 requiredFeatures，移除 deprecated API", "RC")
    )

    val RECIPES = listOf(
        RecipeCard("recipe-001", "Spatial Navigation", "大屏/折叠屏自适应导航布局", "多屏幕尺寸设备", ToolStatus.Integrated, "nav3-recipes/samples/spatial-nav"),
        RecipeCard("recipe-002", "XR Overlay", "XR 环境下的悬浮导航面板", "AR/VR 设备", ToolStatus.Pending, "nav3-recipes/samples/xr-overlay"),
        RecipeCard("recipe-003", "Adaptive BackStack", "响应式 BackStack 管理", "平板/折叠屏", ToolStatus.Integrated, "nav3-recipes/samples/adaptive-backstack"),
        RecipeCard("recipe-004", "Voice Navigation", "语音驱动的导航指令", "车载/可穿戴设备", ToolStatus.NotApplicable, "nav3-recipes/samples/voice-nav")
    )

    val MIGRATION_STEPS = listOf(
        MigrationStep(1, "更新依赖版本", "将 navigation3 依赖从 1.0.0 升级到 1.1.0-rc01", false, "dependencies {\n    implementation(\"androidx.navigation:navigation-compose:1.1.0-rc01\")\n}"),
        MigrationStep(2, "迁移 NavDisplay API", "将 spatialAttributes 参数替换为 requiredFeatures", false, "NavDisplay(\n    navEntry = navEntry,\n    requiredFeatures = setOf(Feature.SPATIAL)\n)"),
        MigrationStep(3, "调整 NavEntry 层级访问", "NavEntry 子节点访问方式从平铺改为树形遍历", false, "navGraph.entries.forEach { entry ->\n    processEntryTree(entry)\n}"),
        MigrationStep(4, "显式声明 NavKey 传递", "为需要向上传递的 NavKey 添加 navigateUpWithNavKey 参数", false, "navController.navigateWithKey(\n    key,\n    navigateUpWithNavKey = true\n)"),
        MigrationStep(5, "回归测试", "运行 Breaking Changes 回归测试用例", false, null)
    )

    val NAV_ENTRY_TREE = NavEntryTree(
        root = NavEntryTreeNode(
            id = "root",
            name = "NavGraph: main",
            type = "NavGraph",
            depth = 0,
            children = listOf(
                NavEntryTreeNode(
                    id = "entry-home",
                    name = "NavEntry: home",
                    type = "NavEntry",
                    depth = 1,
                    children = listOf(
                        NavEntryTreeNode("display-home", "NavDisplay: home_display", "NavDisplay", 2),
                        NavEntryTreeNode("key-home", "NavKey: home_key", "NavKey", 2)
                    )
                ),
                NavEntryTreeNode(
                    id = "entry-detail",
                    name = "NavEntry: detail",
                    type = "NavEntry",
                    depth = 1,
                    children = listOf(
                        NavEntryTreeNode("display-detail", "NavDisplay: detail_display", "NavDisplay", 2)
                    )
                )
            )
        ),
        totalNodes = 7
    )
}
