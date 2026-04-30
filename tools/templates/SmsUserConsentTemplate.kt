// ================================================================
// SmsUserConsentTemplate — SMS User Consent API 集成模板
// ================================================================
// Complete integration template for SMS User Consent API.
//
// PRD-186: Android 17 SMS OTP Delay 合规检测与迁移工具包
// Design Reference: memory/agency/designs/PRD-186-SMS-OTP-Delay-合规检测与迁移工具包.md
//
// SMS User Consent API 是 SMS Retriever API 的替代方案:
//   - 需要用户手动点击确认才能读取 SMS
//   - 不受 Android 17 的 3 小时延迟限制
//   - 适用于 SMS Retriever 无法工作的情况（如特殊格式的 OTP 短信）
//
// When to use User Consent vs Retriever:
//   - Retriever: User receives OTP and it's automatically read (best UX)
//   - User Consent: User receives OTP and must tap to confirm reading
//
// @see SmsRetrieverTemplate for the preferred automatic solution
// ================================================================

package com.mvi.kenny.otpdelay.templates

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.util.Log
import android.view.View
import androidx.annotation.OptIn
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.google.android.gms.auth.api.phone.SmsConsentingClient
import com.google.android.gms.auth.api.phone.SmsRetriever
import com.google.android.gms.auth.api.phone.SmsRetrieverData
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.common.api.Status

/**
 * ============================================================
 * SmsUserConsentManager — SMS User Consent 核心管理类
 * ================================================================
 * Manages SMS User Consent flow for OTP extraction.
 * 管理 SMS User Consent 流程用于 OTP 提取。
 *
 * This API is used when:
 *   1. SMS Retriever is not available on the device
 *   2. The SMS message format cannot be changed (e.g., from a third-party SMS)
 *   3. You need to read SMS from any sender
 *
 * @param activity The activity that will receive the consent intent
 * @param listener Callback for consent and OTP results
 */
