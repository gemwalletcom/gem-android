package com.gemwallet.android.blockchain.clients.stellar.services

import com.wallet.core.blockchain.stellar.StellarNodeStatus
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Url

interface StellarNodeStatusService {
    @GET//("/")
    suspend fun node(@Url url: String): Response<StellarNodeStatus>
}