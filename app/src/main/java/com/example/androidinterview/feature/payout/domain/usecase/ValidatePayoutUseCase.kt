package com.example.androidinterview.feature.payout.domain.usecase

import javax.inject.Inject

class ValidatePayoutUseCase @Inject constructor() {
    fun isAmountValid(amountStr: String): Boolean {
        val amount = amountStr.toDoubleOrNull() ?: return false
        return amount > 0.0
    }

    fun isIbanValid(iban: String): Boolean {
        val cleaned = iban.replace(" ", "")
        return cleaned.length >= 15 && cleaned.take(2).all { it.isUpperCase() }
    }

    fun requiresBiometric(amountStr: String): Boolean {
        val amount = amountStr.toDoubleOrNull() ?: 0.0
        return amount > 1000.0
    }
}