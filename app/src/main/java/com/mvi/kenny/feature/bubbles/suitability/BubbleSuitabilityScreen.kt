package com.mvi.kenny.feature.bubbles.suitability

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
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
import com.mvi.kenny.feature.bubbles.ActivitySuitability
import com.mvi.kenny.feature.bubbles.ComplianceResult
import com.mvi.kenny.feature.bubbles.IssueSeverity

/**
 * ============================================================
 * BubbleSuitabilityScreen — Activity 浮窗适用性分析
 * ============================================================
 * PRD-148: Android 17 App Bubbles 浮窗开发工具包
 * 分析每个 Activity 的 UI 布局和交互模式，输出适合/不适合浮窗的报告。
 *
 * 分析维度：
 * - 多窗口配置完整性
 * - 屏幕方向依赖性
 * - 布局层级复杂度
 * - 交互模式适应性
 * - 性能消耗预估
 *
 * 最终输出 0-100 的适用性评分和建议。
 *
 * @param viewModel BubblesViewModel instance / BubblesViewModel 实例
 * @see ActivitySuitability Activity 适用性数据类
 * @see BubblesIntent.AnalyzeSuitabilities 开始分析
 */
@Composable
fun BubbleSuitabilityScreen(
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
            text = "Activity Suitability Analysis",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "Activity 浮窗适用性分析 — 评估每个 Activity 是否适合以浮窗形态运行",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // ============================================================
        // Analyze Button & Progress / 分析按钮与进度
        // ============================================================
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = { viewModel.sendIntent(BubblesIntent.AnalyzeSuitabilities) },
                enabled = !state.isScanning
            ) {
                Icon(
                    imageVector = Icons.Default.Analytics,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (state.isScanning) "Analyzing..." else "Start Analysis"
                )
            }

            if (state.isScanning) {
                val animatedProgress by animateFloatAsState(
                    targetValue = state.scanProgress,
                    label = "analysis_progress"
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
                            .width(100.dp)
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp)),
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ============================================================
        // Overall Summary / 整体摘要
        // ============================================================
        if (state.suitabilities.isNotEmpty()) {
            OverallSummaryCard(suitabilities = state.suitabilities)
            Spacer(modifier = Modifier.height(16.dp))
        }

        // ============================================================
        // Suitability Cards / 适用性卡片列表
        // ============================================================
        if (state.suitabilities.isNotEmpty()) {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(state.suitabilities) { suitability ->
                    SuitabilityCard(suitability = suitability)
                }
                // Bottom spacing / 底部留白
                item { Spacer(modifier = Modifier.height(16.dp)) }
            }
        } else if (!state.isScanning) {
            // Empty state / 空状态
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Analytics,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = BubblesColors.Primary.copy(alpha = 0.4f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No analysis yet",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "点击上方按钮开始分析",
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
 * OverallSummaryCard — 整体摘要卡片
 * ============================================================
 * 显示所有 Activity 的综合评分和统计信息。
 *
 * @param suitabilities List of activity suitabilities / Activity 适用性列表
 */
@Composable
private fun OverallSummaryCard(suitabilities: List<ActivitySuitability>) {
    val avgScore = if (suitabilities.isNotEmpty()) {
        suitabilities.sumOf { it.suitabilityScore } / suitabilities.size
    } else 0

    val suitableCount = suitabilities.count { it.suitabilityScore >= 70 }
    val moderateCount = suitabilities.count { it.suitabilityScore in 40..69 }
    val unsuitableCount = suitabilities.count { it.suitabilityScore < 40 }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Overall Score / 综合评分",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = avgScore.toString(),
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold,
                            color = when {
                                avgScore >= 70 -> BubblesColors.BubblesActive
                                avgScore >= 40 -> Color(0xFFFF9800)
                                else -> Color(0xFFE53935)
                            }
                        )
                        Text(
                            text = " / 100",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                    }
                }

                // Circular score indicator / 圆形评分指示器
                Box(
                    modifier = Modifier.size(64.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        progress = { avgScore / 100f },
                        modifier = Modifier.fillMaxSize(),
                        color = when {
                            avgScore >= 70 -> BubblesColors.BubblesActive
                            avgScore >= 40 -> Color(0xFFFF9800)
                            else -> Color(0xFFE53935)
                        },
                        trackColor = MaterialTheme.colorScheme.surfaceVariant,
                        strokeWidth = 6.dp,
                    )
                    Text(
                        text = "$avgScore",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Distribution / 分布
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ScoreDistributionItem(
                    count = suitableCount,
                    label = "Suitable\n适用",
                    color = BubblesColors.BubblesActive
                )
                ScoreDistributionItem(
                    count = moderateCount,
                    label = "Moderate\n一般",
                    color = Color(0xFFFF9800)
                )
                ScoreDistributionItem(
                    count = unsuitableCount,
                    label = "Unsuitable\n不推荐",
                    color = Color(0xFFE53935)
                )
            }
        }
    }
}

