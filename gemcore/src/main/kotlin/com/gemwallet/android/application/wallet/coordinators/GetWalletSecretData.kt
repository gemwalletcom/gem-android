package com.gemwallet.android.application.wallet.coordinators

import com.gemwallet.android.domains.wallet.values.WalletSecretDataValue
import kotlinx.coroutines.flow.Flow

interface GetWalletSecretData {
    fun getSecretData(walletId: String): Flow<WalletSecretDataValue>
}