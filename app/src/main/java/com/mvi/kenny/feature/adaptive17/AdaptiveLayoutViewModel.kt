package com.mvi.kenny.feature.adaptive17

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

// =============================================================
// AdaptiveScanViewModel — 自适应布局扫描 ViewModel
// =============================================================
/**
 * Adaptive layout scan ViewModel / 自适应布局扫描 ViewModel
 *
 * Handles scan logic, violation management, and HTML report generation.
 *
 * @see AdaptiveScanState State definition
 * @see ScanIntent User intents
 * @see ScanEffect Side effects
 */
class AdaptiveScanViewModel : ViewModel() {

    private val _state = MutableStateFlow(AdaptiveScanState.Initial)
    val state: StateFlow<AdaptiveScanState> = _state.asStateFlow()

    private val _effect = Channel<ScanEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    // ============================================================
    // Intent Processing
    // ============================================================

    /**
     * Process user intent / 处理用户意图
     *
     * @param intent User intent / 用户意图
     */
    fun handleIntent(intent: ScanIntent) {
        when (intent) {
            is ScanIntent.StartScan -> startScan()
            is ScanIntent.ExportHtml -> exportHtml()
            is ScanIntent.ApplyFix -> applyFix(intent.violationId)
            is ScanIntent.DismissError -> dismissError()
            is ScanIntent.SelectViolation -> selectViolation(intent.violation)
            is ScanIntent.ClearSelectedViolation -> clearSelectedViolation()
        }
    }

    // ============================================================
    // Start Scan — 开始扫描
    // ============================================================

    /**
     * Start scanning for adaptive layout violations / 开始扫描自适应布局违规
     *
     * Simulates a Gradle plugin scan that:
     * 1. Parses AndroidManifest.xml for orientation/resizeable/maxAspectRatio
     * 2. Checks CameraX usage for aspect ratio handling
     * 3. Generates a structured violation list
     */
    private fun startScan() {
        if (_state.value.isScanning) return

        viewModelScope.launch {
            _state.value = _state.value.copy(
                isScanning = true,
                progress = 0f,
                violations = emptyList(),
                error = null,
                reportUrl = null
            )

            try {
                // Simulate scanning progress / 模拟扫描进度
                // In production, this would call the actual Gradle plugin

                // Phase 1: Manifest parsing (0~30%)
                delay(300)
                _state.value = _state.value.copy(progress = 0.1f)

                delay(200)
                _state.value = _state.value.copy(progress = 0.2f)

                delay(300)
                _state.value = _state.value.copy(progress = 0.3f)

                // Phase 2: Violation analysis (30~70%)
                delay(400)
                _state.value = _state.value.copy(progress = 0.5f)

                delay(300)
                _state.value = _state.value.copy(progress = 0.6f)

                delay(300)
                _state.value = _state.value.copy(progress = 0.7f)

                // Phase 3: CameraX check (70~90%)
                delay(400)
                _state.value = _state.value.copy(progress = 0.85f)

                // Phase 4: Generate report (90~100%)
                delay(300)
                _state.value = _state.value.copy(progress = 0.95f)

                // Generate simulated violations / 生成模拟违规数据
                val violations = generateSimulatedViolations()

                _state.value = _state.value.copy(
                    isScanning = false,
                    progress = 1.0f,
                    violations = violations,
                    scanReport = ScanReport(
                        totalViolations = violations.size,
                        criticalCount = violations.count { it.severity == ViolationSeverity.CRITICAL },
                        warningCount = violations.count { it.severity == ViolationSeverity.WARNING },
                        lowCount = violations.count { it.severity == ViolationSeverity.LOW },
                        violations = violations,
                        scannedActivities = violations.map { it.activityName }.distinct(),
                        reportFilePath = "/tmp/adaptive_layout_report.html"
                    ),
                    reportUrl = "/tmp/adaptive_layout_report.html"
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isScanning = false,
                    error = "扫描失败: ${e.message}"
                )
                _effect.send(ScanEffect.ShowError("扫描失败: ${e.message}"))
            }
        }
    }

