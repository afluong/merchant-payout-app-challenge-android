package com.example.androidinterview.feature.merchant.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults.buttonColors
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.example.androidinterview.core.ui.theme.BackgroundLight
import com.example.androidinterview.core.ui.theme.BorderLight
import com.example.androidinterview.core.ui.theme.Dimens
import com.example.androidinterview.core.ui.theme.ErrorRed
import com.example.androidinterview.core.ui.theme.PrimaryBlue
import com.example.androidinterview.core.ui.theme.SuccessGreen
import com.example.androidinterview.core.ui.theme.TextPrimary
import com.example.androidinterview.core.ui.theme.TextSecondary
import com.example.androidinterview.feature.merchant.domain.model.AccountBalance
import com.example.androidinterview.feature.merchant.domain.model.ActivityItem
import java.util.Locale

@Composable
fun MerchantRoute(
    viewModel: MerchantViewModel = hiltViewModel()
) {
    val pagingItems = viewModel.activityPagingFlow.collectAsLazyPagingItems()
    val state by viewModel.uiState.collectAsState()

    MerchantRoute(
        uiState = state,
        pagingItems = pagingItems,
        onIntent = viewModel::onIntent
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MerchantRoute(
    uiState: MerchantUiState,
    pagingItems: LazyPagingItems<ActivityItem>,
    onIntent: (MerchantIntent) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Business Account",
                        fontSize = Dimens.TextScreenTitle,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BackgroundLight)
            )
        },
        containerColor = BackgroundLight
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (uiState) {
                is MerchantUiState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = PrimaryBlue
                    )
                }

                is MerchantUiState.Error -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = uiState.message, color = ErrorRed, fontSize = Dimens.TextBody)
                        Spacer(modifier = Modifier.height(Dimens.PaddingNormal))
                        Button(
                            onClick = { onIntent(MerchantIntent.Retry) },
                            colors = buttonColors(containerColor = PrimaryBlue)
                        ) {
                            Text("Retry", fontWeight = FontWeight.Bold)
                        }
                    }
                }

                is MerchantUiState.Success -> {
                    MerchantContent(
                        balance = uiState.balance,
                        recentActivities = uiState.recentActivities,
                        onShowMoreClick = { onIntent(MerchantIntent.OpenActivityModal) }
                    )

                    if (uiState.isActivityModalVisible) {
                        ActivityHistoryModal(
                            pagingItems = pagingItems,
                            onDismiss = { onIntent(MerchantIntent.CloseActivityModal) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MerchantContent(
    balance: AccountBalance,
    recentActivities: List<ActivityItem>,
    onShowMoreClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = Dimens.PaddingLarge, vertical = Dimens.PaddingMedium)
    ) {
        Text(
            text = "Account Balance",
            fontSize = Dimens.TextTitleLarge,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
            modifier = Modifier.padding(bottom = Dimens.PaddingNormal)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(text = "Available", color = TextSecondary, fontSize = Dimens.TextCaption)
                Text(
                    text = "${balance.currencySymbol}${
                        String.format(
                            Locale.US,
                            "%.2f",
                            balance.availableAmount
                        )
                    }",
                    fontSize = Dimens.TextHeader,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }
            Column {
                Text(text = "Pending", color = TextSecondary, fontSize = Dimens.TextCaption)
                Text(
                    text = "${balance.currencySymbol}${
                        String.format(
                            Locale.US,
                            "%.2f",
                            balance.pendingAmount
                        )
                    }",
                    fontSize = Dimens.TextHeader,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }
        }

        Spacer(modifier = Modifier.height(Dimens.PaddingXXLarge))

        Text(
            text = "Recent Activity",
            fontSize = Dimens.TextTitleLarge,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
            modifier = Modifier.padding(bottom = Dimens.PaddingNormal)
        )

        recentActivities.forEach { item ->
            RecentActivityRow(item = item)
            HorizontalDivider(color = BorderLight, thickness = Dimens.BorderWidth)
        }

        Spacer(modifier = Modifier.height(Dimens.PaddingXLarge))

        Button(
            onClick = onShowMoreClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(Dimens.ButtonHeight),
            colors = buttonColors(
                containerColor = PrimaryBlue.copy(alpha = 0.12f),
                contentColor = PrimaryBlue
            ),
            shape = RoundedCornerShape(Dimens.CornerRadiusNormal)
        ) {
            Text(text = "Show More", fontWeight = FontWeight.SemiBold, fontSize = Dimens.TextBody)
        }
    }
}

@Composable
private fun RecentActivityRow(item: ActivityItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = Dimens.PaddingNormal),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = item.description,
            fontSize = Dimens.TextBody,
            fontWeight = FontWeight.Medium,
            color = TextPrimary
        )
        val isPositive = item.amount >= 0
        val amountColor = if (isPositive) SuccessGreen else TextPrimary
        val prefix = if (isPositive) "" else "-"
        val formattedAmount = "${prefix}${item.currencySymbol}${
            String.format(
                Locale.US,
                "%.2f",
                kotlin.math.abs(item.amount)
            )
        }"

        Text(
            text = formattedAmount,
            fontSize = Dimens.TextBody,
            fontWeight = FontWeight.SemiBold,
            color = amountColor
        )
    }
}