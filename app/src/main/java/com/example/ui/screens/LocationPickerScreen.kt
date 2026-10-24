package com.example.ui.screens

import android.content.Intent
import android.graphics.Paint
import android.graphics.Typeface
import android.net.Uri
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.example.ui.components.OmniMapDisplayMode
import com.example.ui.components.OmniUniversalMapView
import com.example.ui.components.openInGoogleMaps
import com.example.ui.components.rememberOmniMapController
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EditLocation
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.IndianAddress
import com.example.ui.theme.AmberSecondary
import com.example.ui.theme.CoralPrimary
import com.example.ui.theme.GreenSuccess
import com.example.ui.theme.SlateNavyDark
import com.example.ui.viewmodel.EcommerceViewModel
import kotlin.math.roundToInt

enum class LocationPickerStep {
    INITIAL_PROMPT,
    MAP_CONFIRMATION,
    FINAL_CONFIRM_CARD
}

data class PresetCityArea(
    val cityName: String,
    val localityName: String,
    val defaultBuilding: String = "",
    val landmark: String,
    val state: String,
    val pincode: String,
    val darkstorePod: String,
    val initialPanX: Float,
    val initialPanY: Float,
    val latitude: Double = 22.5855,
    val longitude: Double = 88.4144,
    val podLat: Double = 22.5890,
    val podLng: Double = 88.4110
)

val presetLocations = listOf(
    PresetCityArea(
        cityName = "Kolkata",
        localityName = "Salt Lake, Sector 2",
        defaultBuilding = "",
        landmark = "Near City Centre Mall",
        state = "West Bengal",
        pincode = "700091",
        darkstorePod = "OmniCart Salt Lake Pod #02",
        initialPanX = 0f,
        initialPanY = 0f,
        latitude = 22.5855,
        longitude = 88.4144,
        podLat = 22.5890,
        podLng = 88.4110
    ),
    PresetCityArea(
        cityName = "Bengaluru",
        localityName = "Indiranagar, 100ft Road",
        defaultBuilding = "",
        landmark = "Behind Metro Station Gate 1",
        state = "Karnataka",
        pincode = "560038",
        darkstorePod = "OmniCart Indiranagar Pod #04",
        initialPanX = -120f,
        initialPanY = -80f,
        latitude = 12.9784,
        longitude = 77.6408,
        podLat = 12.9815,
        podLng = 77.6375
    ),
    PresetCityArea(
        cityName = "Bengaluru",
        localityName = "Koramangala, 4th Block",
        defaultBuilding = "",
        landmark = "Opposite Sony World Junction",
        state = "Karnataka",
        pincode = "560034",
        darkstorePod = "OmniCart Koramangala Pod #08",
        initialPanX = 140f,
        initialPanY = 90f,
        latitude = 12.9352,
        longitude = 77.6245,
        podLat = 12.9380,
        podLng = 77.6210
    ),
    PresetCityArea(
        cityName = "Mumbai",
        localityName = "Bandra West, Hill Road",
        defaultBuilding = "",
        landmark = "Near Holy Family Hospital",
        state = "Maharashtra",
        pincode = "400050",
        darkstorePod = "OmniCart Bandra Pod #03",
        initialPanX = -90f,
        initialPanY = 110f,
        latitude = 19.0596,
        longitude = 72.8295,
        podLat = 19.0630,
        podLng = 72.8330
    ),
    PresetCityArea(
        cityName = "Gurugram",
        localityName = "DLF CyberCity, Phase 2",
        defaultBuilding = "",
        landmark = "Next to Rapid Metro",
        state = "Haryana",
        pincode = "122002",
        darkstorePod = "OmniCart CyberCity Pod #11",
        initialPanX = 70f,
        initialPanY = -130f,
        latitude = 28.4950,
        longitude = 77.0895,
        podLat = 28.4980,
        podLng = 77.0860
    )
)

