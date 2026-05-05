package com.mvi.kenny.feature.swiftexporttool

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mvi.kenny.feature.swiftexporttool.SwiftExportColors as Colors
import com.mvi.kenny.feature.swiftexporttool.SwiftExportTab as Tab
import com.mvi.kenny.feature.swiftexporttool.SwiftExportToolIntent as Intent
import com.mvi.kenny.feature.swiftexporttool.SwiftExportToolEffect as Effect
import com.mvi.kenny.feature.swiftexporttool.SwiftExportToolState as State
import com.mvi.kenny.feature.swiftexporttool.ScanState as ScanStatus
import com.mvi.kenny.feature.swiftexporttool.ScanResultType as ResultType
import com.mvi.kenny.feature.swiftexporttool.DecisionNodeType as NodeType
import com.mvi.kenny.feature.swiftexporttool.CICheckState as CICheck
import com.mvi.kenny.feature.swiftexporttool.DecisionTreeNode as Node
import com.mvi.kenny.feature.swiftexporttool.SwiftExportConfigResult as ConfigResult
import com.mvi.kenny.feature.swiftexporttool.SKIEAnnotationResult as AnnotationResult
import com.mvi.kenny.feature.swiftexporttool.IOSTemplate as Template
import com.mvi.kenny.feature.swiftexporttool.CICheckItem as CheckItem
import com.mvi.kenny.feature.swiftexporttool.CIValidationResult as CIResult
import com.mvi.kenny.feature.swiftexporttool.ConcurrencyBestPractice as Practice
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*

/**
 * ============================================================
 * SwiftExportToolViewModel — Swift Export iOS 原生互联络工具包 ViewModel
 * ============================================================
 * PRD-232 | Kotlin 2.2.20 Swift Export iOS 原生互联络工具包
 *
 * 职责：
 * - 管理 SwiftExportToolState（UI 状态唯一真相来源）
 * - 接收 Intent（用户意图），执行业务逻辑，输出新状态
 * - 通过 Effect Channel 发送一次性副作用
 *
 * MVI 数据流：
 * Intent → ViewModel → State → Screen recomposition
 *              ↓
 *           Effect（Channel）
 *
 * 注意：本工具不进行真实文件系统扫描，所有结果均为模拟数据
 *
 * @see SwiftExportToolContract MVI 契约定义
 * @see SwiftExportToolScreen UI 渲染层
 */
class SwiftExportToolViewModel : ViewModel() {

    // ============================================================
    // State Management / 状态管理
    // ============================================================

    private val _state = MutableStateFlow(State())
    val state: StateFlow<State> = _state.asStateFlow()

    // ============================================================
    // Effect Channels / 副作用通道
    // ============================================================

    private val _effect = Channel<Effect>(Channel.BUFFERED)
    val effect: Flow<Effect> = _effect.receiveAsFlow()

    // ============================================================
    // Intent Processing / 意图处理
    // ============================================================

    fun processIntent(intent: Intent) {
        when (intent) {
            // Tab 切换
            is Intent.SelectTab -> handleSelectTab(intent.tab)

            // Tab 1: Swift Export 配置扫描
            is Intent.StartSwiftExportScan -> handleStartSwiftExportScan()
            is Intent.CancelScan -> handleCancelScan()
            is Intent.SelectConfig -> handleSelectConfig(intent.configId)

            // Tab 2: SKIE → Swift Export 迁移
            is Intent.StartSKIEScan -> handleStartSKIEScan()
            is Intent.ToggleDecisionNode -> handleToggleDecisionNode(intent.nodeId)
            is Intent.SelectAnnotation -> handleSelectAnnotation(intent.annotationId)

            // Tab 3: iOS 集成模板
            is Intent.SelectTemplate -> handleSelectTemplate(intent.templateType)
            is Intent.CopyTemplate -> handleCopyTemplate(intent.templateType)

            // Tab 4: CI 校验
            is Intent.RunCIValidation -> handleRunCIValidation()
            is Intent.CancelCIValidation -> handleCancelCIValidation()

            // Tab 5: Swift Concurrency
            is Intent.TogglePracticeExpanded -> handleTogglePracticeExpanded(intent.practiceId)
            is Intent.CopyPracticeCode -> handleCopyPracticeCode(intent.practiceId)

            // 全局
            is Intent.DismissError -> handleDismissError()
        }
    }

    // ============================================================
    // Tab Switching / Tab 切换
    // ============================================================

