package com.mvi.kenny.feature.kotlinpausablecompositiontool

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState

import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Badge
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mvi.kenny.base.TopBarConfig
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * ============================================================
 * KotlinPausableCompositionScreen — Kotlin 2.2 + Pausable Composition Tool Screen
 * ============================================================
 * Main screen implementing a 5-tab developer toolkit for Kotlin 2.2 Context Parameters
 * and Jetpack Compose Pausable Composition.
 *
 * 5 Tabs:
 * 1. Kotlin 2.2 Context Parameters 深度指南
 * 2. Compose Pausable Composition 开发者指南
 * 3. Pausable Composition 性能调优工具
 * 4. Context Parameters × Compose 集成指南
 * 5. Compose LazyColumn 性能优化指南
 *
 * Visual Spec (Dark Theme):
 * - Background: #121212
 * - Card Background: #1E1E1E
 * - Primary: #BB86FC
 * - Secondary: #03DAC6
 * - Code Background: #2D2D2D
 * - Text Primary: #FFFFFF
 * - Text Secondary: #B3B3B3
 *
 * @param viewModel ViewModel managing state and effects / 管理状态和副作用的 ViewModel
 * @param onUpdateTopBar TopBar configuration callback / TopBar 配置回调
 */

// =============================================================
// Color Constants (Dark Theme) / 颜色常量
// =============================================================
private val BackgroundColor = Color(0xFF121212)
private val CardBackground = Color(0xFF1E1E1E)
private val PrimaryColor = Color(0xFFBB86FC)
private val SecondaryColor = Color(0xFF03DAC6)
private val TextPrimary = Color(0xFFFFFFFF)
private val TextSecondary = Color(0xFFB3B3B3)
private val CodeBackground = Color(0xFF2D2D2D)
private val BadgeHigh = Color(0xFFCF6679)
private val BadgeMedium = Color(0xFFFFAB40)
private val BadgeLow = Color(0xFF69F0AE)
private val TabIndicator = Color(0xFFBB86FC)

// =============================================================
// Tab Definitions / Tab 定义
// =============================================================
private val tabTitles = listOf(
    "Context Parameters",
    "Pausable Composition",
    "性能调优工具",
    "C×C 集成指南",
    "LazyColumn 优化"
)

// =============================================================
// Main Screen / 主界面
// =============================================================
@Composable
fun KotlinPausableCompositionScreen(
    viewModel: KotlinPausableCompositionViewModel,
    onUpdateTopBar: (TopBarConfig) -> Unit
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    // Update TopBar config / 更新 TopBar 配置
    LaunchedEffect(Unit) {
        onUpdateTopBar(
            TopBarConfig(
                title = "Kotlin 2.2 + Pausable Composition",
                actions = emptyList()
            )
        )
    }

    // Collect effects / 收集副作用
    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is MainEffect.ShowCopiedSnackbar -> {
                    snackbarHostState.showSnackbar(effect.message)
                    viewModel.sendIntent(MainIntent.DismissCopiedSnackbar)
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundColor)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Tab Row / Tab 栏
            TabRow(
                selectedTabIndex = state.selectedTab,
                containerColor = CardBackground,
                contentColor = PrimaryColor,
                indicator = { tabPositions ->
                    if (state.selectedTab < tabTitles.size) {
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[state.selectedTab]),
                            color = TabIndicator
                        )
                    }
                }
            ) {
                tabTitles.forEachIndexed { index, title ->
                    Tab(
                        selected = state.selectedTab == index,
                        onClick = { viewModel.sendIntent(MainIntent.TabSelected(index)) },
                        text = {
                            Text(
                                text = title,
                                color = if (state.selectedTab == index) PrimaryColor else TextSecondary,
                                fontSize = 11.sp,
                                fontWeight = if (state.selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                                maxLines = 1
                            )
                        }
                    )
                }
            }

            // Tab Content / Tab 内容
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
            ) {
                when (state.selectedTab) {
                    0 -> ContextParametersTabContent(
                        onCopyCode = { code, id -> viewModel.sendIntent(MainIntent.CopyCode(code, id)) },
                        expandedCodeId = state.expandedCodeId,
                        onToggleExpand = { viewModel.sendIntent(MainIntent.ToggleCodeExpand(it)) }
                    )
                    1 -> PausableCompositionTabContent(
                        onCopyCode = { code, id -> viewModel.sendIntent(MainIntent.CopyCode(code, id)) },
                        expandedCodeId = state.expandedCodeId,
                        onToggleExpand = { viewModel.sendIntent(MainIntent.ToggleCodeExpand(it)) }
                    )
                    2 -> PerformanceTuningTabContent(
                        state = state,
                        onStartScan = { viewModel.sendIntent(MainIntent.StartScan) },
                        onCopyCode = { code, id -> viewModel.sendIntent(MainIntent.CopyCode(code, id)) }
                    )
                    3 -> CxCIntegrationTabContent(
                        onCopyCode = { code, id -> viewModel.sendIntent(MainIntent.CopyCode(code, id)) },
                        expandedCodeId = state.expandedCodeId,
                        onToggleExpand = { viewModel.sendIntent(MainIntent.ToggleCodeExpand(it)) }
                    )
                    4 -> LazyColumnOptimizationTabContent(
                        onCopyCode = { code, id -> viewModel.sendIntent(MainIntent.CopyCode(code, id)) },
                        expandedCodeId = state.expandedCodeId,
                        onToggleExpand = { viewModel.sendIntent(MainIntent.ToggleCodeExpand(it)) }
                    )
                }
            }
        }

        // Snackbar Host / Snackbar 宿主
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

