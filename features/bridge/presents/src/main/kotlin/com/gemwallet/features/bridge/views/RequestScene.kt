package com.gemwallet.features.bridge.views

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
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gemwallet.android.ui.R
import com.gemwallet.android.ui.components.buttons.MainActionButton
import com.gemwallet.android.ui.components.list_item.property.PropertyItem
import com.gemwallet.android.ui.components.list_item.property.PropertyNetworkItem
import com.gemwallet.android.ui.components.screen.FatalStateScene
import com.gemwallet.android.ui.components.screen.LoadingScene
import com.gemwallet.android.ui.components.screen.Scene
import com.gemwallet.android.ui.models.ListPosition
import com.gemwallet.android.ui.models.actions.AssetIdAction
import com.gemwallet.features.bridge.viewmodels.RequestSceneState
import com.gemwallet.features.bridge.viewmodels.WCRequestViewModel
import com.gemwallet.features.bridge.viewmodels.model.WCRequest
import com.gemwallet.features.confirm.presents.ConfirmScreen
import com.reown.walletkit.client.Wallet
import com.wallet.core.primitives.WalletConnectionMethods

@Composable
fun RequestScene(
    request: Wallet.Model.SessionRequest,
    onBuy: AssetIdAction,
    onCancel: () -> Unit,
) {
    val viewModel: WCRequestViewModel = hiltViewModel()

    DisposableEffect(request.request.id.toString()) {
        viewModel.onRequest(request, onCancel)

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
        is RequestSceneState.Request -> (sceneState as RequestSceneState.Request).let { sceneState ->
            when (sceneState.request) {
                is WCRequest.SignMessage -> SignMessageScene(
                    sceneState.walletName,
                    sceneState.request as WCRequest.SignMessage,
                    viewModel::onSign,
                    viewModel::onReject
                )
                is WCRequest.Transaction -> ConfirmScreen(
                    params = (sceneState.request as WCRequest.Transaction).confirmParams,
                    finishAction = { assetId, hash, route ->
                        when (sceneState.request) {
                            is WCRequest.Transaction.SendTransaction -> viewModel.onSent(hash)
                            is WCRequest.Transaction.SignTransaction -> viewModel.onSigned(hash)
                            else -> {}
                        }
                    },
                    onBuy = onBuy,
                    cancelAction = viewModel::onReject
                )
                is WCRequest.WalletSwitchEthereumChain -> return
            }


        }
//        is RequestSceneState.SignMessage -> Render(
//            request = (sceneState as RequestSceneState.SignMessage),
//            onApprove = viewModel::onSign,
//            onReject = viewModel::onReject,
//        )
//        is RequestSceneState.SendTransaction -> {
//            ConfirmScreen(
//                params = Native(
//                    from = (sceneState as RequestSceneState.SendTransaction).account,
//                    asset = (sceneState as RequestSceneState.SendTransaction).account.chain.asset(),
//                    amount = (sceneState as RequestSceneState.SendTransaction).value,
//                    destination = DestinationAddress(address = (sceneState as RequestSceneState.SendTransaction).to),
//                    memo = (sceneState as RequestSceneState.SendTransaction).data,
//                ),
//                finishAction = { assetId, hash, route -> viewModel.onSent(hash) },
//                onBuy = onBuy,
//                cancelAction = viewModel::onReject
//            )
//        }
//        is RequestSceneState.SendGeneric -> ConfirmScreen(
//            params = (sceneState as RequestSceneState.SendGeneric).params,
//            finishAction = { assetId, hash, route -> viewModel.onSent(hash) },
//            onBuy = onBuy,
//            cancelAction = viewModel::onReject
//        )
//        is RequestSceneState.SignGeneric -> ConfirmScreen(
//            params = (sceneState as RequestSceneState.SignGeneric).params,
//            finishAction = { assetId, hash, route -> viewModel.onSigned(hash) },
//            onBuy = onBuy,
//            cancelAction = viewModel::onReject
//        )
        RequestSceneState.Cancel -> onCancel()
    }
}

@Composable
private fun SignMessageScene(
    walletName: String,
    request: WCRequest.SignMessage,
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
            item { PropertyItem(R.string.wallet_connect_app, request.name, listPosition = ListPosition.First) }
            item { PropertyItem(R.string.wallet_connect_website, request.uri, listPosition = ListPosition.Middle) }
            item { PropertyItem(R.string.transfer_from, walletName, listPosition = ListPosition.Middle) }
            item { PropertyNetworkItem(request.chain, listPosition = ListPosition.Middle) }
            item { PropertyItem("Method", request.method, listPosition = ListPosition.Last) }
            when (request.method) {
                WalletConnectionMethods.SolanaSignTransaction.string -> {}
                else -> item {
                    Spacer(modifier = Modifier.size(24.dp))
                    Surface(shadowElevation = 16.dp, color = MaterialTheme.colorScheme.background) {
                        Text(modifier = Modifier.fillMaxWidth().padding(16.dp), text = request.decoder.plainPreview())
                    }
                }
            }
        }
    }
}