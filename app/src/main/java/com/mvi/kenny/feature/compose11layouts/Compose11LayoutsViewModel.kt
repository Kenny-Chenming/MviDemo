package com.mvi.kenny.feature.compose11layouts

// ============================================================
// Compose11LayoutsViewModel — PRD-161 ViewModel
// Compose 1.11 Layout & Style APIs 开发工具包
// ============================================================
/**
 * MVI ViewModel — 接收 Intent，处理业务逻辑，输出 State + Effect。
 *
 * @see Compose11LayoutsContract
 * @see Compose11LayoutsScreen
 */

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class Compose11LayoutsViewModel : ViewModel() {

    // ============================================================
    // State — 页面状态（唯一真相来源）
    // ============================================================
    private val _state = MutableStateFlow(Compose11LayoutsState.Initial)
    val state: StateFlow<Compose11LayoutsState> = _state.asStateFlow()

    // ============================================================
    // Effect — 副作用 Channel（一次性事件）
    // ============================================================
    private val _effect = Channel<Compose11LayoutsEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    // ============================================================
    // 初始化 — 加载默认数据
    // ============================================================
    init {
        loadInitialData()
    }

    /**
     * 加载初始数据：模板库、指南章节、MediaQuery 模板。
     */
    private fun loadInitialData() {
        _state.update {
            it.copy(
                gridTemplates = getDefaultGridTemplates(),
                flexboxTemplates = getDefaultFlexBoxTemplates(),
                styleGuideSections = getDefaultStyleGuideSections(),
                mediaQueryTemplates = getDefaultMediaQueryTemplates()
            )
        }
    }

    // ============================================================
    // Intent 处理 — sendIntent()
    // ============================================================
    fun sendIntent(intent: Compose11LayoutsIntent) {
        when (intent) {
            is Compose11LayoutsIntent.SelectSection -> handleSelectSection(intent.section)
            is Compose11LayoutsIntent.SelectTab -> handleSelectTab(intent.tab)
            is Compose11LayoutsIntent.ChangeBomVersion -> handleChangeBomVersion(intent.version)
            is Compose11LayoutsIntent.SelectTemplate -> handleSelectTemplate(intent.template)
            is Compose11LayoutsIntent.UpdatePreviewParameter -> handleUpdatePreviewParameter(intent.key, intent.value)
            is Compose11LayoutsIntent.TogglePreviewDialog -> handleTogglePreviewDialog()
            is Compose11LayoutsIntent.ClosePreviewDialog -> handleClosePreviewDialog()
            is Compose11LayoutsIntent.UpdateProjectPath -> handleUpdateProjectPath(intent.path)
            is Compose11LayoutsIntent.StartComplianceScan -> handleStartComplianceScan(intent.projectPath)
            is Compose11LayoutsIntent.CopyTemplateCode -> handleCopyTemplateCode(intent.templateId)
            is Compose11LayoutsIntent.ExportComplianceReport -> handleExportReport(intent.format)
            is Compose11LayoutsIntent.RunVerification -> handleRunVerification()
            is Compose11LayoutsIntent.ToggleDarkMode -> handleToggleDarkMode()
            is Compose11LayoutsIntent.ToggleStyleSection -> handleToggleStyleSection(intent.sectionId)
            is Compose11LayoutsIntent.SelectScreenMode -> handleSelectScreenMode(intent.mode)
            is Compose11LayoutsIntent.DismissError -> handleDismissError()
        }
    }

    // ============================================================
    // Intent Handlers — 业务逻辑处理
    // ============================================================

    private fun handleSelectSection(section: LayoutSection) {
        _state.update {
            it.copy(
                selectedSection = section,
                selectedTab = if (section == LayoutSection.GRID || section == LayoutSection.FLEXBOX) {
                    LayoutTab.GUIDE
                } else {
                    it.selectedTab
                },
                isPreviewDialogOpen = false,
                selectedTemplate = null
            )
        }
    }

    private fun handleSelectTab(tab: LayoutTab) {
        _state.update { it.copy(selectedTab = tab) }
    }

    private fun handleChangeBomVersion(version: String) {
        _state.update { it.copy(currentComposeBom = version) }
    }

    private fun handleSelectTemplate(template: Any) {
        _state.update {
            it.copy(
                selectedTemplate = template,
                isPreviewDialogOpen = true
            )
        }
    }

    private fun handleUpdatePreviewParameter(key: String, value: Any) {
        _state.update {
            it.copy(previewParameters = it.previewParameters + (key to value))
        }
        viewModelScope.launch {
            delay(300) // Debounce preview refresh
        }
    }

    private fun handleTogglePreviewDialog() {
        _state.update { it.copy(isPreviewDialogOpen = !it.isPreviewDialogOpen) }
    }

    private fun handleClosePreviewDialog() {
        _state.update {
            it.copy(
                isPreviewDialogOpen = false,
                selectedTemplate = null,
                previewParameters = emptyMap()
            )
        }
    }

    private fun handleUpdateProjectPath(path: String) {
        _state.update { it.copy(projectPathInput = path) }
    }

    private fun handleStartComplianceScan(projectPath: String) {
        if (projectPath.isBlank()) {
            sendEffect(Compose11LayoutsEffect.ShowError("请输入项目路径 / Please enter a project path"))
            return
        }

        viewModelScope.launch {
            _state.update {
                it.copy(
                    scanInProgress = true,
                    scanProgress = 0f,
                    currentScanFile = null,
                    complianceResults = emptyList()
                )
            }

            val files = listOf(
                "MainActivity.kt",
                "HomeScreen.kt",
                "DashboardScreen.kt",
                "ProfileScreen.kt",
                "SettingsFragment.kt"
            )

            files.forEachIndexed { index, file ->
                delay(400)
                _state.update {
                    it.copy(
                        scanProgress = (index + 1).toFloat() / files.size,
                        currentScanFile = file
                    )
                }
            }

            val results = listOf(
                ComplianceResult(
                    id = "c1",
                    severity = ComplianceSeverity.ERROR,
                    title = "使用旧版 Box 而非 Grid API",
                    description = "检测到在 DashboardScreen.kt 中使用 Box 实现网格布局，建议迁移至 Grid API 以获得更好的性能。",
                    filePath = "$projectPath/DashboardScreen.kt",
                    lineNumber = 42,
                    suggestion = "将 Box 替换为 Grid API，使用 GridItemSpan 进行跨单元格布局。"
                ),
                ComplianceResult(
                    id = "c2",
                    severity = ComplianceSeverity.WARN,
                    title = "LazyVerticalGrid 缺少跨单元格处理",
                    description = "LazyVerticalGrid 疑似用于静态布局场景，建议评估是否应使用 Grid API。",
                    filePath = "$projectPath/ListScreen.kt",
                    lineNumber = 87,
                    suggestion = "如果列表项数量固定且不需要懒加载，考虑迁移到 Grid API。"
                ),
                ComplianceResult(
                    id = "c3",
                    severity = ComplianceSeverity.INFO,
                    title = "Style API 实验性标记缺失",
                    description = "Style API 在 Compose 1.11 中为实验性 API，需要添加 @OptIn 注解。",
                    filePath = "$projectPath/Theme.kt",
                    lineNumber = 15,
                    suggestion = "在文件顶部添加 @OptIn(ExperimentalLayoutApi::class) 注解。"
                )
            )

            _state.update {
                it.copy(
                    scanInProgress = false,
                    scanProgress = 1f,
                    currentScanFile = null,
                    complianceResults = results
                )
            }
            sendEffect(Compose11LayoutsEffect.ScanComplete)
        }
    }

    private fun handleCopyTemplateCode(templateId: String) {
        viewModelScope.launch {
            sendEffect(Compose11LayoutsEffect.CodeCopied)
            sendEffect(Compose11LayoutsEffect.ShowToast("代码已复制 / Code copied to clipboard"))
        }
    }

    private fun handleExportReport(format: ReportFormat) {
        viewModelScope.launch {
            val results = _state.value.complianceResults
            val content = buildString {
                appendLine("# Compose 1.11 布局 API 合规报告")
                appendLine("## 扫描概要")
                appendLine("- 总违规项: ${results.size}")
                appendLine("- 错误: ${results.count { it.severity == ComplianceSeverity.ERROR }}")
                appendLine("- 警告: ${results.count { it.severity == ComplianceSeverity.WARN }}")
                appendLine("- 信息: ${results.count { it.severity == ComplianceSeverity.INFO }}")
                appendLine()
                appendLine("## 违规详情")
                results.forEach { result ->
                    appendLine("### [${result.severity.label}] ${result.title}")
                    appendLine("- 文件: ${result.filePath}")
                    result.lineNumber?.let { appendLine("- 行号: $it") }
                    appendLine("- 描述: ${result.description}")
                    appendLine("- 建议: ${result.suggestion}")
                    appendLine()
                }
            }
            sendEffect(Compose11LayoutsEffect.ShareReport(content, format))
        }
    }

    private fun handleRunVerification() {
        viewModelScope.launch {
            _state.update { it.copy(verificationInProgress = true) }
            delay(1500)

            val result = VerificationResult(
                currentBom = _state.value.currentComposeBom,
                latestBom = "2024.02.00",
                dependencyTree = listOf(
                    DependencyNode(
                        name = "androidx.compose.material3",
                        version = "1.1.2",
                        latestVersion = "1.2.0",
                        children = listOf(
                            DependencyNode("androidx.compose.foundation", "1.5.4", "1.6.0"),
                            DependencyNode("androidx.compose.ui", "1.5.4", "1.6.0")
                        )
                    ),
                    DependencyNode(
                        name = "androidx.compose.ui-tooling",
                        version = "1.5.4",
                        latestVersion = "1.6.0"
                    )
                ),
                suggestions = listOf(
                    "将 compose-bom 从 ${_state.value.currentComposeBom} 升级至 2024.02.00",
                    "material3: 1.1.2 → 1.2.0（新增 Grid/FlexBox 支持）",
                    "建议在升级前运行 ./gradlew dependencies --configuration releaseRuntimeClasspath"
                ),
                isCompliant = false
            )

            _state.update {
                it.copy(
                    verificationInProgress = false,
                    verificationResult = result
                )
            }
        }
    }

    private fun handleToggleDarkMode() {
        _state.update { it.copy(isDarkMode = !it.isDarkMode) }
    }

    private fun handleToggleStyleSection(sectionId: String) {
        _state.update {
            val expanded = it.expandedStyleSections.toMutableSet()
            if (expanded.contains(sectionId)) {
                expanded.remove(sectionId)
            } else {
                expanded.add(sectionId)
            }
            it.copy(expandedStyleSections = expanded)
        }
    }

    private fun handleSelectScreenMode(mode: ScreenMode) {
        _state.update { it.copy(selectedScreenMode = mode) }
    }

    private fun handleDismissError() {
        _state.update { it.copy(errorMessage = null) }
    }

    private fun sendEffect(effect: Compose11LayoutsEffect) {
        viewModelScope.launch {
            _effect.send(effect)
        }
    }

    // ============================================================
    // 默认数据 — Grid 模板库
    // ============================================================
    private fun getDefaultGridTemplates(): List<GridTemplate> = listOf(
        GridTemplate(
            id = "grid_dashboard_1",
            nameCn = "仪表盘主视图",
            nameEn = "Dashboard Main Grid",
            description = "4 列等宽网格，适合 KPI 卡片展示",
            category = GridCategory.DASHBOARD,
            code = """
@Composable
fun DashboardGrid() {
    Grid(
        columns = GridCells.Fixed(4),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item(GridItemSpan(1)) { KpiCard(title = "DAU", value = "12.5K") }
        item(GridItemSpan(1)) { KpiCard(title = "收入", value = "¥8.2万") }
        item(GridItemSpan(2)) { RevenueChart() }
        item(GridItemSpan(2)) { UserGrowthChart() }
        item(GridItemSpan(2)) { ActivityFeed() }
    }
}
            """.trimIndent(),
            parameters = listOf(
                TemplateParameter("columns", "列数", "int", 4, 2f, 6f),
                TemplateParameter("hGap", "横向间距", "float", 12f, 4f, 24f),
                TemplateParameter("vGap", "纵向间距", "float", 12f, 4f, 24f)
            )
        ),
        GridTemplate(
            id = "grid_calendar_1",
            nameCn = "日历网格视图",
            nameEn = "Calendar Grid",
            description = "7 列日历网格，支持跨行日程显示",
            category = GridCategory.CALENDAR,
            code = """
@Composable
fun CalendarGrid(year: Int, month: Int, events: List<CalendarEvent>) {
    Grid(
        columns = GridCells.Fixed(7), // Sun-Sat
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // Day headers
        listOf("日","一","二","三","四","五","六").forEach { day ->
            item(GridItemSpan(1)) { Text(day, style = MaterialTheme.typography.labelSmall) }
        }
        // Date cells
        val firstDayOfMonth = LocalDate.of(year, month, 1)
        val daysInMonth = firstDayOfMonth.lengthOfMonth()
        val startOffset = firstDayOfMonth.dayOfWeek.value % 7
        repeat(startOffset) { item(GridItemSpan(1)) { Box {} } }
        for (day in 1..daysInMonth) {
            item(GridItemSpan(1)) {
                CalendarDayCell(day = day, events = events.filter { it.day == day })
            }
        }
    }
}
            """.trimIndent(),
            parameters = listOf(
                TemplateParameter("columns", "列数", "int", 7, 5f, 10f),
                TemplateParameter("hGap", "横向间距", "float", 4f, 2f, 16f)
            )
        ),
        GridTemplate(
            id = "grid_chessboard_1",
            nameCn = "棋盘网格",
            nameEn = "Chessboard Grid",
            description = "8×8 交替色棋盘格模板",
            category = GridCategory.CHESSBOARD,
            code = """
@Composable
fun ChessboardGrid(boardSize: Int = 8, cellSize: Dp = 48.dp) {
    Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
        repeat(boardSize) { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(0.dp)) {
                repeat(boardSize) { col ->
                    val isBlack = (row + col) % 2 == 1
                    Box(
                        modifier = Modifier
                            .size(cellSize)
                            .background(if (isBlack) Color.Black else Color.White)
                    )
                }
            }
        }
    }
}
            """.trimIndent(),
            parameters = listOf(
                TemplateParameter("boardSize", "棋盘大小", "int", 8, 4f, 12f),
                TemplateParameter("cellSize", "格子大小(dp)", "float", 48f, 24f, 80f)
            )
        ),
        GridTemplate(
            id = "grid_gallery_1",
            nameCn = "瀑布流画廊",
            nameEn = "Masonry Gallery",
            description = "自适应高度瀑布流，支持跨列图片",
            category = GridCategory.GALLERY,
            code = """
@Composable
fun MasonryGallery(images: List<GalleryImage>) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        images.chunked(3).forEach { rowImages ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                rowImages.forEach { image ->
                    AsyncImage(
                        model = image.url,
                        contentDescription = null,
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(image.aspectRatio),
                        contentScale = ContentScale.Crop
                    )
                }
                // Fill empty slots
                repeat(3 - rowImages.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}
            """.trimIndent(),
            parameters = listOf(
                TemplateParameter("columns", "列数", "int", 3, 2f, 5f),
                TemplateParameter("hGap", "横向间距", "float", 8f, 4f, 16f),
                TemplateParameter("vGap", "纵向间距", "float", 8f, 4f, 16f)
            )
        )
    )

    // ============================================================
    // 默认数据 — FlexBox 模板库
    // ============================================================
    private fun getDefaultFlexBoxTemplates(): List<FlexBoxTemplate> = listOf(
        FlexBoxTemplate(
            id = "flex_navbar_1",
            nameCn = "底部导航栏",
            nameEn = "Bottom Navigation Bar",
            description = "水平均匀分布的底部 Tab 导航",
            category = FlexBoxCategory.NAVBAR,
            code = """
@Composable
fun BottomNavBar(items: List<NavItem>, selectedIndex: Int, onItemSelected: (Int) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        items.forEachIndexed { index, item ->
            NavBarItem(
                item = item,
                isSelected = index == selectedIndex,
                onClick = { onItemSelected(index) }
            )
        }
    }
}
            """.trimIndent(),
            parameters = listOf(
                TemplateParameter("itemCount", "Tab 数量", "int", 4, 2f, 6f)
            )
        ),
        FlexBoxTemplate(
            id = "flex_sidebar_1",
            nameCn = "侧边栏导航",
            nameEn = "Sidebar Navigation",
            description = "垂直 FlexBox 侧边栏，支持展开/折叠",
            category = FlexBoxCategory.SIDEBAR,
            code = """
@Composable
fun SidebarNavigation(
    sections: List<SidebarSection>,
    expandedSectionIds: Set<String>,
    onSectionToggle: (String) -> Unit
) {
    Column(
        modifier = Modifier.width(240.dp).fillMaxHeight(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        sections.forEach { section ->
            SidebarSectionItem(
                section = section,
                isExpanded = expandedSectionIds.contains(section.id),
                onToggle = { onSectionToggle(section.id) }
            )
        }
    }
}
            """.trimIndent(),
            parameters = listOf(
                TemplateParameter("sidebarWidth", "侧边栏宽度(dp)", "float", 240f, 180f, 320f)
            )
        ),
        FlexBoxTemplate(
            id = "flex_cardlist_1",
            nameCn = "卡片列表",
            nameEn = "Card List",
            description = "响应式卡片列表，跨列显示重点卡片",
            category = FlexBoxCategory.CARD_LIST,
            code = """
@Composable
fun CardList(items: List<ListItem>) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        items.forEach { item ->
            if (item.isFeatured) {
                FeaturedCard(item = item)
            } else {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    item.images.take(2).forEach { image ->
                        ThumbnailCard(image = image)
                    }
                    DescriptionCard(item = item)
                }
            }
        }
    }
}
            """.trimIndent(),
            parameters = listOf(
                TemplateParameter("spacing", "间距", "float", 12f, 4f, 24f)
            )
        ),
        FlexBoxTemplate(
            id = "flex_flow_1",
            nameCn = "流式标签布局",
            nameEn = "Flow Tags Layout",
            description = "自动换行的 FlexBox 流式布局，适合标签云",
            category = FlexBoxCategory.FLOW,
            code = """
@Composable
fun FlowTagsLayout(tags: List<String>) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        val rows = remember(tags) { tags.chunked(4) }
        rows.forEach { row ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                row.forEach { tag ->
                    TagChip(label = tag, modifier = Modifier.weight(1f))
                }
                repeat(4 - row.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}
            """.trimIndent(),
            parameters = listOf(
                TemplateParameter("hGap", "标签间距", "float", 8f, 4f, 16f),
                TemplateParameter("vGap", "行间距", "float", 8f, 4f, 16f)
            )
        )
    )

    // ============================================================
    // 默认数据 — Style API 指南章节
    // ============================================================
    private fun getDefaultStyleGuideSections(): List<StyleGuideSection> = listOf(
        StyleGuideSection(
            id = "style_decision",
            title = "Style API vs Modifier — 何时使用？",
            content = """
## 决策原则

**Style API 适用场景：**
- 需要跨组件共享的视觉样式（主题化）
- 需要动态切换的视觉效果（动画状态）
- 需要组合多个 Modifier 的复用场景
- 涉及过渡动画的样式变更

**Modifier 适用场景：**
- 一次性、组件特定的布局属性
- 不需要复用和动态切换的静态样式
- 单纯的尺寸、padding、background 等基础修饰
            """.trimIndent(),
            codeExample = """
// Style API 示例
val AppTypography = Style {
    +Typography { DisplayLarge }
    +Color { MaterialTheme.colorScheme.primary }
}

// Modifier 示例
Text("Hello", modifier = Modifier.fillMaxWidth().padding(16.dp))
            """.trimIndent()
        ),
        StyleGuideSection(
            id = "style_performance",
            title = "性能考量",
            content = """
## Style API 性能指南

**重组优化原则：**
1. 避免在 Style 内创建新对象
2. 使用 remember 缓存 Style
3. Style 嵌套不要超过 3 层
4. 动画场景优先使用 Style

**性能红牌：**
- 在 Style builder 内调用 expensiveComputation()
- 在 Style 内使用 mutableStateOf()
- 在 recomposition 中动态构建 Style

**性能绿牌：**
- Style 作为 static val 或 top-level 常量
- 使用 StyleRef 引用已有 Style
            """.trimIndent(),
            codeExample = """
val CardStyle = Style {
    +Background(MaterialTheme.colorScheme.surface)
    +Border(1.dp, MaterialTheme.colorScheme.outline)
}

@Composable
fun ThemedCard(content: @Composable () -> Unit) {
    val style = remember { CardStyle }
    Surface(style = style) { content() }
}
            """.trimIndent()
        ),
        StyleGuideSection(
            id = "style_animation",
            title = "动画过渡集成",
            content = """
## Style API 动画集成

**支持的动画 API：**
- animateStyleAsState() — 样式级别动画
- Transition + Style — 复杂状态机动画
- InfiniteTransition + Style — 循环动画

**典型使用场景：**
- 按钮按下状态的视觉反馈
- 主题切换的平滑过渡
- 列表项进入/退出的过渡动画
            """.trimIndent(),
            codeExample = """
@Composable
fun AnimatedButton() {
    var isPressed by remember { mutableStateOf(false) }
    val buttonStyle by animateStyleAsState(
        targetValue = if (isPressed) PressedButtonStyle else DefaultButtonStyle,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "buttonStyle"
    )
    Surface(style = buttonStyle, onClick = { isPressed = !isPressed }) {
        Text("Press Me")
    }
}
            """.trimIndent()
        ),
        StyleGuideSection(
            id = "style_best_practices",
            title = "最佳实践与反模式",
            content = """
## Style API 最佳实践

**DO：**
- ✅ 为应用定义 Design Token
- ✅ 使用 Style 组合构建复合样式
- ✅ 在 Theme 级别声明全局 Styles
- ✅ Style 命名遵循规范

**DON'T：**
- ❌ 为每个组件创建独立 Style
- ❌ 在 Style 内调用 remember/mutableStateOf
- ❌ Style 与 Modifier 混用导致冲突
            """.trimIndent(),
            codeExample = """
object AppStyles {
    val PrimaryButton = Style {
        +Background(AppColors.Primary)
        +Padding(horizontal = 24.dp, vertical = 12.dp)
        +BorderRadius(12.dp)
    }
    val CardBase = Style {
        +Background(AppColors.Surface)
        +Padding(16.dp)
        +BorderRadius(16.dp)
    }
}
            """.trimIndent()
        )
    )

    // ============================================================
    // 默认数据 — MediaQuery 模板
    // ============================================================
    private fun getDefaultMediaQueryTemplates(): List<MediaQueryTemplate> = listOf(
        MediaQueryTemplate(
            id = "mq_foldable_tabletop",
            nameCn = "折叠屏桌面模式",
            nameEn = "Foldable Tabletop Mode",
            description = "折叠屏展开至 tabletop mode，上下分区布局",
            screenMode = ScreenMode.TOPIC_TABLE_MODE,
            code = """
@Composable
fun FoldableTabletopTemplate() {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        // WindowPosture API 检测（需要 foldingFeature）
        val isTabletop = currentWindowPosture() is WindowPosture.Tabletop

        if (isTabletop) {
            Column(modifier = Modifier.fillMaxWidth().weight(1f).padding(16.dp)) {
                Text("上半屏 — 主要内容", style = MaterialTheme.typography.HeadlineMedium)
                Spacer(Modifier.height(8.dp))
                VideoPlayer()
            }
            Column(
                modifier = Modifier.fillMaxWidth().weight(1f).padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("下半屏 — 交互控制", style = MaterialTheme.typography.TitleMedium)
                PlaybackControls()
            }
        } else {
            Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                VideoPlayer()
                PlaybackControls()
            }
        }
    }
}
            """.trimIndent()
        ),
        MediaQueryTemplate(
            id = "mq_tablet_master_detail",
            nameCn = "平板 Master-Detail",
            nameEn = "Tablet Master-Detail Layout",
            description = "大屏平板双栏布局，左侧列表右侧详情",
            screenMode = ScreenMode.TABLET,
            code = """
@Composable
fun MasterDetailTemplate(
    items: List<Item>,
    selectedItem: Item?,
    onItemSelect: (Item) -> Unit
) {
    if (WindowWidthSizeClass.Expanded) {
        // 平板：大屏双栏
        Row(modifier = Modifier.fillMaxSize()) {
            MasterPane(
                items = items, selectedItem = selectedItem,
                onItemSelect = onItemSelect,
                modifier = Modifier.width(320.dp)
            )
            DetailPane(item = selectedItem, modifier = Modifier.weight(1f))
        }
    } else {
        // 手机：堆叠
        if (selectedItem != null) {
            DetailPane(item = selectedItem, modifier = Modifier.fillMaxSize())
        } else {
            MasterPane(items, null, onItemSelect)
        }
    }
}
            """.trimIndent()
        ),
        MediaQueryTemplate(
            id = "mq_phone_adaptive",
            nameCn = "手机自适应模板",
            nameEn = "Phone Adaptive Layout",
            description = "手机屏幕自适应布局，竖屏/横屏不同排版",
            screenMode = ScreenMode.PHONE,
            code = """
@Composable
fun PhoneAdaptiveTemplate() {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isLandscape = maxWidth > maxHeight

        if (isLandscape) {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Column(modifier = Modifier.weight(0.6f).padding(16.dp)) { PrimaryContent() }
                Column(modifier = Modifier.weight(0.4f).padding(16.dp)) { SecondaryContent() }
            }
        } else {
            Column(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                PrimaryContent()
                SecondaryContent()
            }
        }
    }
}
            """.trimIndent()
        ),
        MediaQueryTemplate(
            id = "mq_foldable_dual_screen",
            nameCn = "折叠屏双屏模板",
            nameEn = "Foldable Dual Screen",
            description = "折叠屏双屏模式，左右分屏独立渲染",
            screenMode = ScreenMode.FOLDABLE,
            code = """
@Composable
fun DualScreenTemplate(foldingFeature: FoldingFeature) {
    if (foldingFeature.state == FoldingFeature.State.HALF_OPENED) {
        Row(modifier = Modifier.fillMaxSize()) {
            Surface(modifier = Modifier.weight(1f).fillMaxHeight()) {
                LeftScreenContent()
            }
            Spacer(
                modifier = Modifier
                    .width(foldingFeature.bounds.width.toDp().dp)
                    .fillMaxHeight()
                    .background(Color.Black)
            )
            Surface(modifier = Modifier.weight(1f).fillMaxHeight()) {
                RightScreenContent()
            }
        }
    } else {
        FullScreenContent()
    }
}
            """.trimIndent()
        )
    )
}
