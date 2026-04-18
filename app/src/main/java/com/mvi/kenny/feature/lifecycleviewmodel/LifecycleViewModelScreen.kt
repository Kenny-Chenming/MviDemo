package com.mvi.kenny.feature.lifecycleviewmodel

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Badge
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mvi.kenny.base.TopBarConfig
import com.mvi.kenny.feature.lifecycleviewmodel.component.ApiCompareTable
import com.mvi.kenny.feature.lifecycleviewmodel.component.CodeDiffPreview
import com.mvi.kenny.feature.lifecycleviewmodel.component.CompatibilitySDKPreview
import com.mvi.kenny.feature.lifecycleviewmodel.component.DecisionTreeView
import com.mvi.kenny.feature.lifecycleviewmodel.component.MigrationProgressRing
import com.mvi.kenny.feature.lifecycleviewmodel.component.PerformanceChart
import com.mvi.kenny.feature.lifecycleviewmodel.component.QuickAccessCardDefaults
import com.mvi.kenny.feature.lifecycleviewmodel.component.QuickAccessType
import com.mvi.kenny.feature.lifecycleviewmodel.component.RadarChart
import com.mvi.kenny.feature.lifecycleviewmodel.component.ScanResultList
import com.mvi.kenny.feature.lifecycleviewmodel.component.SceneRecommendCard
import com.mvi.kenny.feature.lifecycleviewmodel.component.TestTemplateCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LifecycleViewModelScreen(
    onUpdateTopBar: (TopBarConfig) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: LifecycleViewModelViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(state.activeTab) {
        val config = TopBarConfig(
            title = when (state.activeTab) {
                LifecycleDslTab.Dashboard -> "Lifecycle ViewModel DSL"
                LifecycleDslTab.Scanner -> "迁移扫描器"
                LifecycleDslTab.BestPractice -> "最佳实践"
                LifecycleDslTab.Compatibility -> "兼容层 & 性能"
                LifecycleDslTab.Migration -> "迁移 & 测试"
            },
            actions = listOf(TopBarConfig.TopBarAction(icon = androidx.compose.material.icons.Icons.Default.Refresh, contentDescription = "刷新", onClick = { viewModel.sendIntent(LifecycleDslIntent.RefreshDashboard) }))
        )
        onUpdateTopBar(config)
    }

    LaunchedEffect(Unit) { viewModel.effect.collect { _ -> } }

    Column(modifier = modifier.fillMaxSize()) {
        ScrollableTabRow(selectedTabIndex = state.activeTab.ordinal, edgePadding = 16.dp, containerColor = MaterialTheme.colorScheme.surface) {
            LifecycleDslTab.entries.forEach { tab ->
                Tab(selected = state.activeTab == tab, onClick = { viewModel.sendIntent(LifecycleDslIntent.SwitchTab(tab)) }, text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = when (tab) {
                            LifecycleDslTab.Dashboard -> "Dashboard"
                            LifecycleDslTab.Scanner -> "Scanner"
                            LifecycleDslTab.BestPractice -> "BestPractice"
                            LifecycleDslTab.Compatibility -> "Compatibility"
                            LifecycleDslTab.Migration -> "Migration"
                        })
                        if (tab == LifecycleDslTab.Scanner && state.scanResults.isNotEmpty()) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Badge { Text("${state.scanResults.size}") }
                        }
                    }
                })
            }
        }
        Box(modifier = Modifier.fillMaxSize()) {
            when (state.activeTab) {
                LifecycleDslTab.Dashboard -> DashboardTabContent(state, viewModel::sendIntent)
                LifecycleDslTab.Scanner -> ScannerTabContent(state, viewModel::sendIntent)
                LifecycleDslTab.BestPractice -> BestPracticeTabContent(state, viewModel::sendIntent)
                LifecycleDslTab.Compatibility -> CompatibilityTabContent(state, viewModel::sendIntent)
                LifecycleDslTab.Migration -> MigrationTabContent(state, viewModel::sendIntent)
            }
        }
    }
}

