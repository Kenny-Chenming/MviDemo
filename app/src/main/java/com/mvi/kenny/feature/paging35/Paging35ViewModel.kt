package com.mvi.kenny.feature.paging35

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ============================================================
 * Paging35ViewModel — Paging 3.5 asState 操作符开发工具包 ViewModel
 * ============================================================
 * MVI Architecture Pattern / MVI 架构模式
 *
 * - State: Managed via StateFlow, immutable data class
 * - Intent: User intentions processed and update State
 * - Effect: One-time side effects via SharedFlow
 *
 * PRD-093: Paging 3.5 asState 操作符开发工具包
 *
 * @see Paging35Contract MVI definitions
 * @see Paging35Screen Main screen
 */

// =============================================================
// Paging35ViewModel — Main ViewModel
// =============================================================
/**
 * Paging 35 ViewModel / Paging 35 ViewModel
 *
 * Main ViewModel for the Paging 3.5 toolkit.
 * Manages navigation between dashboard and sub-tools.
 *
 * @see Paging35DashboardState Dashboard state
 * @see Paging35DashboardIntent Dashboard intents
 * @see Paging35DashboardEffect Dashboard effects
 */
class Paging35ViewModel : ViewModel() {

    // Dashboard State / 仪表盘状态
    private val _dashboardState = MutableStateFlow(Paging35DashboardState.Initial)
    val dashboardState: StateFlow<Paging35DashboardState> = _dashboardState.asStateFlow()

    // Template Generator State / 模板生成器状态
    private val _templateState = MutableStateFlow(AsStateTemplateState.Initial)
    val templateState: StateFlow<AsStateTemplateState> = _templateState.asStateFlow()

    // Migration Report State / 迁移报告状态
    private val _migrationState = MutableStateFlow(MigrationReportState.Initial)
    val migrationState: StateFlow<MigrationReportState> = _migrationState.asStateFlow()

    // Debug Panel State / 调试面板状态
    private val _debugState = MutableStateFlow(PagingDebugPanelState.Initial)
    val debugState: StateFlow<PagingDebugPanelState> = _debugState.asStateFlow()

    // Troubleshooting State / 踩坑排查状态
    private val _troubleshootingState = MutableStateFlow(PagingTroubleshootingState.Initial)
    val troubleshootingState: StateFlow<PagingTroubleshootingState> = _troubleshootingState.asStateFlow()

    // =============================================================
    // Effects (one-time events / 一次性事件)
    // =============================================================

    // Dashboard Effects / 仪表盘副作用
    private val _dashboardEffects = MutableSharedFlow<Paging35DashboardEffect>()
    val dashboardEffects: SharedFlow<Paging35DashboardEffect> = _dashboardEffects.asSharedFlow()

    // Template Effects / 模板生成器副作用
    private val _templateEffects = MutableSharedFlow<AsStateTemplateEffect>()
    val templateEffects: SharedFlow<AsStateTemplateEffect> = _templateEffects.asSharedFlow()

    // Migration Effects / 迁移报告副作用
    private val _migrationEffects = MutableSharedFlow<MigrationReportEffect>()
    val migrationEffects: SharedFlow<MigrationReportEffect> = _migrationEffects.asSharedFlow()

    // Debug Effects / 调试面板副作用
    private val _debugEffects = MutableSharedFlow<PagingDebugPanelEffect>()
    val debugEffects: SharedFlow<PagingDebugPanelEffect> = _debugEffects.asSharedFlow()

    // Troubleshooting Effects / 踩坑排查副作用
    private val _troubleshootingEffects = MutableSharedFlow<PagingTroubleshootingEffect>()
    val troubleshootingEffects: SharedFlow<PagingTroubleshootingEffect> = _troubleshootingEffects.asSharedFlow()

    // =============================================================
    // Dashboard Intent Processing / 仪表盘意图处理
    // =============================================================

