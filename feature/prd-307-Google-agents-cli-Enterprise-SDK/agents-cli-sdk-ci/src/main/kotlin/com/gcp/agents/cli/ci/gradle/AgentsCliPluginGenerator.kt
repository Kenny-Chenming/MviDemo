package com.gcp.agents.cli.ci.gradle

/**
 * AgentsCliPluginGenerator — 生成 Gradle 插件代码
 * Generates Gradle plugin code for agents-cli SDK integration.
 *
 * Note: This is a code generator that outputs plugin source code.
 * The actual plugin implementation requires Gradle API dependency.
 */
class AgentsCliPluginGenerator {

    /**
     * 生成 Gradle build.gradle.kts 片段 / Generate Gradle build.gradle.kts snippet
     */
    fun generateBuildScriptSnippet(): String {
        return """
            // agents-cli SDK integration
            plugins {
                id("com.gcp.agents.cli-sdk") version "1.0.0"
            }

            agentsCli {
                projectId.set("my-gcp-project")
                region.set("us-central1")

                agent("my-agent") {
                    source.set("github:owner/repo:main")
                    target.set("vertex-ai")
                }
            }
        """.trimIndent()
    }

    /**
     * 生成 settings.gradle.kts 片段 / Generate settings.gradle.kts snippet
     */
    fun generateSettingsSnippet(): String {
        return """
            pluginManagement {
                repositories {
                    google()
                    mavenCentral()
                    gradlePluginPortal()
                }
            }

            plugins {
                id("com.gcp.agents.cli-sdk") version "1.0.0"
            }
        """.trimIndent()
    }

    /**
     * 生成 plugins.gradle.kts / Generate plugins.gradle.kts
     */
    fun generatePluginsDotGradleKts(): String {
        return """
            plugins {
                id("com.gcp.agents.cli-sdk") version "1.0.0"
            }
        """.trimIndent()
    }
}
