package com.mvi.kenny.feature.agp90migration

// ================================================================
// AGP90MigrationViewModel — AGP 9.0 破坏性变更迁移工具包 ViewModel
// ================================================================
// MVI ViewModel for AGP 9.0 Breaking Changes Migration Toolkit.
//
// PRD-226: AGP 9.0 破坏性变更迁移工具包
// Design Reference: memory/agency/designs/PRD-226-AGP-9-0-破坏性变更迁移工具包.md
//
// MVI Pattern:
//   Intent  → ViewModel processes → updates State
//   State   → Screen observes → recomposes
//   Effect  → Channel sends → one-time events (toast, clipboard, navigation)
// ================================================================
//
// Tab Structure:
//   Tab 0: 密度Split检测 — Detect splits.density config, output App Bundle alternative
//   Tab 1: DSL稳定化 — incubating→stable API changes by AGP version
//   Tab 2: BuildConfig & NDK — BuildConfig field changes + NDK compliance check
//   Tab 3: Flutter兼容性 [P0] — Flutter AGP 9.0 incompatibility workaround
//   Tab 4: 升级路径 — Gradle 9.0 + AGP 9.0 upgrade path + CI compliance + checklist
// ================================================================

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

/**
 * ============================================================
 * AGP90MigrationViewModel — AGP 9.0 迁移工具 ViewModel
 * ============================================================
 *
 * Manages UI state and processes user intents.
 * MVI architecture: State is immutable, updated via copy().
 */
class AGP90MigrationViewModel : ViewModel() {

    // ── State ─────────────────────────────────────────────────
    private val _state = MutableStateFlow(AGP90MigrationState.Initial)
    val state: StateFlow<AGP90MigrationState> = _state.asStateFlow()

    // ── Effect Channel ──────────────────────────────────────────
    private val _effect = MutableSharedFlow<AGP90MigrationEffect>()
    val effect: MutableSharedFlow<AGP90MigrationEffect> = _effect

    init {
        // 初始化时自动加载所有数据 / Load all data on init
        loadAllData()
    }

    // ============================================================
    // Intent Handler / 意图处理
    // ============================================================

    /**
     * Process user intent
     * 处理用户意图
     */
    fun processIntent(intent: AGP90MigrationIntent) {
        when (intent) {
            is AGP90MigrationIntent.SelectTab -> selectTab(intent.index)
            is AGP90MigrationIntent.SetProjectPath -> setProjectPath(intent.path)
            is AGP90MigrationIntent.ScanSplitsDensity -> scanSplitsDensity(intent.simulate)
            is AGP90MigrationIntent.LoadDSLChanges -> loadDSLChanges(intent.simulate)
            is AGP90MigrationIntent.AnalyzeBuildConfig -> analyzeBuildConfig(intent.simulate)
            is AGP90MigrationIntent.CheckNDKCompliance -> checkNDKCompliance(intent.simulate)
            is AGP90MigrationIntent.LoadFlutterCompat -> loadFlutterCompat(intent.simulate)
            is AGP90MigrationIntent.GenerateMigrationPath -> generateMigrationPath(intent.simulate)
            is AGP90MigrationIntent.RunCIComplianceCheck -> runCIComplianceCheck(intent.simulate)
            is AGP90MigrationIntent.ToggleChecklist -> toggleChecklist(intent.itemId, intent.checked)
            is AGP90MigrationIntent.CopyCode -> copyCode(intent.code)
            is AGP90MigrationIntent.DismissSnackbar -> dismissSnackbar()
            is AGP90MigrationIntent.ResetAll -> resetAll()
        }
    }

    // ============================================================
    // Tab Selection / Tab 切换
    // ============================================================

