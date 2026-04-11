package com.mvi.kenny.feature.agp9migration

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*

/**
 * ViewModel for AGP 9.0 KMP NDK/C++ Migration Toolkit
 * AGP 9.0 KMP NDK/C++ 迁移检测与自动化修复工具包的 ViewModel
 *
 * Design: Implements MVI pattern — State is the single source of truth,
 * Intent represents user actions, Effect represents one-time side effects.
 * 设计: MVI 架构 — State 是唯一数据源, Intent 表示用户动作, Effect 表示一次性副作用
 */
class Agp9MigrationViewModel : ViewModel() {

    private val _state = MutableStateFlow(Agp9MigrationState())
    val state: StateFlow<Agp9MigrationState> = _state.asStateFlow()

    private val _effect = Channel<Agp9MigrationEffect>(Channel.BUFFERED)
    val effect: Flow<Agp9MigrationEffect> = _effect.receiveAsFlow()

    // Simulated project scan data — In production, this would parse Gradle files
    // 模拟的项目扫描数据 — 实际使用时需要解析 Gradle 配置文件
    private val simulatedModules = listOf(
        ModuleItem("composeApp", ":composeApp", hasNDK = true, hasAndroidBlockIssue = true, hasKSPIssue = false, severity = Severity.P0, status = MigrationStatus.NOT_STARTED),
        ModuleItem("shared", ":shared", hasNDK = true, hasAndroidBlockIssue = false, hasKSPIssue = false, severity = Severity.P0, status = MigrationStatus.NOT_STARTED),
        ModuleItem("androidApp", ":androidApp", hasNDK = false, hasAndroidBlockIssue = false, hasKSPIssue = false, severity = Severity.NONE, status = MigrationStatus.NOT_STARTED),
        ModuleItem("iosApp", ":iosApp", hasNDK = false, hasAndroidBlockIssue = false, hasKSPIssue = false, severity = Severity.NONE, status = MigrationStatus.NOT_STARTED)
    )

    private val simulatedNDKItems = listOf(
        NDKIsolationItem(
            moduleName = "composeApp",
            modulePath = ":composeApp",
            nativeFiles = listOf(
                NativeFileInfo("native-lib.cpp", "src/iosMain/cpp/native-lib.cpp", emptyList()),
                NativeFileInfo("image-processor.cpp", "src/androidMain/cpp/image-processor.cpp", listOf("ImageProcessor.kt"))
            ),
            suggestedTarget = "androidApp",
            hasApplicationModule = true,
            generatedInterface = null
        ),
        NDKIsolationItem(
            moduleName = "shared",
            modulePath = ":shared",
            nativeFiles = listOf(
                NativeFileInfo("crypto.cpp", "src/commonNative/crypto.cpp", listOf("Crypto.kt", "KeyManager.kt"))
            ),
            suggestedTarget = "New module: native-core",
            hasApplicationModule = false,
            generatedInterface = null
        )
    )

    private val simulatedAndroidBlockItems = listOf(
        AndroidBlockItem(
            sourceModule = "composeApp",
            sourceModulePath = ":composeApp",
            currentAndroidBlock = """
                android {
                    namespace = "com.example.app"
                    compileSdk = 35
                    ndk { abiFilters += listOf("armeabi-v7a", "arm64-v8a", "x86", "x86_64") }
                    packagingOptions { jniLibs.useLegacyPackaging = true }
                    defaultConfig { minSdk = 24 }
                }
            """.trimIndent(),
            suggestedAndroidBlock = """
                android {
                    namespace = "com.example.app"
                    compileSdk = 35
                    defaultConfig { minSdk = 24 }
                }
            """.trimIndent(),
            removedProperties = listOf("ndk {}", "packagingOptions {}", "sourceSets {}"),
            targetModuleName = "androidApp"
        )
    )

