package com.mvi.kenny.feature.lifecycleviewmodel

/**
 * ============================================================
 * LifecycleViewModelContract — Lifecycle ViewModel DSL MVI 契约
 * ============================================================
 * 定义 Lifecycle ViewModel Compose DSL 新 API 开发工具包的状态、意图和副作用。
 *
 * MVI 三要素：
 * - State：页面状态的唯一真相来源，Immutable 数据类
 * - Intent：用户意图（用户操作），ViewModel 收到 Intent 后执行业务逻辑
 * - Effect：一次性副作用（导航、Toast），通过 Channel 传递
 *
 * @see LifecycleViewModelViewModel
 * @see LifecycleViewModelScreen
 */

// ============================================================
// State — 页面状态
// ============================================================

/**
 * 严重程度枚举
 * P0：必须迁移，影响核心功能
 * P1：建议迁移，存在潜在风险
 * P2：可选优化，代码规范建议
 */
enum class Severity {
    P0,  // 高危，必须修复
    P1,  // 中危，建议修复
    P2   // 低危，可选优化
}

/**
 * Tab 枚举
 * Dashboard：健康度总览
 * Scanner：迁移扫描器
 * BestPractice：最佳实践指南
 * Compatibility：兼容层 & 性能分析
 * Migration：迁移 & 测试
 */
enum class LifecycleDslTab {
    Dashboard,     // 健康度总览
    Scanner,       // 迁移扫描器
    BestPractice,  // 最佳实践指南
    Compatibility, // 兼容层 & 性能分析
    Migration      // 迁移 & 测试
}

/**
 * 雷达图数据
 *
 * @param labels 维度标签列表（6个维度）
 * @param values 每个维度的值（0f~1f）
 * @param colors 每个维度的颜色
 */
data class RadarChartData(
    val labels: List<String> = listOf(
        "Factory 用法占比",      // 旧工厂类使用占比
        "Lambda 覆盖率",         // 新 Lambda DSL 覆盖率
        "CreationExtras 规范度", // CreationExtras 使用规范程度
        "兼容性封装层状态",       // 兼容层封装完整性
        "viewModelScope 规范度", // viewModelScope 使用规范
        "测试覆盖率"             // 相关测试用例覆盖
    ),
    val values: List<Float> = listOf(0.5f, 0.3f, 0.6f, 0.4f, 0.7f, 0.2f),
    val colors: List<androidx.compose.ui.graphics.Color> = listOf(
        androidx.compose.ui.graphics.Color(0xFF4CAF50),
        androidx.compose.ui.graphics.Color(0xFF2196F3),
        androidx.compose.ui.graphics.Color(0xFFFF9800),
        androidx.compose.ui.graphics.Color(0xFF9C27B0),
        androidx.compose.ui.graphics.Color(0xFF00BCD4),
        androidx.compose.ui.graphics.Color(0xFFFF5722)
    )
)

/**
 * 扫描结果项
 *
 * @param id 唯一标识
 * @param filePath 文件路径
 * @param lineNumber 行号
 * @param severity 严重程度
 * @param issueType 问题类型
 * @param description 问题描述
 * @param currentCode 当前代码片段
 * @param suggestedCode 建议替换的代码
 * @param isSelected 是否被选中
 */
data class ScanResultItem(
    val id: String,
    val filePath: String,
    val lineNumber: Int,
    val severity: Severity,
    val issueType: String,
    val description: String,
    val currentCode: String,
    val suggestedCode: String,
    val isSelected: Boolean = false
)

/**
 * API 对照表项
 *
 * @param description 描述
 * @param oldApi 旧版 API
 * @param newApi 新版 API
 */
data class ApiCompareItem(
    val description: String,
    val oldApi: String,
    val newApi: String
)

/**
 * 场景推荐项
 *
 * @param scene 场景名称
 * @param codeExample 代码示例
 * @param recommendation 推荐说明
 */
data class SceneRecommendItem(
    val scene: String,
    val codeExample: String,
    val recommendation: String
)

/**
 * 兼容层 SDK 项
 *
 * @param name 名称
 * @param signature 方法签名
 * @param description 描述
 * @param beforeCode 迁移前代码
 * @param afterCode 迁移后代码
 */
