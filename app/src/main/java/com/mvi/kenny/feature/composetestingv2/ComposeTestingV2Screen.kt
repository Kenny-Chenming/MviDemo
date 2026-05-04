package com.mvi.kenny.feature.composetestingv2

// ================================================================
// ComposeTestingV2Screen — Jetpack Compose 1.11 Testing v2 API 迁移工具主页面
// ================================================================
// MVI architecture: receives State, renders UI, emits Intent.
//
// PRD-228: Jetpack Compose 1.11 Testing v2 API 迁移工具包
// Design: memory/agency/designs/PRD-228-Jetpack-Compose-Testing-v2-API-迁移工具包.md
//
// 5 Tabs:
//   Tab 0: v2 扫描器 — Detect composeTestRule/runTest/UnconfinedTestDispatcher usage
//   Tab 1: Dispatcher 指南 — UnconfinedTestDispatcher vs StandardTestDispatcher
//   Tab 2: Espresso 协调 — launchFragmentInContainer + FragmentComposeRule migration
//   Tab 3: CI 合规检测 — Gradle plugin compliance + audit checklist
//   Tab 4: KMP + 迁移指南 — Compose Multiplatform v2 + composeTestRule → createComposeRule
// ================================================================

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
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
import com.mvi.kenny.feature.composetestingv2.ComposeTestingV2Intent.*
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

