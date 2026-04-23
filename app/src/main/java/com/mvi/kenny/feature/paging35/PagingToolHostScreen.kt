package com.mvi.kenny.feature.paging35

import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel

/**
 * ============================================================
 * PagingToolHostScreen — Paging 3.5 工具包主入口
 * ============================================================
 * PRD-093 | Paging 3.5 `asState` 操作符开发工具包
 *
 * 功能：
 * - 充当整个工具包的导航主机
 * - Dashboard 作为首页
 * - 根据 currentTool 路由到对应子工具
 *
 * @param modifier 修饰符
 * @param onNavigateToTool 导航到指定工具回调
 *
 * @author 开心果 🥜
 */
@Composable
fun PagingToolHostScreen(
    modifier: Modifier = Modifier,
    onNavigateToTool: ((PagingTool) -> Unit)? = null
) {
    // 当前工具路由状态
    // null = Dashboard / 具体工具 = 子工具屏幕
    var currentTool by remember { mutableStateOf<PagingTool?>(null) }

    // Navigate back handler / 返回处理
    val handleNavigateBack: () -> Unit = {
        currentTool = null
    }

    // Navigate to tool handler / 导航到工具处理
    val handleNavigateToTool: (PagingTool) -> Unit = { tool ->
        currentTool = tool
        // 同时通知外部（如 MainScreen）
        onNavigateToTool?.invoke(tool)
    }

    // Route to appropriate screen / 根据当前工具路由到对应屏幕
    when (currentTool) {
        null -> {
            // Dashboard / 仪表盘
            val viewModel: PagingDashboardViewModel = viewModel()
            PagingDashboardScreen(
                viewModel = viewModel,
                onNavigateTo = handleNavigateToTool
            )
        }

        PagingTool.AS_STATE_TEMPLATE -> {
            val viewModel: AsStateTemplateViewModel = viewModel()
            AsStateTemplateScreen(
                viewModel = viewModel,
                onNavigateBack = handleNavigateBack
            )
        }

        PagingTool.MIGRATION_REPORT -> {
            val viewModel: MigrationReportViewModel = viewModel()
            MigrationReportScreen(
                viewModel = viewModel,
                onNavigateBack = handleNavigateBack
            )
        }

        PagingTool.PAGING_DEBUG_PANEL -> {
            val viewModel: PagingDebugViewModel = viewModel()
            PagingDebugScreen(
                viewModel = viewModel,
                onNavigateBack = handleNavigateBack
            )
        }

        PagingTool.TROUBLESHOOTING -> {
            val viewModel: PagingTroubleshootingViewModel = viewModel()
            PagingTroubleshootingScreen(
                viewModel = viewModel,
                onNavigateBack = handleNavigateBack
            )
        }
    }
}
