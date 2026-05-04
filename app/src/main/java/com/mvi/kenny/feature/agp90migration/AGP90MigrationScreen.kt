package com.mvi.kenny.feature.agp90migration

// ================================================================
// AGP90MigrationScreen — AGP 9.0 破坏性变更迁移工具包 Screen
// ================================================================
// MVI Screen for AGP 9.0 Breaking Changes Migration Toolkit.
//
// PRD-226: AGP 9.0 破坏性变更迁移工具包
// Design Reference: memory/agency/designs/PRD-226-AGP-9-0-破坏性变更迁移工具包.md
//
// Style: 深色 Terminal 风格（Dark Terminal Theme）
// Colors: #121212 bg, #4FC3F7 primary accent, #EF5350 high risk
// ================================================================
//
// Tab Structure:
//   Tab 0: 密度Split检测 — Detect splits.density config, output App Bundle alternative
//   Tab 1: DSL稳定化 — incubating→stable API changes by AGP version
//   Tab 2: BuildConfig & NDK — BuildConfig field changes + NDK compliance check
//   Tab 3: Flutter兼容性 [P0] — Flutter AGP 9.0 incompatibility workaround
//   Tab 4: 升级路径 — Gradle 9.0 + AGP 9.0 upgrade path + CI compliance + checklist
// ================================================================

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

// ================================================================
// Theme Colors / 主题颜色
// ================================================================

private object AGP90Theme {
    val Background = Color(0xFF121212)        // 深色背景
    val Surface = Color(0xFF1E1E1E)          // Card 背景
    val SurfaceVariant = Color(0xFF2D2D2D)   // 变体背景
    val Primary = Color(0xFF4FC3F7)          // 主强调色
    val OnPrimary = Color(0xFF003547)        // 主色文字
    val Secondary = Color(0xFF03DAC6)        // 次强调色
    val Error = Color(0xFFEF5350)            // 高风险色
    val Warning = Color(0xFFFFB74D)           // 中风险色
    val Success = Color(0xFF66BB6A)           // 低风险色
    val TextPrimary = Color(0xFFE0E0E0)       // 主文字
    val TextSecondary = Color(0xFF9E9E9E)     // 次文字
    val CodeBackground = Color(0xFF1E1E1E)   // 代码背景
    val CodeText = Color(0xFFD4D4D4)          // 代码文字
    val TabIndicator = Color(0xFF4FC3F7)      // Tab 指示器
}

// ================================================================
// Tab Data / Tab 数据
// ================================================================

private data class TabItem(
    val title: String,
    val subtitle: String,
    val icon: String
)

private val tabItems = listOf(
    TabItem("密度Split", "Splits Density", "📦"),
    TabItem("DSL稳定化", "DSL Stabilization", "🔧"),
    TabItem("BuildConfig", "& NDK", "🔧"),
    TabItem("Flutter兼容", "Flutter Compat [P0]", "🔥"),
    TabItem("升级路径", "Migration Path", "🚀")
)

// ================================================================
// Main Screen / 主界面
// ================================================================

