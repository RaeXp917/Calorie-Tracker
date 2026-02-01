package com.example.calorie_tracker.presentation.onboarding

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.ViewModel
import com.example.calorie_tracker.domain.model.Gender
import com.example.calorie_tracker.domain.model.Goal
import com.example.calorie_tracker.domain.model.ActivityLevel
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor() : ViewModel() {
    var currentStep by mutableStateOf(0)
        private set

    // Language selection
    var selectedLanguage by mutableStateOf("en")
        private set

    // User Data
    var gender by mutableStateOf<Gender?>(null)
    var age by mutableStateOf(25)
    var height by mutableStateOf(175)
    var weight by mutableStateOf(70.0)
    var activityLevel by mutableStateOf(ActivityLevel.MODERATELY_ACTIVE)
    var goal by mutableStateOf<Goal?>(null)

    fun onNextClick() {
        if (currentStep < 6) {
            currentStep++
        }
    }

    fun onBackClick() {
        if (currentStep > 0) {
            currentStep--
        }
    }

    // Language selection
    fun onLanguageSelected(languageCode: String, context: Context) {
        selectedLanguage = languageCode
        setLocale(languageCode)

        // Recreate activity to apply new language
        // Note: The activity recreation will be handled by MainActivity
    }

    private fun setLocale(languageCode: String) {
        val localeList = LocaleListCompat.forLanguageTags(languageCode)
        AppCompatDelegate.setApplicationLocales(localeList)
    }

    // Logic to calculate BMR (Calories needed)
    fun calculateDailyCalories(): Int {
        val bmr = when (gender) {
            Gender.MALE ->
                10 * weight + 6.25 * height - 5 * age + 5
            Gender.FEMALE ->
                10 * weight + 6.25 * height - 5 * age - 161
            null -> return 0
        }

        val tdee = bmr * activityLevel.factor

        return when (goal) {
            Goal.LOSE_WEIGHT -> (tdee - 500).toInt()
            Goal.MAINTAIN -> tdee.toInt()
            Goal.GAIN_WEIGHT -> (tdee + 500).toInt()
            null -> 0
        }
    }

    // Update height and weight based on gender
    fun onGenderSelected(selectedGender: Gender) {
        gender = selectedGender

        when (selectedGender) {
            Gender.MALE -> {
                height = 175
                weight = 75.0
            }
            Gender.FEMALE -> {
                height = 165
                weight = 65.0
            }
        }
    }
}