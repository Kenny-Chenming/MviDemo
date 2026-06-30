package com.mvi.kenny.feature.antigravity2

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Rocket
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.collectLatest

/**
 * ============================================================
 * Antigravity2Screen — Google Antigravity 2.0 主界面
 * ============================================================
 * PRD-293 | Google Antigravity 2.0 Android 集成开发工具包
 * Design: 仪表盘型 App，深色主题，4 Tab
 *
 * Tab 0: 🚀 概览仪表盘（DashboardScreen）
 * Tab 1: 🛠️ Android 工具链（AndroidToolsScreen）
 * Tab 2: 📦 Skills 市场（SkillsScreen）
 * Tab 3: ⚙️ 企业部署（EnterpriseScreen）
 */

// —————————————————————————————————————————————————————
// Colors — Antigravity 深空紫主题
// —————————————————————————————————————————————————————
private val AntigravityPurple = Color(0xFF7C3AED)
private val CyanAccent = Color(0xFF06B6D4)
private val AmberAccent = Color(0xFFF59E0B)
private val SurfaceDark = Color(0xFF0F0F1A)
private val SurfaceCardDark = Color(0xFF1A1A2E)
private val TextOnDark = Color(0xFFE2E8F0)

/**
 * Antigravity2Screen 主入口
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Antigravity2Screen(
    viewModel: Antigravity2ViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()
    val pagerState = rememberPagerState(pageCount = { 4 })

    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is Antigravity2Effect.ShowToast -> { /* Toast handled externally */ }
                is Antigravity2Effect.NavigateToSkillsDetail -> { /* Navigate */ }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SurfaceDark)
    ) {
        // Tab 指示器
        val tabs = listOf("🚀 概览仪表盘", "🛠️ Android 工具链", "📦 Skills 市场", "⚙️ 企业部署")
        ScrollableTabRow(
            selectedTabIndex = pagerState.currentPage,
            containerColor = SurfaceCardDark,
            edgePadding = 12.dp
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = pagerState.currentPage == index,
                    onClick = {
                        viewModel.sendIntent(Antigravity2Intent.SelectTab(index))
                    },
                    text = {
                        Text(
                            text = title,
                            fontWeight = if (pagerState.currentPage == index) FontWeight.Bold else FontWeight.Normal,
                            color = TextOnDark
                        )
                    }
                )
            }
        }

        // Pager
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            when (page) {
                0 -> DashboardTab(state, viewModel)
                1 -> AndroidToolsTab(state, viewModel)
                2 -> SkillsMarketTab(state, viewModel)
                3 -> EnterpriseTab(state, viewModel)
            }
        }
    }
}

// —————————————————————————————————————————————————————
// Tab 0: 🚀 概览仪表盘（Dashboard）
// —————————————————————————————————————————————————————
@Composable
private fun DashboardTab(state: Antigravity2State, viewModel: Antigravity2ViewModel) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(SurfaceDark),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero 横幅
        item {
            HeroBanner(
                countdownDays = state.countdownDays,
                onMigrate = { viewModel.sendIntent(Antigravity2Intent.SelectTab(3)) }
            )
        }

        // 最新动态时间线
        item {
            Text(
                text = "📰 最新动态",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextOnDark
            )
        }
        items(state.newsItems) { news ->
            NewsTimelineItem(news)
        }

        // 四大模块入口
        item {
            Text(
                text = "🧩 四大支柱",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextOnDark,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
        items(state.dashboardModules) { module ->
            DashboardModuleCard(module)
        }
    }
}

/**
 * Hero 横幅 — Gemini CLI 停用倒计时
 */
@Composable
private fun HeroBanner(countdownDays: Int, onMigrate: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = AntigravityPurple)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Rocket, contentDescription = null, tint = Color.White, modifier = Modifier.size(28.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Antigravity 2.0", style = MaterialTheme.typography.headlineSmall, color = Color.White, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text("统一 Agent 开发生态", style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.85f))
            Spacer(modifier = Modifier.height(12.dp))
            if (countdownDays > 0) {
                Card(
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Red.copy(alpha = 0.9f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("⚠️ Gemini CLI 停用倒计时: ", color = Color.White)
                        Text("$countdownDays 天", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            } else {
                Card(
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = AmberAccent)
                ) {
                    Text("⚠️ Gemini CLI 已停用，请立即迁移！", modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp), color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Button(onClick = onMigrate, shape = RoundedCornerShape(8.dp)) {
                Text("立即迁移")
                Spacer(modifier = Modifier.width(4.dp))
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
            }
        }
    }
}

/**
 * 最新动态时间线条目
 */
@Composable
private fun NewsTimelineItem(news: NewsItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (news.isUrgent) Color.Red.copy(alpha = 0.15f) else SurfaceCardDark
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(if (news.isUrgent) Color.Red else CyanAccent)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = news.title,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (news.isUrgent) Color.Red else TextOnDark,
                    maxLines = 2
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = news.date,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray
                )
            }
        }
    }
}

