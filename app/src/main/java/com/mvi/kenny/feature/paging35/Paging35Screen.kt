package com.mvi.kenny.feature.paging35

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.flow.collectLatest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * ============================================================
 * Paging35Screen — Paging 3.5 asState 操作符开发工具包主界面
 * ============================================================
 * Main screen container for the Paging 3.5 toolkit.
 * Handles navigation between dashboard and sub-tools.
 *
 * PRD-093: Paging 3.5 asState 操作符开发工具包
 */

// =============================================================
// Main Entry Point / 主入口
// =============================================================

/**
 * Paging35Screen — 主界面容器
 *
 * @param viewModel Paging35ViewModel instance
 * @param onNavigateBack Callback when navigating back
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Paging35Screen(
    viewModel: Paging35ViewModel,
    onNavigateBack: () -> Unit
) {
    val dashboardState by viewModel.dashboardState.collectAsState()
    val context = LocalContext.current

    // Handle dashboard effects / 处理仪表盘副作用
    LaunchedEffect(Unit) {
        viewModel.dashboardEffects.collectLatest { effect ->
            when (effect) {
                is Paging35DashboardEffect.NavigateToTool -> { /* Handled by state */ }
                is Paging35DashboardEffect.ShowScanComplete -> {
                    Toast.makeText(context, "Scan complete: ${effect.issuesFound} issues found", Toast.LENGTH_SHORT).show()
                }
                is Paging35DashboardEffect.ShowError -> {
                    Toast.makeText(context, effect.message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    // Handle template effects / 处理模板副作用
    LaunchedEffect(Unit) {
        viewModel.templateEffects.collectLatest { effect ->
            when (effect) {
                is AsStateTemplateEffect.CodeCopied -> {
                    Toast.makeText(context, "Code copied!", Toast.LENGTH_SHORT).show()
                }
                is AsStateTemplateEffect.ShowError -> {
                    Toast.makeText(context, effect.message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    // Handle debug effects / 处理调试副作用
    LaunchedEffect(Unit) {
        viewModel.debugEffects.collectLatest { effect ->
            when (effect) {
                is PagingDebugPanelEffect.ShowPulseAnimation -> {
                    // Animation handled in UI / 动画在 UI 中处理
                }
                is PagingDebugPanelEffect.ShowError -> {
                    Toast.makeText(context, effect.message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    // Handle troubleshooting effects / 处理踩坑副作用
    LaunchedEffect(Unit) {
        viewModel.troubleshootingEffects.collectLatest { effect ->
            when (effect) {
                is PagingTroubleshootingEffect.ShowScanCompleteBottomSheet -> {
                    Toast.makeText(context, "Scan complete: ${effect.results.size} results", Toast.LENGTH_SHORT).show()
                }
                is PagingTroubleshootingEffect.ShowError -> {
                    Toast.makeText(context, effect.message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    // Show sub-tool screen or dashboard / 显示子工具界面或仪表盘
    when (dashboardState.selectedTool) {
        PagingTool.AS_STATE_TEMPLATE -> {
            AsStateTemplateScreen(
                viewModel = viewModel,
                onNavigateBack = { viewModel.processDashboardIntent(Paging35DashboardIntent.BackToDashboard) }
            )
        }
        PagingTool.MIGRATION_REPORT -> {
            MigrationReportScreen(
                viewModel = viewModel,
                onNavigateBack = { viewModel.processDashboardIntent(Paging35DashboardIntent.BackToDashboard) }
            )
        }
        PagingTool.PAGING_DEBUG -> {
            PagingDebugPanelScreen(
                viewModel = viewModel,
                onNavigateBack = { viewModel.processDashboardIntent(Paging35DashboardIntent.BackToDashboard) }
            )
        }
        PagingTool.TROUBLESHOOTING -> {
            PagingTroubleshootingScreen(
                viewModel = viewModel,
                onNavigateBack = { viewModel.processDashboardIntent(Paging35DashboardIntent.BackToDashboard) }
            )
        }
        null -> {
            PagingHealthDashboard(
                viewModel = viewModel,
                onNavigateBack = onNavigateBack
            )
        }
    }
}

// =============================================================
// PagingHealthDashboard — 主仪表盘
// =============================================================

/**
 * Paging Health Dashboard / Paging 健康仪表盘
 *
 * Main dashboard showing Paging health score and quick access to tools.
 *
 * @param viewModel Paging35ViewModel instance
 * @param onNavigateBack Callback when navigating back
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PagingHealthDashboard(
    viewModel: Paging35ViewModel,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.dashboardState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Paging 3.5 Toolkit") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Paging Version Banner / Paging 版本横幅
            item {
                PagingVersionBanner(version = state.pagingVersion)
            }

            // Health Score Card / 健康度评分卡片
            item {
                HealthScoreCard(
                    healthScore = state.healthScore,
                    isScanning = state.isScanning,
                    scanProgress = state.scanProgress,
                    onStartScan = { viewModel.processDashboardIntent(Paging35DashboardIntent.StartScan) }
                )
            }

            // Stats Row / 统计行
            item {
                StatsRow(
                    totalPagingSources = state.totalPagingSources,
                    asStateUsageRatio = state.asStateUsageRatio,
                    collectAsLazyCount = state.collectAsLazyCount
                )
            }

            // Quick Access Cards / 快捷入口卡片
            item {
                Text(
                    text = "Tools / 工具",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            items(PagingTool.entries) { tool ->
                QuickAccessCard(
                    tool = tool,
                    onClick = { viewModel.processDashboardIntent(Paging35DashboardIntent.SelectTool(tool)) }
                )
            }

            // Version Upgrade Tips / 版本升级提示
            item {
                VersionUpgradeTipsCard()
            }
        }
    }
}

/**
 * Paging Version Banner / Paging 版本横幅
 */
@Composable
private fun PagingVersionBanner(version: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Description,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "Paging Version / Paging 版本",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                )
                Text(
                    text = version,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }
    }
}

/**
 * Health Score Card / 健康度评分卡片
 */
@Composable
private fun HealthScoreCard(
    healthScore: Int,
    isScanning: Boolean,
    scanProgress: Float,
    onStartScan: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Circular Health Gauge / 圆形健康度仪表
            Box(
                modifier = Modifier.size(160.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularHealthGauge(
                    percentage = healthScore / 100f,
                    color = when {
                        healthScore >= 80 -> Color(0xFF4CAF50)
                        healthScore >= 50 -> Color(0xFFFF9800)
                        else -> Color(0xFFF44336)
                    }
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "$healthScore",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Health / 健康度",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Scan Progress / 扫描进度
            if (isScanning) {
                LinearProgressIndicator(
                    progress = { scanProgress },
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Scanning... ${(scanProgress * 100).toInt()}%",
                    style = MaterialTheme.typography.bodySmall
                )
            } else {
                Button(
                    onClick = onStartScan,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Start Scan / 开始扫描")
                }
            }
        }
    }
}

/**
 * Circular Health Gauge / 圆形健康度仪表
 */
@Composable
private fun CircularHealthGauge(
    percentage: Float,
    color: Color
) {
    val animatedPercentage by animateFloatAsState(
        targetValue = percentage,
        animationSpec = tween(durationMillis = 1000),
        label = "health_gauge"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val strokeWidth = 16.dp.toPx()
        val radius = (size.minDimension - strokeWidth) / 2
        val center = Offset(size.width / 2, size.height / 2)

        // Background arc / 背景弧
        drawArc(
            color = Color.Gray.copy(alpha = 0.2f),
            startAngle = 135f,
            sweepAngle = 270f,
            useCenter = false,
            topLeft = Offset(center.x - radius, center.y - radius),
            size = Size(radius * 2, radius * 2),
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )

        // Progress arc / 进度弧
        drawArc(
            color = color,
            startAngle = 135f,
            sweepAngle = 270f * animatedPercentage,
            useCenter = false,
            topLeft = Offset(center.x - radius, center.y - radius),
            size = Size(radius * 2, radius * 2),
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )
    }
}

/**
 * Stats Row / 统计行
 */
@Composable
private fun StatsRow(
    totalPagingSources: Int,
    asStateUsageRatio: Float,
    collectAsLazyCount: Int
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StatCard(
            title = "PagingSources",
            titleZh = "PagingSource",
            value = totalPagingSources.toString(),
            modifier = Modifier.weight(1f)
        )
        StatCard(
            title = "asState Ratio",
            titleZh = "asState 比例",
            value = "${(asStateUsageRatio * 100).toInt()}%",
            modifier = Modifier.weight(1f)
        )
        StatCard(
            title = "To Migrate",
            titleZh = "待迁移",
            value = collectAsLazyCount.toString(),
            modifier = Modifier.weight(1f)
        )
    }
}

/**
 * Stat Card / 统计卡片
 */
@Composable
private fun StatCard(
    title: String,
    titleZh: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "$title / $titleZh",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * Quick Access Card / 快捷入口卡片
 */
@Composable
private fun QuickAccessCard(
    tool: PagingTool,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when (tool) {
                        PagingTool.AS_STATE_TEMPLATE -> Icons.Default.Description
                        PagingTool.MIGRATION_REPORT -> Icons.Default.ContentCopy
                        PagingTool.PAGING_DEBUG -> Icons.Default.BugReport
                        PagingTool.TROUBLESHOOTING -> Icons.Default.Warning
                    },
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = tool.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = tool.titleZh,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = tool.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
                Text(
                    text = tool.descriptionZh,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }
        }
    }
}

/**
 * Version Upgrade Tips Card / 版本升级提示卡片
 */
@Composable
private fun VersionUpgradeTipsCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFFF3E0)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "⚠️ Paging 3.5 Upgrade Notes / 升级注意事项",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFE65100)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "• asState requires Kotlin 1.9+ and Compose 1.5+",
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = "• asState 需要 Kotlin 1.9+ 和 Compose 1.5+",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
            Text(
                text = "• Don't mix collectAsLazyPagingItems() with asState()",
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = "• 不要混用 collectAsLazyPagingItems() 和 asState()",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
            Text(
                text = "• Save/restore state on configuration change",
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = "• 在 Configuration Change 时保存/恢复状态",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
    }
}

// =============================================================
// AsStateTemplateScreen — asState 模板生成器
// =============================================================

/**
 * AsState Template Screen / asState 模板生成器
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AsStateTemplateScreen(
    viewModel: Paging35ViewModel,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.templateState.collectAsState()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("asState Template / 模板生成器") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Scenario Selector / 场景选择器
            item {
                Text(
                    text = "Scenario / 场景",
                    style = MaterialTheme.typography.titleMedium
                )
            }

            items(PagingScenario.entries) { scenario ->
                ScenarioSelector(
                    scenario = scenario,
                    isSelected = state.selectedScenario == scenario,
                    onSelect = { viewModel.processTemplateIntent(AsStateTemplateIntent.SelectScenario(scenario)) }
                )
            }

            // Configuration Panel / 配置面板
            item {
                ConfigurationPanel(
                    pageSize = state.pageSize,
                    prefetchEnabled = state.prefetchEnabled,
                    onPageSizeChange = { viewModel.processTemplateIntent(AsStateTemplateIntent.UpdatePageSize(it)) },
                    onPrefetchToggle = { viewModel.processTemplateIntent(AsStateTemplateIntent.TogglePrefetch(it)) }
                )
            }

            // Code Preview / 代码预览
            item {
                CodePreviewBlock(
                    code = state.generatedCode,
                    isLoading = state.isGenerating,
                    onCopy = { viewModel.processTemplateIntent(AsStateTemplateIntent.CopyCode) }
                )
            }
        }
    }
}

/**
 * Scenario Selector / 场景选择器
 */
@Composable
private fun ScenarioSelector(
    scenario: PagingScenario,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onSelect),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = isSelected,
                onClick = onSelect
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "${scenario.label} / ${scenario.labelZh}",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal
            )
        }
    }
}

/**
 * Configuration Panel / 配置面板
 */
@Composable
private fun ConfigurationPanel(
    pageSize: Int,
    prefetchEnabled: Boolean,
    onPageSizeChange: (Int) -> Unit,
    onPrefetchToggle: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Configuration / 配置",
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Page Size / 分页大小
            Text(
                text = "Page Size: $pageSize",
                style = MaterialTheme.typography.bodyMedium
            )
            Slider(
                value = pageSize.toFloat(),
                onValueChange = { onPageSizeChange(it.toInt()) },
                valueRange = 10f..50f,
                steps = 7
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Prefetch Toggle / 预取开关
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Enable Prefetch / 启用预取",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f)
                )
                Switch(
                    checked = prefetchEnabled,
                    onCheckedChange = onPrefetchToggle
                )
            }
        }
    }
}

/**
 * Code Preview Block / 代码预览块
 */
@Composable
private fun CodePreviewBlock(
    code: String,
    isLoading: Boolean,
    onCopy: () -> Unit
) {
    val context = LocalContext.current  // Capture for clipboard / 捕获用于剪贴板
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Generated Code / 生成的代码",
                    style = MaterialTheme.typography.titleMedium
                )
                IconButton(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("Paging Code", code)
                        clipboard.setPrimaryClip(clip)
                        onCopy()
                    }
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = "Copy")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Color(0xFF2D2D2D),
                            RoundedCornerShape(8.dp)
                        )
                        .padding(12.dp)
                ) {
                    Text(
                        text = code.ifEmpty { "// Select options to generate code" },
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontFamily = FontFamily.Monospace,
                            color = if (code.isEmpty()) Color.Gray else Color(0xFFE0E0E0)
                        ),
                        modifier = Modifier.horizontalScroll(
                            rememberScrollState()
                        )
                    )
                }
            }
        }
    }
}
