package com.gemwallet.android.features.amount.views

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.gemwallet.android.features.amount.components.amountErrorString
import com.gemwallet.android.features.amount.models.AmountError
import com.gemwallet.android.features.confirm.models.AmountScreenModel
import com.gemwallet.android.features.stake.components.ValidatorItem
import com.gemwallet.android.ui.R
import com.gemwallet.android.ui.components.AmountField
import com.gemwallet.android.ui.components.Container
import com.gemwallet.android.ui.components.buttons.MainActionButton
import com.gemwallet.android.ui.components.designsystem.Spacer16
import com.gemwallet.android.ui.components.keyboardAsState
import com.gemwallet.android.ui.components.screen.Scene
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
                    title = stringResource(id = R.string.common_continue),
                    onClick = onNext,
                )
            }
        },
        actions = {
            TextButton(onClick = onNext,
                colors = ButtonDefaults.textButtonColors()
                    .copy(contentColor = MaterialTheme.colorScheme.primary)
            ) {
                Text(stringResource(R.string.common_continue).uppercase())
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
            item {
                AssetInfoCard(
                    asset = uiModel.asset,
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