package com.mvi.kenny.feature.nav3tool

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mvi.kenny.feature.nav3tool.NavToolState
import com.mvi.kenny.feature.nav3tool.NavToolIntent
import com.mvi.kenny.feature.nav3tool.NavToolEffect
import com.mvi.kenny.feature.nav3tool.NavToolTab
import com.mvi.kenny.feature.nav3tool.NavFileIssue
import com.mvi.kenny.feature.nav3tool.ScanProgress
import com.mvi.kenny.feature.nav3tool.ScanStatus
import com.mvi.kenny.feature.nav3tool.NavLifecycleEvent
import com.mvi.kenny.feature.nav3tool.BackstackSnapshot
import com.mvi.kenny.feature.nav3tool.BackstackItem
import com.mvi.kenny.feature.nav3tool.MigrationPriority
import com.mvi.kenny.feature.nav3tool.MigrationPreview
import com.mvi.kenny.feature.nav3tool.CodeChange
import com.mvi.kenny.feature.nav3tool.ChangeType
import com.mvi.kenny.feature.nav3tool.KmpPlatform
import com.mvi.kenny.feature.nav3tool.NavToolContract
import com.mvi.kenny.feature.nav3tool.NavToolContract.Colors as NavColors
import kotlinx.coroutines.flow.collectLatest
import java.text.SimpleDateFormat
import java.util.*

/**
 * ============================================================
 * NavToolScreen — Navigation 3 迁移工具主屏幕
 * ============================================================
 * PRD-031 | Navigation 3 迁移与多平台工具套件
 *
 * 6 功能模块 Tab：
 * - DETECTION  → Nav 2 → Nav 3 自动检测报告
 * - MIGRATION  → AI 辅助迁移引擎
 * - VISUALIZER → 可视化调试面板（BackStack / 路由树 / 动画预览）
 * - TEMPLATE   → KMP 通用路由模板生成器
 * - SNAPSHOT   → BackStack 快照与回放
 * - LIFECYCLE  → Navigation 3 生命周期感知调试器
 *
 * @param viewModel NavToolViewModel 实例
 * @param onNavigateTo 路由导航回调
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NavToolScreen(
    viewModel: NavToolViewModel = viewModel(),
    onNavigateTo: ((String) -> Unit)? = null
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    // Collect effects / 收集副作用
    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is NavToolEffect.ShowSnackbar -> snackbarHostState.showSnackbar(effect.message)
                is NavToolEffect.ShowError -> { /* Error dialog */ }
                else -> {}
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Navigation 3 迁移工具", fontWeight = FontWeight.Medium) },
                actions = {
                    IconButton(onClick = { viewModel.processIntent(NavToolIntent.ExportScanReport) }) {
                        Icon(Icons.Default.Download, contentDescription = "导出报告")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Tab Row / Tab 切换栏
            NavToolTabRow(
                selectedTab = state.currentTab,
                onTabSelected = { viewModel.processIntent(NavToolIntent.SwitchTab(it)) }
            )

            // Tab Content / Tab 内容区
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                when (state.currentTab) {
                    NavToolTab.DETECTION -> DetectionTabContent(state, viewModel::processIntent)
                    NavToolTab.MIGRATION -> MigrationTabContent(state, viewModel::processIntent)
                    NavToolTab.VISUALIZER -> VisualizerTabContent(state, viewModel::processIntent)
                    NavToolTab.TEMPLATE -> TemplateTabContent(state, viewModel::processIntent)
                    NavToolTab.SNAPSHOT -> SnapshotTabContent(state, viewModel::processIntent)
                    NavToolTab.LIFECYCLE -> LifecycleTabContent(state, viewModel::processIntent)
                }
            }
        }
    }
}

// ============================================================
// Tab Row / Tab 切换栏
// ============================================================

