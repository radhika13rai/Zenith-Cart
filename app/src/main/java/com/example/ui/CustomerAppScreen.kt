package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material.icons.outlined.LocalShipping
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material.icons.outlined.SupportAgent
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.screens.CartScreen
import com.example.ui.screens.CheckoutScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LocationPickerScreen
import com.example.ui.screens.LoginOtpScreen
import com.example.ui.screens.NotificationsScreen
import com.example.ui.screens.OrdersScreen
import com.example.ui.screens.ProductDetailScreen
import com.example.ui.screens.SearchScreen
import com.example.ui.screens.SupportScreen
import com.example.ui.screens.CustomerOnboardingScreen
import com.example.ui.screens.WishlistScreen
import com.example.ui.theme.CoralPrimary
import com.example.ui.viewmodel.EcommerceViewModel

/**
 * Dedicated, standalone UI root for the OmniCart Shopper / Customer App.
 * Handles instant grocery catalog browsing, cart & checkout, 10-minute order tracking,
 * location confirmation, wishlist, and customer support.
 */
@Composable
fun CustomerAppScreen(
    viewModel: EcommerceViewModel,
    modifier: Modifier = Modifier
) {
    val hasCompletedOnboarding by viewModel.hasCompletedOnboarding.collectAsState()
    var currentCustomerScreen by remember {
        mutableStateOf<Screen>(if (hasCompletedOnboarding) Screen.Home else Screen.Onboarding)
    }
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

    // Intercept hardware and gesture back presses for nested customer sub-screens
    if (currentCustomerScreen !is Screen.Home) {
        BackHandler { navigateBackCustomer() }
    }

    val isCustomerTopLevel = currentCustomerScreen is Screen.Home ||
            currentCustomerScreen is Screen.Orders ||
            (currentCustomerScreen is Screen.Support && (currentCustomerScreen as Screen.Support).prefilledOrderId == null)

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val isWideScreen = maxWidth >= 600.dp

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
                        is Screen.Onboarding -> {
                            CustomerOnboardingScreen(
                                viewModel = viewModel,
                                onComplete = {
                                    viewModel.completeOnboarding()
                                    customerScreenStack.clear()
                                    currentCustomerScreen = Screen.Home
                                },
                                onGetStarted = {
                                    viewModel.completeOnboarding()
                                    customerScreenStack.clear()
                                    currentCustomerScreen = Screen.Home
                                },
                                onNavigateToLocation = {
                                    viewModel.completeOnboarding()
                                    navigateToCustomer(Screen.LocationPicker)
                                },
                                onNavigateToLogin = {
                                    viewModel.completeOnboarding()
                                    navigateToCustomer(Screen.LoginOtp)
                                }
                            )
                        }
                        is Screen.Home -> {
                            HomeScreen(
                                viewModel = viewModel,
                                onNavigateToProduct = { productId -> navigateToCustomer(Screen.ProductDetail(productId)) },
                                onNavigateToCart = { navigateToCustomer(Screen.Cart) },
                                onNavigateToNotifications = { navigateToCustomer(Screen.Notifications) },
                                onNavigateToSearch = { navigateToCustomer(Screen.Search) },
                                onNavigateToWishlist = { navigateToCustomer(Screen.Wishlist) },
                                onNavigateToLocationPicker = { navigateToCustomer(Screen.LocationPicker) },
                                onNavigateToLogin = { navigateToCustomer(Screen.LoginOtp) },
                                onNavigateToOnboarding = { navigateToCustomer(Screen.Onboarding) }
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
                                onOrderPlaced = { _ ->
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
}
