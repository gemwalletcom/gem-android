package com.gemwallet.features.referral.views.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.gemwallet.android.math.getRelativeDate
import com.gemwallet.android.ui.R
import com.gemwallet.android.ui.components.buttons.MainActionButton
import com.gemwallet.android.ui.components.list_item.ListItemSupportText
import com.gemwallet.android.ui.components.list_item.listItem
import com.gemwallet.android.ui.components.list_item.property.PropertyTitleText
import com.gemwallet.android.ui.models.ListPosition
import com.gemwallet.android.ui.theme.WalletTheme
import com.gemwallet.android.ui.theme.paddingDefault
import com.gemwallet.android.ui.theme.paddingHalfSmall
import com.gemwallet.android.ui.theme.paddingSmall
import com.gemwallet.android.ui.theme.pendingColor
import com.gemwallet.android.ui.theme.trailingIconSmall
import com.wallet.core.primitives.ReferralAllowance
import com.wallet.core.primitives.ReferralQuota
import com.wallet.core.primitives.Rewards

internal fun LazyListScope.referralConfirmCode(rewards: Rewards, onConfirm: (String) -> Unit) {
    val code = rewards.usedReferralCode ?: return
    val pendingDate = rewards.pendingVerificationAfter ?: return
    val isEnable = System.currentTimeMillis() > pendingDate
    item {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .listItem(ListPosition.Single).padding(paddingDefault),
            verticalArrangement = Arrangement.spacedBy(paddingHalfSmall)
        ) {
            PropertyTitleText(
                text = R.string.rewards_pending_title,
                trailing = {
                    Icon(
                        modifier = Modifier.size(trailingIconSmall),
                        imageVector = Icons.Default.Info,
                        tint = pendingColor,
                        contentDescription = "",
                    )
                }
            )
            ListItemSupportText(
                if (isEnable) {
                    stringResource(R.string.rewards_pending_description_ready)
                } else {
                    stringResource(R.string.rewards_pending_description, getRelativeDate(pendingDate))
                }
            )
            HorizontalDivider(modifier = Modifier.padding(vertical = paddingSmall), thickness = 0.5.dp)
            MainActionButton(
                title = stringResource(R.string.transfer_confirm),
                enabled = isEnable
            ) {
                onConfirm(code)
            }
        }
    }
}

@Preview
@Composable
private fun ReferralConfirmCodePedningPreview() {
    WalletTheme {
        LazyColumn {
            referralConfirmCode(
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
                    code = "some_code",
                    usedReferralCode = "some_code_1",
                    pendingVerificationAfter = System.currentTimeMillis() + 120000
                )
            ) {}
        }
    }
}

@Preview
@Composable
private fun ReferralConfirmCodeReadyPreview() {
    WalletTheme {
        LazyColumn {
            referralConfirmCode(
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
                    code = "some_code",
                    usedReferralCode = "some_code_1",
                    pendingVerificationAfter = System.currentTimeMillis() - 120000
                )
            ) {}
        }
    }
}