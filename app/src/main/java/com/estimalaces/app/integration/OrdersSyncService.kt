package com.estimalaces.app.integration

import com.estimalaces.app.BuildConfig
import com.estimalaces.app.data.repository.EstimaLacesRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

class OrdersSyncService(
    private val repository: EstimaLacesRepository,
    private val intervalMillis: Long = 5 * 60 * 1000L
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    fun start() {
        if (BuildConfig.ORDERS_API_KEY.isBlank()) return
        scope.launch {
            while (isActive) {
                runCatching { syncOnce() }
                delay(intervalMillis)
            }
        }
    }

    private suspend fun syncOnce() {
        val connection = (URL(BuildConfig.ORDERS_SYNC_URL).openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 15_000
            readTimeout = 20_000
            setRequestProperty("x-api-key", BuildConfig.ORDERS_API_KEY)
            setRequestProperty("Accept", "application/json")
        }

        connection.use {
            if (it.responseCode !in 200..299) return
            val body = it.inputStream.bufferedReader().use { reader -> reader.readText() }
            parseOrders(body).forEach { order ->
                repository.registerExternalSale(
                    externalOrderId = order.externalId,
                    productName = order.productName,
                    clientName = order.clientName,
                    saleValue = order.saleValue,
                    productCost = order.productCost,
                    soldAt = order.soldAt
                )
            }
        }
    }

    private fun parseOrders(body: String): List<ExternalOrder> {
        val trimmed = body.trim()
        if (trimmed.isBlank()) return emptyList()

        val orders = when {
            trimmed.startsWith("[") -> JSONArray(trimmed)
            else -> {
                val root = JSONObject(trimmed)
                root.optJSONArray("orders")
                    ?: root.optJSONArray("data")
                    ?: root.optJSONArray("items")
                    ?: JSONArray().put(root)
            }
        }

        return buildList {
            for (index in 0 until orders.length()) {
                val order = orders.optJSONObject(index) ?: continue
                addAll(order.toExternalOrders(index))
            }
        }
    }

    private fun JSONObject.toExternalOrders(orderIndex: Int): List<ExternalOrder> {
        val orderId = firstText("id", "order_id", "orderId", "number", "codigo")
            .ifBlank { "site-${firstText("created_at", "data", "date")}-$orderIndex" }
        val client = customerName()
        val soldAt = dateMillis()
        val items = optJSONArray("items")
            ?: optJSONArray("products")
            ?: optJSONArray("line_items")

        if (items == null || items.length() == 0) {
            return listOf(
                ExternalOrder(
                    externalId = orderId,
                    productName = firstText("produto", "product", "product_name", "name").ifBlank { "Produto do site" },
                    clientName = client,
                    saleValue = firstDouble("valor", "total", "total_price", "amount", "price"),
                    productCost = firstDouble("custo", "cost", "purchase_value"),
                    soldAt = soldAt
                )
            )
        }

        return buildList {
            for (itemIndex in 0 until items.length()) {
                val item = items.optJSONObject(itemIndex) ?: continue
                val quantity = item.firstInt("quantity", "qty", "quantidade").coerceAtLeast(1)
                val unitPrice = item.firstDouble("unitPrice", "unit_price", "price", "valor")
                val lineTotal = item.firstDouble("total", "total_price", "amount")
                val saleValue = when {
                    unitPrice > 0.0 -> unitPrice
                    lineTotal > 0.0 -> lineTotal / quantity
                    else -> firstDouble("totalAmount", "valor", "total", "amount") / quantity
                }
                repeat(quantity) { unitIndex ->
                    add(
                        ExternalOrder(
                            externalId = "$orderId-$itemIndex-$unitIndex",
                            productName = item.firstText("produto", "product", "product_name", "name", "title")
                                .ifBlank { "Produto do site" },
                            clientName = client,
                            saleValue = saleValue,
                            productCost = item.firstDouble("custo", "cost", "purchase_value"),
                            soldAt = soldAt
                        )
                    )
                }
            }
        }
    }

    private fun JSONObject.customerName(): String {
        firstText("cliente", "client", "customer_name", "name").takeIf { it.isNotBlank() }?.let { return it }
        optJSONObject("customer")?.firstText("name", "full_name", "nome")?.takeIf { it.isNotBlank() }?.let { return it }
        optJSONObject("billing")?.let { billing ->
            val firstName = billing.firstText("first_name", "nome")
            val lastName = billing.firstText("last_name", "sobrenome")
            "$firstName $lastName".trim().takeIf { it.isNotBlank() }?.let { return it }
        }
        return "Cliente do site"
    }

    private fun JSONObject.dateMillis(): Long {
        val raw = firstText("data", "date", "created_at", "createdAt", "paid_at")
        if (raw.isBlank()) return System.currentTimeMillis()
        return runCatching { Instant.parse(raw).toEpochMilli() }
            .recoverCatching {
                LocalDate.parse(raw.take(10)).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            }
            .getOrDefault(System.currentTimeMillis())
    }

    private fun JSONObject.firstText(vararg keys: String): String {
        keys.forEach { key ->
            if (has(key) && !isNull(key)) return optString(key).trim()
        }
        return ""
    }

    private fun JSONObject.firstDouble(vararg keys: String): Double {
        keys.forEach { key ->
            if (has(key) && !isNull(key)) {
                val value = opt(key)
                return when (value) {
                    is Number -> value.toDouble()
                    is String -> value.replace("R$", "", ignoreCase = true)
                        .replace(".", "")
                        .replace(",", ".")
                        .trim()
                        .toDoubleOrNull() ?: 0.0
                    else -> 0.0
                }
            }
        }
        return 0.0
    }

    private fun JSONObject.firstInt(vararg keys: String): Int {
        keys.forEach { key ->
            if (has(key) && !isNull(key)) {
                val value = opt(key)
                return when (value) {
                    is Number -> value.toInt()
                    is String -> value.trim().toIntOrNull() ?: 0
                    else -> 0
                }
            }
        }
        return 0
    }
}

private data class ExternalOrder(
    val externalId: String,
    val productName: String,
    val clientName: String,
    val saleValue: Double,
    val productCost: Double,
    val soldAt: Long
)

private inline fun HttpURLConnection.use(block: (HttpURLConnection) -> Unit) {
    try {
        block(this)
    } finally {
        disconnect()
    }
}
