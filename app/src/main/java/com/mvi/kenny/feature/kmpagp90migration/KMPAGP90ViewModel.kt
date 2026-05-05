package com.mvi.kenny.feature.kmpagp90migration

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*

/**
 * ViewModel for KMP × AGP 9.0 不兼容迁移工具包
 * KMP × AGP 9.0 Incompatibility Migration Toolkit ViewModel
 *
 * MVI: State is single source of truth, Intent = user actions, Effect = one-time side effects.
 */
class KMPAGP90ViewModel : ViewModel() {

    private val _state = MutableStateFlow(KMPAGP90State())
    val state: StateFlow<KMPAGP90State> = _state.asStateFlow()

    private val _effect = Channel<KMPAGP90Effect>(Channel.BUFFERED)
    val effect: Flow<KMPAGP90Effect> = _effect.receiveAsFlow()

    // Simulated scan data — In production, parse Gradle files from project directory.
    // 模拟扫描数据 — 实际使用时需解析项目中的 Gradle 配置文件。
    private val simulatedIncompatiblePlugins = listOf(
        IncompatiblePlugin(
            moduleName = "composeApp",
            modulePath = ":composeApp",
            currentPlugins = listOf("com.android.library", "org.jetbrains.kotlin.multiplatform", "org.jetbrains.kotlin.android"),
            incompatiblePair = "com.android.library + kotlin.multiplatform",
            urgency = UrgencyLevel.CRITICAL,
            suggestion = "迁移到 com.android.kotlin.multiplatform.library Plugin",
            migrationStep = "替换 plugins {} 中的 android.library + kotlin.multiplatform 为 com.android.kotlin.multiplatform.library"
        ),
        IncompatiblePlugin(
            moduleName = "shared",
            modulePath = ":shared",
            currentPlugins = listOf("com.android.library", "org.jetbrains.kotlin.multiplatform"),
            incompatiblePair = "com.android.library + kotlin.multiplatform",
            urgency = UrgencyLevel.CRITICAL,
            suggestion = "迁移到 com.android.kotlin.multiplatform.library Plugin",
            migrationStep = "替换 plugins {} 中的 android.library + kotlin.multiplatform"
        ),
        IncompatiblePlugin(
            moduleName = "androidApp",
            modulePath = ":androidApp",
            currentPlugins = listOf("com.android.application"),
            incompatiblePair = "无（应用模块不受影响）",
            urgency = UrgencyLevel.LOW,
            suggestion = "无需迁移",
            migrationStep = "N/A"
        )
    )

    private val simulatedStructure = ModuleStructure(
        moduleName = "composeApp",
        modulePath = ":composeApp",
        hasAndroidApp = false,
        hasComposeApp = true,
        hasShared = true,
        files = listOf(
            "src/androidMain/kotlin/.../MainActivity.kt",
            "src/androidMain/AndroidManifest.xml",
            "src/commonMain/kotlin/.../Greeting.kt",
            "src/iosMain/kotlin/.../iosMain.kt",
            "build.gradle.kts"
        )
    )

    private val simulatedSplitDiff = ModuleSplitDiff(
        originalModule = ":composeApp (单模块)",
        newAndroidApp = ":androidApp (新增 Android Application 模块)",
        newComposeApp = ":composeApp (保留为 KMP Library 模块)",
        movedFiles = listOf(
            "src/androidMain/kotlin/.../MainActivity.kt → androidApp/src/main/kotlin/...",
            "src/androidMain/AndroidManifest.xml → androidApp/src/main/",
            "src/commonMain/... → composeApp/src/commonMain/..."
        ),
        settingsChanges = """include(":androidApp")          // 新增
include(":composeApp")         // 原 composeApp 改名"""
    )

    private val simulatedMigrationSteps = listOf(
        MigrationStep(1, "Update plugins block", "更新 plugins 块",
            "Replace android.library + kotlin.multiplatform with the new KMP Library Plugin",
            "将 android.library + kotlin.multiplatform 替换为新的 KMP Library Plugin",
            """plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.multiplatform")
    id("org.jetbrains.kotlin.android")
}""",
            """plugins {
    id("com.android.kotlin.multiplatform.library")
    // org.jetbrains.kotlin.android is now built-in
}""",
            "composeApp/build.gradle.kts"),
        MigrationStep(2, "Remove android {} kotlin block", "移除 android {} kotlin 配置块",
            "Built-in Kotlin is provided by AGP 9.0 — remove explicit kotlin {} inside android {}",
            "AGP 9.0 已内置 Kotlin 支持 — 移除 android {} 内的 kotlin {} 显式配置",
            """android {
    namespace = "com.example.app"
    kotlin { android() }
}""",
            """android {
    namespace = "com.example.app"
    // kotlin {} block removed — built-in by AGP 9.0
}""",
            "composeApp/build.gradle.kts"),
        MigrationStep(3, "Update iosTarget configuration", "更新 iosTarget 配置",
            "iOS target configuration for Swift Export (Kotlin 2.2.20+)",
            "iOS 目标配置，Swift Export 默认启用",
            """kotlin {
    android()
    iosX64(); iosArm64(); iosSimulatorArm64()
}""",
            """kotlin {
    android()
    iosX64(); iosArm64(); iosSimulatorArm64()
    // Swift Export is enabled by default
}""",
            "composeApp/build.gradle.kts"),
        MigrationStep(4, "Update build-logic plugins", "更新 build-logic 插件",
            "Update convention plugins to use KMP Plugin with AGP 9.0 DSL",
            "更新 build-logic 中的插件以使用与 AGP 9.0 兼容的 KMP Plugin DSL",
            """id("com.android.library")
id("org.jetbrains.kotlin.multiplatform")""",
            """id("com.android.kotlin.multiplatform.library")
// Built-in Kotlin is automatic""",
            "build-logic/convention/build.gradle.kts"),
        MigrationStep(5, "Verify Gradle 9.0+", "验证 Gradle 9.0+",
            "AGP 9.0 requires Gradle 9.0+",
            "AGP 9.0 要求 Gradle 9.0+",
            "distributionUrl=...gradle-8.7-bin.zip",
            "distributionUrl=...gradle-9.0-bin.zip",
            "gradle/wrapper/gradle-wrapper.properties")
    )