@Composable
private fun ScannerTabContent(state: LifecycleDslState, onIntent: (LifecycleDslIntent) -> Unit) {
    LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item { ScannerConfigCard(state, onIntent) }
        if (state.isScanning) { item { ScanningProgressCard(state.scanProgress) } }
        item { SeverityFilterRow(state, onIntent) }
        if (state.scanResults.isNotEmpty()) {
            item { Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) { Text(text = "扫描结果（${state.filteredScanResults.size}）", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold); if (state.scanResults.any { it.isSelected }) { Button(onClick = { onIntent(LifecycleDslIntent.GeneratePatch(state.scanResults.filter { it.isSelected })) }) { Text("生成补丁") } } } }
        }
        item { ScanResultList(results = state.filteredScanResults, selectedResult = state.selectedResult, onItemClick = { item -> onIntent(LifecycleDslIntent.SelectResult(if (state.selectedResult?.id == item.id) null else item)) }, onToggleSelection = { id -> onIntent(LifecycleDslIntent.ToggleResultSelection(id)) }) }
        if (state.showDiffPreview) { item { CodeDiffPreview(items = state.scanResults.filter { it.isSelected }, onConfirm = { onIntent(LifecycleDslIntent.ConfirmPatch) }, onCancel = { onIntent(LifecycleDslIntent.CancelPatch) }) } }
    }
}

@Composable
private fun ScannerConfigCard(state: LifecycleDslState, onIntent: (LifecycleDslIntent) -> Unit) {
    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)), shape = RoundedCornerShape(16.dp)) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Text(text = "扫描配置", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = "扫描范围：", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("整个项目", "app 模块", "feature 模块").forEach { scope -> FilterChip(selected = state.scanScope == scope, onClick = { onIntent(LifecycleDslIntent.SetScanScope(scope)) }, label = { Text(scope) }) }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth().clickable { onIntent(LifecycleDslIntent.SetIncludeTestDir(!state.includeTestDir)) }, verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = state.includeTestDir, onCheckedChange = { onIntent(LifecycleDslIntent.SetIncludeTestDir(it)) })
                Text(text = "包含 test 目录", style = MaterialTheme.typography.bodyMedium)
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = { onIntent(LifecycleDslIntent.StartScan) }, enabled = !state.isScanning, modifier = Modifier.fillMaxWidth()) { Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp)); Spacer(modifier = Modifier.width(8.dp)); Text(if (state.isScanning) "扫描中..." else "开始扫描") }
        }
    }
}

@Composable
private fun ScanningProgressCard(progress: Float) {
    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)), shape = RoundedCornerShape(12.dp)) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text(text = "正在扫描...", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium); Text(text = "${(progress * 100).toInt()}%", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary) }
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)))
        }
    }
}

@Composable
private fun SeverityFilterRow(state: LifecycleDslState, onIntent: (LifecycleDslIntent) -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
        FilterChip(selected = state.severityFilter == null, onClick = { onIntent(LifecycleDslIntent.FilterBySeverity(null)) }, label = { Text("全部 (${state.filteredScanResults.size})") })
        FilterChip(selected = state.severityFilter == Severity.P0, onClick = { onIntent(LifecycleDslIntent.FilterBySeverity(if (state.severityFilter == Severity.P0) null else Severity.P0)) }, label = { Text("P0 (${state.filteredScanResults.count { it.severity == Severity.P0 }})") })
        FilterChip(selected = state.severityFilter == Severity.P1, onClick = { onIntent(LifecycleDslIntent.FilterBySeverity(if (state.severityFilter == Severity.P1) null else Severity.P1)) }, label = { Text("P1 (${state.filteredScanResults.count { it.severity == Severity.P1 }})") })
    }
}

