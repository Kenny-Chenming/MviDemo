package com.mvi.kenny.feature.swiftpmmigration

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mvi.kenny.feature.swiftpmmigration.ConversionWizardIntent
import com.mvi.kenny.feature.swiftpmmigration.ConversionWizardState
import com.mvi.kenny.feature.swiftpmmigration.FilterMode
import com.mvi.kenny.feature.swiftpmmigration.MigrationPath
import com.mvi.kenny.feature.swiftpmmigration.ProgressTrackerIntent
import com.mvi.kenny.feature.swiftpmmigration.ProgressTrackerState
import com.mvi.kenny.feature.swiftpmmigration.ScanStatus
import com.mvi.kenny.feature.swiftpmmigration.ScannerIntent
import com.mvi.kenny.feature.swiftpmmigration.ScannerState
import com.mvi.kenny.feature.swiftpmmigration.SortMode
import com.mvi.kenny.feature.swiftpmmigration.SwiftPMMigrationEffect
import com.mvi.kenny.feature.swiftpmmigration.SwiftPMMigrationIntent
import com.mvi.kenny.feature.swiftpmmigration.SwiftPMMigrationState
import com.mvi.kenny.feature.swiftpmmigration.WizardStep
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*

/**
 * ============================================================
 * SwiftPMMigrationViewModel — KMP SwiftPM 迁移助手 ViewModel
 * ============================================================
 * PRD-025 | KMP SwiftPM 迁移助手
 *
 * 职责：
 * - 管理 SwiftPMMigrationState（UI 状态唯一真相来源）
 * - 接收 Intent（用户意图），执行业务逻辑，输出新状态
 * - 通过 Effect Channel 发送一次性副作用
 *
 * MVI 数据流：
 * Intent → ViewModel → State → Screen recomposition
 *              ↓
 *           Effect（Channel）
 *
 * 内部子状态管理：
 * - scannerState：ScannerFeature 独立状态
 * - wizardState：ConversionWizardFeature 独立状态
 * - progressState：ProgressTrackerFeature 独立状态
 *
 * @see SwiftPMMigrationContract MVI 契约定义
 * @see SwiftPMMigrationScreen UI 渲染层
 */
@OptIn(ExperimentalStdlibApi::class)
class SwiftPMMigrationViewModel : ViewModel() {

    // ============================================================
    // State Management / 状态管理
    // ============================================================

    /** 主状态流，UI 层 collect */
    private val _state = MutableStateFlow(SwiftPMMigrationState())
    val state: StateFlow<SwiftPMMigrationState> = _state.asStateFlow()

    // ============================================================
    // Effect Channels / 副作用通道
    // ============================================================

    /** 主副作用通道 */
    private val _effect = Channel<SwiftPMMigrationEffect>(Channel.BUFFERED)
    val effect: Flow<SwiftPMMigrationEffect> = _effect.receiveAsFlow()

    /** ScannerFeature 副作用通道 */
    private val _scannerEffect = Channel<ScannerEffect>(Channel.BUFFERED)
    val scannerEffect: Flow<ScannerEffect> = _scannerEffect.receiveAsFlow()

    /** ConversionWizardFeature 副作用通道 */
    private val _wizardEffect = Channel<ConversionWizardEffect>(Channel.BUFFERED)
    val wizardEffect: Flow<ConversionWizardEffect> = _wizardEffect.receiveAsFlow()

    /** ProgressTrackerFeature 副作用通道 */
    private val _progressEffect = Channel<ProgressTrackerEffect>(Channel.BUFFERED)
    val progressEffect: Flow<ProgressTrackerEffect> = _progressEffect.receiveAsFlow()

    // ============================================================
    // Intent Processing / 意图处理
    // ============================================================

    /**
     * 处理主 Intent
     * 入口方法，由 Screen 层调用
     */
    fun processIntent(intent: SwiftPMMigrationIntent) {
        when (intent) {
            is SwiftPMMigrationIntent.SwitchTab -> handleSwitchTab(intent.tab)
            is SwiftPMMigrationIntent.SelectDependencyForDetail -> handleSelectDependency(intent.dependencyId)
            is SwiftPMMigrationIntent.ToggleDetailPanel -> handleToggleDetailPanel()
        }
    }

