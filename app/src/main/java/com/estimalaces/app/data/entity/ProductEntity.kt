package com.estimalaces.app.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val type: String,
    val purchaseValue: Double,
    val suggestedSaleValue: Double,
    val createdAt: Long,
    val supplier: String = "",
    val notes: String = "",
    val currentQuantity: Int = 1,
    val minimumQuantity: Int = 1
)
