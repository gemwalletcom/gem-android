package com.gemwallet.android.application.wallet_import.services

import com.wallet.core.primitives.Wallet

interface ImportAssets { // TODO: Move to coordinators?

    fun importAssets(wallet: Wallet)
}