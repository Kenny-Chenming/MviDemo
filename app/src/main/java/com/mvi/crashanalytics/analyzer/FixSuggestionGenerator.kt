package com.mvi.crashanalytics.analyzer

import com.mvi.crashanalytics.CrashReport

/**
 * Code fix suggestion generator
 */
object FixSuggestionGenerator {

    data class CodeFix(
        val type: FixType,
        val snippet: String,
        val explanation: String,
        val fileHint: String?
    )

    enum class FixType {
        NULL_CHECK,
        LIFECYCLE_GUARD,
        THREAD_SWITCH,
        MEMORY_OPTIMIZE,
        PERMISSION_GUARD,
        STATE_RESET,
        COROUTINE_SCOPE
    }

    /**
     * Generate specific code fix suggestions based on crash context
     */
    fun generateFix(report: CrashReport, analysis: RootCauseAnalysis): List<CodeFix> {
        val fixes = mutableListOf<CodeFix>()
        val frame = analysis.likelyFrame

        when (analysis.category) {
            CrashCategory.NULL_POINTER -> {
                fixes.add(CodeFix(
                    type = FixType.NULL_CHECK,
                    snippet = """
                        // Before (unsafe)
                        val length = obj.field.length

                        // After (safe)
                        val length = obj?.field?.length ?: 0
                    """.trimIndent(),
                    explanation = "使用安全调用操作符避免空指针崩溃",
                    fileHint = frame?.fileName
                ))
                fixes.add(CodeFix(
                    type = FixType.NULL_CHECK,
                    snippet = """
                        // Use let for non-null execution
                        obj?.let {
                            doSomething(it.field)
                        } ?: run {
                            Log.w("Tag", "obj is null, skipped")
                        }
                    """.trimIndent(),
                    explanation = "使用 let/run 块处理空值分支",
                    fileHint = frame?.fileName
                ))
            }

            CrashCategory.THREAD_ISSUE -> {
                fixes.add(CodeFix(
                    type = FixType.THREAD_SWITCH,
                    snippet = """
                        // Wrong: network on main thread
                        val result = api.fetchData()

                        // Correct: use coroutine
                        viewModelScope.launch(Dispatchers.IO) {
                            val result = api.fetchData()
                            withContext(Dispatchers.Main) {
                                _state.value = UIState.Success(result)
                            }
                        }
                    """.trimIndent(),
                    explanation = "使用 Dispatchers.IO 执行耗时操作，Dispatchers.Main 更新 UI",
                    fileHint = frame?.fileName
                ))
                fixes.add(CodeFix(
                    type = FixType.UI_THREAD_GUARD,
                    snippet = """
                        // Safe UI update
                        binding.root.post {
                            if (isValid()) {
                                updateUI()
                            }
                        }
                    """.trimIndent(),
                    explanation = "使用 view.post 确保在主线程更新 UI",
                    fileHint = frame?.fileName
                ))
            }

            CrashCategory.ILLEGAL_STATE -> {
                fixes.add(CodeFix(
                    type = FixType.STATE_RESET,
                    snippet = """
                        sealed class ViewState {
                            object Loading : ViewState()
                            data class Success(val data: Data) : ViewState()
                            data class Error(val msg: String) : ViewState()
                        }

                        // Guard before operation
                        private fun onAction(action: Action) {
                            val currentState = state.value
                            if (currentState !is ViewState.Success) {
                                Log.w("Tag", "Action $action ignored in state $currentState")
                                return
                            }
                            // safe to proceed
                        }
                    """.trimIndent(),
                    explanation = "使用 sealed class 管理状态，状态转换前做校验",
                    fileHint = frame?.fileName
                ))
            }

            CrashCategory.MEMORY_ISSUE -> {
                fixes.add(CodeFix(
                    type = FixType.MEMORY_OPTIMIZE,
                    snippet = """
                        // Before: loading large bitmap
                        val bitmap = BitmapFactory.decodeFile(path)

                        // After: sample down before loading
                        val options = BitmapFactory.Options().apply {
                            inJustDecodeBounds = true
                        }
                        BitmapFactory.decodeFile(path, options)
                        options.inSampleSize = calculateInSampleSize(options, 1024, 1024)
                        options.inJustDecodeBounds = false
                        val bitmap = BitmapFactory.decodeFile(path, options)
                    """.trimIndent(),
                    explanation = "使用 inSampleSize 采样压缩大图片，避免 OOM",
                    fileHint = frame?.fileName
                ))
            }

            CrashCategory.PERMISSION -> {
                fixes.add(CodeFix(
                    type = FixType.PERMISSION_GUARD,
                    snippet = """
                        // Use ActivityResultLauncher
                        private val permissionLauncher = registerForActivityResult(
                            ActivityResultContracts.RequestMultiplePermissions()
                        ) { permissions ->
                            val allGranted = permissions.all { it.value }
                            if (allGranted) {
                                proceedWithOperation()
                            } else {
                                showPermissionDeniedUI()
                            }
                        }

                        fun checkAndRequest() {
                            if (hasRequiredPermissions()) {
                                proceedWithOperation()
                            } else {
                                permissionLauncher.launch(requiredPermissions)
                            }
                        }
                    """.trimIndent(),
                    explanation = "使用 ActivityResultContracts 申请权限，有结果回调后再操作",
                    fileHint = frame?.fileName
                ))
            }

            else -> {
                fixes.add(CodeFix(
                    type = FixType.LIFECYCLE_GUARD,
                    snippet = """
                        // Guard with lifecycle state
                        if (!isAdded || isDetached) {
                            Log.w("Tag", "Fragment detached, skipping operation")
                            return
                        }
                    """.trimIndent(),
                    explanation = "在 Fragment/Activity 已 detach 后避免操作视图",
                    fileHint = frame?.fileName
                ))
            }
        }

        return fixes.take(2)
    }
}
