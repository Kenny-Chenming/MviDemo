package com.mvi.kenny.feature.nav3tool

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.Animation
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.ViewColumn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.collectLatest

// =============================================================
// NavToolScreen — Navigation 3 迁移工具主界面
// PRD-238 | Jetpack Navigation 3 迁移工具包
// =============================================================
/**
 * Main screen for Navigation 3 Migration Tool / Navigation 3 迁移工具主界面
 *
 * Implements tool grid home + tool detail pattern as per design doc:
 * - Home: 10 tool cards in a 2-column grid
 * - Tool detail: tool-specific UI with back navigation
 *
 * @param viewModel ViewModel instance / ViewModel 实例
 */

// =============================================================
// Tool Icon Mapping / 工具图标映射
// =============================================================
private fun getToolIcon(toolId: ToolId): ImageVector = when (toolId) {
    ToolId.SCANNER -> Icons.Default.Radar
    ToolId.API_MAPPING -> Icons.Default.CompareArrows
    ToolId.BOTTOM_NAV -> Icons.Default.TableChart
    ToolId.STATE_MANAGEMENT -> Icons.Default.AccountTree
    ToolId.TYPE_SAFE_ARGS -> Icons.Default.Lock
    ToolId.LIST_DETAIL -> Icons.Default.ViewColumn
    ToolId.DEEP_LINK -> Icons.Default.Link
    ToolId.CI_COMPLIANCE -> Icons.Default.Verified
    ToolId.ANIMATION -> Icons.Default.Animation
    ToolId.DECISION_TREE -> Icons.Default.Help
}

private fun getToolTitle(toolId: ToolId): String = when (toolId) {
    ToolId.SCANNER -> "Nav2→Nav3 迁移扫描器"
    ToolId.API_MAPPING -> "API 对照指南"
    ToolId.BOTTOM_NAV -> "BottomNavigation 迁移 Diff"
    ToolId.STATE_MANAGEMENT -> "状态管理架构指南"
    ToolId.TYPE_SAFE_ARGS -> "NavArgs 类型安全方案"
    ToolId.LIST_DETAIL -> "List-Detail 布局模板"
    ToolId.DEEP_LINK -> "Deep Link 迁移指南"
    ToolId.CI_COMPLIANCE -> "CI 合规检测工具"
    ToolId.ANIMATION -> "动画转场配置指南"
    ToolId.DECISION_TREE -> "Nav2 vs Nav3 决策树"
}

// =============================================================
// Main Entry / 主入口
// =============================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NavToolScreen(
    viewModel: NavToolViewModel = viewModel(),
    onNavigateTo: () -> Unit = {}
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val clipboardManager = LocalClipboardManager.current

    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is NavToolEffect.ShowSnackbar -> snackbarHostState.showSnackbar(effect.message)
                is NavToolEffect.CopyToClipboard -> clipboardManager.setText(AnnotatedString(effect.text))
                is NavToolEffect.ExportReport -> snackbarHostState.showSnackbar("报告已导出: ${effect.reportPath}")
                is NavToolEffect.OpenFile -> snackbarHostState.showSnackbar("打开: ${effect.filePath}:${effect.lineNumber}")
            }
        }
    }

    state.error?.let { errorMsg ->
        AlertDialog(
            onDismissRequest = { viewModel.processIntent(NavToolIntent.DismissError) },
            title = { Text("错误 / Error") },
            text = { Text(errorMsg) },
            confirmButton = { TextButton(onClick = { viewModel.processIntent(NavToolIntent.DismissError) }) { Text("确定 / OK") } }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (state.isHome) "Nav3 迁移工具包" else getToolTitle(state.activeTool),
                        maxLines = 1, overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    if (!state.isHome) {
                        IconButton(onClick = { viewModel.processIntent(NavToolIntent.NavigateHome) }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回 / Back")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF6750A4),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        AnimatedContent(
            targetState = state.isHome,
            transitionSpec = {
                if (targetState) {
                    slideInHorizontally { it } + fadeIn() togetherWith
                    slideOutHorizontally { -it } + fadeOut()
                } else {
                    slideInHorizontally { -it } + fadeIn() togetherWith
                    slideOutHorizontally { it } + fadeOut()
                }
            },
            label = "nav_transition",
            modifier = Modifier.padding(paddingValues)
        ) { showHome ->
            if (showHome) {
                ToolGridHome(state = state, onToolClick = { tool -> viewModel.processIntent(NavToolIntent.SelectTool(tool)) })
            } else {
                ToolDetailContent(state = state, viewModel = viewModel)
            }
        }
    }
}

// =============================================================
// Home: Tool Grid / 首页
// =============================================================
@Composable
private fun ToolGridHome(state: NavToolState, onToolClick: (ToolId) -> Unit) {
    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Nav3Banner()
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(ToolId.entries) { tool ->
                ToolCard(tool = tool, isSelected = state.activeTool == tool && !state.isHome, onClick = { onToolClick(tool) })
            }
        }
    }
}

@Composable
private fun Nav3Banner() {
    Surface(color = Color(0xFF6750A4), modifier = Modifier.fillMaxWidth().padding(16.dp), shape = RoundedCornerShape(12.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("🧭 Navigation 3 迁移工具包", style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text("Jetpack Navigation 3 · 2025年11月稳定版", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.8f))
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                BadgeBox("P0 扫描器", Color(0xFFB3261E))
                BadgeBox("API 对照", Color(0xFF1E88E5))
                BadgeBox("决策树", Color(0xFF4CAF50))
            }
        }
    }
}

