package com.gemwallet.features.swap.views.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.gemwallet.android.ui.R
import com.gemwallet.android.ui.theme.Spacer2
import com.gemwallet.android.ui.theme.Spacer8
import com.gemwallet.android.ui.theme.defaultPadding
import com.gemwallet.android.ui.theme.trailingIconMedium
import com.gemwallet.features.swap.viewmodels.models.SwapError
import com.gemwallet.features.swap.viewmodels.models.SwapState

@Composable
internal fun SwapError(state: SwapState) {
    val state = state as? SwapState.Error ?: return

    val errorText = when (state.error) {
        SwapError.None -> ""
        SwapError.IncorrectInput -> stringResource(
            R.string.common_required_field,
            stringResource(R.string.swap_you_pay)
        )
        SwapError.NotSupportedAsset -> stringResource(R.string.errors_swap_not_supported_asset)
        SwapError.NotSupportedChain -> stringResource(R.string.errors_swap_not_supported_chain)
        SwapError.NotImplemented,
        SwapError.NotSupportedPair -> stringResource(R.string.errors_swap_not_supported_pair)
        SwapError.NetworkError -> "Node not available. Check internet connection."
        is SwapError.Unknown -> "${stringResource(R.string.errors_unknown_try_again)}: ${(state.error as SwapError.Unknown).message}"
        SwapError.None,
        is SwapError.InsufficientBalance -> return
        SwapError.InputAmountTooSmall -> stringResource(R.string.errors_swap_amount_too_small)
        SwapError.NoAvailableProvider,
        SwapError.NoQuote,
        SwapError.TransactionError -> stringResource(R.string.errors_swap_no_quote_available)
    }
    Column(
        modifier = Modifier
            .defaultPadding()
            .background(
                MaterialTheme.colorScheme.errorContainer.copy(0.2f),
                shape = MaterialTheme.shapes.medium
            )
            .fillMaxWidth()
            .defaultPadding(),
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
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.W400),
            )
        }
        Spacer2()
        Text(
            text = errorText,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}