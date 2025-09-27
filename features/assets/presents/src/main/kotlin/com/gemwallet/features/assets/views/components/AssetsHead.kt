package com.gemwallet.features.assets.views.components

import androidx.compose.runtime.Composable
import com.gemwallet.android.ui.components.list_head.AmountListHead
import com.gemwallet.android.ui.components.list_head.AssetHeadActions
import com.gemwallet.features.assets.viewmodels.model.WalletInfoUIState

@Composable
internal fun AssetsHead(
    walletInfo: WalletInfoUIState,
    onSendClick: () -> Unit,
    onReceiveClick: () -> Unit,
    onBuyClick: () -> Unit,
    onHideBalances: () -> Unit,
) {
    AmountListHead(
        amount = walletInfo.totalValue,
        onHideBalances = onHideBalances,
        actions = {
            AssetHeadActions(
                walletType = walletInfo.type,
                transferEnabled = true,
                operationsEnabled = walletInfo.operationsEnabled,
                onTransfer = onSendClick,
                onReceive = onReceiveClick,
                onBuy = onBuyClick,
                onSwap = null, // if (swapEnabled) onSwapClick else null
            )
        }
    )
}