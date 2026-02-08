package com.example.calorie_tracker.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "food_log")
data class FoodLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val brand: String,
    val calories: Int,
    val protein: Double,
    val carbs: Double,
    val fat: Double,
    val imageUrl: String?,
    val timestamp: Long = System.currentTimeMillis(), // When did you eat it?
    val dateString: String // Format: "2024-02-07" (For easy filtering)
)