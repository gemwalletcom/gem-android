package com.gemwallet.features.earn.delegation.viewmodels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gemwallet.android.data.repositoreis.assets.AssetsRepository
import com.gemwallet.android.data.repositoreis.session.SessionRepository
import com.gemwallet.android.data.repositoreis.stake.StakeRepository
import com.gemwallet.android.ext.byChain
import com.gemwallet.android.ext.redelegated
import com.gemwallet.android.model.AmountParams
import com.gemwallet.android.model.ConfirmParams
import com.gemwallet.android.model.Crypto
import com.gemwallet.android.model.format
import com.gemwallet.android.ui.components.list_item.availableIn
import com.gemwallet.android.ui.models.actions.AmountTransactionAction
import com.gemwallet.android.ui.models.actions.ConfirmTransactionAction
import com.gemwallet.features.earn.delegation.models.DelegationActions
import com.gemwallet.features.earn.delegation.models.DelegationBalances
import com.gemwallet.features.earn.delegation.models.DelegationProperty
import com.wallet.core.primitives.DelegationState
import com.wallet.core.primitives.StakeChain
import com.wallet.core.primitives.TransactionType
import com.wallet.core.primitives.WalletType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import java.math.BigInteger
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class DelegationViewModel @Inject constructor(
    private val assetsRepository: AssetsRepository,
    private val stakeRepository: StakeRepository,
    sessionRepository: SessionRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    val validatorId = savedStateHandle.getStateFlow<String?>("validatorId", null).filterNotNull()
    val delegationId = savedStateHandle.getStateFlow<String?>("delegationId", null).filterNotNull()

    val delegation = combine(validatorId, delegationId) { validatorId, delegationId -> Pair(validatorId, delegationId) }
        .flatMapLatest {
            val (validatorId, delegationId) = it
            stakeRepository.getDelegation(delegationId = delegationId, validatorId = validatorId)
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val assetInfo = delegation.filterNotNull()
        .flatMapLatest { assetsRepository.getAssetInfo(it.base.assetId) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val properties = combine(
        delegation,
        assetInfo,
    ) { delegation, assetInfo ->
        if (delegation == null || assetInfo == null) {
            return@combine emptyList()
        }
        val availableIn = availableIn(delegation)
        listOfNotNull(
            DelegationProperty.Name(delegation.validator.name),
            DelegationProperty.Apr(delegation.validator),
            DelegationProperty.TransactionStatus(delegation.base.state, delegation.validator.isActive),
            delegation.base.state.takeIf { (it == DelegationState.Pending
                        || it == DelegationState.Activating
                        || it == DelegationState.Deactivating)
                && availableIn.isNotEmpty() }?.let { DelegationProperty.State(it, availableIn) }
        )
    }
    .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val balances = combine(
        delegation,
        assetInfo,
    ) { delegation, assetInfo ->
        if (delegation == null || assetInfo == null) {
            return@combine emptyList()
        }

        listOfNotNull(
            DelegationBalances.Stake(assetInfo.asset.format(Crypto(delegation.base.balance))),
            DelegationBalances.Rewards(assetInfo.asset.format(Crypto(delegation.base.rewards)))
        )
    }
    .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val actions = combine(
        delegation,
        assetInfo,
        sessionRepository.session().filterNotNull(),
    ) { delegation, assetInfo, session ->
        if (delegation == null || assetInfo == null || session.wallet.type == WalletType.view) {
            return@combine emptyList()
        }
        val stakeChain = StakeChain.byChain(assetInfo.asset.id.chain)!!
        if (delegation.base.state == DelegationState.Active) {
            listOfNotNull(
                DelegationActions.StakeAction,
                DelegationActions.UnstakeAction,
                stakeChain.takeIf { it.redelegated() }?.let { DelegationActions.RedelegateAction }
            )
        } else {
            listOf(
                DelegationActions.WithdrawalAction
            )
        }
    }
    .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val uiState = combine(
        delegation,
        assetInfo,
        sessionRepository.session().filterNotNull(),
    ) { delegation, assetInfo, session ->
        if (assetInfo == null || delegation == null) {
            return@combine null
        }
        val stakeChain = StakeChain.byChain(assetInfo.asset.id.chain)!!
        DelegationSceneState(
            walletType = session.wallet.type,
            state = delegation.base.state,
            validator = delegation.validator,
            stakeBalance = assetInfo.asset.format(Crypto(delegation.base.balance)),
            rewardsBalance = assetInfo.asset.format(Crypto(delegation.base.rewards)),
            availableIn = availableIn(delegation),
            stakeChain = stakeChain,
        )
    }
    .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    fun onStake(call: AmountTransactionAction) {
        call(buildStake(TransactionType.StakeDelegate))
    }

    fun onUnstake(call: AmountTransactionAction) {
        call(buildStake(TransactionType.StakeUndelegate))
    }

    fun onRedelegate(call: AmountTransactionAction) {
        call(buildStake(TransactionType.StakeRedelegate))
    }

    fun onWithdraw(call: ConfirmTransactionAction) {
        val assetInfo = assetInfo.value ?: return
        val from = assetInfo.owner ?: return
        val delegation = delegation.value ?: return
        val balance = Crypto(delegation.base.balance.toBigIntegerOrNull() ?: BigInteger.ZERO)
        val params = ConfirmParams.Builder(assetInfo.asset, from, balance.atomicValue)
            .withdraw(delegation)
        call(params)
    }

    private fun buildStake(type: TransactionType): AmountParams {
        return AmountParams.buildStake(
            assetId = assetInfo.value?.asset?.id!!,
            txType = type,
            validatorId = delegation.value?.validator?.id,
            delegationId = delegation.value?.base?.delegationId!!
        )
    }
}