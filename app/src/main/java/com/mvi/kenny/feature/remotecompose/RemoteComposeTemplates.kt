package com.mvi.kenny.feature.remotecompose

// ================================================================
// RemoteComposeTemplates — 代码模板集合（设计文档第 3 节模块结构）
// ================================================================
// All code templates for the 10 developer tools in PRD-191.
// Each template is a const String containing realistic, annotated code.
// ================================================================

private const val SERVER_EXAMPLE_TEMPLATE = """
// ============================================================
// 服务器端 Composable 示例
// Server Composable Example
// ============================================================
// 服务器端 Composable 使用纯 JVM Kotlin，无需 Android 依赖
// Server-side Composable uses pure JVM Kotlin, no Android dependencies needed

@RemoteComposable
data class ProductCardPayload(
    val id: String,
    val title: String,
    val price: String,
    val imageUrl: String,
    val rating: Float,
    val reviewCount: Int
)

@RemoteComposable
fun ProductCard(payload: ProductCardPayload): RemoteComposeNode {
    return Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth()
    ) {
        // 图像使用 Skia 绘制指令，客户端只需渲染
        // Image rendered as Skia drawing instructions, client just renders
        Image(
            url = payload.imageUrl,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clip(RoundedCornerShape(8.dp))
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = payload.title,
            style = MaterialTheme.typography.titleMedium,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = payload.price, style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.weight(1f))
            RatingBar(rating = payload.rating, reviewCount = payload.reviewCount)
        }
    }
}

// 序列化后的 Skia 二进制通过 HTTP 请求下发到客户端
// Serialized Skia binary is transmitted to client via HTTP request
// POST /api/remote-ui/v1/render
// Request: { "composable": "ProductCard", "payload": {...}, "clientVersion": "alpha06" }
// Response: { "skiaBinary": "<gzip compressed bytes>", "version": 1, "timestamp": 1714304000000 }
"""

private const val DECISION_TREE_MARKDOWN = """
# Remote Compose vs JSON Server-Driven UI 选型决策树

## 问题 1：是否需要动态 UI？
**需要动态更新 UI 但不希望发布 App 更新？**
- ✅ YES → 继续问题 2
- ❌ NO → 推荐：本地 Compose UI（传统开发）

---

## 问题 2：团队技术栈是什么？
**已有 Compose 开发经验还是主要是 Web/后端开发者？**
- ✅ Compose 团队 → 继续问题 3
- ❌ Web/后端为主 → JSON Server-Driven UI（更易上手）

---

## 问题 3：性能要求如何？
**UI 渲染性能是否是核心指标？**

- ✅ 极致性能（60fps、复杂动画）→ **Remote Compose**
  - Skia 二进制指令，渲染性能接近原生
  - 无需 JSON 解析和 widget tree 构建
  - 适合：电商 SKU 列表、运营活动页、高频刷新 UI

- ⚠️ 一般性能要求 → JSON Server-Driven UI
  - 更成熟的生态（JSON 解析库、跨平台方案）
  - 适合：新闻 Feed、简单表单、低频更新 UI

---

## 问题 4：Payload 大小是否可接受？
**Skia 二进制 Payload 可能达到 100KB-500KB（Gzip 后）**
- 网络条件好（Wi-Fi/5G）→ Remote Compose 可行
- 网络条件差（弱网/2G）→ JSON Server-Driven UI（Payload 通常 10-50KB）

---

## 快速决策

| 维度 | Remote Compose | JSON SDUI |
|------|---------------|-----------|
| 渲染性能 | ⭐⭐⭐⭐⭐ | ⭐⭐⭐ |
| Payload 大小 | ⭐⭐（大）| ⭐⭐⭐⭐（小）|
| 开发者体验 | ⭐⭐⭐⭐（Compose）| ⭐⭐⭐（模板代码）|
| 跨平台支持 | ⭐⭐（仅 Android）| ⭐⭐⭐⭐ |
| 生态成熟度 | ⭐⭐（alpha）| ⭐⭐⭐⭐⭐ |

**结论**：
- Remote Compose：极致性能 + Compose 开发者体验，Android 独占，alpha06 阶段
- JSON SDUI：成熟稳定，跨平台，payload 小，适合内容类 App
"""

