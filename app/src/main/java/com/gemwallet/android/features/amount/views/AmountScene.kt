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
import com.gemwallet.android.features.amount.model.AmountScreenState
import com.gemwallet.android.features.stake.components.ValidatorItem
import com.gemwallet.android.ui.components.AmountField
import com.gemwallet.android.ui.components.Container
import com.gemwallet.android.ui.components.MainActionButton
import com.gemwallet.android.ui.components.Scene
import com.wallet.core.primitives.TransactionType

@Composable
fun AmountScene(
    amount: String,
    uiState: AmountScreenState.Loaded,
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
                title = stringResource(id = R.string.common_continue),
                enabled = uiState.error == AmountError.None,
                loading = uiState.loading,
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
                    assetSymbol = uiState.assetSymbol,
                    equivalent = uiState.equivalent,
                    error = amountErrorString(error = uiState.error),
                    onValueChange = onAmount,
                    onNext = onNext
                )
                if (uiState.validator != null) {
                    Container {
                        ValidatorItem(
                            data = uiState.validator,
                            inContainer = true,
                            onClick = when (uiState.txType) {
                                TransactionType.StakeUndelegate -> null
                                else -> {
                                    { onValidator() }
                                }
                            }
                        )
                    }
                }
                AssetInfoCard(
                    assetId = uiState.assetId,
                    assetIcon = uiState.assetIcon,
                    assetTitle = uiState.assetTitle,
                    assetType = uiState.assetType,
                    availableAmount = uiState.availableAmount,
                    onMaxAmount = onMaxAmount
                )
            }
        }
    }
}

@Composable
fun amountErrorString(error: AmountError): String = when (error) {
    AmountError.None -> ""
    AmountError.IncorrectAmount -> stringResource(id = R.string.amount_error_invalid_amount)
    AmountError.Init -> "Init error"
    AmountError.Required -> stringResource(id = R.string.common_required_field, stringResource(id = R.string.transfer_amount))
    AmountError.Unavailable -> ""
    is AmountError.InsufficientBalance -> stringResource(id = R.string.transfer_insufficient_balance, error.assetName)
    AmountError.ZeroAmount -> ""
    is AmountError.MinimumValue -> stringResource(id = R.string.transfer_minimum_amount, error.minimumValue)
}