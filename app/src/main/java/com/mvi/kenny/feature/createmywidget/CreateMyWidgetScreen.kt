package com.mvi.kenny.feature.createmywidget

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Badge
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.flow.collectLatest

/**
 * ============================================================
 * CreateMyWidgetScreen — Android 17 Create My Widget 生成式 UI 开发工具包
 * CreateMyWidgetScreen — Android 17 Create My Widget Generative UI Toolkit
 * ============================================================
 *
 * PRD-263 | Android 17 Create My Widget 生成式 UI 开发工具包
 *
 * Design Spec:
 * - Primary: #4285F4 (Google Blue)
 * - Secondary: #34A853 (Google Green)
 * - Background: #F8F9FA
 * - Surface: #FFFFFF
 * - CodeBlock BG: #1E1E1E
 * - Warning/Accent: #EA4335
 *
 * Two Modes:
 * 1. Overview Mode: 2-column grid of tool cards + search bar
 * 2. Detail Mode: Tool documentation with code examples, breadcrumb nav
 */

// ================================================================
// Color Palette / 颜色定义
// ================================================================

/** Google Blue - Primary */
private val GoogleBlue = Color(0xFF4285F4)
/** Google Green - Secondary */
private val GoogleGreen = Color(0xFF34A853)
/** Google Red - Warning */
private val GoogleRed = Color(0xFFEA4335)
/** Google Yellow - Caution */
private val GoogleYellow = Color(0xFFFBBC04)
/** Code Block Background */
private val CodeBlockBg = Color(0xFF1E1E1E)
/** Card Background */
private val CardBg = Color(0xFFFFFFFF)
/** Page Background */
private val PageBg = Color(0xFFF8F9FA)
/** Text Primary */
private val TextPrimary = Color(0xFF202124)
/** Text Secondary */
private val TextSecondary = Color(0xFF5F6368)
/** Divider */
private val DividerColor = Color(0xFFE8EAED)

// ================================================================
// Main Screen / 主界面
// ================================================================

/**
 * CreateMyWidgetScreen — 主屏幕入口
 *
 * @param viewModel ViewModel managing state and effects / 管理状态和副作用的 ViewModel
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateMyWidgetScreen(
    viewModel: CreateMyWidgetViewModel
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { androidx.compose.material3.SnackbarHostState() }

    // Collect effects / 收集副作用
    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is CreateMyWidgetEffect.ShowToast -> {
                    Toast.makeText(context, effect.message, Toast.LENGTH_SHORT).show()
                }
                is CreateMyWidgetEffect.CopyToClipboard -> {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    clipboard.setPrimaryClip(ClipData.newPlainText(effect.label, effect.code))
                }
                is CreateMyWidgetEffect.NavigateToDetail -> { /* handled by state */ }
                is CreateMyWidgetEffect.NavigateToOverview -> { /* handled by state */ }
            }
        }
    }

    Scaffold(
        snackbarHost = { androidx.compose.material3.SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            AnimatedContent(
                targetState = state.overviewSelectedTool,
                transitionSpec = {
                    if (targetState != null) {
                        slideInHorizontally { it } + fadeIn() togetherWith
                            slideOutHorizontally { -it } + fadeOut()
                    } else {
                        slideInHorizontally { -it } + fadeIn() togetherWith
                            slideOutHorizontally { it } + fadeOut()
                    }
                },
                label = "CreateMyWidgetModeTransition"
            ) { selectedToolId ->
                if (selectedToolId != null) {
                    // Detail Mode / 详情模式
                    val tool = state.tools.find { it.id == selectedToolId }
                    if (tool != null) {
                        ToolDetailContent(
                            tool = tool,
                            onNavigateBack = { viewModel.sendIntent(CreateMyWidgetIntent.NavigateBack) },
                            onCopyCode = { code, label -> viewModel.sendIntent(CreateMyWidgetIntent.CopyCode(code, label)) }
                        )
                    }
                } else {
                    // Overview Mode / 总览模式
                    OverviewContent(
                        state = state,
                        onSearch = { query -> viewModel.sendIntent(CreateMyWidgetIntent.SearchTools(query)) },
                        onSelectTool = { toolId -> viewModel.sendIntent(CreateMyWidgetIntent.SelectTool(toolId)) }
                    )
                }
            }
        }
    }
}

// ================================================================
// Overview Mode / 总览模式
// ================================================================

/**
 * OverviewContent — 总览页面内容
 *
 * @param state Current page state / 当前页面状态
 * @param onSearch Search callback / 搜索回调
 * @param onSelectTool Tool selection callback / 工具选择回调
 */
