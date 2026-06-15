package com.mvi.kenny.feature.exoplayermedia3migration

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LibraryBooks
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Report
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

// =============================================================
// ExoPlayer2Media3MigrationToolScreen — ExoPlayer 2 → Media3
// Migration Toolkit Main Screen
// =============================================================
/**
 * Main screen for ExoPlayer 2 → Media3 Migration Toolkit
 * ExoPlayer 2 → Media3 迁移工具包主屏幕
 *
 * Architecture: MVI (Model-View-Intent)
 * ViewModel manages State, UI consumes State, emits Intent on user action.
 *
 * @param viewModel ViewModel instance / ViewModel 实例
 *
 * @see ExoPlayer2Media3MigrationToolViewModel State management
 * @see ExoPlayer2Media3MigrationToolContract State/Intent/Effect definitions
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExoPlayer2Media3MigrationToolScreen(
    viewModel: ExoPlayer2Media3MigrationToolViewModel
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // ============================================================
    // Effect Handler — 监听并处理副作用
    // Effect Handler — Listen and handle side effects
    // ============================================================
    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is ExoPlayer2Media3MigrationToolEffect.ShowSnackbar -> {
                    scope.launch {
                        snackbarHostState.showSnackbar(effect.message)
                    }
                }
                is ExoPlayer2Media3MigrationToolEffect.CopyToClipboard -> {
                    // Clipboard handled by UI directly / 剪贴板由 UI 直接处理
                }
                is ExoPlayer2Media3MigrationToolEffect.ScanComplete -> {
                    // Handled via state update / 通过状态更新处理
                }
                is ExoPlayer2Media3MigrationToolEffect.NavigateToTab -> {
                    viewModel.sendIntent(ExoPlayer2Media3MigrationToolIntent.SelectTab(effect.tab))
                }
                is ExoPlayer2Media3MigrationToolEffect.StepApplied -> {
                    // Handled via state update / 通过状态更新处理
                }
                is ExoPlayer2Media3MigrationToolEffect.ReportGenerated -> {
                    // Handled via state update / 通过状态更新处理
                }
            }
        }
    }

    // ============================================================
    // Main Scaffold / 主脚手架
    // ============================================================
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Tab Row / Tab 行
            MigrationTabRow(
                activeTab = state.activeTab,
                onTabSelected = { viewModel.sendIntent(ExoPlayer2Media3MigrationToolIntent.SelectTab(it)) }
            )

            // Content / 内容区
            AnimatedContent(
                targetState = state.activeTab,
                transitionSpec = { fadeIn(tween(300)) togetherWith fadeOut(tween(300)) },
                label = "TabContentTransition"
            ) { tab ->
                when (tab) {
                    MigrationTab.HOME -> HomeTabContent(
                        state = state,
                        onQuickScan = { viewModel.sendIntent(ExoPlayer2Media3MigrationToolIntent.StartQuickScan) },
                        onNavigateToMigration = { viewModel.sendIntent(ExoPlayer2Media3MigrationToolIntent.NavigateToMigration) },
                        onNavigateToReport = { viewModel.sendIntent(ExoPlayer2Media3MigrationToolIntent.NavigateToReport) },
                        onScanComplete = { }
                    )
                    MigrationTab.SCANNER -> ScannerTabContent(
                        state = state,
                        onStartScan = { path -> viewModel.sendIntent(ExoPlayer2Media3MigrationToolIntent.StartScan(path ?: "")) },
                        onCancelScan = { viewModel.sendIntent(ExoPlayer2Media3MigrationToolIntent.CancelScan) },
                        onSelectResult = { viewModel.sendIntent(ExoPlayer2Media3MigrationToolIntent.SelectScanResult(it)) },
                        onClearResult = { viewModel.sendIntent(ExoPlayer2Media3MigrationToolIntent.ClearScanResult) }
                    )
                    MigrationTab.MIGRATION -> MigrationTabContent(
                        state = state,
                        onSelectStep = { viewModel.sendIntent(ExoPlayer2Media3MigrationToolIntent.SelectStep(it)) },
                        onApplyStep = { viewModel.sendIntent(ExoPlayer2Media3MigrationToolIntent.ApplyStep(it)) },
                        onNextStep = { viewModel.sendIntent(ExoPlayer2Media3MigrationToolIntent.NextStep) },
                        onPreviousStep = { viewModel.sendIntent(ExoPlayer2Media3MigrationToolIntent.PreviousStep) },
                        onCopyCode = { viewModel.sendIntent(ExoPlayer2Media3MigrationToolIntent.CopyCode(it)) }
                    )
                    MigrationTab.LIBRARY -> LibraryTabContent(
                        state = state,
                        onSelectLibraryTab = { viewModel.sendIntent(ExoPlayer2Media3MigrationToolIntent.SelectLibraryTab(it)) },
                        onFilterCategory = { viewModel.sendIntent(ExoPlayer2Media3MigrationToolIntent.FilterApiCategory(it)) },
                        onCopyCode = { viewModel.sendIntent(ExoPlayer2Media3MigrationToolIntent.CopyCode(it)) }
                    )
                    MigrationTab.REPORT -> ReportTabContent(
                        state = state,
                        onGenerateReport = { viewModel.sendIntent(ExoPlayer2Media3MigrationToolIntent.GenerateReport) }
                    )
                }
            }
        }
    }
}

// =============================================================
// MigrationTabRow — Tab 行组件
// =============================================================
/**
 * Migration tool tab row / 迁移工具 Tab 行
 */
