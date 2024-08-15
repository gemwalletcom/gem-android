package com.gemwallet.android.features.amount.views

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.gemwallet.android.R
import com.gemwallet.android.features.amount.model.AmountError
import com.gemwallet.android.features.confirm.models.AmountScreenModel
import com.gemwallet.android.features.stake.components.ValidatorItem
import com.gemwallet.android.interactors.getIconUrl
import com.gemwallet.android.ui.components.AmountField
import com.gemwallet.android.ui.components.Container
import com.gemwallet.android.ui.components.MainActionButton
import com.gemwallet.android.ui.components.Scene
import com.wallet.core.primitives.DelegationValidator
import com.wallet.core.primitives.TransactionType

@Composable
fun AmountScene(
    amount: String,
    uiModel: AmountScreenModel,
    validatorState: DelegationValidator?,
    inputError: AmountError,
    amountError: AmountError,
    equivalent: String,
    availableBalance: String,
    onNext: () -> Unit,
    onAmount: (String) -> Unit,
    onMaxAmount: () -> Unit,
    onCancel: () -> Unit,
    onValidator: () -> Unit,
) {
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
    Scene(
        title = stringResource(id = R.string.transfer_amount_title),
        onClose = onCancel,
        mainAction = {
            MainActionButton(
                title = if (amountError == AmountError.None) {
                    stringResource(id = R.string.common_continue)
                } else {
                    amountErrorString(amountError)
                },
                enabled = amountError == AmountError.None,
                onClick = onNext,
            )
        }
    ) {
        LazyColumn(
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            item {
                Spacer(modifier = Modifier.size(40.dp))
                AmountField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester),
                    amount = amount,
                    assetSymbol = uiModel.asset.symbol,
                    equivalent = equivalent,
                    error = amountErrorString(error = inputError),
                    onValueChange = onAmount,
                    onNext = onNext
                )
                if (validatorState != null) {
                    Container {
                        ValidatorItem(
                            data = validatorState,
                            inContainer = true,
                            onClick = when (uiModel.txType) {
                                TransactionType.StakeUndelegate -> null
                                else -> {
                                    { onValidator() }
                                }
                            }
                        )
                    }
                }
                AssetInfoCard(
                    assetId = uiModel.asset.id,
                    assetIcon = uiModel.asset.getIconUrl(),
                    assetTitle = uiModel.asset.name,
                    assetType = uiModel.asset.type,
                    availableAmount = availableBalance,
                    onMaxAmount = onMaxAmount
                )
            }
        }
    }
}

@Composable
fun amountErrorString(error: AmountError): String = when (error) {
    AmountError.None -> ""
    AmountError.IncorrectAmount -> stringResource(id = R.string.errors_unable_estimate_network_fee)
    AmountError.Required -> stringResource(id = R.string.common_required_field, stringResource(id = R.string.transfer_amount))
    AmountError.Unavailable -> "Unavailable"
    is AmountError.InsufficientBalance -> stringResource(id = R.string.transfer_insufficient_balance, error.assetName)
    is AmountError.InsufficientFeeBalance -> stringResource(id = R.string.transfer_insufficient_network_fee_balance, error.assetName)
    AmountError.ZeroAmount -> "Zero amount"
    is AmountError.MinimumValue -> stringResource(id = R.string.transfer_minimum_amount, error.minimumValue)
}