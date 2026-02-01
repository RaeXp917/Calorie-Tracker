package com.example.calorie_tracker.di

import com.example.calorie_tracker.common.Constants
import com.example.calorie_tracker.data.remote.OpenFoodFactsApi
import com.example.calorie_tracker.data.repository.FoodRepositoryImpl
import com.example.calorie_tracker.domain.repository.FoodRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideOpenFoodFactsApi(): OpenFoodFactsApi {
        return Retrofit.Builder()
            .baseUrl("https://world.openfoodfacts.org/") // We need to add this to Constants later
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(OpenFoodFactsApi::class.java)
    }

    @Provides
    @Singleton
    fun provideFoodRepository(api: OpenFoodFactsApi): FoodRepository {
        return FoodRepositoryImpl(api)
    }
}