@Composable
private fun NavToolTabRow(
    selectedTab: NavToolTab,
    onTabSelected: (NavToolTab) -> Unit
) {
    ScrollableTabRow(
        selectedTabIndex = NavToolTab.entries.indexOf(selectedTab),
        edgePadding = 0.dp,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        NavToolTab.entries.forEach { tab ->
            Tab(
                selected = selectedTab == tab,
                onClick = { onTabSelected(tab) },
                text = {
                    Text(
                        text = tab.title,
                        fontSize = 13.sp,
                        fontWeight = if (selectedTab == tab) FontWeight.Bold else FontWeight.Normal
                    )
                },
                icon = {
                    Icon(
                        imageVector = getTabIcon(tab),
                        contentDescription = tab.title,
                        modifier = Modifier.size(20.dp)
                    )
                }
            )
        }
    }
}

private fun getTabIcon(tab: NavToolTab): ImageVector = when (tab) {
    NavToolTab.DETECTION -> Icons.Default.Search
    NavToolTab.MIGRATION -> Icons.Default.AutoFixHigh
    NavToolTab.VISUALIZER -> Icons.Default.Dashboard
    NavToolTab.TEMPLATE -> Icons.Default.Description
    NavToolTab.SNAPSHOT -> Icons.Default.History
    NavToolTab.LIFECYCLE -> Icons.Default.Timeline
}

// ============================================================
// Detection Tab / 检测报告模块
// ============================================================

@Composable
private fun DetectionTabContent(
    state: NavToolState,
    onIntent: (NavToolIntent) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {

        // Scan Control Card / 扫描控制卡片
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Nav 2 → Nav 3 迁移检测",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "扫描项目中的 NavHostFragment、NavGraph XML、NavArgs 用法，生成迁移优先级报告。",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))

                if (state.scanProgress.status == ScanStatus.SCANNING) {
                    LinearProgressIndicator(
                        progress = { state.scanProgress.progress },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "正在扫描: ${state.scanProgress.currentFile}",
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = FontFamily.Monospace,
                        maxLines = 1
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    TextButton(onClick = { onIntent(NavToolIntent.CancelScan) }) {
                        Text("取消")
                    }
                } else {
                    Button(
                        onClick = { onIntent(NavToolIntent.StartScan) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Search, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (state.scanResults.isEmpty()) "开始扫描" else "重新扫描")
                    }
                }
            }
        }

        // Summary chips / 优先级汇总
        if (state.scanResults.isNotEmpty()) {
            val p0Count = state.scanResults.count { it.priority == MigrationPriority.P0 }
            val p1Count = state.scanResults.count { it.priority == MigrationPriority.P1 }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                PriorityChip("P0 必须迁移: $p0Count", Color(0xFFB00020), Modifier.weight(1f))
                PriorityChip("P1 建议迁移: $p1Count", Color(0xFFFF6D00), Modifier.weight(1f))
            }

            // Issue List / 问题列表
            state.scanResults.forEach { issue ->
                IssueCard(issue = issue, onClick = { onIntent(NavToolIntent.SelectIssue(issue)) })
            }
        }
    }
}

@Composable
private fun PriorityChip(label: String, color: Color, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = color.copy(alpha = 0.15f)
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            color = color,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp
        )
    }
}

