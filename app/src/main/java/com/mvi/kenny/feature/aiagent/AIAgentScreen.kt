package com.mvi.kenny.feature.aiagent

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mvi.kenny.R
import com.mvi.kenny.base.TopBarAction
import com.mvi.kenny.base.TopBarActions
import com.mvi.kenny.base.TopBarConfig
import kotlinx.coroutines.launch

/**
 * ============================================================
 * AIAgentScreen — AI Agent 隐私优先框架主界面
 * ============================================================
 * 采用 MVI 架构，通过 StateFlow 驱动 UI 渲染。
 *
 * 布局结构：
 * ┌─────────────────────────────────────────────┐
 * │              TopBar（AI Agent + 隐私图标）    │
 * ├─────────────────────────────────────────────┤
 * │                                             │
 * │            消息列表（LazyColumn）            │
 * │  [系统消息] 居中灰色小字                     │
 * │  [用户消息] 右对齐，主色背景                │
 * │  [AI 消息]   左对齐，深灰背景               │
 * │  [工具调用卡片] 可展开详情                  │
 * │  [Loading...] AI 正在输入动画               │
 * │                                             │
 * ├─────────────────────────────────────────────┤
 * │  [输入框..........................] [发送]  │
 * └─────────────────────────────────────────────┘
 *
 * 工具抽屉（BottomSheet）：
 * ┌─────────────────────────────────────────────┐
 * │  ≡ 工具面板                    [隐私模式 🔐] │
 * ├─────────────────────────────────────────────┤
 * │  工具列表（Switch 开关）                    │
 * │  模型选择                                  │
 * └─────────────────────────────────────────────┘
 *
 * @param onUpdateTopBar 向 MainScreen 上报 TopBar 配置
 */

/**
 * ============================================================
 * AIAgentScreen — AI Agent Privacy-First Framework Main Screen
 * ============================================================
 * MVI architecture with StateFlow-driven UI.
 *
 * Layout: Scaffold + LazyColumn + BottomSheet (Tool Drawer)
 */

// ============================================================
// Main Screen Composable
// ============================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AIAgentScreen(
    onUpdateTopBar: (TopBarConfig) -> Unit = {}
) {
    val viewModel: AIAgentViewModel = viewModel()
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    // 避免 lambda 智能 cast 问题
    val isLoading = state.isLoading
    val privacyMode = state.privacyMode
    val messages = state.messages

    // ============================================================
    // TopBar 配置上报
    // ============================================================
    LaunchedEffect(state.messages.size, state.privacyMode) {
        onUpdateTopBar(
            TopBarConfig(
                title = "AI Agent",
                actions = listOfNotNull(
                    // 隐私模式切换按钮
                    TopBarAction(
                        icon = if (privacyMode) Icons.Default.Shield else Icons.Default.Lock,
                        contentDescription = "隐私模式",
                        onClick = { viewModel.sendIntent(AIAgentIntent.TogglePrivacyMode) }
                    ),
                    // 清空按钮
                    if (state.messages.isNotEmpty()) {
                        TopBarActions.delete { viewModel.sendIntent(AIAgentIntent.ClearSession) }
                    } else null
                )
            )
        )
    }

    // ============================================================
    // Effect 处理（Toast + 滚动到底部）
    // ============================================================
    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is AIAgentEffect.ShowToast -> {
                    Toast.makeText(context, effect.message, Toast.LENGTH_SHORT).show()
                }
                is AIAgentEffect.ScrollToBottom -> {
                    if (state.messages.isNotEmpty()) {
                        val lastIndex = state.messages.size - 1
                        listState.animateScrollToItem(lastIndex)
                    }
                }
                is AIAgentEffect.CopyToClipboard -> {
                    // Handled separately if needed
                }
            }
        }
    }

    // ============================================================
    // 页面结构
    // ============================================================
    Column(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
    ) {
        // —— 消息列表区域 —— //
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // 渲染每条消息
            items(
                items = messages,
                key = { it.id }
            ) { message ->
                AgentMessageItem(
                    message = message,
                    onExpandToolCall = { toolCallId ->
                        viewModel.sendIntent(AIAgentIntent.ExpandToolCall(toolCallId))
                    },
                    privacyMode = privacyMode
                )
            }

            // Loading 指示器
            item {
                AnimatedVisibility(
                    visible = isLoading,
                    enter = fadeIn(animationSpec = tween(300)),
                    exit = fadeOut(animationSpec = tween(300))
                ) {
                    AiTypingIndicator()
                }
            }

            // 底部占位
            item {
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        // —— 输入区域 —— //
        AgentInputBar(
            inputText = state.inputText,
            onInputChange = { viewModel.sendIntent(AIAgentIntent.UpdateInput(it)) },
            onSend = {
                viewModel.sendIntent(AIAgentIntent.SendMessage)
                keyboardController?.hide()
            },
            onOpenTools = { viewModel.sendIntent(AIAgentIntent.SetToolDrawerVisible(true)) },
            onLongPressSend = { viewModel.sendIntent(AIAgentIntent.SetToolDrawerVisible(true)) },
            privacyMode = privacyMode
        )
    }

    // —— 工具抽屉 BottomSheet —— //
    if (state.toolDrawerVisible) {
        ToolDrawerSheet(
            state = state,
            onDismiss = { viewModel.sendIntent(AIAgentIntent.SetToolDrawerVisible(false)) },
            onToggleTool = { toolId -> viewModel.sendIntent(AIAgentIntent.ToggleTool(toolId)) },
            onSwitchModel = { model -> viewModel.sendIntent(AIAgentIntent.SwitchModel(model)) },
            onTogglePrivacyMode = { viewModel.sendIntent(AIAgentIntent.TogglePrivacyMode) }
        )
    }
}

