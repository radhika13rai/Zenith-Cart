package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import com.example.ui.components.OmniUniversalMapView
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeliveryDining
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.OrderEntity
import com.example.ui.theme.AmberSecondary
import com.example.ui.theme.CoralPrimary
import com.example.ui.theme.GreenSuccess
import com.example.ui.theme.SlateNavyDark
import com.example.ui.viewmodel.EcommerceViewModel

@Composable
fun OrdersScreen(
    viewModel: EcommerceViewModel,
    onNavigateToSupport: (String?) -> Unit,
    onNavigateToHome: () -> Unit,
    onNavigateToCart: (() -> Unit)? = null
) {
    val orders by viewModel.allOrders.collectAsState()
    val currency by viewModel.selectedCurrency.collectAsState()
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "3PL Logistics & Orders",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Live carrier dispatch and multi-vendor delivery tracking",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(
                onClick = { onNavigateToSupport(null) },
                modifier = Modifier.testTag("orders_support_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.SupportAgent,
                    contentDescription = "Support",
                    tint = CoralPrimary
                )
            }
        }

        if (orders.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.LocalShipping,
                        contentDescription = "No Orders",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(54.dp)
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "No orders placed yet",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Add items to your cart to test live multi-carrier tracking.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = onNavigateToHome,
                        colors = ButtonDefaults.buttonColors(containerColor = CoralPrimary)
                    ) {
                        Text("Browse Products")
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(orders) { order ->
                    OrderTrackingCard(
                        order = order,
                        formattedAmount = currency.format(order.totalAmountUsd),
                        onAdvanceStatus = {
                            viewModel.advanceShipmentStatus(order.orderId, order.shippingStatus)
                        },
                        onCopyTracking = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("Tracking Number", order.trackingNumber))
                            Toast.makeText(context, "Tracking # copied: ${order.trackingNumber}", Toast.LENGTH_SHORT).show()
                        },
                        onReportIssue = {
                            onNavigateToSupport(order.orderId)
                        },
                        onReorder = {
                            viewModel.reorderOrder(order)
                            Toast.makeText(context, "Items added to bag!", Toast.LENGTH_SHORT).show()
                            onNavigateToCart?.invoke()
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun OrderTrackingCard(
    order: OrderEntity,
    formattedAmount: String,
    onAdvanceStatus: () -> Unit,
    onCopyTracking: () -> Unit,
    onReportIssue: () -> Unit,
    onReorder: () -> Unit = {}
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.testTag("order_card_${order.orderId}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row: Order ID, Date & Amount
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = order.orderId,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 15.sp,
                        color = CoralPrimary
                    )
                    Text(
                        text = "Vendor: ${order.vendorName}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = formattedAmount,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = when (order.shippingStatus) {
                            "DELIVERED" -> GreenSuccess.copy(alpha = 0.15f)
                            "IN_TRANSIT", "OUT_FOR_DELIVERY" -> CoralPrimary.copy(alpha = 0.15f)
                            else -> AmberSecondary.copy(alpha = 0.2f)
                        }
                    ) {
                        Text(
                            text = order.shippingStatus.replace("_", " "),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = when (order.shippingStatus) {
                                "DELIVERED" -> GreenSuccess
                                "IN_TRANSIT", "OUT_FOR_DELIVERY" -> CoralPrimary
                                else -> SlateNavyDark
                            },
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = order.productSummary,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2
            )

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
            Spacer(modifier = Modifier.height(12.dp))

            // 10-Minute Flash Delivery & Carrier Banner
            if (order.isTenMinuteDelivery) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = CoralPrimary.copy(alpha = 0.08f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CoralPrimary.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .background(CoralPrimary, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Bolt,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "⚡ 10-Min Flash Pod Delivery",
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 12.sp,
                                        color = CoralPrimary
                                    )
                                    Text(
                                        text = "Partner: ${order.carrierName} • ${order.darkstoreName}",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = CoralPrimary
                            ) {
                                Text(
                                    text = "ETA 10m",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        HorizontalDivider(color = CoralPrimary.copy(alpha = 0.2f))
                        Spacer(modifier = Modifier.height(8.dp))

                        // Assigned Rider & OTP verification
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.DeliveryDining,
                                    contentDescription = null,
                                    tint = SlateNavyDark,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Column {
                                    Text(
                                        text = "Rider: ${order.riderName}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = SlateNavyDark
                                    )
                                    Text(
                                        text = order.riderVehicle,
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            // OTP Chip
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = GreenSuccess.copy(alpha = 0.15f)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Lock,
                                        contentDescription = null,
                                        tint = GreenSuccess,
                                        modifier = Modifier.size(11.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = "OTP: ${order.deliveryOtp}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = GreenSuccess
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
            }

            // Simplified Delivery Partner Radar Map
            if (order.isTenMinuteDelivery) {
                DeliveryRouteMap(status = order.shippingStatus)
                Spacer(modifier = Modifier.height(12.dp))
            }

            // 3PL Carrier Info & Tracking Number with Copy Action
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(10.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (order.isTenMinuteDelivery) Icons.Default.DeliveryDining else Icons.Default.LocalShipping,
                        contentDescription = null,
                        tint = SlateNavyDark,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Text(
                            text = order.carrierName,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = SlateNavyDark
                        )
                        Text(
                            text = "Tracking: ${order.trackingNumber}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                IconButton(
                    onClick = onCopyTracking,
                    modifier = Modifier.size(28.dp).testTag("copy_trk_${order.orderId}")
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copy",
                        tint = CoralPrimary,
                        modifier = Modifier.size(15.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Logistics Timeline
            TrackingTimeline(
                status = order.shippingStatus,
                currentHub = order.currentHub,
                isTenMinuteDelivery = order.isTenMinuteDelivery
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Action Buttons: Advance Simulator & Report Issue
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (order.shippingStatus != "DELIVERED") {
                    Button(
                        onClick = onAdvanceStatus,
                        colors = ButtonDefaults.buttonColors(containerColor = if (order.isTenMinuteDelivery) CoralPrimary else SlateNavyDark),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f).testTag("advance_status_btn_${order.orderId}")
                    ) {
                        Icon(imageVector = Icons.Default.FastForward, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (order.isTenMinuteDelivery) "⚡ Advance Pod Status" else "Next 3PL Hub",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    Button(
                        onClick = onReorder,
                        colors = ButtonDefaults.buttonColors(containerColor = CoralPrimary),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f).testTag("reorder_btn_${order.orderId}")
                    ) {
                        Icon(imageVector = Icons.Default.Bolt, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Reorder Items", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Button(
                    onClick = onReportIssue,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f).testTag("report_issue_btn_${order.orderId}")
                ) {
                    Icon(imageVector = Icons.Default.SupportAgent, contentDescription = null, tint = CoralPrimary, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Support Ticket", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = CoralPrimary)
                }
            }
        }
    }
}

@Composable
fun TrackingTimeline(status: String, currentHub: String, isTenMinuteDelivery: Boolean = false) {
    val stages = if (isTenMinuteDelivery) {
        listOf(
            "Order Received & Verified" to (status in listOf("PLACED", "PACKED", "IN_TRANSIT", "OUT_FOR_DELIVERY", "DELIVERED")),
            "Packed at Darkstore in 90s" to (status in listOf("PACKED", "IN_TRANSIT", "OUT_FOR_DELIVERY", "DELIVERED")),
            "Rider en route on EV" to (status in listOf("IN_TRANSIT", "OUT_FOR_DELIVERY", "DELIVERED")),
            "At Doorstep (Share OTP)" to (status in listOf("OUT_FOR_DELIVERY", "DELIVERED")),
            "Delivered & Verified" to (status == "DELIVERED")
        )
    } else {
        listOf(
            "Order Confirmed" to (status in listOf("PLACED", "PACKED", "IN_TRANSIT", "OUT_FOR_DELIVERY", "DELIVERED")),
            "Packed at Hub" to (status in listOf("PACKED", "IN_TRANSIT", "OUT_FOR_DELIVERY", "DELIVERED")),
            "In 3PL Transit" to (status in listOf("IN_TRANSIT", "OUT_FOR_DELIVERY", "DELIVERED")),
            "Out for Delivery" to (status in listOf("OUT_FOR_DELIVERY", "DELIVERED")),
            "Delivered" to (status == "DELIVERED")
        )
    }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        stages.forEachIndexed { index, (label, isCompleted) ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(18.dp)
                        .background(
                            if (isCompleted) GreenSuccess else Color.LightGray,
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (isCompleted) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = label,
                        fontSize = 12.sp,
                        fontWeight = if (isCompleted) FontWeight.Bold else FontWeight.Normal,
                        color = if (isCompleted) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (index == 2 && status == "IN_TRANSIT") {
                        Text(
                            text = "📍 $currentHub",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = CoralPrimary
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DeliveryRouteMap(status: String) {
    var showEmbeddedMap by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(true) }

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SlateNavyDark.copy(alpha = 0.05f)),
        modifier = Modifier.fillMaxWidth().testTag("delivery_route_map_card")
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "LIVE GPS ROUTE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = SlateNavyDark,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (showEmbeddedMap) CoralPrimary else MaterialTheme.colorScheme.surface,
                        modifier = Modifier.clickable { showEmbeddedMap = !showEmbeddedMap }
                    ) {
                        Text(
                            text = if (showEmbeddedMap) "🗺️ Google Map" else "⚡ Radar View",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (showEmbeddedMap) Color.White else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                Text(
                    text = when (status) {
                        "PLACED" -> "Pod packing items"
                        "PACKED" -> "Rider at darkstore"
                        "IN_TRANSIT" -> "⚡ Arriving in 6 min"
                        "OUT_FOR_DELIVERY" -> "🛵 At your doorstep!"
                        else -> "Delivered"
                    },
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = CoralPrimary
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            val riderRatio = when (status) {
                "PLACED" -> 0.08f
                "PACKED" -> 0.28f
                "IN_TRANSIT" -> 0.65f
                "OUT_FOR_DELIVERY" -> 0.92f
                else -> 1.0f
            }

            val storeLocation = LatLng(22.5890, 88.4110)
            val customerLocation = LatLng(22.5855, 88.4144)
            val riderLocation = LatLng(
                storeLocation.latitude + (customerLocation.latitude - storeLocation.latitude) * riderRatio,
                storeLocation.longitude + (customerLocation.longitude - storeLocation.longitude) * riderRatio
            )

            if (showEmbeddedMap) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(10.dp))
                ) {
                    OmniUniversalMapView(
                        modifier = Modifier.fillMaxSize().testTag("customer_tracking_live_map"),
                        latitude = customerLocation.latitude,
                        longitude = customerLocation.longitude,
                        zoom = 15,
                        podLat = storeLocation.latitude,
                        podLng = storeLocation.longitude,
                        podName = "OmniCart Pod #02",
                        riderLat = riderLocation.latitude,
                        riderLng = riderLocation.longitude,
                        riderName = "Rahul Verma",
                        interactive = false,
                        showControls = true,
                        showExternalMapButton = true
                    )
                }
            } else {
                // Canvas Route line with Store, Rider, and Customer
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(36.dp)
                ) {
                    androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                        val w = size.width
                        val midY = size.height / 2

                        // Base Route Line
                        drawLine(
                            color = androidx.compose.ui.graphics.Color.LightGray.copy(alpha = 0.8f),
                            start = androidx.compose.ui.geometry.Offset(24f, midY),
                            end = androidx.compose.ui.geometry.Offset(w - 24f, midY),
                            strokeWidth = 6f
                        )
                        // Traveled line
                        drawLine(
                            color = CoralPrimary,
                            start = androidx.compose.ui.geometry.Offset(24f, midY),
                            end = androidx.compose.ui.geometry.Offset(24f + (w - 48f) * riderRatio, midY),
                            strokeWidth = 6f
                        )
                    }

                    // Store Icon
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .background(SlateNavyDark, CircleShape)
                            .align(Alignment.CenterStart),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Storefront, contentDescription = "Store", tint = androidx.compose.ui.graphics.Color.White, modifier = Modifier.size(14.dp))
                    }

                    // Customer Icon
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .background(GreenSuccess, CircleShape)
                            .align(Alignment.CenterEnd),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.LocationOn, contentDescription = "Customer", tint = androidx.compose.ui.graphics.Color.White, modifier = Modifier.size(14.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            val context = LocalContext.current
            OutlinedButton(
                onClick = {
                    try {
                        val mapIntent = Intent(Intent.ACTION_VIEW, Uri.parse("geo:22.5855,88.4144?q=Salt+Lake+Sector+2+Kolkata"))
                        context.startActivity(mapIntent)
                    } catch (e: Exception) {
                        try {
                            val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://maps.google.com/?q=Salt+Lake+Sector+2+Kolkata"))
                            context.startActivity(webIntent)
                        } catch (err: Exception) {
                            Toast.makeText(context, "Maps app not found", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().testTag("customer_open_maps_btn"),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = CoralPrimary)
            ) {
                Icon(Icons.Default.Navigation, contentDescription = null, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Open Live Route in Google Maps", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
