package com.example.model

import java.text.DecimalFormat

enum class Currency(
    val code: String,
    val symbol: String,
    val rateToUsd: Double,
    val flag: String,
    val label: String
) {
    USD("USD", "$", 1.00, "🇺🇸", "US Dollar"),
    EUR("EUR", "€", 0.92, "🇪🇺", "Euro"),
    GBP("GBP", "£", 0.79, "🇬🇧", "British Pound"),
    INR("INR", "₹", 83.50, "🇮🇳", "Indian Rupee"),
    JPY("JPY", "¥", 155.00, "🇯🇵", "Japanese Yen"),
    CAD("CAD", "C$", 1.36, "🇨🇦", "Canadian Dollar");

    fun format(amountInUsd: Double): String {
        val converted = amountInUsd * rateToUsd
        return if (this == JPY) {
            val df = DecimalFormat("#,##0")
            "$symbol${df.format(converted)}"
        } else {
            val df = DecimalFormat("#,##0.00")
            "$symbol${df.format(converted)}"
        }
    }

    companion object {
        fun fromCode(code: String): Currency = entries.find { it.code.equals(code, ignoreCase = true) } ?: USD
    }
}
