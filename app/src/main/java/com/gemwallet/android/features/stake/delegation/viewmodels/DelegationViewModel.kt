package com.gemwallet.android.features.stake.delegation.viewmodels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gemwallet.android.data.repositoreis.assets.AssetsRepository
import com.gemwallet.android.data.repositoreis.session.SessionRepository
import com.gemwallet.android.data.repositoreis.stake.StakeRepository
import com.gemwallet.android.ext.byChain
import com.gemwallet.android.features.stake.delegation.model.DelegationSceneState
import com.gemwallet.android.features.stake.model.availableIn
import com.gemwallet.android.model.AmountParams
import com.gemwallet.android.model.Crypto
import com.gemwallet.android.model.format
import com.gemwallet.android.ui.R
import com.gemwallet.android.ui.components.CellEntity
import com.gemwallet.android.ui.models.actions.AmountTransactionAction
import com.wallet.core.primitives.StakeChain
import com.wallet.core.primitives.TransactionType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

internal const val validatorIdArg = "validatorId"
internal const val delegationIdArg = "delegationId"

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class DelegationViewModel @Inject constructor(
    private val assetsRepository: AssetsRepository,
    private val stakeRepository: StakeRepository,
    sessionRepository: SessionRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    val validatorId = savedStateHandle.getStateFlow<String?>(validatorIdArg, null).filterNotNull()
    val delegationId = savedStateHandle.getStateFlow<String?>(delegationIdArg, null).filterNotNull()

    val delegation = combine(validatorId, delegationId) { validatorId, delegationId -> Pair(validatorId, delegationId) }
        .flatMapLatest {
            val (validatorId, delegationId) = it
            stakeRepository.getDelegation(delegationId = delegationId, validatorId = validatorId)
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val assetInfo = delegation.filterNotNull()
        .flatMapLatest { assetsRepository.getAssetInfo(it.base.assetId) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

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

    fun onWithdraw(call: AmountTransactionAction) {
        call(buildStake(TransactionType.StakeWithdraw))
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