@Composable
private fun MigrationTabRow(
    activeTab: MigrationTab,
    onTabSelected: (MigrationTab) -> Unit
) {
    ScrollableTabRow(
        selectedTabIndex = MigrationTab.entries.indexOf(activeTab),
        edgePadding = 8.dp
    ) {
        MigrationTab.entries.forEach { tab ->
            Tab(
                selected = activeTab == tab,
                onClick = { onTabSelected(tab) },
                text = {
                    Text(
                        text = tab.title,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                icon = {
                    Icon(
                        imageVector = when (tab) {
                            MigrationTab.HOME -> Icons.Default.Report
                            MigrationTab.SCANNER -> Icons.Default.Search
                            MigrationTab.MIGRATION -> Icons.Default.Build
                            MigrationTab.LIBRARY -> Icons.Default.LibraryBooks
                            MigrationTab.REPORT -> Icons.Default.Description
                        },
                        contentDescription = tab.title
                    )
                }
            )
        }
    }
}

// =============================================================
// HomeTabContent — 首页内容
// =============================================================
/**
 * Home tab content / 首页 Tab 内容
 * Shows hero card, quick actions, and compliance status
 */
@Composable
private fun HomeTabContent(
    state: ExoPlayer2Media3MigrationToolState,
    onQuickScan: () -> Unit,
    onNavigateToMigration: () -> Unit,
    onNavigateToReport: () -> Unit,
    onScanComplete: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Card / Hero 卡片
        item {
            HeroCard(state = state)
        }

        // Quick Actions / 快速操作
        item {
            QuickActionsSection(
                onQuickScan = onQuickScan,
                onNavigateToMigration = onNavigateToMigration,
                onNavigateToReport = onNavigateToReport,
                isScanning = state.scanStatus == ScanStatus.SCANNING
            )
        }

        // Compliance Banner / 合规状态横幅
        item {
            ComplianceBanner(state = state)
        }

        // Scan Summary / 扫描摘要
        if (state.scanResults.isNotEmpty()) {
            item {
                ScanSummaryCard(state = state)
            }
        }
    }
}

// =============================================================
// HeroCard — Hero 卡片
// =============================================================
/**
 * Hero card showing overall migration status / 显示总体迁移状态的 Hero 卡片
 */
@Composable
private fun HeroCard(state: ExoPlayer2Media3MigrationToolState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Build,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "ExoPlayer 2 → Media3",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Android 17 背景音频合规迁移工具",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Progress Ring / 进度环
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                ProgressRing(
                    progress = state.migrationProgress,
                    label = "${(state.migrationProgress * 100).toInt()}%",
                    sublabel = "迁移进度"
                )
                Spacer(modifier = Modifier.width(24.dp))
                ProgressRing(
                    progress = if (state.detectedPlayerInstances > 0) {
                        state.migratedCount.toFloat() / state.detectedPlayerInstances
                    } else 0f,
                    label = "${state.detectedPlayerInstances}",
                    sublabel = "检测到使用"
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Android 17 (API 37) 严格强制背景音频限制，ExoPlayer 2 已完全失效",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f)
            )
        }
    }
}