data class CompatibilitySDKItem(
    val name: String,
    val signature: String,
    val description: String,
    val beforeCode: String,
    val afterCode: String
)

/**
 * 性能数据点
 *
 * @param label 指标标签
 * @param lambdaValue Lambda DSL 数值
 * @param factoryValue 工厂类数值
 * @param unit 单位
 */
data class PerformanceDataPoint(
    val label: String,
    val lambdaValue: Float,
    val factoryValue: Float,
    val unit: String = "ms"
)

/**
 * 迁移进度
 *
 * @param completed 已完成数量
 * @param inProgress 进行中数量
 * @param notStarted 未开始数量
 * @param total 总数
 * @param progress 进度百分比 0f~1f
 */
data class MigrationProgress(
    val completed: Int = 0,
    val inProgress: Int = 0,
    val notStarted: Int = 10,
    val total: Int = 10,
    val progress: Float = 0f
)

/**
 * 测试模板
 *
 * @param name 模板名称
 * @param testFramework 测试框架（JUnit4/JUnit5）
 * @param content 模板内容
 */
data class TestTemplate(
    val name: String,
    val testFramework: String,
    val content: String
)

/**
 * 决策节点
 *
 * @param id 节点 ID
 * @param question 问题文本
 * @param options 可选选项
 * @param result 推荐结果（叶节点有值）
 */
data class DecisionNode(
    val id: String,
    val question: String,
    val options: List<DecisionOption>,
    val result: String? = null
)

/**
 * 决策选项
 *
 * @param label 选项标签
 * @param nextNodeId 下一节点 ID（null 表示叶节点）
 */
data class DecisionOption(
    val label: String,
    val nextNodeId: String?
)

/**
 * 最近扫描记录
 *
 * @param id 记录 ID
 * @param moduleName 模块名称
 * @param timestamp 时间戳
 * @param resultCount 问题数量
 * @param summary 摘要
 */
data class RecentScanRecord(
    val id: String,
    val moduleName: String,
    val timestamp: Long,
    val resultCount: Int,
    val summary: String
)

/**
 * Lifecycle ViewModel DSL 页面状态
 *
 * @param dashboardHealth 健康度雷达图数据
 * @param scanResults 扫描结果列表
 * @param filteredScanResults 过滤后的扫描结果
 * @param isScanning 是否正在扫描
 * @param scanProgress 扫描进度 0f~1f
 * @param activeTab 当前 Tab
 * @param selectedResult 选中的扫描结果
 * @param severityFilter 严重程度过滤
 * @param scanScope 扫描范围
 * @param includeTestDir 是否包含 test 目录
 * @param showDiffPreview 是否显示 diff 预览
 * @param migrationProgress 迁移进度
 * @param testTemplates 测试模板列表
 * @param apiCompareItems API 对照表列表
 * @param sceneRecommendItems 场景推荐列表
 * @param compatibilitySDKItems 兼容层 SDK 列表
 * @param performanceData 性能数据
 * @param currentDecisionNode 当前决策节点
 * @param decisionPath 决策路径
 * @param recentScans 最近扫描记录
 * @param viewModelScopeChange viewModelScope 变更说明
 * @param errorMessage 错误信息
 */
data class LifecycleDslState(
    val dashboardHealth: RadarChartData = RadarChartData(),
    val scanResults: List<ScanResultItem> = emptyList(),
    val isScanning: Boolean = false,
    val scanProgress: Float = 0f,
    val activeTab: LifecycleDslTab = LifecycleDslTab.Dashboard,
    val selectedResult: ScanResultItem? = null,
    val severityFilter: Severity? = null,
    val scanScope: String = "整个项目",
    val includeTestDir: Boolean = false,
    val showDiffPreview: Boolean = false,
    val migrationProgress: MigrationProgress = MigrationProgress(),
    val testTemplates: List<TestTemplate> = emptyList(),
    val apiCompareItems: List<ApiCompareItem> = emptyList(),
    val sceneRecommendItems: List<SceneRecommendItem> = emptyList(),
    val compatibilitySDKItems: List<CompatibilitySDKItem> = emptyList(),
    val performanceData: List<PerformanceDataPoint> = emptyList(),
    val currentDecisionNode: DecisionNode? = null,
    val decisionPath: List<DecisionNode> = emptyList(),
    val recentScans: List<RecentScanRecord> = emptyList(),
    val viewModelScopeChange: String = "",
    val errorMessage: String? = null
) {
    companion object {
        /** 初始状态 */
        val Initial = LifecycleDslState()
    }

    /**
     * 过滤后的扫描结果
     * 根据严重程度过滤
     */
    val filteredScanResults: List<ScanResultItem>
        get() = if (severityFilter == null) scanResults else scanResults.filter { it.severity == severityFilter }
}

