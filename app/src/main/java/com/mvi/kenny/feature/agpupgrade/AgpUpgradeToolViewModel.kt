package com.mvi.kenny.feature.agpupgrade

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlin.math.absoluteValue

/**
 * ============================================================
 * ViewModel for AGP Upgrade Tool (PRD-200)
 * AGP 升级工具的 ViewModel
 * ============================================================
 *
 * Implements MVI pattern:
 * — State is the single source of truth (UI data)
 * — Intent represents user actions (events)
 * — Effect represents one-time side effects (navigation, toasts, etc.)
 *
 * 设计: MVI 架构 — State 是唯一数据源, Intent 表示用户动作,
 *       Effect 表示一次性副作用（导航、Toast 等）
 */
class AgpUpgradeToolViewModel : ViewModel() {

    private val _state = MutableStateFlow(AgpUpgradeToolState())
    val state: StateFlow<AgpUpgradeToolState> = _state.asStateFlow()

    private val _effect = Channel<AgpUpgradeToolEffect>(Channel.BUFFERED)
    val effect: Flow<AgpUpgradeToolEffect> = _effect.receiveAsFlow()

    // ============================================================
    // Built-in Version Data / 内置版本数据
    // ============================================================

    /**
     * AGP version compatibility matrix
     * AGP 版本兼容性矩阵
     */
    private val agpGradleCompatibility = mapOf(
        "8.5.0" to "8.7",
        "8.6.0" to "8.7",
        "8.7.0" to "8.9",
        "8.8.0" to "8.10",
        "8.9.0" to "8.11",
        "9.0.0" to "8.7",
        "9.1.0" to "8.9",
        "9.2.0" to "8.11",
        "9.3.0" to "8.11"
    )

    /**
     * Available AGP versions for selection
     * 可选择的 AGP 版本列表
     */
    val availableAGPVersions = listOf(
        "8.5.0", "8.6.0", "8.7.0", "8.8.0", "8.9.0",
        "9.0.0", "9.1.0", "9.2.0"
    )

