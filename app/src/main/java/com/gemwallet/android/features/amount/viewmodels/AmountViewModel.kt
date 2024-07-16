package com.gemwallet.android.features.amount.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gemwallet.android.data.asset.AssetsRepository
import com.gemwallet.android.data.repositories.session.SessionRepository
import com.gemwallet.android.data.stake.StakeRepository
import com.gemwallet.android.features.amount.model.AmountError
import com.gemwallet.android.features.amount.model.AmountParams
import com.gemwallet.android.features.amount.navigation.paramsArg
import com.gemwallet.android.features.confirm.models.AmountScreenModel
import com.gemwallet.android.features.recipient.models.InputCurrency
import com.gemwallet.android.math.numberParse
import com.gemwallet.android.model.AssetInfo
import com.gemwallet.android.model.ConfirmParams
import com.gemwallet.android.model.Crypto
import com.gemwallet.android.model.format
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
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.math.BigInteger
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class AmountViewModel @Inject constructor(
    private val sessionRepository: SessionRepository,
    private val assetsRepository: AssetsRepository,
    private val stakeRepository: StakeRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val params = savedStateHandle
        .getStateFlow(paramsArg, "")
        .mapNotNull { AmountParams.unpack(it) }

    private val asset: Flow<AssetInfo> = params.flatMapLatest { assetsRepository.getAssetInfo(it.assetId) }

    private val delegation: StateFlow<Delegation?> = params.flatMapMerge {
        if (it.validatorId != null
            && (it.txType == TransactionType.StakeUndelegate
                    || it.txType == TransactionType.StakeRedelegate
                    || it.txType == TransactionType.StakeWithdraw)) {
            stakeRepository.getDelegation(it.validatorId, it.delegationId ?: "")
        } else {
            emptyFlow()
        }
    }
    .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    private val srcValidator = params.combine(delegation) { params, delegation ->
        when (params.txType) {
            TransactionType.StakeWithdraw,
            TransactionType.StakeUndelegate -> delegation?.validator
            TransactionType.StakeDelegate,
            TransactionType.StakeRedelegate -> stakeRepository.getRecommended(params.assetId.chain)
            else -> null
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, null)
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
            TransactionType.StakeDelegate -> state.assetInfo.balances.available()
            TransactionType.StakeRewards -> Crypto(BigInteger(delegation?.base?.rewards ?: "0"))
            TransactionType.StakeUndelegate,
            TransactionType.StakeRedelegate,
            TransactionType.StakeWithdraw -> Crypto(BigInteger(delegation?.base?.balance ?: "0"))
            null -> Crypto(BigInteger.ZERO)
        }
    }
    .map { state.value?.assetInfo?.asset?.format(it, 8) ?: "" }
    .stateIn(viewModelScope, SharingStarted.Eagerly, "")

    var amount by mutableStateOf("")
        private set

    val equivalentState = snapshotFlow { amount }.combine(asset) { amount, assetInfo ->
        calcEquivalent(amount, InputCurrency.InCrypto, assetInfo)
    }
    .stateIn(viewModelScope, SharingStarted.Eagerly, "")

    val inputErrorState = MutableStateFlow<AmountError>(AmountError.None)
    val nextErrorState = MutableStateFlow<AmountError>(AmountError.None)

    private val maxAmount = MutableStateFlow(false)

    fun setDelegatorValidator(validatorId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val assetId = state.value?.assetInfo?.asset?.id ?: return@launch
            val validator = stakeRepository.getStakeValidator(assetId, validatorId)
                ?: stakeRepository.getRecommended(assetId.chain)
            selectedValidator.update { validator }
        }
    }

    fun updateAmount(input: String, isMax: Boolean = false) {
        amount = input
        inputErrorState.update { AmountError.None }
        nextErrorState.update { AmountError.None }
        maxAmount.update { isMax }
    }

    fun onMaxAmount() = viewModelScope.launch {
        val asset = state.value?.assetInfo ?: return@launch
        val balance = when (delegation.value) {
            null -> asset.balances.available()
            else -> Crypto(delegation.value?.base?.balance?.toBigIntegerOrNull() ?: BigInteger.ZERO)
        }

        updateAmount(balance.value(asset.asset.decimals).stripTrailingZeros().toPlainString(), true)
    }

    fun onNext(onConfirm: (ConfirmParams) -> Unit) = viewModelScope.launch {
        val state = state.value ?: return@launch
        val params = state.params
        val inputCurrency = InputCurrency.InCrypto
        val validator = validatorState.value
        val delegation = delegation.value

        val asset = state.assetInfo.asset
        val decimals = asset.decimals
        val price = state.assetInfo.price?.price?.price ?: 0.0

        val minimumValue = getMinAmount(params.txType, asset.id.chain)
        val inputError = validateAmount(asset, amount, inputCurrency, price, minimumValue)
        if (inputError != AmountError.None) {
            inputErrorState.update { inputError }
            return@launch
        }
        val amount = inputCurrency.getAmount(amount, decimals, price)
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
        inputErrorState.update { AmountError.None }
        nextErrorState.update { AmountError.None }
        val builder = ConfirmParams.Builder(asset.id, amount.atomicValue, params.memo)
        val nextParams = when (params.txType) {
            TransactionType.Transfer -> builder.transfer(
                params.destinationAddress ?: return@launch,
                params.addressDomain,
                maxAmount.value,
            )
            TransactionType.StakeDelegate -> builder.delegate(validator?.id!!)
            TransactionType.StakeUndelegate -> builder.undelegate(delegation!!) // TODO: ???
            TransactionType.StakeRewards -> builder.rewards(
                stakeRepository.getRewards(asset.id, state.assetInfo.owner.address).map { it.validator.id }
            )
            TransactionType.StakeRedelegate -> builder.redelegate(validator?.id!!, delegation!!)
            TransactionType.StakeWithdraw -> builder.withdraw(delegation!!)
            TransactionType.Swap,
            TransactionType.TokenApproval -> throw IllegalArgumentException()
        }
        onConfirm(nextParams)
    }

    private fun calcEquivalent(inputAmount: String, inputCurrency: InputCurrency, assetInfo: AssetInfo): String {
        val currency = sessionRepository.getSession()?.currency ?: return ""
        val price = assetInfo.price?.price?.price ?: return ""
        val decimals = assetInfo.asset.decimals

        val amount = if (validateAmount(assetInfo.asset, inputAmount, inputCurrency, price, BigInteger.ZERO) == AmountError.None) {
            inputAmount
        } else {
            "0"
        }

        return if (inputCurrency == InputCurrency.InFiat) {
            val unit = inputCurrency.getAmount(amount, decimals = decimals, price)
            unit.format(0, currency.string, decimalPlace = 2, dynamicPlace = true, zeroFraction = 0)
        } else {
            val unit = Crypto(amount.numberParse(), decimals).convert(decimals, price)
            unit.format(0, currency.string, decimalPlace = 2, dynamicPlace = true, zeroFraction = 0)
        }
    }

    private fun validateAmount(asset: Asset, amount: String, inputCurrency: InputCurrency, price: Double, minValue: BigInteger): AmountError {
        if (amount.isEmpty()) {
            return AmountError.Required
        }
        try {
            amount.numberParse()
        } catch (err: Throwable) {
            return AmountError.IncorrectAmount
        }
        if (inputCurrency == InputCurrency.InFiat && price <= 0.0) {
            return AmountError.Unavailable
        }
        val crypto = Crypto(amount.numberParse(), asset.decimals)
        if (BigInteger.ZERO != minValue && crypto.atomicValue < minValue) {
            return AmountError.MinimumValue(Crypto(minValue).format(asset.decimals, asset.symbol, decimalPlace = 2))
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
            TransactionType.StakeRewards -> assetInfo.balances.available()
            TransactionType.StakeUndelegate,
            TransactionType.StakeRedelegate,
            TransactionType.StakeWithdraw -> Crypto(BigInteger(delegation?.base?.balance ?: "0"))
        }
        if (amount.atomicValue > availableAmount.atomicValue) {
            return AmountError.InsufficientBalance(assetInfo.asset.name)
        }
        return AmountError.None
    }

    private fun getMinAmount(txType: TransactionType, chain: Chain): BigInteger {
        return when (txType) {
            TransactionType.StakeRedelegate,
            TransactionType.StakeDelegate -> BigInteger.valueOf(uniffi.Gemstone.Config().getStakeConfig(chain.string).minAmount.toLong())
            else -> BigInteger.ZERO
        }
    }

    private data class State(
        val assetInfo: AssetInfo,
        val params: AmountParams,
    )
}