@Composable
fun AGP90MigrationScreen(
    viewModel: AGP90MigrationViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val clipboardManager = LocalClipboardManager.current

    // 监听 Effect / Listen to Effects
    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is AGP90MigrationEffect.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(effect.message)
                }
                is AGP90MigrationEffect.CopyToClipboard -> {
                    // Clipboard handled in individual items
                }
                is AGP90MigrationEffect.OpenUrl -> {
                    // Open URL - would need browser intent
                }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            NavigationBar(
                containerColor = AGP90Theme.Surface,
                contentColor = AGP90Theme.TextPrimary
            ) {
                tabItems.forEachIndexed { index, tab ->
                    val selected = state.selectedTab == index
                    NavigationBarItem(
                        selected = selected,
                        onClick = { viewModel.processIntent(AGP90MigrationIntent.SelectTab(index)) },
                        icon = {
                            Text(
                                text = tab.icon,
                                fontSize = 20.sp
                            )
                        },
                        label = {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = tab.title,
                                    fontSize = 11.sp,
                                    color = if (selected) AGP90Theme.Primary else AGP90Theme.TextSecondary
                                )
                                if (tab.subtitle.isNotEmpty()) {
                                    Text(
                                        text = tab.subtitle,
                                        fontSize = 9.sp,
                                        color = AGP90Theme.TextSecondary
                                    )
                                }
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = AGP90Theme.Primary,
                            unselectedIconColor = AGP90Theme.TextSecondary,
                            indicatorColor = AGP90Theme.Primary.copy(alpha = 0.2f)
                        )
                    )
                }
            }
        },
        containerColor = AGP90Theme.Background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(AGP90Theme.Background)
        ) {
            // Header / 头部
            AGP90Header()

            // Tab Content / Tab 内容
            when (state.selectedTab) {
                0 -> SplitsDensityTab(state, viewModel)
                1 -> DSLStabilizationTab(state, viewModel)
                2 -> BuildConfigNDKTab(state, viewModel)
                3 -> FlutterCompatTab(state, viewModel)
                4 -> MigrationPathTab(state, viewModel)
            }
        }
    }
}

// ================================================================
// Header / 头部
// ================================================================

@Composable
private fun AGP90Header() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(AGP90Theme.Surface)
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "🔧",
                fontSize = 24.sp
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = "AGP 9.0 破坏性变更迁移工具包",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = AGP90Theme.TextPrimary
                )
                Text(
                    text = "Android Gradle Plugin 9.0 Breaking Changes Migration Toolkit",
                    fontSize = 12.sp,
                    color = AGP90Theme.TextSecondary
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            RiskBadge("P0", "严重", AGP90Theme.Error)
            RiskBadge("P1", "高风险", AGP90Theme.Warning)
            RiskBadge("splits.density", "完全移除", AGP90Theme.Error)
            RiskBadge("Flutter", "不兼容", AGP90Theme.Error)
        }
    }
}

@Composable
private fun RiskBadge(label: String, desc: String, color: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(color.copy(alpha = 0.2f))
            .border(1.dp, color, RoundedCornerShape(4.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = "$label $desc",
            fontSize = 10.sp,
            color = color
        )
    }
}

// ================================================================
// Tab 0: Splits Density / 密度Split检测
// ================================================================

@Composable
private fun SplitsDensityTab(
    state: AGP90MigrationState,
    viewModel: AGP90MigrationViewModel
) {
    val clipboardManager = LocalClipboardManager.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            SectionTitle(
                title = "splits.density 完全移除",
                subtitle = "AGP 9.0 历史上首次完全移除已有功能",
                emoji = "🔴 P0"
            )
        }

        if (state.isScanning) {
            item {
                ScanningIndicator()
            }
        }

        items(state.splitsDensityResults) { result ->
            SplitsDensityCard(
                result = result,
                onCopy = { code ->
                    viewModel.processIntent(AGP90MigrationIntent.CopyCode(code))
                }
            )
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun SplitsDensityCard(
    result: SplitsDensityResult,
    onCopy: (String) -> Unit
) {
    val clipboardManager = LocalClipboardManager.current

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = AGP90Theme.Surface),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // File Path / 文件路径
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "📄 ${result.filePath}",
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    color = AGP90Theme.Primary
                )
                RiskBadge(result.riskLevel.displayName, result.riskLevel.emoji, result.riskLevel.color)
            }

            // Current Config / 当前配置
            Text(
                text = "❌ 当前配置 (Invalid in AGP 9.0)",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = AGP90Theme.Error
            )
            CodeBlock(
                code = result.currentConfig,
                onCopy = { onCopy(result.currentConfig) }
            )

            HorizontalDivider(color = AGP90Theme.SurfaceVariant)

            // Alternative / 替代方案
            Text(
                text = "✅ 替代方案 (App Bundle)",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = AGP90Theme.Success
            )
            CodeBlock(
                code = result.alternative,
                onCopy = { onCopy(result.alternative) }
            )

            // Recommendation / 建议
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(4.dp))
                    .background(AGP90Theme.Error.copy(alpha = 0.1f))
                    .border(1.dp, AGP90Theme.Error, RoundedCornerShape(4.dp))
                    .padding(8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = AGP90Theme.Error,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = result.recommendation,
                        fontSize = 12.sp,
                        color = AGP90Theme.Error
                    )
                }
            }
        }
    }
}

