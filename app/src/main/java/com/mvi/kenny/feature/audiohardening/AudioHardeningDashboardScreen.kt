package com.mvi.kenny.feature.audiohardening

// ================================================================
// AudioHardeningDashboardScreen — Android 17 Background Audio Hardening 主仪表盘
// ================================================================
// Dashboard for the Background Audio Hardening toolkit.
//
// PRD-145: Android 17 Background Audio Hardening 合规检测与 Foreground Service 迁移工具包
// Design Reference: memory/agency/designs/PRD-145-Android-17-Background-Audio-Hardening-合规检测与-Foreground-Service-迁移工具包.md
//
// 功能：
// - 顶部 AppBar + 合规状态徽章
// - 设备信息卡片 + 合规摘要卡片
// - 3×3 工具网格（9 个工具入口卡片）
// - 底部快捷操作栏
// ================================================================

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mvi.kenny.feature.audiohardening.screens.*

// ================================================================
// Color Constants（颜色常量）
// ================================================================
private val ColorPass = Color(0xFF4CAF50)
private val ColorWarning = Color(0xFFFFC107)
private val ColorDanger = Color(0xFFF44336)
private val ColorNotChecked = Color(0xFF9E9E9E)
private val ColorScanning = Color(0xFF2196F3)
private val ColorCodeBackground = Color(0xFF1E1E1E)

// ================================================================
// AudioHardeningDashboardScreen — 主入口
// ================================================================

/**
 * ============================================================
 * AudioHardeningDashboardScreen — 主仪表盘
 * ============================================================
 * 9 个工具入口以网格展示，点击进入对应工具详情页。
 *
 * @param viewModel Dashboard ViewModel
 * @param onNavigateToTool 导航到工具详情页的回调
 * @param modifier Compose modifier
 */
@Composable
fun AudioHardeningDashboardScreen(
    viewModel: AudioHardeningViewModel = viewModel(),
    onNavigateToTool: (AudioToolId) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is AudioHardeningDashboardEffect.NavigateToTool -> onNavigateToTool(effect.toolId)
                is AudioHardeningDashboardEffect.ShowSnackbar -> { /* handled externally */ }
                is AudioHardeningDashboardEffect.ShareReport -> { /* handled externally */ }
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        // ── 合规状态徽章卡片 ──
        ComplianceStatusCard(summary = state.complianceSummary)

        Spacer(modifier = Modifier.height(12.dp))

        // ── 设备信息卡片 ──
        DeviceInfoCard(deviceInfo = state.deviceInfo)

        Spacer(modifier = Modifier.height(16.dp))

        // ── 工具网格标题 ──
        Text(
            text = "工具箱（9）",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // ── 3×3 工具网格 ──
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(AudioToolId.entries) { toolId ->
                ToolEntryCard(
                    toolId = toolId,
                    status = state.toolStatuses[toolId] ?: ToolStatus.NotChecked,
                    isScanning = state.isScanning,
                    onClick = {
                        viewModel.sendIntent(AudioHardeningDashboardIntent.OpenTool(toolId))
                    }
                )
            }
        }

        // ── 底部快捷操作栏 ──
        BottomActionBar(
            isScanning = state.isScanning,
            scanProgress = state.scanProgress,
            onRunFullScan = { viewModel.sendIntent(AudioHardeningDashboardIntent.RunFullScan) },
            onExportReport = { viewModel.sendIntent(AudioHardeningDashboardIntent.ExportReport) }
        )

        Spacer(modifier = Modifier.height(16.dp))
    }
}

// ================================================================
// 工具入口卡片 — ToolEntryCard
// ================================================================

/**
 * ============================================================
 * ToolEntryCard — 工具入口卡片
 * ============================================================
 * 显示工具图标、名称和状态标签，点击触发缩放动画并导航。
 *
 * @param toolId 工具 ID
 * @param status 当前状态
 * @param isScanning 是否正在扫描
 * @param onClick 点击回调
 */
