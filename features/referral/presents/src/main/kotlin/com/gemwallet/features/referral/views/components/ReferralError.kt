package com.gemwallet.features.referral.views.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.gemwallet.android.ui.R
import com.gemwallet.android.ui.components.list_item.ListItemSupportText
import com.gemwallet.android.ui.components.list_item.listItem
import com.gemwallet.android.ui.components.list_item.property.PropertyTitleText
import com.gemwallet.android.ui.models.ListPosition
import com.gemwallet.android.ui.theme.WalletTheme
import com.gemwallet.android.ui.theme.paddingDefault
import com.gemwallet.android.ui.theme.paddingHalfSmall
import com.gemwallet.android.ui.theme.trailingIconSmall
import com.wallet.core.primitives.ReferralAllowance
import com.wallet.core.primitives.ReferralQuota
import com.wallet.core.primitives.Rewards

internal fun LazyListScope.referralError(rewards: Rewards) {
    val reason = rewards.disableReason ?: return
    item {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .listItem(ListPosition.Single).padding(paddingDefault),
            verticalArrangement = Arrangement.spacedBy(paddingHalfSmall)
        ) {
            PropertyTitleText(
                text = R.string.errors_error_occured,
                trailing = {
                    Icon(
                        modifier = Modifier.size(trailingIconSmall),
                        imageVector = Icons.Default.WarningAmber,
                        tint = MaterialTheme.colorScheme.error,
                        contentDescription = "",
                    )
                }
            )
            ListItemSupportText(reason)
        }
    }
}

@Preview
@Composable
private fun ReferralErrorPreview() {
    WalletTheme {
        LazyColumn {
            referralError(
                Rewards(
                    referralCount = 0,
                    points = 0,
                    isEnabled = false,
                    verified = false,
                    redemptionOptions = emptyList(),
                    referralAllowance = ReferralAllowance(
                        daily = ReferralQuota(limit = 0, available = 0),
                        weekly = ReferralQuota(limit = 0, available = 0)
                    ),
                    disableReason = "Account verification required"
                )
            )
        }
    }
}