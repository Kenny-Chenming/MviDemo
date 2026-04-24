package com.mvi.kenny.feature.aapm.screens

import androidx.compose.animation.animateColorAsState
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Error
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mvi.kenny.feature.aapm.AAPMDashboardState
import com.mvi.kenny.feature.aapm.AAPMDashboardIntent
import com.mvi.kenny.feature.aapm.AAPMState

// =============================================================
// AAPMStatusScreen — AAPM 状态总览页面
// =============================================================
/**
 * AAPM Status Screen / AAPM 状态总览页面
 *
 * Displays the current AAPM (Advanced Protection Mode) status,
 * affected Accessibility Services, and isAccessibilityTool marker status.
 *
 * Key elements:
 * - Large status display (Enabled/Disabled/Not Supported)
 * - Affected services list
 * - isAccessibilityTool marker status
 *
 * @param state Current dashboard state / 当前仪表板状态
 * @param onIntent Intent handler / 意图处理器
 */
@Composable
fun AAPMStatusScreen(
    state: AAPMDashboardState,
    onIntent: (AAPMDashboardIntent) -> Unit
) {
    val scrollState = rememberScrollState()

    // Color scheme based on AAPM state / 基于 AAPM 状态的配色方案
    val statusColor by animateColorAsState(
        targetValue = when (state.aapmState) {
            is AAPMState.NotSupported -> Color(0xFF757575) // Gray
            is AAPMState.Disabled -> Color(0xFFFFA726) // Orange
            is AAPMState.Enabled -> if ((state.aapmState as AAPMState.Enabled).isUserEnrolled) {
                Color(0xFFEF5350) // Red for enrolled
            } else {
                Color(0xFF66BB6A) // Green for not enrolled
            }
        },
        label = "statusColor"
    )

    val backgroundColor by animateColorAsState(
        targetValue = statusColor.copy(alpha = 0.1f),
        label = "backgroundColor"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .verticalScroll(scrollState)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Status Card / 状态卡片
        AAPMStatusCard(
            aapmState = state.aapmState,
            statusColor = statusColor
        )

        Spacer(modifier = Modifier.height(16.dp))

        // isAccessibilityTool Status / isAccessibilityTool 状态
        AccessibilityToolStatusCard(
            isAccessibilityTool = state.isAccessibilityTool
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Affected Services / 受影响的服务
        if (state.aapmState is AAPMState.Enabled) {
            AffectedServicesCard(
                services = (state.aapmState as AAPMState.Enabled).affectedServices,
                enabledCount = state.enabledServicesCount
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Risk Summary / 风险摘要
        RiskSummaryCard(
            highRiskCount = state.highRiskCount,
            mediumRiskCount = state.mediumRiskCount,
            lowRiskCount = state.lowRiskCount
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Action Buttons / 操作按钮
        ActionButtons(
            isScanning = state.isScanning,
            onCheckStatus = { onIntent(AAPMDashboardIntent.CheckAAPMStatus) },
            onScanServices = { onIntent(AAPMDashboardIntent.ScanAccessibilityServices) }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Note for unsupported devices / 不支持设备的说明
        if (state.aapmState is AAPMState.NotSupported) {
            NoteCard(
                message = "此设备 Android < 17.2，不支持 AAPM。\nAdvancedProtectionManager API 从 Android 17.2 (API 35) 开始引入。"
            )
        }

        // Note for no services / 无服务时的说明
        if (state.services.isEmpty() && !state.isScanning) {
            NoteCard(
                message = "当前 App 未使用 Accessibility API，无需适配 AAPM。"
            )
        }
    }
}

// =============================================================
// AAPMStatusCard — AAPM 状态卡片
// =============================================================
/**
 * AAPM Status Card / AAPM 状态卡片
 *
 * Large card displaying the current AAPM status with icon and description.
 */
@Composable
private fun AAPMStatusCard(
    aapmState: AAPMState,
    statusColor: Color
) {
    val (icon, title, subtitle) = when (aapmState) {
        is AAPMState.NotSupported -> Triple(
            Icons.Rounded.Warning,
            "不支持 / Not Supported",
            "Android < 17.2，不支持 Advanced Protection Mode"
        )
        is AAPMState.Disabled -> Triple(
            Icons.Rounded.Shield,
            "已关闭 / Disabled",
            aapmState.reason ?: "AAPM 未启用"
        )
        is AAPMState.Enabled -> Triple(
            if (aapmState.isUserEnrolled) Icons.Rounded.Error else Icons.Rounded.CheckCircle,
            if (aapmState.isUserEnrolled) "已启用（已注册）/ Enabled (Enrolled)" else "已启用（未注册）/ Enabled (Not Enrolled)",
            if (aapmState.isUserEnrolled) "用户已注册 AAPM，部分服务权限可能被撤销" else "AAPM 已启用，当前服务暂未受影响"
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Status Icon / 状态图标
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(statusColor.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = statusColor
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Status Title / 状态标题
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = statusColor,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Status Subtitle / 状态副标题
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
        }
    }
}

// =============================================================
// AccessibilityToolStatusCard — isAccessibilityTool 状态卡片
// =============================================================
/**
 * Accessibility Tool Status Card / isAccessibilityTool 状态卡片
 */
@Composable
private fun AccessibilityToolStatusCard(
    isAccessibilityTool: Boolean
) {
    val color = if (isAccessibilityTool) Color(0xFF66BB6A) else Color(0xFFEF5350)
    val icon = if (isAccessibilityTool) Icons.Rounded.CheckCircle else Icons.Rounded.Error

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(32.dp)
            )

            Spacer(modifier = Modifier.size(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "isAccessibilityTool 标记",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = if (isAccessibilityTool) "已标记为辅助功能工具 / Marked as accessibility tool"
                           else "未标记 / Not marked",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }

            // Status Badge / 状态徽章
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(color.copy(alpha = 0.1f))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = if (isAccessibilityTool) "已标记" else "未标记",
                    style = MaterialTheme.typography.labelMedium,
                    color = color,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// =============================================================
// AffectedServicesCard — 受影响服务卡片
// =============================================================
/**
 * Affected Services Card / 受影响服务卡片
 */
@Composable
private fun AffectedServicesCard(
    services: List<String>,
    enabledCount: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "受影响的 Accessibility Services",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (services.isEmpty()) {
                Text(
                    text = "暂无受影响的服务 / No affected services",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            } else {
                services.forEach { service ->
                    Text(
                        text = "• $service",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Text(
                    text = "启用中: $enabledCount",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color(0xFFEF5350)
                )
            }
        }
    }
}

// =============================================================
// RiskSummaryCard — 风险摘要卡片
// =============================================================
/**
 * Risk Summary Card / 风险摘要卡片
 */
@Composable
private fun RiskSummaryCard(
    highRiskCount: Int,
    mediumRiskCount: Int,
    lowRiskCount: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "风险摘要 / Risk Summary",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                RiskCountItem(
                    label = "高风险",
                    count = highRiskCount,
                    color = Color(0xFFFF5252)
                )
                RiskCountItem(
                    label = "中风险",
                    count = mediumRiskCount,
                    color = Color(0xFFFFA726)
                )
                RiskCountItem(
                    label = "低风险",
                    count = lowRiskCount,
                    color = Color(0xFF66BB6A)
                )
            }
        }
    }
}

// =============================================================
// RiskCountItem — 风险数量项
// =============================================================
@Composable
private fun RiskCountItem(
    label: String,
    count: Int,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
    }
}

// =============================================================
// ActionButtons — 操作按钮
// =============================================================
@Composable
private fun ActionButtons(
    isScanning: Boolean,
    onCheckStatus: () -> Unit,
    onScanServices: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Button(
            onClick = onCheckStatus,
            modifier = Modifier.weight(1f),
            enabled = !isScanning
        ) {
            Text("检查状态")
        }

        Button(
            onClick = onScanServices,
            modifier = Modifier.weight(1f),
            enabled = !isScanning
        ) {
            if (isScanning) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text("扫描服务")
            }
        }
    }
}

// =============================================================
// NoteCard — 提示卡片
// =============================================================
@Composable
private fun NoteCard(message: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1A1A1A)
        )
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            modifier = Modifier.padding(16.dp),
            textAlign = TextAlign.Center
        )
    }
}
