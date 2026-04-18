package com.mvi.kenny.feature.wifianalyzer

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.VerticalDivider
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.collectLatest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WifiAnalyzerScreen(viewModel: WifiAnalyzerViewModel = viewModel()) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var showConfirmDialog by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is WifiAnalyzerEffect.ShowToast -> snackbarHostState.showSnackbar(effect.message)
                is WifiAnalyzerEffect.ReportExported -> snackbarHostState.showSnackbar("已导出: ${effect.file}")
                is WifiAnalyzerEffect.ScanComplete -> snackbarHostState.showSnackbar("扫描完成 / Scan complete")
                is WifiAnalyzerEffect.NavigateToCode -> snackbarHostState.showSnackbar("导航到: ${effect.filePath}:${effect.lineNumber}")
                is WifiAnalyzerEffect.ShowConfirmDialog -> showConfirmDialog = true
            }
        }
    }
    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = { Text("开始扫描 / Start Scan") },
            text = { Text("即将开始 Wi-Fi API 扫描...") },
            confirmButton = {
                TextButton(onClick = {
                    showConfirmDialog = false
                    viewModel.processIntent(WifiAnalyzerIntent.StartScan(state.selectedModule ?: "app"))
                }) { Text("开始 / Start") }
            },
            dismissButton = { TextButton(onClick = { showConfirmDialog = false }) { Text("取消 / Cancel") } }
        )
    }
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            if (!state.bannerDismissed) {
                Android17StatusBanner { viewModel.processIntent(WifiAnalyzerIntent.DismissBanner) }
            }
            TabRow(
                selectedTabIndex = state.currentTab.ordinal,
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ) {
                WifiTab.entries.forEach { tab ->
                    Tab(
                        selected = state.currentTab == tab,
                        onClick = { viewModel.processIntent(WifiAnalyzerIntent.SwitchTab(tab)) },
                        text = { Text(tab.title, fontSize = 12.sp, maxLines = 1) },
                        icon = {
                            val icon: ImageVector = when (tab) {
                                WifiTab.Dashboard -> Icons.Default.Analytics
                                WifiTab.Scanner -> Icons.Default.Search
                                WifiTab.Alternatives -> Icons.Default.SwapVert
                                WifiTab.Reports -> Icons.Default.Description
                                WifiTab.Settings -> Icons.Default.Settings
                            }
                            Icon(icon, null, Modifier.size(18.dp))
                        }
                    )
                }
            }
            when (state.currentTab) {
                WifiTab.Dashboard -> DashboardTab(
                    state = state,
                    onStartScan = { viewModel.processIntent(WifiAnalyzerIntent.SwitchTab(WifiTab.Scanner)) },
                    onViewAlternatives = { viewModel.processIntent(WifiAnalyzerIntent.SwitchTab(WifiTab.Alternatives)) },
                    onViewReports = { viewModel.processIntent(WifiAnalyzerIntent.SwitchTab(WifiTab.Reports)) }
                )
                WifiTab.Scanner -> ScannerTab(
                    state = state,
                    onStartScan = { showConfirmDialog = true },
                    onCancelScan = { viewModel.processIntent(WifiAnalyzerIntent.CancelScan) },
                    onFilterSeverity = { viewModel.processIntent(WifiAnalyzerIntent.FilterBySeverity(it)) },
                    onSelectModule = { viewModel.processIntent(WifiAnalyzerIntent.SelectModule(it)) },
                    onNavigateToCode = { _, _ -> }
                )
                WifiTab.Alternatives -> AlternativesTab(
                    state = state,
                    onSelectAlternativeType = { viewModel.processIntent(WifiAnalyzerIntent.SelectAlternativeType(it)) },
                    onToggleChecklistItem = { viewModel.processIntent(WifiAnalyzerIntent.ToggleChecklistItem(it)) }
                )
                WifiTab.Reports -> ReportsTab(
                    state = state,
                    onGenerateReport = { viewModel.processIntent(WifiAnalyzerIntent.GenerateReport) },
                    onExportReport = { viewModel.processIntent(WifiAnalyzerIntent.ExportReport(it)) }
                )
                WifiTab.Settings -> SettingsTab(
                    state = state,
                    onUpdateScanConfig = { viewModel.processIntent(WifiAnalyzerIntent.UpdateScanConfig(it)) }
                )
            }
        }
    }
}

