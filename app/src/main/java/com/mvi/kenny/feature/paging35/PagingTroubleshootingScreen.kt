package com.mvi.kenny.feature.paging35

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.collectLatest

/**
 * ============================================================
 * PagingTroubleshootingScreen — 踩坑排查工具屏幕
 * ============================================================
 * PRD-093 | Paging 3.5 `asState` 操作符开发工具包
 *
 * 功能：
 * - 问题场景选择（5 种典型场景 Tab）
 * - 问题诊断卡片（症状/原因/建议）
 * - 自动化检测开关
 *
 * @param viewModel PagingTroubleshootingViewModel 实例
 * @param onNavigateBack 返回上一级回调
 *
 * @author 开心果 🥜
 */
@Composable
fun PagingTroubleshootingScreen(
    viewModel: PagingTroubleshootingViewModel = viewModel(),
    onNavigateBack: () -> Unit = {}
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    // Collect effects / 收集副作用
    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is PagingTroubleshootingEffect.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(effect.message)
                }
                is PagingTroubleshootingEffect.ScanComplete -> {
                    snackbarHostState.showSnackbar("扫描完成，发现 ${effect.issuesFound} 个问题")
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("踩坑排查工具") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
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
            // ============================================================
            // 场景选择 Tab
            // ============================================================

            ScenarioTabRow(
                selectedScenario = state.selectedScenario,
                onScenarioSelected = { viewModel.processIntent(PagingTroubleshootingIntent.SelectScenario(it)) }
            )

            // ============================================================
            // 自动化检测开关
            // ============================================================

            AutoDetectToggle(
                enabled = state.autoDetectEnabled,
                isScanning = state.isScanning,
                scanProgress = state.scanProgress,
                onToggle = { viewModel.processIntent(PagingTroubleshootingIntent.ToggleAutoDetect(it)) }
            )

            // ============================================================
            // 问题诊断列表
            // ============================================================

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(vertical = 12.dp)
            ) {
                val cases = viewModel.getTroubleCases(state.selectedScenario)

                itemsIndexed(
                    items = cases,
                    key = { index, _ -> "${state.selectedScenario.name}_$index" }
                ) { index, troubleCase ->
                    val isExpanded = state.expandedCaseIndex == index

                    TroubleshootingCard(
                        title = troubleCase.title,
                        symptom = troubleCase.symptom,
                        causes = troubleCase.causes,
                        solutions = troubleCase.solutions,
                        modifier = Modifier.clickable {
                            viewModel.processIntent(PagingTroubleshootingIntent.ToggleCaseExpand(index))
                        }
                    )
                }

                // Detected issues from scan / 扫描发现的问题
                if (state.detectedIssues.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "扫描发现的问题",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    itemsIndexed(
                        items = state.detectedIssues,
                        key = { _, issue -> "${issue.filePath}_${issue.lineNumber}" }
                    ) { _, issue ->
                        DetectedIssueCard(issue = issue)
                    }
                }
            }
        }
    }
}

// ============================================================
// Sub-components / 子组件
// ============================================================

/**
 * 场景 Tab 行
 *
 * @param selectedScenario 当前选中的场景
 * @param onScenarioSelected 场景选中回调
 *
 * @author 开心果 🥜
 */
@Composable
private fun ScenarioTabRow(
    selectedScenario: TroubleScenario,
    onScenarioSelected: (TroubleScenario) -> Unit
) {
    ScrollableTabRow(
        selectedTabIndex = TroubleScenario.entries.indexOf(selectedScenario),
        edgePadding = 0.dp,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        TroubleScenario.entries.forEach { scenario ->
            Tab(
                selected = selectedScenario == scenario,
                onClick = { onScenarioSelected(scenario) },
                text = {
                    Text(
                        text = scenario.title,
                        fontSize = 12.sp,
                        fontWeight = if (selectedScenario == scenario) FontWeight.Bold else FontWeight.Normal
                    )
                },
                icon = {
                    Icon(
                        imageVector = getScenarioIcon(scenario),
                        contentDescription = scenario.title,
                        modifier = Modifier.size(18.dp)
                    )
                }
            )
        }
    }
}

private fun getScenarioIcon(scenario: TroubleScenario): androidx.compose.ui.graphics.vector.ImageVector {
    return when (scenario) {
        TroubleScenario.DUPLICATE_LOAD -> Icons.Default.ContentCopy
        TroubleScenario.STATE_LOSS -> Icons.Default.SyncProblem
        TroubleScenario.BOUNDARY_ERROR -> Icons.Default.BorderHorizontal
        TroubleScenario.ROOM_INTEGRATION -> Icons.Default.Storage
        TroubleScenario.PULL_TO_REFRESH -> Icons.Default.Refresh
    }
}

/**
 * 自动化检测开关
 *
 * @param enabled 是否开启
 * @param isScanning 是否正在扫描
 * @param scanProgress 扫描进度
 * @param onToggle 开关回调
 *
 * @author 开心果 🥜
 */
@Composable
private fun AutoDetectToggle(
    enabled: Boolean,
    isScanning: Boolean,
    scanProgress: Float,
    onToggle: (Boolean) -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.BugReport,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "自动化检测",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "开启后扫描项目并高亮问题代码",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Switch(
                    checked = enabled,
                    onCheckedChange = onToggle
                )
            }

            if (isScanning) {
                Spacer(modifier = Modifier.height(12.dp))
                LinearProgressIndicator(
                    progress = { scanProgress },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "正在扫描... ${(scanProgress * 100).toInt()}%",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * 检测到的问题卡片
 *
 * @param issue 检测到的问题
 *
 * @author 开心果 🥜
 */
@Composable
private fun DetectedIssueCard(issue: DetectedIssue) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = issue.severity.color.copy(alpha = 0.08f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Warning,
                contentDescription = null,
                tint = issue.severity.color,
                modifier = Modifier.size(18.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = issue.description,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "${issue.filePath}:${issue.lineNumber}",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            SeverityChip(level = issue.severity)
        }
    }
}
