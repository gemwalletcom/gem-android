package com.gemwallet.android.features.swap.viewmodels

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.clearText
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gemwallet.android.data.asset.AssetsRepository
import com.gemwallet.android.data.repositories.session.SessionRepository
import com.gemwallet.android.data.swap.SwapRepository
import com.gemwallet.android.ext.getAccount
import com.gemwallet.android.ext.type
import com.gemwallet.android.features.swap.model.SwapDetails
import com.gemwallet.android.features.swap.model.SwapError
import com.gemwallet.android.features.swap.model.SwapItemState
import com.gemwallet.android.features.swap.model.SwapItemType
import com.gemwallet.android.features.swap.model.SwapScreenState
import com.gemwallet.android.interactors.getIconUrl
import com.gemwallet.android.math.numberParse
import com.gemwallet.android.math.toHexString
import com.gemwallet.android.model.AssetInfo
import com.gemwallet.android.model.ConfirmParams
import com.gemwallet.android.model.Crypto
import com.gemwallet.android.model.Fiat
import com.wallet.core.primitives.AssetId
import com.wallet.core.primitives.AssetSubtype
import com.wallet.core.primitives.Currency
import com.wallet.core.primitives.SwapQuote
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.math.BigDecimal
import java.math.BigInteger
import javax.inject.Inject
import kotlin.math.max

