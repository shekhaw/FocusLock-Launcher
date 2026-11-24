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
import androidx.compose.ui.Alignment
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

// Global Glass Settings configured via Glass Tuning Studio
data class GlassSettings(
    val refractionOffset: Float = 70f,
    val dispersion: Float = 0.55f,
    val tintTone: String = "CLEAR",
    val tintAlpha: Float = 0.0f
)

val LocalGlassSettings = staticCompositionLocalOf { GlassSettings() }

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
 */
@Composable
fun MacDockGlassCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 26.dp,
    refractionHeight: Dp = 20.dp,
    refractionOffset: Dp? = null,
    dispersion: Float? = null,
    blurRadius: Float = 0.01f,
    content: @Composable BoxScope.() -> Unit
) {
    val wallpaperSource = LocalWallpaperContainer.current
    val globalGlass = LocalGlassSettings.current
    val density = LocalDensity.current

    val effectiveOffsetDp = refractionOffset ?: globalGlass.refractionOffset.dp
    val effectiveDispersion = dispersion ?: globalGlass.dispersion
    val effectiveTintAlpha = globalGlass.tintAlpha

    val (tintR, tintG, tintB) = when (globalGlass.tintTone) {
        "SMOKE" -> Triple(0.15f, 0.15f, 0.18f)
        "ICE" -> Triple(0.0f, 0.83f, 1.0f)
        "ROSE" -> Triple(1.0f, 0.41f, 0.70f)
        "EMERALD" -> Triple(0.0f, 1.0f, 0.53f)
        else -> Triple(1.0f, 1.0f, 1.0f)
    }

    val cornerRadiusPx = with(density) { cornerRadius.toPx() }
    val refractionHeightPx = with(density) { refractionHeight.toPx() }
    val refractionOffsetPx = with(density) { effectiveOffsetDp.toPx() }
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
                        setDispersion(effectiveDispersion)
                        setBlurRadius(blurRadius)
                        setTintColorRed(tintR)
                        setTintColorGreen(tintG)
                        setTintColorBlue(tintB)
                        setTintAlpha(effectiveTintAlpha)
                        bind(wallpaperSource)
                    }
                },
                update = { view ->
                    view.setCornerRadius(cornerRadiusPx)
                    view.setRefractionHeight(refractionHeightPx)
                    view.setRefractionOffset(refractionOffsetPx)
                    view.setDispersion(effectiveDispersion)
                    view.setBlurRadius(blurRadius)
                    view.setTintColorRed(tintR)
                    view.setTintColorGreen(tintG)
                    view.setTintColorBlue(tintB)
                    view.setTintAlpha(effectiveTintAlpha)
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
 * macOS / iOS Liquid Glass Pill:
 * Floating capsule pill with physical liquid glass optics, Apple HIG touch targets,
 * generous padding, and high-contrast specular reflections.
 */
@Composable
fun MacDockGlassPill(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(horizontal = 20.dp, vertical = 10.dp),
    content: @Composable BoxScope.() -> Unit
) {
    val wallpaperSource = LocalWallpaperContainer.current
    val globalGlass = LocalGlassSettings.current
    val density = LocalDensity.current

    val cornerRadiusPx = with(density) { 50.dp.toPx() }
    val refractionHeightPx = with(density) { 16.dp.toPx() }
    val refractionOffsetPx = with(density) { (globalGlass.refractionOffset * 0.75f).dp.toPx() }
    val shape = RoundedCornerShape(50)

    val (tintR, tintG, tintB) = when (globalGlass.tintTone) {
        "SMOKE" -> Triple(0.15f, 0.15f, 0.18f)
        "ICE" -> Triple(0.0f, 0.83f, 1.0f)
        "ROSE" -> Triple(1.0f, 0.41f, 0.70f)
        "EMERALD" -> Triple(0.0f, 1.0f, 0.53f)
        else -> Triple(1.0f, 1.0f, 1.0f)
    }

    Box(
        modifier = modifier
            .shadow(
                elevation = 14.dp,
                shape = shape,
                ambientColor = Color.Black.copy(alpha = 0.55f),
                spotColor = Color.Black.copy(alpha = 0.40f)
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
                        setDispersion(globalGlass.dispersion)
                        setBlurRadius(0.01f)
                        setTintColorRed(tintR)
                        setTintColorGreen(tintG)
                        setTintColorBlue(tintB)
                        setTintAlpha(globalGlass.tintAlpha)
                        bind(wallpaperSource)
                    }
                },
                update = { view ->
                    view.setCornerRadius(cornerRadiusPx)
                    view.setRefractionHeight(refractionHeightPx)
                    view.setRefractionOffset(refractionOffsetPx)
                    view.setDispersion(globalGlass.dispersion)
                    view.setTintColorRed(tintR)
                    view.setTintColorGreen(tintG)
                    view.setTintColorBlue(tintB)
                    view.setTintAlpha(globalGlass.tintAlpha)
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
                                Color.White.copy(alpha = 0.18f),
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

        // Apple HIG Inner Padding & Content Alignment
        Box(
            modifier = Modifier.padding(contentPadding),
            contentAlignment = Alignment.Center
        ) {
            content()
        }
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
