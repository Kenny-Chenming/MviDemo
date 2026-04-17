package com.mvi.kenny.feature.messagequeue

/**
 * PRD-102 | Android 17 Lock-Free MessageQueue Reflection Breaking Change Detection & Migration Toolkit
 * MVI Contract — Defines State, Intent, and Effect
 *
 * Design Doc: memory/agency/designs/PRD-102-Android-17-Lock-Free-MessageQueue-反射破坏性变更检测与迁移工具包.md
 * Status: 设计完成，待移交开发 | Completed: 2026-04-17
 */

// ============ Data Models ============
// ============ 数据模型 ============

/**
 * Scan phase during full project analysis
 * 完整扫描过程中的各阶段
 */
enum class ScanPhase(val label: String) {
    PARSING_FILES("解析源文件 / Parsing source files"),
    DETECTING_REFLECTION("检测反射用法 / Detecting reflection usage"),
    CHECKING_MESSAGE_QUEUE_ACCESS("检查 MessageQueue 访问 / Checking MessageQueue access"),
    ANALYZING_SEVERITY("分析影响级别 / Analyzing severity"),
    GENERATING_REPORT("生成报告 / Generating report")
}

/**
 * Severity level for MessageQueue reflection issues
 * MessageQueue 反射问题严重级别
 * Severity determination logic:
 * - CRASH: Direct call to Field.set() modifying MessageQueue state
 * - BEHAVIOR_ANOMALY: Field.get() called but ART optimization changes value after inlining
 * - SAFE: Only reading immutable fields
 * 严重级别判定逻辑：
 * - CRASH（直接崩溃）：直接调用 Field.set() 修改 MessageQueue 状态
 * - BEHAVIOR_ANOMALY（行为异常）：调用 Field.get() 但 ART 优化内联后值改变
 * - SAFE（安全）：仅读取不变字段
 */
enum class Severity { SAFE, BEHAVIOR_ANOMALY, CRASH }

/**
 * Overall scan status
 * 整体扫描状态
 */
enum class ScanStatus { IDLE, SCANNING, COMPLETED, ERROR }

/**
 * MessageQueue private fields that trigger lock-free breaking changes
 * 会触发无锁破坏性变更的 MessageQueue 私有字段
 */
enum class MQField(val fieldName: String, val description: String) {
    M_QUEUE("mQueue", "MessageQueue native pointer / 消息队列原生指针"),
    M_MESSAGES("mMessages", "First message in queue / 队列中第一个消息"),
    M_IDLE_HANDLERS("mIdleHandlers", "Idle handler list / 空闲处理器列表"),
    M_QUITING("mQuitting", "Quit flag / 退出标志"),
    M_BLOCKING("mBlocking", "Blocking mode flag / 阻塞模式标志"),
    M_LLOCKS("mLlocks", "Lock objects / 锁对象列表")
}

/**
 * Reflection access point detected in source code
 * 源码中检测到的反射访问点
 */
data class ReflectionAccess(
    val filePath: String,
    val lineNumber: Int,
    val codeSnippet: String,
    val accessedField: MQField,
    val severity: Severity,
    val reflectionType: ReflectionType,
    val contextSnippet: String  // Surrounding code for context / 上下文代码片段
)

/**
 * Type of reflection call
 * 反射调用类型
 */
enum class ReflectionType {
    FIELD_SET_ACCESSIBLE,    // setAccessible(true/false)
    FIELD_GET,               // field.get(instance)
    FIELD_SET                // field.set(instance, value)
}

/**
 * Impact summary for dashboard
 * 仪表盘影响摘要
 */
data class ImpactSummary(
    val totalAccessPoints: Int = 0,
    val crashCount: Int = 0,
    val anomalyCount: Int = 0,
    val safeCount: Int = 0,
    val topRiskPaths: List<ReflectionAccess> = emptyList()
) {
    val healthScore: Int
        get() = if (totalAccessPoints == 0) 100
                else ((safeCount * 100) + (anomalyCount * 50)) / totalAccessPoints
}

/**
 * Alternative solution from knowledge base
 * 知识库中的替代方案
 */
data class AlternativeSolution(
    val id: String,
    val targetPattern: String,         // Original reflection pattern / 原反射模式
    val alternativeApi: String,        // Official API to use / 官方替代 API
    val useCase: String,               // When to use / 使用场景
    val exampleCode: String,           // Example code snippet / 示例代码
    val riskLevel: Severity,           // Migration risk / 迁移风险
    val migrationSteps: List<String>    // Step-by-step migration guide / 逐步迁移指南
)

/**
 * Migration step in the migration engine
 * 迁移引擎中的迁移步骤
 */
data class MigrationStep(
    val id: String,
    val access: ReflectionAccess,
    val targetSolution: AlternativeSolution,
    val previewBefore: String,        // Code before migration / 迁移前代码
    val previewAfter: String,          // Code after migration / 迁移后代码
    val isCompleted: Boolean = false,
    val isInProgress: Boolean = false,
    val errorMessage: String? = null
)

/**
 * Regression test case for validation
 * 回归测试用例
 */
data class RegressionTest(
    val id: String,
    val access: ReflectionAccess,
    val testMethodName: String,
    val testDescription: String,
    val testCode: String,
    val isPassed: Boolean? = null,    // null = not run / 未运行
    val isRunning: Boolean = false
)

/**
 * Report export format
 * 报告导出格式
 */
enum class ReportFormat { HTML, JSON, PDF }

