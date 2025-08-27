package com.gemwallet.features.stake.presents.views.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.gemwallet.android.ext.claimed
import com.gemwallet.android.model.AmountParams
import com.gemwallet.android.ui.R
import com.gemwallet.android.ui.components.list_item.SubheaderItem
import com.gemwallet.android.ui.components.list_item.property.DataBadgeChevron
import com.gemwallet.android.ui.components.list_item.property.PropertyDataText
import com.gemwallet.android.ui.components.list_item.property.PropertyItem
import com.gemwallet.android.ui.components.list_item.property.PropertyTitleText
import com.gemwallet.android.ui.models.actions.AmountTransactionAction
import com.gemwallet.android.ui.theme.Spacer16
import com.wallet.core.primitives.AssetId
import com.wallet.core.primitives.StakeChain
import com.wallet.core.primitives.TransactionType
import com.wallet.core.primitives.WalletType

internal fun LazyListScope.stakeActions(
    assetId: AssetId,
    stakeChain: StakeChain,
    rewardsAmount: String,
    hasRewards: Boolean,
    amountAction: AmountTransactionAction,
    walletType: WalletType,
    onConfirm: () -> Unit
) {
    if (walletType == WalletType.view) {
        return
    }
    item {
        Spacer16()
        SubheaderItem(title = stringResource(R.string.common_manage))
    }
    item {
        PropertyItem(
            modifier = Modifier.clickable {
                amountAction(
                    AmountParams.Companion.buildStake(
                        assetId = assetId,
                        txType = TransactionType.StakeDelegate,
                    )
                )
            },
            title = { PropertyTitleText(R.string.transfer_stake_title) },
            data = { PropertyDataText("", badge = { DataBadgeChevron() }) },
        )
    }
    item {
        if (!hasRewards || !stakeChain.claimed()) {
            return@item
        }
        PropertyItem(
            modifier = Modifier.clickable(onClick = onConfirm),
            title = { PropertyTitleText(R.string.transfer_claim_rewards_title) },
            data = { PropertyDataText(rewardsAmount, badge = { DataBadgeChevron() }) },
        )
    }
}