package com.gemwallet.features.transfer_amount.presents.views

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
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
import com.gemwallet.android.ui.R
import com.gemwallet.android.ui.components.Container
import com.gemwallet.android.ui.components.buttons.MainActionButton
import com.gemwallet.android.ui.components.designsystem.Spacer16
import com.gemwallet.android.ui.components.fields.AmountField
import com.gemwallet.android.ui.components.keyboardAsState
import com.gemwallet.android.ui.components.list_item.ValidatorItem
import com.gemwallet.android.ui.components.screen.Scene
import com.gemwallet.features.transfer_amount.presents.components.amountErrorString
import com.gemwallet.features.transfer_amount.viewmodels.models.AmountError
import com.gemwallet.features.transfer_amount.viewmodels.models.AmountScreenModel
import com.wallet.core.primitives.DelegationValidator
import com.wallet.core.primitives.TransactionType

@Composable
fun AmountScene(
    amount: String,
    amountPrefill: String?,
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

    Scene(
        title = when (uiModel.txType) {
            TransactionType.Transfer -> stringResource(id = R.string.transfer_send_title)
            TransactionType.StakeDelegate -> stringResource(id = R.string.transfer_stake_title)
            TransactionType.StakeUndelegate -> stringResource(id = R.string.transfer_unstake_title)
            TransactionType.StakeRedelegate -> stringResource(id = R.string.transfer_redelegate_title)
            TransactionType.StakeWithdraw -> stringResource(id = R.string.transfer_withdraw_title)
            TransactionType.TransferNFT -> stringResource(id = R.string.nft_collection)
            TransactionType.Swap -> stringResource(id = R.string.wallet_swap)
            TransactionType.TokenApproval -> stringResource(id = R.string.transfer_approve_title)
            TransactionType.StakeRewards -> stringResource(id = R.string.transfer_rewards_title)
            TransactionType.AssetActivation -> stringResource(id = R.string.transfer_activate_asset_title)
            TransactionType.SmartContractCall -> stringResource(id = R.string.transfer_amount_title)
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
                    modifier = Modifier.fillMaxWidth().focusRequester(focusRequester),
                    amount = amountPrefill ?: amount,
                    assetSymbol = uiModel.asset.symbol,
                    equivalent = equivalent,
                    readOnly = !amountPrefill.isNullOrEmpty(),
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

    LaunchedEffect(Unit) {
        try {
            focusRequester.requestFocus()
        } catch (_: Throwable) {}
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
                trailingIcon = {
                    Icon(
                        Icons.Default.ChevronRight,
                        contentDescription = "select_validator",
                        tint = MaterialTheme.colorScheme.secondary
                    )
                },
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