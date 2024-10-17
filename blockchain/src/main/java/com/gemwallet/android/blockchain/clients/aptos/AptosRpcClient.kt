package com.gemwallet.android.blockchain.clients.aptos

import com.gemwallet.android.blockchain.clients.aptos.model.AptosAccount
import com.wallet.core.blockchain.aptos.models.AptosGasFee
import com.wallet.core.blockchain.aptos.models.AptosResource
import com.wallet.core.blockchain.aptos.models.AptosResourceBalance
import com.wallet.core.blockchain.aptos.models.AptosTransaction
import com.wallet.core.blockchain.aptos.models.AptosTransactionBroacast
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Path

interface AptosRpcClient {

    @GET("/v1/accounts/{address}")
    suspend fun accounts(@Path("address") address: String): Result<AptosAccount>

    @GET("/v1/accounts/{address}/resource/0x1::coin::CoinStore<0x1::aptos_coin::AptosCoin>")
    suspend fun balance(@Path("address") address: String): Result<AptosResource<AptosResourceBalance>>

    @GET("/v1/transactions/by_hash/{id}")
    suspend fun transactions(@Path("id") txId: String): Result<AptosTransaction>

    @GET("/v1/estimate_gas_price")
    suspend fun feePrice(): Result<AptosGasFee>

    @Headers("Content-type: application/json")
    @POST("/v1/transactions")
    suspend fun broadcast(@Body request: RequestBody): Result<AptosTransactionBroacast>
}