// ─────────────────────────────────────────────────────────────────
// Color System / 颜色系统
// ─────────────────────────────────────────────────────────────────
private object ComposeV2Colors {
    // Dark Terminal Theme
    val Background = Color(0xFF0D1117)
    val Surface = Color(0xFF161B22)
    val SurfaceVariant = Color(0xFF21262D)
    val CardBorder = Color(0xFF30363D)
    val TerminalGreen = Color(0xFF39D353)
    val TerminalBlue = Color(0xFF58A6FF)
    val TerminalPurple = Color(0xFFBC8CFF)
    val TerminalYellow = Color(0xFFD29922)
    val TerminalOrange = Color(0xFFFFA657)
    val DiffRed = Color(0xFFF85149)
    val DiffRedBg = Color(0x1AF85149)
    val DiffGreen = Color(0xFF3FB950)
    val DiffGreenBg = Color(0x1A3FB950)
    val OnSurface = Color(0xFFE6EDF3)
    val OnSurfaceVariant = Color(0xFF8B949E)
    val P0Color = Color(0xFFF85149)
    val P1Color = Color(0xFFD29922)
    val P2Color = Color(0xFF3FB950)
    val P3Color = Color(0xFF6BCF7F)
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
 * ComposeTestingV2Screen — Compose Testing v2 迁移工具主页面
 * ============================================================
 * Entry point composable for the Compose 1.11 Testing v2 Migration Toolkit.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComposeTestingV2Screen(
    viewModel: ComposeTestingV2ViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val clipboardManager = LocalClipboardManager.current

    // Load initial tab data on first composition
    LaunchedEffect(Unit) {
        viewModel.loadTabData(0)
    }

    // Collect effects / 收集副作用
    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is ComposeTestingV2Effect.CopyToClipboard -> {
                    clipboardManager.setText(AnnotatedString(effect.content))
                }
                is ComposeTestingV2Effect.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(effect.message)
                }
            }
        }
    }

    val tabs = listOf(
        TabConfig("v2 扫描器", "v2 Scanner", Icons.Default.Search),
        TabConfig("Dispatcher", "Dispatcher", Icons.Default.Schedule),
        TabConfig("Espresso", "Espresso", Icons.Default.IntegrationInstructions),
        TabConfig("CI 合规", "CI Compliance", Icons.Default.VerifiedUser),
        TabConfig("KMP + 迁移", "KMP + Migration", Icons.Default.DeveloperMode)
    )

    val pagerState = rememberPagerState(pageCount = { tabs.size })

    // Sync pager with state
    LaunchedEffect(pagerState.currentPage) {
        viewModel.sendIntent(SelectTab(pagerState.currentPage))
        viewModel.loadTabData(pagerState.currentPage)
    }

    LaunchedEffect(state.selectedTab) {
        if (pagerState.currentPage != state.selectedTab) {
            pagerState.animateScrollToPage(state.selectedTab)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = ComposeV2Colors.Background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Top App Bar
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeveloperBoard,
                            contentDescription = null,
                            tint = ComposeV2Colors.TerminalPurple
                        )
                        Text(
                            text = "Compose Testing v2",
                            color = ComposeV2Colors.OnSurface,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = ComposeV2Colors.Surface
                )
            )

            // Tab Row
            TabRow(
                selectedTabIndex = pagerState.currentPage,
                containerColor = ComposeV2Colors.Surface,
                contentColor = ComposeV2Colors.OnSurface,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[pagerState.currentPage]),
                        color = ComposeV2Colors.TerminalPurple
                    )
                }
            ) {
                tabs.forEachIndexed { index, tab ->
                    Tab(
                        selected = pagerState.currentPage == index,
                        onClick = {
                            // Pager handles scroll internally; navigate to tab
                            viewModel.sendIntent(SelectTab(index))
                        },
                        text = {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = tab.icon,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = tab.title,
                                    style = MaterialTheme.typography.labelSmall,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        },
                        selectedContentColor = ComposeV2Colors.TerminalPurple,
                        unselectedContentColor = ComposeV2Colors.OnSurfaceVariant
                    )
                }
            }

            // Pager Content
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
                beyondViewportPageCount = 1
            ) { page ->
                when (page) {
                    0 -> V2ScannerTab(state, viewModel)
                    1 -> DispatcherGuideTab(state, viewModel)
                    2 -> EspressoCoordinationTab(state, viewModel)
                    3 -> CIComplianceTab(state, viewModel)
                    4 -> KPPUseGuideTab(state, viewModel)
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────
// Tab 0: v2 Scanner / v2 扫描器
// ─────────────────────────────────────────────────────────────────

@Composable
private fun V2ScannerTab(
    state: ComposeTestingV2State,
    viewModel: ComposeTestingV2ViewModel
) {
    val clipboardManager = LocalClipboardManager.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(ComposeV2Colors.Background)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        // Header Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = ComposeV2Colors.Surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, ComposeV2Colors.CardBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = ComposeV2Colors.TerminalPurple
                        )
                        Text(
                            text = "Compose Testing v2 API Scanner",
                            style = MaterialTheme.typography.titleMedium,
                            color = ComposeV2Colors.OnSurface,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "检测 composeTestRule / UnconfinedTestDispatcher / runTest 等 v1 API 使用，输出 v2 替代方案",
                        style = MaterialTheme.typography.bodySmall,
                        color = ComposeV2Colors.OnSurfaceVariant
                    )
                }
            }
        }

        // Status Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = ComposeV2Colors.Surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, ComposeV2Colors.CardBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Overall Status / 整体状态",
                                style = MaterialTheme.typography.labelMedium,
                                color = ComposeV2Colors.OnSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(
                                    text = state.overallStatus.emoji,
                                    fontSize = 20.sp
                                )
                                Text(
                                    text = state.overallStatus.displayName,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = state.overallStatus.color,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        if (state.isScanning) {
                            Column(horizontalAlignment = Alignment.End) {
                                CircularProgressIndicator(
                                    progress = { state.scanProgress },
                                    modifier = Modifier.size(40.dp),
                                    color = ComposeV2Colors.TerminalPurple,
                                    strokeWidth = 3.dp
                                )
                                Text(
                                    text = state.scanProgressText,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = ComposeV2Colors.OnSurfaceVariant
                                )
                            }
                        }
                    }

                    if (state.scanResults.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider(color = ComposeV2Colors.CardBorder)
                        Spacer(modifier = Modifier.height(12.dp))

                        val p0 = state.scanResults.count { it.riskLevel == RiskLevel.P0_CRITICAL }
                        val p1 = state.scanResults.count { it.riskLevel == RiskLevel.P1_HIGH }
                        val p2 = state.scanResults.count { it.riskLevel == RiskLevel.P2_MEDIUM }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            StatusChip("🔴 P0", p0, ComposeV2Colors.P0Color)
                            StatusChip("🟠 P1", p1, ComposeV2Colors.P1Color)
                            StatusChip("🟡 P2", p2, ComposeV2Colors.P2Color)
                        }
                    }
                }
            }
        }

        // Scan Button
        item {
            Button(
                onClick = { viewModel.sendIntent(RunV2Scan(simulate = true)) },
                enabled = !state.isScanning,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ComposeV2Colors.TerminalPurple,
                    disabledContainerColor = ComposeV2Colors.SurfaceVariant
                )
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (state.isScanning) "Scanning..." else "Start Scan / 开始扫描")
            }
        }

        // Scan Results
        items(state.scanResults) { issue ->
            ScanIssueCard(
                issue = issue,
                onCopy = { viewModel.sendIntent(CopyFixCode(issue.fixSnippet)) }
            )
        }

        // Empty State
        if (state.scanResults.isEmpty() && !state.isScanning) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = ComposeV2Colors.Surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, ComposeV2Colors.CardBorder)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Science,
                            contentDescription = null,
                            tint = ComposeV2Colors.OnSurfaceVariant,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Click 'Start Scan' to detect Testing v1 APIs",
                            color = ComposeV2Colors.OnSurfaceVariant
                        )
                        Text(
                            text = "点击「开始扫描」检测 Testing v1 API",
                            color = ComposeV2Colors.OnSurfaceVariant,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusChip(label: String, count: Int, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = count.toString(), style = MaterialTheme.typography.titleLarge, color = color, fontWeight = FontWeight.Bold)
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = ComposeV2Colors.OnSurfaceVariant)
    }
}