// ============================================================
// Intent — 用户意图
// ============================================================

/**
 * Lifecycle ViewModel DSL 用户意图
 * —————————————————————————————————————————————————————
 * 页面上的每一个用户操作都对应一个 Intent。
 * ViewModel 收到 Intent 后执行业务逻辑，然后更新 State。
 */
sealed interface LifecycleDslIntent {
    /** 开始扫描 */
    data object StartScan : LifecycleDslIntent

    /** 停止扫描 */
    data object StopScan : LifecycleDslIntent

    /** 设置扫描范围
     * @param scope 扫描范围（整个项目 / app 模块 / feature 模块）
     */
    data class SetScanScope(val scope: String) : LifecycleDslIntent

    /** 设置是否包含 test 目录
     * @param include 是否包含
     */
    data class SetIncludeTestDir(val include: Boolean) : LifecycleDslIntent

    /** 按严重程度过滤
     * @param severity 严重程度（null 表示全部）
     */
    data class FilterBySeverity(val severity: Severity?) : LifecycleDslIntent

    /** 选中扫描结果
     * @param item 选中的结果（null 表示取消选中）
     */
    data class SelectResult(val item: ScanResultItem?) : LifecycleDslIntent

    /** 切换扫描结果选中状态
     * @param id 结果 ID
     */
    data class ToggleResultSelection(val id: String) : LifecycleDslIntent

    /** 生成迁移补丁
     * @param items 选中的扫描结果
     */
    data class GeneratePatch(val items: List<ScanResultItem>) : LifecycleDslIntent

    /** 确认生成补丁 */
    data object ConfirmPatch : LifecycleDslIntent

    /** 取消补丁预览 */
    data object CancelPatch : LifecycleDslIntent

    /** 切换 Tab
     * @param tab 目标 Tab
     */
    data class SwitchTab(val tab: LifecycleDslTab) : LifecycleDslIntent

    /** 导航决策树
     * @param nextNodeId 下一节点 ID
     */
    data class NavigateDecision(val nextNodeId: String) : LifecycleDslIntent

    /** 生成测试用例
     * @param viewModelClass ViewModel 类名
     */
    data class GenerateTests(val viewModelClass: String) : LifecycleDslIntent

    /** 刷新 Dashboard */
    data object RefreshDashboard : LifecycleDslIntent

    /** 清除错误信息 */
    data object ClearError : LifecycleDslIntent
}

// ============================================================
// Effect — 副作用
// ============================================================

/**
 * Lifecycle ViewModel DSL 副作用
 * —————————————————————————————————————————————————————
 * 一次性事件，不可变，只能被消费一次。
 * UI 层通过 LaunchedEffect + flow.collect{} 监听并处理。
 */
sealed interface LifecycleDslEffect {
    /** 扫描完成
     * @param resultCount 结果数量
     */
    data class ShowScanComplete(val resultCount: Int) : LifecycleDslEffect

    /** 补丁生成完成
     * @param filePath 文件路径
     */
    data class PatchGenerated(val filePath: String) : LifecycleDslEffect

    /** 测试用例生成完成
     * @param count 生成数量
     * @param dir 输出目录
     */
    data class TestsGenerated(val count: Int, val dir: String) : LifecycleDslEffect

    /** 显示错误
     * @param message 错误信息
     */
    data class ShowError(val message: String) : LifecycleDslEffect

    /** 显示 Toast
     * @param message Toast 文本
     */
    data class ShowToast(val message: String) : LifecycleDslEffect
}