    private fun selectTab(index: Int) {
        _state.value = _state.value.copy(selectedTab = index)
        // 触发当前 Tab 的数据加载 / Trigger data load for current tab
        when (index) {
            0 -> scanSplitsDensity(simulate = true)
            1 -> loadDSLChanges(simulate = true)
            2 -> {
                analyzeBuildConfig(simulate = true)
                checkNDKCompliance(simulate = true)
            }
            3 -> loadFlutterCompat(simulate = true)
            4 -> {
                generateMigrationPath(simulate = true)
                runCIComplianceCheck(simulate = true)
            }
        }
    }

    private fun setProjectPath(path: String) {
        _state.value = _state.value.copy(projectPath = path)
    }

    // ============================================================
    // Tab 0: Splits Density Scan / 密度Split检测
    // ============================================================

    private fun scanSplitsDensity(simulate: Boolean) {
        if (_state.value.splitsDensityResults.isNotEmpty()) return  // 已加载 / Already loaded
        viewModelScope.launch {
            _state.value = _state.value.copy(isScanning = true, scanProgress = 0f)
            if (simulate) {
                // 模拟扫描延迟 / Simulate scan delay
                delay(500)
                val results = getMockSplitsDensityResults()
                _state.value = _state.value.copy(
                    splitsDensityResults = results,
                    isScanning = false,
                    scanProgress = 1f
                )
            }
        }
    }

    private fun getMockSplitsDensityResults(): List<SplitsDensityResult> = listOf(
        SplitsDensityResult(
            filePath = "app/build.gradle.kts",
            currentConfig = """
                splits {
                    density {
                        enable true
                        reset()
                        include "mdpi", "hdpi", "xhdpi", "xxhdpi", "xxxhdpi"
                        compatibleScreens("small", "normal", "large", "xlarge")
                    }
                }
            """.trimIndent(),
            alternative = """
                // AGP 9.0: splits.density is completely removed.
                // Use Android App Bundle instead — the bundletool handles density splits automatically.
                //
                // app/build.gradle.kts
                android {
                    bundle {
                        density {
                            // Enable density splits in App Bundle (default behavior)
                            enableSplit = true
                        }
                    }
                }
            """.trimIndent(),
            riskLevel = RiskLevel.P0_CRITICAL,
            recommendation = "立即修复 / Fix immediately — splits.density API removed in AGP 9.0"
        ),
        SplitsDensityResult(
            filePath = "app/build.gradle",
            currentConfig = """
                splits {
                    density {
                        enable true
                        compatibleScreens 'small','normal','large','xlarge'
                    }
                }
            """.trimIndent(),
            alternative = """
                // AGP 9.0: Use App Bundle for automatic density-based APK generation.
                // Run: ./gradlew bundleRelease
                // Then use bundletool to generate APKs:
                //   java -jar bundletool.jar build-apks --bundle=app.aab --output=app.apks
            """.trimIndent(),
            riskLevel = RiskLevel.P0_CRITICAL,
            recommendation = "立即修复 / Fix immediately — all splits.density configs invalidated in AGP 9.0"
        )
    )

    // ============================================================
    // Tab 1: DSL Stabilization / DSL稳定化
    // ============================================================

    private fun loadDSLChanges(simulate: Boolean) {
        if (_state.value.dslChanges.isNotEmpty()) return
        viewModelScope.launch {
            if (simulate) {
                delay(400)
                val changes = getMockDSLChanges()
                _state.value = _state.value.copy(dslChanges = changes)
            }
        }
    }

