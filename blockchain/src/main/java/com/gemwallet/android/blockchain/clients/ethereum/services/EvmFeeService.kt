package com.gemwallet.android.blockchain.clients.ethereum.services

import com.gemwallet.android.blockchain.clients.ethereum.EvmMethod
import com.gemwallet.android.blockchain.clients.ethereum.services.EvmRpcClient.EvmNumber
import com.gemwallet.android.blockchain.rpc.model.JSONRpcRequest
import com.gemwallet.android.blockchain.rpc.model.JSONRpcResponse
import com.gemwallet.android.math.append0x
import com.wallet.core.blockchain.ethereum.models.EthereumFeeHistory
import retrofit2.http.Body
import retrofit2.http.POST
import java.math.BigInteger

interface EvmFeeService {
    @POST("/")
    suspend fun getFeeHistory(@Body request: JSONRpcRequest<List<Any>>): Result<JSONRpcResponse<EthereumFeeHistory>>

    @POST("/")
    suspend fun getGasLimit(@Body request: JSONRpcRequest<List<Any>>): Result<JSONRpcResponse<EvmNumber?>>

    @POST("/")
    suspend fun getNonce(@Body request: JSONRpcRequest<List<String>>): Result<JSONRpcResponse<EvmNumber?>>
}

internal suspend fun EvmFeeService.getFeeHistory(rewardPercentiles: List<Int>): EthereumFeeHistory? {
    return getFeeHistory(JSONRpcRequest.create(EvmMethod.GetFeeHistory, listOf("10", "latest", rewardPercentiles)))
        .getOrNull()?.result
}

internal suspend fun EvmFeeService.getGasLimit(from: String, to: String, amount: BigInteger, data: String?): BigInteger {
    val transaction = mapOf(
        "from" to from,
        "to" to to,
        "value" to "0x${amount.toString(16)}",
        "data" to if (data.isNullOrEmpty()) "0x" else data.append0x(),
    )
    val request = JSONRpcRequest.create(EvmMethod.GetGasLimit, listOf<Any>(transaction))
    return getGasLimit(request).getOrNull()?.result?.value
        ?: throw Exception("Fail calculate gas limit")
}

internal suspend fun EvmFeeService.getNonce(fromAddress: String): BigInteger {
    val nonceParams = listOf(fromAddress, "latest")
    return getNonce(JSONRpcRequest.create(EvmMethod.GetNonce, nonceParams))
        .getOrNull()?.result?.value ?: throw Exception("Fail get current nonce")
}