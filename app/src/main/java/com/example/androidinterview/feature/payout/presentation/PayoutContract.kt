package com.example.androidinterview.feature.payout.presentation

import com.example.androidinterview.core.network.model.Currency

sealed interface PayoutUiState {
    data class InputForm(
        val amount: String = "",
        val currency: Currency = Currency.GBP,
        val availableCurrencies: List<Currency> = listOf(Currency.GBP, Currency.EUR),
        val iban: String = "",
        val isConfirmEnabled: Boolean = false
    ) : PayoutUiState

    data class ConfirmDialog(
        val amount: String,
        val currency: Currency,
        val iban: String
    ) : PayoutUiState

    data object BiometricPromptState : PayoutUiState
    data object Loading : PayoutUiState
    data object Success : PayoutUiState
    data class Error(val message: String) : PayoutUiState
}

sealed interface PayoutIntent {
    data class UpdateAmount(val amount: String) : PayoutIntent
    data class UpdateCurrency(val currency: Currency) : PayoutIntent
    data class UpdateIban(val iban: String) : PayoutIntent
    data object ClickConfirmInput : PayoutIntent
    data object DismissConfirmDialog : PayoutIntent
    data object ConfirmPayoutExecution : PayoutIntent
    data object BiometricSuccess : PayoutIntent
    data class BiometricFailed(val error: String) : PayoutIntent
    data object Reset : PayoutIntent
}