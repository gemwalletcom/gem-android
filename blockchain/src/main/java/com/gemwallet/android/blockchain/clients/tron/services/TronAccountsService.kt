package com.gemwallet.android.blockchain.clients.tron.services

import com.wallet.core.blockchain.tron.models.TronAccount
import com.wallet.core.blockchain.tron.models.TronAccountRequest
import com.wallet.core.blockchain.tron.models.TronAccountUsage
import retrofit2.http.Body
import retrofit2.http.POST

interface TronAccountsService {
    @POST("/wallet/getaccount")
    suspend fun getAccount(@Body addressRequest: TronAccountRequest): Result<TronAccount>

    @POST("/wallet/getaccountnet")
    suspend fun getAccountUsage(@Body addressRequest: TronAccountRequest): Result<TronAccountUsage>
}

suspend fun TronAccountsService.getAccount(address: String, visible: Boolean = false): TronAccount? {
    return try {
        getAccount(
            TronAccountRequest(
                address = address,
                visible = visible
            )
        ).getOrThrow()
    } catch (_: Throwable) {
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