// =============================================================
// Tab 1: Context Parameters 深度指南 / Context Parameters Deep Guide
// =============================================================
@Composable
private fun ContextParametersTabContent(
    onCopyCode: (String, String) -> Unit,
    expandedCodeId: String?,
    onToggleExpand: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundColor)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { Spacer(modifier = Modifier.height(8.dp)) }

        // Header Card / 头部卡片
        item {
            HeaderCard(
                title = "Kotlin 2.2 Context Parameters",
                subtitle = "隐式依赖注入机制 / Implicit Dependency Injection"
            )
        }

        // Section 1: What is Context Parameters / 什么是 Context Parameters
        item {
            SectionTitle("1. 什么是 Context Parameters？")
        }

        item {
            DescriptionCard(
                text = "Context Parameters 是 Kotlin 2.2 引入的隐式依赖注入机制，允许函数在不显式声明参数的情况下访问预定义的上下文对象。这是一种编译时依赖注入，由 Kotlin 编译器自动管理。"
            )
        }

        item {
            CodeExampleCard(
                codeId = "ctx_param_1",
                languageLabel = "Kotlin",
                title = "基本语法示例 / Basic Syntax",
                code = """@ContextParameters
@Composable
fun MyComposable(userRepo: UserRepository) {
    // userRepo 直接可用，无需作为参数传入
    val users = userRepo.getUsers()
}

@Composable
fun ParentComposable() {
    // 编译器自动注入 userRepo
    MyComposable()  // ✓ 编译通过
}""",
                expandedCodeId = expandedCodeId,
                onToggleExpand = onToggleExpand,
                onCopy = { code -> onCopyCode(code, "ctx_param_1") }
            )
        }

        // Section 2: 与 Hilt/Koin 的对比 / Comparison with Hilt/Koin
        item {
            SectionTitle("2. 与 Hilt/Koin 的对比")
        }

        item {
            ComparisonCard(
                rows = listOf(
                    Triple("特性", "Context Parameters", "Hilt/Koin"),
                    Triple("注入方式", "编译时隐式注入", "运行时反射注入"),
                    Triple("性能", "✓ 零开销", "⚠ 运行时查找开销"),
                    Triple("类型安全", "✓ 编译期保证", "✓ 运行时检查"),
                    Triple("测试友好", "✓ 可 mock 上下文", "✓ 直接 mock"),
                    Triple("学习曲线", "⚠ 需理解编译器", "✓ 社区熟悉")
                )
            )
        }

        // Section 3: 在 Compose 中使用 / Usage in Compose
        item {
            SectionTitle("3. 在 Compose 中使用")
        }

        item {
            CodeExampleCard(
                codeId = "ctx_param_3",
                languageLabel = "Kotlin",
                title = "在 @Composable 中使用 Context Parameters / Using in @Composable",
                code = """// 定义带有 Context Parameters 的 Composable
@ContextParameters
@Composable
fun UserProfileScreen(
    modifier: Modifier = Modifier,
    onNavigateBack: () -> Unit
) {
    // userRepo 和 logger 由编译器自动注入
    val currentUser = userRepo.getCurrentUser()
    
    Column(modifier = modifier.padding(16.dp)) {
        Text(text = currentUser.name)
        Button(onClick = onNavigateBack) {
            Text("返回")
        }
    }
}""",
                expandedCodeId = expandedCodeId,
                onToggleExpand = onToggleExpand,
                onCopy = { code -> onCopyCode(code, "ctx_param_3") }
            )
        }

        // Section 4: 测试策略 / Testing Strategy
        item {
            SectionTitle("4. 测试覆盖策略 / Testing Strategy")
        }

        item {
            TipCard(
                tipType = "best_practice",
                title = "测试建议 / Testing Tips",
                content = "使用 @ContextParameters 的 Composable 可通过 TestParametersRunner 提供 mock 上下文，使单元测试更简洁。"
            )
        }

        item {
            CodeExampleCard(
                codeId = "ctx_param_4",
                languageLabel = "Kotlin",
                title = "单元测试示例 / Unit Test Example",
                code = """@Composable
@TestParameters
fun UserProfileScreenTest() {
    val mockUserRepo = FakeUserRepository()
    val mockLogger = FakeLogger()
    
    // 通过 TestParameters 提供 mock 上下文
    CompositionLocalProvider(
        LocalUserRepository provides mockUserRepo,
        LocalLogger provides mockLogger
    ) {
        UserProfileScreen(
            onNavigateBack = {}
        )
    }
}""",
                expandedCodeId = expandedCodeId,
                onToggleExpand = onToggleExpand,
                onCopy = { code -> onCopyCode(code, "ctx_param_4") }
            )
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}

