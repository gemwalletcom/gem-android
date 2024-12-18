package com.gemwallet.android.blockchain.clients.tron.services

import com.wallet.core.blockchain.tron.models.TronChainParameters
import com.wallet.core.blockchain.tron.models.TronReward
import com.wallet.core.blockchain.tron.models.WitnessesList
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface TronStakeService {
    @GET("/wallet/listwitnesses")
    suspend fun listwitnesses(): Result<WitnessesList>

    @POST("/wallet/getReward")
    suspend fun getReward(@Body address: String): Result<TronReward>

}