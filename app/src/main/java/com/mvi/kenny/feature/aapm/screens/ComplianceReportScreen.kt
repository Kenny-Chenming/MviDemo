package com.mvi.kenny.feature.aapm.screens

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mvi.kenny.feature.aapm.AAPMDashboardIntent
import com.mvi.kenny.feature.aapm.AAPMDashboardState
import com.mvi.kenny.feature.aapm.ComplianceIssue
import com.mvi.kenny.feature.aapm.ComplianceReport
import com.mvi.kenny.feature.aapm.RiskLevel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// =============================================================
// ComplianceReportScreen — 合规报告屏幕
// =============================================================
/**
 * Compliance Report Screen / 合规报告屏幕
 *
 * Displays compliance report with:
 * - Overall compliance status
 * - Service counts (compliant/non-compliant)
 * - List of issues
 * - Generate/export actions
 *
 * @param state Current dashboard state / 当前仪表盘状态
 * @param onIntent Intent callback to ViewModel / Intent 回调到 ViewModel
 */
@Composable
fun ComplianceReportScreen(
    state: AAPMDashboardState,
    onIntent: (AAPMDashboardIntent) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // =============================================================
        // Header / 标题区
        // =============================================================
        Text(
            text = "合规报告",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "AAPM Compliance Report / AAPM 合规报告",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        // =============================================================
        // Generate Button / 生成按钮
        // =============================================================
        if (state.complianceReport == null && !state.isGeneratingReport) {
            GenerateReportPrompt(
                onGenerate = { onIntent(AAPMDashboardIntent.GenerateComplianceReport) }
            )
        } else if (state.isGeneratingReport) {
            GeneratingReportView()
        } else {
            state.complianceReport?.let { report ->
                ComplianceReportContent(
                    report = report,
                    onClear = { onIntent(AAPMDashboardIntent.ClearReport) },
                    onRegenerate = { onIntent(AAPMDashboardIntent.GenerateComplianceReport) }
                )
            }
        }
    }
}

// =============================================================
// GenerateReportPrompt — 生成报告提示
// =============================================================
/**
 * Generate Report Prompt / 生成报告提示
 *
 * @param onGenerate Generate callback / 生成回调
 */
@Composable
private fun GenerateReportPrompt(
    onGenerate: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF1E88E5).copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Description,
                    contentDescription = null,
                    tint = Color(0xFF1E88E5),
                    modifier = Modifier.size(40.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "生成合规报告",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "基于扫描的 Accessibility Services 生成 AAPM 合规报告",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onGenerate,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("生成报告 / Generate Report")
            }
        }
    }
}

// =============================================================
// GeneratingReportView — 生成中视图
// =============================================================
/**
 * Generating Report View / 生成中视图
 */
@Composable
private fun GeneratingReportView() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(48.dp),
                color = Color(0xFF1E88E5)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "正在生成报告...",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Text(
                text = "Generating compliance report...",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// =============================================================
// ComplianceReportContent — 合规报告内容
// =============================================================
/**
 * Compliance Report Content / 合规报告内容
 *
 * @param report Compliance report / 合规报告
 * @param onClear Clear callback / 清除回调
 * @param onRegenerate Regenerate callback / 重新生成回调
 */
@Composable
private fun ComplianceReportContent(
    report: ComplianceReport,
    onClear: () -> Unit,
    onRegenerate: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()) }

    Column {
        // =============================================================
        // Summary Card / 摘要卡片
        // =============================================================
        ComplianceSummaryCard(report = report, dateFormat = dateFormat)

        Spacer(modifier = Modifier.height(16.dp))

        // =============================================================
        // Service Stats / 服务统计
        // =============================================================
        ServiceStatsCard(report = report)

        Spacer(modifier = Modifier.height(16.dp))

        // =============================================================
        // Issues List / 问题列表
        // =============================================================
        if (report.issues.isNotEmpty()) {
            Text(
                text = "合规问题",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))

            report.issues.forEach { issue ->
                IssueCard(issue = issue)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // =============================================================
        // Actions / 操作按钮
        // =============================================================
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onClear,
                modifier = Modifier.weight(1f)
            ) {
                Text("清除报告")
            }
            Button(
                onClick = onRegenerate,
                modifier = Modifier.weight(1f)
            ) {
                Text("重新生成")
            }
        }
    }
}

// =============================================================
// ComplianceSummaryCard — 合规摘要卡片
// =============================================================
/**
 * Compliance Summary Card / 合规摘要卡片
 */
@Composable
private fun ComplianceSummaryCard(
    report: ComplianceReport,
    dateFormat: SimpleDateFormat
) {
    val (icon, color, text) = if (report.isCompliant) {
        Triple(Icons.Default.CheckCircle, Color(0xFF66BB6A), "合规 / Compliant")
    } else {
        Triple(Icons.Default.Error, Color(0xFFFF5252), "不合规 / Non-Compliant")
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(40.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = text,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = color
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "生成时间: ${dateFormat.format(Date(report.generatedAt))}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// =============================================================
// ServiceStatsCard — 服务统计卡片
// =============================================================
/**
 * Service Stats Card / 服务统计卡片
 */
@Composable
private fun ServiceStatsCard(report: ComplianceReport) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "服务统计",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(
                    value = report.totalServices.toString(),
                    label = "总计",
                    color = Color(0xFF1E88E5)
                )
                StatItem(
                    value = report.compliantServices.toString(),
                    label = "合规",
                    color = Color(0xFF66BB6A)
                )
                StatItem(
                    value = report.nonCompliantServices.toString(),
                    label = "不合规",
                    color = Color(0xFFFF5252)
                )
            }
        }
    }
}

// =============================================================
// StatItem — 统计项
// =============================================================
/**
 * Stat Item / 统计项
 */
@Composable
private fun StatItem(
    value: String,
    label: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// =============================================================
// IssueCard — 问题卡片
// =============================================================
/**
 * Issue Card / 问题卡片
 *
 * @param issue Compliance issue / 合规问题
 */
@Composable
private fun IssueCard(issue: ComplianceIssue) {
    val color = when (issue.severity) {
        RiskLevel.HIGH -> Color(0xFFFF5252)
        RiskLevel.MEDIUM -> Color(0xFFFFA726)
        RiskLevel.LOW -> Color(0xFF66BB6A)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(color.copy(alpha = 0.15f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = issue.severity.label,
                        style = MaterialTheme.typography.labelSmall,
                        color = color
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = issue.serviceName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = issue.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xFF1E88E5).copy(alpha = 0.1f))
                    .padding(8.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = Color(0xFF1E88E5),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = issue.suggestion,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF1E88E5)
                )
            }
        }
    }
}