// =============================================================
// Tab 2: Pausable Composition 开发者指南 / Pausable Composition Guide
// =============================================================
@Composable
private fun PausableCompositionTabContent(
    onCopyCode: (String, String) -> Unit,
    expandedCodeId: String?,
    onToggleExpand: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundColor)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { Spacer(modifier = Modifier.height(8.dp)) }

        // Header Card / 头部卡片
        item {
            HeaderCard(
                title = "Compose Pausable Composition",
                subtitle = "分片 composition 工作 / Chunked Composition Work"
            )
        }

        // Section 1: 原理 / Principle
        item { SectionTitle("1. 分片 Composition 原理") }

        item {
            DescriptionCard(
                text = "Pausable Composition 是 Jetpack Compose April '26 (v1.11) 默认启用的特性。它将 composition 工作分成多个时间片（chunk），在每个帧的 VSync 之间暂停，而不是一次性完成所有 composition 工作。这消除了 LazyColumn 滚动时的卡顿问题。"
            )
        }

        item {
            CodeExampleCard(
                codeId = "pausable_1",
                languageLabel = "Kotlin",
                title = "shouldPause Callback 机制 / shouldPause Callback Mechanism",
                code = """/**
 * shouldPause 是 Compose 运行时内部机制
 * 开发者无法直接调用，但可以理解其行为
 * 
 * 当 shouldPause 返回 true 时，composition 暂停
 * 下一帧继续剩余的 composition 工作
 */
@Composable
fun LazyColumnWithPausableComposition(
    items: List<Item>,
    modifier: Modifier = Modifier
) {
    LazyColumn(modifier = modifier) {
        items(
            items = items,
            // Compose Compiler 自动插入 pause 点
            // 确保长时间 composition 可被中断
            key = { it.id }
        ) { item ->
            ItemContent(item = item)
        }
    }
}""",
                expandedCodeId = expandedCodeId,
                onToggleExpand = onToggleExpand,
                onCopy = { code -> onCopyCode(code, "pausable_1") }
            )
        }

        // Section 2: 与 LazyColumn 的关系 / Relationship with LazyColumn
        item { SectionTitle("2. 与 LazyColumn 的关系") }

        item {
            TipCard(
                tipType = "info",
                title = "核心优势 / Core Benefits",
                content = "LazyColumn 在未优化时，滚动时若 composition 工作量超过帧时间（~16ms），就会产生卡顿。Pausable Composition 让运行时智能地在 item 之间插入 pause 点，将工作分散到多帧。"
            )
        }

        item {
            CodeExampleCard(
                codeId = "pausable_2",
                languageLabel = "Kotlin",
                title = "LazyColumn item 优化示例 / LazyColumn Item Optimization",
                code = """@Composable
fun OptimizedLazyList(
    items: List<HeavyItem>,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        // 为每个 item 添加稳定的 key
        // 帮助 Compose 运行时更智能地管理重组
    ) {
        items(
            items = items,
            key = { item -> item.id }  // ✓ 稳定 key
        ) { item ->
            // 重型组件使用 remember 缓存
            // 减少不必要的重组
            val cachedData = remember(item.id) {
                computeHeavyData(item)
            }
            
            HeavyItemContent(
                data = cachedData,
                modifier = Modifier.animateItem()  // ✓ item 动画
            )
        }
    }
}""",
                expandedCodeId = expandedCodeId,
                onToggleExpand = onToggleExpand,
                onCopy = { code -> onCopyCode(code, "pausable_2") }
            )
        }

        // Section 3: 开发者注意事项 / Developer Notes
        item { SectionTitle("3. 开发者注意事项") }

        item {
            WarningCard(
                title = "副作用处理 / Side Effect Handling",
                content = "在预计算阶段（pre-composition），remember 和 LaunchedEffect 的行为与正式 composition 不同。副作用不会在预计算阶段执行，这可能导致数据状态不一致。"
            )
        }

        item {
            CodeExampleCard(
                codeId = "pausable_3",
                languageLabel = "Kotlin",
                title = "副作用安全处理 / Safe Side Effect Handling",
                code = """@Composable
fun SafeComposableWithEffect(
    item: ItemData,
    modifier: Modifier = Modifier
) {
    // ✓ 使用 rememberUpdatedState 确保值最新
    val latestItem by rememberUpdatedState(item)
    
    // ✓ LaunchedEffect 在 composition 完成后执行
    // 不会在预计算阶段触发
    LaunchedEffect(item.id) {
        fetchAdditionalData(item.id)
    }
    
    // ✓ 纯 UI 渲染，无副作用依赖
    ItemContent(
        item = latestItem,
        modifier = modifier
    )
}""",
                expandedCodeId = expandedCodeId,
                onToggleExpand = onToggleExpand,
                onCopy = { code -> onCopyCode(code, "pausable_3") }
            )
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}

