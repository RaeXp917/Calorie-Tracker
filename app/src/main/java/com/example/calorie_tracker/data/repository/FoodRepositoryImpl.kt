package com.example.calorie_tracker.data.repository

import com.example.calorie_tracker.common.Resource
import com.example.calorie_tracker.data.local.dao.FoodLogDao
import com.example.calorie_tracker.data.local.entity.FoodLogEntity
import com.example.calorie_tracker.data.remote.OpenFoodFactsApi
import com.example.calorie_tracker.domain.model.FoodProduct
import com.example.calorie_tracker.domain.model.MealType
import com.example.calorie_tracker.domain.repository.FoodRepository
import java.time.LocalDate
import javax.inject.Inject

class FoodRepositoryImpl @Inject constructor(
    private val api: OpenFoodFactsApi,
    private val dao: FoodLogDao
) : FoodRepository {

    override suspend fun getFoodByBarcode(barcode: String): Resource<FoodProduct> {
        return try {
            val response = api.getProductByBarcode(barcode)
            if (response.status == 1 && response.product != null) {
                val dto = response.product
                val nut = dto.nutriments
                val food = FoodProduct(
                    id = barcode,
                    name = dto.productName ?: "Unknown",
                    brand = dto.brands ?: "",
                    calories = nut?.calories?.toInt() ?: 0,
                    protein = nut?.proteins ?: 0.0,
                    carbs = nut?.carbs ?: 0.0,
                    fat = nut?.fat ?: 0.0,
                    imageUrl = dto.imageUrl,
                    barcode = barcode,
                    // Micros
                    fiber = nut?.fiber ?: 0.0,
                    sugar = nut?.sugar ?: 0.0,
                    sodium = (nut?.salt ?: 0.0) * 400, // salt g -> sodium mg
                    // Serving
                    baseServingSize = 100.0,
                    baseServingUnit = "g",
                    portionSizeGrams = 100,
                    source = "OpenFoodFacts",
                    isVerified = true,
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

    override suspend fun insertFood(food: FoodProduct, mealType: MealType) {
        val entity = FoodLogEntity(
            name = food.name,
            brand = food.brand,
            // Macros (Calculated)
            calories = food.getCaloriesForPortion(),
            protein = food.getProteinForPortion(),
            carbs = food.getCarbsForPortion(),
            fat = food.getFatForPortion(),
            // Micros (Calculated)
            fiber = food.getFiberForPortion(),
            sugar = food.getSugarForPortion(),
            sodium = food.getSodiumForPortion(),
            // Context
            mealType = mealType.name, // Enum to String
            portionSizeGrams = food.portionSizeGrams,
            imageUrl = food.imageUrl,
            dateString = LocalDate.now().toString()
        )
        dao.insertFood(entity)
    }

    override suspend fun searchFoodByName(query: String): Resource<List<FoodProduct>> {
        return try {
            val response = api.searchProductsByName(query)
            if (!response.products.isNullOrEmpty()) {
                val foodList = response.products.map { dto ->
                    FoodProduct(
                        id = dto.productName?.hashCode()?.toString(),
                        name = dto.productName ?: "Unknown",
                        brand = dto.brands ?: "",
                        calories = dto.nutriments?.calories?.toInt() ?: 0,
                        protein = dto.nutriments?.proteins ?: 0.0,
                        carbs = dto.nutriments?.carbs ?: 0.0,
                        fat = dto.nutriments?.fat ?: 0.0,
                        imageUrl = dto.imageUrl,
                        barcode = null,
                        fiber = dto.nutriments?.fiber ?: 0.0,
                        sugar = dto.nutriments?.sugar ?: 0.0,
                        sodium = (dto.nutriments?.salt ?: 0.0) * 400,
                        baseServingSize = 100.0,
                        baseServingUnit = "g",
                        portionSizeGrams = 100,
                        source = "OpenFoodFacts",
                        isVerified = true,
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