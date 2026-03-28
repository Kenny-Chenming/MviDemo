package com.mvi.kenny.base

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.mvi.kenny.R

/**
 * ============================================================
 * LoadingOverlay — 全局 Loading 遮罩
 * ============================================================
 * 覆盖在整个页面上方的半透明遮罩，带有居中加载指示器。
 * 用于需要阻止用户操作的全局加载场景（如登录请求、页面跳转）。
 *
 * 使用方式：
 * 在页面 Composable 的根 Box 中放置此组件：
 * ```
 * Box(modifier = Modifier.fillMaxSize()) {
 *     // 页面内容
 *     LoadingOverlay(loadingState = loadingState)
 * }
 * ```
 *
 * @param loadingState 当前页面的加载状态
 *        - LoadingState.Idle：不显示遮罩
 *        - LoadingState.Loading：显示遮罩和加载指示器
 *        - LoadingState.Error：可用于显示错误状态（当前版本未实现）
 *
 * @see LoadingState 三种状态定义
 */
@Composable
fun LoadingOverlay(loadingState: LoadingState) {
    // 只有 Loading 状态才显示遮罩
    if (loadingState is LoadingState.Loading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                // 半透明黑色背景，阻止用户点击下方内容
                .background(Color.Black.copy(alpha = 0.4f)),
            contentAlignment = Alignment.Center  // 内容居中
        ) {
            Card(
                // 使用 MaterialTheme 的 surface 颜色，避免被遮罩影响可读性
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier.padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // 环形加载指示器
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = stringResource(R.string.common_loading),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}
