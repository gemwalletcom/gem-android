package com.gemwallet.android.data.repositoreis.di

import com.gemwallet.android.cases.device.GetDeviceIdCase
import com.gemwallet.android.cases.nft.GetAssetNftCase
import com.gemwallet.android.cases.nft.GetListNftCase
import com.gemwallet.android.cases.nft.LoadNFTCase
import com.gemwallet.android.data.repositoreis.nft.NftRepository
import com.gemwallet.android.data.service.store.database.NftDao
import com.gemwallet.android.data.services.gemapi.GemApiClient
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class NftModule {

    @Provides
    @Singleton
    fun provideNftRepository(
        gemApiClient: GemApiClient,
        getDeviceIdCase: GetDeviceIdCase,
        nftDao: NftDao
    ): NftRepository {
        return NftRepository(gemApiClient, getDeviceIdCase, nftDao)
    }

    @Provides
    fun provideLoadNftCase(nftRepository: NftRepository): LoadNFTCase = nftRepository

    @Provides
    fun provideGetNfCase(nftRepository: NftRepository): GetListNftCase = nftRepository

    @Provides
    fun provideGetAssetNftCase(nftRepository: NftRepository): GetAssetNftCase = nftRepository
}