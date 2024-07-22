package com.gemwallet.android.features.swap.views

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideIn
import androidx.compose.animation.slideOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.clearText
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gemwallet.android.R
import com.gemwallet.android.ext.type
import com.gemwallet.android.features.asset_select.views.SelectSwapScreen
import com.gemwallet.android.features.swap.model.SwapError
import com.gemwallet.android.features.swap.model.SwapItemModel
import com.gemwallet.android.features.swap.model.SwapItemType
import com.gemwallet.android.features.swap.model.SwapPairSelect
import com.gemwallet.android.features.swap.model.SwapPairUIModel
import com.gemwallet.android.features.swap.viewmodels.SwapViewModel
import com.gemwallet.android.interactors.getIconUrl
import com.gemwallet.android.model.ConfirmParams
import com.gemwallet.android.ui.components.AssetIcon
import com.gemwallet.android.ui.components.CircularProgressIndicator16
import com.gemwallet.android.ui.components.Scene
import com.gemwallet.android.ui.theme.Spacer16
import com.gemwallet.android.ui.theme.Spacer8
import com.gemwallet.android.ui.theme.mainActionHeight
import com.gemwallet.android.ui.theme.padding16
import com.gemwallet.android.ui.theme.padding4
import com.wallet.core.primitives.Asset
import com.wallet.core.primitives.AssetId
import com.wallet.core.primitives.AssetSubtype
import com.wallet.core.primitives.AssetType
import com.wallet.core.primitives.Chain

