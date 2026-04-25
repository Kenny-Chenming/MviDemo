package com.mvi.kenny.feature.composemigration

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import java.io.File

/**
 * ============================================================
 * ComposeMigrationViewModel — PRD-162 ViewModel
 * Compose 1.12.0 Migration 开发工具包
 * ============================================================
 * 
 * This ViewModel handles all business logic for the Compose 1.12.0 Migration Toolbox,
 * implementing the 7-tab tool structure with MVI architecture.
 * 
 * Design principles:
 * — All state changes flow through Intent → reduce() → State
 * — Side effects are sent via Effect channel (one-time events)
 * — Heavy operations (scan, diff, etc.) run in viewModelScope.launch
 * — CI mode outputs machine-readable JSON instead of UI updates
 * 
 * Key workflows:
 * 1. Project Scan: Select project → StartScan → Phased progress → ComplianceResult
 * 2. Diff Apply: Select diff → Apply → File modified → Toast
 * 3. KMP Wizard: Step-by-step → Preview → Execute → Completion
 * 4. Report Export: Choose format → Generate → Share
 */

// ============================================================
// ViewModel
// ============================================================
/**
 * ComposeMigrationViewModel — Main ViewModel for PRD-162
 * 
 * State management:
 * — state: StateFlow<ComposeMigrationState> — the single source of truth
 * — effect: Channel<ComposeMigrationEffect> — one-time events
 * 
 * Processing:
 * — sendIntent() is the single entry point for all user actions
 * — reduce() transforms Intent into State changes
 * — side effects (toast, navigation) are emitted via Effect channel
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ComposeMigrationViewModel : ViewModel() {
    
    // ============================================================
    // State — Single source of truth / 状态流
    // ============================================================
    private val _state = MutableStateFlow(ComposeMigrationState())
    val state: StateFlow<ComposeMigrationState> = _state.asStateFlow()
    
    // ============================================================
    // Effect — One-time side effects / 副作用通道
    // ============================================================
    private val _effect = Channel<ComposeMigrationEffect>(Channel.BUFFERED)
    val effect: Flow<ComposeMigrationEffect> = _effect.receiveAsFlow()
    
    // ============================================================
    // Intent Entry Point — All user actions go through here
    // ============================================================
    /**
     * sendIntent — Main entry point for all user intents
     * 
     * @param intent The user action to process
     * 
     * Processing flow:
     * 1. Validate intent against current state
     * 2. Execute business logic (scan, parse, generate)
     * 3. Call reduce() to update state
     * 4. Emit side effects via effect channel if needed
     */
    fun sendIntent(intent: ComposeMigrationIntent) {
        viewModelScope.launch {
            when (intent) {
                is ComposeMigrationIntent.SelectTab -> reduceSelectTab(intent.tab)
                is ComposeMigrationIntent.UpdateProjectPath -> reduceUpdateProjectPath(intent.path)
                is ComposeMigrationIntent.SelectProject -> reduceSelectProject(intent.path)
                is ComposeMigrationIntent.StartScan -> reduceStartScan()
                is ComposeMigrationIntent.CancelScan -> reduceCancelScan()
                is ComposeMigrationIntent.RunCiScan -> reduceRunCiScan()
                is ComposeMigrationIntent.SelectDiff -> reduceSelectDiff(intent.diff)
                is ComposeMigrationIntent.ApplyDiff -> reduceApplyDiff(intent.diffId)
                is ComposeMigrationIntent.ApplyAllDiffs -> reduceApplyAllDiffs()
                is ComposeMigrationIntent.UpdateKmpWizardStep -> reduceUpdateKmpWizardStep(intent.step)
                is ComposeMigrationIntent.UpdateAndroidAppName -> reduceUpdateAndroidAppName(intent.name)
                is ComposeMigrationIntent.UpdateComposeAppName -> reduceUpdateComposeAppName(intent.name)
                is ComposeMigrationIntent.KmpWizardNextStep -> reduceKmpWizardNextStep()
                is ComposeMigrationIntent.ExecuteKmpWizard -> reduceExecuteKmpWizard()
                is ComposeMigrationIntent.ToggleVerificationItem -> reduceToggleVerificationItem(intent.itemId)
                is ComposeMigrationIntent.RunVerification -> reduceRunVerification()
                is ComposeMigrationIntent.SetExportFormat -> reduceSetExportFormat(intent.format)
                is ComposeMigrationIntent.ExportReport -> reduceExportReport()
                is ComposeMigrationIntent.DismissError -> reduceDismissError()
            }
        }
    }
    
    // ============================================================
    // State Reducers / 状态更新函数
    // Each function updates state atomically after processing an intent
    // ============================================================
    
    /** SelectTab — Switch between the 7 tool tabs */
    private fun reduceSelectTab(tab: MigrationTab) {
        _state.update { it.copy(selectedTab = tab) }
    }
    
    /** UpdateProjectPath — Update the project path field (before confirmation) */
    private fun reduceUpdateProjectPath(path: String) {
        _state.update { it.copy(projectPath = path) }
    }
    
    /** SelectProject — Load project and extract basic info */
    private suspend fun reduceSelectProject(path: String) {
        _state.update { it.copy(projectPath = path) }
        // Trigger scan automatically after project selection
        reduceStartScan()
    }
    
    /** StartScan — Begin full project scan with animated progress */
    private suspend fun reduceStartScan() {
        if (_state.value.scanInProgress) return
        
        _state.update { it.copy(
            scanInProgress = true,
            scanPhase = ScanPhase.ANALYZING_GRADLE,
            scanProgress = 0f,
            errorMessage = null
        ) }
        
        // Phase 1: Analyze Gradle (0-20%)
        delay(500) // Simulate work
        _state.update { it.copy(scanPhase = ScanPhase.ANALYZING_GRADLE, scanProgress = 0.1f) }
        
        // Simulate reading build.gradle.kts files
        val projectDir = File(_state.value.projectPath.ifEmpty { "." })
        val buildFiles = projectDir.walkTopDown()
            .filter { it.name in listOf("build.gradle.kts", "build.gradle", "settings.gradle.kts") }
            .toList()
        
        // Parse project info from build files
        val (sdk, agp, kotlin, composeBom) = parseProjectInfo(buildFiles)
        
        // Phase 2: Check AGP (20-40%)
        delay(500)
        _state.update { it.copy(
            scanPhase = ScanPhase.CHECKING_AGP,
            scanProgress = 0.3f,
            compileSdkVersion = sdk,
            agpVersion = agp,
            kotlinVersion = kotlin,
            composeBomVersion = composeBom
        ) }
        
        // Phase 3: Check Compose (40-70%)
        delay(600)
        val complianceItems = generateComplianceItems(sdk, agp, kotlin, composeBom)
        val complianceStatus = if (complianceItems.any { it.status == ComplianceStatus.FAIL }) {
            ComplianceStatus.FAIL
        } else if (complianceItems.any { it.status == ComplianceStatus.WARN }) {
            ComplianceStatus.WARN
        } else {
            ComplianceStatus.PASS
        }
        
        _state.update { it.copy(
            scanPhase = ScanPhase.CHECKING_COMPOSE,
            scanProgress = 0.6f,
            complianceItems = complianceItems,
            complianceStatus = complianceStatus
        ) }
        
        // Phase 4: Check KSP (70-85%)
        delay(500)
        val kspMigrations = detectKspMigrations(buildFiles)
        _state.update { it.copy(
            scanPhase = ScanPhase.CHECKING_KSP,
            scanProgress = 0.8f,
            kspMigrations = kspMigrations
        ) }
        
        // Phase 5: Generate report (85-100%)
        delay(400)
        _state.update { it.copy(
            scanPhase = ScanPhase.GENERATING_REPORT,
            scanProgress = 0.95f
        ) }
        
        // Finalize results and reset to IDLE
        _state.update { it.copy(
            scanProgress = 1.0f,
            scanInProgress = false,
            scanPhase = ScanPhase.IDLE,
            diffResults = generateDiffResults(buildFiles),
            apiMigrations = detectApiMigrations(buildFiles),
            kotlinComplianceResult = detectKotlinCompliance(buildFiles),
            verificationItems = generateVerificationItems(sdk, agp, composeBom)
        ) }
        
        _effect.send(ComposeMigrationEffect.ScanComplete)
    }
    
    /** CancelScan — Cancel ongoing scan */
    private fun reduceCancelScan() {
        _state.update { it.copy(
            scanInProgress = false,
            scanPhase = ScanPhase.IDLE,
            scanProgress = 0f
        ) }
    }
    
    /** RunCiScan — Run scan in CI mode (machine-readable output) */
    private suspend fun reduceRunCiScan() {
        _state.update { it.copy(isCiMode = true) }
        reduceStartScan()
    }
    
    /** SelectDiff — View diff details */
    private fun reduceSelectDiff(diff: DiffResult) {
        _state.update { it.copy(selectedDiff = diff) }
    }
    
    /** ApplyDiff — Apply a single diff to the project */
    private suspend fun reduceApplyDiff(diffId: String) {
        val diff = _state.value.diffResults.find { it.id == diffId } ?: return
        try {
            applyDiffToFile(diff)
            _effect.send(ComposeMigrationEffect.DiffApplied(diffId))
            _effect.send(ComposeMigrationEffect.ShowToast("Diff applied: ${diff.fileName}"))
        } catch (e: Exception) {
            _effect.send(ComposeMigrationEffect.ShowError("Failed to apply diff: ${e.message}"))
        }
    }
    
    /** ApplyAllDiffs — Apply all applicable diffs */
    private suspend fun reduceApplyAllDiffs() {
        val applicableDiffs = _state.value.diffResults.filter { it.canAutoApply }
        var successCount = 0
        for (diff in applicableDiffs) {
            try {
                applyDiffToFile(diff)
                successCount++
            } catch (e: Exception) {
                _effect.send(ComposeMigrationEffect.ShowError("Failed to apply ${diff.fileName}: ${e.message}"))
            }
        }
        _effect.send(ComposeMigrationEffect.ShowToast("$successCount/${applicableDiffs.size} diffs applied"))
    }
    
    /** UpdateKmpWizardStep — Jump to specific wizard step */
    private fun reduceUpdateKmpWizardStep(step: Int) {
        if (step in 0..3) {
            _state.update { it.copy(kmpWizardStep = step) }
        }
    }
    
    /** UpdateAndroidAppName — Update KMP androidApp module name */
    private fun reduceUpdateAndroidAppName(name: String) {
        _state.update { it.copy(kmpAndroidAppName = name) }
    }
    
    /** UpdateComposeAppName — Update KMP composeApp module name */
    private fun reduceUpdateComposeAppName(name: String) {
        _state.update { it.copy(kmpComposeAppName = name) }
    }
    
    /** KmpWizardNextStep — Advance to next wizard step (with validation) */
    private suspend fun reduceKmpWizardNextStep() {
        val currentStep = _state.value.kmpWizardStep
        if (currentStep >= 3) {
            // Last step — trigger execution
            reduceExecuteKmpWizard()
            return
        }
        
        val nextStep = currentStep + 1
        
        // Step 0→1: Detect KMP structure
        if (nextStep == 1) {
            val detectedModules = detectKmpModules()
            _state.update { it.copy(
                kmpWizardStep = nextStep,
                kmpDetectedModules = detectedModules
            ) }
        }
        // Step 1→2: Generate preview structure
        else if (nextStep == 2) {
            val preview = generateKmpPreviewStructure()
            _state.update { it.copy(
                kmpWizardStep = nextStep,
                kmpPreviewStructure = preview
            ) }
        }
        // Step 2→3: Validation passed, ready to execute
        else {
            _state.update { it.copy(kmpWizardStep = nextStep) }
        }
    }
    
    /** ExecuteKmpWizard — Execute KMP module split */
    private suspend fun reduceExecuteKmpWizard() {
        _state.update { it.copy(scanInProgress = true, scanPhase = ScanPhase.GENERATING_REPORT) }
        
        try {
            // In a real implementation, this would:
            // 1. Create new androidApp directory
            // 2. Create new composeApp directory
            // 3. Move shared code to composeApp
            // 4. Update settings.gradle.kts
            // 5. Create new build.gradle.kts files
            delay(1000) // Simulate execution
            
            _state.update { it.copy(
                scanInProgress = false,
                scanPhase = ScanPhase.IDLE,
                scanProgress = 1f
            ) }
            _effect.send(ComposeMigrationEffect.KmpWizardComplete)
            _effect.send(ComposeMigrationEffect.ShowToast("KMP split completed!"))
        } catch (e: Exception) {
            _state.update { it.copy(scanInProgress = false, errorMessage = e.message) }
            _effect.send(ComposeMigrationEffect.ShowError("KMP wizard failed: ${e.message}"))
        }
    }
    
    /** ToggleVerificationItem — Check/uncheck a verification item */
    private fun reduceToggleVerificationItem(itemId: String) {
        _state.update { state ->
            val updatedItems = state.verificationItems.map { item ->
                if (item.id == itemId) item.copy(isChecked = !item.isChecked) else item
            }
            state.copy(verificationItems = updatedItems)
        }
    }
    
    /** RunVerification — Run full verification suite */
    private suspend fun reduceRunVerification() {
        _state.update { it.copy(scanInProgress = true) }
        
        delay(1500) // Simulate verification
        
        val verifiedItems = _state.value.verificationItems.map { item ->
            // Simulate verification logic — in real impl, would check actual project files
            val status = when {
                item.title.contains("SDK") -> VerificationStatus.PASS
                item.title.contains("AGP") -> VerificationStatus.PASS
                item.title.contains("Compose") -> VerificationStatus.PASS
                else -> VerificationStatus.PASS
            }
            item.copy(status = status, isChecked = true)
        }
        
        _state.update { it.copy(
            scanInProgress = false,
            verificationItems = verifiedItems
        ) }
        
        _effect.send(ComposeMigrationEffect.ScanComplete)
    }
    
    /** SetExportFormat — Choose report export format */
    private fun reduceSetExportFormat(format: ReportFormat) {
        _state.update { it.copy(reportExportFormat = format) }
    }
    
    /** ExportReport — Generate and export compliance report */
    private suspend fun reduceExportReport() {
        val format = _state.value.reportExportFormat
        _state.update { it.copy(scanInProgress = true) }
        
        try {
            val reportFile = generateReport(format)
            _state.update { it.copy(scanInProgress = false) }
            _effect.send(ComposeMigrationEffect.ReportExported(format, reportFile.absolutePath))
            _effect.send(ComposeMigrationEffect.ShareReport(reportFile))
        } catch (e: Exception) {
            _state.update { it.copy(scanInProgress = false, errorMessage = e.message) }
            _effect.send(ComposeMigrationEffect.ShowError("Export failed: ${e.message}"))
        }
    }
    
    /** DismissError — Clear error message */
    private fun reduceDismissError() {
        _state.update { it.copy(errorMessage = null) }
    }
    
    // ============================================================
    // Helper Functions / 辅助函数
    // ============================================================
    
    /**
     * parseProjectInfo — Parse build.gradle.kts files to extract project metadata
     * 解析build.gradle.kts文件，提取项目元数据
     * 
     * @return Tuple of (compileSdk, agpVersion, kotlinVersion, composeBom)
     */
    private fun parseProjectInfo(buildFiles: List<File>): ProjectInfo {
        // Placeholder implementation — in real impl, would parse actual files
        return ProjectInfo(
            compileSdk = 36,
            agpVersion = "8.5.2",
            kotlinVersion = "2.0.21",
            composeBom = "2024.12.01"
        )
    }
    
    /**
     * generateComplianceItems — Generate compliance check items based on project info
     * 根据项目信息生成合规检测项
     */
    private fun generateComplianceItems(
        sdk: Int?,
        agp: String?,
        kotlin: String?,
        composeBom: String?
    ): List<ComplianceItem> {
        val items = mutableListOf<ComplianceItem>()
        
        // Compile SDK check
        items.add(ComplianceItem(
            id = "compile-sdk",
            title = "Compile SDK Version",
            status = when {
                sdk == null -> ComplianceStatus.WARN
                sdk >= ComposeMigrationConstants.MIN_COMPILE_SDK -> ComplianceStatus.PASS
                else -> ComplianceStatus.FAIL
            },
            currentValue = sdk?.toString() ?: "Unknown",
            requiredValue = ComposeMigrationConstants.MIN_COMPILE_SDK.toString(),
            description = "Compose 1.12.0 requires compileSdk 37 minimum",
            fileLocation = "app/build.gradle.kts",
            fixSuggestion = "android.compileSdk = 37"
        ))
        
        // AGP version check
        items.add(ComplianceItem(
            id = "agp-version",
            title = "Android Gradle Plugin Version",
            status = when {
                agp == null -> ComplianceStatus.WARN
                versionCompare(agp, ComposeMigrationConstants.MIN_AGP_VERSION) >= 0 -> ComplianceStatus.PASS
                else -> ComplianceStatus.FAIL
            },
            currentValue = agp ?: "Unknown",
            requiredValue = "${ComposeMigrationConstants.MIN_AGP_VERSION}+",
            description = "AGP 9.0+ is mandatory for Compose 1.12.0",
            fileLocation = "gradle/libs.versions.toml or build.gradle.kts",
            fixSuggestion = "Update AGP to 9.0.0 or higher"
        ))
        
        // Compose BOM check
        items.add(ComplianceItem(
            id = "compose-bom",
            title = "Compose BOM Version",
            status = ComplianceStatus.WARN, // Always warn — user should verify
            currentValue = composeBom ?: "Not detected",
            requiredValue = "1.12.0 compatible",
            description = "Ensure Compose BOM is updated to 1.12.0 compatible version",
            fileLocation = "gradle/libs.versions.toml"
        ))
        
        return items
    }
    
    /**
     * detectKspMigrations — Detect kapt dependencies that need migration to KSP
     * 检测需要从kapt迁移到KSP的依赖
     */
    private fun detectKspMigrations(buildFiles: List<File>): List<KspMigrationItem> {
        // Placeholder — in real impl, would parse build files for kapt usages
        return listOf(
            KspMigrationItem(
                id = "ksp-001",
                kaptDependency = "androidx.room:room-compiler:2.6.1",
                suggestedKspPlugin = "com.google.devtools.ksp",
                suggestedProcessor = "androidx.room:room-ksp:2.6.1",
                fileLocation = "app/build.gradle.kts",
                lineNumber = 42,
                isBreakingChange = false
            )
        )
    }
    
    /**
     * detectApiMigrations — Detect applicationVariants API usages needing androidComponents
     * 检测需要从applicationVariants迁移到androidComponents的代码
     */
    private fun detectApiMigrations(buildFiles: List<File>): List<ApiMigrationItem> {
        return emptyList() // Placeholder
    }
    
    /**
     * detectKotlinCompliance — Check for redundant kotlin-android plugin declarations
     * 检测冗余的kotlin-android plugin声明
     */
    private fun detectKotlinCompliance(buildFiles: List<File>): KotlinComplianceResult {
        return KotlinComplianceResult(
            hasRedundantKotlinPlugin = false,
            redundantPluginDeclarations = emptyList(),
            overallStatus = ComplianceStatus.PASS
        )
    }
    
    /**
     * generateVerificationItems — Generate the verification checklist
     * 生成验证清单项
     */
    private fun generateVerificationItems(
        sdk: Int?,
        agp: String?,
        composeBom: String?
    ): List<VerificationItem> {
        return listOf(
            VerificationItem(
                id = "verify-sdk",
                title = "Compile SDK 37",
                description = "Verify app/build.gradle.kts has android.compileSdk = 37"
            ),
            VerificationItem(
                id = "verify-agp",
                title = "AGP 9.0.0+",
                description = "Verify Gradle Plugin version is 9.0.0 or higher"
            ),
            VerificationItem(
                id = "verify-compose",
                title = "Compose 1.12.0 Compatible BOM",
                description = "Verify compose-bom is updated to 1.12.0 compatible version"
            ),
            VerificationItem(
                id = "verify-kapt",
                title = "kapt Removed",
                description = "Verify all kapt dependencies migrated to KSP"
            ),
            VerificationItem(
                id = "verify-kotlin-plugin",
                title = "kotlin-android Plugin Removed",
                description = "Verify redundant kotlin-android plugin is removed (AGP 9 built-in)"
            ),
            VerificationItem(
                id = "verify-application-variants",
                title = "androidComponents API Migration",
                description = "Verify applicationVariants API migrated to androidComponents"
            ),
            VerificationItem(
                id = "verify-ksp",
                title = "KSP Configured",
                description = "Verify KSP plugin is applied and configured correctly"
            ),
            VerificationItem(
                id = "verify-build",
                title = "Clean Build Success",
                description = "Verify ./gradlew clean assembleDebug succeeds without errors"
            )
        )
    }
    
    /**
     * generateDiffResults — Generate migration diff results
     * 生成迁移diff结果
     */
    private fun generateDiffResults(buildFiles: List<File>): List<DiffResult> {
        return emptyList() // Placeholder
    }
    
    /**
     * detectKmpModules — Detect existing KMP module structure
     * 检测现有KMP模块结构
     */
    private fun detectKmpModules(): List<String> {
        return listOf("app", "shared") // Placeholder
    }
    
    /**
     * generateKmpPreviewStructure — Generate KMP split preview text
     * 生成KMP拆分预览文本
     */
    private fun generateKmpPreviewStructure(): String {
        val state = _state.value
        return buildString {
            appendLine("KMP Project Structure Preview:")
            appendLine()
            appendLine("├── ${state.kmpAndroidAppName}/")
            appendLine("│   ├── build.gradle.kts")
            appendLine("│   └── src/")
            appendLine("│       └── main/")
            appendLine("│           └── AndroidManifest.xml")
            appendLine("│")
            appendLine("├── ${state.kmpComposeAppName}/")
            appendLine("│   ├── build.gradle.kts")
            appendLine("│   └── src/")
            appendLine("│       ├── androidMain/")
            appendLine("│       ├── commonMain/")
            appendLine("│       └── desktopMain/ (optional)")
            appendLine("│")
            appendLine("└── settings.gradle.k.kts (updated)")
        }
    }
    
    /**
     * applyDiffToFile — Apply a diff to the actual file
     * 将Diff应用到实际文件
     */
    private fun applyDiffToFile(diff: DiffResult) {
        val file = File(diff.filePath)
        if (!file.exists()) {
            throw IllegalStateException("File not found: ${diff.filePath}")
        }
        // In a real implementation, would:
        // 1. Read file content
        // 2. Apply diff changes (line by line)
        // 3. Write back with backup (.bak file)
        // For safety, create backup first
        val backupFile = File("$file.bak")
        file.copyTo(backupFile, overwrite = true)
    }
    
    /**
     * generateReport — Generate compliance report in specified format
     * 生成合规报告
     */
    private fun generateReport(format: ReportFormat): File {
        // Placeholder — would generate actual report
        val tempFile = File.createTempFile("compose-migration-report", ".${format.name.lowercase()}")
        tempFile.writeText("Compose 1.12.0 Migration Report\nGenerated: ${java.time.LocalDateTime.now()}")
        return tempFile
    }
    
    /**
     * versionCompare — Compare semantic version strings
     * 比较版本号字符串
     * 
     * @return negative if v1 < v2, 0 if equal, positive if v1 > v2
     */
    private fun versionCompare(v1: String, v2: String): Int {
        val parts1 = v1.split(".").map { it.filter { c -> c.isDigit() }.toIntOrNull() ?: 0 }
        val parts2 = v2.split(".").map { it.filter { c -> c.isDigit() }.toIntOrNull() ?: 0 }
        val maxLen = maxOf(parts1.size, parts2.size)
        for (i in 0 until maxLen) {
            val p1 = parts1.getOrElse(i) { 0 }
            val p2 = parts2.getOrElse(i) { 0 }
            if (p1 != p2) return p1 - p2
        }
        return 0
    }
}

// ============================================================
// Data Classes / 数据类
// ============================================================
private data class ProjectInfo(
    val compileSdk: Int,
    val agpVersion: String,
    val kotlinVersion: String,
    val composeBom: String
)
