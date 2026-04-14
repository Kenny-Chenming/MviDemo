package com.mvi.kenny.feature.remotetoolkit.sub_screens

import com.mvi.kenny.feature.remotetoolkit.RemoteToolkitState
import com.mvi.kenny.feature.remotetoolkit.RemoteToolkitIntent
import com.mvi.kenny.feature.remotetoolkit.RemoteToolkitEffect
import com.mvi.kenny.feature.remotetoolkit.RemoteToolkitColors


import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Canvas
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mvi.kenny.feature.remotetoolkit.CompatibilityIssue
import com.mvi.kenny.feature.remotetoolkit.IssueSeverity
import com.mvi.kenny.feature.remotetoolkit.ScanStatus

// =============================================================
// CompatibilityScannerScreen — 序列化兼容性检测页面
// Compatibility Scanner / 兼容性扫描器
// =============================================================

@Composable
fun CompatibilityScannerScreen(
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
        // ============================================================
        // Header + Run Button / 标题 + 运行按钮
        // ============================================================
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Compatibility Scanner",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = RemoteToolkitColors.OnSurface
                )
                Button(
                    onClick = { onIntent(RemoteToolkitIntent.RunCompatibilityScan) },
                    enabled = state.scanStatus != ScanStatus.RUNNING,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = RemoteToolkitColors.Purple,
                        disabledContainerColor = RemoteToolkitColors.SurfaceVariant
                    )
                ) {
                    if (state.scanStatus == ScanStatus.RUNNING) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = RemoteToolkitColors.OnSurface
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Scanning...")
                    } else {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Run Scan")
                    }
                }
            }
        }

        // ============================================================
        // Module Selector / 模块选择器
        // ============================================================
        item {
            var expanded by remember { mutableStateOf(false) }
            val modules = listOf("app", "feature:home", "feature:profile", "feature:settings")

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = RemoteToolkitColors.Surface),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Project Module / 项目模块",
                        style = MaterialTheme.typography.labelMedium,
                        color = RemoteToolkitColors.OnSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Box {
                        OutlinedTextField(
                            value = state.selectedModule,
                            onValueChange = {},
                            readOnly = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { expanded = true },
                            trailingIcon = {
                                Text(
                                    text = "▼",
                                    color = RemoteToolkitColors.OnSurfaceVariant,
                                    modifier = Modifier.padding(end = 8.dp)
                                )
                            },
                            colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = RemoteToolkitColors.Purple,
                                unfocusedBorderColor = RemoteToolkitColors.Outline
                            )
                        )
                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            modules.forEach { module ->
                                DropdownMenuItem(
                                    text = { Text(module) },
                                    onClick = {
                                        // onIntent(RemoteToolkitIntent.SelectModule(module))
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        // ============================================================
        // Scan Results Card / 扫描结果卡片
        // ============================================================
        if (state.scanStatus == ScanStatus.DONE) {
            item {
                ScanResultsCard(state = state)
            }

            // ============================================================
            // Issue List / 问题列表
            // ============================================================
            item {
                Text(
                    text = "Issues Found: ${state.scanIssues.size}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    color = RemoteToolkitColors.OnSurface
                )
            }

            items(state.scanIssues, key = { it.id }) { issue ->
                IssueCard(
                    issue = issue,
                    isExpanded = issue.id in state.expandedIssueIds,
                    onToggle = { onIntent(RemoteToolkitIntent.ToggleIssueExpanded(issue.id)) }
                )
            }
        } else if (state.scanStatus == ScanStatus.IDLE) {
            item {
                IdleCard()
            }
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}

// =============================================================
// ScanResultsCard — 扫描结果卡片（带进度条和评分圆环）
// =============================================================
@Composable
private fun ScanResultsCard(state: RemoteToolkitState) {
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
                Column {
                    Text(
                        text = "Overall Score / 总体评分",
                        style = MaterialTheme.typography.labelMedium,
                        color = RemoteToolkitColors.OnSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "${state.scanScore}%",
                            style = MaterialTheme.typography.displaySmall,
                            fontWeight = FontWeight.Bold,
                            color = when {
                                state.scanScore >= 80 -> RemoteToolkitColors.PassColor
                                state.scanScore >= 60 -> RemoteToolkitColors.WarningColor
                                else -> RemoteToolkitColors.CriticalColor
                            }
                        )
                        if (state.scanScore >= 80) {
                            Text(" ✅", color = RemoteToolkitColors.PassColor)
                        } else {
                            Text(" ⚠️", color = RemoteToolkitColors.WarningColor)
                        }
                    }
                }
                ScoreRing(score = state.scanScore)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Progress bar / 进度条
            LinearProgressIndicator(
                progress = { state.scanScore / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = when {
                    state.scanScore >= 80 -> RemoteToolkitColors.PassColor
                    state.scanScore >= 60 -> RemoteToolkitColors.WarningColor
                    else -> RemoteToolkitColors.CriticalColor
                },
                trackColor = RemoteToolkitColors.SurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Issue counts / 问题计数
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                IssueCountBadge(
                    label = "Critical / 阻塞",
                    count = state.criticalIssueCount,
                    color = RemoteToolkitColors.CriticalColor
                )
                IssueCountBadge(
                    label = "Warning / 警告",
                    count = state.warningIssueCount,
                    color = RemoteToolkitColors.WarningColor
                )
                IssueCountBadge(
                    label = "Pass / 通过",
                    count = state.passIssueCount,
                    color = RemoteToolkitColors.PassColor
                )
            }
        }
    }
}

// =============================================================
// ScoreRing — 评分圆环（Canvas 绘制）
// =============================================================
@Composable
private fun ScoreRing(score: Int) {
    val sweepAngle = score / 100f * 360f
    val color = when {
        score >= 80 -> RemoteToolkitColors.PassColor
        score >= 60 -> RemoteToolkitColors.WarningColor
        else -> RemoteToolkitColors.CriticalColor
    }
    Canvas(modifier = Modifier.size(80.dp)) {
        drawArc(
            color = RemoteToolkitColors.SurfaceVariant,
            startAngle = -90f,
            sweepAngle = 360f,
            useCenter = false,
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 8.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round),
            size = Size(size.width, size.height)
        )
        drawArc(
            color = color,
            startAngle = -90f,
            sweepAngle = sweepAngle,
            useCenter = false,
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 8.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round),
            size = Size(size.width, size.height)
        )
    }
}

