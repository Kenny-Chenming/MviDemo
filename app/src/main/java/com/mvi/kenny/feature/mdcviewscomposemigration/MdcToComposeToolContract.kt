package com.mvi.kenny.feature.mdcviewscomposemigration

// ================================================================
// MdcToComposeToolContract — MDC-Android Views → Compose 迁移工具包
// ================================================================
// MVI architecture contract for MDC-Android Views → Compose Migration Toolkit.
//
// PRD-283: Android MDC-Views → Compose 迁移工具包
// Design Reference: memory/agency/designs/PRD-283-Android-MDC-Views-Compose迁移工具包.md
//
// MVI 三要素 / Three pillars:
//   Model (State)  — Immutable page state, single source of truth
//   View            — Composable function, consumes State, renders UI
//   Intent          — User intentions, ViewModel processes and updates State
//   Effect          — One-time side effects (navigation, toast), via Channel
//
// This is the "next generation" of PRD-266 MigrationToolkit, with:
//   - 4-Tab navigation: Scanner / Migration / Report / Reference
//   - Enhanced scanning (MDC dependency + XML layout analysis)
//   - Step-by-step migration workflow with progress tracking
//   - Migration report with completion metrics
//   - Searchable component mapping reference
// ================================================================

import android.net.Uri
import androidx.compose.ui.graphics.Color

// ================================================================
// Tab — 底部导航 Tab 枚举
// ================================================================
/**
 * Tab — Bottom navigation tab enumeration.
 * 四 Tab 对应四个主功能模块。
 *
 * @param label Tab display label
 * @param emoji Tab emoji icon
 */
enum class MdcTab(val label: String, val emoji: String) {
    SCANNER("扫描", "🔍"),
    MIGRATION("迁移", "🔄"),
    REPORT("报告", "📊"),
    REFERENCE("参考", "📖")
}

// ================================================================
// Priority — 优先级枚举
// ================================================================
/**
 * Priority — Migration step priority labels and colors.
 *
 * @param label Priority label text
 * @param color Priority color
 */
enum class MdcPriority(
    val label: String,
    val color: Color,
    val emoji: String
) {
    P0("P0", Color(0xFFFF6B6B), "🔴"),
    P1("P1", Color(0xFFFFB347), "🟠"),
    P2("P2", Color(0xFF4CAF50), "🟢")
}

// ================================================================
// ScanState — 扫描状态枚举
// ================================================================
/**
 * ScanState — Scanner tab state enum.
 *
 * @param label Display label
 */
enum class ScanState(val label: String) {
    IDLE("待扫描"),
    RUNNING("扫描中"),
    SUCCESS("扫描完成"),
    ERROR("扫描失败")
}

// ================================================================
// MigrationStepStatus — 迁移步骤状态
// ================================================================
/**
 * MigrationStepStatus — Status of each migration step.
 *
 * @param label Display label
 * @param emoji Status emoji
 */
enum class MigrationStepStatus(val label: String, val emoji: String) {
    NOT_STARTED("未开始", "⏸"),
    IN_PROGRESS("进行中", "🔄"),
    COMPLETED("已完成", "✅")
}

// ================================================================
// MdcToComposeToolState — 页面状态（MVI State）
// ================================================================
/**
 * MdcToComposeToolState — Immutable page state, single source of truth.
 * Contains all state for the 4-tab migration tool.
 *
 * @param selectedTab Currently selected bottom Tab
 * @param scannerState Scanner tab state
 * @param migrationState Migration tab state
 * @param reportState Report tab state
 * @param referenceState Reference tab state
 *
 * @see MdcTab
 * @see ScannerState
 * @see MigrationStep
 * @see MigrationReport
 * @see ComponentMapping
 */
data class MdcToComposeToolState(
    // ── Navigation ────────────────────────────────────────────────
    val selectedTab: MdcTab = MdcTab.SCANNER,

    // ── Scanner State ───────────────────────────────────────────
    val scannerState: ScannerTabState = ScannerTabState(),

    // ── Migration State ─────────────────────────────────────────
    val migrationState: MigrationTabState = MigrationTabState(),

    // ── Report State ────────────────────────────────────────────
    val reportState: ReportTabState = ReportTabState(),

    // ── Reference State ──────────────────────────────────────────
    val referenceState: ReferenceTabState = ReferenceTabState()
) {
    companion object {
        /** Initial / empty state */
        val Initial = MdcToComposeToolState()
    }
}

