package com.gemwallet.android.features.swap.views

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideIn
import androidx.compose.animation.slideOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gemwallet.android.R
import com.gemwallet.android.features.asset_select.views.SelectSwapScreen
import com.gemwallet.android.features.confirm.views.ConfirmScreen
import com.gemwallet.android.features.swap.models.SwapError
import com.gemwallet.android.features.swap.models.SwapItemModel
import com.gemwallet.android.features.swap.models.SwapItemType
import com.gemwallet.android.features.swap.models.SwapPairSelect
import com.gemwallet.android.features.swap.models.SwapState
import com.gemwallet.android.features.swap.viewmodels.SwapViewModel
import com.gemwallet.android.model.ConfirmParams
import com.gemwallet.android.ui.components.TransactionItem
import com.gemwallet.android.ui.components.designsystem.Spacer16
import com.gemwallet.android.ui.components.designsystem.Spacer2
import com.gemwallet.android.ui.components.designsystem.Spacer8
import com.gemwallet.android.ui.components.designsystem.padding16
import com.gemwallet.android.ui.components.designsystem.trailingIcon20
import com.gemwallet.android.ui.components.screen.Scene
import com.wallet.core.primitives.Asset
import com.wallet.core.primitives.AssetId
import com.wallet.core.primitives.AssetType
import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.TransactionState

@Composable
fun SwapScreen(
    viewModel: SwapViewModel = hiltViewModel(),
    onConfirm: (ConfirmParams) -> Unit,
    onCancel: () -> Unit,
) {
    val selectState by viewModel.selectPair.collectAsStateWithLifecycle()
    val pairState by viewModel.swapPairUIModel.collectAsStateWithLifecycle()
    val fromEquivalent by viewModel.fromEquivalent.collectAsStateWithLifecycle()
    val toEquivalent by viewModel.toEquivalent.collectAsStateWithLifecycle()
    val swapState by viewModel.swapScreenState.collectAsStateWithLifecycle()
    val approveTx by viewModel.approveTx.collectAsStateWithLifecycle()

    var approveParams by rememberSaveable { mutableStateOf<ConfirmParams?>(null) }
    val pair = pairState
    BackHandler(selectState != null) {
        if (approveParams != null) {
            approveParams = null
            return@BackHandler
        }

        val fromId = pair?.from?.asset?.id
        val toId = pair?.to?.asset?.id
        if (fromId == null && toId == null) {
            onCancel()
        } else {
            viewModel.onSelect(SwapPairSelect.request(fromId, toId))
        }
    }

    if (pair != null) {
        Scene(
            title = stringResource(id = R.string.wallet_swap),
            padding = PaddingValues(padding16),
            mainAction = {
                SwapAction(swapState, pair) {
                    viewModel.swap(
                        when (swapState) {
                            SwapState.Ready -> onConfirm
                            SwapState.RequestApprove -> { { approveParams = it } }
                            else -> { {} }
                        }
                    )
                }
            },
            onClose = onCancel,
        ) {
            SwapItem(
                type = SwapItemType.Pay,
                item = pair.from,
                equivalent = fromEquivalent,
                state = viewModel.fromValue,
                onAssetSelect = viewModel::changePair
            )
            IconButton(onClick = viewModel::switchSwap) {
                Icon(
                    imageVector = Icons.Default.SwapVert,
                    contentDescription = "swap_switch"
                )
            }
            SwapItem(
                type = SwapItemType.Receive,
                item = pair.to,
                equivalent = toEquivalent,
                state = viewModel.toValue,
                calculating = swapState == SwapState.GetQuote,
                onAssetSelect = viewModel::changePair
            )
            Spacer16()
            val tx = approveTx
            if (tx?.transaction?.state == TransactionState.Pending) {
                TransactionItem(tx, true) { }
            }
            Spacer16()
            SwapError(swapState)
        }
    }

    AnimatedVisibility(
        visible = approveParams != null,
        enter = slideIn { IntOffset(it.width, 0) },
        exit = slideOut { IntOffset(it.width, 0) },
    ) {
        LocalSoftwareKeyboardController.current?.hide()
        ConfirmScreen(
            approveParams ?: return@AnimatedVisibility,
            finishAction = { assetId, hash, route ->
                approveParams = null
                viewModel.onTxHash(hash)
            },
            cancelAction = {
                approveParams = null
            },
        )
    }

    AnimatedVisibility(
        visible = selectState != null,
        enter = slideIn { IntOffset(it.width, 0) },
        exit = slideOut { IntOffset(it.width, 0) },
    ) {
        SelectSwapScreen(
            select = selectState ?: return@AnimatedVisibility,
            onCancel = {
                val fromId = pair?.from?.asset?.id
                val toId = pair?.to?.asset?.id
                if (fromId == null || toId == null) {
                    onCancel()
                } else {
                    viewModel.onSelect(SwapPairSelect.request(fromId, toId))
                }
            },
            onSelect = {select -> viewModel.onSelect(select) },
        )
    }
}

@Composable
private fun SwapError(state: SwapState) {
    if (state !is SwapState.Error || state.error == SwapError.None) {
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
        Spacer2()
        Text(
            text = when (state.error) {
                SwapError.None -> ""
                SwapError.IncorrectInput -> stringResource(R.string.common_required_field, stringResource(R.string.swap_you_pay))
                SwapError.NoQuote -> stringResource(R.string.errors_swap_no_quote_data)
                SwapError.NotSupportedAsset -> stringResource(com.gemwallet.android.localize.R.string.errors_swap_not_supported_asset)
                SwapError.NotSupportedChain -> stringResource(com.gemwallet.android.localize.R.string.errors_swap_not_supported_chain)
                SwapError.NotImplemented,
                SwapError.NotSupportedPair -> stringResource(com.gemwallet.android.localize.R.string.errors_swap_not_supported_pair)
                SwapError.NetworkError -> "Node not available. Check internet connection."
                is SwapError.Unknown -> "${stringResource(com.gemwallet.android.localize.R.string.errors_unknown_try_again)}: ${state.error.message}"
            },
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@Preview
@Composable
fun PreviewSwapItem() {
    MaterialTheme {
        SwapItem(
            type = SwapItemType.Pay,
            item = SwapItemModel(
                Asset(
                    id = AssetId(Chain.Ethereum),
                    symbol = "ETH",
                    name = "Ethereum",
                    type = AssetType.NATIVE,
                    decimals = 18,
                ),
                assetBalanceValue = "10.0",
                assetBalanceLabel = "10.0 ETH",
            ),
            equivalent = "0.0$",
            calculating = true,
            onAssetSelect = {},
        )
    }
}