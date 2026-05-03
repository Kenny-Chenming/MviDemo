package com.mvi.kenny.feature.android17api37tool

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * ============================================================
 * Android17Api37ToolViewModel — Android 17 API 37 工具 ViewModel
 * ============================================================
 * PRD-220 | Android 17 (API 37) 破坏性变更综合迁移工具包
 *
 * ViewModel 职责：
 * — 接收 Intent，处理业务逻辑
 * — 更新 State（通过 mutableStateOf）
 * — 发送 Effect（通过 Channel，供 UI 层消费）
 *
 * @param state UI 状态（StateFlow，不可变对外暴露）
 * @param effect 副作用流（SharedFlow，一次性事件）
 * @param sendIntent 处理用户意图的入口方法
 *
 * @see Android17Api37ToolContract MVI 契约定义
 * @see Android17Api37ToolScreen 主界面
 */
class Android17Api37ToolViewModel : ViewModel() {

    // ================================================================
    // State — UI 状态（内部可变，外部只读）
    // ================================================================
    private val _state = MutableStateFlow(Android17Api37ToolState.Initial)
    val state: StateFlow<Android17Api37ToolState> = _state.asStateFlow()

    // ================================================================
    // Effect — 副作用（一次性事件）
    // ================================================================
    private val _effect = MutableSharedFlow<Android17Api37ToolEffect>()
    val effect: SharedFlow<Android17Api37ToolEffect> = _effect.asSharedFlow()

    // ================================================================
    // Intent 处理入口 / Intent Processing Entry Point
    // ================================================================

    /**
     * 处理用户意图 / Process user intent
     *
     * ViewModel 的核心方法，UI 层通过调用此方法发送用户意图。
     * Each Intent corresponds to a specific user action in the UI.
     *
     * @param intent User intent / 用户意图
     */
    fun sendIntent(intent: Android17Api37ToolIntent) {
        viewModelScope.launch {
            when (intent) {
                is Android17Api37ToolIntent.SetTab -> handleSetTab(intent.tab)
                is Android17Api37ToolIntent.SetScanPath -> handleSetScanPath(intent.path)
                is Android17Api37ToolIntent.StartScan -> handleStartScan()
                is Android17Api37ToolIntent.SelectScanResult -> handleSelectScanResult(intent.result)
                is Android17Api37ToolIntent.SelectScenario -> handleSelectScenario(intent.scenario)
                is Android17Api37ToolIntent.RunCIComplianceCheck -> handleRunCIComplianceCheck()
                is Android17Api37ToolIntent.ToggleBehaviorCheck -> handleToggleBehaviorCheck(intent.itemId)
                is Android17Api37ToolIntent.SetBehaviorSubTab -> handleSetBehaviorSubTab(intent.tab)
                is Android17Api37ToolIntent.ToggleDecisionNode -> handleToggleDecisionNode(intent.nodeId)
                is Android17Api37ToolIntent.DismissError -> handleDismissError()
            }
        }
    }

    // ================================================================
    // Tab Navigation / Tab 导航
    // ================================================================

    /**
     * Handle tab selection / 处理 Tab 切换
     *
     * @param tab Tab index selected by user / 用户选择的 Tab 索引
     */
    private fun handleSetTab(tab: Int) {
        _state.update { it.copy(currentTab = tab) }
    }

    // ================================================================
    // LNP Scanner / LNP 扫描器
    // ================================================================

    /**
     * Handle scan path change / 处理扫描路径变更
     *
     * @param path New scan path / 新扫描路径
     */
    private fun handleSetScanPath(path: String) {
        _state.update { it.copy(scanPath = path) }
    }

    /**
     * Handle start scan action / 处理开始扫描
     *
     * Simulates scanning source code for LAN access patterns:
     * - Socket connections to non-localhost IPs
     * - HttpURLConnection to LAN IPs
     * - InetAddress.getByName() with LAN patterns
     * - mDNS/DNSSD device discovery
     *
     * NOTE: This is a simulated scanner for demonstration.
     * Real implementation would parse actual source files.
     */
    private fun handleStartScan() {
        val currentPath = _state.value.scanPath
        if (currentPath.isBlank()) {
            viewModelScope.launch {
                _effect.emit(Android17Api37ToolEffect.ShowToast("Please enter source path / 请输入源码路径"))
            }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isScanning = true, scanProgress = 0f, scanResults = emptyList()) }