    /**
     * Process Dashboard Intent / 处理仪表盘意图
     *
     * @param intent User intent / 用户意图
     */
    fun processDashboardIntent(intent: Paging35DashboardIntent) {
        when (intent) {
            is Paging35DashboardIntent.StartScan -> handleStartScan()
            is Paging35DashboardIntent.RefreshHealth -> handleRefreshHealth()
            is Paging35DashboardIntent.NavigateToTool -> handleNavigateToTool(intent.tool)
            is Paging35DashboardIntent.SelectTool -> handleSelectTool(intent.tool)
            is Paging35DashboardIntent.BackToDashboard -> handleBackToDashboard()
        }
    }

    /**
     * Handle Start Scan / 处理开始扫描
     */
    private fun handleStartScan() {
        viewModelScope.launch {
            _dashboardState.update { it.copy(isScanning = true, scanProgress = 0f) }

            // Simulate scanning progress / 模拟扫描进度
            for (i in 1..10) {
                delay(200)
                _dashboardState.update { it.copy(scanProgress = i / 10f) }
            }

            // Simulate scan results / 模拟扫描结果
            val mockFiles = listOf(
                MigrationFile(
                    filePath = "com/example/ui/screen/UserListScreen.kt",
                    currentApi = "collectAsLazyPagingItems()",
                    suggestedApi = "asState() + LazyColumn",
                    compatibilityLevel = CompatibilityLevel.DIRECT_REPLACE,
                    issues = emptyList()
                ),
                MigrationFile(
                    filePath = "com/example/ui/screen/ProductGridScreen.kt",
                    currentApi = "collectAsLazyPagingItems()",
                    suggestedApi = "asState() + LazyVerticalGrid",
                    compatibilityLevel = CompatibilityLevel.NEEDS_ADJUSTMENT,
                    issues = listOf("Uses header/footer callbacks", "Needs PagingDataAdditions")
                ),
                MigrationFile(
                    filePath = "com/example/ui/screen/ChatMessagesScreen.kt",
                    currentApi = "collectAsLazyPagingItems()",
                    suggestedApi = "asState() with MultiViewHolder",
                    compatibilityLevel = CompatibilityLevel.NEEDS_REFACTOR,
                    issues = listOf("Multi-type items with different ViewHolders", "Complex click handling per type")
                )
            )

            val totalFiles = mockFiles.size
            val asStateCount = 0
            val asStateRatio = asStateCount.toFloat() / totalFiles

            _dashboardState.update {
                it.copy(
                    isScanning = false,
                    scanProgress = 1f,
                    totalPagingSources = totalFiles,
                    asStateUsageRatio = asStateRatio,
                    collectAsLazyCount = totalFiles,
                    healthScore = calculateHealthScore(asStateRatio, totalFiles),
                    migrationFiles = mockFiles
                )
            }

            _dashboardEffects.emit(
                Paging35DashboardEffect.ShowScanComplete(
                    issuesFound = mockFiles.count { f -> f.compatibilityLevel != CompatibilityLevel.DIRECT_REPLACE }
                )
            )
        }
    }

    /**
     * Calculate health score / 计算健康度评分
     *
     * @param asStateRatio asState usage ratio / asState 使用比例
     * @param totalFiles Total files / 总文件数
     * @return Health score 0-100 / 健康度评分 0-100
     */
    private fun calculateHealthScore(asStateRatio: Float, totalFiles: Int): Int {
        if (totalFiles == 0) return 100
        return (asStateRatio * 100).toInt().coerceIn(0, 100)
    }

    /**
     * Handle Refresh Health / 处理刷新健康度
     */
    private fun handleRefreshHealth() {
        viewModelScope.launch {
            val current = _dashboardState.value
            _dashboardState.update {
                it.copy(healthScore = calculateHealthScore(current.asStateUsageRatio, current.totalPagingSources))
            }
        }
    }

    /**
     * Handle Navigate to Tool / 处理导航到工具
     */
    private fun handleNavigateToTool(tool: PagingTool) {
        viewModelScope.launch {
            _dashboardState.update { it.copy(selectedTool = tool) }
            _dashboardEffects.emit(Paging35DashboardEffect.NavigateToTool(tool))
        }
    }

