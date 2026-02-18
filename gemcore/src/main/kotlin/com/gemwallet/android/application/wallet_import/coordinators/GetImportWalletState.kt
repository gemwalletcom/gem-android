package com.gemwallet.android.application.wallet_import.coordinators

import com.gemwallet.android.application.wallet_import.services.ImportAssets
import com.gemwallet.android.application.wallet_import.values.ImportWalletState
import kotlinx.coroutines.flow.Flow

interface GetImportWalletState {
    fun getImportState(walletId: String): Flow<ImportWalletState>


}