    // ============================================================
    // Export HTML — 导出 HTML 报告
    // ============================================================

    /**
     * Export scan report as HTML / 导出 HTML 扫描报告
     */
    private fun exportHtml() {
        viewModelScope.launch {
            try {
                // Simulate HTML report generation / 模拟 HTML 报告生成
                delay(500)
                val reportPath = "/tmp/adaptive_layout_report_${System.currentTimeMillis()}.html"
                _state.value = _state.value.copy(reportUrl = reportPath)
                _effect.send(ScanEffect.ShowToast("报告已导出: $reportPath"))
            } catch (e: Exception) {
                _effect.send(ScanEffect.ShowError("导出失败: ${e.message}"))
            }
        }
    }

    // ============================================================
    // Apply Fix — 应用修复
    // ============================================================

    /**
     * Apply fix to a specific violation / 应用修复到特定违规
     *
     * @param violationId ID of the violation to fix / 要修复的违规 ID
     */
    private fun applyFix(violationId: String) {
        viewModelScope.launch {
            val violation = _state.value.violations.find { it.id == violationId }
            if (violation == null) {
                _effect.send(ScanEffect.ShowError("未找到违规项: $violationId"))
                return@launch
            }

            if (violation.isAutoFixable) {
                // Auto-fix available / 有自动修复
                _effect.send(ScanEffect.CopyFixCode(violation.suggestedFix))
                _effect.send(ScanEffect.ShowToast("修复代码已复制到剪贴板"))
            } else {
                // Navigate to template generator / 导航到模板生成器
                _effect.send(ScanEffect.NavigateToTemplateGenerator(violation.activityName))
            }
        }
    }

    // ============================================================
    // Dismiss Error — 关闭错误
    // ============================================================

    private fun dismissError() {
        _state.value = _state.value.copy(error = null)
    }

    // ============================================================
    // Select Violation — 选择违规项
    // ============================================================

    private fun selectViolation(violation: Violation) {
        viewModelScope.launch {
            _effect.send(ScanEffect.ShowViolationDetail(violation))
        }
    }

    private fun clearSelectedViolation() {
        // No-op in this simplified version / 此简化版本中无操作
    }

    // ============================================================
    // Simulated Violations — 模拟违规数据
    // ============================================================

