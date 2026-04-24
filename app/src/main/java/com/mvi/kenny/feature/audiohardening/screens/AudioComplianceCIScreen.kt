package com.mvi.kenny.feature.audiohardening.screens

// ================================================================
// AudioComplianceCIScreen — 合规 CI 检测工具（工具⑤）
// ================================================================
// CI 流水线合规检测，输出 GitHub/GitLab Action。
//
// PRD-145 工具⑤
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
import com.mvi.kenny.feature.audiohardening.AudioComplianceCIIntent
import com.mvi.kenny.feature.audiohardening.AudioComplianceCIViewModel
import com.mvi.kenny.feature.audiohardening.CIComplianceItem
import com.mvi.kenny.feature.audiohardening.ToolStatus

@Composable
fun AudioComplianceCIScreen(
    onBack: () -> Unit = {},
    viewModel: AudioComplianceCIViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "⑤ 合规 CI 检测工具",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "CI 流水线合规检测，输出 GitHub/GitLab Action",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // targetSDK 选择
        Text("targetSDK 版本", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Medium)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            state.targetSdkOptions.forEach { version ->
                FilterChip(
                    selected = state.selectedTargetSdk == version,
                    onClick = { viewModel.sendIntent(AudioComplianceCIIntent.SelectTargetSdk(version)) },
                    label = { Text("API $version") }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(1f)
        ) {
            // 合规项
            if (state.complianceItems.isNotEmpty()) {
                item {
                    Text("✅ 合规项", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = Color(0xFF4CAF50))
                }
                items(state.complianceItems) { item ->
                    CIItemCard(item = item)
                }
            }

            // 风险项
            if (state.warningItems.isNotEmpty()) {
                item {
                    Spacer(Modifier.height(8.dp))
                    Text("⚠️ 风险项", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = Color(0xFFFFC107))
                }
                items(state.warningItems) { item ->
                    CIItemCard(item = item)
                }
            }

            // 不合规项
            if (state.violationItems.isNotEmpty()) {
                item {
                    Spacer(Modifier.height(8.dp))
                    Text("❌ 不合规项", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = Color(0xFFF44336))
                }
                items(state.violationItems) { item ->
                    CIItemCard(item = item)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 操作按钮
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = { viewModel.sendIntent(AudioComplianceCIIntent.GenerateGitHubAction) },
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.Code, null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("GitHub Action")
            }
            OutlinedButton(
                onClick = { viewModel.sendIntent(AudioComplianceCIIntent.GenerateGitLabCI) },
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.AccountTree, null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("GitLab CI")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { viewModel.sendIntent(AudioComplianceCIIntent.ExportReport("JSON")) },
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.Download, null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("导出 JSON")
            }
            OutlinedButton(
                onClick = { viewModel.sendIntent(AudioComplianceCIIntent.ExportReport("HTML")) },
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.Download, null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("导出 HTML")
            }
        }
    }
}

@Composable
private fun CIItemCard(item: CIComplianceItem) {
    val color = when (item.status) {
        ToolStatus.Pass -> Color(0xFF4CAF50)
        ToolStatus.Warning -> Color(0xFFFFC107)
        else -> Color(0xFFF44336)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                when (item.status) {
                    ToolStatus.Pass -> Icons.Default.CheckCircle
                    ToolStatus.Warning -> Icons.Default.Warning
                    else -> Icons.Default.Error
                },
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(item.title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                Text(item.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
            }
        }
    }
}
