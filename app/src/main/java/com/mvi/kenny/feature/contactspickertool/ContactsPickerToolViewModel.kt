package com.mvi.kenny.feature.contactspickertool

import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
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
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

// =============================================================
// ContactsPickerToolViewModel — Contacts Picker 工具 ViewModel
// =============================================================
/**
 * ViewModel for Contacts Picker Tool / Contacts Picker 工具 ViewModel
 *
 * Manages all state transitions following MVI pattern.
 * ViewModelScope is used for all coroutine operations.
 *
 * Key responsibilities:
 * - Scan project for READ_CONTACTS permission usage
 * - Generate privacy compliance reports
 * - Provide Contacts Picker Kotlin library code generation
 * - Check device compatibility with Contacts Picker API
 *
 * @see ContactsPickerToolContract For State, Intent, Effect definitions
 * @see ContactsPickerToolScreen For UI implementation
 */
class ContactsPickerToolViewModel : ViewModel() {

    // ---------------------------------------------------------
    // State — single source of truth for UI
    // ---------------------------------------------------------
    private val _state = MutableStateFlow(ContactsPickerToolState.Initial)
    val state: StateFlow<ContactsPickerToolState> = _state.asStateFlow()

    // ---------------------------------------------------------
    // Effect — one-time events for UI
    // ---------------------------------------------------------
    private val _effect = MutableSharedFlow<ContactsPickerToolEffect>()
    val effect: SharedFlow<ContactsPickerToolEffect> = _effect.asSharedFlow()

    // ---------------------------------------------------------
    // Internal state
    // ---------------------------------------------------------
    private var scanJob: Job? = null

    // ---------------------------------------------------------
    // Intent processing — process user actions
    // ---------------------------------------------------------
    /**
     * Process user intent / 处理用户意图
     *
     * Called from UI layer when user performs an action.
     * Each when branch handles one Intent type.
     */
    fun processIntent(intent: ContactsPickerToolIntent) {
        when (intent) {
            is ContactsPickerToolIntent.SelectTab -> handleSelectTab(intent.tab)
            is ContactsPickerToolIntent.StartScan -> handleStartScan(intent.modulePath)
            is ContactsPickerToolIntent.CancelScan -> handleCancelScan()
            is ContactsPickerToolIntent.SelectCallSite -> handleSelectCallSite(intent.callSite)
            is ContactsPickerToolIntent.ClearCallSite -> handleClearCallSite()
            is ContactsPickerToolIntent.UpdatePickerConfig -> handleUpdatePickerConfig(intent.config)
            is ContactsPickerToolIntent.GenerateReport -> handleGenerateReport(intent.format)
            is ContactsPickerToolIntent.SelectTemplate -> handleSelectTemplate(intent.template)
            is ContactsPickerToolIntent.ExportTemplate -> handleExportTemplate(intent.template, intent.targetPath)
            is ContactsPickerToolIntent.CheckCompatibility -> handleCheckCompatibility()
            is ContactsPickerToolIntent.UpdateReportConfig -> handleUpdateReportConfig(intent.config)
            is ContactsPickerToolIntent.DismissError -> handleDismissError()
            is ContactsPickerToolIntent.ClearReport -> handleClearReport()
        }
    }

    // ---------------------------------------------------------
    // Intent handlers
    // ---------------------------------------------------------

    /**
     * Handle tab selection / 处理 Tab 选择
     *
     * Switches between tool modules (Scanner/Library/Report/Templates/Compatibility).
     */
    private fun handleSelectTab(tab: ToolTab) {
        _state.update { it.copy(activeTab = tab) }
    }

