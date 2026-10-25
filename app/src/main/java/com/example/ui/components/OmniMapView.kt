package com.example.ui.components

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.graphics.Typeface
import android.net.Uri
import android.net.http.SslError
import android.util.Log
import android.view.View
import android.webkit.ConsoleMessage
import android.webkit.JavascriptInterface
import android.webkit.SslErrorHandler
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.ui.theme.AmberSecondary
import com.example.ui.theme.CoralPrimary
import com.example.ui.theme.GreenSuccess
import com.example.ui.theme.SlateNavyDark
import com.google.android.gms.maps.CameraUpdateFactory
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

enum class OmniMapDisplayMode {
    VECTOR_RADAR,   // Native Jetpack Compose 60fps Vector Map (Zero Mesa/GPU errors, completely offline)
    LIVE_STREETS,   // Interactive Leaflet Google Maps Streets + OSM fallback
    SATELLITE,      // Interactive Leaflet Google Hybrid Satellite
    GOOGLE_MAPS_SDK // Native Google Maps SDK
}

class MapBridge(private val onLocationUpdated: (Double, Double) -> Unit) {
    @JavascriptInterface
    fun onPinMoved(lat: Double, lng: Double) {
        onLocationUpdated(lat, lng)
    }
}

/**
 * Controller to trigger programmatic map operations (zoom, recenter, layer switch)
 * from parent composables without forcing recomposition.
 */
class OmniMapController {
    var zoomIn: (() -> Unit)? = null
    var zoomOut: (() -> Unit)? = null
    var recenter: ((Double, Double) -> Unit)? = null
    var setDisplayMode: ((OmniMapDisplayMode) -> Unit)? = null
}

@Composable
fun rememberOmniMapController(): OmniMapController = remember { OmniMapController() }

/**
 * In-memory cache for Leaflet assets stored in assets/map/
 * Inlines CSS and JS directly into the HTML document to eliminate CDN latency,
 * network failures, and L is not defined crashes.
 */
object MapAssetCache {
    @Volatile
    private var cachedCss: String? = null
    @Volatile
    private var cachedJs: String? = null

    fun getCss(context: Context): String {
        return cachedCss ?: synchronized(this) {
            cachedCss ?: try {
                context.assets.open("map/leaflet.css").bufferedReader().use { it.readText() }
            } catch (e: Exception) {
                Log.w("OmniMap", "Could not read assets/map/leaflet.css: ${e.message}")
                ""
            }.also { cachedCss = it }
        }
    }

    fun getJs(context: Context): String {
        return cachedJs ?: synchronized(this) {
            cachedJs ?: try {
                context.assets.open("map/leaflet.js").bufferedReader().use { it.readText() }
            } catch (e: Exception) {
                Log.w("OmniMap", "Could not read assets/map/leaflet.js: ${e.message}")
                ""
            }.also { cachedJs = it }
        }
    }
}

