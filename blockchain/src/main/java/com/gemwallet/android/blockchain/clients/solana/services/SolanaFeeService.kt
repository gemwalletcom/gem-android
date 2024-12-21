package com.gemwallet.android.blockchain.clients.solana.services

import com.gemwallet.android.blockchain.clients.solana.SolanaMethod
import com.gemwallet.android.blockchain.rpc.model.JSONRpcRequest
import com.gemwallet.android.blockchain.rpc.model.JSONRpcResponse
import com.wallet.core.blockchain.solana.models.SolanaPrioritizationFee
import retrofit2.http.Body
import retrofit2.http.POST
import java.math.BigInteger

interface SolanaFeeService {
    @POST("/")
    suspend fun rentExemption(@Body request: JSONRpcRequest<List<Int>>): Result<JSONRpcResponse<Int>>

    @POST("/")
    suspend fun getPriorityFees(@Body request: JSONRpcRequest<List<String>>): Result<JSONRpcResponse<List<SolanaPrioritizationFee>>>
}

suspend fun SolanaFeeService.getPriorityFees(): List<SolanaPrioritizationFee> {
    val request = JSONRpcRequest.create(SolanaMethod.GetPriorityFee, listOf<String>())
    return getPriorityFees(request).getOrNull()?.result ?: throw Exception("Can't load fee price")
}

suspend fun SolanaFeeService.rentExemption(tokenAccountSize: Int): BigInteger {
    return rentExemption(
        JSONRpcRequest(
            id = 1,
            method = SolanaMethod.RentExemption.value,
            params = listOf(tokenAccountSize)
        )
    ).getOrNull()?.result?.toBigInteger() ?: throw Exception("Can't load token creation fee")
}