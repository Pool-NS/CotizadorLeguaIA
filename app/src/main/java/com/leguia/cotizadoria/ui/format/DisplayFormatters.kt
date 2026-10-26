package com.leguia.cotizadoria.ui.format

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

private fun formatNumber(value: Double, pattern: String): String {
    val symbols = DecimalFormatSymbols.getInstance(Locale.US)
    return DecimalFormat(pattern, symbols).format(value)
}

fun formatMoney(value: Double): String = "S/ ${formatNumber(value, "#,##0.00")}"

fun formatDecimal(value: Double): String = formatNumber(value, "0.00")

fun formatPercent(value: Double): String = formatNumber(value, "0.0'%'")
