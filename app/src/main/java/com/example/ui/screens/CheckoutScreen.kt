package com.example.ui.screens

import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.CurrencyBitcoin
import androidx.compose.material.icons.filled.DeliveryDining
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
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
import androidx.compose.ui.graphics.Color
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
import com.example.ui.viewmodel.PaymentState

@Composable
fun CheckoutScreen(
    viewModel: EcommerceViewModel,
    onNavigateBack: () -> Unit,
    onOrderPlaced: (String) -> Unit,
    onNavigateToLocationPicker: () -> Unit = {}
) {
    val currency by viewModel.selectedCurrency.collectAsState()
    val cartItems by viewModel.cartItems.collectAsState()
    val subtotal by viewModel.cartSubtotalUsd.collectAsState()
    val promoDiscountPercent by viewModel.promoDiscountPercent.collectAsState()
    val paymentState by viewModel.paymentState.collectAsState()
    val selectedAddressFromVm by viewModel.selectedAddress.collectAsState()
    val is10MinGlobal by viewModel.isTenMinuteDelivery.collectAsState()
    val selectedPartnerGlobal by viewModel.selectedDeliveryPartner.collectAsState()
    val indianPresetAddresses = viewModel.indianPresetAddresses
    val context = LocalContext.current
    val isLocationConfirmed by viewModel.isLocationConfirmed.collectAsState()
    var address by remember(selectedAddressFromVm, isLocationConfirmed) {
        mutableStateOf(
            if (isLocationConfirmed && selectedAddressFromVm.flatAndBuilding.isNotBlank()) {
                selectedAddressFromVm.fullFormattedAddress
            } else {
                ""
            }
        )
    }
    var isTenMinuteDelivery by remember { mutableStateOf(is10MinGlobal) }
    var selectedCarrier by remember { mutableStateOf(selectedPartnerGlobal) }
    var selectedPaymentMethod by remember { mutableStateOf("UPI") }
    var selectedUpiApp by remember { mutableStateOf("Google Pay") }
    var cardNumber by remember { mutableStateOf("") }
    var cardExpiry by remember { mutableStateOf("") }
    var cardCvv by remember { mutableStateOf("") }
    var upiId by remember { mutableStateOf("") }

    var otpInput by remember { mutableStateOf("") }

    val discountAmount = subtotal * (promoDiscountPercent / 100.0)
    val discountedSubtotal = (subtotal - discountAmount).coerceAtLeast(0.0)
    val packagingFee = if (cartItems.isNotEmpty()) 0.18 else 0.0
    val taxGstAmount = discountedSubtotal * 0.05
    val freeShippingThreshold = 25.0
    val deliveryFee = if (discountedSubtotal >= freeShippingThreshold || cartItems.isEmpty()) 0.0 else 1.50
    val finalTotal = if (cartItems.isEmpty()) 0.0 else (discountedSubtotal + packagingFee + taxGstAmount + deliveryFee).coerceAtLeast(0.0)

    // Handle Order Placed successfully
    if (paymentState is PaymentState.Success) {
        val order = (paymentState as PaymentState.Success).order
        OrderSuccessView(
            order = order,
            formattedTotal = currency.format(order.totalAmountUsd),
            onViewTracking = {
                viewModel.resetPaymentState()
                onOrderPlaced(order.orderId)
            }
        )
        return
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 80.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 8.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onNavigateBack, modifier = Modifier.testTag("checkout_back_btn")) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
                Spacer(modifier = Modifier.width(6.dp))
                Column {
                    Text(
                        text = "Instant Checkout & 10-Min Delivery",
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = GreenSuccess,
                            modifier = Modifier.size(11.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "PCI-DSS Level 1 & 256-bit SSL Protected",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Shipping Address Card
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = null,
                                    tint = CoralPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Delivery Address",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }
                            TextButton(
                                onClick = onNavigateToLocationPicker,
                                modifier = Modifier.testTag("checkout_change_address_btn")
                            ) {
                                Text("Change on Map", color = CoralPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        // Quick Indian Address Presets
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            indianPresetAddresses.forEach { addr ->
                                val isSelected = address.contains(addr.pincode)
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isSelected) CoralPrimary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant,
                                    border = if (isSelected) androidx.compose.foundation.BorderStroke(1.dp, CoralPrimary) else null,
                                    modifier = Modifier.clickable {
                                        viewModel.selectAddress(addr)
                                        address = addr.fullFormattedAddress
                                    }
                                ) {
                                    Text(
                                        text = "${addr.label} (${addr.city})",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (isSelected) CoralPrimary else MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = address,
                            onValueChange = { address = it },
                            label = { Text("Complete Delivery Address *", fontSize = 12.sp) },
                            placeholder = { Text("Enter house/flat no., building name, street, locality", fontSize = 12.sp) },
                            singleLine = false,
                            maxLines = 3,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("address_input")
                        )

                        Spacer(modifier = Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Storefront,
                                contentDescription = null,
                                tint = SlateNavyDark,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Serving Darkstore: ${selectedAddressFromVm.nearestDarkstorePod} (${selectedAddressFromVm.podDistanceKm} km)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = CoralPrimary
                            )
                        }
                    }
                }

                // 10-Minute Dark Store & Logistics Partner Selection Card
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CoralPrimary.copy(alpha = 0.25f))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Bolt,
                                    contentDescription = null,
                                    tint = CoralPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Delivery Speed & Partner",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (isTenMinuteDelivery) CoralPrimary else SlateNavyDark
                            ) {
                                Text(
                                    text = if (isTenMinuteDelivery) "⚡ 10 MINS" else "1-2 DAYS",
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Delivery Speed Options
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isTenMinuteDelivery) CoralPrimary.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant,
                                border = if (isTenMinuteDelivery) androidx.compose.foundation.BorderStroke(1.5.dp, CoralPrimary) else null,
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        isTenMinuteDelivery = true
                                        selectedCarrier = "Shadowfax Express"
                                    }
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Bolt, contentDescription = null, tint = CoralPrimary, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("10-Min Flash", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = if (isTenMinuteDelivery) CoralPrimary else MaterialTheme.colorScheme.onSurface)
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text("From local darkstore pod", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (!isTenMinuteDelivery) SlateNavyDark.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant,
                                border = if (!isTenMinuteDelivery) androidx.compose.foundation.BorderStroke(1.5.dp, SlateNavyDark) else null,
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        isTenMinuteDelivery = false
                                        selectedCarrier = "Delhivery Direct"
                                    }
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.LocalShipping, contentDescription = null, tint = SlateNavyDark, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Standard 3PL", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = if (!isTenMinuteDelivery) SlateNavyDark else MaterialTheme.colorScheme.onSurface)
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text("Pan-India surface/air", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (isTenMinuteDelivery) "Select 10-Min Delivery Partner:" else "Select 3PL Carrier Partner:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        val partnersForMode = if (isTenMinuteDelivery) {
                            listOf("Shadowfax Express", "Zepto Quick Fleet", "Blinkit Fast Network")
                        } else {
                            listOf("Delhivery Direct", "BlueDart Prime", "Xpressbees Logistics")
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            partnersForMode.forEach { partner ->
                                val isPartnerSelected = selectedCarrier == partner
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isPartnerSelected) CoralPrimary.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                                    border = if (isPartnerSelected) androidx.compose.foundation.BorderStroke(1.dp, CoralPrimary) else null,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { selectedCarrier = partner }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            RadioButton(
                                                selected = isPartnerSelected,
                                                onClick = { selectedCarrier = partner },
                                                colors = RadioButtonDefaults.colors(selectedColor = CoralPrimary),
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = partner,
                                                fontWeight = FontWeight.SemiBold,
                                                fontSize = 12.sp,
                                                color = if (isPartnerSelected) CoralPrimary else MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                        Text(
                                            text = if (isTenMinuteDelivery) "⚡ 10 mins" else "1-2 days",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isTenMinuteDelivery) CoralPrimary else SlateNavyDark
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Payment Method Selector
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "Payment Gateway (${currency.code})",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Text(
                            text = "Real-time currency conversion applied to your transaction",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Option 1: Credit / Debit Card
                        PaymentMethodRow(
                            title = "Credit or Debit Card",
                            subtitle = "Visa, MasterCard, Amex • 3D-Secure",
                            icon = Icons.Default.CreditCard,
                            isSelected = selectedPaymentMethod == "CREDIT_CARD",
                            onSelect = { selectedPaymentMethod = "CREDIT_CARD" }
                        )

                        if (selectedPaymentMethod == "CREDIT_CARD") {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 28.dp, end = 4.dp, top = 8.dp, bottom = 8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = cardNumber,
                                    onValueChange = { cardNumber = it },
                                    label = { Text("Card Number") },
                                    placeholder = { Text("16-digit card number") },
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth().testTag("card_number_input")
                                )
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedTextField(
                                        value = cardExpiry,
                                        onValueChange = { cardExpiry = it },
                                        label = { Text("MM/YY") },
                                        placeholder = { Text("MM/YY") },
                                        singleLine = true,
                                        modifier = Modifier.weight(1f).testTag("card_expiry_input")
                                    )
                                    OutlinedTextField(
                                        value = cardCvv,
                                        onValueChange = { cardCvv = it },
                                        label = { Text("CVV") },
                                        placeholder = { Text("3 digits") },
                                        singleLine = true,
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                        modifier = Modifier.weight(1f).testTag("card_cvv_input")
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        // Option 2: Instant UPI
                        PaymentMethodRow(
                            title = "Instant UPI (PhonePe / GPay / Paytm)",
                            subtitle = "Zero transaction fees • Instant verification",
                            icon = Icons.Default.AccountBalance,
                            isSelected = selectedPaymentMethod == "UPI",
                            onSelect = { selectedPaymentMethod = "UPI" }
                        )

                        if (selectedPaymentMethod == "UPI") {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 28.dp, end = 4.dp, top = 8.dp, bottom = 8.dp)
                            ) {
                                Text("Popular UPI Handles in India:", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = SlateNavyDark)
                                Spacer(modifier = Modifier.height(6.dp))

                                val upiApps = listOf(
                                    "Google Pay" to "@okhdfcbank",
                                    "PhonePe" to "@ybl",
                                    "Paytm" to "@paytm",
                                    "Cred UPI" to "@axisbank",
                                    "BHIM" to "@upi"
                                )

                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                                    upiApps.take(3).forEach { (appName, suffix) ->
                                        val isAppSelected = selectedUpiApp == appName
                                        Surface(
                                            shape = RoundedCornerShape(12.dp),
                                            color = if (isAppSelected) CoralPrimary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant,
                                            border = if (isAppSelected) androidx.compose.foundation.BorderStroke(1.dp, CoralPrimary) else null,
                                            modifier = Modifier.clickable {
                                                selectedUpiApp = appName
                                                val prefix = if (upiId.contains("@")) upiId.substringBefore("@") else upiId
                                                upiId = if (prefix.isNotBlank()) "$prefix$suffix" else ""
                                            }
                                        ) {
                                            Text(
                                                text = appName,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isAppSelected) CoralPrimary else MaterialTheme.colorScheme.onSurface,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                OutlinedTextField(
                                    value = upiId,
                                    onValueChange = { upiId = it },
                                    label = { Text("UPI ID / VPA") },
                                    placeholder = { Text("e.g. mobile@upi or username@bank") },
                                    singleLine = true,
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth().testTag("upi_input")
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        // Option 3: Digital Wallets
                        PaymentMethodRow(
                            title = "Digital Wallets",
                            subtitle = "Apple Pay, Google Wallet, PayPal",
                            icon = Icons.Default.Payments,
                            isSelected = selectedPaymentMethod == "WALLET",
                            onSelect = { selectedPaymentMethod = "WALLET" }
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        // Option 4: Crypto
                        PaymentMethodRow(
                            title = "Crypto Pay (Web3)",
                            subtitle = "USDC, Bitcoin, Ethereum instant settlement",
                            icon = Icons.Default.CurrencyBitcoin,
                            isSelected = selectedPaymentMethod == "CRYPTO",
                            onSelect = { selectedPaymentMethod = "CRYPTO" }
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        // Option 5: Cash on Delivery
                        PaymentMethodRow(
                            title = "Cash on Delivery",
                            subtitle = "Pay upon doorstep arrival",
                            icon = Icons.Default.DeliveryDining,
                            isSelected = selectedPaymentMethod == "COD",
                            onSelect = { selectedPaymentMethod = "COD" }
                        )
                    }
                }

                // Summary Card
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "Itemized Bill Breakdown",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Items Subtotal", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(currency.format(subtotal), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                        if (discountAmount > 0.0) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Coupon Discount ($promoDiscountPercent%)", fontSize = 12.sp, color = GreenSuccess)
                                Text("- ${currency.format(discountAmount)}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = GreenSuccess)
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(if (isTenMinuteDelivery) "10-Min Flash Delivery" else "Standard Delivery", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(if (deliveryFee == 0.0) "FREE" else currency.format(deliveryFee), fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = if (deliveryFee == 0.0) GreenSuccess else MaterialTheme.colorScheme.onSurface)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Eco Packaging & Bagging", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(currency.format(packagingFee), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Taxes & GST (5%)", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(currency.format(taxGstAmount), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Total Amount (${currency.code})", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                Text("Inclusive of all taxes & packaging", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Text(
                                text = currency.format(finalTotal),
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 18.sp,
                                color = CoralPrimary
                            )
                        }
                    }
                }
            }
        }

        // Bottom Pay Button
        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 14.dp
        ) {
            Button(
                onClick = {
                    if (address.isBlank()) {
                        Toast.makeText(context, "Please enter your delivery address before checkout", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    val methodLabel = when (selectedPaymentMethod) {
                        "CREDIT_CARD" -> "Visa •••• ${cardNumber.takeLast(4)}"
                        "UPI" -> "UPI ($upiId)"
                        "WALLET" -> "Digital Wallet"
                        "CRYPTO" -> "Crypto USDC"
                        else -> "Cash on Delivery"
                    }
                    val requires3ds = selectedPaymentMethod == "CREDIT_CARD"
                    viewModel.initiateCheckout(methodLabel, address, requires3ds)
                },
                enabled = paymentState !is PaymentState.Processing,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = CoralPrimary),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .height(52.dp)
                    .testTag("pay_now_btn")
            ) {
                if (paymentState is PaymentState.Processing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White,
                        strokeWidth = 2.5.dp
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Securing 256-bit Handshake...", fontWeight = FontWeight.Bold)
                } else {
                    Icon(imageVector = Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Pay ${currency.format(finalTotal)} Securely", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
            }
        }

        // 3D Secure / OTP Simulation Dialog
        if (paymentState is PaymentState.OtpRequired) {
            val otpState = paymentState as PaymentState.OtpRequired
            AlertDialog(
                onDismissRequest = { viewModel.resetPaymentState() },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Shield, contentDescription = null, tint = CoralPrimary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("3D Secure 2.0 Verification", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                },
                text = {
                    Column {
                        Text(
                            text = "To authorize payment of ${currency.format(finalTotal)}, please enter the authentication code sent by your card issuer to your registered device.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = AmberSecondary.copy(alpha = 0.2f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Demo OTP Code: ${otpState.otpCode}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = SlateNavyDark,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = otpInput,
                            onValueChange = { otpInput = it },
                            label = { Text("Enter 4-digit OTP") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth().testTag("3ds_otp_input")
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.confirm3dsOtp(otpInput, otpState.maskedCardOrUpi, address)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CoralPrimary),
                        modifier = Modifier.testTag("confirm_otp_btn")
                    ) {
                        Text("Authorize Payment")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.resetPaymentState() }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
private fun PaymentMethodRow(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onSelect)
            .padding(vertical = 8.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = isSelected,
            onClick = onSelect,
            colors = RadioButtonDefaults.colors(selectedColor = CoralPrimary)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (isSelected) CoralPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(
                text = title,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun OrderSuccessView(
    order: OrderEntity,
    formattedTotal: String,
    onViewTracking: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .background(GreenSuccess.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Success",
                    tint = GreenSuccess,
                    modifier = Modifier.size(54.dp)
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = "Payment Verified & Order Placed!",
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Order ID: ${order.orderId}",
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = CoralPrimary
            )

            Spacer(modifier = Modifier.height(20.dp))

            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Amount Paid", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(formattedTotal, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Payment Method", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(order.paymentMethod, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("3PL Carrier Partner", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(order.carrierName, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = SlateNavyDark)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Tracking Number", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(order.trackingNumber, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = CoralPrimary)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Estimated Delivery", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(order.estimatedDelivery, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onViewTracking,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = CoralPrimary),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("track_order_success_btn")
            ) {
                Icon(imageVector = Icons.Default.LocalShipping, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Track Live 3PL Logistics", fontWeight = FontWeight.Bold)
            }
        }
    }
}
