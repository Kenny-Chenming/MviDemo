package com.mvi.kenny.feature.remotetoolkit.sub_screens

import com.mvi.kenny.feature.remotetoolkit.RemoteToolkitState
import com.mvi.kenny.feature.remotetoolkit.RemoteToolkitIntent
import com.mvi.kenny.feature.remotetoolkit.RemoteToolkitEffect
import com.mvi.kenny.feature.remotetoolkit.RemoteToolkitColors


import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

// =============================================================
// AIPipelineScreen — AI → Compose Remote 管道页面
// AI Pipeline / AI 管道
// =============================================================

@Composable
fun AIPipelineScreen(
    state: RemoteToolkitState,
    onIntent: (RemoteToolkitIntent) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(RemoteToolkitColors.Background)
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "AI → Compose Remote Pipeline",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = RemoteToolkitColors.OnSurface
            )
        }

        // ============================================================
        // Pipeline Overview / 管道概览
        // ============================================================
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = RemoteToolkitColors.Surface),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Pipeline Overview / 管道概览",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium,
                        color = RemoteToolkitColors.OnSurface
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    PipelineFlowDiagram()
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Natural Language → LLM → JSON/DSL → Compose Remote Bytes → Native Render",
                        style = MaterialTheme.typography.labelSmall,
                        color = RemoteToolkitColors.OnSurfaceVariant,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }
            }
        }

        // ============================================================
        // Input Panel / 输入面板
        // ============================================================
        item {
            Text(
                text = "Input / 输入",
                style = MaterialTheme.typography.titleMedium,
                color = RemoteToolkitColors.OnSurface
            )
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = RemoteToolkitColors.Surface),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Describe the UI you want / 描述你想要的 UI",
                        style = MaterialTheme.typography.labelMedium,
                        color = RemoteToolkitColors.OnSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = state.aiPrompt,
                        onValueChange = { onIntent(RemoteToolkitIntent.UpdateAIPrompt(it)) },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = {
                            Text(
                                "e.g., A product card with an image, product name, price, and an \"Add to Cart\" button...",
                                color = RemoteToolkitColors.OnSurfaceVariant
                            )
                        },
                        minLines = 4,
                        maxLines = 6,
                        colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = RemoteToolkitColors.Purple,
                            unfocusedBorderColor = RemoteToolkitColors.Outline,
                            cursorColor = RemoteToolkitColors.Purple
                        )
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = { onIntent(RemoteToolkitIntent.GenerateUI) },
                        enabled = state.aiPrompt.isNotBlank() && !state.isGenerating,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = RemoteToolkitColors.Purple)
                    ) {
                        if (state.isGenerating) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                                color = RemoteToolkitColors.OnPurple
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Generating...")
                        } else {
                            Text("Generate UI")
                        }
                    }
                }
            }
        }

        // ============================================================
        // Output Preview / 输出预览
        // ============================================================
        if (state.generatedCode != null || state.isGenerating) {
            item {
                Text(
                    text = "Output / 输出",
                    style = MaterialTheme.typography.titleMedium,
                    color = RemoteToolkitColors.OnSurface
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = RemoteToolkitColors.Surface),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Generated Code Preview / 生成代码预览",
                                style = MaterialTheme.typography.titleSmall,
                                color = RemoteToolkitColors.OnSurface
                            )
                            if (!state.isGenerating && state.generatedCode != null) {
                                Row {
                                    IconButton(onClick = { onIntent(RemoteToolkitIntent.CopyGeneratedCode) }) {
                                        Icon(
                                            Icons.Default.ContentCopy,
                                            contentDescription = "Copy",
                                            tint = RemoteToolkitColors.Purple,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    IconButton(onClick = { onIntent(RemoteToolkitIntent.GenerateUI) }) {
                                        Icon(
                                            Icons.Default.Refresh,
                                            contentDescription = "Regenerate",
                                            tint = RemoteToolkitColors.Info,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        if (state.isGenerating) {
                            // Shimmer placeholder / 骨架屏占位
                            ShimmerCodeBlock()
                        } else {
                            state.generatedCode?.let { code ->
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFF0D1117))
                                        .border(1.dp, RemoteToolkitColors.Outline, RoundedCornerShape(8.dp))
                                        .padding(16.dp)
                                ) {
                                    Text(
                                        text = code,
                                        style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                                        color = Color(0xFFE1E4E8)
                                    )
                                }
                            }
                        }

                        if (!state.isGenerating && state.generatedCode != null) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = { onIntent(RemoteToolkitIntent.DeployGeneratedUI) },
                                modifier = Modifier.fillMaxWidth(),
                                enabled = !state.isDeploying,
                                colors = ButtonDefaults.buttonColors(containerColor = RemoteToolkitColors.Teal)
                            ) {
                                if (state.isDeploying) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(18.dp),
                                        strokeWidth = 2.dp,
                                        color = RemoteToolkitColors.OnTeal
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Deploying...")
                                } else {
                                    Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Deploy to Device")
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedButton(
                                onClick = { },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Iterate / 迭代", color = RemoteToolkitColors.Info)
                            }
                        }
                    }
                }
            }
        }

        // ============================================================
        // Settings / 设置
        // ============================================================
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = { }) {
                    Text("Settings: Model", color = RemoteToolkitColors.OnSurfaceVariant)
                }
                TextButton(onClick = { }) {
                    Text("View Prompt Templates", color = RemoteToolkitColors.OnSurfaceVariant)
                }
            }
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}

// =============================================================
// PipelineFlowDiagram — 管道流程图
// =============================================================
@Composable
private fun PipelineFlowDiagram() {
    val steps = listOf(
        "Natural\nLanguage" to RemoteToolkitColors.Info,
        "LLM" to RemoteToolkitColors.Purple,
        "JSON/DSL" to RemoteToolkitColors.Teal,
        "Binary" to RemoteToolkitColors.Warning,
        "Native\nRender" to RemoteToolkitColors.PassColor
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        steps.forEachIndexed { index, (label, color) ->
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(color.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(color.copy(alpha = 0.4f))
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = RemoteToolkitColors.OnSurface,
                    modifier = Modifier.width(48.dp),
                    maxLines = 2
                )
            }
            if (index < steps.size - 1) {
                Text("→", color = RemoteToolkitColors.OnSurfaceVariant, modifier = Modifier.padding(horizontal = 4.dp))
            }
        }
    }
}

// =============================================================
// ShimmerCodeBlock — 骨架屏代码块
// =============================================================
@Composable
private fun ShimmerCodeBlock() {
    val infiniteTransition = rememberInfiniteTransition(label = "shimmer")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shimmer_alpha"
    )

    Column {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF0D1117))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                repeat(5) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(if (it == 4) 0.6f else 0.9f)
                            .height(10.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(RemoteToolkitColors.Purple.copy(alpha = alpha))
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                }
            }
        }
    }
}
