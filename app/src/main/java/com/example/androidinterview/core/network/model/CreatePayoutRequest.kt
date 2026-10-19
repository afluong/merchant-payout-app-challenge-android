package com.example.androidinterview.core.network.model

import com.google.gson.annotations.SerializedName

data class CreatePayoutRequest(
    val amount: Int,
    val currency: Currency,
    val iban: String,
    @SerializedName("device_id") val deviceId: String)