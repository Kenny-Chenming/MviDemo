package com.mvi.kenny.feature.appfunctionstool

// ================================================================
// AppFunctionsToolContract — Android AppFunctions 开发工具包 MVI 契约
// ================================================================
// MVI architecture contract for Android AppFunctions Development Toolkit.
//
// PRD-214: Android AppFunctions 开发工具包
// Design Reference: memory/agency/designs/PRD-214-Android-AppFunctions-开发工具包.md
//
// MVI 三要素 / Three pillars:
//   Model (State)  — Immutable page state, single source of truth
//   View           — Composable function, consumes State, renders UI
//   Intent         — User intentions, ViewModel processes and updates State
//   Effect         — One-time side effects (navigation, toast), via Channel
// ================================================================

import androidx.compose.ui.graphics.Color

/**
 * ============================================================
 * ExposureLevel — 暴露推荐指数
 * ============================================================
 * AppFunctions 暴露推荐等级。
 *
 * @param displayName 中文显示名称
 * @param emoji Emoji representation
 * @param color 等级颜色
 */
enum class ExposureLevel(
    val displayName: String,
    val emoji: String,
    val color: Color
) {
    /** 高推荐 — 非常适合暴露给 Agent */
    HIGH("高推荐", "🟢", Color(0xFF6BCF7F)),
    /** 中推荐 — 可以暴露，需注意安全 */
    MEDIUM("中推荐", "🟡", Color(0xFFFFD93D)),
    /** 低推荐 — 不建议暴露，风险较高 */
    LOW("低推荐", "🔴", Color(0xFFFF6B6B))
}

/**
 * ============================================================
 * ComplianceStatus — 合规检测状态
 * ============================================================
 *
 * @param displayName 中文显示名称
 * @param emoji Emoji representation
 */
enum class ComplianceStatus(val displayName: String, val emoji: String) {
    PASS("通过", "✅"),
    WARNING("警告", "⚠️"),
    VIOLATION("违规", "❌"),
    UNKNOWN("未知", "⚪")
}

/**
 * ============================================================
 * ErrorType — AppFunction 错误类型
 * ============================================================
 * 四种典型的 AppFunction 执行错误类型。
 */
enum class ErrorType(val displayName: String, val displayNameCn: String) {
    TIMEOUT("Timeout", "超时"),
    PERMISSION_DENIED("Permission Denied", "权限拒绝"),
    INVALID_PARAMS("Invalid Params", "参数错误"),
    SERVICE_UNAVAILABLE("Service Unavailable", "服务不可用")
}

/**
 * ============================================================
 * AppFunctionsToolState — AppFunctions 工具页面状态（MVI State）
 * ============================================================
 * Immutable page state, single source of truth.
 */
data class AppFunctionsToolState(
    // ── Navigation / 全局状态 ────────────────────────────────────
    val selectedTab: Int = 0,

    // ── Tab 1: 策略扫描器 ────────────────────────────────────────
    val sourcePath: String = "",
    val isScanning: Boolean = false,
    val scanProgress: Float = 0f,
    val scanResults: List<ExposureSuggestion> = emptyList(),

    // ── Tab 2: 能力描述器 ────────────────────────────────────────
    val kotlinCode: String = "",
    val generatedJson: String = "",
    val isGeneratingJson: Boolean = false,

    // ── Tab 3: 权限合规检测 ──────────────────────────────────────
    val gradleConfig: String = "",
    val complianceResults: List<ComplianceResult> = emptyList(),
    val isCheckingCompliance: Boolean = false,

    // ── Tab 4: 错误处理模板 ──────────────────────────────────────
    val selectedErrorType: ErrorType = ErrorType.TIMEOUT,
    val generatedTemplate: String = "",

    // ── Tab 5: 决策树 + 调试面板 ─────────────────────────────────
    val debugLogs: List<DebugLogEntry> = emptyList(),
    val simulatedCallParams: String = "",
    val isSimulatingCall: Boolean = false,

    // ── 全局 ────────────────────────────────────────────────────
    val snackbarMessage: String? = null
) {
    companion object {
        val Initial = AppFunctionsToolState()
    }
}

/**
 * ============================================================
 * AppFunctionsToolIntent — 用户意图（User Intent）
 * ============================================================
 * Every user action corresponds to an Intent.
 */
