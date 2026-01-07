package com.cloudcatcher.marstimer.ui

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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cloudcatcher.marstimer.viewmodel.TimerViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun StatisticsScreen(viewModel: TimerViewModel) {
    val totalMinutes by viewModel.totalMinutes.collectAsState()
    val history by viewModel.sessionHistory.collectAsState()

    // Calculate total days (naive implementation: unique days in history)
    val totalDays = history.map { 
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(it.date)) 
    }.distinct().count()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black) // Strict minimalist background
            .padding(32.dp)
    ) {
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "STATISTICS",
                style = MaterialTheme.typography.titleMedium,
                color = Color.Gray,
                modifier = Modifier.padding(bottom = 48.dp)
            )

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
            if (history.isNotEmpty()) {
                val sortedHistory = history.sortedBy { it.date }
                val dataPoints = mutableListOf<Float>()
                var cumulativeSeconds = 0L

                sortedHistory.forEach {
                     cumulativeSeconds += it.duration
                     dataPoints.add(cumulativeSeconds / 60f) // Convert to Minutes
                }

                Canvas(modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                ) {
                    if (dataPoints.isNotEmpty()) {
                        val width = size.width
                        val height = size.height
                        val maxMinutes = dataPoints.lastOrNull() ?: 1f
                         // Avoid division by zero if only one point or 0 minutes
                        val xStep = if (dataPoints.size > 1) width / (dataPoints.size - 1) else width

                        val path = androidx.compose.ui.graphics.Path()
                        
                        dataPoints.forEachIndexed { index, minutes ->
                            val x = index * xStep
                            // Invert Y (0 at bottom)
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
            } else {
                 Text(
                    text = "No Data",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.DarkGray
                )
            }
        }
    }
}