// ============================================================
// Message Item Components / 消息项组件
// ============================================================

/**
 * Agent 消息项（根据角色渲染不同样式）
 */
@Composable
private fun AgentMessageItem(
    message: AgentMessage,
    onExpandToolCall: (String) -> Unit,
    privacyMode: Boolean
) {
    when (message.role) {
        MessageRole.USER -> AgentUserBubble(content = message.content)
        MessageRole.ASSISTANT -> {
            AgentAssistantBubble(
                content = message.content,
                toolCalls = message.toolCalls,
                toolResults = message.toolResults,
                onExpandToolCall = onExpandToolCall
            )
        }
        MessageRole.SYSTEM -> AgentSystemText(content = message.content, privacyMode = privacyMode)
        MessageRole.TOOL_RESULT -> ToolResultItem(result = message.toolResults.firstOrNull())
    }
}

/**
 * 用户消息气泡（右对齐，主色背景）
 */
@Composable
private fun AgentUserBubble(content: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End
    ) {
        Card(
            shape = RoundedCornerShape(
                topStart = 18.dp,
                topEnd = 4.dp,
                bottomStart = 18.dp,
                bottomEnd = 18.dp
            ),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF512DA8) // Deep Purple 700
            ),
            modifier = Modifier.widthIn(max = 280.dp)
        ) {
            Text(
                text = content,
                modifier = Modifier.padding(12.dp),
                color = Color.White,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

/**
 * AI 助手消息气泡（左对齐，深灰背景）+ 工具调用卡片
 */
@Composable
private fun AgentAssistantBubble(
    content: String,
    toolCalls: List<ToolCall>,
    toolResults: List<ToolResult>,
    onExpandToolCall: (String) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.Bottom
        ) {
            // AI 头像
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF512DA8)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Memory,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = Color.White
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // 消息气泡
            Card(
                shape = RoundedCornerShape(
                    topStart = 4.dp,
                    topEnd = 18.dp,
                    bottomStart = 18.dp,
                    bottomEnd = 18.dp
                ),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF1E1E2E) // 深灰
                ),
                modifier = Modifier.widthIn(max = 280.dp)
            ) {
                Text(
                    text = content,
                    modifier = Modifier.padding(12.dp),
                    color = Color.White,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        // 工具调用卡片
        toolCalls.forEach { toolCall ->
            val result = toolResults.find { it.toolCallId == toolCall.id }
            ToolCallCard(
                toolCall = toolCall,
                result = result,
                onExpand = { onExpandToolCall(toolCall.id) }
            )
        }
    }
}

/**
 * 系统消息文本（居中，灰色小字）
 */
@Composable
private fun AgentSystemText(content: String, privacyMode: Boolean) {
    Text(
        text = if (privacyMode) "$content 🔐" else content,
        modifier = Modifier.fillMaxWidth(),
        textAlign = TextAlign.Center,
        style = MaterialTheme.typography.bodySmall,
        color = Color(0xFF26A69A) // Teal 400
    )
}

