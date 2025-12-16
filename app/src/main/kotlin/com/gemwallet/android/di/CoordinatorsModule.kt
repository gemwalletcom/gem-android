package com.gemwallet.android.di

import com.gemwallet.android.application.perpetual.coordinators.GetPerpetual
import com.gemwallet.android.application.perpetual.coordinators.GetPerpetualBalances
import com.gemwallet.android.application.perpetual.coordinators.GetPerpetualBalance
import com.gemwallet.android.application.perpetual.coordinators.GetPerpetualChartData
import com.gemwallet.android.application.perpetual.coordinators.GetPerpetualPosition
import com.gemwallet.android.application.perpetual.coordinators.GetPerpetualPositions
import com.gemwallet.android.application.perpetual.coordinators.GetPerpetuals
import com.gemwallet.android.application.perpetual.coordinators.SyncPerpetualPositions
import com.gemwallet.android.application.perpetual.coordinators.SyncPerpetuals
import com.gemwallet.android.application.perpetual.coordinators.TogglePerpetualPin
import com.gemwallet.android.blockchain.services.PerpetualService
import com.gemwallet.android.data.coordinates.perpetuals.GetPerpetualBalancesImpl
import com.gemwallet.android.data.coordinates.perpetuals.GetPerpetualBalanceImpl
import com.gemwallet.android.data.coordinates.perpetuals.GetPerpetualChartDataImpl
import com.gemwallet.android.data.coordinates.perpetuals.GetPerpetualImpl
import com.gemwallet.android.data.coordinates.perpetuals.GetPerpetualPositionImpl
import com.gemwallet.android.data.coordinates.perpetuals.GetPerpetualPositionsImpl
import com.gemwallet.android.data.coordinates.perpetuals.GetPerpetualsImpl
import com.gemwallet.android.data.coordinates.perpetuals.SyncPerpetualPositionsImpl
import com.gemwallet.android.data.coordinates.perpetuals.SyncPerpetualsImpl
import com.gemwallet.android.data.coordinates.perpetuals.TogglePerpetualPinImpl
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

    @Provides
    @Singleton
    fun provideGetPerpetualPositions(
        sessionRepository: SessionRepository,
        perpetualRepository: PerpetualRepository,
    ): GetPerpetualPositions {
        return GetPerpetualPositionsImpl(
            sessionRepository = sessionRepository,
            perpetualRepository = perpetualRepository,
        )
    }

    @Provides
    @Singleton
    fun provideGetPerpetualPosition(
        perpetualRepository: PerpetualRepository,
    ): GetPerpetualPosition {
        return GetPerpetualPositionImpl(
            perpetualRepository = perpetualRepository,
        )
    }

    @Provides
    @Singleton
    fun provideGetPerpetuals(
        perpetualRepository: PerpetualRepository,
    ): GetPerpetuals {
        return GetPerpetualsImpl(
            perpetualRepository = perpetualRepository,
        )
    }

    @Provides
    @Singleton
    fun provideGetPerpetual(
        perpetualRepository: PerpetualRepository,
    ): GetPerpetual {
        return GetPerpetualImpl(
            perpetualRepository = perpetualRepository,
        )
    }

    @Provides
    @Singleton
    fun provideGetPerpetualBalances(
        sessionRepository: SessionRepository,
        perpetualRepository: PerpetualRepository,
    ): GetPerpetualBalances {
        return GetPerpetualBalancesImpl(
            sessionRepository = sessionRepository,
            perpetualRepository = perpetualRepository,
        )
    }

    @Provides
    @Singleton
    fun provideGetPerpetualBalance(
        perpetualRepository: PerpetualRepository,
    ): GetPerpetualBalance {
        return GetPerpetualBalanceImpl(
            perpetualRepository = perpetualRepository,
        )
    }

    @Provides
    @Singleton
    fun provideTogglePerpetualPin(
        perpetualRepository: PerpetualRepository,
    ): TogglePerpetualPin {
        return TogglePerpetualPinImpl(
            perpetualRepository = perpetualRepository,
        )
    }

    @Provides
    @Singleton
    fun provideGetPerpetualChartData(
        perpetualService: PerpetualService,
    ): GetPerpetualChartData {
        return GetPerpetualChartDataImpl(
            perpetualService = perpetualService,
        )
    }
}