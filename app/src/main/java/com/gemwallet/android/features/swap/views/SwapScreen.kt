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
import androidx.compose.runtime.DisposableEffect
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
import com.gemwallet.android.ext.toIdentifier
import com.gemwallet.android.ext.type
import com.gemwallet.android.features.asset_select.views.SelectSwapScreen
import com.gemwallet.android.features.swap.model.SwapDetails
import com.gemwallet.android.features.swap.model.SwapError
import com.gemwallet.android.features.swap.model.SwapItemState
import com.gemwallet.android.features.swap.model.SwapItemType
import com.gemwallet.android.features.swap.model.SwapScreenState
import com.gemwallet.android.features.swap.viewmodels.SwapViewModel
import com.gemwallet.android.interactors.getIconUrl
import com.gemwallet.android.model.ConfirmParams
import com.gemwallet.android.ui.components.AssetIcon
import com.gemwallet.android.ui.components.CircularProgressIndicator16
import com.gemwallet.android.ui.components.FatalStateScene
import com.gemwallet.android.ui.components.LoadingScene
import com.gemwallet.android.ui.components.Scene
import com.gemwallet.android.ui.theme.Spacer16
import com.gemwallet.android.ui.theme.Spacer8
import com.gemwallet.android.ui.theme.mainActionHeight
import com.gemwallet.android.ui.theme.padding16
import com.gemwallet.android.ui.theme.padding4
import com.wallet.core.primitives.AssetId
import com.wallet.core.primitives.AssetSubtype
import com.wallet.core.primitives.AssetType
import com.wallet.core.primitives.Chain

@Composable
fun SwapScreen(
    fromAssetId: AssetId,
    toAssetId: AssetId,
    viewModel: SwapViewModel = hiltViewModel(),
    onConfirm: (ConfirmParams) -> Unit,
    onCancel: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    DisposableEffect(fromAssetId.toIdentifier(), toAssetId.toIdentifier()) {

        viewModel.init(fromAssetId, toAssetId)

        onDispose {  }
    }

    LaunchedEffect(Unit) {
        viewModel.updateQuote()
    }

    BackHandler(uiState.select != null) {
        viewModel.assetSelect(null)
    }

    val details = uiState.details

    when {
        uiState.isFatal -> FatalStateScene(
            title = stringResource(id = R.string.wallet_swap),
            message = "Swap unavailable",
            onCancel = onCancel,
        )
        uiState.isLoading -> LoadingScene(stringResource(id = R.string.wallet_swap), onCancel)
        details is SwapDetails.Quote -> Form(
            details = details,
            payState = viewModel.payValue,
            receiveState = viewModel.receiveValue,
            onSwap = { viewModel.swap(onConfirm) },
            onSwitch = viewModel::switchSwap,
            onCancel = onCancel,
            onAssetSelect = viewModel::assetSelect
        )
    }

    AnimatedVisibility(
        visible = details is SwapDetails.None || uiState.select != null,
        enter = slideIn { IntOffset(it.width, 0) },
        exit = slideOut { IntOffset(it.width, 0) },
    ) {
        SelectSwapScreen(
            select = SwapScreenState.Select(
                changeType = uiState.select?.changeType ?: SwapItemType.Pay,
                changeAssetId = uiState.select?.changeAssetId,
                oppositeAssetId = uiState.select?.oppositeAssetId,
                prevAssetId = uiState.select?.prevAssetId,
            ),
            onCancel = {
                if (uiState.select == null) {
                    onCancel()
                } else {
                    viewModel.assetSelect(null)
                }
            },
            onSelect = {asset, type -> viewModel.changeAsset(asset, type) },
        )
    }
}


