package com.gemwallet.android.cases.nft

import com.wallet.core.primitives.Wallet

interface LoadNFTCase {
    suspend fun loadNFT(wallet: Wallet)
}