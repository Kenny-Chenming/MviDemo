package com.mvi.kenny.feature.kmpagp90migration

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

// ===== Color Palette — KMP × AGP 9.0 Dark Terminal Theme =====
// ===== 配色方案 — KMP × AGP 9.0 深色终端主题 =====

private val KMPPrimary = Color(0xFF4FC3F7)
private val KMPSecondary = Color(0xFF80DEEA)
private val KMPAcent = Color(0xFFB39DDB)
private val KMPSurface = Color(0xFF1A1A2E)
private val KMPSurfaceVariant = Color(0xFF16213E)
private val KMPBg = Color(0xFF0F0F23)
private val UrgencyCritical = Color(0xFFFF453A)
private val UrgencyHigh = Color(0xFFFF9F0A)
private val UrgencyMedium = Color(0xFFFFD60A)
private val UrgencyLow = Color(0xFF30D158)
private val OnSurfaceLight = Color(0xFFE0E0E0)
private val OnSurfaceDim = Color(0xFF8E8E93)
private val CodeBlock = Color(0xFF1E3A5F)

// ===== Tab Definitions =====
// ===== Tab 定义 =====

private enum class KMPTab(val label: String, val labelCn: String, val icon: ImageVector) {
    Scanner("Scanner", "不兼容检测", Icons.Default.Search),
    ModuleSplit("Module Split", "模块拆分", Icons.Default.AccountTree),
    PluginMigrate("Plugin Migrate", "插件迁移", Icons.Default.Upgrade),
    GradleMigrate("Gradle 9", "Gradle迁移", Icons.Default.LocalFireDepartment),
    CICompliance("CI + Countdown", "CI合规+倒计时", Icons.Default.Timer)
}

