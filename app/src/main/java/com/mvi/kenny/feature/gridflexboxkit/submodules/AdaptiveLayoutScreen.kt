package com.mvi.kenny.feature.gridflexboxkit.submodules

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mvi.kenny.feature.gridflexboxkit.*

// AdaptiveLayoutScreen — 自适应布局模板屏幕
// PRD-141 | Grid/FlexBox × Adaptive Layout 响应式设计模板
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AdaptiveLayoutScreen(onBack: () -> Unit) {
    var selectedTemplate by remember { mutableStateOf<AdaptiveTemplate?>(null) }

    val templates = listOf(
        AdaptiveTemplate(
            name = "仪表盘响应式",
            description = "小屏单列，大屏 2-3 列自适应网格",
            gridParams = AdaptiveGridParams(
                compactColumns = 1,
                mediumColumns = 2,
                expandedColumns = 3
            )
        ),
        AdaptiveTemplate(
            name = "列表响应式",
            description = "小屏单列详情，大屏双列列表+详情",
            gridParams = AdaptiveGridParams(
                compactColumns = 1,
                mediumColumns = 2,
                expandedColumns = 2
            )
        ),
        AdaptiveTemplate(
            name = "表单响应式",
            description = "小屏堆叠，大屏并排自适应表单",
            gridParams = AdaptiveGridParams(
                compactColumns = 1,
                mediumColumns = 2,
                expandedColumns = 2
            )
        ),
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("自适应布局模板") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(androidx.compose.material.icons.Icons.Rounded.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Text(
                text = "🎯 Grid/FlexBox × Adaptive Layout 响应式方案",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "基于 WindowSizeClass 实现小屏/中屏/大屏的动态布局切换",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            templates.forEach { template ->
                AdaptiveTemplateCard(
                    template = template,
                    isSelected = selectedTemplate == template,
                    onClick = { selectedTemplate = template }
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            // 预览区
            if (selectedTemplate != null) {
                Spacer(modifier = Modifier.height(16.dp))

                AdaptivePreviewCard(
                    template = selectedTemplate!!,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

data class AdaptiveTemplate(
    val name: String,
    val description: String,
    val gridParams: AdaptiveGridParams
)

data class AdaptiveGridParams(
    val compactColumns: Int,
    val mediumColumns: Int,
    val expandedColumns: Int
)

@Composable
private fun AdaptiveTemplateCard(
    template: AdaptiveTemplate,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surface
        ),
        border = if (isSelected)
            androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
        else
            null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = template.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = template.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    WindowSizeChip("Compact", "${template.gridParams.compactColumns}列")
                    WindowSizeChip("Medium", "${template.gridParams.mediumColumns}列")
                    WindowSizeChip("Expanded", "${template.gridParams.expandedColumns}列")
                }
            }
            if (isSelected) {
                Icon(
                    Icons.Rounded.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun WindowSizeChip(window: String, columns: String) {
    Surface(
        shape = RoundedCornerShape(4.dp),
        color = MaterialTheme.colorScheme.secondaryContainer
    ) {
        Text(
            text = "$window: $columns",
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            style = MaterialTheme.typography.labelSmall
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AdaptivePreviewCard(
    template: AdaptiveTemplate,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                text = "📱 响应式预览",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))

            // 模拟三种屏幕尺寸
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                listOf(
                    Triple("📱 Compact", template.gridParams.compactColumns, 120.dp),
                    Triple("📱 Medium", template.gridParams.mediumColumns, 200.dp),
                    Triple("🖥️ Expanded", template.gridParams.expandedColumns, 300.dp)
                ).forEach { (label, cols, width) ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(label, style = MaterialTheme.typography.labelSmall)
                        Spacer(modifier = Modifier.height(8.dp))
                        AdaptiveLayoutPreview(
                            columns = cols,
                            modifier = Modifier
                                .width(width)
                                .height(200.dp)
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AdaptiveLayoutPreview(
    columns: Int,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(
            modifier = Modifier.padding(4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            repeat(6) { index ->
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(28.dp),
                    shape = RoundedCornerShape(4.dp),
                    color = when (index % 3) {
                        0 -> Color(0xFF1E88E5).copy(alpha = 0.3f)
                        1 -> Color(0xFF8E24AA).copy(alpha = 0.3f)
                        else -> Color(0xFF43A047).copy(alpha = 0.3f)
                    }
                ) {
                    Box(
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "$index",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
