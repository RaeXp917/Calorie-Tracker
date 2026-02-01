package com.example.calorie_tracker.presentation.dashboard

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor() : ViewModel() {

    // Mock Data (We will replace this with Real DB data later)
    var calorieGoal by mutableStateOf(2500)
    var caloriesConsumed by mutableStateOf(1250)

    var proteinConsumed by mutableStateOf(80)
    var proteinGoal by mutableStateOf(180)

    var carbsConsumed by mutableStateOf(150)
    var carbsGoal by mutableStateOf(250)

    var fatConsumed by mutableStateOf(40)
    var fatGoal by mutableStateOf(80)

    // Helper to calculate progress (0.0 to 1.0)
    fun getCalorieProgress(): Float {
        return if (calorieGoal > 0) caloriesConsumed.toFloat() / calorieGoal.toFloat() else 0f
    }
}