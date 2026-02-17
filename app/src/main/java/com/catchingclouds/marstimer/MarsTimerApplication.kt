package com.catchingclouds.marstimer

import android.app.Application
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.catchingclouds.marstimer.data.AppDatabase
import com.catchingclouds.marstimer.data.MeditationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

class UserPreferencesRepository(private val context: Context) : UserPreferences {
    private val LAST_DURATION_KEY = intPreferencesKey("last_duration")

    override val lastDuration: Flow<Int> = context.dataStore.data
        .map { preferences ->
            preferences[LAST_DURATION_KEY] ?: 20 // Default 20
        }

    override suspend fun saveLastDuration(minutes: Int) {
        context.dataStore.edit { preferences ->
            preferences[LAST_DURATION_KEY] = minutes
        }
    }
}

class MarsTimerApplication : Application() {
    val database by lazy { AppDatabase.getDatabase(this) }
    val repository by lazy { MeditationRepository(database.meditationSessionDao()) }
    val userPreferencesRepository by lazy { UserPreferencesRepository(this) }
}
