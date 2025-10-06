package com.gemwallet.features.asset.viewmodels.details.models

import androidx.annotation.StringRes
import com.gemwallet.android.model.AssetInfo
import com.gemwallet.android.ui.R
import com.gemwallet.android.ui.models.PriceState
import com.wallet.core.primitives.Asset
import com.wallet.core.primitives.AssetType
import com.wallet.core.primitives.BalanceMetadata
import com.wallet.core.primitives.WalletType

class AssetInfoUIModel(
    val assetInfo: AssetInfo,
    val name: String = "",
    val iconUrl: String = "",
    val priceValue: String = "0",
    val priceDayChanges: String = "0",
    val priceChangedType: PriceState = PriceState.Up,
    val tokenType: AssetType = AssetType.NATIVE,
    val accountInfoUIModel: AccountInfoUIModel = AccountInfoUIModel(),
    val isBuyEnabled: Boolean = false,
    val isSwapEnabled: Boolean = false,
    val explorerName: String = "",
    val explorerAddressUrl: String? = null,
    val updated: Long = System.currentTimeMillis(),
) {

    val asset: Asset get() = assetInfo.asset

    data class AccountInfoUIModel(
        val walletType: WalletType = WalletType.view,
        val totalBalance: String = "0",
        val totalFiat: String = "0",
        val owner: String = "",
        val hasBalanceDetails: Boolean = false,
        val available: String = "0",
        val stake: String = "0",
        val reserved: String = "0",
        val balanceMetadata: BalanceMetadata? = null,
    ) {
        val balances: List<BalanceUIModel>
            get() = mutableListOf<BalanceUIModel>().apply {
                if (available.isNotEmpty() && available != "0") {
                    add(BalanceUIModel(BalanceViewType.Available, available))
                }
                if (stake.isNotEmpty() && stake != "0") {
                    add(BalanceUIModel(BalanceViewType.Stake, stake))
                }
                if (reserved.isNotEmpty() && reserved != "0") {
                    add(BalanceUIModel(BalanceViewType.Reserved, reserved))
                }
            }
    }

    data class BalanceUIModel(
        val type: BalanceViewType,
        val value: String = "0",
    )

    enum class BalanceViewType(@param:StringRes val label: Int) {
        Available(R.string.asset_balances_available),
        Stake(R.string.wallet_stake),
        Reserved(R.string.asset_balances_reserved)
    }
}