package com.mvi.kenny.feature.swiftpmmigration

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mvi.kenny.feature.swiftpmmigration.MigrationColors as Colors
import com.mvi.kenny.feature.swiftpmmigration.MigrationTab as Tab
import kotlinx.coroutines.flow.collectLatest

/**
 * ============================================================
 * SwiftPMMigrationScreen — KMP SwiftPM 迁移助手主屏幕
 * ============================================================
 * PRD-025 | KMP SwiftPM 迁移助手
 *
 * 三栏布局：
 * - 左栏 NavigationRail → Tab 导航（Scanner / Wizard / Progress）
 * - 中栏 Content → 各 Feature 主内容区
 * - 右栏 Detail/Preview → 依赖详情 / Diff 预览（可折叠）
 *
 * 架构：
 * - MVI：ViewModel 处理所有 Intent，State 驱动 UI 重组
 * - Effect：通过 LaunchedEffect 收集副作用（Toast 等）
 *
 * @param onNavigateTo 内部导航回调（本工具为独立 ToolWindow，此处为空实现）
 * @see SwiftPMMigrationViewModel 状态管理
 * @see SwiftPMMigrationContract MVI 契约
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun SwiftPMMigrationScreen(
    onNavigateTo: (String) -> Unit = {},
    viewModel: SwiftPMMigrationViewModel = viewModel(),
) {
    // 收集状态和副作用
    val state by viewModel.state.collectAsStateWithLifecycle()

    // 监听 ScannerEffect
    LaunchedEffect(Unit) {
        viewModel.scannerEffect.collectLatest { effect ->
            when (effect) {
                is ScannerEffect.ShowToast -> { /* Toast handled by parent */ }
                is ScannerEffect.ScanCompleted -> { /* UI updates via state */ }
                is ScannerEffect.NavigateToDependencyDetail -> {
                    viewModel.processIntent(SwiftPMMigrationIntent.SelectDependencyForDetail(effect.dependencyId))
                }
                is ScannerEffect.ShowError -> { /* Error shown in UI */ }
            }
        }
    }

    // 主布局：Row 三栏
    Row(modifier = Modifier.fillMaxSize()) {

        // ============================================================
        // 左栏：NavigationRail — Tab 切换
        // ============================================================
        MigrationNavigationRail(
            activeTab = state.activeTab,
            onTabSelected = { tab ->
                viewModel.processIntent(SwiftPMMigrationIntent.SwitchTab(tab))
            },
            modifier = Modifier.width(80.dp)
        )

        VerticalDivider(
            modifier = Modifier.fillMaxHeight(),
            color = Colors.Border,
            thickness = 1.dp,
        )

        // ============================================================
        // 中栏：Content — 主内容区
        // ============================================================
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
        ) {
            // Deadline Banner（距 <90 天显示）
            if (state.activeTab == Tab.SCANNER && state.scannerState.showDeadlineBanner) {
                DeadlineBanner(
                    daysUntilSunset = state.scannerState.daysUntilSunset,
                    urgencyLevel = state.scannerState.urgencyLevel,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                )
            }

            // 主内容：根据 activeTab 渲染对应 Feature
            when (state.activeTab) {
                Tab.SCANNER -> ScannerContent(
                    state = state.scannerState,
                    onIntent = viewModel::processScannerIntent,
                    onDependencyClick = { depId ->
                        viewModel.processIntent(SwiftPMMigrationIntent.SelectDependencyForDetail(depId))
                    },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = if (state.scannerState.showDeadlineBanner) 56.dp else 0.dp)
                )
                Tab.WIZARD -> WizardContent(
                    state = state.wizardState,
                    allDependencies = state.scannerState.dependencies,
                    onIntent = viewModel::processWizardIntent,
                    onDependencyClick = { depId ->
                        viewModel.processIntent(SwiftPMMigrationIntent.SelectDependencyForDetail(depId))
                    },
                    modifier = Modifier.fillMaxSize()
                )
                Tab.PROGRESS -> ProgressContent(
                    state = state.progressState,
                    onIntent = viewModel::processProgressIntent,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        // ============================================================
        // 右栏：Detail/Preview — 详情预览面板（可折叠）
        // ============================================================
        AnimatedVisibility(
            visible = state.isDetailPanelExpanded && state.selectedDependencyId != null,
            enter = slideInHorizontally(initialOffsetX = { it }) + fadeIn(),
            exit = slideOutHorizontally(targetOffsetX = { it }) + fadeOut(),
        ) {
            state.selectedDependencyId?.let { depId ->
                DetailPanel(
                    dependencyId = depId,
                    viewModel = viewModel,
                    onClose = {
                        viewModel.processIntent(SwiftPMMigrationIntent.SelectDependencyForDetail(null))
                    },
                    modifier = Modifier
                        .width(360.dp)
                        .fillMaxHeight()
                )
            }
        }

        // 右侧展开/折叠按钮（当面板关闭时显示）
        if (!state.isDetailPanelExpanded) {
            Box(
                modifier = Modifier.fillMaxHeight(),
                contentAlignment = Alignment.CenterEnd,
            ) {
                IconButton(
                    onClick = {
                        if (state.selectedDependencyId != null) {
                            viewModel.processIntent(SwiftPMMigrationIntent.ToggleDetailPanel)
                        }
                    }
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "展开详情",
                        tint = Colors.TextSecondary,
                    )
                }
            }
        }
    }
}

