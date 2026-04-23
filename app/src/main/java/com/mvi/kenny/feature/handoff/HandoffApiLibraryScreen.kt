package com.mvi.kenny.feature.handoff

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mvi.kenny.base.TopBarConfig
import kotlinx.coroutines.flow.collectLatest

/**
 * ============================================================
 * HandoffApiLibraryScreen — API 封装库页面
 * ============================================================
 */
@Composable
fun HandoffApiLibraryScreen(
    viewModel: HandoffApiLibraryViewModel = viewModel(),
    onUpdateTopBar: (TopBarConfig) -> Unit = {}
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) { onUpdateTopBar(TopBarConfig(title = "API 封装库")) }
    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is HandoffApiLibraryEffect.CodeCopied -> snackbarHostState.showSnackbar("${effect.label} 已复制到剪贴板 ✅")
                is HandoffApiLibraryEffect.ShowSnackbar -> snackbarHostState.showSnackbar(effect.message)
            }
        }
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            TabRow(selectedTabIndex = ApiLibraryTab.entries.indexOf(state.selectedTab)) {
                ApiLibraryTab.entries.forEach { tab ->
                    Tab(selected = state.selectedTab == tab, onClick = { viewModel.sendIntent(HandoffApiLibraryIntent.SwitchTab(tab)) }, text = { Text(tab.title) })
                }
            }

            when (state.selectedTab) {
                ApiLibraryTab.QUICK_START -> QuickStartTabContent(state.quickStartCode, state.copiedItem) { code, label ->
                    copyToClipboard(context, code, label)
                    viewModel.sendIntent(HandoffApiLibraryIntent.CopyCode(code, label))
                }
                ApiLibraryTab.API_USAGE -> ApiUsageTabContent(state.apiUsageExamples, state.copiedItem) { code, label ->
                    copyToClipboard(context, code, label)
                    viewModel.sendIntent(HandoffApiLibraryIntent.CopyCode(code, label))
                }
                ApiLibraryTab.CONFIG -> ConfigTabContent(state.handoffConfig) { config ->
                    viewModel.sendIntent(HandoffApiLibraryIntent.UpdateConfig(config))
                }
            }
        }
    }
}

@Composable
private fun QuickStartTabContent(code: String, copiedItem: String?, onCopy: (String, String) -> Unit) {
    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
        Text("🚀 快速接入 HandoffActivity", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Text("只需三步，让你的 Activity 支持跨设备 Handoff：", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(12.dp))
        listOf("继承 HandoffActivity 而非 ComponentActivity", "实现 onHandoffActivityRequested() 回调", "在回调中返回要传输的 Intent 数据").forEachIndexed { i, text ->
            Row(modifier = Modifier.padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                androidx.compose.foundation.layout.Box(modifier = Modifier.size(24.dp).padding(0.dp).let { m ->
                    androidx.compose.foundation.layout.Box(m, contentAlignment = Alignment.Center) {
                        Text("${i + 1}", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                })
                Spacer(modifier = Modifier.width(12.dp))
                Text(text, style = MaterialTheme.typography.bodyMedium)
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        CodeBlock(code, "HandoffActivity 模板", copiedItem == "HandoffActivity 模板") { onCopy(code, "HandoffActivity 模板") }
    }
}

@Composable
private fun ApiUsageTabContent(examples: List<ApiExample>, copiedItem: String?, onCopy: (String, String) -> Unit) {
    LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        items(examples) { example ->
            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant), shape = RoundedCornerShape(12.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text(example.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                        IconButton(onClick = { onCopy(example.code, example.title) }) {
                            Icon(if (copiedItem == example.title) Icons.Default.Done else Icons.Default.ContentCopy, "复制代码", tint = if (copiedItem == example.title) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    Text(example.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        example.tags.forEach { tag -> FilterChip(selected = false, onClick = { }, label = { Text(tag, fontSize = 10.sp) }) }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    CodeBlock(example.code, example.title, copiedItem == example.title) { onCopy(example.code, example.title) }
                }
            }
        }
    }
}

@Composable
private fun ConfigTabContent(config: HandoffConfig, onUpdateConfig: (HandoffConfig) -> Unit) {
    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("⚙️ Handoff 配置参数", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(4.dp))

        // Auto Resume
        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)), shape = RoundedCornerShape(8.dp)) {
            Row(modifier = Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("自动恢复 (Auto Resume)", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                    Text("收到 Handoff 时自动恢复 Activity", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Switch(checked = config.autoResume, onCheckedChange = { onUpdateConfig(config.copy(autoResume = it)) })
            }
        }

        // Priority
        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)), shape = RoundedCornerShape(8.dp)) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text("优先级 (Priority)", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                HandoffPriority.entries.forEach { priority ->
                    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                        Switch(checked = config.priority == priority, onCheckedChange = { if (it) onUpdateConfig(config.copy(priority = priority)) })
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(priority.displayName, style = MaterialTheme.typography.bodyMedium, fontWeight = if (config.priority == priority) FontWeight.Bold else FontWeight.Normal)
                    }
                }
            }
        }

        // Expiration
        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)), shape = RoundedCornerShape(8.dp)) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text("数据有效期", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Text("当前: ${config.expirationMillis / 1000 / 60} 分钟", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(1, 5, 15, 30).forEach { minutes ->
                        FilterChip(selected = config.expirationMillis == (minutes * 60 * 1000L), onClick = { onUpdateConfig(config.copy(expirationMillis = (minutes * 60 * 1000L))) }, label = { Text("${minutes}min") })
                    }
                }
            }
        }
    }
}

@Composable
private fun CodeBlock(code: String, label: String, isCopied: Boolean, onCopy: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHighest), shape = RoundedCornerShape(8.dp)) {
        Column {
            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                IconButton(onClick = onCopy, modifier = Modifier.size(32.dp)) {
                    Icon(if (isCopied) Icons.Default.Done else Icons.Default.ContentCopy, "复制", modifier = Modifier.size(16.dp), tint = if (isCopied) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Text(code, modifier = Modifier.padding(start = 12.dp, end = 12.dp, bottom = 12.dp), style = MaterialTheme.typography.bodySmall, fontFamily = FontFamily.Monospace, fontSize = 11.sp, lineHeight = 16.sp)
        }
    }
}

private fun copyToClipboard(context: Context, text: String, label: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    clipboard.setPrimaryClip(ClipData.newPlainText(label, text))
}
