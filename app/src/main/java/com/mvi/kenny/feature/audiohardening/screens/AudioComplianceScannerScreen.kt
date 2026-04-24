package com.mvi.kenny.feature.audiohardening.screens

// ================================================================
// AudioComplianceScannerScreen — 自动化检测器（工具①）
// ================================================================
// Gradle 插件 + CLI 自动化检测 Background Audio Hardening 合规问题。
//
// PRD-145 工具①
// ================================================================

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mvi.kenny.feature.audiohardening.*
import com.mvi.kenny.feature.audiohardening.AudioComplianceScannerIntent
import com.mvi.kenny.feature.audiohardening.AudioComplianceScannerViewModel

private val ColorCodeBackground = Color(0xFF1E1E1E)

@Composable
fun AudioComplianceScannerScreen(
    onBack: () -> Unit = {},
    viewModel: AudioComplianceScannerViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is AudioComplianceScannerEffect.ShowSnackbar -> { }
                is AudioComplianceScannerEffect.ShareDiff -> { }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // 标题
        Text(
            text = "① 自动化检测器",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Gradle 插件 + CLI 自动化检测 Background Audio Hardening 合规问题",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 扫描范围选择
        Text("扫描范围", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Medium)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(
                ScanScope.All to "全部模块",
                ScanScope.AffectedOnly to "仅受影响"
            ).forEach { (scope, label) ->
                FilterChip(
                    selected = state.scanScope == scope,
                    onClick = { viewModel.sendIntent(AudioComplianceScannerIntent.SetScanScope(scope)) },
                    label = { Text(label) }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 风险等级过滤
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(null to "全部", RiskLevel.P0 to "P0", RiskLevel.P1 to "P1", RiskLevel.P2 to "P2").forEach { (level, label) ->
                FilterChip(
                    selected = state.selectedFilter == level,
                    onClick = { viewModel.sendIntent(AudioComplianceScannerIntent.SetRiskFilter(level)) },
                    label = { Text(label) }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 扫描按钮 + 进度
        if (state.isScanning) {
            LinearProgressIndicator(
                progress = { state.progress },
                modifier = Modifier.fillMaxWidth().height(4.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        Button(
            onClick = {
                if (state.isScanning) viewModel.sendIntent(AudioComplianceScannerIntent.CancelScan)
                else viewModel.sendIntent(AudioComplianceScannerIntent.StartScan)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Radar, null)
            Spacer(Modifier.width(8.dp))
            Text(if (state.isScanning) "取消扫描" else "开始扫描")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 扫描结果
        val filteredResults = if (state.selectedFilter != null)
            state.scanResults.filter { it.riskLevel == state.selectedFilter }
        else state.scanResults

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(filteredResults, key = { it.id }) { result ->
                ScanResultItem(
                    result = result,
                    isExpanded = state.expandedResultId == result.id,
                    onToggle = { viewModel.sendIntent(AudioComplianceScannerIntent.ToggleResultExpand(result.id)) }
                )
            }
        }

        // 生成修复 Diff
        if (state.scanResults.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = { viewModel.sendIntent(AudioComplianceScannerIntent.GenerateFixDiff) },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
            ) {
                Icon(Icons.Default.Difference, null)
                Spacer(Modifier.width(8.dp))
                Text("生成修复 Diff")
            }
        }
    }
}

@Composable
private fun ScanResultItem(
    result: ScanResult,
    isExpanded: Boolean,
    onToggle: () -> Unit
) {
    val riskColor = when (result.riskLevel) {
        RiskLevel.P0 -> Color(0xFFF44336)
        RiskLevel.P1 -> Color(0xFFFFC107)
        RiskLevel.P2 -> Color(0xFF4CAF50)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle() },
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("${result.riskLevel.emoji}", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "${result.filePath}:${result.lineNumber}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        result.riskLevel.displayName,
                        style = MaterialTheme.typography.bodySmall,
                        color = riskColor
                    )
                }
                Icon(
                    if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null
                )
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        result.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "修复建议: ${result.suggestedFix}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.height(8.dp))
                    CodeBlock(code = result.codeSnippet)
                }
            }
        }
    }
}

@Composable
private fun CodeBlock(code: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = ColorCodeBackground
    ) {
        Text(
            text = code,
            fontFamily = FontFamily.Monospace,
            fontSize = MaterialTheme.typography.bodySmall.fontSize,
            color = Color(0xFFD4D4D4),
            modifier = Modifier.padding(12.dp)
        )
    }
}
