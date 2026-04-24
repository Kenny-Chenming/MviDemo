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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.FactCheck
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mvi.kenny.feature.aapm.AAPMDashboardIntent
import com.mvi.kenny.feature.aapm.AAPMDashboardState
import com.mvi.kenny.feature.aapm.AuditLogEntry
import com.mvi.kenny.feature.aapm.ExportFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// =============================================================
// AuditLogScreen — 审计日志屏幕
// =============================================================
/**
 * Audit Log Screen / 审计日志屏幕
 *
 * Displays audit trail of Accessibility API access.
 * Allows filtering and export of audit logs.
 *
 * @param state Current dashboard state / 当前仪表盘状态
 * @param onIntent Intent callback to ViewModel / Intent 回调到 ViewModel
 */
@Composable
fun AuditLogScreen(
    state: AAPMDashboardState,
    onIntent: (AAPMDashboardIntent) -> Unit
) {
    var showExportMenu by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        // =============================================================
        // Header / 标题区
        // =============================================================
        Text(
            text = "审计日志",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Accessibility API 访问审计追踪",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        // =============================================================
        // Export Section / 导出区域
        // =============================================================
        ExportSection(
            logs = state.auditLogs,
            showExportMenu = showExportMenu,
            onShowExportMenu = { showExportMenu = it },
            onExport = { format ->
                onIntent(AAPMDashboardIntent.ExportAuditLogs(format))
                showExportMenu = false
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // =============================================================
        // Logs List / 日志列表
        // =============================================================
        if (state.auditLogs.isEmpty()) {
            EmptyLogsView()
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(state.auditLogs) { log ->
                    AuditLogCard(log = log)
                }
            }
        }
    }
}

// =============================================================
// ExportSection — 导出区域
// =============================================================
/**
 * Export Section / 导出区域
 *
 * @param logs Current logs / 当前日志
 * @param showExportMenu Whether dropdown is visible / 下拉菜单是否可见
 * @param onShowExportMenu Callback to toggle menu / 切换菜单回调
 * @param onExport Export callback / 导出回调
 */
@Composable
private fun ExportSection(
    logs: List<AuditLogEntry>,
    showExportMenu: Boolean,
    onShowExportMenu: (Boolean) -> Unit,
    onExport: (ExportFormat) -> Unit
) {
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
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "导出审计日志",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "${logs.size} 条记录 / ${logs.count { it.isCompliant }} 合规",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Box {
                OutlinedButton(onClick = { onShowExportMenu(true) }) {
                    Icon(
                        imageVector = Icons.Default.Download,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("导出")
                }

                DropdownMenu(
                    expanded = showExportMenu,
                    onDismissRequest = { onShowExportMenu(false) }
                ) {
                    ExportFormat.entries.forEach { format ->
                        DropdownMenuItem(
                            text = { Text("${format.label} (.${format.extension})") },
                            onClick = { onExport(format) }
                        )
                    }
                }
            }
        }
    }
}

// =============================================================
// AuditLogCard — 审计日志卡片
// =============================================================
/**
 * Audit Log Card / 审计日志卡片
 *
 * @param log Audit log entry / 审计日志条目
 */
@Composable
private fun AuditLogCard(log: AuditLogEntry) {
    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Compliance indicator / 合规指示器
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        if (log.isCompliant) Color(0xFF66BB6A).copy(alpha = 0.15f)
                        else Color(0xFFFF5252).copy(alpha = 0.15f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (log.isCompliant) Icons.Default.CheckCircle else Icons.Default.Error,
                    contentDescription = null,
                    tint = if (log.isCompliant) Color(0xFF66BB6A) else Color(0xFFFF5252),
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = log.targetApp,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = log.operationType,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = log.serviceName,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = dateFormat.format(Date(log.timestamp)),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// =============================================================
// EmptyLogsView — 空日志视图
// =============================================================
/**
 * Empty Logs View / 空日志视图
 */
@Composable
private fun EmptyLogsView() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.FactCheck,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "暂无审计日志",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "No audit logs available",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
