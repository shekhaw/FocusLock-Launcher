package com.dumbphone.launcher.ui.screens

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dumbphone.launcher.data.AppItem
import com.dumbphone.launcher.data.ScreenState
import com.dumbphone.launcher.data.UnlockGateType
import com.dumbphone.launcher.ui.components.GlassTextShadow
import com.dumbphone.launcher.ui.components.MacDockGlassCard
import com.dumbphone.launcher.ui.components.MacDockGlassDivider
import com.dumbphone.launcher.ui.components.MacDockGlassPill
import com.dumbphone.launcher.ui.theme.*
import com.dumbphone.launcher.ui.viewmodel.LauncherViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: LauncherViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val allApps by viewModel.allInstalledApps.collectAsState()
    val homeApps by viewModel.homeApps.collectAsState()
    val gateType by viewModel.gateType.collectAsState()
    val currentPhrase by viewModel.customPhrase.collectAsState()
    val currentPin by viewModel.targetPin.collectAsState()
    val wallpaperTheme by viewModel.wallpaperTheme.collectAsState()
    val hasCustomWallpaper by viewModel.hasCustomWallpaper.collectAsState()
    val refractionOffset by viewModel.glassRefractionOffset.collectAsState()
    val dispersion by viewModel.glassDispersion.collectAsState()
    val tintTone by viewModel.glassTintTone.collectAsState()

    var phraseText by remember(currentPhrase) { mutableStateOf(currentPhrase) }
    var pinText by remember(currentPin) { mutableStateOf(currentPin) }

    var activeSlotToAssign by remember { mutableStateOf<Int?>(null) }

    // Photo picker launcher
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.setCustomPhotoWallpaper(uri)
        }
    }

    // Transparent container: wallpaper from MainActivity is visible underneath!
    Box(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp, vertical = 14.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Launcher Settings",
                    style = Typography.titleLarge.copy(shadow = GlassTextShadow),
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold
                )

                MacDockGlassPill(
                    modifier = Modifier
                        .clickable { viewModel.navigateTo(ScreenState.HOME) }
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "Done ✓",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        style = Typography.labelSmall.copy(shadow = GlassTextShadow)
                    )
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Section 0: Wallpaper & Background Customization
                item {
                    Text(
                        text = "WALLPAPER & BACKGROUND",
                        style = Typography.labelSmall.copy(shadow = GlassTextShadow),
                        color = Color.White.copy(alpha = 0.65f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    MacDockGlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        cornerRadius = 24.dp
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Custom Photo Choice
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Custom Photo Wallpaper",
                                        style = Typography.bodyLarge.copy(shadow = GlassTextShadow),
                                        color = Color.White,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = if (hasCustomWallpaper) "Active: Custom Photo" else "Choose any photo from gallery",
                                        style = Typography.labelSmall.copy(shadow = GlassTextShadow),
                                        color = if (hasCustomWallpaper) Color(0xFF4ADE80) else Color.White.copy(alpha = 0.55f)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                MacDockGlassPill(
                                    modifier = Modifier
                                        .clickable { photoPickerLauncher.launch("image/*") }
                                        .padding(horizontal = 14.dp, vertical = 7.dp)
                                ) {
                                    Text(
                                        text = "Choose Photo 📷",
                                        fontSize = 12.sp,
                                        color = Color.White,
                                        fontWeight = FontWeight.Medium,
                                        style = Typography.labelSmall.copy(shadow = GlassTextShadow)
                                    )
                                }
                            }

                            if (hasCustomWallpaper) {
                                MacDockGlassPill(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { viewModel.clearCustomWallpaper() }
                                        .padding(vertical = 10.dp)
                                ) {
                                    Text(
                                        text = "Remove Photo & Use Fluid Wallpaper",
                                        fontSize = 13.sp,
                                        color = Color(0xFFFCA5A5),
                                        fontWeight = FontWeight.Medium,
                                        style = Typography.bodyMedium.copy(shadow = GlassTextShadow),
                                        modifier = Modifier.align(Alignment.Center)
                                    )
                                }
                            }

                            MacDockGlassDivider(paddingHorizontal = 0.dp)

                            Text(
                                text = "LIVING FLUID PRESETS",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White.copy(alpha = 0.55f),
                                style = Typography.labelSmall.copy(shadow = GlassTextShadow)
                            )

                            WallpaperTheme.values().forEach { theme ->
                                val isSelected = (!hasCustomWallpaper && theme == wallpaperTheme)
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (isSelected) Color.White.copy(alpha = 0.18f) else Color.Transparent)
                                        .clickable { viewModel.setWallpaperTheme(theme) }
                                        .padding(horizontal = 14.dp, vertical = 10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = theme.displayName,
                                        style = Typography.bodyMedium.copy(shadow = GlassTextShadow),
                                        color = Color.White
                                    )
                                    if (isSelected) {
                                        Text("✓ Active", style = Typography.labelSmall.copy(shadow = GlassTextShadow), color = Color.White)
                                    }
                                }
                            }
                        }
                    }
                }

                // Section 0.5: Liquid Glass Tuning Studio
                item {
                    Text(
                        text = "LIQUID GLASS TUNING STUDIO",
                        style = Typography.labelSmall.copy(shadow = GlassTextShadow),
                        color = Color.White.copy(alpha = 0.65f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    MacDockGlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        cornerRadius = 24.dp
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            // 1. Refraction Magnification Lens
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Optical Magnification Lens",
                                        style = Typography.bodyMedium.copy(shadow = GlassTextShadow),
                                        color = Color.White,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = "-${refractionOffset.toInt()}dp",
                                        style = Typography.labelSmall.copy(shadow = GlassTextShadow),
                                        color = Color(0xFF4ADE80)
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Slider(
                                    value = refractionOffset,
                                    onValueChange = { viewModel.setGlassRefractionOffset(it) },
                                    valueRange = 30f..120f,
                                    steps = 8,
                                    colors = SliderDefaults.colors(
                                        thumbColor = Color.White,
                                        activeTrackColor = Color.White,
                                        inactiveTrackColor = Color.White.copy(alpha = 0.2f)
                                    )
                                )
                            }

                            MacDockGlassDivider(paddingHorizontal = 0.dp)

                            // 2. Prism Rainbow Dispersion
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Rainbow Prism Dispersion",
                                        style = Typography.bodyMedium.copy(shadow = GlassTextShadow),
                                        color = Color.White,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = "${(dispersion * 100).toInt()}%",
                                        style = Typography.labelSmall.copy(shadow = GlassTextShadow),
                                        color = Color(0xFF38BDF8)
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Slider(
                                    value = dispersion,
                                    onValueChange = { viewModel.setGlassDispersion(it) },
                                    valueRange = 0.0f..1.0f,
                                    steps = 9,
                                    colors = SliderDefaults.colors(
                                        thumbColor = Color.White,
                                        activeTrackColor = Color.White,
                                        inactiveTrackColor = Color.White.copy(alpha = 0.2f)
                                    )
                                )
                            }

                            MacDockGlassDivider(paddingHorizontal = 0.dp)

                            // 3. Glass Tint Tone Presets
                            Text(
                                text = "GLASS TINT TONES",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White.copy(alpha = 0.55f),
                                style = Typography.labelSmall.copy(shadow = GlassTextShadow)
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                listOf(
                                    Triple("CLEAR", "💎 Clear", 0.0f),
                                    Triple("SMOKE", "🌑 Smoke", 0.12f),
                                    Triple("ICE", "❄️ Ice", 0.12f),
                                    Triple("ROSE", "🌸 Rose", 0.12f),
                                    Triple("EMERALD", "🌲 Jade", 0.12f)
                                ).forEach { (toneKey, label, alpha) ->
                                    val isSelected = (tintTone == toneKey)
                                    MacDockGlassPill(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable { viewModel.setGlassTintTone(toneKey, alpha) }
                                            .padding(vertical = 7.dp)
                                    ) {
                                        Text(
                                            text = label,
                                            fontSize = 9.5.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSelected) Color.White else Color.White.copy(alpha = 0.6f),
                                            textAlign = TextAlign.Center,
                                            style = Typography.labelSmall.copy(shadow = GlassTextShadow),
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }
                                }
                            }

                            MacDockGlassDivider(paddingHorizontal = 0.dp)

                            // 4. Reset Button
                            MacDockGlassPill(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { viewModel.resetGlassToDefault() }
                                    .padding(vertical = 9.dp)
                            ) {
                                Text(
                                    text = "Reset Glass to Factory Default ↺",
                                    fontSize = 12.sp,
                                    color = Color.White.copy(alpha = 0.85f),
                                    fontWeight = FontWeight.Medium,
                                    style = Typography.bodyMedium.copy(shadow = GlassTextShadow),
                                    modifier = Modifier.align(Alignment.Center)
                                )
                            }
                        }
                    }
                }

                // Section 1: The 6 Home Screen Apps
                item {
                    Text(
                        text = "HOME SCREEN APPS (UP TO 6 IN DOCK)",
                        style = Typography.labelSmall.copy(shadow = GlassTextShadow),
                        color = Color.White.copy(alpha = 0.65f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    MacDockGlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        cornerRadius = 24.dp
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            for (i in 0 until 6) {
                                val app = homeApps.getOrNull(i)
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { activeSlotToAssign = i }
                                        .padding(vertical = 6.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Slot ${i + 1}: ${app?.label ?: "(Empty Slot)"}",
                                        style = Typography.bodyMedium.copy(shadow = GlassTextShadow),
                                        color = if (app != null) Color.White else Color.White.copy(alpha = 0.45f)
                                    )
                                    Text(
                                        text = "Change ✎",
                                        style = Typography.labelSmall.copy(shadow = GlassTextShadow),
                                        color = Color.White.copy(alpha = 0.85f)
                                    )
                                }
                                if (i < 5) {
                                    MacDockGlassDivider(paddingHorizontal = 0.dp)
                                }
                            }
                        }
                    }
                }

                // Section 2: Friction Challenge Type
                item {
                    Text(
                        text = "IMPORTANCE GATE CHALLENGE TYPE",
                        style = Typography.labelSmall.copy(shadow = GlassTextShadow),
                        color = Color.White.copy(alpha = 0.65f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        MacDockGlassPill(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { viewModel.setGateMode(UnlockGateType.PHRASE) }
                                .padding(vertical = 10.dp)
                        ) {
                            Text(
                                text = "Mindful Phrase ${if (gateType == UnlockGateType.PHRASE) "✓" else ""}",
                                style = Typography.bodyMedium.copy(shadow = GlassTextShadow),
                                color = if (gateType == UnlockGateType.PHRASE) Color.White else Color.White.copy(alpha = 0.6f),
                                modifier = Modifier.align(Alignment.Center)
                            )
                        }

                        MacDockGlassPill(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { viewModel.setGateMode(UnlockGateType.PIN) }
                                .padding(vertical = 10.dp)
                        ) {
                            Text(
                                text = "Phone PIN ${if (gateType == UnlockGateType.PIN) "✓" else ""}",
                                style = Typography.bodyMedium.copy(shadow = GlassTextShadow),
                                color = if (gateType == UnlockGateType.PIN) Color.White else Color.White.copy(alpha = 0.6f),
                                modifier = Modifier.align(Alignment.Center)
                            )
                        }
                    }
                }

                // Section 3: Configure Phrase or PIN
                if (gateType == UnlockGateType.PHRASE) {
                    item {
                        Text(
                            text = "CUSTOM MINDFUL PHRASE",
                            style = Typography.labelSmall.copy(shadow = GlassTextShadow),
                            color = Color.White.copy(alpha = 0.65f)
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        MacDockGlassPill(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = phraseText,
                                onValueChange = {
                                    phraseText = it
                                    viewModel.updateCustomPhrase(it)
                                },
                                colors = TextFieldDefaults.outlinedTextFieldColors(
                                    focusedBorderColor = Color.Transparent,
                                    unfocusedBorderColor = Color.Transparent,
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    containerColor = Color.Transparent
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp)
                            )
                        }
                    }
                } else {
                    item {
                        Text(
                            text = "4-DIGIT PIN PASSCODE",
                            style = Typography.labelSmall.copy(shadow = GlassTextShadow),
                            color = Color.White.copy(alpha = 0.65f)
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        MacDockGlassPill(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = pinText,
                                onValueChange = {
                                    if (it.length <= 4 && it.all { ch -> ch.isDigit() }) {
                                        pinText = it
                                        if (it.length == 4) {
                                            viewModel.updatePin(it)
                                        }
                                    }
                                },
                                colors = TextFieldDefaults.outlinedTextFieldColors(
                                    focusedBorderColor = Color.Transparent,
                                    unfocusedBorderColor = Color.Transparent,
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    containerColor = Color.Transparent
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp)
                            )
                        }
                    }
                }

                // Section 4: System Integration
                item {
                    Spacer(modifier = Modifier.height(10.dp))
                    MacDockGlassPill(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                val intent = Intent(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS)
                                try {
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    val altIntent = Intent(Settings.ACTION_HOME_SETTINGS)
                                    context.startActivity(altIntent)
                                }
                            }
                            .padding(vertical = 14.dp)
                    ) {
                        Text(
                            text = "Set as Default Android Home App 📱",
                            style = Typography.bodyMedium.copy(shadow = GlassTextShadow),
                            color = Color.White,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                }
            }
        }

        // App selection dialog
        activeSlotToAssign?.let { slotIndex ->
            AlertDialog(
                onDismissRequest = { activeSlotToAssign = null },
                title = { Text("Choose App for Slot ${slotIndex + 1}", color = Color.White) },
                text = {
                    LazyColumn(
                        modifier = Modifier.height(350.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item {
                            Text(
                                text = "— Remove App (Leave Slot Empty) —",
                                style = Typography.bodyMedium,
                                color = AccentDanger,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        viewModel.updateHomeSlot(slotIndex, null)
                                        activeSlotToAssign = null
                                    }
                                    .padding(vertical = 8.dp)
                            )
                        }
                        items(allApps.size) { index ->
                            val item = allApps[index]
                            Text(
                                text = item.label,
                                style = Typography.bodyMedium,
                                color = Color.White,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        viewModel.updateHomeSlot(slotIndex, item.packageName)
                                        activeSlotToAssign = null
                                    }
                                    .padding(vertical = 8.dp)
                            )
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { activeSlotToAssign = null }) {
                        Text("Cancel", color = Color.White)
                    }
                },
                containerColor = DarkSurfaceElevated,
                titleContentColor = Color.White
            )
        }
    }
}
