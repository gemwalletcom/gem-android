package com.gemwallet.android.blockchain.clients.solana.services

import com.gemwallet.android.blockchain.clients.solana.SolanaMethod
import com.gemwallet.android.blockchain.rpc.model.JSONRpcRequest
import com.gemwallet.android.blockchain.rpc.model.JSONRpcResponse
import com.wallet.core.blockchain.solana.SolanaBalance
import com.wallet.core.blockchain.solana.SolanaBalanceValue
import com.wallet.core.blockchain.solana.SolanaValue
import retrofit2.http.Body
import retrofit2.http.POST
import java.math.BigInteger

interface SolanaBalancesService {
    @POST("/")
    suspend fun getBalance(@Body request: JSONRpcRequest<List<Any>>): Result<JSONRpcResponse<SolanaBalance>>

    @POST("/")
    suspend fun getTokenBalance(@Body request: JSONRpcRequest<List<String>>): Result<JSONRpcResponse<SolanaValue<SolanaBalanceValue>>>
}

suspend fun SolanaBalancesService.getBalance(address: String): Long? {
    return getBalance(
        JSONRpcRequest.create(
            SolanaMethod.GetBalance,
            listOf(
                address,
                mapOf(SolanaRpcClient.commitmentValue to SolanaRpcClient.commitmentValue)
            )
        )
    )
    .getOrNull()?.result?.value
}

suspend fun SolanaBalancesService.getTokenBalance(tokenAccount: String): BigInteger? {
    val balanceRequest = JSONRpcRequest.create(SolanaMethod.GetTokenBalance, listOf(tokenAccount))
    return try {
        getTokenBalance(balanceRequest).getOrNull()?.result?.value?.amount?.toBigInteger()
    } catch (_: Throwable) {
        null
    }
}