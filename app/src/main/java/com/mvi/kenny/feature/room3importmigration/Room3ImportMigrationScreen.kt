package com.mvi.kenny.feature.room3importmigration

// ================================================================
// Room3ImportMigrationScreen — Room 3.0 Import 批量迁移工具主页面
// ================================================================
// MVI architecture: receives State, renders UI, emits Intent.
//
// PRD-225: Room 3.0 破坏性变更迁移工具包
// Design: memory/agency/designs/PRD-225-Room-3-0-破坏性变更迁移工具包.md
//
// 5 Tabs:
//   Tab 0: Import迁移 — Scan & batch-replace androidx.room → androidx.room3
//   Tab 1: SQLiteDriver扫描 — Detect SupportSQLiteDatabase usage
//   Tab 2: Suspend函数指南 — Sync DAO → suspend migration patterns
//   Tab 3: 双版本兼容 — Dual-version (2.x & 3.0) strategy
//   Tab 4: CI合规检测 — Gradle plugin compliance + migration checklist
// ================================================================

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mvi.kenny.feature.room3importmigration.Room3ImportMigrationIntent.*
import com.mvi.kenny.feature.room3importmigration.components.ChecklistItemCard
import com.mvi.kenny.feature.room3importmigration.components.DiffCard
import kotlinx.coroutines.flow.collectLatest

// ─────────────────────────────────────────────────────────────────
// Color System / 颜色系统
// ─────────────────────────────────────────────────────────────────
private object Room3ImportColors {
    // Dark Terminal Theme
    val Background = Color(0xFF0D1117)
    val Surface = Color(0xFF161B22)
    val SurfaceVariant = Color(0xFF21262D)
    val CardBorder = Color(0xFF30363D)
    val TerminalGreen = Color(0xFF39D353)
    val TerminalBlue = Color(0xFF58A6FF)
    val TerminalPurple = Color(0xFFBC8CFF)
    val TerminalYellow = Color(0xFFD29922)
    val TerminalOrange = Color(0xFFFFA657)
    val DiffRed = Color(0xFFF85149)
    val DiffRedBg = Color(0x1AF85149)
    val DiffGreen = Color(0xFF3FB950)
    val DiffGreenBg = Color(0x1A3FB950)
    val OnSurface = Color(0xFFE6EDF3)
    val OnSurfaceVariant = Color(0xFF8B949E)
    val P0Color = Color(0xFFF85149)
    val P1Color = Color(0xFFD29922)
    val P2Color = Color(0xFF3FB950)
    val P3Color = Color(0xFF6BCF7F)
}