@Composable
private fun ScanIssueCard(
    issue: ScanIssue,
    onCopy: () -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = ComposeV2Colors.Surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, ComposeV2Colors.CardBorder)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    RiskBadge(issue.riskLevel)
                    Column {
                        Text(
                            text = issue.issueType,
                            style = MaterialTheme.typography.labelMedium,
                            color = ComposeV2Colors.TerminalPurple,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${issue.filePath.substringAfterLast("/")}:${issue.lineNumber}",
                            style = MaterialTheme.typography.labelSmall,
                            color = ComposeV2Colors.OnSurfaceVariant
                        )
                    }
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatusBadge(issue.status)
                    IconButton(onClick = { isExpanded = !isExpanded }) {
                        Icon(
                            imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = "Expand",
                            tint = ComposeV2Colors.OnSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Description
            Text(
                text = issue.description,
                style = MaterialTheme.typography.bodySmall,
                color = ComposeV2Colors.OnSurface
            )

            // Expandable Content
            AnimatedVisibility(visible = isExpanded) {
                Column {
                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = ComposeV2Colors.CardBorder)
                    Spacer(modifier = Modifier.height(12.dp))

                    // File Path
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(Icons.Default.Folder, contentDescription = null, tint = ComposeV2Colors.OnSurfaceVariant, modifier = Modifier.size(14.dp))
                        Text(
                            text = issue.filePath,
                            style = MaterialTheme.typography.labelSmall,
                            color = ComposeV2Colors.OnSurfaceVariant,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Code Snippet (Before)
                    Text("❌ Before (v1)", style = MaterialTheme.typography.labelSmall, color = ComposeV2Colors.DiffRed, fontWeight = FontWeight.Bold)
                    CodeBlock(issue.codeSnippet, ComposeV2Colors.DiffRedBg, ComposeV2Colors.DiffRed)

                    Spacer(modifier = Modifier.height(8.dp))

                    // Fix Snippet (After)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("✅ After (v2)", style = MaterialTheme.typography.labelSmall, color = ComposeV2Colors.DiffGreen, fontWeight = FontWeight.Bold)
                        IconButton(onClick = onCopy) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = ComposeV2Colors.TerminalPurple, modifier = Modifier.size(16.dp))
                        }
                    }
                    CodeBlock(issue.fixSnippet, ComposeV2Colors.DiffGreenBg, ComposeV2Colors.DiffGreen)
                }
            }
        }
    }
}