/**
 * ScannerTabState — Scanner tab state.
 *
 * @param projectPath Input project path string
 * @param scanState Current scan state (Idle/Running/Success/Error)
 * @param scanProgress Scan progress 0.0~1.0
 * @param scanResult Scan result data or null
 * @param selectedFilesCount Number of selected files
 */
data class ScannerTabState(
    val projectPath: String = "",
    val scanState: ScanState = ScanState.IDLE,
    val scanProgress: Float = 0f,
    val scanResult: ScanResult? = null,
    val selectedFilesCount: Int = 0
)

/**
 * MigrationTabState — Migration tab state.
 *
 * @param migrationSteps Ordered list of migration steps
 * @param currentStepIndex Currently selected/active step index
 * @param batchModeEnabled Whether batch auto-migration is enabled
 * @param selectedStepDetail Currently expanded step detail
 */
data class MigrationTabState(
    val migrationSteps: List<MigrationStep> = emptyList(),
    val currentStepIndex: Int = 0,
    val batchModeEnabled: Boolean = false,
    val selectedStepDetail: MigrationStep? = null
)

/**
 * ReportTabState — Report tab state.
 *
 * @param report Generated migration report or null
 * @param completionPercentage Overall migration completion 0-100
 * @param totalComponents Total components count
 * @param migratedComponents Already migrated components count
 */
data class ReportTabState(
    val report: MigrationReport? = null,
    val completionPercentage: Int = 0,
    val totalComponents: Int = 0,
    val migratedComponents: Int = 0
)

/**
 * ReferenceTabState — Reference tab state.
 *
 * @param searchQuery Current search query string
 * @param filteredComponents Filtered component mappings based on search
 */
data class ReferenceTabState(
    val searchQuery: String = "",
    val filteredComponents: List<ComponentMapping> = emptyList()
)

// ================================================================
// MdcToComposeToolIntent — 用户意图（User Intent）
// ================================================================
/**
 * MdcToComposeToolIntent — Every user action corresponds to an Intent.
 * ViewModel receives Intent, processes business logic, then updates State.
 *
 * @see MdcToComposeToolViewModel.sendIntent handles all Intents
 */
sealed interface MdcToComposeToolIntent {

    /** User switches bottom Tab
     * @param tab Target tab
     */
    data class SelectTab(val tab: MdcTab) : MdcToComposeToolIntent

    // ── Scanner Intents ────────────────────────────────────────
    /** User updates project path input
     * @param path Project path string
     */
    data class UpdateProjectPath(val path: String) : MdcToComposeToolIntent

    /** User triggers scan
     * @param path Project path to scan
     */
    data class StartScan(val path: String) : MdcToComposeToolIntent

    // ── Migration Intents ───────────────────────────────────────
    /** User selects a migration step
     * @param index Step index in the list
     */
    data class SelectMigrationStep(val index: Int) : MdcToComposeToolIntent

    /** User toggles batch mode on/off
     * @param enabled Whether batch mode is enabled
     */
    data class ToggleBatchMode(val enabled: Boolean) : MdcToComposeToolIntent

    /** User marks a migration step as completed
     * @param index Step index to mark completed
     */
    data class MarkStepCompleted(val index: Int) : MdcToComposeToolIntent

    // ── Reference Intents ───────────────────────────────────────
    /** User searches component mapping
     * @param query Search query string
     */
    data class SearchComponents(val query: String) : MdcToComposeToolIntent

    /** User copies component mapping note
     * @param mapping Component mapping to copy
     */
    data class CopyMappingNote(val mapping: ComponentMapping) : MdcToComposeToolIntent

    // ── Report Intents ─────────────────────────────────────────
    /** User exports migration report
     * @param format Export format (Markdown/JSON)
     */
    data class ExportReport(val format: ExportFormat) : MdcToComposeToolIntent

    // ── Global ─────────────────────────────────────────────────
    /** User dismisses snackbar / error */
    data object DismissError : MdcToComposeToolIntent
}

