package com.mvi.kenny.feature.locationbutton

/**
 * ============================================================
 * LocationButtonContract — Android 17 Location Button MVI 契约
 * ============================================================
 * MVI architecture contract for LocationButton feature.
 *
 * Design Reference: designs/PRD-081-Android-17-Location-Button-Jetpack-Library.md
 * MVI 三要素 / Three pillars:
 * - Model (State): Immutable page state, single source of truth
 * - View: Composable function, consumes State, renders UI
 * - Intent: User intentions, ViewModel processes and updates State
 * Effect: One-time side effects (navigation, toast), delivered via Channel
 * —————————————————————————————————————————————————————
 */

/**
 * ============================================================
 * LocationStatus — 位置授权状态枚举
 * ============================================================
 *
 * @param displayName 中文显示名称
 */
enum class LocationStatus(val displayName: String) {
    /** 初始状态，等待用户操作 */
    Idle("等待操作"),
    /** 正在请求权限 */
    Requesting("请求中"),
    /** 权限已授予 */
    Granted("已授权"),
    /** 权限被拒绝 */
    Denied("已拒绝"),
    /** 权限被永久拒绝，需手动去设置开启 */
    PermanentlyDenied("永久拒绝"),
    /** 发生错误 */
    Error("错误")
}

/**
 * ============================================================
 * DeniedType — 权限拒绝类型枚举
 * ============================================================
 *
 * @param description 拒绝类型描述
 */
enum class DeniedType(val description: String) {
    /** 用户在对话框中主动拒绝 */
    UserDenied("用户主动拒绝"),
    /** 用户勾选"不再询问"后永久拒绝 */
    PermanentlyDenied("永久拒绝，需去设置开启"),
    /** 设备硬件不支持 Location Button */
    HardwareNotSupported("设备不支持 Location Button"),
    /** 未知原因 */
    Unknown("未知原因")
}

/**
 * ============================================================
 * LocationButtonState — 位置按钮页面状态
 * ============================================================
 *
 * @param status Current location permission status
 * @param isSupported Whether the device supports Location Button (Android 17+)
 * @param fallbackUsed Whether traditional permission fallback was triggered
 * @param activeTemplate Currently active template tab: main / map / checkin / social
 *
 * @see LocationStatus Status enum
 */
data class LocationButtonState(
    val status: LocationStatus = LocationStatus.Idle,
    val isSupported: Boolean = true,
    val fallbackUsed: Boolean = false,
    val activeTemplate: TemplateTab = TemplateTab.Main
) {
    companion object {
        /** Initial/empty state */
        val Initial = LocationButtonState()
    }
}

/**
 * ============================================================
 * TemplateTab — 模板 Tab 枚举
 * ============================================================
 *
 * @param title Tab 显示标题
 */
enum class TemplateTab(val title: String) {
    Main("主入口"),
    Map("地图模板"),
    CheckIn("签到模板"),
    Social("社交模板")
}

/**
 * ============================================================
 * LocationButtonIntent — 用户意图（User Intent）
 * —————————————————————————————————————————————————————
 * Every user action on the page corresponds to an Intent.
 * ViewModel receives Intent, processes business logic, then updates State.
 *
 * @see LocationButtonViewModel.sendIntent handles all Intents
 */
sealed interface LocationButtonIntent {

    /** 用户点击按钮，请求位置权限 */
    data object RequestLocation : LocationButtonIntent

    /** 用户点击"去设置"按钮，跳转到系统设置 */
    data object OpenAppSettings : LocationButtonIntent

    /** 用户点击重置按钮，恢复初始状态 */
    data object Reset : LocationButtonIntent

    /** 切换模板 Tab
     * @param tab Target template tab
     */
    data class SwitchTemplate(val tab: TemplateTab) : LocationButtonIntent
}

/**
 * ============================================================
 * LocationButtonEffect — 一次性副作用（Effect）
 * —————————————————————————————————————————————————————
 * One-time events, immutable, can only be consumed once.
 * UI layer listens via LaunchedEffect + flow.collect{}.
 *
 * @see LocationButtonViewModel _effect.send() sends Effects
 */
sealed interface LocationButtonEffect {

    /** 位置权限已授予
     * @param isPrecise Whether precise location (fine) was granted
     */
    data class LocationGranted(val isPrecise: Boolean) : LocationButtonEffect

    /** 位置权限被拒绝
     * @param type Denial type
     */
    data class LocationDenied(val type: DeniedType) : LocationButtonEffect

    /** 触发了传统权限回退（设备不支持 Location Button）
     * @param reason Fallback reason
     */
    data class FallbackTriggered(val reason: String) : LocationButtonEffect

    /** 打开系统设置页面 */
    data object OpenSettings : LocationButtonEffect

    /** 发生错误
     * @param message Error message
     */
    data class Error(val message: String) : LocationButtonEffect

    /** 显示 Snackbar 消息
     * @param message Snackbar text
     */
    data class ShowSnackbar(val message: String) : LocationButtonEffect
}

/**
 * ============================================================
 * LocationPermissionResult — 权限请求结果（对外回调数据类）
 * ============================================================
 *
 * @param granted Whether location permission was granted
 * @param isPrecise Whether precise location (fine) was granted (only meaningful when granted=true)
 * @param deniedType Type of denial if not granted
 */
data class LocationPermissionResult(
    val granted: Boolean,
    val isPrecise: Boolean = false,
    val deniedType: DeniedType? = null
)
