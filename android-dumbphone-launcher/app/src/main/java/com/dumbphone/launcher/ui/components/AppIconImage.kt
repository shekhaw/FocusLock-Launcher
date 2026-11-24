package com.dumbphone.launcher.ui.components

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Clean Native Android App Icon:
 * Renders the authentic, high-resolution native Android app icon cleanly with natural transparency.
 * Eliminates artificial squircle bounding boxes, mismatched corner outlines, and grey corner shadows
 * so circular and freeform app icons float seamlessly on the macOS Dock liquid glass shelf.
 */
@Composable
fun AppIconImage(
    drawable: Drawable?,
    label: String,
    modifier: Modifier = Modifier,
    cornerRadius: Dp? = null,
    showSpecularBorder: Boolean = false
) {
    val imageBitmap = remember(drawable) {
        drawable?.let { d ->
            try {
                if (d is BitmapDrawable && d.bitmap != null && !d.bitmap.isRecycled) {
                    d.bitmap.asImageBitmap()
                } else {
                    val w = if (d.intrinsicWidth > 0) d.intrinsicWidth else 144
                    val h = if (d.intrinsicHeight > 0) d.intrinsicHeight else 144
                    val targetSize = w.coerceIn(96, 256)
                    val bmp = Bitmap.createBitmap(targetSize, targetSize, Bitmap.Config.ARGB_8888)
                    val canvas = Canvas(bmp)
                    d.setBounds(0, 0, canvas.width, canvas.height)
                    d.draw(canvas)
                    bmp.asImageBitmap()
                }
            } catch (e: Exception) {
                null
            }
        }
    }

    if (imageBitmap != null) {
        // Render the natural native icon without artificial bounding box frames or clipping
        Image(
            bitmap = imageBitmap,
            contentDescription = label,
            contentScale = ContentScale.Fit,
            modifier = modifier
        )
    } else {
        // Fallback tile only if no native icon exists
        val shape = RoundedCornerShape(cornerRadius ?: 12.dp)
        val initials = if (label.isNotBlank()) label.take(1).uppercase() else "•"
        Box(
            modifier = modifier
                .clip(shape)
                .background(
                    Brush.linearGradient(
                        listOf(
                            Color(0xFF38BDF8),
                            Color(0xFF6366F1),
                            Color(0xFFA855F7)
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = initials,
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                style = androidx.compose.ui.text.TextStyle(
                    shadow = androidx.compose.ui.graphics.Shadow(
                        color = Color.Black.copy(alpha = 0.6f),
                        blurRadius = 4f
                    )
                )
            )
        }
    }
}
