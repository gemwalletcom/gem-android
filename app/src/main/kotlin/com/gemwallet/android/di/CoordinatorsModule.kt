package com.gemwallet.android.di

import com.gemwallet.android.application.perpetual.coordinators.SyncPerpetualPositions
import com.gemwallet.android.application.perpetual.coordinators.SyncPerpetuals
import com.gemwallet.android.blockchain.services.PerpetualService
import com.gemwallet.android.data.coordinates.perpetuals.GetPerpetualsImpl
import com.gemwallet.android.data.coordinates.perpetuals.SyncPerpetualPositionsImpl
import com.gemwallet.android.data.coordinates.perpetuals.SyncPerpetualsImpl
import com.gemwallet.android.data.repositoreis.perpetual.PerpetualRepository
import com.gemwallet.android.data.repositoreis.session.SessionRepository
import com.wallet.core.primitives.Chain
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object CoordinatorsModule { // TODO: Move to coordinators
    @Provides
    @Singleton
    fun provideSyncPerpetuals(
        perpetualService: PerpetualService,
        perpetualRepository: PerpetualRepository,
    ): SyncPerpetuals {
        return SyncPerpetualsImpl(
            perpetualService = perpetualService,
            perpetualRepository = perpetualRepository,
            chains = listOf(Chain.HyperCore)
        )
    }

    @Provides
    @Singleton
    fun provideSyncPerpetualPositions(
        sessionRepository: SessionRepository,
        perpetualService: PerpetualService,
        perpetualRepository: PerpetualRepository,
    ): SyncPerpetualPositions {
        return SyncPerpetualPositionsImpl(
            sessionRepository = sessionRepository,
            perpetualService = perpetualService,
            perpetualRepository = perpetualRepository,
            chains = listOf(Chain.HyperCore)
        )
    }
}