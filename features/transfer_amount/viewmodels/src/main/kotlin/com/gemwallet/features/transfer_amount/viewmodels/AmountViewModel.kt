package com.gemwallet.features.transfer_amount.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gemwallet.android.data.repositoreis.assets.AssetsRepository
import com.gemwallet.android.data.repositoreis.stake.StakeRepository
import com.gemwallet.android.math.numberParse
import com.gemwallet.android.model.AmountParams
import com.gemwallet.android.model.AssetInfo
import com.gemwallet.android.model.ConfirmParams
import com.gemwallet.android.model.Crypto
import com.gemwallet.android.model.format
import com.gemwallet.android.ui.models.AmountInputType
import com.gemwallet.features.transfer_amount.viewmodels.models.AmountError
import com.wallet.core.primitives.Asset
import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.Currency
import com.wallet.core.primitives.Delegation
import com.wallet.core.primitives.TransactionType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import uniffi.gemstone.Config
import java.math.BigInteger
import java.math.MathContext
import javax.inject.Inject

private const val paramsArg = "params"

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class AmountViewModel @Inject constructor(
    private val assetsRepository: AssetsRepository,
    private val stakeRepository: StakeRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val params = savedStateHandle
        .getStateFlow(paramsArg, "")
        .mapNotNull { AmountParams.unpack(it) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    var amount by mutableStateOf("")
        private set

    var amountInputType = MutableStateFlow(AmountInputType.Crypto)

    val errorUIState = MutableStateFlow<AmountError>(AmountError.None)

    private val maxAmount = MutableStateFlow(false)

    val assetInfo = params.flatMapLatest {
        it?.assetId?.let { assetId -> assetsRepository.getAssetInfo(assetId).filterNotNull() } ?: emptyFlow()
    }
    .flowOn(Dispatchers.IO)
    .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    private val delegation: StateFlow<Delegation?> = params.flatMapMerge {
        if (it?.validatorId != null
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
        .flatMapLatest { params ->
            params?.assetId?.chain?.let { stakeRepository.getRecommended(params.assetId.chain) } ?: emptyFlow()
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    private val srcValidator = combine(params, delegation, recommendedValidator) { params, delegation, recommended ->
        when (params?.txType) {
            TransactionType.StakeWithdraw,
            TransactionType.StakeUndelegate -> delegation?.validator
            TransactionType.StakeDelegate,
            TransactionType.StakeRedelegate -> recommended
            else -> null
        }
    }
    .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    private val selectedValidatorId = MutableStateFlow<String?>(null)
    private val selectedValidator = combine(assetInfo, selectedValidatorId) { assetInfo, validatorId ->
        val assetId = assetInfo?.asset?.id ?: return@combine null
        validatorId ?: return@combine null

        stakeRepository.getStakeValidator(assetId, validatorId)
            ?: stakeRepository.getRecommended(assetId.chain).firstOrNull()
    }
    .flowOn(Dispatchers.IO)
    .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val validatorState = selectedValidator.combine(srcValidator) { selected, src ->
        selected ?: src
    }.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val availableBalance = combine(params, delegation, assetInfo) { params, delegation, assetInfo ->
        assetInfo ?: return@combine ""
        val value = when (params?.txType) {
            TransactionType.Transfer,
            TransactionType.Swap,
            TransactionType.StakeDelegate -> Crypto(assetInfo.balance.balance.available)
            TransactionType.StakeRewards -> Crypto(BigInteger(delegation?.base?.rewards ?: "0"))
            TransactionType.StakeUndelegate,
            TransactionType.StakeRedelegate,
            TransactionType.StakeWithdraw -> Crypto(BigInteger(delegation?.base?.balance ?: "0"))
            TransactionType.AssetActivation,
            TransactionType.TransferNFT,
            TransactionType.SmartContractCall,
            TransactionType.TokenApproval,
            null -> Crypto(BigInteger.ZERO)
        }
        assetInfo.asset.format(value, 8)
    }
    .stateIn(viewModelScope, SharingStarted.Eagerly, "")

    var prefillAmount = combine(
        params,
        assetInfo,
        delegation
    ) { params, assetInfo, delegation ->
        params ?: return@combine null
        assetInfo ?: return@combine null

        when (params.txType) {
            TransactionType.StakeUndelegate,
            TransactionType.StakeRedelegate,
            TransactionType.StakeWithdraw -> {
                val balance = Crypto(delegation?.base?.balance?.toBigIntegerOrNull() ?: BigInteger.ZERO)
                val value = balance.value(assetInfo.asset.decimals).stripTrailingZeros().toPlainString()
                amount = value
                value
            }
            else -> null
        }
    }
    .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val equivalentState = combine(
        snapshotFlow { amount },
        amountInputType,
        assetInfo
    ) { input, direction, assetInfo ->
        val priceInfo = assetInfo?.price ?: return@combine ""
        calcEquivalent(
            inputAmount = input,
            inputDirection = direction,
            asset = assetInfo.asset,
            price = priceInfo.price.price,
            currency = priceInfo.currency
        )
    }
    .stateIn(viewModelScope, SharingStarted.Eagerly, "")

    fun setDelegatorValidator(validatorId: String?) {
        selectedValidatorId.update { validatorId }
    }

    fun updateAmount(input: String, isMax: Boolean = false) {
        amount = input
        maxAmount.update { isMax }
    }

    fun onMaxAmount() = viewModelScope.launch {
        val assetInfo = this@AmountViewModel.assetInfo.value ?: return@launch
        val balance = when (delegation.value) {
            null -> Crypto(assetInfo.balance.balance.available)
            else -> Crypto(delegation.value?.base?.balance?.toBigIntegerOrNull() ?: BigInteger.ZERO)
        }

        updateAmount(balance.value(assetInfo.asset.decimals).stripTrailingZeros().toPlainString(), true)
    }

    fun switchInputType() {
        amountInputType.update {
            when (it) {
                AmountInputType.Crypto -> AmountInputType.Fiat
                AmountInputType.Fiat -> AmountInputType.Crypto
            }
        }
        amount = ""
    }

    fun onNext(onConfirm: (ConfirmParams) -> Unit) {
        val params = params.value ?: return
        viewModelScope.launch {
            try {
                onNext(params, amount, onConfirm)
            } catch (err: Throwable) {
                when (err) {
                    is AmountError -> errorUIState.update { err }
                    else -> errorUIState.update { AmountError.Unknown(err.message ?: "Unknown error") }
                }
            }
        }
    }

    private suspend fun onNext(
        params: AmountParams,
        rawAmount: String,
        onConfirm: (ConfirmParams) -> Unit
    ) {
        val assetInfo = assetInfo.value
        val owner = assetInfo?.owner ?: return
        val validator = validatorState.value
        val delegation = delegation.value
        val asset = assetInfo.asset
        val decimals = asset.decimals
        val price = assetInfo.price?.price?.price ?: 0.0
        val destination = params.destination
        val memo = params.memo
        val inputType = amountInputType.value

        val minimumValue = getMinAmount(params.txType, asset.id.chain)
        validateAmount(asset, rawAmount, minimumValue)

        val amount = inputType.getAmount(rawAmount, decimals, price)
        validateBalance(assetInfo, params.txType, delegation, amount)

        errorUIState.update { AmountError.None }

        val builder = ConfirmParams.Builder(asset, owner, amount.atomicValue)
        val nextParams = when (params.txType) {
            TransactionType.Transfer -> builder.transfer(destination!!, memo, maxAmount.value,)
            TransactionType.StakeDelegate -> builder.delegate(validator?.id ?: return)
            TransactionType.StakeUndelegate -> builder.undelegate(delegation ?: return)
            TransactionType.StakeRewards -> {
                val validators = stakeRepository.getRewards(asset.id, owner.address)
                    .map { it.validator.id }
                builder.rewards(validators)
            }
            TransactionType.StakeRedelegate -> builder.redelegate(validator?.id!!, delegation!!)
            TransactionType.StakeWithdraw -> builder.withdraw(delegation!!)
            TransactionType.AssetActivation -> builder.activate()
            TransactionType.Swap,
            TransactionType.TransferNFT,
            TransactionType.SmartContractCall,
            TransactionType.TokenApproval -> throw IllegalArgumentException()
        }
        onConfirm(nextParams)
    }

    private fun calcEquivalent(
        inputAmount: String,
        inputDirection: AmountInputType,
        asset: Asset,
        price: Double,
        currency: Currency
    ): String {
        return try {
            when (inputDirection) {
                AmountInputType.Crypto -> {
                    validateAmount(asset, inputAmount, BigInteger.ZERO)
                    val amount = inputAmount.numberParse()
                    val decimals = asset.decimals
                    val unit = Crypto(amount, decimals).convert(decimals, price)
                    currency.format(unit.atomicValue)
                }
                AmountInputType.Fiat -> {
                    val value = inputAmount.numberParse()
                    val crypto = value.divide(price.toBigDecimal(), MathContext.DECIMAL128)
                    validateAmount(asset, crypto.toString(), BigInteger.ZERO)
                    asset.format(crypto, dynamicPlace = true)
                }
            }
        } catch (_: Throwable) {
            when (inputDirection) {
                AmountInputType.Crypto -> {
                    currency.format(0.0)
                }
                AmountInputType.Fiat -> {
                    asset.format(Crypto(BigInteger.ZERO), dynamicPlace = true)
                }
            }
        }
    }

    private fun validateAmount(asset: Asset, amount: String, minValue: BigInteger) {
        if (amount.isEmpty()) {
            throw AmountError.Required
        }
        try {
            amount.numberParse()
        } catch (_: Throwable) {
            throw AmountError.IncorrectAmount
        }
        val crypto = Crypto(amount.numberParse(), asset.decimals)
        if (BigInteger.ZERO != minValue && crypto.atomicValue < minValue) {
            throw AmountError.MinimumValue(asset.format(Crypto(minValue), decimalPlace = 2))
        }
    }

    private fun validateBalance(
        assetInfo: AssetInfo,
        txType: TransactionType,
        delegation: Delegation?,
        amount: Crypto
    ) {
        if (amount.atomicValue == BigInteger.ZERO) {
            throw AmountError.ZeroAmount
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
            TransactionType.AssetActivation,
            TransactionType.TransferNFT,
            TransactionType.SmartContractCall -> throw IllegalArgumentException()
        }
        if (amount.atomicValue > availableAmount.atomicValue) {
            throw  AmountError.InsufficientBalance(assetInfo.asset.name)
        }
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
}