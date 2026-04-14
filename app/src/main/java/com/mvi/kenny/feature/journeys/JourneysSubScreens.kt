package com.mvi.kenny.feature.journeys

// ================================================================
// JourneysSubScreens — Journeys E2E 测试工具包子页面
// ================================================================
// PRD-099: Journeys for Android Studio 自动化 E2E 测试工具包
// Design Reference: memory/agency/designs/PRD-099-Journeys-E2E测试工具包.md
//
// Contains all sub-screens:
//   - JourneyEditorScreen: XML editor with step tree + NL generation
//   - TemplateLibraryScreen: Template gallery with categories
//   - JourneyResultScreen: Execution results with step timeline
//   - CIConfigurationScreen: CI/CD YAML generation
//   - FrameworkComparisonScreen: Journey vs Espresso vs UIAutomator matrix
// ================================================================

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Input
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Login
import androidx.compose.material.icons.filled.NavigateBefore
import androidx.compose.material.icons.filled.NavigateNext
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Badge
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField

import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mvi.kenny.base.TopBarConfig
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

// ================================================================
// Color Constants / 颜色常量
// ================================================================
private val JourneyPrimary = Color(0xFF6750A4)
private val JourneyBackground = Color(0xFFFAFAFA)
private val JourneySurface = Color(0xFFFFFFFF)
private val JourneySuccess = Color(0xFF4CAF50)
private val JourneyWarning = Color(0xFFFF9800)
private val JourneyError = Color(0xFFF44336)
private val JourneyRunning = Color(0xFF2196F3)
private val JourneyTextPrimary = Color(0xFF1F1F1F)
private val JourneyTextSecondary = Color(0xFF5F5F5F)
private val DiffAddBg = Color(0xFFDFFBE6)
private val DiffRemoveBg = Color(0xFFFFDAD6)

// ================================================================
// JourneyEditorScreen — Journey 编辑器
// ================================================================

