package com.example.calorie_tracker.data.repository

import com.example.calorie_tracker.common.Resource
import com.example.calorie_tracker.data.local.entity.FoodLogEntity
import com.example.calorie_tracker.data.remote.OpenFoodFactsApi
import com.example.calorie_tracker.domain.model.FoodProduct
import com.example.calorie_tracker.domain.repository.FoodRepository
import java.time.LocalDate
import javax.inject.Inject

class FoodRepositoryImpl @Inject constructor(
    private val api: OpenFoodFactsApi,
    private val dao: com.example.calorie_tracker.data.local.dao.FoodLogDao
) : FoodRepository {

    override suspend fun getFoodByBarcode(barcode: String): Resource<FoodProduct> {
        return try {
            val response = api.getProductByBarcode(barcode)
            if (response.status == 1 && response.product != null) {
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

    // --- NEW: Implementation to save food ---
    override suspend fun insertFood(food: FoodProduct) {
        val entity = FoodLogEntity(
            name = food.name,
            brand = food.brand,
            calories = food.calories,
            protein = food.protein,
            carbs = food.carbs,
            fat = food.fat,
            imageUrl = food.imageUrl,
            dateString = LocalDate.now().toString() // Saves as "2024-02-07"
        )
        dao.insertFood(entity)
    }

    override suspend fun searchFoodByName(query: String): Resource<List<FoodProduct>> {
        return try {
            val response = api.searchProductsByName(query)
            if (!response.products.isNullOrEmpty()) {
                val foodList = response.products.map { dto ->
                    FoodProduct(
                        name = dto.productName ?: "Unknown",
                        brand = dto.brands ?: "",
                        calories = dto.nutriments?.calories?.toInt() ?: 0,
                        protein = dto.nutriments?.proteins ?: 0.0,
                        carbs = dto.nutriments?.carbs ?: 0.0,
                        fat = dto.nutriments?.fat ?: 0.0,
                        imageUrl = dto.imageUrl,
                        barcode = null, // Search results might not need barcode immediately
                        nutriScore = dto.nutriScore,
                        novaGroup = dto.novaGroup,
                        isHighSugar = (dto.nutrientLevels?.sugarLevel == "high")
                    )
                }
                Resource.Success(foodList)
            } else {
                Resource.Error("No results found for '$query'")
            }
        } catch (e: Exception) {
            Resource.Error("Search Error: ${e.message}")
        }
    }
}