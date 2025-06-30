package com.gemwallet.android.features.transactions.details.viewmodels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gemwallet.android.cases.nodes.GetCurrentBlockExplorer
import com.gemwallet.android.cases.transactions.GetTransaction
import com.gemwallet.android.data.repositoreis.assets.AssetsRepository
import com.gemwallet.android.data.repositoreis.session.SessionRepository
import com.gemwallet.android.ext.asset
import com.gemwallet.android.ext.chain
import com.gemwallet.android.ext.getAssociatedAssetIds
import com.gemwallet.android.ext.getNftMetadata
import com.gemwallet.android.ext.getSwapMetadata
import com.gemwallet.android.features.transactions.details.model.TxDetailsScreenModel
import com.gemwallet.android.features.transactions.navigation.txIdArg
import com.gemwallet.android.model.Crypto
import com.gemwallet.android.model.TransactionExtended
import com.gemwallet.android.model.format
import com.gemwallet.android.ui.components.getRelativeDate
import com.gemwallet.android.ui.components.image.getIconUrl
import com.gemwallet.android.ui.components.image.getSwapProviderIcon
import com.wallet.core.primitives.Currency
import com.wallet.core.primitives.SwapProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import uniffi.gemstone.Explorer
import uniffi.gemstone.GemSwapProvider
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class TransactionDetailsViewModel @Inject constructor(
    sessionRepository: SessionRepository,
    private val getTransaction: GetTransaction,
    private val getCurrentBlockExplorer: GetCurrentBlockExplorer,
    private val assetsRepository: AssetsRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val tx: StateFlow<TransactionExtended?> = savedStateHandle.getStateFlow<String?>(txIdArg, null)
        .filterNotNull()
        .flatMapLatest { getTransaction.getTransaction(it) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    private val assets = tx.flatMapLatest { tx ->
        val ids = tx?.transaction?.getAssociatedAssetIds() ?: return@flatMapLatest emptyFlow()
        assetsRepository.getAssetsInfo(ids)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val screenModel = tx.combine(assets) { transaction, assets ->
        transaction ?: return@combine null
        val swapMetadata = transaction.transaction.getSwapMetadata()
        val nftMetadata = transaction.transaction.getNftMetadata()
        val fromId = swapMetadata?.fromAsset
        val toId = swapMetadata?.toAsset
        val currency = sessionRepository.getSession()?.currency ?: Currency.USD
        val tx = transaction.transaction
        val asset = transaction.asset
        val feeAsset = transaction.feeAsset
        val value = Crypto(tx.value.toBigInteger())
        val fiat = transaction.price?.price?.let {
            currency.format(value.convert(asset.decimals, it).atomicValue)
        } ?: ""
        val fee = Crypto(tx.fee.toBigInteger())
        val feeCrypto = feeAsset.format(fee)
        val feeFiat = transaction.feePrice?.price?.let {
            currency.format(fee.convert(feeAsset.decimals, it).atomicValue)
        } ?: ""
        val blockExplorerName = getCurrentBlockExplorer.getCurrentBlockExplorer(transaction.asset.chain())
        val explorer = Explorer(asset.chain().string)
        val provider = swapMetadata?.provider
        val swapExplorerUrl = provider?.let { explorer.getTransactionSwapUrl(blockExplorerName, tx.hash, provider) }
        val explorerUrl = swapExplorerUrl?.url ?: explorer.getTransactionUrl(blockExplorerName, tx.hash)

        TxDetailsScreenModel(
            assetId = asset.id,
            assetSymbol = asset.symbol,
            assetIcon = asset.getIconUrl(),
            assetType = asset.type,
            cryptoAmount = asset.format(value),
            fiatAmount = fiat,
            createdAt = getRelativeDate(tx.createdAt),
            direction = tx.direction,
            from = tx.from,
            to = tx.to,
            memo = tx.memo,
            state = tx.state,
            networkTitle = asset.id.chain.asset().name,
            feeCrypto = feeCrypto,
            feeFiat = feeFiat,
            type = tx.type,
            explorerUrl = explorerUrl,
            explorerName = swapExplorerUrl?.name ?: blockExplorerName,
            fromAsset = assets.firstOrNull { it.id() == fromId },
            toAsset = assets.firstOrNull { it.id() == toId },
            fromValue = swapMetadata?.fromValue,
            toValue = swapMetadata?.toValue,
            provider = SwapProvider.entries.firstOrNull { it.string == provider },
            currency = currency,
            nftAsset = nftMetadata,
        )
    }
    .flowOn(Dispatchers.Default)
    .stateIn(viewModelScope, started = SharingStarted.Eagerly, null)
}

fun SwapProvider.getIcon(): String {
    return when (this) {
        SwapProvider.UniswapV3 -> GemSwapProvider.UNISWAP_V3
        SwapProvider.UniswapV4 -> GemSwapProvider.UNISWAP_V4
        SwapProvider.PancakeswapV3 -> GemSwapProvider.PANCAKESWAP_V3
        SwapProvider.PancakeswapAptosV2 -> GemSwapProvider.PANCAKESWAP_APTOS_V2
        SwapProvider.Thorchain -> GemSwapProvider.THORCHAIN
        SwapProvider.Orca -> GemSwapProvider.ORCA
        SwapProvider.Jupiter -> GemSwapProvider.JUPITER
        SwapProvider.Across -> GemSwapProvider.ACROSS
        SwapProvider.Oku -> GemSwapProvider.OKU
        SwapProvider.Wagmi -> GemSwapProvider.WAGMI
        SwapProvider.Cetus -> GemSwapProvider.CETUS
        SwapProvider.StonfiV2 -> GemSwapProvider.STONFI_V2
        SwapProvider.Mayan -> GemSwapProvider.MAYAN
        SwapProvider.Reservoir -> GemSwapProvider.RESERVOIR
        SwapProvider.Symbiosis -> GemSwapProvider.SYMBIOSIS
        SwapProvider.Chainflip -> GemSwapProvider.CHAINFLIP
        SwapProvider.CetusAggregator -> GemSwapProvider.CETUS_AGGREGATOR
        SwapProvider.Aerodrome -> GemSwapProvider.AERODROME
        SwapProvider.Relay -> GemSwapProvider.RELAY
    }.getSwapProviderIcon()

}