/**
 * 仪表盘模块卡片
 */
@Composable
private fun DashboardModuleCard(module: AntigravityModule) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCardDark)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(module.icon, fontSize = 28.sp)
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(module.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = TextOnDark)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    module.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    maxLines = 2
                )
            }
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = Color.Gray)
        }
    }
}

// —————————————————————————————————————————————————————
// Tab 1: 🛠️ Android 工具链
// —————————————————————————————————————————————————————
@Composable
private fun AndroidToolsTab(state: Antigravity2State, viewModel: Antigravity2ViewModel) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(SurfaceDark),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Android Resources Bundle 介绍
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = AntigravityPurple.copy(alpha = 0.2f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("📦 Android Resources Bundle", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextOnDark)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Antigravity 2.0 官方支持 Android 开发场景，包含 Android CLI Skills、SKILL.md 格式标准以及与 ADK 2.0 的深度集成。",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextOnDark.copy(alpha = 0.8f)
                    )
                }
            }
        }

        // Android CLI Skills 列表
        item {
            Text("🛠️ Android CLI Skills", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextOnDark)
        }
        items(state.androidBundles) { bundle ->
            BundleCard(bundle)
        }

        // Antigravity × Android CLI 协同架构图
        item {
            ArchDiagramCard()
        }
    }
}

@Composable
private fun BundleCard(bundle: AndroidResourceBundle) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCardDark)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(bundle.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = TextOnDark)
                Spacer(modifier = Modifier.height(4.dp))
                Text(bundle.description, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(CyanAccent.copy(alpha = 0.2f))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(bundle.category.name, style = MaterialTheme.typography.labelSmall, color = CyanAccent)
            }
        }
    }
}

@Composable
private fun ArchDiagramCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCardDark)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("🔗 Antigravity × Android CLI 协同架构", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextOnDark)
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(AntigravityPurple.copy(alpha = 0.3f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("AG", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextOnDark)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Antigravity", style = MaterialTheme.typography.labelSmall, color = TextOnDark)
                }
                Text("×", fontSize = 24.sp, color = CyanAccent, fontWeight = FontWeight.Bold)
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(CyanAccent.copy(alpha = 0.3f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("CLI", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextOnDark)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Android CLI", style = MaterialTheme.typography.labelSmall, color = TextOnDark)
                }
                Text("=", fontSize = 24.sp, color = AmberAccent, fontWeight = FontWeight.Bold)
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(AmberAccent.copy(alpha = 0.3f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("💎", fontSize = 24.sp)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("完整工具链", style = MaterialTheme.typography.labelSmall, color = TextOnDark)
                }
            }
        }
    }
}

// —————————————————————————————————————————————————————
// Tab 2: 📦 Skills 市场
// —————————————————————————————————————————————————————
@Composable
private fun SkillsMarketTab(state: Antigravity2State, viewModel: Antigravity2ViewModel) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(SurfaceDark),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 搜索栏
        item {
            OutlinedTextField(
                value = state.searchQuery,
                onValueChange = { viewModel.sendIntent(Antigravity2Intent.SearchSkills(it)) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("搜索 Skills...", color = Color.Gray) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray) },
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyMedium.copy(color = TextOnDark)
            )
        }

        // 分类筛选
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = true,
                    onClick = { },
                    label = { Text("全部", color = TextOnDark) }
                )
                SkillCategory.entries.forEach { cat ->
                    FilterChip(
                        selected = false,
                        onClick = { },
                        label = { Text(cat.name, color = TextOnDark) }
                    )
                }
            }
        }

        // SKILL.md 格式说明
        item {
            SkillFormatCard()
        }

        // Skills 列表
        items(state.filteredSkills) { skill ->
            SkillCard(skill)
        }
    }
}

