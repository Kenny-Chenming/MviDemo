package com.mvi.kenny.feature.android17memory

// ================================================================
// Android17MemoryViewModelFactory — ViewModel 工厂
// ================================================================
// Provides Android17MemoryViewModel instances for Compose.
//
// ViewModel in MVI: holds business logic and exposes State via StateFlow.
// ViewModelFactory: Android ViewModel architecture component pattern.
// ================================================================

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

/**
 * ViewModelFactory for Android17MemoryViewModel.
 *
 * @param context Application context (required for ActivityManager, PackageManager)
 */
class Android17MemoryViewModelFactory(
    private val context: Context
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(Android17MemoryViewModel::class.java)) {
            return Android17MemoryViewModel(context.applicationContext) as T
        }
        throw IllegalArgumentException(
            "Unknown ViewModel class: ${modelClass.name}. " +
                    "Expected: Android17MemoryViewModel"
        )
    }
}