    /**
     * 处理 ScannerFeature Intent
     */
    fun processScannerIntent(intent: ScannerIntent) {
        when (intent) {
            is ScannerIntent.StartScan -> handleStartScan()
            is ScannerIntent.CancelScan -> handleCancelScan()
            is ScannerIntent.SetFilter -> handleSetFilter(intent.mode)
            is ScannerIntent.SetSort -> handleSetSort(intent.mode)
            is ScannerIntent.SelectDependency -> handleSelectDependencyToggle(intent.id, intent.selected)
            is ScannerIntent.SelectAllMigratable -> handleSelectAllMigratable()
            is ScannerIntent.DeselectAll -> handleDeselectAll()
        }
    }

    /**
     * 处理 ConversionWizardFeature Intent
     */
    fun processWizardIntent(intent: ConversionWizardIntent) {
        when (intent) {
            is ConversionWizardIntent.SelectDependencies -> handleWizardSelectDependencies(intent.ids)
            is ConversionWizardIntent.NextStep -> handleWizardNextStep()
            is ConversionWizardIntent.PreviousStep -> handleWizardPreviousStep()
            is ConversionWizardIntent.ExecuteConversion -> handleExecuteConversion()
            is ConversionWizardIntent.RollbackLast -> handleRollback()
            is ConversionWizardIntent.ConfirmAndClose -> handleConfirmAndClose()
        }
    }

    /**
     * 处理 ProgressTrackerFeature Intent
     */
    fun processProgressIntent(intent: ProgressTrackerIntent) {
        when (intent) {
            is ProgressTrackerIntent.RefreshProgress -> handleRefreshProgress()
            is ProgressTrackerIntent.ExportReport -> handleExportReport(intent.format)
            is ProgressTrackerIntent.SelectModule -> handleSelectModule(intent.moduleName)
        }
    }

    // ============================================================
    // Tab Navigation / 标签页切换
    // ============================================================

    private fun handleSwitchTab(tab: MigrationTab) {
        _state.update { it.copy(activeTab = tab) }
    }

    private fun handleSelectDependency(dependencyId: String?) {
        _state.update { it.copy(selectedDependencyId = dependencyId) }
    }

    private fun handleToggleDetailPanel() {
        _state.update { it.copy(isDetailPanelExpanded = !it.isDetailPanelExpanded) }
    }

    // ============================================================
    // ScannerFeature Handlers / 扫描器业务逻辑
    // ============================================================

    /**
     * 启动依赖扫描
     * 模拟：生成示例 CocoaPods 依赖数据
     * 实际：调用 Gradle 插件扫描 Podfile.lock
     */
    private fun handleStartScan() {
        viewModelScope.launch {
            val scannerState = _state.value.scannerState

            // 更新状态为扫描中
            _state.update {
                it.copy(
                    scannerState = scannerState.copy(
                        scanStatus = ScanStatus.SCANNING,
                        scanProgress = 0,
                        scannedCount = 0,
                        totalCount = 0,
                        dependencies = emptyList(),
                        errorMessage = null,
                    )
                )
            }

            // 模拟扫描过程（实际项目中替换为真实的 Podfile 解析逻辑）
            val mockDependencies = generateMockDependencies()
            val totalCount = mockDependencies.size

            for (i in mockDependencies.indices) {
                delay(80) // 模拟 I/O 延迟
                val current = i + 1
                _state.update {
                    it.copy(
                        scannerState = it.scannerState.copy(
                            scanProgress = (current * 100) / totalCount,
                            scannedCount = current,
                            totalCount = totalCount,
                            dependencies = mockDependencies.take(current),
                        )
                    )
                }
            }

            // 扫描完成
            _state.update {
                it.copy(
                    scannerState = it.scannerState.copy(
                        scanStatus = ScanStatus.COMPLETED,
                        scanProgress = 100,
                        daysUntilSunset = 75, // 模拟：距 Deadline 75 天
                    )
                )
            }
            _scannerEffect.send(ScannerEffect.ScanCompleted)
        }
    }