// =============================================================
// IssueCountBadge — 问题数量徽章
// =============================================================
@Composable
private fun IssueCountBadge(label: String, count: Int, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "$count",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = RemoteToolkitColors.OnSurfaceVariant
        )
    }
}

// =============================================================
// IssueCard — 问题卡片
// =============================================================
@Composable
private fun IssueCard(
    issue: CompatibilityIssue,
    isExpanded: Boolean,
    onToggle: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle),
        colors = CardDefaults.cardColors(containerColor = RemoteToolkitColors.Surface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(issue.severity.color.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = when (issue.severity) {
                            IssueSeverity.CRITICAL -> Icons.Default.Warning
                            IssueSeverity.WARNING -> Icons.Default.Warning
                            IssueSeverity.PASS -> Icons.Default.CheckCircle
                        },
                        contentDescription = null,
                        tint = issue.severity.color,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Composable: ${issue.composableName}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = RemoteToolkitColors.OnSurface
                    )
                    Text(
                        text = "File: ${issue.filePath}:${issue.line}",
                        style = MaterialTheme.typography.labelSmall,
                        color = RemoteToolkitColors.OnSurfaceVariant
                    )
                }
                Text(
                    text = "▼",
                    color = RemoteToolkitColors.OnSurfaceVariant,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    Text(
                        text = issue.reason,
                        style = MaterialTheme.typography.bodySmall,
                        color = RemoteToolkitColors.OnSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Suggestion / 建议:",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Medium,
                        color = RemoteToolkitColors.OnSurfaceVariant
                    )
                    Text(
                        text = issue.suggestion,
                        style = MaterialTheme.typography.bodySmall,
                        color = RemoteToolkitColors.PassColor
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = { },
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = RemoteToolkitColors.Purple
                            )
                        ) {
                            Icon(Icons.Default.Code, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("View Code", style = MaterialTheme.typography.labelSmall)
                        }
                        OutlinedButton(
                            onClick = { },
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = RemoteToolkitColors.Info
                            )
                        ) {
                            Text("Learn More", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
        }
    }
}

// =============================================================
// IdleCard — 空闲状态卡片
// =============================================================
@Composable
private fun IdleCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = RemoteToolkitColors.Surface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.Warning,
                contentDescription = null,
                tint = RemoteToolkitColors.OnSurfaceVariant,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "No scan results yet / 暂无扫描结果",
                style = MaterialTheme.typography.bodyLarge,
                color = RemoteToolkitColors.OnSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Click \"Run Scan\" to start compatibility check",
                style = MaterialTheme.typography.bodySmall,
                color = RemoteToolkitColors.OnSurfaceVariant
            )
        }
    }
}