sealed interface AppFunctionsToolIntent {
    data class SelectTab(val index: Int) : AppFunctionsToolIntent
    data class UpdateSourcePath(val path: String) : AppFunctionsToolIntent
    data class StartScan(val simulate: Boolean = false) : AppFunctionsToolIntent
    data class UpdateKotlinCode(val code: String) : AppFunctionsToolIntent
    data object GenerateJson : AppFunctionsToolIntent
    data class UpdateGradleConfig(val config: String) : AppFunctionsToolIntent
    data class CheckCompliance(val simulate: Boolean = false) : AppFunctionsToolIntent
    data class SelectErrorType(val type: ErrorType) : AppFunctionsToolIntent
    data object GenerateTemplate : AppFunctionsToolIntent
    data class UpdateSimulatedParams(val params: String) : AppFunctionsToolIntent
    data object SimulateCall : AppFunctionsToolIntent
    data class CopyToClipboard(val text: String, val label: String) : AppFunctionsToolIntent
    data object DismissSnackbar : AppFunctionsToolIntent
    data object ResetAll : AppFunctionsToolIntent
}

/**
 * ============================================================
 * AppFunctionsToolEffect — 一次性副作用（Effect）
 * ============================================================
 */
sealed interface AppFunctionsToolEffect {
    data class ShowSnackbar(val message: String) : AppFunctionsToolEffect
    data class CopyToClipboard(val content: String, val label: String) : AppFunctionsToolEffect
    data class ShowError(val message: String) : AppFunctionsToolEffect
}

// ================================================================
// 数据模型 / Data Models
// ================================================================

/**
 * ExposureSuggestion — 暴露建议条目
 */
data class ExposureSuggestion(
    val functionSignature: String,
    val functionName: String,
    val exposureLevel: ExposureLevel,
    val exposureReasons: List<String>,
    val cautionReasons: List<String>,
    val appFunctionTemplate: String,
    val filePath: String
)

/**
 * ComplianceResult — 合规检测结果条目
 */
data class ComplianceResult(
    val rule: String,
    val status: ComplianceStatus,
    val message: String,
    val fixSuggestion: String = ""
)

/**
 * DebugLogEntry — 调试日志条目
 */
data class DebugLogEntry(
    val timestamp: String,
    val caller: String,
    val params: String,
    val result: String,
    val durationMs: Long,
    val status: String = "SUCCESS"
)

// ================================================================
// 模拟数据 / Simulation Data
// ================================================================

