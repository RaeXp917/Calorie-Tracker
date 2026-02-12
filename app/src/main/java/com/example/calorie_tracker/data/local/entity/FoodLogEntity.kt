package com.example.calorie_tracker.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Database Table: food_log
 * Stores the actual food eaten by the user.
 * Updated for Phase 2 to include MealType and Micronutrients.
 */
@Entity(tableName = "food_log")
data class FoodLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val brand: String,

    // Macros (Calculated for the portion eaten)
    val calories: Int,
    val protein: Double,
    val carbs: Double,
    val fat: Double,

    // --- NEW: Micronutrients ---
    val fiber: Double = 0.0,
    val sugar: Double = 0.0,
    val sodium: Double = 0.0,

    // --- NEW: Context ---
    val mealType: String, // Saved as "BREAKFAST", "LUNCH", etc.
    val portionSizeGrams: Int,

    val imageUrl: String?,
    val timestamp: Long = System.currentTimeMillis(),
    val dateString: String // Format: "2024-02-12"
)