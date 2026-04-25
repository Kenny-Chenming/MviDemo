package com.mvi.kenny.feature.bubbles.compliance

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mvi.kenny.feature.bubbles.BubblesColors
import com.mvi.kenny.feature.bubbles.BubblesIntent
import com.mvi.kenny.feature.bubbles.BubblesViewModel
import com.mvi.kenny.feature.bubbles.ComplianceResult
import com.mvi.kenny.feature.bubbles.IssueSeverity

/**
 * ============================================================
 * BubbleComplianceScreen — 多窗口合规检测
 * ============================================================
 * PRD-148: Android 17 App Bubbles 浮窗开发工具包
 * 多窗口合规检测工具入口，检测 App 的多窗口配置是否允许 Bubble 形态运行。
 *
 * 检测项目包括：
 * - android:resizeableActivity 配置
 * - android:screenOrientation 方向设置
 * - android:supportsPictureInPicture 配置
 * - 多窗口布局容错能力
 *
 * @param viewModel BubblesViewModel instance / BubblesViewModel 实例
 * @see BubblesViewModel 状态管理
 * @see BubblesIntent.StartComplianceScan 开始扫描
 */
@Composable
fun BubbleComplianceScreen(
    viewModel: BubblesViewModel = viewModel()
) {
    // Collect state from ViewModel / 从 ViewModel 收集状态
    val state by viewModel.state.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // ============================================================
        // Header / 头部
        // ============================================================
        Text(
            text = "Multi-Window Compliance Scan",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "多窗口合规检测 — 检测 App 是否支持 Bubble 浮窗形态",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // ============================================================
        // Scan Button & Progress / 扫描按钮与进度
        // ============================================================
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = { viewModel.sendIntent(BubblesIntent.StartComplianceScan) },
                enabled = !state.isScanning
            ) {
                if (state.isScanning) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Scanning...")
                } else {
                    Text("Start Scan")
                }
            }

            // Scan progress indicator / 扫描进度指示器
            if (state.isScanning) {
                val animatedProgress by animateFloatAsState(
                    targetValue = state.scanProgress,
                    label = "scan_progress"
                )
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "${(animatedProgress * 100).toInt()}%",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    LinearProgressIndicator(
                        progress = { animatedProgress },
                        modifier = Modifier
                            .width(120.dp)
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp)),
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ============================================================
        // Results Summary / 结果摘要
        // ============================================================
        if (state.complianceResults.isNotEmpty()) {
            ComplianceSummaryRow(results = state.complianceResults)
            Spacer(modifier = Modifier.height(16.dp))
        }

        // ============================================================
        // Results List / 结果列表
        // ============================================================
        if (state.complianceResults.isNotEmpty()) {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(state.complianceResults) { result ->
                    ComplianceResultCard(
                        result = result,
                        onCopyFix = { viewModel.sendIntent(BubblesIntent.CopyCode(result.suggestedFix)) }
                    )
                }
            }
        } else if (!state.isScanning) {
            // Empty state / 空状态
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = BubblesColors.BubblesActive.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No issues found",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "点击上方按钮开始扫描",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

/**
 * ============================================================
 * ComplianceSummaryRow — 结果摘要行
 * ============================================================
 * 显示 P0/P1/P2 问题统计摘要。
 *
 * @param results List of compliance results / 合规结果列表
 */
@Composable
private fun ComplianceSummaryRow(results: List<ComplianceResult>) {
    val p0Count = results.count { it.issueType == IssueSeverity.P0 }
    val p1Count = results.count { it.issueType == IssueSeverity.P1 }
    val p2Count = results.count { it.issueType == IssueSeverity.P2 }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        SummaryChip(count = p0Count, label = "P0 严重", color = Color(0xFFE53935))
        SummaryChip(count = p1Count, label = "P1 警告", color = Color(0xFFFF9800))
        SummaryChip(count = p2Count, label = "P2 注意", color = Color(0xFFFFEB3B))
    }
}

/**
 * ============================================================
 * SummaryChip — 摘要标签
 * ============================================================
 *
 * @param count Issue count / 问题数量
 * @param label Label text / 标签文本
 * @param color Chip color / 标签颜色
 */
@Composable
private fun SummaryChip(count: Int, label: String, color: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(color.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * ============================================================
 * ComplianceResultCard — 单条合规结果卡片
 * ============================================================
 * 展示单条合规检测结果，包含问题级别、描述、配置项和修复建议。
 *
 * @param result Compliance result / 合规结果
 * @param onCopyFix Callback to copy fix suggestion / 复制修复建议回调
 */
@Composable
private fun ComplianceResultCard(
    result: ComplianceResult,
    onCopyFix: () -> Unit
) {
    val cardColor = when (result.issueType) {
        IssueSeverity.P0 -> Color(0xFFE53935).copy(alpha = 0.1f)
        IssueSeverity.P1 -> Color(0xFFFF9800).copy(alpha = 0.1f)
        IssueSeverity.P2 -> Color(0xFFFFEB3B).copy(alpha = 0.1f)
    }

    val borderColor = when (result.issueType) {
        IssueSeverity.P0 -> Color(0xFFE53935)
        IssueSeverity.P1 -> Color(0xFFFF9800)
        IssueSeverity.P2 -> Color(0xFFFFEB3B)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header: Issue type badge + Activity name / 头部：问题级别标签 + Activity 名称
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Severity badge / 严重级别标签
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(borderColor.copy(alpha = 0.2f))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = result.issueType.label,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = borderColor
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = borderColor,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Text(
                    text = result.activityName,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Issue title / 问题标题
            Text(
                text = result.issueTitleZh,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Description / 描述
            Text(
                text = result.descriptionZh,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Config key / 配置项
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Config: ",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = result.configKey,
                    style = MaterialTheme.typography.labelSmall,
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Suggested fix / 建议修复
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "修复建议:",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = result.suggestedFix,
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                IconButton(onClick = onCopyFix) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copy fix",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}
