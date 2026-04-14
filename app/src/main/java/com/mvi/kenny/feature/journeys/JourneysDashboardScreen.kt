package com.mvi.kenny.feature.journeys

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Badge
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mvi.kenny.base.TopBarConfig
import kotlinx.coroutines.flow.collectLatest

// ================================================================
// JourneysDashboardScreen — Journeys E2E 测试工具包主仪表盘
// ================================================================
// PRD-099: Journeys for Android Studio 自动化 E2E 测试工具包
// Design Reference: memory/agency/designs/PRD-099-Journeys-E2E测试工具包.md
//
// 主仪表盘页面，展示所有 Journey 测试用例概览。
// Layout: Header → Stats Cards → Filter Row → Journey List → Quick Actions
// ================================================================

/**
 * ============================================================
 * Primary Colors (from design spec)
 * ================================================================
 */
private val JourneyPrimary = Color(0xFF6750A4)       // Material 3 Purple — 契合 Android Studio Jellyfish
private val JourneyBackground = Color(0xFFFAFAFA)     // 浅色主题背景
private val JourneySurface = Color(0xFFFFFFFF)        // Surface
private val JourneySuccess = Color(0xFF4CAF50)       // 成功绿
private val JourneyWarning = Color(0xFFFF9800)        // 警告橙
private val JourneyError = Color(0xFFF44336)         // 错误红
private val JourneyRunning = Color(0xFF2196F3)       // 运行中蓝
private val JourneyTextPrimary = Color(0xFF1F1F1F)   // 主文字
private val JourneyTextSecondary = Color(0xFF5F5F5F) // 次文字

/**
 * JourneysDashboardScreen — 主仪表盘 Composable
 *
 * @param viewModel JourneysViewModel instance
 * @param onUpdateTopBar Callback to update parent TopAppBar config
 */
@Composable
fun JourneysDashboardScreen(
    viewModel: JourneysViewModel = viewModel(),
    onUpdateTopBar: (TopBarConfig) -> Unit = {}
) {
    val state by viewModel.state.collectAsState()

    // Update parent TopBar / 更新父级 TopBar 配置
    LaunchedEffect(state.activeTab) {
        onUpdateTopBar(TopBarConfig(
            title = "Journeys E2E 测试工具包"
        ))
    }

    // Collect effects / 收集副作用
    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is JourneysEffect.ShowToast -> { /* Toast handled by parent */ }
                is JourneysEffect.JourneyRunCompleted -> { /* handled by state */ }
                is JourneysEffect.NLGenerationCompleted -> { /* handled by state */ }
                is JourneysEffect.CopiedToClipboard -> { /* handled by parent */ }
                is JourneysEffect.ShowError -> { /* Error shown in UI */ }
                is JourneysEffect.JourneyDeleted -> { /* handled by state */ }
                is JourneysEffect.JourneySaved -> { /* handled by state */ }
                is JourneysEffect.JourneyCreated -> { /* handled by state */ }
            }
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = JourneyBackground
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ─────────────────────────────────────────────────────
            // Header + New Journey Button / 标题 + 新建按钮
            // ─────────────────────────────────────────────────────
            item {
                DashboardHeader(
                    onCreateNew = { viewModel.sendIntent(JourneysIntent.CreateBlankJourney) }
                )
            }

            // ─────────────────────────────────────────────────────
            // Stats Overview Cards / 统计概览卡片
            // ─────────────────────────────────────────────────────
            item {
                StatsOverviewRow(
                    total = state.totalCount,
                    passed = state.passedCount,
                    failed = state.failedCount,
                    running = state.runningCount
                )
            }

            // ─────────────────────────────────────────────────────
            // Filter Row / 筛选行
            // ─────────────────────────────────────────────────────
            item {
                FilterRow(
                    currentFilter = state.filterStatus,
                    onFilterChange = { viewModel.sendIntent(JourneysIntent.FilterJourneys(it)) },
                    searchQuery = state.searchQuery,
                    onSearchChange = { viewModel.sendIntent(JourneysIntent.SearchJourneys(it)) }
                )
            }

            // ─────────────────────────────────────────────────────
            // Journey List / Journey 列表
            // ─────────────────────────────────────────────────────
            items(state.filteredJourneys, key = { it.id }) { journey ->
                JourneyCard(
                    journey = journey,
                    onOpen = { viewModel.sendIntent(JourneysIntent.OpenJourney(journey.id)) },
                    onRun = { viewModel.sendIntent(JourneysIntent.RunJourney(journey.id)) },
                    onDelete = { viewModel.sendIntent(JourneysIntent.DeleteJourney(journey.id)) }
                )
            }

            // ─────────────────────────────────────────────────────
            // Quick Actions / 快捷操作入口
            // ─────────────────────────────────────────────────────
            item {
                QuickActionsRow(
                    onOpenTemplates = { viewModel.sendIntent(JourneysIntent.SwitchTab(ActiveTab.Templates)) },
                    onOpenCI = { viewModel.sendIntent(JourneysIntent.SwitchTab(ActiveTab.CI)) },
                    onOpenComparison = { viewModel.sendIntent(JourneysIntent.SwitchTab(ActiveTab.Comparison)) },
                    onOpenEditor = { viewModel.sendIntent(JourneysIntent.SwitchTab(ActiveTab.Editor)) }
                )
            }

            item { Spacer(modifier = Modifier.height(20.dp)) }
        }
    }
}

