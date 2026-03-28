package com.mvi.kenny.base

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * ============================================================
 * TopBarConfig — 顶部导航栏配置
 * ============================================================
 * MainScreen 持有唯一的 TopAppBar，每个 Tab 页面通过 onUpdateTopBar 回调
 * 动态上报自己的标题和操作按钮，MainScreen 统一渲染。
 *
 * 为什么不直接用 TopAppBar？
 * —————————————————————————————————————————————————————
 * 如果每个 Tab 有各自的 TopAppBar，滑动切换时会看到 TopAppBar 重建（闪烁）。
 * 统一 TopAppBar + 动态内容，可以实现平滑的页面切换效果。
 *
 * @param title TopBar 标题文本
 * @param showBackButton 是否显示返回按钮（用于二级页面，如登录页）
 * @param actions 右侧操作按钮列表，从左到右排列
 *
 * @see MainScreen 消费 TopBarConfig 的组件
 * @see TopBarAction 单个操作按钮的配置
 */
data class TopBarConfig(
    /** 标题文本 */
    val title: String,
    /** 是否显示返回按钮（预留，目前通过 actions 实现） */
    val showBackButton: Boolean = false,
    /** 右侧操作按钮列表 */
    val actions: List<TopBarAction> = emptyList()
)

/**
 * ============================================================
 * TopBarAction — 顶部导航栏操作按钮
 * ============================================================
 * 描述一个 IconButton，包含图标、描述文本和点击回调
 *
 * @param icon 按钮图标（ImageVector）
 * @param contentDescription 无障碍描述，供屏幕阅读器使用
 * @param onClick 点击按钮时执行的回调
 *
 * @see TopBarActions 预设按钮工厂方法
 */
data class TopBarAction(
    /** 按钮图标，如 Icons.Default.Settings */
    val icon: ImageVector,
    /** 无障碍文本描述，如 "设置" */
    val contentDescription: String,
    /** 点击回调，不能为 null */
    val onClick: () -> Unit
)

/**
 * ============================================================
 * TopBarActions — 预设 TopBarAction 工厂
 * ============================================================
 * 提供常用的 TopBarAction 实例化方法，统一图标和描述文本。
 *
 * 使用方式：
 * ```
 * TopBarConfig(
 *     title = "Home",
 *     actions = listOf(TopBarActions.settings { showSettings = true })
 * )
 * ```
 *
 * @see TopBarAction 单个按钮配置
 */
object TopBarActions {

    /**
     * 设置按钮（齿轮图标）
     *
     * @param onClick 点击回调
     * @return 预配置的 TopBarAction
     */
    fun settings(onClick: () -> Unit) = TopBarAction(
        icon = Icons.Default.Settings,
        contentDescription = "设置",
        onClick = onClick
    )

    /**
     * 列表按钮（刷新图标）
     * 常用于列表页的刷新操作
     *
     * @param onClick 点击回调
     * @return 预配置的 TopBarAction
     */
    fun list(onClick: () -> Unit) = TopBarAction(
        icon = Icons.Default.List,
        contentDescription = "列表",
        onClick = onClick
    )

    /**
     * 个人信息按钮（用户图标）
     *
     * @param onClick 点击回调
     * @return 预配置的 TopBarAction
     */
    fun person(onClick: () -> Unit) = TopBarAction(
        icon = Icons.Default.Person,
        contentDescription = "个人中心",
        onClick = onClick
    )

    /**
     * 返回按钮（箭头图标）
     * 用于顶部导航的返回操作，如退出登录后返回
     *
     * @param onClick 点击回调
     * @return 预配置的 TopBarAction
     */
    fun back(onClick: () -> Unit) = TopBarAction(
        icon = Icons.Default.ArrowBack,
        contentDescription = "返回",
        onClick = onClick
    )
}
