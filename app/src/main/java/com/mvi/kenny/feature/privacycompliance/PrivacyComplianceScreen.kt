package com.mvi.kenny.feature.privacycompliance

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mvi.kenny.R
import com.mvi.kenny.base.TopBarAction
import com.mvi.kenny.base.TopBarActions
import com.mvi.kenny.base.TopBarConfig
import kotlinx.coroutines.flow.collect
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * ============================================================
 * PrivacyComplianceScreen — 企业隐私合规审计平台主界面
 * ============================================================
 * PRD-309 | Android 企业隐私合规审计与合规状态管理平台
 *
 * 页面结构：
 * - 仪表板首页：App 矩阵卡片 + 风险告警横幅 + 合规债务走势图
 * - App 详情页：4 个 Tab（合规清单 / 风险量化 / CI/CD / 历史报告）
 *
 * @param onNavigateToList 跳转到列表 Tab 的回调（目前通过 Tab 切换即可，无需特殊处理）
 * @param onUpdateTopBar 向 MainScreen 上报 TopBar 配置（标题 + 操作按钮）
 *
 * @see PrivacyComplianceViewModel 状态管理
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacyComplianceScreen(
    onNavigateToList: () -> Unit = {},
    onUpdateTopBar: (TopBarConfig) -> Unit = {}
) {
    val viewModel: PrivacyComplianceViewModel = viewModel()
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    // 避免在 lambda 表达式中直接访问 state.selectedApp / state.isLoading
    val selectedApp = state.selectedApp
    val isLoading = state.isLoading

    // ==========================================================================
    // TopBar 配置（动态）
    // ==========================================================================
    val topBarConfig by remember(selectedApp, state.isGeneratingReport) {
        mutableStateOf(
            if (selectedApp != null) {
                TopBarConfig(
                    title = selectedApp!!.appName,
                    actions = listOfNotNull(
                        TopBarAction(
                            icon = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "返回",
                            onClick = { viewModel.sendIntent(PrivacyComplianceIntent.BackToDashboard) }
                        ),
                        if (state.isGeneratingReport) null
                        else TopBarAction(
                            icon = Icons.Default.Refresh,
                            contentDescription = "刷新",
                            onClick = { viewModel.sendIntent(PrivacyComplianceIntent.RefreshSelectedApp) }
                        )
                    )
                )
            } else {
                TopBarConfig(
                    title = "隐私合规",
                    actions = if (isLoading) emptyList() else listOf(
                        TopBarAction(
                            icon = Icons.Default.Refresh,
                            contentDescription = "刷新",
                            onClick = { viewModel.sendIntent(PrivacyComplianceIntent.RefreshDashboard) }
                        )
                    )
                )
            }
        )
    }

    // 上报 TopBar 配置给 MainScreen
    LaunchedEffect(topBarConfig) {
        onUpdateTopBar(topBarConfig)
    }

    // ==========================================================================
    // Effect 收集（导航、Toast、报告生成完成等一次性事件）
    // ==========================================================================
    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is PrivacyComplianceEffect.ShowCopySuccess -> {
                    Toast.makeText(context, effect.message, Toast.LENGTH_SHORT).show()
                }
                is PrivacyComplianceEffect.ReportReady -> {
                    Toast.makeText(context, "报告已生成: ${effect.downloadUrl}", Toast.LENGTH_LONG).show()
                }
                is PrivacyComplianceEffect.ShowError -> {
                    Toast.makeText(context, "错误: ${effect.message}", Toast.LENGTH_LONG).show()
                }
                is PrivacyComplianceEffect.CICDTestRunComplete -> {
                    Toast.makeText(context, "CI/CD 测试运行完成", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // ==========================================================================
    // 页面主体
    // ==========================================================================
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (selectedApp != null) selectedApp!!.appName else "隐私合规"
                    )
                },
                navigationIcon = {
                    if (selectedApp != null) {
                        IconButton(onClick = { viewModel.sendIntent(PrivacyComplianceIntent.BackToDashboard) }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (selectedApp != null) {
                // ==============================================================
                // App 详情页
                // ==============================================================
                AppDetailContent(
                    detail = selectedApp!!,
                    selectedTab = state.selectedTab,
                    isGeneratingReport = state.isGeneratingReport,
                    selectedReportTemplate = state.selectedReportTemplate,
                    onTabChange = { viewModel.sendIntent(PrivacyComplianceIntent.SwitchTab(it)) },
                    onStartReport = { viewModel.sendIntent(PrivacyComplianceIntent.StartReportGeneration(it)) },
                    onCopyConfig = { viewModel.sendIntent(PrivacyComplianceIntent.CopyCIConfig(it)) },
                    onSelectTemplate = { viewModel.sendIntent(PrivacyComplianceIntent.SelectReportTemplate(it)) }
                )
            } else {
                // ==============================================================
                // 仪表板首页
                // ==============================================================
                DashboardContent(
                    state = state,
                    onAppClick = { viewModel.sendIntent(PrivacyComplianceIntent.SelectApp(it)) },
                    onRefresh = { viewModel.sendIntent(PrivacyComplianceIntent.RefreshDashboard) }
                )
            }

            // 全局加载遮罩
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}

// =============================================================================
// Dashboard Content
// =============================================================================

/**
 * 仪表板内容
 * —————————————————————————————————————————————————————
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DashboardContent(
    state: PrivacyComplianceState,
    onAppClick: (String) -> Unit,
    onRefresh: () -> Unit
) {
    PullToRefreshBox(
        isRefreshing = state.isLoading,
        onRefresh = onRefresh,
        modifier = Modifier.fillMaxSize()
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 风险告警横幅
            if (state.alerts.isNotEmpty()) {
                item {
                    RiskAlertBanners(alerts = state.alerts, onAppClick = onAppClick)
                }
            }

            // 仪表板标题
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "App 合规矩阵",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    state.lastRefreshed?.let {
                        Text(
                            text = "更新于 ${formatInstant(it)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // App 矩阵网格（2列）
            item {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.height(((state.apps.size + 1) / 2 * 140).dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    userScrollEnabled = false
                ) {
                    items(state.apps) { app ->
                        AppComplianceCard(
                            app = app,
                            onClick = { onAppClick(app.appId) }
                        )
                    }
                }
            }

            // 合规概览统计
            item {
                ComplianceOverviewStats(apps = state.apps)
            }

            // CI/CD 插件入口
            item {
                CICDPluginsSection(plugins = state.ciCdPlugins)
            }
        }
    }
}

/**
 * 风险告警横幅
 */
@Composable
private fun RiskAlertBanners(
    alerts: List<RiskAlert>,
    onAppClick: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        alerts.take(3).forEach { alert ->
            val backgroundColor = when (alert.severity) {
                AlertSeverity.CRITICAL -> MaterialTheme.colorScheme.errorContainer
                AlertSeverity.WARNING -> MaterialTheme.colorScheme.tertiaryContainer
            }
            val contentColor = when (alert.severity) {
                AlertSeverity.CRITICAL -> MaterialTheme.colorScheme.onErrorContainer
                AlertSeverity.WARNING -> MaterialTheme.colorScheme.onTertiaryContainer
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onAppClick(alert.appId) },
                colors = CardDefaults.cardColors(containerColor = backgroundColor)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = when (alert.severity) {
                            AlertSeverity.CRITICAL -> Icons.Default.Error
                            AlertSeverity.WARNING -> Icons.Default.Warning
                        },
                        contentDescription = null,
                        tint = contentColor,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = alert.title,
                            style = MaterialTheme.typography.titleSmall,
                            color = contentColor,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = alert.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = contentColor
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "查看",
                        tint = contentColor
                    )
                }
            }
        }
    }
}