    /**
     * Generate simulated violations for demo purposes / 生成模拟违规数据（演示用）
     *
     * In production, this data would come from the Gradle plugin scan.
     */
    private fun generateSimulatedViolations(): List<Violation> {
        return listOf(
            // CRITICAL violations
            Violation(
                id = "V-AL-001",
                activityName = "CameraActivity",
                filePath = "app/src/main/AndroidManifest.xml",
                lineNumber = 42,
                violationType = ViolationType.SCREEN_ORIENTATION,
                severity = ViolationSeverity.CRITICAL,
                description = "android:screenOrientation=\"portrait\" 在 Android 17 大屏设备（sw>600dp）上将被系统忽略，摄像头预览将支持所有方向",
                suggestedFix = """
                    // 移除 android:screenOrientation 或改为:
                    android:screenOrientation="unspecified"
                    // 并在代码中使用 WindowSizeClass 判断布局方向
                """.trimIndent(),
                isAutoFixable = true
            ),
            Violation(
                id = "V-AL-002",
                activityName = "VideoPlayerActivity",
                filePath = "app/src/main/AndroidManifest.xml",
                lineNumber = 58,
                violationType = ViolationType.RESIZEABLE_ACTIVITY,
                severity = ViolationSeverity.CRITICAL,
                description = "android:resizeableActivity=\"false\" 在 Android 17 大屏上将无效，应用必须支持自由窗口尺寸",
                suggestedFix = """
                    // 移除 android:resizeableActivity="false"
                    // 或显式设置为 true:
                    android:resizeableActivity="true"
                """.trimIndent(),
                isAutoFixable = true
            ),
            Violation(
                id = "V-AL-003",
                activityName = "CameraActivity",
                filePath = "app/src/main/java/com/example/CameraActivity.kt",
                lineNumber = 87,
                violationType = ViolationType.CAMERAX_ASPECT_RATIO,
                severity = ViolationSeverity.CRITICAL,
                description = "CameraX PreviewView 使用固定 FILL_CENTER，方向变化时会导致预览拉伸或裁切",
                suggestedFix = """
                    // 方向变化时动态切换 ScaleType:
                    val scaleType = when (resources.configuration.orientation) {
                        Configuration.ORIENTATION_PORTRAIT -> PreviewView.ScaleType.FILL_CENTER
                        else -> PreviewView.ScaleType.FILL_BOUNDS
                    }
                    previewView.scaleType = scaleType
                """.trimIndent(),
                isAutoFixable = false
            ),

            // WARNING violations
            Violation(
                id = "V-AL-004",
                activityName = "LiveStreamActivity",
                filePath = "app/src/main/AndroidManifest.xml",
                lineNumber = 73,
                violationType = ViolationType.MAX_ASPECT_RATIO,
                severity = ViolationSeverity.WARNING,
                description = "android:maxAspectRatio=1.86 限制了视频播放的最大宽高比，在大屏/折叠屏上体验受限",
                suggestedFix = """
                    // 移除 maxAspectRatio 限制或设置为较大值
                    android:maxAspectRatio="2.4"
                    // 推荐在大屏上使用自适应布局而非固定比例
                """.trimIndent(),
                isAutoFixable = true
            ),
            Violation(
                id = "V-AL-005",
                activityName = "MainActivity",
                filePath = "app/src/main/AndroidManifest.xml",
                lineNumber = 25,
                violationType = ViolationType.CONFIG_CHANGES,
                severity = ViolationSeverity.WARNING,
                description = "configChanges 包含 orientation 但不包含 screenSize，Activity 在大屏切换时仍会重建",
                suggestedFix = """
                    // 添加 screenSize 到 configChanges:
                    android:configChanges="orientation|screenSize|screenLayout|smallestScreenSize"
                    // 或完全移除 configChanges，依赖 ViewModel + SavedStateHandle 处理配置变更
                """.trimIndent(),
                isAutoFixable = false
            ),

            // LOW violations
            Violation(
                id = "V-AL-006",
                activityName = "SettingsActivity",
                filePath = "app/src/main/AndroidManifest.xml",
                lineNumber = 91,
                violationType = ViolationType.MIN_ASPECT_RATIO,
                severity = ViolationSeverity.LOW,
                description = "android:minAspectRatio 限制了最小宽高比，建议移除以支持更广泛的设备",
                suggestedFix = """
                    // 移除 minAspectRatio 限制以支持更多设备
                    // 或根据实际 UI 设计需求设置合理的最小值
                """.trimIndent(),
                isAutoFixable = true
            )
        )
    }
}

// =============================================================
// TemplateGeneratorViewModel — 模板生成器 ViewModel
// =============================================================
/**
 * Template generator ViewModel / 模板生成器 ViewModel
 *
 * Handles template code generation for different layout scenarios.
 *
 * @see TemplateState State definition
 * @see TemplateIntent User intents
 * @see TemplateEffect Side effects
 */
class TemplateGeneratorViewModel : ViewModel() {

    private val _state = MutableStateFlow(TemplateState.Initial)
    val state: StateFlow<TemplateState> = _state.asStateFlow()

    private val _effect = Channel<TemplateEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    // ============================================================
    // Intent Processing
    // ============================================================

    /**
     * Process user intent / 处理用户意图
     *
     * @param intent User intent / 用户意图
     */
    fun handleIntent(intent: TemplateIntent) {
        when (intent) {
            is TemplateIntent.SelectScenario -> selectScenario(intent.scenario)
            is TemplateIntent.GenerateTemplate -> generateTemplate()
            is TemplateIntent.CopyToClipboard -> copyToClipboard()
            is TemplateIntent.UpdateActivityName -> updateActivityName(intent.activityName)
        }
    }

