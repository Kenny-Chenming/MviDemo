package com.mvi.kenny.feature.handoff

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.DataObject
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * ============================================================
 * HandoffSerializationScreen — 序列化框架
 * ============================================================
 * 状态传输配置、安全过滤、冲突解决策略
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HandoffSerializationScreen(
    onNavigateBack: () -> Unit,
    viewModel: HandoffViewModel
) {
    val state by viewModel.serializationState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("序列化框架") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(modifier = Modifier.height(8.dp)) }

            // Strategy Selection / 策略选择
            item {
                StrategySelectionCard(
                    selectedStrategy = state.selectedStrategy,
                    onSelectStrategy = { viewModel.sendIntent(HandoffIntent.SelectStrategy(it)) }
                )
            }

            // Security Filters / 安全过滤
            item {
                SecurityFiltersCard(
                    filters = state.securityFilters,
                    onRemoveFilter = { viewModel.sendIntent(HandoffIntent.RemoveSecurityFilter(it)) },
                    onAddFilter = { viewModel.sendIntent(HandoffIntent.AddSecurityFilter(it)) }
                )
            }

            // Code Template / 代码模板
            item {
                CodeTemplateCard(
                    template = state.codeTemplate,
                    isGenerating = state.isGenerating,
                    onGenerate = { viewModel.sendIntent(HandoffIntent.GenerateSerializationCode) }
                )
            }

            // Sample JSON / 示例 JSON
            item {
                SampleJsonCard(json = state.sampleJson)
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}

/**
 * Strategy Selection Card / 策略选择卡片
 */
@Composable
private fun StrategySelectionCard(
    selectedStrategy: SerializationStrategy,
    onSelectStrategy: (SerializationStrategy) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.DataObject, contentDescription = null, tint = HandoffColors.Primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("冲突解决策略", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "当两端状态发生冲突时，选择一种解决策略",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SerializationStrategy.entries.forEach { strategy ->
                    FilterChip(
                        selected = selectedStrategy == strategy,
                        onClick = { onSelectStrategy(strategy) },
                        label = { Text(strategy.labelZh) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Strategy description / 策略说明
            val description = when (selectedStrategy) {
                SerializationStrategy.LWW -> "以时间戳为准，保留最新版本。简单高效，但可能丢失中间状态。"
                SerializationStrategy.MERGE -> "合并两端修改，保留所有变更。需要定义合并规则。"
                SerializationStrategy.MANUAL -> "弹出冲突对话框，让用户手动选择。用户体验最好，但需要实现 UI。"
            }
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

/**
 * Security Filters Card / 安全过滤卡片
 */
@Composable
private fun SecurityFiltersCard(
    filters: List<SecurityFilter>,
    onRemoveFilter: (String) -> Unit,
    onAddFilter: (SecurityFilter) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Close, contentDescription = null, tint = HandoffColors.Error)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("安全过滤规则", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                }
                IconButton(onClick = {
                    onAddFilter(SecurityFilter("newField", SecurityFilterType.EXCLUDE, "新增规则"))
                }) {
                    Icon(Icons.Default.Add, contentDescription = "添加规则")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "以下字段在 Handoff 时会被过滤，不会传输到其他设备",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )

            Spacer(modifier = Modifier.height(12.dp))

            filters.forEach { filter ->
                SecurityFilterItem(filter = filter, onRemove = { onRemoveFilter(filter.fieldName) })
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

/**
 * Security Filter Item / 安全过滤项
 */
@Composable
private fun SecurityFilterItem(
    filter: SecurityFilter,
    onRemove: () -> Unit
) {
    val typeColor = when (filter.filterType) {
        SecurityFilterType.EXCLUDE -> HandoffColors.Error
        SecurityFilterType.REDACT -> Color(0xFFFF9800)
        SecurityFilterType.ENCRYPT -> HandoffColors.Primary
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = filter.fieldName, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(typeColor.copy(alpha = 0.15f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(text = filter.filterType.label, style = MaterialTheme.typography.labelSmall, color = typeColor)
                }
            }
            Text(text = filter.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
        }
        IconButton(onClick = onRemove, modifier = Modifier.size(24.dp)) {
            Icon(Icons.Default.Close, contentDescription = "移除", modifier = Modifier.size(16.dp))
        }
    }
}

/**
 * Code Template Card / 代码模板卡片
 */
@Composable
private fun CodeTemplateCard(
    template: String,
    isGenerating: Boolean,
    onGenerate: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Code, contentDescription = null, tint = HandoffColors.Primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("序列化代码模板", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                }
                androidx.compose.material3.Button(
                    onClick = onGenerate,
                    enabled = !isGenerating
                ) {
                    if (isGenerating) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                    Text(if (isGenerating) "生成中..." else "生成代码")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF1E1E1E))
                    .padding(12.dp)
            ) {
                Text(
                    text = template,
                    style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                    color = Color(0xFFD4D4D4),
                    modifier = Modifier.horizontalScroll(rememberScrollState())
                )
            }
        }
    }
}

/**
 * Sample JSON Card / 示例 JSON 卡片
 */
@Composable
private fun SampleJsonCard(json: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("序列化 JSON 示例", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = json,
                style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(12.dp)
            )
        }
    }
}