/**
 * ============================================================
 * ScoreDistributionItem — 评分分布项
 * ============================================================
 *
 * @param count Activity count / Activity 数量
 * @param label Label text / 标签文本
 * @param color Indicator color / 指示器颜色
 */
@Composable
private fun ScoreDistributionItem(count: Int, label: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            lineHeight = 14.sp
        )
    }
}

/**
 * ============================================================
 * SuitabilityCard — Activity 适用性卡片
 * ============================================================
 * 展示单个 Activity 的适用性分析结果。
 *
 * @param suitability Activity suitability data / Activity 适用性数据
 */
@Composable
private fun SuitabilityCard(suitability: ActivitySuitability) {
    val scoreColor = when {
        suitability.suitabilityScore >= 70 -> BubblesColors.BubblesActive
        suitability.suitabilityScore >= 40 -> Color(0xFFFF9800)
        else -> Color(0xFFE53935)
    }

    val scoreIcon = when {
        suitability.suitabilityScore >= 70 -> Icons.Default.CheckCircle
        suitability.suitabilityScore >= 40 -> Icons.Default.Warning
        else -> Icons.Default.Error
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: Activity name + Score / 头部：Activity 名称 + 评分
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = suitability.activityName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = suitability.modulePath,
                        style = MaterialTheme.typography.labelSmall,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = scoreIcon,
                        contentDescription = null,
                        tint = scoreColor,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${suitability.suitabilityScore}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = scoreColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Score bar / 评分条
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(fraction = suitability.suitabilityScore / 100f)
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(scoreColor)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Recommendation / 建议
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(scoreColor.copy(alpha = 0.1f))
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = when {
                        suitability.suitabilityScore >= 70 -> Icons.Default.CheckCircle
                        else -> Icons.Default.Warning
                    },
                    contentDescription = null,
                    tint = scoreColor,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = suitability.recommendationZh,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = if (suitability.suitabilityScore >= 70) FontWeight.Normal else FontWeight.Medium
                )
            }

            // Issues / 问题列表
            if (suitability.issues.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Issues / 问题 (${suitability.issues.size})",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))

                suitability.issues.take(3).forEach { issue ->
                    IssueItem(result = issue)
                    Spacer(modifier = Modifier.height(6.dp))
                }
            }
        }
    }
}

/**
 * ============================================================
 * IssueItem — 问题项
 * ============================================================
 *
 * @param result Compliance result / 合规结果
 */
@Composable
private fun IssueItem(result: ComplianceResult) {
    val issueColor = when (result.issueType) {
        IssueSeverity.P0 -> Color(0xFFE53935)
        IssueSeverity.P1 -> Color(0xFFFF9800)
        IssueSeverity.P2 -> Color(0xFFFFEB3B)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(issueColor.copy(alpha = 0.1f))
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(issueColor)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = result.issueTitleZh,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = result.issueType.labelZh,
            style = MaterialTheme.typography.labelSmall,
            color = issueColor
        )
    }
}
