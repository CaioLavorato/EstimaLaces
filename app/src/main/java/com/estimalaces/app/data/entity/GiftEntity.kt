package com.estimalaces.app.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "gifts")
data class GiftEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val clientId: Long?,
    val clientName: String,
    val reason: String,
    val valueLimit: Double,
    val used: Boolean = false,
    val createdAt: Long
)
