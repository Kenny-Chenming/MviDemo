package com.mvi.kenny.feature.quailldebugtools

// ================================================================
// QuailDebugToolsScreen — Android Studio Quail 调试/性能工具包主页面
// ================================================================
// MVI architecture: receives State, renders UI, emits Intent.
//
// PRD-242: Android Studio Quail 调试/性能工具包
// Design: memory/agency/designs/PRD-242-Android-Studio-Quail-调试-性能工具包.md
//
// 5 Tabs:
//   Tab 0: Recomposition Observe Node — Layout Inspector 内追踪 Composable 状态读取
//   Tab 1: LeakCanary in Profiler — LeakCanary 原生集成进 Android Studio Profiler
//   Tab 2: 联合诊断工作流 — Observe Node + Layout Inspector + Profiler 三工具联动
//   Tab 3: CI 集成方案 — Recomposition/LeakCanary CI 集成最佳实践
//   Tab 4: Quail vs Panda 选型 — Quail vs Panda 4 调试工具选型决策树
// ================================================================

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.material3.ExperimentalMaterial3Api
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

// ─────────────────────────────────────────────────────────────────
// Color System / 颜色系统
// ─────────────────────────────────────────────────────────────────
private object QuailToolsColors {
    // Android Studio Dark Theme
    val Background = Color(0xFF0D1117)
    val Surface = Color(0xFF161B22)
    val SurfaceVariant = Color(0xFF21262D)
    val CardBorder = Color(0xFF30363D)
    val AndroidGreen = Color(0xFF3DDC84)
    val QuailBlue = Color(0xFF58A6FF)
    val Purple = Color(0xFFBC8CFF)
    val Yellow = Color(0xFFD29922)
    val Orange = Color(0xFFFFA657)
    val Red = Color(0xFFF85149)
    val Green = Color(0xFF3FB950)
    val OnSurface = Color(0xFFE6EDF3)
    val OnSurfaceVariant = Color(0xFF8B949E)
}

