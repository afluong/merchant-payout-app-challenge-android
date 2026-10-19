package com.example.androidinterview.feature.payout.di

import com.example.androidinterview.feature.payout.data.PayoutRepositoryImpl
import com.example.androidinterview.feature.payout.domain.repository.PayoutRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class PayoutModule {
    @Binds
    @Singleton
    abstract fun bindPayoutRepository(
        impl: PayoutRepositoryImpl
    ): PayoutRepository
}