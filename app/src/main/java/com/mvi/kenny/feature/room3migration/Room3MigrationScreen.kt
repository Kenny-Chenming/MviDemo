package com.mvi.kenny.feature.room3migration

// ================================================================
// Room3MigrationScreen — Room 3.0 破坏性变更迁移工具包主界面
// ================================================================
// Main screen for Room 3.0 Migration Toolkit.
//
// PRD-212: Room 3.0 破坏性变更迁移工具包
// Design Reference: memory/agency/designs/PRD-212-Room-3-0-破坏性变更迁移工具包.md
//
// Features:
//   Tab 1: 风险扫描 — KAPT 检测扫描器 + 完整检查清单
//   Tab 2: KSP 迁移 — KAPT→KSP 迁移路径生成器
//   Tab 3: DAO 改造 — DAO suspend 化扫描 + @RawQuery 模板
//   Tab 4: 包与驱动 — Package 重命名 + Driver API 迁移指南
//   Tab 5: 验证上线 — CI 合规 + 渐进迁移规划 + 决策指南
// —————————————————————————————————————————————————————

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.FactCheck
import androidx.compose.material.icons.filled.FileCopy
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.InstallDesktop
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.LinearScale
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.material3.LocalTextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.flow.collectLatest

// ================================================================
// Color Palette — 深色 Terminal 风格
// ================================================================
private val TermGreen = Color(0xFF00E5CC)    // Primary: 青绿色（迁移/工具感）
private val TermPurple = Color(0xFF7C4DFF)   // Secondary: 紫色（高亮强调）
private val TermBg = Color(0xFF0D1117)       // Background: 深黑
private val TermSurface = Color(0xFF161B22)   // Surface: 卡片背景
private val TermSurfaceVar = Color(0xFF21262D) // SurfaceVariant: 输入框/次级
private val TermOnSurface = Color(0xFFE6EDF3)  // OnSurface: 主文字
private val TermOnSurfaceVar = Color(0xFF8B949E) // OnSurfaceVariant: 次级文字
private val TermError = Color(0xFFFF6B6B)      // Error: P0 风险
private val TermWarning = Color(0xFFFFB347)    // Warning: P1/P2 风险
private val TermSuccess = Color(0xFF3FB950)    // Success: 完成/合规
private val TermBlue = Color(0xFF58A6FF)       // Blue: 代码关键字

// ================================================================
// Tab Definitions — 5 主 Tab
// ================================================================
private data class MainTab(
    val title: String,
    val emoji: String,
    val icon: ImageVector,
    val description: String
)

private val MAIN_TABS = listOf(
    MainTab("风险扫描", "🔍", Icons.Default.Search, "KAPT 检测 + 破坏性变更检查清单"),
    MainTab("KSP 迁移", "⚙️", Icons.Default.Terminal, "KAPT→KSP build.gradle.kts Diff"),
    MainTab("DAO 改造", "🔄", Icons.Default.Code, "DAO suspend 化 + @RawQuery 模板"),
    MainTab("包与驱动", "📦", Icons.Default.Storage, "Package 重命名 + Driver API 指南"),
    MainTab("验证上线", "🏁", Icons.Default.CheckCircle, "CI 合规 + 迁移规划 + 决策指南")
)

