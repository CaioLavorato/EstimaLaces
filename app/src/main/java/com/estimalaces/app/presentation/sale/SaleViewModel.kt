package com.estimalaces.app.presentation.sale

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.estimalaces.app.data.entity.ProductEntity
import com.estimalaces.app.data.repository.EstimaLacesRepository
import com.estimalaces.app.domain.rules.GiftRules
import com.estimalaces.app.domain.rules.PricingRules
import kotlinx.coroutines.launch

class SaleViewModel(private val repository: EstimaLacesRepository) : ViewModel() {
    val products = repository.observeProducts()
    val clients = repository.observeClients()

    fun alert(saleValue: Double, cost: Double, giftValue: Double): String? {
        return PricingRules.saleAlert(saleValue, cost, giftValue)
    }

    fun clientMessage(name: String, purchaseCount: Int?): String? {
        if (name.isBlank()) return null
        return GiftRules.loyaltyMessage(purchaseCount ?: 0)
    }

    fun save(
        product: ProductEntity?,
        manualProductName: String,
        clientName: String,
        saleValue: Double,
        cost: Double,
        giftApplied: Boolean,
        giftValue: Double,
        payment: String,
        notes: String
    ) {
        val finalProductName = product?.name ?: manualProductName
        if (finalProductName.isBlank() || saleValue <= 0.0) return
        viewModelScope.launch {
            repository.registerSale(
                productId = product?.id,
                productName = finalProductName,
                productType = product?.type ?: "lace",
                clientName = clientName,
                saleValue = saleValue,
                productCost = cost,
                giftApplied = giftApplied,
                giftValue = giftValue,
                paymentMethod = payment,
                notes = notes
            )
        }
    }
}
