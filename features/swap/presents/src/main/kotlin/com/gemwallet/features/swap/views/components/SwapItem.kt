package com.gemwallet.features.swap.views.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.clearText
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.gemwallet.android.model.AssetInfo
import com.gemwallet.android.ui.R
import com.gemwallet.android.ui.components.clickable
import com.gemwallet.android.ui.components.image.IconWithBadge
import com.gemwallet.android.ui.components.progress.CircularProgressIndicator16
import com.gemwallet.android.ui.theme.Spacer8
import com.gemwallet.android.ui.theme.paddingDefault
import com.gemwallet.android.ui.theme.smallPadding
import com.gemwallet.features.swap.viewmodels.cases.availableBalance
import com.gemwallet.features.swap.viewmodels.cases.availableBalanceFormatted
import com.gemwallet.features.swap.viewmodels.models.SwapItemType
import com.wallet.core.primitives.Asset

@Composable
internal fun SwapItem(
    type: SwapItemType,
    item: AssetInfo?,
    equivalent: String,
    calculating: Boolean = false,
    state: TextFieldState = rememberTextFieldState(),
    onAssetSelect: (SwapItemType) -> Unit,
) {
    val title by remember {
        derivedStateOf {
            when (type) {
                SwapItemType.Pay -> R.string.swap_you_pay
                SwapItemType.Receive -> R.string.swap_you_receive
            }
        }
    }

    Column(modifier = Modifier.padding(horizontal = paddingDefault).fillMaxWidth()) {
        Text(text = stringResource(title), style = MaterialTheme.typography.labelMedium)
        Spacer8()
        Row(
            modifier = Modifier.height(44.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SwapItemInput(calculating, type, state)
            SwapItemLotInfo(item?.asset) { onAssetSelect(type) }
        }
        Spacer8()
        SwapValues(type, calculating, equivalent, item?.availableBalanceFormatted) {
            state.clearText()
            state.edit { append(item?.availableBalance) }
        }
    }
}

@Composable
private fun SwapItemLotInfo(
    asset: Asset?,
    onClick: () -> Unit
) {
    if (asset == null) {
        SelectAssetInfo(onClick)
    } else {
        AssetInfo(asset, onClick)
    }
}

@Composable
private fun SelectAssetInfo(onClick: () -> Unit) {
    Row(
        modifier = Modifier.clickable(onClick).smallPadding(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.End,
    ) {
        Text(
            text = stringResource(R.string.assets_select_asset),
            style = MaterialTheme.typography.bodyLarge,
        )
        Icon(imageVector = Icons.Default.KeyboardArrowDown, contentDescription = "")
    }
}

@Composable
private fun AssetInfo(
    asset: Asset,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier.clickable(onClick).smallPadding(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.End,
    ) {
        IconWithBadge(asset)
        Spacer(modifier = Modifier.size(8.dp))
        Text(
            text = asset.symbol,
            style = MaterialTheme.typography.bodyLarge,
        )
        Icon(imageVector = Icons.Default.KeyboardArrowDown, contentDescription = "")
    }
}

@Composable
private fun SwapValues(
    type: SwapItemType,
    calculating: Boolean,
    equivalent: String,
    balance: String?,
    onBalanceClick: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            modifier = Modifier.fillMaxWidth(0.5f),
            text = if (calculating) "" else equivalent,
            style = MaterialTheme.typography.bodySmall,
        )
        Spacer(modifier = Modifier.size(8.dp))
        Box(modifier = Modifier.fillMaxWidth(1f)) {
            Text(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .clip(MaterialTheme.shapes.small)
                    .clickable(enabled = type == SwapItemType.Pay, onClick = onBalanceClick)
                    .smallPadding(),
                text = balance?.let { stringResource(id = R.string.transfer_balance, it) } ?: "",
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.End,
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

@Composable
private fun RowScope.SwapItemInput(
    calculating: Boolean,
    type: SwapItemType,
    state: TextFieldState = rememberTextFieldState(),
) {
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        if (type == SwapItemType.Pay) {
            focusRequester.requestFocus()
        }
    }
    Box(
        modifier = Modifier
            .weight(1f)
            .fillMaxWidth()
    ) {
        if (calculating) {
            CircularProgressIndicator16()
        } else {
            BasicTextField(
                modifier = Modifier.fillMaxWidth().focusRequester(focusRequester),
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
                            color = MaterialTheme.colorScheme.secondary,
                        )
                    }
                    innerTextField()
                },
                readOnly = type == SwapItemType.Receive
            )
        }
    }
}