@Composable
private fun RiskBadge(riskLevel: RiskLevel) {
    Surface(
        color = riskLevel.color.copy(alpha = 0.15f),
        shape = RoundedCornerShape(4.dp)
    ) {
        Text(
            text = riskLevel.emoji + " " + riskLevel.displayName,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            style = MaterialTheme.typography.labelSmall,
            color = riskLevel.color,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun StatusBadge(status: CompatibilityStatus) {
    Surface(
        color = status.color.copy(alpha = 0.15f),
        shape = RoundedCornerShape(4.dp)
    ) {
        Text(
            text = status.emoji + " " + status.displayName,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            style = MaterialTheme.typography.labelSmall,
            color = status.color
        )
    }
}

// ─────────────────────────────────────────────────────────────────
// Tab 1: Dispatcher Guide / Dispatcher 指南
// ─────────────────────────────────────────────────────────────────

@Composable
private fun DispatcherGuideTab(
    state: ComposeTestingV2State,
    viewModel: ComposeTestingV2ViewModel
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(ComposeV2Colors.Background)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = ComposeV2Colors.Surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, ComposeV2Colors.CardBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.Schedule, contentDescription = null, tint = ComposeV2Colors.TerminalPurple)
                        Text("Dispatcher Behavior Guide / Dispatcher 行为对比指南", style = MaterialTheme.typography.titleMedium, color = ComposeV2Colors.OnSurface, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("UnconfinedTestDispatcher (v1) vs StandardTestDispatcher (v2) — 核心差异是任务执行时序：v1 立即执行，v2 队列等待调度", style = MaterialTheme.typography.bodySmall, color = ComposeV2Colors.OnSurfaceVariant)
                }
            }
        }

        items(state.dispatcherGuideItems) { item ->
            DispatcherGuideCard(item)
        }

        if (state.dispatcherGuideItems.isEmpty()) {
            item {
                Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = ComposeV2Colors.Surface), border = androidx.compose.foundation.BorderStroke(1.dp, ComposeV2Colors.CardBorder)) {
                    Column(modifier = Modifier.fillMaxWidth().padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = ComposeV2Colors.TerminalPurple, modifier = Modifier.size(32.dp))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Loading Dispatcher guide...", color = ComposeV2Colors.OnSurfaceVariant)
                    }
                }
            }
        }
    }
}

