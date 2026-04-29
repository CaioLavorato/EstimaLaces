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
    fun observeClients(): Flow<List<ClientEntity>> = clientDao.observeClients()
    fun observeSales(): Flow<List<SaleEntity>> = saleDao.observeSales()
    fun observeGoal(): Flow<GoalEntity?> = goalDao.observeGoal()

    fun observeDashboard(range: DateRange): Flow<DashboardSummary> {
        return combine(
            saleDao.observeSoldBetween(range.start, range.end),
            productDao.observeSpentBetween(range.start, range.end),
            saleDao.observeProfitBetween(range.start, range.end),
            goalDao.observeGoal()
        ) { sold, spent, profit, goal ->
            DashboardSummary(
                totalSold = sold,
                totalSpent = spent,
                profit = profit,
                monthlyGoal = goal?.monthlyGoal ?: 0.0,
                maxGiftAfterGoal = goal?.maxGiftValue ?: 0.0
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
        notes: String
    ) = withContext(Dispatchers.IO) {
        productDao.insert(
            ProductEntity(
                name = name.trim(),
                type = type,
                purchaseValue = purchaseValue,
                suggestedSaleValue = PricingRules.suggestedSaleValue(purchaseValue),
                createdAt = System.currentTimeMillis(),
                supplier = supplier.trim(),
                notes = notes.trim()
            )
        )
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
        paymentMethod: String,
        notes: String
    ) = withContext(Dispatchers.IO) {
        val cleanedClientName = clientName.trim().ifBlank { "Cliente sem nome" }
        val existingClient = clientDao.findByName(cleanedClientName)
        val client = if (existingClient == null) {
            val id = clientDao.insert(ClientEntity(name = cleanedClientName))
            ClientEntity(id = id, name = cleanedClientName)
        } else {
            existingClient
        }

        val profit = PricingRules.saleProfit(saleValue, productCost, if (giftApplied) giftValue else 0.0)
        saleDao.insert(
            SaleEntity(
                productId = productId,
                productName = productName.trim(),
                productType = productType,
                clientId = client.id.takeIf { it > 0 },
                clientName = cleanedClientName,
                saleValue = saleValue,
                productCost = productCost,
                profit = profit,
                giftApplied = giftApplied,
                giftValue = if (giftApplied) giftValue else 0.0,
                paymentMethod = paymentMethod,
                notes = notes.trim(),
                soldAt = System.currentTimeMillis()
            )
        )

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

    fun observeClientGiftMessage(clientName: String): Flow<String?> {
        val cleanName = clientName.trim()
        if (cleanName.isBlank()) return kotlinx.coroutines.flow.flowOf(null)
        return clientDao.observeClients().map { clients ->
            clients.firstOrNull { it.name.equals(cleanName, ignoreCase = true) }
                ?.let { GiftRules.loyaltyMessage(it.purchaseCount) }
        }
    }
}
