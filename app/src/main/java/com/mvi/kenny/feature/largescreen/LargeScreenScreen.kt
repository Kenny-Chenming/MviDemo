package com.mvi.kenny.feature.largescreen

// ================================================================
// LargeScreenScreen — Android 17 大屏强制适配与 Continuous Canary Release 主界面
// ================================================================
// Main screen for Android 17 Large Screen adaptation toolkit.
//
// PRD-155: Android 17 大屏强制适配与 Continuous Canary Release 开发工具包
// Design Reference: memory/agency/designs/PRD-155-Android-17-大屏强制适配与-Continuous-Canary-Release-开发工具包.md
//
// Features:
//   - Tool selector tabs (5 main tools)
//   - Tool-specific content area
//   - Shared state via LargeScreenViewModel
// —————————————————————————————————————————————————————————————

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.FactCheck
import androidx.compose.material.icons.filled.SyncAlt
import androidx.compose.material.icons.filled.ScreenSearchDesktop
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mvi.kenny.feature.largescreen.screens.CanaryWorkflowScreen
import com.mvi.kenny.feature.largescreen.screens.ComplianceDetectorScreen
import com.mvi.kenny.feature.largescreen.screens.FallbackDetectorScreen
import com.mvi.kenny.feature.largescreen.screens.FoldableGuideScreen
import com.mvi.kenny.feature.largescreen.screens.ScreenCaptureTestScreen
import kotlinx.coroutines.flow.collectLatest

/**
 * ============================================================
 * LargeScreenScreen — 大屏适配工具主界面
 * ============================================================
 * Container screen that renders the selected tool's content.
 * Uses HorizontalPager-style tool tabs at the top.
 *
 * @param viewModel LargeScreenViewModel instance
 * @param onUpdateTopBar TopBar 配置更新回调
 * @param onEffect Effect 处理回调（用于 snackbar/toast）
 *
 * @see LargeScreenViewModel
 * @see LargeScreenState
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LargeScreenScreen(
    viewModel: LargeScreenViewModel,
    onUpdateTopBar: (com.mvi.kenny.base.TopBarConfig) -> Unit,
    onSnackbar: (String) -> Unit
) {
    val state by viewModel.state.collectAsState()

    // Update TopBar when tool changes
    LaunchedEffect(state.currentTool) {
        onUpdateTopBar(
            com.mvi.kenny.base.TopBarConfig(
                title = "大屏适配 · ${state.currentTool.title}",
                actions = emptyList()
            )
        )
    }

    // Listen for effects
    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is LargeScreenEffect.ShowSnackbar -> onSnackbar(effect.message)
                is LargeScreenEffect.ShowToast -> onSnackbar(effect.message)
                is LargeScreenEffect.DiffGenerated -> onSnackbar("Diff 已生成")
                is LargeScreenEffect.ReportExported -> onSnackbar("报告已导出: ${effect.filePath}")
                is LargeScreenEffect.PlayStoreRejectionRisk -> {
                    onSnackbar("Play Store 拒绝风险: ${effect.riskLevel.displayName}")
                }
                is LargeScreenEffect.APIChangeNotification -> {
                    onSnackbar("API 变更: ${effect.change.title}")
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        // ─────────────────────────────────────────────────────────
        // Tool Tab Row — 七大工具切换
        // ─────────────────────────────────────────────────────────
        ToolTabRow(
            currentTool = state.currentTool,
            onToolSelected = { viewModel.sendIntent(LargeScreenIntent.SelectTool(it)) }
        )

        // ─────────────────────────────────────────────────────────
        // Tool Content Area
        // ─────────────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            when (state.currentTool) {
                LargeScreenTool.COMPLIANCE_DETECTOR -> ComplianceDetectorScreen(
                    state = state,
                    onIntent = viewModel::sendIntent
                )
                LargeScreenTool.SCREEN_CAPTURE_TEST -> ScreenCaptureTestScreen(
                    state = state,
                    onIntent = viewModel::sendIntent
                )
                LargeScreenTool.CANARY_WORKFLOW -> CanaryWorkflowScreen(
                    state = state,
                    onIntent = viewModel::sendIntent
                )
                LargeScreenTool.FALLBACK_DETECTOR -> FallbackDetectorScreen(
                    state = state,
                    onIntent = viewModel::sendIntent
                )
                LargeScreenTool.ADAPTIVE_CHECKLIST -> FoldableGuideScreen(
                    state = state,
                    onIntent = viewModel::sendIntent
                )
            }
        }
    }
}

// ================================================================
// ToolTabRow — 工具 Tab 行
// ================================================================

/**
 * Tool tab data for rendering.
 */
private data class ToolTabData(
    val tool: LargeScreenTool,
    val icon: ImageVector,
    val badge: Int? = null
)

/**
 * ============================================================
 * ToolTabRow — 工具 Tab 切换行
 * ============================================================
 * Horizontal scrollable row of tool selection chips.
 *
 * @param currentTool 当前选中的工具
 * @param onToolSelected 工具选中回调
 */
@Composable
private fun ToolTabRow(
    currentTool: LargeScreenTool,
    onToolSelected: (LargeScreenTool) -> Unit
) {
    val tools = listOf(
        ToolTabData(LargeScreenTool.COMPLIANCE_DETECTOR, Icons.Default.Shield),
        ToolTabData(LargeScreenTool.SCREEN_CAPTURE_TEST, Icons.Default.ScreenSearchDesktop),
        ToolTabData(LargeScreenTool.CANARY_WORKFLOW, Icons.Default.SyncAlt),
        ToolTabData(LargeScreenTool.FALLBACK_DETECTOR, Icons.Default.SwapHoriz),
        ToolTabData(LargeScreenTool.ADAPTIVE_CHECKLIST, Icons.Default.FactCheck)
    )

    val scrollState = rememberScrollState()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        tools.forEach { toolData ->
            val selected = currentTool == toolData.tool
            FilterChip(
                selected = selected,
                onClick = { onToolSelected(toolData.tool) },
                label = {
                    Text(
                        text = toolData.tool.title,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = toolData.icon,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Color(0xFF6750A4),
                    selectedLabelColor = Color.White,
                    selectedLeadingIconColor = Color.White
                )
            )
        }
    }
}

// ================================================================
// Utility Components
// ================================================================

/**
 * Summary card showing a key metric.
 */
@Composable
fun SummaryCard(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = accentColor.copy(alpha = 0.1f)
        ),
        shape = RoundedCornerShape(16.dp)
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
                tint = accentColor,
                modifier = Modifier.size(40.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = accentColor
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Status indicator chip.
 */
@Composable
fun StatusChip(
    text: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(
                color = color.copy(alpha = 0.15f),
                shape = RoundedCornerShape(8.dp)
            )
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.SemiBold
        )
    }
}

/**
 * Section header with title.
 */
@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = modifier.padding(vertical = 8.dp)
    )
}

/**
 * Loading overlay.
 */
@Composable
fun LoadingOverlay(
    isLoading: Boolean,
    modifier: Modifier = Modifier
) {
    if (isLoading) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.3f)),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = Color(0xFF6750A4))
        }
    }
}
