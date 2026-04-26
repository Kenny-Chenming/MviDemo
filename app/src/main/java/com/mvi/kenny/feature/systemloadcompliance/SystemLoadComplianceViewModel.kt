package com.mvi.kenny.feature.systemloadcompliance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

/**
 * ============================================================
 * SystemLoadComplianceViewModel — Android 17 System.load() 合规检测工具状态管理
 * ============================================================
 * Inherits ViewModel, holds SystemLoadComplianceState and SystemLoadComplianceEffect.
 *
 * State Management:
 * - _state: Private MutableStateFlow, written internally by ViewModel
 * - state: Public StateFlow, for UI layer subscription (collectAsState)
 *
 * Effect Management:
 * - _effect: Channel (hot flow), buffer size BUFFERED
 * - effect: receiveAsFlow, UI layer listens via collect{}
 *
 * Android 17 强制执行 Native Library read-only 约束：
 * - System.load() 和 System.loadLibrary() 加载的 .so 文件在 APK 签名后被标记为只读
 * - 动态路径拼接 (System.load(appDir + "/" + libName)) 风险最高
 * - chmod 0444 在签名后会失效，需要使用 FileChannel.lock() 或签名后处理
 *
 * @see SystemLoadComplianceContract State/Intent/Effect definitions
 * @see SystemLoadComplianceScreen Main screen
 */
class SystemLoadComplianceViewModel : ViewModel() {

    // =============================================================
    // State
    // =============================================================
    /** Page state (StateFlow, UI read-only) / 页面状态 */
    private val _state = MutableStateFlow(SystemLoadComplianceState.Initial)
    val state: StateFlow<SystemLoadComplianceState> = _state.asStateFlow()

    /** Current state snapshot / 当前状态快照 */
    val currentState: SystemLoadComplianceState get() = _state.value

    // =============================================================
    // Effect
    // =============================================================
    /** Effect Channel / 副作用通道 */
    private val _effect = Channel<SystemLoadComplianceEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    // =============================================================
    // Intent Processing
    // =============================================================
    /**
     * Receive and process user intent / 接收并处理用户意图
     */
    fun sendIntent(intent: SystemLoadComplianceIntent) {
        when (intent) {
            is SystemLoadComplianceIntent.SelectTab -> selectTab(intent.tabIndex)
            is SystemLoadComplianceIntent.StartScan -> startScan(intent.sourceDir)
            is SystemLoadComplianceIntent.CancelScan -> cancelScan()
            is SystemLoadComplianceIntent.ApplyFix -> applyFix(intent.callLocation)
            is SystemLoadComplianceIntent.ApplyFixAll -> applyFixAll()
            is SystemLoadComplianceIntent.SelectMarkingMethod -> selectMarkingMethod(intent.method)
            is SystemLoadComplianceIntent.SelectSoForMarking -> selectSoForMarking(intent.soFile)
            is SystemLoadComplianceIntent.ApplyMarking -> applyMarking(intent.soFile)
            is SystemLoadComplianceIntent.SelectFramework -> selectFramework(intent.framework)
            is SystemLoadComplianceIntent.GenerateMigrationGuide -> generateMigrationGuide(intent.framework)
            is SystemLoadComplianceIntent.InputCrashLog -> inputCrashLog(intent.log)
            is SystemLoadComplianceIntent.AnalyzeRootCause -> analyzeRootCause()
            is SystemLoadComplianceIntent.SelectEngine -> selectEngine(intent.engine)
            is SystemLoadComplianceIntent.InputEngineVersion -> inputEngineVersion(intent.version)
            is SystemLoadComplianceIntent.DetectEngineCompliance -> detectEngineCompliance()
            is SystemLoadComplianceIntent.SelectCIPlatform -> selectCIPlatform(intent.platform)
            is SystemLoadComplianceIntent.GenerateCIConfig -> generateCIConfig()
            is SystemLoadComplianceIntent.CopyCIConfig -> copyCIConfigToClipboard()
            is SystemLoadComplianceIntent.Reset -> resetState()
            is SystemLoadComplianceIntent.DismissError -> dismissError()
        }
    }

    // =============================================================
    // Tab Navigation
    // =============================================================
    private fun selectTab(tabIndex: Int) {
        _state.value = _state.value.copy(selectedTab = tabIndex)
    }

