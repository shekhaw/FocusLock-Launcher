package com.dumbphone.launcher.ui.screens

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dumbphone.launcher.data.AppItem
import com.dumbphone.launcher.data.ScreenState
import com.dumbphone.launcher.ui.components.ControlCenterSheet
import com.dumbphone.launcher.ui.components.GlassTextShadow
import com.dumbphone.launcher.ui.components.MacDockGlassCard
import com.dumbphone.launcher.ui.components.MacDockGlassPill
import com.dumbphone.launcher.ui.theme.*
import com.dumbphone.launcher.ui.viewmodel.LauncherViewModel
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs

@Composable
fun HomeScreen(
    viewModel: LauncherViewModel,
    modifier: Modifier = Modifier
) {
    val homeApps by viewModel.homeApps.collectAsState()
    val desktopWidgetType by viewModel.desktopWidgetType.collectAsState()

    var currentTime by remember { mutableStateOf("") }
    var currentDate by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        val dateFormat = SimpleDateFormat("EEEE, MMMM d", Locale.getDefault())
        while (true) {
            val now = Date()
            currentTime = timeFormat.format(now)
            currentDate = dateFormat.format(now)
            delay(1000)
        }
    }

    // Outer Edge-to-Edge Container
    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(
                    onLongPress = {
                        viewModel.navigateTo(ScreenState.SETTINGS)
                    }
                )
            }
    ) {
        // Safe inner padding for status bar and gesture navigation bar
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 12.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Status & Control Center Launcher Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "FocusLock Liquid",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White.copy(alpha = 0.50f),
                    style = Typography.labelSmall.copy(shadow = GlassTextShadow)
                )

                MacDockGlassPill(
                    modifier = Modifier
                        .clickable { viewModel.openControlCenter() }
                        .padding(horizontal = 12.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = "⌘ Control",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White,
                        style = Typography.labelSmall.copy(shadow = GlassTextShadow)
                    )
                }
            }

            // 1. Floating Clock, Date & Cycleable Liquid Glass Widget
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = currentTime,
                    style = Typography.displayLarge.copy(shadow = GlassTextShadow),
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = currentDate,
                    style = Typography.bodyMedium.copy(shadow = GlassTextShadow),
                    color = Color.White.copy(alpha = 0.90f)
                )
                Spacer(modifier = Modifier.height(10.dp))

                // Interactive Cycleable Liquid Glass Desktop Widget
                MacDockGlassPill(
                    modifier = Modifier
                        .clickable { viewModel.cycleDesktopWidget() }
                        .padding(horizontal = 16.dp, vertical = 7.dp)
                ) {
                    when (desktopWidgetType) {
                        0 -> {
                            Text(
                                text = "☀️ 24°C • Mostly Sunny • H:27° L:16°",
                                fontSize = 11.5.sp,
                                color = Color.White,
                                style = Typography.labelSmall.copy(shadow = GlassTextShadow)
                            )
                        }
                        1 -> {
                            Text(
                                text = "⚡ Battery 85% • 4.2 GB RAM Free",
                                fontSize = 11.5.sp,
                                color = Color.White,
                                style = Typography.labelSmall.copy(shadow = GlassTextShadow)
                            )
                        }
                        else -> {
                            Text(
                                text = "🌱 42m Focus Today • 3 Mindful Unlocks",
                                fontSize = 11.5.sp,
                                color = Color.White,
                                style = Typography.labelSmall.copy(shadow = GlassTextShadow)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "“Simplicity is the ultimate sophistication.”",
                    style = Typography.labelSmall.copy(shadow = GlassTextShadow),
                    color = Color.White.copy(alpha = 0.60f)
                )
            }

            // 2. Bottom Shelf Section: Authentic Horizontal macOS Dock with Wave Animation
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Access Other Apps (Floating macOS Capsule Pill directly above dock)
                MacDockGlassPill(
                    modifier = Modifier
                        .clickable { viewModel.openFrictionGate() }
                        .padding(horizontal = 20.dp, vertical = 9.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Lock",
                            tint = Color.White.copy(alpha = 0.95f),
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(7.dp))
                        Text(
                            text = "Access Other Apps",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White,
                            style = Typography.bodyMedium.copy(shadow = GlassTextShadow)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Authentic macOS Horizontal Dock Shelf with Wave Magnification
                MacDockHorizontalShelf(
                    homeApps = homeApps,
                    onSlotClick = { _, app ->
                        if (app != null) {
                            viewModel.launchApp(app.packageName)
                        } else {
                            viewModel.navigateTo(ScreenState.SETTINGS)
                        }
                    }
                )
            }
        }

        // Liquid Glass Control Center Overlay
        ControlCenterSheet(viewModel = viewModel)
    }
}

