package com.gemwallet.android.application.wallet_import.services

import com.wallet.core.primitives.Wallet

interface ImportAssets {

    fun importAssets(wallet: Wallet)
}