@Composable
private fun DispatcherGuideCard(item: DispatcherGuideItem) {
    var isExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = ComposeV2Colors.Surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, ComposeV2Colors.CardBorder)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = item.title, style = MaterialTheme.typography.titleSmall, color = ComposeV2Colors.OnSurface, fontWeight = FontWeight.Bold)
                    Text(text = item.titleEn, style = MaterialTheme.typography.labelSmall, color = ComposeV2Colors.OnSurfaceVariant)
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    RiskBadge(item.riskLevel)
                    IconButton(onClick = { isExpanded = !isExpanded }) {
                        Icon(if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore, contentDescription = "Expand", tint = ComposeV2Colors.OnSurfaceVariant)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(text = item.description, style = MaterialTheme.typography.bodySmall, color = ComposeV2Colors.OnSurfaceVariant)

            AnimatedVisibility(visible = isExpanded) {
                Column {
                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = ComposeV2Colors.CardBorder)
                    Spacer(modifier = Modifier.height(12.dp))

                    // v1 vs v2 Behavior
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("❌ v1 (Unconfined)", style = MaterialTheme.typography.labelSmall, color = ComposeV2Colors.DiffRed, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(item.v1Behavior, style = MaterialTheme.typography.bodySmall, color = ComposeV2Colors.OnSurface)
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text("✅ v2 (Standard)", style = MaterialTheme.typography.labelSmall, color = ComposeV2Colors.DiffGreen, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(item.v2Behavior, style = MaterialTheme.typography.bodySmall, color = ComposeV2Colors.OnSurface)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Timing Diagrams
                    Text("📊 Timing Diagram / 时序图", style = MaterialTheme.typography.labelSmall, color = ComposeV2Colors.TerminalPurple, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("v1: ${item.timingDiagramV1}", style = MaterialTheme.typography.labelSmall, color = ComposeV2Colors.DiffRed)
                    Text("v2: ${item.timingDiagramV2}", style = MaterialTheme.typography.labelSmall, color = ComposeV2Colors.DiffGreen)

                    Spacer(modifier = Modifier.height(12.dp))

                    // Failure Scenario
                    Text("⚠️ Typical Failure Scenario / 典型失败场景", style = MaterialTheme.typography.labelSmall, color = ComposeV2Colors.TerminalYellow, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(item.failureScenario, style = MaterialTheme.typography.bodySmall, color = ComposeV2Colors.OnSurface)

                    Spacer(modifier = Modifier.height(12.dp))

                    // Fix Guidance
                    Text("🛠 Fix Guidance / 修复指导", style = MaterialTheme.typography.labelSmall, color = ComposeV2Colors.DiffGreen, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(item.fixGuidance, style = MaterialTheme.typography.bodySmall, color = ComposeV2Colors.OnSurface)
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────
// Tab 2: Espresso Coordination / Espresso 协调
// ─────────────────────────────────────────────────────────────────

@Composable
private fun EspressoCoordinationTab(
    state: ComposeTestingV2State,
    viewModel: ComposeTestingV2ViewModel
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(ComposeV2Colors.Background)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = ComposeV2Colors.Surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, ComposeV2Colors.CardBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.IntegrationInstructions, contentDescription = null, tint = ComposeV2Colors.TerminalPurple)
                        Text("Espresso + Compose Coordination / Espresso 协调指南", style = MaterialTheme.typography.titleMedium, color = ComposeV2Colors.OnSurface, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("v2 下 Compose 和 Espresso 测试共享同一 TestScheduler，需显式同步。launchFragmentInContainer + FragmentComposeRule 配置需更新。", style = MaterialTheme.typography.bodySmall, color = ComposeV2Colors.OnSurfaceVariant)
                }
            }
        }

        items(state.espressoGuideItems) { item ->
            EspressoGuideCard(
                item = item,
                onCopy = { viewModel.sendIntent(CopyFixCode(item.afterCode)) }
            )
        }

        if (state.espressoGuideItems.isEmpty()) {
            item {
                Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = ComposeV2Colors.Surface), border = androidx.compose.foundation.BorderStroke(1.dp, ComposeV2Colors.CardBorder)) {
                    Column(modifier = Modifier.fillMaxWidth().padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = ComposeV2Colors.TerminalPurple, modifier = Modifier.size(32.dp))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Loading Espresso guide...", color = ComposeV2Colors.OnSurfaceVariant)
                    }
                }
            }
        }
    }
}