/**
 * App 合规卡片
 */
@Composable
private fun AppComplianceCard(
    app: AppComplianceSummary,
    onClick: () -> Unit
) {
    val statusColor = when (app.overallStatus) {
        ComplianceStatus.COMPLIANT -> Color(0xFF4CAF50)
        ComplianceStatus.PENDING -> Color(0xFFFF9800)
        ComplianceStatus.NON_COMPLIANT -> Color(0xFFF44336)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(130.dp)
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // App 名称 + 状态图标
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = app.appName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
                Icon(
                    imageVector = app.overallStatus.icon,
                    contentDescription = app.overallStatus.label,
                    tint = statusColor,
                    modifier = Modifier.size(20.dp)
                )
            }

            // 风险评分
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "风险评分",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${app.riskScore}",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = statusColor
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = { app.riskScore / 100f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = statusColor,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }

            // 未修复数量
            if (app.unfixedCount > 0) {
                Text(
                    text = "${app.unfixedCount} 项待修复",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            } else {
                Text(
                    text = "全部合规",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

/**
 * 合规概览统计
 */
@Composable
private fun ComplianceOverviewStats(apps: List<AppComplianceSummary>) {
    val total = apps.size
    val compliant = apps.count { it.overallStatus == ComplianceStatus.COMPLIANT }
    val pending = apps.count { it.overallStatus == ComplianceStatus.PENDING }
    val nonCompliant = apps.count { it.overallStatus == ComplianceStatus.NON_COMPLIANT }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "合规概览",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(
                    label = "已合规",
                    value = "$compliant",
                    color = Color(0xFF4CAF50)
                )
                StatItem(
                    label = "待修复",
                    value = "$pending",
                    color = Color(0xFFFF9800)
                )
                StatItem(
                    label = "未合规",
                    value = "$nonCompliant",
                    color = Color(0xFFF44336)
                )
                StatItem(
                    label = "总计",
                    value = "$total",
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun StatItem(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * CI/CD 插件入口
 */
@Composable
private fun CICDPluginsSection(plugins: List<CICDPlugin>) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "CI/CD 合规卡点",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                TextButton(onClick = { /* Navigate to plugin marketplace */ }) {
                    Text("插件市场")
                    Icon(Icons.Default.ChevronRight, contentDescription = null, modifier = Modifier.size(16.dp))
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            plugins.forEach { plugin ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Code,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = plugin.name,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    if (plugin.isInstalled) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Badge { Text("已安装") }
                    }
                }
            }
        }
    }
}

// =============================================================================
// App Detail Content
// =============================================================================

/**
 * App 详情页内容
 */
@Composable
private fun AppDetailContent(
    detail: AppComplianceDetail,
    selectedTab: AppDetailTab,
    isGeneratingReport: Boolean,
    selectedReportTemplate: ReportTemplate,
    onTabChange: (AppDetailTab) -> Unit,
    onStartReport: (ReportTemplate) -> Unit,
    onCopyConfig: (String) -> Unit,
    onSelectTemplate: (ReportTemplate) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        // Tab 行
        TabRow(
            selectedTabIndex = AppDetailTab.entries.indexOf(selectedTab)
        ) {
            AppDetailTab.entries.forEach { tab ->
                Tab(
                    selected = selectedTab == tab,
                    onClick = { onTabChange(tab) },
                    text = {
                        Text(
                            text = when (tab) {
                                AppDetailTab.Checklist -> "合规清单"
                                AppDetailTab.RiskQuantification -> "风险量化"
                                AppDetailTab.CICD -> "CI/CD"
                                AppDetailTab.Reports -> "报告"
                            }
                        )
                    }
                )
            }
        }

        // Tab 内容
        when (selectedTab) {
            AppDetailTab.Checklist -> ChecklistTab(checkItems = detail.checkItems)
            AppDetailTab.RiskQuantification -> RiskQuantificationTab(riskScores = detail.riskScores)
            AppDetailTab.CICD -> CICDTab(
                config = detail.ciCdConfig,
                isGeneratingReport = isGeneratingReport,
                selectedReportTemplate = selectedReportTemplate,
                onStartReport = onStartReport,
                onCopyConfig = onCopyConfig,
                onSelectTemplate = onSelectTemplate
            )
            AppDetailTab.Reports -> ReportsTab(reports = detail.reports)
        }
    }
}

/**
 * 合规清单 Tab
 */
@Composable
private fun ChecklistTab(checkItems: Map<AndroidApiLevel, List<ComplianceCheckItem>>) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        AndroidApiLevel.entries.forEach { apiLevel ->
            val items = checkItems[apiLevel] ?: emptyList()
            if (items.isNotEmpty()) {
                item {
                    Text(
                        text = apiLevel.displayName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                items(items) { item ->
                    ComplianceCheckCard(item = item)
                }
            }
        }
    }
}

/**
 * 单个合规检查卡片
 */
@Composable
private fun ComplianceCheckCard(item: ComplianceCheckItem) {
    val statusColor = when (item.status) {
        ComplianceStatus.COMPLIANT -> Color(0xFF4CAF50)
        ComplianceStatus.PENDING -> Color(0xFFFF9800)
        ComplianceStatus.NON_COMPLIANT -> Color(0xFFF44336)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            // 状态图标
            Icon(
                imageVector = item.status.icon,
                contentDescription = item.status.label,
                tint = statusColor,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))

            // 检查项详情
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = item.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = item.playStorePolicy,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )

                // 风险评分
                if (item.riskScore > 0) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "风险评分",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${item.riskScore}/100",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = statusColor
                        )
                    }
                    LinearProgressIndicator(
                        progress = { item.riskScore / 100f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp)),
                        color = statusColor
                    )
                }

                // 受影响 App 数量
                if (item.affectedApps > 0) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "受影响: ${item.affectedApps} 个模块",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                // 代码路径
                if (item.codeFilePaths.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    item.codeFilePaths.forEach { path ->
                        Text(
                            text = path,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.clickable { /* Copy path */ }
                        )
                    }
                }
            }
        }
    }
}

