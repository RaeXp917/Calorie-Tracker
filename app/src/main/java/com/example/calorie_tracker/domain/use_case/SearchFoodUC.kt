package com.example.calorie_tracker.domain.use_case

import com.example.calorie_tracker.common.Resource
import com.example.calorie_tracker.domain.model.FoodProduct
import com.example.calorie_tracker.domain.repository.FoodRepository
import javax.inject.Inject

class SearchFoodUC @Inject constructor(
    private val repository: FoodRepository
) {
    suspend operator fun invoke(query: String): Resource<List<FoodProduct>> {
        if (query.isBlank()) return Resource.Error("Empty query")
        return repository.searchFoodByName(query)
    }
}