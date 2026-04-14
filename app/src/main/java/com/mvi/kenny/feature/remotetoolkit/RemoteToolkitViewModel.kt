package com.mvi.kenny.feature.remotetoolkit

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// =============================================================
// RemoteToolkitViewModel — MVI ViewModel
// Jetpack Compose Remote Server-Driven Native UI 开发工具包
// =============================================================

class RemoteToolkitViewModel : ViewModel() {

    private val _state = MutableStateFlow(RemoteToolkitState.Initial)
    val state: StateFlow<RemoteToolkitState> = _state.asStateFlow()

    private val _effect = MutableSharedFlow<RemoteToolkitEffect>()
    val effect = _effect.asSharedFlow()

    init {
        loadDashboardData()
    }

    // =============================================================
    // Process Intent / 处理用户意图
    // =============================================================
    fun processIntent(intent: RemoteToolkitIntent) {
        when (intent) {
            is RemoteToolkitIntent.SelectPage -> selectPage(intent.page)
            is RemoteToolkitIntent.RunCompatibilityScan -> runCompatibilityScan()
            is RemoteToolkitIntent.ToggleIssueExpanded -> toggleIssue(intent.issueId)
            is RemoteToolkitIntent.StartDebugServer -> startDebugServer()
            is RemoteToolkitIntent.StopDebugServer -> stopDebugServer()
            is RemoteToolkitIntent.SelectTreeNode -> selectTreeNode(intent.nodeId)
            is RemoteToolkitIntent.ClearTimeline -> clearTimeline()
            is RemoteToolkitIntent.AddRemoteFunction -> addRemoteFunction()
            is RemoteToolkitIntent.RemoveRemoteFunction -> removeRemoteFunction(intent.functionId)
            is RemoteToolkitIntent.UpdateTLSConfig -> updateTLSConfig(intent.config)
            is RemoteToolkitIntent.TestConnection -> testConnection()
            is RemoteToolkitIntent.SaveConfiguration -> saveConfiguration()
            is RemoteToolkitIntent.RollbackVersion -> rollbackVersion(intent.versionId)
            is RemoteToolkitIntent.UpdateTrafficSplit -> updateTrafficSplit(intent.split)
            is RemoteToolkitIntent.RunBenchmark -> runBenchmark()
            is RemoteToolkitIntent.ExportReport -> exportReport()
            is RemoteToolkitIntent.UpdateAIPrompt -> updateAIPrompt(intent.prompt)
            is RemoteToolkitIntent.GenerateUI -> generateUI()
            is RemoteToolkitIntent.CopyGeneratedCode -> copyGeneratedCode()
            is RemoteToolkitIntent.DeployGeneratedUI -> deployGeneratedUI()
            is RemoteToolkitIntent.StopABTest -> stopABTest(intent.testId)
            is RemoteToolkitIntent.DeclareWinner -> declareWinner(intent.testId, intent.variantId)
            is RemoteToolkitIntent.DismissError -> dismissError()
            is RemoteToolkitIntent.RefreshDashboard -> loadDashboardData()
        }
    }

    // =============================================================
    // Private Methods / 私有方法
    // =============================================================

    private fun selectPage(page: RemoteToolkitPage) {
        _state.value = _state.value.copy(activePage = page)
    }