    private val simulatedGradleBreakingChanges = listOf(
        GradleBreakingChange("8.x → 9.0", "KotlinCompileDaemon removed",
            "KotlinCompileDaemon 已移除",
            "ksp.useKSP2 and kotlin.daemon.jvmargs moved to Gradle daemon",
            "ksp.useKSP2 和 kotlin.daemon.jvmargs 移至 Gradle daemon",
            UrgencyLevel.HIGH),
        GradleBreakingChange("8.x → 9.0", "buildSrc no longer supported",
            "buildSrc 不再支持",
            "Move convention plugins from buildSrc to settings.gradle.kts",
            "将 convention 插件从 buildSrc 移至 settings.gradle.kts",
            UrgencyLevel.MEDIUM),
        GradleBreakingChange("8.x → 9.0", "AGP 9.0 requires namespace",
            "AGP 9.0 要求 namespace",
            "android.library must have namespace property set",
            "android.library 必须设置 namespace 属性",
            UrgencyLevel.MEDIUM)
    )

    fun processIntent(intent: KMPAGP90Intent) {
        when (intent) {
            is KMPAGP90Intent.SelectTab -> _state.update { it.copy(selectedTab = intent.index) }
            is KMPAGP90Intent.UpdateProjectPath -> _state.update { it.copy(projectPath = intent.path) }
            is KMPAGP90Intent.StartScan -> startScan()
            is KMPAGP90Intent.CancelScan -> _state.update { it.copy(isScanning = false, scanStatus = ScanStatus.IDLE) }
            is KMPAGP90Intent.ToggleResultExpand -> _state.update {
                it.copy(expandedCardId = if (it.expandedCardId == intent.id) null else intent.id)
            }
            is KMPAGP90Intent.AnalyzeStructure -> analyzeStructure()
            is KMPAGP90Intent.GenerateSplitDiff -> generateSplitDiff()
            is KMPAGP90Intent.SelectMigrationStep -> _state.update { it.copy(selectedStep = intent.index) }
            is KMPAGP90Intent.ShowBeforeAfterDiff -> _state.update { it.copy(
                beforeAfterDiff = BeforeAfterDiff(
                    pluginBlockBefore = """plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.multiplatform")
}""",
                    pluginBlockAfter = """plugins {
    id("com.android.kotlin.multiplatform.library")
}""",
                    androidBlockBefore = """android {
    namespace = "com.example.app"
    kotlin { android() }
}""",
                    androidBlockAfter = """android {
    namespace = "com.example.app"
    // kotlin {} removed — built-in by AGP 9.0
}""",
                    file = "composeApp/build.gradle.kts"
                )
            )}
            is KMPAGP90Intent.DetectBuiltInKotlin -> _state.update {
                it.copy(builtInKotlinDetected = true, kotlinPluginExplicit = true, currentGradleVersion = "8.7",
                    gradleBreakingChanges = simulatedGradleBreakingChanges)
            }
            is KMPAGP90Intent.SetMigrationOrder -> _state.update { it.copy(recommendedMigrationOrder = intent.order) }
            is KMPAGP90Intent.ValidateCICompliance -> validateCICompliance()
            is KMPAGP90Intent.MarkMigrationComplete -> _state.update {
                it.copy(migrationComplete = true, ciComplianceStatus = ComplianceStatus.COMPLIANT, ciMigrationProgress = 100)
            }
        }
    }

    private fun startScan() {
        viewModelScope.launch {
            _state.update { it.copy(isScanning = true, scanStatus = ScanStatus.SCANNING, scanResults = emptyList()) }
            delay(2000)
            val results = simulatedIncompatiblePlugins
            val maxUrgency = results.maxOfOrNull { it.urgency } ?: UrgencyLevel.MEDIUM
            _state.update { it.copy(isScanning = false, scanStatus = ScanStatus.COMPLETED, scanResults = results, scanUrgency = maxUrgency) }
            _effect.send(KMPAGP90Effect.ScanCompleted)
        }
    }

    private fun analyzeStructure() {
        viewModelScope.launch {
            _state.update { it.copy(isAnalyzing = true) }
            delay(1500)
            _state.update { it.copy(isAnalyzing = false, currentStructure = simulatedStructure) }
        }
    }

    private fun generateSplitDiff() {
        viewModelScope.launch {
            _state.update { it.copy(isAnalyzing = true) }
            delay(1000)
            _state.update { it.copy(isAnalyzing = false, splitDiff = simulatedSplitDiff) }
        }
    }

    private fun validateCICompliance() {
        viewModelScope.launch {
            _state.update { it.copy(ciComplianceStatus = ComplianceStatus.PARTIAL) }
            delay(2000)
            _state.update { it.copy(
                ciComplianceStatus = ComplianceStatus.NON_COMPLIANT,
                ciMigrationProgress = 60,
                countdownWarning = CountdownWarning(180, false,
                    "AGP 10.0 release estimated ~180 days",
                    "AGP 10.0 预计在约 180 天后发布"),
                builtInKotlinUsed = true
            ) }
        }
    }
}