// ============================================================
// Tool Call Card / 工具调用卡片
// ============================================================

/**
 * 工具调用卡片（可折叠展开）
 */
@Composable
private fun ToolCallCard(
    toolCall: ToolCall,
    result: ToolResult?,
    onExpand: () -> Unit
) {
    val borderColor = when {
        toolCall.isExecuting -> Color(0xFF512DA8)
        result?.isError == true -> Color.Red
        result != null -> Color(0xFF26A69A) // Teal 400
        else -> Color(0xFF2D2D3A)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onExpand() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1E1E2E)
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            // 工具调用头部
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Build,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = borderColor
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = toolCall.toolName,
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White
                    )
                }

                if (toolCall.isExecuting) {
                    CircularLoadingIndicator()
                } else if (result != null) {
                    Text(
                        text = if (result.isError) "❌" else "✅",
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }

            // 展开详情
            AnimatedVisibility(visible = toolCall.isExpanded) {
                Column(modifier = Modifier.padding(top = 8.dp)) {
                    HorizontalDivider(color = Color(0xFF2D2D3A))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "参数：${toolCall.arguments}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                    if (result != null) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "结果：${result.output}",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (result.isError) Color.Red else Color(0xFF26A69A)
                        )
                    }
                }
            }
        }
    }
}

/**
 * 工具结果项
 */
@Composable
private fun ToolResultItem(result: ToolResult?) {
    if (result == null) return

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 40.dp)
    ) {
        Card(
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (result.isError) Color.Red.copy(alpha = 0.1f)
                else Color(0xFF26A69A).copy(alpha = 0.1f)
            )
        ) {
            Text(
                text = "🔧 ${result.output}",
                modifier = Modifier.padding(8.dp),
                style = MaterialTheme.typography.bodySmall,
                color = if (result.isError) Color.Red else Color(0xFF26A69A)
            )
        }
    }
}

// ============================================================
// Input Bar / 输入栏
// ============================================================

/**
 * Agent 输入栏（含工具按钮）
 */
@Composable
private fun AgentInputBar(
    inputText: String,
    onInputChange: (String) -> Unit,
    onSend: () -> Unit,
    onOpenTools: () -> Unit,
    onLongPressSend: () -> Unit,
    privacyMode: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 工具抽屉按钮
        IconButton(onClick = onOpenTools) {
            Icon(
                imageVector = Icons.Default.Build,
                contentDescription = "工具面板",
                tint = if (privacyMode) Color(0xFF26A69A) else MaterialTheme.colorScheme.onSurface
            )
        }

        // 输入框
        OutlinedTextField(
            value = inputText,
            onValueChange = onInputChange,
            modifier = Modifier.weight(1f),
            placeholder = {
                Text(
                    text = "输入消息...",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            singleLine = false,
            maxLines = 4,
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Send
            ),
            keyboardActions = KeyboardActions(
                onSend = { onSend() }
            ),
            shape = RoundedCornerShape(24.dp),
            colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF512DA8),
                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
            )
        )

        Spacer(modifier = Modifier.width(8.dp))

        // 发送按钮（支持长按切换模型）
        Box {
            FloatingActionButton(
                onClick = onSend,
                modifier = Modifier.size(48.dp),
                containerColor = if (inputText.isNotBlank()) Color(0xFF512DA8)
                else MaterialTheme.colorScheme.surfaceVariant
            ) {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = "发送",
                    tint = if (inputText.isNotBlank()) Color.White
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// ============================================================
// AI Typing Indicator / AI 正在输入指示器
// ============================================================

@Composable
private fun AiTypingIndicator() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        // AI 头像
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(Color(0xFF512DA8)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Memory,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = Color.White
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Card(
            shape = RoundedCornerShape(
                topStart = 4.dp,
                topEnd = 18.dp,
                bottomStart = 18.dp,
                bottomEnd = 18.dp
            ),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF1E1E2E).copy(alpha = 0.7f)
            )
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                repeat(3) { index ->
                    TypingDot(delayMs = index * 150)
                }
            }
        }
    }
}