/**
 * ExportFormat — Supported export formats for migration report.
 */
enum class ExportFormat(val label: String, val extension: String) {
    MARKDOWN("Markdown", "md"),
    JSON("JSON", "json")
}

// ================================================================
// MdcToComposeToolEffect — 一次性副作用（Effect）
// ================================================================
/**
 * MdcToComposeToolEffect — One-time events, immutable, can only be consumed once.
 * UI layer listens via LaunchedEffect + flow.collect{}.
 *
 * @see MdcToComposeToolViewModel _effect.send() sends Effects
 */
sealed interface MdcToComposeToolEffect {

    /** Show snackbar message
     * @param message Message text
     */
    data class ShowSnackbar(val message: String) : MdcToComposeToolEffect

    /** Copy text to clipboard
     * @param content Text content to copy
     * @param label Label for the clipboard content
     */
    data class CopyToClipboard(val content: String, val label: String) : MdcToComposeToolEffect

    /** Share/export report
     * @param content Report content
     * @param format Export format
     */
    data class ShareReport(val content: String, val format: ExportFormat) : MdcToComposeToolEffect

    /** Show error dialog
     * @param message Error message
     */
    data class ShowError(val message: String) : MdcToComposeToolEffect
}

// ================================================================
// 数据模型 / Data Models
// ================================================================

/**
 * ScanResult — Scan analysis result data class.
 *
 * @param projectName Scanned project name
 * @param moduleCount Number of Gradle modules
 * @param xmlFileCount Number of XML layout files
 * @param mdcVersion MDC-Android dependency version
 * @param componentStats Component usage statistics
 * @param complexityScore Complexity score 1-5 stars
 * @param priorityRecommendations Priority-ordered migration recommendations
 * @param navigationMigrationNeeded Whether Navigation 2→3 migration is needed
 * @param themeMigrationNeeded Whether themes.xml migration is needed
 */
data class ScanResult(
    val projectName: String = "MyProject",
    val moduleCount: Int = 0,
    val xmlFileCount: Int = 0,
    val mdcVersion: String = "1.14.0",
    val componentStats: List<ComponentStat> = emptyList(),
    val complexityScore: Int = 3,
    val priorityRecommendations: List<MigrationRecommendation> = emptyList(),
    val navigationMigrationNeeded: Boolean = false,
    val themeMigrationNeeded: Boolean = false
)

/**
 * ComponentStat — Component usage statistics.
 *
 * @param componentName Component name (e.g., Button, TextView)
 * @param count Usage count in the project
 * @param migrateEffort Migration difficulty (LOW/MEDIUM/HIGH)
 */
data class ComponentStat(
    val componentName: String,
    val count: Int,
    val migrateEffort: MigrateEffort
)

/**
 * MigrateEffort — Migration difficulty level.
 *
 * @param label Difficulty label
 * @param color Difficulty color
 */
enum class MigrateEffort(val label: String, val color: Color) {
    LOW("低", Color(0xFF4CAF50)),
    MEDIUM("中", Color(0xFFFFB347)),
    HIGH("高", Color(0xFFFF6B6B))
}

/**
 * MigrationRecommendation — Priority-ordered migration recommendation.
 *
 * @param priority Priority level (P0/P1/P2)
 * @param title Recommendation title
 * @param description Detailed description
 * @param targetComponents List of component names this recommendation addresses
 * @param estimatedHours Estimated migration hours
 */
data class MigrationRecommendation(
    val priority: MdcPriority,
    val title: String,
    val description: String,
    val targetComponents: List<String>,
    val estimatedHours: Int
)

/**
 * MigrationStep — A single migration step in the migration workflow.
 *
 * @param index Step index (0-based)
 * @param title Step title
 * @param description Step description
 * @param componentType Component type this step addresses
 * @param beforeCode Sample before-migration code
 * @param afterCode Sample after-migration code
 * @param notes Migration notes and tips
 * @param status Current step status
 * @param priority Step priority
 */
data class MigrationStep(
    val index: Int,
    val title: String,
    val description: String,
    val componentType: String,
    val beforeCode: String,
    val afterCode: String,
    val notes: String,
    val status: MigrationStepStatus,
    val priority: MdcPriority
)