            // Simulate scanning progress / 模拟扫描进度
            withContext(Dispatchers.Default) {
                repeat(10) { step ->
                    delay(150)
                    _state.update { it.copy(scanProgress = (step + 1) / 10f) }
                }
            }

            // Generate simulated scan results based on path / 根据路径生成模拟扫描结果
            val simulatedResults = generateSimulatedScanResults(currentPath)

            _state.update {
                it.copy(
                    isScanning = false,
                    scanProgress = 1f,
                    scanResults = simulatedResults
                )
            }

            _effect.emit(Android17Api37ToolEffect.ShowToast(
                "Scan complete: ${simulatedResults.size} issues found / 扫描完成：发现 ${simulatedResults.size} 个问题"
            ))
        }
    }

    /**
     * Generate simulated scan results / 生成模拟扫描结果
     *
     * In a real implementation, this would:
     * 1. Walk the source directory
     * 2. Parse Kotlin/Java source files
     * 3. Use regex/AST to detect LAN access patterns
     * 4. Check AndroidManifest.xml for permission declarations
     *
     * @param path Scan path / 扫描路径
     * @return List of detected issues / 检测到的问题列表
     */
    private fun generateSimulatedScanResults(path: String): List<LNPScanResult> {
        // Simulate detection based on common patterns
        // 模拟基于常见模式的检测
        return listOf(
            LNPScanResult(
                id = "lnp_001",
                filePath = "$path/app/src/main/java/com/example/MyCastActivity.kt",
                riskType = "Socket Connection / Socket 连接",
                severity = RiskSeverity.CRITICAL,
                description = "Detected Socket connection to LAN IP without ACCESS_LOCAL_NETWORK permission declared in Manifest.",
                manifestSuggestion = "Add: <uses-permission android:name=\"android.permission.ACCESS_LOCAL_NETWORK\" />",
                lineNumber = 42
            ),
            LNPScanResult(
                id = "lnp_002",
                filePath = "$path/app/src/main/java/com/example/DeviceScanner.kt",
                riskType = "LAN HTTP Request / 局域网 HTTP 请求",
                severity = RiskSeverity.CRITICAL,
                description = "Detected HttpURLConnection to non-localhost IP range (192.168.x.x). Requires runtime permission check.",
                manifestSuggestion = "Add runtime permission request for ACCESS_LOCAL_NETWORK before LAN operations.",
                lineNumber = 78
            ),
            LNPScanResult(
                id = "lnp_003",
                filePath = "$path/app/src/main/java/com/example/SmartHomeClient.kt",
                riskType = "mDNS Device Discovery / mDNS 设备发现",
                severity = RiskSeverity.WARNING,
                description = "Detected mDNS/DNSSD discovery code. This always requires ACCESS_LOCAL_NETWORK permission on Android 17+.",
                manifestSuggestion = "Ensure ACCESS_LOCAL_NETWORK is declared and requested at runtime.",
                lineNumber = 115
            ),
            LNPScanResult(
                id = "lnp_004",
                filePath = "$path/app/src/main/AndroidManifest.xml",
                riskType = "Missing Permission Declaration / 缺少权限声明",
                severity = RiskSeverity.CRITICAL,
                description = "Manifest does not declare ACCESS_LOCAL_NETWORK. Required for any LAN access on Android 17+.",
                manifestSuggestion = "Add: <uses-permission android:name=\"android.permission.ACCESS_LOCAL_NETWORK\" />",
                lineNumber = null
            ),
            LNPScanResult(
                id = "lnp_005",
                filePath = "$path/app/src/main/java/com/example/MediaRenderer.kt",
                riskType = "DLNA/UPnP Operation / DLNA/UPnP 操作",
                severity = RiskSeverity.WARNING,
                description = "DLNA/UPnP operations require Local Network Permission. Verify permission is requested before discovery.",
                manifestSuggestion = "Add ACCESS_LOCAL_NETWORK + runtime permission check + fallback UI.",
                lineNumber = 203
            ),
            LNPScanResult(
                id = "lnp_006",
                filePath = "$path/app/src/main/java/com/example/FileTransfer.kt",
                riskType = "InetAddress LAN Lookup / InetAddress 局域网查询",
                severity = RiskSeverity.INFO,
                description = "Detected InetAddress.getByName() with potential LAN IP pattern. Verify this is localhost (127.0.0.1) only.",
                manifestSuggestion = "localhost/loopback does NOT need ACCESS_LOCAL_NETWORK. LAN IPs do.",
                lineNumber = 56
            )
        )
    }

    /**
     * Handle scan result selection / 处理扫描结果选择
     *
     * @param result Selected result / 选中的结果
     */
    private fun handleSelectScanResult(result: LNPScanResult?) {
        _state.update { it.copy(selectedResult = result) }
        if (result != null) {
            viewModelScope.launch {
                _effect.emit(Android17Api37ToolEffect.ShowToast("Viewing: ${result.filePath}"))
            }
        }
    }

    // ================================================================
    // Permission Template / 权限申请模板
    // ================================================================

    /**
     * Handle scenario selection / 处理场景选择
     *
     * @param scenario Selected LNP scenario / 选中的 LNP 场景
     */
    private fun handleSelectScenario(scenario: LNPScenario) {
        _state.update { it.copy(selectedScenario = scenario) }
    }

    // ================================================================
    // CI Compliance / CI 合规
    // ================================================================

    /**
     * Handle CI compliance check / 处理 CI 合规检查
     *
     * Simulates CI plugin checking:
     * 1. gradle.properties or build.gradle for plugin configuration
     * 2. AndroidManifest.xml for permission declaration
     * 3. CI pipeline blocking if non-compliant
     */
    private fun handleRunCIComplianceCheck() {
        viewModelScope.launch {
            _state.update { it.copy(ciComplianceStatus = ComplianceStatus.UNKNOWN) }
            delay(1000) // Simulate CI check time / 模拟 CI 检查时间

            // Simulate compliance result / 模拟合规结果
            val isCompliant = _state.value.scanResults.any {
                it.filePath.contains("AndroidManifest.xml") &&
                it.severity == RiskSeverity.CRITICAL &&
                it.riskType.contains("Missing Permission")
            }.not()

            val report = ComplianceReport(
                declared = if (isCompliant) 1 else 0,
                missing = if (isCompliant) 0 else 1,
                status = if (isCompliant) ComplianceStatus.COMPLIANT else ComplianceStatus.NON_COMPLIANT,
                blocking = !isCompliant
            )

            _state.update {
                it.copy(
                    ciComplianceStatus = report.status,
                    complianceReport = report
                )
            }

            _effect.emit(Android17Api37ToolEffect.ShowToast(
                if (isCompliant) "CI Compliance: PASSED ✅" else "CI Compliance: FAILED ❌"
            ))
        }
    }

    // ================================================================
    // Behavior Changes / 行为变更
    // ================================================================

    /**
     * Handle behavior sub-tab change / 处理行为变更子 Tab 切换
     *
     * @param tab Sub-tab index / 子 Tab 索引
     */
    private fun handleSetBehaviorSubTab(tab: Int) {
        _state.update { it.copy(selectedBehaviorTab = tab) }
    }

    /**
     * Handle behavior check toggle / 处理行为检查项切换
     *
     * @param itemId Check item ID / 检查项 ID
     */
    private fun handleToggleBehaviorCheck(itemId: String) {
        _state.update { state ->
            state.copy(
                behaviorChecklist = state.behaviorChecklist.map { item ->
                    if (item.id == itemId) item.copy(isChecked = !item.isChecked)
                    else item
                }
            )
        }
    }

    // ================================================================
    // Privacy & Decision / 隐私与决策
    // ================================================================

    /**
     * Handle decision tree node toggle / 处理决策树节点展开/折叠
     *
     * @param nodeId Node ID / 节点 ID
     */
    private fun handleToggleDecisionNode(nodeId: String) {
        _state.update { state ->
            state.copy(
                decisionTreeNodes = toggleNodeInTree(state.decisionTreeNodes, nodeId)
            )
        }
    }

    /**
     * Recursively toggle a node in the decision tree / 递归切换决策树中的节点
     *
     * @param nodes Current node list / 当前节点列表
     * @param targetId Target node ID / 目标节点 ID
     * @return Updated node list / 更新后的节点列表
     */
    private fun toggleNodeInTree(
        nodes: List<DecisionNode>,
        targetId: String
    ): List<DecisionNode> {
        return nodes.map { node ->
            if (node.id == targetId) {
                node.copy(isExpanded = !node.isExpanded)
            } else if (node.children.isNotEmpty()) {
                node.copy(children = toggleNodeInTree(node.children, targetId))
            } else {
                node
            }
        }
    }

    // ================================================================
    // Error Handling / 错误处理
    // ================================================================

    /**
     * Handle dismiss error / 处理关闭错误信息
     */
    private fun handleDismissError() {
        _state.update { it.copy(error = null) }
    }
}
