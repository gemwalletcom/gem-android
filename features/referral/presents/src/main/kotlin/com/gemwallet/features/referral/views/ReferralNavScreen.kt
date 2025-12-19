@file:OptIn(ExperimentalMaterial3Api::class)

package com.gemwallet.features.referral.views

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gemwallet.android.domains.referral.values.ReferralError
import com.gemwallet.android.ui.R
import com.gemwallet.android.ui.components.list_item.WalletItem
import com.gemwallet.android.ui.components.screen.LoadingScene
import com.gemwallet.android.ui.components.screen.ModalBottomSheet
import com.gemwallet.android.ui.models.ListPosition
import com.gemwallet.features.referral.viewmodels.ReferralViewModel
import com.gemwallet.features.referral.viewmodels.SyncType

@Composable
fun ReferralNavScreen(
    onClose: () -> Unit,
    viewModel: ReferralViewModel = hiltViewModel(),
) {
    val context = LocalContext.current

    var isShowSelectWallets by remember { mutableStateOf(false) }
    var showErrorDialog by remember { mutableStateOf<Throwable?>(null) }

    val availableWallets by viewModel.availableWallets.collectAsStateWithLifecycle()
    val currentWallet by viewModel.currentWallet.collectAsStateWithLifecycle()
    val rewards by viewModel.rewards.collectAsStateWithLifecycle()
    val inSync by viewModel.inSync.collectAsStateWithLifecycle()
    val referralCode by viewModel.referralCode.collectAsStateWithLifecycle()

    if (inSync == SyncType.Init) {
        LoadingScene(stringResource(R.string.rewards_title), onClose)
    }
    ReferralScene(
        inSync = inSync,
        isAvailableWalletSelect = availableWallets.size > 1,
        referralCode = referralCode,
        rewards = rewards,
        currentWallet = currentWallet,
        joinPointsCost = 100,
        onUsername = viewModel::createReferral,
        onCode = viewModel::useCode,
        onRefresh = viewModel::sync,
        onWallet = { isShowSelectWallets = true },
        onRedeem = {
            Toast.makeText(context, R.string.common_loading, Toast.LENGTH_SHORT).show()
            viewModel.redeem(it) { err ->
                if (err == null) {
                    Toast.makeText(context, R.string.common_done, Toast.LENGTH_LONG).show()
                } else {
                    showErrorDialog = err
                }

            }
        },
        onClose = onClose,
    )

    if (isShowSelectWallets) {
        ModalBottomSheet(
            dragHandle = { BottomSheetDefaults.DragHandle() },
            onDismissRequest = { isShowSelectWallets = false },
        ) {
            LazyColumn {
                itemsIndexed(availableWallets) { index, item ->
                    WalletItem(
                        wallet = item,
                        isCurrent = item.id == currentWallet?.id,
                        listPosition = ListPosition.getPosition(index, availableWallets.size),
                        modifier = Modifier.clickable {
                            viewModel.setWallet(wallet = item)
                            isShowSelectWallets = false
                        }
                    )
                }
            }
        }
    }

    if (showErrorDialog != null) {
        val message = when (showErrorDialog) {
            is ReferralError.InsufficientPoints -> R.string.rewards_insufficient_points
            else -> R.string.transaction_status_failed
        }
        AlertDialog(
            containerColor = MaterialTheme.colorScheme.background,
            onDismissRequest = { showErrorDialog = null},
            text = {
                Text(
                    stringResource(message)
                )
            },
            confirmButton = {
                Button({ showErrorDialog = null}) { Text(stringResource(R.string.common_cancel)) }
            }
        )
    }
}
