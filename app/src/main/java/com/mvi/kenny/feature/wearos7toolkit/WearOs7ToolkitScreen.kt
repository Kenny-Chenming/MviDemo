package com.mvi.kenny.feature.wearos7toolkit

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mvi.kenny.feature.wearos7toolkit.WearModuleType.*
import com.mvi.kenny.feature.wearos7toolkit.WearOs7ToolkitIntent.*
import kotlinx.coroutines.launch

// ============================================================
// WearOs7ToolkitScreen — 主界面 / Main Screen
// 10 modules A-J Tab navigation + content reader
// ============================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WearOs7ToolkitScreen(
    viewModel: WearOs7ToolkitViewModel,
    onUpdateTopBar: (com.mvi.kenny.base.TopBarConfig) -> Unit
) {
    val state by viewModel.state.collectAsState()
    val snackbarHost = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(state.currentModule) {
        onUpdateTopBar(com.mvi.kenny.base.TopBarConfig(
            title = "Wear OS 7 ${state.currentModule.labelZh}"
        ))
    }

    LaunchedEffect(state.codeCopiedId) {
        state.codeCopiedId?.let {
            scope.launch { snackbarHost.showSnackbar("已复制代码到剪贴板") }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHost) },
        bottomBar = {
            ModuleTabRow(
                currentModule = state.currentModule,
                onModuleSelected = { viewModel.sendIntent(NavigateToModule(it)) }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            ModuleHeader(state.currentModule)
            state.moduleData[state.currentModule]?.let { data ->
                ModuleOverview(data.overview)
                LanguageSwitcher(
                    selected = state.selectedLang,
                    onSelect = { viewModel.sendIntent(SwitchLanguage(it)) }
                )
                when (state.currentModule) {
                    WearModuleType.G_DECISION_TREE -> DecisionTreeContent(
                        state = state,
                        onInput = { viewModel.sendIntent(SetDecisionInput(it)) }
                    )
                    WearModuleType.E_WATCH_FACE -> BeforeAfterContent(data.beforeAfter)
                    else -> StandardModuleContent(
                        data = data,
                        state = state,
                        onToggleSection = { viewModel.sendIntent(ToggleSection(it)) },
                        onCopyCode = { viewModel.sendIntent(CopyCode(it)) }
                    )
                }
            } ?: Box(Modifier.fillMaxSize(), Alignment.Center) {
                CircularProgressIndicator()
            }
        }
    }
}

// ============================================================
// ModuleTabRow — Bottom Tab Navigation / 底部 Tab 导航栏
// ============================================================
@Composable
private fun ModuleTabRow(
    currentModule: WearModuleType,
    onModuleSelected: (WearModuleType) -> Unit
) {
    val scrollState = rememberScrollState()
    Surface(color = MaterialTheme.colorScheme.surface, tonalElevation = 4.dp) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(scrollState)
                .padding(horizontal = 4.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            WearModuleType.entries.forEach { module ->
                val selected = module == currentModule
                FilterChip(
                    selected = selected,
                    onClick = { onModuleSelected(module) },
                    label = {
                        Text(
                            text = "${module.name.take(1)}",
                            fontSize = 11.sp,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = WearOs7Colors.Primary,
                        selectedLabelColor = androidx.compose.ui.graphics.Color.White
                    ),
                    modifier = Modifier.height(32.dp)
                )
            }
        }
    }
}

// ============================================================
// ModuleHeader — Module Title Header / 模块标题头部
// ============================================================
@Composable
private fun ModuleHeader(module: WearModuleType) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(WearOs7Colors.Primary.copy(alpha = 0.08f))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "模块 ${module.name.take(1)}",
            fontSize = 12.sp, color = WearOs7Colors.Primary, fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = module.labelZh,
            fontSize = 18.sp, fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(Modifier.width(8.dp))
        Surface(
            color = if (module.priority == "P0") WearOs7Colors.Accent.copy(alpha = 0.12f)
                    else WearOs7Colors.Warning.copy(alpha = 0.12f),
            shape = RoundedCornerShape(4.dp)
        ) {
            Text(
                text = module.priority,
                fontSize = 11.sp,
                color = if (module.priority == "P0") WearOs7Colors.Accent else WearOs7Colors.Warning,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
            )
        }
    }
}

// ============================================================
// ModuleOverview — Module Overview Text / 模块概述
// ============================================================
@Composable
private fun ModuleOverview(overview: String) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = overview,
            fontSize = 13.sp, lineHeight = 18.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(12.dp)
        )
    }
}

// ============================================================
// LanguageSwitcher — Code Language Switcher / 代码语言切换器
// ============================================================
@Composable
private fun LanguageSwitcher(
    selected: CodeLanguage,
    onSelect: (CodeLanguage) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        CodeLanguage.entries.forEach { lang ->
            FilterChip(
                selected = lang == selected,
                onClick = { onSelect(lang) },
                label = { Text(lang.label, fontSize = 12.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = WearOs7Colors.Secondary
                ),
                modifier = Modifier.height(28.dp)
            )
        }
    }
}

