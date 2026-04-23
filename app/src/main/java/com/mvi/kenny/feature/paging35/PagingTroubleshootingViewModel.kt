package com.mvi.kenny.feature.paging35

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * ============================================================
 * PagingTroubleshootingViewModel — 踩坑排查工具状态管理
 * ============================================================
 * PRD-093 | Paging 3.5 `asState` 操作符开发工具包
 *
 * @author 开心果 🥜
 */
class PagingTroubleshootingViewModel : ViewModel() {

    // ============================================================
    // State / 状态
    // ============================================================

    private val _state = MutableStateFlow(PagingTroubleshootingState())
    val state: StateFlow<PagingTroubleshootingState> = _state.asStateFlow()

    // ============================================================
    // Effect / 副作用
    // ============================================================

    private val _effect = MutableSharedFlow<PagingTroubleshootingEffect>()
    val effect: SharedFlow<PagingTroubleshootingEffect> = _effect.asSharedFlow()

    // ============================================================
    // Intent 处理
    // ============================================================

    fun processIntent(intent: PagingTroubleshootingIntent) {
        when (intent) {
            is PagingTroubleshootingIntent.SelectScenario -> selectScenario(intent.scenario)
            is PagingTroubleshootingIntent.ToggleAutoDetect -> toggleAutoDetect(intent.enabled)
            is PagingTroubleshootingIntent.ToggleCaseExpand -> toggleCaseExpand(intent.index)
            is PagingTroubleshootingIntent.StartScan -> startScan()
            is PagingTroubleshootingIntent.StopScan -> stopScan()
        }
    }

    // ============================================================
    // 业务逻辑
    // ============================================================

    private fun selectScenario(scenario: TroubleScenario) {
        _state.update {
            it.copy(
                selectedScenario = scenario,
                expandedCaseIndex = -1
            )
        }
    }

    private fun toggleAutoDetect(enabled: Boolean) {
        _state.update { it.copy(autoDetectEnabled = enabled) }
        if (enabled) {
            startScan()
        }
    }

    private fun toggleCaseExpand(index: Int) {
        _state.update {
            it.copy(
                expandedCaseIndex = if (it.expandedCaseIndex == index) -1 else index
            )
        }
    }

    private fun startScan() {
        viewModelScope.launch {
            _state.update { it.copy(isScanning = true, scanProgress = 0f, detectedIssues = emptyList()) }

            val mockFiles = listOf(
                "data/remote/ArticleListScreen.kt",
                "feature/feed/FeedViewModel.kt",
                "data/local/HistoryPagingSource.kt",
                "feature/search/SearchResultScreen.kt"
            )

            val allIssues = mutableListOf<DetectedIssue>()

            mockFiles.forEachIndexed { index, file ->
                delay(600)

                _state.update {
                    it.copy(scanProgress = (index + 1).toFloat() / mockFiles.size)
                }

                val issue = when (_state.value.selectedScenario) {
                    TroubleScenario.DUPLICATE_LOAD -> DetectedIssue(
                        filePath = file,
                        lineNumber = (20..80).random(),
                        description = "检测到可能导致重复加载的 PagingSource 实现",
                        severity = SeverityLevel.YELLOW
                    )
                    TroubleScenario.STATE_LOSS -> DetectedIssue(
                        filePath = file,
                        lineNumber = (20..80).random(),
                        description = "collectAsLazyPagingItems 在 Configuration Change 时状态丢失",
                        severity = SeverityLevel.RED
                    )
                    TroubleScenario.BOUNDARY_ERROR -> DetectedIssue(
                        filePath = file,
                        lineNumber = (20..80).random(),
                        description = "首页/末页判断逻辑可能存在边界问题",
                        severity = SeverityLevel.YELLOW
                    )
                    TroubleScenario.ROOM_INTEGRATION -> DetectedIssue(
                        filePath = file,
                        lineNumber = (20..80).random(),
                        description = "Room PagingSource 未在 IO 线程加载",
                        severity = SeverityLevel.GREEN
                    )
                    TroubleScenario.PULL_TO_REFRESH -> DetectedIssue(
                        filePath = file,
                        lineNumber = (20..80).random(),
                        description = "PullToRefresh 与 PagingData 状态可能冲突",
                        severity = SeverityLevel.YELLOW
                    )
                }
                allIssues.add(issue)
            }

            _state.update {
                it.copy(
                    isScanning = false,
                    detectedIssues = allIssues,
                    scanProgress = 1f
                )
            }

            _effect.emit(
                PagingTroubleshootingEffect.ScanComplete(issuesFound = allIssues.size)
            )
        }
    }

