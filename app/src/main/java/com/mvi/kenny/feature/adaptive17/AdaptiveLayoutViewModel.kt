package com.mvi.kenny.feature.adaptive17

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

// =============================================================
// AdaptiveScanViewModel — 自适应布局扫描 ViewModel
// =============================================================
class AdaptiveScanViewModel : ViewModel() {

    private val _state = MutableStateFlow(AdaptiveScanState.Initial)
    val state: StateFlow<AdaptiveScanState> = _state.asStateFlow()

    private val _effect = Channel<ScanEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

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
                delay(300)
                _state.value = _state.value.copy(progress = 0.1f)
                delay(200)
                _state.value = _state.value.copy(progress = 0.2f)
                delay(300)
                _state.value = _state.value.copy(progress = 0.3f)
                delay(400)
                _state.value = _state.value.copy(progress = 0.5f)
                delay(300)
                _state.value = _state.value.copy(progress = 0.6f)
                delay(300)
                _state.value = _state.value.copy(progress = 0.7f)
                delay(400)
                _state.value = _state.value.copy(progress = 0.85f)
                delay(300)
                _state.value = _state.value.copy(progress = 0.95f)

                val violations = generateSimulatedViolations()

                _state.value = _state.value.copy(
                    isScanning = false,
                    progress = 1.0f,
                    violations = violations,
                    scanReport = ScanReport(
                        totalViolations = violations.size,
                        criticalCount = violations.count { v -> v.severity == ViolationSeverity.CRITICAL },
                        warningCount = violations.count { v -> v.severity == ViolationSeverity.WARNING },
                        lowCount = violations.count { v -> v.severity == ViolationSeverity.LOW },
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

    private fun exportHtml() {
        viewModelScope.launch {
            try {
                delay(500)
                val reportPath = "/tmp/adaptive_layout_report_${System.currentTimeMillis()}.html"
                _state.value = _state.value.copy(reportUrl = reportPath)
                _effect.send(ScanEffect.ShowToast("报告已导出: $reportPath"))
            } catch (e: Exception) {
                _effect.send(ScanEffect.ShowError("导出失败: ${e.message}"))
            }
        }
    }

    private fun applyFix(violationId: String) {
        viewModelScope.launch {
            val violation = _state.value.violations.find { it.id == violationId }
            if (violation == null) {
                _effect.send(ScanEffect.ShowError("未找到违规项: $violationId"))
                return@launch
            }

            if (violation.isAutoFixable) {
                _effect.send(ScanEffect.CopyFixCode(violation.suggestedFix))
                _effect.send(ScanEffect.ShowToast("修复代码已复制到剪贴板"))
            } else {
                _effect.send(ScanEffect.NavigateToTemplateGenerator(violation.activityName))
            }
        }
    }

    private fun dismissError() {
        _state.value = _state.value.copy(error = null)
    }

    private fun selectViolation(violation: Violation) {
        viewModelScope.launch {
            _effect.send(ScanEffect.ShowViolationDetail(violation))
        }
    }

    private fun clearSelectedViolation() {
        // no-op
    }

    private fun generateSimulatedViolations(): List<Violation> {
        return listOf(
            Violation(
                id = "V-AL-001",
                activityName = "CameraActivity",
                filePath = "app/src/main/AndroidManifest.xml",
                lineNumber = 42,
                violationType = ViolationType.SCREEN_ORIENTATION,
                severity = ViolationSeverity.CRITICAL,
                description = "android:screenOrientation=\"portrait\" 在 Android 17 大屏设备（sw>600dp）上将被系统忽略，摄像头预览将支持所有方向",
                suggestedFix = "// 移除 android:screenOrientation 或改为:\nandroid:screenOrientation=\"unspecified\"\n// 并在代码中使用 WindowSizeClass 判断布局方向",
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
                suggestedFix = "// 移除 android:resizeableActivity=\"false\"\n// 或显式设置为 true:\nandroid:resizeableActivity=\"true\"",
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
                suggestedFix = "// 方向变化时动态切换 ScaleType:\nval scaleType = when (resources.configuration.orientation) {\n    Configuration.ORIENTATION_PORTRAIT -> PreviewView.ScaleType.FILL_CENTER\n    else -> PreviewView.ScaleType.FILL_BOUNDS\n}\npreviewView.scaleType = scaleType",
                isAutoFixable = false
            ),
            Violation(
                id = "V-AL-004",
                activityName = "LiveStreamActivity",
                filePath = "app/src/main/AndroidManifest.xml",
                lineNumber = 73,
                violationType = ViolationType.MAX_ASPECT_RATIO,
                severity = ViolationSeverity.WARNING,
                description = "android:maxAspectRatio=1.86 限制了视频播放的最大宽高比，在大屏/折叠屏上体验受限",
                suggestedFix = "// 移除 maxAspectRatio 限制或设置为较大值\nandroid:maxAspectRatio=\"2.4\"",
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
                suggestedFix = "// 添加 screenSize 到 configChanges:\nandroid:configChanges=\"orientation|screenSize|screenLayout|smallestScreenSize\"",
                isAutoFixable = false
            ),
            Violation(
                id = "V-AL-006",
                activityName = "SettingsActivity",
                filePath = "app/src/main/AndroidManifest.xml",
                lineNumber = 91,
                violationType = ViolationType.MIN_ASPECT_RATIO,
                severity = ViolationSeverity.LOW,
                description = "android:minAspectRatio 限制了最小宽高比，建议移除以支持更广泛的设备",
                suggestedFix = "// 移除 minAspectRatio 限制以支持更多设备",
                isAutoFixable = true
            )
        )
    }
}

// =============================================================
// TemplateGeneratorViewModel — 模板生成器 ViewModel
// =============================================================
class TemplateGeneratorViewModel : ViewModel() {

    private val _state = MutableStateFlow(TemplateState.Initial)
    val state: StateFlow<TemplateState> = _state.asStateFlow()

    private val _effect = Channel<TemplateEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    fun handleIntent(intent: TemplateIntent) {
        when (intent) {
            is TemplateIntent.SelectScenario -> selectScenario(intent.scenario)
            is TemplateIntent.GenerateTemplate -> generateTemplate()
            is TemplateIntent.CopyToClipboard -> copyToClipboard()
            is TemplateIntent.UpdateActivityName -> updateActivityName(intent.activityName)
        }
    }

    private fun selectScenario(scenario: LayoutScenario) {
        _state.value = _state.value.copy(selectedScenario = scenario)
    }

    private fun updateActivityName(activityName: String) {
        _state.value = _state.value.copy(selectedActivity = activityName)
    }

    private fun generateTemplate() {
        if (_state.value.isGenerating) return

        viewModelScope.launch {
            _state.value = _state.value.copy(isGenerating = true, generatedCode = null)

            try {
                delay(800)
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

@Composable
fun ${activityName}Screen() {
    val configuration = LocalConfiguration.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var scaleType by remember {
        mutableStateOf(
            if (configuration.orientation == Configuration.ORIENTATION_PORTRAIT)
                PreviewView.ScaleType.FILL_CENTER
            else
                PreviewView.ScaleType.FILL_BOUNDS
        )
    }

    DisposableEffect(configuration.orientation) {
        scaleType = if (configuration.orientation == Configuration.ORIENTATION_PORTRAIT)
            PreviewView.ScaleType.FILL_CENTER
        else
            PreviewView.ScaleType.FILL_BOUNDS
        onDispose { }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isLargeScreen = configuration.screenWidthDp >= 600

        val previewModifier = if (isLargeScreen || configuration.orientation != Configuration.ORIENTATION_PORTRAIT) {
            Modifier.fillMaxSize()
        } else {
            Modifier.fillMaxWidth().aspectRatio(3f / 4f)
        }

        CameraPreview(modifier = previewModifier, scaleType = scaleType)
    }
}

@Composable
private fun CameraPreview(modifier: Modifier = Modifier, scaleType: PreviewView.ScaleType) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    AndroidView(
        factory = { ctx -> PreviewView(ctx).apply { this.scaleType = scaleType } },
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
                        lifecycleOwner, CameraSelector.DEFAULT_BACK_CAMERA, preview
                    )
                } catch (e: Exception) { }
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
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView

@Composable
fun ${activityName}Screen() {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val isLargeScreen = configuration.screenWidthDp >= 600

    BoxWithConstraints(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        if (isLandscape || isLargeScreen) {
            FullScreenVideoPlayer(modifier = Modifier.fillMaxSize())
        } else {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
                VideoPlayer(modifier = Modifier.fillMaxWidth().aspectRatio(16f / 9f))
                VideoInfoContent(modifier = Modifier.fillMaxWidth().padding(top = (configuration.screenWidthDp * 9 / 16).dp))
            }
        }
    }
}

@Composable
private fun FullScreenVideoPlayer(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val exoPlayer = remember { ExoPlayer.Builder(context).build() }
    DisposableEffect(Unit) { onDispose { exoPlayer.release() } }
    AndroidView(factory = { ctx -> PlayerView(ctx).apply { player = exoPlayer; useController = true } }, modifier = modifier)
}

@Composable
private fun VideoPlayer(modifier: Modifier = Modifier) { FullScreenVideoPlayer(modifier = modifier) }

@Composable
private fun VideoInfoContent(modifier: Modifier = Modifier) { Surface(modifier = modifier) { } }
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@Composable
fun ${activityName}Screen() {
    val configuration = LocalConfiguration.current
    val isLargeScreen = configuration.screenWidthDp >= 600
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val scrollState = rememberScrollState()
        if (isLargeScreen || isLandscape) {
            TwoColumnForm(modifier = Modifier.fillMaxSize().padding(24.dp))
        } else {
            SingleColumnForm(modifier = Modifier.fillMaxSize().padding(16.dp).verticalScroll(scrollState))
        }
    }
}

@Composable
private fun SingleColumnForm(modifier: Modifier = Modifier) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(16.dp)) {
        FormField(label = "姓名", placeholder = "请输入姓名")
        FormField(label = "邮箱", placeholder = "请输入邮箱", keyboardType = KeyboardType.Email)
        FormField(label = "电话", placeholder = "请输入电话", keyboardType = KeyboardType.Phone)
        FormField(label = "地址", placeholder = "请输入地址", singleLine = false)
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = { }, modifier = Modifier.fillMaxWidth()) { Text("提交") }
    }
}

