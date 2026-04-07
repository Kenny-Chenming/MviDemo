package com.mvi.kenny.feature.page16kb

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
 * Page16KbViewModel — 16KB Page Size 迁移工具状态管理
 * ============================================================
 * Inherits ViewModel, holds Page16KbState and Page16KbEffect.
 *
 * State Management:
 * - _state: Private MutableStateFlow, written internally by ViewModel
 * - state: Public StateFlow, for UI layer subscription (collectAsState)
 *
 * Effect Management:
 * - _effect: Channel (hot flow), buffer size BUFFERED
 * - effect: receiveAsFlow, UI layer listens via collect{}
 *
 * @see Page16KbContract State/Intent/Effect definitions
 * @see Page16KbScreen Main screen
 */
class Page16KbViewModel : ViewModel() {

    // =============================================================
    // State
    // =============================================================
    /** Page state (StateFlow, UI read-only) / 页面状态 */
    private val _state = MutableStateFlow(Page16KbState.Initial)
    val state: StateFlow<Page16KbState> = _state.asStateFlow()

    /** Current state snapshot for lambda access / 当前状态快照 */
    val currentState: Page16KbState get() = _state.value

    // =============================================================
    // Effect
    // =============================================================
    /** Effect Channel / 副作用通道 */
    private val _effect = Channel<Page16KbEffect>(Channel.BUFFERED)
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
    fun sendIntent(intent: Page16KbIntent) {
        when (intent) {
            is Page16KbIntent.UploadApk -> uploadApk(intent.uri)
            is Page16KbIntent.SelectSoDetail -> selectSoDetail(intent.soFileInfo)
            is Page16KbIntent.ClearSoDetail -> clearSoDetail()
            is Page16KbIntent.FilterSoList -> filterSoList(intent.mode)
            is Page16KbIntent.ApplyFix -> applyFix(intent.recommendation)
            is Page16KbIntent.RunVerification -> runVerification(intent.apkUri)
            is Page16KbIntent.SearchSdkCompatibility -> searchSdkCompatibility(intent.query)
            is Page16KbIntent.NextStep -> nextStep()
            is Page16KbIntent.PreviousStep -> previousStep()
            is Page16KbIntent.ExportReport -> exportReport(intent.isJson)
            is Page16KbIntent.DismissError -> dismissError()
        }
    }

