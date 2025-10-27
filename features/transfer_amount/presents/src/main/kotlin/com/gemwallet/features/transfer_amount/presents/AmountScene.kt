package com.gemwallet.features.transfer_amount.presents

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
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
import com.gemwallet.android.ui.R
import com.gemwallet.android.ui.components.buttons.MainActionButton
import com.gemwallet.android.ui.components.fields.AmountField
import com.gemwallet.android.ui.components.keyboardAsState
import com.gemwallet.android.ui.components.list_item.property.PropertyAssetInfoItem
import com.gemwallet.android.ui.components.screen.Scene
import com.gemwallet.android.ui.models.AmountInputType
import com.gemwallet.android.ui.theme.Spacer16
import com.gemwallet.features.transfer_amount.models.AmountError
import com.gemwallet.features.transfer_amount.presents.components.amountErrorString
import com.gemwallet.features.transfer_amount.presents.components.transactionTypeTitle
import com.gemwallet.features.transfer_amount.presents.components.resourceSelect
import com.gemwallet.features.transfer_amount.presents.components.validatorView
import com.wallet.core.primitives.Asset
import com.wallet.core.primitives.Currency
import com.wallet.core.primitives.DelegationValidator
import com.wallet.core.primitives.Resource
import com.wallet.core.primitives.TransactionType

@Composable
fun AmountScene(
    amount: String,
    amountPrefill: String?,
    amountInputType: AmountInputType,
    txType: TransactionType,
    asset: Asset,
    currency: Currency,
    validatorState: DelegationValidator?,
    resource: Resource,
    error: AmountError,
    equivalent: String,
    availableBalance: String,
    onNext: () -> Unit,
    onInputAmount: (String) -> Unit,
    onInputTypeClick: () -> Unit,
    onMaxAmount: () -> Unit,
    onCancel: () -> Unit,
    onResourceSelect: (Resource) -> Unit,
    onValidator: () -> Unit,
) {
    val focusRequester = remember { FocusRequester() }
    val isKeyBoardOpen by keyboardAsState()
    val isSmallScreen = LocalConfiguration.current.screenHeightDp.dp < 680.dp

    Scene(
        title = transactionTypeTitle(txType),
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
            resourceSelect(txType, resource, onResourceSelect)
        }
    }

    LaunchedEffect(Unit) {
        try {
            focusRequester.requestFocus()
        } catch (_: Throwable) {}
    }
}