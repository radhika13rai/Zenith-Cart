package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AmberSecondary
import com.example.ui.theme.CoralPrimary
import com.example.ui.theme.GreenSuccess
import com.example.ui.theme.SlateNavyDark
import com.example.ui.viewmodel.EcommerceViewModel

enum class CustomerAuthMode {
    REGISTER, // Dedicated Customer Registration
    LOGIN     // Existing Customer Sign-in
}

@Composable
fun LoginOtpScreen(
    viewModel: EcommerceViewModel,
    onLoginSuccess: () -> Unit,
    onSkipOrBack: () -> Unit
) {
    var authMode by remember { mutableStateOf(CustomerAuthMode.REGISTER) }

    // Registration Fields - Clean, user-driven empty initial states
    var regFullName by remember { mutableStateOf("") }
    var regEmail by remember { mutableStateOf("") }
    var regPhone by remember { mutableStateOf("") }
    var regCity by remember { mutableStateOf("Kolkata") }
    var regDietary by remember { mutableStateOf("All Groceries") }
    var regReferralCode by remember { mutableStateOf("") }
    var regPin by remember { mutableStateOf("") }
    var termsAccepted by remember { mutableStateOf(false) }

    // Login Fields - Clean, user-driven empty initial states
    var loginPhone by remember { mutableStateOf("") }
    var loginName by remember { mutableStateOf("") }
    var otpCode by remember { mutableStateOf("") }
    var isOtpSent by remember { mutableStateOf(false) }

    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isSuccess by remember { mutableStateOf(false) }
    var successTitle by remember { mutableStateOf("") }
    var successSubtitle by remember { mutableStateOf("") }

    val cartItems by viewModel.cartItems.collectAsState()
    val totalUnits = cartItems.sumOf { it.quantity }
    val userProfile by viewModel.userProfile.collectAsState()

    val availableCities = listOf("Kolkata", "Bengaluru", "Mumbai", "Delhi NCR", "Hyderabad")
    val dietaryOptions = listOf("All Groceries", "Vegetarian", "Vegan", "Organic")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        // Top Nav: Back and Skip
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onSkipOrBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            Spacer(modifier = Modifier.weight(1f))
            TextButton(onClick = onSkipOrBack) {
                Text(
                    "Browse as Guest",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Cart preserved notification badge
        if (totalUnits > 0) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = GreenSuccess.copy(alpha = 0.12f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Filled.ShoppingBag,
                        contentDescription = null,
                        tint = GreenSuccess,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Your bag ($totalUnits items) will be linked to your account.",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = GreenSuccess
                    )
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
        }

        if (isSuccess) {
            // SUCCESS CELEBRATION CARD
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("auth_success_card")
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(76.dp)
                            .background(GreenSuccess.copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Filled.CheckCircle,
                            contentDescription = null,
                            tint = GreenSuccess,
                            modifier = Modifier.size(44.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = successTitle,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = successSubtitle,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    // Member Details Badge
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = SlateNavyDark,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.Stars, contentDescription = null, tint = AmberSecondary, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "ZENITH GOLD MEMBERSHIP",
                                    color = AmberSecondary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    letterSpacing = 1.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Member ID: ${userProfile.id}",
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp
                            )
                            Text(
                                text = "Wallet Balance: ₹${userProfile.walletBalanceInr.toInt()}",
                                color = GreenSuccess,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = onLoginSuccess,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("auth_continue_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = CoralPrimary),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text("START SHOPPING NOW ⚡", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
            }
        } else {
            // AUTH TABS: [ 📝 Register (New Account) | 🔑 Sign In (Existing) ]
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(4.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (authMode == CustomerAuthMode.REGISTER) CoralPrimary else Color.Transparent,
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                authMode = CustomerAuthMode.REGISTER
                                errorMessage = null
                            }
                            .testTag("tab_register_customer")
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 10.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Filled.PersonAdd,
                                contentDescription = null,
                                tint = if (authMode == CustomerAuthMode.REGISTER) Color.White else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Register",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = if (authMode == CustomerAuthMode.REGISTER) Color.White else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (authMode == CustomerAuthMode.LOGIN) CoralPrimary else Color.Transparent,
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                authMode = CustomerAuthMode.LOGIN
                                errorMessage = null
                            }
                            .testTag("tab_login_customer")
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 10.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Filled.Lock,
                                contentDescription = null,
                                tint = if (authMode == CustomerAuthMode.LOGIN) Color.White else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Sign In",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = if (authMode == CustomerAuthMode.LOGIN) Color.White else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ==================== MODE 1: CUSTOMER REGISTRATION ====================
            if (authMode == CustomerAuthMode.REGISTER) {
                // Header Banner
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .background(CoralPrimary.copy(alpha = 0.12f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Filled.PersonAdd,
                            contentDescription = null,
                            tint = CoralPrimary,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text(
                            text = "Customer Registration",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "Create your account for 10-min flash delivery",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Welcome Bonus Banner
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = AmberSecondary.copy(alpha = 0.15f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, AmberSecondary.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.CardGiftcard, contentDescription = null, tint = AmberSecondary, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "₹100 Welcome Cash Bonus",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = SlateNavyDark
                            )
                            Text(
                                text = "Credited instantly to your Zenith Wallet upon registration.",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // 1. Full Name
                OutlinedTextField(
                    value = regFullName,
                    onValueChange = { regFullName = it },
                    label = { Text("Full Name *") },
                    placeholder = { Text("e.g. Alex Johnson") },
                    leadingIcon = { Icon(Icons.Filled.Person, contentDescription = null, tint = CoralPrimary) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("register_fullname_input"),
                    shape = RoundedCornerShape(14.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // 2. Email Address
                OutlinedTextField(
                    value = regEmail,
                    onValueChange = { regEmail = it },
                    label = { Text("Email Address *") },
                    placeholder = { Text("e.g. alex@example.com") },
                    leadingIcon = { Icon(Icons.Filled.Email, contentDescription = null, tint = CoralPrimary) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("register_email_input"),
                    shape = RoundedCornerShape(14.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // 3. Mobile Number
                OutlinedTextField(
                    value = regPhone,
                    onValueChange = { if (it.length <= 10) regPhone = it },
                    label = { Text("Mobile Number (for order OTP) *") },
                    placeholder = { Text("10-digit mobile number") },
                    prefix = { Text("+91 ", fontWeight = FontWeight.Bold, color = SlateNavyDark) },
                    leadingIcon = { Icon(Icons.Filled.Phone, contentDescription = null, tint = CoralPrimary) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("register_phone_input"),
                    shape = RoundedCornerShape(14.dp)
                )

                Spacer(modifier = Modifier.height(14.dp))

                // 4. City Selection
                Text(
                    text = "SELECT YOUR PRIMARY DELIVERY HUB",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 0.8.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    availableCities.take(3).forEach { city ->
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (regCity == city) CoralPrimary else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .weight(1f)
                                .clickable { regCity = city }
                                .testTag("city_chip_$city")
                        ) {
                            Text(
                                text = city,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (regCity == city) Color.White else MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    availableCities.drop(3).forEach { city ->
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (regCity == city) CoralPrimary else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .weight(1f)
                                .clickable { regCity = city }
                                .testTag("city_chip_$city")
                        ) {
                            Text(
                                text = city,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (regCity == city) Color.White else MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 5. Dietary Preference
                Text(
                    text = "SHOPPING & DIETARY PREFERENCE",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 0.8.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    dietaryOptions.forEach { opt ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (regDietary == opt) SlateNavyDark else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .weight(1f)
                                .clickable { regDietary = opt }
                        ) {
                            Text(
                                text = opt.split(" ").first(),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (regDietary == opt) Color.White else MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(vertical = 6.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 6. Referral Code & Security PIN
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = regReferralCode,
                        onValueChange = { regReferralCode = it },
                        label = { Text("Referral Code (Optional)") },
                        placeholder = { Text("e.g. WELCOME100") },
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("register_referral_input"),
                        shape = RoundedCornerShape(14.dp)
                    )

                    OutlinedTextField(
                        value = regPin,
                        onValueChange = { if (it.length <= 4) regPin = it },
                        label = { Text("4-Digit PIN *") },
                        placeholder = { Text("Set 4-digit PIN") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("register_pin_input"),
                        shape = RoundedCornerShape(14.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Terms agreement checkbox
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { termsAccepted = !termsAccepted }
                ) {
                    Checkbox(
                        checked = termsAccepted,
                        onCheckedChange = { termsAccepted = it },
                        colors = CheckboxDefaults.colors(checkedColor = CoralPrimary)
                    )
                    Text(
                        text = "I accept Zenith-Cart Terms of Service & Privacy Policy",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = errorMessage ?: "",
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Big Registration Submit CTA
                Button(
                    onClick = {
                        if (regFullName.isBlank()) {
                            errorMessage = "Please enter your Full Name."
                        } else if (!regEmail.contains("@") || !regEmail.contains(".")) {
                            errorMessage = "Please enter a valid email address."
                        } else if (regPhone.length < 10) {
                            errorMessage = "Please enter a 10-digit mobile number."
                        } else if (!termsAccepted) {
                            errorMessage = "Please accept the Terms of Service to continue."
                        } else {
                            val registered = viewModel.registerCustomer(
                                fullName = regFullName,
                                email = regEmail,
                                phone = regPhone,
                                city = regCity,
                                dietary = regDietary,
                                referralCode = regReferralCode
                            )
                            if (registered) {
                                successTitle = "Welcome to Zenith-Cart!"
                                successSubtitle = "Your customer profile is active with ₹100 welcome bonus."
                                isSuccess = true
                                errorMessage = null
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("register_submit_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = CoralPrimary),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Filled.PersonAdd, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("REGISTER & CLAIM ₹100 BONUS", fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
                }
            } else {
                // ==================== MODE 2: EXISTING USER LOGIN ====================
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .background(CoralPrimary.copy(alpha = 0.12f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            if (isOtpSent) Icons.Filled.Lock else Icons.Filled.Phone,
                            contentDescription = null,
                            tint = CoralPrimary,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text(
                            text = if (!isOtpSent) "Customer Sign In" else "Verify 4-Digit OTP",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = if (!isOtpSent) "Sign in with your registered phone number" else "Code sent to +91 $loginPhone",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                if (!isOtpSent) {
                    OutlinedTextField(
                        value = loginPhone,
                        onValueChange = { if (it.length <= 10) loginPhone = it },
                        label = { Text("Registered Mobile Number *") },
                        placeholder = { Text("10-digit mobile number") },
                        prefix = { Text("+91 ", fontWeight = FontWeight.Bold, color = SlateNavyDark) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("login_phone_input"),
                        shape = RoundedCornerShape(14.dp)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = loginName,
                        onValueChange = { loginName = it },
                        label = { Text("Display Name (Optional)") },
                        placeholder = { Text("e.g. Alex") },
                        leadingIcon = { Icon(Icons.Filled.Person, contentDescription = null, tint = CoralPrimary) },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("login_name_input"),
                        shape = RoundedCornerShape(14.dp)
                    )

                    if (errorMessage != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = errorMessage ?: "",
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(22.dp))

                    Button(
                        onClick = {
                            if (loginPhone.length == 10) {
                                isOtpSent = true
                                otpCode = ""
                                errorMessage = null
                            } else {
                                errorMessage = "Please enter a valid 10-digit mobile number."
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("send_otp_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = CoralPrimary),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text("GET VERIFICATION CODE", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                } else {
                    // OTP Verification
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Filled.Stars, contentDescription = null, tint = AmberSecondary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Verification code sent to +91 $loginPhone (Demo mode: enter any 4 digits)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = SlateNavyDark
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = otpCode,
                        onValueChange = { if (it.length <= 4) otpCode = it },
                        label = { Text("Enter 4-Digit OTP *") },
                        placeholder = { Text("4-digit code") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("otp_code_input"),
                        shape = RoundedCornerShape(14.dp)
                    )

                    if (errorMessage != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = errorMessage ?: "",
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = {
                            if (otpCode.length == 4) {
                                val loggedIn = viewModel.loginWithPhoneOtp(loginPhone, otpCode, loginName)
                                if (loggedIn) {
                                    val displayName = if (loginName.isNotBlank()) loginName.split(" ").first() else "Customer"
                                    successTitle = "Welcome back, $displayName!"
                                    successSubtitle = "Your delivery orders and saved addresses are synced."
                                    isSuccess = true
                                    errorMessage = null
                                } else {
                                    errorMessage = "Please enter a valid 4-digit OTP code."
                                }
                            } else {
                                errorMessage = "Please enter the 4-digit verification code."
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("verify_otp_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = CoralPrimary),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text("VERIFY & SIGN IN", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    TextButton(
                        onClick = { isOtpSent = false },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Change Mobile Number", color = CoralPrimary, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}
