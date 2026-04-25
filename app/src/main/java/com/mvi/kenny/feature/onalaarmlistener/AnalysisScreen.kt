package com.mvi.kenny.feature.onalaarmlistener

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

// =============================================================
// AnalysisScreen — 详细任务分析与耗电可视化
// =============================================================
/**
 * Analysis Screen / 详细任务分析与耗电可视化
 *
 * Provides detailed analysis of affected tasks:
 * - Grouped task list by type / 按类型分组的任务列表
 * - Electricity/battery consumption visualization / 耗电可视化
 * - Task detail view / 任务详情
 * - Report export / 报告导出
 *
 * @param state Current UI state / 当前 UI 状态
 * @param onIntent Intent handler / 意图处理器
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalysisScreen(
    state: OnAlarmState,
    onIntent: (OnAlarmIntent) -> Unit
) {
    var selectedTab by remember { mutableStateOf(AnalysisTab.TASKS) }

    Column(modifier = Modifier.fillMaxSize()) {
        // Tab Row / Tab 行
        TabRow(
            selectedTabIndex = selectedTab.ordinal
        ) {
            AnalysisTab.entries.forEach { tab ->
                Tab(
                    selected = selectedTab == tab,
                    onClick = { selectedTab = tab },
                    text = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(tab.label)
                            if (tab == AnalysisTab.TASKS && state.affectedTasks.isNotEmpty()) {
                                Badge { Text("${state.affectedTasks.size}") }
                            }
                        }
                    },
                    icon = {
                        Icon(
                            imageVector = when (tab) {
                                AnalysisTab.TASKS -> Icons.Default.List
                                AnalysisTab.ELECTRICITY -> Icons.Default.BatteryChargingFull
                                AnalysisTab.REPORT -> Icons.Default.Description
                            },
                            contentDescription = null
                        )
                    }
                )
            }
        }

        // Tab Content / Tab 内容
        Box(modifier = Modifier.weight(1f)) {
            when (selectedTab) {
                AnalysisTab.TASKS -> TaskListTab(
                    state = state,
                    onIntent = onIntent
                )
                AnalysisTab.ELECTRICITY -> ElectricityTab(
                    state = state
                )
                AnalysisTab.REPORT -> ReportTab(
                    state = state,
                    onIntent = onIntent
                )
            }
        }
    }
}

// =============================================================
// AnalysisTab — 分析 Tab 枚举
// =============================================================
private enum class AnalysisTab(val label: String) {
    TASKS("Tasks / 任务"),
    ELECTRICITY("Electricity / 耗电"),
    REPORT("Report / 报告")
}

// =============================================================
// TaskListTab — 任务列表 Tab
// =============================================================
/**
 * Task list tab / 任务列表 Tab
 */
@Composable
private fun TaskListTab(
    state: OnAlarmState,
    onIntent: (OnAlarmIntent) -> Unit
) {
    var selectedFilter by remember { mutableStateOf<TaskType?>(null) }
    val filteredTasks = remember(state.affectedTasks, selectedFilter) {
        if (selectedFilter != null) {
            state.affectedTasks.filter { it.taskType == selectedFilter }
        } else {
            state.affectedTasks
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Filter chips / 筛选标签
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            item {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedFilter == null,
                        onClick = { selectedFilter = null },
                        label = { Text("All / 全部") }
                    )
                    TaskType.entries.forEach { type ->
                        val count = state.taskGroups[type]?.size ?: 0
                        if (count > 0) {
                            FilterChip(
                                selected = selectedFilter == type,
                                onClick = { selectedFilter = if (selectedFilter == type) null else type },
                                label = { Text("${type.label} ($count)") }
                            )
                        }
                    }
                }
            }
        }

        if (filteredTasks.isEmpty()) {
            EmptyTaskListState()
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredTasks, key = { it.id }) { task ->
                    TaskDetailCard(
                        task = task,
                        isExpanded = state.selectedTask?.id == task.id,
                        onClick = {
                            if (state.selectedTask?.id == task.id) {
                                onIntent(OnAlarmIntent.ClearSelectedTask)
                            } else {
                                onIntent(OnAlarmIntent.SelectTask(task))
                            }
                        },
                        onMarkMigrated = { onIntent(OnAlarmIntent.MarkAsMigrated(task.id)) }
                    )
                }
            }
        }
    }
}

