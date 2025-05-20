package com.gemwallet.android.blockchain.clients.tron.services

import android.util.Log
import com.gemwallet.android.math.toHexString
import com.wallet.core.blockchain.tron.models.TronAccount
import com.wallet.core.blockchain.tron.models.TronAccountRequest
import com.wallet.core.blockchain.tron.models.TronAccountUsage
import retrofit2.http.Body
import retrofit2.http.POST
import wallet.core.jni.Base58

interface TronAccountsService {
    @POST("/wallet/getaccount")
    suspend fun getAccount(@Body addressRequest: TronAccountRequest): Result<TronAccount>

    @POST("/wallet/getaccountnet")
    suspend fun getAccountUsage(@Body addressRequest: TronAccountRequest): Result<TronAccountUsage>
}

suspend fun TronAccountsService.getAccount(address: String, visible: Boolean = false): TronAccount? {
    return try {
        val result = getAccount(
            TronAccountRequest(
                address = address,
                visible = visible
            )
        )
        result.getOrThrow()
    } catch (err: Throwable) {
        Log.d("TRON-ACCOUNT", "Error: ", err)
        null
    }
}

suspend fun TronAccountsService.getAccountUsage(address: String): TronAccountUsage? {
    return getAccountUsage(
        TronAccountRequest(
            address = Base58.decode(address).toHexString(""),
            visible = false
        )
    ).getOrNull()
}