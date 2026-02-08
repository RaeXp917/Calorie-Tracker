package com.example.calorie_tracker.presentation.scan

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.calorie_tracker.common.Resource
import com.example.calorie_tracker.domain.model.FoodProduct
import com.example.calorie_tracker.domain.use_case.AddFoodUC
import com.example.calorie_tracker.domain.use_case.GetFoodByBarcodeUC
import com.example.calorie_tracker.domain.use_case.SearchFoodUC
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

enum class ScanMode { BARCODE, FOOD, TEXT }

@HiltViewModel
class ScanViewModel @Inject constructor(
    private val getFoodByBarcodeUC: GetFoodByBarcodeUC,
    private val addFoodUC: AddFoodUC,
    private val searchFoodUC: SearchFoodUC
) : ViewModel() {

    // UI State
    var scanMode by mutableStateOf(ScanMode.BARCODE)
    var scannedProduct by mutableStateOf<FoodProduct?>(null)
    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)

    // Autocomplete State (NEW)
    var nameSuggestions by mutableStateOf<List<FoodProduct>>(emptyList())
    var isSearchingSuggestions by mutableStateOf(false)

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

    // --- 2. Photo Logic ---
    fun onPhotoCaptured(file: File, context: Context) {
        isLoading = true
        errorMessage = null

        viewModelScope.launch(Dispatchers.IO) {
            try {
                Log.d(TAG, "Photo captured. Mode: $scanMode")
                val image = InputImage.fromFilePath(context, Uri.fromFile(file))

                if (scanMode == ScanMode.FOOD) {
                    analyzeImageLabels(image)
                } else if (scanMode == ScanMode.TEXT) {
                    analyzeText(image)
                }
            } catch (e: Exception) {
                showError("Error processing image: ${e.message}")
            }
        }
    }

    // --- 3. Autocomplete Logic (NEW) ---
    fun onEditNameChange(query: String) {
        if (query.length < 3) {
            nameSuggestions = emptyList()
            return
        }

        viewModelScope.launch {
            isSearchingSuggestions = true
            delay(500) // Debounce: Wait 500ms before searching

            when (val result = searchFoodUC(query)) {
                is Resource.Success -> {
                    nameSuggestions = result.data ?: emptyList()
                    isSearchingSuggestions = false
                }
                is Resource.Error -> {
                    isSearchingSuggestions = false
                }
                is Resource.Loading -> isSearchingSuggestions = true
            }
        }
    }

    fun onSuggestionSelected(product: FoodProduct) {
        // Update the popup with the selected suggestion
        scannedProduct = product.copy(
            name = product.name,
            calories = product.calories,
            brand = product.brand,
            nutriScore = product.nutriScore
        )
        nameSuggestions = emptyList() // Hide dropdown
    }

    // --- ML Kit Logic ---
    private fun analyzeImageLabels(image: InputImage) {
        val labeler = ImageLabeling.getClient(ImageLabelerOptions.DEFAULT_OPTIONS)
        labeler.process(image)
            .addOnSuccessListener { labels ->
                val topLabels = labels.sortedByDescending { it.confidence }.take(3)
                if (topLabels.isNotEmpty()) {
                    val bestGuess = topLabels.first().text
                    performSearch(bestGuess)
                } else {
                    showError("Could not identify food.")
                }
            }
            .addOnFailureListener { e -> showError("AI Error: ${e.message}") }
    }

    private fun analyzeText(image: InputImage) {
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        recognizer.process(image)
            .addOnSuccessListener { visionText ->
                val rawText = visionText.text
                Log.d(TAG, "OCR Raw Text:\n$rawText")

                val calories = parseNutritionText(rawText)

                if (calories > 0) {
                    scannedProduct = FoodProduct(
                        name = "Scanned Nutrition Label",
                        brand = "Custom Entry",
                        calories = calories,
                        protein = 0.0,
                        carbs = 0.0,
                        fat = 0.0,
                        imageUrl = null,
                        barcode = null,
                        nutriScore = null,
                        novaGroup = null,
                        isHighSugar = false
                    )
                    isLoading = false
                } else {
                    // Fallback to manual entry
                    scannedProduct = FoodProduct(
                        name = "Unknown Food",
                        brand = "Manual Entry",
                        calories = 0,
                        protein = 0.0,
                        carbs = 0.0,
                        fat = 0.0,
                        imageUrl = null,
                        barcode = null,
                        nutriScore = null,
                        novaGroup = null,
                        isHighSugar = false
                    )
                    isLoading = false
                }
            }
            .addOnFailureListener { e -> showError("Text Error: ${e.message}") }
    }

    private fun parseNutritionText(text: String): Int {
        // 1. Slash Pattern (2190 / 524)
        val slashRegex = Regex("""(\d{3,4})\s*[/|]\s*(\d{2,3})""")
        val slashMatch = slashRegex.find(text)
        if (slashMatch != null) {
            val num1 = slashMatch.groupValues[1].toIntOrNull() ?: 0
            val num2 = slashMatch.groupValues[2].toIntOrNull() ?: 0
            if (num1 > num2) return num2
        }

        // 2. kJ Pattern
        val kjRegex = Regex("""(\d+)\s*(?:kJ|kj|KI|Kj)""", RegexOption.IGNORE_CASE)
        val kjMatch = kjRegex.find(text)
        if (kjMatch != null) {
            val kjValue = kjMatch.groupValues[1].toIntOrNull() ?: 0
            return (kjValue / 4.184).toInt()
        }

        // 3. kcal Pattern
        val kcalRegex = Regex("""(\d+)\s*(?:kcal|cal|Wcal|kcl)""", RegexOption.IGNORE_CASE)
        val kcalMatch = kcalRegex.find(text)
        if (kcalMatch != null) {
            return kcalMatch.groupValues[1].toIntOrNull() ?: 0
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
                        showError("AI saw '$query', but no food found.")
                    }
                    isLoading = false
                }
                is Resource.Error -> showError("Search failed: ${result.message}")
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
                    showError(result.message ?: "Unknown Error")
                    lastScannedBarcode = null
                }
                is Resource.Loading -> isLoading = true
            }
        }
    }

    private fun showError(msg: String) {
        errorMessage = msg
        isLoading = false
        Log.e(TAG, "Error: $msg")
        viewModelScope.launch {
            delay(3000)
            errorMessage = null
        }
    }

    fun saveScannedFood(foodToSave: FoodProduct? = null) {
        val food = foodToSave ?: scannedProduct
        food?.let {
            viewModelScope.launch {
                addFoodUC(it)
            }
        }
    }

    fun resetScanner() {
        scannedProduct = null
        errorMessage = null
        lastScannedBarcode = null
    }
}