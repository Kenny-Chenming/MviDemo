// ================================================================
// CallGraphBuilder — 调用图构建器
// Call Graph Builder for Audio API Call Stack Tracing
// ================================================================
// PRD-306: Android 17 后台音频 Foreground Service 迁移检测与适配工具包
//
// 构建从 Application 到违规调用的简化调用链。
// Builds simplified call chain from Application to violating call.
// ================================================================

package com.mvi.kenny.audiobackground.engine

import java.io.File

/**
 * 调用栈节点
 * Call Stack Node
 *
 * @param functionName 函数名
 * @param file 文件路径
 * @param line 行号
 * @param isSuspend 是否是挂起函数
 */
data class CallStackNode(
    val functionName: String,
    val file: String,
    val line: Int,
    val isSuspend: Boolean = false
)

/**
 * 调用图构建器
 * Call Graph Builder
 *
 * 简化实现：基于正则表达式提取函数定义和调用关系。
 * Simple implementation: extracts function definitions and calls via regex.
 */
class CallGraphBuilder {

    /**
     * 构建简化调用栈
     * Build simplified call stack
     *
     * 从目标行向上搜索，查找包含该行的函数定义。
     * Searches upward from target line to find function definitions containing it.
     *
     * @param fileContent 文件内容
     * @param targetLine 目标行号
     * @param maxDepth 最大搜索深度
     * @return 调用栈列表（从内到外）
     */
    fun buildSimpleCallStack(
        fileContent: String,
        targetLine: Int,
        maxDepth: Int = 5
    ): List<String> {
        val lines = fileContent.lines()
        if (targetLine < 1 || targetLine > lines.size) return emptyList()

        val callStack = mutableListOf<String>()
        var currentLine = targetLine - 1  // 0-indexed

        // 向上搜索函数定义
        // Search upward for function definitions
        while (currentLine >= 0 && callStack.size < maxDepth) {
            val line = lines[currentLine]

            // Kotlin 函数定义
            val kotlinFun = extractKotlinFunction(line)
            if (kotlinFun != null) {
                callStack.add("${kotlinFun.name}() at line ${currentLine + 1}")
            }

            // Java 方法定义
            val javaMethod = extractJavaMethod(line)
            if (javaMethod != null) {
                callStack.add("${javaMethod}() at line ${currentLine + 1}")
            }

            currentLine--
        }

        return callStack.take(maxDepth)
    }

    /**
     * 提取 Kotlin 函数定义
     * Extract Kotlin function definition
     */
    private fun extractKotlinFunction(line: String): KotlinFunctionInfo? {
        // 匹配 fun functionName 或 suspend fun functionName
        // Match fun functionName or suspend fun functionName
        val pattern = Regex("""(suspend\s+)?fun\s+(\w+)\s*(<[^>]+>)?\s*\(""")
        val match = pattern.find(line) ?: return null

        val isSuspend = match.groupValues[1].isNotEmpty()
        val funName = match.groupValues[2]

        return KotlinFunctionInfo(funName, isSuspend)
    }

    /**
     * 提取 Java 方法定义
     * Extract Java method definition
     */
    private fun extractJavaMethod(line: String): String? {
        // 匹配返回类型 方法名( 或 访问修饰符 返回类型 方法名(
        // Match returnType methodName( or accessModifier returnType methodName(
        val pattern = Regex(
            """(public|protected|private)?\s*(static)?\s*(\w+)\s+(\w+)\s*\(""",
            RegexOption.IGNORE_CASE
        )
        val match = pattern.find(line) ?: return null

        val methodName = match.groupValues[4]
        // 排除构造函数和字段
        // Exclude constructors and fields
        if (methodName == "if" || methodName == "while" || methodName == "for") {
            return null
        }

        return methodName
    }

    /**
     * Kotlin 函数信息
     * Kotlin Function Info
     */
    private data class KotlinFunctionInfo(
        val name: String,
        val isSuspend: Boolean
    )

    /**
     * 构建完整调用图
     * Build complete call graph
     *
     * 分析整个文件，构建函数调用关系图。
     * Analyzes entire file to build function call relationship graph.
     *
     * @param file Kotlin/Java source file
     * @return 函数定义 map（函数名 -> CallStackNode）
     */
    fun buildCallGraph(file: File): Map<String, CallStackNode> {
        val callGraph = mutableMapOf<String, CallStackNode>()
        val content = try {
            file.readText()
        } catch (e: Exception) {
            return emptyMap()
        }

        val lines = content.lines()
        lines.forEachIndexed { index, line ->
            val kotlinFun = extractKotlinFunction(line)
            if (kotlinFun != null) {
                callGraph[kotlinFun.name] = CallStackNode(
                    functionName = kotlinFun.name,
                    file = file.path,
                    line = index + 1,
                    isSuspend = kotlinFun.isSuspend
                )
            }

            val javaMethod = extractJavaMethod(line)
            if (javaMethod != null) {
                callGraph[javaMethod] = CallStackNode(
                    functionName = javaMethod,
                    file = file.path,
                    line = index + 1,
                    isSuspend = false
                )
            }
        }

        return callGraph
    }
}