    // ============================================================
    // Select Scenario — 选择场景
    // ============================================================

    private fun selectScenario(scenario: LayoutScenario) {
        _state.value = _state.value.copy(selectedScenario = scenario)
    }

    // ============================================================
    // Update Activity Name — 更新 Activity 名称
    // ============================================================

    private fun updateActivityName(activityName: String) {
        _state.value = _state.value.copy(selectedActivity = activityName)
    }

    // ============================================================
    // Generate Template — 生成模板
    // ============================================================

    /**
     * Generate template code for the selected scenario / 为所选场景生成模板代码
     */
    private fun generateTemplate() {
        if (_state.value.isGenerating) return

        viewModelScope.launch {
            _state.value = _state.value.copy(isGenerating = true, generatedCode = null)

            try {
                delay(800) // Simulate code generation / 模拟代码生成

                val activityName = _state.value.selectedActivity.ifEmpty { "MainActivity" }
                val scenario = _state.value.selectedScenario
                val code = generateTemplateCode(scenario, activityName)

                _state.value = _state.value.copy(
                    isGenerating = false,
                    generatedCode = code,
                    templateResult = TemplateResult(
                        scenario = scenario,
                        activityName = activityName,
                        generatedCode = code,
                        filePath = "app/src/main/java/com/example/ui/${activityName}Screen.kt"
                    )
                )
                _effect.send(TemplateEffect.ShowToast("模板已生成"))
            } catch (e: Exception) {
                _state.value = _state.value.copy(isGenerating = false)
                _effect.send(TemplateEffect.ShowError("生成失败: ${e.message}"))
            }
        }
    }

    // ============================================================
    // Copy to Clipboard — 复制到剪贴板
    // ============================================================

    private fun copyToClipboard() {
        viewModelScope.launch {
            val code = _state.value.generatedCode
            if (code.isNullOrEmpty()) {
                _effect.send(TemplateEffect.ShowError("没有可复制的代码"))
                return@launch
            }
            _effect.send(TemplateEffect.CopySuccess("代码已复制到剪贴板"))
        }
    }

    // ============================================================
    // Template Code Generation — 模板代码生成
    // ============================================================

    /**
     * Generate template code based on scenario / 根据场景生成模板代码
     */
    private fun generateTemplateCode(scenario: LayoutScenario, activityName: String): String {
        return when (scenario) {
            LayoutScenario.CAMERA -> generateCameraTemplate(activityName)
            LayoutScenario.VIDEO -> generateVideoTemplate(activityName)
            LayoutScenario.FORM -> generateFormTemplate(activityName)
            LayoutScenario.GENERIC -> generateGenericTemplate(activityName)
        }
    }

