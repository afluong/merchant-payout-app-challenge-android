package com.example.androidinterview.core.network.model

data class AccountBalanceResponse(
    val availableAmount: Double,
    val pendingAmount: Double,
    val currencySymbol: String
)
