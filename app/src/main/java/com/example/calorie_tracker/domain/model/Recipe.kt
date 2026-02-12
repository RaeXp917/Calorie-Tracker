package com.example.calorie_tracker.domain.model

/**
 * Domain Model: Recipe
 *
 * A composite "plate" made of multiple ingredients.
 * Totals are computed from RecipeIngredients (each references a product + quantity).
 */
data class Recipe(
    val id: String,
    val name: String,
    val ingredients: List<RecipeIngredient>,
    val totalCalories: Int,
    val totalProtein: Double,
    val totalCarbs: Double,
    val totalFat: Double,
    val servings: Int,           // e.g. 4 portions
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * One ingredient in a recipe: product reference + quantity.
 */
data class RecipeIngredient(
    val productId: String,       // FoodProduct.id or barcode
    val quantity: Double,
    val unit: String = "g"
)