// ─────────────────────────────────────────────────────────────────
// Tab Configuration / Tab 配置
// ─────────────────────────────────────────────────────────────────
private data class TabConfig(
    val title: String,
    val emoji: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

private val tabConfigs = listOf(
    TabConfig("Import迁移", "📦", Icons.Default.Inventory2),
    TabConfig("SQLiteDriver", "🔍", Icons.Default.Search),
    TabConfig("Suspend指南", "⚡", Icons.Default.FlashOn),
    TabConfig("双版本兼容", "🔀", Icons.Default.SwapHoriz),
    TabConfig("CI合规", "🛡️", Icons.Default.Shield)
)

// ─────────────────────────────────────────────────────────────────
// Main Screen Composable / 主页面
// ─────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Room3ImportMigrationScreen(
    viewModel: Room3ImportMigrationViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val clipboardManager = LocalClipboardManager.current

    // Collect effects / 收集副作用
    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is Room3ImportMigrationEffect.CopyToClipboard ->
                    clipboardManager.setText(AnnotatedString(effect.content))
                is Room3ImportMigrationEffect.ShowSnackbar ->
                    snackbarHostState.showSnackbar(effect.message)
            }
        }
    }

    Scaffold(
        containerColor = Room3ImportColors.Background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "🔄 Room3迁移",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = Room3ImportColors.TerminalGreen
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "PRD-225",
                            fontSize = 10.sp,
                            color = Room3ImportColors.OnSurfaceVariant,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Room3ImportColors.Surface
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Tab Row / Tab 栏
            TabRow(
                selectedTabIndex = state.selectedTab,
                containerColor = Room3ImportColors.Surface,
                contentColor = Room3ImportColors.TerminalBlue,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[state.selectedTab]),
                        color = Room3ImportColors.TerminalBlue
                    )
                }
            ) {
                tabConfigs.forEachIndexed { index, config ->
                    Tab(
                        selected = state.selectedTab == index,
                        onClick = { viewModel.sendIntent(SelectTab(index)) },
                        text = {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(config.emoji, fontSize = 14.sp)
                                    Spacer(Modifier.width(4.dp))
                                    Text(
                                        config.title,
                                        fontSize = 12.sp,
                                        fontWeight = if (state.selectedTab == index)
                                            FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            }
                        },
                        selectedContentColor = Room3ImportColors.TerminalBlue,
                        unselectedContentColor = Room3ImportColors.OnSurfaceVariant
                    )
                }
            }

            // Pager state / 分页器状态
            val pagerState = rememberPagerState(pageCount = { tabConfigs.size }, initialPage = state.selectedTab)

            // Sync pager with state / 同步分页器与状态
            LaunchedEffect(state.selectedTab) {
                if (pagerState.currentPage != state.selectedTab) {
                    pagerState.animateScrollToPage(state.selectedTab)
                }
            }

            LaunchedEffect(pagerState.currentPage) {
                if (pagerState.currentPage != state.selectedTab) {
                    viewModel.sendIntent(SelectTab(pagerState.currentPage))
                }
            }

            // Pager / 页面
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                when (page) {
                    0 -> ImportMigrationTab(
                        state = state,
                        onIntent = viewModel::sendIntent
                    )
                    1 -> SQLiteDriverScanTab(
                        state = state,
                        onIntent = viewModel::sendIntent
                    )
                    2 -> SuspendGuideTab(
                        state = state,
                        onIntent = viewModel::sendIntent
                    )
                    3 -> DualVersionTab(
                        state = state,
                        onIntent = viewModel::sendIntent
                    )
                    4 -> CIChecklistTab(
                        state = state,
                        onIntent = viewModel::sendIntent
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────
// Tab 0: Import Migration / Import 迁移 Tab
// ─────────────────────────────────────────────────────────────────

@Composable
private fun ImportMigrationTab(
    state: Room3ImportMigrationState,
    onIntent: (Room3ImportMigrationIntent) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Room3ImportColors.Background)
            .padding(16.dp)
    ) {
        // Project Path Input / 项目路径输入
        OutlinedTextField(
            value = state.projectPath,
            onValueChange = { onIntent(SetProjectPath(it)) },
            label = { Text("📁 项目路径 / Project Path", color = Room3ImportColors.OnSurfaceVariant) },
            placeholder = { Text("e.g. /Users/kenny/Projects/MyApp", color = Room3ImportColors.OnSurfaceVariant.copy(alpha = 0.5f)) },
            modifier = Modifier.fillMaxWidth(),
            textStyle = LocalTextStyle.current.copy(
                fontFamily = FontFamily.Monospace,
                color = Room3ImportColors.OnSurface
            ),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Room3ImportColors.TerminalBlue,
                unfocusedBorderColor = Room3ImportColors.CardBorder,
                focusedLabelColor = Room3ImportColors.TerminalBlue
            ),
            singleLine = true
        )

        Spacer(Modifier.height(12.dp))

        // Scan Buttons / 扫描按钮
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { onIntent(RunImportScan(simulate = true)) },
                enabled = !state.isScanning,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Room3ImportColors.TerminalGreen
                ),
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null)
                Spacer(Modifier.width(4.dp))
                Text("▶ 模拟扫描", fontFamily = FontFamily.Monospace)
            }

            Button(
                onClick = { onIntent(RunImportScan(simulate = false)) },
                enabled = !state.isScanning && state.projectPath.isNotBlank(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Room3ImportColors.TerminalBlue
                ),
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.Search, contentDescription = null)
                Spacer(Modifier.width(4.dp))
                Text("🔍 真实扫描", fontFamily = FontFamily.Monospace)
            }
        }

        // Progress Bar / 进度条
        if (state.isScanning) {
            Spacer(Modifier.height(12.dp))
            Column {
                Text(
                    state.scanProgressText,
                    color = Room3ImportColors.TerminalGreen,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp
                )
                Spacer(Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = { state.scanProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = Room3ImportColors.TerminalGreen,
                    trackColor = Room3ImportColors.SurfaceVariant
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        // Stats Header / 统计头
        if (state.importScanResults.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Room3ImportColors.Surface)
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "📊 发现 ${state.importScanResults.size} 个 Import",
                    color = Room3ImportColors.TerminalGreen,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.weight(1f))
                Text(
                    "androidx.room → androidx.room3",
                    color = Room3ImportColors.TerminalBlue,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp
                )
            }
            Spacer(Modifier.height(8.dp))
        }

        // Results List / 结果列表
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(state.importScanResults) { result ->
                ImportResultCard(
                    result = result,
                    onCopy = { onIntent(CopyFixCode(result.newImport)) }
                )
            }

            item { Spacer(Modifier.height(80.dp)) }
        }
    }
}

