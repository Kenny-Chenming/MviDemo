package com.mvi.kenny.feature.android17resizability

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlin.random.Random

/**
 * ============================================================
 * Android17ResizabilityViewModel — Android 17 大屏自适应迁移工具包状态管理
 * ============================================================
 * Inherits ViewModel, holds Android17ResizabilityState and Android17ResizabilityEffect.
 *
 * State Management:
 * - _state: Private MutableStateFlow, written internally by ViewModel
 * - state: Public StateFlow, for UI layer subscription (collectAsState)
 *
 * Effect Management:
 * - _effect: Channel (hot flow), buffer size BUFFERED
 * - effect: receiveAsFlow, UI layer listens via collect{}
 *
 * @see Android17ResizabilityState Page state definition
 * @see Android17ResizabilityIntent User intentions
 * @see Android17ResizabilityEffect Side effects
 * @see Android17ResizabilityScreen Main screen
 */
class Android17ResizabilityViewModel : ViewModel() {

    // =============================================================
    // State
    // =============================================================
    /** Page state (StateFlow, UI read-only) / 页面状态 */
    private val _state = MutableStateFlow(Android17ResizabilityState.Initial)
    val state: StateFlow<Android17ResizabilityState> = _state.asStateFlow()

    /** Current state snapshot for lambda access / 当前状态快照 */
    val currentState: Android17ResizabilityState get() = _state.value

    // =============================================================
    // Effect
    // =============================================================
    /** Effect Channel / 副作用通道 */
    private val _effect = Channel<Android17ResizabilityEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    // =============================================================
    // Decision Tree Data — 决策树数据
    // =============================================================
    /** Full decision tree / 完整决策树 */
    private val decisionTree: Map<String, DecisionTreeNode> = buildDecisionTree()