    private fun stopScan() {
        _state.update { it.copy(isScanning = false, scanProgress = 0f) }
    }

    // ============================================================
    // 场景案例数据
    // ============================================================

    /**
     * 获取指定场景的问题案例列表
     *
     * @param scenario 场景类型
     * @return 问题案例列表
     *
     * @author 开心果 🥜
     */
    fun getTroubleCases(scenario: TroubleScenario): List<TroubleCase> {
        return when (scenario) {
            TroubleScenario.DUPLICATE_LOAD -> listOf(
                TroubleCase(
                    title = "重复请求同一页数据",
                    symptom = "网络请求日志显示同一页被请求了多次，PagingData 出现重复条目",
                    causes = listOf(
                        "PagingSource.getRefreshKey() 返回了错误的 key",
                        "重复触发 load() 而没有取消上一次的协程",
                        "PagingData.map{} 转换后数据源不稳定"
                    ),
                    solutions = listOf(
                        SolutionItem(
                            description = "确保 getRefreshKey 返回正确的上一页 key",
                            codeSnippet = """
                                |override fun getRefreshKey(state: PagingState<Int, Item>): Int? {
                                |    return state.anchorPosition?.let { anchor ->
                                |        state.closestPageToPosition(anchor)?.prevKey?.plus(1)
                                |            ?: state.closestPageToPosition(anchor)?.nextKey?.minus(1)
                                |    }
                                |}
                            """.trimMargin()
                        ),
                        SolutionItem(
                            description = "使用 ConcatAdapter 合并数据源时，确保唯一性",
                            codeSnippet = """
                                |// 避免在多 PagingSource 间切换时重复
                                |adapter.addSource(pagingData1)
                                |adapter.addSource(pagingData2)
                            """.trimMargin()
                        )
                    )
                )
            )

            TroubleScenario.STATE_LOSS -> listOf(
                TroubleCase(
                    title = "Configuration Change 后数据丢失",
                    symptom = "旋转屏幕或进入多窗口模式后，Paging 数据消失，需要重新加载",
                    causes = listOf(
                        "使用 collectAsLazyPagingItems() 而非 remember 保存状态",
                        "asState 的 key 没有正确设置",
                        "ViewModel 的 StateFlow 被重置"
                    ),
                    solutions = listOf(
                        SolutionItem(
                            description = "使用 remember 保存 PagingData，或设置稳定的 asState key",
                            codeSnippet = """
                                |// 方案1: 使用 rememberPagingData
                                |val pagingData = pager.flow.collectAsLazyPagingItems()
                                |
                                |// 方案2: asState 使用稳定的 key
                                |pager.flow.asState(key = "article_list")
                            """.trimMargin()
                        ),
                        SolutionItem(
                            description = "将 PagingData 移到 ViewModel 层管理",
                            codeSnippet = """
                                |// ViewModel
                                |val pagingData: Flow<PagingData<Article>> = pager.flow
                                |
                                |// Compose 层通过 StateFlow 收集
                                |val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                            """.trimMargin()
                        )
                    )
                )
            )

            TroubleScenario.BOUNDARY_ERROR -> listOf(
                TroubleCase(
                    title = "首页/末页判断错误",
                    symptom = "第一页显示"加载更多"，最后一页显示"正在加载..."",
                    causes = listOf(
                        "prevKey / nextKey 返回 null 的逻辑错误",
                        "PagingSource 首次加载时 page 为 null 的处理不当",
                        "LoadParams.key 为 null 时的默认页码逻辑错误"
                    ),
                    solutions = listOf(
                        SolutionItem(
                            description = "正确设置 prevKey 和 nextKey",
                            codeSnippet = """
                                |LoadResult.Page(
                                |    data = response.items,
                                |    prevKey = if (page == 1) null else page - 1,
                                |    nextKey = if (response.hasMore) page + 1 else null
                                |)
                            """.trimMargin()
                        ),
                        SolutionItem(
                            description = "处理 load 参数中的 key",
                            codeSnippet = """
                                |suspend fun load(params: LoadParams<Int>): LoadResult<Int, Item> {
                                |    val page = params.key ?: 1  // 首次加载默认为第1页
                                |    // ...
                                |}
                            """.trimMargin()
                        )
                    )
                )
            )

            TroubleScenario.ROOM_INTEGRATION -> listOf(
                TroubleCase(
                    title = "Room PagingSource 数据不一致",
                    symptom = "Room 数据库数据已更新，但列表显示旧数据",
                    causes = listOf(
                        "PagingSourceFactory 没有 invalidate 机制",
                        "数据库变更后 PagingSource 没有重新加载",
                        "没有在 IO 线程执行数据库操作"
                    ),
                    solutions = listOf(
                        SolutionItem(
                            description = "确保 PagingSourceFactory 在 IO 线程加载",
                            codeSnippet = """
                                |@Dao
                                |interface ArticleDao {
                                |    @Query("SELECT * FROM articles ORDER BY id DESC")
                                |    fun getArticlesPagingSource(): PagingSource<Int, Article>
                                |}
                                |
                                |// Repository 中确保在 IO 线程
                                |withContext(Dispatchers.IO) {
                                |    dao.getArticlesPagingSource()
                                |}
                            """.trimMargin()
                        ),
                        SolutionItem(
                            description = "数据变更后主动 invalidate PagingSource",
                            codeSnippet = """
                                |// 在 Repository 中
                                |suspend fun updateArticle(article: Article) {
                                |    withContext(Dispatchers.IO) {
                                |        dao.update(article)
                                |        // 手动触发 invalidate
                                |        pagingSource.invalidate()
                                |    }
                                |}
                            """.trimMargin()
                        )
                    )
                )
            )

            TroubleScenario.PULL_TO_REFRESH -> listOf(
                TroubleCase(
                    title = "PullToRefresh 与 Paging 状态冲突",
                    symptom = "下拉刷新时，loading 状态和 refresh 状态叠加，导致 UI 闪烁",
                    causes = listOf(
                        "PullToRefresh 的 isRefreshing 和 PagingData 的 LoadState 重复监听",
                        "refresh() 调用时机与 PagingData 加载冲突",
                        "刷新时没有正确重置 PagingSource"
                    ),
                    solutions = listOf(
                        SolutionItem(
                            description = "使用 PagingData.refresh() 统一触发刷新",
                            codeSnippet = """
                                |// Compose 中
                                |val pullRefreshState = rememberPullToRefreshState()
                                |
                                |Box(modifier = Modifier.pullToRefresh(pullRefreshState)) {
                                |    LazyColumn {
                                |        // items
                                |    }
                                |}
                                |
                                |// 下拉刷新时
                                |LaunchedEffect(pullRefreshState.isRefreshing) {
                                |    if (pullRefreshState.isRefreshing) {
                                |        pagingData.refresh()
                                |    }
                                |}
                            """.trimMargin()
                        ),
                        SolutionItem(
                            description = "监听 loadState.refresh 来结束刷新动画",
                            codeSnippet = """
                                |LaunchedEffect(pagingData.loadState.refresh) {
                                |    if (pagingData.loadState.refresh is LoadState.NotLoading) {
                                |        pullRefreshState.endRefresh()
                                |    }
                                |}
                            """.trimMargin()
                        )
                    )
                )
            )
        }
    }
}