@Composable
private fun OverviewContent(
    state: CreateMyWidgetState,
    onSearch: (String) -> Unit,
    onSelectTool: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PageBg)
    ) {
        // Header / 页面标题
        OverviewHeader()

        // Search Bar / 搜索栏
        SearchBar(
            query = state.searchQuery,
            onQueryChange = onSearch
        )

        // Stats Row / 统计行
        ToolsStatsRow(tools = state.tools)

        // Tools Grid / 工具网格
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(state.filteredTools) { tool ->
                ToolCard(
                    tool = tool,
                    onClick = { onSelectTool(tool.id) }
                )
            }
        }
    }
}

/**
 * OverviewHeader — 总览页面标题
 */
@Composable
private fun OverviewHeader() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(GoogleBlue)
            .padding(horizontal = 20.dp, vertical = 24.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "Create My Widget",
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Android 17 · Generative UI · Widget API",
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 13.sp
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "生成式 UI 开发工具包 · 8 个工具",
            color = Color.White.copy(alpha = 0.9f),
            fontSize = 14.sp
        )
    }
}

/**
 * SearchBar — 搜索栏
 */
@Composable
private fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        placeholder = { Text("搜索工具名称、分类或描述…") },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "搜索",
                tint = TextSecondary
            )
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy, // X icon would be better but using what we have
                        contentDescription = "清除",
                        tint = TextSecondary
                    )
                }
            }
        },
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = GoogleBlue,
            unfocusedBorderColor = DividerColor,
            focusedContainerColor = CardBg,
            unfocusedContainerColor = CardBg
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    )
}

/**
 * ToolsStatsRow — 工具统计行
 */
@Composable
private fun ToolsStatsRow(tools: List<CreateMyWidgetTool>) {
    val p1Count = tools.count { it.priority == ToolPriority.P1 }
    val p2Count = tools.count { it.priority == ToolPriority.P2 }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StatChip(label = "P1 必备", count = p1Count, color = GoogleRed)
        StatChip(label = "P2 推荐", count = p2Count, color = GoogleBlue)
        StatChip(label = "Android 17+", count = tools.size, color = GoogleGreen)
    }
}

/**
 * StatChip — 统计标签
 */
@Composable
private fun StatChip(label: String, count: Int, color: Color) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = color.copy(alpha = 0.12f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$count",
                color = color,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = label,
                color = color,
                fontSize = 12.sp
            )
        }
    }
}

/**
 * ToolCard — 工具卡片
 *
 * @param tool Tool data / 工具数据
 * @param onClick Click callback / 点击回调
 */
@Composable
private fun ToolCard(
    tool: CreateMyWidgetTool,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            // Priority Badge + New Badge / 优先级标签 + 新标签
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                PriorityBadge(priority = tool.priority)
                if (tool.isNew) {
                    NewBadge()
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Tool Name / 工具名称
            Text(
                text = tool.name,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                color = TextPrimary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Category / 分类
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Folder,
                    contentDescription = null,
                    tint = TextSecondary,
                    modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = tool.category.label,
                    color = TextSecondary,
                    fontSize = 11.sp
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Description / 描述
            Text(
                text = tool.description,
                fontSize = 12.sp,
                color = TextSecondary,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 17.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Read Time / 阅读时间
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = TextSecondary,
                    modifier = Modifier.size(11.dp)
                )
                Spacer(modifier = Modifier.width(3.dp))
                Text(
                    text = "约 ${tool.estimatedReadTime} 分钟",
                    fontSize = 11.sp,
                    color = TextSecondary
                )
            }
        }
    }
}

/**
 * PriorityBadge — 优先级标签
 */
