package com.gemwallet.android.application.wallet_import.services

import com.wallet.core.primitives.Wallet
import kotlinx.coroutines.flow.Flow

interface ImportAssets {

    fun importAssets(wallet: Wallet)
}