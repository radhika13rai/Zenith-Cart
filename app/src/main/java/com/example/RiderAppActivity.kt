package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.ui.EcosystemApp
import com.example.ui.OmniMainScreen
import com.example.ui.theme.OmniCartTheme
import com.example.ui.viewmodel.EcommerceViewModel

/**
 * Standalone entry point for the OmniCart Rider / Delivery Fleet App.
 * When published as an independent Play Store app or APK, this Activity
 * launches directly into the On/Off Duty toggle, 10-Min Flash Dispatch Queue, and OTP Doorstep Handover experience.
 */
class RiderAppActivity : ComponentActivity() {

    private val viewModel: EcommerceViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            OmniCartTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    OmniMainScreen(
                        viewModel = viewModel,
                        initialMode = EcosystemApp.RIDER,
                        lockMode = true
                    )
                }
            }
        }
    }
}
