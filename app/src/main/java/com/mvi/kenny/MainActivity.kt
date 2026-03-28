package com.mvi.kenny

import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import androidx.compose.runtime.Composable
import androidx.core.view.WindowCompat
import com.mvi.kenny.base.BaseActivity
import com.mvi.kenny.base.ThemeContext
import com.mvi.kenny.feature.login.LoginActivity
import com.mvi.kenny.ui.screen.MainScreen

/**
 * ============================================================
 * MainActivity — App 主入口 Activity
 * ============================================================
 * AndroidManifest.xml 中 android:exported="true"，作为 App 的 Launch Activity。
 *
 * 职责极简：
 * - 继承 BaseActivity，获取语言切换和主题分发能力
 * - 在 onCreate 中配置 Window（状态栏、导航栏样式）
 * - 实现 screen()，渲染 MainScreen 主界面
 *
 * 所有复杂逻辑都封装在 BaseActivity 和各页面的 ViewModel/Screen 中。
 *
 * @see BaseActivity 语言切换、主题分发、Compose 环境初始化
 * @see MainScreen 主界面（共享 TopBar + Pager + BottomNav）
 */
class MainActivity : BaseActivity() {

    /**
     * onCreate — Window 配置
     * —————————————————————————————————————————————————————
     * 1. super.onCreate() 必须首先调用，触发 BaseActivity 的初始化
     * 2. WindowCompat.setDecorFitsSystemWindows(window, false)
     *    让内容延伸到系统窗口（状态栏/导航栏），实现边缘到边缘（edge-to-edge）效果
     * 3. FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS
     *    告诉系统此 Activity 自己绘制系统栏背景，配合上面的方法实现沉浸式
     *
     * @param savedInstanceState Activity 状态恢复数据
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 让内容延伸到系统窗口区域
        WindowCompat.setDecorFitsSystemWindows(window, false)

        // Activity 自己负责绘制系统栏背景（沉浸式效果）
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
    }

    /**
     * screen — 渲染主界面
     * —————————————————————————————————————————————————————
     * BaseActivity 会将 Compose 环境（主题、RTL、CompositionLocal）准备好后调用此方法。
     * 只需渲染 MainScreen 并传递必要的回调。
     *
     * @param theme ThemeContext，包含当前主题/语言状态（BaseActivity 分发）
     *
     * @see MainScreen 主界面组件
     */
    @Composable
    override fun screen(theme: ThemeContext) {
        MainScreen(
            // Profile 页面点击登录/退出时，跳转到 LoginActivity
            onNavigateToLogin = {
                startActivity(Intent(this, LoginActivity::class.java))
            }
        )
    }
}
