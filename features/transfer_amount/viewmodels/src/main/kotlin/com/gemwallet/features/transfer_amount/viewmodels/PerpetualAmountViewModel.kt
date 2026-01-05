package com.gemwallet.features.transfer_amount.viewmodels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.gemwallet.android.application.perpetual.coordinators.GetPerpetual
import com.gemwallet.android.application.perpetual.coordinators.GetPerpetualBalance
import com.gemwallet.android.data.repositoreis.assets.AssetsRepository
import com.gemwallet.android.data.repositoreis.session.SessionRepository
import com.gemwallet.android.data.repositoreis.tokens.TokensRepository
import com.gemwallet.android.domains.asset.chain
import com.gemwallet.android.ext.getAccount
import com.gemwallet.android.model.AmountParams
import com.gemwallet.android.model.AssetInfo
import com.gemwallet.android.model.ConfirmParams
import com.gemwallet.android.model.format
import com.gemwallet.features.transfer_amount.models.AmountError
import com.wallet.core.primitives.AssetId
import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.Currency
import com.wallet.core.primitives.TransactionType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import java.math.BigDecimal
import java.math.BigInteger
import javax.inject.Inject
import kotlin.math.min

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class PerpetualAmountViewModel @Inject constructor(
    sessionRepository: SessionRepository,
    assetsRepository: AssetsRepository,
    tokenRepository: TokensRepository,
    getPerpetual: GetPerpetual,
    getPerpetualBalance: GetPerpetualBalance,
    savedStateHandle: SavedStateHandle
) : AmountBaseViewModel(savedStateHandle) {

    val perpetual = params.filterNotNull()
        .map { it.perpetualId }
        .filterNotNull()
        .flatMapLatest { getPerpetual.getPerpetual(it) }
        .onEach {
            val assetId = getAssetId(it?.asset?.chain ?: return@onEach)
            tokenRepository.search(assetId)
            val session = sessionRepository.session().firstOrNull() ?: return@onEach
            val owner = session.wallet.getAccount(assetId.chain) ?: return@onEach
            assetsRepository.switchVisibility(session.wallet.id, owner, assetId, false)
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val availableLeverages = perpetual.filterNotNull().map {
        val list = mutableListOf<Int>()
        val minLeverage = min(it.maxLeverage, 5)
        for (i in minLeverage .. it.maxLeverage step 5) {
            list.add(i)
        }
        list
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val leverage = MutableStateFlow<Int>(10)

    override val assetInfo: StateFlow<AssetInfo?> = perpetual /// TODO: ???
        .filterNotNull()
        .flatMapLatest {
            val assetId = getAssetId(it.asset.chain)
            assetsRepository.getAssetInfo(assetId)
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    override val availableBalance: StateFlow<BigDecimal> = combine(
        sessionRepository.session().filterNotNull(),
        perpetual.filterNotNull()
    ) { session, perpetual ->
        val chain = perpetual.asset.id.chain
        session.wallet.getAccount(chain)
    }
    .filterNotNull()
    .flatMapLatest { account ->
        getPerpetualBalance.getBalance(account.chain, account.address)
    }
    .mapLatest { it?.available?.toBigDecimal() ?: BigDecimal.ZERO }
    .stateIn(viewModelScope, SharingStarted.Eagerly, BigDecimal.ZERO)

    override val availableBalanceFormatted: StateFlow<String> = availableBalance
        .mapLatest { Currency.USD.format(it.toDouble()) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, "")

    override fun onMaxAmount() {
        updateAmount(availableBalance.value.toString(), true)
    }

    override fun onNext(
        params: AmountParams,
        rawAmount: String,
        onConfirm: (ConfirmParams) -> Unit
    ) {
        val assetInfo = assetInfo.value
        val owner = assetInfo?.owner ?: return
        val asset = assetInfo.asset
        val decimals = asset.decimals
        val price = assetInfo.price?.price?.price ?: 0.0
        val inputType = amountInputType.value
        validateAmount(asset, rawAmount, BigInteger.ZERO)

        val amount = inputType.getAmount(rawAmount, decimals, price)
        validateBalance(assetInfo, amount)

        amountError.update { AmountError.None }

        val builder = ConfirmParams.Builder(asset, owner, amount.atomicValue, maxAmount.value)
        val nextParams = when (params.txType) {
            TransactionType.PerpetualOpenPosition -> builder.perpetual()
            else -> throw IllegalArgumentException()
        }
        onConfirm(nextParams)
    }

    private fun getAssetId(chain: Chain): AssetId { // TODO: Check it
        return when (chain) {
            Chain.HyperCore -> AssetId(Chain.Arbitrum, "0xaf88d065e77c8cC2239327C5EDb3A432268e5831")
            else -> throw IllegalArgumentException()
        }
    }

    fun setLeverage(value: Int) {
        leverage.update { value }
    }
}