/**
 * 风险量化 Tab（简化版雷达图）
 */
@Composable
private fun RiskQuantificationTab(riskScores: RiskQuantification) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "综合风险评分",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        // 综合评分圆形进度
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            CircularRiskGauge(score = riskScores.overallRiskScore())
        }

        // 各维度评分
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "各维度风险详情",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))

                RiskDimensionRow(
                    label = "审核风险",
                    score = riskScores.auditRisk,
                    description = "Play Store 审核被拒风险"
                )
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                RiskDimensionRow(
                    label = "下架风险",
                    score = riskScores.delistRisk,
                    description = "App 被强制下架风险"
                )
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                RiskDimensionRow(
                    label = "数据泄露风险",
                    score = riskScores.dataLeakRisk,
                    description = "用户数据泄露风险"
                )
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                RiskDimensionRow(
                    label = "合规缺口风险",
                    score = riskScores.complianceGapRisk,
                    description = "未满足监管要求风险"
                )
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                RiskDimensionRow(
                    label = "用户影响风险",
                    score = riskScores.userImpactRisk,
                    description = "对用户使用体验的影响程度"
                )
            }
        }
    }
}

/**
 * 风险维度评分行
 */
@Composable
private fun RiskDimensionRow(label: String, score: Int, description: String) {
    val color = when {
        score >= 70 -> Color(0xFFF44336)
        score >= 40 -> Color(0xFFFF9800)
        else -> Color(0xFF4CAF50)
    }

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = label, style = MaterialTheme.typography.bodyMedium)
            Text(
                text = "$score/100",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { score / 100f },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = color,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = description,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * 圆形风险仪表（简化版）
 */
@Composable
private fun CircularRiskGauge(score: Int) {
    val color = when {
        score >= 70 -> Color(0xFFF44336)
        score >= 40 -> Color(0xFFFF9800)
        else -> Color(0xFF4CAF50)
    }

    Box(
        modifier = Modifier.size(160.dp),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            progress = { score / 100f },
            modifier = Modifier.size(160.dp),
            strokeWidth = 12.dp,
            color = color,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "$score",
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = "风险指数",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * CI/CD 配置 Tab
 */
@Composable
private fun CICDTab(
    config: CICDConfig?,
    isGeneratingReport: Boolean,
    selectedReportTemplate: ReportTemplate,
    onStartReport: (ReportTemplate) -> Unit,
    onCopyConfig: (String) -> Unit,
    onSelectTemplate: (ReportTemplate) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // CI/CD 配置卡片
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "CI/CD 合规卡点",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        if (config?.enabled == true) {
                            Badge { Text("已启用") }
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))

                    config?.ymlSnippet?.let { snippet ->
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = snippet,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedButton(
                            onClick = { onCopyConfig(config.pluginId) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("复制 YML 配置")
                        }
                    }

                    // 最后测试结果
                    config?.lastTestResult?.let { result ->
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "最后测试: ${formatInstant(config.lastTestRun!!)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                text = "✓ ${result.passed}",
                                color = Color(0xFF4CAF50)
                            )
                            Text(
                                text = "✗ ${result.failed}",
                                color = Color(0xFFF44336)
                            )
                            Text(
                                text = "⚠ ${result.warnings}",
                                color = Color(0xFFFF9800)
                            )
                        }
                    }
                }
            }
        }

        // 报告生成卡片
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "生成合规报告",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // 模板选择
                    Text(
                        text = "选择报告模板",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(ReportTemplate.entries.toList()) { template ->
                            FilterChip(
                                selected = selectedReportTemplate == template,
                                onClick = { onSelectTemplate(template) },
                                label = { Text(template.displayName) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // 生成按钮
                    if (isGeneratingReport) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("正在生成报告...")
                        }
                    } else {
                        OutlinedButton(
                            onClick = { onStartReport(selectedReportTemplate) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Assessment, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("生成 ${selectedReportTemplate.displayName}")
                        }
                    }
                }
            }
        }
    }
}

/**
 * 历史报告 Tab
 */
@Composable
private fun ReportsTab(reports: List<ComplianceReport>) {
    if (reports.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.Assessment,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "暂无历史报告",
                    style = MaterialTheme.typography.bodyLarge,
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
            items(reports) { report ->
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = report.title,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = report.template.displayName,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "生成于 ${formatInstant(report.generatedAt)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        if (report.downloadUrl != null) {
                            IconButton(onClick = { /* Download report */ }) {
                                Icon(
                                    imageVector = Icons.Default.Assessment,
                                    contentDescription = "下载报告",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// =============================================================================
// Utilities
// =============================================================================

/**
 * 格式化 Instant 为可读时间字符串
 */
private fun formatInstant(instant: Instant): String {
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
        .withZone(ZoneId.systemDefault())
    return formatter.format(instant)
}
