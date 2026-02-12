package com.example.calorie_tracker.presentation.scan

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.ImageCapture
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.calorie_tracker.R
import com.example.calorie_tracker.domain.model.FoodProduct
import com.example.calorie_tracker.presentation.components.CameraPreview
import com.example.calorie_tracker.presentation.components.takePhoto
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanScreen(
    onBackClick: () -> Unit,
    viewModel: ScanViewModel = hiltViewModel()
) {
    val context = LocalContext.current

    val imageCapture = remember {
        ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .setFlashMode(ImageCapture.FLASH_MODE_AUTO)
            .build()
    }

    var zoomRatio by remember { mutableFloatStateOf(1f) }
    var isCapturing by remember { mutableStateOf(false) }
    var captureFlash by remember { mutableStateOf(false) }

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
                title = { Text(stringResource(R.string.scan_title)) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.scan_back)
                        )
                    }
                },
                // --- NEW: Search Action ---
                actions = {
                    IconButton(onClick = { viewModel.startManualEntry() }) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search Food"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White,
                    actionIconContentColor = Color.White
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

                // Flash Animation
                AnimatedVisibility(
                    visible = captureFlash,
                    enter = fadeIn(animationSpec = tween(50)),
                    exit = fadeOut(animationSpec = tween(200))
                ) {
                    Box(modifier = Modifier.fillMaxSize().background(Color.White.copy(alpha = 0.8f)))
                }

                // 2. Overlay
                Box(
                    modifier = Modifier
                        .size(280.dp)
                        .align(Alignment.Center)
                        .border(
                            width = if (isCapturing) 4.dp else 2.dp,
                            color = if (isCapturing) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                            shape = RoundedCornerShape(12.dp)
                        )
                )

                // 3. Controls
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
                            Text(stringResource(R.string.scan_zoom_min), color = Color.White, modifier = Modifier.padding(start = 8.dp))
                            Slider(
                                value = zoomRatio,
                                onValueChange = { zoomRatio = it },
                                valueRange = 1f..4f,
                                modifier = Modifier.weight(1f).padding(horizontal = 8.dp)
                            )
                            Text(stringResource(R.string.scan_zoom_max), color = Color.White, modifier = Modifier.padding(end = 8.dp))
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
                            val modeText = when (mode) {
                                ScanMode.BARCODE -> stringResource(R.string.scan_mode_barcode)
                                ScanMode.TEXT -> stringResource(R.string.scan_mode_text)
                            }
                            Text(
                                text = modeText,
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
                        val buttonScale by animateFloatAsState(
                            targetValue = if (isCapturing) 0.9f else 1f,
                            animationSpec = tween(100),
                            label = "button_scale"
                        )

                        Box(modifier = Modifier.size(80.dp), contentAlignment = Alignment.Center) {
                            val ringSize by animateFloatAsState(
                                targetValue = if (isCapturing) 90f else 80f,
                                animationSpec = tween(200),
                                label = "ring_size"
                            )

                            Box(
                                modifier = Modifier
                                    .size(ringSize.dp)
                                    .border(
                                        width = if (isCapturing) 4.dp else 3.dp,
                                        color = if (isCapturing) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.5f),
                                        shape = CircleShape
                                    )
                            )

                            Button(
                                onClick = {
                                    if (!isCapturing && !viewModel.isLoading) {
                                        isCapturing = true
                                        captureFlash = true
                                        CoroutineScope(Dispatchers.Main).launch {
                                            delay(100)
                                            captureFlash = false
                                        }
                                        takePhoto(
                                            context = context,
                                            imageCapture = imageCapture,
                                            onImageCaptured = { file ->
                                                isCapturing = false
                                                viewModel.onPhotoCaptured(file, context)
                                            },
                                            onError = { errorMsg ->
                                                isCapturing = false
                                                viewModel.showError(errorMsg)
                                            }
                                        )
                                    }
                                },
                                enabled = !isCapturing && !viewModel.isLoading,
                                shape = CircleShape,
                                modifier = Modifier.size(70.dp).scale(buttonScale),
                                colors = ButtonDefaults.buttonColors(containerColor = if (isCapturing) MaterialTheme.colorScheme.primary else Color.White)
                            ) {
                                Box(modifier = Modifier.size(60.dp).border(2.dp, if (isCapturing) Color.White else Color.Black, CircleShape))
                            }
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
                NutritionBreakdownSheet(
                    product = product,
                    onDismiss = { viewModel.resetScanner() },
                    onSave = { updatedProduct ->
                        viewModel.saveScannedFood(updatedProduct)
                        viewModel.resetScanner()
                        onBackClick()
                    },
                    viewModel = viewModel
                )
            }
        }
    }
}

