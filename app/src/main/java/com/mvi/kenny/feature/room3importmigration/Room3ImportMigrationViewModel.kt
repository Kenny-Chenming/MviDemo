package com.mvi.kenny.feature.room3importmigration

// ================================================================
// Room3ImportMigrationViewModel — Room 3.0 Import 迁移工具 ViewModel
// ================================================================
// MVI ViewModel: receives Intent, processes business logic, updates State.
//
// PRD-225: Room 3.0 破坏性变更迁移工具包
// Design: memory/agency/designs/PRD-225-Room-3-0-破坏性变更迁移工具包.md
//
// Key responsibilities:
//   1. Scan project files for androidx.room imports → androidx.room3
//   2. Detect SupportSQLiteDatabase usage
//   3. Provide suspend migration patterns
//   4. Guide dual-version compatibility strategies
//   5. CI compliance checklist management
// ================================================================

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// ================================================================
// State & Effect Aliases
// ================================================================
typealias Room3ImportMigrationUiState = Room3ImportMigrationState
typealias Room3ImportMigrationUiEffect = Room3ImportMigrationEffect

// ================================================================
// ViewModel
// ================================================================

/**
 * ============================================================
 * Room3ImportMigrationViewModel — Room 3.0 Import 迁移工具 ViewModel
 * ============================================================
 * MVI pattern: State is the single source of truth, Intent drives changes.
 *
 * @param initialState Initial page state
 */