@Composable
private fun ImportResultCard(
    result: ImportScanResult,
    onCopy: () -> Unit
) {
    val clipboardManager = LocalClipboardManager.current

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Room3ImportColors.Surface),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(Room3ImportColors.CardBorder)
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // File path + line number / 文件路径 + 行号
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "📄 ${result.filePath.substringAfterLast("/")}",
                    color = Room3ImportColors.TerminalBlue,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    "L${result.lineNumber}",
                    color = Room3ImportColors.TerminalYellow,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp
                )
            }

            Spacer(Modifier.height(8.dp))

            // Before / After / 迁移前后
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .background(Room3ImportColors.DiffRedBg)
                    .padding(8.dp)
            ) {
                Text(
                    "❌ ${result.originalImport}",
                    color = Room3ImportColors.DiffRed,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(4.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .background(Room3ImportColors.DiffGreenBg)
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "✅ ${result.newImport}",
                    color = Room3ImportColors.DiffGreen,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    modifier = Modifier.weight(1f)
                )
                IconButton(
                    onClick = onCopy,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        Icons.Default.ContentCopy,
                        contentDescription = "Copy",
                        tint = Room3ImportColors.DiffGreen,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────
// Tab 1: SQLiteDriver Scan / SQLiteDriver 扫描 Tab
// ─────────────────────────────────────────────────────────────────

@Composable
private fun SQLiteDriverScanTab(
    state: Room3ImportMigrationState,
    onIntent: (Room3ImportMigrationIntent) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Room3ImportColors.Background)
            .padding(16.dp)
    ) {
        // Header / 头部
        Text(
            "🔍 SupportSQLite → SQLiteDriver 扫描器",
            color = Room3ImportColors.TerminalGreen,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold
        )
        Text(
            "检测 SupportSQLiteDatabase / SupportSQLiteOpenHelper 使用，输出迁移 diff",
            color = Room3ImportColors.OnSurfaceVariant,
            fontSize = 12.sp,
            modifier = Modifier.padding(vertical = 4.dp)
        )

        Spacer(Modifier.height(12.dp))

        // Scan Button / 扫描按钮
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { onIntent(RunSQLiteDriverScan(simulate = true)) },
                enabled = !state.isScanning,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Room3ImportColors.TerminalOrange
                ),
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null)
                Spacer(Modifier.width(4.dp))
                Text("▶ 模拟扫描", fontFamily = FontFamily.Monospace)
            }

            Button(
                onClick = { onIntent(RunSQLiteDriverScan(simulate = false)) },
                enabled = !state.isScanning && state.projectPath.isNotBlank(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Room3ImportColors.TerminalBlue
                ),
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.Search, contentDescription = null)
                Spacer(Modifier.width(4.dp))
                Text("🔍 真实扫描", fontFamily = FontFamily.Monospace)
            }
        }

        Spacer(Modifier.height(16.dp))

        // Stats / 统计
        if (state.sqliteDriverResults.isNotEmpty()) {
            val p0Count = state.sqliteDriverResults.count { it.riskLevel == RiskLevel.P0_CRITICAL }
            val p1Count = state.sqliteDriverResults.count { it.riskLevel == RiskLevel.P1_HIGH }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Room3ImportColors.Surface)
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🔴 P0", color = Room3ImportColors.P0Color, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                    Text("$p0Count", color = Room3ImportColors.P0Color, fontFamily = FontFamily.Monospace)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🟠 P1", color = Room3ImportColors.P1Color, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                    Text("$p1Count", color = Room3ImportColors.P1Color, fontFamily = FontFamily.Monospace)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("总计", color = Room3ImportColors.OnSurfaceVariant, fontFamily = FontFamily.Monospace)
                    Text("${state.sqliteDriverResults.size}", color = Room3ImportColors.OnSurface, fontFamily = FontFamily.Monospace)
                }
            }
            Spacer(Modifier.height(12.dp))
        }

        // Results / 结果列表
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(state.sqliteDriverResults) { result ->
                DiffCard(
                    title = "${result.apiUsage} — L${result.lineNumber}",
                    filePath = result.filePath,
                    beforeCode = result.beforeCode,
                    afterCode = result.afterCode,
                    riskLevel = result.riskLevel,
                    onCopy = { onIntent(CopyFixCode(result.afterCode)) }
                )
            }
            item { Spacer(Modifier.height(80.dp)) }
        }
    }
}

