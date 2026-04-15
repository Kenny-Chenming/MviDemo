package com.mvi.kenny.feature.ottermcp

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AuditLogScreen(viewModel: AuditLogViewModel) {
    val uiState by viewModel.state.collectAsState()
    
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Audit Log", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Row { IconButton({ viewModel.sendIntent(AuditLogIntent.Refresh) }) { Icon(Icons.Default.Refresh, "Refresh") }; IconButton({ viewModel.sendIntent(AuditLogIntent.ExportLogs(ExportFormat.CSV)) }) { Icon(Icons.Default.Download, "Export") } }
        }
        Spacer(Modifier.height(16.dp))
        
        if (uiState.isLoading && uiState.logs.isEmpty()) Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        else if (uiState.logs.isEmpty()) Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("No logs found", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)) }
        else LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(uiState.logs, key = { it.id }) { log -> AuditLogItem(log, { viewModel.sendIntent(AuditLogIntent.SelectLog(log)) }) }
            if (uiState.hasMore) item { OutlinedButton({ viewModel.sendIntent(AuditLogIntent.LoadMore) }, Modifier.fillMaxWidth()) { if (uiState.isLoading) CircularProgressIndicator(Modifier.size(16.dp), strokeWidth = 2.dp) else Text("Load More") } }
        }
    }
    
    if (uiState.selectedCall != null) {
        ModalBottomSheet(onDismissRequest = { viewModel.sendIntent(AuditLogIntent.DismissDetail) }) {
            Column(Modifier.fillMaxWidth().padding(24.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Call Details", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    IconButton({ viewModel.sendIntent(AuditLogIntent.DismissDetail) }) { Icon(Icons.Default.Close, "Close") }
                }
                Spacer(Modifier.height(16.dp))
                DetailRow("Tool", uiState.selectedCall!!.toolName)
                DetailRow("Server", uiState.selectedCall!!.serverName)
                DetailRow("Time", formatFull(uiState.selectedCall!!.timestamp))
                DetailRow("Result", uiState.selectedCall!!.result)
                Spacer(Modifier.height(32.dp))
            }
        }
    }
}

@Composable
private fun AuditLogItem(log: McpToolCall, onClick: () -> Unit) {
    Card(Modifier.fillMaxWidth().clickable(onClick = onClick), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant), shape = RoundedCornerShape(8.dp)) {
        Row(Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) { Text(log.toolName, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium); Spacer(Modifier.width(8.dp)); Text(if (log.success) "Success" else "Failed", color = if (log.success) Color(0xFF4CAF50) else MaterialTheme.colorScheme.error) }
                Text(log.serverName, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(formatTs(log.timestamp), style = MaterialTheme.typography.labelSmall, color = Color(0xFF9E9E9E))
            }
            Text(log.params, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) { Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant); Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium) }
}

private fun formatTs(ts: Long): String = SimpleDateFormat("MM-dd HH:mm:ss", Locale.getDefault()).format(Date(ts))
private fun formatFull(ts: Long): String = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(ts))