@Composable
private fun IssueCard(issue: NavFileIssue, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = Color(issue.priority.color).copy(alpha = 0.15f)
                ) {
                    Text(
                        text = issue.priority.label,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        color = Color(issue.priority.color),
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Text(
                        text = issue.issueType.name,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = issue.filePath,
                style = MaterialTheme.typography.bodySmall,
                fontFamily = FontFamily.Monospace,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(4.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                color = NavColors.CodeBackground
            ) {
                Text(
                    text = issue.codeSnippet,
                    modifier = Modifier.padding(8.dp),
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = FontFamily.Monospace,
                    color = Color(0xFFD4D4D4),
                    maxLines = 3
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "第 ${issue.lineNumber} 行",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "预估 ${issue.estimatedMinutes} 分钟",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// ============================================================
// Migration Tab / 迁移引擎模块
// ============================================================

@Composable
private fun MigrationTabContent(
    state: NavToolState,
    onIntent: (NavToolIntent) -> Unit
) {
    var inputCode by remember { mutableStateOf("") }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {

        // Code Input Card / 代码输入卡片
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "粘贴待迁移的 Nav 2 代码",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = inputCode,
                    onValueChange = { inputCode = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp),
                    placeholder = {
                        Text(
                            "例如：\nnavController.navigate(\"home\")\nnavController.navigate(\"detail/\$id\")",
                            style = MaterialTheme.typography.bodySmall
                        )
                    },
                    textStyle = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                    maxLines = 10
                )

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = { onIntent(NavToolIntent.PreviewMigration(inputCode)) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = inputCode.isNotBlank() && !state.isLoading
                ) {
                    if (state.isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Icon(Icons.Default.AutoFixHigh, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("AI 迁移预览")
                }
            }
        }

        // Migration Preview / 迁移预览
        state.migrationPreview?.let { preview ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "迁移预览",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        if (preview.hasAiSuggestion) {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = MaterialTheme.colorScheme.primaryContainer
                            ) {
                                Text(
                                    text = "AI 建议",
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Original Code / 原始代码
                    Text(
                        "原始代码",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error
                    )
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF2D2D2D)
                    ) {
                        Text(
                            text = preview.originalCode,
                            modifier = Modifier.padding(12.dp),
                            style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                            color = Color(0xFFFF6B6B)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Migrated Code / 迁移后代码
                    Text(
                        "迁移后代码",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF4CAF50)
                    )
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF2D2D2D)
                    ) {
                        Text(
                            text = preview.migratedCode,
                            modifier = Modifier.padding(12.dp),
                            style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                            color = Color(0xFF98C379)
                        )
                    }

                    // Migration Notes / 迁移说明
                    if (preview.migrationNotes.isNotBlank()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                        ) {
                            Row(modifier = Modifier.padding(12.dp)) {
                                Icon(
                                    Icons.Default.Lightbulb,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = preview.migrationNotes,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { onIntent(NavToolIntent.DiscardMigration) },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("放弃")
                        }
                        Button(
                            onClick = { onIntent(NavToolIntent.ConfirmMigration) },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("确认应用")
                        }
                    }
                }
            }
        }
    }
}

// ============================================================
// Visualizer Tab / 可视化调试模块
// ============================================================

@Composable
private fun VisualizerTabContent(
    state: NavToolState,
    onIntent: (NavToolIntent) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { onIntent(NavToolIntent.RefreshBackstack) },
                modifier = Modifier.weight(1f),
                enabled = !state.isLoading
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("刷新 BackStack")
            }
            OutlinedButton(
                onClick = { onIntent(NavToolIntent.CaptureSnapshot) },
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.CameraAlt, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("捕获快照")
            }
        }

        state.backstackSnapshot?.let { snapshot ->
            // BackStack Visualization / BackStack 可视化树
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "BackStack 可视化",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = snapshot.currentRoute,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    snapshot.backstackItems.forEach { item ->
                        BackstackTreeNode(
                            item = item,
                            onClick = { onIntent(NavToolIntent.SelectBackstackNode(item.id)) }
                        )
                    }
                }
            }

            // Transition Preview / 过渡动画预览
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "过渡动画预览",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text("HomeRoute → DetailRoute", style = MaterialTheme.typography.bodyMedium)
                        Spacer(modifier = Modifier.weight(1f))
                        IconButton(onClick = { onIntent(NavToolIntent.PlayTransitionPreview) }) {
                            Icon(Icons.Default.Pause, contentDescription = "预览动画")
                        }
                    }

                    LinearProgressIndicator(
                        progress = { 0.6f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                    )
                }
            }
        } ?: run {
            // Empty State / 空状态
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.Dashboard,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "点击「刷新 BackStack」获取当前导航状态",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun BackstackTreeNode(item: BackstackItem, onClick: () -> Unit) {
    val indentPadding = (item.depth * 24).dp

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(start = indentPadding, top = 4.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(
                    if (item.isActive) Color(0xFF4CAF50) else Color(0xFF9E9E9E)
                )
        )

        Spacer(modifier = Modifier.width(8.dp))

        if (item.parentId != null) {
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(16.dp)
                    .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
            )
            Spacer(modifier = Modifier.width(8.dp))
        }

        Text(
            text = item.routeName,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (item.isActive) FontWeight.Bold else FontWeight.Normal,
            color = if (item.isActive) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.weight(1f))

        Surface(
            shape = RoundedCornerShape(4.dp),
            color = MaterialTheme.colorScheme.secondaryContainer
        ) {
            Text(
                text = item.transitionInfo,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                fontSize = 10.sp
            )
        }
    }
}

