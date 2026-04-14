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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// =============================================================
// JourneysFrameworkComparisonScreen — 框架对比屏幕
// PRD-099 | Journeys for Android Studio 自动化 E2E 测试工具包
// =============================================================
/**
 * Framework Comparison Screen / 框架对比屏幕
 *
 * Compare Journeys with Espresso and UIAutomator.
 *
 * @param viewModel JourneysViewModel instance / JourneysViewModel 实例
 * @param onNavigateBack Callback to navigate back / 返回回调
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JourneysFrameworkComparisonScreen(
    viewModel: JourneysViewModel,
    onNavigateBack: () -> Unit
) {
    val frameworks = listOf(
        FrameworkInfo(
            name = "Journeys",
            color = Color(0xFF6750A4),
            emoji = "✨",
            description = "AI-powered E2E testing in Android Studio / Android Studio 中的 AI 驱动 E2E 测试",
            strengths = listOf(
                "AI-driven step understanding / AI 驱动的步骤理解",
                "Visual feedback verification / 视觉反馈验证",
                "No locator needed / 无需定位器",
                "Cross-App automation / 跨 App 自动化",
                "CI/CD friendly / CI/CD 友好"
            ),
            weaknesses = listOf(
                "Requires Android Studio Jellyfish+ / 需要 Android Studio Jellyfish+",
                "Still in alpha / 仍处于 Alpha 阶段",
                "Limited debugging tools / 调试工具有限"
            )
        ),
        FrameworkInfo(
            name = "Espresso",
            color = Color(0xFF4285F4),
            emoji = "☕",
            description = "Google's official Android UI testing framework / Google 官方 Android UI 测试框架",
            strengths = listOf(
                "Fast execution / 执行快速",
                "Stable and reliable / 稳定可靠",
                "Built-in synchronization / 内置同步机制",
                "Excellent for unit testing / 单元测试优秀",
                "Wide community support / 广泛的社区支持"
            ),
            weaknesses = listOf(
                "Requires element locators / 需要元素定位器",
                "Single app only / 仅限单个 App",
                "No visual feedback / 无视觉反馈"
            )
        ),
        FrameworkInfo(
            name = "UIAutomator",
            color = Color(0xFF34A853),
            emoji = "🤖",
            description = "Android's cross-app UI automation framework / Android 跨 App UI 自动化框架",
            strengths = listOf(
                "Cross-app automation / 跨 App 自动化",
                "Device and system UI / 设备和系统 UI",
                "No app modification needed / 无需修改 App",
                "Physical device support / 真机支持好"
            ),
            weaknesses = listOf(
                "Slow execution / 执行较慢",
                "Complex API / API 复杂",
                "Requires element locators / 需要元素定位器",
                "Limited visual verification / 视觉验证有限"
            )
        )
    )

    val comparisonMatrix = listOf(
        CapabilityRow("AI-driven step understanding / AI 驱动的步骤理解", "✅", "❌", "❌"),
        CapabilityRow("Visual feedback verification / 视觉反馈验证", "✅", "⚠️", "⚠️"),
        CapabilityRow("No locator needed / 无需定位器", "✅", "❌", "❌"),
        CapabilityRow("Cross-App automation / 跨 App 自动化", "✅", "❌", "✅"),
        CapabilityRow("CI/CD friendly / CI/CD 友好", "✅", "✅", "✅"),
        CapabilityRow("Fast execution / 快速执行", "⚠️", "✅", "❌"),
        CapabilityRow("Stable and reliable / 稳定可靠", "⚠️", "✅", "⚠️"),
        CapabilityRow("No app modification / 无需修改 App", "❌", "❌", "✅"),
        CapabilityRow("System UI testing / 系统 UI 测试", "❌", "❌", "✅"),
        CapabilityRow("Natural language authoring / 自然语言编写", "✅", "❌", "❌")
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Journey vs Espresso vs UIAutomator", fontWeight = FontWeight.Medium) },
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFFAFAFA)),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Framework cards / 框架卡片
            items(frameworks) { framework ->
                FrameworkCard(framework = framework)
            }

            // Capability matrix / 能力矩阵
            item {
                CapabilityMatrixCard(matrix = comparisonMatrix)
            }

            // Best practices / 最佳实践
            item {
                BestPracticesCard()
            }
        }
    }
}

// ================================================================
// Data Classes — 数据类
// ================================================================
private data class FrameworkInfo(
    val name: String,
    val color: Color,
    val emoji: String,
    val description: String,
    val strengths: List<String>,
    val weaknesses: List<String>
)

// ================================================================
// FrameworkCard — 框架卡片
// ================================================================
/**
 * Framework info card / 框架信息卡片
 */