// ─────────────────────────────────────────────────────────────────
// Tab Configuration / Tab 配置
// ─────────────────────────────────────────────────────────────────
private data class TabConfig(
    val title: String,
    val titleEn: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

// ─────────────────────────────────────────────────────────────────
// Main Screen / 主页面
// ─────────────────────────────────────────────────────────────────

/**
 * ============================================================
 * QuailDebugToolsScreen — Quail 调试/性能工具主页面
 * ============================================================
 * Entry point composable for the Android Studio Quail Debug & Performance Toolkit.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuailDebugToolsScreen(
    viewModel: QuailDebugToolsViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val pagerState = rememberPagerState(pageCount = { 5 })
    val coroutineScope = rememberCoroutineScope()
    val clipboardManager = LocalClipboardManager.current

    // ── Side Effect Handling / 副作用处理 ──────────────────────
    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is QuailDebugToolsEffect.CopyToClipboard -> {
                    clipboardManager.setText(AnnotatedString(effect.content))
                }
                is QuailDebugToolsEffect.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(effect.message)
                }
                is QuailDebugToolsEffect.OpenExternalLink -> {
                    // External link handling would go here
                }
            }
        }
    }

    // ── Sync Tab with Pager / Tab 与 Pager 同步 ───────────────
    LaunchedEffect(pagerState.currentPage) {
        viewModel.sendIntent(QuailDebugToolsIntent.SelectTab(pagerState.currentPage))
    }

    // ── Tab Configuration / Tab 配置 ─────────────────────────
    val tabs = listOf(
        TabConfig("Observe Node", "🔍", Icons.Default.Search),
        TabConfig("LeakCanary", "🔧", Icons.Default.Build),
        TabConfig("联合诊断", "🔗", Icons.Default.Link),
        TabConfig("CI 集成", "⚙️", Icons.Default.Settings),
        TabConfig("选型决策", "⚖️", Icons.Default.Balance)
    )

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = QuailToolsColors.Background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // ── Header / 头部 ────────────────────────────────
            QuailToolsHeader()

            // ── Tab Row / Tab 栏 ────────────────────────────
            TabRow(
                selectedTabIndex = pagerState.currentPage,
                containerColor = QuailToolsColors.Surface,
                contentColor = QuailToolsColors.OnSurface,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[pagerState.currentPage]),
                        color = QuailToolsColors.AndroidGreen
                    )
                }
            ) {
                tabs.forEachIndexed { index, tab ->
                    Tab(
                        selected = pagerState.currentPage == index,
                        onClick = {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(index)
                            }
                        },
                        text = {
                            Text(
                                text = tab.title,
                                color = if (pagerState.currentPage == index)
                                    QuailToolsColors.AndroidGreen
                                else
                                    QuailToolsColors.OnSurfaceVariant
                            )
                        },
                        icon = {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = tab.titleEn,
                                tint = if (pagerState.currentPage == index)
                                    QuailToolsColors.AndroidGreen
                                else
                                    QuailToolsColors.OnSurfaceVariant
                            )
                        }
                    )
                }
            }

            // ── Pager Content / Pager 内容 ──────────────────
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                when (page) {
                    0 -> ObserveNodeTab(state, viewModel)
                    1 -> LeakCanaryTab(state, viewModel)
                    2 -> JointDiagnosticTab(state, viewModel)
                    3 -> CIIntegrationTab(state, viewModel)
                    4 -> QuailVsPandaTab(state, viewModel)
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────
// Header / 头部
// ─────────────────────────────────────────────────────────────────

@Composable
private fun QuailToolsHeader() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(QuailToolsColors.Surface)
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.BugReport,
                contentDescription = "Quail Debug Tools",
                tint = QuailToolsColors.AndroidGreen,
                modifier = Modifier.size(32.dp)
            )
            Column {
                Text(
                    text = "Android Studio Quail 调试/性能工具包",
                    style = MaterialTheme.typography.titleLarge,
                    color = QuailToolsColors.OnSurface,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "PRD-242 | Recomposition Observe Node + LeakCanary in Profiler",
                    style = MaterialTheme.typography.bodySmall,
                    color = QuailToolsColors.OnSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Version Badge / 版本徽章
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            QuailBadge("Quail 1 Canary 4+", QuailToolsColors.QuailBlue)
            QuailBadge("Recomposition Observe Node", QuailToolsColors.Green)
            QuailBadge("LeakCanary in Profiler", QuailToolsColors.Purple)
        }
    }
}

@Composable
private fun QuailBadge(text: String, color: Color) {
    Surface(
        shape = RoundedCornerShape(4.dp),
        color = color.copy(alpha = 0.15f),
        contentColor = color
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium
        )
    }
}

// ─────────────────────────────────────────────────────────────────
// Tab 0: Observe Node / Tab 0: Observe Node 指南
// ─────────────────────────────────────────────────────────────────

@Composable
private fun ObserveNodeTab(
    state: QuailDebugToolsState,
    viewModel: QuailDebugToolsViewModel
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(QuailToolsColors.Background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // ── Observe Node Ready Banner ──────────────────────
        item {
            ObserveNodeReadyBanner(state.observeNodeReady)
        }

        // ── Section: What is Observe Node ─────────────────
        item {
            SectionCard(
                title = "Recomposition Observe Node 是什么？",
                titleEn = "What is Recomposition Observe Node?",
                emoji = "🔍",
                description = "Android Studio Quail 1 Canary 4 在 Layout Inspector 中新增的调试功能，允许开发者直接追踪 Composable 的状态读取（state reads），无需打日志或使用外部工具。这是 Android Studio 史上首次在 IDE 内提供原子级的 recomposition 级别调试能力。"
            )
        }

        // ── Observe Node Guide Items ───────────────────────
        items(state.observeNodeGuideItems) { item ->
            ObserveNodeGuideCard(item, viewModel)
        }
    }
}

@Composable
private fun ObserveNodeReadyBanner(isReady: Boolean) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = if (isReady) QuailToolsColors.Green.copy(alpha = 0.15f) else QuailToolsColors.Yellow.copy(alpha = 0.15f),
        contentColor = if (isReady) QuailToolsColors.Green else QuailToolsColors.Yellow
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = if (isReady) Icons.Default.CheckCircle else Icons.Default.Warning,
                contentDescription = if (isReady) "Ready" else "Not Ready"
            )
            Text(
                text = if (isReady)
                    "✅ Observe Node 已就绪（需要 Quail 1 Canary 4+）"
                else
                    "⚠️ Observe Node 需要 Android Studio Quail 1 Canary 4+",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun ObserveNodeGuideCard(
    item: ObserveNodeGuideItem,
    viewModel: QuailDebugToolsViewModel
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = QuailToolsColors.Surface),
        border = BorderStroke(1.dp, QuailToolsColors.CardBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // ── Title Row ───────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.titleMedium,
                        color = QuailToolsColors.OnSurface,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (expanded) "Collapse" else "Expand",
                    tint = QuailToolsColors.OnSurfaceVariant
                )
            }

            Text(
                text = item.titleEn,
                style = MaterialTheme.typography.bodySmall,
                color = QuailToolsColors.OnSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = item.description,
                style = MaterialTheme.typography.bodyMedium,
                color = QuailToolsColors.OnSurfaceVariant
            )

            // ── Expanded Content ────────────────────────────
            if (expanded) {
                Spacer(modifier = Modifier.height(12.dp))

                // Steps / 步骤
                Text(
                    text = "操作步骤",
                    style = MaterialTheme.typography.labelMedium,
                    color = QuailToolsColors.AndroidGreen,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                item.stepsCn.forEachIndexed { index, step ->
                    Row(
                        modifier = Modifier.padding(vertical = 2.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "${index + 1}.",
                            color = QuailToolsColors.AndroidGreen,
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            text = step,
                            color = QuailToolsColors.OnSurface,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Key Insight / 关键洞察
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(6.dp),
                    color = QuailToolsColors.Yellow.copy(alpha = 0.1f),
                    contentColor = QuailToolsColors.Yellow
                ) {
                    Text(
                        text = item.keyInsight,
                        modifier = Modifier.padding(8.dp),
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Screenshot Placeholder / 截图占位符
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(6.dp),
                    color = QuailToolsColors.SurfaceVariant,
                    contentColor = QuailToolsColors.OnSurfaceVariant
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Image,
                            contentDescription = "Screenshot",
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = item.screenshotPlaceholder,
                            style = MaterialTheme.typography.bodySmall,
                            color = QuailToolsColors.OnSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────
// Tab 1: LeakCanary in Profiler / Tab 1: LeakCanary 指南
// ─────────────────────────────────────────────────────────────────

@Composable
private fun LeakCanaryTab(
    state: QuailDebugToolsState,
    viewModel: QuailDebugToolsViewModel
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(QuailToolsColors.Background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // ── What is LeakCanary in Profiler ─────────────────
        item {
            SectionCard(
                title = "LeakCanary in Profiler 是什么？",
                titleEn = "What is LeakCanary in Profiler?",
                emoji = "🔧",
                description = "Android Studio Quail 1 首次将 LeakCanary 能力集成进 Android Studio Profiler，无需在项目中添加 LeakCanary 依赖即可使用内存泄漏检测功能。LeakCanary in Profiler 与 Profiler Memory 工具合二为一，是 Android Studio 工具链的重大整合。"
            )
        }

        // ── LeakCanary Guide Items ────────────────────────
        items(state.leakcanaryGuideItems) { item ->
            LeakCanaryGuideCard(item, viewModel)
        }

        // ── Profiler vs Library Comparison ────────────────
        item {
            Text(
                text = "Profiler 版 vs 库版对比",
                style = MaterialTheme.typography.titleMedium,
                color = QuailToolsColors.OnSurface,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        items(state.leakcanaryVsLibraryItems) { item ->
            LeakCanaryVsLibraryCard(item)
        }
    }
}

@Composable
private fun LeakCanaryGuideCard(
    item: LeakCanaryGuideItem,
    viewModel: QuailDebugToolsViewModel
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = QuailToolsColors.Surface),
        border = BorderStroke(1.dp, QuailToolsColors.CardBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.titleMedium,
                        color = QuailToolsColors.OnSurface,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = item.titleEn,
                        style = MaterialTheme.typography.bodySmall,
                        color = QuailToolsColors.OnSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = QuailToolsColors.Purple.copy(alpha = 0.15f),
                        contentColor = QuailToolsColors.Purple
                    ) {
                        Text(
                            text = "检测范围：${item.detectionScope}",
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (expanded) "Collapse" else "Expand",
                    tint = QuailToolsColors.OnSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = item.description,
                style = MaterialTheme.typography.bodyMedium,
                color = QuailToolsColors.OnSurfaceVariant
            )

            if (expanded) {
                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "操作步骤",
                    style = MaterialTheme.typography.labelMedium,
                    color = QuailToolsColors.Purple,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                item.stepsCn.forEachIndexed { index, step ->
                    Row(
                        modifier = Modifier.padding(vertical = 2.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "${index + 1}.",
                            color = QuailToolsColors.Purple,
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            text = step,
                            color = QuailToolsColors.OnSurface,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(6.dp),
                    color = QuailToolsColors.Red.copy(alpha = 0.1f),
                    contentColor = QuailToolsColors.Red
                ) {
                    Text(
                        text = item.limitations,
                        modifier = Modifier.padding(8.dp),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}

@Composable
private fun LeakCanaryVsLibraryCard(item: LeakCanaryVsLibraryItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = QuailToolsColors.SurfaceVariant),
        border = BorderStroke(1.dp, QuailToolsColors.CardBorder)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = item.feature,
                style = MaterialTheme.typography.titleSmall,
                color = QuailToolsColors.OnSurface,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Profiler 版",
                        style = MaterialTheme.typography.labelSmall,
                        color = QuailToolsColors.QuailBlue
                    )
                    Text(
                        text = item.profilerCapability,
                        style = MaterialTheme.typography.bodySmall,
                        color = QuailToolsColors.OnSurface
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "库版",
                        style = MaterialTheme.typography.labelSmall,
                        color = QuailToolsColors.Green
                    )
                    Text(
                        text = item.libraryCapability,
                        style = MaterialTheme.typography.bodySmall,
                        color = QuailToolsColors.OnSurface
                    )
                }
            }
            if (item.notes.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "💡 ${item.notes}",
                    style = MaterialTheme.typography.bodySmall,
                    color = QuailToolsColors.OnSurfaceVariant
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────
// Tab 2: Joint Diagnostic Workflow / Tab 2: 联合诊断工作流
// ─────────────────────────────────────────────────────────────────

@Composable
private fun JointDiagnosticTab(
    state: QuailDebugToolsState,
    viewModel: QuailDebugToolsViewModel
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(QuailToolsColors.Background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            SectionCard(
                title = "联合诊断工作流",
                titleEn = "Joint Diagnostic Workflow",
                emoji = "🔗",
                description = "Recomposition Observe Node + Layout Inspector + LeakCanary in Profiler 三工具联动，完整覆盖性能问题排查路径：收集 → 分析 → 修复 → 验证。"
            )
        }

        // ── Diagnostic Step Selector ───────────────────────
        item {
            DiagnosticStepSelector(state, viewModel)
        }

        // ── Workflow Steps ─────────────────────────────────
        items(state.diagnosticWorkflowSteps) { step ->
            DiagnosticWorkflowCard(step, state.currentDiagnosticStep == step.step)
        }
    }
}

@Composable
private fun DiagnosticStepSelector(
    state: QuailDebugToolsState,
    viewModel: QuailDebugToolsViewModel
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        DiagnosticStep.entries.forEach { step ->
            val isSelected = state.currentDiagnosticStep == step
            Surface(
                modifier = Modifier.clickable {
                    viewModel.sendIntent(QuailDebugToolsIntent.SelectDiagnosticStep(step))
                },
                shape = RoundedCornerShape(8.dp),
                color = if (isSelected) QuailToolsColors.AndroidGreen.copy(alpha = 0.2f) else QuailToolsColors.Surface,
                contentColor = if (isSelected) QuailToolsColors.AndroidGreen else QuailToolsColors.OnSurfaceVariant,
                border = BorderStroke(
                    1.dp,
                    if (isSelected) QuailToolsColors.AndroidGreen else QuailToolsColors.CardBorder
                )
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(text = step.emoji)
                    Text(
                        text = "Step ${step.stepNumber}: ${step.displayName}",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                    )
                }
            }
        }
    }
}

@Composable
private fun DiagnosticWorkflowCard(
    step: DiagnosticWorkflowStep,
    isActive: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isActive) QuailToolsColors.AndroidGreen.copy(alpha = 0.08f) else QuailToolsColors.Surface
        ),
        border = BorderStroke(
            2.dp,
            if (isActive) QuailToolsColors.AndroidGreen else QuailToolsColors.CardBorder
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = step.step.emoji,
                    style = MaterialTheme.typography.titleLarge
                )
                Column {
                    Text(
                        text = "Step ${step.step.stepNumber}",
                        style = MaterialTheme.typography.labelSmall,
                        color = QuailToolsColors.AndroidGreen
                    )
                    Text(
                        text = step.title,
                        style = MaterialTheme.typography.titleMedium,
                        color = QuailToolsColors.OnSurface,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = step.description,
                style = MaterialTheme.typography.bodyMedium,
                color = QuailToolsColors.OnSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "使用工具：${step.tools.joinToString(" + ")}",
                style = MaterialTheme.typography.bodySmall,
                color = QuailToolsColors.OnSurfaceVariant
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "预期结果：${step.expectedOutcome}",
                style = MaterialTheme.typography.bodySmall,
                color = QuailToolsColors.AndroidGreen
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────
// Tab 3: CI Integration / Tab 3: CI 集成
// ─────────────────────────────────────────────────────────────────

@Composable
private fun CIIntegrationTab(
    state: QuailDebugToolsState,
    viewModel: QuailDebugToolsViewModel
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(QuailToolsColors.Background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            SectionCard(
                title = "CI 集成方案",
                titleEn = "CI Integration Best Practices",
                emoji = "⚙️",
                description = "Observe Node 是设计时工具，无法 CI 自动化。LeakCanary 推荐使用库版（而非 Profiler 版）集成到 CI，支持 Gradle 插件方式阻塞未修复泄漏的构建。Profiler 版用于开发调试，库版用于 CI 自动化。"
            )
        }

        items(state.ciIntegrationItems) { item ->
            CIIntegrationCard(item, viewModel)
        }
    }
}

@Composable
private fun CIIntegrationCard(
    item: CIIntegrationItem,
    viewModel: QuailDebugToolsViewModel
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = QuailToolsColors.Surface),
        border = BorderStroke(1.dp, QuailToolsColors.CardBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = item.title,
                            style = MaterialTheme.typography.titleMedium,
                            color = QuailToolsColors.OnSurface,
                            fontWeight = FontWeight.SemiBold
                        )
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = QuailToolsColors.Yellow.copy(alpha = 0.15f),
                            contentColor = QuailToolsColors.Yellow
                        ) {
                            Text(
                                text = item.ciPlatform,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = item.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = QuailToolsColors.OnSurfaceVariant,
                        maxLines = if (expanded) Int.MAX_VALUE else 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (expanded) "Collapse" else "Expand",
                    tint = QuailToolsColors.OnSurfaceVariant
                )
            }

            if (expanded) {
                Spacer(modifier = Modifier.height(12.dp))

                // Code Block / 代码块
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            viewModel.sendIntent(QuailDebugToolsIntent.CopyCode(item.codeExample))
                        },
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF1E1E1E),
                    contentColor = QuailToolsColors.OnSurface
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "YAML / Groovy",
                                style = MaterialTheme.typography.labelSmall,
                                color = QuailToolsColors.OnSurfaceVariant
                            )
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copy",
                                tint = QuailToolsColors.OnSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        SelectionContainer {
                            Text(
                                text = item.codeExample,
                                style = MaterialTheme.typography.bodySmall,
                                fontFamily = FontFamily.Monospace,
                                color = QuailToolsColors.OnSurface
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(6.dp),
                    color = QuailToolsColors.AndroidGreen.copy(alpha = 0.1f),
                    contentColor = QuailToolsColors.AndroidGreen
                ) {
                    Text(
                        text = "💡 ${item.notes}",
                        modifier = Modifier.padding(8.dp),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────
// Tab 4: Quail vs Panda Selection / Tab 4: Quail vs Panda 选型
// ─────────────────────────────────────────────────────────────────

@Composable
private fun QuailVsPandaTab(
    state: QuailDebugToolsState,
    viewModel: QuailDebugToolsViewModel
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(QuailToolsColors.Background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            SectionCard(
                title = "Quail vs Panda 4 选型决策树",
                titleEn = "Quail vs Panda 4 Debug Tool Selection",
                emoji = "⚖️",
                description = "Quail（Canary）和 Panda（稳定版）各有优势，根据调试场景选择合适的版本：需要最新功能（如 Recomposition Observe Node）用 Quail，需要稳定性用 Panda。"
            )
        }

        // ── Studio Version Selector ───────────────────────
        item {
            StudioVersionSelector(state, viewModel)
        }

        // ── Comparison Items ──────────────────────────────
        items(state.comparisonItems) { item ->
            VersionComparisonCard(item)
        }
    }
}

@Composable
private fun StudioVersionSelector(
    state: QuailDebugToolsState,
    viewModel: QuailDebugToolsViewModel
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        StudioVersion.entries.forEach { version ->
            val isSelected = state.studioVersion == version
            Surface(
                modifier = Modifier.clickable {
                    viewModel.sendIntent(QuailDebugToolsIntent.SelectStudioVersion(version))
                },
                shape = RoundedCornerShape(8.dp),
                color = if (isSelected) when (version) {
                    StudioVersion.QUAIL_CANARY -> QuailToolsColors.QuailBlue.copy(alpha = 0.2f)
                    StudioVersion.QUAIL_STABLE -> QuailToolsColors.Green.copy(alpha = 0.2f)
                    StudioVersion.PANDA_4 -> QuailToolsColors.Yellow.copy(alpha = 0.2f)
                } else QuailToolsColors.Surface,
                contentColor = when (version) {
                    StudioVersion.QUAIL_CANARY -> QuailToolsColors.QuailBlue
                    StudioVersion.QUAIL_STABLE -> QuailToolsColors.Green
                    StudioVersion.PANDA_4 -> QuailToolsColors.Yellow
                },
                border = BorderStroke(
                    1.dp,
                    if (isSelected) when (version) {
                        StudioVersion.QUAIL_CANARY -> QuailToolsColors.QuailBlue
                        StudioVersion.QUAIL_STABLE -> QuailToolsColors.Green
                        StudioVersion.PANDA_4 -> QuailToolsColors.Yellow
                    } else QuailToolsColors.CardBorder
                )
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = version.emoji,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = version.displayName,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                    )
                }
            }
        }
    }
}

@Composable
private fun VersionComparisonCard(item: VersionComparisonItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = QuailToolsColors.Surface),
        border = BorderStroke(1.dp, QuailToolsColors.CardBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = item.emoji,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = item.feature,
                    style = MaterialTheme.typography.titleMedium,
                    color = QuailToolsColors.OnSurface,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(6.dp),
                    color = QuailToolsColors.QuailBlue.copy(alpha = 0.1f),
                    contentColor = QuailToolsColors.QuailBlue
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text(
                            text = "Quail",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = item.quailCapability,
                            style = MaterialTheme.typography.bodySmall,
                            color = QuailToolsColors.OnSurface
                        )
                    }
                }

                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(6.dp),
                    color = QuailToolsColors.Yellow.copy(alpha = 0.1f),
                    contentColor = QuailToolsColors.Yellow
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text(
                            text = "Panda 4",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = item.pandaCapability,
                            style = MaterialTheme.typography.bodySmall,
                            color = QuailToolsColors.OnSurface
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(6.dp),
                color = QuailToolsColors.AndroidGreen.copy(alpha = 0.1f),
                contentColor = QuailToolsColors.AndroidGreen
            ) {
                Text(
                    text = "💡 ${item.recommendation}",
                    modifier = Modifier.padding(8.dp),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────
// Section Card / 区块卡片
// ─────────────────────────────────────────────────────────────────

@Composable
private fun SectionCard(
    title: String,
    titleEn: String,
    emoji: String,
    description: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = QuailToolsColors.Surface),
        border = BorderStroke(1.dp, QuailToolsColors.CardBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = emoji,
                    style = MaterialTheme.typography.titleLarge
                )
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        color = QuailToolsColors.OnSurface,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = titleEn,
                        style = MaterialTheme.typography.bodySmall,
                        color = QuailToolsColors.OnSurfaceVariant
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = QuailToolsColors.OnSurfaceVariant
            )
        }
    }
}
