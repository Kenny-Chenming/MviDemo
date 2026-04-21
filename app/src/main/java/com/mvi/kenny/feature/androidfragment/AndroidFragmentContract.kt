package com.mvi.kenny.feature.androidfragment

/**
 * ================================================================
 * AndroidFragmentContract — AndroidFragment Composable 开发工具包 MVI 契约
 * ================================================================
 * PRD-123 | AndroidFragment Composable 开发工具包
 *
 * MVI 三要素：
 * - State: 页面状态的唯一真相来源，Immutable data class
 * - Intent: 用户意图，ViewModel 收到后执行业务逻辑
 * - Effect: 一次性副作用，通过 Channel 传递
 *
 * @see AndroidFragmentViewModel
 * @see AndroidFragmentScreen
 */

// ================================================================
// Tab index constants / Tab 索引常量
// ================================================================
object AndroidFragmentTab {
    const val DASHBOARD = 0
    const val SCENARIO_GUIDE = 1
    const val MIGRATION_SCANNER = 2
    const val LIFECYCLE_DEBUG = 3
    const val RESULT_API = 4
    const val ANIMATION_CONFIG = 5
    const val NAVIGATION_FUSION = 6
}

// ================================================================
// Health dimension / 健康度维度
// ================================================================
/**
 * 健康度维度枚举
 *
 * @param label 中文标签
 * @param weight 权重 (0-1)
 */
enum class HealthDimension(val label: String, val weight: Float) {
    MIGRATION_COVERAGE("迁移覆盖率", 0.20f),
    LIFECYCLE_SYNC("生命周期同步率", 0.20f),
    RESULT_API_COMPLIANCE("Result API 合规率", 0.15f),
    ANIMATION_CONFIG("动画配置率", 0.15f),
    NAVIGATION_FUSION("导航融合度", 0.15f),
    COMPONENT_MODERNIZATION("组件现代化度", 0.15f)
}

// ================================================================
// Scan result / 扫描结果
// ================================================================
/**
 * 迁移扫描结果
 *
 * @param id 唯一标识
 * @param filePath 文件路径
 * @param fragmentType Fragment 类型（FragmentContainerView / AndroidViewBinding / AndroidFragment）
 * @param severity 严重程度 (P0/P1/P2)
 * @param description 描述
 * @param suggestedAction 建议操作
 * @param canMigrate 是否可迁移
 */
data class FragmentScanResult(
    val id: String,
    val filePath: String,
    val fragmentType: FragmentType,
    val severity: ScanSeverity,
    val description: String,
    val suggestedAction: String,
    val canMigrate: Boolean = true
)

/**
 * Fragment 类型
 */
enum class FragmentType {
    FRAGMENT_CONTAINER_VIEW,
    ANDROID_VIEW_BINDING,
    ANDROID_FRAGMENT,
    UNKNOWN
}

/**
 * 扫描严重程度
 *
 * P0: 必须迁移（使用 FragmentContainerView + AndroidViewBinding）
 * P1: 建议迁移（使用 AndroidView 但非最佳实践）
 * P2: 可选（已是 AndroidFragment）
 */
enum class ScanSeverity {
    P0, P1, P2
}

// ================================================================
// Lifecycle event / 生命周期事件
// ================================================================
/**
 * 生命周期事件
 *
 * @param timestamp 时间戳
 * @param state 状态
 * @param isSynced 是否与 Compose 同步
 * @param description 描述
 */
data class LifecycleEvent(
    val timestamp: Long,
    val state: FragmentLifecycleState,
    val isSynced: Boolean,
    val description: String
)

/**
 * Fragment 生命周期状态
 */
enum class FragmentLifecycleState {
    CREATED, STARTED, RESUMED, PAUSED, STOPPED, DESTROYED
}

// ================================================================
// Scenario / 场景方案
// ================================================================
/**
 * 场景方案
 *
 * @param type 方案类型
 * @param title 标题
 * @param description 描述
 * @param reason 选中的原因
 */
data class Scenario(
    val type: ScenarioType,
    val title: String,
    val description: String,
    val reason: String
)

/**
 * 场景方案类型
 */
enum class ScenarioType {
    ANDROID_FRAGMENT,    // 使用 AndroidFragment Composable
    ANDROID_VIEW,        // 继续使用 AndroidView
    FULL_COMPOSE_MIGRATION  // 全量迁移到 Compose
}

// ================================================================
// Transition type / 动画类型
// ================================================================
/**
 * 动画类型
 */
enum class TransitionType {
    ENTER, EXIT, POP_ENTER, POP_EXIT
}

// ================================================================
// Nav graph analysis / 导航图分析
// ================================================================
/**
 * 导航图分析结果
 *
 * @param totalFragments Fragment 总数
 * @param migratedFragments 已迁移到 AndroidFragment 的数量
 * @param suggestions 建议列表
 */
data class NavGraphAnalysis(
    val totalFragments: Int,
    val migratedFragments: Int,
    val suggestions: List<String>
)

