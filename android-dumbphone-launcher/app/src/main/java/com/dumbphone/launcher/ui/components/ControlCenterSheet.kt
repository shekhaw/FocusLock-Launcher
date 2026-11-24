package com.dumbphone.launcher.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dumbphone.launcher.ui.theme.Typography
import com.dumbphone.launcher.ui.viewmodel.LauncherViewModel

@Composable
fun ControlCenterSheet(
    viewModel: LauncherViewModel,
    modifier: Modifier = Modifier
) {
    val isOpen by viewModel.isControlCenterOpen.collectAsState()
    val isTorchOn by viewModel.isTorchOn.collectAsState()
    val volumePercent by viewModel.volumePercent.collectAsState()
    val brightnessPercent by viewModel.brightnessPercent.collectAsState()
    val isMusicPlaying by viewModel.isMusicPlaying.collectAsState()

    var wifiActive by remember { mutableStateOf(true) }
    var bluetoothActive by remember { mutableStateOf(true) }
    var dndActive by remember { mutableStateOf(false) }

    AnimatedVisibility(
        visible = isOpen,
        enter = fadeIn() + scaleIn(initialScale = 0.92f),
        exit = fadeOut() + scaleOut(targetScale = 0.92f)
    ) {
        // Scrim background to dismiss
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.45f))
                .pointerInput(Unit) {
                    detectTapGestures { viewModel.closeControlCenter() }
                },
            contentAlignment = Alignment.Center
        ) {
            // Liquid Glass Control Center Floating Panel
            MacDockGlassCard(
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .wrapContentHeight()
                    .pointerInput(Unit) {
                        detectTapGestures { /* Intercept tap inside card */ }
                    },
                cornerRadius = 28.dp,
                refractionHeight = 22.dp,
                refractionOffset = 70.dp,
                dispersion = 0.55f
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Header Bar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "⌘ Control Center",
                                style = Typography.titleMedium.copy(shadow = GlassTextShadow),
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            MacDockGlassPill(
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "🔋 85%",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White,
                                    style = Typography.labelSmall.copy(shadow = GlassTextShadow)
                                )
                            }

                            MacDockGlassPill(
                                modifier = Modifier.clickable { viewModel.closeControlCenter() },
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "✕",
                                    fontSize = 13.sp,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    style = Typography.labelSmall.copy(shadow = GlassTextShadow)
                                )
                            }
                        }
                    }

                    MacDockGlassDivider(paddingHorizontal = 0.dp)

                    // 2x2 Glass Quick Toggles
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        ControlCenterTogglePill(
                            icon = Icons.Default.Wifi,
                            label = "Wi-Fi",
                            subtitle = if (wifiActive) "Connected" else "Off",
                            isActive = wifiActive,
                            onClick = { wifiActive = !wifiActive },
                            modifier = Modifier.weight(1f)
                        )

                        ControlCenterTogglePill(
                            icon = Icons.Default.Bluetooth,
                            label = "Bluetooth",
                            subtitle = if (bluetoothActive) "Ready" else "Off",
                            isActive = bluetoothActive,
                            onClick = { bluetoothActive = !bluetoothActive },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        ControlCenterTogglePill(
                            icon = Icons.Default.FlashlightOn,
                            label = "Torch",
                            subtitle = if (isTorchOn) "On" else "Off",
                            isActive = isTorchOn,
                            onClick = { viewModel.toggleTorch() },
                            modifier = Modifier.weight(1f)
                        )

                        ControlCenterTogglePill(
                            icon = Icons.Default.DoNotDisturb,
                            label = "Silence",
                            subtitle = if (dndActive) "Active" else "Off",
                            isActive = dndActive,
                            onClick = { dndActive = !dndActive },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    MacDockGlassDivider(paddingHorizontal = 0.dp)

                    // Interactive Liquid Glass Sliders
                    Text(
                        text = "LIGHT & SOUND SLIDERS",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White.copy(alpha = 0.55f),
                        style = Typography.labelSmall.copy(shadow = GlassTextShadow)
                    )

                    // Brightness Glass Slider
                    GlassSliderBar(
                        icon = Icons.Default.WbSunny,
                        value = brightnessPercent,
                        onValueChange = { viewModel.setBrightnessPercent(it) },
                        label = "Display Brightness",
                        valueText = "${(brightnessPercent * 100).toInt()}%"
                    )

                    // Volume Glass Slider
                    GlassSliderBar(
                        icon = Icons.Default.VolumeUp,
                        value = volumePercent,
                        onValueChange = { viewModel.setVolumePercent(it) },
                        label = "Media Volume",
                        valueText = "${(volumePercent * 100).toInt()}%"
                    )

                    MacDockGlassDivider(paddingHorizontal = 0.dp)

                    // Liquid Glass Media Player
                    MacDockGlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        cornerRadius = 18.dp
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(9.dp))
                                        .background(Color.White.copy(alpha = 0.18f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.MusicNote,
                                        contentDescription = "Music",
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "Deep Focus Ambient",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color.White,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        style = Typography.bodyMedium.copy(shadow = GlassTextShadow)
                                    )
                                    Text(
                                        text = if (isMusicPlaying) "Playing • Focus Session" else "Paused",
                                        fontSize = 11.sp,
                                        color = if (isMusicPlaying) Color(0xFF4ADE80) else Color.White.copy(alpha = 0.5f),
                                        style = Typography.labelSmall.copy(shadow = GlassTextShadow)
                                    )
                                }
                            }

                            MacDockGlassPill(
                                modifier = Modifier.clickable { viewModel.toggleMusic() },
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = if (isMusicPlaying) "⏸ Pause" else "▶ Play",
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White,
                                    style = Typography.labelSmall.copy(shadow = GlassTextShadow)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ControlCenterTogglePill(
    icon: ImageVector,
    label: String,
    subtitle: String,
    isActive: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    MacDockGlassPill(
        modifier = modifier.clickable(onClick = onClick),
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 11.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(50))
                    .background(
                        if (isActive) Color.White.copy(alpha = 0.38f) else Color.White.copy(alpha = 0.10f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = if (isActive) Color.White else Color.White.copy(alpha = 0.6f),
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = label,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White,
                    style = Typography.bodyMedium.copy(shadow = GlassTextShadow)
                )
                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    color = if (isActive) Color(0xFF4ADE80) else Color.White.copy(alpha = 0.50f),
                    style = Typography.labelSmall.copy(shadow = GlassTextShadow)
                )
            }
        }
    }
}

