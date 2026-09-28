package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import java.net.URLEncoder
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
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.ui.draw.clip
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ElectricScooter
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.OrderEntity
import com.example.ui.theme.AmberSecondary
import com.example.ui.theme.CoralPrimary
import com.example.ui.theme.GreenSuccess
import com.example.ui.theme.RedDanger
import com.example.ui.theme.SlateNavyDark
import com.example.ui.viewmodel.EcommerceViewModel

@Composable
fun RiderFleetScreen(viewModel: EcommerceViewModel) {
    val orders by viewModel.allOrders.collectAsState()
    val context = LocalContext.current

    var isOnDuty by remember { mutableStateOf(true) }
    var showRiderKycDialog by remember { mutableStateOf(false) }
    var orderForOtpDialog by remember { mutableStateOf<OrderEntity?>(null) }
    var showLastDeliveryCelebration by remember { mutableStateOf(false) }

    // Filter active orders that need rider action
    val activeDeliveries = orders.filter { it.shippingStatus != "DELIVERED" }
    val completedDeliveries = orders.filter { it.shippingStatus == "DELIVERED" }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Rider Profile & Duty Bar
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SlateNavyDark),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .background(CoralPrimary, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.ElectricScooter,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Ramesh Kumar (SH-8421)",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                                Text(
                                    text = "Shadowfax 10-Min Flash Fleet",
                                    color = Color(0xFFA6A9C0),
                                    fontSize = 11.sp
                                )
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.Star,
                                        contentDescription = null,
                                        tint = AmberSecondary,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = "4.92 ★ (1,480 Drops) • Ather 450X EV",
                                        color = AmberSecondary,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Switch(
                                checked = isOnDuty,
                                onCheckedChange = {
                                    isOnDuty = it
                                    Toast.makeText(
                                        context,
                                        if (it) "Duty ON: Ready for 10-Min Flash Dispatches!" else "Duty OFF: Shift Paused",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = GreenSuccess
                                )
                            )
                            Text(
                                text = if (isOnDuty) "🟢 ON DUTY" else "🔴 OFF DUTY",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isOnDuty) GreenSuccess else RedDanger
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Daily Earnings & Speed Metric Highlights
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White.copy(alpha = 0.08f), RoundedCornerShape(10.dp))
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Today's Earnings", fontSize = 10.sp, color = Color(0xFFA6A9C0))
                            Text("₹820", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = GreenSuccess)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Completed Drops", fontSize = 10.sp, color = Color(0xFFA6A9C0))
                            Text("${completedDeliveries.size + 11}", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Avg Delivery Time", fontSize = 10.sp, color = Color(0xFFA6A9C0))
                            Text("10.2 Mins", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = AmberSecondary)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("On-Time SLA", fontSize = 10.sp, color = Color(0xFFA6A9C0))
                            Text("99.4%", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = CoralPrimary)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Rider KYC & 3PL Integration Button
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color.White.copy(alpha = 0.12f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showRiderKycDialog = true }
                            .testTag("rider_kyc_details_btn")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = GreenSuccess, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Rider KYC & 3PL Partner Integration", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                            Text("View Details >", color = AmberSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Live Celebration Banner if order was just completed
        if (showLastDeliveryCelebration) {
            item {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = GreenSuccess.copy(alpha = 0.12f)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, GreenSuccess),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = GreenSuccess, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("Delivery Completed & Verified!", fontWeight = FontWeight.Bold, color = GreenSuccess, fontSize = 13.sp)
                                Text("+₹55 Base Payout + ₹15 Flash Speed Bonus credited!", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface)
                            }
                        }
                        Text(
                            text = "Dismiss",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = GreenSuccess,
                            modifier = Modifier.clickable { showLastDeliveryCelebration = false }
                        )
                    }
                }
            }
        }

        // Section: Active Delivery Queue
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Active Dispatch Queue (${activeDeliveries.size})",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                if (activeDeliveries.isEmpty()) {
                    Button(
                        onClick = {
                            viewModel.simulateIncomingFlashOrder()
                            Toast.makeText(context, "New 10-Min Flash Order dispatched to rider!", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CoralPrimary),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.height(32.dp).testTag("simulate_order_button")
                    ) {
                        Icon(Icons.Default.FlashOn, contentDescription = null, modifier = Modifier.size(13.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Simulate Flash Order", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        if (activeDeliveries.isEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = GreenSuccess, modifier = Modifier.size(36.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Queue All Clear!", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text(
                            text = "You are currently online. When a customer orders within your 2 km darkstore radius, the flash dispatch alert will sound here.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        } else {
            items(activeDeliveries, key = { it.orderId }) { order ->
                RiderOrderCard(
                    order = order,
                    onAdvanceStatus = { currentStatus ->
                        viewModel.advanceShipmentStatus(order.orderId, currentStatus)
                    },
                    onRequestOtpHandover = {
                        orderForOtpDialog = order
                    }
                )
            }
        }

        // Section: Completed Trips History
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Recent Completed Deliveries",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        if (completedDeliveries.isEmpty()) {
            item {
                Text(
                    text = "No deliveries completed in this session yet.",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            items(completedDeliveries.take(5), key = { it.orderId }) { order ->
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(GreenSuccess.copy(alpha = 0.15f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = GreenSuccess, modifier = Modifier.size(18.dp))
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Order #${order.orderId}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                                Text(
                                    text = "To: ${order.shippingAddress.take(24)}...",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "+₹65.00",
                                fontWeight = FontWeight.ExtraBold,
                                color = GreenSuccess,
                                fontSize = 13.sp
                            )
                            Text(
                                text = "Verified via OTP",
                                fontSize = 9.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }

    // Modal: Handover OTP Verification Dialog
    orderForOtpDialog?.let { order ->
        var enteredOtp by remember { mutableStateOf("") }
        var isError by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { orderForOtpDialog = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Key, contentDescription = null, tint = CoralPrimary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Doorstep Handover OTP", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Ask the customer for their 4-digit security OTP shown on their OmniCart Orders screen to complete the handover.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Helper badge for demo purposes
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = AmberSecondary.copy(alpha = 0.15f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.FlashOn, contentDescription = null, tint = AmberSecondary, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Customer's Active OTP: ${order.deliveryOtp ?: "4829"}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = SlateNavyDark
                            )
                        }
                    }

                    OutlinedTextField(
                        value = enteredOtp,
                        onValueChange = {
                            if (it.length <= 4) enteredOtp = it
                            isError = false
                        },
                        label = { Text("4-Digit OTP") },
                        placeholder = { Text("e.g. ${order.deliveryOtp ?: "4829"}") },
                        isError = isError,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("rider_otp_input")
                    )

                    if (isError) {
                        Text(
                            text = "Incorrect OTP. Please check with customer.",
                            color = RedDanger,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val verified = viewModel.verifyRiderDeliveryOtp(order.orderId, enteredOtp)
                        if (verified) {
                            Toast.makeText(context, "OTP Verified! Order marked as Delivered.", Toast.LENGTH_SHORT).show()
                            showLastDeliveryCelebration = true
                            orderForOtpDialog = null
                        } else {
                            isError = true
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CoralPrimary),
                    modifier = Modifier.testTag("confirm_rider_otp")
                ) {
                    Text("Verify & Complete Delivery")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { orderForOtpDialog = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Modal: Rider KYC & 3PL Integration Details Dialog
    if (showRiderKycDialog) {
        AlertDialog(
            onDismissRequest = { showRiderKycDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Security, contentDescription = null, tint = GreenSuccess)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Rider KYC & Partner Dispatch", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    // Fleet Verification Badge
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = GreenSuccess.copy(alpha = 0.12f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = GreenSuccess, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text("Aadhaar & Police Verified Rider", color = GreenSuccess, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                Text("Cleared for 10-Minute Hyperlocal Micro-Fulfillment", color = SlateNavyDark, fontSize = 10.sp)
                            }
                        }
                    }

                    // Onboarding Documents Checklist
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Onboarding Documents & Telemetry:", fontSize = 11.sp, fontWeight = FontWeight.Bold)

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = GreenSuccess, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("UIDAI Aadhaar: •••• •••• 8421 (Active)", fontSize = 11.sp)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = GreenSuccess, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Driving License: DL-KA01-20220084920 (Commercial EV)", fontSize = 11.sp)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = GreenSuccess, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("EV Registration: Ather 450X (KA-01-EQ-4821)", fontSize = 11.sp)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = GreenSuccess, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Insurance: Digit Transit Cover ₹5,00,000", fontSize = 11.sp)
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = SlateNavyDark,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("3PL Partner Integration Architecture:", color = AmberSecondary, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("• Dispatch Engine: Shadowfax Hyperlocal Radius API\n• Geo-Fence: Indiranagar Pod #04 (< 2.5 km radius)\n• Doorstep Security: 4-digit Handover OTP verification\n• Daily Payouts: Auto-credited to linked UPI/Bank", color = Color.White, fontSize = 10.sp, lineHeight = 14.sp)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showRiderKycDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = CoralPrimary)
                ) {
                    Text("Got It")
                }
            }
        )
    }
}

@Composable
private fun RiderOrderCard(
    order: OrderEntity,
    onAdvanceStatus: (String) -> Unit,
    onRequestOtpHandover: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, CoralPrimary.copy(alpha = 0.4f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        modifier = Modifier.fillMaxWidth().testTag("rider_order_card_${order.orderId}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header Row: Order ID, Distance, Payout
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = CoralPrimary.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = if (order.isTenMinuteDelivery) "⚡ 10-MIN FLASH" else "📦 EXPRESS",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = CoralPrimary,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "#${order.orderId}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }

                Text(
                    text = "Payout: ₹65.00",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = GreenSuccess
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Pickup Node (Darkstore Pod)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Store, contentDescription = null, tint = AmberSecondary, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "Pick up at: ${order.darkstoreName ?: "OmniCart Indiranagar Pod #04"}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Distance: 0.3 km away • Pod Bay #3",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Delivery Destination (Customer)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.LocationOn, contentDescription = null, tint = CoralPrimary, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "Deliver to: ${order.shippingAddress}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 2
                    )
                    Text(
                        text = "Payment: ${order.paymentMethod} • ₹${String.format("%.2f", order.totalAmountUsd * 83.0)}",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            val pickupLoc = LatLng(22.5890, 88.4110)
            val dropLoc = LatLng(22.5855, 88.4144)
            val riderCamState = rememberCameraPositionState {
                position = CameraPosition.fromLatLngZoom(LatLng(22.5872, 88.4127), 15f)
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .clip(RoundedCornerShape(8.dp))
            ) {
                OmniUniversalMapView(
                    modifier = Modifier.fillMaxSize().testTag("rider_embedded_live_map_${order.orderId}"),
                    latitude = dropLoc.latitude,
                    longitude = dropLoc.longitude,
                    zoom = 15,
                    podLat = pickupLoc.latitude,
                    podLng = pickupLoc.longitude,
                    podName = "OmniCart Pod #02",
                    interactive = false,
                    showControls = true,
                    showExternalMapButton = false
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            val context = LocalContext.current
            OutlinedButton(
                onClick = {
                    try {
                        val encodedAddr = URLEncoder.encode(order.shippingAddress, "UTF-8")
                        val mapIntent = Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=$encodedAddr"))
                        context.startActivity(mapIntent)
                    } catch (e: Exception) {
                        try {
                            val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://maps.google.com/?q=" + URLEncoder.encode(order.shippingAddress, "UTF-8")))
                            context.startActivity(webIntent)
                        } catch (err: Exception) {
                            Toast.makeText(context, "Map unavailable on this device", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().testTag("rider_open_maps_${order.orderId}"),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = CoralPrimary)
            ) {
                Icon(Icons.Default.Navigation, contentDescription = null, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Open Live Route in Google Maps", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Current Step Phase Box
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Current Status: ${order.shippingStatus}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = CoralPrimary
                    )
                    Text(
                        text = order.currentHub.take(28) + if (order.currentHub.length > 28) "..." else "",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Rider Action Button depending on current fulfillment status
            when (order.shippingStatus) {
                "PLACED" -> {
                    Button(
                        onClick = { onAdvanceStatus("PLACED") },
                        colors = ButtonDefaults.buttonColors(containerColor = CoralPrimary),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth().height(42.dp)
                    ) {
                        Icon(Icons.Default.Store, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("1. Arrived at Pod & Picked Up", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
                "PACKED" -> {
                    Button(
                        onClick = { onAdvanceStatus("PACKED") },
                        colors = ButtonDefaults.buttonColors(containerColor = SlateNavyDark),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth().height(42.dp)
                    ) {
                        Icon(Icons.Default.Navigation, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("2. Start GPS Route (En Route to Customer)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
                "IN_TRANSIT" -> {
                    Button(
                        onClick = { onAdvanceStatus("IN_TRANSIT") },
                        colors = ButtonDefaults.buttonColors(containerColor = AmberSecondary),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth().height(42.dp)
                    ) {
                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = SlateNavyDark, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("3. Arrived at Customer Doorstep", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SlateNavyDark)
                    }
                }
                "OUT_FOR_DELIVERY" -> {
                    Button(
                        onClick = { onRequestOtpHandover() },
                        colors = ButtonDefaults.buttonColors(containerColor = GreenSuccess),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth().height(42.dp).testTag("rider_handover_otp_button")
                    ) {
                        Icon(Icons.Default.Key, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("4. Enter Customer OTP to Handover", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
                else -> {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = GreenSuccess.copy(alpha = 0.15f),
                        modifier = Modifier.fillMaxWidth().height(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("Delivery Completed & Handed Over", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = GreenSuccess)
                        }
                    }
                }
            }
        }
    }
}
