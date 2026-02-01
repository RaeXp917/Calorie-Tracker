package com.example.calorie_tracker.domain.repository

import com.example.calorie_tracker.domain.model.FoodProduct
import com.example.calorie_tracker.common.Resource

interface FoodRepository {
    suspend fun getFoodByBarcode(barcode: String): Resource<FoodProduct>
}