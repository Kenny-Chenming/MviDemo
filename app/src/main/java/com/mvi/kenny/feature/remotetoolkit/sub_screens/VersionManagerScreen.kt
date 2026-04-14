package com.mvi.kenny.feature.remotetoolkit.sub_screens

import com.mvi.kenny.feature.remotetoolkit.RemoteToolkitState
import com.mvi.kenny.feature.remotetoolkit.RemoteToolkitIntent
import com.mvi.kenny.feature.remotetoolkit.RemoteToolkitEffect
import com.mvi.kenny.feature.remotetoolkit.RemoteToolkitColors


import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import com.mvi.kenny.feature.remotetoolkit.UIVersion
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Redo
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// =============================================================
// VersionManagerScreen — UI 版本管理与回滚页面
// Version Manager / 版本管理器
// =============================================================

private val sampleVersions = listOf(
    UIVersion("v3", "v3", System.currentTimeMillis() - 15 * 60 * 1000, "99.9%", "0.02%", 60, true),
    UIVersion("v2", "v2", System.currentTimeMillis() - 2 * 60 * 60 * 1000, "99.7%", "0.05%", 30, false),
    UIVersion("v1", "v1", System.currentTimeMillis() - 24 * 60 * 60 * 1000, "98.5%", "0.12%", 10, false)
)

@Composable
fun VersionManagerScreen(
    state: RemoteToolkitState,
    onIntent: (RemoteToolkitIntent) -> Unit
) {
    val versions = sampleVersions
    val activeVersion = versions.firstOrNull { it.isActive }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(RemoteToolkitColors.Background)
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "UI Version Manager",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = RemoteToolkitColors.OnSurface
                )
                Button(
                    onClick = { },
                    colors = ButtonDefaults.buttonColors(containerColor = RemoteToolkitColors.Purple)
                ) {
                    Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Deploy New Version")
                }
            }
        }

        // ============================================================
        // Current Active Version / 当前活跃版本
        // ============================================================
        item {
            if (activeVersion != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = RemoteToolkitColors.PurpleContainer.copy(alpha = 0.3f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Current Active Version",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = RemoteToolkitColors.OnSurfaceVariant
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(RemoteToolkitColors.Purple)
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "ACTIVE",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = RemoteToolkitColors.OnPurple,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = activeVersion.version,
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = RemoteToolkitColors.Purple
                            )
                            Text(
                                text = "Deployed ${formatRelativeTime(activeVersion.deployedAt)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = RemoteToolkitColors.OnSurfaceVariant
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "Uptime: ${activeVersion.uptime}",
                                style = MaterialTheme.typography.bodySmall,
                                color = RemoteToolkitColors.PassColor
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Error Rate: ${activeVersion.errorRate}",
                                style = MaterialTheme.typography.bodySmall,
                                color = RemoteToolkitColors.OnSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // ============================================================
        // Version History Timeline / 版本历史时间线
        // ============================================================
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.History, contentDescription = null, tint = RemoteToolkitColors.OnSurfaceVariant, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Version History / 版本历史",
                    style = MaterialTheme.typography.titleMedium,
                    color = RemoteToolkitColors.OnSurface
                )
            }
        }

        items(versions, key = { it.id }) { version ->
            VersionTimelineCard(
                version = version,
                isFirst = version.id == versions.first().id,
                isLast = version.id == versions.last().id,
                onRollback = { onIntent(RemoteToolkitIntent.RollbackVersion(version.id)) }
            )
        }

        // ============================================================
        // Traffic Split Config / 流量分配配置
        // ============================================================
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Traffic Split Config / 流量分配配置",
                style = MaterialTheme.typography.titleMedium,
                color = RemoteToolkitColors.OnSurface
            )
        }

        item {
            TrafficSplitCard(versions = versions)
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}

