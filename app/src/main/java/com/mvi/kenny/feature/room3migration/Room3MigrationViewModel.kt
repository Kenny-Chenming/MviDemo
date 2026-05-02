package com.mvi.kenny.feature.room3migration

// ================================================================
// Room3MigrationViewModel — Room 3.0 破坏性变更迁移工具包 MVI ViewModel
// ================================================================
// ViewModel for Room 3.0 Migration Toolkit.
//
// PRD-212: Room 3.0 破坏性变更迁移工具包
// Implements MVI pattern: Intent → ViewModel → State/Effect
//
// Key responsibilities:
//   - Scan project for KAPT usages and Room 2.x patterns
//   - Generate KSP migration diffs (build.gradle.kts)
//   - Identify non-suspend DAO functions
//   - Generate package rename batch replacements
//   - Provide Driver API migration guides per platform
//   - Generate CI compliance checks and migration plans
//   - Expose one-time Effects (snackbar, clipboard, share)
// ================================================================

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
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
 * Room3MigrationViewModel — Room 3.0 迁移工具 ViewModel
 * ============================================================
 * Manages the Room3MigrationState and processes Room3MigrationIntent.
 *
 * Implements the full Room 3.0 migration workflow:
 *   Tab 1 (Risk Scan)     → Detect KAPT usages + breaking changes
 *   Tab 2 (KSP Migrate)   → Generate build.gradle.kts diff
 *   Tab 3 (DAO Transform) → Identify non-suspend DAOs + @RawQuery templates
 *   Tab 4 (Package+Driver) → Package rename + Driver API guides
 *   Tab 5 (Verify)        → CI compliance + migration plan + decision guide
 *
 * @see Room3MigrationState
 * @see Room3MigrationIntent
 * @see Room3MigrationEffect
 */
class Room3MigrationViewModel : ViewModel() {

    // ─────────────────────────────────────────────────────────────
    // State — Single source of truth, exposed as immutable StateFlow
    // ─────────────────────────────────────────────────────────────
    private val _state = MutableStateFlow(Room3MigrationState.Initial)
    val state: StateFlow<Room3MigrationState> = _state.asStateFlow()

    // ─────────────────────────────────────────────────────────────
    // Effect — One-time events via SharedFlow
    // ─────────────────────────────────────────────────────────────
    private val _effect = MutableSharedFlow<Room3MigrationEffect>()
    val effect = _effect.asSharedFlow()

    // ─────────────────────────────────────────────────────────────
    // Public API for UI to send intents
    // ─────────────────────────────────────────────────────────────
    fun sendIntent(intent: Room3MigrationIntent) {
        viewModelScope.launch {
            when (intent) {
                is Room3MigrationIntent.SelectTab -> handleSelectTab(intent.index)
                is Room3MigrationIntent.SelectScanTab -> handleSelectScanTab(intent.tab)
                is Room3MigrationIntent.SetProjectPath -> handleSetProjectPath(intent.path)
                is Room3MigrationIntent.SelectGradleFileType -> handleSelectGradleFileType(intent.type)
                is Room3MigrationIntent.PasteGradleContent -> handlePasteGradleContent(intent.content)
                is Room3MigrationIntent.ImportProject -> handleImportProject(intent.simulate)
                is Room3MigrationIntent.RunFullScan -> handleRunFullScan()
                is Room3MigrationIntent.GenerateKspMigration -> handleGenerateKspMigration()
                is Room3MigrationIntent.CopyDaoMigration -> handleCopyDaoMigration(intent.dao)
                is Room3MigrationIntent.RunPackageReplacement -> handleRunPackageReplacement()
                is Room3MigrationIntent.GenerateDriverGuide -> handleGenerateDriverGuide(intent.platform)
                is Room3MigrationIntent.SelectPlatform -> handleSelectPlatform(intent.platform)
                is Room3MigrationIntent.SetKspVersion -> handleSetKspVersion(intent.version)
                is Room3MigrationIntent.SetupCiCompliance -> handleSetupCiCompliance()
                is Room3MigrationIntent.GenerateMigrationPlan -> handleGenerateMigrationPlan()
                is Room3MigrationIntent.ExportReport -> handleExportReport()
                is Room3MigrationIntent.DismissSnackbar -> handleDismissSnackbar()
                is Room3MigrationIntent.ResetAll -> handleResetAll()
            }
        }
    }

