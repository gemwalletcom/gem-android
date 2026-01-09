package com.gemwallet.features.referral.views

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults.Indicator
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.gemwallet.android.ui.R
import com.gemwallet.android.ui.components.buttons.MainActionButton
import com.gemwallet.android.ui.components.clickable
import com.gemwallet.android.ui.components.screen.Scene
import com.gemwallet.android.ui.theme.Spacer8
import com.gemwallet.android.ui.theme.WalletTheme
import com.gemwallet.android.ui.theme.paddingDefault
import com.gemwallet.android.ui.theme.paddingSmall
import com.gemwallet.features.referral.viewmodels.SyncType
import com.gemwallet.features.referral.views.components.referralConfirmCode
import com.gemwallet.features.referral.views.components.referralError
import com.gemwallet.features.referral.views.components.referralHead
import com.gemwallet.features.referral.views.components.referralInfo
import com.gemwallet.features.referral.views.dialogs.GetStartedDialog
import com.gemwallet.features.referral.views.dialogs.ReferralCodeDialog
import com.wallet.core.primitives.ReferralAllowance
import com.wallet.core.primitives.ReferralQuota
import com.wallet.core.primitives.RewardRedemptionOption
import com.wallet.core.primitives.Rewards
import com.wallet.core.primitives.Wallet
import com.wallet.core.primitives.WalletSource
import com.wallet.core.primitives.WalletType

@Composable
fun ReferralScene(
    inSync: SyncType,
    isAvailableWalletSelect: Boolean,
    rewards: Rewards?,
    currentWallet: Wallet?,
    joinPointsCost: Int,
    referralCode: String? = null,
    onUsername: (String, (Exception?) -> Unit) -> Unit,
    onCode: (String, (Exception?) -> Unit) -> Unit,
    onCancelCode: () -> Unit,
    onRefresh: () -> Unit,
    onWallet: () -> Unit,
    onRedeem: (RewardRedemptionOption) -> Unit,
    onClose: () -> Unit
) {

    val context = LocalContext.current
    val link = "https://gemwallet.com/join?code=${rewards?.code}"
    val joinText = stringResource(R.string.rewards_share_text, link)
    val shareTitle = stringResource(id = R.string.common_share, link)

    val pullToRefreshState = rememberPullToRefreshState()
    var getStartedDialogShow by remember(rewards) { mutableStateOf(false) }
    var codeDialogShow by remember(referralCode, inSync) { mutableStateOf(referralCode != null && inSync == SyncType.None) }
    var referralCode by remember(referralCode) { mutableStateOf(referralCode) }

    val successStr = stringResource(R.string.common_done)

    val onShare = fun () {
        val type = "text/plain"
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = type
        intent.putExtra(Intent.EXTRA_SUBJECT, link)
        intent.putExtra(Intent.EXTRA_TEXT, joinText)

        context.startActivity(Intent.createChooser(intent, shareTitle))
    }

    Scene(
        title = stringResource(R.string.rewards_title),
        actions = {
            if (isAvailableWalletSelect) {
                Row(
                    modifier = Modifier
                        .widthIn(max = 250.dp)
                        .padding(horizontal = paddingDefault)
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(16.dp))
                        .clickable(onWallet)
                        .padding(start = paddingDefault, end = paddingSmall)
                        .padding(vertical = paddingSmall)
                    ,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = currentWallet?.name ?: "",
                        maxLines = 1,
                        overflow = TextOverflow.MiddleEllipsis,
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = "select wallet",
                        tint = MaterialTheme.colorScheme.onPrimary,
                    )
                }
            }
        },
        onClose = onClose,
    ) {
        if (inSync == SyncType.Init) {
            Box(modifier = Modifier.fillMaxSize()) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
            return@Scene
        }
        PullToRefreshBox(
            isRefreshing = inSync != SyncType.None,
            onRefresh = onRefresh,
            state = pullToRefreshState,
            indicator = {
                Indicator(
                    modifier = Modifier.align(Alignment.TopCenter),
                    isRefreshing = inSync != SyncType.None,
                    state = pullToRefreshState,
                    containerColor = MaterialTheme.colorScheme.background
                )
            }
        ) {
            LazyColumn {
                referralHead(
                    joinPointsCost = joinPointsCost,
                    rewards = rewards,
                    onGetStarted = { getStartedDialogShow = true },
                    onShare = onShare,
                )

                if (rewards == null) {
                    item {
                        Spacer8()
                        Box(modifier = Modifier.padding(horizontal = paddingDefault)) {
                            MainActionButton(
                                title = stringResource(R.string.rewards_activate_referral_code_title),
                                colors = ButtonDefaults.buttonColors()
                                    .copy(containerColor = Color.White, contentColor = Color.Black)
                            ) { codeDialogShow = true }
                        }
                        Spacer8()
                        Text(
                            modifier = Modifier.fillMaxWidth(),
                            text = stringResource(R.string.rewards_activate_referral_code_description),
                            color = MaterialTheme.colorScheme.secondary,
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    referralError(rewards)
                    referralConfirmCode(rewards) {
                        onCode(it) {
                            Toast.makeText(context, it?.message ?: successStr, Toast.LENGTH_SHORT).show()
                            onRefresh()
                        }
                    }
                    referralInfo(rewards, onRedeem)
                }
            }
        }
    }

    if (getStartedDialogShow) {
        GetStartedDialog(onUsername) {
            getStartedDialogShow = false
        }
    }

    if (codeDialogShow) {
        ReferralCodeDialog(referralCode = referralCode, onCode = onCode) {
            codeDialogShow = false
            onCancelCode()
        }
    }
}

@Preview
@Composable
private fun ReferralScenePreview() {
    WalletTheme {
        ReferralScene(
            inSync = SyncType.None,
            isAvailableWalletSelect = false,
            rewards = Rewards(
                code = "testuser",
                referralCount = 5,
                points = 1000,
                usedReferralCode = null,
                isEnabled = true,
                redemptionOptions = emptyList(),
                verified = true,
                referralAllowance = ReferralAllowance(
                    daily = ReferralQuota(0, 0),
                    weekly = ReferralQuota(0, 0),
                ),
            ),
            currentWallet = Wallet(
                id = "1",
                name = "Wallet 1",
                index = 0,
                type = WalletType.multicoin,
                accounts = emptyList(),
                order = 0,
                isPinned = false,
                imageUrl = null,
                source = WalletSource.Create
            ),
            joinPointsCost = 1000,
            onUsername = { _, _ -> },
            onCode = { _, _ -> },
            onCancelCode = {},
            onRefresh = {},
            onWallet = {},
            onRedeem = {},
            onClose = {},
        )
    }
}

@Preview
@Composable
private fun ReferralSceneNoRewardsPreview() {
    WalletTheme {
        ReferralScene(
            inSync = SyncType.None,
            isAvailableWalletSelect = false,
            rewards = null,
            currentWallet = Wallet(
                id = "1",
                name = "Wallet 1",
                index = 0,
                type = WalletType.multicoin,
                accounts = emptyList(),
                order = 0,
                isPinned = false,
                imageUrl = null,
                source = WalletSource.Create
            ),
            joinPointsCost = 1000,
            onUsername = { _, _ -> },
            onCode = { _, _ -> },
            onCancelCode = {},
            onRefresh = {},
            onWallet = {},
            onRedeem = {},
            onClose = {},
        )
    }
}