    /**
     * Handle Select Tool / 处理选择工具
     */
    private fun handleSelectTool(tool: PagingTool) {
        _dashboardState.update { it.copy(selectedTool = tool) }
    }

    /**
     * Handle Back to Dashboard / 处理返回仪表盘
     */
    private fun handleBackToDashboard() {
        _dashboardState.update { it.copy(selectedTool = null) }
    }

    // =============================================================
    // Template Generator Intent Processing / 模板生成器意图处理
    // =============================================================

    /**
     * Process Template Intent / 处理模板意图
     *
     * @param intent User intent / 用户意图
     */
    fun processTemplateIntent(intent: AsStateTemplateIntent) {
        when (intent) {
            is AsStateTemplateIntent.SelectScenario -> handleSelectScenario(intent.scenario)
            is AsStateTemplateIntent.UpdatePageSize -> handleUpdatePageSize(intent.size)
            is AsStateTemplateIntent.TogglePrefetch -> handleTogglePrefetch(intent.enabled)
            is AsStateTemplateIntent.GenerateCode -> handleGenerateCode()
            is AsStateTemplateIntent.CopyCode -> handleCopyCode()
        }
    }

    /**
     * Handle Select Scenario / 处理选择场景
     */
    private fun handleSelectScenario(scenario: PagingScenario) {
        _templateState.update { it.copy(selectedScenario = scenario) }
        handleGenerateCode()
    }

    /**
     * Handle Update Page Size / 处理更新分页大小
     */
    private fun handleUpdatePageSize(size: Int) {
        _templateState.update { it.copy(pageSize = size) }
        handleGenerateCode()
    }

    /**
     * Handle Toggle Prefetch / 处理切换预取
     */
    private fun handleTogglePrefetch(enabled: Boolean) {
        _templateState.update { it.copy(prefetchEnabled = enabled) }
        handleGenerateCode()
    }

    /**
     * Handle Generate Code / 处理生成代码
     */
    private fun handleGenerateCode() {
        viewModelScope.launch {
            _templateState.update { it.copy(isGenerating = true) }

            delay(300) // Simulate code generation / 模拟代码生成

            val state = _templateState.value
            val code = generateTemplateCode(state.selectedScenario, state.pageSize, state.prefetchEnabled)

            _templateState.update { it.copy(generatedCode = code, isGenerating = false) }
        }
    }

    /**
     * Generate Template Code / 生成模板代码
     *
     * @param scenario Paging scenario / 分页场景
     * @param pageSize Page size / 分页大小
     * @param prefetchEnabled Whether prefetch is enabled / 是否启用预取
     * @return Generated code / 生成的代码
     */
    private fun generateTemplateCode(scenario: PagingScenario, pageSize: Int, prefetchEnabled: Boolean): String {
        return when (scenario) {
            PagingScenario.BASIC_LIST -> generateBasicListTemplate(pageSize, prefetchEnabled)
            PagingScenario.HEADER_FOOTER -> generateHeaderFooterTemplate(pageSize, prefetchEnabled)
            PagingScenario.GRID_LIST -> generateGridTemplate(pageSize, prefetchEnabled)
            PagingScenario.MULTI_TYPE -> generateMultiTypeTemplate(pageSize, prefetchEnabled)
            PagingScenario.ROOM_INTEGRATION -> generateRoomTemplate(pageSize, prefetchEnabled)
        }
    }

