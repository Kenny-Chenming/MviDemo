package com.mvi.kenny.feature.localnetworkpermission

/**
 * ============================================================
 * LocalNetworkPermissionContract — 本地网络权限 MVI 契约
 * ============================================================
 * PRD-122 | Android 17 ACCESS_LOCAL_NETWORK 运行时权限迁移检测工具包
 *
 * MVI 三要素：
 * - State: 页面状态的唯一真相来源，Immutable data class
 * - Intent: 用户意图，ViewModel 收到后执行业务逻辑
 * - Effect: 一次性副作用（导航/Toast），通过 Channel 传递
 *
 * @see LocalNetworkPermissionViewModel
 * @see LocalNetworkPermissionScreen
 */

// ================================================================
// Tab index constants / Tab 索引常量
// ================================================================
object LocalNetworkPermissionTab {
    const val DASHBOARD = 0
    const val SCANNER = 1
    const val MIGRATION = 2
    const val SETTINGS = 3
}

// ================================================================
// Severity levels / 风险级别
// ================================================================
/**
 * 风险级别枚举
 *
 * P0: 阻断级 — 核心 API 调用无权限保护，编译/运行失败
 * P1: 严重级 — 使用了受限 API，但未配置正确权限
 * P2: 注意级 — 使用了相关 API，建议检查权限覆盖
 */
enum class Severity {
    ALL, P0, P1, P2
}

/**
 * 权限状态枚举
 *
 * Android 17 ACCESS_LOCAL_NETWORK 权限的四种状态：
 * - GRANTED: 用户已授权本地网络访问
 * - DENIED: 用户拒绝授权
 * - PERMANENTLY_DENIED: 用户永久拒绝（不再提示）
 * - NOT_ASKED: 尚未向用户请求授权
 */
enum class PermissionState {
    GRANTED,
    DENIED,
    PERMANENTLY_DENIED,
    NOT_ASKED
}

// ================================================================
// Radar scores / 雷达图评分
// ================================================================
/**
 * 六维度健康度评分
 *
 * - manifestDeclaration: Manifest 中权限声明是否完整
 * - permissionRequestPath: 权限请求路径是否正确（NEARBY_WIFI_DEVICES / ACCESS_LOCAL_NETWORK）
 * - gracefulDegradation: 权限被拒后是否实现了优雅降级
 * - testCoverage: 权限状态测试覆盖是否完整
 * - dependencyLibraries: 依赖库是否兼容 Android 17
 * - apiCalls: API 调用是否合规
 *
 * @param manifestDeclaration Manifest 声明完整性评分 (0-100)
 * @param permissionRequestPath 权限请求路径评分 (0-100)
 * @param gracefulDegradation 降级策略评分 (0-100)
 * @param testCoverage 测试覆盖评分 (0-100)
 * @param dependencyLibraries 依赖库兼容性评分 (0-100)
 * @param apiCalls API 调用合规评分 (0-100)
 */
data class RadarScores(
    val manifestDeclaration: Int = 0,
    val permissionRequestPath: Int = 0,
    val gracefulDegradation: Int = 0,
    val testCoverage: Int = 0,
    val dependencyLibraries: Int = 0,
    val apiCalls: Int = 0
)

// ================================================================
// Affected API / 受影响 API
// ================================================================
/**
 * 受影响的 API 条目
 *
 * @param apiName API 名称，如 WifiManager / Socket / NetworkInterface
 * @param filePath 源文件路径
 * @param lineNumber 代码行号
 * @param severity 风险级别
 * @param codeContext 前后各 3 行代码上下文
 * @param suggestedFix 建议修复方案
 */
data class AffectedApi(
    val apiName: String,
    val filePath: String,
    val lineNumber: Int,
    val severity: Severity,
    val codeContext: String,
    val suggestedFix: String
)

// ================================================================
// Migration guide / 迁移指南
// ================================================================
/**
 * 权限请求路径类型
 *
 * NEARBY_WIFI_DEVICES: 临时访问，90 天有效期，适合配网场景
 * ACCESS_LOCAL_NETWORK: 持久访问，适合长期局域网通信
 * PRINTER_API: 打印机专用 API
 */