// =============================================================
// ProgressRing — 环形进度图
// =============================================================
/**
 * Circular progress ring / 环形进度图
 *
 * @param progress Progress value 0.0~1.0 / 进度值 0.0~1.0
 * @param label Main label / 主标签
 * @param sublabel Sub label / 副标签
 */
@Composable
private fun ProgressRing(
    progress: Float,
    label: String,
    sublabel: String
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(500),
        label = "ProgressAnimation"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(80.dp)
        ) {
            Canvas(modifier = Modifier.size(80.dp)) {
                drawArc(
                    color = Color.Gray.copy(alpha = 0.2f),
                    startAngle = -90f,
                    sweepAngle = 360f,
                    useCenter = false,
                    style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
                )
                drawArc(
                    color = Color(0xFF1976D2),
                    startAngle = -90f,
                    sweepAngle = animatedProgress * 360f,
                    useCenter = false,
                    style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
                )
            }
            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = sublabel,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
    }
}

// =============================================================
// QuickActionsSection — 快速操作区
// =============================================================
/**
 * Quick action buttons section / 快速操作按钮区
 */
@Composable
private fun QuickActionsSection(
    onQuickScan: () -> Unit,
    onNavigateToMigration: () -> Unit,
    onNavigateToReport: () -> Unit,
    isScanning: Boolean
) {
    Column {
        Text(
            text = "快速操作 / Quick Actions",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Quick Scan / 快速扫描
            QuickActionCard(
                icon = Icons.Default.Search,
                title = "一键扫描",
                subtitle = "检测 ExoPlayer 2 使用",
                onClick = onQuickScan,
                isLoading = isScanning,
                modifier = Modifier.weight(1f)
            )

            // Start Migration / 开始迁移
            QuickActionCard(
                icon = Icons.Default.Build,
                title = "开始迁移",
                subtitle = "分步骤向导",
                onClick = onNavigateToMigration,
                modifier = Modifier.weight(1f)
            )

            // View Report / 查看报告
            QuickActionCard(
                icon = Icons.Default.Description,
                title = "合规报告",
                subtitle = "生成 Android 17 报告",
                onClick = onNavigateToReport,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

// =============================================================
// QuickActionCard — 快速操作卡片
// =============================================================
/**
 * Quick action card / 快速操作卡片
 */
@Composable
private fun QuickActionCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false
) {
    Card(
        modifier = modifier.clickable(enabled = !isLoading, onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp))
            } else {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    }
}

// =============================================================
// ComplianceBanner — 合规状态横幅
// =============================================================
/**
 * Compliance status banner / 合规状态横幅
 */
@Composable
private fun ComplianceBanner(state: ExoPlayer2Media3MigrationToolState) {
    val backgroundColor by animateColorAsState(
        targetValue = when {
            state.isCompliant == true -> Color(0xFF4CAF50).copy(alpha = 0.1f)
            state.isCompliant == false -> Color(0xFFD32F2F).copy(alpha = 0.1f)
            else -> Color(0xFFFF9800).copy(alpha = 0.1f)
        },
        label = "BannerColorAnimation"
    )

    val borderColor by animateColorAsState(
        targetValue = when {
            state.isCompliant == true -> Color(0xFF4CAF50)
            state.isCompliant == false -> Color(0xFFD32F2F)
            else -> Color(0xFFFF9800)
        },
        label = "BannerBorderAnimation"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, borderColor, RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = when {
                    state.isCompliant == true -> Icons.Default.CheckCircle
                    state.isCompliant == false -> Icons.Default.Error
                    else -> Icons.Default.Warning
                },
                contentDescription = null,
                tint = borderColor,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = when {
                        state.isCompliant == true -> "✅ Android 17 合规"
                        state.isCompliant == false -> "❌ Android 17 不合规"
                        else -> "⚠️ 尚未检测"
                    },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = state.complianceStatusText,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
        }
    }
}

// =============================================================
// ScanSummaryCard — 扫描摘要卡片
// =============================================================
/**
 * Scan summary card / 扫描摘要卡片
 */
@Composable
private fun ScanSummaryCard(state: ExoPlayer2Media3MigrationToolState) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "📊 扫描结果摘要",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                IssueCountBadge(
                    count = state.criticalCount,
                    label = "P0 严重",
                    color = IssueSeverity.CRITICAL.color
                )
                IssueCountBadge(
                    count = state.warningCount,
                    label = "P1 警告",
                    color = IssueSeverity.WARNING.color
                )
                IssueCountBadge(
                    count = state.infoCount,
                    label = "P2 信息",
                    color = IssueSeverity.INFO.color
                )
                IssueCountBadge(
                    count = state.migratedCount,
                    label = "已迁移",
                    color = Color(0xFF4CAF50)
                )
            }
        }
    }
}

