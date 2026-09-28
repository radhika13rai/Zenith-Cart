package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ElectricScooter
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.screens.RiderFleetScreen
import com.example.ui.theme.AmberSecondary
import com.example.ui.theme.CoralPrimary
import com.example.ui.theme.SlateNavyDark
import com.example.ui.viewmodel.EcommerceViewModel

enum class EcosystemApp(val label: String, val appTitle: String, val roleBadge: String, val icon: ImageVector) {
    CUSTOMER("Shopper", "OmniCart", "10-Min Flash & Mart", Icons.Filled.ShoppingBag),
    PARTNER("Partner", "OmniCart Partner", "Merchant & Darkstore Central", Icons.Filled.Store),
    RIDER("Rider", "OmniCart Rider", "Fleet Dispatch & Handover", Icons.Filled.ElectricScooter)
}

sealed class Screen {
    object Onboarding : Screen()
    object Home : Screen()
    object Orders : Screen()
    object Analytics : Screen()
    object Vendors : Screen()
    data class Support(val prefilledOrderId: String? = null) : Screen()
    data class ProductDetail(val productId: String) : Screen()
    object Cart : Screen()
    object Checkout : Screen()
    object Search : Screen()
    object Notifications : Screen()
    object Wishlist : Screen()
    object LocationPicker : Screen()
    object LoginOtp : Screen()
}

/**
 * Unified container composable that orchestrates the three OmniCart ecosystem applications.
 * Can be used in multi-mode testing or locked to a single role.
 */
@Composable
fun OmniMainScreen(
    viewModel: EcommerceViewModel,
    initialMode: EcosystemApp = EcosystemApp.CUSTOMER,
    lockMode: Boolean = false
) {
    if (lockMode) {
        when (initialMode) {
            EcosystemApp.CUSTOMER -> CustomerAppScreen(viewModel = viewModel)
            EcosystemApp.PARTNER -> PartnerAppScreen(viewModel = viewModel)
            EcosystemApp.RIDER -> RiderFleetScreen(viewModel = viewModel)
        }
        return
    }

    var activeAppMode by remember { mutableStateOf(initialMode) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Ecosystem App Switcher Header Bar (Debug / Unified Mode)
        Surface(
            color = SlateNavyDark,
            tonalElevation = 4.dp,
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.statusBars)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 7.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .background(CoralPrimary, RoundedCornerShape(6.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            activeAppMode.icon,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = activeAppMode.appTitle,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Text(
                            text = activeAppMode.roleBadge,
                            color = AmberSecondary,
                            fontSize = 9.sp
                        )
                    }
                }

                // 3 Dedicated Apps Switcher Pills
                Row(
                    modifier = Modifier
                        .background(Color.White.copy(alpha = 0.12f), RoundedCornerShape(20.dp))
                        .padding(2.dp)
                ) {
                    EcosystemApp.values().forEach { app ->
                        val isSelected = activeAppMode == app
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = if (isSelected) CoralPrimary else Color.Transparent,
                            modifier = Modifier
                                .clickable { activeAppMode = app }
                                .testTag("switch_app_${app.name.lowercase()}")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    app.icon,
                                    contentDescription = null,
                                    tint = if (isSelected) Color.White else Color(0xFFA6A9C0),
                                    modifier = Modifier.size(11.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = app.label,
                                    color = if (isSelected) Color.White else Color(0xFFA6A9C0),
                                    fontSize = 10.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
        }

        // Active Ecosystem App View
        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            when (activeAppMode) {
                EcosystemApp.CUSTOMER -> CustomerAppScreen(viewModel = viewModel)
                EcosystemApp.PARTNER -> PartnerAppScreen(viewModel = viewModel)
                EcosystemApp.RIDER -> RiderFleetScreen(viewModel = viewModel)
            }
        }
    }
}
