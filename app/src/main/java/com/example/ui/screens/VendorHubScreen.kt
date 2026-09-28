package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Vendor
import com.example.ui.theme.AmberSecondary
import com.example.ui.theme.CoralPrimary
import com.example.ui.theme.GreenSuccess
import com.example.ui.theme.RedDanger
import com.example.ui.theme.SlateNavyDark
import com.example.ui.viewmodel.EcommerceViewModel

@Composable
fun VendorHubScreen(viewModel: EcommerceViewModel) {
    val vendors by viewModel.vendors.collectAsState()
    val products by viewModel.allProducts.collectAsState()
    val currency by viewModel.selectedCurrency.collectAsState()
    val context = LocalContext.current

    var showPaymentFaqDialog by remember { mutableStateOf(false) }
    var showRegisterVendorDialog by remember { mutableStateOf(false) }
    var vendorToEditPayout by remember { mutableStateOf<Vendor?>(null) }
    var vendorToEditDelivery by remember { mutableStateOf<Vendor?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.Store, contentDescription = null, tint = CoralPrimary)
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "Multi-Vendor Logistics & Payout Hub",
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Manage inventory, 3PL dispatch & automated settlements",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            IconButton(
                onClick = { showPaymentFaqDialog = true },
                modifier = Modifier.testTag("vendor_payment_faq_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.HelpOutline,
                    contentDescription = "Payout FAQ",
                    tint = CoralPrimary
                )
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Marketplace Gateway Notice Banner
            item {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = CoralPrimary.copy(alpha = 0.08f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CoralPrimary.copy(alpha = 0.35f)),
                    modifier = Modifier.fillMaxWidth().testTag("marketplace_gateway_banner")
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .background(CoralPrimary, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Payments,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Marketplace Split Payout Architecture",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 13.sp,
                                color = CoralPrimary
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "💡 Vendors DO NOT need to add or integrate their own payment gateways. OmniCart serves as the master payment processor (Instant UPI, Cards, NetBanking, COD). Payments are automatically split and settled directly to your linked Bank Account or UPI VPA.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            lineHeight = 17.sp
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Security, contentDescription = null, tint = GreenSuccess, modifier = Modifier.size(13.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Automated TDS 194-O & GST TCS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = GreenSuccess)
                            }

                            Text(
                                text = "How payouts work →",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = CoralPrimary,
                                modifier = Modifier
                                    .clickable { showPaymentFaqDialog = true }
                                    .padding(vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            // Summary banner
            item {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = SlateNavyDark)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Active Verified Vendors", color = Color(0xFFA6A9C0), fontSize = 11.sp)
                            Text("${vendors.size} Merchant Hubs", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                            Text("Direct Bank & UPI Auto-Settlements Enabled", color = AmberSecondary, fontSize = 10.sp)
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = CoralPrimary
                        ) {
                            Text(
                                text = "T+1 & Instant Flash",
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            // Onboard New Merchant Button
            item {
                Button(
                    onClick = { showRegisterVendorDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = CoralPrimary),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().testTag("onboard_vendor_btn")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("+ Register New Merchant / Darkstore Partner", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }

            // Vendor Portfolios
            items(vendors) { vendor ->
                val vendorProducts = products.filter { it.vendorId == vendor.id }

                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.testTag("vendor_card_${vendor.id}")
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
                                        .size(36.dp)
                                        .background(CoralPrimary.copy(alpha = 0.12f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Store, contentDescription = null, tint = CoralPrimary, modifier = Modifier.size(18.dp))
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = vendor.name,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp
                                        )
                                        if (vendor.verified) {
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Icon(Icons.Default.Verified, contentDescription = "Verified", tint = CoralPrimary, modifier = Modifier.size(14.dp))
                                        }
                                    }
                                    Text(
                                        text = "Hub: ${vendor.dispatchHub}",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = AmberSecondary.copy(alpha = 0.2f)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Icon(Icons.Default.Star, contentDescription = null, tint = AmberSecondary, modifier = Modifier.size(11.dp))
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text("${vendor.rating}", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // 3PL Partner & Logistics Info
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.LocalShipping, contentDescription = null, modifier = Modifier.size(14.dp), tint = SlateNavyDark)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Column {
                                        Text(
                                            text = "Logistics: ${vendor.carrierPartner}",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Text(
                                            text = "Dispatch SLA: ~${vendor.avgDispatchHours}h • Hub: ${vendor.dispatchHub}",
                                            fontSize = 10.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = CoralPrimary.copy(alpha = 0.12f),
                                    modifier = Modifier
                                        .clickable { vendorToEditDelivery = vendor }
                                        .testTag("configure_delivery_${vendor.id}")
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Icon(Icons.Default.Edit, contentDescription = null, tint = CoralPrimary, modifier = Modifier.size(11.dp))
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text("Configure", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = CoralPrimary)
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Vendor Payout & Settlement Card
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.AccountBalance, contentDescription = null, tint = CoralPrimary, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(5.dp))
                                        Text(
                                            text = "Settlement Account",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            color = SlateNavyDark
                                        )
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = GreenSuccess.copy(alpha = 0.15f)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                        ) {
                                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = GreenSuccess, modifier = Modifier.size(10.dp))
                                            Spacer(modifier = Modifier.width(3.dp))
                                            Text("Active", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = GreenSuccess)
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text(
                                            text = "${vendor.bankName} (${vendor.accountNumberMasked})",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "IFSC: ${vendor.ifscCode} • UPI: ${vendor.upiId}",
                                            fontSize = 10.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = vendor.settlementCycle,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = CoralPrimary
                                        )
                                        Text(
                                            text = "GSTIN: ${vendor.gstin.take(10)}...",
                                            fontSize = 9.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                OutlinedButton(
                                    onClick = { vendorToEditPayout = vendor },
                                    shape = RoundedCornerShape(6.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                    modifier = Modifier.fillMaxWidth().height(30.dp).testTag("payout_config_${vendor.id}")
                                ) {
                                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(12.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Configure Bank / UPI Settlement Details", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Live Inventory Management (${vendorProducts.size} SKUs)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        // Product Inventory Items & Restock Buttons
                        vendorProducts.forEach { prod ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = prod.title,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        maxLines = 1
                                    )
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "Stock: ${prod.stock} units",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (prod.stock <= 10) RedDanger else GreenSuccess
                                        )
                                        if (prod.stock <= 10) {
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Surface(
                                                shape = RoundedCornerShape(3.dp),
                                                color = RedDanger.copy(alpha = 0.15f)
                                            ) {
                                                Text(
                                                    text = "LOW",
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = RedDanger,
                                                    modifier = Modifier.padding(horizontal = 4.dp)
                                                )
                                            }
                                        }
                                    }
                                }

                                // Instant Restock Action (+10)
                                Button(
                                    onClick = { viewModel.restockProduct(prod.id, 10) },
                                    colors = ButtonDefaults.buttonColors(containerColor = CoralPrimary),
                                    shape = RoundedCornerShape(6.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                    modifier = Modifier.height(30.dp).testTag("restock_${prod.id}")
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(12.dp))
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text("Restock +10", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal: Payout Configuration Dialog
    vendorToEditPayout?.let { vendor ->
        var bankNameInput by remember { mutableStateOf(vendor.bankName) }
        var accountInput by remember { mutableStateOf(vendor.accountNumberMasked.replace("•••• ", "")) }
        var ifscInput by remember { mutableStateOf(vendor.ifscCode) }
        var upiInput by remember { mutableStateOf(vendor.upiId) }
        var selectedCycle by remember { mutableStateOf(vendor.settlementCycle) }

        val cycles = listOf(
            "Instant for 10-Min Flash / T+1 Daily",
            "Daily T+1 Automated Split",
            "Weekly Consolidated (Every Monday)"
        )

        AlertDialog(
            onDismissRequest = { vendorToEditPayout = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AccountBalance, contentDescription = null, tint = CoralPrimary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Settlement Settings: ${vendor.name}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Link your verified business account. OmniCart automatically splits and direct-deposits proceeds for every customer order. No separate gateway required.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    OutlinedTextField(
                        value = bankNameInput,
                        onValueChange = { bankNameInput = it },
                        label = { Text("Bank Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = accountInput,
                        onValueChange = { accountInput = it },
                        label = { Text("Bank Account Number") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = ifscInput,
                        onValueChange = { ifscInput = it },
                        label = { Text("IFSC Code") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = upiInput,
                        onValueChange = { upiInput = it },
                        label = { Text("Settlement UPI VPA") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text(
                        text = "Settlement Frequency",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    cycles.forEach { cycle ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (selectedCycle == cycle) CoralPrimary.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant,
                            border = if (selectedCycle == cycle) androidx.compose.foundation.BorderStroke(1.dp, CoralPrimary) else null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedCycle = cycle }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = cycle,
                                    fontSize = 11.sp,
                                    fontWeight = if (selectedCycle == cycle) FontWeight.Bold else FontWeight.Normal,
                                    color = if (selectedCycle == cycle) CoralPrimary else MaterialTheme.colorScheme.onSurface
                                )
                                if (selectedCycle == cycle) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = CoralPrimary, modifier = Modifier.size(14.dp))
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.updateVendorPayoutConfig(
                            vendorId = vendor.id,
                            bankName = bankNameInput.ifBlank { "HDFC Bank Ltd." },
                            accountNum = accountInput.ifBlank { "8421" },
                            ifscCode = ifscInput.ifBlank { "HDFC0001234" },
                            upiId = upiInput.ifBlank { "merchant@okhdfcbank" },
                            settlementCycle = selectedCycle
                        )
                        Toast.makeText(context, "Settlement profile updated for ${vendor.name}!", Toast.LENGTH_SHORT).show()
                        vendorToEditPayout = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CoralPrimary)
                ) {
                    Text("Save & Verify Account")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { vendorToEditPayout = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Modal: Delivery & 3PL Logistics Configuration Dialog
    vendorToEditDelivery?.let { vendor ->
        var selectedHub by remember { mutableStateOf(vendor.dispatchHub) }
        var selectedCarrier by remember { mutableStateOf(vendor.carrierPartner) }
        var selectedHours by remember { mutableStateOf(vendor.avgDispatchHours) }

        val hubOptions = listOf(
            "Koramangala Darkstore Pod #12, Bengaluru",
            "Indiranagar QuickPod #04, Bengaluru",
            "Lower Parel Logistics Center, Mumbai",
            "Bhiwandi E-Commerce Cargo Park, Thane",
            "CyberCity QuickPod #11, Gurugram"
        )

        val carrierOptions = listOf(
            "Shadowfax Express (10-Min Flash)" to 0,
            "Zepto Quick Fleet (10-Min Flash)" to 0,
            "Delhivery Direct Express" to 4,
            "BlueDart Prime Aviation" to 6,
            "Xpressbees Logistics" to 12
        )

        AlertDialog(
            onDismissRequest = { vendorToEditDelivery = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocalShipping, contentDescription = null, tint = CoralPrimary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Delivery & 3PL Setup: ${vendor.name}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Configure your fulfillment tier, micro-darkstore origin pod, and integrated 3PL courier fleet.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Text(
                        text = "1. Assigned 3PL Fleet & SLA Speed",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    carrierOptions.forEach { (carrier, hours) ->
                        val isSelected = selectedCarrier == carrier
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) CoralPrimary.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant,
                            border = if (isSelected) androidx.compose.foundation.BorderStroke(1.dp, CoralPrimary) else null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedCarrier = carrier
                                    selectedHours = hours
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(
                                        text = carrier,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) CoralPrimary else MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = if (hours == 0) "⚡ 10-Min Quick Commerce Pod Dispatch" else "📦 ~${hours}h Standard Handover SLA",
                                        fontSize = 9.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                if (isSelected) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = CoralPrimary, modifier = Modifier.size(14.dp))
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "2. Micro-Darkstore / Dispatch Origin Hub",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    hubOptions.forEach { hub ->
                        val isSelected = selectedHub == hub
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) CoralPrimary.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant,
                            border = if (isSelected) androidx.compose.foundation.BorderStroke(1.dp, CoralPrimary) else null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedHub = hub }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = hub,
                                    fontSize = 10.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) CoralPrimary else MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1
                                )
                                if (isSelected) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = CoralPrimary, modifier = Modifier.size(14.dp))
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.updateVendorDeliveryConfig(
                            vendorId = vendor.id,
                            dispatchHub = selectedHub,
                            carrierPartner = selectedCarrier,
                            avgDispatchHours = selectedHours
                        )
                        Toast.makeText(context, "Logistics & Delivery updated for ${vendor.name}!", Toast.LENGTH_SHORT).show()
                        vendorToEditDelivery = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CoralPrimary)
                ) {
                    Text("Save Delivery Configuration")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { vendorToEditDelivery = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Modal: Onboard New Merchant Dialog
    if (showRegisterVendorDialog) {
        var merchantName by remember { mutableStateOf("") }
        var gstinInput by remember { mutableStateOf("") }
        var panInput by remember { mutableStateOf("") }
        var hubInput by remember { mutableStateOf("") }
        var carrierInput by remember { mutableStateOf("") }
        var bankInput by remember { mutableStateOf("") }
        var accountInput by remember { mutableStateOf("") }
        var ifscInput by remember { mutableStateOf("") }
        var upiInput by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showRegisterVendorDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Store, contentDescription = null, tint = CoralPrimary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Onboard New Merchant", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Register brand for micro-fulfillment & automated UPI/Bank settlements:",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    OutlinedTextField(
                        value = merchantName,
                        onValueChange = { merchantName = it },
                        label = { Text("Brand / Store Name", fontSize = 11.sp) },
                        placeholder = { Text("e.g. Fresh Roots Organics", fontSize = 11.sp) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("onboard_vendor_name")
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        OutlinedTextField(
                            value = gstinInput,
                            onValueChange = { gstinInput = it.uppercase() },
                            label = { Text("GSTIN", fontSize = 11.sp) },
                            placeholder = { Text("15-char GSTIN", fontSize = 10.sp) },
                            singleLine = true,
                            modifier = Modifier.weight(1.3f).testTag("onboard_vendor_gstin")
                        )
                        OutlinedTextField(
                            value = panInput,
                            onValueChange = { panInput = it.uppercase() },
                            label = { Text("PAN", fontSize = 11.sp) },
                            placeholder = { Text("10-char PAN", fontSize = 10.sp) },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    OutlinedTextField(
                        value = hubInput,
                        onValueChange = { hubInput = it },
                        label = { Text("Assigned Darkstore Pod", fontSize = 11.sp) },
                        placeholder = { Text("e.g. Indiranagar Pod #04", fontSize = 11.sp) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = carrierInput,
                        onValueChange = { carrierInput = it },
                        label = { Text("3PL Dispatch Carrier", fontSize = 11.sp) },
                        placeholder = { Text("e.g. Shadowfax Express", fontSize = 11.sp) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        OutlinedTextField(
                            value = bankInput,
                            onValueChange = { bankInput = it },
                            label = { Text("Settlement Bank", fontSize = 11.sp) },
                            placeholder = { Text("Bank Name", fontSize = 10.sp) },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = upiInput,
                            onValueChange = { upiInput = it },
                            label = { Text("Business UPI VPA", fontSize = 11.sp) },
                            placeholder = { Text("merchant@upi", fontSize = 10.sp) },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = GreenSuccess.copy(alpha = 0.12f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Verified, contentDescription = null, tint = GreenSuccess, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "1% GST TCS & Sec 194-O TDS automated by OmniCart Master Gateway",
                                fontSize = 10.sp,
                                color = GreenSuccess,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (merchantName.isBlank()) {
                            Toast.makeText(context, "Please enter merchant name", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        viewModel.registerNewVendor(
                            name = merchantName,
                            gstin = gstinInput,
                            panNumber = panInput,
                            dispatchHub = hubInput,
                            carrierPartner = carrierInput,
                            bankName = bankInput,
                            accountNumberMasked = accountInput,
                            ifscCode = ifscInput,
                            upiId = upiInput
                        )
                        Toast.makeText(context, "Merchant registered & verified successfully!", Toast.LENGTH_SHORT).show()
                        showRegisterVendorDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CoralPrimary),
                    modifier = Modifier.testTag("submit_onboard_vendor")
                ) {
                    Text("Verify & Onboard")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showRegisterVendorDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Modal: How Vendor Payments Work FAQ Dialog
    if (showPaymentFaqDialog) {
        AlertDialog(
            onDismissRequest = { showPaymentFaqDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Payments, contentDescription = null, tint = CoralPrimary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Vendor Payments FAQ",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    FaqItem(
                        question = "Do vendors need their own payment gateway?",
                        answer = "NO. Vendors NEVER need to sign up for Stripe, Razorpay, or integrate payment APIs. OmniCart operates as the Master Marketplace Payment Processor."
                    )

                    FaqItem(
                        question = "How do customers pay?",
                        answer = "Customers pay directly on OmniCart using Instant UPI (Google Pay, PhonePe, Paytm, CRED), Credit/Debit Cards, NetBanking, and Cash on Delivery (COD)."
                    )

                    FaqItem(
                        question = "How do vendors receive their money?",
                        answer = "Upon delivery completion, OmniCart automatically calculates the net amount (sales minus platform commission) and auto-settles the funds into the vendor's linked Bank Account (IMPS/NEFT) or UPI VPA."
                    )

                    FaqItem(
                        question = "What is the payout timeline?",
                        answer = "• 10-Min Flash Pod Orders: Settled instantly or at end-of-day.\n• Standard 3PL Orders: Settled on a standard T+1 or T+2 rolling cycle."
                    )

                    FaqItem(
                        question = "Tax Compliance & Invoicing",
                        answer = "OmniCart automatically handles 1% GST TCS and Section 194-O Income Tax TDS withholding, and generates downloadable monthly payout statements."
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { showPaymentFaqDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = CoralPrimary)
                ) {
                    Text("Understood")
                }
            }
        )
    }
}

@Composable
private fun FaqItem(question: String, answer: String) {
    Column {
        Text(
            text = question,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            color = SlateNavyDark
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = answer,
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            lineHeight = 15.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
    }
}