/**
 * MigrationReport — Migration progress report.
 *
 * @param generatedAt Report generation timestamp
 * @param totalSteps Total number of migration steps
 * @param completedSteps Number of completed steps
 * @param totalComponents Total components to migrate
 * @param migratedComponents Components already migrated
 * @param skippedSteps Steps skipped by user
 * @param recommendations Remaining recommendations
 */
data class MigrationReport(
    val generatedAt: Long = System.currentTimeMillis(),
    val totalSteps: Int = 0,
    val completedSteps: Int = 0,
    val totalComponents: Int = 0,
    val migratedComponents: Int = 0,
    val skippedSteps: Int = 0,
    val recommendations: List<MigrationRecommendation> = emptyList()
)

/**
 * ComponentMapping — MDC Views → Compose component mapping entry.
 *
 * @param viewsName MDC-Android Views component name
 * @param viewsImport Full import path for Views component
 * @param composeImport Full import path for Compose component
 * @param migrationNote Migration instruction note
 * @param effort Migration difficulty
 */
data class ComponentMapping(
    val viewsName: String,
    val viewsImport: String,
    val composeImport: String,
    val migrationNote: String,
    val effort: MigrateEffort
)

// ================================================================
// 默认数据 / Default & Simulation Data
// ================================================================

/**
 * SIMULATED_SCAN_RESULT — Demo scan result for preview/testing.
 */
val SIMULATED_SCAN_RESULT = ScanResult(
    projectName = "MyAndroidApp",
    moduleCount = 3,
    xmlFileCount = 47,
    mdcVersion = "1.14.0",
    componentStats = listOf(
        ComponentStat("Button", 52, MigrateEffort.LOW),
        ComponentStat("TextView", 124, MigrateEffort.LOW),
        ComponentStat("ImageView", 38, MigrateEffort.LOW),
        ComponentStat("CardView", 18, MigrateEffort.LOW),
        ComponentStat("RecyclerView", 12, MigrateEffort.MEDIUM),
        ComponentStat("ConstraintLayout", 9, MigrateEffort.MEDIUM),
        ComponentStat("LinearLayout", 76, MigrateEffort.MEDIUM),
        ComponentStat("FrameLayout", 15, MigrateEffort.HIGH),
        ComponentStat("FloatingActionButton", 6, MigrateEffort.LOW),
        ComponentStat("TextInputLayout", 14, MigrateEffort.MEDIUM),
        ComponentStat("Toolbar", 8, MigrateEffort.LOW),
        ComponentStat("TabLayout", 4, MigrateEffort.MEDIUM),
        ComponentStat("ViewPager2", 3, MigrateEffort.HIGH),
        ComponentStat("NestedScrollView", 7, MigrateEffort.MEDIUM)
    ),
    complexityScore = 3,
    priorityRecommendations = listOf(
        MigrationRecommendation(
            priority = MdcPriority.P0,
            title = "优先迁移独立组件",
            description = "先迁移不含嵌套的 Button、TextView、CardView、FAB，降低风险",
            targetComponents = listOf("Button", "TextView", "CardView", "FloatingActionButton"),
            estimatedHours = 8
        ),
        MigrationRecommendation(
            priority = MdcPriority.P0,
            title = "RecyclerView → LazyColumn/Grid",
            description = "RecyclerView 是最常用的列表组件，建议尽早迁移",
            targetComponents = listOf("RecyclerView"),
            estimatedHours = 24
        ),
        MigrationRecommendation(
            priority = MdcPriority.P1,
            title = "ConstraintLayout → Compose ConstraintLayout",
            description = "ConstraintLayout 嵌套深的页面优先处理",
            targetComponents = listOf("ConstraintLayout"),
            estimatedHours = 16
        ),
        MigrationRecommendation(
            priority = MdcPriority.P1,
            title = "ViewPager2 → HorizontalPager",
            description = "ViewPager2 迁移到 Compose 需要使用 accompanist pager 或官方 HorizontalPager",
            targetComponents = listOf("ViewPager2"),
            estimatedHours = 12
        ),
        MigrationRecommendation(
            priority = MdcPriority.P2,
            title = "复杂嵌套布局最后处理",
            description = "FrameLayout + include 标签的混合嵌套布局建议最后处理",
            targetComponents = listOf("FrameLayout", "NestedScrollView"),
            estimatedHours = 32
        )
    ),
    navigationMigrationNeeded = true,
    themeMigrationNeeded = true
)

