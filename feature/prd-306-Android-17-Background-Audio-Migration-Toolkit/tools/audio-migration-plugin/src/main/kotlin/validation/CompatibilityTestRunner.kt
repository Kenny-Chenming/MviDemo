// ================================================================
// CompatibilityTestRunner — 兼容性测试运行器
// Compatibility Test Runner
// ================================================================
// PRD-306: Android 17 后台音频 Foreground Service 迁移检测与适配工具包
//
// 运行兼容性测试用例并生成报告。
// Runs compatibility test cases and generates reports.
// ================================================================

package com.mvi.kenny.audiobackground.validation

import com.mvi.kenny.audiobackground.engine.AudioApiSeverity
import com.mvi.kenny.audiobackground.engine.AudioViolation

/**
 * 测试用例
 * Test Case
 *
 * @param id 用例 ID
 * @param name 用例名称
 * @param description 描述
 * @param context 调用上下文
 * @param expectedAllowed 预期允许的 API 列表
 * @param severity 相关严重性
 */
data class CompatibilityTestCase(
    val id: String,
    val name: String,
    val description: String,
    val context: CallContext,
    val expectedAllowed: List<AudioApiType>,
    val severity: AudioApiSeverity = AudioApiSeverity.CRITICAL
)

/**
 * 单个测试结果
 * Single Test Result
 *
 * @param testCase 测试用例
 * @param passed 是否通过
 * @param actualAllowed 实际允许的 API
 * @param actualBlocked 实际被拦截的 API
 * @param errorMessage 错误信息（如失败）
 */
data class TestResult(
    val testCase: CompatibilityTestCase,
    val passed: Boolean,
    val actualAllowed: List<AudioApiType>,
    val actualBlocked: List<AudioApiType>,
    val errorMessage: String? = null
)

/**
 * 兼容性测试套件结果
 * Compatibility Test Suite Result
 *
 * @param totalTests 总测试数
 * @param passedTests 通过的测试数
 * @param failedTests 失败的测试数
 * @param testResults 所有测试结果
 * @param overallScore 总体合规分数
 */
data class CompatibilityTestSuiteResult(
    val totalTests: Int,
    val passedTests: Int,
    val failedTests: Int,
    val testResults: List<TestResult>,
    val overallScore: Int  // 0-100
) {
    val isPassed: Boolean get() = failedTests == 0
}

/**
 * 兼容性测试运行器
 * CompatibilityTestRunner
 *
 * 运行预定义测试用例，验证迁移效果。
 * Runs predefined test cases to verify migration effectiveness.
 */
class CompatibilityTestRunner {

    private val interceptor = Android17AudioInterceptor()

    /**
     * 运行默认测试套件
     * Run default test suite
     *
     * 测试常见的音频使用场景。
     * Tests common audio usage scenarios.
     *
     * @return 测试套件结果
     */
    fun runDefaultTestSuite(): CompatibilityTestSuiteResult {
        val testCases = getDefaultTestCases()
        return runTests(testCases)
    }

    /**
     * 运行自定义测试用例
     * Run custom test cases
     *
     * @param testCases 测试用例列表
     * @return 测试套件结果
     */
    fun runTests(testCases: List<CompatibilityTestCase>): CompatibilityTestSuiteResult {
        val results = testCases.map { testCase ->
            runSingleTest(testCase)
        }

        val passed = results.count { it.passed }
        val failed = results.count { !it.passed }

        // 计算总体分数（基于测试通过率和 API 覆盖）
        val overallScore = calculateOverallScore(results, testCases)

        return CompatibilityTestSuiteResult(
            totalTests = testCases.size,
            passedTests = passed,
            failedTests = failed,
            testResults = results,
            overallScore = overallScore
        )
    }

    /**
     * 运行单个测试
     * Run single test
     */
    private fun runSingleTest(testCase: CompatibilityTestCase): TestResult {
        val allApis = AudioApiType.entries.toList()
        val checkResults = interceptor.checkMultiple(allApis, testCase.context)

        val actualAllowed = checkResults
            .filter { !it.second.wasBlocked }
            .map { it.first }

        val actualBlocked = checkResults
            .filter { it.second.wasBlocked }
            .map { it.first }

        // 检查预期允许的 API 是否都被允许
        val expectedAllowedSet = testCase.expectedAllowed.toSet()
        val actualAllowedSet = actualAllowed.toSet()
        
        val passed = expectedAllowedSet == actualAllowedSet

        val errorMessage = if (!passed) {
            val missingAllowed = expectedAllowedSet - actualAllowedSet
            val unexpectedBlocked = missingAllowed.map { api ->
                val result = checkResults.find { it.first == api }?.second
                "$api: ${result?.reason ?: "Unknown"}"
            }
            "Expected allowed: $expectedAllowedSet, Actual blocked: $missingBlocked"
        } else null

        return TestResult(
            testCase = testCase,
            passed = passed,
            actualAllowed = actualAllowed,
            actualBlocked = actualBlocked,
            errorMessage = errorMessage
        )
    }

