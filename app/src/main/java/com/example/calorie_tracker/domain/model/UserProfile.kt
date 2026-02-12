package com.example.calorie_tracker.domain.model

/**
 * Domain Model: UserProfile
 *
 * User's physical data + goal type. Used to compute daily targets.
 * Targets are stored here so the app knows what to compare against (e.g. "2551 cals/avg").
 */
enum class Gender { MALE, FEMALE }

enum class ActivityLevel(val factor: Double) {
    SEDENTARY(1.2),
    LIGHTLY_ACTIVE(1.375),
    MODERATELY_ACTIVE(1.55),
    VERY_ACTIVE(1.725)
}

enum class Goal { LOSE_WEIGHT, MAINTAIN, GAIN_WEIGHT }

data class UserProfile(
    val gender: Gender,
    val age: Int,
    val heightCm: Int,
    val weightKg: Double,
    val activityLevel: ActivityLevel,
    val goal: Goal,
    // Daily targets (computed from above or set manually)
    val dailyCalorieTarget: Int,
    val proteinTarget: Double,   // grams per day
    val carbsTarget: Double,
    val fatTarget: Double,
    val updatedAt: Long = System.currentTimeMillis()
)