@Composable
private fun EspressoGuideCard(
    item: EspressoGuideItem,
    onCopy: () -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = ComposeV2Colors.Surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, ComposeV2Colors.CardBorder)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = item.title, style = MaterialTheme.typography.titleSmall, color = ComposeV2Colors.OnSurface, fontWeight = FontWeight.Bold)
                    Text(text = item.titleEn, style = MaterialTheme.typography.labelSmall, color = ComposeV2Colors.OnSurfaceVariant)
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    RiskBadge(item.riskLevel)
                    IconButton(onClick = { isExpanded = !isExpanded }) {
                        Icon(if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore, contentDescription = "Expand", tint = ComposeV2Colors.OnSurfaceVariant)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(text = item.description, style = MaterialTheme.typography.bodySmall, color = ComposeV2Colors.OnSurfaceVariant)

            AnimatedVisibility(visible = isExpanded) {
                Column {
                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = ComposeV2Colors.CardBorder)
                    Spacer(modifier = Modifier.height(12.dp))

                    // Steps
                    Text("📋 Migration Steps / 迁移步骤", style = MaterialTheme.typography.labelSmall, color = ComposeV2Colors.TerminalPurple, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    item.stepsCn.forEachIndexed { index, step ->
                        Text("${index + 1}. $step", style = MaterialTheme.typography.bodySmall, color = ComposeV2Colors.OnSurface)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Before Code
                    Text("❌ Before (v1)", style = MaterialTheme.typography.labelSmall, color = ComposeV2Colors.DiffRed, fontWeight = FontWeight.Bold)
                    CodeBlock(item.beforeCode, ComposeV2Colors.DiffRedBg, ComposeV2Colors.DiffRed)

                    Spacer(modifier = Modifier.height(8.dp))

                    // After Code
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("✅ After (v2)", style = MaterialTheme.typography.labelSmall, color = ComposeV2Colors.DiffGreen, fontWeight = FontWeight.Bold)
                        IconButton(onClick = onCopy) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = ComposeV2Colors.TerminalPurple, modifier = Modifier.size(16.dp))
                        }
                    }
                    CodeBlock(item.afterCode, ComposeV2Colors.DiffGreenBg, ComposeV2Colors.DiffGreen)

                    if (item.notes.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("💡 ${item.notes}", style = MaterialTheme.typography.bodySmall, color = ComposeV2Colors.TerminalYellow)
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────
// Tab 3: CI Compliance / CI 合规检测
// ─────────────────────────────────────────────────────────────────

@Composable
private fun CIComplianceTab(
    state: ComposeTestingV2State,
    viewModel: ComposeTestingV2ViewModel
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(ComposeV2Colors.Background)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = ComposeV2Colors.Surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, ComposeV2Colors.CardBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = ComposeV2Colors.TerminalPurple)
                        Text("CI Compliance Checklist / CI 合规检查清单", style = MaterialTheme.typography.titleMedium, color = ComposeV2Colors.OnSurface, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(8.dp))

                    if (state.ciChecklistItems.isNotEmpty()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(state.ciCompliancePassed.toString(), style = MaterialTheme.typography.titleLarge, color = ComposeV2Colors.DiffGreen, fontWeight = FontWeight.Bold)
                                Text("✅ Passed", style = MaterialTheme.typography.labelSmall, color = ComposeV2Colors.OnSurfaceVariant)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(state.ciComplianceFailed.toString(), style = MaterialTheme.typography.titleLarge, color = ComposeV2Colors.DiffRed, fontWeight = FontWeight.Bold)
                                Text("🔴 Failed", style = MaterialTheme.typography.labelSmall, color = ComposeV2Colors.OnSurfaceVariant)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(state.ciComplianceWarning.toString(), style = MaterialTheme.typography.titleLarge, color = ComposeV2Colors.TerminalYellow, fontWeight = FontWeight.Bold)
                                Text("⚠️ Warning", style = MaterialTheme.typography.labelSmall, color = ComposeV2Colors.OnSurfaceVariant)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Progress Bar
                        val total = state.ciChecklistItems.size
                        val progress = if (total > 0) state.ciCompliancePassed.toFloat() / total else 0f
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                            color = ComposeV2Colors.DiffGreen,
                            trackColor = ComposeV2Colors.SurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Compliance: ${(progress * 100).toInt()}%",
                            style = MaterialTheme.typography.labelSmall,
                            color = ComposeV2Colors.OnSurfaceVariant
                        )
                    }
                }
            }
        }

        // Gradle Plugin Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = ComposeV2Colors.Surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, ComposeV2Colors.CardBorder)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("🛠 Gradle Plugin Configuration / Gradle 插件配置", style = MaterialTheme.typography.titleSmall, color = ComposeV2Colors.OnSurface, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    CodeBlock(
                        """// build.gradle.kts (root project)
plugins {
    id("com.android.compose-testing-v2-check") version "1.0.0"
}

// settings.gradle.kts
pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
    }
}

// ✅ CI will block builds that don't pass v2 compliance
// ❌ Non-compliant builds → failure + detailed report""",
                        ComposeV2Colors.SurfaceVariant,
                        ComposeV2Colors.OnSurface
                    )
                }
            }
        }


        // CI Checklist Items
        items(state.ciChecklistItems) { item ->
            CIChecklistCard(
                item = item,
                onToggle = { checked ->
                    viewModel.sendIntent(ToggleChecklistItem(item.id, checked))
                }
            )
        }
    }
}