// ================================================================
// Alert message / 告警消息
// ================================================================
/**
 * 告警消息
 *
 * @param id 唯一标识
 * @param severity 严重程度
 * @param title 标题
 * @param message 内容
 */
data class AlertMessage(
    val id: String,
    val severity: AlertSeverity,
    val title: String,
    val message: String
)

/**
 * 告警严重程度
 */
enum class AlertSeverity {
    ERROR, WARNING, INFO
}

// ================================================================
// Migration step / 迁移步骤
// ================================================================
/**
 * 迁移步骤
 *
 * @param step 步骤序号
 * @param title 标题
 * @param description 描述
 * @param codeSnippet 代码示例（可选）
 */
data class MigrationStep(
    val step: Int,
    val title: String,
    val description: String,
    val codeSnippet: String? = null
)

// ================================================================
// State / 页面状态
// ================================================================
/**
 * 页面状态
 *
 * @param currentTab 当前 Tab 索引
 * @param healthScore 健康度评分 (0-100)
 * @param healthDimensions 健康度各维度分数
 * @param scanResults 扫描结果列表
 * @param isScanning 是否正在扫描
 * @param scanProgress 扫描进度 (0f ~ 1f)
 * @param lifecycleEvents 生命周期事件列表
 * @param selectedScenario 选中的场景方案
 * @param decisionTreeAnswers 决策树答案
 * @param transitionType 动画类型
 * @param navGraphAnalysis 导航图分析结果
 * @param alertMessages 告警消息列表
 * @param errorMessage 错误信息
 */
data class AndroidFragmentState(
    val currentTab: Int = AndroidFragmentTab.DASHBOARD,
    val healthScore: Int = 0,
    val healthDimensions: Map<HealthDimension, Float> = HealthDimension.entries.associateWith { 0f },
    // Scanner
    val scanResults: List<FragmentScanResult> = emptyList(),
    val isScanning: Boolean = false,
    val scanProgress: Float = 0f,
    val selectedSeverity: ScanSeverity? = null,
    // Lifecycle
    val lifecycleEvents: List<LifecycleEvent> = emptyList(),
    val isLiveMonitoring: Boolean = false,
    // Scenario
    val selectedScenario: Scenario? = null,
    val decisionTreeAnswers: Map<Int, Boolean> = emptyMap(),
    // Animation
    val transitionType: TransitionType = TransitionType.ENTER,
    // Navigation
    val navGraphAnalysis: NavGraphAnalysis? = null,
    // Alerts
    val alertMessages: List<AlertMessage> = emptyList(),
    // Error
    val errorMessage: String? = null
) {
    companion object {
        /** 初始状态 */
        val Initial = AndroidFragmentState()
    }
}

// ================================================================
// Intent / 用户意图
// ================================================================
/**
 * 用户意图
 *
 * @see AndroidFragmentViewModel.sendIntent
 */
sealed interface AndroidFragmentIntent {
    /** 切换 Tab / Select tab */
    data class SelectTab(val index: Int) : AndroidFragmentIntent

    // ----- Scanner -----
    /** 开始扫描 / Start scan */
    data object StartScan : AndroidFragmentIntent

    /** 取消扫描 / Cancel scan */
    data object CancelScan : AndroidFragmentIntent

    /** 按严重程度筛选 / Filter by severity */
    data class FilterBySeverity(val severity: ScanSeverity?) : AndroidFragmentIntent

    // ----- Lifecycle -----
    /** 开始实时监控 / Start live monitoring */
    data object StartLiveMonitoring : AndroidFragmentIntent

    /** 停止实时监控 / Stop live monitoring */
    data object StopLiveMonitoring : AndroidFragmentIntent

    // ----- Scenario -----
    /** 回答决策树问题 / Answer decision tree question */
    data class AnswerDecisionTree(val questionIndex: Int, val answer: Boolean) : AndroidFragmentIntent

    // ----- Animation -----
    /** 设置动画类型 / Set transition type */
    data class SetTransitionType(val type: TransitionType) : AndroidFragmentIntent

    // ----- Navigation -----
    /** 分析导航图 / Analyze nav graph */
    data object AnalyzeNavGraph : AndroidFragmentIntent

    /** 刷新健康度 / Refresh health score */
    data object RefreshHealthScore : AndroidFragmentIntent

    /** 清除错误 / Clear error */
    data object ClearError : AndroidFragmentIntent
}

// ================================================================
// Effect / 副作用
// ================================================================
/**
 * 副作用
 *
 * @see AndroidFragmentViewModel
 */
sealed interface AndroidFragmentEffect {
    /** 显示迁移指南 / Show migration guide */
    data class ShowMigrationGuide(val steps: List<MigrationStep>) : AndroidFragmentEffect

    /** 显示告警 / Show alert */
    data class ShowAlert(val message: String) : AndroidFragmentEffect

    /** 跳转到指定 Tab / Navigate to tab */
    data class NavigateToTab(val tabIndex: Int) : AndroidFragmentEffect

    /** 导出扫描报告 / Export scan report */
    data class ExportScanReport(val filePath: String) : AndroidFragmentEffect
}
