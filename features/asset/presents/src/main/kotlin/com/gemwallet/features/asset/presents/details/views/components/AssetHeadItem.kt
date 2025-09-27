package com.gemwallet.features.asset.presents.details.views.components

import androidx.compose.runtime.Composable
import com.gemwallet.android.ext.asset
import com.gemwallet.android.ui.components.list_head.AmountListHead
import com.gemwallet.android.ui.components.list_head.AssetHeadActions
import com.gemwallet.android.ui.models.actions.AssetIdAction
import com.gemwallet.features.asset.viewmodels.details.models.AssetInfoUIModel
import com.wallet.core.primitives.AssetId
import com.wallet.core.primitives.AssetType
import com.wallet.core.primitives.WalletType

@Composable
internal fun AssetHeadItem(
    uiState: AssetInfoUIModel,
    isOperationEnabled: Boolean,
    onTransfer: AssetIdAction,
    onReceive: (AssetId) -> Unit,
    onBuy: (AssetId) -> Unit,
    onSwap: (AssetId, AssetId?) -> Unit,
) {
    AmountListHead(
        amount = uiState.accountInfoUIModel.totalBalance,
        equivalent = uiState.accountInfoUIModel.totalFiat,
        icon = uiState.asset,
    ) {
        AssetHeadActions(
            walletType = uiState.accountInfoUIModel.walletType,
            transferEnabled = uiState.accountInfoUIModel.walletType != WalletType.view,
            operationsEnabled = isOperationEnabled,
            onTransfer = { onTransfer(uiState.asset.id) },
            onReceive = { onReceive(uiState.asset.id) },
            onBuy = if (uiState.isBuyEnabled) {
                { onBuy(uiState.asset.id) }
            } else {
                null
            },
            onSwap = if (uiState.isSwapEnabled && uiState.accountInfoUIModel.walletType != WalletType.view) {
                {
                    val toAssetId = if (uiState.asset.type == AssetType.NATIVE) {
                        null
                    } else {
                        uiState.asset.id.chain.asset().id
                    }
                    onSwap(uiState.asset.id, toAssetId)
                }
            } else {
                null
            },
        )
    }
}