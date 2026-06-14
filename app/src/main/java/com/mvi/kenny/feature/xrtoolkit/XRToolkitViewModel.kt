// ================================================================
// XRToolkitViewModel — Android XR Gemini 智能眼镜开发工具包 ViewModel
// ================================================================
// MVI ViewModel for Android XR Gemini Smart Glasses Toolkit.
//
// PRD-267: Android XR Gemini 智能眼镜应用开发工具包
// Design Reference: memory/agency/designs/PRD-267-Android-XR-Gemini智能眼镜开发工具包.md
//
// 职责 / Responsibilities:
//   1. 接收 Intent，更新 State
//   2. 处理业务逻辑（模拟 SDK 下载、Gemini 调用、隐私审计等）
//   3. 通过 Effect Channel 发送一次性副作用（Toast、剪贴板等）
// ================================================================

package com.mvi.kenny.feature.xrtoolkit

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
 * XRToolkitViewModel — XR 工具包 MVI ViewModel
 * ============================================================
 *
 * @see XRToolkitState
 * @see XRToolkitIntent
 * @see XRToolkitEffect
 */
class XRToolkitViewModel : ViewModel() {

    // ── State ──────────────────────────────────────────────────────
    private val _state = MutableStateFlow(XRToolkitState.Initial)
    val state: StateFlow<XRToolkitState> = _state.asStateFlow()

    // ── Effect Channel ─────────────────────────────────────────────
    // One-time events — 用 Channel 而非 StateFlow 保证一次性消费
    private val _effect = Channel<XRToolkitEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    // ── Intent Handler ──────────────────────────────────────────────
    /**
     * Process user intent and update state accordingly.
     * 处理用户意图并相应更新状态。
     *
     * @param intent User intent from UI
     */
    fun sendIntent(intent: XRToolkitIntent) {
        when (intent) {
            is XRToolkitIntent.NavigateTo -> navigateTo(intent.page)
            is XRToolkitIntent.CompleteStage -> completeStage(intent.stageIndex)
            is XRToolkitIntent.DownloadSDK -> downloadSDK(intent.url)
            is XRToolkitIntent.CompleteConfigStep -> completeConfigStep(intent.stepIndex)
            is XRToolkitIntent.BuildHelloXR -> buildHelloXR(intent.projectPath)
            is XRToolkitIntent.SendGeminiPrompt -> sendGeminiPrompt(intent.prompt)
            is XRToolkitIntent.UpdatePrompt -> updatePrompt(intent.prompt)
            is XRToolkitIntent.SetVoiceInput -> setVoiceInput(intent.enabled)
            is XRToolkitIntent.SelectUISpecTab -> selectUISpecTab(intent.tab)
            is XRToolkitIntent.SetGlassesSimulation -> setGlassesSimulation(intent.active)
            is XRToolkitIntent.CopyCodeSample -> copyCodeSample(intent.code)
            is XRToolkitIntent.SetTranslateAnimation -> setTranslateAnimation(intent.show)
            is XRToolkitIntent.SelectARCoreVsXRScenario -> selectARCoreVsXRScenario(intent.scenario)
            is XRToolkitIntent.SetCamera -> setCamera(intent.enabled)
            is XRToolkitIntent.SetMicrophone -> setMicrophone(intent.enabled)
            is XRToolkitIntent.SetCrossDeviceConnection -> setCrossDeviceConnection(intent.enabled)
            is XRToolkitIntent.RunPrivacyAudit -> runPrivacyAudit()
            is XRToolkitIntent.DismissSnackbar -> dismissSnackbar()
            is XRToolkitIntent.NavigateBack -> navigateBack()
        }
    }

    // ── Navigation ─────────────────────────────────────────────────
    private fun navigateTo(page: XRToolkitPage) {
        _state.value = _state.value.copy(currentPage = page)
    }

    private fun navigateBack() {
        val current = _state.value.currentPage
        if (current == XRToolkitPage.INDEX) return

        // Navigate back to previous logical page
        val prevPage = when (current) {
            XRToolkitPage.GETTING_STARTED,
            XRToolkitPage.GEMINI,
            XRToolkitPage.UI_DESIGN,
            XRToolkitPage.LIVE_TRANSLATE,
            XRToolkitPage.ARCORE_VS_XR,
            XRToolkitPage.VIDEO_CALL,
            XRToolkitPage.TEST_DEBUG,
            XRToolkitPage.PRIVACY,
            XRToolkitPage.CROSS_DEVICE -> XRToolkitPage.INDEX
            else -> XRToolkitPage.INDEX
        }
        _state.value = _state.value.copy(currentPage = prevPage)
    }

