package com.gemwallet.android.features.amount.views

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.gemwallet.android.R
import com.gemwallet.android.blockchain.PayloadType
import com.gemwallet.android.blockchain.memo
import com.gemwallet.android.features.amount.components.amountErrorString
import com.gemwallet.android.features.amount.models.AmountError
import com.gemwallet.android.features.amount.models.QrScanField
import com.gemwallet.android.features.confirm.models.AmountScreenModel
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
    addressError: AmountError,
    memoError: AmountError,
    equivalent: String,
    availableBalance: String,
    onNext: () -> Unit,
    onQrScan: (QrScanField) -> Unit,
    onAmount: (String) -> Unit,
    onMaxAmount: () -> Unit,
    onCancel: () -> Unit,
    onValidator: () -> Unit,
) {
    val focusRequester = remember { FocusRequester() }
    val isKeyBoardOpen by keyboardAsState()
    val isSmallScreen = LocalConfiguration.current.screenHeightDp.dp < 680.dp

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
    Scene(
        title = when (uiModel.txType) {
            TransactionType.Transfer -> stringResource(id = R.string.transfer_send_title)
            TransactionType.StakeDelegate -> stringResource(id = R.string.transfer_stake_title)
            TransactionType.StakeUndelegate -> stringResource(id = R.string.transfer_unstake_title)
            TransactionType.StakeRedelegate -> stringResource(id = R.string.transfer_redelegate_title)
            else -> stringResource(id = R.string.transfer_amount_title)
        },
        onClose = onCancel,
        mainAction = {
            if (!isKeyBoardOpen || !isSmallScreen) {
                MainActionButton(
                    title = stringResource(id = R.string.common_next),
                    onClick = onNext,
                )
            }
        },
        actions = {
            TextButton(onClick = onNext,
                colors = ButtonDefaults.textButtonColors()
                    .copy(contentColor = MaterialTheme.colorScheme.primary)
            ) {
                Text(stringResource(R.string.common_next).uppercase())
            }
        }
    ) {
        LazyColumn(horizontalAlignment = Alignment.CenterHorizontally) {
            item {
                Spacer16()
                AmountField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester),
                    amount = amount,
                    assetSymbol = uiModel.asset.symbol,
                    equivalent = equivalent,
                    error = amountErrorString(error = if (AmountError.None == inputError) amountError else inputError),
                    onValueChange = onAmount,
                    onNext = onNext
                )
            }
            validatorView(uiModel, validatorState, onValidator)
            addressDestinationView(
                uiModel = uiModel,
                addressState = addressState,
                addressError = addressError,
                memoState = memoState,
                memoError = memoError,
                nameRecordState = nameRecordState,
                validatorState = validatorState,
                onQrScan = onQrScan,
            )
            item {
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

private fun LazyListScope.validatorView(
    uiModel: AmountScreenModel,
    validatorState: DelegationValidator?,
    onValidator: () -> Unit
) {
    validatorState ?: return
    item {
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
}

private fun LazyListScope.addressDestinationView(
    uiModel: AmountScreenModel,
    addressState: MutableState<String>,
    addressError: AmountError,
    memoState: MutableState<String>,
    memoError: AmountError,
    nameRecordState: MutableState<NameRecord?>,
    validatorState: DelegationValidator?,
    onQrScan: (QrScanField) -> Unit,
) {
    if (validatorState != null) return
    item {
        Column(modifier = Modifier.padding(padding16)) {
            AddressChainField(
                chain = uiModel.asset.chain(),
                value = addressState.value,
                label = stringResource(id = R.string.transfer_recipient_address_field),
                error = amountErrorString(error = addressError),
                onValueChange = { input, nameRecord ->
                    addressState.value = input
                    nameRecordState.value = nameRecord
                },
                onQrScanner = { onQrScan(QrScanField.Address) }
            )
            if (uiModel.asset.chain().memo() != PayloadType.None) {
                Spacer(modifier = Modifier.size(space4))
                MemoTextField(
                    value = memoState.value,
                    label = stringResource(id = R.string.transfer_memo),
                    onValueChange = { memoState.value = it },
                    error = memoError,
                    onQrScanner = { onQrScan(QrScanField.Address) },
                )
            }
        }
    }
}

@Composable
private fun keyboardAsState(): State<Boolean> {
    val isImeVisible = WindowInsets.ime.getBottom(LocalDensity.current) > 0
    return rememberUpdatedState(isImeVisible)
}