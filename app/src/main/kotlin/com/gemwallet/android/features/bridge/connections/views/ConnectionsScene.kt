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
import androidx.compose.material.icons.outlined.Info
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
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gemwallet.android.features.bridge.connections.viewmodels.ConnectionsViewModel
import com.gemwallet.android.features.bridge.model.SessionUI
import com.gemwallet.android.ui.R
import com.gemwallet.android.ui.components.QrCodeRequest
import com.gemwallet.android.ui.components.clipboard.getPlainText
import com.gemwallet.android.ui.components.image.IconWithBadge
import com.gemwallet.android.ui.components.list_item.ListItem
import com.gemwallet.android.ui.components.list_item.ListItemSupportText
import com.gemwallet.android.ui.components.list_item.ListItemTitleText
import com.gemwallet.android.ui.components.open
import com.gemwallet.android.ui.components.screen.Scene
import com.wallet.core.primitives.WalletConnection
import kotlinx.coroutines.launch
import uniffi.gemstone.Config
import uniffi.gemstone.DocsUrl
import java.text.DateFormat
import java.util.Date

@Composable
fun ConnectionsScene(
    onConnection: (String) -> Unit,
    onCancel: () -> Unit,
    viewModel: ConnectionsViewModel = hiltViewModel()
) {
    val clipboardManager = LocalClipboard.current.nativeClipboard
    var scannerShowed by remember { mutableStateOf(false) }
    val uriHandler = LocalUriHandler.current

    val connections by viewModel.connections.collectAsStateWithLifecycle()

    var pairError by remember { mutableStateOf("") }

    val connectionToastText = stringResource(id = R.string.wallet_connect_connection_title)
    val scope = rememberCoroutineScope()
    val snackbar = remember { SnackbarHostState() }

    Scene(
        title = stringResource(id = R.string.wallet_connect_title),
        backHandle = true,
        snackbar = snackbar,
        actions = {
            IconButton(
                onClick = {
                    viewModel.addPairing(
                        clipboardManager.getPlainText() ?: return@IconButton,
                        { scope.launch { snackbar.showSnackbar(message = connectionToastText) } },
                        { pairError = it }
                    )
                }
            ) {
                Icon(imageVector = Icons.Default.ContentPaste, contentDescription = "paste_uri")
            }
            IconButton(onClick = { scannerShowed  = true }) {
                Icon(imageVector = Icons.Default.QrCodeScanner, contentDescription = "scan_qr")
            }
            IconButton(onClick = { uriHandler.open(Config().getDocsUrl(DocsUrl.WALLET_CONNECT)) }) {
                Icon(imageVector = Icons.Outlined.Info, contentDescription = "WC_INFO")
            }
        },
        onClose = onCancel,
    ) {
        if (connections.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize()) {
                Text(modifier = Modifier.align(Alignment.Center), text = stringResource(id = R.string.wallet_connect_no_active_connections))
            }
        } else {
            LazyColumn {
                items(connections) { connection ->
                    ConnectionItem(connection, onConnection)
                }
            }
        }
    }

    if (scannerShowed) {
        QrCodeRequest(onCancel = { scannerShowed = false }) {
            viewModel.addPairing(it, onSuccess = {}, onError = {})
            scannerShowed = false
        }
    }

    if (pairError.isNotEmpty()) {
        AlertDialog(
            onDismissRequest = {  pairError = "" },
            confirmButton = {
                Button(onClick = { pairError = "" }) { Text(text = stringResource(id = R.string.common_done)) }
            },
            text = { Text(text = pairError) }
        )
    }
}

@Composable
fun ConnectionItem(
    connection: WalletConnection,
    onClick: ((String) -> Unit)? = null,
) {
    ConnectionItem(
        SessionUI(
            icon = connection.session.metadata.icon,
            name = connection.session.metadata.name,
            uri = connection.session.metadata.url,
            id = connection.session.id,
            expire = DateFormat.getDateInstance(DateFormat.MEDIUM).format(Date(connection.session.expireAt)),
        ),
        onClick,
    )
}

@Composable
fun ConnectionItem(
    connection: SessionUI,
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