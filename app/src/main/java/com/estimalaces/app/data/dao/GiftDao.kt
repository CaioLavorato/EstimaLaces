package com.estimalaces.app.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.estimalaces.app.data.entity.GiftEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GiftDao {
    @Query("SELECT * FROM gifts WHERE used = 0 ORDER BY createdAt DESC")
    fun observeOpenGifts(): Flow<List<GiftEntity>>

    @Insert
    suspend fun insert(gift: GiftEntity): Long
}
