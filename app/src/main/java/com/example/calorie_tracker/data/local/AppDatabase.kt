package com.example.calorie_tracker.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.calorie_tracker.data.local.dao.FoodLogDao
import com.example.calorie_tracker.data.local.entity.FoodLogEntity

@Database(
    entities = [FoodLogEntity::class],
    version = 1,
    exportSchema = false // <--- Added this to fix the warning
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun foodLogDao(): FoodLogDao
}