/**
 * JourneyEditorScreen — Journey XML 编辑器主页面
 *
 * Layout: Left Step Tree + Right Editor + Bottom Preview + NL Generation Sheet
 *
 * @param viewModel JourneysViewModel instance
 * @param onNavigateBack Callback to navigate back to dashboard
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JourneyEditorScreen(
    viewModel: JourneysViewModel = viewModel(),
    onNavigateBack: () -> Unit = {}
) {
    val state by viewModel.state.collectAsState()
    val scrollState = rememberScrollState()
    var showNLSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // NL Generation Bottom Sheet / 自然语言生成底部弹窗
    if (showNLSheet) {
        ModalBottomSheet(
            onDismissRequest = { showNLSheet = false },
            sheetState = sheetState,
            dragHandle = { BottomSheetDefaults.DragHandle() },
            containerColor = JourneySurface
        ) {
            NLToJourneySheet(
                nlInput = state.nlInputText,
                onNLInputChange = { viewModel.sendIntent(JourneysIntent.UpdateNLInput(it)) },
                isGenerating = state.isGeneratingNL,
                generatedResult = state.nlGenerationResult,
                onGenerate = { viewModel.sendIntent(JourneysIntent.GenerateFromNL) },
                onInsert = {
                    viewModel.sendIntent(JourneysIntent.InsertGeneratedXML)
                    showNLSheet = false
                },
                onDismiss = { showNLSheet = false }
            )
        }
    }

    Surface(modifier = Modifier.fillMaxSize(), color = JourneyBackground) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Editor TopBar / 编辑器顶栏
            TopAppBar(
                title = {
                    Text(
                        text = state.currentJourney?.name ?: "Journey 编辑器",
                        fontFamily = FontFamily.Monospace
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                actions = {
                    // Mode indicator / 模式指示
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = when (state.editorMode) {
                            EditorMode.View -> JourneySuccess.copy(alpha = 0.1f)
                            EditorMode.Edit -> JourneyWarning.copy(alpha = 0.1f)
                            EditorMode.Create -> JourneyPrimary.copy(alpha = 0.1f)
                        }
                    ) {
                        Text(
                            text = when (state.editorMode) {
                                EditorMode.View -> "查看"
                                EditorMode.Edit -> "编辑"
                                EditorMode.Create -> "新建"
                            },
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = when (state.editorMode) {
                                EditorMode.View -> JourneySuccess
                                EditorMode.Edit -> JourneyWarning
                                EditorMode.Create -> JourneyPrimary
                            }
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    if (state.editorMode != EditorMode.View) {
                        IconButton(onClick = {
                            state.activeJourneyId?.let { id ->
                                viewModel.sendIntent(JourneysIntent.SaveJourney(id, state.currentJourneyXml))
                            }
                        }) {
                            Icon(Icons.Default.Save, "Save")
                        }
                    }
                    IconButton(onClick = { showNLSheet = true }) {
                        Icon(Icons.Default.AutoAwesome, "AI Generate")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = JourneySurface)
            )

            // Main editor area / 主编辑区
            if (state.currentJourney == null && state.activeJourneyId == null) {
                // Empty state / 空状态
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.Description,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = JourneyTextSecondary.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "选择或创建一个 Journey",
                            style = MaterialTheme.typography.bodyLarge,
                            color = JourneyTextSecondary
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { viewModel.sendIntent(JourneysIntent.CreateBlankJourney) },
                            colors = ButtonDefaults.buttonColors(containerColor = JourneyPrimary)
                        ) {
                            Icon(Icons.Default.Add, null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("新建 Journey")
                        }
                    }
                }
            } else {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Tab: XML Editor / XML 编辑器
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        // Left: Step Tree Panel / 左侧：步骤树面板
                        Surface(
                            modifier = Modifier
                                .width(240.dp)
                                .fillMaxHeight(),
                            color = JourneySurface,
                            tonalElevation = 1.dp
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "步骤结构 / Steps",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = JourneyTextSecondary,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                                LazyColumn(
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    itemsIndexed(state.currentSteps, key = { _, step -> step.id }) { index, step ->
                                        StepTreeItem(
                                            step = step,
                                            isSelected = state.selectedStepId == step.id,
                                            onSelect = { viewModel.sendIntent(JourneysIntent.SelectStep(step.id)) },
                                            onDelete = { viewModel.sendIntent(JourneysIntent.DeleteStep(step.id)) },
                                            onMoveUp = if (index > 0) {
                                                { viewModel.sendIntent(JourneysIntent.ReorderSteps(index, index - 1)) }
                                            } else null,
                                            onMoveDown = if (index < state.currentSteps.size - 1) {
                                                { viewModel.sendIntent(JourneysIntent.ReorderSteps(index, index + 1)) }
                                            } else null
                                        )
                                    }
                                    item {
                                        // Add step button / 添加步骤按钮
                                        OutlinedButton(
                                            onClick = {
                                                val newStep = JourneyStep(
                                                    id = java.util.UUID.randomUUID().toString(),
                                                    order = state.currentSteps.size + 1,
                                                    description = "New Step / 新步骤",
                                                    actionType = "click"
                                                )
                                                viewModel.sendIntent(
                                                    JourneysIntent.AddStep(
                                                        state.currentSteps.lastOrNull()?.id,
                                                        newStep
                                                    )
                                                )
                                            },
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("添加步骤")
                                        }
                                    }
                                }
                            }
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .width(1.dp)
                                .background(JourneyTextSecondary.copy(alpha = 0.2f))
                        )

                        // Right: XML Editor Panel / 右侧：XML 编辑面板
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                        ) {
                            // Editor toolbar / 编辑器工具栏
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(JourneySurface)
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "XML 内容 / XML Content",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = JourneyTextSecondary
                                )
                                Spacer(modifier = Modifier.weight(1f))
                                if (state.editorMode == EditorMode.View) {
                                    TextButton(
                                        onClick = {
                                            state.activeJourneyId?.let { id ->
                                                viewModel.sendIntent(JourneysIntent.SaveJourney(id, state.currentJourneyXml))
                                            }
                                        }
                                    ) {
                                        Icon(Icons.Default.Edit, null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("编辑")
                                    }
                                }
                            }

                            // XML content / XML 内容
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth()
                                    .background(Color(0xFFF5F5F5))
                                    .padding(12.dp)
                                    .verticalScroll(scrollState)
                            ) {
                                Text(
                                    text = state.currentJourneyXml.ifEmpty { "<!-- Empty Journey / 空 Journey -->" },
                                    style = MaterialTheme.typography.bodySmall,
                                    fontFamily = FontFamily.Monospace,
                                    color = JourneyTextPrimary,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ================================================================
// StepTreeItem — 步骤树中的单个步骤项
// ================================================================

@Composable
private fun StepTreeItem(
    step: JourneyStep,
    isSelected: Boolean,
    onSelect: () -> Unit,
    onDelete: () -> Unit,
    onMoveUp: (() -> Unit)?,
    onMoveDown: (() -> Unit)?
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) JourneyPrimary.copy(alpha = 0.1f) else Color.Transparent,
        label = "bg"
    )
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) JourneyPrimary else Color.Transparent,
        label = "border"
    )

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() }
            .border(1.dp, borderColor, RoundedCornerShape(6.dp)),
        color = backgroundColor,
        shape = RoundedCornerShape(6.dp)
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Step icon / 步骤图标
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(
                        when (step.status) {
                            StepStatus.Passed -> JourneySuccess
                            StepStatus.Failed -> JourneyError
                            StepStatus.Running -> JourneyRunning
                            StepStatus.Skipped -> JourneyTextSecondary
                            StepStatus.Pending -> JourneyPrimary.copy(alpha = 0.5f)
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${step.order}",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = step.actionType,
                    style = MaterialTheme.typography.labelSmall,
                    color = JourneyPrimary,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = step.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = JourneyTextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Reorder buttons / 排序按钮
            Column {
                IconButton(
                    onClick = { onMoveUp?.invoke() },
                    enabled = onMoveUp != null,
                    modifier = Modifier.size(20.dp)
                ) {
                    Icon(Icons.Default.KeyboardArrowUp, "Move up", modifier = Modifier.size(14.dp))
                }
                IconButton(
                    onClick = { onMoveDown?.invoke() },
                    enabled = onMoveDown != null,
                    modifier = Modifier.size(20.dp)
                ) {
                    Icon(Icons.Default.KeyboardArrowDown, "Move down", modifier = Modifier.size(14.dp))
                }
            }

            IconButton(onClick = onDelete, modifier = Modifier.size(20.dp)) {
                Icon(Icons.Default.Delete, "Delete", modifier = Modifier.size(14.dp), tint = JourneyError)
            }
        }
    }
}

// ================================================================
// NLToJourneySheet — 自然语言生成面板 (BottomSheet)
// ================================================================

@Composable
private fun NLToJourneySheet(
    nlInput: String,
    onNLInputChange: (String) -> Unit,
    isGenerating: Boolean,
    generatedResult: String?,
    onGenerate: () -> Unit,
    onInsert: () -> Unit,
    onDismiss: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Text(
            text = "自然语言 → Journey XML",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "描述测试步骤，AI 自动生成 Journey XML",
            style = MaterialTheme.typography.bodySmall,
            color = JourneyTextSecondary
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = nlInput,
            onValueChange = onNLInputChange,
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp),
            placeholder = { Text("例如：首先打开 App，然后在搜索框输入 'iPhone'，点击搜索按钮...") },
            shape = RoundedCornerShape(8.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = onGenerate,
            enabled = nlInput.isNotEmpty() && !isGenerating,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = JourneyPrimary),
            shape = RoundedCornerShape(8.dp)
        ) {
            if (isGenerating) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("生成中...")
            } else {
                Icon(Icons.Default.AutoAwesome, null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("✨ 生成 Journey XML")
            }
        }

        // Generated result / 生成结果
        AnimatedVisibility(
            visible = generatedResult != null,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Column {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "生成结果 / Generated Result:",
                    style = MaterialTheme.typography.labelMedium,
                    color = JourneyTextSecondary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color(0xFFF5F5F5),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = generatedResult ?: "",
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(12.dp),
                        fontSize = 12.sp
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onGenerate,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Refresh, null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("重新生成")
                    }
                    Button(
                        onClick = onInsert,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = JourneySuccess),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Check, null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("确认插入")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

// ================================================================
// TemplateLibraryScreen — 模板库
// ================================================================

/**
 * TemplateLibraryScreen — Journey 模板库页面
 *
 * @param viewModel JourneysViewModel instance
 */
