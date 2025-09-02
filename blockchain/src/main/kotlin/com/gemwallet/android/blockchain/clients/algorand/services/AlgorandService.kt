package com.gemwallet.android.blockchain.clients.algorand.services

import com.wallet.core.blockchain.algorand.AlgorandTransactionParams
import retrofit2.http.GET

interface AlgorandService {
        @GET("/v2/transactions/params")
        suspend fun transactionsParams(): Result<AlgorandTransactionParams>
}