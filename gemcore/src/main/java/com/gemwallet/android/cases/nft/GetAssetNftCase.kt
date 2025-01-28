package com.gemwallet.android.cases.nft

import com.wallet.core.primitives.NFTData
import kotlinx.coroutines.flow.Flow

interface GetAssetNftCase {
    fun getAssetNft(id: String): Flow<NFTData>
}