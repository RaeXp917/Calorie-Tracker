package com.example.calorie_tracker.domain.use_case

import com.example.calorie_tracker.domain.model.FoodProduct
import com.example.calorie_tracker.domain.model.MealType
import com.example.calorie_tracker.domain.repository.FoodRepository
import javax.inject.Inject

class AddFoodUC @Inject constructor(
    private val repository: FoodRepository
) {
    suspend operator fun invoke(food: FoodProduct, mealType: MealType) {
        repository.insertFood(food, mealType)
    }
}