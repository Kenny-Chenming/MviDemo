package com.mvi.kenny.feature.largescreen.screens

// ================================================================
// CanaryWorkflowScreen — Continuous Canary 适配工作流
// ================================================================
// Tool 3: Continuous Canary adaptation workflow manager.
//
// PRD-155: Android 17 大屏强制适配
// Manages Android API change feed and migration task board.
//
// Features:
//   - API change log with version/category filters
//   - Migration task board (TODO / In Progress / Done)
//   - API change subscription
//   - CI webhook configuration guidance
// —————————————————————————————————————————————————————————————

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mvi.kenny.feature.largescreen.APIChange
import com.mvi.kenny.feature.largescreen.LargeScreenIntent
import com.mvi.kenny.feature.largescreen.LargeScreenState
import com.mvi.kenny.feature.largescreen.MigrationTask
import com.mvi.kenny.feature.largescreen.MigrationTaskStatus
import com.mvi.kenny.feature.largescreen.SummaryCard

/**
 * ============================================================
 * CanaryWorkflowScreen — Canary 工作流主界面
 * ============================================================
 */
@Composable
fun CanaryWorkflowScreen(
    state: LargeScreenState,
    onIntent: (LargeScreenIntent) -> Unit
) {
    var selectedVersionFilter by remember { mutableIntStateOf(0) }
    var selectedCategoryFilter by remember { mutableStateOf("全部") }
    var showAddTaskDialog by remember { mutableStateOf(false) }

    val versionFilters = listOf(0, 37, 36, 35)
    val categories = listOf("全部", "Windowing", "Permissions", "Battery", "Health", "SDK")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // ─────────────────────────────────────────────────────
        // Stats Overview — 统计概览
        // ─────────────────────────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val breakingChanges = state.apiChanges.count { it.isBreakingChange }
            val subscribedChanges = state.apiChanges.count { it.isSubscribed }
            SummaryCard(
                title = "破坏性变更",
                value = breakingChanges.toString(),
                subtitle = "需优先处理",
                icon = Icons.Default.Warning,
                accentColor = Color(0xFFB3261E),
                modifier = Modifier.weight(1f)
            )
            SummaryCard(
                title = "已订阅变更",
                value = subscribedChanges.toString(),
                subtitle = "持续跟踪中",
                icon = Icons.Default.Notifications,
                accentColor = Color(0xFF6750A4),
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ─────────────────────────────────────────────────────
        // API Change Log — API 变更日志
        // ─────────────────────────────────────────────────────
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "API 变更日志",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        TextButton(onClick = {
                            val versions = state.apiChanges.map { it.version }.distinct()
                            onIntent(LargeScreenIntent.SubscribeAPIChanges(versions))
                        }) {
                            Icon(Icons.Default.Bookmark, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("订阅全部")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Version filter chips
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    versionFilters.forEach { version ->
                        FilterChip(
                            selected = selectedVersionFilter == version,
                            onClick = { selectedVersionFilter = version },
                            label = {
                                Text(if (version == 0) "全部版本" else "API $version")
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF6750A4),
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Category filter chips
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    categories.forEach { category ->
                        FilterChip(
                            selected = selectedCategoryFilter == category,
                            onClick = { selectedCategoryFilter = category },
                            label = { Text(category) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF625B71),
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Filtered API changes
                val filteredChanges = state.apiChanges.filter { change ->
                    (selectedVersionFilter == 0 || change.version == selectedVersionFilter) &&
                    (selectedCategoryFilter == "全部" || change.category == selectedCategoryFilter)
                }

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.height(300.dp)
                ) {
                    items(filteredChanges, key = { it.id }) { change ->
                        APIChangeCard(
                            change = change,
                            onSubscribe = {
                                onIntent(LargeScreenIntent.SubscribeAPIChanges(listOf(change.version)))
                            }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ─────────────────────────────────────────────────────
        // Migration Task Board — 适配任务看板
        // ─────────────────────────────────────────────────────
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "适配任务看板",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Button(
                        onClick = { showAddTaskDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6750A4))
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("添加任务")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Kanban columns
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    KanbanColumn(
                        title = "待办",
                        tasks = state.migrationTasks.filter { it.status == MigrationTaskStatus.TODO },
                        status = MigrationTaskStatus.TODO,
                        color = Color(0xFF625B71),
                        onStatusChange = { taskId, status ->
                            onIntent(LargeScreenIntent.UpdateMigrationTaskStatus(taskId, status))
                        },
                        modifier = Modifier.weight(1f)
                    )
                    KanbanColumn(
                        title = "进行中",
                        tasks = state.migrationTasks.filter { it.status == MigrationTaskStatus.IN_PROGRESS },
                        status = MigrationTaskStatus.IN_PROGRESS,
                        color = Color(0xFFF5A623),
                        onStatusChange = { taskId, status ->
                            onIntent(LargeScreenIntent.UpdateMigrationTaskStatus(taskId, status))
                        },
                        modifier = Modifier.weight(1f)
                    )
                    KanbanColumn(
                        title = "已完成",
                        tasks = state.migrationTasks.filter { it.status == MigrationTaskStatus.DONE },
                        status = MigrationTaskStatus.DONE,
                        color = Color(0xFF146B3A),
                        onStatusChange = { taskId, status ->
                            onIntent(LargeScreenIntent.UpdateMigrationTaskStatus(taskId, status))
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

/**
 * ============================================================
 * APIChangeCard — API 变更卡片
 * ============================================================
 */
@Composable
private fun APIChangeCard(
    change: APIChange,
    onSubscribe: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (change.isBreakingChange)
                Color(0xFFB3261E).copy(alpha = 0.08f)
            else
                MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (change.isBreakingChange) {
                            Box(
                                modifier = Modifier
                                    .background(Color(0xFFB3261E), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "破坏性",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White,
                                    fontSize = 10.sp
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                        }
                        Box(
                            modifier = Modifier
                                .background(Color(0xFF6750A4).copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "API ${change.version}",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF6750A4),
                                fontSize = 10.sp
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .background(Color(0xFF625B71).copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = change.category,
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF625B71),
                                fontSize = 10.sp
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = change.title,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                IconButton(
                    onClick = onSubscribe,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = if (change.isSubscribed) Icons.Default.Bookmark else Icons.Default.Bookmark,
                        contentDescription = "订阅",
                        tint = if (change.isSubscribed) Color(0xFF6750A4) else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = change.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/**
 * ============================================================
 * KanbanColumn — 看板列
 * ============================================================
 */
@Composable
private fun KanbanColumn(
    title: String,
    tasks: List<MigrationTask>,
    status: MigrationTaskStatus,
    color: Color,
    onStatusChange: (String, MigrationTaskStatus) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Box(
                modifier = Modifier
                    .background(color.copy(alpha = 0.15f), CircleShape)
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = tasks.size.toString(),
                    style = MaterialTheme.typography.labelSmall,
                    color = color,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (tasks.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .background(color.copy(alpha = 0.05f), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "暂无任务",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            tasks.forEach { task ->
                KanbanTaskCard(
                    task = task,
                    color = color,
                    onStatusChange = { newStatus -> onStatusChange(task.id, newStatus) }
                )
                Spacer(modifier = Modifier.height(4.dp))
            }
        }
    }
}

/**
 * ============================================================
 * KanbanTaskCard — 看板任务卡片
 * ============================================================
 */
@Composable
private fun KanbanTaskCard(
    task: MigrationTask,
    color: Color,
    onStatusChange: (MigrationTaskStatus) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                // Cycle through statuses
                val nextStatus = when (task.status) {
                    MigrationTaskStatus.TODO -> MigrationTaskStatus.IN_PROGRESS
                    MigrationTaskStatus.IN_PROGRESS -> MigrationTaskStatus.DONE
                    MigrationTaskStatus.DONE -> MigrationTaskStatus.TODO
                    MigrationTaskStatus.BLOCKED -> MigrationTaskStatus.TODO
                }
                onStatusChange(nextStatus)
            },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Text(
                text = task.title,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = task.description,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "P${task.priority}",
                    style = MaterialTheme.typography.labelSmall,
                    color = when (task.priority) {
                        1 -> Color(0xFFB3261E)
                        2 -> Color(0xFFF5A623)
                        else -> Color(0xFF146B3A)
                    },
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
