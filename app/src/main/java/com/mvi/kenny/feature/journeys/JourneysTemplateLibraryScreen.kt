package com.mvi.kenny.feature.journeys

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.unit.sp

// =============================================================
// JourneysTemplateLibraryScreen — 模板库屏幕
// PRD-099 | Journeys for Android Studio 自动化 E2E 测试工具包
// =============================================================
/**
 * Template Library Screen / 模板库屏幕
 *
 * Browse and use pre-built Journey templates.
 *
 * @param viewModel JourneysViewModel instance / JourneysViewModel 实例
 * @param onNavigateBack Callback to navigate back / 返回回调
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JourneysTemplateLibraryScreen(
    viewModel: JourneysViewModel,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var previewTemplateId by remember { mutableStateOf<String?>(null) }

    val categories = TemplateCategory.entries

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Journey 模板库", fontWeight = FontWeight.Medium) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back / 返回",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF6750A4),
                    titleContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFFAFAFA))
        ) {
            // Category tabs / 分类 Tab
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = Color.White,
                contentColor = Color(0xFF6750A4)
            ) {
                Tab(
                    selected = selectedTabIndex == 0,
                    onClick = { selectedTabIndex = 0 },
                    text = { Text("All / 全部") }
                )
                categories.forEach { category ->
                    Tab(
                        selected = selectedTabIndex == categories.indexOf(category) + 1,
                        onClick = { selectedTabIndex = categories.indexOf(category) + 1 },
                        text = { Text("${category.emoji} ${category.label.split(" / ").first()}") }
                    )
                }
            }

            // Templates list / 模板列表
            val filteredTemplates = if (selectedTabIndex == 0) {
                state.templates
            } else {
                val selectedCategory = categories[selectedTabIndex - 1]
                state.templates.filter { it.category == selectedCategory }
            }

            if (filteredTemplates.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "📭", fontSize = 48.sp)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No templates found / 未找到模板",
                            fontSize = 16.sp,
                            color = Color(0xFF5F5F5F)
                        )
                    }
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredTemplates, key = { it.id }) { template ->
                        TemplateGridCard(
                            template = template,
                            onPreview = { previewTemplateId = template.id },
                            onUse = {
                                viewModel.processIntent(JourneysIntent.SelectTemplate(template.id))
                                onNavigateBack()
                            }
                        )
                    }
                }
            }
        }

        // Preview dialog / 预览对话框
        previewTemplateId?.let { templateId ->
            val template = state.templates.find { it.id == templateId }
            template?.let {
                TemplatePreviewDialog(
                    template = it,
                    onDismiss = { previewTemplateId = null },
                    onUse = {
                        viewModel.processIntent(JourneysIntent.SelectTemplate(templateId))
                        previewTemplateId = null
                        onNavigateBack()
                    }
                )
            }
        }
    }
}

/**
 * Template grid card / 模板网格卡片
 */
@Composable
private fun TemplateGridCard(
    template: JourneyTemplate,
    onPreview: () -> Unit,
    onUse: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Header / 头部
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = template.category.emoji,
                    fontSize = 24.sp
                )
                Text(
                    text = template.category.label.split(" / ").first(),
                    fontSize = 10.sp,
                    color = Color(0xFF5F5F5F)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Name / 名称
            Text(
                text = template.name,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
                color = Color(0xFF1F1F1F),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Description / 描述
            Text(
                text = template.description,
                fontSize = 12.sp,
                color = Color(0xFF5F5F5F),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Tags / 标签
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                template.tags.take(2).forEach { tag ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF6750A4).copy(alpha = 0.1f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = tag,
                            fontSize = 9.sp,
                            color = Color(0xFF6750A4)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action buttons / 操作按钮
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                OutlinedButton(
                    onClick = onPreview,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 4.dp)
                ) {
                    Icon(
                        Icons.Default.Visibility,
                        contentDescription = null,
                        modifier = Modifier.padding(end = 4.dp),
                        tint = Color(0xFF6750A4)
                    )
                    Text("Preview", fontSize = 10.sp, color = Color(0xFF6750A4))
                }
                Button(
                    onClick = onUse,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6750A4)),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 4.dp)
                ) {
                    Text("Use / 使用", fontSize = 10.sp)
                }
            }
        }
    }
}

/**
 * Template preview dialog / 模板预览对话框
 */
@Composable
private fun TemplatePreviewDialog(
    template: JourneyTemplate,
    onDismiss: () -> Unit,
    onUse: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f)),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header / 头部
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = template.category.emoji, fontSize = 24.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = template.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Color(0xFF1F1F1F)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = template.description,
                    fontSize = 14.sp,
                    color = Color(0xFF5F5F5F)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // XML Preview / XML 预览
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "XML Content / XML 内容:",
                            fontSize = 10.sp,
                            color = Color(0xFF9E9E9E)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = template.xmlContent,
                            fontSize = 11.sp,
                            color = Color(0xFF4EC9B0)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Actions / 操作
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Close / 关闭")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = onUse,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6750A4)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Use Template / 使用模板")
                    }
                }
            }
        }
    }
}