@Composable
fun Form(
    details: SwapDetails.Quote,
    payState: TextFieldState,
    receiveState: TextFieldState,
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
                if (!details.allowance) {
                    Text(
                        modifier = Modifier.fillMaxWidth(),
                        text = stringResource(id = R.string.swap_approve_token_permission, details.pay.assetSymbol),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodySmall,
                    )
                    Spacer16()
                }
                Button(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(mainActionHeight),
                    onClick = onSwap,
                    enabled = details.error == SwapError.None && !details.swaping
                ) {
                    if (details.swaping) {
                        Box(modifier = Modifier.fillMaxWidth()) {
                            CircularProgressIndicator16(modifier = Modifier.align(Alignment.Center))
                        }
                        return@Button
                    }

                    if (details.error == SwapError.None) {
                        when (details.allowance) {
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
                                        details.pay.assetSymbol
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
        SwapItem(item = details.pay, state = payState, isPay = true, onAssetSelect = onAssetSelect)
        IconButton(onClick = onSwitch) {
            Icon(
                imageVector = Icons.Default.SwapVert,
                contentDescription = "swap_switch"
            )
        }
        SwapItem(item = details.receive, state = receiveState, onAssetSelect = onAssetSelect)
    }
}

@Composable
fun SwapItem(
    item: SwapItemState,
    state: TextFieldState = rememberTextFieldState(),
    isPay: Boolean = false,
    onAssetSelect: (SwapItemType) -> Unit,
) {
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        if (isPay) {
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
                id = when (item.type){
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
            Box(modifier = Modifier
                .weight(1f)
                .fillMaxWidth()) {
                if (item.calculating) {
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
                        readOnly = item.type == SwapItemType.Receive
                    )
                }
            }
            Row(
                modifier = Modifier
                    .clip(MaterialTheme.shapes.medium)
                    .clickable { onAssetSelect(item.type) }
                    .padding(padding4)
                ,
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End,
            ) {
                AssetIcon(
                    modifier = Modifier.size(36.dp),
                    iconUrl = item.assetIcon,
                    placeholder = item.assetType.string,
                    supportIcon = if (item.assetId.type() == AssetSubtype.NATIVE) {
                        null
                    } else {
                        item.assetId.chain.getIconUrl()
                    },
                )
                Spacer(modifier = Modifier.size(8.dp))
                Text(
                    text = item.assetSymbol,
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
                text = item.equivalentValue,
                style = MaterialTheme.typography.bodySmall,
            )
            Spacer(modifier = Modifier.size(8.dp))
            Box(modifier = Modifier.fillMaxWidth(1f)) {
                Text(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .clip(MaterialTheme.shapes.small)
                        .clickable(
                            enabled = isPay,
                        ) {
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
            item = SwapItemState(
                type = SwapItemType.Pay,
                assetId = AssetId(Chain.Ethereum),
                assetIcon = "",
                assetSymbol = "ETH",
                assetType = AssetType.NATIVE,
                equivalentValue = "0.0$",
                assetBalanceValue = "10.0",
                assetBalanceLabel = "10.0 ETH",
                calculating = true,
            ),
            onAssetSelect = {},
        )
    }
}

@Preview
@Composable
fun PreviewSwapScene() {
    MaterialTheme {
        Form(
            details = SwapDetails.Quote(
                allowance = false,
                error = SwapError.None,
                swaping = false,
                pay = SwapItemState(
                    type = SwapItemType.Pay,
                    assetId = AssetId(Chain.Ethereum),
                    assetIcon = "",
                    assetSymbol = "ETH",
                    assetType = AssetType.NATIVE,
                    equivalentValue = "0.0$",
                    assetBalanceValue = "10.0",
                    assetBalanceLabel = "10.0 ETH",
                ),
                receive = SwapItemState(
                    type = SwapItemType.Pay,
                    assetId = AssetId(Chain.Ethereum),
                    assetIcon = "",
                    assetSymbol = "ETH",
                    assetType = AssetType.NATIVE,
                    equivalentValue = "0.0$",
                    assetBalanceValue = "10.0",
                    assetBalanceLabel = "10.0 ETH",
                    calculating = true,
                ),
            ),
            payState = rememberTextFieldState(),
            receiveState = rememberTextFieldState(),
            onSwap = {},
            onSwitch = {},
            onCancel = {},
            onAssetSelect = {},
        )
    }
}