    private fun handleSelectTab(tab: Tab) {
        _state.update { it.copy(selectedTab = tab) }
    }

    // ============================================================
    // Tab 1: Swift Export 配置扫描
    // ============================================================

    private fun handleStartSwiftExportScan() {
        viewModelScope.launch {
            _state.update { it.copy(scanState = ScanStatus.SCANNING, scanProgress = 0, swiftExportConfigs = emptyList()) }

            // 模拟扫描进度
            for (progress in 0..100 step 10) {
                delay(150)
                _state.update { it.copy(scanProgress = progress) }
            }

            // 模拟扫描结果
            val configs = listOf(
                ConfigResult(
                    id = "cfg-001",
                    configName = "swiftExport",
                    configValue = "enabled = true",
                    sourceFile = "shared/build.gradle.kts",
                    lineNumber = 42,
                    resultType = ResultType.SUCCESS,
                    message = "Swift Export 已启用"
                ),
                ConfigResult(
                    id = "cfg-002",
                    configName = "exportedFunctions",
                    configValue = "listOf(\"computeSum\", \"fetchData\")",
                    sourceFile = "shared/build.gradle.kts",
                    lineNumber = 43,
                    resultType = ResultType.SUCCESS,
                    message = "已配置 2 个导出函数"
                ),
                ConfigResult(
                    id = "cfg-003",
                    configName = "iosTarget",
                    configValue = "iosX64(), iosArm64(), iosSimulatorArm64()",
                    sourceFile = "shared/build.gradle.kts",
                    lineNumber = 38,
                    resultType = ResultType.WARNING,
                    message = "建议添加 iosSimulatorArm64() 以支持 Apple Silicon"
                ),
                ConfigResult(
                    id = "cfg-004",
                    configName = "swiftExportVersion",
                    configValue = "\"2.2.20\"",
                    sourceFile = "shared/build.gradle.kts",
                    lineNumber = 39,
                    resultType = ResultType.SUCCESS,
                    message = "Swift Export 版本: 2.2.20"
                ),
                ConfigResult(
                    id = "cfg-005",
                    configName = "swiftBuildConfig",
                    configValue = "freeCompilerArgs = listOf(\"-Xswiftc\", \"-cross-module-availability\")",
                    sourceFile = "shared/build.gradle.kts",
                    lineNumber = 44,
                    resultType = ResultType.WARNING,
                    message = "缺少交叉模块可用性标志，建议添加"
                )
            )

            _state.update { it.copy(scanState = ScanStatus.COMPLETED, swiftExportConfigs = configs) }
            _effect.send(Effect.ShowToast("扫描完成，发现 ${configs.size} 个配置项"))
        }
    }

    private fun handleCancelScan() {
        _state.update { it.copy(scanState = ScanStatus.IDLE, scanProgress = 0) }
    }

    private fun handleSelectConfig(configId: String?) {
        _state.update { it.copy(selectedConfigId = configId) }
    }

    // ============================================================
    // Tab 2: SKIE → Swift Export 迁移
    // ============================================================

