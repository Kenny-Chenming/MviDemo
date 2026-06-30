// ================================================================
// GradleConfigPatcher — Gradle 配置修补器
// Gradle Configuration Patcher
// ================================================================
// PRD-306: Android 17 后台音频 Foreground Service 迁移检测与适配工具包
//
// 修改 build.gradle 添加必要权限和服务声明。
// Modifies build.gradle to add necessary permissions and service declarations.
// ================================================================

package com.mvi.kenny.audiobackground.generator

import java.io.File

/**
 * Gradle 配置修补结果
 * Gradle Config Patch Result
 *
 * @param modifiedFiles 修改的文件列表
 * @param newPermissions 新增的权限列表
 * @param newServices 新增的 Service 声明列表
 */
data class GradleConfigPatchResult(
    val modifiedFiles: List<String>,
    val newPermissions: List<String>,
    val newServices: List<String>,
    val suggestions: List<String>
)

/**
 * Gradle 配置修补器
 * GradleConfigPatcher
 *
 * 自动修改 AndroidManifest.xml 和 build.gradle.kts 添加必要配置。
 * Automatically modifies AndroidManifest.xml and build.gradle.kts to add required config.
 */
class GradleConfigPatcher(
    private val packageName: String = "com.example.audioplayback"
) {

    companion object {
        /** 必须的权限列表 */
        val REQUIRED_PERMISSIONS = listOf(
            "android.permission.FOREGROUND_SERVICE",
            "android.permission.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK",
            "android.permission.POST_NOTIFICATIONS"  // Android 13+
        )

        /** Service 声明模板 */
        const val SERVICE_DECLARATION = """
            <!-- Audio Playback Foreground Service -->
            <service
                android:name=".$serviceName"
                android:enabled="true"
                android:exported="false"
                android:foregroundServiceType="mediaPlayback" />
"""
    }

    /**
     * 生成 AndroidManifest.xml 修改建议
     * Generate AndroidManifest.xml modification suggestions
     *
     * @param serviceName Service 名称（不含包名）
     * @return 建议添加的 XML 内容
     */
    fun generateManifestAdditions(serviceName: String = "AudioPlaybackService"): ManifestAdditions {
        val permissionsXml = REQUIRED_PERMISSIONS.joinToString("\n") { permission ->
            "    <uses-permission android:name=\"$permission\" />"
        }

        val serviceXml = SERVICE_DECLARATION.trimIndent().replace("\$serviceName", serviceName)

        return ManifestAdditions(
            permissions = permissionsXml,
            serviceDeclaration = serviceXml
        )
    }

    /**
     * 生成 build.gradle.kts 修改建议
     * Generate build.gradle.kts modification suggestions
     *
     * @return 建议添加的 build.gradle.kts 内容
     */
    fun generateBuildGradleAdditions(): BuildGradleAdditions {
        return BuildGradleAdditions(
            namespaceWarning = """
                // ⚠️ 如果在 app/build.gradle.kts 中使用 namespace，
                // 确保 Service 在正确的包名下声明。
                // 如果 namespace 与实际 Service 所在包不一致，
                // 可能需要添加 serviceClasses 属性。
            """.trimIndent(),
            
            minSdkRecommendation = """
                // 建议将 minSdk 提升到 26（Android 8.0）以获得最佳兼容性
                // minSdk = 26
            """.trimIndent(),
            
            compileSdkNote = """
                // compileSdk 应设置为 37 或更高以支持 Android 17
                // compileSdk = 37
            """.trimIndent()
        )
    }

    /**
     * 修补 AndroidManifest.xml 文件
     * Patch AndroidManifest.xml file
     *
     * @param manifestFile AndroidManifest.xml 文件
     * @param serviceName Service 名称
     * @return 是否成功修改
     */
    fun patchManifest(
        manifestFile: File,
        serviceName: String = "AudioPlaybackService"
    ): Boolean {
        if (!manifestFile.exists()) {
            return false
        }

        val content = manifestFile.readText()
        val additions = generateManifestAdditions(serviceName)

        val modifiedContent = buildString {
            // 1. 添加权限
            if (!content.contains("android.permission.FOREGROUND_SERVICE")) {
                appendln(content)
                appendln()
                appendln("<!-- PRD-306: Android 17 Audio Migration - Required permissions -->")
                appendln(additions.permissions)
            }

            // 2. 添加 Service 声明（在 </manifest> 前）
            if (!content.contains("android:foregroundServiceType=\"mediaPlayback\"")) {
                val insertBeforeManifestEnd = content.lastIndexOf("</manifest>")
                if (insertBeforeManifestEnd > 0) {
                    val before = content.substring(0, insertBeforeManifestEnd)
                    val after = content.substring(insertBeforeManifestEnd)
                    append(before)
                    appendln()
                    appendln("    <!-- PRD-306: Android 17 Audio Migration - Audio Playback Service -->")
                    append(additions.serviceDeclaration.prependIndent("    "))
                    append(after)
                }
            }
        }

        return try {
            manifestFile.writeText(modifiedContent)
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * 生成完整的迁移报告
     * Generate complete migration report
     *
     * @param moduleName 模块名
     * @param violations 违规列表
     * @return 迁移报告
     */
    fun generateMigrationReport(
        moduleName: String,
        violations: List<String>
    ): MigrationReport {
        val manifestAdditions = generateManifestAdditions()
        val buildGradleAdditions = generateBuildGradleAdditions()

        return MigrationReport(
            moduleName = moduleName,
            filesToModify = listOf(
                "app/src/main/AndroidManifest.xml",
                "app/build.gradle.kts"
            ),
            manifestChanges = listOf(
                "添加 FOREGROUND_SERVICE 权限",
                "添加 FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK 权限（Android 10+）",
                "添加 POST_NOTIFICATIONS 权限（Android 13+）",
                "声明 AudioPlaybackService"
            ),
            gradleChanges = listOf(
                "确保 compileSdk >= 37",
                "建议 minSdk >= 26"
            ),
            permissionsToAdd = REQUIRED_PERMISSIONS,
            servicesToAdd = listOf("AudioPlaybackService"),
            violationsToFix = violations,
            migrationSteps = listOf(
                "1. 复制以下权限到 AndroidManifest.xml 的 <manifest> 标签内：",
                manifestAdditions.permissions,
                "",
                "2. 复制以下 Service 声明到 AndroidManifest.xml 的 <application> 标签内：",
                manifestAdditions.serviceDeclaration,
                "",
                "3. 将生成的 AudioPlaybackService.kt 放入 src/main/kotlin/ 目录",
                "",
                "4. 将生成的 AudioFocusManager.kt 放入 src/main/kotlin/ 目录",
                "",
                "5. 修改原有音频播放代码，通过 Service 或 AudioFocusManager 管理"
            ),
            buildGradleSuggestions = buildGradleAdditions
        )
    }
}

/**
 * Manifest 添加内容
 * Manifest Additions
 */
data class ManifestAdditions(
    val permissions: String,
    val serviceDeclaration: String
)

/**
 * build.gradle 添加内容
 * build.gradle Additions
 */
data class BuildGradleAdditions(
    val namespaceWarning: String,
    val minSdkRecommendation: String,
    val compileSdkNote: String
)

/**
 * 迁移报告
 * Migration Report
 */
data class MigrationReport(
    val moduleName: String,
    val filesToModify: List<String>,
    val manifestChanges: List<String>,
    val gradleChanges: List<String>,
    val permissionsToAdd: List<String>,
    val servicesToAdd: List<String>,
    val violationsToFix: List<String>,
    val migrationSteps: List<String>,
    val buildGradleSuggestions: BuildGradleAdditions
)