// ─────────────────────────────────────────────────────────────────
// Tab 2: Suspend Guide / Suspend 函数指南 Tab
// ─────────────────────────────────────────────────────────────────

@Composable
private fun SuspendGuideTab(
    state: Room3ImportMigrationState,
    onIntent: (Room3ImportMigrationIntent) -> Unit
) {
    LaunchedEffect(state.selectedTab) {
        // Load data when tab is first shown
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Room3ImportColors.Background)
            .padding(16.dp)
    ) {
        // Header / 头部
        Text(
            "⚡ Room 3.0 Suspend 函数改写指南",
            color = Room3ImportColors.TerminalGreen,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold
        )
        Text(
            "同步 DAO → suspend 函数改写模式，含 Before/After 代码对比",
            color = Room3ImportColors.OnSurfaceVariant,
            fontSize = 12.sp,
            modifier = Modifier.padding(vertical = 4.dp)
        )

        Spacer(Modifier.height(12.dp))

        // Load data / 加载数据
        if (state.suspendGuideItems.isEmpty()) {
            LaunchedEffect(Unit) {
                // Pre-load simulated data on first composition
            }
            // Use simulated data directly
        }
        val items = state.suspendGuideItems.ifEmpty { SIMULATED_SUSPEND_GUIDE_ITEMS }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(items) { item ->
                SuspendGuideCard(
                    item = item,
                    onCopyAfter = { onIntent(CopyFixCode(item.afterCode)) }
                )
            }
            item { Spacer(Modifier.height(80.dp)) }
        }
    }
}

