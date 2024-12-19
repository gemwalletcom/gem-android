package com.gemwallet.android.blockchain.clients.tron.services

import com.wallet.core.blockchain.tron.models.TronBlock
import com.wallet.core.blockchain.tron.models.TronChainParameters
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Url

interface TronNodeStatusService {
    @GET("/wallet/getchainparameters")
    suspend fun getChainParameters(): Result<TronChainParameters>

    @POST("/wallet/getnowblock")
    suspend fun nowBlock(): Result<TronBlock>

    @POST//("/wallet/getnowblock")
    suspend fun nowBlock(@Url url: String): Response<TronBlock>
}