    /**
     * 取消扫描
     */
    private fun handleCancelScan() {
        viewModelScope.launch {
            _state.update {
                it.copy(
                    scannerState = it.scannerState.copy(
                        scanStatus = ScanStatus.IDLE,
                        scanProgress = 0,
                        scannedCount = 0,
                    )
                )
            }
            _scannerEffect.send(ScannerEffect.ShowToast("扫描已取消"))
        }
    }

    private fun handleSetFilter(mode: FilterMode) {
        _state.update {
            it.copy(scannerState = it.scannerState.copy(filterMode = mode))
        }
    }

    private fun handleSetSort(mode: SortMode) {
        _state.update {
            it.copy(scannerState = it.scannerState.copy(sortMode = mode))
        }
    }

    private fun handleSelectDependencyToggle(id: String, selected: Boolean) {
        _state.update {
            val deps = it.scannerState.dependencies.map { dep ->
                if (dep.id == id) dep.copy(isSelected = selected) else dep
            }
            it.copy(scannerState = it.scannerState.copy(dependencies = deps))
        }
    }

    private fun handleSelectAllMigratable() {
        _state.update {
            val deps = it.scannerState.dependencies.map { dep ->
                if (dep.migrationPath != MigrationPath.UNSUPPORTED) dep.copy(isSelected = true) else dep
            }
            it.copy(scannerState = it.scannerState.copy(dependencies = deps))
        }
    }

    private fun handleDeselectAll() {
        _state.update {
            val deps = it.scannerState.dependencies.map { it.copy(isSelected = false) }
            it.copy(scannerState = it.scannerState.copy(dependencies = deps))
        }
    }

    // ============================================================
    // ConversionWizardFeature Handlers / 转换向导业务逻辑
    // ============================================================

    private fun handleWizardSelectDependencies(ids: Set<String>) {
        _state.update {
            it.copy(wizardState = it.wizardState.copy(selectedDependencies = ids))
        }

        // 生成转换方案（模拟）
        viewModelScope.launch {
            val plans = ids.associateWith { id ->
                generateMockConversionPlan(id)
            }
            _state.update {
                it.copy(wizardState = it.wizardState.copy(conversionPlans = plans))
            }
        }
    }

    private fun handleWizardNextStep() {
        val current = _state.value.wizardState.currentStep
        val next = when (current) {
            WizardStep.SELECT -> WizardStep.CONFIRM
            WizardStep.CONFIRM -> WizardStep.DIFF
            WizardStep.DIFF -> WizardStep.EXECUTE
            WizardStep.EXECUTE -> WizardStep.EXECUTE
        }
        if (next != current) {
            _state.update {
                it.copy(wizardState = it.wizardState.copy(currentStep = next))
            }

            // 在 DIFF 步骤生成 diff 结果
            if (next == WizardStep.DIFF) {
                viewModelScope.launch {
                    val diffs = _state.value.wizardState.selectedDependencies.associateWith { id ->
                        generateMockDiffResult(id)
                    }
                    _state.update {
                        it.copy(wizardState = it.wizardState.copy(diffResults = diffs))
                    }
                }
            }
        }
    }

    private fun handleWizardPreviousStep() {
        val current = _state.value.wizardState.currentStep
        val prev = when (current) {
            WizardStep.SELECT -> WizardStep.SELECT
            WizardStep.CONFIRM -> WizardStep.SELECT
            WizardStep.DIFF -> WizardStep.CONFIRM
            WizardStep.EXECUTE -> WizardStep.DIFF
        }
        if (prev != current) {
            _state.update {
                it.copy(wizardState = it.wizardState.copy(currentStep = prev))
            }
        }
    }

    /**
     * 执行转换
     * 模拟：逐步执行转换，显示进度
     */
    private fun handleExecuteConversion() {
        viewModelScope.launch {
            _state.update {
                it.copy(
                    wizardState = it.wizardState.copy(
                        isExecuting = true,
                        executionTotal = it.wizardState.selectedDependencies.size,
                        executionProgress = 0,
                        rollbackAvailable = false,
                    )
                )
            }

            val total = _state.value.wizardState.selectedDependencies.size
            for (i in 0 until total) {
                delay(300) // 模拟每个依赖的转换时间
                _state.update {
                    it.copy(
                        wizardState = it.wizardState.copy(
                            executionProgress = i + 1,
                            executedCount = i + 1,
                        )
                    )
                }
            }

            _state.update {
                it.copy(
                    wizardState = it.wizardState.copy(
                        isExecuting = false,
                        rollbackAvailable = true,
                        rollbackSnapshotId = "snapshot-${System.currentTimeMillis()}",
                    )
                )
            }
            _wizardEffect.send(ConversionWizardEffect.ConversionCompleted)
        }
    }

