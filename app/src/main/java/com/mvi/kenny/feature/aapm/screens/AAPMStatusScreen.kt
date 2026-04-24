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
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mvi.kenny.feature.aapm.AAPMDashboardIntent
import com.mvi.kenny.feature.aapm.AAPMDashboardState
import com.mvi.kenny.feature.aapm.AAPMState
import com.mvi.kenny.feature.aapm.AAPMTab

// =============================================================
// AAPMStatusScreen — AAPM 状态概览屏幕
// =============================================================
/**
 * AAPM Status Screen / AAPM 状态概览屏幕
 *
 * Displays:
 * - Current AAPM (Advanced Protection Mode) status
 * - Risk summary (high/medium/low counts)
 * - Quick action buttons (Check Status, Scan Services)
 *
 * @param state Current dashboard state / 当前仪表盘状态
 * @param onIntent Intent callback to ViewModel / Intent 回调到 ViewModel
 */
@Composable
fun AAPMStatusScreen(
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
            text = "Android 17 AAPM",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "Advanced Protection Mode 合规检测",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(24.dp))

        // =============================================================
        // AAPM Status Card / AAPM 状态卡片
        // =============================================================
        AAPMStatusCard(
            aapmState = state.aapmState,
            onCheckStatus = { onIntent(AAPMDashboardIntent.CheckAAPMStatus) }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // =============================================================
        // Risk Summary Card / 风险摘要卡片
        // =============================================================
        RiskSummaryCard(
            highRiskCount = state.highRiskCount,
            mediumRiskCount = state.mediumRiskCount,
            lowRiskCount = state.lowRiskCount,
            totalServices = state.services.size
        )

        Spacer(modifier = Modifier.height(16.dp))

        // =============================================================
        // Quick Actions / 快捷操作
        // =============================================================
        QuickActionsCard(
            isScanning = state.isScanning,
            onScan = { onIntent(AAPMDashboardIntent.ScanAccessibilityServices) },
            onGenerateReport = { onIntent(AAPMDashboardIntent.GenerateComplianceReport) }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // =============================================================
        // Info Card / 信息卡片
        // =============================================================
        InfoCard()
    }
}

// =============================================================
// AAPMStatusCard — AAPM 状态卡片
// =============================================================
/**
 * AAPM Status Card / AAPM 状态卡片
 *
 * Shows the current status of Advanced Protection Mode.
 * Green = Enabled, Orange = Disabled, Gray = Not Supported
 *
 * @param aapmState Current AAPM state / 当前 AAPM 状态
 * @param onCheckStatus Callback for check button / 检查按钮回调
 */
@Composable
private fun AAPMStatusCard(
    aapmState: AAPMState,
    onCheckStatus: () -> Unit
) {
    val (statusColor, statusText, statusIcon) = when (aapmState) {
        is AAPMState.NotSupported -> Triple(
            Color(0xFF9E9E9E), // Gray / 灰色
            "API 35+ Required\nAndroid 17.2 及以上",
            Icons.Default.Warning
        )
        is AAPMState.Disabled -> Triple(
            Color(0xFFFFA726), // Orange / 橙色
            "保护已关闭\nProtection Disabled",
            Icons.Default.Warning
        )
        is AAPMState.Enabled -> Triple(
            Color(0xFF66BB6A), // Green / 绿色
            if (aapmState.isUserEnrolled) "保护已开启\n保护已激活" else "未注册用户\nNot Enrolled",
            Icons.Default.CheckCircle
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(statusColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = statusIcon,
                        contentDescription = null,
                        tint = statusColor,
                        modifier = Modifier.size(28.dp)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "保护状态",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = statusText,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Status details for Enabled state / 启用状态下的详细信息
            if (aapmState is AAPMState.Enabled) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF66BB6A).copy(alpha = 0.1f))
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "受影响服务",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${aapmState.affectedServices.size}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF66BB6A)
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "用户注册",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = if (aapmState.isUserEnrolled) "是" else "否",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = if (aapmState.isUserEnrolled) Color(0xFF66BB6A) else Color(0xFFFFA726)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            OutlinedButton(
                onClick = onCheckStatus,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("检查状态 / Check Status")
            }
        }
    }
}

// =============================================================
// RiskSummaryCard — 风险摘要卡片
// =============================================================
/**
 * Risk Summary Card / 风险摘要卡片
 *
 * Shows count of services at each risk level.
 *
 * @param highRiskCount High risk services count / 高风险服务数量
 * @param mediumRiskCount Medium risk services count / 中风险服务数量
 * @param lowRiskCount Low risk services count / 低风险服务数量
 * @param totalServices Total services count / 总服务数量
 */
@Composable
private fun RiskSummaryCard(
    highRiskCount: Int,
    mediumRiskCount: Int,
    lowRiskCount: Int,
    totalServices: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "风险摘要",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "Accessibility Services 风险分析",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                RiskCountItem(
                    count = highRiskCount,
                    label = "高风险",
                    color = Color(0xFFFF5252)
                )
                RiskCountItem(
                    count = mediumRiskCount,
                    label = "中风险",
                    color = Color(0xFFFFA726)
                )
                RiskCountItem(
                    count = lowRiskCount,
                    label = "低风险",
                    color = Color(0xFF66BB6A)
                )
            }

            if (totalServices > 0) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "共 $totalServices 个服务",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

// =============================================================
// RiskCountItem — 风险计数项
// =============================================================
/**
 * Risk Count Item / 风险计数项
 *
 * @param count Count value / 计数
 * @param label Label text / 标签文本
 * @param color Display color / 显示颜色
 */
@Composable
private fun RiskCountItem(
    count: Int,
    label: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// =============================================================
// QuickActionsCard — 快捷操作卡片
// =============================================================
/**
 * Quick Actions Card / 快捷操作卡片
 *
 * @param isScanning Whether scan is in progress / 扫描是否进行中
 * @param onScan Scan callback / 扫描回调
 * @param onGenerateReport Generate report callback / 生成报告回调
 */
@Composable
private fun QuickActionsCard(
    isScanning: Boolean,
    onScan: () -> Unit,
    onGenerateReport: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "快捷操作",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onScan,
                    modifier = Modifier.weight(1f),
                    enabled = !isScanning
                ) {
                    if (isScanning) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text(if (isScanning) "扫描中..." else "扫描服务")
                }

                OutlinedButton(
                    onClick = onGenerateReport,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("生成报告")
                }
            }
        }
    }
}

// =============================================================
// InfoCard — 信息卡片
// =============================================================
/**
 * Info Card / 信息卡片
 *
 * Shows information about AAPM requirements.
 */
@Composable
private fun InfoCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1E88E5).copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Shield,
                    contentDescription = null,
                    tint = Color(0xFF1E88E5),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "关于 AAPM",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF1E88E5)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Android 17 引入的 Advanced Protection Mode (AAPM) 会自动限制可能滥用 Accessibility API 的应用。",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "AAPM requires Android 17.2+ (API 35) and QUERY_ALL_PACKAGES permission to detect other apps' Accessibility Services.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