    // ─────────────────────────────────────────────────────────────
    // Intent Handlers
    // ─────────────────────────────────────────────────────────────

    private fun handleSelectTab(index: Int) {
        _state.update { it.copy(currentTab = index) }
    }

    private fun handleSelectScanTab(tab: ScanTab) {
        _state.update { it.copy(scanTab = tab) }
    }

    private fun handleSetProjectPath(path: String) {
        _state.update { it.copy(projectPath = path) }
    }

    private fun handleSelectGradleFileType(type: GradleFileType) {
        _state.update { it.copy(gradleFileType = type) }
    }

    private fun handlePasteGradleContent(content: String) {
        _state.update { it.copy(gradleFileContent = content) }
    }

    /**
     * Import project — simulates project import with realistic mock data.
     * In a real implementation, this would parse the actual project structure.
     *
     * @param simulate If true, uses pre-defined mock data for demonstration
     */
    private suspend fun handleImportProject(simulate: Boolean) {
        if (simulate) {
            _state.update { it.copy(isSimulatingFile = true, isScanning = true) }
            // Simulate scanning delay (2 seconds)
            withContext(Dispatchers.Default) {
                delay(2000)
            }
            // Load simulated scan results
            _state.update {
                it.copy(
                    isSimulatingFile = false,
                    isScanning = false,
                    scanResults = ScanResults(
                        kaptUsages = SIMULATED_KAPT_USAGES,
                        breakingChanges = SIMULATED_BREAKING_CHANGES,
                        daoNonSuspend = SIMULATED_DAO_FUNCTIONS,
                        packageImports = SIMULATED_PACKAGE_IMPORTS,
                        riskLevel = RiskLevel.P0_CRITICAL,
                        modules = listOf("app", "data", "features:user")
                    ),
                    daoMigrations = SIMULATED_DAO_FUNCTIONS.map { dao ->
                        DaoMigration(
                            daoFunction = dao,
                            status = MigrationStatus.PENDING,
                            beforeCode = dao.migrationDiff.lines().filter { l -> l.contains("Before") || l.contains("fun ") }.joinToString("\n"),
                            afterCode = dao.migrationDiff.lines().filter { l -> l.contains("After") || l.contains("suspend") }.joinToString("\n")
                        )
                    },
                    migrationProgress = MigrationProgress(
                        kaptTotal = SIMULATED_KAPT_USAGES.size,
                        daoTotal = SIMULATED_DAO_FUNCTIONS.size,
                        packagesTotal = SIMULATED_PACKAGE_IMPORTS.sumOf { it.occurrences },
                        driverTotal = 4 // Android/iOS/JS/Desktop
                    ),
                    snackbarMessage = "✅ 模拟项目导入完成，发现 ${SIMULATED_KAPT_USAGES.size} 处 KAPT 使用"
                )
            }
        }
    }

    /**
     * Run full scan — simulates comprehensive Room 3.0 migration analysis.
     * In production, this would perform actual Gradle file parsing,
     * DAO interface analysis, and package import counting.
     */
    private suspend fun handleRunFullScan() {
        _state.update { it.copy(isScanning = true, scanProgress = 0f) }

        withContext(Dispatchers.Default) {
            // Phase 1: KAPT scan (0-25%)
            delay(500)
            _state.update { it.copy(scanProgress = 0.1f) }

            // Phase 2: DAO analysis (25-50%)
            delay(500)
            _state.update { it.copy(scanProgress = 0.35f) }

            // Phase 3: Package import scan (50-75%)
            delay(500)
            _state.update { it.copy(scanProgress = 0.6f) }

            // Phase 4: Risk level calculation (75-100%)
            delay(500)
            _state.update { it.copy(scanProgress = 0.9f) }

            delay(300)
            _state.update {
                it.copy(
                    isScanning = false,
                    scanProgress = 1f,
                    snackbarMessage = "✅ 完整扫描完成，发现 ${it.scanResults.breakingChanges.size} 个破坏性变更"
                )
            }
        }
    }