// ===== Main Screen =====
// ===== 主屏幕 =====

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KMPAGP90Screen(viewModel: KMPAGP90ViewModel = viewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Column(modifier = Modifier.fillMaxSize().background(KMPBg)) {
        // Top App Bar
        TopAppBar(
            title = {
                Column {
                    Text("KMP × AGP 9.0", style = MaterialTheme.typography.titleMedium, color = Color.White)
                    Text("不兼容迁移工具包 / Incompatibility Migration Toolkit",
                        style = MaterialTheme.typography.bodySmall, color = OnSurfaceDim)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = KMPSurface),
            actions = {
                val urgencyColor = when (state.scanUrgency) {
                    UrgencyLevel.CRITICAL -> UrgencyCritical
                    UrgencyLevel.HIGH -> UrgencyHigh
                    UrgencyLevel.MEDIUM -> UrgencyMedium
                    UrgencyLevel.LOW -> UrgencyLow
                }
                Surface(color = urgencyColor.copy(alpha = 0.2f), shape = RoundedCornerShape(4.dp)) {
                    Text(state.scanUrgency.name,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        color = urgencyColor, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.width(8.dp))
            }
        )

        // Tab Row
        ScrollableTabRow(
            selectedTabIndex = state.selectedTab,
            containerColor = KMPSurface,
            contentColor = KMPPrimary,
            edgePadding = 12.dp
        ) {
            KMPTab.entries.forEachIndexed { index, tab ->
                Tab(
                    selected = state.selectedTab == index,
                    onClick = { viewModel.processIntent(KMPAGP90Intent.SelectTab(index)) },
                    text = {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(tab.label, fontSize = 12.sp,
                                fontWeight = if (state.selectedTab == index) FontWeight.Bold else FontWeight.Normal)
                            Text(tab.labelCn, fontSize = 9.sp, color = OnSurfaceDim)
                        }
                    },
                    icon = { Icon(tab.icon, contentDescription = tab.label, modifier = Modifier.size(20.dp)) }
                )
            }
        }

        // Tab Content
        Box(modifier = Modifier.fillMaxSize().background(KMPBg)) {
            when (state.selectedTab) {
                0 -> ScannerTabContent(state, viewModel)
                1 -> ModuleSplitTabContent(state, viewModel)
                2 -> PluginMigrateTabContent(state, viewModel)
                3 -> GradleMigrateTabContent(state, viewModel)
                4 -> CIComplianceTabContent(state, viewModel)
            }
        }
    }
}

// ===== Tab 1: 不兼容检测扫描器 =====
// ===== Tab 1: Incompatible Plugin Scanner =====

@Composable
private fun ScannerTabContent(state: KMPAGP90State, viewModel: KMPAGP90ViewModel) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        // Project path input card
        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = KMPSurface), shape = RoundedCornerShape(12.dp)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.FolderOpen, null, tint = KMPPrimary, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("项目路径 / Project Path", style = MaterialTheme.typography.titleSmall, color = Color.White)
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = state.projectPath, onValueChange = { viewModel.processIntent(KMPAGP90Intent.UpdateProjectPath(it)) },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("输入 KMP 项目路径 / Enter KMP project path", color = OnSurfaceDim) },
                    textStyle = LocalTextStyle.current.copy(color = Color.White),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = KMPPrimary, unfocusedBorderColor = OnSurfaceDim, cursorColor = KMPPrimary),
                    trailingIcon = { if (state.isScanning) CircularProgressIndicator(modifier = Modifier.size(20.dp), color = KMPPrimary, strokeWidth = 2.dp) }
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row {
                    Button(onClick = { viewModel.processIntent(KMPAGP90Intent.StartScan) },
                        enabled = !state.isScanning && state.projectPath.isNotEmpty(),
                        colors = ButtonDefaults.buttonColors(containerColor = KMPPrimary)) {
                        Icon(Icons.Default.Search, null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (state.isScanning) "扫描中..." else "开始扫描 / Start Scan")
                    }
                    if (state.isScanning) {
                        Spacer(modifier = Modifier.width(8.dp))
                        OutlinedButton(onClick = { viewModel.processIntent(KMPAGP90Intent.CancelScan) },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = UrgencyHigh)) { Text("取消 / Cancel") }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Scan status card
        if (state.scanStatus != ScanStatus.IDLE) {
            ScanStatusCard(state)
            Spacer(modifier = Modifier.height(12.dp))
        }

        // Scan results list
        if (state.scanResults.isNotEmpty()) {
            Text("检测结果 / Scan Results (${state.scanResults.size})", style = MaterialTheme.typography.titleSmall, color = Color.White, modifier = Modifier.padding(bottom = 8.dp))
            LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                itemsIndexed(state.scanResults) { _, result -> IncompatiblePluginCard(result, state.expandedCardId, viewModel) }
            }
        } else if (state.scanStatus == ScanStatus.IDLE) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Security, null, tint = OnSurfaceDim, modifier = Modifier.size(64.dp))
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("输入项目路径后点击「开始扫描」", color = OnSurfaceDim)
                    Text("Enter project path and click Start Scan", color = OnSurfaceDim, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable private fun ScanStatusCard(state: KMPAGP90State) {
    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = KMPSurfaceVariant), shape = RoundedCornerShape(8.dp)) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            val (statusIcon, statusColor) = when (state.scanStatus) {
                ScanStatus.SCANNING -> Icons.Default.Pending to KMPPrimary
                ScanStatus.COMPLETED -> Icons.Default.CheckCircle to UrgencyLow
                ScanStatus.ERROR -> Icons.Default.Error to UrgencyCritical
                ScanStatus.IDLE -> Icons.Default.Info to OnSurfaceDim
            }
            Icon(statusIcon, null, tint = statusColor, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(when (state.scanStatus) {
                ScanStatus.SCANNING -> "扫描中... / Scanning..."
                ScanStatus.COMPLETED -> "扫描完成 ${state.scanResults.size} 个模块 / ${state.scanResults.size} modules scanned"
                ScanStatus.ERROR -> "扫描失败: ${state.scanError ?: "Unknown error"}"
                ScanStatus.IDLE -> "就绪 / Ready"
            }, color = Color.White, fontSize = 14.sp)
            if (state.isScanning) { Spacer(modifier = Modifier.width(8.dp)); CircularProgressIndicator(modifier = Modifier.size(16.dp), color = KMPPrimary, strokeWidth = 2.dp) }
        }
    }
}

