package com.mvi.kenny.feature.list

import android.os.Bundle
import android.view.WindowManager
import androidx.compose.runtime.Composable
import androidx.core.view.WindowCompat
import com.mvi.kenny.base.BaseActivity
import com.mvi.kenny.base.ThemeContext

/**
 * ListActivity — 保留作为独立页面（可直接被 Intent 唤起）
 * 主 App 内通过 Navigation Compose 使用 ListScreen
 */
class ListActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
    }

    @Composable
    override fun screen(theme: ThemeContext) {
        ListScreen()
    }
}
