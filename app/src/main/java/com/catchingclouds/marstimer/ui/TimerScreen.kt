package com.catchingclouds.marstimer.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.background
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.TextButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.catchingclouds.marstimer.UserPreferences
import com.catchingclouds.marstimer.data.MeditationSession
import com.catchingclouds.marstimer.data.MeditationSessionDao
import com.catchingclouds.marstimer.ui.theme.NotoSans
import com.catchingclouds.marstimer.viewmodel.TimerViewModel
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TimerScreen(timerViewModel: TimerViewModel = viewModel()) {
    val uiState by timerViewModel.uiState.collectAsState()

    val showStats = remember { mutableStateOf(false) }

    if (showStats.value) {
        BackHandler { showStats.value = false }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .combinedClickable(
                    onClick = { showStats.value = false },
                    onLongClick = { showStats.value = false }
                )
        ) {
            // Show the real statistics screen using the ViewModel so it always has data or a fallback
            StatisticsScreen(viewModel = timerViewModel)
        }
    } else {
        TimerScreenContent(
            uiState = uiState,
            onStart = { timerViewModel.startTimer() },
            onPause = { timerViewModel.pauseTimer() },
            onResume = { timerViewModel.resumeTimer() },
            onIncrementMeditation = { timerViewModel.incrementMeditationTime() },
            onDecrementMeditation = { timerViewModel.decrementMeditationTime() },
            onSetMeditationTime = { timerViewModel.setMeditationTime(it) },
            onIncrementPrep = { timerViewModel.incrementPrepTime() },
            onDecrementPrep = { timerViewModel.decrementPrepTime() },
            onStop = { timerViewModel.stopTimer() },
            onSavePartial = { timerViewModel.savePartialSession() },
            onShowStats = { showStats.value = true }
        )
    }
}

