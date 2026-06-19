package com.mvi.kenny.feature.mdcviewscomposemigration

// ================================================================
// MdcToComposeToolViewModel — MDC-Android Views → Compose 迁移工具包 MVI ViewModel
// ================================================================
// ViewModel for MDC-Android Views → Compose Migration Toolkit.
//
// PRD-283: Android MDC-Views → Compose 迁移工具包
// Design Reference: memory/agency/designs/PRD-283-Android-MDC-Views-Compose迁移工具包.md
//
// Implements MVI pattern: Intent → ViewModel → State/Effect
//
// Key responsibilities:
//   - Scanner: scan project for MDC dependency + XML layouts, generate scan result
//   - Migration: display ordered migration steps, track progress, batch mode
//   - Report: generate migration completion report, export to Markdown/JSON
//   - Reference: searchable MDC Views → Compose component mapping table
//
// Architecture:
//   - State: immutable data class, single source of truth
//   - Intent: user actions, processed by sendIntent()
//   - Effect: one-time side effects via SharedFlow (toast, clipboard, share)
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
 * MdcToComposeToolViewModel — 迁移工具包 ViewModel
 * ============================================================
 * Manages the MdcToComposeToolState and processes MdcToComposeToolIntent.
 *
 * @see MdcToComposeToolState
 * @see MdcToComposeToolIntent
 * @see MdcToComposeToolEffect
 */
class MdcToComposeToolViewModel : ViewModel() {

    // ─────────────────────────────────────────────────────────────
    // State — Single source of truth, exposed as immutable StateFlow
    // ─────────────────────────────────────────────────────────────
    private val _state = MutableStateFlow(MdcToComposeToolState.Initial)
    val state: StateFlow<MdcToComposeToolState> = _state.asStateFlow()

    // ─────────────────────────────────────────────────────────────
    // Effect — One-time events via SharedFlow
    // ─────────────────────────────────────────────────────────────
    private val _effect = MutableSharedFlow<MdcToComposeToolEffect>()
    val effect = _effect.asSharedFlow()

    // ─────────────────────────────────────────────────────────────
    // Public API for UI to send intents
    // ─────────────────────────────────────────────────────────────
    /**
     * Send user intent to ViewModel for processing.
     *
     * @param intent User intent action
     */
    fun sendIntent(intent: MdcToComposeToolIntent) {
        viewModelScope.launch {
            when (intent) {
                is MdcToComposeToolIntent.SelectTab -> handleSelectTab(intent.tab)
                is MdcToComposeToolIntent.UpdateProjectPath -> handleUpdateProjectPath(intent.path)
                is MdcToComposeToolIntent.StartScan -> handleStartScan(intent.path)
                is MdcToComposeToolIntent.SelectMigrationStep -> handleSelectMigrationStep(intent.index)
                is MdcToComposeToolIntent.ToggleBatchMode -> handleToggleBatchMode(intent.enabled)
                is MdcToComposeToolIntent.MarkStepCompleted -> handleMarkStepCompleted(intent.index)
                is MdcToComposeToolIntent.SearchComponents -> handleSearchComponents(intent.query)
                is MdcToComposeToolIntent.CopyMappingNote -> handleCopyMappingNote(intent.mapping)
                is MdcToComposeToolIntent.ExportReport -> handleExportReport(intent.format)
                is MdcToComposeToolIntent.DismissError -> handleDismissError()
            }
        }
    }

    // ─────────────────────────────────────────────────────────────
    // Intent Handlers
    // ─────────────────────────────────────────────────────────────

    /**
     * Handle tab selection — switch bottom navigation Tab.
     *
     * @param tab Target tab to switch to
     */
    private fun handleSelectTab(tab: MdcTab) {
        _state.update { it.copy(selectedTab = tab) }
    }

    /**
     * Handle project path input update.
     *
     * @param path Updated project path string
     */
    private fun handleUpdateProjectPath(path: String) {
        _state.update {
            it.copy(scannerState = it.scannerState.copy(projectPath = path))
        }
    }

