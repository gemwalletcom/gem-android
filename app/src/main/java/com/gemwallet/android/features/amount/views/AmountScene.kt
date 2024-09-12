package com.gemwallet.android.features.amount.views

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import com.gemwallet.android.R
import com.gemwallet.android.blockchain.PayloadType
import com.gemwallet.android.blockchain.memo
import com.gemwallet.android.features.amount.model.AmountError
import com.gemwallet.android.features.confirm.models.AmountScreenModel
import com.gemwallet.android.features.recipient.models.RecipientFormError
import com.gemwallet.android.features.recipient.views.MemoTextField
import com.gemwallet.android.features.recipient.views.recipientErrorString
import com.gemwallet.android.features.stake.components.ValidatorItem
import com.gemwallet.android.interactors.chain
import com.gemwallet.android.interactors.getIconUrl
import com.gemwallet.android.ui.components.AddressChainField
import com.gemwallet.android.ui.components.AmountField
import com.gemwallet.android.ui.components.Container
import com.gemwallet.android.ui.components.MainActionButton
import com.gemwallet.android.ui.components.Scene
import com.gemwallet.android.ui.theme.Spacer16
import com.gemwallet.android.ui.theme.padding16
import com.gemwallet.android.ui.theme.space4
import com.wallet.core.primitives.DelegationValidator
import com.wallet.core.primitives.NameRecord
import com.wallet.core.primitives.TransactionType

@Composable
fun AmountScene(
    amount: String,
    addressState: MutableState<String>,
    memoState: MutableState<String>,
    nameRecordState: MutableState<NameRecord?>,
    uiModel: AmountScreenModel,
    validatorState: DelegationValidator?,
    inputError: AmountError,
    amountError: AmountError,
    addressError: RecipientFormError,
    memoError: RecipientFormError,
    equivalent: String,
    availableBalance: String,
    onNext: () -> Unit,
    onScanAddress: () -> Unit,
    onScanMemo: () -> Unit,
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
        LazyColumn(horizontalAlignment = Alignment.CenterHorizontally) {
            item {
                Spacer16()
                AmountField(
                    modifier = Modifier.fillMaxWidth().focusRequester(focusRequester),
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
                } else {
                    Column(modifier = Modifier.padding(padding16)) {
                        AddressChainField(
                            chain = uiModel.asset.chain(),
                            value = addressState.value,
                            label = stringResource(id = R.string.transfer_recipient_address_field),
                            error = recipientErrorString(error = addressError),
                            onValueChange = { input, nameRecord ->
                                addressState.value = input
                                nameRecordState.value = nameRecord
                            },
                            onQrScanner = onScanAddress
                        )
                        if (uiModel.asset.chain().memo() != PayloadType.None) {
                            Spacer(modifier = Modifier.size(space4))
                            MemoTextField(
                                value = memoState.value,
                                label = stringResource(id = R.string.transfer_memo),
                                onValueChange = { memoState.value = it },
                                error = memoError,
                                onQrScanner = onScanMemo,
                            )
                        }
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