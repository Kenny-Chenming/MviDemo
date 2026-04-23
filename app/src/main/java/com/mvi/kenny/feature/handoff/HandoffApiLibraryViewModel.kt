package com.mvi.kenny.feature.handoff

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
 * HandoffApiLibraryViewModel — API Library ViewModel
 * ============================================================
 */
class HandoffApiLibraryViewModel : ViewModel() {

    private val _state = MutableStateFlow(HandoffApiLibraryState.Initial)
    val state: StateFlow<HandoffApiLibraryState> = _state.asStateFlow()

    private val _effect = Channel<HandoffApiLibraryEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    init { loadInitialData() }

    private fun loadInitialData() {
        _state.value = _state.value.copy(
            quickStartCode = buildQuickStartCode(),
            apiUsageExamples = buildApiExamples(),
            handoffConfig = HandoffConfig()
        )
    }

    private fun buildQuickStartCode(): String = """
package com.example.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.google.android.gms.handover.HandoffActivity

/**
 * HandoffActivity — 支持跨设备 Handoff 的 Activity
 * 继承 HandoffActivity，实现 onHandoffActivityRequested() 即可接入
 */
class MainActivity : HandoffActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { MyApp() }
    }

    override fun onHandoffActivityRequested(): HandoffActivity.Data? {
        val intent = intent.cloneFilter().apply {
            putExtra("handoff_timestamp", System.currentTimeMillis())
        }
        return HandoffActivity.Data.Builder(intent)
            .setActivityName(this::class.java.name)
            .setAppPackageName(packageName)
            .setPriority(HandoffActivity.Data.PRIORITY_HIGH)
            .build()
    }
}

@Composable
fun MyApp() {
    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Text("Handoff Ready App")
    }
}
    """.trimIndent()

    private fun buildApiExamples(): List<ApiExample> = listOf(
        ApiExample(
            title = "HandoffActivity 基础用法",
            description = "继承 HandoffActivity 并实现 onHandoffActivityRequested() 回调",
            code = """
// 继承 HandoffActivity
class ComposeActivity : HandoffActivity() {

    override fun onHandoffActivityRequested(): HandoffActivity.Data? {
        val intent = Intent(this, ComposeActivity::class.java).apply {
            putExtra("document_id", currentDocumentId)
            putExtra("scroll_position", scrollOffset)
        }
        return HandoffActivity.Data.Builder(intent)
            .setActivityName(this::class.java.name)
            .setAppPackageName(packageName)
            .build()
    }
}
            """.trimIndent(),
            tags = listOf("HandoffActivity", "基础", "Activity")
        ),
        ApiExample(
            title = "HandoffManager 设备发现",
            description = "使用 HandoffManager 发现附近已配对设备",
            code = """
// 获取 HandoffManager 实例
val handoffManager = HandoffManager.getInstance(context)

// 发现附近设备
handoffManager.startDiscovery(object : DiscoveryCallback {
    override fun onDeviceFound(device: HandoffDevice) {
        Log.d("Handoff", "发现设备: " + device.name)
    }

    override fun onDiscoveryFailed(error: Int) {
        Log.e("Handoff", "发现失败: " + error)
    }
})

// 停止发现（节省电量）
handoffManager.stopDiscovery()
            """.trimIndent(),
            tags = listOf("HandoffManager", "设备发现", "Discovery")
        ),
        ApiExample(
            title = "发送 Handoff 请求",
            description = "从当前设备向目标设备发送 Handoff 请求",
            code = """
// 构建 Handoff 请求
val handoffRequest = HandoffActivity.Data.Builder(sourceIntent)
    .setActivityName(targetActivityName)
    .setAppPackageName(packageName)
    .setPriority(HandoffActivity.Data.PRIORITY_NORMAL)
    .setExpiration(System.currentTimeMillis() + 5 * 60 * 1000)
    .build()

// 发送到目标设备
handoffManager.sendHandoffRequest(targetDeviceId, handoffRequest,
    object : HandoffCallback {
        override fun onSuccess() { Log.d("Handoff", "发送成功") }
        override fun onFailure(error: Int) { Log.e("Handoff", "发送失败") }
    })
            """.trimIndent(),
            tags = listOf("发送", "HandoffManager", "Request")
        ),
        ApiExample(
            title = "Kotlin Compose 封装",
            description = "Compose 版本的一键式 Handoff 按钮组件",
            code = """
@Composable
fun HandoffButton(
    activityClass: Class<*>,
    handoffData: Map<String, Any>,
    modifier: Modifier = Modifier
) {
    val handoffManager = remember { HandoffManager.getInstance(context) }

    Button(
        onClick = {
            val intent = Intent(context, activityClass).apply {
                handoffData.forEach { (key, value) ->
                    when (value) {
                        is String -> putExtra(key, value)
                        is Int -> putExtra(key, value)
                        is Boolean -> putExtra(key, value)
                    }
                }
            }
            // Trigger Handoff...
        },
        modifier = modifier
    ) {
        Icon(Icons.Default.Share, contentDescription = null)
        Spacer(Modifier.width(8.dp))
        Text("跨设备继续")
    }
}
            """.trimIndent(),
            tags = listOf("Compose", "封装", "UI")
        )
    )

    fun sendIntent(intent: HandoffApiLibraryIntent) {
        when (intent) {
            is HandoffApiLibraryIntent.SwitchTab -> _state.value = _state.value.copy(selectedTab = intent.tab)
            is HandoffApiLibraryIntent.CopyCode -> {
                _state.value = _state.value.copy(copiedItem = intent.label)
                viewModelScope.launch { _effect.send(HandoffApiLibraryEffect.CodeCopied(intent.label)) }
                viewModelScope.launch {
                    delay(2000)
                    _state.value = _state.value.copy(copiedItem = null)
                }
            }
            is HandoffApiLibraryIntent.UpdateConfig -> _state.value = _state.value.copy(handoffConfig = intent.config)
            is HandoffApiLibraryIntent.ClearCopyFeedback -> _state.value = _state.value.copy(copiedItem = null)
        }
    }
}