@Composable
private fun BadgeBox(text: String, color: Color) {
    Surface(color = color.copy(alpha = 0.2f), shape = RoundedCornerShape(4.dp)) {
        Text(text = text, style = MaterialTheme.typography.labelSmall, color = Color.White, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
    }
}

@Composable
private fun ToolCard(tool: ToolId, isSelected: Boolean, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)
            .then(if (isSelected) Modifier.border(2.dp, Color(0xFF6750A4), RoundedCornerShape(12.dp)) else Modifier),
        colors = CardDefaults.cardColors(containerColor = if (isSelected) Color(0xFFF3E8FF) else MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 4.dp else 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.Start) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = getToolIcon(tool), contentDescription = null,
                    tint = if (tool.isCore) Color(0xFF6750A4) else Color(0xFF625B71), modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(8.dp))
                if (tool.isCore) {
                    Surface(color = Color(0xFFB3261E).copy(alpha = 0.1f), shape = RoundedCornerShape(4.dp)) {
                        Text("核心", style = MaterialTheme.typography.labelSmall, color = Color(0xFFB3261E), modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp))
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(tool.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Spacer(modifier = Modifier.height(4.dp))
            Text(tool.subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), maxLines = 2, overflow = TextOverflow.Ellipsis)
        }
    }
}

// =============================================================
// Tool Detail Router / 工具详情路由
// =============================================================
@Composable
private fun ToolDetailContent(state: NavToolState, viewModel: NavToolViewModel) {
    when (state.activeTool) {
        ToolId.SCANNER -> ScannerToolContent(state = state, viewModel = viewModel)
        ToolId.API_MAPPING -> ApiMappingToolContent(state = state, viewModel = viewModel)
        ToolId.BOTTOM_NAV -> BottomNavToolContent(state = state, viewModel = viewModel)
        ToolId.STATE_MANAGEMENT -> StateManagementContent(state = state, viewModel = viewModel)
        ToolId.TYPE_SAFE_ARGS -> TypeSafeArgsContent(state = state, viewModel = viewModel)
        ToolId.LIST_DETAIL -> ListDetailContent(state = state, viewModel = viewModel)
        ToolId.DEEP_LINK -> DeepLinkContent(state = state, viewModel = viewModel)
        ToolId.CI_COMPLIANCE -> CiComplianceContent(state = state, viewModel = viewModel)
        ToolId.ANIMATION -> AnimationContent(state = state, viewModel = viewModel)
        ToolId.DECISION_TREE -> DecisionTreeContent(state = state, viewModel = viewModel)
    }
}