/**
 * Empty task list state / 空任务列表状态
 */
@Composable
private fun EmptyTaskListState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.List,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
            Text(
                text = "No tasks found / 未找到任务",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Run a scan from the Scanner tab first. / 请先从扫描器 Tab 运行扫描。",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Task detail card / 任务详情卡片
 */
@Composable
private fun TaskDetailCard(
    task: AffectedTask,
    isExpanded: Boolean,
    onClick: () -> Unit,
    onMarkMigrated: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Header / 头部
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Type icon / 类型图标
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(getTaskTypeColor(task.taskType).copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = getTaskTypeIcon(task.taskType),
                        contentDescription = null,
                        tint = getTaskTypeColor(task.taskType),
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Task info / 任务信息
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = task.taskName,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${task.className} / L${task.lineNumber}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Badges / 徽章
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    AssistChip(
                        onClick = {},
                        label = { Text(task.impactLevel.label, style = MaterialTheme.typography.labelSmall) },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = task.impactLevel.color.copy(alpha = 0.1f),
                            labelColor = task.impactLevel.color
                        ),
                        modifier = Modifier.height(24.dp)
                    )
                    if (task.isMigrated) {
                        AssistChip(
                            onClick = {},
                            label = { Text("Migrated / 已迁移", style = MaterialTheme.typography.labelSmall) },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp)
                                )
                            },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = Color(0xFF4CAF50).copy(alpha = 0.1f),
                                labelColor = Color(0xFF4CAF50)
                            ),
                            modifier = Modifier.height(24.dp)
                        )
                    }
                }
            }

            // Expanded content / 展开内容
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    HorizontalDivider()
                    Text(
                        text = "File: ${task.filePath}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Suggestion / 建议: ${task.migrationSuggestion}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    if (task.estimatedDurationMinutes > 0) {
                        Text(
                            text = "Est. duration: ${task.estimatedDurationMinutes} min / 预估时长：${task.estimatedDurationMinutes} 分钟",
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                    if (task.estimatedFrequency > 0) {
                        Text(
                            text = "Est. frequency: ${task.estimatedFrequency}/day / 预估频率：${task.estimatedFrequency}/天",
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                    if (!task.isMigrated) {
                        Button(
                            onClick = onMarkMigrated,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Mark as Migrated / 标记为已迁移")
                        }
                    }
                }
            }
        }
    }
}

// =============================================================
// ElectricityTab — 耗电可视化 Tab
// =============================================================
/**
 * Electricity/battery consumption visualization tab / 耗电可视化 Tab
 */
@Composable
private fun ElectricityTab(
    state: OnAlarmState
) {
    if (state.electricityData.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.BatteryChargingFull,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
                Text(
                    text = "No electricity data / 暂无耗电数据",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Run a scan first to see battery consumption analysis. / 请先运行扫描以查看电池消耗分析。",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Summary / 摘要
            item {
                ElectricitySummaryCard(data = state.electricityData)
            }

            // Bar chart / 柱状图
            item {
                ElectricityBarChart(data = state.electricityData)
            }

            // Individual items / 单项列表
            item {
                Text(
                    text = "Task Breakdown / 任务分解",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            items(state.electricityData) { item ->
                ElectricityItem(item = item)
            }
        }
    }
}

/**
 * Electricity summary card / 耗电摘要卡片
 */
@Composable
private fun ElectricitySummaryCard(
    data: List<ElectricityContribution>
) {
    val totalMah = data.sumOf { it.estimatedMah.toDouble() }.toFloat()

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Battery Consumption Summary / 电池消耗摘要",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "%.1f mAh".format(totalMah),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Est. Total / 预估总量",
                        style = MaterialTheme.typography.labelSmall
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${data.size}",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Tasks / 任务数",
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }
    }
}

