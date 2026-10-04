package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddLocationAlt
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeliveryDining
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Work
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.widget.Toast
import com.example.model.Currency
import com.example.model.IndianAddress
import com.example.model.IndianDeliveryPartner
import com.example.ui.components.CurrencySelectorModal
import com.example.ui.components.ProductCard
import com.example.ui.theme.AmberSecondary
import com.example.ui.theme.CoralPrimary
import com.example.ui.theme.GreenSuccess
import com.example.ui.theme.RedDanger
import com.example.ui.theme.SlateNavyDark
import com.example.ui.theme.SlateNavyDeep
import com.example.ui.viewmodel.EcommerceViewModel

@Composable
fun HomeScreen(
    viewModel: EcommerceViewModel,
    onNavigateToProduct: (String) -> Unit,
    onNavigateToCart: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    onNavigateToSearch: () -> Unit,
    onNavigateToWishlist: () -> Unit,
    onNavigateToLocationPicker: () -> Unit = {},
    onNavigateToLogin: (() -> Unit)? = null,
    onNavigateToOnboarding: (() -> Unit)? = null
) {
    val currency by viewModel.selectedCurrency.collectAsState()
    val products by viewModel.filteredProducts.collectAsState()
    val recommended by viewModel.recommendedProducts.collectAsState()
    val flashDeals by viewModel.flashDeals.collectAsState()
    val cartItems by viewModel.cartItems.collectAsState()
    val wishlistIds by viewModel.wishlistIds.collectAsState()
    val notifications by viewModel.notifications.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val selectedAddress by viewModel.selectedAddress.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()
    val userAddresses by viewModel.userAddresses.collectAsState()
    val indianPartners = viewModel.indianDeliveryPartners
    val context = LocalContext.current

    var showLocationModal by remember { mutableStateOf(false) }
    var showAddAddressModal by remember { mutableStateOf(false) }
    var showProfileModal by remember { mutableStateOf(false) }
    var showCurrencyModal by remember { mutableStateOf(false) }

    val categories = listOf("All", "Produce", "Dairy", "Snacks", "Pantry", "Household", "Electronics", "Fashion")
    val totalCartUnits = cartItems.sumOf { it.quantity }
    val cartSubtotal = cartItems.sumOf { it.product.priceUsd * it.quantity }

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(bottom = if (totalCartUnits > 0) 90.dp else 24.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            // Header: Location, Currency Selector & Notifications
            item(span = { GridItemSpan(2) }) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Location & Speed (Tapping navigates to interactive delivery map)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clickable { onNavigateToLocationPicker() }
                                .testTag("home_location_selector")
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .background(CoralPrimary.copy(alpha = 0.12f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Bolt,
                                    contentDescription = "Express Delivery",
                                    tint = CoralPrimary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "⚡ Delivery in 10–12 min",
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 13.sp,
                                        color = CoralPrimary
                                    )
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Icon(
                                        imageVector = Icons.Default.KeyboardArrowDown,
                                        contentDescription = "Select City/Pod",
                                        tint = CoralPrimary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                val isLocationConfirmed by viewModel.isLocationConfirmed.collectAsState()
                                Text(
                                    text = if (isLocationConfirmed && selectedAddress.flatAndBuilding.isNotBlank()) {
                                        "${selectedAddress.flatAndBuilding}, ${selectedAddress.streetAndLocality} • ${selectedAddress.label}"
                                    } else if (isLocationConfirmed) {
                                        "${selectedAddress.streetAndLocality}, ${selectedAddress.city} • ${selectedAddress.label}"
                                    } else {
                                        "Tap to set delivery location on map"
                                    },
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        // Right actions: User Profile/Login, Currency switcher & notifications
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            val isUserLoggedIn by viewModel.isUserLoggedIn.collectAsState()
                            val userProfile by viewModel.userProfile.collectAsState()

                            // User Login / Profile
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isUserLoggedIn) GreenSuccess.copy(alpha = 0.12f) else CoralPrimary.copy(alpha = 0.12f),
                                modifier = Modifier
                                    .clickable {
                                        if (isUserLoggedIn) {
                                            showProfileModal = true
                                        } else {
                                            onNavigateToLogin?.invoke()
                                        }
                                    }
                                    .testTag("user_profile_login_btn")
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isUserLoggedIn) Icons.Default.CheckCircle else Icons.Default.Person,
                                        contentDescription = null,
                                        tint = if (isUserLoggedIn) GreenSuccess else CoralPrimary,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (isUserLoggedIn && userProfile.name.isNotBlank()) userProfile.name.split(" ").first() else if (isUserLoggedIn) "Account" else "Register / Login",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = if (isUserLoggedIn) SlateNavyDark else CoralPrimary
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(6.dp))

                            // Currency Button
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier
                                    .clickable { showCurrencyModal = true }
                                    .testTag("currency_switch_btn")
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                                ) {
                                    Text(
                                        text = currency.flag,
                                        fontSize = 13.sp
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "${currency.code} (${currency.symbol})",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(6.dp))

                            // Wishlist Button with local Room counter
                            IconButton(
                                onClick = onNavigateToWishlist,
                                modifier = Modifier.testTag("home_wishlist_btn")
                            ) {
                                BadgedBox(
                                    badge = {
                                        if (wishlistIds.isNotEmpty()) {
                                            Badge(
                                                containerColor = CoralPrimary,
                                                contentColor = Color.White
                                            ) {
                                                Text(text = "${wishlistIds.size}", fontSize = 9.sp)
                                            }
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = if (wishlistIds.isNotEmpty()) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                        contentDescription = "Wishlist",
                                        tint = if (wishlistIds.isNotEmpty()) RedDanger else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(4.dp))

                            // Notifications Bell
                            IconButton(
                                onClick = onNavigateToNotifications,
                                modifier = Modifier.testTag("notifications_bell_btn")
                            ) {
                                BadgedBox(
                                    badge = {
                                        val unreadCount = notifications.count { !it.isRead }
                                        if (unreadCount > 0) {
                                            Badge(
                                                containerColor = CoralPrimary,
                                                contentColor = Color.White
                                            ) {
                                                Text(text = "$unreadCount", fontSize = 9.sp)
                                            }
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Notifications,
                                        contentDescription = "Notifications",
                                        tint = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }

                            // Shopper Profile / Registration Icon Button
                            IconButton(
                                onClick = { showProfileModal = true },
                                modifier = Modifier.testTag("home_profile_btn")
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .background(CoralPrimary.copy(alpha = 0.15f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = "Shopper Profile & Registration",
                                        tint = CoralPrimary,
                                        modifier = Modifier.size(19.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Instant Search Bar Clickable
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(onClick = onNavigateToSearch)
                            .testTag("home_search_bar")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 11.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Search headphones, sneakers, coffee...",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 13.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Indian 10-Min Quick-Commerce Partner Strip
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Storefront,
                                contentDescription = null,
                                tint = CoralPrimary,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Darkstore: ${selectedAddress.nearestDarkstorePod}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = SlateNavyDark
                            )
                        }
                        Text(
                            text = "${selectedAddress.podDistanceKm} km away",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = CoralPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(indianPartners) { partner ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (partner.type.contains("10-MIN")) CoralPrimary.copy(alpha = 0.10f) else MaterialTheme.colorScheme.surfaceVariant,
                                border = if (partner.type.contains("10-MIN")) androidx.compose.foundation.BorderStroke(1.dp, CoralPrimary.copy(alpha = 0.35f)) else null
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                ) {
                                    Icon(
                                        imageVector = if (partner.type.contains("10-MIN")) Icons.Default.DeliveryDining else Icons.Default.LocalShipping,
                                        contentDescription = null,
                                        tint = if (partner.type.contains("10-MIN")) CoralPrimary else SlateNavyDark,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(5.dp))
                                    Text(
                                        text = partner.name,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (partner.type.contains("10-MIN")) CoralPrimary else MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.width(5.dp))
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (partner.type.contains("10-MIN")) CoralPrimary else SlateNavyDark
                                    ) {
                                        Text(
                                            text = partner.averageEta,
                                            fontSize = 9.sp,
                                            color = Color.White,
                                            fontWeight = FontWeight.ExtraBold,
                                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Promotional Hero Banner
            item(span = { GridItemSpan(2) }) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(SlateNavyDark, SlateNavyDeep)
                            )
                        )
                        .padding(18.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = AmberSecondary
                            ) {
                                Text(
                                    text = "GLOBAL FLASH DEALS",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SlateNavyDark,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Timer,
                                    contentDescription = "Timer",
                                    tint = CoralPrimary,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = "Ends in 45m : 12s",
                                    color = CoralPrimary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Up to 30% Off Tech &\nArtisanal Essentials",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            lineHeight = 22.sp
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "Multi-vendor logistics with verified 3PL tracking",
                            color = Color(0xFFC4C7D7),
                            fontSize = 12.sp
                        )
                    }
                }
            }

            // Category Chips Row
            item(span = { GridItemSpan(2) }) {
                Column(modifier = Modifier.padding(vertical = 4.dp)) {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(categories) { cat ->
                            val isSelected = cat == selectedCategory
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) CoralPrimary else MaterialTheme.colorScheme.surface,
                                border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                                modifier = Modifier
                                    .clickable { viewModel.selectedCategory.value = cat }
                                    .testTag("category_chip_$cat")
                            ) {
                                Text(
                                    text = cat,
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Flash Deals Horizontal Section
            if (flashDeals.isNotEmpty()) {
                item(span = { GridItemSpan(2) }) {
                    Column(modifier = Modifier.padding(top = 16.dp)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "⚡ Flash Deals",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = CoralPrimary.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = "HOT",
                                        color = CoralPrimary,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(flashDeals) { product ->
                                val inCart = cartItems.find { it.product.id == product.id }?.quantity ?: 0
                                val isFav = wishlistIds.contains(product.id)
                                ProductCard(
                                    product = product,
                                    currency = currency,
                                    inCartQuantity = inCart,
                                    isWishlisted = isFav,
                                    onProductClick = { onNavigateToProduct(product.id) },
                                    onAddToCart = { viewModel.addToCart(product.id) },
                                    onRemoveFromCart = { viewModel.updateCartQuantity(product.id, inCart - 1) },
                                    onToggleWishlist = { viewModel.toggleWishlist(product.id) },
                                    modifier = Modifier.width(165.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Personalized Recommendations Section
            if (recommended.isNotEmpty()) {
                item(span = { GridItemSpan(2) }) {
                    Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 20.dp, bottom = 8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "🎯 Recommended For You",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Text(
                            text = "Based on browsing affinities & top customer reviews",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Product Grid items
            items(products) { product ->
                val inCart = cartItems.find { it.product.id == product.id }?.quantity ?: 0
                val isFav = wishlistIds.contains(product.id)
                ProductCard(
                    product = product,
                    currency = currency,
                    inCartQuantity = inCart,
                    isWishlisted = isFav,
                    onProductClick = { onNavigateToProduct(product.id) },
                    onAddToCart = { viewModel.addToCart(product.id) },
                    onRemoveFromCart = { viewModel.updateCartQuantity(product.id, inCart - 1) },
                    onToggleWishlist = { viewModel.toggleWishlist(product.id) },
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 6.dp)
                )
            }
        }

        // Floating Sticky Blinkit-Style Cart Bar
        AnimatedVisibility(
            visible = totalCartUnits > 0,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = CoralPrimary,
                shadowElevation = 8.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onNavigateToCart)
                    .testTag("floating_cart_bar")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "$totalCartUnits ITEMS • ${currency.format(cartSubtotal)}",
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "Extra discounts & express delivery ready",
                            color = Color.White.copy(alpha = 0.85f),
                            fontSize = 11.sp
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "View Cart",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowRight,
                            contentDescription = "Proceed",
                            tint = Color.White
                        )
                    }
                }
            }
        }

        // Currency Modal Sheet
        if (showCurrencyModal) {
            CurrencySelectorModal(
                selectedCurrency = currency,
                onCurrencySelected = { viewModel.setCurrency(it) },
                onDismiss = { showCurrencyModal = false }
            )
        }

        // Indian Delivery Address & Darkstore Pod Modal
        if (showLocationModal) {
            AlertDialog(
                onDismissRequest = { showLocationModal = false },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Bolt, contentDescription = null, tint = CoralPrimary)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Select 10-Min Delivery Pod", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "Deliveries dispatched instantly from your nearest local micro-darkstore pod:",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        // Button: Add New Delivery Address
                        Button(
                            onClick = {
                                showLocationModal = false
                                showAddAddressModal = true
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = CoralPrimary),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth().testTag("add_new_address_btn")
                        ) {
                            Icon(Icons.Default.AddLocationAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("+ Add New Delivery Address", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        userAddresses.forEach { addr ->
                            val isSelected = addr.id == selectedAddress.id
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) CoralPrimary.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant,
                                border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, CoralPrimary) else null,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        viewModel.selectAddress(addr)
                                        showLocationModal = false
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = addr.label,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp,
                                                color = if (isSelected) CoralPrimary else MaterialTheme.colorScheme.onSurface
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Surface(
                                                shape = RoundedCornerShape(4.dp),
                                                color = AmberSecondary.copy(alpha = 0.25f)
                                            ) {
                                                Text(
                                                    text = "${addr.podDistanceKm} km",
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = addr.fullFormattedAddress,
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "⚡ Pod: ${addr.nearestDarkstorePod}",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = CoralPrimary
                                        )
                                    }
                                    if (isSelected) {
                                        Icon(Icons.Default.Check, contentDescription = "Selected", tint = CoralPrimary, modifier = Modifier.size(18.dp))
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showLocationModal = false }) {
                        Text("Done", fontWeight = FontWeight.Bold, color = CoralPrimary)
                    }
                }
            )
        }

        // Modal: Add New Address Dialog
        if (showAddAddressModal) {
            var selectedLabel by remember { mutableStateOf("Home") }
            var flatInput by remember { mutableStateOf("") }
            var streetInput by remember { mutableStateOf("") }
            var landmarkInput by remember { mutableStateOf("") }
            var cityInput by remember { mutableStateOf("Bengaluru") }
            var pincodeInput by remember { mutableStateOf("560034") }

            AlertDialog(
                onDismissRequest = { showAddAddressModal = false },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AddLocationAlt, contentDescription = null, tint = CoralPrimary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Add Delivery Address", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Save your address for 10-minute instant doorstep delivery:",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        // Label Chips (Home, Work, Other)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("Home", "Work", "Other").forEach { tag ->
                                val isTagSelected = selectedLabel == tag
                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = if (isTagSelected) CoralPrimary else MaterialTheme.colorScheme.surfaceVariant,
                                    modifier = Modifier.clickable { selectedLabel = tag }
                                ) {
                                    Text(
                                        text = tag,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isTagSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                                    )
                                }
                            }
                        }

                        OutlinedTextField(
                            value = flatInput,
                            onValueChange = { flatInput = it },
                            label = { Text("Flat / House No. / Building", fontSize = 11.sp) },
                            placeholder = { Text("e.g. Flat 302, Palm Heights", fontSize = 11.sp) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("add_addr_flat")
                        )

                        OutlinedTextField(
                            value = streetInput,
                            onValueChange = { streetInput = it },
                            label = { Text("Area / Street / Locality", fontSize = 11.sp) },
                            placeholder = { Text("e.g. 5th Block, Koramangala", fontSize = 11.sp) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("add_addr_street")
                        )

                        OutlinedTextField(
                            value = landmarkInput,
                            onValueChange = { landmarkInput = it },
                            label = { Text("Landmark (Optional)", fontSize = 11.sp) },
                            placeholder = { Text("e.g. Near Sony Signal", fontSize = 11.sp) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = cityInput,
                                onValueChange = { cityInput = it },
                                label = { Text("City", fontSize = 11.sp) },
                                singleLine = true,
                                modifier = Modifier.weight(1f).testTag("add_addr_city")
                            )

                            OutlinedTextField(
                                value = pincodeInput,
                                onValueChange = { if (it.length <= 6) pincodeInput = it },
                                label = { Text("Pincode", fontSize = 11.sp) },
                                singleLine = true,
                                modifier = Modifier.weight(1f).testTag("add_addr_pincode")
                            )
                        }

                        // Smart Pod Snapping Banner
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = CoralPrimary.copy(alpha = 0.10f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Bolt, contentDescription = null, tint = CoralPrimary, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Auto-snapped to nearest micro-darkstore: ~0.5 km (⚡ 9-11 Mins ETA)",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = CoralPrimary
                                )
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (flatInput.isBlank() || streetInput.isBlank()) {
                                Toast.makeText(context, "Please enter your flat and street details", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            viewModel.addNewAddress(
                                label = selectedLabel,
                                flatAndBuilding = flatInput,
                                streetAndLocality = streetInput,
                                landmark = landmarkInput.ifBlank { "Near Main Gate" },
                                city = cityInput.ifBlank { "Bengaluru" },
                                state = if (cityInput.contains("Mumbai", ignoreCase = true)) "Maharashtra" else "Karnataka",
                                pincode = pincodeInput.ifBlank { "560034" }
                            )
                            Toast.makeText(context, "Address saved & selected for 10-Min Delivery!", Toast.LENGTH_SHORT).show()
                            showAddAddressModal = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CoralPrimary),
                        modifier = Modifier.testTag("save_new_address_btn")
                    ) {
                        Text("Save & Deliver Here")
                    }
                },
                dismissButton = {
                    OutlinedButton(onClick = { showAddAddressModal = false }) {
                        Text("Cancel")
                    }
                }
            )
        }

        // Modal: Shopper Registration & Profile Dialog
        if (showProfileModal) {
            com.example.ui.components.CustomerProfileModal(
                userProfile = userProfile,
                onDismiss = { showProfileModal = false },
                onRegisterNewAccount = {
                    showProfileModal = false
                    viewModel.logoutCustomer()
                    onNavigateToLogin?.invoke()
                },
                onLogout = {
                    showProfileModal = false
                    viewModel.logoutCustomer()
                    Toast.makeText(context, "Logged out successfully", Toast.LENGTH_SHORT).show()
                },
                onViewOnboarding = onNavigateToOnboarding
            )
        }
    }
}
