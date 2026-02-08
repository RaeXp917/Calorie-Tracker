package com.example.calorie_tracker.domain.use_case

import com.example.calorie_tracker.common.Resource
import com.example.calorie_tracker.domain.model.FoodProduct
import com.example.calorie_tracker.domain.repository.FoodRepository
import javax.inject.Inject

class GetFoodByBarcodeUC @Inject constructor(
    private val repository: FoodRepository
) {
    suspend operator fun invoke(barcode: String): Resource<FoodProduct> {
        if (barcode.isBlank()) {
            return Resource.Error("Invalid Barcode")
        }
        return repository.getFoodByBarcode(barcode)
    }
}