@Composable private fun IncompatiblePluginCard(result: IncompatiblePlugin, expandedId: String?, viewModel: KMPAGP90ViewModel) {
    val cardId = result.modulePath
    val isExpanded = expandedId == cardId
    val urgencyColor = when (result.urgency) {
        UrgencyLevel.CRITICAL -> UrgencyCritical; UrgencyLevel.HIGH -> UrgencyHigh
        UrgencyLevel.MEDIUM -> UrgencyMedium; UrgencyLevel.LOW -> UrgencyLow
    }
    Card(modifier = Modifier.fillMaxWidth().animateContentSize(), colors = CardDefaults.cardColors(containerColor = KMPSurface), shape = RoundedCornerShape(8.dp)) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(modifier = Modifier.fillMaxWidth().clickable { viewModel.processIntent(KMPAGP90Intent.ToggleResultExpand(cardId)) }, verticalAlignment = Alignment.CenterVertically) {
                Surface(color = urgencyColor.copy(alpha = 0.2f), shape = RoundedCornerShape(4.dp)) {
                    Text(result.urgency.name, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), color = urgencyColor, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(result.moduleName, style = MaterialTheme.typography.titleSmall, color = Color.White, fontWeight = FontWeight.Bold)
                    Text(result.modulePath, style = MaterialTheme.typography.bodySmall, color = OnSurfaceDim, fontSize = 11.sp)
                }
                Icon(if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore, null, tint = OnSurfaceDim)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Surface(color = UrgencyCritical.copy(alpha = 0.1f), shape = RoundedCornerShape(4.dp)) {
                Text("⚠ ${result.incompatiblePair}", modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), color = UrgencyCritical, fontSize = 12.sp)
            }
            AnimatedVisibility(visible = isExpanded) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    HorizontalDivider(color = OnSurfaceDim.copy(alpha = 0.2f))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("建议 / Suggestion:", color = KMPPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Text(result.suggestion, color = OnSurfaceLight, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("迁移步骤 / Migration Step:", color = KMPSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Text(result.migrationStep, color = OnSurfaceLight, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("当前插件 / Current Plugins:", color = OnSurfaceDim, fontSize = 11.sp)
                    result.currentPlugins.forEach { Text("  • $it", color = OnSurfaceDim, fontSize = 11.sp, fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace) }
                }
            }
        }
    }
}

// ===== Tab 2: KMP 模块拆分规划 =====
// ===== Tab 2: KMP Module Split Planner =====

