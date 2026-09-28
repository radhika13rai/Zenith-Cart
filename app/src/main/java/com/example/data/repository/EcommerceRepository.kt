package com.example.data.repository

import com.example.data.local.OmniCartDao
import com.example.model.CartItem
import com.example.model.CartItemEntity
import com.example.model.CategoryMetric
import com.example.model.ChatMessage
import com.example.model.Currency
import com.example.model.IndianAddress
import com.example.model.IndianDeliveryPartner
import com.example.model.NotificationEntity
import com.example.model.OrderEntity
import com.example.model.ProductEntity
import com.example.model.StoreAnalyticsData
import com.example.model.SupportTicketEntity
import com.example.model.Vendor
import com.example.model.VendorPerformance
import com.example.model.WishlistItemEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.util.UUID

class EcommerceRepository(private val dao: OmniCartDao) {

    val allProducts: Flow<List<ProductEntity>> = dao.getAllProducts()
    val allOrders: Flow<List<OrderEntity>> = dao.getAllOrders()
    val allTickets: Flow<List<SupportTicketEntity>> = dao.getAllTickets()
    val allNotifications: Flow<List<NotificationEntity>> = dao.getAllNotifications()

    // Combined Cart Items with full Product details
    val cartItems: Flow<List<CartItem>> = combine(dao.getCartItems(), dao.getAllProducts()) { cartEntities, products ->
        val productMap = products.associateBy { it.id }
        cartEntities.mapNotNull { entity ->
            productMap[entity.productId]?.let { product ->
                CartItem(product = product, quantity = entity.quantity)
            }
        }
    }

    val wishlistIds: Flow<Set<String>> = dao.getWishlistItems().map { items ->
        items.map { it.productId }.toSet()
    }

    // Reactive list of saved Wishlist products from Room
    val wishlistProducts: Flow<List<ProductEntity>> = combine(dao.getWishlistItems(), dao.getAllProducts()) { wishlistEntities, products ->
        val productMap = products.associateBy { it.id }
        wishlistEntities.mapNotNull { entity ->
            productMap[entity.productId]
        }
    }

    // Indian Delivery Partners & Quick Commerce Fleets
    val indianDeliveryPartners = listOf(
        IndianDeliveryPartner(
            id = "PARTNER-SFX",
            name = "Shadowfax Express",
            type = "10-MIN FLASH POD",
            averageEta = "8-11 Mins",
            coverage = "Pan-India Metros (1500+ Darkstore Pods)",
            description = "Hyper-local darkstore fulfillment with dedicated electric two-wheeler fleet and instant bag-to-door dispatch.",
            activeFleetSize = "150,000+ Active EV Riders",
            onTimeRatePercent = 99
        ),
        IndianDeliveryPartner(
            id = "PARTNER-ZEPTO",
            name = "Zepto Quick Fleet",
            type = "10-MIN FLASH POD",
            averageEta = "7-10 Mins",
            coverage = "Bengaluru, Mumbai, Delhi-NCR, Hyderabad, Pune",
            description = "Optimized micro-warehouses packed within 1.5km radius. Average packing time under 90 seconds.",
            activeFleetSize = "85,000+ Quick Runners",
            onTimeRatePercent = 98
        ),
        IndianDeliveryPartner(
            id = "PARTNER-BLINKIT",
            name = "Blinkit Fast Network",
            type = "10-MIN FLASH POD",
            averageEta = "9-12 Mins",
            coverage = "Top 35+ Indian Urban Cities",
            description = "Instant algorithmic pod routing with live telemetry and doorstep OTP confirmation.",
            activeFleetSize = "110,000+ Fleet Riders",
            onTimeRatePercent = 99
        ),
        IndianDeliveryPartner(
            id = "PARTNER-DLV",
            name = "Delhivery Direct",
            type = "EXPRESS AIR & SURFACE",
            averageEta = "Same Day / Next Day",
            coverage = "18,500+ Indian Pincodes",
            description = "India's largest integrated logistics provider for nationwide surface and air cargo express fulfillment.",
            activeFleetSize = "Automated Mega-Sort Centers",
            onTimeRatePercent = 97
        ),
        IndianDeliveryPartner(
            id = "PARTNER-BLD",
            name = "BlueDart Prime",
            type = "AVIATION EXPRESS AIR",
            averageEta = "Next Morning by 10 AM",
            coverage = "55,000+ Locations Nationwide",
            description = "Dedicated Boeing cargo aircraft fleet with temperature-controlled air transit and premium tracking.",
            activeFleetSize = "6 Dedicated Cargo Aircraft",
            onTimeRatePercent = 99
        ),
        IndianDeliveryPartner(
            id = "PARTNER-XPRESS",
            name = "Xpressbees Logistics",
            type = "NATIONWIDE 3PL",
            averageEta = "1-2 Business Days",
            coverage = "Tier 1 to Tier 4 Indian Cities",
            description = "Scalable multi-vendor logistics with live geo-fencing and automated return-to-origin workflows.",
            activeFleetSize = "Pan-India Hub Network",
            onTimeRatePercent = 96
        )
    )

