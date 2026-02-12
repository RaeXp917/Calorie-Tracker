package com.example.calorie_tracker.di

import android.app.Application
import androidx.room.Room
import com.example.calorie_tracker.common.Constants
import com.example.calorie_tracker.data.local.AppDatabase
import com.example.calorie_tracker.data.local.dao.FoodLogDao
import com.example.calorie_tracker.data.remote.OpenFoodFactsApi
import com.example.calorie_tracker.data.repository.FoodRepositoryImpl
import com.example.calorie_tracker.domain.repository.FoodRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY // Logs Headers + Body
        }
        return OkHttpClient.Builder()
            .addInterceptor(logging)
            .connectTimeout(30, TimeUnit.SECONDS) // Fix for "Timeout" error
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideOpenFoodFactsApi(client: OkHttpClient): OpenFoodFactsApi {
        return Retrofit.Builder()
            .baseUrl(Constants.BASE_URL)
            .client(client) // Attach the debugger
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(OpenFoodFactsApi::class.java)
    }

    @Provides
    @Singleton
    fun provideDatabase(app: Application): AppDatabase {
        return Room.databaseBuilder(
            app,
            AppDatabase::class.java,
            "calorie_tracker_db"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun provideFoodLogDao(db: AppDatabase): FoodLogDao {
        return db.foodLogDao()
    }

    @Provides
    @Singleton
    fun provideFoodRepository(
        api: OpenFoodFactsApi,
        dao: FoodLogDao
    ): FoodRepository {
        return FoodRepositoryImpl(api, dao)
    }
}