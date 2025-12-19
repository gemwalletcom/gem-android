package com.gemwallet.features.referral.views.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import com.gemwallet.android.model.format
import com.gemwallet.android.ui.R
import com.gemwallet.android.ui.components.list_item.SubheaderItem
import com.gemwallet.android.ui.components.list_item.property.PropertyItem
import com.gemwallet.android.ui.components.list_item.property.PropertyTitleText
import com.gemwallet.android.ui.components.list_item.property.itemsPositioned
import com.gemwallet.android.ui.models.ListPosition
import com.gemwallet.android.ui.theme.Spacer4
import com.wallet.core.primitives.RewardRedemptionOption
import com.wallet.core.primitives.Rewards

internal fun LazyListScope.referralInfo(
    rewards: Rewards,
    onRedeem: (RewardRedemptionOption) -> Unit,
) {
    item {
        SubheaderItem(stringResource(R.string.common_info))
        PropertyItem(
            title = R.string.rewards_my_referral_code,
            data = rewards.code,
            listPosition = ListPosition.First
        )
        PropertyItem(
            title = R.string.rewards_referrals,
            data = "${rewards.referralCount}",
            listPosition = ListPosition.Middle
        )
        PropertyItem(
            title = { PropertyTitleText(R.string.rewards_points) },
            data = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "${rewards.points}",
                        overflow = TextOverflow.MiddleEllipsis,
                        color = MaterialTheme.colorScheme.secondary,
                        style = MaterialTheme.typography.bodyLarge,
                    )
                    Spacer4()
                    Text(
                        text = "\uD83D\uDC8E",
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
            },
            listPosition = ListPosition.Last
        )
    }
    item { SubheaderItem(stringResource(R.string.rewards_ways_spend_title)) }
    itemsPositioned(rewards.redemptionOptions.filter { it.asset != null }) { position, item ->
        RewardRedemptionOptionItem(item, position) { onRedeem(item) }
    }
}

@Composable
private fun RewardRedemptionOptionItem(
    option: RewardRedemptionOption,
    listPosition: ListPosition = ListPosition.Middle,
    onClick: () -> Unit
) {
    PropertyItem(
        action = stringResource(R.string.rewards_ways_spend_asset_title, option.asset?.format(option.value.toBigInteger()) ?: ""),
        actionIconModel = option.asset,
        data = "${option.points} \uD83D\uDC8E",
        listPosition = listPosition,
        onClick = onClick,
    )
}