    private fun generateCameraTemplate(activityName: String): String = """
package com.example.ui

import android.content.res.Configuration
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 * $activityName — CameraX 自适应预览页面
 *
 * Android 17 大屏适配：
 * - 使用 WindowSizeClass 判断屏幕尺寸
 * - 动态切换 PreviewView.ScaleType 以适配不同方向
 * - 方向变化时重新计算 aspect ratio
 */
@Composable
fun ${activityName}Screen() {
    val configuration = LocalConfiguration.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // 动态 ScaleType：竖屏用 FILL_CENTER，横屏/大屏用 FILL_BOUNDS
    var scaleType by remember {
        mutableStateOf(
            if (configuration.orientation == Configuration.ORIENTATION_PORTRAIT)
                PreviewView.ScaleType.FILL_CENTER
            else
                PreviewView.ScaleType.FILL_BOUNDS
        )
    }

    // 监听方向变化，动态更新 ScaleType
    DisposableEffect(configuration.orientation) {
        scaleType = if (configuration.orientation == Configuration.ORIENTATION_PORTRAIT)
            PreviewView.ScaleType.FILL_CENTER
        else
            PreviewView.ScaleType.FILL_BOUNDS
        onDispose { }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val screenWidthDp = configuration.screenWidthDp
        val screenHeightDp = configuration.screenHeightDp

        // 判断是否为折叠屏中间态或大屏
        val isLargeScreen = screenWidthDp >= 600

        // 摄像头预览区域：竖屏保持 3:4，横屏/大屏填满
        val previewModifier = if (isLargeScreen || configuration.orientation != Configuration.ORIENTATION_PORTRAIT) {
            Modifier.fillMaxSize()
        } else {
            Modifier
                .fillMaxWidth()
                .aspectRatio(3f / 4f)
        }

        CameraPreview(
            modifier = previewModifier,
            scaleType = scaleType
        )
    }
}

@Composable
private fun CameraPreview(
    modifier: Modifier = Modifier,
    scaleType: PreviewView.ScaleType
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    AndroidView(
        factory = { ctx ->
            PreviewView(ctx).apply {
                this.scaleType = scaleType
            }
        },
        update = { previewView ->
            previewView.scaleType = scaleType

            val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()
                val preview = Preview.Builder().build()
                preview.setSurfaceProvider(previewView.surfaceProvider)

                try {
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        CameraSelector.DEFAULT_BACK_CAMERA,
                        preview
                    )
                } catch (e: Exception) {
                    // Handle camera initialization error
                }
            }, androidx.core.content.ContextCompat.getMainExecutor(context))
        },
        modifier = modifier
    )
}
    """.trimIndent()

    private fun generateVideoTemplate(activityName: String): String = """
package com.example.ui

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView

/**
 * $activityName — VideoPlayer 自适应播放页面
 *
 * Android 17 大屏适配：
 * - 16:9 视频在竖屏时显示为"字幕条"（letterbox）布局
 * - 横屏/大屏时全屏播放
 * - 支持分屏和自由窗口模式
 */
@Composable
fun ${activityName}Screen() {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val isLargeScreen = configuration.screenWidthDp >= 600

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        if (isLandscape || isLargeScreen) {
            // 横屏或大屏：全屏视频
            FullScreenVideoPlayer(modifier = Modifier.fillMaxSize())
        } else {
            // 竖屏：16:9 视频 + 底部内容
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.TopCenter
            ) {
                // 视频区域（16:9 letterbox）
                VideoPlayer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(16f / 9f)
                )
                // 下方内容区域（简介、评论等）
                VideoInfoContent(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = (configuration.screenWidthDp * 9 / 16).dp)
                )
            }
        }
    }
}

@Composable
private fun FullScreenVideoPlayer(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            // setMediaItem(MediaItem.fromUri("https://example.com/video.mp4"))
            // prepare()
        }
    }

    DisposableEffect(Unit) {
        onDispose { exoPlayer.release() }
    }

    AndroidView(
        factory = { ctx ->
            PlayerView(ctx).apply {
                player = exoPlayer
                useController = true
            }
        },
        modifier = modifier
    )
}

@Composable
private fun VideoPlayer(modifier: Modifier = Modifier) {
    FullScreenVideoPlayer(modifier = modifier)
}

@Composable
private fun VideoInfoContent(modifier: Modifier = Modifier) {
    Surface(modifier = modifier) {
        // Video info, comments, related videos...
    }
}
    """.trimIndent()

