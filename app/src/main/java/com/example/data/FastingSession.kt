package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "fasting_sessions")
data class FastingSession(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val startTime: Long,                         // Timestamp of initial session start
    val endTime: Long? = null,                   // Timestamp of session end (null if active/paused)
    val xpEarned: Int = 0,                       // XP awarded for this session
    val isCompleted: Boolean = false             // Whether the session was successfully saved
) {
    /**
     * Calculates the total elapsed fasting duration in milliseconds.
     */
    fun getElapsedMillis(currentTime: Long): Long {
        return if (endTime != null) {
            (endTime - startTime).coerceAtLeast(0)
        } else {
            (currentTime - startTime).coerceAtLeast(0)
        }
    }
}