@Composable
fun TemplateLibraryScreen(
    viewModel: JourneysViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()

    Surface(modifier = Modifier.fillMaxSize(), color = JourneyBackground) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header / 标题
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Journey 模板库",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "开箱即用的最小可运行测试模板",
                    style = MaterialTheme.typography.bodySmall,
                    color = JourneyTextSecondary
                )
            }

            // Search / 搜索
            OutlinedTextField(
                value = state.templateSearchQuery,
                onValueChange = { viewModel.sendIntent(JourneysIntent.SearchTemplates(it)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                placeholder = { Text("搜索模板... / Search templates...") },
                leadingIcon = { Icon(Icons.Default.Search, null) },
                shape = RoundedCornerShape(8.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Category filter chips / 分类筛选
            LazyRow(
                contentPadding = PaddingValues(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(
                        selected = state.selectedCategory == null,
                        onClick = { viewModel.sendIntent(JourneysIntent.FilterTemplatesByCategory(null)) },
                        label = { Text("全部") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = JourneyPrimary.copy(alpha = 0.15f),
                            selectedLabelColor = JourneyPrimary
                        )
                    )
                }
                items(TemplateCategory.entries) { category ->
                    FilterChip(
                        selected = state.selectedCategory == category,
                        onClick = { viewModel.sendIntent(JourneysIntent.FilterTemplatesByCategory(category)) },
                        label = { Text(category.displayName) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = JourneyPrimary.copy(alpha = 0.15f),
                            selectedLabelColor = JourneyPrimary
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Template grid / 模板网格
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(state.filteredTemplates, key = { it.id }) { template ->
                    TemplateCard(
                        template = template,
                        onUse = { viewModel.sendIntent(JourneysIntent.CreateJourneyFromTemplate(template.id)) },
                        onPreview = { /* Preview logic */ }
                    )
                }
                item { Spacer(modifier = Modifier.height(20.dp)) }
            }
        }
    }
}

@Composable
private fun TemplateCard(
    template: JourneyTemplate,
    onUse: () -> Unit,
    onPreview: () -> Unit
) {
    val icon = when (template.category) {
        TemplateCategory.LoginFlow -> Icons.Default.Login
        TemplateCategory.PaymentFlow -> Icons.Default.Payment
        TemplateCategory.ListOperation -> Icons.Default.ShoppingCart
        TemplateCategory.FormFilling -> Icons.Default.EditNote
        TemplateCategory.DeepLink -> Icons.Default.Link
        TemplateCategory.Navigation -> Icons.Default.NavigateNext
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = JourneySurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon / 图标
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(JourneyPrimary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = JourneyPrimary, modifier = Modifier.size(28.dp))
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Info / 信息
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = template.name,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium
                    )
                    if (template.isBuiltIn) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = JourneyPrimary.copy(alpha = 0.1f)
                        ) {
                            Text(
                                text = "内置",
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = JourneyPrimary
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = template.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = JourneyTextSecondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${template.stepCount} steps · ${template.category.displayName}",
                    style = MaterialTheme.typography.labelSmall,
                    color = JourneyTextSecondary
                )
            }

            // Actions / 操作
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = onUse,
                    colors = ButtonDefaults.buttonColors(containerColor = JourneyPrimary),
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text("使用", fontSize = 12.sp)
                }
                OutlinedButton(
                    onClick = onPreview,
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text("预览", fontSize = 12.sp)
                }
            }
        }
    }
}

