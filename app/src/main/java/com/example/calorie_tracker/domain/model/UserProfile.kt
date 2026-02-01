package com.example.calorie_tracker.domain.model

enum class Gender { MALE, FEMALE }

enum class ActivityLevel(val factor: Double) {
    SEDENTARY(1.2),      // Desk job
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
    val goal: Goal
)