    private fun loadDashboardData() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            delay(300) // Simulate network / 模拟网络延迟
            _state.value = _state.value.copy(
                isLoading = false,
                quickStats = QuickStats(
                    compatibilityScore = 78,
                    serializedUis = 24,
                    activeVersions = 3,
                    abTestsRunning = 2
                ),
                recentActivities = listOf(
                    Activity("1", "PRD-XXX scanned — 94% ✅", System.currentTimeMillis() - 2 * 60 * 1000, ActivityType.SCAN),
                    Activity("2", "New version deployed v3", System.currentTimeMillis() - 15 * 60 * 1000, ActivityType.DEPLOY),
                    Activity("3", "A/B test \"CTA color\" → variant B leads by 12%", System.currentTimeMillis() - 60 * 60 * 1000, ActivityType.AB_TEST)
                )
            )
        }
    }

    private fun runCompatibilityScan() {
        viewModelScope.launch {
            _state.value = _state.value.copy(scanStatus = ScanStatus.RUNNING)
            delay(2000) // Simulate scan / 模拟扫描
            _state.value = _state.value.copy(
                scanStatus = ScanStatus.DONE,
                scanScore = 78,
                scanIssues = listOf(
                    CompatibilityIssue(
                        id = "c1", severity = IssueSeverity.CRITICAL,
                        composableName = "MyCustomView",
                        filePath = "ui/screens/HomeScreen.kt", line = 34,
                        reason = "Captures non-serializable state / 捕获了不可序列化的状态",
                        suggestion = "Use rememberSaveable or remember { mutableStateOf(...) }"
                    ),
                    CompatibilityIssue(
                        id = "c2", severity = IssueSeverity.CRITICAL,
                        composableName = "DashboardWidget",
                        filePath = "ui/components/Dashboard.kt", line = 88,
                        reason = "Lambda prop captured in closure / Lambda 属性被闭包捕获",
                        suggestion = "Use @Stable / @Serializable annotations"
                    ),
                    CompatibilityIssue(
                        id = "w1", severity = IssueSeverity.WARNING,
                        composableName = "ProductCard",
                        filePath = "ui/cards/ProductCard.kt", line = 45,
                        reason = "Complex object passed as prop / 复杂对象作为 prop 传递",
                        suggestion = "Extract to @RemoteSerializable data class"
                    ),
                    CompatibilityIssue(
                        id = "w2", severity = IssueSeverity.WARNING,
                        composableName = "ChatBubble",
                        filePath = "ui/chat/ChatBubble.kt", line = 22,
                        reason = "Inline lambda in recompose / 重组合并中的内联 lambda",
                        suggestion = "Extract to remember { mutableStateOf(...) }"
                    ),
                    CompatibilityIssue(
                        id = "p1", severity = IssueSeverity.PASS,
                        composableName = "SimpleText",
                        filePath = "ui/text/SimpleText.kt", line = 10,
                        reason = "All props are primitive types / 所有 prop 都是原生类型",
                        suggestion = "No action needed"
                    ),
                    CompatibilityIssue(
                        id = "p2", severity = IssueSeverity.PASS,
                        composableName = "IconButton",
                        filePath = "ui/buttons/IconButton.kt", line = 15,
                        reason = "Stateless composable / 无状态 Composable",
                        suggestion = "No action needed"
                    )
                )
            )
            _effect.emit(RemoteToolkitEffect.ScanComplete)
        }
    }

    private fun toggleIssue(issueId: String) {
        val current = _state.value.expandedIssueIds
        _state.value = _state.value.copy(
            expandedIssueIds = if (issueId in current) current - issueId else current + issueId
        )
    }

    private fun startDebugServer() {
        viewModelScope.launch {
            _state.value = _state.value.copy(serverStatus = ServerStatus.STARTING)
            delay(1000)
            _state.value = _state.value.copy(
                serverStatus = ServerStatus.RUNNING,
                uiTree = UIRemoteNode(
                    id = "root",
                    name = "Column",
                    type = "Column",
                    props = mapOf("modifier" to "Modifier"),
                    children = listOf(
                        UIRemoteNode("c1", "Card", "Card", mapOf("elevation" to "4.dp"), emptyList(), 128),
                        UIRemoteNode("c2", "Row", "Row", mapOf("modifier" to "Modifier.fillMaxWidth()"), emptyList(), 64)
                    ),
                    serializedSize = 512
                ),
                timelineEvents = listOf(
                    TimelineEvent("t1", "Send", 50, TimelineStatus.COMPLETE),
                    TimelineEvent("t2", "Recv", 12, TimelineStatus.COMPLETE),
                    TimelineEvent("t3", "Decode", 300, TimelineStatus.COMPLETE),
                    TimelineEvent("t4", "Render", 800, TimelineStatus.ACTIVE)
                )
            )
            _effect.emit(RemoteToolkitEffect.ServerStarted(8080))
        }
    }

    private fun stopDebugServer() {
        _state.value = _state.value.copy(serverStatus = ServerStatus.STOPPED, uiTree = null, timelineEvents = emptyList())
    }

    private fun selectTreeNode(nodeId: String) {
        _state.value = _state.value.copy(selectedNodeId = nodeId)
    }

    private fun clearTimeline() {
        _state.value = _state.value.copy(timelineEvents = emptyList())
    }

    private fun addRemoteFunction() {
        val newFn = RemoteFunction(
            id = "fn_${System.currentTimeMillis()}",
            name = "getUserProfile",
            returnType = "RemoteResult<UserProfile>",
            parameters = listOf("userId: String"),
            code = "@RemoteFunction\nfun getUserProfile(userId: String): RemoteResult<UserProfile>"
        )
        _state.value = _state.value.copy(
            registeredFunctions = _state.value.registeredFunctions + newFn
        )
    }

    private fun removeRemoteFunction(functionId: String) {
        _state.value = _state.value.copy(
            registeredFunctions = _state.value.registeredFunctions.filter { it.id != functionId }
        )
    }

    private fun updateTLSConfig(config: TLSConfig) {
        _state.value = _state.value.copy(tlsConfig = config)
    }

    private fun testConnection() {
        viewModelScope.launch {
            delay(1500)
            _effect.emit(RemoteToolkitEffect.ConnectionTestResult(true))
        }
    }

    private fun saveConfiguration() {
        viewModelScope.launch {
            delay(500)
            _effect.emit(RemoteToolkitEffect.ConfigSaved)
        }
    }

    private fun rollbackVersion(versionId: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isDeploying = true)
            delay(1000)
            _state.value = _state.value.copy(isDeploying = false)
            _effect.emit(RemoteToolkitEffect.RollbackComplete)
        }
    }

    private fun updateTrafficSplit(split: Map<String, Int>) {
        _state.value = _state.value.copy(trafficSplit = split)
        viewModelScope.launch { _effect.emit(RemoteToolkitEffect.TrafficSplitUpdated) }
    }

    private fun runBenchmark() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isBenchmarking = true)
            delay(2000)
            _state.value = _state.value.copy(
                isBenchmarking = false,
                metrics = PerformanceMetrics(
                    avgSerializeMs = 2.3f,
                    avgDeserializeMs = 1.8f,
                    avgByteSizeKb = 4.2f,
                    compressionPercent = 87,
                    serializeTrend = Trend.DOWN,
                    deserializeTrend = Trend.UP,
                    sizeTrend = Trend.DOWN,
                    compressionTrend = Trend.UP
                ),
                breakdownItems = listOf(
                    BreakdownItem("Layout", 42, Color(0xFFBB86FC)),
                    BreakdownItem("Style", 25, Color(0xFF03DAC6)),
                    BreakdownItem("Props", 17, Color(0xFF64B5F6)),
                    BreakdownItem("Strings", 8, Color(0xFFFFAB40)),
                    BreakdownItem("Images", 4, Color(0xFFCF6679))
                )
            )
            _effect.emit(RemoteToolkitEffect.BenchmarkComplete)
        }
    }

    private fun exportReport() {
        viewModelScope.launch { _effect.emit(RemoteToolkitEffect.ReportExported) }
    }

    private fun updateAIPrompt(prompt: String) {
        _state.value = _state.value.copy(aiPrompt = prompt)
    }

    private fun generateUI() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isGenerating = true)
            delay(2500)
            _state.value = _state.value.copy(
                isGenerating = false,
                generatedCode = """@RemoteComposable
fun ProductCard(
    imageUrl: String,
    name: String,
    price: Double,
    onAddToCart: () -> Unit
) {
    Card(elevation = 4.dp) {
        Column {
            AsyncImage(
                model = imageUrl,
                contentDescription = name,
                modifier = Modifier.height(160.dp).fillMaxWidth()
            )
            Column(modifier = Modifier.padding(12.dp)) {
                Text(text = name, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = "${'$'}{String.format("%.2f", price)}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = onAddToCart, modifier = Modifier.fillMaxWidth()) {
                    Text("Add to Cart")
                }
            }
        }
    }
}"""
            )
        }
    }

    private fun copyGeneratedCode() {
        viewModelScope.launch { _effect.emit(RemoteToolkitEffect.CodeCopied("Code copied to clipboard!")) }
    }

    private fun deployGeneratedUI() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isDeploying = true)
            delay(1500)
            _state.value = _state.value.copy(isDeploying = false)
            _effect.emit(RemoteToolkitEffect.DeploymentStarted)
        }
    }

    private fun stopABTest(testId: String) {
        _state.value = _state.value.copy(
            activeTests = _state.value.activeTests.filter { it.id != testId }
        )
    }

    private fun declareWinner(testId: String, variantId: String) {
        viewModelScope.launch { _effect.emit(RemoteToolkitEffect.WinnerDeclared) }
    }

    private fun dismissError() {
        _state.value = _state.value.copy(error = null)
    }
}