    private fun getMockDSLChanges(): List<DSLChange> = listOf(
        // AGP 4.1 changes
        DSLChange(
            agpVersion = "4.1",
            apiName = "android {} / CommonExtension",
            oldPackage = "com.android.build.gradle.internal.core.ProductFlavor",
            newPackage = "com.android.build.api.ProductFlavor",
            changeType = "Incubating → Stable",
            riskLevel = RiskLevel.P2_MEDIUM,
            beforeCode = """
                // AGP 4.1 (incubating)
                val flavor = productFlavors.create("dev") {
                    dimension = "environment"
                    applicationIdSuffix = ".dev"
                }
            """.trimIndent(),
            afterCode = """
                // AGP 4.1+ (stable)
                val flavor = productFlavors.create("dev") {
                    dimension.set("environment")
                    applicationIdSuffix.set(".dev")
                }
            """.trimIndent()
        ),
        // AGP 4.2 changes
        DSLChange(
            agpVersion = "4.2",
            apiName = "android.library / LibraryExtension",
            oldPackage = "com.android.build.gradle.internal.LibraryExtension",
            newPackage = "com.android.build.api.library.LibraryExtension",
            changeType = "Incubating → Stable",
            riskLevel = RiskLevel.P1_HIGH,
            beforeCode = """
                // AGP 4.2 (incubating)
                android {
                    library {
                        enableJacocoInstrumentation = true
                        packagingOptions {}
                    }
                }
            """.trimIndent(),
            afterCode = """
                // AGP 4.2+ (stable)
                android {
                    library {
                        enableJacocoInstrumentation.set(true)
                        packaging {}
                    }
                }
            """.trimIndent()
        ),
        // AGP 7.0 changes
        DSLChange(
            agpVersion = "7.0",
            apiName = "android.buildTypes / BuildType",
            oldPackage = "com.android.build.gradle.internal.core.BuildType",
            newPackage = "com.android.build.api.dsl.BuildType",
            changeType = "Incubating → Stable",
            riskLevel = RiskLevel.P1_HIGH,
            beforeCode = """
                // AGP 7.0 (incubating)
                android {
                    buildTypes {
                        release {
                            minifyEnabled true
                            proguardFiles getDefaultProguardFile('proguard-android.txt')
                        }
                    }
                }
            """.trimIndent(),
            afterCode = """
                // AGP 7.0+ (stable)
                android {
                    buildTypes {
                        release {
                            minifyEnabled.set(true)
                            proguardFiles.add(file('proguard-android.txt'))
                        }
                    }
                }
            """.trimIndent()
        ),
        DSLChange(
            agpVersion = "7.0",
            apiName = "android.namespace",
            oldPackage = "android.namespace (manifest placeholder)",
            newPackage = "android.namespace (DSL property)",
            changeType = "Manifest Placeholder → DSL Property",
            riskLevel = RiskLevel.P0_CRITICAL,
            beforeCode = """
                // AGP <7.0: namespace via manifest
                <manifest xmlns:android="http://schemas.android.com/apk/res/android"
                    package="com.example.app">
            """.trimIndent(),
            afterCode = """
                // AGP 7.0+: namespace via build.gradle.kts
                android {
                    namespace = "com.example.app"
                }
                // Remove package from AndroidManifest.xml
            """.trimIndent()
        )
    )

    // ============================================================
    // Tab 2: BuildConfig & NDK / BuildConfig分析 & NDK合规检测
    // ============================================================

    private fun analyzeBuildConfig(simulate: Boolean) {
        if (_state.value.buildConfigAnalysis != null) return
        viewModelScope.launch {
            if (simulate) {
                delay(350)
                val analysis = getMockBuildConfigAnalysis()
                _state.value = _state.value.copy(buildConfigAnalysis = analysis)
            }
        }
    }

