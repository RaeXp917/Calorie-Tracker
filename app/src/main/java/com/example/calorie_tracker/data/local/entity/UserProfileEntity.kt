package com.example.calorie_tracker.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey val id: Int = 0, // Always 0 (we only have 1 user)
    val gender: String,
    val age: Int,
    val heightCm: Int,
    val weightKg: Double,
    val activityLevel: String,
    val goal: String,
    // Targets
    val dailyCalorieTarget: Int,
    val proteinTarget: Double,
    val carbsTarget: Double,
    val fatTarget: Double
)