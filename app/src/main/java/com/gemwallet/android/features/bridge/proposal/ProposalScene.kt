package com.gemwallet.android.features.bridge.proposal

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gemwallet.android.features.bridge.model.PeerUI
import com.gemwallet.android.features.wallets.components.WalletItem
import com.gemwallet.android.ui.R
import com.gemwallet.android.ui.components.CellEntity
import com.gemwallet.android.ui.components.FatalStateScene
import com.gemwallet.android.ui.components.LoadingScene
import com.gemwallet.android.ui.components.Table
import com.gemwallet.android.ui.components.buttons.MainActionButton
import com.gemwallet.android.ui.components.image.AsyncImage
import com.gemwallet.android.ui.components.screen.ModalBottomSheet
import com.gemwallet.android.ui.components.screen.Scene
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
    peer: PeerUI,
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
            model = peer.peerIcon,
            size = 74.dp,
            placeholderText = peer.peerName.substring(0..1),
            contentDescription = "peer_icon"
        )
        Spacer(modifier = Modifier.size(20.dp))
        Table(
            items = listOf(
                CellEntity(
                    label = stringResource(id = R.string.common_wallet),
                    data = selectedWallet?.name ?: "",
                    action = { isShowSelectWallets = true },
                ),
                CellEntity(
                    label = stringResource(id = R.string.wallet_connect_app),
                    data = peer.peerName,
                ),
                CellEntity(
                    label = stringResource(id = R.string.wallet_connect_website),
                    data = peer.peerUri,
                ),
            ),
        )
        Spacer(modifier = Modifier.size(24.dp))
    }

    if (isShowSelectWallets) {
        ModalBottomSheet(
            dragHandle = { BottomSheetDefaults.DragHandle() },
            onDismissRequest = { isShowSelectWallets = false },
        ) {
            LazyColumn {
                items(availableWallets) {
                    WalletItem(
                        wallet = it,
                        isCurrent = it.id == selectedWallet?.id,
                        modifier = Modifier.clickable {
                            onWalletSelected(it.id)
                            isShowSelectWallets = false
                        }
                    )
                }
            }
        }
    }
}