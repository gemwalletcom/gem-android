package com.gemwallet.features.referral.views

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.gemwallet.android.ui.R
import com.gemwallet.android.ui.components.GemTextField
import com.gemwallet.android.ui.components.buttons.MainActionButton
import com.gemwallet.android.ui.components.clickable
import com.gemwallet.android.ui.components.filters.FormDialog
import com.gemwallet.android.ui.components.list_item.SubheaderItem
import com.gemwallet.android.ui.components.list_item.listItem
import com.gemwallet.android.ui.components.list_item.property.PropertyItem
import com.gemwallet.android.ui.components.parseMarkdownToAnnotatedString
import com.gemwallet.android.ui.components.screen.Scene
import com.gemwallet.android.ui.models.ListPosition
import com.gemwallet.android.ui.theme.Spacer8
import com.gemwallet.android.ui.theme.WalletTheme
import com.gemwallet.android.ui.theme.paddingDefault
import com.gemwallet.android.ui.theme.paddingHalfSmall
import com.gemwallet.features.referral.viewmodels.SyncType
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
    onUsername: (String, (Exception?) -> Unit) -> Unit,
    onCode: (String, (Exception?) -> Unit) -> Unit,
    onRefresh: () -> Unit,
    onWallet: () -> Unit,
    onClose: () -> Unit
) {
    if (inSync == SyncType.Init) return

    val context = LocalContext.current
    val joinText = stringResource(R.string.rewards_share_text, "https://gemwallet.com/join?code=${rewards?.code}")
    val shareTitle = stringResource(id = R.string.common_share)

    val pullToRefreshState = rememberPullToRefreshState()
    var getStartedDialogShow by remember(rewards) { mutableStateOf(false) }
    var codeDialogShow by remember { mutableStateOf(false) }

    val onShare = fun () {
        val type = "text/plain"
        val subject = joinText

        val intent = Intent(Intent.ACTION_SEND)
        intent.type = type
        intent.putExtra(Intent.EXTRA_SUBJECT, subject)
        intent.putExtra(Intent.EXTRA_TEXT, rewards?.code)

        context.startActivity(Intent.createChooser(intent, shareTitle))
    }

    Scene(
        title = stringResource(R.string.rewards_title),
        actions = {
            if (isAvailableWalletSelect) {
                Row(
                    modifier = Modifier.padding(horizontal = paddingDefault).clickable(onWallet),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = currentWallet?.name ?: "",
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = "select wallet",
                        tint = MaterialTheme.colorScheme.primary,
                    )
                }
            }
        },
        onClose = onClose,
    ) {
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
                item {
                    Column(
                        modifier = Modifier
                            .listItem(ListPosition.Single)
                            .padding(paddingDefault),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(paddingDefault),
                    ) {
                        Text("\uD83C\uDF81", fontSize = 64.sp)
                        Text(
                            text = parseMarkdownToAnnotatedString(
                                markdown = stringResource(R.string.rewards_invite_friends_title)
                            ),
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            style = MaterialTheme.typography.headlineSmall
                        )
                        Text(
                            text = parseMarkdownToAnnotatedString(
                                markdown = stringResource(
                                    R.string.rewards_invite_friends_description,
                                    joinPointsCost
                                )
                            ),
                            color = MaterialTheme.colorScheme.secondary,
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(paddingDefault),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(
                                modifier = Modifier.weight(1f),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(paddingHalfSmall)
                            ) {
                                Text(
                                    text = "\uD83D\uDC65",
                                    fontSize = 26.sp
                                )
                                Text(
                                    text = stringResource(R.string.rewards_invite_friends_title),
                                    color = MaterialTheme.colorScheme.secondary,
                                    style = MaterialTheme.typography.bodyMedium,
                                    textAlign = TextAlign.Center
                                )
                            }
                            Column(
                                modifier = Modifier.weight(1f),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(paddingHalfSmall)
                            ) {
                                Text(
                                    text = "\uD83D\uDC8E",
                                    fontSize = 26.sp,
                                )
                                Text(
                                    text = stringResource(R.string.rewards_earn_points_title),
                                    color = MaterialTheme.colorScheme.secondary,
                                    style = MaterialTheme.typography.bodyMedium,
                                    textAlign = TextAlign.Center
                                )
                            }
                            Column(
                                modifier = Modifier.weight(1f),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(paddingHalfSmall)
                            ) {
                                Text(
                                    text = "\uD83C\uDF89",
                                    fontSize = 26.sp
                                )
                                Text(
                                    text = stringResource(R.string.rewards_get_rewards_title),
                                    color = MaterialTheme.colorScheme.secondary,
                                    style = MaterialTheme.typography.bodyMedium,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                        rewards?.let {
                            MainActionButton(
                                onClick = onShare
                            ) {
                                Icon(Icons.Default.Share, contentDescription = "share")
                                Spacer8()
                                Text(stringResource(R.string.rewards_invite_friends_title))
                            }
                        } ?: MainActionButton(
                            title = stringResource(R.string.common_get_started)
                        ) { getStartedDialogShow = true }
                    }
                }

                if (rewards == null) {
                    item {
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
                            title = R.string.rewards_points,
                            data = "${rewards.points} \uD83D\uDC65",
                            listPosition = ListPosition.Last
                        )
                    }
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
        ReferralCodeDialog(onCode) {
            codeDialogShow = false
        }
    }
}

@Composable
fun GetStartedDialog(
    onUsername: (String, (Exception?) -> Unit) -> Unit,
    onDismiss: () -> Unit,
) {
    var username by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf<Exception?>(null) }
    val dismissDialog: () -> Unit = {
        onDismiss()
        username = ""
    }
    FormDialog(
        title = stringResource(R.string.rewards_create_referral_code_title),
        onDismiss = dismissDialog,
        onDone = {
            onUsername(username) {
                if (it == null) {
                    dismissDialog()
                } else {
                    showError = it
                }
            }
        },
    ) {
        GemTextField(
            modifier = Modifier.fillMaxWidth(),
            label = stringResource(id = R.string.rewards_username),
            value = username,
            onValueChange = {
                username = it
            },
            singleLine = true,
        )
        Spacer8()
        Text(
            modifier = Modifier.fillMaxWidth(),
            text = stringResource(R.string.rewards_create_referral_code_info),
            color = MaterialTheme.colorScheme.secondary,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )
    }

    if (showError != null) {
        AlertDialog(
            onDismissRequest = { showError = null},
            confirmButton = {
                Button({ showError = null}) {
                    Text(stringResource(R.string.common_cancel))
                }
            },
            text = {
                Text(showError?.message ?: return@AlertDialog)
            }
        )
    }
}

@Composable
fun ReferralCodeDialog(
    onCode: (String, (Exception?) -> Unit) -> Unit,
    onDismiss: () -> Unit,
) {
    var code by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf<Exception?>(null) }
    val dismissDialog: () -> Unit = {
        onDismiss()
        code = ""
    }
    FormDialog(
        title = stringResource(R.string.rewards_referral_code),
        onDismiss = dismissDialog,
        onDone = {
            onCode(code) {
                if (it == null) {
                    dismissDialog()
                } else {
                    showError = it
                }
            }
        },
    ) {
        GemTextField(
            modifier = Modifier.fillMaxWidth(),
            label = stringResource(id = R.string.rewards_referral_code),
            value = code,
            onValueChange = {
                code = it
            },
            singleLine = true,
        )
    }
    if (showError != null) {
        AlertDialog(
            onDismissRequest = { showError = null},
            confirmButton = {
                Button({ showError = null}) {
                    Text(stringResource(R.string.common_cancel))
                }
            },
            text = {
                Text(showError?.message ?: return@AlertDialog)
            }
        )
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
                redemptionOptions = emptyList()
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
            onRefresh = {},
            onWallet = {},
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
            onRefresh = {},
            onWallet = {},
            onClose = {},
        )
    }
}

@Preview
@Composable
private fun GetStartedDialogPreview() {
    WalletTheme {
        GetStartedDialog(
            onUsername = { _, _ -> },
            onDismiss = {},
        )
    }
}

@Preview
@Composable
private fun ReferralCodeDialogPreview() {
    WalletTheme {
        ReferralCodeDialog(
            onCode = { _, _ -> },
            onDismiss = {},
        )
    }
}
