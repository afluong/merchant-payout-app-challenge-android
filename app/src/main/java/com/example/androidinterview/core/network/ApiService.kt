package com.example.androidinterview.core.network

import com.example.androidinterview.core.network.model.CreatePayoutRequest
import com.example.androidinterview.core.network.model.MerchantDataResponse
import com.example.androidinterview.core.network.model.PaginatedActivityResponse
import com.example.androidinterview.core.network.model.PayoutResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {

    @GET("api/merchant")
    suspend fun getMerchantData(): MerchantDataResponse

    @GET("api/merchant/activity")
    suspend fun getActivities(
        @Query("cursor") cursor: String?,
        @Query("limit") limit: Int = 15
    ): PaginatedActivityResponse

    @POST("api/payouts")
    suspend fun createPayout(
        @Body request: CreatePayoutRequest
    ): Response<PayoutResponse>

    @GET("api/payouts/{id}")
    suspend fun getPayoutStatus(
        @Path("id") payoutId: String
    ): PayoutResponse

    @GET("api/devices")
    suspend fun getDevice(): Map<String, String>
}