enum class PermissionRequestPath {
    NEARBY_WIFI_DEVICES,
    ACCESS_LOCAL_NETWORK,
    PRINTER_API
}

/**
 * 迁移指南数据
 *
 * @param path 选中的权限请求路径
 * @param beforeCode 修改前的代码
 * @param afterCode 修改后的代码
 * @param manifestDiff Manifest 变更内容
 */
data class MigrationGuide(
    val path: PermissionRequestPath,
    val beforeCode: String,
    val afterCode: String,
    val manifestDiff: String
)

// ================================================================
// State / 页面状态
// ================================================================
/**
 * 页面状态
 *
 * @param currentTab 当前 Tab 索引 (0=Dashboard, 1=Scanner, 2=Migration, 3=Settings)
 * @param scanProgress 扫描进度 (0.0 ~ 1.0)
 * @param isScanning 是否正在扫描
 * @param radarScores 六维度健康度评分
 * @param affectedApis 受影响 API 列表
 * @param severityFilter 当前风险级别过滤
 * @param selectedPermissionState 当前选中的权限状态
 * @param migrationGuide 当前迁移指南（选中的路径）
 * @param errorMessage 错误信息，null 表示无错误
 * @param expandedApiIndex 展开的 API 条目索引，-1 表示全部折叠
 * @param testSuiteCode 生成的测试用例代码
 * @param exportedReport 导出的 Markdown 报告内容
 */
data class LocalNetworkPermissionState(
    val currentTab: Int = LocalNetworkPermissionTab.DASHBOARD,
    val scanProgress: Float = 0f,
    val isScanning: Boolean = false,
    val radarScores: RadarScores = RadarScores(),
    val affectedApis: List<AffectedApi> = emptyList(),
    val severityFilter: Severity = Severity.ALL,
    val selectedPermissionState: PermissionState = PermissionState.GRANTED,
    val migrationGuide: MigrationGuide? = null,
    val errorMessage: String? = null,
    val expandedApiIndex: Int = -1,
    val testSuiteCode: String? = null,
    val exportedReport: String? = null
) {
    companion object {
        /** 初始状态 */
        val Initial = LocalNetworkPermissionState()
    }
}

// ================================================================
// Intent / 用户意图
// ================================================================
/**
 * 用户意图
 *
 * @see LocalNetworkPermissionViewModel.sendIntent
 */
sealed interface LocalNetworkPermissionIntent {
    /** 切换 Tab / Select tab */
    data class SelectTab(val index: Int) : LocalNetworkPermissionIntent

    /** 开始扫描 / Start scan */
    data object StartScan : LocalNetworkPermissionIntent

    /** 设置风险过滤 / Set severity filter */
    data class SetSeverityFilter(val severity: Severity) : LocalNetworkPermissionIntent

    /** 选择权限状态 / Select permission state */
    data class SelectPermissionState(val state: PermissionState) : LocalNetworkPermissionIntent

    /** 展开/折叠 API 条目 / Toggle API item expansion */
    data class ToggleApiExpansion(val index: Int) : LocalNetworkPermissionIntent

    /** 应用迁移方案 / Apply migration guide */
    data class ApplyMigration(val path: PermissionRequestPath) : LocalNetworkPermissionIntent

    /** 生成测试用例 / Generate test suite */
    data object GenerateTestSuite : LocalNetworkPermissionIntent

    /** 导出报告 / Export report */
    data object ExportReport : LocalNetworkPermissionIntent

    /** 复制代码 / Copy code */
    data class CopyCode(val code: String) : LocalNetworkPermissionIntent
}

// ================================================================
// Effect / 副作用
// ================================================================
/**
 * 副作用
 *
 * @see LocalNetworkPermissionViewModel
 */
sealed interface LocalNetworkPermissionEffect {
    /** 显示 Snackbar 提示 / Show snackbar */
    data class ShowSnackbar(val message: String) : LocalNetworkPermissionEffect

    /** 跳转到扫描结果 / Navigate to scanner result */
    data class NavigateToScanner(val filePath: String, val lineNumber: Int) : LocalNetworkPermissionEffect

    /** 复制到剪贴板 / Copy to clipboard */
    data class CopyToClipboard(val text: String) : LocalNetworkPermissionEffect
}
