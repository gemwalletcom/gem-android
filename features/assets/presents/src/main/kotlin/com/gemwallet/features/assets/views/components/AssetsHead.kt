package com.gemwallet.features.assets.views.components

import androidx.compose.runtime.Composable
import com.gemwallet.android.domains.price.PriceState
import com.gemwallet.android.domains.wallet.aggregates.WalletSummaryAggregate
import com.gemwallet.android.ui.components.list_head.AmountListHead
import com.gemwallet.android.ui.components.list_head.AssetHeadActions
import com.wallet.core.primitives.AssetId

@Composable
internal fun AssetsHead(
    walletSummary: WalletSummaryAggregate?,
    onSendClick: () -> Unit,
    onReceiveClick: () -> Unit,
    onBuyClick: () -> Unit,
    onSwapClick: (AssetId?) -> Unit,
    onHideBalances: () -> Unit,
) {
    walletSummary ?: return // TODO: Amount list head and other should accept nullable values.

    AmountListHead(
        amount = walletSummary.walletTotalValue,
        changedValue = walletSummary.changedValue?.valueFormated,
        changedPercentages = walletSummary.changedValue?.changePercentageFormatted,
        changeState = walletSummary.changedValue?.state ?: PriceState.None,
        onHideBalances = onHideBalances,
        actions = {
            AssetHeadActions(
                walletType = walletSummary.walletType,
                transferEnabled = true,
                operationsEnabled = walletSummary.isOperationsAvailable,
                onTransfer = onSendClick,
                onReceive = onReceiveClick,
                onBuy = onBuyClick,
                onSwap = if (walletSummary.isSwapAvailable) {
                    { onSwapClick(null) }
                } else null
            )
        }
    )
}