// ================================================================
// JourneyResultScreen — 执行结果面板
// ================================================================

/**
 * JourneyResultScreen — Journey 执行结果展示页面
 *
 * @param viewModel JourneysViewModel instance
 */
@Composable
fun JourneyResultScreen(
    viewModel: JourneysViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()
    val result = state.executionResult

    Surface(modifier = Modifier.fillMaxSize(), color = JourneyBackground) {
        if (result == null) {
            // Empty state / 空状态
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.BugReport,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = JourneyTextSecondary.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "还没有执行结果",
                        style = MaterialTheme.typography.bodyLarge,
                        color = JourneyTextSecondary
                    )
                    Text(
                        text = "运行一个 Journey 来查看结果",
                        style = MaterialTheme.typography.bodySmall,
                        color = JourneyTextSecondary.copy(alpha = 0.7f)
                    )
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Overall status header / 整体状态头
                item {
                    ResultHeader(result = result)
                }

                // Summary cards / 汇总卡片
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        ResultSummaryCard(
                            modifier = Modifier.weight(1f),
                            label = "通过",
                            value = result.totalPassed.toString(),
                            color = JourneySuccess
                        )
                        ResultSummaryCard(
                            modifier = Modifier.weight(1f),
                            label = "失败",
                            value = result.totalFailed.toString(),
                            color = JourneyError
                        )
                        ResultSummaryCard(
                            modifier = Modifier.weight(1f),
                            label = "跳过",
                            value = result.totalSkipped.toString(),
                            color = JourneyTextSecondary
                        )
                        ResultSummaryCard(
                            modifier = Modifier.weight(1f),
                            label = "耗时",
                            value = "${result.totalDurationMs / 1000}s",
                            color = JourneyPrimary
                        )
                    }
                }

                // Step timeline / 步骤时间线
                item {
                    Text(
                        text = "步骤详情 / Step Details",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                items(result.steps, key = { it.id }) { step ->
                    StepTimelineItem(
                        step = step,
                        isExpanded = state.expandedStepId == step.id,
                        onToggle = { viewModel.sendIntent(JourneysIntent.ToggleStepDetail(step.id)) },
                        onRerun = if (step.status == StepStatus.Failed) {
                            { result.journeyId.let { id -> viewModel.sendIntent(JourneysIntent.RerunFailedStep(id, step.id)) } }
                        } else null
                    )
                }

                item { Spacer(modifier = Modifier.height(20.dp)) }
            }
        }
    }
}

