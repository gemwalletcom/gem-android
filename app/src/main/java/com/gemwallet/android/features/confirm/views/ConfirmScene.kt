package com.gemwallet.android.features.confirm.views

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.gemwallet.android.features.confirm.models.ConfirmError
import com.gemwallet.android.features.confirm.models.ConfirmSceneState
import com.gemwallet.android.ui.components.AmountListHead
import com.gemwallet.android.ui.components.MainActionButton
import com.gemwallet.android.ui.components.Scene
import com.gemwallet.android.ui.components.SwapListHead
import com.gemwallet.android.ui.components.Table
import com.wallet.core.primitives.TransactionType

@Composable
fun ConfirmScene(
    state: ConfirmSceneState.Loaded,
    onCancel: () -> Unit,
    onFinish: (String) -> Unit,
    onSend: () -> Unit,
) {
    Scene(
        title = stringResource(state.title),
        onClose = onCancel,
        mainAction = {
            MainActionButton(
                title = state.error.stringResource(),
                enabled = state.error == ConfirmError.None,
                loading = state.sending,
                onClick = onSend
            )
        }
    ) {
        if (state.type == TransactionType.Swap) {
            SwapListHead(
                fromAsset = state.fromAsset,
                fromValue = state.fromAmount!!,
                toAsset = state.toAsset!!,
                toValue = state.toAmount!!,
                currency = state.currency,
            )
        } else {
            AmountListHead(
                amount = state.amount,
                equivalent = state.amountEquivalent,
            )
        }
        Table(state.cells)
        if (!state.txHash.isNullOrEmpty()) {
            onFinish(state.txHash)
        }
    }
}