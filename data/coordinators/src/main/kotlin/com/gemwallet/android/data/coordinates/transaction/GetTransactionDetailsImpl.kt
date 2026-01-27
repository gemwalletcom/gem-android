package com.gemwallet.android.data.coordinates.transaction

import androidx.compose.runtime.Stable
import com.gemwallet.android.application.transactions.coordinators.GetTransactionDetails
import com.gemwallet.android.cases.nodes.GetCurrentBlockExplorer
import com.gemwallet.android.data.repositoreis.assets.AssetsRepository
import com.gemwallet.android.data.repositoreis.session.SessionRepository
import com.gemwallet.android.data.repositoreis.transactions.TransactionRepository
import com.gemwallet.android.domains.transaction.aggregates.TransactionDetailsAggregate
import com.gemwallet.android.domains.transaction.values.TransactionDetailsValue
import com.gemwallet.android.domains.transaction.values.ValueGroup
import com.gemwallet.android.ext.getAssociatedAssetIds
import com.gemwallet.android.ext.getNftMetadata
import com.gemwallet.android.ext.getSwapMetadata
import com.gemwallet.android.math.getRelativeDate
import com.gemwallet.android.model.AssetInfo
import com.gemwallet.android.model.Crypto
import com.gemwallet.android.model.TransactionExtended
import com.gemwallet.android.model.format
import com.wallet.core.primitives.Asset
import com.wallet.core.primitives.Currency
import com.wallet.core.primitives.TransactionDirection
import com.wallet.core.primitives.TransactionState
import com.wallet.core.primitives.TransactionSwapMetadata
import com.wallet.core.primitives.TransactionType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.mapLatest
import uniffi.gemstone.GemSwapper
import uniffi.gemstone.SwapperProviderType

@OptIn(ExperimentalCoroutinesApi::class)
class GetTransactionDetailsImpl(
    private val sessionRepository: SessionRepository,
    private val transactionRepository: TransactionRepository,
    private val assetsRepository: AssetsRepository,
    private val getCurrentBlockExplorer: GetCurrentBlockExplorer,
    private val gemSwapper: GemSwapper,
) : GetTransactionDetails {

    override fun getTransactionDetails(id: String): Flow<TransactionDetailsAggregate?> {
        return combine(
            sessionRepository.session().filterNotNull(),
            transactionRepository.getTransaction(id),
        ) { session, data ->  Pair(session, data) }
            .flatMapLatest { (session, data) ->
                val ids = data?.transaction?.getAssociatedAssetIds() ?: return@flatMapLatest emptyFlow()
                val explorerInfo = getCurrentBlockExplorer.getBlockExplorerInfo(data.transaction).let { (url, name) ->
                    TransactionDetailsValue.Explorer(url, name)
                }
                assetsRepository.getAssetsInfo(ids).mapLatest { assets ->
                    val swapMetadata = data.transaction.getSwapMetadata()
                    val provider = gemSwapper.getProviders().firstOrNull { it.protocolId ==  swapMetadata?.provider }
                    TransactionDetailsAggregateImpl(
                        data = data,
                        associatedAssets = assets,
                        explorer = explorerInfo,
                        currency = session.currency,
                        swapProvider = provider,
                        swapMetadata = swapMetadata,
                    )
                }
            }
            .flowOn(Dispatchers.IO)
    }
}