    // =============================================================
    // APK Upload & Parsing
    // =============================================================
    /**
     * Upload and parse APK file / 上传并解析 APK 文件
     *
     * Simulates APK parsing to extract:
     * 1. Package name, version from AndroidManifest.xml (simulated)
     * 2. .so file list from lib/ directory (simulated)
     * 3. ELF header analysis for 16KB alignment detection (simulated)
     *
     * Real implementation would use:
     * - AAPT2 / Apktool to parse AndroidManifest.xml
     * - ZipFile to read lib/ directory entries
     * - ELF parsing (kotlin-elffile library or custom parser)
     *
     * @param uri APK file URI / APK 文件 URI
     */
    private fun uploadApk(uri: android.net.Uri) {
        viewModelScope.launch {
            _state.value = _state.value.copy(
                isParsingApk = true,
                parseProgress = 0f,
                error = null,
                currentStep = Step.DETECT
            )

            try {
                // Simulate APK parsing with progress updates
                // 模拟 APK 解析，逐步更新进度
                delay(300) // Initial file access
                _state.value = _state.value.copy(parseProgress = 0.1f)
                delay(400) // Manifest parsing
                _state.value = _state.value.copy(parseProgress = 0.3f)
                delay(500) // .so list extraction
                _state.value = _state.value.copy(parseProgress = 0.6f)
                delay(600) // ELF header analysis
                _state.value = _state.value.copy(parseProgress = 0.9f)
                delay(200) // Finalization

                // Generate mock data for demonstration
                // 为演示生成模拟数据
                val apkInfo = ApkInfo(
                    packageName = "com.example.myapp",
                    versionName = "2.3.1",
                    versionCode = 2310,
                    minSdkVersion = 24,
                    targetSdkVersion = 35,
                    soCount = 12,
                    fileSize = 48_000_000L
                )

                val soList = generateMockSoList()
                val issueCount = soList.count { it.alignmentStatus == SoAlignmentStatus.MISALIGNED }
                val fixRecommendations = soList
                    .filter { it.alignmentStatus == SoAlignmentStatus.MISALIGNED }
                    .map { generateFixRecommendation(it) }

                _state.value = _state.value.copy(
                    isParsingApk = false,
                    parseProgress = 1f,
                    uploadedApkInfo = apkInfo,
                    soList = soList,
                    issueCount = issueCount,
                    totalCount = soList.size,
                    fixRecommendations = fixRecommendations,
                    currentStep = Step.ANALYZE
                )
                _effect.send(Page16KbEffect.ShowToast("APK parsed: ${soList.size} .so files found / APK 解析完成：发现 ${soList.size} 个 .so 文件"))
                _effect.send(Page16KbEffect.ScrollToTop)

            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isParsingApk = false,
                    error = "Failed to parse APK: ${e.message}"
                )
                _effect.send(Page16KbEffect.ShowError("APK 解析失败: ${e.message}"))
            }
        }
    }

    /**
     * Generate mock .so list for demonstration / 生成模拟 .so 列表用于演示
     *
     * Real implementation would parse ELF headers and check p_align field.
     * 16KB page size requires p_align == 0x4000 (16384 bytes).
     *
     * @return List of mock SoFileInfo / 模拟 .so 文件信息列表
     */
    private fun generateMockSoList(): List<SoFileInfo> {
        val mockSoData = listOf(
            Triple("libnative-lib.so", "com.example:myapp", "arm64-v8a"),
            Triple("libjnidispatch.so", "react-native:react-native", "arm64-v8a"),
            Triple("libflutter.so", "flutter:flutter", "arm64-v8a"),
            Triple("libBugsnag.so", "bugsnag:bugsnag-android", "arm64-v8a"),
            Triple("libstripe.so", "com.stripe:stripe-android", "armeabi-v7a"),
            Triple("libopencv_java4.so", "opencv:opencv", "arm64-v8a"),
            Triple("libhermes.so", "com.facebook:hermes", "arm64-v8a"),
            Triple("libsqlcipher.so", "net.zetetic:android-database-sqlcipher", "arm64-v8a"),
            Triple("libwebrtc.so", "com.twilio:webrtc-android", "arm64-v8a"),
            Triple("libtensorflow_demo.so", "org.tensorflow:tensorflow-android", "arm64-v8a"),
            Triple("libagora_rtc.so", "io.agora:agora-rtc-sdk", "armeabi-v7a"),
            Triple("libunity.so", "com.unity3d:unity", "arm64-v8a"),
        )

        return mockSoData.mapIndexed { index, (name, source, arch) ->
            val isThirdParty = source.contains(":")
            val isMisaligned = index in listOf(1, 4, 7, 10) // Simulate some as misaligned

            SoFileInfo(
                name = name,
                source = source,
                architecture = arch,
                size = Random.nextLong(500_000, 30_000_000),
                alignmentStatus = if (isMisaligned) SoAlignmentStatus.MISALIGNED else SoAlignmentStatus.ALIGNED,
                minNdkVersion = if (isMisaligned) "NDK r26+" else "NDK r23+",
                elfHeaderInfo = "p_align: 0x${if (isMisaligned) "1000" else "4000"} (16KB ${if (isMisaligned) "✗" else "✓"})",
                isFromThirdParty = isThirdParty
            )
        }
    }

    /**
     * Generate fix recommendation for a misaligned .so / 为未对齐 .so 生成修复建议
     *
     * @param soInfo .so file info / .so 文件信息
     * @return Fix recommendation / 修复建议
     */
    private fun generateFixRecommendation(soInfo: SoFileInfo): FixRecommendation {
        val fixType = when {
            soInfo.isFromThirdParty -> FixType.UPGRADE_SDK
            soInfo.architecture == "armeabi-v7a" -> FixType.RECOMPILE
            else -> FixType.RECOMPILE
        }

        val ndkVersion = when (fixType) {
            FixType.RECOMPILE -> "NDK r26+ (最低)"
            FixType.UPGRADE_SDK -> "升级到支持 16KB 的 SDK 版本"
            FixType.SUPPRESS -> "N/A"
        }

        val fixCommand = when (fixType) {
            FixType.RECOMPILE -> """
                |# Rebuild .so with 16KB page size support
                |# 使用 16KB page size 支持重新编译 .so
                |
                |# For CMake projects / CMake 项目:
                |cmake ... -DANDROID_PLATFORM=android-24 \
                |            -DANDROID_NDK_HOST=darwin-arm64 \
                |            -DCMAKE_TOOLCHAIN_FILE=${'$'}ANDROID_NDK/build/cmake/android.toolchain.cmake
                |
                |# For ndk-build projects / ndk-build 项目:
                |# 在 Application.mk 中设置:
                |APP_PLATFORM := android-24
                |APP_ABI := ${soInfo.architecture}
            """.trimMargin()

            FixType.UPGRADE_SDK -> """
                |# Upgrade the third-party SDK to a version that supports 16KB page size
                |# 升级第三方 SDK 到支持 16KB page size 的版本
                |
                |# Check the SDK's release notes or contact the vendor for 16KB support.
                |# 查看 SDK 发布说明或联系供应商确认 16KB 支持情况。
                |
                |# Common upgrade path / 常见升级路径:
                |# implementation "com.vendor:sdk:2.x.x"  # Replace with 16KB-capable version
            """.trimMargin()

            FixType.SUPPRESS -> """
                |# If the .so cannot be recompiled immediately, use suppression strategy
                |# 如果 .so 无法立即重新编译，使用 suppression 策略
                |
                |<meta-data android:name="android.bundle.16kb.suppression" android:value="true" />
                |
                |# Note: This suppresses the crash but the .so will not benefit from 16KB optimizations.
                |# 注意：这会抑制崩溃，但 .so 不会受益于 16KB 优化。
            """.trimMargin()
        }

        val suppressionNote = when (fixType) {
            FixType.RECOMPILE -> "推荐：尽快重新编译以获得 16KB 性能优势和 Google Play 合规"
            FixType.UPGRADE_SDK -> "建议：联系 SDK 供应商获取 16KB 兼容版本，或寻找替代方案"
            FixType.SUPPRESS -> "临时方案：仅当无法重新编译且 SDK 无更新时使用，⚠️ 长期风险"
        }

        return FixRecommendation(
            soFileInfo = soInfo,
            fixType = fixType,
            ndkVersion = ndkVersion,
            fixCommand = fixCommand,
            suppressionNote = suppressionNote,
            isAutoFixable = fixType == FixType.RECOMPILE && !soInfo.isFromThirdParty
        )
    }

    // =============================================================
    // .so Detail
    // =============================================================
    /**
     * Select .so to view detail / 选择查看 .so 详情
     */
    private fun selectSoDetail(soFileInfo: SoFileInfo) {
        _state.value = _state.value.copy(selectedSoDetail = soFileInfo)
        viewModelScope.launch {
            _effect.send(Page16KbEffect.NavigateToSoDetail(soFileInfo))
        }
    }

    /**
     * Clear selected .so detail / 清除选中的 .so 详情
     */
    private fun clearSoDetail() {
        _state.value = _state.value.copy(selectedSoDetail = null)
    }

    // =============================================================
    // Filtering
    // =============================================================
    /**
     * Filter .so list / 过滤 .so 列表
     *
     * @param mode Filter mode / 过滤模式
     */
    private fun filterSoList(mode: FilterMode) {
        _state.value = _state.value.copy(filterMode = mode)
    }

    // =============================================================
    // Fix Application
    // =============================================================
    /**
     * Apply fix recommendation / 应用修复建议
     *
     * @param recommendation Fix recommendation to apply / 要应用的修复建议
     */
    private fun applyFix(recommendation: FixRecommendation) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            try {
                // Simulate fix application
                delay(800)

                // Update the .so in list to aligned status
                val updatedSoList = _state.value.soList.map { so ->
                    if (so.name == recommendation.soFileInfo.name) {
                        so.copy(alignmentStatus = SoAlignmentStatus.ALIGNED)
                    } else so
                }

                val newIssueCount = updatedSoList.count { it.alignmentStatus == SoAlignmentStatus.MISALIGNED }

                _state.value = _state.value.copy(
                    isLoading = false,
                    soList = updatedSoList,
                    issueCount = newIssueCount,
                    fixRecommendations = _state.value.fixRecommendations.filter {
                        it.soFileInfo.name != recommendation.soFileInfo.name
                    }
                )
                _effect.send(Page16KbEffect.ShowToast("Fix applied for ${recommendation.soFileInfo.name} / 已应用修复：${recommendation.soFileInfo.name}"))
            } catch (e: Exception) {
                _state.value = _state.value.copy(isLoading = false)
                _effect.send(Page16KbEffect.ShowError("Fix failed: ${e.message}"))
            }
        }
    }

    // =============================================================
    // Verification
    // =============================================================
    /**
     * Run verification on APK / 在 APK 上运行验证
     *
     * @param apkUri APK file URI / APK 文件 URI
     */
    private fun runVerification(apkUri: android.net.Uri) {
        viewModelScope.launch {
            _state.value = _state.value.copy(
                isLoading = true,
                currentStep = Step.VERIFY
            )

            try {
                // Simulate verification
                delay(2000)

                val currentSoList = _state.value.soList
                val allPassed = currentSoList.all { it.alignmentStatus == SoAlignmentStatus.ALIGNED }
                val passedCount = currentSoList.count { it.alignmentStatus == SoAlignmentStatus.ALIGNED }

                val result = VerificationResult(
                    isAllPassed = allPassed,
                    passedCount = passedCount,
                    totalCount = currentSoList.size,
                    verificationMessage = if (allPassed) {
                        "All .so files are 16KB aligned. APK is ready for Google Play 16KB enforcement."
                    } else {
                        "${currentSoList.size - passedCount} .so file(s) still need 16KB alignment. Deadline: 2026-05-31"
                    }
                )

                _state.value = _state.value.copy(
                    isLoading = false,
                    verificationResult = result
                )

                if (allPassed) {
                    _effect.send(Page16KbEffect.ShowToast("Verification PASSED / 验证通过"))
                } else {
                    _effect.send(Page16KbEffect.ShowError("Verification FAILED: ${currentSoList.size - passedCount} issues remain / 验证失败：仍有 ${currentSoList.size - passedCount} 个问题"))
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = "Verification failed: ${e.message}"
                )
                _effect.send(Page16KbEffect.ShowError("验证失败: ${e.message}"))
            }
        }
    }

    // =============================================================
    // SDK Compatibility Search
    // =============================================================
    /**
     * Search SDK 16KB compatibility / 搜索 SDK 16KB 兼容性
     *
     * @param query Search query / 搜索查询
     */
    private fun searchSdkCompatibility(query: String) {
        _state.value = _state.value.copy(sdkCompatibilityQuery = query)

        if (query.length < 2) {
            _state.value = _state.value.copy(sdkCompatibilityResults = emptyList())
            return
        }

        viewModelScope.launch {
            // Simulate SDK compatibility search
            delay(300)

            val mockDatabase = listOf(
                SdkCompatibility("Bugsnag", "6.4.0", true, "6.4.0+", "Fully supports 16KB page size from this version"),
                SdkCompatibility("Bugsnag", "5.x.x", false, "6.4.0+", "Older versions may crash on 16KB devices"),
                SdkCompatibility("Stripe", "20.5.0", true, "20.5.0+", "16KB compatible since this release"),
                SdkCompatibility("Stripe", "19.x.x", false, "20.5.0+", "Upgrade required before 2026-05-31"),
                SdkCompatibility("React Native", "0.76.0+", true, "0.76.0+", "New Architecture supports 16KB page size"),
                SdkCompatibility("React Native", "0.75.x", false, "0.76.0+", "Legacy Architecture requires upgrade"),
                SdkCompatibility("Firebase", "33.0.0+", true, "33.0.0+", "All Firebase SDKs support 16KB from this version"),
                SdkCompatibility("SQLCipher", "4.5.6+", true, "4.5.6+", "Supports 16KB page size with NDK r26+"),
                SdkCompatibility("Agora RTC", "2.9.0+", true, "2.9.0+", "16KB support added"),
                SdkCompatibility("Unity", "2023.2+", true, "2023.2+", "IL2CPP with 16KB page support"),
                SdkCompatibility("TensorFlow Lite", "2.14.0+", true, "2.14.0+", "Native library rebuilt with 16KB support"),
                SdkCompatibility("OpenCV", "4.9.0+", true, "4.9.0+", "Rebuilt with NDK r26"),
            )

            val results = mockDatabase.filter {
                it.sdkName.contains(query, ignoreCase = true) ||
                it.sdkName.contains(query, ignoreCase = true)
            }.distinctBy { "${it.sdkName}:${it.version}" }

            _state.value = _state.value.copy(sdkCompatibilityResults = results)
        }
    }

    // =============================================================
    // Step Navigation
    // =============================================================
    /**
     * Navigate to next step / 进入下一步
     */
    private fun nextStep() {
        val currentIndex = _state.value.stepIndex
        if (currentIndex < Step.entries.size - 1) {
            val nextStep = Step.entries[currentIndex + 1]
            _state.value = _state.value.copy(currentStep = nextStep)
            viewModelScope.launch {
                _effect.send(Page16KbEffect.ScrollToTop)
            }
        }
    }

    /**
     * Navigate to previous step / 返回上一步
     */
    private fun previousStep() {
        val currentIndex = _state.value.stepIndex
        if (currentIndex > 0) {
            val prevStep = Step.entries[currentIndex - 1]
            _state.value = _state.value.copy(currentStep = prevStep)
            viewModelScope.launch {
                _effect.send(Page16KbEffect.ScrollToTop)
            }
        }
    }

    // =============================================================
    // Export
    // =============================================================
    /**
     * Export migration report / 导出迁移报告
     *
     * @param isJson Export as JSON (false = Markdown) / 导出为 JSON
     */
    private fun exportReport(isJson: Boolean) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            try {
                delay(1200)

                val extension = if (isJson) "json" else "md"
                val filePath = "/storage/emulated/0/Download/16kb_migration_report.$extension"

                _state.value = _state.value.copy(isLoading = false)
                _effect.send(Page16KbEffect.ShowExportSuccess(filePath))
                _effect.send(Page16KbEffect.ShowToast("Report exported to $filePath"))
            } catch (e: Exception) {
                _state.value = _state.value.copy(isLoading = false)
                _effect.send(Page16KbEffect.ShowError("Export failed: ${e.message}"))
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