    private fun handleStartSKIEScan() {
        viewModelScope.launch {
            _state.update { it.copy(scanState = ScanStatus.SCANNING, scanProgress = 0) }

            for (progress in 0..100 step 5) {
                delay(100)
                _state.update { it.copy(scanProgress = progress) }
            }

            // 构建决策树
            val rootNode = Node(
                id = "root",
                label = "SKIE → Swift Export 迁移决策树",
                description = "分析 @SKIE 注解，确定迁移路径",
                nodeType = NodeType.CONDITION,
                isExpanded = true,
                children = listOf(
                    Node(
                        id = "node-1",
                        label = "@SKIE跨平台注解检测",
                        description = "检测到 3 个 @SKIE 注解",
                        nodeType = NodeType.CONDITION,
                        isExpanded = false,
                        children = listOf(
                            Node(
                                id = "node-1-1",
                                label = "检查 @SKIE.Deprecated",
                                description = "发现 1 个 Deprecated 注解",
                                nodeType = NodeType.CONDITION,
                                children = listOf(
                                    Node(
                                        id = "node-1-1-a",
                                        label = "移除 @SKIE.Deprecated",
                                        description = "可直接移除，使用 Swift Export 原生替代",
                                        nodeType = NodeType.ACTION,
                                        recommendation = "直接移除该注解，Swift Export 提供同等功能",
                                        children = emptyList()
                                    )
                                )
                            ),
                            Node(
                                id = "node-1-2",
                                label = "检查 @SKIE.Attributes",
                                description = "发现 1 个 Attributes 注解",
                                nodeType = NodeType.CONDITION,
                                children = listOf(
                                    Node(
                                        id = "node-1-2-a",
                                        label = "迁移到 @SwiftExport.Attributes",
                                        description = "Swift Export 提供同名属性",
                                        nodeType = NodeType.ACTION,
                                        recommendation = "将 @SKIE.Attributes 替换为 @SwiftExport.Attributes",
                                        children = emptyList()
                                    )
                                )
                            ),
                            Node(
                                id = "node-1-3",
                                label = "检查 @SKIE.CustomEnum",
                                description = "发现 1 个 CustomEnum 注解",
                                nodeType = NodeType.CONDITION,
                                children = listOf(
                                    Node(
                                        id = "node-1-3-a",
                                        label = "使用 Swift Export 枚举导出",
                                        description = "Swift Export 原生支持枚举类型",
                                        nodeType = NodeType.ACTION,
                                        recommendation = "使用 swiftExport { enumExporting { ... } } 替代",
                                        children = emptyList()
                                    )
                                )
                            )
                        )
                    ),
                    Node(
                        id = "node-2",
                        label = "Swift Export 兼容性检查",
                        description = "检查目标 Kotlin 版本和插件版本",
                        nodeType = NodeType.CONDITION,
                        children = listOf(
                            Node(
                                id = "node-2-1",
                                label = "Kotlin 版本检查",
                                description = "当前版本: 2.0.20",
                                nodeType = NodeType.CONDITION,
                                children = listOf(
                                    Node(
                                        id = "node-2-1-a",
                                        label = "升级到 Kotlin 2.2.20",
                                        description = "Kotlin 2.2.20 完全支持 Swift Export",
                                        nodeType = NodeType.ACTION,
                                        recommendation = "升级 Kotlin 版本到 2.2.20",
                                        children = emptyList()
                                    )
                                )
                            ),
                            Node(
                                id = "node-2-2",
                                label = "Swift Export 插件版本",
                                description = "插件版本: 1.0.0",
                                nodeType = NodeType.CONDITION,
                                children = listOf(
                                    Node(
                                        id = "node-2-2-a",
                                        label = "更新插件到最新版本",
                                        description = "最新版本包含更多 Swift 5.9+ 特性支持",
                                        nodeType = NodeType.ACTION,
                                        recommendation = "更新 Swift Export 插件到最新稳定版",
                                        children = emptyList()
                                    )
                                )
                            )
                        )
                    ),
                    Node(
                        id = "node-3",
                        label = "迁移完成",
                        nodeType = NodeType.END,
                        recommendation = "所有 SKIE 注解已成功迁移到 Swift Export",
                        children = emptyList()
                    )
                )
            )

            val annotations = listOf(
                AnnotationResult(
                    id = "ann-001",
                    annotationName = "@SKIE.Deprecated",
                    annotatedElement = "SharedHelper.kt::SharedHelper",
                    sourceFile = "shared/src/commonMain/kotlin/SharedHelper.kt",
                    lineNumber = 15,
                    decisionTree = rootNode.children?.get(0)?.children?.get(0) ?: rootNode,
                    isMigratable = true
                ),
                AnnotationResult(
                    id = "ann-002",
                    annotationName = "@SKIE.Attributes",
                    annotatedElement = "UserModel.kt::UserAttributes",
                    sourceFile = "shared/src/commonMain/kotlin/model/UserModel.kt",
                    lineNumber = 28,
                    decisionTree = rootNode.children?.get(0)?.children?.get(1) ?: rootNode,
                    isMigratable = true
                ),
                AnnotationResult(
                    id = "ann-003",
                    annotationName = "@SKIE.CustomEnum",
                    annotatedElement = "Status.kt::UserStatus",
                    sourceFile = "shared/src/commonMain/kotlin/model/Status.kt",
                    lineNumber = 12,
                    decisionTree = rootNode.children?.get(0)?.children?.get(2) ?: rootNode,
                    isMigratable = true
                )
            )

            _state.update {
                it.copy(
                    scanState = ScanStatus.COMPLETED,
                    rootDecisionNode = rootNode,
                    skieAnnotationResults = annotations
                )
            }
            _effect.send(Effect.ShowToast("SKIE 迁移分析完成，发现 ${annotations.size} 个注解"))
        }
    }

