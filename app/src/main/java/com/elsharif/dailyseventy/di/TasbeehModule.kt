package com.elsharif.dailyseventy.di

import com.example.core.data.repository.TasbeehRepository
import com.example.core.usecase.GetTasbeehCountUseCase
import com.example.core.usecase.IncreaseTasbeehUseCase
import com.example.core.usecase.ResetTasbeehUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object TasbeehModule {

    @Provides
    fun provideIncreaseTasbeehUseCase(
        repo: TasbeehRepository
    ): IncreaseTasbeehUseCase = IncreaseTasbeehUseCase(repo)

    @Provides
    fun provideResetTasbeehUseCase(
        repo: TasbeehRepository
    ): ResetTasbeehUseCase = ResetTasbeehUseCase(repo)

    @Provides
    fun provideGetTasbeehCountUseCase(
        repo: TasbeehRepository
    ): GetTasbeehCountUseCase = GetTasbeehCountUseCase(repo)
}
