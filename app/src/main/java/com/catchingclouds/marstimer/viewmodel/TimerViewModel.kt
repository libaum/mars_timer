package com.catchingclouds.marstimer.viewmodel

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.catchingclouds.marstimer.data.MeditationSession
import com.catchingclouds.marstimer.data.MeditationSessionDao
import com.catchingclouds.marstimer.service.TimerService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

import com.catchingclouds.marstimer.UserPreferences
import kotlinx.coroutines.flow.first

class TimerViewModel(
    private val dao: MeditationSessionDao,
    private val userPreferences: UserPreferences
) : ViewModel() {

    enum class TimerState {
        IDLE,
        PREP,
        RUNNING,
        PAUSED,
        FINISHED
    }

    // Initialize uiState with a concrete non-zero remainingTime so the UI doesn't show 0:00
    private val defaultMinutes = 20
    private val _uiState = MutableStateFlow(
        TimerUiState(
            meditationTime = defaultMinutes,
            prepTime = 0,
            remainingTime = defaultMinutes * 60 * 1000L,
            timerState = TimerViewModel.TimerState.IDLE,
            wasPausedDuringPrep = false
        )
    )
    val uiState: StateFlow<TimerUiState> = _uiState

    // Statistics
    val totalMinutes: StateFlow<Long> = dao.getTotalMeditationTime()
        .map { (it ?: 0L) / 60 } // Convert seconds to minutes, handling null
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    val sessionHistory: StateFlow<List<MeditationSession>> = dao.getAllSessions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            val lastDuration = userPreferences.lastDuration.first()
            val initialDuration = if (lastDuration > 0) lastDuration else 20
            _uiState.value = _uiState.value.copy(
                meditationTime = initialDuration,
                remainingTime = initialDuration * 60 * 1000L
            )
        }
    }



    private var timerService: TimerService? = null
    private var isBound = false

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            val binder = service as TimerService.TimerBinder
            timerService = binder.getService()
            isBound = true

            viewModelScope.launch {
                timerService?.timerState?.collectLatest { state ->
                    val previousState = _uiState.value.timerState
                    _uiState.value = _uiState.value.copy(timerState = state)

                    // Check for completion to save session
                    if (previousState == TimerState.RUNNING && state == TimerState.FINISHED) {
                        saveSession()
                    }
                }
            }
            viewModelScope.launch {
                timerService?.remainingTime?.collectLatest { time ->
                    // Ignore initial 0 value from the service when it's in IDLE state
                    val serviceState = timerService?.timerState?.value
                    if (time > 0 || serviceState != TimerViewModel.TimerState.IDLE) {
                        _uiState.value = _uiState.value.copy(remainingTime = time)
                    }
                }
            }
            viewModelScope.launch {
                timerService?.wasPausedDuringPrep?.collectLatest { wasDuringPrep ->
                    _uiState.value = _uiState.value.copy(wasPausedDuringPrep = wasDuringPrep)
                }
            }
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            isBound = false
        }
    }

    private fun saveSession() {
        val durationSeconds = (_uiState.value.meditationTime * 60).toLong()
        // Only save if duration is meaningful (e.g. > 1 minute? or always?)
        // Let's save always for now.
        val session = MeditationSession(
            date = System.currentTimeMillis(),
            duration = durationSeconds
        )
        viewModelScope.launch {
            dao.insertSession(session)
        }
    }

    fun bindService(context: Context) {
        Intent(context, TimerService::class.java).also { intent ->
            context.bindService(intent, connection, Context.BIND_AUTO_CREATE)
        }
    }

    fun unbindService(context: Context) {
        if (isBound) {
            context.unbindService(connection)
            isBound = false
        }
    }

    fun startTimer() {
        if (isBound) {
            val meditationTime = _uiState.value.meditationTime * 60L
            val prepTime = _uiState.value.prepTime.toLong()
            timerService?.startTimer(meditationTime, prepTime)
        }
    }

    fun stopTimer() {
        if (isBound) {
            timerService?.stopTimer()
            // Reset to initial time
            val initialTime = _uiState.value.meditationTime * 60 * 1000L
            _uiState.value = _uiState.value.copy(remainingTime = initialTime)
        }
    }

    fun pauseTimer() {
        if (isBound) {
            timerService?.pauseTimer()
        }
    }

    fun resumeTimer() {
        if (isBound) {
            timerService?.resumeTimer()
        }
    }

    fun savePartialSession() {
        if (isBound) {
            val totalScheduled = _uiState.value.meditationTime * 60L // seconds
            // We need to know explicitly the remaining time from the Service, but uiState.remainingTime should have it (updated by flow).
            // But uiState.remainingTime is in millis.
            val remainingSeconds = (_uiState.value.remainingTime / 1000)
            val elapsedSeconds = (totalScheduled - remainingSeconds).coerceAtLeast(0)

            if (elapsedSeconds > 0) {
                 val session = MeditationSession(
                    date = System.currentTimeMillis(),
                    duration = elapsedSeconds
                )
                viewModelScope.launch {
                    dao.insertSession(session)
                }
            }
            stopTimer()
        }
    }

    fun setMeditationTime(minutes: Int) {
        val newTime = minutes.coerceIn(1, 180) // Cap at reasonable limits
        _uiState.value = _uiState.value.copy(meditationTime = newTime, remainingTime = newTime * 60 * 1000L)
        saveMeditationTime(newTime)
    }

    private fun saveMeditationTime(minutes: Int) {
        viewModelScope.launch {
            userPreferences.saveLastDuration(minutes)
        }
    }

    // Refactored increment/decrement to use setMeditationTime
    fun incrementMeditationTime() {
        setMeditationTime(_uiState.value.meditationTime + 1)
    }

    fun decrementMeditationTime() {
        setMeditationTime(_uiState.value.meditationTime - 1)
    }

    fun incrementPrepTime() {
        _uiState.value = _uiState.value.copy(prepTime = _uiState.value.prepTime + 5)
    }

    fun decrementPrepTime() {
        _uiState.value = _uiState.value.copy(prepTime = (_uiState.value.prepTime - 5).coerceAtLeast(0))
    }
}

class TimerViewModelFactory(
    private val dao: MeditationSessionDao,
    private val userPreferences: UserPreferences
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TimerViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TimerViewModel(dao, userPreferences) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

data class TimerUiState(
    val meditationTime: Int = 20, // in minutes
    val prepTime: Int = 0, // in seconds
    val remainingTime: Long = meditationTime * 60 * 1000L,
    val timerState: TimerViewModel.TimerState = TimerViewModel.TimerState.IDLE,
    val wasPausedDuringPrep: Boolean = false
)