    /**
     * Generate Basic List Template / 生成基础列表模板
     */
    private fun generateBasicListTemplate(pageSize: Int, prefetchEnabled: Boolean): String {
        val prefetchStr = if (prefetchEnabled) """
    // Enable prefetch / 启用预取
    .flow
    .asState()
""" else ""
        return """
// ============================================================
// Basic List with asState / 基础列表 + asState
// ============================================================

@Composable
fun BasicPagingList(
    pagingSourceFactory: () -> PagingSource<Int, Item>
) {
    // Create pager with config / 创建 pager 并配置
    val pager = rememberPager(
        pagingSourceFactory = pagingSourceFactory,
        config = PagingConfig(
            pageSize = $pageSize,
            enablePlaceholders = false,
            prefetchDistance = ${if (prefetchEnabled) pageSize / 2 else 0}
        )
    )

    // Collect as State using asState operator
    // 使用 asState 操作符收集为 State
    val pagingData = pager.flow
        .asState(initialState = PagingData.empty())

    // Display in LazyColumn / 在 LazyColumn 中显示
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(
            count = pagingData.itemCount,
            key = { index -> pagingData[index]?.id ?: index }
        ) { index ->
            val item = pagingData[index]
            item?.let { ItemRow(item = it) }
        }
    }

    // Handle LoadState / 处理 LoadState
    val refreshLoadState = pager.loadStateFlow
        .map { it.refresh }
        .asState(initialLoadState = LoadState.Idle)

    when (refreshLoadState) {
        is LoadState.Loading -> {
            // Show loading indicator / 显示加载指示器
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
        is LoadState.Error -> {
            // Show error / 显示错误
            val error = (refreshLoadState as LoadState.Error).error
            ErrorMessage(message = error.localizedMessage ?: "Unknown error")
        }
        is LoadState.Idle -> { /* Idle state / 空闲状态 */ }
    }
}
""".trimIndent()
    }

    /**
     * Generate Header/Footer Template / 生成 Header/Footer 模板
     */
    private fun generateHeaderFooterTemplate(pageSize: Int, prefetchEnabled: Boolean): String {
        return """
// ============================================================
// List with Header/Footer / 带 Header/Footer 的列表
// ============================================================

@Composable
fun HeaderFooterPagingList(
    pagingSourceFactory: () -> PagingSource<Int, Item>
) {
    val pager = rememberPager(
        pagingSourceFactory = pagingSourceFactory,
        config = PagingConfig(
            pageSize = $pageSize,
            enablePlaceholders = false,
            prefetchDistance = ${if (prefetchEnabled) pageSize / 2 else 0}
        )
    )

    val pagingData = pager.flow.asState(initialState = PagingData.empty())
    val appendLoadState = pager.loadStateFlow
        .map { it.append }
        .asState(initialLoadState = LoadState.Idle)

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Header / 头部
        item(key = "header") {
            Text(
                text = "Paging 3.5 with asState",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }

        // Items / 数据项
        items(
            count = pagingData.itemCount,
            key = { index -> "item_" + "$" + "{pagingData[index]?.id ?: index}" }
        ) { index ->
            val item = pagingData[index]
            item?.let { ItemCard(item = it) }
        }

        // Append load state / 追加加载状态
        when (appendLoadState) {
            is LoadState.Loading -> {
                item(key = "footer_loading") {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    }
                }
            }
            is LoadState.Error -> {
                item(key = "footer_error") {
                    TextButton(
                        onClick = { /* Retry / 重试 */ },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Retry Loading / 重试加载")
                    }
                }
            }
            else -> { /* Idle / 空闲 */ }
        }
    }
}
""".trimIndent()
    }

    /**
     * Generate Grid Template / 生成 Grid 模板
     */
    private fun generateGridTemplate(pageSize: Int, prefetchEnabled: Boolean): String {
        return """
// ============================================================
// Grid List with asState / Grid 列表 + asState
// ============================================================

@Composable
fun GridPagingList(
    pagingSourceFactory: () -> PagingSource<Int, Item>,
    columns: Int = 2
) {
    val pager = rememberPager(
        pagingSourceFactory = pagingSourceFactory,
        config = PagingConfig(
            pageSize = $pageSize,
            enablePlaceholders = false,
            prefetchDistance = ${if (prefetchEnabled) pageSize / 2 else 0}
        )
    )

    val pagingData = pager.flow.asState(initialState = PagingData.empty())

    LazyVerticalGrid(
        columns = GridCells.Fixed(columns),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(
            count = pagingData.itemCount,
            key = { index -> "grid_item_" + "$" + "{pagingData[index]?.id ?: index}" }
        ) { index ->
            val item = pagingData[index]
            item?.let { GridItemCard(item = it) }
        }
    }
}
""".trimIndent()
    }