private const val DECISION_FLOW_CHART = """
# Remote Compose 选型流程图
# Remote Compose Decision Flow Chart

[开始] --> {需要动态 UI？}
          |YES| --> {团队技术栈}
                     |Compose| --> {性能要求？}
                                  |极致| --> [选择 Remote Compose ✅]
                                  |一般| --> {Payload 大小可接受？}
                                            |YES| --> [选择 Remote Compose ✅]
                                            |NO| --> [选择 JSON SDUI]
                     |Web/后端| --> [选择 JSON SDUI]
          |NO| --> [选择本地 Compose UI]

# 颜色说明
- 绿色框：最终决策
- 蓝色框：中间判断
- 灰色框：起点/终点
"""

private const val PAYLOAD_SIGNING_TEMPLATE = """
// ============================================================
// Payload 签名机制
// Payload Signing Mechanism
// ============================================================
// HMAC-SHA256 是 Google 推荐的 Payload 签名算法
// HMAC-SHA256 is Google's recommended payload signing algorithm

import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import java.security.MessageDigest
import java.nio.charset.StandardCharsets
import java.util.Base64

/**
 * Payload 签名器
 * 使用 HMAC-SHA256 对 Skia 二进制 Payload 进行签名
 * Signs Skia binary payloads using HMAC-SHA256
 *
 * @param secretKey 签名密钥（服务器端安全存储，不能暴露给客户端）
 * @param secretKey Signing key (stored securely server-side, never exposed to client)
 */
class PayloadSigner(private val secretKey: ByteArray) {

    /**
     * 对 Payload 进行签名
     * @param payload Skia 二进制 Payload
     * @param timestamp 签名时间戳（毫秒）
     * @return Base64 编码的签名
     */
    fun sign(payload: ByteArray, timestamp: Long): String {
        // 签名密钥必须安全存储，不能硬编码或放在客户端
        // Signing key must be stored securely, never hardcoded or in client
        require(secretKey.isNotEmpty()) { "Signing key must not be empty" }

        // 时间戳验证防止重放攻击，Payload 有效期通常设为 5-10 分钟
        // Timestamp validation prevents replay attacks, payload TTL usually 5-10 minutes
        val dataToSign = ByteArray(payload.size + 8)
        System.arraycopy(payload, 0, dataToSign, 0, payload.size)
        // Append timestamp (8 bytes, big-endian)
        dataToSign[payload.size] = ((timestamp shr 56) and 0xFF).toByte()
        dataToSign[payload.size + 1] = ((timestamp shr 48) and 0xFF).toByte()
        dataToSign[payload.size + 2] = ((timestamp shr 40) and 0xFF).toByte()
        dataToSign[payload.size + 3] = ((timestamp shr 32) and 0xFF).toByte()
        dataToSign[payload.size + 4] = ((timestamp shr 24) and 0xFF).toByte()
        dataToSign[payload.size + 5] = ((timestamp shr 16) and 0xFF).toByte()
        dataToSign[payload.size + 6] = ((timestamp shr 8) and 0xFF).toByte()
        dataToSign[payload.size + 7] = (timestamp and 0xFF).toByte()

        val mac = Mac.getInstance("HmacSHA256")
        mac.init(SecretKeySpec(secretKey, "HmacSHA256"))
        return Base64.getEncoder().encodeToString(mac.doFinal(dataToSign))
    }
}
"""

private const val PAYLOAD_VERIFICATION_TEMPLATE = """
// ============================================================
// Payload 验签流程
// Payload Verification Flow
// ============================================================

/**
 * 客户端验签
 * Verifies payload signature on client side
 *
 * @param payload Skia 二进制 Payload
 * @param signature Base64 编码的签名
 * @param timestamp 时间戳
 * @param secretKey 客户端存储的验签密钥（与服务器端不同，仅用于完整性校验）
 * @return 验签是否通过
 */
fun verifyPayloadSignature(
    payload: ByteArray,
    signature: String,
    timestamp: Long,
    secretKey: ByteArray
): Boolean {
    val maxAgeMs = 10 * 60 * 1000L  // 10 分钟有效期
    val currentTime = System.currentTimeMillis()

    // 时间戳过期检查
    if (kotlin.math.abs(currentTime - timestamp) > maxAgeMs) {
        // 验签失败时必须拒绝 Payload，展示 fallback UI
        // On verification failure, must reject payload and show fallback UI
        return false
    }

    // 计算本地签名
    val serverSigner = PayloadSigner(secretKey)
    val expectedSignature = serverSigner.sign(payload, timestamp)

    // 日志仅记录验签失败事件，不记录 Payload 内容
    // Only log verification failure events, never log payload content
    return MessageDigest.isEqual(
        signature.toByteArray(StandardCharsets.UTF_8),
        expectedSignature.toByteArray(StandardCharsets.UTF_8)
    )
}
"""

