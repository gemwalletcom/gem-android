package com.gemwallet.android.blockchain.clients.cardano.services

import com.wallet.core.blockchain.cardano.CardanoBlockData
import com.wallet.core.blockchain.graphql.GraphqlData
import com.wallet.core.blockchain.graphql.GraphqlRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Url

interface CardanoNodeStatusService {
    @POST
    suspend fun latestBlock(@Url url: String, @Body request: GraphqlRequest): Response<GraphqlData<CardanoBlockData>>
}

suspend fun CardanoNodeStatusService.latestBlock(url: String): Response<GraphqlData<CardanoBlockData>> {
    val request = GraphqlRequest(
            operationName = "GetBlockNumber",
            variables = hashMapOf(),
            query = "query GetBlockNumber { cardano { tip { number } } }"
        )
    return latestBlock(url, request)
}