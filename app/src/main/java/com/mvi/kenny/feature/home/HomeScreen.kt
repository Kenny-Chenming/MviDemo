package com.mvi.kenny.feature.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mvi.kenny.R
import com.mvi.kenny.base.TopBarActions
import com.mvi.kenny.base.TopBarConfig
import com.mvi.kenny.ui.theme.rememberThemeContext
import kotlinx.coroutines.flow.collect

/**
 * ============================================================
 * HomeScreen — 首页Composable
 * ============================================================
 * 首页主页面，负责：
 * 1. 从 ViewModel 获取状态并渲染 UI
 * 2. 通过 onUpdateTopBar 回调向 MainScreen 上报 TopBar 配置
 * 3. 响应用户操作（发送 Intent）
 *
 * ViewModel 生命周期：
 * viewModel() 是 Composable 扩展函数，在 Composable 作用域内获取 ViewModel。
 * 由于 MainScreen 使用 HorizontalPager，三个 Tab 页面同时存在于内存中，
 * 每个页面的 ViewModel 独立管理自己的状态，互不影响。
 *
 * @param onNavigateToList 跳转到列表 Tab 的回调（目前通过 Tab 切换即可，无需特殊处理）
 * @param onNavigateToLogin 退出登录后跳转登录页的回调
 * @param onUpdateTopBar 向 MainScreen 上报 TopBar 配置（标题 + 操作按钮）
 *
 * @see HomeViewModel 状态管理
 * @see MainScreen 消费 onUpdateTopBar 的父组件
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToList: () -> Unit = {},
    onNavigateToLogin: () -> Unit = {},
    onUpdateTopBar: (TopBarConfig) -> Unit = {}
) {
    // 从 BaseActivity 的 CompositionLocal 获取主题上下文（主题/语言）
    val theme = rememberThemeContext()

    // 获取 ViewModel（viewModel() 是 lifecycle-viewmodel-compose 提供的 Composable 扩展）
    val viewModel: HomeViewModel = viewModel()

    // 订阅 ViewModel 的状态（State → Compose State，变化时自动重组）
    val state by viewModel.state.collectAsState()

    // 对话框可见性状态
    var showEditDialog by remember { mutableStateOf(false) }  // 编辑昵称对话框
    var showSettingsDialog by remember { mutableStateOf(false) }  // 设置对话框

    // 避免在 lambda 表达式中直接访问 state.user / state.errorMessage
    //（因为状态更新可能发生在 recomposition 之外，导致 smart cast 失效）
    val user = state.user
    val errorMessage = state.errorMessage

    // ============================================================
    // TopBar 配置（动态）
    // ============================================================
    // remember(showSettingsDialog) 在对话框状态变化时重建 TopBarConfig
    // mutableStateOf() 使其成为可观察状态，确保 onUpdateTopBar 能感知变化
    val topBarConfig by remember(showSettingsDialog) {
        mutableStateOf(
            TopBarConfig(
                title = "Home",
                actions = listOfNotNull(
                    // 设置对话框打开时隐藏按钮（避免重复打开）
                    if (showSettingsDialog) null
                    else TopBarActions.settings { showSettingsDialog = true }
                )
            )
        )
    }

    // 上报 TopBar 配置给 MainScreen
    // LaunchedEffect(topBarConfig) 确保每次配置变化时都执行上报
    LaunchedEffect(topBarConfig) {
        onUpdateTopBar(topBarConfig)
    }

    // ============================================================
    // Effect 收集（导航、Toast等一次性事件）
    // ============================================================
    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is HomeEffect.NavigateToLogin -> onNavigateToLogin()
                is HomeEffect.NavigateToList -> onNavigateToList()
                is HomeEffect.ShowToast -> { /* Toast 由其他方式处理 */ }
                is HomeEffect.ShowError -> { /* 错误已在 UI 显示 */ }
            }
        }
    }

    // ============================================================
    // 页面主体
    // ============================================================
    Scaffold { innerPadding ->
        Box(modifier = Modifier.fillMaxSize()) {
            // 主内容区域（可滚动）
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())  // 内容超出时可滚动
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 加载指示器（初始加载时显示）
                if (state.isLoading) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // 已登录：显示用户卡片
                if (user != null) {
                    UserCard(
                        name = user.name,
                        email = user.email,
                        onEditClick = { showEditDialog = true },
                        onLogoutClick = { viewModel.sendIntent(HomeIntent.Logout) }
                    )
                }
                // 未登录且不在加载中：显示提示和加载按钮
                else if (!state.isLoading) {
                    Text(
                        text = stringResource(R.string.home_no_user),
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { viewModel.sendIntent(HomeIntent.LoadUser) }) {
                        Text(stringResource(R.string.home_load_user))
                    }
                }

                // 错误提示卡片
                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Text(
                            text = errorMessage,
                            modifier = Modifier.padding(16.dp),
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }

            // 全屏加载遮罩（初始加载时覆盖整个页面）
            if (state.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }
    }

    // 编辑昵称对话框
    if (showEditDialog) {
        EditNameDialog(
            currentName = user?.name ?: "",
            onDismiss = { showEditDialog = false },
            onConfirm = { newName ->
                viewModel.sendIntent(HomeIntent.UpdateName(newName))
                showEditDialog = false
            }
        )
    }

    // 设置对话框（主题 + 语言）
    if (showSettingsDialog) {
        SettingsDialog(
            currentThemeMode = theme.themeMode,
            currentLanguage = theme.language,
            onThemeModeChange = theme.onThemeModeChange,
            onLanguageChange = theme.onLanguageChange,
            onDismiss = { showSettingsDialog = false }
        )
    }
}

/**
 * 用户信息卡片
 * —————————————————————————————————————————————————————
 * 显示用户头像、昵称、邮箱，以及编辑和退出登录按钮
 *
 * @param name 用户昵称
 * @param email 用户邮箱
 * @param onEditClick 点击编辑按钮的回调
 * @param onLogoutClick 点击退出登录按钮的回调
 */
@Composable
private fun UserCard(
    name: String,
    email: String,
    onEditClick: () -> Unit,
    onLogoutClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 用户头像
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                modifier = Modifier.size(72.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(16.dp))

            // 昵称
            Text(text = name, style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(8.dp))

            // 邮箱
            Text(
                text = email,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(24.dp))

            // 操作按钮行
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(onClick = onEditClick, modifier = Modifier.weight(1f)) {
                    Text(stringResource(R.string.home_edit))
                }
                Button(
                    onClick = onLogoutClick,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text(stringResource(R.string.home_logout))
                }
            }
        }
    }
}

/**
 * 编辑昵称对话框
 * —————————————————————————————————————————————————————
 *
 * @param currentName 当前昵称（预填到输入框）
 * @param onDismiss 关闭对话框回调
 * @param onConfirm 确认修改回调，传入新昵称
 */
@Composable
private fun EditNameDialog(
    currentName: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    // remember(currentName) 在对话框打开时初始化输入框内容
    var name by remember { mutableStateOf(currentName) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.home_edit_name_title)) },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text(stringResource(R.string.home_name_label)) },
                singleLine = true,  // 单行输入
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(name) },
                enabled = name.isNotBlank()  // 空内容时禁用确认按钮
            ) {
                Text(stringResource(R.string.home_confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.home_cancel))
            }
        }
    )
}
