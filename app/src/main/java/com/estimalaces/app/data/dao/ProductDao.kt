package com.estimalaces.app.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.estimalaces.app.data.entity.ProductEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {
    @Query("SELECT * FROM products ORDER BY createdAt DESC")
    fun observeProducts(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE currentQuantity <= minimumQuantity ORDER BY currentQuantity ASC, name ASC")
    fun observeLowStockProducts(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE id = :id")
    suspend fun findById(id: Long): ProductEntity?

    @Query("SELECT * FROM products WHERE name = :name LIMIT 1")
    suspend fun findByName(name: String): ProductEntity?

    @Query("SELECT * FROM products")
    suspend fun getAllProducts(): List<ProductEntity>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(product: ProductEntity): Long

    @Update
    suspend fun update(product: ProductEntity)

    @Query("UPDATE products SET currentQuantity = currentQuantity + :amount WHERE id = :id")
    suspend fun addStock(id: Long, amount: Int)

    @Query("UPDATE products SET currentQuantity = CASE WHEN currentQuantity > 0 THEN currentQuantity - 1 ELSE 0 END WHERE id = :id")
    suspend fun decrementStock(id: Long)

    @Query("SELECT COALESCE(SUM(purchaseValue), 0) FROM products WHERE createdAt BETWEEN :start AND :end")
    fun observeSpentBetween(start: Long, end: Long): Flow<Double>
}