    // Preset Indian Addresses for Quick Commerce Selection
    val indianPresetAddresses = listOf(
        IndianAddress(
            id = "ADDR-1",
            label = "Home",
            flatAndBuilding = "Flat 402, Prestige Ozone",
            streetAndLocality = "12th Main, HAL 2nd Stage, Indiranagar",
            landmark = "Near Metro Pillar 84",
            city = "Bengaluru",
            state = "Karnataka",
            pincode = "560038",
            nearestDarkstorePod = "OmniCart Indiranagar Pod #04",
            podDistanceKm = 0.6
        ),
        IndianAddress(
            id = "ADDR-2",
            label = "Work",
            flatAndBuilding = "Tower 4, 11th Floor, DLF CyberCity",
            streetAndLocality = "DLF Phase 2, Sector 24",
            landmark = "Opposite Rapid Metro Station",
            city = "Gurugram",
            state = "Haryana (Delhi NCR)",
            pincode = "122002",
            nearestDarkstorePod = "CyberCity QuickPod #11",
            podDistanceKm = 0.8
        ),
        IndianAddress(
            id = "ADDR-3",
            label = "Apartment",
            flatAndBuilding = "B-204, Sea Green Apartments",
            streetAndLocality = "Perry Cross Road, Bandra West",
            landmark = "Behind St. Andrews College",
            city = "Mumbai",
            state = "Maharashtra",
            pincode = "400050",
            nearestDarkstorePod = "Bandra West Darkstore #08",
            podDistanceKm = 0.5
        ),
        IndianAddress(
            id = "ADDR-4",
            label = "Other",
            flatAndBuilding = "Villa 18, Road No. 36",
            streetAndLocality = "Jubilee Hills",
            landmark = "Near Peddamma Temple",
            city = "Hyderabad",
            state = "Telangana",
            pincode = "500033",
            nearestDarkstorePod = "Jubilee QuickHub #03",
            podDistanceKm = 0.9
        )
    )

    // Multi-Vendor Logistics Profiles with Indian Darkstores & Carriers
    val vendors = listOf(
        Vendor(
            id = "VEND-APEX",
            name = "Apex Electronics India",
            rating = 4.9f,
            verified = true,
            dispatchHub = "Koramangala Darkstore Pod #12, Bengaluru",
            avgDispatchHours = 0,
            fulfillmentScore = 99,
            carrierPartner = "Shadowfax Express (10-Min Flash)",
            activeListingsCount = 8,
            totalRevenueUsd = 142850.00,
            bankName = "HDFC Bank Ltd.",
            accountNumberMasked = "•••• 9182",
            ifscCode = "HDFC0001234",
            upiId = "apex.settlement@okhdfcbank",
            settlementCycle = "Instant for 10-Min Flash / T+1 Daily",
            gstin = "29AABCU9603R1ZM",
            panNumber = "AABCU9603R"
        ),
        Vendor(
            id = "VEND-LUXE",
            name = "Luxe Atelier & Apparel India",
            rating = 4.8f,
            verified = true,
            dispatchHub = "Lower Parel Logistics Center, Mumbai",
            avgDispatchHours = 4,
            fulfillmentScore = 98,
            carrierPartner = "Delhivery Direct Express",
            activeListingsCount = 12,
            totalRevenueUsd = 98400.00,
            bankName = "ICICI Bank",
            accountNumberMasked = "•••• 4410",
            ifscCode = "ICIC0000004",
            upiId = "luxe.settlement@icici",
            settlementCycle = "Daily T+1 Automated Split",
            gstin = "27AABCL8412K1Z9",
            panNumber = "AABCL8412K"
        ),
        Vendor(
            id = "VEND-GREEN",
            name = "GreenGlow Organic Artisans",
            rating = 4.95f,
            verified = true,
            dispatchHub = "Indiranagar QuickPod #04, Bengaluru",
            avgDispatchHours = 0,
            fulfillmentScore = 100,
            carrierPartner = "Zepto Quick Fleet (10-Min Flash)",
            activeListingsCount = 6,
            totalRevenueUsd = 45200.00,
            bankName = "State Bank of India",
            accountNumberMasked = "•••• 7329",
            ifscCode = "SBIN0000812",
            upiId = "greenglow.payout@oksbi",
            settlementCycle = "Instant Flash Pod Settlement",
            gstin = "29AAACG4129L1Z2",
            panNumber = "AAACG4129L"
        ),
        Vendor(
            id = "VEND-NOVA",
            name = "Nova Living India",
            rating = 4.7f,
            verified = true,
            dispatchHub = "Bhiwandi E-Commerce Cargo Park, Thane",
            avgDispatchHours = 6,
            fulfillmentScore = 96,
            carrierPartner = "BlueDart Prime Aviation",
            activeListingsCount = 9,
            totalRevenueUsd = 76100.00,
            bankName = "Axis Bank",
            accountNumberMasked = "•••• 1928",
            ifscCode = "UTIB0000123",
            upiId = "novaliving.ops@axisbank",
            settlementCycle = "Weekly Consolidated (Every Monday)",
            gstin = "27AAACN5910P1Z6",
            panNumber = "AAACN5910P"
        )
    )

