package com.gemwallet.android.blockchain.clients

import com.gemwallet.android.model.ConfirmParams
import com.gemwallet.android.model.SignerParams
import com.wallet.core.primitives.Account

interface SignerPreload : BlockchainClient {
    suspend operator fun invoke(
        owner: Account,
        params: ConfirmParams,
    ): Result<SignerParams>
}