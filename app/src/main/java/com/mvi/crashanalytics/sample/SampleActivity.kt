package com.mvi.crashanalytics.sample

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.mvi.crashanalytics.CrashAnalyticsEngine
import com.mvi.crashanalytics.CrashReport
import com.mvi.crashanalytics.R
import com.mvi.crashanalytics.analyzer.RootCauseAnalysis

/**
 * Sample usage demonstration
 */
class SampleActivity : AppCompatActivity() {

    private lateinit var engine: CrashAnalyticsEngine
    private lateinit var outputView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sample)

        engine = CrashAnalyticsEngine()
        outputView = findViewById(R.id.outputView)

        findViewById<Button>(R.id.btnTestNullPointer)?.setOnClickListener {
            testNullPointer()
        }

        findViewById<Button>(R.id.btnTestIllegalState)?.setOnClickListener {
            testIllegalState()
        }

        findViewById<Button>(R.id.btnTestThread)?.setOnClickListener {
            testThreadIssue()
        }
    }

    private fun testNullPointer() {
        val sampleLog = """
            java.lang.NullPointerException: Attempt to invoke virtual method 'int java.lang.String.length()' on a null object reference
                at com.example.app.MainActivity.onCreate(MainActivity.kt:42)
                at android.app.Activity.performCreate(Activity.java:7892)
                at android.app.ActivityThread.performLaunchActivity(ActivityThread.java:3320)
                at android.app.ActivityThread.handleLaunchActivity(ActivityThread.java:3514)
                at android.app.ActivityThread.access$1300(ActivityThread.java:267)
                at android.app.ActivityThread$H.handleMessage(ActivityThread.java:1947)
                at android.os.Handler.dispatchMessage(Handler.java:100)
                at android.os.Looper.loop(Looper.java:238)
                at android.app.ActivityThread.main(ActivityThread.java:7725)
                at java.lang.reflect.Method.invoke(Method.java:538)
                at com.android.internal.os.ZygoteInit$MethodAndArgsCaller.run(ZygoteInit.java:1134)
                at com.android.internal.os.ZygoteInit.main(ZygoteInit.java:1002)
        """.trimIndent()

        analyze(sampleLog)
    }

    private fun testIllegalState() {
        val sampleLog = """
            java.lang.IllegalStateException: Cannot perform this action after onSaveInstanceState
                at androidx.fragment.app.FragmentManager.checkStateLoss(FragmentManager.java:14)
                at androidx.fragment.app.FragmentManager.enqueueAction(FragmentManager.java:19)
                at androidx.fragment.app.BackStackRecord.commitInternal(BackStackRecord.java:30)
                at androidx.fragment.app.BackStackRecord.commit(BackStackRecord.java:26)
                at com.example.app.MainActivity.onStop(MainActivity.kt:78)
                at android.app.Instrumentation.callActivityOnStop(Instrumentation.java:1371)
                at android.app.Activity.performStop(Activity.java:7581)
                at android.app.ActivityThread.performPauseActivityIfNeeded(ActivityThread.java:4423)
        """.trimIndent()

        analyze(sampleLog)
    }

    private fun testThreadIssue() {
        val sampleLog = """
            android.os.NetworkOnMainThreadException
                at android.app.ActivityThread.performResumeActivity(ActivityThread.java:4268)
                at android.app.ActivityThread.handleResumeActivity(ActivityThread.java:4303)
                at android.app.ActivityThread$H.handleMessage(ActivityThread.java:2079)
                at android.os.Handler.dispatchMessage(Handler.java:111)
                at android.os.Looper.loop(Looper.java:212)
                at android.app.ActivityThread.main(ActivityThread.java:809)
                at java.lang.reflect.Method.invoke(Method.java:515)
                at com.android.internal.os.RuntimeInit$MethodAndArgsCaller.run(RuntimeInit.java:556)
                at com.android.internal.os.ZygoteInit.main(ZygoteInit.java:920)
        """.trimIndent()

        analyze(sampleLog)
    }

    private fun analyze(rawLog: String) {
        val deviceInfo = CrashReport.DeviceInfo(
            manufacturer = "Google",
            model = "Pixel 6",
            osVersion = "14",
            sdkVersion = 34,
            memoryAvailable = 4 * 1024 * 1024 * 1024L,
            isRooted = false
        )

        engine.processCrash(rawLog, deviceInfo, "1.0.0") { analysis, report ->
            val markdown = engine.generateMarkdownReport(report, analysis)
            outputView.text = markdown
        }
    }
}