    /**
     * Generate Multi-Type Template / 生成多类型模板
     */
    private fun generateMultiTypeTemplate(pageSize: Int, prefetchEnabled: Boolean): String {
        return """
// ============================================================
// Multi-Type Items with asState / 多类型 Item + asState
// ============================================================

enum class ItemType {
    TEXT, IMAGE, VIDEO, AD
}

sealed class PagingItem {
    data class TextItem(val id: String, val content: String) : PagingItem()
    data class ImageItem(val id: String, val url: String) : PagingItem()
    data class VideoItem(val id: String, val url: String, val duration: Int) : PagingItem()
    data class AdItem(val id: String, val adContent: String) : PagingItem()
}

@Composable
fun MultiTypePagingList(
    pagingSourceFactory: () -> PagingSource<Int, PagingItem>
) {
    val pager = rememberPager(
        pagingSourceFactory = pagingSourceFactory,
        config = PagingConfig(
            pageSize = $pageSize,
            enablePlaceholders = false,
            prefetchDistance = ${if (prefetchEnabled) pageSize / 2 else 0}
        )
    )

    val pagingData = pager.flow.asState(initialState = PagingData.empty())

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(
            count = pagingData.itemCount,
            key = { index -> "multi_" + "$" + "{pagingData[index]?.hashCode() ?: index}" }
        ) { index ->
            val item = pagingData[index]
            when (item) {
                is PagingItem.TextItem -> TextRow(item = item)
                is PagingItem.ImageItem -> ImageRow(item = item)
                is PagingItem.VideoItem -> VideoRow(item = item)
                is PagingItem.AdItem -> AdRow(item = item)
                null -> { /* Placeholder / 占位符 */ }
            }
        }
    }
}
""".trimIndent()
    }

    /**
     * Generate Room Integration Template / 生成 Room 集成模板
     */
    private fun generateRoomTemplate(pageSize: Int, prefetchEnabled: Boolean): String {
        return """
// ============================================================
// Room + Paging 3 with asState / Room + Paging 3 + asState
// ============================================================

@Dao
interface ItemDao {
    @Query("SELECT * FROM items ORDER BY id ASC")
    fun pagingSourceFactory(): PagingSource<Int, Item>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<Item>)

    @Query("DELETE FROM items")
    suspend fun clearAll()
}

@Database(entities = [Item::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun itemDao(): ItemDao
}

@Composable
fun RoomPagingList(
    database: AppDatabase,
    query: String = ""
) {
    val pager = rememberPager(
        pagingSourceFactory = {
            if (query.isBlank()) {
                database.itemDao().pagingSourceFactory()
            } else {
                // Filtered query / 过滤查询
                database.itemDao()
                    .pagingSourceFactory()
            }
        },
        config = PagingConfig(
            pageSize = $pageSize,
            enablePlaceholders = false,
            prefetchDistance = ${if (prefetchEnabled) pageSize / 2 else 0}
        )
    )

    val pagingData = pager.flow.asState(initialState = PagingData.empty())

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp)
    ) {
        items(
            count = pagingData.itemCount,
            key = { index -> "room_" + "$" + "{pagingData[index]?.id ?: index}" }
        ) { index ->
            val item = pagingData[index]
            item?.let { ItemRow(item = it) }
        }
    }

    // IMPORTANT: Room + Paging 3 Key Points
    // 重要：Room + Paging 3 关键点
    // 1. PagingSourceFactory must be a lambda / PagingSourceFactory 必须是 lambda
    // 2. Use IO dispatcher for database operations / 使用 IO dispatcher 执行数据库操作
    // 3. Invalidate PagingSource when data changes / 数据变化时失效 PagingSource
}
""".trimIndent()
    }

    /**
     * Handle Copy Code / 处理复制代码
     */
    private fun handleCopyCode() {
        viewModelScope.launch {
            _templateEffects.emit(AsStateTemplateEffect.CodeCopied)
        }
    }

    // =============================================================
    // Migration Report Intent Processing / 迁移报告意图处理
    // =============================================================

