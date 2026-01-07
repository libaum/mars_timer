package com.cloudcatcher.marstimer

import android.app.Application
import com.cloudcatcher.marstimer.data.AppDatabase
import com.cloudcatcher.marstimer.data.MeditationRepository

class MarsTimerApplication : Application() {
    val database by lazy { AppDatabase.getDatabase(this) }
    val repository by lazy { MeditationRepository(database.meditationSessionDao()) }
}
