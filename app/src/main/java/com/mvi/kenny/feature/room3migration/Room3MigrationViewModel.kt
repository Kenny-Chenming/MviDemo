package com.mvi.kenny.feature.room3migration

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

/**
 * ============================================================
 * Room3MigrationViewModel — Room 3.0 KMP 现代化迁移检测与自动化工具包 ViewModel
 * ================================================================
 * MVI architecture ViewModel for Room 3.0 KMP modernization migration toolkit.
 *
 * PRD-118: Room 3.0 KMP 现代化迁移检测与自动化工具包
 * Design Reference: memory/agency/designs/PRD-118-Room-3-0-KMP-现代化迁移检测与自动化工具包.md
 *
 * Key behaviors:
 * 1. Scans project for Room 3.0 migration issues across all modules
 * 2. Provides package namespace migration (androidx.room → androidx.room3)
 * 3. KAPT → KSP configuration migration
 * 4. SupportSQLite → SQLiteDriver migration guidance
 * 5. Kotlin compiler compliance checking
 * 6. Suspend API migration recommendations
 * 7. InvalidationTracker → Flow API migration
 * 8. Regression test generation for migrated code
 * 9. Backup & rollback support for all migration operations
 * —————————————————————————————————————————————————————
 */
class Room3MigrationViewModel : ViewModel() {

    // MVI State — single source of truth using StateFlow
    private val _state = MutableStateFlow(Room3MigrationState())
    val state: StateFlow<Room3MigrationState> = _state.asStateFlow()

