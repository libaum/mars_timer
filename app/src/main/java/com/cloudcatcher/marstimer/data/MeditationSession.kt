package com.cloudcatcher.marstimer.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "meditation_sessions")
data class MeditationSession(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val date: Long,
    val duration: Long // in seconds
)
