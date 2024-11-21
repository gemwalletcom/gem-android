package com.gemwallet.android.features.bridge.request

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gemwallet.android.R
import com.gemwallet.android.features.confirm.views.ConfirmScreen
import com.gemwallet.android.model.ConfirmParams
import com.gemwallet.android.model.DestinationAddress
import com.gemwallet.android.ui.components.CellEntity
import com.gemwallet.android.ui.components.FatalStateScene
import com.gemwallet.android.ui.components.LoadingScene
import com.gemwallet.android.ui.components.Table
import com.gemwallet.android.ui.components.buttons.MainActionButton
import com.gemwallet.android.ui.components.screen.Scene
import com.wallet.core.primitives.AssetId
import com.walletconnect.web3.wallet.client.Wallet

@Composable
fun RequestScene(
    request: Wallet.Model.SessionRequest,
    onCancel: () -> Unit,
) {
    val viewModel: RequestViewModel = hiltViewModel()

    DisposableEffect(request.request.id.toString()) {
        viewModel.onRequest(request)

        onDispose { viewModel.reset() }
    }

    val sceneState by viewModel.sceneState.collectAsStateWithLifecycle()

    when (sceneState) {
        RequestSceneState.Loading -> LoadingScene(stringResource(id = R.string.wallet_connect_title), onCancel)
        is RequestSceneState.Error -> FatalStateScene(
            title = stringResource(id = R.string.wallet_connect_title),
            message = (sceneState as RequestSceneState.Error).message,
            onCancel = viewModel::onReject
        )
        is RequestSceneState.SignMessage -> (sceneState as RequestSceneState.SignMessage).Render(
            onApprove = viewModel::onSign,
            onReject = viewModel::onReject,
        )
        is RequestSceneState.SendTransaction -> {
            ConfirmScreen(
                params = ConfirmParams.TransferParams(
                    assetId = AssetId((sceneState as RequestSceneState.SendTransaction).chain),
                    amount = (sceneState as RequestSceneState.SendTransaction).value,
                    destination = DestinationAddress(address = (sceneState as RequestSceneState.SendTransaction).to),
                    memo = (sceneState as RequestSceneState.SendTransaction).data,
                ),
                finishAction = { assetId, hash, route -> viewModel.onSent(hash) },
                cancelAction = viewModel::onReject
            )
        }

        RequestSceneState.Cancel -> onCancel()
    }
}

@Composable
private fun RequestSceneState.SignMessage.Render(
    onApprove: () -> Unit,
    onReject: () -> Unit,
) {
    Scene(
        title = stringResource(id = R.string.wallet_connect_title),
        mainAction = {
            MainActionButton(title = stringResource(id = R.string.transfer_approve_title), onClick = onApprove)
        },
        onClose = onReject,
    ) {
        LazyColumn {
            item {
                Table(
                    items = listOf(
                        CellEntity(
                            label = stringResource(id = R.string.asset_market_cap),
                            data = peer.peerName,
                        ),
                        CellEntity(
                            label = stringResource(id = R.string.asset_circulating_supply),
                            data = peer.peerDescription,
                        ),
                        CellEntity(
                            label = stringResource(id = R.string.asset_total_supply),
                            data = peer.peerUri,
                        ),
                    ),
                )
                Spacer(modifier = Modifier.size(24.dp))
            }
            item {
                Surface(
                    shadowElevation = 16.dp,
                    color = MaterialTheme.colorScheme.background,
                ) {
                    Text(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        text = params
                    )
                }
            }
        }
    }
}