    /**
     * Handle scan trigger — analyze project for MDC dependencies and XML layouts.
     * In production, this would parse actual build.gradle files and XML layouts.
     *
     * @param path Project path to scan (currently unused, uses simulated data)
     */
    private suspend fun handleStartScan(path: String) {
        _state.update {
            it.copy(
                scannerState = it.scannerState.copy(
                    scanState = ScanState.RUNNING,
                    scanProgress = 0f
                )
            )
        }

        withContext(Dispatchers.Default) {
            // Simulate scanning with realistic progress updates
            for (progress in listOf(0.1f, 0.3f, 0.5f, 0.7f, 0.9f, 1.0f)) {
                delay(400)
                _state.update {
                    it.copy(
                        scannerState = it.scannerState.copy(scanProgress = progress)
                    )
                }
            }

            // Use simulated scan result (in production: parse actual project files)
            val scanResult = SIMULATED_SCAN_RESULT

            _state.update {
                it.copy(
                    scannerState = it.scannerState.copy(
                        scanState = ScanState.SUCCESS,
                        scanProgress = 1.0f,
                        scanResult = scanResult
                    )
                )
            }

            // Pre-populate migration steps based on scan result
            val migrationSteps = SIMULATED_MIGRATION_STEPS.mapIndexed { index, step ->
                val rec = scanResult.priorityRecommendations.find { r ->
                    r.targetComponents.any { step.componentType in it }
                }
                step.copy(
                    priority = rec?.priority ?: step.priority
                )
            }

            _state.update {
                it.copy(migrationState = it.migrationState.copy(migrationSteps = migrationSteps))
            }

            _effect.emit(
                MdcToComposeToolEffect.ShowSnackbar(
                    "✅ 扫描完成！发现 ${scanResult.xmlFileCount} 个 XML 文件，" +
                    "${scanResult.componentStats.sumOf { it.count }} 个 Views 组件"
                )
            )
        }
    }

    /**
     * Handle migration step selection — show step detail.
     *
     * @param index Selected step index
     */
    private fun handleSelectMigrationStep(index: Int) {
        _state.update {
            val steps = it.migrationState.migrationSteps
            it.copy(
                migrationState = it.migrationState.copy(
                    currentStepIndex = index,
                    selectedStepDetail = steps.getOrNull(index)
                )
            )
        }
    }

    /**
     * Handle batch mode toggle.
     *
     * @param enabled Whether batch mode should be enabled
     */
    private fun handleToggleBatchMode(enabled: Boolean) {
        _state.update {
            it.copy(
                migrationState = it.migrationState.copy(batchModeEnabled = enabled)
            )
        }

        viewModelScope.launch {
            if (enabled) {
                _effect.emit(
                    MdcToComposeToolEffect.ShowSnackbar(
                        "⚠️ 批量模式已开启：低难度组件将自动标记为完成"
                    )
                )
            }
        }
    }

    /**
     * Handle marking a migration step as completed.
     *
     * @param index Step index to mark completed
     */
    private suspend fun handleMarkStepCompleted(index: Int) {
        _state.update {
            val steps = it.migrationState.migrationSteps.toMutableList()
            if (index in steps.indices) {
                steps[index] = steps[index].copy(status = MigrationStepStatus.COMPLETED)
            }

            // Update report state based on completed steps
            val completedCount = steps.count { it.status == MigrationStepStatus.COMPLETED }
            val totalCount = steps.size
            val completionPct = if (totalCount > 0) (completedCount * 100 / totalCount) else 0

            it.copy(
                migrationState = it.migrationState.copy(migrationSteps = steps),
                reportState = it.reportState.copy(
                    completionPercentage = completionPct
                )
            )
        }

        // If batch mode → auto-complete all remaining steps
        val currentState = _state.value
        if (currentState.migrationState.batchModeEnabled) {
            autoCompleteRemainingSteps()
        }
    }

    /**
     * Auto-complete all remaining NOT_STARTED steps when batch mode is enabled.
     * Called after marking a step as completed in batch mode.
     */
    private suspend fun autoCompleteRemainingSteps() {
        _state.update {
            val steps = it.migrationState.migrationSteps.map { step ->
                if (step.status == MigrationStepStatus.NOT_STARTED) {
                    step.copy(status = MigrationStepStatus.COMPLETED)
                } else {
                    step
                }
            }
            val completedCount = steps.count { it.status == MigrationStepStatus.COMPLETED }
            val totalCount = steps.size
            val completionPct = if (totalCount > 0) (completedCount * 100 / totalCount) else 0

            it.copy(
                migrationState = it.migrationState.copy(migrationSteps = steps),
                reportState = it.reportState.copy(
                    completionPercentage = completionPct
                )
            )
        }
    }

    /**
     * Handle component search in the reference tab.
     *
     * @param query Search query string
     */
    private fun handleSearchComponents(query: String) {
        val filtered = if (query.isBlank()) {
            SIMULATED_COMPONENT_MAPPINGS
        } else {
            SIMULATED_COMPONENT_MAPPINGS.filter {
                it.viewsName.contains(query, ignoreCase = true) ||
                it.composeImport.contains(query, ignoreCase = true) ||
                it.migrationNote.contains(query, ignoreCase = true)
            }
        }

        _state.update {
            it.copy(
                referenceState = it.referenceState.copy(
                    searchQuery = query,
                    filteredComponents = filtered
                )
            )
        }
    }