/**
 * SIMULATED_MIGRATION_STEPS — Demo migration steps for preview/testing.
 */
val SIMULATED_MIGRATION_STEPS = listOf(
    MigrationStep(
        index = 0,
        title = "Button → Button",
        description = "迁移 Material Button 到 Compose Button，onClick 属性保持不变",
        componentType = "Button",
        beforeCode = """<Button
    android:id="@+id/btn_submit"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:text="Submit"
    app:backgroundTint="@color/primary" />""",
        afterCode = """@Composable
fun SubmitButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier.fillMaxWidth()
    ) {
        Text("Submit")
    }
}""",
        notes = "• onClick 属性迁移无变化\n• backgroundTint → ButtonDefaults.buttonColors()\n• 移除 app:backgroundTint 使用 Compose color",
        status = MigrationStepStatus.NOT_STARTED,
        priority = MdcPriority.P0
    ),
    MigrationStep(
        index = 1,
        title = "TextView → Text",
        description = "迁移 TextView 到 Compose Text，支持样式和主题集成",
        componentType = "TextView",
        beforeCode = """<TextView
    android:id="@+id/tv_title"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:text="Hello World"
    android:textSize="24sp"
    android:textColor="@color/primary" />""",
        afterCode = """@Composable
fun TitleText(
    text: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = text,
        modifier = modifier,
        fontSize = 24.sp,
        color = MaterialTheme.colorScheme.primary
    )
}""",
        notes = "• android:text → Compose Text content\n• android:textColor 推荐使用 MaterialTheme.colorScheme\n• android:textSize → fontSize = X.sp",
        status = MigrationStepStatus.NOT_STARTED,
        priority = MdcPriority.P0
    ),
    MigrationStep(
        index = 2,
        title = "CardView → Card",
        description = "迁移 CardView 到 Compose Card，使用 tonalElevation 替代 elevation",
        componentType = "CardView",
        beforeCode = """<com.google.android.material.card.MaterialCardView
    android:id="@+id/card_item"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    app:cardCornerRadius="8dp"
    app:cardElevation="4dp">""",
        afterCode = """@Composable
fun ItemCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        tonalElevation = 4.dp
    ) {
        Column(content = content)
    }
}""",
        notes = "• app:cardCornerRadius → shape = RoundedCornerShape()\n• app:cardElevation → tonalElevation\n• Card content 使用 Column/Row 替代嵌套 LinearLayout",
        status = MigrationStepStatus.NOT_STARTED,
        priority = MdcPriority.P0
    ),
    MigrationStep(
        index = 3,
        title = "RecyclerView → LazyColumn",
        description = "迁移 RecyclerView 到 Compose LazyColumn，消除 Adapter 和 ViewHolder",
        componentType = "RecyclerView",
        beforeCode = """<androidx.recyclerview.widget.RecyclerView
    android:id="@+id/rv_list"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    app:layoutManager="LinearLayoutManager" />""",
        afterCode = """@Composable
fun ItemList(
    items: List<Item>,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize()
    ) {
        items(items) { item ->
            ItemRow(item = item)
        }
    }
}""",
        notes = "• RecyclerView.Adapter → LazyListScope items{}\n• ViewHolder → item {} composable function\n• LayoutManager 不再需要，orientation 由 LazyColumn/LazyRow 控制",
        status = MigrationStepStatus.NOT_STARTED,
        priority = MdcPriority.P0
    ),
    MigrationStep(
        index = 4,
        title = "LinearLayout → Column/Row",
        description = "迁移 LinearLayout 到 Compose Column/Row，使用 Arrangement 替代 weight",
        componentType = "LinearLayout",
        beforeCode = """<LinearLayout
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:orientation="vertical"
    android:padding="16dp">

    <TextView
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="Title" />

    <Button
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="Click" />

</LinearLayout>""",
        afterCode = """@Composable
fun ContentSection(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text("Title")
        Button(onClick = { /* ... */ }) {
            Text("Click")
        }
    }
}""",
        notes = "• android:orientation=vertical → Column\n• android:orientation=horizontal → Row\n• android:weight → Modifier.weight() in Row/Column\n• spacing/arrangement 替代 margin between children",
        status = MigrationStepStatus.NOT_STARTED,
        priority = MdcPriority.P1
    ),
    MigrationStep(
        index = 5,
        title = "FloatingActionButton → FloatingActionButton",
        description = "迁移 FAB 到 Compose FloatingActionButton，属性基本一致",
        componentType = "FloatingActionButton",
        beforeCode = """<com.google.android.material.floatingactionbutton.FloatingActionButton
    android:id="@+id/fab_add"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:contentDescription="Add"
    app:fabSize="normal"
    app:tint="@color/white" />""",
        afterCode = """@Composable
fun AddFab(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FloatingActionButton(
        onClick = onClick,
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.primaryContainer
    ) {
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = "Add"
        )
    }
}""",
        notes = "• android:contentDescription → Icon contentDescription\n• app:fabSize → size (FABSize in M3)\n• app:tint → tint at Icon level or containerColor",
        status = MigrationStepStatus.NOT_STARTED,
        priority = MdcPriority.P0
    ),
    MigrationStep(
        index = 6,
        title = "Toolbar → TopAppBar",
        description = "迁移 Toolbar 到 Compose TopAppBar，menu 迁移到 actions",
        componentType = "Toolbar",
        beforeCode = """<androidx.appcompat.widget.Toolbar
    android:id="@+id/toolbar"
    android:layout_width="match_parent"
    android:layout_height="?attr/actionBarSize"
    android:background="?attr/colorPrimary">

    <TextView
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="Title" />

</androidx.appcompat.widget.Toolbar>""",
        afterCode = """@Composable
fun AppTopBar(
    title: String,
    modifier: Modifier = Modifier,
    navigationIcon: @Composable (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {}
) {
    TopAppBar(
        title = { Text(title) },
        modifier = modifier,
        navigationIcon = navigationIcon,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        actions = actions
    )
}""",
        notes = "• android:title → title = { Text() }\n• onMenuItemClick → actions = { IconButton {} }\n• setNavigationOnClick → navigationIcon composable",
        status = MigrationStepStatus.NOT_STARTED,
        priority = MdcPriority.P1
    ),
    MigrationStep(
        index = 7,
        title = "TabLayout + ViewPager2 → TabRow + HorizontalPager",
        description = "迁移 TabLayout + ViewPager2 到 Compose TabRow + HorizontalPager",
        componentType = "TabLayout + ViewPager2",
        beforeCode = """<com.google.android.material.tabs.TabLayout
    android:id="@+id/tab_layout"
    android:layout_width="match_parent"
    android:layout_height="wrap_content" />

<androidx.viewpager2.widget.ViewPager2
    android:id="@+id/view_pager"
    android:layout_width="match_parent"
    android:layout_height="match_parent" />""",
        afterCode = """@Composable
fun TabbedScreen(
    pagerState: PagerState = rememberPagerState(pageCount = { 3 })
) {
    val tabs = listOf("Tab 1", "Tab 2", "Tab 3")

    Column {
        TabRow(selectedTabIndex = pagerState.currentPage) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = pagerState.currentPage == index,
                    onClick = { /* select tab */ },
                    text = { Text(title) }
                )
            }
        }

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            // Page content
        }
    }
}""",
        notes = "• TabLayout.Tab → TabRow Tab composable\n• ViewPager2 + FragmentStateAdapter → HorizontalPager + PagerState\n• PagerState manages selected page and scroll position",
        status = MigrationStepStatus.NOT_STARTED,
        priority = MdcPriority.P1
    )
)

