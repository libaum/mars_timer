package com.catchingclouds.marstimer.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.catchingclouds.marstimer.viewmodel.TimerViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.ui.tooling.preview.Preview
import com.catchingclouds.marstimer.data.MeditationSession

@Composable
fun StatisticsScreen(viewModel: TimerViewModel) {
    val totalMinutes by viewModel.totalMinutes.collectAsState()
    val history by viewModel.sessionHistory.collectAsState()

    StatisticsContent(totalMinutes = totalMinutes, history = history)
}

// Pure data-driven composable suitable for Preview and unit testing
@Composable
fun StatisticsContent(
    totalMinutes: Long,
    history: List<MeditationSession>,
    modifier: Modifier = Modifier
) {
    // Calculate total days (unique days in history)
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val totalDays = history.map { dateFormat.format(Date(it.date)) }.distinct().count()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(32.dp)
    ) {
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Always show this header so the screen never appears empty
            Text(
                text = "STATS",
                style = MaterialTheme.typography.titleMedium,
                color = Color.Gray,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            // If there's no history, show a helpful placeholder
            if (history.isEmpty()) {
                Text(
                    text = "no meditation yet",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.DarkGray
                )
                return@Column
            }

            // Stats Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "$totalDays",
                        style = MaterialTheme.typography.displayMedium,
                        color = Color.White
                    )
                    Text(
                        text = "Days",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.Gray
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "$totalMinutes",
                        style = MaterialTheme.typography.displayMedium,
                        color = Color.White
                    )
                    Text(
                        text = "Minutes",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.height(64.dp))

            // Cumulative Line Graph (Minutes since beginning)
            val sortedHistory = history.sortedBy { it.date }
            val dataPoints = mutableListOf<Float>()
            var cumulativeSeconds = 0L

            sortedHistory.forEach {
                cumulativeSeconds += it.duration
                dataPoints.add(cumulativeSeconds / 60f) // Convert to Minutes (duration stored in seconds)
            }

            Canvas(modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
            ) {
                if (dataPoints.isNotEmpty()) {
                    val width = size.width
                    val height = size.height
                    val maxMinutes = (dataPoints.lastOrNull() ?: 1f).coerceAtLeast(1f)
                    val xStep = if (dataPoints.size > 1) width / (dataPoints.size - 1) else width

                    val path = androidx.compose.ui.graphics.Path()

                    dataPoints.forEachIndexed { index, minutes ->
                        val x = index * xStep
                        val y = height - (minutes / maxMinutes * height)

                        if (index == 0) {
                            path.moveTo(x, y)
                        } else {
                            path.lineTo(x, y)
                        }
                    }

                    drawPath(
                        path = path,
                        color = Color.White,
                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.dp.toPx())
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
fun StatisticsContentPreview() {
    // Create sample history (duration in seconds)
    val now = System.currentTimeMillis()
    val day = 24 * 60 * 60 * 1000L
    val sampleHistory = listOf(
        MeditationSession(date = now - 4 * day, duration = 10 * 60),
        MeditationSession(date = now - 3 * day, duration = 15 * 60),
        MeditationSession(date = now - 2 * day, duration = 5 * 60),
        MeditationSession(date = now - 1 * day, duration = 20 * 60)
    )

    StatisticsContent(totalMinutes = sampleHistory.sumOf { it.duration } / 60, history = sampleHistory)
}

@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
fun StatisticsContentEmptyPreview() {
    StatisticsContent(totalMinutes = 0, history = emptyList())
}
