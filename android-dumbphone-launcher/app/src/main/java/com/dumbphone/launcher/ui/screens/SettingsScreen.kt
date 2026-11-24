package com.dumbphone.launcher.ui.screens

import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dumbphone.launcher.data.AppItem
import com.dumbphone.launcher.data.ScreenState
import com.dumbphone.launcher.data.UnlockGateType
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
    val wallpaperUpdateTrigger by viewModel.wallpaperUpdateTrigger.collectAsState()

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

    // Custom wallpaper preview bitmap
    val customWallpaperBitmap = remember(hasCustomWallpaper, wallpaperUpdateTrigger) {
        if (hasCustomWallpaper) {
            val file = viewModel.getCustomWallpaperFile()
            if (file.exists() && file.length() > 0) {
                try {
                    BitmapFactory.decodeFile(file.absolutePath)
                } catch (e: Exception) {
                    null
                }
            } else null
        } else null
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .then(
                if (customWallpaperBitmap == null) {
                    Modifier.background(wallpaperTheme.getBackgroundBrush())
                } else {
                    Modifier
                }
            )
    ) {
        // Background photo if set
        if (customWallpaperBitmap != null) {
            Image(
                bitmap = customWallpaperBitmap.asImageBitmap(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.65f))
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Launcher Settings",
                    style = Typography.titleLarge,
                    color = Color.White
                )
                Button(
                    onClick = { viewModel.navigateTo(ScreenState.HOME) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GlassWhiteMedium,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Done")
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                // Section 0: Wallpaper & Background Customization
                item {
                    Text(
                        text = "WALLPAPER & BACKGROUND",
                        style = Typography.labelSmall,
                        color = Color.White.copy(alpha = 0.6f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(22.dp))
                            .background(
                                Brush.verticalGradient(
                                    listOf(MacDockGlassTop, MacDockGlassBottom)
                                )
                            )
                            .border(
                                BorderStroke(
                                    1.dp,
                                    Brush.verticalGradient(
                                        listOf(MacDockBorderTop, MacDockBorderMid)
                                    )
                                ),
                                shape = RoundedCornerShape(22.dp)
                            )
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
                                    style = Typography.bodyLarge,
                                    color = Color.White,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = if (hasCustomWallpaper) "Active: Custom Photo" else "Choose any photo from your device",
                                    style = Typography.labelSmall,
                                    color = if (hasCustomWallpaper) Color(0xFF4ADE80) else Color.White.copy(alpha = 0.5f)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Button(
                                onClick = { photoPickerLauncher.launch("image/*") },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.White.copy(alpha = 0.22f),
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Choose Photo")
                            }
                        }

                        if (hasCustomWallpaper) {
                            Button(
                                onClick = { viewModel.clearCustomWallpaper() },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0x33EF4444),
                                    contentColor = Color(0xFFFCA5A5)
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Remove Photo & Use Fluid Wallpaper")
                            }
                        }

                        HorizontalDivider(
                            color = MacDockDivider,
                            thickness = 0.8.dp,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )

                        Text(
                            text = "LIVING FLUID PRESETS",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White.copy(alpha = 0.5f)
                        )

                        WallpaperTheme.values().forEach { theme ->
                            val isSelected = (!hasCustomWallpaper && theme == wallpaperTheme)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSelected) Color.White.copy(alpha = 0.20f) else Color.Transparent)
                                    .clickable { viewModel.setWallpaperTheme(theme) }
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = theme.displayName,
                                    style = Typography.bodyMedium,
                                    color = Color.White
                                )
                                if (isSelected) {
                                    Text("✓ Active", style = Typography.labelSmall, color = Color.White)
                                }
                            }
                        }
                    }
                }

                // Section 1: The 6 Home Screen Apps
                item {
                    Text(
                        text = "HOME SCREEN APPS (UP TO 6 IN DOCK)",
                        style = Typography.labelSmall,
                        color = Color.White.copy(alpha = 0.6f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(22.dp))
                            .background(
                                Brush.verticalGradient(
                                    listOf(MacDockGlassTop, MacDockGlassBottom)
                                )
                            )
                            .border(
                                BorderStroke(
                                    1.dp,
                                    Brush.verticalGradient(
                                        listOf(MacDockBorderTop, MacDockBorderMid)
                                    )
                                ),
                                shape = RoundedCornerShape(22.dp)
                            )
                            .padding(14.dp),
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
                                    style = Typography.bodyMedium,
                                    color = if (app != null) Color.White else Color.White.copy(alpha = 0.4f)
                                )
                                Text(
                                    text = "Change ✎",
                                    style = Typography.labelSmall,
                                    color = Color.White.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }
                }

                // Section 2: Friction Challenge Type
                item {
                    Text(
                        text = "IMPORTANCE GATE CHALLENGE TYPE",
                        style = Typography.labelSmall,
                        color = Color.White.copy(alpha = 0.6f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        FilterChip(
                            selected = (gateType == UnlockGateType.PHRASE),
                            onClick = { viewModel.setGateMode(UnlockGateType.PHRASE) },
                            label = { Text("Mindful Phrase") },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = (gateType == UnlockGateType.PIN),
                            onClick = { viewModel.setGateMode(UnlockGateType.PIN) },
                            label = { Text("Phone PIN") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Section 3: Configure Phrase or PIN
                if (gateType == UnlockGateType.PHRASE) {
                    item {
                        Text(
                            text = "CUSTOM MINDFUL PHRASE",
                            style = Typography.labelSmall,
                            color = Color.White.copy(alpha = 0.6f)
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = phraseText,
                            onValueChange = {
                                phraseText = it
                                viewModel.updateCustomPhrase(it)
                            },
                            colors = TextFieldDefaults.outlinedTextFieldColors(
                                focusedBorderColor = GlassWhiteHighlight,
                                unfocusedBorderColor = GlassWhiteBorder,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                containerColor = GlassWhiteSubtle
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                } else {
                    item {
                        Text(
                            text = "4-DIGIT PIN PASSCODE",
                            style = Typography.labelSmall,
                            color = Color.White.copy(alpha = 0.6f)
                        )
                        Spacer(modifier = Modifier.height(8.dp))

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
                                focusedBorderColor = GlassWhiteHighlight,
                                unfocusedBorderColor = GlassWhiteBorder,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                containerColor = GlassWhiteSubtle
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                // Section 4: System Integration
                item {
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = {
                            val intent = Intent(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS)
                            try {
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                val altIntent = Intent(Settings.ACTION_HOME_SETTINGS)
                                context.startActivity(altIntent)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = GlassWhiteMedium,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Set as Default Android Home App 📱")
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
