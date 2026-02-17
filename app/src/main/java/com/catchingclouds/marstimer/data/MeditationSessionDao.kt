package com.catchingclouds.marstimer.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface MeditationSessionDao {

    @Insert
    suspend fun insertSession(session: MeditationSession)

    @Query("SELECT * FROM meditation_sessions ORDER BY date DESC")
    fun getAllSessions(): Flow<List<MeditationSession>>

    @Query("SELECT SUM(duration) FROM meditation_sessions")
    fun getTotalMeditationTime(): Flow<Long?>

    @Query("SELECT COUNT(*) FROM meditation_sessions")
    suspend fun getSessionsCount(): Int
}
