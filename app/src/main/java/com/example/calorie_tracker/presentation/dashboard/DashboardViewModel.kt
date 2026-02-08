package com.example.calorie_tracker.presentation.dashboard

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.calorie_tracker.domain.use_case.GetDailyStatsUC
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val getDailyStatsUC: GetDailyStatsUC
) : ViewModel() {

    // Goals (These will come from UserProfile later, hardcoded for now)
    var calorieGoal by mutableStateOf(2500)
    var proteinGoal by mutableStateOf(180)
    var carbsGoal by mutableStateOf(250)
    var fatGoal by mutableStateOf(80)

    // Real Data from DB
    var caloriesConsumed by mutableStateOf(0)
    var proteinConsumed by mutableStateOf(0)
    var carbsConsumed by mutableStateOf(0)
    var fatConsumed by mutableStateOf(0)

    init {
        subscribeToDailyStats()
    }

    private fun subscribeToDailyStats() {
        viewModelScope.launch {
            getDailyStatsUC().collect { stats ->
                caloriesConsumed = stats.calories
                proteinConsumed = stats.protein
                carbsConsumed = stats.carbs
                fatConsumed = stats.fat
            }
        }
    }

    fun getCalorieProgress(): Float {
        return if (calorieGoal > 0) caloriesConsumed.toFloat() / calorieGoal.toFloat() else 0f
    }
}