@Composable
private fun Android17StatusBanner(onDismiss: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFB3261E).copy(alpha = 0.15f))
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Warning, null, tint = Color(0xFFB3261E), modifier = Modifier.size(24.dp))
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = "Android 17 Beta 4 Wi-Fi 问题",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Wi-Fi 扫描 API 在 Android 17 中受到重大限制",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
            IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, "关闭") }
        }
    }
}

@Composable
private fun DashboardTab(
    state: WifiAnalyzerState,
    onStartScan: () -> Unit,
    onViewAlternatives: () -> Unit,
    onViewReports: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        item { WiFiHealthScoreCard(state.healthScore) }
        item { AffectedAPIsCard(state.affectedApis) }
        item { QuickActionsCard(onStartScan, onViewAlternatives, onViewReports) }
        item { RecentScansCard(state.recentScans) }
    }
}

@Composable
private fun WiFiHealthScoreCard(score: Int) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(120.dp)) {
                val animatedScore by animateFloatAsState(score.toFloat(), label = "score")
                Canvas(Modifier.size(120.dp)) {
                    val sw = 12.dp.toPx()
                    val r = (size.minDimension - sw) / 2
                    val c = Offset(size.width / 2, size.height / 2)
                    drawArc(
                        color = Color.Gray.copy(alpha = 0.3f),
                        startAngle = -90f,
                        sweepAngle = 360f,
                        useCenter = false,
                        topLeft = Offset(c.x - r, c.y - r),
                        size = Size(r * 2, r * 2),
                        style = Stroke(width = sw, cap = StrokeCap.Round)
                    )
                    val sc = when {
                        score >= 70 -> Color(0xFF4CAF50)
                        score >= 40 -> Color(0xFFFF9800)
                        else -> Color(0xFFF44336)
                    }
                    drawArc(
                        color = sc,
                        startAngle = -90f,
                        sweepAngle = (animatedScore / 100f) * 360f,
                        useCenter = false,
                        topLeft = Offset(c.x - r, c.y - r),
                        size = Size(r * 2, r * 2),
                        style = Stroke(width = sw, cap = StrokeCap.Round)
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "$score",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = when {
                            score >= 70 -> Color(0xFF4CAF50)
                            score >= 40 -> Color(0xFFFF9800)
                            else -> Color(0xFFF44336)
                        }
                    )
                    Text(text = "健康度", style = MaterialTheme.typography.labelSmall)
                }
            }
            Spacer(Modifier.width(24.dp))
            Column {
                Text(
                    text = "Wi-Fi 兼容性评分",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = when {
                        score >= 70 -> "良好 — 兼容性风险较低"
                        score >= 40 -> "中等 — 需要关注并规划迁移"
                        else -> "严重 — 立即需要处理"
                    },
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = when {
                        score >= 70 -> "Good — Low risk"
                        score >= 40 -> "Warning — Plan migration"
                        else -> "Critical — Immediate action"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }
    }
}