    /**
     * Process Migration Intent / 处理迁移意图
     *
     * @param intent User intent / 用户意图
     */
    fun processMigrationIntent(intent: MigrationReportIntent) {
        when (intent) {
            is MigrationReportIntent.GenerateReport -> handleGenerateReport()
            is MigrationReportIntent.IgnoreFile -> handleIgnoreFile(intent.filePath)
            is MigrationReportIntent.ExportMarkdown -> handleExportMarkdown()
            is MigrationReportIntent.SelectFile -> handleSelectFile(intent.file)
            is MigrationReportIntent.ClearSelectedFile -> handleClearSelectedFile()
            is MigrationReportIntent.FilterByCompatibility -> handleFilterByCompatibility(intent.level)
        }
    }

    /**
     * Handle Generate Report / 处理生成报告
     */
    private fun handleGenerateReport() {
        viewModelScope.launch {
            _migrationState.update { it.copy(isGeneratingReport = true) }

            // Use dashboard state files / 使用仪表盘状态中的文件
            val files = _dashboardState.value.migrationFiles

            delay(500) // Simulate report generation / 模拟报告生成

            _migrationState.update {
                it.copy(
                    isGeneratingReport = false,
                    filesToMigrate = files,
                    totalFiles = files.size,
                    directReplaceCount = files.count { f -> f.compatibilityLevel == CompatibilityLevel.DIRECT_REPLACE },
                    needsAdjustmentCount = files.count { f -> f.compatibilityLevel == CompatibilityLevel.NEEDS_ADJUSTMENT },
                    needsRefactorCount = files.count { f -> f.compatibilityLevel == CompatibilityLevel.NEEDS_REFACTOR }
                )
            }
        }
    }

    /**
     * Handle Ignore File / 处理忽略文件
     */
    private fun handleIgnoreFile(filePath: String) {
        _migrationState.update { state ->
            state.copy(
                filesToMigrate = state.filesToMigrate.map { file ->
                    if (file.filePath == filePath) file.copy(isIgnored = true) else file
                }
            )
        }
    }

    /**
     * Handle Export Markdown / 处理导出 Markdown
     */
    private fun handleExportMarkdown() {
        viewModelScope.launch {
            val state = _migrationState.value
            val markdown = buildMarkdownReport(state)

            // In real app, save to file / 实际应用中保存到文件
            _migrationEffects.emit(MigrationReportEffect.ExportComplete("Migration_Report_${System.currentTimeMillis()}.md"))
        }
    }

    /**
     * Build Markdown Report / 构建 Markdown 报告
     */
    private fun buildMarkdownReport(state: MigrationReportState): String {
        return buildString {
            appendLine("# Paging 3 Migration Report")
            appendLine()
            appendLine("## Summary / 摘要")
            appendLine()
            appendLine("| Metric | Count |")
            appendLine("|--------|-------|")
            appendLine("| Total Files | ${state.totalFiles} |")
            appendLine("| Direct Replace | ${state.directReplaceCount} |")
            appendLine("| Needs Adjustment | ${state.needsAdjustmentCount} |")
            appendLine("| Needs Refactor | ${state.needsRefactorCount} |")
            appendLine()
            appendLine("## Migration Priority / 迁移优先级")
            appendLine()
            state.filesToMigrate
                .filter { !it.isIgnored }
                .sortedBy { it.compatibilityLevel.ordinal }
                .forEach { file ->
                    appendLine("### ${file.filePath}")
                    appendLine()
                    appendLine("- **Current API**: `${file.currentApi}`")
                    appendLine("- **Suggested API**: `${file.suggestedApi}`")
                    appendLine("- **Compatibility**: ${file.compatibilityLevel.emoji} ${file.compatibilityLevel.labelZh}")
                    if (file.issues.isNotEmpty()) {
                        appendLine("- **Issues**:")
                        file.issues.forEach { issue -> appendLine("  - $issue") }
                    }
                    appendLine()
                }
        }
    }

    /**
     * Handle Select File / 处理选择文件
     */
    private fun handleSelectFile(file: MigrationFile) {
        _migrationState.update { it.copy(selectedFile = file) }
    }

