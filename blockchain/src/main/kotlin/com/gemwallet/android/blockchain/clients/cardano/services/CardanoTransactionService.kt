package com.gemwallet.android.blockchain.clients.cardano.services

import com.wallet.core.blockchain.cardano.generated.CardanoTransaction
import com.wallet.core.blockchain.cardano.generated.CardanoTransactions
import com.wallet.core.blockchain.graphql.generated.GraphqlData
import com.wallet.core.blockchain.graphql.generated.GraphqlRequest
import retrofit2.http.Body
import retrofit2.http.POST

interface CardanoTransactionService {

    @POST("/")
    suspend fun transaction(@Body request: GraphqlRequest): Result<GraphqlData<CardanoTransactions>>
}

suspend fun CardanoTransactionService.transaction(hash: String): CardanoTransaction? {
    val request = GraphqlRequest(
        operationName = "TransactionsByHash",
        variables = hashMapOf(
            "hash" to hash,
        ),
        query = "query TransactionsByHash(\$hash: Hash32Hex!) { transactions(where: { hash: { _eq: \$hash }  } ) { block { number } fee } }"
    )
    return transaction(request).getOrNull()?.data?.transactions?.firstOrNull()
}