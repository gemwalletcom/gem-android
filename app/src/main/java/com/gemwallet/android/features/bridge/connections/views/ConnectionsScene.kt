package com.gemwallet.android.features.bridge.connections.views

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gemwallet.android.R
import com.gemwallet.android.features.bridge.connections.viewmodels.ConnectionsViewModel
import com.gemwallet.android.features.bridge.model.ConnectionUI
import com.gemwallet.android.ui.components.ListItem
import com.gemwallet.android.ui.components.image.IconWithBadge
import com.gemwallet.android.ui.components.list_item.ListItemSupportText
import com.gemwallet.android.ui.components.list_item.ListItemTitleText
import com.gemwallet.android.ui.components.qrCodeRequest
import com.gemwallet.android.ui.components.screen.Scene
import kotlinx.coroutines.launch

@Composable
fun ConnectionsScene(
    onConnection: (String) -> Unit,
    onCancel: () -> Unit,
    viewModel: ConnectionsViewModel = hiltViewModel()
) {
    val clipboardManager = LocalClipboardManager.current
    var scannerShowed by remember {
        mutableStateOf(false)
    }

    val state by viewModel.sceneState.collectAsStateWithLifecycle()

    val connectionToastText = stringResource(id = R.string.wallet_connect_connection_title)
    val scope = rememberCoroutineScope()
    val snackbar = remember {
        SnackbarHostState()
    }

    Scene(
        title = stringResource(id = R.string.wallet_connect_title),
        backHandle = true,
        snackbar = snackbar,
        actions = {
            IconButton(
                onClick = {
                    viewModel.addPairing(clipboardManager.getText()?.text ?: return@IconButton) {
                        scope.launch {
                            snackbar.showSnackbar(
                                message = connectionToastText
                            )
                        }
                    }
                }
            ) {
                Icon(imageVector = Icons.Default.ContentPaste, contentDescription = "paste_uri")
            }
            IconButton(onClick = { scannerShowed  = true }) {
                Icon(imageVector = Icons.Default.QrCodeScanner, contentDescription = "scan_qr")
            }
        },
        onClose = onCancel,
    ) {
        if (state.connections.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize()) {
                Text(modifier = Modifier.align(Alignment.Center), text = stringResource(id = R.string.wallet_connect_no_active_connections))
            }
        } else {
            LazyColumn {
                items(state.connections) { connection ->
                    ConnectionItem(connection, onConnection)
                }
            }
        }
    }

    if (scannerShowed) {
        qrCodeRequest(onCancel = { scannerShowed = false }) {
            viewModel.addPairing(it, onSuccess = {})
            scannerShowed = false
        }
    }

    if (!state.pairError.isNullOrEmpty()) {
        AlertDialog(
            onDismissRequest = viewModel::resetErrors,
            confirmButton = {
                Button(onClick = viewModel::resetErrors) {
                    Text(text = stringResource(id = R.string.common_done))
                }
            },
            text = { Text(text = state.pairError!!) }
        )
    }
}

@Composable
fun ConnectionItem(
    connection: ConnectionUI,
    onClick: ((String) -> Unit)? = null,
) {
    ListItem(
        modifier = (if (onClick == null) Modifier else Modifier.clickable { onClick(connection.id) })
            .heightIn(72.dp),
        leading = {
            IconWithBadge(connection.icon, placeholder = if (connection.name.isEmpty()) "WC" else connection.name[0].toString())
        },
        title = { ListItemTitleText(connection.name) },
        subtitle = { ListItemSupportText(connection.uri) },
        dividerShowed = onClick != null,
    )
}