package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.OmniCartDatabase
import com.example.data.repository.EcommerceRepository
import com.example.model.CartItem
import com.example.model.ChatMessage
import com.example.model.Currency
import com.example.model.IndianAddress
import com.example.model.IndianDeliveryPartner
import com.example.model.NotificationEntity
import com.example.model.OrderEntity
import com.example.model.ProductEntity
import com.example.model.StoreAnalyticsData
import com.example.model.SupportTicketEntity
import com.example.model.RiderProfile
import com.example.model.UserProfile
import com.example.model.Vendor
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

enum class SortOption(val label: String) {
    FEATURED("Featured"),
    PRICE_LOW_HIGH("Price: Low to High"),
    PRICE_HIGH_LOW("Price: High to Low"),
    HIGHEST_RATED("Highest Rated"),
    FASTEST_DELIVERY("Fastest Delivery")
}

sealed class PaymentState {
    object Idle : PaymentState()
    object Processing : PaymentState()
    data class OtpRequired(val maskedCardOrUpi: String, val otpCode: String) : PaymentState()
    data class Success(val order: OrderEntity) : PaymentState()
    data class Failed(val error: String) : PaymentState()
}

class EcommerceViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: EcommerceRepository
    init {
        val database = OmniCartDatabase.getDatabase(application)
        repository = EcommerceRepository(database.dao())
        viewModelScope.launch {
            repository.initializeCatalogIfEmpty()
        }
    }

    // --- Currency State (Defaults to INR ₹ for Indian Quick-Commerce) ---
    private val _selectedCurrency = MutableStateFlow(Currency.INR)
    val selectedCurrency: StateFlow<Currency> = _selectedCurrency

    fun setCurrency(currency: Currency) {
        _selectedCurrency.value = currency
    }

    // --- Indian Delivery Partners & 10-Minute Micro-Fulfillment ---
    val indianDeliveryPartners: List<IndianDeliveryPartner> get() = repository.indianDeliveryPartners
    val indianPresetAddresses: List<IndianAddress> get() = userAddresses.value

    val userProfile = MutableStateFlow(com.example.model.UserProfile())

    private val _userAddresses = MutableStateFlow<List<IndianAddress>>(repository.indianPresetAddresses)
    val userAddresses: StateFlow<List<IndianAddress>> = _userAddresses

    val selectedAddress = MutableStateFlow<IndianAddress>(
        repository.indianPresetAddresses.first()
    )
    val isTenMinuteDelivery = MutableStateFlow<Boolean>(true)
    val selectedDeliveryPartner = MutableStateFlow<String>("Shadowfax Express")
    
    // Quick-Commerce Customer Journey States
    private val prefs by lazy {
        getApplication<Application>().getSharedPreferences("omnicart_shopper_prefs", Context.MODE_PRIVATE)
    }
    val hasCompletedOnboarding = MutableStateFlow(
        prefs.getBoolean("has_completed_onboarding", false)
    )

    fun completeOnboarding() {
        hasCompletedOnboarding.value = true
        prefs.edit().putBoolean("has_completed_onboarding", true).apply()
    }

    fun resetOnboarding() {
        hasCompletedOnboarding.value = false
        prefs.edit().putBoolean("has_completed_onboarding", false).apply()
    }

    val isLocationConfirmed = MutableStateFlow<Boolean>(false)
    val isUserLoggedIn = MutableStateFlow<Boolean>(false)
    val activeDeliveryEta = MutableStateFlow<String>("10–12 min")
    val selectedDeliveryInstruction = MutableStateFlow<String>("Leave at door")
    val activeLiveTrackingOrder = MutableStateFlow<OrderEntity?>(null)

    fun resetJourney() {
        isLocationConfirmed.value = false
        isUserLoggedIn.value = false
    }

    fun setLocationConfirmed(confirmed: Boolean) {
        isLocationConfirmed.value = confirmed
    }

    fun loginWithPhoneOtp(phone: String, otp: String, name: String? = null): Boolean {
        if (phone.length >= 10 && otp.length >= 4) {
            val updatedName = if (!name.isNullOrBlank()) name.trim() else (if (userProfile.value.name.isNotBlank()) userProfile.value.name else "Customer ${phone.takeLast(4)}")
            val currentId = if (userProfile.value.id.isNotBlank()) userProfile.value.id else "ZENITH-" + (1000..9999).random()
            userProfile.value = userProfile.value.copy(
                id = currentId,
                phone = if (phone.startsWith("+91")) phone else "+91 ${phone.trim()}",
                name = updatedName,
                isPrimeMember = true,
                memberTier = "Gold Member"
            )
            isUserLoggedIn.value = true
            return true
        }
        return false
    }

    fun registerCustomer(
        fullName: String,
        email: String,
        phone: String,
        city: String = "",
        dietary: String = "All Groceries",
        referralCode: String = ""
    ): Boolean {
        if (fullName.isNotBlank() && phone.length >= 10 && email.contains("@")) {
            val welcomeBonus = if (referralCode.isNotBlank()) 100.0 else 50.0
            val newUserId = "ZENITH-" + (1000..9999).random()
            userProfile.value = userProfile.value.copy(
                id = newUserId,
                name = fullName.trim(),
                email = email.trim(),
                phone = if (phone.startsWith("+91")) phone else "+91 ${phone.trim()}",
                city = city.ifBlank { "Primary Hub" },
                dietaryPreference = dietary,
                walletBalanceInr = welcomeBonus,
                isPrimeMember = true,
                memberTier = "Gold Member"
            )
            isUserLoggedIn.value = true
            return true
        }
        return false
    }

    fun logoutCustomer() {
        isUserLoggedIn.value = false
        userProfile.value = UserProfile()
    }

    fun confirmMapLocation(
        label: String,
        flatAndBuilding: String,
        floor: String,
        streetAndLocality: String,
        landmark: String,
        city: String,
        state: String,
        pincode: String,
        deliveryInstructions: String,
        latitude: Double,
        longitude: Double,
        accuracyText: String
    ) {
        val darkstorePod = when {
            city.contains("Bengaluru", ignoreCase = true) || pincode.startsWith("560") -> "OmniCart Indiranagar Pod #04"
            city.contains("Kolkata", ignoreCase = true) || pincode.startsWith("700") -> "OmniCart Salt Lake Pod #02"
            city.contains("Mumbai", ignoreCase = true) || pincode.startsWith("400") -> "OmniCart Bandra West Pod #08"
            city.contains("Delhi", ignoreCase = true) || city.contains("Gurugram", ignoreCase = true) || pincode.startsWith("110") || pincode.startsWith("122") -> "OmniCart CyberCity Pod #11"
            else -> "OmniCart Central Pod #01"
        }
        val distance = 0.8
        val newAddr = IndianAddress(
            id = "ADDR-${System.currentTimeMillis() % 10000}",
            label = label,
            flatAndBuilding = flatAndBuilding,
            streetAndLocality = streetAndLocality,
            landmark = landmark,
            city = city,
            state = state,
            pincode = pincode,
            nearestDarkstorePod = darkstorePod,
            podDistanceKm = distance,
            floor = floor,
            deliveryInstructions = deliveryInstructions,
            latitude = latitude,
            longitude = longitude,
            accuracyText = accuracyText
        )
        _userAddresses.value = listOf(newAddr) + _userAddresses.value.filter { it.id != newAddr.id }
        selectedAddress.value = newAddr
        isLocationConfirmed.value = true
    }

    fun reorderOrder(order: OrderEntity) {
        viewModelScope.launch {
            val catalog = allProducts.value
            val summaries = order.productSummary.split(",").map { it.trim().lowercase() }
            val matched = catalog.filter { prod ->
                summaries.any { s -> prod.title.lowercase().contains(s) || s.contains(prod.title.lowercase()) }
            }
            if (matched.isNotEmpty()) {
                for (p in matched) {
                    repository.addToCart(p.id)
                }
            } else {
                catalog.take(2).forEach { repository.addToCart(it.id) }
            }
        }
    }

    fun selectAddress(address: IndianAddress) {
        selectedAddress.value = address
    }

    fun addNewAddress(
        label: String,
        flatAndBuilding: String,
        streetAndLocality: String,
        landmark: String,
        city: String,
        state: String,
        pincode: String
    ) {
        val darkstorePod = when {
            city.contains("Bengaluru", ignoreCase = true) || pincode.startsWith("560") -> "OmniCart Indiranagar Pod #04"
            city.contains("Mumbai", ignoreCase = true) || pincode.startsWith("400") -> "OmniCart Bandra West Pod #08"
            city.contains("Delhi", ignoreCase = true) || city.contains("Gurugram", ignoreCase = true) || pincode.startsWith("110") || pincode.startsWith("122") -> "OmniCart CyberCity Pod #11"
            else -> "OmniCart Central Pod #01"
        }
        val distance = (0.5 + (pincode.hashCode().mod(5)) * 0.1).coerceIn(0.4, 1.2)
        val newAddr = IndianAddress(
            id = "ADDR-${System.currentTimeMillis() % 10000}",
            label = label,
            flatAndBuilding = flatAndBuilding,
            streetAndLocality = streetAndLocality,
            landmark = landmark,
            city = city,
            state = state,
            pincode = pincode,
            nearestDarkstorePod = darkstorePod,
            podDistanceKm = Math.round(distance * 10.0) / 10.0
        )
        _userAddresses.value = listOf(newAddr) + _userAddresses.value
        selectedAddress.value = newAddr
    }

    fun updateUserProfile(name: String, email: String, phone: String) {
        userProfile.value = userProfile.value.copy(name = name, email = email, phone = phone)
    }

    fun setTenMinuteDelivery(enabled: Boolean) {
        isTenMinuteDelivery.value = enabled
        if (enabled && selectedDeliveryPartner.value in listOf("Delhivery Direct", "BlueDart Prime", "Xpressbees Logistics")) {
            selectedDeliveryPartner.value = "Shadowfax Express"
        }
    }

    fun selectDeliveryPartner(partnerName: String) {
        selectedDeliveryPartner.value = partnerName
    }

    // --- Search & Filters ---
    val searchQuery = MutableStateFlow("")
    val selectedCategory = MutableStateFlow("All")
    val selectedSort = MutableStateFlow(SortOption.FEATURED)
    val fastDeliveryOnly = MutableStateFlow(false)
    val inStockOnly = MutableStateFlow(false)

    val allProducts: StateFlow<List<ProductEntity>> = repository.allProducts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val vendors = MutableStateFlow<List<Vendor>>(repository.vendors)

    fun registerNewVendor(
        name: String,
        gstin: String,
        panNumber: String,
        dispatchHub: String,
        carrierPartner: String,
        bankName: String,
        accountNumberMasked: String,
        ifscCode: String,
        upiId: String
    ) {
        val newVendor = Vendor(
            id = "VEND-${System.currentTimeMillis() % 10000}",
            name = name,
            rating = 4.9f,
            verified = true,
            dispatchHub = dispatchHub,
            avgDispatchHours = if (carrierPartner.contains("Flash", ignoreCase = true) || carrierPartner.contains("Zepto", ignoreCase = true)) 0 else 4,
            fulfillmentScore = 98,
            carrierPartner = carrierPartner,
            activeListingsCount = 4,
            totalRevenueUsd = 0.0,
            bankName = bankName,
            accountNumberMasked = accountNumberMasked,
            ifscCode = ifscCode,
            upiId = upiId,
            settlementCycle = "Daily T+1 (Instant for Flash Pods)",
            gstin = gstin,
            panNumber = panNumber
        )
        vendors.value = listOf(newVendor) + vendors.value
    }

    // --- Rider Fleet Profile & Registration State ---
    private val _currentRider = MutableStateFlow(RiderProfile())
    val currentRider: StateFlow<RiderProfile> = _currentRider

    fun registerOrUpdateRider(
        name: String,
        phone: String,
        vehicleType: String,
        vehicleModel: String,
        vehicleNumber: String,
        licenseNumber: String,
        aadhaarNumber: String,
        payoutUpiId: String,
        assignedPod: String
    ) {
        val maskedAadhaar = if (aadhaarNumber.length >= 4) {
            "•••• •••• " + aadhaarNumber.takeLast(4)
        } else {
            "•••• •••• 8421"
        }
        _currentRider.value = RiderProfile(
            id = "SH-${(1000..9999).random()}",
            name = name.ifBlank { "Ramesh Kumar" },
            phone = phone.ifBlank { "+91 98765 43210" },
            vehicleType = vehicleType,
            vehicleModel = vehicleModel.ifBlank { "Ather 450X EV" },
            vehicleNumber = vehicleNumber.ifBlank { "KA-01-EQ-4821" }.uppercase(),
            licenseNumber = licenseNumber.ifBlank { "DL-KA01-20220084920" }.uppercase(),
            aadhaarMasked = maskedAadhaar,
            payoutUpiId = payoutUpiId.ifBlank { "rider@upi" },
            assignedPod = assignedPod.ifBlank { "Indiranagar Darkstore Pod #04" },
            isKycVerified = true,
            rating = 5.0f,
            completedDeliveriesCount = 0,
            onTimeRatePercent = 100.0
        )
    }

    fun updateVendorPayoutConfig(
        vendorId: String,
        bankName: String,
        accountNum: String,
        ifscCode: String,
        upiId: String,
        settlementCycle: String
    ) {
        val current = vendors.value
        val updated = current.map { v ->
            if (v.id == vendorId) {
                v.copy(
                    bankName = bankName,
                    accountNumberMasked = if (accountNum.length > 4) "•••• " + accountNum.takeLast(4) else accountNum,
                    ifscCode = ifscCode.uppercase(),
                    upiId = upiId,
                    settlementCycle = settlementCycle
                )
            } else v
        }
        vendors.value = updated
    }

    fun updateVendorDeliveryConfig(
        vendorId: String,
        dispatchHub: String,
        carrierPartner: String,
        avgDispatchHours: Int
    ) {
        val current = vendors.value
        val updated = current.map { v ->
            if (v.id == vendorId) {
                v.copy(
                    dispatchHub = dispatchHub,
                    carrierPartner = carrierPartner,
                    avgDispatchHours = avgDispatchHours
                )
            } else v
        }
        vendors.value = updated
    }

    data class FilterCriteria(
        val query: String,
        val category: String,
        val sort: SortOption,
        val fastOnly: Boolean,
        val inStock: Boolean
    )

    private val filterCriteria = combine(
        searchQuery,
        selectedCategory,
        selectedSort,
        fastDeliveryOnly,
        inStockOnly
    ) { query, category, sort, fastOnly, inStock ->
        FilterCriteria(query, category, sort, fastOnly, inStock)
    }

    // Filtered and Sorted Products
    val filteredProducts: StateFlow<List<ProductEntity>> = combine(
        allProducts,
        filterCriteria
    ) { products, criteria ->
        var list = products

        if (criteria.query.isNotBlank()) {
            val q = criteria.query.trim().lowercase()
            list = list.filter {
                it.title.lowercase().contains(q) ||
                it.description.lowercase().contains(q) ||
                it.brand.lowercase().contains(q) ||
                it.category.lowercase().contains(q) ||
                it.vendorName.lowercase().contains(q)
            }
        }

        if (criteria.category != "All") {
            list = list.filter { it.category.equals(criteria.category, ignoreCase = true) }
        }

        if (criteria.fastOnly) {
            list = list.filter { it.isFastDelivery }
        }

        if (criteria.inStock) {
            list = list.filter { it.stock > 0 }
        }

        when (criteria.sort) {
            SortOption.FEATURED -> list.sortedByDescending { it.rating * (if (it.isFlashDeal) 1.2f else 1.0f) }
            SortOption.PRICE_LOW_HIGH -> list.sortedBy { it.priceUsd }
            SortOption.PRICE_HIGH_LOW -> list.sortedByDescending { it.priceUsd }
            SortOption.HIGHEST_RATED -> list.sortedByDescending { it.rating }
            SortOption.FASTEST_DELIVERY -> list.sortedByDescending { it.isFastDelivery }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Recommendation Algorithm (personalized based on browsed category or high rated)
    val recommendedProducts: StateFlow<List<ProductEntity>> = combine(allProducts, selectedCategory) { products, cat ->
        if (cat == "All") {
            products.filter { it.rating >= 4.8f }
        } else {
            val sameCat = products.filter { it.category == cat }
            if (sameCat.isNotEmpty()) sameCat else products.take(4)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val flashDeals: StateFlow<List<ProductEntity>> = allProducts.combine(selectedCategory) { list, _ ->
        list.filter { it.isFlashDeal }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Cart & Wishlist ---
    val cartItems: StateFlow<List<CartItem>> = repository.cartItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val wishlistIds: StateFlow<Set<String>> = repository.wishlistIds
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    val wishlistProducts: StateFlow<List<ProductEntity>> = repository.wishlistProducts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val appliedPromoCode = MutableStateFlow<String?>(null)
    val promoDiscountPercent = MutableStateFlow(0)

    val cartSubtotalUsd: StateFlow<Double> = cartItems.combine(appliedPromoCode) { items, _ ->
        items.sumOf { it.product.priceUsd * it.quantity }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    fun applyCoupon(code: String): Boolean {
        val clean = code.trim().uppercase()
        return if (clean == "OMNI10" || clean == "SAVE10" || clean == "BLINKIT10") {
            appliedPromoCode.value = clean
            promoDiscountPercent.value = 10
            true
        } else if (clean == "VIP20") {
            appliedPromoCode.value = clean
            promoDiscountPercent.value = 20
            true
        } else {
            false
        }
    }

    fun removeCoupon() {
        appliedPromoCode.value = null
        promoDiscountPercent.value = 0
    }

    fun addToCart(productId: String) {
        viewModelScope.launch {
            repository.addToCart(productId)
        }
    }

    fun updateCartQuantity(productId: String, quantity: Int) {
        viewModelScope.launch {
            repository.updateCartQuantity(productId, quantity)
        }
    }

    fun removeFromCart(productId: String) {
        viewModelScope.launch {
            repository.removeFromCart(productId)
        }
    }

    fun clearCart() {
        viewModelScope.launch {
            repository.clearCart()
        }
    }

    fun toggleWishlist(productId: String) {
        viewModelScope.launch {
            repository.toggleWishlist(productId)
        }
    }

    fun removeFromWishlist(productId: String) {
        viewModelScope.launch {
            repository.removeFromWishlist(productId)
        }
    }

    fun clearWishlist() {
        viewModelScope.launch {
            repository.clearWishlist()
        }
    }

    fun moveWishlistItemToCart(productId: String) {
        viewModelScope.launch {
            repository.moveToCart(productId)
        }
    }

    fun saveForLater(productId: String) {
        viewModelScope.launch {
            repository.saveForLater(productId)
        }
    }

    fun moveAllWishlistToCart() {
        viewModelScope.launch {
            val list = wishlistProducts.value
            for (p in list) {
                repository.addToCart(p.id)
            }
            repository.clearWishlist()
        }
    }

    fun triggerAbandonedCartEmail() {
        viewModelScope.launch {
            repository.triggerAbandonedCartReminder(cartItems.value, selectedCurrency.value)
        }
    }

    // --- Orders & 3PL Logistics ---
    val allOrders: StateFlow<List<OrderEntity>> = repository.allOrders
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val paymentState = MutableStateFlow<PaymentState>(PaymentState.Idle)

    fun initiateCheckout(
        paymentMethod: String,
        shippingAddress: String,
        isTenMinDelivery: Boolean = isTenMinuteDelivery.value,
        carrierName: String? = selectedDeliveryPartner.value,
        requires3ds: Boolean = true
    ) {
        if (cartItems.value.isEmpty()) return

        val subtotal = cartSubtotalUsd.value
        val discount = subtotal * (promoDiscountPercent.value / 100.0)
        val discountedSubtotal = (subtotal - discount).coerceAtLeast(0.0)
        val packagingFee = if (cartItems.value.isNotEmpty()) 0.18 else 0.0
        val taxGst = discountedSubtotal * 0.05
        val deliveryFee = if (discountedSubtotal >= 25.0 || cartItems.value.isEmpty()) 0.0 else 1.50
        val totalUsd = if (cartItems.value.isEmpty()) 0.0 else (discountedSubtotal + packagingFee + taxGst + deliveryFee).coerceAtLeast(0.0)

        viewModelScope.launch {
            paymentState.value = PaymentState.Processing
            delay(1200) // Simulate secure SSL 256-bit handshake

            if (requires3ds && (paymentMethod.contains("Card", true) || paymentMethod.contains("Visa", true) || paymentMethod.contains("RuPay", true))) {
                // Trigger 3D Secure OTP verification
                val simulatedOtp = "4921"
                paymentState.value = PaymentState.OtpRequired(
                    maskedCardOrUpi = paymentMethod,
                    otpCode = simulatedOtp
                )
            } else {
                finalizeOrder(paymentMethod, shippingAddress, totalUsd, isTenMinDelivery, carrierName)
            }
        }
    }

    fun confirm3dsOtp(
        otpInput: String,
        paymentMethod: String,
        shippingAddress: String,
        isTenMinDelivery: Boolean = isTenMinuteDelivery.value,
        carrierName: String? = selectedDeliveryPartner.value
    ) {
        val currentState = paymentState.value
        if (currentState is PaymentState.OtpRequired) {
            if (otpInput.trim() == currentState.otpCode || otpInput.trim().length >= 4) {
                viewModelScope.launch {
                    paymentState.value = PaymentState.Processing
                    delay(800)
                    val subtotal = cartSubtotalUsd.value
                    val discount = subtotal * (promoDiscountPercent.value / 100.0)
                    val discountedSubtotal = (subtotal - discount).coerceAtLeast(0.0)
                    val packagingFee = if (cartItems.value.isNotEmpty()) 0.18 else 0.0
                    val taxGst = discountedSubtotal * 0.05
                    val deliveryFee = if (discountedSubtotal >= 25.0 || cartItems.value.isEmpty()) 0.0 else 1.50
                    val totalUsd = if (cartItems.value.isEmpty()) 0.0 else (discountedSubtotal + packagingFee + taxGst + deliveryFee).coerceAtLeast(0.0)
                    finalizeOrder(paymentMethod, shippingAddress, totalUsd, isTenMinDelivery, carrierName)
                }
            } else {
                paymentState.value = PaymentState.Failed("Invalid 3D Secure authorization code. Please retry.")
            }
        }
    }

    private suspend fun finalizeOrder(
        paymentMethod: String,
        address: String,
        totalUsd: Double,
        isTenMinDelivery: Boolean = isTenMinuteDelivery.value,
        carrierName: String? = selectedDeliveryPartner.value
    ) {
        val order = repository.placeOrder(
            items = cartItems.value,
            totalAmountUsd = totalUsd,
            currency = selectedCurrency.value,
            paymentMethodName = paymentMethod,
            shippingAddress = address,
            isTenMinuteDelivery = isTenMinDelivery,
            selectedCarrierName = carrierName
        )
        paymentState.value = PaymentState.Success(order)
    }

    fun resetPaymentState() {
        paymentState.value = PaymentState.Idle
    }

    // 3PL & 10-Minute Instant Darkstore Milestone Simulator
    fun advanceShipmentStatus(orderId: String, currentStatus: String) {
        viewModelScope.launch {
            val order = allOrders.value.find { it.orderId == orderId }
            val is10Min = order?.isTenMinuteDelivery ?: true
            val (nextStatus, nextHub) = if (is10Min) {
                when (currentStatus) {
                    "PLACED" -> "PACKED" to "${order?.darkstoreName ?: "Darkstore Pod"} (Bagged & Sealed in 90s)"
                    "PACKED" -> "IN_TRANSIT" to "Rider ${order?.riderName ?: "Ramesh"} en route on EV (0.9 km away)"
                    "IN_TRANSIT" -> "OUT_FOR_DELIVERY" to "At Your Doorstep! Share OTP: ${order?.deliveryOtp ?: "4829"}"
                    "OUT_FOR_DELIVERY" -> "DELIVERED" to "Delivered at Doorstep! Verified via OTP."
                    else -> "DELIVERED" to "Delivered in 8 mins 42 secs"
                }
            } else {
                when (currentStatus) {
                    "PLACED" -> "PACKED" to "Indian Regional Fulfillment Hub"
                    "PACKED" -> "IN_TRANSIT" to "${order?.carrierName ?: "Delhivery"} Sort Center (Scanned in Transit)"
                    "IN_TRANSIT" -> "OUT_FOR_DELIVERY" to "Local Courier Hub - Out for Delivery"
                    "OUT_FOR_DELIVERY" -> "DELIVERED" to "Delivered to Doorstep (Signed)"
                    else -> "DELIVERED" to "Final Delivery Completed"
                }
            }
            repository.advanceShipmentStatus(orderId, nextStatus, nextHub)
        }
    }

    fun advanceShipmentStatusDirect(orderId: String, status: String, nextHub: String) {
        viewModelScope.launch {
            repository.advanceShipmentStatus(orderId, status, nextHub)
        }
    }

    fun verifyRiderDeliveryOtp(orderId: String, enteredOtp: String): Boolean {
        val order = allOrders.value.find { it.orderId == orderId } ?: return false
        val clean = enteredOtp.trim()
        val expected = order.deliveryOtp ?: "4829"
        if (clean == expected || clean == "1234" || clean.length == 4) {
            viewModelScope.launch {
                repository.advanceShipmentStatus(orderId, "DELIVERED", "Delivered at Doorstep! Verified by Rider via Handover OTP ($clean).")
            }
            return true
        }
        return false
    }

    fun simulateIncomingFlashOrder() {
        viewModelScope.launch {
            val randomItem = allProducts.value.firstOrNull() ?: return@launch
            val item = CartItem(
                product = randomItem,
                quantity = 1
            )
            repository.placeOrder(
                items = listOf(item),
                totalAmountUsd = randomItem.priceUsd,
                currency = selectedCurrency.value,
                paymentMethodName = "Google Pay UPI (Instant)",
                shippingAddress = "#402, 12th Main, HAL 2nd Stage, Indiranagar, Bengaluru - 560038",
                isTenMinuteDelivery = true,
                selectedCarrierName = "Shadowfax Express (10-Min Flash)"
            )
        }
    }

    // --- Multi-Vendor Inventory Management ---
    fun restockProduct(productId: String, amount: Int) {
        viewModelScope.launch {
            repository.restockProduct(productId, amount)
        }
    }

    fun addNewProduct(
        title: String,
        category: String,
        priceUsd: Double,
        stock: Int,
        sku: String,
        vendorId: String = "VEND-GREEN",
        vendorName: String = "GreenGlow Organic Artisans",
        description: String = "",
        brand: String = "OmniFresh",
        fastEta: String = "10 Mins"
    ) {
        viewModelScope.launch {
            val prod = ProductEntity(
                id = "SKU-${System.currentTimeMillis() % 100000}",
                title = title,
                category = category,
                priceUsd = priceUsd,
                originalPriceUsd = priceUsd * 1.25,
                discountPercent = 20,
                rating = 4.8f,
                reviewCount = 1,
                stock = stock,
                sku = sku.ifBlank { "SKU-${(1000..9999).random()}" },
                vendorId = vendorId,
                vendorName = vendorName,
                description = description.ifBlank { "Fresh quick-commerce SKU verified by darkstore merchant." },
                features = "Hyperlocal Flash Delivery, Temperature Controlled, 100% Quality Assured",
                isFastDelivery = true,
                fastDeliveryEta = fastEta,
                isTrending = true,
                isFlashDeal = false,
                flashDealEndsInMinutes = 0,
                brand = brand
            )
            repository.addProduct(prod)
        }
    }

    fun updateProductPriceAndStock(productId: String, priceUsd: Double, stock: Int) {
        viewModelScope.launch {
            repository.updateProductPriceAndStock(productId, priceUsd, stock)
        }
    }

    fun deleteProduct(productId: String) {
        viewModelScope.launch {
            repository.deleteProduct(productId)
        }
    }

    // --- Darkstore Order Fulfillment (Pick, Pack & Dispatch) ---
    fun markOrderAsPacked(orderId: String) {
        viewModelScope.launch {
            val order = allOrders.value.find { it.orderId == orderId }
            val podName = order?.darkstoreName ?: "Darkstore Pod"
            repository.advanceShipmentStatus(
                orderId,
                "PACKED",
                "Packed & Sealed at $podName in 78 seconds (Awaiting carrier pickup)"
            )
        }
    }

    fun handOffOrderToRider(orderId: String) {
        viewModelScope.launch {
            val order = allOrders.value.find { it.orderId == orderId }
            val riderName = order?.riderName ?: "Ramesh Kumar"
            val vehicle = order?.riderVehicle ?: "Ather EV"
            repository.advanceShipmentStatus(
                orderId,
                "IN_TRANSIT",
                "Handed over to Rider $riderName ($vehicle) • 10-Min Flash Transit"
            )
        }
    }

    // --- Analytics Dashboard ---
    val analyticsData: StateFlow<StoreAnalyticsData> = allOrders.combine(cartItems) { orders, _ ->
        repository.getStoreAnalytics(orders)
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        repository.getStoreAnalytics(emptyList())
    )

    // --- Support & Live Chat ---
    val supportTickets: StateFlow<List<SupportTicketEntity>> = repository.allTickets
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val chatMessages = MutableStateFlow<List<ChatMessage>>(
        listOf(
            ChatMessage(
                id = "msg-1",
                sender = "agent",
                text = "Hello! Welcome to OmniCart Global Support. How can we help you today with your orders, payments, or logistics?",
                timestamp = System.currentTimeMillis() - 60000,
                isAutomated = true
            )
        )
    )

    fun sendChatMessage(text: String) {
        if (text.isBlank()) return
        val userMsg = ChatMessage(
            id = UUID.randomUUID().toString(),
            sender = "user",
            text = text,
            timestamp = System.currentTimeMillis()
        )
        chatMessages.value = chatMessages.value + userMsg

        // Automated intelligent response
        viewModelScope.launch {
            delay(1000)
            val replyText = when {
                text.contains("track", true) || text.contains("where", true) || text.contains("delivery", true) ->
                    "You can track real-time 3PL logistics with live GPS transit hubs in the 'Orders' tab. All shipments via FedEx and DHL are updated every 30 minutes."
                text.contains("refund", true) || text.contains("return", true) || text.contains("cancel", true) ->
                    "Our hassle-free return policy offers instant refunds to original payment method within 2-4 hours of carrier pickup."
                text.contains("payment", true) || text.contains("card", true) || text.contains("upi", true) ->
                    "We support 256-bit encrypted global checkout across Cards, UPI, Wallets, and Multi-Currency conversions (USD, EUR, GBP, INR, JPY, CAD)."
                text.contains("vendor", true) || text.contains("seller", true) ->
                    "Vendors undergo strict ISO quality screening. Check the 'Vendor Hub' to view warehouse dispatch speed and inventory counts."
                else ->
                    "Thank you for contacting OmniCart Support! We have logged your request. An automated support ticket has been created with priority status."
            }

            chatMessages.value = chatMessages.value + ChatMessage(
                id = UUID.randomUUID().toString(),
                sender = "agent",
                text = replyText,
                timestamp = System.currentTimeMillis()
            )
        }
    }

    fun createSupportTicket(orderId: String?, subject: String, category: String, message: String) {
        viewModelScope.launch {
            val ticketId = repository.createTicket(orderId, subject, category, message)
            chatMessages.value = chatMessages.value + ChatMessage(
                id = UUID.randomUUID().toString(),
                sender = "system",
                text = "Ticket #$ticketId opened under '$category'. Our 24/7 logistics desk is reviewing it.",
                timestamp = System.currentTimeMillis()
            )
        }
    }

    // --- Notifications ---
    val notifications: StateFlow<List<NotificationEntity>> = repository.allNotifications
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}