    private fun handleToggleDecisionNode(nodeId: String) {
        fun toggleNode(node: Node): Node {
            return if (node.id == nodeId) {
                node.copy(isExpanded = !node.isExpanded)
            } else {
                node.copy(children = node.children.map { toggleNode(it) })
            }
        }
        _state.update {
            it.copy(rootDecisionNode = it.rootDecisionNode?.let { toggleNode(it) })
        }
    }

    private fun handleSelectAnnotation(annotationId: String?) {
        _state.update { it.copy(selectedAnnotationId = annotationId) }
    }

    // ============================================================
    // Tab 3: iOS 集成模板
    // ============================================================

    init {
        // 初始化 iOS 集成模板
        _state.update {
            it.copy(
                templates = listOf(
                    Template(
                        type = com.mvi.kenny.feature.swiftexporttool.TemplateType.PACKAGE_SWIFT,
                        name = "Package.swift",
                        checksum = "a3b7c9d2e5f8g0h1i2j3k4l5m6n7o8p9q0r1s2t3u4v5w6x7y8z9",
                        description = "Swift Package Manager 依赖配置文件",
                        content = """// swift-tools-version: 5.9
import PackageDescription

let package = Package(
    name: "SharedKit",
    platforms: [
        .iOS(.v15),
        .macOS(.v12)
    ],
    products: [
        .library(
            name: "SharedKit",
            targets: ["SharedKit"]
        )
    ],
    targets: [
        .target(
            name: "SharedKit",
            dependencies: [],
            path: "Sources/SharedKit",
            swiftSettings: [
                .define("SWIFT_EXPORT_ENABLED"),
                .enableFeatureSpecifiers
            ]
        )
    ],
    swiftLanguageVersions: [.v5, .v6]
)
"""
                    ),
                    Template(
                        type = com.mvi.kenny.feature.swiftexporttool.TemplateType.SPM_STEPS,
                        name = "Xcode 集成步骤",
                        checksum = "b4c8d0e1f2g3h4i5j6k7l8m9n0o1p2q3r4s5t6u7v8w9x0y1z2",
                        description = "在 Xcode 中集成 Swift Export 产物的详细步骤",
                        content = """# Swift Export Xcode 集成指南

## 步骤 1: 添加 SPM 依赖

1. 打开 Xcode 项目
2. File → Add Package Dependencies
3. 输入 Package.swift 的 URL 或本地路径
4. 选择 SharedKit 产品并添加到目标

## 步骤 2: 配置 Build Settings

```swift
SWIFT_EXPORT_ENABLED = YES
OTHER_LDFLAGS = -Wl,-no_warn_duplicate_externals
```

## 步骤 3: 验证集成

```swift
import SharedKit

// 调用 Kotlin 导出的函数
let result = computeSum(a: 10, b: 20)
print("Result: \\(result)")
```

## 步骤 4: 启用 Swift 6 Concurrency（可选）

在 Xcode 15+ 中，添加以下设置以启用严格 Concurrency 检查：

```swift
SWIFT_STRICT_CONCURRENCY = complete
```
"""
                    ),
                    Template(
                        type = com.mvi.kenny.feature.swiftexporttool.TemplateType.GRADLE_CONFIG,
                        name = "build.gradle.kts 配置",
                        checksum = "c5d9e2f3g4h5i6j7k8l9m0n1o2p3q4r5s6t7u8v9w0x1y2z3a4b5",
                        description = "Swift Export Gradle 完整配置示例",
                        content = """// shared/build.gradle.kts
plugins {
    kotlin("multiplatform") version "2.2.20"
    id("com.swiftexport") version "1.0.0"
}

kotlin {
    iosX64()
    iosArm64()
    iosSimulatorArm64()

    swiftExport {
        enabled = true
        version = "2.2.20"

        exportedFunctions {
            // 导出计算函数
            include("com.example.shared.compute.*")
            // 导出数据模型
            include("com.example.shared.model.*")
        }

        iosTarget {
            moduleName = "SharedKit"
            productName = "SharedKit"
            minOsVersion = "15.0"
        }

        swiftBuildConfig {
            freeCompilerArgs = listOf(
                "-Xswiftc", "-cross-module-availability",
                "-Xswiftc", "-enable-upcoming-feature=Swift6LanguageVersion"
            )
        }
    }

    sourceSets {
        commonMain {
            dependencies {
                implementation("org.jetbrains.kotlin:kotlin-stdlib")
            }
        }
    }
}
"""
                    ),
                    Template(
                        type = com.mvi.kenny.feature.swiftexporttool.TemplateType.XCODE_PROJECT,
                        name = "xcconfig 配置",
                        checksum = "d6e0f3g4h5i6j7k8l9m0n1o2p3q4r5s6t7u8v9w0x1y2z3a4b5c6d7",
                        description = "Xcode Build Configuration 文件",
                        content = """// SharedKit.xcconfig
SWIFT_VERSION = 5.9
IPHONEOS_DEPLOYMENT_TARGET = 15.0
MACOSX_DEPLOYMENT_TARGET = 12.0

// Swift Export 配置
SWIFT_EXPORT_ENABLED = YES
SWIFT_EXPORT_MODULE_NAME = SharedKit
OTHER_LDFLAGS = -Wl,-no_warn_duplicate_externals

// Swift 6 Concurrency
SWIFT_STRICT_CONCURRENCY = complete
GCC_PREPROCESSOR_DEFINITIONS = SWIFT_EXPORT_ENABLED=1
"""
                    )
                )
            )
        }
    }

