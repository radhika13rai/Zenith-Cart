package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.ElectricScooter
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material.icons.outlined.LocalShipping
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material.icons.outlined.Store
import androidx.compose.material.icons.outlined.SupportAgent
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
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
import com.example.ui.screens.AnalyticsDashboardScreen
import com.example.ui.screens.CartScreen
import com.example.ui.screens.CheckoutScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LocationPickerScreen
import com.example.ui.screens.LoginOtpScreen
import com.example.ui.screens.NotificationsScreen
import com.example.ui.screens.OrdersScreen
import com.example.ui.screens.ProductDetailScreen
import com.example.ui.screens.RiderFleetScreen
import com.example.ui.screens.SearchScreen
import com.example.ui.screens.SupportScreen
import com.example.ui.screens.VendorHubScreen
import com.example.ui.screens.WishlistScreen
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

@Composable
fun OmniMainScreen(
    viewModel: EcommerceViewModel,
    initialMode: EcosystemApp = EcosystemApp.CUSTOMER,
    lockMode: Boolean = false
) {
    var activeAppMode by remember { mutableStateOf(initialMode) }
    val isLocationConfirmed by viewModel.isLocationConfirmed.collectAsState()
    val isUserLoggedIn by viewModel.isUserLoggedIn.collectAsState()

    var currentCustomerScreen by remember {
        mutableStateOf<Screen>(Screen.Home)
    }
    var currentPartnerScreen by remember { mutableStateOf<Screen>(Screen.Vendors) }
    val customerScreenStack = remember { mutableStateListOf<Screen>() }

    fun navigateToCustomer(screen: Screen) {
        customerScreenStack.add(currentCustomerScreen)
        currentCustomerScreen = screen
    }

    fun navigateBackCustomer() {
        if (customerScreenStack.isNotEmpty()) {
            currentCustomerScreen = customerScreenStack.removeAt(customerScreenStack.size - 1)
        } else {
            currentCustomerScreen = Screen.Home
        }
    }

    // Intercept back presses for customer sub-screens
    if (activeAppMode == EcosystemApp.CUSTOMER && currentCustomerScreen !is Screen.Home) {
        BackHandler { navigateBackCustomer() }
    }

    val cartItems by viewModel.cartItems.collectAsState()
    val totalCartUnits = cartItems.sumOf { it.quantity }

    val isCustomerTopLevel = currentCustomerScreen is Screen.Home ||
            currentCustomerScreen is Screen.Orders ||
            (currentCustomerScreen is Screen.Support && (currentCustomerScreen as Screen.Support).prefilledOrderId == null)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Ecosystem App Switcher Header Bar (Only visible when launched in multi-role debug mode)
        if (!lockMode) {
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

                    // 3 Dedicated Apps Pill Switcher
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
        }

        // Active Ecosystem App View
        BoxWithConstraints(modifier = Modifier.weight(1f).fillMaxWidth()) {
            val isWideScreen = maxWidth >= 600.dp

            when (activeAppMode) {
                EcosystemApp.CUSTOMER -> {
                    Scaffold(
                        bottomBar = {
                            if (isCustomerTopLevel && !isWideScreen) {
                                NavigationBar(
                                    modifier = Modifier
                                        .windowInsetsPadding(WindowInsets.navigationBars)
                                        .testTag("bottom_nav_bar"),
                                    containerColor = MaterialTheme.colorScheme.surface,
                                    tonalElevation = 6.dp
                                ) {
                                    NavigationBarItem(
                                        selected = currentCustomerScreen is Screen.Home,
                                        onClick = { currentCustomerScreen = Screen.Home },
                                        icon = {
                                            Icon(
                                                imageVector = if (currentCustomerScreen is Screen.Home) Icons.Filled.ShoppingBag else Icons.Outlined.ShoppingBag,
                                                contentDescription = "Shop"
                                            )
                                        },
                                        label = { Text("Shop", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                                        colors = NavigationBarItemDefaults.colors(
                                            selectedIconColor = CoralPrimary,
                                            selectedTextColor = CoralPrimary,
                                            indicatorColor = CoralPrimary.copy(alpha = 0.15f)
                                        ),
                                        modifier = Modifier.testTag("nav_item_shop")
                                    )

                                    NavigationBarItem(
                                        selected = currentCustomerScreen is Screen.Orders,
                                        onClick = { currentCustomerScreen = Screen.Orders },
                                        icon = {
                                            Icon(
                                                imageVector = if (currentCustomerScreen is Screen.Orders) Icons.Filled.LocalShipping else Icons.Outlined.LocalShipping,
                                                contentDescription = "Orders"
                                            )
                                        },
                                        label = { Text("3PL Track", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                                        colors = NavigationBarItemDefaults.colors(
                                            selectedIconColor = CoralPrimary,
                                            selectedTextColor = CoralPrimary,
                                            indicatorColor = CoralPrimary.copy(alpha = 0.15f)
                                        ),
                                        modifier = Modifier.testTag("nav_item_orders")
                                    )

                                    NavigationBarItem(
                                        selected = currentCustomerScreen is Screen.Support,
                                        onClick = { currentCustomerScreen = Screen.Support() },
                                        icon = {
                                            Icon(
                                                imageVector = if (currentCustomerScreen is Screen.Support) Icons.Filled.SupportAgent else Icons.Outlined.SupportAgent,
                                                contentDescription = "Support"
                                            )
                                        },
                                        label = { Text("Support", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                                        colors = NavigationBarItemDefaults.colors(
                                            selectedIconColor = CoralPrimary,
                                            selectedTextColor = CoralPrimary,
                                            indicatorColor = CoralPrimary.copy(alpha = 0.15f)
                                        ),
                                        modifier = Modifier.testTag("nav_item_support")
                                    )
                                }
                            }
                        }
                    ) { innerPadding ->
                        Row(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
                            if (isCustomerTopLevel && isWideScreen) {
                                NavigationRail(
                                    containerColor = MaterialTheme.colorScheme.surface,
                                    modifier = Modifier.testTag("side_navigation_rail")
                                ) {
                                    NavigationRailItem(
                                        selected = currentCustomerScreen is Screen.Home,
                                        onClick = { currentCustomerScreen = Screen.Home },
                                        icon = { Icon(Icons.Filled.ShoppingBag, contentDescription = "Shop") },
                                        label = { Text("Shop") }
                                    )
                                    NavigationRailItem(
                                        selected = currentCustomerScreen is Screen.Orders,
                                        onClick = { currentCustomerScreen = Screen.Orders },
                                        icon = { Icon(Icons.Filled.LocalShipping, contentDescription = "3PL Track") },
                                        label = { Text("Logistics") }
                                    )
                                    NavigationRailItem(
                                        selected = currentCustomerScreen is Screen.Support,
                                        onClick = { currentCustomerScreen = Screen.Support() },
                                        icon = { Icon(Icons.Filled.SupportAgent, contentDescription = "Support") },
                                        label = { Text("Support") }
                                    )
                                }
                            }

                            Box(modifier = Modifier.weight(1f).fillMaxSize()) {
                                when (val screen = currentCustomerScreen) {
                                    is Screen.Home -> {
                                        HomeScreen(
                                            viewModel = viewModel,
                                            onNavigateToProduct = { productId -> navigateToCustomer(Screen.ProductDetail(productId)) },
                                            onNavigateToCart = { navigateToCustomer(Screen.Cart) },
                                            onNavigateToNotifications = { navigateToCustomer(Screen.Notifications) },
                                            onNavigateToSearch = { navigateToCustomer(Screen.Search) },
                                            onNavigateToWishlist = { navigateToCustomer(Screen.Wishlist) },
                                            onNavigateToLocationPicker = { navigateToCustomer(Screen.LocationPicker) },
                                            onNavigateToLogin = { navigateToCustomer(Screen.LoginOtp) }
                                        )
                                    }
                                    is Screen.Orders -> {
                                        OrdersScreen(
                                            viewModel = viewModel,
                                            onNavigateToSupport = { orderId -> navigateToCustomer(Screen.Support(orderId)) },
                                            onNavigateToHome = { currentCustomerScreen = Screen.Home },
                                            onNavigateToCart = { navigateToCustomer(Screen.Cart) }
                                        )
                                    }
                                    is Screen.Support -> {
                                        SupportScreen(
                                            prefilledOrderId = screen.prefilledOrderId,
                                            viewModel = viewModel
                                        )
                                    }
                                    is Screen.ProductDetail -> {
                                        ProductDetailScreen(
                                            productId = screen.productId,
                                            viewModel = viewModel,
                                            onNavigateBack = { navigateBackCustomer() },
                                            onNavigateToCart = { navigateToCustomer(Screen.Cart) }
                                        )
                                    }
                                    is Screen.Cart -> {
                                        CartScreen(
                                            viewModel = viewModel,
                                            onNavigateBack = { navigateBackCustomer() },
                                            onNavigateToCheckout = { navigateToCustomer(Screen.Checkout) },
                                            onNavigateToHome = {
                                                customerScreenStack.clear()
                                                currentCustomerScreen = Screen.Home
                                            },
                                            onNavigateToWishlist = {
                                                navigateToCustomer(Screen.Wishlist)
                                            }
                                        )
                                    }
                                    is Screen.Checkout -> {
                                        CheckoutScreen(
                                            viewModel = viewModel,
                                            onNavigateBack = { navigateBackCustomer() },
                                            onOrderPlaced = { orderId ->
                                                customerScreenStack.clear()
                                                currentCustomerScreen = Screen.Orders
                                            },
                                            onNavigateToLocationPicker = {
                                                navigateToCustomer(Screen.LocationPicker)
                                            }
                                        )
                                    }
                                    is Screen.Search -> {
                                        SearchScreen(
                                            viewModel = viewModel,
                                            onNavigateBack = { navigateBackCustomer() },
                                            onNavigateToProduct = { productId -> navigateToCustomer(Screen.ProductDetail(productId)) }
                                        )
                                    }
                                    is Screen.Notifications -> {
                                        NotificationsScreen(
                                            viewModel = viewModel,
                                            onNavigateBack = { navigateBackCustomer() },
                                            onNavigateToOrders = {
                                                customerScreenStack.clear()
                                                currentCustomerScreen = Screen.Orders
                                            },
                                            onNavigateToCart = {
                                                navigateToCustomer(Screen.Cart)
                                            }
                                        )
                                    }
                                    is Screen.Wishlist -> {
                                        WishlistScreen(
                                            viewModel = viewModel,
                                            onNavigateBack = { navigateBackCustomer() },
                                            onNavigateToProduct = { productId -> navigateToCustomer(Screen.ProductDetail(productId)) },
                                            onNavigateToCart = { navigateToCustomer(Screen.Cart) },
                                            onNavigateToHome = {
                                                customerScreenStack.clear()
                                                currentCustomerScreen = Screen.Home
                                            }
                                        )
                                    }
                                    is Screen.LocationPicker -> {
                                        LocationPickerScreen(
                                            viewModel = viewModel,
                                            onLocationConfirmed = {
                                                if (!viewModel.isUserLoggedIn.value) {
                                                    currentCustomerScreen = Screen.LoginOtp
                                                } else {
                                                    customerScreenStack.clear()
                                                    currentCustomerScreen = Screen.Home
                                                }
                                            },
                                            onNavigateBack = {
                                                if (customerScreenStack.isNotEmpty()) {
                                                    navigateBackCustomer()
                                                } else {
                                                    currentCustomerScreen = Screen.Home
                                                }
                                            }
                                        )
                                    }
                                    is Screen.LoginOtp -> {
                                        LoginOtpScreen(
                                            viewModel = viewModel,
                                            onLoginSuccess = {
                                                customerScreenStack.clear()
                                                currentCustomerScreen = Screen.Home
                                            },
                                            onSkipOrBack = {
                                                customerScreenStack.clear()
                                                currentCustomerScreen = Screen.Home
                                            }
                                        )
                                    }
                                    else -> {}
                                }
                            }
                        }
                    }
                }

                EcosystemApp.PARTNER -> {
                    Scaffold(
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

                EcosystemApp.RIDER -> {
                    RiderFleetScreen(viewModel = viewModel)
                }
            }
        }
    }
}
