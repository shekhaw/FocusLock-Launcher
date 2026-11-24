package com.dumbphone.launcher.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// High-contrast readable text shadow for physical glass over light/dark wallpapers
val GlassTextShadow = Shadow(
    color = Color.Black.copy(alpha = 0.75f),
    offset = Offset(0f, 1.5f),
    blurRadius = 4f
)

/**
 * macOS Dock Glass Container:
 * Genuine translucent frosted glass shelf.
 * Does NOT blow out to solid white — lets ~85% of wallpaper show through,
 * with signature dual-rim top specular reflection highlight and ambient drop shadow.
 */
@Composable
fun MacDockGlassCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 26.dp,
    content: @Composable BoxScope.() -> Unit
) {
    val shape = RoundedCornerShape(cornerRadius)

    Box(
        modifier = modifier
            .shadow(
                elevation = 20.dp,
                shape = shape,
                ambientColor = Color.Black.copy(alpha = 0.55f),
                spotColor = Color.Black.copy(alpha = 0.40f)
            )
            .clip(shape)
            // Translucent Frosted Glass Medium
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color.White.copy(alpha = 0.16f), // Soft frosted upper sheen
                        Color.White.copy(alpha = 0.07f)  // Deep translucent floor
                    )
                )
            )
            // macOS Specular Dual-Rim Reflection Border
            .border(
                BorderStroke(
                    1.25.dp,
                    Brush.verticalGradient(
                        listOf(
                            Color.White.copy(alpha = 0.70f), // Crisp bright top rim reflection
                            Color.White.copy(alpha = 0.22f), // Translucent side edge
                            Color.White.copy(alpha = 0.08f)  // Soft bottom falloff
                        )
                    )
                ),
                shape = shape
            )
    ) {
        // Top Specular Glare Arc (Soft overhead light reflection)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(28.dp)
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color.White.copy(alpha = 0.20f),
                            Color.Transparent
                        )
                    )
                )
        )

        content()
    }
}

/**
 * macOS Dock Glass Pill:
 * Floating capsule pill used for action buttons, search bars, and input fields.
 */
@Composable
fun MacDockGlassPill(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val shape = RoundedCornerShape(50)

    Box(
        modifier = modifier
            .shadow(
                elevation = 12.dp,
                shape = shape,
                ambientColor = Color.Black.copy(alpha = 0.45f),
                spotColor = Color.Black.copy(alpha = 0.30f)
            )
            .clip(shape)
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color.White.copy(alpha = 0.20f),
                        Color.White.copy(alpha = 0.09f)
                    )
                )
            )
            .border(
                BorderStroke(
                    1.2.dp,
                    Brush.verticalGradient(
                        listOf(
                            Color.White.copy(alpha = 0.75f),
                            Color.White.copy(alpha = 0.18f)
                        )
                    )
                ),
                shape = shape
            )
    ) {
        content()
    }
}

/**
 * Sleek Hairline Glass Divider
 */
@Composable
fun MacDockGlassDivider(
    modifier: Modifier = Modifier,
    paddingHorizontal: Dp = 16.dp
) {
    HorizontalDivider(
        color = Color.White.copy(alpha = 0.10f),
        thickness = 0.75.dp,
        modifier = modifier.padding(horizontal = paddingHorizontal)
    )
}
