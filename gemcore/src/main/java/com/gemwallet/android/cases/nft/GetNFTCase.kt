package com.gemwallet.android.cases.nft

import com.wallet.core.primitives.NFTData
import kotlinx.coroutines.flow.Flow

interface GetNFTCase {
    fun getNft(collectionId: String? = null): Flow<List<NFTData>>
}