package com.mvi.kenny.feature.migrationtoolkit

// ================================================================
// MigrationToolkitViewModel — Material Views → Compose 迁移工具包 MVI ViewModel
// ================================================================
// ViewModel for MDC-Android Views → Compose Migration Toolkit.
//
// PRD-266: Material Views → Compose 迁移工具包
// Implements MVI pattern: Intent → ViewModel → State/Effect
//
// Key responsibilities:
//   - Display 10 tool cards on homepage (ASSESSMENT / CONVERTER / GUIDE)
//   - Assessment: scan XML layouts, calculate complexity score, generate priority recommendations
//   - Converter: convert XML → Compose code for high-frequency components
//   - Mapping: provide searchable MDC Views → Compose component mapping table
//   - Theming: guide colors.xml/dimens.xml/strings.xml → Compose Theme
//   - Hybrid: provide progressive migration checklist for mixed apps
//   - M2→M3: Material 2 → Material 3 breaking changes guide
//   - MDC Maintenance: explain MDC-Android maintenance mode SLA and timeline
//   - Expose one-time Effects (snackbar, clipboard, share, navigation)
// ================================================================

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.w3c.dom.Document
import org.w3c.dom.Element
import javax.xml.parsers.DocumentBuilderFactory

/**
 * ============================================================
 * MigrationToolkitViewModel — 迁移工具包 ViewModel
 * ============================================================
 * Manages the MigrationToolkitState and processes MigrationToolkitIntent.
 *
 * @see MigrationToolkitState
 * @see MigrationToolkitIntent
 * @see MigrationToolkitEffect
 */
class MigrationToolkitViewModel : ViewModel() {

    // ─────────────────────────────────────────────────────────────
    // State — Single source of truth, exposed as immutable StateFlow
    // ─────────────────────────────────────────────────────────────
    private val _state = MutableStateFlow(MigrationToolkitState.Initial)
    val state: StateFlow<MigrationToolkitState> = _state.asStateFlow()

    // ─────────────────────────────────────────────────────────────
    // Effect — One-time events via SharedFlow
    // ─────────────────────────────────────────────────────────────
    private val _effect = MutableSharedFlow<MigrationToolkitEffect>()
    val effect = _effect.asSharedFlow()

    init {
        // Load default tool cards on init
        _state.update { it.copy(tools = DEFAULT_TOOL_CARDS) }
    }

    // ─────────────────────────────────────────────────────────────
    // Public API for UI to send intents
    // ─────────────────────────────────────────────────────────────
    fun sendIntent(intent: MigrationToolkitIntent) {
        viewModelScope.launch {
            when (intent) {
                is MigrationToolkitIntent.SelectTab -> handleSelectTab(intent.tab)
                is MigrationToolkitIntent.SelectTool -> handleSelectTool(intent.tool)
                is MigrationToolkitIntent.StartAssessment -> handleStartAssessment(intent.uris)
                is MigrationToolkitIntent.SelectXmlFile -> handleSelectXmlFile(intent.uri)
                is MigrationToolkitIntent.InputXml -> handleInputXml(intent.xml)
                is MigrationToolkitIntent.ConvertXml -> handleConvertXml(intent.xml)
                is MigrationToolkitIntent.CopyCode -> handleCopyCode(intent.code)
                is MigrationToolkitIntent.ExportReport -> handleExportReport(intent.report)
                is MigrationToolkitIntent.DismissSnackbar -> handleDismissSnackbar()
                is MigrationToolkitIntent.ResetAll -> handleResetAll()
            }
        }
    }

    // ─────────────────────────────────────────────────────────────
    // Intent Handlers
    // ─────────────────────────────────────────────────────────────

    private fun handleSelectTab(tab: MigrationTab) {
        _state.update { it.copy(selectedTab = tab) }
    }

