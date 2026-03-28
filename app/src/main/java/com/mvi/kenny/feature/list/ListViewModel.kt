package com.mvi.kenny.feature.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

/**
 * ============================================================
 * ListViewModel — 列表页状态管理
 * ============================================================
 */
class ListViewModel : ViewModel() {

    private val _state = MutableStateFlow(ListState.Initial)
    val state: StateFlow<ListState> = _state.asStateFlow()
    val currentState: ListState get() = _state.value

    private val _effect = Channel<ListEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    init {
        sendIntent(ListIntent.LoadItems)
    }

    fun sendIntent(intent: ListIntent) {
        when (intent) {
            is ListIntent.LoadItems -> loadItems()
            is ListIntent.RefreshItems -> refreshItems()
            is ListIntent.DeleteItem -> deleteItem(intent.id)
            is ListIntent.ClickItem -> clickItem(intent.item)
        }
    }

    /**
     * 初始加载列表
     */
    private fun loadItems() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, errorMessage = null)
            try {
                delay(1000)  // 模拟网络请求延迟
                val items = List(20) { i ->
                    ListItem(
                        id = i.toLong(),
                        title = "Item ${i + 1}",
                        subtitle = "这是第 ${i + 1} 条数据的描述信息，内容随机 ${('a'..'z').shuffled().take(8).joinToString("")}"
                    )
                }
                _state.value = _state.value.copy(isLoading = false, items = items)
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    errorMessage = "加载失败: ${e.message}"
                )
                _effect.send(ListEffect.ShowToast("加载失败: ${e.message}"))
            }
        }
    }

    /**
     * 下拉/点击刷新
     */
    private fun refreshItems() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isRefreshing = true, errorMessage = null)
            try {
                delay(800)
                val items = List(20) { i ->
                    ListItem(
                        id = i.toLong(),
                        title = "刷新 Item ${i + 1}",
                        subtitle = "下拉刷新后的新数据，内容随机 ${('A'..'Z').shuffled().take(6).joinToString("")}"
                    )
                }
                _state.value = _state.value.copy(isRefreshing = false, items = items)
                _effect.send(ListEffect.ShowToast("刷新成功"))
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isRefreshing = false,
                    errorMessage = "刷新失败: ${e.message}"
                )
            }
        }
    }

    /**
     * 删除列表项（本地删除，无网络请求）
     */
    private fun deleteItem(id: Long) {
        viewModelScope.launch {
            _state.value = _state.value.copy(
                items = _state.value.items.filter { it.id != id }
            )
            _effect.send(ListEffect.ShowToast("已删除"))
        }
    }

    /**
     * 点击列表项
     */
    private fun clickItem(item: ListItem) {
        viewModelScope.launch {
            _effect.send(ListEffect.NavigateToDetail(item))
        }
    }
}