/**
 * Tool settings
 * 工具设置
 */
data class MessageQueueSettings(
    val scanIncludePaths: List<String> = listOf("app/src"),
    val scanExcludePaths: List<String> = emptyList(),
    val ignoredAccessPoints: List<String> = emptyList(),  // FilePath+Line as ID
    val reportFormat: ReportFormat = ReportFormat.HTML,
    val autoBackup: Boolean = true
)

// ============ State ============
// ============ 状态定义 ============

/**
 * Overall UI state for MessageQueue Reflection Toolkit
 * MessageQueue 反射工具包的整体 UI 状态
 */
data class MessageQueueState(
    // Navigation / 导航
    val selectedTab: MessageQueueTab = MessageQueueTab.DASHBOARD,

    // Scan / 扫描状态
    val scanStatus: ScanStatus = ScanStatus.IDLE,
    val currentPhase: ScanPhase? = null,
    val scanPhases: List<ScanPhase> = emptyList(),
    val scanProgress: Float = 0f,
    val scannedFilesCount: Int = 0,
    val totalFilesCount: Int = 0,

    // Impact Summary / 影响摘要
    val impactSummary: ImpactSummary = ImpactSummary(),

    // Scan Results / 扫描结果
    val scanResults: List<ReflectionAccess> = emptyList(),
    val selectedFilter: SeverityFilter = SeverityFilter.ALL,

    // Knowledge Base / 知识库
    val knowledgeBase: List<AlternativeSolution> = emptyList(),
    val knowledgeSearchQuery: String = "",

    // Migration Engine / 迁移引擎
    val migrationSteps: List<MigrationStep> = emptyList(),
    val isMigrationInProgress: Boolean = false,
    val selectedMigrationStep: MigrationStep? = null,

    // Regression Tests / 回归测试
    val regressionTests: List<RegressionTest> = emptyList(),
    val isRunningAllTests: Boolean = false,

    // Settings / 设置
    val settings: MessageQueueSettings = MessageQueueSettings(),

    // Detail / 详情
    val selectedAccess: ReflectionAccess? = null,
    val isDetailSheetOpen: Boolean = false,

    // Error / 错误
    val errorMessage: String? = null
)

/**
 * Navigation tabs for MessageQueue toolkit
 * MessageQueue 工具包的导航标签页
 */
enum class MessageQueueTab(
    val label: String,
    val labelZh: String,
    val description: String
) {
    DASHBOARD("Dashboard", "仪表盘", "健康度总览 / Health overview"),
    SCANNER("Scanner", "扫描器", "MessageQueue 反射用法扫描 / Reflection scan"),
    IMPACT("Impact", "影响分析", "受影响代码影响分析 / Impact analysis"),
    KNOWLEDGE("Knowledge Base", "知识库", "兼容替代方案 / Compatible alternatives"),
    MIGRATION("Migration", "迁移引擎", "自动化迁移 / Auto migration"),
    TESTS("Tests", "回归测试", "验证测试用例生成 / Test generation"),
    SETTINGS("Settings", "设置", "扫描范围与配置 / Scan config")
}

/**
 * Severity filter options
 * 严重级别过滤选项
 */
enum class SeverityFilter(val label: String, val labelZh: String) {
    ALL("All", "全部"),
    CRASH("Crash", "直接崩溃"),
    ANOMALY("Behavior Anomaly", "行为异常"),
    SAFE("Safe", "安全")
}

// ============ Intent ============
// ============ 用户意图 ============

sealed class MessageQueueIntent {
    // Navigation / 导航
    data class SelectTab(val tab: MessageQueueTab) : MessageQueueIntent()

    // Scan / 扫描
    object StartScan : MessageQueueIntent()
    object CancelScan : MessageQueueIntent()

    // Filter / 过滤
    data class FilterBySeverity(val filter: SeverityFilter) : MessageQueueIntent()

    // Detail / 详情
    data class SelectAccess(val access: ReflectionAccess) : MessageQueueIntent()
    object DismissDetail : MessageQueueIntent()

    // Knowledge Base / 知识库
    data class SearchKnowledgeBase(val query: String) : MessageQueueIntent()

    // Migration / 迁移
    data class StartMigration(val step: MigrationStep) : MessageQueueIntent()
    data class PreviewMigration(val step: MigrationStep) : MessageQueueIntent()
    object DismissMigrationPreview : MessageQueueIntent()

    // Regression Tests / 回归测试
    object GenerateAllTests : MessageQueueIntent()
    data class RunTest(val test: RegressionTest) : MessageQueueIntent()
    object RunAllTests : MessageQueueIntent()

    // Settings / 设置
    data class UpdateSettings(val settings: MessageQueueSettings) : MessageQueueIntent()

    // Report / 报告
    data class ExportReport(val format: ReportFormat) : MessageQueueIntent()

    // Error / 错误处理
    object DismissError : MessageQueueIntent()
}

// ============ Effect ============
// ============ 副作用（一次性事件）===========

sealed class MessageQueueEffect {
    data class ShowSnackbar(
        val message: String,
        val isError: Boolean = false
    ) : MessageQueueEffect()

    data class ReportGenerated(
        val filePath: String,
        val format: ReportFormat
    ) : MessageQueueEffect()

    data class ShareFile(val filePath: String) : MessageQueueEffect()

    object MigrationCompleted : MessageQueueEffect()

    data class ShowToast(val message: String) : MessageQueueEffect()
}