@Composable
private fun SkillFormatCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CyanAccent.copy(alpha = 0.1f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text("📋 SKILL.md 格式说明", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = CyanAccent)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                """
                # SKILL.md 示例结构：
                - name: Skill 名称
                - description: 技能描述
                - triggers: 触发关键词
                - actions: 执行动作列表
                - examples: 使用示例
                """.trimIndent(),
                style = MaterialTheme.typography.bodySmall,
                color = TextOnDark.copy(alpha = 0.8f),
                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
            )
        }
    }
}

@Composable
private fun SkillCard(skill: AntigravitySkill) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCardDark)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(skill.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = TextOnDark)
                    if (skill.isPopular) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(modifier = Modifier.clip(RoundedCornerShape(4.dp)).background(AmberAccent).padding(horizontal = 6.dp, vertical = 2.dp)) {
                            Text("🔥 热门", style = MaterialTheme.typography.labelSmall, color = Color.Black)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(skill.description, style = MaterialTheme.typography.bodySmall, color = Color.Gray, maxLines = 2)
                Spacer(modifier = Modifier.height(4.dp))
                Box(modifier = Modifier.clip(RoundedCornerShape(4.dp)).background(AntigravityPurple.copy(alpha = 0.2f)).padding(horizontal = 6.dp, vertical = 2.dp)) {
                    Text(skill.category.name, style = MaterialTheme.typography.labelSmall, color = AntigravityPurple)
                }
            }
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = Color.Gray)
        }
    }
}

// —————————————————————————————————————————————————————
// Tab 3: ⚙️ 企业部署
// —————————————————————————————————————————————————————
@Composable
private fun EnterpriseTab(state: Antigravity2State, viewModel: Antigravity2ViewModel) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(SurfaceDark),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Gemini Enterprise Agent Platform 介绍
        item {
            EnterprisePlatformCard()
        }

        // 部署 Checklist
        item {
            Text("✅ 企业部署 Checklist", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextOnDark)
        }
        items(state.enterpriseChecklist) { item ->
            ChecklistCard(item) {
                viewModel.sendIntent(Antigravity2Intent.ToggleChecklistItem(item.id))
            }
        }

        // 迁移助手
        item {
            MigrationAssistant(state, viewModel)
        }
    }
}

@Composable
private fun EnterprisePlatformCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCardDark)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Settings, contentDescription = null, tint = AmberAccent, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Gemini Enterprise Agent Platform", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextOnDark)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "通过 Google Cloud Gemini Enterprise Agent Platform 提供企业级部署，支持私有网络、IAM 权限管理、数据合规保留。",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
        }
    }
}

@Composable
private fun ChecklistCard(item: ChecklistItem, onToggle: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (item.checked) SuccessGreen.copy(alpha = 0.1f) else SurfaceCardDark
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = item.checked,
                onCheckedChange = { onToggle() }
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(item.title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, color = TextOnDark)
                Text(item.description, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
            if (item.checked) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SuccessGreen)
            }
        }
    }
}

private val SuccessGreen = Color(0xFF34A853)

/**
 * 迁移助手 — Step-by-step 向导
 */
@Composable
private fun MigrationAssistant(state: Antigravity2State, viewModel: Antigravity2ViewModel) {
    val steps = listOf(
        "卸载 Gemini CLI",
        "安装 Antigravity CLI (Go)",
        "配置 Antigravity 认证",
        "迁移现有 Skills",
        "验证 Agent 运行"
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCardDark)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("🔄 Gemini CLI → Antigravity CLI 迁移助手", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextOnDark)
            Spacer(modifier = Modifier.height(12.dp))

            // 进度条
            LinearProgressIndicator(
                progress = { (state.migrationStep + 1).toFloat() / steps.size },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = CyanAccent,
                trackColor = Color.Gray.copy(alpha = 0.3f)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text("${state.migrationStep + 1} / ${steps.size}", style = MaterialTheme.typography.labelSmall, color = Color.Gray)

            Spacer(modifier = Modifier.height(16.dp))

            // 当前步骤
            val currentStep = steps.getOrNull(state.migrationStep) ?: steps.last()
            Text(
                text = "步骤 ${state.migrationStep + 1}: $currentStep",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = CyanAccent
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 导航按钮
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { viewModel.sendIntent(Antigravity2Intent.PreviousMigrationStep) },
                    enabled = state.migrationStep > 0,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("上一步")
                }
                Button(
                    onClick = { viewModel.sendIntent(Antigravity2Intent.NextMigrationStep) },
                    enabled = state.migrationStep < steps.size - 1,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("下一步")
                }
            }
        }
    }
}
