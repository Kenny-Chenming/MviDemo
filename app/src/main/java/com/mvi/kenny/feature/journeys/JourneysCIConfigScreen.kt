package com.mvi.kenny.feature.journeys

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// =============================================================
// JourneysCIConfigScreen — CI/CD 配置屏幕
// PRD-099 | Journeys for Android Studio 自动化 E2E 测试工具包
// =============================================================
/**
 * CI/CD Configuration Screen / CI/CD 配置屏幕
 *
 * Generate CI/CD YAML configuration for GitHub Actions, GitLab CI, or Jenkins.
 *
 * @param viewModel JourneysViewModel instance / JourneysViewModel 实例
 * @param onNavigateBack Callback to navigate back / 返回回调
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JourneysCIConfigScreen(
    viewModel: JourneysViewModel,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val clipboardManager = LocalClipboardManager.current
    val scrollState = rememberScrollState()

    val yamlContent = when (state.selectedCIPlatform) {
        CIPlatform.GITHUB_ACTIONS -> githubActionsYaml()
        CIPlatform.GITLAB_CI -> gitlabCiYaml()
        CIPlatform.JENKINS -> jenkinsfileYaml()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("CI/CD 集成配置", fontWeight = FontWeight.Medium) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back / 返回",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF6750A4),
                    titleContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFFAFAFA))
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // CI Platform Selection / CI 平台选择
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "选择 CI 平台 / Select CI Platform",
                        fontWeight = FontWeight.Medium,
                        fontSize = 16.sp,
                        color = Color(0xFF1F1F1F)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CIPlatform.entries.forEach { platform ->
                            FilterChip(
                                selected = state.selectedCIPlatform == platform,
                                onClick = {
                                    viewModel.processIntent(
                                        JourneysIntent.SelectCIPlatform(platform)
                                    )
                                },
                                label = { Text(platform.label) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFF6750A4),
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }
                }
            }

            // YAML Preview / YAML 预览
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    // Header / 头部
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = state.selectedCIPlatform.yamlFileName,
                                fontWeight = FontWeight.Medium,
                                fontSize = 14.sp,
                                color = Color(0xFF9E9E9E)
                            )
                            Text(
                                text = "${yamlContent.lines().size} lines / 行",
                                fontSize = 11.sp,
                                color = Color(0xFF5F5F5F)
                            )
                        }
                        Row {
                            IconButton(
                                onClick = { /* Download */ }
                            ) {
                                Icon(
                                    Icons.Default.Download,
                                    contentDescription = "Download / 下载",
                                    tint = Color(0xFF9E9E9E)
                                )
                            }
                            IconButton(
                                onClick = { /* Copy */ }
                            ) {
                                Icon(
                                    Icons.Default.ContentCopy,
                                    contentDescription = "Copy / 复制",
                                    tint = Color(0xFF9E9E9E)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // YAML content / YAML 内容
                    Text(
                        text = yamlContent,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        color = Color(0xFF4EC9B0)
                    )
                }
            }

            // Platform Info / 平台信息
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "集成说明 / Integration Guide",
                        fontWeight = FontWeight.Medium,
                        fontSize = 16.sp,
                        color = Color(0xFF1F1F1F)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    when (state.selectedCIPlatform) {
                        CIPlatform.GITHUB_ACTIONS -> GitHubActionsGuide()
                        CIPlatform.GITLAB_CI -> GitLabCIGuide()
                        CIPlatform.JENKINS -> JenkinsGuide()
                    }
                }
            }

            // Action buttons / 操作按钮
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { /* Copy */ },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF5F5F5F)
                    ),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Copy to Clipboard / 复制")
                }
                Button(
                    onClick = { /* Download */ },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6750A4)),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Download, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Download File / 下载")
                }
            }
        }
    }
}

// ================================================================
// CI Platform Guides — CI 平台指南
// ================================================================
@Composable
private fun GitHubActionsGuide() {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        GuideStep(
            number = "1",
            title = "创建工作流目录 / Create workflow directory",
            description = "在项目根目录创建 .github/workflows/ 目录（如果不存在）"
        )
        GuideStep(
            number = "2",
            title = "复制配置文件 / Copy configuration",
            description = "将左侧生成的 YAML 内容复制到 journeys.yml 文件"
        )
        GuideStep(
            number = "3",
            title = "提交并推送 / Commit and push",
            description = "git add . && git commit -m \"Add Journeys E2E workflow\" && git push"
        )
        GuideStep(
            number = "4",
            title = "查看 Actions / Check Actions",
            description = "在 GitHub 仓库的 Actions 标签页查看测试执行结果"
        )
    }
}

@Composable
private fun GitLabCIGuide() {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        GuideStep(
            number = "1",
            title = "复制配置文件 / Copy configuration",
            description = "将左侧生成的 YAML 内容复制到项目根目录的 .gitlab-ci.yml"
        )
        GuideStep(
            number = "2",
            title = "提交并推送 / Commit and push",
            description = "git add . && git commit -m \"Add Journeys E2E CI\" && git push"
        )
        GuideStep(
            number = "3",
            title = "查看 Pipeline / Check Pipeline",
            description = "在 GitLab 仓库的 CI/CD → Pipelines 查看测试执行"
        )
    }
}