    /**
     * Handle Clear Selected File / 处理清除选择文件
     */
    private fun handleClearSelectedFile() {
        _migrationState.update { it.copy(selectedFile = null) }
    }

    /**
     * Handle Filter By Compatibility / 处理按兼容性过滤
     */
    private fun handleFilterByCompatibility(level: CompatibilityLevel?) {
        _migrationState.update { it.copy(filterCompatibility = level) }
    }

    // =============================================================
    // Debug Panel Intent Processing / 调试面板意图处理
    // =============================================================

    /**
     * Process Debug Intent / 处理调试意图
     *
     * @param intent User intent / 用户意图
     */
    fun processDebugIntent(intent: PagingDebugPanelIntent) {
        when (intent) {
            is PagingDebugPanelIntent.SimulateLoadNextPage -> handleSimulateLoadNextPage()
            is PagingDebugPanelIntent.SimulateLoadError -> handleSimulateLoadError()
            is PagingDebugPanelIntent.SimulateRefresh -> handleSimulateRefresh()
            is PagingDebugPanelIntent.ResetState -> handleResetDebugState()
            is PagingDebugPanelIntent.RecordLoadStateChange -> handleRecordLoadStateChange(intent.state)
        }
    }

    /**
     * Handle Simulate Load Next Page / 处理模拟加载下一页
     */
    private fun handleSimulateLoadNextPage() {
        viewModelScope.launch {
            _debugState.update { it.copy(isSimulating = true, loadState = LoadState.LOADING) }

            delay(1000) // Simulate loading / 模拟加载

            val newPage = _debugState.value.currentPage + 1
            val newItems = _debugState.value.totalItems + _templateState.value.pageSize

            _debugState.update {
                it.copy(
                    isSimulating = false,
                    loadState = LoadState.SUCCESS,
                    currentPage = newPage,
                    totalItems = newItems
                )
            }

            handleRecordLoadStateChange(LoadState.SUCCESS)
            _debugEffects.emit(PagingDebugPanelEffect.ShowPulseAnimation(LoadState.SUCCESS.color))
        }
    }

    /**
     * Handle Simulate Load Error / 处理模拟加载失败
     */
    private fun handleSimulateLoadError() {
        viewModelScope.launch {
            _debugState.update { it.copy(isSimulating = true, loadState = LoadState.LOADING) }

            delay(500) // Simulate loading / 模拟加载

            _debugState.update { it.copy(isSimulating = false, loadState = LoadState.ERROR) }

            handleRecordLoadStateChange(LoadState.ERROR)
            _debugEffects.emit(PagingDebugPanelEffect.ShowPulseAnimation(LoadState.ERROR.color))
        }
    }

    /**
     * Handle Simulate Refresh / 处理模拟刷新
     */
    private fun handleSimulateRefresh() {
        viewModelScope.launch {
            _debugState.update {
                it.copy(
                    isSimulating = true,
                    loadState = LoadState.LOADING,
                    refreshTriggerTimes = it.refreshTriggerTimes + 1
                )
            }

            delay(1500) // Simulate refresh / 模拟刷新

            _debugState.update {
                it.copy(
                    isSimulating = false,
                    loadState = LoadState.SUCCESS,
                    currentPage = 0,
                    totalItems = 0
                )
            }

            handleRecordLoadStateChange(LoadState.SUCCESS)
            _debugEffects.emit(PagingDebugPanelEffect.ShowPulseAnimation(LoadState.SUCCESS.color))
        }
    }

    /**
     * Handle Reset Debug State / 处理重置调试状态
     */
    private fun handleResetDebugState() {
        _debugState.update {
            it.copy(
                loadState = LoadState.IDLE,
                currentPage = 0,
                totalPages = 0,
                totalItems = 0,
                loadStateHistory = emptyList(),
                isSimulating = false,
                refreshTriggerTimes = 0
            )
        }
    }

