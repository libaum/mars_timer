package com.cloudcatcher.marstimer.ui

import android.graphics.Color
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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.background
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cloudcatcher.marstimer.UserPreferences
import com.cloudcatcher.marstimer.data.MeditationSession
import com.cloudcatcher.marstimer.data.MeditationSessionDao
import com.cloudcatcher.marstimer.ui.theme.Montserrat
import com.cloudcatcher.marstimer.viewmodel.TimerViewModel
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

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
                    text = if (isWarmup) "WARMUP" else if (isPaused) "PAUSED" else "MEDITATION",
                    style = MaterialTheme.typography.titleMedium.copy(
                        letterSpacing = 2.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    color = MaterialTheme.colorScheme.onSecondary,
                    modifier = Modifier
                        .align(BiasAlignment(0f, -0.6f))
                        .padding(bottom = 16.dp)
                )
            }

            // 2. Reference for Time Position
            val timeBias = -0.3f // Slightly centered

            // Time Display & Adjustments
            if (isIdle) {
                // Main Timer Row
                Row(
                    modifier = Modifier
                        .align(BiasAlignment(0f, timeBias))
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically, // Corrected alignment
                    horizontalArrangement = Arrangement.Center
                ) {
                    // Minus
                    Text(
                        text = "-",
                        style = MaterialTheme.typography.displayMedium.copy(
                            fontFamily = Montserrat,
                            fontWeight = FontWeight.Thin
                        ),
                        color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.5f),
                        modifier = Modifier
                            .clickable { timerViewModel.decrementMeditationTime() }
                            .padding(16.dp)
                    )

                    // Timer Display
                    Text(
                        text = formatTime(uiState.remainingTime),
                        style = MaterialTheme.typography.displayLarge.copy(
                            fontSize = 72.sp,
                            fontFamily = Montserrat,
                            fontWeight = FontWeight.Thin, // Corrected to use Thin
                            fontFeatureSettings = "tnum" // Tabular numbers to prevent jitter
                        ),
                        color = androidx.compose.ui.graphics.Color.White,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )

                    // Plus
                    Text(
                        text = "+",
                        style = MaterialTheme.typography.displayMedium.copy(
                            fontFamily = Montserrat,
                            fontWeight = FontWeight.Thin
                        ),
                        color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.5f),
                        modifier = Modifier
                            .clickable { timerViewModel.incrementMeditationTime() }
                            .padding(16.dp)
                    )
                }

                // Quick Select Buttons
                Row(
                    modifier = Modifier
                        .align(BiasAlignment(0f, timeBias + 0.35f)) // Pushed down
                        .padding(top = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    val quickTimes = listOf(5, 10, 15, 20)
                    quickTimes.forEach { time ->
                        val isSelected = uiState.meditationTime == time
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(RoundedCornerShape(50))
                                .clickable { timerViewModel.setMeditationTime(time) }
                                .background(androidx.compose.ui.graphics.Color.Black
                                    //if (isSelected) androidx.compose.ui.graphics.Color.DarkGray.copy(alpha = 0.5f)
                                    //else androidx.compose.ui.graphics.Color.DarkGray.copy(alpha = 0.2f)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "$time",
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontFamily = Montserrat,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 16.sp,
                                    fontFeatureSettings = "tnum"
                                ),
                                color = if (isSelected) androidx.compose.ui.graphics.Color.White else androidx.compose.ui.graphics.Color.White.copy(alpha = 0.6f)
                            )
                        }
                    }
                }

                // Prep Time Control - Subtle & Low
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 72.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "PREP TIME",
                        style = MaterialTheme.typography.labelMedium.copy(
                            letterSpacing = 2.sp,
                            fontWeight = FontWeight.Medium,
                            fontSize = 12.sp
                        ),
                        color = androidx.compose.ui.graphics.Color.Gray
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "-",
                            modifier = Modifier
                                .clickable { timerViewModel.decrementPrepTime() }
                                .padding(12.dp),
                            style = MaterialTheme.typography.bodyLarge,
                            color = androidx.compose.ui.graphics.Color.Gray
                        )
                         Text(
                             "${uiState.prepTime}s",
                             style = MaterialTheme.typography.bodyLarge.copy(
                                 fontFamily = Montserrat,
                                 fontWeight = FontWeight.Medium,
                                 fontFeatureSettings = "tnum"
                             ),
                             color = androidx.compose.ui.graphics.Color.LightGray
                         )
                         Text(
                            text = "+",
                            modifier = Modifier
                                .clickable { timerViewModel.incrementPrepTime() }
                                .padding(12.dp),
                            style = MaterialTheme.typography.bodyLarge,
                            color = androidx.compose.ui.graphics.Color.Gray
                        )
                    }
                }

            } else {
                // Running/Paused
                 Text(
                    text = formatTime(uiState.remainingTime),
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontSize = 72.sp,
                        fontFamily = Montserrat,
                        fontWeight = FontWeight.Thin,
                        fontFeatureSettings = "tnum"
                    ),
                    color = if (isWarmup) androidx.compose.ui.graphics.Color.Gray else androidx.compose.ui.graphics.Color.White,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .align(BiasAlignment(0f, timeBias))
                        .width(300.dp)
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
                            text = "CLEAR",
                            style = MaterialTheme.typography.labelMedium.copy(
                                letterSpacing = 2.sp,
                                fontWeight = FontWeight.Medium,
                                fontSize = 12.sp
                            ),
                            color = androidx.compose.ui.graphics.Color.Gray
                        )
                    }

                    // Only show save if meditation time has actually elapsed
                    if (canSave) {
                        TextButton(onClick = { timerViewModel.savePartialSession() }) {
                            Text(
                                text = "SAVE",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    letterSpacing = 2.sp,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 12.sp
                                ),
                                color = androidx.compose.ui.graphics.Color.Gray
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

@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
fun TimerScreenPreview() {
    val fakeDao = object : MeditationSessionDao {
        override fun getAllSessions(): Flow<List<MeditationSession>> = flowOf(emptyList())
        override fun getTotalMeditationTime(): Flow<Long?> = flowOf(1000L)
        override suspend fun insertSession(session: MeditationSession) {}
        override suspend fun getSessionsCount(): Int = 0
    }
    val fakeUserPrefs = object : UserPreferences {
        override val lastDuration: Flow<Int> = flowOf(15)
        override suspend fun saveLastDuration(duration: Int) {}
    }
    val timerViewModel = TimerViewModel(fakeDao, fakeUserPrefs)
    TimerScreen(timerViewModel)
}