    /**
     * Handle scan initiation / 处理扫描启动
     *
     * Starts a simulated READ_CONTACTS permission scan.
     * In production, this would use Kotlin PSI or lint AST analysis.
     *
     * @param modulePath Path to scan / 扫描路径
     */
    private fun handleStartScan(modulePath: String) {
        // Cancel any existing scan / 取消任何现有扫描
        scanJob?.cancel()

        _state.update {
            it.copy(
                scanStatus = ScanStatus.SCANNING,
                scanProgress = 0f,
                scanResults = emptyList(),
                selectedModule = modulePath,
                error = null
            )
        }

        scanJob = viewModelScope.launch {
            try {
                // Simulate scanning progress / 模拟扫描进度
                // In production: use Kotlin PSI or lint to analyze source files
                val results = withContext(Dispatchers.Default) {
                    simulateScan(modulePath)
                }

                _state.update {
                    it.copy(
                        scanStatus = ScanStatus.COMPLETED,
                        scanProgress = 1f,
                        scanResults = results
                    )
                }
                _effect.emit(ContactsPickerToolEffect.ScanComplete)

            } catch (e: Exception) {
                if (e is kotlinx.coroutines.CancellationException) {
                    // Scan was cancelled / 扫描被取消
                    _state.update {
                        it.copy(scanStatus = ScanStatus.IDLE, scanProgress = 0f)
                    }
                } else {
                    _state.update {
                        it.copy(
                            scanStatus = ScanStatus.ERROR,
                            error = e.message ?: "Unknown scan error / 未知扫描错误"
                        )
                    }
                    _effect.emit(
                        ContactsPickerToolEffect.ShowSnackbar(
                            "Scan failed: ${e.message}",
                            isError = true
                        )
                    )
                }
            }
        }
    }

    /**
     * Simulate scan with demo results / 用演示结果模拟扫描
     *
     * In production, this would be replaced with actual PSI/AST analysis.
     *
     * @param modulePath Module path being scanned / 正在扫描的模块路径
     * @return List of detected call sites / 检测到的调用点列表
     */
    private suspend fun simulateScan(modulePath: String): List<PermissionCallSite> {
        // Simulate 3-second scan with progress updates / 模拟 3 秒扫描过程
        val totalSteps = 10
        repeat(totalSteps) { step ->
            delay(300)
            _state.update {
                it.copy(scanProgress = (step + 1).toFloat() / totalSteps)
            }
        }

        // Return simulated call sites / 返回模拟的调用点
        return listOf(
            PermissionCallSite(
                id = UUID.randomUUID().toString(),
                filePath = "$modulePath/data/repository/ContactRepository.kt",
                lineNumber = 34,
                methodName = "getAllContacts()",
                className = "ContactRepository",
                callChain = "ContactRepository.getAllContacts() → ContentResolver.query(READ_CONTACTS)",
                migrabilityRating = Migrability.P0,
                migrationSuggestion = "Replace with ContactsContract.UserDictionary.entry or Contacts Picker API",
                isAlreadyUsingPicker = false
            ),
            PermissionCallSite(
                id = UUID.randomUUID().toString(),
                filePath = "$modulePath/ui/contacts/ContactListFragment.kt",
                lineNumber = 78,
                methodName = "loadContacts()",
                className = "ContactListFragment",
                callChain = "ContactListFragment.loadContacts() → ActivityCompat.requestPermissions(READ_CONTACTS)",
                migrabilityRating = Migrability.P0,
                migrationSuggestion = "Use ActivityResultContracts.PickContactContracts to launch Contacts Picker",
                isAlreadyUsingPicker = false
            ),
            PermissionCallSite(
                id = UUID.randomUUID().toString(),
                filePath = "$modulePath/domain/usecase/GetContactByPhoneUseCase.kt",
                lineNumber = 45,
                methodName = "execute(phone: String)",
                className = "GetContactByPhoneUseCase",
                callChain = "GetContactByPhoneUseCase.execute() → contentResolver.query(READ_CONTACTS)",
                migrabilityRating = Migrability.P1,
                migrationSuggestion = "Use Contacts Picker with phone field filter",
                isAlreadyUsingPicker = false
            ),
            PermissionCallSite(
                id = UUID.randomUUID().toString(),
                filePath = "$modulePath/di/ContactModule.kt",
                lineNumber = 22,
                methodName = "provideContactResolver()",
                className = "ContactModule",
                callChain = "ContactModule.provideContactResolver() → addManifestPermission(READ_CONTACTS)",
                migrabilityRating = Migrability.P2,
                migrationSuggestion = "Keep permission declaration but migrate actual usage to Contacts Picker",
                isAlreadyUsingPicker = false
            )
        )
    }