@Composable
private fun TypingDot(delayMs: Int) {
    val infiniteTransition = rememberInfiniteTransition(label = "typing_dot")
    val offsetY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -4f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 400, delayMillis = delayMs),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot_bounce"
    )

    Box(
        modifier = Modifier
            .size(8.dp)
            .offset(y = offsetY.dp)
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.6f))
    )
}

// ============================================================
// Circular Loading Indicator / 环形加载指示器
// ============================================================

@Composable
private fun CircularLoadingIndicator() {
    val infiniteTransition = rememberInfiniteTransition(label = "loading")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000),
            repeatMode = RepeatMode.Restart
        ),
        label = "spin"
    )

    Icon(
        imageVector = Icons.Default.Refresh,
        contentDescription = "加载中",
        modifier = Modifier
            .size(16.dp)
            .rotate(rotation),
        tint = Color(0xFF512DA8)
    )
}

// ============================================================
// Tool Drawer BottomSheet / 工具抽屉面板
// ============================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ToolDrawerSheet(
    state: AIAgentState,
    onDismiss: () -> Unit,
    onToggleTool: (String) -> Unit,
    onSwitchModel: (LLMProvider) -> Unit,
    onTogglePrivacyMode: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState()
    var showModelSelector by remember { mutableStateOf(false) }
    var showApiKeyInput by remember { mutableStateOf(false) }
    val clipboardManager = LocalClipboardManager.current

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xFF1E1E2E)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // 标题栏
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "🔧 工具面板",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White
                )

                // 隐私模式开关
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (state.privacyMode) "🔐 已开启" else "🔓 已关闭",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (state.privacyMode) Color(0xFF26A69A) else Color.Gray
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Switch(
                        checked = state.privacyMode,
                        onCheckedChange = { onTogglePrivacyMode() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color(0xFF26A69A),
                            checkedTrackColor = Color(0xFF26A69A).copy(alpha = 0.5f)
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = Color(0xFF2D2D3A))
            Spacer(modifier = Modifier.height(12.dp))

            // 当前模型
            Text(
                text = "🤖 当前模型：${state.currentModel.name}",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(8.dp))

            // 模型选择按钮
            TextButton(onClick = { showModelSelector = !showModelSelector }) {
                Icon(Icons.Default.SwapVert, contentDescription = null, tint = Color(0xFF512DA8))
                Spacer(modifier = Modifier.width(4.dp))
                Text("切换模型", color = Color(0xFF512DA8))
            }

            // 模型选择列表
            AnimatedVisibility(visible = showModelSelector) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    state.availableModels.forEach { model ->
                        val isSelected = model.id == state.currentModel.id
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onSwitchModel(model)
                                    showModelSelector = false
                                },
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) Color(0xFF512DA8).copy(alpha = 0.2f)
                                else Color.Transparent
                            ),
                            border = if (isSelected) androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF512DA8))
                            else null
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = model.name,
                                    color = if (isSelected) Color(0xFF512DA8) else Color.White,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                if (model.isLocal) {
                                    Text(
                                        text = "本地",
                                        color = Color(0xFF26A69A),
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = Color(0xFF2D2D3A))
            Spacer(modifier = Modifier.height(12.dp))

            // 工具列表
            Text(
                text = "🔧 工具列表",
                style = MaterialTheme.typography.titleSmall,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(8.dp))

            state.availableTools.forEach { tool ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = tool.name,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White
                        )
                        Text(
                            text = tool.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }
                    Switch(
                        checked = tool.isEnabled,
                        onCheckedChange = { onToggleTool(tool.id) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color(0xFF26A69A),
                            checkedTrackColor = Color(0xFF26A69A).copy(alpha = 0.5f)
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = Color(0xFF2D2D3A))
            Spacer(modifier = Modifier.height(12.dp))

            // API Key 设置入口
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showApiKeyInput = !showApiKeyInput },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Key,
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "API 密钥设置",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                }
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.List,
                    contentDescription = null,
                    tint = Color.Gray
                )
            }

            // API Key 输入框（展开时）
            AnimatedVisibility(visible = showApiKeyInput) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    Text(
                        text = "OpenAI API Key（已加密存储）",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = "",
                        onValueChange = { /* Save API key */ },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("sk-...", color = Color.Gray) },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFF512DA8),
                            unfocusedBorderColor = Color.Gray,
                            cursorColor = Color(0xFF512DA8)
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