private const val INJECTION_DEFENSE_TEMPLATE = """
# Remote Compose 安全防护指南
# Remote Compose Security Defense Guide

## 三大攻击向量 / Three Attack Vectors

1. **MITM（中间人攻击）** — 攻击者拦截并篡改网络请求
2. **Content Injection（内容注入）** — 恶意 Payload 注入到响应中
3. **Payload Tampering（Payload 篡改）** — 已签名的 Payload 被篡改后重放

---

## 防御措施 / Defense Measures

### 1. HTTPS + 证书固定
- HTTPS + 证书固定防止 MITM，中间人无法解密 Payload
- Implement TLS with certificate pinning to prevent MITM attacks

```
# OkHttp 证书固定配置
val certificatePinner = CertificatePinner.Builder()
    .add("api.example.com", "sha256/AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=")
    .build()
```

### 2. HMAC 签名验证
- HMAC 签名防止 Payload 被篡改或重放
- HMAC signature prevents payload tampering or replay attacks

### 3. Payload 版本号校验
- 每次请求携带当前已知的最新 Payload 版本号
- 服务器拒绝低于客户端版本的请求

### 4. 运行时沙箱
- RemoteComposePlayer 运行在受限的 Compose 沙箱中
- 禁止执行任意代码、访问文件系统或发起网络请求
"""

private const val CI_PIPELINE_TEMPLATE = """
# ============================================================
# Remote Compose CI/CD Pipeline — GitHub Actions
# ============================================================
// 使用 K2 编译器模式，远程 Composable DSL 需要 K2 支持
// K2 compiler mode is required for Remote Compose DSL support

name: Remote Compose Server DSL CI

on:
  push:
    branches: [main, 'feature/**']
  pull_request:
    branches: [main]

jobs:
  test-server-dsl:
    runs-on: ubuntu-latest
    # 服务器端 Compose DSL 使用纯 JVM 测试，无需 Android 模拟器
    # Server-side Compose DSL uses pure JVM tests, no Android emulator needed
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with:
          distribution: 'zulu'
          java-version: '21'

      - name: Setup Kotlin K2
        run: |
          ./gradlew wrapper --gradle-version=8.5
          ./gradlew -Pkotlin.mpp.enableK2=true :server:compileKotlin

      - name: Run Server DSL Tests
        run: |
          ./gradlew :server:test \
            --tests "com.example.server.RemoteComposeCompilerTest"

      - name: Payload Size Check
        run: |
          ./gradlew :server:test \
            --tests "com.example.server.PayloadSizeBenchmark"
          # payload-size-check 限制单个 Payload 不超过 500KB（Gzip 后）
          # payload-size-check enforces max 500KB per Payload (Gzip-compressed)

      - name: Upload Test Results
        uses: actions/upload-artifact@v4
        with:
          name: test-results
          path: server/build/test-results/

  security-scan:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - name: Run OWASP Dependency Check
        run: ./gradlew dependencyCheckAnalyze
"""

private const val SERVER_TEST_TEMPLATE = """
// ============================================================
// 服务器端 Compose 单元测试模板
// Server Compose Unit Test Template
// ============================================================
// 测试纯度：验证 Composable 是否为纯函数（无副作用）
// Test purity: verify Composable is a pure function (no side effects)

import org.junit.Test
import org.junit.Assert.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class RemoteComposeCompilerTest {

    @Test
    fun `CardComposable is pure function - same input yields same output`() {
        // Given: same payload
        val payload = ProductCardPayload(
            id = "1",
            title = "Test Product",
            price = "$99.99",
            imageUrl = "https://example.com/image.jpg",
            rating = 4.5f,
            reviewCount = 128
        )

        // When: compile twice
        val result1 = RemoteComposeCompiler.compile { ProductCard(payload) }
        val result2 = RemoteComposeCompiler.compile { ProductCard(payload) }

        // Then: results should be identical (pure function guarantee)
        // 序列化测试：验证 Composable 结果可正确序列化为 Skia 二进制
        // Serialization test: verify Composable result can serialize to Skia binary
        assertEquals(result1.skiaBinary.size, result2.skiaBinary.size)
        assertTrue(result1.skiaBinary.contentEquals(result2.skiaBinary))
    }

    @Test
    fun `Payload serialization roundtrip preserves data`() {
        val originalPayload = ProductCardPayload(
            id = "1",
            title = "Test",
            price = "$10",
            imageUrl = "https://example.com/img.jpg",
            rating = 4.0f,
            reviewCount = 50
        )

        val json = Json.encodeToString(originalPayload)
        val decoded = Json.decodeFromString<ProductCardPayload>(json)

        assertEquals(originalPayload, decoded)
    }
}
"""