@HiltViewModel
class SwapViewModel @Inject constructor(
    private val sessionRepository: SessionRepository,
    private val assetsRepository: AssetsRepository,
    private val swapRepository: SwapRepository,
) : ViewModel() {

    private val state = MutableStateFlow(State())
    val uiState = state.map { it.toUIState() }
        .stateIn(viewModelScope, SharingStarted.Eagerly, SwapScreenState())
    val payValue: TextFieldState = TextFieldState()
    val receiveValue: TextFieldState = TextFieldState()

    private var quoteJob: Job? = null
    private var allowanceJob: Job? = null

    fun init(fromId: AssetId?, toId: AssetId?) = viewModelScope.launch {
        withContext(Dispatchers.IO) {
            if (fromId == null || toId == null) {
                state.update { it.copy(loading = false, error = SwapError.SelectAsset) }
                return@withContext
            }
            loadData(fromId, toId)
            updateBalances(fromId, toId)
        }
    }

    private suspend fun updateBalances(fromId: AssetId, toId: AssetId) {
        val session = sessionRepository.getSession()
        val account = session?.wallet?.getAccount(fromId.chain)
        if (account == null) {
            state.update { it.copy(loading = false, select = SwapItemType.Pay) }
            return
        }
        assetsRepository.updatePrices(session.currency, fromId, toId)
        assetsRepository.updateBalances(fromId, toId)
        loadData(fromId, toId)
    }

    private suspend fun loadData(fromId: AssetId, toId: AssetId) = withContext(Dispatchers.IO) {
        val session = sessionRepository.getSession() ?: return@withContext
        val from = assetsRepository.getById(session.wallet, fromId).getOrNull()?.firstOrNull()
        val to = assetsRepository.getById(session.wallet, toId).getOrNull()?.firstOrNull()

        withContext(Dispatchers.Main) {
            receiveValue.clearText()
        }
        state.update {
            State(loading = false, payAsset = from, receiveAsset = to, quote = null, select = it.select)
        }
    }

    private fun onRefreshAssets() {
        val fromAssetId = state.value.payAsset?.asset?.id ?: return
        val toAssetId = state.value.receiveAsset?.asset?.id ?: return
        viewModelScope.launch {
            loadData(fromAssetId, toAssetId)
        }
    }

    suspend fun updateQuote() {
        snapshotFlow { payValue.text }.collectLatest { newValue ->
            if (quoteJob?.isActive == true) {
                quoteJob?.cancel()
            }
            if (allowanceJob?.isActive == true) {
                allowanceJob?.cancel()
            }
            if (newValue.isEmpty() || newValue.toString() == "0") {
                receiveValue.edit { replace(0, length, "0") }
                state.update { it.copy(quote = null, calculatingQuote = false) }
                return@collectLatest
            }
            val equivalent = try {
                Fiat(newValue.toString().numberParse().toDouble() * (state.value.payAsset?.price?.price?.price ?: throw Exception()))
            } catch (err: Throwable) {
                null
            }
            state.update { it.copy(payEquivalent = equivalent, quote = null, calculatingQuote = true) }

            quoteJob = viewModelScope.launch(Dispatchers.IO) {
                delay(500L)
                val receiveAsset = state.value.receiveAsset ?: return@launch
                val quote = try {
                    Result.success(getQuote())
                } catch (err: Throwable) {
                    Result.failure(err)
                }
                withContext(Dispatchers.Main) {
                    receiveValue.edit {
                        replace(
                            0,
                            length,
                            Crypto(
                                quote.getOrNull()?.toAmount ?: "0"
                            ).format(receiveAsset.asset.decimals, "", 8)
                        )
                    }
                }
                state.update { it.copy(quote = quote.getOrNull(), calculatingQuote = false, error = SwapError.None) }
            }
        }
    }

    private suspend fun getQuote(includeData: Boolean = false): SwapQuote? {
        val payAsset = state.value.payAsset
        val receiveAsset = state.value.receiveAsset
        if (payAsset == null || receiveAsset == null) {
            return null
        }
        val amount = try {
            getPayAmount()
        } catch (err: Throwable) {
            BigDecimal.TEN
        }

        val quote = swapRepository.getQuote(
            from = payAsset.asset.id,
            to = receiveAsset.asset.id,
            ownerAddress = payAsset.owner.address,
            amount = Crypto(amount, payAsset.asset.decimals).atomicValue.toString(),
            includeData = includeData,
        )
        allowanceJob = updateAllowance(payAsset.owner.address, payAsset.asset.id, quote?.quote?.approval?.spender ?: "")
        return quote?.quote
    }

    fun switchSwap() {
        if (quoteJob?.isActive == true) {
            quoteJob?.cancel()
        }
        if (allowanceJob?.isActive == true) {
            allowanceJob?.cancel()
        }
        clearPayAmount()
        receiveValue.clearText()
        val receiveAsset = state.value.payAsset
        val payAsset = state.value.receiveAsset
        state.update {
            it.copy(payAsset = payAsset, receiveAsset = receiveAsset, quote = null, error = SwapError.None)
        }
    }

    fun assetSelect(type: SwapItemType?) {
        state.update { it.copy(select = type) }
    }

    fun changeAsset(assetId: AssetId, select: SwapItemType) {
        val prevSelected = state.value.selectedAssetId
        val fromId = when (select) {
            SwapItemType.Pay -> assetId
            SwapItemType.Receive -> prevSelected ?: state.value.payAsset?.asset?.id
        }
        val toId = when (select) {
            SwapItemType.Pay -> prevSelected ?: state.value.receiveAsset?.asset?.id
            SwapItemType.Receive -> assetId
        }
        if (fromId?.chain != toId?.chain) {
            state.update {
                it.copy(
                    selectedAssetId = assetId,
                    select = if (select == SwapItemType.Pay) SwapItemType.Receive else SwapItemType.Pay
                )
            }
        } else {
            state.update { State() }
            init(fromId!!, toId!!)
        }
    }

    private fun updateAllowance(ownerAddress: String, payAssetId: AssetId, spender: String) = viewModelScope.launch {
        val allowance = swapRepository.getAllowance(assetId = payAssetId, owner = ownerAddress, spender = spender)
        state.update { it.copy(allowance = allowance) }
    }

    fun swap(
        onConfirm: (ConfirmParams) -> Unit,
    ) = viewModelScope.launch {
        if (state.value.swapping) {
            return@launch
        }
        state.update { it.copy(swapping = true) }
        val payAsset = state.value.payAsset ?: return@launch
        val receiveAsset = state.value.receiveAsset ?: return@launch
        val allowance = state.value.allowance
        val quote = getQuote(allowance)
        if (quote == null && allowance) {
            state.update {
                it.copy(loading = false, swapping = false, error = SwapError.NoQuote)
            }
            return@launch
        }
        state.update { it.copy(swapping = false) }
        if (allowance) {
            if (quote == null) {
                return@launch
            }
            onConfirm(
                ConfirmParams.SwapParams(
                    fromAssetId = payAsset.asset.id,
                    toAssetId = receiveAsset.asset.id,
                    fromAmount = Crypto(getPayAmount(), payAsset.asset.decimals).atomicValue,
                    toAmount = BigInteger(quote.toAmount),
                    swapData = quote.data?.data ?: return@launch,
                    provider = quote.provider.name,
                    to = quote.data?.to ?: return@launch,
                    value = quote.data?.value ?: return@launch,
                )
            )
        } else {
            val meta = swapRepository.encodeApprove(quote?.approval?.spender ?: "") // TODO: Move to special operator
            onConfirm(
                ConfirmParams.TokenApprovalParams(
                    assetId = if (payAsset.asset.id.type() == AssetSubtype.TOKEN) payAsset.asset.id else receiveAsset.asset.id,
                    approvalData = meta.toHexString(),
                    provider = quote?.provider?.name ?: "",
                )
            )
        }
    }

    private fun getPayAmount(): BigDecimal = payValue.text.toString().numberParse()

    private fun clearPayAmount() = payValue.clearText()

    private data class State(
        val loading: Boolean = true,
        val swapping: Boolean = false,
        val error: SwapError = SwapError.None,
        val currency: Currency = Currency.USD,
        val payAsset: AssetInfo? = null,
        val receiveAsset: AssetInfo? = null,
        val quote: SwapQuote? = null,
        val allowance: Boolean = false,
        val payEquivalent: Fiat? = null,
        val calculatingQuote: Boolean = false,
        val select: SwapItemType? = null,
        val selectedAssetId: AssetId? = null,
    ) {
        fun toUIState(): SwapScreenState {
            val symbolLength = max(
                payAsset?.asset?.symbol?.length ?: 0,
                receiveAsset?.asset?.symbol?.length ?: 0
            )
            val payItem = getSwapItem(SwapItemType.Pay, payAsset, symbolLength)
            val receiveItem = getSwapItem(SwapItemType.Receive, receiveAsset, symbolLength)
            return SwapScreenState(
                isLoading = loading,
                details = when {
                    payItem == null || receiveItem == null -> SwapDetails.None
                    else -> SwapDetails.Quote(
                        error = error,
                        swaping = swapping,
                        allowance = allowance,
                        pay = payItem,
                        receive = receiveItem,
                    )
                },
                select = when {
                    select == null -> null
                    else -> SwapScreenState.Select(
                        changeType = select,
                        changeAssetId = when (select) {
                            SwapItemType.Pay -> payItem?.assetId
                            SwapItemType.Receive -> receiveItem?.assetId
                        },
                        oppositeAssetId = when (select) {
                            SwapItemType.Pay -> receiveItem?.assetId
                            SwapItemType.Receive -> payItem?.assetId
                        },
                        prevAssetId = selectedAssetId,
                    )
                }
            )
        }

        private fun getSwapItem(itemType: SwapItemType, assetInfo: AssetInfo?, symbolLength: Int): SwapItemState? {
            val asset = assetInfo?.asset ?: return null
            val equivalentValue = when (itemType) {
                SwapItemType.Pay -> (payEquivalent ?: Fiat(0.0)).format(0, currency.string, 2)
                SwapItemType.Receive -> if ((assetInfo.price?.price?.price ?: 0.0) > 0 && quote != null) {
                    Crypto(quote.toAmount).convert(asset.decimals, assetInfo.price!!.price.price).format(0, currency.string, 2)
                } else {
                    ""
                }
            }
            return SwapItemState(
                type = itemType,
                assetId = assetInfo.asset.id,
                assetType = asset.type,
                assetIcon = asset.getIconUrl(),
                assetSymbol = asset.symbol.padStart(symbolLength),
                equivalentValue = equivalentValue,
                assetBalanceValue = assetInfo.balances.calcTotal().value(asset.decimals).stripTrailingZeros().toPlainString(),
                assetBalanceLabel = assetInfo.balances.calcTotal().format(asset.decimals, asset.symbol, 4),
                calculating = itemType == SwapItemType.Receive && calculatingQuote
            )
        }
    }
}