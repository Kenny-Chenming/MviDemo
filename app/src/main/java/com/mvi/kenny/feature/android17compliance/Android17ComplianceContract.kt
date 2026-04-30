package com.mvi.kenny.feature.android17compliance

import androidx.compose.ui.graphics.Color

// ================================================================
// Android17ComplianceContract — Android 17 大屏适配 & 明文流量拦截 MVI 契约
// ================================================================
// MVI architecture contract for Android 17 compliance toolkit.
//
// PRD-206: Android 17 大屏强制适配 + 明文流量拦截 开发工具包
// Design Reference: memory/agency/designs/PRD-206-Android-17-大屏适配-明文流量拦截-工具包.md
//
// MVI 三要素 / Three pillars:
//   Model (State)  — Immutable page state, single source of truth
//   View           — Composable function, consumes State, renders UI
//   Intent         — User intentions, ViewModel processes and updates State
//   Effect         — One-time side effects (navigation, toast), via Channel
// ================================================================

// =============================================================
// Tab — 底部导航 Tab 枚举
// =============================================================
/**
 * Tab enumeration / Tab 枚举
 *
 * @param title Tab display name (bilingual)
 * @param iconName Material icon name
 */
enum class ComplianceTab(val title: String, val iconName: String) {
    SCREEN_SCAN("大屏扫描器", "Shield"),
    MIGRATION_WIZARD("适配向导", "Flow"),
    NETWORK_SCAN("明文流量检测", "NetworkCheck"),
    CONFIG_GENERATOR("Config生成器", "Settings"),
    COMPLIANCE_REPORT("合规报告", "Report")
}

// =============================================================
// ScanState — 扫描状态枚举
// =============================================================
/**
 * Scan state / 扫描状态
 *
 * @param label Chinese label
 */
enum class ScanState(val label: String) {
    IDLE("待命"),
    SCANNING("扫描中"),
    SUCCESS("完成"),
    ERROR("错误")
}

// =============================================================
// ConfigMode — Network Security Config 生成模式
// =============================================================
/**
 * Config generation mode / 配置生成模式
 *
 * @param label Display label
 */
enum class ConfigMode(val label: String) {
    DOMAIN_LEVEL("域名级别"),
    GLOBAL("全局级别")
}

// =============================================================
// RiskLevel — 风险等级
// =============================================================
/**
 * Risk level / 风险等级
 *
 * @param emoji Emoji representation
 * @param labelZh Chinese label
 * @param color Compose Color
 */
enum class RiskLevel(val emoji: String, val labelZh: String, val labelEn: String, val color: Color) {
    P0("🚫", "严重", "Critical", Color(0xFFFF1744)),
    P1("⚠️", "警告", "Warning", Color(0xFFFF6D00)),
    P2("🔶", "提示", "Info", Color(0xFFFFAB00)),
    SAFE("✅", "安全", "Safe", Color(0xFF00C853))
}

// =============================================================
// ScreenRisk — 大屏适配风险项
// =============================================================
/**
 * Screen adaptation risk item / 大屏适配风险项
 *
 * @param id Unique identifier
 * @param file File path
 * @param line Line number
 * @param riskType Risk type
 * @param riskLevel Risk level
 * @param description Risk description
 * @param snippet Code snippet
 * @param suggestion Fix suggestion
 */
data class ScreenRisk(
    val id: String,
    val file: String,
    val line: Int,
    val riskType: String,
    val riskLevel: RiskLevel,
    val description: String,
    val snippet: String,
    val suggestion: String
)

// =============================================================
// NetworkRisk — 明文流量风险项
// =============================================================
/**
 * Network traffic risk item / 明文流量风险项
 *
 * @param id Unique identifier
 * @param file File path
 * @param line Line number
 * @param url HTTP URL found
 * @param library Library used (OkHttp/Retrofit/HttpUrlConnection)
 * @param riskLevel Risk level
 * @param snippet Code snippet
 * @param suggestion Fix suggestion
 */
data class NetworkRisk(
    val id: String,
    val file: String,
    val line: Int,
    val url: String,
    val library: String,
    val riskLevel: RiskLevel,
    val snippet: String,
    val suggestion: String
)

// =============================================================
// MigrationStep — 迁移步骤
// =============================================================
/**
 * Migration step / 迁移步骤
 *
 * @param stepNumber Step number (1-indexed)
 * @param title Step title
 * @param description Step description
 * @param originalCode Original code snippet
 * @param migratedCode Migrated code snippet
 * @param isCompleted Whether step is completed
 */
