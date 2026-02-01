package com.example.calorie_tracker.domain.model

data class FoodProduct(
    val name: String,
    val brand: String,
    val calories: Int,
    val protein: Double,
    val carbs: Double,
    val fat: Double,
    val imageUrl: String?,
    val barcode: String?,

    // Advanced Health Data
    val nutriScore: String?, // "A", "B", "E"
    val novaGroup: Int?,     // 4 = Ultra Processed
    val isHighSugar: Boolean // Derived from nutrient levels
)