    suspend fun initializeCatalogIfEmpty() {
        val existing = dao.getAllProducts().first()
        val hasGroceries = existing.any { it.category in listOf("Produce", "Dairy", "Snacks", "Pantry", "Household") }
        if (existing.isEmpty() || !hasGroceries) {
            val initialCatalog = listOf(
                // 1. Produce
                ProductEntity(
                    id = "PROD-GROC-01",
                    title = "Fresh Organic Robusta Bananas (1kg)",
                    category = "Produce",
                    priceUsd = 0.72,
                    originalPriceUsd = 0.95,
                    discountPercent = 24,
                    rating = 4.9f,
                    reviewCount = 2840,
                    stock = 65,
                    sku = "BAN-ROB-1K",
                    vendorId = "VEND-GREEN",
                    vendorName = "GreenGlow Organic Artisans",
                    description = "Naturally ripened, chemical-free farm-fresh Cavendish Robusta bananas. High in potassium and dietary fiber.",
                    features = "100% Farm Fresh, Zero Carbide, Rich in Potassium, Daily Morning Harvest",
                    isFastDelivery = true,
                    fastDeliveryEta = "8 Mins",
                    isTrending = true,
                    isFlashDeal = true,
                    flashDealEndsInMinutes = 35,
                    brand = "FarmDirect"
                ),
                ProductEntity(
                    id = "PROD-GROC-02",
                    title = "Hydroponic Crisp Cherry Tomatoes (250g)",
                    category = "Produce",
                    priceUsd = 1.08,
                    originalPriceUsd = 1.45,
                    discountPercent = 25,
                    rating = 4.85f,
                    reviewCount = 920,
                    stock = 38,
                    sku = "TOM-CHERRY-250",
                    vendorId = "VEND-GREEN",
                    vendorName = "GreenGlow Organic Artisans",
                    description = "Vine-ripened pesticide-free sweet cherry tomatoes grown in climate-controlled hydroponic greenhouses.",
                    features = "Pesticide-Free, Vine Ripened, High Lycopene, Clean Washed",
                    isFastDelivery = true,
                    fastDeliveryEta = "10 Mins",
                    isTrending = true,
                    isFlashDeal = false,
                    flashDealEndsInMinutes = 0,
                    brand = "PurePluck"
                ),
                ProductEntity(
                    id = "PROD-GROC-03",
                    title = "Crisp Royal Gala Apples (4 Pack)",
                    category = "Produce",
                    priceUsd = 1.68,
                    originalPriceUsd = 2.15,
                    discountPercent = 22,
                    rating = 4.8f,
                    reviewCount = 1430,
                    stock = 45,
                    sku = "APP-GALA-4PK",
                    vendorId = "VEND-GREEN",
                    vendorName = "GreenGlow Organic Artisans",
                    description = "Hand-picked crisp Shimla Royal Gala apples with natural sweetness, smooth texture, and high antioxidant content.",
                    features = "Wax-Free, Hand-Picked, Rich in Antioxidants, Sweet & Crisp",
                    isFastDelivery = true,
                    fastDeliveryEta = "10 Mins",
                    isTrending = false,
                    isFlashDeal = false,
                    flashDealEndsInMinutes = 0,
                    brand = "Himachal Orchards"
                ),

                // 2. Dairy
                ProductEntity(
                    id = "PROD-GROC-04",
                    title = "Country Fresh Full Cream Milk (1 Litre)",
                    category = "Dairy",
                    priceUsd = 0.82,
                    originalPriceUsd = 0.90,
                    discountPercent = 9,
                    rating = 4.95f,
                    reviewCount = 6420,
                    stock = 120,
                    sku = "MILK-FULL-1L",
                    vendorId = "VEND-GREEN",
                    vendorName = "GreenGlow Organic Artisans",
                    description = "Homogenized and pasteurized pure cow's milk with 6.0% fat content. Sourced directly from local cooperative dairy farms.",
                    features = "6.0% Fat / 9.0% SNF, Chilled at 4°C, Zero Adulteration, Glass Bottle Sealed",
                    isFastDelivery = true,
                    fastDeliveryEta = "7 Mins",
                    isTrending = true,
                    isFlashDeal = true,
                    flashDealEndsInMinutes = 20,
                    brand = "Amul Heritage"
                ),
                ProductEntity(
                    id = "PROD-GROC-05",
                    title = "Organic Free-Range Brown Eggs (6 Pack)",
                    category = "Dairy",
                    priceUsd = 1.02,
                    originalPriceUsd = 1.30,
                    discountPercent = 21,
                    rating = 4.9f,
                    reviewCount = 3100,
                    stock = 80,
                    sku = "EGG-BROWN-6PK",
                    vendorId = "VEND-GREEN",
                    vendorName = "GreenGlow Organic Artisans",
                    description = "Nutritious brown eggs from pasture-raised hens fed on 100% vegetarian corn & flaxseed grain diet. Rich in Omega-3.",
                    features = "Pasture-Raised, High Omega-3, UV Sanitized, Antibiotic-Free",
                    isFastDelivery = true,
                    fastDeliveryEta = "8 Mins",
                    isTrending = true,
                    isFlashDeal = false,
                    flashDealEndsInMinutes = 0,
                    brand = "Eggoz Prime"
                ),
                ProductEntity(
                    id = "PROD-GROC-06",
                    title = "Farm Fresh Artisanal Malai Paneer (200g)",
                    category = "Dairy",
                    priceUsd = 1.14,
                    originalPriceUsd = 1.40,
                    discountPercent = 18,
                    rating = 4.88f,
                    reviewCount = 2180,
                    stock = 52,
                    sku = "PAN-MALAI-200",
                    vendorId = "VEND-GREEN",
                    vendorName = "GreenGlow Organic Artisans",
                    description = "Ultra-soft cottage cheese made from 100% pasteurized whole cow's milk with silky texture and rich protein content.",
                    features = "Vacuum Packed, 18g Protein per 100g, Zero Preservatives, Melt-in-Mouth",
                    isFastDelivery = true,
                    fastDeliveryEta = "9 Mins",
                    isTrending = false,
                    isFlashDeal = true,
                    flashDealEndsInMinutes = 40,
                    brand = "MilkyMist"
                ),

                // 3. Snacks
                ProductEntity(
                    id = "PROD-GROC-07",
                    title = "Peri-Peri Gourmet Handcrafted Kettle Crisps (120g)",
                    category = "Snacks",
                    priceUsd = 0.90,
                    originalPriceUsd = 1.20,
                    discountPercent = 25,
                    rating = 4.85f,
                    reviewCount = 4120,
                    stock = 90,
                    sku = "SNK-CRISP-PERI",
                    vendorId = "VEND-LUXE",
                    vendorName = "Luxe Atelier & Apparel",
                    description = "Batch-cooked thick cut farm potatoes crisped in rice bran oil and dusted with fiery African bird's eye chili.",
                    features = "Zero Trans Fat, Gluten-Free, Kettle Cooked, Nitrogen Flushed",
                    isFastDelivery = true,
                    fastDeliveryEta = "8 Mins",
                    isTrending = true,
                    isFlashDeal = true,
                    flashDealEndsInMinutes = 50,
                    brand = "BRB Popped"
                ),
                ProductEntity(
                    id = "PROD-GROC-08",
                    title = "Slow-Roasted California Salted Almonds (200g)",
                    category = "Snacks",
                    priceUsd = 2.64,
                    originalPriceUsd = 3.35,
                    discountPercent = 21,
                    rating = 4.92f,
                    reviewCount = 1890,
                    stock = 40,
                    sku = "NUT-ALM-SALT-200",
                    vendorId = "VEND-LUXE",
                    vendorName = "Luxe Atelier & Apparel",
                    description = "Jumbo California Nonpareil almonds dry-roasted with pink Himalayan salt. Loaded with vitamin E and healthy fats.",
                    features = "Dry Roasted, Pink Himalayan Salt, High Fiber, Resealable Pouch",
                    isFastDelivery = true,
                    fastDeliveryEta = "10 Mins",
                    isTrending = true,
                    isFlashDeal = false,
                    flashDealEndsInMinutes = 0,
                    brand = "NuttyGritties"
                ),
                ProductEntity(
                    id = "PROD-GROC-09",
                    title = "Belgian Artisan 70% Dark Chocolate Bar (100g)",
                    category = "Snacks",
                    priceUsd = 2.15,
                    originalPriceUsd = 2.80,
                    discountPercent = 23,
                    rating = 4.9f,
                    reviewCount = 1250,
                    stock = 34,
                    sku = "CHOC-DARK-70",
                    vendorId = "VEND-LUXE",
                    vendorName = "Luxe Atelier & Apparel",
                    description = "Ethically sourced single-origin West African cocoa beans conched to perfection with subtle fruity notes.",
                    features = "70% Cocoa, Single Origin, Vegan Friendly, Fairtrade Certified",
                    isFastDelivery = true,
                    fastDeliveryEta = "10 Mins",
                    isTrending = false,
                    isFlashDeal = true,
                    flashDealEndsInMinutes = 60,
                    brand = "Paul And Mike"
                ),

                // 4. Pantry
                ProductEntity(
                    id = "PROD-GROC-10",
                    title = "Cold-Pressed Extra Virgin Olive Oil (500ml)",
                    category = "Pantry",
                    priceUsd = 5.39,
                    originalPriceUsd = 6.95,
                    discountPercent = 22,
                    rating = 4.95f,
                    reviewCount = 1670,
                    stock = 28,
                    sku = "OIL-OLIVE-EV-500",
                    vendorId = "VEND-GREEN",
                    vendorName = "GreenGlow Organic Artisans",
                    description = "First cold press of Spanish Arbequina olives. Acidity below 0.3%, rich in polyphenols and monounsaturated oleic acid.",
                    features = "First Cold Pressed, Acidity < 0.3%, Dark Glass Bottle, Spanish Olives",
                    isFastDelivery = true,
                    fastDeliveryEta = "12 Mins",
                    isTrending = true,
                    isFlashDeal = false,
                    flashDealEndsInMinutes = 0,
                    brand = "Borges Natura"
                ),
                ProductEntity(
                    id = "PROD-GROC-11",
                    title = "Stone-Ground Sharbati Whole Wheat Atta (5kg)",
                    category = "Pantry",
                    priceUsd = 3.11,
                    originalPriceUsd = 3.80,
                    discountPercent = 18,
                    rating = 4.87f,
                    reviewCount = 3890,
                    stock = 60,
                    sku = "PAN-ATTA-5KG",
                    vendorId = "VEND-GREEN",
                    vendorName = "GreenGlow Organic Artisans",
                    description = "Traditional slow stone-milled MP Sharbati wheat flour retaining 100% natural bran and germ for softest rotis.",
                    features = "100% Whole Grain, Slow Stone Ground, Zero Maida, High Fiber",
                    isFastDelivery = true,
                    fastDeliveryEta = "12 Mins",
                    isTrending = false,
                    isFlashDeal = false,
                    flashDealEndsInMinutes = 0,
                    brand = "Aashirvaad Select"
                ),

                // 5. Household
                ProductEntity(
                    id = "PROD-GROC-12",
                    title = "Eco-Friendly Concentrated Citrus Dishwash Gel (750ml)",
                    category = "Household",
                    priceUsd = 1.62,
                    originalPriceUsd = 2.10,
                    discountPercent = 23,
                    rating = 4.88f,
                    reviewCount = 2410,
                    stock = 45,
                    sku = "HH-DISH-750",
                    vendorId = "VEND-NOVA",
                    vendorName = "Nova Nordic Home Living",
                    description = "Plant-powered grease stripping formulation infused with organic sweet orange oil. Gentle on skin and septic-safe.",
                    features = "Plant-Based Surfactants, Real Lemon & Orange Extracts, Tough on Grease, Gentle on Hands",
                    isFastDelivery = true,
                    fastDeliveryEta = "10 Mins",
                    isTrending = true,
                    isFlashDeal = true,
                    flashDealEndsInMinutes = 45,
                    brand = "Vim Pure"
                ),
                ProductEntity(
                    id = "PROD-GROC-13",
                    title = "3-Ply Bamboo Soft Kitchen Towel Rolls (Pack of 2)",
                    category = "Household",
                    priceUsd = 1.32,
                    originalPriceUsd = 1.75,
                    discountPercent = 25,
                    rating = 4.82f,
                    reviewCount = 1530,
                    stock = 50,
                    sku = "HH-TOWEL-2PK",
                    vendorId = "VEND-NOVA",
                    vendorName = "Nova Nordic Home Living",
                    description = "100% unbleached natural organic bamboo pulp. Ultra-absorbent, lint-free, compostable, and tree-free.",
                    features = "100% Bamboo Pulp, Ultra Absorbent 3-Ply, Chemical Free, Compostable",
                    isFastDelivery = true,
                    fastDeliveryEta = "10 Mins",
                    isTrending = false,
                    isFlashDeal = false,
                    flashDealEndsInMinutes = 0,
                    brand = "Beco Eco"
                ),

                // 6. Electronics & Lifestyle
                ProductEntity(
                    id = "PROD-101",
                    title = "AuraPro Wireless ANC Headphones",
                    category = "Electronics",
                    priceUsd = 199.99,
                    originalPriceUsd = 279.99,
                    discountPercent = 28,
                    rating = 4.9f,
                    reviewCount = 1420,
                    stock = 24,
                    sku = "AP-ANC-01",
                    vendorId = "VEND-APEX",
                    vendorName = "Apex Electronics Corp",
                    description = "Ultra-premium 45mm beryllium drivers, 50-hour battery life, hybrid active noise cancellation, and lossless spatial audio.",
                    features = "Hybrid ANC, 50h Battery, Spatial Audio, Fast Charge, Bluetooth 5.4",
                    isFastDelivery = true,
                    fastDeliveryEta = "15 Mins",
                    isTrending = true,
                    isFlashDeal = true,
                    flashDealEndsInMinutes = 45,
                    brand = "AuraPro"
                ),
                ProductEntity(
                    id = "PROD-102",
                    title = "UltraSlim Titanium Smartwatch Ultra",
                    category = "Electronics",
                    priceUsd = 249.00,
                    originalPriceUsd = 320.00,
                    discountPercent = 22,
                    rating = 4.8f,
                    reviewCount = 890,
                    stock = 15,
                    sku = "TI-SMART-8",
                    vendorId = "VEND-APEX",
                    vendorName = "Apex Electronics Corp",
                    description = "Grade 5 titanium chassis, sapphire crystal display, ECG + Blood Oxygen sensor, 100m water resistance, and 7-day battery.",
                    features = "Grade 5 Titanium, Sapphire Glass, ECG & SpO2, Dual-band GPS",
                    isFastDelivery = true,
                    fastDeliveryEta = "20 Mins",
                    isTrending = true,
                    isFlashDeal = false,
                    flashDealEndsInMinutes = 0,
                    brand = "Chronos"
                )
            )
            dao.insertProducts(initialCatalog)

            // Seed initial notifications & orders
            val sampleOrder = OrderEntity(
                orderId = "ORD-89412",
                timestamp = System.currentTimeMillis() - 420000L,
                totalAmountUsd = 279.49,
                currencyCode = "INR",
                paymentMethod = "Google Pay UPI (UPI-ID: ananya@okhdfc)",
                paymentStatus = "CAPTURED",
                paymentTxnId = "UPI_TXN_99214782",
                shippingStatus = "OUT_FOR_DELIVERY",
                carrierName = "Shadowfax Express",
                trackingNumber = "SFX-882910492",
                estimatedDelivery = "10 Mins (Arriving in 3 mins)",
                currentHub = "Koramangala Darkstore Pod #12 (400m away)",
                shippingAddress = "Flat 402, Prestige Ozone, 12th Main, HAL 2nd Stage, Indiranagar, Bengaluru - 560038",
                vendorName = "Apex Electronics India",
                productSummary = "AuraPro Wireless ANC Headphones + 1 item",
                itemCount = 2,
                isTenMinuteDelivery = true,
                riderName = "Ramesh Kumar",
                riderPhone = "+91 98765 43210",
                deliveryOtp = "4829",
                darkstoreName = "Koramangala Darkstore Pod #12",
                riderVehicle = "Ather 450X EV (KA-01-EQ-4821)"
            )
            dao.insertOrder(sampleOrder)

            dao.insertNotification(
                NotificationEntity(
                    id = UUID.randomUUID().toString(),
                    title = "⚡ Rider Ramesh is 300m away!",
                    message = "Your 10-Min Flash Delivery with Shadowfax (#SFX-882910492) is arriving now. Share OTP 4829 on arrival.",
                    timestamp = System.currentTimeMillis() - 120000L,
                    type = "ORDER_UPDATE",
                    isRead = false,
                    relatedOrderId = "ORD-89412"
                )
            )

            dao.insertTicket(
                SupportTicketEntity(
                    ticketId = "TCK-4819",
                    orderId = "ORD-89412",
                    subject = "Delivery window request",
                    category = "Shipping Logistics",
                    status = "IN_PROGRESS",
                    createdAt = System.currentTimeMillis() - 5000000L,
                    lastUpdate = System.currentTimeMillis() - 1000000L,
                    lastMessage = "Agent Michael: Package is scheduled for delivery before 2 PM."
                )
            )
        }
    }

