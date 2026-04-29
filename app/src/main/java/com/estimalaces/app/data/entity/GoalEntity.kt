package com.estimalaces.app.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "goals")
data class GoalEntity(
    @PrimaryKey val id: Long = 1,
    val monthlyGoal: Double,
    val maxGiftValue: Double,
    val updatedAt: Long
)
