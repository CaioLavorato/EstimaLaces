package com.estimalaces.app.data.repository

import com.estimalaces.app.data.dao.ClientDao
import com.estimalaces.app.data.dao.GiftDao
import com.estimalaces.app.data.dao.GoalDao
import com.estimalaces.app.data.dao.ProductDao
import com.estimalaces.app.data.dao.SaleDao
import com.estimalaces.app.data.dao.SaleExportRow
import com.estimalaces.app.data.entity.ClientEntity
import com.estimalaces.app.data.entity.GiftEntity
import com.estimalaces.app.data.entity.GoalEntity
import com.estimalaces.app.data.entity.ProductEntity
import com.estimalaces.app.data.entity.SaleEntity
import com.estimalaces.app.domain.model.DashboardSummary
import com.estimalaces.app.domain.model.ReportSummary
import com.estimalaces.app.domain.rules.GiftRules
import com.estimalaces.app.domain.rules.PricingRules
import com.estimalaces.app.domain.usecase.DateRange
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class EstimaLacesRepository(
    private val productDao: ProductDao,
    private val clientDao: ClientDao,
    private val saleDao: SaleDao,
    private val goalDao: GoalDao,
    private val giftDao: GiftDao
) {
    fun observeProducts(): Flow<List<ProductEntity>> = productDao.observeProducts()
    fun observeLowStockProducts(): Flow<List<ProductEntity>> = productDao.observeLowStockProducts()
    fun observeClients(): Flow<List<ClientEntity>> = clientDao.observeClients()
    fun observeSales(): Flow<List<SaleEntity>> = saleDao.observeSales()
    fun observeGoal(): Flow<GoalEntity?> = goalDao.observeGoal()

    fun observeDashboard(range: DateRange): Flow<DashboardSummary> {
        return combine(
            saleDao.observeSoldBetween(range.start, range.end),
            productDao.observeSpentBetween(range.start, range.end),
            saleDao.observeProfitBetween(range.start, range.end),
            goalDao.observeGoal(),
            productDao.observeLowStockProducts()
        ) { sold, spent, profit, goal, lowStock ->
            DashboardSummary(
                totalSold = sold,
                totalSpent = spent,
                profit = profit,
                monthlyGoal = goal?.monthlyGoal ?: 0.0,
                maxGiftAfterGoal = goal?.maxGiftValue ?: 0.0,
                lowStockMessages = lowStock.map {
                    "${it.name} esta acabando (${it.currentQuantity} unidades restantes)"
                }
            )
        }
    }

    fun observeReport(range: DateRange): Flow<ReportSummary> {
        return combine(
            saleDao.observeCountBetween(range.start, range.end),
            saleDao.observeSoldBetween(range.start, range.end),
            productDao.observeSpentBetween(range.start, range.end),
            saleDao.observeProfitBetween(range.start, range.end),
            saleDao.observeBestProductBetween(range.start, range.end),
            clientDao.observeRecurringCount(),
            saleDao.observeGiftCountBetween(range.start, range.end),
            goalDao.observeGoal()
        ) { values ->
            val quantity = values[0] as Int
            val sold = values[1] as Double
            val spent = values[2] as Double
            val profit = values[3] as Double
            val best = values[4] as String?
            val recurring = values[5] as Int
            val gifts = values[6] as Int
            val goal = values[7] as GoalEntity?
            ReportSummary(
                quantitySold = quantity,
                totalSold = sold,
                totalSpent = spent,
                profit = profit,
                bestProduct = best ?: "-",
                recurringClients = recurring,
                giftsReleased = gifts,
                goalReached = goal != null && goal.monthlyGoal > 0.0 && sold >= goal.monthlyGoal
            )
        }
    }

    suspend fun addProduct(
        name: String,
        type: String,
        purchaseValue: Double,
        supplier: String,
        notes: String,
        currentQuantity: Int = 1,
        minimumQuantity: Int = 1
    ) = withContext(Dispatchers.IO) {
        val cleanedName = name.trim()
        val existing = productDao.findByName(cleanedName)
        val product = ProductEntity(
            id = existing?.id ?: 0,
            name = cleanedName,
            type = type,
            purchaseValue = purchaseValue,
            suggestedSaleValue = PricingRules.suggestedSaleValue(purchaseValue),
            createdAt = existing?.createdAt ?: System.currentTimeMillis(),
            supplier = supplier.trim(),
            notes = notes.trim(),
            currentQuantity = currentQuantity.coerceAtLeast(0),
            minimumQuantity = minimumQuantity.coerceAtLeast(0)
        )
        if (existing == null) {
            productDao.insert(product)
        } else {
            productDao.update(product)
        }
    }

    suspend fun addStock(productId: Long, amount: Int) = withContext(Dispatchers.IO) {
        if (amount > 0) productDao.addStock(productId, amount)
    }

    private suspend fun ensureProduct(
        name: String,
        type: String,
        purchaseValue: Double,
        initialQuantity: Int = 0
    ): ProductEntity? {
        val cleanedName = name.trim()
        if (cleanedName.isBlank()) return null
        val existing = productDao.findByName(cleanedName)
        if (existing != null) return existing
        val id = productDao.insert(
            ProductEntity(
                name = name.trim(),
                type = type,
                purchaseValue = purchaseValue,
                suggestedSaleValue = PricingRules.suggestedSaleValue(purchaseValue),
                createdAt = System.currentTimeMillis(),
                currentQuantity = initialQuantity.coerceAtLeast(0),
                minimumQuantity = 1
            )
        )
        return productDao.findById(id)
    }

    suspend fun registerSale(
        productId: Long?,
        productName: String,
        productType: String,
        clientName: String,
        saleValue: Double,
        productCost: Double,
        giftApplied: Boolean,
        giftValue: Double,
        giftType: String,
        giftProductId: Long?,
        giftProductName: String,
        paymentMethod: String,
        cardFeePercent: Double,
        notes: String
    ) = withContext(Dispatchers.IO) {
        val product = productId?.let { productDao.findById(it) }
            ?: ensureProduct(productName, productType, productCost, initialQuantity = 0)
        val cleanedClientName = clientName.trim().ifBlank { "Cliente sem nome" }
        val existingClient = clientDao.findByName(cleanedClientName)
        val client = if (existingClient == null) {
            val id = clientDao.insert(ClientEntity(name = cleanedClientName))
            ClientEntity(id = id, name = cleanedClientName)
        } else {
            existingClient
        }

        val finalGiftValue = if (giftApplied && giftType == "VALOR") giftValue else 0.0
        val cardFee = if (paymentMethod == "Cartao") cardFeePercent.coerceAtLeast(0.0) else 0.0
        val profit = PricingRules.saleProfit(saleValue, productCost, finalGiftValue, cardFee)
        saleDao.insert(
            SaleEntity(
                productId = product?.id,
                productName = product?.name ?: productName.trim(),
                productType = product?.type ?: productType,
                clientId = client.id.takeIf { it > 0 },
                clientName = cleanedClientName,
                saleValue = saleValue,
                productCost = productCost,
                profit = profit,
                giftApplied = giftApplied,
                giftValue = finalGiftValue,
                giftType = if (giftApplied) giftType else "VALOR",
                giftProductId = giftProductId,
                giftProductName = if (giftApplied && giftType == "PRODUTO") giftProductName.trim() else "",
                paymentMethod = paymentMethod,
                cardFeePercent = cardFee,
                cardFeeValue = PricingRules.cardFeeValue(saleValue, cardFee),
                notes = notes.trim(),
                soldAt = System.currentTimeMillis()
            )
        )

        product?.let { if (it.currentQuantity > 0) productDao.decrementStock(it.id) }
        if (giftApplied && giftType == "PRODUTO") {
            val giftProduct = giftProductId?.let { productDao.findById(it) }
                ?: ensureProduct(giftProductName, "produto", 0.0, initialQuantity = 0)
            giftProduct?.let { if (it.currentQuantity > 0) productDao.decrementStock(it.id) }
        }

        val newPurchaseCount = client.purchaseCount + 1
        clientDao.update(client.copy(purchaseCount = newPurchaseCount, lastPurchaseAt = System.currentTimeMillis()))
        if (GiftRules.shouldReleaseLoyaltyGift(newPurchaseCount)) {
            giftDao.insert(
                GiftEntity(
                    clientId = client.id.takeIf { it > 0 },
                    clientName = cleanedClientName,
                    reason = "FIDELIDADE",
                    valueLimit = 0.0,
                    createdAt = System.currentTimeMillis()
                )
            )
        }
    }

    suspend fun saveGoal(monthlyGoal: Double, maxGiftValue: Double) = withContext(Dispatchers.IO) {
        goalDao.save(
            GoalEntity(
                monthlyGoal = monthlyGoal,
                maxGiftValue = maxGiftValue,
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun exportRows(): List<SaleExportRow> = withContext(Dispatchers.IO) {
        saleDao.exportRows()
    }

    suspend fun registerExternalSale(
        externalOrderId: String,
        productName: String,
        clientName: String,
        saleValue: Double,
        productCost: Double,
        soldAt: Long = System.currentTimeMillis()
    ) = withContext(Dispatchers.IO) {
        if (externalOrderId.isNotBlank() && saleDao.countByExternalOrderId(externalOrderId) > 0) {
            return@withContext
        }
        val product = ensureProduct(productName, "lace", productCost, initialQuantity = 0)
        val cleanedClientName = clientName.trim().ifBlank { "Cliente sem nome" }
        val existingClient = clientDao.findByName(cleanedClientName)
        val client = if (existingClient == null) {
            val id = clientDao.insert(ClientEntity(name = cleanedClientName))
            ClientEntity(id = id, name = cleanedClientName)
        } else {
            existingClient
        }
        saleDao.insert(
            SaleEntity(
                productId = product?.id,
                productName = productName.trim(),
                productType = product?.type ?: "lace",
                clientId = client.id.takeIf { it > 0 },
                clientName = cleanedClientName,
                saleValue = saleValue,
                productCost = productCost,
                profit = PricingRules.saleProfit(saleValue, productCost, 0.0),
                giftApplied = false,
                giftValue = 0.0,
                paymentMethod = "Site",
                notes = "Venda importada do site",
                soldAt = soldAt,
                externalOrderId = externalOrderId,
                source = "SITE"
            )
        )
        product?.let { if (it.currentQuantity > 0) productDao.decrementStock(it.id) }
        clientDao.update(client.copy(purchaseCount = client.purchaseCount + 1, lastPurchaseAt = soldAt))
    }

    fun observeClientGiftMessage(clientName: String): Flow<String?> {
        val cleanName = clientName.trim()
        if (cleanName.isBlank()) return kotlinx.coroutines.flow.flowOf(null)
        return clientDao.observeClients().map { clients ->
            clients.firstOrNull { it.name.equals(cleanName, ignoreCase = true) }
                ?.let { GiftRules.loyaltyMessage(it.purchaseCount) }
        }
    }
}
