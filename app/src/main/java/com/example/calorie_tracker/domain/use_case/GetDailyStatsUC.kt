package com.example.calorie_tracker.domain.use_case

import com.example.calorie_tracker.data.local.dao.FoodLogDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import javax.inject.Inject

class GetDailyStatsUC @Inject constructor(
    private val dao: FoodLogDao
) {
    // Returns a Flow (stream) of the total nutrients for today
    operator fun invoke(): Flow<DailyNutrients> {
        val today = LocalDate.now().toString()

        return dao.getFoodsForDate(today).map { foods ->
            // Calculate totals manually from the list
            val totalCalories = foods.sumOf { it.calories }
            val totalProtein = foods.sumOf { it.protein }
            val totalCarbs = foods.sumOf { it.carbs }
            val totalFat = foods.sumOf { it.fat }

            DailyNutrients(
                calories = totalCalories,
                protein = totalProtein.toInt(),
                carbs = totalCarbs.toInt(),
                fat = totalFat.toInt()
            )
        }
    }
}

// Simple data class to hold the totals
data class DailyNutrients(
    val calories: Int,
    val protein: Int,
    val carbs: Int,
    val fat: Int
)