class SmsUserConsentManager(
    private val activity: Activity,
    private val listener: UserConsentListener
) {
    companion object {
        private const val TAG = "SmsUserConsent"
        private const val CONSENT_REQUEST_CODE = 2001  // Unique request code
    }

    /**
     * User Consent listener interface
     * User Consent 监听器接口
     */
    interface UserConsentListener {
        /**
         * Called when user grants consent to read SMS
         * 用户同意读取 SMS 时调用
         * @param phoneNumber Sender's phone number (may be masked/null)
         */
        fun onConsentGranted(phoneNumber: String?)

        /**
         * Called when user denies consent
         * 用户拒绝同意时调用
         */
        fun onConsentDenied()

        /**
         * Called when OTP is successfully retrieved after consent
         * 同意后 OTP 提取成功时调用
         * @param otp The extracted OTP code
         */
        fun onOtpRetrieved(otp: String)

        /**
         * Called when SMS User Consent times out (5 minutes)
         * SMS User Consent 超时时调用
         */
        fun onTimeout()

        /**
         * Called when an error occurs
         * 发生错误时调用
         * @param error Error message
         */
        fun onError(error: String)
    }

    private val smsConsentingClient: SmsConsentingClient by lazy {
        SmsRetriever.getConsentingClient(activity)
    }

    private var isListening = false

    /**
     * Start SMS User Consent flow
     * 启动 SMS User Consent 流程
     *
     * This launches a system consent dialog asking the user to grant
     * permission to read the incoming SMS. After consent, the BroadcastReceiver
     * will receive the SMS content.
     *
     * Call this when the user taps "Read SMS" button.
     * 在用户点击"读取短信"按钮时调用此方法。
     */
    fun startConsentFlow() {
        Log.d(TAG, "Starting SMS User Consent flow...")

        val task = smsConsentingClient.startSmsUserConsent(null)

        task.addOnSuccessListener { pendingIntent ->
            Log.d(TAG, "Consent flow started, waiting for user...")
            // The system will show a consent dialog
            // 系统将显示同意对话框
            listener.onConsentGranted(null)
        }

        task.addOnFailureListener { e ->
            Log.e(TAG, "Failed to start consent flow", e)
            listener.onError("Failed to start: ${e.message}")
        }
    }

    /**
     * Start SMS User Consent with specific sender phone number
     * 使用指定发送者电话号码启动 SMS User Consent
     *
     * Use this overload to limit consent to SMS from a specific number.
     * Use null to allow SMS from any number.
     *
     * @param senderPhoneNumber The sender's phone number, or null for any
     */
    fun startConsentFlow(senderPhoneNumber: String?) {
        Log.d(TAG, "Starting SMS User Consent flow for: $senderPhoneNumber")

        val task = if (senderPhoneNumber != null) {
            smsConsentingClient.startSmsUserConsent(senderPhoneNumber)
        } else {
            smsConsentingClient.startSmsUserConsent(null)
        }

        task.addOnSuccessListener {
            Log.d(TAG, "Consent flow started")
            listener.onConsentGranted(senderPhoneNumber)
        }

        task.addOnFailureListener { e ->
            Log.e(TAG, "Failed to start consent flow", e)
            listener.onError("Failed to start: ${e.message}")
        }
    }

    /**
     * Register BroadcastReceiver for SMS User Consent
     * 注册 SMS User Consent 广播接收器
     *
     * Call this in onResume() to receive SMS after consent is granted.
     * Call unregisterSmsReceiver() in onPause().
     */
    @OptIn(androidx.annotation.OptIn::class)
    fun registerSmsReceiver() {
        if (isListening) return

        val filter = IntentFilter().apply {
            addAction(SmsRetriever.SMS_RETRIEVED_ACTION)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            activity.registerReceiver(
                smsReceiver,
                filter,
                Context.RECEIVER_NOT_EXPORTED
            )
        } else {
            activity.registerReceiver(smsReceiver, filter)
        }

        isListening = true
        Log.d(TAG, "SMS receiver registered")
    }

    /**
     * Unregister BroadcastReceiver
     * 注销广播接收器
     */
    fun unregisterSmsReceiver() {
        if (!isListening) return

        try {
            activity.unregisterReceiver(smsReceiver)
            isListening = false
            Log.d(TAG, "SMS receiver unregistered")
        } catch (e: IllegalArgumentException) {
            // Receiver was not registered
        }
    }

    /**
     * Handle activity result for consent
     * 处理活动结果以确认同意
     *
     * Call this in onActivityResult():
     *
     * ```kotlin
     * override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
     *     super.onActivityResult(requestCode, resultCode, data)
     *     if (requestCode == 2001) {
     *         if (resultCode == Activity.RESULT_OK) {
     *             // User granted consent
     *             smsUserConsentManager.registerSmsReceiver()
     *         } else {
     *             // User denied consent
     *             listener.onConsentDenied()
     *         }
     *     }
     * }
     * ```
     */
    fun onActivityResult(requestCode: Int, resultCode: Int) {
        if (requestCode == CONSENT_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                Log.d(TAG, "User granted consent")
                listener.onConsentGranted(null)
                registerSmsReceiver()
            } else {
                Log.d(TAG, "User denied consent")
                listener.onConsentDenied()
            }
        }
    }

    /**
     * SMS BroadcastReceiver for User Consent
     * User Consent 的 SMS 广播接收器
     */
    private val smsReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action != SmsRetriever.SMS_RETRIEVED_ACTION) {
                return
            }

            val extras = intent.extras ?: return
            val smsRetrieverStatus = extras.get(SmsRetriever.EXTRA_STATUS) as? Status

            when (smsRetrieverStatus?.statusCode) {
                CommonStatusCodes.SUCCESS -> {
                    val smsMessage = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        extras.get(SmsRetriever.EXTRA_SMS_MESSAGE, SmsRetrieverData::class.java)
                    } else {
                        @Suppress("DEPRECATION")
                        extras.get(SmsRetriever.EXTRA_SMS_MESSAGE) as? String
                    }

                    if (smsMessage != null) {
                        val otp = extractOtp(smsMessage)
                        if (otp != null) {
                            Log.d(TAG, "OTP extracted: ${otp.substring(0, 2)}**")
                            listener.onOtpRetrieved(otp)
                        } else {
                            Log.w(TAG, "No OTP found")
                            listener.onError("No OTP found in message")
                        }
                    }

                    // Unregister after successful retrieval
                    // 提取成功后注销
                    unregisterSmsReceiver()
                }

                CommonStatusCodes.TIMEOUT -> {
                    Log.d(TAG, "Consent timed out")
                    listener.onTimeout()
                    unregisterSmsReceiver()
                }

                else -> {
                    Log.e(TAG, "SMS consent error: ${smsRetrieverStatus?.statusCode}")
                    listener.onError("SMS consent failed")
                }
            }
        }

        /**
         * Extract OTP from SMS message
         * 从 SMS 消息中提取 OTP
         */
        private fun extractOtp(message: String): String? {
            // Pattern: 4-8 consecutive digits
            val digitPattern = Regex("\\b(\\d{4,8})\\b")
            return digitPattern.find(message)?.groupValues?.get(1)
        }
    }
}