private const val BINARY_BENCHMARK_TEMPLATE = """
// ============================================================
// Payload 大小基准测试
// Binary Size Benchmark
// ============================================================
// 复杂 UI（100+ 组件）的 Skia 二进制可能达到 500KB+
// Complex UIs (100+ components) Skia binary can reach 500KB+

import org.junit.Test
import kotlinx.serialization.json.Json
import java.io.ByteArrayOutputStream
import java.util.zip.GZIPOutputStream

class PayloadSizeBenchmark {

    @Test
    fun `benchmark Skia binary size for various UI complexities`() {
        val testCases = listOf(
            TestCase("SimpleCard", 5),      // 5 components
            TestCase("ProductList", 50),    // 50 components
            TestCase("ComplexDashboard", 100),  // 100 components
            TestCase("FullPage", 200)       // 200 components
        )

        testCases.forEach { case ->
            val rawBinary = RemoteComposeCompiler.compile(case.composable)
            val gzipped = gzip(rawBinary.skiaBinary)

            println("""
                |=== Payload Size Report ===
                |Test Case: ${case.name}
                |Components: ${case.componentCount}
                |Raw Size: ${rawBinary.skiaBinary.size / 1024.0} KB
                |Gzipped Size: ${gzipped.size / 1024.0} KB
                |Compression Ratio: ${(1 - gzipped.size.toDouble() / rawBinary.skiaBinary.size) * 100}%
            """.trimMargin())

            // Assert: Gzipped payload should be under 500KB for all cases
            assertTrue(
                "Payload \${'$'}{case.name} exceeds 500KB limit",
                gzipped.size <= 500 * 1024
            )
        }
    }

    // gzip 压缩率通常在 70-80%，对 Skia 二进制效果显著
    // Gzip compression ratio is typically 70-80%, significant for Skia binary
    private fun gzip(data: ByteArray): ByteArray {
        val bos = ByteArrayOutputStream()
        GZIPOutputStream(bos).use { it.write(data) }
        return bos.toByteArray()
    }
}
"""

private const val AB_TESTING_TEMPLATE = """
// ============================================================
// A/B Testing 集成模板
// A/B Testing Integration Template
// ============================================================
// variantId 通过 HTTP Header 或 URL 参数传递给服务器
// variantId is passed to server via HTTP header or URL parameter

class RemoteComposeViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(RemoteUiState())
    val uiState: StateFlow<RemoteUiState> = _uiState.asStateFlow()

    /**
     * 加载指定变体的 Payload
     * Load Payload for a specific variant
     */
    fun loadVariant(variantId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, variantId = variantId) }

            try {
                val payload = remoteComposeRepository.fetchPayload(
                    url = "https://api.example.com/remote-ui/v1/payload",
                    variantId = variantId  // 实时切换：服务器可随时下发不同 variantId 的 Payload
                    // Real-time switching: server can push different variantId payloads at any time
                )
                _uiState.update {
                    it.copy(
                        payloadStatus = PayloadStatus.READY,
                        currentPayload = payload,
                        isLoading = false
                    )
                }

                // ABTestMetrics 用于记录变体切换事件，支持 Firebase/自建分析
                // ABTestMetrics records variant switch events, supports Firebase/custom analytics
                ABTestMetrics.recordVariantLoaded(
                    variantId = variantId,
                    payloadVersion = payload.version
                )

            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        payloadStatus = PayloadStatus.ERROR,
                        errorMessage = e.message,
                        isLoading = false
                    )
                }
            }
        }
    }
}
"""