val SIMULATED_EXPOSURE_SUGGESTIONS = listOf(
    ExposureSuggestion(
        functionSignature = "fun sendMessage(recipientId: String, content: String): Boolean",
        functionName = "sendMessage",
        exposureLevel = ExposureLevel.HIGH,
        exposureReasons = listOf(
            "操作原子性强，返回明确成功/失败结果",
            "参数语义清晰，Agent 容易构造正确参数",
            "无副作用或副作用可控"
        ),
        cautionReasons = emptyList(),
        appFunctionTemplate = """@AppFunction(
    name = "send_message",
    description = "Send a text message to a specified recipient",
    parameters = [
        AppFunctionParameter(name = "recipientId", type = "String", description = "Recipient user ID"),
        AppFunctionParameter(name = "content", type = "String", description = "Message content")
    ],
    returnType = "Boolean",
    actions = ["messaging", "communication"]
)
// TODO: Import from androidx.appfunctions (GA pending)
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
annotation class AppFunction(...)""",
        filePath = "app/src/main/java/com/example/app/MessagingService.kt"
    ),
    ExposureSuggestion(
        functionSignature = "fun createReminder(title: String, datetime: Long, note: String?): Long",
        functionName = "createReminder",
        exposureLevel = ExposureLevel.HIGH,
        exposureReasons = listOf(
            "标准的增删改查操作，适合 Agent 代理执行",
            "时间参数明确，Agent 可根据日程安排设置提醒",
            "返回新创建的提醒 ID，Agent 可验证执行结果"
        ),
        cautionReasons = emptyList(),
        appFunctionTemplate = """@AppFunction(
    name = "create_reminder",
    description = "Create a new reminder with specified time",
    parameters = [
        AppFunctionParameter(name = "title", type = "String", description = "Reminder title"),
        AppFunctionParameter(name = "datetime", type = "Long", description = "Reminder timestamp in ms"),
        AppFunctionParameter(name = "note", type = "String?", description = "Optional note", required = false)
    ],
    returnType = "Long",
    actions = ["productivity", "scheduling"]
)""",
        filePath = "app/src/main/java/com/example/app/ReminderService.kt"
    ),
    ExposureSuggestion(
        functionSignature = "fun searchProducts(query: String, category: String?, maxPrice: Double?): List<Product>",
        functionName = "searchProducts",
        exposureLevel = ExposureLevel.MEDIUM,
        exposureReasons = listOf(
            "搜索类操作是 Agent 的核心用例之一",
            "返回列表结构，Agent 可进一步处理结果"
        ),
        cautionReasons = listOf(
            "大范围搜索可能暴露用户购物偏好数据",
            "建议配合 EXECUTE_APP_FUNCTIONS 权限校验"
        ),
        appFunctionTemplate = """@AppFunction(
    name = "search_products",
    description = "Search products by query with optional filters",
    parameters = [
        AppFunctionParameter(name = "query", type = "String", description = "Search query"),
        AppFunctionParameter(name = "category", type = "String?", description = "Category filter", required = false),
        AppFunctionParameter(name = "maxPrice", type = "Double?", description = "Max price filter", required = false)
    ],
    returnType = "List<Product>",
    actions = ["ecommerce", "search"],
    rateLimit = RateLimit(maxPerMinute = 10)
)""",
        filePath = "app/src/main/java/com/example/app/ProductService.kt"
    ),
    ExposureSuggestion(
        functionSignature = "fun deleteUserData(userId: String): Boolean",
        functionName = "deleteUserData",
        exposureLevel = ExposureLevel.LOW,
        exposureReasons = emptyList(),
        cautionReasons = listOf(
            "高风险操作！删除用户数据是不可逆操作",
            "Agent 误触发可能导致用户数据永久丢失",
            "必须要求用户显式确认，不建议暴露给 Agent",
            "建议：仅暴露导出功能，不暴露删除功能"
        ),
        appFunctionTemplate = """// NOT RECOMMENDED - High risk operation
// fun deleteUserData(userId: String): Boolean
//
// Recommended: expose export function instead
@AppFunction(
    name = "export_user_data",
    description = "Export all user data as JSON archive",
    ...
)""",
        filePath = "app/src/main/java/com/example/app/UserService.kt"
    ),
    ExposureSuggestion(
        functionSignature = "fun getOrderHistory(status: OrderStatus?): List<Order>",
        functionName = "getOrderHistory",
        exposureLevel = ExposureLevel.HIGH,
        exposureReasons = listOf(
            "只读查询操作，安全性高",
            "Agent 可基于订单状态提供购物建议",
            "参数简单，语义明确"
        ),
        cautionReasons = emptyList(),
        appFunctionTemplate = """@AppFunction(
    name = "get_order_history",
    description = "Retrieve user order history with optional status filter",
    parameters = [
        AppFunctionParameter(name = "status", type = "OrderStatus?", description = "Filter by status", required = false)
    ],
    returnType = "List<Order>",
    actions = ["ecommerce", "information_retrieval"]
)""",
        filePath = "app/src/main/java/com/example/app/OrderService.kt"
    ),
    ExposureSuggestion(
        functionSignature = "fun playMedia(mediaId: String, position: Long): Boolean",
        functionName = "playMedia",
        exposureLevel = ExposureLevel.MEDIUM,
        exposureReasons = listOf(
            "媒体控制是智能家居场景的核心需求",
            "Agent 可根据场景（睡眠/工作/娱乐）自动调整"
        ),
        cautionReasons = listOf(
            "自动播放媒体可能干扰用户当前活动",
            "建议 Agent 调用前先检查用户当前状态"
        ),
        appFunctionTemplate = """@AppFunction(
    name = "play_media",
    description = "Play media content at specified position",
    parameters = [
        AppFunctionParameter(name = "mediaId", type = "String", description = "Media content ID"),
        AppFunctionParameter(name = "position", type = "Long", description = "Start position in ms")
    ],
    returnType = "Boolean",
    actions = ["media_control", "entertainment"],
    requiresContext = [UserAvailabilityCheck::class]
)""",
        filePath = "app/src/main/java/com/example/app/MediaService.kt"
    )
)