    /**
     * 获取默认测试用例
     * Get default test cases
     */
    private fun getDefaultTestCases(): List<CompatibilityTestCase> = listOf(
        // ========== WIU Service 场景 ==========
        CompatibilityTestCase(
            id = "TC-001",
            name = "WIU Service - MediaPlayer.play()",
            description = "在 WIU Foreground Service 中调用 MediaPlayer.start()",
            context = CallContext(
                hasVisibleActivity = false,
                hasWiuService = true,
                isInForegroundProcess = false,
                apiLevel = 17
            ),
            expectedAllowed = listOf(
                AudioApiType.MEDIA_PLAYER_START,
                AudioApiType.MEDIA_PLAYER_PAUSE,
                AudioApiType.MEDIA_PLAYER_STOP,
                AudioApiType.AUDIO_MANAGER_REQUEST_FOCUS
            ),
            severity = AudioApiSeverity.CRITICAL
        ),

        CompatibilityTestCase(
            id = "TC-002",
            name = "WIU Service - ExoPlayer.play()",
            description = "在 WIU Foreground Service 中调用 ExoPlayer.play()",
            context = CallContext(
                hasVisibleActivity = false,
                hasWiuService = true,
                isInForegroundProcess = false,
                apiLevel = 17
            ),
            expectedAllowed = listOf(
                AudioApiType.EXO_PLAYER_PLAY,
                AudioApiType.AUDIO_MANAGER_REQUEST_FOCUS
            ),
            severity = AudioApiSeverity.CRITICAL
        ),

        // ========== 可见 Activity 场景 ==========
        CompatibilityTestCase(
            id = "TC-003",
            name = "Visible Activity - MediaPlayer.play()",
            description = "在可见 Activity 中调用 MediaPlayer.start()",
            context = CallContext(
                hasVisibleActivity = true,
                hasWiuService = false,
                isInForegroundProcess = false,
                apiLevel = 17
            ),
            expectedAllowed = listOf(
                AudioApiType.MEDIA_PLAYER_START,
                AudioApiType.MEDIA_PLAYER_PAUSE
            ),
            severity = AudioApiSeverity.INFO
        ),

        // ========== 后台场景（应被拦截） ==========
        CompatibilityTestCase(
            id = "TC-004",
            name = "Background - MediaPlayer.play() should be BLOCKED",
            description = "在后台（无 WIU Service）调用 MediaPlayer.start() 应被拦截",
            context = CallContext(
                hasVisibleActivity = false,
                hasWiuService = false,
                isInForegroundProcess = false,
                apiLevel = 17
            ),
            expectedAllowed = emptyList(),  // 应该全部被拦截
            severity = AudioApiSeverity.CRITICAL
        ),

        // ========== AudioFocus 场景 ==========
        CompatibilityTestCase(
            id = "TC-005",
            name = "WIU Service - AudioFocus management",
            description = "在 WIU Service 中管理 AudioFocus",
            context = CallContext(
                hasVisibleActivity = false,
                hasWiuService = true,
                isInForegroundProcess = false,
                apiLevel = 17
            ),
            expectedAllowed = listOf(
                AudioApiType.AUDIO_MANAGER_REQUEST_FOCUS,
                AudioApiType.AUDIO_MANAGER_ABANDON_FOCUS
            ),
            severity = AudioApiSeverity.CRITICAL
        ),

        // ========== 前景进程场景 ==========
        CompatibilityTestCase(
            id = "TC-006",
            name = "Foreground Process - Any audio API",
            description = "在前景进程中的音频 API 调用应被允许",
            context = CallContext(
                hasVisibleActivity = false,
                hasWiuService = false,
                isInForegroundProcess = true,
                apiLevel = 17
            ),
            expectedAllowed = AudioApiType.entries.toList(),
            severity = AudioApiSeverity.INFO
        ),

        // ========== Android 17 以下（不拦截） ==========
        CompatibilityTestCase(
            id = "TC-007",
            name = "API 16 - Background audio should be allowed",
            description = "Android 16（API 16）后台音频不应被拦截",
            context = CallContext(
                hasVisibleActivity = false,
                hasWiuService = false,
                isInForegroundProcess = false,
                apiLevel = 16
            ),
            expectedAllowed = AudioApiType.entries.toList(),  // 全部允许
            severity = AudioApiSeverity.INFO
        )
    )

