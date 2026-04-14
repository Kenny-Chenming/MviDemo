package com.mvi.kenny.feature.remotetoolkit.sub_screens

import com.mvi.kenny.feature.remotetoolkit.RemoteToolkitState
import com.mvi.kenny.feature.remotetoolkit.RemoteToolkitIntent
import com.mvi.kenny.feature.remotetoolkit.RemoteToolkitPage
import com.mvi.kenny.feature.remotetoolkit.RemoteToolkitEffect
import com.mvi.kenny.feature.remotetoolkit.RemoteToolkitColors


import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mvi.kenny.feature.remotetoolkit.Activity
import com.mvi.kenny.feature.remotetoolkit.QuickStats
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// =============================================================
// DashboardScreen — 工具包首页仪表盘
// Compose Remote Toolkit Dashboard / 工具包首页仪表盘
// =============================================================

@Composable
fun DashboardScreen(
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
        // Quick Stats Row / 快速统计行
        // ============================================================
        item {
            Text(
                text = "Quick Stats / 快速统计",
                style = MaterialTheme.typography.titleMedium,
                color = RemoteToolkitColors.OnSurface,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    StatCard(
                        title = "Compatibility Score",
                        value = "${state.quickStats.compatibilityScore}%",
                        subtitle = "序列化兼容性",
                        color = RemoteToolkitColors.Purple,
                        icon = Icons.Default.CheckCircle,
                        onClick = { onIntent(RemoteToolkitIntent.SelectPage(RemoteToolkitPage.SCANNER)) }
                    )
                }
                item {
                    StatCard(
                        title = "Serialized UIs",
                        value = "${state.quickStats.serializedUis}",
                        subtitle = "已序列化 UI",
                        color = RemoteToolkitColors.Teal,
                        icon = Icons.Default.BarChart,
                        onClick = { }
                    )
                }
                item {
                    StatCard(
                        title = "Active Versions",
                        value = "${state.quickStats.activeVersions}",
                        subtitle = "活跃版本",
                        color = RemoteToolkitColors.Info,
                        icon = Icons.Default.CloudUpload,
                        onClick = { onIntent(RemoteToolkitIntent.SelectPage(RemoteToolkitPage.VERSION)) }
                    )
                }
                item {
                    StatCard(
                        title = "AB Tests Running",
                        value = "${state.quickStats.abTestsRunning}",
                        subtitle = "运行中的 A/B 测试",
                        color = RemoteToolkitColors.Warning,
                        icon = Icons.Default.BarChart,
                        onClick = { onIntent(RemoteToolkitIntent.SelectPage(RemoteToolkitPage.AB_TESTING)) }
                    )
                }
            }
        }

        // ============================================================
        // Recent Activity Timeline / 最近活动时间线
        // ============================================================
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Recent Activity / 最近活动",
                style = MaterialTheme.typography.titleMedium,
                color = RemoteToolkitColors.OnSurface,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = RemoteToolkitColors.Surface),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    state.recentActivities.forEach { activity ->
                        ActivityRow(activity = activity)
                    }
                    if (state.recentActivities.isEmpty()) {
                        Text(
                            text = "No recent activity / 暂无最近活动",
                            style = MaterialTheme.typography.bodyMedium,
                            color = RemoteToolkitColors.OnSurfaceVariant
                        )
                    }
                }
            }
        }

        // ============================================================
        // Quick Actions Grid / 快捷操作网格
        // ============================================================
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Quick Actions / 快捷操作",
                style = MaterialTheme.typography.titleMedium,
                color = RemoteToolkitColors.OnSurface,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    QuickActionCard(
                        title = "Run Compatibility Scan",
                        subtitle = "兼容性扫描",
                        icon = Icons.Default.Search,
                        color = RemoteToolkitColors.Purple,
                        onClick = { onIntent(RemoteToolkitIntent.SelectPage(RemoteToolkitPage.SCANNER)) },
                        modifier = Modifier.weight(1f)
                    )
                    QuickActionCard(
                        title = "Start Debug Server",
                        subtitle = "启动调试服务器",
                        icon = Icons.Default.PlayArrow,
                        color = RemoteToolkitColors.Teal,
                        onClick = { onIntent(RemoteToolkitIntent.SelectPage(RemoteToolkitPage.DEBUG)) },
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    QuickActionCard(
                        title = "Deploy New UI Version",
                        subtitle = "部署新 UI 版本",
                        icon = Icons.Default.CloudUpload,
                        color = RemoteToolkitColors.Info,
                        onClick = { onIntent(RemoteToolkitIntent.SelectPage(RemoteToolkitPage.VERSION)) },
                        modifier = Modifier.weight(1f)
                    )
                    QuickActionCard(
                        title = "View AB Test Results",
                        subtitle = "查看 A/B 测试结果",
                        icon = Icons.Default.BarChart,
                        color = RemoteToolkitColors.Warning,
                        onClick = { onIntent(RemoteToolkitIntent.SelectPage(RemoteToolkitPage.AB_TESTING)) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // ============================================================
        // Architecture Overview / 架构概览
        // ============================================================
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Architecture Overview / 架构概览",
                style = MaterialTheme.typography.titleMedium,
                color = RemoteToolkitColors.OnSurface,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = RemoteToolkitColors.Surface),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    ArchitectureFlow()
                }
            }
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}

// =============================================================
// StatCard — 统计卡片
// =============================================================
@Composable
private fun StatCard(
    title: String,
    value: String,
    subtitle: String,
    color: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(160.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = RemoteToolkitColors.Surface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
                Text(
                    text = value,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = RemoteToolkitColors.OnSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = RemoteToolkitColors.OnSurfaceVariant
            )
        }
    }
}

// =============================================================
// ActivityRow — 活动行
// =============================================================
@Composable
private fun ActivityRow(activity: Activity) {
    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(RemoteToolkitColors.Purple)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = activity.description,
                style = MaterialTheme.typography.bodyMedium,
                color = RemoteToolkitColors.OnSurface
            )
            Text(
                text = timeFormat.format(Date(activity.timestamp)),
                style = MaterialTheme.typography.labelSmall,
                color = RemoteToolkitColors.OnSurfaceVariant
            )
        }
    }
}

// =============================================================
// QuickActionCard — 快捷操作卡片
// =============================================================
@Composable
private fun QuickActionCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = RemoteToolkitColors.Surface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(color.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = RemoteToolkitColors.OnSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = RemoteToolkitColors.OnSurfaceVariant
                )
            }
        }
    }
}

// =============================================================
// ArchitectureFlow — 架构流程图（Canvas 绘制）
// =============================================================
@Composable
private fun ArchitectureFlow() {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            FlowNode(label = "Compose UI", color = RemoteToolkitColors.Purple)
            Arrow()
            FlowNode(label = "Binary", color = RemoteToolkitColors.Teal)
            Arrow()
            FlowNode(label = "Transport", color = RemoteToolkitColors.Info)
            Arrow()
            FlowNode(label = "Native Render", color = RemoteToolkitColors.Warning)
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "UI Definition → Serialize → Transfer → Render",
            style = MaterialTheme.typography.labelSmall,
            color = RemoteToolkitColors.OnSurfaceVariant,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
    }
}

// =============================================================
// FlowNode — 流程节点
// =============================================================
@Composable
private fun FlowNode(label: String, color: Color) {
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
            color = RemoteToolkitColors.OnSurface
        )
    }
}

// =============================================================
// Arrow — 箭头
// =============================================================
@Composable
private fun Arrow() {
    Text(
        text = "→",
        color = RemoteToolkitColors.OnSurfaceVariant,
        fontSize = 20.sp
    )
}
