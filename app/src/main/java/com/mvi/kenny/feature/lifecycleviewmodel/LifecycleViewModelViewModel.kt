package com.mvi.kenny.feature.lifecycleviewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlin.random.Random

/**
 * Lifecycle ViewModel DSL MVI ViewModel
 */
class LifecycleViewModelViewModel : ViewModel() {

    private val _state = MutableStateFlow(LifecycleDslState.Initial)
    val state: StateFlow<LifecycleDslState> = _state.asStateFlow()
    val currentState: LifecycleDslState get() = _state.value
    private val _effect = Channel<LifecycleDslEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    init {
        _state.value = _state.value.copy(
            currentDecisionNode = buildDecisionTree(),
            apiCompareItems = buildApiCompareItems(),
            sceneRecommendItems = buildSceneRecommendItems(),
            compatibilitySDKItems = buildCompatibilitySDKItems(),
            performanceData = buildPerformanceData(),
            viewModelScopeChange = VIEW_MODEL_SCOPE_CHANGE_TEXT,
            recentScans = buildRecentScans()
        )
    }

    fun sendIntent(intent: LifecycleDslIntent) {
        when (intent) {
            is LifecycleDslIntent.StartScan -> startScan()
            is LifecycleDslIntent.StopScan -> stopScan()
            is LifecycleDslIntent.SetScanScope -> setScanScope(intent.scope)
            is LifecycleDslIntent.SetIncludeTestDir -> setIncludeTestDir(intent.include)
            is LifecycleDslIntent.FilterBySeverity -> filterBySeverity(intent.severity)
            is LifecycleDslIntent.SelectResult -> selectResult(intent.item)
            is LifecycleDslIntent.ToggleResultSelection -> toggleResultSelection(intent.id)
            is LifecycleDslIntent.GeneratePatch -> generatePatch(intent.items)
            is LifecycleDslIntent.ConfirmPatch -> confirmPatch()
            is LifecycleDslIntent.CancelPatch -> cancelPatch()
            is LifecycleDslIntent.SwitchTab -> switchTab(intent.tab)
            is LifecycleDslIntent.NavigateDecision -> navigateDecision(intent.nextNodeId)
            is LifecycleDslIntent.GenerateTests -> generateTests(intent.viewModelClass)
            is LifecycleDslIntent.RefreshDashboard -> refreshDashboard()
            is LifecycleDslIntent.ClearError -> clearError()
        }
    }