    private fun getMockBuildConfigAnalysis(): BuildConfigAnalysis = BuildConfigAnalysis(
        fieldName = "BuildConfig.VERSION_CODE / VERSION_NAME",
        oldBehavior = "BuildConfig fields always generated and accessible",
        newBehavior = "BuildConfig generation behavior changed — some fields may not be available or paths changed",
        affectedCode = """
            // Common BuildConfig usages affected in AGP 9.0:
            val versionCode = BuildConfig.VERSION_CODE       // May not compile
            val versionName = BuildConfig.VERSION_NAME       // Still works but deprecated
            val debuggable = BuildConfig.DEBUG               // Still works
            val buildType = BuildConfig.BUILD_TYPE           // Path changed
        """.trimIndent(),
        migrationStep = """
            // Recommended migration:
            // 1. Replace BuildConfig.VERSION_CODE with:
            val versionCode = android.defaultConfig.versionCode ?: 1

            // 2. Use Gradle properties instead:
            //    versionName in android.defaultConfig
            //    versionCode in android.defaultConfig

            // 3. For build-time values, use BuildConfigField in android:
            android {
                buildConfigField "String", "API_BASE_URL", "\"https://api.example.com\""
            }
        """.trimIndent(),
        riskLevel = RiskLevel.P1_HIGH
    )

    private fun checkNDKCompliance(simulate: Boolean) {
        if (_state.value.ndkComplianceResult != null) return
        viewModelScope.launch {
            if (simulate) {
                delay(350)
                val result = getMockNDKComplianceResult()
                _state.value = _state.value.copy(ndkComplianceResult = result)
            }
        }
    }

    private fun getMockNDKComplianceResult(): NDKComplianceResult = NDKComplianceResult(
        currentVersion = "25.1.10896954",
        minRequiredVersion = "27.0.1400281",
        isCompliant = false,
        currentVersionProblems = listOf(
            "NDK 25.x is below AGP 9.0 minimum requirement",
            "AGP 9.0 requires NDK 27+ for full compatibility",
            "Old NDK may cause native build failures with AGP 9.0"
        ),
        upgradePath = """
            Recommended NDK upgrade path:
            1. NDK 25.x → NDK 26.x (intermediate)
            2. NDK 26.x → NDK 27.x (target)

            To upgrade in local.properties:
                ndk.dir=/path/to/android-ndk-r27

            Or in gradle.properties:
                android.ndkVersion=27.0.1400281

            Check available versions:
                ls ${'$'}ANDROID_HOME/ndk/
        """.trimIndent(),
        riskLevel = RiskLevel.P1_HIGH
    )

    // ============================================================
    // Tab 3: Flutter Compatibility / Flutter兼容性 [P0]
    // ============================================================

    private fun loadFlutterCompat(simulate: Boolean) {
        if (_state.value.flutterCompatInfo != null) return
        viewModelScope.launch {
            if (simulate) {
                delay(600)
                val info = getMockFlutterCompatInfo()
                _state.value = _state.value.copy(flutterCompatInfo = info)
            }
        }
    }

    private fun getMockFlutterCompatInfo(): FlutterCompatInfo = FlutterCompatInfo(
        flutterVersion = "3.41.x (latest)",
        dartVersion = "3.11.x",
        isCompatible = false,
        officialIssueUrl = "https://github.com/flutter/flutter/issues/164704",
        currentStatus = "Flutter官方明确警告不兼容 / Flutter officially warns incompatible",
        estimatedFixVersion = "Flutter 3.42 (expected June 2026)",
        workarounds = listOf(
            FlutterWorkaround(
                step = 1,
                title = "暂时降级 AGP 到 8.x / Downgrade AGP to 8.x temporarily",
                description = "在 Flutter 项目中暂时使用 AGP 8.x，等待 Flutter 官方修复",
                code = """
                    // android/app/build.gradle.kts
                    plugins {
                        id("com.android.application") version "8.7.3" apply false
                    }
                """.trimIndent()
            ),
            FlutterWorkaround(
                step = 2,
                title = "锁定 Gradle 版本 / Lock Gradle version",
                description = "确保 Gradle 版本与 AGP 8.x 兼容",
                code = """
                    // gradle/wrapper/gradle-wrapper.properties
                    distributionUrl=https\\://services.gradle.org/distributions/gradle-8.10.2-all.zip
                """.trimIndent()
            ),
            FlutterWorkaround(
                step = 3,
                title = "监控官方 Issue 进度 / Monitor official issue progress",
                description = "关注 Flutter 官方 GitHub Issue 获取修复进度",
                code = "https://github.com/flutter/flutter/issues/164704"
            ),
            FlutterWorkaround(
                step = 4,
                title = "不要升级到 AGP 9.0 / Do NOT upgrade to AGP 9.0",
                description = "Flutter 官方明确说明使用 plugin 的 Flutter Android 项目当前不兼容 AGP 9.0"
            )
        )
    )

