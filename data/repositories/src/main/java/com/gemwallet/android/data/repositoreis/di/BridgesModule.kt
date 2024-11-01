package com.gemwallet.android.data.repositoreis.di

import com.gemwallet.android.data.repositoreis.bridge.BridgesRepository
import com.gemwallet.android.data.repositoreis.wallets.WalletsRepository
import com.gemwallet.android.data.service.store.database.ConnectionsDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object BridgesModule {
    @Singleton
    @Provides
    fun provideBridgeRepository(
        walletsRepository: WalletsRepository,
        connectionsDao: ConnectionsDao,
    ): BridgesRepository = BridgesRepository(
        walletsRepository,
        connectionsDao
    )
}

