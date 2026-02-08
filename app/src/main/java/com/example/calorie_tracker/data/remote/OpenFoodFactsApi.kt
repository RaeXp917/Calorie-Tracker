package com.example.calorie_tracker.data.remote

import com.example.calorie_tracker.data.remote.dto.OpenFoodFactsResponse
import retrofit2.http.GET
import retrofit2.http.Path

interface OpenFoodFactsApi {
    @GET("api/v0/product/{barcode}.json")
    suspend fun getProductByBarcode(
        @Path("barcode") barcode: String
    ): OpenFoodFactsResponse

    @GET("cgi/search.pl?search_simple=1&action=process&json=1")
    suspend fun searchProductsByName(
        @retrofit2.http.Query("search_terms") query: String
    ): com.example.calorie_tracker.data.remote.dto.OpenFoodFactsSearchResponse

}