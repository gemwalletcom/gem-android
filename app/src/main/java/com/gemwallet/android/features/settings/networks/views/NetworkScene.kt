package com.gemwallet.android.features.settings.networks.views

import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.gemwallet.android.BuildConfig
import com.gemwallet.android.R
import com.gemwallet.android.data.config.ConfigRepository
import com.gemwallet.android.ext.asset
import com.gemwallet.android.features.settings.networks.models.AddSourceType
import com.gemwallet.android.features.settings.networks.models.NetworksUIState
import com.gemwallet.android.ui.components.CellEntity
import com.gemwallet.android.ui.components.ListItem
import com.gemwallet.android.ui.components.Scene
import com.gemwallet.android.ui.components.SubheaderItem
import com.gemwallet.android.ui.components.Table
import com.gemwallet.android.ui.components.TransferTextFieldActions
import com.gemwallet.android.ui.theme.Spacer16
import com.gemwallet.android.ui.theme.padding8
import com.wallet.core.primitives.Node

@Composable
fun NetworkScene(
    state: NetworksUIState,
    onSelectNode: (Node) -> Unit,
    onAddSource: (AddSourceType) -> Unit,
    onCancel: () -> Unit,
) {
    val chain = state.chain ?: return
    Scene(title = chain.asset().name, onClose = onCancel) {
        LazyColumn {
            item {
                SubheaderItem(
                    title = stringResource(id = R.string.settings_networks_source),
                )
            }
            if (BuildConfig.DEBUG) {
                item {
                    UrlField("Add Network Source URL")
                    Spacer16()
                }
            }
            items(state.nodes) { node: Node ->
                ListItem(
                    modifier = Modifier.clickable { onSelectNode(node) },
                    dividerShowed = true,
                    trailing = {
                        if (node.url == state.currentNode?.url) {
                            Icon(
                                modifier = Modifier
                                    .padding(end = padding8)
                                    .size(20.dp),
                                imageVector = Icons.Default.Done,
                                contentDescription = ""
                            )
                        }
                    }
                ) {
                    Text(
                        text = if (node.url == ConfigRepository.getGemNodeUrl(state.chain)) {
                            "Gem Wallet Node"
                        } else {
                            node.url
                        },
                        maxLines = 1,
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
            }
            item {
                Spacer16()
                SubheaderItem(
                    title = stringResource(id = R.string.settings_networks_explorer),
                )
            }
            if (BuildConfig.DEBUG) {
                item {
                    UrlField("Add Explorer Source URL")
                    Spacer16()
                }
            }
            item {
                val uri = uniffi.Gemstone.Explorer().getTransactionUrl(state.chain.string, "")
                Table(
                    items = listOf(
                        CellEntity(
                            label = uniffi.Gemstone.Explorer().getNameByHost(Uri.parse(uri).host ?: "") ?: uri,
                            data = "",
                        )
                    )
                )
            }
        }
    }
}

@Composable
private fun UrlField(
    label: String,
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    var value by remember {
        mutableStateOf("")
    }
    OutlinedTextField(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth()
            .onFocusChanged {
                if (it.hasFocus) keyboardController?.show() else keyboardController?.hide()
            },
        value = value,
        singleLine = true,
        label = { Text(label) },
        onValueChange = { newValue ->
            value = newValue
        },
        trailingIcon = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
//                if (uiState.isLoading) {
//                    CircularProgressIndicator16()
//                    Spacer(modifier = Modifier.size(8.dp))
//                }
//                if (uiState.isResolve) {
//                    androidx.compose.material3.Icon(
//                        modifier = Modifier.size(24.dp),
//                        imageVector = Icons.Default.CheckCircle,
//                        contentDescription = "Name is resolved",
//                        tint = MaterialTheme.colorScheme.tertiary,
//                    )
//                    Spacer(modifier = Modifier.size(8.dp))
//                }
//                if (uiState.isFail) {
//                    androidx.compose.material3.Icon(
//                        modifier = Modifier.size(24.dp),
//                        imageVector = Icons.Default.Error,
//                        contentDescription = "Name is fail",
//                        tint = MaterialTheme.colorScheme.error,
//                    )
//                    Spacer(modifier = Modifier.size(8.dp))
//                }
                TransferTextFieldActions(
                    paste = { /*onValueChange(clipboardManager.getText()?.text ?: "", uiState.nameRecord)*/ },
                    qrScanner = null
                )
                IconButton(
                    onClick = { /*TODO*/ },
                    enabled = value.isNotEmpty()
                ) {
                    Icon(imageVector = Icons.Default.Save, contentDescription = "")
                }
            }
        }
    )
}