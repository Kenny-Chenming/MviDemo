package com.mvi.kenny.feature.xrglassestoolkit

/**
 * ============================================================
 * XRGlassesToolkitScreen — Android XR AI Glasses 开发工具包主屏幕
 * XRGlassesToolkitScreen — Android XR AI Glasses Dev Toolkit Main Screen
 * ============================================================
 *
 * PRD-258 | Android XR AI Glasses 开发工具包
 * Ref: memory/agency/designs/PRD-258-Android-XR-AI-Glasses开发工具包.md
 *
 * Architecture: MVI (Model-View-Intent)
 * - Receives XRGlassesToolkitState and renders UI
 * - Sends XRGlassesToolkitIntent to ViewModel on user interaction
 * - Listens for XRGlassesToolkitEffect for side effects
 * —————————————————————————————————————————————————————
 *
 * Design specs:
 * - 5 Tab bottom navigation: 入门指南 / Projected API / Compose Glimmer / Emulator工作流 / 隐私合规
 * - Dark theme only (#0D1117 background, #4FC3F7 primary)
 * - XR 科技感风格
 */

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.mvi.kenny.base.TopBarConfig
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.launch

// ============================================================
// XR Color Palette (Dark Theme XR Style) / XR 配色方案（深色主题）
// ============================================================

private object XRColors {
    val Background = Color(0xFF0D1117)
    val Surface = Color(0xFF161B22)
    val SurfaceVariant = Color(0xFF21262D)
    val Primary = Color(0xFF4FC3F7)        // XR Blue / XR 蓝
    val Secondary = Color(0xFF7C4DFF)       // AI Purple / AI 紫
    val Tertiary = Color(0xFF69F0AE)        // Glimmer Green / Glimmer 绿
    val OnBackground = Color(0xFFE6EDF3)
    val OnSurfaceVariant = Color(0xFF8B949E)
    val Error = Color(0xFFFF6B6B)
    val Success = Color(0xFF69F0AE)
    val CodeBackground = Color(0xFF1E1E1E)
    val CodeText = Color(0xFF79C0FF)
}

// ============================================================
// XR Glasses Toolkit Screen / Android XR AI Glasses 开发工具包主屏幕
// ============================================================

