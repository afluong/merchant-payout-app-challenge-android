package com.example.androidinterview.feature.merchant.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.androidinterview.feature.merchant.domain.model.ActivityItem
import com.example.androidinterview.feature.merchant.domain.repository.MerchantRepository
import com.example.androidinterview.feature.merchant.domain.usecase.GetMerchantDataUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MerchantViewModel @Inject constructor(
    private val getMerchantDataUseCase: GetMerchantDataUseCase,
    repository: MerchantRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<MerchantUiState>(MerchantUiState.Loading)
    val uiState: StateFlow<MerchantUiState> = _uiState.asStateFlow()

    val activityPagingFlow: Flow<PagingData<ActivityItem>> =
        repository.getActivityPagingStream().cachedIn(viewModelScope)

    init {
        onIntent(MerchantIntent.LoadMerchantData)
    }

    fun onIntent(intent: MerchantIntent) {
        when (intent) {
            MerchantIntent.LoadMerchantData, MerchantIntent.Retry -> fetchMerchantData()
            MerchantIntent.OpenActivityModal -> setModalVisibility(true)
            MerchantIntent.CloseActivityModal -> setModalVisibility(false)
        }
    }

    private fun fetchMerchantData() {
        viewModelScope.launch {
            _uiState.value = MerchantUiState.Loading
            getMerchantDataUseCase()
                .onSuccess { data ->
                    _uiState.value = MerchantUiState.Success(
                        balance = data.balance,
                        recentActivities = data.recentActivities
                    )
                }
                .onFailure { error ->
                    _uiState.value = MerchantUiState.Error(
                        message = error.localizedMessage ?: "Failed to load dashboard"
                    )
                }
        }
    }

    private fun setModalVisibility(isVisible: Boolean) {
        val currentState = _uiState.value
        if (currentState is MerchantUiState.Success) {
            _uiState.value = currentState.copy(isActivityModalVisible = isVisible)
        }
    }
}