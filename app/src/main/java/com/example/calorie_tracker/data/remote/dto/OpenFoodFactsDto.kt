package com.example.calorie_tracker.data.remote.dto

import com.google.gson.annotations.SerializedName

// 1. The Main Response
data class OpenFoodFactsResponse(
    @SerializedName("product") val product: ProductDto?,
    @SerializedName("status") val status: Int
)

// 2. The Product Details (Expanded for "Product DNA")
data class ProductDto(
    @SerializedName("product_name") val productName: String?,
    @SerializedName("brands") val brands: String?,
    @SerializedName("image_url") val imageUrl: String?,
    @SerializedName("nutriments") val nutriments: NutrimentsDto?,

    // New Fields for FitHub-like accuracy:
    @SerializedName("nutriscore_grade") val nutriScore: String?, // "a", "b", "c"...
    @SerializedName("nova_group") val novaGroup: Int?, // 1-4 (Processed level)
    @SerializedName("allergens_from_ingredients") val allergens: String?,
    @SerializedName("nutrient_levels") val nutrientLevels: NutrientLevelsDto?
)

// 3. Nutritional Values
data class NutrimentsDto(
    @SerializedName("energy-kcal_100g") val calories: Double?,
    @SerializedName("proteins_100g") val proteins: Double?,
    @SerializedName("carbohydrates_100g") val carbs: Double?,
    @SerializedName("fat_100g") val fat: Double?,
    @SerializedName("sugars_100g") val sugar: Double?,
    @SerializedName("fiber_100g") val fiber: Double?,
    @SerializedName("salt_100g") val salt: Double?
)

// 4. Traffic Lights (High/Low/Moderate)
data class NutrientLevelsDto(
    @SerializedName("fat") val fatLevel: String?,
    @SerializedName("sugars") val sugarLevel: String?,
    @SerializedName("salt") val saltLevel: String?
)