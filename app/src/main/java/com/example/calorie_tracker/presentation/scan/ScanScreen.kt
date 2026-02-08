package com.example.calorie_tracker.presentation.scan

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.ImageCapture
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.calorie_tracker.R
import com.example.calorie_tracker.presentation.components.CameraPreview
import com.example.calorie_tracker.presentation.components.takePhoto

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanScreen(
    onBackClick: () -> Unit,
    viewModel: ScanViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val imageCapture = remember { ImageCapture.Builder().build() }

    // Zoom State
    var zoomRatio by remember { mutableFloatStateOf(1f) }

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted -> hasCameraPermission = granted }
    )

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) permissionLauncher.launch(Manifest.permission.CAMERA)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Scanner") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (hasCameraPermission) {
                // 1. Camera Preview with Zoom
                CameraPreview(
                    modifier = Modifier.fillMaxSize(),
                    imageCapture = imageCapture,
                    zoomRatio = zoomRatio,
                    analyzer = if (viewModel.scanMode == ScanMode.BARCODE) {
                        BarcodeAnalyzer { code -> viewModel.onBarcodeDetected(code) }
                    } else null
                )

                // 2. Overlay
                Box(
                    modifier = Modifier
                        .size(280.dp)
                        .align(Alignment.Center)
                        .border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(12.dp))
                )

                // 3. Controls (Zoom + Mode + Shutter)
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Zoom Slider
                    if (viewModel.scanMode != ScanMode.BARCODE) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 32.dp).background(Color.Black.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                        ) {
                            Text("1x", color = Color.White, modifier = Modifier.padding(start = 8.dp))
                            Slider(
                                value = zoomRatio,
                                onValueChange = { zoomRatio = it },
                                valueRange = 1f..4f,
                                modifier = Modifier.weight(1f).padding(horizontal = 8.dp)
                            )
                            Text("4x", color = Color.White, modifier = Modifier.padding(end = 8.dp))
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // Mode Tabs
                    Row(
                        modifier = Modifier
                            .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(20.dp))
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        ScanMode.values().forEach { mode ->
                            Text(
                                text = mode.name,
                                color = if (viewModel.scanMode == mode) MaterialTheme.colorScheme.primary else Color.White,
                                fontWeight = if (viewModel.scanMode == mode) FontWeight.Bold else FontWeight.Normal,
                                modifier = Modifier
                                    .clickable { viewModel.scanMode = mode }
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Shutter Button
                    if (viewModel.scanMode != ScanMode.BARCODE) {
                        Button(
                            onClick = {
                                takePhoto(context, imageCapture) { file ->
                                    viewModel.onPhotoCaptured(file, context)
                                }
                            },
                            shape = CircleShape,
                            modifier = Modifier.size(70.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White)
                        ) {
                            Box(modifier = Modifier.size(60.dp).border(2.dp, Color.Black, CircleShape))
                        }
                    } else {
                        Text(stringResource(R.string.scan_searching), color = Color.White)
                    }
                }
            } else {
                Text(stringResource(R.string.scan_permission_rationale), modifier = Modifier.align(Alignment.Center))
            }

            // 4. Loading
            if (viewModel.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }

            // 5. Error
            AnimatedVisibility(
                visible = viewModel.errorMessage != null,
                enter = slideInVertically { it } + fadeIn(),
                exit = slideOutVertically { it } + fadeOut(),
                modifier = Modifier.align(Alignment.TopCenter).padding(top = 64.dp)
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = viewModel.errorMessage ?: "",
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }

            // 6. Success Result (Bottom Sheet with Autocomplete)
            viewModel.scannedProduct?.let { product ->
                ModalBottomSheet(
                    onDismissRequest = { viewModel.resetScanner() },
                    containerColor = MaterialTheme.colorScheme.surface
                ) {
                    // State for editing
                    var editedName by remember(product) { mutableStateOf(product.name) }
                    var editedCalories by remember(product) { mutableStateOf(product.calories.toString()) }
                    var expanded by remember { mutableStateOf(false) }

                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Edit & Save", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(16.dp))

                        // Smart Name Input
                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = editedName,
                                onValueChange = {
                                    editedName = it
                                    viewModel.onEditNameChange(it)
                                    expanded = true
                                },
                                label = { Text("Food Name") },
                                modifier = Modifier.fillMaxWidth()
                            )

                            DropdownMenu(
                                expanded = expanded && viewModel.nameSuggestions.isNotEmpty(),
                                onDismissRequest = { expanded = false },
                                modifier = Modifier.fillMaxWidth(0.9f).background(MaterialTheme.colorScheme.surface)
                            ) {
                                viewModel.nameSuggestions.take(5).forEach { suggestion ->
                                    DropdownMenuItem(
                                        text = {
                                            Column {
                                                Text(text = suggestion.name, fontWeight = FontWeight.Bold)
                                                Text(text = "${suggestion.calories} kcal - ${suggestion.brand}", style = MaterialTheme.typography.bodySmall)
                                            }
                                        },
                                        onClick = {
                                            editedName = suggestion.name
                                            editedCalories = suggestion.calories.toString()
                                            viewModel.onSuggestionSelected(suggestion)
                                            expanded = false
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Calories Input
                        OutlinedTextField(
                            value = editedCalories,
                            onValueChange = { editedCalories = it },
                            label = { Text("Calories (kcal)") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        // Save Button
                        Button(
                            onClick = {
                                val finalProduct = viewModel.scannedProduct?.copy(
                                    name = editedName,
                                    calories = editedCalories.toIntOrNull() ?: 0
                                )
                                if (finalProduct != null) viewModel.saveScannedFood(finalProduct)
                                viewModel.resetScanner()
                                onBackClick()
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Save to Diary")
                        }
                        Spacer(modifier = Modifier.height(32.dp))
                    }
                }
            }
        }
    }
}