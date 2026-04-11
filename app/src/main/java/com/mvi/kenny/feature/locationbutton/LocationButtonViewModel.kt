package com.mvi.kenny.feature.locationbutton

import android.Manifest
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

/**
 * ============================================================
 * LocationButtonViewModel — 位置按钮 ViewModel
 * ============================================================
 * MVI architecture ViewModel for LocationButton feature.
 *
 * Design Reference: designs/PRD-081-Android-17-Location-Button-Jetpack-Library.md
 *
 * Key behaviors:
 * 1. Checks device support for Location Button (Android 17+ = API 37)
 * 2. If supported: requests location via system Location Button dialog
 * 3. If not supported: falls back to traditional permission request
 * 4. Handles permanently denied scenario with settings navigation
 * —————————————————————————————————————————————————————
 */
class LocationButtonViewModel : ViewModel() {

    // MVI State — single source of truth using StateFlow
    private val _state = MutableStateFlow(LocationButtonState.Initial)
    val state: StateFlow<LocationButtonState> = _state.asStateFlow()

    // MVI Effect — one-time side effects via Channel
    private val _effect = Channel<LocationButtonEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    init {
        checkDeviceSupport()
    }

    /**
     * Checks whether the device supports Android 17 Location Button.
     * Sets fallbackUsed flag if device does not support it.
     */
    private fun checkDeviceSupport() {
        val isSupported = LocationButtonChecker.isLocationButtonSupported()
        _state.value = _state.value.copy(
            isSupported = isSupported,
            fallbackUsed = !isSupported
        )
        if (!isSupported) {
            viewModelScope.launch {
                _effect.send(LocationButtonEffect.FallbackTriggered("设备不支持 Location Button，已回退到传统权限流程"))
            }
        }
    }

    /**
     * Processes incoming user Intents and updates state accordingly.
     *
     * @param intent User intent from the UI layer
     */
    fun sendIntent(intent: LocationButtonIntent) {
        when (intent) {
            is LocationButtonIntent.RequestLocation -> requestLocation()
            is LocationButtonIntent.OpenAppSettings -> openSettings()
            is LocationButtonIntent.Reset -> reset()
            is LocationButtonIntent.SwitchTemplate -> switchTemplate(intent.tab)
        }
    }

    /**
     * Initiates location permission request.
     * Uses Location Button on Android 17+, falls back to traditional
     * ACCESS_FINE_LOCATION request on older versions.
     */
    private fun requestLocation() {
        _state.value = _state.value.copy(status = LocationStatus.Requesting)
    }

    /**
     * Updates permission request status after result.
     * Called from the UI layer after permission result is received.
     *
     * @param granted Whether permission was granted
     * @param isPrecise Whether fine (precise) location was granted
     * @param isPermanentlyDenied Whether user selected "Don't ask again"
     */
    fun onPermissionResult(granted: Boolean, isPrecise: Boolean, isPermanentlyDenied: Boolean) {
        if (granted) {
            _state.value = _state.value.copy(status = LocationStatus.Granted)
            viewModelScope.launch {
                _effect.send(LocationButtonEffect.LocationGranted(isPrecise))
            }
        } else {
            val deniedType = when {
                isPermanentlyDenied -> DeniedType.PermanentlyDenied
                else -> DeniedType.UserDenied
            }
            _state.value = _state.value.copy(status = LocationStatus.PermanentlyDenied)
            viewModelScope.launch {
                _effect.send(LocationButtonEffect.LocationDenied(deniedType))
            }
        }
    }

    /**
     * Opens the app's system settings page where user can manually
     * grant location permission after permanent denial.
     */
    private fun openSettings() {
        viewModelScope.launch {
            _effect.send(LocationButtonEffect.OpenSettings)
        }
    }

    /**
     * Resets the state to initial, clearing any previous permission result.
     */
    private fun reset() {
        _state.value = LocationButtonState(
            isSupported = _state.value.isSupported,
            fallbackUsed = _state.value.fallbackUsed
        )
    }

    /**
     * Switches the active template tab.
     *
     * @param tab Target template tab
     */
    private fun switchTemplate(tab: TemplateTab) {
        _state.value = _state.value.copy(activeTemplate = tab)
    }
}

/**
 * ============================================================
 * LocationButtonChecker — Location Button 支持性检测工具类
 * ============================================================
 * Static utility for detecting whether the device supports
 * Android 17 Location Button feature.
 *
 * Location Button is available on Android 17 (API 37).
 * On older versions, the feature falls back to traditional permission request.
 */
object LocationButtonChecker {

    /**
     * Checks whether the device supports Android 17 Location Button.
     *
     * Location Button requires:
     * - Android 17 (API Level 37)
     * - The feature is part of the Google Play Services location button system
     *
     * @return true if Location Button is supported, false otherwise
     */
    fun isLocationButtonSupported(): Boolean {
        // Android 17 is API level 37
        // Build.VERSION_CODES.VANILLA is API 35 (Android 15)
        // We use numeric comparison to target Android 17 specifically
        return Build.VERSION.SDK_INT >= 37
    }

    /**
     * Returns a human-readable description of why Location Button is
     * or is not supported on this device.
     *
     * @return Support description string
     */
    fun getSupportDescription(): String {
        return if (isLocationButtonSupported()) {
            "设备支持 Location Button（Android ${Build.VERSION.SDK_INT}）"
        } else {
            "设备不支持 Location Button（Android ${Build.VERSION.SDK_INT} < 37），将使用传统权限流程"
        }
    }
}

/**
 * ============================================================
 * rememberLocationButtonState — 状态持有 Composable Hook
 * ============================================================
 * Remembers and manages LocationButton state within a Composable context.
 * Provides a convenient way to use LocationButton in any Composable
 * without requiring a full ViewModel instance.
 *
 * Usage:
 * ```
 * val locationState = rememberLocationButtonState()
 * LocationButton(
 *     state = locationState.state,
 *     onIntent = locationState.sendIntent,
 *     effect = locationState.effect
 * )
 * ```
 *
 * @return LocationButtonStateHolder with state, intent, and effect
 */
@Composable
fun rememberLocationButtonState(): LocationButtonStateHolder {
    // Use the traditional permission request launcher
    // Location Button dialog is system-managed on Android 17+
    val fineLocationLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        // This result is processed by the caller
    }

    val coarseLocationLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        // This result is processed by the caller
    }

    return remember {
        LocationButtonStateHolder(
            fineLocationLauncher = { fineLocationLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION) },
            coarseLocationLauncher = { coarseLocationLauncher.launch(Manifest.permission.ACCESS_COARSE_LOCATION) }
        )
    }
}

/**
 * LocationButton state holder returned by rememberLocationButtonState().
 * Bundles launcher references for permission requests.
 *
 * @param fineLocationLauncher Launcher for ACCESS_FINE_LOCATION
 * @param coarseLocationLauncher Launcher for ACCESS_COARSE_LOCATION
 */
data class LocationButtonStateHolder(
    val fineLocationLauncher: () -> Unit,
    val coarseLocationLauncher: () -> Unit
)