@Composable
private fun CIChecklistCard(
    item: CIChecklistItem,
    onToggle: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (item.isChecked) ComposeV2Colors.DiffGreenBg.copy(alpha = 0.05f) else ComposeV2Colors.Surface
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (item.isChecked) ComposeV2Colors.DiffGreen.copy(alpha = 0.3f) else ComposeV2Colors.CardBorder
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = item.title, style = MaterialTheme.typography.bodyMedium, color = ComposeV2Colors.OnSurface, fontWeight = FontWeight.Medium)
                    Surface(
                        color = item.riskLevel.color.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(text = item.riskLevel.displayName, modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp), style = MaterialTheme.typography.labelSmall, color = item.riskLevel.color, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = item.description, style = MaterialTheme.typography.bodySmall, color = ComposeV2Colors.OnSurfaceVariant)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "[${item.category}]", style = MaterialTheme.typography.labelSmall, color = ComposeV2Colors.TerminalPurple)
            }
            Checkbox(
                checked = item.isChecked,
                onCheckedChange = onToggle,
                colors = CheckboxDefaults.colors(
                    checkedColor = ComposeV2Colors.DiffGreen,
                    uncheckedColor = ComposeV2Colors.OnSurfaceVariant
                )
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────
// Tab 4: KMP + Migration Guide / KMP + 迁移指南
// ─────────────────────────────────────────────────────────────────

@Composable
private fun KPPUseGuideTab(
    state: ComposeTestingV2State,
    viewModel: ComposeTestingV2ViewModel
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(ComposeV2Colors.Background)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = ComposeV2Colors.Surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, ComposeV2Colors.CardBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.DeveloperMode, contentDescription = null, tint = ComposeV2Colors.TerminalPurple)
                        Text("KMP + Migration Guide / KMP + 迁移指南", style = MaterialTheme.typography.titleMedium, color = ComposeV2Colors.OnSurface, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Compose Multiplatform Testing v2 配置 + composeTestRule → createComposeRule 完整迁移路径", style = MaterialTheme.typography.bodySmall, color = ComposeV2Colors.OnSurfaceVariant)
                }
            }
        }

        // createComposeRule Migration Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = ComposeV2Colors.Surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, ComposeV2Colors.CardBorder)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.ArrowForward, contentDescription = null, tint = ComposeV2Colors.TerminalPurple)
                        Text("composeTestRule → createComposeRule() / 完整迁移指南", style = MaterialTheme.typography.titleSmall, color = ComposeV2Colors.OnSurface, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("❌ Before (v1)", style = MaterialTheme.typography.labelSmall, color = ComposeV2Colors.DiffRed, fontWeight = FontWeight.Bold)
                    CodeBlock(
                        """import androidx.compose.ui.test.junit4.composeTestRule

class LoginScreenTest {
    @get:Rule
    val composeTestRule = composeTestRule

    @Test
    fun loginScreen_displaysFields() {
        composeTestRule.setContent { LoginScreen() }
        composeTestRule.onNodeWithText("Login")
            .assertIsDisplayed()
    }
}""",
                        ComposeV2Colors.DiffRedBg,
                        ComposeV2Colors.DiffRed
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("✅ After (v2)", style = MaterialTheme.typography.labelSmall, color = ComposeV2Colors.DiffGreen, fontWeight = FontWeight.Bold)
                    CodeBlock(
                        """import androidx.compose.ui.test.createComposeRule

class LoginScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun loginScreen_displaysFields() {
        composeTestRule.setContent { LoginScreen() }
        composeTestRule.onNodeWithText("Login")
            .assertIsDisplayed()
    }
}""",
                        ComposeV2Colors.DiffGreenBg,
                        ComposeV2Colors.DiffGreen
                    )
                }
            }
        }

        // KMP Guide Items
        items(state.kmpGuideItems) { item ->
            KMPGuideCard(item)
        }

        // Migration Path Section
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = ComposeV2Colors.Surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, ComposeV2Colors.CardBorder)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.Route, contentDescription = null, tint = ComposeV2Colors.TerminalPurple)
                        Text("Complete Migration Path / 完整迁移路径", style = MaterialTheme.typography.titleSmall, color = ComposeV2Colors.OnSurface, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("8 步完成 Compose Testing v2 迁移", style = MaterialTheme.typography.bodySmall, color = ComposeV2Colors.OnSurfaceVariant)
                }
            }
        }

        items(state.migrationPathItems) { item ->
            MigrationPathCard(item)
        }

        if (state.kmpGuideItems.isEmpty() && state.migrationPathItems.isEmpty()) {
            item {
                Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = ComposeV2Colors.Surface), border = androidx.compose.foundation.BorderStroke(1.dp, ComposeV2Colors.CardBorder)) {
                    Column(modifier = Modifier.fillMaxWidth().padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = ComposeV2Colors.TerminalPurple, modifier = Modifier.size(32.dp))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Loading KMP guide...", color = ComposeV2Colors.OnSurfaceVariant)
                    }
                }
            }
        }
    }
}

