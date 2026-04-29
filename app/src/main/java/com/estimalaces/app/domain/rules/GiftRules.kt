package com.estimalaces.app.domain.rules

object GiftRules {
    const val PURCHASES_FOR_GIFT = 3

    fun purchasesUntilGift(purchaseCount: Int): Int {
        val remainder = purchaseCount % PURCHASES_FOR_GIFT
        return if (remainder == 0 && purchaseCount > 0) 0 else PURCHASES_FOR_GIFT - remainder
    }

    fun loyaltyMessage(purchaseCount: Int): String {
        val missing = purchasesUntilGift(purchaseCount)
        return if (missing == 0) {
            "Cliente atingiu 3 compras. Brinde liberado!"
        } else {
            "Cliente recorrente: ${PURCHASES_FOR_GIFT - missing} de 3 compras para brinde"
        }
    }

    fun shouldReleaseLoyaltyGift(purchaseCountAfterSale: Int): Boolean {
        return purchaseCountAfterSale > 0 && purchaseCountAfterSale % PURCHASES_FOR_GIFT == 0
    }
}
