package com.mvi.kenny.feature.photostoragecompliance

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ============================================================
 * PhotoStorageComplianceViewModel — Photo Picker & Scoped Storage
 *                                   合规迁移工具包 ViewModel
 * ============================================================
 *
 * MVI Architecture: ViewModel handles Intent → executes business logic → updates State.
 *
 * State management:
 * - _state: MutableStateFlow holding current UI state
 * - state: Public immutable StateFlow exposed to UI layer
 *
 * Effect management:
 * - _effect: Channel for one-time side effects (Toast, Clipboard, Share)
 * - effect: Public receiveAsFlow for UI to collect
 *
 * Scan simulation: This ViewModel simulates a 3-phase scan using coroutines.
 * In production, the static analysis and runtime detection would integrate
 * with KSP/KtLint for code analysis.
 *
 * @see PhotoStorageComplianceState
 * @see PhotoStorageComplianceIntent
 * @see PhotoStorageComplianceEffect
 */
class PhotoStorageComplianceViewModel : ViewModel() {

    // ============================================================
    // State — UI 状态
    // ============================================================

    private val _state = MutableStateFlow(PhotoStorageComplianceState.Initial)
    val state: StateFlow<PhotoStorageComplianceState> = _state.asStateFlow()

    // ============================================================
    // Effect — 副作用通道
    // ============================================================

    private val _effect = Channel<PhotoStorageComplianceEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    // ============================================================
    // Intent Processing — 意图处理
    // ============================================================

    /**
     * Process user intention
     * 处理用户意图
     *
     * Entry point for all user interactions. Called from UI layer via:
     *   viewModel.sendIntent(PhotoStorageComplianceIntent.xxx)
     *
     * @param intent User intention / 用户意图
     */
    fun sendIntent(intent: PhotoStorageComplianceIntent) {
        when (intent) {
            is PhotoStorageComplianceIntent.StartScan -> handleStartScan()
            is PhotoStorageComplianceIntent.CancelScan -> handleCancelScan()
            is PhotoStorageComplianceIntent.SelectScanScope -> handleSelectScanScope(intent.scope)
            is PhotoStorageComplianceIntent.SelectTargetApiLevel -> handleSelectTargetApiLevel(intent.apiLevel)
            is PhotoStorageComplianceIntent.SelectModule -> handleSelectModule(intent.module)
            is PhotoStorageComplianceIntent.ToggleItemSelection -> handleToggleItemSelection(intent.id)
            is PhotoStorageComplianceIntent.SelectAll -> handleSelectAll()
            is PhotoStorageComplianceIntent.DeselectAll -> handleDeselectAll()
            is PhotoStorageComplianceIntent.ToggleViolationExpansion -> handleToggleViolationExpansion(intent.id)
            is PhotoStorageComplianceIntent.CopyFixCode -> handleCopyFixCode(intent.id)
            is PhotoStorageComplianceIntent.BatchCopyFixCode -> handleBatchCopyFixCode()
            is PhotoStorageComplianceIntent.ToggleHealthItemExpansion -> handleToggleHealthItemExpansion(intent.id)
            is PhotoStorageComplianceIntent.AdvanceHealthStep -> handleAdvanceHealthStep(intent.id)
            is PhotoStorageComplianceIntent.SelectExportFormat -> handleSelectExportFormat(intent.format)
            is PhotoStorageComplianceIntent.ExportReport -> handleExportReport(intent.format)
            is PhotoStorageComplianceIntent.ShareReport -> handleShareReport(intent.uri)
        }
    }

    // ============================================================
    // Scan Logic / 扫描逻辑
    // ============================================================

