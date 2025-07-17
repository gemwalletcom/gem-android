package com.gemwallet.android.features.swap.views

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gemwallet.android.features.swap.models.SwapError
import com.gemwallet.android.features.swap.models.SwapPairUIModel
import com.gemwallet.android.features.swap.models.SwapState
import com.gemwallet.android.ui.R
import com.gemwallet.android.ui.components.designsystem.Spacer16
import com.gemwallet.android.ui.components.designsystem.mainActionHeight
import com.gemwallet.android.ui.components.progress.CircularProgressIndicator20

@Composable
internal fun SwapAction(swapState: SwapState, pair: SwapPairUIModel, onSwap: () -> Unit) {
    Column {
        if (swapState == SwapState.RequestApprove) {
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(
                    id = R.string.swap_approve_token_permission,
                    pair.from.asset.symbol
                ),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodySmall,
            )
            Spacer16()
        }
        Button(
            modifier = Modifier.fillMaxWidth().heightIn(mainActionHeight),
            onClick = onSwap,
            enabled = (swapState == SwapState.Ready || swapState == SwapState.RequestApprove || swapState !is SwapState.Error)
        ) {
            when (swapState) {
                SwapState.None,
                SwapState.Ready -> Text(
                    text = stringResource(R.string.wallet_swap),
                    style = MaterialTheme.typography.bodyLarge,
                    fontSize = 18.sp,
                )

                SwapState.RequestApprove -> {
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
                SwapState.Swapping,
                SwapState.GetQuote,
                SwapState.CheckAllowance,
                SwapState.Approving -> CircularProgressIndicator20(color = Color.White)

                is SwapState.Error -> Text(
                    modifier = Modifier.padding(4.dp),
                    text = when(swapState.error) {
                        is SwapError.InsufficientBalance -> stringResource(R.string.transfer_insufficient_balance, swapState.error.symbol)
                        else -> stringResource(R.string.common_try_again)
                    },
                    fontSize = 18.sp,
                )
            }
        }
    }
}