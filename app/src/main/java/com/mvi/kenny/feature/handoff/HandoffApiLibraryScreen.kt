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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
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
 * HandoffApiLibraryScreen — Handoff API 封装库
 * ============================================================
 * 代码示例、快速接入模板、参数配置
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HandoffApiLibraryScreen(
    onNavigateBack: () -> Unit,
    viewModel: HandoffViewModel
) {
    val state by viewModel.apiLibraryState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("API 封装库") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Tab Row / Tab 栏
            TabRow(
                selectedTabIndex = state.selectedTab.ordinal,
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                ApiLibraryTab.entries.forEach { tab ->
                    Tab(
                        selected = state.selectedTab == tab,
                        onClick = { viewModel.sendIntent(HandoffIntent.SelectApiTab(tab)) },
                        text = { Text(tab.titleZh) }
                    )
                }
            }

            // Tab Content / Tab 内容
            when (state.selectedTab) {
                ApiLibraryTab.QUICK_START -> QuickStartTab(
                    code = state.quickStartCode,
                    copiedItem = state.copiedItem,
                    onCopy = { code -> viewModel.sendIntent(HandoffIntent.CopyCode(code, "quick_start")) }
                )
                ApiLibraryTab.API_USAGE -> ApiUsageTab(
                    examples = state.apiUsageExamples,
                    copiedItem = state.copiedItem,
                    onCopy = { code, id -> viewModel.sendIntent(HandoffIntent.CopyCode(code, id)) }
                )
                ApiLibraryTab.CONFIG -> ConfigTab(
                    config = state.handoffConfig,
                    onUpdateConfig = { viewModel.sendIntent(HandoffIntent.UpdateHandoffConfig(it)) }
                )
            }
        }
    }
}

/**
 * Quick Start Tab / 快速接入 Tab
 */
@Composable
private fun QuickStartTab(
    code: String,
    copiedItem: String?,
    onCopy: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "快速接入模板",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "复制以下代码到你的 Activity 中，立即启用 Handoff 支持",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
        )

        // Code Block / 代码块
        CodeBlock(
            code = code,
            isCopied = copiedItem == "quick_start",
            onCopy = { onCopy(code) }
        )
    }
}

/**
 * API Usage Tab / API 用法 Tab
 */
@Composable
private fun ApiUsageTab(
    examples: List<ApiExample>,
    copiedItem: String?,
    onCopy: (String, String) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        itemsIndexed(examples) { index, example ->
            ApiExampleCard(
                example = example,
                isCopied = copiedItem == "example_$index",
                onCopy = { onCopy(example.code, "example_$index") }
            )
        }
    }
}

/**
 * API Example Card / API 示例卡片
 */
@Composable
private fun ApiExampleCard(
    example: ApiExample,
    isCopied: Boolean,
    onCopy: () -> Unit
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
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = example.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = example.titleZh,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                IconButton(onClick = onCopy) {
                    Icon(
                        imageVector = if (isCopied) Icons.Default.Done else Icons.Default.ContentCopy,
                        contentDescription = "复制",
                        tint = if (isCopied) HandoffColors.HandoffActive else MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Text(
                text = example.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                modifier = Modifier.padding(top = 8.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            CodeBlock(code = example.code, isCopied = isCopied, onCopy = onCopy)
        }
    }
}

/**
 * Config Tab / 参数配置 Tab
 */
@Composable
private fun ConfigTab(
    config: HandoffConfig,
    onUpdateConfig: (HandoffConfig) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "HandoffConfig 配置",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "配置 onHandoffActivityRequested() 回调参数",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
        )

        // Activity Name / Activity 名称
        ConfigItem(
            label = "Activity 类名",
            value = config.activityName.ifEmpty { "com.example.app.MainActivity" }
        )

        // Large Screen Support / 大屏支持
        ConfigToggle(
            label = "支持大屏",
            description = "启用 Large Screen SDK 联动",
            checked = config.supportsLargeScreen,
            onCheckedChange = { onUpdateConfig(config.copy(supportsLargeScreen = it)) }
        )

        // Keyboard Support / 键盘支持
        ConfigToggle(
            label = "支持键盘",
            description = "桌面模式键盘快捷键",
            checked = config.supportsKeyboard,
            onCheckedChange = { onUpdateConfig(config.copy(supportsKeyboard = it)) }
        )

        // Web Fallback / Web 降级
        ConfigItem(
            label = "Web 降级 URL",
            value = config.webFallbackUrl.ifEmpty { "未配置" }
        )
    }
}

/**
 * Config Item / 配置项
 */
@Composable
private fun ConfigItem(label: String, value: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(text = label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(text = value, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 4.dp))
        }
    }
}

/**
 * Config Toggle / 配置开关
 */
@Composable
private fun ConfigToggle(
    label: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clickable { onCheckedChange(!checked) },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = label, style = MaterialTheme.typography.labelMedium)
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
            Surface(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(if (checked) HandoffColors.HandoffActive else Color.Gray),
                shape = RoundedCornerShape(4.dp)
            ) {
                Text(
                    text = if (checked) "ON" else "OFF",
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                    color = Color.White,
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}

/**
 * Code Block / 代码块
 */
@Composable
private fun CodeBlock(
    code: String,
    isCopied: Boolean,
    onCopy: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Kotlin",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { onCopy() }
                ) {
                    Icon(
                        imageVector = if (isCopied) Icons.Default.Done else Icons.Default.ContentCopy,
                        contentDescription = null,
                        tint = if (isCopied) HandoffColors.HandoffActive else Color.Gray,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isCopied) "已复制" else "复制",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isCopied) HandoffColors.HandoffActive else Color.Gray
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = code,
                style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                color = Color(0xFFD4D4D4),
                modifier = Modifier.horizontalScroll(rememberScrollState())
            )
        }
    }
}
