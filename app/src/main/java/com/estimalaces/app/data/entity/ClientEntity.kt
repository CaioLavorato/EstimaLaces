package com.estimalaces.app.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "clients",
    indices = [Index(value = ["name"], unique = true)]
)
data class ClientEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val purchaseCount: Int = 0,
    val lastPurchaseAt: Long? = null,
    val notes: String = ""
)