// =============================================================
// IssueCountBadge — 问题数量徽章
// =============================================================
/**
 * Issue count badge / 问题数量徽章
 */
@Composable
private fun IssueCountBadge(count: Int, label: String, color: Color) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.1f))
                .border(2.dp, color, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
    }
}

// =============================================================
// ScannerTabContent — 扫描 Tab 内容
// =============================================================
/**
 * Scanner tab content / 扫描 Tab 内容
 */
@Composable
private fun ScannerTabContent(
    state: ExoPlayer2Media3MigrationToolState,
    onStartScan: (String?) -> Unit,
    onCancelScan: () -> Unit,
    onSelectResult: (ScanResult) -> Unit,
    onClearResult: () -> Unit
) {
    var modulePath by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Scan Control / 扫描控制区
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "🔍 ExoPlayer 2 使用扫描器",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = modulePath,
                    onValueChange = { modulePath = it },
                    label = { Text("模块/项目路径（可选）") },
                    placeholder = { Text("app / library/core") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (state.scanStatus == ScanStatus.SCANNING) {
                        Button(
                            onClick = onCancelScan,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error
                            ),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Warning, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("取消")
                        }
                    } else {
                        Button(
                            onClick = { onStartScan(modulePath.ifEmpty { null }) },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Search, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("开始扫描")
                        }
                    }
                }

                if (state.scanStatus == ScanStatus.SCANNING) {
                    Spacer(modifier = Modifier.height(12.dp))
                    LinearProgressIndicator(
                        progress = { state.scanProgress },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        text = "扫描中... ${state.scanPercentage}%",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Scan Results / 扫描结果
        if (state.scanResults.isNotEmpty()) {
            Text(
                text = "📋 扫描结果 (${state.scanResults.size} 处)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(state.sortedScanResults) { result ->
                    ScanResultCard(
                        result = result,
                        onClick = { onSelectResult(result) }
                    )
                }
            }
        } else if (state.scanStatus == ScanStatus.COMPLETED) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Color(0xFF4CAF50),
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "未检测到 ExoPlayer 2 使用",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }
    }
}

// =============================================================
// ScanResultCard — 扫描结果卡片
// =============================================================
/**
 * Scan result card / 扫描结果卡片
 */
