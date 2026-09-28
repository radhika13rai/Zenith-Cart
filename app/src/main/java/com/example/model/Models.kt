package com.example.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey val id: String,
    val title: String,
    val category: String,
    val priceUsd: Double,
    val originalPriceUsd: Double,
    val discountPercent: Int,
    val rating: Float,
    val reviewCount: Int,
    val stock: Int,
    val sku: String,
    val vendorId: String,
    val vendorName: String,
    val description: String,
    val features: String, // comma separated
    val isFastDelivery: Boolean,
    val fastDeliveryEta: String, // e.g. "12 Mins"
    val isTrending: Boolean,
    val isFlashDeal: Boolean,
    val flashDealEndsInMinutes: Int,
    val brand: String
)

data class Vendor(
    val id: String,
    val name: String,
    val rating: Float,
    val verified: Boolean,
    val dispatchHub: String,
    val avgDispatchHours: Int,
    val fulfillmentScore: Int,
    val carrierPartner: String,
    val activeListingsCount: Int,
    val totalRevenueUsd: Double,
    val bankName: String = "HDFC Bank Ltd.",
    val accountNumberMasked: String = "•••• 8421",
    val ifscCode: String = "HDFC0001234",
    val upiId: String = "merchant@okhdfcbank",
    val settlementCycle: String = "Daily T+1 (Instant for Flash Pods)",
    val gstin: String = "29AABCU9603R1ZM",
    val panNumber: String = "AABCU9603R",
    val gatewayMode: String = "OmniCart Master Split Gateway (No Vendor Setup Required)"
)

@Entity(tableName = "cart_items")
data class CartItemEntity(
    @PrimaryKey val productId: String,
    val quantity: Int,
    val addedTimestamp: Long = System.currentTimeMillis()
)

data class CartItem(
    val product: ProductEntity,
    val quantity: Int
)

@Entity(tableName = "wishlist_items")
data class WishlistItemEntity(
    @PrimaryKey val productId: String,
    val addedTimestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "orders")
data class OrderEntity(
    @PrimaryKey val orderId: String,
    val timestamp: Long,
    val totalAmountUsd: Double,
    val currencyCode: String,
    val paymentMethod: String,
    val paymentStatus: String, // "CAPTURED", "AUTHORIZED"
    val paymentTxnId: String,
    val shippingStatus: String, // "PLACED", "PACKED", "IN_TRANSIT", "OUT_FOR_DELIVERY", "DELIVERED"
    val carrierName: String, // "Shadowfax Express", "Zepto Fleet", "Blinkit Quick", "Delhivery Direct", "BlueDart Prime", "Xpressbees"
    val trackingNumber: String,
    val estimatedDelivery: String,
    val currentHub: String,
    val shippingAddress: String,
    val vendorName: String,
    val productSummary: String,
    val itemCount: Int,
    val isTenMinuteDelivery: Boolean = true,
    val riderName: String = "Ramesh Kumar",
    val riderPhone: String = "+91 98765 43210",
    val deliveryOtp: String = "4829",
    val darkstoreName: String = "Indiranagar Darkstore Pod #04",
    val riderVehicle: String = "Ather 450X EV (KA-01-EQ-4821)"
)

data class IndianDeliveryPartner(
    val id: String,
    val name: String,
    val type: String, // "10-MIN FLASH POD", "EXPRESS AIR 3PL", "HYPER-LOCAL RUNNER"
    val averageEta: String,
    val coverage: String,
    val description: String,
    val activeFleetSize: String,
    val onTimeRatePercent: Int
)

data class IndianAddress(
    val id: String,
    val label: String, // "Home", "Work", "Other"
    val flatAndBuilding: String,
    val streetAndLocality: String,
    val landmark: String,
    val city: String,
    val state: String,
    val pincode: String,
    val nearestDarkstorePod: String,
    val podDistanceKm: Double,
    val floor: String = "",
    val deliveryInstructions: String = "",
    val latitude: Double = 12.9716,
    val longitude: Double = 77.5946,
    val accuracyText: String = "High (±5m)"
) {
    val fullFormattedAddress: String
        get() = buildString {
            append(flatAndBuilding)
            if (floor.isNotBlank()) append(", Floor $floor")
            append(", $streetAndLocality")
            if (landmark.isNotBlank()) append(" (Near $landmark)")
            append(", $city, $state - $pincode")
        }
}

data class UserProfile(
    val id: String = "",
    val name: String = "",
    val phone: String = "",
    val email: String = "",
    val isPrimeMember: Boolean = false,
    val savedCardsCount: Int = 0,
    val walletBalanceInr: Double = 0.0,
    val memberTier: String = "Standard Member",
    val city: String = "",
    val dietaryPreference: String = "All Groceries"
)

@Entity(tableName = "support_tickets")
data class SupportTicketEntity(
    @PrimaryKey val ticketId: String,
    val orderId: String?,
    val subject: String,
    val category: String,
    val status: String, // "OPEN", "IN_PROGRESS", "RESOLVED"
    val createdAt: Long,
    val lastUpdate: Long,
    val lastMessage: String
)

data class ChatMessage(
    val id: String,
    val sender: String, // "user", "agent", "system"
    val text: String,
    val timestamp: Long,
    val isAutomated: Boolean = false
)

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey val id: String,
    val title: String,
    val message: String,
    val timestamp: Long,
    val type: String, // "ABANDONED_CART", "ORDER_UPDATE", "INVENTORY_ALERT", "PROMO"
    val isRead: Boolean = false,
    val relatedOrderId: String? = null
)

data class StoreAnalyticsData(
    val totalGmvUsd: Double,
    val totalOrders: Int,
    val activeVisitors: Int,
    val conversionRatePercent: Double,
    val abandonedCartRatePercent: Double,
    val averageOrderValueUsd: Double,
    val hourlyTraffic: List<Int>,
    val topCategories: List<CategoryMetric>,
    val vendorMetrics: List<VendorPerformance>
)

data class CategoryMetric(
    val category: String,
    val salesPercent: Int,
    val revenueUsd: Double
)

data class VendorPerformance(
    val vendorName: String,
    val orderCount: Int,
    val onTimeFulfillmentPercent: Int,
    val rating: Float
)
