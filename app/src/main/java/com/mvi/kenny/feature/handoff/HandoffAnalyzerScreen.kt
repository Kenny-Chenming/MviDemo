package com.mvi.kenny.feature.handoff

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mvi.kenny.base.TopBarConfig
import kotlinx.coroutines.flow.collectLatest

/**
 * ============================================================
 * HandoffAnalyzerScreen — 适用性分析器页面
 * ============================================================
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HandoffAnalyzerScreen(
    viewModel: HandoffAnalyzerViewModel = viewModel(),
    onUpdateTopBar: (TopBarConfig) -> Unit = {}
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(Unit) { onUpdateTopBar(TopBarConfig(title = "适用性分析器")) }
    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is HandoffAnalyzerEffect.ShowToast -> Toast.makeText(context, effect.message, Toast.LENGTH_SHORT).show()
                is HandoffAnalyzerEffect.ScanCompleted -> Toast.makeText(context, "扫描完成：${effect.totalCount} 个 Activity，${effect.handoffableCount} 个可 Handoff", Toast.LENGTH_LONG).show()
            }
        }
    }

    Scaffold(topBar = { TopAppBar(title = { Text("适用性分析器") }, navigationIcon = { if (state.selectedActivity != null) IconButton(onClick = { viewModel.sendIntent(HandoffAnalyzerIntent.ClearSelection) }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "返回") } }) }) { innerPadding ->
        if (state.selectedActivity != null) {
            ActivityDetailContent(state.selectedActivity!!, Modifier.padding(innerPadding))
        } else {
            Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
                ScanInputSection(state.inputPath, state.scanStatus, { viewModel.sendIntent(HandoffAnalyzerIntent.UpdateInputPath(it)) }, { viewModel.sendIntent(HandoffAnalyzerIntent.StartScan) }, { viewModel.sendIntent(HandoffAnalyzerIntent.CancelScan) })
                if (state.scanStatus == ScanStatus.DONE) {
                    ScanResultsSummary(state.analyzedActivities) { viewModel.sendIntent(HandoffAnalyzerIntent.SelectActivity(it)) }
                }
            }
        }
    }
}

@Composable
private fun ScanInputSection(inputPath: String, scanStatus: ScanStatus, onPathChange: (String) -> Unit, onStartScan: () -> Unit, onCancelScan: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().padding(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant), shape = RoundedCornerShape(12.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("🔍 APK / 源码路径", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(value = inputPath, onValueChange = onPathChange, modifier = Modifier.fillMaxWidth(), placeholder = { Text("输入 APK 路径或源码目录") }, enabled = scanStatus != ScanStatus.SCANNING, singleLine = true)
            Spacer(modifier = Modifier.height(12.dp))
            if (scanStatus == ScanStatus.SCANNING) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Row(verticalAlignment = Alignment.CenterVertically) { CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp); Spacer(modifier = Modifier.width(8.dp)); Text("扫描中...", style = MaterialTheme.typography.bodyMedium) }
                    OutlinedButton(onClick = onCancelScan) { Icon(Icons.Default.Close, null, modifier = Modifier.size(16.dp)); Spacer(modifier = Modifier.width(4.dp)); Text("取消") }
                }
            } else {
                Button(onClick = onStartScan, modifier = Modifier.fillMaxWidth(), enabled = inputPath.isNotBlank()) { Icon(Icons.Default.PlayArrow, null); Spacer(modifier = Modifier.width(8.dp)); Text("开始分析") }
            }
        }
    }
}

@Composable
private fun ScanResultsSummary(activities: List<ActivityHandoffScore>, onActivityClick: (ActivityHandoffScore) -> Unit) {
    val handoffable = activities.count { it.isHandoffable }
    val avgScore = if (activities.isNotEmpty()) activities.map { it.score }.average().toInt() else 0
    LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        item {
            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer), shape = RoundedCornerShape(12.dp)) {
                Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
                    StatItem("Activity 总数", "${activities.size}")
                    StatItem("可 Handoff", "$handoffable")
                    StatItem("平均评分", "$avgScore")
                }
            }
        }
        item { Text("Activity 列表", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)) }
        items(activities.sortedByDescending { it.score }) { activity -> ActivityScoreCard(activity, onActivityClick) }
    }
}

@Composable
private fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f))
    }
}

@Composable
private fun ActivityScoreCard(activity: ActivityHandoffScore, onClick: () -> Unit) {
    val scoreColor = when { activity.score >= 80 -> Color(0xFF4CAF50); activity.score >= 50 -> Color(0xFFFF9800); else -> Color(0xFFB3261E) }
    Card(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)), shape = RoundedCornerShape(8.dp)) {
        Row(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(48.dp).clip(RoundedCornerShape(24.dp)).background(scoreColor.copy(alpha = 0.15f)), contentAlignment = Alignment.Center) {
                Text("${activity.score}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = scoreColor)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(activity.simpleName, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(activity.activityName, style = MaterialTheme.typography.labelSmall, fontFamily = FontFamily.Monospace, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Spacer(modifier = Modifier.height(4.dp))
                Text(activity.recommendation, style = MaterialTheme.typography.labelSmall, color = scoreColor)
            }
            Icon(if (activity.isHandoffable) Icons.Default.CheckCircle else Icons.Default.Warning, null, tint = scoreColor, modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
private fun ActivityDetailContent(activity: ActivityHandoffScore, modifier: Modifier = Modifier) {
    val scoreColor = when { activity.score >= 80 -> Color(0xFF4CAF50); activity.score >= 50 -> Color(0xFFFF9800); else -> Color(0xFFB3261E) }
    Column(modifier = modifier.fillMaxSize().padding(16.dp)) {
        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = scoreColor.copy(alpha = 0.1f)), shape = RoundedCornerShape(16.dp)) {
            Column(modifier = Modifier.fillMaxWidth().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("${activity.score}", style = MaterialTheme.typography.displayLarge, fontWeight = FontWeight.Bold, color = scoreColor)
                Text("/ 100", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(8.dp))
                Text(activity.recommendation, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = scoreColor)
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant), shape = RoundedCornerShape(12.dp)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Activity 信息", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                DetailRow("类名", activity.activityName)
                DetailRow("简单名", activity.simpleName)
                DetailRow("可 Handoff", if (activity.isHandoffable) "是 ✅" else "否 ❌")
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant), shape = RoundedCornerShape(12.dp)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("评分依据", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                activity.reasons.forEach { reason -> Row(modifier = Modifier.padding(vertical = 2.dp)) { Text("• ", color = MaterialTheme.colorScheme.primary); Text(reason, style = MaterialTheme.typography.bodySmall) } }
            }
        }
        if (activity.dependencies.isNotEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))
            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant), shape = RoundedCornerShape(12.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("依赖的 Activity", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    activity.dependencies.forEach { dep -> Row(modifier = Modifier.padding(vertical = 2.dp)) { Text("→ ", color = MaterialTheme.colorScheme.secondary); Text(dep, style = MaterialTheme.typography.bodySmall, fontFamily = FontFamily.Monospace) } }
                }
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium, fontFamily = if (value.contains(".")) FontFamily.Monospace else FontFamily.Default)
    }
}
