package com.gemwallet.android.features.bridge.proposal

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gemwallet.android.R
import com.gemwallet.android.features.wallets.components.WalletItem
import com.gemwallet.android.ui.components.AsyncImage
import com.gemwallet.android.ui.components.CellEntity
import com.gemwallet.android.ui.components.FatalStateScene
import com.gemwallet.android.ui.components.LoadingScene
import com.gemwallet.android.ui.components.MainActionButton
import com.gemwallet.android.ui.components.Scene
import com.gemwallet.android.ui.components.Table
import com.wallet.core.primitives.WalletType
import com.walletconnect.web3.wallet.client.Wallet

@Composable
fun ProposalScene(
    proposal: Wallet.Model.SessionProposal,
    onCancel: () -> Unit,
) {
    val viewModel: ProposalSceneViewModel = hiltViewModel()
    val uiState by viewModel.sceneState.collectAsStateWithLifecycle()

    DisposableEffect(key1 = proposal) {
        viewModel.onProposal(proposal)

        onDispose { viewModel.reset() }
    }

    when (uiState) {
        ProposalSceneState.Canceled -> onCancel()
        is ProposalSceneState.Fail -> FatalStateScene(
            title = stringResource(id = R.string.wallet_connect_connect_title),
            message = (uiState as ProposalSceneState.Fail).message,
            onCancel = onCancel,
        )
        ProposalSceneState.Init -> LoadingScene(stringResource(id = R.string.wallet_connect_connect_title), onCancel)
        is ProposalSceneState.Proposal -> Proposal(
            state = (uiState as ProposalSceneState.Proposal),
            onReject = viewModel::onReject,
            onApprove = viewModel::onApprove,
            onWalletSelect = viewModel::onWalletSelect,
            onWalletSelectCancel = viewModel::onWalletSelectCancel,
            onWalletSelected = viewModel::onWalletSelected
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Proposal(
    state: ProposalSceneState.Proposal,
    onReject: () -> Unit,
    onApprove: () -> Unit,
    onWalletSelect: () -> Unit,
    onWalletSelectCancel: () -> Unit,
    onWalletSelected: (String) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    Scene(
        title = stringResource(id = R.string.wallet_connect_connect_title),
        mainAction = {
            MainActionButton(
                enabled = state.walletType != WalletType.view,
                title = stringResource(id = R.string.transfer_confirm),
                onClick = onApprove
            )
        },
        onClose = onReject,
    ) {
        AsyncImage(
            modifier = Modifier.size(74.dp),
            model = state.peer.peerIcon,
            placeholderText = state.peer.peerName.substring(0..1),
            contentDescription = "peer_icon"
        )
        Spacer(modifier = Modifier.size(20.dp))
        Table(
            items = listOf(
                CellEntity(
                    label = stringResource(id = R.string.common_wallet),
                    data = state.walletName,
                    action = onWalletSelect,
                ),
                CellEntity(
                    label = stringResource(id = R.string.wallet_connect_app),
                    data = state.peer.peerName,
                ),
                CellEntity(
                    label = stringResource(id = R.string.wallet_connect_website),
                    data = state.peer.peerUri,
                ),
            ),
        )
        Spacer(modifier = Modifier.size(24.dp))
    }

    if (!state.wallets.isNullOrEmpty()) {
        ModalBottomSheet(
            onDismissRequest = {
                onWalletSelectCancel()
            },
            sheetState = sheetState,
            dragHandle = { Box {} },
            containerColor = MaterialTheme.colorScheme.background,
        ) {
            LazyColumn {
                items(state.wallets) {
                    WalletItem(
                        wallet = it,
                        isCurrent = it.id == state.walletId,
                        modifier = Modifier.clickable { onWalletSelected(it.id) }
                    )
                }
            }
        }
    }
}