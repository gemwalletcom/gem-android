package com.gemwallet.android.data.repositoreis.di

import com.gemwallet.android.data.repositoreis.assets.AssetsRepository
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface WidgetEntryPoint {
    fun assetsRepository(): AssetsRepository
}