@Composable
private fun JenkinsGuide() {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        GuideStep(
            number = "1",
            title = "创建 Pipeline / Create Pipeline",
            description = "在 Jenkins 中创建新 Pipeline 项目"
        )
        GuideStep(
            number = "2",
            title = "复制 Jenkinsfile / Copy Jenkinsfile",
            description = "将左侧生成的 Jenkinsfile 内容粘贴到 Pipeline script"
        )
        GuideStep(
            number = "3",
            title = "配置触发器 / Configure triggers",
            description = "设置 Webhook 或定期触发构建"
        )
        GuideStep(
            number = "4",
            title = "运行构建 / Run build",
            description = "手动触发或等待自动触发，查看控制台输出"
        )
    }
}

/**
 * Guide step item / 指南步骤项
 */
@Composable
private fun GuideStep(number: String, title: String, description: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .background(Color(0xFF6750A4), RoundedCornerShape(50))
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text(
                text = number,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = Color.White
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(
                text = title,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
                color = Color(0xFF1F1F1F)
            )
            Text(
                text = description,
                fontSize = 12.sp,
                color = Color(0xFF5F5F5F)
            )
        }
    }
}

// ================================================================
// YAML Templates — YAML 模板
// ================================================================
private fun githubActionsYaml(): String = """name: Journeys E2E Tests
on:
  push:
    branches: [main, develop]
  pull_request:
    branches: [main]

jobs:
  journey-tests:
    runs-on: ubuntu-latest
    timeout-minutes: 30
    
    steps:
      - name: Checkout code / 检出代码
        uses: actions/checkout@v4

      - name: Set up JDK 17 / 设置 JDK
        uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'temurin'

      - name: Cache Gradle packages / 缓存 Gradle
        uses: actions/cache@v4
        with:
          path: ~/.gradle/caches
          key: ${'$'}{{ runner.os }}-gradle-${'$'}{{ hashFiles('**/*.gradle*') }}

      - name: Grant execute permission / 授予执行权限
        run: chmod +x gradlew

      - name: Run Journeys E2E / 运行 Journey 测试
        run: ./gradlew runJourneys --no-daemon

      - name: Upload test results / 上传测试结果
        if: always()
        uses: actions/upload-artifact@v4
        with:
          name: journey-results
          path: app/build/journeys/results/
          retention-days: 30

      - name: Publish test report / 发布测试报告
        if: always()
        uses: actions/upload-artifact@v4
        with:
          name: journey-report
          path: app/build/journeys/report.html"""

private fun gitlabCiYaml(): String = """stages:
  - test

variables:
  GRADLE_OPTS: "-Dorg.gradle.daemon=false -Dorg.gradle.parallel=false"

journeys-e2e:
  stage: test
  image: ubuntu:22.04
  timeout: 30m
  
  before_script:
    - apt-get update -qq
    - apt-get install -y -qq openjdk-17-jdk > /dev/null 2>&1
    - export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
    - chmod +x gradlew
  
  script:
    - ./gradlew runJourneys --no-daemon --stacktrace
  
  artifacts:
    when: always()
    paths:
      - app/build/journeys/results/
      - app/build/journeys/report.html
    expire_in: 30 days
  
  coverage: '/(?i)(total.*?)(?<percentage>[0-9]{1,3})%/'
  
  rules:
    - if: "${'$'}CI_PIPELINE_SOURCE == \"merge_request_event\""
    - if: "${'$'}CI_COMMIT_BRANCH == \"main\""
    - if: "${'$'}CI_COMMIT_BRANCH == \"develop\"""""

private fun jenkinsfileYaml(): String = """pipeline {
    agent { label 'android' }
    
    options {
        timeout(time: 30, unit: 'MINUTES')
        buildDiscarder(logRotator(numToKeepStr: '10'))
    }
    
    stages {
        stage('Setup / 设置') {
            steps {
                echo 'Preparing Android environment / 准备 Android 环境'
                sh '''
                    export JAVA_HOME=${'$'}JAVA_HOME
                    chmod +x gradlew
                '''
            }
        }
        
        stage('Journeys E2E Tests / Journey E2E 测试') {
            steps {
                echo 'Running Journeys E2E Tests / 运行 Journey E2E 测试'
                sh './gradlew runJourneys --no-daemon --stacktrace'
            }
            post {
                always {
                    echo 'Archiving test results / 归档测试结果'
                    archiveArtifacts artifacts: 'app/build/journeys/results/**', allowEmptyArchive: true
                    junit 'app/build/journeys/results/*.xml'
                }
            }
        }
    }
    
    post {
        always {
            echo 'Pipeline completed / Pipeline 完成'
        }
        failure {
            echo 'Pipeline failed / Pipeline 失败'
        }
    }
}"""
