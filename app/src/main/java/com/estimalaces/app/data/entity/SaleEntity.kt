package com.estimalaces.app.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sales")
data class SaleEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val productId: Long?,
    val productName: String,
    val productType: String,
    val clientId: Long?,
    val clientName: String,
    val saleValue: Double,
    val productCost: Double,
    val profit: Double,
    val giftApplied: Boolean,
    val giftValue: Double,
    val giftType: String = "VALOR",
    val giftProductId: Long? = null,
    val giftProductName: String = "",
    val paymentMethod: String,
    val cardFeePercent: Double = 0.0,
    val cardFeeValue: Double = 0.0,
    val notes: String,
    val soldAt: Long,
    val externalOrderId: String = "",
    val source: String = "APP"
)