    // ── Learning Path ─────────────────────────────────────────────
    private fun completeStage(stageIndex: Int) {
        val currentCompleted = _state.value.completedStages
        val newCompleted = currentCompleted + stageIndex
        _state.value = _state.value.copy(completedStages = newCompleted)

        viewModelScope.launch {
            _effect.send(XRToolkitEffect.ShowToast("阶段 ${stageIndex + 1} 已完成！"))
        }
    }

    // ── Getting Started ────────────────────────────────────────────
    private fun downloadSDK(url: String) {
        viewModelScope.launch {
            val currentGS = _state.value.gettingStartedState
            // Simulate download progress — 模拟下载进度
            for (progress in 1..10) {
                delay(300)
                _state.value = _state.value.copy(
                    gettingStartedState = currentGS.copy(
                        sdkDownloadProgress = progress / 10f
                    )
                )
            }
            _state.value = _state.value.copy(
                gettingStartedState = _state.value.gettingStartedState.copy(
                    sdkDownloadProgress = 1f,
                    isSdkInstalled = true
                )
            )
            _effect.send(XRToolkitEffect.SdkDownloadComplete)
            _effect.send(XRToolkitEffect.ShowToast("SDK 安装完成！"))
        }
    }

    private fun completeConfigStep(stepIndex: Int) {
        val currentSteps = _state.value.gettingStartedState.emulatorConfigSteps.toMutableList()
        if (stepIndex < currentSteps.size) {
            currentSteps[stepIndex] = currentSteps[stepIndex].copy(status = StepStatus.DONE)
            val nextStep = minOf(stepIndex + 1, currentSteps.size - 1)
            if (currentSteps[nextStep].status == StepStatus.PENDING) {
                currentSteps[nextStep] = currentSteps[nextStep].copy(status = StepStatus.IN_PROGRESS)
            }
            _state.value = _state.value.copy(
                gettingStartedState = _state.value.gettingStartedState.copy(
                    emulatorConfigSteps = currentSteps,
                    currentStep = nextStep
                )
            )
        }
    }

    private fun buildHelloXR(projectPath: String) {
        viewModelScope.launch {
            // Simulate build — 模拟构建过程
            delay(2000)
            _state.value = _state.value.copy(
                gettingStartedState = _state.value.gettingStartedState.copy(
                    isHelloXRBuilt = true
                )
            )
            _effect.send(XRToolkitEffect.HelloXRBuildSuccess)
            _effect.send(XRToolkitEffect.ShowToast("HelloXR 构建成功！"))
        }
    }

    // ── Gemini Integration ─────────────────────────────────────────
    private fun updatePrompt(prompt: String) {
        _state.value = _state.value.copy(
            geminiState = _state.value.geminiState.copy(inputPrompt = prompt)
        )
    }

    private fun setVoiceInput(enabled: Boolean) {
        _state.value = _state.value.copy(
            geminiState = _state.value.geminiState.copy(useVoiceInput = enabled)
        )
    }

    private fun sendGeminiPrompt(prompt: String) {
        if (prompt.isBlank()) return

        viewModelScope.launch {
            _state.value = _state.value.copy(
                geminiState = _state.value.geminiState.copy(
                    inputPrompt = prompt,
                    isLoading = true,
                    error = null
                )
            )

            // Simulate Gemini API call — 模拟 Gemini API 调用
            delay(1500)

            // Simulated response based on prompt content
            val simulatedResponse = buildString {
                appendLine("**Gemini XR Command Response**")
                appendLine()
                appendLine("我理解您想要在 Android XR 眼镜中实现这个功能。")
                appendLine("以下是推荐的技术实现路径：")
                appendLine()
                appendLine("1. **环境准备**")
                appendLine("   - 确保已安装 Android XR SDK (API 34+)")
                appendLine("   - 配置 Gemini API Key")
                appendLine()
                appendLine("2. **核心代码架构**")
                appendLine("   - 使用 GeminiClient 初始化 Gemini 连接")
                appendLine("   - 通过 VoiceInputManager 获取语音输入")
                appendLine("   - 使用 GlassesDisplay 渲染响应")
                appendLine()
                appendLine("3. **API 调用示例**")
                appendLine("   ```kotlin")
                appendLine("   val response = geminiModel.generateContent(prompt)")
                appendLine("   glassesDisplay.showText(response.text)")
                appendLine("   ```")
                appendLine()
                appendLine("4. **注意事项**")
                appendLine("   - XR 场景下 Gemini 响应延迟应 < 2s")
                appendLine("   - 文本显示使用高对比度白色字体")
                appendLine("   - 建议开启语音反馈增强无障碍体验")
            }

            _state.value = _state.value.copy(
                geminiState = _state.value.geminiState.copy(
                    geminiResponse = simulatedResponse,
                    isLoading = false
                )
            )
        }
    }