/**
 * XR Glasses Toolkit main screen
 * Android XR AI Glasses 开发工具包主屏幕
 *
 * @param viewModel XRGlassesToolkitViewModel instance / XRGlassesToolkitViewModel 实例
 * @param onUpdateTopBar Callback to update TopBar config / 更新 TopBar 配置的回调
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun XRGlassesToolkitScreen(
    viewModel: XRGlassesToolkitViewModel,
    onUpdateTopBar: (TopBarConfig) -> Unit
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Update TopBar config / 更新 TopBar 配置
    LaunchedEffect(Unit) {
        onUpdateTopBar(
            TopBarConfig(
                title = "Android XR AI Glasses",
                actions = emptyList()
            )
        )
    }

    // Listen for side effects / 监听副作用
    LaunchedEffect(Unit) {
        viewModel.effect.collect { e ->
            when (e) {
                is XRGlassesToolkitEffect.CopyToClipboard -> {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    clipboard.setPrimaryClip(ClipData.newPlainText("code", e.text))
                    Toast.makeText(context, "已复制到剪贴板", Toast.LENGTH_SHORT).show()
                }
                is XRGlassesToolkitEffect.ShowSnackbar -> {
                    Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show()
                }
                is XRGlassesToolkitEffect.DownloadFile -> {
                    Toast.makeText(context, "下载: ${e.fileName}", Toast.LENGTH_SHORT).show()
                }
                is XRGlassesToolkitEffect.ExecuteCommand -> {
                    Toast.makeText(context, "执行: ${e.command}", Toast.LENGTH_SHORT).show()
                }
                is XRGlassesToolkitEffect.ShareFile -> {
                    Toast.makeText(context, "报告已生成", Toast.LENGTH_SHORT).show()
                }
                else -> {}
            }
        }
    }

    // Load initial data on first composition / 首次组合时加载初始数据
    LaunchedEffect(Unit) {
        viewModel.sendIntent(XRGlassesToolkitIntent.LoadData)
    }

    // Bottom navigation tabs / 底部导航 Tab
    val tabItems = listOf(
        "入门指南" to Icons.Default.PlayArrow,
        "Projected API" to Icons.Default.ContentCopy,
        "Glimmer" to Icons.Default.CheckCircle,
        "模拟器" to Icons.Default.PlayArrow,
        "隐私合规" to Icons.Default.Warning
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(XRColors.Background)
    ) {
        // Top App Bar / 顶部导航栏
        TopAppBar(
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Android XR AI Glasses",
                        color = XRColors.OnBackground,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    // Device type selector / 设备类型选择器
                    DeviceTypeDropdown(
                        selectedType = state.deviceType,
                        onTypeSelected = { viewModel.sendIntent(XRGlassesToolkitIntent.SelectDevice(it)) }
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = XRColors.Surface
            )
        )

        // Tab Content / Tab 内容
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            when (state.selectedTab) {
                0 -> GettingStartedTab(
                    state = state,
                    onIntent = viewModel::sendIntent
                )
                1 -> ProjectedApiTab(
                    state = state,
                    onIntent = viewModel::sendIntent
                )
                2 -> ComposeGlimmerTab(
                    state = state,
                    onIntent = viewModel::sendIntent
                )
                3 -> EmulatorWorkflowTab(
                    state = state,
                    onIntent = viewModel::sendIntent
                )
                4 -> PrivacyComplianceTab(
                    state = state,
                    onIntent = viewModel::sendIntent
                )
            }
        }

        // Bottom Navigation Bar / 底部导航栏
        NavigationBar(
            containerColor = XRColors.Surface
        ) {
            tabItems.forEachIndexed { index, (title, icon) ->
                NavigationBarItem(
                    icon = { Icon(icon, contentDescription = title) },
                    label = { Text(title, fontSize = 10.sp) },
                    selected = state.selectedTab == index,
                    onClick = { viewModel.sendIntent(XRGlassesToolkitIntent.SelectTab(index)) }
                )
            }
        }
    }

    // Full-screen preview dialog for Tab 2 / Tab 2 全屏预览对话框
    state.fullScreenPreviewComponent?.let { component ->
        GlimmerFullScreenPreviewDialog(
            component = component,
            onDismiss = { viewModel.sendIntent(XRGlassesToolkitIntent.DismissFullScreenPreview) }
        )
    }
}

// ============================================================
// Tab 0: Getting Started / 入门指南
// ============================================================

/**
 * Getting Started tab - HelloGlasses 示例 + 快速上手流程
 * Tab 0: HelloGlasses example + quick start guide
 */