@Composable
private fun ScanResultCard(
    result: ScanResult,
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
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Severity badge / 严重程度徽章
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(result.severity.color.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when (result.severity) {
                        IssueSeverity.CRITICAL -> Icons.Default.Error
                        IssueSeverity.WARNING -> Icons.Default.Warning
                        IssueSeverity.INFO -> Icons.Default.Info
                    },
                    contentDescription = null,
                    tint = result.severity.color,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = result.usageType.label,
                        style = MaterialTheme.typography.labelMedium,
                        color = result.severity.color,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Line ${result.lineNumber}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }

                Text(
                    text = result.filePath.split("/").takeLast(2).joinToString("/"),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )

                Text(
                    text = result.description,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

// =============================================================
// MigrationTabContent — 迁移向导 Tab 内容
// =============================================================
/**
 * Migration tab content / 迁移向导 Tab 内容
 */
@Composable
private fun MigrationTabContent(
    state: ExoPlayer2Media3MigrationToolState,
    onSelectStep: (MigrationStep) -> Unit,
    onApplyStep: (Int) -> Unit,
    onNextStep: () -> Unit,
    onPreviousStep: () -> Unit,
    onCopyCode: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Stepper / 步骤指示器
        MigrationStepper(
            steps = state.migrationSteps,
            currentStep = state.currentStep,
            onStepClick = { onSelectStep(state.migrationSteps[it]) }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Current Step Content / 当前步骤内容
        val currentStepData = state.migrationSteps.getOrNull(state.currentStep)
        currentStepData?.let { step ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = step.title,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        if (step.isCompleted) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "已完成",
                                tint = Color(0xFF4CAF50)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = step.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Code diff / 代码 Diff
                    step.templateCode?.let { code ->
                        CodeBlock(
                            code = code,
                            onCopy = { onCopyCode(code) }
                        )
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    // Apply button / 应用按钮
                    if (!step.isCompleted) {
                        Button(
                            onClick = { onApplyStep(step.index) },
                            enabled = !state.isApplyingStep,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            if (state.isApplyingStep) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            } else {
                                Icon(Icons.Default.Check, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("应用此步骤")
                            }
                        }
                    } else {
                        OutlinedButton(
                            onClick = { },
                            enabled = false,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF4CAF50))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("已应用 / Applied")
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Navigation buttons / 导航按钮
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onPreviousStep,
                enabled = state.currentStep > 0,
                modifier = Modifier.weight(1f)
            ) {
                Text("← 上一步")
            }
            Button(
                onClick = onNextStep,
                enabled = state.currentStep < state.migrationSteps.size - 1,
                modifier = Modifier.weight(1f)
            ) {
                Text("下一步 →")
            }
        }
    }
}

// =============================================================
// MigrationStepper — 迁移步骤指示器
// =============================================================
/**
 * Migration step indicator / 迁移步骤指示器
 */
@Composable
private fun MigrationStepper(
    steps: List<MigrationStep>,
    currentStep: Int,
    onStepClick: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        steps.forEachIndexed { index, step ->
            val isActive = index == currentStep
            val isCompleted = step.isCompleted

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        when {
                            isActive -> MaterialTheme.colorScheme.primary
                            isCompleted -> Color(0xFF4CAF50)
                            else -> MaterialTheme.colorScheme.surfaceVariant
                        }
                    )
                    .clickable { onStepClick(index) }
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isCompleted) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                    Text(
                        text = "Step ${index + 1}",
                        style = MaterialTheme.typography.labelMedium,
                        color = if (isActive || isCompleted) Color.White
                        else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

// =============================================================
// LibraryTabContent — 参考库 Tab 内容
// =============================================================
/**
 * Library tab content / 参考库 Tab 内容
 */
@Composable
private fun LibraryTabContent(
    state: ExoPlayer2Media3MigrationToolState,
    onSelectLibraryTab: (LibraryTab) -> Unit,
    onFilterCategory: (ApiCategory?) -> Unit,
    onCopyCode: (String) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Library Sub-Tab Row / 参考库子 Tab 行
        ScrollableTabRow(
            selectedTabIndex = LibraryTab.entries.indexOf(state.libraryTab),
            edgePadding = 8.dp
        ) {
            LibraryTab.entries.forEach { tab ->
                Tab(
                    selected = state.libraryTab == tab,
                    onClick = { onSelectLibraryTab(tab) },
                    text = { Text(tab.title) }
                )
            }
        }

        when (state.libraryTab) {
            LibraryTab.API_TABLE -> ApiComparisonContent(
                state = state,
                onFilterCategory = onFilterCategory,
                onCopyCode = onCopyCode
            )
            LibraryTab.TEMPLATES -> TemplatesContent(
                state = state,
                onCopyCode = onCopyCode
            )
            LibraryTab.FAQ -> FaqContent()
        }
    }
}

// =============================================================
// ApiComparisonContent — API 对照内容
// =============================================================
/**
 * API comparison content / API 对照内容
 */
@Composable
private fun ApiComparisonContent(
    state: ExoPlayer2Media3MigrationToolState,
    onFilterCategory: (ApiCategory?) -> Unit,
    onCopyCode: (String) -> Unit
) {
    Column(
        modifier = Modifier.padding(16.dp)
    ) {
        // Category filter / 类别过滤
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = state.selectedApiCategory == null,
                onClick = { onFilterCategory(null) },
                label = { Text("全部") }
            )
            ApiCategory.entries.forEach { category ->
                FilterChip(
                    selected = state.selectedApiCategory == category,
                    onClick = { onFilterCategory(category) },
                    label = { Text(category.label.split(" / ")[0]) }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        val filteredComparisons = if (state.selectedApiCategory != null) {
            state.apiComparisons.filter { it.category == state.selectedApiCategory }
        } else {
            state.apiComparisons
        }

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(filteredComparisons) { comparison ->
                ApiComparisonCard(
                    comparison = comparison,
                    onCopyExo = { onCopyCode(comparison.exoPlayer2Api) },
                    onCopyMedia3 = { onCopyCode(comparison.media3Api) }
                )
            }
        }
    }
}

// =============================================================
// ApiComparisonCard — API 对照卡片
// =============================================================
/**
 * API comparison card / API 对照卡片
 */
@Composable
private fun ApiComparisonCard(
    comparison: ApiComparisonItem,
    onCopyExo: () -> Unit,
    onCopyMedia3: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            FilterChip(
                selected = false,
                onClick = { },
                label = { Text(comparison.category.label.split(" / ")[0]) }
            )

            Spacer(modifier = Modifier.height(8.dp))

            // ExoPlayer 2 API / ExoPlayer 2 API
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .background(
                            Color(0xFFD32F2F).copy(alpha = 0.1f),
                            RoundedCornerShape(4.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "ExoPlayer 2",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFFD32F2F),
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                IconButton(onClick = onCopyExo) {
                    Icon(Icons.Default.ContentCopy, contentDescription = "复制", modifier = Modifier.size(16.dp))
                }
            }
            Text(
                text = comparison.exoPlayer2Api,
                style = MaterialTheme.typography.bodySmall,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(4.dp))
                    .padding(8.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Media3 API / Media3 API
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .background(
                            Color(0xFF1976D2).copy(alpha = 0.1f),
                            RoundedCornerShape(4.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "Media3",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF1976D2),
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                IconButton(onClick = onCopyMedia3) {
                    Icon(Icons.Default.ContentCopy, contentDescription = "复制", modifier = Modifier.size(16.dp))
                }
            }
            Text(
                text = comparison.media3Api,
                style = MaterialTheme.typography.bodySmall,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF1976D2).copy(alpha = 0.05f), RoundedCornerShape(4.dp))
                    .padding(8.dp)
            )

            if (comparison.notes.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = comparison.notes,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }
    }
}

