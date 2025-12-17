@file:OptIn(ExperimentalMaterial3Api::class)

package com.gemwallet.features.referral.views

import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gemwallet.android.ui.R
import com.gemwallet.android.ui.components.list_item.WalletItem
import com.gemwallet.android.ui.components.screen.LoadingScene
import com.gemwallet.android.ui.components.screen.ModalBottomSheet
import com.gemwallet.android.ui.models.ListPosition
import com.gemwallet.features.referral.viewmodels.ReferralViewModel
import com.gemwallet.features.referral.viewmodels.SyncType
import kotlinx.coroutines.flow.update

@Composable
fun ReferralNavScreen(
    onClose: () -> Unit,
    viewModel: ReferralViewModel = hiltViewModel(),
) {
    var isShowSelectWallets by remember { mutableStateOf(false) }

    val availableWallets by viewModel.availableWallets.collectAsStateWithLifecycle()
    val currentWallet by viewModel.currentWallet.collectAsStateWithLifecycle()
    val rewards by viewModel.rewards.collectAsStateWithLifecycle()
    val inSync by viewModel.inSync.collectAsStateWithLifecycle()

    if (inSync == SyncType.Init) {
        LoadingScene(stringResource(R.string.rewards_title), onClose)
    }
    ReferralScene(
        inSync = inSync,
        isAvailableWalletSelect = availableWallets.size > 1,
        rewards = rewards,
        currentWallet = currentWallet,
        joinPointsCost = 100,
        onUsername = viewModel::createReferral,
        onCode = viewModel::useCode,
        onRefresh = viewModel::sync,
        onWallet = { isShowSelectWallets = true },
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
                            viewModel.currentWallet.update { item }
                            isShowSelectWallets = false
                        }
                    )
                }
            }
        }
    }
}
