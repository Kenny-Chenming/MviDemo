package com.mvi.kenny.feature.continueon

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack

import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Pending
import androidx.compose.material.icons.filled.QuestionMark
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mvi.kenny.feature.continueon.ContinueOnTab
import kotlinx.coroutines.launch

// ============================================================
// ContinueOnScreen — Android 17 Continue On API 开发工具包主界面
// ============================================================
// PRD-296 | Main Screen / 主界面
//
// Features / 功能:
// - HomeScreen: Hero Banner + 4 quick entry cards
// - 4 Tabs: Guide / Toolkit / Scenarios / Reference
// - Settings: Dark mode toggle
// - Decision Tree: Interactive Canvas visualization
// - Code Viewer: Kotlin/Java toggle with copy

// ─────────────────────────────────────────────────────────────────
// Colors / 颜色配置
// ─────────────────────────────────────────────────────────────────

private object ContinueOnColors {
    val Primary = Color(0xFF1565C0)
    val PrimaryDark = Color(0xFF42A5F5)
    val Secondary = Color(0xFF00838F)
    val SecondaryDark = Color(0xFF26C6DA)
    val Surface = Color(0xFFFAFAFA)
    val SurfaceDark = Color(0xFF121212)
    val Background = Color(0xFFFFFFFF)
    val BackgroundDark = Color(0xFF1E1E1E)
    val Error = Color(0xFFC62828)
    val ErrorDark = Color(0xFFEF5350)
    val Success = Color(0xFF2E7D32)
    val SuccessDark = Color(0xFF66BB6A)
    val CodeBackground = Color(0xFF1E1E1E)
    val OnPrimary = Color.White
    val OnSurfaceVariant = Color(0xFF666666)
    val BannerGradientStart = Color(0xFF1565C0)
    val BannerGradientEnd = Color(0xFF0D47A1)
}

// ─────────────────────────────────────────────────────────────────
// Navigation State / 导航状态
// ─────────────────────────────────────────────────────────────────

private enum class ContinueOnNav {
    HOME, SETTINGS, MAIN
}

