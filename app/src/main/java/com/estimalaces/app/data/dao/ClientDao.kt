package com.estimalaces.app.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.estimalaces.app.data.entity.ClientEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ClientDao {
    @Query("SELECT * FROM clients ORDER BY purchaseCount DESC, name ASC")
    fun observeClients(): Flow<List<ClientEntity>>

    @Query("SELECT * FROM clients WHERE name = :name LIMIT 1")
    suspend fun findByName(name: String): ClientEntity?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(client: ClientEntity): Long

    @Update
    suspend fun update(client: ClientEntity)

    @Query("SELECT COUNT(*) FROM clients WHERE purchaseCount >= 2")
    fun observeRecurringCount(): Flow<Int>
}