// =============================================================
// TemplatesContent — 配置模板内容
// =============================================================
/**
 * Templates content / 配置模板内容
 */
@Composable
private fun TemplatesContent(
    state: ExoPlayer2Media3MigrationToolState,
    onCopyCode: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(state.migrationSteps) { step ->
            step.templateCode?.let { code ->
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = step.title,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold
                            )
                            IconButton(onClick = { onCopyCode(code) }) {
                                Icon(Icons.Default.ContentCopy, contentDescription = "复制")
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        CodeBlock(
                            code = code,
                            onCopy = { onCopyCode(code) }
                        )
                    }
                }
            }
        }
    }
}

// =============================================================
// FaqContent — 常见问题内容
// =============================================================
/**
 * FAQ content / 常见问题内容
 */
@Composable
private fun FaqContent() {
    val faqs = listOf(
        FaqItem(
            q = "ExoPlayer 2 和 Media3 可以同时使用吗？",
            a = "不可以。两者都有 Player 接口，同时使用会产生冲突。迁移期间需要完全替换依赖，不能并存。"
        ),
        FaqItem(
            q = "Android 17 对后台音频有什么硬性要求？",
            a = "所有音频播放必须通过 MediaSessionService + 前台服务实现。不启动前台服务直接播放音频将完全失效。"
        ),
        FaqItem(
            q = "Media3 的音频焦点处理和 ExoPlayer 2 有什么不同？",
            a = "ExoPlayer 2 需要手动调用 AudioFocusRequest.requestAudioFocus()，Media3 通过 AudioFocusMember 自动处理，无需手动管理。"
        ),
        FaqItem(
            q = "Notification 权限有什么变化？",
            a = "MediaSessionService 在 Android 13+ 需要 POST_NOTIFICATIONS 运行时权限，需要在代码中动态请求。"
        ),
        FaqItem(
            q = "迁移期间可以分步骤进行吗？",
            a = "可以。建议按依赖替换 → Manifest → Player API → MediaSession → AudioFocus 的顺序逐步迁移，每次小步提交。"
        ),
        FaqItem(
            q = "哪些 App 最需要立即迁移？",
            a = "音乐播放器、播客、有声书、新闻类、健身/正念/学习类 App 风险最高，这些类型的 App 几乎都需要后台音频能力。"
        )
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(faqs) { faq ->
            FaqCard(faq = faq)
        }
    }
}