    /**
     * Generate KSP migration diff — creates build.gradle.kts before/after diff.
     * Recommends KSP version based on Room 3.0 requirements.
     */
    private suspend fun handleGenerateKspMigration() {
        _state.update { it.copy(isGeneratingKspDiff = true) }

        withContext(Dispatchers.Default) {
            delay(800)

            val kspVersion = _state.value.kspVersion
            val gradleType = _state.value.gradleFileType

            val kspDiff = if (gradleType == GradleFileType.KOTLIN_DSL) {
                KspDiff(
                    beforeContent = """// build.gradle.kts — Room 2.x (KAPT) ❌
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.kapt")  // ❌ Room 3.0 必须移除
}

dependencies {
    // ❌ Room compiler via KAPT — 迁移到 KSP
    kapt("androidx.room:room-compiler:2.6.1")
    
    // Room 运行时（保留）
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
}""",
                    afterContent = """// build.gradle.kts — Room 3.0 (KSP) ✅
// ✅ KSP version must match Kotlin version — Room 3.0 recommends 1.0.25+
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp") version "$kspVersion"  // ✅ KSP plugin
}

dependencies {
    // ✅ Room 3.0 compiler via KSP
    ksp("androidx.room:room-compiler:3.0.0-alpha01")
    
    // Room 3.0 运行时（Kotlin Multiplatform）
    implementation("androidx.room:room-runtime:3.0.0-alpha01")
    implementation("androidx.room:room-ktx:3.0.0-alpha01")
    
    // KSP plugin required for Kotlin Symbol Processing
    // Note: KSP version must be compatible with your Kotlin version.
    // Room 3.0 recommends KSP 1.0.25+ with Kotlin 1.9.24+
}""",
                    kspVersion = kspVersion,
                    roomVersion = "3.0.0-alpha01"
                )
            } else {
                KspDiff(
                    beforeContent = """// build.gradle — Room 2.x (KAPT) ❌
plugins {
    id 'com.android.application'
    id 'org.jetbrains.kotlin.android'
    id 'org.jetbrains.kotlin.kapt'  // ❌ Room 3.0 必须移除
}

dependencies {
    // ❌ Room compiler via KAPT
    kapt 'androidx.room:room-compiler:2.6.1'
    
    implementation 'androidx.room:room-runtime:2.6.1'
    implementation 'androidx.room:room-ktx:2.6.1'
}""",
                    afterContent = """// build.gradle — Room 3.0 (KSP) ✅
plugins {
    id 'com.android.application'
    id 'org.jetbrains.kotlin.android'
    id 'com.google.devtools.ksp' version '$kspVersion'  // ✅ KSP plugin
}

dependencies {
    // ✅ Room 3.0 compiler via KSP
    ksp 'androidx.room:room-compiler:3.0.0-alpha01'
    
    implementation 'androidx.room:room-runtime:3.0.0-alpha01'
    implementation 'androidx.room:room-ktx:3.0.0-alpha01'
}""",
                    kspVersion = kspVersion,
                    roomVersion = "3.0.0-alpha01"
                )
            }

            _state.update {
                it.copy(
                    isGeneratingKspDiff = false,
                    kspMigrationDiff = kspDiff,
                    snackbarMessage = "✅ KSP 迁移 Diff 生成完成（KSP $kspVersion + Room 3.0.0-alpha01）"
                )
            }
        }
    }