// ============================================================
// Left Column: NavigationRail / 左栏导航
// ============================================================

/**
 * MigrationNavigationRail — 左栏 Tab 导航
 *
 * 三个 Tab：依赖扫描 / 转换向导 / 进度追踪
 */
@Composable
private fun MigrationNavigationRail(
    activeTab: Tab,
    onTabSelected: (Tab) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxHeight()
            .background(Colors.Surface)
            .padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        // 标题
        Text(
            text = "SwiftPM",
            style = TextStyle(
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Colors.Urgent,
            ),
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Tab 按钮
        Tab.entries.forEach { tab ->
            val selected = activeTab == tab
            val icon = when (tab) {
                Tab.SCANNER -> Icons.Default.Radar
                Tab.WIZARD -> Icons.Default.AutoFixHigh
                Tab.PROGRESS -> Icons.Default.Timeline
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        if (selected) Colors.Urgent.copy(alpha = 0.15f) else Color.Transparent
                    )
                    .clickable { onTabSelected(tab) }
                    .padding(12.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = tab.title,
                    tint = if (selected) Colors.Urgent else Colors.TextSecondary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = tab.title,
                    style = TextStyle(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        color = if (selected) Colors.Urgent else Colors.TextSecondary,
                    )
                )
            }
        }
    }
}

// ============================================================
// Deadline Banner / 紧迫度 Banner
// ============================================================

/**
 * DeadlineBanner — 顶部 Deadline 紧迫度提示 Banner
 * 距 Sunset Deadline <90 天时显示
 */
@Composable
private fun DeadlineBanner(
    daysUntilSunset: Int,
    urgencyLevel: UrgencyLevel,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = urgencyLevel.color.copy(alpha = 0.15f),
        contentColor = urgencyLevel.color,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Icon(
                Icons.Default.HourglassBottom,
                contentDescription = null,
                tint = urgencyLevel.color,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = "CocoaPods 将于 $daysUntilSunset 天后（2026-12）被弃用",
                style = TextStyle(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = urgencyLevel.color,
                )
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = urgencyLevel.label,
                style = TextStyle(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = urgencyLevel.color,
                ),
                modifier = Modifier
                    .background(urgencyLevel.color.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            )
        }
    }
}

// ============================================================
// Scanner Content / 扫描器内容区
// ============================================================