// ================================================================
// Room3MigrationScreen — 主入口 Composable
// ================================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Room3MigrationScreen(
    state: Room3MigrationState,
    onIntent: (Room3MigrationIntent) -> Unit
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    var localTabIndex by remember { mutableIntStateOf(0) }

    // Listen for effects (snackbar, clipboard, share)
    LaunchedEffect(Unit) {
        // Effect collection would go here in production
    }

    // Show snackbar when state changes
    LaunchedEffect(state.snackbarMessage) {
        state.snackbarMessage?.let {
            snackbarHostState.showSnackbar(it)
            onIntent(Room3MigrationIntent.DismissSnackbar)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = TermBg,
        contentColor = TermOnSurface
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // ── Tab Row ───────────────────────────────────────
            TabRow(
                selectedTabIndex = localTabIndex,
                containerColor = TermSurface,
                contentColor = TermOnSurface,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[localTabIndex]),
                        color = TermGreen
                    )
                }
            ) {
                MAIN_TABS.forEachIndexed { index, tab ->
                    Tab(
                        selected = localTabIndex == index,
                        onClick = {
                            localTabIndex = index
                            onIntent(Room3MigrationIntent.SelectTab(index))
                        },
                        text = {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "${tab.emoji} ${tab.title}",
                                    fontSize = 11.sp,
                                    maxLines = 1
                                )
                            }
                        },
                        selectedContentColor = TermGreen,
                        unselectedContentColor = TermOnSurfaceVar
                    )
                }
            }

            // ── Tab Content ───────────────────────────────────
            AnimatedContent(
                targetState = localTabIndex,
                transitionSpec = {
                    fadeIn(tween(200)) togetherWith fadeOut(tween(200))
                },
                label = "tab_animation"
            ) { tab ->
                when (tab) {
                    0 -> TabRiskScan(state, onIntent)
                    1 -> TabKspMigration(state, onIntent)
                    2 -> TabDaoTransform(state, onIntent)
                    3 -> TabPackageDriver(state, onIntent)
                    4 -> TabVerifyLaunch(state, onIntent)
                }
            }
        }
    }
}

// ================================================================
// Tab 1: 风险扫描 (Risk Scan)
// ================================================================
@Composable
private fun TabRiskScan(
    state: Room3MigrationState,
    onIntent: (Room3MigrationIntent) -> Unit
) {
    var scanTab by remember { mutableIntStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(TermBg)
    ) {
        // Sub-tabs: KAPT Scanner | Checklist
        ScrollableTabRow(
            selectedTabIndex = scanTab,
            containerColor = TermSurface,
            contentColor = TermOnSurface,
            edgePadding = 8.dp,
            indicator = {},
            divider = {}
        ) {
            listOf("🔍 KAPT 检测" to 0, "📋 检查清单" to 1).forEach { (title, idx) ->
                Tab(
                    selected = scanTab == idx,
                    onClick = {
                        scanTab = idx
                        onIntent(Room3MigrationIntent.SelectScanTab(
                            if (idx == 0) ScanTab.KAPT_SCANNER else ScanTab.CHECKLIST
                        ))
                    },
                    text = { Text(title, fontSize = 12.sp) },
                    selectedContentColor = TermGreen,
                    unselectedContentColor = TermOnSurfaceVar
                )
            }
        }

        when (scanTab) {
            0 -> KaptScannerContent(state, onIntent)
            1 -> ChecklistContent(state, onIntent)
        }
    }
}

