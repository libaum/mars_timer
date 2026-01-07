package com.cloudcatcher.marstimer.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class MeditationRepository(private val meditationSessionDao: MeditationSessionDao) {

    fun getAllSessions(): Flow<List<MeditationSession>> {
        return meditationSessionDao.getAllSessions()
    }

    fun getTotalMeditationTime(): Flow<Long> {
        return meditationSessionDao.getTotalMeditationTime().map { it ?: 0L }
    }

    suspend fun insertSession(session: MeditationSession) {
        meditationSessionDao.insertSession(session)
    }
}
