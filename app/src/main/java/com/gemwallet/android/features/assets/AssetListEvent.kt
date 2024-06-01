package com.gemwallet.android.features.assets

import com.wallet.core.primitives.AssetId

sealed class AssetListEvent {
    class BackClick(val onCancel: () -> Unit) : AssetListEvent()

    data object WalletSelectClick : AssetListEvent()

    data object CloseSubScreen : AssetListEvent()

    data object SettingsClick : AssetListEvent()

    data class ActivityClick(val txId: String? = null) : AssetListEvent()

    data object WalletConnectClick : AssetListEvent()

    data object ReceiveClick : AssetListEvent()

    data object TransferClick : AssetListEvent()

    data object BuyClick : AssetListEvent()

    data object ManageClick : AssetListEvent()

    data class AssetClick(val id: AssetId) : AssetListEvent()

    data class HideAssetClick(val id: AssetId) : AssetListEvent()
}