    // --- Cart Actions ---
    suspend fun addToCart(productId: String) {
        val existing = dao.getCartItems().first().find { it.productId == productId }
        if (existing != null) {
            dao.updateCartQuantity(productId, existing.quantity + 1)
        } else {
            dao.insertCartItem(CartItemEntity(productId = productId, quantity = 1))
        }
    }

    suspend fun updateCartQuantity(productId: String, quantity: Int) {
        if (quantity <= 0) {
            dao.deleteCartItem(productId)
        } else {
            dao.updateCartQuantity(productId, quantity)
        }
    }

    suspend fun removeFromCart(productId: String) {
        dao.deleteCartItem(productId)
    }

    suspend fun clearCart() {
        dao.clearCart()
    }

    // --- Wishlist Actions ---
    suspend fun toggleWishlist(productId: String) {
        val items = dao.getWishlistItems().first()
        val exists = items.any { it.productId == productId }
        if (exists) {
            dao.deleteWishlistItem(productId)
        } else {
            dao.insertWishlist(WishlistItemEntity(productId = productId))
        }
    }

    suspend fun removeFromWishlist(productId: String) {
        dao.deleteWishlistItem(productId)
    }

    suspend fun clearWishlist() {
        dao.clearWishlist()
    }

    suspend fun moveToCart(productId: String) {
        addToCart(productId)
        dao.deleteWishlistItem(productId)
    }

