package com.mvi.kenny.feature.gridflexboxkit.submodules

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mvi.kenny.feature.gridflexboxkit.*

// CssMigrationGuideScreen — CSS 迁移手册屏幕
// PRD-141 | CSS Grid/Flexbox → Compose Grid/FlexBox 迁移指南
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CssMigrationGuideScreen(onBack: () -> Unit) {
    var selectedTab by remember { mutableStateOf(CssTab.Grid) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("CSS 迁移手册") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(androidx.compose.material.icons.Icons.Rounded.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Tab 切换 / Tab switch
            TabRow(
                selectedTabIndex = if (selectedTab == CssTab.Grid) 0 else 1
            ) {
                Tab(
                    selected = selectedTab == CssTab.Grid,
                    onClick = { selectedTab = CssTab.Grid },
                    text = { Text("CSS Grid → Compose Grid") }
                )
                Tab(
                    selected = selectedTab == CssTab.Flexbox,
                    onClick = { selectedTab = CssTab.Flexbox },
                    text = { Text("CSS Flexbox → Compose FlexBox") }
                )
            }

            when (selectedTab) {
                CssTab.Grid -> CssGridMappingTable()
                CssTab.Flexbox -> CssFlexboxMappingTable()
            }
        }
    }
}

private enum class CssTab { Grid, Flexbox }

@Composable
private fun CssGridMappingTable() {
    val mappings = listOf(
        CssPropertyMapping(
            cssProperty = "display: grid",
            cssCode = "display: grid;\ngrid-template-columns: 1fr 2fr 1fr;\ngrid-template-rows: auto 200px;\ngap: 16px;",
            composeCode = "@OptIn(ExperimentalLayoutApi::class)\nGridItemSpan(maxLineSpan = 3) { ... }\n// Or with explicit Grid:\nGridItemSpan(maxLineSpan = 2)\n// Note: Compose Grid uses different API",
            description = "定义 Grid 容器 / Define Grid container"
        ),
        CssPropertyMapping(
            cssProperty = "grid-template-columns",
            cssCode = "grid-template-columns: 100px 1fr 2fr;\ngrid-template-columns: repeat(3, 1fr);\ngrid-template-columns: masonry;",
            composeCode = "// Compose Grid columns parameter:\nGridItemSpan(2) // span 2 columns\n// For adaptive:\nGridItemSpan(maxLineSpan = 2)",
            description = "定义列轨道 / Define column tracks"
        ),
        CssPropertyMapping(
            cssProperty = "grid-column / grid-row",
            cssCode = ".item {\n  grid-column: 1 / 3;  /* span 2 cols */\n  grid-row: 1 / 2;\n}",
            composeCode = "// Grid: use GridItemSpan to span\nGridItemSpan(maxLineSpan = 2) // span all columns",
            description = "跨行跨列 / Span rows and columns"
        ),
        CssPropertyMapping(
            cssProperty = "justify-items / align-items",
            cssCode = "justify-items: center;\nalign-items: start;\njustify-content: center;\nalign-content: space-between;",
            composeCode = "// Alignment via Arrangement:\nhorizontalArrangement = Arrangement.Center\nverticalArrangement = Arrangement.SpaceBetween",
            description = "对齐方式 / Alignment"
        ),
        CssPropertyMapping(
            cssProperty = "gap",
            cssCode = "gap: 16px;\nrow-gap: 8px;\ncolumn-gap: 24px;",
            composeCode = "columnGap = 16.dp\nrowGap = 8.dp",
            description = "间距（行间距/列间距）/ Spacing"
        ),
        CssPropertyMapping(
            cssProperty = "grid-template-areas",
            cssCode = "grid-template-areas:\n  'header header header'\n  'sidebar main main'\n  'footer footer footer';",
            composeCode = "// Compose: no direct equivalent\n// Use GridItemSpan to approximate:\nGridItemSpan(maxLineSpan = 2) // header\nGridItemSpan(1) // sidebar\nGridItemSpan(1) // main",
            description = "命名网格区域（无直接等价）/ Named areas (no direct equivalent)"
        ),
        CssPropertyMapping(
            cssProperty = "auto-fill / auto-fit",
            cssCode = "grid-template-columns:\n  repeat(auto-fill, minmax(200px, 1fr));",
            composeCode = "// Compose adaptive layout:\nGridItemSpan(maxLineSpan = AdaptiveCallback)\n// Or use Grid with fixed columns",
            description = "自适应列数 / Auto-fill / Auto-fit"
        ),
    )

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(mappings) { mapping ->
            CssPropertyMappingCard(mapping = mapping)
        }
    }
}