@Composable
private fun ScannerContent(
    state: ScannerState,
    onIntent: (ScannerIntent) -> Unit,
    onDependencyClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.padding(16.dp)) {

        // 操作栏
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // 扫描按钮
            Button(
                onClick = { onIntent(ScannerIntent.StartScan) },
                enabled = state.scanStatus != ScanStatus.SCANNING,
                colors = ButtonDefaults.buttonColors(containerColor = Colors.Urgent),
            ) {
                Icon(Icons.Default.Radar, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text(
                    text = if (state.scanStatus == ScanStatus.SCANNING) "扫描中..." else "一键扫描",
                    fontFamily = FontFamily.Monospace,
                )
            }

            // 取消按钮
            if (state.scanStatus == ScanStatus.SCANNING) {
                OutlinedButton(onClick = { onIntent(ScannerIntent.CancelScan) }) {
                    Text("取消", fontFamily = FontFamily.Monospace)
                }
            }

            // 进度文本
            if (state.scanStatus == ScanStatus.SCANNING) {
                Text(
                    text = "${state.scannedCount}/${state.totalCount}",
                    style = TextStyle(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 13.sp,
                        color = Colors.TextSecondary,
                    )
                )
                LinearProgressIndicator(
                    progress = { state.scanProgress / 100f },
                    modifier = Modifier
                        .weight(1f)
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = Colors.Urgent,
                )
            }

            Spacer(Modifier.weight(1f))

            // 批量选择
            if (state.scanStatus == ScanStatus.COMPLETED) {
                TextButton(onClick = { onIntent(ScannerIntent.SelectAllMigratable) }) {
                    Text("全选可迁移", fontFamily = FontFamily.Monospace, color = Colors.Success)
                }
                TextButton(onClick = { onIntent(ScannerIntent.DeselectAll) }) {
                    Text("取消全选", fontFamily = FontFamily.Monospace, color = Colors.TextSecondary)
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        // 过滤和排序栏
        if (state.scanStatus == ScanStatus.COMPLETED || state.scanStatus == ScanStatus.SCANNING) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // 过滤模式
                FilterMode.entries.forEach { mode ->
                    FilterChip(
                        selected = state.filterMode == mode,
                        onClick = { onIntent(ScannerIntent.SetFilter(mode)) },
                        label = {
                            Text(mode.label, fontFamily = FontFamily.Monospace, fontSize = 11.sp)
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = when (mode) {
                                FilterMode.ALL -> Colors.Info
                                FilterMode.DIRECT -> Colors.Success
                                FilterMode.NEEDS_ADJUSTMENT -> Colors.Warning
                                FilterMode.UNSUPPORTED -> Colors.Error
                            }.copy(alpha = 0.2f),
                        ),
                    )
                }

                Spacer(Modifier.weight(1f))

                // 排序
                var sortMenuExpanded by remember { mutableStateOf(false) }
                Box {
                    TextButton(onClick = { sortMenuExpanded = true }) {
                        Text("排序: ${state.sortMode.label}", fontFamily = FontFamily.Monospace, fontSize = 11.sp)
                        Icon(Icons.Default.Sort, contentDescription = null, modifier = Modifier.size(14.dp))
                    }
                    DropdownMenu(expanded = sortMenuExpanded, onDismissRequest = { sortMenuExpanded = false }) {
                        SortMode.entries.forEach { mode ->
                            DropdownMenuItem(
                                text = { Text(mode.label, fontFamily = FontFamily.Monospace) },
                                onClick = {
                                    onIntent(ScannerIntent.SetSort(mode))
                                    sortMenuExpanded = false
                                },
                            )
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        // 依赖列表
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (state.dependencies.isEmpty() && state.scanStatus == ScanStatus.IDLE) {
                item {
                    EmptyStateCard(
                        icon = Icons.Default.Radar,
                        title = "尚未扫描",
                        description = "点击「一键扫描」检测项目中的 CocoaPods 依赖",
                    )
                }
            }

            items(
                items = state.filteredDependencies,
                key = { it.id },
            ) { dep ->
                DependencyCard(
                    dependency = dep,
                    onCheckedChange = { selected ->
                        onIntent(ScannerIntent.SelectDependency(dep.id, selected))
                    },
                    onClick = { onDependencyClick(dep.id) },
                )
            }

            // 扫描中占位
            if (state.scanStatus == ScanStatus.SCANNING && state.filteredDependencies.isEmpty()) {
                item {
                    DependencyCardSkeleton()
                }
            }
        }
    }
}

// ============================================================
// Dependency Card / 依赖卡片
// ============================================================

@Composable
private fun DependencyCard(
    dependency: DependencyInfo,
    onCheckedChange: (Boolean) -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val pathColor = dependency.migrationPath.color
    val urgency = when {
        dependency.daysUntilSunset < 30 -> Colors.Urgent
        dependency.daysUntilSunset < 60 -> Colors.Warning
        else -> Colors.TextSecondary
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        color = Colors.Surface,
        border = BorderStroke(1.dp, Colors.Border),
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Checkbox
            Checkbox(
                checked = dependency.isSelected,
                onCheckedChange = onCheckedChange,
                colors = CheckboxDefaults.colors(
                    checkedColor = pathColor,
                    uncheckedColor = Colors.TextSecondary,
                ),
            )

            // 状态指示点
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .background(pathColor, CircleShape)
            )

            // 依赖信息
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = dependency.name,
                    style = TextStyle(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Colors.TextPrimary,
                    )
                )
                Text(
                    text = "v${dependency.version}",
                    style = TextStyle(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        color = Colors.TextSecondary,
                    )
                )
            }

            // 迁移路径标签
            Surface(
                shape = RoundedCornerShape(4.dp),
                color = pathColor.copy(alpha = 0.15f),
            ) {
                Text(
                    text = dependency.migrationPath.label,
                    style = TextStyle(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        color = pathColor,
                    ),
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }

            // Deadline 紧迫度
            if (dependency.daysUntilSunset < 60) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.HourglassBottom,
                        contentDescription = null,
                        tint = urgency,
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        text = "${dependency.daysUntilSunset}天",
                        style = TextStyle(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp,
                            color = urgency,
                        )
                    )
                }
            }

            // 箭头
            Icon(
                Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = "查看详情",
                tint = Colors.TextSecondary,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
private fun DependencyCardSkeleton() {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp),
        shape = RoundedCornerShape(8.dp),
        color = Colors.Surface,
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(18.dp)
                    .background(Colors.Border, RoundedCornerShape(4.dp))
            )
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .background(Colors.Border, CircleShape)
            )
            Column(modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.5f)
                        .height(14.dp)
                        .background(Colors.Border, RoundedCornerShape(2.dp))
                )
                Spacer(Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.25f)
                        .height(10.dp)
                        .background(Colors.Border, RoundedCornerShape(2.dp))
                )
            }
        }
    }
}

