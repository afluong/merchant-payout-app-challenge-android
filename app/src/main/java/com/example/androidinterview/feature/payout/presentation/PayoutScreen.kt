package com.example.androidinterview.feature.payout.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.fragment.app.FragmentActivity
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.androidinterview.core.ui.theme.BackgroundLight
import com.example.androidinterview.core.ui.theme.BorderLight
import com.example.androidinterview.core.ui.theme.Dimens
import com.example.androidinterview.core.ui.theme.DisabledContainer
import com.example.androidinterview.core.ui.theme.DisabledContent
import com.example.androidinterview.core.ui.theme.ErrorRed
import com.example.androidinterview.core.ui.theme.PrimaryBlue
import com.example.androidinterview.core.ui.theme.SuccessGreen
import com.example.androidinterview.core.ui.theme.SurfaceWhite
import com.example.androidinterview.core.ui.theme.TextPlaceholder
import com.example.androidinterview.core.ui.theme.TextPrimary
import com.example.androidinterview.core.ui.theme.TextSecondary

@Composable
fun PayoutRoute(viewModel: PayoutViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    var showSecurityWarning by remember { mutableStateOf(false) }

    RememberScreenshotProtection(
        onScreenshotDetected = {
            showSecurityWarning = true
        }
    )

    PayoutScreen(
        uiState = uiState,
        onIntent = viewModel::onIntent,
        showSecurityWarning = showSecurityWarning,
        onDismissSecurityWarning = { showSecurityWarning = false }
    )
}

@Composable
fun PayoutScreen(
    uiState: PayoutUiState,
    onIntent: (PayoutIntent) -> Unit,
    showSecurityWarning: Boolean,
    onDismissSecurityWarning: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundLight)
    ) {
        when (uiState) {
            is PayoutUiState.InputForm -> {
                PayoutInputFormContent(state = uiState, onIntent = onIntent)
            }

            is PayoutUiState.ConfirmDialog -> {
                PayoutInputFormContent(
                    state = PayoutUiState.InputForm(
                        amount = uiState.amount,
                        currency = uiState.currency,
                        iban = uiState.iban
                    ),
                    onIntent = onIntent
                )
                ConfirmPayoutModal(state = uiState, onIntent = onIntent)
            }

            is PayoutUiState.BiometricPromptState -> {
                HandleBiometricPrompt(
                    onSuccess = { onIntent(PayoutIntent.BiometricSuccess) },
                    onError = { errorMsg -> onIntent(PayoutIntent.BiometricFailed(errorMsg)) }
                )
            }

            is PayoutUiState.Loading -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = PrimaryBlue
                )
            }

            is PayoutUiState.Success -> {
                PayoutSuccessScreen(onIntent = onIntent)
            }

            is PayoutUiState.Error -> {
                PayoutErrorScreen(message = uiState.message, onIntent = onIntent)
            }
        }

        if (showSecurityWarning) {
            AlertDialog(
                onDismissRequest = onDismissSecurityWarning,
                title = {
                    Text(
                        text = "Security Reminder",
                        fontWeight = FontWeight.Bold,
                        fontSize = Dimens.TextTitleLarge
                    )
                },
                text = {
                    Text(
                        text = "Please keep your financial data private. Screenshots may contain sensitive information.",
                        fontSize = Dimens.TextBody,
                        color = TextSecondary
                    )
                },
                confirmButton = {
                    TextButton(onClick = onDismissSecurityWarning) {
                        Text("OK", fontWeight = FontWeight.Bold, color = PrimaryBlue)
                    }
                },
                containerColor = SurfaceWhite,
                shape = RoundedCornerShape(Dimens.CornerRadiusDialog)
            )
        }
    }
}