    // ============================================================
    // Tab 4: Migration Path / 升级路径
    // ============================================================

    private fun generateMigrationPath(simulate: Boolean) {
        if (_state.value.migrationPath != null) return
        viewModelScope.launch {
            if (simulate) {
                delay(500)
                val path = getMockMigrationPath()
                _state.value = _state.value.copy(migrationPath = path)
            }
        }
    }

    private fun getMockMigrationPath(): MigrationPath = MigrationPath(
        currentGradleVersion = "8.7",
        targetGradleVersion = "9.0",
        currentAGPVersion = "8.5.2",
        targetAGPVersion = "9.0.1",
        upgradeOrder = """
            ⚠️ IMPORTANT: Upgrade Gradle BEFORE AGP!
            ─────────────────────────────────────────
            1. [必须先] Upgrade Gradle 8.7 → 9.0
               ./gradlew wrapper --gradle-version=9.0
               or update gradle-wrapper.properties

            2. [第二步] Upgrade AGP 8.5.2 → 9.0.1
               In settings.gradle.kts:
               pluginManagement {
                   plugins {
                       id("com.android.application") version "9.0.1"
                   }
               }

            3. [第三步] Run ./gradlew :app:dependencies
               Check for any dependency conflicts

            4. [第四步] Run ./gradlew :app:compileDebugKotlin
               Fix any compilation errors

            ⚠️ WARNING: Never upgrade AGP before Gradle!
               AGP 9.0 requires Gradle 9.0+ (not 8.x)
        """.trimIndent(),
        compatibilityMatrix = listOf(
            VersionCompatibilityItem("8.5", "8.2", true),
            VersionCompatibilityItem("8.6", "8.3", true),
            VersionCompatibilityItem("8.7", "8.4", true),
            VersionCompatibilityItem("8.7", "8.5", true),
            VersionCompatibilityItem("8.10", "8.5", true),
            VersionCompatibilityItem("8.10", "8.7", true),
            VersionCompatibilityItem("9.0", "9.0", true),
            VersionCompatibilityItem("9.0", "8.7", false),  // AGP 8.x not compatible with Gradle 9
            VersionCompatibilityItem("8.7", "9.0", false),  // AGP 9.x requires Gradle 9.x
        ),
        riskLevel = RiskLevel.P0_CRITICAL
    )

    private fun runCIComplianceCheck(simulate: Boolean) {
        if (_state.value.ciComplianceStatus != null) return
        viewModelScope.launch {
            if (simulate) {
                delay(400)
                val status = getMockCIComplianceStatus()
                _state.value = _state.value.copy(ciComplianceStatus = status)
            }
        }
    }

    private fun getMockCIComplianceStatus(): CIComplianceStatus = CIComplianceStatus(
        agpVersionCheck = "⚠️ AGP 8.5.2 detected — upgrade to 9.0.1 required",
        gradleVersionCheck = "⚠️ Gradle 8.7 detected — upgrade to 9.0 required",
        overallPassed = false,
        blockingIssues = listOf(
            "AGP version 8.5.2 is below minimum requirement 9.0.1",
            "Gradle version 8.7 is below minimum requirement 9.0",
            "splits.density configuration will fail in AGP 9.0"
        ),
        recommendations = listOf(
            "Upgrade Gradle to 9.0 before upgrading AGP",
            "Remove or migrate splits.density configuration",
            "Run migration toolkit for full checklist"
        )
    )

    // ============================================================
    // Checklist / 检查清单
    // ============================================================

