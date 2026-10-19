package com.example.androidinterview.core.network.model

data class MerchantDataResponse(
    val available_balance: Int,
    val pending_balance: Int,
    val currency: Currency,
    val activity: List<ActivityItemResponse>,
)