// ─────────────────────────────────────────────────────────────────
// Main Screen Entry / 主入口
// ─────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContinueOnScreen(
    viewModel: ContinueOnViewModel = viewModel(),
    onNavigateBack: () -> Unit = {}
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    var currentNav by remember { mutableIntStateOf(ContinueOnNav.MAIN.ordinal) }

    // Listen for effects / 监听副作用
    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is ContinueOnEffect.ShowSnackbar -> {
                    scope.launch { snackbarHostState.showSnackbar(effect.message) }
                }
                is ContinueOnEffect.CopyToClipboard -> {
                    clipboardManager.setText(AnnotatedString(effect.text))
                }
                is ContinueOnEffect.NavigateToSettings -> {
                    currentNav = ContinueOnNav.SETTINGS.ordinal
                }
                is ContinueOnEffect.ShareCode -> {
                    // Share handled by system / 系统处理分享
                }
            }
        }
    }

    val primary = if (state.isDarkMode) ContinueOnColors.PrimaryDark else ContinueOnColors.Primary
    val background = if (state.isDarkMode) ContinueOnColors.BackgroundDark else ContinueOnColors.Background
    val surface = if (state.isDarkMode) ContinueOnColors.SurfaceDark else ContinueOnColors.Surface
    val onSurfaceVariant = ContinueOnColors.OnSurfaceVariant

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = when (currentNav) {
                            ContinueOnNav.HOME.ordinal -> "Continue On API"
                            ContinueOnNav.SETTINGS.ordinal -> "设置 / Settings"
                            else -> "Continue On API"
                        },
                        color = ContinueOnColors.OnPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        when (currentNav) {
                            ContinueOnNav.HOME.ordinal -> onNavigateBack()
                            ContinueOnNav.SETTINGS.ordinal -> currentNav = ContinueOnNav.MAIN.ordinal
                            else -> currentNav = ContinueOnNav.HOME.ordinal
                        }
                    }) {
                        Icon(
                            imageVector = if (currentNav == ContinueOnNav.HOME.ordinal)
                                Icons.AutoMirrored.Filled.ArrowBack else Icons.Default.Home,
                            contentDescription = "Back",
                            tint = ContinueOnColors.OnPrimary
                        )
                    }
                },
                actions = {
                    if (currentNav == ContinueOnNav.MAIN.ordinal) {
                        IconButton(onClick = { currentNav = ContinueOnNav.SETTINGS.ordinal }) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Settings",
                                tint = ContinueOnColors.OnPrimary
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = primary
                )
            )
        },
        bottomBar = {
            if (currentNav == ContinueOnNav.MAIN.ordinal) {
                ContinueOnBottomNav(
                    selectedTab = state.currentTab,
                    onTabSelected = { tab -> viewModel.sendIntent(ContinueOnIntent.SelectTab(tab)) }
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = background
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            when (currentNav) {
                ContinueOnNav.HOME.ordinal -> HomeContent(
                    state = state,
                    onToolClick = { tab ->
                        viewModel.sendIntent(ContinueOnIntent.SelectTab(tab))
                        currentNav = ContinueOnNav.MAIN.ordinal
                    },
                    onSettingsClick = { currentNav = ContinueOnNav.SETTINGS.ordinal },
                    primary = primary,
                    surface = surface,
                    background = background
                )
                ContinueOnNav.SETTINGS.ordinal -> SettingsContent(
                    state = state,
                    onDarkModeToggle = { viewModel.sendIntent(ContinueOnIntent.ToggleDarkMode(it)) },
                    onClearData = { viewModel.sendIntent(ContinueOnIntent.ClearData) },
                    surface = surface,
                    background = background,
                    primary = primary
                )
                else -> MainTabContent(
                    state = state,
                    viewModel = viewModel,
                    surface = surface,
                    background = background,
                    primary = primary,
                    onSurfaceVariant = onSurfaceVariant
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────
// Home Content / 首页内容
// ─────────────────────────────────────────────────────────────────

@Composable
private fun HomeContent(
    state: ContinueOnState,
    onToolClick: (ContinueOnTab) -> Unit,
    onSettingsClick: () -> Unit,
    primary: Color,
    surface: Color,
    background: Color
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(background)
            .verticalScroll(rememberScrollState())
    ) {
        // Hero Banner / 英雄横幅
        HeroBanner(
            primary = primary,
            surface = surface
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Quick Entry Cards / 快速入口卡片
        Text(
            text = "开发工具 / Development Tools",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp),
            color = primary
        )

        Spacer(modifier = Modifier.height(12.dp))

        // 4 Quick Entry Cards / 4个快速入口卡片
        val quickEntries = listOf(
            Triple(ContinueOnTab.GUIDE, "指南", Icons.Default.MenuBook),
            Triple(ContinueOnTab.TOOLKIT, "工具包", Icons.Default.Build),
            Triple(ContinueOnTab.SCENARIOS, "场景决策", Icons.Default.AccountTree),
            Triple(ContinueOnTab.REFERENCE, "API参考", Icons.Default.TableChart)
        )

        quickEntries.forEach { (tab, title, icon) ->
            QuickEntryCard(
                title = title,
                icon = icon,
                primary = primary,
                surface = surface,
                onClick = { onToolClick(tab) }
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

// ─────────────────────────────────────────────────────────────────
// Hero Banner / 英雄横幅
// ─────────────────────────────────────────────────────────────────

@Composable
private fun HeroBanner(primary: Color, surface: Color) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(ContinueOnColors.BannerGradientStart, ContinueOnColors.BannerGradientEnd)
                )
            )
            .padding(20.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Android 17",
                style = MaterialTheme.typography.titleLarge,
                color = Color.White.copy(alpha = 0.8f),
                fontWeight = FontWeight.Normal
            )
            Text(
                text = "Continue On API",
                style = MaterialTheme.typography.headlineLarge,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "跨设备连续性开发工具包",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White.copy(alpha = 0.9f)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Cross-Device Continuity Dev Toolkit",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.7f)
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Feature Tags / 特性标签
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FeatureTag("App-to-App Handoff")
                FeatureTag("Web Fallback")
                FeatureTag("Deep Link")
            }
        }

        // Decorative Icon / 装饰图标
        Icon(
            imageVector = Icons.Default.Share,
            contentDescription = null,
            tint = Color.White.copy(alpha = 0.15f),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .size(120.dp)
        )
    }
}

@Composable
private fun FeatureTag(text: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(Color.White.copy(alpha = 0.2f))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = Color.White
        )
    }
}

// ─────────────────────────────────────────────────────────────────
// Quick Entry Card / 快速入口卡片
// ─────────────────────────────────────────────────────────────────

@Composable
private fun QuickEntryCard(
    title: String,
    icon: ImageVector,
    primary: Color,
    surface: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = primary,
                modifier = Modifier.size(40.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = Icons.Default.ArrowForward,
                contentDescription = null,
                tint = primary
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────
// Bottom Navigation / 底部导航
// ─────────────────────────────────────────────────────────────────

@Composable
private fun ContinueOnBottomNav(
    selectedTab: ContinueOnTab,
    onTabSelected: (ContinueOnTab) -> Unit
) {
    NavigationBar {
        ContinueOnTab.entries.forEach { tab ->
            NavigationBarItem(
                icon = {
                    Icon(
                        imageVector = when (tab) {
                            ContinueOnTab.GUIDE -> Icons.Default.MenuBook
                            ContinueOnTab.TOOLKIT -> Icons.Default.Build
                            ContinueOnTab.SCENARIOS -> Icons.Default.AccountTree
                            ContinueOnTab.REFERENCE -> Icons.Default.TableChart
                        },
                        contentDescription = tab.titleZh
                    )
                },
                label = { Text(tab.titleZh) },
                selected = selectedTab == tab,
                onClick = { onTabSelected(tab) }
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────
// Main Tab Content / 主Tab内容
// ─────────────────────────────────────────────────────────────────

@Composable
private fun MainTabContent(
    state: ContinueOnState,
    viewModel: ContinueOnViewModel,
    surface: Color,
    background: Color,
    primary: Color,
    onSurfaceVariant: Color
) {
    val pagerState = rememberPagerState(
        pageCount = { ContinueOnTab.entries.size },
        initialPage = ContinueOnTab.entries.indexOf(state.currentTab)
    )
    val scope = rememberCoroutineScope()

    // Sync pager with state / 同步分页器与状态
    LaunchedEffect(state.currentTab) {
        val targetPage = ContinueOnTab.entries.indexOf(state.currentTab)
        if (pagerState.currentPage != targetPage) {
            pagerState.animateScrollToPage(targetPage)
        }
    }

    LaunchedEffect(pagerState.currentPage) {
        val tab = ContinueOnTab.entries[pagerState.currentPage]
        if (tab != state.currentTab) {
            viewModel.sendIntent(ContinueOnIntent.SelectTab(tab))
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Tab Row / Tab行
        TabRow(
            selectedTabIndex = pagerState.currentPage,
            containerColor = surface
        ) {
            ContinueOnTab.entries.forEachIndexed { index, tab ->
                Tab(
                    selected = pagerState.currentPage == index,
                    onClick = {
                        scope.launch { pagerState.animateScrollToPage(index) }
                    },
                    text = { Text(tab.titleZh, maxLines = 1) },
                    icon = {
                        Icon(
                            imageVector = when (tab) {
                                ContinueOnTab.GUIDE -> Icons.Default.MenuBook
                                ContinueOnTab.TOOLKIT -> Icons.Default.Build
                                ContinueOnTab.SCENARIOS -> Icons.Default.AccountTree
                                ContinueOnTab.REFERENCE -> Icons.Default.TableChart
                            },
                            contentDescription = tab.titleEn
                        )
                    }
                )
            }
        }

        // Pager / 分页器
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            when (page) {
                0 -> GuideTabContent(state = state, viewModel = viewModel, primary = primary, surface = surface)
                1 -> ToolkitTabContent(state = state, viewModel = viewModel, primary = primary, surface = surface)
                2 -> ScenariosTabContent(state = state, viewModel = viewModel, primary = primary, surface = surface)
                3 -> ReferenceTabContent(state = state, viewModel = viewModel, primary = primary, surface = surface)
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────
// Tab 1: Guide / 指南Tab
// ─────────────────────────────────────────────────────────────────

@Composable
private fun GuideTabContent(
    state: ContinueOnState,
    viewModel: ContinueOnViewModel,
    primary: Color,
    surface: Color
) {
    val clipboardManager = LocalClipboardManager.current
    val codeExamples = viewModel.getCodeExamples()
    var selectedExampleIndex by remember { mutableIntStateOf(0) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(state.ifDarkThen(ContinueOnColors.BackgroundDark, ContinueOnColors.Background))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Overview Card / 概述卡片
        item {
            OverviewCard(primary = primary, surface = surface)
        }

        // Quick Start / 快速入门
        item {
            QuickStartCard(primary = primary, surface = surface)
        }

        // Code Examples / 代码示例
        item {
            Text(
                text = "代码示例 / Code Examples",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = primary,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        // Language Chips / 语言选择Chip
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Kotlin", "Java").forEach { lang ->
                    FilterChip(
                        selected = state.selectedLanguage == lang,
                        onClick = { viewModel.sendIntent(ContinueOnIntent.SelectLanguage(lang)) },
                        label = { Text(lang) }
                    )
                }
            }
        }

        // Example Cards / 示例卡片
        itemsIndexed(codeExamples) { index, example ->
            CodeExampleCard(
                example = example,
                selectedLanguage = state.selectedLanguage,
                isSelected = selectedExampleIndex == index,
                onSelect = { selectedExampleIndex = index },
                onCopy = {
                    val code = if (state.selectedLanguage == "Kotlin") example.kotlinCode else example.javaCode
                    viewModel.sendIntent(ContinueOnIntent.CopyCode(code))
                },
                surface = surface,
                primary = primary
            )
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}

@Composable
private fun OverviewCard(primary: Color, surface: Color) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "什么是 Continue On？",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Continue On 是 Android 17 推出的首个原生跨设备连续性 API，让用户在一台设备开始任务，无缝切换到另一台设备继续。类似 Apple Handoff，但专为 Android 生态设计。",
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatusChip("App-to-App", Icons.Default.CheckCircle, ContinueOnColors.Success)
                StatusChip("Web Fallback", Icons.Default.Language, ContinueOnColors.Secondary)
                StatusChip("Deep Link", Icons.Default.Link, primary)
            }
        }
    }
}

@Composable
private fun StatusChip(text: String, icon: ImageVector, color: Color) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(color.copy(alpha = 0.1f))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(12.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = text, style = MaterialTheme.typography.labelSmall, color = color)
    }
}

@Composable
private fun QuickStartCard(primary: Color, surface: Color) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "快速入门 / Quick Start",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = primary
            )
            Spacer(modifier = Modifier.height(12.dp))

            val steps = listOf(
                "声明 Activity 支持 Handoff" to "在 AndroidManifest.xml 中添加 android:reportHandoffAvailable=\"true\"",
                "发布 Handoff Activity" to "在 onPause() 中调用 reportHandoff() 分享 Activity 状态",
                "处理接收的 Handoff" to "在接收端 Activity 通过 Deep Link data filter 重建上下文"
            )

            steps.forEachIndexed { index, (title, desc) ->
                Row(
                    modifier = Modifier.padding(vertical = 4.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${index + 1}",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(text = title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                        Text(text = desc, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    }
                }
                if (index < steps.lastIndex) HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            }
        }
    }
}

@Composable
private fun CodeExampleCard(
    example: CodeExample,
    selectedLanguage: String,
    isSelected: Boolean,
    onSelect: () -> Unit,
    onCopy: () -> Unit,
    surface: Color,
    primary: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onSelect),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) primary.copy(alpha = 0.05f) else surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 4.dp else 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = example.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onCopy, modifier = Modifier.size(24.dp)) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copy",
                        tint = primary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(ContinueOnColors.CodeBackground)
                    .padding(12.dp)
            ) {
                Text(
                    text = if (selectedLanguage == "Kotlin") example.kotlinCode else example.javaCode,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp
                    ),
                    color = Color.White
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────
// Tab 2: Toolkit / 工具包Tab
// ─────────────────────────────────────────────────────────────────

@Composable
private fun ToolkitTabContent(
    state: ContinueOnState,
    viewModel: ContinueOnViewModel,
    primary: Color,
    surface: Color
) {
    val tools = viewModel.getToolkitModules()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(state.ifDarkThen(ContinueOnColors.BackgroundDark, ContinueOnColors.Background))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "开发工具包 / Development Toolkit",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = primary
            )
        }

        item {
            Text(
                text = "选择子模块开始开发 / Select a submodule to start",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }

        items(tools) { tool ->
            ToolkitCard(
                tool = tool,
                primary = primary,
                surface = surface,
                onClick = { /* Navigate to submodule / 导航到子模块 */ }
            )
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}

@Composable
private fun ToolkitCard(
    tool: ContinueOnTool,
    primary: Color,
    surface: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(primary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = getToolIcon(tool.iconName),
                    contentDescription = null,
                    tint = primary,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = tool.titleZh,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = tool.titleEn,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = tool.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
            Icon(
                imageVector = Icons.Default.ArrowForward,
                contentDescription = null,
                tint = primary
            )
        }
    }
}