@Composable
private fun GettingStartedTab(
    state: XRGlassesToolkitState,
    onIntent: (XRGlassesToolkitIntent) -> Unit
) {
    val steps = listOf(
        StepData(
            number = 1,
            title = "环境准备",
            description = "安装 Android Studio Ladybug+，下载 AI Glasses Emulator 系统镜像。",
            code = "# 检查 Android Studio 版本\nandroid-studio --version\n\n# 启动 AI Glasses Emulator\nadb emu avd start ai_glasses"
        ),
        StepData(
            number = 2,
            title = "创建 HelloGlasses 项目",
            description = "在 Android Studio 中创建新项目，选择 AI Glasses 模板。",
            code = "// MainActivity.kt\nclass MainActivity : ComponentActivity() {\n    override fun onCreate(savedInstanceState: Bundle?) {\n        super.onCreate(savedInstanceState)\n        setContent {\n            GlimmerApp {\n                HelloGlassesScreen()\n            }\n        }\n    }\n}"
        ),
        StepData(
            number = 3,
            title = "集成 Jetpack Projected",
            description = "添加 Projected 库依赖，建立手机与眼镜的连接。",
            code = "// build.gradle.kts\ndependencies {\n    implementation(\"androidx.projected:projected:1.0.0\")\n}"
        ),
        StepData(
            number = 4,
            title = "使用 Compose Glimmer 构建 UI",
            description = "使用 Glimmer 专用组件构建眼镜端 UI，注意 16:9 屏幕比例。",
            code = "@Composable\nfun HelloGlassesScreen() {\n    GlimmerText(\n        text = \"Hello, AI Glasses!\",\n        style = GlimmerTextStyle.Headline\n    )\n}"
        ),
        StepData(
            number = 5,
            title = "运行和调试",
            description = "在 Emulator 中运行 App，使用 Glimmer Inspector 调试 UI。",
            code = "# 安装到眼镜模拟器\nadb install app-debug.apk\n\n# 查看日志\nadb logcat | grep Glimmer"
        )
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Header / 标题区
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = XRColors.Surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "🚀 HelloGlasses 快速上手",
                        style = MaterialTheme.typography.headlineSmall,
                        color = XRColors.Primary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "5 步创建你的第一个 AI Glasses App",
                        color = XRColors.OnSurfaceVariant
                    )
                }
            }
        }

        // Steps / 步骤列表
        items(steps.size) { index ->
            val step = steps[index]
            val isExpanded = state.expandedStepIndex == index

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .animateContentSize()
                    .clickable { onIntent(XRGlassesToolkitIntent.ToggleStep(index)) },
                colors = CardDefaults.cardColors(
                    containerColor = if (isExpanded) XRColors.SurfaceVariant else XRColors.Surface
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Step number circle / 步骤编号圆圈
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(XRColors.Primary, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${step.number}",
                                color = XRColors.Background,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = step.title,
                            style = MaterialTheme.typography.titleMedium,
                            color = XRColors.OnBackground,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Text(
                        text = step.description,
                        color = XRColors.OnSurfaceVariant,
                        modifier = Modifier.padding(start = 44.dp, top = 4.dp)
                    )

                    // Expanded code block / 展开的代码块
                    if (isExpanded) {
                        Spacer(modifier = Modifier.height(12.dp))
                        CodeBlock(
                            code = step.code,
                            modifier = Modifier.padding(start = 44.dp)
                        )
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}

// ============================================================
// Tab 1: Projected API / Projected API 参考
// ============================================================

/**
 * Projected API tab - API 参考和代码生成
 * Tab 1: API reference and code generation
 */
@Composable
private fun ProjectedApiTab(
    state: XRGlassesToolkitState,
    onIntent: (XRGlassesToolkitIntent) -> Unit
) {
    var showBottomSheet by remember { mutableStateOf(false) }
    var selectedApi by remember { mutableStateOf<ProjectedApi?>(null) }

    LaunchedEffect(state.isBottomSheetVisible, state.selectedApi) {
        showBottomSheet = state.isBottomSheetVisible
        selectedApi = state.selectedApi
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Tab header / Tab 标题
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(XRColors.Surface)
                .padding(16.dp)
        ) {
            Column {
                Text(
                    text = "Jetpack Projected API 参考",
                    style = MaterialTheme.typography.titleLarge,
                    color = XRColors.Primary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "点击 API 查看完整代码，支持复制和下载",
                    color = XRColors.OnSurfaceVariant,
                    fontSize = 12.sp
                )
            }
        }

        // API List / API 列表
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(state.apiList) { api ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            selectedApi = api
                            showBottomSheet = true
                            onIntent(XRGlassesToolkitIntent.SelectApi(api))
                        },
                    colors = CardDefaults.cardColors(containerColor = XRColors.Surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = api.name,
                                style = MaterialTheme.typography.titleMedium,
                                color = XRColors.Primary,
                                fontFamily = FontFamily.Monospace
                            )
                            Row {
                                IconButton(
                                    onClick = { onIntent(XRGlassesToolkitIntent.CopyApiCode(api.id)) }
                                ) {
                                    Icon(
                                        Icons.Default.ContentCopy,
                                        contentDescription = "复制",
                                        tint = XRColors.OnSurfaceVariant
                                    )
                                }
                                IconButton(
                                    onClick = { onIntent(XRGlassesToolkitIntent.DownloadApiCode(api.id)) }
                                ) {
                                    Icon(
                                        Icons.Default.FileDownload,
                                        contentDescription = "下载",
                                        tint = XRColors.OnSurfaceVariant
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = api.description,
                            color = XRColors.OnSurfaceVariant,
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        CodeBlock(
                            code = api.codePreview,
                            modifier = Modifier.height(48.dp)
                        )
                    }
                }
            }
        }
    }

    // Bottom Sheet for API details / API 详情底部 Sheet
    if (showBottomSheet && selectedApi != null) {
        AlertDialog(
            onDismissRequest = {
                showBottomSheet = false
                onIntent(XRGlassesToolkitIntent.DismissBottomSheet)
            },
            title = {
                Text(
                    text = selectedApi!!.name,
                    fontFamily = FontFamily.Monospace,
                    color = XRColors.Primary
                )
            },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = selectedApi!!.description,
                        color = XRColors.OnSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "完整代码:",
                        color = XRColors.OnBackground,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    CodeBlock(code = selectedApi!!.fullCode)
                }
            },
            confirmButton = {
                TextButton(
                    onClick = { onIntent(XRGlassesToolkitIntent.CopyApiCode(selectedApi!!.id)) }
                ) {
                    Text("复制代码")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showBottomSheet = false
                        onIntent(XRGlassesToolkitIntent.DismissBottomSheet)
                    }
                ) {
                    Text("关闭")
                }
            },
            containerColor = XRColors.Surface
        )
    }
}

