package com.gemwallet.android.blockchain.clients.tron

import com.gemwallet.android.blockchain.clients.tron.services.TronAccountsService
import com.gemwallet.android.blockchain.clients.tron.services.TronCallService
import com.gemwallet.android.blockchain.clients.tron.services.TronNodeStatusService
import com.gemwallet.android.blockchain.clients.tron.services.TronStakeService
import com.wallet.core.blockchain.tron.models.TronTransactionBroadcast
import com.wallet.core.blockchain.tron.models.TronTransactionReceipt
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.POST

interface TronRpcClient :
    TronAccountsService,
    TronCallService,
    TronStakeService,
    TronNodeStatusService
{
    @POST("/wallet/broadcasttransaction")
    suspend fun broadcast(@Body body: RequestBody): Result<TronTransactionBroadcast>

    @POST("/wallet/gettransactioninfobyid")
    suspend fun transaction(@Body value: TronValue): Result<TronTransactionReceipt>

    class TronValue(val value: String)
}