    private fun startScan() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isScanning = true, scanProgress = 0f)
            try {
                for (i in 1..10) {
                    delay(200)
                    _state.value = _state.value.copy(scanProgress = i / 10f)
                }
                val results = generateMockScanResults()
                _state.value = _state.value.copy(isScanning = false, scanResults = results, scanProgress = 1f)
                _effect.send(LifecycleDslEffect.ShowScanComplete(results.size))
            } catch (e: Exception) {
                _state.value = _state.value.copy(isScanning = false)
                _effect.send(LifecycleDslEffect.ShowError("扫描失败: " + e.message))
            }
        }
    }

    private fun stopScan() { _state.value = _state.value.copy(isScanning = false) }
    private fun setScanScope(scope: String) { _state.value = _state.value.copy(scanScope = scope) }
    private fun setIncludeTestDir(include: Boolean) { _state.value = _state.value.copy(includeTestDir = include) }
    private fun filterBySeverity(severity: Severity?) { _state.value = _state.value.copy(severityFilter = severity) }
    private fun selectResult(item: ScanResultItem?) { _state.value = _state.value.copy(selectedResult = item) }

    private fun toggleResultSelection(id: String) {
        _state.value = _state.value.copy(
            scanResults = _state.value.scanResults.map {
                if (it.id == id) it.copy(isSelected = !it.isSelected) else it
            }
        )
    }

    private fun generatePatch(items: List<ScanResultItem>) { _state.value = _state.value.copy(showDiffPreview = true) }

    private fun confirmPatch() {
        viewModelScope.launch {
            val selectedCount = _state.value.scanResults.count { it.isSelected }
            _state.value = _state.value.copy(showDiffPreview = false)
            _effect.send(LifecycleDslEffect.PatchGenerated("/path/to/patch.kt"))
            _effect.send(LifecycleDslEffect.ShowToast("已生成 " + selectedCount + " 个文件的迁移补丁"))
        }
    }

    private fun cancelPatch() { _state.value = _state.value.copy(showDiffPreview = false) }
    private fun switchTab(tab: LifecycleDslTab) { _state.value = _state.value.copy(activeTab = tab) }

    private fun navigateDecision(nextNodeId: String) {
        val nextNode = findNodeById(nextNodeId, _state.value.currentDecisionNode)
        if (nextNode != null) {
            _state.value = _state.value.copy(currentDecisionNode = nextNode, decisionPath = _state.value.decisionPath + nextNode)
        }
    }

    private fun generateTests(viewModelClass: String) {
        viewModelScope.launch {
            val templates = listOf(
                TestTemplate(
                    name = viewModelClass + "CreationTest",
                    testFramework = "JUnit5",
                    content = "@Test\nfun create" + viewModelClass + "() { /* TODO */ }"
                ),
                TestTemplate(
                    name = viewModelClass + "StateTest",
                    testFramework = "JUnit5",
                    content = "@Test\nfun verify" + viewModelClass + "State() { /* TODO */ }"
                )
            )
            _state.value = _state.value.copy(testTemplates = _state.value.testTemplates + templates)
            _effect.send(LifecycleDslEffect.TestsGenerated(templates.size, "app/src/test/java/"))
        }
    }

    private fun refreshDashboard() {
        viewModelScope.launch {
            _state.value = _state.value.copy(
                dashboardHealth = RadarChartData(
                    values = listOf(
                        Random.nextFloat().coerceIn(0f, 1f),
                        Random.nextFloat().coerceIn(0f, 1f),
                        Random.nextFloat().coerceIn(0f, 1f),
                        Random.nextFloat().coerceIn(0f, 1f),
                        Random.nextFloat().coerceIn(0f, 1f),
                        Random.nextFloat().coerceIn(0f, 1f)
                    )
                )
            )
            _effect.send(LifecycleDslEffect.ShowToast("Dashboard 已刷新"))
        }
    }

    private fun clearError() { _state.value = _state.value.copy(errorMessage = null) }

    private fun generateMockScanResults(): List<ScanResultItem> {
        return listOf(
            ScanResultItem(id = "1", filePath = "app/src/main/java/com/mvi/kenny/feature/home/HomeViewModel.kt", lineNumber = 23, severity = Severity.P0, issueType = "工厂类用法", description = "检测到 object : ViewModelProvider.Factory 用法，建议迁移到 viewModel {} lambda DSL", currentCode = "val factory = object : ViewModelProvider.Factory { ... }", suggestedCode = "val viewModel = viewModel<HomeViewModel> { viewModelFactory { HomeViewModel(...) } }"),
            ScanResultItem(id = "2", filePath = "app/src/main/java/com/mvi/kenny/feature/list/ListViewModel.kt", lineNumber = 45, severity = Severity.P1, issueType = "CreationExtras 缺失", description = "ViewModel 创建时未传递 CreationExtras，建议在需要 SavedStateHandle 时显式传递", currentCode = "viewModel { ListViewModel() }", suggestedCode = "viewModel(factory = ListViewModelFactory(extras[CreationExtras.SavedStateHandle]))"),
            ScanResultItem(id = "3", filePath = "app/src/main/java/com/mvi/kenny/feature/profile/ProfileViewModel.kt", lineNumber = 67, severity = Severity.P2, issueType = "类型推断", description = "建议显式指定泛型类型以获得更好的类型安全", currentCode = "viewModel { ProfileViewModel(getUserCase) }", suggestedCode = "viewModel<ProfileViewModel> { viewModelFactory { ProfileViewModel(getUserCase) } }"),
            ScanResultItem(id = "4", filePath = "app/src/main/java/com/mvi/kenny/feature/chat/ChatViewModel.kt", lineNumber = 89, severity = Severity.P1, issueType = "Lambda 捕获", description = "Lambda 中捕获了外部作用域的大型对象，建议重构以避免内存泄漏风险", currentCode = "val viewModel = viewModel { ChatViewModel(largeObject) }", suggestedCode = "val viewModel = viewModel<ChatViewModel>(factory = ChatViewModelFactory(largeObject))"),
            ScanResultItem(id = "5", filePath = "app/src/main/java/com/mvi/kenny/feature/login/LoginViewModel.kt", lineNumber = 34, severity = Severity.P0, issueType = "工厂类用法", description = "LoginViewModel 使用工厂类创建，建议迁移到 viewModelFactory {} DSL", currentCode = "ViewModelProvider(this, factory)[LoginViewModel::class.java]", suggestedCode = "viewModel<LoginViewModel> { viewModelFactory { LoginViewModel(authRepository) } }"),
            ScanResultItem(id = "6", filePath = "app/src/main/java/com/mvi/kenny/feature/detail/DetailViewModel.kt", lineNumber = 12, severity = Severity.P2, issueType = "代码规范", description = "建议使用 viewModel {} 替代 viewModelFactory() 以保持一致性", currentCode = "viewModelFactory<DetailViewModel> { DetailViewModel() }", suggestedCode = "viewModel<DetailViewModel> { viewModelFactory { DetailViewModel() } }")
        )
    }

    private fun buildDecisionTree(): DecisionNode {
        return DecisionNode(
            id = "root",
            question = "你的 ViewModel 需要依赖注入吗？",
            options = listOf(
                DecisionOption("是，使用 Hilt/Koin", "need_di"),
                DecisionOption("否，简单 ViewModel", "simple_vm"),
                DecisionOption("需要 SavedStateHandle", "need_savedstate"),
                DecisionOption("复杂协程作用域管理", "complex_scope")
            )
        )
    }

    private fun findNodeById(id: String, root: DecisionNode?): DecisionNode? {
        if (root == null) return null
        if (root.id == id) return root
        root.options.forEach { option ->
            if (option.nextNodeId == id) {
                return when (id) {
                    "need_di" -> DecisionNode(id, "你使用哪个依赖注入框架？", listOf(DecisionOption("Hilt", null), DecisionOption("Koin", null)), "推荐使用 viewModel { viewModelFactory { MyViewModel(hiltViewModel()) } }")
                    "simple_vm" -> DecisionNode(id, "你的 ViewModel 是否有外部依赖？", listOf(DecisionOption("无依赖", null), DecisionOption("有简单依赖", null)), "推荐使用 viewModel { MyViewModel() }")
                    "need_savedstate" -> DecisionNode(id, "SavedStateHandle 传递方式？", listOf(DecisionOption("CreationExtras", null), DecisionOption("构造函数参数", null)), "推荐使用 viewModel(factory = MyFactory(extras[CreationExtras.SavedStateHandle]))")
                    "complex_scope" -> DecisionNode(id, "协程作用域需求？", listOf(DecisionOption("独立作用域", null), DecisionOption("与 lifecycleScope 关联", null)), "推荐使用 viewModel { viewModelScope.launch { ... }; MyViewModel() }")
                    else -> null
                }
            }
        }
        return null
    }

    private fun buildApiCompareItems(): List<ApiCompareItem> {
        return listOf(
            ApiCompareItem(description = "ViewModelProvider.Factory 创建", oldApi = "object : ViewModelProvider.Factory { override fun <T> create(...): T = MyViewModel() as T }", newApi = "viewModelFactory { MyViewModel() }"),
            ApiCompareItem(description = "带依赖注入的 ViewModel", oldApi = "ViewModelProvider(this, factory)[MyViewModel::class.java]", newApi = "viewModel<MyViewModel> { viewModelFactory { MyViewModel(dep1, dep2) } }"),
            ApiCompareItem(description = "SavedStateHandle 传递（旧版）", oldApi = "class MyViewModel(private val handle: SavedStateHandle) : ViewModel()", newApi = "viewModel<MyViewModel>(factory = MyViewModelFactory(extras[CreationExtras.SavedStateHandle]))"),
            ApiCompareItem(description = "Hilt 集成", oldApi = "@HiltViewModel class MyViewModel @Inject constructor(...) { }", newApi = "val vm = hiltViewModel<MyViewModel>()"),
            ApiCompareItem(description = "viewModelScope 协程启动", oldApi = "viewModelScope.launch { /* 协程代码 */ }", newApi = "viewModelScope.launch { /* 新版 viewModelScope 行为一致 */ }")
        )
    }

    private fun buildSceneRecommendItems(): List<SceneRecommendItem> {
        return listOf(
            SceneRecommendItem(scene = "登录态管理", codeExample = "viewModel<LoginViewModel> { viewModelFactory { LoginViewModel(authRepository = get(), savedStateHandle = extras[CreationExtras.SavedStateHandle]) } }", recommendation = "推荐使用 viewModelFactory {} + CreationExtras.SavedStateHandle，登录态持久化更可靠"),
            SceneRecommendItem(scene = "依赖注入场景", codeExample = "val viewModel: HomeViewModel = hiltViewModel()", recommendation = "Hilt 场景下直接使用 hiltViewModel()，无需手动 factory"),
            SceneRecommendItem(scene = "列表 + 详情页", codeExample = "viewModel<ListViewModel>(factory = ListViewModelFactory(items))", recommendation = "列表页推荐使用 viewModelFactory {} 封装参数传递，避免 lambda 捕获大对象"),
            SceneRecommendItem(scene = "简单无状态 ViewModel", codeExample = "viewModel<ProfileViewModel> { viewModelFactory { ProfileViewModel() } }", recommendation = "无状态或简单 ViewModel 推荐使用简洁的 viewModel {} 语法")
        )
    }

    private fun buildCompatibilitySDKItems(): List<CompatibilitySDKItem> {
        return listOf(
            CompatibilitySDKItem(name = "createViewModelFactory", signature = "fun <T : ViewModel> createViewModelFactory(creationExtras: CreationExtras, factory: () -> T): ViewModelProvider.Factory", description = "封装新旧 API 差异，提供统一的 factory 创建方式", beforeCode = "object : ViewModelProvider.Factory { override fun <T> create(...): T = MyViewModel() as T }", afterCode = "createViewModelFactory(extras) { MyViewModel() }"),
            CompatibilitySDKItem(name = "dslViewModel", signature = "@Composable inline fun <reified T : ViewModel> dslViewModel(noinline factory: () -> T): T", description = "Compose 环境下自动选择合适的 ViewModel 创建方式", beforeCode = "val vm = viewModel<MyViewModel>(factory = MyFactory())", afterCode = "val vm = dslViewModel { MyViewModel() }")
        )
    }

    private fun buildPerformanceData(): List<PerformanceDataPoint> {
        return listOf(
            PerformanceDataPoint("实例化耗时", 1.2f, 2.1f, "ms"),
            PerformanceDataPoint("首次渲染", 16f, 18f, "ms"),
            PerformanceDataPoint("内存分配", 3.4f, 5.6f, "KB"),
            PerformanceDataPoint("Lambda 捕获", 0.8f, 0f, "ms"),
            PerformanceDataPoint("GC 压力", 1.1f, 1.8f, "次/帧")
        )
    }

    private fun buildRecentScans(): List<RecentScanRecord> {
        return listOf(
            RecentScanRecord(id = "scan_1", moduleName = "app", timestamp = System.currentTimeMillis() - 3600000, resultCount = 6, summary = "发现 2 个 P0 问题，3 个 P1 问题，1 个 P2 问题"),
            RecentScanRecord(id = "scan_2", moduleName = "feature/home", timestamp = System.currentTimeMillis() - 86400000, resultCount = 3, summary = "发现 1 个 P0 问题，2 个 P1 问题")
        )
    }

    companion object {
        private val VIEW_MODEL_SCOPE_CHANGE_TEXT = """
新版 viewModelScope 行为变更说明：

1. 协程父子关系
   旧版：lifecycleScope.launch 在 viewModelScope.launch 外部时，父子关系取决于创建顺序
   新版：viewModelScope 现在始终是 lifecycleScope 的父作用域

2. 取消传播
   旧版：ViewModel onCleared 时会自动取消 viewModelScope 中的协程
   新版：行为一致，但取消传播顺序略有调整

3. 异常处理
   旧版：viewModelScope 中未捕获的异常会在 ViewModel 被清除时统一处理
   新版：行为一致，推荐使用 CoroutineExceptionHandler

4. 测试注意事项
   - 测试时需要确保 TestCoroutineScheduler 与 StandardTestDispatcher 正确配置
   - viewModelScope.cancel() 后再创建新协程需要重新调度
"""
    }
}
