package com.gemwallet.android.features.transactions.details.viewmodels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gemwallet.android.cases.transactions.GetTransactionCase
import com.gemwallet.android.data.repositories.asset.AssetsRepository
import com.gemwallet.android.data.config.ConfigRepository
import com.gemwallet.android.data.repositories.session.SessionRepository
import com.gemwallet.android.ext.asset
import com.gemwallet.android.ext.getSwapMetadata
import com.gemwallet.android.features.transactions.details.model.TxDetailsScreenModel
import com.gemwallet.android.features.transactions.navigation.txIdArg
import com.gemwallet.android.interactors.chain
import com.gemwallet.android.interactors.getIconUrl
import com.gemwallet.android.model.Crypto
import com.gemwallet.android.model.format
import com.gemwallet.android.ui.components.getRelativeDate
import com.wallet.core.primitives.Currency
import com.wallet.core.primitives.TransactionExtended
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import uniffi.Gemstone.Explorer
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class TransactionDetailsViewModel @Inject constructor(
    sessionRepository: SessionRepository,
    private val configRepository: ConfigRepository,
    private val getTransactionCase: GetTransactionCase,
    private val assetsRepository: AssetsRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val tx: StateFlow<TransactionExtended?> = savedStateHandle.getStateFlow<String?>(txIdArg, null)
    .filterNotNull()
    .flatMapLatest { getTransactionCase.getTransaction(it) }
    .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    private val assets = tx.flatMapLatest { tx ->
        val swapMetadata = tx?.transaction?.getSwapMetadata() ?: return@flatMapLatest emptyFlow()
        assetsRepository.getAssetsInfo(listOf(swapMetadata.fromAsset, swapMetadata.toAsset))
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val screenModel = tx.combine(assets) { transaction, assets ->
        transaction ?: return@combine null
        val swapMetadata = transaction.transaction.getSwapMetadata()
        val fromId = swapMetadata?.fromAsset
        val toId = swapMetadata?.toAsset
        val currency = sessionRepository.getSession()?.currency ?: Currency.USD
        val tx = transaction.transaction
        val asset = transaction.asset
        val feeAsset = transaction.feeAsset
        val value = Crypto(tx.value.toBigInteger())
        val fiat = transaction.price?.price?.let {
            currency.format(value.convert(asset.decimals, it))
        } ?: ""
        val fee = Crypto(tx.fee.toBigInteger())
        val feeCrypto = feeAsset.format(fee)
        val feeFiat = transaction.feePrice?.price?.let {
            currency.format(fee.convert(feeAsset.decimals, it))
        } ?: ""
        val blockExplorerName = configRepository.getCurrentBlockExplorer(transaction.asset.chain())
        val explorerUrl = Explorer(asset.chain().string).getTransactionUrl(blockExplorerName, tx.hash)

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
            explorerName = blockExplorerName,
            fromAsset = assets.firstOrNull { it.id() == fromId },
            toAsset = assets.firstOrNull { it.id() == toId },
            fromValue = swapMetadata?.fromValue,
            toValue = swapMetadata?.toValue
        )
    }
    .stateIn(viewModelScope, started = SharingStarted.Eagerly, null)
}