    suspend fun saveForLater(productId: String) {
        dao.deleteCartItem(productId)
        val items = dao.getWishlistItems().first()
        if (!items.any { it.productId == productId }) {
            dao.insertWishlist(WishlistItemEntity(productId = productId))
        }
    }

    // --- Inventory Updates ---
    suspend fun restockProduct(productId: String, additionalStock: Int) {
        val product = dao.getProductByIdDirect(productId)
        if (product != null) {
            val newStock = product.stock + additionalStock
            dao.updateStock(productId, newStock)
            dao.insertNotification(
                NotificationEntity(
                    id = UUID.randomUUID().toString(),
                    title = "📦 Inventory Restocked",
                    message = "${product.title} restocked by +$additionalStock units (Total: $newStock).",
                    timestamp = System.currentTimeMillis(),
                    type = "INVENTORY_ALERT"
                )
            )
        }
    }

    suspend fun addProduct(product: ProductEntity) {
        dao.insertProduct(product)
        dao.insertNotification(
            NotificationEntity(
                id = UUID.randomUUID().toString(),
                title = "✨ New SKU Listed",
                message = "${product.title} added to ${product.category} catalog (${product.stock} units in stock).",
                timestamp = System.currentTimeMillis(),
                type = "INVENTORY_ALERT"
            )
        )
    }