@Composable
private fun ResultHeader(result: ExecutionResult) {
    val statusColor = when (result.overallStatus) {
        JourneyStatus.Passed -> JourneySuccess
        JourneyStatus.Failed -> JourneyError
        JourneyStatus.Running -> JourneyRunning
        else -> JourneyTextSecondary
    }
    val statusIcon = when (result.overallStatus) {
        JourneyStatus.Passed -> Icons.Default.CheckCircle
        JourneyStatus.Failed -> Icons.Default.Error
        JourneyStatus.Running -> Icons.Default.Refresh
        else -> Icons.Default.Description
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = JourneySurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(statusColor.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = statusIcon, contentDescription = null, tint = statusColor, modifier = Modifier.size(28.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = when (result.overallStatus) {
                        JourneyStatus.Passed -> "全部通过 / All Passed"
                        JourneyStatus.Failed -> "执行失败 / Execution Failed"
                        JourneyStatus.Running -> "运行中 / Running..."
                        else -> "未知状态"
                    },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = statusColor
                )
                Text(
                    text = "${result.startTime} → ${result.endTime}",
                    style = MaterialTheme.typography.bodySmall,
                    color = JourneyTextSecondary
                )
            }
        }
    }
}

@Composable
private fun ResultSummaryCard(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    color: Color
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = JourneySurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = JourneyTextSecondary
            )
        }
    }
}

@Composable
private fun StepTimelineItem(
    step: JourneyStep,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    onRerun: (() -> Unit)?
) {
    val statusColor = when (step.status) {
        StepStatus.Passed -> JourneySuccess
        StepStatus.Failed -> JourneyError
        StepStatus.Running -> JourneyRunning
        StepStatus.Skipped -> JourneyTextSecondary
        StepStatus.Pending -> JourneyPrimary.copy(alpha = 0.5f)
    }
    val statusIcon = when (step.status) {
        StepStatus.Passed -> Icons.Default.CheckCircle
        StepStatus.Failed -> Icons.Default.Error
        StepStatus.Running -> Icons.Default.Refresh // Placeholder; UI handled below
        StepStatus.Skipped -> Icons.Default.NavigateNext
        StepStatus.Pending -> Icons.Default.BugReport
    }
    val isRunningStep = step.status == StepStatus.Running

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle() },
        colors = CardDefaults.cardColors(containerColor = JourneySurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Status indicator / 状态指示
                if (isRunningStep) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = statusColor, strokeWidth = 2.dp)
                } else {
                    Icon(imageVector = statusIcon, contentDescription = null, tint = statusColor, modifier = Modifier.size(20.dp))
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Step ${step.order}: ${step.description}",
                    style = MaterialTheme.typography.bodySmall,
                    color = JourneyTextPrimary
                )
            }

            // Expanded content / 展开详情
            if (isExpanded) {
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = JourneyTextSecondary.copy(alpha = 0.1f))
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Column {
                        Text("Action / 动作", style = MaterialTheme.typography.labelSmall, color = JourneyTextSecondary)
                        Text(step.actionType, style = MaterialTheme.typography.bodySmall, fontFamily = FontFamily.Monospace, color = JourneyPrimary)
                    }
                    if (step.target.isNotEmpty()) {
                        Column {
                            Text("Target / 目标", style = MaterialTheme.typography.labelSmall, color = JourneyTextSecondary)
                            Text(step.target, style = MaterialTheme.typography.bodySmall, fontFamily = FontFamily.Monospace)
                        }
                    }
                    if (step.durationMs > 0) {
                        Column {
                            Text("Duration / 耗时", style = MaterialTheme.typography.labelSmall, color = JourneyTextSecondary)
                            Text("${'$'}{step.durationMs / 1000.0}s", style = MaterialTheme.typography.bodySmall, color = JourneyTextPrimary)
                        }
                    }
                }

                if (step.status == StepStatus.Failed && step.errorMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = JourneyError.copy(alpha = 0.05f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = step.errorMessage,
                            modifier = Modifier.padding(8.dp),
                            style = MaterialTheme.typography.bodySmall,
                            color = JourneyError
                        )
                    }
                }

                if (step.screenshotPath != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp),
                        color = JourneyTextSecondary.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.BugReport, null, tint = JourneyTextSecondary.copy(alpha = 0.5f), modifier = Modifier.size(32.dp))
                        }
                    }
                }

                if (onRerun != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = onRerun,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Icon(Icons.Default.Replay, null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("重跑此步 / Rerun")
                    }
                }
            }
        }
    }
}

