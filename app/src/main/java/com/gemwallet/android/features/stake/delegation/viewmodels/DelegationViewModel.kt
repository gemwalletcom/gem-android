package com.gemwallet.android.features.stake.delegation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gemwallet.android.R
import com.gemwallet.android.data.asset.AssetsRepository
import com.gemwallet.android.data.repositories.session.SessionRepository
import com.gemwallet.android.data.stake.StakeRepository
import com.gemwallet.android.ext.byChain
import com.gemwallet.android.features.amount.model.AmountParams
import com.gemwallet.android.features.amount.navigation.OnAmount
import com.gemwallet.android.features.stake.delegation.model.DelegationSceneState
import com.gemwallet.android.features.stake.model.availableIn
import com.gemwallet.android.model.AssetInfo
import com.gemwallet.android.model.Crypto
import com.gemwallet.android.ui.components.CellEntity
import com.wallet.core.primitives.Delegation
import com.wallet.core.primitives.StakeChain
import com.wallet.core.primitives.TransactionType
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
import javax.inject.Inject

@HiltViewModel
class DelegationViewModel @Inject constructor(
    private val sessionRepository: SessionRepository,
    private val assetsRepository: AssetsRepository,
    private val stakeRepository: StakeRepository,
) : ViewModel() {

    private val state = MutableStateFlow(State())
    val uiState  = state.map { it.toUIState() }
        .stateIn(viewModelScope, SharingStarted.Eagerly, DelegationSceneState.Loading)

    fun init(validatorId: String, delegationId: String) {
        val session = sessionRepository.getSession() ?: return
        viewModelScope.launch(Dispatchers.IO) {
            val delegation = stakeRepository.getDelegation(
                delegationId = delegationId,
                validatorId = validatorId
            ).firstOrNull()
            val assetInfo = assetsRepository.getById(session.wallet, delegation?.base?.assetId ?: return@launch)
                .getOrNull()
                ?.firstOrNull()
            state.update { it.copy(walletType = session.wallet.type, delegation = delegation, assetInfo = assetInfo) }
        }
    }

    fun onStake(call: OnAmount) {
        call(
            AmountParams.buildStake(
                assetId = state.value.assetInfo?.asset?.id!!,
                txType = TransactionType.StakeDelegate,
                validatorId = state.value.delegation?.validator?.id,
                delegationId = state.value.delegation?.base?.delegationId!!
            )
        )
    }

    fun onUnstake(call: OnAmount) {
        call(
            AmountParams.buildStake(
                assetId = state.value.assetInfo?.asset?.id!!,
                txType = TransactionType.StakeUndelegate,
                validatorId = state.value.delegation?.validator?.id,
                delegationId = state.value.delegation?.base?.delegationId!!
            )
        )
    }

    fun onRedelegate(call: OnAmount) {
        call(
            AmountParams.buildStake(
                assetId = state.value.assetInfo?.asset?.id!!,
                txType = TransactionType.StakeRedelegate,
                validatorId = state.value.delegation?.validator?.id,
                delegationId = state.value.delegation?.base?.delegationId!!
            )
        )
    }

    fun onWithdraw(call: OnAmount) {
        call(
            AmountParams.buildStake(
                assetId = state.value.assetInfo?.asset?.id!!,
                txType = TransactionType.StakeWithdraw,
                validatorId = state.value.delegation?.validator?.id,
                delegationId = state.value.delegation?.base?.delegationId!!
            )
        )
    }

    private data class State(
        val walletType: WalletType = WalletType.view,
        val assetInfo: AssetInfo? = null,
        val delegation: Delegation? = null,
    ) {
        fun toUIState(): DelegationSceneState {
            if (assetInfo == null || delegation == null) {
                return DelegationSceneState.Loading
            }
            val stakeChain = StakeChain.byChain(assetInfo.asset.id.chain)!!
            val balances = listOf(
                CellEntity(
                    label = R.string.wallet_stake,
                    data = Crypto(delegation.base.balance).format(assetInfo.asset.decimals, assetInfo.asset.symbol, 6)
                ),
                CellEntity(
                    label = R.string.stake_rewards,
                    data = Crypto(delegation.base.rewards).format(assetInfo.asset.decimals, assetInfo.asset.symbol, 6)
                ),
            )
            return DelegationSceneState.Loaded(
                walletType = walletType,
                state = delegation.base.state,
                validator = delegation.validator,
                balances = balances,
                availableIn = availableIn(delegation),
                stakeChain = stakeChain,
            )
        }
    }
}