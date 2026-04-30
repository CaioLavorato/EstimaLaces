package com.estimalaces.app.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.estimalaces.app.data.entity.StockMovementEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StockMovementDao {
    @Insert
    suspend fun insert(movement: StockMovementEntity): Long

    @Query("SELECT * FROM stock_movements ORDER BY createdAt DESC")
    fun observeMovements(): Flow<List<StockMovementEntity>>

    @Query("SELECT * FROM stock_movements WHERE productId = :productId ORDER BY createdAt DESC")
    fun observeByProduct(productId: Long): Flow<List<StockMovementEntity>>
}
