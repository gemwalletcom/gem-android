package com.gemwallet.features.confirm.views

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gemwallet.android.ext.asset
import com.gemwallet.android.model.ConfirmParams
import com.gemwallet.android.ui.R
import com.gemwallet.android.ui.components.InfoBottomSheet
import com.gemwallet.android.ui.components.InfoSheetEntity
import com.gemwallet.android.ui.components.buttons.MainActionButton
import com.gemwallet.android.ui.components.list_head.AmountListHead
import com.gemwallet.android.ui.components.list_head.NftHead
import com.gemwallet.android.ui.components.list_head.SwapListHead
import com.gemwallet.android.ui.components.list_item.getTitle
import com.gemwallet.android.ui.components.list_item.property.PropertyDataText
import com.gemwallet.android.ui.components.list_item.property.PropertyItem
import com.gemwallet.android.ui.components.list_item.property.PropertyNetworkFee
import com.gemwallet.android.ui.components.list_item.property.PropertyNetworkItem
import com.gemwallet.android.ui.components.list_item.property.PropertyTitleText
import com.gemwallet.android.ui.components.progress.CircularProgressIndicator16
import com.gemwallet.android.ui.components.screen.Scene
import com.gemwallet.android.ui.models.ListPosition
import com.gemwallet.android.ui.models.actions.AssetIdAction
import com.gemwallet.android.ui.models.actions.CancelAction
import com.gemwallet.android.ui.models.actions.FinishConfirmAction
import com.gemwallet.android.ui.theme.Spacer4
import com.gemwallet.android.ui.theme.Spacer8
import com.gemwallet.android.ui.theme.defaultPadding
import com.gemwallet.android.ui.theme.paddingDefault
import com.gemwallet.android.ui.theme.trailingIconMedium
import com.gemwallet.features.confirm.models.ConfirmError
import com.gemwallet.features.confirm.models.ConfirmProperty
import com.gemwallet.features.confirm.models.ConfirmState
import com.gemwallet.features.confirm.models.FeeUIModel
import com.gemwallet.features.confirm.viewmodels.ConfirmViewModel
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
    val txProperties by viewModel.txProperties.collectAsStateWithLifecycle()
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
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            item {
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
                        icon = amountModel?.asset?.asset,
                    )
                }
            }
            itemsIndexed(txProperties) { index, item ->
                val listPosition = ListPosition.getPosition(index, txProperties.size)
                when (item) {
                    is ConfirmProperty.Destination -> PropertyDestination(item, listPosition)
                    is ConfirmProperty.Memo -> PropertyItem(R.string.transfer_memo, item.data, listPosition = listPosition)
                    is ConfirmProperty.Network -> PropertyNetworkItem(item.data, listPosition)
                    is ConfirmProperty.Source -> PropertyItem(R.string.common_wallet, item.data, listPosition = listPosition)
                }
            }
            item {
                feeModel?.let {
                    when (it) {
                        FeeUIModel.Calculating -> PropertyItem(
                            modifier = Modifier.height(72.dp),
                            title = { PropertyTitleText(R.string.transfer_network_fee) },
                            data = { Row(horizontalArrangement = Arrangement.End) { CircularProgressIndicator16() } },
                            listPosition = ListPosition.Single,
                        )
                        is FeeUIModel.FeeInfo -> PropertyNetworkFee(
                            it.feeAsset.name,
                            it.feeAsset.symbol,
                            it.cryptoAmount,
                            it.fiatAmount,
                            allFee.size > 1,
                        ) { showSelectTxSpeed = true }
                        FeeUIModel.Error -> PropertyItem(
                            modifier = Modifier.height(72.dp),
                            title = { PropertyTitleText(R.string.transfer_network_fee) },
                            data = { PropertyDataText("~") },
                            listPosition = ListPosition.Single,
                        )
                    }

                }
            }
            item {
                ConfirmErrorInfo(state, feeValue = feeValue, isShowBottomSheetInfo, onBuy)
            }
        }

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
    val message = state.message
    val infoSheetEntity = when (message) {
        is ConfirmError.InsufficientFee -> InfoSheetEntity.NetworkBalanceRequiredInfo(
            chain = message.chain,
            value = feeValue,
            actionLabel = stringResource(R.string.asset_buy_asset, message.chain.asset().symbol),
            action = { onBuy(message.chain.asset().id) },
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
            .padding(horizontal = paddingDefault)
            .background(
                MaterialTheme.colorScheme.errorContainer.copy(0.3f),
                shape = MaterialTheme.shapes.medium
            )
            .fillMaxWidth()
            .defaultPadding()
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
    is ConfirmError.PreloadError -> "${stringResource(R.string.confirm_fee_error)}: $message"
    is ConfirmError.InsufficientBalance -> stringResource(R.string.transfer_insufficient_balance, chainTitle)
    is ConfirmError.InsufficientFee -> stringResource(R.string.transfer_insufficient_network_fee_balance, chain.asset().name)
    is ConfirmError.BroadcastError ->  "${stringResource(R.string.errors_transfer_error)}: ${message ?: stringResource(R.string.errors_unknown)}"
    is ConfirmError.SignFail -> stringResource(R.string.errors_transfer_error)
    is ConfirmError.RecipientEmpty -> "${stringResource(R.string.errors_transfer_error)}: recipient can't empty"
    is ConfirmError.None -> stringResource(id = R.string.transfer_confirm)
}