@Composable
private fun EmptyStateCard(
    icon: ImageVector,
    title: String,
    description: String,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = Colors.Surface,
    ) {
        Column(
            modifier = Modifier.padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = Colors.TextSecondary,
                modifier = Modifier.size(48.dp)
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = title,
                style = TextStyle(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Colors.TextPrimary,
                )
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = description,
                style = TextStyle(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 13.sp,
                    color = Colors.TextSecondary,
                ),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            )
        }
    }
}

// ============================================================
// Wizard Content / 转换向导内容区
// ============================================================

@Composable
private fun WizardContent(
    state: ConversionWizardState,
    allDependencies: List<DependencyInfo>,
    onIntent: (ConversionWizardIntent) -> Unit,
    onDependencyClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.padding(16.dp)) {

        // 步骤指示器
        WizardStepIndicator(
            currentStep = state.currentStep,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(24.dp))

        // 步骤内容
        Box(modifier = Modifier.weight(1f)) {
            when (state.currentStep) {
                WizardStep.SELECT -> WizardSelectStep(
                    allDependencies = allDependencies,
                    selectedIds = state.selectedDependencies,
                    onIntent = onIntent,
                    onDependencyClick = onDependencyClick,
                )
                WizardStep.CONFIRM -> WizardConfirmStep(
                    state = state,
                    allDependencies = allDependencies,
                    onDependencyClick = onDependencyClick,
                )
                WizardStep.DIFF -> WizardDiffStep(
                    state = state,
                    allDependencies = allDependencies,
                )
                WizardStep.EXECUTE -> WizardExecuteStep(state = state)
            }
        }

        Spacer(Modifier.height(16.dp))

        // 底部导航按钮
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // 上一步
            OutlinedButton(
                onClick = { onIntent(ConversionWizardIntent.PreviousStep) },
                enabled = state.canGoBackward,
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                Spacer(Modifier.width(6.dp))
                Text("上一步", fontFamily = FontFamily.Monospace)
            }

            Spacer(Modifier.weight(1f))

            // 回滚按钮
            if (state.rollbackAvailable) {
                OutlinedButton(
                    onClick = { onIntent(ConversionWizardIntent.RollbackLast) },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Colors.Warning),
                ) {
                    Icon(Icons.Default.Undo, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text("撤销转换", fontFamily = FontFamily.Monospace)
                }
            }

            // 下一步 / 执行
            if (state.currentStep == WizardStep.EXECUTE) {
                Button(
                    onClick = { onIntent(ConversionWizardIntent.ConfirmAndClose) },
                    colors = ButtonDefaults.buttonColors(containerColor = Colors.Success),
                ) {
                    Text("完成", fontFamily = FontFamily.Monospace)
                }
            } else {
                Button(
                    onClick = { onIntent(ConversionWizardIntent.NextStep) },
                    enabled = state.canGoForward,
                    colors = ButtonDefaults.buttonColors(containerColor = Colors.Urgent),
                ) {
                    Text(
                        text = if (state.currentStep == WizardStep.DIFF) "执行转换" else "下一步",
                        fontFamily = FontFamily.Monospace,
                    )
                    Spacer(Modifier.width(6.dp))
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
                }
            }
        }
    }
}