@Composable
fun SwapScreen(
    viewModel: SwapViewModel = hiltViewModel(),
    onConfirm: (ConfirmParams) -> Unit,
    onCancel: () -> Unit,
) {
    val selectState by viewModel.selectPairUiState.collectAsStateWithLifecycle()
    val allowance by viewModel.allowance.collectAsStateWithLifecycle()
    val pair by viewModel.swapPairUIModel.collectAsStateWithLifecycle()
    val fromEquivalent by viewModel.fromEquivalent.collectAsStateWithLifecycle()
    val toEquivalent by viewModel.toEquivalent.collectAsStateWithLifecycle()
    val calculating by viewModel.calculatingQuote.collectAsStateWithLifecycle()
    val swapping by viewModel.swapping.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()

    BackHandler(selectState != null) {
        val fromId = pair?.from?.asset?.id // TODO:
        val toId = pair?.to?.asset?.id
        if (fromId == null || toId == null) {
            onCancel()
        } else {
            viewModel.onSelect(SwapPairSelect.request(fromId, toId))
        }
    }

    if (pair != null) {
        Form(
            pair = pair!!,
            allowance = allowance,
            fromEquivalent = fromEquivalent,
            toEquivalent = toEquivalent,
            calculating = calculating,
            swapping = swapping,
            error = error,
            fromState = viewModel.fromValue,
            toState = viewModel.toValue,
            onSwap = { viewModel.swap(onConfirm) },
            onSwitch = viewModel::switchSwap,
            onCancel = onCancel,
            onAssetSelect = {}
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
                val fromId = pair?.from?.asset?.id // TODO:
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
fun Form(
    pair: SwapPairUIModel,
    allowance: Boolean,
    fromEquivalent: String,
    toEquivalent: String,
    calculating: Boolean,
    error: SwapError,
    swapping: Boolean,
    fromState: TextFieldState,
    toState: TextFieldState,
    onSwap: () -> Unit,
    onSwitch: () -> Unit,
    onAssetSelect: (SwapItemType) -> Unit,
    onCancel: () -> Unit,
) {
    Scene(
        title = stringResource(id = R.string.wallet_swap),
        padding = PaddingValues(padding16),
        mainAction = {
            Column {
                if (!allowance) {
                    Text(
                        modifier = Modifier.fillMaxWidth(),
                        text = stringResource(id = R.string.swap_approve_token_permission, pair.from.asset.symbol),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodySmall,
                    )
                    Spacer16()
                }
                Button(
                    modifier = Modifier.fillMaxWidth().heightIn(mainActionHeight),
                    onClick = onSwap,
                    enabled = error == SwapError.None && !swapping
                ) {
                    if (swapping) {
                        Box(modifier = Modifier.fillMaxWidth()) {
                            CircularProgressIndicator16(modifier = Modifier.align(Alignment.Center))
                        }
                        return@Button
                    }

                    if (error == SwapError.None) {
                        when (allowance) {
                            true -> Text(
                                text = stringResource(R.string.wallet_swap),
                                style = MaterialTheme.typography.bodyLarge,
                                fontSize = 18.sp,
                            )
                            false -> {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = "approve_icon"
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                                Text(
                                    modifier = Modifier.padding(4.dp),
                                    text = stringResource(
                                        id = R.string.swap_approve_token,
                                        pair.from.asset.symbol
                                    ),
                                    fontSize = 18.sp,
                                )
                            }
                        }
                    } else {
                        Text(
                            modifier = Modifier.padding(4.dp),
                            text = "Quote error",
                            fontSize = 18.sp,
                        )
                    }
                }
            }
        },
        onClose = onCancel,
    ) {
        SwapItem(type = SwapItemType.Pay, item = pair.from, equivalent = fromEquivalent, state = fromState, onAssetSelect = onAssetSelect)
        IconButton(onClick = onSwitch) {
            Icon(
                imageVector = Icons.Default.SwapVert,
                contentDescription = "swap_switch"
            )
        }
        SwapItem(type = SwapItemType.Receive, item = pair.to, equivalent = toEquivalent, state = toState, calculating = calculating, onAssetSelect = onAssetSelect)
    }
}

@Composable
fun SwapItem(
    type: SwapItemType,
    item: SwapItemModel,
    equivalent: String,
    calculating: Boolean = false,
    state: TextFieldState = rememberTextFieldState(),
    onAssetSelect: (SwapItemType) -> Unit,
) {
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        if (type == SwapItemType.Pay) {
            focusRequester.requestFocus()
        }
    }

    Column(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth()
    ) {
        Text(
            text = stringResource(
                id = when (type){
                    SwapItemType.Pay -> R.string.swap_you_pay
                    SwapItemType.Receive -> R.string.swap_you_receive
                }
            ),
            style = MaterialTheme.typography.labelMedium,
        )
        Spacer8()
        Row(
            modifier = Modifier
                .height(40.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                if (calculating) {
                    CircularProgressIndicator16()
                } else {
                    BasicTextField(
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(focusRequester),
                        state = state,
                        textStyle = MaterialTheme.typography.headlineLarge.copy(
                            color = MaterialTheme.colorScheme.onSurface
                        ),
                        lineLimits = TextFieldLineLimits.SingleLine,
                        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                        keyboardOptions = KeyboardOptions.Default.copy(
                            autoCorrectEnabled = false,
                            keyboardType = KeyboardType.Decimal
                        ),
                        decorator = { innerTextField ->
                            if (state.text.isEmpty()) {
                                Text(
                                    text = "0",
                                    style = MaterialTheme.typography.headlineLarge,
                                    color = Color.Gray.copy(alpha = 0.5f),
                                )
                            }
                            innerTextField()
                        },
                        readOnly = type == SwapItemType.Receive
                    )
                }
            }
            Row(
                modifier = Modifier
                    .clip(MaterialTheme.shapes.medium)
                    .clickable { onAssetSelect(type) }
                    .padding(padding4)
                ,
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End,
            ) {
                AssetIcon(
                    modifier = Modifier.size(36.dp),
                    iconUrl = item.asset.getIconUrl(),
                    placeholder = item.asset.type.string,
                    supportIcon = if (item.asset.id.type() == AssetSubtype.NATIVE) {
                        null
                    } else {
                        item.asset.id.chain.getIconUrl()
                    },
                )
                Spacer(modifier = Modifier.size(8.dp))
                Text(
                    text = item.asset.symbol,
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
        }
        Spacer8()
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                modifier = Modifier.fillMaxWidth(0.5f),
                text = equivalent,
                style = MaterialTheme.typography.bodySmall,
            )
            Spacer(modifier = Modifier.size(8.dp))
            Box(modifier = Modifier.fillMaxWidth(1f)) {
                Text(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .clip(MaterialTheme.shapes.small)
                        .clickable(enabled = type == SwapItemType.Pay) {
                            state.clearText()
                            state.edit { append(item.assetBalanceValue) }
                        }
                        .padding(padding4)
                    ,
                    text = stringResource(id = R.string.transfer_balance, item.assetBalanceLabel),
                    textAlign = TextAlign.End,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
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

@Preview
@Composable
fun PreviewSwapScene() {
    MaterialTheme {
        Form(
            pair = SwapPairUIModel(
                from = SwapItemModel(
                    asset = Asset(
                        id = AssetId(Chain.Ethereum),
                        symbol = "ETH",
                        name = "Ethereum",
                        type = AssetType.NATIVE,
                        decimals = 18,
                    ),
                    assetBalanceLabel = "10.0 ETH",
                    assetBalanceValue = "10.0",
                ),
                to = SwapItemModel(
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
            ),
            fromEquivalent = "0.0$",
            toEquivalent = "0.0$",
            allowance = false,
            calculating = false,
            swapping = false,
            error = SwapError.None,
            fromState = rememberTextFieldState(),
            toState = rememberTextFieldState(),
            onSwap = {},
            onSwitch = {},
            onCancel = {},
            onAssetSelect = {},
        )
    }
}