// Lightweight, fast-to-render UI composable that receives state and callbacks
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TimerScreenContent(
    uiState: com.catchingclouds.marstimer.viewmodel.TimerUiState,
    onStart: () -> Unit = {},
    onPause: () -> Unit = {},
    onResume: () -> Unit = {},
    onIncrementMeditation: () -> Unit = {},
    onDecrementMeditation: () -> Unit = {},
    onSetMeditationTime: (Int) -> Unit = {},
    onIncrementPrep: () -> Unit = {},
    onDecrementPrep: () -> Unit = {},
    onStop: () -> Unit = {},
    onSavePartial: () -> Unit = {},
    onShowStats: () -> Unit = {}
) {
    val timerState = uiState.timerState

    val isIdle = timerState == TimerViewModel.TimerState.IDLE || timerState == TimerViewModel.TimerState.FINISHED
    val isRunning = timerState == TimerViewModel.TimerState.RUNNING || timerState == TimerViewModel.TimerState.PREP
    val isPaused = timerState == TimerViewModel.TimerState.PAUSED
    val isWarmup = timerState == TimerViewModel.TimerState.PREP

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 24.dp)
            .statusBarsPadding()
            .combinedClickable(
                onClick = {
                    if (isIdle) onStart()
                    else if (isRunning) onPause()
                    else if (isPaused) onResume()
                },
                onLongClick = {
                    if (isIdle) onShowStats()
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
            // Main Timer Row (Always visible, buttons hidden when running)
            Row(
                modifier = Modifier
                    .align(BiasAlignment(0f, timeBias))
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                // Minus
                Text(
                    text = "-",
                    style = MaterialTheme.typography.displayMedium.copy(
                        fontFamily = NotoSans,
                        fontWeight = FontWeight.Thin
                    ),
                    color = androidx.compose.ui.graphics.Color.White.copy(alpha = if (isIdle) 0.5f else 0f),
                    modifier = Modifier
                        .clickable(enabled = isIdle) { onDecrementMeditation() }
                        .padding(16.dp)
                )

                // Timer Display
                Text(
                    text = formatTime(uiState.remainingTime),
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontSize = 72.sp,
                        fontFamily = NotoSans,
                        fontWeight = FontWeight.Thin,
                        fontFeatureSettings = "tnum"
                    ),
                    color = if (isWarmup) androidx.compose.ui.graphics.Color.Gray else androidx.compose.ui.graphics.Color.White,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )

                // Plus
                Text(
                    text = "+",
                    style = MaterialTheme.typography.displayMedium.copy(
                        fontFamily = NotoSans,
                        fontWeight = FontWeight.Thin
                    ),
                    color = androidx.compose.ui.graphics.Color.White.copy(alpha = if (isIdle) 0.5f else 0f),
                    modifier = Modifier
                        .clickable(enabled = isIdle) { onIncrementMeditation() }
                        .padding(16.dp)
                )
            }

        // Additional Controls (Only visible in Idle)
        if (isIdle) {
            // Quick Select Buttons
            Row(
                modifier = Modifier
                    .align(BiasAlignment(0f, timeBias + 0.35f))
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
                            .clickable { onSetMeditationTime(time) }
                            .background(androidx.compose.ui.graphics.Color.Black),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "$time",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontFamily = NotoSans,
                                fontWeight = FontWeight.Medium,
                                fontSize = 16.sp,
                                fontFeatureSettings = "tnum"
                            ),
                            color = if (isSelected) androidx.compose.ui.graphics.Color.White else androidx.compose.ui.graphics.Color.White.copy(alpha = 0.6f)
                        )
                    }
                }
            }

            // Prep Time Control - Inline countdown indicator
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 72.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Show either the countdown value or a hint to add one
                if (uiState.prepTime > 0) {
                    // Active prep time display
                    Text(
                        text = "${uiState.prepTime}s",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontFamily = NotoSans,
                            fontWeight = FontWeight.Light,
                            fontSize = 24.sp,
                            fontFeatureSettings = "tnum"
                        ),
                        color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.6f)
                    )
                    Text(
                        text = "delay",
                        style = MaterialTheme.typography.labelSmall.copy(
                            letterSpacing = 1.sp,
                            fontWeight = FontWeight.Normal,
                            fontSize = 10.sp
                        ),
                        color = androidx.compose.ui.graphics.Color.Gray,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                    // Adjust controls (smaller, subtle)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        Text(
                            text = "−",
                            modifier = Modifier
                                .clickable { onDecrementPrep() }
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            color = androidx.compose.ui.graphics.Color.Gray.copy(alpha = 0.6f)
                        )
                        Text(
                            text = "+",
                            modifier = Modifier
                                .clickable { onIncrementPrep() }
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            color = androidx.compose.ui.graphics.Color.Gray.copy(alpha = 0.6f)
                        )
                    }
                } else {
                    // No prep time - show subtle add option
                    Text(
                        text = "+ delay",
                        style = MaterialTheme.typography.labelMedium.copy(
                            letterSpacing = 1.sp,
                            fontWeight = FontWeight.Normal,
                            fontSize = 12.sp
                        ),
                        color = androidx.compose.ui.graphics.Color.Gray.copy(alpha = 0.5f),
                        modifier = Modifier
                            .clickable { onIncrementPrep() }
                            .padding(12.dp)
                    )
                }
            }

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
                TextButton(onClick = { onStop() }) {
                    Text(
                        text = "CLEAR",
                        style = MaterialTheme.typography.labelMedium.copy(
                            letterSpacing = 2.sp,
                            fontWeight = FontWeight.Medium,
                            fontSize = 12.sp
                        ),
                        color = androidx.compose.ui.graphics.Color.White
                    )
                }

                // Only show save if meditation time has actually elapsed
                if (canSave) {
                    TextButton(onClick = { onSavePartial() }) {
                        Text(
                            text = "SAVE",
                            style = MaterialTheme.typography.labelMedium.copy(
                                letterSpacing = 2.sp,
                                fontWeight = FontWeight.Medium,
                                fontSize = 12.sp
                            ),
                            color = androidx.compose.ui.graphics.Color.White
                        )
                    }
                }
            }
        }
    }
}

private fun formatTime(millis: Long): String {
    // Round up to the nearest second to show "5" für [5000, 4001] range
    val roundedMillis = if (millis > 0) millis + 999 else 0
    val minutes = TimeUnit.MILLISECONDS.toMinutes(roundedMillis)
    val seconds = TimeUnit.MILLISECONDS.toSeconds(roundedMillis) - TimeUnit.MINUTES.toSeconds(minutes)
    return String.format(Locale.US, "%02d:%02d", minutes, seconds)
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