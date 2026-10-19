package com.example.androidinterview.feature.merchant.di

import com.example.androidinterview.feature.merchant.data.MerchantRepositoryImpl
import com.example.androidinterview.feature.merchant.domain.repository.MerchantRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DashboardModule {

    @Binds
    @Singleton
    abstract fun bindDashboardRepository(
        impl: MerchantRepositoryImpl
    ): MerchantRepository
}