    /**
     * Handle start scan intent
     * 处理开始扫描意图
     *
     * Simulates a 3-phase scan:
     * Phase 1: Static Analysis (READ_MEDIA_* permissions, MANAGE_EXTERNAL_STORAGE)
     * Phase 2: Runtime Detection (Photo Picker fallback behavior)
     * Phase 3: Report Generation
     *
     * In production, this would invoke KSP/KtLint analysis and APK instrumentation.
     * —————————————————————————————————————————————————————
     * 处理开始扫描意图
     * 模拟三阶段扫描：
     * 阶段1: 静态分析（READ_MEDIA_*权限, MANAGE_EXTERNAL_STORAGE）
     * 阶段2: 运行时检测（Photo Picker fallback行为）
     * 阶段3: 报告生成
     * 实际生产中会调用 KSP/KtLint 分析和 APK 插桩测试
     */
    private fun handleStartScan() {
        viewModelScope.launch {
            try {
                // Phase 1: Static Analysis / 阶段1: 静态分析
                _state.update { it.copy(scanPhase = ScanPhase.StaticAnalysis) }
                delay(2000) // Simulate analysis time / 模拟分析耗时

                // Phase 2: Runtime Detection / 阶段2: 运行时检测
                _state.update { it.copy(scanPhase = ScanPhase.RuntimeDetection) }
                delay(1500) // Simulate runtime simulation / 模拟运行时模拟

                // Phase 3: Generate Report / 阶段3: 生成报告
                _state.update { it.copy(scanPhase = ScanPhase.Generating) }
                delay(1000) // Simulate report compilation / 模拟报告生成

                // Load simulated results based on target API level / 根据目标API级别加载模拟结果
                val (violations, healthItems) = generateSimulatedResults()

                // Complete scan / 完成扫描
                _state.update {
                    it.copy(
                        scanPhase = ScanPhase.Idle,
                        photoViolations = violations,
                        healthConnectItems = healthItems,
                        overallStatus = it.copy(
                            photoViolations = violations,
                            healthConnectItems = healthItems
                        ).computeOverallStatus()
                    )
                }

                _effect.send(PhotoStorageComplianceEffect.ScanComplete)
                _effect.send(PhotoStorageComplianceEffect.ShowToast("扫描完成 / Scan complete"))

            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        scanPhase = ScanPhase.Idle,
                        error = "扫描失败: ${e.message}"
                    )
                }
                _effect.send(PhotoStorageComplianceEffect.ShowToast("扫描失败: ${e.message}"))
            }
        }
    }

    /**
     * Generate simulated scan results for demo purposes
     * 生成模拟扫描结果（演示用）
     *
     * In production, these would come from KSP analysis + APK instrumentation.
     * 实际生产中，这些数据来自 KSP 代码分析和 APK 插桩测试
     */
    private fun generateSimulatedResults(): Pair<List<Violation>, List<HealthMigrationItem>> {
        val apiLevel = _state.value.targetApiLevel
        val violations = if (apiLevel >= 36) {
            listOf(
                Violation(
                    id = "vio_001",
                    title = "直接使用 READ_MEDIA_IMAGES 权限",
                    filePath = "app/src/main/java/com/example/myapp/GalleryActivity.kt",
                    lineNumber = 24,
                    codeSnippet = "if (checkSelfPermission(Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_DENIED)",
                    fixCode = """
// Use Photo Picker instead of direct permission request
// 使用 Photo Picker 替代直接权限申请
val photoPicker = registerForActivityResult(
    ActivityResultContracts.PickVisualMedia()
) { uri ->
    // Handle selected photo / 处理选中的照片
    uri?.let { loadImage(it) }
}
photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    """.trimIndent(),
                    severity = ViolationSeverity.Critical,
                    apiLevel = 36
                ),
                Violation(
                    id = "vio_002",
                    title = "使用 MANAGE_EXTERNAL_STORAGE 例外条款",
                    filePath = "app/src/main/java/com/example/myapp/FileManager.kt",
                    lineNumber = 56,
                    codeSnippet = "<uses-permission android:name=\"android.permission.MANAGE_EXTERNAL_STORAGE\" />",
                    fixCode = """
// Replace MANAGE_EXTERNAL_STORAGE with scoped storage APIs
// 使用 Scoped Storage API 替代 MANAGE_EXTERNAL_STORAGE
// Use MediaStore for media file access:
// 使用 MediaStore 访问媒体文件：
val projection = arrayOf(MediaStore.Images.Media._ID, MediaStore.Images.Media.DISPLAY_NAME)
val selection = "${"$"}{MediaStore.Images.Media.SIZE} > ?"
val selectionArgs = arrayOf("0")
val sortOrder = "${"$"}收了。MediaStore.Images.Media.DATE_ADDED} DESC"
                    """.trimIndent(),
                    severity = ViolationSeverity.Critical,
                    apiLevel = 36
                ),
                Violation(
                    id = "vio_003",
                    title = "直接使用 READ_MEDIA_VIDEO 权限",
                    filePath = "app/src/main/java/com/example/myapp/VideoPlayer.kt",
                    lineNumber = 18,
                    codeSnippet = "requestPermissions(arrayOf(Manifest.permission.READ_MEDIA_VIDEO), REQUEST_CODE)",
                    fixCode = """
// Use Photo Picker for video selection
// 使用 Photo Picker 选择视频
val videoPicker = registerForActivityResult(
    ActivityResultContracts.PickMultipleVisualMedia(5)
) { uris ->
    uris?.forEach { uri -> processVideo(uri) }
}
videoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly))
                    """.trimIndent(),
                    severity = ViolationSeverity.High,
                    apiLevel = 36
                )
            )
        } else {
            listOf(
                Violation(
                    id = "vio_004",
                    title = "READ_MEDIA_IMAGES (API 36+ 将触发审核警告)",
                    filePath = "app/src/main/java/com/example/myapp/GalleryActivity.kt",
                    lineNumber = 24,
                    codeSnippet = "checkSelfPermission(Manifest.permission.READ_MEDIA_IMAGES)",
                    fixCode = """
// Photo Picker is recommended for API 36+
// API 36+ 建议使用 Photo Picker
// See: https://developer.android.com/training/data-storage/shared/photo-picker
                    """.trimIndent(),
                    severity = ViolationSeverity.Medium,
                    apiLevel = 35
                )
            )
        }

        val healthItems = listOf(
            HealthMigrationItem(
                id = "hc_001",
                title = "直接传感器读取健康数据 → Health Connect",
                currentCode = """
val sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
val stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
sensorManager.registerListener(this, stepSensor, SensorManager.SENSOR_DELAY_NORMAL)
                """.trimIndent(),
                migrationSteps = listOf(
                    MigrationStep(
                        stepNumber = 1,
                        title = "添加 Health Connect 依赖",
                        description = "在 build.gradle 中添加 Health Connect SDK 依赖",
                        code = """
// build.gradle (app)
dependencies {
    implementation "androidx.health.connect:connect-client:1.1.0-alpha07"
}
                        """.trimIndent()
                    ),
                    MigrationStep(
                        stepNumber = 2,
                        title = "声明 Health Connect 权限",
                        description = "在 AndroidManifest.xml 中声明所需权限",
                        code = """
<queries>
    <package android:name="com.google.android.apps.healthdata" />
</queries>
<uses-permission android:name="android.permission.health.READ_STEPS" />
                        """.trimIndent()
                    ),
                    MigrationStep(
                        stepNumber = 3,
                        title = "使用 Health Connect API 读取数据",
                        description = "替换直接传感器调用为 Health Connect API",
                        code = """
val healthConnectClient = HealthConnectClient.getOrCreate(context)
val response = healthConnectClient.readRecords(
    ReadRecordsRequest(
        recordType = Steps::class,
        timeRangeFilter = TimeRangeFilter.beforeNow(Instant.now())
    )
)
response.records.forEach { step -> Log.d("Health", "${"$"}收了。step.count}") }
                        """.trimIndent()
                    )
                )
            )
        )

        return violations to healthItems
    }

    /**
     * Handle cancel scan intent
     * 处理取消扫描意图
     */
    private fun handleCancelScan() {
        _state.update { it.copy(scanPhase = ScanPhase.Idle, error = null) }
    }

    // ============================================================
    // Configuration Handlers / 配置处理
    // ============================================================

    /**
     * Handle scan scope selection
     * 处理扫描范围选择
     */
    private fun handleSelectScanScope(scope: ScanScope) {
        _state.update { it.copy(scanScope = scope) }
    }

    /**
     * Handle target API level selection
     * 处理目标API级别选择
     *
     * API 35 = Warning level / 警告级别
     * API 36 = Enforcement level (Play Store review flag) / 强制级别（Play Store审核标记）
     */
    private fun handleSelectTargetApiLevel(apiLevel: Int) {
        _state.update { it.copy(targetApiLevel = apiLevel) }
    }

    /**
     * Handle module selection for module-scoped scan
     * 处理模块选择（扫描范围为指定模块时）
     */
    private fun handleSelectModule(module: String) {
        _state.update { it.copy(selectedModule = module) }
    }

    // ============================================================
    // Selection & Expansion / 选中与展开
    // ============================================================

    /**
     * Toggle single violation item selection
     * 切换单个违规项的选中状态
     */
    private fun handleToggleItemSelection(id: String) {
        _state.update { currentState ->
            val newSelected = if (id in currentState.selectedItems) {
                currentState.selectedItems - id
            } else {
                currentState.selectedItems + id
            }
            currentState.copy(selectedItems = newSelected)
        }
    }

    /**
     * Select all violations
     * 全选所有违规项
     */
    private fun handleSelectAll() {
        _state.update { currentState ->
            val allIds = currentState.photoViolations.map { it.id }.toSet()
            currentState.copy(selectedItems = allIds)
        }
    }

    /**
     * Deselect all violations
     * 取消全选
     */
    private fun handleDeselectAll() {
        _state.update { it.copy(selectedItems = emptySet()) }
    }

    /**
     * Toggle violation detail expansion
     * 切换违规详情的展开/收起状态
     */
    private fun handleToggleViolationExpansion(id: String) {
        _state.update { currentState ->
            val updatedViolations = currentState.photoViolations.map { v ->
                if (v.id == id) v.copy(isExpanded = !v.isExpanded) else v
            }
            currentState.copy(photoViolations = updatedViolations)
        }
    }

    /**
     * Toggle Health Connect migration item expansion
     * 切换 Health Connect 迁移项的展开/收起状态
     */
    private fun handleToggleHealthItemExpansion(id: String) {
        _state.update { currentState ->
            val updatedItems = currentState.healthConnectItems.map { item ->
                if (item.id == id) item.copy(isExpanded = !item.isExpanded) else item
            }
            currentState.copy(healthConnectItems = updatedItems)
        }
    }

    /**
     * Advance Health Connect migration step to next step
     * 进入 Health Connect 迁移的下一个步骤
     */
    private fun handleAdvanceHealthStep(id: String) {
        _state.update { currentState ->
            val updatedItems = currentState.healthConnectItems.map { item ->
                if (item.id == id) {
                    val nextStep = minOf(item.currentStep + 1, item.migrationSteps.size - 1)
                    item.copy(currentStep = nextStep)
                } else item
            }
            currentState.copy(healthConnectItems = updatedItems)
        }
    }

    // ============================================================
    // Clipboard & Export / 剪贴板与导出
    // ============================================================

    /**
     * Copy fix code for a single violation
     * 复制单个违规项的修复代码
     */
    private fun handleCopyFixCode(id: String) {
        viewModelScope.launch {
            val violation = _state.value.photoViolations.find { it.id == id }
            if (violation != null) {
                _effect.send(PhotoStorageComplianceEffect.CopyToClipboard(violation.fixCode))
                _effect.send(PhotoStorageComplianceEffect.ShowToast("修复代码已复制 / Fix code copied"))
            }
        }
    }

    /**
     * Copy fix codes for all selected violations (batch operation)
     * 批量复制所有选中违规项的修复代码
     */
    private fun handleBatchCopyFixCode() {
        viewModelScope.launch {
            val selectedViolations = _state.value.photoViolations
                .filter { it.id in _state.value.selectedItems }

            if (selectedViolations.isEmpty()) {
                _effect.send(PhotoStorageComplianceEffect.ShowToast("请先选择要复制的违规项 / Select items first"))
                return@launch
            }

            val combinedCode = selectedViolations.joinToString("\n\n// ---\n\n") {
                "// ${it.title}\n// File: ${it.filePath}:${it.lineNumber}\n${it.fixCode}"
            }

            _effect.send(PhotoStorageComplianceEffect.CopyToClipboard(combinedCode))
            _effect.send(
                PhotoStorageComplianceEffect.ShowToast(
                    "已复制 ${selectedViolations.size} 项修复代码 / ${selectedViolations.size} fix codes copied"
                )
            )
        }
    }

    /**
     * Handle export format selection
     * 处理导出格式选择
     */
    private fun handleSelectExportFormat(format: ExportFormat) {
        _state.update { it.copy(selectedExportFormat = format) }
    }

    /**
     * Handle report export
     * 处理报告导出
     *
     * Generates a JSON report (or PDF in production with a PDF library).
     * In production, this would write to a file and return a URI.
     * —————————————————————————————————————————————————————
     * 生成 JSON 报告（生产中可扩展为 PDF）
     * 实际生产中会写入文件并返回 URI
     */
    private fun handleExportReport(format: ExportFormat) {
        viewModelScope.launch {
            try {
                _state.update { it.copy(isExporting = true) }
                delay(1500) // Simulate export / 模拟导出

                val violations = _state.value.photoViolations
                val healthItems = _state.value.healthConnectItems

                // Generate report data / 生成报告数据
                val report = Report(
                    generatedAt = System.currentTimeMillis(),
                    photoViolations = violations.size,
                    healthConnectItems = healthItems.size,
                    overallStatus = _state.value.computeOverallStatus(),
                    targetApiLevel = _state.value.targetApiLevel,
                    reportUri = null
                )

                _state.update {
                    it.copy(
                        isExporting = false,
                        exportedReport = report
                    )
                }

                _effect.send(
                    PhotoStorageComplianceEffect.ReportReady(
                        "Report exported as ${format.name} / 报告已导出为 ${format.name}"
                    )
                )
                _effect.send(
                    PhotoStorageComplianceEffect.ShowToast(
                        "报告已导出 / Report exported"
                    )
                )

            } catch (e: Exception) {
                _state.update { it.copy(isExporting = false) }
                _effect.send(PhotoStorageComplianceEffect.ShowToast("导出失败: ${e.message}"))
            }
        }
    }

    /**
     * Handle report sharing
     * 处理报告分享
     */
    private fun handleShareReport(uri: Uri) {
        viewModelScope.launch {
            _effect.send(
                PhotoStorageComplianceEffect.ShareReport(
                    uri,
                    _state.value.selectedExportFormat.mimeType
                )
            )
        }
    }
}
