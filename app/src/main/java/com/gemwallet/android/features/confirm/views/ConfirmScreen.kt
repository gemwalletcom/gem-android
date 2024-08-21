package com.gemwallet.android.features.confirm.views

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gemwallet.android.R
import com.gemwallet.android.features.confirm.models.ConfirmError
import com.gemwallet.android.features.confirm.models.ConfirmState
import com.gemwallet.android.features.confirm.viewmodels.ConfirmViewModel
import com.gemwallet.android.model.ConfirmParams
import com.gemwallet.android.ui.components.AmountListHead
import com.gemwallet.android.ui.components.MainActionButton
import com.gemwallet.android.ui.components.Scene
import com.gemwallet.android.ui.components.SwapListHead
import com.gemwallet.android.ui.components.Table
import com.gemwallet.android.ui.components.titles.getTitle
import com.gemwallet.android.ui.theme.Spacer16
import com.gemwallet.android.ui.theme.Spacer8
import com.gemwallet.android.ui.theme.padding16
import com.gemwallet.android.ui.theme.trailingIcon20
import com.wallet.core.primitives.TransactionType

@Composable
fun ConfirmScreen(
    params: ConfirmParams? = null,
    onFinish: (String) -> Unit,
    onCancel: () -> Unit,
    viewModel: ConfirmViewModel = hiltViewModel(),
) {
    val amountModel by viewModel.amountUIModel.collectAsStateWithLifecycle()
    val txInfoUIModel by viewModel.txInfoUIModel.collectAsStateWithLifecycle()
    val feeModel by viewModel.feeUIModel.collectAsStateWithLifecycle()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val txSpeed by viewModel.txSpeed.collectAsStateWithLifecycle()
    val allFee by viewModel.allFee.collectAsStateWithLifecycle()
    val uiState = state

    var showSelectTxSpeed by remember { mutableStateOf(false) }

    DisposableEffect(params.hashCode()) {
        if (params != null) {
            viewModel.init(params)
        }

        onDispose { }
    }

    BackHandler(true) {
        onCancel()
    }

    if (uiState is ConfirmState.Result) {
        if (uiState.txHash.isNotEmpty() && uiState.error == null) {
            onFinish(uiState.txHash)
        }
    }

    Scene(
        title = stringResource(amountModel?.txType?.getTitle() ?: R.string.transfer_confirm),
        onClose = onCancel,
        mainAction = {
            MainActionButton(
                title = state.buttonLabel(),
                enabled = state !is ConfirmState.Prepare && state !is ConfirmState.Sending,
                loading = state is ConfirmState.Sending || state is ConfirmState.Prepare,
                onClick = viewModel::send,
            )
        }
    ) {
        if (amountModel?.txType == TransactionType.Swap) {
            SwapListHead(
                fromAsset = amountModel?.fromAsset,
                fromValue = amountModel?.fromAmount!!,
                toAsset = amountModel?.toAsset!!,
                toValue = amountModel?.toAmount!!,
                currency = amountModel?.currency,
            )
        } else {
            AmountListHead(
                amount = amountModel?.amount ?: "",
                equivalent = amountModel?.amountEquivalent,
            )
        }
        Table(txInfoUIModel)
        Spacer16()
        Table(
            if (allFee.size > 1) listOf(feeModel.firstOrNull()?.copy(action = { showSelectTxSpeed = true })) else feeModel
        )
        Spacer16()
        ConfirmErrorInfo(state)

        if (showSelectTxSpeed) {
            SelectTxSpeed(
                currentSpeed = txSpeed,
                fee = allFee,
                onSelect = {
                    showSelectTxSpeed = false
                    viewModel.changeTxSpeed(it)
                }
            ) { showSelectTxSpeed = false }
        }
    }
}

@Composable
private fun ConfirmErrorInfo(state: ConfirmState) {
    if (state !is ConfirmState.Error || state.message == ConfirmError.None) {
        return
    }
    Column(
        modifier = Modifier
            .padding(padding16)
            .background(
                MaterialTheme.colorScheme.errorContainer.copy(0.2f),
                shape = MaterialTheme.shapes.medium
            )
            .fillMaxWidth()
            .padding(padding16),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                modifier = Modifier.size(trailingIcon20),
                imageVector = Icons.Outlined.Warning,
                tint = MaterialTheme.colorScheme.error,
                contentDescription = ""
            )
            Spacer8()
            Text(
                text = stringResource(R.string.errors_error_occured),
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.W400),
            )
        }
        Text(
            text = when (state.message) {
                is ConfirmError.Init,
                is ConfirmError.SignFail,
                is ConfirmError.BroadcastError,
                is ConfirmError.TransactionIncorrect,
                is ConfirmError.CalculateFee -> stringResource(R.string.confirm_fee_error)
                is ConfirmError.InsufficientBalance -> stringResource(R.string.transfer_insufficient_network_fee_balance, state.message.chainTitle)
                is ConfirmError.InsufficientFee -> stringResource(R.string.transfer_insufficient_network_fee_balance, state.message.chainTitle)
                is ConfirmError.None -> stringResource(id = R.string.transfer_confirm)
            },
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@Composable
fun ConfirmState.buttonLabel(): String {
    return when (this) {
        is ConfirmState.Error -> stringResource(R.string.common_try_again)
        ConfirmState.FatalError -> ""
        ConfirmState.Prepare,
        ConfirmState.Ready,
        ConfirmState.Sending ->  stringResource(id = R.string.transfer_confirm)
        is ConfirmState.Result -> ""
    }
}