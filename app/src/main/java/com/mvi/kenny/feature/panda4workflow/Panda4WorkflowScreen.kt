package com.mvi.kenny.feature.panda4workflow

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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Compare
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FactCheck
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.collectLatest

/**
 * ============================================================
 * Panda4WorkflowScreen — Android Studio Panda 4 AI 工作流工具包主界面
 * Panda4WorkflowScreen — Android Studio Panda 4 AI Workflow Toolkit Main Screen
 * ============================================================
 *
 * PRD-259 | Android Studio Panda 4 AI 原生开发工作流工具包
 *
 * Design: Terminal Console Style
 * - Deep dark background (#121212)
 * - Cyan accent (#00E5FF)
 * - Green accent (#76FF03)
 * - Orange accent (#FF9800)
 * - Red error (#FF5252)
 * - JetBrains Mono for code / 等宽字体显示代码
 *
 * 10 Tab Bottom Navigation:
 * - Tab 1: Overview / 总览
 * - Tab 2: Planning Mode / Planning Mode 最佳实践
 * - Tab 3: NEP / Next Edit Prediction 深度解析
 * - Tab 4: Web Search / Agent Web Search 使用指南
 * - Tab 5: Skills / Skills 开发指南
 * - Tab 6: Combined Workflow / Planning + NEP 组合工作流
 * - Tab 7: Norms / AI 原生开发规范白皮书
 * - Tab 8: Privacy / NEP 隐私与代码安全
 * - Tab 9: Comparison / 竞品对比工具
 * - Tab 10: Permission / 权限管理指南
 */

// ============================================================
// Terminal Style Color Palette / Terminal 风格配色
// ============================================================

private object PandaColors {
    val Background = Color(0xFF121212)
    val Surface = Color(0xFF1E1E1E)
    val SurfaceVariant = Color(0xFF2D2D2D)
    val Primary = Color(0xFF00E5FF)       // Cyan / 青色
    val Secondary = Color(0xFF76FF03)     // Green / 绿色
    val Warning = Color(0xFFFF9800)         // Orange / 橙色
    val Error = Color(0xFFFF5252)          // Red / 红色
    val TextPrimary = Color(0xFFE0E0E0)
    val TextSecondary = Color(0xFF9E9E9E)
    val CodeBackground = Color(0xFF0D1117)
    val Divider = Color(0xFF3D3D3D)
}

private val tabIcons = listOf(
    Icons.Default.AutoAwesome,  // Overview
    Icons.Default.FactCheck,         // Planning Mode
    Icons.Default.AutoAwesome,   // NEP (using AutoAwesome as placeholder)
    Icons.Default.Search,        // Web Search
    Icons.Default.Code,     // Skills
    Icons.Default.Link,          // Combined
    Icons.Default.Policy,        // Norms
    Icons.Default.Security,      // Privacy
    Icons.Default.Compare,       // Comparison
    Icons.Default.Settings       // Permission
)

// ============================================================
// Main Screen / 主界面
// ============================================================

