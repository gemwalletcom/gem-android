package com.gemwallet.features.transfer_amount.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gemwallet.android.math.parseNumber
import com.gemwallet.android.model.AmountParams
import com.gemwallet.android.model.AssetInfo
import com.gemwallet.android.model.ConfirmParams
import com.gemwallet.android.model.Crypto
import com.gemwallet.android.model.format
import com.gemwallet.android.ui.models.AmountInputType
import com.gemwallet.features.transfer_amount.models.AmountError
import com.wallet.core.primitives.Asset
import com.wallet.core.primitives.Currency
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.math.BigInteger
import java.math.MathContext

private const val paramsArg = "params"

@OptIn(ExperimentalCoroutinesApi::class)
abstract class AmountBaseViewModel(
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    val params = savedStateHandle
        .getStateFlow(paramsArg, "")
        .mapNotNull { AmountParams.unpack(it) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val txType = params.mapLatest { it?.txType }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    var amount by mutableStateOf("")
        private set

    open val inputTypeToggleable
        get() = true
    val amountInputType = MutableStateFlow(AmountInputType.Crypto)

    val amountError = MutableStateFlow<AmountError>(AmountError.None)

    abstract val assetInfo: StateFlow<AssetInfo?>

    val amountEquivalent: StateFlow<String>
        get() = combine(
            snapshotFlow { amount },
            amountInputType,
            assetInfo,
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

    internal val maxAmount = MutableStateFlow(false)

    val reserveForFee: StateFlow<BigInteger?> get() = MutableStateFlow(null)

    abstract val availableBalance: StateFlow<BigDecimal>
    abstract val availableBalanceFormatted: StateFlow<String>

    fun updateAmount(input: String, isMax: Boolean = false) {
        amount = input
        maxAmount.update { isMax }
    }

    abstract fun onMaxAmount()

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
                    is AmountError -> amountError.update { err }
                    else -> amountError.update { AmountError.Unknown(err.message ?: "Unknown error") }
                }
            }
        }
    }

    internal abstract fun onNext(
        params: AmountParams,
        rawAmount: String,
        onConfirm: (ConfirmParams) -> Unit
    )

    internal fun validateAmount(asset: Asset, amount: String, minValue: BigInteger) {
        if (amount.isEmpty()) {
            throw AmountError.Required
        }
        try {
            amount.parseNumber()
        } catch (_: Throwable) {
            throw AmountError.IncorrectAmount
        }
        val crypto = Crypto(amount.parseNumber(), asset.decimals)
        if (BigInteger.ZERO != minValue && crypto.atomicValue < minValue) {
            throw AmountError.MinimumValue(asset.format(Crypto(minValue), decimalPlace = 2))
        }
    }

    internal fun validateBalance(
        assetInfo: AssetInfo,
        amount: Crypto
    ) {
        if (amount.atomicValue == BigInteger.ZERO) {
            throw AmountError.ZeroAmount
        }
        if (amount.atomicValue.toBigDecimal() > availableBalance.value) {
            throw  AmountError.InsufficientBalance(assetInfo.asset.name)
        }
    }

    internal fun calcEquivalent(
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
                    val amount = inputAmount.parseNumber()
                    val decimals = asset.decimals
                    val unit = Crypto(amount, decimals).convert(decimals, price)
                    currency.format(unit.atomicValue)
                }
                AmountInputType.Fiat -> {
                    val value = inputAmount.parseNumber()
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
}