    /**
     * Build the resizability migration decision tree / 构建 resizability 迁移决策树
     *
     * Decision path:
     * 1. Does your app declare screenOrientation in manifest?
     *   → Yes → Is it "unspecified" or "behind"?
     *       → Yes → P2: Low priority, but recommended
     *       → No → Is it "portrait" or "landscape"?
     *           → Yes → P0: Must fix — will be ignored on API 37+ large screens
     *           → No → P1: Check if landscape-only on tablet is intentional
     *   → No → Next question
     * 2. Does your app declare resizeableActivity="false"?
     *   → Yes → P0: Will be forced into Desktop Windowing on API 37+ large screens
     *   → No → Next question
     * 3. Does your app use minAspectRatio or maxAspectRatio?
     *   → Yes → P1: These attributes are ignored on API 37+ large screens
     *   → No → Next question
     * 4. Does your Camera code assume fixed aspect ratio?
     *   → Yes → P1: Camera preview may break with forced resize
     *   → No → Compliant: No immediate action needed
     */
    private fun buildDecisionTree(): Map<String, DecisionTreeNode> {
        return mapOf(
            "root" to DecisionTreeNode(
                id = "root",
                question = "Does your app declare android:screenOrientation in AndroidManifest.xml?",
                questionZh = "你的 App 是否在 AndroidManifest.xml 中声明了 android:screenOrientation？",
                options = listOf(
                    DecisionOption("Yes", "是", "orientation_yes"),
                    DecisionOption("No", "否", "resize_check")
                )
            ),
            "orientation_yes" to DecisionTreeNode(
                id = "orientation_yes",
                question = "Is the screenOrientation value one of: 'unspecified' or 'behind'?",
                questionZh = "screenOrientation 的值是否为 'unspecified' 或 'behind'？",
                options = listOf(
                    DecisionOption("Yes (unspecified/behind)", "是 (unspecified/behind)", "result_p2_orientation"),
                    DecisionOption("No (portrait/landscape/sensor/...)", "否 (portrait/landscape/sensor/...)", "orientation_fixed")
                )
            ),
            "orientation_fixed" to DecisionTreeNode(
                id = "orientation_fixed",
                question = "Is the fixed orientation 'portrait' or 'landscape' (not 'sensor', 'fullSensor', 'fullUser')?",
                questionZh = "固定方向是否为 'portrait' 或 'landscape'（不是 'sensor'、'fullSensor'、'fullUser'）？",
                options = listOf(
                    DecisionOption("Yes (portrait/landscape)", "是 (portrait/landscape)", "result_p0_fixed_orientation"),
                    DecisionOption("No (sensor/fullSensor/fullUser)", "否 (sensor/fullSensor/fullUser)", "result_p1_limited")
                )
            ),
            "resize_check" to DecisionTreeNode(
                id = "resize_check",
                question = "Does your app declare android:resizeableActivity=\"false\"?",
                questionZh = "你的 App 是否声明了 android:resizeableActivity=\"false\"？",
                options = listOf(
                    DecisionOption("Yes (false)", "是 (false)", "result_p0_resize_false"),
                    DecisionOption("No (not declared or true)", "否（未声明或为true）", "aspectratio_check")
                )
            ),
            "aspectratio_check" to DecisionTreeNode(
                id = "aspectratio_check",
                question = "Does your app use android:minAspectRatio or android:maxAspectRatio?",
                questionZh = "你的 App 是否使用了 android:minAspectRatio 或 android:maxAspectRatio？",
                options = listOf(
                    DecisionOption("Yes", "是", "result_p1_aspectratio"),
                    DecisionOption("No", "否", "camera_check")
                )
            ),
            "camera_check" to DecisionTreeNode(
                id = "camera_check",
                question = "Does your Camera code assume a fixed preview aspect ratio?",
                questionZh = "你的 Camera 代码是否假设固定的预览 aspect ratio？",
                options = listOf(
                    DecisionOption("Yes", "是", "result_p1_camera"),
                    DecisionOption("No", "否", "result_compliant")
                )
            ),
            // Leaf nodes / 叶子节点
            "result_p0_fixed_orientation" to DecisionTreeNode(
                id = "result_p0_fixed_orientation",
                question = "🚨 P0 — Fixed Orientation Will Be Ignored",
                questionZh = "🚨 P0 — 固定方向将在 API 37+ 大屏上被忽略",
                result = "android:screenOrientation=\"portrait\" or \"landscape\" is IGNORED on API 37+ large screens (sw>600dp). All apps will be forced into Desktop Windowing or multi-orientation mode. You MUST migrate to dynamic orientation handling.",
                resultZh = "android:screenOrientation=\"portrait\" 或 \"landscape\" 在 API 37+ 大屏设备（sw>600dp）上会被系统忽略。所有 App 将被强制进入 Desktop Windowing 或多方向模式。你必须迁移到动态方向处理。",
                priority = Priority.P0,
                options = emptyList()
            ),
            "result_p2_orientation" to DecisionTreeNode(
                id = "result_p2_orientation",
                question = "✅ P2 — Low Priority (unspecified/behind)",
                questionZh = "✅ P2 — 低优先级（unspecified/behind）",
                result = "android:screenOrientation=\"unspecified\" or \"behind\" has minimal impact on API 37+ large screens. Recommended to review but not urgent.",
                resultZh = "android:screenOrientation=\"unspecified\" 或 \"behind\" 对 API 37+ 大屏影响较小。建议审查但不紧急。",
                priority = Priority.P2,
                options = emptyList()
            ),
            "result_p1_limited" to DecisionTreeNode(
                id = "result_p1_limited",
                question = "⚠️ P1 — Limited Orientation May Cause Issues",
                questionZh = "⚠️ P1 — 有限方向可能导致问题",
                result = "sensor/fullSensor/fullUser orientation may work but verify behavior on large screens and foldables. Test in Desktop Windowing simulation.",
                resultZh = "sensor/fullSensor/fullUser 方向可能可用，但需在大屏和折叠屏上验证行为。在 Desktop Windowing 模拟环境中测试。",
                priority = Priority.P1,
                options = emptyList()
            ),
            "result_p0_resize_false" to DecisionTreeNode(
                id = "result_p0_resize_false",
                question = "🚨 P0 — resizeableActivity=false Will Be Ignored",
                questionZh = "🚨 P0 — resizeableActivity=false 将被忽略",
                result = "android:resizeableActivity=\"false\" is IGNORED on API 37+ large screens. Your app will be forced into Desktop Windowing. You must set resizeableActivity=\"true\" and ensure UI handles dynamic resize.",
                resultZh = "android:resizeableActivity=\"false\" 在 API 37+ 大屏上被系统忽略。你的 App 将被强制进入 Desktop Windowing。你必须设置 resizeableActivity=\"true\" 并确保 UI 处理动态调整大小。",
                priority = Priority.P0,
                options = emptyList()
            ),
            "result_p1_aspectratio" to DecisionTreeNode(
                id = "result_p1_aspectratio",
                question = "⚠️ P1 — Aspect Ratio Constraints Will Be Ignored",
                questionZh = "⚠️ P1 — Aspect Ratio 限制将被忽略",
                result = "android:minAspectRatio and android:maxAspectRatio are IGNORED on API 37+ large screens. Your fixed aspect ratio assumption may break layouts. Use WindowSizeClass for adaptive layouts instead.",
                resultZh = "android:minAspectRatio 和 android:maxAspectRatio 在 API 37+ 大屏上被系统忽略。你固定的 aspect ratio 假设可能破坏布局。应使用 WindowSizeClass 进行自适应布局。",
                priority = Priority.P1,
                options = emptyList()
            ),
            "result_p1_camera" to DecisionTreeNode(
                id = "result_p1_camera",
                question = "⚠️ P1 — Camera Aspect Ratio May Break",
                questionZh = "⚠️ P1 — Camera Aspect Ratio 可能出问题",
                result = "Camera preview assuming fixed aspect ratio may break under forced resize. Use StreamConfigurationMap.getOutputSizes() with AspectRatioStrategy to handle dynamic resize correctly.",
                resultZh = "假设固定 aspect ratio 的 Camera 预览在强制 resize 下可能出问题。使用 StreamConfigurationMap.getOutputSizes() 配合 AspectRatioStrategy 正确处理动态调整大小。",
                priority = Priority.P1,
                options = emptyList()
            ),
            "result_compliant" to DecisionTreeNode(
                id = "result_compliant",
                question = "✅ Compliant — No Immediate Action Needed",
                questionZh = "✅ 合规 — 暂不需处理",
                result = "Your app appears to be compliant with Android 17 resizability requirements. Continue monitoring for new breaking changes in future Android versions.",
                resultZh = "你的 App 似乎符合 Android 17 resizability 要求。继续监控未来 Android 版本的新破坏性变更。",
                priority = Priority.P2,
                options = emptyList()
            )
        )
    }

