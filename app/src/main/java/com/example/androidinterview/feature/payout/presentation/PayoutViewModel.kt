package com.example.androidinterview.feature.payout.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.androidinterview.core.network.model.Currency
import com.example.androidinterview.feature.payout.domain.repository.PayoutRepository
import com.example.androidinterview.feature.payout.domain.usecase.ValidatePayoutUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PayoutViewModel @Inject constructor(
    private val payoutRepository: PayoutRepository,
    private val validatePayoutUseCase: ValidatePayoutUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<PayoutUiState>(PayoutUiState.InputForm())
    val uiState: StateFlow<PayoutUiState> = _uiState.asStateFlow()

    fun onIntent(intent: PayoutIntent) {
        when (intent) {
            is PayoutIntent.UpdateAmount -> updateForm { it.copy(amount = intent.amount) }
            is PayoutIntent.UpdateCurrency -> updateForm { it.copy(currency = intent.currency) }
            is PayoutIntent.UpdateIban -> updateForm { it.copy(iban = intent.iban) }
            PayoutIntent.ClickConfirmInput -> handleConfirmInput()
            PayoutIntent.DismissConfirmDialog -> restoreInputForm()
            PayoutIntent.ConfirmPayoutExecution -> processPayoutOrBiometric()
            PayoutIntent.BiometricSuccess -> executeApiPayout()
            is PayoutIntent.BiometricFailed -> _uiState.value = PayoutUiState.Error(intent.error)
            PayoutIntent.Reset -> _uiState.value = PayoutUiState.InputForm()
        }
    }

    private fun updateForm(update: (PayoutUiState.InputForm) -> PayoutUiState.InputForm) {
        val current = _uiState.value
        if (current is PayoutUiState.InputForm) {
            val updated = update(current)
            val isValid = validatePayoutUseCase.isAmountValid(updated.amount) &&
                    validatePayoutUseCase.isIbanValid(updated.iban)
            _uiState.value = updated.copy(isConfirmEnabled = isValid)
        }
    }

    private fun handleConfirmInput() {
        val current = _uiState.value
        if (current is PayoutUiState.InputForm) {
            _uiState.value = PayoutUiState.ConfirmDialog(
                amount = current.amount,
                currency = current.currency,
                iban = current.iban
            )
        }
    }

    private fun restoreInputForm() {
        _uiState.value = PayoutUiState.InputForm()
    }

    private fun processPayoutOrBiometric() {
        val current = _uiState.value
        if (current is PayoutUiState.ConfirmDialog) {
            if (validatePayoutUseCase.requiresBiometric(current.amount)) {
                _uiState.value = PayoutUiState.BiometricPromptState
            } else {
                executeApiPayout()
            }
        }
    }

    private fun executeApiPayout() {
        val current = _uiState.value
        val (amountStr, currency, iban) = when (current) {
            is PayoutUiState.ConfirmDialog -> Triple(current.amount, current.currency, current.iban)
            is PayoutUiState.BiometricPromptState -> Triple("1200", Currency.GBP, "")
            else -> return
        }

        viewModelScope.launch {
            _uiState.value = PayoutUiState.Loading
            val amountInPence = ((amountStr.toDoubleOrNull() ?: 0.0) * 100).toInt()

            payoutRepository.executePayout(amountInPence, currency, iban)
                .onSuccess {
                    _uiState.value = PayoutUiState.Success
                }
                .onFailure { error ->
                    _uiState.value = PayoutUiState.Error(error.localizedMessage ?: "Unable to Process Payout")
                }
        }
    }
}