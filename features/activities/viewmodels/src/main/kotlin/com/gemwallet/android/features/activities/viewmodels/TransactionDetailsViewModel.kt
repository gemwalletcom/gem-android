package com.gemwallet.android.features.activities.viewmodels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gemwallet.android.cases.nodes.GetCurrentBlockExplorer
import com.gemwallet.android.cases.transactions.GetTransaction
import com.gemwallet.android.data.repositoreis.assets.AssetsRepository
import com.gemwallet.android.data.repositoreis.session.SessionRepository
import com.gemwallet.android.ext.chain
import com.gemwallet.android.ext.getAssociatedAssetIds
import com.gemwallet.android.ext.getNftMetadata
import com.gemwallet.android.ext.getSwapMetadata
import com.gemwallet.android.model.Crypto
import com.gemwallet.android.model.TransactionExtended
import com.gemwallet.android.model.format
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

    private val tx: StateFlow<TransactionExtended?> = savedStateHandle.getStateFlow<String?>("txId", null)
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
            asset = asset,
            cryptoAmount = asset.format(value),
            fiatAmount = fiat,
            createdAt = tx.createdAt,
            direction = tx.direction,
            from = tx.from,
            to = tx.to,
            memo = tx.memo,
            state = tx.state,
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