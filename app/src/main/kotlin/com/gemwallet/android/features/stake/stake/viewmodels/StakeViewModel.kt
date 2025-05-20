package com.gemwallet.android.features.stake.stake.viewmodels

import android.text.format.DateUtils
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gemwallet.android.data.repositoreis.assets.AssetsRepository
import com.gemwallet.android.data.repositoreis.session.SessionRepository
import com.gemwallet.android.data.repositoreis.stake.StakeRepository
import com.gemwallet.android.ext.asset
import com.gemwallet.android.ext.byChain
import com.gemwallet.android.ext.getAccount
import com.gemwallet.android.ext.toAssetId
import com.gemwallet.android.features.stake.stake.model.StakeError
import com.gemwallet.android.features.stake.stake.model.StakeUIState
import com.gemwallet.android.model.AssetInfo
import com.gemwallet.android.model.ConfirmParams
import com.gemwallet.android.model.Crypto
import com.gemwallet.android.model.Session
import com.gemwallet.android.model.format
import com.gemwallet.android.ui.components.image.getIconUrl
import com.wallet.core.primitives.Account
import com.wallet.core.primitives.Delegation
import com.wallet.core.primitives.StakeChain
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import uniffi.gemstone.Config
import java.math.BigInteger
import javax.inject.Inject

private const val assetIdArg = "assetId"

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class StakeViewModel @Inject constructor(
    private val assetsRepository: AssetsRepository,
    private val stakeRepository: StakeRepository,
    sessionRepository: SessionRepository,
    stateHandle: SavedStateHandle,
): ViewModel() {

    private val assetId = stateHandle.getStateFlow<String?>(assetIdArg, null)
        .filterNotNull()
        .map { it.toAssetId() }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    private val assetInfo = assetId.filterNotNull().flatMapLatest { assetsRepository.getAssetInfo(it) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    private val session = sessionRepository.session()
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    private val account = session.combine(assetId.filterNotNull()) { session, assetId ->
        session?.wallet?.getAccount(assetId.chain)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    private val delegations = account.combine(assetId.filterNotNull()) { account, assetId -> Pair(account, assetId) }
        .flatMapLatest {
            val (account, assetId) = it
            val accountAddress = account?.address ?: return@flatMapLatest emptyFlow()
            stakeRepository.getDelegations(assetId, accountAddress)
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private val rewardsAmount = delegations
        .mapLatest { delegations ->
            delegations.map { BigInteger(it.base.rewards) }
                .reduceOrNull { acc, delegation -> acc + delegation } ?: BigInteger.ZERO
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, BigInteger.ZERO)

    private val sync = MutableStateFlow<Boolean>(true)

    private val isSync = combine(sync, assetId.filterNotNull(), account.filterNotNull()) { sync, assetId, account -> Triple(sync, assetId, account) }
        .flatMapLatest {
            val (isSync, assetId, account) = it
            flow {
                if (!isSync) {
                    emit(false)
                    return@flow
                }
                emit(true)
                assetsRepository.syncMarketInfo(assetId, account)
                stakeRepository.sync(assetId.chain, account.address)
                emit(false)
                sync.update { false }
            }
        }
        .flowOn(Dispatchers.IO)
        .stateIn(viewModelScope, SharingStarted.Eagerly, true)

    val uiState = combine(
        assetInfo.filterNotNull(),
        session.filterNotNull(),
        account.filterNotNull(),
        delegations,
        rewardsAmount,
        isSync,
    ) { flows ->
        val assetInfo = flows[0] as AssetInfo
        val session = flows[1] as Session
        val account = flows[2] as Account
        val delegations = flows[3] as List<Delegation>
        val rewardsAmount = flows[4] as BigInteger
        val isSync = flows[5] as Boolean
        StakeUIState(
            loading = isSync,
            error = StakeError.None,
            assetId = assetInfo.asset.id,
            assetIcon = assetInfo.id().getIconUrl(),
            walletType = session.wallet.type,
            stakeChain = StakeChain.byChain(assetInfo.asset.id.chain)!!,
            assetDecimals = assetInfo.asset.decimals,
            assetSymbol = assetInfo.asset.symbol,
            ownerAddress = account.address,
            title = "${assetInfo.asset.id.chain.asset().name} (${assetInfo.asset.symbol})",
            apr = assetInfo.stakeApr ?: 0.0,
            lockTime = (Config().getStakeConfig(account.chain.string).timeLock / (DateUtils.DAY_IN_MILLIS / 1000).toULong()).toInt(),
            hasRewards = rewardsAmount > BigInteger.ZERO,
            rewardsAmount = assetInfo.asset.format(Crypto(rewardsAmount)),
            delegations = delegations,
        )
    }.stateIn(viewModelScope, SharingStarted.Eagerly, null)


    fun onRefresh() {
        sync.update { true }
    }

    fun onRewards(onConfirm: (ConfirmParams) -> Unit) {
        val assetInfo = assetInfo.value ?: return
        val account = account.value ?: return
        val validatorsId = delegations.value.filter { BigInteger(it.base.rewards) > BigInteger.ZERO }
            .map { it.base.validatorId }
            .toSet()
            .toList()
        onConfirm(
            ConfirmParams.Stake.RewardsParams(
                asset = assetInfo.asset,
                from = account,
                validatorsId = validatorsId,
                amount = rewardsAmount.value
            )
        )
    }
}