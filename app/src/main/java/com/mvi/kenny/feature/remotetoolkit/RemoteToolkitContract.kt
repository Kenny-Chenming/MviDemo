package com.mvi.kenny.feature.remotetoolkit

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Speed
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

// =============================================================
// RemoteToolkitContract — Jetpack Compose Remote Server-Driven
// Native UI 开发工具包 MVI 契约
// =============================================================
// MVI Architecture Pattern / MVI 架构模式
//
// - Model (State): Immutable data class, single source of truth
// - View: Composable functions that consume State
// - Intent: User intentions processed by ViewModel
// - Effect: One-time side effects (navigation, toast, etc.)
//
// @see RemoteToolkitViewModel State management

// =============================================================
// RemoteToolkitPage — 工具包页面枚举
// =============================================================
/**
 * Remote Toolkit page / 工具包页面
 *
 * @param title Display title / 显示标题
 * @param icon Navigation icon / 导航图标
 */
enum class RemoteToolkitPage(
    val title: String,
    val icon: ImageVector
) {
    DASHBOARD("Dashboard", Icons.Filled.Dashboard),
    SCANNER("Compatibility Scanner", Icons.Filled.Search),
    SDK("Server SDK", Icons.Filled.Code),
    DEBUG("Debug Panel", Icons.Filled.BugReport),
    RPC("RPC Framework", Icons.Filled.Call),
    SECURITY("Security Layer", Icons.Filled.Shield),
    VERSION("Version Manager", Icons.Filled.History),
    PERFORMANCE("Performance", Icons.Filled.Speed),
    AI_PIPELINE("AI Pipeline", Icons.Filled.AutoAwesome),
    AB_TESTING("A/B Testing", Icons.Filled.BarChart)
}

// =============================================================
// ScanStatus — 扫描状态
// =============================================================
/**
 * Scan status / 扫描状态
 */
enum class ScanStatus { IDLE, RUNNING, DONE }

// =============================================================
// ServerStatus — 调试服务器状态
// =============================================================
/**
 * Debug server status / 调试服务器状态
 */
enum class ServerStatus { STOPPED, STARTING, RUNNING }

// =============================================================
// CompatibilityIssue — 兼容性问题
// =============================================================
/**
 * Compatibility issue / 兼容性问题
 *
 * @param id Issue unique ID / 问题唯一ID
 * @param severity Severity level / 严重程度
 * @param composableName Composable name causing issue / 导致问题的 Composable 名称
 * @param filePath File path / 文件路径
 * @param line Line number / 行号
 * @param reason Reason why this is an issue / 问题原因
 * @param suggestion Fix suggestion / 修复建议
 */
data class CompatibilityIssue(
    val id: String,
    val severity: IssueSeverity,
    val composableName: String,
    val filePath: String,
    val line: Int,
    val reason: String,
    val suggestion: String
)

/**
 * Issue severity / 问题严重程度
 */
enum class IssueSeverity(val label: String, val color: Color) {
    CRITICAL("Critical / 阻塞", Color(0xFFCF6679)),
    WARNING("Warning / 警告", Color(0xFFFFAB40)),
    PASS("Pass / 通过", Color(0xFF03DAC6))
}

// =============================================================
// Activity — 最近活动
// =============================================================
/**
 * Recent activity / 最近活动
 *
 * @param id Activity ID / 活动ID
 * @param description Activity description / 活动描述
 * @param timestamp Timestamp in millis / 时间戳（毫秒）
 * @param type Activity type / 活动类型
 */
data class Activity(
    val id: String,
    val description: String,
    val timestamp: Long,
    val type: ActivityType
)

enum class ActivityType { SCAN, DEPLOY, AB_TEST, CONFIG }

// =============================================================
// QuickStats — 快速统计
// =============================================================
/**
 * Dashboard quick stats / 仪表盘快速统计
 */
data class QuickStats(
    val compatibilityScore: Int,
    val serializedUis: Int,
    val activeVersions: Int,
    val abTestsRunning: Int
)

// =============================================================
// RemoteTemplate — 远程模板
// =============================================================
/**
 * Remote composable template / 远程 Composable 模板
 */
data class RemoteTemplate(
    val id: String,
    val name: String,
    val description: String,
    val category: String,
    val code: String
)

// =============================================================
// UIRemoteNode — UI 树节点
// =============================================================
/**
 * UI tree node / UI 树节点
 */
