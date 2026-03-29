package com.yourpackage.ui.theme

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// ============================================================
// 通用多色 SVG 图标组件
//
// 使用方式：
//
//   // 在 StockTheme 内：
//   ThemedVectorIcon(
//     resId = R.drawable.ic_candlestick,
//     colorMap = mapOf(
//       // 原色（tools:fillColor 值）  →  目标色（StockColors 中取）
//       Color(0xFFF24966) to StockColors.get().upFunctional,
//       Color(0xFF07B067) to StockColors.get().downFunctional,
//       Color(0xFFF3F3F3) to StockColors.get().base.backgroundL3,
//       Color(0xFFDDDDDD) to StockColors.get().base.borderL1,
//     )
//   )
//
// 一个组件通用所有多色 SVG，不用每个都写 Canvas 实现。
// colorMap 从 XML 的 tools:fillColor 取原始颜色值填入即可。
// ============================================================

/**
 * 通用多色主题图标
 *
 * @param resId    SVG 转成的 VectorDrawable 资源 ID（R.drawable.xxx）
 * @param colorMap 映射表：原色（XML tools:fillColor 的值）→ 目标色
 *                 只需要填需要替换的颜色，其他颜色保持原样
 * @param size     图标尺寸，默认 24.dp
 */
@Composable
fun ThemedVectorIcon(
    resId: Int,
    colorMap: Map<Color, Color>,
    size: Dp = 24.dp,
) {
    val vector = vectorResource(resId)

    Canvas(modifier = Modifier.size(size)) {
        drawIntoCanvas { canvas ->
            // 创建一张 bitmap，把 vector 画上去
            val bitmap = Bitmap.createBitmap(
                vector.defaultWidth,
                vector.defaultHeight,
                Bitmap.Config.ARGB_8888
            )
            val androidCanvas = Canvas(bitmap)

            // 用 Android Paint 把 vector 画到 bitmap
            val paint = Paint().apply { isAntiAlias = true }
            androidCanvas.drawVector(vector, paint)

            // 逐颜色替换
            colorMap.forEach { (from, to) ->
                val fromArgb = from.toArgb()
                val toArgb = to.toArgb()
                replaceColor(bitmap, fromArgb, toArgb)
            }

            // 画回 Compose canvas
            canvas.nativeCanvas.drawBitmap(bitmap, 0f, 0f, null)
            bitmap.recycle()
        }
    }
}

/**
 * 替换 bitmap 中的指定颜色（逐像素）
 */
private fun replaceColor(bitmap: Bitmap, fromColor: Int, toColor: Int) {
    val width = bitmap.width
    val height = bitmap.height
    val pixels = IntArray(width * height)
    bitmap.getPixels(pixels, 0, width, 0, 0, width, height)

    for (i in pixels.indices) {
        // 忽略 alpha 差异（抗锯齿边缘的半透明像素不做替换）
        val pixelAlpha = (pixels[i] shr 24) and 0xFF
        val fromAlpha = (fromColor shr 24) and 0xFF
        if (pixelAlpha == 0 && fromAlpha == 0) continue  // 跳过透明像素
        if (pixels[i] == fromColor) {
            pixels[i] = toColor
        }
    }

    bitmap.setPixels(pixels, 0, width, 0, 0, width, height)
}

private fun Color.toArgb(): Int =
    android.graphics.Color.argb(
        (alpha * 255).toInt(),
        (red * 255).toInt(),
        (green * 255).toInt(),
        (blue * 255).toInt()
    )