    /**
     * Handle tool card selection — navigate to corresponding tool page.
     *
     * @param tool Selected tool card
     */
    private suspend fun handleSelectTool(tool: ToolCard) {
        when (tool.id) {
            "assessment" -> _effect.emit(MigrationToolkitEffect.NavigateToRoute("migration_toolkit_assessment"))
            "converter" -> _effect.emit(MigrationToolkitEffect.NavigateToRoute("migration_toolkit_converter"))
            "mapping" -> _effect.emit(MigrationToolkitEffect.NavigateToRoute("migration_toolkit_mapping"))
            "theme" -> _effect.emit(MigrationToolkitEffect.NavigateToRoute("migration_toolkit_theme"))
            "hybrid" -> _effect.emit(MigrationToolkitEffect.NavigateToRoute("migration_toolkit_hybrid"))
            "mdc-maintenance" -> _effect.emit(MigrationToolkitEffect.NavigateToRoute("migration_toolkit_mdc_maintenance"))
            "m2-m3" -> _effect.emit(MigrationToolkitEffect.NavigateToRoute("migration_toolkit_m2_m3"))
            "expressive" -> _effect.emit(MigrationToolkitEffect.NavigateToRoute("migration_toolkit_expressive"))
            "dynamic-color" -> _effect.emit(MigrationToolkitEffect.NavigateToRoute("migration_toolkit_dynamic_color"))
            "styles-api" -> _effect.emit(MigrationToolkitEffect.NavigateToRoute("migration_toolkit_styles_api"))
            else -> _effect.emit(MigrationToolkitEffect.ShowSnackbar("工具「${tool.name}」正在开发中"))
        }
    }

    /**
     * Start assessment — scan XML layouts and generate complexity report.
     * In a real implementation, this would parse actual XML files.
     *
     * @param uris XML file URIs selected by user
     */
    private suspend fun handleStartAssessment(uris: List<Uri>) {
        _state.update {
            it.copy(
                assessmentState = it.assessmentState.copy(
                    isScanning = true,
                    selectedFiles = uris
                )
            )
        }

        withContext(Dispatchers.Default) {
            // Simulate scanning with realistic delay
            delay(2000)

            // In production, this would parse actual XML files:
            // val scanResult = parseXmlFiles(uris)
            val scanResult = SIMULATED_SCAN_RESULT

            _state.update {
                it.copy(
                    assessmentState = it.assessmentState.copy(
                        isScanning = false,
                        scanResult = scanResult
                    )
                )
            }

            _effect.emit(
                MigrationToolkitEffect.ShowSnackbar(
                    "✅ 评估完成！发现 ${scanResult.totalViews} 个 Views，复杂度 ${"⭐".repeat(scanResult.complexityScore)}"
                )
            )
        }
    }

    /**
     * Select XML file — read file content and populate converter.
     *
     * @param uri Selected file URI
     */
    private suspend fun handleSelectXmlFile(uri: Uri) {
        // In production: read from ContentResolver
        _state.update {
            it.copy(
                converterState = it.converterState.copy(
                    sourceXml = getSampleXml()
                )
            )
        }
    }

    /**
     * Input XML — update source XML content.
     *
     * @param xml XML content
     */
    private fun handleInputXml(xml: String) {
        _state.update {
            it.copy(
                converterState = it.converterState.copy(sourceXml = xml)
            )
        }
    }

    /**
     * Convert XML to Compose code — parse XML and generate Compose code.
     * In production, this would use actual XML parsing and code generation.
     *
     * @param xml XML layout content
     */
    private suspend fun handleConvertXml(xml: String) {
        _state.update {
            it.copy(
                converterState = it.converterState.copy(isConverting = true)
            )
        }

        withContext(Dispatchers.Default) {
            delay(1500)

            val converted = convertXmlToCompose(xml)

            _state.update {
                it.copy(
                    converterState = it.converterState.copy(
                        isConverting = false,
                        convertedCode = converted,
                        conversionError = null
                    )
                )
            }

            _effect.emit(MigrationToolkitEffect.ShowSnackbar("✅ 转换完成！请检查预览区域"))
        }
    }