private fun getToolIcon(iconName: String): ImageVector = when (iconName) {
    "AccountTree" -> Icons.Default.AccountTree
    "Language" -> Icons.Default.Language
    "Link" -> Icons.Default.Link
    "Security" -> Icons.Default.Security
    else -> Icons.Default.Build
}

// ─────────────────────────────────────────────────────────────────
// Tab 3: Scenarios / 场景Tab
// ─────────────────────────────────────────────────────────────────

@Composable
private fun ScenariosTabContent(
    state: ContinueOnState,
    viewModel: ContinueOnViewModel,
    primary: Color,
    surface: Color
) {
    val scenarios = viewModel.getScenarios()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(state.ifDarkThen(ContinueOnColors.BackgroundDark, ContinueOnColors.Background))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "适用场景决策树 / Scenario Decision Tree",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = primary
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Decision Tree Canvas / 决策树Canvas
        item {
            DecisionTreeCanvas(
                treeState = state.decisionTreeState,
                onNodeClick = { nodeId ->
                    viewModel.sendIntent(ContinueOnIntent.SelectDecisionNode(nodeId))
                },
                surface = surface,
                primary = primary
            )
        }

        item {
            HorizontalDivider()
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "预置场景 / Pre-built Scenarios",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = primary
            )
        }

        items(scenarios) { scenario ->
            ScenarioCard(
                scenario = scenario,
                primary = primary,
                surface = surface,
                isSelected = state.selectedScenario?.id == scenario.id,
                onClick = { viewModel.sendIntent(ContinueOnIntent.SelectScenario(scenario)) }
            )
        }

        // Selected Scenario Detail / 选中场景详情
        state.selectedScenario?.let { scenario ->
            item {
                ScenarioDetailCard(scenario = scenario, primary = primary, surface = surface)
            }
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}

