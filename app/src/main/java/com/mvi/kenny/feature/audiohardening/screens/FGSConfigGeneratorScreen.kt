package com.mvi.kenny.feature.audiohardening.screens

// ================================================================
// FGSConfigGeneratorScreen — FGS 配置生成器（工具③）
// ================================================================
// 生成 while-in-use Foreground Service 配置。
//
// PRD-145 工具③
// ================================================================

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.mvi.kenny.feature.audiohardening.FGSConfigGeneratorIntent
import com.mvi.kenny.feature.audiohardening.FGSConfigGeneratorViewModel
import com.mvi.kenny.feature.audiohardening.FGSUseCase

private val ColorCodeBackground = Color(0xFF1E1E1E)

@Composable
fun FGSConfigGeneratorScreen(
    onBack: () -> Unit = {},
    viewModel: FGSConfigGeneratorViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "③ FGS 配置生成器",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "生成 while-in-use Foreground Service 配置",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 场景选择
        Text("选择音频场景", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Medium)
        Spacer(Modifier.height(8.dp))

        FGSUseCase.entries.forEach { useCase ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        if (state.selectedUseCase == useCase)
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                        else Color.Transparent
                    )
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = state.selectedUseCase == useCase,
                    onClick = { viewModel.sendIntent(FGSConfigGeneratorIntent.SelectUseCase(useCase)) }
                )
                Spacer(Modifier.width(8.dp))
                Column {
                    Text(useCase.titleCn, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                    Text(useCase.titleEn, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Manifest 预览
        Text("AndroidManifest.xml 配置", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Medium)
        Spacer(Modifier.height(8.dp))
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            color = ColorCodeBackground
        ) {
            Text(
                text = state.generatedManifest.ifEmpty { "选择场景后生成配置…" },
                fontFamily = FontFamily.Monospace,
                fontSize = MaterialTheme.typography.bodySmall.fontSize,
                color = Color(0xFFD4D4D4),
                modifier = Modifier.padding(12.dp)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 权限列表
        Text("所需权限", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Medium)
        Spacer(Modifier.height(4.dp))
        state.generatedPermissions.forEach { permission ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.CheckCircle, null, tint = Color(0xFF4CAF50), modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(6.dp))
                Text(permission, style = MaterialTheme.typography.bodySmall, fontFamily = FontFamily.Monospace)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Capability 配置
        Text("while-in-use Capability", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Medium)
        Spacer(Modifier.height(4.dp))
        state.generatedCapabilities.forEach { cap ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Link, null, tint = Color(0xFF2196F3), modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(6.dp))
                Text(cap, style = MaterialTheme.typography.bodySmall, fontFamily = FontFamily.Monospace)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 最佳实践提示
        Text("最佳实践提示", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Medium)
        Spacer(Modifier.height(4.dp))
        state.bestPracticeTips.forEachIndexed { index, tip ->
            Row(modifier = Modifier.padding(vertical = 2.dp)) {
                Text("${index + 1}. ", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                Text(tip, style = MaterialTheme.typography.bodySmall)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 操作按钮
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = { viewModel.sendIntent(FGSConfigGeneratorIntent.CopyManifest) },
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.ContentCopy, null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("复制配置")
            }
            Button(
                onClick = { viewModel.sendIntent(FGSConfigGeneratorIntent.ApplyToProject) },
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.Download, null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("应用到项目")
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}
