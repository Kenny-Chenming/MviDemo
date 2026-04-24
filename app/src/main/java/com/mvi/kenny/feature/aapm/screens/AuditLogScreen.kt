package com.mvi.kenny.feature.aapm.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Error
import androidx.compose.material.icons.rounded.FileDownload
import androidx.compose.material.icons.rounded.FilterList
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.mvi.kenny.feature.aapm.AAPMDashboardIntent
import com.mvi.kenny.feature.aapm.AAPMDashboardState
import com.mvi.kenny.feature.aapm.AuditLogEntry
import com.mvi.kenny.feature.aapm.ExportFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// =============================================================
// AuditLogScreen — 审计日志页面
// =============================================================
/**
 * Audit Log Screen / 审计日志页面
 *
 * Displays Accessibility API access records with:
 * - Time, target app, operation type, service name
 * - Log export functionality (JSON/CSV)
 * - Filtering and search
 *
 * @param state Current dashboard state / 当前仪表板状态
 * @param onIntent Intent handler / 意图处理器
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuditLogScreen(
    state: AAPMDashboardState,
    onIntent: (AAPMDashboardIntent) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var showExportMenu by remember { mutableStateOf(false) }
    var showCopyDialog by remember { mutableStateOf<AuditLogEntry?>(null) }
    var selectedFilter by remember { mutableStateOf<FilterOption>(FilterOption.ALL) }

    val filteredLogs = state.auditLogs.filter { log ->
        val matchesSearch = searchQuery.isEmpty() ||
                log.targetApp.contains(searchQuery, ignoreCase = true) ||
                log.operationType.contains(searchQuery, ignoreCase = true) ||
                log.serviceName.contains(searchQuery, ignoreCase = true)

        val matchesFilter = when (selectedFilter) {
            FilterOption.ALL -> true
            FilterOption.COMPLIANT -> log.isCompliant
            FilterOption.NON_COMPLIANT -> !log.isCompliant
        }

        matchesSearch && matchesFilter
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Search and Filter Bar / 搜索和过滤栏
        SearchFilterBar(
            searchQuery = searchQuery,
            onSearchQueryChange = { searchQuery = it },
            selectedFilter = selectedFilter,
            onFilterChange = { selectedFilter = it },
            showExportMenu = showExportMenu,
            onExportMenuToggle = { showExportMenu = !showExportMenu },
            onExport = { format ->
                onIntent(AAPMDashboardIntent.ExportAuditLogs(format))
                showExportMenu = false
            }
        )

        // Privacy Notice / 隐私注意事项
        PrivacyNotice()

        // Log List / 日志列表
        if (filteredLogs.isEmpty()) {
            EmptyAuditLog()
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item { Spacer(modifier = Modifier.height(4.dp)) }

                items(filteredLogs, key = { it.id }) { log ->
                    AuditLogItem(
                        log = log,
                        onLongPress = { showCopyDialog = log }
                    )
                }

                item { Spacer(modifier = Modifier.height(8.dp)) }
            }
        }
    }

    // Copy Dialog / 复制对话框
    showCopyDialog?.let { log ->
        AlertDialog(
            onDismissRequest = { showCopyDialog = null },
            title = { Text("复制日志条目") },
            text = {
                Column {
                    Text("服务: ${log.serviceName}")
                    Text("目标 App: ${log.targetApp}")
                    Text("操作类型: ${log.operationType}")
                    Text("合规: ${if (log.isCompliant) "是" else "否"}")
                    Text("时间: ${formatTimestamp(log.timestamp)}")
                }
            },
            confirmButton = {
                TextButton(onClick = { showCopyDialog = null }) {
                    Text("关闭")
                }
            }
        )
    }
}

// =============================================================
// FilterOption — 过滤选项
// =============================================================
private enum class FilterOption(val label: String) {
    ALL("全部 / All"),
    COMPLIANT("合规 / Compliant"),
    NON_COMPLIANT("不合规 / Non-Compliant")
}

// =============================================================
// SearchFilterBar — 搜索过滤栏
// =============================================================
@Composable
private fun SearchFilterBar(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    selectedFilter: FilterOption,
    onFilterChange: (FilterOption) -> Unit,
    showExportMenu: Boolean,
    onExportMenuToggle: () -> Unit,
    onExport: (ExportFormat) -> Unit
) {
    var filterExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
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
            // Search Field / 搜索字段
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("搜索日志…") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Rounded.Search,
                        contentDescription = null
                    )
                },
                singleLine = true,
                shape = RoundedCornerShape(8.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Filter and Export Row / 过滤和导出行
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Filter Dropdown / 过滤下拉菜单
                Box {
                    TextButton(onClick = { filterExpanded = true }) {
                        Icon(
                            imageVector = Icons.Rounded.FilterList,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(selectedFilter.label)
                    }

                    DropdownMenu(
                        expanded = filterExpanded,
                        onDismissRequest = { filterExpanded = false }
                    ) {
                        FilterOption.values().forEach { filter ->
                            DropdownMenuItem(
                                text = { Text(filter.label) },
                                onClick = {
                                    onFilterChange(filter)
                                    filterExpanded = false
                                }
                            )
                        }
                    }
                }

                // Export Button / 导出按钮
                Box {
                    IconButton(onClick = onExportMenuToggle) {
                        Icon(
                            imageVector = Icons.Rounded.FileDownload,
                            contentDescription = "Export"
                        )
                    }

                    DropdownMenu(
                        expanded = showExportMenu,
                        onDismissRequest = { onExportMenuToggle() }
                    ) {
                        DropdownMenuItem(
                            text = { Text("导出 JSON") },
                            onClick = { onExport(ExportFormat.JSON) }
                        )
                        DropdownMenuItem(
                            text = { Text("导出 CSV") },
                            onClick = { onExport(ExportFormat.CSV) }
                        )
                    }
                }
            }
        }
    }
}

// =============================================================
// PrivacyNotice — 隐私注意事项
// =============================================================
@Composable
private fun PrivacyNotice() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFE3F2FD) // Light blue / 浅蓝色
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "ℹ️",
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "记录 Accessibility API 使用情况涉及隐私合规（GDPR/CCPA）。请确保已告知用户并提供关闭选项。",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF1565C0)
            )
        }
    }
}

// =============================================================
// AuditLogItem — 审计日志条目
// =============================================================
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun AuditLogItem(
    log: AuditLogEntry,
    onLongPress: () -> Unit
) {
    val complianceColor = if (log.isCompliant) Color(0xFF66BB6A) else Color(0xFFEF5350)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = { },
                onLongClick = { onLongPress() }
            ),
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
            // Compliance Icon / 合规图标
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(complianceColor.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (log.isCompliant) Icons.Rounded.CheckCircle else Icons.Rounded.Error,
                    contentDescription = null,
                    tint = complianceColor,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Log Details / 日志详情
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = log.targetApp,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    Text(
                        text = formatTimestamp(log.timestamp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "${log.operationType} · ${log.serviceName}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Compliance Badge / 合规徽章
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(complianceColor.copy(alpha = 0.1f))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = if (log.isCompliant) "合规" else "违规",
                    style = MaterialTheme.typography.labelSmall,
                    color = complianceColor,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// =============================================================
// EmptyAuditLog — 空审计日志
// =============================================================
@Composable
private fun EmptyAuditLog() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Rounded.FilterList,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "暂无审计日志",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
            Text(
                text = "Accessibility API 访问记录将显示在这里",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
            )
        }
    }
}

// =============================================================
// formatTimestamp — 格式化时间戳
// =============================================================
private fun formatTimestamp(timestamp: Long): String {
    val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
    return sdf.format(Date(timestamp))
}