class Room3ImportMigrationViewModel(
    private val initialState: Room3ImportMigrationState = Room3ImportMigrationState()
) : ViewModel() {

    // ── State ─────────────────────────────────────────────────
    private val _state = MutableStateFlow(initialState)
    val state: StateFlow<Room3ImportMigrationState> = _state.asStateFlow()

    // ── Effects ───────────────────────────────────────────────
    private val _effect = MutableSharedFlow<Room3ImportMigrationEffect>()
    val effect: MutableSharedFlow<Room3ImportMigrationEffect> = _effect

    // ==========================================================
    // Public API
    // ==========================================================

    /**
     * Process user intent / 处理用户意图
     * @param intent User action intent
     */
    fun sendIntent(intent: Room3ImportMigrationIntent) {
        when (intent) {
            is Room3ImportMigrationIntent.SelectTab ->
                _state.update { it.copy(selectedTab = intent.index) }

            is Room3ImportMigrationIntent.SetProjectPath ->
                _state.update { it.copy(projectPath = intent.path) }

            is Room3ImportMigrationIntent.RunImportScan ->
                runImportScan(simulate = intent.simulate)

            is Room3ImportMigrationIntent.RunSQLiteDriverScan ->
                runSQLiteDriverScan(simulate = intent.simulate)

            is Room3ImportMigrationIntent.CopyFixCode ->
                copyFixCode(intent.code)

            is Room3ImportMigrationIntent.ToggleChecklistItem ->
                toggleChecklistItem(intent.itemId, intent.checked)

            is Room3ImportMigrationIntent.DismissSnackbar ->
                _state.update { it.copy(snackbarMessage = null) }

            is Room3ImportMigrationIntent.ResetAll ->
                _state.update { Room3ImportMigrationState.Initial }
        }
    }

    // ==========================================================
    // Import Scan Logic / Import 扫描逻辑
    // ==========================================================

    /**
     * Run import scan — scan project for androidx.room imports
     * 运行 Import 扫描 — 扫描项目中所有 androidx.room import
     *
     * @param simulate If true, use simulated data; otherwise scan project files
     */
    private fun runImportScan(simulate: Boolean) {
        viewModelScope.launch {
            _state.update {
                it.copy(
                    isScanning = true,
                    scanProgress = 0f,
                    scanProgressText = "Initializing scan..."
                )
            }

            if (simulate) {
                runSimulatedImportScan()
            } else {
                runRealImportScan()
            }
        }
    }

    /**
     * Simulated import scan with preset data
     * 模拟 Import 扫描（使用预设数据）
     */
    private suspend fun runSimulatedImportScan() {
        // Phase 1: Scanning
        val phases = listOf(
            "Scanning project files..." to 0.15f,
            "Detecting androidx.room imports..." to 0.40f,
            "Categorizing import statements..." to 0.65f,
            "Generating migration diff..." to 0.85f,
            "Complete!" to 1.0f
        )

        for ((text, progress) in phases) {
            _state.update { it.copy(scanProgressText = text, scanProgress = progress) }
            delay(300)
        }

        _state.update {
            it.copy(
                isScanning = false,
                scanProgress = 1f,
                scanProgressText = "Scan complete!",
                importScanResults = SIMULATED_IMPORT_RESULTS,
                snackbarMessage = "Found ${SIMULATED_IMPORT_RESULTS.size} imports to migrate"
            )
        }

        _effect.emit(Room3ImportMigrationEffect.ShowSnackbar(
            "Found ${SIMULATED_IMPORT_RESULTS.size} imports to migrate"
        ))
    }

    /**
     * Real import scan — scan actual project files using ProcessBuilder + grep/find
     * 真实 Import 扫描 — 使用 ProcessBuilder 扫描实际项目文件
     */
    private suspend fun runRealImportScan() {
        val projectPath = _state.value.projectPath
        if (projectPath.isBlank()) {
            _state.update {
                it.copy(
                    isScanning = false,
                    snackbarMessage = "Please enter project path first"
                )
            }
            _effect.emit(Room3ImportMigrationEffect.ShowSnackbar("Please enter project path first"))
            return
        }

        try {
            withContext(Dispatchers.IO) {
                // Scan for androidx.room imports (exclude room3)
                val importPattern = "import androidx\\.room(?!3)".toRegex()
                val ktFiles = findKotlinFiles(projectPath)
                val results = mutableListOf<ImportScanResult>()

                ktFiles.forEachIndexed { index, file ->
                    val progress = (index + 1).toFloat() / ktFiles.size
                    _state.update { it.copy(scanProgress = progress * 0.8f) }

                    val content = readFileContent(file)
                    content.lines().forEachIndexed { lineIndex, line ->
                        if (importPattern.containsMatchIn(line)) {
                            val packageName = extractPackageName(line)
                            results.add(
                                ImportScanResult(
                                    originalImport = line.trim(),
                                    newImport = line.trim().replace("androidx.room", "androidx.room3"),
                                    filePath = file,
                                    lineNumber = lineIndex + 1,
                                    packageName = packageName
                                )
                            )
                        }
                    }
                }

                _state.update {
                    it.copy(
                        isScanning = false,
                        scanProgress = 1f,
                        scanProgressText = "Scan complete!",
                        importScanResults = results,
                        snackbarMessage = "Found ${results.size} imports to migrate"
                    )
                }

                _effect.emit(Room3ImportMigrationEffect.ShowSnackbar(
                    "Found ${results.size} imports to migrate"
                ))
            }
        } catch (e: Exception) {
            _state.update {
                it.copy(
                    isScanning = false,
                    snackbarMessage = "Scan failed: ${e.message}"
                )
            }
            _effect.emit(Room3ImportMigrationEffect.ShowSnackbar("Scan failed: ${e.message}"))
        }
    }

    // ==========================================================
    // SQLiteDriver Scan Logic / SQLiteDriver 扫描逻辑
    // ==========================================================

    /**
     * Run SQLiteDriver scan
     * 运行 SQLiteDriver 扫描
     */
    private fun runSQLiteDriverScan(simulate: Boolean) {
        viewModelScope.launch {
            _state.update {
                it.copy(
                    isScanning = true,
                    scanProgress = 0f,
                    scanProgressText = "Scanning for SupportSQLite usage..."
                )
            }

            if (simulate) {
                runSimulatedSQLiteDriverScan()
            } else {
                runRealSQLiteDriverScan()
            }
        }
    }

    private suspend fun runSimulatedSQLiteDriverScan() {
        delay(800) // simulate scan time
        _state.update {
            it.copy(
                isScanning = false,
                scanProgress = 1f,
                scanProgressText = "Scan complete!",
                sqliteDriverResults = SIMULATED_SQLITE_DRIVER_RESULTS,
                snackbarMessage = "Found ${SIMULATED_SQLITE_DRIVER_RESULTS.size} SupportSQLite usages"
            )
        }
        _effect.emit(Room3ImportMigrationEffect.ShowSnackbar(
            "Found ${SIMULATED_SQLITE_DRIVER_RESULTS.size} SupportSQLite usages"
        ))
    }

    private suspend fun runRealSQLiteDriverScan() {
        val projectPath = _state.value.projectPath
        if (projectPath.isBlank()) {
            _state.update {
                it.copy(
                    isScanning = false,
                    snackbarMessage = "Please enter project path first"
                )
            }
            _effect.emit(Room3ImportMigrationEffect.ShowSnackbar("Please enter project path first"))
            return
        }

        try {
            withContext(Dispatchers.IO) {
                val deprecatedApis = listOf(
                    "SupportSQLiteDatabase",
                    "SupportSQLiteOpenHelper",
                    "androidx.sqlite:sqlite"
                )
                val results = mutableListOf<SQLiteDriverResult>()
                val ktFiles = findKotlinFiles(projectPath)

                ktFiles.forEach { file ->
                    val content = readFileContent(file)
                    content.lines().forEachIndexed { lineIndex, line ->
                        for (api in deprecatedApis) {
                            if (line.contains(api) && !line.trim().startsWith("//")) {
                                val risk = when {
                                    api.contains("SupportSQLiteDatabase") -> RiskLevel.P0_CRITICAL
                                    api.contains("OpenHelper") -> RiskLevel.P0_CRITICAL
                                    else -> RiskLevel.P1_HIGH
                                }
                                results.add(
                                    SQLiteDriverResult(
                                        filePath = file,
                                        lineNumber = lineIndex + 1,
                                        apiUsage = api,
                                        suggestedFix = getSuggestedFix(api),
                                        riskLevel = risk,
                                        beforeCode = line.trim(),
                                        afterCode = getSuggestedCode(api)
                                    )
                                )
                            }
                        }
                    }
                }

                _state.update {
                    it.copy(
                        isScanning = false,
                        scanProgress = 1f,
                        sqliteDriverResults = results
                    )
                }
            }
        } catch (e: Exception) {
            _state.update {
                it.copy(
                    isScanning = false,
                    snackbarMessage = "Scan failed: ${e.message}"
                )
            }
        }
    }

    // ==========================================================
    // Suspend Guide / Suspend 指南
    // ==========================================================

    /**
     * Load suspend guide items
     * 加载 Suspend 指南条目
     */
    private fun loadSuspendGuide() {
        viewModelScope.launch {
            _state.update {
                it.copy(
                    isScanning = true,
                    scanProgressText = "Loading suspend migration guide..."
                )
            }
            delay(500)
            _state.update {
                it.copy(
                    isScanning = false,
                    suspendGuideItems = SIMULATED_SUSPEND_GUIDE_ITEMS
                )
            }
        }
    }

    // ==========================================================
    // Dual Version Strategies / 双版本兼容策略
    // ==========================================================

    /**
     * Load dual version strategies
     * 加载双版本兼容策略
     */
    private fun loadDualVersionStrategies() {
        viewModelScope.launch {
            _state.update {
                it.copy(
                    isScanning = true,
                    scanProgressText = "Loading dual-version strategies..."
                )
            }
            delay(400)
            _state.update {
                it.copy(
                    isScanning = false,
                    dualVersionStrategies = SIMULATED_DUAL_VERSION_STRATEGIES
                )
            }
        }
    }

    // ==========================================================
    // CI Checklist / CI 检查清单
    // ==========================================================

    /**
     * Load CI checklist items
     * 加载 CI 检查清单
     */
    private fun loadCIChecklist() {
        viewModelScope.launch {
            _state.update {
                it.copy(
                    isScanning = true,
                    scanProgressText = "Loading CI compliance checklist..."
                )
            }
            delay(400)
            val items = SIMULATED_CI_CHECKLIST
            val passed = items.count { it.isChecked }
            val failed = items.count { !it.isChecked && it.riskLevel == RiskLevel.P0_CRITICAL }
            val warning = items.count { !it.isChecked && it.riskLevel != RiskLevel.P0_CRITICAL }

            val overallRisk = when {
                failed > 0 -> RiskLevel.P0_CRITICAL
                warning > 0 -> RiskLevel.P1_HIGH
                passed == items.size -> RiskLevel.P3_LOW
                else -> RiskLevel.UNKNOWN
            }

            _state.update {
                it.copy(
                    isScanning = false,
                    ciChecklistItems = items,
                    ciCompliancePassed = passed,
                    ciComplianceFailed = failed,
                    ciComplianceWarning = warning,
                    overallRiskLevel = overallRisk
                )
            }
        }
    }

    // ==========================================================
    // Copy to Clipboard / 复制到剪贴板
    // ==========================================================

    private fun copyFixCode(code: String) {
        viewModelScope.launch {
            _effect.emit(Room3ImportMigrationEffect.CopyToClipboard(code))
            _effect.emit(Room3ImportMigrationEffect.ShowSnackbar("Code copied to clipboard!"))
        }
    }

    // ==========================================================
    // Checklist Toggle / 检查清单切换
    // ==========================================================

    private fun toggleChecklistItem(itemId: String, checked: Boolean) {
        _state.update { currentState ->
            val updatedItems = currentState.ciChecklistItems.map { item ->
                if (item.id == itemId) item.copy(isChecked = checked) else item
            }
            val passed = updatedItems.count { it.isChecked }
            val failed = updatedItems.count { !it.isChecked && it.riskLevel == RiskLevel.P0_CRITICAL }
            val warning = updatedItems.count { !it.isChecked && it.riskLevel != RiskLevel.P0_CRITICAL }

            val overallRisk = when {
                failed > 0 -> RiskLevel.P0_CRITICAL
                warning > 0 -> RiskLevel.P1_HIGH
                passed == updatedItems.size -> RiskLevel.P3_LOW
                else -> RiskLevel.UNKNOWN
            }

            currentState.copy(
                ciChecklistItems = updatedItems,
                ciCompliancePassed = passed,
                ciComplianceFailed = failed,
                ciComplianceWarning = warning,
                overallRiskLevel = overallRisk
            )
        }
    }

    // ==========================================================
    // File System Utilities / 文件系统工具
    // ==========================================================

    /**
     * Find all Kotlin files in project directory
     * 查找项目中所有 Kotlin 文件
     */
    private fun findKotlinFiles(projectPath: String): List<String> {
        return try {
            val process = Runtime.getRuntime().exec(
                arrayOf("find", projectPath, "-name", "*.kt", "-type", "f"),
            )
            process.inputStream.bufferedReader().readLines()
                .filter { !it.contains("/build/") && !it.contains("/.gradle/") }
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Read file content
     * 读取文件内容
     */
    private fun readFileContent(path: String): String {
        return try {
            java.io.File(path).readText()
        } catch (e: Exception) {
            ""
        }
    }

    /**
     * Extract package name from import statement
     * 从 import 语句提取包名
     */
    private fun extractPackageName(importLine: String): String {
        return importLine
            .removePrefix("import ")
            .trim()
            .substringAfterLast(".")
    }

    /**
     * Get suggested fix for deprecated API
     * 获取废弃 API 的建议修复方案
     */
    private fun getSuggestedFix(api: String): String {
        return when {
            api.contains("SupportSQLiteDatabase") ->
                "迁移到 SQLiteDriver，使用 SupportSQLiteQuery 替代原生 SQL"
            api.contains("OpenHelper") ->
                "SupportSQLiteOpenHelper 已在 Room 3.0 移除，使用 Room.databaseBuilder"
            api.contains("sqlite") ->
                "移除 legacy sqlite dependency，迁移到 Room 3.0 driver"
            else -> "检查 Room 3.0 文档获取替代方案"
        }
    }

    /**
     * Get suggested replacement code
     * 获取建议的替换代码
     */
    private fun getSuggestedCode(api: String): String {
        return when {
            api.contains("SupportSQLiteDatabase") -> "// 迁移到 AndroidDriver/SQLiteDriver\nval driver = AndroidDriver(context, \"myapp.db\")"
            api.contains("OpenHelper") -> "// 使用 Room.databaseBuilder + AndroidDriver"
            else -> "// 迁移到 Room 3.0 driver API"
        }
    }
}