// ============================================================
// Wizard Step Indicator / 向导步骤指示器
// ============================================================

@Composable
private fun WizardStepIndicator(
    currentStep: WizardStep,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        WizardStep.entries.forEachIndexed { index, step ->
            val isActive = step.stepNumber <= currentStep.stepNumber
            val isCurrent = step == currentStep

            // 步骤圆点
            Box(
                modifier = Modifier
                    .size(if (isCurrent) 32.dp else 28.dp)
                    .background(
                        color = when {
                            isCurrent -> Colors.Urgent
                            isActive -> Colors.Success
                            else -> Colors.Border
                        },
                        shape = CircleShape,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "${step.stepNumber}",
                    style = TextStyle(
                        fontFamily = FontFamily.Monospace,
                        fontSize = if (isCurrent) 14.sp else 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isActive) Color.White else Colors.TextSecondary,
                    )
                )
            }

            // 连接线
            if (index < WizardStep.entries.size - 1) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(2.dp)
                        .background(
                            if (step.stepNumber < currentStep.stepNumber) Colors.Success else Colors.Border,
                            RoundedCornerShape(1.dp),
                        )
                )
            }
        }
    }

    // 步骤标签
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        WizardStep.entries.forEach { step ->
            Text(
                text = step.label,
                style = TextStyle(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp,
                    color = if (step == currentStep) Colors.Urgent else Colors.TextSecondary,
                )
            )
        }
    }
}

// ============================================================
// Wizard Select Step / 选择依赖步骤
// ============================================================

@Composable
private fun WizardSelectStep(
    allDependencies: List<DependencyInfo>,
    selectedIds: Set<String>,
    onIntent: (ConversionWizardIntent) -> Unit,
    onDependencyClick: (String) -> Unit,
) {
    if (allDependencies.isEmpty()) {
        EmptyStateCard(
            icon = Icons.Default.PlaylistAdd,
            title = "无可用依赖",
            description = "请先在「依赖扫描」页面完成扫描",
        )
    } else {
        Column {
            Text(
                text = "选择要迁移的依赖（${selectedIds.size} 个已选）",
                style = TextStyle(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 14.sp,
                    color = Colors.TextSecondary,
                ),
                modifier = Modifier.padding(bottom = 12.dp)
            )
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(allDependencies, key = { it.id }) { dep ->
                    val isSelected = selectedIds.contains(dep.id)
                    val isMigratable = dep.migrationPath != MigrationPath.UNSUPPORTED

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(enabled = isMigratable) { onDependencyClick(dep.id) },
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSelected) dep.migrationPath.color.copy(alpha = 0.1f) else Colors.Surface,
                        border = BorderStroke(
                            1.dp,
                            if (isSelected) dep.migrationPath.color else Colors.Border,
                        ),
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            Checkbox(
                                checked = isSelected,
                                onCheckedChange = { checked ->
                                    val newSet = if (checked) selectedIds + dep.id else selectedIds - dep.id
                                    onIntent(ConversionWizardIntent.SelectDependencies(newSet))
                                },
                                enabled = isMigratable,
                            )
                            Text(
                                text = dep.name,
                                style = TextStyle(
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 13.sp,
                                    color = if (isMigratable) Colors.TextPrimary else Colors.TextSecondary,
                                ),
                                modifier = Modifier.weight(1f),
                            )
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = dep.migrationPath.color.copy(alpha = 0.15f),
                            ) {
                                Text(
                                    text = dep.migrationPath.label,
                                    style = TextStyle(
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 10.sp,
                                        color = dep.migrationPath.color,
                                    ),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ============================================================
// Wizard Confirm Step / 确认方案步骤
// ============================================================

@Composable
private fun WizardConfirmStep(
    state: ConversionWizardState,
    allDependencies: List<DependencyInfo>,
    onDependencyClick: (String) -> Unit,
) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        items(state.conversionPlans.entries.toList(), key = { it.key }) { (depId, plan) ->
            val dep = allDependencies.find { it.id == depId }
            ConversionPlanCard(
                dependency = dep,
                plan = plan,
                onClick = { onDependencyClick(depId) },
            )
        }
    }
}

@Composable
private fun ConversionPlanCard(
    dependency: DependencyInfo?,
    plan: ConversionPlan,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        color = Colors.Surface,
        border = BorderStroke(1.dp, Colors.Border),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = dependency?.name ?: plan.dependencyId,
                    style = TextStyle(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Colors.TextPrimary,
                    ),
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text = "v${dependency?.version ?: "?"}",
                    style = TextStyle(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        color = Colors.TextSecondary,
                    )
                )
            }
            Spacer(Modifier.height(8.dp))
            Text(
                text = plan.recommendedAction,
                style = TextStyle(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    color = Colors.TextSecondary,
                )
            )
            if (plan.warnings.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                plan.warnings.forEach { warning ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Warning,
                            contentDescription = null,
                            tint = Colors.Warning,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = warning,
                            style = TextStyle(
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp,
                                color = Colors.Warning,
                            )
                        )
                    }
                }
            }
        }
    }
}