@Composable
private fun PayoutInputFormContent(
    state: PayoutUiState.InputForm,
    onIntent: (PayoutIntent) -> Unit
) {
    var currencyDropdownExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = Dimens.PaddingLarge, vertical = Dimens.PaddingNormal)
    ) {
        Text(
            text = "Send Payout",
            fontSize = Dimens.TextScreenTitle,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
            modifier = Modifier.padding(bottom = Dimens.PaddingXLarge)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Dimens.PaddingNormal)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Amount",
                    fontSize = Dimens.TextBody,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    modifier = Modifier.padding(bottom = Dimens.PaddingSmall)
                )
                CustomInputField(
                    value = state.amount,
                    onValueChange = { onIntent(PayoutIntent.UpdateAmount(it)) },
                    placeholder = "0.00",
                    keyboardType = KeyboardType.Number,
                    modifier = Modifier.height(Dimens.InputHeight)
                )
            }

            // Champ Currency (Dropdown)
            Column(modifier = Modifier.width(Dimens.CurrencyDropdownWidth)) {
                Text(
                    text = "Currency",
                    fontSize = Dimens.TextBody,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    modifier = Modifier.padding(bottom = Dimens.PaddingSmall)
                )

                Box {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(Dimens.InputHeight)
                            .background(SurfaceWhite, RoundedCornerShape(Dimens.CornerRadiusNormal))
                            .border(Dimens.BorderWidth, BorderLight, RoundedCornerShape(Dimens.CornerRadiusNormal))
                            .clickable { currencyDropdownExpanded = true }
                            .padding(horizontal = Dimens.PaddingNormal),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = state.currency.name,
                                fontSize = Dimens.TextTitleMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = null,
                                tint = TextSecondary
                            )
                        }
                    }

                    DropdownMenu(
                        expanded = currencyDropdownExpanded,
                        onDismissRequest = { currencyDropdownExpanded = false }
                    ) {
                        state.availableCurrencies.forEach { currency ->
                            DropdownMenuItem(
                                text = { Text(currency.name, fontWeight = FontWeight.SemiBold) },
                                onClick = {
                                    onIntent(PayoutIntent.UpdateCurrency(currency))
                                    currencyDropdownExpanded = false
                                }
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(Dimens.PaddingXLarge))

        Text(
            text = "IBAN",
            fontSize = Dimens.TextBody,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
            modifier = Modifier.padding(bottom = Dimens.PaddingSmall)
        )

        CustomInputField(
            value = state.iban,
            onValueChange = { onIntent(PayoutIntent.UpdateIban(it)) },
            placeholder = "FR1212345123451234567A1231013123123",
            keyboardType = KeyboardType.Ascii,
            modifier = Modifier
                .fillMaxWidth()
                .height(Dimens.InputHeight)
        )

        Text(
            text = "Enter the destination bank account IBAN",
            fontSize = Dimens.TextCaption,
            color = TextSecondary,
            modifier = Modifier.padding(top = Dimens.PaddingSmall)
        )

        Spacer(modifier = Modifier.height(Dimens.PaddingXXLarge))

        Button(
            onClick = { onIntent(PayoutIntent.ClickConfirmInput) },
            enabled = state.isConfirmEnabled,
            modifier = Modifier
                .fillMaxWidth()
                .height(Dimens.ButtonHeight),
            shape = RoundedCornerShape(Dimens.CornerRadiusNormal),
            colors = ButtonDefaults.buttonColors(
                containerColor = PrimaryBlue,
                disabledContainerColor = DisabledContainer,
                disabledContentColor = DisabledContent
            )
        ) {
            Text(
                text = "Confirm",
                fontSize = Dimens.TextTitleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun CustomInputField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    keyboardType: KeyboardType,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(SurfaceWhite, RoundedCornerShape(Dimens.CornerRadiusNormal))
            .border(Dimens.BorderWidth, BorderLight, RoundedCornerShape(Dimens.CornerRadiusNormal))
            .padding(horizontal = Dimens.PaddingNormal),
        contentAlignment = Alignment.CenterStart
    ) {
        if (value.isEmpty()) {
            Text(
                text = placeholder,
                color = TextPlaceholder,
                fontSize = Dimens.TextBody,
                maxLines = 1
            )
        }
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            maxLines = 1,
            textStyle = TextStyle(
                fontSize = Dimens.TextBody,
                color = TextPrimary,
                fontWeight = FontWeight.Normal
            ),
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun ConfirmPayoutModal(
    state: PayoutUiState.ConfirmDialog,
    onIntent: (PayoutIntent) -> Unit
) {
    AlertDialog(
        onDismissRequest = { onIntent(PayoutIntent.DismissConfirmDialog) },
        title = {
            Text(
                text = "Confirm Payout",
                fontWeight = FontWeight.Bold,
                fontSize = Dimens.TextHeader,
                modifier = Modifier.fillMaxWidth()
            )
        },
        text = {
            Column(modifier = Modifier.padding(vertical = Dimens.PaddingSmall)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Amount:", color = TextSecondary, fontSize = Dimens.TextBody)
                    Text("£${state.amount}.00", fontWeight = FontWeight.Bold, fontSize = Dimens.TextBody)
                }
                Spacer(modifier = Modifier.height(Dimens.PaddingMedium))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Currency:", color = TextSecondary, fontSize = Dimens.TextBody)
                    Text(state.currency.name, fontWeight = FontWeight.Bold, fontSize = Dimens.TextBody)
                }
                Spacer(modifier = Modifier.height(Dimens.PaddingMedium))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("IBAN:", color = TextSecondary, fontSize = Dimens.TextBody)
                    val maskedIban = if (state.iban.length > 8) {
                        "${state.iban.take(4)}********************${state.iban.takeLast(4)}"
                    } else state.iban
                    Text(maskedIban, fontWeight = FontWeight.Bold, fontSize = Dimens.TextCaption)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onIntent(PayoutIntent.ConfirmPayoutExecution) },
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                shape = RoundedCornerShape(Dimens.PaddingSmall)
            ) {
                Text("Confirm", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = { onIntent(PayoutIntent.DismissConfirmDialog) }) {
                Text("Cancel", color = TextSecondary, fontWeight = FontWeight.Bold)
            }
        },
        containerColor = SurfaceWhite,
        shape = RoundedCornerShape(Dimens.CornerRadiusDialog)
    )
}

@Composable
private fun PayoutSuccessScreen(onIntent: (PayoutIntent) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(Dimens.PaddingXLarge),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            tint = SuccessGreen,
            modifier = Modifier.size(Dimens.IconSizeLarge)
        )
        Spacer(modifier = Modifier.height(Dimens.PaddingLarge))
        Text(
            text = "Payout Completed",
            fontSize = Dimens.TextHeader,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        Spacer(modifier = Modifier.height(Dimens.PaddingSmall))
        Text(
            text = "Your payout of £400.00 has been\nprocessed successfully.",
            fontSize = Dimens.TextBody,
            color = TextSecondary,
            modifier = Modifier.padding(horizontal = Dimens.PaddingNormal)
        )
        Spacer(modifier = Modifier.height(Dimens.PaddingXXLarge))
        Button(
            onClick = { onIntent(PayoutIntent.Reset) },
            modifier = Modifier
                .fillMaxWidth()
                .height(Dimens.ButtonHeight),
            shape = RoundedCornerShape(Dimens.CornerRadiusNormal),
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
        ) {
            Text("Create Another Payout", fontSize = Dimens.TextBody, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun PayoutErrorScreen(message: String, onIntent: (PayoutIntent) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(Dimens.PaddingXLarge),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Close,
            contentDescription = null,
            tint = ErrorRed,
            modifier = Modifier.size(Dimens.IconSizeLarge)
        )
        Spacer(modifier = Modifier.height(Dimens.PaddingLarge))
        Text(
            text = "Unable to Process Payout",
            fontSize = Dimens.TextHeader,
            fontWeight = FontWeight.Bold,
            color = ErrorRed
        )
        Spacer(modifier = Modifier.height(Dimens.PaddingMedium))
        Text(
            text = message,
            fontSize = Dimens.TextBody,
            color = ErrorRed,
            modifier = Modifier.padding(horizontal = Dimens.PaddingNormal)
        )
        Spacer(modifier = Modifier.height(Dimens.PaddingXXLarge))
        Button(
            onClick = { onIntent(PayoutIntent.Reset) },
            modifier = Modifier
                .fillMaxWidth()
                .height(Dimens.ButtonHeight),
            shape = RoundedCornerShape(Dimens.CornerRadiusNormal),
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
        ) {
            Text("Try Again", fontSize = Dimens.TextBody, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun HandleBiometricPrompt(
    onSuccess: () -> Unit,
    onError: (String) -> Unit
) {
    val context = LocalContext.current
    val activity = context as? FragmentActivity

    LaunchedEffect(Unit) {
        if (activity != null) {
            val biometricManager = BiometricPromptManager(activity)
            when (biometricManager.canAuthenticate()) {
                is BiometricResult.NotEnrolled -> {
                    onError("Biometrics are not set up. Please set up biometrics in your device settings and try again.")
                }

                is BiometricResult.FeatureUnavailable -> {
                    onError("Biometric authentication is not available on this device.")
                }

                else -> {
                    biometricManager.showBiometricPrompt { result ->
                        when (result) {
                            BiometricResult.Success -> onSuccess()
                            is BiometricResult.Error -> onError(result.error)
                            else -> onError("Biometric authentication failed.")
                        }
                    }
                }
            }
        } else {
            onError("Unable to launch biometric prompt.")
        }
    }
}