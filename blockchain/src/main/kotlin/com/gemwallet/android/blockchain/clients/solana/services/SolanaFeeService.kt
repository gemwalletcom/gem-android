package com.gemwallet.android.blockchain.clients.solana.services

import com.gemwallet.android.blockchain.clients.solana.SolanaMethod
import com.gemwallet.android.blockchain.rpc.model.JSONRpcRequest
import com.gemwallet.android.blockchain.rpc.model.JSONRpcResponse
import com.wallet.core.blockchain.solana.SolanaPrioritizationFee
import retrofit2.http.Body
import retrofit2.http.POST

interface SolanaFeeService {
    @POST("/")
    suspend fun getPriorityFees(@Body request: JSONRpcRequest<List<String>>): Result<JSONRpcResponse<List<SolanaPrioritizationFee>>>
}

suspend fun SolanaFeeService.getPriorityFees(): List<SolanaPrioritizationFee> {
    val request = JSONRpcRequest.create(SolanaMethod.GetPriorityFee, listOf<String>())
    return getPriorityFees(request).getOrNull()?.result ?: throw Exception("Can't load fee price")
}