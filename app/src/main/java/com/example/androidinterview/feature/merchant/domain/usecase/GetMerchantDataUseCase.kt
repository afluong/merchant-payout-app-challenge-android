package com.example.androidinterview.feature.merchant.domain.usecase

import com.example.androidinterview.feature.merchant.domain.model.AccountBalance
import com.example.androidinterview.feature.merchant.domain.model.ActivityItem
import com.example.androidinterview.feature.merchant.domain.repository.MerchantRepository
import javax.inject.Inject

data class MerchantData(
    val balance: AccountBalance,
    val recentActivities: List<ActivityItem>
)

class GetMerchantDataUseCase @Inject constructor(
    private val repository: MerchantRepository
) {
    suspend operator fun invoke(): Result<MerchantData> {
        return try {
            val balanceRes = repository.getAccountBalance().getOrThrow()
            val activitiesRes = repository.getRecentActivities(limit = 3).getOrThrow()
            Result.success(MerchantData(balance = balanceRes, recentActivities = activitiesRes))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}