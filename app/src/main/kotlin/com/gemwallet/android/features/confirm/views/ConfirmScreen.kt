package com.gemwallet.android.features.confirm.views

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gemwallet.android.ext.asset
import com.gemwallet.android.features.confirm.models.ConfirmError
import com.gemwallet.android.features.confirm.models.ConfirmState
import com.gemwallet.android.features.confirm.viewmodels.ConfirmViewModel
import com.gemwallet.android.model.ConfirmParams
import com.gemwallet.android.ui.R
import com.gemwallet.android.ui.components.list_head.AmountListHead
import com.gemwallet.android.ui.components.InfoBottomSheet
import com.gemwallet.android.ui.components.InfoSheetEntity
import com.gemwallet.android.ui.components.list_head.NftHead
import com.gemwallet.android.ui.components.list_head.SwapListHead
import com.gemwallet.android.ui.components.Table
import com.gemwallet.android.ui.components.buttons.MainActionButton
import com.gemwallet.android.ui.components.designsystem.Spacer16
import com.gemwallet.android.ui.components.designsystem.Spacer4
import com.gemwallet.android.ui.components.designsystem.Spacer8
import com.gemwallet.android.ui.components.designsystem.padding16
import com.gemwallet.android.ui.components.designsystem.trailingIconMedium
import com.gemwallet.android.ui.components.list_item.getTitle
import com.gemwallet.android.ui.components.screen.Scene
import com.gemwallet.android.ui.models.actions.AssetIdAction
import com.gemwallet.android.ui.models.actions.CancelAction
import com.gemwallet.android.ui.models.actions.FinishConfirmAction
import com.wallet.core.primitives.TransactionType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfirmScreen(
    params: ConfirmParams? = null,
    finishAction: FinishConfirmAction,
    cancelAction: CancelAction,
    onBuy: AssetIdAction,
    viewModel: ConfirmViewModel = hiltViewModel(),
) {
    val amountModel by viewModel.amountUIModel.collectAsStateWithLifecycle()
    val txInfoUIModel by viewModel.txInfoUIModel.collectAsStateWithLifecycle()
    val feeModel by viewModel.feeUIModel.collectAsStateWithLifecycle()
    val feeValue by viewModel.feeValue.collectAsStateWithLifecycle()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val feePriority by viewModel.feePriority.collectAsStateWithLifecycle()
    val allFee by viewModel.allFee.collectAsStateWithLifecycle()

    var showSelectTxSpeed by remember { mutableStateOf(false) }
    var isShowedBroadcastError by remember((state as? ConfirmState.BroadcastError)?.message) {
        mutableStateOf(state is ConfirmState.BroadcastError)
    }
    var isShowBottomSheetInfo by remember(state as? ConfirmState.Error) {
        mutableStateOf((state as? ConfirmState.Error)?.message is ConfirmError.InsufficientFee )
    }

    DisposableEffect(params.hashCode()) {
        if (params != null) {
            viewModel.init(params)
        }

        onDispose { }
    }

    BackHandler(true) {
        cancelAction()
    }

    Scene(
        title = stringResource(amountModel?.txType?.getTitle() ?: R.string.transfer_title),
        onClose = { cancelAction() },
        mainAction = {
            MainActionButton(
                title = state.buttonLabel(),
                enabled = state !is ConfirmState.Prepare && state !is ConfirmState.Sending,
                loading = state is ConfirmState.Sending || state is ConfirmState.Prepare,
                onClick = { viewModel.send(finishAction) },
            )
        }
    ) {
        when (amountModel?.txType) {
            TransactionType.Swap -> SwapListHead(
                fromAsset = amountModel?.fromAsset,
                fromValue = amountModel?.fromAmount!!,
                toAsset = amountModel?.toAsset!!,
                toValue = amountModel?.toAmount!!,
                currency = amountModel?.currency,
            )
            TransactionType.TransferNFT -> amountModel?.nftAsset?.let { NftHead(it) }
            else -> AmountListHead(
                amount = amountModel?.amount ?: "",
                equivalent = amountModel?.amountEquivalent,
            )
        }
        Table(txInfoUIModel)
        Table(
            if (allFee.size > 1) {
                listOf(feeModel.firstOrNull()?.copy(action = { showSelectTxSpeed = true }))
            } else {
                feeModel
            }
        )
        Spacer16()
        ConfirmErrorInfo(state, feeValue = feeValue, isShowBottomSheetInfo, onBuy)

        if (showSelectTxSpeed) {
            SelectFeePriority(
                currentPriority = feePriority,
                fee = allFee,
                onSelect = {
                    showSelectTxSpeed = false
                    viewModel.changeFeePriority(it)
                },
            ) { showSelectTxSpeed = false }
        }
    }

    if (isShowedBroadcastError) {
        AlertDialog(
            onDismissRequest = { isShowedBroadcastError = false },
            confirmButton = {
                Button({ isShowedBroadcastError = false }) { Text(stringResource(R.string.common_done)) }
            },
            title = {
                Text(stringResource(R.string.errors_transfer_error))
            },
            text = {
                Text((state as? ConfirmState.BroadcastError)?.message?.toLabel() ?: "Unknown error")
            }
        )
    }
}

