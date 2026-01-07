package com.cloudcatcher.marstimer.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.cloudcatcher.marstimer.MainActivity
import com.cloudcatcher.marstimer.R
import com.cloudcatcher.marstimer.viewmodel.TimerViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class TimerService : LifecycleService() {

    private val binder = TimerBinder()
    private var timerJob: Job? = null
    private var wakeLock: PowerManager.WakeLock? = null

    private val _timerState = MutableStateFlow(TimerViewModel.TimerState.IDLE)
    val timerState: StateFlow<TimerViewModel.TimerState> get() = _timerState

    private val _remainingTime = MutableStateFlow(0L)
    val remainingTime: StateFlow<Long> get() = _remainingTime

    override fun onBind(intent: Intent): IBinder {
        super.onBind(intent)
        return binder
    }

    inner class TimerBinder : Binder() {
        fun getService(): TimerService = this@TimerService
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        // TODO: Handle actions like start, pause, stop
        return START_STICKY
    }

    fun startTimer(meditationTime: Long, prepTime: Long) {
        acquireWakeLock()
        _timerState.value = TimerViewModel.TimerState.PREP
        timerJob = lifecycleScope.launch {
            // Prep Time
            var time = prepTime
            while (time > 0) {
                while (_timerState.value == TimerViewModel.TimerState.PAUSED) {
                    delay(100)
                }
                if (_timerState.value == TimerViewModel.TimerState.FINISHED) return@launch

                _remainingTime.value = time * 1000L
                updateNotification(time * 1000L)
                delay(1000)
                time--
            }

            // Play Sound logic (Start of Meditation)
            playSound()

            // Meditation Time
            _timerState.value = TimerViewModel.TimerState.RUNNING
            time = meditationTime
            while (time > 0) {
                 while (_timerState.value == TimerViewModel.TimerState.PAUSED) {
                    delay(100)
                }
                if (_timerState.value == TimerViewModel.TimerState.FINISHED) return@launch

                _remainingTime.value = time * 1000L
                updateNotification(time * 1000L)
                delay(1000)
                time--
            }

            // Play Sound logic (End of Meditation)
            playSound()
            stopTimer()
        }
    }


    private var mediaPlayer: android.media.MediaPlayer? = null

    private fun playSound() {
        try {
            if (mediaPlayer == null) {
                mediaPlayer = android.media.MediaPlayer.create(this, R.raw.singing_bowl)
            }
            mediaPlayer?.start()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private var previousState: TimerViewModel.TimerState = TimerViewModel.TimerState.IDLE

    fun pauseTimer() {
        if (_timerState.value == TimerViewModel.TimerState.RUNNING || _timerState.value == TimerViewModel.TimerState.PREP) {
            previousState = _timerState.value
            _timerState.value = TimerViewModel.TimerState.PAUSED
        }
    }

     fun resumeTimer() {
        if (_timerState.value == TimerViewModel.TimerState.PAUSED) {
            _timerState.value = previousState
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
        mediaPlayer = null
    }

    fun stopTimer() {
        timerJob?.cancel()
        _timerState.value = TimerViewModel.TimerState.FINISHED

        // Stop audio immediately
        try {
            if (mediaPlayer?.isPlaying == true) {
                mediaPlayer?.stop()
            }
            mediaPlayer?.release()
            mediaPlayer = null
        } catch (e: Exception) {
            e.printStackTrace()
        }

        releaseWakeLock()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            stopForeground(STOP_FOREGROUND_REMOVE)
        } else {
            @Suppress("DEPRECATION")
            stopForeground(true)
        }
        stopSelf()
    }

    private fun acquireWakeLock() {
        wakeLock =
            (getSystemService(Context.POWER_SERVICE) as PowerManager).run {
                newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "TimerApp::WakeLock").apply {
                    acquire()
                }
            }
    }

    private fun releaseWakeLock() {
        wakeLock?.let {
            if (it.isHeld) {
                it.release()
            }
        }
    }

    private fun updateNotification(remainingTime: Long) {
        val notification = createNotification(remainingTime)
        startForeground(1, notification)
    }

    private fun createNotification(remainingTime: Long) = NotificationCompat.Builder(this, "TIMER_CHANNEL")
        .setContentTitle("Mars Timer")
        .setContentText("Remaining Time: ${formatTime(remainingTime)}")
        .setSmallIcon(R.drawable.ic_launcher_foreground)
        .setContentIntent(pendingIntent())
        .build()

    private fun pendingIntent(): PendingIntent {
        val intent = Intent(this, MainActivity::class.java)
        return PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Timer Notification"
            val descriptionText = "Shows the current timer status"
            val importance = NotificationManager.IMPORTANCE_LOW
            val channel = NotificationChannel("TIMER_CHANNEL", name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun formatTime(millis: Long): String {
        val minutes = TimeUnit.MILLISECONDS.toMinutes(millis)
        val seconds = TimeUnit.MILLISECONDS.toSeconds(millis) - TimeUnit.MINUTES.toSeconds(minutes)
        return String.format("%02d:%02d", minutes, seconds)
    }
}