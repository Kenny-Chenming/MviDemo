package com.mvi.kenny.feature.contactspicker

import android.os.Build
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
 * ContactsPickerViewModel — Contacts Picker 工具状态管理
 * ============================================================
 * Inherits ViewModel, holds ContactsPickerToolState and ContactsPickerEffect.
 *
 * State Management:
 * - _state: Private MutableStateFlow, written internally by ViewModel
 * - state: Public StateFlow, for UI layer subscription (collectAsState)
 *
 * Effect Management:
 * - _effect: Channel (hot flow), buffer size BUFFERED
 * - effect: receiveAsFlow, UI layer listens via collect{}
 *
 * @see ContactsPickerToolState Page state definition
 * @see ContactsPickerIntent User intentions
 * @see ContactsPickerEffect Side effects
 * @see ContactsPickerScreen Main screen UI
 */
class ContactsPickerViewModel : ViewModel() {

    // =============================================================
    // State
    // =============================================================
    /** Page state (StateFlow, UI read-only) / 页面状态 */
    private val _state = MutableStateFlow(ContactsPickerToolState.Initial)
    val state: StateFlow<ContactsPickerToolState> = _state.asStateFlow()

    /** Current state snapshot for lambda access / 当前状态快照 */
    val currentState: ContactsPickerToolState get() = _state.value

    // =============================================================
    // Effect
    // =============================================================
    /** Effect Channel / 副作用通道 */
    private val _effect = Channel<ContactsPickerEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

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
    fun sendIntent(intent: ContactsPickerIntent) {
        when (intent) {
            is ContactsPickerIntent.StartScan -> startScan()
            is ContactsPickerIntent.CancelScan -> cancelScan()
            is ContactsPickerIntent.SelectCallSite -> selectCallSite(intent.callSite)
            is ContactsPickerIntent.UpdatePickerConfig -> updatePickerConfig(intent.config)
            is ContactsPickerIntent.GenerateReport -> generateReport(intent.format)
            is ContactsPickerIntent.SelectTemplate -> selectTemplate(intent.template)
            is ContactsPickerIntent.ExportTemplate -> exportTemplate(intent.template, intent.targetPath)
            is ContactsPickerIntent.CheckCompatibility -> checkCompatibility()
            is ContactsPickerIntent.SwitchTab -> switchTab(intent.tab)
        }
    }

    // =============================================================
    // Tab Navigation
    // =============================================================
    /**
     * Switch active tab / 切换活跃 Tab
     *
     * @param tab Tab to switch to / 要切换到的 Tab
     */
    private fun switchTab(tab: ToolTab) {
        _state.value = _state.value.copy(activeTab = tab)
    }

    // =============================================================
    // Scan
    // =============================================================
    /** Flag to cancel ongoing scan / 取消正在进行的扫描 */
    @Volatile
    private var isScanCancelled = false