    private fun generateFormTemplate(activityName: String): String = """
package com.example.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

/**
 * $activityName — 表单输入响应式页面
 *
 * Android 17 大屏适配：
 * - 手机竖屏：单列表单
 * - 手机横屏/平板：大屏单列或双列表单
 * - 使用 WindowSizeClass 自适应布局
 */
@Composable
fun ${activityName}Screen() {
    val configuration = LocalConfiguration.current
    val screenWidthDp = configuration.screenWidthDp

    // 根据屏幕宽度判断布局模式
    val isLargeScreen = screenWidthDp >= 600
    val isLandscape = configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val scrollState = rememberScrollState()

        if (isLargeScreen || isLandscape) {
            // 大屏/横屏：双列表单
            TwoColumnForm(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
            )
        } else {
            // 手机竖屏：单列表单
            SingleColumnForm(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(scrollState)
            )
        }
    }
}

@Composable
private fun SingleColumnForm(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        FormField(label = "姓名", placeholder = "请输入姓名")
        FormField(label = "邮箱", placeholder = "请输入邮箱", keyboardType = KeyboardType.Email)
        FormField(label = "电话", placeholder = "请输入电话", keyboardType = KeyboardType.Phone)
        FormField(label = "地址", placeholder = "请输入地址", singleLine = false)

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { /* Submit */ },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("提交")
        }
    }
}

@Composable
private fun TwoColumnForm(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            FormField(
                label = "姓名",
                placeholder = "请输入姓名",
                modifier = Modifier.weight(1f)
            )
            FormField(
                label = "电话",
                placeholder = "请输入电话",
                keyboardType = KeyboardType.Phone,
                modifier = Modifier.weight(1f)
            )
        }

        FormField(
            label = "邮箱",
            placeholder = "请输入邮箱",
            keyboardType = KeyboardType.Email
        )

        FormField(
            label = "地址",
            placeholder = "请输入地址",
            singleLine = false
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { /* Submit */ },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("提交")
        }
    }
}

@Composable
private fun FormField(
    label: String,
    placeholder: String,
    modifier: Modifier = Modifier,
    keyboardType: KeyboardType = KeyboardType.Text,
    singleLine: Boolean = true
) {
    OutlinedTextField(
        value = "",
        onValueChange = { },
        label = { Text(label) },
        placeholder = { Text(placeholder) },
        singleLine = singleLine,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        modifier = modifier.fillMaxWidth()
    )
}
    """.trimIndent()

    private fun generateGenericTemplate(activityName: String): String = """
package com.example.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * $activityName — 通用响应式布局页面
 *
 * Android 17 大屏适配：
 * - 使用 WindowSizeClass 判断设备类型
 * - Compact: 手机竖屏，单列布局
 * - Medium: 手机横屏或小平板，双列布局
 * - Expanded: 大屏平板，多列布局
 */
@Composable
fun ${activityName}Screen(
    windowSizeClass: WindowWidthSizeClass = WindowWidthSizeClass.Compact
) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val modifier = Modifier.padding(16.dp)

        when (windowSizeClass) {
            WindowWidthSizeClass.Compact -> {
                // 手机竖屏：单列布局
                CompactLayout(modifier = modifier)
            }
            WindowWidthSizeClass.Medium -> {
                // 手机横屏或小平板：双列布局
                MediumLayout(modifier = modifier)
            }
            WindowWidthSizeClass.Expanded -> {
                // 大屏平板：多列布局
                ExpandedLayout(modifier = modifier)
            }
        }
    }
}

@Composable
private fun CompactLayout(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        repeat(3) { index ->
            ResponsiveCard(
                title = "卡片 ${index + 1}",
                description = "手机竖屏单列布局",
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun MediumLayout(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ResponsiveCard(
                title = "卡片 1",
                description = "手机横屏双列",
                modifier = Modifier.weight(1f)
            )
            ResponsiveCard(
                title = "卡片 2",
                description = "手机横屏双列",
                modifier = Modifier.weight(1f)
            )
        }
        ResponsiveCard(
            title = "卡片 3",
            description = "全宽卡片",
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun ExpandedLayout(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            repeat(3) { index ->
                ResponsiveCard(
                    title = "卡片 ${index + 1}",
                    description = "大屏平板三列",
                    modifier = Modifier.weight(1f)
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            repeat(2) { index ->
                ResponsiveCard(
                    title = "卡片 ${index + 4}",
                    description = "大屏平板双列",
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun ResponsiveCard(
    title: String,
    description: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
    """.trimIndent()
}
