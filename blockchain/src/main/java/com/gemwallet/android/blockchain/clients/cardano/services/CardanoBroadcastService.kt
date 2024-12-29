package com.gemwallet.android.blockchain.clients.cardano.services

import com.wallet.core.blockchain.cardano.CardanoTransactionBroadcast
import com.wallet.core.blockchain.graphql.GraphqlData
import com.wallet.core.blockchain.graphql.GraphqlRequest
import retrofit2.http.Body
import retrofit2.http.POST

interface CardanoBroadcastService {
    @POST("/")
    suspend fun broadcast(@Body request: GraphqlRequest): Result<GraphqlData<CardanoTransactionBroadcast>>
}

suspend fun CardanoBroadcastService.broadcast(data: String): CardanoTransactionBroadcast? {
    val request = GraphqlRequest(
        operationName = "SubmitTransaction",
        variables = hashMapOf("transaction" to data),
        query = "mutation SubmitTransaction(\$transaction: String!) { submitTransaction(transaction: \$transaction) { hash } }"
    )
    val result = broadcast(request)
    return result.getOrNull()?.data
}