package com.gemwallet.features.confirm.presents.components

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
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.gemwallet.android.ext.asset
import com.gemwallet.android.ui.R
import com.gemwallet.android.ui.components.InfoBottomSheet
import com.gemwallet.android.ui.components.InfoSheetEntity
import com.gemwallet.android.ui.models.actions.AssetIdAction
import com.gemwallet.android.ui.theme.Spacer4
import com.gemwallet.android.ui.theme.Spacer8
import com.gemwallet.android.ui.theme.defaultPadding
import com.gemwallet.android.ui.theme.paddingDefault
import com.gemwallet.android.ui.theme.trailingIconMedium
import com.gemwallet.features.confirm.models.ConfirmError
import com.gemwallet.features.confirm.models.ConfirmState
import com.gemwallet.features.confirm.presents.toLabel

@Composable
internal fun ConfirmErrorInfo(state: ConfirmState, feeValue: String, isShowBottomSheetInfo: Boolean, onBuy: AssetIdAction) {
    var isShowInfoSheet by remember(isShowBottomSheetInfo) { mutableStateOf(isShowBottomSheetInfo) }

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
    Column(
        modifier = Modifier.Companion
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
            verticalAlignment = Alignment.Companion.CenterVertically
        ) {
            Icon(
                modifier = Modifier.Companion.size(trailingIconMedium),
                imageVector = Icons.Outlined.Warning,
                tint = MaterialTheme.colorScheme.error,
                contentDescription = ""
            )
            Spacer8()
            Text(
                text = stringResource(R.string.errors_error_occured),
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Companion.W500),
            )
        }
        Spacer4()
        Row {
            infoSheetEntity?.let {
                Icon(
                    modifier = Modifier.Companion
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