package com.mvi.kenny.feature.android17api37tool

import androidx.compose.ui.graphics.Color

/**
 * ============================================================
 * Android17Api37ToolContract — Android 17 API 37 综合迁移工具包 MVI 契约
 * ============================================================
 * PRD-220 | Android 17 (API 37) 破坏性变更综合迁移工具包
 *
 * MVI 架构：
 * — Model（State）：页面状态唯一真相来源，Immutable 数据类
 * — View：Composable 函数，消费 State，渲染 UI
 * — Intent：用户意图，ViewModel 收到 Intent 后执行业务逻辑
 * — Effect：一次性副作用（Toast、导航），通过 Channel 传递
 *
 * 设计文档：memory/agency/designs/PRD-220-Android-17-API-37-迁移工具包.md
 *
 * @see Android17Api37ToolViewModel 状态管理逻辑
 * @see Android17Api37ToolScreen 主界面
 */

// ================================================================
// Color Constants / 颜色常量
// ================================================================
/** Android 17 brand color / Android 17 品牌色 */
val Android17Green = Color(0xFF00D4AA)

/** Critical severity / 严重 */
val SeverityCritical = Color(0xFFFF5252)

/** Warning severity / 警告 */
val SeverityWarning = Color(0xFFFFB74D)

/** Info severity / 提示 */
val SeverityInfo = Color(0xFF4FC3F7)

// ================================================================
// Enums / 枚举
// ================================================================

/**
 * 主 Tab 枚举（五阶段流水线）
 * Main tab enumeration (5-stage pipeline)
 *
 * @param titleZh 中文标题
 * @param titleEn 英文标题
 */
enum class MainTab(val titleZh: String, val titleEn: String) {
    LNP_SCANNER("LNP检测", "LNP Scanner"),
    PERMISSION_TEMPLATE("申请模板", "Permission Template"),
    CI_COMPLIANCE("CI合规", "CI Compliance"),
    BEHAVIOR_CHANGES("行为变更", "Behavior Changes"),
    PRIVACY_DECISION("隐私决策", "Privacy & Decision")
}

/**
 * LNP 场景枚举
 * Local Network Permission scenario types
 */
enum class LNPScenario(val titleZh: String, val titleEn: String) {
    CASTING("投屏", "Casting"),
    FILE_SHARING("文件共享", "File Sharing"),
    SMART_HOME("智能家居", "Smart Home"),
    MEDIA_SERVER("媒体服务器", "Media Server")
}

/**
 * 风险严重等级
 * Risk severity level
 */
enum class RiskSeverity(val label: String, val labelZh: String, val color: Color) {
    CRITICAL("Critical", "严重", SeverityCritical),
    WARNING("Warning", "警告", SeverityWarning),
    INFO("Info", "提示", SeverityInfo)
}

/**
 * CI 合规状态
 * CI compliance status
 */
enum class ComplianceStatus(val label: String, val labelZh: String, val color: Color) {
    COMPLIANT("Compliant", "合规", Android17Green),
    NON_COMPLIANT("Non-Compliant", "不合规", SeverityCritical),
    UNKNOWN("Unknown", "未知", SeverityWarning)
}

/**
 * 行为变更子 Tab
 * Behavior changes sub-tab enumeration
 */
enum class BehaviorSubTab(val titleZh: String, val titleEn: String) {
    NATIVE_DCL("Native DCL-C", "Native DCL-C"),
    ACTIVITY_SECURITY("Activity 安全", "Activity Security"),
    MEDIA("媒体变更", "Media Changes"),
    CONNECTIVITY("连接变更", "Connectivity")
}

// ================================================================
// Data Models / 数据模型
// ================================================================

/**
 * LNP 扫描结果数据模型
 * Local Network Permission scan result
 *
 * @param id 唯一标识
 * @param filePath 文件路径
 * @param riskType 风险类型描述
 * @param severity 严重等级
 * @param description 问题描述
 * @param manifestSuggestion Manifest 配置建议
 * @param lineNumber 代码行号
 */
data class LNPScanResult(
    val id: String,
    val filePath: String,
    val riskType: String,
    val severity: RiskSeverity,
    val description: String,
    val manifestSuggestion: String,
    val lineNumber: Int? = null
)

/**
 * 代码模板数据模型
 * Code template data model
 *
 * @param scenario 适用场景
 * @param title 模板标题
 * @param code 代码内容
 * @param language 语言类型
 */
data class CodeTemplate(
    val scenario: LNPScenario,
    val title: String,
    val code: String,
    val language: String = "kotlin"
)

