package com.example.androidinterview.core.network.mock

import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.UUID
import java.util.concurrent.atomic.AtomicInteger

internal object MockData {

    const val AVAILABLE_BALANCE = 500000
    const val PENDING_BALANCE = 25000
    const val CURRENCY = "GBP"

    private val deviceId = "device_${UUID.randomUUID()}"

    fun getDeviceId() = deviceId

    // --- Activity ---

    data class ActivityRecord(
        val id: String,
        val type: String,
        val amount: Int,
        val currency: String,
        val date: String,
        val description: String,
        val status: String,
    )

    val activities: List<ActivityRecord> = buildList {
        val now = Instant.now()
        val rows = listOf(
            Triple("payout",  -12500, "Invoice payment"),
            Triple("deposit",  75000, "Payment received"),
            Triple("fee",      -1500, "Monthly service fee"),
            Triple("payout",  -50000, "Supplier transfer"),
            Triple("deposit", 120000, "Client payment"),
            Triple("refund",   10000, "Refund from merchant"),
            Triple("payout",   -8750, "Card transaction"),
            Triple("deposit",  35000, "Direct debit receipt"),
            Triple("fee",      -2000, "Transaction fee"),
            Triple("payout",  -22500, "Standing order"),
            Triple("deposit",  60000, "Online purchase refund"),
            Triple("payout",   -5000, "Service charge"),
            Triple("deposit",  90000, "Wire transfer received"),
            Triple("fee",      -3500, "Annual fee"),
            Triple("payout",  -18000, "Contractor payment"),
            Triple("deposit",  45000, "Invoice settlement"),
            Triple("refund",   15000, "Overcharge refund"),
            Triple("payout",  -30000, "Equipment purchase"),
            Triple("deposit",  80000, "Partnership payment"),
            Triple("fee",      -1000, "Statement fee"),
            Triple("payout",  -42000, "Vendor payment"),
            Triple("deposit", 110000, "Project milestone"),
            Triple("refund",   25000, "Product return"),
            Triple("payout",  -16500, "Utility payment"),
            Triple("deposit",  55000, "Retainer payment"),
            Triple("fee",      -2500, "Wire transfer fee"),
            Triple("payout",  -70000, "Software licence"),
            Triple("deposit",  95000, "Consultancy fee"),
            Triple("payout",   -9000, "Office supplies"),
            Triple("deposit",  40000, "Royalty payment"),
        )
        rows.forEachIndexed { index, (type, amount, description) ->
            val i = index + 1
            val daysAgo = index / 3L
            add(
                ActivityRecord(
                    id = "act_${i.toString().padStart(3, '0')}",
                    type = type,
                    amount = amount,
                    currency = "GBP",
                    date = now.minus(daysAgo, ChronoUnit.DAYS).toString(),
                    description = description,
                    status = if (daysAgo > 1) "completed" else "processing",
                )
            )
        }
    }

    fun getPaginatedActivity(cursor: String?, limit: Int = 15): PaginatedResult {
        val startIndex = if (cursor == null) {
            0
        } else {
            val idx = activities.indexOfFirst { it.id == cursor }
            if (idx < 0) 0 else idx + 1
        }
        val page = activities.subList(startIndex, minOf(startIndex + limit, activities.size))
        val hasMore = startIndex + limit < activities.size
        return PaginatedResult(
            items = page,
            nextCursor = if (hasMore) page.last().id else null,
            hasMore = hasMore,
        )
    }

    data class PaginatedResult(
        val items: List<ActivityRecord>,
        val nextCursor: String?,
        val hasMore: Boolean,
    )

    // --- Payouts ---

    data class PayoutRecord(
        val id: String,
        val amount: Int,
        val currency: String,
        val iban: String,
        val createdAt: Long,
        var status: String = "pending",
    )

    private val payoutCounter = AtomicInteger(1)
    private val payouts = mutableMapOf<String, PayoutRecord>()

    fun createPayout(amount: Int, currency: String, iban: String): PayoutRecord {
        val id = "pay_${payoutCounter.getAndIncrement().toString().padStart(3, '0')}"
        val record = PayoutRecord(id, amount, currency, iban, System.currentTimeMillis())
        synchronized(payouts) { payouts[id] = record }
        return record
    }

    fun getPayoutById(id: String): PayoutRecord? {
        val record = synchronized(payouts) { payouts[id] } ?: return null
        val elapsedSeconds = (System.currentTimeMillis() - record.createdAt) / 1_000
        record.status = when {
            elapsedSeconds < 3 -> "pending"
            elapsedSeconds < 8 -> "processing"
            else -> "completed"
        }
        return record
    }
}
