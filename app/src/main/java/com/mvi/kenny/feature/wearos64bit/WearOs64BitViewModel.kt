package com.mvi.kenny.feature.wearos64bit

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
 * WearOs64BitViewModel — Wear OS 64位合规工具状态管理
 * ============================================================
 * Inherits ViewModel, holds WearOs64BitState and WearOs64BitEffect.
 *
 * State Management:
 * - _state: Private MutableStateFlow, written internally by ViewModel
 * - state: Public StateFlow, for UI layer subscription (collectAsState)
 *
 * Effect Management:
 * - _effect: Channel (hot flow), buffer size BUFFERED
 * - effect: receiveAsFlow, UI layer listens via collect{}
 *
 * PRD-061 / Wear OS 64-Bit Compliance Tool
 * Google Deadline: 2026-09-15
 *
 * @see WearOs64BitContract State/Intent/Effect definitions
 * @see WearOs64BitScreen Main screen
 */
class WearOs64BitViewModel : ViewModel() {

    // =============================================================
    // State
    // =============================================================
    /** Page state (StateFlow, UI read-only) / 页面状态 */
    private val _state = MutableStateFlow(WearOs64BitState.Initial)
    val state: StateFlow<WearOs64BitState> = _state.asStateFlow()

    /** Current state snapshot / 当前状态快照 */
    val currentState: WearOs64BitState get() = _state.value

    // =============================================================
    // Effect
    // =============================================================
    /** Effect Channel / 副作用通道 */
    private val _effect = Channel<WearOs64BitEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    init {
        // Initialize with mock scan history / 用模拟数据初始化扫描历史
        loadMockScanHistory()
        loadMockSdkDatabase()
        loadMockMigrationProjects()
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
    fun sendIntent(intent: WearOs64BitIntent) {
        when (intent) {
            is WearOs64BitIntent.SwitchTab -> switchTab(intent.tab)
            is WearOs64BitIntent.SwitchHomeSubTab -> switchHomeSubTab(intent.subTab)
            is WearOs64BitIntent.UploadApk -> uploadApk(intent.uri)
            is WearOs64BitIntent.StartApkScan -> startApkScan(intent.uri)
            is WearOs64BitIntent.SelectSoDetail -> selectSoDetail(intent.library)
            is WearOs64BitIntent.ClearSoDetail -> clearSoDetail()
            is WearOs64BitIntent.SearchSdk -> searchSdk(intent.query)
            is WearOs64BitIntent.ToggleModuleSelection -> toggleModuleSelection(intent.moduleName)
            is WearOs64BitIntent.StartProjectScan -> startProjectScan(intent.projectPath)
            is WearOs64BitIntent.ToggleFixSoSelection -> toggleFixSoSelection(intent.soName)
            is WearOs64BitIntent.SetFixStrategy -> setFixStrategy(intent.strategy)
            is WearOs64BitIntent.StartFixWizard -> startFixWizard()
            is WearOs64BitIntent.NextWizardStep -> nextWizardStep()
            is WearOs64BitIntent.PrevWizardStep -> prevWizardStep()
            is WearOs64BitIntent.ExecuteFix -> executeFix()
            is WearOs64BitIntent.ResetWizard -> resetWizard()
            is WearOs64BitIntent.FilterTracker -> filterTracker(intent.filter)
            is WearOs64BitIntent.ExportReport -> exportReport(intent.isJson)
            is WearOs64BitIntent.DismissError -> dismissError()
        }
    }

    // =============================================================
    // Tab Navigation
    // =============================================================
    /**
     * Switch bottom navigation tab / 切换底部导航 Tab
     *
     * @param tab Target tab / 目标 Tab
     */
    private fun switchTab(tab: BottomTab) {
        _state.value = _state.value.copy(activeTab = tab)
    }

    /**
     * Switch home sub-tab (APK scan / Project scan) / 切换首页子 Tab
     *
     * @param subTab Target sub-tab / 目标子 Tab
     */
    private fun switchHomeSubTab(subTab: HomeSubTab) {
        _state.value = _state.value.copy(
            activeHomeTab = subTab,
            homeState = _state.value.homeState.copy(
                uploadStatus = UploadStatus.IDLE,
                apkInfo = null,
                soLibraries = emptyList(),
                complianceResult = null,
                parseProgress = 0f,
                projectModules = emptyList()
            )
        )
    }

    // =============================================================
    // APK Upload & Scan
    // =============================================================
    /**
     * Upload APK file / 上传 APK 文件
     *
     * @param uri APK file URI / APK 文件 URI
     */
    private fun uploadApk(uri: android.net.Uri) {
        viewModelScope.launch {
            _state.value = _state.value.copy(
                isLoading = true,
                error = null
            )

            try {
                // Simulate file access delay
                delay(500)

                _state.value = _state.value.copy(
                    isLoading = false,
                    homeState = _state.value.homeState.copy(uploadStatus = UploadStatus.UPLOADING)
                )
                _effect.send(WearOs64BitEffect.ShowToast("APK selected: $uri / APK 已选择"))

            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = "Upload failed: ${e.message}"
                )
                _effect.send(WearOs64BitEffect.ShowError("上传失败: ${e.message}"))
            }
        }
    }

