package com.mvi.kenny.feature.audiohardening.screens

// ================================================================
// MediaSessionBinderScreen — MediaSession 绑定引导器（工具④）
// ================================================================
// 分步骤引导绑定 MediaSession 到后台音频。
//
// PRD-145 工具④
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
import com.mvi.kenny.feature.audiohardening.MediaSessionBinderIntent
import com.mvi.kenny.feature.audiohardening.MediaSessionBinderViewModel

private val ColorCodeBackground = Color(0xFF1E1E1E)
private val ColorSuccess = Color(0xFF4CAF50)

@Composable
fun MediaSessionBinderScreen(
    onBack: () -> Unit = {},
    viewModel: MediaSessionBinderViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "④ MediaSession 绑定引导器",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "分步骤引导绑定 MediaSession 到后台音频",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 步骤指示器
        StepperIndicator(
            currentStep = state.currentStep,
            totalSteps = state.totalSteps,
            stepTitles = state.stepTitles
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 步骤内容
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            shape = RoundedCornerShape(12.dp),
            color = ColorCodeBackground
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // 步骤标题
                Text(
                    text = "Step ${state.currentStep + 1}: ${state.stepTitles.getOrNull(state.currentStep) ?: ""}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = ColorSuccess
                )
                Spacer(Modifier.height(12.dp))

                // 代码模板
                val code = state.codeTemplates.getOrNull(state.currentStep) ?: ""
                Text(
                    text = code,
                    fontFamily = FontFamily.Monospace,
                    fontSize = MaterialTheme.typography.bodySmall.fontSize,
                    color = Color(0xFFD4D4D4),
                    lineHeight = MaterialTheme.typography.bodySmall.lineHeight * 1.4
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 导航按钮
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = { viewModel.sendIntent(MediaSessionBinderIntent.PreviousStep) },
                enabled = state.currentStep > 0,
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.ArrowBack, null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("上一步")
            }

            if (state.currentStep < state.totalSteps - 1) {
                Button(
                    onClick = { viewModel.sendIntent(MediaSessionBinderIntent.NextStep) },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("下一步")
                    Spacer(Modifier.width(4.dp))
                    Icon(Icons.Default.ArrowForward, null, modifier = Modifier.size(16.dp))
                }
            } else {
                Button(
                    onClick = { viewModel.sendIntent(MediaSessionBinderIntent.CopyAllCode) },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = ColorSuccess)
                ) {
                    Icon(Icons.Default.ContentCopy, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("复制全部代码")
                }
            }
        }
    }
}

@Composable
private fun StepperIndicator(
    currentStep: Int,
    totalSteps: Int,
    stepTitles: List<String>
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(totalSteps) { step ->
            val isCompleted = step < currentStep
            val isCurrent = step == currentStep

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            when {
                                isCompleted -> ColorSuccess
                                isCurrent -> MaterialTheme.colorScheme.primary
                                else -> Color(0xFF9E9E9E)
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (isCompleted) {
                        Icon(Icons.Default.Check, null, tint = Color.White, modifier = Modifier.size(16.dp))
                    } else {
                        Text(
                            "${step + 1}",
                            color = Color.White,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    stepTitles.getOrNull(step) ?: "",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isCurrent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    maxLines = 2
                )
            }

            if (step < totalSteps - 1) {
                Box(
                    modifier = Modifier
                        .weight(0.3f)
                        .height(2.dp)
                        .background(if (step < currentStep) ColorSuccess else Color(0xFF9E9E9E))
                )
            }
        }
    }
}
