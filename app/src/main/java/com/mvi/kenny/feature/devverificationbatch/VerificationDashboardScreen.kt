package com.mvi.kenny.feature.devverificationbatch

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mvi.kenny.base.TopBarAction
import com.mvi.kenny.base.TopBarConfig
import kotlinx.coroutines.flow.collectLatest
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * ============================================================
 * PRD-304 | Android 开发者身份验证合规批量管理平台
 * VerificationDashboardScreen — 主界面 / Main Screen
 * ============================================================
 * Main entry point for the Verification Dashboard feature.
 * Implements a tabbed UI with: Dashboard / Developers / Apps / CI-CD / Reports
 *
 * Design Reference: `memory/agency/designs/PRD-304-Android-开发者身份验证合规批量管理平台.md`
 * MVI: VerificationDashboardIntent / VerificationDashboardState / VerificationDashboardEffect
 * Bilingual comments: CN + EN
 */

// ─────────────────────────────────────────────────────────────────
// Color System (from Design Spec Section 7)
// ─────────────────────────────────────────────────────────────────
private object VerifBatchColors {
    val GoogleBlue = Color(0xFF4285F4)
    val Verified = Color(0xFF34A853)
    val Pending = Color(0xFFFBBC04)
    val Expired = Color(0xFFEA4335)
    val Failed = Color(0xFFEA4335)
    val Surface = Color(0xFFF8F9FA)
    val SurfaceDark = Color(0xFF1E1E1E)
    val OnSurface = Color(0xFF202124)
    val OnSurfaceDark = Color(0xFFE0E0E0)
    val Outline = Color(0xFFDADCE0)
    val CardBg = Color(0xFFFFFFFF)
    val CardBgDark = Color(0xFF2D2D2D)
}

