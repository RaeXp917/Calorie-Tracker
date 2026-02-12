package com.example.calorie_tracker.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.calorie_tracker.data.local.dao.FoodLogDao
import com.example.calorie_tracker.data.local.dao.UserProfileDao
import com.example.calorie_tracker.data.local.entity.FoodLogEntity
import com.example.calorie_tracker.data.local.entity.UserProfileEntity

// Bump version to 2 because we changed tables
@Database(
    entities = [FoodLogEntity::class, UserProfileEntity::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun foodLogDao(): FoodLogDao
    abstract fun userProfileDao(): UserProfileDao // <--- Add this
}