package com.example.calorie_tracker.domain.repository

import com.example.calorie_tracker.common.Resource
import com.example.calorie_tracker.domain.model.FoodProduct
import com.example.calorie_tracker.domain.model.MealType

interface FoodRepository {
    suspend fun getFoodByBarcode(barcode: String): Resource<FoodProduct>

    // Updated to require MealType
    suspend fun insertFood(food: FoodProduct, mealType: MealType)

    suspend fun searchFoodByName(query: String): Resource<List<FoodProduct>>
}