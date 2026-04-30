// ================================================================
// SmsRetrieverTemplate — SMS Retriever API 集成模板
// ================================================================
// Complete integration template for SMS Retriever API.
//
// PRD-186: Android 17 SMS OTP Delay 合规检测与迁移工具包
// Design Reference: memory/agency/designs/PRD-186-SMS-OTP-Delay-合规检测与迁移工具包.md
//
// SMS Retriever API 是 Google 推荐的 OTP 读取方案:
//   - 用户无感知，无需手动输入
//   - 不受 Android 17 的 3 小时延迟限制
//   - 需要在服务端 SMS 消息中包含 hash
//
// Prerequisites / 前置条件:
//   1. 配置 google-services.json（从 Firebase Console 下载）
//   2. 在 build.gradle 中应用 google-services 插件
//   3. 生成 SMS Retriever hash 并添加到服务端
//
// @see SmsUserConsentTemplate for User Consent API alternative
// ================================================================

package com.mvi.kenny.otpdelay.templates

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.google.android.gms.auth.api.phone.SmsRetriever
import com.google.android.gms.auth.api.phone.SmsRetrieverClient
import com.google.android.gms.auth.api.phone.SmsRetrieverData
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.common.api.Status

/**
 * ============================================================
 * SmsRetrieverManager — SMS Retriever 核心管理类
 * ================================================================
 * Manages the SMS Retriever lifecycle and OTP extraction.
 * 管理 SMS Retriever 生命周期和 OTP 提取。
 *
 * @param context Application context
 * @param listener Callback for OTP extraction results
 */
class SmsRetrieverManager(
    private val context: Context,
    private val listener: OtpRetrieverListener
) {
    companion object {
        private const val TAG = "SmsRetrieverManager"
    }

    /**
     * OTP Retriever listener interface
     * OTP 提取监听器接口
     */
    interface OtpRetrieverListener {
        /**
         * Called when OTP is successfully retrieved
         * OTP 提取成功时调用
         * @param otp The extracted OTP code (typically 4-8 digits)
         */
        fun onOtpRetrieved(otp: String)

        /**
         * Called when SMS Retriever times out (5 minutes)
         * SMS Retriever 超时时调用（5分钟）
         */
        fun onTimeout()

        /**
         * Called when an error occurs
         * 发生错误时调用
         * @param error Error message
         */
        fun onError(error: String)
    }

    private val smsRetrieverClient: SmsRetrieverClient by lazy {
        SmsRetriever.getClient(context)
    }

    /**
     * Start SMS Retriever
     * 启动 SMS Retriever
     *
     * This initiates listening for SMS messages from Google Play services.
     * The system will wait up to 5 minutes for a matching SMS.
     *
     * Call this when the user navigates to the OTP verification screen.
     * 在用户进入 OTP 验证屏幕时调用此方法。
     */
    fun startRetriever() {
        Log.d(TAG, "Starting SMS Retriever...")

        // Start SMS Retriever intent
        // 启动 SMS Retriever 意图
        val task = smsRetrieverClient.startSmsRetriever()

        task.addOnSuccessListener { isRetrieverAvailable ->
            if (isRetrieverAvailable) {
                Log.d(TAG, "SMS Retriever started successfully")
                // Listen for the OTP SMS
                // 监听 OTP SMS
                registerSmsBroadcastReceiver()
            } else {
                Log.e(TAG, "SMS Retriever not available on this device")
                listener.onError("SMS Retriever not available")
            }
        }

        task.addOnFailureListener { e ->
            Log.e(TAG, "Failed to start SMS Retriever", e)
            listener.onError("Failed to start: ${e.message}")
        }
    }

    /**
     * Stop SMS Retriever
     * 停止 SMS Retriever
     *
     * Call this when the user leaves the OTP screen or completes verification.
     * 在用户离开 OTP 屏幕或完成验证时调用。
     */
    fun stopRetriever() {
        Log.d(TAG, "Stopping SMS Retriever...")
        unregisterSmsBroadcastReceiver()
    }

    /**
     * Register BroadcastReceiver for SMS
     * 注册 SMS 广播接收器
     */
    private fun registerSmsBroadcastReceiver() {
        val filter = android.content.IntentFilter().apply {
            addAction(SmsRetriever.SMS_RETRIEVED_ACTION)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(
                smsReceiver,
                filter,
                Context.RECEIVER_NOT_EXPORTED
            )
        } else {
            context.registerReceiver(smsReceiver, filter)
        }
    }

    /**
     * Unregister BroadcastReceiver
     * 注销广播接收器
     */
    private fun unregisterSmsBroadcastReceiver() {
        try {
            context.unregisterReceiver(smsReceiver)
        } catch (e: IllegalArgumentException) {
            // Receiver was not registered, ignore
            // 接收器未注册，忽略
        }
    }

    /**
     * SMS BroadcastReceiver
     * SMS 广播接收器
     *
     * Receives the SMS_RETRIEVED_ACTION broadcast from Play services
     * containing the OTP message.
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
                    // Get SMS message
                    // 获取 SMS 消息
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
                            Log.w(TAG, "No OTP found in message")
                            listener.onError("No OTP found in message")
                        }
                    }
                }

                CommonStatusCodes.TIMEOUT -> {
                    Log.d(TAG, "SMS Retriever timed out")
                    listener.onTimeout()
                }

                else -> {
                    Log.e(TAG, "SMS Retriever error: ${smsRetrieverStatus?.statusCode}")
                    listener.onError("SMS Retriever failed")
                }
            }
        }

        /**
         * Extract OTP from SMS message
         * 从 SMS 消息中提取 OTP
         *
         * Supports common OTP formats:
         *   - 123456 (6 digits)
         *   - Your code is 123456
         *   - Your verification code is: 123456
         *   - 123-456 (with dash)
         *
         * @param message SMS message text
         * @return Extracted OTP or null if not found
         */
        private fun extractOtp(message: String): String? {
            // Pattern 1: 4-8 consecutive digits (most common)
            // 模式 1: 4-8 位连续数字（最常见）
            val digitPattern = Regex("\\b(\\d{4,8})\\b")
            val match = digitPattern.find(message)
            if (match != null) {
                return match.groupValues[1]
            }

            // Pattern 2: OTP after keywords
            // 模式 2: 关键词后的 OTP
            val keywordPatterns = listOf(
                Regex("code[:\\s]*(\\d{4,8})", RegexOption.IGNORE_CASE),
                Regex("verification[:\\s]*(\\d{4,8})", RegexOption.IGNORE_CASE),
                Regex("otp[:\\s]*(\\d{4,8})", RegexOption.IGNORE_CASE)
            )

            for (pattern in keywordPatterns) {
                val keywordMatch = pattern.find(message)
                if (keywordMatch != null) {
                    return keywordMatch.groupValues[1]
                }
            }

            return null
        }
    }
}