/**
 * CI 合规报告数据模型
 * CI compliance report data model
 *
 * @param declared 已在 Manifest 声明的数量
 * @param missing 未声明的数量
 * @param status 合规状态
 * @param blocking 是否阻塞 CI
 */
data class ComplianceReport(
    val declared: Int,
    val missing: Int,
    val status: ComplianceStatus,
    val blocking: Boolean
)

/**
 * 行为变更检查项数据模型
 * Behavior change checklist item
 *
 * @param id 唯一标识
 * @param title 变更标题
 * @param description 变更描述
 * @param isBreaking 是否为破坏性变更
 * @param priority 优先级
 * @param isChecked 是否已勾选
 */
data class BehaviorCheckItem(
    val id: String,
    val title: String,
    val titleZh: String,
    val description: String,
    val descriptionZh: String,
    val isBreaking: Boolean,
    val priority: Int,
    val isChecked: Boolean = false
)

/**
 * 决策树节点数据模型
 * Decision tree node data model
 *
 * @param id 节点唯一标识
 * @param question 问题描述
 * @param questionZh 中文问题描述
 * @param answerYes 肯定答案的描述
 * @param answerNo 否定答案的描述
 * @param action 必须立即执行的操作
 * @param actionZh 中文操作描述
 * @param children 子节点列表
 * @param isExpanded 是否展开
 */
data class DecisionNode(
    val id: String,
    val question: String,
    val questionZh: String,
    val answerYes: String,
    val answerNo: String,
    val action: String,
    val actionZh: String,
    val children: List<DecisionNode> = emptyList(),
    val isExpanded: Boolean = false
)

// ================================================================
// State / 状态
// ================================================================

/**
 * Android 17 API 37 工具页面状态
 * Android 17 API 37 Tool Page State
 *
 * Single source of truth for the entire tool UI.
 * 所有 UI 状态都从该数据类派生。
 *
 * @param currentTab 当前主 Tab 索引
 * @param scanPath 用户输入的源码路径
 * @param isScanning 是否正在扫描
 * @param scanProgress 扫描进度 0.0~1.0
 * @param scanResults 扫描结果列表
 * @param selectedResult 选中的扫描结果
 * @param selectedScenario 选择的 LNP 场景
 * @param manifestDiff Manifest diff 内容
 * @param codeTemplates 代码模板列表
 * @param ciComplianceStatus CI 合规状态
 * @param complianceReport CI 合规报告
 * @param selectedBehaviorTab 选中的行为变更子 Tab
 * @param behaviorChecklist 行为变更检查清单
 * @param decisionTreeNodes 决策树节点列表
 * @param privacyGuideText 隐私合规指南文本
 * @param error 错误信息
 */
data class Android17Api37ToolState(
    val currentTab: Int = 0,
    // LNP Scanner / LNP 扫描器
    val scanPath: String = "",
    val isScanning: Boolean = false,
    val scanProgress: Float = 0f,
    val scanResults: List<LNPScanResult> = emptyList(),
    val selectedResult: LNPScanResult? = null,
    // Permission Template / 权限申请模板
    val selectedScenario: LNPScenario = LNPScenario.CASTING,
    val manifestDiff: String = buildDefaultManifestDiff(),
    val codeTemplates: List<CodeTemplate> = buildDefaultCodeTemplates(),
    // CI Compliance / CI 合规
    val ciComplianceStatus: ComplianceStatus = ComplianceStatus.UNKNOWN,
    val complianceReport: ComplianceReport? = null,
    // Behavior Changes / 行为变更
    val selectedBehaviorTab: Int = 0,
    val behaviorChecklist: List<BehaviorCheckItem> = buildDefaultBehaviorChecklist(),
    // Privacy & Decision / 隐私与决策
    val decisionTreeNodes: List<DecisionNode> = buildDefaultDecisionTree(),
    val privacyGuideText: String = buildDefaultPrivacyGuide(),
    // Error / 错误
    val error: String? = null
) {
    companion object {
        /** Initial state / 初始状态 */
        val Initial = Android17Api37ToolState()
    }

    /** Tab count / Tab 总数 */
    val tabCount: Int get() = MainTab.entries.size
}

/**
 * Build default Manifest diff / 构建默认 Manifest diff
 * 这里展示 Android 17 LNP 的 before/after 对比
 */
