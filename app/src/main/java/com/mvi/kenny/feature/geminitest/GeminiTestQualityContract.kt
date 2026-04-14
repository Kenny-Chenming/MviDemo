package com.mvi.kenny.feature.geminitest

// =============================================================
// GeminiTestQualityContract — Gemini Test Quality MVI Contract
// Stub implementation / 占位符实现
// =============================================================

data class GeminiTestQualityState(
    val message: String = "Gemini Test Quality - Stub"
)

sealed interface GeminiTestQualityIntent
sealed interface GeminiTestQualityEffect
