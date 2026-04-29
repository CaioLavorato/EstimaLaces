package com.estimalaces.app.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.estimalaces.app.data.entity.GoalEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GoalDao {
    @Query("SELECT * FROM goals WHERE id = 1")
    fun observeGoal(): Flow<GoalEntity?>

    @Query("SELECT * FROM goals WHERE id = 1")
    suspend fun getGoal(): GoalEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun save(goal: GoalEntity)
}