// ============================================================
// Tab 2: Compose Glimmer / Compose Glimmer 组件
// ============================================================

/**
 * Compose Glimmer tab - 眼镜端 UI 组件规范 + 预览工具
 * Tab 2: Glimmer UI component specs + preview tool
 */
@Composable
private fun ComposeGlimmerTab(
    state: XRGlassesToolkitState,
    onIntent: (XRGlassesToolkitIntent) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        // Sub Tab Row / 子 Tab 行
        TabRow(
            selectedTabIndex = state.glimmerTab.ordinal,
            containerColor = XRColors.Surface
        ) {
            GlimmerTab.entries.forEach { tab ->
                Tab(
                    selected = state.glimmerTab == tab,
                    onClick = { onIntent(XRGlassesToolkitIntent.SwitchGlimmerTab(tab)) },
                    text = {
                        Text(
                            text = tab.title,
                            color = if (state.glimmerTab == tab) XRColors.Primary else XRColors.OnSurfaceVariant
                        )
                    }
                )
            }
        }

        when (state.glimmerTab) {
            GlimmerTab.COMPONENTS -> {
                // Component preview grid / 组件预览网格
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier
                        .weight(1f)
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(state.componentGrid) { component ->
                        GlimmerComponentCard(
                            component = component,
                            onClick = {
                                onIntent(XRGlassesToolkitIntent.OpenFullScreenPreview(component))
                            }
                        )
                    }
                }
            }
            GlimmerTab.SPEC -> {
                // Design spec content / 设计规范内容
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        SpecSection(
                            title = "屏幕规格",
                            content = "AI Glasses 显示区域为 16:9（纵向模式），分辨率 640x360，OLED 材质。"
                        )
                    }
                    item {
                        SpecSection(
                            title = "色彩系统",
                            content = "背景: #0D1117，主色: #4FC3F7，辅助色: #7C4DFF，Glimmer 绿: #69F0AE。"
                        )
                    }
                    item {
                        SpecSection(
                            title = "字体规范",
                            content = "标题: 24sp Bold，正文: 16sp Medium，辅助: 12sp Regular。所有文字必须高对比度。"
                        )
                    }
                    item {
                        SpecSection(
                            title = "触控区域",
                            content = "最小触控目标: 48x48dp，眼镜端建议使用 64x64dp 以上确保可操作性。"
                        )
                    }
                }
            }
        }
    }
}

/**
 * Glimmer component card for preview grid
 * 组件预览网格中的 Glimmer 组件卡片
 */
@Composable
private fun GlimmerComponentCard(
    component: GlimmerComponent,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = XRColors.Surface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(XRColors.Primary.copy(alpha = 0.2f), XRColors.Secondary.copy(alpha = 0.2f))
                    )
                )
                .padding(12.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = component.name,
                    color = XRColors.Primary,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = component.category,
                    color = XRColors.OnSurfaceVariant,
                    fontSize = 10.sp
                )
            }
        }
    }
}

/**
 * Glimmer design spec section
 * Glimmer 设计规范段落
 */
@Composable
private fun SpecSection(title: String, content: String) {
    Card(
        colors = CardDefaults.cardColors(containerColor = XRColors.Surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                color = XRColors.Tertiary,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = content,
                color = XRColors.OnSurfaceVariant,
                fontSize = 13.sp
            )
        }
    }
}

/**
 * Full-screen preview dialog for Glimmer component
 * Glimmer 组件全屏预览对话框
 */
@Composable
private fun GlimmerFullScreenPreviewDialog(
    component: GlimmerComponent,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .aspectRatio(16f / 9f),
            shape = RoundedCornerShape(16.dp),
            color = XRColors.Background
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                // Close button / 关闭按钮
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.TopEnd)
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "关闭",
                        tint = XRColors.OnSurfaceVariant
                    )
                }

                // Component preview / 组件预览
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = component.name,
                        color = XRColors.Primary,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = component.description,
                        color = XRColors.OnSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "分类: ${component.category}",
                        color = XRColors.Tertiary,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

// ============================================================
// Tab 3: Emulator Workflow / 模拟器工作流
// ============================================================

/**
 * Emulator Workflow tab - AI Glasses Emulator 调试指南 + 模拟输入
 * Tab 3: AI Glasses Emulator debugging guide + simulation input
 */
