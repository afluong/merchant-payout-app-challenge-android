package com.example.androidinterview.feature.merchant.domain.repository

import androidx.paging.PagingData
import com.example.androidinterview.feature.merchant.domain.model.AccountBalance
import com.example.androidinterview.feature.merchant.domain.model.ActivityItem
import kotlinx.coroutines.flow.Flow

interface MerchantRepository {
    suspend fun getAccountBalance(): Result<AccountBalance>
    suspend fun getRecentActivities(limit: Int = 3): Result<List<ActivityItem>>
    fun getActivityPagingStream(): Flow<PagingData<ActivityItem>>
}