// =============================================================
// Tool 1: Scanner / 工具1：扫描器
// =============================================================
@Composable
private fun ScannerToolContent(state: NavToolState, viewModel: NavToolViewModel) {
    var projectPath by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
        OutlinedTextField(value = projectPath, onValueChange = { projectPath = it },
            label = { Text("项目路径 / Project Path") },
            placeholder = { Text("例如: /Users/name/workspace/MyProject") },
            modifier = Modifier.fillMaxWidth(), singleLine = true)

        Spacer(modifier = Modifier.height(12.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            TextButton(onClick = { viewModel.processIntent(NavToolIntent.StartScan(projectPath)) },
                enabled = state.scanStatus != ScanStatus.SCANNING) {
                Icon(Icons.Default.Radar, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text(if (state.scanStatus == ScanStatus.SCANNING) "扫描中... / Scanning..." else "开始扫描 / Start Scan")
            }
            if (state.scanStatus == ScanStatus.SCANNING) {
                TextButton(onClick = { viewModel.processIntent(NavToolIntent.CancelScan) }) { Text("取消 / Cancel") }
            }
        }

        if (state.scanStatus == ScanStatus.SCANNING) {
            Spacer(modifier = Modifier.height(16.dp))
            Column {
                Text("扫描进度 / Progress: ${state.scanPercentage}%", style = MaterialTheme.typography.bodySmall)
                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(progress = { state.scanProgress }, modifier = Modifier.fillMaxWidth())
            }
        }

        state.scanResult?.let { result ->
            Spacer(modifier = Modifier.height(24.dp))
            ScanResultSummary(result = result)
            Spacer(modifier = Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = { viewModel.processIntent(NavToolIntent.ExportScanReport("markdown")) }) {
                    Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("导出报告 / Export")
                }
            }

            val allIssues = listOf(
                Nav2ApiUsage("1", "app/src/main/java/com/example/ui/home/HomeFragment.kt", 34, "NavHostFragment",
                    "val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment",
                    Severity.P0_BLOCKER, "将 NavHostFragment 替换为 Compose NavDisplay", 30),
                Nav2ApiUsage("2", "app/src/main/java/com/example/ui/home/HomeFragment.kt", 45, "navController.navigate(route)",
                    "navController.navigate(R.id.action_home_to_detail, bundleOf(\"id\" to itemId))",
                    Severity.P0_BLOCKER, "改用 navigator.navigateTo(DetailKey(itemId))", 15),
                Nav2ApiUsage("3", "app/src/main/java/com/example/navigation/NavGraph.kt", 12, "navGraph.xml",
                    "<navigation xmlns:android=\"http://schemas.android.com/apk/res/android\"",
                    Severity.P0_BLOCKER, "将 XML NavGraph 重写为 Kotlin DSL (entryProvider)", 60),
                Nav2ApiUsage("4", "app/src/main/java/com/example/ui/main/MainActivity.kt", 67, "NavBackStackEntry",
                    "navController.currentBackStackEntry.observe(this) { entry -> ... }",
                    Severity.P1_HIGH, "改用 navigationState.currentEntryProvider.collectAsState()", 20),
                Nav2ApiUsage("5", "app/src/main/java/com/example/ui/main/MainActivity.kt", 89, "BottomNavigation + NavController",
                    "bottomNav.setOnItemSelectedListener { item -> navController.navigate(item.itemId) }",
                    Severity.P1_HIGH, "统一使用 navigator，BottomNavigation 不再持有独立 NavController", 45),
                Nav2ApiUsage("6", "app/src/main/java/com/example/ui/main/MainActivity.kt", 112, "navArgument + Safe Args",
                    "navArgument(name = \"userId\") { type = NavType.IntType }",
                    Severity.P1_HIGH, "改用 sealed class RouteKey 替代 Safe Args KSP 生成", 30)
            )

            Spacer(modifier = Modifier.height(16.dp))
            Text("📋 受影响文件 (${result.affectedFiles.size} 个)", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))

            allIssues.forEach { issue ->
                IssueCard(issue = issue,
                    isSelected = state.selectedIssue?.id == issue.id,
                    onClick = {
                        if (state.selectedIssue?.id == issue.id) viewModel.processIntent(NavToolIntent.ClearIssue)
                        else viewModel.processIntent(NavToolIntent.SelectIssue(issue))
                    })
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun ScanResultSummary(result: ScanResult) {
    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color(0xFFF3E8FF)), shape = RoundedCornerShape(12.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("📊 扫描结果 / Scan Result", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.SpaceEvenly, modifier = Modifier.fillMaxWidth()) {
                StatBox("文件", "${result.totalFilesScanned}", Color(0xFF1E88E5))
                StatBox("问题", "${result.totalIssuesFound}", Color(0xFFB3261E))
                StatBox("预计", "${result.estimatedTotalMinutes}min", Color(0xFF4CAF50))
            }
            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                result.issuesBySeverity.forEach { (severity, count) ->
                    if (count > 0) {
                        Surface(color = severity.color.copy(alpha = 0.15f), shape = RoundedCornerShape(8.dp)) {
                            Text(text = "${severity.label} × $count", style = MaterialTheme.typography.labelMedium, color = severity.color, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text("💡 ${result.recommendation}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
        }
    }
}

@Composable
private fun StatBox(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = color)
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
    }
}

@Composable
private fun IssueCard(issue: Nav2ApiUsage, isSelected: Boolean, onClick: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = if (isSelected) severityBg(issue.severity).copy(alpha = 0.1f) else MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(8.dp)) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Surface(color = issue.severity.color.copy(alpha = 0.15f), shape = RoundedCornerShape(4.dp)) {
                    Text(text = issue.severity.label, style = MaterialTheme.typography.labelSmall, color = issue.severity.color, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                }
                Text("⏱ ${issue.estimatedMinutes}min", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(issue.apiName, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            Text("${issue.filePath}:${issue.lineNumber}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
            if (isSelected) {
                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(8.dp))
                Text(issue.codeSnippet, style = MaterialTheme.typography.bodySmall, fontFamily = FontFamily.Monospace, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                Spacer(modifier = Modifier.height(8.dp))
                Surface(color = Color(0xFF4CAF50).copy(alpha = 0.1f), shape = RoundedCornerShape(4.dp)) {
                    Text("💡 ${issue.migrationHint}", style = MaterialTheme.typography.bodySmall, color = Color(0xFF4CAF50), modifier = Modifier.padding(8.dp))
                }
            }
        }
    }
}

private fun severityBg(s: Severity) = when (s) { Severity.P0_BLOCKER -> Color(0xFFB3261E); Severity.P1_HIGH -> Color(0xFFF57C00); Severity.P2_MEDIUM -> Color(0xFFF9A825); Severity.P3_LOW -> Color(0xFF4CAF50) }

// =============================================================
// Tool 2: API Mapping / 工具2：API 对照指南
// =============================================================
@Composable
private fun ApiMappingToolContent(state: NavToolState, viewModel: NavToolViewModel) {
    Column(modifier = Modifier.fillMaxSize()) {
        Row(modifier = Modifier.horizontalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            state.apiCategories.forEach { category ->
                FilterChip(selected = state.apiMappingFilter == category, onClick = { viewModel.processIntent(NavToolIntent.FilterApiMappings(category)) },
                    label = { Text(category) },
                    leadingIcon = if (state.apiMappingFilter == category) { { Icon(Icons.Default.FilterList, contentDescription = null, modifier = Modifier.size(16.dp)) } } else null)
            }
        }
        LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(state.filteredApiMappings) { item -> ApiMappingCard(item = item) }
        }
    }
}

@Composable
private fun ApiMappingCard(item: ApiMappingItem) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.CompareArrows, contentDescription = null, tint = Color(0xFF6750A4), modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(item.category, style = MaterialTheme.typography.labelMedium, color = Color(0xFF6750A4))
            }
            Spacer(modifier = Modifier.height(12.dp))
            Surface(color = Color(0xFFB3261E).copy(alpha = 0.08f), shape = RoundedCornerShape(8.dp)) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("📕 Nav2", style = MaterialTheme.typography.labelSmall, color = Color(0xFFB3261E))
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(item.nav2Api, style = MaterialTheme.typography.bodyMedium, fontFamily = FontFamily.Monospace, color = Color(0xFFB3261E))
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text("↓", style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(8.dp))
            Surface(color = Color(0xFF4CAF50).copy(alpha = 0.08f), shape = RoundedCornerShape(8.dp)) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("📗 Nav3", style = MaterialTheme.typography.labelSmall, color = Color(0xFF4CAF50))
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(item.nav3Api, style = MaterialTheme.typography.bodyMedium, fontFamily = FontFamily.Monospace, color = Color(0xFF4CAF50))
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(12.dp))
            Text("🔄 ${item.behaviorChange}", style = MaterialTheme.typography.bodySmall)
            Spacer(modifier = Modifier.height(4.dp))
            Surface(color = Color(0xFF1E88E5).copy(alpha = 0.08f), shape = RoundedCornerShape(4.dp)) {
                Text("📝 ${item.migrationStep}", style = MaterialTheme.typography.bodySmall, color = Color(0xFF1E88E5), modifier = Modifier.padding(8.dp))
            }
        }
    }
}

