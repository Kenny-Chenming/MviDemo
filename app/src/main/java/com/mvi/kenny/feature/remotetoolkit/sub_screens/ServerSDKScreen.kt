package com.mvi.kenny.feature.remotetoolkit.sub_screens

import com.mvi.kenny.feature.remotetoolkit.RemoteToolkitState
import com.mvi.kenny.feature.remotetoolkit.RemoteToolkitIntent
import com.mvi.kenny.feature.remotetoolkit.RemoteToolkitEffect
import com.mvi.kenny.feature.remotetoolkit.RemoteToolkitColors


import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mvi.kenny.feature.remotetoolkit.RemoteTemplate

// =============================================================
// ServerSDKScreen — Compose Remote Kotlin 服务端 SDK 页面
// Server SDK / 服务端 SDK
// =============================================================

private val sampleTemplates = listOf(
    RemoteTemplate(
        id = "t1",
        name = "E-commerce Card",
        description = "Product display card with image, price and CTA",
        category = "E-commerce",
        code = """@RemoteComposable
fun ProductCard(
    imageUrl: String,
    name: String,
    price: Double,
    onAddToCart: () -> Unit
) {
    Card(elevation = 4.dp) {
        Column {
            AsyncImage(model = imageUrl, contentDescription = name)
            Column(modifier = Modifier.padding(12.dp)) {
                Text(name, style = MaterialTheme.typography.titleMedium)
                Text("$" + String.format("%.2f", price))
                Button(onClick = onAddToCart) { Text("Add to Cart") }
            }
        }
    }
}"""
    ),
    RemoteTemplate(
        id = "t2",
        name = "Chat Bubble",
        description = "Chat message bubble component",
        category = "Chat",
        code = """@RemoteComposable
fun ChatBubble(
    message: String,
    isFromUser: Boolean,
    timestamp: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isFromUser) Arrangement.End else Arrangement.Start
    ) {
        Surface(
            color = if (isFromUser) Purple else SurfaceVariant,
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(message)
                Text(timestamp, style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}"""
    ),
    RemoteTemplate(
        id = "t3",
        name = "Form Input",
        description = "Text input field with label and validation",
        category = "Forms",
        code = """@RemoteComposable
fun FormInput(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String = "",
    isError: Boolean = false
) {
    Column {
        Text(label, style = MaterialTheme.typography.labelMedium)
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder) },
            isError = isError,
            modifier = Modifier.fillMaxWidth()
        )
    }
}"""
    ),
    RemoteTemplate(
        id = "t4",
        name = "Dashboard Widget",
        description = "Data visualization widget card",
        category = "Dashboard",
        code = """@RemoteComposable
fun DashboardWidget(
    title: String,
    value: String,
    trend: String,
    trendDirection: TrendDirection
) {
    Card(elevation = 2.dp) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.labelMedium)
            Text(value, style = MaterialTheme.typography.headlineMedium)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (trendDirection == TrendDirection.UP)
                        Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                    contentDescription = null
                )
                Text(trend, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}"""
    ),
    RemoteTemplate(
        id = "t5",
        name = "AI Chat Message",
        description = "AI assistant message component",
        category = "AI",
        code = """@RemoteComposable
fun AIChatMessage(
    message: String,
    isStreaming: Boolean = false,
    onRetry: (() -> Unit)? = null
) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(Purple),
            contentAlignment = Alignment.Center
        ) {
            Text("AI", color = OnPrimary)
        }
        Spacer(modifier = Modifier.width(8.dp))
        Surface(
            color = SurfaceVariant,
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(message)
                if (isStreaming) {
                    LinearProgressIndicator(modifier = Modifier.padding(top = 4.dp))
                }
            }
        }
    }
}"""
    )
)

// =============================================================
// Syntax-highlighted code block / 语法高亮代码块
// =============================================================
@Composable
private fun CodeBlock(code: String, modifier: Modifier = Modifier) {
    val scrollState = rememberScrollState()
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF0D1117))
            .border(1.dp, RemoteToolkitColors.Outline, RoundedCornerShape(8.dp))
            .horizontalScroll(scrollState)
            .padding(16.dp)
    ) {
        Text(
            text = code,
            style = MaterialTheme.typography.bodySmall.copy(
                fontFamily = FontFamily.Monospace,
                fontSize = androidx.compose.ui.unit.TextUnit(12f, androidx.compose.ui.unit.TextUnitType.Sp)
            ),
            color = Color(0xFFE1E4E8)
        )
    }
}

