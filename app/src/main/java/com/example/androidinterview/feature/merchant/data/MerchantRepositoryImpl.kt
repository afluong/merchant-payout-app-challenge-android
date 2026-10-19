package com.example.androidinterview.feature.merchant.data

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.example.androidinterview.core.network.ApiService
import com.example.androidinterview.feature.merchant.data.mapper.toDomain
import com.example.androidinterview.feature.merchant.domain.model.AccountBalance
import com.example.androidinterview.feature.merchant.domain.model.ActivityItem
import com.example.androidinterview.feature.merchant.domain.repository.MerchantRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class MerchantRepositoryImpl @Inject constructor(private val apiService: ApiService) :
    MerchantRepository {
    override suspend fun getAccountBalance(): Result<AccountBalance> {
        return try {
            val response = apiService.getMerchantData()
            val balance = AccountBalance(
                availableAmount = response.available_balance / 100.0,
                pendingAmount = response.pending_balance / 100.0,
                currencySymbol = getCurrencySymbol(response.currency.name)
            )
            Result.success(balance)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getRecentActivities(limit: Int): Result<List<ActivityItem>> {
        return try {
            val response = apiService.getActivities(cursor = null, limit = limit)
            val activities = response.items.map { it.toDomain() }
            Result.success(activities)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getActivityPagingStream(): Flow<PagingData<ActivityItem>> {
        return Pager(
            config = PagingConfig(pageSize = 15, enablePlaceholders = false),
            pagingSourceFactory = { ActivityPagingDataSource(apiService) }
        ).flow
    }

    private fun getCurrencySymbol(currencyStr: String): String {
        return when (currencyStr) {
            "GBP" -> "£"
            "EUR" -> "€"
            else -> currencyStr
        }
    }
}