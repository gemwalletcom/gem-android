package com.gemwallet.android.features.stake.stake.viewmodels

import android.text.format.DateUtils
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gemwallet.android.data.repositoreis.assets.AssetsRepository
import com.gemwallet.android.data.repositoreis.session.SessionRepository
import com.gemwallet.android.data.repositoreis.stake.StakeRepository
import com.gemwallet.android.ext.asset
import com.gemwallet.android.ext.byChain
import com.gemwallet.android.ext.getAccount
import com.gemwallet.android.features.stake.stake.model.StakeError
import com.gemwallet.android.features.stake.stake.model.StakeUIState
import com.gemwallet.android.model.AssetInfo
import com.gemwallet.android.model.ConfirmParams
import com.gemwallet.android.model.Crypto
import com.gemwallet.android.model.format
import com.gemwallet.android.ui.components.image.getIconUrl
import com.wallet.core.primitives.Account
import com.wallet.core.primitives.AssetId
import com.wallet.core.primitives.Delegation
import com.wallet.core.primitives.StakeChain
import com.wallet.core.primitives.WalletType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import uniffi.gemstone.Config
import java.math.BigInteger
import javax.inject.Inject

@HiltViewModel
class StakeViewModel @Inject constructor(
    private val sessionRepository: SessionRepository,
    private val assetsRepository: AssetsRepository,
    private val stakeRepository: StakeRepository,
): ViewModel() {

    private val state = MutableStateFlow(State())
    val uiState = state.map { it.toUIState() }
        .stateIn(viewModelScope, SharingStarted.Eagerly, StakeUIState.Loading)

    fun init(assetId: AssetId) {
        val session = sessionRepository.getSession() ?: return
        val account = session.wallet.getAccount(assetId.chain) ?: return
        viewModelScope.launch {
            val asset = assetsRepository.getAssetInfo(assetId).firstOrNull() ?: return@launch
            state.update { it.copy(apr = asset.stakeApr ?: 0.0) }
            onRefresh(assetId)
            stakeRepository.getDelegations(assetId, account.address).collect { delegations ->
                state.update { state ->
                    state.copy(
                        loading = false,
                        walletType = session.wallet.type,
                        account = account,
                        asset = asset,
                        delegations = delegations,
                        rewardsAmount = delegations.map { BigInteger(it.base.rewards) }
                            .reduceOrNull { acc, delegation -> acc + delegation } ?: BigInteger.ZERO,
                    )
                }
            }
        }
    }

    fun onRefresh() {
        onRefresh(state.value.asset?.id() ?: return)
    }

    fun onRefresh(assetId: AssetId) {
        viewModelScope.launch(Dispatchers.IO) {
            val asset = assetsRepository.getAssetInfo(assetId).firstOrNull() ?: return@launch
            val assetId = asset.id()
            state.update { it.copy(loading = true) }
            stakeRepository.sync(assetId.chain, asset.owner?.address ?: return@launch, asset.stakeApr ?: return@launch)
            state.update { it.copy(loading = false) }
        }
    }

    fun onRewards(onConfirm: (ConfirmParams) -> Unit) {
        val currentState = state.value
        onConfirm(
            ConfirmParams.Stake.RewardsParams(
                asset = currentState.asset?.asset!!,
                from = currentState.asset.owner!!,
                validatorsId = currentState.delegations
                    .filter { BigInteger(it.base.rewards) > BigInteger.ZERO }
                    .map { it.base.validatorId }
                    .toSet()
                    .toList(),
                amount = state.value.rewardsAmount
            )
        )
    }

    private data class State(
        val loading: Boolean = true,
        val error: StakeError = StakeError.None,
        val walletType: WalletType = WalletType.view,
        val asset: AssetInfo? = null,
        val account: Account? = null,
        val apr: Double = 0.0,
        val rewardsAmount: BigInteger = BigInteger.ZERO,
        val delegations: List<Delegation> = emptyList(),
    ) {
        fun toUIState(): StakeUIState {
            return when {
                asset == null || account == null -> StakeUIState.Loading
                else -> {
                    StakeUIState.Loaded(
                        loading = loading,
                        error = error,
                        assetId = asset.asset.id,
                        assetIcon = asset.id().getIconUrl(),
                        walletType = walletType,
                        stakeChain = StakeChain.byChain(asset.asset.id.chain)!!,
                        assetDecimals = asset.asset.decimals,
                        assetSymbol = asset.asset.symbol,
                        ownerAddress = account.address,
                        title = "${asset.asset.id.chain.asset().name} (${asset.asset.symbol})",
                        apr = apr,
                        lockTime = (Config().getStakeConfig(account.chain.string).timeLock / (DateUtils.DAY_IN_MILLIS / 1000).toULong()).toInt(),
                        hasRewards = rewardsAmount > BigInteger.ZERO,
                        rewardsAmount = asset.asset.format(Crypto(rewardsAmount)),
                        delegations = delegations,
                    )
                }
            }
        }
    }
}