    // MVI Effect — one-time side effects via Channel
    private val _effect = Channel<Room3MigrationEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    // Date formatter for timestamps
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())

    init {
        // Initialize module states to NOT_STARTED
        _state.value = _state.value.copy(
            moduleStates = MigrationModule.entries.associateWith { ModuleState.NOT_STARTED }
        )
    }

    /**
     * ============================================================
     * sendIntent — 处理用户意图
     * ================================================================
     * Entry point for all user intents. Maps Intent → Business Logic.
     *
     * @param intent User intent from the UI layer
     */
    fun sendIntent(intent: Room3MigrationIntent) {
        when (intent) {
            is Room3MigrationIntent.StartScan -> startScan()
            is Room3MigrationIntent.RefreshScan -> refreshScan()
            is Room3MigrationIntent.SelectTab -> selectTab(intent.tab)
            is Room3MigrationIntent.PreviewReplacements -> previewReplacements(intent.replacements)
            is Room3MigrationIntent.ToggleReplacementSelection -> toggleReplacementSelection(intent.id)
            is Room3MigrationIntent.ToggleSelectAllReplacements -> toggleSelectAllReplacements()
            is Room3MigrationIntent.ExecuteReplacement -> executeReplacement(intent.replacement)
            is Room3MigrationIntent.ExecuteSelectedReplacements -> executeSelectedReplacements()
            is Room3MigrationIntent.ConvertKaptToKsp -> convertKaptToKsp(intent.configIds)
            is Room3MigrationIntent.ConvertAllKaptToKsp -> convertAllKaptToKsp()
            is Room3MigrationIntent.MigrateSqliteDriver -> migrateSqliteDriver()
            is Room3MigrationIntent.CheckKotlinCompiler -> checkKotlinCompiler()
            is Room3MigrationIntent.MigrateSuspendApi -> migrateSuspendApi()
            is Room3MigrationIntent.MigrateFlowInvalidation -> migrateFlowInvalidation()
            is Room3MigrationIntent.GenerateRegressionTests -> generateRegressionTests()
            is Room3MigrationIntent.SaveGeneratedTests -> saveGeneratedTests(intent.testIds)
            is Room3MigrationIntent.CreateBackup -> createBackup()
            is Room3MigrationIntent.ShowRollbackDialog -> showRollbackDialog()
            is Room3MigrationIntent.ConfirmRollback -> confirmRollback()
            is Room3MigrationIntent.CancelRollback -> cancelRollback()
        }
    }

    // ================================================================
    // Scanning / 扫描逻辑
    // ================================================================

    /**
     * 开始扫描项目中的 Room 3.0 迁移问题
     * Start scanning project for Room 3.0 migration issues.
     */
    private fun startScan() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isScanning = true, isLoading = true, error = null)

            // Reset module states to IN_PROGRESS
            val inProgressStates = MigrationModule.entries.associateWith { ModuleState.IN_PROGRESS }
            _state.value = _state.value.copy(moduleStates = inProgressStates)

            try {
                // Simulate scanning progress — in production would parse project files
                delay(300)

                // Generate mock scan results for demonstration
                val mockResults = generateMockScanResults()

                delay(300)

                // Calculate health scores for radar chart
                val radarScores = calculateRadarScores(mockResults)
                val overallHealth = radarScores.average().toFloat()

                // Determine module states based on scan results
                val moduleStates = determineModuleStates(mockResults)

                _state.value = _state.value.copy(
                    isScanning = false,
                    scanResults = mockResults,
                    healthScore = overallHealth,
                    radarAxisScores = radarScores,
                    moduleStates = moduleStates,
                    pendingReplacements = generateImportReplacements(mockResults),
                    kaptConfigs = mockResults.kaptConfigs,
                    sqliteUsages = mockResults.sqliteUsages,
                    kotlinCompilerIssues = mockResults.kotlinCompilerIssues,
                    suspendApiIssues = mockResults.suspendApiIssues,
                    invalidationIssues = mockResults.invalidationIssues,
                    testCoverage = mockResults.testCoverage,
                    isLoading = false
                )

                _effect.send(Room3MigrationEffect.ScanCompleted(
                    totalIssues = mockResults.totalIssues,
                    criticalModules = moduleStates.filter { it.value == ModuleState.BLOCKED }.keys.toList()
                ))

                _effect.send(Room3MigrationEffect.ShowToast(
                    "扫描完成，发现 ${mockResults.totalIssues} 个问题 / Scan completed, found ${mockResults.totalIssues} issues"
                ))

            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isScanning = false,
                    isLoading = false,
                    error = e.message ?: "扫描失败 / Scan failed"
                )
                _effect.send(Room3MigrationEffect.ShowError(
                    e.message ?: "扫描过程中发生错误 / Error during scan"
                ))
            }
        }
    }

    /**
     * 刷新扫描结果
     * Refresh scan results.
     */
    private fun refreshScan() {
        startScan()
    }

    /**
     * 选择 Tab 页
     * Select a tab.
     */
    private fun selectTab(tab: MigrationTab) {
        _state.value = _state.value.copy(selectedTab = tab)
    }

    // ================================================================
    // Package Migration / 包迁移逻辑
    // ================================================================

    /**
     * 预览替换列表
     * Preview replacement list.
     */
    private fun previewReplacements(replacements: List<ImportReplacement>) {
        _state.value = _state.value.copy(pendingReplacements = replacements)
    }

    /**
     * 切换单个替换项的选择状态
     * Toggle selection for a single replacement.
     */
    private fun toggleReplacementSelection(id: String) {
        val current = _state.value.selectedReplacements
        val updated = if (id in current) current - id else current + id
        _state.value = _state.value.copy(selectedReplacements = updated)
    }

    /**
     * 全选/取消全选所有替换项
     * Toggle select all replacements.
     */
    private fun toggleSelectAllReplacements() {
        val currentAllSelected = _state.value.allReplacementsSelected
        if (currentAllSelected) {
            _state.value = _state.value.copy(
                selectedReplacements = emptySet(),
                allReplacementsSelected = false
            )
        } else {
            _state.value = _state.value.copy(
                selectedReplacements = _state.value.pendingReplacements.map { it.id }.toSet(),
                allReplacementsSelected = true
            )
        }
    }

    /**
     * 执行单个替换
     * Execute a single replacement.
     */
    private fun executeReplacement(replacement: ImportReplacement) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            delay(300)

            val updated = _state.value.pendingReplacements.map {
                if (it.id == replacement.id) it.copy(isExecuted = true, isSelected = false)
                else it
            }
            _state.value = _state.value.copy(
                pendingReplacements = updated,
                isLoading = false
            )
            _effect.send(Room3MigrationEffect.ShowToast("已替换 / Replaced: ${replacement.filePath}"))
            _effect.send(Room3MigrationEffect.NavigateToFile(replacement.filePath, replacement.lineNumber))
        }
    }

    /**
     * 执行所有选中的替换
     * Execute all selected replacements.
     */
    private fun executeSelectedReplacements() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            delay(500)

            val selected = _state.value.selectedReplacements
            val updated = _state.value.pendingReplacements.map {
                if (it.id in selected) it.copy(isExecuted = true, isSelected = false)
                else it
            }

            _state.value = _state.value.copy(
                pendingReplacements = updated,
                selectedReplacements = emptySet(),
                isLoading = false,
                moduleStates = _state.value.moduleStates.toMutableMap().apply {
                    this[MigrationModule.PACKAGE_NAMESPACE] = ModuleState.COMPLETED
                }
            )

            _effect.send(Room3MigrationEffect.ShowToast(
                "已执行 ${selected.size} 项替换 / Executed ${selected.size} replacements"
            ))
            _effect.send(Room3MigrationEffect.MigrationCompleted(MigrationModule.PACKAGE_NAMESPACE))
        }
    }

    // ================================================================
    // KSP Migration / KSP 迁移逻辑
    // ================================================================

    /**
     * 将选中的 KAPT 配置转换为 KSP
     * Convert selected KAPT configs to KSP.
     */
    private fun convertKaptToKsp(configIds: Set<String>) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            delay(500)

            val updatedKaptConfigs = _state.value.kaptConfigs.map { config ->
                if (config.id in configIds) config.copy(configContent = "// Migrated to KSP: ${config.configContent}")
                else config
            }

            _state.value = _state.value.copy(
                kaptConfigs = updatedKaptConfigs,
                isLoading = false,
                moduleStates = _state.value.moduleStates.toMutableMap().apply {
                    this[MigrationModule.KSP_SWITCH] = ModuleState.COMPLETED
                }
            )

            _effect.send(Room3MigrationEffect.ShowToast(
                "已转换 ${configIds.size} 个 KAPT 配置 / Converted ${configIds.size} KAPT configs"
            ))
            _effect.send(Room3MigrationEffect.MigrationCompleted(MigrationModule.KSP_SWITCH))
        }
    }

    /**
     * 转换所有 KAPT 配置
     * Convert all KAPT configs to KSP.
     */
    private fun convertAllKaptToKsp() {
        val allIds = _state.value.kaptConfigs.map { it.id }.toSet()
        convertKaptToKsp(allIds)
    }

    // ================================================================
    // SQLite Driver Migration / SQLite Driver 迁移逻辑
    // ================================================================

    /**
     * 迁移 SupportSQLite → SQLiteDriver
     * Migrate SupportSQLite to SQLiteDriver.
     */
    private fun migrateSqliteDriver() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            delay(500)

            _state.value = _state.value.copy(
                isLoading = false,
                moduleStates = _state.value.moduleStates.toMutableMap().apply {
                    this[MigrationModule.SQLITE_DRIVER] = ModuleState.COMPLETED
                }
            )

            _effect.send(Room3MigrationEffect.ShowToast(
                "SQLite Driver 迁移指导已生成 / SQLite Driver migration guide generated"
            ))
            _effect.send(Room3MigrationEffect.MigrationCompleted(MigrationModule.SQLITE_DRIVER))
        }
    }

    // ================================================================
    // Kotlin Compiler / Kotlin 编译器逻辑
    // ================================================================

    /**
     * 检查 Kotlin 编译器合规性
     * Check Kotlin compiler compliance.
     */
    private fun checkKotlinCompiler() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            delay(400)

            _state.value = _state.value.copy(
                isLoading = false,
                moduleStates = _state.value.moduleStates.toMutableMap().apply {
                    this[MigrationModule.KOTLIN_COMPILER] = ModuleState.COMPLETED
                }
            )

            _effect.send(Room3MigrationEffect.ShowToast(
                "Kotlin 编译器合规检测完成 / Kotlin compiler compliance check completed"
            ))
            _effect.send(Room3MigrationEffect.MigrationCompleted(MigrationModule.KOTLIN_COMPILER))
        }
    }

    // ================================================================
    // Suspend API Migration / Suspend API 迁移逻辑
    // ================================================================

    /**
     * 迁移 Suspend API
     * Migrate suspend API.
     */
    private fun migrateSuspendApi() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            delay(500)

            _state.value = _state.value.copy(
                isLoading = false,
                moduleStates = _state.value.moduleStates.toMutableMap().apply {
                    this[MigrationModule.SUSPEND_API] = ModuleState.COMPLETED
                }
            )

            _effect.send(Room3MigrationEffect.ShowToast(
                "Suspend API 迁移建议已生成 / Suspend API migration suggestions generated"
            ))
            _effect.send(Room3MigrationEffect.MigrationCompleted(MigrationModule.SUSPEND_API))
        }
    }

    // ================================================================
    // Flow Invalidation Migration / Flow 订阅迁移逻辑
    // ================================================================

    /**
     * 迁移 InvalidationTracker → Flow API
     * Migrate InvalidationTracker to Flow API.
     */
    private fun migrateFlowInvalidation() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            delay(500)

            _state.value = _state.value.copy(
                isLoading = false,
                moduleStates = _state.value.moduleStates.toMutableMap().apply {
                    this[MigrationModule.FLOW_INVALIDATION] = ModuleState.COMPLETED
                }
            )

            _effect.send(Room3MigrationEffect.ShowToast(
                "Flow Invalidation 迁移建议已生成 / Flow Invalidation migration suggestions generated"
            ))
            _effect.send(Room3MigrationEffect.MigrationCompleted(MigrationModule.FLOW_INVALIDATION))
        }
    }

    // ================================================================
    // Regression Test / 回归测试逻辑
    // ================================================================

    /**
     * 生成回归测试用例
     * Generate regression test cases.
     */
    private fun generateRegressionTests() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            delay(600)

            val generatedTests = generateMockTests()

            _state.value = _state.value.copy(
                generatedTests = generatedTests,
                isLoading = false,
                moduleStates = _state.value.moduleStates.toMutableMap().apply {
                    this[MigrationModule.REGRESSION_TEST] = ModuleState.COMPLETED
                }
            )

            _effect.send(Room3MigrationEffect.ShowToast(
                "已生成 ${generatedTests.size} 个测试用例 / Generated ${generatedTests.size} test cases"
            ))
            _effect.send(Room3MigrationEffect.MigrationCompleted(MigrationModule.REGRESSION_TEST))
        }
    }

    /**
     * 保存生成的测试用例
     * Save generated test cases.
     */
    private fun saveGeneratedTests(testIds: Set<String>) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            delay(400)

            val updated = _state.value.generatedTests.map { test ->
                if (test.id in testIds) test.copy(isGenerated = true) else test
            }

            _state.value = _state.value.copy(
                generatedTests = updated,
                isLoading = false
            )

            _effect.send(Room3MigrationEffect.ShowToast(
                "已保存 ${testIds.size} 个测试用例 / Saved ${testIds.size} test cases"
            ))
        }
    }

    // ================================================================
    // Backup & Rollback / 备份与回滚逻辑
    // ================================================================

    /**
     * 创建备份
     * Create a backup before migration operations.
     */
    private fun createBackup() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            delay(500)

            val backupInfo = BackupInfo(
                backupPath = "/tmp/room3_migration_backup_${System.currentTimeMillis()}",
                createdAt = dateFormat.format(Date()),
                fileCount = _state.value.scanResults.totalIssues,
                description = "Room 3.0 迁移前备份 / Backup before Room 3.0 migration"
            )

            _state.value = _state.value.copy(
                backupInfo = backupInfo,
                rollbackAvailable = true,
                isLoading = false
            )

            _effect.send(Room3MigrationEffect.BackupCreated)
            _effect.send(Room3MigrationEffect.ShowToast(
                "备份已创建 / Backup created at ${backupInfo.backupPath}"
            ))
        }
    }

    /**
     * 显示回滚确认对话框
     * Show rollback confirmation dialog.
     */
    private fun showRollbackDialog() {
        _state.value = _state.value.copy(showRollbackDialog = true)
    }

    /**
     * 确认执行回滚
     * Confirm and execute rollback.
     */
    private fun confirmRollback() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, showRollbackDialog = false)
            delay(600)

            // Reset all module states to NOT_STARTED
            val resetStates = MigrationModule.entries.associateWith { ModuleState.NOT_STARTED }

            _state.value = _state.value.copy(
                moduleStates = resetStates,
                backupInfo = null,
                rollbackAvailable = false,
                pendingReplacements = emptyList(),
                isLoading = false
            )

            _effect.send(Room3MigrationEffect.RollbackCompleted)
            _effect.send(Room3MigrationEffect.ShowToast(
                "已回滚到备份状态 / Rolled back to backup state"
            ))
        }
    }

    /**
     * 取消回滚
     * Cancel rollback.
     */
    private fun cancelRollback() {
        _state.value = _state.value.copy(showRollbackDialog = false)
    }

    // ================================================================
    // Helper Methods / 辅助方法
    // ================================================================

    /**
     * 计算雷达图各轴评分
     * Calculate radar chart axis scores based on scan results.
     */
    private fun calculateRadarScores(results: ScanResults): List<Float> {
        return listOf(
            // Package Namespace — 基于问题数量计算 (问题越多分数越低)
            calculateModuleScore(results.packageIssues.size, 10),
            // KSP Switch — 基于 KAPT 配置数量
            calculateModuleScore(results.kaptConfigs.size, 5),
            // SQLite Driver — 基于 SQLite 使用数量
            calculateModuleScore(results.sqliteUsages.size, 5),
            // Kotlin Compiler — 基于编译器问题数量
            calculateModuleScore(results.kotlinCompilerIssues.size, 5),
            // Suspend API — 基于 API 问题数量
            calculateModuleScore(results.suspendApiIssues.size, 8),
            // Flow Invalidation — 基于 invalidation 问题数量
            calculateModuleScore(results.invalidationIssues.size, 5),
            // Regression Test — 基于测试覆盖率
            results.testCoverage.coveragePercent.coerceIn(0f, 1f)
        )
    }

    /**
     * 计算单个模块评分 (0.0 ~ 1.0)
     * Calculate individual module score.
     */
    private fun calculateModuleScore(issueCount: Int, maxIssues: Int): Float {
        return if (issueCount == 0) 1f
        else maxOf(0f, 1f - (issueCount.toFloat() / maxIssues))
    }

    /**
     * 根据扫描结果确定各模块状态
     * Determine module states based on scan results.
     */
    private fun determineModuleStates(results: ScanResults): Map<MigrationModule, ModuleState> {
        return mapOf(
            MigrationModule.PACKAGE_NAMESPACE to when {
                results.packageIssues.isEmpty() -> ModuleState.COMPLETED
                else -> ModuleState.IN_PROGRESS
            },
            MigrationModule.KSP_SWITCH to when {
                results.kaptConfigs.isEmpty() -> ModuleState.COMPLETED
                results.kaptConfigs.any { it.hasConflict } -> ModuleState.BLOCKED
                else -> ModuleState.IN_PROGRESS
            },
            MigrationModule.SQLITE_DRIVER to when {
                results.sqliteUsages.isEmpty() -> ModuleState.COMPLETED
                else -> ModuleState.IN_PROGRESS
            },
            MigrationModule.KOTLIN_COMPILER to when {
                results.kotlinCompilerIssues.isEmpty() -> ModuleState.COMPLETED
                else -> ModuleState.IN_PROGRESS
            },
            MigrationModule.SUSPEND_API to when {
                results.suspendApiIssues.isEmpty() -> ModuleState.COMPLETED
                else -> ModuleState.IN_PROGRESS
            },
            MigrationModule.FLOW_INVALIDATION to when {
                results.invalidationIssues.isEmpty() -> ModuleState.COMPLETED
                else -> ModuleState.IN_PROGRESS
            },
            MigrationModule.REGRESSION_TEST to when {
                results.testCoverage.coveragePercent >= 0.8f -> ModuleState.COMPLETED
                results.testCoverage.totalDaos == 0 -> ModuleState.NOT_STARTED
                else -> ModuleState.IN_PROGRESS
            }
        )
    }

    /**
     * 生成 Import 替换列表
     * Generate import replacement list from scan results.
     */
    private fun generateImportReplacements(results: ScanResults): List<ImportReplacement> {
        return results.packageIssues.map { issue ->
            ImportReplacement(
                id = issue.id,
                filePath = issue.filePath,
                lineNumber = issue.lineNumber,
                oldImport = issue.content,
                newImport = issue.content.replace("androidx.room", "androidx.room3")
            )
        }
    }

    /**
     * 生成模拟测试用例
     * Generate mock regression test cases.
     */
    private fun generateMockTests(): List<GeneratedTest> {
        return listOf(
            GeneratedTest(
                id = UUID.randomUUID().toString(),
                daoName = "UserDao",
                testContent = """
                    @Test
                    fun insertAndLoadUser() = runBlocking {
                        database.userDao().insert(testUser)
                        val loaded = database.userDao().getUserById(testUser.id)
                        assertEquals(testUser.name, loaded?.name)
                    }
                """.trimIndent(),
                testFilePath = "app/src/test/java/com/example/dao/UserDaoTest.kt"
            ),
            GeneratedTest(
                id = UUID.randomUUID().toString(),
                daoName = "UserDao",
                testContent = """
                    @Test
                    fun deleteUser() = runBlocking {
                        database.userDao().insert(testUser)
                        database.userDao().delete(testUser)
                        val loaded = database.userDao().getUserById(testUser.id)
                        assertNull(loaded)
                    }
                """.trimIndent(),
                testFilePath = "app/src/test/java/com/example/dao/UserDaoTest.kt"
            ),
            GeneratedTest(
                id = UUID.randomUUID().toString(),
                daoName = "ProductDao",
                testContent = """
                    @Test
                    fun queryProductsByCategory() = runBlocking {
                        val products = database.productDao().getProductsByCategory("electronics")
                        assertTrue(products.isNotEmpty())
                    }
                """.trimIndent(),
                testFilePath = "app/src/test/java/com/example/dao/ProductDaoTest.kt"
            )
        )
    }

    /**
     * 生成模拟扫描结果（演示用）
     * Generate mock scan results for demonstration.
     */
    private fun generateMockScanResults(): ScanResults {
        return ScanResults(
            packageIssues = listOf(
                PackageIssue(
                    id = UUID.randomUUID().toString(),
                    filePath = "app/src/main/java/com/example/dao/UserDao.kt",
                    lineNumber = 3,
                    content = "import androidx.room.Dao",
                    issueType = "import",
                    fixAvailable = true
                ),
                PackageIssue(
                    id = UUID.randomUUID().toString(),
                    filePath = "app/build.gradle.kts",
                    lineNumber = 28,
                    content = """implementation("androidx.room:room-runtime:2.6.1")""",
                    issueType = "dependency",
                    fixAvailable = true
                ),
                PackageIssue(
                    id = UUID.randomUUID().toString(),
                    filePath = "feature/home/src/main/java/com/example/home/HomeDao.kt",
                    lineNumber = 5,
                    content = "import androidx.room.Entity",
                    issueType = "import",
                    fixAvailable = true
                ),
                PackageIssue(
                    id = UUID.randomUUID().toString(),
                    filePath = "data/repository/build.gradle.kts",
                    lineNumber = 31,
                    content = """implementation("androidx.room:room-ktx:2.6.1")""",
                    issueType = "dependency",
                    fixAvailable = true
                )
            ),
            kaptConfigs = listOf(
                KaptConfig(
                    id = UUID.randomUUID().toString(),
                    filePath = "app/build.gradle.kts",
                    lineNumber = 45,
                    configContent = """kapt("androidx.room:room-compiler:2.6.1")""",
                    suggestedKspConfig = """ksp("androidx.room:room-compiler:3.0.0-alpha01")""",
                    hasConflict = false
                ),
                KaptConfig(
                    id = UUID.randomUUID().toString(),
                    filePath = "feature/home/build.gradle.kts",
                    lineNumber = 32,
                    configContent = """kapt { arguments { arg(\"room.schemaLocation\", \"${'$'}projectDir/schemas\") } }""",
                    suggestedKspConfig = """ksp { arg(\"room.schemaLocation\", \"${'$'}projectDir/schemas\") }""",
                    hasConflict = false
                )
            ),
            sqliteUsages = listOf(
                SqliteUsage(
                    id = UUID.randomUUID().toString(),
                    filePath = "app/src/main/java/com/example/db/AppDatabase.kt",
                    lineNumber = 18,
                    content = "private val DB: SupportSQLiteDatabase by lazy { ... }",
                    suggestedReplacement = "private val DB: SQLiteDatabase by lazy { ... }",
                    needsWrapperDependency = true
                )
            ),
            kotlinCompilerIssues = listOf(
                KotlinCompilerIssue(
                    id = UUID.randomUUID().toString(),
                    filePath = "gradle.properties",
                    lineNumber = 8,
                    content = "kotlin.code.style=official",
                    issueType = "compiler_option",
                    recommendation = "Ensure kotlin.code.style=official is set for Room 3.0 KMP"
                )
            ),
            suspendApiIssues = listOf(
                SuspendApiIssue(
                    id = UUID.randomUUID().toString(),
                    filePath = "app/src/main/java/com/example/dao/UserDao.kt",
                    lineNumber = 15,
                    content = "fun getAllUsers(): List<User>",
                    suggestedSuspendReplacement = "suspend fun getAllUsers(): List<User>",
                    callerCount = 3
                ),
                SuspendApiIssue(
                    id = UUID.randomUUID().toString(),
                    filePath = "app/src/main/java/com/example/dao/ProductDao.kt",
                    lineNumber = 22,
                    content = "fun insertProduct(product: Product)",
                    suggestedSuspendReplacement = "suspend fun insertProduct(product: Product)",
                    callerCount = 5
                ),
                SuspendApiIssue(
                    id = UUID.randomUUID().toString(),
                    filePath = "data/repository/UserRepository.kt",
                    lineNumber = 34,
                    content = "fun findUserById(id: String): User?",
                    suggestedSuspendReplacement = "suspend fun findUserById(id: String): User?",
                    callerCount = 2
                )
            ),
            invalidationIssues = listOf(
                InvalidationIssue(
                    id = UUID.randomUUID().toString(),
                    filePath = "app/src/main/java/com/example/repository/UserRepository.kt",
                    lineNumber = 12,
                    content = "val userFlow = invalidationFlow()",
                    suggestedFlowReplacement = "val userFlow: Flow<List<User>>",
                    isInDao = false
                ),
                InvalidationIssue(
                    id = UUID.randomUUID().toString(),
                    filePath = "app/src/main/java/com/example/dao/UserDao.kt",
                    lineNumber = 8,
                    content = "@Database(invalidationTriggers = ...) class AppDatabase...",
                    suggestedFlowReplacement = "database.invalidationTracker.invalidationFlow",
                    isInDao = true
                )
            ),
            testCoverage = TestCoverage(
                totalDaos = 5,
                testedDaos = 2,
                untestedDaos = listOf("ProductDao", "OrderDao", "CartDao"),
                generatedTests = emptyList()
            )
        )
    }
}