/**
 * ============================================================
 * LifecycleObserver Implementation — 生命周期观察者实现
 * ================================================================
 * Helper class to manage consent flow with Activity lifecycle.
 */
class SmsUserConsentLifecycleObserver(
    private val consentManager: SmsUserConsentManager
) : DefaultLifecycleObserver {

    override fun onResume(owner: LifecycleOwner) {
        super.onResume(owner)
        // Auto-register when activity resumes
        consentManager.registerSmsReceiver()
    }

    override fun onPause(owner: LifecycleOwner) {
        super.onPause(owner)
        // Auto-unregister when activity pauses
        consentManager.unregisterSmsReceiver()
    }
}

/**
 * ============================================================
 * Usage Example — 使用示例
 * ================================================================
 *
 * ```kotlin
 * class OtpVerificationActivity : AppCompatActivity(), SmsUserConsentManager.UserConsentListener {
 *
 *     private lateinit var consentManager: SmsUserConsentManager
 *
 *     override fun onCreate(savedInstanceState: Bundle?) {
 *         super.onCreate(savedInstanceState)
 *         setContentView(R.layout.activity_otp_verification)
 *
 *         consentManager = SmsUserConsentManager(this, this)
 *
 *         // Optional: Add lifecycle observer for automatic registration
 *         lifecycle.addObserver(SmsUserConsentLifecycleObserver(consentManager))
 *
 *         readSmsButton.setOnClickListener {
 *             // Start consent flow when user taps button
 *             consentManager.startConsentFlow()
 *         }
 *     }
 *
 *     override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
 *         super.onActivityResult(requestCode, resultCode, data)
 *         consentManager.onActivityResult(requestCode, resultCode)
 *     }
 *
 *     // UserConsentListener callbacks
 *
 *     override fun onConsentGranted(phoneNumber: String?) {
 *         Log.d(TAG, "User granted consent to read SMS")
 *         // If using manual registration instead of lifecycle observer
 *         // consentManager.registerSmsReceiver()
 *     }
 *
 *     override fun onConsentDenied() {
 *         showError("Please enter OTP manually")
 *     }
 *
 *     override fun onOtpRetrieved(otp: String) {
 *         otpEditText.setText(otp)
 *         verifyOtp()
 *     }
 *
 *     override fun onTimeout() {
 *         showError("SMS consent timed out. Please try again or enter manually.")
 *     }
 *
 *     override fun onError(error: String) {
 *         showError(error)
 *     }
 * }
 * ```
 *
 * ============================================================
 * Fallback Strategy — 降级策略
 * ================================================================
 *
 * When both SMS Retriever and User Consent fail, implement fallback:
 *
 * ```kotlin
 * private fun handleOtpFallback() {
 *     // Show fallback UI with manual OTP entry
 *     showManualOtpEntry()
 *
 *     // Provide alternative verification methods
 *     showAlternatives(
 *         "Resend via Email",
 *         "Resend via Call",
 *         "Use Backup Code"
 *     )
 * }
 * ```
 *
 * ============================================================
 * Comparison: Retriever vs User Consent — Retriever vs User Consent 对比
 * ================================================================
 *
 * | Feature          | SMS Retriever    | SMS User Consent |
 * |------------------|------------------|------------------|
 * | User Experience  | Automatic read   | Manual confirm   |
 * | SMS Format       | Must include hash| Any format       |
 * | Timeout          | 5 minutes        | 5 minutes        |
 * | Sender Control   | Any              | Specific/Any     |
 * | API Requirement  | Google Play svcs  | Google Play svcs |
 * | Permission       | None required    | None required    |
 *
 * Recommendation:
 *   - Use SMS Retriever when possible (better UX)
 *   - Use User Consent when SMS format cannot be controlled
 */
