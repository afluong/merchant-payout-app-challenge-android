package com.example.androidinterview.core.network.mock

import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.RecordedRequest
import org.json.JSONObject
import java.time.Instant

internal class MockDispatcher : Dispatcher() {

    override fun dispatch(request: RecordedRequest): MockResponse {
        simulateLatency()
        val path = request.path ?: return notFound()
        val cleanPath = path.substringBefore("?")
        val query = path.substringAfter("?", "")
        return when {
            request.method == "GET" && cleanPath == "/api/merchant" -> getMerchant()
            request.method == "GET" && cleanPath == "/api/merchant/activity" -> getActivity(query)
            request.method == "POST" && cleanPath == "/api/payouts" -> createPayout(request)
            request.method == "GET" && cleanPath.startsWith("/api/payouts/") -> {
                val id = cleanPath.removePrefix("/api/payouts/")
                getPayout(id)
            }
            request.method == "GET" && cleanPath == "/api/devices" -> getDevice()
            else -> notFound()
        }
    }

    private fun getMerchant(): MockResponse {
        val recentActivity = MockData.activities.take(3).joinToString(",") { it.toJson() }
        val body = """
            {
              "available_balance": ${MockData.AVAILABLE_BALANCE},
              "pending_balance": ${MockData.PENDING_BALANCE},
              "currency": "${MockData.CURRENCY}",
              "activity": [$recentActivity]
            }
        """.trimIndent()
        return jsonResponse(body)
    }

    private fun getActivity(query: String): MockResponse {
        val params = parseQuery(query)
        val cursor = params["cursor"]
        val limit = params["limit"]?.toIntOrNull() ?: 15
        val result = MockData.getPaginatedActivity(cursor, limit)
        val itemsJson = result.items.joinToString(",") { it.toJson() }
        val nextCursorJson = result.nextCursor?.let { "\"$it\"" } ?: "null"
        val body = """
            {
              "items": [$itemsJson],
              "next_cursor": $nextCursorJson,
              "has_more": ${result.hasMore}
            }
        """.trimIndent()
        return jsonResponse(body)
    }

    private fun createPayout(request: RecordedRequest): MockResponse {
        val body = request.body.readUtf8()
        val json = JSONObject(body)
        val amount = json.getInt("amount")
        return when (amount) {
            99999 -> MockResponse().setResponseCode(503)
                .setBody("""{"error":"Service unavailable","code":"SERVICE_UNAVAILABLE"}""")
                .addHeader("Content-Type", "application/json")
            88888 -> MockResponse().setResponseCode(400)
                .setBody("""{"error":"Insufficient funds","code":"INSUFFICIENT_FUNDS"}""")
                .addHeader("Content-Type", "application/json")
            else -> {
                val currency = json.getString("currency")
                val iban = json.getString("iban")
                val record = MockData.createPayout(amount, currency, iban)
                jsonResponse(record.toJson())
            }
        }
    }

    private fun getPayout(id: String): MockResponse {
        val record = MockData.getPayoutById(id) ?: return notFound()
        return jsonResponse(record.toJson())
    }

    private fun getDevice(): MockResponse {
        val body = """{"device_id":"${MockData.getDeviceId()}"}"""
        return jsonResponse(body)
    }

    // --- Helpers ---

    private fun simulateLatency() {
        Thread.sleep((500L..2000L).random())
    }

    private fun jsonResponse(body: String) = MockResponse()
        .setResponseCode(200)
        .setBody(body)
        .addHeader("Content-Type", "application/json")

    private fun notFound() = MockResponse().setResponseCode(404)

    private fun parseQuery(query: String): Map<String, String> {
        if (query.isBlank()) return emptyMap()
        return query.split("&").mapNotNull { param ->
            val parts = param.split("=", limit = 2)
            if (parts.size == 2) parts[0] to parts[1] else null
        }.toMap()
    }

    private fun MockData.ActivityRecord.toJson() = """
        {
          "id": "$id",
          "type": "$type",
          "amount": $amount,
          "currency": "$currency",
          "date": "$date",
          "description": "$description",
          "status": "$status"
        }
    """.trimIndent()

    private fun MockData.PayoutRecord.toJson() = """
        {
          "id": "$id",
          "status": "$status",
          "amount": $amount,
          "currency": "$currency",
          "iban": "$iban",
          "created_at": "${Instant.ofEpochMilli(createdAt)}"
        }
    """.trimIndent()
}