    // ── UI Design ──────────────────────────────────────────────────
    private fun selectUISpecTab(tab: UISpecTab) {
        _state.value = _state.value.copy(
            uiDesignState = _state.value.uiDesignState.copy(selectedSpecTab = tab)
        )
    }

    private fun setGlassesSimulation(active: Boolean) {
        _state.value = _state.value.copy(
            uiDesignState = _state.value.uiDesignState.copy(glassesSimulationActive = active)
        )

        viewModelScope.launch {
            val simType = when (_state.value.uiDesignState.selectedSpecTab) {
                UISpecTab.FONT_SIZE -> XRSimulationType.FONT_SIZE
                UISpecTab.COLOR -> XRSimulationType.COLOR_CONTRAST
                UISpecTab.LAYOUT -> XRSimulationType.LAYOUT_SAFETY
                UISpecTab.INTERACTION -> XRSimulationType.FONT_SIZE
            }
            if (active) {
                _effect.send(XRSimulationType.COLOR_CONTRAST.let {
                    XRToolkitEffect.ShowSimulation(simType)
                }.let { it })
            }
        }
    }

    // ── Code Copy ──────────────────────────────────────────────────
    private fun copyCodeSample(code: String) {
        viewModelScope.launch {
            _effect.send(XRToolkitEffect.CopyToClipboard(code))
            _effect.send(XRToolkitEffect.ShowToast("代码已复制到剪贴板"))
        }
    }

    // ── Translation ────────────────────────────────────────────────
    private fun setTranslateAnimation(show: Boolean) {
        _state.value = _state.value.copy(
            liveTranslateState = _state.value.liveTranslateState.copy(showAnimation = show)
        )
    }

    // ── ARCore vs XR ───────────────────────────────────────────────
    private fun selectARCoreVsXRScenario(scenario: String) {
        _state.value = _state.value.copy(
            arcoreVsXrState = _state.value.arcoreVsXrState.copy(selectedScenario = scenario)
        )
    }

    // ── Video Call ─────────────────────────────────────────────────
    private fun setCamera(enabled: Boolean) {
        _state.value = _state.value.copy(
            videoCallState = _state.value.videoCallState.copy(isCameraEnabled = enabled)
        )
    }

    private fun setMicrophone(enabled: Boolean) {
        _state.value = _state.value.copy(
            videoCallState = _state.value.videoCallState.copy(isMicrophoneEnabled = enabled)
        )
    }

    // ── Cross-Device ───────────────────────────────────────────────
    private fun setCrossDeviceConnection(enabled: Boolean) {
        viewModelScope.launch {
            val crossDevice = _state.value.crossDeviceState
            _state.value = _state.value.copy(
                crossDeviceState = crossDevice.copy(
                    isConnected = enabled,
                    syncStatus = if (enabled) SyncStatus.CONNECTING else SyncStatus.DISCONNECTED
                )
            )

            if (enabled) {
                // Simulate connection + sync — 模拟连接和同步
                delay(1500)
                _state.value = _state.value.copy(
                    crossDeviceState = _state.value.crossDeviceState.copy(
                        syncStatus = SyncStatus.SYNCED,
                        lastSyncTime = "刚刚"
                    )
                )
                _effect.send(XRToolkitEffect.ShowToast("跨设备连接成功！"))
            }
        }
    }

    // ── Privacy Audit ──────────────────────────────────────────────
    private fun runPrivacyAudit() {
        viewModelScope.launch {
            _state.value = _state.value.copy(
                privacyState = _state.value.privacyState.copy(isAuditing = true)
            )

            // Simulate audit progress — 模拟审计过程
            delay(2000)

            val items = defaultPrivacyAuditItems()
            val passed = items.count { it.status == PrivacyAuditStatus.PASS }
            val failed = items.count { it.status == PrivacyAuditStatus.FAIL }

            _state.value = _state.value.copy(
                privacyState = _state.value.privacyState.copy(
                    auditResults = items,
                    isAuditing = false,
                    passedItems = passed,
                    failedItems = failed
                )
            )

            val message = if (failed > 0) {
                "隐私审计完成：$passed 项通过，$failed 项需要修复"
            } else {
                "隐私审计完成：全部 $passed 项通过！"
            }
            _effect.send(XRToolkitEffect.ShowToast(message))
        }
    }

    // ── Snackbar ───────────────────────────────────────────────────
    private fun dismissSnackbar() {
        _state.value = _state.value.copy(snackbarMessage = null)
    }
}