    /**
     * Built-in breaking changes data
     * 内置破坏性变更数据
     */
    private val breakingChangesData = listOf(
        // AGP 9.0 Breaking Changes
        BreakingChangeItem(
            id = "agp9-001",
            category = "AGP 9.0",
            title = "Kotlin 2.0+ Required",
            titleZh = "强制要求 Kotlin 2.0+",
            description = "AGP 9.0 requires Kotlin 2.0 or later. Kotlin 1.x is no longer supported.",
            descriptionZh = "AGP 9.0 要求 Kotlin 2.0 或更高版本。Kotlin 1.x 不再受支持。",
            codeBefore = "kotlin_version = '1.9.24'",
            codeAfter = "kotlin_version = '2.0.0'",
            riskLevel = RiskLevel.P0,
            affectedScope = "build.gradle.kts",
            migrationGuide = "Upgrade Kotlin version to 2.0.0 or later in your root build.gradle.kts"
        ),
        BreakingChangeItem(
            id = "agp9-002",
            category = "AGP 9.0",
            title = "NDK Version Required",
            titleZh = "强制要求 NDK 版本",
            description = "Projects with NDK must specify ndkVersion explicitly. AGP no longer bundles a default NDK.",
            descriptionZh = "含有 NDK 的项目必须显式指定 ndkVersion。AGP 不再捆绑默认 NDK。",
            codeBefore = "android { ndk { abiFilters += listOf(\"arm64-v8a\") } }",
            codeAfter = "android { ndkVersion = \"27.0.0\" }",
            riskLevel = RiskLevel.P1,
            affectedScope = "android {} block",
            migrationGuide = "Add explicit ndkVersion to your android {} block"
        ),
        BreakingChangeItem(
            id = "agp9-003",
            category = "AGP 9.0",
            title = "namespace Required for Java Modules",
            titleZh = "Java 模块强制要求 namespace",
            description = "Java-only modules must declare a namespace. This was optional in AGP 8.x.",
            descriptionZh = "纯 Java 模块必须声明 namespace。在 AGP 8.x 中这是可选的。",
            codeBefore = "// No namespace declaration",
            codeAfter = "android { namespace = \"com.example.mymodule\" }",
            riskLevel = RiskLevel.P1,
            affectedScope = "android {} block",
            migrationGuide = "Add namespace to all android {} blocks"
        ),
        // AGP 9.1 Breaking Changes
        BreakingChangeItem(
            id = "agp9-004",
            category = "AGP 9.1",
            title = "Java 17 Toolchain Required",
            titleZh = "强制要求 Java 17 Toolchain",
            description = "AGP 9.1 requires JDK 17 or later for the Android build process.",
            descriptionZh = "AGP 9.1 要求 JDK 17 或更高版本用于 Android 构建过程。",
            codeBefore = "java { toolchain { languageVersion.set(JavaLanguageVersion.of(11)) } }",
            codeAfter = "java { toolchain { languageVersion.set(JavaLanguageVersion.of(17)) } }",
            riskLevel = RiskLevel.P1,
            affectedScope = "build.gradle.kts",
            migrationGuide = "Update Java toolchain to version 17"
        ),
        BreakingChangeItem(
            id = "agp9-005",
            category = "AGP 9.1",
            title = "Kotlin 2.1+ Required",
            titleZh = "强制要求 Kotlin 2.1+",
            description = "AGP 9.1 requires Kotlin 2.1 or later.",
            descriptionZh = "AGP 9.1 要求 Kotlin 2.1 或更高版本。",
            codeBefore = "kotlin_version = '2.0.0'",
            codeAfter = "kotlin_version = '2.1.0'",
            riskLevel = RiskLevel.P1,
            affectedScope = "build.gradle.kts",
            migrationGuide = "Upgrade Kotlin version to 2.1.0 or later"
        ),
        // AGP 9.2 Breaking Changes
        BreakingChangeItem(
            id = "agp9-006",
            category = "AGP 9.2",
            title = "Gradle 8.11+ Required",
            titleZh = "强制要求 Gradle 8.11+",
            description = "AGP 9.2 requires Gradle 8.11 or later. This is a hard requirement.",
            descriptionZh = "AGP 9.2 要求 Gradle 8.11 或更高版本。这是硬性要求。",
            codeBefore = "distributionUrl=https\\://services.gradle.org/distributions/gradle-8.9-bin.zip",
            codeAfter = "distributionUrl=https\\://services.gradle.org/distributions/gradle-8.11-bin.zip",
            riskLevel = RiskLevel.P0,
            affectedScope = "gradle/wrapper/gradle-wrapper.properties",
            migrationGuide = "Update gradle-wrapper.properties to use Gradle 8.11 or later"
        ),
        BreakingChangeItem(
            id = "agp9-007",
            category = "AGP 9.2",
            title = "Compose 1.12.0 compileSdk 37",
            titleZh = "Compose 1.12.0 需要 compileSdk 37",
            description = "Compose 1.12.0 requires compileSdk 37. Projects using Compose 1.12.0 must upgrade compileSdk.",
            descriptionZh = "Compose 1.12.0 要求 compileSdk 37。使用 Compose 1.12.0 的项目必须升级 compileSdk。",
            codeBefore = "android { compileSdk = 36 }",
            codeAfter = "android { compileSdk = 37 }",
            riskLevel = RiskLevel.P0,
            affectedScope = "android {} block",
            migrationGuide = "Update compileSdk to 37 in your android {} block"
        ),
        // compileSdk 37 Breaking Changes
        BreakingChangeItem(
            id = "sdk37-001",
            category = "compileSdk 37",
            title = "New Local Network Permission",
            titleZh = "新增局域网权限",
            description = "Android 17 requires ACCESS_LOCAL_NETWORK permission for apps accessing local network.",
            descriptionZh = "Android 17 要求访问局域网的 App 申请 ACCESS_LOCAL_NETWORK 权限。",
            riskLevel = RiskLevel.P1,
            affectedScope = "AndroidManifest.xml",
            migrationGuide = "Add ACCESS_LOCAL_NETWORK permission if your app accesses local network"
        ),
        BreakingChangeItem(
            id = "sdk37-002",
            category = "compileSdk 37",
            title = "Photo Picker Min SDK Increase",
            titleZh = "Photo Picker 最低 SDK 提高",
            description = "The photo picker API behavior changes for apps targeting API 37+.",
            descriptionZh = "Photo Picker API 行为对 targeting API 37+ 的 App 发生变化。",
            riskLevel = RiskLevel.P2,
            affectedScope = "Photo picker usage",
            migrationGuide = "Review photo picker usage in your app for API 37 compatibility"
        ),
        BreakingChangeItem(
            id = "sdk37-003",
            category = "compileSdk 37",
            title = "AlarmManager Restrictions Tightened",
            titleZh = "AlarmManager 限制收紧",
            description = "Apps targeting API 37+ have stricter constraints on exact alarms.",
            descriptionZh = "Targeting API 37+ 的 App 使用精确闹钟有更严格的限制。",
            riskLevel = RiskLevel.P1,
            affectedScope = "AlarmManager usage",
            migrationGuide = "Review AlarmManager usage and migrate to inexact alarms where possible"
        )
    )

