package com.mvi.kenny.feature.ottermcp

import androidx.compose.foundation.background
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun TemplateGalleryScreen(viewModel: TemplateGalleryViewModel) {
    val uiState by viewModel.state.collectAsState()
    var selected by remember { mutableStateOf<McpTemplate?>(null) }
    
    Column(modifier = Modifier.fillMaxSize()) {
        ScrollableTabRow(selectedTabIndex = TemplateCategory.entries.indexOf(uiState.selectedTab), edgePadding = 16.dp, containerColor = MaterialTheme.colorScheme.surface) {
            TemplateCategory.entries.forEach { cat ->
                val isSelected = cat == uiState.selectedTab
                Tab(isSelected, { viewModel.sendIntent(TemplateGalleryIntent.SelectTab(cat)) }) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(16.dp)) {
                        Icon(getCatIcon(cat), null, Modifier.size(16.dp), tint = if (isSelected) getCatColor(cat) else MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.width(4.dp))
                        Text(getCatName(cat), color = if (isSelected) getCatColor(cat) else MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
        
        val filtered = uiState.templates.filter { it.category == uiState.selectedTab }
        
        if (uiState.isLoading) Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        else if (filtered.isEmpty()) Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("No templates", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)) }
        else LazyColumn(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) { items(filtered) { t -> TemplateCard(t, { selected = t }, { viewModel.sendIntent(TemplateGalleryIntent.ImportTemplate(t)) }) } }
    }
    
    if (selected != null) {
        ModalBottomSheet(onDismissRequest = { selected = null }) {
            Column(Modifier.fillMaxWidth().padding(24.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(48.dp).clip(RoundedCornerShape(12.dp)).background(getCatColor(selected!!.category).copy(alpha = 0.2f)), contentAlignment = Alignment.Center) { Icon(getCatIcon(selected!!.category), null, Modifier.size(28.dp), tint = getCatColor(selected!!.category)) }
                    Spacer(Modifier.width(16.dp))
                    Column { Text(selected!!.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold); Text(getCatName(selected!!.category), style = MaterialTheme.typography.bodyMedium, color = getCatColor(selected!!.category)) }
                }
                Spacer(Modifier.height(16.dp))
                Text("Description", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.height(4.dp))
                Text(selected!!.description, style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.height(24.dp))
                Button({ viewModel.sendIntent(TemplateGalleryIntent.ImportTemplate(selected!!)); selected = null }, Modifier.fillMaxWidth()) { Icon(Icons.Default.Cloud, null, Modifier.size(18.dp)); Spacer(Modifier.width(8.dp)); Text("Import Template") }
                Spacer(Modifier.height(32.dp))
            }
        }
    }
}

@Composable
private fun TemplateCard(template: McpTemplate, onClick: () -> Unit, onUse: () -> Unit) {
    val color = getCatColor(template.category)
    Card(Modifier.fillMaxWidth().clickable(onClick = onClick), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant), shape = RoundedCornerShape(12.dp)) {
        Column(Modifier.fillMaxWidth().padding(16.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                Row(Modifier.weight(1f)) {
                    Box(Modifier.size(40.dp).clip(RoundedCornerShape(8.dp)).background(color.copy(alpha = 0.2f)), contentAlignment = Alignment.Center) { Icon(getCatIcon(template.category), null, Modifier.size(24.dp), tint = color) }
                    Spacer(Modifier.width(12.dp))
                    Column { Text(template.name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium); Text(template.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2) }
                }
            }
            Spacer(Modifier.height(12.dp))
            Button(onUse, Modifier.fillMaxWidth()) { Text("Use Template") }
        }
    }
}

private fun getCatColor(c: TemplateCategory) = when (c) { TemplateCategory.DEVICE_INTERACTION -> Color(0xFF2196F3); TemplateCategory.BUILD_SYSTEM -> Color(0xFF9C27B0); TemplateCategory.PLAY_CONSOLE -> Color(0xFF009688); TemplateCategory.FIREBASE -> Color(0xFFFF9800); TemplateCategory.CRASHLYTICS -> Color(0xFFFF5722); TemplateCategory.CUSTOM -> Color(0xFF607D8B) }
private fun getCatIcon(c: TemplateCategory): ImageVector = when (c) { TemplateCategory.DEVICE_INTERACTION -> Icons.Default.PhoneAndroid; TemplateCategory.BUILD_SYSTEM -> Icons.Default.Build; TemplateCategory.PLAY_CONSOLE -> Icons.Default.PlayArrow; TemplateCategory.FIREBASE -> Icons.Default.Analytics; TemplateCategory.CRASHLYTICS -> Icons.Default.BugReport; TemplateCategory.CUSTOM -> Icons.Default.Settings }
private fun getCatName(c: TemplateCategory) = when (c) { TemplateCategory.DEVICE_INTERACTION -> "Device"; TemplateCategory.BUILD_SYSTEM -> "Build"; TemplateCategory.PLAY_CONSOLE -> "Play"; TemplateCategory.FIREBASE -> "Firebase"; TemplateCategory.CRASHLYTICS -> "Crashlytics"; TemplateCategory.CUSTOM -> "Custom" }
