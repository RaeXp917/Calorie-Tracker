package com.example.calorie_tracker.domain.model

/**
 * Domain Model: FoodProduct
 *
 * Represents static nutrition info for a food item (from API or user-created).
 * Does NOT represent "what the user ate" — use FoodLogEntry for that.
 *
 * Portion: All nutrient values are per baseServingSize + baseServingUnit.
 * Use getXxxForPortion(quantity, unit) to compute values for a consumed amount.
 */
data class FoodProduct(
    val id: String? = null,           // Stable ID (barcode or generated UUID)
    val name: String,
    val brand: String,
    val calories: Int,
    val protein: Double,
    val carbs: Double,
    val fat: Double,
    val imageUrl: String?,
    val barcode: String?,

    // ----- 1. Micronutrients (per base serving) -----
    val fiber: Double = 0.0,
    val sugar: Double = 0.0,
    val addedSugar: Double = 0.0,
    val sodium: Double = 0.0,         // mg per base serving
    val cholesterol: Double = 0.0,
    val saturatedFat: Double = 0.0,
    val transFat: Double = 0.0,
    // Optional micronutrients
    val potassium: Double? = null,
    val calcium: Double? = null,
    val iron: Double? = null,
    val vitaminA: Double? = null,
    val vitaminC: Double? = null,
    val vitaminD: Double? = null,

    // ----- 2. Portion & serving -----
    val baseServingSize: Double = 100.0,
    val baseServingUnit: String = "g",
    val householdServingSize: Double? = null,   // e.g. 1.0
    val householdServingName: String? = null,   // "slice", "cup", "piece"
    /** Actual portion user is entering (for UI/edit flow). */
    val portionSizeGrams: Int = 100,

    // ----- 3. Product metadata -----
    val category: String? = null,
    val ingredients: List<String>? = null,
    val allergens: List<String>? = null,
    val isVerified: Boolean = false,
    val source: String? = null,       // "OpenFoodFacts", "User"
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val createdByUserId: String? = null,

    // ----- 4. Health / labels -----
    val nutriScore: String? = null,
    val novaGroup: Int? = null,
    val isHighSugar: Boolean = false
) {
    /** Backward compatibility: reference size in grams for calculations. */
    val baseSizeGrams: Int get() = if (baseServingUnit == "g") baseServingSize.toInt() else 100

    fun getCaloriesForPortion(): Int {
        if (baseSizeGrams == 0) return calories
        return ((calories.toDouble() / baseSizeGrams) * portionSizeGrams).toInt()
    }

    fun getProteinForPortion(): Double {
        if (baseSizeGrams == 0) return protein
        return (protein / baseSizeGrams) * portionSizeGrams
    }

    fun getCarbsForPortion(): Double {
        if (baseSizeGrams == 0) return carbs
        return (carbs / baseSizeGrams) * portionSizeGrams
    }

    fun getFatForPortion(): Double {
        if (baseSizeGrams == 0) return fat
        return (fat / baseSizeGrams) * portionSizeGrams
    }

    fun getFiberForPortion(): Double {
        if (baseSizeGrams == 0) return fiber
        return (fiber / baseSizeGrams) * portionSizeGrams
    }

    fun getSugarForPortion(): Double {
        if (baseSizeGrams == 0) return sugar
        return (sugar / baseSizeGrams) * portionSizeGrams
    }

    fun getSodiumForPortion(): Double {
        if (baseSizeGrams == 0) return sodium
        return (sodium / baseSizeGrams) * portionSizeGrams
    }

    fun getSaturatedFatForPortion(): Double {
        if (baseSizeGrams == 0) return saturatedFat
        return (saturatedFat / baseSizeGrams) * portionSizeGrams
    }
}
