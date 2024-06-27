package com.gemwallet.android.features.amount.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gemwallet.android.data.asset.AssetsRepository
import com.gemwallet.android.data.session.SessionRepository
import com.gemwallet.android.data.stake.StakeRepository
import com.gemwallet.android.features.amount.model.AmountError
import com.gemwallet.android.features.amount.model.AmountScreenState
import com.gemwallet.android.features.recipient.models.InputCurrency
import com.gemwallet.android.interactors.getIconUrl
import com.gemwallet.android.math.numberParse
import com.gemwallet.android.model.AssetInfo
import com.gemwallet.android.model.ConfirmParams
import com.gemwallet.android.model.Crypto
import com.wallet.core.primitives.Asset
import com.wallet.core.primitives.AssetId
import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.Currency
import com.wallet.core.primitives.Delegation
import com.wallet.core.primitives.DelegationValidator
import com.wallet.core.primitives.TransactionType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.math.BigInteger
import javax.inject.Inject

@HiltViewModel
class AmountViewModel @Inject constructor(
    private val sessionRepository: SessionRepository,
    private val assetsRepository: AssetsRepository,
    private val stakeRepository: StakeRepository,
) : ViewModel() {

    private val state = MutableStateFlow(State())
    val uiState = state.map { it.toUIState() }
        .stateIn(viewModelScope, SharingStarted.Eagerly, AmountScreenState.Loading)
    var amount by mutableStateOf("")
        private set

    internal fun init(
        assetId: AssetId,
        destinationAddress: String,
        addressDomain: String,
        memo: String,
        delegationId: String,
        validatorId: String,
        txType: TransactionType,
    ) = viewModelScope.launch(Dispatchers.IO) {
        val session = sessionRepository.getSession() ?: return@launch
        val wallet = session.wallet
        val asset = withContext(Dispatchers.IO) { assetsRepository.getById(wallet, assetId) }
            .getOrNull()?.firstOrNull()

        if (asset == null) {
            state.update { State(fatalError = AmountError.Init) }
            return@launch
        }
        val delegation = getDelegation(txType, validatorId, delegationId)
        val validator = when (txType) {
            TransactionType.StakeWithdraw,
            TransactionType.StakeUndelegate -> delegation?.validator
            TransactionType.StakeDelegate,
            TransactionType.StakeRedelegate -> stakeRepository.getRecommended(assetId.chain)
            else -> null
        }

        state.update {
            State(
                fiatCurrency = session.currency,
                loading = false,
                assetInfo = asset,
                destinationAddress = destinationAddress,
                addressDomain = addressDomain,
                memo = memo,
                amount = null,
                txType = txType,
                equivalent = " ",
                validator = validator,
                delegation = delegation,
                rewardValidators = stakeRepository.getRewards(asset.asset.id, asset.owner.address).map { it.validator.id },
                srcValidatorId = delegation?.validator?.id,
            )
        }
    }

    fun onNext(onConfirm: (ConfirmParams) -> Unit) {
        val currentState = state.value.copy()
        val assetInfo = currentState.assetInfo
        val inputCurrency = InputCurrency.InCrypto
        val maxAmount = currentState.maxAmount

        if (assetInfo == null) {
            state.update { it.copy(fatalError = AmountError.Init) }
            return
        }
        val asset = assetInfo.asset
        val decimals = asset.decimals
        val price = assetInfo.price?.price ?: 0.0

        val minimumValue = getMinAmount(currentState.txType, asset.id.chain)
        val inputError = validateAmount(asset, amount, inputCurrency, price, minimumValue)
        if (inputError != AmountError.None) {
            state.update { currentState.copy(inputError = inputError) }
            return
        }

        val amount = inputCurrency.getAmount(amount, decimals, price)

        val balanceError = validateBalance(
            assetInfo = assetInfo,
            txType = currentState.txType,
            delegation = currentState.delegation,
            amount = amount
        )
        if (balanceError != AmountError.None) {
            state.update { it.copy(inputError = balanceError) }
            return
        }
        state.update {
            currentState.copy(
                loading = false,
                inputError = AmountError.None,
            )
        }
        val params = when (currentState.txType) {
            TransactionType.Transfer -> ConfirmParams.TransferParams(
                assetId = asset.id,
                amount = amount.atomicValue,
                domainName = currentState.addressDomain,
                to = currentState.destinationAddress,
                isMaxAmount = maxAmount,
                memo = currentState.memo,
            )
            TransactionType.Swap,
            TransactionType.TokenApproval -> throw IllegalArgumentException()
            TransactionType.StakeDelegate -> ConfirmParams.DelegateParams(
                assetId = asset.id,
                amount = amount.atomicValue,
                validatorId = currentState.validator?.id!!
            )
            TransactionType.StakeUndelegate -> ConfirmParams.UndelegateParams(
                assetId = asset.id,
                amount = amount.atomicValue,
                validatorId = currentState.validator?.id!!,
                delegationId = currentState.delegation?.base?.delegationId!!,
                share = currentState.delegation.base.shares,
                balance = currentState.delegation.base.balance,
            )
            TransactionType.StakeRewards -> ConfirmParams.RewardsParams(
                assetId = asset.id,
                validatorsId = currentState.rewardValidators,
            )
            TransactionType.StakeRedelegate -> ConfirmParams.RedeleateParams(
                assetId = asset.id,
                amount = amount.atomicValue,
                srcValidatorId = currentState.srcValidatorId!!,
                dstValidatorId = currentState.validator?.id!!,
                share = currentState.delegation?.base?.shares!!,
                balance = currentState.delegation.base.balance,
            )
            TransactionType.StakeWithdraw -> ConfirmParams.WithdrawParams(
                assetId = asset.id,
                amount = amount.atomicValue,
                validatorId = currentState.validator?.id!!,
                delegationId = currentState.delegation?.base?.delegationId!!,
            )
        }
        onConfirm(params)
    }

    fun updateAmount(input: String, maxAmount: Boolean = false) {
        amount = input
        state.update {
            it.copy(
                equivalent = calcEquivalent(input, InputCurrency.InCrypto),
                inputError = AmountError.None,
                maxAmount = maxAmount,
            )
        }
    }

    fun onMaxAmount() {
        val asset = state.value.assetInfo ?: return
        val balance = when (state.value.delegation) {
            null -> state.value.assetInfo?.balances?.available() ?: Crypto(BigInteger.ZERO)
            else -> Crypto(state.value.delegation?.base?.balance?.toBigIntegerOrNull() ?: BigInteger.ZERO)
        }

        updateAmount(balance.value(asset.asset.decimals).stripTrailingZeros().toPlainString(), true)
    }

    private fun calcEquivalent(inputAmount: String, inputCurrency: InputCurrency): String {
        val currency = sessionRepository.getSession()?.currency ?: return ""
        val assetInfo = state.value.assetInfo ?: return ""
        val price = assetInfo.price?.price ?: return ""
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

    fun updateValidator(validatorId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val assetInfo = state.value.assetInfo!!
            val validator = stakeRepository.getStakeValidator(assetInfo.asset.id, validatorId)
                ?: stakeRepository.getRecommended(assetInfo.asset.id.chain)
            state.update {
                it.copy(validator = validator)
            }
        }
    }

    private suspend fun getDelegation(txType: TransactionType, validatorId: String, delegationId: String) = when (txType) {
        TransactionType.StakeUndelegate,
        TransactionType.StakeRedelegate,
        TransactionType.StakeWithdraw -> stakeRepository.getDelegation(validatorId, delegationId)
        else -> null
    }

    private fun getMinAmount(txType: TransactionType, chain: Chain): BigInteger {
        return when (txType) {
            TransactionType.StakeRedelegate,
            TransactionType.StakeDelegate -> BigInteger.valueOf(uniffi.Gemstone.Config().getStakeConfig(chain.string).minAmount.toLong())
            else -> BigInteger.ZERO
        }
    }

    private data class State(
        val fiatCurrency: Currency = Currency.USD,
        val loading: Boolean = false,

        val assetInfo: AssetInfo? = null,

        val destinationAddress: String = "",
        val addressDomain: String = "",
        val memo: String = "",

        val inputCurrency: InputCurrency = InputCurrency.InCrypto,
        val amount: Crypto? = null,
        val maxAmount: Boolean = false,
        val equivalent: String = "",

        val txType: TransactionType = TransactionType.Transfer,

        val inputError: AmountError = AmountError.None,
        val fatalError: AmountError = AmountError.None,


        val validator: DelegationValidator? = null,
        val srcValidatorId: String? = null,
        val delegation: Delegation? = null,
        val rewardValidators: List<String> = emptyList(),
    ) {

        fun toUIState(): AmountScreenState {
            val asset = assetInfo?.asset ?: return AmountScreenState.Fatal//(fatalError = AmountFormError.Init)
            return AmountScreenState.Loaded(
                loading = loading,
                txType = txType,
                assetId = asset.id,
                assetIcon = asset.getIconUrl(),
                assetTitle = asset.name,
                assetSymbol = asset.symbol,
                assetType = asset.type,
                equivalent = equivalent,
                availableAmount = when (txType) {
                    TransactionType.Transfer,
                    TransactionType.Swap,
                    TransactionType.TokenApproval,
                    TransactionType.StakeDelegate,
                    TransactionType.StakeRewards -> assetInfo.balances.available()
                    TransactionType.StakeUndelegate,
                    TransactionType.StakeRedelegate,
                    TransactionType.StakeWithdraw -> Crypto(BigInteger(delegation?.base?.balance ?: "0"))
                }.format(asset.decimals, asset.symbol, 8),
                inputCurrency = inputCurrency,
                error = inputError,
                validator = validator,
            )
        }
    }
}