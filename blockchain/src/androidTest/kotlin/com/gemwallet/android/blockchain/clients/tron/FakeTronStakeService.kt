package com.gemwallet.android.blockchain.clients.tron

import com.gemwallet.android.blockchain.clients.tron.services.TronStakeService
import com.wallet.core.blockchain.tron.TronReward
import com.wallet.core.blockchain.tron.WitnessesList

class FakeTronStakeService(
    private val reward: Long? = null
) : TronStakeService {
    var addressRequest: String? = null


    override suspend fun listwitnesses(): Result<WitnessesList> {
        TODO("Not yet implemented")
    }

    override suspend fun getReward(address: String): Result<TronReward> {
        addressRequest = address
        return Result.success(TronReward(reward ?: return Result.failure(Exception())))
    }
}