package com.example.androidinterview.feature.merchant.domain.model

import java.util.Date

enum class ActivityType { DEPOSIT, REFUND, PAYOUT, FEE }

data class ActivityItem(
    val id: String,
    val type: ActivityType,
    val title: String,
    val description: String,
    val amount: Double,
    val currencySymbol: String,
    val date: Date,
    val status: String
)