@Composable
private fun DecisionTreeCanvas(
    treeState: DecisionTreeState,
    onNodeClick: (String) -> Unit,
    surface: Color,
    primary: Color
) {
    val nodes = treeState.nodes
    if (nodes.isEmpty()) return

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        colors = CardDefaults.cardColors(containerColor = surface)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            // Simple tree visualization / 简单树形可视化
            val root = nodes.first()
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                TreeNodeView(
                    node = root,
                    isExpanded = treeState.expandedNodeIds.contains(root.id),
                    isSelected = treeState.selectedNodeId == root.id,
                    primary = primary,
                    onClick = { onNodeClick(root.id) }
                )
                if (treeState.expandedNodeIds.contains(root.id) && root.children.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    // Draw connection lines / 绘制连接线
                    Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                        root.children.take(3).forEach { child ->
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Canvas(modifier = Modifier.size(2.dp, 16.dp)) {
                                    drawLine(color = primary.copy(alpha = 0.5f), start = Offset(size.width / 2, 0f), end = Offset(size.width / 2, size.height))
                                }
                                TreeNodeView(
                                    node = child,
                                    isExpanded = treeState.expandedNodeIds.contains(child.id),
                                    isSelected = treeState.selectedNodeId == child.id,
                                    primary = primary,
                                    onClick = { onNodeClick(child.id) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TreeNodeView(
    node: DecisionNode,
    isExpanded: Boolean,
    isSelected: Boolean,
    primary: Color,
    onClick: () -> Unit
) {
    val borderColor by animateColorAsState(if (isSelected) primary else Color.Gray.copy(alpha = 0.3f))

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) primary.copy(alpha = 0.1f) else Color.White)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = node.questionZh,
                style = MaterialTheme.typography.labelSmall,
                textAlign = TextAlign.Center,
                color = if (isSelected) primary else Color.Black
            )
            if (node.children.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = primary,
                    modifier = Modifier.size(16.dp)
                )
            }
            if (node.recommendation != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "✓ 推荐",
                    style = MaterialTheme.typography.labelSmall,
                    color = ContinueOnColors.Success
                )
            }
        }
    }
}