// =============================================================
// Tool 3: BottomNav Migration / 工具3：BottomNavigation 迁移 Diff
// =============================================================
@Composable
private fun BottomNavToolContent(state: NavToolState, viewModel: NavToolViewModel) {
    val bottomNavDiffs = remember {
        listOf(
            DiffEntry(1, ChangeType.CONTEXT, null, "import androidx.navigation.navArgs", "迁移前上下文 / Context before migration"),
            DiffEntry(2, ChangeType.REMOVE, "val navController = findNavController()", null, "移除 NavController — Nav3 不再使用"),
            DiffEntry(3, ChangeType.ADD, null, "val navigator = rememberNavigator()", "添加 Nav3 Navigator 实例"),
            DiffEntry(4, ChangeType.CONTEXT, null, "override fun onViewCreated(view: View, savedInstanceState: Bundle?) {", "迁移前上下文 / Context before migration"),
            DiffEntry(5, ChangeType.REMOVE, "bottomNav.setOnItemSelectedListener { item ->", null, "Nav2: BottomNavigation 持有独立 NavController"),
            DiffEntry(6, ChangeType.REMOVE, "    navController.navigate(item.itemId) { launchSingleTop = true }", null, "Nav2: 每个 tab 有独立 NavController"),
            DiffEntry(7, ChangeType.REMOVE, "    bottomNavVisibility.show = navController.currentDestination?.id != R.id.detail", null, "Nav2: 手动管理 bottom bar 显示"),
            DiffEntry(8, ChangeType.REMOVE, "}", null, "Nav2 BottomNavigation 监听器结束"),
            DiffEntry(9, ChangeType.ADD, null, "// Nav3: 统一 Navigator + NavigationState", "Nav3: 使用统一的 Navigator"),
            DiffEntry(10, ChangeType.ADD, null, "val navState = rememberNavigationState()", "Nav3: 创建 NavigationState"),
            DiffEntry(11, ChangeType.ADD, null, "bottomNav.setOnItemSelectedListener { item ->", "Nav3: BottomNavigation 仍然存在"),
            DiffEntry(12, ChangeType.ADD, null, "    val routeKey = when (item.itemId) {", "Nav3: 将 menu item ID 映射为 RouteKey"),
            DiffEntry(13, ChangeType.ADD, null, "        R.id.home -> HomeKey", "Home tab → HomeKey"),
            DiffEntry(14, ChangeType.ADD, null, "        R.id.profile -> ProfileKey", "Profile tab → ProfileKey"),
            DiffEntry(15, ChangeType.ADD, null, "        else -> return@setOnItemSelectedListener false", "未知项 → 不处理"),
            DiffEntry(16, ChangeType.ADD, null, "    }", "when 表达式结束"),
            DiffEntry(17, ChangeType.ADD, null, "    navigator.navigateTo(routeKey) { launchSingleTop = true }", "Nav3: 使用 navigator.navigateTo()"),
            DiffEntry(18, ChangeType.ADD, null, "    true", "消费点击事件"),
        )
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Surface(color = Color(0xFFF3E8FF), modifier = Modifier.fillMaxWidth().padding(16.dp), shape = RoundedCornerShape(12.dp)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("🔄 BottomNavigation 迁移 Diff", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Text("Nav2 BottomNavigation + 独立 NavController → Nav3 统一 Navigator",
                    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    DiffLegend("红行删除", Color(0xFFB3261E))
                    DiffLegend("绿行新增", Color(0xFF4CAF50))
                    DiffLegend("灰行为上下文", Color(0xFF9E9E9E))
                }
            }
        }

        LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            items(bottomNavDiffs) { entry -> DiffLine(entry = entry) }
        }
    }
}

