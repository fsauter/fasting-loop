package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class FastingRepository(private val fastingDao: FastingDao) {

    val activeSession: Flow<FastingSession?> = fastingDao.getActiveSessionFlow()
    val completedSessions: Flow<List<FastingSession>> = fastingDao.getAllCompletedSessionsFlow()

    suspend fun startFasting(startTime: Long) = withContext(Dispatchers.IO) {
        // First, check if there is an existing active session. If so, end it or discard it.
        val existing = fastingDao.getActiveSession()
        if (existing != null) {
            fastingDao.deleteSession(existing)
        }
        val session = FastingSession(
            startTime = startTime
        )
        fastingDao.insertSession(session)
    }

    suspend fun adjustActiveStartTime(newStartTime: Long) = withContext(Dispatchers.IO) {
        val active = fastingDao.getActiveSession() ?: return@withContext
        val updated = active.copy(
            startTime = newStartTime
        )
        fastingDao.updateSession(updated)
    }

    suspend fun saveCompletedSession(
        startTime: Long,
        endTime: Long,
        xpEarned: Int
    ) = withContext(Dispatchers.IO) {
        // This is for direct entry or replacing the active session
        val active = fastingDao.getActiveSession()
        if (active != null) {
            fastingDao.deleteSession(active)
        }
        val completed = FastingSession(
            startTime = startTime,
            endTime = endTime,
            xpEarned = xpEarned,
            isCompleted = true
        )
        fastingDao.insertSession(completed)
    }

    suspend fun endFasting(
        currentTime: Long,
        customStartTime: Long? = null,
        customEndTime: Long? = null,
        xpEarned: Int
    ) = withContext(Dispatchers.IO) {
        val active = fastingDao.getActiveSession() ?: return@withContext
        val finalStart = customStartTime ?: active.startTime
        val finalEnd = customEndTime ?: currentTime

        val completed = active.copy(
            startTime = finalStart,
            endTime = finalEnd,
            xpEarned = xpEarned,
            isCompleted = true
        )
        fastingDao.updateSession(completed)
    }

    suspend fun discardActiveSession() = withContext(Dispatchers.IO) {
        val active = fastingDao.getActiveSession()
        if (active != null) {
            fastingDao.deleteSession(active)
        }
    }

    suspend fun deleteSession(session: FastingSession) = withContext(Dispatchers.IO) {
        fastingDao.deleteSession(session)
    }

    suspend fun clearHistory() = withContext(Dispatchers.IO) {
        fastingDao.clearAll()
    }
}