// ============================================================
// Wizard Diff Step / Diff 预览步骤
// ============================================================

@Composable
private fun WizardDiffStep(
    state: ConversionWizardState,
    allDependencies: List<DependencyInfo>,
) {
    var selectedDepId by remember { mutableStateOf(state.selectedDependencies.firstOrNull()) }

    Row(modifier = Modifier.fillMaxSize()) {
        // 左侧：依赖列表
        LazyColumn(
            modifier = Modifier
                .width(200.dp)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            items(state.diffResults.keys.toList(), key = { it }) { depId ->
                val dep = allDependencies.find { it.id == depId }
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { selectedDepId = depId },
                    shape = RoundedCornerShape(6.dp),
                    color = if (selectedDepId == depId) Colors.Info.copy(alpha = 0.15f) else Colors.Surface,
                    border = BorderStroke(
                        1.dp,
                        if (selectedDepId == depId) Colors.Info else Colors.Border,
                    ),
                ) {
                    Text(
                        text = dep?.name ?: depId,
                        style = TextStyle(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                            color = if (selectedDepId == depId) Colors.Info else Colors.TextPrimary,
                        ),
                        modifier = Modifier.padding(10.dp)
                    )
                }
            }
        }

        Spacer(Modifier.width(16.dp))

        // 右侧：Diff 视图
        selectedDepId?.let { depId ->
            val diff = state.diffResults[depId]
            val dep = allDependencies.find { it.id == depId }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${dep?.name ?: depId} — Diff 预览",
                    style = TextStyle(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Colors.TextPrimary,
                    ),
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF1A1A1A),
                ) {
                    diff?.diffLines?.forEach { line ->
                        Text(
                            text = line.content,
                            style = TextStyle(
                                fontFamily = FontFamily.Monospace,
                                fontSize = 12.sp,
                                color = when (line.type) {
                                    DiffLineType.ADDED -> Colors.Success
                                    DiffLineType.REMOVED -> Colors.Error
                                    DiffLineType.CONTEXT -> Colors.TextSecondary
                                },
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 2.dp)
                        )
                    } ?: Text(
                        text = "无 Diff 数据",
                        style = TextStyle(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                            color = Colors.TextSecondary,
                        ),
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
        }
    }
}

// ============================================================
// Wizard Execute Step / 执行转换步骤
// ============================================================

@Composable
private fun WizardExecuteStep(state: ConversionWizardState) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        if (state.isExecuting) {
            // 执行中
            CircularProgressIndicator(
                progress = { state.executionPercent },
                modifier = Modifier.size(80.dp),
                strokeWidth = 8.dp,
                color = Colors.Urgent,
            )
            Spacer(Modifier.height(24.dp))
            Text(
                text = "正在转换 ${state.executionProgress}/${state.executionTotal} 个依赖",
                style = TextStyle(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 16.sp,
                    color = Colors.TextPrimary,
                )
            )
            Spacer(Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { state.executionPercent },
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = Colors.Urgent,
            )
        } else if (state.executionProgress > 0) {
            // 执行完成
            Icon(
                Icons.Default.CheckCircle,
                contentDescription = null,
                tint = Colors.Success,
                modifier = Modifier.size(80.dp)
            )
            Spacer(Modifier.height(24.dp))
            Text(
                text = "转换完成！",
                style = TextStyle(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Colors.Success,
                )
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "已成功转换 ${state.executedCount} 个依赖",
                style = TextStyle(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 14.sp,
                    color = Colors.TextSecondary,
                )
            )
        } else {
            // 待执行
            Icon(
                Icons.Default.Pending,
                contentDescription = null,
                tint = Colors.TextSecondary,
                modifier = Modifier.size(80.dp)
            )
            Spacer(Modifier.height(24.dp))
            Text(
                text = "准备就绪",
                style = TextStyle(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 16.sp,
                    color = Colors.TextPrimary,
                )
            )
            Text(
                text = "点击「执行转换」开始迁移",
                style = TextStyle(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    color = Colors.TextSecondary,
                )
            )
        }
    }
}