/**
 * ============================================================
 * Usage Example — 使用示例
 * ================================================================
 *
 * ```kotlin
 * class OtpVerificationActivity : AppCompatActivity(), OtpRetrieverListener {
 *
 *     private lateinit var smsRetrieverManager: SmsRetrieverManager
 *
 *     override fun onCreate(savedInstanceState: Bundle?) {
 *         super.onCreate(savedInstanceState)
 *         setContentView(R.layout.activity_otp_verification)
 *
 *         smsRetrieverManager = SmsRetrieverManager(this, this)
 *     }
 *
 *     override fun onStart() {
 *         super.onStart()
 *         // Start listening for OTP SMS when screen is visible
 *         // 屏幕可见时开始监听 OTP SMS
 *         smsRetrieverManager.startRetriever()
 *     }
 *
 *     override fun onStop() {
 *         super.onStop()
 *         // Stop listening when screen is no longer visible
 *         // 屏幕不可见时停止监听
 *         smsRetrieverManager.stopRetriever()
 *     }
 *
 *     override fun onOtpRetrieved(otp: String) {
 *         // Auto-fill OTP and verify
 *         // 自动填写 OTP 并验证
 *         otpEditText.setText(otp)
 *         verifyOtp()
 *     }
 *
 *     override fun onTimeout() {
 *         showError("SMS Retriever timed out. Please try again.")
 *     }
 *
 *     override fun onError(error: String) {
 *         showError(error)
 *     }
 *
 *     private fun verifyOtp() {
 *         // Verify OTP with your backend
 *         // 使用 OTP 验证后端
 *     }
 * }
 * ```
 *
 * ============================================================
 * AndroidManifest.xml Requirements — AndroidManifest.xml 要求
 * ================================================================
 *
 * Add to AndroidManifest.xml (inside <application>):
 *
 * ```xml
 * <!-- SMS Retriever requires Google Play services -->
 * <meta-data
 *     android:name="com.google.android.gms.version"
 *     android:value="@integer/google_play_services_version" />
 *
 * <!-- If using SMS Retriever with other SMS permissions -->
 * <uses-permission android:name="android.permission.READ_SMS" />
 * ```
 *
 * Note: SMS Retriever API itself does NOT require READ_SMS permission.
 * It uses Google Play services to read SMS on behalf of your app.
 *
 * ============================================================
 * Server-Side SMS Format — 服务端 SMS 格式
 * ================================================================
 *
 * Your SMS message MUST include the app's hash signature:
 *
 * ```
 * Your verification code is 123456.
 * <##>app_signature_hash_here</##>
 * ```
 *
 * Replace `app_signature_hash_here` with the hash generated by:
 *   - Hash Generator tool (tools/hash-generator/)
 *   - Play Console App Signing page
 *   - Google's Python script (sms_retriever_hash_v2.py)
 */
