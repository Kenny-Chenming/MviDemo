package com.mvi.kenny.feature.list

/**
 * ============================================================
 * ListContract — 列表页 MVI 契约
 * ============================================================
 */

/**
 * 列表项数据模型
 *
 * @param id 唯一标识符
 * @param title 标题
 * @param subtitle 副标题/描述
 * @param imageUrl 配图 URL（预留字段，当前版本未使用）
 */
data class ListItem(
    val id: Long,
    val title: String,
    val subtitle: String,
    val imageUrl: String? = null
)

/**
 * 列表页面状态
 *
 * @param isLoading 初始加载中
 * @param isRefreshing 下拉刷新中
 * @param items 当前列表数据
 * @param errorMessage 错误信息
 */
data class ListState(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val items: List<ListItem> = emptyList(),
    val errorMessage: String? = null
) {
    companion object {
        val Initial = ListState()
    }
}

/**
 * 列表页面意图
 *
 * @see ListViewModel.sendIntent
 */
sealed interface ListIntent {
    /** 初始加载列表数据 */
    data object LoadItems : ListIntent

    /** 下拉/点击刷新 */
    data object RefreshItems : ListIntent

    /** 删除指定 id 的列表项
     * @param id 要删除的 item id
     */
    data class DeleteItem(val id: Long) : ListIntent

    /** 点击列表项
     * @param item 被点击的列表项数据
     */
    data class ClickItem(val item: ListItem) : ListIntent
}

/**
 * 列表页面副作用
 */
sealed interface ListEffect {
    /** 显示 Toast
     * @param message Toast 文本
     */
    data class ShowToast(val message: String) : ListEffect

    /** 跳转详情页
     * @param item 被点击的列表项
     */
    data class NavigateToDetail(val item: ListItem) : ListEffect
}
