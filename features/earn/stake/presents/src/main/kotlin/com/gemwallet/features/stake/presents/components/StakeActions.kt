package com.gemwallet.features.stake.presents.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.gemwallet.android.model.AmountParams
import com.gemwallet.android.ui.R
import com.gemwallet.android.ui.components.list_item.SubheaderItem
import com.gemwallet.android.ui.components.list_item.property.DataBadgeChevron
import com.gemwallet.android.ui.components.list_item.property.PropertyDataText
import com.gemwallet.android.ui.components.list_item.property.PropertyItem
import com.gemwallet.android.ui.components.list_item.property.PropertyTitleText
import com.gemwallet.android.ui.components.list_item.property.itemsPositioned
import com.gemwallet.android.ui.models.actions.AmountTransactionAction
import com.gemwallet.android.ui.theme.Spacer16
import com.gemwallet.features.stake.models.StakeAction
import com.wallet.core.primitives.AssetId

internal fun LazyListScope.stakeActions(
    actions: List<StakeAction>,
    assetId: AssetId,
    amountAction: AmountTransactionAction,
    onConfirm: () -> Unit
) {
    item {
        Spacer16()
        SubheaderItem(title = stringResource(R.string.common_manage))
    }
    itemsPositioned(actions) { position, item ->
        val title = when (item) {
            is StakeAction.Rewards -> R.string.transfer_rewards_title
            StakeAction.Stake -> R.string.transfer_stake_title
            StakeAction.Freeze -> R.string.transfer_freeze_title
            StakeAction.Unfreeze -> R.string.transfer_unfreeze_title
        }
        val onClick = when(item) {
            StakeAction.Stake,
            StakeAction.Freeze,
            StakeAction.Unfreeze -> {
                {
                    amountAction(
                        AmountParams.Companion.buildStake(
                            assetId = assetId,
                            txType = item.transactionType,
                        )
                    )
                }
            }
            is StakeAction.Rewards -> onConfirm
        }
        PropertyItem(
            modifier = Modifier.clickable(onClick = onClick),
            title = { PropertyTitleText(title) },
            data = { PropertyDataText(item.data ?: "", badge = { DataBadgeChevron() }) },
            listPosition = position
        )
    }
}