    // =============================================================
    // Intent Processing
    // =============================================================
    /**
     * Receive and process user intent / 接收并处理用户意图
     *
     * Entry point, UI layer calls via viewModel.sendIntent(intent).
     *
     * @param intent User intent / 用户意图
     */
    fun sendIntent(intent: Android17ResizabilityIntent) {
        when (intent) {
            is Android17ResizabilityIntent.SelectTab -> selectTab(intent.tab)
            is Android17ResizabilityIntent.UpdateScannerInput -> updateScannerInput(intent.path)
            is Android17ResizabilityIntent.StartScan -> startScan()
            is Android17ResizabilityIntent.CancelScan -> cancelScan()
            is Android17ResizabilityIntent.ExpandMigrationGuide -> expandMigrationGuide(intent.index)
            is Android17ResizabilityIntent.NavigateDecisionTree -> navigateDecisionTree(intent.nodeId)
            is Android17ResizabilityIntent.ResetDecisionTree -> resetDecisionTree()
            is Android17ResizabilityIntent.UpdateCiConfig -> updateCiConfig(intent.config)
            is Android17ResizabilityIntent.CopyCode -> copyCode(intent.code)
            is Android17ResizabilityIntent.DismissSnackbar -> dismissSnackbar()
        }
    }

    // =============================================================
    // Tab Selection
    // =============================================================
    /**
     * Select tab / 选择 Tab
     */
    private fun selectTab(tab: ResizabilityTab) {
        _state.value = _state.value.copy(selectedTab = tab)
        // Initialize decision tree when entering decision tab / 进入决策树 Tab 时初始化决策树
        if (tab == ResizabilityTab.DECISION && _state.value.decisionTreeState == null) {
            _state.value = _state.value.copy(
                decisionTreeState = decisionTree["root"],
                decisionPath = listOf("root")
            )
        }
    }

    // =============================================================
    // Scanner
    // =============================================================
    /**
     * Update scanner input / 更新扫描器输入
     */
    private fun updateScannerInput(path: String) {
        _state.value = _state.value.copy(scannerInput = path)
    }

    /** Flag to cancel ongoing scan / 取消正在进行的扫描 */
    @Volatile
    private var isScanCancelled = false