@Composable
private fun ScenarioCard(
    scenario: Scenario,
    primary: Color,
    surface: Color,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) primary.copy(alpha = 0.05f) else surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 4.dp else 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = scenario.titleZh,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                AssistChip(
                    text = scenario.handoffType,
                    primary = primary
                )
            }
            Text(
                text = scenario.titleEn,
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = scenario.description,
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(8.dp))
            AssistChip(text = scenario.appType, primary = primary)
        }
    }
}

@Composable
private fun AssistChip(text: String, primary: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(primary.copy(alpha = 0.1f))
            .padding(horizontal = 8.dp, vertical = 2.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = primary
        )
    }
}

@Composable
private fun ScenarioDetailCard(
    scenario: Scenario,
    primary: Color,
    surface: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = primary.copy(alpha = 0.05f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "关键实现要点 / Key Implementation Points",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            scenario.keyPoints.forEach { point ->
                Row(
                    modifier = Modifier.padding(vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = ContinueOnColors.Success,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = point,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────
// Tab 4: Reference / 参考Tab
// ─────────────────────────────────────────────────────────────────

@Composable
private fun ReferenceTabContent(
    state: ContinueOnState,
    viewModel: ContinueOnViewModel,
    primary: Color,
    surface: Color
) {
    val apiDiffItems = viewModel.getApiDiffItems()
    val faqItems = viewModel.getFaqItems()
    var expandedFaqIndex by remember { mutableIntStateOf(-1) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(state.ifDarkThen(ContinueOnColors.BackgroundDark, ContinueOnColors.Background))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // API Diff Table / API差异表
        item {
            Text(
                text = "API 差异对照表 / API Diff Table",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = primary
            )
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = surface)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    // Header / 表头
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(primary.copy(alpha = 0.1f))
                            .padding(8.dp)
                    ) {
                        Text(
                            text = "API",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = "API 37+",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.width(80.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                    HorizontalDivider()
                    apiDiffItems.forEachIndexed { index, (api, versions) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(if (index % 2 == 0) Color.Transparent else Color.Gray.copy(alpha = 0.05f))
                                .padding(8.dp)
                        ) {
                            Text(
                                text = api,
                                style = MaterialTheme.typography.bodySmall,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = versions[0],
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.width(80.dp),
                                textAlign = TextAlign.Center,
                                color = if (versions[0] == "N/A") Color.Gray else ContinueOnColors.Success
                            )
                        }
                    }
                }
            }
        }

        // Permissions / 权限清单
        item {
            Text(
                text = "权限要求 / Permission Requirements",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = primary,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    val permissions = listOf(
                        "INTERNET" to "网络访问（Web Fallback）",
                        "BIND_CONTINUATION_HANDLER" to "Handoff 绑定权限",
                        "MANAGE_HANDOVERS" to "管理 Handoff 会话"
                    )
                    permissions.forEach { (perm, desc) ->
                        Row(
                            modifier = Modifier.padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = null,
                                tint = primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = perm,
                                style = MaterialTheme.typography.bodySmall,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.width(200.dp)
                            )
                            Text(
                                text = desc,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray
                            )
                        }
                    }
                }
            }
        }

        // FAQ / 常见问题
        item {
            Text(
                text = "FAQ / 常见问题",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = primary,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        itemsIndexed(faqItems) { index, (question, answer) ->
            FaqCard(
                question = question,
                answer = answer,
                isExpanded = expandedFaqIndex == index,
                onToggle = { expandedFaqIndex = if (expandedFaqIndex == index) -1 else index },
                primary = primary,
                surface = surface
            )
        }

        // Resources / 资源链接
        item {
            Text(
                text = "资源链接 / Resources",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = primary,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    ResourceLink("Android Developers - Continue On", "developer.android.com/guide/topics/ui/continue-on")
                    ResourceLink("Better Together / Handoff Blog", "android-developers.googleblog.com")
                    ResourceLink("Sample Code Repository", "github.com/android/app-samples")
                }
            }
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}

@Composable
private fun ResourceLink(title: String, url: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Info,
            contentDescription = null,
            tint = ContinueOnColors.Secondary,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = url,
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

@Composable
private fun FaqCard(
    question: String,
    answer: String,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    primary: Color,
    surface: Color
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle),
        colors = CardDefaults.cardColors(containerColor = surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Icon(
                        imageVector = Icons.Default.QuestionMark,
                        contentDescription = null,
                        tint = primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = question,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium
                    )
                }
                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = primary
                )
            }
            if (isExpanded) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = answer,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────
// Settings Content / 设置内容
// ─────────────────────────────────────────────────────────────────

@Composable
private fun SettingsContent(
    state: ContinueOnState,
    onDarkModeToggle: (Boolean) -> Unit,
    onClearData: () -> Unit,
    surface: Color,
    background: Color,
    primary: Color
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "外观 / Appearance",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = primary
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = surface)
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
                        imageVector = if (state.isDarkMode) Icons.Default.DarkMode else Icons.Default.LightMode,
                        contentDescription = null,
                        tint = primary
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "深色模式 / Dark Mode",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "跟随系统 / Follow system",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Gray
                        )
                    }
                }
                Switch(
                    checked = state.isDarkMode,
                    onCheckedChange = onDarkModeToggle
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "数据 / Data",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = primary
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = surface)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onClearData)
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = ContinueOnColors.Error
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "清除数据 / Clear Data",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "清除所有本地存储 / Clear all local storage",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Gray
                        )
                    }
                }
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = null,
                    tint = Color.Gray
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "关于 / About",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = primary
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "版本 / Version", style = MaterialTheme.typography.bodyMedium)
                    Text(text = "1.0.0", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                }
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "PRD / 需求", style = MaterialTheme.typography.bodyMedium)
                    Text(text = "PRD-296", style = MaterialTheme.typography.bodyMedium, color = primary)
                }
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                Text(
                    text = "Android 17 Continue On (Handoff) API 开发工具包",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────
// Extension Functions / 扩展函数
// ─────────────────────────────────────────────────────────────────

private fun ContinueOnState.ifDarkThen(dark: Color, light: Color): Color =
    if (isDarkMode) dark else light