private fun buildDefaultManifestDiff(): String = """
<!-- Before: Android 16 (opt-in) -->
<uses-permission android:name="android.permission.ACCESS_LOCAL_NETWORK" />

<!-- After: Android 17 (mandatory) -->
<uses-permission android:name="android.permission.ACCESS_LOCAL_NETWORK" />
<!-- Need to also declare for backwards compat -->
<uses-permission android:name="android.permission.INTERNET" />
""".trimIndent()

/**
 * Build default code templates / 构建默认代码模板
 */
private fun buildDefaultCodeTemplates(): List<CodeTemplate> = listOf(
    CodeTemplate(
        scenario = LNPScenario.CASTING,
        title = "Runtime Permission Request / 运行时权限申请",
        code = """
// Step 1: Check permission / 检查权限
val hasPermission = ContextCompat.checkSelfPermission(
    this,
    Manifest.permission.ACCESS_LOCAL_NETWORK
) == PackageManager.PERMISSION_GRANTED

// Step 2: Request permission / 申请权限
if (!hasPermission) {
    requestPermissions(
        arrayOf(Manifest.permission.ACCESS_LOCAL_NETWORK),
        REQUEST_CODE_LOCAL_NETWORK
    )
}

// Step 3: Handle result / 处理结果
override fun onRequestPermissionsResult(
    requestCode: Int,
    permissions: Array<String>,
    grantResults: IntArray
) {
    if (requestCode == REQUEST_CODE_LOCAL_NETWORK) {
        if (grantResults.isNotEmpty() && 
            grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // Permission granted, proceed with LAN operations
            discoverDevices()
        } else {
            // Show rationale or fallback UI
            showFallbackUI()
        }
    }
}
        """.trimIndent()
    ),
    CodeTemplate(
        scenario = LNPScenario.CASTING,
        title = "Fallback Strategy / 降级策略",
        code = """
/**
 * Fallback when LNP is denied
 * 权限被拒绝时的降级策略
 */
private fun showFallbackUI() {
    // Option 1: Show local-only content
    // 显示本地内容（无需 LAN）
    showLocalContentOnly()

    // Option 2: Guide user to settings
    // 引导用户去设置页开启权限
    if (shouldShowRequestPermissionRationale(
            Manifest.permission.ACCESS_LOCAL_NETWORK)) {
        showRationaleDialog()
    } else {
        openAppSettings()
    }
}
        """.trimIndent()
    ),
    CodeTemplate(
        scenario = LNPScenario.FILE_SHARING,
        title = "File Sharing Scenario / 文件共享场景",
        code = """
// File sharing: graceful degradation
// 文件共享：优雅降级
private suspend fun shareFile(file: File) {
    if (hasLocalNetworkPermission()) {
        // Full LAN discovery + transfer
        val devices = discoverLanDevices()
        transferToDevice(file, devices.first())
    } else {
        // Fallback: cloud upload or local-only
        uploadToCloud(file)
        showLocalNetworkPermissionPrompt()
    }
}
        """.trimIndent()
    ),
    CodeTemplate(
        scenario = LNPScenario.SMART_HOME,
        title = "Smart Home Scenario / 智能家居场景",
        code = """
// Smart Home: auto-discovery with fallback
// 智能家居：自动发现 + 降级
private suspend fun discoverSmartHomeDevices() {
    if (!hasLocalNetworkPermission()) {
        _uiState.update { it.copy(
            status = SmartHomeStatus.PERMISSION_DENIED,
            availableDevices = emptyList()
        )}
        return
    }

    // Full mDNS/DNSSD discovery
    val devices = withContext(Dispatchers.IO) {
        mDnsBrowser.discover("_smarthome._tcp.local")
    }
    _uiState.update { it.copy(
        status = SmartHomeStatus.DISCOVERED,
        availableDevices = devices
    )}
}
        """.trimIndent()
    ),
    CodeTemplate(
        scenario = LNPScenario.MEDIA_SERVER,
        title = "Media Server Scenario / 媒体服务器场景",
        code = """
// Media server: DLNA/UPnP with LNP
// 媒体服务器：DLNA/UPnP + LNP
class MediaServerManager {
    private val hasLNP: Boolean
        get() = context.checkLocalNetworkPermission()

    suspend fun startServer() {
        if (!hasLNP) {
            // Restrict to localhost only
            startLocalOnlyServer()
            notifyUserAboutLimitation()
            return
        }

        // Full DLNA server with LAN discovery
        startDlnaServer()
        registerWithMediaRouter()
    }
}
        """.trimIndent()
    )
)

/**
 * Build default behavior checklist / 构建默认行为变更检查清单
 */
