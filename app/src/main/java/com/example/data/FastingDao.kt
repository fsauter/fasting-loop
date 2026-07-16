package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface FastingDao {
    @Query("SELECT * FROM fasting_sessions WHERE endTime IS NULL LIMIT 1")
    fun getActiveSessionFlow(): Flow<FastingSession?>

    @Query("SELECT * FROM fasting_sessions WHERE endTime IS NULL LIMIT 1")
    suspend fun getActiveSession(): FastingSession?

    @Query("SELECT * FROM fasting_sessions WHERE endTime IS NOT NULL ORDER BY endTime DESC")
    fun getAllCompletedSessionsFlow(): Flow<List<FastingSession>>

    @Query("SELECT * FROM fasting_sessions WHERE endTime IS NOT NULL ORDER BY endTime DESC")
    suspend fun getAllCompletedSessions(): List<FastingSession>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: FastingSession): Long

    @Update
    suspend fun updateSession(session: FastingSession)

    @Delete
    suspend fun deleteSession(session: FastingSession)

    @Query("DELETE FROM fasting_sessions")
    suspend fun clearAll()
}