@Composable
private fun ConfirmErrorInfo(state: ConfirmState, feeValue: String, isShowBottomSheetInfo: Boolean, onBuy: AssetIdAction) {
    if (state !is ConfirmState.Error || state.message == ConfirmError.None) {
        return
    }
    val infoSheetEntity = when (state.message) {
        is ConfirmError.InsufficientFee -> InfoSheetEntity.NetworkBalanceRequiredInfo(
            chain = state.message.chain,
            value = feeValue,
            actionLabel = stringResource(R.string.asset_buy_asset, state.message.chain.asset().symbol),
            action = { onBuy(state.message.chain.asset().id) },
        )
        is ConfirmError.BroadcastError,
        is ConfirmError.Init,
        is ConfirmError.InsufficientBalance,
        
        ConfirmError.None,
        is ConfirmError.PreloadError,
        ConfirmError.RecipientEmpty,
        is ConfirmError.SignFail,
        ConfirmError.TransactionIncorrect -> null
    }
    var isShowInfoSheet by remember(isShowBottomSheetInfo) { mutableStateOf(isShowBottomSheetInfo) }
    Column(
        modifier = Modifier
            .padding(padding16)
            .background(
                MaterialTheme.colorScheme.errorContainer.copy(0.2f),
                shape = MaterialTheme.shapes.medium
            )
            .fillMaxWidth()
            .padding(padding16)
            .clickable(
                enabled = infoSheetEntity != null,
                onClick = { isShowInfoSheet = true }
            ),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                modifier = Modifier.size(trailingIconMedium),
                imageVector = Icons.Outlined.Warning,
                tint = MaterialTheme.colorScheme.error,
                contentDescription = ""
            )
            Spacer8()
            Text(
                text = stringResource(R.string.errors_error_occured),
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.W500),
            )
        }
        Spacer4()
        Row {
            infoSheetEntity?.let {
                Icon(
                    modifier = Modifier
                        .clip(RoundedCornerShape(percent = 50))
                        .size(trailingIconMedium)
                        .clickable(onClick = { isShowInfoSheet = true }),
                    imageVector = Icons.Outlined.Info,
                    contentDescription = "",
                    tint = MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f),
                )
                Spacer8()
            }
            Text(
                text = state.message.toLabel(),
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
    if (isShowInfoSheet) {
        InfoBottomSheet(item = infoSheetEntity) { isShowInfoSheet = false }
    }
}

@Composable
fun ConfirmState.buttonLabel(): String {
    return when (this) {
        is ConfirmState.BroadcastError,
        is ConfirmState.Error -> stringResource(R.string.common_try_again)
        ConfirmState.FatalError -> ""
        ConfirmState.Prepare,
        ConfirmState.Ready,
        ConfirmState.Sending ->  stringResource(id = R.string.transfer_confirm)
        is ConfirmState.Result -> ""
    }
}

@Composable
fun ConfirmError.toLabel() = when (this) {
    is ConfirmError.Init,
    is ConfirmError.TransactionIncorrect,
    is ConfirmError.PreloadError -> stringResource(R.string.confirm_fee_error)
    is ConfirmError.InsufficientBalance -> stringResource(R.string.transfer_insufficient_balance, chainTitle)
    is ConfirmError.InsufficientFee -> stringResource(R.string.transfer_insufficient_network_fee_balance, chain.asset().name)
    is ConfirmError.BroadcastError ->  "${stringResource(R.string.errors_transfer_error)}: ${message ?: stringResource(R.string.errors_unknown)}"
    is ConfirmError.SignFail -> stringResource(R.string.errors_transfer_error)
    is ConfirmError.RecipientEmpty -> "${stringResource(R.string.errors_transfer_error)}: recipient can't empty"
    is ConfirmError.None -> stringResource(id = R.string.transfer_confirm)
}