package com.mvi.kenny.feature.corektx

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
 * CoreKtxMigrationViewModel — AndroidX core-ktx 迁移工具 ViewModel
 * ================================================================
 * MVI architecture ViewModel for AndroidX core-ktx → core migration toolkit.
 *
 * PRD-115: AndroidX core-ktx 合并至 core 历史性迁移检测与自动化工具包
 * Design Reference: memory/agency/designs/PRD-115-AndroidX-core-ktx-历史性迁移检测与自动化工具包.md
 *
 * Key behaviors:
 * 1. Scans build.gradle files for core-ktx dependency declarations
 * 2. Validates Kotlin extension function import compatibility
 * 3. Analyzes third-party library core-ktx dependency impact
 * 4. Provides automated fix for common migration scenarios
 * 5. CI/CD compliance checking with curl command examples
 * —————————————————————————————————————————————————————
 */
class CoreKtxMigrationViewModel : ViewModel() {

    // MVI State — single source of truth using StateFlow
    private val _state = MutableStateFlow(CoreKtxMigrationState())
    val state: StateFlow<CoreKtxMigrationState> = _state.asStateFlow()

    // MVI Effect — one-time side effects via Channel
    private val _effect = Channel<CoreKtxMigrationEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    // Date formatter for timestamps
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())

    init {
        // Initialize with mock compliance rules / 初始化模拟合规检查规则
        loadMockComplianceRules()
    }

    /**
     * ============================================================
     * sendIntent — 处理用户意图
     * ================================================================
     * Entry point for all user intents. Maps Intent → Business Logic.
     *
     * @param intent User intent from the UI layer
     */
    fun sendIntent(intent: CoreKtxMigrationIntent) {
        when (intent) {
            is CoreKtxMigrationIntent.StartScan -> startScan()
            is CoreKtxMigrationIntent.RefreshScan -> refreshScan()
            is CoreKtxMigrationIntent.FilterBySeverity -> filterBySeverity(intent.severity)
            is CoreKtxMigrationIntent.FixItem -> prepareFix(intent.itemId)
            is CoreKtxMigrationIntent.ConfirmFix -> confirmFix()
            is CoreKtxMigrationIntent.CancelFix -> cancelFix()
            is CoreKtxMigrationIntent.FixAll -> fixAll()
            is CoreKtxMigrationIntent.ValidateImports -> validateImports(intent.path)
            is CoreKtxMigrationIntent.ClearValidation -> clearValidation()
            is CoreKtxMigrationIntent.UpdateSettings -> updateSettings(intent.settings)
            is CoreKtxMigrationIntent.SelectTab -> selectTab(intent.tabIndex)
        }
    }

    // ================================================================
    // Scanning / 扫描逻辑
    // ================================================================

    /**
     * 开始扫描项目中的 core-ktx 使用情况
     * Start scanning project for core-ktx usage.
     */
    private fun startScan() {
        viewModelScope.launch {
            _state.value = _state.value.copy(
                scanState = ScanState.Scanning,
                scanProgress = 0f,
                isLoading = true,
                error = null
            )

            try {
                // Simulate scanning progress / 模拟扫描进度
                delay(300)
                _state.value = _state.value.copy(scanProgress = 0.2f)

                // Mock scan results for demonstration
                // In production, this would parse build.gradle files
                val mockResults = generateMockScanResults()

                delay(300)
                _state.value = _state.value.copy(scanProgress = 0.6f)

                delay(300)
                _state.value = _state.value.copy(scanProgress = 1.0f)

                // Calculate health score / 计算健康度评分
                val criticalCount = mockResults.count { it.severity == Severity.P0 }
                val warningCount = mockResults.count { it.severity == Severity.P1 }
                val healthScore = calculateHealthScore(criticalCount, warningCount, mockResults.size)

                _state.value = _state.value.copy(
                    scanState = ScanState.Completed,
                    scanResults = mockResults,
                    healthScore = healthScore,
                    totalDependencies = 47,  // Mock total
                    affectedDependencies = mockResults.size,
                    isLoading = false
                )

                _effect.send(CoreKtxMigrationEffect.ScanCompleted(
                    totalFound = mockResults.size,
                    criticalCount = criticalCount
                ))

            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    scanState = ScanState.Failed,
                    isLoading = false,
                    error = e.message ?: "扫描失败 / Scan failed"
                )
                _effect.send(CoreKtxMigrationEffect.ShowError(
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
     * 按严重程度过滤扫描结果
     * Filter scan results by severity level.
     */
    private fun filterBySeverity(severity: Set<Severity>) {
        _state.value = _state.value.copy(filterSeverity = severity)
    }

    // ================================================================
    // Fixing / 修复逻辑
    // ================================================================

    /**
     * 准备修复单个问题项
     * Prepare to fix a single issue item.
     */
    private fun prepareFix(itemId: String) {
        val item = _state.value.scanResults.find { it.id == itemId }
        if (item != null) {
            _state.value = _state.value.copy(
                showFixDialog = true,
                pendingFixItem = item
            )
            viewModelScope.launch {
                _effect.send(CoreKtxMigrationEffect.ShowFixConfirmation(
                    item = item,
                    onConfirm = { sendIntent(CoreKtxMigrationIntent.ConfirmFix) },
                    onDismiss = { sendIntent(CoreKtxMigrationIntent.CancelFix) }
                ))
            }
        }
    }

    /**
     * 确认执行修复操作
     * Confirm and execute the fix operation.
     */
    private fun confirmFix() {
        val pendingItem = _state.value.pendingFixItem ?: return
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)

            // Simulate fix operation / 模拟修复操作
            delay(500)

            val updatedResults = _state.value.scanResults.map {
                if (it.id == pendingItem.id) it.copy(isFixed = true) else it
            }

            val fixedCount = updatedResults.count { it.isFixed }

            _state.value = _state.value.copy(
                scanResults = updatedResults,
                showFixDialog = false,
                pendingFixItem = null,
                isLoading = false,
                healthScore = calculateHealthScore(
                    updatedResults.count { it.severity == Severity.P0 && !it.isFixed },
                    updatedResults.count { it.severity == Severity.P1 && !it.isFixed },
                    updatedResults.size
                )
            )

            _effect.send(CoreKtxMigrationEffect.FixCompleted(1))
            _effect.send(CoreKtxMigrationEffect.ShowToast("已修复 / Fixed: ${pendingItem.filePath}"))

        }
    }

    /**
     * 取消修复操作
     * Cancel the fix operation.
     */
    private fun cancelFix() {
        _state.value = _state.value.copy(
            showFixDialog = false,
            pendingFixItem = null
        )
    }

    /**
     * 修复所有符合条件的问题项
     * Fix all issues that meet the criteria.
     */
    private fun fixAll() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)

            // Simulate fix operation / 模拟修复操作
            delay(800)

            val settings = _state.value.settings
            val fixableItems = _state.value.scanResults.filter {
                it.fixAvailable &&
                !it.isFixed &&
                it.severity.order <= settings.minSeverityForFix.order
            }

            val updatedResults = _state.value.scanResults.map { item ->
                if (fixableItems.any { it.id == item.id }) item.copy(isFixed = true)
                else item
            }

            val fixedCount = fixableItems.size

            _state.value = _state.value.copy(
                scanResults = updatedResults,
                isLoading = false,
                healthScore = 100
            )

            _effect.send(CoreKtxMigrationEffect.FixCompleted(fixedCount))
            _effect.send(CoreKtxMigrationEffect.ShowToast(
                "已修复 $fixedCount 项 / Fixed $fixedCount items"
            ))
        }
    }

    // ================================================================
    // Validation / 验证逻辑
    // ================================================================

    /**
     * 验证指定路径的 Kotlin 扩展函数 import 兼容性
     * Validate Kotlin extension function import compatibility for given path.
     */
    private fun validateImports(path: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)

            // Simulate validation / 模拟验证过程
            delay(600)

            val mockValidationResults = generateMockValidationResults(path)

            _state.value = _state.value.copy(
                validatorResults = mockValidationResults,
                isLoading = false
            )

        }
    }

    /**
     * 清空验证结果
     * Clear validation results.
     */
    private fun clearValidation() {
        _state.value = _state.value.copy(
            validatorResults = emptyList(),
            validatorInput = ""
        )
    }

    // ================================================================
    // Settings / 设置逻辑
    // ================================================================

    /**
     * 更新迁移工具设置
     * Update migration tool settings.
     */
    private fun updateSettings(settings: MigrationSettings) {
        _state.value = _state.value.copy(settings = settings)
        viewModelScope.launch {
            _effect.send(CoreKtxMigrationEffect.ShowToast("设置已保存 / Settings saved"))
        }
    }

    // ================================================================
    // Navigation / 导航逻辑
    // ================================================================

    /**
     * 切换 Tab 页
     * Switch tab page.
     */
    private fun selectTab(tabIndex: Int) {
        _state.value = _state.value.copy(selectedTab = tabIndex)
    }

    // ================================================================
    // Helper Methods / 辅助方法
    // ================================================================

    /**
     * 计算健康度评分
     * Calculate migration health score based on remaining issues.
     */
    private fun calculateHealthScore(critical: Int, warning: Int, total: Int): Int {
        if (total == 0) return 100
        val penalty = critical * 30 + warning * 10
        return maxOf(0, minOf(100, 100 - penalty))
    }

    /**
     * 生成模拟扫描结果（演示用）
     * Generate mock scan results for demonstration.
     */
    private fun generateMockScanResults(): List<ScanResult> {
        return listOf(
            ScanResult(
                id = UUID.randomUUID().toString(),
                filePath = "app/build.gradle.kts",
                lineNumber = 24,
                content = """implementation("androidx.core:core-ktx:1.15.0")""",
                severity = Severity.P0,
                fixAvailable = true,
                fixSuggestion = """Replace with: implementation("androidx.core:core:1.15.0")"""
            ),
            ScanResult(
                id = UUID.randomUUID().toString(),
                filePath = "feature/home/build.gradle.kts",
                lineNumber = 18,
                content = """implementation("androidx.core:core-ktx:+")""",
                severity = Severity.P0,
                fixAvailable = true,
                fixSuggestion = """Pin to specific version: implementation("androidx.core:core:1.15.0")"""
            ),
            ScanResult(
                id = UUID.randomUUID().toString(),
                filePath = "build.gradle.kts",
                lineNumber = 45,
                content = """implementation("androidx.core:core-ktx:1.12.0")""",
                severity = Severity.P1,
                fixAvailable = true,
                fixSuggestion = """Upgrade to core: implementation("androidx.core:core:1.15.0")"""
            ),
            ScanResult(
                id = UUID.randomUUID().toString(),
                filePath = "settings.gradle.kts",
                lineNumber = 12,
                content = """implementation("androidx.core:core-ktx")""",
                severity = Severity.P1,
                fixAvailable = true,
                fixSuggestion = """Remove transitive core-ktx: configurationManagement { ... }"""
            ),
            ScanResult(
                id = UUID.randomUUID().toString(),
                filePath = "app/proguard-rules.pro",
                lineNumber = 87,
                content = """-keep class androidx.core.** { *; }""",
                severity = Severity.P2,
                fixAvailable = true,
                fixSuggestion = """No change needed - androidx.core covers both core and core-ktx"""
            )
        )
    }

    /**
     * 生成模拟验证结果（演示用）
     * Generate mock validation results for demonstration.
     */
    private fun generateMockValidationResults(path: String): List<ValidationResult> {
        return listOf(
            ValidationResult(
                filePath = "$path/CoreExtensions.kt",
                importStatement = "androidx.core.content.pm.core-ktx",
                isCompatible = true,
                status = ValidationStatus.Pass,
                message = "Import 兼容 / Import is compatible after migration"
            ),
            ValidationResult(
                filePath = "$path/ContextExtensions.kt",
                importStatement = "androidx.core.app.core-ktx",
                isCompatible = true,
                status = ValidationStatus.Pass,
                message = "All extension functions available in androidx.core"
            ),
            ValidationResult(
                filePath = "$path/ActivityExtensions.kt",
                importStatement = "androidx.core.app.ActivityCompat",
                isCompatible = true,
                status = ValidationStatus.Pass,
                message = "ActivityCompat API unchanged in core"
            ),
            ValidationResult(
                filePath = "$path/os/HandlerExt.kt",
                importStatement = "androidx.core.os.Handler",
                isCompatible = true,
                status = ValidationStatus.Pass,
                message = "Handler API fully compatible"
            )
        )
    }

    /**
     * 加载模拟合规检查规则
     * Load mock compliance rules.
     */
    private fun loadMockComplianceRules() {
        val rules = listOf(
            ComplianceRule(
                id = "rule-1",
                name = "禁止声明 core-ktx 依赖",
                description = "检测 build.gradle 中是否仍有 core-ktx 依赖声明",
                enabled = true,
                level = ComplianceLevel.Warning,
                lastChecked = dateFormat.format(Date()),
                curlCommand = "curl -X POST https://api.example.com/compliance/check \\\n  -H \"Authorization: Bearer \${'$'}CI_JOB_TOKEN\" \\\n  -d '{\"project\":\"\${'$'}CI_PROJECT_PATH\",\"check\":\"no-core-ktx\"}'"
            ),
            ComplianceRule(
                id = "rule-2",
                name = "R8 规则检查",
                description = "确保 R8/Proguard 规则已从 core-ktx 迁移到 core",
                enabled = true,
                level = ComplianceLevel.Compliant,
                lastChecked = dateFormat.format(Date()),
                curlCommand = "curl -X POST https://api.example.com/compliance/check \\\n  -H \"Authorization: Bearer \${'$'}CI_JOB_TOKEN\" \\\n  -d '{\"project\":\"\${'$'}CI_PROJECT_PATH\",\"check\":\"r8-rules\"}'"
            ),
            ComplianceRule(
                id = "rule-3",
                name = "minSdk 兼容性检查",
                description = "验证 core 升级后 minSdk 要求是否满足",
                enabled = true,
                level = ComplianceLevel.Compliant,
                lastChecked = dateFormat.format(Date()),
                curlCommand = "curl -X POST https://api.example.com/compliance/check \\\n  -H \"Authorization: Bearer \${'$'}CI_JOB_TOKEN\" \\\n  -d '{\"project\":\"\${'$'}CI_PROJECT_PATH\",\"check\":\"min-sdk\"}'"
            ),
            ComplianceRule(
                id = "rule-4",
                name = "第三方库依赖审查",
                description = "检查项目中第三方库是否仍依赖 core-ktx",
                enabled = true,
                level = ComplianceLevel.Warning,
                lastChecked = dateFormat.format(Date()),
                curlCommand = "curl -X POST https://api.example.com/compliance/check \\\n  -H \"Authorization: Bearer \${'$'}CI_JOB_TOKEN\" \\\n  -d '{\"project\":\"\${'$'}CI_PROJECT_PATH\",\"check\":\"third-party-deps\"}'"
            )
        )
        _state.value = _state.value.copy(complianceRules = rules)
    }
}
