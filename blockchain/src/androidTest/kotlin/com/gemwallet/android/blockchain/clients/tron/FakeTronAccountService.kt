package com.gemwallet.android.blockchain.clients.tron

import com.gemwallet.android.blockchain.clients.tron.services.TronAccountsService
import com.wallet.core.blockchain.tron.models.TronAccount
import com.wallet.core.blockchain.tron.models.TronAccountRequest
import com.wallet.core.blockchain.tron.models.TronAccountUsage

class FakeTronAccountService(
    private val tronAccount: TronAccount? = null
) : TronAccountsService {
    var accountRequest: TronAccountRequest? = null
    var usageRequest: TronAccountRequest? = null

    override suspend fun getAccount(addressRequest: TronAccountRequest): Result<TronAccount> {
        this.accountRequest = addressRequest
        return Result.success(tronAccount ?: return Result.failure(Exception()))
    }

    override suspend fun getAccountUsage(addressRequest: TronAccountRequest): Result<TronAccountUsage> {
        this.usageRequest = addressRequest

        return Result.success(
            TronAccountUsage(
                freeNetUsed = 1,
                freeNetLimit = 2,
            )
        )
    }


}