private const val VARIANT_ROUTER_TEMPLATE = """
// ============================================================
// 变体路由示例
// Variant Router Example
// ============================================================
// 变体路由逻辑可在客户端或服务器端实现，推荐服务器端
// Variant routing can be implemented on client or server side, server-side is recommended

/**
 * A/B 变体路由器
 * A/B Variant Router
 *
 * @param variants 可用变体列表
 * @param defaultVariant 默认变体（当用户不符合任何条件时使用）
 */
class VariantRouter(
    private val variants: List<ABVariant>,
    private val defaultVariant: String = "control"
) {
    /**
     * 根据用户 ID 路由到对应变体
     * Route user to corresponding variant based on user ID
     *
     * 用户分群策略：uid hash 后取模，保证同一用户每次路由结果一致
     * User segmentation: hash(uid) % mod ensures consistent routing per user
     */
    fun route(uid: String, experimentId: String): String {
        val hash = uid.hashCode().toLong()
        val variantCount = variants.size
        val targetIndex = kotlin.math.abs(hash % variantCount).toInt()

        return variants.getOrNull(targetIndex)?.variantId ?: defaultVariant
    }
}

// 使用示例
val router = VariantRouter(
    variants = listOf(
        ABVariant("control", 50.0),   // 50% 用户
        ABVariant("variant_a", 30.0), // 30% 用户
        ABVariant("variant_b", 20.0)   // 20% 用户
    )
)
val variantId = router.route(uid = "user_12345", experimentId = "exp_001")
// user_12345 每次调用都会返回相同的 variantId
"""

private const val TALKBACK_GUIDE_TEMPLATE = """
# Remote Compose TalkBack 适配指南
# Remote Compose TalkBack Compatibility Guide

## 核心约束
// RemoteComposePlayer 内的 Composable 必须随 Payload 下发 semantics 信息
// Composables inside RemoteComposePlayer must carry semantics info in payload

## 服务器端要求
// 动态内容的 Accessibility 信息无法静态分析，需服务器端协同
// Dynamic content accessibility info cannot be analyzed statically, requires server-side cooperation

每个可交互元素必须包含：

```kotlin
@RemoteComposable
fun AccessibleButton(
    label: String,
    onClick: () -> Unit,
    semanticsLabel: String  // 必须包含，用于 TalkBack 读取
    // Must include for TalkBack to read
): RemoteComposeNode {
    return Button(
        onClick = onClick,
        modifier = Modifier.semantics {
            contentDescription = semanticsLabel
            // Modifier.semantics 必须添加到每个需要 TalkBack 支持的组件
            // Modifier.semantics must be added to every component requiring TalkBack support
        }
    ) {
        Text(label)
    }
}
```

## 客户端要求
1. RemoteComposePlayer 必须启用 Accessibility 服务
2. 捕获所有 RemoteComposePlayer 内的 Accessibility 事件
3. 将事件回传到服务器用于分析

## 测试清单
- [ ] TalkBack 可以朗读所有文本内容
- [ ] 所有按钮有 contentDescription
- [ ] 焦点顺序符合视觉顺序
- [ ] 动态内容加载后 Accessibility 事件正确触发
"""

private const val DYNAMIC_ACCESSIBILITY_TEMPLATE = """
# Remote Compose 动态内容 Accessibility 规范
# Dynamic Content Accessibility Specification

## 核心原则

1. **所有可交互元素必须有 Label**
   // Payload 中必须包含每个可交互元素的 accessibilityLabel
   // Every interactive element must include accessibilityLabel in payload

2. **焦点管理**
   // 焦点管理：TalkBack 模式下的焦点遍历顺序需明确定义
   // Focus management: TalkBack mode focus traversal order must be explicitly defined

3. **动态内容通知**
   - 使用 `announceForAccessibility()` 通知用户 UI 变化
   - 延迟announce，确保屏幕阅读器完成当前朗读

## Payload 中的 Accessibility 信息

```json
{
  "node": "Button",
  "payload": {
    "label": "Buy Now",
    "accessibilityLabel": "购买此商品，按钮",
    "accessibilityHint": "双击购买",
    "role": "button"
  }
}
```

## 禁止事项
- ❌ 不能依赖客户端推断 Accessibility 信息
- ❌ 不能省略 `accessibilityLabel`
- ❌ 不能在 Payload 中包含敏感信息（如价格、密码）
"""

