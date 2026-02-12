package com.example.calorie_tracker.presentation.scan

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.calorie_tracker.R
import com.example.calorie_tracker.common.Resource
import com.example.calorie_tracker.domain.model.FoodProduct
import com.example.calorie_tracker.domain.model.MealType
import com.example.calorie_tracker.domain.use_case.AddFoodUC
import com.example.calorie_tracker.domain.use_case.GetFoodByBarcodeUC
import com.example.calorie_tracker.domain.use_case.SearchFoodUC
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.File
import javax.inject.Inject

// Removed FOOD mode
enum class ScanMode { BARCODE, TEXT }

@HiltViewModel
class ScanViewModel @Inject constructor(
    private val getFoodByBarcodeUC: GetFoodByBarcodeUC,
    private val addFoodUC: AddFoodUC,
    private val searchFoodUC: SearchFoodUC,
    @ApplicationContext private val context: Context
) : ViewModel() {

    var scanMode by mutableStateOf(ScanMode.BARCODE)
    var scannedProduct by mutableStateOf<FoodProduct?>(null)
    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)

    var nameSuggestions by mutableStateOf<List<FoodProduct>>(emptyList())
    var isSearchingSuggestions by mutableStateOf(false)

    // This is set when navigating to the screen
    var selectedMealType by mutableStateOf(MealType.SNACK)

    private var lastScannedBarcode: String? = null
    private val TAG = "[SCAN_DEBUG]"

    // --- 1. Barcode Logic ---
    fun onBarcodeDetected(barcode: String) {
        if (scanMode != ScanMode.BARCODE) return
        if (isLoading || barcode == lastScannedBarcode) return

        Log.d(TAG, "Barcode detected: $barcode")
        lastScannedBarcode = barcode
        fetchProduct(barcode)
    }

    // --- 2. Photo Logic (Text Only) ---
    fun onPhotoCaptured(file: File, context: Context) {
        isLoading = true
        errorMessage = null

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val image = InputImage.fromFilePath(context, Uri.fromFile(file))
                // Only Text analysis now
                analyzeText(image)
            } catch (e: Exception) {
                showError(context.getString(R.string.scan_error_processing_image, e.message ?: ""))
            }
        }
    }

    // --- 3. Autocomplete Logic ---
    fun onEditNameChange(query: String) {
        if (query.length < 3) {
            nameSuggestions = emptyList()
            return
        }

        viewModelScope.launch {
            isSearchingSuggestions = true
            delay(500)
            when (val result = searchFoodUC(query)) {
                is Resource.Success -> {
                    nameSuggestions = result.data ?: emptyList()
                    isSearchingSuggestions = false
                }
                is Resource.Error -> isSearchingSuggestions = false
                is Resource.Loading -> isSearchingSuggestions = true
            }
        }
    }

    fun onSuggestionSelected(product: FoodProduct) {
        scannedProduct = product.copy(
            name = product.name,
            calories = product.calories,
            brand = product.brand,
            nutriScore = product.nutriScore
        )
        nameSuggestions = emptyList()
    }

    fun startManualEntry() {
        scannedProduct = FoodProduct(
            name = "",
            brand = "",
            calories = 0,
            protein = 0.0, carbs = 0.0, fat = 0.0,
            imageUrl = null, barcode = null,
            baseServingSize = 100.0, baseServingUnit = "g", portionSizeGrams = 100,
            source = "User", nutriScore = null, novaGroup = null, isHighSugar = false
        )
    }

    // --- ML Kit: Text Recognition ---
    private suspend fun analyzeText(image: InputImage) {
        try {
            val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
            val visionText = recognizer.process(image).await()
            val rawText = visionText.text

            val calories = parseNutritionText(rawText)

            if (calories > 0) {
                scannedProduct = FoodProduct(
                    name = context.getString(R.string.scan_scanned_nutrition_label),
                    brand = context.getString(R.string.scan_custom_entry),
                    calories = calories,
                    protein = 0.0, carbs = 0.0, fat = 0.0,
                    imageUrl = null, barcode = null,
                    baseServingSize = 100.0, baseServingUnit = "g", portionSizeGrams = 100,
                    source = "User", nutriScore = null, novaGroup = null, isHighSugar = false
                )
                isLoading = false
            } else {
                val possibleBrand = visionText.textBlocks
                    .maxByOrNull { it.boundingBox?.height() ?: 0 }
                    ?.text?.replace("\n", " ")?.take(30)

                if (!possibleBrand.isNullOrBlank()) {
                    performSearch(possibleBrand)
                } else {
                    startManualEntry() // Fallback to manual
                    isLoading = false
                }
            }
        } catch (e: Exception) {
            showError(context.getString(R.string.scan_error_text_recognition, e.message ?: ""))
        }
    }

    private fun parseNutritionText(text: String): Int {
        // 1. Slash Pattern (2190 / 524)
        val slashRegex = Regex("""(\d{3,4})\s*[a-zA-Z]*\s*[/|]\s*(\d{2,3})""")
        val slashMatch = slashRegex.find(text)
        if (slashMatch != null) {
            val num1 = slashMatch.groupValues[1].toIntOrNull() ?: 0
            val num2 = slashMatch.groupValues[2].toIntOrNull() ?: 0
            if (num1 > num2 && num1 < num2 * 5) return num2
        }

        // 2. kcal Pattern
        val kcalRegex = Regex("""(\d+)\s*(?:kcal|cal|Wcal|kcl)""", RegexOption.IGNORE_CASE)
        val kcalMatch = kcalRegex.find(text)
        if (kcalMatch != null) return kcalMatch.groupValues[1].toIntOrNull() ?: 0

        // 3. kJ Pattern
        val kjRegex = Regex("""(\d+)\s*(?:kJ|kj|KI|Kj)""", RegexOption.IGNORE_CASE)
        val kjMatch = kjRegex.find(text)
        if (kjMatch != null) {
            val kjValue = kjMatch.groupValues[1].toIntOrNull() ?: 0
            return (kjValue / 4.184).toInt()
        }
        return 0
    }

    private fun performSearch(query: String) {
        viewModelScope.launch {
            when (val result = searchFoodUC(query)) {
                is Resource.Success -> {
                    val bestMatch = result.data?.firstOrNull()
                    if (bestMatch != null) {
                        scannedProduct = bestMatch.copy(name = "AI: ${bestMatch.name}")
                    } else {
                        startManualEntry()
                    }
                    isLoading = false
                }
                is Resource.Error -> {
                    startManualEntry()
                    isLoading = false
                }
                is Resource.Loading -> isLoading = true
            }
        }
    }

    private fun fetchProduct(barcode: String) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            when (val result = getFoodByBarcodeUC(barcode)) {
                is Resource.Success -> {
                    scannedProduct = result.data
                    isLoading = false
                }
                is Resource.Error -> {
                    showError(result.message ?: context.getString(R.string.scan_error_unknown))
                    lastScannedBarcode = null
                }
                is Resource.Loading -> isLoading = true
            }
        }
    }

    fun showError(msg: String) {
        errorMessage = msg
        isLoading = false
        viewModelScope.launch {
            delay(3000)
            errorMessage = null
        }
    }

    fun saveScannedFood(foodToSave: FoodProduct? = null) {
        val food = foodToSave ?: scannedProduct
        food?.let {
            viewModelScope.launch {
                addFoodUC(it, selectedMealType)
            }
        }
    }

    fun resetScanner() {
        scannedProduct = null
        errorMessage = null
        lastScannedBarcode = null
    }
}