    /**
     * Built-in AGP upgrade path data
     * 内置 AGP 升级路径数据
     */
    private fun buildUpgradePath(fromVersion: String, toVersion: String): List<UpgradeStep> {
        val versions = availableAGPVersions
        val fromIdx = versions.indexOf(fromVersion)
        val toIdx = versions.indexOf(toVersion)
        if (fromIdx < 0 || toIdx < 0 || fromIdx >= toIdx) return emptyList()

        val steps = mutableListOf<UpgradeStep>()
        for (i in fromIdx until toIdx) {
            val from = versions[i]
            val to = versions[i + 1]
            steps.add(
                UpgradeStep(
                    fromVersion = from,
                    toVersion = to,
                    breakingChanges = getBreakingChangesForVersion(to),
                    riskLevel = getRiskForVersion(to),
                    keyNotes = getKeyNotesForVersion(to)
                )
            )
        }
        return steps
    }

    private fun getBreakingChangesForVersion(version: String): List<String> {
        return when {
            version.startsWith("9.2") -> listOf(
                "Gradle 8.11+ required",
                "Compose 1.12.0 requires compileSdk 37",
                "Minimum Kotlin 2.1+"
            )
            version.startsWith("9.1") -> listOf(
                "JDK 17 required",
                "Kotlin 2.1+ required"
            )
            version.startsWith("9.0") -> listOf(
                "Kotlin 2.0+ required",
                "Explicit ndkVersion required",
                "Java modules need namespace"
            )
            else -> emptyList()
        }
    }

    private fun getRiskForVersion(version: String): RiskLevel {
        return when {
            version.startsWith("9.2") -> RiskLevel.P0
            version.startsWith("9.1") -> RiskLevel.P1
            version.startsWith("9.0") -> RiskLevel.P1
            else -> RiskLevel.P2
        }
    }

    private fun getKeyNotesForVersion(version: String): String {
        return when {
            version.startsWith("9.2") -> "Compose 1.12.0 稳定版关键升级节点"
            version.startsWith("9.1") -> "Kotlin 2.1 和 Java 17 强制要求"
            version.startsWith("9.0") -> "Kotlin 2.0 强制要求，告别 Kotlin 1.x"
            else -> ""
        }
    }

    // ============================================================
    // Intent Handler / Intent 处理器
    // ============================================================

    fun sendIntent(intent: AgpUpgradeToolIntent) {
        when (intent) {
            is AgpUpgradeToolIntent.SwitchTab -> switchTab(intent.tab)
            is AgpUpgradeToolIntent.UpdateScannerPath -> updateScannerPath(intent.path)
            is AgpUpgradeToolIntent.StartScan -> startScan()
            is AgpUpgradeToolIntent.ClearScanResult -> clearScanResult()
            is AgpUpgradeToolIntent.SelectCurrentVersion -> selectCurrentVersion(intent.version)
            is AgpUpgradeToolIntent.NextWizardStep -> nextWizardStep()
            is AgpUpgradeToolIntent.PrevWizardStep -> prevWizardStep()
            is AgpUpgradeToolIntent.GenerateConfig -> generateConfig()
            is AgpUpgradeToolIntent.UpdateWizardPath -> updateWizardPath(intent.path)
            is AgpUpgradeToolIntent.UpdateSearchQuery -> updateSearchQuery(intent.query)
            is AgpUpgradeToolIntent.FilterByRisk -> filterByRisk(intent.risk)
            is AgpUpgradeToolIntent.FilterByCategory -> filterByCategory(intent.category)
            is AgpUpgradeToolIntent.ToggleChangeExpanded -> toggleChangeExpanded(intent.id)
            is AgpUpgradeToolIntent.UpdateErrorLog -> updateErrorLog(intent.log)
            is AgpUpgradeToolIntent.AnalyzeLog -> analyzeLog()
            is AgpUpgradeToolIntent.ClearDiagnosis -> clearDiagnosis()
            is AgpUpgradeToolIntent.UpdateCompliancePath -> updateCompliancePath(intent.path)
            is AgpUpgradeToolIntent.RunComplianceCheck -> runComplianceCheck()
            is AgpUpgradeToolIntent.ExportComplianceReport -> exportComplianceReport()
            is AgpUpgradeToolIntent.ClearComplianceReport -> clearComplianceReport()
        }
    }

