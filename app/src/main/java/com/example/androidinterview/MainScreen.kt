package com.example.androidinterview

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowCircleUp
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.androidinterview.feature.merchant.presentation.MerchantRoute
import com.example.androidinterview.feature.payout.presentation.PayoutRoute

sealed class Screen(route: String, title: String, val icon: ImageVector) {
    object Home : Screen("home", "Home", Icons.Default.Home)
    object Payouts : Screen("payouts", "Payouts", Icons.Default.ArrowCircleUp)
}

@Composable
fun MainScreen() {
    var selectedScreen by remember { mutableStateOf<Screen>(Screen.Home) }

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                contentColor = Color(0xFF0284C7)
            ) {
                NavigationBarItem(
                    icon = { Icon(Screen.Home.icon, contentDescription = null) },
                    label = { Text("Home") },
                    selected = selectedScreen == Screen.Home,
                    onClick = { selectedScreen = Screen.Home }
                )
                NavigationBarItem(
                    icon = { Icon(Screen.Payouts.icon, contentDescription = null) },
                    label = { Text("Payouts") },
                    selected = selectedScreen == Screen.Payouts,
                    onClick = { selectedScreen = Screen.Payouts }
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (selectedScreen) {
                is Screen.Home -> MerchantRoute()
                is Screen.Payouts -> PayoutRoute()
            }
        }
    }
}