    /**
     * Start APK scan and analysis / 开始 APK 扫描和分析
     *
     * Simulates the full APK analysis flow:
     * 1. Parse APK to extract native libraries
     * 2. Analyze ABI distribution
     * 3. Generate compliance report
     *
     * Real implementation would:
     * - Use AAPT2/Apktool to parse AndroidManifest.xml
     * - Use ZipFile to read lib/ directory entries
     * - Parse ELF headers to detect actual ABI support
     *
     * @param uri APK file URI / APK 文件 URI
     */
    private fun startApkScan(uri: android.net.Uri) {
        viewModelScope.launch {
            _state.value = _state.value.copy(
                isLoading = true,
                error = null,
                homeState = _state.value.homeState.copy(
                    uploadStatus = UploadStatus.PROCESSING,
                    parseProgress = 0f
                )
            )

            try {
                // Simulate multi-stage APK parsing with progress updates
                // 模拟多阶段 APK 解析，逐步更新进度
                delay(300)
                _state.value = _state.value.copy(
                    homeState = _state.value.homeState.copy(parseProgress = 0.1f)
                )
                delay(400)
                _state.value = _state.value.copy(
                    homeState = _state.value.homeState.copy(parseProgress = 0.3f)
                )
                delay(500)
                _state.value = _state.value.copy(
                    homeState = _state.value.homeState.copy(parseProgress = 0.6f)
                )
                delay(600)
                _state.value = _state.value.copy(
                    homeState = _state.value.homeState.copy(parseProgress = 0.9f)
                )
                delay(200)

                // Generate mock data / 生成模拟数据
                val apkInfo = generateMockApkInfo()
                val soLibraries = generateMockSoLibraries()
                val complianceResult = generateComplianceResult(apkInfo, soLibraries)

                _state.value = _state.value.copy(
                    isLoading = false,
                    homeState = _state.value.homeState.copy(
                        uploadStatus = UploadStatus.DONE,
                        parseProgress = 1f,
                        apkInfo = apkInfo,
                        soLibraries = soLibraries,
                        complianceResult = complianceResult,
                        overallScore = complianceResult.score,
                        overallGrade = complianceResult.grade
                    )
                )

                _effect.send(WearOs64BitEffect.ShowToast("Scan complete: ${soLibraries.size} .so files found / 扫描完成：发现 ${soLibraries.size} 个 .so 文件"))
                _effect.send(WearOs64BitEffect.ScrollToTop)

            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    homeState = _state.value.homeState.copy(uploadStatus = UploadStatus.ERROR),
                    error = "Scan failed: ${e.message}"
                )
                _effect.send(WearOs64BitEffect.ShowError("扫描失败: ${e.message}"))
            }
        }
    }

    /**
     * Generate mock APK info / 生成模拟 APK 信息
     *
     * @return Mock ApkInfo / 模拟 APK 信息
     */
    private fun generateMockApkInfo(): ApkInfo {
        return ApkInfo(
            packageName = "com.example.wearos.app",
            versionName = "3.1.2",
            versionCode = 3120,
            minSdkVersion = 25,
            targetSdkVersion = 35,
            soCount = 8,
            fileSize = 25_000_000L,
            architectures = listOf(Abis.ARM64_V8A, Abis.ARMEABI_V7A, Abis.X86_64)
        )
    }

    /**
     * Generate mock .so library list / 生成模拟 .so 库列表
     *
     * Real implementation would parse APK lib/ directory and ELF headers.
     *
     * @return List of mock SoLibrary / 模拟 .so 库列表
     */
    private fun generateMockSoLibraries(): List<SoLibrary> {
        val mockData = listOf(
            Quadruple("libnative-lib.so", "com.example:app", true, false),
            Quadruple("libflutter.so", "flutter:flutter", true, false),
            Quadruple("libhermes.so", "com.facebook:hermes", true, false),
            Quadruple("libwearable.so", "com.google.android.wearable:wearable", true, true),
            Quadruple("libstripe-wear.so", "com.stripe:stripe-android", false, true),
            Quadruple("libopencv_java4.so", "opencv:opencv", true, false),
            Quadruple("libagora_rtc.so", "io.agora:agora-rtc-sdk", false, true),
            Quadruple("libunity.so", "com.unity3d:unity", true, false)
        )

        return mockData.map { (name, source, has64Bit, isThirdParty) ->
            val supportedAbis = if (has64Bit) {
                listOf(Abis.ARM64_V8A, Abis.ARMEABI_V7A)
            } else {
                listOf(Abis.ARMEABI_V7A)
            }

            SoLibrary(
                name = name,
                source = source,
                supportedAbis = supportedAbis,
                has64Bit = has64Bit,
                isFromThirdParty = isThirdParty,
                size = Random.nextLong(500_000, 15_000_000),
                recommendedFix = if (!has64Bit) {
                    if (isThirdParty) FixStrategy.REPLACE_SDK else FixStrategy.RECOMPILE
                } else FixStrategy.RECOMPILE
            )
        }
    }

    /**
     * Generate compliance result from APK info and .so list / 根据 APK 信息和 .so 列表生成合规结果
     *
     * @param apkInfo APK information / APK 信息
     * @param soLibraries List of .so libraries / .so 库列表
     * @return Compliance result / 合规结果
     */
    private fun generateComplianceResult(apkInfo: ApkInfo, soLibraries: List<SoLibrary>): ComplianceResult {
        val totalSo = soLibraries.size
        val soWith64Bit = soLibraries.count { it.has64Bit }
        val passRate = if (totalSo > 0) soWith64Bit.toFloat() / totalSo else 1f

        val missingAbis = soLibraries
            .filter { !it.has64Bit }
            .flatMap { it.supportedAbis }
            .distinct()

        val score = (passRate * 100).toInt().coerceIn(0, 100)

        val grade = when {
            score >= 90 -> ComplianceGrade.COMPLIANT
            score >= 60 -> ComplianceGrade.PARTIAL
            else -> ComplianceGrade.NON_COMPLIANT
        }

        val summary = when (grade) {
            ComplianceGrade.COMPLIANT -> "APK includes 64-bit support for all .so files. Compliant with Wear OS 64-bit requirement."
            ComplianceGrade.PARTIAL -> "${totalSo - soWith64Bit} of $totalSo .so files missing 64-bit variants. Partial compliance."
            ComplianceGrade.NON_COMPLIANT -> "Majority of .so files missing 64-bit support. Non-compliant with Wear OS 64-bit enforcement."
        }

        return ComplianceResult(
            grade = grade,
            score = score,
            pass = grade == ComplianceGrade.COMPLIANT,
            missing64BitAbis = missingAbis,
            summary = summary
        )
    }

    // =============================================================
    // .so Detail
    // =============================================================
    /**
     * Select .so to view detail / 选择查看 .so 详情
     *
     * @param library .so library info / .so 库信息
     */
    private fun selectSoDetail(library: SoLibrary) {
        _state.value = _state.value.copy(
            homeState = _state.value.homeState.copy(selectedSoDetail = library)
        )
        viewModelScope.launch {
            _effect.send(WearOs64BitEffect.NavigateToSoDetail(library))
        }
    }

    /**
     * Clear selected .so detail / 清除选中的 .so 详情
     */
    private fun clearSoDetail() {
        _state.value = _state.value.copy(
            homeState = _state.value.homeState.copy(selectedSoDetail = null)
        )
    }

    // =============================================================
    // SDK Query
    // =============================================================
    /**
     * Load mock SDK database / 加载模拟 SDK 数据库
     */
    private fun loadMockSdkDatabase() {
        val mockSdks = listOf(
            SdkInfo("Stripe", "Stripe", SdkSupportStatus.SUPPORTED, "20.5.0", "20.5.0+", "Full 64-bit support since v20.5.0"),
            SdkInfo("Stripe", "Stripe", SdkSupportStatus.NOT_SUPPORTED, "19.x.x", "20.5.0+", "Upgrade required before 2026-09-15"),
            SdkInfo("Firebase", "Google", SdkSupportStatus.SUPPORTED, "33.0.0+", "33.0.0+", "All Firebase SDKs support 64-bit"),
            SdkInfo("Bugsnag", "Bugsnag", SdkSupportStatus.SUPPORTED, "6.4.0", "6.4.0+", "Full 64-bit support"),
            SdkInfo("React Native", "Meta", SdkSupportStatus.SUPPORTED, "0.76.0+", "0.76.0+", "New Architecture supports 64-bit"),
            SdkInfo("Unity", "Unity", SdkSupportStatus.SUPPORTED, "2023.2+", "2023.2+", "IL2CPP with full 64-bit support"),
            SdkInfo("Agora RTC", "Agora", SdkSupportStatus.PARTIAL, "2.9.0", "3.0.0+", "Partial 64-bit support, upgrade recommended"),
            SdkInfo("SQLCipher", "Zetetic", SdkSupportStatus.SUPPORTED, "4.5.6+", "4.5.6+", "64-bit supported with NDK r26+"),
            SdkInfo("TensorFlow Lite", "Google", SdkSupportStatus.SUPPORTED, "2.14.0+", "2.14.0+", "Native library rebuilt with 64-bit"),
            SdkInfo("OpenCV", "OpenCV", SdkSupportStatus.SUPPORTED, "4.9.0+", "4.9.0+", "Rebuilt with NDK r26"),
            SdkInfo("PayPal", "PayPal", SdkSupportStatus.PARTIAL, "3.0.0", "3.2.0+", "Some 64-bit variants missing"),
            SdkInfo("Tencent Bugly", "Tencent", SdkSupportStatus.NOT_SUPPORTED, "2.0.x", "Unknown", "No 64-bit variant available")
        )

        _state.value = _state.value.copy(
            sdkQueryState = _state.value.sdkQueryState.copy(
                sdkList = mockSdks,
                filteredSdkList = mockSdks
            )
        )
    }

    /**
     * Search SDK compatibility / 搜索 SDK 兼容性
     *
     * @param query Search query / 搜索查询
     */
    private fun searchSdk(query: String) {
        _state.value = _state.value.copy(
            sdkQueryState = _state.value.sdkQueryState.copy(searchQuery = query)
        )

        if (query.length < 1) {
            _state.value = _state.value.copy(
                sdkQueryState = _state.value.sdkQueryState.copy(
                    filteredSdkList = _state.value.sdkQueryState.sdkList
                )
            )
            return
        }

        viewModelScope.launch {
            delay(200)

            val filtered = _state.value.sdkQueryState.sdkList.filter {
                it.name.contains(query, ignoreCase = true) ||
                it.vendor.contains(query, ignoreCase = true)
            }.distinctBy { it.name }

            _state.value = _state.value.copy(
                sdkQueryState = _state.value.sdkQueryState.copy(filteredSdkList = filtered)
            )
        }
    }

    // =============================================================
    // Project Scan
    // =============================================================
    /**
     * Toggle module selection for project scan / 切换项目模块选择状态
     *
     * @param moduleName Module name / 模块名称
     */
    private fun toggleModuleSelection(moduleName: String) {
        val updatedModules = _state.value.homeState.projectModules.map { module ->
            if (module.name == moduleName) {
                module.copy(isSelected = !module.isSelected)
            } else module
        }
        _state.value = _state.value.copy(
            homeState = _state.value.homeState.copy(projectModules = updatedModules)
        )
    }

    /**
     * Start project scan / 开始项目扫描
     *
     * @param projectPath Project path / 项目路径
     */
    private fun startProjectScan(projectPath: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(
                isLoading = true,
                error = null,
                homeState = _state.value.homeState.copy(
                    projectScanActive = true,
                    parseProgress = 0f
                )
            )

            try {
                delay(500)
                _state.value = _state.value.copy(
                    homeState = _state.value.homeState.copy(parseProgress = 0.3f)
                )
                delay(600)
                _state.value = _state.value.copy(
                    homeState = _state.value.homeState.copy(parseProgress = 0.7f)
                )
                delay(400)

                // Generate mock project modules / 生成模拟项目模块
                val mockModules = listOf(
                    ProjectModule(":app", generateMockSoLibraries().take(3), 0.67f),
                    ProjectModule(":library:core", generateMockSoLibraries().take(2), 1.0f),
                    ProjectModule(":library:ui", generateMockSoLibraries().take(1), 0.5f),
                    ProjectModule(":wear:module", generateMockSoLibraries().take(4), 0.25f)
                )

                _state.value = _state.value.copy(
                    isLoading = false,
                    homeState = _state.value.homeState.copy(
                        parseProgress = 1f,
                        projectModules = mockModules
                    )
                )

                _effect.send(WearOs64BitEffect.ShowToast("Project scan complete: ${mockModules.size} modules analyzed / 项目扫描完成：分析了 ${mockModules.size} 个模块"))
                _effect.send(WearOs64BitEffect.ScrollToTop)

            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = "Project scan failed: ${e.message}"
                )
                _effect.send(WearOs64BitEffect.ShowError("项目扫描失败: ${e.message}"))
            }
        }
    }

    // =============================================================
    // Fix Wizard
    // =============================================================
    /**
     * Toggle .so selection in fix wizard / 在修复向导中切换 .so 选择状态
     *
     * @param soName .so file name / .so 文件名
     */
    private fun toggleFixSoSelection(soName: String) {
        val current = _state.value.homeState.selectedLibrariesForFix
        val updated = if (soName in current) {
            current - soName
        } else {
            current + soName
        }
        _state.value = _state.value.copy(
            homeState = _state.value.homeState.copy(selectedLibrariesForFix = updated)
        )
    }

    /**
     * Set fix strategy / 设置修复策略
     *
     * @param strategy Fix strategy / 修复策略
     */
    private fun setFixStrategy(strategy: FixStrategy) {
        _state.value = _state.value.copy(
            homeState = _state.value.homeState.copy(selectedFixStrategy = strategy)
        )
    }

    /**
     * Start fix wizard / 开始修复向导
     */
    private fun startFixWizard() {
        _state.value = _state.value.copy(
            homeState = _state.value.homeState.copy(
                wizardStep = FixWizardStep.SELECT,
                wizardLog = emptyList(),
                wizardComplete = false,
                selectedLibrariesForFix = emptySet()
            )
        )
    }

    /**
     * Next wizard step / 下一步
     */
    private fun nextWizardStep() {
        val currentIndex = _state.value.homeState.wizardStep.index
        if (currentIndex < FixWizardStep.entries.size - 1) {
            val nextStep = FixWizardStep.entries[currentIndex + 1]
            _state.value = _state.value.copy(
                homeState = _state.value.homeState.copy(wizardStep = nextStep)
            )
            viewModelScope.launch {
                _effect.send(WearOs64BitEffect.ScrollToTop)
            }
        }
    }

    /**
     * Previous wizard step / 上一步
     */
    private fun prevWizardStep() {
        val currentIndex = _state.value.homeState.wizardStep.index
        if (currentIndex > 0) {
            val prevStep = FixWizardStep.entries[currentIndex - 1]
            _state.value = _state.value.copy(
                homeState = _state.value.homeState.copy(wizardStep = prevStep)
            )
            viewModelScope.launch {
                _effect.send(WearOs64BitEffect.ScrollToTop)
            }
        }
    }

    /**
     * Execute fix / 执行修复
     */
    private fun executeFix() {
        viewModelScope.launch {
            _state.value = _state.value.copy(
                isLoading = true,
                homeState = _state.value.homeState.copy(wizardLog = emptyList())
            )

            try {
                val logs = mutableListOf<String>()
                val selectedLibs = _state.value.homeState.selectedLibrariesForFix

                logs.add("[1/4] Analyzing ${selectedLibs.size} selected .so files...")
                _state.value = _state.value.copy(
                    homeState = _state.value.homeState.copy(wizardLog = logs.toList())
                )
                delay(600)

                logs.add("[2/4] Determining NDK requirements (min: NDK r26)...")
                _state.value = _state.value.copy(
                    homeState = _state.value.homeState.copy(wizardLog = logs.toList())
                )
                delay(500)

                logs.add("[3/4] Generating Gradle configuration changes...")
                _state.value = _state.value.copy(
                    homeState = _state.value.homeState.copy(wizardLog = logs.toList())
                )
                delay(700)

                logs.add("[4/4] Verifying build configuration...")
                _state.value = _state.value.copy(
                    homeState = _state.value.homeState.copy(wizardLog = logs.toList())
                )
                delay(400)

                logs.add("✅ Fix completed. Run ./gradlew :app:assemble to rebuild with 64-bit support.")
                _state.value = _state.value.copy(
                    isLoading = false,
                    homeState = _state.value.homeState.copy(
                        wizardLog = logs.toList(),
                        wizardComplete = true
                    )
                )

                _effect.send(WearOs64BitEffect.ShowToast("Fix completed / 修复完成"))
                _effect.send(WearOs64BitEffect.ScrollToTop)

            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = "Fix execution failed: ${e.message}"
                )
                _effect.send(WearOs64BitEffect.ShowError("修复执行失败: ${e.message}"))
            }
        }
    }

    /**
     * Reset wizard / 重置向导
     */
    private fun resetWizard() {
        _state.value = _state.value.copy(
            homeState = _state.value.homeState.copy(
                wizardStep = FixWizardStep.SELECT,
                selectedLibrariesForFix = emptySet(),
                wizardLog = emptyList(),
                wizardComplete = false
            )
        )
    }

    // =============================================================
    // Migration Tracker
    // =============================================================
    /**
     * Load mock migration projects / 加载模拟迁移项目
     */
    private fun loadMockMigrationProjects() {
        val mockProjects = listOf(
            MigrationProject("MyWearApp", 4, 1, MigrationStatus.IN_PROGRESS, System.currentTimeMillis() - 86400000),
            MigrationProject("HealthTracker", 6, 6, MigrationStatus.COMPLETED, System.currentTimeMillis() - 172800000),
            MigrationProject("WatchFaceApp", 3, 0, MigrationStatus.NOT_STARTED, 0),
            MigrationProject("FitnessApp", 5, 2, MigrationStatus.IN_PROGRESS, System.currentTimeMillis() - 3600000)
        )
        _state.value = _state.value.copy(
            trackerState = _state.value.trackerState.copy(projects = mockProjects)
        )
    }

    /**
     * Filter tracker list / 筛选进度列表
     *
     * @param filter Filter option / 筛选选项
     */
    private fun filterTracker(filter: TrackerFilter) {
        _state.value = _state.value.copy(
            trackerState = _state.value.trackerState.copy(filter = filter)
        )
    }

    /**
     * Get filtered projects / 获取过滤后的项目列表
     */
    val filteredProjects: List<MigrationProject>
        get() {
            val filter = _state.value.trackerState.filter
            val projects = _state.value.trackerState.projects
            return when (filter) {
                TrackerFilter.ALL -> projects
                TrackerFilter.COMPLIANT -> projects.filter { it.status == MigrationStatus.COMPLETED }
                TrackerFilter.IN_PROGRESS -> projects.filter { it.status == MigrationStatus.IN_PROGRESS }
                TrackerFilter.NOT_STARTED -> projects.filter { it.status == MigrationStatus.NOT_STARTED }
            }
        }

    // =============================================================
    // Mock Data Helpers
    // =============================================================
    /**
     * Load mock scan history / 加载模拟扫描历史
     */
    private fun loadMockScanHistory() {
        val mockRecords = listOf(
            ScanRecord(
                id = "scan-001",
                apkName = "com.example.wearapp",
                scanTime = System.currentTimeMillis() - 3600000,
                result = ComplianceResult(
                    grade = ComplianceGrade.PARTIAL,
                    score = 67,
                    pass = false,
                    missing64BitAbis = listOf(Abis.ARMEABI_V7A),
                    summary = "3 of 8 .so files missing 64-bit variants"
                )
            ),
            ScanRecord(
                id = "scan-002",
                apkName = "com.google.android.wearable.health",
                scanTime = System.currentTimeMillis() - 86400000,
                result = ComplianceResult(
                    grade = ComplianceGrade.COMPLIANT,
                    score = 100,
                    pass = true,
                    missing64BitAbis = emptyList(),
                    summary = "All .so files include 64-bit variants"
                )
            )
        )
        _state.value = _state.value.copy(
            homeState = _state.value.homeState.copy(
                recentScans = mockRecords,
                overallScore = 67,
                overallGrade = ComplianceGrade.PARTIAL
            )
        )
    }

    // =============================================================
    // Export
    // =============================================================
    /**
     * Export compliance report / 导出合规报告
     *
     * @param isJson Export as JSON (false = Markdown) / 导出为 JSON
     */
    private fun exportReport(isJson: Boolean) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            try {
                delay(1000)

                val extension = if (isJson) "json" else "md"
                val filePath = "/storage/emulated/0/Download/wearos_64bit_report.$extension"

                _state.value = _state.value.copy(isLoading = false)
                _effect.send(WearOs64BitEffect.ShowExportSuccess(filePath))
                _effect.send(WearOs64BitEffect.ShowToast("Report exported to Downloads / 报告已导出到下载目录"))
            } catch (e: Exception) {
                _state.value = _state.value.copy(isLoading = false)
                _effect.send(WearOs64BitEffect.ShowError("Export failed: ${e.message}"))
            }
        }
    }

    // =============================================================
    // Error Handling
    // =============================================================
    /**
     * Dismiss error message / 关闭错误信息
     */
    private fun dismissError() {
        _state.value = _state.value.copy(error = null)
    }
}

/**
 * Simple data class for mock .so generation / 用于模拟 .so 生成的简单数据类
 */
private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