    private fun handleSelectTemplate(templateType: com.mvi.kenny.feature.swiftexporttool.TemplateType) {
        // 模板选择逻辑（当前为展示选中状态）
    }

    private fun handleCopyTemplate(templateType: com.mvi.kenny.feature.swiftexporttool.TemplateType) {
        viewModelScope.launch {
            val template = _state.value.templates.find { it.type == templateType }
            template?.let {
                _effect.send(Effect.CopyToClipboard(it.content, it.name))
                _effect.send(Effect.ShowToast("${it.name} 已复制到剪贴板"))
            }
        }
    }

    // ============================================================
    // Tab 4: CI 校验工具
    // ============================================================

    private fun handleRunCIValidation() {
        viewModelScope.launch {
            val checks = listOf(
                CheckItem(id = "check-1", name = ".swiftmodule 版本一致性检查", description = "验证各平台 swiftmodule 版本一致", status = CICheck.PENDING),
                CheckItem(id = "check-2", name = "Gradle Task 依赖链验证", description = "检查 swiftExport* 任务依赖完整性", status = CICheck.PENDING),
                CheckItem(id = "check-3", name = "导出函数符号检查", description = "验证 exportedFunctions 符号可解析", status = CICheck.PENDING),
                CheckItem(id = "check-4", name = "iOS 目标平台检查", description = "检查 iosX64/iosArm64/iosSimulatorArm64 配置", status = CICheck.PENDING),
                CheckItem(id = "check-5", name = "Swift 版本兼容性检查", description = "验证 Swift 5.9+ 语法特性使用", status = CICheck.PENDING),
                CheckItem(id = "check-6", name = "CI Pipeline 集成验证", description = "检查 GitHub Actions / GitLab CI 配置", status = CICheck.PENDING)
            )

            _state.update {
                it.copy(
                    ciValidationResult = CIResult(
                        totalChecks = checks.size,
                        passedChecks = 0,
                        failedChecks = 0,
                        items = checks,
                        overallPassed = false,
                        gradleTaskChain = ":shared:swiftExportIosArm64 :shared:swiftExportIosX64 :shared:swiftExportIosSimulatorArm64"
                    )
                )
            }

            // 模拟 CI 执行
            for (check in checks) {
                _state.update {
                    val updatedItems = it.ciValidationResult.items.map { item ->
                        if (item.id == check.id) item.copy(status = CICheck.RUNNING) else item
                    }
                    it.copy(ciValidationResult = it.ciValidationResult.copy(items = updatedItems))
                }

                delay(800)

                // 模拟结果：大部分通过
                val passed = check.id != "check-6"  // CI Pipeline 检查失败（示例）
                _state.update {
                    val updatedItems = it.ciValidationResult.items.map { item ->
                        if (item.id == check.id) {
                            item.copy(
                                status = if (passed) CICheck.PASSED else CICheck.FAILED,
                                message = if (passed) "检查通过" else "未找到 CI 配置文件",
                                durationMs = 800
                            )
                        } else item
                    }
                    val passedCount = updatedItems.count { it.status == CICheck.PASSED }
                    val failedCount = updatedItems.count { it.status == CICheck.FAILED }
                    it.copy(
                        ciValidationResult = it.ciValidationResult.copy(
                            items = updatedItems,
                            passedChecks = passedCount,
                            failedChecks = failedCount,
                            overallPassed = failedCount == 0
                        )
                    )
                }
            }

            val overallPassed = _state.value.ciValidationResult.overallPassed
            _effect.send(
                if (overallPassed) Effect.ShowToast("CI 校验全部通过 ✓")
                else Effect.ShowToast("CI 校验完成，1 项失败")
            )
        }
    }

