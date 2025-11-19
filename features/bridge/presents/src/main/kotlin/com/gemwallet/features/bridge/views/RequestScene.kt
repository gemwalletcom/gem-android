@file:OptIn(ExperimentalMaterial3Api::class)

package com.gemwallet.features.bridge.views

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gemwallet.android.ui.R
import com.gemwallet.android.ui.components.buttons.MainActionButton
import com.gemwallet.android.ui.components.dialog.DialogBar
import com.gemwallet.android.ui.components.list_item.SubheaderItem
import com.gemwallet.android.ui.components.list_item.listItem
import com.gemwallet.android.ui.components.list_item.property.PropertyItem
import com.gemwallet.android.ui.components.list_item.property.PropertyNetworkItem
import com.gemwallet.android.ui.components.list_item.property.itemsPositioned
import com.gemwallet.android.ui.components.screen.FatalStateScene
import com.gemwallet.android.ui.components.screen.LoadingScene
import com.gemwallet.android.ui.components.screen.ModalBottomSheet
import com.gemwallet.android.ui.components.screen.Scene
import com.gemwallet.android.ui.models.ListPosition
import com.gemwallet.android.ui.models.actions.AssetIdAction
import com.gemwallet.android.ui.theme.paddingDefault
import com.gemwallet.features.bridge.viewmodels.RequestSceneState
import com.gemwallet.features.bridge.viewmodels.WCRequestViewModel
import com.gemwallet.features.bridge.viewmodels.model.WCRequest
import com.gemwallet.features.confirm.presents.ConfirmScreen
import com.reown.walletkit.client.Wallet
import uniffi.gemstone.MessagePreview

@Composable
fun RequestScene(
    request: Wallet.Model.SessionRequest,
    verifyContext: Wallet.Model.VerifyContext,
    onBuy: AssetIdAction,
    onCancel: () -> Unit,
) {
    val viewModel: WCRequestViewModel = hiltViewModel()

    DisposableEffect(request.request.id.toString()) {
        viewModel.onRequest(request, verifyContext, onCancel)

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
    var isShowFullMessage by remember { mutableStateOf(false) }

    val preview = request.decoder.preview()

    Scene(
        title = stringResource(id = R.string.wallet_connect_title),
        mainAction = {
            MainActionButton(title = stringResource(id = R.string.transfer_approve_title), onClick = onApprove)
        },
        onClose = onReject,
    ) {
        LazyColumn {
            item { PropertyItem(R.string.wallet_connect_app, "${request.name} (${request.uri})", listPosition = ListPosition.First) }
            item { PropertyItem(R.string.common_wallet, walletName, listPosition = ListPosition.Middle) }
            item { PropertyNetworkItem(request.chain, listPosition = ListPosition.Last) }

            when (preview) {
                is MessagePreview.Eip712 -> domainMessage(preview) { isShowFullMessage = true }
                is MessagePreview.Text -> textMessage(preview)
            }
        }

        if (isShowFullMessage) {
            ModalBottomSheet(
                onDismissRequest = { isShowFullMessage = false },
                dragHandle = { DialogBar(stringResource(R.string.common_done), { isShowFullMessage = false }) },
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(paddingDefault)
                ) {
                    Text(
                        modifier = Modifier.fillMaxWidth(),
                        text = request.decoder.plainPreview()
                    )
                }
            }
        }
    }
}

private fun LazyListScope.textMessage(message: MessagePreview.Text) {
    item {
        SubheaderItem(stringResource(R.string.sign_message_message))
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .listItem()
                .padding(paddingDefault),
            text = message.v1,
        )
    }
}

private fun LazyListScope.domainMessage(message: MessagePreview.Eip712, onFullMessage: () -> Unit) {
    val message = message.v1
    item {
        SubheaderItem(stringResource(R.string.wallet_connect_domain))
        PropertyItem(R.string.application_name, message.domain.name, listPosition = ListPosition.First)
        PropertyItem(R.string.asset_contract, message.domain.verifyingContract, listPosition = ListPosition.Last)
    }
    message.message.firstOrNull()?.let { section ->
        item { SubheaderItem(section.name) }
        itemsPositioned(section.values) { position, item ->
            PropertyItem(item.name, item.value, listPosition = position)
        }
    }
    item {
        PropertyItem(
            R.string.sign_message_view_full_message,
            listPosition = ListPosition.Single,
            onClick = onFullMessage,
        )
    }
}