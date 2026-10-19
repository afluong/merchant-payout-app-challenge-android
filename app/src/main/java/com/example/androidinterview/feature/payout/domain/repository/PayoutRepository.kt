package com.example.androidinterview.feature.payout.domain.repository

import com.example.androidinterview.core.network.model.Currency

interface PayoutRepository {
    suspend fun executePayout(amountInPence: Int, currency: Currency, iban: String): Result<String>
}