    /**
     * Handle copying component mapping note to clipboard.
     *
     * @param mapping Component mapping to copy
     */
    private suspend fun handleCopyMappingNote(mapping: ComponentMapping) {
        val content = buildString {
            appendLine("=== ${mapping.viewsName} → ${mapping.composeImport.substringAfterLast(".")} ===")
            appendLine("Views: ${mapping.viewsImport}")
            appendLine("Compose: ${mapping.composeImport}")
            appendLine("Migration: ${mapping.migrationNote}")
            appendLine("Difficulty: ${mapping.effort.label}")
        }
        _effect.emit(MdcToComposeToolEffect.CopyToClipboard(content, "Component Mapping"))
        _effect.emit(MdcToComposeToolEffect.ShowSnackbar("✅ 组件映射已复制到剪贴板"))
    }

    /**
     * Handle report export.
     *
     * @param format Export format (Markdown or JSON)
     */
    private suspend fun handleExportReport(format: ExportFormat) {
        val currentState = _state.value
        val report = buildMigrationReport(currentState)
        val content = when (format) {
            ExportFormat.MARKDOWN -> generateMarkdownReport(report, currentState)
            ExportFormat.JSON -> generateJsonReport(report)
        }
        _effect.emit(MdcToComposeToolEffect.ShareReport(content, format))
    }

    /**
     * Build migration report from current state.
     */
    private fun buildMigrationReport(state: MdcToComposeToolState): MigrationReport {
        val migrationState = state.migrationState
        val completedCount = migrationState.migrationSteps.count { it.status == MigrationStepStatus.COMPLETED }
        val totalCount = migrationState.migrationSteps.size
        val scanResult = state.scannerState.scanResult

        return MigrationReport(
            generatedAt = System.currentTimeMillis(),
            totalSteps = totalCount,
            completedSteps = completedCount,
            totalComponents = scanResult?.componentStats?.sumOf { it.count } ?: 0,
            migratedComponents = 0, // Would be derived from actual migrated components
            skippedSteps = 0,
            recommendations = scanResult?.priorityRecommendations ?: emptyList()
        )
    }

    /**
     * Generate Markdown-formatted migration report.
     */
    private fun generateMarkdownReport(report: MigrationReport, state: MdcToComposeToolState): String {
        return buildString {
            appendLine("# MDC-Android → Compose 迁移报告")
            appendLine()
            appendLine("**生成时间**: ${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date(report.generatedAt))}")
            appendLine("**项目**: ${state.scannerState.scanResult?.projectName ?: "N/A"}")
            appendLine()
            appendLine("## 迁移进度")
            appendLine()
            appendLine("| 指标 | 值 |")
            appendLine("|------|-----|")
            appendLine("| 总步骤 | ${report.totalSteps} |")
            appendLine("| 已完成 | ${report.completedSteps} |")
            appendLine("| 完成率 | ${report.completedSteps * 100 / report.totalSteps}% |")
            appendLine("| 总组件数 | ${report.totalComponents} |")
            appendLine()
            appendLine("## 优先级建议")
            appendLine()
            report.recommendations.forEach { rec ->
                appendLine("### ${rec.priority.emoji} ${rec.title} (${rec.priority.label})")
                appendLine("- 描述: ${rec.description}")
                appendLine("- 涉及组件: ${rec.targetComponents.joinToString(", ")}")
                appendLine("- 预估工时: ${rec.estimatedHours}h")
                appendLine()
            }
            appendLine("## 迁移步骤清单")
            appendLine()
            state.migrationState.migrationSteps.forEach { step ->
                val checkbox = if (step.status == MigrationStepStatus.COMPLETED) "[x]" else "[ ]"
                appendLine("$checkbox ${step.priority.emoji} ${step.title} (${step.componentType})")
            }
        }
    }

    /**
     * Generate JSON-formatted migration report.
     */
    private fun generateJsonReport(report: MigrationReport): String {
        return org.json.JSONObject().apply {
            put("generatedAt", report.generatedAt)
            put("totalSteps", report.totalSteps)
            put("completedSteps", report.completedSteps)
            put("totalComponents", report.totalComponents)
            put("migratedComponents", report.migratedComponents)
            put("skippedSteps", report.skippedSteps)
            put("completionPercentage", report.completedSteps * 100 / report.totalSteps)
            put("recommendations", org.json.JSONArray().apply {
                report.recommendations.forEach { rec ->
                    put(org.json.JSONObject().apply {
                        put("priority", rec.priority.label)
                        put("title", rec.title)
                        put("description", rec.description)
                        put("targetComponents", org.json.JSONArray(rec.targetComponents))
                        put("estimatedHours", rec.estimatedHours)
                    })
                }
            })
        }.toString(2)
    }

    private fun handleDismissError() {
        // Currently no persistent error state, but kept for future use
    }
}
