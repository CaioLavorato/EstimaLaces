package com.estimalaces.app.data.dao

data class SaleExportRow(
    val date: Long,
    val client: String,
    val product: String,
    val type: String,
    val purchaseValue: Double,
    val suggestedValue: Double,
    val soldValue: Double,
    val profit: Double,
    val giftApplied: Boolean,
    val giftValue: Double,
    val paymentMethod: String,
    val notes: String
)