// ================================================================
// Sub-Components / 子组件
// ================================================================

/**
 * Dashboard header with title and "New Journey" button.
 */
@Composable
private fun DashboardHeader(
    onCreateNew: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "Journeys E2E 测试工具包",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = JourneyTextPrimary
            )
            Text(
                text = "AI 驱动的端到端测试 · Android Studio 集成",
                style = MaterialTheme.typography.bodySmall,
                color = JourneyTextSecondary
            )
        }
        Button(
            onClick = onCreateNew,
            colors = ButtonDefaults.buttonColors(containerColor = JourneyPrimary),
            shape = RoundedCornerShape(8.dp)
        ) {
            Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("新建 Journey")
        }
    }
}

/**
 * Stats overview row — 4 stat cards: Total / Passed / Failed / Running.
 */
@Composable
private fun StatsOverviewRow(
    total: Int,
    passed: Int,
    failed: Int,
    running: Int
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StatCard(
            modifier = Modifier.weight(1f),
            label = "总计",
            value = total.toString(),
            icon = Icons.Default.Description,
            color = JourneyPrimary
        )
        StatCard(
            modifier = Modifier.weight(1f),
            label = "通过",
            value = passed.toString(),
            icon = Icons.Default.CheckCircle,
            color = JourneySuccess
        )
        StatCard(
            modifier = Modifier.weight(1f),
            label = "失败",
            value = failed.toString(),
            icon = Icons.Default.Error,
            color = JourneyError
        )
        StatCard(
            modifier = Modifier.weight(1f),
            label = "运行中",
            value = running.toString(),
            icon = Icons.Default.Refresh,
            color = JourneyRunning
        )
    }
}

/**
 * Single stat card component.
 */
@Composable
private fun StatCard(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    icon: ImageVector,
    color: Color
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = JourneySurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = JourneyTextSecondary
            )
        }
    }
}

/**
 * Filter row: status filter chips + search field.
 */
@Composable
private fun FilterRow(
    currentFilter: JourneyStatus,
    onFilterChange: (JourneyStatus) -> Unit,
    searchQuery: String,
    onSearchChange: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        // Status filter chips / 状态筛选
        SingleChoiceSegmentedButtonRow(
            modifier = Modifier.fillMaxWidth()
        ) {
            JourneyStatus.entries.forEachIndexed { index, status ->
                SegmentedButton(
                    selected = currentFilter == status,
                    onClick = { onFilterChange(status) },
                    shape = SegmentedButtonDefaults.itemShape(index = index, count = JourneyStatus.entries.size),
                    colors = SegmentedButtonDefaults.colors(
                        activeContainerColor = JourneyPrimary.copy(alpha = 0.15f),
                        activeContentColor = JourneyPrimary
                    )
                ) {
                    Text(status.name, fontSize = 12.sp)
                }
            }
        }

        // Search field / 搜索框
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("搜索 Journey... / Search journeys...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            shape = RoundedCornerShape(8.dp),
            singleLine = true
        )
    }
}

