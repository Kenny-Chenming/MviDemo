package com.mvi.kenny.feature.journeys

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

// =============================================================
// JourneysEditorScreen — Journey 编辑器屏幕
// PRD-099 | Journeys for Android Studio 自动化 E2E 测试工具包
// =============================================================
/**
 * Journey XML Editor Screen / Journey XML 编辑器屏幕
 *
 * Provides:
 * - Step tree view (left panel)
 * - XML editor (right panel)
 * - Natural language generation bottom sheet
 * - Step flow preview (bottom)
 *
 * @param viewModel JourneysViewModel instance / JourneysViewModel 实例
 * @param onNavigateBack Callback to navigate back / 返回回调
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JourneysEditorScreen(
    viewModel: JourneysViewModel,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    var showNLSheet by remember { mutableStateOf(false) }
    var showAddStepDialog by remember { mutableStateOf(false) }
    var selectedStepId by remember { mutableStateOf<String?>(null) }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        when (state.editorMode) {
                            EditorMode.VIEW -> "View Journey / 浏览 Journey"
                            EditorMode.EDIT -> "Edit Journey / 编辑 Journey"
                            EditorMode.CREATE -> "Create Journey / 新建 Journey"
                        },
                        fontWeight = FontWeight.Medium
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back / 返回",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF6750A4),
                    titleContentColor = Color.White
                ),
                actions = {
                    // Save button / 保存按钮
                    IconButton(
                        onClick = {
                            state.activeJourneyId?.let { id ->
                                viewModel.processIntent(
                                    JourneysIntent.SaveJourney(id, state.currentJourneyXml)
                                )
                            }
                        }
                    ) {
                        Icon(
                            Icons.Default.Save,
                            contentDescription = "Save / 保存",
                            tint = Color.White
                        )
                    }
                    // Run button / 运行按钮
                    IconButton(
                        onClick = {
                            state.activeJourneyId?.let { id ->
                                viewModel.processIntent(JourneysIntent.RunJourney(id))
                            }
                        },
                        enabled = !state.isRunning
                    ) {
                        if (state.isRunning) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                Icons.Default.PlayArrow,
                                contentDescription = "Run / 运行",
                                tint = Color.White
                            )
                        }
                    }
                    // NL generation / 自然语言生成
                    IconButton(onClick = { showNLSheet = true }) {
                        Icon(
                            Icons.Default.AutoAwesome,
                            contentDescription = "NL Generation / 自然语言生成",
                            tint = Color.White
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFFAFAFA))
        ) {
            // Main editor area / 主编辑区
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                // Step tree view (left panel) / 步骤树视图（左侧面板）
                StepTreePanel(
                    steps = state.activeJourneySteps,
                    selectedStepId = selectedStepId,
                    onStepSelect = { selectedStepId = it },
                    onStepDelete = { stepId ->
                        viewModel.processIntent(JourneysIntent.DeleteStep(stepId))
                    },
                    onAddStep = { showAddStepDialog = true },
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(0.4f)
                )

                // XML editor (right panel) / XML 编辑器（右侧面板）
                XmlEditorPanel(
                    xml = state.currentJourneyXml,
                    onXmlChange = { xml ->
                        viewModel.processIntent(JourneysIntent.UpdateJourneyXml(xml))
                    },
                    editorMode = state.editorMode,
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(0.6f)
                )
            }

            // Step flow preview (bottom) / 步骤流预览（底部）
            StepFlowPreview(
                steps = state.activeJourneySteps,
                onStepClick = { stepId -> selectedStepId = stepId }
            )
        }

        // NL Generation Bottom Sheet / 自然语言生成底部弹窗
        if (showNLSheet) {
            ModalBottomSheet(
                onDismissRequest = { showNLSheet = false },
                sheetState = sheetState
            ) {
                NLToJourneySheet(
                    isGenerating = state.isGeneratingNL,
                    generationResult = state.nlGenerationResult,
                    onGenerate = { description ->
                        viewModel.processIntent(JourneysIntent.GenerateFromNL(description))
                    },
                    onInsert = { xml ->
                        viewModel.processIntent(JourneysIntent.InsertGeneratedJourney(xml))
                        scope.launch {
                            sheetState.hide()
                            showNLSheet = false
                        }
                    },
                    onDismiss = {
                        scope.launch {
                            sheetState.hide()
                            showNLSheet = false
                        }
                    }
                )
            }
        }

        // Add Step Dialog / 添加步骤对话框
        if (showAddStepDialog) {
            AddStepDialog(
                onDismiss = { showAddStepDialog = false },
                onAddStep = { step ->
                    viewModel.processIntent(
                        JourneysIntent.AddStep(selectedStepId, step)
                    )
                    showAddStepDialog = false
                }
            )
        }
    }
}

// ================================================================
// StepTreePanel — 步骤树形面板
// ================================================================
/**
 * Step tree panel (left side) / 步骤树形面板（左侧）
 */