@Composable private fun ModuleSplitTabContent(state: KMPAGP90State, viewModel: KMPAGP90ViewModel) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = KMPSurface), shape = RoundedCornerShape(12.dp)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AccountTree, null, tint = KMPPrimary, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("KMP 模块拆分规划 / Module Split Planner", style = MaterialTheme.typography.titleSmall, color = Color.White)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text("将旧的 composeApp 单模块拆分为 androidApp + composeApp 双模块", color = OnSurfaceDim, fontSize = 12.sp)
                Text("Split legacy composeApp single-module into androidApp + composeApp dual-module", color = OnSurfaceDim, fontSize = 11.sp)
                Spacer(modifier = Modifier.height(12.dp))
                Row {
                    Button(onClick = { viewModel.processIntent(KMPAGP90Intent.AnalyzeStructure) }, enabled = !state.isAnalyzing,
                        colors = ButtonDefaults.buttonColors(containerColor = KMPPrimary)) {
                        Icon(Icons.Default.Analytics, null, modifier = Modifier.size(16.dp)); Spacer(modifier = Modifier.width(4.dp)); Text("分析结构 / Analyze")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedButton(onClick = { viewModel.processIntent(KMPAGP90Intent.GenerateSplitDiff) }, enabled = !state.isAnalyzing,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = KMPSecondary)) {
                        Icon(Icons.Default.Difference, null, modifier = Modifier.size(16.dp)); Spacer(modifier = Modifier.width(4.dp)); Text("生成 Diff")
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item {
                if (state.currentStructure != null) {
                    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = KMPSurfaceVariant), shape = RoundedCornerShape(8.dp)) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("📁 当前结构 / Current Structure", color = KMPPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("${state.currentStructure.moduleName} (${state.currentStructure.modulePath})", color = Color.White, fontSize = 13.sp)
                            Text("hasAndroidApp=${state.currentStructure.hasAndroidApp}, hasComposeApp=${state.currentStructure.hasComposeApp}", color = OnSurfaceDim, fontSize = 11.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("文件列表 / Files:", color = OnSurfaceDim, fontSize = 11.sp)
                            state.currentStructure.files.forEach { Text("  • $it", color = OnSurfaceDim, fontSize = 10.sp, fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace) }
                        }
                    }
                }
            }
            item {
                if (state.splitDiff != null) {
                    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = KMPSurface), shape = RoundedCornerShape(8.dp)) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("🔀 拆分方案 / Split Plan", color = KMPSecondary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(modifier = Modifier.fillMaxWidth()) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Surface(color = Color(0xFF1B4332), shape = RoundedCornerShape(4.dp)) { Text("✅ androidApp (新增)", modifier = Modifier.padding(4.dp), color = UrgencyLow, fontSize = 11.sp) }
                                    Text(state.splitDiff.newAndroidApp, color = Color.White, fontSize = 12.sp)
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Surface(color = Color(0xFF1B3A4B), shape = RoundedCornerShape(4.dp)) { Text("🔵 composeApp (保留)", modifier = Modifier.padding(4.dp), color = KMPPrimary, fontSize = 11.sp) }
                                    Text(state.splitDiff.newComposeApp, color = Color.White, fontSize = 12.sp)
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("📦 移动文件 / Moved Files:", color = OnSurfaceDim, fontSize = 11.sp)
                            state.splitDiff.movedFiles.forEach { Text("  • $it", color = OnSurfaceLight, fontSize = 10.sp, fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace) }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("⚙ settings.gradle.kts 变更:", color = OnSurfaceDim, fontSize = 11.sp)
                            Surface(color = CodeBlock, shape = RoundedCornerShape(4.dp)) {
                                Text(state.splitDiff.settingsChanges, modifier = Modifier.padding(8.dp), color = KMPSecondary, fontSize = 10.sp, fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace)
                            }
                        }
                    }
                }
            }
            if (state.currentStructure == null && state.splitDiff == null) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.AccountTree, null, tint = OnSurfaceDim, modifier = Modifier.size(48.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("点击「分析结构」开始", color = OnSurfaceDim)
                        }
                    }
                }
            }
        }
    }
}

// ===== Tab 3: Plugin 迁移 =====
// ===== Tab 3: Plugin Migration =====

private val migrationSteps = listOf(
    MigrationStep(1, "Update plugins block", "更新 plugins 块",
        "Replace android.library + kotlin.multiplatform with the new KMP Library Plugin",
        "将 android.library + kotlin.multiplatform 替换为新的 KMP Library Plugin",
        """plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.multiplatform")
    id("org.jetbrains.kotlin.android")
}""",
        """plugins {
    id("com.android.kotlin.multiplatform.library")
    // org.jetbrains.kotlin.android is now built-in
}""",
        "composeApp/build.gradle.kts"),
    MigrationStep(2, "Remove android {} kotlin block", "移除 android {} kotlin 块",
        "Built-in Kotlin is provided by AGP 9.0",
        "AGP 9.0 已内置 Kotlin 支持",
        """android {
    namespace = "com.example.app"
    kotlin { android() }
}""",
        """android {
    namespace = "com.example.app"
    // kotlin {} block removed
}""",
        "composeApp/build.gradle.kts"),
    MigrationStep(3, "Update iosTarget", "更新 iosTarget",
        "Swift Export is enabled by default in Kotlin 2.2.20+",
        "Swift Export 在 Kotlin 2.2.20+ 中默认启用",
        "kotlin { iosArm64() }",
        "kotlin { iosArm64() /* Swift Export default */ }",
        "composeApp/build.gradle.kts"),
    MigrationStep(4, "Update build-logic", "更新 build-logic",
        "Update convention plugins to use KMP Plugin",
        "更新 build-logic 中的插件以使用 KMP Plugin",
        "id(\"org.jetbrains.kotlin.multiplatform\")",
        "id(\"com.android.kotlin.multiplatform.library\")",
        "build-logic/convention/build.gradle.kts"),
    MigrationStep(5, "Update Gradle wrapper", "更新 Gradle wrapper",
        "AGP 9.0 requires Gradle 9.0+",
        "AGP 9.0 要求 Gradle 9.0+",
        "gradle-8.7-bin.zip",
        "gradle-9.0-bin.zip",
        "gradle/wrapper/gradle-wrapper.properties")
)