// ============================================================
// Template Tab / 模板生成模块
// ============================================================

@Composable
private fun TemplateTabContent(
    state: NavToolState,
    onIntent: (NavToolIntent) -> Unit
) {
    var yamlEditor by remember { mutableStateOf(buildDefaultYaml()) }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {

        // Route Definition Editor / 路由定义编辑器
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "路由定义（YAML）",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = yamlEditor,
                    onValueChange = {
                        yamlEditor = it
                        onIntent(NavToolIntent.UpdateRouteDef(it))
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    textStyle = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                    maxLines = 15
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "目标平台",
                    style = MaterialTheme.typography.labelMedium
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    KmpPlatform.entries.forEach { platform ->
                        FilterChip(
                            selected = platform in state.selectedPlatforms,
                            onClick = { onIntent(NavToolIntent.TogglePlatform(platform)) },
                            label = { Text(platform.displayName, fontSize = 12.sp) },
                            leadingIcon = if (platform in state.selectedPlatforms) {
                                { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                            } else null
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = { onIntent(NavToolIntent.GenerateTemplate) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !state.isLoading && state.selectedPlatforms.isNotEmpty()
                ) {
                    if (state.isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Icon(Icons.Default.Build, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("生成模板代码")
                }
            }
        }

        // Generated Templates / 生成的模板代码
        state.generatedTemplate.forEach { (platform, code) ->
            GeneratedTemplateCard(
                platform = platform,
                code = code,
                onCopy = { onIntent(NavToolIntent.CopyTemplate(platform)) }
            )
        }
    }
}

@Composable
private fun GeneratedTemplateCard(
    platform: KmpPlatform,
    code: String,
    onCopy: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = platform.displayName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onCopy) {
                    Icon(Icons.Default.ContentCopy, contentDescription = "复制")
                }
            }

            Text(
                text = platform.routeSyntax,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFF1E1E1E)
            ) {
                Text(
                    text = code,
                    modifier = Modifier.padding(12.dp),
                    style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                    color = Color(0xFFD4D4D4)
                )
            }
        }
    }
}

private fun buildDefaultYaml(): String = """
    |routes:
    |  - name: HomeRoute
    |    path: /home
    |    arguments: []
    |    deepLinks:
    |      - myapp://home
    |
    |  - name: DetailRoute
    |    path: /detail/{id}
    |    arguments:
    |      - name: id
    |        type: String
    |        required: true
    |    deepLinks:
    |      - myapp://detail/{id}
""".trimMargin()

// ============================================================
// Snapshot Tab / 快照回放模块
// ============================================================