private const val RENDER_METRICS_TEMPLATE = """
// ============================================================
// 渲染指标收集器
// Render Metrics Collector
// ============================================================
// 三个核心指标：渲染成功率 / 渲染延迟 / 崩溃率
// Three core metrics: render success rate / render latency / crash rate

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * 渲染指标收集器（单例）
 * Render Metrics Collector (singleton)
 *
 * @param metricsEndpoint 上报指标的后端地址
 */
class RenderMetricsCollector(
    private val metricsEndpoint: String = "https://metrics.example.com/remote-compose"
) {
    // RenderMetricsCollector 作为单例，跨页面共享
    // RenderMetricsCollector as singleton, shared across pages

    companion object {
        @Volatile
        private var instance: RenderMetricsCollector? = null

        fun getInstance(): RenderMetricsCollector {
            return instance ?: synchronized(this) {
                instance ?: RenderMetricsCollector().also { instance = it }
            }
        }
    }

    /**
     * 记录渲染成功事件
     * Record render success event
     */
    fun recordRenderSuccess(
        payloadVersion: Int,
        renderDurationMs: Long,
        payloadSizeBytes: Long
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            // 使用快慢通道分离：成功指标走快通道，详情走异步
            // Use fast/slow channel separation: success metrics via fast path, details async
            sendMetric(
                RenderMetric(
                    eventType = "render_success",
                    payloadVersion = payloadVersion,
                    renderDurationMs = renderDurationMs,
                    payloadSizeBytes = payloadSizeBytes,
                    timestamp = System.currentTimeMillis()
                )
            )
        }
    }

    /**
     * 记录渲染失败事件
     * Record render failure event
     */
    fun recordRenderFailure(
        payloadVersion: Int,
        errorCode: String,
        errorMessage: String
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            sendMetric(
                RenderMetric(
                    eventType = "render_failure",
                    payloadVersion = payloadVersion,
                    errorCode = errorCode,
                    errorMessage = errorMessage,
                    timestamp = System.currentTimeMillis()
                )
            )
        }
    }

    private suspend fun sendMetric(metric: RenderMetric) {
        // 实现实际上报逻辑（HTTP/UDP/Firebase 等）
        // Actual metric reporting implementation
    }
}

data class RenderMetric(
    val eventType: String,
    val payloadVersion: Int,
    val renderDurationMs: Long = 0,
    val payloadSizeBytes: Long = 0,
    val errorCode: String? = null,
    val errorMessage: String? = null,
    val timestamp: Long
)
"""

private const val MONITORING_DASHBOARD_TEMPLATE = """
# Remote Compose 监控仪表盘配置
# Monitoring Dashboard Configuration

# Prometheus + Grafana 是推荐的开源监控栈
# Prometheus + Grafana is the recommended open-source monitoring stack

groups:
  - name: remote_compose_render
    rules:
      # SLO 指标：渲染成功率 > 99.5%
      # SLO Target: render success rate > 99.5%
      - record: remote_compose:render_success_rate:5m
        expr: |
          sum(rate(remote_compose_render_success_total[5m]))
          /
          sum(rate(remote_compose_render_total[5m]))

      # 渲染延迟 P99
      # Render latency P99
      - record: remote_compose:render_latency_p99:5m
        expr: histogram_quantile(0.99,
          sum(rate(remote_compose_render_duration_bucket[5m])) by (le))

      # Payload 大小
      - record: remote_compose:payload_size_kb:p99:5m
        expr: histogram_quantile(0.99,
          sum(rate(remote_compose_payload_size_bytes_bucket[5m])) by (le)) / 1024

# Grafana Dashboard JSON
# SLO 目标：渲染成功率 > 99.5%，P99 渲染延迟 < 2s
# SLO Target: render success rate > 99.5%, P99 render latency < 2s
# - Panel 1: Render Success Rate (Gauge, green > 99.5%)
# - Panel 2: Render Latency P99 (Time series, threshold at 2000ms)
# - Panel 3: Payload Size Distribution (Histogram)
# - Panel 4: Error Rate by Error Code (Pie chart)
"""

