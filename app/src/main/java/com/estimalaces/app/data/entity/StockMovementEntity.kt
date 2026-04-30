package com.estimalaces.app.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "stock_movements")
data class StockMovementEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val productId: Long,
    val productName: String,
    val type: String,
    val quantity: Int,
    val previousStock: Int,
    val currentStock: Int,
    val note: String = "",
    val createdAt: Long
)
