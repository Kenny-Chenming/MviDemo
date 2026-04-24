package com.mvi.kenny.feature.audiohardening.screens

// ================================================================
// AudioRegressionTestScreen — 回归测试框架（工具⑦）
// ================================================================
// Android 16→17 音频行为回归测试框架。
//
// PRD-145 工具⑦
// ================================================================

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mvi.kenny.feature.audiohardening.AudioRegressionTestIntent
import com.mvi.kenny.feature.audiohardening.AudioRegressionTestViewModel
import com.mvi.kenny.feature.audiohardening.RegressionScenario
import com.mvi.kenny.feature.audiohardening.TestResult

@Composable
fun AudioRegressionTestScreen(
    onBack: () -> Unit = {},
    viewModel: AudioRegressionTestViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "⑦ 回归测试框架",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Android 16→17 音频行为回归测试框架",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 目标版本
        Text("目标 Android 版本", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Medium)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(16, 17).forEach { version ->
                FilterChip(
                    selected = state.selectedVersions.contains(version),
                    onClick = { viewModel.sendIntent(AudioRegressionTestIntent.ToggleVersion(version)) },
                    label = { Text("Android $version") }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 测试场景
        Text("测试场景", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Medium)
        Column {
            RegressionScenario.entries.forEach { scenario ->
                Row(
                    modifier = Modifier.padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = state.selectedScenarios.contains(scenario),
                        onCheckedChange = { viewModel.sendIntent(AudioRegressionTestIntent.ToggleScenario(scenario)) }
                    )
                    Spacer(Modifier.width(8.dp))
                    Column {
                        Text(scenario.titleCn, style = MaterialTheme.typography.bodyMedium)
                        Text(scenario.titleEn, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 执行按钮
        Button(
            onClick = { viewModel.sendIntent(AudioRegressionTestIntent.RunTests) },
            modifier = Modifier.fillMaxWidth(),
            enabled = !state.isRunning && state.selectedScenarios.isNotEmpty() && state.selectedVersions.isNotEmpty()
        ) {
            Icon(if (state.isRunning) Icons.Default.Pending else Icons.Default.Science, null, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(8.dp))
            Text(if (state.isRunning) "测试运行中…" else "执行测试")
        }

        if (state.isRunning) {
            Spacer(Modifier.height(8.dp))
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 测试结果
        if (state.testResults.isNotEmpty()) {
            Text("测试结果", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(8.dp))
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(state.testResults) { result ->
                    TestResultCard(result = result)
                }
            }
        } else {
            // CI 集成说明
            Text("CI 集成说明", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(8.dp))
            TextButton(onClick = { viewModel.sendIntent(AudioRegressionTestIntent.ViewCIIntegration) }) {
                Icon(Icons.Default.Info, null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("查看 CI 集成方法")
            }
            if (state.ciIntegrationHint.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Text(
                        state.ciIntegrationHint,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun TestResultCard(result: TestResult) {
    val color = if (result.passed) Color(0xFF4CAF50) else Color(0xFFF44336)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                if (result.passed) Icons.Default.CheckCircle else Icons.Default.Cancel,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Android ${result.version} | ${result.scenario.titleCn}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    if (result.passed) "✅ Pass" else "❌ Fail",
                    style = MaterialTheme.typography.bodySmall,
                    color = color
                )
            }
        }
    }
}