    private fun handleRollback() {
        viewModelScope.launch {
            delay(500) // 模拟回滚操作
            _state.update {
                it.copy(
                    wizardState = it.wizardState.copy(
                        isExecuting = false,
                        executionProgress = 0,
                        executedCount = 0,
                        rollbackAvailable = false,
                        rollbackSnapshotId = null,
                    )
                )
            }
            _wizardEffect.send(ConversionWizardEffect.RollbackCompleted)
        }
    }

    private fun handleConfirmAndClose() {
        viewModelScope.launch {
            _wizardEffect.send(ConversionWizardEffect.ConversionCompleted)
            _state.update {
                it.copy(
                    wizardState = ConversionWizardState(),
                    activeTab = MigrationTab.PROGRESS,
                )
            }
        }
    }

    // ============================================================
    // ProgressTrackerFeature Handlers / 进度追踪业务逻辑
    // ============================================================

    private fun handleRefreshProgress() {
        viewModelScope.launch {
            // 模拟从持久化状态加载进度
            val modules = listOf(
                ModuleProgress("shared", 24, 18, 2),
                ModuleProgress("ios", 16, 12, 1),
                ModuleProgress("android", 20, 20, 0),
                ModuleProgress("desktop", 8, 3, 3),
            )
            val totalMigrated = modules.sumOf { it.migratedCount }
            val total = modules.sumOf { it.totalDependencies }
            val overallProgress = if (total > 0) totalMigrated.toFloat() / total else 0f

            _state.update {
                it.copy(
                    progressState = it.progressState.copy(
                        overallProgress = overallProgress,
                        modules = modules,
                        daysUntilSunset = it.scannerState.daysUntilSunset,
                    )
                )
            }
        }
    }

    private fun handleExportReport(format: ReportFormat) {
        viewModelScope.launch {
            delay(200) // 模拟文件写入
            val path = "/tmp/swiftpm-migration-report.${format.extension}"
            _state.update {
                it.copy(progressState = it.progressState.copy(exportedReportPath = path))
            }
            _progressEffect.send(ProgressTrackerEffect.ReportExported(path))
        }
    }

    private fun handleSelectModule(moduleName: String) {
        _state.update {
            it.copy(progressState = it.progressState.copy(selectedModuleName = moduleName))
        }
    }

    // ============================================================
    // Mock Data Generators / 模拟数据生成器
    // ============================================================

    /**
     * 生成模拟依赖列表（实际项目中替换为 Podfile 解析结果）
     */
    private fun generateMockDependencies(): List<DependencyInfo> {
        return listOf(
            DependencyInfo("pod_alamofire", "Alamofire", "5.8.0", MigrationPath.DIRECT, false, 75, "Alamofire", null, 1, "", 5),
            DependencyInfo("pod_kingfisher", "Kingfisher", "7.10.0", MigrationPath.DIRECT, false, 75, "Kingfisher", null, 1, "", 5),
            DependencyInfo("pod_snapkit", "SnapKit", "5.6.0", MigrationPath.DIRECT, false, 75, null, null, 1, "", 2),
            DependencyInfo("pod_moya", "Moya", "15.0.0", MigrationPath.NEEDS_ADJUSTMENT, false, 60, "Moya", null, 3, "", 10),
            DependencyInfo("pod_rxswift", "RxSwift", "6.6.0", MigrationPath.NEEDS_ADJUSTMENT, false, 60, null, "Combine/async-await", 4, "", 15),
            DependencyInfo("pod_socketio", "Socket.IO-Client-Swift", "16.0.0", MigrationPath.UNSUPPORTED, false, 25, null, "Starscream", 5, "", 20),
            DependencyInfo("pod_firebase", "Firebase/iOS", "10.18.0", MigrationPath.NEEDS_ADJUSTMENT, false, 75, "FirebaseFirestore", null, 3, "", 12),
            DependencyInfo("pod_lottie", "lottie-ios", "4.3.0", MigrationPath.DIRECT, false, 75, "lottie-ios", null, 1, "", 3),
            DependencyInfo("pod_graphql", "Apollo", "1.7.0", MigrationPath.UNSUPPORTED, false, 20, null, "Apollo iOS", 5, "", 20),
            DependencyInfo("pod_charts", "DGCharts", "5.0.0", MigrationPath.NEEDS_ADJUSTMENT, false, 45, "DGCharts", null, 2, "", 8),
            DependencyInfo("pod_keychain", "KeychainAccess", "4.2.2", MigrationPath.DIRECT, false, 75, "KeychainAccess", null, 1, "", 2),
            DependencyInfo("pod_logger", "CocoaLumberjack", "3.8.0", MigrationPath.UNSUPPORTED, false, 15, null, "OSLog", 5, "", 20),
        )
    }

