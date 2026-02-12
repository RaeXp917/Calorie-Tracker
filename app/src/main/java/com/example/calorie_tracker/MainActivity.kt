package com.example.calorie_tracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.calorie_tracker.domain.model.MealType
import com.example.calorie_tracker.presentation.dashboard.DashboardScreen
import com.example.calorie_tracker.presentation.onboarding.OnboardingScreen
import com.example.calorie_tracker.presentation.scan.ScanScreen
import com.example.calorie_tracker.presentation.scan.ScanViewModel
import com.example.calorie_tracker.ui.theme.CalorieTrackerTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CalorieTrackerTheme {
                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                // Show BottomBar only on main screens
                val showBottomBar = currentRoute in listOf("dashboard", "progress", "profile")

                Scaffold(
                    bottomBar = {
                        if (showBottomBar) {
                            NavigationBar {
                                NavigationBarItem(
                                    icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                                    label = { Text("Home") },
                                    selected = currentRoute == "dashboard",
                                    onClick = { navController.navigate("dashboard") }
                                )
                                NavigationBarItem(
                                    icon = { Icon(Icons.Default.ShowChart, contentDescription = "Progress") },
                                    label = { Text("Progress") },
                                    selected = currentRoute == "progress",
                                    onClick = { /* TODO */ }
                                )
                                NavigationBarItem(
                                    icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
                                    label = { Text("Profile") },
                                    selected = currentRoute == "profile",
                                    onClick = { /* TODO */ }
                                )
                            }
                        }
                    }
                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = "dashboard",
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable("onboarding") {
                            OnboardingScreen(onFinish = {
                                navController.navigate("dashboard") { popUpTo("onboarding") { inclusive = true } }
                            })
                        }

                        composable("dashboard") {
                            DashboardScreen(
                                onScanClick = { mealType ->
                                    // Navigate to scan with meal type
                                    navController.navigate("scan/${mealType.name}")
                                }
                            )
                        }

                        // Scan Screen with Argument (MealType)
                        composable(
                            route = "scan/{mealType}",
                            arguments = listOf(navArgument("mealType") { type = NavType.StringType })
                        ) { backStackEntry ->
                            val mealTypeName = backStackEntry.arguments?.getString("mealType") ?: "SNACK"
                            val mealType = try { MealType.valueOf(mealTypeName) } catch (e: Exception) { MealType.SNACK }

                            // Get ViewModel and set the meal type
                            val viewModel: ScanViewModel = hiltViewModel()
                            LaunchedEffect(mealType) {
                                viewModel.selectedMealType = mealType
                            }

                            ScanScreen(
                                onBackClick = { navController.popBackStack() },
                                viewModel = viewModel
                            )
                        }
                    }
                }
            }
        }
    }
}