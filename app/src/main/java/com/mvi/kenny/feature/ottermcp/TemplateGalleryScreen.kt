package com.mvi.kenny.feature.ottermcp

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.Architecture
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

/**
 * Template Gallery Screen / 模板库屏幕
 * Browse and import MCP Server templates / 浏览和导入 MCP Server 模板
 */
@Composable
fun TemplateGalleryScreen(
    state: TemplateGalleryState,
    onIntent: (TemplateGalleryIntent) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().padding(0.dp)) {
        // Category tabs / 类别 Tab
        ScrollableTabRow(
            selectedTabIndex = TemplateCategory.entries.indexOf(state.selectedTab),
            modifier = Modifier.padding(0.dp),
            edgePadding = 16.dp
        ) {
            TemplateCategory.entries.forEach { category ->
                Tab(
                    selected = state.selectedTab == category,
                    onClick = { onIntent(TemplateGalleryIntent.SelectTab(category)) },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(category.color)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                when (category) {
                                    TemplateCategory.DEVICE_INTERACTION -> "设备"
                                    TemplateCategory.BUILD_SYSTEM -> "构建"
                                    TemplateCategory.PLAY_CONSOLE -> "Play"
                                    TemplateCategory.FIREBASE -> "Firebase"
                                    TemplateCategory.CRASHLYTICS -> "Crash"
                                    TemplateCategory.CUSTOM -> "自定义"
                                }
                            )
                        }
                    }
                )
            }
        }

        // Template list / 模板列表
        if (state.isLoading && state.templates.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (state.templates.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "No templates in this category",
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(state.templates, key = { it.id }) { template ->
                    TemplateCard(template) {
                        onIntent(TemplateGalleryIntent.SelectTemplate(template))
                    }
                }
            }
        }
    }

    // Template detail bottom sheet / 模板详情底部弹窗
    state.selectedTemplate?.let { template ->
        ModalBottomSheet(
            onDismissRequest = { onIntent(TemplateGalleryIntent.DismissTemplateDetail) },
            sheetState = rememberModalBottomSheetState()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(template.category.color.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = getTemplateIcon(template.iconName),
                            contentDescription = null,
                            tint = template.category.color,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            template.name,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            template.category.label,
                            style = MaterialTheme.typography.bodySmall,
                            color = template.category.color
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "Description / 描述",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    template.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "Tools / 提供的工具",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(8.dp))
                template.tools.forEach { tool ->
                    Row(
                        modifier = Modifier.padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(template.category.color)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(tool, style = MaterialTheme.typography.bodyMedium)
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = { onIntent(TemplateGalleryIntent.ImportTemplate(template)) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !state.isImporting
                ) {
                    if (state.isImporting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text("Import Template / 导入模板")
                    }
                }
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
private fun TemplateCard(
    template: McpTemplate,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(template.category.color.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = getTemplateIcon(template.iconName),
                    contentDescription = null,
                    tint = template.category.color,
                    modifier = Modifier.size(28.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    template.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    template.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

private fun getTemplateIcon(iconName: String): ImageVector = when (iconName) {
    "PhoneAndroid" -> Icons.Default.Android
    "Build", "Architecture" -> Icons.Default.Architecture
    "PlayArrow" -> Icons.Default.PlayArrow
    "Cloud" -> Icons.Default.Cloud
    "BugReport" -> Icons.Default.BugReport
    "Description", "DesignServices" -> Icons.Default.Description
    else -> Icons.Default.Android
}
