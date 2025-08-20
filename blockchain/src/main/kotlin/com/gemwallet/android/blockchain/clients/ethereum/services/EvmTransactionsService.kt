package com.gemwallet.android.blockchain.clients.ethereum.services

import com.gemwallet.android.blockchain.rpc.model.JSONRpcRequest
import com.gemwallet.android.blockchain.rpc.model.JSONRpcResponse
import com.wallet.core.blockchain.ethereum.EthereumTransactionReciept
import retrofit2.http.Body
import retrofit2.http.POST

interface EvmTransactionsService {
    @POST("/")
    suspend fun transaction(@Body request: JSONRpcRequest<List<String>>): JSONRpcResponse<EthereumTransactionReciept>
}