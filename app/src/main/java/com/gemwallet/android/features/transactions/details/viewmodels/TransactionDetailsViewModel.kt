package com.gemwallet.android.features.transactions.details.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gemwallet.android.data.asset.AssetsRepository
import com.gemwallet.android.data.config.ConfigRepository
import com.gemwallet.android.data.repositories.session.SessionRepository
import com.gemwallet.android.data.transaction.TransactionsRepository
import com.gemwallet.android.ext.asset
import com.gemwallet.android.ext.getSwapMetadata
import com.gemwallet.android.features.transactions.details.model.TxDetailsSceneState
import com.gemwallet.android.interactors.chain
import com.gemwallet.android.interactors.getIconUrl
import com.gemwallet.android.model.AssetInfo
import com.gemwallet.android.model.Crypto
import com.gemwallet.android.ui.components.getRelativeDate
import com.wallet.core.primitives.Currency
import com.wallet.core.primitives.TransactionExtended
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import uniffi.Gemstone.Explorer
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class TransactionDetailsViewModel @Inject constructor(
    sessionRepository: SessionRepository,
    private val configRepository: ConfigRepository,
    private val transactionsRepository: TransactionsRepository,
    private val assetsRepository: AssetsRepository,
) : ViewModel() {

    private val txId = MutableStateFlow<String?>(null)
    private val _state = combine(
        sessionRepository.session(),
        txId.flatMapLatest {
            transactionsRepository.getTransaction(it ?: return@flatMapLatest MutableStateFlow(null))
        }
    ) { session, tx ->
        val swapMetadata = tx?.transaction?.getSwapMetadata()
        val wallet = session?.wallet
        val (fromAsset, toAsset) = if (swapMetadata != null && wallet !=  null) {
            Pair(
                assetsRepository.getById(wallet, swapMetadata.fromAsset).getOrNull()?.firstOrNull(),
                assetsRepository.getById(wallet, swapMetadata.toAsset).getOrNull()?.firstOrNull(),
            )
        } else {
            Pair(null, null)
        }

        val blockExplorerName = if (tx != null) {
            configRepository.getCurrentBlockExplorer(tx.asset.chain())
        } else {
            ""
        }

        State(
            loading = false,
            transaction = tx,
            currency = session?.currency ?: Currency.USD,
            blockExplorerName = blockExplorerName,
            fromAsset = fromAsset,
            toAsset = toAsset,
            fromValue = swapMetadata?.fromValue,
            toValue = swapMetadata?.toValue
        )
    }.flowOn(Dispatchers.IO)

    val uiState = _state.map { it.toUIState() }
        .stateIn(viewModelScope, started = SharingStarted.Eagerly, TxDetailsSceneState.Loading)

    fun setTxId(txId: String) {
        this.txId.update { txId }
    }

    private data class State(
        val loading: Boolean = false,
        val transaction: TransactionExtended? = null,
        val blockExplorerName: String = "",
        val currency: Currency = Currency.USD,
        val fromAsset: AssetInfo? = null,
        val toAsset: AssetInfo? = null,
        val fromValue: String? = null,
        val toValue: String? = null,
    ) {
        fun toUIState(): TxDetailsSceneState {
            return when {
                transaction == null -> TxDetailsSceneState.Loading
                else -> {
                    val tx = transaction.transaction
                    val asset = transaction.asset
                    val feeAsset = transaction.feeAsset
                    val value = Crypto(tx.value.toBigInteger())
                    val fiat = transaction.price?.price?.let {
                        value.convert(asset.decimals, it).format(0, currency.string, 2)
                    } ?: ""
                    val fee = Crypto(tx.fee.toBigInteger())
                    val feeCrypto = fee.format(feeAsset.decimals, feeAsset.symbol, 6)
                    val feeFiat = transaction.feePrice?.price?.let {
                        fee.convert(feeAsset.decimals, it).format(feeAsset.decimals, currency.string, 2, dynamicPlace = true)
                    } ?: ""

                    TxDetailsSceneState.Loaded(
                        assetId = asset.id,
                        assetSymbol = asset.symbol,
                        assetIcon = asset.getIconUrl(),
                        assetType = asset.type,
                        cryptoAmount = value.format(asset.decimals, asset.symbol, 6),
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
                        explorerUrl = Explorer(asset.chain().string).getTransactionUrl(
                            blockExplorerName, tx.hash),
                        explorerName = blockExplorerName,
                        fromAsset = fromAsset,
                        toAsset = toAsset,
                        fromValue = fromValue,
                        toValue = toValue,
                    )
                }
            }
        }
    }
}