    private val simulatedKSPCheckItems = listOf(
        KSPCheckItem(":shared", ":shared", "Room 2.6 KSP version may have compatibility issue with AGP 9.0", "Upgrade Room to 3.0+ or use KSP 2.0", Severity.P1)
    )

    /**
     * Process user intents and update state accordingly
     * 处理用户意图并相应更新状态
     */
    fun processIntent(intent: Agp9MigrationIntent) {
        when (intent) {
            is Agp9MigrationIntent.RunFullScan -> runFullScan()
            is Agp9MigrationIntent.CancelScan -> cancelScan()
            is Agp9MigrationIntent.SelectModule -> selectModule(intent.module)
            is Agp9MigrationIntent.ClearModuleSelection -> clearModuleSelection()
            is Agp9MigrationIntent.SelectNDKItem -> selectNDKItem(intent.item)
            is Agp9MigrationIntent.DismissNDKDialog -> dismissNDKDialog()
            is Agp9MigrationIntent.StartNDKIsolation -> startNDKIsolation(intent.item, intent.targetModule)
            is Agp9MigrationIntent.PreviewAndroidBlockMigration -> previewAndroidBlockMigration(intent.item)
            is Agp9MigrationIntent.DismissAndroidBlockDialog -> dismissAndroidBlockDialog()
            is Agp9MigrationIntent.ApplyAndroidBlockMigration -> applyAndroidBlockMigration(intent.item)
            is Agp9MigrationIntent.UpdateSettings -> updateSettings(intent.settings)
            is Agp9MigrationIntent.ExportReport -> exportReport(intent.format)
            is Agp9MigrationIntent.DismissError -> dismissError()
            is Agp9MigrationIntent.DismissConfirmDialog -> dismissConfirmDialog()
        }
    }

    // ===== Scan Logic / 扫描逻辑 =====

    private fun runFullScan() {
        if (_state.value.scanStatus == ScanStatus.SCANNING) return

        viewModelScope.launch {
            _state.update { it.copy(scanStatus = ScanStatus.SCANNING, scanProgress = 0f, errorMessage = null) }

            val phases = ScanPhase.entries
            for ((index, phase) in phases.withIndex()) {
                _state.update { it.copy(currentPhase = phase, scanPhases = phases.take(index + 1)) }

                // Simulate scanning work / 模拟扫描工作
                delay(400L)

                val progress = ((index + 1) * 100f) / phases.size
                _state.update { it.copy(scanProgress = progress) }
            }

            // Calculate health score based on severity / 根据严重级别计算健康分数
            val p0Count = simulatedModules.count { it.severity == Severity.P0 }
            val p1Count = simulatedModules.count { it.severity == Severity.P1 }
            val p2Count = simulatedModules.count { it.severity == Severity.P2 }
            val healthScore = maxOf(0, 100 - (p0Count * 30) - (p1Count * 15) - (p2Count * 5))

            val progress = MigrationProgress(
                totalModules = simulatedModules.size,
                completedModules = 0,
                inProgressModules = 0,
                blockedModules = p0Count
            )

            _state.update {
                it.copy(
                    scanStatus = ScanStatus.COMPLETED,
                    currentPhase = null,
                    scanProgress = 1f,
                    overallHealthScore = healthScore,
                    moduleItems = simulatedModules,
                    ndkIsolationItems = simulatedNDKItems,
                    androidBlockItems = simulatedAndroidBlockItems,
                    kspCheckItems = simulatedKSPCheckItems,
                    migrationProgress = progress
                )
            }

            _effect.send(Agp9MigrationEffect.ShowSnackbar("Scan completed — Health Score: $healthScore%"))
        }
    }

    private fun cancelScan() {
        viewModelScope.coroutineContext.cancelChildren()
        _state.update {
            it.copy(
                scanStatus = ScanStatus.IDLE,
                currentPhase = null,
                scanProgress = 0f,
                scanPhases = emptyList()
            )
        }
    }

    // ===== Module Selection / 模块选择 =====

