package com.mvi.kenny.feature.geminitest

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class GeminiTestQualityViewModel : ViewModel() {
    private val _state = MutableStateFlow(GeminiTestQualityState())
    val state: StateFlow<GeminiTestQualityState> = _state.asStateFlow()
}