@Composable
private fun DiffLegend(text: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(12.dp).background(color, RoundedCornerShape(2.dp)))
        Spacer(modifier = Modifier.width(4.dp))
        Text(text, style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
private fun DiffLine(entry: DiffEntry) {
    val bgColor = when (entry.changeType) {
        ChangeType.ADD -> Color(0xFF4CAF50).copy(alpha = 0.1f)
        ChangeType.REMOVE -> Color(0xFFB3261E).copy(alpha = 0.1f)
        ChangeType.CONTEXT -> Color(0xFF9E9E9E).copy(alpha = 0.05f)
    }
    val textColor = when (entry.changeType) {
        ChangeType.ADD -> Color(0xFF4CAF50)
        ChangeType.REMOVE -> Color(0xFFB3261E)
        ChangeType.CONTEXT -> Color(0xFF9E9E9E)
    }
    val prefix = when (entry.changeType) { ChangeType.ADD -> "+"; ChangeType.REMOVE -> "-"; ChangeType.CONTEXT -> " " }
    val code = entry.newLine ?: entry.oldLine ?: ""

    Surface(color = bgColor, modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)) {
            Text("$prefix", style = MaterialTheme.typography.bodyMedium, fontFamily = FontFamily.Monospace, color = textColor, modifier = Modifier.width(20.dp))
            Text(code, style = MaterialTheme.typography.bodySmall, fontFamily = FontFamily.Monospace, color = textColor)
        }
    }
}

// =============================================================
// Tool 4: State Management / 工具4：状态管理架构指南
// =============================================================
@Composable
private fun StateManagementContent(state: NavToolState, viewModel: NavToolViewModel) {
    val templates = remember {
        listOf(
            CodeTemplate("sm-1", "NavigationState + SnapshotStateList",
                "Nav3 的核心状态管理 — back stack 即 Compose state",
                """
// NavigationState definition / NavigationState 定义
class NavigationState {
    val tabStacks: SnapshotStateList<SnapshotStateList<String>> = SnapshotStateList()
    val currentEntry: StateFlow<String?> = MutableStateFlow(null)
}

// Create NavigationState / 创建 NavigationState
val navState = remember { NavigationState() }

// Access current route / 访问当前路由
val currentRoute by navState.currentEntry.collectAsState()
                """.trimIndent(), "kotlin", listOf("NavigationState", "SnapshotStateList", "back stack")),
            CodeTemplate("sm-2", "entryProvider DSL",
                "Nav3 的路由注册 DSL，替代 Nav2 的静态 NavGraph includes",
                """
// Nav3 entryProvider DSL / Nav3 entryProvider DSL
NavDisplay(
    navigationState = navState,
    entryProvider = entryProvider {
        // Register routes / 注册路由
        registerRoute(HomeKey) {
            // Composable content for HomeKey / HomeKey 的Composable内容
            HomeScreen(
                onNavigateToDetail = { id ->
                    navigator.navigateTo(DetailKey(id))
                }
            )
        }
        registerRoute(DetailKey::class) { key ->
            DetailScreen(itemId = key.itemId)
        }
    }
)
                """.trimIndent(), "kotlin", listOf("entryProvider", "registerRoute", "NavDisplay")),
            CodeTemplate("sm-3", "Tab Stacks 管理",
                "Nav3 多 tab back stack 的管理方式 — 每个 tab 有独立 stack",
                """
// Tab stacks management / Tab stacks 管理
val homeStack = SnapshotStateList<String>()
val profileStack = SnapshotStateList<String>()
navState.tabStacks.add(homeStack)
navState.tabStacks.add(profileStack)

// Navigate within tab / 在 tab 内导航
homeStack.add("home_detail_1")

// Switch tab / 切换 tab
navigator.navigateTo(ProfileKey) {
    // Pop to root of current tab before switching / 切换前先 pop 到当前 tab 根
    popCurrentTabToRoot = true
}
                """.trimIndent(), "kotlin", listOf("tabStacks", "SnapshotStateList", "multi-back-stack"))
        )
    }

    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item {
            Text("📚 Navigation 3 状态管理架构指南", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }
        item {
            Text("Nav3 的核心变化：将 Navigation State 变为 Compose State，back stack 就是普通的 SnapshotStateList。",
                style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
        }
        items(templates) { template ->
            CodeTemplateCard(template = template, onCopy = { viewModel.processIntent(NavToolIntent.CopyTemplate(template)) })
        }
    }
}

@Composable
private fun CodeTemplateCard(template: CodeTemplate, onCopy: () -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(template.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(template.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                }
                IconButton(onClick = onCopy) {
                    Icon(Icons.Default.ContentCopy, contentDescription = "复制 / Copy", tint = Color(0xFF6750A4))
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Surface(color = Color(0xFF1E1E1E), shape = RoundedCornerShape(8.dp), modifier = Modifier.clickable { expanded = !expanded }) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Kotlin · 点击 ${if (expanded) "折叠" else "展开"}",
                        style = MaterialTheme.typography.labelSmall, color = Color(0xFF9E9E9E))
                    if (expanded) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(template.code, style = MaterialTheme.typography.bodySmall, fontFamily = FontFamily.Monospace, color = Color(0xFFD4D4D4))
                    }
                }
            }
        }
    }
}