val SIMULATED_COMPLIANCE_RESULTS = listOf(
    ComplianceResult(
        rule = "EXECUTE_APP_FUNCTIONS 权限声明",
        status = ComplianceStatus.VIOLATION,
        message = "AndroidManifest.xml 中缺少 EXECUTE_APP_FUNCTIONS 权限声明",
        fixSuggestion = """<uses-permission android:name="android.permission.EXECUTE_APP_FUNCTIONS" />
<!-- 此权限仅供需要被 Agent 调用的 App 使用 -->"""
    ),
    ComplianceResult(
        rule = "@AppFunction 注解方法可见性",
        status = ComplianceStatus.PASS,
        message = "所有 @AppFunction 注解的方法均为 public，符合要求"
    ),
    ComplianceResult(
        rule = "AppFunctionContext 身份验证",
        status = ComplianceStatus.WARNING,
        message = "部分 AppFunction 未验证 caller 身份，建议验证 AppFunctionContext.callerIdentity",
        fixSuggestion = """// Verify caller at AppFunction execution start
val callerIdentity = appFunctionContext.callerIdentity
if (callerIdentity == null) {
    throw SecurityException("Unauthorized caller")
}
// Further verify caller is in whitelist
if (!isAuthorizedCaller(callerIdentity)) {
    throw SecurityException("Caller not authorized")
}"""
    ),
    ComplianceResult(
        rule = "敏感数据暴露检查",
        status = ComplianceStatus.PASS,
        message = "AppFunction 参数和返回值中未检测到敏感字段（如密码、支付令牌）"
    ),
    ComplianceResult(
        rule = "隐私政策披露",
        status = ComplianceStatus.WARNING,
        message = "AppFunctions 功能尚未在隐私政策中披露，建议添加",
        fixSuggestion = """// 建议在隐私政策中添加：
// "我们与 AI Agent（如 Google Gemini）共享特定功能，
// 使 Agent 能够代表您执行操作。您可以在设置中
// 管理哪些功能可被 Agent 访问。""""
    ),
    ComplianceResult(
        rule = "Rate Limiting 配置",
        status = ComplianceStatus.WARNING,
        message = "高风险 AppFunction 未配置 rate limiting，建议添加防止滥用",
        fixSuggestion = """@AppFunction(
    name = "sensitive_operation",
    rateLimit = RateLimit(maxPerMinute = 5, maxPerHour = 50)
)"""
    )
)

