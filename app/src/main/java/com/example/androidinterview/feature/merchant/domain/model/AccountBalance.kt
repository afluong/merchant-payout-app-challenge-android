package com.example.androidinterview.feature.merchant.domain.model

data class AccountBalance(
    val availableAmount: Double,
    val pendingAmount: Double,
    val currencySymbol: String
)