    /**
     * Copy code to clipboard.
     *
     * @param code Code content to copy
     */
    private suspend fun handleCopyCode(code: String) {
        _effect.emit(MigrationToolkitEffect.CopyToClipboard(code))
        _effect.emit(MigrationToolkitEffect.ShowSnackbar("代码已复制到剪贴板"))
    }

    /**
     * Export assessment report as Markdown.
     *
     * @param report Report content
     */
    private suspend fun handleExportReport(report: String) {
        _effect.emit(MigrationToolkitEffect.ShareReport(report))
    }

    private fun handleDismissSnackbar() {
        _state.update {
            it.copy(
                snackbarMessage = null,
                assessmentState = it.assessmentState.copy(isScanning = false),
                converterState = it.converterState.copy(isConverting = false)
            )
        }
    }

    private fun handleResetAll() {
        _state.update { MigrationToolkitState.Initial.copy(tools = DEFAULT_TOOL_CARDS) }
    }

    // ─────────────────────────────────────────────────────────────
    // XML → Compose Conversion Logic
    // ─────────────────────────────────────────────────────────────

    /**
     * Convert XML layout to Compose code.
     * Handles high-frequency components: Button, TextView, LinearLayout, etc.
     *
     * @param xml Raw XML layout string
     * @return Generated Compose Composable code
     */
    private fun convertXmlToCompose(xml: String): String {
        return try {
            val doc = parseXml(xml)
            val root = doc.documentElement
            val rootTag = root.tagName

            val generatedCode = StringBuilder()
            generatedCode.appendLine("@Composable")
            generatedCode.appendLine("fun ${getComposableName(rootTag)}() {")
            generatedCode.appendLine("    // Generated from XML: ${rootTag}")
            generatedCode.appendLine("    // ⚠️ Review and adjust before production use")
            generatedCode.appendLine()

            convertElement(root, generatedCode, indent = "    ")

            generatedCode.appendLine("}")

            // If root is a layout container, wrap with appropriate Compose container
            val wrapper = when (rootTag) {
                "LinearLayout" -> {
                    val orientation = root.getAttribute("android:orientation") ?: "vertical"
                    if (orientation == "horizontal") "Row" else "Column"
                }
                "FrameLayout" -> "Box"
                "RelativeLayout" -> "Box"
                "ConstraintLayout" -> "Box" // or ConstraintLayout Compose
                else -> "Column"
            }

            val composableName = getComposableName(rootTag)
            val modifier = getModifiers(root)

            """
@Composable
fun ${composableName}Screen(modifier: Modifier = Modifier) {
    // ============================================================
    // ⚠️  Auto-generated Compose code — review before production!
    // ============================================================
    // Source: $rootTag
    // Container: $wrapper
    // ============================================================

    $wrapper(
        modifier = modifier$modifier
    ) {
${generatedCode.toString().indentForCompose(4)}
    }
}
""".trimIndent()

        } catch (e: Exception) {
            """
// ❌ Conversion failed: ${e.message}
// Please check your XML syntax and try again.
//
// Common issues:
//   - Unclosed tags
//   - Invalid attribute names
//   - Unsupported namespace prefixes
            """.trimIndent()
        }
    }

    /**
     * Parse XML string into DOM Document.
     */
    private fun parseXml(xml: String): Document {
        val factory = DocumentBuilderFactory.newInstance()
        factory.isNamespaceAware = false
        val builder = factory.newDocumentBuilder()
        return builder.parse(xml.byteInputStream())
    }