@Composable
private fun KaptScannerContent(
    state: Room3MigrationState,
    onIntent: (Room3MigrationIntent) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Import section
        SectionCard(title = "📥 导入项目", emoji = "📥") {
            OutlinedTextField(
                value = state.gradleFileContent,
                onValueChange = { onIntent(Room3MigrationIntent.PasteGradleContent(it)) },
                label = { Text("粘贴 build.gradle.kts 内容，或使用模拟数据") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 4,
                maxLines = 8,
                textStyle = LocalTextStyle.current.copy(fontFamily = FontFamily.Monospace, fontSize = 11.sp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { onIntent(Room3MigrationIntent.ImportProject(simulate = true)) },
                    enabled = !state.isScanning,
                    colors = ButtonDefaults.buttonColors(containerColor = TermGreen)
                ) {
                    if (state.isScanning) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = TermBg, strokeWidth = 2.dp
                        )
                    } else {
                        Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                    }
                    Spacer(Modifier.width(6.dp))
                    Text("导入并扫描")
                }

                OutlinedButton(
                    onClick = { onIntent(Room3MigrationIntent.RunFullScan) },
                    enabled = !state.isScanning && state.scanResults.kaptUsages.isNotEmpty()
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("完整扫描")
                }
            }

            if (state.isScanning) {
                Spacer(modifier = Modifier.height(12.dp))
                LinearProgressIndicator(
                    progress = { state.scanProgress },
                    modifier = Modifier.fillMaxWidth(),
                    color = TermGreen,
                    trackColor = TermSurfaceVar
                )
                Text(
                    "扫描进度: ${(state.scanProgress * 100).toInt()}%",
                    color = TermOnSurfaceVar,
                    fontSize = 12.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Risk Level Banner
        RiskLevelBanner(riskLevel = state.scanResults.riskLevel)

        Spacer(modifier = Modifier.height(16.dp))

        // KAPT Usages
        if (state.scanResults.kaptUsages.isNotEmpty()) {
            SectionCard(title = "🔍 KAPT 使用位置", emoji = "🔍") {
                state.scanResults.kaptUsages.forEach { usage ->
                    KaptUsageCard(usage)
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Module summary
            SectionCard(title = "📦 多模块扫描", emoji = "📦") {
                val modules = state.scanResults.modules
                if (modules.isNotEmpty()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        modules.forEach { module ->
                            BadgeChip(
                                text = module,
                                color = TermGreen
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "共 ${modules.size} 个模块，建议按依赖顺序从底层到上层迁移",
                        color = TermOnSurfaceVar,
                        fontSize = 12.sp
                    )
                } else {
                    Text("请先导入项目以扫描模块结构", color = TermOnSurfaceVar, fontSize = 12.sp)
                }
            }
        } else {
            EmptyStateCard(
                icon = Icons.Default.Terminal,
                title = "暂无扫描结果",
                description = "导入项目或粘贴 build.gradle.kts 内容开始扫描"
            )
        }
    }
}

@Composable
private fun ChecklistContent(
    state: Room3MigrationState,
    onIntent: (Room3MigrationIntent) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            "📋 Room 3.0 破坏性变更完整检查清单",
            color = TermOnSurface,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            "共 ${state.scanResults.breakingChanges.size} 项变更需要处理",
            color = TermOnSurfaceVar,
            fontSize = 12.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        state.scanResults.breakingChanges.forEachIndexed { index, bc ->
            BreakingChangeCard(bc)
            if (index < state.scanResults.breakingChanges.lastIndex) {
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        if (state.scanResults.breakingChanges.isEmpty()) {
            EmptyStateCard(
                icon = Icons.Default.FactCheck,
                title = "暂无检查清单",
                description = "导入项目后自动生成 Room 3.0 破坏性变更检查清单"
            )
        }
    }
}

// ================================================================
// Tab 2: KSP 迁移
// ================================================================
@Composable
private fun TabKspMigration(
    state: Room3MigrationState,
    onIntent: (Room3MigrationIntent) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        SectionCard(title = "⚙️ KSP 版本配置", emoji = "⚙️") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("KSP Version:", color = TermOnSurface, fontSize = 14.sp)
                Spacer(Modifier.width(12.dp))
                OutlinedTextField(
                    value = state.kspVersion,
                    onValueChange = { onIntent(Room3MigrationIntent.SetKspVersion(it)) },
                    modifier = Modifier.weight(1f),
                    textStyle = LocalTextStyle.current.copy(fontFamily = FontFamily.Monospace, fontSize = 14.sp)
                )
            }
            Spacer(Modifier.height(8.dp))
            Text(
                "Room 3.0 推荐 KSP 1.0.25+（与 Kotlin 1.9.24+ 配合）",
                color = TermOnSurfaceVar,
                fontSize = 12.sp
            )

            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { onIntent(Room3MigrationIntent.GenerateKspMigration) },
                    enabled = !state.isGeneratingKspDiff,
                    colors = ButtonDefaults.buttonColors(containerColor = TermGreen)
                ) {
                    if (state.isGeneratingKspDiff) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = TermBg, strokeWidth = 2.dp
                        )
                    } else {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                    }
                    Spacer(Modifier.width(6.dp))
                    Text("生成 KSP 迁移 Diff")
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // Gradle file type selector
        SectionCard(title = "📄 Gradle 文件类型", emoji = "📄") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = state.gradleFileType == GradleFileType.KOTLIN_DSL,
                    onClick = { onIntent(Room3MigrationIntent.SelectGradleFileType(GradleFileType.KOTLIN_DSL)) },
                    label = { Text("Kotlin DSL (.kts)") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = TermGreen,
                        selectedLabelColor = TermBg
                    )
                )
                FilterChip(
                    selected = state.gradleFileType == GradleFileType.GROOVY_DSL,
                    onClick = { onIntent(Room3MigrationIntent.SelectGradleFileType(GradleFileType.GROOVY_DSL)) },
                    label = { Text("Groovy DSL (.gradle)") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = TermGreen,
                        selectedLabelColor = TermBg
                    )
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        // Diff display
        state.kspMigrationDiff?.let { diff ->
            DiffCard(
                title = "📊 build.gradle.kts Diff",
                beforeLabel = "迁移前（KAPT）❌",
                afterLabel = "迁移后（KSP）✅",
                beforeContent = diff.beforeContent,
                afterContent = diff.afterContent,
                onCopyBefore = {},
                onCopyAfter = {}
            )

            Spacer(Modifier.height(16.dp))

            // Summary
            SectionCard(title = "💡 迁移要点", emoji = "💡") {
                val points = listOf(
                    "1. 移除 `org.jetbrains.kotlin.kapt` plugin",
                    "2. 添加 `com.google.devtools.ksp` plugin（需指定 version）",
                    "3. 将所有 `kapt(\"androidx.room:room-compiler:...\")` 改为 `ksp(...)`",
                    "4. 更新 Room 依赖版本至 3.0.0-alpha01",
                    "5. 验证增量编译：`./gradlew :app:kspDebugKotlin`"
                )
                points.forEach { point ->
                    Row(
                        modifier = Modifier.padding(vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = TermGreen,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(point, color = TermOnSurface, fontSize = 13.sp)
                    }
                }
            }
        } ?: run {
            EmptyStateCard(
                icon = Icons.Default.Terminal,
                title = "尚未生成 Diff",
                description = "点击上方「生成 KSP 迁移 Diff」按钮"
            )
        }
    }
}

// ================================================================
// Tab 3: DAO 改造
// ================================================================
@Composable
private fun TabDaoTransform(
    state: Room3MigrationState,
    onIntent: (Room3MigrationIntent) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // DAO Suspend Scanner
        SectionCard(title = "🔄 DAO Suspend 化扫描", emoji = "🔄") {
            if (state.daoMigrations.isEmpty()) {
                Text(
                    "请先在「风险扫描」Tab 导入项目以扫描 DAO 函数",
                    color = TermOnSurfaceVar,
                    fontSize = 13.sp
                )
            } else {
                Text(
                    "发现 ${state.daoMigrations.size} 个非 suspend DAO 函数需要改造：",
                    color = TermOnSurface,
                    fontSize = 13.sp
                )
                Spacer(Modifier.height(12.dp))
                state.daoMigrations.forEachIndexed { idx, migration ->
                    DaoMigrationCard(
                        migration = migration,
                        onCopy = { onIntent(Room3MigrationIntent.CopyDaoMigration(migration)) }
                    )
                    if (idx < state.daoMigrations.lastIndex) {
                        Spacer(Modifier.height(8.dp))
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // @RawQuery Templates
        SectionCard(title = "📝 @RawQuery 迁移模板", emoji = "📝") {
            SIMULATED_RAW_QUERY_TEMPLATES.forEachIndexed { idx, template ->
                RawQueryTemplateCard(template)
                if (idx < SIMULATED_RAW_QUERY_TEMPLATES.lastIndex) {
                    Spacer(Modifier.height(12.dp))
                }
            }
        }
    }
}

// ================================================================
// Tab 4: 包与驱动
// ================================================================
@Composable
private fun TabPackageDriver(
    state: Room3MigrationState,
    onIntent: (Room3MigrationIntent) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Package Rename
        SectionCard(title = "📦 Package 重命名", emoji = "📦") {
            if (state.scanResults.packageImports.isEmpty()) {
                Text(
                    "请先在「风险扫描」Tab 导入项目以扫描 Package imports",
                    color = TermOnSurfaceVar,
                    fontSize = 13.sp
                )
            } else {
                Text(
                    "发现 ${state.scanResults.packageImports.sumOf { it.occurrences }} 处 import 需要替换：",
                    color = TermOnSurface,
                    fontSize = 13.sp
                )
                Spacer(Modifier.height(12.dp))

                state.scanResults.packageImports.forEach { pkg ->
                    PackageImportCard(pkg)
                    Spacer(Modifier.height(8.dp))
                }

                Spacer(Modifier.height(12.dp))

                Button(
                    onClick = { onIntent(Room3MigrationIntent.RunPackageReplacement) },
                    enabled = !state.isScanning,
                    colors = ButtonDefaults.buttonColors(containerColor = TermPurple)
                ) {
                    if (state.isScanning) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = Color.White, strokeWidth = 2.dp
                        )
                    } else {
                        Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                    }
                    Spacer(Modifier.width(6.dp))
                    Text("执行批量替换")
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // Driver API Guide
        SectionCard(title = "🚀 Driver API 迁移指南", emoji = "🚀") {
            // Platform selector
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Platform.entries.forEach { platform ->
                    FilterChip(
                        selected = state.selectedPlatform == platform,
                        onClick = { onIntent(Room3MigrationIntent.SelectPlatform(platform)) },
                        label = { Text("${platform.icon} ${platform.displayName}") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = TermPurple,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            state.driverApiGuide?.let { guide ->
                DiffCard(
                    title = "${guide.platform.icon} ${guide.platform.displayName} Driver API",
                    beforeLabel = "迁移前 ❌",
                    afterLabel = "迁移后 ✅",
                    beforeContent = guide.beforeCode,
                    afterContent = guide.afterCode,
                    onCopyBefore = {},
                    onCopyAfter = {}
                )

                Spacer(Modifier.height(12.dp))

                if (guide.notes.isNotEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = TermSurfaceVar)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("📌 注意事项", color = TermWarning, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(4.dp))
                            Text(guide.notes, color = TermOnSurfaceVar, fontSize = 12.sp)
                        }
                    }
                }
            } ?: run {
                Text(
                    "选择目标平台以查看 Driver API 迁移指南",
                    color = TermOnSurfaceVar,
                    fontSize = 13.sp
                )
            }
        }
    }
}

// ================================================================
// Tab 5: 验证上线
// ================================================================
@Composable
private fun TabVerifyLaunch(
    state: Room3MigrationState,
    onIntent: (Room3MigrationIntent) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Migration Progress Overview
        SectionCard(title = "📊 迁移进度总览", emoji = "📊") {
            val progress = state.migrationProgress
            if (progress.kaptTotal > 0 || progress.daoTotal > 0) {
                MigrationProgressBar(progress)
                Spacer(Modifier.height(16.dp))

                // Stats row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    ProgressStat("KAPT", progress.kaptMigrated, progress.kaptTotal, TermGreen)
                    ProgressStat("DAO", progress.daoMigrated, progress.daoTotal, TermPurple)
                    ProgressStat("Package", progress.packagesMigrated, progress.packagesTotal, TermWarning)
                    ProgressStat("Driver", progress.driverMigrated, progress.driverTotal, TermBlue)
                }
            } else {
                Text(
                    "请先在「风险扫描」Tab 导入项目以评估迁移进度",
                    color = TermOnSurfaceVar,
                    fontSize = 13.sp
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        // Decision Guide
        state.decisionGuide?.let { guide ->
            SectionCard(title = "🧭 升级决策指南", emoji = "🧭") {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (guide.recommendation == "CONDITIONAL") TermWarning.copy(alpha = 0.15f)
                        else TermSurfaceVar
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            when (guide.recommendation) {
                                "UPGRADE_NOW" -> "🚀 建议立即升级"
                                "WAIT" -> "⏳ 建议等待"
                                else -> "⚠️ 有条件升级"
                            },
                            color = if (guide.recommendation == "CONDITIONAL") TermWarning
                            else if (guide.recommendation == "UPGRADE_NOW") TermSuccess
                            else TermOnSurfaceVar,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            guide.reasoning,
                            color = TermOnSurface,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
        }

        // Migration Plan
        SectionCard(title = "🗺️ 渐进迁移规划", emoji = "🗺️") {
            if (state.migrationPlan == null) {
                Button(
                    onClick = { onIntent(Room3MigrationIntent.GenerateMigrationPlan) },
                    colors = ButtonDefaults.buttonColors(containerColor = TermGreen)
                ) {
                    Icon(Icons.Default.AccountTree, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("生成迁移计划")
                }
            } else {
                state.migrationPlan.phases.forEachIndexed { idx, phase ->
                    MigrationPhaseCard(phase)
                    if (idx < state.migrationPlan.phases.lastIndex) {
                        Spacer(Modifier.height(8.dp))
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // CI Compliance
        SectionCard(title = "☁️ CI 合规检测", emoji = "☁️") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        "状态: ${state.ciComplianceStatus.emoji} ${state.ciComplianceStatus.displayName}",
                        color = TermOnSurface,
                        fontSize = 14.sp
                    )
                    Text(
                        "通过 ${state.ciComplianceStatus.passedChecks} | 失败 ${state.ciComplianceStatus.failedChecks} | 警告 ${state.ciComplianceStatus.warningChecks}",
                        color = TermOnSurfaceVar,
                        fontSize = 12.sp
                    )
                }
                Button(
                    onClick = { onIntent(Room3MigrationIntent.SetupCiCompliance) },
                    colors = ButtonDefaults.buttonColors(containerColor = TermBlue)
                ) {
                    Icon(Icons.Default.CloudDone, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("运行 CI 检测")
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // Export Report
        Button(
            onClick = { onIntent(Room3MigrationIntent.ExportReport) },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = TermPurple)
        ) {
            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("📤 导出完整迁移报告 (Markdown)")
        }
    }
}

// ================================================================
// Reusable UI Components
// ================================================================

@Composable
private fun SectionCard(
    title: String,
    emoji: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = TermSurface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "$emoji $title",
                color = TermOnSurface,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
private fun EmptyStateCard(
    icon: ImageVector,
    title: String,
    description: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = TermSurface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = TermOnSurfaceVar,
                modifier = Modifier.size(48.dp)
            )
            Spacer(Modifier.height(12.dp))
            Text(title, color = TermOnSurface, fontSize = 16.sp, fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(4.dp))
            Text(
                description,
                color = TermOnSurfaceVar,
                fontSize = 13.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun RiskLevelBanner(riskLevel: RiskLevel) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = riskLevel.color.copy(alpha = 0.15f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = when (riskLevel) {
                    RiskLevel.P0_CRITICAL -> "🚨 P0 严重风险 — 必须立即处理"
                    RiskLevel.P1_HIGH -> "⚠️ P1 高风险 — 建议优先处理"
                    RiskLevel.P2_MEDIUM -> "⚡ P2 中风险 — 建议处理"
                    RiskLevel.P3_LOW -> "✅ P3 低风险 — 可延后处理"
                    RiskLevel.UNKNOWN -> "❓ 未知 — 请先导入项目"
                },
                color = riskLevel.color,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun KaptUsageCard(usage: KaptUsage) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = TermSurfaceVar),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    BadgeChip(
                        text = usage.module,
                        color = TermGreen
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        usage.filePath,
                        color = TermOnSurfaceVar,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                }
                BadgeChip(
                    text = usage.severity.displayName,
                    color = usage.severity.color
                )
            }
            Spacer(Modifier.height(8.dp))
            Text(
                usage.configSnippet,
                color = TermError,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

@Composable
private fun BadgeChip(text: String, color: Color) {
    Box(
        modifier = Modifier
            .background(color.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
            .border(1.dp, color.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text,
            color = color,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun BreakingChangeCard(bc: BreakingChange) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded },
        colors = CardDefaults.cardColors(containerColor = TermSurfaceVar),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "${bc.riskLevel.emoji} ${bc.title}",
                        color = bc.riskLevel.color,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    BadgeChip(text = bc.category, color = TermBlue)
                    Spacer(Modifier.width(6.dp))
                    Icon(
                        if (expanded) Icons.Default.ArrowForward else Icons.Default.List,
                        contentDescription = null,
                        tint = TermOnSurfaceVar,
                        modifier = Modifier
                            .size(16.dp)
                    )
                }
            }

            if (expanded) {
                Spacer(Modifier.height(8.dp))
                Text(
                    bc.description,
                    color = TermOnSurfaceVar,
                    fontSize = 12.sp
                )
                Spacer(Modifier.height(8.dp))

                // Code diff
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = TermBg)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("【迁移前】❌", color = TermError, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(4.dp))
                        Text(
                            bc.beforeCode,
                            color = TermOnSurfaceVar,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(Modifier.height(8.dp))
                        Text("【迁移后】✅", color = TermSuccess, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(4.dp))
                        Text(
                            bc.afterCode,
                            color = TermSuccess,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))
                Text(
                    "⏱ 预估工时: ${bc.estimatedEffortMinutes} 分钟",
                    color = TermOnSurfaceVar,
                    fontSize = 11.sp
                )
            }
        }
    }
}

@Composable
private fun DiffCard(
    title: String,
    beforeLabel: String,
    afterLabel: String,
    beforeContent: String,
    afterContent: String,
    onCopyBefore: () -> Unit,
    onCopyAfter: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = TermSurface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, color = TermOnSurface, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Before
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(beforeLabel, color = TermError, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        IconButton(
                            onClick = onCopyBefore,
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                Icons.Default.ContentCopy,
                                contentDescription = "Copy",
                                tint = TermOnSurfaceVar,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = TermBg)
                    ) {
                        Text(
                            beforeContent,
                            color = TermError.copy(alpha = 0.8f),
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier
                                .padding(8.dp)
                                .horizontalScroll(rememberScrollState())
                        )
                    }
                }

                // After
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(afterLabel, color = TermSuccess, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        IconButton(
                            onClick = onCopyAfter,
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                Icons.Default.ContentCopy,
                                contentDescription = "Copy",
                                tint = TermOnSurfaceVar,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = TermBg)
                    ) {
                        Text(
                            afterContent,
                            color = TermSuccess,
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier
                                .padding(8.dp)
                                .horizontalScroll(rememberScrollState())
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DaoMigrationCard(
    migration: DaoMigration,
    onCopy: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = TermSurfaceVar),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "${migration.daoFunction.daoInterfaceName}.${migration.daoFunction.functionName}()",
                        color = TermOnSurface,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        migration.daoFunction.filePath,
                        color = TermOnSurfaceVar,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                IconButton(onClick = onCopy) {
                    Icon(
                        Icons.Default.ContentCopy,
                        contentDescription = "Copy",
                        tint = TermGreen,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = TermBg)
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                  Text("【After】✅", color = TermSuccess, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                  Spacer(Modifier.height(2.dp))
                  Text(
                      migration.afterCode,
                      color = TermSuccess,
                      fontSize = 10.sp,
                      fontFamily = FontFamily.Monospace
                  )
                }
            }

            Spacer(Modifier.height(4.dp))

            Text(
                "💡 ${migration.daoFunction.suggestedMigration}",
                color = TermOnSurfaceVar,
                fontSize = 11.sp
            )
        }
    }
}

@Composable
private fun RawQueryTemplateCard(template: RawQueryTemplate) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded },
        colors = CardDefaults.cardColors(containerColor = TermSurfaceVar),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "📝 ${template.name}",
                        color = TermOnSurface,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        template.applicableScenario,
                        color = TermOnSurfaceVar,
                        fontSize = 11.sp
                    )
                }
                Icon(
                    if (expanded) Icons.Default.ArrowForward else Icons.Default.List,
                    contentDescription = null,
                    tint = TermOnSurfaceVar,
                    modifier = Modifier.size(16.dp)
                )
            }

            if (expanded) {
                Spacer(Modifier.height(8.dp))
                Text(template.description, color = TermOnSurfaceVar, fontSize = 12.sp)
                Spacer(Modifier.height(8.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = TermBg)
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text("【Before】❌", color = TermError, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Text(template.beforeCode, color = TermError, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                        Spacer(Modifier.height(8.dp))
                        Text("【After】✅", color = TermSuccess, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Text(template.afterCode, color = TermSuccess, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                    }
                }
            }
        }
    }
}

@Composable
private fun PackageImportCard(pkg: PackageImport) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = TermSurfaceVar),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "${pkg.originalPackage} → ${pkg.newPackage}",
                        color = TermOnSurface,
                        fontSize = 13.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        "出现 ${pkg.occurrences} 次，影响 ${pkg.affectedFiles.size} 个文件",
                        color = TermOnSurfaceVar,
                        fontSize = 11.sp
                    )
                }
                BadgeChip(text = if (pkg.isReplaced) "已完成" else "待处理", color = if (pkg.isReplaced) TermSuccess else TermWarning)
            }
        }
    }
}

