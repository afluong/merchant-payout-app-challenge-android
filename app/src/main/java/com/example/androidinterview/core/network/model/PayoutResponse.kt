package com.example.androidinterview.core.network.model

enum class PayoutStatus { pending, processing, completed, failed }

data class PayoutResponse(
    val id: String,
    val status: PayoutStatus,
    val amount: Int,
    val currency: Currency,
    val iban: String,
    val created_at: String,
)