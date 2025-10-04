package com.gemwallet.features.bridge.views

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gemwallet.android.ui.R
import com.gemwallet.android.ui.components.buttons.MainActionButton
import com.gemwallet.android.ui.components.image.AsyncImage
import com.gemwallet.android.ui.components.list_item.WalletItem
import com.gemwallet.android.ui.components.list_item.property.DataBadgeChevron
import com.gemwallet.android.ui.components.list_item.property.PropertyDataText
import com.gemwallet.android.ui.components.list_item.property.PropertyItem
import com.gemwallet.android.ui.components.list_item.property.PropertyTitleText
import com.gemwallet.android.ui.components.screen.FatalStateScene
import com.gemwallet.android.ui.components.screen.LoadingScene
import com.gemwallet.android.ui.components.screen.ModalBottomSheet
import com.gemwallet.android.ui.components.screen.Scene
import com.gemwallet.android.ui.models.ListPosition
import com.gemwallet.features.bridge.viewmodels.ProposalSceneState
import com.gemwallet.features.bridge.viewmodels.ProposalSceneViewModel
import com.gemwallet.features.bridge.viewmodels.model.SessionUI
import com.reown.walletkit.client.Wallet

@Composable
fun ProposalScene(
    proposal: Wallet.Model.SessionProposal,
    onCancel: () -> Unit,
) {
    val viewModel: ProposalSceneViewModel = hiltViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val peer by viewModel.proposal.collectAsStateWithLifecycle()
    val selectedWallet by viewModel.selectedWallet.collectAsStateWithLifecycle()
    val availableWallets by viewModel.availableWallets.collectAsStateWithLifecycle()

    DisposableEffect(key1 = proposal) {
        viewModel.onProposal(proposal)

        onDispose { viewModel.reset() }
    }

    when {
        state is ProposalSceneState.Canceled -> onCancel()
        state is ProposalSceneState.Fail -> FatalStateScene(
            title = stringResource(id = R.string.wallet_connect_connect_title),
            message = (state as ProposalSceneState.Fail).message,
            onCancel = onCancel,
        )
        peer == null && state is ProposalSceneState.Init -> LoadingScene(stringResource(id = R.string.wallet_connect_connect_title), onCancel)
        else -> Proposal(
            peer = peer!!,
            selectedWallet = selectedWallet,
            availableWallets = availableWallets,
            onReject = viewModel::onReject,
            onApprove = viewModel::onApprove,
            onWalletSelected = viewModel::onWalletSelected
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Proposal(
    peer: SessionUI,
    selectedWallet: com.wallet.core.primitives.Wallet?,
    availableWallets: List<com.wallet.core.primitives.Wallet>,
    onReject: () -> Unit,
    onApprove: () -> Unit,
    onWalletSelected: (String) -> Unit,
) {
    var isShowSelectWallets by remember { mutableStateOf(false) }

    Scene(
        title = stringResource(id = R.string.wallet_connect_connect_title),
        mainAction = {
            MainActionButton(
                enabled = selectedWallet != null,
                title = stringResource(id = R.string.transfer_confirm),
                onClick = onApprove
            )
        },
        onClose = onReject,
    ) {
        AsyncImage(
            model = peer.icon,
            size = 74.dp,
            placeholderText = peer.name.takeIf { it.isNotEmpty() }?.substring(0..1) ?: "C",
            contentDescription = "peer_icon"
        )
        Spacer(modifier = Modifier.size(20.dp))
        PropertyItem(
            modifier = Modifier.clickable { isShowSelectWallets = true },
            title = { PropertyTitleText(R.string.common_wallet) },
            data = { PropertyDataText(selectedWallet?.name ?: "", badge = { DataBadgeChevron() })},
            listPosition = ListPosition.First,
        )
        PropertyItem(R.string.wallet_connect_app, peer.name, listPosition = ListPosition.Middle)
        PropertyItem(R.string.wallet_connect_website, peer.uri, listPosition = ListPosition.Last)
    }

    if (isShowSelectWallets) {
        ModalBottomSheet(
            dragHandle = { BottomSheetDefaults.DragHandle() },
            onDismissRequest = { isShowSelectWallets = false },
        ) {
            LazyColumn {
                itemsIndexed(availableWallets) { index, item ->
                    WalletItem(
                        wallet = item,
                        isCurrent = item.id == selectedWallet?.id,
                        listPosition = ListPosition.getPosition(index, availableWallets.size),
                        modifier = Modifier.clickable {
                            onWalletSelected(item.id)
                            isShowSelectWallets = false
                        }
                    )
                }
            }
        }
    }
}