// =============================================================
// ServerSDKScreen — 主内容
// =============================================================
@Composable
fun ServerSDKScreen(
    state: RemoteToolkitState,
    onIntent: (com.mvi.kenny.feature.remotetoolkit.RemoteToolkitIntent) -> Unit
) {
    val tabs = listOf("Overview", "Templates", "Reference")
    val selectedTab = state.sdkSelectedTab.coerceIn(0, tabs.size - 1)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(RemoteToolkitColors.Background)
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Compose Remote Kotlin SDK",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = RemoteToolkitColors.OnSurface
            )
        }

        // ============================================================
        // Tab Row
        // ============================================================
        item {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = RemoteToolkitColors.Surface,
                contentColor = RemoteToolkitColors.Purple,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = RemoteToolkitColors.Purple
                    )
                }
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { /* tab selection handled via state */ },
                        text = {
                            Text(
                                title,
                                color = if (selectedTab == index) RemoteToolkitColors.Purple
                                        else RemoteToolkitColors.OnSurfaceVariant
                            )
                        }
                    )
                }
            }
        }

        // ============================================================
        // Overview Tab
        // ============================================================
        item {
            if (selectedTab == 0) {
                OverviewContent()
            }
        }

        // ============================================================
        // Templates Tab
        // ============================================================
        if (selectedTab == 1) {
            item {
                Text(
                    text = "Template Library / 模板库",
                    style = MaterialTheme.typography.titleMedium,
                    color = RemoteToolkitColors.OnSurface
                )
            }

            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(sampleTemplates) { template ->
                        TemplateCard(template = template)
                    }
                }
            }

            // Selected template detail
            state.selectedTemplate?.let { template ->
                item {
                    Text(
                        text = "Template Detail / 模板详情: ${template.name}",
                        style = MaterialTheme.typography.titleMedium,
                        color = RemoteToolkitColors.OnSurface
                    )
                }
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = RemoteToolkitColors.Surface),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = template.description,
                                style = MaterialTheme.typography.bodyMedium,
                                color = RemoteToolkitColors.OnSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = template.category,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = RemoteToolkitColors.Purple
                                )
                                IconButton(onClick = { }) {
                                    Icon(
                                        Icons.Default.ContentCopy,
                                        contentDescription = "Copy",
                                        tint = RemoteToolkitColors.Purple
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            CodeBlock(code = template.code)
                        }
                    }
                }
            }
        }

        // ============================================================
        // Reference Tab
        // ============================================================
        if (selectedTab == 2) {
            item { ReferenceContent() }
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}

// =============================================================
// OverviewContent — SDK 概述内容
// =============================================================
@Composable
private fun OverviewContent() {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = RemoteToolkitColors.Surface),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "What is Compose Remote? / 什么是 Compose Remote？",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    color = RemoteToolkitColors.OnSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Compose Remote lets you serialize Compose UI to binary and render it natively on Android — no WebView, no JSON mapping. The UI is defined in Kotlin, serialized to a portable binary format, and rendered natively on the device.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = RemoteToolkitColors.OnSurfaceVariant
                )
            }
        }

        Text(
            text = "Quick Start / 快速开始",
            style = MaterialTheme.typography.titleMedium,
            color = RemoteToolkitColors.OnSurface
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = RemoteToolkitColors.Surface),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                StepItem(
                    number = "1",
                    title = "Define your UI in Kotlin",
                    code = """@RemoteComposable
fun MyCard(title: String, onClick: () -> Unit) {
    Card(onClick = onClick) {
        Text(title)
    }
}"""
                )
                Spacer(modifier = Modifier.height(16.dp))
                StepItem(
                    number = "2",
                    title = "Serialize to binary",
                    code = """val bytes = composeRemote {
    MyCard("Hello", onClick = { /*...*/ })
}.toByteArray()"""
                )
                Spacer(modifier = Modifier.height(16.dp))
                StepItem(
                    number = "3",
                    title = "Send to device",
                    code = """val client = RemoteClient()
client.send(bytes)"""
                )
            }
        }
    }
}

// =============================================================
// StepItem — 步骤项
// =============================================================
@Composable
private fun StepItem(number: String, title: String, code: String) {
    Row(verticalAlignment = Alignment.Top) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(RemoteToolkitColors.Purple),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = number,
                style = MaterialTheme.typography.labelMedium,
                color = RemoteToolkitColors.OnPurple,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = RemoteToolkitColors.OnSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            CodeBlock(code = code)
        }
    }
}

// =============================================================
// TemplateCard — 模板卡片
// =============================================================
@Composable
private fun TemplateCard(template: RemoteTemplate) {
    Card(
        modifier = Modifier
            .width(200.dp)
            .clickable { },
        colors = CardDefaults.cardColors(containerColor = RemoteToolkitColors.Surface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = template.name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = RemoteToolkitColors.OnSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = template.category,
                style = MaterialTheme.typography.labelSmall,
                color = RemoteToolkitColors.Purple
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = template.description,
                style = MaterialTheme.typography.bodySmall,
                color = RemoteToolkitColors.OnSurfaceVariant,
                maxLines = 2
            )
        }
    }
}

// =============================================================
// ReferenceContent — SDK 参考文档内容
// =============================================================
@Composable
private fun ReferenceContent() {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "API Reference / API 参考",
            style = MaterialTheme.typography.titleMedium,
            color = RemoteToolkitColors.OnSurface
        )

        ReferenceItem("@RemoteComposable", "Marks a composable as remotely renderable")
        ReferenceItem("composeRemote { }", "Creates a remote UI context")
        ReferenceItem("RemoteResult<T>", "Type-safe result wrapper for remote calls")
        ReferenceItem("@RemoteFunction", "Marks a function as a remote procedure")
        ReferenceItem("RemoteClient", "Client for sending/receiving remote UI")
        ReferenceItem("RemoteConfig", "Runtime configuration for remote rendering")
    }
}

// =============================================================
// ReferenceItem — 参考条目
// =============================================================
@Composable
private fun ReferenceItem(symbol: String, description: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = RemoteToolkitColors.Surface),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(RemoteToolkitColors.Purple.copy(alpha = 0.2f))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = symbol,
                    style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                    color = RemoteToolkitColors.Purple
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = RemoteToolkitColors.OnSurfaceVariant
            )
        }
    }
}
