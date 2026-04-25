package com.mvi.kenny.feature.largescreen.screens

// ================================================================
// ComplianceDetectorScreen — 多窗口合规性自动化检测器
// ================================================================
// Tool 1: Multi-window compliance automated detector.
//
// PRD-155: Android 17 大屏强制适配
// Detects non-compliant configurations: resizeableActivity=false,
// configChanges=orientation|screenSize, missing targetSdk upgrades.
//
// Features:
//   - APK path / code path input
//   - Run compliance check
//   - Violation list with severity badges
//   - Violation detail sheet with fix suggestions
//   - Fix diff generation
// —————————————————————————————————————————————————————————————

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mvi.kenny.feature.largescreen.ComplianceViolation
import com.mvi.kenny.feature.largescreen.LargeScreenIntent
import com.mvi.kenny.feature.largescreen.LargeScreenState
import com.mvi.kenny.feature.largescreen.LargeScreenSubTab
import com.mvi.kenny.feature.largescreen.SummaryCard
import com.mvi.kenny.feature.largescreen.ViolationSeverity

/**
 * ============================================================
 * ComplianceDetectorScreen — 合规检测器主界面
 * ============================================================
 *
 * @param state 当前状态
 * @param onIntent Intent 发送器
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComplianceDetectorScreen(
    state: LargeScreenState,
    onIntent: (LargeScreenIntent) -> Unit
) {
    var targetPath by remember { mutableStateOf(state.checkTargetPath) }
    var showDiffSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // ─────────────────────────────────────────────────────
        // Input Section — 输入检查目标
        // ─────────────────────────────────────────────────────
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "检查目标",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "输入 APK 路径或代码目录路径，系统将自动检测不合规配置",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = targetPath,
                    onValueChange = {
                        targetPath = it
                        onIntent(LargeScreenIntent.UpdateCheckTarget(it))
                    },
                    label = { Text("APK 路径或代码路径") },
                    placeholder = { Text("/path/to/your/app") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    trailingIcon = {
                        if (targetPath.isNotBlank()) {
                            TextButton(onClick = {
                                onIntent(LargeScreenIntent.RunComplianceCheck(targetPath))
                            }) {
                                Icon(Icons.Default.PlayArrow, contentDescription = null)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("检测")
                            }
                        }
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ─────────────────────────────────────────────────────
        // Summary Cards — 结果摘要
        // ─────────────────────────────────────────────────────
        val errorCount = state.complianceViolations.count { it.severity == ViolationSeverity.ERROR }
        val warningCount = state.complianceViolations.count { it.severity == ViolationSeverity.WARNING }
        val infoCount = state.complianceViolations.count { it.severity == ViolationSeverity.INFO }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SummaryCard(
                title = "错误",
                value = errorCount.toString(),
                subtitle = "需立即修复",
                icon = Icons.Default.Error,
                accentColor = Color(0xFFB3261E),
                modifier = Modifier.weight(1f)
            )
            SummaryCard(
                title = "警告",
                value = warningCount.toString(),
                subtitle = "建议修复",
                icon = Icons.Default.Warning,
                accentColor = Color(0xFFF5A623),
                modifier = Modifier.weight(1f)
            )
            SummaryCard(
                title = "提示",
                value = infoCount.toString(),
                subtitle = "最佳实践",
                icon = Icons.Default.Info,
                accentColor = Color(0xFF146B3A),
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ─────────────────────────────────────────────────────
        // Violation List — 违规列表
        // ─────────────────────────────────────────────────────
        if (state.complianceViolations.isEmpty() && !state.isLoading) {
            EmptyStateCard(
                icon = Icons.Default.CheckCircle,
                title = "暂未检测",
                subtitle = "输入路径并点击检测开始分析"
            )
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(state.complianceViolations, key = { it.id }) { violation ->
                    ViolationCard(
                        violation = violation,
                        onClick = { onIntent(LargeScreenIntent.SelectViolation(violation)) },
                        onGenerateDiff = { onIntent(LargeScreenIntent.GenerateFixDiff(violation)) }
                    )
                }
            }
        }
    }

    // ─────────────────────────────────────────────────────────
    // Violation Detail Bottom Sheet
    // ─────────────────────────────────────────────────────────
    state.selectedViolation?.let { violation ->
        ModalBottomSheet(
            onDismissRequest = { onIntent(LargeScreenIntent.DismissViolationDetail) },
            sheetState = sheetState
        ) {
            ViolationDetailSheet(
                violation = violation,
                generatedDiff = state.generatedDiff,
                onGenerateDiff = { onIntent(LargeScreenIntent.GenerateFixDiff(violation)) }
            )
        }
    }
}

/**
 * ============================================================
 * ViolationCard — 违规项卡片
 * ============================================================
 */
@Composable
private fun ViolationCard(
    violation: ComplianceViolation,
    onClick: () -> Unit,
    onGenerateDiff: () -> Unit
) {
    val severityColor = when (violation.severity) {
        ViolationSeverity.ERROR -> Color(0xFFB3261E)
        ViolationSeverity.WARNING -> Color(0xFFF5A623)
        ViolationSeverity.INFO -> Color(0xFF146B3A)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .background(
                                color = severityColor.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "${violation.severity.emoji} ${violation.severity.displayName}",
                            style = MaterialTheme.typography.labelSmall,
                            color = severityColor,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                TextButton(onClick = onGenerateDiff) {
                    Icon(Icons.Default.Code, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("生成修复", fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = violation.issue,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = violation.file,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "第 ${violation.line} 行",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = violation.config,
                style = MaterialTheme.typography.bodySmall,
                fontFamily = FontFamily.Monospace,
                color = Color(0xFF6750A4)
            )
        }
    }
}

/**
 * ============================================================
 * ViolationDetailSheet — 违规详情底部 Sheet
 * ============================================================
 */
@Composable
private fun ViolationDetailSheet(
    violation: ComplianceViolation,
    generatedDiff: String?,
    onGenerateDiff: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "违规详情",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Severity
        DetailRow(label = "严重程度", value = "${violation.severity.emoji} ${violation.severity.displayName}")

        // File & Line
        DetailRow(label = "文件位置", value = "${violation.file}:${violation.line}")

        // Config
        DetailRow(label = "不合规配置", value = violation.config)

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "问题描述",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = violation.issue,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "修复建议",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = violation.fixSuggestion,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Generated Diff
        if (generatedDiff != null) {
            Text(
                text = "修复 Diff",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF1E1E1E)
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = generatedDiff,
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = FontFamily.Monospace,
                    color = Color(0xFFD4D4D4),
                    modifier = Modifier.padding(12.dp)
                )
            }
        } else {
            Button(
                onClick = onGenerateDiff,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF6750A4)
                )
            ) {
                Icon(Icons.Default.Code, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("生成修复 Diff")
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

/**
 * ============================================================
 * DetailRow — 详情行组件
 * ============================================================
 */
@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Text(
            text = "$label: ",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.width(100.dp)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * ============================================================
 * EmptyStateCard — 空状态卡片
 * ============================================================
 */
@Composable
private fun EmptyStateCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