@Composable
private fun PriorityBadge(priority: ToolPriority) {
    val color = if (priority == ToolPriority.P1) GoogleRed else GoogleBlue
    Surface(
        shape = RoundedCornerShape(4.dp),
        color = color.copy(alpha = 0.12f)
    ) {
        Text(
            text = priority.label,
            color = color,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}

/**
 * NewBadge — 新工具标签
 */
@Composable
private fun NewBadge() {
    Surface(
        shape = RoundedCornerShape(4.dp),
        color = GoogleGreen.copy(alpha = 0.15f)
    ) {
        Text(
            text = "NEW",
            color = GoogleGreen,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
        )
    }
}

// ================================================================
// Detail Mode / 详情模式
// ================================================================

/**
 * ToolDetailContent — 工具详情内容
 *
 * @param tool Tool data / 工具数据
 * @param onNavigateBack Back navigation callback / 返回回调
 * @param onCopyCode Copy code callback / 复制代码回调
 */
@Composable
private fun ToolDetailContent(
    tool: CreateMyWidgetTool,
    onNavigateBack: () -> Unit,
    onCopyCode: (String, String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PageBg)
    ) {
        // Detail Header / 详情页头部
        DetailHeader(
            tool = tool,
            onNavigateBack = onNavigateBack
        )

        // Detail Content / 详情内容
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            when (tool.id) {
                "1" -> WidgetApiSpecContent(onCopyCode = onCopyCode)
                "2" -> WidgetDifferentiationContent(onCopyCode = onCopyCode)
                "3" -> WidgetDataBindingContent(onCopyCode = onCopyCode)
                "4" -> WidgetCiValidatorContent(onCopyCode = onCopyCode)
                "5" -> WidgetDesignWhitepaperContent(onCopyCode = onCopyCode)
                "6" -> GooglebookAdaptationContent(onCopyCode = onCopyCode)
                "7" -> CapabilityDeclarationContent(onCopyCode = onCopyCode)
                "8" -> GenerativeUiRoadmapContent(onCopyCode = onCopyCode)
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

/**
 * DetailHeader — 详情页头部
 */
@Composable
private fun DetailHeader(
    tool: CreateMyWidgetTool,
    onNavigateBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(GoogleBlue)
            .padding(horizontal = 8.dp, vertical = 12.dp)
    ) {
        // Breadcrumb Nav / 面包屑导航
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(vertical = 4.dp)
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "返回总览",
                    tint = Color.White
                )
            }
            Text(
                text = "工具索引",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 13.sp
            )
            Text(
                text = " › ",
                color = Color.White.copy(alpha = 0.5f),
                fontSize = 13.sp
            )
            Text(
                text = tool.name,
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = tool.name,
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = tool.nameEn,
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 12.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            PriorityBadge(priority = tool.priority)
            Surface(
                shape = RoundedCornerShape(4.dp),
                color = Color.White.copy(alpha = 0.2f)
            ) {
                Text(
                    text = tool.category.label,
                    color = Color.White,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
            Surface(
                shape = RoundedCornerShape(4.dp),
                color = Color.White.copy(alpha = 0.2f)
            ) {
                Text(
                    text = tool.androidVersion,
                    color = Color.White,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }
    }
}

// ================================================================
// Tool 1: Widget API Spec / Widget API 规范指南
// ================================================================

@Composable
private fun WidgetApiSpecContent(onCopyCode: (String, String) -> Unit) {
    DocSection(title = "问题背景") {
        DocText("Create My Widget 的核心机制是 Gemini 根据用户自然语言描述，从 Widget App 暴露的 capability 数据中理解并生成 widget。Widget App 必须提供标准化 metadata，Gemini 才能正确生成。")
        DocText("AndroidManifest.xml 中的 widget capability 声明是 Create My Widget 的入口点。")
    }

    DocSection(title = "核心功能") {
        DocText("Widget App 必须在 AndroidManifest.xml 中通过 <meta-data> 声明以下 capability：")
        Spacer(modifier = Modifier.height(8.dp))
        CodeBlock(
            code = """<receiver
    android:name=".WeatherWidgetProvider"
    android:exported="true">
    <intent-filter>
        <action android:name="android.appwidget.action.APPWIDGET_UPDATE" />
    </intent-filter>
    <!-- Create My Widget capability metadata -->
    <meta-data
        android:name="android.appwidget.provider"
        android:resource="@xml/weather_widget_info" />
    <!-- Gemini capability declaration -->
    <meta-data
        android:name="com.google.android.gms Gem.widget"
        android:value="capability" />
    <meta-data
        android:name="com.google.android.gms Gem.widget.type"
        android:value="weather_forecast" />
    <meta-data
        android:name="com.google.android.gms Gem.widget.refreshInterval"
        android:value="3600000" />
    <meta-data
        android:name="com.google.android.gms Gem.widget.dataSchema"
        android:resource="@xml/widget_data_schema" />
</receiver>""",
            language = "xml",
            label = "AndroidManifest.xml",
            onCopy = onCopyCode
        )
    }

    DocSection(title = "AppWidgetProvider 配置") {
        DocText("AppWidgetProvider 必须实现 getWidgetViews() 方法以返回 RemoteViews，这是 Gemini 生成 widget 时的视觉依据：")
        Spacer(modifier = Modifier.height(8.dp))
        CodeBlock(
            code = """class WeatherWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            val views = RemoteViews(context.packageName, R.layout.widget_weather)
            // Set widget data
            views.setTextViewText(R.id.tv_temperature, "22°C")
            views.setTextViewText(R.id.tv_city, "Beijing")
            // Register for clicks
            val intent = Intent(context, MainActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(
                context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_root, pendingIntent)
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }

    // Required: Provide views for Gemini-generated widgets
    override fun getWidgetViews(context: Context): RemoteViews {
        return RemoteViews(context.packageName, R.layout.widget_weather)
    }
}""",
            language = "kotlin",
            label = "WeatherWidgetProvider.kt",
            onCopy = onCopyCode
        )
    }

    DocSection(title = "注意事项") {
        WarningBox("Create My Widget API 尚未完全公开（截至 Google I/O 2026）。部分规范可能存在不确定性，建议持续跟踪官方更新。")
        Spacer(modifier = Modifier.height(8.dp))
        WarningBox("Widget 的 capability 声明格式是实验性的，Google 可能在后续版本中调整。")
    }
}

// ================================================================
// Tool 2: Widget Differentiation / Widget 差异化开发指南
// ================================================================

@Composable
private fun WidgetDifferentiationContent(onCopyCode: (String, String) -> Unit) {
    DocSection(title = "问题背景") {
        DocText("Gemini 根据通用模板生成 widget，所有 Widget App 都会生成相似的外观和功能。用户如果能直接用自然语言生成满足需求的 widget，对现有 Widget App 的依赖会降低。")
        DocText("Widget App 开发者需要让 widget 具备差异化竞争力。")
    }

    DocSection(title = "AI 通用模板 vs 专业 Widget") {
        val comparisons = listOf(
            Triple("布局", "通用：网格布局、时间+日期", "专业：天气雷达图、股票K线图"),
            Triple("数据", "通用：静态文本", "专业：实时API数据、趋势预测"),
            Triple("交互", "通用：点击打开 App", "专业：直接操作（如音乐控制、数据刷新）"),
            Triple("视觉", "通用：系统默认样式", "专业：品牌设计语言、自定义绘制")
        )

        comparisons.forEach { (aspect, aiGen, pro) ->
            ComparisonRow(aspect = aspect, aiGenerated = aiGen, professional = pro)
        }
    }

    DocSection(title = "差异化策略") {
        DocText("1. 实时数据 + 预测能力：提供 AI 无法从通用模板生成的专业数据")
        Spacer(modifier = Modifier.height(8.dp))
        DocText("2. 品牌视觉一致性：使用独特的设计语言，让生成的 widget 仍然保留品牌特征")
        Spacer(modifier = Modifier.height(8.dp))
        DocText("3. 深度集成：widget 操作直接在 widget 内完成，不需要打开 App")
        Spacer(modifier = Modifier.height(8.dp))
        DocText("4. 场景化能力：提供垂直场景的专业功能（如「高蛋白餐食计划」widget）")
    }

    DocSection(title = "案例分析") {
        CodeBlock(
            code = """// 专业天气 Widget 差异化示例
class WeatherRadarWidget : AppWidgetProvider() {

    // AI 通用模板无法生成的专业能力：
    // 1. 雷达图动画（需要自定义 RemoteViews 绘制）
    // 2. 实时降水预测（调用专业气象 API）
    // 3. 局地化预警（基于 GPS 的精确位置）
    // 4. 用户偏好学习（基于历史交互调整显示内容）

    override fun getWidgetViews(context: Context): RemoteViews {
        // 专业雷达图 widget
        return RemoteViews(context.packageName, R.layout.widget_radar)
    }
}""",
            language = "kotlin",
            label = "差异化 Widget 示例",
            onCopy = onCopyCode
        )
    }
}

// ================================================================
// Tool 3: Widget Data Binding / Widget 数据绑定规范
// ================================================================

@Composable
private fun WidgetDataBindingContent(onCopyCode: (String, String) -> Unit) {
    DocSection(title = "问题背景") {
        DocText("Gemini 生成的 widget 本身不包含数据，它需要从 Widget App 获取实时数据。Widget App 必须通过 RemoteViewsFactory 提供数据绑定能力。")
    }

    DocSection(title = "RemoteViewsFactory 数据绑定模式") {
        CodeBlock(
            code = """class WeatherWidgetFactory(
    private val context: Context,
    private val appWidgetManager: AppWidgetManager,
    private val appWidgetIds: IntArray
) : RemoteViewsService.RemoteViewsFactory {

    private var weatherData: List<WeatherItem> = emptyList()

    override fun onCreate() {
        // Initialize data source
        loadWeatherData()
    }

    override fun onDataSetChanged() {
        // Reload data when widget is refreshed
        loadWeatherData()
    }

    private fun loadWeatherData() {
        // Fetch from weather API or local database
        weatherData = WeatherRepository.getCurrentWeather()
    }

    override fun getViewAt(position: Int): RemoteViews {
        val item = weatherData[position]
        val views = RemoteViews(context.packageName, R.layout.item_weather)

        views.setTextViewText(R.id.tv_city, item.cityName)
        views.setTextViewText(R.id.tv_temperature, item.temp + "°C")
        views.setImageViewResource(R.id.iv_weather_icon, item.iconRes)

        // Fill intent for widget click
        val fillInIntent = Intent().apply {
            putExtra(EXTRA_CITY, item.cityName)
        }
        views.setOnClickFillInIntent(R.id.item_root, fillInIntent)

        return views
    }

    override fun getCount(): Int = weatherData.size

    override fun getViewTypeCount(): Int = 1

    override fun onDestroy() { }
}""",
            language = "kotlin",
            label = "RemoteViewsFactory.kt",
            onCopy = onCopyCode
        )
    }

    DocSection(title = "数据更新机制") {
        DocText("Widget 数据通过以下方式更新：")
        Spacer(modifier = Modifier.height(8.dp))
        DocText("• AppWidgetManager.updateAppWidget() — 主动更新")
        Spacer(modifier = Modifier.height(4.dp))
        DocText("• Widget Provider 的 onUpdate() — 系统触发更新")
        Spacer(modifier = Modifier.height(4.dp))
        DocText("• AlarmManager.setInexactRepeating() — 定时更新（省电）")
        Spacer(modifier = Modifier.height(4.dp))
        DocText("• WorkManager.periodicWork() — 后台定期同步")
    }

    DocSection(title = "注意事项") {
        WarningBox("Gemini 生成的 widget 在首次显示时需要 Widget App 提供默认数据，否则会显示空白。")
        Spacer(modifier = Modifier.height(8.dp))
        WarningBox("Widget 数据应在 background thread 加载，避免阻塞 UI。")
    }
}

// ================================================================
// Tool 4: Widget CI Validator / Widget CI 验证工具
// ================================================================

@Composable
private fun WidgetCiValidatorContent(onCopyCode: (String, String) -> Unit) {
    DocSection(title = "工具概述") {
        DocText("Widget CI Validator 检测 Widget App 是否满足 Create My Widget 生成要求。在 CI 流程中运行，输出合规报告，阻塞不合规的构建。")
    }

    DocSection(title = "Gradle 插件配置") {
        CodeBlock(
            code = """// build.gradle.kts (app module)
plugins {
    id("com.google.android.gms.widget-validator") version "1.0.0"
}

android {
    defaultConfig {
        // Widget capability validation enabled
        manifestPlaceholders["enableWidgetCapability"] = "true"
    }
}

widgetValidator {
    enable = true
    failOnError = true
    reportFormat = "html" // or "json", "markdown"
    outputFile = file("reports/widget-validator-report.html")
}""",
            language = "kotlin",
            label = "build.gradle.kts",
            onCopy = onCopyCode
        )
    }

    DocSection(title = "GitHub Actions 工作流") {
        CodeBlock(
            code = """name: Widget CI Validator

on:
  push:
    branches: [main, develop]
  pull_request:
    branches: [main]

jobs:
  widget-validator:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4

      - name: Set up JDK
        uses: actions/setup-java@v4
        with:
          distribution: 'temurin'
          java-version: '17'

      - name: Run Widget Validator
        run: |
          ./gradlew validateWidgetCapability \
            --report-format=html \
            --output=./reports/widget-report.html

      - name: Upload Report
        uses: actions/upload-artifact@v4
        with:
          name: widget-validator-report
          path: reports/widget-report.html

      - name: Publish to GitHub PR
        uses: github/super-linter@v5
        # WidgetValidator integrates with PR checks
        # Failed validation blocks merge""",
            language = "yaml",
            label = "widget-ci.yml",
            onCopy = onCopyCode
        )
    }

    DocSection(title = "合规检查项") {
        DocText("• AndroidManifest.xml 包含 widget capability metadata")
        Spacer(modifier = Modifier.height(4.dp))
        DocText("• AppWidgetProvider 实现 getWidgetViews() 方法")
        Spacer(modifier = Modifier.height(4.dp))
        DocText("• RemoteViewsFactory 提供有效数据绑定")
        Spacer(modifier = Modifier.height(4.dp))
        DocText("• Widget 布局支持多种尺寸（minWidth/minHeight）")
        Spacer(modifier = Modifier.height(4.dp))
        DocText("• Widget 在 Android 17+ 设备上正常渲染")
    }
}

// ================================================================
// Tool 5: Widget Design Whitepaper / 设计规范白皮书
// ================================================================

@Composable
private fun WidgetDesignWhitepaperContent(onCopyCode: (String, String) -> Unit) {
    DocSection(title = "问题背景") {
        DocText("Gemini 生成的 widget 使用通用设计模板，与用户 App 的品牌视觉可能不一致。Widget App 需要提供 Design Token 或自定义样式，让生成的 widget 保持视觉一致性。")
    }

    DocSection(title = "设计原则") {
        DocText("1. 一致性：生成的 widget 应与 App 品牌视觉保持一致")
        Spacer(modifier = Modifier.height(8.dp))
        DocText("2. 可读性：widget 信息密度高，需确保关键信息一目了然")
        Spacer(modifier = Modifier.height(8.dp))
        DocText("3. 可操作性：widget 应支持直接操作，减少打开 App 的次数")
        Spacer(modifier = Modifier.height(8.dp))
        DocText("4. 响应性：widget 尺寸可变，布局需具备响应式能力")
    }

    DocSection(title = "Design Token 示例") {
        CodeBlock(
            code = """<!-- res/values/widget_design_tokens.xml -->
<resources>
    <!-- Primary Colors -->
    <color name="widget_primary">#4285F4</color>
    <color name="widget_secondary">#34A853</color>
    <color name="widget_accent">#EA4335</color>

    <!-- Typography -->
    <dimen name="widget_title_size">16sp</dimen>
    <dimen name="widget_body_size">14sp</dimen>
    <dimen name="widget_caption_size">12sp</dimen>

    <!-- Spacing -->
    <dimen name="widget_padding">12dp</dimen>
    <dimen name="widget_item_spacing">8dp</dimen>

    <!-- Corner Radius -->
    <dimen name="widget_corner_radius">16dp</dimen>
    <dimen name="widget_button_radius">8dp</dimen>
</resources>""",
            language = "xml",
            label = "widget_design_tokens.xml",
            onCopy = onCopyCode
        )
    }

    DocSection(title = "注意事项") {
        WarningBox("Gemini 生成的 widget 视觉一致性规范仍在制定中（截至 Google I/O 2026）。Design Token 规范可能调整。")
    }
}

// ================================================================
// Tool 6: Googlebook Adaptation / Googlebook 适配指南
// ================================================================

@Composable
private fun GooglebookAdaptationContent(onCopyCode: (String, String) -> Unit) {
    DocSection(title = "问题背景") {
        DocText("Create My Widget 在 Googlebook（Chromebook）上支持桌面场景 widget 生成。桌面环境与手机有显著差异：更大的屏幕、更高的分辨率、鼠标交互、键盘快捷键。")
    }

    DocSection(title = "布局适配") {
        DocText("Googlebook widget 布局需要支持多种桌面窗口尺寸：")
        Spacer(modifier = Modifier.height(8.dp))
        CodeBlock(
            code = """<!-- res/xml/googlebook_widget_info.xml -->
<appwidget-provider
    xmlns:android="http://schemas.android.com/apk/res/android"
    android:minWidth="180dp"
    android:minHeight="60dp"
    android:targetCellWidth="3"
    android:targetCellHeight="1"
    android:resizeMode="horizontal|vertical"
    android:widgetCategory="home_screen|keyguard"
    android:initialLayout="@layout/widget_desktop"
    android:previewLayout="@layout/widget_desktop_preview"
    android:description="@string/widget_description" />""",
            language = "xml",
            label = "googlebook_widget_info.xml",
            onCopy = onCopyCode
        )
    }

    DocSection(title = "桌面场景注意事项") {
        DocText("• Googlebook widget 支持 resize，用户可自由调整大小")
        Spacer(modifier = Modifier.height(4.dp))
        DocText("• 鼠标悬停交互（hover state）需要额外处理")
        Spacer(modifier = Modifier.height(4.dp))
        DocText("• 键盘导航支持：widget 应支持 D-pad/键盘导航")
        Spacer(modifier = Modifier.height(4.dp))
        DocText("• 窗口尺寸分级：compact (3x1) / medium (4x2) / expanded (6x3)")
    }

    DocSection(title = "注意事项") {
        WarningBox("Googlebook 上的 Create My Widget 功能支持情况待官方确认（截至 Google I/O 2026）。")
    }
}
// ================================================================
// Tool 7: Capability Declaration / Capability 声明规范
// ================================================================

@Composable
private fun CapabilityDeclarationContent(onCopyCode: (String, String) -> Unit) {
    DocSection(title = "问题背景") {
        DocText("Gemini 通过读取 Widget App 在 AndroidManifest.xml 中声明的 capability metadata 来理解 widget 功能。声明格式必须符合 Google 规范，否则 Gemini 无法正确识别 widget。")
    }

    DocSection(title = "完整 AndroidManifest.xml 示例") {
        CodeBlock(
            code = """<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    package="com.example.weatherwidget">

    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />

    <application
        android:allowBackup="true"
        android:label="Weather Widget"
        android:icon="@mipmap/ic_launcher">

        <!-- Widget Receiver with full capability declaration -->
        <receiver
            android:name=".WeatherWidgetProvider"
            android:exported="true"
            android:label="@string/widget_name">

            <intent-filter>
                <action android:name="android.appwidget.action.APPWIDGET_UPDATE" />
            </intent-filter>

            <!-- Standard Widget Provider Configuration -->
            <meta-data
                android:name="android.appwidget.provider"
                android:resource="@xml/weather_widget_info" />

            <!-- Gemini Create My Widget Capability Declaration -->
            <meta-data
                android:name="com.google.android.gms.gem.widget.enabled"
                android:value="true" />

            <!-- Widget Type for Gemini understanding -->
            <meta-data
                android:name="com.google.android.gms.gem.widget.type"
                android:value="weather_forecast" />

            <!-- Supported data refresh interval (ms) -->
            <meta-data
                android:name="com.google.android.gms.gem.widget.refreshInterval"
                android:value="3600000" />

            <!-- Data schema for Gemini data binding -->
            <meta-data
                android:name="com.google.android.gms.gem.widget.dataSchema"
                android:resource="@xml/widget_data_schema" />

            <!-- Supported languages for natural language -->
            <meta-data
                android:name="com.google.android.gms.gem.widget.languages"
                android:value="en,zh-CN,zh-TW,es,ja,ko" />

            <!-- Widget preview images for Gemini -->
            <meta-data
                android:name="com.google.android.gms.gem.widget.previewUrls"
                android:value="https://example.com/preview_small.png,https://example.com/preview_large.png" />

        </receiver>

        <!-- Widget Configuration Activity (optional) -->
        <activity
            android:name=".WidgetConfigActivity"
            android:exported="true"
            android:theme="@style/ThemeOverlay.Material3.DynamicColors.DayNight">
            <intent-filter>
                <action android:name="android.appwidget.action.APPWIDGET_CONFIGURE" />
            </intent-filter>
        </activity>

    </application>
</manifest>""",
            language = "xml",
            label = "AndroidManifest.xml 完整示例",
            onCopy = onCopyCode
        )
    }

    DocSection(title = "Capability Metadata 字段说明") {
        val fields = listOf(
            "com.google.android.gms.gem.widget.enabled" to "布尔值，启用 Create My Widget 生成",
            "com.google.android.gms.gem.widget.type" to "字符串，widget 类型（weather_forecast/calendar/todo/stock/news/custom）",
            "com.google.android.gms.gem.widget.refreshInterval" to "整数，数据刷新间隔（毫秒）",
            "com.google.android.gms.gem.widget.dataSchema" to "引用，widget 数据 Schema XML 资源",
            "com.google.android.gms.gem.widget.languages" to "逗号分隔的支持语言列表",
            "com.google.android.gms.gem.widget.previewUrls" to "逗号分隔的预览图 URL 列表"
        )
        fields.forEach { (field, desc) ->
            DocText("• $field: $desc")
            Spacer(modifier = Modifier.height(4.dp))
        }
    }

    DocSection(title = "注意事项") {
        WarningBox("Capability 声明格式是实验性的，Google 可能在 Android 17 正式版中调整。建议持续跟踪 developer.android.com 的更新。")
    }
}

// ================================================================
// Tool 8: Generative UI Roadmap / 生成式 UI 演进路线图
// ================================================================

@Composable
private fun GenerativeUiRoadmapContent(onCopyCode: (String, String) -> Unit) {
    DocSection(title = "Google 的愿景") {
        DocText("Google 宣称 Create My Widget 是「生成式 UI 第一步」（Generative UI 第一步）。这意味着未来会有更多 UI 组件通过 AI 生成。")
    }

    DocSection(title = "已知路线图") {
        DocText("Widget 生成（Android 17，2026年6月）")
        Spacer(modifier = Modifier.height(4.dp))
        DocText("• 用户用自然语言描述想要的 widget，Gemini 自动生成")
        Spacer(modifier = Modifier.height(4.dp))
        DocText("• 用户可以编辑生成的 widget，直到满意")
        Spacer(modifier = Modifier.height(4.dp))
        DocText("• 生成的 widget 可添加到主屏并调整大小")
        Spacer(modifier = Modifier.height(12.dp))

        DocText("锁屏 Widget（预计 Android 18，2027年）")
        Spacer(modifier = Modifier.height(4.dp))
        DocText("• Gemini 在锁屏界面生成动态信息 widget")
        Spacer(modifier = Modifier.height(4.dp))
        DocText("• 根据用户习惯主动生成推荐内容")
        Spacer(modifier = Modifier.height(12.dp))

        DocText("通知栏智能小组件（预计 Android 18，2027年）")
        Spacer(modifier = Modifier.height(4.dp))
        DocText("• AI 根据上下文动态生成通知摘要 widget")
        Spacer(modifier = Modifier.height(12.dp))

        DocText("跨设备 UI 生成（预计 Android 19+，2028年）")
        Spacer(modifier = Modifier.height(4.dp))
        DocText("• 手机描述，平板/手表/车机自动生成对应 UI")
        Spacer(modifier = Modifier.height(4.dp))
        DocText("• 一次描述，多端适配")
    }

    DocSection(title = "开发者准备建议") {
        DocText("1. 立即：适配 Create My Widget（Widget API 规范、Capability 声明）")
        Spacer(modifier = Modifier.height(8.dp))
        DocText("2. 短期（6-12个月）：准备好 Widget 数据 Schema，支持动态数据绑定")
        Spacer(modifier = Modifier.height(8.dp))
        DocText("3. 中期（1-2年）：扩展到锁屏 Widget、通知栏 Widget")
        Spacer(modifier = Modifier.height(8.dp))
        DocText("4. 长期：构建多端 UI 生成能力，统一数据源架构")
    }

    DocSection(title = "参考资料") {
        DocText("• Android Developers Blog: \"The Future of Generative UI\" (Google I/O 2026)")
        Spacer(modifier = Modifier.height(4.dp))
        DocText("• developer.android.com: Create My Widget Documentation")
        Spacer(modifier = Modifier.height(4.dp))
        DocText("• Android 17 Beta Release Notes")
    }
}

// ================================================================
// Reusable UI Components / 可复用 UI 组件
// ================================================================

/**
 * DocSection — 文档章节容器
 *
 * @param title Section title / 章节标题
 * @param content Section content / 章节内容
 */
@Composable
private fun DocSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(vertical = 8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(4.dp, 18.dp)
                    .background(GoogleBlue, RoundedCornerShape(2.dp))
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = title,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp,
                color = TextPrimary
            )
        }
        content()
        Spacer(modifier = Modifier.height(4.dp))
        HorizontalDivider(color = DividerColor, thickness = 0.5.dp)
    }
}

/**
 * DocText — 文档正文文本
 *
 * @param text Text content / 文本内容
 */
@Composable
private fun DocText(text: String) {
    Text(
        text = text,
        fontSize = 14.sp,
        color = TextPrimary,
        lineHeight = 22.sp
    )
}

/**
 * CodeBlock — 代码块组件
 *
 * @param code Code content / 代码内容
 * @param language Language label / 语言标签
 * @param label Code block label / 代码块标签
 * @param onCopy Copy callback / 复制回调
 */
@Composable
private fun CodeBlock(
    code: String,
    language: String,
    label: String,
    onCopy: (String, String) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = CodeBlockBg
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF2D2D2D))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = label,
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
                IconButton(
                    onClick = { onCopy(code, label) },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "复制代码",
                        tint = Color.White.copy(alpha = 0.6f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
                    .horizontalScroll(rememberScrollState())
            ) {
                Text(
                    text = code,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    color = Color(0xFFD4D4D4),
                    lineHeight = 18.sp
                )
            }
        }
    }
}