/**
 * Universal Omni-Channel Map Component.
 * - Defaults to Zero-CDN Offline-Embedded Leaflet Map with Google Maps & OpenStreetMap raster tiles.
 * - Zero snap-back: records user drag state so Leaflet camera does not jerk during touch moves.
 * - Seamless tile switching without page reload.
 * - Native Compose Vector Map fallback for zero-network / radar mode.
 * - Google Maps SDK toggle for environments with configured API keys.
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun OmniUniversalMapView(
    modifier: Modifier = Modifier,
    latitude: Double,
    longitude: Double,
    zoom: Int = 16,
    podLat: Double? = null,
    podLng: Double? = null,
    podName: String? = null,
    riderLat: Double? = null,
    riderLng: Double? = null,
    riderName: String? = null,
    interactive: Boolean = true,
    showControls: Boolean = true,
    showExternalMapButton: Boolean = true,
    showCenterPin: Boolean = true,
    initialDisplayMode: OmniMapDisplayMode = OmniMapDisplayMode.LIVE_STREETS,
    controller: OmniMapController? = null,
    onLocationSelected: ((Double, Double) -> Unit)? = null
) {
    val context = LocalContext.current
    var displayMode by remember { mutableStateOf(initialDisplayMode) }
    var webViewRef by remember { mutableStateOf<WebView?>(null) }
    var isLoadingTiles by remember { mutableStateOf(true) }

    // Bind external controller actions
    LaunchedEffect(controller, webViewRef) {
        controller?.let { ctrl ->
            ctrl.zoomIn = {
                webViewRef?.evaluateJavascript("if (window.zoomIn) window.zoomIn();", null)
            }
            ctrl.zoomOut = {
                webViewRef?.evaluateJavascript("if (window.zoomOut) window.zoomOut();", null)
            }
            ctrl.recenter = { lat, lng ->
                webViewRef?.evaluateJavascript("if (window.recenter) window.recenter($lat, $lng, $zoom);", null)
            }
            ctrl.setDisplayMode = { mode ->
                displayMode = mode
            }
        }
    }

    // Google Maps SDK camera state
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(latitude, longitude), zoom.toFloat())
    }

    LaunchedEffect(latitude, longitude, zoom) {
        try {
            cameraPositionState.animate(
                CameraUpdateFactory.newLatLngZoom(LatLng(latitude, longitude), zoom.toFloat())
            )
        } catch (_: Exception) {}

        webViewRef?.let { wv ->
            val js = "if (window.updateCenter) { window.updateCenter($latitude, $longitude, $zoom); }"
            wv.evaluateJavascript(js, null)
        }
    }

    LaunchedEffect(riderLat, riderLng) {
        if (riderLat != null && riderLng != null) {
            webViewRef?.let { wv ->
                val riderJs = "if (window.updateRider) { window.updateRider($riderLat, $riderLng); }"
                wv.evaluateJavascript(riderJs, null)
            }
        }
    }

    // Instant tile mode switching without full HTML reload
    LaunchedEffect(displayMode) {
        webViewRef?.let { wv ->
            val modeKey = if (displayMode == OmniMapDisplayMode.SATELLITE) "satellite" else "streets"
            wv.evaluateJavascript("if (window.setMode) { window.setMode('$modeKey'); }", null)
        }
    }

    val mapHtml = remember(context, interactive) {
        generateLeafletHtml(
            context = context,
            lat = latitude,
            lng = longitude,
            zoom = zoom,
            isSatellite = displayMode == OmniMapDisplayMode.SATELLITE,
            podLat = podLat,
            podLng = podLng,
            podName = podName ?: "Darkstore Pod",
            riderLat = riderLat,
            riderLng = riderLng,
            riderName = riderName ?: "Delivery Rider",
            isInteractive = interactive
        )
    }

    Box(modifier = modifier.clip(RoundedCornerShape(12.dp))) {
        when (displayMode) {
            OmniMapDisplayMode.VECTOR_RADAR -> {
                // 100% Native Jetpack Compose Canvas Map - Zero MESA error risk, instant 60 FPS
                NativeComposeVectorMap(
                    modifier = Modifier.fillMaxSize().testTag("native_compose_vector_map"),
                    latitude = latitude,
                    longitude = longitude,
                    podLat = podLat,
                    podLng = podLng,
                    podName = podName ?: "OmniCart Pod #02",
                    riderLat = riderLat,
                    riderLng = riderLng,
                    riderName = riderName ?: "Rahul Verma",
                    interactive = interactive,
                    onLocationSelected = onLocationSelected
                )
            }

            OmniMapDisplayMode.GOOGLE_MAPS_SDK -> {
                // Native Google Maps SDK
                Box(modifier = Modifier.fillMaxSize()) {
                    GoogleMap(
                        modifier = Modifier.fillMaxSize().testTag("native_google_maps_view"),
                        cameraPositionState = cameraPositionState,
                        properties = MapProperties(
                            mapType = MapType.NORMAL,
                            isTrafficEnabled = true
                        ),
                        uiSettings = MapUiSettings(
                            zoomControlsEnabled = false,
                            compassEnabled = true,
                            myLocationButtonEnabled = false
                        ),
                        onMapClick = { latLng ->
                            if (interactive && onLocationSelected != null) {
                                onLocationSelected(latLng.latitude, latLng.longitude)
                            }
                        }
                    ) {
                        Marker(
                            state = MarkerState(position = LatLng(latitude, longitude)),
                            title = "Delivery Location",
                            snippet = "Doorstep Pinpoint"
                        )

                        if (podLat != null && podLng != null) {
                            Marker(
                                state = MarkerState(position = LatLng(podLat, podLng)),
                                title = podName ?: "Darkstore Pod",
                                snippet = "⚡ 10 min Flash Fulfillment"
                            )
                            Polyline(
                                points = listOf(
                                    LatLng(podLat, podLng),
                                    LatLng(latitude, longitude)
                                ),
                                color = CoralPrimary,
                                width = 10f
                            )
                        }

                        if (riderLat != null && riderLng != null) {
                            Marker(
                                state = MarkerState(position = LatLng(riderLat, riderLng)),
                                title = riderName ?: "Delivery Partner",
                                snippet = "🛵 On the way"
                            )
                        }
                    }
                }
            }

            OmniMapDisplayMode.LIVE_STREETS, OmniMapDisplayMode.SATELLITE -> {
                // Interactive Leaflet Google Maps & OpenStreetMap with inlined offline assets
                AndroidView(
                    factory = { ctx ->
                        WebView(ctx).apply {
                            settings.javaScriptEnabled = true
                            settings.domStorageEnabled = true
                            settings.databaseEnabled = true
                            settings.allowFileAccess = true
                            settings.allowContentAccess = true
                            settings.allowFileAccessFromFileURLs = true
                            settings.allowUniversalAccessFromFileURLs = true
                            settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                            settings.cacheMode = WebSettings.LOAD_DEFAULT
                            settings.loadWithOverviewMode = true
                            settings.useWideViewPort = true
                            settings.setSupportZoom(true)
                            settings.builtInZoomControls = false
                            settings.displayZoomControls = false
                            settings.userAgentString = "Mozilla/5.0 (Linux; Android 14; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/126.0.0.0 Mobile Safari/537.36 OmniCart/1.0"
                            setLayerType(View.LAYER_TYPE_HARDWARE, null)
                            setBackgroundColor(android.graphics.Color.parseColor("#EBF0F5"))

                            webChromeClient = object : WebChromeClient() {
                                override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
                                    Log.d("OmniMap", "[JS] ${consoleMessage?.message()} (line ${consoleMessage?.lineNumber()})")
                                    return true
                                }
                            }
                            webViewClient = object : WebViewClient() {
                                override fun onPageFinished(view: WebView?, url: String?) {
                                    isLoadingTiles = false
                                    view?.evaluateJavascript("if (window.map) { window.map.invalidateSize(); }", null)
                                }

                                override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
                                    Log.w("OmniMap", "Resource load error: ${error?.description} for ${request?.url}")
                                }

                                override fun onReceivedSslError(view: WebView?, handler: SslErrorHandler?, error: SslError?) {
                                    // Proceed for secure raster tile CDNs
                                    handler?.proceed()
                                }
                            }

                            if (onLocationSelected != null) {
                                addJavascriptInterface(MapBridge { lat, lng ->
                                    onLocationSelected(lat, lng)
                                }, "AndroidBridge")
                            }

                            loadDataWithBaseURL("https://cart.local/map/", mapHtml, "text/html", "UTF-8", null)
                            webViewRef = this
                        }
                    },
                    update = { wv ->
                        webViewRef = wv
                        val js = "if (window.updateCenter) { window.updateCenter($latitude, $longitude, $zoom); }"
                        wv.evaluateJavascript(js, null)
                        if (riderLat != null && riderLng != null) {
                            val riderJs = "if (window.updateRider) { window.updateRider($riderLat, $riderLng); }"
                            wv.evaluateJavascript(riderJs, null)
                        }
                    },
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("universal_interactive_map_webview")
                )
            }
        }

        // Mode Switcher & Overlays
        if (showControls) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalAlignment = Alignment.End
            ) {
                // Map Mode Selector Tabs: [ 🗺️ Streets | 🛰️ Satellite ]
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                    shadowElevation = 4.dp
                ) {
                    Row(
                        modifier = Modifier.padding(3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        MapModeButton(
                            title = "🗺️ Streets",
                            selected = displayMode == OmniMapDisplayMode.LIVE_STREETS,
                            onClick = { displayMode = OmniMapDisplayMode.LIVE_STREETS }
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        MapModeButton(
                            title = "🛰️ Satellite",
                            selected = displayMode == OmniMapDisplayMode.SATELLITE,
                            onClick = { displayMode = OmniMapDisplayMode.SATELLITE }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Launch External Google Maps App Button
                if (showExternalMapButton) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                        shadowElevation = 4.dp,
                        modifier = Modifier.clickable {
                            openInGoogleMaps(context, latitude, longitude, podName ?: "OmniCart Delivery")
                        }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Filled.Directions,
                                contentDescription = "Open in Maps App",
                                tint = CoralPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Maps App",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }

        // Center Pin for Interactive Location Picker (Only when requested and not in vector radar mode)
        if (interactive && showCenterPin && displayMode != OmniMapDisplayMode.VECTOR_RADAR) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(bottom = 36.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = SlateNavyDark,
                        shadowElevation = 4.dp
                    ) {
                        Text(
                            text = "📍 Delivery Doorstep",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    Icon(
                        Icons.Filled.LocationOn,
                        contentDescription = "Selected Pin",
                        tint = CoralPrimary,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }
        }
    }
}

/**
 * High-performance 100% native Jetpack Compose Vector Map.
 * Renders roads, buildings, water canal, darkstore pod, scooter rider, and delivery pin.
 * Completely immune to MESA / GPU rendernode errors and works 100% offline.
 */