    /**
     * 计算总体分数
     * Calculate overall score
     */
    private fun calculateOverallScore(
        results: List<TestResult>,
        testCases: List<CompatibilityTestCase>
    ): Int {
        if (testCases.isEmpty()) return 100

        // 基础分：测试通过率
        val passRate = (results.count { it.passed } * 100) / results.size

        // 考虑 CRITICAL 用例的权重
        val criticalTests = testCases.filter { it.severity == AudioApiSeverity.CRITICAL }
        val criticalPassed = results.count { r ->
            r.passed && r.testCase.severity == AudioApiSeverity.CRITICAL
        }
        val criticalScore = if (criticalTests.isNotEmpty()) {
            (criticalPassed * 100) / criticalTests.size
        } else 100

        // 加权平均：60% 测试通过率 + 40% CRITICAL 用例通过率
        return (passRate * 60 + criticalScore * 40) / 100
    }

    /**
     * 生成 HTML 报告
     * Generate HTML report
     */
    fun generateHtmlReport(result: CompatibilityTestSuiteResult): String {
        val scoreColor = when {
            result.overallScore >= 80 -> "#3fb950"  // 绿色
            result.overallScore >= 60 -> "#ffd700"  // 黄色
            else -> "#f85149"  // 红色
        }

        val rows = result.testResults.joinToString("\n") { tr ->
            val statusColor = if (tr.passed) "#3fb950" else "#f85149"
            val statusText = if (tr.passed) "✅ PASS" else "❌ FAIL"
            
            val blockedApis = tr.actualBlocked.joinToString(", ") { it.name }
                .ifEmpty { "None" }
            
            val allowedApis = tr.actualAllowed.joinToString(", ") { it.name }
                .ifEmpty { "None" }

            """
            <tr>
                <td>${tr.testCase.id}</td>
                <td>${tr.testCase.name}</td>
                <td style="color: ${statusColor}">$statusText</td>
                <td>${tr.testCase.severity.name}</td>
                <td><code>$blockedApis</code></td>
                <td><code>$allowedApis</code></td>
                <td>${tr.errorMessage ?: "-"}</td>
            </tr>
            """.trimIndent()
        }

        return """
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Audio Compatibility Test Report</title>
    <style>
        body { font-family: -apple-system, BlinkMacSystemFont, sans-serif; margin: 40px; background: #1e1e1e; color: #e0e0e0; }
        h1 { color: #ffffff; border-bottom: 2px solid #3fb950; padding-bottom: 10px; }
        .summary { background: #2d2d2d; padding: 20px; border-radius: 8px; margin-bottom: 20px; }
        .score { font-size: 48px; font-weight: bold; color: ${scoreColor}; }
        .passed { color: #3fb950; }
        .failed { color: #f85149; }
        table { border-collapse: collapse; width: 100%; margin-top: 20px; }
        th, td { border: 1px solid #3d3d3d; padding: 12px; text-align: left; }
        th { background: #2d2d2d; color: #ffffff; }
        tr:nth-child(even) { background: #252525; }
        code { background: #333; padding: 2px 6px; border-radius: 4px; font-size: 12px; }
    </style>
</head>
<body>
    <h1>🔊 Android 17 Audio Compatibility Test Report</h1>
    
    <div class="summary">
        <div class="score">${result.overallScore}%</div>
        <div>Overall Compliance Score</div>
        <br>
        <span class="passed">✅ Passed: ${result.passedTests}</span> | 
        <span class="failed">❌ Failed: ${result.failedTests}</span> | 
        Total: ${result.totalTests}
    </div>
    
    <h2>Test Results</h2>
    <table>
        <tr>
            <th>ID</th>
            <th>Test Case</th>
            <th>Status</th>
            <th>Severity</th>
            <th>Blocked APIs</th>
            <th>Allowed APIs</th>
            <th>Error</th>
        </tr>
        $rows
    </table>
    
    <footer style="margin-top: 40px; color: #888; font-size: 12px;">
        Generated by PRD-306 Audio Migration Toolkit | Android 17 Compatibility Test
    </footer>
</body>
</html>
        """.trimIndent()
    }
}