@Composable
private fun EmulatorWorkflowTab(
    state: XRGlassesToolkitState,
    onIntent: (XRGlassesToolkitIntent) -> Unit
) {
    val steps = listOf(
        EmulatorStep(1, "安装 AI Glasses Emulator", "下载并安装 Android Studio Ladybug+。"),
        EmulatorStep(2, "创建 AVD 设备", "在 AVD Manager 中创建 AI Glasses 虚拟设备。"),
        EmulatorStep(3, "启动模拟器", "点击下方按钮启动 AI Glasses Emulator。"),
        EmulatorStep(4, "安装 HelloGlasses", "使用 adb install 安装你的 App 到模拟器。"),
        EmulatorStep(5, "调试和验证", "使用 Glimmer Inspector 检查 UI 渲染效果。")
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header / 标题区
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = XRColors.Surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "AI Glasses Emulator 工作流",
                    style = MaterialTheme.typography.titleLarge,
                    color = XRColors.Primary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val statusColor = when (state.emulatorStatus) {
                        EmulatorStatus.RUNNING -> XRColors.Success
                        EmulatorStatus.STARTING -> XRColors.Primary
                        EmulatorStatus.ERROR -> XRColors.Error
                        EmulatorStatus.NOT_STARTED -> XRColors.OnSurfaceVariant
                    }
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(statusColor, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "模拟器状态: ${state.emulatorStatus.displayName}",
                        color = XRColors.OnSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Stepper / 步骤指引
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            steps.forEach { step ->
                StepperItemView(
                    step = step,
                    isActive = state.currentStep == step.number - 1,
                    isCompleted = state.currentStep > step.number - 1,
                    onClick = {
                        if (step.number - 1 <= state.currentStep) {
                            // Allow going back / 可以返回
                        }
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Launch emulator button / 启动模拟器按钮
        if (state.currentStep == 2 && state.emulatorStatus == EmulatorStatus.NOT_STARTED) {
            Button(
                text = "🚀 启动模拟器",
                onClick = { onIntent(XRGlassesToolkitIntent.LaunchEmulator("ai_glasses")) },
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Next step button / 下一步按钮
        if (state.currentStep < 4) {
            Spacer(modifier = Modifier.weight(1f))
            Button(
                text = "下一步",
                onClick = { onIntent(XRGlassesToolkitIntent.NextStep) },
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Simulator input area / 模拟器输入区域
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = XRColors.Surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "触摸板事件序列输入",
                    color = XRColors.OnBackground,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = state.simulatorInput,
                    onValueChange = { onIntent(XRGlassesToolkitIntent.UpdateSimulatorInput(it)) },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("例如: [{\"type\": \"swipe\", \"direction\": \"up\"}]") },
                    colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = XRColors.Primary,
                        unfocusedBorderColor = XRColors.SurfaceVariant
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "格式: JSON 数组，每个事件包含 type/direction/duration",
                    color = XRColors.OnSurfaceVariant,
                    fontSize = 11.sp
                )
            }
        }
    }
}

/**
 * Emulator step data
 * 模拟器步骤数据
 */
private data class EmulatorStep(
    val number: Int,
    val title: String,
    val description: String
)

/**
 * Stepper item view
 * 步骤指示器条目视图
 */
@Composable
private fun StepperItemView(
    step: EmulatorStep,
    isActive: Boolean,
    isCompleted: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Step indicator / 步骤指示器
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(
                    when {
                        isCompleted -> XRColors.Success
                        isActive -> XRColors.Primary
                        else -> XRColors.SurfaceVariant
                    },
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isCompleted) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = XRColors.Background,
                    modifier = Modifier.size(20.dp)
                )
            } else {
                Text(
                    text = "${step.number}",
                    color = if (isActive) XRColors.Background else XRColors.OnSurfaceVariant,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column {
            Text(
                text = step.title,
                color = if (isActive) XRColors.OnBackground else XRColors.OnSurfaceVariant,
                fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal
            )
            Text(
                text = step.description,
                color = XRColors.OnSurfaceVariant,
                fontSize = 12.sp
            )
        }
    }
}

// ============================================================
// Tab 4: Privacy Compliance / 隐私合规
// ============================================================

/**
 * Privacy Compliance tab - 隐私设计检查清单
 * Tab 4: Privacy design checklist
 */
@Composable
private fun PrivacyComplianceTab(
    state: XRGlassesToolkitState,
    onIntent: (XRGlassesToolkitIntent) -> Unit
) {
    val filteredList = state.filteredChecklist()
    val compliantCount = state.checklist.count { it.isCompliant == true }
    val nonCompliantCount = state.checklist.count { it.isCompliant == false }

    Column(modifier = Modifier.fillMaxSize()) {
        // Header with stats / 带统计的标题区
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = XRColors.Surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "隐私合规检查清单",
                    style = MaterialTheme.typography.titleLarge,
                    color = XRColors.Primary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    StatItem("合规", compliantCount, XRColors.Success)
                    StatItem("不合规", nonCompliantCount, XRColors.Error)
                    StatItem("未检测", filteredList.size - compliantCount - nonCompliantCount, XRColors.OnSurfaceVariant)
                }
            }
        }

        // Filter chips / 过滤器芯片
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ChecklistFilter.entries.forEach { filter ->
                FilterChip(
                    selected = state.checklistFilter == filter,
                    onClick = { onIntent(XRGlassesToolkitIntent.FilterChecklist(filter)) },
                    label = { Text(filter.label) }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Checklist / 检查清单
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(filteredList) { item ->
                PrivacyChecklistItem(
                    item = item,
                    onToggle = { onIntent(XRGlassesToolkitIntent.ToggleChecklistItem(item.id)) }
                )
            }
        }

        // Generate report button / 生成报告按钮
        Button(
            text = "📄 生成合规报告",
            onClick = { onIntent(XRGlassesToolkitIntent.GeneratePrivacyReport) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        )
    }
}

/**
 * Privacy checklist item
 * 隐私检查清单条目
 */
@Composable
private fun PrivacyChecklistItem(
    item: PrivacyCheckItem,
    onToggle: () -> Unit
) {
    val statusColor = when (item.isCompliant) {
        true -> XRColors.Success
        false -> XRColors.Error
        null -> XRColors.OnSurfaceVariant
    }
    val statusIcon: ImageVector = when (item.isCompliant) {
        true -> Icons.Default.CheckCircle
        false -> Icons.Default.Error
        null -> Icons.Default.Help
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle),
        colors = CardDefaults.cardColors(containerColor = XRColors.Surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = statusIcon,
                contentDescription = null,
                tint = statusColor,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.title,
                    color = XRColors.OnBackground,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = item.description,
                    color = XRColors.OnSurfaceVariant,
                    fontSize = 12.sp
                )
            }
            Checkbox(
                checked = item.isCompliant == true,
                onCheckedChange = { onToggle() }
            )
        }
    }
}