    suspend fun updateProductPriceAndStock(productId: String, priceUsd: Double, stock: Int) {
        dao.updateProductPriceAndStock(productId, priceUsd, stock)
    }

    suspend fun deleteProduct(productId: String) {
        dao.deleteProduct(productId)
    }

    // --- Checkout & Payment Gateway ---
    suspend fun placeOrder(
        items: List<CartItem>,
        totalAmountUsd: Double,
        currency: Currency,
        paymentMethodName: String,
        shippingAddress: String,
        isTenMinuteDelivery: Boolean = true,
        selectedCarrierName: String? = null
    ): OrderEntity {
        val orderId = "ORD-${(10000..99999).random()}"
        val primaryVendor = items.firstOrNull()?.product?.vendorName ?: "OmniCart QuickPod India"
        val vendorId = items.firstOrNull()?.product?.vendorId ?: "VEND-APEX"
        val vendorObj = vendors.find { it.id == vendorId }
        val carrier = selectedCarrierName ?: if (isTenMinuteDelivery) "Shadowfax Express" else (vendorObj?.carrierPartner ?: "Delhivery Direct")
        val trackingNo = when {
            carrier.contains("Shadowfax", true) -> "SFX-${(10000000..99999999).random()}"
            carrier.contains("Zepto", true) -> "ZPT-${(10000000..99999999).random()}"
            carrier.contains("Blinkit", true) -> "BLK-${(10000000..99999999).random()}"
            carrier.contains("Delhivery", true) -> "DLV-${(10000000..99999999).random()}"
            carrier.contains("BlueDart", true) -> "BLD-${(10000000..99999999).random()}"
            else -> "TRK-${(10000000..99999999).random()}"
        }

        // Deduct inventory
        for (item in items) {
            val currentStock = item.product.stock
            val newStock = (currentStock - item.quantity).coerceAtLeast(0)
            dao.updateStock(item.product.id, newStock)
        }

        val indianRiders = listOf("Ramesh Kumar", "Vikram Singh", "Sunil Gowda", "Arun Patil", "Deepak Sharma", "Mohammed Faizal")
        val riderName = indianRiders.random()
        val randomOtp = (1000..9999).random().toString()
        val darkstores = listOf(
            "Indiranagar Darkstore Pod #04",
            "Koramangala Darkstore Pod #12",
            "Bandra West Micro-Pod #08",
            "CyberCity QuickPod #02"
        )
        val podName = if (isTenMinuteDelivery) darkstores.random() else "${vendorObj?.dispatchHub ?: "Central Indian Sorting Hub"}"

        val order = OrderEntity(
            orderId = orderId,
            timestamp = System.currentTimeMillis(),
            totalAmountUsd = totalAmountUsd,
            currencyCode = currency.code,
            paymentMethod = paymentMethodName,
            paymentStatus = "CAPTURED",
            paymentTxnId = if (paymentMethodName.contains("UPI", true)) "UPI_TXN_${(10000000..99999999).random()}" else "TXN_SEC_${(100000..999999).random()}",
            shippingStatus = "PLACED",
            carrierName = carrier,
            trackingNumber = trackingNo,
            estimatedDelivery = if (isTenMinuteDelivery) "10 Mins (Instant Dispatch)" else "Tomorrow by 2:00 PM",
            currentHub = "$podName (Packing in progress)",
            shippingAddress = shippingAddress,
            vendorName = primaryVendor,
            productSummary = items.joinToString(", ") { "${it.product.title} x${it.quantity}" },
            itemCount = items.sumOf { it.quantity },
            isTenMinuteDelivery = isTenMinuteDelivery,
            riderName = riderName,
            riderPhone = "+91 ${(9800000000L..9899999999L).random()}",
            deliveryOtp = randomOtp,
            darkstoreName = podName,
            riderVehicle = "Ather 450X EV (KA-01-EQ-${(1000..9999).random()})"
        )

        dao.insertOrder(order)
        dao.clearCart()

        // Send simulated Order Confirmation Push Notification
        dao.insertNotification(
            NotificationEntity(
                id = UUID.randomUUID().toString(),
                title = if (isTenMinuteDelivery) "⚡ 10-Min Delivery Dispatched ($orderId)" else "🎉 Order Confirmed ($orderId)",
                message = if (isTenMinuteDelivery)
                    "Dispatched via $carrier from $podName. Rider $riderName assigned. Share OTP $randomOtp upon delivery."
                else
                    "Payment of ${currency.format(totalAmountUsd)} verified via $paymentMethodName. Carrier: $carrier.",
                timestamp = System.currentTimeMillis(),
                type = "ORDER_UPDATE",
                relatedOrderId = orderId
            )
        )

        return order
    }