// =============================================================
// VersionTimelineCard — 版本时间线卡片
// =============================================================
@Composable
private fun VersionTimelineCard(
    version: UIVersion,
    isFirst: Boolean,
    isLast: Boolean,
    onRollback: () -> Unit
) {
    val timeFormat = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())

    Row(modifier = Modifier.fillMaxWidth()) {
        // Timeline line / 时间线
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(40.dp)
        ) {
            if (!isFirst) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(20.dp)
                        .background(RemoteToolkitColors.Outline)
                )
            }
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        if (version.isActive) RemoteToolkitColors.Purple
                        else RemoteToolkitColors.Outline
                    )
            )
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(60.dp)
                        .background(RemoteToolkitColors.Outline)
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (version.isActive) RemoteToolkitColors.Surface
                                else RemoteToolkitColors.Surface.copy(alpha = 0.7f)
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = version.version,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (version.isActive) RemoteToolkitColors.Purple else RemoteToolkitColors.OnSurface
                        )
                        if (version.isActive) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(RemoteToolkitColors.Purple)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "ACTIVE",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = RemoteToolkitColors.OnPurple,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                    Text(
                        text = "${version.trafficPercent}% traffic",
                        style = MaterialTheme.typography.labelMedium,
                        color = RemoteToolkitColors.OnSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Traffic bar / 流量条
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                ) {
                    drawRoundRect(
                        color = RemoteToolkitColors.SurfaceVariant,
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(3.dp.toPx())
                    )
                    drawRoundRect(
                        color = if (version.isActive) RemoteToolkitColors.Purple else RemoteToolkitColors.Outline,
                        size = Size(size.width * (version.trafficPercent / 100f), size.height),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(3.dp.toPx())
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Deployed: ${timeFormat.format(Date(version.deployedAt))}",
                            style = MaterialTheme.typography.labelSmall,
                            color = RemoteToolkitColors.OnSurfaceVariant
                        )
                        Text(
                            text = "Uptime: ${version.uptime} · Error: ${version.errorRate}",
                            style = MaterialTheme.typography.labelSmall,
                            color = RemoteToolkitColors.OnSurfaceVariant
                        )
                    }
                    if (!version.isActive) {
                        OutlinedButton(
                            onClick = onRollback,
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = RemoteToolkitColors.WarningColor)
                        ) {
                            Icon(Icons.Default.Undo, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Rollback", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
        }
    }
}

// =============================================================
// TrafficSplitCard — 流量分配卡片
// =============================================================
@Composable
private fun TrafficSplitCard(versions: List<UIVersion>) {
    val sliderValues = remember {
        mutableStateMapOf<String, Float>().apply {
            versions.forEach { put(it.id, it.trafficPercent.toFloat()) }
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = RemoteToolkitColors.Surface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            versions.forEach { version ->
                Column(modifier = Modifier.padding(vertical = 4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = version.version,
                            style = MaterialTheme.typography.labelMedium,
                            color = RemoteToolkitColors.OnSurface
                        )
                        Text(
                            text = "${sliderValues[version.id]?.toInt() ?: version.trafficPercent}%",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = RemoteToolkitColors.Purple
                        )
                    }
                    Slider(
                        value = sliderValues[version.id] ?: version.trafficPercent.toFloat(),
                        onValueChange = { sliderValues[version.id] = it },
                        valueRange = 0f..100f,
                        colors = SliderDefaults.colors(
                            thumbColor = RemoteToolkitColors.Purple,
                            activeTrackColor = RemoteToolkitColors.Purple,
                            inactiveTrackColor = RemoteToolkitColors.SurfaceVariant
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = {
                    val split = sliderValues.mapValues { it.value.toInt() }
                    // onIntent(RemoteToolkitIntent.UpdateTrafficSplit(split))
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = RemoteToolkitColors.Purple)
            ) {
                Text("Apply Traffic Split")
            }
        }
    }
}

// =============================================================
// formatRelativeTime — 相对时间格式化
// =============================================================
private fun formatRelativeTime(timestamp: Long): String {
    val diff = System.currentTimeMillis() - timestamp
    return when {
        diff < 60 * 1000 -> "just now"
        diff < 60 * 60 * 1000 -> "${diff / (60 * 1000)} min ago"
        diff < 24 * 60 * 60 * 1000 -> "${diff / (60 * 60 * 1000)} hours ago"
        else -> "${diff / (24 * 60 * 60 * 1000)} days ago"
    }
}