// ============================================================
// Progress Content / 进度追踪内容区
// ============================================================

@Composable
private fun ProgressContent(
    state: ProgressTrackerState,
    onIntent: (ProgressTrackerIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    LaunchedEffect(Unit) {
        onIntent(ProgressTrackerIntent.RefreshProgress)
    }

    Column(modifier = modifier.padding(16.dp)) {

        // 总进度卡片
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Colors.Surface),
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "整体迁移进度",
                    style = TextStyle(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 14.sp,
                        color = Colors.TextSecondary,
                    )
                )
                Spacer(Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${(state.overallProgress * 100).toInt()}%",
                        style = TextStyle(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 36.sp,
                            fontWeight = FontWeight.Bold,
                            color = when {
                                state.overallProgress >= 1f -> Colors.Success
                                state.overallProgress >= 0.6f -> Colors.Info
                                else -> Colors.Warning
                            },
                        )
                    )
                    Spacer(Modifier.width(24.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        LinearProgressIndicator(
                            progress = { state.overallProgress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(12.dp)
                                .clip(RoundedCornerShape(6.dp)),
                            color = Colors.Success,
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = "${state.totalMigrated} / ${state.totalDependencies} 个依赖已迁移",
                            style = TextStyle(
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp,
                                color = Colors.TextSecondary,
                            )
                        )
                    }
                }
                Spacer(Modifier.height(12.dp))
                Row {
                    Text(
                        text = "距 Deadline：${state.daysUntilSunset} 天",
                        style = TextStyle(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                            color = if (state.daysUntilSunset < 30) Colors.Urgent else Colors.TextSecondary,
                        )
                    )
                    Spacer(Modifier.weight(1f))
                    Text(
                        text = "剩余 ${state.remainingDependencies} 个",
                        style = TextStyle(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                            color = Colors.TextSecondary,
                        )
                    )
                }
            }
        }

        Spacer(Modifier.height(20.dp))

        // 导出报告按钮
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ReportFormat.entries.forEach { format ->
                OutlinedButton(
                    onClick = { onIntent(ProgressTrackerIntent.ExportReport(format)) },
                ) {
                    Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("导出 ${format.label}", fontFamily = FontFamily.Monospace, fontSize = 12.sp)
                }
            }
            if (state.exportedReportPath != null) {
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "✓ 已导出",
                    style = TextStyle(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        color = Colors.Success,
                    ),
                    modifier = Modifier.align(Alignment.CenterVertically)
                )
            }
        }

        Spacer(Modifier.height(20.dp))

        // 模块进度列表
        Text(
            text = "模块进度",
            style = TextStyle(
                fontFamily = FontFamily.Monospace,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Colors.TextPrimary,
            )
        )
        Spacer(Modifier.height(12.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(state.modules, key = { it.moduleName }) { module ->
                ModuleProgressCard(
                    module = module,
                    isSelected = state.selectedModuleName == module.moduleName,
                    onClick = { onIntent(ProgressTrackerIntent.SelectModule(module.moduleName)) },
                )
            }
        }
    }
}

// ============================================================
// Module Progress Card / 模块进度卡片
// ============================================================

@Composable
private fun ModuleProgressCard(
    module: ModuleProgress,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val progressColor = when {
        module.progress >= 1f -> Colors.Success
        module.progress >= 0.5f -> Colors.Info
        module.progress >= 0.25f -> Colors.Warning
        else -> Colors.Error
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        color = if (isSelected) progressColor.copy(alpha = 0.08f) else Colors.Surface,
        border = BorderStroke(
            1.dp,
            if (isSelected) progressColor else Colors.Border,
        ),
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = module.moduleName,
                    style = TextStyle(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Colors.TextPrimary,
                    ),
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text = "${module.migratedCount}/${module.totalDependencies}",
                    style = TextStyle(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        color = Colors.TextSecondary,
                    )
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "${(module.progress * 100).toInt()}%",
                    style = TextStyle(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = progressColor,
                    )
                )
            }
            Spacer(Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { module.progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = progressColor,
            )
            if (module.blockedCount > 0) {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "${module.blockedCount} 个依赖被阻塞",
                    style = TextStyle(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        color = Colors.Error,
                    )
                )
            }
        }
    }
}