@Composable
private fun StepTreePanel(
    steps: List<JourneyStep>,
    selectedStepId: String?,
    onStepSelect: (String) -> Unit,
    onStepDelete: (String) -> Unit,
    onAddStep: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.padding(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header / 头部
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF6750A4).copy(alpha = 0.1f))
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Steps / 步骤",
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp,
                    color = Color(0xFF6750A4)
                )
                IconButton(
                    onClick = onAddStep,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "Add step / 添加步骤",
                        tint = Color(0xFF6750A4),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Step list / 步骤列表
            if (steps.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No steps yet\nAdd steps to start",
                        fontSize = 14.sp,
                        color = Color(0xFF5F5F5F)
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    itemsIndexed(steps, key = { _, step -> step.id }) { index, step ->
                        StepTreeItem(
                            step = step,
                            index = index + 1,
                            isSelected = step.id == selectedStepId,
                            onSelect = { onStepSelect(step.id) },
                            onDelete = { onStepDelete(step.id) }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Step tree item / 步骤树形项
 */
@Composable
private fun StepTreeItem(
    step: JourneyStep,
    index: Int,
    isSelected: Boolean,
    onSelect: () -> Unit,
    onDelete: () -> Unit
) {
    val statusColor = when (step.status) {
        StepStatus.PASSED -> Color(0xFF4CAF50)
        StepStatus.FAILED -> Color(0xFFF44336)
        StepStatus.RUNNING -> Color(0xFF2196F3)
        StepStatus.PENDING -> Color(0xFF9E9E9E)
        StepStatus.SKIPPED -> Color(0xFFFF9800)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onSelect)
            .then(
                if (isSelected) Modifier.border(
                    2.dp,
                    Color(0xFF6750A4),
                    RoundedCornerShape(8.dp)
                ) else Modifier
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected)
                Color(0xFF6750A4).copy(alpha = 0.05f)
            else Color(0xFFF5F5F5)
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Status indicator / 状态指示器
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(statusColor)
            )
            Spacer(modifier = Modifier.width(8.dp))

            // Step info / 步骤信息
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Step $index: ${step.actionType}",
                    fontWeight = FontWeight.Medium,
                    fontSize = 12.sp,
                    color = Color(0xFF1F1F1F)
                )
                Text(
                    text = step.description,
                    fontSize = 11.sp,
                    color = Color(0xFF5F5F5F),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Delete button / 删除按钮
            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Delete / 删除",
                    tint = Color(0xFFF44336),
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

// ================================================================
// XmlEditorPanel — XML 编辑器面板
// ================================================================
/**
 * XML Editor panel (right side) / XML 编辑器面板（右侧）
 */
@Composable
private fun XmlEditorPanel(
    xml: String,
    onXmlChange: (String) -> Unit,
    editorMode: EditorMode,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.padding(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header / 头部
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF2D2D2D))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "journey.xml",
                    fontSize = 12.sp,
                    color = Color(0xFF9E9E9E),
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = when (editorMode) {
                        EditorMode.VIEW -> "View / 浏览"
                        EditorMode.EDIT -> "Edit / 编辑"
                        EditorMode.CREATE -> "Create / 新建"
                    },
                    fontSize = 10.sp,
                    color = Color(0xFF6750A4)
                )
            }

            // XML content / XML 内容
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp)
            ) {
                if (editorMode == EditorMode.VIEW) {
                    Text(
                        text = xml,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        color = Color(0xFF4EC9B0)
                    )
                } else {
                    BasicTextField(
                        value = xml,
                        onValueChange = onXmlChange,
                        modifier = Modifier.fillMaxSize(),
                        textStyle = androidx.compose.ui.text.TextStyle(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                            color = Color(0xFF4EC9B0)
                        ),
                        cursorBrush = SolidColor(Color(0xFF569CD6))
                    )
                }
            }
        }
    }
}

// ================================================================
// StepFlowPreview — 步骤流预览
// ================================================================
/**
 * Step flow preview (bottom) / 步骤流预览（底部）
 */
