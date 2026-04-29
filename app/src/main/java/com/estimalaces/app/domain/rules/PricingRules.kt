package com.estimalaces.app.domain.rules

object PricingRules {
    fun suggestedSaleValue(purchaseValue: Double): Double = purchaseValue * 2

    fun expectedProfit(purchaseValue: Double): Double = suggestedSaleValue(purchaseValue) - purchaseValue

    fun marginPercent(purchaseValue: Double, saleValue: Double): Double {
        if (purchaseValue <= 0.0) return 0.0
        return ((saleValue - purchaseValue) / purchaseValue) * 100
    }

    fun saleProfit(saleValue: Double, productCost: Double, giftValue: Double): Double {
        return saleValue - productCost - giftValue
    }

    fun saleAlert(saleValue: Double, productCost: Double, giftValue: Double): String? {
        val profit = saleProfit(saleValue, productCost, giftValue)
        return when {
            profit < 0.0 -> "Venda com prejuizo. Revise o preco antes de confirmar."
            saleValue < suggestedSaleValue(productCost) -> "Venda abaixo da margem ideal de 100%."
            else -> null
        }
    }
}