data class UIRemoteNode(
    val id: String,
    val name: String,
    val type: String,
    val props: Map<String, String>,
    val children: List<UIRemoteNode>,
    val serializedSize: Int // bytes
)

// =============================================================
// TimelineEvent — 时间线事件
// =============================================================
/**
 * Timeline event / 时间线事件
 */
data class TimelineEvent(
    val id: String,
    val label: String,
    val durationMs: Long,
    val status: TimelineStatus
)

enum class TimelineStatus { PENDING, ACTIVE, COMPLETE }

// =============================================================
// RemoteFunction — 远程函数
// =============================================================
/**
 * Remote function definition / 远程函数定义
 */
data class RemoteFunction(
    val id: String,
    val name: String,
    val returnType: String,
    val parameters: List<String>,
    val code: String
)

// =============================================================
// TLSConfig — TLS 配置
// =============================================================
/**
 * TLS configuration / TLS 配置
 */
data class TLSConfig(
    val protocol: String = "TLS 1.3",
    val certificatePath: String = "",
    val privateKeyPath: String = ""
)

// =============================================================
// UIVersion — UI 版本
// =============================================================
/**
 * UI version / UI 版本
 */
data class UIVersion(
    val id: String,
    val version: String,
    val deployedAt: Long,
    val uptime: String,
    val errorRate: String,
    val trafficPercent: Int,
    val isActive: Boolean
)

// =============================================================
// PerformanceMetrics — 性能指标
// =============================================================
/**
 * Performance metrics / 性能指标
 */
data class PerformanceMetrics(
    val avgSerializeMs: Float,
    val avgDeserializeMs: Float,
    val avgByteSizeKb: Float,
    val compressionPercent: Int,
    val serializeTrend: Trend,
    val deserializeTrend: Trend,
    val sizeTrend: Trend,
    val compressionTrend: Trend
)

enum class Trend { UP, DOWN, STABLE }

/**
 * Breakdown item / 分解项目
 */
data class BreakdownItem(
    val label: String,
    val percent: Int,
    val color: Color
)

// =============================================================
// ABTest — A/B 测试
// =============================================================
/**
 * A/B test definition / A/B 测试定义
 */
data class ABTest(
    val id: String,
    val name: String,
    val status: ABTestStatus,
    val currentDay: Int,
    val totalDays: Int,
    val traffic: Int,
    val variants: List<ABVariant>
)

enum class ABTestStatus { RUNNING, COLLECTING, STOPPED }

/**
 * A/B test variant / A/B 测试变体
 */
data class ABVariant(
    val id: String,
    val name: String,
    val ctr: Float, // click-through rate
    val color: Color
)

// =============================================================
// RemoteToolkitState — 页面状态
// =============================================================
/**
 * Remote Toolkit State / 远程工具包状态
 */
data class RemoteToolkitState(
    val activePage: RemoteToolkitPage = RemoteToolkitPage.DASHBOARD,
    // Dashboard
    val quickStats: QuickStats = QuickStats(0, 0, 0, 0),
    val recentActivities: List<Activity> = emptyList(),
    // Scanner
    val selectedModule: String = "app",
    val scanStatus: ScanStatus = ScanStatus.IDLE,
    val scanScore: Int = 0,
    val scanIssues: List<CompatibilityIssue> = emptyList(),
    val expandedIssueIds: Set<String> = emptySet(),
    // SDK
    val sdkSelectedTab: Int = 0,
    val templates: List<RemoteTemplate> = emptyList(),
    val selectedTemplate: RemoteTemplate? = null,
    // Debug
    val serverStatus: ServerStatus = ServerStatus.STOPPED,
    val uiTree: UIRemoteNode? = null,
    val selectedNodeId: String? = null,
    val timelineEvents: List<TimelineEvent> = emptyList(),
    // RPC
    val registeredFunctions: List<RemoteFunction> = emptyList(),
    val isAddingFunction: Boolean = false,
    // Security
    val securityScore: Int = 0,
    val tlsConfig: TLSConfig = TLSConfig(),
    val certPinningEnabled: Boolean = false,
    val signingEnabled: Boolean = true,
    val antiTamperEnabled: Boolean = true,
    // Version
    val versions: List<UIVersion> = emptyList(),
    val activeVersionId: String = "",
    val trafficSplit: Map<String, Int> = emptyMap(),
    val isDeploying: Boolean = false,
    // Performance
    val metrics: PerformanceMetrics = PerformanceMetrics(0f, 0f, 0f, 0, Trend.STABLE, Trend.STABLE, Trend.STABLE, Trend.STABLE),
    val breakdownItems: List<BreakdownItem> = emptyList(),
    val isBenchmarking: Boolean = false,
    // AI Pipeline
    val aiPrompt: String = "",
    val generatedCode: String? = null,
    val isGenerating: Boolean = false,
    // AB Testing
    val activeTests: List<ABTest> = emptyList(),
    val completedTests: List<ABTest> = emptyList(),
    val abSelectedTab: Int = 0,
    // Loading / Error
    val isLoading: Boolean = false,
    val error: String? = null
) {
    companion object {
        val Initial = RemoteToolkitState()
    }

    val criticalIssueCount: Int get() = scanIssues.count { it.severity == IssueSeverity.CRITICAL }
    val warningIssueCount: Int get() = scanIssues.count { it.severity == IssueSeverity.WARNING }
    val passIssueCount: Int get() = scanIssues.count { it.severity == IssueSeverity.PASS }
}