    // ============================================================
    // Tab Navigation / Tab 切换
    // ============================================================

    private fun switchTab(tab: ToolTab) {
        _state.update { it.copy(currentTab = tab) }
    }

    // ============================================================
    // Scanner / 扫描器
    // ============================================================

    private fun updateScannerPath(path: String) {
        _state.update { it.copy(scanner = it.scanner.copy(projectPath = path)) }
    }

    private fun startScan() {
        val path = _state.value.scanner.projectPath
        if (path.isBlank()) {
            viewModelScope.launch {
                _effect.send(AgpUpgradeToolEffect.ShowToast("Please enter project path / 请输入项目路径"))
            }
            return
        }

        _state.update { it.copy(scanner = it.scanner.copy(isScanning = true, error = null)) }

        viewModelScope.launch {
            // Simulate async scan / 模拟异步扫描
            delay(1500)

            // Simulate scan result / 生成模拟扫描结果
            val simulatedResult = simulateScan(path)
            _state.update {
                it.copy(
                    scanner = it.scanner.copy(
                        isScanning = false,
                        scanResult = simulatedResult
                    )
                )
            }
        }
    }

    private fun simulateScan(path: String): ScanResult {
        // Simulate detecting various project states
        // 模拟检测不同项目状态
        val versionInfo = ProjectVersionInfo(
            agpVersion = "8.7.0",
            gradleVersion = "8.9",
            kotlinVersion = "2.0.0",
            composeVersion = "1.6.0",
            compileSdk = 36,
            targetSdk = 36,
            minSdk = 24
        )

        val blockingIssues = mutableListOf<String>()
        val recommendations = mutableListOf<String>()

        // Check AGP version / 检查 AGP 版本
        if (versionInfo.agpVersion < "9.2.0") {
            blockingIssues.add("AGP ${versionInfo.agpVersion} < 9.2.0 — Required for Compose 1.12.0")
            recommendations.add("Upgrade to AGP 9.2.0 for Compose 1.12.0 compatibility")
        }

        // Check compileSdk / 检查 compileSdk
        if (versionInfo.compileSdk < 37) {
            blockingIssues.add("compileSdk ${versionInfo.compileSdk} < 37 — Required by Compose 1.12.0")
            recommendations.add("Upgrade compileSdk to 37 for Compose 1.12.0")
        }

        // Check Gradle version / 检查 Gradle 版本
        val requiredGradle = agpGradleCompatibility["9.2.0"] ?: "8.11"
        if (versionInfo.gradleVersion < requiredGradle) {
            blockingIssues.add("Gradle ${versionInfo.gradleVersion} < $requiredGradle — Required by AGP 9.2.0")
            recommendations.add("Upgrade Gradle to $requiredGradle or later")
        }

        val riskLevel = when {
            blockingIssues.size >= 2 -> RiskLevel.P0
            blockingIssues.isNotEmpty() -> RiskLevel.P1
            else -> RiskLevel.Pass
        }

        return ScanResult(
            projectPath = path,
            versionInfo = versionInfo,
            riskLevel = riskLevel,
            upgradeRecommendations = recommendations,
            blockingIssues = blockingIssues,
            compatibleVersions = CompatibleVersions(
                recommendedAGP = "9.2.0",
                recommendedGradle = "8.11",
                recommendedKotlin = "2.1.0"
            )
        )
    }

    private fun clearScanResult() {
        _state.update {
            it.copy(scanner = ScannerState())
        }
    }

    // ============================================================
    // Wizard / 升级向导
    // ============================================================

    private fun updateWizardPath(path: String) {
        _state.update {
            it.copy(wizard = it.wizard.copy(currentAGPVersion = path))
        }
    }

    private fun selectCurrentVersion(version: String) {
        _state.update {
            it.copy(wizard = it.wizard.copy(currentAGPVersion = version))
        }
    }

