package com.gemwallet.android.blockchain.clients.aptos.services

import com.wallet.core.blockchain.aptos.models.AptosLedger
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Url

interface AptosNodeStatusService {
    @GET
    suspend fun ledger(@Url url: String): Response<AptosLedger>

    suspend fun AptosServices.getLedger(url: String): Response<AptosLedger> {
        return ledger("$url/v1")
    }
}

suspend fun AptosNodeStatusService.getLedger(url: String): Response<AptosLedger> {
    return ledger("$url/v1")
}