// ================================================================
// Tab 1: DSL Stabilization / DSL稳定化
// ================================================================

@Composable
private fun DSLStabilizationTab(
    state: AGP90MigrationState,
    viewModel: AGP90MigrationViewModel
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            SectionTitle(
                title = "DSL 稳定化变更",
                subtitle = "incubating → stable API 变化（按 AGP 版本分组）",
                emoji = "🔧"
            )
        }

        // Group by AGP version / 按 AGP 版本分组
        val groupedChanges = state.dslChanges.groupBy { it.agpVersion }
        groupedChanges.forEach { (version, changes) ->
            item {
                VersionGroupHeader(version = version)
            }
            items(changes) { change ->
                DSLChangeCard(
                    change = change,
                    onCopy = { code ->
                        viewModel.processIntent(AGP90MigrationIntent.CopyCode(code))
                    }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun VersionGroupHeader(version: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(AGP90Theme.Primary.copy(alpha = 0.2f))
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = "AGP $version",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = AGP90Theme.Primary
        )
    }
}

@Composable
private fun DSLChangeCard(
    change: DSLChange,
    onCopy: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = AGP90Theme.Surface),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // API Name & Risk / API名称 & 风险
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = change.apiName,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = AGP90Theme.TextPrimary
                )
                RiskBadge(
                    change.riskLevel.displayName,
                    change.changeType,
                    change.riskLevel.color
                )
            }

            // Package migration / 包迁移
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "旧包 (Incubating)",
                        fontSize = 10.sp,
                        color = AGP90Theme.TextSecondary
                    )
                    Text(
                        text = change.oldPackage,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        color = AGP90Theme.Error
                    )
                }
                Text(
                    text = "→",
                    color = AGP90Theme.TextSecondary,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "新包 (Stable)",
                        fontSize = 10.sp,
                        color = AGP90Theme.TextSecondary
                    )
                    Text(
                        text = change.newPackage,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        color = AGP90Theme.Success
                    )
                }
            }

            HorizontalDivider(color = AGP90Theme.SurfaceVariant)

            // Before Code / 变更前
            Text(
                text = "❌ Before (AGP < 4.1/4.2/7.0)",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = AGP90Theme.Error
            )
            CodeBlock(
                code = change.beforeCode,
                onCopy = { onCopy(change.beforeCode) }
            )

            // After Code / 变更后
            Text(
                text = "✅ After (AGP >= 4.1/4.2/7.0)",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = AGP90Theme.Success
            )
            CodeBlock(
                code = change.afterCode,
                onCopy = { onCopy(change.afterCode) }
            )
        }
    }
}

// ================================================================
// Tab 2: BuildConfig & NDK / BuildConfig分析 & NDK合规
// ================================================================