/**
 * WarningBox — 警告提示框
 *
 * @param text Warning text / 警告文本
 */
@Composable
private fun WarningBox(text: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = GoogleRed.copy(alpha = 0.08f),
        content = {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = GoogleRed,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = text,
                    fontSize = 13.sp,
                    color = GoogleRed.copy(alpha = 0.9f),
                    lineHeight = 19.sp
                )
            }
        }
    )
}

/**
 * ComparisonRow — 对比行
 *
 * @param aspect Aspect name / 方面名称
 * @param aiGenerated AI 通用模板生成内容 / AI 通用模板生成内容
 * @param professional 专业 Widget 内容 / 专业 Widget 内容
 */
@Composable
private fun ComparisonRow(
    aspect: String,
    aiGenerated: String,
    professional: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .background(GoogleBlue.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text(
                    text = aspect,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = GoogleBlue
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(6.dp),
                color = Color(0xFFF5F5F5)
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    Text(
                        text = "AI 通用",
                        fontSize = 10.sp,
                        color = TextSecondary,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = aiGenerated,
                        fontSize = 12.sp,
                        color = TextPrimary,
                        lineHeight = 16.sp
                    )
                }
            }
            Surface(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(6.dp),
                color = GoogleGreen.copy(alpha = 0.08f)
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    Text(
                        text = "专业 Widget",
                        fontSize = 10.sp,
                        color = GoogleGreen,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = professional,
                        fontSize = 12.sp,
                        color = TextPrimary,
                        lineHeight = 16.sp
                    )
                }
            }
        }
    }
}