// ================================================================
// CIConfigurationScreen — CI/CD 配置页面
// ================================================================

/**
 * CIConfigurationScreen — CI/CD YAML 配置生成页面
 *
 * @param viewModel JourneysViewModel instance
 */
@Composable
fun CIConfigurationScreen(
    viewModel: JourneysViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()

    Surface(modifier = Modifier.fillMaxSize(), color = JourneyBackground) {
        LazyColumn(
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header / 标题
            item {
                Text(
                    text = "CI/CD 集成配置",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "生成适用于主流 CI 平台的 Journey 测试配置",
                    style = MaterialTheme.typography.bodySmall,
                    color = JourneyTextSecondary
                )
            }

            // Platform selector / 平台选择
            item {
                Text(
                    text = "CI 平台 / CI Platform",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(8.dp))
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    CIPlatform.entries.forEachIndexed { index, platform ->
                        SegmentedButton(
                            selected = state.selectedCIPlatform == platform,
                            onClick = { viewModel.sendIntent(JourneysIntent.SelectCIPlatform(platform)) },
                            shape = SegmentedButtonDefaults.itemShape(index = index, count = CIPlatform.entries.size),
                            colors = SegmentedButtonDefaults.colors(
                                activeContainerColor = JourneyPrimary.copy(alpha = 0.15f),
                                activeContentColor = JourneyPrimary
                            )
                        ) {
                            Text(platform.displayName, fontSize = 12.sp)
                        }
                    }
                }
            }

            // YAML preview / YAML 预览
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)), // Dark background for code
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${state.selectedCIPlatform.displayName} YAML",
                                style = MaterialTheme.typography.labelMedium,
                                color = Color(0xFF9CDCFE)
                            )
                            Row {
                                IconButton(
                                    onClick = { viewModel.sendIntent(JourneysIntent.CopyCIConfig) }
                                ) {
                                    Icon(
                                        Icons.Default.ContentCopy,
                                        "Copy",
                                        tint = Color(0xFF9CDCFE),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                IconButton(onClick = { viewModel.sendIntent(JourneysIntent.DownloadCIConfig) }) {
                                    Icon(
                                        Icons.Default.FileDownload,
                                        "Download",
                                        tint = Color(0xFF9CDCFE),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = state.generatedYAML.ifEmpty { "# Select a platform to generate YAML / 选择平台生成 YAML" },
                            style = MaterialTheme.typography.bodySmall,
                            fontFamily = FontFamily.Monospace,
                            color = Color(0xFFDCDCAA),
                            fontSize = 12.sp
                        )
                    }
                }
            }

            // Usage instructions / 使用说明
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = JourneySurface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "使用说明 / Usage",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "1. 复制上述 YAML 配置到你的 CI 配置文件\n" +
                                "2. 确保 Journey XML 文件位于 app/src/test/journeys/ 目录\n" +
                                "3. 运行 ./gradlew runJourneys 执行所有测试\n" +
                                "4. 查看 app/build/journeys/results/ 中的执行结果",
                            style = MaterialTheme.typography.bodySmall,
                            color = JourneyTextSecondary
                        )
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(20.dp)) }
        }
    }
}

