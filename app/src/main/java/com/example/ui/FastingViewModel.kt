package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.FastingDatabase
import com.example.data.FastingMilestone
import com.example.data.FastingRepository
import com.example.data.FastingSession
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class FastingViewModel(application: Application) : AndroidViewModel(application) {

    private val database = FastingDatabase.getDatabase(application)
    private val repository = FastingRepository(database.fastingDao())

    val activeSession: StateFlow<FastingSession?> = repository.activeSession
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val completedSessions: StateFlow<List<FastingSession>> = repository.completedSessions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Tracks current elapsed fasting time in real-time
    private val _currentElapsedMillis = MutableStateFlow(0L)
    val currentElapsedMillis: StateFlow<Long> = _currentElapsedMillis.asStateFlow()

    private var timerJob: Job? = null

    init {
        // Automatically start/stop timer ticking when active session changes
        viewModelScope.launch {
            activeSession.collect { session ->
                if (session != null) {
                    startTimerTicking(session)
                } else {
                    stopTimerTicking()
                }
            }
        }
    }

    private fun startTimerTicking(session: FastingSession) {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (isActive) {
                val now = System.currentTimeMillis()
                _currentElapsedMillis.value = session.getElapsedMillis(now)
                delay(1000)
            }
        }
    }

    private fun stopTimerTicking() {
        timerJob?.cancel()
        _currentElapsedMillis.value = 0L
    }

    // --- Actions ---

    fun startFasting(startTime: Long = System.currentTimeMillis()) {
        viewModelScope.launch {
            repository.startFasting(startTime)
        }
    }

    fun adjustActiveStartTime(newStartTime: Long) {
        viewModelScope.launch {
            repository.adjustActiveStartTime(newStartTime)
        }
    }

    fun endFastingAndSave(customStartTime: Long? = null, customEndTime: Long? = null) {
        viewModelScope.launch {
            val session = activeSession.value ?: return@launch
            val now = System.currentTimeMillis()
            val finalStart = customStartTime ?: session.startTime
            val finalEnd = customEndTime ?: now

            val finalElapsed = if (customStartTime != null || customEndTime != null) {
                (finalEnd - finalStart).coerceAtLeast(0)
            } else {
                _currentElapsedMillis.value
            }

            val xp = calculateXpForSession(finalElapsed)
            repository.endFasting(now, customStartTime, customEndTime, xp)
        }
    }

    fun saveCompletedSessionDirectly(startTime: Long, endTime: Long) {
        viewModelScope.launch {
            val elapsed = (endTime - startTime).coerceAtLeast(0)
            val xp = calculateXpForSession(elapsed)
            repository.saveCompletedSession(startTime, endTime, xp)
        }
    }

    fun discardActiveSession() {
        viewModelScope.launch {
            repository.discardActiveSession()
        }
    }

    fun deleteSession(session: FastingSession) {
        viewModelScope.launch {
            repository.deleteSession(session)
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            repository.clearHistory()
        }
    }

    // --- Gamification Helpers & Live Calculations ---

    /**
     * Calculates the XP earned for a fasting duration.
     * - Base XP: 10 XP per hour
     * - Milestone bonus: 20 XP for every milestone completed
     */
    fun calculateXpForSession(elapsedMillis: Long): Int {
        val hours = elapsedMillis.toDouble() / (1000.0 * 60.0 * 60.0)
        val baseXp = (hours * 10.0).toInt()
        val milestoneBonus = FastingMilestone.getCompletedMilestonesCount(hours) * 20
        return baseXp + milestoneBonus
    }

    // Live level statistics exposed to Compose
    val userLevelStats: StateFlow<UserLevelStats> = completedSessions
        .map { sessions ->
            val totalXp = sessions.sumOf { it.xpEarned }
            val level = 1 + (totalXp / 100)
            val xpInCurrentLevel = totalXp % 100
            val levelProgress = xpInCurrentLevel.toFloat() / 100f
            val streak = calculateStreak(sessions)

            UserLevelStats(
                level = level,
                totalXp = totalXp,
                xpInCurrentLevel = xpInCurrentLevel,
                xpForNextLevel = 100,
                levelProgress = levelProgress,
                streakDays = streak
            )
        }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            UserLevelStats(1, 0, 0, 100, 0f, 0)
        )

    private fun calculateStreak(sessions: List<FastingSession>): Int {
        if (sessions.isEmpty()) return 0
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        // Get unique fasting days where a session was completed, sorted descending
        val fastingDays = sessions
            .map { sdf.format(Date(it.endTime ?: 0)) }
            .distinct()
            .sortedDescending()

        if (fastingDays.isEmpty()) return 0

        val todayStr = sdf.format(Date())
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -1)
        val yesterdayStr = sdf.format(cal.time)

        // If the most recent fasting day is neither today nor yesterday, streak is broken
        val mostRecent = fastingDays[0]
        if (mostRecent != todayStr && mostRecent != yesterdayStr) {
            return 0
        }

        var streak = 1
        val currentDayCal = Calendar.getInstance()
        val date = sdf.parse(mostRecent) ?: return 1
        currentDayCal.time = date

        for (i in 1 until fastingDays.size) {
            currentDayCal.add(Calendar.DAY_OF_YEAR, -1)
            val expectedPrevDayStr = sdf.format(currentDayCal.time)
            if (fastingDays[i] == expectedPrevDayStr) {
                streak++
            } else {
                break // Gap found
            }
        }
        return streak
    }
}

data class UserLevelStats(
    val level: Int,
    val totalXp: Int,
    val xpInCurrentLevel: Int,
    val xpForNextLevel: Int,
    val levelProgress: Float,
    val streakDays: Int
) {
    val title: String
        get() = when {
            level < 3 -> "Novice Celldetoxer"
            level < 6 -> "Lipid Burner"
            level < 10 -> "Autophagy Master"
            level < 15 -> "Metabolic Sage"
            else -> "Immune Alchemist"
        }
}
