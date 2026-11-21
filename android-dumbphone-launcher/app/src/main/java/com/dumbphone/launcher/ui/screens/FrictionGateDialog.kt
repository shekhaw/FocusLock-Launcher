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
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dumbphone.launcher.data.ScreenState
import com.dumbphone.launcher.data.UnlockGateType
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

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(OledBlack)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp, vertical = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "🛡️",
                fontSize = 36.sp,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Text(
                text = "Is this important?",
                style = Typography.titleLarge,
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Take a breath. Avoid mindless autopilot browsing.",
                style = Typography.bodyMedium,
                color = TextSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            if (gateType == UnlockGateType.PHRASE) {
                // MINDFUL PHRASE GATE
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "TYPE THIS INTENTIONAL PHRASE TO UNLOCK:",
                        style = Typography.labelSmall,
                        color = TextMuted
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(DarkSurfaceElevated, RoundedCornerShape(8.dp))
                            .border(1.dp, BorderSubtle, RoundedCornerShape(8.dp))
                            .padding(14.dp)
                    ) {
                        Text(
                            text = "\"$customPhrase\"",
                            style = Typography.bodyMedium,
                            color = TextPrimary,
                            fontStyle = FontStyle.Italic
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = enteredPhrase,
                        onValueChange = { viewModel.setEnteredPhrase(it) },
                        placeholder = {
                            Text(
                                "Type the exact phrase above...",
                                color = TextMuted,
                                fontSize = 14.sp
                            )
                        },
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            focusedBorderColor = AccentPurple,
                            unfocusedBorderColor = BorderSubtle,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            containerColor = DarkSurface
                        ),
                        singleLine = false,
                        maxLines = 3,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    Button(
                        onClick = { viewModel.unlockVault() },
                        enabled = isPhraseMatch,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = TextPrimary,
                            contentColor = OledBlack,
                            disabledContainerColor = DarkSurfaceElevated,
                            disabledContentColor = TextMuted
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
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
                    // PIN Dots (4 digits)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.padding(bottom = 32.dp)
                    ) {
                        for (i in 0 until 4) {
                            val isFilled = i < enteredPin.length
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .clip(CircleShape)
                                    .background(if (isFilled) TextPrimary else BorderSubtle)
                                    .border(1.dp, BorderSubtle, CircleShape)
                            )
                        }
                    }

                    // Keypad
                    val keys = listOf(
                        listOf('1', '2', '3'),
                        listOf('4', '5', '6'),
                        listOf('7', '8', '9'),
                        listOf('C', '0', '⌫')
                    )

                    Column(
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        keys.forEach { row ->
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(20.dp)
                            ) {
                                row.forEach { key ->
                                    Box(
                                        modifier = Modifier
                                            .size(62.dp)
                                            .clip(CircleShape)
                                            .background(DarkSurfaceElevated)
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
                                            style = Typography.titleLarge,
                                            color = TextPrimary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            TextButton(
                onClick = { viewModel.navigateTo(ScreenState.HOME) }
            ) {
                Text(
                    text = "← Nevermind, go back home",
                    style = Typography.bodyMedium,
                    color = TextMuted
                )
            }
        }
    }
}