@Stable
class TransactionDetailsAggregateImpl(
    private val data: TransactionExtended,
    private val associatedAssets: List<AssetInfo>,
    private val swapMetadata: TransactionSwapMetadata? = null,
    override val explorer: TransactionDetailsValue.Explorer,
    override val currency: Currency,
    swapProvider: SwapperProviderType? = null,
) : TransactionDetailsAggregate {

    override val id: String = data.transaction.id

    override val asset: Asset = data.asset
    override val type: TransactionType = data.transaction.type
    override val direction: TransactionDirection = data.transaction.direction
    override val state: TransactionState = data.transaction.state
    private val swapProvider = swapProvider?.name

    override val amount: TransactionDetailsValue.Amount
        get() {
            return when (data.transaction.type) {
                TransactionType.Swap -> {
                    val fromAsset = associatedAssets.firstOrNull { it.id() == swapMetadata?.fromAsset }
                    val toAsset = associatedAssets.firstOrNull { it.id() == swapMetadata?.toAsset }

                    if (swapMetadata == null || fromAsset == null || toAsset == null) {
                        TransactionDetailsValue.Amount.None
                    } else {
                        TransactionDetailsValue.Amount.Swap(
                            fromAsset = fromAsset,
                            toAsset = toAsset,
                            fromValue = swapMetadata.fromValue,
                            toValue = swapMetadata.toValue,
                            currency = currency,
                        )
                    }
                }
                TransactionType.TransferNFT -> {
                    data.transaction.getNftMetadata()?.let { TransactionDetailsValue.Amount.NFT(it) }
                        ?: TransactionDetailsValue.Amount.None
                }

                else -> {
                    val value = Crypto(data.transaction.value.toBigInteger())
                    val fiat = data.price?.price?.let {
                        currency.format(value.convert(asset.decimals, it).atomicValue)
                    } ?: ""

                    val (amount, equivalent) = when (data.transaction.type) {
                        TransactionType.StakeDelegate,
                        TransactionType.StakeUndelegate,
                        TransactionType.StakeRewards,
                        TransactionType.StakeRedelegate,
                        TransactionType.StakeWithdraw,
                        TransactionType.Swap,
                        TransactionType.StakeFreeze,
                        TransactionType.StakeUnfreeze,
                        TransactionType.Transfer -> Pair(asset.format(value), fiat)
                        TransactionType.TransferNFT,
                        TransactionType.AssetActivation,
                        TransactionType.SmartContractCall,
                        TransactionType.PerpetualOpenPosition,
                        TransactionType.PerpetualClosePosition,
                        TransactionType.PerpetualModifyPosition,
                        TransactionType.TokenApproval -> Pair(data.asset.symbol, null)
                    }
                    TransactionDetailsValue.Amount.Plain(data.asset, amount, equivalent)
                }
            }
        }

    override val fee: TransactionDetailsValue.Fee
        get() {
            val fee = Crypto(data.transaction.fee.toBigInteger())
            val feeCrypto = data.feeAsset.format(fee)
            val feeFiat = data.feePrice?.price?.let {
                currency.format(fee.convert(data.feeAsset.decimals, it).atomicValue, decimalPlace = 4)
            } ?: ""
            return TransactionDetailsValue.Fee(data.feeAsset, feeCrypto, feeFiat)
        }

    override val date: TransactionDetailsValue.Date = TransactionDetailsValue.Date(
        getRelativeDate(data.transaction.createdAt)
    )

    override val status: TransactionDetailsValue.Status = TransactionDetailsValue.Status(data.transaction.state)

    override val memo: TransactionDetailsValue.Memo? = data.transaction.memo
        ?.takeIf { it.isNotEmpty() }
        ?.let { TransactionDetailsValue.Memo(it) }

    override val network: TransactionDetailsValue.Network = TransactionDetailsValue.Network(asset)

    override val destination: TransactionDetailsValue.Destination? = when (data.transaction.type) {
        TransactionType.TokenApproval,
        TransactionType.StakeDelegate,
        TransactionType.StakeUndelegate,
        TransactionType.StakeRewards,
        TransactionType.StakeRedelegate,
        TransactionType.AssetActivation,
        TransactionType.SmartContractCall,
        TransactionType.PerpetualOpenPosition,
        TransactionType.PerpetualClosePosition,
        TransactionType.StakeFreeze,
        TransactionType.StakeUnfreeze,
        TransactionType.PerpetualModifyPosition,
        TransactionType.StakeWithdraw -> null
        TransactionType.Swap -> this@TransactionDetailsAggregateImpl.swapProvider?.let { TransactionDetailsValue.Destination.Provider(it) }
        TransactionType.Transfer,
        TransactionType.TransferNFT -> when (data.transaction.direction) {
            TransactionDirection.SelfTransfer,
            TransactionDirection.Outgoing -> TransactionDetailsValue.Destination.Recipient(data.transaction.to)
            TransactionDirection.Incoming -> TransactionDetailsValue.Destination.Sender(data.transaction.from)
        }
    }

    override val valueGroups: List<ValueGroup<TransactionDetailsValue>>
        get() = listOf(
            ValueGroup(listOf(amount)),
            ValueGroup(
                listOfNotNull(
                    date,
                    status,
                    destination,
                    network
                )
            ),
            ValueGroup(listOf(fee)),
            ValueGroup(listOf(explorer))
        )
}