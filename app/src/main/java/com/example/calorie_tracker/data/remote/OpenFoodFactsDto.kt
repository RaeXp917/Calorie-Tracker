package com.example.calorie_tracker.data.remote.dto

import com.google.gson.annotations.SerializedName

data class OpenFoodFactsResponse(
    @SerializedName("product") val product: ProductDto?,
    @SerializedName("status") val status: Int // 1 = Found, 0 = Not Found
)

data class ProductDto(
    @SerializedName("product_name") val productName: String?,
    @SerializedName("brands") val brands: String?, // e.g., "Sklavenitis", "Fage"
    @SerializedName("image_url") val imageUrl: String?,
    @SerializedName("nutriments") val nutriments: NutrimentsDto?
)

data class NutrimentsDto(
    // Macros
    @SerializedName("energy-kcal_100g") val calories: Double?,
    @SerializedName("proteins_100g") val proteins: Double?,
    @SerializedName("carbohydrates_100g") val carbs: Double?,
    @SerializedName("fat_100g") val fat: Double?,

    // Extra Details for Accuracy
    @SerializedName("sugars_100g") val sugar: Double?,
    @SerializedName("fiber_100g") val fiber: Double?,
    @SerializedName("salt_100g") val salt: Double?
)