    // --- Automated Abandoned Cart Simulation ---
    suspend fun triggerAbandonedCartReminder(cartItems: List<CartItem>, currency: Currency) {
        if (cartItems.isEmpty()) return
        val total = cartItems.sumOf { it.product.priceUsd * it.quantity }
        val firstItem = cartItems.first().product.title
        dao.insertNotification(
            NotificationEntity(
                id = UUID.randomUUID().toString(),
                title = "🛒 Left Something Behind?",
                message = "Your cart (${currency.format(total)}) with $firstItem is reserved! Use code OMNI10 for 10% off before stock runs out.",
                timestamp = System.currentTimeMillis(),
                type = "ABANDONED_CART"
            )
        )
    }

    // --- Advance 3PL Logistics Tracking Simulation ---
    suspend fun advanceShipmentStatus(orderId: String, nextStatus: String, hubName: String) {
        dao.updateOrderStatus(orderId, nextStatus, hubName)
        dao.insertNotification(
            NotificationEntity(
                id = UUID.randomUUID().toString(),
                title = "🚚 3PL Logistics Update ($orderId)",
                message = "Status changed to: $nextStatus at $hubName.",
                timestamp = System.currentTimeMillis(),
                type = "ORDER_UPDATE",
                relatedOrderId = orderId
            )
        )
    }

