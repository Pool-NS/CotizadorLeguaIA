package com.leguia.cotizadoria.data

import android.content.Context
import org.json.JSONObject

data class SyntheticDemoInventory(
    val name: String,
    val quantity: Double,
    val unit: String,
    val unitCost: Double
)

data class SyntheticDemoPriceRule(
    val service: String,
    val rate: Double,
    val rule: String,
    val unit: String
)

data class SyntheticDemoQuote(
    val id: String,
    val service: String,
    val length: Double,
    val width: Double,
    val material: String,
    val required: Double,
    val available: Double,
    val shortage: Double,
    val simulatedPrice: Double,
    val directCost: Double,
    val contribution: Double,
    val feasibility: String
)

data class SyntheticDemoDashboard(
    val quoteCount: Int,
    val averageMinutes: Double,
    val averagePrice: Double,
    val totalPrices: Double,
    val conversionRatePercent: Double,
    val averageDirectCost: Double,
    val averageContribution: Double,
    val nonPositiveContributionCount: Int,
    val feasibilityCounts: Map<String, Int>,
    val inventory: List<SyntheticDemoInventory>,
    val priceRules: List<SyntheticDemoPriceRule>,
    val sampleQuotes: List<SyntheticDemoQuote>
)

object SyntheticDemoDataLoader {
    fun load(context: Context): SyntheticDemoDashboard {
        val root = context.assets.open("synthetic_stock_dashboard.json").bufferedReader().use { reader ->
            JSONObject(reader.readText())
        }
        val feasibility = root.getJSONObject("feasibility_counts")
        val feasibilityCounts = buildMap {
            val keys = feasibility.keys()
            while (keys.hasNext()) {
                val key = keys.next()
                put(key, feasibility.getInt(key))
            }
        }
        val inventoryJson = root.getJSONArray("inventory")
        val inventory = (0 until inventoryJson.length()).map { index ->
            val item = inventoryJson.getJSONObject(index)
            SyntheticDemoInventory(
                name = item.getString("name"),
                quantity = item.getDouble("stock"),
                unit = item.getString("unit"),
                unitCost = item.getDouble("cost")
            )
        }
        val priceJson = root.getJSONArray("price_rules")
        val priceRules = (0 until priceJson.length()).map { index ->
            val item = priceJson.getJSONObject(index)
            SyntheticDemoPriceRule(
                service = item.getString("service"),
                rate = item.getDouble("rate"),
                rule = item.getString("rule"),
                unit = item.getString("unit")
            )
        }
        val quotesJson = root.getJSONArray("sample_quotes")
        val samples = (0 until quotesJson.length()).map { index ->
            val item = quotesJson.getJSONObject(index)
            SyntheticDemoQuote(
                id = item.getString("quote_id"),
                service = item.getString("service_type"),
                length = item.getDouble("length_m"),
                width = item.getDouble("width_m"),
                material = item.getString("material_name"),
                required = item.getDouble("required_material_quantity"),
                available = item.getDouble("stock_snapshot_quantity"),
                shortage = item.getDouble("material_shortage_quantity"),
                simulatedPrice = item.getDouble("simulated_final_price"),
                directCost = item.getDouble("simulated_direct_material_cost"),
                contribution = item.getDouble("simulated_gross_contribution_before_labor_overhead"),
                feasibility = item.getString("synthetic_feasibility_result")
            )
        }
        return SyntheticDemoDashboard(
            quoteCount = root.getInt("quote_count"),
            averageMinutes = root.getDouble("mean_quote_minutes"),
            averagePrice = root.getDouble("mean_simulated_final_price"),
            totalPrices = root.getDouble("sum_simulated_final_prices"),
            conversionRatePercent = root.getDouble("conversion_rate_pct"),
            averageDirectCost = root.getDouble("mean_simulated_direct_material_cost"),
            averageContribution = root.getDouble("mean_simulated_contribution_before_labor_overhead"),
            nonPositiveContributionCount = root.getInt("non_positive_direct_contribution_count"),
            feasibilityCounts = feasibilityCounts,
            inventory = inventory,
            priceRules = priceRules,
            sampleQuotes = samples
        )
    }
}