    /**
     * Handle scan cancellation / 处理扫描取消
     */
    private fun handleCancelScan() {
        scanJob?.cancel()
        _state.update {
            it.copy(
                scanStatus = ScanStatus.IDLE,
                scanProgress = 0f
            )
        }
    }

    /**
     * Handle call site selection / 处理调用点选择
     *
     * @param callSite Selected call site / 选中的调用点
     */
    private fun handleSelectCallSite(callSite: PermissionCallSite) {
        _state.update { it.copy(selectedCallSite = callSite) }
    }

    /**
     * Handle clearing selected call site / 处理清除选中的调用点
     */
    private fun handleClearCallSite() {
        _state.update { it.copy(selectedCallSite = null) }
    }

    /**
     * Handle Picker configuration update / 处理 Picker 配置更新
     *
     * @param config New picker configuration / 新的 Picker 配置
     */
    private fun handleUpdatePickerConfig(config: PickerConfig) {
        _state.update { it.copy(pickerConfig = config) }
    }

    /**
     * Handle report generation / 处理报告生成
     *
     * @param format Report format / 报告格式
     */
    private fun handleGenerateReport(format: ReportFormat) {
        viewModelScope.launch {
            _state.update { it.copy(isGeneratingReport = true, error = null) }

            try {
                val currentState = _state.value
                val report = withContext(Dispatchers.Default) {
                    generateComplianceReport(currentState, format)
                }

                _state.update {
                    it.copy(
                        generatedReport = report,
                        isGeneratingReport = false,
                        reportConfig = it.reportConfig.copy(format = format)
                    )
                }

                _effect.emit(
                    ContactsPickerToolEffect.ShowSnackbar(
                        "Report generated: ${report.fileName}",
                        isError = false
                    )
                )

            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isGeneratingReport = false,
                        error = "Report generation failed: ${e.message}"
                    )
                }
                _effect.emit(
                    ContactsPickerToolEffect.ShowSnackbar(
                        "Report generation failed: ${e.message}",
                        isError = true
                    )
                )
            }
        }
    }

    /**
     * Generate compliance report / 生成合规报告
     *
     * @param currentState Current UI state / 当前 UI 状态
     * @param format Report format / 报告格式
     * @return Generated report / 生成的报告
     */
    private fun generateComplianceReport(
        currentState: ContactsPickerToolState,
        format: ReportFormat
    ): GeneratedReport {
        val timestamp = System.currentTimeMillis()
        val dateStr = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date(timestamp))
        val fileName = "contacts_compliance_report_$dateStr.${format.name.lowercase()}"
        val filePath = "/tmp/$fileName" // In production: use project directory

        val totalCount = currentState.scanResults.size
        val migratedCount = currentState.scanResults.count { it.isAlreadyUsingPicker }
        val remainingCount = totalCount - migratedCount

        return GeneratedReport(
            fileName = fileName,
            filePath = filePath,
            generatedAt = timestamp,
            summary = buildString {
                append("## Privacy Compliance Report Summary / 隐私合规报告摘要\n\n")
                append("Total READ_CONTACTS call sites found: $totalCount\n")
                append("Already migrated to Contacts Picker: $migratedCount\n")
                append("Remaining to migrate: $remainingCount\n")
                append("P0 critical: ${currentState.p0Count}\n")
                append("P1 important: ${currentState.p1Count}\n")
                append("P2 optional: ${currentState.p2Count}\n")
            },
            permissionUsageCount = totalCount,
            migratedCount = migratedCount,
            remainingCount = remainingCount
        )
    }

    /**
     * Handle template selection / 处理模板选择
     *
     * @param template Selected template / 选中的模板
     */
    private fun handleSelectTemplate(template: Template) {
        _state.update { it.copy(selectedTemplate = template) }
        viewModelScope.launch {
            _effect.emit(ContactsPickerToolEffect.NavigateToTemplateDetail(template))
        }
    }

    /**
     * Handle template export / 处理模板导出
     *
     * @param template Template to export / 要导出的模板
     * @param targetPath Target file path / 目标文件路径
     */
    private fun handleExportTemplate(template: Template, targetPath: String) {
        viewModelScope.launch {
            _state.update { it.copy(isExportingTemplate = true, error = null) }

            try {
                withContext(Dispatchers.IO) {
                    File(targetPath).apply {
                        parentFile?.mkdirs()
                        writeText(template.code)
                    }
                }

                _state.update { it.copy(isExportingTemplate = false) }
                _effect.emit(ContactsPickerToolEffect.ExportSuccess(targetPath))
                _effect.emit(
                    ContactsPickerToolEffect.ShowSnackbar(
                        "Template exported to: $targetPath",
                        isError = false
                    )
                )

            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isExportingTemplate = false,
                        error = "Export failed: ${e.message}"
                    )
                }
                _effect.emit(
                    ContactsPickerToolEffect.ShowSnackbar(
                        "Export failed: ${e.message}",
                        isError = true
                    )
                )
            }
        }
    }

    /**
     * Handle compatibility check / 处理兼容性检测
     *
     * Checks if the current device supports Contacts Picker API (Android 17+).
     */
    private fun handleCheckCompatibility() {
        viewModelScope.launch {
            _state.update { it.copy(isCheckingCompatibility = true, error = null) }

            try {
                val result = withContext(Dispatchers.Default) {
                    checkDeviceCompatibility()
                }

                _state.update {
                    it.copy(
                        compatibilityResult = result,
                        isCheckingCompatibility = false
                    )
                }

            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isCheckingCompatibility = false,
                        error = "Compatibility check failed: ${e.message}"
                    )
                }
                _effect.emit(
                    ContactsPickerToolEffect.ShowSnackbar(
                        "Compatibility check failed: ${e.message}",
                        isError = true
                    )
                )
            }
        }
    }

    /**
     * Check device Contacts Picker compatibility / 检测设备 Contacts Picker 兼容性
     *
     * @return Compatibility result / 兼容性结果
     */
    private fun checkDeviceCompatibility(): CompatibilityResult {
        val deviceVersion = Build.VERSION.SDK_INT
        val isCompatible = deviceVersion >= 34 // API 34+ has full Contacts Picker support

        val supportedFields = if (isCompatible) {
            listOf("name", "phone", "email", "address")
        } else if (deviceVersion >= 29) {
            listOf("name", "phone") // Basic support on API 29+
        } else {
            emptyList()
        }

        val unsupportedFields = PickerField.ALL_FIELDS
            .map { it.key }
            .filter { it !in supportedFields }

        val (recommendation, fallbackStrategy) = when {
            deviceVersion >= 34 -> Pair(
                "Full Contacts Picker API supported. No additional work required.",
                "N/A — device fully supports new API."
            )
            deviceVersion >= 29 -> Pair(
                "Basic Contacts Picker supported. Advanced fields (address) not available.",
                "Use getContext().contentResolver.query() with READ_CONTACTS as fallback for address field."
            )
            else -> Pair(
                "Contacts Picker API not available. Device must be Android 10 (API 29) or higher.",
                "Use traditional ContentResolver.query() with READ_CONTACTS permission. Consider prompting user to upgrade device."
            )
        }

        return CompatibilityResult(
            deviceAndroidVersion = deviceVersion,
            isCompatible = isCompatible,
            supportedFields = supportedFields,
            unsupportedFields = unsupportedFields,
            recommendation = recommendation,
            fallbackStrategy = fallbackStrategy
        )
    }

    /**
     * Handle report config update / 处理报告配置更新
     *
     * @param config New report configuration / 新的报告配置
     */
    private fun handleUpdateReportConfig(config: ReportConfig) {
        _state.update { it.copy(reportConfig = config) }
    }

    /**
     * Handle error dismissal / 处理错误关闭
     */
    private fun handleDismissError() {
        _state.update { it.copy(error = null) }
    }

    /**
     * Handle report clearing / 处理报告清除
     */
    private fun handleClearReport() {
        _state.update { it.copy(generatedReport = null) }
    }

    // ---------------------------------------------------------
    // Lifecycle
    // ---------------------------------------------------------

    override fun onCleared() {
        super.onCleared()
        scanJob?.cancel()
    }
}
