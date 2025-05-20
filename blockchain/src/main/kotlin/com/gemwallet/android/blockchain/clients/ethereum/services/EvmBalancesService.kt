package com.gemwallet.android.blockchain.clients.ethereum.services

import com.gemwallet.android.blockchain.clients.ethereum.EvmMethod
import com.gemwallet.android.blockchain.clients.ethereum.services.EvmRpcClient.EvmNumber
import com.gemwallet.android.blockchain.rpc.model.JSONRpcRequest
import com.gemwallet.android.blockchain.rpc.model.JSONRpcResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface EvmBalancesService {
    @POST("/")
    suspend fun getBalance(@Body request: JSONRpcRequest<List<String>>): Result<JSONRpcResponse<EvmNumber?>>
}

suspend fun EvmBalancesService.getBalance(address: String): Result<JSONRpcResponse<EvmNumber?>> {
    return getBalance(
        JSONRpcRequest.create(
            method = EvmMethod.GetBalance,
            params = listOf(address, "latest")
        )
    )
}