    /**
     * Copy DAO migration code to clipboard.
     *
     * @param dao DAO migration to copy
     */
    private suspend fun handleCopyDaoMigration(dao: DaoMigration) {
        val content = buildString {
            appendLine("╔══════════════════════════════════════════╗")
            appendLine("║  DAO Suspend 迁移 — ${dao.daoFunction.functionName}  ║")
            appendLine("╚══════════════════════════════════════════╝")
            appendLine()
            appendLine("📄 文件: ${dao.daoFunction.filePath}")
            appendLine("🔧 接口: ${dao.daoFunction.daoInterfaceName}")
            appendLine("📦 模块: ${dao.daoFunction.module}")
            appendLine()
            appendLine("【迁移前】❌")
            appendLine(dao.beforeCode)
            appendLine()
            appendLine("【迁移后】✅")
            appendLine(dao.afterCode)
            appendLine()
            appendLine("【说明】")
            appendLine(dao.daoFunction.suggestedMigration)
        }

        _effect.emit(Room3MigrationEffect.CopyToClipboard(content))
        _effect.emit(Room3MigrationEffect.ShowSnackbar("DAO 迁移代码已复制到剪贴板"))
    }

    /**
     * Run package replacement — simulates batch package rename.
     * In production, this would generate IDE refactoring scripts.
     */
    private suspend fun handleRunPackageReplacement() {
        _state.update { it.copy(isScanning = true) }

        withContext(Dispatchers.Default) {
            delay(1200)

            val updatedPackages = _state.value.scanResults.packageImports.map { pkg ->
                PackageReplacement(
                    packageImport = pkg,
                    status = MigrationStatus.IN_PROGRESS,
                    filesToUpdate = pkg.affectedFiles
                )
            }

            // Simulate completion after delay
            delay(800)

            val completedPackages = updatedPackages.map { it.copy(status = MigrationStatus.COMPLETED) }

            _state.update {
                it.copy(
                    packageReplacements = completedPackages,
                    isScanning = false,
                    snackbarMessage = "✅ Package 重命名替换完成（${completedPackages.size} 个 package）"
                )
            }
        }
    }

    /**
     * Generate Driver API guide for selected platform.
     *
     * @param platform Target platform (Android/iOS/JS/Desktop)
     */
    private suspend fun handleGenerateDriverGuide(platform: Platform) {
        _state.update { it.copy(isGeneratingDriverGuide = true, selectedPlatform = platform) }

        withContext(Dispatchers.Default) {
            delay(600)

            val guide = SIMULATED_DRIVER_GUIDES[platform]
                ?: SIMULATED_DRIVER_GUIDES[Platform.ANDROID]!!

            _state.update {
                it.copy(
                    driverApiGuide = guide,
                    isGeneratingDriverGuide = false,
                    snackbarMessage = "✅ ${platform.displayName} Driver API 指南生成完成"
                )
            }
        }
    }

    private fun handleSelectPlatform(platform: Platform) {
        _state.update { it.copy(selectedPlatform = platform) }
        // Auto-generate guide when platform changes
        viewModelScope.launch { handleGenerateDriverGuide(platform) }
    }

    private fun handleSetKspVersion(version: String) {
        _state.update { it.copy(kspVersion = version) }
    }

    /**
     * Setup CI compliance check — simulates CI Gradle plugin configuration.
     * In production, this would generate actual Gradle plugin code.
     */
    private suspend fun handleSetupCiCompliance() {
        withContext(Dispatchers.Default) {
            delay(500)

            _state.update {
                it.copy(
                    ciComplianceStatus = CiComplianceStatus.WARNING
                )
            }

            _effect.emit(Room3MigrationEffect.ShowSnackbar("CI 合规检测完成：7 项通过，3 项失败"))
        }
    }