@Composable
private fun BuildConfigNDKTab(
    state: AGP90MigrationState,
    viewModel: AGP90MigrationViewModel
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            SectionTitle(
                title = "BuildConfig & NDK 合规检测",
                subtitle = "AGP 9.0 BuildConfig 变更分析 + NDK 版本合规检测",
                emoji = "🔧"
            )
        }

        // BuildConfig Analysis / BuildConfig 分析
        state.buildConfigAnalysis?.let { analysis ->
            item {
                BuildConfigCard(
                    analysis = analysis,
                    onCopy = { code ->
                        viewModel.processIntent(AGP90MigrationIntent.CopyCode(code))
                    }
                )
            }
        }

        // NDK Compliance / NDK 合规
        state.ndkComplianceResult?.let { result ->
            item {
                NDKComplianceCard(result = result)
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun BuildConfigCard(
    analysis: BuildConfigAnalysis,
    onCopy: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = AGP90Theme.Surface),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "BuildConfig 变更分析",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = AGP90Theme.TextPrimary
                )
                RiskBadge(
                    analysis.riskLevel.displayName,
                    analysis.riskLevel.emoji,
                    analysis.riskLevel.color
                )
            }

            // Field Name / 字段名
            Text(
                text = "📋 ${analysis.fieldName}",
                fontSize = 13.sp,
                fontFamily = FontFamily.Monospace,
                color = AGP90Theme.Primary
            )

            // Behavior Change / 行为变更
            BehaviorRow("旧行为", analysis.oldBehavior, AGP90Theme.Error)
            BehaviorRow("新行为", analysis.newBehavior, AGP90Theme.Success)

            HorizontalDivider(color = AGP90Theme.SurfaceVariant)

            // Affected Code / 受影响代码
            Text(
                text = "⚠️ 受影响的代码",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = AGP90Theme.Warning
            )
            CodeBlock(
                code = analysis.affectedCode,
                onCopy = { onCopy(analysis.affectedCode) }
            )

            // Migration Step / 迁移步骤
            Text(
                text = "✅ 迁移步骤",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = AGP90Theme.Success
            )
            CodeBlock(
                code = analysis.migrationStep,
                onCopy = { onCopy(analysis.migrationStep) }
            )
        }
    }
}

@Composable
private fun BehaviorRow(label: String, content: String, color: Color) {
    Column {
        Text(
            text = label,
            fontSize = 11.sp,
            color = color,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = content,
            fontSize = 12.sp,
            color = AGP90Theme.TextSecondary
        )
    }
}

@Composable
private fun NDKComplianceCard(result: NDKComplianceResult) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = AGP90Theme.Surface),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "NDK 合规检测",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = AGP90Theme.TextPrimary
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(
                            if (result.isCompliant) AGP90Theme.Success.copy(alpha = 0.2f)
                            else AGP90Theme.Error.copy(alpha = 0.2f)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (result.isCompliant) "✅ 合规" else "❌ 不合规",
                        fontSize = 12.sp,
                        color = if (result.isCompliant) AGP90Theme.Success else AGP90Theme.Error
                    )
                }
            }

            // Version Info / 版本信息
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                VersionInfoBox(
                    label = "当前版本",
                    value = result.currentVersion,
                    color = if (result.isCompliant) AGP90Theme.Success else AGP90Theme.Error
                )
                VersionInfoBox(
                    label = "最低要求",
                    value = result.minRequiredVersion,
                    color = AGP90Theme.Primary
                )
            }

            // Problems / 问题列表
            if (result.currentVersionProblems.isNotEmpty()) {
                Text(
                    text = "⚠️ 当前版本问题",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = AGP90Theme.Warning
                )
                result.currentVersionProblems.forEach { problem ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = "•",
                            color = AGP90Theme.Error,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text(
                            text = problem,
                            fontSize = 12.sp,
                            color = AGP90Theme.TextSecondary
                        )
                    }
                }
            }

            HorizontalDivider(color = AGP90Theme.SurfaceVariant)

            // Upgrade Path / 升级路径
            Text(
                text = "🚀 升级路径",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = AGP90Theme.Success
            )
            CodeBlock(
                code = result.upgradePath,
                onCopy = { }
            )
        }
    }
}

