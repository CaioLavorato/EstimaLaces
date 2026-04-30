package com.estimalaces.app.domain.rules

object PricingRules {
    fun suggestedSaleValue(purchaseValue: Double): Double = purchaseValue * 2

    fun expectedProfit(purchaseValue: Double): Double = suggestedSaleValue(purchaseValue) - purchaseValue

    fun marginPercent(purchaseValue: Double, saleValue: Double): Double {
        if (purchaseValue <= 0.0) return 0.0
        return ((saleValue - purchaseValue) / purchaseValue) * 100
    }

    fun cardFeeValue(saleValue: Double, cardFeePercent: Double): Double {
        if (cardFeePercent <= 0.0) return 0.0
        return saleValue * (cardFeePercent / 100.0)
    }

    fun saleProfit(
        saleValue: Double,
        productCost: Double,
        giftValue: Double,
        cardFeePercent: Double = 0.0
    ): Double {
        return saleValue - productCost - giftValue - cardFeeValue(saleValue, cardFeePercent)
    }

    fun saleAlert(
        saleValue: Double,
        productCost: Double,
        giftValue: Double,
        cardFeePercent: Double = 0.0
    ): String? {
        val profit = saleProfit(saleValue, productCost, giftValue, cardFeePercent)
        return when {
            profit < 0.0 -> "Venda com prejuízo. Revise o preço antes de confirmar."
            saleValue < suggestedSaleValue(productCost) -> "Venda abaixo da margem ideal de 100%."
            else -> null
        }
    }
}