@Composable
private fun StepFlowPreview(
    steps: List<JourneyStep>,
    onStepClick: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .padding(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
        ) {
            Text(
                text = "Step Flow Preview / 步骤流预览",
                fontWeight = FontWeight.Medium,
                fontSize = 12.sp,
                color = Color(0xFF6750A4)
            )
            Spacer(modifier = Modifier.height(8.dp))
            if (steps.isEmpty()) {
                Text(
                    text = "No steps / 无步骤",
                    fontSize = 12.sp,
                    color = Color(0xFF5F5F5F)
                )
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    steps.take(6).forEachIndexed { index, step ->
                        StepFlowItem(
                            step = step,
                            index = index + 1,
                            onClick = { onStepClick(step.id) }
                        )
                        if (index < steps.size - 1 && index < 5) {
                            Text(
                                text = "→",
                                fontSize = 16.sp,
                                color = Color(0xFF6750A4)
                            )
                        }
                    }
                    if (steps.size > 6) {
                        Text(
                            text = "+${steps.size - 6}",
                            fontSize = 12.sp,
                            color = Color(0xFF5F5F5F)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Step flow item / 步骤流项
 */
@Composable
private fun StepFlowItem(
    step: JourneyStep,
    index: Int,
    onClick: () -> Unit
) {
    val bgColor = when (step.status) {
        StepStatus.PASSED -> Color(0xFF4CAF50)
        StepStatus.FAILED -> Color(0xFFF44336)
        StepStatus.RUNNING -> Color(0xFF2196F3)
        StepStatus.PENDING -> Color(0xFF9E9E9E)
        StepStatus.SKIPPED -> Color(0xFFFF9800)
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor.copy(alpha = 0.15f))
            .border(1.dp, bgColor, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = step.status.emoji,
                fontSize = 14.sp
            )
            Text(
                text = "Step$index",
                fontSize = 10.sp,
                color = bgColor,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

// ================================================================
// NLToJourneySheet — 自然语言生成底部弹窗
// ================================================================
/**
 * Natural language to Journey XML bottom sheet
 * / 自然语言生成 Journey XML 底部弹窗
 */
@Composable
private fun NLToJourneySheet(
    isGenerating: Boolean,
    generationResult: String?,
    onGenerate: (String) -> Unit,
    onInsert: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var nlDescription by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp)
    ) {
        Text(
            text = "✨ Natural Language → Journey XML",
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = Color(0xFF1F1F1F)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Describe your test flow in plain English / 用自然语言描述你的测试流程",
            fontSize = 14.sp,
            color = Color(0xFF5F5F5F)
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Input field / 输入框
        OutlinedTextField(
            value = nlDescription,
            onValueChange = { nlDescription = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                Text("e.g., Open the app, then tap the search box, enter \"iPhone\", click search, and verify results appear")
            },
            minLines = 3,
            maxLines = 5,
            shape = RoundedCornerShape(8.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Generate button / 生成按钮
        Button(
            onClick = { onGenerate(nlDescription) },
            enabled = nlDescription.isNotBlank() && !isGenerating,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6750A4)),
            modifier = Modifier.fillMaxWidth()
        ) {
            if (isGenerating) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Generating... / 生成中...")
            } else {
                Icon(Icons.Default.AutoAwesome, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("✨ Generate Journey XML / 生成 Journey XML")
            }
        }

        // Generated result / 生成结果
        if (generationResult != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "Generated XML / 生成的 XML:",
                        fontSize = 12.sp,
                        color = Color(0xFF9E9E9E)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = generationResult,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        color = Color(0xFF4EC9B0)
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { /* Copy to clipboard */ },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF5F5F5F)
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Copy / 复制")
                }
                Button(
                    onClick = { onInsert(generationResult) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Insert / 插入")
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}

// ================================================================
// AddStepDialog — 添加步骤对话框
// ================================================================
/**
 * Add step dialog / 添加步骤对话框
 */
@Composable
private fun AddStepDialog(
    onDismiss: () -> Unit,
    onAddStep: (JourneyStep) -> Unit
) {
    var actionType by remember { mutableStateOf("click") }
    var description by remember { mutableStateOf("") }
    var target by remember { mutableStateOf("") }
    var value by remember { mutableStateOf("") }
    var showDropdown by remember { mutableStateOf(false) }

    val actionTypes = listOf("launch", "click", "input", "swipe", "assert", "wait", "deeplink")

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f))
            .clickable(onClick = onDismiss),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .clickable { }, // Prevent click through
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    text = "Add New Step / 添加新步骤",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color(0xFF1F1F1F)
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Action type dropdown / 动作类型下拉框
                Text("Action Type / 动作类型", fontSize = 12.sp, color = Color(0xFF5F5F5F))
                Spacer(modifier = Modifier.height(4.dp))
                Box {
                    FilterChip(
                        selected = true,
                        onClick = { showDropdown = true },
                        label = { Text(actionType) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFF6750A4),
                            selectedLabelColor = Color.White
                        )
                    )
                    DropdownMenu(
                        expanded = showDropdown,
                        onDismissRequest = { showDropdown = false }
                    ) {
                        actionTypes.forEach { type ->
                            DropdownMenuItem(
                                text = { Text(type) },
                                onClick = {
                                    actionType = type
                                    showDropdown = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description / 描述") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = target,
                    onValueChange = { target = it },
                    label = { Text("Target / 目标") },
                    placeholder = { Text("#element_id or com.example.app") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                )

                if (actionType == "input") {
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = value,
                        onValueChange = { value = it },
                        label = { Text("Value / 输入值") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF5F5F5F)
                        )
                    ) {
                        Text("Cancel / 取消")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            onAddStep(
                                JourneyStep(
                                    id = java.util.UUID.randomUUID().toString().take(8),
                                    actionType = actionType,
                                    description = description.ifBlank { actionType },
                                    target = target,
                                    value = value
                                )
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6750A4))
                    ) {
                        Text("Add / 添加")
                    }
                }
            }
        }
    }
}