    /**
     * Generate phased migration plan for multi-module projects.
     * Recommends migration order based on module dependencies.
     */
    private suspend fun handleGenerateMigrationPlan() {
        withContext(Dispatchers.Default) {
            delay(800)

            val plan = MigrationPlan(
                phases = listOf(
                    MigrationPhase(
                        phaseNumber = 1,
                        title = "依赖环境准备",
                        description = "升级 Kotlin + KSP 版本，验证编译环境",
                        tasks = listOf(
                            MigrationTask("t-1-1", "升级 Kotlin 到 1.9.24+", "KSP 1.0.25 需要 Kotlin 1.9.24+", RiskLevel.P1_HIGH),
                            MigrationTask("t-1-2", "添加 KSP plugin", "在 settings.gradle.kts 添加 pluginManagement", RiskLevel.P1_HIGH),
                            MigrationTask("t-1-3", "验证 KSP 编译", "./gradlew :app:kspDebugKotlin", RiskLevel.P2_MEDIUM)
                        ),
                        estimatedHours = 2,
                        dependencies = emptyList()
                    ),
                    MigrationPhase(
                        phaseNumber = 2,
                        title = "Gradle 配置迁移",
                        description = "将所有模块的 KAPT 替换为 KSP",
                        tasks = listOf(
                            MigrationTask("t-2-1", "替换 room-compiler kapt → ksp", "在 app/build.gradle.kts 中替换", RiskLevel.P0_CRITICAL),
                            MigrationTask("t-2-2", "更新 Room 依赖版本", "2.6.1 → 3.0.0-alpha01", RiskLevel.P0_CRITICAL),
                            MigrationTask("t-2-3", "验证增量编译", "./gradlew assembleDebug --rerun-tasks", RiskLevel.P1_HIGH)
                        ),
                        estimatedHours = 4,
                        dependencies = listOf(1)
                    ),
                    MigrationPhase(
                        phaseNumber = 3,
                        title = "Package Import 迁移",
                        description = "批量替换所有 androidx.room.* → androidx.room3.*",
                        tasks = listOf(
                            MigrationTask("t-3-1", "Find-Replace 全局替换", "IDE Refactor → Find in Path → Replace All", RiskLevel.P1_HIGH),
                            MigrationTask("t-3-2", "验证编译通过", "./gradlew compileDebugKotlin", RiskLevel.P1_HIGH),
                            MigrationTask("t-3-3", "回归测试", "执行单元测试和集成测试", RiskLevel.P2_MEDIUM)
                        ),
                        estimatedHours = 3,
                        dependencies = listOf(2)
                    ),
                    MigrationPhase(
                        phaseNumber = 4,
                        title = "DAO Suspend 化",
                        description = "将所有同步 DAO 函数改为 suspend/return Flow",
                        tasks = SIMULATED_DAO_FUNCTIONS.mapIndexed { idx, dao ->
                            MigrationTask(
                                "t-4-${idx + 1}",
                                "迁移 ${dao.functionName}",
                                "${dao.daoInterfaceName}.${dao.functionName}() → suspend",
                                RiskLevel.P0_CRITICAL
                            )
                        },
                        estimatedHours = 8,
                        dependencies = listOf(3)
                    ),
                    MigrationPhase(
                        phaseNumber = 5,
                        title = "Driver API 迁移",
                        description = "如有需要，更新数据库初始化代码以适配新 Driver API",
                        tasks = listOf(
                            MigrationTask("t-5-1", "评估 Driver 变更", "检查数据库初始化代码是否需要修改", RiskLevel.P1_HIGH),
                            MigrationTask("t-5-2", "更新 Driver 配置", "如有需要，更新为 AndroidDriver/SQLiteDriver", RiskLevel.P2_MEDIUM),
                            MigrationTask("t-5-3", "多平台构建验证", "./gradlew :androidMain:build :iosMain:build", RiskLevel.P2_MEDIUM)
                        ),
                        estimatedHours = 4,
                        dependencies = listOf(4)
                    ),
                    MigrationPhase(
                        phaseNumber = 6,
                        title = "CI 验证与上线",
                        description = "配置 CI 合规检测，验证迁移完整性",
                        tasks = listOf(
                            MigrationTask("t-6-1", "集成 CI 合规检测", "添加 room3-compliance Gradle 插件", RiskLevel.P1_HIGH),
                            MigrationTask("t-6-2", "生成迁移报告", "导出完整迁移报告供审核", RiskLevel.P2_MEDIUM),
                            MigrationTask("t-6-3", "合并到主分支", "PR review + 合入 agency-product-sprint", RiskLevel.P0_CRITICAL)
                        ),
                        estimatedHours = 2,
                        dependencies = listOf(5)
                    )
                ),
                totalEstimatedHours = 23,
                criticalPath = listOf("app", "data")
            )

            _state.update {
                it.copy(
                    migrationPlan = plan,
                    decisionGuide = SIMULATED_DECISION_GUIDE,
                    snackbarMessage = "✅ 渐进迁移计划生成完成（预计 ${plan.totalEstimatedHours} 小时）"
                )
            }
        }
    }

