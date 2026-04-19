package com.mvi.kenny.feature.localnetworkpermission

/**
 * PRD-122 | Android 17 ACCESS_LOCAL_NETWORK 运行时权限迁移检测与本地网络访问合规工具包
 * MVI Contract — Defines State, Intent, and Effect
 *
 * 设计文档: ~/WorkSpace/.openclaw/workspace/memory/agency/designs/PRD-122-Android-17-ACCESS-LOCAL-NETWORK-本地网络权限迁移检测工具包.md
 * Status: 设计完成，开发中 | Completed: 2026-04-19
 */

// ============ Data Models ============
// ============ 数据模型 ============

/**
 * Severity level for affected API calls
 * 受影响 API 调用的严重级别
 */
enum class Severity {
    P0,  // Error/Blocking — 直接崩溃或功能完全不可用
    P1,  // Warning — 需要权限但可能未申请
    P2   // Caution — 建议检查但不影响基本功能
}

/**
 * Permission state options in Settings
 * 设置中的权限状态选项
 */
enum class PermissionState(val displayName: String, val description: String) {
    UNKNOWN("Unknown / 未知", "未检测到项目中使用情况 / No usage detected"),
    DISABLED("Disabled / 已禁用", "项目未使用任何本地网络 API / No local network APIs used"),
    NEARBY_WIFI_DEVICES("NEARBY_WIFI_DEVICES", "使用 Wi-Fi P2P / Wi-Fi P2P only"),
    ACCESS_LOCAL_NETWORK("ACCESS_LOCAL_NETWORK", "全网络访问（需要申请权限）/ Full network access (requires permission)")
}

/**
 * Radar chart score for a single dimension (0-100)
 * 雷达图单个维度的分数 (0-100)
 */
data class RadarScore(
    val dimension: String,
    val score: Int  // 0-100
)

/**
 * Radar chart scores — 6 dimensions for local network permission health
 * 雷达图分数 — 本地网络权限健康的 6 个维度
 */
data class RadarScores(
    val permissionDecl: RadarScore = RadarScore("Permission Declaration / 权限声明", 0),
    val runtimeRequest: RadarScore = RadarScore("Runtime Request / 运行时请求", 0),
    val apiUsage: RadarScore = RadarScore("API Usage Pattern / API 使用模式", 0),
    val fallbackHandling: RadarScore = RadarScore("Fallback Handling / 降级处理", 0),
    val manifestConfig: RadarScore = RadarScore("Manifest Config / Manifest 配置", 0),
    val testCoverage: RadarScore = RadarScore("Test Coverage / 测试覆盖", 0)
) {
    fun toList(): List<RadarScore> = listOf(permissionDecl, runtimeRequest, apiUsage, fallbackHandling, manifestConfig, testCoverage)
}

/**
 * Single affected API call found during scan
 * 扫描过程中发现的单个受影响 API 调用
 */
data class AffectedApi(
    val apiName: String,         // e.g., "WifiManager.discoverServices()"
    val filePath: String,        // e.g., "app/src/main/java/com/example/App.kt"
    val lineNumber: Int,         // e.g., 42
    val severity: Severity,      // P0 / P1 / P2
    val suggestion: String,      // e.g., "Use NEARBY_WIFI_DEVICES instead"
    val alternativeApi: String? = null  // e.g., "WifiP2pManager.discoverServices()"
)

/**
 * Migration guide for a permission path
 * 权限路径的迁移指南
 */
data class MigrationGuide(
    val permission: PermissionState,
    val title: String,
    val description: String,
    val steps: List<String>,
    val codeSnippet: String?,
    val priority: Int  // 1 = recommended, 2, 3 = alternative
)

/**
 * Scan status
 * 扫描状态
 */
enum class ScanStatus { IDLE, SCANNING, COMPLETED, ERROR }

/**
 * Tab type for the bottom navigation
 * 底部导航的标签页类型
 */
enum class LocalNetworkTab(val title: String, val iconName: String) {
    DASHBOARD("Dashboard", "仪表盘 / 健康度总览"),
    SCANNER("Scanner", "扫描器 / 受影响 API"),
    MIGRATION("Migration", "迁移指南 / 权限路径"),
    SETTINGS("Settings", "设置 / 权限状态")
}

// ============ State ============
// ============ 状态定义 ============

/**
 * Overall UI state for Android 17 Local Network Permission Toolkit
 * Android 17 本地网络权限工具包的整体 UI 状态
 */
data class LocalNetworkPermissionState(
    // Tab / 标签页
    val currentTab: LocalNetworkTab = LocalNetworkTab.DASHBOARD,

    // Scan / 扫描状态
    val scanStatus: ScanStatus = ScanStatus.IDLE,
    val scanProgress: Float = 0f,  // 0.0 to 1.0
    val isScanning: Boolean = false,

    // Radar chart / 雷达图
    val radarScores: RadarScores = RadarScores(),

    // Scan results / 扫描结果
    val affectedApis: List<AffectedApi> = emptyList(),
    val severityFilter: Severity? = null,  // null = show all

    // Settings / 设置
    val selectedPermissionState: PermissionState = PermissionState.UNKNOWN,

    // Migration guides / 迁移指南
    val migrationGuides: List<MigrationGuide> = emptyList(),

    // Error / 错误
    val errorMessage: String? = null
) {
    /**
     * Filter affected APIs by severity
     * 按严重级别过滤受影响的 API
     */
    fun filteredApis(): List<AffectedApi> {
        return if (severityFilter == null) affectedApis
        else affectedApis.filter { it.severity == severityFilter }
    }

    /**
     * Count by severity
     * 按严重级别计数
     */
    fun countBySeverity(severity: Severity): Int = affectedApis.count { it.severity == severity }

    /**
     * Overall health score derived from radar scores
     * 从雷达图分数派生的总体健康分
     */
    val overallHealthScore: Int
        get() {
            val scores = radarScores.toList()
            return if (scores.isEmpty()) 0
            else scores.sumOf { it.score } / scores.size
        }
}

// ============ Intent ============
// ============ 用户意图 ============

sealed class LocalNetworkPermissionIntent {
    // Tab navigation / 标签页导航
    data class SelectTab(val tab: LocalNetworkTab) : LocalNetworkPermissionIntent()

    // Scan / 扫描
    object StartScan : LocalNetworkPermissionIntent()
    object CancelScan : LocalNetworkPermissionIntent()

    // Severity filter / 严重级别过滤
    data class SetSeverityFilter(val severity: Severity?) : LocalNetworkPermissionIntent()

    // Settings / 设置
    data class SetPermissionState(val state: PermissionState) : LocalNetworkPermissionIntent()
    object GenerateTestSuite : LocalNetworkPermissionIntent()

    // Error / 错误处理
    object DismissError : LocalNetworkPermissionIntent()
}

// ============ Effect ============
// ============ 副作用（一次性事件）===========

sealed class LocalNetworkPermissionEffect {
    data class ShowSnackbar(
        val message: String,
        val isError: Boolean = false
    ) : LocalNetworkPermissionEffect()

    data class TestSuiteGenerated(
        val filePath: String
    ) : LocalNetworkPermissionEffect()
}
