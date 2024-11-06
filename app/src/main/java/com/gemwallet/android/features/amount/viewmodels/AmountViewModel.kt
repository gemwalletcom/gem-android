package com.gemwallet.android.features.amount.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gemwallet.android.blockchain.PayloadType
import com.gemwallet.android.blockchain.memo
import com.gemwallet.android.blockchain.operators.ValidateAddressOperator
import com.gemwallet.android.data.repositoreis.assets.AssetsRepository
import com.gemwallet.android.data.repositoreis.session.SessionRepository
import com.gemwallet.android.data.repositoreis.stake.StakeRepository
import com.gemwallet.android.ext.mutableStateIn
import com.gemwallet.android.features.amount.models.AmountError
import com.gemwallet.android.features.amount.models.AmountParams
import com.gemwallet.android.features.amount.models.InputCurrency
import com.gemwallet.android.features.amount.models.QrScanField
import com.gemwallet.android.features.amount.navigation.paramsArg
import com.gemwallet.android.features.confirm.models.AmountScreenModel
import com.gemwallet.android.interactors.chain
import com.gemwallet.android.math.numberParse
import com.gemwallet.android.model.AssetInfo
import com.gemwallet.android.model.ConfirmParams
import com.gemwallet.android.model.Crypto
import com.gemwallet.android.model.DestinationAddress
import com.gemwallet.android.model.format
import com.wallet.core.primitives.Asset
import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.Delegation
import com.wallet.core.primitives.DelegationValidator
import com.wallet.core.primitives.NameRecord
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
import uniffi.gemstone.Config
import java.math.BigInteger
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class AmountViewModel @Inject constructor(
    private val sessionRepository: SessionRepository,
    private val assetsRepository: AssetsRepository,
    private val stakeRepository: StakeRepository,
    private val validateAddressOperator: ValidateAddressOperator,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val addressState = mutableStateOf("")
    val memoState = mutableStateOf("")
    val nameRecordState = mutableStateOf<NameRecord?>(null)

    private val params = savedStateHandle
        .getStateFlow(paramsArg, "")
        .mapNotNull { AmountParams.unpack(it) }

    private val asset: Flow<AssetInfo> = params.flatMapLatest {
        addressState.value = it.destination?.address ?: ""
        memoState.value = it.memo ?: ""
        assetsRepository.getAssetInfo(it.assetId)
    }

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
            TransactionType.StakeDelegate -> Crypto(state.assetInfo.balance.balance.available)
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

    val addressError = combine(
        state,
        snapshotFlow { addressState.value },
        snapshotFlow { nameRecordState.value },
    ) { state, address, nameRecord ->
        val nameAddress = nameRecord?.address
        if (state == null || (address.isEmpty() && nameAddress.isNullOrEmpty())) return@combine AmountError.None
        val destination = DestinationAddress(nameAddress ?: address, nameRecord?.name)
        val validation = validateDestination(state.assetInfo.asset.chain(), state.params.txType, destination)
        validation
    }.mutableStateIn(viewModelScope, AmountError.None)

    val memoErrorState = MutableStateFlow<AmountError>(AmountError.None)
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
            null -> Crypto(asset.balance.balance.available)
            else -> Crypto(delegation.value?.base?.balance?.toBigIntegerOrNull() ?: BigInteger.ZERO)
        }

        updateAmount(balance.value(asset.asset.decimals).stripTrailingZeros().toPlainString(), true)
    }

    fun onNext(onConfirm: (ConfirmParams) -> Unit) = viewModelScope.launch {
        val params = state.value?.params?.copy(
            destination = DestinationAddress(
                address = nameRecordState.value?.address ?: addressState.value,
                domainName = nameRecordState.value?.name,
            ),
            memo = memoState.value,
        ) ?: return@launch

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
        val addressError = validateDestination(asset.chain(), params.txType, destination)
        if (addressError != AmountError.None) {
            this@AmountViewModel.addressError.update { addressError }
            return@launch
        }
        inputErrorState.update { AmountError.None }
        nextErrorState.update { AmountError.None }
        this@AmountViewModel.addressError.update { AmountError.None }
        val builder = ConfirmParams.Builder(asset.id, amount.atomicValue)
        val nextParams = when (params.txType) {
            TransactionType.Transfer -> builder.transfer(
                destination = destination!!,
                memo = memo,
                isMax = maxAmount.value,
            )
            TransactionType.StakeDelegate -> builder.delegate(validator?.id!!)
            TransactionType.StakeUndelegate -> builder.undelegate(delegation!!)
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

    private fun validateDestination(
        chain: Chain,
        txType: TransactionType,
        destination: DestinationAddress?
    ): AmountError {
        if (txType != TransactionType.Transfer) {
            return AmountError.None
        }
        return if (validateAddressOperator(destination?.address ?: "", chain).getOrNull() != true) {
            AmountError.IncorrectAddress
        } else {
            AmountError.None
        }
    }

    private fun calcEquivalent(inputAmount: String, inputCurrency: InputCurrency, assetInfo: AssetInfo): String {
        val currency = sessionRepository.getSession()?.currency ?: return ""
        val price = assetInfo.price?.price?.price ?: return ""
        val decimals = assetInfo.asset.decimals

        val amount = if (validateAmount(assetInfo.asset, inputAmount, BigInteger.ZERO) == AmountError.None) {
            inputAmount
        } else {
            return " "
        }

        return if (inputCurrency == InputCurrency.InFiat) {
//            val unit = inputCurrency.getAmount(amount, decimals = decimals, price)
//            unit.format(0, currency.string, decimalPlace = 2, dynamicPlace = true, zeroFraction = 0)
            ""
        } else {
            val unit = Crypto(amount.numberParse(), decimals).convert(decimals, price)
            currency.format(unit.atomicValue)
        }
    }

    private fun validateAmount(asset: Asset, amount: String, minValue: BigInteger): AmountError {
        if (amount.isEmpty()) {
            return AmountError.Required
        }
        try {
            amount.numberParse()
        } catch (err: Throwable) {
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

    fun setQrData(type: QrScanField, data: String, onConfirm: (ConfirmParams) -> Unit) {
        val paymentWrapper = uniffi.gemstone.paymentDecodeUrl(data)
        val amount = paymentWrapper.amount
        val address = paymentWrapper.address
        val memo = paymentWrapper.memo

        if (
            address.isNotEmpty()
            && !amount.isNullOrEmpty()
            && (state.value?.assetInfo?.asset?.chain()?.memo() == PayloadType.None || !memo.isNullOrEmpty())
        ) {
            val assetId = state.value?.assetInfo?.asset?.id ?: return
            val params = AmountParams.buildTransfer(assetId, DestinationAddress(address), memo ?: "")
            onNext(params, amount, onConfirm)
            return
        }

        when (type) {
            QrScanField.None -> {}
            QrScanField.Address -> {
                addressState.value = address.ifEmpty { data }
                memoState.value = memo?.ifEmpty { memoState.value } ?: memoState.value
            }
            QrScanField.Memo -> {
                addressState.value = address.ifEmpty { addressState.value }
                memoState.value = paymentWrapper.memo ?: data
            }
        }
        this.amount = amount ?: this.amount
    }

    private data class State(
        val assetInfo: AssetInfo,
        val params: AmountParams,
    )
}