package com.gemwallet.android.data.coordinates.di

import com.gemwallet.android.application.GetAuthPayload
import com.gemwallet.android.application.referral.coordinators.CreateReferral
import com.gemwallet.android.application.referral.coordinators.GetRewards
import com.gemwallet.android.application.referral.coordinators.Redeem
import com.gemwallet.android.application.referral.coordinators.UseReferralCode
import com.gemwallet.android.data.coordinates.referral.CreateReferralImpl
import com.gemwallet.android.data.coordinates.referral.GetRewardsImpl
import com.gemwallet.android.data.coordinates.referral.RedeemImpl
import com.gemwallet.android.data.coordinates.referral.UseReferralCodeImpl
import com.gemwallet.android.data.repositoreis.assets.AssetsRepository
import com.gemwallet.android.data.repositoreis.session.SessionRepository
import com.gemwallet.android.data.repositoreis.tokens.TokensRepository
import com.gemwallet.android.data.services.gemapi.GemDeviceApiClient
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object ReferralModule {
    @Provides
    @Singleton
    fun provideCreateReferral(
        gemDeviceApiClient: GemDeviceApiClient,
        getAuthPayload: GetAuthPayload,
    ): CreateReferral {
        return CreateReferralImpl(
            gemDeviceApiClient = gemDeviceApiClient,
            getAuthPayload = getAuthPayload
        )
    }

    @Provides
    @Singleton
    fun provideGetRewards(
        gemDeviceApiClient: GemDeviceApiClient,
    ): GetRewards {
        return GetRewardsImpl(
            gemDeviceApiClient = gemDeviceApiClient,
        )
    }

    @Provides
    @Singleton
    fun provideRedeem(
        sessionRepository: SessionRepository,
        gemDeviceApiClient: GemDeviceApiClient,
        getAuthPayload: GetAuthPayload,
        tokensRepository: TokensRepository,
        assetsRepository: AssetsRepository,
    ): Redeem {
        return RedeemImpl(
            sessionRepository = sessionRepository,
            gemDeviceApiClient = gemDeviceApiClient,
            getAuthPayload = getAuthPayload,
            tokensRepository = tokensRepository,
            assetsRepository = assetsRepository,
        )
    }

    @Provides
    @Singleton
    fun provideUseReferralCode(
        gemDeviceApiClient: GemDeviceApiClient,
        getAuthPayload: GetAuthPayload
    ): UseReferralCode {
        return UseReferralCodeImpl(
            gemDeviceApiClient = gemDeviceApiClient,
            getAuthPayload = getAuthPayload
        )
    }
}