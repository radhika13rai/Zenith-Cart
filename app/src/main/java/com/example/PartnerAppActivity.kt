package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.ui.PartnerAppScreen
import com.example.ui.theme.OmniCartTheme
import com.example.ui.viewmodel.EcommerceViewModel

/**
 * Standalone entry point for the OmniCart Partner / Merchant Hub App.
 * When published as an independent Play Store app or APK, this Activity
 * launches directly into the Vendor Catalog, Inventory Restock, and Bank Settlements experience.
 */
class PartnerAppActivity : ComponentActivity() {

    private val viewModel: EcommerceViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            OmniCartTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    PartnerAppScreen(viewModel = viewModel)
                }
            }
        }
    }
}