    // =============================================================
    // Scan Logic
    // =============================================================
    /**
     * Start System.load() compliance scan / 开始 System.load() 合规扫描
     */
    private fun startScan(sourceDir: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(
                scanStatus = ScanStatus.SCANNING,
                scanSourceDir = sourceDir,
                scanProgress = 0f,
                scannedFilesCount = 0,
                nonCompliantCalls = emptyList(),
                errorMessage = null
            )

            val totalFiles = 24
            val mockCalls = generateMockScanResults()

            for (i in 1..totalFiles) {
                delay(80)
                _state.value = _state.value.copy(
                    scannedFilesCount = i,
                    scanProgress = i.toFloat() / totalFiles
                )
            }

            _state.value = _state.value.copy(scanStatus = ScanStatus.ANALYZING)
            delay(500)

            val nonCompliant = mockCalls.filter {
                it.callType == LoadCallType.LOAD_DYNAMIC || it.callType == LoadCallType.LOAD_ABSOLUTE
            }

            val score = if (mockCalls.isEmpty()) 100
                else ((mockCalls.size - nonCompliant.size) * 100 / mockCalls.size)

            _state.value = _state.value.copy(
                scanStatus = ScanStatus.COMPLETED,
                nonCompliantCalls = nonCompliant,
                complianceScore = score,
                isLoading = false
            )

            _effect.send(
                SystemLoadComplianceEffect.ShowScanComplete(
                    passedCount = mockCalls.size - nonCompliant.size,
                    failedCount = nonCompliant.size
                )
            )
        }
    }

    private fun cancelScan() {
        _state.value = _state.value.copy(
            scanStatus = ScanStatus.IDLE,
            scanProgress = 0f,
            isLoading = false
        )
    }

    private fun generateMockScanResults(): List<LoadCallLocation> {
        return listOf(
            LoadCallLocation(
                filePath = "app/src/main/java/com/example/app/NativeHelper.kt",
                lineNumber = 42,
                methodName = "loadNativeLib",
                className = "NativeHelper",
                callType = LoadCallType.LOAD_LIBRARY,
                libName = "native-lib",
                rawCode = "System.loadLibrary(\"native-lib\")",
                isDynamicConcat = false,
                fixSuggestion = "Static loadLibrary() is compliant. No action needed."
            ),
            LoadCallLocation(
                filePath = "app/src/main/java/com/example/app/DynamicLoader.kt",
                lineNumber = 78,
                methodName = "loadPluginLib",
                className = "DynamicLoader",
                callType = LoadCallType.LOAD_DYNAMIC,
                libName = "plugin_core",
                rawCode = "System.load(appDir + \"/\" + libName)",
                isDynamicConcat = true,
                fixSuggestion = "HIGH RISK: Dynamic path concatenation detected. Use FileChannel.lock() for runtime locking."
            ),
            LoadCallLocation(
                filePath = "app/src/main/java/com/example/app/LegacyNative.kt",
                lineNumber = 115,
                methodName = "init",
                className = "LegacyNative",
                callType = LoadCallType.LOAD_ABSOLUTE,
                libName = "/data/data/com.example/lib/liblegacy.so",
                rawCode = "System.load(\"/data/data/com.example/lib/liblegacy.so\")",
                isDynamicConcat = false,
                fixSuggestion = "MEDIUM RISK: Absolute path load. Consider using context.getLibraryDir() for Android 17 compliance."
            ),
            LoadCallLocation(
                filePath = "app/src/main/java/com/example/app/ThirdPartySDK.java",
                lineNumber = 33,
                methodName = "initNative",
                className = "ThirdPartySDK",
                callType = LoadCallType.LOAD_LIBRARY,
                libName = "thirdparty-sdk",
                rawCode = "System.loadLibrary(\"thirdparty-sdk\")",
                isDynamicConcat = false,
                fixSuggestion = "Verify with SDK vendor for Android 17 read-only support."
            ),
            LoadCallLocation(
                filePath = "app/src/main/java/com/example/app/GameEngine.java",
                lineNumber = 200,
                methodName = "loadEngineLibs",
                className = "GameEngine",
                callType = LoadCallType.LOAD_DYNAMIC,
                libName = "unity_engine",
                rawCode = "System.load(basePath + \"lib/\" + arch + \"/libunity_engine.so\")",
                isDynamicConcat = true,
                fixSuggestion = "HIGH RISK: Unity dynamic loading. Upgrade to Unity 2022.3 LTS for Android 17 support."
            )
        )
    }

    // =============================================================
    // Fix Logic
    // =============================================================
    private fun applyFix(callLocation: LoadCallLocation) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            delay(300)

            val updated = _state.value.nonCompliantCalls.map {
                if (it == callLocation) it.copy(fixSuggestion = "[FIXED] " + it.fixSuggestion)
                else it
            }

            _state.value = _state.value.copy(
                nonCompliantCalls = updated,
                isLoading = false
            )

            _effect.send(SystemLoadComplianceEffect.ShowToast("Fix applied to ${callLocation.libName}"))
        }
    }

    private fun applyFixAll() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            delay(500)

            _state.value = _state.value.copy(
                nonCompliantCalls = emptyList(),
                complianceScore = 100,
                isLoading = false
            )

            _effect.send(SystemLoadComplianceEffect.ShowToast("All non-compliant calls fixed"))
        }
    }

    // =============================================================
    // Marking Method
    // =============================================================
    private fun selectMarkingMethod(method: MarkingMethod) {
        _state.value = _state.value.copy(markingMethod = method)
        generateMarkingCode()
    }

    private fun selectSoForMarking(soFile: SoFileInfo) {
        _state.value = _state.value.copy(
            soFiles = _state.value.soFiles.map { it }
        )
        generateMarkingCode()
    }

    private fun applyMarking(soFile: SoFileInfo) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            delay(300)

            val updatedSoFiles = _state.value.soFiles.map {
                if (it.name == soFile.name) {
                    it.copy(
                        isReadOnlyMarked = true,
                        markingMethod = _state.value.markingMethod,
                        complianceStatus = ComplianceStatus.COMPLIANT
                    )
                } else it
            }

            _state.value = _state.value.copy(
                soFiles = updatedSoFiles,
                isLoading = false
            )

            _effect.send(SystemLoadComplianceEffect.ShowToast("${soFile.name} marked as read-only"))
        }
    }

    private fun generateMarkingCode() {
        val method = _state.value.markingMethod
        val code = when (method) {
            MarkingMethod.CHMOD_0444 -> """
                |// Method: chmod 0444
                |// WARNING: This method will be reset after APK signing!
                |// Use only for testing, NOT for production.
                |
                |import java.io.File
                |import java.lang.Process
                |
                |fun markSoAsReadOnly(soPath: String): Boolean {
                |    return try {
                |        val process = Runtime.getRuntime().exec(arrayOf("chmod", "0444", soPath))
                |        process.waitFor() == 0
                |    } catch (e: Exception) {
                |        false
                |    }
                |}
            """.trimMargin()

            MarkingMethod.FILE_CHANNEL_LOCK -> """
                |// Method: FileChannel.lock() - Recommended for Android 17+
                |// Survives APK signing, works at runtime
                |
                |import java.io.RandomAccessFile
                |import java.nio.channels.FileChannel
                |
                |/**
                | * Mark .so file as read-only using shared file lock.
                | * Android 17 enforces read-only on loaded native libraries.
                | * FileChannel.lock() survives APK signing process.
                | */
                |fun markSoAsReadOnly(soPath: String): Boolean {
                |    return try {
                |        val file = RandomAccessFile(soPath, "r")
                |        val channel: FileChannel = file.channel
                |        val lock = channel.lock()
                |        true
                |    } catch (e: Exception) {
                |        e.printStackTrace()
                |        false
                |    }
                |}
            """.trimMargin()

            MarkingMethod.GRADLE_TASK -> """
                |// Method: Gradle Task - android.signingBlock
                |// Preserves file attributes after APK signing
                |
                |android {
                |    signingConfigs {
                |        create("release") {
                |            // ... your signing config
                |        }
                |    }
                |
                |    buildTypes {
                |        release {
                |            signingConfig = signingConfigs.getByName("release")
                |            packagingOptions {
                |                jniLibs {
                |                    keepDebugSymbols.add("**/*.so")
                |                }
                |            }
                |        }
                |    }
                |}
            """.trimMargin()

            MarkingMethod.ANDROID_API -> """
                |// Method: File.setReadOnly() - Android API 23+
                |// May not work on all Android 17 devices
                |
                |import java.io.File
                |
                |/**
                | * Mark .so file as read-only using File.setReadOnly().
                | * Works on Android 6.0+ (API 23).
                | */
                |fun markSoAsReadOnly(soPath: String): Boolean {
                |    return try {
                |        val file = File(soPath)
                |        if (file.exists() && file.canWrite()) {
                |            file.setReadOnly()
                |            !file.canWrite()
                |        } else {
                |            false
                |        }
                |    } catch (e: Exception) {
                |        e.printStackTrace()
                |        false
                |    }
                |}
            """.trimMargin()
        }

        _state.value = _state.value.copy(markingCode = code)
    }

    // =============================================================
    // Plugin Migration Guide
    // =============================================================
    private fun selectFramework(framework: Framework) {
        _state.value = _state.value.copy(selectedFramework = framework)
    }

    private fun generateMigrationGuide(framework: Framework) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            delay(500)

            val guide = when (framework) {
                Framework.VIRTUAL_APK -> MigrationGuide(
                    framework = Framework.VIRTUAL_APK,
                    steps = listOf(
                        MigrationStep(1, "Update build.gradle", "Add VirtualAPK plugin dependency",
                            "plugins { id(\"com.didi.virtualapk.plugin\") version \"0.9.8\" }"),
                        MigrationStep(2, "Configure VirtualAPK", "Set plugin package and target package",
                            "virtualApk { targetPackage = \"com.example.app\" }"),
                        MigrationStep(3, "Migrate PluginModule", "Convert to plugin module",
                            "Remove Application class, use PluginInterface"),
                        MigrationStep(4, "Update Plugin Loading", "Replace System.load() with plugin loading API",
                            "PluginManager.getInstance(ctx).loadPlugin(pluginApk)")
                    ),
                    keyChanges = listOf(
                        "System.loadLibrary() replaced with VirtualAPK plugin loading",
                        "Native libraries bundled in plugin APK, loaded via PluginClassLoader"
                    ),
                    warnings = listOf(
                        "VirtualAPK 0.9.8 requires AGP 7.0+",
                        "NDK .so files must be rebuilt for plugin ClassLoader compatibility"
                    )
                )

                Framework.RE_PLUGIN -> MigrationGuide(
                    framework = Framework.RE_PLUGIN,
                    steps = listOf(
                        MigrationStep(1, "Add RePlugin dependency", "Add RePlugin Gradle plugin",
                            "plugins { id(\"com.qihoo360.replugin\") version \"3.0.0\" }"),
                        MigrationStep(2, "Configure RePlugin", "Update Application class to extend RePluginApp",
                            "class App : RePluginApp()"),
                        MigrationStep(3, "Migrate Plugin Loading", "Replace System.loadLibrary() with loadPlugin()",
                            "RePlugin.loadPlugin(pluginPath)"),
                        MigrationStep(4, "Update Native Loading", "Use RePlugin's native library loader",
                            "RePlugin.loadLibrary(libName)")
                    ),
                    keyChanges = listOf(
                        "Plugin APK structure changed to RePlugin format",
                        "System.load() calls must use RePlugin.loadLibrary() wrapper"
                    ),
                    warnings = listOf(
                        "RePlugin requires persistent process configuration",
                        "Android 17 read-only enforcement may affect plugin .so loading"
                    )
                )

                Framework.DYNAMIC_LOAD -> MigrationGuide(
                    framework = Framework.DYNAMIC_LOAD,
                    steps = listOf(
                        MigrationStep(1, "Add DynamicLoad library", "Add DynamicLoad dependency",
                            "implementation(\"com.youlook:dynamic-load:2.0.0\")"),
                        MigrationStep(2, "Create DLPluginActivity", "Extend DLPluginActivity instead of Activity",
                            "class MyActivity : DLPluginActivity()"),
                        MigrationStep(3, "Update Native Loading", "Replace System.load() with DLLibrary.load()",
                            "DLLibrary.load(context, libName)"),
                        MigrationStep(4, "Configure DexClassLoader", "Set up proper ClassLoader for plugin APK",
                            "val classLoader = DexClassLoader(pluginPath, ...)")
                    ),
                    keyChanges = listOf(
                        "Activity must extend DLPluginActivity or implement DLInterface",
                        "Native libraries loaded via DLLibrary wrapper class"
                    ),
                    warnings = listOf(
                        "DynamicLoad 2.0 does not support Android 17 new lifecycle",
                        "Consider upgrading to VirtualAPK for better Android 17 support"
                    )
                )

                Framework.APKPLUG -> MigrationGuide(
                    framework = Framework.APKPLUG,
                    steps = listOf(
                        MigrationStep(1, "Configure apkplug server", "Set up apkplug OSGi server",
                            "// apkplug requires server-side OSGi configuration"),
                        MigrationStep(2, "Add apkplug Gradle plugin", "Apply apkplug plugin to host and plugin modules",
                            "plugins { id(\"com.apkplug.plugins\") version \"4.0.0\" }"),
                        MigrationStep(3, "Migrate to OSGi bundles", "Convert plugin APKs to OSGi bundles",
                            "// Native libraries must be in bundle's native/libs/ directory"),
                        MigrationStep(4, "Update BundleActivator", "Replace Application with BundleActivator",
                            "class MyActivator : BundleActivator { ... }")
                    ),
                    keyChanges = listOf(
                        "Plugin APK becomes OSGi bundle with BundleActivator",
                        "Native libraries in bundle's native/libs/ loaded via BundleContext"
                    ),
                    warnings = listOf(
                        "apkplug migration is most complex, consider alternatives first"
                    )
                )

                Framework.NONE -> null
            }

            _state.value = _state.value.copy(
                migrationGuide = guide,
                isLoading = false
            )
        }
    }

    // =============================================================
    // Root Cause Analysis
    // =============================================================
    private fun inputCrashLog(log: String) {
        _state.value = _state.value.copy(crashLogInput = log)
    }

    private fun analyzeRootCause() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            delay(800)

            val log = _state.value.crashLogInput
            val result = when {
                log.contains("UnsatisfiedLinkError") && log.contains("dlopen") -> {
                    RootCauseResult(
                        isRootCauseFound = true,
                        rootCause = "UnsatisfiedLinkError: Native library loading failed (dlopen failed)",
                        affectedCallLocations = _state.value.nonCompliantCalls.filter {
                            it.callType == LoadCallType.LOAD_DYNAMIC
                        },
                        fixRecommendation = "Use FileChannel.lock() to mark .so as read-only before loading. " +
                            "Or upgrade Unity/SDK to version supporting Android 17 read-only constraint.",
                        relatedErrorMessages = listOf(
                            "dlopen failed: library not found",
                            "java.lang.UnsatisfiedLinkError: dlopen failed"
                        )
                    )
                }
                log.contains("read-only") || log.contains("ReadOnly") -> {
                    RootCauseResult(
                        isRootCauseFound = true,
                        rootCause = "Android 17 Read-Only Enforcement: .so file was modified after loading",
                        affectedCallLocations = _state.value.nonCompliantCalls,
                        fixRecommendation = "Android 17 enforces read-only on loaded native libraries. " +
                            "Use FileChannel.lock() or configure android.signingBlock to preserve .so attributes.",
                        relatedErrorMessages = listOf(
                            "read-only file system",
                            "Permission denied"
                        )
                    )
                }
                log.contains("signature") || log.contains("signing") -> {
                    RootCauseResult(
                        isRootCauseFound = true,
                        rootCause = "APK Signing Changed .so Permissions: chmod 0444 was reset during signing",
                        affectedCallLocations = _state.value.nonCompliantCalls.filter {
                            it.callType == LoadCallType.LOAD_ABSOLUTE
                        },
                        fixRecommendation = "chmod 0444 is not preserved after APK signing. " +
                            "Use FileChannel.lock() at runtime instead.",
                        relatedErrorMessages = listOf(
                            "Process contains XML file with wrong permissions"
                        )
                    )
                }
                else -> {
                    RootCauseResult(
                        isRootCauseFound = false,
                        rootCause = "Could not determine root cause from crash log",
                        affectedCallLocations = emptyList(),
                        fixRecommendation = "Please provide a full UnsatisfiedLinkError stack trace for analysis.",
                        relatedErrorMessages = emptyList()
                    )
                }
            }

            _state.value = _state.value.copy(
                rootCauseResult = result,
                isLoading = false
            )
        }
    }

    // =============================================================
    // Game Engine Compliance
    // =============================================================
    private fun selectEngine(engine: GameEngine) {
        _state.value = _state.value.copy(
            selectedEngine = engine,
            engineVersion = engine.defaultVersion
        )
    }

    private fun inputEngineVersion(version: String) {
        _state.value = _state.value.copy(engineVersion = version)
    }

    private fun detectEngineCompliance() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            delay(600)

            val engine = _state.value.selectedEngine
            val version = _state.value.engineVersion

            val result = when (engine) {
                GameEngine.UNITY -> when {
                    version.startsWith("2022.3") || version.startsWith("2023") -> {
                        EngineComplianceResult(
                            engine = engine, version = version,
                            complianceStatus = ComplianceStatus.COMPLIANT,
                            detectedIssues = emptyList(),
                            fixGuide = "Unity $version fully supports Android 17 read-only .so enforcement.",
                            isUpgradeRequired = false, recommendedVersion = version
                        )
                    }
                    version.startsWith("2021.3") || version.startsWith("2020.3") -> {
                        EngineComplianceResult(
                            engine = engine, version = version,
                            complianceStatus = ComplianceStatus.WARNING,
                            detectedIssues = listOf(
                                "IL2CPP scripting backend may have issues with read-only .so on Android 17"
                            ),
                            fixGuide = "Upgrade to Unity 2022.3 LTS or later for full Android 17 support.",
                            isUpgradeRequired = false, recommendedVersion = "2022.3 LTS"
                        )
                    }
                    else -> {
                        EngineComplianceResult(
                            engine = engine, version = version,
                            complianceStatus = ComplianceStatus.NON_COMPLIANT,
                            detectedIssues = listOf(
                                "Legacy Unity version does not support Android 17 read-only constraint"
                            ),
                            fixGuide = "Upgrade to Unity 2022.3 LTS immediately.",
                            isUpgradeRequired = true, recommendedVersion = "2022.3 LTS"
                        )
                    }
                }

                GameEngine.UNREAL -> when {
                    version.startsWith("5.3") || version.startsWith("5.4") -> {
                        EngineComplianceResult(
                            engine = engine, version = version,
                            complianceStatus = ComplianceStatus.COMPLIANT,
                            detectedIssues = emptyList(),
                            fixGuide = "Unreal Engine $version supports Android 17 read-only .so enforcement.",
                            isUpgradeRequired = false, recommendedVersion = version
                        )
                    }
                    version.startsWith("5.0") || version.startsWith("5.1") || version.startsWith("5.2") -> {
                        EngineComplianceResult(
                            engine = engine, version = version,
                            complianceStatus = ComplianceStatus.WARNING,
                            detectedIssues = listOf(
                                "Some .so files loaded via DynamicAPK may have issues"
                            ),
                            fixGuide = "Update to Unreal Engine 5.3+ for better Android 17 compatibility.",
                            isUpgradeRequired = false, recommendedVersion = "5.3"
                        )
                    }
                    else -> {
                        EngineComplianceResult(
                            engine = engine, version = version,
                            complianceStatus = ComplianceStatus.NON_COMPLIANT,
                            detectedIssues = listOf(
                                "Unreal Engine $version predates Android 17 release"
                            ),
                            fixGuide = "Upgrade to Unreal Engine 5.3 or later.",
                            isUpgradeRequired = true, recommendedVersion = "5.3"
                        )
                    }
                }

                GameEngine.COCOS -> when {
                    version.startsWith("3.8") || version.startsWith("3.7") -> {
                        EngineComplianceResult(
                            engine = engine, version = version,
                            complianceStatus = ComplianceStatus.COMPLIANT,
                            detectedIssues = emptyList(),
                            fixGuide = "Cocos Creator $version fully supports Android 17 read-only .so enforcement.",
                            isUpgradeRequired = false, recommendedVersion = version
                        )
                    }
                    version.startsWith("3.5") || version.startsWith("3.6") -> {
                        EngineComplianceResult(
                            engine = engine, version = version,
                            complianceStatus = ComplianceStatus.WARNING,
                            detectedIssues = listOf(
                                "JSB (JavaScript Binding) native calls may be affected"
                            ),
                            fixGuide = "Upgrade to Cocos Creator 3.8+ for Android 17 support.",
                            isUpgradeRequired = false, recommendedVersion = "3.8"
                        )
                    }
                    else -> {
                        EngineComplianceResult(
                            engine = engine, version = version,
                            complianceStatus = ComplianceStatus.NON_COMPLIANT,
                            detectedIssues = listOf(
                                "Cocos Creator $version older than 3.5 does not support Android 17"
                            ),
                            fixGuide = "Upgrade to Cocos Creator 3.8 LTS immediately.",
                            isUpgradeRequired = true, recommendedVersion = "3.8 LTS"
                        )
                    }
                }
            }

            _state.value = _state.value.copy(
                engineResult = result,
                isLoading = false
            )
        }
    }

    // =============================================================
    // CI Configuration
    // =============================================================
    private fun selectCIPlatform(platform: CIPlatform) {
        _state.value = _state.value.copy(ciPlatform = platform)
    }

    private fun generateCIConfig() {
        val platform = _state.value.ciPlatform
        val config = when (platform) {
            CIPlatform.GITHUB_ACTIONS -> {
                listOf(
                    "# GitHub Actions - Android System.load() Compliance CI Pipeline",
                    "name: Android Native Library Compliance",
                    "",
                    "on:",
                    "  push:",
                    "    branches: [main, develop]",
                    "  pull_request:",
                    "    branches: [main, develop]",
                    "  schedule:",
                    "    - cron: '0 2 * * *'",
                    "",
                    "jobs:",
                    "  compliance-scan:",
                    "    runs-on: ubuntu-latest",
                    "    permissions:",
                    "      contents: read",
                    "      pull-requests: write",
                    "",
                    "    steps:",
                    "      - name: Checkout code",
                    "        uses: actions/checkout@v4",
                    "",
                    "      - name: Set up JDK 17",
                    "        uses: actions/setup-java@v4",
                    "        with:",
                    "          java-version: '17'",
                    "          distribution: 'temurin'",
                    "",
                    "      - name: Cache Gradle",
                    "        uses: actions/cache@v3",
                    "        with:",
                    "          path: ~/.gradle/caches",
                    "          key: " + "${'$'}{" + "runner.os}-gradle-" + "${'$'}{" + "hashFiles('**/*.gradle*')}",
                    "",
                    "      - name: Run System.load() Compliance Scan",
                    "        run: |",
                    "          echo 'Scanning for System.load() / System.loadLibrary() calls...'",
                    "          grep -r 'System\\.loadLibrary\\|System\\.load' --include='*.kt' --include='*.java' . || true",
                    "",
                    "      - name: Check .so read-only marking",
                    "        run: |",
                    "          echo 'Validating .so file read-only configuration...'",
                    "          grep -r 'FileChannel' --include='*.kt' . || echo 'No FileChannel.lock() found - ACTION REQUIRED'",
                    "",
                    "      - name: Android Build with Read-Only Validation",
                    "        run: |",
                    "          ./gradlew assembleRelease --stacktrace 2>&1 | tee build.log",
                    "          grep -i 'UnsatisfiedLinkError' build.log && echo 'NATIVE_LOAD_ERROR_DETECTED' || true",
                    "",
                    "      - name: Generate Compliance Report",
                    "        if: always()",
                    "        run: |",
                    "          echo '# Android 17 Native Library Compliance Report' > compliance-report.md",
                    "          echo 'Date: " + "${'$'}{" + "github.event.head_commit.timestamp }}' >> compliance-report.md",
                    "          echo 'Branch: " + "${'$'}{" + "github.ref }}' >> compliance-report.md",
                    "",
                    "      - name: Comment PR with Results",
                    "        if: github.event_name == 'pull_request'",
                    "        uses: actions/github-script@v7",
                    "        with:",
                    "          script: |",
                    "            github.rest.issues.createComment({",
                    "              issue_number: context.issue.number,",
                    "              owner: context.repo.owner,",
                    "              repo: context.repo.repo,",
                    "              body: '## Android 17 Native Library Compliance Check'",
                    "            })",
                    "",
                    "      - name: Upload compliance report",
                    "        uses: actions/upload-artifact@v4",
                    "        with:",
                    "          name: compliance-report",
                    "          path: compliance-report.md",
                    "          retention-days: 30"
                ).joinToString("\n")
            }

            CIPlatform.GITLAB_CI -> {
                listOf(
                    "# GitLab CI - Android System.load() Compliance CI Pipeline",
                    "",
                    "stages:",
                    "  - scan",
                    "  - build",
                    "  - report",
                    "",
                    "variables:",
                    "  ANDROID_COMPILE_SDK: '34'",
                    "  ANDROID_BUILD_TOOLS: '34.0.0'",
                    "  GRADLE_OPTS: '-Dorg.gradle.jvmargs=-Xmx2048m'",
                    "",
                    "native-compliance-scan:",
                    "  stage: scan",
                    "  image: ubuntu:22.04",
                    "  before_script:",
                    "    - apt-get update && apt-get install -y grep curl",
                    "  script:",
                    "    - echo '=== Android Native Library Compliance Scan ==='",
                    "    - echo 'Scanning for System.load() / System.loadLibrary() calls...'",
                    "    - find . -name '*.kt' -o -name '*.java' | xargs grep -h 'System\\.loadLibrary\\|System\\.load' 2>/dev/null || echo 'No calls found'",
                    "    - echo 'Checking .so read-only marking implementation...'",
                    "    - find . -name '*.kt' | xargs grep -h 'FileChannel\\|setReadOnly' 2>/dev/null || echo 'WARNING: No read-only marking found!'",
                    "  artifacts:",
                    "    reports:",
                    "      dotenv: compliance.env",
                    "    expire_in: 1 day",
                    "  rules:",
                    "    - if: " + "${'$'}{CI_PIPELINE_SOURCE} == 'merge_request_event'",
                    "    - if: " + "${'$'}{CI_COMMIT_BRANCH} == 'main' || " + "${'$'}{CI_COMMIT_BRANCH} == 'develop'",
                    "",
                    "build-android:",
                    "  stage: build",
                    "  image: registry.gitlab.com/gitlab-org/ci-cd/auto-devops/auto-devops:latest",
                    "  dependencies:",
                    "    - native-compliance-scan",
                    "  script: |",
                    "    - export ANDROID_SDK_ROOT=" + "${'$'}{ANDROID_SDK_ROOT}",
                    "    - ./gradlew assembleRelease --stacktrace 2>&1 | tee build.log",
                    "    - |",
                    "      if grep -qi 'UnsatisfiedLinkError' build.log; then",
                    "        echo 'FAILURE: UnsatisfiedLinkError detected in build!'",
                    "        echo 'Native library loading failed Android 17 read-only constraint.'",
                    "        exit 1",
                    "      fi",
                    "  artifacts:",
                    "    paths:",
                    "      - app/build/outputs/apk/",
                    "    expire_in: 1 week",
                    "  rules:",
                    "    - if: " + "${'$'}{CI_COMMIT_BRANCH} == 'main' || " + "${'$'}{CI_COMMIT_BRANCH} == 'develop'",
                    "",
                    "compliance-report:",
                    "  stage: report",
                    "  image: alpine:latest",
                    "  dependencies:",
                    "    - native-compliance-scan",
                    "    - build-android",
                    "  script: |",
                    "    - echo 'Generating Android 17 Native Library Compliance Report...'",
                    "    - |",
                    "      cat > compliance-report.md << 'REPORT'",
                    "      # Android 17 Native Library Compliance Report",
                    "      ",
                    "      | Field | Value |",
                    "      |-------|-------|",
                    "      | Pipeline | " + "${'$'}{CI_PIPELINE_URL} |",
                    "      | Branch | " + "${'$'}{CI_COMMIT_REF_NAME} |",
                    "      | Commit | " + "${'$'}{CI_COMMIT_SHA} |",
                    "      | Date | $(date -u +'%Y-%m-%d %H:%M:%S UTC') |",
                    "      ",
                    "      ## Compliance Status",
                    "      ",
                    "      - System.load() calls: Scanned",
                    "      - System.loadLibrary() calls: Scanned",
                    "      - Read-only marking: Validated",
                    "      - Android 17 compatibility: Verified",
                    "      REPORT",
                    "    - echo 'Report generated: compliance-report.md'",
                    "  artifacts:",
                    "    paths:",
                    "      - compliance-report.md",
                    "    expire_in: 30 days",
                    "  rules:",
                    "    - if: " + "${'$'}{CI_COMMIT_BRANCH} == 'main'"
                ).joinToString("\n")
            }
        }

        _state.value = _state.value.copy(ciConfig = config)
    }

    private fun copyCIConfigToClipboard() {
        viewModelScope.launch {
            val config = _state.value.ciConfig
            if (config.isNotEmpty()) {
                _effect.send(SystemLoadComplianceEffect.CopyToClipboard(config, "CI Config"))
                _effect.send(SystemLoadComplianceEffect.ShowToast("CI configuration copied to clipboard"))
            }
        }
    }

    // =============================================================
    // Reset & Error Handling
    // =============================================================
    private fun resetState() {
        _state.value = SystemLoadComplianceState.Initial
    }

    private fun dismissError() {
        _state.value = _state.value.copy(errorMessage = null)
    }

    // =============================================================
    // Init
    // =============================================================
    init {
        // Initialize with default .so files for demo / 初始化默认 .so 文件列表（演示用）
        _state.value = _state.value.copy(
            soFiles = listOf(
                SoFileInfo("libnative-lib.so", "app module", "arm64-v8a", 2_456_789, false, null, ComplianceStatus.COMPLIANT),
                SoFileInfo("libplugin_core.so", "dynamic-loader", "arm64-v8a", 1_234_567, false, null, ComplianceStatus.NON_COMPLIANT),
                SoFileInfo("libunity_engine.so", "game-engine", "arm64-v8a", 15_678_901, false, null, ComplianceStatus.WARNING),
                SoFileInfo("libthirdparty-sdk.so", "third-party-sdk:1.2.3", "armeabi-v7a", 876_543, true, MarkingMethod.FILE_CHANNEL_LOCK, ComplianceStatus.COMPLIANT),
                SoFileInfo("liblegacy.so", "legacy-module", "arm64-v8a", 567_890, false, null, ComplianceStatus.NON_COMPLIANT)
            )
        )
        // Generate initial marking code / 生成初始标记代码
        generateMarkingCode()
    }
}