// =============================================================
// RemoteToolkitIntent — 用户意图
// =============================================================
/**
 * Remote Toolkit User Intents / 远程工具包用户意图
 */
sealed interface RemoteToolkitIntent {
    data class SelectPage(val page: RemoteToolkitPage) : RemoteToolkitIntent
    data object RunCompatibilityScan : RemoteToolkitIntent
    data class ToggleIssueExpanded(val issueId: String) : RemoteToolkitIntent
    data object StartDebugServer : RemoteToolkitIntent
    data object StopDebugServer : RemoteToolkitIntent
    data class SelectTreeNode(val nodeId: String) : RemoteToolkitIntent
    data object ClearTimeline : RemoteToolkitIntent
    data object AddRemoteFunction : RemoteToolkitIntent
    data class RemoveRemoteFunction(val functionId: String) : RemoteToolkitIntent
    data class UpdateTLSConfig(val config: TLSConfig) : RemoteToolkitIntent
    data object TestConnection : RemoteToolkitIntent
    data object SaveConfiguration : RemoteToolkitIntent
    data class RollbackVersion(val versionId: String) : RemoteToolkitIntent
    data class UpdateTrafficSplit(val split: Map<String, Int>) : RemoteToolkitIntent
    data object RunBenchmark : RemoteToolkitIntent
    data object ExportReport : RemoteToolkitIntent
    data class UpdateAIPrompt(val prompt: String) : RemoteToolkitIntent
    data object GenerateUI : RemoteToolkitIntent
    data object CopyGeneratedCode : RemoteToolkitIntent
    data object DeployGeneratedUI : RemoteToolkitIntent
    data class StopABTest(val testId: String) : RemoteToolkitIntent
    data class DeclareWinner(val testId: String, val variantId: String) : RemoteToolkitIntent
    data object DismissError : RemoteToolkitIntent
    data object RefreshDashboard : RemoteToolkitIntent
}

// =============================================================
// RemoteToolkitEffect — 副作用
// =============================================================
/**
 * Remote Toolkit Side Effects / 远程工具包副作用
 */
sealed interface RemoteToolkitEffect {
    data class ShowSnackbar(val message: String, val isError: Boolean = false) : RemoteToolkitEffect
    data object ScanComplete : RemoteToolkitEffect
    data class ServerStarted(val port: Int) : RemoteToolkitEffect
    data class ServerError(val message: String) : RemoteToolkitEffect
    data object ConfigSaved : RemoteToolkitEffect
    data class ConnectionTestResult(val success: Boolean) : RemoteToolkitEffect
    data object VersionDeployed : RemoteToolkitEffect
    data object RollbackComplete : RemoteToolkitEffect
    data object TrafficSplitUpdated : RemoteToolkitEffect
    data object BenchmarkComplete : RemoteToolkitEffect
    data object ReportExported : RemoteToolkitEffect
    data class CodeCopied(val message: String) : RemoteToolkitEffect
    data object DeploymentStarted : RemoteToolkitEffect
    data object WinnerDeclared : RemoteToolkitEffect
    data class ScrollToBottom(val offset: Int) : RemoteToolkitEffect
}