    private fun loadAllData() {
        viewModelScope.launch {
            delay(300)
            val checklist = getMockChecklist()
            _state.value = _state.value.copy(
                checklistItems = checklist,
                completedChecklistCount = 0
            )
            scanSplitsDensity(simulate = true)
        }
    }

    private fun getMockChecklist(): List<ChecklistItem> = listOf(
        // P0 Critical
        ChecklistItem(
            id = "p0-1",
            category = "splits.density",
            title = "移除 splits.density 配置",
            description = "AGP 9.0 完全移除 splits.density API，所有配置立即失效",
            priority = RiskLevel.P0_CRITICAL
        ),
        ChecklistItem(
            id = "p0-2",
            category = "Flutter",
            title = "Flutter 项目降级 AGP 或等待官方修复",
            description = "Flutter 官方明确警告不兼容 AGP 9.0",
            priority = RiskLevel.P0_CRITICAL
        ),
        ChecklistItem(
            id = "p0-3",
            category = "Gradle",
            title = "升级 Gradle 到 9.0",
            description = "AGP 9.0 要求 Gradle 9.0+，不能反向升级",
            priority = RiskLevel.P0_CRITICAL
        ),
        // P1 High
        ChecklistItem(
            id = "p1-1",
            category = "DSL",
            title = "更新 DSL 从 incubating 到 stable",
            description = "AGP 4.1/4.2/7.0 的 incubating DSL 已升级为 stable",
            priority = RiskLevel.P1_HIGH
        ),
        ChecklistItem(
            id = "p1-2",
            category = "BuildConfig",
            title = "检查 BuildConfig 字段可用性",
            description = "AGP 9.0 改变了 BuildConfig 生成逻辑",
            priority = RiskLevel.P1_HIGH
        ),
        ChecklistItem(
            id = "p1-3",
            category = "NDK",
            title = "升级 NDK 到 27.0+",
            description = "AGP 9.0 要求 NDK 27+",
            priority = RiskLevel.P1_HIGH
        ),
        // P2 Medium
        ChecklistItem(
            id = "p2-1",
            category = "CommonExtension",
            title = "检查 CommonExtension 用法",
            description = "CommonExtension 在 AGP 9.0 中行为变化",
            priority = RiskLevel.P2_MEDIUM
        ),
        ChecklistItem(
            id = "p2-2",
            category = "New DSL",
            title = "适配新 DSL 语法",
            description = "AGP 9.0 引入了新的 DSL 语法",
            priority = RiskLevel.P2_MEDIUM
        ),
        ChecklistItem(
            id = "p2-3",
            category = "CI",
            title = "更新 CI 配置",
            description = "CI 中 AGP/Gradle 版本需要同步更新",
            priority = RiskLevel.P2_MEDIUM
        )
    )

    private fun toggleChecklist(itemId: String, checked: Boolean) {
        val currentItems = _state.value.checklistItems.toMutableList()
        val index = currentItems.indexOfFirst { it.id == itemId }
        if (index != -1) {
            currentItems[index] = currentItems[index].copy(isChecked = checked)
            val completedCount = currentItems.count { it.isChecked }
            _state.value = _state.value.copy(
                checklistItems = currentItems,
                completedChecklistCount = completedCount
            )
        }
    }

    // ============================================================
    // Effects / 副作用处理
    // ============================================================

    private fun copyCode(code: String) {
        viewModelScope.launch {
            _effect.emit(AGP90MigrationEffect.CopyToClipboard("Code"))
            _effect.emit(AGP90MigrationEffect.ShowSnackbar("Copied to clipboard / 已复制到剪贴板"))
        }
    }

    private fun dismissSnackbar() {
        _state.value = _state.value.copy(snackbarMessage = null)
    }

    private fun resetAll() {
        _state.value = AGP90MigrationState.Initial
        loadAllData()
    }
}