@Composable
private fun BestPracticeTabContent(state: LifecycleDslState, onIntent: (LifecycleDslIntent) -> Unit) {
    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(24.dp)) {
        Text(text = "场景化决策树", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        state.currentDecisionNode?.let { node -> DecisionTreeView(node = node, onSelectOption = { onIntent(LifecycleDslIntent.NavigateDecision(it)) }) }
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "新旧 API 对照", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        if (state.apiCompareItems.isNotEmpty()) { ApiCompareTable(items = state.apiCompareItems) }
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "场景推荐", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        if (state.sceneRecommendItems.isNotEmpty()) { SceneRecommendCard(items = state.sceneRecommendItems) }
    }
}

@Composable
private fun CompatibilityTabContent(state: LifecycleDslState, onIntent: (LifecycleDslIntent) -> Unit) {
    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(24.dp)) {
        Text(text = "兼容层 SDK 预览", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        if (state.compatibilitySDKItems.isNotEmpty()) { CompatibilitySDKPreview(items = state.compatibilitySDKItems) }
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "性能分析", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        if (state.performanceData.isNotEmpty()) { PerformanceChart(dataPoints = state.performanceData) }
    }
}

@Composable
private fun MigrationTabContent(state: LifecycleDslState, onIntent: (LifecycleDslIntent) -> Unit) {
    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(24.dp)) {
        Text(text = "迁移进度", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        MigrationProgressRing(progress = state.migrationProgress)
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "回归测试用例", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        TestTemplateCard(templates = state.testTemplates, onGenerateTests = { viewModelClass -> onIntent(LifecycleDslIntent.GenerateTests(viewModelClass)) })
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "viewModelScope 行为变更", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        ViewModelScopeCard(content = state.viewModelScopeChange)
    }
}

@Composable
private fun ViewModelScopeCard(content: String, modifier: Modifier = Modifier) {
    Card(modifier = modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)), shape = RoundedCornerShape(16.dp)) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) { Text(text = content, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface) }
    }
}

@Composable
private fun DashboardTabContent(state: LifecycleDslState, onIntent: (LifecycleDslIntent) -> Unit) {
    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(text = "项目健康度", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)), shape = RoundedCornerShape(16.dp)) { RadarChart(data = state.dashboardHealth, modifier = Modifier.padding(16.dp)) }
        Text(text = "快速操作", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            QuickAccessCardDefaults.quickAccessCard(type = QuickAccessType.StartScan, onClick = { onIntent(LifecycleDslIntent.SwitchTab(LifecycleDslTab.Scanner)) })
            QuickAccessCardDefaults.quickAccessCard(type = QuickAccessType.ViewGuide, onClick = { onIntent(LifecycleDslIntent.SwitchTab(LifecycleDslTab.BestPractice)) })
            QuickAccessCardDefaults.quickaccesscard(type = QuickAccessType.GenerateCompatibility, onClick = { onIntent(LifecycleDslIntent.SwitchTab(LifecycleDslTab.Compatibility)) })
            QuickAccessCardDefaults.quickAccessCard(type = QuickAccessType.ViewScopeGuide, onClick = { onIntent(LifecycleDslIntent.SwitchTab(LifecycleDslTab.Migration)) })
        }
        if (state.recentScans.isNotEmpty()) {
            Text(text = "最近扫描", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) { state.recentScans.take(3).forEach { scan -> RecentScanItem(record = scan) } }
        }
    }
}

@Composable
private fun RecentScanItem(record: RecentScanRecord, modifier: Modifier = Modifier) {
    Card(modifier = modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)), shape = RoundedCornerShape(8.dp)) {
        Row(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = Icons.Default.History, contentDescription = null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) { Text(text = record.moduleName, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium); Text(text = record.summary, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
            Text(text = "${record.resultCount} 个问题", style = MaterialTheme.typography.labelMedium, color = if (record.resultCount > 0) Color(0xFFF44336) else Color(0xFF4CAF50))
        }
    }
}
