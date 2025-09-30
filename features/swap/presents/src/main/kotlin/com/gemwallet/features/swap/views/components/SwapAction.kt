package com.gemwallet.features.swap.views.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gemwallet.android.model.AssetInfo
import com.gemwallet.android.ui.R
import com.gemwallet.android.ui.components.progress.CircularProgressIndicator20
import com.gemwallet.android.ui.theme.mainActionHeight
import com.gemwallet.features.swap.viewmodels.models.SwapError
import com.gemwallet.features.swap.viewmodels.models.SwapState

@Composable
internal fun SwapAction(swapState: SwapState, pay: AssetInfo?, onSwap: () -> Unit) {
    Button(
        modifier = Modifier.fillMaxWidth().heightIn(mainActionHeight),
        onClick = onSwap,
        enabled = (swapState == SwapState.Ready || (swapState is SwapState.Error && swapState.error !is SwapError.InsufficientBalance))
    ) {
        when (swapState) {
            SwapState.None,
            SwapState.Ready -> Text(
                text = stringResource(R.string.wallet_swap),
                style = MaterialTheme.typography.bodyLarge,
                fontSize = 18.sp,
            )
            SwapState.Swapping,
            SwapState.GetQuote,
            SwapState.CheckAllowance,
            SwapState.Approving -> CircularProgressIndicator20(color = Color.White)

            is SwapState.Error -> Text(
                modifier = Modifier.padding(4.dp),
                text = when(swapState.error) {
                    is SwapError.InsufficientBalance -> stringResource(R.string.transfer_insufficient_balance, (swapState.error as SwapError.InsufficientBalance).symbol)
                    else -> stringResource(R.string.common_try_again)
                },
                fontSize = 18.sp,
            )
        }
    }
}