    /**
     * Start manifest opt-out scan / 开始 manifest opt-out 扫描
     *
     * Simulates scanning AndroidManifest.xml for deprecated orientation/resize attributes.
     * Note: This is a simulation. Real implementation would use SAF to access project files
     * and XmlPullParser to parse AndroidManifest.xml.
     */
    private fun startScan() {
        isScanCancelled = false
        viewModelScope.launch {
            _state.value = _state.value.copy(
                isScanning = true,
                scanProgress = 0f,
                scanResults = emptyList(),
                cameraResults = emptyList(),
                snackbarMessage = null
            )

            try {
                // Simulate scanning with progress updates / 模拟扫描，逐步更新进度
                val totalSteps = 20
                val mockResults = generateMockScanResults()

                for (i in 0 until totalSteps) {
                    if (isScanCancelled) {
                        _state.value = _state.value.copy(
                            isScanning = false,
                            scanProgress = 0f
                        )
                        return@launch
                    }
                    delay(150)
                    val progress = (i + 1).toFloat() / totalSteps
                    _state.value = _state.value.copy(scanProgress = progress)
                }

                // Scan complete / 扫描完成
                _state.value = _state.value.copy(
                    isScanning = false,
                    scanProgress = 1f,
                    scanResults = mockResults.optOutResults,
                    cameraResults = mockResults.cameraResults
                )
                _effect.send(Android17ResizabilityEffect.ScanComplete)
                _effect.send(Android17ResizabilityEffect.ShowSnackbar("Scan complete: ${mockResults.optOutResults.size} opt-out issues, ${mockResults.cameraResults.size} camera issues"))

            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isScanning = false,
                    snackbarMessage = "Scan failed: ${e.message}"
                )
                _effect.send(Android17ResizabilityEffect.ShowSnackbar("Scan failed: ${e.message}"))
            }
        }
    }

    /**
     * Generate mock scan results for demonstration / 生成模拟扫描结果用于演示
     */
    private fun generateMockScanResults(): MockScanResults {
        val optOutResults = listOf(
            OptOutResult(
                attribute = "android:screenOrientation",
                attributeZh = "屏幕方向锁定",
                element = "<activity>",
                filePath = "app/src/main/AndroidManifest.xml",
                description = "screenOrientation is IGNORED on API 37+ large screens (sw>600dp). Apps will be forced into Desktop Windowing or multi-orientation mode.",
                descriptionZh = "screenOrientation 在 API 37+ 大屏设备（sw>600dp）上被系统忽略。App 将被强制进入 Desktop Windowing 或多方向模式。",
                fixSuggestion = """<!-- Before: Locked to portrait -->
<activity android:name=".MainActivity"
    android:screenOrientation="portrait" />

<!-- After: Dynamic orientation via ActivityInfo.screenOrientation -->
<activity android:name=".MainActivity"
    android:configChanges="orientation|screenSize" />

// In Activity.kt:
val orientation = if (isLargeScreen) {
    ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
} else {
    ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
}
requestedOrientation = orientation""",
                priority = Priority.P0
            ),
            OptOutResult(
                attribute = "android:resizeableActivity",
                attributeZh = "可调整大小 Activity",
                element = "<activity>",
                filePath = "app/src/main/AndroidManifest.xml",
                description = "resizeableActivity=false is IGNORED on API 37+ large screens. All apps will be forced into Desktop Windowing.",
                descriptionZh = "resizeableActivity=false 在 API 37+ 大屏上被系统忽略。所有 App 将被强制进入 Desktop Windowing。",
                fixSuggestion = """<!-- Before: Non-resizable -->
<activity android:name=".MainActivity"
    android:resizeableActivity="false" />

<!-- After: Resizable with proper handling -->
<activity android:name=".MainActivity"
    android:resizeableActivity="true"
    android:configChanges="screenSize|smallestScreenSize|screenLayout|orientation" />""",
                priority = Priority.P0
            ),
            OptOutResult(
                attribute = "android:minAspectRatio",
                attributeZh = "最小宽高比",
                element = "<activity>",
                filePath = "app/src/main/AndroidManifest.xml",
                description = "minAspectRatio is IGNORED on API 37+ large screens. Fixed aspect ratio assumptions may break layouts.",
                descriptionZh = "minAspectRatio 在 API 37+ 大屏上被系统忽略。固定的 aspect ratio 假设可能破坏布局。",
                fixSuggestion = """<!-- Remove minAspectRatio from manifest -->
<!-- Use WindowSizeClass in Compose instead -->
val windowSizeClass = calculateWindowSizeClass(this)
when (windowSizeClass.widthSizeClass) {
    WindowWidthSizeClass.COMPACT -> { /* Phone layout */ }
    WindowWidthSizeClass.MEDIUM -> { /* Tablet layout */ }
    WindowWidthSizeClass.EXPANDED -> { /* Desktop layout */ }
}""",
                priority = Priority.P1
            ),
            OptOutResult(
                attribute = "android:maxAspectRatio",
                attributeZh = "最大宽高比",
                element = "<activity>",
                filePath = "app/src/main/AndroidManifest.xml",
                description = "maxAspectRatio is IGNORED on API 37+ large screens.",
                descriptionZh = "maxAspectRatio 在 API 37+ 大屏上被系统忽略。",
                fixSuggestion = """<!-- Remove maxAspectRatio from manifest -->
<!-- Use adaptive layouts with WindowSizeClass -->
@Composable
fun AdaptiveLayout() {
    val windowSizeClass = rememberWindowSizeClass()
    // Respond to window size changes dynamically
}""",
                priority = Priority.P1
            )
        )

        val cameraResults = listOf(
            CameraIssueResult(
                cameraApi = "Camera2 API",
                filePath = "app/src/main/java/com/example/app/CameraHelper.kt",
                methodName = "setupCameraPreview()",
                description = "Camera2 API assuming fixed aspect ratio may break under forced resize. Use StreamConfigurationMap.getOutputSizes() with AspectRatioStrategy.",
                descriptionZh = "假设固定 aspect ratio 的 Camera2 API 在强制 resize 下可能出问题。使用 StreamConfigurationMap.getOutputSizes() 配合 AspectRatioStrategy。",
                fixSuggestion = """// Before: Fixed aspect ratio assumption
val previewSize = streamConfigMap.getOutputSizes(ImageFormat.JPEG)[0]

// After: Dynamic size with AspectRatioStrategy
val aspectRatioStrategy = AspectRatioStrategy.RATIO_16_9_FALLBACK_AUTO_STRATEGY
val outputSizes = streamConfigMap.getOutputSizes(
    SurfaceTexture::class.java,
    aspectRatioStrategy
)""",
                priority = Priority.P1
            ),
            CameraIssueResult(
                cameraApi = "CameraX",
                filePath = "app/src/main/java/com/example/app/ui/camera/CameraScreen.kt",
                methodName = "bindCameraUseCases()",
                description = "CameraX Preview with fixed aspect ratio may break under forced resize. Use Preview.SurfaceProvider with dynamic sizing.",
                descriptionZh = "使用固定 aspect ratio 的 CameraX Preview 在强制 resize 下可能出问题。使用 Preview.SurfaceProvider 配合动态调整大小。",
                fixSuggestion = """// Before: Fixed aspect ratio
val preview = Preview.Builder()
    .setTargetAspectRatio(AspectRatio.RATIO_16_9)
    .build()

// After: Adaptive sizing
val preview = Preview.Builder()
    .setTargetResolution(
        Size(INPUT.width, INPUT.height)
    )
    .build()
preview.setSurfaceProvider(binding.preview.surfaceProvider)""",
                priority = Priority.P1
            )
        )

        return MockScanResults(optOutResults, cameraResults)
    }

    private data class MockScanResults(
        val optOutResults: List<OptOutResult>,
        val cameraResults: List<CameraIssueResult>
    )

    /**
     * Cancel ongoing scan / 取消正在进行的扫描
     */
    private fun cancelScan() {
        isScanCancelled = true
        _state.value = _state.value.copy(
            isScanning = false,
            scanProgress = 0f
        )
    }

    // =============================================================
    // Migration Guide
    // =============================================================
    /**
     * Expand migration guide / 展开迁移指南
     */
    private fun expandMigrationGuide(index: Int?) {
        _state.value = _state.value.copy(
            migrationGuidesExpanded = if (_state.value.migrationGuidesExpanded == index) null else index
        )
    }

    // =============================================================
    // Decision Tree
    // =============================================================
    /**
     * Navigate decision tree / 导航决策树
     */
    private fun navigateDecisionTree(nodeId: String) {
        val node = decisionTree[nodeId] ?: return
        _state.value = _state.value.copy(
            decisionTreeState = node,
            decisionPath = _state.value.decisionPath + nodeId
        )
    }

    /**
     * Reset decision tree / 重置决策树
     */
    private fun resetDecisionTree() {
        _state.value = _state.value.copy(
            decisionTreeState = decisionTree["root"],
            decisionPath = listOf("root")
        )
    }

    // =============================================================
    // CI Config
    // =============================================================
    /**
     * Update CI config / 更新 CI 配置
     */
    private fun updateCiConfig(config: CiConfig) {
        _state.value = _state.value.copy(ciConfig = config)
    }

    // =============================================================
    // Copy Code
    // =============================================================
    /**
     * Copy code to clipboard / 复制代码到剪贴板
     */
    private fun copyCode(code: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(copiedCode = code)
            _effect.send(Android17ResizabilityEffect.CopyToClipboard(code))
            _effect.send(Android17ResizabilityEffect.ShowSnackbar("Code copied! / 代码已复制！"))
        }
    }

    // =============================================================
    // Snackbar
    // =============================================================
    /**
     * Dismiss snackbar / 关闭 Snackbar
     */
    private fun dismissSnackbar() {
        _state.value = _state.value.copy(snackbarMessage = null)
    }
}