@Composable
private fun SuspendGuideCard(
    item: SuspendGuideItem,
    onCopyAfter: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Room3ImportColors.Surface),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(Room3ImportColors.CardBorder)
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Header / 头部
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "⚡ ${item.patternName}",
                    color = Room3ImportColors.TerminalGreen,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                RiskBadge(riskLevel = item.riskLevel)
            }

            Text(
                item.description,
                color = Room3ImportColors.OnSurfaceVariant,
                fontSize = 12.sp,
                modifier = Modifier.padding(vertical = 4.dp)
            )

            Text(
                "📍 适用场景: ${item.applicableScenario}",
                color = Room3ImportColors.TerminalYellow,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Before Code / 迁移前代码
            Text(
                "❌ 迁移前",
                color = Room3ImportColors.DiffRed,
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
            SelectionContainer {
                Text(
                    item.beforeCode,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp,
                    color = Room3ImportColors.DiffRed,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(Room3ImportColors.DiffRedBg)
                        .padding(8.dp)
                )
            }

            Spacer(Modifier.height(8.dp))

            // After Code / 迁移后代码
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "✅ 迁移后",
                    color = Room3ImportColors.DiffGreen,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                TextButton(onClick = onCopyAfter) {
                    Icon(
                        Icons.Default.ContentCopy,
                        contentDescription = "Copy",
                        tint = Room3ImportColors.DiffGreen,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        "复制",
                        color = Room3ImportColors.DiffGreen,
                        fontSize = 11.sp
                    )
                }
            }
            SelectionContainer {
                Text(
                    item.afterCode,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp,
                    color = Room3ImportColors.DiffGreen,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(Room3ImportColors.DiffGreenBg)
                        .padding(8.dp)
                )
            }

            if (item.notes.isNotBlank()) {
                Spacer(Modifier.height(8.dp))
                Text(
                    "💡 ${item.notes}",
                    color = Room3ImportColors.TerminalBlue,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(Room3ImportColors.SurfaceVariant)
                        .padding(8.dp)
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────
// Tab 3: Dual Version / 双版本兼容 Tab
// ─────────────────────────────────────────────────────────────────

@Composable
private fun DualVersionTab(
    state: Room3ImportMigrationState,
    onIntent: (Room3ImportMigrationIntent) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Room3ImportColors.Background)
            .padding(16.dp)
    ) {
        // Header / 头部
        Text(
            "🔀 Room 2.x + 3.0 双版本兼容策略",
            color = Room3ImportColors.TerminalGreen,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold
        )
        Text(
            "同时支持 Room 2.x 和 3.0 的代码组织策略，含 Flavor / SourceSet / 接口抽象方案",
            color = Room3ImportColors.OnSurfaceVariant,
            fontSize = 12.sp,
            modifier = Modifier.padding(vertical = 4.dp)
        )

        Spacer(Modifier.height(12.dp))

        val strategies = state.dualVersionStrategies.ifEmpty { SIMULATED_DUAL_VERSION_STRATEGIES }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(strategies) { strategy ->
                DualVersionStrategyCard(
                    strategy = strategy,
                    onCopyAfter = { onIntent(CopyFixCode(strategy.afterCode)) }
                )
            }
            item { Spacer(Modifier.height(80.dp)) }
        }
    }
}