@Composable
private fun ToolEntryCard(
    toolId: AudioToolId,
    status: ToolStatus,
    isScanning: Boolean,
    onClick: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = tween(100),
        label = "scale"
    )

    val statusColor = when (status) {
        ToolStatus.Pass -> ColorPass
        ToolStatus.Warning -> ColorWarning
        ToolStatus.NotChecked -> ColorNotChecked
        ToolStatus.Scanning -> ColorScanning
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .clickable {
                isPressed = true
                onClick()
            },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 工具图标
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(statusColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = getToolIcon(toolId),
                    contentDescription = toolId.titleCn,
                    tint = statusColor,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 工具名称
            Text(
                text = toolId.titleCn,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                maxLines = 2
            )

            Spacer(modifier = Modifier.height(4.dp))

            // 状态标签
            StatusBadge(status = status, color = statusColor)
        }
    }
}

// ============================================================
// 状态徽章 — StatusBadge
// ============================================================

@Composable
private fun StatusBadge(status: ToolStatus, color: Color) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = color.copy(alpha = 0.15f)
    ) {
        Text(
            text = "${status.emoji} ${status.displayName}",
            color = color,
            fontSize = 10.sp,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}

// ================================================================
// 合规状态卡片 — ComplianceStatusCard
// ================================================================

@Composable
private fun ComplianceStatusCard(summary: ComplianceSummary) {
    val statusColor = when (summary.overallStatus) {
        ToolStatus.Pass -> ColorPass
        ToolStatus.Warning -> ColorWarning
        else -> ColorNotChecked
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = statusColor.copy(alpha = 0.1f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = when (summary.overallStatus) {
                    ToolStatus.Pass -> Icons.Default.CheckCircle
                    ToolStatus.Warning -> Icons.Default.Warning
                    else -> Icons.Default.Search
                },
                contentDescription = "合规状态",
                tint = statusColor,
                modifier = Modifier.size(48.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = when (summary.overallStatus) {
                        ToolStatus.Pass -> "✅ 合规"
                        ToolStatus.Warning -> "⚠️ 有风险"
                        else -> "🔍 待检测"
                    },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = statusColor
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "通过 ${summary.passCount} / 风险 ${summary.warningCount} / 待检测 ${summary.notCheckedCount}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
        }
    }
}

// ================================================================
// 设备信息卡片 — DeviceInfoCard
// ================================================================

@Composable
private fun DeviceInfoCard(deviceInfo: DeviceInfo) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.PhoneAndroid,
                contentDescription = "设备信息",
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "设备信息",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Android ${deviceInfo.androidVersion} / ${deviceInfo.deviceModel}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                Text(
                    text = if (deviceInfo.isBackgroundAudioHardeningSupported)
                        "✅ 支持 Background Audio Hardening"
                    else
                        "❌ 不支持或低于 Android 17",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (deviceInfo.isBackgroundAudioHardeningSupported) ColorPass else ColorDanger
                )
            }
        }
    }
}

// ================================================================
// 底部操作栏 — BottomActionBar
// ================================================================

@Composable
private fun BottomActionBar(
    isScanning: Boolean,
    scanProgress: Float,
    onRunFullScan: () -> Unit,
    onExportReport: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 0.dp, bottomEnd = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            if (isScanning) {
                LinearProgressIndicator(
                    progress = { scanProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = ColorScanning
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onRunFullScan,
                    modifier = Modifier.weight(1f),
                    enabled = !isScanning,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(Icons.Default.Radar, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (isScanning) "扫描中…" else "全量扫描")
                }

                OutlinedButton(
                    onClick = onExportReport,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("导出报告")
                }
            }
        }
    }
}

// ================================================================
// 工具图标映射
// ================================================================

private fun getToolIcon(toolId: AudioToolId): ImageVector {
    return when (toolId) {
        AudioToolId.Scanner -> Icons.Default.Radar
        AudioToolId.Monitor -> Icons.Default.MonitorHeart
        AudioToolId.FGSGenerator -> Icons.Default.Build
        AudioToolId.MediaSessionBinder -> Icons.Default.Link
        AudioToolId.AudioComplianceCI -> Icons.Default.CheckCircle
        AudioToolId.AudioFocusDegradation -> Icons.Default.TrendingDown
        AudioToolId.AudioRegressionTest -> Icons.Default.Science
        AudioToolId.AudioDebugPanel -> Icons.Default.BugReport
        AudioToolId.AudioFallbackPath -> Icons.Default.Route
    }
}