    private fun nextWizardStep() {
        val current = _state.value.wizard.currentStep
        val next = when (current) {
            WizardStep.SelectCurrent -> WizardStep.ShowPath
            WizardStep.ShowPath -> WizardStep.ShowConfig
            WizardStep.ShowConfig -> WizardStep.CICommands
            WizardStep.CICommands -> return
        }

        if (current == WizardStep.SelectCurrent) {
            // Build upgrade path when moving to ShowPath / 进入 ShowPath 时构建升级路径
            val path = buildUpgradePath(
                _state.value.wizard.currentAGPVersion,
                _state.value.wizard.targetAGPVersion
            )
            _state.update {
                it.copy(wizard = it.wizard.copy(upgradePath = path))
            }
        }

        if (current == WizardStep.ShowPath) {
            // Generate config when moving to ShowConfig / 进入 ShowConfig 时生成配置
            generateConfigInternal()
        }

        _state.update {
            it.copy(wizard = it.wizard.copy(currentStep = next))
        }
    }

    private fun prevWizardStep() {
        val current = _state.value.wizard.currentStep
        val prev = when (current) {
            WizardStep.SelectCurrent -> return
            WizardStep.ShowPath -> WizardStep.SelectCurrent
            WizardStep.ShowConfig -> WizardStep.ShowPath
            WizardStep.CICommands -> WizardStep.ShowConfig
        }
        _state.update {
            it.copy(wizard = it.wizard.copy(currentStep = prev))
        }
    }

    private fun generateConfig() {
        generateConfigInternal()
    }

    private fun generateConfigInternal() {
        val wizard = _state.value.wizard
        val recommendedGradle = agpGradleCompatibility[wizard.targetAGPVersion] ?: "8.11"

        val gradleWrapper = """
            |# gradle/wrapper/gradle-wrapper.properties
            |distributionBase=GRADLE_USER_HOME
            |distributionPath=wrapper/dists
            |distributionUrl=https\://services.gradle.org/distributions/gradle-${recommendedGradle}-bin.zip
            |networkTimeout=10000
            |validateDistributionUrl=true
            |zipStoreBase=GRADLE_USER_HOME
            |zipStorePath=wrapper/dists
        """.trimMargin()

        val buildGradle = """
            |// build.gradle.kts (root)
            |plugins {
            |    id("com.android.application") version "${wizard.targetAGPVersion}" apply false
            |    id("org.jetbrains.kotlin.android") version "2.1.0" apply false
            |    id("org.jetbrains.kotlin.plugin.compose") version "2.1.0" apply false
            |}
            |
            |// app/build.gradle.kts
            |android {
            |    namespace = "com.example.app"
            |    compileSdk = 37
            |    defaultConfig {
            |        minSdk = 24
            |        targetSdk = 37
            |    }
            |}
        """.trimMargin()

        val ciCommands = """
            |#!/bin/bash
            |# CI Validation Script for AGP ${wizard.targetAGPVersion} Upgrade
            |
            |# 1. Verify Gradle version
            |./gradlew --version | grep "Gradle ${recommendedGradle}"
            |
            |# 2. Verify AGP version
            |./gradlew --version | grep "Android Gradle Plugin: ${wizard.targetAGPVersion}"
            |
            |# 3. Run full build
            |./gradlew assembleDebug --stacktrace
            |
            |# 4. Run Compose compiler reports
            |./gradlew lintDebug
        """.trimMargin()

        _state.update {
            it.copy(
                wizard = it.wizard.copy(
                    generatedGradleWrapper = gradleWrapper,
                    generatedBuildGradle = buildGradle,
                    generatedCICommands = ciCommands
                )
            )
        }
    }

    // ============================================================
    // Breaking Changes / 变更清单
    // ============================================================

    private fun updateSearchQuery(query: String) {
        _state.update { it.copy(changes = it.changes.copy(searchQuery = query)) }
        applyChangesFilter()
    }

    private fun filterByRisk(risk: RiskLevel?) {
        _state.update { it.copy(changes = it.changes.copy(selectedRiskFilter = risk)) }
        applyChangesFilter()
    }

    private fun filterByCategory(category: String?) {
        _state.update { it.copy(changes = it.changes.copy(selectedCategoryFilter = category)) }
        applyChangesFilter()
    }