    private fun generateMockConversionPlan(dependencyId: String): ConversionPlan {
        val dep = generateMockDependencies().find { it.id == dependencyId }
        return ConversionPlan(
            dependencyId = dependencyId,
            recommendedAction = when (dep?.migrationPath) {
                MigrationPath.DIRECT -> "直接替换为 SwiftPM Product，可自动完成迁移"
                MigrationPath.NEEDS_ADJUSTMENT -> "需要调整代码以适配 SwiftPM API 变更"
                else -> "无直接 SwiftPM 替代，建议手动处理或寻找替代库"
            },
            newPodspecContent = """
                // Package.swift 添加：
                .package(url: "https://github.com/example/${dep?.name?.lowercase()}.git", from: "${dep?.version}"),
            """.trimIndent(),
            warnings = listOf("部分 API 在 SwiftPM 版本中存在差异", "建议在迁移后运行测试套件"),
            breakingChanges = if (dep?.migrationPath == MigrationPath.UNSUPPORTED) listOf("无 SwiftPM 直接替代") else emptyList(),
        )
    }

    private fun generateMockDiffResult(dependencyId: String): DiffResult {
        val dep = generateMockDependencies().find { it.id == dependencyId }
        return DiffResult(
            dependencyId = dependencyId,
            originalContent = """
                # Uncomment the next line to define a global platform for your project
                # platform :ios, '9.0'

                target 'MyApp' do
                  use_frameworks!

                  pod '${dep?.name}', '~> ${dep?.version}'
                end
            """.trimIndent(),
            convertedContent = """
                // swift-tools-version:5.9
                import PackageDescription

                let package = Package(
                    name: "MyApp",
                    platforms: [.iOS(.v15)],
                    products: [],
                    dependencies: [
                        .package(url: "https://github.com/example/${dep?.name?.lowercase()}.git", from: "${dep?.version}"),
                    ],
                    targets: []
                )
            """.trimIndent(),
            diffLines = listOf(
                DiffLine(DiffLineType.CONTEXT, " # platform :ios, '9.0'"),
                DiffLine(DiffLineType.REMOVED, "- pod '${dep?.name}', '~> ${dep?.version}'"),
                DiffLine(DiffLineType.ADDED, "+ .package(url: \"https://github.com/example/${dep?.name?.lowercase()}.git\", from: \"${dep?.version}\")"),
            ),
            hasConflicts = dep?.migrationPath == MigrationPath.UNSUPPORTED,
        )
    }

    // ============================================================
    // Utility / 工具方法
    // ============================================================

    /** 获取指定依赖的详细信息 */
    fun getDependencyById(id: String): DependencyInfo? {
        return _state.value.scannerState.dependencies.find { it.id == id }
    }

    /** 获取指定依赖的 Diff 结果 */
    fun getDiffResultById(id: String): DiffResult? {
        return _state.value.wizardState.diffResults[id]
    }

    /** 获取指定依赖的转换方案 */
    fun getConversionPlanById(id: String): ConversionPlan? {
        return _state.value.wizardState.conversionPlans[id]
    }
}
