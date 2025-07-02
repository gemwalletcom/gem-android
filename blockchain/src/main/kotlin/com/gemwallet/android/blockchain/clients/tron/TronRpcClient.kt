package com.gemwallet.android.blockchain.clients.tron

import com.gemwallet.android.blockchain.clients.tron.services.TronAccountsService
import com.gemwallet.android.blockchain.clients.tron.services.TronBroadcastService
import com.gemwallet.android.blockchain.clients.tron.services.TronCallService
import com.gemwallet.android.blockchain.clients.tron.services.TronNodeStatusService
import com.gemwallet.android.blockchain.clients.tron.services.TronStakeService
import com.wallet.core.blockchain.tron.TronTransactionReceipt
import retrofit2.http.Body
import retrofit2.http.POST

interface TronRpcClient :
    TronAccountsService,
    TronCallService,
    TronStakeService,
    TronNodeStatusService,
    TronBroadcastService
{

    @POST("/wallet/gettransactioninfobyid")
    suspend fun transaction(@Body value: TronValue): Result<TronTransactionReceipt>

    class TronValue(val value: String)
}