@Composable
private fun AffectedAPIsCard(apis: List<AffectedApi>) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(
                text = "受影响的关键 API",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(8.dp))
            if (apis.isEmpty()) {
                Text(
                    text = "暂未扫描 / No scan data",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            } else {
                apis.take(4).forEach { api ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(Modifier.size(8.dp).clip(CircleShape).background(Color(0xFFB3261E)))
                        Spacer(Modifier.width(8.dp))
                        Column(Modifier.weight(1f)) {
                            Text(api.name, style = MaterialTheme.typography.bodySmall, fontFamily = FontFamily.Monospace)
                            Text(
                                text = api.description,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Text(
                            text = "API ${api.minApiLevel}+",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    if (api != apis.last()) HorizontalDivider(Modifier.padding(vertical = 4.dp))
                }
            }
        }
    }
}

@Composable
private fun QuickActionsCard(
    onStartScan: () -> Unit,
    onViewAlternatives: () -> Unit,
    onViewReports: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(
                text = "快捷操作 / Quick Actions",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                QuickActionButton(Icons.Default.Search, "立即扫描", "Start Scan", onStartScan)
                QuickActionButton(Icons.Default.SwapVert, "替代方案", "Alternatives", onViewAlternatives)
                QuickActionButton(Icons.Default.Description, "生成报告", "Reports", onViewReports)
            }
        }
    }
}

@Composable
private fun QuickActionButton(
    icon: ImageVector,
    label: String,
    sub: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(12.dp)
    ) {
        Icon(icon, label, Modifier.size(32.dp), tint = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.height(4.dp))
        Text(label, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Medium)
        Text(
            text = sub,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
            fontSize = 10.sp
        )
    }
}

@Composable
private fun RecentScansCard(recentScans: List<RecentScan>) {
    val dateFormat = remember { SimpleDateFormat("MM-dd HH:mm", Locale.getDefault()) }
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(
                text = "最近扫描 / Recent Scans",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(8.dp))
            if (recentScans.isEmpty()) {
                Text(
                    text = "暂无扫描记录 / No scan records",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            } else {
                recentScans.take(3).forEach { scan ->
                    val color = when {
                        scan.score >= 70 -> Color(0xFF4CAF50)
                        scan.score >= 40 -> Color(0xFFFF9800)
                        else -> Color(0xFFF44336)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(scan.moduleName, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
                            Text(
                                text = dateFormat.format(Date(scan.timestamp)),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                        }
                        Box(
                            Modifier.clip(RoundedCornerShape(4.dp))
                                .background(color.copy(alpha = 0.2f))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "${scan.score} 分",
                                style = MaterialTheme.typography.labelSmall,
                                color = color,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    if (scan != recentScans.take(3).last()) HorizontalDivider(Modifier.padding(vertical = 4.dp))
                }
            }
        }
    }
}

@Composable
private fun ScannerTab(
    state: WifiAnalyzerState,
    onStartScan: () -> Unit,
    onCancelScan: () -> Unit,
    onFilterSeverity: (Severity?) -> Unit,
    onSelectModule: (String) -> Unit,
    onNavigateToCode: (String, Int) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        ModuleSelector(state.selectedModule, onSelectModule)
        ScanControlRow(state.isScanning, onStartScan, onCancelScan)
        ScanProgressIndicator(state.scanProgress, state.isScanning)
        SeverityFilterChips(state.selectedSeverity, onFilterSeverity, state.p0Count, state.p1Count, state.p2Count)
        AffectedCodeList(state.filteredScanResults, onNavigateToCode)
    }
}

@Composable
private fun ModuleSelector(selectedModule: String?, onSelectModule: (String) -> Unit) {
    val modules = remember { listOf("app", "feature", "core", "data", "common") }
    Column(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        Text(
            text = "选择模块 / Select Module",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            modules.forEach { module ->
                FilterChip(
                    selected = selectedModule == module,
                    onClick = { onSelectModule(module) },
                    label = { Text(module) },
                    leadingIcon = if (selectedModule == module) {
                        { Icon(Icons.Default.Check, null, Modifier.size(16.dp)) }
                    } else null
                )
            }
        }
    }
}

@Composable
private fun ScanControlRow(isScanning: Boolean, onStartScan: () -> Unit, onCancelScan: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (isScanning) {
            TextButton(onClick = onCancelScan) {
                Icon(Icons.Default.Close, null)
                Spacer(Modifier.width(4.dp))
                Text("取消 / Cancel")
            }
        } else {
            TextButton(onClick = onStartScan) {
                Icon(Icons.Default.Search, null)
                Spacer(Modifier.width(4.dp))
                Text("立即扫描 / Start Scan")
            }
        }
    }
}