    /**
     * Recursively convert an XML element to Compose code.
     */
    private fun convertElement(element: Element, sb: StringBuilder, indent: String) {
        val tagName = element.tagName
        val id = element.getAttribute("android:id")?.let { extractId(it) }

        when (tagName) {
            "TextView" -> {
                val text = element.getAttribute("android:text")?.let { "\"$it\"" } ?: "TODO(\"Set text\")"
                val textSize = element.getAttribute("android:textSize")?.let { parseTextSize(it) }
                val textColor = element.getAttribute("android:textColor")?.let { "Color(0xFF${it.removePrefix("#")})" }
                val gravity = element.getAttribute("android:gravity")

                sb.appendLine("${indent}// TextView → Text")
                sb.append("${indent}Text(")
                val args = mutableListOf<String>()
                args.add("text = $text")
                textSize?.let { size -> args.add("fontSize = ${size}sp") }
                textColor?.let { color -> args.add("color = $color") }
                if (gravity.contains("center")) args.add("textAlign = TextAlign.Center")
                sb.appendLine(args.joinToString(", "))
                sb.appendLine("${indent})")
            }

            "Button", "com.google.android.material.button.MaterialButton" -> {
                val text = element.getAttribute("android:text")?.let { "\"$it\"" } ?: "TODO(\"Set text\")"
                val onClick = element.getAttribute("android:onClick")?.let { "/* onClick: $it */" } ?: ""

                sb.appendLine("${indent}// Button → Button")
                sb.appendLine("${indent}Button(")
                sb.appendLine("${indent}    onClick = { $onClick /* TODO: implement */ },")
                sb.appendLine("${indent}    content = { Text($text) }")
                sb.appendLine("${indent})")
            }

            "ImageView" -> {
                val src = element.getAttribute("android:src")
                val contentDesc = element.getAttribute("android:contentDescription")?.let { "\"$it\"" } ?: "null"

                sb.appendLine("${indent}// ImageView → Image")
                if (src.contains("@drawable/")) {
                    val drawableName = src.substringAfter("@drawable/")
                    sb.appendLine("${indent}Image(")
                    sb.appendLine("${indent}    painter = painterResource(id = R.drawable.$drawableName),")
                    sb.appendLine("${indent}    contentDescription = $contentDesc")
                    sb.appendLine("${indent})")
                } else {
                    sb.appendLine("${indent}// ⚠️ ImageView with src=$src needs manual handling")
                    sb.appendLine("${indent}// TODO: Replace with appropriate Image Composable")
                }
            }

            "LinearLayout" -> {
                val orientation = element.getAttribute("android:orientation") ?: "vertical"
                val container = if (orientation == "horizontal") "Row" else "Column"
                val spacing = if (orientation == "horizontal") "horizontalArrangement = Arrangement.spacedBy(8.dp)" else "verticalArrangement = Arrangement.spacedBy(8.dp)"

                sb.appendLine("${indent}// LinearLayout → $container")
                sb.appendLine("${indent}$container(")
                sb.appendLine("${indent}    $spacing,")

                // Process children
                val children = element.childNodes
                for (i in 0 until children.length) {
                    val child = children.item(i)
                    if (child is Element) {
                        convertElement(child, sb, "$indent    ")
                    }
                }

                sb.appendLine("${indent})")
            }

            "FrameLayout" -> {
                sb.appendLine("${indent}// FrameLayout → Box")
                sb.appendLine("${indent}Box(")
                val children = element.childNodes
                for (i in 0 until children.length) {
                    val child = children.item(i)
                    if (child is Element) {
                        convertElement(child, sb, "$indent    ")
                    }
                }
                sb.appendLine("${indent})")
            }

            "CardView" -> {
                val cardElevation = element.getAttribute("app:cardElevation") ?: "4dp"
                val cornerRadius = element.getAttribute("app:cardCornerRadius") ?: "8dp"

                sb.appendLine("${indent}// CardView → Card")
                sb.appendLine("${indent}Card(")
                sb.appendLine("${indent}    modifier = Modifier,")
                sb.appendLine("${indent}    shape = RoundedCornerShape($cornerRadius),")
                sb.appendLine("${indent}    tonalElevation = ${cardElevation.removeSuffix("dp").toIntOrNull() ?: 4}.dp")
                sb.appendLine("${indent}) {")
                val children = element.childNodes
                for (i in 0 until children.length) {
                    val child = children.item(i)
                    if (child is Element) {
                        convertElement(child, sb, "$indent    ")
                    }
                }
                sb.appendLine("${indent}}")
            }

            "RecyclerView" -> {
                sb.appendLine("${indent}// ⚠️ RecyclerView → LazyColumn/LazyRow")
                sb.appendLine("${indent}// Manual migration required — RecyclerView has complex adapter patterns")
                sb.appendLine("${indent}// TODO: Replace with LazyColumn { items(items) { item -> ... } }")
            }

            else -> {
                if (element.childNodes.length > 1 || element.childNodes.item(0)?.nodeValue?.isBlank() == false) {
                    // Has meaningful children
                    sb.appendLine("${indent}// ⚠️ $tagName — manual migration required")
                }
            }
        }
    }

    /**
     * Extract ID name from android:id attribute (e.g., "@+id/button_submit" → "button_submit").
     */
    private fun extractId(idAttr: String): String {
        return idAttr.substringAfterLast("/").substringAfterLast(":")
    }

    /**
     * Get Composable name from XML tag name.
     */
    private fun getComposableName(tagName: String): String {
        return tagName.replace(Regex("[^a-zA-Z0-9]")) {
            it.value.uppercase()
        }.replaceFirstChar { it.uppercase() } + "Screen"
    }

    /**
     * Generate Modifier chain from XML attributes.
     */
    private fun getModifiers(element: Element): String {
        val mods = mutableListOf<String>()

        element.getAttribute("android:layout_width")?.let {
            mods.add(convertSize(it))
        }
        element.getAttribute("android:layout_height")?.let {
            val h = convertSize(it)
            if (!mods.isEmpty()) mods.add(h)
        }

        val layoutParams = element.getAttribute("android:layout_margin")?.let { convertSize(it) }
        val padding = element.getAttribute("android:padding")?.let { "padding = ${convertSize(it)}" }

        val result = StringBuilder()
        if (padding != null) result.append(".padding(${padding.drop(9)})")
        return result.toString()
    }

    /**
     * Convert Android dp/sp size to Compose Modifier.
     */
    private fun convertSize(size: String): String {
        return when {
            size == "match_parent" -> ".fillMaxSize()"
            size == "wrap_content" -> ""
            size.endsWith("dp") -> ".size(${size.dropLast(2)}.dp)"
            size.endsWith("sp") -> ".fontSize(${size.dropLast(2)}.sp)"
            else -> ""
        }
    }

    /**
     * Parse Android textSize attribute (e.g., "14sp" → "14").
     */
    private fun parseTextSize(textSize: String): String {
        return textSize.replace(Regex("[^0-9]"), "")
    }

    /**
     * Get sample XML for demonstration.
     */
    private fun getSampleXml(): String = """
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout
    xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:orientation="vertical"
    android:padding="16dp">

    <TextView
        android:id="@+id/tv_title"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="Migration Demo"
        android:textSize="24sp"
        android:textColor="#6200EE" />

    <TextView
        android:id="@+id/tv_description"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_marginTop="8dp"
        android:text="This XML will be converted to Compose code."
        android:textSize="14sp" />

    <Button
        android:id="@+id/btn_start"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_marginTop="16dp"
        android:text="Start Migration" />

    <com.google.android.material.card.MaterialCardView
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_marginTop="16dp"
        app:cardCornerRadius="8dp"
        app:cardElevation="4dp">

        <TextView
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:padding="12dp"
            android:text="Card content here" />

    </com.google.android.material.card.MaterialCardView>

</LinearLayout>
    """.trimIndent()

    /**
     * Indent Compose code for nested output.
     */
    private fun String.indentForCompose(baseIndent: Int): String {
        val indent = " ".repeat(baseIndent)
        return this.lines().joinToString("\n") { line ->
            if (line.isNotEmpty()) "$indent$line" else line
        }
    }
}
