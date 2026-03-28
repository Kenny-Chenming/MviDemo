package com.mvi.kenny.feature.list

import android.widget.Toast
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mvi.kenny.R
import com.mvi.kenny.base.TopBarActions
import com.mvi.kenny.base.TopBarConfig
import kotlinx.coroutines.flow.collect

/**
 * ============================================================
 * ListScreen — 列表页Composable
 * ============================================================
 * 支持：
 * - 初始加载状态（Loading）
 * - 下拉刷新（PullToRefreshBox）
 * - 空状态（无数据时显示空状态 UI）
 * - 点击列表项（Toast 提示）
 * - 左滑/点击删除列表项
 * - 错误提示（底部卡片）
 *
 * @param onUpdateTopBar 向 MainScreen 上报 TopBar 配置
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListScreen(
    onUpdateTopBar: (TopBarConfig) -> Unit = {}
) {
    val viewModel: ListViewModel = viewModel()
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    // 避免 lambda 内的 smart cast 问题
    val errorMessage = state.errorMessage
    val listTitle = stringResource(R.string.list_title)

    // 上报 TopBar 配置（刷新按钮）
    LaunchedEffect(state.isRefreshing) {
        onUpdateTopBar(
            TopBarConfig(
                title = listTitle,
                actions = listOf(
                    TopBarActions.list {
                        viewModel.sendIntent(ListIntent.RefreshItems)
                    }
                )
            )
        )
    }

    // Effect 处理
    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is ListEffect.ShowToast -> {
                    Toast.makeText(context, effect.message, Toast.LENGTH_SHORT).show()
                }
                is ListEffect.NavigateToDetail -> {
                    Toast.makeText(context, "点击: ${effect.item.title}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    Scaffold { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // 状态分支渲染
            when {
                // 初始加载中
                state.isLoading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                // 空列表
                state.items.isEmpty() && !state.isLoading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.List,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = stringResource(R.string.list_empty),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(onClick = { viewModel.sendIntent(ListIntent.LoadItems) }) {
                                Text(stringResource(R.string.list_retry))
                            }
                        }
                    }
                }
                // 正常列表（支持下拉刷新）
                else -> {
                    PullToRefreshBox(
                        isRefreshing = state.isRefreshing,
                        onRefresh = { viewModel.sendIntent(ListIntent.RefreshItems) },
                        modifier = Modifier.fillMaxSize()
                    ) {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(
                                items = state.items,
                                key = { it.id }  // key 优化重组性能
                            ) { item ->
                                ListItemCard(
                                    item = item,
                                    onDelete = { viewModel.sendIntent(ListIntent.DeleteItem(item.id)) },
                                    onClick = { viewModel.sendIntent(ListIntent.ClickItem(item)) }
                                )
                            }
                        }
                    }
                }
            }

            // 错误提示卡片（固定在底部）
            if (errorMessage != null && !state.isLoading) {
                Card(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        text = errorMessage,
                        modifier = Modifier.padding(12.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}

/**
 * 列表项卡片
 *
 * @param item 列表项数据
 * @param onDelete 删除按钮回调
 * @param onClick 整卡点击回调
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ListItemCard(
    item: ListItem,
    onDelete: () -> Unit,
    onClick: () -> Unit,
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = item.subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = stringResource(R.string.list_delete),
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}
