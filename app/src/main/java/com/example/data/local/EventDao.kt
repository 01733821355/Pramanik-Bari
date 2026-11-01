package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface EventDao {
    @Query("SELECT * FROM family_events ORDER BY date DESC, updatedAt DESC")
    fun getAllEventsFlow(): Flow<List<EventEntity>>

    @Query("SELECT * FROM family_events ORDER BY date DESC, updatedAt DESC")
    suspend fun getAllEvents(): List<EventEntity>

    @Query("SELECT * FROM family_events WHERE id = :id LIMIT 1")
    suspend fun getEventById(id: String): EventEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(event: EventEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(events: List<EventEntity>)

    @Query("DELETE FROM family_events WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM family_events")
    suspend fun clearAll()
}
