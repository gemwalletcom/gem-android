package com.gemwallet.android.features.settings.networks.views

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircleOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gemwallet.android.ext.asset
import com.gemwallet.android.features.settings.networks.viewmodels.AddNodeViewModel
import com.gemwallet.android.ui.R
import com.gemwallet.android.ui.components.list_item.AssetListItem
import com.gemwallet.android.ui.components.QrCodeRequest
import com.gemwallet.android.ui.components.TransferTextFieldActions
import com.gemwallet.android.ui.components.buttons.MainActionButton
import com.gemwallet.android.ui.components.clipboard.getPlainText
import com.gemwallet.android.ui.components.designsystem.Spacer16
import com.gemwallet.android.ui.components.list_item.property.PropertyDataText
import com.gemwallet.android.ui.components.list_item.property.PropertyItem
import com.gemwallet.android.ui.components.list_item.property.PropertyTitleText
import com.gemwallet.android.ui.components.screen.Scene
import com.wallet.core.primitives.Chain
import java.text.NumberFormat

@Composable
fun AddNodeScene(chain: Chain, onCancel: () -> Unit) {
    val viewModel: AddNodeViewModel = hiltViewModel()
    val uiModel by viewModel.uiModel.collectAsStateWithLifecycle()

    DisposableEffect(chain) {
        viewModel.init(chain)

        onDispose {  }
    }

    var isShowQRScan by remember { mutableStateOf(false) }

    BackHandler {
        onCancel()
    }

    Scene(
        title = stringResource(id = R.string.nodes_import_node_title),
        mainAction = {
            MainActionButton(
                title = stringResource(id = R.string.wallet_import_action),
                enabled = uiModel.status != null,
                loading = uiModel.checking,
            ) {
                viewModel.addUrl()
                onCancel()
            }
        },
        onClose = onCancel,
    ) {
        val asset = chain.asset()
        AssetListItem(
            modifier = Modifier.height(74.dp),
            asset = asset,
            dividerShowed = false,
        )
        UrlField(
            value = viewModel.url,
            onValueChange = viewModel::onUrlChange,
            onQRScan = {
                isShowQRScan = true
            }
        )
        Spacer16()
        if (uiModel.status != null) {
            val nf = NumberFormat.getInstance()

            PropertyItem(R.string.nodes_import_node_chain_id, uiModel.status?.chainId ?: "")
            PropertyItem(
                title = {
                    PropertyTitleText(R.string.nodes_import_node_in_sync)
                },
                data = {
                    PropertyDataText(
                        "",
                        badge = {
                            if (uiModel.status?.inSync == true) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircleOutline,
                                    tint = MaterialTheme.colorScheme.tertiary,
                                    contentDescription = ""
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Cancel,
                                    tint = MaterialTheme.colorScheme.error,
                                    contentDescription = ""
                                )
                            }
                        }
                    )
                },
            )
            PropertyItem(R.string.nodes_import_node_latest_block, nf.format(uiModel.status?.blockNumber?.toLong() ?: ""))
            PropertyItem(R.string.nodes_import_node_latency, stringResource(R.string.common_latency_in_ms, uiModel.status?.latency ?: 0))
        }
    }

    if (isShowQRScan) {
        QrCodeRequest(onCancel = { isShowQRScan = false }) {
            isShowQRScan = false
            viewModel.url.value = it
            viewModel.onUrlChange()
        }
    }
}

@Composable
private fun UrlField(
    value: MutableState<String> = mutableStateOf(""),
    onValueChange: () -> Unit,
    onQRScan: () -> Unit,
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val clipboardManager = LocalClipboard.current.nativeClipboard
    OutlinedTextField(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth()
            .onFocusChanged {
                if (it.hasFocus) keyboardController?.show() else keyboardController?.hide()
            },
        value = value.value,
        singleLine = true,
        label = { Text("URL") },
        onValueChange = { newValue ->
            value.value = newValue
            onValueChange()
        },
        trailingIcon = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TransferTextFieldActions(
                    value = value.value,
                    paste = {
                        value.value = clipboardManager.getPlainText() ?: ""
                        onValueChange()
                    },
                    onClean = { value.value = "" },
                    qrScanner = onQRScan
                )
            }
        }
    )
}