// =============================================================
// Tab 3: 性能调优工具 / Performance Tuning Tool
// =============================================================
@Composable
private fun PerformanceTuningTabContent(
    state: MainState,
    onStartScan: () -> Unit,
    onCopyCode: (String, String) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundColor)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { Spacer(modifier = Modifier.height(8.dp)) }

        // Header Card / 头部卡片
        item {
            HeaderCard(
                title = "Pausable Composition 性能调优工具",
                subtitle = "Composable 扫描与优化建议"
            )
        }

        // Scan Button / 扫描按钮
        item {
            ScanActionCard(
                scanInProgress = state.scanInProgress,
                onStartScan = onStartScan
            )
        }

        // Scan Results / 扫描结果
        if (state.scanResults.isNotEmpty()) {
            item {
                Text(
                    text = "扫描结果 / Scan Results (${state.scanResults.size})",
                    color = TextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            items(state.scanResults) { result ->
                ScanResultCard(
                    result = result,
                    onCopyCode = { code -> onCopyCode(code, result.id) }
                )
            }
        }

        // Empty State / 空状态
        if (!state.scanInProgress && state.scanResults.isEmpty()) {
            item {
                EmptyStateCard(
                    title = "点击上方按钮开始扫描",
                    subtitle = "工具将分析 Composable 函数并提供优化建议"
                )
            }
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}

// =============================================================
// Tab 4: C×C 集成指南 / Context Parameters × Compose Integration
// =============================================================
@Composable
private fun CxCIntegrationTabContent(
    onCopyCode: (String, String) -> Unit,
    expandedCodeId: String?,
    onToggleExpand: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundColor)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { Spacer(modifier = Modifier.height(8.dp)) }

        // Header Card / 头部卡片
        item {
            HeaderCard(
                title = "Context Parameters × Compose",
                subtitle = "两者结合的架构模式 / Combined Architecture Patterns"
            )
        }

        // Section 1: 为什么结合 / Why Combine
        item { SectionTitle("1. 为什么结合使用？") }

        item {
            DescriptionCard(
                text = "Context Parameters 提供隐式的编译时依赖注入，Compose Pausable Composition 提供流畅的运行时性能。两者结合可以实现：依赖注入无感知 + UI 渲染零卡顿的完美开发体验。"
            )
        }

        // Section 2: 架构模式 / Architecture Pattern
        item { SectionTitle("2. 推荐架构模式 / Recommended Architecture") }

        item {
            CodeExampleCard(
                codeId = "cxc_2",
                languageLabel = "Kotlin",
                title = "分层架构示例 / Layered Architecture Example",
                code = """// Layer 1: Context Definition / 上下文定义
@ContextParameters
object AppContext {
    val userRepository: UserRepository
    val networkClient: NetworkClient
    val logger: Logger
}

// Layer 2: Feature Context / 功能上下文
@ContextParameters
object FeatureContext {
    // 继承 AppContext 的依赖
    // 添加功能级别依赖
    val analyticsTracker: AnalyticsTracker
    val featureConfig: FeatureConfig
}

// Layer 3: Composable 使用 / Composable Usage
@ContextParameters
@Composable
fun ProductDetailScreen(
    productId: String,
    onNavigateBack: () -> Unit
) {
    // 自动注入所需依赖
    val products = userRepository.getProducts()
    val config = featureConfig.getProductDetailConfig()
    
    ProductDetailContent(
        product = products.find { it.id == productId },
        config = config,
        onBack = onNavigateBack
    )
}""",
                expandedCodeId = expandedCodeId,
                onToggleExpand = onToggleExpand,
                onCopy = { code -> onCopyCode(code, "cxc_2") }
            )
        }

        // Section 3: 代码组织 / Code Organization
        item { SectionTitle("3. 代码组织建议 / Code Organization") }

        item {
            TipCard(
                tipType = "best_practice",
                title = "目录结构 / Directory Structure",
                content = "推荐按功能模块（feature）组织，每个模块包含：context/（上下文定义）、domain/（业务逻辑）、ui/（Compose 页面）。"
            )
        }

        item {
            CodeExampleCard(
                codeId = "cxc_3",
                languageLabel = "Kotlin",
                title = "目录结构示例 / Directory Structure Example",
                code = """// feature/
// └── products/
//     ├── context/
//     │   └── ProductContext.kt      # @ContextParameters 定义
//     ├── domain/
//     │   ├── ProductRepository.kt  # Domain 接口
//     │   └── ProductUseCase.kt      # 用例
//     ├── data/
//     │   └── ProductRepositoryImpl.kt  # 数据层实现
//     └── ui/
//         ├── ProductListScreen.kt   # 列表页
//         ├── ProductDetailScreen.kt # 详情页
//         └── components/
//             └── ProductCard.kt     # 通用组件""",
                expandedCodeId = expandedCodeId,
                onToggleExpand = onToggleExpand,
                onCopy = { code -> onCopyCode(code, "cxc_3") }
            )
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}

// =============================================================
// Tab 5: LazyColumn 优化指南 / LazyColumn Optimization Guide
// =============================================================
@Composable
private fun LazyColumnOptimizationTabContent(
    onCopyCode: (String, String) -> Unit,
    expandedCodeId: String?,
    onToggleExpand: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundColor)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { Spacer(modifier = Modifier.height(8.dp)) }

        // Header Card / 头部卡片
        item {
            HeaderCard(
                title = "LazyColumn 性能优化指南",
                subtitle = "Pausable Composition + Item Complexity Analysis"
            )
        }

        // Section 1: Item Complexity Analysis / Item 复杂度分析
        item { SectionTitle("1. Item 复杂度分析") }

        item {
            DescriptionCard(
                text = "每个 LazyColumn item 的渲染复杂度直接影响滚动流畅度。高复杂度 item 应使用 remember 和 derivedStateOf 优化，减少不必要的重组。"
            )
        }

        item {
            CodeExampleCard(
                codeId = "lazy_opt_1",
                languageLabel = "Kotlin",
                title = "Item 复杂度分级 / Item Complexity Classification",
                code = """/**
 * Item Complexity Levels / Item 复杂度分级
 * 
 * LOW:    简单文本/图标，< 50 节点
 * MEDIUM: 带状态切换的图片/卡片，50-200 节点
 * HIGH:   复杂嵌套布局/自定义绘制，> 200 节点
 */

// LOW Complexity Item / 低复杂度 Item
@Composable
fun SimpleTextItem(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium
    )
}

// MEDIUM Complexity Item / 中复杂度 Item
@Composable
fun MediaCardItem(
    media: Media,
    isPlaying: Boolean,
    onPlayPause: () -> Unit
) {
    val albumArt by remember(media.id) {
        mutableStateOf(loadAlbumArt(media))
    }
    
    Card(modifier = Modifier.fillMaxWidth()) {
        Row {
            Image(bitmap = albumArt, contentDescription = null)
            Column {
                Text(media.title)
                IconButton(onClick = onPlayPause) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Pause else Icons.Play,
                        contentDescription = null
                    )
                }
            }
        }
    }
}""",
                expandedCodeId = expandedCodeId,
                onToggleExpand = onToggleExpand,
                onCopy = { code -> onCopyCode(code, "lazy_opt_1") }
            )
        }

        // Section 2: 优化策略 / Optimization Strategies
        item { SectionTitle("2. Pausable Composition 优化策略") }

        item {
            TipCard(
                tipType = "best_practice",
                title = "最佳实践 / Best Practices",
                content = "使用 stable key 减少重组、使用 remember 缓存计算结果、使用 derivedStateOf 避免频繁重组。"
            )
        }

        item {
            CodeExampleCard(
                codeId = "lazy_opt_2",
                languageLabel = "Kotlin",
                title = "完整优化示例 / Complete Optimization Example",
                code = """@Composable
fun OptimizedLazyColumn(
    items: List<ComplexItem>,
    modifier: Modifier = Modifier
) {
    LazyColumn(modifier = modifier) {
        items(
            items = items,
            key = { item -> item.id }  // ✓ 稳定 key，减少重组
        ) { item ->
            // ✓ 使用 remember 缓存 expensive 计算
            val displayData = remember(item.id, item.revision) {
                computeDisplayData(item)
            }
            
            // ✓ 使用 derivedStateOf 避免频繁状态更新
            val isExpanded by remember {
                derivedStateOf { displayData.defaultExpanded }
            }
            
            // ✓ 使用 animateItem() 提供流畅 item 动画
            ComplexItemContent(
                data = displayData,
                isExpanded = isExpanded,
                modifier = Modifier.animateItem()
            )
        }
    }
}""",
                expandedCodeId = expandedCodeId,
                onToggleExpand = onToggleExpand,
                onCopy = { code -> onCopyCode(code, "lazy_opt_2") }
            )
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}

// =============================================================
// Reusable UI Components / 可复用 UI 组件
// =============================================================

/**
 * Header Card / 头部卡片
 */
@Composable
private fun HeaderCard(title: String, subtitle: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                color = PrimaryColor,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subtitle,
                color = TextSecondary,
                fontSize = 14.sp
            )
        }
    }
}

