package com.example.androidinterview.feature.payout.data

import android.annotation.SuppressLint
import android.content.Context
import android.provider.Settings
import com.example.androidinterview.core.network.ApiService
import com.example.androidinterview.core.network.model.CreatePayoutRequest
import com.example.androidinterview.core.network.model.Currency
import com.example.androidinterview.feature.payout.domain.repository.PayoutRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PayoutRepositoryImpl @Inject constructor(
    private val apiService: ApiService,
    @ApplicationContext private val context: Context
) : PayoutRepository {

    @SuppressLint("HardwareIds")
    override suspend fun executePayout(
        amountInPence: Int,
        currency: Currency,
        iban: String
    ): Result<String> {
        return try {
            val deviceId =
                Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
                    ?: "unknown_device"

            val request = CreatePayoutRequest(
                amount = amountInPence,
                currency = currency,
                iban = iban,
                deviceId = deviceId
            )

            val response = apiService.createPayout(request)

            if (response.isSuccessful) {
                Result.success(response.body()?.id ?: "Payout success")
            } else {
                val errorBodyString = response.errorBody()?.string()
                Result.failure(Exception(parseErrorMessage(response.code(), errorBodyString)))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun parseErrorMessage(code: Int, errorBody: String?): String {
        return when {
            errorBody?.contains("insufficient", ignoreCase = true) == true -> "Insufficient funds."
            code == 400 -> "Invalid payout details. Please check your IBAN and amount."
            code >= 500 -> "Service temporarily unavailable. Please try again later."
            else -> "Unable to Process Payout"
        }
    }
}