    /**
     * Handle Record Load State Change / 处理记录加载状态变化
     */
    private fun handleRecordLoadStateChange(state: LoadState) {
        _debugState.update { current ->
            val event = LoadStateEvent(
                timestamp = System.currentTimeMillis(),
                loadState = state,
                page = current.currentPage,
                itemCount = current.totalItems
            )
            current.copy(
                loadStateHistory = (current.loadStateHistory + event).takeLast(10)
            )
        }
    }

    // =============================================================
    // Troubleshooting Intent Processing / 踩坑排查意图处理
    // =============================================================

    /**
     * Process Troubleshooting Intent / 处理踩坑排查意图
     *
     * @param intent User intent / 用户意图
     */
    fun processTroubleshootingIntent(intent: PagingTroubleshootingIntent) {
        when (intent) {
            is PagingTroubleshootingIntent.SelectTab -> handleSelectTroubleshootingTab(intent.index)
            is PagingTroubleshootingIntent.ToggleAutoScan -> handleToggleAutoScan(intent.enabled)
            is PagingTroubleshootingIntent.StartScan -> handleStartTroubleshootingScan()
            is PagingTroubleshootingIntent.ExpandCard -> handleExpandCard(intent.cardId)
            is PagingTroubleshootingIntent.CollapseCard -> handleCollapseCard()
            is PagingTroubleshootingIntent.SelectProblem -> handleSelectProblem(intent.problem)
        }
    }

    /**
     * Handle Select Troubleshooting Tab / 处理选择踩坑 Tab
     */
    private fun handleSelectTroubleshootingTab(index: Int) {
        _troubleshootingState.update { it.copy(selectedTabIndex = index, expandedCardId = null) }
    }

    /**
     * Handle Toggle Auto Scan / 处理切换自动扫描
     */
    private fun handleToggleAutoScan(enabled: Boolean) {
        _troubleshootingState.update { it.copy(isAutoScanEnabled = enabled) }
        if (enabled) {
            handleStartTroubleshootingScan()
        }
    }

    /**
     * Handle Start Troubleshooting Scan / 处理开始踩坑扫描
     */
    private fun handleStartTroubleshootingScan() {
        viewModelScope.launch {
            _troubleshootingState.update { it.copy(isScanning = true, scanProgress = 0f) }

            // Simulate scanning / 模拟扫描
            for (i in 1..10) {
                delay(200)
                _troubleshootingState.update { it.copy(scanProgress = i / 10f) }
            }

            // Mock scan results / 模拟扫描结果
            val mockResults = listOf(
                ScanResult(
                    filePath = "com/example/ui/screen/UserListScreen.kt",
                    lineNumber = 42,
                    issueDescription = "collectAsLazyPagingItems usage detected",
                    issueDescriptionZh = "检测到 collectAsLazyPagingItems 使用",
                    severity = CompatibilityLevel.DIRECT_REPLACE
                ),
                ScanResult(
                    filePath = "com/example/ui/screen/ProductGridScreen.kt",
                    lineNumber = 78,
                    issueDescription = "Missing PagingSource invalidation",
                    issueDescriptionZh = "缺少 PagingSource 失效逻辑",
                    severity = CompatibilityLevel.NEEDS_ADJUSTMENT
                )
            )

            _troubleshootingState.update {
                it.copy(isScanning = false, scanProgress = 1f, scanResults = mockResults)
            }

            _troubleshootingEffects.emit(
                PagingTroubleshootingEffect.ShowScanCompleteBottomSheet(mockResults)
            )
        }
    }

    /**
     * Handle Expand Card / 处理展开卡片
     */
    private fun handleExpandCard(cardId: String) {
        _troubleshootingState.update { it.copy(expandedCardId = cardId) }
    }

    /**
     * Handle Collapse Card / 处理收起卡片
     */
    private fun handleCollapseCard() {
        _troubleshootingState.update { it.copy(expandedCardId = null) }
    }

    /**
     * Handle Select Problem / 处理选择问题
     */
    private fun handleSelectProblem(problem: TroubleshootingProblem) {
        _troubleshootingState.update { it.copy(selectedProblem = problem) }
    }
}
