package com.cloudcatcher.marstimer

import android.Manifest


import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.cloudcatcher.marstimer.ui.TimerScreen
import com.cloudcatcher.marstimer.ui.theme.MarsTimerTheme
import com.cloudcatcher.marstimer.viewmodel.TimerViewModel
import com.cloudcatcher.marstimer.viewmodel.TimerViewModelFactory

import androidx.activity.SystemBarStyle


import android.graphics.Color

import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat



class MainActivity : ComponentActivity() {

    private val timerViewModel: TimerViewModel by viewModels {
val app = application as MarsTimerApplication

TimerViewModelFactory(app.database.meditationSessionDao(), app.userPreferencesRepository)

    }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                // Permission is granted. Continue the action or workflow in your
                // app.
            } else {
                // Explain to the user that the feature is unavailable because the
                // features requires a permission that the user has denied. At the
                // same time, respect the user's decision. Don't link to system
                // settings in an effort to convince the user to change their
                // decision.
            }
        }
    override fun onCreate(savedInstanceState: Bundle?) {
setTheme(R.style.Theme_MarsTimer)

        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            // light(...) means "Light Bar" -> Dark Icons. We want dark icons on black background (invisible).
            statusBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT)
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        timerViewModel.bindService(this)

        setContent {
            MarsTimerTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    TimerScreen(timerViewModel)
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        timerViewModel.unbindService(this)
    }
}
