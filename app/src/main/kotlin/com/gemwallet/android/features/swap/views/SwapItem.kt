package com.gemwallet.android.features.swap.views

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.gemwallet.android.features.swap.viewmodels.models.SwapItemModel
import com.gemwallet.android.features.swap.viewmodels.models.SwapItemType
import com.gemwallet.android.ui.R
import com.gemwallet.android.ui.components.designsystem.Spacer8
import com.gemwallet.android.ui.components.designsystem.padding4
import com.gemwallet.android.ui.components.image.IconWithBadge
import com.gemwallet.android.ui.components.progress.CircularProgressIndicator16

@Composable
internal fun SwapItem(
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
                id = when (type) {
                    SwapItemType.Pay -> R.string.swap_you_pay
                    SwapItemType.Receive -> R.string.swap_you_receive
                }
            ),
            style = MaterialTheme.typography.labelMedium,
        )
        Spacer8()
        Row(
            modifier = Modifier
                .height(44.dp) // TODO: Out to constant
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
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
                                    color = MaterialTheme.colorScheme.secondary,
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
                    .padding(padding4),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End,
            ) {
                IconWithBadge(item.asset)
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
                text = if (calculating) "" else equivalent,
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
                        .padding(padding4),
                    text = stringResource(id = R.string.transfer_balance, item.assetBalanceLabel),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.End,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }
}