@Composable
private fun DualVersionStrategyCard(
    strategy: DualVersionStrategy,
    onCopyAfter: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Room3ImportColors.Surface),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(Room3ImportColors.CardBorder)
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Header / 头部
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "🔀 ${strategy.name}",
                    color = Room3ImportColors.TerminalPurple,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    "[${strategy.approach}]",
                    color = Room3ImportColors.TerminalYellow,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp
                )
            }

            Spacer(Modifier.height(4.dp))
            Text(
                strategy.description,
                color = Room3ImportColors.OnSurfaceVariant,
                fontSize = 12.sp
            )

            Spacer(Modifier.height(8.dp))

            // Pros / 优点
            Text(
                "✅ 优点",
                color = Room3ImportColors.DiffGreen,
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
            strategy.pros.forEach { pro ->
                Text(
                    "  • $pro",
                    color = Room3ImportColors.OnSurface,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )
            }

            Spacer(Modifier.height(4.dp))

            // Cons / 缺点
            Text(
                "❌ 缺点",
                color = Room3ImportColors.DiffRed,
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
            strategy.cons.forEach { con ->
                Text(
                    "  • $con",
                    color = Room3ImportColors.OnSurface,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )
            }

            Spacer(Modifier.height(8.dp))

            // Recommended For / 推荐场景
            Text(
                "📍 推荐: ${strategy.recommendedFor}",
                color = Room3ImportColors.TerminalBlue,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .background(Room3ImportColors.SurfaceVariant)
                    .padding(8.dp)
            )

            Spacer(Modifier.height(8.dp))

            // Code Example / 代码示例
            Text(
                "💻 代码示例",
                color = Room3ImportColors.OnSurfaceVariant,
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp
            )
            SelectionContainer {
                Text(
                    strategy.afterCode,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp,
                    color = Room3ImportColors.TerminalGreen,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(Room3ImportColors.SurfaceVariant)
                        .padding(8.dp)
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────
// Tab 4: CI Checklist / CI 合规检测 Tab
// ─────────────────────────────────────────────────────────────────

@Composable
private fun CIChecklistTab(
    state: Room3ImportMigrationState,
    onIntent: (Room3ImportMigrationIntent) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Room3ImportColors.Background)
            .padding(16.dp)
    ) {
        // Header / 头部
        Text(
            "🛡️ Room 3.0 CI 合规检测",
            color = Room3ImportColors.TerminalGreen,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold
        )
        Text(
            "Gradle 插件合规检测 + 迁移检查清单，确保项目完全合规 Room 3.0",
            color = Room3ImportColors.OnSurfaceVariant,
            fontSize = 12.sp,
            modifier = Modifier.padding(vertical = 4.dp)
        )

        Spacer(Modifier.height(12.dp))

        // Compliance Score Card / 合规评分卡
        val items = state.ciChecklistItems.ifEmpty { SIMULATED_CI_CHECKLIST }
        ComplianceScoreCard(
            passed = state.ciCompliancePassed.coerceAtLeast(items.count { it.isChecked }),
            failed = state.ciComplianceFailed.coerceAtLeast(items.count { !it.isChecked && it.riskLevel == RiskLevel.P0_CRITICAL }),
            warning = state.ciComplianceWarning.coerceAtLeast(items.count { !it.isChecked && it.riskLevel != RiskLevel.P0_CRITICAL }),
            total = items.size,
            riskLevel = state.overallRiskLevel
        )

        Spacer(Modifier.height(12.dp))

        // Reset Button / 重置按钮
        TextButton(
            onClick = { onIntent(ResetAll) },
            colors = ButtonDefaults.textButtonColors(
                contentColor = Room3ImportColors.OnSurfaceVariant
            )
        ) {
            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(4.dp))
            Text("重置进度", fontSize = 12.sp)
        }

        Spacer(Modifier.height(8.dp))

        // Checklist / 检查清单
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(items) { item ->
                ChecklistItemCard(
                    item = item,
                    onToggle = { checked ->
                        onIntent(ToggleChecklistItem(item.id, checked))
                    }
                )
            }
            item { Spacer(Modifier.height(80.dp)) }
        }
    }
}

@Composable
private fun ComplianceScoreCard(
    passed: Int,
    failed: Int,
    warning: Int,
    total: Int,
    riskLevel: RiskLevel
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Room3ImportColors.Surface),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(riskLevel.color.copy(alpha = 0.5f))
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "合规评分",
                    color = Room3ImportColors.OnSurface,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                RiskBadge(riskLevel = riskLevel)
            }

            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("$passed", color = Room3ImportColors.DiffGreen, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 24.sp)
                    Text("通过", color = Room3ImportColors.OnSurfaceVariant, fontSize = 12.sp)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("$failed", color = Room3ImportColors.DiffRed, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 24.sp)
                    Text("失败", color = Room3ImportColors.OnSurfaceVariant, fontSize = 12.sp)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("$warning", color = Room3ImportColors.TerminalYellow, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 24.sp)
                    Text("警告", color = Room3ImportColors.OnSurfaceVariant, fontSize = 12.sp)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("$total", color = Room3ImportColors.OnSurface, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 24.sp)
                    Text("总计", color = Room3ImportColors.OnSurfaceVariant, fontSize = 12.sp)
                }
            }

            Spacer(Modifier.height(12.dp))

            // Progress bar / 进度条
            val progress = if (total > 0) passed.toFloat() / total else 0f
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = riskLevel.color,
                trackColor = Room3ImportColors.SurfaceVariant
            )
            Text(
                "${(progress * 100).toInt()}% compliant",
                color = riskLevel.color,
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────
// Shared Components / 共享组件
// ─────────────────────────────────────────────────────────────────

/**
 * Risk Badge — 风险标签
 */
@Composable
private fun RiskBadge(riskLevel: RiskLevel) {
    Surface(
        shape = RoundedCornerShape(4.dp),
        color = riskLevel.color.copy(alpha = 0.2f)
    ) {
        Text(
            text = "${riskLevel.emoji} ${riskLevel.displayName}",
            color = riskLevel.color,
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}