@Composable
private fun TwoColumnForm(modifier: Modifier = Modifier) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            FormField(label = "姓名", placeholder = "请输入姓名", modifier = Modifier.weight(1f))
            FormField(label = "电话", placeholder = "请输入电话", keyboardType = KeyboardType.Phone, modifier = Modifier.weight(1f))
        }
        FormField(label = "邮箱", placeholder = "请输入邮箱", keyboardType = KeyboardType.Email)
        FormField(label = "地址", placeholder = "请输入地址", singleLine = false)
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = { }, modifier = Modifier.fillMaxWidth()) { Text("提交") }
    }
}

@Composable
private fun FormField(label: String, placeholder: String, modifier: Modifier = Modifier, keyboardType: KeyboardType = KeyboardType.Text, singleLine: Boolean = true) {
    OutlinedTextField(value = "", onValueChange = { }, label = { Text(label) }, placeholder = { Text(placeholder) }, singleLine = singleLine, keyboardOptions = KeyboardOptions(keyboardType = keyboardType), modifier = modifier.fillMaxWidth())
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ${activityName}Screen(windowSizeClass: WindowWidthSizeClass = WindowWidthSizeClass.Compact) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val modifier = Modifier.padding(16.dp)
        when (windowSizeClass) {
            WindowWidthSizeClass.Compact -> CompactLayout(modifier)
            WindowWidthSizeClass.Medium -> MediumLayout(modifier)
            WindowWidthSizeClass.Expanded -> ExpandedLayout(modifier)
        }
    }
}

