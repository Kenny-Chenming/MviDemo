package com.mvi.kenny.feature.chat

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.layout.imePadding
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
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mvi.kenny.R
import com.mvi.kenny.base.TopBarActions
import com.mvi.kenny.base.TopBarConfig
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

/**
 * ============================================================
 * ChatScreen — AI 聊天助手Composable
 * ============================================================
 * 采用 MVI 架构，通过 StateFlow 驱动 UI 渲染。
 *
 * 布局结构：
 * ┌─────────────────────────────────────────────┐
 * │              TopBar（动态标题）               │
 * ├─────────────────────────────────────────────┤
 * │                                             │
 * │            消息列表（LazyColumn）            │
 * │  [系统消息] 居中灰色小字                     │
 * │  [用户消息] 右对齐，蓝色背景                  │
 * │  [AI 消息]   左对齐，白色背景                │
 * │  [Typing...]  AI 正在输入动画               │
 * │                                             │
 * ├─────────────────────────────────────────────┤
 * │  [输入框..........................] [发送]  │
 * └─────────────────────────────────────────────┘
 *
 * @param onUpdateTopBar 向 MainScreen 上报 TopBar 配置
 */
@Composable
fun ChatScreen(
    onUpdateTopBar: (TopBarConfig) -> Unit = {}
) {
    val viewModel: ChatViewModel = viewModel()
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current

    // 消息列表的 LazyColumn 状态（用于自动滚动到底部）
    val listState = rememberLazyListState()

    // 避免 lambda 智能 cast 问题
    val chatTitle = stringResource(R.string.chat_title)
    val isAiTyping = state.isAiTyping

    // ============================================================
    // TopBar 配置上报（清空按钮）
    // ============================================================
    LaunchedEffect(state.messages.size) {
        onUpdateTopBar(
            TopBarConfig(
                title = chatTitle,
                actions = listOfNotNull(
                    if (state.messages.isNotEmpty()) {
                        TopBarActions.delete { viewModel.sendIntent(ChatIntent.ClearChat) }
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
                is ChatEffect.ShowToast -> {
                    Toast.makeText(context, effect.message, Toast.LENGTH_SHORT).show()
                }
                is ChatEffect.ScrollToBottom -> {
                    // 消息列表滚动到底部
                    if (state.messages.isNotEmpty()) {
                        val lastIndex = state.messages.size - 1
                        listState.animateScrollToItem(lastIndex)
                    }
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
            .imePadding()  // 键盘弹出时调整布局
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
                items = state.messages,
                key = { it.id }  // key 优化重组性能
            ) { message ->
                ChatMessageItem(message = message)
            }

            // AI Typing 指示器（当 isAiTyping = true 时显示）
            item {
                AnimatedVisibility(
                    visible = isAiTyping,
                    enter = fadeIn(animationSpec = tween(300)),
                    exit = fadeOut(animationSpec = tween(300))
                ) {
                    AiTypingIndicator()
                }
            }

            // 底部占位（确保最后一条消息不被输入框遮挡）
            item {
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        // —— 输入区域 —— //
        ChatInputBar(
            inputText = state.inputText,
            onInputChange = { viewModel.sendIntent(ChatIntent.UpdateInput(it)) },
            onSend = {
                viewModel.sendIntent(ChatIntent.SendMessage)
                keyboardController?.hide()  // 发送后隐藏键盘
            }
        )
    }
}

/**
 * ============================================================
 * ChatMessageItem — 单条聊天消息
 * ============================================================
 * 根据消息角色（user / assistant / system）使用不同的对齐方式和样式。
 *
 * @param message 聊天消息数据
 */
@Composable
private fun ChatMessageItem(message: ChatMessage) {
    when (message.role) {
        ChatRole.USER -> UserMessageBubble(content = message.content)
        ChatRole.ASSISTANT -> AssistantMessageBubble(content = message.content)
        ChatRole.SYSTEM -> SystemMessageText(content = message.content)
    }
}

/**
 * 用户消息气泡（右对齐，右侧蓝色）
 *
 * @param content 消息文本
 */
@Composable
private fun UserMessageBubble(content: String) {
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
                containerColor = MaterialTheme.colorScheme.primary
            ),
            modifier = Modifier.widthIn(max = 280.dp)
        ) {
            Text(
                text = content,
                modifier = Modifier.padding(12.dp),
                color = MaterialTheme.colorScheme.onPrimary,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

/**
 * AI 助手消息气泡（左对齐，左侧渐变/品牌色）
 *
 * @param content 消息文本
 */
@Composable
private fun AssistantMessageBubble(content: String) {
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
                .background(MaterialTheme.colorScheme.tertiaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Brush,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = MaterialTheme.colorScheme.onTertiaryContainer
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
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            ),
            modifier = Modifier.widthIn(max = 280.dp)
        ) {
            Text(
                text = content,
                modifier = Modifier.padding(12.dp),
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

/**
 * 系统消息文本（居中，灰色小字）
 *
 * @param content 消息文本
 */
@Composable
private fun SystemMessageText(content: String) {
    Text(
        text = content,
        modifier = Modifier.fillMaxWidth(),
        textAlign = TextAlign.Center,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

/**
 * ============================================================
 * AiTypingIndicator — AI 正在输入指示器
 * ============================================================
 * 三个跳动的圆点动画，模拟 AI"思考"状态。
 * Compose 的 AnimatedVisibility 包裹，控制显示/隐藏。
 */
@Composable
private fun AiTypingIndicator() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        // AI 头像占位
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.tertiaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Brush,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = MaterialTheme.colorScheme.onTertiaryContainer
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
                containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f)
            )
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // 三个跳动的圆点
                repeat(3) { index ->
                    TypingDot(delayMs = index * 150)
                }
            }
        }
    }
}

/**
 * 单个跳动的圆点
 * 使用 animateFloatAsState 实现上下弹跳动画
 *
 * @param delayMs 动画延迟（错开三个圆点的动画时机）
 */
@Composable
private fun TypingDot(delayMs: Int) {
    var visible by remember { mutableStateOf(false) }

    // 启动弹跳动画
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(delayMs.toLong())
        visible = true
    }

    val offset by animateFloatAsState(
        targetValue = if (visible) -4f else 4f,
        animationSpec = tween(durationMillis = 400),
        label = "typing_dot"
    )

    Box(
        modifier = Modifier
            .size(8.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.6f))
            .padding(top = offset.dp)
    )
}

/**
 * ============================================================
 * ChatInputBar — 聊天输入框组件
 * ============================================================
 * 包含文本输入框和发送按钮。
 * 支持键盘回车发送（ImeAction.Send）。
 *
 * @param inputText 当前输入文本
 * @param onInputChange 输入文本变化回调
 * @param onSend 发送按钮点击回调
 */
@Composable
private fun ChatInputBar(
    inputText: String,
    onInputChange: (String) -> Unit,
    onSend: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 输入框
        OutlinedTextField(
            value = inputText,
            onValueChange = onInputChange,
            modifier = Modifier.weight(1f),
            placeholder = {
                Text(
                    text = stringResource(R.string.chat_input_hint),
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            // 单行输入，回车发送
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Send
            ),
            keyboardActions = KeyboardActions(
                onSend = { onSend() }
            ),
            shape = RoundedCornerShape(24.dp),
            colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
            )
        )

        Spacer(modifier = Modifier.width(8.dp))

        // 发送按钮
        FloatingActionButton(
            onClick = onSend,
            modifier = Modifier.size(48.dp),
            containerColor = if (inputText.isNotBlank()) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Send,
                contentDescription = stringResource(R.string.chat_send),
                tint = if (inputText.isNotBlank()) {
                    MaterialTheme.colorScheme.onPrimary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
        }
    }
}