// ... (Keep NutritionBreakdownSheet, NutritionCard, MacroCard, PortionButton exactly as they were in the previous file) ...
// I will paste them here again just to be safe so you have one full file.

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NutritionBreakdownSheet(
    product: FoodProduct,
    onDismiss: () -> Unit,
    onSave: (FoodProduct) -> Unit,
    viewModel: ScanViewModel
) {
    var editedName by remember(product) { mutableStateOf(product.name) }
    var portionSize by remember(product) { mutableStateOf(product.portionSizeGrams) }
    var editedCalories by remember(product) { mutableStateOf(product.calories.toString()) }
    var editedProtein by remember(product) { mutableStateOf(product.protein.toString()) }
    var editedCarbs by remember(product) { mutableStateOf(product.carbs.toString()) }
    var editedFat by remember(product) { mutableStateOf(product.fat.toString()) }
    var expanded by remember { mutableStateOf(false) }

    val actualCalories = if (product.baseSizeGrams > 0) {
        ((editedCalories.toDoubleOrNull() ?: product.calories.toDouble()) / product.baseSizeGrams * portionSize).toInt()
    } else {
        editedCalories.toIntOrNull() ?: product.calories
    }

    val actualProtein = if (product.baseSizeGrams > 0) {
        (editedProtein.toDoubleOrNull() ?: product.protein) / product.baseSizeGrams * portionSize
    } else {
        editedProtein.toDoubleOrNull() ?: product.protein
    }

    val actualCarbs = if (product.baseSizeGrams > 0) {
        (editedCarbs.toDoubleOrNull() ?: product.carbs) / product.baseSizeGrams * portionSize
    } else {
        editedCarbs.toDoubleOrNull() ?: product.carbs
    }

    val actualFat = if (product.baseSizeGrams > 0) {
        (editedFat.toDoubleOrNull() ?: product.fat) / product.baseSizeGrams * portionSize
    } else {
        editedFat.toDoubleOrNull() ?: product.fat
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxHeight(0.9f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.scan_edit_save_title),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = editedName,
                    onValueChange = {
                        editedName = it
                        viewModel.onEditNameChange(it)
                        expanded = true
                    },
                    label = { Text(stringResource(R.string.scan_food_name_label)) },
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
                                    val brandText = if (suggestion.brand.isNullOrBlank()) "" else stringResource(R.string.scan_brand_label, suggestion.brand)
                                    Text(
                                        text = stringResource(R.string.scan_calories_display, suggestion.calories) + if (brandText.isNotEmpty()) " - $brandText" else "",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            },
                            onClick = {
                                editedName = suggestion.name
                                editedCalories = suggestion.calories.toString()
                                editedProtein = suggestion.protein.toString()
                                editedCarbs = suggestion.carbs.toString()
                                editedFat = suggestion.fat.toString()
                                viewModel.onSuggestionSelected(suggestion)
                                expanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = stringResource(R.string.portion_size_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                PortionButton(
                    text = stringResource(R.string.portion_size_100g),
                    selected = portionSize == 100,
                    onClick = { portionSize = 100 },
                    modifier = Modifier.weight(1f)
                )
                PortionButton(
                    text = "150g",
                    selected = portionSize == 150,
                    onClick = { portionSize = 150 },
                    modifier = Modifier.weight(1f)
                )
                PortionButton(
                    text = "200g",
                    selected = portionSize == 200,
                    onClick = { portionSize = 200 },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = portionSize.toString(),
                onValueChange = { portionSize = it.toIntOrNull() ?: 100 },
                label = { Text(stringResource(R.string.portion_size_grams)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = stringResource(R.string.nutrition_breakdown_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                NutritionCard(
                    title = stringResource(R.string.nutrition_total_weight),
                    value = stringResource(R.string.nutrition_grams_label, portionSize),
                    modifier = Modifier.weight(1f),
                    color = MaterialTheme.colorScheme.primaryContainer
                )
                NutritionCard(
                    title = stringResource(R.string.nutrition_energy),
                    value = stringResource(R.string.nutrition_kcal_label, actualCalories),
                    modifier = Modifier.weight(1f),
                    color = MaterialTheme.colorScheme.secondaryContainer
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MacroCard(
                    title = stringResource(R.string.nutrition_protein),
                    value = String.format("%.1fg", actualProtein),
                    color = Color(0xFFFF9800),
                    modifier = Modifier.weight(1f)
                )
                MacroCard(
                    title = stringResource(R.string.nutrition_carbs),
                    value = String.format("%.1fg", actualCarbs),
                    color = Color(0xFF2196F3),
                    modifier = Modifier.weight(1f)
                )
                MacroCard(
                    title = stringResource(R.string.nutrition_fat),
                    value = String.format("%.1fg", actualFat),
                    color = Color(0xFF4CAF50),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = stringResource(R.string.manual_edit_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Edit values per ${product.baseSizeGrams}g (reference size)",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = editedCalories,
                    onValueChange = { editedCalories = it },
                    label = { Text(stringResource(R.string.manual_edit_calories)) },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = editedProtein,
                    onValueChange = { editedProtein = it },
                    label = { Text(stringResource(R.string.manual_edit_protein)) },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                OutlinedTextField(
                    value = editedCarbs,
                    onValueChange = { editedCarbs = it },
                    label = { Text(stringResource(R.string.manual_edit_carbs)) },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                OutlinedTextField(
                    value = editedFat,
                    onValueChange = { editedFat = it },
                    label = { Text(stringResource(R.string.manual_edit_fat)) },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    val updatedProduct = product.copy(
                        name = editedName,
                        calories = editedCalories.toIntOrNull() ?: product.calories,
                        protein = editedProtein.toDoubleOrNull() ?: product.protein,
                        carbs = editedCarbs.toDoubleOrNull() ?: product.carbs,
                        fat = editedFat.toDoubleOrNull() ?: product.fat,
                        portionSizeGrams = portionSize
                    )
                    onSave(updatedProduct)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.scan_save_button))
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun NutritionCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.surfaceVariant
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = color),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun MacroCard(
    title: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.2f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

@Composable
fun PortionButton(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(text) },
        modifier = modifier
    )
}