@Composable
fun LocationPickerScreen(
    viewModel: EcommerceViewModel,
    onLocationConfirmed: () -> Unit,
    onNavigateBack: (() -> Unit)? = null
) {
    val context = LocalContext.current
    var currentStep by remember { mutableStateOf(LocationPickerStep.INITIAL_PROMPT) }
    val savedAddresses by viewModel.userAddresses.collectAsState()
    val activeAddress by viewModel.selectedAddress.collectAsState()

    var activePreset by remember { mutableStateOf(presetLocations.first()) }
    var currentLatitude by remember { mutableDoubleStateOf(activePreset.latitude) }
    var currentLongitude by remember { mutableDoubleStateOf(activePreset.longitude) }
    var selectedLabel by remember { mutableStateOf("Home") }
    var flatAndBuilding by remember { mutableStateOf("") }
    var floorNumber by remember { mutableStateOf("") }
    var streetLocality by remember { mutableStateOf(activePreset.localityName) }
    var landmark by remember { mutableStateOf("") }
    var city by remember { mutableStateOf(activePreset.cityName) }
    var state by remember { mutableStateOf(activePreset.state) }
    var pincode by remember { mutableStateOf(activePreset.pincode) }
    var selectedInstruction by remember { mutableStateOf("Leave at door") }

    // Map Pan and Zoom State
    var panOffsetX by remember { mutableFloatStateOf(0f) }
    var panOffsetY by remember { mutableFloatStateOf(0f) }
    var zoomScale by remember { mutableFloatStateOf(1.0f) }
    var isLocatingGps by remember { mutableStateOf(false) }
    var searchAreaText by remember { mutableStateOf("") }

    val instructionsList = listOf("Leave at door", "Do not ring bell", "Avoid calling", "Leave with guard")
    val floorList = listOf("Ground", "1st", "2nd", "3rd", "4th", "5th+")

    // Synchronize address based on map pan
    fun updateAddressFromPan(dx: Float, dy: Float) {
        val nearestPreset = presetLocations.minByOrNull {
            val dist = (panOffsetX - it.initialPanX) * (panOffsetX - it.initialPanX) +
                    (panOffsetY - it.initialPanY) * (panOffsetY - it.initialPanY)
            dist
        } ?: presetLocations.first()

        streetLocality = if (panOffsetX > 60f) {
            "${nearestPreset.localityName} (Near Park View)"
        } else if (panOffsetX < -60f) {
            "${nearestPreset.localityName} (Avenue Cross)"
        } else {
            nearestPreset.localityName
        }
        city = nearestPreset.cityName
        state = nearestPreset.state
        pincode = nearestPreset.pincode
        if (landmark.isBlank()) {
            landmark = nearestPreset.landmark
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        when (currentStep) {
            LocationPickerStep.INITIAL_PROMPT -> {
                InitialLocationPrompt(
                    savedAddresses = savedAddresses,
                    presetList = presetLocations,
                    isLocatingGps = isLocatingGps,
                    onUseCurrentLocation = {
                        isLocatingGps = true
                        panOffsetX = 0f
                        panOffsetY = 0f
                        zoomScale = 1.15f
                        activePreset = presetLocations.first()
                        streetLocality = activePreset.localityName
                        city = activePreset.cityName
                        state = activePreset.state
                        pincode = activePreset.pincode
                        currentLatitude = activePreset.latitude
                        currentLongitude = activePreset.longitude
                        currentStep = LocationPickerStep.MAP_CONFIRMATION
                        isLocatingGps = false
                    },
                    onSelectPresetArea = { preset ->
                        activePreset = preset
                        currentLatitude = preset.latitude
                        currentLongitude = preset.longitude
                        panOffsetX = preset.initialPanX
                        panOffsetY = preset.initialPanY
                        streetLocality = preset.localityName
                        city = preset.cityName
                        state = preset.state
                        pincode = preset.pincode
                        currentStep = LocationPickerStep.MAP_CONFIRMATION
                    },
                    onEnterManually = {
                        currentStep = LocationPickerStep.MAP_CONFIRMATION
                    },
                    onSelectSavedAddress = { addr ->
                        viewModel.selectAddress(addr)
                        viewModel.setLocationConfirmed(true)
                        onLocationConfirmed()
                    },
                    onNavigateBack = onNavigateBack,
                    searchText = searchAreaText,
                    onSearchTextChange = { searchAreaText = it }
                )
            }

            LocationPickerStep.MAP_CONFIRMATION -> {
                InteractiveMapView(
                    panOffsetX = panOffsetX,
                    panOffsetY = panOffsetY,
                    zoomScale = zoomScale,
                    activePreset = activePreset,
                    currentLatitude = currentLatitude,
                    currentLongitude = currentLongitude,
                    onLocationMoved = { lat, lng ->
                        currentLatitude = lat
                        currentLongitude = lng
                        val deltaLat = lat - activePreset.latitude
                        val deltaLng = lng - activePreset.longitude
                        streetLocality = if (kotlin.math.abs(deltaLat) > 0.0005 || kotlin.math.abs(deltaLng) > 0.0005) {
                            "${activePreset.localityName} (${String.format(java.util.Locale.US, "%.4f", lat)}, ${String.format(java.util.Locale.US, "%.4f", lng)})"
                        } else {
                            activePreset.localityName
                        }
                    },
                    streetLocality = streetLocality,
                    flatAndBuilding = flatAndBuilding,
                    onFlatChange = { flatAndBuilding = it },
                    floorNumber = floorNumber,
                    onFloorChange = { floorNumber = it },
                    landmark = landmark,
                    onLandmarkChange = { landmark = it },
                    selectedLabel = selectedLabel,
                    onLabelChange = { selectedLabel = it },
                    selectedInstruction = selectedInstruction,
                    onInstructionChange = { selectedInstruction = it },
                    floorList = floorList,
                    instructionsList = instructionsList,
                    onMapDragged = { dx, dy ->
                        panOffsetX += dx
                        panOffsetY += dy
                        updateAddressFromPan(panOffsetX, panOffsetY)
                    },
                    onZoomIn = { if (zoomScale < 2.2f) zoomScale += 0.2f },
                    onZoomOut = { if (zoomScale > 0.8f) zoomScale -= 0.2f },
                    onRecenterGps = {
                        panOffsetX = 0f
                        panOffsetY = 0f
                        zoomScale = 1.0f
                        currentLatitude = activePreset.latitude
                        currentLongitude = activePreset.longitude
                        updateAddressFromPan(0f, 0f)
                    },
                    onSelectPresetChip = { preset ->
                        activePreset = preset
                        currentLatitude = preset.latitude
                        currentLongitude = preset.longitude
                        panOffsetX = preset.initialPanX
                        panOffsetY = preset.initialPanY
                        streetLocality = preset.localityName
                        city = preset.cityName
                        state = preset.state
                        pincode = preset.pincode
                    },
                    onConfirmClick = {
                        if (flatAndBuilding.isBlank()) {
                            Toast.makeText(context, "Please enter your flat or building details", Toast.LENGTH_SHORT).show()
                        } else {
                            currentStep = LocationPickerStep.FINAL_CONFIRM_CARD
                        }
                    },
                    onBackClick = {
                        currentStep = LocationPickerStep.INITIAL_PROMPT
                    }
                )
            }

            LocationPickerStep.FINAL_CONFIRM_CARD -> {
                FinalAddressConfirmationCard(
                    label = selectedLabel,
                    flatAndBuilding = flatAndBuilding.ifBlank { streetLocality },
                    floor = floorNumber,
                    streetLocality = streetLocality,
                    landmark = landmark,
                    city = city,
                    state = state,
                    pincode = pincode,
                    instruction = selectedInstruction,
                    onChangeLocation = {
                        currentStep = LocationPickerStep.MAP_CONFIRMATION
                    },
                    onConfirmAndContinue = {
                        viewModel.confirmMapLocation(
                            label = selectedLabel,
                            flatAndBuilding = flatAndBuilding.ifBlank { streetLocality },
                            floor = floorNumber,
                            streetAndLocality = streetLocality,
                            landmark = landmark,
                            city = city,
                            state = state,
                            pincode = pincode,
                            deliveryInstructions = selectedInstruction,
                            latitude = currentLatitude,
                            longitude = currentLongitude,
                            accuracyText = "High (±4m)"
                        )
                        onLocationConfirmed()
                    }
                )
            }
        }
    }
}

@Composable
private fun InitialLocationPrompt(
    savedAddresses: List<IndianAddress>,
    presetList: List<PresetCityArea>,
    isLocatingGps: Boolean,
    onUseCurrentLocation: () -> Unit,
    onSelectPresetArea: (PresetCityArea) -> Unit,
    onEnterManually: () -> Unit,
    onSelectSavedAddress: (IndianAddress) -> Unit,
    onNavigateBack: (() -> Unit)?,
    searchText: String,
    onSearchTextChange: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(28.dp))

        if (onNavigateBack != null) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
                Text(
                    text = "Delivery Location",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Hero Graphic & Icon
        Box(
            modifier = Modifier
                .size(76.dp)
                .background(CoralPrimary.copy(alpha = 0.12f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Filled.LocationOn,
                contentDescription = null,
                tint = CoralPrimary,
                modifier = Modifier.size(42.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Where should we deliver?",
            fontSize = 24.sp,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Ultra-fast 10–12 minute flash delivery from your local darkstore pod.",
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Search Input
        OutlinedTextField(
            value = searchText,
            onValueChange = onSearchTextChange,
            placeholder = { Text("Search area, apartment, street or landmark...") },
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null, tint = CoralPrimary) },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("search_delivery_area_input"),
            shape = RoundedCornerShape(10.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = CoralPrimary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Primary: Use My Current Location
        Button(
            onClick = onUseCurrentLocation,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .testTag("use_current_location_button"),
            colors = ButtonDefaults.buttonColors(containerColor = CoralPrimary),
            shape = RoundedCornerShape(10.dp)
        ) {
            if (isLocatingGps) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                Spacer(modifier = Modifier.width(10.dp))
                Text("Detecting high-precision GPS...", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            } else {
                Icon(Icons.Filled.MyLocation, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Use my current location",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Secondary: Set Location on Map Manually
        OutlinedButton(
            onClick = onEnterManually,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .testTag("enter_address_manually_button"),
            shape = RoundedCornerShape(10.dp),
            border = androidx.compose.foundation.BorderStroke(1.5.dp, CoralPrimary)
        ) {
            Icon(Icons.Filled.EditLocation, contentDescription = null, tint = CoralPrimary, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "Set location on map manually",
                fontWeight = FontWeight.SemiBold,
                color = CoralPrimary,
                fontSize = 14.sp
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Popular Delivery Hubs / Localities
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "POPULAR DELIVERY HUBS",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 1.sp
            )
        }
        Spacer(modifier = Modifier.height(10.dp))

        Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            val filteredPresets = if (searchText.isBlank()) presetList else presetList.filter {
                it.cityName.contains(searchText, ignoreCase = true) ||
                        it.localityName.contains(searchText, ignoreCase = true)
            }

            filteredPresets.forEach { preset ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelectPresetArea(preset) }
                        .testTag("preset_locality_${preset.cityName}"),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .background(AmberSecondary.copy(alpha = 0.15f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Filled.Explore, contentDescription = null, tint = AmberSecondary, modifier = Modifier.size(18.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "${preset.localityName}, ${preset.cityName}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            Text(
                                text = "${preset.darkstorePod} • ⚡ 10 min ETA",
                                fontSize = 11.sp,
                                color = GreenSuccess,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Icon(Icons.Filled.LocationOn, contentDescription = null, tint = CoralPrimary, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Saved Addresses Section
        if (savedAddresses.isNotEmpty()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "SAVED ADDRESSES",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 1.sp
                )
            }
            Spacer(modifier = Modifier.height(10.dp))

            savedAddresses.forEach { addr ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clickable { onSelectSavedAddress(addr) }
                        .testTag("saved_address_item_${addr.id}"),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .background(CoralPrimary.copy(alpha = 0.12f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                if (addr.label == "Home") Icons.Filled.Home else Icons.Filled.Work,
                                contentDescription = null,
                                tint = CoralPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = addr.label,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = GreenSuccess.copy(alpha = 0.12f)
                                ) {
                                    Text(
                                        text = "⚡ 10 min",
                                        color = GreenSuccess,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Text(
                                text = addr.fullFormattedAddress,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }
}


@Composable
private fun InteractiveMapView(
    panOffsetX: Float,
    panOffsetY: Float,
    zoomScale: Float,
    activePreset: PresetCityArea,
    currentLatitude: Double,
    currentLongitude: Double,
    onLocationMoved: (Double, Double) -> Unit,
    streetLocality: String,
    flatAndBuilding: String,
    onFlatChange: (String) -> Unit,
    floorNumber: String,
    onFloorChange: (String) -> Unit,
    landmark: String,
    onLandmarkChange: (String) -> Unit,
    selectedLabel: String,
    onLabelChange: (String) -> Unit,
    selectedInstruction: String,
    onInstructionChange: (String) -> Unit,
    floorList: List<String>,
    instructionsList: List<String>,
    onMapDragged: (Float, Float) -> Unit,
    onZoomIn: () -> Unit,
    onZoomOut: () -> Unit,
    onRecenterGps: () -> Unit,
    onSelectPresetChip: (PresetCityArea) -> Unit,
    onConfirmClick: () -> Unit,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    var isSatelliteView by remember { mutableStateOf(false) }
    val mapController = rememberOmniMapController()

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(currentLatitude, currentLongitude), 16.5f)
    }

    LaunchedEffect(currentLatitude, currentLongitude) {
        try {
            cameraPositionState.animate(
                CameraUpdateFactory.newLatLngZoom(
                    LatLng(currentLatitude, currentLongitude),
                    16.5f
                )
            )
        } catch (_: Exception) {}
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // TOP 50% INTERACTIVE MAP CANVAS
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1.05f)
                .background(Color(0xFFF3F5F3))
                .testTag("interactive_delivery_map")
        ) {
            // Real Live Embedded Map with Street, Satellite and Google SDK modes
            OmniUniversalMapView(
                modifier = Modifier.fillMaxSize().testTag("universal_delivery_map"),
                latitude = currentLatitude,
                longitude = currentLongitude,
                zoom = 16,
                podLat = activePreset.podLat,
                podLng = activePreset.podLng,
                podName = activePreset.darkstorePod,
                interactive = true,
                showControls = false,
                showExternalMapButton = false,
                showCenterPin = false,
                initialDisplayMode = if (isSatelliteView) OmniMapDisplayMode.SATELLITE else OmniMapDisplayMode.LIVE_STREETS,
                controller = mapController,
                onLocationSelected = { lat, lng ->
                    onLocationMoved(lat, lng)
                }
            )

            // Top Header: Back button, Accuracy Indicator & City Quick-Switch Chips
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surface,
                        shadowElevation = 4.dp
                    ) {
                        IconButton(onClick = onBackClick) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }

                    // Map Style Switcher: [ 🗺️ Streets | 🛰️ Satellite ]
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surface,
                        shadowElevation = 4.dp
                    ) {
                        Row(modifier = Modifier.padding(2.dp), verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (!isSatelliteView) CoralPrimary else Color.Transparent,
                                modifier = Modifier.clickable {
                                    isSatelliteView = false
                                    mapController.setDisplayMode?.invoke(OmniMapDisplayMode.LIVE_STREETS)
                                }
                            ) {
                                Text(
                                    text = "🗺️ Streets",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (!isSatelliteView) Color.White else MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (isSatelliteView) CoralPrimary else Color.Transparent,
                                modifier = Modifier.clickable {
                                    isSatelliteView = true
                                    mapController.setDisplayMode?.invoke(OmniMapDisplayMode.SATELLITE)
                                }
                            ) {
                                Text(
                                    text = "🛰️ Satellite",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSatelliteView) Color.White else MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                )
                            }
                        }
                    }

                    // Accuracy Indicator Badge
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surface,
                        shadowElevation = 4.dp
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(GreenSuccess, CircleShape)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "±4m",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = GreenSuccess
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Fast City Switcher Chips
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(presetLocations) { preset ->
                        val isSelected = activePreset.localityName == preset.localityName
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) CoralPrimary else MaterialTheme.colorScheme.surface,
                            shadowElevation = 2.dp,
                            modifier = Modifier.clickable {
                                onSelectPresetChip(preset)
                                mapController.recenter?.invoke(preset.latitude, preset.longitude)
                            }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = preset.localityName,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }

            // Fixed Screen-Center Delivery Pin (Moves map under it)
            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = SlateNavyDark,
                    shadowElevation = 8.dp
                ) {
                    Text(
                        text = "Order will be delivered here",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }
                Spacer(modifier = Modifier.height(3.dp))
                Icon(
                    Icons.Filled.LocationOn,
                    contentDescription = "Delivery Pin",
                    tint = CoralPrimary,
                    modifier = Modifier.size(52.dp)
                )
                // Pin ground shadow
                Box(
                    modifier = Modifier
                        .size(width = 18.dp, height = 5.dp)
                        .background(Color.Black.copy(alpha = 0.35f), CircleShape)
                )
            }

            // Floating Map Controls: Zoom (+/-) and Recenter GPS
            Column(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 4.dp,
                    modifier = Modifier
                        .size(40.dp)
                        .clickable {
                            onZoomIn()
                            mapController.zoomIn?.invoke()
                        }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Filled.Add, contentDescription = "Zoom In", tint = CoralPrimary)
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 4.dp,
                    modifier = Modifier
                        .size(40.dp)
                        .clickable {
                            onZoomOut()
                            mapController.zoomOut?.invoke()
                        }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Filled.Remove, contentDescription = "Zoom Out", tint = CoralPrimary)
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 6.dp,
                    modifier = Modifier
                        .size(44.dp)
                        .clickable {
                            onRecenterGps()
                            mapController.recenter?.invoke(activePreset.latitude, activePreset.longitude)
                        }
                        .testTag("map_recenter_button")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Filled.GpsFixed, contentDescription = "Recenter", tint = CoralPrimary)
                    }
                }
            }
        }

        // BOTTOM 50% ADDRESS ENTRY & CONFIRMATION PANEL
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1.15f),
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 16.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 14.dp)
            ) {
                // Drag handle
                Box(
                    modifier = Modifier
                        .size(width = 40.dp, height = 4.dp)
                        .background(MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(2.dp))
                        .align(Alignment.CenterHorizontally)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Detected Address and Locality
                Row(verticalAlignment = Alignment.Top) {
                    Icon(
                        Icons.Filled.LocationOn,
                        contentDescription = null,
                        tint = CoralPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = streetLocality,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Drag the map or tap city chips to position exact pin",
                            fontSize = 11.sp,
                            color = CoralPrimary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedButton(
                    onClick = {
                        try {
                            val encoded = java.net.URLEncoder.encode("$streetLocality, ${activePreset.cityName}", "UTF-8")
                            val mapIntent = Intent(Intent.ACTION_VIEW, Uri.parse("geo:${activePreset.latitude},${activePreset.longitude}?q=$encoded"))
                            context.startActivity(mapIntent)
                        } catch (e: Exception) {
                            try {
                                val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://maps.google.com/?q=${activePreset.latitude},${activePreset.longitude}"))
                                context.startActivity(webIntent)
                            } catch (err: Exception) {
                                Toast.makeText(context, "Maps app not found", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().testTag("open_location_in_google_maps_btn"),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = CoralPrimary)
                ) {
                    Icon(Icons.Filled.Navigation, contentDescription = null, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Open Pinpoint in Google Maps App", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(12.dp))

                // House / Flat / Building (Mandatory)
                OutlinedTextField(
                    value = flatAndBuilding,
                    onValueChange = onFlatChange,
                    label = { Text("House / Flat / Block / Building *") },
                    placeholder = { Text("Enter flat/house no. and building name") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_house_flat"),
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Floor selection chips
                Text(
                    text = "FLOOR",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(floorList) { flr ->
                        val isSelected = floorNumber == flr
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) CoralPrimary else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.clickable { onFloorChange(if (isSelected) "" else flr) }
                        ) {
                            Text(
                                text = flr,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Landmark input
                OutlinedTextField(
                    value = landmark,
                    onValueChange = onLandmarkChange,
                    label = { Text("Nearby Landmark (Optional)") },
                    placeholder = { Text("e.g. Near Metro Station / Main Gate") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_landmark"),
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Save As Chips (Home, Work, Other)
                Text(
                    text = "SAVE ADDRESS AS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Home", "Work", "Other").forEach { tag ->
                        val isSelected = selectedLabel == tag
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) CoralPrimary else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .clickable { onLabelChange(tag) }
                                .testTag("tag_address_$tag")
                        ) {
                            Text(
                                text = tag,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Delivery Instructions Chips
                Text(
                    text = "DELIVERY INSTRUCTIONS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(instructionsList) { inst ->
                        val isSelected = selectedInstruction == inst
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) AmberSecondary.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
                            border = if (isSelected) androidx.compose.foundation.BorderStroke(1.dp, AmberSecondary) else null,
                            modifier = Modifier.clickable { onInstructionChange(inst) }
                        ) {
                            Text(
                                text = inst,
                                color = if (isSelected) SlateNavyDark else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Primary Action Button: Confirm Location & Proceed
                Button(
                    onClick = onConfirmClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("confirm_delivery_location_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = CoralPrimary),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = "CONFIRM LOCATION & PROCEED ➔",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun FinalAddressConfirmationCard(
    label: String,
    flatAndBuilding: String,
    floor: String,
    streetLocality: String,
    landmark: String,
    city: String,
    state: String,
    pincode: String,
    instruction: String,
    onChangeLocation: () -> Unit,
    onConfirmAndContinue: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("deliver_here_confirmation_card"),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .background(GreenSuccess.copy(alpha = 0.12f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Filled.CheckCircle,
                        contentDescription = null,
                        tint = GreenSuccess,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "DELIVER HERE?",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = CoralPrimary.copy(alpha = 0.12f)
                ) {
                    Text(
                        text = label.uppercase(),
                        color = CoralPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = flatAndBuilding,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center
                )

                if (floor.isNotBlank()) {
                    Text(
                        text = "Floor: $floor",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Text(
                    text = streetLocality,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                if (landmark.isNotBlank()) {
                    Text(
                        text = "Landmark: Near $landmark",
                        fontSize = 12.sp,
                        color = AmberSecondary,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Text(
                    text = "$city, $state - $pincode",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(10.dp))

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = SlateNavyDark.copy(alpha = 0.06f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Instruction: $instruction",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = SlateNavyDark
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Primary: Confirm and Continue
                Button(
                    onClick = onConfirmAndContinue,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("confirm_and_continue_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = CoralPrimary),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = "CONFIRM & CONTINUE",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Secondary: Change Location
                TextButton(
                    onClick = onChangeLocation,
                    modifier = Modifier.testTag("change_location_button")
                ) {
                    Text(
                        text = "CHANGE LOCATION / ADJUST PIN",
                        color = CoralPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}