/**
 * SIMULATED_COMPONENT_MAPPINGS — Component mapping table data.
 */
val SIMULATED_COMPONENT_MAPPINGS = listOf(
    ComponentMapping("Button", "android.widget.Button", "androidx.compose.material3.Button", "onClick → onClick, text → content", MigrateEffort.LOW),
    ComponentMapping("TextView", "android.widget.TextView", "androidx.compose.foundation.text.BasicText", "android:text → Text content", MigrateEffort.LOW),
    ComponentMapping("ImageView", "android.widget.ImageView", "androidx.compose.foundation.Image", "android:src → painter/imageResource", MigrateEffort.LOW),
    ComponentMapping("CardView", "androidx.cardview.widget.CardView", "androidx.compose.material3.Card", "cardCornerRadius → shape, elevation → tonalElevation", MigrateEffort.LOW),
    ComponentMapping("RecyclerView", "androidx.recyclerview.widget.RecyclerView", "androidx.compose.foundation.lazy.LazyColumn / LazyRow", "Adapter → LazyListScope, ViewHolder → item {}", MigrateEffort.MEDIUM),
    ComponentMapping("ConstraintLayout", "androidx.constraintlayout.widget.ConstraintLayout", "androidx.compose.foundation.layout.Box / Column / Row", "app:layout_constraintXxx → Modifier.align()", MigrateEffort.MEDIUM),
    ComponentMapping("LinearLayout", "android.widget.LinearLayout", "androidx.compose.foundation.layout.Column / Row", "orientation → Column/Row, weight → weight Modifier", MigrateEffort.MEDIUM),
    ComponentMapping("FrameLayout", "android.widget.FrameLayout", "androidx.compose.foundation.layout.Box", "FrameLayout → Box for stacking", MigrateEffort.HIGH),
    ComponentMapping("FloatingActionButton", "com.google.android.material.floatingactionbutton.FloatingActionButton", "androidx.compose.material3.FloatingActionButton", "fabSize → size, onClick unchanged", MigrateEffort.LOW),
    ComponentMapping("TextInputLayout", "com.google.android.material.textfield.TextInputLayout", "androidx.compose.material3.OutlinedTextField", "hint → placeholder", MigrateEffort.MEDIUM),
    ComponentMapping("Toolbar", "androidx.appcompat.widget.Toolbar", "androidx.compose.material3.TopAppBar", "android:title → title, onMenuItemClick → actions", MigrateEffort.LOW),
    ComponentMapping("TabLayout", "com.google.android.material.tabs.TabLayout", "androidx.compose.material3.TabRow", "addTab → tabs {}, TabLayout.Tab → Tab", MigrateEffort.MEDIUM),
    ComponentMapping("ViewPager2", "androidx.viewpager2.widget.ViewPager2", "androidx.compose.foundation.pager.HorizontalPager", "ViewPager2 + FragmentStateAdapter → HorizontalPager + PagerState", MigrateEffort.HIGH),
    ComponentMapping("SwipeRefreshLayout", "androidx.swiperefreshlayout.widget.SwipeRefreshLayout", "androidx.compose.material3.pullrefresh.PullRefreshIndicator", "SwipeRefreshLayout → Box + pullRefresh Modifier", MigrateEffort.HIGH),
    ComponentMapping("NestedScrollView", "androidx.core.widget.NestedScrollView", "androidx.compose.foundation.rememberScrollState + Column", "NestedScrollView → Column(Modifier.verticalScroll(scrollState))", MigrateEffort.MEDIUM),
    ComponentMapping("RadioButton", "android.widget.RadioButton", "androidx.compose.material3.RadioButton", "android:checked → selected, onCheckedChange → onClick", MigrateEffort.LOW),
    ComponentMapping("CheckBox", "android.widget.CheckBox", "androidx.compose.material3.Checkbox", "android:checked → checked, onCheckedChange unchanged", MigrateEffort.LOW),
    ComponentMapping("Switch", "android.widget.Switch", "androidx.compose.material3.Switch", "android:checked → checked, onCheckedChange unchanged", MigrateEffort.LOW),
    ComponentMapping("ProgressBar", "android.widget.ProgressBar", "androidx.compose.material3.LinearProgressIndicator / CircularProgressIndicator", "style=horizontal → LinearProgressIndicator", MigrateEffort.LOW),
    ComponentMapping("SeekBar", "android.widget.SeekBar", "androidx.compose.material3.Slider", "SeekBar → Slider, OnSeekBarChangeListener → onValueChange", MigrateEffort.MEDIUM)
)
