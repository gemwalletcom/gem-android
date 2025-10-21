package com.gemwallet.features.swap.viewmodels.cases

import com.gemwallet.android.cases.swap.GetSwapQuotes
import com.gemwallet.android.model.AssetInfo
import com.gemwallet.android.model.Crypto
import com.gemwallet.features.swap.viewmodels.models.QuoteRequestParams
import com.gemwallet.features.swap.viewmodels.models.QuotesState
import com.gemwallet.features.swap.viewmodels.models.create
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onEach
import java.math.BigDecimal
import java.math.BigInteger
import javax.inject.Inject

class QuoteRequester @Inject constructor(
    private val getSwapQuotes: GetSwapQuotes
) {

    @OptIn(ExperimentalCoroutinesApi::class)
    internal fun requestQuotes(
        payValue: Flow<BigDecimal>,
        payAsset: Flow<AssetInfo?>,
        receiveAsset: Flow<AssetInfo?>,
        refreshState: Flow<Long>,
        onStart: (QuoteRequestParams?) -> Unit,
        onError: (Throwable) -> Unit
    ): Flow<QuotesState> {
        return combine(
            payValue,
            payAsset,
            receiveAsset,
            refreshState,
        ) { value, fromAsset, toAsset, _ ->
            QuoteRequestParams.create(value, fromAsset, toAsset)
        }
        .onEach { onStart(it) }
        .filterNotNull()
        .mapLatest { it }
        .mapLatest {
            delay(500)
            requestQuotes(it)
        }
        .onEach { data ->
            data.err?.let { onError(it) }
        }
        .flowOn(Dispatchers.IO)
    }

    private suspend fun requestQuotes(params: QuoteRequestParams): QuotesState = try {
        val amount = Crypto(params.value, params.pay.asset.decimals).atomicValue
        val quotes = getSwapQuotes.getQuotes(
            from = params.pay.asset,
            to = params.receive.asset,
            ownerAddress = params.pay.owner!!.address,
            destination = params.receive.owner!!.address,
            amount = amount.toString(),
            useMaxAmount = BigInteger(params.pay.balance.balance.available) == amount,
        ) ?: emptyList()
        QuotesState(quotes, params.pay, params.receive)
    } catch (err: Throwable) {
        QuotesState(pay = params.pay, receive = params.receive, err = err)
    }
}