package com.example.androidinterview.feature.merchant.data.mapper

import com.example.androidinterview.core.network.model.ActivityItemResponse
import com.example.androidinterview.core.network.model.ActivityStatusResponse
import com.example.androidinterview.core.network.model.Currency
import com.example.androidinterview.feature.merchant.domain.model.ActivityItem
import com.example.androidinterview.feature.merchant.domain.model.ActivityType
import java.time.Instant
import java.util.Date

fun ActivityItemResponse.toDomain(): ActivityItem {
    val symbol = when (currency) {
        Currency.GBP -> "£"
        Currency.EUR -> "€"
    }

    return ActivityItem(
        id = id,
        type = when (type) {
            "deposit" -> ActivityType.DEPOSIT
            "refund" -> ActivityType.REFUND
            "payout" -> ActivityType.PAYOUT
            "fee" -> ActivityType.FEE
            else -> ActivityType.FEE
        },
        title = description,
        description = description,
        amount = amount / 100.0,
        currencySymbol = symbol,
        date = runCatching { Date.from(Instant.parse(date)) }.getOrDefault(Date()),
        status = when (status) {
            ActivityStatusResponse.completed -> "completed"
            ActivityStatusResponse.pending -> "pending"
            ActivityStatusResponse.processing -> "processing"
            ActivityStatusResponse.failed -> "failed"
        }
    )
}