/**
 * Section Title / 章节标题
 */
@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title,
        color = TextPrimary,
        fontSize = 16.sp,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(top = 8.dp)
    )
}

/**
 * Description Card / 描述卡片
 */
@Composable
private fun DescriptionCard(text: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(
            text = text,
            color = TextSecondary,
            fontSize = 14.sp,
            modifier = Modifier.padding(12.dp),
            lineHeight = 20.sp
        )
    }
}

/**
 * Code Example Card / 代码示例卡片
 */
@Composable
private fun CodeExampleCard(
    codeId: String,
    languageLabel: String,
    title: String,
    code: String,
    expandedCodeId: String?,
    onToggleExpand: (String) -> Unit,
    onCopy: (String) -> Unit
) {
    val isExpanded = expandedCodeId == codeId
    val codeLines = code.lines()
    val shouldCollapse = codeLines.size > 15
    val displayCode = if (!isExpanded && shouldCollapse) {
        codeLines.take(15).joinToString("\n") + "\n    ..."
    } else {
        code
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Header Row / 头部行
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CodeBackground.copy(alpha = 0.5f))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Badge(
                        containerColor = SecondaryColor,
                        contentColor = Color.Black
                    ) {
                        Text(
                            text = languageLabel,
                            fontSize = 10.sp,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = title,
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                }

                Row {
                    IconButton(
                        onClick = { onCopy(code) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy",
                            tint = TextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    if (shouldCollapse) {
                        IconButton(
                            onClick = { onToggleExpand(codeId) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = if (isExpanded) "Collapse" else "Expand",
                                tint = PrimaryColor,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            // Code Content / 代码内容
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CodeBackground)
                    .padding(12.dp)
                    .horizontalScroll(rememberScrollState())
            ) {
                Text(
                    text = displayCode,
                    color = TextPrimary,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    lineHeight = 18.sp
                )
            }
        }
    }
}

/**
 * Comparison Card / 对比卡片
 */
@Composable
private fun ComparisonCard(rows: List<Triple<String, String, String>>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Header / 表头
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(PrimaryColor.copy(alpha = 0.2f))
                    .padding(12.dp)
            ) {
                Text(
                    text = rows[0].first,
                    color = PrimaryColor,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = rows[0].second,
                    color = PrimaryColor,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = rows[0].third,
                    color = PrimaryColor,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
            }

            // Rows / 数据行
            rows.drop(1).forEachIndexed { index, row ->
                if (index > 0) {
                    HorizontalDivider(color = CodeBackground)
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                ) {
                    Text(
                        text = row.first,
                        color = TextPrimary,
                        fontSize = 11.sp,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = row.second,
                        color = TextSecondary,
                        fontSize = 11.sp,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = row.third,
                        color = TextSecondary,
                        fontSize = 11.sp,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

/**
 * Tip Card / 提示卡片
 */
@Composable
private fun TipCard(tipType: String, title: String, content: String) {
    val accentColor = when (tipType) {
        "best_practice" -> SecondaryColor
        "info" -> PrimaryColor
        "warning" -> BadgeMedium
        else -> SecondaryColor
    }
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(4.dp, 40.dp)
                    .background(accentColor, RoundedCornerShape(2.dp))
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = title,
                    color = accentColor,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = content,
                    color = TextSecondary,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )
            }
        }
    }
}

/**
 * Warning Card / 警告卡片
 */
@Composable
private fun WarningCard(title: String, content: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(4.dp, 40.dp)
                    .background(BadgeHigh, RoundedCornerShape(2.dp))
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = title,
                    color = BadgeHigh,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = content,
                    color = TextSecondary,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )
            }
        }
    }
}