/**
 * ============================================================
 * Main Screen Composable — 主界面
 * ============================================================
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VerificationDashboardScreen(
    viewModel: VerificationDashboardViewModel = viewModel(),
    onUpdateTopBar: (TopBarConfig) -> Unit = {}
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val clipboardManager = LocalClipboardManager.current

    // Update TopBar config / 更新顶部导航栏配置
    LaunchedEffect(state.activeTab, state.selectedDevelopers.size) {
        val actions = if (state.selectedDevelopers.isNotEmpty()) {
            listOf(
                TopBarAction(
                    icon = Icons.Default.Send,
                    contentDescription = "Submit Batch / 批量提交",
                    onClick = { viewModel.sendIntent(VerificationDashboardIntent.OpenWizard()) }
                ),
                TopBarAction(
                    icon = Icons.Default.Clear,
                    contentDescription = "Clear Selection / 清除选择",
                    onClick = { viewModel.sendIntent(VerificationDashboardIntent.ClearSelection) }
                )
            )
        } else emptyList()

        val title = when (state.activeTab) {
            DashboardTab.Dashboard -> "Dev验证管理 / Verification Dashboard"
            DashboardTab.Developers -> "开发者管理 / Developer Management"
            DashboardTab.Apps -> "App 管理 / App Management"
            DashboardTab.CICD -> "CI/CD 集成 / CI/CD Integration"
            DashboardTab.Reports -> "合规报告 / Compliance Reports"
        }

        onUpdateTopBar(TopBarConfig(
            title = if (state.selectedDevelopers.isNotEmpty()) {
                "已选 ${state.selectedDevelopers.size} 个 / ${state.selectedDevelopers.size} Selected"
            } else title,
            actions = actions
        ))
    }

    // Collect one-time effects / 收集一次性副作用
    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is VerificationDashboardEffect.ShowSuccess -> {
                    snackbarHostState.showSnackbar(effect.message)
                }
                is VerificationDashboardEffect.ShowError -> {
                    snackbarHostState.showSnackbar(effect.message)
                }
                is VerificationDashboardEffect.CopiedToClipboard -> {
                    clipboardManager.setText(AnnotatedString(effect.content))
                }
                is VerificationDashboardEffect.ReportExported -> {
                    snackbarHostState.showSnackbar("报告已导出: ${effect.filePath}")
                }
                else -> {}
            }
        }
    }

    // Show wizard dialog if active / 如果向导打开则显示对话框
    if (state.showWizard) {
        VerificationWizardDialog(
            state = state,
            onIntent = viewModel::sendIntent
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Tab Row / Tab 行
            TabRow(
                selectedTabIndex = state.activeTab.ordinal,
                containerColor = VerifBatchColors.GoogleBlue
            ) {
                DashboardTab.entries.forEach { tab ->
                    Tab(
                        selected = state.activeTab == tab,
                        onClick = { viewModel.sendIntent(VerificationDashboardIntent.SwitchTab(tab)) },
                        text = {
                            Text(
                                text = when (tab) {
                                    DashboardTab.Dashboard -> "📊 概览"
                                    DashboardTab.Developers -> "👤 开发者"
                                    DashboardTab.Apps -> "📱 App"
                                    DashboardTab.CICD -> "⚙️ CI/CD"
                                    DashboardTab.Reports -> "📋 报告"
                                },
                                color = if (state.activeTab == tab) Color.White else Color.White.copy(alpha = 0.7f)
                            )
                        }
                    )
                }
            }

            // Tab Content / Tab 内容
            PullToRefreshBox(
                isRefreshing = state.isRefreshing,
                onRefresh = { viewModel.sendIntent(VerificationDashboardIntent.RefreshDashboard) },
                modifier = Modifier.fillMaxSize()
            ) {
                when (state.activeTab) {
                    DashboardTab.Dashboard -> DashboardTabContent(state = state, onIntent = viewModel::sendIntent)
                    DashboardTab.Developers -> DevelopersTabContent(state = state, onIntent = viewModel::sendIntent)
                    DashboardTab.Apps -> AppsTabContent(state = state)
                    DashboardTab.CICD -> CICDTabContent(state = state, onIntent = viewModel::sendIntent)
                    DashboardTab.Reports -> ReportsTabContent(state = state, onIntent = viewModel::sendIntent)
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────
// Dashboard Tab Content — 概览 Tab 内容
// ─────────────────────────────────────────────────────────────────

@Composable
private fun DashboardTabContent(
    state: VerificationDashboardState,
    onIntent: (VerificationDashboardIntent) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Countdown Banner / 倒计时横幅
        item {
            CountdownBanner(countdownDays = state.countdownDays)
        }

        // Stats Cards / 统计卡片
        item {
            StatsCardsRow(stats = state.stats)
        }

        // Warnings / 预警列表
        item {
            Text(
                text = "⚠️ 预警中心 / Warning Center",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        items(state.warnings) { warning ->
            WarningCard(warning = warning, onIntent = onIntent)
        }

        // Quick Actions / 快捷操作
        item {
            QuickActionsCard(
                selectedCount = state.selectedDevelopers.size,
                onBatchSubmit = { onIntent(VerificationDashboardIntent.OpenWizard()) },
                onRefresh = { onIntent(VerificationDashboardIntent.RefreshDashboard) }
            )
        }
    }
}

@Composable
private fun CountdownBanner(countdownDays: Int) {
    val (bgColor, emoji) = when {
        countdownDays <= 7 -> VerifBatchColors.Expired to "🔴"
        countdownDays <= 30 -> VerifBatchColors.Pending to "🟡"
        else -> VerifBatchColors.GoogleBlue to "🟢"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = bgColor.copy(alpha = 0.15f)),
        border = BorderStroke(1.dp, bgColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(text = emoji, fontSize = 32.sp)
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "Google 开发者验证强制执行倒计时",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Enforcement Countdown — Sep 30, 2026",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "$countdownDays days remaining / 剩余 $countdownDays 天",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = bgColor
                )
            }
        }
    }
}

@Composable
private fun StatsCardsRow(stats: DashboardStats) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StatCard(
            modifier = Modifier.weight(1f),
            title = "总开发者",
            value = stats.totalDevelopers.toString(),
            subtitle = "Total Developers",
            color = VerifBatchColors.GoogleBlue
        )
        StatCard(
            modifier = Modifier.weight(1f),
            title = "已验证",
            value = stats.verified.toString(),
            subtitle = "Verified",
            color = VerifBatchColors.Verified
        )
    }
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StatCard(
            modifier = Modifier.weight(1f),
            title = "待验证",
            value = stats.pending.toString(),
            subtitle = "Pending",
            color = VerifBatchColors.Pending
        )
        StatCard(
            modifier = Modifier.weight(1f),
            title = "已过期",
            value = stats.expired.toString(),
            subtitle = "Expired",
            color = VerifBatchColors.Expired
        )
    }
}

@Composable
private fun StatCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    subtitle: String,
    color: Color
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f)),
        border = BorderStroke(1.dp, color.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }
    }
}

@Composable
private fun WarningCard(
    warning: WarningItem,
    onIntent: (VerificationDashboardIntent) -> Unit
) {
    val (bgColor, icon) = when (warning.severity) {
        WarningSeverity.CRITICAL -> VerifBatchColors.Expired to Icons.Default.Error
        WarningSeverity.WARNING -> VerifBatchColors.Pending to Icons.Default.Warning
        WarningSeverity.INFO -> VerifBatchColors.GoogleBlue to Icons.Default.Info
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = bgColor.copy(alpha = 0.08f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = bgColor,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = warning.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = warning.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
            if (warning.developerId != null) {
                IconButton(onClick = { onIntent(VerificationDashboardIntent.RefreshDeveloperStatus(warning.developerId)) }) {
                    Icon(Icons.Default.Refresh, contentDescription = "Refresh / 刷新", tint = bgColor)
                }
            }
        }
    }
}

@Composable
private fun QuickActionsCard(
    selectedCount: Int,
    onBatchSubmit: () -> Unit,
    onRefresh: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "⚡ 快捷操作 / Quick Actions",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onBatchSubmit,
                    enabled = selectedCount > 0,
                    colors = ButtonDefaults.buttonColors(containerColor = VerifBatchColors.GoogleBlue),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Send, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("批量提交 ($selectedCount)")
                }
                OutlinedButton(
                    onClick = onRefresh,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("刷新")
                }
            }
            if (selectedCount == 0) {
                Text(
                    text = "在「开发者」Tab 选择开发者后即可批量提交",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────
// Developers Tab Content — 开发者管理 Tab
// ─────────────────────────────────────────────────────────────────

@Composable
private fun DevelopersTabContent(
    state: VerificationDashboardState,
    onIntent: (VerificationDashboardIntent) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        // Search & Filter Bar / 搜索筛选栏
        SearchFilterBar(
            searchQuery = state.searchQuery,
            filterStatus = state.filterStatus,
            onSearch = { onIntent(VerificationDashboardIntent.Search(it)) },
            onFilterChange = { onIntent(VerificationDashboardIntent.FilterByStatus(it)) },
            onSelectAll = { onIntent(VerificationDashboardIntent.SelectAllFiltered) },
            onClearSelection = { onIntent(VerificationDashboardIntent.ClearSelection) },
            totalCount = state.developers.size,
            selectedCount = state.selectedDevelopers.size
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(state.developers, key = { it.id }) { developer ->
                DeveloperCard(
                    developer = developer,
                    isSelected = state.selectedDevelopers.contains(developer.id),
                    onToggleSelect = { onIntent(VerificationDashboardIntent.ToggleDeveloperSelection(developer.id)) },
                    onRefresh = { onIntent(VerificationDashboardIntent.RefreshDeveloperStatus(developer.id)) }
                )
            }
        }
    }
}

@Composable
private fun SearchFilterBar(
    searchQuery: String,
    filterStatus: VerificationStatus?,
    onSearch: (String) -> Unit,
    onFilterChange: (VerificationStatus?) -> Unit,
    onSelectAll: () -> Unit,
    onClearSelection: () -> Unit,
    totalCount: Int,
    selectedCount: Int
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearch,
            label = { Text("搜索开发者 / Search") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { onSearch("") }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear / 清除")
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Status filter chips / 状态筛选 Chip
            VerificationStatus.entries.forEach { status ->
                FilterChip(
                    selected = filterStatus == status,
                    onClick = { onFilterChange(if (filterStatus == status) null else status) },
                    label = {
                        Text(
                            text = when (status) {
                                VerificationStatus.Verified -> "✅ 已验证"
                                VerificationStatus.Pending -> "⏳ 待验证"
                                VerificationStatus.Expired -> "❌ 已过期"
                                VerificationStatus.Failed -> "⚠️ 失败"
                            },
                            fontSize = 11.sp
                        )
                    },
                    modifier = Modifier.padding(end = 4.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$totalCount 个开发者 / $totalCount developers",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
            Row {
                TextButton(onClick = onSelectAll) { Text("全选", fontSize = 12.sp) }
                if (selectedCount > 0) {
                    TextButton(onClick = onClearSelection) { Text("清除 ($selectedCount)", fontSize = 12.sp) }
                }
            }
        }
    }
}

@Composable
private fun DeveloperCard(
    developer: DeveloperItem,
    isSelected: Boolean,
    onToggleSelect: () -> Unit,
    onRefresh: () -> Unit
) {
    val statusColor = when (developer.verificationStatus) {
        VerificationStatus.Verified -> VerifBatchColors.Verified
        VerificationStatus.Pending -> VerifBatchColors.Pending
        VerificationStatus.Expired -> VerifBatchColors.Expired
        VerificationStatus.Failed -> VerifBatchColors.Failed
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggleSelect() },
        border = if (isSelected) BorderStroke(2.dp, VerifBatchColors.GoogleBlue) else null,
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) VerifBatchColors.GoogleBlue.copy(alpha = 0.05f) else MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = isSelected,
                onCheckedChange = { onToggleSelect() }
            )

            // Avatar / 头像
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(statusColor.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = developer.name.take(1).uppercase(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = statusColor
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = developer.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = developer.email,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    VerificationStatusChip(status = developer.verificationStatus)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${developer.appCount} Apps",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
            }

            IconButton(onClick = onRefresh) {
                Icon(Icons.Default.Refresh, contentDescription = "Refresh / 刷新", tint = Color.Gray)
            }
        }
    }
}

@Composable
private fun VerificationStatusChip(status: VerificationStatus) {
    val (color, text) = when (status) {
        VerificationStatus.Verified -> VerifBatchColors.Verified to "已验证"
        VerificationStatus.Pending -> VerifBatchColors.Pending to "待验证"
        VerificationStatus.Expired -> VerifBatchColors.Expired to "已过期"
        VerificationStatus.Failed -> VerifBatchColors.Failed to "失败"
    }

    Surface(
        shape = RoundedCornerShape(4.dp),
        color = color.copy(alpha = 0.15f)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = color,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}

// ─────────────────────────────────────────────────────────────────
// Apps Tab Content — App 管理 Tab
// ─────────────────────────────────────────────────────────────────

@Composable
private fun AppsTabContent(state: VerificationDashboardState) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Text(
                text = "${state.apps.size} 个 App / ${state.apps.size} Apps",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        items(state.apps, key = { it.id }) { app ->
            AppCard(app = app)
        }
    }
}

@Composable
private fun AppCard(app: AppItem) {
    val statusColor = when (app.verificationStatus) {
        VerificationStatus.Verified -> VerifBatchColors.Verified
        VerificationStatus.Pending -> VerifBatchColors.Pending
        VerificationStatus.Expired -> VerifBatchColors.Expired
        VerificationStatus.Failed -> VerifBatchColors.Failed
    }

    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(statusColor.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Android,
                    contentDescription = null,
                    tint = statusColor
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = app.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = app.packageName,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                VerificationStatusChip(status = app.verificationStatus)
            }

            if (app.expiresAt != null) {
                val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd").withZone(ZoneId.systemDefault())
                Text(
                    text = "到期: ${formatter.format(Instant.ofEpochMilli(app.expiresAt))}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────
// CI/CD Tab Content — CI/CD 集成 Tab
// ─────────────────────────────────────────────────────────────────

@Composable
private fun CICDTabContent(
    state: VerificationDashboardState,
    onIntent: (VerificationDashboardIntent) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "CI/CD 配置片段生成器",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "CI/CD Config Snippet Generator",
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Platform selection / 平台选择
        Text("选择 CI/CD 平台 / Select Platform:", style = MaterialTheme.typography.bodyMedium)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CICDPlatform.entries.forEach { platform ->
                FilterChip(
                    selected = state.cicdPlatform == platform,
                    onClick = { onIntent(VerificationDashboardIntent.GenerateCICDSnippet(platform)) },
                    label = {
                        Text(
                            text = when (platform) {
                                CICDPlatform.GitHub_Actions -> "GitHub Actions"
                                CICDPlatform.GitLab_CI -> "GitLab CI"
                                CICDPlatform.Jenkins -> "Jenkins"
                            },
                            fontSize = 12.sp
                        )
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Generated snippet / 生成的配置片段
        if (state.generatedConfigSnippet.isNotEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "配置文件 / Config Snippet",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Button(
                            onClick = { onIntent(VerificationDashboardIntent.CopyCICDSnippet) },
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("复制 / Copy", fontSize = 12.sp)
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    SelectionContainer {
                        Text(
                            text = state.generatedConfigSnippet,
                            style = MaterialTheme.typography.bodySmall,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier
                                .fillMaxWidth()
                                .verticalScroll(rememberScrollState())
                        )
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────
// Reports Tab Content — 报告 Tab
// ─────────────────────────────────────────────────────────────────

@Composable
private fun ReportsTabContent(
    state: VerificationDashboardState,
    onIntent: (VerificationDashboardIntent) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "合规报告导出",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Compliance Report Export",
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Report summary / 报告摘要
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("报告摘要 / Report Summary", fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.height(12.dp))
                ReportStatRow("总开发者数 / Total Developers", state.stats.totalDevelopers.toString())
                ReportStatRow("已验证 / Verified", state.stats.verified.toString())
                ReportStatRow("待验证 / Pending", state.stats.pending.toString())
                ReportStatRow("已过期 / Expired", state.stats.expired.toString())
                ReportStatRow("验证失败 / Failed", state.stats.failed.toString())
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Export buttons / 导出按钮
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = {
                    val now = System.currentTimeMillis()
                    val thirtyDaysAgo = now - 30L * 24 * 3600 * 1000
                    onIntent(VerificationDashboardIntent.ExportReport(
                        ReportFormat.CSV,
                        DateRange(thirtyDaysAgo, now)
                    ))
                },
                modifier = Modifier.weight(1f),
                enabled = !state.isLoading
            ) {
                Icon(Icons.Default.TableChart, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("导出 CSV")
            }

            Button(
                onClick = {
                    val now = System.currentTimeMillis()
                    val thirtyDaysAgo = now - 30L * 24 * 3600 * 1000
                    onIntent(VerificationDashboardIntent.ExportReport(
                        ReportFormat.PDF,
                        DateRange(thirtyDaysAgo, now)
                    ))
                },
                modifier = Modifier.weight(1f),
                enabled = !state.isLoading
            ) {
                Icon(Icons.Default.PictureAsPdf, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("导出 PDF")
            }
        }

        if (state.isLoading) {
            Spacer(modifier = Modifier.height(16.dp))
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }
    }
}

@Composable
private fun ReportStatRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
        Text(text = value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
    }
}

// ─────────────────────────────────────────────────────────────────
// Verification Wizard Dialog — 验证申请向导对话框
// ─────────────────────────────────────────────────────────────────

@Composable
private fun VerificationWizardDialog(
    state: VerificationDashboardState,
    onIntent: (VerificationDashboardIntent) -> Unit
) {
    AlertDialog(
        onDismissRequest = { onIntent(VerificationDashboardIntent.CloseWizard) },
        title = {
            Text(
                text = when (state.wizardStep) {
                    0 -> "选择开发者 & App / Select Developers & Apps"
                    1 -> "确认身份信息 / Confirm Identity Info"
                    2 -> "选择验证类型 / Select Verification Type"
                    3 -> "提交 & 生成 CI/CD 配置 / Submit & Generate CI/CD Config"
                    else -> "验证向导 / Verification Wizard"
                }
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Step indicator / 步骤指示器
                LinearProgressIndicator(
                    progress = { (state.wizardStep + 1) / 4f },
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(modifier = Modifier.height(16.dp))

                when (state.wizardStep) {
                    0 -> WizardStepSelectDevelopers(state = state, onIntent = onIntent)
                    1 -> WizardStepConfirmIdentity(state = state)
                    2 -> WizardStepSelectType(state = state, onIntent = onIntent)
                    3 -> WizardStepSubmit(state = state, onIntent = onIntent)
                }
            }
        },
        confirmButton = {
            Row {
                if (state.wizardStep > 0) {
                    TextButton(onClick = { onIntent(VerificationDashboardIntent.WizardPrevStep) }) {
                        Text("上一步 / Back")
                    }
                }
                if (state.wizardStep < 3) {
                    Button(onClick = { onIntent(VerificationDashboardIntent.WizardNextStep) }) {
                        Text("下一步 / Next")
                    }
                } else {
                    Button(
                        onClick = {
                            onIntent(VerificationDashboardIntent.BatchSubmit(
                                developerIds = state.selectedDevelopers.toList(),
                                appIds = state.selectedApps.toList(),
                                verificationType = VerificationType.FirstTime
                            ))
                        },
                        enabled = !state.isBatchSubmitting
                    ) {
                        if (state.isBatchSubmitting) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                        } else {
                            Text("提交 / Submit")
                        }
                    }
                }
            }
        },
        dismissButton = {
            TextButton(onClick = { onIntent(VerificationDashboardIntent.CloseWizard) }) {
                Text("取消 / Cancel")
            }
        }
    )
}

@Composable
private fun WizardStepSelectDevelopers(
    state: VerificationDashboardState,
    onIntent: (VerificationDashboardIntent) -> Unit
) {
    Column {
        Text("已选择 ${state.selectedDevelopers.size} 个开发者 / ${state.selectedDevelopers.size} developers selected")
        Spacer(modifier = Modifier.height(8.dp))
        state.developers.filter { state.selectedDevelopers.contains(it.id) }.forEach { dev ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("${dev.name} (${dev.email})", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
private fun WizardStepConfirmIdentity(state: VerificationDashboardState) {
    Column {
        Text("确认以下开发者身份信息 / Confirm developer identity info:")
        Spacer(modifier = Modifier.height(8.dp))
        state.developers.filter { state.selectedDevelopers.contains(it.id) }.forEach { dev ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                colors = CardDefaults.cardColors(containerColor = Color.LightGray.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    Text("Name: ${dev.name}", style = MaterialTheme.typography.bodySmall)
                    Text("Email: ${dev.email}", style = MaterialTheme.typography.bodySmall)
                    Text("Status: ${dev.verificationStatus}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                }
            }
        }
    }
}

@Composable
private fun WizardStepSelectType(
    state: VerificationDashboardState,
    onIntent: (VerificationDashboardIntent) -> Unit
) {
    Column {
        Text("选择验证类型 / Select verification type:")
        Spacer(modifier = Modifier.height(8.dp))
        VerificationType.entries.forEach { type ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { }
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = true,
                    onClick = { }
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = when (type) {
                            VerificationType.FirstTime -> "首次验证 / First-time Verification"
                            VerificationType.Renewal -> "续期 / Renewal"
                        },
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = when (type) {
                            VerificationType.FirstTime -> "适用于未通过验证的开发者"
                            VerificationType.Renewal -> "适用于已过期或即将过期的开发者"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}

@Composable
private fun WizardStepSubmit(
    state: VerificationDashboardState,
    onIntent: (VerificationDashboardIntent) -> Unit
) {
    Column {
        Text("即将提交以下申请 / About to submit:", style = MaterialTheme.typography.bodyMedium)
        Spacer(modifier = Modifier.height(8.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = VerifBatchColors.GoogleBlue.copy(alpha = 0.1f))
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = "${state.selectedDevelopers.size} 个开发者 / ${state.selectedDevelopers.size} developers",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "${state.selectedApps.size} 个 App / ${state.selectedApps.size} apps",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "验证类型: 首次验证 / Verification Type: First-time",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
        }
    }
}