@Composable
private fun FrameworkCard(framework: FrameworkInfo) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header / 头部
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(framework.color.copy(alpha = 0.15f))
                        .padding(12.dp)
                ) {
                    Text(text = framework.emoji, fontSize = 28.sp)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = framework.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = framework.color
                    )
                    Text(
                        text = framework.description,
                        fontSize = 12.sp,
                        color = Color(0xFF5F5F5F)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Strengths / 优势
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Strengths column / 优势列
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "✅ Strengths / 优势",
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp,
                        color = Color(0xFF4CAF50)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    framework.strengths.forEach { strength ->
                        Row(
                            modifier = Modifier.padding(vertical = 2.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Text(text = "•", color = Color(0xFF4CAF50), fontSize = 14.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = strength,
                                fontSize = 12.sp,
                                color = Color(0xFF1F1F1F)
                            )
                        }
                    }
                }

                // Weaknesses column / 劣势列
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "⚠️ Weaknesses / 劣势",
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp,
                        color = Color(0xFFFF9800)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    framework.weaknesses.forEach { weakness ->
                        Row(
                            modifier = Modifier.padding(vertical = 2.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Text(text = "•", color = Color(0xFFFF9800), fontSize = 14.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = weakness,
                                fontSize = 12.sp,
                                color = Color(0xFF1F1F1F)
                            )
                        }
                    }
                }
            }
        }
    }
}

// ================================================================
// CapabilityMatrixCard — 能力矩阵卡片
// ================================================================
private data class CapabilityRow(
    val capability: String,
    val journeys: String,
    val espresso: String,
    val uiautomator: String
)

/**
 * Capability comparison matrix / 能力对比矩阵
 */
@Composable
private fun CapabilityMatrixCard(matrix: List<CapabilityRow>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "⚖️ Capability Matrix / 能力矩阵",
                fontWeight = FontWeight.Medium,
                fontSize = 18.sp,
                color = Color(0xFF1F1F1F)
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Header row / 表头行
            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(modifier = Modifier.weight(2f)) {
                    Text(
                        text = "Capability / 能力",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = Color(0xFF5F5F5F)
                    )
                }
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Journeys",
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = Color(0xFF6750A4)
                    )
                }
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Espresso",
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = Color(0xFF4285F4)
                    )
                }
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "UIAutomator",
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = Color(0xFF34A853)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Divider / 分隔线
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(Color(0xFFE0E0E0))
            )

            // Matrix rows / 矩阵行
            matrix.forEach { row ->
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.weight(2f)) {
                        Text(
                            text = row.capability,
                            fontSize = 11.sp,
                            color = Color(0xFF1F1F1F)
                        )
                    }
                    MatrixCell(text = row.journeys, journeysColor = true, modifier = Modifier.weight(1f))
                    MatrixCell(text = row.espresso, espressoColor = true, modifier = Modifier.weight(1f))
                    MatrixCell(text = row.uiautomator, uiautomatorColor = true, modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

/**
 * Matrix cell / 矩阵单元格
 */
@Composable
private fun MatrixCell(
    text: String,
    journeysColor: Boolean = false,
    espressoColor: Boolean = false,
    uiautomatorColor: Boolean = false,
    modifier: Modifier = Modifier
) {
    val color = when {
        journeysColor -> Color(0xFF6750A4)
        espressoColor -> Color(0xFF4285F4)
        uiautomatorColor -> Color(0xFF34A853)
        else -> Color(0xFF1F1F1F)
    }

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 14.sp,
            color = color,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
    }
}

// ================================================================
// BestPracticesCard — 最佳实践卡片
// ================================================================
/**
 * Best practices card / 最佳实践卡片
 */
@Composable
private fun BestPracticesCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "💡 Best Practices / 最佳实践",
                fontWeight = FontWeight.Medium,
                fontSize = 18.sp,
                color = Color(0xFF1F1F1F)
            )
            Spacer(modifier = Modifier.height(16.dp))

            BestPracticeItem(
                emoji = "🟣",
                title = "Simple UI interactions → Espresso / 简单 UI 交互 → Espresso",
                description = "Fast, stable, no AI overhead / 快速、稳定、无 AI 开销"
            )
            BestPracticeItem(
                emoji = "🔵",
                title = "Complex flows / Visual → Journeys / 复杂流程/视觉 → Journeys",
                description = "AI-assisted, visual verification, natural language / AI 辅助、视觉验证、自然语言"
            )
            BestPracticeItem(
                emoji = "🟢",
                title = "Cross-App scenarios → UIAutomator / 跨 App 场景 → UIAutomator",
                description = "System UI, settings, multi-app flows / 系统 UI、设置、多 App 流程"
            )
            BestPracticeItem(
                emoji = "⚡",
                title = "Best: Journeys + Espresso / 最佳：Journeys + Espresso",
                description = "Journeys for E2E flows, Espresso for detailed assertions / Journeys 做 E2E 流程，Espresso 做细节断言"
            )
        }
    }
}

/**
 * Best practice item / 最佳实践项
 */
@Composable
private fun BestPracticeItem(emoji: String, title: String, description: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(text = emoji, fontSize = 20.sp)
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = title,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
                color = Color(0xFF1F1F1F)
            )
            Text(
                text = description,
                fontSize = 12.sp,
                color = Color(0xFF5F5F5F)
            )
        }
    }
}
