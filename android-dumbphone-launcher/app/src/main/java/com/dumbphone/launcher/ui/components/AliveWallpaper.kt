package com.dumbphone.launcher.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.dumbphone.launcher.ui.theme.WallpaperTheme
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun AliveWallpaper(
    theme: WallpaperTheme,
    modifier: Modifier = Modifier
) {
    // Smooth, organic infinite transitions for undulating living fluid motion
    val infiniteTransition = rememberInfiniteTransition(label = "alive_wallpaper_motion")

    // Slow phase 1: 14-second smooth organic loop
    val phase1 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 14000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase1"
    )

    // Slower counter-balancing phase 2: 20-second organic loop
    val phase2 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 20000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase2"
    )

    // Gentle breathing scale factor
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 8000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    val (colorBase, orbColor1, orbColor2, orbColor3) = when (theme) {
        WallpaperTheme.AURORA -> QuadColors(
            base = Color(0xFF030712),
            orb1 = Color(0xFF4C1D95), // Deep living violet
            orb2 = Color(0xFF065F46), // Ethereal emerald
            orb3 = Color(0xFF1E1B4B)  // Midnight blue
        )
        WallpaperTheme.NEBULA -> QuadColors(
            base = Color(0xFF020617),
            orb1 = Color(0xFF3730A3), // Electric indigo
            orb2 = Color(0xFF6B21A8), // Deep cosmic purple
            orb3 = Color(0xFF1E1B4B)
        )
        WallpaperTheme.DUSK -> QuadColors(
            base = Color(0xFF0C0A09),
            orb1 = Color(0xFF831843), // Rich magenta rose
            orb2 = Color(0xFF581C87), // Plum violet
            orb3 = Color(0xFF701A75)
        )
        WallpaperTheme.SMOKE -> QuadColors(
            base = Color(0xFF09090B),
            orb1 = Color(0xFF27272A), // Soft granite
            orb2 = Color(0xFF3F3F46), // Ethereal smoke
            orb3 = Color(0xFF18181B)
        )
        WallpaperTheme.OBSIDIAN -> QuadColors(
            base = Color(0xFF000000),
            orb1 = Color(0xFF151518), // Ultra subtle graphite pulse
            orb2 = Color(0xFF0E0E12),
            orb3 = Color(0xFF000000)
        )
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        // 1. Base Dark Canvas
        drawRect(color = colorBase)

        if (theme != WallpaperTheme.OBSIDIAN) {
            // Orb 1: Floats gracefully across the upper diagonal
            val orb1X = w * 0.35f + sin(phase1) * (w * 0.22f)
            val orb1Y = h * 0.30f + cos(phase1) * (h * 0.15f)
            val radius1 = (w * 0.85f) * pulse

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(orbColor1.copy(alpha = 0.70f), Color.Transparent),
                    center = Offset(orb1X, orb1Y),
                    radius = radius1
                ),
                center = Offset(orb1X, orb1Y),
                radius = radius1
            )

            // Orb 2: Counter-drifts across the lower-right quadrant
            val orb2X = w * 0.70f + cos(phase2) * (w * 0.25f)
            val orb2Y = h * 0.68f + sin(phase2) * (h * 0.18f)
            val radius2 = (w * 0.95f) * (2f - pulse)

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(orbColor2.copy(alpha = 0.65f), Color.Transparent),
                    center = Offset(orb2X, orb2Y),
                    radius = radius2
                ),
                center = Offset(orb2X, orb2Y),
                radius = radius2
            )

            // Orb 3: Central breathing ambient shimmer
            val orb3X = w * 0.50f + sin(phase2 * 0.8f) * (w * 0.15f)
            val orb3Y = h * 0.50f + cos(phase1 * 0.8f) * (h * 0.12f)
            val radius3 = w * 0.75f

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(orbColor3.copy(alpha = 0.50f), Color.Transparent),
                    center = Offset(orb3X, orb3Y),
                    radius = radius3
                ),
                center = Offset(orb3X, orb3Y),
                radius = radius3
            )
        } else {
            // Subtle living breathing pulse for pure OLED obsidian mode
            val center = Offset(w * 0.5f, h * 0.45f)
            val radius = (w * 0.9f) * pulse
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(orbColor1.copy(alpha = 0.4f), Color.Transparent),
                    center = center,
                    radius = radius
                ),
                center = center,
                radius = radius
            )
        }
    }
}

private data class QuadColors(
    val base: Color,
    val orb1: Color,
    val orb2: Color,
    val orb3: Color
)
