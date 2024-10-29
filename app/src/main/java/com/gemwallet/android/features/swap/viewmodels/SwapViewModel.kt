package com.gemwallet.android.features.swap.viewmodels

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.clearText
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gemwallet.android.cases.transactions.GetTransactionCase
import com.gemwallet.android.data.asset.AssetsRepository
import com.gemwallet.android.data.repositories.session.SessionRepository
import com.gemwallet.android.data.repositories.swap.SwapRepository
import com.gemwallet.android.ext.getAccount
import com.gemwallet.android.ext.toAssetId
import com.gemwallet.android.ext.toIdentifier
import com.gemwallet.android.ext.type
import com.gemwallet.android.features.swap.models.SwapError
import com.gemwallet.android.features.swap.models.SwapItemModel
import com.gemwallet.android.features.swap.models.SwapItemType
import com.gemwallet.android.features.swap.models.SwapPairSelect
import com.gemwallet.android.features.swap.models.SwapPairUIModel
import com.gemwallet.android.features.swap.models.SwapState
import com.gemwallet.android.features.swap.navigation.pairArg
import com.gemwallet.android.math.numberParse
import com.gemwallet.android.math.toHexString
import com.gemwallet.android.model.AssetInfo
import com.gemwallet.android.model.ConfirmParams
import com.gemwallet.android.model.Crypto
import com.gemwallet.android.model.Fiat
import com.gemwallet.android.model.format
import com.wallet.core.primitives.AssetId
import com.wallet.core.primitives.AssetSubtype
import com.wallet.core.primitives.SwapQuote
import com.wallet.core.primitives.TransactionState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.math.BigDecimal
import java.math.BigInteger
import javax.inject.Inject
import kotlin.math.max

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class SwapViewModel @Inject constructor(
    private val sessionRepository: SessionRepository,
    private val assetsRepository: AssetsRepository,
    private val swapRepository: SwapRepository,
    private val savedStateHandle: SavedStateHandle,
    private val getTransactionCase: GetTransactionCase,
) : ViewModel() {

    val swapScreenState = MutableStateFlow<SwapState>(SwapState.None)

    val selectPair = MutableStateFlow<SwapPairSelect?>(null)

    private val approveTxHash = MutableStateFlow<String?>(null)
    val approveTx = approveTxHash.flatMapLatest { getTransactionCase.getTransaction(it ?: return@flatMapLatest emptyFlow()) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    private val swapPairState: StateFlow<SwapPairState?> = savedStateHandle.getStateFlow<String?>(pairArg, null)
        .mapNotNull {
            val values = it?.split("|")
            listOf(
                values?.firstOrNull()?.toAssetId(),
                values?.lastOrNull()?.toAssetId()
            )
        }
        .map {
            val fromId = it.firstOrNull()
            val toId = it.lastOrNull()
            if (fromId == null || toId == null) {
                selectPair.update { SwapPairSelect.request(fromId, toId) }
            }
            SwapPairState(fromId, toId)
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    private val assetsState = swapPairState.flatMapLatest { ids ->
        if (ids?.fromId == null || ids.toId == null || ids.fromId.chain != ids.toId.chain || ids.fromId.toIdentifier() == ids.toId.toIdentifier()) {
            return@flatMapLatest emptyFlow()
        }
        assetsRepository.getAssetsInfo(listOf(ids.fromId, ids.toId))
            .map { assets ->
                SwapAssetsState(
                    from = assets.firstOrNull { ids.fromId.toIdentifier() == it.id().toIdentifier() },
                    to = assets.firstOrNull { ids.toId.toIdentifier() == it.id().toIdentifier() },
                )
            }
    }
    .flowOn(Dispatchers.IO)
    .stateIn(viewModelScope, SharingStarted.Lazily, null)

    val swapPairUIModel = assetsState.mapNotNull { assets ->
        if (assets?.from == null || assets.to == null) {
            return@mapNotNull null
        }
        val symbolLength = max(assets.from.asset.symbol.length, assets.to.asset.symbol.length)
        SwapPairUIModel(
            from = SwapItemModel(
                asset = assets.from.asset.copy(symbol = assets.from.asset.symbol.padStart(symbolLength)),
                assetBalanceValue = assets.from.balances.available().value(assets.from.asset.decimals).stripTrailingZeros().toPlainString(),
                assetBalanceLabel = assets.from.asset.format(assets.from.balances.available(), 4),
            ),
            to = SwapItemModel(
                asset = assets.to.asset.copy(symbol = assets.to.asset.symbol.padStart(symbolLength)),
                assetBalanceValue = assets.to.balances.available().value(assets.to.asset.decimals).stripTrailingZeros().toPlainString(),
                assetBalanceLabel = assets.to.asset.format(assets.to.balances.available(), 4),
            ),
        )
    }
    .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val fromValue: TextFieldState = TextFieldState()
    private val fromValueFlow = snapshotFlow { fromValue.text }.map { it.toString() }
    val fromEquivalent = fromValueFlow.combine(assetsState) { value, assets ->
        swapScreenState.update { SwapState.GetQuote }
        val price = assets?.from?.price ?: return@combine null
        val valueNum = try {
            value.numberParse()
        } catch (err: Throwable) {
            BigDecimal.ZERO
        }
        val fiat = Fiat(valueNum.toDouble() * price.price.price)
        price.currency.format(fiat)
    }
    .filterNotNull()
    .stateIn(viewModelScope, SharingStarted.Eagerly, "")

    private val quote = fromValueFlow.mapLatest { input ->
        val value = try {
            input.numberParse()
        } catch (err: Throwable) {
            BigDecimal.ZERO
        }
        Pair(value, assetsState.value)
    }
    .mapLatest {
        val fromAsset = it.second?.from
        val toAsset = it.second?.to
        if (fromAsset == null || toAsset == null || it.first == BigDecimal.ZERO) {
            withContext(Dispatchers.Main) { toValue.edit { replace(0, length, "0") } }
            return@mapLatest null
        }
        swapScreenState.update { SwapState.GetQuote }
        delay(500L)
        val quote = getQuote(fromAsset, toAsset, it.first)
        val amount = toAsset.asset.format(Crypto(quote?.toAmount ?: "0"), 8, showSymbol = false)
        withContext(Dispatchers.Main) { toValue.edit { replace(0, length, amount) } }
        quote
    }.flowOn(Dispatchers.IO)
    .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val toValue: TextFieldState = TextFieldState()
    val toEquivalent = quote.combine(assetsState) { quote, assets ->
        if (quote == null || assets?.to == null) {
            return@combine ""
        }
        val price = assets.to.price
        if (price?.currency != null && price.price.price > 0) {
            assets.to.price!!.currency.format(Crypto(quote.toAmount).convert(assets.to.asset.decimals, price.price.price))
        } else {
            ""
        }
    }
    .filterNotNull()
    .stateIn(viewModelScope, SharingStarted.Eagerly, "")

    private val requestApprove = combine(quote, assetsState, approveTx) { quote, assets, tx ->
        if (assets?.from == null) {
            swapScreenState.update { SwapState.None }
            return@combine false
        }
        if (!swapRepository.isRequestApprove(assets.from.id().chain) || assets.from.asset.id.type() != AssetSubtype.TOKEN) {
            swapScreenState.update { SwapState.Ready }
            return@combine false
        }

        if (tx?.transaction?.state == TransactionState.Pending) {
            swapScreenState.update { SwapState.Approving }
            return@combine false
        }

        val spender = quote?.approval?.spender
        if (spender.isNullOrEmpty()) {
            swapScreenState.update { SwapState.None }
            return@combine false
        }

        swapScreenState.update { SwapState.CheckAllowance }
        val allowance = swapRepository.getAllowance(assets.from.id(), assets.from.owner.address, spender)
        val requestApprove = allowance < BigInteger(quote.fromAmount)
        swapScreenState.update { if (requestApprove) SwapState.RequestApprove else SwapState.Ready }
        requestApprove
    }
    .flowOn(Dispatchers.IO)
    .stateIn(viewModelScope, SharingStarted.Eagerly, true)

    private val sync = swapPairState.flatMapLatest {
        flow {
            updateBalances(it?.fromId ?: return@flow, it.toId ?: return@flow)
            emit(true)
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, false)

    fun onSelect(select: SwapPairSelect) {
        val current = if (select.fromId == null || select.toId == null) {
            when (select) {
                is SwapPairSelect.From -> SwapPairSelect.From(
                    select.fromId,
                    swapPairState.value?.toId ?: select.toId
                )

                is SwapPairSelect.To -> SwapPairSelect.To(
                    swapPairState.value?.fromId ?: select.fromId, select.toId
                )
            }
        } else {
            select
        }
        val update = if (current.sameChain()) {
            savedStateHandle[pairArg] = "${current.fromId?.toIdentifier()}|${current.toId?.toIdentifier()}"
            null
        } else {
            current.opposite()
        }
        selectPair.update { update }
    }

    private suspend fun updateBalances(fromId: AssetId, toId: AssetId) {
        val session = sessionRepository.getSession() ?: return
        val account = session.wallet.getAccount(fromId.chain) ?: return
        viewModelScope.launch(Dispatchers.IO) {
            assetsRepository.switchVisibility(session.wallet.id, account, fromId, true, session.currency)
        }
        viewModelScope.launch(Dispatchers.IO) {
            assetsRepository.switchVisibility(session.wallet.id, account, toId, true, session.currency)
        }
    }

    private suspend fun getQuote(from: AssetInfo, to: AssetInfo, amount: BigDecimal, includeData: Boolean = false): SwapQuote? {
        val quote = swapRepository.getQuote(
            from = from.asset.id,
            to = to.asset.id,
            ownerAddress = from.owner.address,
            amount = Crypto(amount, from.asset.decimals).atomicValue.toString(),
            includeData = includeData,
        )
        return quote
    }

    fun switchSwap() {
        onSelect(SwapPairSelect.From(swapPairState.value?.toId, swapPairState.value?.fromId))
        fromValue.clearText()
    }

    fun swap(onConfirm: (ConfirmParams) -> Unit) = viewModelScope.launch {
        if (swapScreenState.value == SwapState.Swapping) return@launch

        swapScreenState.update { SwapState.Swapping }
        val fromAmount = try {
            fromValue.text.toString().numberParse()
        } catch (err: Throwable) {
            swapScreenState.update { SwapState.Error(SwapError.IncorrectInput) }
            return@launch
        }
        val from = assetsState.value?.from ?: return@launch
        val to = assetsState.value?.to ?: return@launch
        val requestApprove = requestApprove.value
        val quote = getQuote(from, to, fromAmount, !requestApprove)
        if (quote == null) {
            swapScreenState.update { SwapState.Error(SwapError.NoQuote) }
            return@launch
        }
        if (requestApprove) {
            swapScreenState.update { SwapState.RequestApprove }
            val meta = swapRepository.encodeApprove(quote.approval?.spender ?: "")
            onConfirm(
                ConfirmParams.TokenApprovalParams(
                    assetId = if (from.asset.id.type() == AssetSubtype.TOKEN) from.asset.id else to.asset.id,
                    approvalData = meta.toHexString(),
                    provider = quote.provider.name,
                )
            )
        } else {
            swapScreenState.update { SwapState.Ready }
            onConfirm(
                ConfirmParams.SwapParams(
                    fromAssetId = from.asset.id,
                    toAssetId = to.asset.id,
                    fromAmount = Crypto(fromAmount, from.asset.decimals).atomicValue,
                    toAmount = BigInteger(quote.toAmount),
                    swapData = quote.data?.data ?: return@launch,
                    provider = quote.provider.name,
                    to = quote.data?.to ?: return@launch,
                    value = quote.data?.value ?: return@launch,
                )
            )
        }
    }

    fun changePair(swapItemType: SwapItemType) {
        val fromId = if (swapItemType == SwapItemType.Pay) swapPairState.value?.fromId else null
        val toId = if (swapItemType == SwapItemType.Pay) null else swapPairState.value?.fromId
        selectPair.update {
            val select = SwapPairSelect.From(fromId, toId)
            if (swapItemType == SwapItemType.Pay) select else select.opposite()
        }
    }

    fun onTxHash(hash: String) {
        approveTxHash.update { "${assetsState.value?.from?.id()?.chain?.string}_$hash" }
    }

    private class SwapPairState(
        val fromId: AssetId? = null,
        val toId: AssetId? = null,
    )

    private class SwapAssetsState(
        val from: AssetInfo? = null,
        val to: AssetInfo? = null,
    )
}