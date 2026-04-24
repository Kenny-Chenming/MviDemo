package com.mvi.kenny.feature.gridflexboxkit.submodules

// ============================================================
// GridDecisionTreeScreen — Grid/FlexBox 决策树屏幕
// PRD-141 | Compose Grid + FlexBox 双布局 API 开发工具包
// ============================================================
/**
 * 决策树屏幕
 * 帮助开发者根据场景选择最适合的布局类型。
 *
 * 决策维度：
 * 1. 数据规模（静态/虚拟化）
 * 2. 布局维度（1D/2D）
 * 3. 对齐需求
 * 4. 空间分配（固定/等分/比例/换行）
 */

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mvi.kenny.feature.gridflexboxkit.*

/**
 * Grid 决策树屏幕
 * Decision tree screen for choosing between Grid/FlexBox/LazyGrid/Column/Row.
 *
 * @param onBack 返回回调
 * @param viewModel ViewModel
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun GridDecisionTreeScreen(
    onBack: () -> Unit,
    viewModel: GridDecisionTreeViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()
    val currentNode = viewModel.getCurrentNode()

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is GridDecisionTreeEffect.ShowRecommendation -> {
                    // Recommendation shown via state
                }
                is GridDecisionTreeEffect.NavigateToPlayground -> {
                    // Navigate to playground
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Grid/FlexBox 决策树", style = MaterialTheme.typography.titleMedium)
                        Text(
                            "场景化最佳实践指南",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Rounded.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            // 决策路径面包屑 / Decision path breadcrumb
            if (state.currentPath.isNotEmpty()) {
                DecisionPathBreadcrumb(
                    path = state.currentPath,
                    onReset = { viewModel.sendIntent(GridDecisionTreeIntent.ResetTree) }
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            // 决策树内容区 / Decision tree content
            if (state.recommendedLayout != null) {
                // 显示推荐结果 / Show recommendation result
                RecommendationResult(
                    layoutType = state.recommendedLayout!!,
                    onReset = { viewModel.sendIntent(GridDecisionTreeIntent.ResetTree) },
                    onNavigateToPlayground = { viewModel.sendIntent(GridDecisionTreeIntent.NavigateToPlayground) },
                    modifier = Modifier.weight(1f)
                )
            } else if (currentNode != null) {
                // 显示当前决策节点 / Show current decision node
                DecisionNodeCard(
                    node = currentNode,
                    onSelectOption = { viewModel.sendIntent(GridDecisionTreeIntent.SelectOption(it)) },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 代码反查区 / Code analysis section
            CodeAnalysisSection(
                codeInput = state.codeInput,
                analysisResult = state.analysisResult,
                onAnalyze = { viewModel.sendIntent(GridDecisionTreeIntent.AnalyzeCode(it)) }
            )
        }
    }
}

/**
 * 决策路径面包屑 / Decision path breadcrumb
 */
@Composable
private fun DecisionPathBreadcrumb(
    path: List<String>,
    onReset: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                Icons.Rounded.Route,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
            path.forEachIndexed { index, step ->
                if (index > 0) {
                    Icon(
                        Icons.Rounded.ChevronRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Text(
                    text = step,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        TextButton(onClick = onReset) {
            Icon(Icons.Rounded.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("重置", style = MaterialTheme.typography.labelMedium)
        }
    }
}

/**
 * 决策节点卡片 / Decision node card
 */
@Composable
private fun DecisionNodeCard(
    node: DecisionNode,
    onSelectOption: (DecisionOption) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = node.question,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))
            node.options.forEach { option ->
                DecisionOptionItem(
                    option = option,
                    onClick = { onSelectOption(option) }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

/**
 * 决策选项项 / Decision option item
 */
@Composable
private fun DecisionOptionItem(
    option: DecisionOption,
    onClick: () -> Unit
) {
    val isRecommendation = option.recommendedLayout != null

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        color = if (isRecommendation)
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        else
            MaterialTheme.colorScheme.surfaceVariant,
        border = if (isRecommendation)
            androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
        else
            null
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = option.label,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f)
            )
            if (isRecommendation) {
                Icon(
                    Icons.Rounded.CheckCircle,
                    contentDescription = "Recommended",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            } else {
                Icon(
                    Icons.Rounded.ChevronRight,
                    contentDescription = "Next",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

/**
 * 推荐结果卡片 / Recommendation result card
 */
@Composable
private fun RecommendationResult(
    layoutType: LayoutType,
    onReset: () -> Unit,
    onNavigateToPlayground: () -> Unit,
    modifier: Modifier = Modifier
) {
    val layoutColor = when (layoutType) {
        LayoutType.Grid -> Color(0xFF1E88E5)
        LayoutType.FlexBox -> Color(0xFF8E24AA)
        LayoutType.LazyGrid -> Color(0xFF43A047)
        LayoutType.ColumnRow -> Color(0xFF757575)
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = layoutColor.copy(alpha = 0.1f)
        ),
        border = androidx.compose.foundation.BorderStroke(2.dp, layoutColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = when (layoutType) {
                    LayoutType.Grid -> Icons.Rounded.GridOn
                    LayoutType.FlexBox -> Icons.Rounded.ViewModule
                    LayoutType.LazyGrid -> Icons.Rounded.ViewList
                    LayoutType.ColumnRow -> Icons.Rounded.ViewAgenda
                },
                contentDescription = null,
                tint = layoutColor,
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "✅ 推荐结果",
                style = MaterialTheme.typography.labelLarge,
                color = layoutColor
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = layoutType.displayNameCn,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = layoutColor
            )
            Text(
                text = layoutType.displayNameEn,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = layoutType.description,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(24.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(onClick = onReset) {
                    Icon(Icons.Rounded.Refresh, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("重新选择")
                }
                Button(onClick = onNavigateToPlayground) {
                    Icon(Icons.Rounded.PlayArrow, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("去 Playground 体验")
                }
            }
        }
    }
}

/**
 * 代码分析区 / Code analysis section
 */
@Composable
private fun CodeAnalysisSection(
    codeInput: String,
    analysisResult: CodeAnalysisResult?,
    onAnalyze: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "🔍 代码反查 — 输入代码片段，自动分析应使用的布局类型",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = codeInput,
                onValueChange = { onAnalyze(it) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("粘贴代码片段，例如: LazyVerticalGrid(columns = 3) { ... }") },
                minLines = 2,
                maxLines = 4,
                textStyle = MaterialTheme.typography.bodySmall
            )

            if (analysisResult != null) {
                Spacer(modifier = Modifier.height(12.dp))
                val resultColor = when (analysisResult.detectedLayout) {
                    LayoutType.Grid -> Color(0xFF1E88E5)
                    LayoutType.FlexBox -> Color(0xFF8E24AA)
                    LayoutType.LazyGrid -> Color(0xFF43A047)
                    LayoutType.ColumnRow -> Color(0xFF757575)
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = resultColor.copy(alpha = 0.1f)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "检测结果：",
                                style = MaterialTheme.typography.labelMedium
                            )
                            Text(
                                text = analysisResult.detectedLayout.displayNameCn,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = resultColor
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "置信度 ${(analysisResult.confidence * 100).toInt()}%",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = analysisResult.suggestion,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