// =============================================================
// Tool 5: Type Safe Args / 工具5：NavArgs 类型安全方案
// =============================================================
@Composable
private fun TypeSafeArgsContent(state: NavToolState, viewModel: NavToolViewModel) {
    val templates = remember {
        listOf(
            CodeTemplate("ts-1", "sealed class RouteKey",
                "Nav3 的类型安全路由方案 — 替代 Safe Args KSP 生成",
                """
// Nav2 (Safe Args) / Nav2（Safe Args）
@Serializable
data class HomeFragmentArgs(val userId: Int, val name: String)
val args: HomeFragmentArgs by navArgs()

// Nav3 (RouteKey) / Nav3（RouteKey）
sealed class RouteKey(val routeName: String) {
    data object Home : RouteKey("home")
    data class Detail(val itemId: Int, val title: String) : RouteKey("detail/{itemId}") {
        companion object {
            fun create(itemId: Int, title: String) = "detail/${'$'}itemId?title=${'$'}title"
        }
    }
}

// Navigate with type-safe key / 使用类型安全 key 导航
navigator.navigateTo(RouteKey.Detail(itemId = 123, title = "Item"))
                """.trimIndent(), "kotlin", listOf("RouteKey", "sealed class", "type-safe")),
            CodeTemplate("ts-2", "Key-based Routing in entryProvider",
                "在 entryProvider 中使用 RouteKey 做路由分发",
                """
// entryProvider with RouteKey / 带 RouteKey 的 entryProvider
entryProvider {
    registerRoute(RouteKey.Home) {
        HomeScreen()
    }
    registerRoute(RouteKey.Detail::class) { key ->
        DetailScreen(itemId = key.itemId, title = key.title)
    }
}

// Route matching / 路由匹配
val currentKey = navState.currentEntryProvider.value
when (currentKey) {
    is RouteKey.Home -> HomeScreen()
    is RouteKey.Detail -> DetailScreen(itemId = currentKey.itemId)
}
                """.trimIndent(), "kotlin", listOf("entryProvider", "registerRoute", "RouteKey"))
        )
    }

    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item {
            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0)), shape = RoundedCornerShape(12.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("🔑 核心变化", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Nav2: KSP 生成 @Serializable Args 类 → 编译时类型检查",
                        style = MaterialTheme.typography.bodySmall)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Nav3: sealed class RouteKey → 运行时 key string，无代码生成，无 KSP 开销",
                        style = MaterialTheme.typography.bodySmall)
                    Spacer(modifier = Modifier.height(4.dp))
                    Surface(color = Color(0xFF4CAF50).copy(alpha = 0.1f), shape = RoundedCornerShape(4.dp)) {
                        Text("✅ Nav3 无需 Safe Args Plugin，移除 build.gradle 中的 safe-args 插件",
                            style = MaterialTheme.typography.bodySmall, color = Color(0xFF4CAF50), modifier = Modifier.padding(8.dp))
                    }
                }
            }
        }
        items(templates) { template ->
            CodeTemplateCard(template = template, onCopy = { viewModel.processIntent(NavToolIntent.CopyTemplate(template)) })
        }
    }
}

// =============================================================
// Tool 6: List-Detail Layout / 工具6：List-Detail 自适应布局模板
// =============================================================
@Composable
private fun ListDetailContent(state: NavToolState, viewModel: NavToolViewModel) {
    val template = remember {
        CodeTemplate("ld-1", "Nav3 List-Detail 完整模板",
            "Nav3 原生支持的 list-detail 自适应布局 — 替代 SlidingPaneLayout",
            """
// Nav3 List-Detail with Adaptive Layout / Nav3 自适应布局
@Composable
fun ListDetailScreen(
    list: List<Item>,
    selectedItem: Item?,
    onItemSelected: (Item) -> Unit
) {
    Row(modifier = Modifier.fillMaxSize()) {
        // List pane / 列表 pane
        ListPane(
            items = list,
            selectedItem = selectedItem,
            onItemSelected = onItemSelected,
            modifier = Modifier
                .width(360.dp)
                .fillMaxHeight()
        )

        // Detail pane / 详情 pane
        if (selectedItem != null) {
            DetailPane(
                item = selectedItem,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )
        }
    }
}

// NavigationState integration / NavigationState 集成
entryProvider {
    registerRoute(ListDetailKey::class) { key ->
        ListDetailScreen(
            list = items,
            selectedItem = items.find { it.id == key.itemId },
            onItemSelected = { item ->
                navigator.navigateTo(ListDetailKey(item.id))
            }
        )
    }
}
            """.trimIndent(), "kotlin", listOf("List-Detail", "adaptive", "multi-pane"))
    }

    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)), shape = RoundedCornerShape(12.dp)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("🆕 Nav3 原生支持", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Nav2 需要第三方 SlidingPaneLayout 才能实现 list-detail；Nav3 原生支持多 pane 自适应布局。",
                    style = MaterialTheme.typography.bodySmall)
                Spacer(modifier = Modifier.height(4.dp))
                Text("Google 官方推荐使用 Jetpack WindowManager 实现跨设备自适应。",
                    style = MaterialTheme.typography.bodySmall)
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        CodeTemplateCard(template = template, onCopy = { viewModel.processIntent(NavToolIntent.CopyTemplate(template)) })
    }
}