/**
 * Journey list card — single journey item.
 */
@Composable
private fun JourneyCard(
    journey: JourneyItem,
    onOpen: () -> Unit,
    onRun: () -> Unit,
    onDelete: () -> Unit
) {
    val statusColor = when (journey.status) {
        JourneyStatus.Passed -> JourneySuccess
        JourneyStatus.Failed -> JourneyError
        JourneyStatus.Running -> JourneyRunning
        JourneyStatus.All -> JourneyTextSecondary
    }

    val statusIcon = when (journey.status) {
        JourneyStatus.Passed -> Icons.Default.CheckCircle
        JourneyStatus.Failed -> Icons.Default.Error
        JourneyStatus.Running -> Icons.Default.Refresh
        JourneyStatus.All -> Icons.Default.Description
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onOpen() },
        colors = CardDefaults.cardColors(containerColor = JourneySurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Status icon / 状态图标
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(statusColor.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                if (journey.isRunning) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = statusColor,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        imageVector = statusIcon,
                        contentDescription = null,
                        tint = statusColor,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Journey info / Journey 信息
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = journey.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium,
                    color = JourneyTextPrimary,
                    fontFamily = FontFamily.Monospace
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${journey.stepCount} steps",
                        style = MaterialTheme.typography.bodySmall,
                        color = JourneyTextSecondary
                    )
                    if (journey.lastRunDurationMs > 0) {
                        Text(
                            text = " · ${journey.durationDisplay}",
                            style = MaterialTheme.typography.bodySmall,
                            color = JourneyTextSecondary
                        )
                    }
                    if (journey.lastRunTime.isNotEmpty()) {
                        Text(
                            text = " · ${journey.lastRunTime.takeLast(8)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = JourneyTextSecondary
                        )
                    }
                }
            }

            // Status badge / 状态徽章
            Surface(
                shape = RoundedCornerShape(4.dp),
                color = statusColor.copy(alpha = 0.1f)
            ) {
                Text(
                    text = journey.status.name,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = statusColor,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Action buttons / 操作按钮
            if (!journey.isRunning) {
                IconButton(onClick = onRun) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Run / 运行",
                        tint = JourneyPrimary
                    )
                }
            }

            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete / 删除",
                    tint = JourneyError.copy(alpha = 0.7f)
                )
            }
        }
    }
}

/**
 * Quick actions row — shortcuts to templates, CI, comparison, editor.
 */
@Composable
private fun QuickActionsRow(
    onOpenTemplates: () -> Unit,
    onOpenCI: () -> Unit,
    onOpenComparison: () -> Unit,
    onOpenEditor: () -> Unit
) {
    Column {
        Text(
            text = "快捷入口 / Quick Access",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Medium,
            color = JourneyTextSecondary,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                QuickActionCard(
                    icon = Icons.Default.Code,
                    label = "Journey 编辑器",
                    sublabel = "Editor",
                    color = JourneyPrimary,
                    onClick = onOpenEditor
                )
            }
            item {
                QuickActionCard(
                    icon = Icons.Default.AutoAwesome,
                    label = "模板库",
                    sublabel = "Templates",
                    color = JourneyPrimary,
                    onClick = onOpenTemplates
                )
            }
            item {
                QuickActionCard(
                    icon = Icons.Default.Settings,
                    label = "CI/CD 配置",
                    sublabel = "CI Config",
                    color = JourneyPrimary,
                    onClick = onOpenCI
                )
            }
            item {
                QuickActionCard(
                    icon = Icons.Default.Science,
                    label = "框架对比",
                    sublabel = "Comparison",
                    color = JourneyPrimary,
                    onClick = onOpenComparison
                )
            }
        }
    }
}

/**
 * Single quick action card.
 */
@Composable
private fun QuickActionCard(
    icon: ImageVector,
    label: String,
    sublabel: String,
    color: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(120.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = JourneySurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Medium,
                color = JourneyTextPrimary
            )
            Text(
                text = sublabel,
                style = MaterialTheme.typography.bodySmall,
                color = JourneyTextSecondary
            )
        }
    }
}