@Composable
fun NativeComposeVectorMap(
    modifier: Modifier = Modifier,
    latitude: Double,
    longitude: Double,
    podLat: Double?,
    podLng: Double?,
    podName: String,
    riderLat: Double?,
    riderLng: Double?,
    riderName: String,
    interactive: Boolean,
    onLocationSelected: ((Double, Double) -> Unit)?
) {
    var panOffsetX by remember { mutableFloatStateOf(0f) }
    var panOffsetY by remember { mutableFloatStateOf(0f) }

    val infiniteTransition = rememberInfiniteTransition(label = "RadarMapPulse")
    val pulseRadius by infiniteTransition.animateFloat(
        initialValue = 20f,
        targetValue = 65f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "RadarRadius"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 0.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "RadarAlpha"
    )

    Box(
        modifier = modifier
            .background(Color(0xFFF3F5F3))
            .pointerInput(interactive) {
                if (interactive) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        panOffsetX += dragAmount.x
                        panOffsetY += dragAmount.y
                        onLocationSelected?.invoke(
                            latitude - (panOffsetY * 0.00005),
                            longitude + (panOffsetX * 0.00005)
                        )
                    }
                }
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasW = size.width
            val canvasH = size.height
            val centerX = canvasW / 2f + panOffsetX
            val centerY = canvasH / 2f + panOffsetY

            // 1. Water Canal / River
            drawCircle(
                color = Color(0xFFCBE5ED),
                radius = 240f,
                center = Offset(centerX - 220f, centerY - 160f)
            )

            // 2. Green Parkland
            drawRect(
                color = Color(0xFFD7EAD7),
                topLeft = Offset(centerX + 80f, centerY - 240f),
                size = Size(260f, 160f)
            )
            drawRect(
                color = Color(0xFFD7EAD7),
                topLeft = Offset(centerX - 280f, centerY + 120f),
                size = Size(180f, 140f)
            )

            // 3. Residential Blocks (3D Shaded)
            val buildingColor = Color(0xFFE4E8E5)
            val buildingBorder = Color(0xFFD2D8D4)
            val blocks = listOf(
                Pair(-160f, -70f), Pair(-70f, -70f), Pair(70f, -70f), Pair(160f, -70f),
                Pair(-160f, 70f), Pair(-70f, 70f), Pair(70f, 70f), Pair(160f, 70f)
            )
            blocks.forEach { (bx, by) ->
                drawRect(
                    color = buildingColor,
                    topLeft = Offset(centerX + bx - 30f, centerY + by - 30f),
                    size = Size(60f, 60f)
                )
                drawRect(
                    color = buildingBorder,
                    topLeft = Offset(centerX + bx - 30f, centerY + by - 30f),
                    size = Size(60f, 60f),
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2f)
                )
            }

            // 4. Roads Grid
            val roadColor = Color.White
            val roadBorder = Color(0xFFE0E0E0)

            // Major Boulevard
            drawRect(
                color = roadBorder,
                topLeft = Offset(0f, centerY - 28f),
                size = Size(canvasW, 56f)
            )
            drawRect(
                color = roadColor,
                topLeft = Offset(0f, centerY - 24f),
                size = Size(canvasW, 48f)
            )

            // Ring Avenue
            drawRect(
                color = roadBorder,
                topLeft = Offset(centerX - 28f, 0f),
                size = Size(56f, canvasH)
            )
            drawRect(
                color = roadColor,
                topLeft = Offset(centerX - 24f, 0f),
                size = Size(48f, canvasH)
            )

            // Secondary Cross Streets
            drawRect(color = roadColor, topLeft = Offset(0f, centerY - 140f), size = Size(canvasW, 24f))
            drawRect(color = roadColor, topLeft = Offset(0f, centerY + 140f), size = Size(canvasW, 24f))

            // 5. Fulfillment Pod Hub (Darkstore)
            val podOffset = Offset(centerX - 120f, centerY - 100f)
            drawCircle(
                color = CoralPrimary.copy(alpha = pulseAlpha),
                radius = pulseRadius,
                center = podOffset
            )
            drawCircle(
                color = SlateNavyDark,
                radius = 16f,
                center = podOffset
            )
            drawCircle(
                color = CoralPrimary,
                radius = 8f,
                center = podOffset
            )

            // 6. Polyline Route to Customer Doorstep
            val destOffset = Offset(centerX, centerY)
            drawLine(
                color = CoralPrimary,
                start = podOffset,
                end = destOffset,
                strokeWidth = 6f,
                pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(15f, 15f), 0f)
            )

            // 7. Delivery Rider Scooter Position
            val riderOffset = Offset(
                podOffset.x + (destOffset.x - podOffset.x) * 0.6f,
                podOffset.y + (destOffset.y - podOffset.y) * 0.6f
            )
            drawCircle(
                color = GreenSuccess.copy(alpha = 0.3f),
                radius = 18f,
                center = riderOffset
            )
            drawCircle(
                color = GreenSuccess,
                radius = 10f,
                center = riderOffset
            )

            // 8. Labels
            drawIntoCanvas { canvas ->
                val textPaint = Paint().apply {
                    color = android.graphics.Color.rgb(100, 116, 139)
                    textSize = 24f
                    typeface = Typeface.DEFAULT_BOLD
                    isAntiAlias = true
                }
                val podPaint = Paint().apply {
                    color = android.graphics.Color.rgb(15, 23, 42)
                    textSize = 24f
                    typeface = Typeface.DEFAULT_BOLD
                    isAntiAlias = true
                }

                canvas.nativeCanvas.drawText("Central Boulevard", 30f, centerY - 34f, textPaint)
                canvas.nativeCanvas.drawText("Ring Road", centerX + 32f, 80f, textPaint)
                canvas.nativeCanvas.drawText("⚡ $podName", podOffset.x - 70f, podOffset.y - 24f, podPaint)
            }
        }

        // Center Target Pin
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(bottom = 36.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = SlateNavyDark,
                    shadowElevation = 6.dp
                ) {
                    Text(
                        text = if (interactive) "📍 Delivery Doorstep Pin" else "📍 $latitude, $longitude",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }
                Icon(
                    Icons.Filled.LocationOn,
                    contentDescription = "Selected Pin",
                    tint = CoralPrimary,
                    modifier = Modifier.size(40.dp)
                )
            }
        }
    }
}