// =============================================================
// Tool 7: Deep Link / 工具7：Deep Link 迁移指南
// =============================================================
@Composable
private fun DeepLinkContent(state: NavToolState, viewModel: NavToolViewModel) {
    val templates = remember {
        listOf(
            CodeTemplate("dl-1", "Nav3 URI-based Deep Link",
                "Nav3 的 Deep Link 使用 URI 而非 navArgument uriPatterns",
                """
// Nav3 Deep Link / Nav3 Deep Link
entryProvider {
    registerRoute(
        route = DetailKey,
        uris = listOf(
            Uri("https://myapp.com/detail/{itemId}"),
            Uri("myapp://detail/{itemId}")
        )
    ) { key ->
        DetailScreen(itemId = key.itemId)
    }
}

// Handle deep link / 处理 Deep Link
val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://myapp.com/detail/123"))
navigator.handleDeepLink(intent)
                """.trimIndent(), "kotlin", listOf("deep-link", "URI", "intent")),
            CodeTemplate("dl-2", "Multi-back-stack Deep Link",
                "多 tab 场景下的 Deep Link 处理 — 需要指定 tab context",
                """
// Multi-back-stack deep link / 多 back-stack Deep Link
entryProvider {
    registerRoute(
        route = ProfileKey,
        tabStack = Tab.Profile, // Specify which tab stack / 指定 tab stack
        uris = listOf(Uri("https://myapp.com/profile/{userId}"))
    ) { key ->
        ProfileScreen(userId = key.userId)
    }
}

// Navigate with tab context / 带 tab context 的导航
navigator.navigateTo(
    ProfileKey(userId = 456),
    tabContext = Tab.Profile, // Pop to Profile tab root then navigate / 先 pop 到 Profile tab 根再导航
    navOptions = { launchSingleTop = true }
)
                """.trimIndent(), "kotlin", listOf("deep-link", "tab-stack", "multi-back-stack"))
        )
    }

    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item {
            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD)), shape = RoundedCornerShape(12.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("🔗 API 变化", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("📕 Nav2", style = MaterialTheme.typography.labelMedium, color = Color(0xFFB3261E))
                            Text("navArgument { uriPatterns = listOf(...) }",
                                style = MaterialTheme.typography.bodySmall, fontFamily = FontFamily.Monospace)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("📗 Nav3", style = MaterialTheme.typography.labelMedium, color = Color(0xFF4CAF50))
                            Text("uris = listOf(Uri(...))",
                                style = MaterialTheme.typography.bodySmall, fontFamily = FontFamily.Monospace)
                        }
                    }
                }
            }
        }
        items(templates) { template ->
            CodeTemplateCard(template = template, onCopy = { viewModel.processIntent(NavToolIntent.CopyTemplate(template)) })
        }
    }
}

// =============================================================
// Tool 8: CI Compliance / 工具8：CI 合规检测工具
// =============================================================
@Composable
private fun CiComplianceContent(state: NavToolState, viewModel: NavToolViewModel) {
    val gradlePluginCode = remember {
        CodeTemplate("ci-1", "Nav3Compliance Gradle Plugin",
            "检测项目中是否仍使用 Nav2 API，阻塞未合规 CI",
            """
// build.gradle.kts
plugins {
    id("com.example.nav3-compliance") version "1.0.0"
}

// nav3Compliance { }
nav3Compliance {
    failOnUnmigratedApi = true
    allowedDeprecatedUsages = listOf("navController.popBackStack()")
    reportFormat = "json"
}

// Gradle task / Gradle 任务
// ./gradlew nav3ComplianceScan
// ./gradlew nav3ComplianceReport
// ./gradlew check (包含合规检测)
            """.trimIndent(), "kotlin", listOf("CI", "Gradle Plugin", "compliance"))
    }

    val ciOutput = remember {
        CodeTemplate("ci-2", "CI 输出示例",
            "合规 vs 不合规的 CI 输出对比",
            """
# ❌ CI 失败 — 检测到 Nav2 API
[Nav3Compliance] ERROR: Found 3 unmigrated Nav2 APIs:
  - HomeFragment.kt:34 — NavHostFragment
  - MainActivity.kt:45 — navController.navigate()
  - NavGraph.kt:12 — navGraph.xml

[Nav3Compliance] FAILED: Build blocked by Nav3 compliance check.
See full report: build/reports/nav3-compliance/report.json

# ✅ CI 通过 — 全部 Nav3 合规
[Nav3Compliance] INFO: Nav3 compliance check passed.
[Nav3Compliance] INFO: All Nav2 APIs have been migrated to Nav3.
            """.trimIndent(), "text", listOf("CI", "output", "example"))
    }

    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item {
            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color(0xFFF3E8FF)), shape = RoundedCornerShape(12.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("🔒 CI 合规检测", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Gradle Plugin 检测项目中是否存在 Nav2 API 使用，阻塞未迁移代码进入 CI。",
                        style = MaterialTheme.typography.bodySmall)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Surface(color = Color(0xFFB3261E).copy(alpha = 0.1f), shape = RoundedCornerShape(4.dp)) {
                            Text("阻塞未迁移 CI", style = MaterialTheme.typography.labelSmall, color = Color(0xFFB3261E), modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                        }
                        Surface(color = Color(0xFF4CAF50).copy(alpha = 0.1f), shape = RoundedCornerShape(4.dp)) {
                            Text("支持 JSON 报告", style = MaterialTheme.typography.labelSmall, color = Color(0xFF4CAF50), modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                        }
                    }
                }
            }
        }
        item { CodeTemplateCard(template = gradlePluginCode, onCopy = { viewModel.processIntent(NavToolIntent.CopyTemplate(gradlePluginCode)) }) }
        item { CodeTemplateCard(template = ciOutput, onCopy = { viewModel.processIntent(NavToolIntent.CopyTemplate(ciOutput)) }) }
    }
}