    /**
     * Start project scan for READ_CONTACTS usage / 开始扫描 READ_CONTACTS 使用
     *
     * Simulates scanning project for READ_CONTACTS permission call sites.
     * Uses coroutines with viewModelScope for background processing.
     */
    private fun startScan() {
        isScanCancelled = false
        viewModelScope.launch {
            _state.value = _state.value.copy(
                scanStatus = ScanStatus.SCANNING,
                scanProgress = 0f,
                scanResults = emptyList()
            )

            try {
                // Simulate scan with progress 0→1 over 2 seconds
                // 模拟扫描，进度 0→1 持续 2 秒
                val totalSteps = 20
                val allResults = mutableListOf<PermissionCallSite>()

                for (step in 0 until totalSteps) {
                    if (isScanCancelled) {
                        _state.value = _state.value.copy(
                            scanStatus = ScanStatus.IDLE,
                            scanProgress = 0f
                        )
                        return@launch
                    }

                    delay(100) // 100ms per step, total ~2 seconds

                    // Generate mock scan results
                    // 生成模拟扫描结果
                    val resultsThisStep = generateMockCallSites(step)
                    allResults.addAll(resultsThisStep)

                    val progress = (step + 1).toFloat() / totalSteps
                    _state.value = _state.value.copy(
                        scanProgress = progress,
                        scanResults = allResults.toList()
                    )
                }

                // Scan complete
                _state.value = _state.value.copy(
                    scanStatus = ScanStatus.COMPLETED,
                    scanProgress = 1f
                )
                _effect.send(ContactsPickerEffect.ScanComplete)
                _effect.send(ContactsPickerEffect.ShowSnackbar(
                    "扫描完成，发现 ${allResults.size} 处 READ_CONTACTS 调用",
                    SnackbarType.SUCCESS
                ))

            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    scanStatus = ScanStatus.ERROR,
                    scanProgress = 0f
                )
                _effect.send(ContactsPickerEffect.ShowSnackbar(
                    "扫描失败: ${e.message}",
                    SnackbarType.ERROR
                ))
            }
        }
    }

    /**
     * Generate mock call sites for simulation / 生成模拟调用点用于演示
     *
     * @param step Current scan step / 当前扫描步骤
     * @return List of generated call sites / 生成的调用点列表
     */
    private fun generateMockCallSites(step: Int): List<PermissionCallSite> {
        val templates = listOf(
            Triple("ContactsContract.Contacts.CONTENT_URI", "直接使用 Contacts URI", Migrability.P0),
            Triple("ContentResolver.query(ContactsContract.RawContacts.CONTENT_URI)", "RawContacts 查询", Migrability.P1),
            Triple("Cursor.getString(columnIndex)", "游标读取联系人字段", Migrability.P2),
            Triple("ActivityCompat.requestPermissions", "动态权限请求", Migrability.P0),
            Triple("Manifest.permission.READ_CONTACTS", "Manifest 声明检查", Migrability.P1)
        )

        val files = listOf(
            "app/src/main/java/com/example/app/ContactListActivity.kt",
            "app/src/main/java/com/example/app/ContactDetailFragment.kt",
            "app/src/main/java/com/example/app/ContactSyncService.kt",
            "app/src/main/java/com/example/app/ContactImportHelper.kt"
        )

        return if (step % 3 == 0) {
            val template = templates[step % templates.size]
            val file = files[step % files.size]
            listOf(
                PermissionCallSite(
                    id = "call_${step}_0",
                    filePath = file,
                    lineNumber = 42 + step * 13,
                    callChain = "ViewModel → Repository → ContentResolver.query()",
                    migrability = template.third,
                    codeSnippet = """
                        |// ${template.second}
                        |if (ContextCompat.checkSelfPermission(
                        |    context,
                        |    Manifest.permission.READ_CONTACTS
                        |) == PackageManager.PERMISSION_GRANTED) {
                        |    val cursor = contentResolver.query(
                        |        ${template.first},
                        |        null, null, null, null
                        |    )
                        |}
                    """.trimMargin()
                )
            )
        } else {
            emptyList()
        }
    }

    /**
     * Cancel ongoing scan / 取消正在进行的扫描
     */
    private fun cancelScan() {
        isScanCancelled = true
        _state.value = _state.value.copy(
            scanStatus = ScanStatus.IDLE,
            scanProgress = 0f
        )
        viewModelScope.launch {
            _effect.send(ContactsPickerEffect.ShowSnackbar("扫描已取消", SnackbarType.INFO))
        }
    }

    // =============================================================
    // Call Site Selection
    // =============================================================
    /**
     * Select a call site to view detail / 选择调用点查看详情
     *
     * @param callSite Call site to select / 要选择的调用点
     */
    private fun selectCallSite(callSite: PermissionCallSite) {
        _state.value = _state.value.copy(selectedCallSite = callSite)
    }

    // =============================================================
    // Picker Config
    // =============================================================
    /**
     * Update Contacts Picker configuration / 更新 Contacts Picker 配置
     *
     * @param config New picker config / 新的 picker 配置
     */
    private fun updatePickerConfig(config: PickerConfig) {
        _state.value = _state.value.copy(pickerConfig = config)
        viewModelScope.launch {
            _effect.send(ContactsPickerEffect.ShowSnackbar("配置已更新", SnackbarType.SUCCESS))
        }
    }

    // =============================================================
    // Report Generation
    // =============================================================
    /**
     * Generate privacy compliance report / 生成隐私合规报告
     *
     * @param format Report format / 报告格式
     */
    private fun generateReport(format: ReportFormat) {
        viewModelScope.launch {
            try {
                val state = _state.value
                val config = state.reportConfig

                // Create mock report content / 创建模拟报告内容
                val content = buildString {
                    appendLine("# Android 联系人隐私合规报告")
                    appendLine()
                    appendLine("## 项目信息")
                    appendLine("- 项目名称: MyMviProject")
                    appendLine("- 检测日期: ${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm").format(java.util.Date())}")
                    appendLine()
                    appendLine("## READ_CONTACTS 使用统计")
                    appendLine("- P0 (可直接迁移): ${state.p0Count} 处")
                    appendLine("- P1 (需修改): ${state.p1Count} 处")
                    appendLine("- P2 (需评估): ${state.p2Count} 处")
                    appendLine()
                    if (config.includePermissionUsage) {
                        appendLine("## 权限使用记录")
                        appendLine("- 检测到 ${state.scanResults.size} 处 READ_CONTACTS 调用")
                        appendLine("- 建议迁移至 Android 17 Contacts Picker API")
                        appendLine()
                    }
                    if (config.includeMigrationComparison) {
                        appendLine("## 迁移前后对比")
                        appendLine()
                        appendLine("### 迁移前")
                        appendLine("- 需要 READ_CONTACTS 权限")
                        appendLine("- 可读取全部联系人数据")
                        appendLine("- 用户无法选择性分享字段")
                        appendLine()
                        appendLine("### 迁移后")
                        appendLine("- 无需 READ_CONTACTS 权限")
                        appendLine("- 仅获取用户选择的联系人字段")
                        appendLine("- 符合 Google Play 隐私政策")
                        appendLine()
                    }
                    appendLine("## 建议")
                    appendLine("1. 将所有 P0 调用点迁移至 Contacts Picker API")
                    appendLine("2. 在 Android 17+ 使用系统 Picker，兼容模式降级处理")
                    appendLine("3. 使用 PickerConfig 配置所需字段，最小化数据获取")
                }

                val report = GeneratedReport(content = content, format = format)
                _state.value = _state.value.copy(generatedReport = report)

                _effect.send(ContactsPickerEffect.ShowSnackbar("报告生成成功", SnackbarType.SUCCESS))

            } catch (e: Exception) {
                _effect.send(ContactsPickerEffect.ShowSnackbar(
                    "报告生成失败: ${e.message}",
                    SnackbarType.ERROR
                ))
            }
        }
    }

    // =============================================================
    // Template
    // =============================================================
    /**
     * Select an integration template / 选择集成模板
     *
     * @param template Template to select / 要选择的模板
     */
    private fun selectTemplate(template: Template) {
        _state.value = _state.value.copy(selectedTemplate = template)
        viewModelScope.launch {
            _effect.send(ContactsPickerEffect.NavigateToTemplateDetail(template))
        }
    }

    /**
     * Export template to target path / 导出模板到目标路径
     *
     * @param template Template to export / 要导出的模板
     * @param targetPath Target file path / 目标文件路径
     */
    private fun exportTemplate(template: Template, targetPath: String) {
        viewModelScope.launch {
            try {
                // Simulate file export / 模拟文件导出
                delay(500)
                _effect.send(ContactsPickerEffect.ExportSuccess(targetPath))
                _effect.send(ContactsPickerEffect.ShowSnackbar(
                    "模板已导出: $targetPath",
                    SnackbarType.SUCCESS
                ))
            } catch (e: Exception) {
                _effect.send(ContactsPickerEffect.ShowSnackbar(
                    "导出失败: ${e.message}",
                    SnackbarType.ERROR
                ))
            }
        }
    }

    // =============================================================
    // Compatibility Check
    // =============================================================
    /**
     * Check Android version compatibility / 检查 Android 版本兼容性
     *
     * Uses Build.VERSION.SDK_INT to determine if Contacts Picker API is available.
     * Contacts Picker API (ACTION_PICK_CONTACTS) is available from Android 17 (API 36).
     */
    private fun checkCompatibility() {
        viewModelScope.launch {
            val sdkInt = Build.VERSION.SDK_INT
            val isCompatible = sdkInt >= 36 // Android 17 (API 36)

            val features = if (isCompatible) {
                listOf(
                    "ContactsContract.EXTRA_PICK_CONTACTS_REQUESTED_DATA_FIELDS 支持",
                    "多选模式支持",
                    "字段级别选择性读取",
                    "无需 READ_CONTACTS 权限"
                )
            } else {
                listOf(
                    "需升级至 Android 17+ 以使用完整功能",
                    "兼容模式: 使用传统 READ_CONTACTS 权限",
                    "建议: 添加版本检测，降级处理"
                )
            }

            val fallbackStrategy = if (isCompatible) {
                "无需降级，使用原生 Contacts Picker API"
            } else {
                "检测 SDK 版本，Android 17+ 使用新 API，旧版本请求 READ_CONTACTS 权限"
            }

            val result = CompatibilityResult(
                currentSdkInt = sdkInt,
                isCompatible = isCompatible,
                fallbackStrategy = fallbackStrategy,
                features = features
            )

            _state.value = _state.value.copy(compatibilityResult = result)

            _effect.send(ContactsPickerEffect.ShowSnackbar(
                if (isCompatible) "当前设备支持 Contacts Picker API" else "当前设备不支持 Contacts Picker API",
                if (isCompatible) SnackbarType.SUCCESS else SnackbarType.INFO
            ))
        }
    }
}
