package com.cloudcatcher.marstimer.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.IconButton
import androidx.compose.material3.TextButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cloudcatcher.marstimer.viewmodel.TimerViewModel
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.delay

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TimerScreen(timerViewModel: TimerViewModel = viewModel()) {
    val uiState by timerViewModel.uiState.collectAsState()
    val timerState = uiState.timerState

    val isIdle = timerState == TimerViewModel.TimerState.IDLE || timerState == TimerViewModel.TimerState.FINISHED
    val isRunning = timerState == TimerViewModel.TimerState.RUNNING || timerState == TimerViewModel.TimerState.PREP
    val isPaused = timerState == TimerViewModel.TimerState.PAUSED
    val isWarmup = timerState == TimerViewModel.TimerState.PREP

    var showStats by remember { mutableStateOf(false) }

    if (showStats) {
        BackHandler { showStats = false }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .combinedClickable(
                    onClick = { showStats = false },
                    onLongClick = { showStats = false } // Double safety?
                )
        ) {
            StatisticsScreen(viewModel = timerViewModel)
        }
    } else {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .combinedClickable(
                    onClick = {
                        if (isIdle) {
                            timerViewModel.startTimer()
                        } else if (isRunning) {
                            timerViewModel.pauseTimer()
                        } else if (isPaused) {
                            timerViewModel.resumeTimer()
                        }
                    },
                    onLongClick = {
                        showStats = true
                    }
                )
        ) {
            // 1. Status Label (Fixed Position)
            if (!isIdle) {
                Text(
                    text = if (isWarmup) "Warmup" else if (isPaused) "Paused" else "Meditation",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSecondary,
                    modifier = Modifier
                        .align(BiasAlignment(0f, -0.5f)) // Fixed above timer
                        .padding(bottom = 16.dp)
                )
            }

            // 2. The Time Display (Fixed Position)
            Text(
                text = formatTime(uiState.remainingTime),
                style = MaterialTheme.typography.displayLarge,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier
                    .align(BiasAlignment(0f, -0.3f)) // Fixed position
                    .padding(24.dp)
            )

            // 3. Controls - Positioned below
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 100.dp), // Push up a bit
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
            if (isIdle) {
                // Settings & Start
                // Settings (Meditation Time)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(vertical = 8.dp)
                ) {
                    Text(
                        text = "-",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier
                            .clickable { timerViewModel.decrementMeditationTime() }
                            .padding(8.dp)
                    )
                    Text(
                        text = "${uiState.meditationTime} m",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier
                            .width(80.dp)
                            .padding(horizontal = 8.dp),
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "+",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier
                            .clickable { timerViewModel.incrementMeditationTime() }
                            .padding(8.dp)
                    )
                }

                // Settings (Prep Time)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(vertical = 8.dp)
                ) {
                    Text(
                        text = "-",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier
                            .clickable { timerViewModel.decrementPrepTime() }
                            .padding(8.dp)
                    )
                    Text(
                        text = "${uiState.prepTime} s",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier
                            .width(80.dp)
                            .padding(horizontal = 8.dp),
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "+",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier
                            .clickable { timerViewModel.incrementPrepTime() }
                            .padding(8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

            } else if (isPaused) {
                // Clear Button (Positioned to match settings height)
                Box(
                    modifier = Modifier.height(180.dp),
                    contentAlignment = Alignment.Center
                ) {
                    TextButton(onClick = { timerViewModel.stopTimer() }) {
                        Text(
                            text = "clear",
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }
            }
        }
        }
    }
}

private fun formatTime(millis: Long): String {
    val minutes = TimeUnit.MILLISECONDS.toMinutes(millis)
    val seconds = TimeUnit.MILLISECONDS.toSeconds(millis) - TimeUnit.MINUTES.toSeconds(minutes)
    return String.format("%02d:%02d", minutes, seconds)
}

@Preview(showBackground = true)
@Composable
fun TimerScreenPreview() {
    TimerScreen()
}