data class MigrationStep(
    val stepNumber: Int,
    val title: String,
    val description: String,
    val originalCode: String,
    val migratedCode: String,
    val isCompleted: Boolean = false
)

// =============================================================
// ComplianceSummary — 合规摘要
// =============================================================
/**
 * Compliance summary / 合规摘要
 *
 * @param screenRisksCount Number of screen risks found
 * @param screenRisksFixed Number of screen risks fixed
 * @param networkRisksCount Number of network risks found
 * @param networkRisksFixed Number of network risks fixed
 * @param isCompliant Overall compliance status
 */
data class ComplianceSummary(
    val screenRisksCount: Int,
    val screenRisksFixed: Int,
    val networkRisksCount: Int,
    val networkRisksFixed: Int
) {
    val isCompliant: Boolean
        get() = screenRisksFixed == screenRisksCount && networkRisksFixed == networkRisksCount

    val screenProgress: Float
        get() = if (screenRisksCount == 0) 1f else screenRisksFixed.toFloat() / screenRisksCount

    val networkProgress: Float
        get() = if (networkRisksCount == 0) 1f else networkRisksFixed.toFloat() / networkRisksCount
}

// =============================================================
// Android17ComplianceState — 页面状态
// =============================================================
/**
 * Page state (MVI State) / 页面状态
 *
 * Single source of truth for the entire Android 17 Compliance UI.
 *
 * @param selectedTab Currently selected tab index
 * @param screenScanState Screen scan state
 * @param screenScanProgress Scan progress 0.0~1.0
 * @param screenRisks Screen risks found
 * @param networkScanState Network scan state
 * @param networkScanProgress Network scan progress
 * @param networkRisks Network risks found
 * @param migrationStep Current migration wizard step (0-indexed)
 * @param migrationSteps All migration steps
 * @param configDomains Domain list for config generation
 * @param configMode Config generation mode
 * @param generatedConfig Generated network_security_config.xml content
 * @param complianceSummary Compliance summary (null until both scans done)
 */
data class Android17ComplianceState(
    val selectedTab: Int = 0,
    val screenScanState: ScanState = ScanState.IDLE,
    val screenScanProgress: Float = 0f,
    val screenRisks: List<ScreenRisk> = emptyList(),
    val networkScanState: ScanState = ScanState.IDLE,
    val networkScanProgress: Float = 0f,
    val networkRisks: List<NetworkRisk> = emptyList(),
    val migrationStep: Int = 0,
    val migrationSteps: List<MigrationStep> = defaultMigrationSteps(),
    val configDomains: List<String> = emptyList(),
    val configMode: ConfigMode = ConfigMode.DOMAIN_LEVEL,
    val generatedConfig: String = "",
    val complianceSummary: ComplianceSummary? = null
) {
    companion object {
        /** Initial state / 初始状态 */
        val Initial = Android17ComplianceState()
    }
}

/**
 * Default migration steps / 默认迁移步骤
 */