// ============================================================
// Reusable Components / 可复用组件
// ============================================================

/**
 * Stat item for privacy compliance summary
 * 隐私合规统计条目
 */
@Composable
private fun StatItem(label: String, value: Int, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "$value",
            color = color,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            color = XRColors.OnSurfaceVariant,
            fontSize = 12.sp
        )
    }
}

/**
 * Device type dropdown selector
 * 设备类型下拉选择器
 */
@Composable
private fun DeviceTypeDropdown(
    selectedType: DeviceType,
    onTypeSelected: (DeviceType) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        Surface(
            modifier = Modifier.clickable { expanded = true },
            shape = RoundedCornerShape(8.dp),
            color = XRColors.SurfaceVariant
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = selectedType.displayName,
                    color = XRColors.Primary,
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("▼", color = XRColors.OnSurfaceVariant, fontSize = 8.sp)
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DeviceType.entries.forEach { type ->
                DropdownMenuItem(
                    text = { Text(type.displayName) },
                    onClick = {
                        onTypeSelected(type)
                        expanded = false
                    }
                )
            }
        }
    }
}

/**
 * Code block with syntax highlighting style
 * 代码块（语法高亮风格）
 */
@Composable
private fun CodeBlock(
    code: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = XRColors.CodeBackground
    ) {
        Row(modifier = Modifier.padding(12.dp)) {
            Text(
                text = code,
                color = XRColors.CodeText,
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp
            )
        }
    }
}

/**
 * Primary button / 主要操作按钮
 */
@Composable
private fun Button(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    androidx.compose.material3.Button(
        onClick = onClick,
        modifier = modifier,
        colors = androidx.compose.material3.ButtonDefaults.buttonColors(
            containerColor = XRColors.Primary
        )
    ) {
        Text(text = text, color = XRColors.Background)
    }
}

/**
 * Step data for getting started tab
 * 入门 Tab 的步骤数据
 */
private data class StepData(
    val number: Int,
    val title: String,
    val description: String,
    val code: String
)