package com.catchingclouds.marstimer

import kotlinx.coroutines.flow.Flow

interface UserPreferences {
    val lastDuration: Flow<Int>
    suspend fun saveLastDuration(duration: Int)
}