/**
 * Panda 4 Workflow Toolkit Screen
 * Panda 4 AI 工作流工具包主界面
 *
 * @param viewModel Panda4WorkflowViewModel instance
 * @param onNavigateBack Navigation callback / 导航回调
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Panda4WorkflowScreen(
    viewModel: Panda4WorkflowViewModel = viewModel(),
    onNavigateBack: () -> Unit = {}
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    // Collect side effects / 收集副作用
    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is Panda4WorkflowEffect.ShowToast -> {
                    Toast.makeText(context, effect.message, Toast.LENGTH_SHORT).show()
                }
                is Panda4WorkflowEffect.CopyToClipboard -> {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = ClipData.newPlainText("Panda4Workflow", effect.text)
                    clipboard.setPrimaryClip(clip)
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "${Panda4Tab.entries[state.selectedTab].title} | Panda 4 AI",
                        color = PandaColors.Primary,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PandaColors.Surface
                ),
                actions = {
                    TextButton(onClick = onNavigateBack) {
                        Text("← Back", color = PandaColors.Primary)
                    }
                }
            )
        },
        bottomBar = {
            PandaNavigationBar(
                selectedIndex = state.selectedTab,
                onTabSelected = { viewModel.sendIntent(Panda4WorkflowIntent.SelectTab(it)) }
            )
        },
        containerColor = PandaColors.Background
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (state.selectedTab) {
                0 -> OverviewTab(state, viewModel)
                1 -> PlanningModeTab(state, viewModel)
                2 -> NEPTab(state, viewModel)
                3 -> WebSearchTab(state, viewModel)
                4 -> SkillsTab(state, viewModel)
                5 -> CombinedWorkflowTab(state, viewModel)
                6 -> NormsTab(state, viewModel)
                7 -> PrivacyTab(state, viewModel)
                8 -> ComparisonTab(state, viewModel)
                9 -> PermissionTab(state, viewModel)
            }
        }
    }
}

// ============================================================
// Bottom Navigation Bar / 底部导航栏
// ============================================================

@Composable
private fun PandaNavigationBar(
    selectedIndex: Int,
    onTabSelected: (Int) -> Unit
) {
    NavigationBar(
        containerColor = PandaColors.Surface,
        contentColor = PandaColors.Primary
    ) {
        Panda4Tab.entries.forEachIndexed { index, tab ->
            NavigationBarItem(
                selected = selectedIndex == index,
                onClick = { onTabSelected(index) },
                icon = {
                    Icon(
                        imageVector = tabIcons[index],
                        contentDescription = tab.title
                    )
                },
                label = {
                    Text(
                        text = tab.title,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 9.sp
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = PandaColors.Primary,
                    selectedTextColor = PandaColors.Primary,
                    unselectedIconColor = PandaColors.TextSecondary,
                    unselectedTextColor = PandaColors.TextSecondary,
                    indicatorColor = PandaColors.Primary.copy(alpha = 0.15f)
                )
            )
        }
    }
}

// ============================================================
// Tab 0: Overview / 总览
// ============================================================

@Composable
private fun OverviewTab(state: Panda4WorkflowState, viewModel: Panda4WorkflowViewModel) {
    val overviewCards = rememberOverviewCards()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "// Android Studio Panda 4 AI 工作流完全指南",
                color = PandaColors.Primary,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
            Text(
                text = "Planning Mode / Next Edit Prediction / Agent Web Search",
                color = PandaColors.TextSecondary,
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 4.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = PandaColors.Divider)
        }

        item {
            FeatureCard(
                title = "Planning Mode",
                description = "Agent 创建实现计划，开发者审查和调整后，Agent 按任务列表执行",
                icon = Icons.Default.FactCheck,
                color = PandaColors.Primary,
                onCopy = { viewModel.sendIntent(Panda4WorkflowIntent.CopyToClipboard("Planning Mode: Agent creates implementation plan, developer reviews and adjusts, then Agent executes task list")) }
            )
        }

        item {
            FeatureCard(
                title = "Next Edit Prediction (NEP)",
                description = "AI 预测下一次编辑，消除在多个文件间跳转的上下文切换痛苦",
                icon = Icons.Default.AutoAwesome,
                color = PandaColors.Secondary,
                onCopy = { viewModel.sendIntent(Panda4WorkflowIntent.CopyToClipboard("NEP: AI predicts next edit, eliminating context switching between multiple files")) }
            )
        }

        item {
            FeatureCard(
                title = "Agent Web Search",
                description = "IDE 内实时访问第三方库文档，无需离开编辑器",
                icon = Icons.Default.Search,
                color = PandaColors.Warning,
                onCopy = { viewModel.sendIntent(Panda4WorkflowIntent.CopyToClipboard("Agent Web Search: Real-time access to third-party library documentation within the IDE")) }
            )
        }

        itemsIndexed(overviewCards) { index, card ->
            ExpandableDocCard(
                card = card,
                isExpanded = index in state.expandedCards,
                onToggle = { viewModel.sendIntent(Panda4WorkflowIntent.ToggleCard(index)) },
                onCopy = { viewModel.sendIntent(Panda4WorkflowIntent.CopyToClipboard(card.codeContent)) }
            )
        }
    }
}

@Composable
private fun FeatureCard(
    title: String,
    description: String,
    icon: ImageVector,
    color: Color,
    onCopy: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = PandaColors.Surface),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = title,
                        color = color,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Text(
                        text = description,
                        color = PandaColors.TextSecondary,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
            IconButton(onClick = onCopy) {
                Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = "Copy",
                    tint = PandaColors.TextSecondary
                )
            }
        }
    }
}

@Composable
private fun rememberOverviewCards(): List<DocCard> = listOf(
    DocCard(
        id = 0,
        title = "Planning Mode 工作流程",
        description = "从需求到实现的完整 Planning Mode 流程",
        codeContent = """
# Planning Mode 工作流程 / Planning Mode Workflow

1. 打开 Planning Mode
   - Android Studio: Tools → Agent → Planning Mode
   - 快捷键: Ctrl+Shift+P (Mac: Cmd+Shift+P)

2. 输入自然语言需求
   "实现一个用户登录功能，包含邮箱验证和记住我选项"

3. Agent 生成实现计划
   - Agent 分析项目结构
   - 生成多步骤实现计划
   - 显示文件变更预览

4. 开发者审查计划
   - 添加/删除/修改步骤
   - 调整执行顺序
   - 确认后执行

5. Agent 执行计划
   - 按任务列表逐步实现
   - 生成可审计的 walkthrough
   - 自动提交 git (可选)
        """.trimIndent(),
        language = "markdown"
    ),
    DocCard(
        id = 1,
        title = "NEP + Planning Mode 组合使用",
        description = "两个功能协同使用的最佳实践",
        codeContent = """
# NEP + Planning Mode 组合工作流

场景: 实现一个完整的 RecyclerView 列表功能

步骤 1: 使用 Planning Mode 创建整体结构
   输入: "实现一个电影列表页面，包含:
         - RecyclerView 显示电影海报和标题
         - 点击进入详情页
         - 下拉刷新"

步骤 2: Agent 生成计划后执行
   - 创建 MovieListActivity
   - 创建 MovieAdapter
   - 创建 MovieItem布局
   - 创建 DetailActivity

步骤 3: 使用 NEP 优化单文件编辑
   - 在 MovieAdapter 中编辑时
   - NEP 预测下一个方法/变量
   - Tab 键接受预测
   - 减少键盘输入量

步骤 4: 使用 Agent Web Search 查阅文档
   - 不确定 RecyclerView 优化方法时
   - 使用 Agent Web Search 查阅官方文档
        """.trimIndent(),
        language = "markdown"
    )
)

@Composable
private fun ExpandableDocCard(
    card: DocCard,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    onCopy: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle() },
        colors = CardDefaults.cardColors(containerColor = PandaColors.Surface),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = card.title,
                        color = PandaColors.Primary,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Text(
                        text = card.description,
                        color = PandaColors.TextSecondary,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (isExpanded) "Collapse" else "Expand",
                    tint = PandaColors.TextSecondary
                )
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(PandaColors.CodeBackground)
                            .border(1.dp, PandaColors.Divider, RoundedCornerShape(6.dp))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = card.codeContent,
                            color = PandaColors.TextPrimary,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            lineHeight = 16.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(
                            onClick = onCopy,
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = PandaColors.Primary
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copy",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Copy", fontFamily = FontFamily.Monospace, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

// ============================================================
// Tab 1: Planning Mode / Planning Mode 最佳实践
// ============================================================

@Composable
private fun PlanningModeTab(state: Panda4WorkflowState, viewModel: Panda4WorkflowViewModel) {
    val planningCards = rememberPlanningCards()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "// Planning Mode 最佳实践指南",
                color = PandaColors.Primary,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
            Text(
                text = "何时用计划模式 vs 直接编码 / 计划审查框架 / 多轮对话技巧",
                color = PandaColors.TextSecondary,
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 4.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = PandaColors.Divider)
        }

        item {
            WhenToUseCard()
        }

        itemsIndexed(planningCards) { index, card ->
            ExpandableDocCard(
                card = card,
                isExpanded = index in state.expandedCards,
                onToggle = { viewModel.sendIntent(Panda4WorkflowIntent.ToggleCard(index)) },
                onCopy = { viewModel.sendIntent(Panda4WorkflowIntent.CopyToClipboard(card.codeContent)) }
            )
        }
    }
}

@Composable
private fun WhenToUseCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = PandaColors.Surface),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "// 何时使用 Planning Mode",
                color = PandaColors.Warning,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(8.dp))

            val scenarios = listOf(
                "✅ 使用" to listOf(
                    "多文件重构（涉及 3+ 文件）",
                    "新功能实现（需要创建多个组件）",
                    "架构变更（影响多个模块）",
                    "测试用例生成（涉及多个测试类）"
                ),
                "❌ 避免" to listOf(
                    "简单单行修改",
                    "变量重命名",
                    "简单文案修改",
                    "格式化代码"
                )
            )

            scenarios.forEach { (title, items) ->
                Text(
                    text = title,
                    color = if (title.startsWith("✅")) PandaColors.Secondary else PandaColors.Error,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                items.forEach { item ->
                    Text(
                        text = "  $item",
                        color = PandaColors.TextSecondary,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun rememberPlanningCards(): List<DocCard> = listOf(
    DocCard(
        id = 0,
        title = "Planning Mode 计划审查清单",
        description = "开发者审查 AI 生成计划时的检查项",
        codeContent = """
# 计划审查清单 / Plan Review Checklist

## 1. 正确性检查
   [ ] 计划是否符合需求描述
   [ ] 是否遗漏关键功能点
   [ ] 文件依赖关系是否正确
   [ ] 是否有循环依赖风险

## 2. 完整性检查
   [ ] 是否包含所有必要的文件创建
   [ ] 是否包含单元测试
   [ ] 是否包含必要的资源文件
   [ ] 是否包含 manifest 更新（如需要）

## 3. 安全性检查
   [ ] 是否有权限泄漏风险
   [ ] 是否有数据泄露风险
   [ ] 是否有 SQL 注入风险
   [ ] 是否有 XSS 风险（WebView 相关）

## 4. 性能检查
   [ ] 是否有不必要的重复计算
   [ ] 是否正确处理后台线程
   [ ] 是否有内存泄漏风险

## 5. 可维护性检查
   [ ] 命名是否符合规范
   [ ] 是否遵循项目架构
   [ ] 是否有适当的注释
        """.trimIndent(),
        language = "markdown"
    ),
    DocCard(
        id = 1,
        title = "Planning Mode 多轮对话技巧",
        description = "如何通过多轮对话优化计划",
        codeContent = """
# Planning Mode 多轮对话技巧

## 技巧 1: 增量式需求
不要:
  "实现完整的用户系统"
应该:
  "实现用户注册功能"
  → 完成后: "添加邮箱验证"
  → 完成后: "实现用户登录"
  → 完成后: "添加记住我功能"

## 技巧 2: 明确约束条件
不要:
  "实现一个列表页面"
应该:
  "实现列表页面，使用 LazyColumn，
   每项显示标题和图片，支持下拉刷新，
   数据来自 ViewModel，不使用 Room"

## 技巧 3: 指定架构风格
不要:
  "实现 MVVM 架构"
应该:
  "使用 MVI 架构，使用 viewModelScope.launch
   处理协程，StateFlow 管理状态，
   Channel<UiEffect> 处理副作用"

## 技巧 4: 引用现有代码
  "参考现有的 UserAdapter 实现新的 ProductAdapter，
   保持相同的代码风格和架构模式"
        """.trimIndent(),
        language = "markdown"
    )
)

// ============================================================
// Tab 2: NEP / Next Edit Prediction 深度解析
// ============================================================

@Composable
private fun NEPTab(state: Panda4WorkflowState, viewModel: Panda4WorkflowViewModel) {
    val nepCards = rememberNEPCards()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "// Next Edit Prediction 深度解析",
                color = PandaColors.Primary,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
            Text(
                text = "NEP 工作原理 / 上下文学习机制 / 预测准确性提升策略",
                color = PandaColors.TextSecondary,
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 4.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = PandaColors.Divider)
        }

        itemsIndexed(nepCards) { index, card ->
            ExpandableDocCard(
                card = card,
                isExpanded = index in state.expandedCards,
                onToggle = { viewModel.sendIntent(Panda4WorkflowIntent.ToggleCard(index)) },
                onCopy = { viewModel.sendIntent(Panda4WorkflowIntent.CopyToClipboard(card.codeContent)) }
            )
        }
    }
}

@Composable
private fun rememberNEPCards(): List<DocCard> = listOf(
    DocCard(
        id = 0,
        title = "NEP 工作原理",
        description = "Next Edit Prediction 的技术实现机制",
        codeContent = """
# NEP 工作原理 / How NEP Works

## 核心技术
NEP 使用本地机器学习模型预测开发者下一步编辑。

## 预测类型

### 1. 内联补全 (Inline Completions)
在光标位置直接显示预测文本
- 灰色背景显示预测内容
- Tab 键接受预测
- Esc 键拒绝预测

### 2. 幽灵文本 (Ghost Text)
在编辑器中直接显示预测
- 预测文本以不同颜色显示
- 继续输入时自动调整预测
- 适合模板代码生成

### 3. 多行预测
预测多行代码片段
- 基于函数签名的完整实现
- 基于注释的代码生成
- 测试用例快速生成

## 性能优化
- 本地模型，无需网络
- GPU 加速（如可用）
- 增量更新，减少重计算
        """.trimIndent(),
        language = "markdown"
    ),
    DocCard(
        id = 1,
        title = "NEP 上下文学习机制",
        description = "NEP 如何学习和适应项目上下文",
        codeContent = """
# NEP 上下文学习机制

## 学习阶段

### 初始训练
- 基于大规模开源代码库训练
- 掌握通用编程模式
- 理解常见 API 用法

### 项目适应
- 打开项目后，NEP 分析项目结构
- 学习项目特定的命名规范
- 理解项目使用的框架和库

### 会话内学习
- 在当前编辑会话中持续学习
- 适应开发者的编码风格
- 记住常用的代码模式

## 上下文理解深度
- 项目级别：整体架构和模块关系
- 文件级别：当前文件的代码结构
- 函数级别：当前函数的逻辑
- 语义级别：变量和方法的语义

## 隐私保证
- 所有学习在本地完成
- 不上传代码到服务器
- 可以完全禁用学习功能
        """.trimIndent(),
        language = "markdown"
    ),
    DocCard(
        id = 2,
        title = "NEP 预测准确性提升策略",
        description = "如何提高 NEP 预测的准确性",
        codeContent = """
# NEP 预测准确性提升策略

## 1. 良好的代码结构
- 遵循一致的文件组织
- 使用标准的命名规范
- 保持清晰的代码层次

## 2. 完整的类型信息
- 使用强类型语言特性
- 提供完整的泛型信息
- 避免使用 any/Object

## 3. 适当的注释
- 为复杂逻辑添加注释
- 使用 KDoc/Javadoc 文档化
- 注释有助于理解意图

## 4. 一致的编码风格
- 遵循项目的编码规范
- 使用项目自带的代码模板
- 保持相同的缩进和格式

## 5. 定期训练
- 定期使用 NEP 帮助其学习
- 接受正确的预测加速学习
- 拒绝错误的预测防止误导

## 注意事项
⚠️ NEP 预测仅供参考
⚠️ 重要代码需人工审核
⚠️ 安全相关代码需额外检查
        """.trimIndent(),
        language = "markdown"
    )
)

// ============================================================
// Tab 3: Agent Web Search / Agent Web Search 使用指南
// ============================================================

@Composable
private fun WebSearchTab(state: Panda4WorkflowState, viewModel: Panda4WorkflowViewModel) {
    val searchCards = rememberWebSearchCards()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "// Agent Web Search 使用指南",
                color = PandaColors.Primary,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
            Text(
                text = "支持库列表 / 搜索语法 / 文档质量评估",
                color = PandaColors.TextSecondary,
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 4.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = PandaColors.Divider)
        }

        item {
            SupportedLibrariesCard()
        }

        itemsIndexed(searchCards) { index, card ->
            ExpandableDocCard(
                card = card,
                isExpanded = index in state.expandedCards,
                onToggle = { viewModel.sendIntent(Panda4WorkflowIntent.ToggleCard(index)) },
                onCopy = { viewModel.sendIntent(Panda4WorkflowIntent.CopyToClipboard(card.codeContent)) }
            )
        }
    }
}

@Composable
private fun SupportedLibrariesCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = PandaColors.Surface),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "// 支持的库列表（持续更新）",
                color = PandaColors.Secondary,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(8.dp))

            val libraries = listOf(
                "Jetpack Compose", "Android KTX", "Room", "Hilt",
                "Retrofit", "OkHttp", "Kotlin Coroutines", "Flow",
                "Material 3", "Navigation Component", "WorkManager",
                "CameraX", "ML Kit", "Firebase"
            )

            libraries.chunked(3).forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    row.forEach { lib ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .background(
                                    PandaColors.Primary.copy(alpha = 0.1f),
                                    RoundedCornerShape(4.dp)
                                )
                                .padding(6.dp)
                        ) {
                            Text(
                                text = lib,
                                color = PandaColors.Primary,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.sp
                            )
                        }
                    }
                    // Fill remaining space if row is not complete
                    repeat(3 - row.size) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
            }
        }
    }
}

@Composable
private fun rememberWebSearchCards(): List<DocCard> = listOf(
    DocCard(
        id = 0,
        title = "Agent Web Search 搜索语法",
        description = "如何有效地使用 Agent Web Search",
        codeContent = """
# Agent Web Search 搜索语法

## 基本用法
直接在代码中选中相关内容
使用快捷键 Ctrl+Shift+R (Mac: Cmd+Shift+R)
触发 Agent Web Search

## 搜索技巧

### 1. 选择性搜索
不要搜索整个文件
只选择相关的代码片段

### 2. 上下文清晰
搜索时确保光标在相关代码附近
这有助于 Agent 理解你的需求

### 3. 具体问题
不要问太宽泛的问题
问: "这个 Room DAO 的写法是否正确"
不问: "怎么写 Android 代码"

### 4. 版本信息
提及具体的库版本
"Jetpack Compose 1.5 中的 LazyColumn 性能优化"

## 覆盖度说明
⚠️ Agent Web Search 主要覆盖官方文档
⚠️ 社区博客和 StackOverflow 覆盖有限
⚠️ 最新的第三方库文档可能未收录
        """.trimIndent(),
        language = "markdown"
    ),
    DocCard(
        id = 1,
        title = "文档质量评估指南",
        description = "如何评估 Agent Web Search 返回的文档质量",
        codeContent = """
# 文档质量评估指南

## 评估标准

### 1. 来源可靠性
✅ 可靠来源:
   - developer.android.com
   - Kotlin 官方文档
   - Jetpack 官方库文档
   - 官方 release notes

⚠️ 需要验证:
   - 社区博客（可能过时）
   - StackOverflow（答案可能不准确）
   - GitHub issues（可能是 bug，非文档）

### 2. 版本匹配
检查文档对应的版本
确保与你使用的版本匹配

### 3. 示例完整性
检查代码示例是否完整
是否可以独立运行

## 如何处理不可靠信息
1. 交叉验证多个来源
2. 查看官方文档的最新更新
3. 在官方 issue tracker 确认
4. 小规模测试验证
        """.trimIndent(),
        language = "markdown"
    )
)

// ============================================================
// Tab 4: Skills / Skills 开发指南
// ============================================================

@Composable
private fun SkillsTab(state: Panda4WorkflowState, viewModel: Panda4WorkflowViewModel) {
    val skillsCards = rememberSkillsCards()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "// Android Studio Agent Mode Skills 开发指南",
                color = PandaColors.Primary,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
            Text(
                text = "Skills 编写规范 / 社区 Skills 发现 / 自定义 Skills 集成",
                color = PandaColors.TextSecondary,
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 4.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = PandaColors.Divider)
        }

        item {
            SkillsStructureCard()
        }

        itemsIndexed(skillsCards) { index, card ->
            ExpandableDocCard(
                card = card,
                isExpanded = index in state.expandedCards,
                onToggle = { viewModel.sendIntent(Panda4WorkflowIntent.ToggleCard(index)) },
                onCopy = { viewModel.sendIntent(Panda4WorkflowIntent.CopyToClipboard(card.codeContent)) }
            )
        }
    }
}

@Composable
private fun SkillsStructureCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = PandaColors.Surface),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "// Skills 文件结构",
                color = PandaColors.Warning,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .background(PandaColors.CodeBackground)
                    .border(1.dp, PandaColors.Divider, RoundedCornerShape(6.dp))
                    .padding(12.dp)
            ) {
                Text(
                    text = """
# .android/studio/skill.json

{
  "name": "WorkManager Best Practices",
  "description": "WorkManager 最佳实践指南",
  "version": "1.0.0",
  "triggers": [
    "workmanager",
    "background task",
    "periodic work"
  ],
  "content": {
    "rules": [...],
    "examples": [...],
    "antiPatterns":                    "antiPatterns": [...]
  }
}
                    """.trimIndent(),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    lineHeight = 16.sp
                )
            }
        }
    }
}

@Composable
private fun rememberSkillsCards(): List<DocCard> = listOf(
    DocCard(
        id = 0,
        title = "自定义 Skill 开发模板",
        description = "为自己的 App/SDK 编写 Skill 的完整指南",
        codeContent = """
# 自定义 Skill 开发模板

## Skill JSON Schema

{
  "name": "Your Skill Name",
  "version": "1.0.0",
  "description": "Skill description",
  "author": "Your Name",
  "triggers": ["keyword1", "keyword2"],
  "content": {
    "overview": "Overview text",
    "rules": [
      {
        "id": "rule-001",
        "title": "Rule Title",
        "description": "Rule description",
        "examples": {
          "good": "Good example code",
          "bad": "Bad example code"
        }
      }
    ],
    "examples": [...],
    "references": [...]
  }
}

## 目录结构
.your-skill/
├── skill.json          # Skill 定义
├── rules/              # 规则目录
│   ├── rule-001.md
│   └── rule-002.md
├── examples/           # 示例目录
│   ├── good/
│   └── bad/
└── references/         # 参考资料
    └── links.json
        """.trimIndent(),
        language = "markdown"
    ),
    DocCard(
        id = 1,
        title = "社区 Skills 发现与使用",
        description = "如何发现和使用社区贡献的 Skills",
        codeContent = """
# 社区 Skills 发现与使用

## 发现渠道
1. Android Studio Marketplace
2. GitHub android-studio-skills 话题
3. Google 官方 Skills 库

## 安装社区 Skills
1. 打开 Settings → Agent → Skills
2. 点击 "Browse Community Skills"
3. 选择需要的 Skill
4. 点击 Install

## 验证 Skill 安装
- 检查 Settings → Agent → Skills 列表
- 尝试触发 Skill (输入 trigger 关键词)
- 查看 Skill 是否正确加载

## 贡献自己的 Skill
1. 创建符合规范的 Skill
2. 在 GitHub 创建公开仓库
3. 添加 android-studio-skill 话题
4. 提交到 Android Studio Marketplace 审核
        """.trimIndent(),
        language = "markdown"
    )
)

// ============================================================
// Tab 5: Combined Workflow / Planning + NEP 组合工作流
// ============================================================

@Composable
private fun CombinedWorkflowTab(state: Panda4WorkflowState, viewModel: Panda4WorkflowViewModel) {
    val combinedCards = rememberCombinedCards()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "// Planning Mode + NEP 组合工作流",
                color = PandaColors.Primary,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
            Text(
                text = "两者协同的最佳实践，含实际项目案例",
                color = PandaColors.TextSecondary,
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 4.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = PandaColors.Divider)
        }

        item {
            CombinedWorkflowDiagram()
        }

        itemsIndexed(combinedCards) { index, card ->
            ExpandableDocCard(
                card = card,
                isExpanded = index in state.expandedCards,
                onToggle = { viewModel.sendIntent(Panda4WorkflowIntent.ToggleCard(index)) },
                onCopy = { viewModel.sendIntent(Panda4WorkflowIntent.CopyToClipboard(card.codeContent)) }
            )
        }
    }
}

@Composable
private fun CombinedWorkflowDiagram() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = PandaColors.Surface),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "// 组合工作流示意图",
                color = PandaColors.Warning,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .background(PandaColors.CodeBackground)
                    .border(1.dp, PandaColors.Divider, RoundedCornerShape(6.dp))
                    .padding(12.dp)
                    .horizontalScroll(rememberScrollState())
            ) {
                Text(
                    text = """
┌─────────────────┐     ┌─────────────────┐     ┌─────────────────┐
│  Planning Mode  │────▶│  Plan Review    │────▶│  Execute Plan   │
│  (宏观规划)      │     │  (计划审查)      │     │  (执行计划)      │
└─────────────────┘     └─────────────────┘     └─────────────────┘
                                                         │
                                                         ▼
┌─────────────────┐     ┌─────────────────┐     ┌─────────────────┐
│  Agent Web      │◀────│  NEP Edit        │◀────│  File Edit      │
│  Search (查文档) │     │  (智能补全)      │     │  (编辑文件)      │
└─────────────────┘     └─────────────────┘     └─────────────────┘
                    """.trimIndent(),
                    color = PandaColors.TextPrimary,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    lineHeight = 20.sp
                )
            }
        }
    }
}

@Composable
private fun rememberCombinedCards(): List<DocCard> = listOf(
    DocCard(
        id = 0,
        title = "实战案例：实现电影列表页",
        description = "从需求到实现的完整组合工作流",
        codeContent = """
# 实战案例：实现电影列表页

## 步骤 1: Planning Mode 规划
输入:
"实现一个电影列表页面
- 使用 LazyColumn 显示电影海报和标题
- 点击进入电影详情页
- 支持下拉刷新和加载更多
- 使用 Hilt 注入依赖"

Agent 生成计划:
1. 创建 MovieListScreen.kt
2. 创建 MovieListViewModel.kt
3. 创建 MovieAdapter.kt
4. 创建 MovieDetailScreen.kt
5. 添加导航路由
6. 编写单元测试

## 步骤 2: Plan Review
审查计划，添加修改:
- 添加 Movie model
- 添加 MovieRepository
- 添加网络层错误处理

## 步骤 3: 执行计划 + NEP 辅助
在创建 MovieAdapter 时:
- NEP 预测常见的 ViewHolder 实现
- Tab 接受预测
- 减少样板代码输入

## 步骤 4: Agent Web Search 查文档
不确定 PullToRefresh 用法时:
- 选中使用 PullToRefresh 的代码
- 触发 Agent Web Search
- 查看最新文档
        """.trimIndent(),
        language = "markdown"
    ),
    DocCard(
        id = 1,
        title = "组合使用决策树",
        description = "何时使用哪种工具的决策指南",
        codeContent = """
# 组合使用决策树

## 问题: 我现在应该用什么？

### 需要创建新文件或多个文件？
  是 → Planning Mode
  否 → 继续判断

### 需要理解不熟悉的 API？
  是 → Agent Web Search
  否 → 继续判断

### 在编辑现有代码？
  是 → NEP
  否 → 继续判断

### 需要多步骤重构？
  是 → Planning Mode + NEP
  否 → 直接编码

## 效率提升技巧
1. Planning Mode 规划大方向
2. NEP 处理单文件编辑
3. Agent Web Search 解决疑难
4. 三者循环使用
        """.trimIndent(),
        language = "markdown"
    )
)

// ============================================================
// Tab 6: AI 原生开发规范白皮书 / Norms
// ============================================================

@Composable
private fun NormsTab(state: Panda4WorkflowState, viewModel: Panda4WorkflowViewModel) {
    val normsCards = rememberNormsCards()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "// AI 原生开发规范白皮书",
                color = PandaColors.Primary,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
            Text(
                text = "团队 AI 工具使用规范 / 代码审核 / 安全边界",
                color = PandaColors.TextSecondary,
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 4.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = PandaColors.Divider)
        }

        itemsIndexed(normsCards) { index, card ->
            ExpandableDocCard(
                card = card,
                isExpanded = index in state.expandedCards,
                onToggle = { viewModel.sendIntent(Panda4WorkflowIntent.ToggleCard(index)) },
                onCopy = { viewModel.sendIntent(Panda4WorkflowIntent.CopyToClipboard(card.codeContent)) }
            )
        }
    }
}

@Composable
private fun rememberNormsCards(): List<DocCard> = listOf(
    DocCard(
        id = 0,
        title = "团队 AI 工具使用规范",
        description = "团队使用 Panda 4 等 AI 工具的规范",
        codeContent = """
# 团队 AI 工具使用规范

## 1. 适用场景
✅ 推荐使用:
- 代码生成和补全
- 文档查询和解释
- 单元测试生成
- 代码重构建议
- 学习新技术

❌ 不建议使用:
- 安全敏感代码（如加密）
- 性能关键代码（未经审核）
- 不熟悉的代码库大改

## 2. 审核要求
- AI 生成的代码必须人工审核
- 关键逻辑需额外测试
- 安全相关代码需安全团队审核

## 3. 文档要求
- AI 生成的代码需添加注释说明
- 复杂逻辑需额外解释
- 第三方库使用需注明来源

## 4. 知识共享
- 使用 AI 学习的成果需沉淀到团队 wiki
- 常见问题解决方案需记录
- 最佳实践需定期总结
        """.trimIndent(),
        language = "markdown"
    ),
    DocCard(
        id = 1,
        title = "AI 生成代码审核清单",
        description = "审核 AI 生成代码时的检查项",
        codeContent = """
# AI 生成代码审核清单

## 通用检查
[ ] 代码逻辑是否正确
[ ] 是否符合项目编码规范
[ ] 命名是否清晰有意义
[ ] 是否有适当的错误处理
[ ] 是否有资源释放（close/dispose）
[ ] 是否有适当的日志

## Android 特定检查
[ ] 是否正确处理生命周期
[ ] 是否在主线程更新 UI
[ ] 是否有内存泄漏风险
[ ] 权限申请是否完整
[ ] 是否处理了配置变更

## 安全检查
[ ] 是否有 SQL 注入风险
[ ] 是否有 XSS 风险
[ ] 敏感数据是否硬编码
[ ] 网络请求是否加密

## 性能检查
[ ] 是否有不必要的重复计算
[ ] 是否有阻塞主线程的操作
[ ] 图片等资源是否正确加载
[ ] 列表是否使用了优化
        """.trimIndent(),
        language = "markdown"
    ),
    DocCard(
        id = 2,
        title = "AI 工具安全边界",
        description = "明确 AI 工具的安全使用边界",
        codeContent = """
# AI 工具安全边界

## 禁止事项
🚫 禁止在 AI 对话中输入:
- 用户隐私数据
- 商业机密
- 密钥和凭证
- 未发布的财务信息

🚫 禁止使用 AI:
- 替代安全代码审计
- 替代性能测试
- 生成用户权限相关内容（未经审核）
- 生成支付相关代码（未经安全团队审核）

## 建议做法
✅ 推荐做法:
- 使用脱敏数据测试 AI
- 关键代码多人审核
- 保持人工监督
- 定期审计 AI 使用情况

## 数据安全
- NEP 处理在本地完成（推荐）
- Planning Mode 可能需要网络（注意）
- Agent Web Search 是网络搜索（注意）
        """.trimIndent(),
        language = "markdown"
    )
)

// ============================================================
// Tab 7: Privacy & Security / 隐私与安全
// ============================================================

@Composable
private fun PrivacyTab(state: Panda4WorkflowState, viewModel: Panda4WorkflowViewModel) {
    val privacyCards = rememberPrivacyCards()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "// NEP 隐私与代码安全指南",
                color = PandaColors.Primary,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
            Text(
                text = "代码上传范围 / 数据处理 / 企业安全合规",
                color = PandaColors.TextSecondary,
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 4.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = PandaColors.Divider)
        }

        item {
            PrivacyInfoCard()
        }

        itemsIndexed(privacyCards) { index, card ->
            ExpandableDocCard(
                card = card,
                isExpanded = index in state.expandedCards,
                onToggle = { viewModel.sendIntent(Panda4WorkflowIntent.ToggleCard(index)) },
                onCopy = { viewModel.sendIntent(Panda4WorkflowIntent.CopyToClipboard(card.codeContent)) }
            )
        }
    }
}

@Composable
private fun PrivacyInfoCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = PandaColors.Surface),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "// NEP 数据处理模式对比",
                color = PandaColors.Error,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(8.dp))

            val modes = listOf(
                "本地模式" to listOf(
                    "所有处理在本地完成",
                    "代码不上传服务器",
                    "无网络延迟",
                    "适合处理敏感代码"
                ),
                "云端模式" to listOf(
                    "代码上传到服务器处理",
                    "需要网络连接",
                    "可能有更好的预测准确性",
                    "需确认服务条款和隐私政策"
                )
            )

            modes.forEach { (mode, features) ->
                Text(
                    text = mode,
                    color = PandaColors.Warning,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                features.forEach { feature ->
                    Text(
                        text = "  • $feature",
                        color = PandaColors.TextSecondary,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun rememberPrivacyCards(): List<DocCard> = listOf(
    DocCard(
        id = 0,
        title = "代码上传范围说明",
        description = "明确哪些代码会被上传处理",
        codeContent = """
# 代码上传范围说明

## 本地模式（推荐）
不上传任何代码到服务器
- 所有 ML 模型推理在本地
- 使用本地缓存的代码上下文
- 完全离线的预测能力

## 云端模式（可选）
可能上传的内容:
- 当前编辑文件的代码片段
- 光标附近的代码上下文
- 项目的一些元信息

## 不上传的内容
🚫 不会上传:
- 项目完整代码库
- Git 历史
- 注释中的敏感信息
- 其他未打开的文件

## 企业用户建议
1. 使用本地模式
2. 配置企业内部部署
3. 定期审计日志
        """.trimIndent(),
        language = "markdown"
    ),
    DocCard(
        id = 1,
        title = "企业安全合规指南",
        description = "企业环境下使用 AI 工具的合规建议",
        codeContent = """
# 企业安全合规指南

## 合规要求
1. 数据本地化要求
2. 代码不出网要求
3. 审计日志要求
4. 访问控制要求

## 推荐配置
- 使用本地模式 NEP
- 配置企业内部的 AI 服务
- 开启完整的审计日志
- 限制可使用的 AI 功能

## 合规检查清单
[ ] 已确认数据处理方式
[ ] 已评估安全风险
[ ] 已配置适当的访问控制
[ ] 员工已接受安全培训
[ ] 已有应急响应流程

## 审计建议
- 定期审查 AI 工具使用日志
- 监控异常的数据访问
- 定期更新安全策略
        """.trimIndent(),
        language = "markdown"
    )
)

// ============================================================
// Tab 8: Comparison Tool / 竞品对比工具
// ============================================================

@Composable
private fun ComparisonTab(state: Panda4WorkflowState, viewModel: Panda4WorkflowViewModel) {
    val scenarios = rememberComparisonScenarios()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "// Android Studio Panda 4 vs 竞品对比工具",
            color = PandaColors.Primary,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp
        )
        Text(
            text = "勾选你的使用场景，获取推荐工具",
            color = PandaColors.TextSecondary,
            fontFamily = FontFamily.Monospace,
            fontSize = 12.sp
        )

        HorizontalDivider(color = PandaColors.Divider)

        Text(
            text = "// 选择使用场景:",
            color = PandaColors.TextPrimary,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
        )

        scenarios.forEach { scenario ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.sendIntent(Panda4WorkflowIntent.ToggleScenario(scenario.id)) }
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = scenario.id in state.selectedScenarios,
                    onCheckedChange = { viewModel.sendIntent(Panda4WorkflowIntent.ToggleScenario(scenario.id)) },
                    colors = CheckboxDefaults.colors(
                        checkedColor = PandaColors.Primary,
                        uncheckedColor = PandaColors.TextSecondary
                    )
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = scenario.name,
                        color = PandaColors.TextPrimary,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = scenario.description,
                        color = PandaColors.TextSecondary,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp
                    )
                }
            }
        }

        Button(
            onClick = { viewModel.sendIntent(Panda4WorkflowIntent.RunComparison) },
            colors = ButtonDefaults.buttonColors(containerColor = PandaColors.Primary),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "运行对比 / Run Comparison",
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )
        }

        state.comparisonResult?.let { result ->
            HorizontalDivider(color = PandaColors.Divider)

            Text(
                text = "// 推荐结果",
                color = PandaColors.Secondary,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = PandaColors.Surface),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "推荐工具: ${result.recommended}",
                        color = PandaColors.Primary,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = result.reasoning,
                        color = PandaColors.TextSecondary,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        lineHeight = 18.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "备选工具:",
                        color = PandaColors.Warning,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    result.alternatives.forEach { alt ->
                        Text(
                            text = "  • $alt",
                            color = PandaColors.TextSecondary,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            Button(
                onClick = { viewModel.sendIntent(Panda4WorkflowIntent.CopyToClipboard(
                    "Recommended: ${result.recommended}\n\nReasoning:\n${result.reasoning}\n\nAlternatives:\n${result.alternatives.joinToString("\n") { "  • $it" }}"
                )) },
                colors = ButtonDefaults.buttonColors(containerColor = PandaColors.Secondary),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("复制结果 / Copy Result", fontFamily = FontFamily.Monospace)
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun rememberComparisonScenarios(): List<ComparisonScenario> = listOf(
    ComparisonScenario(
        id = 0,
        name = "多文件重构任务",
        description = "需要同时修改多个文件的复杂重构"
    ),
    ComparisonScenario(
        id = 1,
        name = "追求编辑速度",
        description = "希望减少击键次数，提高单文件编辑效率"
    ),
    ComparisonScenario(
        id = 2,
        name = "第三方库文档查询",
        description = "需要频繁查阅 Android 库文档"
    ),
    ComparisonScenario(
        id = 3,
        name = "CI/CD 自动化任务",
        description = "需要自动化构建、测试、部署流程"
    ),
    ComparisonScenario(
        id = 4,
        name = "企业安全合规",
        description = "代码不能上传到外部服务器"
    )
)

// ============================================================
// Tab 9: Permission Guide / 权限管理
// ============================================================

@Composable
private fun PermissionTab(state: Panda4WorkflowState, viewModel: Panda4WorkflowViewModel) {
    val permissionCards = rememberPermissionCards()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "// Agent Mode 权限管理指南",
                color = PandaColors.Primary,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
            Text(
                text = "Panda 3 Patch 1 新增细粒度权限控制文档",
                color = PandaColors.TextSecondary,
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 4.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = PandaColors.Divider)
        }

        item {
            PermissionMatrixCard()
        }

        itemsIndexed(permissionCards) { index, card ->
            ExpandableDocCard(
                card = card,
                isExpanded = index in state.expandedCards,
                onToggle = { viewModel.sendIntent(Panda4WorkflowIntent.ToggleCard(index)) },
                onCopy = { viewModel.sendIntent(Panda4WorkflowIntent.CopyToClipboard(card.codeContent)) }
            )
        }
    }
}

@Composable
private fun PermissionMatrixCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = PandaColors.Surface),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "// 权限配置矩阵（按角色）",
                color = PandaColors.Warning,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(8.dp))

            val roles = listOf(
                "开发者" to listOf(
                    "文件读写: ✅ 允许",
                    "网络访问: ✅ 允许",
                    "Git 操作: ✅ 允许",
                    "终端执行: ⚠️ 确认后执行"
                ),
                "Tech Lead" to listOf(
                    "文件读写: ✅ 允许",
                    "网络访问: ✅ 允许",
                    "Git 操作: ✅ 允许",
                    "终端执行: ✅ 允许"
                ),
                "安全管理员" to listOf(
                    "文件读写: ⚠️ 只读",
                    "网络访问: ⚠️ 白名单",
                    "Git 操作: ⚠️ 只读",
                    "终端执行: ❌ 禁止"
                )
            )

            roles.forEach { (role, perms) ->
                Text(
                    text = role,
                    color = PandaColors.Primary,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                perms.forEach { perm ->
                    Text(
                        text = "  $perm",
                        color = PandaColors.TextSecondary,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun rememberPermissionCards(): List<DocCard> = listOf(
    DocCard(
        id = 0,
        title = "细粒度权限配置方法",
        description = "如何在 Android Studio 中配置 Agent Mode 权限",
        codeContent = """
# 细粒度权限配置方法

## 入口
Settings → Agent → Permissions

## 权限分类

### 1. 文件访问权限
- 只读访问
- 项目内读写
- 完全访问

### 2. 网络访问权限
- 禁止网络
- 白名单模式
- 完全允许

### 3. Git 操作权限
- 禁止 Git
- 只读操作
- 完全允许

### 4. 终端执行权限
- 禁止执行
- 确认后执行
- 完全允许

## 配置建议
开发阶段: 宽松配置以提高效率
生产环境: 严格配置确保安全
        """.trimIndent(),
        language = "markdown"
    ),
    DocCard(
        id = 1,
        title = "权限配置代码示例",
        description = "通过配置文件管理团队权限",
        codeContent = """
# 权限配置代码示例

## team-permissions.json
{
  "version": "1.0",
  "roles": {
    "developer": {
      "fileAccess": "project-read-write",
      "networkAccess": "whitelist",
      "gitAccess": "read-write",
      "terminalAccess": "confirm-before-execute",
      "allowedCommands": ["gradle", "git"]
    },
    "tech-lead": {
      "fileAccess": "full",
      "networkAccess": "full",
      "gitAccess": "full",
      "terminalAccess": "allow",
      "allowedCommands": ["*"]
    },
    "security-admin": {
      "fileAccess": "read-only",
      "networkAccess": "whitelist",
      "gitAccess": "read-only",
      "terminalAccess": "deny",
      "allowedCommands": []
    }
  },
  "whitelist": {
    "network": [
      "developer.android.com",
      "kotlinlang.org",
      "github.com"
    ]
  }
}
        """.trimIndent(),
        language = "json"
    )
)
