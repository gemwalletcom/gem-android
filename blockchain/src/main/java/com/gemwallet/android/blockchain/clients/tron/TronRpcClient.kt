package com.gemwallet.android.blockchain.clients.tron

import com.gemwallet.android.blockchain.clients.tron.services.TronAccountsService
import com.gemwallet.android.blockchain.clients.tron.services.TronCallService
import com.gemwallet.android.blockchain.clients.tron.services.TronStakeService
import com.gemwallet.android.math.toHexString
import com.wallet.core.blockchain.tron.models.TronAccountRequest
import com.wallet.core.blockchain.tron.models.TronAccountUsage
import com.wallet.core.blockchain.tron.models.TronBlock
import com.wallet.core.blockchain.tron.models.TronChainParameters
import com.wallet.core.blockchain.tron.models.TronTransactionBroadcast
import com.wallet.core.blockchain.tron.models.TronTransactionReceipt
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Url
import wallet.core.jni.Base58

interface TronRpcClient :
    TronAccountsService,
    TronCallService,
    TronStakeService
{
    @POST("/wallet/getnowblock")
    suspend fun nowBlock(): Result<TronBlock>

    @GET("/wallet/getchainparameters")
    suspend fun getChainParameters(): Result<TronChainParameters>

    @POST("/wallet/getaccountnet")
    suspend fun getAccountUsage(@Body addressRequest: TronAccountRequest): Result<TronAccountUsage>

    @POST("/wallet/broadcasttransaction")
    suspend fun broadcast(@Body body: RequestBody): Result<TronTransactionBroadcast>

    @POST("/wallet/gettransactioninfobyid")
    suspend fun transaction(@Body value: TronValue): Result<TronTransactionReceipt>

    @POST//("/wallet/getnowblock")
    suspend fun nowBlock(@Url url: String): Response<TronBlock>

    class TronValue(val value: String)
}

suspend fun TronRpcClient.getAccountUsage(address: String): TronAccountUsage? {
    return getAccountUsage(
        TronAccountRequest(
            address = Base58.decode(address).toHexString(""),
            visible = false
        )
    ).getOrNull()
}