/**
 * Electricity bar chart / 耗电柱状图
 */
@Composable
private fun ElectricityBarChart(
    data: List<ElectricityContribution>
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Consumption Distribution / 消耗分布",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            data.take(6).forEach { item ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = item.taskType.label,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.width(80.dp),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    LinearProgressIndicator(
                        progress = { item.contributionPercent / 100f },
                        modifier = Modifier
                            .weight(1f)
                            .height(16.dp),
                        color = getTaskTypeColor(item.taskType),
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                    Text(
                        text = "%.1f%%".format(item.contributionPercent),
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.width(48.dp)
                    )
                }
            }
        }
    }
}

/**
 * Electricity item row / 耗电单项行
 */
@Composable
private fun ElectricityItem(item: ElectricityContribution) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = getTaskTypeIcon(item.taskType),
                contentDescription = null,
                tint = getTaskTypeColor(item.taskType),
                modifier = Modifier.size(24.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.taskName,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = item.taskType.label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "%.2f mAh".format(item.estimatedMah),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "%.1f%%".format(item.contributionPercent),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// =============================================================
// ReportTab — 报告 Tab
// =============================================================
/**
 * Report export tab / 报告导出 Tab
 */
@Composable
private fun ReportTab(
    state: OnAlarmState,
    onIntent: (OnAlarmIntent) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Description,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Compliance Report / 合规报告",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Export a compliance report in your preferred format. The report includes overall score, task list, and migration suggestions. / 以您偏好的格式导出合规报告。报告包含总体评分、任务列表和迁移建议。",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Report summary / 报告摘要
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Report Summary / 报告摘要",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                ReportSummaryRow("Compliance Score / 合规评分", "${state.overallScore}/100")
                ReportSummaryRow("Risk Level / 风险等级", state.batteryRiskLevel.label)
                ReportSummaryRow("Total Tasks / 总任务数", "${state.affectedTasks.size}")
                ReportSummaryRow("Migrated / 已迁移", "${state.migratedCount}")
                ReportSummaryRow("Remaining / 剩余", "${state.affectedTasks.size - state.migratedCount}")
                ReportSummaryRow("High Impact / 高影响", "${state.highImpactCount}")
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Export buttons / 导出按钮
        Text(
            text = "Export Format / 导出格式",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ReportFormat.entries.forEach { format ->
                Button(
                    onClick = { onIntent(OnAlarmIntent.ExportReport(format)) },
                    enabled = !state.isLoading && state.affectedTasks.isNotEmpty(),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = when (format) {
                            ReportFormat.PDF -> Icons.Default.PictureAsPdf
                            ReportFormat.JSON -> Icons.Default.Code
                            ReportFormat.MARKDOWN -> Icons.Default.TextSnippet
                        },
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(format.name, style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }
}

@Composable
private fun ReportSummaryRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium
        )
    }
}

// =============================================================
// Helper Functions
// =============================================================
/**
 * Get icon for task type / 获取任务类型图标
 */
private fun getTaskTypeIcon(taskType: TaskType): androidx.compose.ui.graphics.vector.ImageVector {
    return when (taskType) {
        TaskType.WAKELOCK -> Icons.Default.BatteryChargingFull
        TaskType.ALARM -> Icons.Default.Alarm
        TaskType.WORK_MANAGER -> Icons.Default.WorkHistory
        TaskType.EXPEDITED -> Icons.Default.FlashOn
        TaskType.USER_INITIATED -> Icons.Default.SwapHoriz
    }
}

/**
 * Get color for task type / 获取任务类型颜色
 */
private fun getTaskTypeColor(taskType: TaskType): Color {
    return when (taskType) {
        TaskType.WAKELOCK -> Color(0xFFFF9800)
        TaskType.ALARM -> Color(0xFF2196F3)
        TaskType.WORK_MANAGER -> Color(0xFF9C27B0)
        TaskType.EXPEDITED -> Color(0xFF4CAF50)
        TaskType.USER_INITIATED -> Color(0xFF00BCD4)
    }
}