@Composable private fun PluginMigrateTabContent(state: KMPAGP90State, viewModel: KMPAGP90ViewModel) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = KMPSurface), shape = RoundedCornerShape(12.dp)) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Upgrade, null, tint = KMPAcent, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Plugin 迁移步骤 / Plugin Migration Steps", style = MaterialTheme.typography.titleSmall, color = Color.White)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text("com.android.library + kotlin.multiplatform → com.android.kotlin.multiplatform.library",
                    color = UrgencyHigh, fontSize = 11.sp, fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace)
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            migrationSteps.forEachIndexed { index, _ ->
                FilterChip(selected = state.selectedStep == index,
                    onClick = { viewModel.processIntent(KMPAGP90Intent.SelectMigrationStep(index)) },
                    label = { Text("Step ${index + 1}", fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = KMPPrimary, selectedLabelColor = Color.Black))
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            itemsIndexed(migrationSteps) { index, step ->
                MigrationStepCard(step = step, isSelected = state.selectedStep == index) {
                    viewModel.processIntent(KMPAGP90Intent.SelectMigrationStep(index))
                }
            }
        }
    }
}

@Composable private fun MigrationStepCard(step: MigrationStep, isSelected: Boolean, onClick: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = KMPSurface), shape = RoundedCornerShape(8.dp)) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(color = KMPPrimary, shape = RoundedCornerShape(50)) {
                    Text("${step.order}", modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp), color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(step.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text(step.titleCn, color = KMPSecondary, fontSize = 12.sp)
                }
                Text(step.file, color = OnSurfaceDim, fontSize = 10.sp, fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(step.description, color = OnSurfaceLight, fontSize = 12.sp)
            Text(step.descriptionCn, color = OnSurfaceDim, fontSize = 11.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("BEFORE", color = UrgencyHigh, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Surface(color = CodeBlock, shape = RoundedCornerShape(4.dp)) {
                        Text(step.codeBefore, modifier = Modifier.padding(8.dp), color = UrgencyHigh, fontSize = 10.sp, fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace)
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("AFTER", color = UrgencyLow, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Surface(color = CodeBlock, shape = RoundedCornerShape(4.dp)) {
                        Text(step.codeAfter, modifier = Modifier.padding(8.dp), color = UrgencyLow, fontSize = 10.sp, fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace)
                    }
                }
            }
        }
    }
}

// ===== Tab 4: Built-in Kotlin + Gradle 9.0 =====
// ===== Tab 4: Built-in Kotlin + Gradle 9.0 Migration =====

@Composable private fun GradleMigrateTabContent(state: KMPAGP90State, viewModel: KMPAGP90ViewModel) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = KMPSurface), shape = RoundedCornerShape(12.dp)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocalFireDepartment, null, tint = UrgencyHigh, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Built-in Kotlin 检测", style = MaterialTheme.typography.titleSmall, color = Color.White)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Built-in Kotlin Detection", style = MaterialTheme.typography.bodySmall, color = OnSurfaceDim)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text("检测 build.gradle.kts 中是否显式 apply 了 org.jetbrains.kotlin.android", color = OnSurfaceDim, fontSize = 12.sp)
                Text("Detect whether build.gradle.kts explicitly applies org.jetbrains.kotlin.android", color = OnSurfaceDim, fontSize = 11.sp)
                Spacer(modifier = Modifier.height(12.dp))
                Button(onClick = { viewModel.processIntent(KMPAGP90Intent.DetectBuiltInKotlin) },
                    enabled = !state.builtInKotlinDetected,
                    colors = ButtonDefaults.buttonColors(containerColor = UrgencyHigh)) {
                    Icon(Icons.Default.Search, null, modifier = Modifier.size(16.dp)); Spacer(modifier = Modifier.width(4.dp)); Text("检测 Built-in Kotlin")
                }
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        if (state.builtInKotlinDetected) {
            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = KMPSurfaceVariant), shape = RoundedCornerShape(8.dp)) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row {
                        Icon(if (state.kotlinPluginExplicit) Icons.Default.Warning else Icons.Default.CheckCircle,
                            null, tint = if (state.kotlinPluginExplicit) UrgencyHigh else UrgencyLow, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(if (state.kotlinPluginExplicit) "⚠️ 检测到显式 kotlin.android apply" else "✅ 未检测到显式 kotlin.android",
                                color = Color.White, fontWeight = FontWeight.Bold)
                            Text(if (state.kotlinPluginExplicit) "显式 kotlin.android 在 AGP 9.0 下应移除" else "Built-in Kotlin 已启用",
                                color = OnSurfaceDim, fontSize = 12.sp)
                        }
                    }
                    if (state.currentGradleVersion.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("当前 Gradle 版本: ${state.currentGradleVersion}", color = OnSurfaceLight, fontSize = 12.sp)
                        Text("AGP 9.0 需要 Gradle 9.0+", color = UrgencyHigh, fontSize = 12.sp)
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = KMPSurface), shape = RoundedCornerShape(8.dp)) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text("推荐迁移顺序 / Recommended Migration Order", color = KMPPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(8.dp))
                MigrationOrder.entries.forEach { order ->
                    val isSelected = state.recommendedMigrationOrder == order
                    Row(modifier = Modifier.fillMaxWidth().clickable { viewModel.processIntent(KMPAGP90Intent.SetMigrationOrder(order)) }.padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = isSelected, onClick = { viewModel.processIntent(KMPAGP90Intent.SetMigrationOrder(order)) },
                            colors = RadioButtonDefaults.colors(selectedColor = KMPPrimary))
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(when (order) {
                                MigrationOrder.KSP2_FIRST -> "① KSP2 先 → ② AGP 9.0"
                                MigrationOrder.AGP90_FIRST -> "① AGP 9.0 先 → ② KSP2"
                                MigrationOrder.PARALLEL -> "① KSP2 + AGP 9.0 并行迁移"
                            }, color = if (isSelected) KMPPrimary else Color.White, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal, fontSize = 13.sp)
                            Text(when (order) {
                                MigrationOrder.KSP2_FIRST -> "Recommended: Migrate KSP2 first, then AGP 9.0"
                                MigrationOrder.AGP90_FIRST -> "Alternative: Migrate AGP 9.0 first, then KSP2"
                                MigrationOrder.PARALLEL -> "Advanced: Migrate both simultaneously"
                            }, color = OnSurfaceDim, fontSize = 11.sp)
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text("Gradle 9.0 Breaking Changes", color = UrgencyHigh, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        Spacer(modifier = Modifier.height(8.dp))
        LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            itemsIndexed(state.gradleBreakingChanges.ifEmpty { simulatedGradleBreakingChangesStatic }) { _, change ->
                BreakingChangeCard(change)
            }
        }
    }
}