// Error templates — $ is escaped as \$\$ to prevent Kotlin string interpolation
val ERROR_TEMPLATE_MAP = mapOf(
    ErrorType.TIMEOUT to """
// ============================================================
// AppFunction Timeout Error Handling Template
// ============================================================
// Timeout: Agent's execution deadline is reached before
// AppFunction completes. Typical timeout: 30-60 seconds.
//
// Strategy: Implement timeout at function level, return partial
// results or a clear "incomplete" status instead of failing.
//
// Reference: developer.android.com/ai/appfunctions (2026-02)
// ============================================================

/**
 * AppFunction with timeout handling.
 * @param context Execution context provided by the Agent
 * @return Result containing data or timeout status
 */
// TODO: Import from androidx.appfunctions when GA
@AppFunction(
    name = "data_fetch_with_timeout",
    description = "Fetch data with automatic timeout handling",
    timeoutSeconds = 30
)
suspend fun fetchDataWithTimeout(
    context: AppFunctionContext,
    query: String
): AppFunctionResult<DataResponse> {
    return try {
        val result = withTimeoutOrNull(30_000L) {
            val data = dataRepository.fetch(query)
            AppFunctionResult.Success(data)
        } ?: AppFunctionResult.Timeout(
            message = "Operation timed out after 30 seconds",
            partialData = null
        )
    } catch (e: CancellationException) {
        AppFunctionResult.Timeout(
            message = "Operation cancelled (timeout)",
            partialData = null
        )
    } catch (e: Exception) {
        AppFunctionResult.Failure(
            errorCode = "FETCH_ERROR",
            message = "Unexpected error: \$\$\{e.message\}"
        )
    }
}

/*
 * Agent-side timeout handling:
 * 1. Inform user operation took too long
 * 2. Offer to retry with simpler query, OR
 * 3. Suggest breaking into smaller steps
 */
""".trimIndent(),

    ErrorType.PERMISSION_DENIED to """
// ============================================================
// AppFunction Permission Denied Error Handling Template
// ============================================================
// Permission denied: calling Agent doesn't hold the required
// EXECUTE_APP_FUNCTIONS permission or specific AppFunction
// permission.
//
// Reference: developer.android.com/ai/appfunctions (2026-02)
// ============================================================

/**
 * AppFunction with permission verification.
 * @param context Execution context provided by the Agent
 * @return Result or permission error
 */
// TODO: Import from androidx.appfunctions when GA
@AppFunction(
    name = "privileged_operation",
    description = "Privileged operation requiring specific permissions",
    requiredPermissions = ["android.permission.ACCESS_FINE_LOCATION"]
)
suspend fun privilegedOperation(
    context: AppFunctionContext
): AppFunctionResult<LocationData> {
    // Step 1: Verify EXECUTE_APP_FUNCTIONS permission (auto-checked by framework,
    // but explicit check adds security layer)
    if (!context.hasPermission("android.permission.EXECUTE_APP_FUNCTIONS")) {
        return AppFunctionResult.PermissionDenied(
            requiredPermission = "android.permission.EXECUTE_APP_FUNCTIONS",
            message = "Calling Agent does not hold EXECUTE_APP_FUNCTIONS permission"
        )
    }

    // Step 2: Verify AppFunction-specific permissions
    if (!context.hasPermission("android.permission.ACCESS_FINE_LOCATION")) {
        return AppFunctionResult.PermissionDenied(
            requiredPermission = "android.permission.ACCESS_FINE_LOCATION",
            message = "This AppFunction requires ACCESS_FINE_LOCATION"
        )
    }

    // Step 3: Verify caller identity (recommended)
    val callerIdentity = context.callerIdentity
    if (callerIdentity == null) {
        return AppFunctionResult.PermissionDenied(
            requiredPermission = "android.permission.EXECUTE_APP_FUNCTIONS",
            message = "Unable to verify caller identity"
        )
    }

    // Step 4: Optional - verify caller in whitelist
    if (!isAuthorizedCaller(callerIdentity)) {
        return AppFunctionResult.PermissionDenied(
            requiredPermission = "android.permission.EXECUTE_APP_FUNCTIONS",
            message = "Caller \$\$\{callerIdentity.packageName\} is not authorized"
        )
    }

    // Permission checks passed - proceed
    return try {
        val location = locationManager.getCurrentLocation()
        AppFunctionResult.Success(location)
    } catch (e: SecurityException) {
        AppFunctionResult.PermissionDenied(
            requiredPermission = "android.permission.ACCESS_FINE_LOCATION",
            message = "Permission revoked during execution"
        )
    }
}

/*
 * Agent-side handling (do NOT retry automatically):
 * 1. Log the unauthorized call attempt
 * 2. Inform user additional permissions are required
 */

/*
 * Optional whitelist-based caller authorization.
 */
private fun isAuthorizedCaller(identity: CallerIdentity): Boolean {
    val authorizedAgents = listOf(
        "com.google.android.apps.search.agent",   // Google Gemini
        "com.example.my.custom.agent"             // Your custom agent
    )
    return identity.packageName in authorizedAgents
}
""".trimIndent(),

    ErrorType.INVALID_PARAMS to """
// ============================================================
// AppFunction Invalid Parameters Error Handling Template
// ============================================================
// Invalid params: Agent provides parameters that don't match
// the AppFunction's parameter schema or constraints.
//
// Strategy: Validate early, return structured error with details
// and provide parameter schema in error response.
//
// Reference: developer.android.com/ai/appfunctions (2026-02)
// ============================================================

/**
 * AppFunction with comprehensive parameter validation.
 * @param context Execution context provided by the Agent
 * @return Result or validation error with schema
 */
// TODO: Import from androidx.appfunctions when GA
@AppFunction(
    name = "create_appointment",
    description = "Create a calendar appointment",
    parameters = [
        AppFunctionParameter(
            name = "title",
            type = "String",
            description = "Appointment title (1-100 chars)",
            constraints = ["minLength: 1", "maxLength: 100"]
        ),
        AppFunctionParameter(
            name = "datetime",
            type = "Long",
            description = "Appointment time in ms since epoch",
            constraints = ["min: 1746400000000"]  // Example future timestamp
        ),
        AppFunctionParameter(
            name = "durationMinutes",
            type = "Int",
            description = "Duration in minutes",
            constraints = ["min: 5", "max: 480"]
        )
    ]
)
suspend fun createAppointment(
    context: AppFunctionContext,
    title: String,
    datetime: Long,
    durationMinutes: Int
): AppFunctionResult<Appointment> {
    val validationErrors = mutableListOf<ValidationError>()

    // Validate title
    if (title.isBlank()) {
        validationErrors.add(ValidationError(
            parameter = "title",
            error = "Title cannot be blank",
            providedValue = "¥"¥"¥"¥"¥"¥"(blank)¥"¥"¥"¥"¥"¥""
        ))
    } else if (title.length > 100) {
        validationErrors.add(ValidationError(
            parameter = "title",
            error = "Title exceeds 100 characters",
            providedValue = "title.length chars (max: 100)"
        ))
    }

    // Validate datetime - must be in the future
    val now = System.currentTimeMillis()
    if (datetime <= now) {
        validationErrors.add(ValidationError(
            parameter = "datetime",
            error = "Appointment time must be in the future",
            providedValue = "datetime (current: now)"
        ))
    }

    // Validate duration
    if (durationMinutes < 5) {
        validationErrors.add(ValidationError(
            parameter = "durationMinutes",
            error = "Duration must be at least 5 minutes",
            providedValue = "durationMinutes (min: 5)"
        ))
    } else if (durationMinutes > 480) {
        validationErrors.add(ValidationError(
            parameter = "durationMinutes",
            error = "Duration cannot exceed 8 hours",
            providedValue = "durationMinutes (max: 480)"
        ))
    }

    // Return structured InvalidParams with schema
    if (validationErrors.isNotEmpty()) {
        return AppFunctionResult.InvalidParams(
            message = "Parameter validation failed",
            errors = validationErrors,
            parameterSchema = mapOf(
                "title" to mapOf("type" to "String", "minLength" to 1, "maxLength" to 100),
                "datetime" to mapOf("type" to "Long", "min" to "future_timestamp"),
                "durationMinutes" to mapOf("type" to "Int", "min" to 5, "max" to 480)
            )
        )
    }

    // All validations passed
    return try {
        val appointment = calendarService.create(
            title = title,
            startTime = datetime,
            endTime = datetime + (durationMinutes * 60 * 1000L)
        )
        AppFunctionResult.Success(appointment)
    } catch (ex: Exception) {
        AppFunctionResult.Failure(
            errorCode = "CALENDAR_ERROR",
            message = "Failed to create appointment: ¥{ex.message}"
        )
    }
}

/*
 * Agent-side handling when InvalidParams received:
 * 1. Parse ValidationError list and parameterSchema
 * 2. Ask user to provide corrected values
 * 3. Re-invoke with corrected parameters
 * 4. After 3 failed attempts, inform user of issue
 */
""".trimIndent(),

    ErrorType.SERVICE_UNAVAILABLE to """
// ============================================================
// AppFunction Service Unavailable Error Handling Template
// ============================================================
// Service unavailable: AppFunction cannot execute because a
// required backend service (network, database, etc.) is down.
//
// Strategy: Distinguish between temporary/permanent unavailability,
// implement retry logic, provide clear status to Agent.
//
// Reference: developer.android.com/ai/appfunctions (2026-02)
// ============================================================

/**
 * AppFunction with service availability check and retry.
 * @param context Execution context provided by the Agent
 * @return Result or service unavailability error
 */
// TODO: Import from androidx.appfunctions when GA
@AppFunction(
    name = "fetch_realtime_data",
    description = "Fetch real-time data from remote API",
    retryPolicy = RetryPolicy(maxAttempts = 3, backoffMillis = 1000)
)
suspend fun fetchRealtimeData(
    context: AppFunctionContext,
    dataType: String
): AppFunctionResult<DataPayload> {
    // Check service availability before attempting operation
    val availability = checkServiceAvailability()

    when (availability.status) {
        ServiceStatus.UNAVAILABLE -> {
            return AppFunctionResult.ServiceUnavailable(
                serviceName = availability.serviceName,
                message = availability.message,
                estimatedRecoveryTime = availability.estimatedRecoveryTime,
                alternatives = listOf(
                    AppFunctionAlternative(
                        name = "fetch_cached_data",
                        description = "Use cached data as fallback"
                    ),
                    AppFunctionAlternative(
                        name = "notify_when_available",
                        description = "Notify when service recovers"
                    )
                )
            )
        }
        ServiceStatus.DEGRADED -> {
            context.logWarning("Service \$\$\{availability.serviceName\} is degraded")
        }
        ServiceStatus.AVAILABLE -> { /* proceed */ }
    }

    // Retry loop
    var lastError: Exception? = null
    repeat(3) { attempt ->
        try {
            val data = apiService.fetch(dataType)
            return AppFunctionResult.Success(data)
        } catch (ex: ServiceException) when (ex.type) {
            ServiceException.Type.TIMEOUT -> {
                lastError = ex
                if (attempt < 2) delay((1L shl attempt) * 1000)  // 1s, 2s, 4s
            }
            ServiceException.Type.CONNECTION_ERROR -> {
                lastError = ex
                if (attempt < 2) delay((1L shl attempt) * 1000)
            }
            else -> throw ex  // Non-retryable
        }
    }

    // All retries exhausted
    return AppFunctionResult.ServiceUnavailable(
        serviceName = "RemoteDataService",
        message = "Service failed after 3 attempts: ¥{lastError?.message}",
        estimatedRecoveryTime = null,
        alternatives = listOf(
            AppFunctionAlternative(
                name = "fetch_cached_data",
                description = "Use locally cached data (max age: 24h)"
            )
        )
    )
}

/*
 * Agent-side handling when ServiceUnavailable received:
 * 1. Check alternatives - if provided, use first alternative
 * 2. If no alternatives, wait for estimatedRecoveryTime
 * 3. If no ETA, ask user whether to retry or use fallback
 * 4. After service recovery, re-invoke automatically
 */
""".trimIndent()
)