    /**
     * Export migration report as Markdown.
     * Combines all scan results, migration diffs, and checklists.
     */
    private suspend fun handleExportReport() {
        withContext(Dispatchers.Default) {
            val state = _state.value

            val report = buildString {
                appendLine("# Room 3.0 迁移报告")
                appendLine()
                appendLine("**生成时间**: ${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date())}")
                appendLine("**项目路径**: ${state.projectPath.ifEmpty { "（未指定）" }}")
                appendLine("**整体风险等级**: ${state.scanResults.riskLevel.emoji} ${state.scanResults.riskLevel.displayName}")
                appendLine()

                // 1. 执行摘要
                appendLine("## 1. 执行摘要")
                appendLine()
                appendLine("| 指标 | 数值 |")
                appendLine("|------|------|")
                appendLine("| KAPT 使用位置 | ${state.scanResults.kaptUsages.size} |")
                appendLine("| 非 Suspend DAO | ${state.scanResults.daoNonSuspend.size} |")
                appendLine("| Package Import | ${state.scanResults.packageImports.sumOf { it.occurrences }} |")
                appendLine("| 破坏性变更 | ${state.scanResults.breakingChanges.size} |")
                appendLine()

                // 2. KAPT 迁移
                if (state.scanResults.kaptUsages.isNotEmpty()) {
                    appendLine("## 2. KAPT 迁移")
                    appendLine()
                    state.scanResults.kaptUsages.forEach { usage ->
                        appendLine("### `${usage.module}/build.gradle.kts`")
                        appendLine("```")
                        appendLine(usage.configSnippet)
                        appendLine("```")
                        appendLine()
                    }
                }

                // 3. DAO Suspend 迁移
                if (state.daoMigrations.isNotEmpty()) {
                    appendLine("## 3. DAO Suspend 迁移")
                    appendLine()
                    state.daoMigrations.forEach { migration ->
                        appendLine("### `${migration.daoFunction.daoInterfaceName}.${migration.daoFunction.functionName}()`")
                        appendLine("**文件**: `${migration.daoFunction.filePath}`")
                        appendLine()
                        appendLine("```kotlin")
                        appendLine("// Before ❌")
                        appendLine(migration.beforeCode)
                        appendLine()
                        appendLine("// After ✅")
                        appendLine(migration.afterCode)
                        appendLine("```")
                        appendLine()
                    }
                }

                // 4. 迁移计划
                state.migrationPlan?.let { plan ->
                    appendLine("## 4. 渐进迁移计划")
                    appendLine()
                    appendLine("**预计总工时**: ${plan.totalEstimatedHours} 小时")
                    appendLine()
                    plan.phases.forEach { phase ->
                        appendLine("### Phase ${phase.phaseNumber}: ${phase.title}（${phase.estimatedHours}h）")
                        appendLine(phase.description)
                        appendLine()
                        phase.tasks.forEach { task ->
                            val status = if (task.isCompleted) "✅" else "⬜"
                            appendLine("- $status ${task.title}")
                        }
                        appendLine()
                    }
                }
            }

            _state.update { it.copy(exportedReport = report) }
            _effect.emit(Room3MigrationEffect.ShareReport(report))
        }
    }

    private fun handleDismissSnackbar() {
        _state.update { it.copy(snackbarMessage = null) }
    }

    private fun handleResetAll() {
        _state.update { Room3MigrationState.Initial }
    }
}