// ============================================================
// StandardModuleContent — Standard Module Content / 标准模块内容
// ============================================================
@Composable
private fun StandardModuleContent(
    data: ModuleData,
    state: WearOs7ToolkitState,
    onToggleSection: (String) -> Unit,
    onCopyCode: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(data.sections) { section ->
            SectionCard(
                section = section,
                isExpanded = section.id in state.expandedSections,
                onToggle = { onToggleSection(section.id) }
            )
        }
        if (data.codeExamples.isNotEmpty()) {
            item { SectionTitle("代码示例 / Code Examples") }
            items(data.codeExamples) { example ->
                CodeBlockCard(
                    example = example,
                    isCopied = state.codeCopiedId == example.id,
                    onCopy = { onCopyCode(example.id) }
                )
            }
        }
        if (data.apiTable.isNotEmpty()) {
            item { SectionTitle("API 参考 / API Reference") }
            item { ApiTableCard(data.apiTable) }
        }
        items(data.notes) { note -> NoteCard(note) }
    }
}

// ============================================================
// SectionCard — Expandable Section Card / 可折叠章节卡片
// ============================================================
@Composable
private fun SectionCard(
    section: ContentSection,
    isExpanded: Boolean,
    onToggle: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = section.title,
                    fontSize = 15.sp, fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = if (isExpanded) "▲" else "▼",
                    fontSize = 12.sp, color = WearOs7Colors.OnSurfaceVar
                )
            }
            if (isExpanded) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = section.body,
                    fontSize = 13.sp, lineHeight = 19.sp,
                    color = WearOs7Colors.OnSurfaceVar
                )
            }
        }
    }
}

// ============================================================
// SectionTitle — Small Title / 小标题
// ============================================================
@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title,
        fontSize = 14.sp, fontWeight = FontWeight.Bold,
        color = WearOs7Colors.Primary,
        modifier = Modifier.padding(vertical = 4.dp)
    )
}

// ============================================================
// CodeBlockCard — Code Block Card / 代码块卡片
// ============================================================
@Composable
private fun CodeBlockCard(
    example: CodeExample,
    isCopied: Boolean,
    onCopy: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = WearOs7Colors.CodeBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(WearOs7Colors.Border.copy(alpha = 0.5f))
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = example.title,
                    fontSize = 12.sp, fontWeight = FontWeight.Medium,
                    color = WearOs7Colors.OnSurface
                )
                TextButton(onClick = onCopy) {
                    Text(
                        text = if (isCopied) "已复制" else "复制",
                        fontSize = 12.sp,
                        color = if (isCopied) WearOs7Colors.Secondary else WearOs7Colors.Primary
                    )
                }
            }
            Text(
                text = example.code,
                fontSize = 12.sp, fontFamily = FontFamily.Monospace,
                lineHeight = 16.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(12.dp),
                color = WearOs7Colors.OnSurface
            )
        }
    }
}

// ============================================================
// ApiTableCard — API Parameter Table / API 参数表格
// ============================================================
@Composable
private fun ApiTableCard(entries: List<ApiEntry>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(WearOs7Colors.Primary.copy(alpha = 0.08f))
                    .padding(8.dp)
            ) {
                Text("参数", fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                Text("类型", fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(0.8f))
                Text("必填", fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(0.4f))
            }
            Divider()
            entries.forEachIndexed { idx, entry ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp, horizontal = 8.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(entry.param, fontSize = 12.sp, fontWeight = FontWeight.Medium, fontFamily = FontFamily.Monospace)
                        Text(entry.desc, fontSize = 11.sp, color = WearOs7Colors.OnSurfaceVar, lineHeight = 15.sp)
                    }
                    Text(entry.type, fontSize = 11.sp, fontFamily = FontFamily.Monospace, modifier = Modifier.weight(0.8f))
                    Text(
                        if (entry.required) "Y" else "-",
                        fontSize = 12.sp,
                        color = if (entry.required) WearOs7Colors.Accent else WearOs7Colors.OnSurfaceVar,
                        modifier = Modifier.weight(0.4f)
                    )
                }
                if (idx < entries.lastIndex) Divider(color = WearOs7Colors.Border.copy(alpha = 0.5f))
            }
        }
    }
}

// ============================================================
// NoteCard — Tip/Warning Callout / 提示/警告卡片
// ============================================================
@Composable
private fun NoteCard(note: NoteCallout) {
    val (bgColor, icon) = when (note.type) {
        "warning" -> WearOs7Colors.Accent.copy(alpha = 0.1f) to "⚠️"
        "tip"     -> WearOs7Colors.Secondary.copy(alpha = 0.1f) to "💡"
        else      -> WearOs7Colors.Primary.copy(alpha = 0.1f) to "ℹ️"
    }
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = bgColor,
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.Top) {
            Text(icon, fontSize = 14.sp)
            Spacer(Modifier.width(8.dp))
            Text(note.content, fontSize = 13.sp, lineHeight = 18.sp)
        }
    }
}