@Composable private fun CompactLayout(modifier: Modifier = Modifier) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        CardItem(title = "卡片 1", description = "手机竖屏单列布局")
        CardItem(title = "卡片 2", description = "手机竖屏单列布局")
        CardItem(title = "卡片 3", description = "手机竖屏单列布局")
    }
}

@Composable private fun MediumLayout(modifier: Modifier = Modifier) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            CardItem(title = "卡片 1", description = "手机横屏双列", modifier = Modifier.weight(1f))
            CardItem(title = "卡片 2", description = "手机横屏双列", modifier = Modifier.weight(1f))
        }
        CardItem(title = "卡片 3", description = "全宽卡片")
    }
}

@Composable private fun ExpandedLayout(modifier: Modifier = Modifier) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            CardItem(title = "卡片 1", description = "大屏平板三列", modifier = Modifier.weight(1f))
            CardItem(title = "卡片 2", description = "大屏平板三列", modifier = Modifier.weight(1f))
            CardItem(title = "卡片 3", description = "大屏平板三列", modifier = Modifier.weight(1f))
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            CardItem(title = "卡片 4", description = "大屏平板双列", modifier = Modifier.weight(1f))
            CardItem(title = "卡片 5", description = "大屏平板双列", modifier = Modifier.weight(1f))
        }
    }
}

@Composable private fun CardItem(title: String, description: String, modifier: Modifier = Modifier) {
    Card(modifier = modifier, elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = title, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = description, style = MaterialTheme.typography.bodyMedium)
        }
    }
}
    """.trimIndent()
}