@Composable
private fun CssFlexboxMappingTable() {
    val mappings = listOf(
        CssPropertyMapping(
            cssProperty = "display: flex",
            cssCode = "display: flex;\nflex-direction: row;\nflex-wrap: wrap;",
            composeCode = "// Compose FlexBox:\nRow(horizontalArrangement = ...) // Row\nColumn(...) // Column",
            description = "定义 Flex 容器 / Define Flex container"
        ),
        CssPropertyMapping(
            cssProperty = "flex-direction",
            cssCode = "flex-direction: row;  /* default */\nflex-direction: column;\nflex-direction: row-reverse;\nflex-direction: column-reverse;",
            composeCode = "// Row = flex-direction: row\n// Column = flex-direction: column\n// Compose has no direct reverse modifier",
            description = "主轴方向 / Main axis direction"
        ),
        CssPropertyMapping(
            cssProperty = "flex-grow",
            cssCode = ".item { flex-grow: 1; }\n/* 0 = no grow, 1+ = grow proportionally */",
            composeCode = "Modifier.flexGrow(1f)\n// FlexBox API: flex { grow(1f) }",
            description = "等比放大 / Grow proportionally"
        ),
        CssPropertyMapping(
            cssProperty = "flex-shrink",
            cssCode = ".item { flex-shrink: 0; }\n/* 0 = no shrink, 1+ = shrink proportionally */",
            composeCode = "Modifier.flexShrink(1f)\n// FlexBox API: flex { shrink(1f) }",
            description = "等比缩小 / Shrink proportionally"
        ),
        CssPropertyMapping(
            cssProperty = "flex-basis",
            cssCode = ".item { flex-basis: 200px; }\n/* Equivalent to width/height based on direction */",
            composeCode = "Modifier.flexBasis(200.dp)\n// FlexBox API: flex { basis(200.dp) }",
            description = "初始基准尺寸 / Initial base size"
        ),
        CssPropertyMapping(
            cssProperty = "flex-wrap",
            cssCode = "flex-wrap: wrap;       /* wrap on overflow */\nflex-wrap: nowrap;   /* no wrap, default */\nflex-wrap: wrap-reverse;",
            composeCode = "// Compose: use FlowRow for wrap:\nFlowRow(\n  horizontalArrangement = Arrangement.spacedBy(8.dp),\n  verticalArrangement = Arrangement.spacedBy(8.dp)\n) { ... }",
            description = "换行控制 / Wrap control"
        ),
        CssPropertyMapping(
            cssProperty = "justify-content",
            cssCode = "justify-content: flex-start;   /* default */\njustify-content: center;\njustify-content: space-between;\njustify-content: space-around;",
            composeCode = "// Row: horizontalArrangement\n// Column: verticalArrangement\nhorizontalArrangement = Arrangement.SpaceBetween",
            description = "主轴对齐 / Main axis alignment"
        ),
        CssPropertyMapping(
            cssProperty = "align-items",
            cssCode = "align-items: stretch;     /* default */\nalign-items: center;\nalign-items: flex-start;\nalign-items: flex-end;",
            composeCode = "// Row: verticalAlignment\n// Column: horizontalAlignment\nverticalAlignment = Alignment.CenterVertically",
            description = "交叉轴对齐 / Cross axis alignment"
        ),
        CssPropertyMapping(
            cssProperty = "align-self",
            cssCode = ".item { align-self: center; }\n/* Override align-items for this item */",
            composeCode = "// Apply to individual item:\nRow {\n  Item(Modifier.align(Alignment.CenterVertically))\n}",
            description = "单项对齐（覆盖容器设置）/ Per-item alignment"
        ),
    )

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(mappings) { mapping ->
            CssPropertyMappingCard(mapping = mapping)
        }
    }
}

data class CssPropertyMapping(
    val cssProperty: String,
    val cssCode: String,
    val composeCode: String,
    val description: String
)

@Composable
private fun CssPropertyMappingCard(mapping: CssPropertyMapping) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // CSS 属性名 / CSS property name
            Surface(
                shape = RoundedCornerShape(4.dp),
                color = Color(0xFF157A6E).copy(alpha = 0.1f)
            ) {
                Text(
                    text = mapping.cssProperty,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF157A6E)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 描述 / Description
            Text(
                text = mapping.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            // CSS 代码 / CSS code
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "🎨 CSS",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = Color(0xFF264653).copy(alpha = 0.1f)
                    ) {
                        Text(
                            text = mapping.cssCode,
                            modifier = Modifier.padding(8.dp),
                            style = MaterialTheme.typography.bodySmall,
                            fontFamily = FontFamily.Monospace,
                            color = Color(0xFF98C1D9)
                        )
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "⚡ Compose",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = Color(0xFF3D5A80).copy(alpha = 0.1f)
                    ) {
                        Text(
                            text = mapping.composeCode,
                            modifier = Modifier.padding(8.dp),
                            style = MaterialTheme.typography.bodySmall,
                            fontFamily = FontFamily.Monospace,
                            color = Color(0xFF98C1D9)
                        )
                    }
                }
            }
        }
    }
}
