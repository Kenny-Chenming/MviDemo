package com.mvi.kenny.feature.ai_studio_vibe_coding

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FactCheck
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.IntegrationInstructions
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.flow.collectLatest

/**
 * ============================================================
 * AiStudioToolkitScreen — Google AI Studio Android Vibe Coding 开发工具包主界面
 * Google AI Studio Android Vibe Coding Developer Toolkit Main Screen
 * ============================================================
 *
 * MVI Architecture: Screen is the View layer, consumes State and emits Intent.
 *
 * @param viewModel ViewModel instance / ViewModel 实例
 * @param onNavigateBack Back navigation callback / 返回导航回调
 *
 * @see AiStudioState
 * @see AiStudioIntent
 * @see AiStudioToolkitViewModel
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiStudioToolkitScreen(
    viewModel: AiStudioToolkitViewModel,
    onNavigateBack: () -> Unit = {}
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    var showQualityDialog by remember { mutableStateOf(false) }
    var qualityReportText by remember { mutableStateOf("") }

    // ============================================================
    // Effect Collection — 副作用收集
    // ============================================================
    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is AiStudioEffect.ShowToast -> {
                    Toast.makeText(context, effect.message, Toast.LENGTH_SHORT).show()
                }
                is AiStudioEffect.CopyToClipboard -> {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = ClipData.newPlainText("AI Studio Code", effect.text)
                    clipboard.setPrimaryClip(clip)
                }
                is AiStudioEffect.ScrollToTop -> {
                    // Handled by LazyColumn state
                }
                is AiStudioEffect.ShowQualityReport -> {
                    qualityReportText = effect.report
                    showQualityDialog = true
                }
            }
        }
    }

    // Quality Report Dialog / 质量报告对话框
    if (showQualityDialog) {
        AlertDialog(
            onDismissRequest = { showQualityDialog = false },
            title = { Text("质量评估报告 / Quality Report") },
            text = { Text(qualityReportText) },
            confirmButton = {
                TextButton(onClick = { showQualityDialog = false }) {
                    Text("关闭 / Close")
                }
            }
        )
    }

    // ============================================================
    // Scaffold — 页面脚手架
    // ============================================================
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Tab Content / Tab 内容区
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                when (state.selectedTab) {
                    0 -> QuickStartGuideTab(state, viewModel)
                    1 -> QualityAssessmentTab(state, viewModel)
                    2 -> HandoffWorkflowTab(state, viewModel)
                    3 -> EmulatorDebugTab(state, viewModel)
                    4 -> MobileWorkflowTab(state, viewModel)
                    5 -> WorkspaceIntegrationTab(state, viewModel)
                    6 -> BestPracticesTab(state, viewModel)
                }
            }

            // Bottom Navigation Bar / 底部导航栏
            BottomNavigationBar(
                selectedTab = state.selectedTab,
                onTabSelected = { viewModel.sendIntent(AiStudioIntent.SelectTab(it)) }
            )
        }
    }
}

// ============================================================
// Bottom Navigation Bar / 底部导航栏
// ============================================================

@Composable
private fun BottomNavigationBar(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit
) {
    val tabs = listOf(
        AiStudioTab.QUICK_START to Icons.Default.RocketLaunch,
        AiStudioTab.QUALITY to Icons.Default.FactCheck,
        AiStudioTab.HANDOFF to Icons.Default.SwapHoriz,
        AiStudioTab.EMULATOR to Icons.Default.PhoneAndroid,
        AiStudioTab.MOBILE_WORKFLOW to Icons.Default.Smartphone,
        AiStudioTab.WORKSPACE to Icons.Default.IntegrationInstructions,
        AiStudioTab.BEST_PRACTICES to Icons.Default.Star
    )

    NavigationBar {
        tabs.forEachIndexed { index, (tab, icon) ->
            NavigationBarItem(
                icon = { Icon(icon, contentDescription = tab.title) },
                label = { Text(tab.title, fontSize = 10.sp) },
                selected = selectedTab == index,
                onClick = { onTabSelected(index) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    }
}

// ============================================================
// Tab 0: Quick Start Guide / 上手指南
// ============================================================

@Composable
private fun QuickStartGuideTab(state: AiStudioState, viewModel: AiStudioToolkitViewModel) {
    val scrollState = rememberScrollState()

    LaunchedEffect(state.selectedTab) {
        if (state.selectedTab == 0) scrollState.scrollTo(0)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        // Hero Banner / Hero 横幅
        HeroBanner()

        Spacer(modifier = Modifier.height(16.dp))

        // Google I/O 2026 Badge / Google I/O 2026 徽章
        GoogleIOBadge()

        Spacer(modifier = Modifier.height(16.dp))

        // Quick Start Steps / 快速开始步骤
        Text(
            "快速开始 (3 Steps)",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))

        val quickStartSteps = listOf(
            QuickStartStep(
                stepNumber = 1,
                title = "编写提示词 / Write Your Prompt",
                description = "在 AI Studio 中用自然语言描述你的 App 需求。越具体的描述，生成的代码质量越高。",
                codeSnippet = "创建一个天气应用，显示当前温度、天气状况和未来3天预报，使用 Jetpack Compose"
            ),
            QuickStartStep(
                stepNumber = 2,
                title = "生成并预览 / Generate & Preview",
                description = "AI Studio 使用 Gemini 3.5 Flash 生成 Kotlin + Jetpack Compose 代码。在嵌入式模拟器中预览效果。",
                codeSnippet = null
            ),
            QuickStartStep(
                stepNumber = 3,
                title = "调试或导出 / Debug or Export",
                description = "使用嵌入式 Logcat 调试，或通过 ADB 安装到真机，或导出 ZIP/GitHub 移交给 Android Studio。",
                codeSnippet = "# ADB 安装到真机\nadb install generated-app.apk"
            )
        )

        quickStartSteps.forEach { step ->
            QuickStartStepCard(
                step = step,
                isExpanded = step.stepNumber in state.expandedQuickStartSteps,
                onToggle = { viewModel.sendIntent(AiStudioIntent.ToggleQuickStartStep(step.stepNumber)) },
                copiedBlockId = state.copiedCodeBlockId,
                onCopy = { code, blockId ->
                    viewModel.sendIntent(AiStudioIntent.CopyCode(code, blockId))
                }
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Feature Navigation Grid / 功能导航网格
        Text(
            "功能模块导航",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))

        FeatureNavigationGrid(
            onNavigateToTab = { tabIndex ->
                viewModel.sendIntent(AiStudioIntent.SelectTab(tabIndex))
            }
        )
    }
}

@Composable
private fun HeroBanner() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.Smartphone,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Google AI Studio\nAndroid Vibe Coding",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "从提示到原生 App，最快路径",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
private fun GoogleIOBadge() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF4285F4).copy(alpha = 0.1f))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.Default.Star,
            contentDescription = null,
            tint = Color(0xFF4285F4)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            "Google I/O 2026 — AI Studio Android Vibe Coding 正式发布",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun QuickStartStepCard(
    step: QuickStartStep,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    copiedBlockId: String?,
    onCopy: (String, String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle() },
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "${step.stepNumber}",
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        step.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Icon(
                    if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = "Expand"
                )
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        step.description,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    if (step.codeSnippet != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        CodeBlock(
                            code = step.codeSnippet,
                            blockId = "quickstart_${step.stepNumber}",
                            copiedBlockId = copiedBlockId,
                            onCopy = onCopy
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FeatureNavigationGrid(onNavigateToTab: (Int) -> Unit) {
    val features = listOf(
        Triple("质量评估", "fact_check", Icons.Default.FactCheck),
        Triple("Studio移交", "swap_horiz", Icons.Default.SwapHoriz),
        Triple("Emulator调试", "phone", Icons.Default.PhoneAndroid),
        Triple("移动端工作流", "smartphone", Icons.Default.Smartphone),
        Triple("Workspace集成", "integration", Icons.Default.IntegrationInstructions),
        Triple("最佳实践", "star", Icons.Default.Star)
    )

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        features.chunked(3).forEach { rowFeatures ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                rowFeatures.forEach { (title, _, icon) ->
                    val tabIndex = features.indexOfFirst { it.first == title }
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onNavigateToTab(tabIndex + 1) },
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(icon, contentDescription = null, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(title, fontSize = 12.sp, textAlign = TextAlign.Center)
                        }
                    }
                }
                // Fill empty spaces if less than 3 items
                repeat(3 - rowFeatures.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

// ============================================================
// Tab 1: Quality Assessment / 质量评估
// ============================================================

@Composable
private fun QualityAssessmentTab(state: AiStudioState, viewModel: AiStudioToolkitViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            "AI Studio 生成质量评估工具",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            "检测生成代码的 API 版本、Lint 规范、Google 推荐模式",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Prompt Input Section / 提示词输入区
        OutlinedTextField(
            value = state.promptInput,
            onValueChange = { viewModel.sendIntent(AiStudioIntent.UpdatePromptInput(it)) },
            label = { Text("输入你的提示词 / Enter your prompt") },
            placeholder = { Text("例如：创建一个待办事项列表应用...") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3
        )

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = { viewModel.sendIntent(AiStudioIntent.RunQualityAssessment(state.promptInput)) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.FactCheck, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("运行质量评估 / Run Assessment")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Quality Report Summary / 质量报告摘要
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            )
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.CheckCircle, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(state.generateQualityReport(), fontWeight = FontWeight.Medium)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Quality Checklist / 质量检查清单
        Text(
            "质量检查清单",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))

        val groupedChecks = state.qualityChecks.groupBy { it.category }
        groupedChecks.forEach { (category, checks) ->
            Text(
                category,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(4.dp))

            checks.forEach { check ->
                QualityCheckRow(
                    check = check,
                    onToggle = { viewModel.sendIntent(AiStudioIntent.ToggleQualityCheck(check.id)) }
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun QualityCheckRow(
    check: QualityCheckItem,
    onToggle: () -> Unit
) {
    val icon = when (check.severity) {
        "error" -> Icons.Default.Error
        "warning" -> Icons.Default.Warning
        else -> Icons.Default.Info
    }
    val color = when (check.severity) {
        "error" -> Color(0xFFEA4335)
        "warning" -> Color(0xFFFBBC04)
        else -> Color(0xFF4285F4)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle() }
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(checked = check.isChecked, onCheckedChange = { onToggle() })
        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            check.checkItem,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )
        if (check.isChecked) {
            Icon(
                Icons.Default.CheckCircle,
                contentDescription = null,
                tint = Color(0xFF34A853),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

// ============================================================
// Tab 2: Handoff Workflow / Studio 移交工作流
// ============================================================

@Composable
private fun HandoffWorkflowTab(state: AiStudioState, viewModel: AiStudioToolkitViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            "AI Studio × Android Studio 移交工作流",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            "ZIP 导出流程 / GitHub 导出流程 / Studio 导入完整步骤",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Decision Tree / 决策树
        DecisionTreeCard()

        Spacer(modifier = Modifier.height(16.dp))

        // Handoff Steps / 移交步骤
        val handoffSteps = listOf(
            HandoffStep(
                id = 1,
                title = "ZIP 导出流程",
                description = "适用于一次性移交，无需版本控制",
                subSteps = listOf(
                    "在 AI Studio 中点击 Export → Download ZIP",
                    "解压 ZIP 文件到目标目录",
                    "使用 Android Studio 打开项目 (File → Open)",
                    "等待 Gradle 同步完成"
                ),
                codeSnippet = "# 命令行方式\ngrep -r \"TODO\" app/src/main/java/ | head -20"
            ),
            HandoffStep(
                id = 2,
                title = "GitHub 导出流程",
                description = "适用于团队协作，保留版本历史",
                subSteps = listOf(
                    "在 AI Studio 中点击 Export → Connect GitHub",
                    "授权 AI Studio 访问 GitHub",
                    "创建新仓库或选择已有仓库",
                    "在 Android Studio 中 Clone 项目"
                )
            ),
            HandoffStep(
                id = 3,
                title = "Studio 导入完整步骤",
                description = "导入后的配置和验证",
                subSteps = listOf(
                    "File → Open → 选择项目根目录",
                    "等待 Sync Project with Gradle Files",
                    "检查 app/build.gradle.kts 中的 SDK 版本",
                    "运行 ./gradlew assembleDebug 验证构建"
                ),
                codeSnippet = "# 验证构建\n./gradlew assembleDebug\n\n# 查看生成的 APK\nls app/build/outputs/apk/debug/"
            ),
            HandoffStep(
                id = 4,
                title = "踩坑案例集",
                description = "常见问题和解决方案",
                subSteps = listOf(
                    "问题: Gradle 版本不兼容 → 解决: 升级 Gradle Wrapper",
                    "问题: SDK 版本冲突 → 解决: 统一 compileSdk/targetSdk",
                    "问题: 依赖库版本过旧 → 解决: 使用 AndroidX BOM"
                )
            )
        )

        handoffSteps.forEach { step ->
            HandoffStepCard(
                step = step,
                isExpanded = step.id in state.expandedHandoffSteps,
                onToggle = { viewModel.sendIntent(AiStudioIntent.ToggleHandoffStep(step.id)) },
                copiedBlockId = state.copiedCodeBlockId,
                onCopy = { code, blockId ->
                    viewModel.sendIntent(AiStudioIntent.CopyCode(code, blockId))
                }
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun DecisionTreeCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "何时用 ZIP vs GitHub？",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Decision ASCII art / 决策 ASCII 图
            Text(
                """
                开始
                  │
                  ▼
                需要版本控制？
                  │
            ┌─────┴─────┐
            ▼           ▼
          是           否
            │           │
            ▼           ▼
        GitHub 导出   ZIP 导出
                """.trimIndent(),
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DecisionChip(
                    label = "GitHub: 团队协作、持续开发",
                    color = Color(0xFF4285F4),
                    modifier = Modifier.weight(1f)
                )
                DecisionChip(
                    label = "ZIP: 一次性交付、快速验证",
                    color = Color(0xFF34A853),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun DecisionChip(label: String, color: Color, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(color.copy(alpha = 0.1f))
            .padding(8.dp)
    ) {
        Text(
            label,
            fontSize = 11.sp,
            color = color
        )
    }
}

@Composable
private fun HandoffStepCard(
    step: HandoffStep,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    copiedBlockId: String?,
    onCopy: (String, String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle() },
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
                        step.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        step.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
                Icon(
                    if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = "Expand"
                )
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column {
                    Spacer(modifier = Modifier.height(12.dp))
                    step.subSteps.forEachIndexed { index, subStep ->
                        Row {
                            Text("${index + 1}.", fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(subStep)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                    if (step.codeSnippet != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        CodeBlock(
                            code = step.codeSnippet,
                            blockId = "handoff_${step.id}",
                            copiedBlockId = copiedBlockId,
                            onCopy = onCopy
                        )
                    }
                }
            }
        }
    }
}

// ============================================================
// Tab 3: Emulator Debug Guide / 嵌入式 Emulator 调试指南
// ============================================================

@Composable
private fun EmulatorDebugTab(state: AiStudioState, viewModel: AiStudioToolkitViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            "AI Studio 嵌入式 Emulator 调试指南",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            "浏览器内调试 / Logcat / 断点 / Device File Explorer",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Emulator Features / 模拟器功能
        val emulatorFeatures = listOf(
            Triple("Logcat 日志查看", "查看应用运行日志", Icons.Default.Description),
            Triple("断点调试", "在浏览器中设置断点", Icons.Default.BugReport),
            Triple("Device File Explorer", "浏览设备文件系统", Icons.Default.Code),
            Triple("屏幕截图", "截取模拟器屏幕", Icons.Default.Smartphone)
        )

        emulatorFeatures.forEach { (title, desc, icon) ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(title, fontWeight = FontWeight.SemiBold)
                        Text(desc, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        Spacer(modifier = Modifier.height(8.dp))

        // ⚠️ Limitations Warning / ⚠️ 限制警告
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFFBBC04).copy(alpha = 0.1f)
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Warning,
                        contentDescription = null,
                        tint = Color(0xFFFBBC04)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "⚠️ 嵌入式 Emulator 限制",
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                val limitations = listOf(
                    "传感器/GPS 类功能行为可能与本地模拟器不同",
                    "性能可能低于本地模拟器",
                    "不支持多设备同时调试",
                    "网络配置有限制"
                )
                limitations.forEach { limit ->
                    Text("• $limit", fontSize = 13.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ADB Installation / ADB 真机安装
        Text(
            "ADB 真机安装",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))

        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("前置条件：")
                Spacer(modifier = Modifier.height(4.dp))
                val prerequisites = listOf(
                    "1. 设备开启开发者选项",
                    "2. 设备开启 USB 调试",
                    "3. 使用 USB 连接电脑",
                    "4. ADB 工具已安装并配置 PATH"
                )
                prerequisites.forEach { item ->
                    Text(item, fontSize = 13.sp)
                }

                Spacer(modifier = Modifier.height(12.dp))

                CodeBlock(
                    code = """# 检查设备连接
adb devices

# 安装 APK
adb install app-debug.apk

# 如需卸载
adb uninstall com.example.app""",
                    blockId = "adb_install",
                    copiedBlockId = state.copiedCodeBlockId,
                    onCopy = { code, blockId ->
                        viewModel.sendIntent(AiStudioIntent.CopyCode(code, blockId))
                    }
                )
            }
        }
    }
}

// ============================================================
// Tab 4: Mobile Workflow / 移动端开发工作流
// ============================================================

@Composable
private fun MobileWorkflowTab(state: AiStudioState, viewModel: AiStudioToolkitViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            "AI Studio 移动端开发工作流",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            "AI Studio 移动端 App → 桌面 Studio 的 Session Continuity",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Session Continuity Flow / Session 连续性流程
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "移动端 → 桌面端 Session 同步",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SessionFlowStep(icon = Icons.Default.Smartphone, label = "AI Studio\n移动端")
                    Icon(Icons.Default.SwapHoriz, contentDescription = null)
                    SessionFlowStep(icon = Icons.Default.Smartphone, label = "Google\n账号同步")
                    Icon(Icons.Default.SwapHoriz, contentDescription = null)
                    SessionFlowStep(icon = Icons.Default.Code, label = "Android\nStudio")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Step by Step / 分步说明
        val mobileSteps = listOf(
            Pair("第一步：移动端开始", "在 AI Studio 移动端 App 中用自然语言描述需求，生成 App 原型。生成的代码保存在你的 Google 账号下。"),
            Pair("第二步：Session 同步", "确保移动端和桌面端使用同一个 Google 账号登录。Session 会自动同步到云端。"),
            Pair("第三步：桌面端继续", "在桌面浏览器打开 AI Studio (aistudio.google.com)，找到同步的 Session，点击「继续开发」。"),
            Pair("第四步：移交 Studio", "完成开发后，使用 ZIP 或 GitHub 导出到 Android Studio 做高级调试和 UI 打磨。")
        )

        mobileSteps.forEach { (title, desc) ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(title, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(desc, fontSize = 13.sp)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        Spacer(modifier = Modifier.height(8.dp))

        // ⚠️ Account Requirement /        // ⚠️ Account Requirement / ⚠️ 账号要求
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFEA4335).copy(alpha = 0.1f)
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Warning,
                    contentDescription = null,
                    tint = Color(0xFFEA4335)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Session Continuity 依赖 Google 账号登录状态，请确保两端使用同一账号。",
                    fontSize = 13.sp
                )
            }
        }
    }
}

@Composable
private fun SessionFlowStep(icon: ImageVector, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(28.dp))
                .background(MaterialTheme.colorScheme.primary),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary)
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(label, fontSize = 11.sp, textAlign = TextAlign.Center)
    }
}

// ============================================================
// Tab 5: Workspace Integration / Google Workspace 集成
// ============================================================

@Composable
private fun WorkspaceIntegrationTab(state: AiStudioState, viewModel: AiStudioToolkitViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            "AI Studio × Google Workspace 集成",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            "从 AI Studio 构建的 App 如何调用 Sheets / Docs / Calendar API",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // API Options / API 选项
        val apiOptions = listOf(
            Triple("Sheets API", "读写 Google 表格数据", Icons.Default.Description),
            Triple("Docs API", "读写 Google 文档", Icons.Default.Description),
            Triple("Calendar API", "读写 Google 日历", Icons.Default.Description)
        )

        apiOptions.forEach { (name, desc, icon) ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(name, fontWeight = FontWeight.SemiBold)
                        Text(desc, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Code Example / 代码示例
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "Kotlin + Jetpack Compose 示例",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    "// 添加依赖 (build.gradle.kts)\n" +
                    "implementation(\"com.google.api-client:google-api-client-android:2.0.0\")\n" +
                    "implementation(\"com.google.apis:google-api-services-sheets:v4-rev20220927\")",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                CodeBlock(
                    code = """@Composable
fun SheetsIntegrationExample() {
    val spreadsheetId = "YOUR_SPREADSHEET_ID"
    val scope = listOf("https://www.googleapis.com/auth/spreadsheets")

    // 读取数据
    LaunchedEffect(Unit) {
        val service = Sheets.Builder(
            NetHttpTransport(),
            JacksonFactory.getDefaultInstance(),
            // 请使用 Application Default Credentials
        ).setApplicationName("AI Studio App").build()

        val range = "Sheet1!A1:B10"
        val response = service.spreadsheets().values()
            .get(spreadsheetId, range)
            .execute()
        val values = response.getValues()
    }
}""",
                    blockId = "workspace_sheets",
                    copiedBlockId = state.copiedCodeBlockId,
                    onCopy = { code, blockId ->
                        viewModel.sendIntent(AiStudioIntent.CopyCode(code, blockId))
                    }
                )
            }
        }
    }
}

// ============================================================
// Tab 6: Best Practices & Security / 最佳实践与安全合规
// ============================================================

@Composable
private fun BestPracticesTab(state: AiStudioState, viewModel: AiStudioToolkitViewModel) {
    var selectedComparisonTab by remember { mutableStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Tab Row for sub-sections / 子标签页
        TabRow(selectedTabIndex = selectedComparisonTab) {
            Tab(
                selected = selectedComparisonTab == 0,
                onClick = { selectedComparisonTab = 0 },
                text = { Text("最佳实践", fontSize = 12.sp) }
            )
            Tab(
                selected = selectedComparisonTab == 1,
                onClick = { selectedComparisonTab = 1 },
                text = { Text("工具对比", fontSize = 12.sp) }
            )
            Tab(
                selected = selectedComparisonTab == 2,
                onClick = { selectedComparisonTab = 2 },
                text = { Text("安全合规", fontSize = 12.sp) }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        when (selectedComparisonTab) {
            0 -> BestPracticesContent(state, viewModel)
            1 -> ToolComparisonContent(state, viewModel)
            2 -> SecurityComplianceContent(state, viewModel)
        }
    }
}

@Composable
private fun BestPracticesContent(state: AiStudioState, viewModel: AiStudioToolkitViewModel) {
    Column {
        Text(
            "AI Studio Android Vibe Coding 最佳实践",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(12.dp))

        // Prompt Templates / 提示词模板
        Text(
            "提示词模板",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(4.dp))

        val templates = listOf(
            Pair("基础模板", "创建一个简单的待办事项列表应用"),
            Pair("详细模板", "创建一个天气应用，使用 Jetpack Compose，显示当前温度、天气状况和未来3天预报，支持城市搜索"),
            Pair("高级模板", "创建一个笔记应用，支持创建/编辑/删除笔记，使用 Room 数据库存储，Material Design 3 主题")
        )

        templates.forEach { (title, template) ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(title, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    Text(template, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Iteration Strategy / 迭代优化策略
        Text(
            "迭代优化策略",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(4.dp))

        val strategies = listOf(
            "从简单开始：先生成一个最小可用的 App，再逐步添加功能",
            "明确技术栈：指定使用 Jetpack Compose、Room、Hilt 等",
            "分步生成：复杂 App 分多个提示词生成，避免一次性生成过多代码",
            "检查生成结果：AI Studio 生成的代码依赖 Gemini 3.5 Flash，需检查依赖库版本"
        )

        strategies.forEach { strategy ->
            Row(modifier = Modifier.padding(vertical = 2.dp)) {
                Text("• ", fontSize = 13.sp)
                Text(strategy, fontSize = 13.sp)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Common Failure Modes / 常见失败模式
        Text(
            "常见失败模式",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(4.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFEA4335).copy(alpha = 0.05f)
            ),
            shape = RoundedCornerShape(8.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                val failures = listOf(
                    "生成代码包含过时 API（如 findViewById）→ 使用质量评估工具检查",
                    "复杂数据库操作生成不完整 → 分步骤生成，手动补充",
                    "UI 布局在大屏设备上显示异常 → 导出到 Studio 后做响应式适配",
                    "生成的依赖库版本过旧 → 使用 AndroidX BOM 统一版本"
                )
                failures.forEach { failure ->
                    Text("⚠️ $failure", fontSize = 12.sp, modifier = Modifier.padding(vertical = 2.dp))
                }
            }
        }
    }
}

@Composable
private fun ToolComparisonContent(state: AiStudioState, viewModel: AiStudioToolkitViewModel) {
    Column {
        Text(
            "AI Studio vs Android Studio vs Android CLI",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(12.dp))

        // Comparison Table / 对比表格
        val comparisons = listOf(
            ToolComparisonEntry(
                aspect = "使用场景",
                aiStudio = "快速原型、概念验证、移动端 vibe coding",
                androidStudio = "生产级开发、复杂调试、深度定制",
                androidCli = "自动化构建、CI/CD、服务器端构建"
            ),
            ToolComparisonEntry(
                aspect = "代码生成",
                aiStudio = "✅ Gemini 3.5 Flash 自然语言生成",
                androidStudio = "❌ 手动编写",
                androidCli = "❌ 手动编写"
            ),
            ToolComparisonEntry(
                aspect = "模拟器",
                aiStudio = "✅ 嵌入式浏览器模拟器",
                androidStudio = "✅ 本地模拟器 / 真机",
                androidCli = "❌ 需手动启动"
            ),
            ToolComparisonEntry(
                aspect = "调试能力",
                aiStudio = "⚠️ 基础 Logcat + 断点",
                androidStudio = "✅ 完整调试套件",
                androidCli = "⚠️ ADB 调试"
            ),
            ToolComparisonEntry(
                aspect = "团队协作",
                aiStudio = "⚠️ GitHub 导出",
                androidStudio = "✅ 完整 Git 集成",
                androidCli = "✅ 命令行 Git"
            ),
            ToolComparisonEntry(
                aspect = "适合项目类型",
                aiStudio = "简单 App、原型、Hackathon",
                androidStudio = "生产级 App、企业级",
                androidCli = "自动化、脚本、CI/CD"
            )
        )

        // Table Header / 表格表头
        Row(modifier = Modifier.fillMaxWidth()) {
            Text("维度", fontWeight = FontWeight.Bold, modifier = Modifier.weight(1.2f), fontSize = 11.sp)
            Text("AI Studio", fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f), fontSize = 11.sp, color = Color(0xFF4285F4))
            Text("Android Studio", fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f), fontSize = 11.sp, color = Color(0xFF34A853))
            Text("Android CLI", fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f), fontSize = 11.sp, color = Color(0xFFEA4335))
        }
        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

        comparisons.forEach { entry ->
            Row(modifier = Modifier.fillMaxWidth()) {
                Text(entry.aspect, modifier = Modifier.weight(1.2f), fontSize = 11.sp)
                Text(entry.aiStudio, modifier = Modifier.weight(1f), fontSize = 10.sp)
                Text(entry.androidStudio, modifier = Modifier.weight(1f), fontSize = 10.sp)
                Text(entry.androidCli, modifier = Modifier.weight(1f), fontSize = 10.sp)
            }
            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Decision Flowchart / 决策流程图
        Text(
            "选型决策流程图",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(8.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(
                """
                开始
                  │
                  ▼
                是否需要快速原型？
                  │
            ┌─────┴─────┐
            ▼           ▼
          是           否
            │           │
            ▼           ▼
        AI Studio    需要复杂调试？
                      │
                ┌─────┴─────┐
                ▼           ▼
              是           否
                │           │
                ▼           ▼
          Android    Android CLI
          Studio      (CI/CD)
                """.trimIndent(),
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                modifier = Modifier.padding(12.dp)
            )
        }
    }
}

@Composable
private fun SecurityComplianceContent(state: AiStudioState, viewModel: AiStudioToolkitViewModel) {
    Column {
        Text(
            "AI Studio Android App 安全合规自查工具",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            "生成的代码的隐私/安全/权限审计",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Compliance Report Summary / 合规报告摘要
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            )
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Security, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(state.generateSecurityReport(), fontWeight = FontWeight.Medium)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Security Checklist / 安全检查清单
        val groupedSecurity = state.securityChecks.groupBy { it.category }
        groupedSecurity.forEach { (category, checks) ->
            Text(
                category,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(4.dp))

            checks.forEach { check ->
                SecurityCheckRow(
                    check = check,
                    onToggle = { viewModel.sendIntent(AiStudioIntent.ToggleSecurityCheck(check.id)) }
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Play Store Warning / Play Store 上架警告
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFEA4335).copy(alpha = 0.1f)
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Warning,
                        contentDescription = null,
                        tint = Color(0xFFEA4335)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "⚠️ Play Store 上架重要提示",
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "AI Studio 生成的代码需要开发者深度审核和修改才能上架。Google 建议将 AI Studio 用于原型开发，生产级 App 需移至 Android Studio 打磨后发布。",
                    fontSize = 13.sp
                )
            }
        }
    }
}

@Composable
private fun SecurityCheckRow(
    check: SecurityCheckItem,
    onToggle: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle() }
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(checked = check.isPassed, onCheckedChange = { onToggle() })
        Column(modifier = Modifier.weight(1f)) {
            Text(check.checkItem, style = MaterialTheme.typography.bodyMedium)
            Text(
                check.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
        if (check.isPassed) {
            Icon(
                Icons.Default.CheckCircle,
                contentDescription = null,
                tint = Color(0xFF34A853),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

// ============================================================
// Shared Components / 共享组件
// ============================================================

/**
 * Code block with copy button
 * 带复制按钮的代码块
 *
 * @param code Code content / 代码内容
 * @param blockId Unique block identifier / 唯一代码块标识
 * @param copiedBlockId Currently copied block ID / 当前已复制的代码块 ID
 * @param onCopy Copy callback / 复制回调
 */
@Composable
private fun CodeBlock(
    code: String,
    blockId: String,
    copiedBlockId: String?,
    onCopy: (String, String) -> Unit
) {
    val isCopied = copiedBlockId == blockId

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1E1E1E)
        )
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(
                    onClick = { onCopy(code, blockId) },
                    contentPadding = ButtonDefaults.TextButtonContentPadding
                ) {
                    Icon(
                        if (isCopied) Icons.Default.Check else Icons.Default.ContentCopy,
                        contentDescription = "Copy",
                        tint = if (isCopied) Color(0xFF34A853) else Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        if (isCopied) "已复制" else "复制",
                        fontSize = 12.sp,
                        color = if (isCopied) Color(0xFF34A853) else Color.White
                    )
                }
            }
            Text(
                code,
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp,
                color = Color.White,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
            )
        }
    }
}