    private fun applyChangesFilter() {
        val changes = _state.value.changes
        var filtered = breakingChangesData

        // Apply search query / 应用搜索查询
        if (changes.searchQuery.isNotBlank()) {
            val q = changes.searchQuery.lowercase()
            filtered = filtered.filter {
                it.title.lowercase().contains(q) ||
                        it.titleZh.contains(q) ||
                        it.description.lowercase().contains(q) ||
                        it.category.lowercase().contains(q)
            }
        }

        // Apply risk filter / 应用风险过滤
        changes.selectedRiskFilter?.let { risk ->
            filtered = filtered.filter { it.riskLevel == risk }
        }

        // Apply category filter / 应用分类过滤
        changes.selectedCategoryFilter?.let { cat ->
            filtered = filtered.filter { it.category == cat }
        }

        _state.update { it.copy(changes = it.changes.copy(filteredChanges = filtered)) }
    }

    private fun toggleChangeExpanded(id: String) {
        _state.update {
            it.copy(
                changes = it.changes.copy(
                    expandedChangeId = if (it.changes.expandedChangeId == id) null else id
                )
            )
        }
    }

    init {
        // Initialize breaking changes data / 初始化变更清单数据
        _state.update {
            it.copy(
                changes = it.changes.copy(
                    breakingChanges = breakingChangesData,
                    filteredChanges = breakingChangesData
                )
            )
        }
    }

    // ============================================================
    // Diagnostics / 诊断工具
    // ============================================================

    private fun updateErrorLog(log: String) {
        _state.update { it.copy(diagnostics = it.diagnostics.copy(errorLog = log)) }
    }

    private fun analyzeLog() {
        val log = _state.value.diagnostics.errorLog
        if (log.isBlank()) {
            viewModelScope.launch {
                _effect.send(AgpUpgradeToolEffect.ShowToast("Please paste error log / 请粘贴错误日志"))
            }
            return
        }

        _state.update { it.copy(diagnostics = it.diagnostics.copy(isAnalyzing = true, error = null)) }

        viewModelScope.launch {
            delay(800) // Simulate analysis / 模拟分析过程
            val result = analyzeErrorLog(log)
            _state.update {
                it.copy(
                    diagnostics = it.diagnostics.copy(
                        isAnalyzing = false,
                        diagnosisResult = result
                    )
                )
            }
        }
    }