/**
 * Interactive Liquid Glass Slider Bar:
 * Real draggable slider with liquid glass fill and tactile percentage feedback.
 */
@Composable
fun GlassSliderBar(
    icon: ImageVector,
    value: Float,
    onValueChange: (Float) -> Unit,
    label: String,
    valueText: String,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(40.dp)
            .clip(RoundedCornerShape(50))
            .background(Color.White.copy(alpha = 0.10f))
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    val fraction = (offset.x / size.width).coerceIn(0f, 1f)
                    onValueChange(fraction)
                }
            }
            .pointerInput(Unit) {
                detectDragGestures { change, _ ->
                    change.consume()
                    val fraction = (change.position.x / size.width).coerceIn(0f, 1f)
                    onValueChange(fraction)
                }
            }
    ) {
        val filledWidth = maxWidth * value

        // Active Liquid Fill with chromatic edge
        Box(
            modifier = Modifier
                .width(filledWidth)
                .fillMaxHeight()
                .clip(RoundedCornerShape(50))
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            Color.White.copy(alpha = 0.28f),
                            Color.White.copy(alpha = 0.45f)
                        )
                    )
                )
        )

        // Specular Top Glare
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(14.dp)
                .background(
                    Brush.verticalGradient(
                        listOf(Color.White.copy(alpha = 0.15f), Color.Transparent)
                    )
                )
        )

        // Icons and Percentage Text Layer
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = label,
                    fontSize = 11.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Medium,
                    style = Typography.bodyMedium.copy(shadow = GlassTextShadow)
                )
            }

            Text(
                text = valueText,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White,
                style = Typography.labelSmall.copy(shadow = GlassTextShadow)
            )
        }
    }
}
