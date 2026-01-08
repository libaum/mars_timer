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
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.windowInsetsPadding
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
                    onLongClick = { showStats = false }
                )
        ) {
            StatisticsScreen(viewModel = timerViewModel)
        }
    } else {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 24.dp) // Manual padding or use statusBarsPadding
                // User said: Touch Surface must not go into Status Bar.
                // We use statusBarsPadding() to push the content down, but we want the background to be black.
                // The Surface in MainActivity is black.
                // So we apply statusBarsPadding to the Box that has the Clickable modifier.
                .statusBarsPadding()
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
                        if (isIdle) {
                            showStats = true
                        }
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
                        .align(BiasAlignment(0f, -0.6f))
                        .padding(bottom = 16.dp)
                )
            }

            // 2. Reference for Time Position
            val timeBias = -0.3f

            // Time Display & Adjustments
            if (isIdle) {
                Row(
                    modifier = Modifier
                        .align(BiasAlignment(0f, timeBias))
                        .padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    // Minus
                    IconButton(
                        onClick = { timerViewModel.decrementMeditationTime() },
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text("-", style = MaterialTheme.typography.displayMedium, color = MaterialTheme.colorScheme.onBackground)
                    }

                    // Use Monospace to prevent jitter
                    Text(
                        text = formatTime(uiState.remainingTime),
                        style = MaterialTheme.typography.displayLarge.copy(
                            // fontFeatureSettings = "tnum" // Monospace includes this implicitly
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                        ),
                        color = MaterialTheme.colorScheme.onBackground,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.width(200.dp) // Fixed width container
                    )

                    // Plus
                    IconButton(
                        onClick = { timerViewModel.incrementMeditationTime() },
                        modifier = Modifier.padding(start = 8.dp)
                    ) {
                        Text("+", style = MaterialTheme.typography.displayMedium, color = MaterialTheme.colorScheme.onBackground)
                    }
                }

                // Quick Select Buttons
                Row(
                    modifier = Modifier
                        .align(BiasAlignment(0f, timeBias + 0.3f))
                        .padding(top = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    val quickTimes = listOf(5, 10, 15, 20)
                    quickTimes.forEach { time ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .clickable { timerViewModel.setMeditationTime(time) }
                                // "unauffällige buttons" -> maybe just text with padding?
                                .padding(12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "$time",
                                style = MaterialTheme.typography.titleLarge,
                                color = if (uiState.meditationTime == time) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Prep Time Control - Larger and Higher
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 120.dp), // Lifted higher as requested
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Prep", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = "-",
                        modifier = Modifier
                            .clickable { timerViewModel.decrementPrepTime() }
                            .padding(12.dp),
                        style = MaterialTheme.typography.headlineSmall, // Larger
                        color = MaterialTheme.colorScheme.onBackground
                    )
                     Text(
                         "${uiState.prepTime}s",
                         style = MaterialTheme.typography.titleLarge, // Larger
                         color = MaterialTheme.colorScheme.onBackground
                     )
                     Text(
                        text = "+",
                        modifier = Modifier
                            .clickable { timerViewModel.incrementPrepTime() }
                            .padding(12.dp),
                        style = MaterialTheme.typography.headlineSmall, // Larger
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

            } else {
                // Running/Paused
                // Running/Paused
                 Text(
                    text = formatTime(uiState.remainingTime),
                    style = MaterialTheme.typography.displayLarge.copy(
                        // fontFeatureSettings = "tnum"
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                    ),
                    // Visual distinction for Warmup (Prep)
                    color = if (isWarmup) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .align(BiasAlignment(0f, timeBias))
                        // .padding(24.dp) // REMOVED: caused vertical shift compared to Idle state
                        .width(200.dp) // Fixed width
                )
            }

            // 3. Controls (Save/Clear)
            if (isPaused) {
                // Calculate elapsed meditation time for save button visibility
                val totalMeditationMs = uiState.meditationTime * 60 * 1000L
                val elapsedMeditationTime = if (!uiState.wasPausedDuringPrep) {
                    totalMeditationMs - uiState.remainingTime
                } else {
                    0L
                }
                val canSave = !uiState.wasPausedDuringPrep && elapsedMeditationTime > 0

                Row(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 100.dp),
                    horizontalArrangement = Arrangement.spacedBy(32.dp)
                ) {
                     TextButton(onClick = { timerViewModel.stopTimer() }) {
                        Text(
                            text = "clear",
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }

                    // Only show save if meditation time has actually elapsed
                    if (canSave) {
                        TextButton(onClick = { timerViewModel.savePartialSession() }) {
                            Text(
                                text = "save",
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
    // Round up to the nearest second to show "5" for [5000, 4001] range
    val roundedMillis = if (millis > 0) millis + 999 else 0
    val minutes = TimeUnit.MILLISECONDS.toMinutes(roundedMillis)
    val seconds = TimeUnit.MILLISECONDS.toSeconds(roundedMillis) - TimeUnit.MINUTES.toSeconds(minutes)
    return String.format("%02d:%02d", minutes, seconds)
}

@Preview(showBackground = true)
@Composable
fun TimerScreenPreview() {
    TimerScreen()
}