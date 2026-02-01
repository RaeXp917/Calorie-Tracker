package com.example.calorie_tracker.data.repository

import com.example.calorie_tracker.common.Resource
import com.example.calorie_tracker.data.remote.OpenFoodFactsApi
import com.example.calorie_tracker.domain.model.FoodProduct
import com.example.calorie_tracker.domain.repository.FoodRepository
import javax.inject.Inject

class FoodRepositoryImpl @Inject constructor(
    private val api: OpenFoodFactsApi
) : FoodRepository {

    override suspend fun getFoodByBarcode(barcode: String): Resource<FoodProduct> {
        return try {
            val response = api.getProductByBarcode(barcode)
            if (response.status == 1 && response.product != null) {
                // Mapping DTO to Domain
                val dto = response.product
                val food = FoodProduct(
                    name = dto.productName ?: "Unknown",
                    brand = dto.brands ?: "",
                    calories = dto.nutriments?.calories?.toInt() ?: 0,
                    protein = dto.nutriments?.proteins ?: 0.0,
                    carbs = dto.nutriments?.carbs ?: 0.0,
                    fat = dto.nutriments?.fat ?: 0.0,
                    imageUrl = dto.imageUrl,
                    barcode = barcode,
                    nutriScore = dto.nutriScore,
                    novaGroup = dto.novaGroup,
                    isHighSugar = (dto.nutrientLevels?.sugarLevel == "high")
                )
                Resource.Success(food)
            } else {
                Resource.Error("Product not found")
            }
        } catch (e: Exception) {
            Resource.Error("Network Error: ${e.message}")
        }
    }
}