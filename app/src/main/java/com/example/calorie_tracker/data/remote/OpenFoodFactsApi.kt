package com.example.calorie_tracker.data.remote

import com.example.calorie_tracker.data.remote.dto.OpenFoodFactsResponse
import retrofit2.http.GET
import retrofit2.http.Path

interface OpenFoodFactsApi {

    // This function calls: https://world.openfoodfacts.org/api/v0/product/{barcode}.json
    @GET("api/v0/product/{barcode}.json")
    suspend fun getProductByBarcode(
        @Path("barcode") barcode: String
    ): OpenFoodFactsResponse
}