    private fun analyzeErrorLog(log: String): DiagnosisResult {
        // Pattern matching for common AGP/Gradle errors
        // 常见 AGP/Gradle 错误的模式匹配
        return when {
            log.contains("unsupported Gradle version") || log.contains("requires Gradle") -> {
                DiagnosisResult(
                    category = DiagnosticCategory.GRADLE_VERSION_MISMATCH,
                    summary = "Gradle version mismatch detected",
                    summaryZh = "检测到 Gradle 版本不匹配",
                    rootCause = "The project requires a different Gradle version than the one currently installed",
                    rootCauseZh = "项目要求的 Gradle 版本与当前安装的版本不匹配",
                    fixSuggestions = listOf(
                        "Update gradle-wrapper.properties to the required Gradle version",
                        "Run: ./gradlew wrapper --gradle-version=<required-version>",
                        "Check AGP compatibility matrix for required Gradle version"
                    ),
                    fixSuggestionsZh = listOf(
                        "更新 gradle-wrapper.properties 中的 Gradle 版本",
                        "运行: ./gradlew wrapper --gradle-version=<所需版本>",
                        "查看 AGP 兼容性矩阵确认所需 Gradle 版本"
                    ),
                    referenceLinks = listOf(
                        "https://developer.android.com/build/gradle-plugin-version-compatibility"
                    )
                )
            }
            log.contains("requires Android Gradle Plugin") || log.contains("AGP version") -> {
                DiagnosisResult(
                    category = DiagnosticCategory.AGP_VERSION_INCOMPATIBLE,
                    summary = "AGP version incompatibility",
                    summaryZh = "AGP 版本不兼容",
                    rootCause = "The current AGP version is not compatible with the project configuration or dependencies",
                    rootCauseZh = "当前 AGP 版本与项目配置或依赖不兼容",
                    fixSuggestions = listOf(
                        "Check build.gradle.kts for plugin versions",
                        "Update AGP to a compatible version",
                        "Review dependency requirements in build files"
                    ),
                    fixSuggestionsZh = listOf(
                        "检查 build.gradle.kts 中的插件版本",
                        "更新 AGP 到兼容版本",
                        "审查构建文件中的依赖要求"
                    ),
                    referenceLinks = listOf(
                        "https://developer.android.com/build/gradle-plugin-version-compatibility"
                    )
                )
            }
            log.contains("compileSdkVersion") || log.contains("requires compileSdk") -> {
                DiagnosisResult(
                    category = DiagnosticCategory.COMPILE_SDK_TOO_LOW,
                    summary = "compileSdk version too low",
                    summaryZh = "compileSdk 版本过低",
                    rootCause = "A dependency requires a higher compileSdk version than currently set",
                    rootCauseZh = "某个依赖要求比当前设置更高的 compileSdk 版本",
                    fixSuggestions = listOf(
                        "Update compileSdk to the required version (typically 37 for Compose 1.12.0)",
                        "Update targetSdk to match or exceed compileSdk",
                        "Review dependency requirements"
                    ),
                    fixSuggestionsZh = listOf(
                        "将 compileSdk 更新到所需版本（Compose 1.12.0 通常需要 37）",
                        "更新 targetSdk 以匹配或超过 compileSdk",
                        "审查依赖要求"
                    ),
                    referenceLinks = listOf(
                        "https://developer.android.com/studio/releases#api-levels"
                    )
                )
            }
            log.contains("compose") && (log.contains("version") || log.contains("dependency")) -> {
                DiagnosisResult(
                    category = DiagnosticCategory.COMPOSE_DEPENDENCY_CONFLICT,
                    summary = "Compose dependency conflict",
                    summaryZh = "Compose 依赖冲突",
                    rootCause = "Multiple Compose versions or incompatible Compose dependencies are present",
                    rootCauseZh = "存在多个 Compose 版本或不兼容的 Compose 依赖",
                    fixSuggestions = listOf(
                        "Unify all Compose dependencies to the same version",
                        "Use Compose BOM to manage versions automatically",
                        "Remove duplicate Compose artifact declarations"
                    ),
                    fixSuggestionsZh = listOf(
                        "统一所有 Compose 依赖到相同版本",
                        "使用 Compose BOM 自动管理版本",
                        "移除重复的 Compose artifact 声明"
                    ),
                    referenceLinks = listOf(
                        "https://developer.android.com/develop/ui/compose/setup"
                    )
                )
            }
            else -> {
                DiagnosisResult(
                    category = DiagnosticCategory.UNKNOWN,
                    summary = "Unknown error pattern",
                    summaryZh = "未知错误模式",
                    rootCause = "The error pattern could not be automatically identified",
                    rootCauseZh = "无法自动识别错误模式",
                    fixSuggestions = listOf(
                        "Review the error message carefully",
                        "Check Google search for the specific error",
                        "Consult AGP and Gradle release notes"
                    ),
                    fixSuggestionsZh = listOf(
                        "仔细审查错误信息",
                        "使用 Google 搜索特定错误",
                        "查阅 AGP 和 Gradle 发布说明"
                    ),
                    referenceLinks = listOf(
                        "https://developer.android.com/build/gradle-plugin-version-compatibility",
                        "https://docs.gradle.org/current/release-notes.html"
                    )
                )
            }
        }
    }

    private fun clearDiagnosis() {
        _state.update {
            it.copy(diagnostics = DiagnosticsState())
        }
    }

    // ============================================================
    // Compliance / 合规检测
    // ============================================================

    private fun updateCompliancePath(path: String) {
        _state.update { it.copy(compliance = it.compliance.copy(projectPath = path)) }
    }

