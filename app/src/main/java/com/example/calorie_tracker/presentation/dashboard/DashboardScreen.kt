package com.example.calorie_tracker.presentation.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
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
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.calorie_tracker.R
import com.example.calorie_tracker.domain.model.MealType

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = hiltViewModel(),
    onScanClick: (MealType) -> Unit // Now accepts MealType
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // 1. Header
            Text(
                text = stringResource(R.string.dashboard_title),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 2. Summary Card (Calories + Macros)
            SummaryCard(viewModel)

            Spacer(modifier = Modifier.height(24.dp))

            // 3. Meal Sections
            MealSection(
                title = "Breakfast",
                calories = 0, // TODO: Get from DB
                onAddClick = { onScanClick(MealType.BREAKFAST) }
            )

            MealSection(
                title = "Lunch",
                calories = 0, // TODO: Get from DB
                onAddClick = { onScanClick(MealType.LUNCH) }
            )

            MealSection(
                title = "Dinner",
                calories = 0, // TODO: Get from DB
                onAddClick = { onScanClick(MealType.DINNER) }
            )

            MealSection(
                title = "Snack",
                calories = 0, // TODO: Get from DB
                onAddClick = { onScanClick(MealType.SNACK) }
            )

            Spacer(modifier = Modifier.height(80.dp)) // Space for Bottom Nav
        }
    }
}

@Composable
fun SummaryCard(viewModel: DashboardViewModel) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left: Circular Progress
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(120.dp)) {
                CircularProgressIndicator(
                    progress = { 1f },
                    modifier = Modifier.fillMaxSize(),
                    color = Color.LightGray.copy(alpha = 0.2f),
                    strokeWidth = 10.dp,
                )
                CircularProgressIndicator(
                    progress = { viewModel.getCalorieProgress() },
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.primary,
                    strokeWidth = 10.dp,
                    strokeCap = StrokeCap.Round
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${viewModel.calorieGoal - viewModel.caloriesConsumed}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Left",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray
                    )
                }
            }

            // Right: Macros
            Column(
                modifier = Modifier.weight(1f).padding(start = 24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MacroRow("Protein", viewModel.proteinConsumed, viewModel.proteinGoal, Color(0xFFFF9800))
                MacroRow("Carbs", viewModel.carbsConsumed, viewModel.carbsGoal, Color(0xFF2196F3))
                MacroRow("Fat", viewModel.fatConsumed, viewModel.fatGoal, Color(0xFF4CAF50))
            }
        }
    }
}

@Composable
fun MacroRow(name: String, current: Int, max: Int, color: Color) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = name, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
            Text(text = "${current}/${max}g", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { current.toFloat() / max.toFloat() },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = color,
            trackColor = color.copy(alpha = 0.2f)
        )
    }
}

@Composable
fun MealSection(title: String, calories: Int, onAddClick: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(text = "$calories kcal", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
            }

            // Add Button
            IconButton(
                onClick = onAddClick,
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), CircleShape)
                    .size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Food",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}