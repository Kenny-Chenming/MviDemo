package com.mvi.kenny.feature.gridflexboxkit

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridItemSpan
import com.mvi.kenny.feature.gridflexboxkit.submodules.*
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

// ============================================================
// ComposeLayoutsKitScreen — 主入口屏幕
// PRD-141 | Compose Grid + FlexBox 双布局 API 开发工具包
// ============================================================
/**
 * ComposeLayoutsKit 主入口 Screen
 * 展示 8 个子模块入口卡片，点击后进入对应子模块。
 *
 * @param state 页面状态
 * @param onIntent 发送用户意图
 * @param onNavigateToSubmodule 导航到子模块回调
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComposeLayoutsKitScreen(
    state: ComposeLayoutsKitState,
    onIntent: (ComposeLayoutsKitIntent) -> Unit,
    onNavigateToSubmodule: (LayoutKitModule) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // 顶部标题栏 / Top header bar
        TopAppBar(
            title = {
                Column {
                    Text(
                        text = "ComposeLayoutsKit",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Compose Grid + FlexBox 双布局 API 开发工具包",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
            )
        )

        // 子模块网格 / Sub-modules grid
        // 使用 Grid API 展示模块卡片（自身即是对 Grid API 的展示）
        if (state.modules.isNotEmpty()) {
            ModulesGrid(
                modules = state.modules,
                onModuleClick = { module ->
                    onIntent(ComposeLayoutsKitIntent.SelectModule(module))
                    onNavigateToSubmodule(module)
                },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            )
        }
    }
}

/**
 * 子模块网格 — 使用 Compose Grid API 展示模块卡片
 * Sub-modules grid using Compose Grid API to showcase the Grid API itself.
 *
 * @param modules 子模块列表
 * @param onModuleClick 点击回调
 * @param modifier 修饰符
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ModulesGrid(
    modules: List<LayoutKitModule>,
    onModuleClick: (LayoutKitModule) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 首行：介绍卡片（跨两列）/ Introduction card (spans 2 columns)
        IntroductionCard(
            modifier = Modifier.fillMaxWidth()
        )

        // 模块卡片网格 / Module cards grid
        modules.chunked(2).forEach { rowModules ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                rowModules.forEach { module ->
                    ModuleCard(
                        module = module,
                        onClick = { onModuleClick(module) },
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1.2f)
                    )
                }
                // 如果只有单个模块（最后一行），填充空位 / Fill empty slot if single module
                if (rowModules.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

/**
 * 介绍卡片 / Introduction card
 */
@Composable
private fun IntroductionCard(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "🎉 Compose 1.11.0 Grid + FlexBox API",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onTertiaryContainer
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Jetpack Compose April 2026 引入两个全新的实验性布局 API。Grid API 实现二维网格布局，FlexBox API 实现高性能自适应 UI。本工具包提供场景化最佳实践、迁移指南、性能分析、调试面板和交互式 Playground。",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.8f)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AssistChip(
                    onClick = { },
                    label = { Text("Grid API", style = MaterialTheme.typography.labelSmall) },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        labelColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
                AssistChip(
                    onClick = { },
                    label = { Text("FlexBox API", style = MaterialTheme.typography.labelSmall) },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = MaterialTheme.colorScheme.secondary,
                        labelColor = MaterialTheme.colorScheme.onSecondary
                    )
                )
                AssistChip(
                    onClick = { },
                    label = { Text("Experimental", style = MaterialTheme.typography.labelSmall) },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        labelColor = MaterialTheme.colorScheme.onError
                    )
                )
            }
        }
    }
}

/**
 * 模块卡片组件 / Module card component
 *
 * @param module 模块信息
 * @param onClick 点击回调
 * @param modifier 修饰符
 */
@Composable
fun ModuleCard(
    module: LayoutKitModule,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // 模块图标和优先级标签 / Icon and priority badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                // 模块图标（使用 Material Symbols）/ Module icon
                val iconRes = when (module) {
                    LayoutKitModule.GridDecisionTree -> Icons.Rounded.AccountTree
                    LayoutKitModule.CssMigrationGuide -> Icons.Rounded.Code
                    LayoutKitModule.PerformanceAnalysis -> Icons.Rounded.Speed
                    LayoutKitModule.GridDebugPanel -> Icons.Rounded.GridOn
                    LayoutKitModule.FlexBoxDebugPanel -> Icons.Rounded.ViewModule
                    LayoutKitModule.AdaptiveLayout -> Icons.Rounded.Devices
                    LayoutKitModule.MigrationScanner -> Icons.Rounded.Search
                    LayoutKitModule.Playground -> Icons.Rounded.PlayArrow
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = when (module.priority) {
                        "P0" -> Color(0xFFE53935).copy(alpha = 0.1f)
                        "P1" -> Color(0xFFFF6F00).copy(alpha = 0.1f)
                        else -> Color(0xFF1E88E5).copy(alpha = 0.1f)
                    }
                ) {
                    Text(
                        text = module.priority,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = when (module.priority) {
                            "P0" -> Color(0xFFE53935)
                            "P1" -> Color(0xFFFF6F00)
                            else -> Color(0xFF1E88E5)
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 模块标题 / Module title
            Column {
                Text(
                    text = module.titleCn,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = module.titleEn,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // 描述 / Description
            Text(
                text = module.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            // 进入箭头 / Enter arrow
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Icon(
                    imageVector = Icons.Rounded.ChevronRight,
                    contentDescription = "Enter",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

// ============================================================
// 子模块屏幕入口 / Sub-module screen entry
// ============================================================
/**
 * 导航到子模块屏幕 / Navigate to sub-module screen
 * 根据 selectedModule 显示对应的子模块屏幕。
 *
 * @param module 选中的模块
 * @param onBack 返回回调
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ComposeLayoutsKitNavHost(
    module: LayoutKitModule,
    onBack: () -> Unit
) {
    when (module) {
        LayoutKitModule.GridDecisionTree -> GridDecisionTreeScreen(onBack = onBack)
        LayoutKitModule.CssMigrationGuide -> CssMigrationGuideScreen(onBack = onBack)
        LayoutKitModule.PerformanceAnalysis -> PerformanceAnalysisScreen(onBack = onBack)
        LayoutKitModule.GridDebugPanel -> GridDebugPanelScreen(onBack = onBack)
        LayoutKitModule.FlexBoxDebugPanel -> FlexBoxDebugPanelScreen(onBack = onBack)
        LayoutKitModule.AdaptiveLayout -> AdaptiveLayoutScreen(onBack = onBack)
        LayoutKitModule.MigrationScanner -> MigrationScannerScreen(onBack = onBack)
        LayoutKitModule.Playground -> PlaygroundScreen(onBack = onBack)
    }
}
