package com.gemwallet.android.blockchain.clients.tron

import com.gemwallet.android.blockchain.clients.tron.services.TronAccountsService
import com.wallet.core.blockchain.tron.models.TronAccount
import com.wallet.core.blockchain.tron.models.TronAccountRequest

class TestTronAccountService(
    private val tronAccount: TronAccount? = null
) : TronAccountsService {
    var addressRequest: TronAccountRequest? = null

    override suspend fun getAccount(addressRequest: TronAccountRequest): Result<TronAccount> {
        this.addressRequest = addressRequest
        return Result.success(tronAccount ?: return Result.failure(Exception()))
    }


}