/**
 * Scan Action Card / 扫描操作卡片
 */
@Composable
private fun ScanActionCard(
    scanInProgress: Boolean,
    onStartScan: () -> Unit
) {
    val rotation by animateFloatAsState(
        targetValue = if (scanInProgress) 360f else 0f,
        label = "scan_rotation"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = !scanInProgress) { onStartScan() },
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = "Scan",
                tint = if (scanInProgress) PrimaryColor else SecondaryColor,
                modifier = Modifier
                    .size(48.dp)
                    .then(
                        if (scanInProgress) Modifier else Modifier
                    )
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (scanInProgress) "扫描中..." else "开始扫描 / Start Scan",
                color = if (scanInProgress) TextSecondary else PrimaryColor,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
            if (scanInProgress) {
                Spacer(modifier = Modifier.height(12.dp))
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth(),
                    color = PrimaryColor
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "正在分析 Composable 函数...",
                    color = TextSecondary,
                    fontSize = 12.sp
                )
            } else {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "扫描项目中的 Composable 并提供优化建议",
                    color = TextSecondary,
                    fontSize = 12.sp
                )
            }
        }
    }
}

/**
 * Scan Result Card / 扫描结果卡片
 */
@Composable
private fun ScanResultCard(
    result: ComposableInfo,
    onCopyCode: (String) -> Unit
) {
    val badgeColor = when (result.optimizationBenefit) {
        BenefitLevel.HIGH -> BadgeHigh
        BenefitLevel.MEDIUM -> BadgeMedium
        BenefitLevel.LOW -> BadgeLow
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = result.name,
                    color = TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Badge(
                    containerColor = badgeColor,
                    contentColor = Color.Black
                ) {
                    Text(
                        text = result.optimizationBenefit.label,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = result.suggestion,
                color = TextSecondary,
                fontSize = 12.sp,
                lineHeight = 16.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(
                    onClick = { onCopyCode(result.suggestion) },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copy",
                        tint = TextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

/**
 * Empty State Card / 空状态卡片
 */
@Composable
private fun EmptyStateCard(title: String, subtitle: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                color = TextSecondary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = subtitle,
                color = TextSecondary.copy(alpha = 0.7f),
                fontSize = 13.sp
            )
        }
    }
}