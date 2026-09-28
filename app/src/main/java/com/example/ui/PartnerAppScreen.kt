package com.example.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material.icons.outlined.Store
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.screens.AnalyticsDashboardScreen
import com.example.ui.screens.VendorHubScreen
import com.example.ui.theme.CoralPrimary
import com.example.ui.viewmodel.EcommerceViewModel

/**
 * Dedicated, standalone UI root for the OmniCart Partner / Merchant App.
 * Handles 90-second inbound pick & pack queue, inventory stock editing,
 * darkstore pod assignment, and bank/UPI payouts.
 */
@Composable
fun PartnerAppScreen(
    viewModel: EcommerceViewModel,
    modifier: Modifier = Modifier
) {
    var currentPartnerScreen by remember { mutableStateOf<Screen>(Screen.Vendors) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar(
                modifier = Modifier
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .testTag("partner_bottom_nav"),
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp
            ) {
                NavigationBarItem(
                    selected = currentPartnerScreen is Screen.Vendors,
                    onClick = { currentPartnerScreen = Screen.Vendors },
                    icon = {
                        Icon(
                            imageVector = if (currentPartnerScreen is Screen.Vendors) Icons.Filled.Store else Icons.Outlined.Store,
                            contentDescription = "Vendors"
                        )
                    },
                    label = { Text("Darkstores & Stock", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = CoralPrimary,
                        selectedTextColor = CoralPrimary,
                        indicatorColor = CoralPrimary.copy(alpha = 0.15f)
                    ),
                    modifier = Modifier.testTag("partner_nav_vendors")
                )

                NavigationBarItem(
                    selected = currentPartnerScreen is Screen.Analytics,
                    onClick = { currentPartnerScreen = Screen.Analytics },
                    icon = {
                        Icon(
                            imageVector = if (currentPartnerScreen is Screen.Analytics) Icons.Filled.Analytics else Icons.Outlined.Analytics,
                            contentDescription = "Analytics"
                        )
                    },
                    label = { Text("Revenue & Taxes", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = CoralPrimary,
                        selectedTextColor = CoralPrimary,
                        indicatorColor = CoralPrimary.copy(alpha = 0.15f)
                    ),
                    modifier = Modifier.testTag("partner_nav_analytics")
                )
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            when (currentPartnerScreen) {
                is Screen.Vendors -> VendorHubScreen(viewModel = viewModel)
                is Screen.Analytics -> AnalyticsDashboardScreen(viewModel = viewModel)
                else -> VendorHubScreen(viewModel = viewModel)
            }
        }
    }
}