@Composable
private fun VersionInfoBox(label: String, value: String, color: Color) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(AGP90Theme.SurfaceVariant)
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            color = AGP90Theme.TextSecondary
        )
        Text(
            text = value,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            color = color
        )
    }
}

// ================================================================
// Tab 3: Flutter Compatibility / Flutter兼容性 [P0]
// ================================================================

@Composable
private fun FlutterCompatTab(
    state: AGP90MigrationState,
    viewModel: AGP90MigrationViewModel
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            // P0 Banner / P0 警告横幅
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(AGP90Theme.Error.copy(alpha = 0.15f))
                    .border(2.dp, AGP90Theme.Error, RoundedCornerShape(8.dp))
                    .padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "🚨",
                        fontSize = 28.sp
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "P0 BLOCKER — Flutter 官方明确警告不兼容",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = AGP90Theme.Error
                        )
                        Text(
                            text = "Flutter apps using plugins are currently incompatible with AGP 9",
                            fontSize = 12.sp,
                            color = AGP90Theme.TextSecondary
                        )
                    }
                }
            }
        }

        state.flutterCompatInfo?.let { info ->
            // Status Card / 状态卡片
            item {
                FlutterStatusCard(info = info)
            }

            // Workarounds / Workaround 列表
            item {
                SectionTitle(
                    title = "Workaround 步骤",
                    subtitle = "在 Flutter 官方修复前使用的临时方案",
                    emoji = "📋"
                )
            }

            items(info.workarounds) { workaround ->
                FlutterWorkaroundCard(
                    workaround = workaround,
                    onCopy = { code ->
                        viewModel.processIntent(AGP90MigrationIntent.CopyCode(code ?: ""))
                    }
                )
            }

            // Official Issue / 官方 Issue
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = AGP90Theme.Surface),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "🔗 官方 Issue 追踪",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = AGP90Theme.TextPrimary
                        )
                        Text(
                            text = info.officialIssueUrl,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            color = AGP90Theme.Primary
                        )
                        Text(
                            text = "预计修复版本: ${info.estimatedFixVersion}",
                            fontSize = 12.sp,
                            color = AGP90Theme.TextSecondary
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun FlutterStatusCard(info: FlutterCompatInfo) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = AGP90Theme.Surface),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Flutter 兼容性状态",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = AGP90Theme.TextPrimary
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(AGP90Theme.Error.copy(alpha = 0.2f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "❌ 不兼容",
                        fontSize = 12.sp,
                        color = AGP90Theme.Error
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                VersionInfoBox(
                    label = "Flutter",
                    value = info.flutterVersion,
                    color = AGP90Theme.Error
                )
                VersionInfoBox(
                    label = "Dart",
                    value = info.dartVersion,
                    color = AGP90Theme.Warning
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(4.dp))
                    .background(AGP90Theme.Error.copy(alpha = 0.1f))
                    .padding(8.dp)
            ) {
                Text(
                    text = info.currentStatus,
                    fontSize = 12.sp,
                    color = AGP90Theme.Error,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun FlutterWorkaroundCard(
    workaround: FlutterWorkaround,
    onCopy: (String?) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = AGP90Theme.Surface),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(AGP90Theme.Primary.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${workaround.step}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = AGP90Theme.Primary
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = workaround.title,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = AGP90Theme.TextPrimary
                    )
                }
                workaround.code?.let {
                    IconButton(
                        onClick = { onCopy(it) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy",
                            tint = AGP90Theme.TextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Text(
                text = workaround.description,
                fontSize = 12.sp,
                color = AGP90Theme.TextSecondary
            )

            workaround.code?.let { code ->
                CodeBlock(
                    code = code,
                    onCopy = { onCopy(code) }
                )
            }
        }
    }
}

// ================================================================
// Tab 4: Migration Path / 升级路径
// ================================================================

@Composable
private fun MigrationPathTab(
    state: AGP90MigrationState,
    viewModel: AGP90MigrationViewModel
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            SectionTitle(
                title = "Gradle 9.0 + AGP 9.0 升级路径",
                subtitle = "版本兼容矩阵 + CI 合规检测 + 完整检查清单",
                emoji = "🚀"
            )
        }

        // Version Matrix / 版本兼容矩阵
        state.migrationPath?.let { path ->
            item {
                VersionMatrixCard(migrationPath = path)
            }

            // Upgrade Order / 升级顺序
            item {
                UpgradeOrderCard(migrationPath = path)
            }
        }

        // CI Compliance / CI 合规
        state.ciComplianceStatus?.let { status ->
            item {
                CIComplianceCard(status = status)
            }
        }

        // Checklist / 检查清单
        item {
            ChecklistCard(
                checklistItems = state.checklistItems,
                completedCount = state.completedChecklistCount,
                onToggle = { itemId, checked ->
                    viewModel.processIntent(AGP90MigrationIntent.ToggleChecklist(itemId, checked))
                }
            )
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun VersionMatrixCard(migrationPath: MigrationPath) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = AGP90Theme.Surface),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "📊 版本兼容矩阵",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = AGP90Theme.TextPrimary
            )

            // Matrix table / 矩阵表格
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Header
                Box(
                    modifier = Modifier
                        .width(80.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(AGP90Theme.SurfaceVariant)
                        .padding(8.dp)
                ) {
                    Text(
                        text = "Gradle \\ AGP",
                        fontSize = 10.sp,
                        color = AGP90Theme.TextSecondary
                    )
                }
                migrationPath.compatibilityMatrix.take(5).forEach {
                    Box(
                        modifier = Modifier
                            .width(70.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(AGP90Theme.SurfaceVariant)
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = it.agpVersion,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = AGP90Theme.Primary
                        )
                    }
                }
                // Matrix rows / 矩阵行
                migrationPath.compatibilityMatrix.take(5).forEach { item ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .width(80.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(AGP90Theme.SurfaceVariant)
                                .padding(8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = item.gradleVersion,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = AGP90Theme.TextPrimary
                            )
                        }
                        migrationPath.compatibilityMatrix.take(5).filter { it.gradleVersion == item.gradleVersion }.forEach { rowItem ->
                            Box(
                                modifier = Modifier
                                    .width(70.dp)
                                    .height(32.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(
                                        if (rowItem.isCompatible) AGP90Theme.Success.copy(alpha = 0.2f)
                                        else AGP90Theme.Error.copy(alpha = 0.2f)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (rowItem.isCompatible) "✅" else "❌",
                                    fontSize = 14.sp
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
private fun UpgradeOrderCard(migrationPath: MigrationPath) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = AGP90Theme.Surface),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "🚀 升级顺序（必须先 Gradle 后 AGP）",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = AGP90Theme.TextPrimary
            )
            CodeBlock(
                code = migrationPath.upgradeOrder,
                onCopy = { }
            )
        }
    }
}

@Composable
private fun CIComplianceCard(status: CIComplianceStatus) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = AGP90Theme.Surface),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "CI 合规检测",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = AGP90Theme.TextPrimary
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(
                            if (status.overallPassed) AGP90Theme.Success.copy(alpha = 0.2f)
                            else AGP90Theme.Error.copy(alpha = 0.2f)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (status.overallPassed) "✅ 通过" else "❌ 未通过",
                        fontSize = 12.sp,
                        color = if (status.overallPassed) AGP90Theme.Success else AGP90Theme.Error
                    )
                }
            }

            // Version checks / 版本检查
            Text(
                text = status.agpVersionCheck,
                fontSize = 12.sp,
                color = AGP90Theme.Warning
            )
            Text(
                text = status.gradleVersionCheck,
                fontSize = 12.sp,
                color = AGP90Theme.Warning
            )

            // Blocking issues / 阻塞性问题
            if (status.blockingIssues.isNotEmpty()) {
                HorizontalDivider(color = AGP90Theme.SurfaceVariant)
                Text(
                    text = "⚠️ 阻塞性问题",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = AGP90Theme.Error
                )
                status.blockingIssues.forEach { issue ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = "•",
                            color = AGP90Theme.Error,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text(
                            text = issue,
                            fontSize = 11.sp,
                            color = AGP90Theme.TextSecondary
                        )
                    }
                }
            }

            // Recommendations / 建议
            if (status.recommendations.isNotEmpty()) {
                HorizontalDivider(color = AGP90Theme.SurfaceVariant)
                Text(
                    text = "💡 建议",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = AGP90Theme.Success
                )
                status.recommendations.forEach { rec ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = "✓",
                            color = AGP90Theme.Success,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text(
                            text = rec,
                            fontSize = 11.sp,
                            color = AGP90Theme.TextSecondary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ChecklistCard(
    checklistItems: List<ChecklistItem>,
    completedCount: Int,
    onToggle: (String, Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = AGP90Theme.Surface),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "📋 完整检查清单",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = AGP90Theme.TextPrimary
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(AGP90Theme.Primary.copy(alpha = 0.2f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "$completedCount/${checklistItems.size}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = AGP90Theme.Primary
                    )
                }
            }

            // Progress bar / 进度条
            val progress = if (checklistItems.isNotEmpty()) {
                completedCount.toFloat() / checklistItems.size
            } else 0f
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(AGP90Theme.SurfaceVariant)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progress)
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(AGP90Theme.Primary)
                )
            }

            // Group by priority / 按优先级分组
            val groupedItems = checklistItems.groupBy { it.priority }
            groupedItems.forEach { (priority, items) ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(4.dp))
                        .background(priority.color.copy(alpha = 0.1f))
                        .padding(8.dp)
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "${priority.emoji} ${priority.displayName}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = priority.color
                        )
                        items.forEach { item ->
                            ChecklistItemRow(
                                item = item,
                                onToggle = { checked -> onToggle(item.id, checked) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ChecklistItemRow(
    item: ChecklistItem,
    onToggle: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle(!item.isChecked) }
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = item.isChecked,
            onCheckedChange = onToggle,
            colors = CheckboxDefaults.colors(
                checkedColor = AGP90Theme.Success,
                uncheckedColor = AGP90Theme.TextSecondary
            ),
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(
                text = item.title,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = if (item.isChecked) AGP90Theme.TextSecondary else AGP90Theme.TextPrimary
            )
            Text(
                text = item.description,
                fontSize = 10.sp,
                color = AGP90Theme.TextSecondary
            )
        }
    }
}

@Composable
private fun SectionTitle(
    title: String,
    subtitle: String,
    emoji: String
) {
    Column(
        modifier = Modifier.padding(vertical = 8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = emoji,
                fontSize = 18.sp
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = AGP90Theme.TextPrimary
            )
        }
        Text(
            text = subtitle,
            fontSize = 12.sp,
            color = AGP90Theme.TextSecondary,
            modifier = Modifier.padding(start = 26.dp)
        )
    }
}

@Composable
private fun ScanningIndicator() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(24.dp),
            color = AGP90Theme.Primary,
            strokeWidth = 2.dp
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = "Scanning... / 扫描中...",
            fontSize = 14.sp,
            color = AGP90Theme.TextSecondary
        )
    }
}

@Composable
private fun CodeBlock(
    code: String,
    onCopy: () -> Unit
) {
    val clipboardManager = LocalClipboardManager.current

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = AGP90Theme.CodeBackground),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column {
            // Copy button / 复制按钮
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(
                    onClick = {
                        clipboardManager.setText(AnnotatedString(code))
                        onCopy()
                    },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copy",
                        tint = AGP90Theme.TextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            // Code content / 代码内容
            Text(
                text = code,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                color = AGP90Theme.CodeText,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            )
        }
    }
}