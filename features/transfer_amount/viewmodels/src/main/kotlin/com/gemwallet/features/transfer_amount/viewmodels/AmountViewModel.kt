package com.gemwallet.features.transfer_amount.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gemwallet.android.data.repositoreis.assets.AssetsRepository
import com.gemwallet.android.data.repositoreis.session.SessionRepository
import com.gemwallet.android.data.repositoreis.stake.StakeRepository
import com.gemwallet.android.math.numberParse
import com.gemwallet.android.model.AmountParams
import com.gemwallet.android.model.AssetInfo
import com.gemwallet.android.model.ConfirmParams
import com.gemwallet.android.model.Crypto
import com.gemwallet.android.model.format
import com.gemwallet.features.transfer_amount.viewmodels.models.AmountError
import com.gemwallet.features.transfer_amount.viewmodels.models.AmountScreenModel
import com.gemwallet.features.transfer_amount.viewmodels.models.InputCurrency
import com.wallet.core.primitives.Asset
import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.Delegation
import com.wallet.core.primitives.DelegationValidator
import com.wallet.core.primitives.TransactionType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import uniffi.gemstone.Config
import java.math.BigInteger
import javax.inject.Inject

private const val paramsArg = "params"

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class AmountViewModel @Inject constructor(
    private val sessionRepository: SessionRepository,
    private val assetsRepository: AssetsRepository,
    private val stakeRepository: StakeRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    var amount by mutableStateOf("")
        private set

    val inputErrorState = MutableStateFlow<AmountError>(AmountError.None)
    val nextErrorState = MutableStateFlow<AmountError>(AmountError.None)

    private val maxAmount = MutableStateFlow(false)

    private val params = savedStateHandle
        .getStateFlow(paramsArg, "")
        .mapNotNull { AmountParams.unpack(it) }

    private val asset: Flow<AssetInfo> = params.flatMapLatest {
        assetsRepository.getAssetInfo(it.assetId).mapNotNull { it }
    }
    .flowOn(Dispatchers.IO)

    private val delegation: StateFlow<Delegation?> = params.flatMapMerge {
        if (it.validatorId != null
            && (it.txType == TransactionType.StakeUndelegate
                    || it.txType == TransactionType.StakeRedelegate
                    || it.txType == TransactionType.StakeWithdraw)) {
            stakeRepository.getDelegation(it.validatorId!!, it.delegationId ?: "")
        } else {
            emptyFlow()
        }
    }
    .flowOn(Dispatchers.IO)
    .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    private val recommendedValidator = params
        .flatMapLatest { params -> stakeRepository.getRecommended(params.assetId.chain) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    private val srcValidator = combine(params, delegation, recommendedValidator) { params, delegation, recommended ->
        when (params.txType) {
            TransactionType.StakeWithdraw,
            TransactionType.StakeUndelegate -> delegation?.validator
            TransactionType.StakeDelegate,
            TransactionType.StakeRedelegate -> recommended
            else -> null
        }
    }
    .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    private val selectedValidator = MutableStateFlow<DelegationValidator?>(null)

    val validatorState = selectedValidator.combine(srcValidator) { selected, src ->
        selected ?: src
    }.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    private val state: StateFlow<State?> = params.combine(asset) { params, asset ->
        State(assetInfo = asset, params = params)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val uiModel: StateFlow<AmountScreenModel?> = state.map { state ->
        state ?: return@map null
        AmountScreenModel(
            txType = state.params.txType,
            asset = state.assetInfo.asset,
        )
    }
    .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val availableBalance = state.combine(delegation) { state, delegation ->
        when (state?.params?.txType) {
            TransactionType.Transfer,
            TransactionType.Swap,
            TransactionType.TokenApproval,
            TransactionType.StakeDelegate -> Crypto(state.assetInfo.balance.balance.available)
            TransactionType.StakeRewards -> Crypto(BigInteger(delegation?.base?.rewards ?: "0"))
            TransactionType.StakeUndelegate,
            TransactionType.StakeRedelegate,
            TransactionType.StakeWithdraw -> Crypto(BigInteger(delegation?.base?.balance ?: "0"))
            TransactionType.AssetActivation,
            TransactionType.TransferNFT,
            TransactionType.SmartContractCall,
            null -> Crypto(BigInteger.ZERO)
        }
    }
    .map { state.value?.assetInfo?.asset?.format(it, 8) ?: "" }
    .stateIn(viewModelScope, SharingStarted.Eagerly, "")

    var prefillAmount = combine(
        state,
        delegation
    ) { state, delegation ->
        when (state?.params?.txType) {
            TransactionType.StakeUndelegate,
            TransactionType.StakeRedelegate,
            TransactionType.StakeWithdraw -> {
                val balance = Crypto(delegation?.base?.balance?.toBigIntegerOrNull() ?: BigInteger.ZERO)
                val value = balance.value(state.assetInfo.asset.decimals).stripTrailingZeros().toPlainString()
                amount = value
                value
            }
            else -> null
        }
    }
    .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val equivalentState = snapshotFlow { amount }.combine(asset) { amount, assetInfo ->
        calcEquivalent(amount, assetInfo)
    }
    .stateIn(viewModelScope, SharingStarted.Eagerly, "")

    fun setDelegatorValidator(validatorId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val assetId = state.value?.assetInfo?.asset?.id ?: return@launch
            val validator = stakeRepository.getStakeValidator(assetId, validatorId)
                ?: stakeRepository.getRecommended(assetId.chain).firstOrNull()
            selectedValidator.update { validator }
        }
    }

    fun updateAmount(input: String, isMax: Boolean = false) {
        amount = input
        maxAmount.update { isMax }
    }

    fun onMaxAmount() = viewModelScope.launch {
        val asset = state.value?.assetInfo ?: return@launch
        val balance = when (delegation.value) {
            null -> Crypto(asset.balance.balance.available)
            else -> Crypto(delegation.value?.base?.balance?.toBigIntegerOrNull() ?: BigInteger.ZERO)
        }

        updateAmount(balance.value(asset.asset.decimals).stripTrailingZeros().toPlainString(), true)
    }

    fun onNext(onConfirm: (ConfirmParams) -> Unit) = viewModelScope.launch {
        val params = state.value?.params ?: return@launch
        onNext(params, amount, onConfirm)
    }

    private fun onNext(
        params: AmountParams,
        rawAmount: String,
        onConfirm: (ConfirmParams) -> Unit
    ) = viewModelScope.launch {
        val state = state.value ?: return@launch
        val inputCurrency = InputCurrency.InCrypto
        val validator = validatorState.value
        val delegation = delegation.value

        val asset = state.assetInfo.asset
        val decimals = asset.decimals
        val price = state.assetInfo.price?.price?.price ?: 0.0

        val minimumValue = getMinAmount(params.txType, asset.id.chain)
        val inputError = validateAmount(asset, rawAmount, minimumValue)
        if (inputError != AmountError.None) {
            inputErrorState.update { inputError }
            return@launch
        }
        val amount = inputCurrency.getAmount(rawAmount, decimals, price)
        val balanceError = validateBalance(
            assetInfo = state.assetInfo,
            txType = params.txType,
            delegation = delegation,
            amount = amount
        )
        if (balanceError != AmountError.None) {
            nextErrorState.update { balanceError }
            return@launch
        }
        val destination = params.destination
        val memo = params.memo
        inputErrorState.update { AmountError.None }
        nextErrorState.update { AmountError.None }
        val builder = ConfirmParams.Builder(asset, state.assetInfo.owner!!, amount.atomicValue)
        val nextParams = when (params.txType) {
            TransactionType.Transfer -> builder.transfer(
                destination = destination!!,
                memo = memo,
                isMax = maxAmount.value,
            )
            TransactionType.StakeDelegate -> builder.delegate(validator?.id ?: return@launch) // TODO: Add error showing
            TransactionType.StakeUndelegate -> builder.undelegate(delegation ?: return@launch)  // TODO: Add error showing
            TransactionType.StakeRewards -> builder.rewards(
                stakeRepository.getRewards(asset.id, state.assetInfo.owner!!.address).map { it.validator.id }
            )
            TransactionType.StakeRedelegate -> builder.redelegate(validator?.id!!, delegation!!)
            TransactionType.StakeWithdraw -> builder.withdraw(delegation!!)
            TransactionType.Swap,
            TransactionType.AssetActivation -> builder.activate()
            TransactionType.TransferNFT -> TODO()
            TransactionType.SmartContractCall -> TODO()
            TransactionType.TokenApproval -> throw IllegalArgumentException()
        }
        onConfirm(nextParams)
    }

    private fun calcEquivalent(inputAmount: String, assetInfo: AssetInfo): String {
        val currency = sessionRepository.getSession()?.currency ?: return ""
        val price = assetInfo.price?.price?.price ?: return ""
        val decimals = assetInfo.asset.decimals

        val amount = if (validateAmount(assetInfo.asset, inputAmount, BigInteger.ZERO) == AmountError.None) {
            inputAmount
        } else {
            return " "
        }
        val unit = Crypto(amount.numberParse(), decimals).convert(decimals, price)
        return currency.format(unit.atomicValue)
    }

    private fun validateAmount(asset: Asset, amount: String, minValue: BigInteger): AmountError {
        if (amount.isEmpty()) {
            return AmountError.Required
        }
        try {
            amount.numberParse()
        } catch (_: Throwable) {
            return AmountError.IncorrectAmount
        }
        val crypto = Crypto(amount.numberParse(), asset.decimals)
        if (BigInteger.ZERO != minValue && crypto.atomicValue < minValue) {
            return AmountError.MinimumValue(asset.format(Crypto(minValue), decimalPlace = 2))
        }
        return AmountError.None
    }

    private fun validateBalance(
        assetInfo: AssetInfo,
        txType: TransactionType,
        delegation: Delegation?,
        amount: Crypto
    ): AmountError {
        if (amount.atomicValue == BigInteger.ZERO) {
            return AmountError.ZeroAmount
        }
        val availableAmount = when (txType) {
            TransactionType.Transfer,
            TransactionType.Swap,
            TransactionType.TokenApproval,
            TransactionType.StakeDelegate,
            TransactionType.StakeRewards -> Crypto(assetInfo.balance.balance.available)
            TransactionType.StakeUndelegate,
            TransactionType.StakeRedelegate,
            TransactionType.StakeWithdraw -> Crypto(BigInteger(delegation?.base?.balance ?: "0"))
            TransactionType.AssetActivation -> TODO()
            TransactionType.TransferNFT -> TODO()
            TransactionType.SmartContractCall -> TODO()
        }
        if (amount.atomicValue > availableAmount.atomicValue) {
            return AmountError.InsufficientBalance(assetInfo.asset.name)
        }
        return AmountError.None
    }

    private fun getMinAmount(txType: TransactionType, chain: Chain): BigInteger {
        return when (txType) {
            TransactionType.StakeRedelegate,
            TransactionType.StakeDelegate -> BigInteger.valueOf(
                Config().getStakeConfig(chain.string).minAmount.toLong()
            )
            else -> BigInteger.ZERO
        }
    }

    private data class State(
        val assetInfo: AssetInfo,
        val params: AmountParams,
    )
}