// ============================================================
// BeforeAfterContent — Before/After Comparison / Before/After 对比
// ============================================================
@Composable
private fun BeforeAfterContent(beforeAfter: BeforeAfter?) {
    if (beforeAfter == null) {
        Box(Modifier.fillMaxSize(), Alignment.Center) {
            Text("无 Before/After 对比", fontSize = 13.sp, color = WearOs7Colors.OnSurfaceVar)
        }
        return
    }
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SectionTitle("迁移对比 / Migration Comparison")
        CodeBlockCard(
            example = CodeExample("before", "v4 (Before)", CodeLanguage.XML, beforeAfter.before),
            isCopied = false,
            onCopy = {}
        )
        CodeBlockCard(
            example = CodeExample("after", "v5 (After)", CodeLanguage.XML, beforeAfter.after),
            isCopied = false,
            onCopy = {}
        )
        NoteCard(NoteCallout("tip", beforeAfter.explanation))
    }
}

// ============================================================
// DecisionTreeContent — Workout Decision Tree / 健身方案决策树
// ============================================================
@Composable
private fun DecisionTreeContent(
    state: WearOs7ToolkitState,
    onInput: (AppCategory) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { SectionTitle("选择你的 App 类型 / Select App Category") }
        items(AppCategory.entries) { category ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onInput(category) },
                colors = CardDefaults.cardColors(
                    containerColor = if (state.decisionCategory == category)
                        WearOs7Colors.Primary.copy(alpha = 0.12f)
                    else MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = state.decisionCategory == category,
                        onClick = { onInput(category) }
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(category.labelZh, fontSize = 15.sp, fontWeight = FontWeight.Medium)
                }
            }
        }
        state.decisionCategory?.let { category ->
            item {
                val rec = getRecommendation(category)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = WearOs7Colors.Secondary.copy(alpha = 0.08f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("推荐方案 / Recommendation", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = WearOs7Colors.Secondary)
                        Spacer(Modifier.height(8.dp))
                        Text("方案: ${rec.approach}", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                        Text("原因: ${rec.reason}", fontSize = 13.sp, lineHeight = 18.sp)
                        Text("预估成本: ${rec.cost}", fontSize = 13.sp, color = WearOs7Colors.OnSurfaceVar)
                        Text("推荐工具: ${rec.providers}", fontSize = 13.sp, color = WearOs7Colors.OnSurfaceVar)
                    }
                }
            }
        }
    }
}

// Decision tree recommendation / 决策树推荐算法
private data class Recommendation(val approach: String, val reason: String, val cost: String, val providers: String)

private fun getRecommendation(appType: AppCategory): Recommendation {
    return when (appType) {
        AppCategory.FITNESS -> Recommendation(
            approach = "WEAR_WORKOUT_TRACKER",
            reason = "专用健身 App，推荐接入 Google 原生 Workout Tracker",
            cost = "低（1-2 周）",
            providers = "Fitness Workouts API, ASICS Runkeeper"
        )
        AppCategory.HEALTH -> Recommendation(
            approach = "HYBRID",
            reason = "综合健康 App，需要自有数据层 + Tracker 桥接",
            cost = "中（2-4 周）",
            providers = "Workout Tracker + Own Data Layer + Live Updates"
        )
        AppCategory.NAVIGATION, AppCategory.COMMUNICATION -> Recommendation(
            approach = "APP_FUNCTIONS",
            reason = "工具类 App，通过 AppFunctions 与 Gemini 集成",
            cost = "低（3-7 天）",
            providers = "AppFunctions API (模块A)"
        )
        AppCategory.ENTERTAINMENT -> Recommendation(
            approach = "LIVE_UPDATES",
            reason = "娱乐 App，通过 Live Updates 同步内容到手表",
            cost = "低（3-5 天）",
            providers = "Live Updates Bridge (模块C)"
        )
        AppCategory.PRODUCTIVITY -> Recommendation(
            approach = "APP_FUNCTIONS + LIVE_UPDATES",
            reason = "AppFunctions 语音控制 + Live Updates 数据同步",
            cost = "中（1-2 周）",
            providers = "AppFunctions API (模块A) + Live Updates (模块C)"
        )
        AppCategory.OTHER -> Recommendation(
            approach = "LIVE_UPDATES",
            reason = "其他类型，优先考虑 Live Updates 数据桥接",
            cost = "低（3-5 天）",
            providers = "Live Updates Bridge (模块C)"
        )
    }
}