/**
 * Authentic Horizontal macOS Dock Shelf with Interactive Wave Magnification:
 * When dragging or touching along the shelf, items expand and lift in an authentic
 * macOS parabolic magnification wave with spring physics.
 */
@Composable
fun MacDockHorizontalShelf(
    homeApps: List<AppItem?>,
    onSlotClick: (Int, AppItem?) -> Unit,
    modifier: Modifier = Modifier
) {
    var touchX by remember { mutableStateOf<Float?>(null) }
    val totalSlots = 6

    MacDockGlassCard(
        modifier = modifier
            .fillMaxWidth()
            .height(86.dp)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { offset -> touchX = offset.x },
                    onDrag = { change, _ ->
                        change.consume()
                        touchX = change.position.x
                    },
                    onDragEnd = { touchX = null },
                    onDragCancel = { touchX = null }
                )
            },
        cornerRadius = 26.dp,
        refractionHeight = 20.dp,
        refractionOffset = 70.dp,
        dispersion = 0.55f
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 6.dp)
        ) {
            val totalWidthPx = constraints.maxWidth.toFloat()
            val slotWidthPx = totalWidthPx / totalSlots

            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                homeApps.forEachIndexed { index, app ->
                    val slotCenterPx = (index + 0.5f) * slotWidthPx
                    val currentTouch = touchX

                    val targetScale = if (currentTouch != null) {
                        val distance = abs(currentTouch - slotCenterPx)
                        val maxDistance = slotWidthPx * 1.5f
                        if (distance < maxDistance) {
                            val factor = (maxDistance - distance) / maxDistance
                            1.0f + 0.36f * factor
                        } else 1.0f
                    } else 1.0f

                    val animatedScale by animateFloatAsState(
                        targetValue = targetScale,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessLow
                        ),
                        label = "dock_slot_scale_$index"
                    )

                    val animatedOffsetY by animateDpAsState(
                        targetValue = if (animatedScale > 1.05f) (-(animatedScale - 1f) * 36f).dp else 0.dp,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessLow
                        ),
                        label = "dock_slot_offset_$index"
                    )

                    MacDockAppSlot(
                        app = app,
                        scale = animatedScale,
                        offsetY = animatedOffsetY,
                        onClick = { onSlotClick(index, app) }
                    )
                }
            }
        }
    }
}

/**
 * Single App Slot inside the macOS Horizontal Dock Shelf
 */
@Composable
fun MacDockAppSlot(
    app: AppItem?,
    scale: Float,
    offsetY: Dp,
    onClick: () -> Unit
) {
    val isAssigned = (app != null)

    Column(
        modifier = Modifier
            .width(50.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                translationY = offsetY.toPx()
            }
            .clickable(onClick = onClick)
            .padding(vertical = 2.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // App Squircle Icon
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(RoundedCornerShape(11.dp))
                .background(
                    if (isAssigned) {
                        Brush.linearGradient(
                            listOf(
                                Color.White.copy(alpha = 0.28f),
                                Color.White.copy(alpha = 0.12f)
                            )
                        )
                    } else {
                        Brush.linearGradient(
                            listOf(
                                Color.White.copy(alpha = 0.08f),
                                Color.White.copy(alpha = 0.03f)
                            )
                        )
                    }
                )
                .border(
                    width = 1.dp,
                    brush = Brush.verticalGradient(
                        listOf(
                            if (isAssigned) Color.White.copy(alpha = 0.70f) else Color.White.copy(alpha = 0.25f),
                            if (isAssigned) Color.White.copy(alpha = 0.20f) else Color.White.copy(alpha = 0.08f)
                        )
                    ),
                    shape = RoundedCornerShape(11.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            if (app != null) {
                val initial = if (app.label.isNotBlank()) app.label.take(1).uppercase() else "•"
                Text(
                    text = initial,
                    color = Color.White,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    style = Typography.titleMedium.copy(shadow = GlassTextShadow)
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Slot",
                    tint = Color.White.copy(alpha = 0.45f),
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Label below squircle
        Text(
            text = app?.label ?: "Add",
            fontSize = 10.sp,
            color = if (isAssigned) Color.White else Color.White.copy(alpha = 0.50f),
            fontWeight = FontWeight.Normal,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            style = Typography.labelSmall.copy(shadow = GlassTextShadow),
            modifier = Modifier.fillMaxWidth()
        )

        // 3D Glass Pip indicator dot under assigned app
        if (isAssigned) {
            Spacer(modifier = Modifier.height(2.dp))
            Box(
                modifier = Modifier
                    .size(3.dp)
                    .clip(RoundedCornerShape(50))
                    .background(Color.White.copy(alpha = 0.90f))
            )
        } else {
            Spacer(modifier = Modifier.height(5.dp))
        }
    }
}
