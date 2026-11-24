package com.dumbphone.launcher.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dumbphone.launcher.data.ScreenState
import com.dumbphone.launcher.data.UnlockGateType
import com.dumbphone.launcher.ui.components.GlassTextShadow
import com.dumbphone.launcher.ui.components.MacDockGlassCard
import com.dumbphone.launcher.ui.components.MacDockGlassPill
import com.dumbphone.launcher.ui.theme.*
import com.dumbphone.launcher.ui.viewmodel.LauncherViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FrictionGateScreen(
    viewModel: LauncherViewModel,
    modifier: Modifier = Modifier
) {
    val gateType by viewModel.gateType.collectAsState()
    val customPhrase by viewModel.customPhrase.collectAsState()
    val enteredPhrase by viewModel.enteredPhrase.collectAsState()
    val enteredPin by viewModel.enteredPin.collectAsState()

    val isPhraseMatch = viewModel.isPhraseMatching()

    // Transparent container: wallpaper from MainActivity is visible underneath!
    Box(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 22.dp, vertical = 20.dp),
        contentAlignment = Alignment.Center
    ) {
        // macOS Glass Card housing the friction gate
        MacDockGlassCard(
            modifier = Modifier.fillMaxWidth(),
            cornerRadius = 28.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "🛡️",
                    fontSize = 36.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Text(
                    text = "Is this important?",
                    style = Typography.titleLarge.copy(shadow = GlassTextShadow),
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Take a breath. Avoid mindless autopilot browsing.",
                    style = Typography.bodySmall.copy(shadow = GlassTextShadow),
                    color = Color.White.copy(alpha = 0.75f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 12.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                if (gateType == UnlockGateType.PHRASE) {
                    // MINDFUL PHRASE GATE
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "TYPE THIS INTENTIONAL PHRASE TO UNLOCK:",
                            style = Typography.labelSmall.copy(shadow = GlassTextShadow),
                            color = Color.White.copy(alpha = 0.6f)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Target phrase box
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color.White.copy(alpha = 0.14f))
                                .border(1.dp, Color.White.copy(alpha = 0.25f), RoundedCornerShape(14.dp))
                                .padding(14.dp)
                        ) {
                            Text(
                                text = "\"$customPhrase\"",
                                style = Typography.bodyMedium.copy(shadow = GlassTextShadow),
                                color = Color.White,
                                fontStyle = FontStyle.Italic
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Phrase input pill
                        MacDockGlassPill(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = enteredPhrase,
                                onValueChange = { viewModel.setEnteredPhrase(it) },
                                placeholder = {
                                    Text(
                                        "Type exact phrase above...",
                                        color = Color.White.copy(alpha = 0.45f),
                                        fontSize = 14.sp
                                    )
                                },
                                colors = TextFieldDefaults.outlinedTextFieldColors(
                                    focusedBorderColor = Color.Transparent,
                                    unfocusedBorderColor = Color.Transparent,
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    containerColor = Color.Transparent
                                ),
                                singleLine = false,
                                maxLines = 3,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        // Unlock button
                        Button(
                            onClick = { viewModel.unlockVault() },
                            enabled = isPhraseMatch,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.White.copy(alpha = if (isPhraseMatch) 0.95f else 0.25f),
                                contentColor = if (isPhraseMatch) Color(0xFF101012) else Color.White.copy(alpha = 0.4f),
                                disabledContainerColor = Color.White.copy(alpha = 0.15f),
                                disabledContentColor = Color.White.copy(alpha = 0.35f)
                            ),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                        ) {
                            Text(
                                text = "Unlock All Apps",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 15.sp
                            )
                        }
                    }
                } else {
                    // PIN PASSCODE GATE
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // PIN Dots
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.padding(bottom = 24.dp)
                        ) {
                            for (i in 0 until 4) {
                                val isFilled = i < enteredPin.length
                                Box(
                                    modifier = Modifier
                                        .size(16.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (isFilled) Color.White else Color.White.copy(alpha = 0.25f)
                                        )
                                        .border(1.dp, Color.White.copy(alpha = 0.5f), CircleShape)
                                )
                            }
                        }

                        // Glass Keypad
                        val keys = listOf(
                            listOf('1', '2', '3'),
                            listOf('4', '5', '6'),
                            listOf('7', '8', '9'),
                            listOf('C', '0', '⌫')
                        )

                        Column(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            keys.forEach { row ->
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(18.dp)
                                ) {
                                    row.forEach { key ->
                                        Box(
                                            modifier = Modifier
                                                .size(58.dp)
                                                .clip(CircleShape)
                                                .background(Color.White.copy(alpha = 0.14f))
                                                .border(1.dp, Color.White.copy(alpha = 0.28f), CircleShape)
                                                .clickable {
                                                    when (key) {
                                                        'C' -> viewModel.clearPin()
                                                        '⌫' -> viewModel.backspacePin()
                                                        else -> viewModel.onPinDigit(key)
                                                    }
                                                },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = key.toString(),
                                                style = Typography.titleLarge.copy(shadow = GlassTextShadow),
                                                color = Color.White
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                TextButton(
                    onClick = { viewModel.navigateTo(ScreenState.HOME) }
                ) {
                    Text(
                        text = "← Nevermind, go back home",
                        style = Typography.bodyMedium.copy(shadow = GlassTextShadow),
                        color = Color.White.copy(alpha = 0.70f)
                    )
                }
            }
        }
    }
}