@Composable
private fun MigrationProgressBar(progress: MigrationProgress) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("整体进度", color = TermOnSurface, fontSize = 13.sp)
            Text(
                "${progress.overallPercent.toInt()}%",
                color = TermGreen,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(Modifier.height(6.dp))
        LinearProgressIndicator(
            progress = { progress.overallPercent / 100f },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = TermGreen,
            trackColor = TermSurfaceVar
        )
    }
}

@Composable
private fun ProgressStat(label: String, done: Int, total: Int, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            "$done/$total",
            color = color,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
        Text(label, color = TermOnSurfaceVar, fontSize = 11.sp)
    }
}

@Composable
private fun MigrationPhaseCard(phase: MigrationPhase) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded },
        colors = CardDefaults.cardColors(
            containerColor = if (phase.isCompleted) TermSuccess.copy(alpha = 0.1f) else TermSurfaceVar
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "Phase ${phase.phaseNumber}",
                        color = if (phase.isCompleted) TermSuccess else TermOnSurfaceVar,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        phase.title,
                        color = TermOnSurface,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "${phase.estimatedHours}h",
                        color = TermOnSurfaceVar,
                        fontSize = 11.sp
                    )
                    Spacer(Modifier.width(6.dp))
                    Icon(
                        if (expanded) Icons.Default.ArrowForward else Icons.Default.List,
                        contentDescription = null,
                        tint = TermOnSurfaceVar,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            if (expanded) {
                Spacer(Modifier.height(8.dp))
                Text(phase.description, color = TermOnSurfaceVar, fontSize = 12.sp)
                Spacer(Modifier.height(8.dp))
                phase.tasks.forEach { task ->
                    Row(
                        modifier = Modifier.padding(vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            if (task.isCompleted) Icons.Default.CheckCircle else Icons.Default.Schedule,
                            contentDescription = null,
                            tint = if (task.isCompleted) TermSuccess else TermOnSurfaceVar,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(task.title, color = TermOnSurface, fontSize = 12.sp)
                    }
                }
                if (phase.dependencies.isNotEmpty()) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "依赖 Phase: ${phase.dependencies.joinToString(", ")}",
                        color = TermOnSurfaceVar,
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}
