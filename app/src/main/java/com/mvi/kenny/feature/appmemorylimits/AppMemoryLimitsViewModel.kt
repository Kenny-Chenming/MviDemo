package com.mvi.kenny.feature.appmemorylimits

// ================================================================
// AppMemoryLimitsViewModel — Android 17 App Memory Limits ViewModel
// ================================================================
// MVI ViewModel for Android 17 App Memory Limits Developer Toolkit.
//
// PRD-262: Android 17 App Memory Limits 开发者适配工具包
//
// Responsibilities:
//   - Detect device RAM and calculate per-app memory limit
//   - Handle tool selection and detail display
//   - Process risk assessment for different app types
//   - Manage simulator and report export
// ================================================================

import android.app.ActivityManager
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * ============================================================
 * AppMemoryLimitsViewModel — MVI ViewModel
 * ============================================================
 *
 * @param context Android Context (typically Application)
 */
class AppMemoryLimitsViewModel(
    private val context: Context? = null
) : ViewModel() {

    // ─────────────────────────────────────────────────────────
    // State — MVI single source of truth
    // ─────────────────────────────────────────────────────────
    private val _state = MutableStateFlow(AppMemoryLimitsState(
        tools = getDefaultTools()
    ))
    val state: StateFlow<AppMemoryLimitsState> = _state.asStateFlow()

    // ─────────────────────────────────────────────────────────
    // Intent Handler — Process user intentions
    // ─────────────────────────────────────────────────────────
    fun sendIntent(intent: AppMemoryLimitsIntent) {
        when (intent) {
            is AppMemoryLimitsIntent.DetectDevice -> detectDevice()
            is AppMemoryLimitsIntent.SelectTool -> selectTool(intent.toolIndex)
            is AppMemoryLimitsIntent.LoadRiskAssessment -> loadRiskAssessment(intent.appType)
            is AppMemoryLimitsIntent.CopyCode -> copyCode(intent.code)
            is AppMemoryLimitsIntent.RunSimulator -> runSimulator(intent.deviceRam)
            is AppMemoryLimitsIntent.ExportReport -> exportReport(intent.format)
            is AppMemoryLimitsIntent.UpdateSearch -> updateSearch(intent.query)
            is AppMemoryLimitsIntent.ClearError -> clearError()
        }
    }

    // ─────────────────────────────────────────────────────────
    // Device Detection — 获取设备 RAM 和 App 内存上限
    // ─────────────────────────────────────────────────────────
    private fun detectDevice() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }

            try {
                val result = withContext(Dispatchers.IO) {
                    detectDeviceRamInternal()
                }
                _state.update {
                    it.copy(
                        isLoading = false,
                        deviceRamBytes = result.first,
                        appMemoryLimitBytes = result.second
                    )
                }
            } catch (e: Exception) {
                val simulatedRam = 8L * 1024 * 1024 * 1024
                val simulatedLimit = calculateAppMemoryLimit(simulatedRam)
                _state.update {
                    it.copy(
                        isLoading = false,
                        deviceRamBytes = simulatedRam,
                        appMemoryLimitBytes = simulatedLimit,
                        errorMessage = "真实设备检测失败，使用模拟数据：" + formatBytes(simulatedRam)
                    )
                }
            }
        }
    }

    private fun detectDeviceRamInternal(): Pair<Long, Long> {
        if (context == null) {
            val ram = 8L * 1024 * 1024 * 1024
            return ram to calculateAppMemoryLimit(ram)
        }

        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memoryInfo)

        val deviceRamBytes = memoryInfo.totalMem
        val appMemoryLimitBytes = activityManager.memoryClass * 1024L * 1024
        val calculatedLimit = calculateAppMemoryLimit(deviceRamBytes)
        val finalLimit = minOf(appMemoryLimitBytes, calculatedLimit)
        return deviceRamBytes to finalLimit
    }

    // ─────────────────────────────────────────────────────────
    // Tool Selection — 工具选择
    // ─────────────────────────────────────────────────────────
    private fun selectTool(toolIndex: Int) {
        _state.update { it.copy(selectedToolIndex = toolIndex) }
    }

    // ─────────────────────────────────────────────────────────
    // Risk Assessment — 风险评估
    // ─────────────────────────────────────────────────────────
    private fun loadRiskAssessment(appType: AppType) {
        val riskLevel = when (appType) {
            AppType.GAME -> AppRiskLevel.HIGH
            AppType.IMAGE_PROCESSING -> AppRiskLevel.HIGH
            AppType.NORMAL -> AppRiskLevel.LOW
        }
        _state.update {
            it.copy(currentAppType = appType, riskLevel = riskLevel)
        }
    }

    // ─────────────────────────────────────────────────────────
    // Copy Code — 复制代码
    // ─────────────────────────────────────────────────────────
    private fun copyCode(code: String) {
        viewModelScope.launch {
            _effect.emit(AppMemoryLimitsEffect.CopyToClipboard(code))
        }
    }

    // ─────────────────────────────────────────────────────────
    // Simulator — 模拟器
    // ─────────────────────────────────────────────────────────
    private fun runSimulator(deviceRam: Long) {
        val limit = calculateAppMemoryLimit(deviceRam)
        val warnings = mutableListOf<String>()
        val suggestions = mutableListOf<String>()

        when {
            deviceRam <= 6L * 1024 * 1024 * 1024 -> {
                warnings.add("⚠️ 6GB 及以下设备最严苛，per-app 上限仅 " + formatBytes(limit))
                warnings.add("游戏/图像处理应用在此类设备上风险最高")
                suggestions.add("优先在 6GB 设备上测试")
            }
            deviceRam <= 8L * 1024 * 1024 * 1024 -> {
                warnings.add("8GB 设备 per-app 上限为 " + formatBytes(limit))
            }
            else -> {
                warnings.add(formatBytes(deviceRam) + " 设备 per-app 上限为 " + formatBytes(limit) + "（已达512MB上限）")
            }
        }

        suggestions.add("使用 LeakCanary 检测内存泄漏")
        suggestions.add("实现 Bitmap inSampleSize 优化")
        suggestions.add("使用对象池减少 GC 压力")

        val riskLevel = when {
            deviceRam <= 6L * 1024 * 1024 * 1024 -> AppRiskLevel.HIGH
            deviceRam <= 8L * 1024 * 1024 * 1024 -> AppRiskLevel.MEDIUM
            else -> AppRiskLevel.LOW
        }

        val result = SimulatorResult(
            deviceRam = deviceRam,
            appMemoryLimit = limit,
            riskLevel = riskLevel,
            warnings = warnings,
            suggestions = suggestions
        )

        _state.update {
            it.copy(simulatorInputRam = deviceRam, simulatorResult = result)
        }
    }

    // ─────────────────────────────────────────────────────────
    // Export Report — 导出报告
    // ─────────────────────────────────────────────────────────
    private fun exportReport(format: ExportFormat) {
        viewModelScope.launch {
            val currentState = _state.value
            val content = buildString {
                if (format == ExportFormat.JSON) {
                    append("{\n")
                    append("  \"deviceRamBytes\": " + currentState.deviceRamBytes + ",\n")
                    append("  \"appMemoryLimitBytes\": " + currentState.appMemoryLimitBytes + ",\n")
                    append("  \"appMemoryLimitFormatted\": \"" + formatBytes(currentState.appMemoryLimitBytes) + "\",\n")
                    append("  \"riskLevel\": \"" + currentState.riskLevel.name + "\",\n")
                    append("  \"currentAppType\": \"" + currentState.currentAppType.name + "\",\n")
                    append("  \"tools\": [\n")
                    currentState.tools.forEachIndexed { index, tool ->
                        append("    {\"id\": " + tool.id + ", \"name\": \"" + tool.name + "\", \"category\": \"" + tool.category.name + "\"}")
                        if (index < currentState.tools.size - 1) append(",")
                        append("\n")
                    }
                    append("  ]\n")
                    append("}")
                } else {
                    append("<html><body>\n")
                    append("<h1>Android 17 App Memory Limits Report</h1>\n")
                    append("<h2>Device: " + formatBytes(currentState.deviceRamBytes) + "</h2>\n")
                    append("<h2>Per-App Limit: " + formatBytes(currentState.appMemoryLimitBytes) + "</h2>\n")
                    append("<h2>Risk Level: " + currentState.riskLevel.displayName + "</h2>\n")
                    append("</body></html>")
                }
            }

            _effect.emit(AppMemoryLimitsEffect.ExportFile(content, format))
            _state.update { it.copy(exportFormat = format, exportResult = content) }
        }
    }

    // ─────────────────────────────────────────────────────────
    // Search — 搜索
    // ─────────────────────────────────────────────────────────
    private fun updateSearch(query: String) {
        _state.update { it.copy(searchQuery = query) }
    }

    // ─────────────────────────────────────────────────────────
    // Error — 错误处理
    // ─────────────────────────────────────────────────────────
    private fun clearError() {
        _state.update { it.copy(errorMessage = null) }
    }

    // ─────────────────────────────────────────────────────────
    // Effect Channel — 一次性副作用
    // ─────────────────────────────────────────────────────────
    private val _effect = MutableSharedFlow<AppMemoryLimitsEffect>()
    val effect = _effect.asSharedFlow()
}