// =============================================================
// FaqItem & FaqCard
// =============================================================
private data class FaqItem(val q: String, val a: String)

@Composable
private fun FaqCard(faq: FaqItem) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded }
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Q: ${faq.q}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = if (expanded) Icons.AutoMirrored.Filled.ArrowForward
                        else Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .size(20.dp)
                )
            }

            if (expanded) {
                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "A: ${faq.a}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
        }
    }
}

// =============================================================
// ReportTabContent — 合规报告 Tab 内容
// =============================================================
/**
 * Report tab content / 合规报告 Tab 内容
 */
@Composable
private fun ReportTabContent(
    state: ExoPlayer2Media3MigrationToolState,
    onGenerateReport: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Generate Report Button / 生成报告按钮
        Button(
            onClick = onGenerateReport,
            enabled = !state.isGeneratingReport && state.scanResults.isNotEmpty(),
            modifier = Modifier.fillMaxWidth()
        ) {
            if (state.isGeneratingReport) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Icon(Icons.Default.Description, contentDescription = null)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(if (state.isGeneratingReport) "生成中..." else "📊 生成合规报告")
        }

        if (state.scanResults.isEmpty() && !state.isGeneratingReport) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Description,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "请先运行扫描",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                    Text(
                        text = "扫描完成后再生成合规报告",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    )
                }
            }
        } else {
            state.complianceReport?.let { report ->
                Spacer(modifier = Modifier.height(16.dp))

                // Score Card / 评分卡
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = when (report.level) {
                            ComplianceLevel.COMPLIANT -> Color(0xFF4CAF50).copy(alpha = 0.1f)
                            ComplianceLevel.PARTIAL -> Color(0xFFFF9800).copy(alpha = 0.1f)
                            ComplianceLevel.NON_COMPLIANT -> Color(0xFFD32F2F).copy(alpha = 0.1f)
                        }
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "📊 合规评分",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        ProgressRing(
                            progress = report.score / 100f,
                            label = "${report.score}",
                            sublabel = "/100"
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = report.level.label,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = report.level.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Issue Stats / 问题统计
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "问题统计 / Issue Statistics",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            IssueCountBadge(
                                count = report.criticalCount,
                                label = "P0",
                                color = IssueSeverity.CRITICAL.color
                            )
                            IssueCountBadge(
                                count = report.warningCount,
                                label = "P1",
                                color = IssueSeverity.WARNING.color
                            )
                            IssueCountBadge(
                                count = report.infoCount,
                                label = "P2",
                                color = IssueSeverity.INFO.color
                            )
                            IssueCountBadge(
                                count = report.migratedCount,
                                label = "已迁移",
                                color = Color(0xFF4CAF50)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Remaining Issues / 剩余问题
                if (report.remainingIssues.isNotEmpty()) {
                    Text(
                        text = "剩余问题 (${report.remainingIssues.size})",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(report.remainingIssues.take(5)) { issue ->
                            ScanResultCard(
                                result = issue,
                                onClick = { }
                            )
                        }
                    }
                }
            }
        }
    }
}

// =============================================================
// CodeBlock — 代码块组件
// =============================================================
/**
 * Code block component with copy button / 带复制按钮的代码块组件
 *
 * @param code Code content / 代码内容
 * @param onCopy Callback when copy button is clicked / 复制按钮点击时的回调
 */
@Composable
private fun CodeBlock(
    code: String,
    onCopy: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF0F172A)
        )
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(
                    onClick = onCopy,
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "复制",
                        tint = Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "复制",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }
            }
            Text(
                text = code,
                style = MaterialTheme.typography.bodySmall,
                fontFamily = FontFamily.Monospace,
                color = Color.White.copy(alpha = 0.9f),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}