/**
 * ============================================================
 * DECISION_MATRIX — AppFunctions vs Deep Links vs Shortcuts 决策矩阵
 * ============================================================
 */
data class DecisionItem(
    val dimension: String,
    val appFunctions: String,
    val deepLinks: String,
    val shortcuts: String
)

val DECISION_MATRIX = listOf(
    DecisionItem(
        dimension = "触发方式",
        appFunctions = "AI Agent 自动调用，无需用户操作",
        deepLinks = "用户点击链接（手动）",
        shortcuts = "用户长按 App 图标（手动）"
    ),
    DecisionItem(
        dimension = "用户意图推断",
        appFunctions = "Agent 理解自然语言意图并调用",
        deepLinks = "需要 URL scheme，不支持自然语言",
        shortcuts = "固定快捷方式，不支持动态参数"
    ),
    DecisionItem(
        dimension = "参数传递",
        appFunctions = "结构化参数，Agent 自动填充",
        deepLinks = "URL query parameters，手动构造",
        shortcuts = "固定参数，不支持动态内容"
    ),
    DecisionItem(
        dimension = "权限模型",
        appFunctions = "EXECUTE_APP_FUNCTIONS 权限",
        deepLinks = "无需特殊权限",
        shortcuts = "无需特殊权限"
    ),
    DecisionItem(
        dimension = "适用场景",
        appFunctions = "Agent 代表的自动化操作",
        deepLinks = "跨 App 导航、社交分享",
        shortcuts = "快速访问高频功能"
    ),
    DecisionItem(
        dimension = "Google I/O 2026",
        appFunctions = "核心新功能，AppFunctions GA",
        deepLinks = "稳定维护",
        shortcuts = "稳定维护"
    ),
    DecisionItem(
        dimension = "隐私保护",
        appFunctions = "需显式授权，可撤回",
        deepLinks = "用户主动触发，风险低",
        shortcuts = "用户主动触发，风险低"
    ),
    DecisionItem(
        dimension = "多轮对话支持",
        appFunctions = "Agent 可在对话中调用多个 AppFunction",
        deepLinks = "单次跳转，无状态",
        shortcuts = "单次启动，无多轮"
    )
)