    private fun runComplianceCheck() {
        val path = _state.value.compliance.projectPath
        if (path.isBlank()) {
            viewModelScope.launch {
                _effect.send(AgpUpgradeToolEffect.ShowToast("Please enter project path / 请输入项目路径"))
            }
            return
        }

        _state.update {
            it.copy(
                compliance = it.compliance.copy(
                    isChecking = true,
                    logOutput = "Starting compliance check...\n",
                    error = null
                )
            )
        }

        viewModelScope.launch {
            // Simulate compliance check with progress output
            // 模拟带进度输出的合规检测
            val checkItems = mutableListOf<ComplianceCheckItem>()

            delay(500)
            _state.update {
                it.copy(compliance = it.compliance.copy(
                    logOutput = it.compliance.logOutput + "[1/4] Checking compileSdk version...\n"
                ))
            }

            delay(700)
            val compileSdkCheck = ComplianceCheckItem(
                name = "compileSdk >= 37",
                nameZh = "compileSdk >= 37",
                checkRule = "compileSdk >= 37 for Compose 1.12.0",
                actualValue = "36",
                expectedValue = "37",
                status = ComplianceStatus.NonCompliant,
                fixSuggestion = "Set compileSdk = 37 in android {} block",
                fixSuggestionZh = "在 android {} 块中设置 compileSdk = 37"
            )
            checkItems.add(compileSdkCheck)

            _state.update {
                it.copy(compliance = it.compliance.copy(
                    logOutput = it.compliance.logOutput + "[2/4] Checking AGP version...\n"
                ))
            }

            delay(700)
            val agpCheck = ComplianceCheckItem(
                name = "AGP >= 9.2.0",
                nameZh = "AGP >= 9.2.0",
                checkRule = "AGP >= 9.2.0 for Compose 1.12.0",
                actualValue = "8.7.0",
                expectedValue = "9.2.0",
                status = ComplianceStatus.NonCompliant,
                fixSuggestion = "Update Android Gradle Plugin to 9.2.0",
                fixSuggestionZh = "将 Android Gradle Plugin 更新到 9.2.0"
            )
            checkItems.add(agpCheck)

            _state.update {
                it.copy(compliance = it.compliance.copy(
                    logOutput = it.compliance.logOutput + "[3/4] Checking Gradle version...\n"
                ))
            }

            delay(700)
            val gradleCheck = ComplianceCheckItem(
                name = "Gradle >= 8.11",
                nameZh = "Gradle >= 8.11",
                checkRule = "Gradle >= 8.11 required for AGP 9.2.0",
                actualValue = "8.9",
                expectedValue = "8.11",
                status = ComplianceStatus.NonCompliant,
                fixSuggestion = "Update Gradle wrapper to 8.11",
                fixSuggestionZh = "更新 Gradle wrapper 到 8.11"
            )
            checkItems.add(gradleCheck)

            _state.update {
                it.copy(compliance = it.compliance.copy(
                    logOutput = it.compliance.logOutput + "[4/4] Checking Kotlin version...\n"
                ))
            }

            delay(700)
            val kotlinCheck = ComplianceCheckItem(
                name = "Kotlin >= 2.1.0",
                nameZh = "Kotlin >= 2.1.0",
                checkRule = "Kotlin >= 2.1.0 required for AGP 9.2.0",
                actualValue = "2.0.0",
                expectedValue = "2.1.0",
                status = ComplianceStatus.Partial,
                fixSuggestion = "Update Kotlin version to 2.1.0 or later",
                fixSuggestionZh = "将 Kotlin 版本更新到 2.1.0 或更高"
            )
            checkItems.add(kotlinCheck)

            delay(500)
            val overallStatus = when {
                checkItems.all { it.status == ComplianceStatus.Compliant } -> ComplianceStatus.Compliant
                checkItems.any { it.status == ComplianceStatus.NonCompliant } -> ComplianceStatus.NonCompliant
                else -> ComplianceStatus.Partial
            }

            val report = ComplianceReport(
                projectPath = path,
                overallStatus = overallStatus,
                checkItems = checkItems
            )

            _state.update {
                it.copy(
                    compliance = it.compliance.copy(
                        isChecking = false,
                        report = report,
                        logOutput = it.compliance.logOutput + "\n✅ Compliance check complete!\n"
                    )
                )
            }
        }
    }

    private fun exportComplianceReport() {
        val report = _state.value.compliance.report ?: return
        viewModelScope.launch {
            val json = buildString {
                appendLine("{")
                appendLine("  \"projectPath\": \"${report.projectPath}\",")
                appendLine("  \"overallStatus\": \"${report.overallStatus}\",")
                appendLine("  \"timestamp\": ${report.reportTimestamp},")
                appendLine("  \"checkItems\": [")
                report.checkItems.forEachIndexed { index, item ->
                    appendLine("    {")
                    appendLine("      \"name\": \"${item.name}\",")
                    appendLine("      \"actualValue\": \"${item.actualValue}\",")
                    appendLine("      \"expectedValue\": \"${item.expectedValue}\",")
                    appendLine("      \"status\": \"${item.status}\"")
                    appendLine("    }${if (index < report.checkItems.lastIndex) "," else ""}")
                }
                appendLine("  ]")
                appendLine("}")
            }
            _effect.send(AgpUpgradeToolEffect.ExportReport(json, "compliance-report-${System.currentTimeMillis()}.json"))
        }
    }

    private fun clearComplianceReport() {
        _state.update {
            it.copy(compliance = ComplianceState())
        }
    }
}
