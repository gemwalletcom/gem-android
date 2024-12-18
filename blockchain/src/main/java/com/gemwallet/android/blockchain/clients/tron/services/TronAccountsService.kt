package com.gemwallet.android.blockchain.clients.tron.services

import com.gemwallet.android.blockchain.clients.tron.TronRpcClient
import com.gemwallet.android.math.toHexString
import com.wallet.core.blockchain.tron.models.TronAccount
import com.wallet.core.blockchain.tron.models.TronAccountRequest
import retrofit2.http.Body
import retrofit2.http.POST
import wallet.core.jni.Base58

interface TronAccountsService {
    @POST("/wallet/getaccount")
    suspend fun getAccount(@Body addressRequest: TronAccountRequest): Result<TronAccount>
}

suspend fun TronAccountsService.getAccount(address: String, visible: Boolean = false): TronAccount? {
    return try {
        getAccount(
            TronAccountRequest(
                address = Base58.decode(address).toHexString(""),
                visible = visible
            )
        ).getOrThrow()
    } catch (_: Throwable) {
        null
    }
}