@Composable
private fun SnapshotTabContent(
    state: NavToolState,
    onIntent: (NavToolIntent) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {

        // Playback Controls / 回放控制
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "快照回放控制",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { onIntent(NavToolIntent.StepBackward) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "后退一步")
                    }

                    FloatingActionButton(
                        onClick = {
                            if (state.isPlayingSnapshot) {
                                onIntent(NavToolIntent.PauseSnapshot)
                            } else {
                                onIntent(NavToolIntent.PlaybackSnapshot)
                            }
                        },
                        containerColor = MaterialTheme.colorScheme.primary
                    ) {
                        Icon(
                            if (state.isPlayingSnapshot) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (state.isPlayingSnapshot) "暂停" else "播放"
                        )
                    }

                    IconButton(onClick = { onIntent(NavToolIntent.StepForward) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "前进一步")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                LinearProgressIndicator(
                    progress = { if (state.isPlayingSnapshot) 0.5f else 0f },
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { onIntent(NavToolIntent.ImportSnapshot) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("导入")
                    }
                    OutlinedButton(
                        onClick = { onIntent(NavToolIntent.ExportSnapshot) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Upload, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("导出")
                    }
                }
            }
        }

        // Snapshot Timeline / 快照时间轴
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "历史快照",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(12.dp))

                state.backstackSnapshot?.let { snapshot ->
                    SnapshotTimelineItem(
                        snapshot = snapshot,
                        isSelected = state.selectedSnapshotId == snapshot.snapshotId,
                        onClick = { onIntent(NavToolIntent.SelectSnapshot(snapshot.snapshotId)) }
                    )
                } ?: Text(
                    text = "暂无快照记录，请先在「可视化调试」中捕获快照",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun SnapshotTimelineItem(
    snapshot: BackstackSnapshot,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val timeStr = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        .format(Date(snapshot.timestamp))

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer
        else MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Circle,
                contentDescription = null,
                modifier = Modifier.size(12.dp),
                tint = if (isSelected) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.outline
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = snapshot.snapshotId.take(20) + "...",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = snapshot.currentRoute,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = timeStr,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// ============================================================
// Lifecycle Tab / 生命周期调试模块
// ============================================================

@Composable
private fun LifecycleTabContent(
    state: NavToolState,
    onIntent: (NavToolIntent) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {

        // Lifecycle Monitor Controls / 生命周期监控控制
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "NavDisplay 生命周期监控",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "实时监控 NavDisplay 与 Screen 的生命周期状态映射关系，帮助理解 Navigation 3 的新生命周期语义。",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { onIntent(NavToolIntent.StartLifecycleMonitor) },
                        modifier = Modifier.weight(1f),
                        enabled = !state.isLoading
                    ) {
                        Icon(Icons.Default.PlayCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("启动监控")
                    }
                    OutlinedButton(
                        onClick = { onIntent(NavToolIntent.StopLifecycleMonitor) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.StopCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("停止监控")
                    }
                    TextButton(onClick = { onIntent(NavToolIntent.ClearLifecycleLog) }) {
                        Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("清空")
                    }
                }
            }
        }

        // Lifecycle
        // Lifecycle Event Timeline / 生命周期事件时序图
        if (state.lifecycleEvents.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "NavDisplay vs Screen 生命周期映射",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Timeline Header / 时间轴表头
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("时间", style = MaterialTheme.typography.labelSmall, modifier = Modifier.weight(1f))
                        Text("NavDisplay", style = MaterialTheme.typography.labelSmall, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                        Text("Screen", style = MaterialTheme.typography.labelSmall, modifier = Modifier.weight(1f), textAlign = TextAlign.End)
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                    // Event List / 事件列表
                    state.lifecycleEvents.forEach { event ->
                        LifecycleEventRow(event = event)
                    }
                }
            }
        } else {
            // Empty State / 空状态
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.Timeline,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "点击「启动监控」开始记录生命周期事件",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun LifecycleEventRow(event: NavLifecycleEvent) {
    val timeStr = SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault())
        .format(Date(event.timestamp))

    Column(modifier = Modifier.padding(vertical = 6.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = timeStr,
                style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                modifier = Modifier.weight(1f)
            )

            Surface(
                shape = RoundedCornerShape(4.dp),
                color = Color(0xFF1E88E5).copy(alpha = 0.15f),
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = event.navDisplayLifecycle,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 2.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF1E88E5),
                    textAlign = TextAlign.Center
                )
            }

            Surface(
                shape = RoundedCornerShape(4.dp),
                color = Color(0xFF00ACC1).copy(alpha = 0.15f),
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = event.screenLifecycle,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 2.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF00ACC1),
                    textAlign = TextAlign.End
                )
            }
        }

        Text(
            text = event.eventType,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 2.dp)
        )

        HorizontalDivider(
            modifier = Modifier.padding(top = 6.dp),
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
        )
    }
}
