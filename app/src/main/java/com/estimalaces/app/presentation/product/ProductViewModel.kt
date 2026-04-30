package com.estimalaces.app.presentation.product

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.estimalaces.app.data.repository.EstimaLacesRepository
import com.estimalaces.app.domain.rules.PricingRules
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ProductViewModel(private val repository: EstimaLacesRepository) : ViewModel() {
    val products = repository.observeProducts()
    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    fun suggestedValue(purchaseValue: Double): Double = PricingRules.suggestedSaleValue(purchaseValue)
    fun expectedProfit(purchaseValue: Double): Double = PricingRules.expectedProfit(purchaseValue)

    fun save(
        name: String,
        type: String,
        purchaseValue: Double,
        supplier: String,
        notes: String,
        currentQuantity: Int,
        minimumQuantity: Int
    ) {
        if (name.isBlank() || purchaseValue <= 0.0) return
        viewModelScope.launch {
            repository.addProduct(name, type, purchaseValue, supplier, notes, currentQuantity, minimumQuantity)
        }
    }

    fun addStock(productId: Long, amount: Int) {
        viewModelScope.launch {
            repository.addStock(productId, amount)
        }
    }

    fun onReplenishStock(productId: Long, quantity: Int, note: String?) {
        if (quantity <= 0) {
            _message.value = "Informe uma quantidade valida"
            return
        }
        viewModelScope.launch {
            runCatching {
                repository.replenishStock(productId, quantity, note)
            }.onSuccess {
                _message.value = "Estoque reposto com sucesso"
            }.onFailure { error ->
                _message.value = error.message ?: "Produto nao encontrado"
            }
        }
    }

    fun clearMessage() {
        _message.value = null
    }
}