// ================================================================
// FrameworkComparisonScreen — 框架对比页面
// ================================================================

/**
 * FrameworkComparisonScreen — Journey vs Espresso vs UIAutomator 能力对比页面
 *
 * @param viewModel JourneysViewModel instance
 */
@Composable
fun FrameworkComparisonScreen(
    viewModel: JourneysViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()

    Surface(modifier = Modifier.fillMaxSize(), color = JourneyBackground) {
        LazyColumn(
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header / 标题
            item {
                Text(
                    text = "Journey vs Espresso vs UIAutomator",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "选择最适合你的测试框架",
                    style = MaterialTheme.typography.bodySmall,
                    color = JourneyTextSecondary
                )
            }

            // Capability matrix / 能力矩阵
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = JourneySurface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "能力矩阵 / Capability Matrix",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        // Table header / 表头
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = "能力 / Capability",
                                modifier = Modifier.weight(2f),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = JourneyTextSecondary
                            )
                            Text(
                                text = "Journey",
                                modifier = Modifier.weight(1f),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = JourneyPrimary,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = "Espresso",
                                modifier = Modifier.weight(1f),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = JourneyTextSecondary,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = "UIAutom.",
                                modifier = Modifier.weight(1f),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = JourneyTextSecondary,
                                textAlign = TextAlign.Center
                            )
                        }

                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 8.dp),
                            color = JourneyTextSecondary.copy(alpha = 0.1f)
                        )

                        // Capability rows / 能力行
                        val capabilities = listOf(
                            Triple("AI-driven step understanding", "✅", "❌"),
                            Triple("Visual feedback verification", "✅", "⚠️"),
                            Triple("No element locators needed", "✅", "❌"),
                            Triple("Cross-app automation", "✅", "❌"),
                            Triple("CI/CD friendly", "✅", "✅"),
                            Triple("Scripting language", "XML", "Kotlin"),
                            Triple("Learning curve", "Low", "High"),
                            Triple("Android Studio integrated", "✅", "✅"),
                        )

                        capabilities.forEach { (capability, journey, espresso) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = capability,
                                    modifier = Modifier.weight(2f),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = JourneyTextPrimary
                                )
                                Text(
                                    text = journey,
                                    modifier = Modifier.weight(1f),
                                    textAlign = TextAlign.Center,
                                    style = MaterialTheme.typography.bodySmall
                                )
                                Text(
                                    text = espresso,
                                    modifier = Modifier.weight(1f),
                                    textAlign = TextAlign.Center,
                                    style = MaterialTheme.typography.bodySmall
                                )
                                Text(
                                    text = "✅",
                                    modifier = Modifier.weight(1f),
                                    textAlign = TextAlign.Center,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }
            }

            // Fusion suggestions / 融合建议
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = JourneySurface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "融合建议 / Fusion Suggestions",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        val suggestions = listOf(
                            Triple("Simple UI interactions", "Espresso", "Fast and stable / 快速稳定"),
                            Triple("Complex flows / visual verification", "Journey", "AI-assisted / AI 辅助"),
                            Triple("Cross-app scenarios", "UIAutomator", "System-level access / 系统级访问"),
                        )

                        suggestions.forEach { (scenario, framework, reason) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(JourneyPrimary)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = scenario,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = reason,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = JourneyTextSecondary
                                    )
                                }
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = JourneyPrimary.copy(alpha = 0.1f)
                                ) {
                                    Text(
                                        text = framework,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = JourneyPrimary,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        HorizontalDivider(color = JourneyTextSecondary.copy(alpha = 0.1f))
                        Spacer(modifier = Modifier.height(8.dp))

                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = JourneyPrimary.copy(alpha = 0.05f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.AutoAwesome,
                                    null,
                                    tint = JourneyPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "Best Practice / 最佳实践：Journey handles main flows, Espresso handles detailed assertions",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = JourneyTextPrimary
                                )
                            }
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(20.dp)) }
        }
    }
}
