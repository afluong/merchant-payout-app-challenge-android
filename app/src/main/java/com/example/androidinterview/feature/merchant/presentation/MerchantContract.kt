package com.example.androidinterview.feature.merchant.presentation

import com.example.androidinterview.feature.merchant.domain.model.AccountBalance
import com.example.androidinterview.feature.merchant.domain.model.ActivityItem

// State
sealed interface MerchantUiState {
    data object Loading : MerchantUiState
    data class Success(
        val balance: AccountBalance,
        val recentActivities: List<ActivityItem>,
        val isActivityModalVisible: Boolean = false
    ) : MerchantUiState
    data class Error(val message: String) : MerchantUiState
}

// Intent (User Actions)
sealed interface MerchantIntent {
    data object LoadMerchantData : MerchantIntent
    data object OpenActivityModal : MerchantIntent
    data object CloseActivityModal : MerchantIntent
    data object Retry : MerchantIntent
}