    private fun selectModule(module: ModuleItem) {
        _state.update { it.copy(selectedModule = module) }
    }

    private fun clearModuleSelection() {
        _state.update { it.copy(selectedModule = null) }
    }

    // ===== NDK Isolation / NDK 隔离 =====

    private fun selectNDKItem(item: NDKIsolationItem) {
        _state.update { it.copy(selectedNDKItem = item, isNDKDialogOpen = true) }
    }

    private fun dismissNDKDialog() {
        _state.update { it.copy(isNDKDialogOpen = false, selectedNDKItem = null) }
    }

    private fun startNDKIsolation(item: NDKIsolationItem, targetModule: String) {
        dismissNDKDialog()
        viewModelScope.launch {
            // Update module status / 更新模块状态
            val updatedModules = _state.value.moduleItems.map {
                if (it.name == item.moduleName) it.copy(status = MigrationStatus.IN_PROGRESS)
                else it
            }
            val updatedProgress = _state.value.migrationProgress.copy(
                inProgressModules = updatedModules.count { it.status == MigrationStatus.IN_PROGRESS }
            )
            _state.update {
                it.copy(
                    moduleItems = updatedModules,
                    migrationProgress = updatedProgress
                )
            }
            _effect.send(Agp9MigrationEffect.ShowSnackbar("NDK isolation started for ${item.moduleName} → $targetModule"))
        }
    }

    // ===== Android Block Migration / android{} 迁移 =====

    private fun previewAndroidBlockMigration(item: AndroidBlockItem) {
        _state.update { it.copy(selectedAndroidBlockItem = item, isAndroidBlockDialogOpen = true) }
    }

    private fun dismissAndroidBlockDialog() {
        _state.update { it.copy(isAndroidBlockDialogOpen = false, selectedAndroidBlockItem = null) }
    }

    private fun applyAndroidBlockMigration(item: AndroidBlockItem) {
        dismissAndroidBlockDialog()
        viewModelScope.launch {
            _effect.send(
                Agp9MigrationEffect.ShowConfirmDialog(
                    title = "Confirm Migration",
                    message = "This will modify build.gradle.kts in ${item.sourceModule}. Backup is recommended.",
                    onConfirm = {
                        viewModelScope.launch {
                            val updatedModules = _state.value.moduleItems.map {
                                if (it.name == item.sourceModule) it.copy(
                                    hasAndroidBlockIssue = false,
                                    severity = Severity.NONE
                                )
                                else it
                            }
                            _state.update {
                                it.copy(
                                    moduleItems = updatedModules,
                                    androidBlockItems = it.androidBlockItems.filter { b -> b.sourceModule != item.sourceModule }
                                )
                            }
                            _effect.send(Agp9MigrationEffect.ShowSnackbar("android{} migration applied for ${item.sourceModule}"))
                            _effect.send(Agp9MigrationEffect.MigrationCompleted)
                        }
                    }
                )
            )
        }
    }

    // ===== Settings / 设置 =====

    private fun updateSettings(settings: AgpSettings) {
        _state.update { it.copy(settings = settings) }
    }

    // ===== Report / 报告 =====

    private fun exportReport(format: ReportFormat) {
        viewModelScope.launch {
            val fileName = "agp9-migration-report-${System.currentTimeMillis()}.${format.name.lowercase()}"
            val filePath = "/reports/$fileName"
            _effect.send(Agp9MigrationEffect.ReportGenerated(filePath, format))
            _effect.send(Agp9MigrationEffect.ShowSnackbar("Report exported: $fileName"))
        }
    }

    // ===== Error Handling / 错误处理 =====

    private fun dismissError() {
        _state.update { it.copy(errorMessage = null) }
    }

    private fun dismissConfirmDialog() {
        _state.update { it.copy(isConfirmDialogOpen = false, confirmDialogOnConfirm = null) }
    }

    override fun onCleared() {
        super.onCleared()
        viewModelScope.coroutineContext.cancelChildren()
    }
}