private fun buildDefaultBehaviorChecklist(): List<BehaviorCheckItem> = listOf(
    // Native DCL-C / 原生 DCL-C
    BehaviorCheckItem(
        id = "dcl_001",
        title = "Native DCL-C Security Hardening",
        titleZh = "Native DCL-C 安全加固",
        description = "Android 17 introduces safer native DCL-C implementations. Old code may trigger warnings or fail.",
        descriptionZh = "Android 17 引入更安全的原生 DCL-C 实现，旧代码可能触发警告或失败。",
        isBreaking = true,
        priority = 1
    ),
    BehaviorCheckItem(
        id = "dcl_002",
        title = "Native Library Scanning",
        titleZh = "Native 库扫描",
        description = "Scan for old DCL patterns in .so files and native C/C++ code.",
        descriptionZh = "扫描 .so 文件和原生 C/C++ 代码中的旧 DCL 模式。",
        isBreaking = true,
        priority = 2
    ),
    // Activity Security / Activity 安全
    BehaviorCheckItem(
        id = "act_001",
        title = "Activity Launch Behavior Changes",
        titleZh = "Activity 启动行为变更",
        description = "Activity launch behavior changes in Android 17 affect deep links and intent handling.",
        descriptionZh = "Android 17 的 Activity 启动行为变更影响 Deep Link 和 Intent 处理。",
        isBreaking = true,
        priority = 1
    ),
    BehaviorCheckItem(
        id = "act_002",
        title = "Deep Link Validation",
        titleZh = "Deep Link 验证",
        description = "Verify all deep links still resolve correctly after Android 17 update.",
        descriptionZh = "验证所有 Deep Link 在 Android 17 更新后仍能正确解析。",
        isBreaking = false,
        priority = 2
    ),
    // Media / 媒体变更
    BehaviorCheckItem(
        id = "med_001",
        title = "Media API Behavior Changes",
        titleZh = "媒体 API 行为变更",
        description = "Media APIs have behavior changes in Android 17. Test all media operations.",
        descriptionZh = "Android 17 媒体 API 有行为变更，测试所有媒体操作。",
        isBreaking = true,
        priority = 2
    ),
    // Connectivity / 连接变更
    BehaviorCheckItem(
        id = "conn_001",
        title = "Network Connection Management",
        titleZh = "网络连接管理变更",
        description = "Connectivity APIs have changed. Network state queries may return different results.",
        descriptionZh = "连接 API 有变更，网络状态查询可能返回不同结果。",
        isBreaking = false,
        priority = 3
    )
)

/**
 * Build default decision tree / 构建默认决策树
 */
private fun buildDefaultDecisionTree(): List<DecisionNode> = listOf(
    DecisionNode(
        id = "root",
        question = "Does your app access the local network (LAN)?",
        questionZh = "你的 App 是否访问本地网络（LAN）？",
        answerYes = "Yes, we have LAN access",
        answerNo = "No, local network access only",
        action = "MANDATORY: Request ACCESS_LOCAL_NETWORK permission",
        actionZh = "必须申请 ACCESS_LOCAL_NETWORK 权限",
        children = listOf(
            DecisionNode(
                id = "lan_type",
                question = "What type of LAN access does your app use?",
                questionZh = "你的 App 使用哪种类型的 LAN 访问？",
                answerYes = "Device discovery",
                answerNo = "Direct IP connection",
                action = "For device discovery: Use mDNS/DNSSD, also request ACCESS_LOCAL_NETWORK",
                actionZh = "设备发现：使用 mDNS/DNSSD，同时申请 ACCESS_LOCAL_NETWORK",
                children = listOf(
                    DecisionNode(
                        id = "casting",
                        question = "Is this for casting/投屏?",
                        questionZh = "这是用于投屏吗？",
                        answerYes = "Casting app",
                        answerNo = "Other",
                        action = "Casting: Use CastContext + LNP permission + fallback UI",
                        actionZh = "投屏：使用 CastContext + LNP 权限 + 降级 UI"
                    )
                )
            ),
            DecisionNode(
                id = "fallback",
                question = "Can your app degrade gracefully if LNP is denied?",
                questionZh = "如果 LNP 被拒绝，你的 App 能优雅降级吗？",
                answerYes = "Can degrade",
                answerNo = "Cannot degrade",
                action = "If cannot degrade: Explain to users why LNP is critical",
                actionZh = "如果无法降级：向用户解释为何 LNP 是必需的"
            )
        )
    ),
    DecisionNode(
        id = "localhost",
        question = "Does your app use 127.0.0.1 or localhost only?",
        questionZh = "你的 App 是否只使用 127.0.0.1 或 localhost？",
        answerYes = "localhost only",
        answerNo = "Uses actual LAN IPs",
        action = "localhost/loopback does NOT need ACCESS_LOCAL_NETWORK",
        actionZh = "localhost/loopback 不需要 ACCESS_LOCAL_NETWORK"
    )
)