private val simulatedGradleBreakingChangesStatic = listOf(
    GradleBreakingChange("8.x → 9.0", "KotlinCompileDaemon removed",
        "KotlinCompileDaemon 已移除", "ksp.useKSP2 and kotlin.daemon.jvmargs moved to Gradle daemon",
        "ksp.useKSP2 和 kotlin.daemon.jvmargs 移至 Gradle daemon", UrgencyLevel.HIGH),
    GradleBreakingChange("8.x → 9.0", "buildSrc no longer supported",
        "buildSrc 不再支持", "Move convention plugins from buildSrc to settings.gradle.kts",
        "将 convention 插件从 buildSrc 移至 settings.gradle.kts", UrgencyLevel.MEDIUM),
    GradleBreakingChange("8.x → 9.0", "AGP 9.0 requires namespace",
        "AGP 9.0 要求 namespace", "android.library must have namespace property set",
        "android.library 必须设置 namespace 属性", UrgencyLevel.MEDIUM)
)

@Composable private fun BreakingChangeCard(change: GradleBreakingChange) {
    val severityColor = when (change.severity) {
        UrgencyLevel.CRITICAL -> UrgencyCritical; UrgencyLevel.HIGH -> UrgencyHigh
        UrgencyLevel.MEDIUM -> UrgencyMedium; UrgencyLevel.LOW -> UrgencyLow
    }
    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = KMPSurface), shape = RoundedCornerShape(8.dp)) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(color = severityColor.copy(alpha = 0.2f), shape = RoundedCornerShape(4.dp)) {
                    Text(change.severity.name, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), color = severityColor, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(change.version, color = KMPPrimary, fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace, fontSize = 12.sp)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(change.change, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            Text(change.changeCn, color = OnSurfaceDim, fontSize = 11.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text("Impact:", color = KMPSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Text(change.impact, color = OnSurfaceLight, fontSize = 12.sp)
            Text(change.impactCn, color = OnSurfaceDim, fontSize = 11.sp)
        }
    }
}

// ===== Tab 5: CI 合规 + AGP 10.0 倒计时 =====
// ===== Tab 5: CI Compliance + AGP 10.0 Countdown =====

@Composable private fun CIComplianceTabContent(state: KMPAGP90State, viewModel: KMPAGP90ViewModel) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        // Countdown banner
        Card(modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = if ((state.countdownWarning?.daysRemaining ?: 365) < 90) UrgencyCritical.copy(alpha = 0.15f) else KMPSurface),
            shape = RoundedCornerShape(12.dp)) {
            Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.Timer, null,
                    tint = if ((state.countdownWarning?.daysRemaining ?: 365) < 90) UrgencyCritical else KMPPrimary,
                    modifier = Modifier.size(40.dp))
                Spacer(modifier = Modifier.height(8.dp))
                Text("AGP 10.0 ${if (state.countdownWarning !=null) "倒计时" else "预计发布时间"}",
                    style = MaterialTheme.typography.titleMedium, color = Color.White)
                if (state.countdownWarning != null) {
                    Text("${state.countdownWarning.daysRemaining} 天",
                        style = MaterialTheme.typography.headlineLarge,
                        color = if (state.countdownWarning.isUrgent) UrgencyCritical else UrgencyHigh,
                        fontWeight = FontWeight.Bold)
                    Text(state.countdownWarning.messageCn, color = OnSurfaceDim, fontSize = 12.sp)
                    Text(state.countdownWarning.message, color = OnSurfaceDim, fontSize = 11.sp)
                } else {
                    Text("android.builtInKotlin=false 即将彻底失效",
                        color = UrgencyMedium, fontSize = 13.sp)
                    Text("android.builtInKotlin=false will be removed in AGP 10.0",
                        color = OnSurfaceDim, fontSize = 11.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // CI Compliance Status card
        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = KMPSurface), shape = RoundedCornerShape(12.dp)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.VerifiedUser, null, tint = KMPPrimary, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("CI 合规检测 / CI Compliance Check", style = MaterialTheme.typography.titleSmall, color = Color.White)
                }
                Spacer(modifier = Modifier.height(12.dp))

                val complianceColor = when (state.ciComplianceStatus) {
                    ComplianceStatus.COMPLIANT -> UrgencyLow
                    ComplianceStatus.NON_COMPLIANT -> UrgencyCritical
                    ComplianceStatus.PARTIAL -> UrgencyMedium
                    ComplianceStatus.UNKNOWN -> OnSurfaceDim
                }
                val complianceText = when (state.ciComplianceStatus) {
                    ComplianceStatus.COMPLIANT -> "✅ 合规 / Compliant"
                    ComplianceStatus.NON_COMPLIANT -> "❌ 不合规 / Non-Compliant"
                    ComplianceStatus.PARTIAL -> "⚠️ 部分合规 / Partial"
                    ComplianceStatus.UNKNOWN -> "⏳ 未知 / Unknown"
                }
                val complianceDesc = when (state.ciComplianceStatus) {
                    ComplianceStatus.COMPLIANT -> "所有 KMP 模块已正确迁移到 AGP 9.0"
                    ComplianceStatus.NON_COMPLIANT -> "部分 KMP 模块仍使用不兼容的插件组合"
                    ComplianceStatus.PARTIAL -> "迁移进行中，部分模块已合规"
                    ComplianceStatus.UNKNOWN -> "点击「验证合规」开始检测"
                }

                Surface(color = complianceColor.copy(alpha = 0.15f), shape = RoundedCornerShape(8.dp)) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(complianceText, color = complianceColor, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text(complianceDesc, color = OnSurfaceLight, fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (state.ciComplianceStatus != ComplianceStatus.UNKNOWN) {
                    Text("迁移进度 / Migration Progress: ${state.ciMigrationProgress}%", color = OnSurfaceDim, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = { state.ciMigrationProgress / 100f },
                        modifier = Modifier.fillMaxWidth().height(8.dp),
                        color = if (state.ciMigrationProgress == 100) UrgencyLow else KMPPrimary,
                        trackColor = KMPSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }

                Row {
                    Button(onClick = { viewModel.processIntent(KMPAGP90Intent.ValidateCICompliance) },
                        enabled = state.ciComplianceStatus == ComplianceStatus.UNKNOWN || state.ciComplianceStatus == ComplianceStatus.NON_COMPLIANT,
                        colors = ButtonDefaults.buttonColors(containerColor = KMPPrimary)) {
                        Icon(Icons.Default.CheckCircle, null, modifier = Modifier.size(16.dp)); Spacer(modifier = Modifier.width(4.dp)); Text("验证合规 / Validate")
                    }
                    if (state.ciComplianceStatus == ComplianceStatus.NON_COMPLIANT) {
                        Spacer(modifier = Modifier.width(8.dp))
                        OutlinedButton(onClick = { viewModel.processIntent(KMPAGP90Intent.MarkMigrationComplete) },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = UrgencyLow)) {
                            Icon(Icons.Default.Done, null, modifier = Modifier.size(16.dp)); Spacer(modifier = Modifier.width(4.dp)); Text("标记完成 / Mark Done")
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Migration complete banner
        if (state.migrationComplete) {
            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = UrgencyLow.copy(alpha = 0.15f)), shape = RoundedCornerShape(12.dp)) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Celebration, null, tint = UrgencyLow, modifier = Modifier.size(32.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("🎉 迁移完成 / Migration Complete!", color = UrgencyLow, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text("KMP × AGP 9.0 迁移已全部完成", color = Color.White, fontSize = 13.sp)
                        Text("All KMP modules are now AGP 9.0 compatible", color = OnSurfaceDim, fontSize = 11.sp)
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        // AGP 10.0 countdown warning details
        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = KMPSurfaceVariant), shape = RoundedCornerShape(8.dp)) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text("⚠️ android.builtInKotlin=false 倒计时警告", color = UrgencyMedium, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text("此配置项在 AGP 10.0 发布后将彻底失效。届时所有使用该配置的项目将无法编译。",
                    color = OnSurfaceLight, fontSize = 12.sp)
                Text("This property will be removed in AGP 10.0. Projects using it will fail to compile.",
                    color = OnSurfaceDim, fontSize = 11.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Surface(color = CodeBlock, shape = RoundedCornerShape(4.dp)) {
                    Text("# gradle.properties\nandroid.builtInKotlin=false  # AGP 10.0后将失效",
                        modifier = Modifier.padding(8.dp), color = UrgencyMedium, fontSize = 11.sp,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace)
                }
            }
        }
    }
}