@Composable
private fun KMPGuideCard(item: KMPGuideItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = ComposeV2Colors.Surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, ComposeV2Colors.CardBorder)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(item.platform.icon, fontSize = 18.sp)
                Column(modifier = Modifier.weight(1f)) {
                    Text(item.title, style = MaterialTheme.typography.titleSmall, color = ComposeV2Colors.OnSurface, fontWeight = FontWeight.Bold)
                    Text(item.titleEn, style = MaterialTheme.typography.labelSmall, color = ComposeV2Colors.OnSurfaceVariant)
                }
                RiskBadge(item.riskLevel)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(item.description, style = MaterialTheme.typography.bodySmall, color = ComposeV2Colors.OnSurfaceVariant)
            Spacer(modifier = Modifier.height(8.dp))
            Text("💡 ${item.specialNote}", style = MaterialTheme.typography.bodySmall, color = ComposeV2Colors.TerminalYellow)
        }
    }
}

@Composable
private fun MigrationPathCard(item: MigrationPathItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = ComposeV2Colors.Surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, ComposeV2Colors.CardBorder)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(
                        color = ComposeV2Colors.TerminalPurple,
                        shape = RoundedCornerShape(50)
                    ) {
                        Text(
                            text = item.stepNumber.toString(),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(item.title, style = MaterialTheme.typography.titleSmall, color = ComposeV2Colors.OnSurface, fontWeight = FontWeight.Bold)
                        Text("⏱ ${item.estimatedTime}", style = MaterialTheme.typography.labelSmall, color = ComposeV2Colors.OnSurfaceVariant)
                    }
                }
                RiskBadge(item.riskLevel)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(item.description, style = MaterialTheme.typography.bodySmall, color = ComposeV2Colors.OnSurfaceVariant)
            if (item.codeExample.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                CodeBlock(item.codeExample, ComposeV2Colors.SurfaceVariant, ComposeV2Colors.OnSurface)
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────
// Shared Components / 共享组件
// ─────────────────────────────────────────────────────────────────

/**
 * CodeBlock — Syntax-highlighted code display component
 * 代码块展示组件
 */
@Composable
private fun CodeBlock(
    code: String,
    backgroundColor: Color,
    textColor: Color
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color(0xFF1E1E1E),
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, ComposeV2Colors.CardBorder)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            SelectionContainer {
                Text(
                    text = code,
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = FontFamily.Monospace,
                    color = Color(0xFFD4D4D4),
                    lineHeight = 18.sp
                )
            }
        }
    }
}