/**
 * Build default privacy guide / 构建默认隐私合规指南
 */
private fun buildDefaultPrivacyGuide(): String = """
# Android 17 Privacy Compliance Guide / Android 17 隐私合规指南

## Local Network Permission Disclosure / 本地网络权限披露

If your app uses `ACCESS_LOCAL_NETWORK`, add this to your Privacy Policy:

> **Local Network Access (Android 17+)**
> Our app may access your local network to discover and communicate with 
> compatible devices such as smart TVs, media servers, and other cast-compatible 
> devices. This requires the ACCESS_LOCAL_NETWORK permission on Android 17+ devices.
> No personal data is transmitted to external servers without your consent.

## Password Visibility / 密码可见性

Android 17 hides passwords by default on physical keyboards. If your app 
relies on password visibility toggles, update accordingly.

## Key Requirements / 关键要求

1. Declare `ACCESS_LOCAL_NETWORK` in AndroidManifest.xml
2. Request permission at runtime on Android 17+
3. Provide fallback UI when permission is denied
4. Update Privacy Policy to disclose LAN access
5. Test on Android 17 Beta/Emulator before stable release
""".trimIndent()

// ================================================================
// Intent / 意图
// ================================================================

/**
 * Android 17 API 37 工具用户意图
 * Android 17 API 37 Tool User Intents
 *
 * Every user action in the UI corresponds to an Intent.
 * ViewModel receives Intent and executes business logic.
 */
sealed interface Android17Api37ToolIntent {
    /** Set current main tab / 设置当前主 Tab
     * @param tab Tab index / Tab 索引
     */
    data class SetTab(val tab: Int) : Android17Api37ToolIntent

    /** Set scan path / 设置扫描路径
     * @param path Source code path / 源码路径
     */
    data class SetScanPath(val path: String) : Android17Api37ToolIntent

    /** Start LNP scan / 开始 LNP 扫描
     * Simulates scanning source code for LAN access patterns.
     * 模拟扫描源码中的 LAN 访问模式。
     */
    data object StartScan : Android17Api37ToolIntent

    /** Select scan result / 选择扫描结果
     * @param result Selected result / 选中的结果
     */
    data class SelectScanResult(val result: LNPScanResult?) : Android17Api37ToolIntent

    /** Select LNP scenario / 选择 LNP 场景
     * @param scenario Selected scenario / 选中的场景
     */
    data class SelectScenario(val scenario: LNPScenario) : Android17Api37ToolIntent

    /** Run CI compliance check / 运行 CI 合规检查 */
    data object RunCIComplianceCheck : Android17Api37ToolIntent

    /** Toggle behavior check item / 切换行为检查项
     * @param itemId Item ID / 检查项 ID
     */
    data class ToggleBehaviorCheck(val itemId: String) : Android17Api37ToolIntent

    /** Set behavior sub-tab / 设置行为变更子 Tab
     * @param tab Sub-tab index / 子 Tab 索引
     */
    data class SetBehaviorSubTab(val tab: Int) : Android17Api37ToolIntent

    /** Toggle decision tree node / 切换决策树节点
     * @param nodeId Node ID / 节点 ID
     */
    data class ToggleDecisionNode(val nodeId: String) : Android17Api37ToolIntent

    /** Dismiss error / 关闭错误信息 */
    data object DismissError : Android17Api37ToolIntent
}

// ================================================================
// Effect / 副作用
// ================================================================

/**
 * Android 17 API 37 工具副作用
 * Android 17 API 37 Tool Side Effects
 *
 * One-time events, immutable, consumed only once.
 * UI layer listens via LaunchedEffect + flow.collect{}.
 */
sealed interface Android17Api37ToolEffect {
    /** Show toast message / 显示 Toast 消息
     * @param message Toast text / Toast 文本
     */
    data class ShowToast(val message: String) : Android17Api37ToolEffect

    /** Copy text to clipboard / 复制文本到剪贴板
     * @param text Text to copy / 要复制的文本
     */
    data class CopyToClipboard(val text: String) : Android17Api37ToolEffect

    /** Navigate to Manifest diff view / 导航到 Manifest diff 视图 */
    data object NavigateToManifestDiff : Android17Api37ToolEffect
}