// =============================================================
// Tool 9: Animation / 工具9：动画转场配置指南
// =============================================================
@Composable
private fun AnimationContent(state: NavToolState, viewModel: NavToolViewModel) {
    val template = remember {
        CodeTemplate("anim-1", "SpatialTransitions API",
            "Nav3 的新动画系统 — 替代 Nav2 的 fadeIn/slideIn",
            """
// Nav3 SpatialTransitions / Nav3 空间转场动画
entryProvider {
    registerRoute(
        route = HomeKey,
        // Enter transition / 入场转场
        enterTransition = {
            enterSpatial(
                duration = 300.ms,
                fade = FadeMode.In,
                slide = SlideEffect.Direction.Up
            )
        },
        // Exit transition / 出场转场
        exitTransition = {
            exitSpatial(
                duration = 200.ms,
                fade = FadeMode.Out
            )
        },
        // Pop enter (return) / 返回入场
        popEnterTransition = {
            popEnterSpatial(
                duration = 200.ms,
                fade = FadeMode.In
            )
        },
        // Pop exit (return) / 返回出场
        popExitTransition = {
            popExitSpatial(
                duration = 300.ms,
                fade = FadeMode.Out,
                slide = SlideEffect.Direction.Down
            )
        }
    ) {
        HomeScreen()
    }
}

// Nav2 → Nav3 对照 / Nav2 → Nav3 comparison
// Nav2: enterTransition = fadeIn() / slideIn()
// Nav3: enterTransition = enterSpatial(fade = FadeMode.In, slide = SlideEffect.Direction.Up)
            """.trimIndent(), "kotlin", listOf("animation", "SpatialTransitions", "transitions"))
    }

    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E1)), shape = RoundedCornerShape(12.dp)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("🎬 SpatialTransitions API", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Nav3 引入 SpatialTransitions — 完全不同于 Nav2 的动画系统。",
                    style = MaterialTheme.typography.bodySmall)
                Spacer(modifier = Modifier.height(4.dp))
                Text("不再支持 fadeIn()/fadeOut() 等简单 API，需迁移到 enterSpatial()/exitSpatial()。",
                    style = MaterialTheme.typography.bodySmall)
                Spacer(modifier = Modifier.height(8.dp))
                Surface(color = Color(0xFFF9A825).copy(alpha = 0.15f), shape = RoundedCornerShape(4.dp)) {
                    Text("⚠️ 这是一个破坏性变更，Nav2 的动画配置无法直接迁移",
                        style = MaterialTheme.typography.bodySmall, color = Color(0xFFF57C00), modifier = Modifier.padding(8.dp))
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        CodeTemplateCard(template = template, onCopy = { viewModel.processIntent(NavToolIntent.CopyTemplate(template)) })
    }
}

// =============================================================
// Tool 10: Decision Tree / 工具10：决策树
// =============================================================
@Composable
private fun DecisionTreeContent(state: NavToolState, viewModel: NavToolViewModel) {
    val currentNode = state.decisionTreeCurrentNode
    val result = state.decisionTreeResult

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        if (result != null) {
            // Show decision result / 显示决策结果
            Card(modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = result.riskLevel.color.copy(alpha = 0.1f)),
                shape = RoundedCornerShape(12.dp)) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("🎯 决策结论", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = result.riskLevel.color)
                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(color = result.riskLevel.color.copy(alpha = 0.2f), shape = RoundedCornerShape(8.dp)) {
                        Text(result.verdict, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold,
                            color = result.riskLevel.color, modifier = Modifier.padding(12.dp))
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("📝 理由: ${result.reason}", style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("📋 建议步骤:", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    result.nextSteps.forEachIndexed { index, step ->
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("${index + 1}. $step", style = MaterialTheme.typography.bodySmall)
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    TextButton(onClick = { viewModel.processIntent(NavToolIntent.ResetDecisionTree) }) {
                        Icon(Icons.Default.Refresh, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("重新测试 / Start Over")
                    }
                }
            }
        } else if (currentNode != null) {
            // Show current question / 显示当前问题
            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("📊 决策树 / Decision Tree",
                        style = MaterialTheme.typography.labelMedium, color = Color(0xFF6750A4))
                    Spacer(modifier = Modifier.height(4.dp))
                    val progress = state.decisionTreeAnswers.size.toFloat() / 5f
                    if (progress < 1f) {
                        LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth())
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(currentNode.question,
                        style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(20.dp))
                    currentNode.options.forEach { option ->
                        Spacer(modifier = Modifier.height(8.dp))
                        Card(modifier = Modifier.fillMaxWidth().clickable {
                            if (option.resultIfChosen != null) {
                                viewModel.processIntent(NavToolIntent.SelectTool(ToolId.SCANNER))
                            } else if (option.nextNodeId != null) {
                                viewModel.processIntent(NavToolIntent.AnswerDecisionTree(option.answer, option.nextNodeId))
                            }
                        }, colors = CardDefaults.cardColors(containerColor = Color(0xFFF3E8FF)), shape = RoundedCornerShape(8.dp)) {
                            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Help, contentDescription = null, tint = Color(0xFF6750A4), modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(option.answer, style = MaterialTheme.typography.bodyMedium)
                                Spacer(modifier = Modifier.weight(1f))
                                if (option.resultIfChosen != null) {
                                    Surface(color = Color(0xFF4CAF50).copy(alpha = 0.2f), shape = RoundedCornerShape(4.dp)) {
                                        Text("→ 查看结论", style = MaterialTheme.typography.labelSmall, color = Color(0xFF4CAF50), modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                    }
                                } else {
                                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = Color(0xFF625B71), modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    TextButton(onClick = { viewModel.processIntent(NavToolIntent.ResetDecisionTree) }) {
                        Text("从头开始 / Start Over")
                    }
                }
            }
        }
    }
}
