package com.dumbphone.launcher.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

val OledBlack = Color(0xFF000000)
val DarkSurface = Color(0xFF101012)
val DarkSurfaceElevated = Color(0xFF18181B)
val BorderSubtle = Color(0xFF27272A)
val TextPrimary = Color(0xFFF4F4F5)
val TextSecondary = Color(0xFFA1A1AA)
val TextMuted = Color(0xFF52525B)
val AccentPurple = Color(0xFF6366F1)
val AccentDanger = Color(0xFFEF4444)

// Apple Liquid Glass Palette
val GlassWhiteVerySubtle = Color(0x0EFFFFFF) // 5% white
val GlassWhiteSubtle = Color(0x18FFFFFF)     // 9% white
val GlassWhiteMedium = Color(0x28FFFFFF)     // 16% white
val GlassWhiteBorder = Color(0x35FFFFFF)     // 21% white
val GlassWhiteHighlight = Color(0x55FFFFFF)  // 33% white

// macOS Dock Glass Palette & Highlights
val MacDockGlassTop = Color(0x2BFFFFFF)       // 17% white frosted top
val MacDockGlassBottom = Color(0x14FFFFFF)    // 8% white deep floor
val MacDockBorderTop = Color(0x8CFFFFFF)      // 55% white crisp specular top reflection
val MacDockBorderMid = Color(0x30FFFFFF)      // 19% white mid rim
val MacDockBorderBottom = Color(0x12FFFFFF)   // 7% white subtle bottom bevel
val MacDockDivider = Color(0x18FFFFFF)        // Hairline glass divider between dock items
val MacDockItemActive = Color(0x2EFFFFFF)     // Subtle highlight on tap


enum class WallpaperTheme(val displayName: String) {
    AURORA("Aurora Night"),
    NEBULA("Cosmic Nebula"),
    DUSK("Warm Dusk"),
    SMOKE("Frosted Smoke"),
    OBSIDIAN("Deep Obsidian");

    fun getBackgroundBrush(): Brush {
        return when (this) {
            AURORA -> Brush.radialGradient(
                colors = listOf(
                    Color(0xFF3B0764),
                    Color(0xFF030712),
                    Color(0xFF064E3B),
                    Color(0xFF000000)
                ),
                radius = 1600f
            )
            NEBULA -> Brush.radialGradient(
                colors = listOf(
                    Color(0xFF3730A3),
                    Color(0xFF020617),
                    Color(0xFF581C87),
                    Color(0xFF000000)
                ),
                radius = 1600f
            )
            DUSK -> Brush.radialGradient(
                colors = listOf(
                    Color(0xFF831843),
                    Color(0xFF18181B),
                    Color(0xFF701A75),
                    Color(0xFF000000)
                ),
                radius = 1600f
            )
            SMOKE -> Brush.radialGradient(
                colors = listOf(
                    Color(0xFF27272A),
                    Color(0xFF09090B),
                    Color(0xFF000000)
                ),
                radius = 1600f
            )
            OBSIDIAN -> Brush.verticalGradient(
                colors = listOf(Color(0xFF000000), Color(0xFF000000))
            )
        }
    }
}
