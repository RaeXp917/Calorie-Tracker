package com.example.calorie_tracker.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.calorie_tracker.data.local.entity.FoodLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FoodLogDao {

    // Save food
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFood(food: FoodLogEntity)

    // Get all food for a specific date (e.g., "2024-02-07")
    @Query("SELECT * FROM food_log WHERE dateString = :date ORDER BY timestamp DESC")
    fun getFoodsForDate(date: String): Flow<List<FoodLogEntity>>

    // Delete a mistake
    @Delete
    suspend fun deleteFood(food: FoodLogEntity)

    // Get total calories for today (SQL Math)
    @Query("SELECT SUM(calories) FROM food_log WHERE dateString = :date")
    fun getTotalCaloriesForDate(date: String): Flow<Int?>
}