private const val FALLBACK_UI_TEMPLATE = """
// ============================================================
// Fallback UI 模板
// Fallback UI Template
// ============================================================
// Fallback UI 使用现有 Material 3 主题，与框架风格一致
// Fallback UI uses existing Material 3 theme, consistent with framework style

@Composable
fun ErrorFallback(
    error: RenderError?,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Warning,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(64.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = error?.displayNameCn ?: "渲染失败",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.error
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = error?.suggestion ?: "请检查网络连接后重试",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(onClick = onRetry) {
            Text("重试 / Retry")
        }
    }
}

@Composable
fun LoadingFallback(
    modifier: Modifier = Modifier
) {
    // isLoading 时显示 CircularProgressIndicator，与框架风格一致
    // isLoading shows CircularProgressIndicator, consistent with framework style
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}
"""

private const val NETWORK_FAILURE_TEMPLATE = """
// ============================================================
// 网络失败处理器
// Network Failure Handler
// ============================================================
// 指数退避重试：首次 1s，第二次 2s，第三次 4s，最多 3 次
// Exponential backoff: 1s, 2s, 4s, max 3 retries

class NetworkFailureHandler(
    private val maxRetries: Int = 3,
    private val baseDelayMs: Long = 1000L
) {
    private val cache = PayloadCache()

    /**
     * 带重试的 Payload 获取
     * Get Payload with retry logic
     */
    suspend fun fetchWithRetry(url: String): Result<SkiaPayload> {
        var lastException: Exception? = null

        repeat(maxRetries) { attempt ->
            try {
                // 缓存优先策略：先展示缓存 Payload，再异步获取新 Payload
                // Cache-first strategy: show cached Payload first, then fetch new one async
                val cached = cache.get(url)
                if (cached != null && attempt == 0) {
                    return Result.success(cached)
                }

                // 执行网络请求
                val fresh = networkClient.fetch(url)
                cache.put(url, fresh)
                return Result.success(fresh)

            } catch (e: Exception) {
                lastException = e
                val delayMs = baseDelayMs * (1 shl attempt)  // 1s, 2s, 4s

                // 所有网络错误统一记录，便于服务端排查
                // All network errors logged uniformly for server-side troubleshooting
                logger.warn("Payload fetch attempt \${'$'}{attempt + 1} failed: \${'$'}{e.message}")

                if (attempt < maxRetries - 1) {
                    delay(delayMs)
                }
            }
        }

        return Result.failure(lastException ?: Exception("Unknown error after $maxRetries retries"))
    }
}
"""

private const val MIGRATION_STEPS_TEMPLATE = """
# 从 JSON Server-Driven UI 迁移到 Remote Compose
# Migration Path: JSON SDUI → Remote Compose

## Phase 1：环境准备（1-2周）
// Phase 1（1-2周）：搭建 Remote Compose 环境，验证最小可行 Payload
// Phase 1 (1-2 weeks): Set up Remote Compose environment, verify minimal viable payload

1. 在项目中添加 Remote Compose 依赖（alpha06）
2. 配置服务器端 Compose 编译环境（JVM Kotlin）
3. 编写最简单的 RemoteComposable（仅一个 Text）
4. 验证完整链路：服务端编译 → Skia 序列化 → HTTP 下发 → 客户端渲染
5. 搭建 CI 流水线（GitHub Actions）

## Phase 2：灰度验证（2-4周）
// Phase 2（2-4周）：灰度 5% 流量，对比 JSON SDUI 与 Remote Compose 性能
// Phase 2 (2-4 weeks): 5% traffic canary, compare JSON SDUI vs Remote Compose performance

1. 选择一个简单页面（仅有文本+图片）作为第一个迁移目标
2. 实现该页面的 RemoteCompose 版本
3. 配置 Feature Flag，灰度 5% 用户
4. 收集指标对比：渲染成功率 / 渲染延迟 / Payload 大小
5. 达到预期后，逐步扩大灰度比例（5% → 20% → 50% → 100%）

## Phase 3：全量迁移（4-8周）
1. 按优先级逐个迁移页面
2. 建立 Remote Compose 组件库（原子组件 + 业务组件）
3. 制定服务器端 DSL 规范和代码审查标准
4. 迁移完成后，下线旧的 JSON SDUI 端点

## Phase 4：持续优化（持续）
// Phase 4（持续）：建立 Remote Compose 组件库，沉淀最佳实践
// Phase 4 (ongoing): Build Remote Compose component library, accumulate best practices

1. 建立 Remote Compose 组件库
2. 沉淀最佳实践：Payload 大小优化 / 渲染性能优化
3. 建设可观测性体系（监控仪表盘 + 告警）
4. 探索 A/B Testing 深度应用
"""
