package com.example.calorie_tracker.domain.model

// Commented out until database is set up
// import androidx.room.Entity
// import androidx.room.PrimaryKey

// @Entity(tableName = "app_settings")
data class AppSettings(
    // @PrimaryKey
    val id: String = "default",
    val colorPrimary: String = "0xFF6200EE",
    val colorSecondary: String = "0xFF03DAC5",
    val colorBackground: String = "0xFFFFFFFF",
    val colorSurface: String = "0xFFFFFFFF",
    val colorAccent: String = "0xFF6200EE",
    val colorTextPrimary: String = "0xFF000000",
    val colorTextSecondary: String = "0xFF666666",
    val colorButtonPositive: String = "0xFF6200EE",
    val colorButtonNegative: String = "0xFFB00020",
    val isDarkMode: Boolean = false,
    val notificationsEnabled: Boolean = true,
    val reminderTime: String = "09:00"
)