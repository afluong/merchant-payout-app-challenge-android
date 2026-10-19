package com.example.androidinterview.core.di

import com.example.androidinterview.core.network.mock.MockServerManager
import com.example.androidinterview.core.network.ApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton
import kotlin.concurrent.thread

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideRetrofit(): Retrofit {
        var baseUrl: String? = null
        thread { baseUrl = MockServerManager.baseUrl }.join()

        return Retrofit.Builder()
            .baseUrl(checkNotNull(baseUrl))
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideApiService(retrofit: Retrofit): ApiService {
        return retrofit.create(ApiService::class.java)
    }
}