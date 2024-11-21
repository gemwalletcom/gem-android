package com.gemwallet.android.features.swap.viewmodels

import android.util.Log
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.clearText
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gemwallet.android.blockchain.clients.ethereum.encodeApprove
import com.gemwallet.android.cases.transactions.GetTransactionCase
import com.gemwallet.android.data.repositoreis.assets.AssetsRepository
import com.gemwallet.android.data.repositoreis.session.SessionRepository
import com.gemwallet.android.data.repositoreis.swap.SwapRepository
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
import com.gemwallet.android.math.hexToBigInteger
import com.gemwallet.android.math.numberParse
import com.gemwallet.android.model.AssetInfo
import com.gemwallet.android.model.ConfirmParams
import com.gemwallet.android.model.Crypto
import com.gemwallet.android.model.availableFormatted
import com.gemwallet.android.model.format
import com.wallet.core.primitives.AssetId
import com.wallet.core.primitives.AssetSubtype
import com.wallet.core.primitives.TransactionState
import com.walletconnect.util.bytesToHex
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
import uniffi.gemstone.ApprovalType
import uniffi.gemstone.SwapperException
import uniffi.gemstone.swapProviderNameToString
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
        if (ids?.fromId == null || ids.toId == null || ids.fromId.toIdentifier() == ids.toId.toIdentifier()) {
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
                assetBalanceValue = assets.from.balance.balanceAmount.available.toBigDecimal().toPlainString(),
                assetBalanceLabel = assets.from.balance.availableFormatted(4),
            ),
            to = SwapItemModel(
                asset = assets.to.asset.copy(symbol = assets.to.asset.symbol.padStart(symbolLength)),
                assetBalanceValue = assets.to.balance.balanceAmount.available.toBigDecimal().toPlainString(),
                assetBalanceLabel = assets.to.balance.availableFormatted(4),
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
        } catch (_: Throwable) {
            BigDecimal.ZERO
        }
        val fiat = valueNum.toDouble() * price.price.price
        price.currency.format(fiat)
    }
    .filterNotNull()
    .stateIn(viewModelScope, SharingStarted.Eagerly, "")

    private val quote  = combine(fromValueFlow, assetsState, approveTx) { fromValue, assets, tx ->
        Triple(fromValue, assets, tx)
    }.mapLatest { data ->
        val assets = data.second
        val fromAsset = assets?.from
        val toAsset = assets?.to
        val fromValue = try {
            data.first.numberParse()
        } catch (_: Throwable) {
            BigDecimal.ZERO
        }
        if (fromAsset == null || toAsset == null || fromValue.compareTo(BigDecimal.ZERO) == 0) {
            withContext(Dispatchers.Main) { toValue.edit { replace(0, length, "0") } }
            swapScreenState.update { SwapState.None }
            return@mapLatest null
        }
        if (data.third?.transaction?.state == TransactionState.Pending) {
            swapScreenState.update { SwapState.Approving }
            delay(1000L) // Wait transactions and don't spam servers, it'll cancel on next values. Return null isn't correct
        }
        swapScreenState.update { SwapState.GetQuote }
        delay(500L) // User input type - doesn't want spam nodes
        val quote = try {
            swapRepository.getQuote(
                from = fromAsset.asset.id,
                to = toAsset.asset.id,
                ownerAddress = fromAsset.owner.address,
                destination = toAsset.owner.address,
                amount = Crypto(fromValue, fromAsset.asset.decimals).atomicValue.toString(),
            )
        } catch (err: Throwable) {
            swapScreenState.update {
                SwapState.Error(
                    when (err) {
                        is SwapperException.NotSupportedAsset -> SwapError.NotSupportedAsset
                        is SwapperException.NotSupportedPair -> SwapError.NotSupportedPair
                        is SwapperException.NotSupportedChain -> SwapError.NotSupportedChain
                        is SwapperException.NoQuoteAvailable -> SwapError.NoQuote
                        else -> SwapError.Unknown(err.localizedMessage ?: err.message ?: "")
                    }
                )
            }
            return@mapLatest null
        }
        val amount = toAsset.asset.format(Crypto(quote?.toValue ?: "0"), 8, showSymbol = false)
        withContext(Dispatchers.Main) { toValue.edit { replace(0, length, amount) } }
        val requestApprove = quote?.approval is ApprovalType.Approve
        swapScreenState.update {
            if (it == SwapState.Approving) return@update it
            if (requestApprove) SwapState.RequestApprove else SwapState.Ready
        }
        quote
    }
    .flowOn(Dispatchers.IO)
    .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val toValue: TextFieldState = TextFieldState()
    val toEquivalent = quote.combine(assetsState) { quote, assets ->
        if (quote == null || assets?.to == null) {
            return@combine ""
        }
        val price = assets.to.price
        if (price?.currency != null && price.price.price > 0) {
            assets.to.price!!.currency.format(Crypto(quote.toValue).convert(assets.to.asset.decimals, price.price.price).atomicValue)
        } else {
            ""
        }
    }
    .filterNotNull()
    .stateIn(viewModelScope, SharingStarted.Eagerly, "")

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
        val update = if (current.sameChain()) { // TODO: Change it validation
            savedStateHandle[pairArg] = "${current.fromId?.toIdentifier()}|${current.toId?.toIdentifier()}"
            null
        } else {
            current.opposite()
        }
        selectPair.update { update }
    }

    private fun updateBalances(fromId: AssetId, toId: AssetId) {
        val session = sessionRepository.getSession() ?: return
        val account = session.wallet.getAccount(fromId.chain) ?: return
        viewModelScope.launch(Dispatchers.IO) {
            assetsRepository.switchVisibility(session.wallet.id, account, fromId, true, session.currency)
        }
        viewModelScope.launch(Dispatchers.IO) {
            assetsRepository.switchVisibility(session.wallet.id, account, toId, true, session.currency)
        }
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
        } catch (_: Throwable) {
            swapScreenState.update { SwapState.Error(SwapError.IncorrectInput) }
            return@launch
        }
        val from = assetsState.value?.from ?: return@launch
        val to = assetsState.value?.to ?: return@launch
        val quote = quote.value
        if (quote == null) {
            swapScreenState.update { SwapState.Error(SwapError.NoQuote) }
            return@launch
        }
        val wallet = sessionRepository.getSession()?.wallet ?: return@launch
        val swapData = swapRepository.getQuoteData(quote, wallet)
        val approvalData = quote.approval
        when (approvalData) {
            is ApprovalType.Approve -> {
                swapScreenState.update { SwapState.RequestApprove }
                onConfirm(
                    ConfirmParams.TokenApprovalParams(
                        assetId = if (from.asset.id.type() == AssetSubtype.TOKEN) from.asset.id else to.asset.id,
                        data = encodeApprove(approvalData.v1.spender).bytesToHex(),
                        provider = swapProviderNameToString(quote.data.provider),
                        contract = approvalData.v1.token,
                    )
                )
            }
            is ApprovalType.Permit2,
            ApprovalType.None -> {
                swapScreenState.update { SwapState.Ready }
                onConfirm(
                    ConfirmParams.SwapParams(
                        fromAssetId = from.asset.id,
                        toAssetId = to.asset.id,
                        fromAmount = Crypto(fromAmount, from.asset.decimals).atomicValue,
                        toAmount = BigInteger(quote.toValue),
                        swapData = swapData.data,
                        provider = swapProviderNameToString(quote.data.provider),
                        to = swapData.to,
                        value = swapData.value,
                    )
                )
            }
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