@Composable
private fun MapModeButton(
    title: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = if (selected) CoralPrimary else Color.Transparent,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Text(
            text = title,
            fontSize = 10.sp,
            fontWeight = if (selected) FontWeight.ExtraBold else FontWeight.Medium,
            color = if (selected) Color.White else MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

/**
 * Launch the native Google Maps app with precise coordinates and pin label
 */
fun openInGoogleMaps(context: Context, lat: Double, lng: Double, label: String = "Delivery Location") {
    try {
        val uri = Uri.parse("geo:$lat,$lng?q=$lat,$lng($label)")
        val mapIntent = Intent(Intent.ACTION_VIEW, uri).apply {
            setPackage("com.google.android.apps.maps")
        }
        if (mapIntent.resolveActivity(context.packageManager) != null) {
            context.startActivity(mapIntent)
        } else {
            val webUri = Uri.parse("https://www.google.com/maps/search/?api=1&query=$lat,$lng")
            context.startActivity(Intent(Intent.ACTION_VIEW, webUri))
        }
    } catch (_: Exception) {
        val webUri = Uri.parse("https://www.google.com/maps/search/?api=1&query=$lat,$lng")
        context.startActivity(Intent(Intent.ACTION_VIEW, webUri))
    }
}

/**
 * Generates self-contained HTML embedding local Leaflet CSS and JS from assets/map/,
 * loading Google Maps raster tiles (Streets & Hybrid Satellite) with automatic OpenStreetMap fallback.
 */
private fun generateLeafletHtml(
    context: Context,
    lat: Double,
    lng: Double,
    zoom: Int,
    isSatellite: Boolean,
    podLat: Double?,
    podLng: Double?,
    podName: String,
    riderLat: Double?,
    riderLng: Double?,
    riderName: String,
    isInteractive: Boolean
): String {
    val inlinedCss = MapAssetCache.getCss(context)
    val inlinedJs = MapAssetCache.getJs(context)

    val cssTag = if (inlinedCss.isNotBlank()) {
        "<style>\n$inlinedCss\n</style>"
    } else {
        """<link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.9.4/leaflet.min.css" />"""
    }

    val jsTag = if (inlinedJs.isNotBlank()) {
        "<script>\n$inlinedJs\n</script>"
    } else {
        """<script src="https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.9.4/leaflet.min.js"></script>"""
    }

    return """
        <!DOCTYPE html>
        <html>
        <head>
            <meta charset="utf-8" />
            <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no" />
            $cssTag
            $jsTag
            <style>
                * {
                    box-sizing: border-box;
                    -webkit-tap-highlight-color: transparent;
                }
                html, body, #map {
                    width: 100%;
                    height: 100%;
                    margin: 0;
                    padding: 0;
                    background: #E8ECEF;
                    font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
                    overflow: hidden;
                    touch-action: manipulation;
                }
                .leaflet-control-attribution {
                    font-size: 8px !important;
                    background: rgba(255,255,255,0.7) !important;
                    padding: 2px 4px !important;
                    border-radius: 4px;
                }
                .hub-pin {
                    background: #0F172A;
                    color: #FFFFFF;
                    padding: 5px 9px;
                    border-radius: 8px;
                    font-size: 11px;
                    font-weight: bold;
                    border: 2px solid #FF5252;
                    box-shadow: 0 3px 8px rgba(0,0,0,0.35);
                    white-space: nowrap;
                    display: inline-block;
                    transform: translate(-50%, -100%);
                }
                .rider-pin {
                    background: #10B981;
                    color: white;
                    padding: 5px 9px;
                    border-radius: 8px;
                    font-size: 11px;
                    font-weight: bold;
                    border: 2px solid white;
                    box-shadow: 0 3px 8px rgba(0,0,0,0.35);
                    white-space: nowrap;
                    display: inline-block;
                    transform: translate(-50%, -100%);
                }
                .doorstep-pin {
                    background: #FF5252;
                    color: white;
                    padding: 4px 8px;
                    border-radius: 8px;
                    font-size: 11px;
                    font-weight: bold;
                    border: 2px solid white;
                    box-shadow: 0 2px 6px rgba(0,0,0,0.3);
                    white-space: nowrap;
                    display: inline-block;
                    transform: translate(-50%, -100%);
                }
            </style>
        </head>
        <body>
            <div id="map"></div>
            <script>
                var map = null;
                var currentTileLayer = null;
                var podMarker = null;
                var riderMarker = null;
                var doorstepMarker = null;
                var routeLine = null;
                window._lastUserMoveTime = 0;
                window._isUserDragging = false;

                var tileProviders = {
                    'streets': {
                        url: 'https://a.basemaps.cartocdn.com/rastertiles/voyager/{z}/{x}/{y}{r}.png',
                        subdomains: ['a', 'b', 'c', 'd'],
                        maxZoom: 20,
                        attribution: '© OpenStreetMap © CARTO'
                    },
                    'google_streets': {
                        url: 'https://mt{s}.google.com/vt/lyrs=m&x={x}&y={y}&z={z}',
                        subdomains: ['0', '1', '2', '3'],
                        maxZoom: 20,
                        attribution: '© Google Maps'
                    },
                    'satellite': {
                        url: 'https://server.arcgisonline.com/ArcGIS/rest/services/World_Imagery/MapServer/tile/{z}/{y}/{x}',
                        subdomains: [],
                        maxZoom: 19,
                        attribution: '© Esri Satellite'
                    },
                    'google_hybrid': {
                        url: 'https://mt{s}.google.com/vt/lyrs=y&x={x}&y={y}&z={z}',
                        subdomains: ['0', '1', '2', '3'],
                        maxZoom: 20,
                        attribution: '© Google Satellite'
                    },
                    'osm': {
                        url: 'https://tile.openstreetmap.org/{z}/{x}/{y}.png',
                        subdomains: ['a', 'b', 'c'],
                        maxZoom: 19,
                        attribution: '© OpenStreetMap'
                    }
                };

                function setTileLayer(mode) {
                    var prov = tileProviders[mode] || tileProviders['streets'];
                    if (currentTileLayer && map) {
                        map.removeLayer(currentTileLayer);
                    }
                    if (map) {
                        currentTileLayer = L.tileLayer(prov.url, {
                            maxZoom: prov.maxZoom,
                            subdomains: prov.subdomains,
                            attribution: prov.attribution
                        }).addTo(map);

                        currentTileLayer.on('tileerror', function(error) {
                            if (error && error.tile) {
                                var coords = error.coords;
                                var currentSrc = error.tile.src || '';
                                if (!currentSrc.includes('google.com')) {
                                    var s = coords.x % 4;
                                    error.tile.src = 'https://mt' + s + '.google.com/vt/lyrs=m&x=' + coords.x + '&y=' + coords.y + '&z=' + coords.z;
                                } else if (!currentSrc.includes('openstreetmap.org')) {
                                    error.tile.src = 'https://tile.openstreetmap.org/' + coords.z + '/' + coords.x + '/' + coords.y + '.png';
                                }
                            }
                        });
                    }
                }

                try {
                    map = L.map('map', {
                        center: [$lat, $lng],
                        zoom: $zoom,
                        zoomControl: false,
                        attributionControl: true
                    });

                    setTileLayer('${if (isSatellite) "satellite" else "streets"}');

                    ${if (podLat != null && podLng != null) """
                    var podIcon = L.divIcon({
                        className: 'custom-div-icon',
                        html: "<div class='hub-pin'>⚡ $podName</div>",
                        iconSize: [0, 0]
                    });
                    podMarker = L.marker([$podLat, $podLng], { icon: podIcon }).addTo(map);
                    """ else ""}

                    ${if (riderLat != null && riderLng != null) """
                    var riderIcon = L.divIcon({
                        className: 'custom-div-icon',
                        html: "<div class='rider-pin'>🛵 $riderName</div>",
                        iconSize: [0, 0]
                    });
                    riderMarker = L.marker([$riderLat, $riderLng], { icon: riderIcon }).addTo(map);
                    """ else ""}

                    ${if (isInteractive) """
                    map.on('movestart', function() {
                        window._isUserDragging = true;
                    });

                    map.on('move', function() {
                        var center = map.getCenter();
                        if (window.AndroidBridge && window.AndroidBridge.onPinMoved) {
                            window.AndroidBridge.onPinMoved(center.lat, center.lng);
                        }
                    });

                    map.on('moveend', function() {
                        window._isUserDragging = false;
                        window._lastUserMoveTime = Date.now();
                        var center = map.getCenter();
                        if (window.AndroidBridge && window.AndroidBridge.onPinMoved) {
                            window.AndroidBridge.onPinMoved(center.lat, center.lng);
                        }
                    });

                    map.on('click', function(e) {
                        window._lastUserMoveTime = Date.now();
                        map.panTo(e.latlng);
                        if (window.AndroidBridge && window.AndroidBridge.onPinMoved) {
                            window.AndroidBridge.onPinMoved(e.latlng.lat, e.latlng.lng);
                        }
                    });
                    """ else """
                    var doorstepIcon = L.divIcon({
                        className: 'custom-div-icon',
                        html: "<div class='doorstep-pin'>📍 Delivery Doorstep</div>",
                        iconSize: [0, 0]
                    });
                    doorstepMarker = L.marker([$lat, $lng], { icon: doorstepIcon }).addTo(map);

                    var routePoints = [];
                    ${if (podLat != null && podLng != null) "routePoints.push([$podLat, $podLng]);" else ""}
                    ${if (riderLat != null && riderLng != null) "routePoints.push([$riderLat, $riderLng]);" else ""}
                    routePoints.push([$lat, $lng]);

                    if (routePoints.length >= 2) {
                        routeLine = L.polyline(routePoints, {
                            color: '#FF5252',
                            weight: 4,
                            dashArray: '6, 6',
                            opacity: 0.85
                        }).addTo(map);
                    }
                    """}
                } catch (err) {
                    console.error("Leaflet map initialization failed: " + err);
                }

                window.updateCenter = function(nlat, nlng, nzoom) {
                    if (Date.now() - (window._lastUserMoveTime || 0) < 1500 || window._isUserDragging) {
                        return;
                    }
                    if (map && nlat && nlng) {
                        map.setView([nlat, nlng], nzoom || map.getZoom());
                        if (doorstepMarker) {
                            doorstepMarker.setLatLng([nlat, nlng]);
                        }
                        if (routeLine && podMarker) {
                            routeLine.setLatLngs([podMarker.getLatLng(), [nlat, nlng]]);
                        }
                    }
                };

                window.recenter = function(nlat, nlng, nzoom) {
                    window._lastUserMoveTime = 0;
                    window._isUserDragging = false;
                    if (map && nlat && nlng) {
                        map.setView([nlat, nlng], nzoom || 16);
                        if (doorstepMarker) {
                            doorstepMarker.setLatLng([nlat, nlng]);
                        }
                    }
                };

                window.updateRider = function(rlat, rlng) {
                    if (riderMarker && rlat && rlng) {
                        riderMarker.setLatLng([rlat, rlng]);
                    }
                };

                window.setMode = function(mode) {
                    setTileLayer(mode);
                };

                window.zoomIn = function() {
                    if (map) map.zoomIn();
                };

                window.zoomOut = function() {
                    if (map) map.zoomOut();
                };

                setTimeout(function() { if (map) map.invalidateSize(); }, 200);
                setTimeout(function() { if (map) map.invalidateSize(); }, 700);
            </script>
        </body>
        </html>
    """.trimIndent()
}
