package com.estimalaces.app.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.estimalaces.app.data.entity.ProductEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {
    @Query("SELECT * FROM products ORDER BY createdAt DESC")
    fun observeProducts(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE id = :id")
    suspend fun findById(id: Long): ProductEntity?

    @Insert
    suspend fun insert(product: ProductEntity): Long

    @Query("SELECT COALESCE(SUM(purchaseValue), 0) FROM products WHERE createdAt BETWEEN :start AND :end")
    fun observeSpentBetween(start: Long, end: Long): Flow<Double>
}
