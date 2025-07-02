package com.gemwallet.android.blockchain.clients.cardano.services

import com.wallet.core.blockchain.cardano.CardanoAggregateBalance
import com.wallet.core.blockchain.cardano.CardanoUTXOS
import com.wallet.core.primitives.GraphqlData
import com.wallet.core.primitives.GraphqlRequest
import retrofit2.http.Body
import retrofit2.http.POST
import java.math.BigInteger

interface CardanoBalanceService {

    @POST("/")
    suspend fun balance(@Body request: GraphqlRequest): Result<GraphqlData<CardanoUTXOS<CardanoAggregateBalance>>>

}

suspend fun CardanoBalanceService.balance(address: String): BigInteger? {
    val request = GraphqlRequest(
        operationName = "GetBalance",
        variables = hashMapOf(
            "address" to address
        ),
        query = "query GetBalance(\$address: String!) { utxos: utxos_aggregate(where: { address: { _eq: \$address }  } ) { aggregate { sum { value } } } }"
    )
    return balance(request).getOrNull()?.data?.utxos?.aggregate?.sum?.value?.toBigInteger()
}