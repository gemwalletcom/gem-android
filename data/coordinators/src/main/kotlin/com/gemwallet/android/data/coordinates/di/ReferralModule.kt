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
import com.gemwallet.android.data.services.gemapi.GemApiClient
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
        gemApiClient: GemApiClient,
        getAuthPayload: GetAuthPayload
    ): CreateReferral {
        return CreateReferralImpl(
            gemApiClient = gemApiClient,
            getAuthPayload = getAuthPayload
        )
    }

    @Provides
    @Singleton
    fun provideGetRewards(
        gemApiClient: GemApiClient,
    ): GetRewards {
        return GetRewardsImpl(
            gemApiClient = gemApiClient,
        )
    }

    @Provides
    @Singleton
    fun provideRedeem(
        sessionRepository: SessionRepository,
        gemApiClient: GemApiClient,
        getAuthPayload: GetAuthPayload,
        tokensRepository: TokensRepository,
        assetsRepository: AssetsRepository,
    ): Redeem {
        return RedeemImpl(
            sessionRepository = sessionRepository,
            gemApiClient = gemApiClient,
            getAuthPayload = getAuthPayload,
            tokensRepository = tokensRepository,
            assetsRepository = assetsRepository,
        )
    }

    @Provides
    @Singleton
    fun provideUseReferralCode(
        gemApiClient: GemApiClient,
        getAuthPayload: GetAuthPayload
    ): UseReferralCode {
        return UseReferralCodeImpl(
            gemApiClient = gemApiClient,
            getAuthPayload = getAuthPayload
        )
    }
}