    // --- Support Tickets ---
    suspend fun createTicket(orderId: String?, subject: String, category: String, message: String): String {
        val ticketId = "TCK-${(1000..9999).random()}"
        val ticket = SupportTicketEntity(
            ticketId = ticketId,
            orderId = orderId,
            subject = subject,
            category = category,
            status = "OPEN",
            createdAt = System.currentTimeMillis(),
            lastUpdate = System.currentTimeMillis(),
            lastMessage = "You: $message"
        )
        dao.insertTicket(ticket)
        return ticketId
    }

    // --- Analytics Computation ---
    fun getStoreAnalytics(orders: List<OrderEntity>): StoreAnalyticsData {
        val totalGmv = orders.sumOf { it.totalAmountUsd } + 42390.00
        val totalOrderCount = orders.size + 148
        val aov = if (totalOrderCount > 0) totalGmv / totalOrderCount else 0.0

        return StoreAnalyticsData(
            totalGmvUsd = totalGmv,
            totalOrders = totalOrderCount,
            activeVisitors = 1840,
            conversionRatePercent = 4.2,
            abandonedCartRatePercent = 18.5,
            averageOrderValueUsd = aov,
            hourlyTraffic = listOf(140, 210, 380, 520, 890, 1140, 960, 720),
            topCategories = listOf(
                CategoryMetric("Electronics", 46, totalGmv * 0.46),
                CategoryMetric("Fashion", 24, totalGmv * 0.24),
                CategoryMetric("Home & Living", 18, totalGmv * 0.18),
                CategoryMetric("Health & Gourmet", 12, totalGmv * 0.12)
            ),
            vendorMetrics = vendors.map { v ->
                VendorPerformance(
                    vendorName = v.name,
                    orderCount = (24..78).random(),
                    onTimeFulfillmentPercent = v.fulfillmentScore,
                    rating = v.rating
                )
            }
        )
    }
}
