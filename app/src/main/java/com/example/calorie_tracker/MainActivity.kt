package com.example.calorie_tracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.calorie_tracker.presentation.dashboard.DashboardScreen
import com.example.calorie_tracker.presentation.onboarding.OnboardingScreen
import com.example.calorie_tracker.ui.theme.CalorieTrackerTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint // <--- CRITICAL: Needed for Hilt to work
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CalorieTrackerTheme {
                // 1. Create the Controller that handles navigation
                val navController = rememberNavController()

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->

                    // 2. The Navigation Graph (The Map of your app)
                    NavHost(
                        navController = navController,
                        startDestination = "onboarding", // Start here
                        modifier = Modifier.padding(innerPadding)
                    ) {

                        // Screen A: Onboarding (Intro)
                        composable("onboarding") {
                            OnboardingScreen(
                                onFinish = {
                                    // When user clicks Finish, go to Dashboard
                                    // popUpTo("onboarding") removes the intro from back history
                                    navController.navigate("dashboard") {
                                        popUpTo("onboarding") { inclusive = true }
                                    }
                                }
                            )
                        }

                        // Screen B: Dashboard (Main App)
                        composable("dashboard") {
                            DashboardScreen(
                                onScanClick = {
                                    // TODO: We will add the Camera Screen navigation here next
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}