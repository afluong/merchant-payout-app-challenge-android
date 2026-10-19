package com.example.androidinterview.core.network.model

enum class Currency { GBP, EUR }
enum class ActivityStatusResponse { completed, pending, processing, failed }

data class ActivityItemResponse(
    val id: String,
    val type: String,
    val amount: Int,           // pence; negative for outflows
    val currency: Currency,
    val date: String,          // ISO 8601
    val description: String,
    val status: ActivityStatusResponse,
)