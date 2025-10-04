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
import com.gemwallet.android.ui.components.buttons.MainActionButton
import com.gemwallet.android.ui.components.fields.AmountField
import com.gemwallet.android.ui.components.keyboardAsState
import com.gemwallet.android.ui.components.list_item.ValidatorItem
import com.gemwallet.android.ui.components.list_item.property.PropertyAssetInfoItem
import com.gemwallet.android.ui.components.screen.Scene
import com.gemwallet.android.ui.models.AmountInputType
import com.gemwallet.android.ui.models.ListPosition
import com.gemwallet.android.ui.theme.Spacer16
import com.gemwallet.features.transfer_amount.viewmodels.models.AmountError
import com.wallet.core.primitives.Asset
import com.wallet.core.primitives.DelegationValidator
import com.wallet.core.primitives.TransactionType

@Composable
fun AmountScene(
    amount: String,
    amountPrefill: String?,
    amountInputType: AmountInputType,
    txType: TransactionType,
    asset: Asset,
    currency: com.wallet.core.primitives.Currency,
    validatorState: DelegationValidator?,
    error: AmountError,
    equivalent: String,
    availableBalance: String,
    onNext: () -> Unit,
    onInputAmount: (String) -> Unit,
    onInputTypeClick: () -> Unit,
    onMaxAmount: () -> Unit,
    onCancel: () -> Unit,
    onValidator: () -> Unit,
) {
    val focusRequester = remember { FocusRequester() }
    val isKeyBoardOpen by keyboardAsState()
    val isSmallScreen = LocalConfiguration.current.screenHeightDp.dp < 680.dp

    Scene(
        title = getTitle(txType),
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
                    assetSymbol = asset.symbol,
                    currency = currency,
                    inputType = amountInputType,
                    onInputTypeClick = onInputTypeClick,
                    equivalent = equivalent,
                    readOnly = !amountPrefill.isNullOrEmpty(),
                    error = amountErrorString(error = error),
                    onValueChange = onInputAmount,
                    onNext = onNext
                )
            }
            validatorView(txType, validatorState, onValidator)
            item {
                PropertyAssetInfoItem(
                    asset = asset,
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
    txType: TransactionType,
    validatorState: DelegationValidator?,
    onValidator: () -> Unit
) {
    validatorState ?: return
    item {
        ValidatorItem(
            data = validatorState,
            listPosition = ListPosition.Single,
            trailingIcon = {
                Icon(
                    Icons.Default.ChevronRight,
                    contentDescription = "select_validator",
                    tint = MaterialTheme.colorScheme.secondary
                )
            },
            onClick = when (txType) {
                TransactionType.StakeUndelegate -> null
                else -> {
                    { onValidator() }
                }
            }
        )
    }
}

@Composable
private fun getTitle(txType: TransactionType) = when (txType) {
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
    TransactionType.PerpetualOpenPosition -> stringResource(R.string.perpetual_position)
    TransactionType.PerpetualClosePosition -> stringResource(R.string.perpetual_close_position)
    TransactionType.StakeFreeze -> stringResource(R.string.transfer_freeze_title)
    TransactionType.StakeUnfreeze -> stringResource(R.string.transfer_unfreeze_title)
}