    private fun handleCancelCIValidation() {
        _state.update {
            it.copy(ciValidationResult = CIResult())
        }
    }

    // ============================================================
    // Tab 5: Swift Concurrency 最佳实践
    // ============================================================

    init {
        // 初始化最佳实践
        _state.update {
            it.copy(
                bestPractices = listOf(
                    Practice(
                        id = "prac-001",
                        patternName = "Kotlin suspend → Swift async/await",
                        category = "函数导出",
                        codeExample = """// Kotlin
@suspendable
fun computeSum(a: Int, b: Int): Int = a + b

// Swift 调用
Task {
    let result = try await shared.computeSum(a: 10, b: 20)
    print("Result: \\(result)")
}""",
                        description = "Kotlin suspend 函数自动映射为 Swift async 函数",
                        benefits = listOf("原生 async/await 体验", "结构化并发支持", "取消操作自动传播")
                    ),
                    Practice(
                        id = "prac-002",
                        patternName = "Flow → AsyncSequence",
                        category = "数据流导出",
                        codeExample = """// Kotlin
fun observeData(): Flow<Data> = dataFlow

// Swift 调用
for await data in shared.observeData() {
    print("Received: \\(data)")
}""",
                        description = "Kotlin Flow 导出为 Swift AsyncSequence",
                        benefits = listOf("懒加载数据流", "背压自动处理", "与 Swift 生态无缝集成")
                    ),
                    Practice(
                        id = "prac-003",
                        patternName = "Result 类型导出",
                        category = "错误处理",
                        codeExample = """// Kotlin
fun fetchUser(): Result<User, ApiError>

// Swift 调用
do {
    let user = try await shared.fetchUser().get()
    print("User: \\(user)")
} catch {
    print("Error: \\(error)")
}""",
                        description = "Kotlin Result 导出为 Swift Result 类型",
                        benefits = listOf("类型安全错误处理", "与 Swift Error 互通", "编译期错误检查")
                    ),
                    Practice(
                        id = "prac-004",
                        patternName = "Actor 隔离状态",
                        category = "并发安全",
                        codeExample = """// Kotlin
@SwiftExport
class SafeCounter {
    private var count = 0
    suspend fun increment() { count++ }
}

// Swift 调用
actor SafeCounterClient {
    func increment() async {
        await shared.safeCounter.increment()
    }
}""",
                        description = "使用 Actor 隔离导出类的状态访问",
                        benefits = listOf("数据竞争保护", "状态修改线程安全", "与 Swift Actor 语义一致")
                    ),
                    Practice(
                        id = "prac-005",
                        patternName = "MainActor UI 绑定",
                        category = "UI 线程调度",
                        codeExample = """// Kotlin
@MainActor
fun updateUI(data: String) { ... }

// Swift
@MainActor
func refreshView() async {
    await shared.updateUI(data: "Hello")
}""",
                        description = "@MainActor 注解确保 UI 操作在主线程执行",
                        benefits = listOf("自动线程切换", "UI 线程安全", "SwiftUI 完美适配")
                    )
                )
            )
        }
    }

    private fun handleTogglePracticeExpanded(practiceId: String) {
        _state.update {
            val current = it.expandedPracticeId
            it.copy(expandedPracticeId = if (current == practiceId) null else practiceId)
        }
    }

    private fun handleCopyPracticeCode(practiceId: String) {
        viewModelScope.launch {
            val practice = _state.value.bestPractices.find { it.id == practiceId }
            practice?.let {
                _effect.send(Effect.CopyToClipboard(it.codeExample, it.patternName))
                _effect.send(Effect.ShowToast("代码已复制到剪贴板"))
            }
        }
    }

    // ============================================================
    // Global / 全局
    // ============================================================

    private fun handleDismissError() {
        _state.update { it.copy(errorMessage = null) }
    }
}
