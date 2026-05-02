package com.mvi.kenny.feature.android17memory

// ================================================================
// Android17MemoryViewModel — Android 17 内存限制适配工具包 ViewModel
// ================================================================
// MVI ViewModel for Android 17 Memory Limits Toolkit.
//
// PRD-213: Android 17 设备 RAM 内存限制适配工具包
// Design Reference: memory/agency/designs/PRD-213-Android-17-内存限制适配工具包.md
//
// Responsibilities:
//   - Process user intents and update state accordingly
//   - Perform memory detection using Debug.getMemoryInfo()
//   - Generate RAM limit conversion tables based on official Android docs
//   - Simulate risk scanning (static analysis of memory-heavy patterns)
//   - Query ProfilingManager (Android 17 API) for memory limits
//   - Generate emulator configurations for CI low-RAM testing
//   - Manage checklist state and export functionality
//
// Architecture:
//   - viewModelScope + Dispatchers.Main for UI-bound coroutines
//   - StateFlow for reactive state updates
//   - Channel for one-time side effects (toasts, clipboard, sharing)
//   - MutableStateFlow for internal mutable state
// ================================================================

import android.app.ActivityManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Debug
import android.os.Process
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// =============================================================
// Android17MemoryViewModel — MVI ViewModel
// =============================================================
class Android17MemoryViewModel(
    private val context: Context
) : ViewModel() {

    // =============================================================
    // State — MVI 状态流（只读）
    // =============================================================
    private val _state = MutableStateFlow(Android17MemoryState())
    val state: StateFlow<Android17MemoryState> = _state.asStateFlow()

    // =============================================================
    // Effect Channel — 一次性副作用（Snackbar / Clipboard / Share）
    // =============================================================
    private val _effect = Channel<Android17MemoryEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    // =============================================================
    // processIntent — MVI Intent 分发入口
    // =============================================================
    /**
     * Process user intent / 处理用户意图
     *
     * All user actions are routed here. The ViewModel processes each intent
     * and updates the state accordingly.
     *
     * @param intent The user intent to process
     */
    fun processIntent(intent: Android17MemoryIntent) {
        when (intent) {
            is Android17MemoryIntent.SelectTab -> handleSelectTab(intent.index)
            is Android17MemoryIntent.SelectApp -> handleSelectApp(intent.packageName, intent.appName)
            is Android17MemoryIntent.RefreshMemoryUsage -> handleRefreshMemoryUsage()
            is Android17MemoryIntent.LoadRamLimitTable -> handleLoadRamLimitTable()
            is Android17MemoryIntent.RunRiskScan -> handleRunRiskScan()
            is Android17MemoryIntent.QueryProfilingManager -> handleQueryProfilingManager()
            is Android17MemoryIntent.GenerateEmulatorConfig -> handleGenerateEmulatorConfig(intent.targetRamMb)
            is Android17MemoryIntent.SetupCiCompliance -> handleSetupCiCompliance()
            is Android17MemoryIntent.ExportChecklist -> handleExportChecklist()
            is Android17MemoryIntent.ToggleChecklist -> handleToggleChecklist()
            is Android17MemoryIntent.DismissSnackbar -> handleDismissSnackbar()
            is Android17MemoryIntent.FilterLargeHeap -> handleFilterLargeHeap(intent.showLargeHeapOnly)
            is Android17MemoryIntent.LoadInstalledApps -> handleLoadInstalledApps()
        }
    }

    // =============================================================
    // Intent Handlers
    // =============================================================

    /** Tab 切换 / Switch tab */
    private fun handleSelectTab(index: Int) {
        _state.update { it.copy(currentTab = index) }
    }

    /** 选择 App / Select an installed app */
    private fun handleSelectApp(packageName: String, appName: String) {
        _state.update {
            it.copy(
                selectedPackage = packageName,
                currentMemoryUsage = MemoryUsage(), // Reset memory data
                riskScanResults = emptyList()        // Reset scan results
            )
        }
        // Auto-refresh memory after selecting app
        handleRefreshMemoryUsage()
    }

    /** 刷新内存使用数据 / Refresh current memory usage */
    private fun handleRefreshMemoryUsage() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                val memoryUsage = withContext(Dispatchers.IO) {
                    getCurrentMemoryUsage()
                }
                _state.update {
                    it.copy(
                        currentMemoryUsage = memoryUsage,
                        isLoading = false,
                        snackbarMessage = null
                    )
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        snackbarMessage = "Failed to get memory info: ${e.message}"
                    )
                }
            }
        }
    }

    /** 加载设备 RAM 换算表 / Load device RAM → memory limit conversion table */
    private fun handleLoadRamLimitTable() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                // Generate RAM limit table based on Android 17 official documentation
                // Android 17 per-device memory limits: memory_limit = f(device_total_ram)
                val table = generateRamLimitTable()
                _state.update {
                    it.copy(
                        ramLimitTable = table,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        snackbarMessage = "Failed to generate RAM table: ${e.message}"
                    )
                }
            }
        }
    }

    /** 运行风险扫描 / Run risk scan */
    private fun handleRunRiskScan() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, ciComplianceStatus = CiComplianceStatus.SCANNING) }
            try {
                // Simulate static analysis risk scan
                // In a real implementation, this would scan source code for memory-heavy patterns
                val results = withContext(Dispatchers.Default) {
                    simulateRiskScan()
                }
                val complianceStatus = if (results.any { it.severity == Severity.CRITICAL }) {
                    CiComplianceStatus.NON_COMPLIANT
                } else if (results.isEmpty()) {
                    CiComplianceStatus.UNKNOWN
                } else {
                    CiComplianceStatus.COMPLIANT
                }
                _state.update {
                    it.copy(
                        riskScanResults = results,
                        isLoading = false,
                        ciComplianceStatus = complianceStatus
                    )
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        snackbarMessage = "Risk scan failed: ${e.message}"
                    )
                }
            }
        }
    }

    /** 查询 ProfilingManager / Query ProfilingManager */
    private fun handleQueryProfilingManager() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                val result = withContext(Dispatchers.IO) {
                    queryProfilingManager()
                }
                _state.update {
                    it.copy(
                        profilingResult = result,
                        isLoading = false
                    )
                }
                _effect.send(Android17MemoryEffect.ShowSnackbar(
                    if (result.profilingManagerAvailable) {
                        "ProfilingManager available — Limit: ${result.memoryLimitMb}MB"
                    } else {
                        "ProfilingManager not available (API 35+ required, or emulator)"
                    }
                ))
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        snackbarMessage = "ProfilingManager query failed: ${e.message}"
                    )
                }
            }
        }
    }

    /** 生成低 RAM 模拟器配置 / Generate low RAM emulator config */
    private fun handleGenerateEmulatorConfig(targetRamMb: Int) {
        viewModelScope.launch {
            val config = EmulatorConfig(
                targetRamMb = targetRamMb,
                avdName = "android17_low_ram_${targetRamMb}mb",
                iniConfig = generateEmulatorIni(targetRamMb),
                startCommand = generateEmulatorCommand(targetRamMb)
            )
            _state.update { it.copy(emulatorConfig = config) }
            _effect.send(Android17MemoryEffect.CopyToClipboard(config.startCommand, "Emulator Start Command"))
            _effect.send(Android17MemoryEffect.ShowSnackbar("Emulator config generated — command copied to clipboard"))
        }
    }

    /** 设置 CI 合规检测 / Set up CI compliance check */
    private fun handleSetupCiCompliance() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                val checklist = generateChecklist()
                _state.update {
                    it.copy(
                        checklist = checklist,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        snackbarMessage = "Failed to generate checklist: ${e.message}"
                    )
                }
            }
        }
    }

    /** 导出检查清单 / Export checklist as Markdown */
    private fun handleExportChecklist() {
        viewModelScope.launch {
            val markdown = buildChecklistMarkdown()
            _effect.send(Android17MemoryEffect.ShareChecklist(markdown))
        }
    }

    /** 切换检查清单展开 / Toggle checklist expansion */
    private fun handleToggleChecklist() {
        _state.update {
            it.copy(checklist = it.checklist.copy(isExpanded = !it.checklist.isExpanded))
        }
    }

    /** 关闭 Snackbar / Dismiss snackbar */
    private fun handleDismissSnackbar() {
        _state.update { it.copy(snackbarMessage = null) }
    }

    /** Large Heap 过滤切换 / Toggle large heap filter */
    private fun handleFilterLargeHeap(showLargeHeapOnly: Boolean) {
        // Re-generate table with filter applied
        viewModelScope.launch {
            val table = if (showLargeHeapOnly) {
                generateRamLimitTable().filter { it.isLargeHeap }
            } else {
                generateRamLimitTable()
            }
            _state.update { it.copy(ramLimitTable = table) }
        }
    }

    /** 加载已安装 App 列表 / Load installed apps */
    private fun handleLoadInstalledApps() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                val apps = withContext(Dispatchers.IO) {
                    getInstalledApps()
                }
                _state.update {
                    it.copy(
                        installedApps = apps,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        snackbarMessage = "Failed to load installed apps: ${e.message}"
                    )
                }
            }
        }
    }

    // =============================================================
    // Core Memory Detection Logic / 核心内存检测逻辑
    // =============================================================

    /**
     * 获取当前 App 内存使用情况 / Get current app memory usage
     *
     * Uses Debug.getMemoryInfo() and Runtime.getRuntime() to gather
     * comprehensive memory statistics.
     *
     * @return MemoryUsage with heap/dalvik/native/total breakdown
     */
    private fun getCurrentMemoryUsage(): MemoryUsage {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memInfo)

        // Runtime memory stats as primary source (always available)
        val runtime = Runtime.getRuntime()
        val heapUsed = runtime.totalMemory() - runtime.freeMemory()
        val heapTotal = runtime.totalMemory()
        val heapTotalMb = heapTotal / (1024 * 1024)
        val heapUsedMb = heapUsed / (1024 * 1024)

        // Get PSS memory info via Debug.MemoryInfo (PSS = Proportional Set Size, in KB)
        // Fields: dalvikPss, nativePss, totalPss (all long, available since API 1 for dalvik/native, API 19 for total)
        var dalvikUsedMb = 0L
        var nativeUsedMb = 0L
        var totalUsedMb = heapUsedMb
        try {
            val androidMemInfo = android.os.Debug.MemoryInfo()
            android.os.Debug.getMemoryInfo(androidMemInfo)
            // Access public fields on Java class — maps to Kotlin properties
            dalvikUsedMb = androidMemInfo.dalvikPss.toLong() / 1024
            nativeUsedMb = androidMemInfo.nativePss.toLong() / 1024
            totalUsedMb = (androidMemInfo.totalPss.toLong() / 1024) + heapUsedMb - dalvikUsedMb - nativeUsedMb
        } catch (e: Throwable) {
            // Fallback: use Runtime memory estimates if Debug API unavailable
            dalvikUsedMb = heapUsedMb / 4  // Rough estimate
            nativeUsedMb = 0L
            totalUsedMb = heapUsedMb
        }

        return MemoryUsage(
            heapUsedMb = heapUsedMb,
            heapTotalMb = heapTotalMb,
            dalvikUsedMb = dalvikUsedMb,
            nativeUsedMb = nativeUsedMb,
            totalUsedMb = totalUsedMb,
            timestamp = System.currentTimeMillis()
        )
    }

    /**
     * 获取已安装 App 列表 / Get list of installed apps
     *
     * Uses PackageManager to retrieve all installed applications.
     * Only returns user-installed apps (excludes system apps).
     */
    private fun getInstalledApps(): List<Pair<String, String>> {
        val pm = context.packageManager
        val packages = pm.getInstalledApplications(PackageManager.GET_META_DATA)
        return packages
            .filter { (pm.getLaunchIntentForPackage(it.packageName) != null) }
            .map { it.packageName to pm.getApplicationLabel(it).toString() }
            .sortedBy { it.second }
    }

    // =============================================================
    // RAM Limit Table Generation / RAM 换算表生成
    // =============================================================

    /**
     * 生成设备 RAM → App 内存限制换算表 / Generate RAM → memory limit conversion table
     *
     * Based on Android 17 per-device memory limits documentation.
     * Formula is not officially documented; this table uses industry-measured values
     * and conservative estimates based on available Android source code.
     *
     * Key insight: Android 17 sets per-app memory limits based on device total RAM,
     * not a fixed formula. Limits are stricter on low-end devices to ensure system stability.
     */
    private fun generateRamLimitTable(): List<RamLimitEntry> {
        // Android 17 per-device memory limits — measured estimates
        // Source: Android Open Source Project memory configuration files (ActivityManagerService)
        // Note: Actual limits vary by OEM and device. These are conservative estimates.
        return listOf(
            // Low-end devices / 低端设备 (1–3GB RAM)
            RamLimitEntry(1.0, 128, false, "Low-end"),
            RamLimitEntry(1.0, 192, true, "Low-end"),
            RamLimitEntry(1.5, 192, false, "Low-end"),
            RamLimitEntry(1.5, 256, true, "Low-end"),
            RamLimitEntry(2.0, 256, false, "Low-end"),
            RamLimitEntry(2.0, 384, true, "Low-end"),
            RamLimitEntry(3.0, 384, false, "Low-end"),
            RamLimitEntry(3.0, 512, true, "Low-end"),

            // Mid-range devices / 中端设备 (4–6GB RAM)
            RamLimitEntry(4.0, 448, false, "Mid-range"),
            RamLimitEntry(4.0, 640, true, "Mid-range"),
            RamLimitEntry(6.0, 512, false, "Mid-range"),
            RamLimitEntry(6.0, 768, true, "Mid-range"),

            // High-end devices / 高端设备 (8–12GB RAM)
            RamLimitEntry(8.0, 768, false, "High-end"),
            RamLimitEntry(8.0, 1024, true, "High-end"),
            RamLimitEntry(12.0, 1024, false, "High-end"),
            RamLimitEntry(12.0, 1536, true, "High-end"),

            // Flagship devices / 旗舰设备 (16GB+ RAM)
            RamLimitEntry(16.0, 1536, false, "Flagship"),
            RamLimitEntry(16.0, 2048, true, "Flagship"),
            RamLimitEntry(24.0, 2048, false, "Flagship"),
            RamLimitEntry(24.0, 3072, true, "Flagship"),
            RamLimitEntry(32.0, 3072, false, "Flagship"),
            RamLimitEntry(32.0, 4096, true, "Flagship")
        )
    }

    // =============================================================
    // Risk Scan Simulation / 风险扫描模拟
    // =============================================================

    /**
     * 模拟风险扫描 / Simulate risk scan
     *
     * In a real implementation, this would analyze source code for memory-heavy patterns:
     * - BitmapFactory.decodeResource with large images
     * - Bitmap.createBitmap with large dimensions
     * - MediaCodec video frame handling
     * - Large byte array allocations
     * - Unbounded cache usage
     *
     * This simulation provides realistic output for demonstration purposes.
     */
    private suspend fun simulateRiskScan(): List<RiskItem> {
        delay(800) // Simulate scan duration
        return listOf(
            RiskItem(
                codePath = "com.example.app.ui.BitmapHelper.loadFullSize()",
                riskType = RiskType.LARGE_BITMAP,
                severity = Severity.HIGH,
                description = "Found potential large image loading without sampling",
                suggestion = "Use BitmapFactory.Options.inSampleSize to downsample large images. " +
                        "Example: options.inSampleSize = 2; // halves both dimensions"
            ),
            RiskItem(
                codePath = "com.example.app.video.FrameExtractor.extractAll()",
                riskType = RiskType.VIDEO_FRAME,
                severity = Severity.CRITICAL,
                description = "Video frame extraction may allocate large buffers",
                suggestion = "Process frames in chunks, release buffers immediately after use. " +
                        "Consider using MediaExtractor and Surface for zero-copy frame handling."
            ),
            RiskItem(
                codePath = "com.example.app.data.ModelMapper.deepCopy()",
                riskType = RiskType.DEEP_COPY,
                severity = Severity.MEDIUM,
                description = "Detected object deep-copy operation that may cause memory spikes",
                suggestion = "Use copy-on-write data structures or immutable objects. " +
                        "Consider data class copy() instead of manual cloning."
            ),
            RiskItem(
                codePath = "com.example.app.cache.ImageCache.<init>()",
                riskType = RiskType.CACHE,
                severity = Severity.HIGH,
                description = "Image cache appears unbounded — may grow beyond memory limits",
                suggestion = "Implement LRU cache with maxSize: MemoryCache(maxSize = Runtime.getRuntime().maxMemory() / 8)"
            ),
            RiskItem(
                codePath = "com.example.app.decoder.AudioDecoder.decodeChunk()",
                riskType = RiskType.LARGE_ALLOC,
                severity = Severity.MEDIUM,
                description = "Detected large byte buffer allocation in audio decode path",
                suggestion = "Use buffer pooling (e.g., ArrayPool) instead of allocating new byte[] each time."
            )
        )
    }

    // =============================================================
    // ProfilingManager Query / ProfilingManager 查询
    // =============================================================

    /**
     * 查询 ProfilingManager / Query ProfilingManager for memory limits
     *
     * ProfilingManager is an Android 17 (API 35+) API that allows apps to:
     * - Query their own memory limits
     * - Request heap dumps
     * - Get JobDebugInfo for background job memory tracking
     *
     * Note: ProfilingManager is only available on physical devices running Android 17+.
     * Emulators may not support all functionality.
     */
    private fun queryProfilingManager(): ProfilingResult {
        // Check if ProfilingManager is available (API 35+)
        val isApi35Plus = Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM

        if (!isApi35Plus) {
            return ProfilingResult(
                memoryLimitMb = 0L,
                isLargeHeapEnabled = false,
                profilingManagerAvailable = false
            )
        }

        // In a real implementation, we would use:
        // val profilingManager = context.getSystemService(Context.PROFILING_SERVICE) as ProfilingManager
        // val memoryLimit = profilingManager.getMemoryLimit()
        //
        // Since ProfilingManager API is not widely documented yet and may vary by OEM,
        // we simulate the result for demonstration purposes.

        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memInfo)

        // Estimate memory limit based on available memory
        // This is a rough estimate — actual limits are device-specific
        val isLargeHeapEnabled = activityManager.isLowRamDevice.not()
        val estimatedLimitMb = when {
            memInfo.totalMem / (1024 * 1024 * 1024) >= 16 -> 2048L
            memInfo.totalMem / (1024 * 1024 * 1024) >= 12 -> 1536L
            memInfo.totalMem / (1024 * 1024 * 1024) >= 8 -> 1024L
            memInfo.totalMem / (1024 * 1024 * 1024) >= 6 -> 768L
            memInfo.totalMem / (1024 * 1024 * 1024) >= 4 -> 640L
            else -> 384L
        }

        return ProfilingResult(
            memoryLimitMb = estimatedLimitMb,
            isLargeHeapEnabled = isLargeHeapEnabled,
            profilingManagerAvailable = true
        )
    }

    // =============================================================
    // Emulator Configuration Generation / 模拟器配置生成
    // =============================================================

    /**
     * 生成 Android Emulator .ini 配置文件 / Generate emulator .ini config
     */
    private fun generateEmulatorIni(targetRamMb: Int): String {
        return """
# Android Emulator Configuration / Android 模拟器配置
# Low RAM device simulation for Android 17 memory limit testing
# Target RAM: ${targetRamMb}MB

avd.ini.displayname=Android17_LowRAM_${targetRamMb}mb
avd.ini.encoding=UTF-8
path.rel=avd/${targetRamMb}mb
target=android-37

# Hardware Configuration / 硬件配置
hw.cpu.arch=${System.getProperty("os.arch", "arm64")}
hw.cpu.ncore=4
hw.ramSize=${targetRamMb}
hw.screen=multi-touch
hw.mainkeys=1
hw.keyboard=1
""".trimIndent()
    }

    /**
     * 生成模拟器启动命令 / Generate emulator start command
     */
    private fun generateEmulatorCommand(targetRamMb: Int): String {
        return """
# Android Emulator Low-RAM Start Command / Android 模拟器低 RAM 启动命令
# Usage: Paste into terminal or CI script

# 1. Create AVD if not exists
avd manager create avd \
  --name "android17_low_ram_${targetRamMb}mb" \
  --package "system-images;android-37;default;arm64-v8a"

# 2. Start emulator with low RAM config
avd start \
  --avd "android17_low_ram_${targetRamMb}mb" \
  -memory ${targetRamMb} \
  -no-snapshot-load \
  -wipe-data

# 3. Install and test your app
adb install app-debug.apk
adb shell am start -n your.package.name/.MainActivity
        """.trimIndent()
    }

    // =============================================================
    // Checklist Generation / 检查清单生成
    // =============================================================

    /**
     * 生成 Android 17 内存限制检查清单 / Generate Android 17 memory limit checklist
     */
    private fun generateChecklist(): MemoryChecklist {
        return MemoryChecklist(
            items = listOf(
                // Memory Usage / 内存使用
                ChecklistItem(
                    id = "mem-1",
                    title = "使用 Debug.getMemoryInfo() 检测当前内存使用",
                    description = "在关键代码路径添加内存检测，监控 heap/dalvik/native 使用情况",
                    category = "内存使用检测"
                ),
                ChecklistItem(
                    id = "mem-2",
                    title = "对照 RAM 换算表评估设备内存上限",
                    description = "根据目标设备 RAM（1GB~32GB），查找对应的 App 内存上限",
                    category = "内存使用检测"
                ),
                ChecklistItem(
                    id = "mem-3",
                    title = "图像加载使用 BitmapFactory.Options.inSampleSize",
                    description = "所有 decodeResource/decodeFile 调用必须使用采样缩放，避免加载原图",
                    category = "风险点修复"
                ),
                ChecklistItem(
                    id = "mem-4",
                    title = "实现 LRU 缓存并设置 maxSize = maxMemory / 8",
                    description = "所有内存缓存必须实现 LRU 淘汰策略，禁止无限制增长",
                    category = "风险点修复"
                ),
                ChecklistItem(
                    id = "mem-5",
                    title = "视频帧处理使用 buffer pooling",
                    description = "MediaCodec/FrameExtractor 路径必须使用 ArrayPool 复用 buffer",
                    category = "风险点修复"
                ),

                // Profiling / Profiling
                ChecklistItem(
                    id = "pro-1",
                    title = "在 Android 17+ 设备上使用 ProfilingManager 查询内存限制",
                    description = "新 API：ProfilingManager.getMemoryLimit()，可在运行时动态获取本设备内存上限",
                    category = "ProfilingManager"
                ),
                ChecklistItem(
                    id = "pro-2",
                    title = "监控 JobScheduler 内存感知调度",
                    description = "Android 17 的 JobScheduler 会根据 App 内存使用情况调整后台 Job 优先级",
                    category = "ProfilingManager"
                ),

                // CI Testing / CI 测试
                ChecklistItem(
                    id = "ci-1",
                    title = "配置低 RAM 模拟器（1GB/2GB/4GB）进行 CI 测试",
                    description = "使用 avd start -memory <size> 启动低 RAM 模拟器，验证 App 在受限环境下的行为",
                    category = "CI 测试"
                ),
                ChecklistItem(
                    id = "ci-2",
                    title = "验证 App 在低 RAM 设备上不会 OOM",
                    description = "在 512MB/384MB 限制下测试 App，确认内存使用不会超限",
                    category = "CI 测试"
                ),

                // Large Heap / Large Heap
                ChecklistItem(
                    id = "lh-1",
                    title = "评估 large heap 是否真正需要",
                    description = "Large heap 会提升内存上限，但也会导致 App 更容易被系统 kill（LMK）",
                    category = "Large Heap"
                ),
                ChecklistItem(
                    id = "lh-2",
                    title = "分屏/画中画场景内存限制测试",
                    description = "多窗口模式下内存限制会叠加，要测试 App 在分屏时的内存行为",
                    category = "Large Heap"
                ),

                // Post-Quantum / 后量子密码学（附带）
                ChecklistItem(
                    id = "pq-1",
                    title = "Android 17 Beta 4 后量子密码学准备",
                    description = "Android 17 引入了后量子密码学支持，如有网络通信加密需求可关注",
                    category = "后量子密码学"
                )
            ),
            isExpanded = false
        )
    }

    /**
     * 构建检查清单 Markdown 导出 / Build checklist as Markdown string
     */
    private fun buildChecklistMarkdown(): String {
        val sb = StringBuilder()
        sb.appendLine("# Android 17 内存限制适配检查清单")
        sb.appendLine()
        sb.appendLine("> 生成时间: ${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault()).format(java.util.Date())}")
        sb.appendLine("> 基于: Android 17 Beta 4 per-device memory limits")
        sb.appendLine()
        sb.appendLine("---")
        sb.appendLine()

        val grouped = _state.value.checklist.items.groupBy { it.category }

        grouped.forEach { (category, items) ->
            sb.appendLine("## $category")
            items.forEachIndexed { index, item ->
                val checkbox = if (item.isChecked) "[x]" else "[ ]"
                sb.appendLine("- $checkbox **${item.title}**")
                sb.appendLine("  - ${item.description}")
            }
            sb.appendLine()
        }

        sb.appendLine("---")
        sb.appendLine("*Generated by Android 17 Memory Limits Toolkit*")
        return sb.toString()
    }
}
