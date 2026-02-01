package com.example.calorie_tracker.presentation.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.calorie_tracker.R

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = hiltViewModel(),
    onScanClick: () -> Unit = {}
) {
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onScanClick,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White
            ) {
                Icon(
                    imageVector = Icons.Default.CameraAlt,
                    contentDescription = stringResource(R.string.dashboard_scan_food)
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.dashboard_title),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(32.dp))

            // 1. Big Calorie Circle
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(200.dp)) {
                CircularProgressIndicator(
                    progress = { 1f }, // Background circle (gray)
                    modifier = Modifier.fillMaxSize(),
                    color = Color.LightGray.copy(alpha = 0.3f),
                    strokeWidth = 12.dp,
                )
                CircularProgressIndicator(
                    progress = { viewModel.getCalorieProgress() }, // Foreground circle (colored)
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.primary,
                    strokeWidth = 12.dp,
                    strokeCap = StrokeCap.Round
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${viewModel.calorieGoal - viewModel.caloriesConsumed}",
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = stringResource(R.string.dashboard_calories_left),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // 2. Macros Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                MacroItem(
                    name = stringResource(R.string.macro_protein),
                    current = viewModel.proteinConsumed,
                    max = viewModel.proteinGoal,
                    color = Color(0xFFFF9800)
                )
                MacroItem(
                    name = stringResource(R.string.macro_carbs),
                    current = viewModel.carbsConsumed,
                    max = viewModel.carbsGoal,
                    color = Color(0xFF2196F3)
                )
                MacroItem(
                    name = stringResource(R.string.macro_fat),
                    current = viewModel.fatConsumed,
                    max = viewModel.fatGoal,
                    color = Color(0xFF4CAF50)
                )
            }
        }
    }
}

@Composable
fun MacroItem(name: String, current: Int, max: Int, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = name, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(8.dp))
        // Small Progress Bar
        LinearProgressIndicator(
            progress = { current.toFloat() / max.toFloat() },
            modifier = Modifier
                .width(80.dp)
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = color,
            trackColor = color.copy(alpha = 0.2f)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = stringResource(R.string.macro_value, current, max),
            style = MaterialTheme.typography.bodySmall
        )
    }
}