@Composable
private fun ScanProgressIndicator(progress: Float, isScanning: Boolean) {
    if (!isScanning && progress == 0f) return
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "扫描进度 / Scan Progress", style = MaterialTheme.typography.titleSmall)
            Text(
                text = "${(progress * 100).toInt()}%",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(Modifier.height(8.dp))
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp))
        )
        if (isScanning) {
            Spacer(Modifier.height(4.dp))
            Text(
                text = "正在分析源文件... / Analyzing source files...",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
private fun SeverityFilterChips(
    selectedSeverity: Severity?,
    onFilterSeverity: (Severity?) -> Unit,
    p0: Int, p1: Int, p2: Int
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FilterChip(
            selected = selectedSeverity == null,
            onClick = { onFilterSeverity(null) },
            label = { Text("全部 (${p0 + p1 + p2})") }
        )
        FilterChip(
            selected = selectedSeverity == Severity.P0,
            onClick = { onFilterSeverity(if (selectedSeverity == Severity.P0) null else Severity.P0) },
            label = { Text("P0 ($p0)") },
            colors = androidx.compose.material3.FilterChipDefaults.filterChipColors(
                selectedContainerColor = Severity.P0.color.copy(alpha = 0.2f)
            )
        )
        FilterChip(
            selected = selectedSeverity == Severity.P1,
            onClick = { onFilterSeverity(if (selectedSeverity == Severity.P1) null else Severity.P1) },
            label = { Text("P1 ($p1)") },
            colors = androidx.compose.material3.FilterChipDefaults.filterChipColors(
                selectedContainerColor = Severity.P1.color.copy(alpha = 0.2f)
            )
        )
        FilterChip(
            selected = selectedSeverity == Severity.P2,
            onClick = { onFilterSeverity(if (selectedSeverity == Severity.P2) null else Severity.P2) },
            label = { Text("P2 ($p2)") },
            colors = androidx.compose.material3.FilterChipDefaults.filterChipColors(
                selectedContainerColor = Severity.P2.color.copy(alpha = 0.2f)
            )
        )
    }
}

@Composable
private fun AffectedCodeList(
    results: List<ScanResult>,
    onNavigateToCode: (String, Int) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (results.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.Search, null,
                            Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = "暂未扫描 / No scan results",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                        Text(
                            text = "点击上方「立即扫描」开始分析",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                        )
                    }
                }
            }
        }
        items(results, key = { it.id }) { result ->
            var expanded by remember { mutableStateOf(false) }
            Card(
                modifier = Modifier.fillMaxWidth().clickable { expanded = !expanded },
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            Modifier.clip(RoundedCornerShape(4.dp))
                                .background(result.severity.color.copy(alpha = 0.2f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = result.severity.label,
                                style = MaterialTheme.typography.labelSmall,
                                color = result.severity.color,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = result.apiName,
                            style = MaterialTheme.typography.bodySmall,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.weight(1f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = result.filePath,
                        style = MaterialTheme.typography.labelSmall,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Text(
                        text = "${result.className}.${result.methodName} (line ${result.lineNumber})",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    )
                    if (expanded) {
                        Spacer(Modifier.height(8.dp))
                        HorizontalDivider()
                        Spacer(Modifier.height(8.dp))
                        Box(
                            modifier = Modifier.fillMaxWidth()
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color.Black.copy(alpha = 0.1f))
                                .padding(8.dp)
                        ) {
                            Text(
                                text = result.codeSnippet,
                                style = MaterialTheme.typography.bodySmall,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp
                            )
                        }
                        Spacer(Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "建议: ${result.suggestedFix}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.weight(1f)
                            )
                            TextButton(onClick = { onNavigateToCode(result.filePath, result.lineNumber) }) {
                                Text("查看代码 / View", fontSize = 11.sp)
                                Icon(Icons.Default.ArrowForward, null, Modifier.size(12.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AlternativesTab(
    state: WifiAnalyzerState,
    onSelectAlternativeType: (AlternativeType) -> Unit,
    onToggleChecklistItem: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        AlternativeTypeSelector(state.alternativeType, onSelectAlternativeType)
        when (state.alternativeType) {
            AlternativeType.WIFI_RTT -> WiFiRTTTabContent()
            AlternativeType.NEIGHBOR_REPORTING -> NeighborReportingTabContent()
            AlternativeType.DEGRADATION_STRATEGY -> DegradationStrategyTabContent()
        }
        MigrationChecklistCard(
            checklist = state.migrationChecklist.ifEmpty {
                listOf(
                    ChecklistItem("check_1", "检查项目中所有 WifiManager.startScan() 调用点"),
                    ChecklistItem("check_2", "评估 Wi-Fi RTT 设备支持情况"),
                    ChecklistItem("check_3", "设计降级策略：无可用 Wi-Fi 数据时提示用户"),
                    ChecklistItem("check_4", "实现 WifiRttManager.isAvailable() 设备能力检测"),
                    ChecklistItem("check_5", "添加精确定位权限申请逻辑"),
                    ChecklistItem("check_6", "审查 Wi-Fi 数据使用隐私政策披露"),
                    ChecklistItem("check_7", "生成 Android 17 回归测试用例"),
                    ChecklistItem("check_8", "在 Android 17 设备上进行真机测试")
                )
            },
            onToggleItem = onToggleChecklistItem
        )
    }
}

@Composable
private fun AlternativeTypeSelector(selectedType: AlternativeType, onSelectType: (AlternativeType) -> Unit) {
    TabRow(
        selectedTabIndex = selectedType.ordinal,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        AlternativeType.entries.forEach { type ->
            Tab(
                selected = selectedType == type,
                onClick = { onSelectType(type) },
                text = { Text(type.title, fontSize = 12.sp) }
            )
        }
    }
}

@Composable
private fun WiFiRTTTabContent() {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text(
                        text = "Wi-Fi RTT (IEEE 802.11mc)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "Wi-Fi Round-Trip-Time (RTT) 是 Android 9 引入的精确测距 API，可测量设备到 Wi-Fi 接入点的距离，精度达到 +/- 1-2 米。",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        InfoChip("支持设备", "Android 9+")
                        InfoChip("精度", "+/- 1-2 米")
                    }
                }
            }
        }
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text(
                        text = "API 对照表 / API Mapping",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(8.dp))
                    ApiMappingTable()
                }
            }
        }
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text(
                        text = "RTT 代码示例 / RTT Code Example",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(8.dp))
                    Box(
                        modifier = Modifier.fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.Black.copy(alpha = 0.1f))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = "val wifirttManager = getSystemService(Context.WIFI_RTT_RANGING_SERVICE) as WifiRttManager\nif (!wifirttManager.isAvailable()) { implementDegradationStrategy() }\n\nval req = RangingRequest.Builder().addAccessPoint(scanResult.getBssid()).build()\nwifirttManager.startRanging(req, executor, object : RangingResultCallback() { ... })",
                            style = MaterialTheme.typography.bodySmall,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NeighborReportingTabContent() {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text(
                        text = "Neighbor Reporting (NDP/FRM)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "Neighbor Reporting 是 Wi-Fi Alliance 的 FTM (Fine Timing Measurement) 的一部分，支持室内定位和 Wi-Fi 分析。",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        InfoChip("支持设备", "Android 10+")
                        InfoChip("精度", "+/- 1-3 米")
                    }
                }
            }
        }
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text(
                        text = "与传统扫描对比 / Comparison",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(8.dp))
                    val comparisons = listOf(
                        Triple("精度", "~1-3 米", "依赖 RSSI 精度低"),
                        Triple("功耗", "中等", "较高"),
                        Triple("响应速度", "快速 (~100ms)", "较慢 (~秒级)")
                    )
                    comparisons.forEachIndexed { index, (aspect, nr, trad) ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                                .background(
                                    if (index % 2 == 0) Color.Transparent
                                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                                )
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = aspect,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.weight(0.3f)
                            )
                            Text(
                                text = nr,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.weight(0.35f)
                            )
                            Text(
                                text = trad,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                modifier = Modifier.weight(0.35f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DegradationStrategyTabContent() {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text(
                        text = "优雅降级流程 / Graceful Degradation",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(8.dp))
                    val steps = listOf(
                        "尝试 Wi-Fi RTT 定位" to Color(0xFF4CAF50),
                        "RTT 不可用 -> 尝试 Neighbor Reporting" to Color(0xFF2196F3),
                        "NR 不可用 -> 使用 Cell ID 粗略定位" to Color(0xFFFF9800),
                        "Cell 不可用 -> 提示用户手动输入" to Color(0xFFF44336),
                        "用户拒绝 -> 显示免责声明" to Color(0xFF9E9E9E)
                    )
                    steps.forEachIndexed { index, (step, color) ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            Box(
                                Modifier.size(24.dp).clip(CircleShape).background(color),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${index + 1}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(Modifier.width(8.dp))
                            Text(step, style = MaterialTheme.typography.bodySmall)
                        }
                        if (index < 4) {
                            Box(
                                Modifier.padding(start = 11.dp)
                                    .width(2.dp)
                                    .height(8.dp)
                                    .background(color.copy(alpha = 0.3f))
                            )
                        }
                    }
                }
            }
        }
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text(
                        text = "降级策略配置 / Degradation Config",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(8.dp))
                    var selected by remember { mutableStateOf(setOf("rtt", "manual")) }
                    val options = listOf(
                        "rtt" to ("Wi-Fi RTT" to "优先使用 RTT 精确定位"),
                        "nr" to ("Neighbor Reporting" to "使用 NDP/FRM 邻居报告"),
                        "cell" to ("Cell ID" to "使用基站 ID 进行粗略定位"),
                        "manual" to ("手动输入" to "让用户手动输入位置信息")
                    )
                    options.forEach { (key, pair) ->
                        val (label, desc) = pair
                        Row(
                            modifier = Modifier.fillMaxWidth().clickable {
                                selected = if (selected.contains(key)) selected - key else selected + key
                            }.padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = selected.contains(key),
                                onCheckedChange = { selected = if (it) selected + key else selected - key }
                            )
                            Spacer(Modifier.width(4.dp))
                            Column {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = desc,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MigrationChecklistCard(
    checklist: List<ChecklistItem>,
    onToggleItem: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "迁移清单 / Migration Checklist",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                if (checklist.isNotEmpty()) {
                    Text(
                        text = "${checklist.count { it.isChecked }}/${checklist.size}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            if (checklist.isEmpty()) {
                Text(
                    text = "暂无可用清单 / No checklist available",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            } else {
                checklist.forEach { item ->
                    Row(
                        modifier = Modifier.fillMaxWidth().clickable { onToggleItem(item.id) }.padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = item.isChecked,
                            onCheckedChange = { onToggleItem(item.id) }
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = item.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = if (item.isChecked) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoChip(label: String, value: String) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun ApiMappingTable() {
    val mappings = listOf(
        Triple("WifiManager.startScan()", "WifiRttManager.startRanging()", "测距替代扫描"),
        Triple("WifiInfo.getRssi()", "RangingResult.distanceMm", "精确距离代替信号强度"),
        Triple("WifiInfo.getSSID()", "RangingRequest + BSSID", "通过接入点标识定位"),
        Triple("WifiP2pManager.discoverPeers()", "NDP/FRM Neighbor Reporting", "邻居设备发现")
    )
    Column {
        Row(
            modifier = Modifier.fillMaxWidth()
                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
                .padding(8.dp)
        ) {
            Text(
                text = "旧 API",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            VerticalDivider()
            Text(
                text = "新 API",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            VerticalDivider()
            Text(
                text = "说明",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(0.8f)
            )
        }
        mappings.forEachIndexed { index, (old, new, desc) ->
            Row(
                modifier = Modifier.fillMaxWidth().padding(8.dp)
                    .background(
                        if (index % 2 == 0) Color.Transparent
                        else MaterialTheme.colorScheme.surface
                    )
            ) {
                Text(
                    text = old,
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp,
                    modifier = Modifier.weight(1f)
                )
                VerticalDivider()
                Text(
                    text = new,
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
                VerticalDivider()
                Text(
                    text = desc,
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 10.sp,
                    modifier = Modifier.weight(0.8f)
                )
            }
        }
    }
}

@Composable
private fun ReportsTab(
    state: WifiAnalyzerState,
    onGenerateReport: () -> Unit,
    onExportReport: (ExportFormat) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        item { ReportGeneratorCard(state.isGeneratingReport, onGenerateReport) }
        item { ImpactLevelChart(state.p0Count, state.p1Count, state.p2Count) }
        item { AffectedFeaturesCard(state.reports) }
        item { ExportActionsCard(state.isExporting, onExportReport) }
        item { RegressionTestCard() }
    }
}

@Composable
private fun ReportGeneratorCard(isGenerating: Boolean, onGenerate: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    text = "影响分级报告 / Impact Report",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "生成详细的 API 影响分析报告",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
            Spacer(Modifier.width(16.dp))
            if (isGenerating) {
                CircularProgressIndicator(Modifier.size(32.dp))
            } else {
                TextButton(onClick = onGenerate) {
                    Icon(Icons.Default.BugReport, null)
                    Spacer(Modifier.width(4.dp))
                    Text("生成报告")
                }
            }
        }
    }
}

@Composable
private fun ImpactLevelChart(p0: Int, p1: Int, p2: Int) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(
                text = "影响级别分布 / Impact Distribution",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(16.dp))
            val max = maxOf(p0, p1, p2, 1)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom
            ) {
                BarItem("P0", p0, max, Severity.P0.color, "严重")
                BarItem("P1", p1, max, Severity.P1.color, "警告")
                BarItem("P2", p2, max, Severity.P2.color, "提示")
            }
        }
    }
}

@Composable
private fun BarItem(label: String, count: Int, max: Int, color: Color, desc: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        val h by animateFloatAsState(
            if (max > 0) (count.toFloat() / max) * 120f else 0f,
            label = "bar"
        )
        Box(
            Modifier.width(48.dp).height(120.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            Box(
                Modifier.width(48.dp).height(h.dp)
                    .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                    .background(color)
            )
        }
        Spacer(Modifier.height(4.dp))
        Text(
            text = "$count",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(text = label, style = MaterialTheme.typography.labelSmall)
        Text(
            text = desc,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
        )
    }
}

@Composable
private fun AffectedFeaturesCard(reports: List<ImpactReport>) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(
                text = "受影响功能 / Affected Features",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(8.dp))
            if (reports.isEmpty()) {
                Text(
                    text = "暂无报告 / No reports generated",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            } else {
                reports.first().affectedFeatures.forEach { feature ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Warning, null,
                            Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = feature,
                            style = MaterialTheme.typography.bodySmall,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ExportActionsCard(isExporting: Boolean, onExport: (ExportFormat) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(
                text = "导出报告 / Export Report",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ExportFormat.entries.forEach { format ->
                    if (isExporting) {
                        CircularProgressIndicator(Modifier.size(32.dp))
                    } else {
                        TextButton(
                            onClick = { onExport(format) },
                            modifier = Modifier.weight(1f)
                        ) {
                            val icon: ImageVector = when (format) {
                                ExportFormat.PDF -> Icons.Default.Description
                                ExportFormat.JSON -> Icons.Default.Info
                                ExportFormat.MARKDOWN -> Icons.Default.ArrowForward
                            }
                            Icon(icon, null, Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text(format.label)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RegressionTestCard() {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f))
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.BugReport, null,
                    tint = MaterialTheme.colorScheme.tertiary
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Android 17 Wi-Fi 回归测试 / Regression Tests",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(Modifier.height(8.dp))
            Text(
                text = "生成 JUnit 4 测试用例模板，包含 @RequiresApi(17) 注解和 assertThrows 断言。",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
            Spacer(Modifier.height(8.dp))
            Box(
                modifier = Modifier.fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.Black.copy(alpha = 0.1f))
                    .padding(12.dp)
            ) {
                Text(
                    text = "@RunWith(AndroidJUnit4::class)\n@RequiresApi(17)\nclass WifiRttRegressionTest { ... }",
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp
                )
            }
            Spacer(Modifier.height(8.dp))
            TextButton(onClick = { }) {
                Icon(Icons.Default.Info, null, Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("生成测试用例 / Generate Tests")
            }
        }
    }
}

@Composable
private fun SettingsTab(
    state: WifiAnalyzerState,
    onUpdateScanConfig: (ScanConfig) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        item { ScanConfigCard(state.scanConfig, onUpdateScanConfig) }
        item { NotificationPrefsCard() }
        item { AboutCard() }
    }
}

@Composable
private fun ScanConfigCard(config: ScanConfig, onUpdateConfig: (ScanConfig) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(
                text = "扫描配置 / Scan Config",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = "扫描范围 / Scan Scope",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            Spacer(Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ScanScope.entries.forEach { scope ->
                    FilterChip(
                        selected = config.scanScope == scope,
                        onClick = { onUpdateConfig(config.copy(scanScope = scope)) },
                        label = { Text(scope.label, fontSize = 11.sp) }
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
            Text(
                text = "模拟目标 API 版本 / Target API Level",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            Spacer(Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(16, 17, 18).forEach { level ->
                    FilterChip(
                        selected = config.targetApiLevel == level,
                        onClick = { onUpdateConfig(config.copy(targetApiLevel = level)) },
                        label = { Text("API $level") }
                    )
                }
            }
        }
    }
}

@Composable
private fun NotificationPrefsCard() {
    var enabled by remember { mutableStateOf(true) }
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    text = "Issue 预警通知 / Issue Alerts",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "当检测到新的 Android Wi-Fi API 问题时通知",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
            Switch(checked = enabled, onCheckedChange = { enabled = it })
        }
    }
}

@Composable
private fun AboutCard() {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Info, null, tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "关于 / About",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(Modifier.height(12.dp))
            Text(
                text = "Wi-Fi Analyzer Tool v1.0.0",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "基于 Android 17 Beta 4 Wi-Fi API 变更设计",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            Spacer(Modifier.height(8.dp))
            HorizontalDivider()
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Wi-Fi API 变更日志 / Wi-Fi API Changelog",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(4.dp))
            val changelog = listOf(
                "API 33" to "ACCESS_FINE_LOCATION 权限强制要求",
                "API 34" to "startScan() 返回空列表限制",
                "API 35" to "Wi-Fi RTT 精度提升",
                "API 36" to "Neighbor Reporting 正式支持"
            )
            changelog.forEach { (api, desc) ->
                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
                    Text(
                        text = "Android $api:",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.width(80.dp)
                    )
                    Text(
                        text = desc,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}