// ============================================================
// Detail Panel / 详情面板
// ============================================================

@Composable
private fun DetailPanel(
    dependencyId: String,
    viewModel: SwiftPMMigrationViewModel,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val dependency = viewModel.getDependencyById(dependencyId)
    val conversionPlan = viewModel.getConversionPlanById(dependencyId)
    val diffResult = viewModel.getDiffResultById(dependencyId)

    Surface(
        modifier = modifier,
        color = Colors.Surface,
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = dependency?.name ?: dependencyId,
                    style = TextStyle(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Colors.TextPrimary,
                    ),
                    modifier = Modifier.weight(1f),
                )
                IconButton(onClick = onClose) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "关闭",
                        tint = Colors.TextSecondary,
                    )
                }
            }

            if (dependency != null) {
                Spacer(Modifier.height(16.dp))

                // 基本信息
                DetailSection(title = "基本信息") {
                    DetailRow("版本", dependency.version)
                    DetailRow("迁移路径", dependency.migrationPath.label, dependency.migrationPath.color)
                    DetailRow("距 Deadline", "${dependency.daysUntilSunset} 天")
                    DetailRow("复杂度", "${dependency.complexity}/5")
                    DetailRow("预估耗时", "${dependency.estimatedMinutes} 分钟")
                }

                // SwiftPM 替代
                if (dependency.swiftpmProduct != null) {
                    Spacer(Modifier.height(12.dp))
                    DetailSection(title = "SwiftPM 替代") {
                        DetailRow("Product", dependency.swiftpmProduct)
                    }
                }

                // 替代库
                if (dependency.alternativeLibrary != null) {
                    Spacer(Modifier.height(12.dp))
                    DetailSection(title = "替代方案") {
                        DetailRow("替代库", dependency.alternativeLibrary, Colors.Warning)
                    }
                }

                // 转换方案
                if (conversionPlan != null) {
                    Spacer(Modifier.height(12.dp))
                    DetailSection(title = "推荐方案") {
                        Text(
                            text = conversionPlan.recommendedAction,
                            style = TextStyle(
                                fontFamily = FontFamily.Monospace,
                                fontSize = 12.sp,
                                color = Colors.TextPrimary,
                            )
                        )
                        if (conversionPlan.warnings.isNotEmpty()) {
                            Spacer(Modifier.height(8.dp))
                            conversionPlan.warnings.forEach { warning ->
                                Row {
                                    Icon(
                                        Icons.Default.Warning,
                                        contentDescription = null,
                                        tint = Colors.Warning,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(Modifier.width(4.dp))
                                    Text(
                                        text = warning,
                                        style = TextStyle(
                                            fontFamily = FontFamily.Monospace,
                                            fontSize = 11.sp,
                                            color = Colors.Warning,
                                        )
                                    )
                                }
                            }
                        }
                    }
                }

                // Diff 预览
                if (diffResult != null) {
                    Spacer(Modifier.height(12.dp))
                    DetailSection(title = "Diff 预览") {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 200.dp),
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFF1A1A1A),
                        ) {
                            diffResult.diffLines.forEach { line ->
                                Text(
                                    text = line.content,
                                    style = TextStyle(
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 11.sp,
                                        color = when (line.type) {
                                            DiffLineType.ADDED -> Colors.Success
                                            DiffLineType.REMOVED -> Colors.Error
                                            DiffLineType.CONTEXT -> Colors.TextSecondary
                                        },
                                    ),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 1.dp)
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
private fun DetailSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column {
        Text(
            text = title,
            style = TextStyle(
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Colors.TextSecondary,
            ),
            modifier = Modifier.padding(bottom = 6.dp)
        )
        Surface(
            shape = RoundedCornerShape(6.dp),
            color = Colors.Background,
        ) {
            Column(modifier = Modifier.padding(10.dp)) {
                content()
            }
        }
    }
}

@Composable
private fun DetailRow(
    label: String,
    value: String,
    valueColor: Color = Colors.TextPrimary,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = label,
            style = TextStyle(
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp,
                color = Colors.TextSecondary,
            )
        )
        Text(
            text = value,
            style = TextStyle(
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp,
                color = valueColor,
            )
        )
    }
}
