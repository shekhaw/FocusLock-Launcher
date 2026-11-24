package com.dumbphone.launcher.ui.components

import android.os.Build
import android.view.ViewGroup
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.dumbphone.launcher.ui.liquidglass.widget.LiquidGlassView
import com.dumbphone.launcher.ui.theme.*

// CompositionLocal for the wallpaper sampling source
val LocalWallpaperContainer = staticCompositionLocalOf<ViewGroup?> { null }

// High-contrast text shadow for physical glass over any light/dark wallpapers
val GlassTextShadow = Shadow(
    color = Color.Black.copy(alpha = 0.85f),
    offset = Offset(0f, 2f),
    blurRadius = 4f
)

/**
 * macOS Dock Glass Card:
 * Genuine optical liquid glass container powered by AndroidLiquidGlassView (QmDeve).
 * Samples the live background wallpaper view and applies real GPU AGSL convex lens
 * magnification, multi-wavelength chromatic dispersion prism split, and specular rim reflections.
 *
 * Optically clear (tintAlpha = 0.0f) so it never blows out to solid white.
 */
@Composable
fun MacDockGlassCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 26.dp,
    refractionHeight: Dp = 20.dp,
    refractionOffset: Dp = 70.dp,
    dispersion: Float = 0.55f,
    blurRadius: Float = 0.01f,
    content: @Composable BoxScope.() -> Unit
) {
    val wallpaperSource = LocalWallpaperContainer.current
    val density = LocalDensity.current
    val cornerRadiusPx = with(density) { cornerRadius.toPx() }
    val refractionHeightPx = with(density) { refractionHeight.toPx() }
    val refractionOffsetPx = with(density) { refractionOffset.toPx() }
    val shape = RoundedCornerShape(cornerRadius)

    Box(
        modifier = modifier
            .shadow(
                elevation = 18.dp,
                shape = shape,
                ambientColor = Color.Black.copy(alpha = 0.60f),
                spotColor = Color.Black.copy(alpha = 0.45f)
            )
            .clip(shape)
    ) {
        // Physical Liquid Glass Refraction Layer (samples the live wallpaper)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && wallpaperSource != null) {
            AndroidView(
                modifier = Modifier.matchParentSize(),
                factory = { ctx ->
                    LiquidGlassView(ctx).apply {
                        setCornerRadius(cornerRadiusPx)
                        setRefractionHeight(refractionHeightPx)
                        setRefractionOffset(refractionOffsetPx)
                        setDispersion(dispersion)
                        setBlurRadius(blurRadius)
                        setTintAlpha(0.0f) // Optical clarity, NO milky white blowout!
                        bind(wallpaperSource)
                    }
                },
                update = { view ->
                    view.setCornerRadius(cornerRadiusPx)
                    view.setRefractionHeight(refractionHeightPx)
                    view.setRefractionOffset(refractionOffsetPx)
                    view.setDispersion(dispersion)
                    view.setBlurRadius(blurRadius)
                    view.setTintAlpha(0.0f)
                    view.bind(wallpaperSource)
                }
            )
        } else {
            // High quality fallback for older devices or when source is not bound
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color.White.copy(alpha = 0.14f),
                                Color.White.copy(alpha = 0.06f)
                            )
                        )
                    )
            )
        }

        // Specular Chromatic Edge Highlight & Overhead Light Bevel
        Box(
            modifier = Modifier
                .matchParentSize()
                .border(
                    BorderStroke(
                        1.25.dp,
                        Brush.linearGradient(
                            listOf(
                                Color.White.copy(alpha = 0.85f),
                                GlassChromaticCyan,
                                Color.White.copy(alpha = 0.25f),
                                GlassChromaticMagenta,
                                Color.White.copy(alpha = 0.10f)
                            ),
                            start = Offset(0f, 0f),
                            end = Offset(400f, 400f)
                        )
                    ),
                    shape = shape
                )
        )

        // Top Specular Glare Arc (Soft overhead light reflection)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(20.dp)
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color.White.copy(alpha = 0.16f),
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
 * Floating capsule pill with physical liquid glass optics.
 */
@Composable
fun MacDockGlassPill(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val wallpaperSource = LocalWallpaperContainer.current
    val density = LocalDensity.current
    val cornerRadiusPx = with(density) { 50.dp.toPx() }
    val refractionHeightPx = with(density) { 16.dp.toPx() }
    val refractionOffsetPx = with(density) { 50.dp.toPx() }
    val shape = RoundedCornerShape(50)

    Box(
        modifier = modifier
            .shadow(
                elevation = 12.dp,
                shape = shape,
                ambientColor = Color.Black.copy(alpha = 0.50f),
                spotColor = Color.Black.copy(alpha = 0.35f)
            )
            .clip(shape)
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && wallpaperSource != null) {
            AndroidView(
                modifier = Modifier.matchParentSize(),
                factory = { ctx ->
                    LiquidGlassView(ctx).apply {
                        setCornerRadius(cornerRadiusPx)
                        setRefractionHeight(refractionHeightPx)
                        setRefractionOffset(refractionOffsetPx)
                        setDispersion(0.60f)
                        setBlurRadius(0.01f)
                        setTintAlpha(0.0f)
                        bind(wallpaperSource)
                    }
                },
                update = { view ->
                    view.setCornerRadius(cornerRadiusPx)
                    view.setRefractionHeight(refractionHeightPx)
                    view.setRefractionOffset(refractionOffsetPx)
                    view.setDispersion(0.60f)
                    view.setTintAlpha(0.0f)
                    view.bind(wallpaperSource)
                }
            )
        } else {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color.White.copy(alpha = 0.16f),
                                Color.White.copy(alpha = 0.08f)
                            )
                        )
                    )
            )
        }

        // Specular Chromatic Edge Highlight
        Box(
            modifier = Modifier
                .matchParentSize()
                .border(
                    BorderStroke(
                        1.2.dp,
                        Brush.linearGradient(
                            listOf(
                                Color.White.copy(alpha = 0.85f),
                                GlassChromaticCyan,
                                Color.White.copy(alpha = 0.30f),
                                GlassChromaticMagenta,
                                Color.White.copy(alpha = 0.12f)
                            )
                        )
                    ),
                    shape = shape
                )
        )

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
        color = Color.White.copy(alpha = 0.12f),
        thickness = 0.75.dp,
        modifier = modifier.padding(horizontal = paddingHorizontal)
    )
}