private fun defaultMigrationSteps(): List<MigrationStep> = listOf(
    MigrationStep(
        stepNumber = 1,
        title = "移除方向锁定",
        description = "从 AndroidManifest.xml 移除 android:screenOrientation，启用完全自适应",
        originalCode = """<activity
    android:name=".MainActivity"
    android:screenOrientation="portrait"
    android:resizeableActivity="false"
/>""",
        migratedCode = """<activity
    android:name=".MainActivity"
    android:configChanges="orientation|screenSize|screenLayout|smallestScreenSize"
/>"""
    ),
    MigrationStep(
        stepNumber = 2,
        title = "迁移相机预览",
        description = "修复横竖屏切换时相机预览扭曲问题，正确处理 sensor orientation",
        originalCode = """// Lock camera to portrait
camera.bindToLifecycle(
    lifecycleOwner,
    cameraSelector,
    preview
)
preview.setSurfaceProvider(viewBinding.previewView.surfaceProvider)""",
        migratedCode = """// Handle orientation changes properly
val display = display ?: return@collect
val rotation = display.rotation
val cameraLifecycle = lifecycleOwner
camera.cameraControl.cancelFutureWhenRotationIsStable(rotation)
camera.bindToLifecycle(cameraLifecycle, cameraSelector, preview)
preview.setSurfaceProvider(viewBinding.previewView.surfaceProvider)"""
    ),
    MigrationStep(
        stepNumber = 3,
        title = "处理配置变更",
        description = "使用 ViewModel + SavedStateHandle 保存状态，正确响应 Configuration 变更",
        originalCode = """// Handle config change manually (anti-pattern)
override fun onConfigurationChanged(newConfig: Configuration) {
    super.onConfigurationChanged(newConfig)
    // Recreate is called implicitly
}""",
        migratedCode = """// Use ViewModel with SavedStateHandle (recommended)
class MyViewModel(
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val _uiState = savedStateHandle.getStateFlow("uiState", UiState())
    // Configuration changes automatically managed by ViewModel
}"""
    ),
    MigrationStep(
        stepNumber = 4,
        title = "验证多宽高比",
        description = "在不同宽高比（16:9/18:9/21:9/折叠屏）下验证 UI 布局",
        originalCode = """// Fixed aspect ratio (anti-pattern)
android:maxAspectRatio="1.86"
android:minAspectRatio="1.33" """,
        migratedCode = """<!-- Full flexibility (recommended) -->
<!-- No max/min aspect ratio restrictions -->
<!-- Test on: 5" phone / 6" phone / 7" tablet / foldable -->"""
    ),
    MigrationStep(
        stepNumber = 5,
        title = "配置明文流量",
        description = "为 HTTP 域名配置 Network Security Config，将所有流量迁移到 HTTPS",
        originalCode = """<!-- AndroidManifest.xml (deprecated in API 37) -->
<application
    android:usesCleartextTraffic="true"
    ...>""",
        migratedCode = """<!-- network_security_config.xml -->
<network-security-config>
    <domain-config cleartextTrafficPermitted="false">
        <domain includeSubdomains="true">api.example.com</domain>
        <trust-anchors>
            <certificates src="system" />
        </trust-anchors>
    </domain-config>
</network-security-config>"""
    )
)

// =============================================================
// Android17ComplianceIntent — 用户意图
// =============================================================
/**
 * User intent (MVI Intent) / 用户意图
 *
 * All user actions correspond to an Intent.
 * ViewModel receives Intent and executes business logic.
 */
sealed interface Android17ComplianceIntent {
    /** Select tab / 选择 Tab */
    data class SelectTab(val tabIndex: Int) : Android17ComplianceIntent

    /** Start screen scan / 开始大屏扫描 */
    data object StartScreenScan : Android17ComplianceIntent

    /** Start network scan / 开始明文流量扫描 */
    data object StartNetworkScan : Android17ComplianceIntent

    /** Add domain to config / 添加域名 */
    data class AddDomain(val domain: String) : Android17ComplianceIntent

    /** Remove domain from config / 删除域名 */
    data class RemoveDomain(val domain: String) : Android17ComplianceIntent

    /** Set config mode / 设置配置模式 */
    data class SetConfigMode(val mode: ConfigMode) : Android17ComplianceIntent

    /** Generate network security config / 生成配置 */
    data object GenerateConfig : Android17ComplianceIntent

    /** Copy generated config to clipboard / 复制配置 */
    data object CopyConfig : Android17ComplianceIntent

    /** Set migration step / 设置迁移步骤 */
    data class SetMigrationStep(val step: Int) : Android17ComplianceIntent

    /** Mark migration step as completed / 标记步骤完成 */
    data class CompleteMigrationStep(val step: Int) : Android17ComplianceIntent

    /** Run all scans and generate compliance report / 运行全部扫描 */
    data object RunFullComplianceCheck : Android17ComplianceIntent

    /** Export compliance report / 导出合规报告 */
    data object ExportReport : Android17ComplianceIntent

    /** Clear selected item / 清除选中项 */
    data object ClearSelection : Android17ComplianceIntent
}

// =============================================================
// Android17ComplianceEffect — 副作用
// =============================================================
/**
 * One-time side effects (MVI Effect) / 一次性副作用
 *
 * Immutable, consumed only once.
 * UI layer listens via LaunchedEffect + flow.collect{}.
 */
sealed interface Android17ComplianceEffect {
    /** Show snackbar message / 显示消息 */
    data class ShowSnackbar(val message: String) : Android17ComplianceEffect

    /** Config copied to clipboard / 配置已复制 */
    data object ConfigCopied : Android17ComplianceEffect

    /** Report exported via share intent / 报告已导出 */
    data class ReportExported(val content: String) : Android17ComplianceEffect

    /** Navigate to specific tab / 导航到指定 Tab */
    data class NavigateToTab(val tabIndex: Int) : Android17ComplianceEffect
}
