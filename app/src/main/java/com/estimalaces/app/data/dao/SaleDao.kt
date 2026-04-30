package com.estimalaces.app.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.estimalaces.app.data.entity.SaleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SaleDao {
    @Query("SELECT * FROM sales ORDER BY soldAt DESC")
    fun observeSales(): Flow<List<SaleEntity>>

    @Query("SELECT * FROM sales WHERE soldAt BETWEEN :start AND :end ORDER BY soldAt DESC")
    fun observeSalesBetween(start: Long, end: Long): Flow<List<SaleEntity>>

    @Insert
    suspend fun insert(sale: SaleEntity): Long

    @Query("SELECT COUNT(*) FROM sales WHERE externalOrderId = :externalOrderId AND externalOrderId != ''")
    suspend fun countByExternalOrderId(externalOrderId: String): Int

    @Query("SELECT COALESCE(SUM(saleValue), 0) FROM sales WHERE soldAt BETWEEN :start AND :end")
    fun observeSoldBetween(start: Long, end: Long): Flow<Double>

    @Query("SELECT COALESCE(SUM(profit), 0) FROM sales WHERE soldAt BETWEEN :start AND :end")
    fun observeProfitBetween(start: Long, end: Long): Flow<Double>

    @Query("SELECT COUNT(*) FROM sales WHERE soldAt BETWEEN :start AND :end")
    fun observeCountBetween(start: Long, end: Long): Flow<Int>

    @Query("SELECT productName FROM sales WHERE soldAt BETWEEN :start AND :end GROUP BY productName ORDER BY COUNT(*) DESC LIMIT 1")
    fun observeBestProductBetween(start: Long, end: Long): Flow<String?>

    @Query("SELECT COUNT(*) FROM sales WHERE giftApplied = 1 AND soldAt BETWEEN :start AND :end")
    fun observeGiftCountBetween(start: Long, end: Long): Flow<Int>

    @Query("""
        SELECT sales.soldAt AS date,
               sales.clientName AS client,
               sales.productName AS product,
               sales.productType AS type,
               sales.productCost AS purchaseValue,
               sales.productCost * 2 AS suggestedValue,
               sales.saleValue AS soldValue,
               sales.profit AS profit,
               sales.giftApplied AS giftApplied,
               sales.giftValue AS giftValue,
               sales.giftType AS giftType,
               sales.giftProductName AS giftProduct,
               sales.paymentMethod AS paymentMethod,
               sales.cardFeePercent AS cardFeePercent,
               sales.cardFeeValue AS cardFeeValue,
               sales.notes AS notes
        FROM sales
        ORDER BY sales.soldAt DESC
    """)
    suspend fun exportRows(): List<SaleExportRow>
}
